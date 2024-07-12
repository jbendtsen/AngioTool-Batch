// "bitwise" is only 2% faster than "ifelse"

class FloatAbsSwapBenchmark {
    static final String[] METHODS = new String[] {
        "ifelse",
        "bitwise",
    };

    public static void main(String[] args) {
        System.out.println("FloatAbsSwapBenchmark");

        int width = 640, height = 480;
        int iterations = 5000;
        int method = 1;

        int area = width * height;
        float[] values = new float[area * 2];
        float[] output = new float[area * 2];
        int x = 0;

        for (int i = 0; i < area; i += 2) {
            x += 1337;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = (x >> 16) ^ x;
            values[i] = Float.intBitsToFloat(x & 0xbfffffff);

            x += 1337;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = (x >> 16) ^ x;
            values[i+1] = Float.intBitsToFloat(x & 0xbfffffff);
        }

        long startTime = System.nanoTime();
        switch (method) {
            case 0:
                for (int i = 0; i < iterations; i++)
                    absSwapIfElse(values, output);
                break;
            case 1:
                for (int i = 0; i < iterations; i++)
                    absSwapBitwise(values, output);
                break;
            default:
                throw new RuntimeException("Unsupported method " + method);
        }
        long endTime = System.nanoTime();

        double elapsedMs = (double)(endTime - startTime) / 1E6;
        System.out.println(
            "Dimensions: " + width + "x" + height +
            "\nIterations: " + iterations +
            "\nMethod: " + METHODS[method] +
            "\nTime taken: " + elapsedMs + "ms"
        );
    }

    static void absSwapIfElse(float[] values, float[] output)
    {
        int area = values.length / 2;
        int len = area * 2;
        for (int i = 0; i < len; i += 2) {
            float e0 = values[i];
            float e1 = values[i+1];
            float small, large;
            if (Math.abs(e0) > Math.abs(e1)) {
                large = e0;
                small = e1;
            }
            else {
                large = e1;
                small = e0;
            }
            output[i] = small;
            output[i+1] = large;
        }
    }

    static void absSwapBitwise(float[] values, float[] output)
    {
        int area = values.length / 2;
        int len = area * 2;
        for (int i = 0; i < len; i += 2) {
            float e0 = values[i];
            float e1 = values[i+1];
            int isSmaller = Float.floatToRawIntBits(Math.abs(e0) - Math.abs(e1)) >> 31;
            float small = Float.intBitsToFloat(isSmaller & 0x3f000000) * e0 + Float.intBitsToFloat(~isSmaller & 0x3f000000) * e1;
            float large = Float.intBitsToFloat(~isSmaller & 0x3f000000) * e0 + Float.intBitsToFloat(isSmaller & 0x3f000000) * e1;
            output[i] = small;
            output[i+1] = large;
        }
    }
}
