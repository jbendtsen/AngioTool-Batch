package vesselThickness;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkStep2 extends RecursiveTask<ArrayList<float[]>> {
   private int mLength;
   private int mStart;
   private int sThreshold = 250;
   private int depth = 1;
   private int width;
   private int height;
   private float[] outputImage_;
   private float[] src_;

   public ForkStep2(float[] src_, int width, int height, int start, int length) {
      this.src_ = src_;
      this.outputImage_ = src_;
      this.width = width;
      this.height = height;
      this.mStart = start;
      this.mLength = length;
   }

   ForkStep2() {
   }

   public ArrayList<float[]> computeDirectly() {
      int w = this.width;
      int h = this.height;
      int d = this.depth;
      float[] sk = null;
      int n = w;
      if (h > w) {
         n = h;
      }

      if (d > n) {
         n = d;
      }

      int[] tempS = new int[n];
      int[] tempInt = new int[n];
      int noResult = 3 * (n + 1) * (n + 1);

      for(int z = 0; z < this.depth; ++z) {
         sk = this.src_;

         for(int x = this.mStart; x < this.mStart + this.mLength; ++x) {
            boolean nonempty = false;

            for(int y = 0; y < this.height; ++y) {
               tempS[y] = (int)sk[x + w * y];
               if (tempS[y] > 0) {
                  nonempty = true;
               }
            }

            if (nonempty) {
               for(int y = 0; y < h; ++y) {
                  int min = noResult;
                  int delta = y;

                  for(int y_ = 0; y_ < h; ++y_) {
                     int test = tempS[y_] + delta * delta--;
                     if (test < min) {
                        min = test;
                     }
                  }

                  tempInt[y] = min;
               }

               for(int y = 0; y < h; ++y) {
                  sk[x + w * y] = (float)tempInt[y];
               }
            }
         }
      }

      ArrayList<float[]> f = new ArrayList<>();
      f.add(sk);
      return f;
   }

   protected ArrayList<float[]> compute() {
      if (this.mLength < this.sThreshold) {
         return this.computeDirectly();
      } else {
         int split = this.mLength / 2;
         int remainder = this.mLength % 2;
         int secondHalf = split + remainder;
         ForkStep2 left = new ForkStep2(this.src_, this.width, this.height, this.mStart, split);
         ForkStep2 right = new ForkStep2(this.src_, this.width, this.height, this.mStart + split, secondHalf);
         right.fork();
         ArrayList<float[]> a = left.compute();
         a.addAll(right.join());
         return a;
      }
   }

   public ArrayList<float[]> thin(float[] src, int width, int height) {
      new ArrayList();
      this.outputImage_ = src;
      ForkStep2 fe = new ForkStep2(this.outputImage_, width, height, 0, width);
      ForkJoinPool pool = new ForkJoinPool();
      return pool.invoke(fe);
   }
}
