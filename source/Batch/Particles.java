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

        int startColor = image[0];
        int startIdx = 0;
        for (int y = 0; y < height; y++) {
            int color = image[y*width];
            if ((color ^ startColor) < 0)
                startIdx++;

            startColor = color;
            int prev = startColor;
            int r = startIdx;
            regions[y*width] = r;

            for (int x = 1; x < width; x++) {
                color = image[x+width*y];
                if ((color ^ prev) < 0) {
                    r++;
                    /*
                    if (y > 0 && (image[x+width*(y-1)] ^ color) >= 0)
                        r = regions[x+width*(y-1)];
                    else
                        r++;
                    */
                }
                regions[x+width*y] = r;
                prev = color;
            }
        }

        /*
        for (int x = 0; x < width; x++) {
            int prev = image[x];
            int region = regions[x];
            for (int y = 1; y < height; y++) {
                int color = image[x+width*y];
                if ((color ^ prev) < 0)
                    region = regions[x+width*y];
                else
                    regions[x+width*y] = region;
                prev = color;
            }
        }
        */

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
