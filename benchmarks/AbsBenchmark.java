// "bitwise" and "math" are equivalent in speed
// "if" is 35% slower

public class FiltersBenchmark
{
    public static final int METHOD_IF = 0;
    public static final int METHOD_MATH = 1;
    public static final int METHOD_BITWISE = 2;

    public static final String[] methodNames = new String[] {
        "if",
        "math",
        "bitwise"
    };

    public static void main(String[] args)
    {
        int method = METHOD_IF;

        int iterations = 10000;
        int width = 640;
        int height = 480;
        int[] input = new int[width * height];
        int[] output = new int[width * height];

        int x = 0;
        for (int i = 0; i < input.length; i++) {
            x += 1337;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = (x >> 16) ^ x;
            input[i] = x;
        }

        long startTime = System.nanoTime();

        switch (method) {
            case METHOD_IF:
                for (int i = 0; i < iterations; i++)
                    absIf(output, input, width, height);
                break;
            case METHOD_MATH:
                for (int i = 0; i < iterations; i++)
                    absMath(output, input, width, height);
                break;
            case METHOD_BITWISE:
                for (int i = 0; i < iterations; i++)
                    absBitwise(output, input, width, height);
                break;
        }

        long endTime = System.nanoTime();

        System.out.println(
            FiltersBenchmark.class.getSimpleName() + "\n" +
            "Method: " + methodNames[method] + "\n" +
            "Area: " + width + "x" + height + "\n" +
            "Iterations: " + iterations + "\n" +
            "Time: " + (double)(endTime - startTime) / 1E6 + "ms"
        );
    }

    public static void absIf(int[] output, int[] input, int width, int height)
    {
        int area = width * height;
        for (int i = 0; i < area; i++) {
            int v = input[i];
            if (v < 0)
                v = -v;
            output[i] = v;
        }
    }

    public static void absMath(int[] output, int[] input, int width, int height)
    {
        int area = width * height;
        for (int i = 0; i < area; i++)
            output[i] = Math.abs(input[i]);
    }

    public static void absBitwise(int[] output, int[] input, int width, int height)
    {
        int area = width * height;
        for (int i = 0; i < area; i++) {
            int sign = input[i] >> 31;
            output[i] = (input[i] ^ sign) - sign;
        }
    }
}
