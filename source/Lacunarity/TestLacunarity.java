package Lacunarity;

import ij.IJ;
import ij.ImagePlus;

public class TestLacunarity {
   public static void main(String[] args) {
      ImagePlus iplus = IJ.openImage("C:/Documents and Settings/zudairee/My Documents/KO3b.tif lacunarity.tif");
      Lacunarity l = new Lacunarity(iplus, 10, 10, 5, true);

      for(int i = 0; i < l.getBoxes().length; ++i) {
         System.out.println(l.getBoxes()[i] + "\t" + l.getEl3().get(i) + "\t" + l.getFl3().get(i));
      }

      System.out.println(l.getEl3Slope() + "\t" + l.getFl3Slope());
   }
}
