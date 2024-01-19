package math3d;

public class JacobiFloat {
   private float[][] matrix;
   private float[][] eigenmatrix;
   private float[] eigenvalues;
   private int numberOfRotationsNeeded;
   private int maxSweeps;

   public JacobiFloat(float[][] matrix) {
      this(matrix, 50);
   }

   public JacobiFloat(float[][] matrix, int maxSweeps) {
      this.matrix = matrix;

      for(int i = 0; i < matrix.length; ++i) {
         for(int j = i + 1; j < matrix.length; ++j) {
            if (!this.isSmallComparedTo(Math.abs(matrix[i][j] - matrix[j][i]), matrix[i][j])) {
               throw new RuntimeException("Matrix is not symmetric!");
            }
         }
      }

      this.eigenmatrix = new float[matrix.length][matrix.length];
      this.eigenvalues = new float[matrix.length];
      this.maxSweeps = maxSweeps;
      this.perform();
   }

   public float[][] getEigenVectors() {
      return FloatMatrixN.transpose(this.eigenmatrix);
   }

   public float[][] getEigenMatrix() {
      return this.eigenmatrix;
   }

   public float[] getEigenValues() {
      return this.eigenvalues;
   }

   public int getNumberOfRotations() {
      return this.numberOfRotationsNeeded;
   }

   private float offDiagonalSum() {
      float sum = 0.0F;

      for(int i = 0; i < this.matrix.length - 1; ++i) {
         for(int j = i + 1; j < this.matrix.length; ++j) {
            sum += Math.abs(this.matrix[i][j]);
         }
      }

      return sum;
   }

   private void rotate(int i, int j, int k, int l, float s, float tau) {
      float tmp1 = this.matrix[i][j];
      float tmp2 = this.matrix[k][l];
      this.matrix[i][j] = tmp1 - s * (tmp2 + tmp1 * tau);
      this.matrix[k][l] = tmp2 + s * (tmp1 - tmp2 * tau);
   }

   private void rotateEigenMatrix(int i, int j, int k, int l, float s, float tau) {
      float tmp1 = this.eigenmatrix[i][j];
      float tmp2 = this.eigenmatrix[k][l];
      this.eigenmatrix[i][j] = tmp1 - s * (tmp2 + tmp1 * tau);
      this.eigenmatrix[k][l] = tmp2 + s * (tmp1 - tmp2 * tau);
   }

   private boolean isSmallComparedTo(float value, float reference) {
      return Math.abs(reference) + value == Math.abs(reference);
   }

   private void perform() {
      float[] b = new float[this.matrix.length];
      float[] z = new float[this.matrix.length];

      for(int i = 0; i < this.matrix.length; ++i) {
         for(int j = 0; j < this.matrix.length; ++j) {
            this.eigenmatrix[i][j] = 0.0F;
         }

         this.eigenmatrix[i][i] = 1.0F;
         b[i] = this.eigenvalues[i] = this.matrix[i][i];
         z[i] = 0.0F;
      }

      this.numberOfRotationsNeeded = 0;

      for(int sweeps = 0; sweeps < this.maxSweeps; ++sweeps) {
         float sum = this.offDiagonalSum();
         if (sum == 0.0F) {
            return;
         }

         float thresh = 0.0F;
         if (sweeps < 3) {
            thresh = 0.2F * sum / (float)(this.matrix.length * this.matrix.length);
         }

         for(int p = 0; p < this.matrix.length - 1; ++p) {
            for(int q = p + 1; q < this.matrix.length; ++q) {
               float tmp = 100.0F * Math.abs(this.matrix[p][q]);
               if (sweeps > 3 && this.isSmallComparedTo(tmp, this.eigenvalues[p]) && this.isSmallComparedTo(tmp, this.eigenvalues[q])) {
                  this.matrix[p][q] = 0.0F;
               } else if (Math.abs(this.matrix[p][q]) > thresh) {
                  float diff = this.eigenvalues[q] - this.eigenvalues[p];
                  float t;
                  if (this.isSmallComparedTo(tmp, diff)) {
                     t = this.matrix[p][q] / diff;
                  } else {
                     float theta = 0.5F * diff / this.matrix[p][q];
                     t = 1.0F / (float)((double)Math.abs(theta) + Math.sqrt((double)(1.0F + theta * theta)));
                     if (theta < 0.0F) {
                        t = -t;
                     }
                  }

                  float c = 1.0F / (float)Math.sqrt((double)(1.0F + t * t));
                  float s = t * c;
                  float tau = s / (1.0F + c);
                  float h = t * this.matrix[p][q];
                  z[p] -= h;
                  z[q] += h;
                  this.eigenvalues[p] -= h;
                  this.eigenvalues[q] += h;
                  this.matrix[p][q] = 0.0F;

                  for(int j = 0; j <= p - 1; ++j) {
                     this.rotate(j, p, j, q, s, tau);
                  }

                  for(int j = p + 1; j <= q - 1; ++j) {
                     this.rotate(p, j, j, q, s, tau);
                  }

                  for(int j = q + 1; j < this.matrix.length; ++j) {
                     this.rotate(p, j, q, j, s, tau);
                  }

                  for(int j = 0; j < this.matrix.length; ++j) {
                     this.rotateEigenMatrix(j, p, j, q, s, tau);
                  }

                  ++this.numberOfRotationsNeeded;
               }
            }
         }

         for(int p = 0; p < this.matrix.length; ++p) {
            b[p] += z[p];
            this.eigenvalues[p] = b[p];
            z[p] = 0.0F;
         }
      }
   }

