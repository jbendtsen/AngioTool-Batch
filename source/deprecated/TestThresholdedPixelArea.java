package Utils;

import ij.IJ;
import ij.ImagePlus;

public class TestThresholdedPixelArea {
   public static void main(String[] args) {
      ImagePlus iplus = IJ.openImage("C:/Kike Zudaire/Laure Gambardella/Nature Protocols/wt-ko-ki/KO3.tif lacunarity.tif");
      iplus.show();
      long pixels = Utils.thresholdedPixelArea(iplus.getProcessor());
      System.out.println("pixels= " + pixels);
   }
}
