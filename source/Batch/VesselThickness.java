package Batch;

public class VesselThickness
{
    public static void computeThickness(
        ISliceRunner runner,
        int maxWorkers,
        float[] output,
        byte[] input,
        int width,
        int height
    ) {
        final int thresh = 200;

        step1(input, output, width, height, thresh);

        runner.runSlices(new Step2(output, width, height), maxWorkers, width, Step2.IN_PLACE_THRESHOLD - 1);

        // There was a step 3, but since it only iterated over the depth of the image, it was entirely redundant

        //float distMax = 0.0f;
        int area = width * height;

        for (int i = 0; i < area; i++) {
            float dist = 0.0f;
            if ((input[i] & 255) >= thresh) {
                dist = (float)Math.sqrt(output[i]);
                //distMax = Math.max(dist, distMax);
            }
            output[i] = dist;
        }

        /*
        String title = this.stripExtension(this.imp.getTitle());
        this.impOut = new ImagePlus(title + "EDT", sStack);
        this.impOut.getProcessor().setMinAndMax(0.0, (double)distMax);
        long end = System.currentTimeMillis();
        */
    }

    static void step1(byte[] input, float[] output, int width, int height, int thresh)
    {
        final int maxDimension = Math.max(width, height);
        final int maxResult = 3 * (maxDimension + 1) * (maxDimension + 1);

        for (int j = 0; j < height; j++) {
            for(int i = 0; i < width; ++i) {
                int min = maxResult;

                for(int x = i; x < width; ++x) {
                    if ((input[x + width * j] & 255) < thresh) {
                        int test = i - x;
                        test *= test;
                        min = test;
                        break;
                    }
                }

                for(int x = i - 1; x >= 0; --x) {
                    if ((input[x + width * j] & 255) < thresh) {
                        int test = i - x;
                        test *= test;
                        min = Math.min(test, min);
                        break;
                    }
                }

                output[i + width * j] = (float)min;
            }
        }
    }

    static class Step2 implements ISliceCompute
    {
        static final int IN_PLACE_THRESHOLD = 250;

        private final int width;
        private final int height;
        private final float[] src;

        public Step2(float[] src, int width, int height)
        {
            this.src = src;
            this.width = width;
            this.height = height;
        }

        @Override
        public Object computeSlice(int sliceIdx, int start, int length)
        {
            final int maxDimension = Math.max(width, height);
            final int maxResult = 3 * (maxDimension + 1) * (maxDimension + 1);

            int[] tempInt = IntBufferPool.acquireAsIs(maxDimension);

            for(int x = start; x < start + length; ++x) {
                boolean empty = true;

                for(int y = 0; empty && y < height; ++y)
                    empty = empty && (int)src[x + width * y] == 0;

                if (empty)
                    continue;

                for(int y1 = 0; y1 < height; ++y1) {
                    int min = maxResult;
                    int delta = y1;

                    for(int y2 = 0; y2 < height; ++y2)
                        min = Math.min((int)src[x + width * y2] + delta * delta--, min);

                    tempInt[y1] = min;
                }

                for(int y = 0; y < height; ++y)
                    src[x + width * y] = (float)tempInt[y];
            }

            IntBufferPool.release(tempInt);
            return null;
        }
    }
}
