// "math" is the fastest
// "bitwise" is 2x slower than "math"
// "if" is 3x slower than "math"

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
        int method = METHOD_BITWISE;

        int iterations = 1000;
        int width = 640;
        int height = 480;
        byte[] input = new byte[width * height];
        byte[] output = new byte[width * height];

        int x = 0;
        for (int i = 0; i < input.length; i++) {
            x += 1337;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = ((x >> 16) ^ x) * 0x45d9f3b;
            x = (x >> 16) ^ x;
            input[i] = (byte)(x & 7);
        }

        long startTime = System.nanoTime();

        switch (method) {
            case METHOD_IF:
                for (int i = 0; i < iterations; i++)
                    filterMinIf(output, input, width, height);
                break;
            case METHOD_MATH:
                for (int i = 0; i < iterations; i++)
                    filterMinMath(output, input, width, height);
                break;
            case METHOD_BITWISE:
                for (int i = 0; i < iterations; i++)
                    filterMinBitwise(output, input, width, height);
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

    public static void filterMinIf(byte[] outputPixels, byte[] inputPixels, int width, int height)
    {
        if (width == 1) {
            filterEdgeMinIf(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
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
                if (p1<min) min = p1;
                if (p2<min) min = p2;
                if (p3<min) min = p3;
                if (p4<min) min = p4;
                if (p6<min) min = p6;
                if (p7<min) min = p7;
                if (p8<min) min = p8;
                if (p9<min) min = p9;

                outputPixels[offset+x] = (byte)min;
            }
        }

        filterEdgeMinIf(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        filterEdgeMinIf(outputPixels, inputPixels, width, height, width, 0, 0, 1, 0);
        filterEdgeMinIf(outputPixels, inputPixels, width, height, height, width-1, 0, 0, 1);
        filterEdgeMinIf(outputPixels, inputPixels, width, height, width, 0, height-1, 1, 0);
    }

    public static void filterEdgeMinIf(byte[] outputPixels, byte[] inputPixels, int width, int height, int n, int x, int y, int xinc, int yinc)
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
            if (p1<min) min = p1;
            if (p2<min) min = p2;
            if (p3<min) min = p3;
            if (p4<min) min = p4;
            if (p6<min) min = p6;
            if (p7<min) min = p7;
            if (p8<min) min = p8;
            if (p9<min) min = p9;

            outputPixels[x+y*width] = (byte)min;
            x += xinc;
            y += yinc;
        }
    }

    public static void filterMinMath(byte[] outputPixels, byte[] inputPixels, int width, int height)
    {
        if (width == 1) {
            filterEdgeMinMath(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
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

        filterEdgeMinMath(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        filterEdgeMinMath(outputPixels, inputPixels, width, height, width, 0, 0, 1, 0);
        filterEdgeMinMath(outputPixels, inputPixels, width, height, height, width-1, 0, 0, 1);
        filterEdgeMinMath(outputPixels, inputPixels, width, height, width, 0, height-1, 1, 0);
    }

    public static void filterEdgeMinMath(byte[] outputPixels, byte[] inputPixels, int width, int height, int n, int x, int y, int xinc, int yinc)
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

    public static void filterMinBitwise(byte[] outputPixels, byte[] inputPixels, int width, int height)
    {
        if (width == 1) {
            filterEdgeMinBitwise(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
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
			    min = (((p1-min) >> 31) & p1) | (~((p1-min) >> 31) & min);
			    min = (((p2-min) >> 31) & p2) | (~((p2-min) >> 31) & min);
			    min = (((p3-min) >> 31) & p3) | (~((p3-min) >> 31) & min);
			    min = (((p4-min) >> 31) & p4) | (~((p4-min) >> 31) & min);
			    min = (((p6-min) >> 31) & p6) | (~((p6-min) >> 31) & min);
			    min = (((p7-min) >> 31) & p7) | (~((p7-min) >> 31) & min);
			    min = (((p8-min) >> 31) & p8) | (~((p8-min) >> 31) & min);
			    min = (((p9-min) >> 31) & p9) | (~((p9-min) >> 31) & min);

                outputPixels[offset+x] = (byte)min;
            }
        }

        filterEdgeMinBitwise(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        filterEdgeMinBitwise(outputPixels, inputPixels, width, height, width, 0, 0, 1, 0);
        filterEdgeMinBitwise(outputPixels, inputPixels, width, height, height, width-1, 0, 0, 1);
        filterEdgeMinBitwise(outputPixels, inputPixels, width, height, width, 0, height-1, 1, 0);
    }

    public static void filterEdgeMinBitwise(byte[] outputPixels, byte[] inputPixels, int width, int height, int n, int x, int y, int xinc, int yinc)
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
		    min = (((p1-min) >> 31) & p1) | (~((p1-min) >> 31) & min);
		    min = (((p2-min) >> 31) & p2) | (~((p2-min) >> 31) & min);
		    min = (((p3-min) >> 31) & p3) | (~((p3-min) >> 31) & min);
		    min = (((p4-min) >> 31) & p4) | (~((p4-min) >> 31) & min);
		    min = (((p6-min) >> 31) & p6) | (~((p6-min) >> 31) & min);
		    min = (((p7-min) >> 31) & p7) | (~((p7-min) >> 31) & min);
		    min = (((p8-min) >> 31) & p8) | (~((p8-min) >> 31) & min);
		    min = (((p9-min) >> 31) & p9) | (~((p9-min) >> 31) & min);

            outputPixels[x+y*width] = (byte)min;
            x += xinc;
            y += yinc;
        }
    }
}
