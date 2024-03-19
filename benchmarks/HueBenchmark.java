// "if" is 5% faster than "bitwise"

public class HueBenchmark
{
    public static final int METHOD_BITWISE = 0;
    public static final int METHOD_IF = 1;

    public static final String[] methodNames = new String[] {
        "bitwise",
        "if"
    };

    public static void main(String[] args)
    {
        int method = METHOD_IF;

        int iterations = 500;
        int width = 640;
        int height = 480;

        int[] input = new int[width * height];
        float[] output = new float[width * height];
        int[] colors = new int[iterations];

        int x = 0;
        for (int i = 0; i < input.length; i++) {
            x += 1337;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = (x >> 16) ^ x;
            input[i] = x;
        }

        for (int i = 0; i < colors.length; i++) {
            x += 1337;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = (x >> 16) ^ x;
            colors[i] = x;
        }

        long startTime = System.nanoTime();

        switch (method) {
            case METHOD_BITWISE:
                for (int i = 0; i < iterations; i++)
                    computeHueBitwise(output, input, width, height, colors[i]);
                break;
            case METHOD_IF:
                for (int i = 0; i < iterations; i++)
                    computeHueIf(output, input, width, height, colors[i]);
                break;
        }

        long endTime = System.nanoTime();

        System.out.println(
            HueBenchmark.class.getSimpleName() + "\n" +
            "Method: " + methodNames[method] + "\n" +
            "Area: " + width + "x" + height + "\n" +
            "Iterations: " + iterations + "\n" +
            "Time: " + (double)(endTime - startTime) / 1E6 + "ms"
        );
    }

    static final int[] MAX_CH_LUT = new int[] {16, 8, 0, 0, 16, 8, 16};

    public static void computeHueBitwise(float[] output, int[] input, int width, int height, int targetColor)
    {
        int area = width * height;

        // calculate target hue
        float targetHue = 0f;
        {
            int r = (targetColor >> 16) & 0xff;
            int g = (targetColor >> 8) & 0xff;
            int b = targetColor & 0xff;

            int hueType = (-((r - g) >> 31)) | (-((g - b) >> 31) << 1) | (-((b - r) >> 31) << 2);

            // prevent divide by zero
            if (hueType == 0) {
                for (int i = 0; i < area; i++)
                    output[i] = 0f;
                return;
            }

            float dMaxMin = Math.max(Math.max(r, g), b) - Math.min(Math.min(r, g), b);
            int maxChannel = MAX_CH_LUT[hueType];
            float dAlt = ((targetColor >> ((maxChannel + 16) % 24)) & 0xff) - ((targetColor >> ((maxChannel + 8) % 24)) & 0xff);
            targetHue = (4f - (float)maxChannel * 0.25f) + dAlt / dMaxMin;
        }

        for (int i = 0; i < area; i++) {
            int rgb = input[i];
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;

            int hueType = (-((r - g) >> 31)) | (-((g - b) >> 31) << 1) | (-((b - r) >> 31) << 2);

            int maxChannel = MAX_CH_LUT[hueType];
            int mask = (hueType - 1) >> 31;
            float dAlt = ~mask & (((rgb >> ((maxChannel + 16) % 24)) & 0xff) - ((rgb >> ((maxChannel + 8) % 24)) & 0xff));
            float dMaxMin = -mask + Math.max(Math.max(r, g), b) - Math.min(Math.min(r, g), b);

            float hue = (4f - (float)maxChannel * 0.25f) + dAlt / dMaxMin;
            output[i] = (((hue - targetHue) + 6f) % 6f);
        }
    }

    public static void computeHueIf(float[] output, int[] input, int width, int height, int targetColor)
    {
        int area = width * height;

        // calculate target hue
        float targetHue = 0f;
        {
            int r = (targetColor >> 16) & 0xff;
            int g = (targetColor >> 8) & 0xff;
            int b = targetColor & 0xff;

            float max = Math.max(Math.max(r, g), b);
            float dMaxMin = max - Math.min(Math.min(r, g), b);

            // prevent divide by zero
            if (dMaxMin == 0f) {
                for (int i = 0; i < area; i++)
                    output[i] = 0f;
                return;
            }

            if (max == (float)r) {
                targetHue = (g - b) / dMaxMin;
            }
            else if (max == (float)g) {
                targetHue = 2f + (b - r) / dMaxMin;
            }
            else {
                targetHue = 4f + (r - g) / dMaxMin;
            }
        }

        for (int i = 0; i < area; i++) {
            int rgb = input[i];
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;

            float hue = targetHue;
            float max = Math.max(Math.max(r, g), b);
            float dMaxMin = max - Math.min(Math.min(r, g), b);
            if (dMaxMin != 0f) {
                if (max == (float)r) {
                    hue = (g - b) / dMaxMin;
                }
                else if (max == (float)g) {
                    hue = 2f + (b - r) / dMaxMin;
                }
                else {
                    hue = 4f + (r - g) / dMaxMin;
                }
            }

            output[i] = (((hue - targetHue) + 6f) % 6f);
        }
    }
}
