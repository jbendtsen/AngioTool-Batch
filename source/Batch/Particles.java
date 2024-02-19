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

            // TODO:
            // if the bounding box resembles a square and the filled area is greater than a certain fraction of the bounding box area
            // given that the area of that bounding box is above a certain threshold
            // or the bounding box does not resemble a square, yet the bounding width or bounding height is too large
            // etc.
            // then skip this shape

            int xMin = data.shapes.buf[i+2] % width;
            int yMin = data.shapes.buf[i+3] / width;
            int xMax = data.shapes.buf[i+4] % width;
            int yMax = data.shapes.buf[i+5] / width;

            // TODO: Use image index instead of x,y pair for min x, min y, max x, max y

            int shapeW = (xMax - xMin + 1);
            int shapeH = (yMax - yMin + 1);
            double occupied = (double)shapeArea / (double)(shapeW * shapeH);
            double sdr = shapeW > shapeH ?
                (double)shapeH / (double)shapeW :
                (double)shapeW / (double)shapeH;

            System.out.println(
                "" + (i / N_SHAPE_MEMBERS) + ": dimensions = " + shapeW + "x" + shapeH +
                ", shapeArea = " + shapeArea + ", occupied = " + occupied + ", sdr = " + sdr
            );

            if (occupied + sdr > 1.0)
                continue;

            /*
            int x = (xMin + xMax) / 2;
            int y = (yMin + yMax) / 2;
            int s = (i / N_SHAPE_MEMBERS) + 1;

            int voidW = 0;
            int voidH = 0;
            int pinchW = 0;
            int pinchH = 0;
            int leftW = 0;
            int leftH = 0;
            int rightW = 0;
            int rightH = 0;

            for (int axis = 0; axis < 2; axis++) {
                int min, max, inc;
                if (axis == 0) {
                    min = y * width;
                    max = min + width - 1;
                    inc = 1;
                }
                else {
                    min = x;
                    max = x + (height-1) * width;
                    inc = width;
                }

                int pos = x + width * y;
                int enterVoidPoint = regions[pos] == s ? pos : -1;
                int voidCounter = 0;
                int pinchCounter = 0;

                do {
                    boolean exitedVoid = false;

                    while (true) {
                        int c = image[pos];
                        if (c >= 0) {
                            if (exitedVoid)
                                break;
                            if (enterVoidPoint < 0 && regions[pos] == s)
                                enterVoidPoint = pos;
                            if (enterVoidPoint >= 0)
                                voidCounter++;
                        }
                        else {
                            if (enterVoidPoint >= 0)
                                exitedVoid = true;
                            if (exitedVoid)
                                pinchCounter++;
                        }

                        pos += inc;
                        if ((inc < 0 && pos < min) || (inc > 0 && pos > max))
                            break;
                    }

                    pos = enterVoidPoint;
                    if (pos < 0)
                        pos = x + width * y;

                    inc *= -1;
                } while (inc < 0);

                pinchW = pinchH;
                pinchH = pinchCounter;

                voidW = voidH;
                voidH = voidCounter;

                leftW = leftH;
                rightW = rightH;

                for (int side = 0; side < 2; side++) {
                    pinchCounter = 0;

                    int edge = 2*side + (axis^1);
                    int start = data.shapes.buf[i+2+edge];

                    switch (edge) {
                        case 0:
                            if (start % width == 0)
                                continue;
                            start--;
                            break;
                        case 1:
                            if (start / width == 0)
                                continue;
                            start -= width;
                            break;
                        case 2:
                            if (start % width == width-1)
                                continue;
                            start++;
                            break;
                        case 3:
                            if (start / width == height-1)
                                continue;
                            start += width;
                            break;
                    }

                    pos = start;
                    while (pos >= min) {
                        if (image[pos] < 0)
                            pinchCounter++;
                        else
                            break;
                        pos -= inc;
                    }

                    pos = start;
                    while (pos <= max) {
                        if (image[pos] < 0)
                            pinchCounter++;
                        else
                            break;
                        pos += inc;
                    }

                    leftH = rightH;
                    rightH = pinchCounter;
                }
            }

            if (leftW == 0)
                leftW = rightW;
            if (rightW == 0)
                rightW = leftW;
            if (leftH == 0)
                leftH = rightH;
            if (rightH == 0)
                rightH = leftH;

            double lengthsRatio = 0.0;
            if (leftW > 0 && rightW > 0 && (voidW + pinchW) > 0) {
                double a = voidW + pinchW;
                double b = leftW;
                double c = rightW;
                lengthsRatio = (a < b ? a / b : b / a) * (a < c ? a / c : c / a);
            }
            if (leftH > 0 && rightH > 0 && (voidH + pinchH) > 0) {
                double a = voidH + pinchH;
                double b = leftH;
                double c = rightH;
                lengthsRatio = Math.max(lengthsRatio, (a < b ? a / b : b / a) * (a < c ? a / c : c / a));
            }

            System.out.println(
                "" + (i / N_SHAPE_MEMBERS) + ": voidW = " + voidW + ", voidH = " + voidH +
                ", pinchW = " + pinchW + ", pinchH = " + pinchH + ", leftW = " + leftW +
                ", rightW = " + rightW + ", leftH = " + leftH + ", rightH = " + rightH
            );

            */
            double lengthsRatio = 1.0;

            // decide if shape should be filled with white
            if (lengthsRatio > 0.5) {
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

            int shapeArea = data.shapes.buf[idx];

            if (shapeArea <= maxPixelCount && (image[i] ^ colorToMatch) >= 0)
                image[i] = (byte)(~colorToMatch);
        }
    }
}
