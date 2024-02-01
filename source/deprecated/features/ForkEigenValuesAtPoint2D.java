package features;

import Utils.Utils;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import math3d.Eigensystem2x2Float;
import math3d.Eigensystem3x3Float;
import math3d.JacobiFloat;

public class ForkEigenValuesAtPoint2D extends RecursiveAction {
   float[] evalues = new float[3];
   protected static ForkEigenValuesAtPoint2D.FloatArray2D data2D;
   protected boolean fixUp = false;
   protected boolean orderOnAbsoluteSize = true;
   protected boolean normalize = false;
   float sepX = 1.0F;
   float sepY = 1.0F;
   int width = 0;
   int height = 0;
   ImagePlus original;
   int threshold;
   public float[] sliceFinal;
   float minResult = Float.MAX_VALUE;
   float maxResult = Float.MIN_VALUE;
   int mLength;
   int mStartA;
   int mStartB;
   protected double sigma;
   protected static int sThreshold = 5;

   public ForkEigenValuesAtPoint2D(
      ImagePlus original, ForkEigenValuesAtPoint2D.FloatArray2D data, int start, int length, double sigma, int threshold, float[] dst
   ) {
      this.original = original;
      this.width = original.getWidth();
      this.height = original.getHeight();
      data2D = data;
      this.mLength = length;
      this.mStartA = this.mStartB = start;
      if (this.mStartA == 0) {
         this.mStartA = 1;
      }

      if (this.mStartB + this.mLength >= this.width - 1) {
         --this.mLength;
      }

      this.sigma = sigma;
      this.threshold = threshold;
      this.sliceFinal = dst;
   }

   ForkEigenValuesAtPoint2D() {
   }

   private void computeDirectly() {
      long count = 0L;
      long total = (long)(this.height * this.width);
      ImageProcessor fp = this.original.getProcessor();

      for(int y = 1; y < this.height - 1; ++y) {
         for(int x = this.mStartA; x < this.mStartB + this.mLength; ++x) {
            if (fp.getPixelValue(x, y) > (float)this.threshold) {
               boolean real = this.hessianEigenvaluesAtPoint2D(x, y, true, this.evalues, this.normalize, false, this.sepX, this.sepY);
               int index = y * this.width + x;
               float value = 0.0F;
               if (real) {
                  value = measureFromEvalues2D(this.evalues, 1);
               }

               this.sliceFinal[index] = value;
            }

            ++count;
         }
      }
   }

   @Override
   protected void compute() {
      if (this.mLength < sThreshold) {
         this.computeDirectly();
      } else {
         int split = this.mLength / 2;
         int remainder = this.mLength % 2;
         int secondHalf = split + remainder;
         invokeAll(
            new ForkEigenValuesAtPoint2D(this.original, data2D, this.mStartB, split, this.sigma, this.threshold, this.sliceFinal),
            new ForkEigenValuesAtPoint2D(this.original, data2D, this.mStartB + split, secondHalf, this.sigma, this.threshold, this.sliceFinal)
         );
      }
   }

   public float[] computeEigenvalues(ImagePlus original, double sigma, int _threshold) {
      //System.out.println("_threshold= " + _threshold + "\ttreshold = " + this.threshold);
      data2D = this.ImageToFloatArray(original.getProcessor());
      int[] histogram2 = Utils.getFloatHistogram2(original);
      this.threshold = Utils.findHistogramMax(histogram2) + 2;
      /*
      if (!Utils.isReleaseVersion) {
         System.out.println("treshold = " + this.threshold);
      }
      */

      if (_threshold <= 0) {
         this.threshold = 3;
      } else {
         this.threshold = _threshold;
      }

      //System.out.println("_threshold= " + _threshold + "\ttreshold = " + this.threshold);
      this.sliceFinal = new float[data2D.data.length];
      ForkEigenValuesAtPoint2D fe = new ForkEigenValuesAtPoint2D(original, data2D, 0, original.getWidth(), sigma, this.threshold, this.sliceFinal);
      ForkJoinPool pool = new ForkJoinPool();
      long startTime = System.currentTimeMillis();
      pool.invoke(fe);
      long endTime = System.currentTimeMillis();
      return this.sliceFinal;
   }

