package math3d;

public class JacobiDouble {
   private double[][] matrix;
   private double[][] eigenmatrix;
   private double[] eigenvalues;
   private int numberOfRotationsNeeded;
   private int maxSweeps;

   public JacobiDouble(double[][] matrix) {
      this(matrix, 50);
   }

   public JacobiDouble(double[][] matrix, int maxSweeps) {
      this.matrix = matrix;

      for(int i = 0; i < matrix.length; ++i) {
         for(int j = i + 1; j < matrix.length; ++j) {
            if (!this.isSmallComparedTo(Math.abs(matrix[i][j] - matrix[j][i]), matrix[i][j])) {
               throw new RuntimeException("Matrix is not symmetric!");
            }
         }
      }

      this.eigenmatrix = new double[matrix.length][matrix.length];
      this.eigenvalues = new double[matrix.length];
      this.maxSweeps = maxSweeps;
      this.perform();
   }

   public double[][] getEigenVectors() {
      return FastMatrixN.transpose(this.eigenmatrix);
   }

   public double[][] getEigenMatrix() {
      return this.eigenmatrix;
   }

   public double[] getEigenValues() {
      return this.eigenvalues;
   }

   public int getNumberOfRotations() {
      return this.numberOfRotationsNeeded;
   }

   private double offDiagonalSum() {
      double sum = 0.0;

      for(int i = 0; i < this.matrix.length - 1; ++i) {
         for(int j = i + 1; j < this.matrix.length; ++j) {
            sum += Math.abs(this.matrix[i][j]);
         }
      }

      return sum;
   }

   private void rotate(int i, int j, int k, int l, double s, double tau) {
      double tmp1 = this.matrix[i][j];
      double tmp2 = this.matrix[k][l];
      this.matrix[i][j] = tmp1 - s * (tmp2 + tmp1 * tau);
      this.matrix[k][l] = tmp2 + s * (tmp1 - tmp2 * tau);
   }

   private void rotateEigenMatrix(int i, int j, int k, int l, double s, double tau) {
      double tmp1 = this.eigenmatrix[i][j];
      double tmp2 = this.eigenmatrix[k][l];
      this.eigenmatrix[i][j] = tmp1 - s * (tmp2 + tmp1 * tau);
      this.eigenmatrix[k][l] = tmp2 + s * (tmp1 - tmp2 * tau);
   }

   private boolean isSmallComparedTo(double value, double reference) {
      return Math.abs(reference) + value == Math.abs(reference);
   }

   private void perform() {
      double[] b = new double[this.matrix.length];
      double[] z = new double[this.matrix.length];

      for(int i = 0; i < this.matrix.length; ++i) {
         for(int j = 0; j < this.matrix.length; ++j) {
            this.eigenmatrix[i][j] = 0.0;
         }

         this.eigenmatrix[i][i] = 1.0;
         b[i] = this.eigenvalues[i] = this.matrix[i][i];
         z[i] = 0.0;
      }

      this.numberOfRotationsNeeded = 0;

      for(int sweeps = 0; sweeps < this.maxSweeps; ++sweeps) {
         double sum = this.offDiagonalSum();
         if (sum == 0.0) {
            return;
         }

         double thresh = 0.0;
         if (sweeps < 3) {
            thresh = 0.2F * sum / (double)(this.matrix.length * this.matrix.length);
         }

         for(int p = 0; p < this.matrix.length - 1; ++p) {
            for(int q = p + 1; q < this.matrix.length; ++q) {
               double tmp = 100.0 * Math.abs(this.matrix[p][q]);
               if (sweeps > 3 && this.isSmallComparedTo(tmp, this.eigenvalues[p]) && this.isSmallComparedTo(tmp, this.eigenvalues[q])) {
                  this.matrix[p][q] = 0.0;
               } else if (Math.abs(this.matrix[p][q]) > thresh) {
                  double diff = this.eigenvalues[q] - this.eigenvalues[p];
                  double t;
                  if (this.isSmallComparedTo(tmp, diff)) {
                     t = this.matrix[p][q] / diff;
                  } else {
                     double theta = 0.5 * diff / this.matrix[p][q];
                     t = 1.0 / (Math.abs(theta) + Math.sqrt(1.0 + theta * theta));
                     if (theta < 0.0) {
                        t = -t;
                     }
                  }

                  double c = 1.0 / Math.sqrt(1.0 + t * t);
                  double s = t * c;
                  double tau = s / (1.0 + c);
                  double h = t * this.matrix[p][q];
                  z[p] -= h;
                  z[q] += h;
                  this.eigenvalues[p] -= h;
                  this.eigenvalues[q] += h;
                  this.matrix[p][q] = 0.0;

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
            z[p] = 0.0;
         }
      }
   }

   public static String toString(double[] doubleArray) {
      String result = "{";

      for(int i = 0; i < doubleArray.length; ++i) {
         if (i > 0) {
            result = result + ",";
         }

         result = result + doubleArray[i];
      }

      return result + "}";
   }

   public static String toString(double[][] double2Array) {
      String result = "{";

      for(int i = 0; i < double2Array.length; ++i) {
         if (i > 0) {
            result = result + ",";
         }

         result = result + toString(double2Array[i]);
      }

      return result + "}";
   }

   public static double[] getColumn(double[][] matrix, int i) {
      double[] result = new double[matrix.length];

      for(int j = 0; j < matrix.length; ++j) {
         result[j] = matrix[j][i];
      }

      return result;
   }

   public static double[][] matMult(double[][] m1, double[][] m2) {
      int r = m1.length;
      int c = m2[0].length;
      double[][] result = new double[r][c];

      for(int i = 0; i < r; ++i) {
         for(int j = 0; j < c; ++j) {
            result[i][j] = 0.0;

            for(int k = 0; k < m2.length; ++k) {
               result[i][j] += m1[i][k] * m2[k][j];
            }
         }
      }

      return result;
   }

   public static double[] vecMult(double[][] m, double[] v) {
      int r = m.length;
      double[] result = new double[r];

      for(int i = 0; i < r; ++i) {
         result[i] = 0.0;

         for(int k = 0; k < v.length; ++k) {
            result[i] += m[i][k] * v[k];
         }
      }

      return result;
   }

   public static double[][] transpose(double[][] m) {
      int r = m.length;
      int c = m[0].length;
      double[][] result = new double[c][r];

      for(int i = 0; i < r; ++i) {
         for(int j = 0; j < c; ++j) {
            result[j][i] = m[i][j];
         }
      }

      return result;
   }

   public static void main(String[] args) {
      double[][] matrix = new double[][]{{1.0, 2.0}, {2.0, 1.0}};
      JacobiDouble jacobi = new JacobiDouble(matrix);
      double[] eigenValuesVector = jacobi.getEigenValues();
      double[][] eigenValues = new double[eigenValuesVector.length][eigenValuesVector.length];

      for(int i = 0; i < eigenValuesVector.length; ++i) {
         eigenValues[i][i] = eigenValuesVector[i];
      }

      double[][] eigenVectors = jacobi.getEigenVectors();
      double[][] result = matMult(eigenVectors, matMult(eigenValues, transpose(eigenVectors)));
      System.out.println("out: " + toString(result));
   }
}
