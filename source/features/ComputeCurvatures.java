package features;

import Utils.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import math3d.Eigensystem2x2Double;
import math3d.Eigensystem2x2Float;
import math3d.Eigensystem3x3Double;
import math3d.Eigensystem3x3Float;
import math3d.JacobiDouble;
import math3d.JacobiFloat;

public class ComputeCurvatures implements Runnable {
   private boolean _3D;
   private ComputeCurvatures.FloatArray data;
   private double[][] hessianMatrix;
   private float[] eigenValues = new float[3];
   private ComputeCurvatures.FloatArray3D[] result3D;
   private ComputeCurvatures.FloatArray3D result2D;
   private double min = Double.MAX_VALUE;
   private double max = Double.MIN_VALUE;
   protected ImagePlus imp;
   protected double sigma;
   protected boolean useCalibration;
   protected GaussianGenerationCallback callback;
   private boolean cancelGeneration = false;

   public ComputeCurvatures() {
   }

   public ComputeCurvatures(ImagePlus imp, double sigma, GaussianGenerationCallback callback, boolean useCalibration) {
      this.imp = imp;
      this.sigma = sigma;
      this.callback = callback;
      this.useCalibration = useCalibration;
   }

   public void cancelGaussianGeneration() {
      this.cancelGeneration = true;
   }

   @Override
   public void run() {
      if (this.imp == null) {
         IJ.error("BUG: imp should not be null - are you using the right constructor?");
      } else {
         this.setup();
      }
   }

   public void setup() {
      try {
         if (this.imp == null) {
            IJ.error("BUG: imp should not be null - are you using the right constructor?");
         } else {
            if (this.callback != null) {
               this.callback.proportionDone(0.0);
            }

            if (this.imp.getStackSize() > 1) {
               ImageStack stack = this.imp.getStack();
               this._3D = true;
               this.data = this.StackToFloatArray(stack);
               if (this.data == null) {
                  return;
               }
            } else {
               this._3D = false;
               this.data = this.ImageToFloatArray(this.imp.getProcessor());
               if (this.data == null) {
                  return;
               }
            }

            boolean computeGauss = true;
            boolean showGauss = true;
            Calibration calibration = this.imp.getCalibration();
            if (!Utils.isReleaseVersion) {
               System.out.println("Computing Gauss image");
            }

            if (this._3D) {
               this.data = this.computeGaussianFastMirror(
                  (ComputeCurvatures.FloatArray3D)this.data, (float)this.sigma, this.callback, this.useCalibration ? calibration : null
               );
               if (this.data == null) {
                  if (this.callback != null) {
                     this.callback.proportionDone(-1.0);
                  }

                  return;
               }

               if (showGauss) {
                  this.FloatArrayToStack((ComputeCurvatures.FloatArray3D)this.data, "Gauss image", 0.0F, 255.0F).show();
               }
            } else {
               this.data = this.computeGaussianFastMirror(
                  (ComputeCurvatures.FloatArray2D)this.data, (float)this.sigma, this.callback, this.useCalibration ? calibration : null
               );
               if (this.data == null) {
                  if (this.callback != null) {
                     this.callback.proportionDone(-1.0);
                  }

                  return;
               }

               if (showGauss) {
                  FloatArrayToImagePlus((ComputeCurvatures.FloatArray2D)this.data, "Gauss image", 0.0F, 255.0F).show();
               }
            }

            if (this.callback != null) {
               this.callback.proportionDone(1.0);
            }
         }
      } catch (OutOfMemoryError var4) {
         long requiredMiB = (long)(this.imp.getWidth() * this.imp.getHeight() * this.imp.getStackSize() * 4 / 1048576);
         IJ.error("Out of memory when calculating the Gaussian convolution of the image (requires " + requiredMiB + "MiB");
         if (this.callback != null) {
            this.callback.proportionDone(-1.0);
         }
      }
   }

   public void setData(ImagePlus imp) {
      this.data = this.ImageToFloatArray(imp.getProcessor());
      if (this.data == null && !Utils.isReleaseVersion) {
         System.err.println("Data = null!!!!");
      }
   }

   public void setSigma(double sigma) {
      this.sigma = sigma;
   }

   public ComputeCurvatures.FloatArray getData() {
      return this.data;
   }

