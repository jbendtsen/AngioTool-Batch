package Batch;

import java.util.Arrays;

public class Particles
{
    /*
    struct Shape {
        int areaAndColor;
        int firstPointOrReplacement;
        int pointMinX;
        int pointMinY;
        int pointMaxX;
        int pointMaxY;
    }
    */

    public static final int N_SHAPE_MEMBERS = 6;

    public static class Scratch
    {
        public IntVector shapes = new IntVector();
        public IntVector spanIndex = new IntVector();
    }

    public static void computeShapes(Scratch data, int[] regions, byte[] image, int width, int height)
    {
        data.shapes.size = 0;
        data.spanIndex.size = 0;

        IntVector spans = data.spanIndex;

        int area = width * height;
        Arrays.fill(regions, 0, area, 0);

        int startColor = image[0];
        spans.addTwo(0, 0);

        for (int y = 0; y < height; y++) {
            int prev = image[y*width];

            for (int x = 0; x < width; x++) {
                int color = image[x+width*y];
                if ((color ^ prev) < 0)
                    spans.addTwo(spans.size + 2, 0);

                int idx = spans.size - 2;
                if (y > 0 && (image[x+width*(y-1)] ^ color) >= 0) {
                    int aboveIdx = regions[x+width*(y-1)];
                    if (spans.buf[idx] > spans.buf[aboveIdx])
                        spans.buf[idx] = spans.buf[aboveIdx];
                    else if (spans.buf[idx] < spans.buf[aboveIdx])
                        spans.buf[aboveIdx] = spans.buf[idx];
                }

                regions[x+width*y] = idx;
                prev = color;
            }

            spans.addTwo(spans.size + 2, 0);
        }

        int start = 0;
        int end = height-1;
        int dir = 1;
        boolean anyCaptures;
        do {
            anyCaptures = false;

            start ^= height-1;
            end ^= height-1;
            dir *= -1;
            for (int y = start; y != end; y += dir) {
                for (int x = 0; x < width; x++) {
                    int idx = regions[x+width*y];
                    if ((image[x+width*y] ^ image[x+width*(y+dir)]) >= 0) {
                        int nextIdx = regions[x+width*(y+dir)];
                        if (spans.buf[idx] > spans.buf[nextIdx]) {
                            spans.buf[idx] = spans.buf[nextIdx];
                            anyCaptures = true;
                        }
                        else if (spans.buf[idx] < spans.buf[nextIdx]) {
                            spans.buf[nextIdx] = spans.buf[idx];
                            anyCaptures = true;
                        }
                    }
                }
            }
        } while (anyCaptures);

        int nUnique = 0;
        for (int i = 0; i < spans.size; i += 2) {
            int startingSpan = spans.buf[i];
            if (spans.buf[startingSpan+1] == 0)
                spans.buf[startingSpan+1] = ++nUnique;
        }

        data.shapes.resize(nUnique * N_SHAPE_MEMBERS);
        Arrays.fill(data.shapes.buf, 0, data.shapes.size, 0);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = x + width * y;
                int r = spans.buf[spans.buf[regions[pos]] + 1];
                int idx = N_SHAPE_MEMBERS * (r-1);

                // TODO: Use image index instead of x,y pair for min x, min y, max x, max y

                if (data.shapes.buf[idx] == 0) {
                    data.shapes.buf[idx + 1] = pos; // firstPoint
                    data.shapes.buf[idx + 2] = pos;
                    data.shapes.buf[idx + 3] = pos;
                }

                data.shapes.buf[idx] = (data.shapes.buf[idx] | ((int)image[pos] & 0x80000000)) + 1; // areaAndColor

                if (x < (data.shapes.buf[idx + 2] % width))
                    data.shapes.buf[idx + 2] = pos;
                if (y < (data.shapes.buf[idx + 3] / width))
                    data.shapes.buf[idx + 3] = pos;
                if (x > (data.shapes.buf[idx + 4] % width))
                    data.shapes.buf[idx + 4] = pos;
                if (y > (data.shapes.buf[idx + 5] / width))
                    data.shapes.buf[idx + 5] = pos;

                regions[pos] = r;
            }
        }
    }

    public static void removeVesselVoids(Scratch data, int[] regions, byte[] image, int width, int height)
    {
        for (int i = 0; i < data.shapes.size; i += N_SHAPE_MEMBERS) {
            // if this shape is white, then skip
            int shapeArea = data.shapes.buf[i] & 0x7fffFFFF;
            if (data.shapes.buf[i] <= 0)
                continue;

            int xMin = data.shapes.buf[i+2] % width;
            int yMin = data.shapes.buf[i+3] / width;
            int xMax = data.shapes.buf[i+4] % width;
            int yMax = data.shapes.buf[i+5] / width;

            int shapeW = (xMax - xMin + 1);
            int shapeH = (yMax - yMin + 1);
            double occupied = (double)shapeArea / (double)(shapeW * shapeH);
            double sdr = shapeW > shapeH ?
                (double)shapeH / (double)shapeW :
                (double)shapeW / (double)shapeH;
            double boxness = occupied * sdr;

            double highestDistance = 0.0;
            for (int a = 2; a <= 4; a++) {
                int xa = data.shapes.buf[i+a] % width;
                int ya = data.shapes.buf[i+a] / width;
                for (int b = a+1; b <= 5; b++) {
                    int xb = data.shapes.buf[i+b] % width;
                    int yb = data.shapes.buf[i+b] / width;
                    double dx = xa - xb;
                    double dy = ya - yb;
                    highestDistance = Math.max(highestDistance, Math.sqrt(dx*dx + dy*dy));
                }
            }

            // decide if shape should be filled with white
            if (boxness <= 0.09375 || shapeArea / highestDistance <= 16.0) {
                int firstPoint = data.shapes.buf[i + 1];
                int neighbor;
                if ((firstPoint % width) > 0)
                    neighbor = regions[firstPoint-1];
                else if (firstPoint >= width)
                    neighbor = regions[firstPoint-width];
                else
                    neighbor = 2;

                data.shapes.buf[i + 1] = -neighbor;
            }
        }

        int nShapes = data.shapes.size / N_SHAPE_MEMBERS;
        for (int i = 0; i < width * height; i++) {
            int s = N_SHAPE_MEMBERS * (regions[i] - 1);
            if (s >= 0 && data.shapes.buf[s + 1] < 0) {
                regions[i] = -data.shapes.buf[s + 1];
                image[i] = -1;
            }
        }
    }

    public static void fillShapes(Scratch data, int[] regions, byte[] image, int width, int height, double maxSize, boolean lookingForWhite)
    {
        int maxPixelCount = (int)(3.14159 * maxSize * maxSize);
        int colorToMatch = lookingForWhite ? -1 : 0;
        int area = width * height;

        for (int i = 0; i < area; i++) {
            int idx = N_SHAPE_MEMBERS * (Math.abs(regions[i]) - 1);
            if (idx < 0)
                continue;

            int shapeArea = data.shapes.buf[idx] & 0x7fffFFFF;

            if (shapeArea <= maxPixelCount && (image[i] ^ colorToMatch) >= 0)
                image[i] = (byte)(~colorToMatch);
        }
    }
}
