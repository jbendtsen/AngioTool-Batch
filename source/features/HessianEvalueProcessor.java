package features;

import AngioTool.AngioToolMain;
import Batch.ComputeEigenValuesAtPoint2D;
import Utils.Utils;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public abstract class HessianEvalueProcessor implements GaussianGenerationCallback {
   protected boolean normalize = false;
   protected double[] sigma;
   protected boolean useCalibration = false;
   private int threshold = -1;

   @Override
   public void proportionDone(double d) {
   }

   public abstract float measureFromEvalues2D(float[] var1, int var2);

   public abstract float measureFromEvalues3D(float[] var1);

   public void setSigma(double[] newSigma) {
      this.sigma = newSigma;
   }

   public ImagePlus generateImage(ImagePlus original) {
      Calibration calibration = original.getCalibration();
      float sepX = 1.0F;
      float sepY = 1.0F;
      float sepZ = 1.0F;
      if (this.useCalibration && calibration != null) {
         sepX = (float)calibration.pixelWidth;
         sepY = (float)calibration.pixelHeight;
         sepZ = (float)calibration.pixelDepth;
      }

      double minimumSeparation = (double)Math.min(sepX, Math.min(sepY, sepX));
      int width = original.getWidth();
      int height = original.getHeight();
      int depth = original.getStackSize();
      ImageStack stack = new ImageStack(width, height);
      float[] evalues = new float[3];
      long start = System.currentTimeMillis();
      float minResult = Float.MAX_VALUE;
      float maxResult = Float.MIN_VALUE;
      if (depth == 1) {
         float[] slice = new float[width * height];

         for(int i = 0; i < slice.length; ++i) {
            slice[i] = 0.0F;
         }

         for(int s = 0; s < this.sigma.length; ++s) {
            ImageProcessor ipp = original.getProcessor().duplicate();
            ComputeCurvatures c = new ComputeCurvatures();
            c.setData(new ImagePlus("", new FloatProcessor(width, height)));
            c.setSigma(this.sigma[s]);
            GaussianGenerationCallback callback = null;
            ComputeCurvatures.FloatArray2D fa2dInput = c.ImageToFloatArray(ipp);
            ComputeCurvatures.FloatArray2D fa2d = c.computeGaussianFastMirror(fa2dInput, (float)this.sigma[s], callback, calibration);
            ImagePlus fa2dIP = ComputeCurvatures.FloatArrayToImagePlus(fa2d, "fa2dIP", 0.0F, 255.0F);
            if (!Utils.isReleaseVersion) {
               System.out.println("We are running the new ForkEigenValuesAtPoint2D2");
               System.out.println("threshold_0= " + this.threshold);
            }

            /*
                OLD AND SLOW
                //ForkEigenValuesAtPoint2D fe = new ForkEigenValuesAtPoint2D();
                //float[] slice2 = fe.computeEigenvalues(fa2dIP, this.sigma[s], this.threshold);
            */
            float[] slice2 = ComputeEigenValuesAtPoint2D.computeEigenvalues(AngioToolMain.threadPool, AngioToolMain.MAX_WORKERS, fa2dIP, this.sigma[s], this.threshold);

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

            long endHE = System.currentTimeMillis();
            this.proportionDone((double)Math.round((float)((s + 1) * 100 / this.sigma.length)));
         }

         long end = System.currentTimeMillis();
         FloatProcessor fp = new FloatProcessor(width, height);
         fp.setPixels(slice);
         stack.addSlice(null, fp);
      }

      this.proportionDone(100.0);
      ImagePlus result = new ImagePlus("processed " + original.getTitle(), stack);
      result.setCalibration(calibration);
      result.getProcessor().setMinAndMax((double)minResult, (double)maxResult);
      result.updateAndDraw();
      return result;
   }
}
