package Algorithms;

import Utils.*;
import java.util.Arrays;

public class Tubeness
{
    public static final int IN_PLACE_THRESHOLD = 5;

    public static class Scratch
    {
        public Params params = new Params();
        public float[] image;
        public float[] maxEigenOutput;
        public float[] gaussianOutput;
        public float[] scratchBuf;

        public void useBuffers(float[] image, float[] maxEigenOutput, float[] gaussianOutput, float[] scratchBuf)
        {
            this.image = image;
            this.maxEigenOutput = maxEigenOutput;
            this.gaussianOutput = gaussianOutput;
            this.scratchBuf = scratchBuf;
        }
    }

    public static void computeTubenessImage(
        Scratch data,
        ISliceRunner sliceRunner,
        int maxWorkers,
        byte[] output,
        int[] input,
        int width,
        int height,
        int brightestChannel,
        double[] sigma,
        int nSigmas
    ) {
        final int area = width * height;
        for (int i = 0; i < area; i++)
            data.image[i] = (float)((input[i] >> (8 * (2 - brightestChannel))) & 0xff);

        Arrays.fill(data.maxEigenOutput, 0, area, 0.0f);

        double maxResult = 0.0;
        for (int s = 0; s < nSigmas; s++) {
            computeGaussianFastMirror(
                data.gaussianOutput,
                data.image,
                data.scratchBuf,
                width,
                height,
                sigma[s]
            );
            double highestValue = computeEigenvalues(
                data,
                sliceRunner,
                maxWorkers,
                data.maxEigenOutput,
                data.gaussianOutput,
                width,
                height,
                sigma[s]
            );
            maxResult = Math.max(maxResult, highestValue);
        }

        float factor = maxResult > 0.0 ? (float)(256.0 / maxResult) : 1.0f;
        //System.out.println("maxResult: " + maxResult + ", factor: " + factor);

        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++)
                output[x+width*y] = (byte)(Math.min(data.maxEigenOutput[x+width*y] * factor, 255.0f));
        }

        if (width >= 2) {
            for (int y = 1; y < height-1; y++) {
                output[width*y] = output[1+width*y];
                output[width*(y+1)-1] = output[width*(y+1)-2];
            }
        }

        if (height >= 2) {
            for (int x = 1; x < width-1; x++)
                output[x] = output[x+width];
            for (int x = 1; x < width-1; x++)
                output[x+width*(height-1)] = output[x+width*(height-2)];
        }

        if (width >= 2 && height >= 2) {
            output[0] = output[width+1];
            output[width-1] = output[2*width-2];
            output[width*(height-1)] = output[1+width*(height-2)];
            output[width*height-1] = output[width*(height-1)-2];
        }
    }

    // scratch MUST NOT ALIAS image
    private static void computeGaussianFastMirror(
        float[] output,
        float[] image,
        float[] scratch,
        int width,
        int height,
        double sigma
    ) {
        int kernelSize = determineGaussianKernelSize(sigma);
        float[] kernel = FloatBufferPool.acquireAsIs(kernelSize);
        populateGaussianKernel1D(kernel, sigma);

        final int ksHalf = kernelSize / 2;
        final int edgeSizeX = Math.min(ksHalf + 1, width / 2);
        final int edgeSizeY = Math.min(ksHalf + 1, height / 2);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < edgeSizeX; x++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++) {
                    int xf = Math.abs(x+f) % (2*width-2);
                    int xfm = Math.min(xf, 2*width-2 - xf);
                    avg += kernel[f + ksHalf] * image[xfm + width * y];
                }
                scratch[x + width * y] = avg;
            }
            for (int x = edgeSizeX; x < width - edgeSizeX; x++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++)
                    avg += kernel[f + ksHalf] * image[x+f + width * y];

                scratch[x + width * y] = avg;
            }
            for (int x = width - edgeSizeX; x < width; x++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++) {
                    int xf = Math.abs(x+f) % (2*width-2);
                    int xfm = Math.min(xf, 2*width-2 - xf);
                    avg += kernel[f + ksHalf] * image[xfm + width * y];
                }
                scratch[x + width * y] = avg;
            }
        }

        for (int y = 0; y < edgeSizeY; y++) {
            for (int x = 0; x < width; x++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++) {
                    int yf = Math.abs(y+f) % (2*height-2);
                    int yfm = Math.min(yf, 2*height-2 - yf);
                    avg += kernel[f + ksHalf] * scratch[x + width * yfm];
                }
                output[x + width * y] = avg;
            }
        }
        for (int y = edgeSizeY; y < height - edgeSizeY; y++) {
            for (int x = 0; x < width; x++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++)
                    avg += kernel[f + ksHalf] * scratch[x + width * (y+f)];

                output[x + width * y] = avg;
            }
        }
        for (int y = height - edgeSizeY; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++) {
                    int yf = Math.abs(y+f) % (2*height-2);
                    int yfm = Math.min(yf, 2*height-2 - yf);
                    avg += kernel[f + ksHalf] * scratch[x + width * yfm];
                }
                output[x + width * y] = avg;
            }
        }

        FloatBufferPool.release(kernel);
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
        Scratch data,
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
            data.params.setup(input, width, height, sigma, 3, output);
            runner.runSlices(
                data.params,
                maxWorkers,
                width,
                IN_PLACE_THRESHOLD - 1
            );
            highestValue = data.params.finalMaximum;
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
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
        public float[] data;
        public int width;
        public int height;
        public double sigma;
        public float threshold;
        public float[] output;
        public DoubleVector maximums = new DoubleVector();
        public double finalMaximum;

        public void setup(float[] data, int width, int height, double sigma, float threshold, float[] output)
        {
            this.data = data;
            this.width = width;
            this.height = height;
            this.sigma = sigma;
            this.threshold = threshold;
            this.output = output;
            this.finalMaximum = 0.0;
        }

        @Override
        public void initSlices(int nSlices)
        {
            this.maximums.resize(nSlices);
            Arrays.fill(this.maximums.buf, 0, nSlices, 0.0);
        }

        @Override
        public Object computeSlice(int sliceIdx, int start, int length)
        {
            int end = start + length;
            if (start <= 0)
                start = 1;
            if (end >= width)
                end = width - 1;

            for (int y = 1; y < height - 1; ++y) {
                for (int x = start; x < end; ++x) {
                    int index = x + width*y;
                    if (data[index] > threshold) {
                        float ev = findSecondHessianEigenvalueAtPoint2D(data, width, x, y, sigma);
                        if (ev < 0.0f) {
                            output[index] = Math.max(output[index], -ev);
                            maximums.buf[sliceIdx] = Math.max(maximums.buf[sliceIdx], -ev);
                        }
                    }
                }
            }

            return null;
        }

        @Override
        public void finishSlice(ISliceCompute.Result res)
        {
            finalMaximum = Math.max(finalMaximum, maximums.buf[res.idx]);
        }
    }
}
