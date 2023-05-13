package vesselThickness;

import ij.IJ;
import ij.ImagePlus;

public class TestThickness {
   public static void main(String[] args) {
      ImagePlus imp = IJ.openImage("c:\\a.tif");
      imp.show();
      EDT_S1D ed = new EDT_S1D();
      ed.setup(null, imp);
      ed.run(imp.getProcessor());
      IJ.saveAs(ed.getImageResult(), "tif", "c:\\a2.tif");
   }
}
