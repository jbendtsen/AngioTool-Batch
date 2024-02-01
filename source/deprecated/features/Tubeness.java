package features;

import Utils.Utils;
import ij.ImagePlus;

public class Tubeness {
   public ImagePlus runTubeness(ImagePlus original, int threshold, double[] _sigma, boolean showResult) {
      if (original == null) {
         if (!Utils.isReleaseVersion) {
            System.err.println("No current image to calculate tubeness of.");
         }

         return null;
      } else {
         TubenessProcessor tp = new TubenessProcessor(threshold, _sigma);
         ImagePlus result = tp.generateImage(original);
         result.setTitle("Tubeness of " + original.getTitle());
         if (showResult) {
            result.show();
         }

         return result;
      }
   }
}
