package Batch;

import java.util.Arrays;
import java.util.concurrent.ThreadPoolExecutor;

public class ComputeEigenValuesAtPoint2D
{
    public static final int IN_PLACE_THRESHOLD = 5;

    public static void computeEigenvalues(
        ISliceRunner runner,
        int maxWorkers,
        float[] output,
        float[] input,
        int width,
        int height,
        double sigma,
        int threshold
    ) {
        int area = width * height;
        Arrays.fill(output, 0, width, 0.0f);
        Arrays.fill(output, area-width, area, 0.0f);
        try {
            runner.runSlices(
                new Params(input, width, height, sigma, threshold > 0 ? threshold : 3, output),
                maxWorkers,
                width,
                IN_PLACE_THRESHOLD - 1
            );
        }
        catch (Throwable ignored) {}
    }

    static float findSecondHessianEigenvalueAtPoint2D(float[] data, int width, int x, int y, double sigma)
    {
        double s2 = sigma * sigma;
        int pos = x + width*y;
        float dblCenter = 2.0F * data[pos];
        float corners = (
            ((data[pos+1+width] - data[pos-1+width]) / 2.0f) -
            ((data[pos+1-width] - data[pos-1-width]) / 2.0f)
        ) / 2.0f;

        // mA = matrix[0][0]
        // mB = matrix[0][1]
        // mC = matrix[1][0]
        // mD = matrix[1][1]
        double mA = (data[pos+1] - dblCenter + data[pos-1]) * s2;
        double mB = corners * s2;
        double mC = mB;
        double mD = (data[pos+width] - dblCenter + data[pos-width]) * s2;

        double a = 1.0;
        double b = -(mA + mD);
        double c = mA * mD - mB * mB;
        double discriminant = b * b - 4.0 * a * c;

        if (discriminant < 0.0)
            return 0.0f;

        double dR = Math.sqrt(discriminant);
        float e0 = (float)((-b + dR) / (2.0 * a));
        float e1 = (float)((-b - dR) / (2.0 * a));

        // return the second value, accounting for inverse
        return Math.abs(e0) <= Math.abs(e1) ? e1 : e0;
    }

    static class Params implements ISliceCompute
    {
        public final float[] data;
        public final int width;
        public final int height;
        public final double sigma;
        public final float threshold;
        public final float[] output;

        public Params(float[] data, int width, int height, double sigma, float threshold, float[] output)
        {
            this.data = data;
            this.width = width;
            this.height = height;
            this.sigma = sigma;
            this.threshold = threshold;
            this.output = output;
        }

        @Override
        public Object computeSlice(int sliceIdx, int start, int length)
        {
            int end = start + length;
            if (start <= 0)
                start = 1;
            if (end >= width - 1)
                end = width - 2;

            for (int y = 1; y < height - 1; ++y) {
                for (int x = start; x < end; ++x) {
                    int index = x + width*y;
                    float value = 0.0f;
                    if (data[index] > threshold) {
                        float ev = findSecondHessianEigenvalueAtPoint2D(data, width, x, y, sigma);
                        if (ev < 0.0f)
                            value = -ev;
                    }
                    output[index] = value;
                }
            }

            return null;
        }
    }
}