   public static String toString(float[] floatArray) {
      String result = "{";

      for(int i = 0; i < floatArray.length; ++i) {
         if (i > 0) {
            result = result + ",";
         }

         result = result + floatArray[i];
      }

      return result + "}";
   }

   public static String toString(float[][] float2Array) {
      String result = "{";

      for(int i = 0; i < float2Array.length; ++i) {
         if (i > 0) {
            result = result + ",";
         }

         result = result + toString(float2Array[i]);
      }

      return result + "}";
   }

   public static float[] getColumn(float[][] matrix, int i) {
      float[] result = new float[matrix.length];

      for(int j = 0; j < matrix.length; ++j) {
         result[j] = matrix[j][i];
      }

      return result;
   }

   public static float[][] matMult(float[][] m1, float[][] m2) {
      int r = m1.length;
      int c = m2[0].length;
      float[][] result = new float[r][c];

      for(int i = 0; i < r; ++i) {
         for(int j = 0; j < c; ++j) {
            result[i][j] = 0.0F;

            for(int k = 0; k < m2.length; ++k) {
               result[i][j] += m1[i][k] * m2[k][j];
            }
         }
      }

      return result;
   }

   public static float[] vecMult(float[][] m, float[] v) {
      int r = m.length;
      float[] result = new float[r];

      for(int i = 0; i < r; ++i) {
         result[i] = 0.0F;

         for(int k = 0; k < v.length; ++k) {
            result[i] += m[i][k] * v[k];
         }
      }

      return result;
   }

   public static float[][] transpose(float[][] m) {
      int r = m.length;
      int c = m[0].length;
      float[][] result = new float[c][r];

      for(int i = 0; i < r; ++i) {
         for(int j = 0; j < c; ++j) {
            result[j][i] = m[i][j];
         }
      }

      return result;
   }

   public static void main(String[] args) {
      float[][] matrix = new float[][]{{1.0F, 2.0F}, {2.0F, 1.0F}};
      JacobiFloat jacobi = new JacobiFloat(matrix);
      float[] eigenValuesVector = jacobi.getEigenValues();
      float[][] eigenValues = new float[eigenValuesVector.length][eigenValuesVector.length];

      for(int i = 0; i < eigenValuesVector.length; ++i) {
         eigenValues[i][i] = eigenValuesVector[i];
      }

      float[][] eigenVectors = jacobi.getEigenVectors();
      float[][] result = matMult(eigenVectors, matMult(eigenValues, transpose(eigenVectors)));
      System.out.println("out: " + toString(result));
   }
}
