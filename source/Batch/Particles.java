package Batch;

import java.util.Arrays;

// TODO: Add width and height to Shape "struct"
public class Particles
{
    /*
    struct Shape {
        unsigned int flags;
        int pixelCount;
    }
    */

    public static final int N_SHAPE_MEMBERS = 2;

    public static final int FLAG_IS_WHITE = 1;
    public static final int FLAG_NOT_SURROUNDED = 2;

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

            //spans.buf[i+1] = spans.buf[startingSpan+1];
        }

        data.shapes.resize(nUnique * N_SHAPE_MEMBERS);
        Arrays.fill(data.shapes.buf, 0, data.shapes.size, 0);

        for (int i = 0; i < area; i++) {
            int x = i % width;
            int r = spans.buf[spans.buf[regions[i]] + 1];
            int idx = (r-1) * N_SHAPE_MEMBERS;

            int flags = data.shapes.buf[idx];
            flags |= (image[i] >> 31) & FLAG_IS_WHITE;
            if (i < width || i >= area-width || x == 0 || x == width-1)
                flags |= FLAG_NOT_SURROUNDED;

            data.shapes.buf[idx] = flags;
            data.shapes.buf[idx+1]++; // pixelCount
            regions[i] = r;
        }
    }

    public static void fillShapes(Scratch data, int[] regions, byte[] image, int width, int height, double maxSize, boolean lookingForWhite)
    {
        int maxPixelCount = (int)(3.14159 * maxSize * maxSize);
        int colorToMatch = lookingForWhite ? -1 : 0;
        int area = width * height;

        for (int i = 0; i < area; i++) {
            int idx = N_SHAPE_MEMBERS * (regions[i] - 1);
            if (idx < 0)
                continue;

            int flags = data.shapes.buf[idx];
            int pixelCount = data.shapes.buf[idx+1];

            if (pixelCount <= maxPixelCount && (image[i] ^ colorToMatch) >= 0)
                image[i] = (byte)(~colorToMatch);
        }
    }
}
