package Batch;

public class Tubeness
{
    @Override
    public float measureFromEvalues2D(float[] evalues, int vesselness)
    {
        float measure = 0.0F;
        return evalues[1] >= 0.0F ? 0.0F : Math.abs(evalues[1]);
    }

    @Override
    public float measureFromEvalues3D(float[] evalues)
    {
        return !(evalues[1] >= 0.0F) && !(evalues[2] >= 0.0F) ? (float)Math.sqrt((double)(evalues[2] * evalues[1])) : 0.0F;
    }

    public ImagePlus generateImage(ImagePlus original, double[] sigma)
    {
        int width = original.getWidth();
        int height = original.getHeight();
        int depth = original.getStackSize();
        ImageStack stack = new ImageStack(width, height);
        float minResult = Float.MAX_VALUE;
        float maxResult = Float.MIN_VALUE;
        //if (depth != 1) bail();

        float[] slice = new float[width * height];

        for(int s = 0; s < sigma.length; ++s) {
            ImageProcessor ipp = original.getProcessor().duplicate();

            /*
            ComputeCurvatures c = new ComputeCurvatures();
            c.setData(new ImagePlus("", new FloatProcessor(width, height)));
            c.setSigma(this.sigma[s]);
            ComputeCurvatures.FloatArray2D fa2dInput = c.ImageToFloatArray(ipp);
            ComputeCurvatures.FloatArray2D fa2d = c.computeGaussianFastMirror(fa2dInput, (float)this.sigma[s], null, calibration);
            */
            computeGaussianFastMirror(fa2dInput, sigma[s], calibration);

            ImagePlus fa2dIP = ComputeCurvatures.FloatArrayToImagePlus(fa2d, "fa2dIP", 0.0F, 255.0F);

            float[] slice2 = ComputeEigenValuesAtPoint2D.computeEigenvalues(Analyzer.threadPool, Analyzer.MAX_WORKERS, fa2dIP, this.sigma[s], this.threshold);

            for(int i = 0; i < slice.length; ++i) {
                if (slice2[i] > slice[i]) {
                    slice[i] = slice2[i];
                }

                if (slice[i] < minResult) {
                    minResult = slice[i];
                }

                if (slice[i] > maxResult) {
                    maxResult = slice[i];
                }
            }
        }

        long end = System.currentTimeMillis();
        FloatProcessor fp = new FloatProcessor(width, height);
        fp.setPixels(slice);
        stack.addSlice(null, fp);

        ImagePlus result = new ImagePlus("processed " + original.getTitle(), stack);
        result.setCalibration(calibration);
        result.getProcessor().setMinAndMax((double)minResult, (double)maxResult);
        result.updateAndDraw();
        return result;
    }

    // was float sigma
    public static ComputeCurvatures.FloatArray2D computeGaussianFastMirror(
        ComputeCurvatures.FloatArray2D input,
        double sigma,
        Calibration calibration
    ) {
        long s0 = System.currentTimeMillis();
        ComputeCurvatures.FloatArray2D output = new ComputeCurvatures.FloatArray2D(input.width, input.height);
        float kernelsumX = 0.0F;
        float kernelsumY = 0.0F;
        float kernelsumZ = 0.0F;
        float pixelWidth = 1.0F;
        float pixelHeight = 1.0F;
        float pixelDepth = 1.0F;
        if (calibration != null) {
            pixelWidth = (float)calibration.pixelWidth;
            pixelHeight = (float)calibration.pixelHeight;
            pixelDepth = (float)calibration.pixelDepth;
        }

        long s1 = System.currentTimeMillis();
        float[] kernelX = createGaussianKernel1D(sigma / pixelWidth, true);
        float[] kernelY = createGaussianKernel1D(sigma / pixelHeight, true);
        int filterSizeX = kernelX.length;
        int filterSizeY = kernelY.length;
        long s2 = System.currentTimeMillis();

        for(int i = 0; i < kernelX.length; ++i)
            kernelsumX += kernelX[i];

        for(int i = 0; i < kernelY.length; ++i)
            kernelsumY += kernelY[i];

        for(int x = 0; x < input.width; ++x) {
            for(int y = 0; y < input.height; ++y) {
                float avg = 0.0F;
                if (x - filterSizeX / 2 >= 0 && x + filterSizeX / 2 < input.width) {
                    for(int f = -filterSizeX / 2; f <= filterSizeX / 2; ++f)
                        avg += input.get(x + f, y) * kernelX[f + filterSizeX / 2];
                }
                else {
                    for(int f = -filterSizeX / 2; f <= filterSizeX / 2; ++f)
                        avg += input.getMirror(x + f, y) * kernelX[f + filterSizeX / 2];
                }

                output.set(avg / kernelsumX, x, y);
            }
        }

        for(int x = 0; x < input.width; ++x) {
            float[] temp = new float[input.height];

            for(int y = 0; y < input.height; ++y) {
                float avg = 0.0F;
                if (y - filterSizeY / 2 >= 0 && y + filterSizeY / 2 < input.height) {
                    for(int f = -filterSizeY / 2; f <= filterSizeY / 2; ++f)
                        avg += output.get(x, y + f) * kernelY[f + filterSizeY / 2];
                }
                else {
                    for(int f = -filterSizeY / 2; f <= filterSizeY / 2; ++f)
                        avg += output.getMirror(x, y + f) * kernelY[f + filterSizeY / 2];
                }

                temp[y] = avg / kernelsumY;
            }

            for(int y = 0; y < input.height; ++y)
                output.set(temp[y], x, y);
        }

        return output;
    }
}
