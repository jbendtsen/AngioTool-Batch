package Batch;

public class Tubeness
{
    public static final int IN_PLACE_THRESHOLD = 5;

    public static void computeTubenessImage(
        ISliceRunner sliceRunner,
        byte[] output,
        byte[] input,
        int width,
        int height,
        float pixelWidth,
        float pixelHeight,
        double[] sigma,
        int nSigmas
    ) {
        final int area = width * height;
        float[] image = FloatBufferPool.acquireAsIs(area);
        for (int i = 0; i < area; i++)
            image[i] = (float)(input[i] & 0xff);

        float[] maxEigenOutput = FloatBufferPool.acquireZeroed(area);

        float[] gaussianOutput = FloatBufferPool.acquireAsIs(area);
        float[] scratchBuf = FloatBufferPool.acquireAsIs(area);

        double maxResult = 0.0;
        for (int s = 0; s < nSigmas; s++) {
            computeGaussianFastMirror(
                gaussianOutput,
                image,
                scratchBuf,
                width,
                height,
                sigma[s],
                pixelWidth,
                pixelHeight
            );
            double highestValue = computeEigenvalues(
                sliceRunner,
                Analyzer.MAX_WORKERS,
                maxEigenOutput,
                gaussianOutput,
                width,
                height,
                sigma[s]
            );
            maxResult = Math.max(maxResult, highestValue);
        }

        float factor = maxResult > 0.0 ? (float)(255.0 / maxResult) : 1.0f;

        for (int i = 0; i < area; i++)
            output[i] = (byte)(Math.max(maxEigenOutput[i] * factor, 255.0f));

        FloatBufferPool.release(maxEigenOutput);
        FloatBufferPool.release(gaussianOutput);
        FloatBufferPool.release(scratchBuf);
        FloatBufferPool.release(image);
    }

    // scratch MUST NOT ALIAS image
    private static void computeGaussianFastMirror(
        float[] output,
        float[] image,
        float[] scratch,
        int width,
        int height,
        double sigma,
        float pixelWidth,
        float pixelHeight
    ) {
        pixelWidth = pixelWidth > 0.0F ? pixelWidth : 1.0F;
        pixelHeight = pixelHeight > 0.0F ? pixelHeight : 1.0F;

        double sigmaW = sigma; // / pixelWidth;
        int kernelSizeX = determineGaussianKernelSize(sigmaW);
        float[] kernelX = FloatBufferPool.acquireAsIs(kernelSizeX);
        populateGaussianKernel1D(kernelX, sigmaW);

        double sigmaH = sigma; // / pixelHeight;
        int kernelSizeY = determineGaussianKernelSize(sigmaH);
        float[] kernelY = FloatBufferPool.acquireAsIs(kernelSizeY);
        populateGaussianKernel1D(kernelY, sigmaH);

        final int ksHalfX = kernelSizeX / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float avg = 0.0f;
                for (int f = -ksHalfX; f <= ksHalfX; f++) {
                    // xx := mirror(x+f)
                    //   int xf = abs(x+f) % (2*width)
                    int xf = (x+f - (((x+f) >> 31) & ((x+f) << 1))) % ((width-1) << 1);
                    int cond = (width - 1 - xf) >> 31;
                    //   int xx = xf >= width ? 2*width - xf : xf;
                    int xx = ((cond ^ xf) + cond) + (cond & (width << 1));
                    avg += kernelX[f + ksHalfX] * image[xx + width * y];
                }
                scratch[x + width * y] = avg;
            }
        }

        final int ksHalfY = kernelSizeY / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float avg = 0.0f;
                for (int f = -ksHalfY; f <= ksHalfY; f++) {
                    // yy := mirror(y+f)
                    //   int yf = abs(y+f) % (2*height)
                    int yf = (y+f - (((y+f) >> 31) & ((y+f) << 1))) % ((height-1) << 1);
                    int cond = (height - 1 - yf) >> 31;
                    //   int yy = yf >= height ? 2*height - yf : yf;
                    int yy = ((cond ^ yf) + cond) + (cond & (height << 1));
                    avg += kernelY[f + ksHalfY] * scratch[x + width * yy];
                }
                output[x + width * y] = avg;
            }
        }

        FloatBufferPool.release(kernelX);
        FloatBufferPool.release(kernelY);
    }

    private static int determineGaussianKernelSize(double sigma)
    {
        if (sigma <= 0.0F)
            return 3;

        double twoSigmaSq = 2.0 * sigma * sigma;
        int size = Math.max(3, 2 * (int)(3.0 * sigma + 0.5) + 1);
        return size;
    }

    private static void populateGaussianKernel1D(float[] kernel, double sigma)
    {
        if (sigma <= 0.0F) {
            kernel[0] = 0.0f;
            kernel[1] = 1.0f;
            kernel[2] = 0.0f;
            return;
        }

        double twoSigmaSq = 2.0 * sigma * sigma;
        int size = Math.max(3, 2 * (int)(3.0 * sigma + 0.5) + 1);

        for (int x = size / 2; x >= 0; --x) {
            float val = (float)Math.exp((double)(-x * x) / twoSigmaSq);
            kernel[size / 2 - x] = val;
            kernel[size / 2 + x] = val;
        }

        float sum = 0.0F;

        for (int i = 0; i < size; ++i)
            sum += kernel[i];

        for (int i = 0; i < size; ++i)
            kernel[i] /= sum;
    }

    static double computeEigenvalues(
        ISliceRunner runner,
        int maxWorkers,
        float[] output,
        float[] input,
        int width,
        int height,
        double sigma
    ) {
        double highestValue = 0.0;
        try {
            Params params = new Params(input, width, height, sigma, 3, output, maxWorkers);
            runner.runSlices(
                params,
                maxWorkers,
                width,
                IN_PLACE_THRESHOLD - 1
            );
            highestValue = params.finalMaximum;
        }
        catch (Throwable ignored) {}
        return highestValue;
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
        public final double[] maximums;
        public double finalMaximum;

        public Params(float[] data, int width, int height, double sigma, float threshold, float[] output, int maxWorkers)
        {
            this.data = data;
            this.width = width;
            this.height = height;
            this.sigma = sigma;
            this.threshold = threshold;
            this.output = output;
            this.maximums = new double[maxWorkers];
            this.finalMaximum = 0.0;
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
                    if (data[index] > threshold) {
                        float ev = findSecondHessianEigenvalueAtPoint2D(data, width, x, y, sigma);
                        if (ev < 0.0f) {
                            output[index] = Math.max(output[index], -ev);
                            maximums[sliceIdx] = Math.max(maximums[sliceIdx], -ev);
                        }
                    }
                }
            }

            return null;
        }

        @Override
        public void finishSlice(ISliceCompute.Result res)
        {
            finalMaximum = Math.max(finalMaximum, maximums[res.idx]);
        }
    }
}