   public boolean hessianEigenvaluesAtPoint2D(
      int x, int y, boolean orderOnAbsoluteSize, float[] result, boolean normalize, boolean fixUp, float sepX, float sepY
   ) {
      long start = System.currentTimeMillis();
      if (this._3D) {
         if (!Utils.isReleaseVersion) {
            System.err.println("hessianEigenvaluesAtPoint2D( x, y, z, ... ) is only for 2D data.");
         }

         return false;
      } else {
         ComputeCurvatures.FloatArray2D data2D = (ComputeCurvatures.FloatArray2D)this.data;
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
         float[] eigenValues = this.computeEigenValues(hessianMatrix);
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
   }

   public long[] hessianEigenvaluesAtPoint2D2(
      int x, int y, boolean orderOnAbsoluteSize, float[] result, boolean normalize, boolean fixUp, float sepX, float sepY
   ) {
      long start = System.currentTimeMillis();
      if (this._3D) {
         if (!Utils.isReleaseVersion) {
            System.err.println("hessianEigenvaluesAtPoint2D( x, y, z, ... ) is only for 2D data.");
         }

         return null;
      } else {
         ComputeCurvatures.FloatArray2D data2D = (ComputeCurvatures.FloatArray2D)this.data;
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
         float[] eigenValues = this.computeEigenValues(hessianMatrix);
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
   }

   public float[] doubleToFloat(double[] a) {
      float[] a2 = new float[a.length];

      for(int r = 0; r < a.length; ++r) {
         a2[r] = (float)a[r];
      }

      return a2;
   }

   public double[][] floatToDouble(float[][] a) {
      double[][] a2 = new double[a.length][a[0].length];

      for(int r = 0; r < a.length; ++r) {
         for(int c = 0; c < a[0].length; ++c) {
            a2[r][c] = (double)a[r][c];
         }
      }

      return a2;
   }

   public boolean hessianEigenvaluesAtPoint2D(
      int x, int y, boolean orderOnAbsoluteSize, double[] result, boolean normalize, boolean fixUp, float sepX, float sepY
   ) {
      if (this._3D) {
         IJ.error("hessianEigenvaluesAtPoint2D( x, y, z, ... ) is only for 2D data.");
         return false;
      } else {
         ComputeCurvatures.FloatArray2D data2D = (ComputeCurvatures.FloatArray2D)this.data;
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

         double[][] hessianMatrix = this.computeHessianMatrix2DDouble(data2D, x, y, this.sigma, sepX, sepY);
         double[] eigenValues = this.computeEigenValues(hessianMatrix);
         if (eigenValues == null) {
            return false;
         } else {
            double e0 = eigenValues[0];
            double e1 = eigenValues[1];
            double e0c = orderOnAbsoluteSize ? Math.abs(e0) : e0;
            double e1c = orderOnAbsoluteSize ? Math.abs(e1) : e1;
            if (e0c <= e1c) {
               result[0] = e0;
               result[1] = e1;
            } else {
               result[0] = e1;
               result[1] = e0;
            }

            if (normalize) {
               double divideBy = Math.abs(result[1]);
               result[0] /= divideBy;
               result[1] /= divideBy;
            }

            return true;
         }
      }
   }

   public boolean hessianEigenvaluesAtPoint3D(
      int x, int y, int z, boolean orderOnAbsoluteSize, float[] result, boolean normalize, boolean fixUp, float sepX, float sepY, float sepZ
   ) {
      if (!this._3D) {
         IJ.error("hessianEigenvaluesAtPoint3D( x, y, z, ... ) is only for 3D data.");
         return false;
      } else {
         ComputeCurvatures.FloatArray3D data3D = (ComputeCurvatures.FloatArray3D)this.data;
         if (fixUp) {
            if (x == 0) {
               x = 1;
            }

            if (x == data3D.width - 1) {
               x = data3D.width - 2;
            }

            if (y == 0) {
               y = 1;
            }

            if (y == data3D.height - 1) {
               y = data3D.height - 2;
            }

            if (z == 0) {
               z = 1;
            }

            if (z == data3D.depth - 1) {
               z = data3D.depth - 2;
            }
         }

         float[][] hessianMatrix = this.computeHessianMatrix3DFloat(data3D, x, y, z, this.sigma, sepX, sepY, sepZ);
         float[] eigenValues = this.computeEigenValues(hessianMatrix);
         if (eigenValues == null) {
            return false;
         } else {
            float e0 = eigenValues[0];
            float e1 = eigenValues[1];
            float e2 = eigenValues[2];
            float e0c = orderOnAbsoluteSize ? Math.abs(e0) : e0;
            float e1c = orderOnAbsoluteSize ? Math.abs(e1) : e1;
            float e2c = orderOnAbsoluteSize ? Math.abs(e2) : e2;
            if (e0c <= e1c) {
               if (e1c <= e2c) {
                  result[0] = e0;
                  result[1] = e1;
                  result[2] = e2;
               } else if (e0c <= e2c) {
                  result[0] = e0;
                  result[1] = e2;
                  result[2] = e1;
               } else {
                  result[0] = e2;
                  result[1] = e0;
                  result[2] = e1;
               }
            } else if (e0c <= e2c) {
               result[0] = e1;
               result[1] = e0;
               result[2] = e2;
            } else if (e1c <= e2c) {
               result[0] = e1;
               result[1] = e2;
               result[2] = e0;
            } else {
               result[0] = e2;
               result[1] = e1;
               result[2] = e0;
            }

            if (normalize) {
               float divideBy = Math.abs(result[2]);
               result[0] /= divideBy;
               result[1] /= divideBy;
               result[2] /= divideBy;
            }

            return true;
         }
      }
   }

   public boolean hessianEigenvaluesAtPoint3D(
      int x, int y, int z, boolean orderOnAbsoluteSize, double[] result, boolean normalize, boolean fixUp, float sepX, float sepY, float sepZ
   ) {
      if (!this._3D) {
         IJ.error("hessianEigenvaluesAtPoint3D( x, y, z, ... ) is only for 3D data.");
         return false;
      } else {
         ComputeCurvatures.FloatArray3D data3D = (ComputeCurvatures.FloatArray3D)this.data;
         if (fixUp) {
            if (x == 0) {
               x = 1;
            }

            if (x == data3D.width - 1) {
               x = data3D.width - 2;
            }

            if (y == 0) {
               y = 1;
            }

            if (y == data3D.height - 1) {
               y = data3D.height - 2;
            }

            if (z == 0) {
               z = 1;
            }

            if (z == data3D.depth - 1) {
               z = data3D.depth - 2;
            }
         }

         double[][] hessianMatrix = this.computeHessianMatrix3DDouble(data3D, x, y, z, this.sigma, sepX, sepY, sepZ);
         double[] eigenValues = this.computeEigenValues(hessianMatrix);
         if (eigenValues == null) {
            return false;
         } else {
            double e0 = eigenValues[0];
            double e1 = eigenValues[1];
            double e2 = eigenValues[2];
            double e0c = orderOnAbsoluteSize ? Math.abs(e0) : e0;
            double e1c = orderOnAbsoluteSize ? Math.abs(e1) : e1;
            double e2c = orderOnAbsoluteSize ? Math.abs(e2) : e2;
            if (e0c <= e1c) {
               if (e1c <= e2c) {
                  result[0] = e0;
                  result[1] = e1;
                  result[2] = e2;
               } else if (e0c <= e2c) {
                  result[0] = e0;
                  result[1] = e2;
                  result[2] = e1;
               } else {
                  result[0] = e2;
                  result[1] = e0;
                  result[2] = e1;
               }
            } else if (e0c <= e2c) {
               result[0] = e1;
               result[1] = e0;
               result[2] = e2;
            } else if (e1c <= e2c) {
               result[0] = e1;
               result[1] = e2;
               result[2] = e0;
            } else {
               result[0] = e2;
               result[1] = e1;
               result[2] = e0;
            }

            if (normalize) {
               double divideBy = Math.abs(result[2]);
               result[0] /= divideBy;
               result[1] /= divideBy;
               result[2] /= divideBy;
            }

            return true;
         }
      }
   }

   public static ImagePlus FloatArrayToImagePlus(ComputeCurvatures.FloatArray2D image, String name, float min, float max) {
      ImagePlus imp = IJ.createImage(name, "32-Bit Black", image.width, image.height, 1);
      FloatProcessor ip = (FloatProcessor)imp.getProcessor();
      FloatArrayToFloatProcessor(ip, image);
      if (min == max) {
         ip.resetMinAndMax();
      } else {
         ip.setMinAndMax((double)min, (double)max);
      }

      imp.updateAndDraw();
      return imp;
   }

   public static void FloatArrayToFloatProcessor(ImageProcessor ip, ComputeCurvatures.FloatArray2D pixels) {
      float[] data = new float[pixels.width * pixels.height];
      int count = 0;

      for(int y = 0; y < pixels.height; ++y) {
         for(int x = 0; x < pixels.width; ++x) {
            data[count] = pixels.data[count++];
         }
      }

      ip.setPixels(data);
      ip.resetMinAndMax();
   }

   public ImagePlus FloatArrayToStack(ComputeCurvatures.FloatArray3D image, String name, float min, float max) {
      int width = image.width;
      int height = image.height;
      int nstacks = image.depth;
      ImageStack stack = new ImageStack(width, height);

      for(int slice = 0; slice < nstacks; ++slice) {
         ImagePlus impResult = IJ.createImage(name, "32-Bit Black", width, height, 1);
         ImageProcessor ipResult = impResult.getProcessor();
         float[] sliceImg = new float[width * height];

         for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
               sliceImg[y * width + x] = image.get(x, y, slice);
            }
         }

         ipResult.setPixels(sliceImg);
         if (min == max) {
            ipResult.resetMinAndMax();
         } else {
            ipResult.setMinAndMax((double)min, (double)max);
         }

         stack.addSlice("Slice " + slice, ipResult);
      }

      return new ImagePlus(name, stack);
   }

