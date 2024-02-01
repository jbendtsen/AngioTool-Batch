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

        for(int s = 0; s < this.sigma.length; ++s) {
            ImageProcessor ipp = original.getProcessor().duplicate();
            ComputeCurvatures c = new ComputeCurvatures();
            c.setData(new ImagePlus("", new FloatProcessor(width, height)));
            c.setSigma(this.sigma[s]);
            GaussianGenerationCallback callback = null;
            ComputeCurvatures.FloatArray2D fa2dInput = c.ImageToFloatArray(ipp);
            ComputeCurvatures.FloatArray2D fa2d = c.computeGaussianFastMirror(fa2dInput, (float)this.sigma[s], callback, calibration);
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
}
