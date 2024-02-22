package Batch;

public class Planes
{
    static final int BLOCK_SIZE = 64; // for optimal cache utilization

    public static byte[][] combine8(byte[] output, int width, int height, Object[] slicesObj)
    {
        final int area = width * height;
        final int breadth = slicesObj.length;

        byte[][] slices = new byte[breadth][];
        for (int i = 0; i < breadth; i++)
            slices[i] = (byte[])slicesObj[i];

        for (int i = 0; i < area; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, area - i);

            for (int k = 0; k < block; k++)
                output[i+k] = (byte)(slices[0][i+k] >>> 31 | -slices[0][i+k] >>> 31);

            for (int j = 1; j < breadth; j++) {
                for (int k = 0; k < block; k++)
                    output[i+k] |= (slices[j][i+k] >>> 31 | -slices[j][i+k] >>> 31) << j;
            }
        }

        return slices;
    }

    public static short[][] combine16(byte[] output, int width, int height, Object[] slicesObj)
    {
        final int area = width * height;
        final int breadth = slicesObj.length;

        short[][] slices = new short[breadth][];
        for (int i = 0; i < breadth; i++)
            slices[i] = (short[])slicesObj[i];

        for (int i = 0; i < area; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, area - i);

            for (int k = 0; k < block; k++)
                output[i+k] = (byte)(slices[0][i+k] >>> 31 | -slices[0][i+k] >>> 31);

            for (int j = 1; j < breadth; j++) {
                for (int k = 0; k < block; k++)
                    output[i+k] |= (slices[j][i+k] >>> 31 | -slices[j][i+k] >>> 31) << j;
            }
        }

        return slices;
    }

    public static int[][] combineRgb(byte[] output, int width, int height, Object[] slicesObj)
    {
        final int area = width * height;
        final int breadth = slicesObj.length;

        int[][] slices = new int[breadth][];
        for (int i = 0; i < breadth; i++)
            slices[i] = (int[])slicesObj[i];

        for (int i = 0; i < area; i++) {
            int rgb = slices[0][i];
            output[i] = (byte)(
                ((rgb & 0xff0000) >>> 31 | -(rgb & 0xff0000) >>> 31) |
                ((rgb &   0xff00) >>> 31 | -(rgb &   0xff00) >>> 31) << 1 |
                ((rgb &     0xff) >>> 31 | -(rgb &     0xff) >>> 31) << 2
            );
        }

        return slices;
    }

    public static float[][] combine32(byte[] output, int width, int height, Object[] slicesObj)
    {
        final int area = width * height;
        final int breadth = slicesObj.length;

        float[][] slices = new float[breadth][];
        for (int i = 0; i < breadth; i++)
            slices[i] = (float[])slicesObj[i];

        for (int i = 0; i < area; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, area - i);

            for (int k = 0; k < block; k++) {
                int exp = (Float.floatToRawIntBits(slices[0][i+k]) >> 23) & 0xff;
                output[i+k] = (byte)(-(exp - 0x76) >>> 31);
            }

            for (int j = 1; j < breadth; j++) {
                for (int k = 0; k < block; k++) {
                    int exp = (Float.floatToRawIntBits(slices[j][i+k]) >> 23) & 0xff;
                    output[i+k] |= (-(exp - 0x76) >>> 31) << j;
                }
            }
        }

        return slices;
    }

    public static void split8(byte[][] output, int width, int height, byte[] planes)
    {
        final int area = width * height;
        final int breadth = output.length;

        for (int i = 0; i < area; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, area - i);
            for (int j = 0; j < breadth; j++) {
                for (int k = 0; k < block; k++)
                    output[j][i+k] = (byte)-((planes[i+k] >>> j) & 1);
            }
        }
    }

    public static void split16(short[][] output, int width, int height, byte[] planes)
    {
        final int area = width * height;
        final int breadth = output.length;

        for (int i = 0; i < area; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, area - i);
            for (int j = 0; j < breadth; j++) {
                for (int k = 0; k < block; k++)
                    output[j][i+k] = (short)-((planes[i+k] >>> j) & 1);
            }
        }
    }

    public static void splitRgb(int[][] output, int width, int height, byte[] planes)
    {
        final int area = width * height;

        for (int i = 0; i < area; i++) {
            int r = -(planes[i] & 1) & 0xff;
            int g = -((planes[i] >>> 1) & 1) & 0xff;
            int b = -((planes[i] >>> 2) & 1) & 0xff;
            output[0][i] = (r << 16) | (g << 8) | b;
        }
    }

    // using the lookup table "float[] {0.0f, 255.0f}" is 5x faster than using a ternary (ie. bit == 1 ? 255.0f : 0.0f)
    // see FloatManipBenchmark.java
    public static void split32(float[][] output, int width, int height, byte[] planes, float[] lut)
    {
        final int area = width * height;
        final int breadth = output.length;

        lut[0] = 0.0f;
        lut[1] = 255.0f;

        for (int i = 0; i < area; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, area - i);
            for (int j = 0; j < breadth; j++) {
                for (int k = 0; k < block; k++)
                    output[j][i+k] = lut[(planes[i+k] >>> j) & 1];
            }
        }
    }
}
