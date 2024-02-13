package Batch;

import java.util.Arrays;

// TODO: Add width and height to Shape "struct"
public class Particles
{
    /*
    struct Shape {
        int perimeter;
        int area;
        int skelIterations;
    }
    */

    public static final int N_SHAPE_MEMBERS = 3;
    public static final int MAX_SKEL_ITERATIONS = 16;

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

        for (int i = 0; i < area; i++) {
            int x = i % width;
            int r = spans.buf[spans.buf[regions[i]] + 1];
            int idx = N_SHAPE_MEMBERS * (r-1);

            data.shapes.buf[idx+1]++; // area++
            regions[i] = r;

            // use marching squares to determine the perimeter
            if (i >= width+1 && x > 0) {
                int edges =
                    ((image[i-width-1] >> 31) & 8) |
                    ((image[i-width]   >> 31) & 4) |
                    ((image[i]         >> 31) & 2) |
                    ((image[i-1]       >> 31) & 1);

                if (edges != 0 && edges != 15) {
                    int nw = regions[i-width-1];
                    int ne = regions[i-width];
                    //int se = r;
                    int sw = regions[i-1];

                    if (nw > 0 && edges != 2 && edges != 13) {
                        data.shapes.buf[N_SHAPE_MEMBERS * (nw-1)]++; // perimeter++
                        regions[i-width-1] = -nw;
                    }
                    if (ne > 0 && edges != 1 && edges != 14) {
                        data.shapes.buf[N_SHAPE_MEMBERS * (ne-1)]++; // perimeter++
                        regions[i-width] = -ne;
                    }
                    if (r > 0 && edges != 7 && edges != 8) {
                        data.shapes.buf[idx]++; // perimeter++
                        regions[i] = -r;
                    }
                    if (sw > 0 && edges != 4 && edges != 11) {
                        data.shapes.buf[N_SHAPE_MEMBERS * (sw-1)]++; // perimeter++
                        regions[i-1] = -sw;
                    }
                }
            }
        }

        int[] scratch = IntBufferPool.acquireAsIs(area);
        int[] a = null, b = null;

        int nRemovals;
        int totalPasses = 0;
        do {
            a = regions;
            b = scratch;

            nRemovals = 0;
            for (int pass = 1; pass <= 2; pass++) {
                for (int y = 1; y < height-1; y++) {
                    for (int x = 1; x < width-1; x++) {
                        int idx = x+width*y;
                        int r = a[idx];
                        if (r > 0) {
                            int value = Zha84.lut[
                                (((Integer.bitCount(a[idx-width-1] - r) - 1) >> 31) & 1) |
                                (((Integer.bitCount(a[idx-width]   - r) - 1) >> 31) & 2) |
                                (((Integer.bitCount(a[idx-width+1] - r) - 1) >> 31) & 4) |
                                (((Integer.bitCount(a[idx+1]       - r) - 1) >> 31) & 8) |
                                (((Integer.bitCount(a[idx+width+1] - r) - 1) >> 31) & 16) |
                                (((Integer.bitCount(a[idx+width]   - r) - 1) >> 31) & 32) |
                                (((Integer.bitCount(a[idx+width-1] - r) - 1) >> 31) & 64) |
                                (((Integer.bitCount(a[idx-1]       - r) - 1) >> 31) & 128)
                            ];

                            //boolean shouldTrim = value == 3 || value == pass
                            int shouldTrim = (Integer.bitCount(value - 3) * Integer.bitCount(value - pass) - 1) >> 31;

                            nRemovals -= shouldTrim;
                            data.shapes.buf[N_SHAPE_MEMBERS * (r - 1) + 2] |= shouldTrim & (1 << 31);
                            r = (r ^ shouldTrim) - shouldTrim;
                        }
                        b[idx] = r;
                    }
                }
                int[] temp = a;
                a = b;
                b = temp;
            }

            // if the removal flag is set, clear it and add one
            for (int i = 0; i < data.shapes.size; i += N_SHAPE_MEMBERS)
                data.shapes.buf[i+2] = (data.shapes.buf[i+2] & 0x7fffFFFF) + (data.shapes.buf[i+2] >>> 31);

            totalPasses++;
        } while (nRemovals > 0 && totalPasses <= MAX_SKEL_ITERATIONS);

        System.out.println("totalPasses: " + totalPasses);

        /*
        String[] names = new String[] {"perimeter", "area", "iterations"};
        String msg = "";
        for (int i = 0; i < data.shapes.size; i++) {
            msg += "" + (i / 3) + ": " + names[i % 3] + " = " + data.shapes.buf[i] + "\n";
        }
        System.out.println(msg);
        */

        IntBufferPool.release(scratch);
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

            //int perimeter = data.shapes.buf[idx];
            int shapeArea = data.shapes.buf[idx+1];

            if (shapeArea <= maxPixelCount && (image[i] ^ colorToMatch) >= 0)
                image[i] = (byte)(~colorToMatch);
        }
    }
}
