package Batch;

public class Tubeness
{
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

        float[] maxOutput = FloatBufferPool.acquireZeroed(area);

        float[] gaussianOutput = FloatBufferPool.acquireAsIs(area);
        float[] eigenOutput = FloatBufferPool.acquireAsIs(area);

        float minResult = Float.MAX_VALUE;
        float maxResult = Float.MIN_VALUE;
        for (int s = 0; s < nSigmas; s++) {
            /*
            ImageProcessor ipp = original.getProcessor().duplicate();
            ComputeCurvatures c = new ComputeCurvatures();
            c.setData(new ImagePlus("", new FloatProcessor(width, height)));
            c.setSigma(this.sigma[s]);
            ComputeCurvatures.FloatArray2D fa2dInput = c.ImageToFloatArray(ipp);
            ComputeCurvatures.FloatArray2D fa2d = c.computeGaussianFastMirror(fa2dInput, (float)this.sigma[s], null, calibration);
            */

            // eigenOutput is used as scratch. Its contents are only useful after computeEigenvalues() is called
            computeGaussianFastMirror(gaussianOutput, image, eigenOutput, width, height, sigma[s], pixelWidth, pixelHeight);

            final int threshold = -1;
            ComputeEigenValuesAtPoint2D.computeEigenvalues(
                sliceRunner,
                Analyzer.MAX_WORKERS,
                eigenOutput,
                gaussianOutput,
                width,
                height,
                sigma[s],
                threshold
            );

            for(int i = 0; i < area; ++i) {
                maxOutput[i] = Math.max(maxOutput[i], eigenOutput[i]);
                /*
                if (eigenOutput[i] > image[i]) {
                    image[i] = eigenOutput[i];
                }

                if (slice[i] < minResult) {
                    minResult = slice[i];
                }

                if (slice[i] > maxResult) {
                    maxResult = slice[i];
                }
                */
            }
        }

        byte[] outputFile = new byte[maxOutput.length * 4];
        for (int i = 0; i < outputFile.length; i += 4) {
            int bits = Float.floatToIntBits(maxOutput[i>>2]);
            outputFile[i] = (byte)(bits >> 24);
            outputFile[i+1] = (byte)(bits >> 16);
            outputFile[i+2] = (byte)(bits >> 8);
            outputFile[i+3] = (byte)bits;
        }
        try {
            java.nio.file.Files.write(
                java.nio.file.FileSystems.getDefault().getPath("", "tubeness-result-float-r7.bin"),
                outputFile,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.WRITE
            );
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < area; i++)
            output[i] = (byte)maxOutput[i];

        /*
        try {
            java.nio.file.Files.write(
                java.nio.file.FileSystems.getDefault().getPath("", "tubeness-result-byte-r7.bin"),
                output,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.WRITE
            );
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        */

        FloatBufferPool.release(maxOutput);
        FloatBufferPool.release(gaussianOutput);
        FloatBufferPool.release(eigenOutput);
        FloatBufferPool.release(image);

        /*
        long end = System.currentTimeMillis();
        FloatProcessor fp = new FloatProcessor(width, height);
        fp.setPixels(slice);
        stack.addSlice(null, fp);

        ImagePlus result = new ImagePlus("processed " + original.getTitle(), stack);
        result.setCalibration(calibration);
        result.getProcessor().setMinAndMax((double)minResult, (double)maxResult);
        result.updateAndDraw();
        return result;
        */
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
}
