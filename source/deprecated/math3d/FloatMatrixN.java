package math3d;

import ij.IJ;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class FloatMatrixN {
   public static void invert(float[][] matrix) {
      invert(matrix, false);
   }

   public static void invert(float[][] matrix, boolean showStatus) {
      int M = matrix.length;
      if (M != matrix[0].length) {
         throw new RuntimeException("invert: no square matrix");
      } else {
         float[][] other = new float[M][M];

         for(int i = 0; i < M; ++i) {
            other[i][i] = 1.0F;
         }

         for(int i = 0; i < M; ++i) {
            if (showStatus) {
               IJ.showStatus("invert matrix: " + i + "/" + 2 * M);
            }

            int p = i;

            for(int j = i + 1; j < M; ++j) {
               if (Math.abs(matrix[j][i]) > Math.abs(matrix[p][i])) {
                  p = j;
               }
            }

            if (p != i) {
               float[] d = matrix[p];
               matrix[p] = matrix[i];
               matrix[i] = d;
               d = other[p];
               other[p] = other[i];
               other[i] = d;
            }

            if (matrix[i][i] != 1.0F) {
               float f = matrix[i][i];

               for(int j = i; j < M; ++j) {
                  matrix[i][j] /= f;
               }

               for(int j = 0; j < M; ++j) {
                  other[i][j] /= f;
               }
            }

            for(int j = i + 1; j < M; ++j) {
               float f = matrix[j][i];

               for(int k = i; k < M; ++k) {
                  matrix[j][k] -= f * matrix[i][k];
               }

               for(int k = 0; k < M; ++k) {
                  other[j][k] -= f * other[i][k];
               }
            }
         }

         for(int i = M - 1; i > 0; --i) {
            if (showStatus) {
               IJ.showStatus("invert matrix: " + (2 * M - i) + "/" + 2 * M);
            }

            for(int j = i - 1; j >= 0; --j) {
               float f = matrix[j][i] / matrix[i][i];

               for(int k = i; k < M; ++k) {
                  matrix[j][k] -= f * matrix[i][k];
               }

               for(int k = 0; k < M; ++k) {
                  other[j][k] -= f * other[i][k];
               }
            }
         }

         for(int i = 0; i < M; ++i) {
            matrix[i] = other[i];
         }
      }
   }

   public static float[][] clone(float[][] matrix) {
      int M = matrix.length;
      int N = matrix[0].length;
      float[][] result = new float[M][N];

      for(int i = 0; i < M; ++i) {
         System.arraycopy(matrix[i], 0, result[i], 0, N);
      }

      return result;
   }

   public static float[][] times(float[][] m1, float[][] m2) {
      int K = m2.length;
      if (m1[0].length != m2.length) {
         throw new RuntimeException("rank mismatch");
      } else {
         int M = m1.length;
         int N = m2[0].length;
         float[][] result = new float[M][N];

         for(int i = 0; i < M; ++i) {
            for(int j = 0; j < N; ++j) {
               for(int k = 0; k < K; ++k) {
                  result[i][j] += m1[i][k] * m2[k][j];
               }
            }
         }

         return result;
      }
   }

   public static float[] times(float[][] m, float[] v) {
      int K = v.length;
      if (m[0].length != v.length) {
         throw new RuntimeException("rank mismatch");
      } else {
         int M = m.length;
         float[] result = new float[M];

         for(int i = 0; i < M; ++i) {
            for(int k = 0; k < K; ++k) {
               result[i] += m[i][k] * v[k];
            }
         }

         return result;
      }
   }

   static float[][] LU_decomposition(float[][] m) {
      int N = m.length;
      float[][] R = new float[N][N];
      float[][] L = new float[N][N];

      for(int i = 0; i < N; ++i) {
         for(int j = i; j < N; ++j) {
            R[i][j] = m[i][j];

            for(int k = 0; k < i; ++k) {
               R[i][j] -= L[i][k] * R[k][j];
            }
         }

         for(int j = i + 1; j < N; ++j) {
            L[j][i] = m[j][i];

            for(int k = 0; k < i; ++k) {
               L[j][i] -= L[j][k] * R[k][i];
            }

            L[j][i] /= R[i][i];
         }
      }

      float[][] LU = new float[N][N];

      for(int i = 0; i < N; ++i) {
         for(int j = 0; j < N; ++j) {
            LU[i][j] = L[i][j] + R[i][j];
         }
      }

      return LU;
   }

   static float[][] choleskyDecomposition(float[][] m) {
      if (m.length != m[0].length) {
         throw new RuntimeException("Row and column rank must be equal");
      } else {
         int N = m.length;
         float[][] l = new float[N][N];

         for(int i = 0; i < N; ++i) {
            for(int j = 0; j < N; ++j) {
               l[i][j] = 0.0F;
            }
         }

         float sum = 0.0F;

         for(int i = 0; i < N; ++i) {
            sum = 0.0F;

            for(int k = 0; k < i; ++k) {
               sum += l[k][i] * l[k][i];
            }

            if (m[i][i] - sum < 0.0F) {
               throw new RuntimeException("Matrix must be positive definite (trace is " + sum + ", but diagonal element " + i + " is " + m[i][i] + ")");
            }

            l[i][i] = (float)Math.sqrt((double)(m[i][i] - sum));

            for(int j = i + 1; j < N; ++j) {
               sum = 0.0F;

               for(int k = 0; k < i; ++k) {
                  sum += l[k][j] * l[k][i];
               }

               l[i][j] = (m[i][j] - sum) / l[i][i];
            }
         }

         return l;
      }
   }

   public static float[][] transpose(float[][] m) {
      float[][] ret = new float[m[0].length][m.length];

      for(int i = 0; i < ret.length; ++i) {
         for(int j = 0; j < ret[i].length; ++j) {
            ret[i][j] = m[j][i];
         }
      }

      return ret;
   }

   public static float[] solve_UL(float[][] A, float[] b) {
      float[][] LU = LU_decomposition(A);
      float[] y = new float[b.length];

      for(int i = 0; i < y.length; ++i) {
         float sum = 0.0F;

         for(int j = 0; j < i; ++j) {
            sum += LU[i][j] * y[j];
         }

         y[i] = b[i] - sum;
      }

      float[] x = new float[b.length];

      for(int i = x.length - 1; i >= 0; --i) {
         float sum = 0.0F;

         for(int j = i + 1; j < x.length; ++j) {
            sum += LU[i][j] * x[j];
         }

         x[i] = (y[i] - sum) / LU[i][i];
      }

      return x;
   }

   public static float[] solve_cholesky(float[][] A, float[] b) {
      try {
         float[][] U = choleskyDecomposition(A);
      } catch (RuntimeException var6) {
         throw var6;
      }

      float[][] var7 = choleskyDecomposition(A);
      float[][] L = transpose(var7);
      float[] y = forward_substitution(L, b);
      return backward_substitution(var7, y);
   }

   private static float[] backward_substitution(float[][] U, float[] b) {
      float[] x = new float[b.length];

      for(int i = x.length - 1; i >= 0; --i) {
         float sum = 0.0F;

         for(int j = i + 1; j < x.length; ++j) {
            sum += U[i][j] * x[j];
         }

         x[i] = (b[i] - sum) / U[i][i];
      }

      return x;
   }

   private static float[] forward_substitution(float[][] L, float[] b) {
      float[] y = new float[b.length];

      for(int i = 0; i < y.length; ++i) {
         float sum = 0.0F;

         for(int j = 0; j < i; ++j) {
            sum += L[i][j] * y[j];
         }

         y[i] = (b[i] - sum) / L[i][i];
      }

      return y;
   }

   public static float[] apply(float[][] A, float[] x) {
      int m = A.length;
      int n = A[0].length;
      float[] b = new float[x.length];

      for(int i = 0; i < m; ++i) {
         b[i] = 0.0F;

         for(int j = 0; j < n; ++j) {
            b[i] += A[i][j] * x[j];
         }
      }

      return b;
   }

   public static void print(float[] v) {
      System.out.print("[");

      for(int i = 0; i < v.length; ++i) {
         System.out.print(v[i] + ", ");
      }

      System.out.print("]");
      System.out.println();
   }

   public static void print(float[][] m) {
      print(m, System.out);
   }

   public static void print(float[][] m, PrintStream out, char del) {
      DecimalFormat f = new DecimalFormat("0.00f");

      for(int i = 0; i < m.length; ++i) {
         for(int j = 0; j < m[i].length; ++j) {
            out.print(f.format((double)m[i][j]) + del);
         }

         out.println("");
      }

      out.println();
   }

   public static float round(float d, int scale, RoundingMode mode) {
      BigDecimal bd = BigDecimal.valueOf((double)d);
      return bd.setScale(scale, mode).floatValue();
   }

   public static void print(float[][] m, PrintStream out) {
      print(m, out, '\t');
   }

   public static void main(String[] args) {
      float dou = 1234.1234F;
      BigDecimal bd = BigDecimal.valueOf((double)dou);
      System.out.println(bd.unscaledValue() + " " + bd.scale());
      int inte = BigDecimal.valueOf((double)dou).movePointLeft(2).unscaledValue().intValue() * 100;
      bd = bd.movePointLeft(2);
      System.out.println(bd.unscaledValue());
      System.out.println("Test rounding");
      float d = 1.2345679F;
      System.out.println("Math.round(" + d + ") = " + Math.round(d));
      System.out.println("round(" + d + ",2) = " + round(d, 2, RoundingMode.HALF_EVEN));
      float[][] m = new float[][]{{1.0F, 2.0F, 3.0F, 2.0F}, {-1.0F, 0.0F, 2.0F, -3.0F}, {-2.0F, 1.0F, 1.0F, 1.0F}, {0.0F, -2.0F, 3.0F, 0.0F}};
      float[][] m1 = clone(m);
      invert(m1);
      float[][] m2 = times(m, m1);
      print(m2);
      float[][] k = new float[5][5];

      for(int i = 0; i < k.length; ++i) {
         for(int j = i; j < k.length; ++j) {
            k[i][j] = k[j][i] = 1.0F / (float)(i + j + 1);
         }
      }

      System.out.println("Original matrix ");
      print(k);
      float[][] l = choleskyDecomposition(k);
      System.out.println("Upper triangular form u of cholesky decomposition ");
      print(l);
      float[][] l_t = transpose(l);
      System.out.println("Transposed form u^T of u ");
      print(l_t);
      float[][] prod = times(l_t, l);
      System.out.println("Finally the product of the u^T and u, which should give the original matrix ");
      print(prod);
      float[] x = new float[]{1.0F, 2.0F, 3.0F, 4.0F, 5.0F};
      System.out.println("A vector x: x = [1.0f 2.0f 3.0f]^T\n");
      float[] b = apply(k, x);
      System.out.println("Applying the original matrix to x gives b: ");
      print(b);
      System.out.println("\n\nTest different solve methods");
      System.out.println("\nTest Cholesky decomposition");
      float[] x_n = solve_cholesky(k, b);
      System.out.println("Now solve Ax = b for x and see if it is the original x");
      print(x_n);
      System.out.println("\nTest LU decomposition");
      System.out.println("Now solve Ax = b for x and see if it is the original x");
      x_n = solve_UL(k, b);
      print(x_n);
      System.out.println("\nTest ordinary invert method");
      System.out.println("Now solve Ax = b for x and see if it is the original x");
      float[][] k_inv = clone(k);
      invert(k_inv);
      x_n = apply(k_inv, b);
      print(x_n);
   }
}
