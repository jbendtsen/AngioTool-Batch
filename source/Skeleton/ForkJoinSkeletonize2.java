package Skeleton;

import ij.ImageStack;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinSkeletonize2 extends RecursiveTask<ArrayList<int[]>> {
   private int mLength;
   private int mStart;
   private int sThreshold = 250;
   private int depth = 1;
   private int width;
   private int height;
   private ImageStack outputImage;
   private ImageStack src;
   private int[] eulerLUT = new int[256];
   private int currentBorder;
   private ArrayList<int[]> mSimpleBorderPoints;

   public ForkJoinSkeletonize2(ImageStack src, int start, int length, int currentBorder, int[] eulerLUT) {
      this.src = src;
      this.outputImage = src;
      this.width = src.getWidth();
      this.height = src.getHeight();
      this.mStart = start;
      this.mLength = length;
      this.currentBorder = currentBorder;
      this.mSimpleBorderPoints = new ArrayList<>();
      this.eulerLUT = eulerLUT;
   }

   ForkJoinSkeletonize2() {
   }

   public ArrayList<int[]> computeDirectly() {
      for(int z = 0; z < this.depth; ++z) {
         for(int y = 0; y < this.height; ++y) {
            for(int x = this.mStart; x < this.mStart + this.mLength; ++x) {
               if (SUM.getPixel(this.outputImage, x, y, z) == 1) {
                  boolean isBorderPoint = false;
                  if (this.currentBorder == 1 && SUM.N(this.outputImage, x, y, z) <= 0) {
                     isBorderPoint = true;
                  }

                  if (this.currentBorder == 2 && SUM.S(this.outputImage, x, y, z) <= 0) {
                     isBorderPoint = true;
                  }

                  if (this.currentBorder == 3 && SUM.E(this.outputImage, x, y, z) <= 0) {
                     isBorderPoint = true;
                  }

                  if (this.currentBorder == 4 && SUM.W(this.outputImage, x, y, z) <= 0) {
                     isBorderPoint = true;
                  }

                  if (this.currentBorder == 5 && SUM.U(this.outputImage, x, y, z) <= 0) {
                     isBorderPoint = true;
                  }

                  if (this.currentBorder == 6 && SUM.B(this.outputImage, x, y, z) <= 0) {
                     isBorderPoint = true;
                  }

                  if (isBorderPoint) {
                     int numberOfNeighbors = -1;
                     byte[] neighbor = SUM.getNeighborhood(this.outputImage, x, y, z);

                     for(int i = 0; i < 27; ++i) {
                        if (neighbor[i] == 1) {
                           ++numberOfNeighbors;
                        }
                     }

                     if (numberOfNeighbors != 1 && SUM.isEulerInvariant(neighbor, this.eulerLUT) && SUM.isSimplePoint(neighbor)) {
                        int[] index = new int[]{x, y, z};
                        this.mSimpleBorderPoints.add(index);
                     }
                  }
               }
            }
         }
      }

      return this.mSimpleBorderPoints;
   }

   protected ArrayList<int[]> compute() {
      if (this.mLength < this.sThreshold) {
         return this.computeDirectly();
      } else {
         int split = this.mLength / 2;
         int remainder = this.mLength % 2;
         int secondHalf = split + remainder;
         ForkJoinSkeletonize2 left = new ForkJoinSkeletonize2(this.src, this.mStart, split, this.currentBorder, this.eulerLUT);
         ForkJoinSkeletonize2 right = new ForkJoinSkeletonize2(this.src, this.mStart + split, secondHalf, this.currentBorder, this.eulerLUT);
         right.fork();
         ArrayList<int[]> a = left.compute();
         a.addAll(right.join());
         return a;
      }
   }

   public ArrayList<int[]> thin(ImageStack src, int currentBorder, int[] eulerLUT) {
      new ArrayList();
      this.outputImage = src;
      ForkJoinSkeletonize2 fe = new ForkJoinSkeletonize2(this.outputImage, 0, this.outputImage.getWidth(), currentBorder, eulerLUT);
      ForkJoinPool pool = new ForkJoinPool();
      return pool.invoke(fe);
   }
}
