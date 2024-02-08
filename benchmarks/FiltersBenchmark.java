// "if" is the fastest
// "math" is 20% slower
// "bitwise" is 150% slower

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
			int offset = y * width;
			p2 = inputPixels[offset-width] & 0xff;
			p3 = inputPixels[offset-width+1] & 0xff;
			p5 = inputPixels[offset] & 0xff;
			p6 = inputPixels[offset+1] & 0xff;
			p8 = inputPixels[offset+width] & 0xff;
			p9 = inputPixels[offset+width+1] & 0xff;

			for (int x = 1; x < width-1; x++) {
				p1 = p2; p2 = p3;
				p3 = inputPixels[offset-width+1] & 0xff;
				p4 = p5; p5 = p6;
				p6 = inputPixels[offset+1] & 0xff;
				p7 = p8; p8 = p9;
				p9 = inputPixels[offset+width+1] & 0xff;

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

			p1 = inputPixels[left + width * top];
			p2 = inputPixels[x + width * top];
			p3 = inputPixels[right + width * top];
			p4 = inputPixels[left + width * y];
			p5 = inputPixels[x + width * y];
			p6 = inputPixels[right + width * y];
			p7 = inputPixels[left + width * bottom];
			p8 = inputPixels[x + width * bottom];
			p9 = inputPixels[right + width * bottom];

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

    public static void filterMaxIf(byte[] outputPixels, byte[] inputPixels, int width, int height)
    {
		if (width == 1) {
        	filterEdgeMaxIf(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        	return;
		}

        int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int max = 0;

		for (int y = 1; y < height-1; y++) {
			int offset = y * width;
			p2 = inputPixels[offset-width] & 0xff;
			p3 = inputPixels[offset-width+1] & 0xff;
			p5 = inputPixels[offset] & 0xff;
			p6 = inputPixels[offset+1] & 0xff;
			p8 = inputPixels[offset+width] & 0xff;
			p9 = inputPixels[offset+width+1] & 0xff;

			for (int x = 1; x < width-1; x++) {
				p1 = p2; p2 = p3;
				p3 = inputPixels[offset-width+1] & 0xff;
				p4 = p5; p5 = p6;
				p6 = inputPixels[offset+1] & 0xff;
				p7 = p8; p8 = p9;
				p9 = inputPixels[offset+width+1] & 0xff;

				max = p5;
				if (p1>max) max = p1;
				if (p2>max) max = p2;
				if (p3>max) max = p3;
				if (p4>max) max = p4;
				if (p6>max) max = p6;
				if (p7>max) max = p7;
				if (p8>max) max = p8;
				if (p9>max) max = p9;

				outputPixels[offset+x] = (byte)max;
			}
		}

        filterEdgeMaxIf(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        filterEdgeMaxIf(outputPixels, inputPixels, width, height, width, 0, 0, 1, 0);
        filterEdgeMaxIf(outputPixels, inputPixels, width, height, height, width-1, 0, 0, 1);
        filterEdgeMaxIf(outputPixels, inputPixels, width, height, width, 0, height-1, 1, 0);
    }

	public static void filterEdgeMaxIf(byte[] outputPixels, byte[] inputPixels, int width, int height, int n, int x, int y, int xinc, int yinc)
	{
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int max = 0;
        int count;

		for (int i=0; i<n; i++) {
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

            max = p5;
            if (p1>max) max = p1;
            if (p2>max) max = p2;
            if (p3>max) max = p3;
            if (p4>max) max = p4;
            if (p6>max) max = p6;
            if (p7>max) max = p7;
            if (p8>max) max = p8;
            if (p9>max) max = p9;

            outputPixels[x+y*width] = (byte)max;
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
			int offset = y * width;
			p2 = inputPixels[offset-width] & 0xff;
			p3 = inputPixels[offset-width+1] & 0xff;
			p5 = inputPixels[offset] & 0xff;
			p6 = inputPixels[offset+1] & 0xff;
			p8 = inputPixels[offset+width] & 0xff;
			p9 = inputPixels[offset+width+1] & 0xff;

			for (int x = 1; x < width-1; x++) {
				p1 = p2; p2 = p3;
				p3 = inputPixels[offset-width+1] & 0xff;
				p4 = p5; p5 = p6;
				p6 = inputPixels[offset+1] & 0xff;
				p7 = p8; p8 = p9;
				p9 = inputPixels[offset+width+1] & 0xff;

			    min = p5;
			    min = Math.min(p1, min);
			    min = Math.min(p2, min);
			    min = Math.min(p3, min);
			    min = Math.min(p4, min);
			    min = Math.min(p6, min);
			    min = Math.min(p7, min);
			    min = Math.min(p8, min);
			    min = Math.min(p9, min);

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

			p1 = inputPixels[left + width * top];
			p2 = inputPixels[x + width * top];
			p3 = inputPixels[right + width * top];
			p4 = inputPixels[left + width * y];
			p5 = inputPixels[x + width * y];
			p6 = inputPixels[right + width * y];
			p7 = inputPixels[left + width * bottom];
			p8 = inputPixels[x + width * bottom];
			p9 = inputPixels[right + width * bottom];

            min = p5;
            min = Math.min(p1, min);
		    min = Math.min(p2, min);
		    min = Math.min(p3, min);
		    min = Math.min(p4, min);
		    min = Math.min(p6, min);
		    min = Math.min(p7, min);
		    min = Math.min(p8, min);
		    min = Math.min(p9, min);

            outputPixels[x+y*width] = (byte)min;
            x += xinc;
            y += yinc;
        }
    }

    public static void filterMaxMath(byte[] outputPixels, byte[] inputPixels, int width, int height)
    {
		if (width == 1) {
        	filterEdgeMaxMath(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        	return;
		}

        int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int max = 0;

		for (int y = 1; y < height-1; y++) {
			int offset = y * width;
			p2 = inputPixels[offset-width] & 0xff;
			p3 = inputPixels[offset-width+1] & 0xff;
			p5 = inputPixels[offset] & 0xff;
			p6 = inputPixels[offset+1] & 0xff;
			p8 = inputPixels[offset+width] & 0xff;
			p9 = inputPixels[offset+width+1] & 0xff;

			for (int x = 1; x < width-1; x++) {
				p1 = p2; p2 = p3;
				p3 = inputPixels[offset-width+1] & 0xff;
				p4 = p5; p5 = p6;
				p6 = inputPixels[offset+1] & 0xff;
				p7 = p8; p8 = p9;
				p9 = inputPixels[offset+width+1] & 0xff;

				max = p5;
				max = Math.max(p1, max);
			    max = Math.max(p2, max);
			    max = Math.max(p3, max);
			    max = Math.max(p4, max);
			    max = Math.max(p6, max);
			    max = Math.max(p7, max);
			    max = Math.max(p8, max);
			    max = Math.max(p9, max);

				outputPixels[offset+x] = (byte)max;
			}
		}

        filterEdgeMaxMath(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        filterEdgeMaxMath(outputPixels, inputPixels, width, height, width, 0, 0, 1, 0);
        filterEdgeMaxMath(outputPixels, inputPixels, width, height, height, width-1, 0, 0, 1);
        filterEdgeMaxMath(outputPixels, inputPixels, width, height, width, 0, height-1, 1, 0);
    }

	public static void filterEdgeMaxMath(byte[] outputPixels, byte[] inputPixels, int width, int height, int n, int x, int y, int xinc, int yinc)
	{
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int max = 0;
        int count;

		for (int i=0; i<n; i++) {
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

            max = p5;
			max = Math.max(p1, max);
		    max = Math.max(p2, max);
		    max = Math.max(p3, max);
		    max = Math.max(p4, max);
		    max = Math.max(p6, max);
		    max = Math.max(p7, max);
		    max = Math.max(p8, max);
		    max = Math.max(p9, max);

            outputPixels[x+y*width] = (byte)max;
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
			int offset = y * width;
			p2 = inputPixels[offset-width] & 0xff;
			p3 = inputPixels[offset-width+1] & 0xff;
			p5 = inputPixels[offset] & 0xff;
			p6 = inputPixels[offset+1] & 0xff;
			p8 = inputPixels[offset+width] & 0xff;
			p9 = inputPixels[offset+width+1] & 0xff;

			for (int x = 1; x < width-1; x++) {
				p1 = p2; p2 = p3;
				p3 = inputPixels[offset-width+1] & 0xff;
				p4 = p5; p5 = p6;
				p6 = inputPixels[offset+1] & 0xff;
				p7 = p8; p8 = p9;
				p9 = inputPixels[offset+width+1] & 0xff;

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

			p1 = inputPixels[left + width * top];
			p2 = inputPixels[x + width * top];
			p3 = inputPixels[right + width * top];
			p4 = inputPixels[left + width * y];
			p5 = inputPixels[x + width * y];
			p6 = inputPixels[right + width * y];
			p7 = inputPixels[left + width * bottom];
			p8 = inputPixels[x + width * bottom];
			p9 = inputPixels[right + width * bottom];

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

    public static void filterMaxBitwise(byte[] outputPixels, byte[] inputPixels, int width, int height)
    {
		if (width == 1) {
        	filterEdgeMaxBitwise(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        	return;
		}

        int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int max = 0;

		for (int y = 1; y < height-1; y++) {
			int offset = y * width;
			p2 = inputPixels[offset-width] & 0xff;
			p3 = inputPixels[offset-width+1] & 0xff;
			p5 = inputPixels[offset] & 0xff;
			p6 = inputPixels[offset+1] & 0xff;
			p8 = inputPixels[offset+width] & 0xff;
			p9 = inputPixels[offset+width+1] & 0xff;

			for (int x = 1; x < width-1; x++) {
				p1 = p2; p2 = p3;
				p3 = inputPixels[offset-width+1] & 0xff;
				p4 = p5; p5 = p6;
				p6 = inputPixels[offset+1] & 0xff;
				p7 = p8; p8 = p9;
				p9 = inputPixels[offset+width+1] & 0xff;

			    max = p5;
			    max = (((max-p1) >> 31) & p1) | (~((max-p1) >> 31) & max);
			    max = (((max-p2) >> 31) & p2) | (~((max-p2) >> 31) & max);
			    max = (((max-p3) >> 31) & p3) | (~((max-p3) >> 31) & max);
			    max = (((max-p4) >> 31) & p4) | (~((max-p4) >> 31) & max);
			    max = (((max-p6) >> 31) & p6) | (~((max-p6) >> 31) & max);
			    max = (((max-p7) >> 31) & p7) | (~((max-p7) >> 31) & max);
			    max = (((max-p8) >> 31) & p8) | (~((max-p8) >> 31) & max);
			    max = (((max-p9) >> 31) & p9) | (~((max-p9) >> 31) & max);

				outputPixels[offset+x] = (byte)max;
			}
		}

        filterEdgeMaxBitwise(outputPixels, inputPixels, width, height, height, 0, 0, 0, 1);
        filterEdgeMaxBitwise(outputPixels, inputPixels, width, height, width, 0, 0, 1, 0);
        filterEdgeMaxBitwise(outputPixels, inputPixels, width, height, height, width-1, 0, 0, 1);
        filterEdgeMaxBitwise(outputPixels, inputPixels, width, height, width, 0, height-1, 1, 0);
    }

	public static void filterEdgeMaxBitwise(byte[] outputPixels, byte[] inputPixels, int width, int height, int n, int x, int y, int xinc, int yinc)
	{
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int max = 0;
        int count;

		for (int i=0; i<n; i++) {
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

		    max = p5;
		    max = (((max-p1) >> 31) & p1) | (~((max-p1) >> 31) & max);
		    max = (((max-p2) >> 31) & p2) | (~((max-p2) >> 31) & max);
		    max = (((max-p3) >> 31) & p3) | (~((max-p3) >> 31) & max);
		    max = (((max-p4) >> 31) & p4) | (~((max-p4) >> 31) & max);
		    max = (((max-p6) >> 31) & p6) | (~((max-p6) >> 31) & max);
		    max = (((max-p7) >> 31) & p7) | (~((max-p7) >> 31) & max);
		    max = (((max-p8) >> 31) & p8) | (~((max-p8) >> 31) & max);
		    max = (((max-p9) >> 31) & p9) | (~((max-p9) >> 31) & max);

            outputPixels[x+y*width] = (byte)max;
            x += xinc;
            y += yinc;
        }
    }
}
