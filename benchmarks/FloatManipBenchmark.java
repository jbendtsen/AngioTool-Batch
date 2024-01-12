// The "get-multiply" and "get-boolean" methods appear to have equivalent performance,
// but the "get-shift" method outperforms them both by 2.5x most of the time (with it occasionally only being 20-30% faster)
// The "set-lookup" method is only 5-10% faster than "set-ternary".

class FloatManipBenchmark {
    static final int BLOCK_SIZE = 64; // for optimal cache utilization
    static final int N_GET_METHODS = 3;

    static final String[] METHODS = new String[] {
        "get-multiply",
        "get-shift",
        "get-boolean",
        "set-ternary",
        "set-lookup"
    };

    public static void main(String[] args) {
        System.out.println("FloatManipBenchmark");

        int width = 640, height = 480, breadth = 3;
        int iterations = 1000;
        int method = 1;
        boolean usingSet = method >= N_GET_METHODS;

        byte[] planes = new byte[width * height];
        float[][] slices = new float[breadth][];
        int x = 0;

        if (usingSet) {
            for (int i = 0; i < breadth; i++)
                slices[i] = new float[planes.length];

            for (int i = 0; i < planes.length; i++) {
                x += 1337;
                x = ((x >> 16) ^ x) * 0x45d9f3b;
                x = ((x >> 16) ^ x) * 0x45d9f3b;
                x = (x >> 16) ^ x;
                planes[i] = (byte)(x & 7);
            }
        }
        else {
            for (int i = 0; i < breadth; i++) {
                slices[i] = new float[planes.length];
                for (int j = 0; j < planes.length; j++) {
                    x += 1337;
                    x = ((x >> 16) ^ x) * 0x45d9f3b;
                    x = ((x >> 16) ^ x) * 0x45d9f3b;
                    x = (x >> 16) ^ x;
                    slices[i][j] = Float.intBitsToFloat(x & 0x3f7fffff);
                }
            }
        }

        float[] lut = new float[2];

        long startTime = System.nanoTime();
        switch (method) {
            case 0:
                for (int i = 0; i < iterations; i++)
                    getPlanesMultiplyCast(planes, slices);
                break;
            case 1:
                for (int i = 0; i < iterations; i++)
                    getPlanesTakeShift(planes, slices);
                break;
            case 2:
                for (int i = 0; i < iterations; i++)
                    getPlanesBoolean(planes, slices);
                break;
            case 3:
                for (int i = 0; i < iterations; i++)
                    setPlanesTernary(slices, planes);
                break;
            case 4:
                for (int i = 0; i < iterations; i++)
                    setPlanesLookup(slices, planes, lut);
                break;
            default:
                throw new RuntimeException("Unsupported method " + method);
        }
        long endTime = System.nanoTime();

        double elapsedMs = (double)(endTime - startTime) / 1E6;
        System.out.println(
            "Dimensions: " + width + "x" + height + "x" + breadth +
            "\nIterations: " + iterations +
            "\nMethod: " + METHODS[method] +
            "\nTime taken: " + elapsedMs + "ms"
        );
    }

    static void getPlanesMultiplyCast(byte[] output, float[][] slices) {
        for (int i = 0; i < output.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < slices.length; j++) {
                for (int k = 0; k < block; k++) {
                    int value = (int)(slices[j][i+k] * 256.0f);
                    output[i+k] |= (value >>> 31 | -value >>> 31) << j;
                }
            }
        }
    }

    static void getPlanesTakeShift(byte[] output, float[][] slices) {
        for (int i = 0; i < output.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < slices.length; j++) {
                for (int k = 0; k < block; k++) {
                    int exp = (Float.floatToRawIntBits(slices[j][i+k]) >> 23) & 0xff;
                    output[i+k] |= (-(exp - 0x76) >>> 31) << j;
                }
            }
        }
    }

    static void getPlanesBoolean(byte[] output, float[][] slices) {
        for (int i = 0; i < output.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < slices.length; j++) {
                for (int k = 0; k < block; k++) {
                    int value = slices[j][i+k] < 1.0f / 256.0f ? 1 : 0;
                    output[i+k] |= value << j;
                }
            }
        }
    }

    static void setPlanesTernary(float[][] output, byte[] planes) {
        for (int i = 0; i < planes.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < output.length; j++) {
                for (int k = 0; k < block; k++)
                    output[j][i+k] = ((planes[i+k] >>> j) & 1) == 1 ? 255.0f : 0.0f;
            }
        }
    }

    static void setPlanesLookup(float[][] output, byte[] planes, float[] lut) {
        lut[0] = 0.0f;
        lut[1] = 255.0f;
        for (int i = 0; i < planes.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < output.length; j++) {
                for (int k = 0; k < block; k++)
                    output[j][i+k] = lut[(planes[i+k] >>> j) & 1];
            }
        }
    }
}