   public double[] computeEigenValues(double[][] matrix) {
      if (matrix.length == 3 && matrix[0].length == 3) {
         Eigensystem3x3Double e = new Eigensystem3x3Double(matrix);
         boolean result = e.findEvalues();
         return result ? e.getEvaluesCopy() : null;
      } else if (matrix.length == 2 && matrix[0].length == 2) {
         Eigensystem2x2Double e = new Eigensystem2x2Double(matrix);
         boolean result = e.findEvalues();
         return result ? e.getEvaluesCopy() : null;
      } else {
         JacobiDouble jc = new JacobiDouble(matrix, 50);
         return jc.getEigenValues();
      }
   }

   public float[] computeEigenValues(float[][] matrix) {
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

   public double[][] computeHessianMatrix2DDouble(ComputeCurvatures.FloatArray2D laPlace, int x, int y, double sigma, float sepX, float sepY) {
      if (laPlace == null) {
         laPlace = (ComputeCurvatures.FloatArray2D)this.data;
      }

      double[][] hessianMatrix = new double[2][2];
      double temp = (double)(2.0F * laPlace.get(x, y));
      hessianMatrix[0][0] = (double)laPlace.get(x + 1, y) - temp + (double)laPlace.get(x - 1, y);
      hessianMatrix[1][1] = (double)laPlace.get(x, y + 1) - temp + (double)laPlace.get(x, y - 1);
      hessianMatrix[0][1] = hessianMatrix[1][0] = (double)(
         ((laPlace.get(x + 1, y + 1) - laPlace.get(x - 1, y + 1)) / 2.0F - (laPlace.get(x + 1, y - 1) - laPlace.get(x - 1, y - 1)) / 2.0F) / 2.0F
      );

      for(int i = 0; i < 2; ++i) {
         for(int j = 0; j < 2; ++j) {
            hessianMatrix[i][j] *= sigma * sigma;
         }
      }

      return hessianMatrix;
   }

   public float[][] computeHessianMatrix2DFloat(ComputeCurvatures.FloatArray2D laPlace, int x, int y, double sigma, float sepX, float sepY) {
      if (laPlace == null) {
         laPlace = (ComputeCurvatures.FloatArray2D)this.data;
      }

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

   public double[][] computeHessianMatrix3DDouble(ComputeCurvatures.FloatArray3D img, int x, int y, int z, double sigma, float sepX, float sepY, float sepZ) {
      if (img == null) {
         img = (ComputeCurvatures.FloatArray3D)this.data;
      }

      double[][] hessianMatrix = new double[3][3];
      double temp = (double)(2.0F * img.get(x, y, z));
      hessianMatrix[0][0] = (double)img.get(x + 1, y, z) - temp + (double)img.get(x - 1, y, z);
      hessianMatrix[1][1] = (double)img.get(x, y + 1, z) - temp + (double)img.get(x, y - 1, z);
      hessianMatrix[2][2] = (double)img.get(x, y, z + 1) - temp + (double)img.get(x, y, z - 1);
      hessianMatrix[0][1] = hessianMatrix[1][0] = (double)(
         ((img.get(x + 1, y + 1, z) - img.get(x - 1, y + 1, z)) / 2.0F - (img.get(x + 1, y - 1, z) - img.get(x - 1, y - 1, z)) / 2.0F) / 2.0F
      );
      hessianMatrix[0][2] = hessianMatrix[2][0] = (double)(
         ((img.get(x + 1, y, z + 1) - img.get(x - 1, y, z + 1)) / 2.0F - (img.get(x + 1, y, z - 1) - img.get(x - 1, y, z - 1)) / 2.0F) / 2.0F
      );
      hessianMatrix[1][2] = hessianMatrix[2][1] = (double)(
         ((img.get(x, y + 1, z + 1) - img.get(x, y - 1, z + 1)) / 2.0F - (img.get(x, y + 1, z - 1) - img.get(x, y - 1, z - 1)) / 2.0F) / 2.0F
      );

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            hessianMatrix[i][j] *= sigma * sigma;
         }
      }

      return hessianMatrix;
   }