   public boolean hessianEigenvaluesAtPoint2D(
      int x, int y, boolean orderOnAbsoluteSize, float[] result, boolean normalize, boolean fixUp, float sepX, float sepY
   ) {
      if (fixUp) {
         if (x == 0) {
            x = 1;
         }

         if (x == data2D.width - 1) {
            x = data2D.width - 2;
         }

         if (y == 0) {
            y = 1;
         }

         if (y == data2D.height - 1) {
            y = data2D.height - 2;
         }
      }

      float[][] hessianMatrix = this.computeHessianMatrix2DFloat(data2D, x, y, this.sigma, sepX, sepY);
      float[] eigenValues = this.findEigenValuesForMatrix(hessianMatrix);
      if (eigenValues == null) {
         return false;
      } else {
         float e0 = eigenValues[0];
         float e1 = eigenValues[1];
         float e0c = orderOnAbsoluteSize ? Math.abs(e0) : e0;
         float e1c = orderOnAbsoluteSize ? Math.abs(e1) : e1;
         if (e0c <= e1c) {
            result[0] = e0;
            result[1] = e1;
         } else {
            result[0] = e1;
            result[1] = e0;
         }

         if (normalize) {
            float divideBy = Math.abs(result[1]);
            result[0] /= divideBy;
            result[1] /= divideBy;
         }

         return true;
      }
   }

   public static float measureFromEvalues2D(float[] evalues, int vesselness) {
      float measure = 0.0F;
      return evalues[1] >= 0.0F ? 0.0F : Math.abs(evalues[1]);
   }

   public long[] hessianEigenvaluesAtPoint2D2(
      int x, int y, boolean orderOnAbsoluteSize, float[] result, boolean normalize, boolean fixUp, float sepX, float sepY
   ) {
      long start = System.currentTimeMillis();
      if (fixUp) {
         if (x == 0) {
            x = 1;
         }

         if (x == data2D.width - 1) {
            x = data2D.width - 2;
         }

         if (y == 0) {
            y = 1;
         }

         if (y == data2D.height - 1) {
            y = data2D.height - 2;
         }
      }

      long hessianMatrixStart = System.currentTimeMillis();
      float[][] hessianMatrix = this.computeHessianMatrix2DFloat(data2D, x, y, this.sigma, sepX, sepY);
      long hessianMatrixEnd = System.currentTimeMillis();
      float[] eigenValues = this.findEigenValuesForMatrix(hessianMatrix);
      long eigenValuesEnd = System.currentTimeMillis();
      if (eigenValues == null) {
         return null;
      } else {
         float e0 = eigenValues[0];
         float e1 = eigenValues[1];
         float e0c = orderOnAbsoluteSize ? Math.abs(e0) : e0;
         float e1c = orderOnAbsoluteSize ? Math.abs(e1) : e1;
         if (e0c <= e1c) {
            result[0] = e0;
            result[1] = e1;
         } else {
            result[0] = e1;
            result[1] = e0;
         }

         if (normalize) {
            float divideBy = Math.abs(result[1]);
            result[0] /= divideBy;
            result[1] /= divideBy;
         }

         long end1 = System.currentTimeMillis();
         long z1 = hessianMatrixEnd - hessianMatrixStart;
         long z2 = eigenValuesEnd - hessianMatrixEnd;
         long z3 = end1 - eigenValuesEnd;
         long end = System.currentTimeMillis();
         long z4 = end - start;
         return new long[]{z1, z2, z3, z4};
      }
   }

   public float[][] computeHessianMatrix2DFloat(ForkEigenValuesAtPoint2D.FloatArray2D laPlace, int x, int y, double sigma, float sepX, float sepY) {
      float[][] hessianMatrix = new float[2][2];
      float temp = 2.0F * laPlace.get(x, y);
      hessianMatrix[0][0] = laPlace.get(x + 1, y) - temp + laPlace.get(x - 1, y);
      hessianMatrix[1][1] = laPlace.get(x, y + 1) - temp + laPlace.get(x, y - 1);
      hessianMatrix[0][1] = hessianMatrix[1][0] = (
            (laPlace.get(x + 1, y + 1) - laPlace.get(x - 1, y + 1)) / 2.0F - (laPlace.get(x + 1, y - 1) - laPlace.get(x - 1, y - 1)) / 2.0F
         )
         / 2.0F;

      for(int i = 0; i < 2; ++i) {
         for(int j = 0; j < 2; ++j) {
            hessianMatrix[i][j] = (float)((double)hessianMatrix[i][j] * sigma * sigma);
         }
      }

      return hessianMatrix;
   }

