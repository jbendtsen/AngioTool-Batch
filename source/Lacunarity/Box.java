package Lacunarity;

public class Box {
   int xStart;
   int yStart;
   int xBorder;
   int yBorder;
   int firstRawPixelCount;
   int firstColumnPixelCount;
   int pixelCount = Integer.MIN_VALUE;

   @Override
   public String toString() {
      return new String(
         "xStart= "
            + this.xStart
            + " yStart= "
            + this.yStart
            + " pixelCount= "
            + this.pixelCount
            + " firstRawPixelCount= "
            + this.firstRawPixelCount
            + " firstColumnPixelCount "
            + this.firstColumnPixelCount
      );
   }
}
