package features;

public class TubenessProcessor extends HessianEvalueProcessor {
   public TubenessProcessor(int threshold, double[] sigma) {
      this.sigma = sigma;
      this.useCalibration = false;
   }

   @Override
   public float measureFromEvalues2D(float[] evalues, int vesselness) {
      float measure = 0.0F;
      return evalues[1] >= 0.0F ? 0.0F : Math.abs(evalues[1]);
   }

   @Override
   public float measureFromEvalues3D(float[] evalues) {
      return !(evalues[1] >= 0.0F) && !(evalues[2] >= 0.0F) ? (float)Math.sqrt((double)(evalues[2] * evalues[1])) : 0.0F;
   }
}
