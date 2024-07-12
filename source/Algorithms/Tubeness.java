package Algorithms;

import Utils.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import Pixels.ImageFile;

public class Tubeness
{
    public static final int IN_PLACE_THRESHOLD = 5;

    public static void computeTubenessImage(
        ISliceRunner sliceRunner,
        int maxWorkers,
        byte[] output,
        float[] image,
        int width,
        int height,
        int brightestChannel,
        double[] sigma,
        int nSigmas
    ) throws ExecutionException
    {
        final int area = width * height;
        float[] gaussianOutput = FloatBufferPool.acquireAsIs(area);
        float[] maxEigenOutput = FloatBufferPool.acquireZeroed(area);
        byte[] rainbowGold = ByteBufferPool.acquireZeroed(area);

        double maxResult = 0.0;

        for (int s = 0; s < nSigmas; s++) {
            computeGaussianFastMirror(
                gaussianOutput,
                image,
                width,
                height,
                sigma[s]
            );
            double highestValue = computeEigenvalues(
                maxEigenOutput,
                gaussianOutput,
                rainbowGold,
                width,
                height,
                sigma[s]
            );
            maxResult = Math.max(maxResult, highestValue);
        }

        FloatBufferPool.release(gaussianOutput);

        ImageFile.writePgm(rainbowGold, width, height, "pots-o-gold.pgm");
        ByteBufferPool.release(rainbowGold);

        float factor = maxResult > 0.0 ? (float)(256.0 / maxResult) : 1.0f;
        //System.out.println("maxResult: " + maxResult + ", factor: " + factor);

        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++)
                output[x+width*y] = (byte)(Math.min(maxEigenOutput[x+width*y] * factor, 255.0f));
        }

        FloatBufferPool.release(maxEigenOutput);

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

    private static void computeGaussianFastMirror(
        float[] output,
        float[] image,
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
                output[x + width * y] = avg;
            }
            for (int x = edgeSizeX; x < width - edgeSizeX; x++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++)
                    avg += kernel[f + ksHalf] * image[x+f + width * y];

                output[x + width * y] = avg;
            }
            for (int x = width - edgeSizeX; x < width; x++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++) {
                    int xf = Math.abs(x+f) % (2*width-2);
                    int xfm = Math.min(xf, 2*width-2 - xf);
                    avg += kernel[f + ksHalf] * image[xfm + width * y];
                }
                output[x + width * y] = avg;
            }
        }

        float[] tempColumn = FloatBufferPool.acquireAsIs(height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < edgeSizeY; y++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++) {
                    int yf = Math.abs(y+f) % (2*height-2);
                    int yfm = Math.min(yf, 2*height-2 - yf);
                    avg += kernel[f + ksHalf] * output[x + width * yfm];
                }
                tempColumn[y] = avg;
            }
            for (int y = edgeSizeY; y < height - edgeSizeY; y++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++)
                    avg += kernel[f + ksHalf] * output[x + width * (y+f)];

                tempColumn[y] = avg;
            }
            for (int y = height - edgeSizeY; y < height; y++) {
                float avg = 0.0f;
                for (int f = -ksHalf; f <= ksHalf; f++) {
                    int yf = Math.abs(y+f) % (2*height-2);
                    int yfm = Math.min(yf, 2*height-2 - yf);
                    avg += kernel[f + ksHalf] * output[x + width * yfm];
                }
                tempColumn[y] = avg;
            }
            for (int y = 0; y < height; y++)
                output[x + width * y] = tempColumn[y];
        }

        FloatBufferPool.release(tempColumn);
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
        float[] output,
        float[] input,
        byte[] extra,
        int width,
        int height,
        double sigma
    ) throws ExecutionException
    {
        final int threshold = 3;
        double maximum = 0.0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int pos = x + width*y;
                if (input[pos] <= threshold)
                    continue;

                double s2 = sigma * sigma;
                float dblCenter = 2.0F * input[pos];
                float corners = (
                    ((input[pos+1+width] - input[pos-1+width]) / 2.0f) -
                    ((input[pos+1-width] - input[pos-1-width]) / 2.0f)
                ) / 2.0f;

                // mA = matrix[0][0]
                // mB = matrix[0][1] = matrix[1][0]
                // mC = matrix[1][1]
                double mA = (input[pos+1] - dblCenter + input[pos-1]) * s2;
                double mB = corners * s2;
                double mC = (input[pos+width] - dblCenter + input[pos-width]) * s2;

                double b = mA + mC;
                double c = mA * mC - mB * mB;
                double discriminant = b * b - 4.0 * c;

                if (discriminant < 0.0)
                    continue;

                double dR = Math.sqrt(discriminant);
                float e0 = (float)((b + dR) / 2.0);
                float e1 = (float)((b - dR) / 2.0);

                float small, large;
                if (Math.abs(e0) > Math.abs(e1)) {
                    large = e0;
                    small = e1;
                }
                else {
                    large = e1;
                    small = e0;
                }

                if (large < 0.0f) {
                    float sub0 = (float)mA - small;
                    float sub1 = (float)mA - large;

                    float a1, a2, comp;
                    if (Math.abs(sub1) > Math.abs((float)mB)) {
                        a1 = -(float)mB;
                        a2 = sub1;
                        comp = sub1;
                    }
                    else {
                        a1 = sub0;
                        a2 = (float)mB;
                        comp = sub0;
                    }

                    float len = (float)Math.sqrt(comp * comp + mB * mB);
                    a1 *= 2.0f / len;
                    a2 *= 2.0f / len;
                    int x1 = (int)(((float)x + 0.5f) - a1);
                    int x2 = (int)(((float)x + 0.5f) + a1);
                    int y1 = (int)(((float)y + 0.5f) - a2);
                    int y2 = (int)(((float)y + 0.5f) + a2);
                    if (x1 >= 0 && y1 >= 0 && x1 < width && y1 < height)
                        extra[x1 + width * y1] += (int)len;
                    if (x2 >= 0 && y2 >= 0 && x2 < width && y2 < height)
                        extra[x2 + width * y2] += (int)len;

                    output[pos] = Math.max(output[pos], -large);
                    maximum = Math.max(maximum, -large);
                }
            }
        }

        return maximum;
    }
}
