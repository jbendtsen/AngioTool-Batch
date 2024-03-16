package Algorithms;

// Modified snippet from ij.process.ByteProcessor
// Using "a = Math.min(a, b)" is faster than "if (b<a) a = b"
// See FiltersBenchmark.java for details

public class Filters
{
    public static void filterMin(byte[] outputPixels, byte[] inputPixels, int width, int height)
    {
        if (width == 1) {
            filterEdgeMin(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
            return;
        }

        int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int min = 0;

        for (int y = 1; y < height-1; y++) {
            int offset = 1 + width * y;
            p2 = inputPixels[offset-width-1] & 0xff;
            p3 = inputPixels[offset-width] & 0xff;
            p5 = inputPixels[offset-1] & 0xff;
            p6 = inputPixels[offset] & 0xff;
            p8 = inputPixels[offset+width-1] & 0xff;
            p9 = inputPixels[offset+width] & 0xff;

            for (int x = 0; x < width-2; x++) {
                p1 = p2; p2 = p3;
                p3 = inputPixels[offset-width+x+1] & 0xff;
                p4 = p5; p5 = p6;
                p6 = inputPixels[offset+x+1] & 0xff;
                p7 = p8; p8 = p9;
                p9 = inputPixels[offset+width+x+1] & 0xff;

                min = p5;
                min = Math.min(min, p1);
                min = Math.min(min, p2);
                min = Math.min(min, p3);
                min = Math.min(min, p4);
                min = Math.min(min, p6);
                min = Math.min(min, p7);
                min = Math.min(min, p8);
                min = Math.min(min, p9);

                outputPixels[offset+x] = (byte)min;
            }
        }

        filterEdgeMin(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        filterEdgeMin(outputPixels, inputPixels, width, height, width, 0, 0, 1, 0);
        filterEdgeMin(outputPixels, inputPixels, width, height, height, width-1, 0, 0, 1);
        filterEdgeMin(outputPixels, inputPixels, width, height, width, 0, height-1, 1, 0);
    }

    static void filterEdgeMin(byte[] outputPixels, byte[] inputPixels, int width, int height, int n, int x, int y, int xinc, int yinc)
    {
        int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int min = 0;
        int count;

        for (int i=0; i<n; i++) {
            int left = x > 0 ? x-1 : 0;
            int right = x < width-1 ? x+1 : width-1;
            int top = y > 0 ? y-1 : 0;
            int bottom = y < height-1 ? y+1 : height-1;

            p1 = inputPixels[left + width * top] & 0xff;
            p2 = inputPixels[x + width * top] & 0xff;
            p3 = inputPixels[right + width * top] & 0xff;
            p4 = inputPixels[left + width * y] & 0xff;
            p5 = inputPixels[x + width * y] & 0xff;
            p6 = inputPixels[right + width * y] & 0xff;
            p7 = inputPixels[left + width * bottom] & 0xff;
            p8 = inputPixels[x + width * bottom] & 0xff;
            p9 = inputPixels[right + width * bottom] & 0xff;

            min = p5;
            min = Math.min(min, p1);
            min = Math.min(min, p2);
            min = Math.min(min, p3);
            min = Math.min(min, p4);
            min = Math.min(min, p6);
            min = Math.min(min, p7);
            min = Math.min(min, p8);
            min = Math.min(min, p9);

            outputPixels[x+y*width] = (byte)min;
            x += xinc;
            y += yinc;
        }
    }

    public static void filterMax(byte[] outputPixels, byte[] inputPixels, int width, int height)
    {
        if (width == 1) {
            filterEdgeMax(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
            return;
        }

        int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int max = 0;

        for (int y = 1; y < height-1; y++) {
            int offset = 1 + width * y;
            p2 = inputPixels[offset-width-1] & 0xff;
            p3 = inputPixels[offset-width] & 0xff;
            p5 = inputPixels[offset-1] & 0xff;
            p6 = inputPixels[offset] & 0xff;
            p8 = inputPixels[offset+width-1] & 0xff;
            p9 = inputPixels[offset+width] & 0xff;

            for (int x = 0; x < width-2; x++) {
                p1 = p2; p2 = p3;
                p3 = inputPixels[offset-width+x+1] & 0xff;
                p4 = p5; p5 = p6;
                p6 = inputPixels[offset+x+1] & 0xff;
                p7 = p8; p8 = p9;
                p9 = inputPixels[offset+width+x+1] & 0xff;

                max = p5;
                max = Math.max(max, p1);
                max = Math.max(max, p2);
                max = Math.max(max, p3);
                max = Math.max(max, p4);
                max = Math.max(max, p6);
                max = Math.max(max, p7);
                max = Math.max(max, p8);
                max = Math.max(max, p9);

                outputPixels[offset+x] = (byte)max;
            }
        }

        filterEdgeMax(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        filterEdgeMax(outputPixels, inputPixels, width, height, width, 0, 0, 1, 0);
        filterEdgeMax(outputPixels, inputPixels, width, height, height, width-1, 0, 0, 1);
        filterEdgeMax(outputPixels, inputPixels, width, height, width, 0, height-1, 1, 0);
    }

    static void filterEdgeMax(byte[] outputPixels, byte[] inputPixels, int width, int height, int n, int x, int y, int xinc, int yinc)
    {
        int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int max = 0;
        int count;

        for (int i=0; i<n; i++) {
            int left = x > 0 ? x-1 : 0;
            int right = x < width-1 ? x+1 : width-1;
            int top = y > 0 ? y-1 : 0;
            int bottom = y < height-1 ? y+1 : height-1;

            p1 = inputPixels[left + width * top] & 0xff;
            p2 = inputPixels[x + width * top] & 0xff;
            p3 = inputPixels[right + width * top] & 0xff;
            p4 = inputPixels[left + width * y] & 0xff;
            p5 = inputPixels[x + width * y] & 0xff;
            p6 = inputPixels[right + width * y] & 0xff;
            p7 = inputPixels[left + width * bottom] & 0xff;
            p8 = inputPixels[x + width * bottom] & 0xff;
            p9 = inputPixels[right + width * bottom] & 0xff;

            max = p5;
            max = Math.max(max, p1);
            max = Math.max(max, p2);
            max = Math.max(max, p3);
            max = Math.max(max, p4);
            max = Math.max(max, p6);
            max = Math.max(max, p7);
            max = Math.max(max, p8);
            max = Math.max(max, p9);

            outputPixels[x+y*width] = (byte)max;
            x += xinc;
            y += yinc;
        }
    }

    public static void removeInsulatedBrightPixels(byte[] outputPixels, byte[] inputPixels, int width, int height, int depth)
    {
        depth = Math.min(Math.max(depth, 1), 8);

        if (width == 1) {
            removeEdgeInsulatedBrightPixels(outputPixels, inputPixels, width, height, depth, height, 0, 0, 0, 1);
            return;
        }

        int p1, p2, p3, p4, p5, p6, p7, p8, p9;

        for (int z = 0; z < depth; z++) {
            for (int y = 1; y < height-1; y++) {
                // No AND with 0xff
                int offset = 1 + width * y;
                p2 = inputPixels[offset-width-1];
                p3 = inputPixels[offset-width];
                p5 = inputPixels[offset-1];
                p6 = inputPixels[offset];
                p8 = inputPixels[offset+width-1];
                p9 = inputPixels[offset+width];

                for (int x = 0; x < width-2; x++) {
                    p1 = p2; p2 = p3;
                    p3 = inputPixels[offset-width+x+1];
                    p4 = p5; p5 = p6;
                    p6 = inputPixels[offset+x+1];
                    p7 = p8; p8 = p9;
                    p9 = inputPixels[offset+width+x+1];

                    int sum =
                        ((p1 >> z) & 1) +
                        ((p2 >> z) & 1) +
                        ((p3 >> z) & 1) +
                        ((p4 >> z) & 1) +
                        ((p5 >> z) & 1) +
                        ((p6 >> z) & 1) +
                        ((p7 >> z) & 1) +
                        ((p8 >> z) & 1) +
                        ((p9 >> z) & 1);

                    outputPixels[offset+x] = (byte)(p5 ^ (((8 - sum) >> 31) & (1 << z)));
                }
            }
        }

        removeEdgeInsulatedBrightPixels(outputPixels, inputPixels, width, height, depth, height, 0, 0, 0, 1);
        removeEdgeInsulatedBrightPixels(outputPixels, inputPixels, width, height, depth, width, 0, 0, 1, 0);
        removeEdgeInsulatedBrightPixels(outputPixels, inputPixels, width, height, depth, height, width-1, 0, 0, 1);
        removeEdgeInsulatedBrightPixels(outputPixels, inputPixels, width, height, depth, width, 0, height-1, 1, 0);
    }

    static void removeEdgeInsulatedBrightPixels(
        byte[] outputPixels,
        byte[] inputPixels,
        int width,
        int height,
        int depth,
        int n,
        int x,
        int y,
        int xinc,
        int yinc
    ) {
        int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int max = 0;
        int count;

        for (int z = 0; z < depth; z++) {
            for (int i = 0; i < n; i++) {
                int left = x > 0 ? x-1 : 0;
                int right = x < width-1 ? x+1 : width-1;
                int top = y > 0 ? y-1 : 0;
                int bottom = y < height-1 ? y+1 : height-1;

                p1 = inputPixels[left + width * top];
                p2 = inputPixels[x + width * top];
                p3 = inputPixels[right + width * top];
                p4 = inputPixels[left + width * y];
                p5 = inputPixels[x + width * y];
                p6 = inputPixels[right + width * y];
                p7 = inputPixels[left + width * bottom];
                p8 = inputPixels[x + width * bottom];
                p9 = inputPixels[right + width * bottom];

                int sum =
                    ((p1 >> z) & 1) +
                    ((p2 >> z) & 1) +
                    ((p3 >> z) & 1) +
                    ((p4 >> z) & 1) +
                    ((p5 >> z) & 1) +
                    ((p6 >> z) & 1) +
                    ((p7 >> z) & 1) +
                    ((p8 >> z) & 1) +
                    ((p9 >> z) & 1);

                outputPixels[x+y*width] = (byte)(p5 ^ (((8 - sum) >> 31) & (1 << z)));
                x += xinc;
                y += yinc;
            }
        }
    }
}
