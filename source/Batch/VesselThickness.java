package Batch;

public class VesselThickness
{
    public static void computeThickness(
        ISliceRunner runner,
        int maxWorkers,
        int[] points,
        int arraySize,
        int pointSize,
        float[] output,
        byte[] input,
        int[] scratch,
        int width,
        int height
    ) {
        final int thresh = 200;

        step1(scratch, input, width, height, thresh);

        try {
            runner.runSlices(new Step2(output, scratch, width, height), maxWorkers, width, Step2.IN_PLACE_THRESHOLD - 1);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }

        // There was a step 3, but since it only iterated over the depth of the image, it was entirely redundant

        float distMax = 0.0f;
        int area = width * height;

        for (int i = 0; i < area; i++) {
            float dist = 0.0f;
            if ((input[i] & 255) >= thresh) {
                dist = (float)Math.sqrt(output[i]);
                distMax = Math.max(dist, distMax);
            }
            output[i] = dist;
        }

        float factor = distMax > 0.0f ? 256.0f / distMax : 1.0f;
        for (int i = 0; i < area; i++)
            output[i] = Math.min(output[i] * factor, 255.0f);

        /*
        String title = this.stripExtension(this.imp.getTitle());
        this.impOut = new ImagePlus(title + "EDT", sStack);
        this.impOut.getProcessor().setMinAndMax(0.0, (double)distMax);
        long end = System.currentTimeMillis();
        */
    }

    static class Step1 implements ISliceCompute
    {
        final int[] output;
        final byte[] input;
        final int width;
        final int height;
        final int thresh;

        public Step1(int[] output, byte[] input, int width, int height, int thresh)
        {
            this.output = output;
            this.input = input;
            this.width = width;
            this.height = height;
            this.thresh = thresh;
        }

        @Override
        public void initSlices(int nSlices) {}

        @Override
        public Object computeSlice(int sliceIdx, int start, int length)
        {
            final int maxDimension = Math.max(width, height);
            final int maxResult = 3 * (maxDimension + 1) * (maxDimension + 1);

            for (int j = start; j < start + length; j++) {
                for (int i = 0; i < width; ++i) {
                    int min = maxResult;

                    for (int x = i; x < width; ++x) {
                        if ((input[x + width * j] & 255) < thresh) {
                            min = (i-x)*(i-x);
                            break;
                        }
                    }

                    for(int x = i - 1; x >= 0; --x) {
                        if ((input[x + width * j] & 255) < thresh) {
                            min = Math.min((i-x)*(i-x), min);
                            break;
                        }
                    }

                    // invert X and Y to improve locatlity in the next step
                    output[j + height * i] = min;
                }
            }

            return null;
        }

        @Override
        public void finishSlice(ISliceCompute.Result res) {}
    }

    static class Step2 implements ISliceCompute
    {
        static final int IN_PLACE_THRESHOLD = 250;

        final int width;
        final int height;
        final int[] src;
        final float[] dst;
        final int[] points;
        final int arraySize;
        final int pointsSize;

        public Step2(float[] dst, int[] src, int width, int height, int[] points, int arraySize, int pointsSize)
        {
            this.dst = dst;
            this.src = src;
            this.width = width;
            this.height = height;
            this.points = points;
            this.arraySize = arraySize;
            this.pointsSize = pointsSize;
        }

        @Override
        public void initSlices(int nSlices) {}

        @Override
        public Object computeSlice(int sliceIdx, int start, int length)
        {
            final int maxDimension = Math.max(width, height);
            final int maxResult = 3 * (maxDimension + 1) * (maxDimension + 1);

            for(int i = start; i < start + length; ++i) {
                /*
                boolean empty = true;

                for(int y = 0; empty && y < height; ++y)
                    empty = empty && src[x + width * y] == 0;

                if (empty) {
                    for (int y = 0; y < height; y++)
                        dst[x + width * y] = 0;
                    continue;
                }
                */
                int x  = points[i*pointSize];
                int y1 = points[i*pointSize + 1];

                int min = maxResult;

                // the X and Y of src were flipped in the previous step
                for (int y2 = 0; y2 < height; ++y2)
                    min = Math.min(src[y2 + height * x] + (y1-y2)*(y1-y2), min);

                dst[x + width * y1] = min;
            }

            return null;
        }

        @Override
        public void finishSlice(ISliceCompute.Result res) {}
    }
}