   public float[] findEigenValuesForMatrix(float[][] matrix) {
      if (matrix.length == 3 && matrix[0].length == 3) {
         Eigensystem3x3Float e = new Eigensystem3x3Float(matrix);
         boolean result = e.findEvalues();
         return result ? e.getEvaluesCopy() : null;
      } else if (matrix.length == 2 && matrix[0].length == 2) {
         Eigensystem2x2Float e = new Eigensystem2x2Float(matrix);
         boolean result = e.findEvalues();
         return result ? e.getEvaluesCopy() : null;
      } else {
         JacobiFloat jc = new JacobiFloat(matrix, 50);
         return jc.getEigenValues();
      }
   }

   public ForkEigenValuesAtPoint2D.FloatArray2D ImageToFloatArray(ImageProcessor ip) {
      Object pixelArray = ip.getPixels();
      int size = ip.getWidth() * ip.getHeight();
      ForkEigenValuesAtPoint2D.FloatArray2D image = null;

      if (ip instanceof ByteProcessor) {
         image = new ForkEigenValuesAtPoint2D.FloatArray2D(ip.getWidth(), ip.getHeight());
         byte[] pixels = (byte[])pixelArray;

         for(int i = 0; i < size; i++) {
            image.data[i] = (float)(pixels[i] & 0xff);
         }
      } else if (ip instanceof ShortProcessor) {
         image = new ForkEigenValuesAtPoint2D.FloatArray2D(ip.getWidth(), ip.getHeight());
         short[] pixels = (short[])pixelArray;

         for(int i = 0; i < size; i++) {
            image.data[i] = (float)(pixels[i] & 0xffff);
         }
      } else if (ip instanceof FloatProcessor) {
         image = new ForkEigenValuesAtPoint2D.FloatArray2D(ip.getWidth(), ip.getHeight());
         System.arraycopy((float[])pixelArray, 0, image.data, 0, size);
      } else {
         if (!Utils.isReleaseVersion) {
            System.err.println("RGB images not supported");
         }
      }

      return image;
   }

   public static abstract class FloatArray {
      public float[] data = null;

      public abstract ForkEigenValuesAtPoint2D.FloatArray clone();
   }

   public static class FloatArray2D extends ForkEigenValuesAtPoint2D.FloatArray {
      public float[] data = null;
      public int width = 0;
      public int height = 0;

      public FloatArray2D(int width, int height) {
         this.data = new float[width * height];
         this.width = width;
         this.height = height;
      }

      public FloatArray2D(float[] data, int width, int height) {
         this.data = data;
         this.width = width;
         this.height = height;
      }

      public ForkEigenValuesAtPoint2D.FloatArray2D clone() {
         ForkEigenValuesAtPoint2D.FloatArray2D clone = new ForkEigenValuesAtPoint2D.FloatArray2D(this.width, this.height);
         System.arraycopy(this.data, 0, clone.data, 0, this.data.length);
         return clone;
      }

      public int getPos(int x, int y) {
         return x + this.width * y;
      }

      public float get(int x, int y) {
         return this.data[this.getPos(x, y)];
      }

      public float getMirror(int x, int y) {
         if (x >= this.width) {
            x = this.width - (x - this.width + 2);
         }

         if (y >= this.height) {
            y = this.height - (y - this.height + 2);
         }

         if (x < 0) {
            int tmp = 0;

            for(int dir = 1; x < 0; ++x) {
               tmp += dir;
               if (tmp == this.width - 1 || tmp == 0) {
                  dir *= -1;
               }
            }

            x = tmp;
         }

         if (y < 0) {
            int tmp = 0;

            for(int dir = 1; y < 0; ++y) {
               tmp += dir;
               if (tmp == this.height - 1 || tmp == 0) {
                  dir *= -1;
               }
            }

            y = tmp;
         }

         return this.data[this.getPos(x, y)];
      }

      public float getZero(int x, int y) {
         if (x >= this.width) {
            return 0.0F;
         } else if (y >= this.height) {
            return 0.0F;
         } else if (x < 0) {
            return 0.0F;
         } else {
            return y < 0 ? 0.0F : this.data[this.getPos(x, y)];
         }
      }

      public void set(float value, int x, int y) {
         this.data[this.getPos(x, y)] = value;
      }
   }
}
