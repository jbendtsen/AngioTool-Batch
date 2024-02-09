package Batch;

import java.util.Arrays;

public class Particles
{
    /*
    struct Shape {
        unsigned int flags;
        int pixelCount;
        int firstX;
        int firstY;
    }
    */

    public static final int N_SHAPE_MEMBERS = 0;

    public static final int FLAG_IS_WHITE = 1;
    public static final int FLAG_SURROUNDED = 2;

    public static class Scratch
    {
        public IntVector shapes = new IntVector();
        public IntVector ffStack = new IntVector();
    }

    public static void computeShapes(Scratch data, int[] regions, byte[] image, int width, int height)
    {
        data.shapes.size = 0;

        int area = width * height;
        Arrays.fill(regions, 0, width * height, 0);

        int occ = 0;

        for (int i = 0; i < area; i++) {
            if (regions[i] != 0)
                continue;

            occ++;
            regions[i] = occ;
            int color = image[i];

            int flags = (color >> 31) & FLAG_IS_WHITE;
            int pixelCount = 1;
            int firstX = i % width;
            int firstY = i / width;

            IntVector stack = data.ffStack;
            stack.size = 0;
            stack.addFour(firstX, firstX, firstY, 1);
            stack.addFour(firstX, firstX, firstY - 1, 1);

            boolean isSurroundedByOtherShape = true;
            int sp = 0;
            while (sp < stack.size) {
                int x1 = stack.buf[sp];
                int x2 = stack.buf[sp+1];
                int y  = stack.buf[sp+2];
                int dy = stack.buf[sp+3];
                sp += 4;

                int x = x1;
                if (x < 0 || y < 0 || x >= width || y >= height) {
                    isSurroundedByOtherShape = false;
                }
                else {
                    while (--x >= 0 && regions[x + width * y] == 0 && (image[x + width * y] ^ color) >= 0)
                        regions[x + width * y] = occ;

                    isSurroundedByOtherShape = isSurroundedByOtherShape && x >= 0;
                }

                if (x < x1)
                    stack.addFour(x, x1 - 1, y - dy, -dy);

                while (x1 <= x2) {
                    if (x1 < 0 || y < 0 || x1 >= width || y >= height) {
                        isSurroundedByOtherShape = false;
                    }
                    else {
                        while (x1 < width && regions[x1 + width * y] == 0 && (image[x1 + width * y] ^ color) >= 0) {
                            regions[x1 + width * y] = occ;
                            x1++;
                        }
                        isSurroundedByOtherShape = isSurroundedByOtherShape && x1 < width;
                    }

                    if (x1 > x)
                        stack.addFour(x, x1 - 1, y + dy, dy);
                    if (x1 - 1 > x2)
                        stack.addFour(x2 + 1, x1 - 1, y - dy, -dy);

                    x1++;
                    while (
                        x1 < x2 && x1 >= 0 && x1 < width && y >= 0 && y < height &&
                        regions[x1 + width * y] == 0 && (image[x1 + width * y] ^ color) >= 0
                    ) {
                        x1++;
                    }

                    isSurroundedByOtherShape = isSurroundedByOtherShape && x1 < width;
                    x = x1;
                }
            }

            if (isSurroundedByOtherShape)
                flags |= FLAG_SURROUNDED;

            data.shapes.addFour(flags, pixelCount, firstX, firstY);
        }

        byte[] shapeOutput = ByteBufferPool.acquireAsIs(area * 3);
        for (int i = 0; i < area; i++) {
            int v = regions[i];
            v = ((v >>> 16) ^ v) * 0x45d9f3b;
            v = ((v >>> 16) ^ v) * 0x45d9f3b;
            v = (v >>> 16) ^ v;
            shapeOutput[i*3] = (byte)(v >>> 16);
            shapeOutput[i*3+1] = (byte)(v >>> 8);
            shapeOutput[i*3+2] = (byte)v;
        }

        ImageUtils.writePpm24(shapeOutput, width, height, "shape-output.ppm");
        ByteBufferPool.release(shapeOutput);
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
