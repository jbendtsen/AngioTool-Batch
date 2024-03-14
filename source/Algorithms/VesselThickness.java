package Algorithms;

import Utils.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class VesselThickness
{
    public static double computeMedianVesselThickness(
        ISliceRunner runner,
        int maxWorkers,
        int[] points,
        int arraySize,
        int pointSize,
        int[] scratch,
        byte[] input,
        int width,
        int height
    ) throws ExecutionException
    {
        final int thresh = 200;

        runner.runSlices(new Step1(scratch, input, width, height, thresh), maxWorkers, height, (height / 4) + 1);

        final int nPoints = arraySize / pointSize;
        double[] vesselThickness = DoubleBufferPool.acquireAsIs(nPoints);

        final int maxDimension = Math.max(width, height);
        final int maxResult = 3 * (maxDimension + 1) * (maxDimension + 1);

        for(int i = 0; i < nPoints; i++) {
            int x  = points[i*pointSize];
            int y1 = points[i*pointSize + 1];

            if ((input[x + width * y1] & 255) < thresh) {
                vesselThickness[i] = 0.0;
                continue;
            }

            int min = maxResult;

            // the X and Y of 'scratch' were flipped in step 1
            for (int y2 = 0; y2 < height; ++y2)
                min = Math.min(scratch[y2 + height * x] + (y1-y2)*(y1-y2), min);

            double result = Math.sqrt(min);
            vesselThickness[i] = result;
        }

        Arrays.sort(vesselThickness, 0, nPoints);
        int middle = nPoints / 2;

        double thickness = nPoints % 2 == 1 ?
            vesselThickness[middle] :
            (vesselThickness[middle - 1] + vesselThickness[middle]) / 2.0;

        DoubleBufferPool.release(vesselThickness);

        return thickness * 2.0;
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

                    // swap X and Y to improve memory locality in the next step
                    output[j + height * i] = min;
                }
            }

            return null;
        }

        @Override
        public void finishSlice(ISliceCompute.Result res) {}
    }
}
