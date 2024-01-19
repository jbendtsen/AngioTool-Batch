package Skeleton;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import java.util.ArrayList;

public class Skeletonize3D {
   private ImagePlus imRef;
   private int width = 0;
   private int height = 0;
   private int depth = 0;
   private ImageStack inputImage = null;

   public void setup(String arg, ImagePlus imp) {
      this.imRef = imp;
      SUM.setImage(this.imRef);
   }

   public ImageProcessor run(ImageProcessor ip) {
      this.width = this.imRef.getWidth();
      this.height = this.imRef.getHeight();
      this.depth = this.imRef.getStackSize();
      this.inputImage = this.imRef.getStack();
      this.prepareData(this.inputImage);
      this.computeThinImage(this.inputImage);

      for(int i = 1; i <= this.inputImage.getSize(); ++i) {
         this.inputImage.getProcessor(i).multiply(255.0);
      }

      this.inputImage.update(ip);
      return this.inputImage.getProcessor(1);
   }

   private void prepareData(ImageStack outputImage) {
      for(int z = 0; z < this.depth; ++z) {
         for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
               if (((byte[])((byte[])this.inputImage.getPixels(z + 1)))[x + y * this.width] != 0) {
                  ((byte[])outputImage.getPixels(z + 1))[x + y * this.width] = 1;
               }
            }
         }
      }
   }

   public void computeThinImage(ImageStack outputImage) {
      new ImagePlus();
      new ArrayList();
      int[] eulerLUT = new int[256];
      SUM.fillEulerLUT(eulerLUT);
      int iter = 1;

      for(int unchangedBorders = 0; unchangedBorders < 6; ++iter) {
         unchangedBorders = 0;

         for(int currentBorder = 1; currentBorder <= 6; ++currentBorder) {
            ForkJoinSkeletonize2 fs2 = new ForkJoinSkeletonize2();
            ArrayList<int[]> simpleBorderPoints = fs2.thin(outputImage, currentBorder, eulerLUT);
            boolean noChange = true;
            int[] index = null;

            for(int i = 0; i < simpleBorderPoints.size(); ++i) {
               index = (int[])simpleBorderPoints.get(i);
               SUM.setPixel(outputImage, index[0], index[1], index[2], (byte)0);
               if (!SUM.isSimplePoint(SUM.getNeighborhood(outputImage, index[0], index[1], index[2]))) {
                  SUM.setPixel(outputImage, index[0], index[1], index[2], (byte)1);
               } else {
                  noChange = false;
               }
            }

            if (noChange) {
               ++unchangedBorders;
            }

            simpleBorderPoints.clear();
         }
      }
   }
}