   public float[][] computeHessianMatrix3DFloat(ComputeCurvatures.FloatArray3D img, int x, int y, int z, double sigma, float sepX, float sepY, float sepZ) {
      if (img == null) {
         img = (ComputeCurvatures.FloatArray3D)this.data;
      }

      float[][] hessianMatrix = new float[3][3];
      float temp = 2.0F * img.get(x, y, z);
      hessianMatrix[0][0] = img.get(x + 1, y, z) - temp + img.get(x - 1, y, z);
      hessianMatrix[1][1] = img.get(x, y + 1, z) - temp + img.get(x, y - 1, z);
      hessianMatrix[2][2] = img.get(x, y, z + 1) - temp + img.get(x, y, z - 1);
      hessianMatrix[0][1] = hessianMatrix[1][0] = (
            (img.get(x + 1, y + 1, z) - img.get(x - 1, y + 1, z)) / 2.0F - (img.get(x + 1, y - 1, z) - img.get(x - 1, y - 1, z)) / 2.0F
         )
         / 2.0F;
      hessianMatrix[0][2] = hessianMatrix[2][0] = (
            (img.get(x + 1, y, z + 1) - img.get(x - 1, y, z + 1)) / 2.0F - (img.get(x + 1, y, z - 1) - img.get(x - 1, y, z - 1)) / 2.0F
         )
         / 2.0F;
      hessianMatrix[1][2] = hessianMatrix[2][1] = (
            (img.get(x, y + 1, z + 1) - img.get(x, y - 1, z + 1)) / 2.0F - (img.get(x, y + 1, z - 1) - img.get(x, y - 1, z - 1)) / 2.0F
         )
         / 2.0F;

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            hessianMatrix[i][j] = (float)((double)hessianMatrix[i][j] * sigma * sigma);
         }
      }

      return hessianMatrix;
   }

   public static float[] createGaussianKernel1D(float sigma, boolean normalize) {
      float[] gaussianKernel;
      if (sigma <= 0.0F) {
         gaussianKernel = new float[]{0.0F, 1.0F, 0.0F};
      } else {
         int size = Math.max(3, 2 * (int)((double)(3.0F * sigma) + 0.5) + 1);
         float two_sq_sigma = 2.0F * sigma * sigma;
         gaussianKernel = new float[size];

         for(int x = size / 2; x >= 0; --x) {
            float val = (float)Math.exp((double)(-((float)(x * x)) / two_sq_sigma));
            gaussianKernel[size / 2 - x] = val;
            gaussianKernel[size / 2 + x] = val;
         }
      }

      if (normalize) {
         float sum = 0.0F;

         for(int i = 0; i < gaussianKernel.length; ++i) {
            sum += gaussianKernel[i];
         }

         for(int i = 0; i < gaussianKernel.length; ++i) {
            gaussianKernel[i] /= sum;
         }
      }

      return gaussianKernel;
   }

   public ComputeCurvatures.FloatArray2D computeGaussianFastMirror(
      ComputeCurvatures.FloatArray2D input, float sigma, GaussianGenerationCallback callback, Calibration calibration
   ) {
      long s0 = System.currentTimeMillis();
      ComputeCurvatures.FloatArray2D output = new ComputeCurvatures.FloatArray2D(input.width, input.height);
      float kernelsumX = 0.0F;
      float kernelsumY = 0.0F;
      float kernelsumZ = 0.0F;
      float pixelWidth = 1.0F;
      float pixelHeight = 1.0F;
      float pixelDepth = 1.0F;
      if (calibration != null) {
         pixelWidth = (float)calibration.pixelWidth;
         pixelHeight = (float)calibration.pixelHeight;
         pixelDepth = (float)calibration.pixelDepth;
      }

      long s1 = System.currentTimeMillis();
      float[] kernelX = createGaussianKernel1D(sigma / pixelWidth, true);
      float[] kernelY = createGaussianKernel1D(sigma / pixelHeight, true);
      int filterSizeX = kernelX.length;
      int filterSizeY = kernelY.length;
      long s2 = System.currentTimeMillis();

      for(int i = 0; i < kernelX.length; ++i) {
         kernelsumX += kernelX[i];
      }

      for(int i = 0; i < kernelY.length; ++i) {
         kernelsumY += kernelY[i];
      }

      long s3 = System.currentTimeMillis();
      double totalPoints = (double)(input.width * input.height * 2);
      long pointsDone = 0L;

      for(int x = 0; x < input.width; ++x) {
         if (this.cancelGeneration) {
            return null;
         }

         for(int y = 0; y < input.height; ++y) {
            float avg = 0.0F;
            if (x - filterSizeX / 2 >= 0 && x + filterSizeX / 2 < input.width) {
               for(int f = -filterSizeX / 2; f <= filterSizeX / 2; ++f) {
                  avg += input.get(x + f, y) * kernelX[f + filterSizeX / 2];
               }
            } else {
               for(int f = -filterSizeX / 2; f <= filterSizeX / 2; ++f) {
                  avg += input.getMirror(x + f, y) * kernelX[f + filterSizeX / 2];
               }
            }

            output.set(avg / kernelsumX, x, y);
         }

         pointsDone += (long)input.height;
         if (callback != null) {
            callback.proportionDone((double)pointsDone / totalPoints);
         }
      }

      long s4 = System.currentTimeMillis();

      for(int x = 0; x < input.width; ++x) {
         if (this.cancelGeneration) {
            return null;
         }

         float[] temp = new float[input.height];

         for(int y = 0; y < input.height; ++y) {
            float avg = 0.0F;
            if (y - filterSizeY / 2 >= 0 && y + filterSizeY / 2 < input.height) {
               for(int f = -filterSizeY / 2; f <= filterSizeY / 2; ++f) {
                  avg += output.get(x, y + f) * kernelY[f + filterSizeY / 2];
               }
            } else {
               for(int f = -filterSizeY / 2; f <= filterSizeY / 2; ++f) {
                  avg += output.getMirror(x, y + f) * kernelY[f + filterSizeY / 2];
               }
            }

            temp[y] = avg / kernelsumY;
         }

         for(int y = 0; y < input.height; ++y) {
            output.set(temp[y], x, y);
         }

         pointsDone += (long)input.height;
         if (callback != null) {
            callback.proportionDone((double)pointsDone / totalPoints);
         }
      }

      long s5 = System.currentTimeMillis();
      if (callback != null) {
         callback.proportionDone(1.0);
      }

      if (!Utils.isReleaseVersion) {
         System.err.println("s1=" + (s1 - s0) + "ms\ts2=" + (s2 - s3) + "ms\ts3=" + (s4 - s3) + "ms\ts4=" + (s5 - s4));
      }

      return output;
   }

   public ComputeCurvatures.FloatArray3D computeGaussianFastMirror(
      ComputeCurvatures.FloatArray3D input, float sigma, GaussianGenerationCallback callback, Calibration calibration
   ) {
      ComputeCurvatures.FloatArray3D output = new ComputeCurvatures.FloatArray3D(input.width, input.height, input.depth);
      float kernelsumX = 0.0F;
      float kernelsumY = 0.0F;
      float kernelsumZ = 0.0F;
      float pixelWidth = 1.0F;
      float pixelHeight = 1.0F;
      float pixelDepth = 1.0F;
      if (calibration != null) {
         pixelWidth = (float)calibration.pixelWidth;
         pixelHeight = (float)calibration.pixelHeight;
         pixelDepth = (float)calibration.pixelDepth;
      }

      float[] kernelX = createGaussianKernel1D(sigma / pixelWidth, true);
      float[] kernelY = createGaussianKernel1D(sigma / pixelHeight, true);
      float[] kernelZ = createGaussianKernel1D(sigma / pixelDepth, true);
      int filterSizeX = kernelX.length;
      int filterSizeY = kernelY.length;
      int filterSizeZ = kernelZ.length;

      for(int i = 0; i < kernelX.length; ++i) {
         kernelsumX += kernelX[i];
      }

      for(int i = 0; i < kernelY.length; ++i) {
         kernelsumY += kernelY[i];
      }

      for(int i = 0; i < kernelZ.length; ++i) {
         kernelsumZ += kernelZ[i];
      }

      double totalPoints = (double)(input.width * input.height * input.depth * 3);
      long pointsDone = 0L;

      for(int x = 0; x < input.width; ++x) {
         if (this.cancelGeneration) {
            return null;
         }

         for(int y = 0; y < input.height; ++y) {
            for(int z = 0; z < input.depth; ++z) {
               float avg = 0.0F;
               if (x - filterSizeX / 2 >= 0 && x + filterSizeX / 2 < input.width) {
                  for(int f = -filterSizeX / 2; f <= filterSizeX / 2; ++f) {
                     avg += input.get(x + f, y, z) * kernelX[f + filterSizeX / 2];
                  }
               } else {
                  for(int f = -filterSizeX / 2; f <= filterSizeX / 2; ++f) {
                     avg += input.getMirror(x + f, y, z) * kernelX[f + filterSizeX / 2];
                  }
               }

               output.set(avg / kernelsumX, x, y, z);
            }
         }

         pointsDone += (long)(input.height * input.depth);
         if (callback != null) {
            callback.proportionDone((double)pointsDone / totalPoints);
         }
      }

      for(int x = 0; x < input.width; ++x) {
         if (this.cancelGeneration) {
            return null;
         }

         for(int z = 0; z < input.depth; ++z) {
            float[] temp = new float[input.height];

            for(int y = 0; y < input.height; ++y) {
               float avg = 0.0F;
               if (y - filterSizeY / 2 >= 0 && y + filterSizeY / 2 < input.height) {
                  for(int f = -filterSizeY / 2; f <= filterSizeY / 2; ++f) {
                     avg += output.get(x, y + f, z) * kernelY[f + filterSizeY / 2];
                  }
               } else {
                  for(int f = -filterSizeY / 2; f <= filterSizeY / 2; ++f) {
                     avg += output.getMirror(x, y + f, z) * kernelY[f + filterSizeY / 2];
                  }
               }

               temp[y] = avg / kernelsumY;
            }

            for(int y = 0; y < input.height; ++y) {
               output.set(temp[y], x, y, z);
            }
         }

         pointsDone += (long)(input.depth * input.height);
         if (callback != null) {
            callback.proportionDone((double)pointsDone / totalPoints);
         }
      }

      for(int x = 0; x < input.width; ++x) {
         if (this.cancelGeneration) {
            return null;
         }

         for(int y = 0; y < input.height; ++y) {
            float[] temp = new float[input.depth];

            for(int z = 0; z < input.depth; ++z) {
               float avg = 0.0F;
               if (z - filterSizeZ / 2 >= 0 && z + filterSizeZ / 2 < input.depth) {
                  for(int f = -filterSizeZ / 2; f <= filterSizeZ / 2; ++f) {
                     avg += output.get(x, y, z + f) * kernelZ[f + filterSizeZ / 2];
                  }
               } else {
                  for(int f = -filterSizeZ / 2; f <= filterSizeZ / 2; ++f) {
                     avg += output.getMirror(x, y, z + f) * kernelZ[f + filterSizeZ / 2];
                  }
               }

               temp[z] = avg / kernelsumZ;
            }

            for(int z = 0; z < input.depth; ++z) {
               output.set(temp[z], x, y, z);
            }
         }

         pointsDone += (long)(input.height * input.depth);
         if (callback != null) {
            callback.proportionDone((double)pointsDone / totalPoints);
         }
      }

      if (callback != null) {
         callback.proportionDone(1.0);
      }

      return output;
   }

   public ComputeCurvatures.FloatArray3D StackToFloatArray(ImageStack stack) {
      Object[] imageStack = stack.getImageArray();
      int width = stack.getWidth();
      int height = stack.getHeight();
      int nstacks = stack.getSize();
      if (imageStack == null || imageStack.length == 0) {
         IJ.error("Image Stack is empty.");
         return null;
      } else if (imageStack[0] instanceof int[]) {
         IJ.error("RGB images not supported at the moment.");
         return null;
      } else {
         ComputeCurvatures.FloatArray3D pixels = new ComputeCurvatures.FloatArray3D(width, height, nstacks);
         if (imageStack[0] instanceof byte[]) {
            for(int countSlice = 0; countSlice < nstacks; ++countSlice) {
               byte[] pixelTmp = (byte[])imageStack[countSlice];
               int count = 0;

               for(int y = 0; y < height; ++y) {
                  for(int x = 0; x < width; ++x) {
                     pixels.data[pixels.getPos(x, y, countSlice)] = (float)(pixelTmp[count++] & 255);
                  }
               }
            }
         } else if (imageStack[0] instanceof short[]) {
            for(int countSlice = 0; countSlice < nstacks; ++countSlice) {
               short[] pixelTmp = (short[])imageStack[countSlice];
               int count = 0;

               for(int y = 0; y < height; ++y) {
                  for(int x = 0; x < width; ++x) {
                     pixels.data[pixels.getPos(x, y, countSlice)] = (float)(pixelTmp[count++] & '\uffff');
                  }
               }
            }
         } else {
            for(int countSlice = 0; countSlice < nstacks; ++countSlice) {
               float[] pixelTmp = (float[])imageStack[countSlice];
               int count = 0;

               for(int y = 0; y < height; ++y) {
                  for(int x = 0; x < width; ++x) {
                     pixels.data[pixels.getPos(x, y, countSlice)] = pixelTmp[count++];
                  }
               }
            }
         }

         return pixels;
      }
   }

   public ComputeCurvatures.FloatArray2D ImageToFloatArray(ImageProcessor ip) {
      Object pixelArray = ip.getPixels();
      int count = 0;
      ComputeCurvatures.FloatArray2D image;
      if (ip instanceof ByteProcessor) {
         image = new ComputeCurvatures.FloatArray2D(ip.getWidth(), ip.getHeight());
         byte[] pixels = (byte[])pixelArray;

         for(int y = 0; y < ip.getHeight(); ++y) {
            for(int x = 0; x < ip.getWidth(); ++x) {
               image.data[count] = (float)(pixels[count++] & 255);
            }
         }
      } else if (ip instanceof ShortProcessor) {
         image = new ComputeCurvatures.FloatArray2D(ip.getWidth(), ip.getHeight());
         short[] pixels = (short[])pixelArray;

         for(int y = 0; y < ip.getHeight(); ++y) {
            for(int x = 0; x < ip.getWidth(); ++x) {
               image.data[count] = (float)(pixels[count++] & '\uffff');
            }
         }
      } else if (ip instanceof FloatProcessor) {
         image = new ComputeCurvatures.FloatArray2D(ip.getWidth(), ip.getHeight());
         float[] pixels = (float[])pixelArray;

         for(int y = 0; y < ip.getHeight(); ++y) {
            for(int x = 0; x < ip.getWidth(); ++x) {
               image.data[count] = pixels[count++];
            }
         }
      } else {
         System.err.println("RGB images not supported");
         image = null;
      }

      return image;
   }

   public abstract class FloatArray {
      public float[] data = null;

      public abstract ComputeCurvatures.FloatArray clone();
   }

   public class FloatArray2D extends ComputeCurvatures.FloatArray {
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

      public ComputeCurvatures.FloatArray2D clone() {
         ComputeCurvatures.FloatArray2D clone = ComputeCurvatures.this.new FloatArray2D(this.width, this.height);
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

   public class FloatArray3D extends ComputeCurvatures.FloatArray {
      public float[] data = null;
      public int width = 0;
      public int height = 0;
      public int depth = 0;

      public FloatArray3D(float[] data, int width, int height, int depth) {
         this.data = data;
         this.width = width;
         this.height = height;
         this.depth = depth;
      }

      public FloatArray3D(int width, int height, int depth) {
         this.data = new float[width * height * depth];
         this.width = width;
         this.height = height;
         this.depth = depth;
      }

      public ComputeCurvatures.FloatArray3D clone() {
         ComputeCurvatures.FloatArray3D clone = ComputeCurvatures.this.new FloatArray3D(this.width, this.height, this.depth);
         System.arraycopy(this.data, 0, clone.data, 0, this.data.length);
         return clone;
      }

      public int getPos(int x, int y, int z) {
         return x + this.width * (y + z * this.height);
      }

      public float get(int x, int y, int z) {
         return this.data[this.getPos(x, y, z)];
      }

      public float getMirror(int x, int y, int z) {
         if (x >= this.width) {
            x = this.width - (x - this.width + 2);
         }

         if (y >= this.height) {
            y = this.height - (y - this.height + 2);
         }

         if (z >= this.depth) {
            z = this.depth - (z - this.depth + 2);
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

         if (z < 0) {
            int tmp = 0;

            for(int dir = 1; z < 0; ++z) {
               tmp += dir;
               if (tmp == this.height - 1 || tmp == 0) {
                  dir *= -1;
               }
            }

            z = tmp;
         }

         return this.data[this.getPos(x, y, z)];
      }

      public void set(float value, int x, int y, int z) {
         this.data[this.getPos(x, y, z)] = value;
      }

      public ComputeCurvatures.FloatArray2D getXPlane(int x) {
         ComputeCurvatures.FloatArray2D plane = ComputeCurvatures.this.new FloatArray2D(this.height, this.depth);

         for(int y = 0; y < this.height; ++y) {
            for(int z = 0; z < this.depth; ++z) {
               plane.set(this.get(x, y, z), y, z);
            }
         }

         return plane;
      }

      public float[][] getXPlane_float(int x) {
         float[][] plane = new float[this.height][this.depth];

         for(int y = 0; y < this.height; ++y) {
            for(int z = 0; z < this.depth; ++z) {
               plane[y][z] = this.get(x, y, z);
            }
         }

         return plane;
      }

      public ComputeCurvatures.FloatArray2D getYPlane(int y) {
         ComputeCurvatures.FloatArray2D plane = ComputeCurvatures.this.new FloatArray2D(this.width, this.depth);

         for(int x = 0; x < this.width; ++x) {
            for(int z = 0; z < this.depth; ++z) {
               plane.set(this.get(x, y, z), x, z);
            }
         }

         return plane;
      }

      public float[][] getYPlane_float(int y) {
         float[][] plane = new float[this.width][this.depth];

         for(int x = 0; x < this.width; ++x) {
            for(int z = 0; z < this.depth; ++z) {
               plane[x][z] = this.get(x, y, z);
            }
         }

         return plane;
      }

      public ComputeCurvatures.FloatArray2D getZPlane(int z) {
         ComputeCurvatures.FloatArray2D plane = ComputeCurvatures.this.new FloatArray2D(this.width, this.height);

         for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
               plane.set(this.get(x, y, z), x, y);
            }
         }

         return plane;
      }

      public float[][] getZPlane_float(int z) {
         float[][] plane = new float[this.width][this.height];

         for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
               plane[x][y] = this.get(x, y, z);
            }
         }

         return plane;
      }

      public void setXPlane(ComputeCurvatures.FloatArray2D plane, int x) {
         for(int y = 0; y < this.height; ++y) {
            for(int z = 0; z < this.depth; ++z) {
               this.set(plane.get(y, z), x, y, z);
            }
         }
      }

      public void setXPlane(float[][] plane, int x) {
         for(int y = 0; y < this.height; ++y) {
            for(int z = 0; z < this.depth; ++z) {
               this.set(plane[y][z], x, y, z);
            }
         }
      }

      public void setYPlane(ComputeCurvatures.FloatArray2D plane, int y) {
         for(int x = 0; x < this.width; ++x) {
            for(int z = 0; z < this.depth; ++z) {
               this.set(plane.get(x, z), x, y, z);
            }
         }
      }

      public void setYPlane(float[][] plane, int y) {
         for(int x = 0; x < this.width; ++x) {
            for(int z = 0; z < this.depth; ++z) {
               this.set(plane[x][z], x, y, z);
            }
         }
      }

      public void setZPlane(ComputeCurvatures.FloatArray2D plane, int z) {
         for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
               this.set(plane.get(x, y), x, y, z);
            }
         }
      }

      public void setZPlane(float[][] plane, int z) {
         for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
               this.set(plane[x][y], x, y, z);
            }
         }
      }
   }

   static class TrivialProgressDisplayer implements GaussianGenerationCallback {
      @Override
      public void proportionDone(double proportion) {
         if (proportion < 0.0) {
            IJ.showProgress(1.0);
         } else {
            IJ.showProgress(proportion);
         }
      }
   }
}
