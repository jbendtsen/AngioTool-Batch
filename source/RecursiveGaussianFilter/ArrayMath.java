package RecursiveGaussianFilter;

import java.util.Random;

public class ArrayMath {
   public static final double E = Math.E;
   public static final float FLT_E = (float) Math.E;
   public static final double DBL_E = Math.E;
   public static final double PI = Math.PI;
   public static final float FLT_PI = (float) Math.PI;
   public static final double DBL_PI = Math.PI;
   public static final float FLT_MAX = Float.MAX_VALUE;
   public static final float FLT_MIN = Float.MIN_VALUE;
   public static final float FLT_EPSILON = 1.1920929E-7F;
   public static final double DBL_MAX = Double.MAX_VALUE;
   public static final double DBL_MIN = Double.MIN_VALUE;
   public static final double DBL_EPSILON = 2.220446E-16F;
   private static Random _random = new Random();
   private static final int NSMALL_SORT = 7;
   private static final int NLARGE_SORT = 40;
   private static ArrayMath.Binary _add = new ArrayMath.Binary() {
      @Override
      void apply(float[] rx, float[] ry, float[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] + ry[i1];
         }
      }

      @Override
      void apply(float ra, float[] ry, float[] rz) {
         int n1 = ry.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = ra + ry[i1];
         }
      }

      @Override
      void apply(float[] rx, float rb, float[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] + rb;
         }
      }

      @Override
      void apply(double[] rx, double[] ry, double[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] + ry[i1];
         }
      }

      @Override
      void apply(double ra, double[] ry, double[] rz) {
         int n1 = ry.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = ra + ry[i1];
         }
      }

      @Override
      void apply(double[] rx, double rb, double[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] + rb;
         }
      }
   };
   private static ArrayMath.Binary _sub = new ArrayMath.Binary() {
      @Override
      void apply(float[] rx, float[] ry, float[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] - ry[i1];
         }
      }

      @Override
      void apply(float ra, float[] ry, float[] rz) {
         int n1 = ry.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = ra - ry[i1];
         }
      }

      @Override
      void apply(float[] rx, float rb, float[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] - rb;
         }
      }

      @Override
      void apply(double[] rx, double[] ry, double[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] - ry[i1];
         }
      }

      @Override
      void apply(double ra, double[] ry, double[] rz) {
         int n1 = ry.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = ra - ry[i1];
         }
      }

      @Override
      void apply(double[] rx, double rb, double[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] - rb;
         }
      }
   };
   private static ArrayMath.Binary _mul = new ArrayMath.Binary() {
      @Override
      void apply(float[] rx, float[] ry, float[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] * ry[i1];
         }
      }

      @Override
      void apply(float ra, float[] ry, float[] rz) {
         int n1 = ry.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = ra * ry[i1];
         }
      }

      @Override
      void apply(float[] rx, float rb, float[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] * rb;
         }
      }

      @Override
      void apply(double[] rx, double[] ry, double[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] * ry[i1];
         }
      }

      @Override
      void apply(double ra, double[] ry, double[] rz) {
         int n1 = ry.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = ra * ry[i1];
         }
      }

      @Override
      void apply(double[] rx, double rb, double[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] * rb;
         }
      }
   };
   private static ArrayMath.Binary _div = new ArrayMath.Binary() {
      @Override
      void apply(float[] rx, float[] ry, float[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] / ry[i1];
         }
      }

      @Override
      void apply(float ra, float[] ry, float[] rz) {
         int n1 = ry.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = ra / ry[i1];
         }
      }

      @Override
      void apply(float[] rx, float rb, float[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] / rb;
         }
      }

      @Override
      void apply(double[] rx, double[] ry, double[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] / ry[i1];
         }
      }

      @Override
      void apply(double ra, double[] ry, double[] rz) {
         int n1 = ry.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = ra / ry[i1];
         }
      }

      @Override
      void apply(double[] rx, double rb, double[] rz) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            rz[i1] = rx[i1] / rb;
         }
      }
   };
   private static ArrayMath.Unary _abs = new ArrayMath.Unary() {
      @Override
      void apply(float[] rx, float[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            float rxi = rx[i1];
            ry[i1] = rxi >= 0.0F ? rxi : -rxi;
         }
      }

      @Override
      void apply(double[] rx, double[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            double rxi = rx[i1];
            ry[i1] = rxi >= 0.0 ? rxi : -rxi;
         }
      }
   };
   private static ArrayMath.Unary _neg = new ArrayMath.Unary() {
      @Override
      void apply(float[] rx, float[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = -rx[i1];
         }
      }

      @Override
      void apply(double[] rx, double[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = -rx[i1];
         }
      }
   };
   private static ArrayMath.Unary _cos = new ArrayMath.Unary() {
      @Override
      void apply(float[] rx, float[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = (float)Math.cos((double)rx[i1]);
         }
      }

      @Override
      void apply(double[] rx, double[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = Math.cos(rx[i1]);
         }
      }
   };
   private static ArrayMath.Unary _sin = new ArrayMath.Unary() {
      @Override
      void apply(float[] rx, float[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = (float)Math.sin((double)rx[i1]);
         }
      }

      @Override
      void apply(double[] rx, double[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = Math.sin(rx[i1]);
         }
      }
   };
   private static ArrayMath.Unary _exp = new ArrayMath.Unary() {
      @Override
      void apply(float[] rx, float[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = (float)Math.exp((double)rx[i1]);
         }
      }

      @Override
      void apply(double[] rx, double[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = Math.exp(rx[i1]);
         }
      }
   };
   private static ArrayMath.Unary _log = new ArrayMath.Unary() {
      @Override
      void apply(float[] rx, float[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = (float)Math.log((double)rx[i1]);
         }
      }

      @Override
      void apply(double[] rx, double[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = Math.log(rx[i1]);
         }
      }
   };
   private static ArrayMath.Unary _log10 = new ArrayMath.Unary() {
      @Override
      void apply(float[] rx, float[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = (float)Math.log10((double)rx[i1]);
         }
      }

      @Override
      void apply(double[] rx, double[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = Math.log10(rx[i1]);
         }
      }
   };
   private static ArrayMath.Unary _sqrt = new ArrayMath.Unary() {
      @Override
      void apply(float[] rx, float[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = (float)Math.sqrt((double)rx[i1]);
         }
      }

      @Override
      void apply(double[] rx, double[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = Math.sqrt(rx[i1]);
         }
      }
   };
   private static ArrayMath.Unary _sgn = new ArrayMath.Unary() {
      @Override
      void apply(float[] rx, float[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = rx[i1] > 0.0F ? 1.0F : (rx[i1] < 0.0F ? -1.0F : 0.0F);
         }
      }

      @Override
      void apply(double[] rx, double[] ry) {
         int n1 = rx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1] = rx[i1] > 0.0 ? 1.0 : (rx[i1] < 0.0 ? -1.0 : 0.0);
         }
      }
   };
   private static ArrayMath.CBinary _cadd = new ArrayMath.CBinary() {
      @Override
      void apply(float[] cx, float[] cy, float[] cz) {
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = cx[ir] + cy[ir];
            cz[ii] = cx[ii] + cy[ii];
            ir += 2;
         }
      }

      @Override
      void apply(Cfloat ca, float[] cy, float[] cz) {
         int n1 = cy.length / 2;
         float ar = ca.r;
         float ai = ca.i;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = ar + cy[ir];
            cz[ii] = ai + cy[ii];
            ir += 2;
         }
      }

      @Override
      void apply(float[] cx, Cfloat cb, float[] cz) {
         int n1 = cx.length / 2;
         float br = cb.r;
         float bi = cb.i;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = cx[ir] + br;
            cz[ii] = cx[ii] + bi;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy, double[] cz) {
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = cx[ir] + cy[ir];
            cz[ii] = cx[ii] + cy[ii];
            ir += 2;
         }
      }

      @Override
      void apply(Cdouble ca, double[] cy, double[] cz) {
         int n1 = cy.length / 2;
         double ar = ca.r;
         double ai = ca.i;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = ar + cy[ir];
            cz[ii] = ai + cy[ii];
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, Cdouble cb, double[] cz) {
         int n1 = cx.length / 2;
         double br = cb.r;
         double bi = cb.i;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = cx[ir] + br;
            cz[ii] = cx[ii] + bi;
            ir += 2;
         }
      }
   };
   private static ArrayMath.CBinary _csub = new ArrayMath.CBinary() {
      @Override
      void apply(float[] cx, float[] cy, float[] cz) {
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = cx[ir] - cy[ir];
            cz[ii] = cx[ii] - cy[ii];
            ir += 2;
         }
      }

      @Override
      void apply(Cfloat ca, float[] cy, float[] cz) {
         float ar = ca.r;
         float ai = ca.i;
         int n1 = cy.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = ar - cy[ir];
            cz[ii] = ai - cy[ii];
            ir += 2;
         }
      }

      @Override
      void apply(float[] cx, Cfloat cb, float[] cz) {
         float br = cb.r;
         float bi = cb.i;
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = cx[ir] - br;
            cz[ii] = cx[ii] - bi;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy, double[] cz) {
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = cx[ir] - cy[ir];
            cz[ii] = cx[ii] - cy[ii];
            ir += 2;
         }
      }

      @Override
      void apply(Cdouble ca, double[] cy, double[] cz) {
         double ar = ca.r;
         double ai = ca.i;
         int n1 = cy.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = ar - cy[ir];
            cz[ii] = ai - cy[ii];
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, Cdouble cb, double[] cz) {
         double br = cb.r;
         double bi = cb.i;
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cz[ir] = cx[ir] - br;
            cz[ii] = cx[ii] - bi;
            ir += 2;
         }
      }
   };
   private static ArrayMath.CBinary _cmul = new ArrayMath.CBinary() {
      @Override
      void apply(float[] cx, float[] cy, float[] cz) {
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            float xr = cx[ir];
            float xi = cx[ii];
            float yr = cy[ir];
            float yi = cy[ii];
            cz[ir] = xr * yr - xi * yi;
            cz[ii] = xr * yi + xi * yr;
            ir += 2;
         }
      }

      @Override
      void apply(Cfloat ca, float[] cy, float[] cz) {
         float ar = ca.r;
         float ai = ca.i;
         int n1 = cy.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            float yr = cy[ir];
            float yi = cy[ii];
            cz[ir] = ar * yr - ai * yi;
            cz[ii] = ar * yi + ai * yr;
            ir += 2;
         }
      }

      @Override
      void apply(float[] cx, Cfloat cb, float[] cz) {
         float br = cb.r;
         float bi = cb.i;
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            float xr = cx[ir];
            float xi = cx[ii];
            cz[ir] = xr * br - xi * bi;
            cz[ii] = xr * bi + xi * br;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy, double[] cz) {
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            double xr = cx[ir];
            double xi = cx[ii];
            double yr = cy[ir];
            double yi = cy[ii];
            cz[ir] = xr * yr - xi * yi;
            cz[ii] = xr * yi + xi * yr;
            ir += 2;
         }
      }

      @Override
      void apply(Cdouble ca, double[] cy, double[] cz) {
         double ar = ca.r;
         double ai = ca.i;
         int n1 = cy.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            double yr = cy[ir];
            double yi = cy[ii];
            cz[ir] = ar * yr - ai * yi;
            cz[ii] = ar * yi + ai * yr;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, Cdouble cb, double[] cz) {
         double br = cb.r;
         double bi = cb.i;
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            double xr = cx[ir];
            double xi = cx[ii];
            cz[ir] = xr * br - xi * bi;
            cz[ii] = xr * bi + xi * br;
            ir += 2;
         }
      }
   };
   private static ArrayMath.CBinary _cdiv = new ArrayMath.CBinary() {
      @Override
      void apply(float[] cx, float[] cy, float[] cz) {
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            float xr = cx[ir];
            float xi = cx[ii];
            float yr = cy[ir];
            float yi = cy[ii];
            float yd = yr * yr + yi * yi;
            cz[ir] = (xr * yr + xi * yi) / yd;
            cz[ii] = (xi * yr - xr * yi) / yd;
            ir += 2;
         }
      }

      @Override
      void apply(Cfloat ca, float[] cy, float[] cz) {
         float ar = ca.r;
         float ai = ca.i;
         int n1 = cy.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            float yr = cy[ir];
            float yi = cy[ii];
            float yd = yr * yr + yi * yi;
            cz[ir] = (ar * yr + ai * yi) / yd;
            cz[ii] = (ai * yr - ar * yi) / yd;
            ir += 2;
         }
      }

      @Override
      void apply(float[] cx, Cfloat cb, float[] cz) {
         float br = cb.r;
         float bi = cb.i;
         float bd = br * br + bi * bi;
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            float xr = cx[ir];
            float xi = cx[ii];
            cz[ir] = (xr * br + xi * bi) / bd;
            cz[ii] = (xi * br - xr * bi) / bd;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy, double[] cz) {
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            double xr = cx[ir];
            double xi = cx[ii];
            double yr = cy[ir];
            double yi = cy[ii];
            double yd = yr * yr + yi * yi;
            cz[ir] = (xr * yr + xi * yi) / yd;
            cz[ii] = (xi * yr - xr * yi) / yd;
            ir += 2;
         }
      }

      @Override
      void apply(Cdouble ca, double[] cy, double[] cz) {
         double ar = ca.r;
         double ai = ca.i;
         int n1 = cy.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            double yr = cy[ir];
            double yi = cy[ii];
            double yd = yr * yr + yi * yi;
            cz[ir] = (ar * yr + ai * yi) / yd;
            cz[ii] = (ai * yr - ar * yi) / yd;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, Cdouble cb, double[] cz) {
         double br = cb.r;
         double bi = cb.i;
         double bd = br * br + bi * bi;
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            double xr = cx[ir];
            double xi = cx[ii];
            cz[ir] = (xr * br + xi * bi) / bd;
            cz[ii] = (xi * br - xr * bi) / bd;
            ir += 2;
         }
      }
   };
   private static ArrayMath.ComplexToComplex _cneg = new ArrayMath.ComplexToComplex() {
      @Override
      void apply(float[] cx, float[] cy) {
         int n1 = cx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            cy[i1] = -cx[i1];
         }
      }

      @Override
      void apply(double[] cx, double[] cy) {
         int n1 = cx.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            cy[i1] = -cx[i1];
         }
      }
   };
   private static ArrayMath.ComplexToComplex _cconj = new ArrayMath.ComplexToComplex() {
      @Override
      void apply(float[] cx, float[] cy) {
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cy[ir] = cx[ir];
            cy[ii] = -cx[ii];
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy) {
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            cy[ir] = cx[ir];
            cy[ii] = -cx[ii];
            ir += 2;
         }
      }
   };
   private static ArrayMath.ComplexToComplex _ccos = new ArrayMath.ComplexToComplex() {
      @Override
      void apply(float[] cx, float[] cy) {
         Cfloat ct = new Cfloat();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cfloat ce = Cfloat.cos(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy) {
         Cdouble ct = new Cdouble();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cdouble ce = Cdouble.cos(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }
   };
   private static ArrayMath.ComplexToComplex _csin = new ArrayMath.ComplexToComplex() {
      @Override
      void apply(float[] cx, float[] cy) {
         Cfloat ct = new Cfloat();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cfloat ce = Cfloat.sin(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy) {
         Cdouble ct = new Cdouble();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cdouble ce = Cdouble.sin(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }
   };
   private static ArrayMath.ComplexToComplex _csqrt = new ArrayMath.ComplexToComplex() {
      @Override
      void apply(float[] cx, float[] cy) {
         Cfloat ct = new Cfloat();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cfloat ce = Cfloat.sqrt(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy) {
         Cdouble ct = new Cdouble();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cdouble ce = Cdouble.sqrt(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }
   };
   private static ArrayMath.ComplexToComplex _cexp = new ArrayMath.ComplexToComplex() {
      @Override
      void apply(float[] cx, float[] cy) {
         Cfloat ct = new Cfloat();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cfloat ce = Cfloat.exp(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy) {
         Cdouble ct = new Cdouble();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cdouble ce = Cdouble.exp(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }
   };
   private static ArrayMath.ComplexToComplex _clog = new ArrayMath.ComplexToComplex() {
      @Override
      void apply(float[] cx, float[] cy) {
         Cfloat ct = new Cfloat();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cfloat ce = Cfloat.log(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy) {
         Cdouble ct = new Cdouble();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cdouble ce = Cdouble.log(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }
   };
   private static ArrayMath.ComplexToComplex _clog10 = new ArrayMath.ComplexToComplex() {
      @Override
      void apply(float[] cx, float[] cy) {
         Cfloat ct = new Cfloat();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cfloat ce = Cfloat.log10(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] cy) {
         Cdouble ct = new Cdouble();
         int n1 = cx.length / 2;
         int ir = 0;
         int ii = 1;

         for(int nn = 2 * n1; ir < nn; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            Cdouble ce = Cdouble.log10(ct);
            cy[ir] = ce.r;
            cy[ii] = ce.i;
            ir += 2;
         }
      }
   };
   private static ArrayMath.ComplexToReal _creal = new ArrayMath.ComplexToReal() {
      @Override
      void apply(float[] cx, float[] ry) {
         int n1 = cx.length / 2;
         int i1 = 0;

         for(int ir = 0; i1 < n1; ir += 2) {
            ry[i1] = cx[ir];
            ++i1;
         }
      }

      @Override
      void apply(double[] cx, double[] ry) {
         int n1 = cx.length / 2;
         int i1 = 0;

         for(int ir = 0; i1 < n1; ir += 2) {
            ry[i1] = cx[ir];
            ++i1;
         }
      }
   };
   private static ArrayMath.ComplexToReal _cimag = new ArrayMath.ComplexToReal() {
      @Override
      void apply(float[] cx, float[] ry) {
         int n1 = cx.length / 2;
         int i1 = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            ry[i1] = cx[ii];
            ++i1;
         }
      }

      @Override
      void apply(double[] cx, double[] ry) {
         int n1 = cx.length / 2;
         int i1 = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            ry[i1] = cx[ii];
            ++i1;
         }
      }
   };
   private static ArrayMath.ComplexToReal _cabs = new ArrayMath.ComplexToReal() {
      @Override
      void apply(float[] cx, float[] ry) {
         Cfloat ct = new Cfloat();
         int n1 = cx.length / 2;
         int i1 = 0;
         int ir = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            ry[i1] = Cfloat.abs(ct);
            ++i1;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] ry) {
         Cdouble ct = new Cdouble();
         int n1 = cx.length / 2;
         int i1 = 0;
         int ir = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            ry[i1] = Cdouble.abs(ct);
            ++i1;
            ir += 2;
         }
      }
   };
   private static ArrayMath.ComplexToReal _carg = new ArrayMath.ComplexToReal() {
      @Override
      void apply(float[] cx, float[] ry) {
         Cfloat ct = new Cfloat();
         int n1 = cx.length / 2;
         int i1 = 0;
         int ir = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            ry[i1] = Cfloat.arg(ct);
            ++i1;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] ry) {
         Cdouble ct = new Cdouble();
         int n1 = cx.length / 2;
         int i1 = 0;
         int ir = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            ct.r = cx[ir];
            ct.i = cx[ii];
            ry[i1] = Cdouble.arg(ct);
            ++i1;
            ir += 2;
         }
      }
   };
   private static ArrayMath.ComplexToReal _cnorm = new ArrayMath.ComplexToReal() {
      @Override
      void apply(float[] cx, float[] ry) {
         int n1 = cx.length / 2;
         int i1 = 0;
         int ir = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            float cr = cx[ir];
            float ci = cx[ii];
            ry[i1] = cr * cr + ci * ci;
            ++i1;
            ir += 2;
         }
      }

      @Override
      void apply(double[] cx, double[] ry) {
         int n1 = cx.length / 2;
         int i1 = 0;
         int ir = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            double cr = cx[ir];
            double ci = cx[ii];
            ry[i1] = cr * cr + ci * ci;
            ++i1;
            ir += 2;
         }
      }
   };
   private static ArrayMath.RealToComplex _cmplx = new ArrayMath.RealToComplex() {
      @Override
      void apply(float[] rx, float[] ry, float[] cz) {
         int n1 = rx.length;
         int i1 = 0;
         int ir = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            cz[ir] = rx[i1];
            cz[ii] = ry[i1];
            ++i1;
            ir += 2;
         }
      }

      @Override
      void apply(double[] rx, double[] ry, double[] cz) {
         int n1 = rx.length;
         int i1 = 0;
         int ir = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            cz[ir] = rx[i1];
            cz[ii] = ry[i1];
            ++i1;
            ir += 2;
         }
      }
   };
   private static ArrayMath.RealToComplex _polar = new ArrayMath.RealToComplex() {
      @Override
      void apply(float[] rx, float[] ry, float[] cz) {
         int n1 = rx.length;
         int i1 = 0;
         int ir = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            float r = rx[i1];
            float a = ry[i1];
            cz[ir] = r * (float)Math.cos((double)a);
            cz[ii] = r * (float)Math.sin((double)a);
            ++i1;
            ir += 2;
         }
      }

      @Override
      void apply(double[] rx, double[] ry, double[] cz) {
         int n1 = rx.length;
         int i1 = 0;
         int ir = 0;

         for(int ii = 1; i1 < n1; ii += 2) {
            double r = rx[i1];
            double a = ry[i1];
            cz[ir] = r * Math.cos(a);
            cz[ii] = r * Math.sin(a);
            ++i1;
            ir += 2;
         }
      }
   };

   public static float sin(float x) {
      return (float)Math.sin((double)x);
   }

   public static double sin(double x) {
      return Math.sin(x);
   }

   public static float cos(float x) {
      return (float)Math.cos((double)x);
   }

   public static double cos(double x) {
      return Math.cos(x);
   }

   public static float tan(float x) {
      return (float)Math.tan((double)x);
   }

   public static double tan(double x) {
      return Math.tan(x);
   }

   public static float asin(float x) {
      return (float)Math.asin((double)x);
   }

   public static double asin(double x) {
      return Math.asin(x);
   }

   public static float acos(float x) {
      return (float)Math.acos((double)x);
   }

   public static double acos(double x) {
      return Math.acos(x);
   }

   public static float atan(float x) {
      return (float)Math.atan((double)x);
   }

   public static double atan(double x) {
      return Math.atan(x);
   }

   public static float atan2(float y, float x) {
      return (float)Math.atan2((double)y, (double)x);
   }

   public static double atan2(double y, double x) {
      return Math.atan2(y, x);
   }

   public static float toRadians(float angdeg) {
      return (float)Math.toRadians((double)angdeg);
   }

   public static double toRadians(double angdeg) {
      return Math.toRadians(angdeg);
   }

   public static float toDegrees(float angrad) {
      return (float)Math.toDegrees((double)angrad);
   }

   public static double toDegrees(double angrad) {
      return Math.toDegrees(angrad);
   }

   public static float exp(float x) {
      return (float)Math.exp((double)x);
   }

   public static double exp(double x) {
      return Math.exp(x);
   }

   public static float log(float x) {
      return (float)Math.log((double)x);
   }

   public static double log(double x) {
      return Math.log(x);
   }

   public static float log10(float x) {
      return (float)Math.log10((double)x);
   }

   public static double log10(double x) {
      return Math.log10(x);
   }

   public static float sqrt(float x) {
      return (float)Math.sqrt((double)x);
   }

   public static double sqrt(double x) {
      return Math.sqrt(x);
   }

   public static float pow(float x, float y) {
      return (float)Math.pow((double)x, (double)y);
   }

   public static double pow(double x, double y) {
      return Math.pow(x, y);
   }

   public static float sinh(float x) {
      return (float)Math.sinh((double)x);
   }

   public static double sinh(double x) {
      return 0.5 * (Math.exp(x) - Math.exp(-x));
   }

   public static float cosh(float x) {
      return (float)Math.cosh((double)x);
   }

   public static double cosh(double x) {
      return 0.5 * (Math.exp(x) + Math.exp(-x));
   }

   public static float tanh(float x) {
      return (float)tanh((double)x);
   }

   public static double tanh(double x) {
      double ep = Math.exp(x);
      double em = Math.exp(-x);
      return (ep - em) / (ep + em);
   }

   public static float ceil(float x) {
      return (float)Math.ceil((double)x);
   }

   public static double ceil(double x) {
      return Math.ceil(x);
   }

   public static float floor(float x) {
      return (float)Math.floor((double)x);
   }

   public static double floor(double x) {
      return Math.floor(x);
   }

   public static float rint(float x) {
      return (float)Math.rint((double)x);
   }

   public static double rint(double x) {
      return Math.rint(x);
   }

   public static int round(float x) {
      return Math.round(x);
   }

   public static long round(double x) {
      return Math.round(x);
   }

   public static float signum(float x) {
      return x > 0.0F ? 1.0F : (x < 0.0F ? -1.0F : 0.0F);
   }

   public static double signum(double x) {
      return x > 0.0 ? 1.0 : (x < 0.0 ? -1.0 : 0.0);
   }

   public static int abs(int x) {
      return x > 0 ? x : -x;
   }

   public static long abs(long x) {
      return x > 0L ? x : -x;
   }

   public static float abs(float x) {
      return x > 0.0F ? x : -x;
   }

   public static double abs(double x) {
      return x >= 0.0 ? x : -x;
   }

   public static int max(int a, int b) {
      return a >= b ? a : b;
   }

   public static int max(int a, int b, int c) {
      return max(a, max(b, c));
   }

   public static int max(int a, int b, int c, int d) {
      return max(a, max(b, max(c, d)));
   }

   public static long max(long a, long b) {
      return a >= b ? a : b;
   }

   public static long max(long a, long b, long c) {
      return max(a, max(b, c));
   }

   public static long max(long a, long b, long c, long d) {
      return max(a, max(b, max(c, d)));
   }

   public static float max(float a, float b) {
      return a >= b ? a : b;
   }

   public static float max(float a, float b, float c) {
      return max(a, max(b, c));
   }

   public static float max(float a, float b, float c, float d) {
      return max(a, max(b, max(c, d)));
   }

   public static double max(double a, double b) {
      return a >= b ? a : b;
   }

   public static double max(double a, double b, double c) {
      return max(a, max(b, c));
   }

   public static double max(double a, double b, double c, double d) {
      return max(a, max(b, max(c, d)));
   }

   public static int min(int a, int b) {
      return a <= b ? a : b;
   }

   public static int min(int a, int b, int c) {
      return min(a, min(b, c));
   }

   public static int min(int a, int b, int c, int d) {
      return min(a, min(b, min(c, d)));
   }

   public static long min(long a, long b) {
      return a <= b ? a : b;
   }

   public static long min(long a, long b, long c) {
      return min(a, min(b, c));
   }

   public static long min(long a, long b, long c, long d) {
      return min(a, min(b, min(c, d)));
   }

   public static float min(float a, float b) {
      return a <= b ? a : b;
   }

   public static float min(float a, float b, float c) {
      return min(a, min(b, c));
   }

   public static float min(float a, float b, float c, float d) {
      return min(a, min(b, min(c, d)));
   }

   public static double min(double a, double b) {
      return a <= b ? a : b;
   }

   public static double min(double a, double b, double c) {
      return min(a, min(b, c));
   }

   public static double min(double a, double b, double c, double d) {
      return min(a, min(b, min(c, d)));
   }

   public static double cbrt(double a) {
      return Math.cbrt(a);
   }

   public static float cbrt(float a) {
      return (float)Math.cbrt((double)a);
   }

   public static double IEEEremainder(double f1, double f2) {
      return Math.IEEEremainder(f1, f2);
   }

   public static float IEEEremainder(float f1, float f2) {
      return (float)Math.IEEEremainder((double)f1, (double)f2);
   }

   public static double random() {
      return Math.random();
   }

   public static double randomDouble() {
      return Math.random();
   }

   public static float randomFloat() {
      return (float)Math.random();
   }

   public static double ulp(double d) {
      return Math.ulp(d);
   }

   public static float ulp(float d) {
      return Math.ulp(d);
   }

   public static double hypot(double x, double y) {
      return Math.hypot(x, y);
   }

   public static float hypot(float x, float y) {
      return (float)Math.hypot((double)x, (double)y);
   }

   public static double expm1(double x) {
      return Math.expm1(x);
   }

   public static float expm1(float x) {
      return (float)Math.expm1((double)x);
   }

   public static double log1p(double x) {
      return Math.log1p(x);
   }

   public static float log1p(float x) {
      return (float)Math.log1p((double)x);
   }

   public static byte[] zerobyte(int n1) {
      return new byte[n1];
   }

   public static byte[][] zerobyte(int n1, int n2) {
      return new byte[n2][n1];
   }

   public static byte[][][] zerobyte(int n1, int n2, int n3) {
      return new byte[n3][n2][n1];
   }

   public static void zero(byte[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = 0;
      }
   }

   public static void zero(byte[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         zero(rx[i2]);
      }
   }

   public static void zero(byte[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         zero(rx[i3]);
      }
   }

   public static short[] zeroshort(int n1) {
      return new short[n1];
   }

   public static short[][] zeroshort(int n1, int n2) {
      return new short[n2][n1];
   }

   public static short[][][] zeroshort(int n1, int n2, int n3) {
      return new short[n3][n2][n1];
   }

   public static void zero(short[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = 0;
      }
   }

   public static void zero(short[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         zero(rx[i2]);
      }
   }

   public static void zero(short[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         zero(rx[i3]);
      }
   }

   public static int[] zeroint(int n1) {
      return new int[n1];
   }

   public static int[][] zeroint(int n1, int n2) {
      return new int[n2][n1];
   }

   public static int[][][] zeroint(int n1, int n2, int n3) {
      return new int[n3][n2][n1];
   }

   public static void zero(int[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = 0;
      }
   }

   public static void zero(int[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         zero(rx[i2]);
      }
   }

   public static void zero(int[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         zero(rx[i3]);
      }
   }

   public static long[] zerolong(int n1) {
      return new long[n1];
   }

   public static long[][] zerolong(int n1, int n2) {
      return new long[n2][n1];
   }

   public static long[][][] zerolong(int n1, int n2, int n3) {
      return new long[n3][n2][n1];
   }

   public static void zero(long[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = 0L;
      }
   }

   public static void zero(long[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         zero(rx[i2]);
      }
   }

   public static void zero(long[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         zero(rx[i3]);
      }
   }

   public static float[] zerofloat(int n1) {
      return new float[n1];
   }

   public static float[][] zerofloat(int n1, int n2) {
      return new float[n2][n1];
   }

   public static float[][][] zerofloat(int n1, int n2, int n3) {
      return new float[n3][n2][n1];
   }

   public static void zero(float[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = 0.0F;
      }
   }

   public static void zero(float[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         zero(rx[i2]);
      }
   }

   public static void zero(float[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         zero(rx[i3]);
      }
   }

   public static float[] czerofloat(int n1) {
      return new float[2 * n1];
   }

   public static float[][] czerofloat(int n1, int n2) {
      return new float[n2][2 * n1];
   }

   public static float[][][] czerofloat(int n1, int n2, int n3) {
      return new float[n3][n2][2 * n1];
   }

   public static void czero(float[] cx) {
      zero(cx);
   }

   public static void czero(float[][] cx) {
      zero(cx);
   }

   public static void czero(float[][][] cx) {
      zero(cx);
   }

   public static double[] zerodouble(int n1) {
      return new double[n1];
   }

   public static double[][] zerodouble(int n1, int n2) {
      return new double[n2][n1];
   }

   public static double[][][] zerodouble(int n1, int n2, int n3) {
      return new double[n3][n2][n1];
   }

   public static void zero(double[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = 0.0;
      }
   }

   public static void zero(double[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         zero(rx[i2]);
      }
   }

   public static void zero(double[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         zero(rx[i3]);
      }
   }

   public static double[] czerodouble(int n1) {
      return new double[2 * n1];
   }

   public static double[][] czerodouble(int n1, int n2) {
      return new double[n2][2 * n1];
   }

   public static double[][][] czerodouble(int n1, int n2, int n3) {
      return new double[n3][n2][2 * n1];
   }

   public static void czero(double[] cx) {
      zero(cx);
   }

   public static void czero(double[][] cx) {
      zero(cx);
   }

   public static void czero(double[][][] cx) {
      zero(cx);
   }

   public static int[] randint(int n1) {
      return randint(_random, n1);
   }

   public static int[][] randint(int n1, int n2) {
      return randint(_random, n1, n2);
   }

   public static int[][][] randint(int n1, int n2, int n3) {
      return randint(_random, n1, n2, n3);
   }

   public static int[] randint(Random random, int n1) {
      int[] rx = new int[n1];
      rand(random, rx);
      return rx;
   }

   public static int[][] randint(Random random, int n1, int n2) {
      int[][] rx = new int[n2][n1];
      rand(random, rx);
      return rx;
   }

   public static int[][][] randint(Random random, int n1, int n2, int n3) {
      int[][][] rx = new int[n3][n2][n1];
      rand(random, rx);
      return rx;
   }

   public static void rand(int[] rx) {
      rand(_random, rx);
   }

   public static void rand(int[][] rx) {
      rand(_random, rx);
   }

   public static void rand(int[][][] rx) {
      rand(_random, rx);
   }

   public static void rand(Random random, int[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = random.nextInt();
      }
   }

   public static void rand(Random random, int[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         rand(random, rx[i2]);
      }
   }

   public static void rand(Random random, int[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         rand(random, rx[i3]);
      }
   }

   public static long[] randlong(int n1) {
      return randlong(_random, n1);
   }

   public static long[][] randlong(int n1, int n2) {
      return randlong(_random, n1, n2);
   }

   public static long[][][] randlong(int n1, int n2, int n3) {
      return randlong(_random, n1, n2, n3);
   }

   public static long[] randlong(Random random, int n1) {
      long[] rx = new long[n1];
      rand(random, rx);
      return rx;
   }

   public static long[][] randlong(Random random, int n1, int n2) {
      long[][] rx = new long[n2][n1];
      rand(random, rx);
      return rx;
   }

   public static long[][][] randlong(Random random, int n1, int n2, int n3) {
      long[][][] rx = new long[n3][n2][n1];
      rand(random, rx);
      return rx;
   }

   public static void rand(long[] rx) {
      rand(_random, rx);
   }

   public static void rand(long[][] rx) {
      rand(_random, rx);
   }

   public static void rand(long[][][] rx) {
      rand(_random, rx);
   }

   public static void rand(Random random, long[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = random.nextLong();
      }
   }

   public static void rand(Random random, long[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         rand(random, rx[i2]);
      }
   }

   public static void rand(Random random, long[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         rand(random, rx[i3]);
      }
   }

   public static float[] randfloat(int n1) {
      return randfloat(_random, n1);
   }

   public static float[][] randfloat(int n1, int n2) {
      return randfloat(_random, n1, n2);
   }

   public static float[][][] randfloat(int n1, int n2, int n3) {
      return randfloat(_random, n1, n2, n3);
   }

   public static float[] randfloat(Random random, int n1) {
      float[] rx = new float[n1];
      rand(random, rx);
      return rx;
   }

   public static float[][] randfloat(Random random, int n1, int n2) {
      float[][] rx = new float[n2][n1];
      rand(random, rx);
      return rx;
   }

   public static float[][][] randfloat(Random random, int n1, int n2, int n3) {
      float[][][] rx = new float[n3][n2][n1];
      rand(random, rx);
      return rx;
   }

   public static void rand(float[] rx) {
      rand(_random, rx);
   }

   public static void rand(float[][] rx) {
      rand(_random, rx);
   }

   public static void rand(float[][][] rx) {
      rand(_random, rx);
   }

   public static void rand(Random random, float[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = random.nextFloat();
      }
   }

   public static void rand(Random random, float[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         rand(random, rx[i2]);
      }
   }

   public static void rand(Random random, float[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         rand(random, rx[i3]);
      }
   }

   public static float[] crandfloat(int n1) {
      return crandfloat(_random, n1);
   }

   public static float[][] crandfloat(int n1, int n2) {
      return crandfloat(_random, n1, n2);
   }

   public static float[][][] crandfloat(int n1, int n2, int n3) {
      return crandfloat(_random, n1, n2, n3);
   }

   public static float[] crandfloat(Random random, int n1) {
      float[] cx = new float[2 * n1];
      crand(random, cx);
      return cx;
   }

   public static float[][] crandfloat(Random random, int n1, int n2) {
      float[][] cx = new float[n2][2 * n1];
      crand(random, cx);
      return cx;
   }

   public static float[][][] crandfloat(Random random, int n1, int n2, int n3) {
      float[][][] cx = new float[n3][n2][2 * n1];
      crand(random, cx);
      return cx;
   }

   public static void crand(float[] cx) {
      crand(_random, cx);
   }

   public static void crand(float[][] cx) {
      crand(_random, cx);
   }

   public static void crand(float[][][] cx) {
      crand(_random, cx);
   }

   public static void crand(Random random, float[] cx) {
      rand(random, cx);
   }

   public static void crand(Random random, float[][] cx) {
      rand(random, cx);
   }

   public static void crand(Random random, float[][][] cx) {
      rand(random, cx);
   }

   public static double[] randdouble(int n1) {
      return randdouble(_random, n1);
   }

   public static double[][] randdouble(int n1, int n2) {
      return randdouble(_random, n1, n2);
   }

   public static double[][][] randdouble(int n1, int n2, int n3) {
      return randdouble(_random, n1, n2, n3);
   }

   public static double[] randdouble(Random random, int n1) {
      double[] rx = new double[n1];
      rand(random, rx);
      return rx;
   }

   public static double[][] randdouble(Random random, int n1, int n2) {
      double[][] rx = new double[n2][n1];
      rand(random, rx);
      return rx;
   }

   public static double[][][] randdouble(Random random, int n1, int n2, int n3) {
      double[][][] rx = new double[n3][n2][n1];
      rand(random, rx);
      return rx;
   }

   public static void rand(double[] rx) {
      rand(_random, rx);
   }

   public static void rand(double[][] rx) {
      rand(_random, rx);
   }

   public static void rand(double[][][] rx) {
      rand(_random, rx);
   }

   public static void rand(Random random, double[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = random.nextDouble();
      }
   }

   public static void rand(Random random, double[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         rand(random, rx[i2]);
      }
   }

   public static void rand(Random random, double[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         rand(random, rx[i3]);
      }
   }

   public static double[] cranddouble(int n1) {
      return cranddouble(_random, n1);
   }

   public static double[][] cranddouble(int n1, int n2) {
      return cranddouble(_random, n1, n2);
   }

   public static double[][][] cranddouble(int n1, int n2, int n3) {
      return cranddouble(_random, n1, n2, n3);
   }

   public static double[] cranddouble(Random random, int n1) {
      double[] cx = new double[2 * n1];
      crand(random, cx);
      return cx;
   }

   public static double[][] cranddouble(Random random, int n1, int n2) {
      double[][] cx = new double[n2][2 * n1];
      crand(random, cx);
      return cx;
   }

   public static double[][][] cranddouble(Random random, int n1, int n2, int n3) {
      double[][][] cx = new double[n3][n2][2 * n1];
      crand(random, cx);
      return cx;
   }

   public static void crand(double[] cx) {
      crand(_random, cx);
   }

   public static void crand(double[][] cx) {
      crand(_random, cx);
   }

   public static void crand(double[][][] cx) {
      crand(_random, cx);
   }

   public static void crand(Random random, double[] cx) {
      rand(random, cx);
   }

   public static void crand(Random random, double[][] cx) {
      rand(random, cx);
   }

   public static void crand(Random random, double[][][] cx) {
      rand(random, cx);
   }

   public static byte[] fillbyte(byte ra, int n1) {
      byte[] rx = new byte[n1];
      fill(ra, rx);
      return rx;
   }

   public static byte[][] fillbyte(byte ra, int n1, int n2) {
      byte[][] rx = new byte[n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static byte[][][] fillbyte(byte ra, int n1, int n2, int n3) {
      byte[][][] rx = new byte[n3][n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static void fill(byte ra, byte[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = ra;
      }
   }

   public static void fill(byte ra, byte[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         fill(ra, rx[i2]);
      }
   }

   public static void fill(byte ra, byte[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         fill(ra, rx[i3]);
      }
   }

   public static short[] fillshort(short ra, int n1) {
      short[] rx = new short[n1];
      fill(ra, rx);
      return rx;
   }

   public static short[][] fillshort(short ra, int n1, int n2) {
      short[][] rx = new short[n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static short[][][] fillshort(short ra, int n1, int n2, int n3) {
      short[][][] rx = new short[n3][n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static void fill(short ra, short[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = ra;
      }
   }

   public static void fill(short ra, short[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         fill(ra, rx[i2]);
      }
   }

   public static void fill(short ra, short[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         fill(ra, rx[i3]);
      }
   }

   public static int[] fillint(int ra, int n1) {
      int[] rx = new int[n1];
      fill(ra, rx);
      return rx;
   }

   public static int[][] fillint(int ra, int n1, int n2) {
      int[][] rx = new int[n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static int[][][] fillint(int ra, int n1, int n2, int n3) {
      int[][][] rx = new int[n3][n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static void fill(int ra, int[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = ra;
      }
   }

   public static void fill(int ra, int[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         fill(ra, rx[i2]);
      }
   }

   public static void fill(int ra, int[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         fill(ra, rx[i3]);
      }
   }

   public static long[] filllong(long ra, int n1) {
      long[] rx = new long[n1];
      fill(ra, rx);
      return rx;
   }

   public static long[][] filllong(long ra, int n1, int n2) {
      long[][] rx = new long[n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static long[][][] filllong(long ra, int n1, int n2, int n3) {
      long[][][] rx = new long[n3][n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static void fill(long ra, long[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = ra;
      }
   }

   public static void fill(long ra, long[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         fill(ra, rx[i2]);
      }
   }

   public static void fill(long ra, long[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         fill(ra, rx[i3]);
      }
   }

   public static float[] fillfloat(float ra, int n1) {
      float[] rx = new float[n1];
      fill(ra, rx);
      return rx;
   }

   public static float[][] fillfloat(float ra, int n1, int n2) {
      float[][] rx = new float[n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static float[][][] fillfloat(float ra, int n1, int n2, int n3) {
      float[][][] rx = new float[n3][n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static void fill(float ra, float[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = ra;
      }
   }

   public static void fill(float ra, float[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         fill(ra, rx[i2]);
      }
   }

   public static void fill(float ra, float[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         fill(ra, rx[i3]);
      }
   }

   public static float[] cfillfloat(Cfloat ca, int n1) {
      float[] cx = new float[2 * n1];
      cfill(ca, cx);
      return cx;
   }

   public static float[][] cfillfloat(Cfloat ca, int n1, int n2) {
      float[][] cx = new float[n2][2 * n1];
      cfill(ca, cx);
      return cx;
   }

   public static float[][][] cfillfloat(Cfloat ca, int n1, int n2, int n3) {
      float[][][] cx = new float[n3][n2][2 * n1];
      cfill(ca, cx);
      return cx;
   }

   public static void cfill(Cfloat ca, float[] cx) {
      int n1 = cx.length / 2;
      float ar = ca.r;
      float ai = ca.i;
      int ir = 0;
      int ii = 1;

      for(int nn = 2 * n1; ir < nn; ii += 2) {
         cx[ir] = ar;
         cx[ii] = ai;
         ir += 2;
      }
   }

   public static void cfill(Cfloat ca, float[][] cx) {
      int n2 = cx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         cfill(ca, cx[i2]);
      }
   }

   public static void cfill(Cfloat ca, float[][][] cx) {
      int n3 = cx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         cfill(ca, cx[i3]);
      }
   }

   public static double[] filldouble(double ra, int n1) {
      double[] rx = new double[n1];
      fill(ra, rx);
      return rx;
   }

   public static double[][] filldouble(double ra, int n1, int n2) {
      double[][] rx = new double[n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static double[][][] filldouble(double ra, int n1, int n2, int n3) {
      double[][][] rx = new double[n3][n2][n1];
      fill(ra, rx);
      return rx;
   }

   public static void fill(double ra, double[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = ra;
      }
   }

   public static void fill(double ra, double[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         fill(ra, rx[i2]);
      }
   }

   public static void fill(double ra, double[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         fill(ra, rx[i3]);
      }
   }

   public static double[] cfilldouble(Cdouble ca, int n1) {
      double[] cx = new double[2 * n1];
      cfill(ca, cx);
      return cx;
   }

   public static double[][] cfilldouble(Cdouble ca, int n1, int n2) {
      double[][] cx = new double[n2][2 * n1];
      cfill(ca, cx);
      return cx;
   }

   public static double[][][] cfilldouble(Cdouble ca, int n1, int n2, int n3) {
      double[][][] cx = new double[n3][n2][2 * n1];
      cfill(ca, cx);
      return cx;
   }

   public static void cfill(Cdouble ca, double[] cx) {
      int n1 = cx.length / 2;
      double ar = ca.r;
      double ai = ca.i;
      int ir = 0;
      int ii = 1;

      for(int nn = 2 * n1; ir < nn; ii += 2) {
         cx[ir] = ar;
         cx[ii] = ai;
         ir += 2;
      }
   }

   public static void cfill(Cdouble ca, double[][] cx) {
      int n2 = cx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         cfill(ca, cx[i2]);
      }
   }

   public static void cfill(Cdouble ca, double[][][] cx) {
      int n3 = cx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         cfill(ca, cx[i3]);
      }
   }

   public static byte[] rampbyte(byte ra, byte rb1, int n1) {
      byte[] rx = new byte[n1];
      ramp(ra, rb1, rx);
      return rx;
   }

   public static byte[][] rampbyte(byte ra, byte rb1, byte rb2, int n1, int n2) {
      byte[][] rx = new byte[n2][n1];
      ramp(ra, rb1, rb2, rx);
      return rx;
   }

   public static byte[][][] rampbyte(byte ra, byte rb1, byte rb2, byte rb3, int n1, int n2, int n3) {
      byte[][][] rx = new byte[n3][n2][n1];
      ramp(ra, rb1, rb2, rb3, rx);
      return rx;
   }

   public static void ramp(byte ra, byte rb1, byte[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = (byte)(ra + rb1 * i1);
      }
   }

   public static void ramp(byte ra, byte rb1, byte rb2, byte[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         ramp((byte)(ra + rb2 * i2), rb1, rx[i2]);
      }
   }

   public static void ramp(byte ra, byte rb1, byte rb2, byte rb3, byte[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         ramp((byte)(ra + rb3 * i3), rb1, rb2, rx[i3]);
      }
   }

   public static short[] rampshort(short ra, short rb1, int n1) {
      short[] rx = new short[n1];
      ramp(ra, rb1, rx);
      return rx;
   }

   public static short[][] rampshort(short ra, short rb1, short rb2, int n1, int n2) {
      short[][] rx = new short[n2][n1];
      ramp(ra, rb1, rb2, rx);
      return rx;
   }

   public static short[][][] rampshort(short ra, short rb1, short rb2, short rb3, int n1, int n2, int n3) {
      short[][][] rx = new short[n3][n2][n1];
      ramp(ra, rb1, rb2, rb3, rx);
      return rx;
   }

   public static void ramp(short ra, short rb1, short[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = (short)(ra + rb1 * i1);
      }
   }

   public static void ramp(short ra, short rb1, short rb2, short[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         ramp((short)(ra + rb2 * i2), rb1, rx[i2]);
      }
   }

   public static void ramp(short ra, short rb1, short rb2, short rb3, short[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         ramp((short)(ra + rb3 * i3), rb1, rb2, rx[i3]);
      }
   }

   public static int[] rampint(int ra, int rb1, int n1) {
      int[] rx = new int[n1];
      ramp(ra, rb1, rx);
      return rx;
   }

   public static int[][] rampint(int ra, int rb1, int rb2, int n1, int n2) {
      int[][] rx = new int[n2][n1];
      ramp(ra, rb1, rb2, rx);
      return rx;
   }

   public static int[][][] rampint(int ra, int rb1, int rb2, int rb3, int n1, int n2, int n3) {
      int[][][] rx = new int[n3][n2][n1];
      ramp(ra, rb1, rb2, rb3, rx);
      return rx;
   }

   public static void ramp(int ra, int rb1, int[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = ra + rb1 * i1;
      }
   }

   public static void ramp(int ra, int rb1, int rb2, int[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         ramp(ra + rb2 * i2, rb1, rx[i2]);
      }
   }

   public static void ramp(int ra, int rb1, int rb2, int rb3, int[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         ramp(ra + rb3 * i3, rb1, rb2, rx[i3]);
      }
   }

   public static long[] ramplong(long ra, long rb1, int n1) {
      long[] rx = new long[n1];
      ramp(ra, rb1, rx);
      return rx;
   }

   public static long[][] ramplong(long ra, long rb1, long rb2, int n1, int n2) {
      long[][] rx = new long[n2][n1];
      ramp(ra, rb1, rb2, rx);
      return rx;
   }

   public static long[][][] ramplong(long ra, long rb1, long rb2, long rb3, int n1, int n2, int n3) {
      long[][][] rx = new long[n3][n2][n1];
      ramp(ra, rb1, rb2, rb3, rx);
      return rx;
   }

   public static void ramp(long ra, long rb1, long[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = ra + rb1 * (long)i1;
      }
   }

   public static void ramp(long ra, long rb1, long rb2, long[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         ramp(ra + rb2 * (long)i2, rb1, rx[i2]);
      }
   }

   public static void ramp(long ra, long rb1, long rb2, long rb3, long[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         ramp(ra + rb3 * (long)i3, rb1, rb2, rx[i3]);
      }
   }

   public static float[] rampfloat(float ra, float rb1, int n1) {
      float[] rx = new float[n1];
      ramp(ra, rb1, rx);
      return rx;
   }

   public static float[][] rampfloat(float ra, float rb1, float rb2, int n1, int n2) {
      float[][] rx = new float[n2][n1];
      ramp(ra, rb1, rb2, rx);
      return rx;
   }

   public static float[][][] rampfloat(float ra, float rb1, float rb2, float rb3, int n1, int n2, int n3) {
      float[][][] rx = new float[n3][n2][n1];
      ramp(ra, rb1, rb2, rb3, rx);
      return rx;
   }

   public static void ramp(float ra, float rb1, float[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = ra + rb1 * (float)i1;
      }
   }

   public static void ramp(float ra, float rb1, float rb2, float[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         ramp(ra + rb2 * (float)i2, rb1, rx[i2]);
      }
   }

   public static void ramp(float ra, float rb1, float rb2, float rb3, float[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         ramp(ra + rb3 * (float)i3, rb1, rb2, rx[i3]);
      }
   }

   public static float[] crampfloat(Cfloat ca, Cfloat cb1, int n1) {
      float[] cx = new float[2 * n1];
      cramp(ca, cb1, cx);
      return cx;
   }

   public static float[][] crampfloat(Cfloat ca, Cfloat cb1, Cfloat cb2, int n1, int n2) {
      float[][] cx = new float[n2][2 * n1];
      cramp(ca, cb1, cb2, cx);
      return cx;
   }

   public static float[][][] crampfloat(Cfloat ca, Cfloat cb1, Cfloat cb2, Cfloat cb3, int n1, int n2, int n3) {
      float[][][] cx = new float[n3][n2][2 * n1];
      cramp(ca, cb1, cb2, cb3, cx);
      return cx;
   }

   public static void cramp(Cfloat ca, Cfloat cb1, float[] cx) {
      int n1 = cx.length / 2;
      float ar = ca.r;
      float ai = ca.i;
      float br = cb1.r;
      float bi = cb1.i;
      int i1 = 0;
      int ir = 0;

      for(int ii = 1; i1 < n1; ii += 2) {
         cx[ir] = ar + br * (float)i1;
         cx[ii] = ai + bi * (float)i1;
         ++i1;
         ir += 2;
      }
   }

   public static void cramp(Cfloat ca, Cfloat cb1, Cfloat cb2, float[][] cx) {
      int n2 = cx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         cramp(ca.plus(cb2.times((float)i2)), cb1, cx[i2]);
      }
   }

   public static void cramp(Cfloat ca, Cfloat cb1, Cfloat cb2, Cfloat cb3, float[][][] cx) {
      int n3 = cx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         cramp(ca.plus(cb3.times((float)i3)), cb1, cb2, cx[i3]);
      }
   }

   public static double[] rampdouble(double ra, double rb1, int n1) {
      double[] rx = new double[n1];
      ramp(ra, rb1, rx);
      return rx;
   }

   public static double[][] rampdouble(double ra, double rb1, double rb2, int n1, int n2) {
      double[][] rx = new double[n2][n1];
      ramp(ra, rb1, rb2, rx);
      return rx;
   }

   public static double[][][] rampdouble(double ra, double rb1, double rb2, double rb3, int n1, int n2, int n3) {
      double[][][] rx = new double[n3][n2][n1];
      ramp(ra, rb1, rb2, rb3, rx);
      return rx;
   }

   public static void ramp(double ra, double rb1, double[] rx) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         rx[i1] = ra + rb1 * (double)i1;
      }
   }

   public static void ramp(double ra, double rb1, double rb2, double[][] rx) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         ramp(ra + rb2 * (double)i2, rb1, rx[i2]);
      }
   }

   public static void ramp(double ra, double rb1, double rb2, double rb3, double[][][] rx) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         ramp(ra + rb3 * (double)i3, rb1, rb2, rx[i3]);
      }
   }

   public static double[] crampdouble(Cdouble ca, Cdouble cb1, int n1) {
      double[] cx = new double[2 * n1];
      cramp(ca, cb1, cx);
      return cx;
   }

   public static double[][] crampdouble(Cdouble ca, Cdouble cb1, Cdouble cb2, int n1, int n2) {
      double[][] cx = new double[n2][2 * n1];
      cramp(ca, cb1, cb2, cx);
      return cx;
   }

   public static double[][][] crampdouble(Cdouble ca, Cdouble cb1, Cdouble cb2, Cdouble cb3, int n1, int n2, int n3) {
      double[][][] cx = new double[n3][n2][2 * n1];
      cramp(ca, cb1, cb2, cb3, cx);
      return cx;
   }

   public static void cramp(Cdouble ca, Cdouble cb1, double[] cx) {
      int n1 = cx.length / 2;
      double ar = ca.r;
      double ai = ca.i;
      double br = cb1.r;
      double bi = cb1.i;
      int i1 = 0;
      int ir = 0;

      for(int ii = 1; i1 < n1; ii += 2) {
         cx[ir] = ar + br * (double)i1;
         cx[ii] = ai + bi * (double)i1;
         ++i1;
         ir += 2;
      }
   }

   public static void cramp(Cdouble ca, Cdouble cb1, Cdouble cb2, double[][] cx) {
      int n2 = cx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         cramp(ca.plus(cb2.times((double)i2)), cb1, cx[i2]);
      }
   }

   public static void cramp(Cdouble ca, Cdouble cb1, Cdouble cb2, Cdouble cb3, double[][][] cx) {
      int n3 = cx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         cramp(ca.plus(cb3.times((double)i3)), cb1, cb2, cx[i3]);
      }
   }

   public static byte[] copy(byte[] rx) {
      return copy(rx.length, rx);
   }

   public static byte[][] copy(byte[][] rx) {
      int n2 = rx.length;
      byte[][] ry = new byte[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         ry[i2] = copy(rx[i2]);
      }

      return ry;
   }

   public static byte[][][] copy(byte[][][] rx) {
      int n3 = rx.length;
      byte[][][] ry = new byte[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         ry[i3] = copy(rx[i3]);
      }

      return ry;
   }

   public static void copy(byte[] rx, byte[] ry) {
      copy(rx.length, rx, ry);
   }

   public static void copy(byte[][] rx, byte[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         copy(rx[i2], ry[i2]);
      }
   }

   public static void copy(byte[][][] rx, byte[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         copy(rx[i3], ry[i3]);
      }
   }

   public static byte[] copy(int n1, byte[] rx) {
      byte[] ry = new byte[n1];
      copy(n1, rx, ry);
      return ry;
   }

   public static byte[][] copy(int n1, int n2, byte[][] rx) {
      byte[][] ry = new byte[n2][n1];
      copy(n1, n2, rx, ry);
      return ry;
   }

   public static byte[][][] copy(int n1, int n2, int n3, byte[][][] rx) {
      byte[][][] ry = new byte[n3][n2][n1];
      copy(n1, n2, n3, rx, ry);
      return ry;
   }

   public static void copy(int n1, byte[] rx, byte[] ry) {
      for(int i1 = 0; i1 < n1; ++i1) {
         ry[i1] = rx[i1];
      }
   }

   public static void copy(int n1, int n2, byte[][] rx, byte[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, rx[i2], ry[i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, byte[][][] rx, byte[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, rx[i3], ry[i3]);
      }
   }

   public static byte[] copy(int n1, int j1, byte[] rx) {
      byte[] ry = new byte[n1];
      copy(n1, j1, rx, 0, ry);
      return ry;
   }

   public static byte[][] copy(int n1, int n2, int j1, int j2, byte[][] rx) {
      byte[][] ry = new byte[n2][n1];
      copy(n1, n2, j1, j2, rx, 0, 0, ry);
      return ry;
   }

   public static byte[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, byte[][][] rx) {
      byte[][][] ry = new byte[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, rx, 0, 0, 0, ry);
      return ry;
   }

   public static byte[] copy(int n1, int j1, int k1, byte[] rx) {
      byte[] ry = new byte[n1];
      copy(n1, j1, k1, rx, 0, 1, ry);
      return ry;
   }

   public static byte[][] copy(int n1, int n2, int j1, int j2, int k1, int k2, byte[][] rx) {
      byte[][] ry = new byte[n2][n1];
      copy(n1, n2, j1, j2, k1, k2, rx, 0, 0, 1, 1, ry);
      return ry;
   }

   public static byte[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, int k1, int k2, int k3, byte[][][] rx) {
      byte[][][] ry = new byte[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, k1, k2, k3, rx, 0, 0, 0, 1, 1, 1, ry);
      return ry;
   }

   public static void copy(int n1, int j1x, byte[] rx, int j1y, byte[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; ++i1) {
         ry[iy++] = rx[ix++];
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, byte[][] rx, int j1y, int j2y, byte[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, rx[j2x + i2], j1y, ry[j2y + i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, int j1x, int j2x, int j3x, byte[][][] rx, int j1y, int j2y, int j3y, byte[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, rx[j3x + i3], j1y, j2y, ry[j3y + i3]);
      }
   }

   public static void copy(int n1, int j1x, int k1x, byte[] rx, int j1y, int k1y, byte[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; iy += k1y) {
         ry[iy] = rx[ix];
         ++i1;
         ix += k1x;
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, int k1x, int k2x, byte[][] rx, int j1y, int j2y, int k1y, int k2y, byte[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, k1x, rx[j2x + i2 * k2x], j1y, k1y, ry[j2y + i2 * k2y]);
      }
   }

   public static void copy(
      int n1,
      int n2,
      int n3,
      int j1x,
      int j2x,
      int j3x,
      int k1x,
      int k2x,
      int k3x,
      byte[][][] rx,
      int j1y,
      int j2y,
      int j3y,
      int k1y,
      int k2y,
      int k3y,
      byte[][][] ry
   ) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, k1x, k2x, rx[j3x + i3 * k3x], j1y, j2y, k1y, k2y, ry[j3y + i3 * k3y]);
      }
   }

   public static short[] copy(short[] rx) {
      return copy(rx.length, rx);
   }

   public static short[][] copy(short[][] rx) {
      int n2 = rx.length;
      short[][] ry = new short[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         ry[i2] = copy(rx[i2]);
      }

      return ry;
   }

   public static short[][][] copy(short[][][] rx) {
      int n3 = rx.length;
      short[][][] ry = new short[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         ry[i3] = copy(rx[i3]);
      }

      return ry;
   }

   public static void copy(short[] rx, short[] ry) {
      copy(rx.length, rx, ry);
   }

   public static void copy(short[][] rx, short[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         copy(rx[i2], ry[i2]);
      }
   }

   public static void copy(short[][][] rx, short[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         copy(rx[i3], ry[i3]);
      }
   }

   public static short[] copy(int n1, short[] rx) {
      short[] ry = new short[n1];
      copy(n1, rx, ry);
      return ry;
   }

   public static short[][] copy(int n1, int n2, short[][] rx) {
      short[][] ry = new short[n2][n1];
      copy(n1, n2, rx, ry);
      return ry;
   }

   public static short[][][] copy(int n1, int n2, int n3, short[][][] rx) {
      short[][][] ry = new short[n3][n2][n1];
      copy(n1, n2, n3, rx, ry);
      return ry;
   }

   public static void copy(int n1, short[] rx, short[] ry) {
      for(int i1 = 0; i1 < n1; ++i1) {
         ry[i1] = rx[i1];
      }
   }

   public static void copy(int n1, int n2, short[][] rx, short[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, rx[i2], ry[i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, short[][][] rx, short[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, rx[i3], ry[i3]);
      }
   }

   public static short[] copy(int n1, int j1, short[] rx) {
      short[] ry = new short[n1];
      copy(n1, j1, rx, 0, ry);
      return ry;
   }

   public static short[][] copy(int n1, int n2, int j1, int j2, short[][] rx) {
      short[][] ry = new short[n2][n1];
      copy(n1, n2, j1, j2, rx, 0, 0, ry);
      return ry;
   }

   public static short[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, short[][][] rx) {
      short[][][] ry = new short[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, rx, 0, 0, 0, ry);
      return ry;
   }

   public static short[] copy(int n1, int j1, int k1, short[] rx) {
      short[] ry = new short[n1];
      copy(n1, j1, k1, rx, 0, 1, ry);
      return ry;
   }

   public static short[][] copy(int n1, int n2, int j1, int j2, int k1, int k2, short[][] rx) {
      short[][] ry = new short[n2][n1];
      copy(n1, n2, j1, j2, k1, k2, rx, 0, 0, 1, 1, ry);
      return ry;
   }

   public static short[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, int k1, int k2, int k3, short[][][] rx) {
      short[][][] ry = new short[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, k1, k2, k3, rx, 0, 0, 0, 1, 1, 1, ry);
      return ry;
   }

   public static void copy(int n1, int j1x, short[] rx, int j1y, short[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; ++i1) {
         ry[iy++] = rx[ix++];
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, short[][] rx, int j1y, int j2y, short[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, rx[j2x + i2], j1y, ry[j2y + i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, int j1x, int j2x, int j3x, short[][][] rx, int j1y, int j2y, int j3y, short[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, rx[j3x + i3], j1y, j2y, ry[j3y + i3]);
      }
   }

   public static void copy(int n1, int j1x, int k1x, short[] rx, int j1y, int k1y, short[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; iy += k1y) {
         ry[iy] = rx[ix];
         ++i1;
         ix += k1x;
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, int k1x, int k2x, short[][] rx, int j1y, int j2y, int k1y, int k2y, short[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, k1x, rx[j2x + i2 * k2x], j1y, k1y, ry[j2y + i2 * k2y]);
      }
   }

   public static void copy(
      int n1,
      int n2,
      int n3,
      int j1x,
      int j2x,
      int j3x,
      int k1x,
      int k2x,
      int k3x,
      short[][][] rx,
      int j1y,
      int j2y,
      int j3y,
      int k1y,
      int k2y,
      int k3y,
      short[][][] ry
   ) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, k1x, k2x, rx[j3x + i3 * k3x], j1y, j2y, k1y, k2y, ry[j3y + i3 * k3y]);
      }
   }

   public static int[] copy(int[] rx) {
      return copy(rx.length, rx);
   }

   public static int[][] copy(int[][] rx) {
      int n2 = rx.length;
      int[][] ry = new int[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         ry[i2] = copy(rx[i2]);
      }

      return ry;
   }

   public static int[][][] copy(int[][][] rx) {
      int n3 = rx.length;
      int[][][] ry = new int[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         ry[i3] = copy(rx[i3]);
      }

      return ry;
   }

   public static void copy(int[] rx, int[] ry) {
      copy(rx.length, rx, ry);
   }

   public static void copy(int[][] rx, int[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         copy(rx[i2], ry[i2]);
      }
   }

   public static void copy(int[][][] rx, int[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         copy(rx[i3], ry[i3]);
      }
   }

   public static int[] copy(int n1, int[] rx) {
      int[] ry = new int[n1];
      copy(n1, rx, ry);
      return ry;
   }

   public static int[][] copy(int n1, int n2, int[][] rx) {
      int[][] ry = new int[n2][n1];
      copy(n1, n2, rx, ry);
      return ry;
   }

   public static int[][][] copy(int n1, int n2, int n3, int[][][] rx) {
      int[][][] ry = new int[n3][n2][n1];
      copy(n1, n2, n3, rx, ry);
      return ry;
   }

   public static void copy(int n1, int[] rx, int[] ry) {
      for(int i1 = 0; i1 < n1; ++i1) {
         ry[i1] = rx[i1];
      }
   }

   public static void copy(int n1, int n2, int[][] rx, int[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, rx[i2], ry[i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, int[][][] rx, int[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, rx[i3], ry[i3]);
      }
   }

   public static int[] copy(int n1, int j1, int[] rx) {
      int[] ry = new int[n1];
      copy(n1, j1, rx, 0, ry);
      return ry;
   }

   public static int[][] copy(int n1, int n2, int j1, int j2, int[][] rx) {
      int[][] ry = new int[n2][n1];
      copy(n1, n2, j1, j2, rx, 0, 0, ry);
      return ry;
   }

   public static int[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, int[][][] rx) {
      int[][][] ry = new int[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, rx, 0, 0, 0, ry);
      return ry;
   }

   public static int[] copy(int n1, int j1, int k1, int[] rx) {
      int[] ry = new int[n1];
      copy(n1, j1, k1, rx, 0, 1, ry);
      return ry;
   }

   public static int[][] copy(int n1, int n2, int j1, int j2, int k1, int k2, int[][] rx) {
      int[][] ry = new int[n2][n1];
      copy(n1, n2, j1, j2, k1, k2, rx, 0, 0, 1, 1, ry);
      return ry;
   }

   public static int[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, int k1, int k2, int k3, int[][][] rx) {
      int[][][] ry = new int[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, k1, k2, k3, rx, 0, 0, 0, 1, 1, 1, ry);
      return ry;
   }

   public static void copy(int n1, int j1x, int[] rx, int j1y, int[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; ++i1) {
         ry[iy++] = rx[ix++];
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, int[][] rx, int j1y, int j2y, int[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, rx[j2x + i2], j1y, ry[j2y + i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, int j1x, int j2x, int j3x, int[][][] rx, int j1y, int j2y, int j3y, int[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, rx[j3x + i3], j1y, j2y, ry[j3y + i3]);
      }
   }

   public static void copy(int n1, int j1x, int k1x, int[] rx, int j1y, int k1y, int[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; iy += k1y) {
         ry[iy] = rx[ix];
         ++i1;
         ix += k1x;
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, int k1x, int k2x, int[][] rx, int j1y, int j2y, int k1y, int k2y, int[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, k1x, rx[j2x + i2 * k2x], j1y, k1y, ry[j2y + i2 * k2y]);
      }
   }

   public static void copy(
      int n1,
      int n2,
      int n3,
      int j1x,
      int j2x,
      int j3x,
      int k1x,
      int k2x,
      int k3x,
      int[][][] rx,
      int j1y,
      int j2y,
      int j3y,
      int k1y,
      int k2y,
      int k3y,
      int[][][] ry
   ) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, k1x, k2x, rx[j3x + i3 * k3x], j1y, j2y, k1y, k2y, ry[j3y + i3 * k3y]);
      }
   }

   public static long[] copy(long[] rx) {
      return copy(rx.length, rx);
   }

   public static long[][] copy(long[][] rx) {
      int n2 = rx.length;
      long[][] ry = new long[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         ry[i2] = copy(rx[i2]);
      }

      return ry;
   }

   public static long[][][] copy(long[][][] rx) {
      int n3 = rx.length;
      long[][][] ry = new long[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         ry[i3] = copy(rx[i3]);
      }

      return ry;
   }

   public static void copy(long[] rx, long[] ry) {
      copy(rx.length, rx, ry);
   }

   public static void copy(long[][] rx, long[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         copy(rx[i2], ry[i2]);
      }
   }

   public static void copy(long[][][] rx, long[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         copy(rx[i3], ry[i3]);
      }
   }

   public static long[] copy(int n1, long[] rx) {
      long[] ry = new long[n1];
      copy(n1, rx, ry);
      return ry;
   }

   public static long[][] copy(int n1, int n2, long[][] rx) {
      long[][] ry = new long[n2][n1];
      copy(n1, n2, rx, ry);
      return ry;
   }

   public static long[][][] copy(int n1, int n2, int n3, long[][][] rx) {
      long[][][] ry = new long[n3][n2][n1];
      copy(n1, n2, n3, rx, ry);
      return ry;
   }

   public static void copy(int n1, long[] rx, long[] ry) {
      for(int i1 = 0; i1 < n1; ++i1) {
         ry[i1] = rx[i1];
      }
   }

   public static void copy(int n1, int n2, long[][] rx, long[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, rx[i2], ry[i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, long[][][] rx, long[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, rx[i3], ry[i3]);
      }
   }

   public static long[] copy(int n1, int j1, long[] rx) {
      long[] ry = new long[n1];
      copy(n1, j1, rx, 0, ry);
      return ry;
   }

   public static long[][] copy(int n1, int n2, int j1, int j2, long[][] rx) {
      long[][] ry = new long[n2][n1];
      copy(n1, n2, j1, j2, rx, 0, 0, ry);
      return ry;
   }

   public static long[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, long[][][] rx) {
      long[][][] ry = new long[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, rx, 0, 0, 0, ry);
      return ry;
   }

   public static long[] copy(int n1, int j1, int k1, long[] rx) {
      long[] ry = new long[n1];
      copy(n1, j1, k1, rx, 0, 1, ry);
      return ry;
   }

   public static long[][] copy(int n1, int n2, int j1, int j2, int k1, int k2, long[][] rx) {
      long[][] ry = new long[n2][n1];
      copy(n1, n2, j1, j2, k1, k2, rx, 0, 0, 1, 1, ry);
      return ry;
   }

   public static long[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, int k1, int k2, int k3, long[][][] rx) {
      long[][][] ry = new long[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, k1, k2, k3, rx, 0, 0, 0, 1, 1, 1, ry);
      return ry;
   }

   public static void copy(int n1, int j1x, long[] rx, int j1y, long[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; ++i1) {
         ry[iy++] = rx[ix++];
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, long[][] rx, int j1y, int j2y, long[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, rx[j2x + i2], j1y, ry[j2y + i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, int j1x, int j2x, int j3x, long[][][] rx, int j1y, int j2y, int j3y, long[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, rx[j3x + i3], j1y, j2y, ry[j3y + i3]);
      }
   }

   public static void copy(int n1, int j1x, int k1x, long[] rx, int j1y, int k1y, long[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; iy += k1y) {
         ry[iy] = rx[ix];
         ++i1;
         ix += k1x;
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, int k1x, int k2x, long[][] rx, int j1y, int j2y, int k1y, int k2y, long[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, k1x, rx[j2x + i2 * k2x], j1y, k1y, ry[j2y + i2 * k2y]);
      }
   }

   public static void copy(
      int n1,
      int n2,
      int n3,
      int j1x,
      int j2x,
      int j3x,
      int k1x,
      int k2x,
      int k3x,
      long[][][] rx,
      int j1y,
      int j2y,
      int j3y,
      int k1y,
      int k2y,
      int k3y,
      long[][][] ry
   ) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, k1x, k2x, rx[j3x + i3 * k3x], j1y, j2y, k1y, k2y, ry[j3y + i3 * k3y]);
      }
   }

   public static float[] copy(float[] rx) {
      return copy(rx.length, rx);
   }

   public static float[][] copy(float[][] rx) {
      int n2 = rx.length;
      float[][] ry = new float[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         ry[i2] = copy(rx[i2]);
      }

      return ry;
   }

   public static float[][][] copy(float[][][] rx) {
      int n3 = rx.length;
      float[][][] ry = new float[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         ry[i3] = copy(rx[i3]);
      }

      return ry;
   }

   public static void copy(float[] rx, float[] ry) {
      copy(rx.length, rx, ry);
   }

   public static void copy(float[][] rx, float[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         copy(rx[i2], ry[i2]);
      }
   }

   public static void copy(float[][][] rx, float[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         copy(rx[i3], ry[i3]);
      }
   }

   public static float[] copy(int n1, float[] rx) {
      float[] ry = new float[n1];
      copy(n1, rx, ry);
      return ry;
   }

   public static float[][] copy(int n1, int n2, float[][] rx) {
      float[][] ry = new float[n2][n1];
      copy(n1, n2, rx, ry);
      return ry;
   }

   public static float[][][] copy(int n1, int n2, int n3, float[][][] rx) {
      float[][][] ry = new float[n3][n2][n1];
      copy(n1, n2, n3, rx, ry);
      return ry;
   }

   public static void copy(int n1, float[] rx, float[] ry) {
      for(int i1 = 0; i1 < n1; ++i1) {
         ry[i1] = rx[i1];
      }
   }

   public static void copy(int n1, int n2, float[][] rx, float[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, rx[i2], ry[i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, float[][][] rx, float[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, rx[i3], ry[i3]);
      }
   }

   public static float[] copy(int n1, int j1, float[] rx) {
      float[] ry = new float[n1];
      copy(n1, j1, rx, 0, ry);
      return ry;
   }

   public static float[][] copy(int n1, int n2, int j1, int j2, float[][] rx) {
      float[][] ry = new float[n2][n1];
      copy(n1, n2, j1, j2, rx, 0, 0, ry);
      return ry;
   }

   public static float[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, float[][][] rx) {
      float[][][] ry = new float[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, rx, 0, 0, 0, ry);
      return ry;
   }

   public static float[] copy(int n1, int j1, int k1, float[] rx) {
      float[] ry = new float[n1];
      copy(n1, j1, k1, rx, 0, 1, ry);
      return ry;
   }

   public static float[][] copy(int n1, int n2, int j1, int j2, int k1, int k2, float[][] rx) {
      float[][] ry = new float[n2][n1];
      copy(n1, n2, j1, j2, k1, k2, rx, 0, 0, 1, 1, ry);
      return ry;
   }

   public static float[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, int k1, int k2, int k3, float[][][] rx) {
      float[][][] ry = new float[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, k1, k2, k3, rx, 0, 0, 0, 1, 1, 1, ry);
      return ry;
   }

   public static void copy(int n1, int j1x, float[] rx, int j1y, float[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; ++i1) {
         ry[iy++] = rx[ix++];
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, float[][] rx, int j1y, int j2y, float[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, rx[j2x + i2], j1y, ry[j2y + i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, int j1x, int j2x, int j3x, float[][][] rx, int j1y, int j2y, int j3y, float[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, rx[j3x + i3], j1y, j2y, ry[j3y + i3]);
      }
   }

   public static void copy(int n1, int j1x, int k1x, float[] rx, int j1y, int k1y, float[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; iy += k1y) {
         ry[iy] = rx[ix];
         ++i1;
         ix += k1x;
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, int k1x, int k2x, float[][] rx, int j1y, int j2y, int k1y, int k2y, float[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, k1x, rx[j2x + i2 * k2x], j1y, k1y, ry[j2y + i2 * k2y]);
      }
   }

   public static void copy(
      int n1,
      int n2,
      int n3,
      int j1x,
      int j2x,
      int j3x,
      int k1x,
      int k2x,
      int k3x,
      float[][][] rx,
      int j1y,
      int j2y,
      int j3y,
      int k1y,
      int k2y,
      int k3y,
      float[][][] ry
   ) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, k1x, k2x, rx[j3x + i3 * k3x], j1y, j2y, k1y, k2y, ry[j3y + i3 * k3y]);
      }
   }

   public static float[] ccopy(float[] cx) {
      return ccopy(cx.length / 2, cx);
   }

   public static float[][] ccopy(float[][] cx) {
      int n2 = cx.length;
      float[][] cy = new float[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         cy[i2] = ccopy(cx[i2]);
      }

      return cy;
   }

   public static float[][][] ccopy(float[][][] cx) {
      int n3 = cx.length;
      float[][][] cy = new float[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         cy[i3] = ccopy(cx[i3]);
      }

      return cy;
   }

   public static void ccopy(float[] cx, float[] cy) {
      ccopy(cx.length / 2, cx, cy);
   }

   public static void ccopy(float[][] cx, float[][] cy) {
      int n2 = cx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         ccopy(cx[i2], cy[i2]);
      }
   }

   public static void ccopy(float[][][] cx, float[][][] cy) {
      int n3 = cx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         ccopy(cx[i3], cy[i3]);
      }
   }

   public static float[] ccopy(int n1, float[] cx) {
      float[] cy = new float[2 * n1];
      ccopy(n1, cx, cy);
      return cy;
   }

   public static float[][] ccopy(int n1, int n2, float[][] cx) {
      float[][] cy = new float[n2][2 * n1];
      ccopy(n1, n2, cx, cy);
      return cy;
   }

   public static float[][][] ccopy(int n1, int n2, int n3, float[][][] cx) {
      float[][][] cy = new float[n3][n2][2 * n1];
      ccopy(n1, n2, n3, cx, cy);
      return cy;
   }

   public static void ccopy(int n1, float[] cx, float[] cy) {
      copy(2 * n1, cx, cy);
   }

   public static void ccopy(int n1, int n2, float[][] cx, float[][] cy) {
      for(int i2 = 0; i2 < n2; ++i2) {
         ccopy(n1, cx[i2], cy[i2]);
      }
   }

   public static void ccopy(int n1, int n2, int n3, float[][][] cx, float[][][] cy) {
      for(int i3 = 0; i3 < n3; ++i3) {
         ccopy(n1, n2, cx[i3], cy[i3]);
      }
   }

   public static float[] ccopy(int n1, int j1, float[] cx) {
      float[] cy = new float[2 * n1];
      ccopy(n1, j1, cx, 0, cy);
      return cy;
   }

   public static float[][] ccopy(int n1, int n2, int j1, int j2, float[][] cx) {
      float[][] cy = new float[n2][2 * n1];
      ccopy(n1, n2, j1, j2, cx, 0, 0, cy);
      return cy;
   }

   public static float[][][] ccopy(int n1, int n2, int n3, int j1, int j2, int j3, float[][][] cx) {
      float[][][] cy = new float[n3][n2][2 * n1];
      ccopy(n1, n2, n3, j1, j2, j3, cx, 0, 0, 0, cy);
      return cy;
   }

   public static float[] ccopy(int n1, int j1, int k1, float[] cx) {
      float[] cy = new float[2 * n1];
      ccopy(n1, j1, k1, cx, 0, 1, cy);
      return cy;
   }

   public static float[][] ccopy(int n1, int n2, int j1, int j2, int k1, int k2, float[][] cx) {
      float[][] cy = new float[n2][2 * n1];
      ccopy(n1, n2, j1, j2, k1, k2, cx, 0, 0, 1, 1, cy);
      return cy;
   }

   public static float[][][] ccopy(int n1, int n2, int n3, int j1, int j2, int j3, int k1, int k2, int k3, float[][][] cx) {
      float[][][] cy = new float[n3][n2][2 * n1];
      ccopy(n1, n2, n3, j1, j2, j3, k1, k2, k3, cx, 0, 0, 0, 1, 1, 1, cy);
      return cy;
   }

   public static void ccopy(int n1, int j1x, float[] cx, int j1y, float[] cy) {
      int i1 = 0;
      int ix = 2 * j1x;

      for(int iy = 2 * j1y; i1 < n1; ++i1) {
         cy[iy++] = cx[ix++];
         cy[iy++] = cx[ix++];
      }
   }

   public static void ccopy(int n1, int n2, int j1x, int j2x, float[][] cx, int j1y, int j2y, float[][] cy) {
      for(int i2 = 0; i2 < n2; ++i2) {
         ccopy(n1, j1x, cx[j2x + i2], j1y, cy[j2y + i2]);
      }
   }

   public static void ccopy(int n1, int n2, int n3, int j1x, int j2x, int j3x, float[][][] cx, int j1y, int j2y, int j3y, float[][][] cy) {
      for(int i3 = 0; i3 < n3; ++i3) {
         ccopy(n1, n2, j1x, j2x, cx[j3x + i3], j1y, j2y, cy[j3y + i3]);
      }
   }

   public static void ccopy(int n1, int j1x, int k1x, float[] cx, int j1y, int k1y, float[] cy) {
      int k1x2 = k1x * 2;
      int k1y2 = k1y * 2;
      int i1 = 0;
      int ix = 2 * j1x;

      for(int iy = 2 * j1y; i1 < n1; iy += k1y2) {
         cy[iy] = cx[ix];
         cy[iy + 1] = cx[ix + 1];
         ++i1;
         ix += k1x2;
      }
   }

   public static void ccopy(int n1, int n2, int j1x, int j2x, int k1x, int k2x, float[][] cx, int j1y, int j2y, int k1y, int k2y, float[][] cy) {
      for(int i2 = 0; i2 < n2; ++i2) {
         ccopy(n1, j1x, k1x, cx[j2x + i2 * k2x], j1y, k1y, cy[j2y + i2 * k2y]);
      }
   }

   public static void ccopy(
      int n1,
      int n2,
      int n3,
      int j1x,
      int j2x,
      int j3x,
      int k1x,
      int k2x,
      int k3x,
      float[][][] cx,
      int j1y,
      int j2y,
      int j3y,
      int k1y,
      int k2y,
      int k3y,
      float[][][] cy
   ) {
      for(int i3 = 0; i3 < n3; ++i3) {
         ccopy(n1, n2, j1x, j2x, k1x, k2x, cx[j3x + i3 * k3x], j1y, j2y, k1y, k2y, cy[j3y + i3 * k3y]);
      }
   }

   public static double[] copy(double[] rx) {
      return copy(rx.length, rx);
   }

   public static double[][] copy(double[][] rx) {
      int n2 = rx.length;
      double[][] ry = new double[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         ry[i2] = copy(rx[i2]);
      }

      return ry;
   }

   public static double[][][] copy(double[][][] rx) {
      int n3 = rx.length;
      double[][][] ry = new double[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         ry[i3] = copy(rx[i3]);
      }

      return ry;
   }

   public static void copy(double[] rx, double[] ry) {
      copy(rx.length, rx, ry);
   }

   public static void copy(double[][] rx, double[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         copy(rx[i2], ry[i2]);
      }
   }

   public static void copy(double[][][] rx, double[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         copy(rx[i3], ry[i3]);
      }
   }

   public static double[] copy(int n1, double[] rx) {
      double[] ry = new double[n1];
      copy(n1, rx, ry);
      return ry;
   }

   public static double[][] copy(int n1, int n2, double[][] rx) {
      double[][] ry = new double[n2][n1];
      copy(n1, n2, rx, ry);
      return ry;
   }

   public static double[][][] copy(int n1, int n2, int n3, double[][][] rx) {
      double[][][] ry = new double[n3][n2][n1];
      copy(n1, n2, n3, rx, ry);
      return ry;
   }

   public static void copy(int n1, double[] rx, double[] ry) {
      for(int i1 = 0; i1 < n1; ++i1) {
         ry[i1] = rx[i1];
      }
   }

   public static void copy(int n1, int n2, double[][] rx, double[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, rx[i2], ry[i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, double[][][] rx, double[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, rx[i3], ry[i3]);
      }
   }

   public static double[] copy(int n1, int j1, double[] rx) {
      double[] ry = new double[n1];
      copy(n1, j1, rx, 0, ry);
      return ry;
   }

   public static double[][] copy(int n1, int n2, int j1, int j2, double[][] rx) {
      double[][] ry = new double[n2][n1];
      copy(n1, n2, j1, j2, rx, 0, 0, ry);
      return ry;
   }

   public static double[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, double[][][] rx) {
      double[][][] ry = new double[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, rx, 0, 0, 0, ry);
      return ry;
   }

   public static double[] copy(int n1, int j1, int k1, double[] rx) {
      double[] ry = new double[n1];
      copy(n1, j1, k1, rx, 0, 1, ry);
      return ry;
   }

   public static double[][] copy(int n1, int n2, int j1, int j2, int k1, int k2, double[][] rx) {
      double[][] ry = new double[n2][n1];
      copy(n1, n2, j1, j2, k1, k2, rx, 0, 0, 1, 1, ry);
      return ry;
   }

   public static double[][][] copy(int n1, int n2, int n3, int j1, int j2, int j3, int k1, int k2, int k3, double[][][] rx) {
      double[][][] ry = new double[n3][n2][n1];
      copy(n1, n2, n3, j1, j2, j3, k1, k2, k3, rx, 0, 0, 0, 1, 1, 1, ry);
      return ry;
   }

   public static void copy(int n1, int j1x, double[] rx, int j1y, double[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; ++i1) {
         ry[iy++] = rx[ix++];
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, double[][] rx, int j1y, int j2y, double[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, rx[j2x + i2], j1y, ry[j2y + i2]);
      }
   }

   public static void copy(int n1, int n2, int n3, int j1x, int j2x, int j3x, double[][][] rx, int j1y, int j2y, int j3y, double[][][] ry) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, rx[j3x + i3], j1y, j2y, ry[j3y + i3]);
      }
   }

   public static void copy(int n1, int j1x, int k1x, double[] rx, int j1y, int k1y, double[] ry) {
      int i1 = 0;
      int ix = j1x;

      for(int iy = j1y; i1 < n1; iy += k1y) {
         ry[iy] = rx[ix];
         ++i1;
         ix += k1x;
      }
   }

   public static void copy(int n1, int n2, int j1x, int j2x, int k1x, int k2x, double[][] rx, int j1y, int j2y, int k1y, int k2y, double[][] ry) {
      for(int i2 = 0; i2 < n2; ++i2) {
         copy(n1, j1x, k1x, rx[j2x + i2 * k2x], j1y, k1y, ry[j2y + i2 * k2y]);
      }
   }

   public static void copy(
      int n1,
      int n2,
      int n3,
      int j1x,
      int j2x,
      int j3x,
      int k1x,
      int k2x,
      int k3x,
      double[][][] rx,
      int j1y,
      int j2y,
      int j3y,
      int k1y,
      int k2y,
      int k3y,
      double[][][] ry
   ) {
      for(int i3 = 0; i3 < n3; ++i3) {
         copy(n1, n2, j1x, j2x, k1x, k2x, rx[j3x + i3 * k3x], j1y, j2y, k1y, k2y, ry[j3y + i3 * k3y]);
      }
   }

   public static double[] ccopy(double[] cx) {
      return ccopy(cx.length / 2, cx);
   }

   public static double[][] ccopy(double[][] cx) {
      int n2 = cx.length;
      double[][] cy = new double[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         cy[i2] = ccopy(cx[i2]);
      }

      return cy;
   }

   public static double[][][] ccopy(double[][][] cx) {
      int n3 = cx.length;
      double[][][] cy = new double[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         cy[i3] = ccopy(cx[i3]);
      }

      return cy;
   }

   public static void ccopy(double[] cx, double[] cy) {
      ccopy(cx.length / 2, cx, cy);
   }

   public static void ccopy(double[][] cx, double[][] cy) {
      int n2 = cx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         ccopy(cx[i2], cy[i2]);
      }
   }

   public static void ccopy(double[][][] cx, double[][][] cy) {
      int n3 = cx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         ccopy(cx[i3], cy[i3]);
      }
   }

   public static double[] ccopy(int n1, double[] cx) {
      double[] cy = new double[2 * n1];
      ccopy(n1, cx, cy);
      return cy;
   }

   public static double[][] ccopy(int n1, int n2, double[][] cx) {
      double[][] cy = new double[n2][2 * n1];
      ccopy(n1, n2, cx, cy);
      return cy;
   }

   public static double[][][] ccopy(int n1, int n2, int n3, double[][][] cx) {
      double[][][] cy = new double[n3][n2][2 * n1];
      ccopy(n1, n2, n3, cx, cy);
      return cy;
   }

   public static void ccopy(int n1, double[] cx, double[] cy) {
      copy(2 * n1, cx, cy);
   }

   public static void ccopy(int n1, int n2, double[][] cx, double[][] cy) {
      for(int i2 = 0; i2 < n2; ++i2) {
         ccopy(n1, cx[i2], cy[i2]);
      }
   }

   public static void ccopy(int n1, int n2, int n3, double[][][] cx, double[][][] cy) {
      for(int i3 = 0; i3 < n3; ++i3) {
         ccopy(n1, n2, cx[i3], cy[i3]);
      }
   }

   public static double[] ccopy(int n1, int j1, double[] cx) {
      double[] cy = new double[2 * n1];
      ccopy(n1, j1, cx, 0, cy);
      return cy;
   }

   public static double[][] ccopy(int n1, int n2, int j1, int j2, double[][] cx) {
      double[][] cy = new double[n2][2 * n1];
      ccopy(n1, n2, j1, j2, cx, 0, 0, cy);
      return cy;
   }

   public static double[][][] ccopy(int n1, int n2, int n3, int j1, int j2, int j3, double[][][] cx) {
      double[][][] cy = new double[n3][n2][2 * n1];
      ccopy(n1, n2, n3, j1, j2, j3, cx, 0, 0, 0, cy);
      return cy;
   }

   public static double[] ccopy(int n1, int j1, int k1, double[] cx) {
      double[] cy = new double[2 * n1];
      ccopy(n1, j1, k1, cx, 0, 1, cy);
      return cy;
   }

   public static double[][] ccopy(int n1, int n2, int j1, int j2, int k1, int k2, double[][] cx) {
      double[][] cy = new double[n2][2 * n1];
      ccopy(n1, n2, j1, j2, k1, k2, cx, 0, 0, 1, 1, cy);
      return cy;
   }

   public static double[][][] ccopy(int n1, int n2, int n3, int j1, int j2, int j3, int k1, int k2, int k3, double[][][] cx) {
      double[][][] cy = new double[n3][n2][2 * n1];
      ccopy(n1, n2, n3, j1, j2, j3, k1, k2, k3, cx, 0, 0, 0, 1, 1, 1, cy);
      return cy;
   }

   public static void ccopy(int n1, int j1x, double[] cx, int j1y, double[] cy) {
      int i1 = 0;
      int ix = 2 * j1x;

      for(int iy = 2 * j1y; i1 < n1; ++i1) {
         cy[iy++] = cx[ix++];
         cy[iy++] = cx[ix++];
      }
   }

   public static void ccopy(int n1, int n2, int j1x, int j2x, double[][] cx, int j1y, int j2y, double[][] cy) {
      for(int i2 = 0; i2 < n2; ++i2) {
         ccopy(n1, j1x, cx[j2x + i2], j1y, cy[j2y + i2]);
      }
   }

   public static void ccopy(int n1, int n2, int n3, int j1x, int j2x, int j3x, double[][][] cx, int j1y, int j2y, int j3y, double[][][] cy) {
      for(int i3 = 0; i3 < n3; ++i3) {
         ccopy(n1, n2, j1x, j2x, cx[j3x + i3], j1y, j2y, cy[j3y + i3]);
      }
   }

   public static void ccopy(int n1, int j1x, int k1x, double[] cx, int j1y, int k1y, double[] cy) {
      int k1x2 = k1x * 2;
      int k1y2 = k1y * 2;
      int i1 = 0;
      int ix = 2 * j1x;

      for(int iy = 2 * j1y; i1 < n1; iy += k1y2) {
         cy[iy] = cx[ix];
         cy[iy + 1] = cx[ix + 1];
         ++i1;
         ix += k1x2;
      }
   }

   public static void ccopy(int n1, int n2, int j1x, int j2x, int k1x, int k2x, double[][] cx, int j1y, int j2y, int k1y, int k2y, double[][] cy) {
      for(int i2 = 0; i2 < n2; ++i2) {
         ccopy(n1, j1x, k1x, cx[j2x + i2 * k2x], j1y, k1y, cy[j2y + i2 * k2y]);
      }
   }

   public static void ccopy(
      int n1,
      int n2,
      int n3,
      int j1x,
      int j2x,
      int j3x,
      int k1x,
      int k2x,
      int k3x,
      double[][][] cx,
      int j1y,
      int j2y,
      int j3y,
      int k1y,
      int k2y,
      int k3y,
      double[][][] cy
   ) {
      for(int i3 = 0; i3 < n3; ++i3) {
         ccopy(n1, n2, j1x, j2x, k1x, k2x, cx[j3x + i3 * k3x], j1y, j2y, k1y, k2y, cy[j3y + i3 * k3y]);
      }
   }

   public static byte[] reverse(byte[] rx) {
      byte[] ry = new byte[rx.length];
      reverse(rx, ry);
      return ry;
   }

   public static void reverse(byte[] rx, byte[] ry) {
      int n1 = rx.length;
      int i1 = 0;

      for(int j1 = n1 - 1; i1 < n1; --j1) {
         ry[j1] = rx[i1];
         ++i1;
      }
   }

   public static short[] reverse(short[] rx) {
      short[] ry = new short[rx.length];
      reverse(rx, ry);
      return ry;
   }

   public static void reverse(short[] rx, short[] ry) {
      int n1 = rx.length;
      int i1 = 0;

      for(int j1 = n1 - 1; i1 < n1; --j1) {
         ry[j1] = rx[i1];
         ++i1;
      }
   }

   public static int[] reverse(int[] rx) {
      int[] ry = new int[rx.length];
      reverse(rx, ry);
      return ry;
   }

   public static void reverse(int[] rx, int[] ry) {
      int n1 = rx.length;
      int i1 = 0;

      for(int j1 = n1 - 1; i1 < n1; --j1) {
         ry[j1] = rx[i1];
         ++i1;
      }
   }

   public static long[] reverse(long[] rx) {
      long[] ry = new long[rx.length];
      reverse(rx, ry);
      return ry;
   }

   public static void reverse(long[] rx, long[] ry) {
      int n1 = rx.length;
      int i1 = 0;

      for(int j1 = n1 - 1; i1 < n1; --j1) {
         ry[j1] = rx[i1];
         ++i1;
      }
   }

   public static float[] reverse(float[] rx) {
      float[] ry = new float[rx.length];
      reverse(rx, ry);
      return ry;
   }

   public static void reverse(float[] rx, float[] ry) {
      int n1 = rx.length;
      int i1 = 0;

      for(int j1 = n1 - 1; i1 < n1; --j1) {
         ry[j1] = rx[i1];
         ++i1;
      }
   }

   public static float[] creverse(float[] rx) {
      float[] ry = new float[rx.length];
      reverse(rx, ry);
      return ry;
   }

   public static void creverse(float[] rx, float[] ry) {
      int n1 = rx.length / 2;
      int i1 = 0;

      for(int j1 = 2 * n1 - 2; i1 < n1; j1 -= 2) {
         ry[j1] = rx[i1];
         ry[j1 + 1] = rx[i1 + 1];
         i1 += 2;
      }
   }

   public static double[] reverse(double[] rx) {
      double[] ry = new double[rx.length];
      reverse(rx, ry);
      return ry;
   }

   public static void reverse(double[] rx, double[] ry) {
      int n1 = rx.length;
      int i1 = 0;

      for(int j1 = n1 - 1; i1 < n1; --j1) {
         ry[j1] = rx[i1];
         ++i1;
      }
   }

   public static double[] creverse(double[] rx) {
      double[] ry = new double[rx.length];
      reverse(rx, ry);
      return ry;
   }

   public static void creverse(double[] rx, double[] ry) {
      int n1 = rx.length / 2;
      int i1 = 0;

      for(int j1 = 2 * n1 - 2; i1 < n1; j1 -= 2) {
         ry[j1] = rx[i1];
         ry[j1 + 1] = rx[i1 + 1];
         i1 += 2;
      }
   }

   public static byte[] flatten(byte[][] rx) {
      int n = 0;
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         n += rx[i2].length;
      }

      byte[] ry = new byte[n];
      int i2 = 0;

      for(int iy = 0; i2 < n2; ++i2) {
         int n1 = rx[i2].length;
         copy(n1, 0, rx[i2], iy, ry);
         iy += n1;
      }

      return ry;
   }

   public static byte[] flatten(byte[][][] rx) {
      int n = 0;
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            n += rx[i3][i2].length;
         }
      }

      byte[] ry = new byte[n];
      int i3 = 0;

      for(int iy = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            int n1 = rx[i3][i2].length;
            copy(n1, 0, rx[i3][i2], iy, ry);
            iy += n1;
         }
      }

      return ry;
   }

   public static short[] flatten(short[][] rx) {
      int n = 0;
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         n += rx[i2].length;
      }

      short[] ry = new short[n];
      int i2 = 0;

      for(int iy = 0; i2 < n2; ++i2) {
         int n1 = rx[i2].length;
         copy(n1, 0, rx[i2], iy, ry);
         iy += n1;
      }

      return ry;
   }

   public static short[] flatten(short[][][] rx) {
      int n = 0;
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            n += rx[i3][i2].length;
         }
      }

      short[] ry = new short[n];
      int i3 = 0;

      for(int iy = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            int n1 = rx[i3][i2].length;
            copy(n1, 0, rx[i3][i2], iy, ry);
            iy += n1;
         }
      }

      return ry;
   }

   public static int[] flatten(int[][] rx) {
      int n = 0;
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         n += rx[i2].length;
      }

      int[] ry = new int[n];
      int i2 = 0;

      for(int iy = 0; i2 < n2; ++i2) {
         int n1 = rx[i2].length;
         copy(n1, 0, rx[i2], iy, ry);
         iy += n1;
      }

      return ry;
   }

   public static int[] flatten(int[][][] rx) {
      int n = 0;
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            n += rx[i3][i2].length;
         }
      }

      int[] ry = new int[n];
      int i3 = 0;

      for(int iy = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            int n1 = rx[i3][i2].length;
            copy(n1, 0, rx[i3][i2], iy, ry);
            iy += n1;
         }
      }

      return ry;
   }

   public static long[] flatten(long[][] rx) {
      int n = 0;
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         n += rx[i2].length;
      }

      long[] ry = new long[n];
      int i2 = 0;

      for(int iy = 0; i2 < n2; ++i2) {
         int n1 = rx[i2].length;
         copy(n1, 0, rx[i2], iy, ry);
         iy += n1;
      }

      return ry;
   }

   public static long[] flatten(long[][][] rx) {
      int n = 0;
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            n += rx[i3][i2].length;
         }
      }

      long[] ry = new long[n];
      int i3 = 0;

      for(int iy = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            int n1 = rx[i3][i2].length;
            copy(n1, 0, rx[i3][i2], iy, ry);
            iy += n1;
         }
      }

      return ry;
   }

   public static float[] flatten(float[][] rx) {
      int n = 0;
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         n += rx[i2].length;
      }

      float[] ry = new float[n];
      int i2 = 0;

      for(int iy = 0; i2 < n2; ++i2) {
         int n1 = rx[i2].length;
         copy(n1, 0, rx[i2], iy, ry);
         iy += n1;
      }

      return ry;
   }

   public static float[] flatten(float[][][] rx) {
      int n = 0;
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            n += rx[i3][i2].length;
         }
      }

      float[] ry = new float[n];
      int i3 = 0;

      for(int iy = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            int n1 = rx[i3][i2].length;
            copy(n1, 0, rx[i3][i2], iy, ry);
            iy += n1;
         }
      }

      return ry;
   }

   public static float[] cflatten(float[][] cx) {
      return flatten(cx);
   }

   public static float[] cflatten(float[][][] cx) {
      return flatten(cx);
   }

   public static double[] flatten(double[][] rx) {
      int n = 0;
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         n += rx[i2].length;
      }

      double[] ry = new double[n];
      int i2 = 0;

      for(int iy = 0; i2 < n2; ++i2) {
         int n1 = rx[i2].length;
         copy(n1, 0, rx[i2], iy, ry);
         iy += n1;
      }

      return ry;
   }

   public static double[] flatten(double[][][] rx) {
      int n = 0;
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            n += rx[i3][i2].length;
         }
      }

      double[] ry = new double[n];
      int i3 = 0;

      for(int iy = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            int n1 = rx[i3][i2].length;
            copy(n1, 0, rx[i3][i2], iy, ry);
            iy += n1;
         }
      }

      return ry;
   }

   public static double[] cflatten(double[][] cx) {
      return flatten(cx);
   }

   public static double[] cflatten(double[][][] cx) {
      return flatten(cx);
   }

   public static byte[][] reshape(int n1, int n2, byte[] rx) {
      byte[][] ry = new byte[n2][n1];
      int i2 = 0;

      for(int ix = 0; i2 < n2; ++i2) {
         copy(n1, ix, rx, 0, ry[i2]);
         ix += n1;
      }

      return ry;
   }

   public static byte[][][] reshape(int n1, int n2, int n3, byte[] rx) {
      byte[][][] ry = new byte[n3][n2][n1];
      int i3 = 0;

      for(int ix = 0; i3 < n3; ++i3) {
         for(int i2 = 0; i2 < n2; ++i2) {
            copy(n1, ix, rx, 0, ry[i3][i2]);
            ix += n1;
         }
      }

      return ry;
   }

   public static short[][] reshape(int n1, int n2, short[] rx) {
      short[][] ry = new short[n2][n1];
      int i2 = 0;

      for(int ix = 0; i2 < n2; ++i2) {
         copy(n1, ix, rx, 0, ry[i2]);
         ix += n1;
      }

      return ry;
   }

   public static short[][][] reshape(int n1, int n2, int n3, short[] rx) {
      short[][][] ry = new short[n3][n2][n1];
      int i3 = 0;

      for(int ix = 0; i3 < n3; ++i3) {
         for(int i2 = 0; i2 < n2; ++i2) {
            copy(n1, ix, rx, 0, ry[i3][i2]);
            ix += n1;
         }
      }

      return ry;
   }

   public static int[][] reshape(int n1, int n2, int[] rx) {
      int[][] ry = new int[n2][n1];
      int i2 = 0;

      for(int ix = 0; i2 < n2; ++i2) {
         copy(n1, ix, rx, 0, ry[i2]);
         ix += n1;
      }

      return ry;
   }

   public static int[][][] reshape(int n1, int n2, int n3, int[] rx) {
      int[][][] ry = new int[n3][n2][n1];
      int i3 = 0;

      for(int ix = 0; i3 < n3; ++i3) {
         for(int i2 = 0; i2 < n2; ++i2) {
            copy(n1, ix, rx, 0, ry[i3][i2]);
            ix += n1;
         }
      }

      return ry;
   }

   public static long[][] reshape(int n1, int n2, long[] rx) {
      long[][] ry = new long[n2][n1];
      int i2 = 0;

      for(int ix = 0; i2 < n2; ++i2) {
         copy(n1, ix, rx, 0, ry[i2]);
         ix += n1;
      }

      return ry;
   }

   public static long[][][] reshape(int n1, int n2, int n3, long[] rx) {
      long[][][] ry = new long[n3][n2][n1];
      int i3 = 0;

      for(int ix = 0; i3 < n3; ++i3) {
         for(int i2 = 0; i2 < n2; ++i2) {
            copy(n1, ix, rx, 0, ry[i3][i2]);
            ix += n1;
         }
      }

      return ry;
   }

   public static float[][] reshape(int n1, int n2, float[] rx) {
      float[][] ry = new float[n2][n1];
      int i2 = 0;

      for(int ix = 0; i2 < n2; ++i2) {
         copy(n1, ix, rx, 0, ry[i2]);
         ix += n1;
      }

      return ry;
   }

   public static float[][][] reshape(int n1, int n2, int n3, float[] rx) {
      float[][][] ry = new float[n3][n2][n1];
      int i3 = 0;

      for(int ix = 0; i3 < n3; ++i3) {
         for(int i2 = 0; i2 < n2; ++i2) {
            copy(n1, ix, rx, 0, ry[i3][i2]);
            ix += n1;
         }
      }

      return ry;
   }

   public static float[][] creshape(int n1, int n2, float[] cx) {
      return reshape(2 * n1, n2, cx);
   }

   public static float[][][] creshape(int n1, int n2, int n3, float[] cx) {
      return reshape(2 * n1, n2, n3, cx);
   }

   public static double[][] reshape(int n1, int n2, double[] rx) {
      double[][] ry = new double[n2][n1];
      int i2 = 0;

      for(int ix = 0; i2 < n2; ++i2) {
         copy(n1, ix, rx, 0, ry[i2]);
         ix += n1;
      }

      return ry;
   }

   public static double[][][] reshape(int n1, int n2, int n3, double[] rx) {
      double[][][] ry = new double[n3][n2][n1];
      int i3 = 0;

      for(int ix = 0; i3 < n3; ++i3) {
         for(int i2 = 0; i2 < n2; ++i2) {
            copy(n1, ix, rx, 0, ry[i3][i2]);
            ix += n1;
         }
      }

      return ry;
   }

   public static double[][] creshape(int n1, int n2, double[] cx) {
      return reshape(2 * n1, n2, cx);
   }

   public static double[][][] creshape(int n1, int n2, int n3, double[] cx) {
      return reshape(2 * n1, n2, n3, cx);
   }

   public static byte[][] transpose(byte[][] rx) {
      int n2 = rx.length;
      int n1 = rx[0].length;
      byte[][] ry = new byte[n1][n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1][i2] = rx[i2][i1];
         }
      }

      return ry;
   }

   public static short[][] transpose(short[][] rx) {
      int n2 = rx.length;
      int n1 = rx[0].length;
      short[][] ry = new short[n1][n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1][i2] = rx[i2][i1];
         }
      }

      return ry;
   }

   public static int[][] transpose(int[][] rx) {
      int n2 = rx.length;
      int n1 = rx[0].length;
      int[][] ry = new int[n1][n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1][i2] = rx[i2][i1];
         }
      }

      return ry;
   }

   public static long[][] transpose(long[][] rx) {
      int n2 = rx.length;
      int n1 = rx[0].length;
      long[][] ry = new long[n1][n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1][i2] = rx[i2][i1];
         }
      }

      return ry;
   }

   public static float[][] transpose(float[][] rx) {
      int n2 = rx.length;
      int n1 = rx[0].length;
      float[][] ry = new float[n1][n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1][i2] = rx[i2][i1];
         }
      }

      return ry;
   }

   public static float[][] ctranspose(float[][] cx) {
      int n2 = cx.length;
      int n1 = cx[0].length / 2;
      float[][] cy = new float[n1][2 * n2];
      int i2 = 0;

      for(int iy = 0; i2 < n2; iy += 2) {
         int i1 = 0;

         for(int ix = 0; i1 < n1; ix += 2) {
            cy[i1][iy] = cx[i2][ix];
            cy[i1][iy + 1] = cx[i2][ix + 1];
            ++i1;
         }

         ++i2;
      }

      return cy;
   }

   public static double[][] transpose(double[][] rx) {
      int n2 = rx.length;
      int n1 = rx[0].length;
      double[][] ry = new double[n1][n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i1][i2] = rx[i2][i1];
         }
      }

      return ry;
   }

   public static double[][] ctranspose(double[][] cx) {
      int n2 = cx.length;
      int n1 = cx[0].length / 2;
      double[][] cy = new double[n1][2 * n2];
      int i2 = 0;

      for(int iy = 0; i2 < n2; iy += 2) {
         int i1 = 0;

         for(int ix = 0; i1 < n1; ix += 2) {
            cy[i1][iy] = cx[i2][ix];
            cy[i1][iy + 1] = cx[i2][ix + 1];
            ++i1;
         }

         ++i2;
      }

      return cy;
   }

   public static boolean equal(byte[] rx, byte[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         if (rx[i1] != ry[i1]) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(byte[][] rx, byte[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         if (!equal(rx[i2], ry[i2])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(byte[][][] rx, byte[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (!equal(rx[i3], ry[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(short[] rx, short[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         if (rx[i1] != ry[i1]) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(short[][] rx, short[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         if (!equal(rx[i2], ry[i2])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(short[][][] rx, short[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (!equal(rx[i3], ry[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(int[] rx, int[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         if (rx[i1] != ry[i1]) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(int[][] rx, int[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         if (!equal(rx[i2], ry[i2])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(int[][][] rx, int[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (!equal(rx[i3], ry[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(long[] rx, long[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         if (rx[i1] != ry[i1]) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(long[][] rx, long[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         if (!equal(rx[i2], ry[i2])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(long[][][] rx, long[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (!equal(rx[i3], ry[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(float[] rx, float[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         if (rx[i1] != ry[i1]) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(float[][] rx, float[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         if (!equal(rx[i2], ry[i2])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(float[][][] rx, float[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (!equal(rx[i3], ry[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(float tolerance, float[] rx, float[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         if (!equal(tolerance, rx[i1], ry[i1])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(float tolerance, float[][] rx, float[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         if (!equal(tolerance, rx[i2], ry[i2])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(float tolerance, float[][][] rx, float[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (!equal(tolerance, rx[i3], ry[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean cequal(float[] cx, float[] cy) {
      return equal(cx, cy);
   }

   public static boolean cequal(float[][] cx, float[][] cy) {
      return equal(cx, cy);
   }

   public static boolean cequal(float[][][] cx, float[][][] cy) {
      return equal(cx, cy);
   }

   public static boolean cequal(float tolerance, float[] cx, float[] cy) {
      return equal(tolerance, cx, cy);
   }

   public static boolean cequal(float tolerance, float[][] cx, float[][] cy) {
      return equal(tolerance, cx, cy);
   }

   public static boolean cequal(float tolerance, float[][][] cx, float[][][] cy) {
      return equal(tolerance, cx, cy);
   }

   private static boolean equal(float tolerance, float ra, float rb) {
      return ra < rb ? rb - ra <= tolerance : ra - rb <= tolerance;
   }

   public static boolean equal(double[] rx, double[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         if (rx[i1] != ry[i1]) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(double[][] rx, double[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         if (!equal(rx[i2], ry[i2])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(double[][][] rx, double[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (!equal(rx[i3], ry[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(double tolerance, double[] rx, double[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         if (!equal(tolerance, rx[i1], ry[i1])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(double tolerance, double[][] rx, double[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         if (!equal(tolerance, rx[i2], ry[i2])) {
            return false;
         }
      }

      return true;
   }

   public static boolean equal(double tolerance, double[][][] rx, double[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (!equal(tolerance, rx[i3], ry[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean cequal(double[] cx, double[] cy) {
      return equal(cx, cy);
   }

   public static boolean cequal(double[][] cx, double[][] cy) {
      return equal(cx, cy);
   }

   public static boolean cequal(double[][][] cx, double[][][] cy) {
      return equal(cx, cy);
   }

   public static boolean cequal(double tolerance, double[] cx, double[] cy) {
      return equal(tolerance, cx, cy);
   }

   public static boolean cequal(double tolerance, double[][] cx, double[][] cy) {
      return equal(tolerance, cx, cy);
   }

   public static boolean cequal(double tolerance, double[][][] cx, double[][][] cy) {
      return equal(tolerance, cx, cy);
   }

   private static boolean equal(double tolerance, double ra, double rb) {
      return ra < rb ? rb - ra <= tolerance : ra - rb <= tolerance;
   }

   public static boolean isRegular(byte[][] a) {
      int n2 = a.length;
      int n1 = a[0].length;

      for(int i2 = 1; i2 < n2; ++i2) {
         if (a[i2].length != n1) {
            return false;
         }
      }

      return true;
   }

   public static boolean isRegular(byte[][][] a) {
      int n3 = a.length;
      int n2 = a[0].length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (a[i3].length != n2 || !isRegular(a[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean isRegular(short[][] a) {
      int n2 = a.length;
      int n1 = a[0].length;

      for(int i2 = 1; i2 < n2; ++i2) {
         if (a[i2].length != n1) {
            return false;
         }
      }

      return true;
   }

   public static boolean isRegular(short[][][] a) {
      int n3 = a.length;
      int n2 = a[0].length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (a[i3].length != n2 || !isRegular(a[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean isRegular(int[][] a) {
      int n2 = a.length;
      int n1 = a[0].length;

      for(int i2 = 1; i2 < n2; ++i2) {
         if (a[i2].length != n1) {
            return false;
         }
      }

      return true;
   }

   public static boolean isRegular(int[][][] a) {
      int n3 = a.length;
      int n2 = a[0].length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (a[i3].length != n2 || !isRegular(a[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean isRegular(float[][] a) {
      int n2 = a.length;
      int n1 = a[0].length;

      for(int i2 = 1; i2 < n2; ++i2) {
         if (a[i2].length != n1) {
            return false;
         }
      }

      return true;
   }

   public static boolean isRegular(float[][][] a) {
      int n3 = a.length;
      int n2 = a[0].length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (a[i3].length != n2 || !isRegular(a[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean isRegular(double[][] a) {
      int n2 = a.length;
      int n1 = a[0].length;

      for(int i2 = 1; i2 < n2; ++i2) {
         if (a[i2].length != n1) {
            return false;
         }
      }

      return true;
   }

   public static boolean isRegular(double[][][] a) {
      int n3 = a.length;
      int n2 = a[0].length;

      for(int i3 = 0; i3 < n3; ++i3) {
         if (a[i3].length != n2 || !isRegular(a[i3])) {
            return false;
         }
      }

      return true;
   }

   public static boolean isIncreasing(byte[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] >= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isDecreasing(byte[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] <= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isMonotonic(byte[] a) {
      return isIncreasing(a) || isDecreasing(a);
   }

   public static boolean isIncreasing(short[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] >= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isDecreasing(short[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] <= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isMonotonic(short[] a) {
      return isIncreasing(a) || isDecreasing(a);
   }

   public static boolean isIncreasing(int[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] >= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isDecreasing(int[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] <= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isMonotonic(int[] a) {
      return isIncreasing(a) || isDecreasing(a);
   }

   public static boolean isIncreasing(long[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] >= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isDecreasing(long[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] <= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isMonotonic(long[] a) {
      return isIncreasing(a) || isDecreasing(a);
   }

   public static boolean isIncreasing(float[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] >= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isDecreasing(float[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] <= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isMonotonic(float[] a) {
      return isIncreasing(a) || isDecreasing(a);
   }

   public static boolean isIncreasing(double[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] >= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isDecreasing(double[] a) {
      int n = a.length;
      if (n > 1) {
         for(int i = 1; i < n; ++i) {
            if (a[i - 1] <= a[i]) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isMonotonic(double[] a) {
      return isIncreasing(a) || isDecreasing(a);
   }

   public static void quickSort(byte[] a) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, 0, n - 1, m);
      }
   }

   public static void quickIndexSort(byte[] a, int[] i) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, i, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, i, 0, n - 1, m);
      }
   }

   public static void quickPartialSort(int k, byte[] a) {
      int n = a.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, p, q);
   }

   public static void quickPartialIndexSort(int k, byte[] a, int[] i) {
      int n = i.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, i, p, q);
   }

   public static void quickSort(short[] a) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, 0, n - 1, m);
      }
   }

   public static void quickIndexSort(short[] a, int[] i) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, i, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, i, 0, n - 1, m);
      }
   }

   public static void quickPartialSort(int k, short[] a) {
      int n = a.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, p, q);
   }

   public static void quickPartialIndexSort(int k, short[] a, int[] i) {
      int n = i.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, i, p, q);
   }

   public static void quickSort(int[] a) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, 0, n - 1, m);
      }
   }

   public static void quickIndexSort(int[] a, int[] i) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, i, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, i, 0, n - 1, m);
      }
   }

   public static void quickPartialSort(int k, int[] a) {
      int n = a.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, p, q);
   }

   public static void quickPartialIndexSort(int k, int[] a, int[] i) {
      int n = i.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, i, p, q);
   }

   public static void quickSort(long[] a) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, 0, n - 1, m);
      }
   }

   public static void quickIndexSort(long[] a, int[] i) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, i, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, i, 0, n - 1, m);
      }
   }

   public static void quickPartialSort(int k, long[] a) {
      int n = a.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, p, q);
   }

   public static void quickPartialIndexSort(int k, long[] a, int[] i) {
      int n = i.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, i, p, q);
   }

   public static void quickSort(float[] a) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, 0, n - 1, m);
      }
   }

   public static void quickIndexSort(float[] a, int[] i) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, i, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, i, 0, n - 1, m);
      }
   }

   public static void quickPartialSort(int k, float[] a) {
      int n = a.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, p, q);
   }

   public static void quickPartialIndexSort(int k, float[] a, int[] i) {
      int n = i.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, i, p, q);
   }

   public static void quickSort(double[] a) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, 0, n - 1, m);
      }
   }

   public static void quickIndexSort(double[] a, int[] i) {
      int n = a.length;
      if (n < 7) {
         insertionSort(a, i, 0, n - 1);
      } else {
         int[] m = new int[2];
         quickSort(a, i, 0, n - 1, m);
      }
   }

   public static void quickPartialSort(int k, double[] a) {
      int n = a.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, p, q);
   }

   public static void quickPartialIndexSort(int k, double[] a, int[] i) {
      int n = i.length;
      int p = 0;
      int q = n - 1;
      int[] m = n > 7 ? new int[2] : null;

      while(q - p >= 7) {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         if (k < m[0]) {
            q = m[0] - 1;
         } else {
            if (k <= m[1]) {
               return;
            }

            p = m[1] + 1;
         }
      }

      insertionSort(a, i, p, q);
   }

   private static int med3(byte[] a, int i, int j, int k) {
      return a[i] < a[j] ? (a[j] < a[k] ? j : (a[i] < a[k] ? k : i)) : (a[j] > a[k] ? j : (a[i] > a[k] ? k : i));
   }

   private static int med3(byte[] a, int[] i, int j, int k, int l) {
      return a[i[j]] < a[i[k]] ? (a[i[k]] < a[i[l]] ? k : (a[i[j]] < a[i[l]] ? l : j)) : (a[i[k]] > a[i[l]] ? k : (a[i[j]] > a[i[l]] ? l : j));
   }

   private static void swap(byte[] a, int i, int j) {
      byte ai = a[i];
      a[i] = a[j];
      a[j] = ai;
   }

   private static void swap(byte[] a, int i, int j, int n) {
      while(n > 0) {
         byte ai = a[i];
         a[i++] = a[j];
         a[j++] = ai;
         --n;
      }
   }

   private static void insertionSort(byte[] a, int p, int q) {
      for(int i = p; i <= q; ++i) {
         for(int j = i; j > p && a[j - 1] > a[j]; --j) {
            swap(a, j, j - 1);
         }
      }
   }

   private static void insertionSort(byte[] a, int[] i, int p, int q) {
      for(int j = p; j <= q; ++j) {
         for(int k = j; k > p && a[i[k - 1]] > a[i[k]]; --k) {
            swap(i, k, k - 1);
         }
      }
   }

   private static void quickSort(byte[] a, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, s + 1, q, m);
         }
      }
   }

   private static void quickSort(byte[] a, int[] i, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, i, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, i, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, i, s + 1, q, m);
         }
      }
   }

   private static void quickPartition(byte[] x, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, p, p + s, p + 2 * s);
            k = med3(x, k - s, k, k + s);
            l = med3(x, q - 2 * s, q - s, q);
         }

         k = med3(x, j, k, l);
      }

      byte y = x[k];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || x[b] > y) {
            for(; c >= b && x[c] >= y; --c) {
               if (x[c] == y) {
                  swap(x, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(x, p, b - r, r);
               swap(x, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(x, b, c);
            ++b;
            --c;
         }

         if (x[b] == y) {
            swap(x, a++, b);
         }

         ++b;
      }
   }

   private static void quickPartition(byte[] x, int[] i, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, i, p, p + s, p + 2 * s);
            k = med3(x, i, k - s, k, k + s);
            l = med3(x, i, q - 2 * s, q - s, q);
         }

         k = med3(x, i, j, k, l);
      }

      byte y = x[i[k]];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || x[i[b]] > y) {
            for(; c >= b && x[i[c]] >= y; --c) {
               if (x[i[c]] == y) {
                  swap(i, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(i, p, b - r, r);
               swap(i, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(i, b, c);
            ++b;
            --c;
         }

         if (x[i[b]] == y) {
            swap(i, a++, b);
         }

         ++b;
      }
   }

   private static int med3(short[] a, int i, int j, int k) {
      return a[i] < a[j] ? (a[j] < a[k] ? j : (a[i] < a[k] ? k : i)) : (a[j] > a[k] ? j : (a[i] > a[k] ? k : i));
   }

   private static int med3(short[] a, int[] i, int j, int k, int l) {
      return a[i[j]] < a[i[k]] ? (a[i[k]] < a[i[l]] ? k : (a[i[j]] < a[i[l]] ? l : j)) : (a[i[k]] > a[i[l]] ? k : (a[i[j]] > a[i[l]] ? l : j));
   }

   private static void swap(short[] a, int i, int j) {
      short ai = a[i];
      a[i] = a[j];
      a[j] = ai;
   }

   private static void swap(short[] a, int i, int j, int n) {
      while(n > 0) {
         short ai = a[i];
         a[i++] = a[j];
         a[j++] = ai;
         --n;
      }
   }

   private static void insertionSort(short[] a, int p, int q) {
      for(int i = p; i <= q; ++i) {
         for(int j = i; j > p && a[j - 1] > a[j]; --j) {
            swap(a, j, j - 1);
         }
      }
   }

   private static void insertionSort(short[] a, int[] i, int p, int q) {
      for(int j = p; j <= q; ++j) {
         for(int k = j; k > p && a[i[k - 1]] > a[i[k]]; --k) {
            swap(i, k, k - 1);
         }
      }
   }

   private static void quickSort(short[] a, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, s + 1, q, m);
         }
      }
   }

   private static void quickSort(short[] a, int[] i, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, i, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, i, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, i, s + 1, q, m);
         }
      }
   }

   private static void quickPartition(short[] x, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, p, p + s, p + 2 * s);
            k = med3(x, k - s, k, k + s);
            l = med3(x, q - 2 * s, q - s, q);
         }

         k = med3(x, j, k, l);
      }

      short y = x[k];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || x[b] > y) {
            for(; c >= b && x[c] >= y; --c) {
               if (x[c] == y) {
                  swap(x, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(x, p, b - r, r);
               swap(x, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(x, b, c);
            ++b;
            --c;
         }

         if (x[b] == y) {
            swap(x, a++, b);
         }

         ++b;
      }
   }

   private static void quickPartition(short[] x, int[] i, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, i, p, p + s, p + 2 * s);
            k = med3(x, i, k - s, k, k + s);
            l = med3(x, i, q - 2 * s, q - s, q);
         }

         k = med3(x, i, j, k, l);
      }

      short y = x[i[k]];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || x[i[b]] > y) {
            for(; c >= b && x[i[c]] >= y; --c) {
               if (x[i[c]] == y) {
                  swap(i, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(i, p, b - r, r);
               swap(i, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(i, b, c);
            ++b;
            --c;
         }

         if (x[i[b]] == y) {
            swap(i, a++, b);
         }

         ++b;
      }
   }

   private static int med3(int[] a, int i, int j, int k) {
      return a[i] < a[j] ? (a[j] < a[k] ? j : (a[i] < a[k] ? k : i)) : (a[j] > a[k] ? j : (a[i] > a[k] ? k : i));
   }

   private static int med3(int[] a, int[] i, int j, int k, int l) {
      return a[i[j]] < a[i[k]] ? (a[i[k]] < a[i[l]] ? k : (a[i[j]] < a[i[l]] ? l : j)) : (a[i[k]] > a[i[l]] ? k : (a[i[j]] > a[i[l]] ? l : j));
   }

   private static void swap(int[] a, int i, int j) {
      int ai = a[i];
      a[i] = a[j];
      a[j] = ai;
   }

   private static void swap(int[] a, int i, int j, int n) {
      while(n > 0) {
         int ai = a[i];
         a[i++] = a[j];
         a[j++] = ai;
         --n;
      }
   }

   private static void insertionSort(int[] a, int p, int q) {
      for(int i = p; i <= q; ++i) {
         for(int j = i; j > p && a[j - 1] > a[j]; --j) {
            swap(a, j, j - 1);
         }
      }
   }

   private static void insertionSort(int[] a, int[] i, int p, int q) {
      for(int j = p; j <= q; ++j) {
         for(int k = j; k > p && a[i[k - 1]] > a[i[k]]; --k) {
            swap(i, k, k - 1);
         }
      }
   }

   private static void quickSort(int[] a, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, s + 1, q, m);
         }
      }
   }

   private static void quickSort(int[] a, int[] i, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, i, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, i, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, i, s + 1, q, m);
         }
      }
   }

   private static void quickPartition(int[] x, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, p, p + s, p + 2 * s);
            k = med3(x, k - s, k, k + s);
            l = med3(x, q - 2 * s, q - s, q);
         }

         k = med3(x, j, k, l);
      }

      int y = x[k];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || x[b] > y) {
            for(; c >= b && x[c] >= y; --c) {
               if (x[c] == y) {
                  swap(x, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(x, p, b - r, r);
               swap(x, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(x, b, c);
            ++b;
            --c;
         }

         if (x[b] == y) {
            swap(x, a++, b);
         }

         ++b;
      }
   }

   private static void quickPartition(int[] x, int[] i, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, i, p, p + s, p + 2 * s);
            k = med3(x, i, k - s, k, k + s);
            l = med3(x, i, q - 2 * s, q - s, q);
         }

         k = med3(x, i, j, k, l);
      }

      int y = x[i[k]];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || x[i[b]] > y) {
            for(; c >= b && x[i[c]] >= y; --c) {
               if (x[i[c]] == y) {
                  swap(i, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(i, p, b - r, r);
               swap(i, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(i, b, c);
            ++b;
            --c;
         }

         if (x[i[b]] == y) {
            swap(i, a++, b);
         }

         ++b;
      }
   }

   private static int med3(long[] a, int i, int j, int k) {
      return a[i] < a[j] ? (a[j] < a[k] ? j : (a[i] < a[k] ? k : i)) : (a[j] > a[k] ? j : (a[i] > a[k] ? k : i));
   }

   private static int med3(long[] a, int[] i, int j, int k, int l) {
      return a[i[j]] < a[i[k]] ? (a[i[k]] < a[i[l]] ? k : (a[i[j]] < a[i[l]] ? l : j)) : (a[i[k]] > a[i[l]] ? k : (a[i[j]] > a[i[l]] ? l : j));
   }

   private static void swap(long[] a, int i, int j) {
      long ai = a[i];
      a[i] = a[j];
      a[j] = ai;
   }

   private static void swap(long[] a, int i, int j, int n) {
      while(n > 0) {
         long ai = a[i];
         a[i++] = a[j];
         a[j++] = ai;
         --n;
      }
   }

   private static void insertionSort(long[] a, int p, int q) {
      for(int i = p; i <= q; ++i) {
         for(int j = i; j > p && a[j - 1] > a[j]; --j) {
            swap(a, j, j - 1);
         }
      }
   }

   private static void insertionSort(long[] a, int[] i, int p, int q) {
      for(int j = p; j <= q; ++j) {
         for(int k = j; k > p && a[i[k - 1]] > a[i[k]]; --k) {
            swap(i, k, k - 1);
         }
      }
   }

   private static void quickSort(long[] a, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, s + 1, q, m);
         }
      }
   }

   private static void quickSort(long[] a, int[] i, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, i, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, i, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, i, s + 1, q, m);
         }
      }
   }

   private static void quickPartition(long[] x, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, p, p + s, p + 2 * s);
            k = med3(x, k - s, k, k + s);
            l = med3(x, q - 2 * s, q - s, q);
         }

         k = med3(x, j, k, l);
      }

      long y = x[k];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || x[b] > y) {
            for(; c >= b && x[c] >= y; --c) {
               if (x[c] == y) {
                  swap(x, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(x, p, b - r, r);
               swap(x, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(x, b, c);
            ++b;
            --c;
         }

         if (x[b] == y) {
            swap(x, a++, b);
         }

         ++b;
      }
   }

   private static void quickPartition(long[] x, int[] i, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, i, p, p + s, p + 2 * s);
            k = med3(x, i, k - s, k, k + s);
            l = med3(x, i, q - 2 * s, q - s, q);
         }

         k = med3(x, i, j, k, l);
      }

      long y = x[i[k]];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || x[i[b]] > y) {
            for(; c >= b && x[i[c]] >= y; --c) {
               if (x[i[c]] == y) {
                  swap(i, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(i, p, b - r, r);
               swap(i, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(i, b, c);
            ++b;
            --c;
         }

         if (x[i[b]] == y) {
            swap(i, a++, b);
         }

         ++b;
      }
   }

   private static int med3(float[] a, int i, int j, int k) {
      return a[i] < a[j] ? (a[j] < a[k] ? j : (a[i] < a[k] ? k : i)) : (a[j] > a[k] ? j : (a[i] > a[k] ? k : i));
   }

   private static int med3(float[] a, int[] i, int j, int k, int l) {
      return a[i[j]] < a[i[k]] ? (a[i[k]] < a[i[l]] ? k : (a[i[j]] < a[i[l]] ? l : j)) : (a[i[k]] > a[i[l]] ? k : (a[i[j]] > a[i[l]] ? l : j));
   }

   private static void swap(float[] a, int i, int j) {
      float ai = a[i];
      a[i] = a[j];
      a[j] = ai;
   }

   private static void swap(float[] a, int i, int j, int n) {
      while(n > 0) {
         float ai = a[i];
         a[i++] = a[j];
         a[j++] = ai;
         --n;
      }
   }

   private static void insertionSort(float[] a, int p, int q) {
      for(int i = p; i <= q; ++i) {
         for(int j = i; j > p && a[j - 1] > a[j]; --j) {
            swap(a, j, j - 1);
         }
      }
   }

   private static void insertionSort(float[] a, int[] i, int p, int q) {
      for(int j = p; j <= q; ++j) {
         for(int k = j; k > p && a[i[k - 1]] > a[i[k]]; --k) {
            swap(i, k, k - 1);
         }
      }
   }

   private static void quickSort(float[] a, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, s + 1, q, m);
         }
      }
   }

   private static void quickSort(float[] a, int[] i, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, i, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, i, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, i, s + 1, q, m);
         }
      }
   }

   private static void quickPartition(float[] x, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, p, p + s, p + 2 * s);
            k = med3(x, k - s, k, k + s);
            l = med3(x, q - 2 * s, q - s, q);
         }

         k = med3(x, j, k, l);
      }

      float y = x[k];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || !(x[b] <= y)) {
            for(; c >= b && x[c] >= y; --c) {
               if (x[c] == y) {
                  swap(x, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(x, p, b - r, r);
               swap(x, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(x, b, c);
            ++b;
            --c;
         }

         if (x[b] == y) {
            swap(x, a++, b);
         }

         ++b;
      }
   }

   private static void quickPartition(float[] x, int[] i, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, i, p, p + s, p + 2 * s);
            k = med3(x, i, k - s, k, k + s);
            l = med3(x, i, q - 2 * s, q - s, q);
         }

         k = med3(x, i, j, k, l);
      }

      float y = x[i[k]];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || !(x[i[b]] <= y)) {
            for(; c >= b && x[i[c]] >= y; --c) {
               if (x[i[c]] == y) {
                  swap(i, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(i, p, b - r, r);
               swap(i, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(i, b, c);
            ++b;
            --c;
         }

         if (x[i[b]] == y) {
            swap(i, a++, b);
         }

         ++b;
      }
   }

   private static int med3(double[] a, int i, int j, int k) {
      return a[i] < a[j] ? (a[j] < a[k] ? j : (a[i] < a[k] ? k : i)) : (a[j] > a[k] ? j : (a[i] > a[k] ? k : i));
   }

   private static int med3(double[] a, int[] i, int j, int k, int l) {
      return a[i[j]] < a[i[k]] ? (a[i[k]] < a[i[l]] ? k : (a[i[j]] < a[i[l]] ? l : j)) : (a[i[k]] > a[i[l]] ? k : (a[i[j]] > a[i[l]] ? l : j));
   }

   private static void swap(double[] a, int i, int j) {
      double ai = a[i];
      a[i] = a[j];
      a[j] = ai;
   }

   private static void swap(double[] a, int i, int j, int n) {
      while(n > 0) {
         double ai = a[i];
         a[i++] = a[j];
         a[j++] = ai;
         --n;
      }
   }

   private static void insertionSort(double[] a, int p, int q) {
      for(int i = p; i <= q; ++i) {
         for(int j = i; j > p && a[j - 1] > a[j]; --j) {
            swap(a, j, j - 1);
         }
      }
   }

   private static void insertionSort(double[] a, int[] i, int p, int q) {
      for(int j = p; j <= q; ++j) {
         for(int k = j; k > p && a[i[k - 1]] > a[i[k]]; --k) {
            swap(i, k, k - 1);
         }
      }
   }

   private static void quickSort(double[] a, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, s + 1, q, m);
         }
      }
   }

   private static void quickSort(double[] a, int[] i, int p, int q, int[] m) {
      if (q - p <= 7) {
         insertionSort(a, i, p, q);
      } else {
         m[0] = p;
         m[1] = q;
         quickPartition(a, i, m);
         int r = m[0];
         int s = m[1];
         if (p < r - 1) {
            quickSort(a, i, p, r - 1, m);
         }

         if (q > s + 1) {
            quickSort(a, i, s + 1, q, m);
         }
      }
   }

   private static void quickPartition(double[] x, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, p, p + s, p + 2 * s);
            k = med3(x, k - s, k, k + s);
            l = med3(x, q - 2 * s, q - s, q);
         }

         k = med3(x, j, k, l);
      }

      double y = x[k];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || !(x[b] <= y)) {
            for(; c >= b && x[c] >= y; --c) {
               if (x[c] == y) {
                  swap(x, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(x, p, b - r, r);
               swap(x, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(x, b, c);
            ++b;
            --c;
         }

         if (x[b] == y) {
            swap(x, a++, b);
         }

         ++b;
      }
   }

   private static void quickPartition(double[] x, int[] i, int[] m) {
      int p = m[0];
      int q = m[1];
      int n = q - p + 1;
      int k = (p + q) / 2;
      if (n > 7) {
         int j = p;
         int l = q;
         if (n > 40) {
            int s = n / 8;
            j = med3(x, i, p, p + s, p + 2 * s);
            k = med3(x, i, k - s, k, k + s);
            l = med3(x, i, q - 2 * s, q - s, q);
         }

         k = med3(x, i, j, k, l);
      }

      double y = x[i[k]];
      int a = p;
      int b = p;
      int c = q;
      int d = q;

      while(true) {
         while(b > c || !(x[i[b]] <= y)) {
            for(; c >= b && x[i[c]] >= y; --c) {
               if (x[i[c]] == y) {
                  swap(i, c, d--);
               }
            }

            if (b > c) {
               int r = Math.min(a - p, b - a);
               int s = Math.min(d - c, q - d);
               int t = q + 1;
               swap(i, p, b - r, r);
               swap(i, b, t - s, s);
               m[0] = p + (b - a);
               m[1] = q - (d - c);
               return;
            }

            swap(i, b, c);
            ++b;
            --c;
         }

         if (x[i[b]] == y) {
            swap(i, a++, b);
         }

         ++b;
      }
   }

   public static int binarySearch(byte[] a, byte x) {
      return binarySearch(a, x, a.length);
   }

   public static int binarySearch(byte[] a, byte x, int i) {
      int n = a.length;
      int nm1 = n - 1;
      int low = 0;
      int high = nm1;
      boolean increasing = n < 2 || a[0] < a[1];
      if (i < n) {
         high = 0 <= i ? i : -(i + 1);
         low = high - 1;
         int step = 1;
         if (increasing) {
            while(0 < low && x < a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] < x) {
               low = high;
               high += step;
               step += step;
            }
         } else {
            while(0 < low && x > a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] > x) {
               low = high;
               high += step;
               step += step;
            }
         }

         if (low < 0) {
            low = 0;
         }

         if (high > nm1) {
            high = nm1;
         }
      }

      if (increasing) {
         while(low <= high) {
            int mid = low + high >> 1;
            byte amid = a[mid];
            if (amid < x) {
               low = mid + 1;
            } else {
               if (amid <= x) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      } else {
         while(low <= high) {
            int mid = low + high >> 1;
            byte amid = a[mid];
            if (amid > x) {
               low = mid + 1;
            } else {
               if (amid >= x) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      }

      return -(low + 1);
   }

   public static int binarySearch(short[] a, short x) {
      return binarySearch(a, x, a.length);
   }

   public static int binarySearch(short[] a, short x, int i) {
      int n = a.length;
      int nm1 = n - 1;
      int low = 0;
      int high = nm1;
      boolean increasing = n < 2 || a[0] < a[1];
      if (i < n) {
         high = 0 <= i ? i : -(i + 1);
         low = high - 1;
         int step = 1;
         if (increasing) {
            while(0 < low && x < a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] < x) {
               low = high;
               high += step;
               step += step;
            }
         } else {
            while(0 < low && x > a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] > x) {
               low = high;
               high += step;
               step += step;
            }
         }

         if (low < 0) {
            low = 0;
         }

         if (high > nm1) {
            high = nm1;
         }
      }

      if (increasing) {
         while(low <= high) {
            int mid = low + high >> 1;
            short amid = a[mid];
            if (amid < x) {
               low = mid + 1;
            } else {
               if (amid <= x) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      } else {
         while(low <= high) {
            int mid = low + high >> 1;
            short amid = a[mid];
            if (amid > x) {
               low = mid + 1;
            } else {
               if (amid >= x) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      }

      return -(low + 1);
   }

   public static int binarySearch(int[] a, int x) {
      return binarySearch(a, x, a.length);
   }

   public static int binarySearch(int[] a, int x, int i) {
      int n = a.length;
      int nm1 = n - 1;
      int low = 0;
      int high = nm1;
      boolean increasing = n < 2 || a[0] < a[1];
      if (i < n) {
         high = 0 <= i ? i : -(i + 1);
         low = high - 1;
         int step = 1;
         if (increasing) {
            while(0 < low && x < a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] < x) {
               low = high;
               high += step;
               step += step;
            }
         } else {
            while(0 < low && x > a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] > x) {
               low = high;
               high += step;
               step += step;
            }
         }

         if (low < 0) {
            low = 0;
         }

         if (high > nm1) {
            high = nm1;
         }
      }

      if (increasing) {
         while(low <= high) {
            int mid = low + high >> 1;
            int amid = a[mid];
            if (amid < x) {
               low = mid + 1;
            } else {
               if (amid <= x) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      } else {
         while(low <= high) {
            int mid = low + high >> 1;
            int amid = a[mid];
            if (amid > x) {
               low = mid + 1;
            } else {
               if (amid >= x) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      }

      return -(low + 1);
   }

   public static int binarySearch(long[] a, long x) {
      return binarySearch(a, x, a.length);
   }

   public static int binarySearch(long[] a, long x, int i) {
      int n = a.length;
      int nm1 = n - 1;
      int low = 0;
      int high = nm1;
      boolean increasing = n < 2 || a[0] < a[1];
      if (i < n) {
         high = 0 <= i ? i : -(i + 1);
         low = high - 1;
         int step = 1;
         if (increasing) {
            while(0 < low && x < a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] < x) {
               low = high;
               high += step;
               step += step;
            }
         } else {
            while(0 < low && x > a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] > x) {
               low = high;
               high += step;
               step += step;
            }
         }

         if (low < 0) {
            low = 0;
         }

         if (high > nm1) {
            high = nm1;
         }
      }

      if (increasing) {
         while(low <= high) {
            int mid = low + high >> 1;
            long amid = a[mid];
            if (amid < x) {
               low = mid + 1;
            } else {
               if (amid <= x) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      } else {
         while(low <= high) {
            int mid = low + high >> 1;
            long amid = a[mid];
            if (amid > x) {
               low = mid + 1;
            } else {
               if (amid >= x) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      }

      return -(low + 1);
   }

   public static int binarySearch(float[] a, float x) {
      return binarySearch(a, x, a.length);
   }

   public static int binarySearch(float[] a, float x, int i) {
      int n = a.length;
      int nm1 = n - 1;
      int low = 0;
      int high = nm1;
      boolean increasing = n < 2 || a[0] < a[1];
      if (i < n) {
         high = 0 <= i ? i : -(i + 1);
         low = high - 1;
         int step = 1;
         if (increasing) {
            while(0 < low && x < a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] < x) {
               low = high;
               high += step;
               step += step;
            }
         } else {
            while(0 < low && x > a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] > x) {
               low = high;
               high += step;
               step += step;
            }
         }

         if (low < 0) {
            low = 0;
         }

         if (high > nm1) {
            high = nm1;
         }
      }

      if (increasing) {
         while(low <= high) {
            int mid = low + high >> 1;
            float amid = a[mid];
            if (amid < x) {
               low = mid + 1;
            } else {
               if (!(amid > x)) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      } else {
         while(low <= high) {
            int mid = low + high >> 1;
            float amid = a[mid];
            if (amid > x) {
               low = mid + 1;
            } else {
               if (!(amid < x)) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      }

      return -(low + 1);
   }

   public static int binarySearch(double[] a, double x) {
      return binarySearch(a, x, a.length);
   }

   public static int binarySearch(double[] a, double x, int i) {
      int n = a.length;
      int nm1 = n - 1;
      int low = 0;
      int high = nm1;
      boolean increasing = n < 2 || a[0] < a[1];
      if (i < n) {
         high = 0 <= i ? i : -(i + 1);
         low = high - 1;
         int step = 1;
         if (increasing) {
            while(0 < low && x < a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] < x) {
               low = high;
               high += step;
               step += step;
            }
         } else {
            while(0 < low && x > a[low]) {
               high = low;
               low -= step;
               step += step;
            }

            while(high < nm1 && a[high] > x) {
               low = high;
               high += step;
               step += step;
            }
         }

         if (low < 0) {
            low = 0;
         }

         if (high > nm1) {
            high = nm1;
         }
      }

      if (increasing) {
         while(low <= high) {
            int mid = low + high >> 1;
            double amid = a[mid];
            if (amid < x) {
               low = mid + 1;
            } else {
               if (!(amid > x)) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      } else {
         while(low <= high) {
            int mid = low + high >> 1;
            double amid = a[mid];
            if (amid > x) {
               low = mid + 1;
            } else {
               if (!(amid < x)) {
                  return mid;
               }

               high = mid - 1;
            }
         }
      }

      return -(low + 1);
   }

   public static float[] add(float[] rx, float[] ry) {
      return _add.apply(rx, ry);
   }

   public static float[] add(float ra, float[] ry) {
      return _add.apply(ra, ry);
   }

   public static float[] add(float[] rx, float rb) {
      return _add.apply(rx, rb);
   }

   public static float[][] add(float[][] rx, float[][] ry) {
      return _add.apply(rx, ry);
   }

   public static float[][] add(float ra, float[][] ry) {
      return _add.apply(ra, ry);
   }

   public static float[][] add(float[][] rx, float rb) {
      return _add.apply(rx, rb);
   }

   public static float[][][] add(float[][][] rx, float[][][] ry) {
      return _add.apply(rx, ry);
   }

   public static float[][][] add(float ra, float[][][] ry) {
      return _add.apply(ra, ry);
   }

   public static float[][][] add(float[][][] rx, float rb) {
      return _add.apply(rx, rb);
   }

   public static void add(float[] rx, float[] ry, float[] rz) {
      _add.apply(rx, ry, rz);
   }

   public static void add(float ra, float[] ry, float[] rz) {
      _add.apply(ra, ry, rz);
   }

   public static void add(float[] rx, float rb, float[] rz) {
      _add.apply(rx, rb, rz);
   }

   public static void add(float[][] rx, float[][] ry, float[][] rz) {
      _add.apply(rx, ry, rz);
   }

   public static void add(float ra, float[][] ry, float[][] rz) {
      _add.apply(ra, ry, rz);
   }

   public static void add(float[][] rx, float rb, float[][] rz) {
      _add.apply(rx, rb, rz);
   }

   public static void add(float[][][] rx, float[][][] ry, float[][][] rz) {
      _add.apply(rx, ry, rz);
   }

   public static void add(float ra, float[][][] ry, float[][][] rz) {
      _add.apply(ra, ry, rz);
   }

   public static void add(float[][][] rx, float rb, float[][][] rz) {
      _add.apply(rx, rb, rz);
   }

   public static float[] sub(float[] rx, float[] ry) {
      return _sub.apply(rx, ry);
   }

   public static float[] sub(float ra, float[] ry) {
      return _sub.apply(ra, ry);
   }

   public static float[] sub(float[] rx, float rb) {
      return _sub.apply(rx, rb);
   }

   public static float[][] sub(float[][] rx, float[][] ry) {
      return _sub.apply(rx, ry);
   }

   public static float[][] sub(float ra, float[][] ry) {
      return _sub.apply(ra, ry);
   }

   public static float[][] sub(float[][] rx, float rb) {
      return _sub.apply(rx, rb);
   }

   public static float[][][] sub(float[][][] rx, float[][][] ry) {
      return _sub.apply(rx, ry);
   }

   public static float[][][] sub(float ra, float[][][] ry) {
      return _sub.apply(ra, ry);
   }

   public static float[][][] sub(float[][][] rx, float rb) {
      return _sub.apply(rx, rb);
   }

   public static void sub(float[] rx, float[] ry, float[] rz) {
      _sub.apply(rx, ry, rz);
   }

   public static void sub(float ra, float[] ry, float[] rz) {
      _sub.apply(ra, ry, rz);
   }

   public static void sub(float[] rx, float rb, float[] rz) {
      _sub.apply(rx, rb, rz);
   }

   public static void sub(float[][] rx, float[][] ry, float[][] rz) {
      _sub.apply(rx, ry, rz);
   }

   public static void sub(float ra, float[][] ry, float[][] rz) {
      _sub.apply(ra, ry, rz);
   }

   public static void sub(float[][] rx, float rb, float[][] rz) {
      _sub.apply(rx, rb, rz);
   }

   public static void sub(float[][][] rx, float[][][] ry, float[][][] rz) {
      _sub.apply(rx, ry, rz);
   }

   public static void sub(float ra, float[][][] ry, float[][][] rz) {
      _sub.apply(ra, ry, rz);
   }

   public static void sub(float[][][] rx, float rb, float[][][] rz) {
      _sub.apply(rx, rb, rz);
   }

   public static float[] mul(float[] rx, float[] ry) {
      return _mul.apply(rx, ry);
   }

   public static float[] mul(float ra, float[] ry) {
      return _mul.apply(ra, ry);
   }

   public static float[] mul(float[] rx, float rb) {
      return _mul.apply(rx, rb);
   }

   public static float[][] mul(float[][] rx, float[][] ry) {
      return _mul.apply(rx, ry);
   }

   public static float[][] mul(float ra, float[][] ry) {
      return _mul.apply(ra, ry);
   }

   public static float[][] mul(float[][] rx, float rb) {
      return _mul.apply(rx, rb);
   }

   public static float[][][] mul(float[][][] rx, float[][][] ry) {
      return _mul.apply(rx, ry);
   }

   public static float[][][] mul(float ra, float[][][] ry) {
      return _mul.apply(ra, ry);
   }

   public static float[][][] mul(float[][][] rx, float rb) {
      return _mul.apply(rx, rb);
   }

   public static void mul(float[] rx, float[] ry, float[] rz) {
      _mul.apply(rx, ry, rz);
   }

   public static void mul(float ra, float[] ry, float[] rz) {
      _mul.apply(ra, ry, rz);
   }

   public static void mul(float[] rx, float rb, float[] rz) {
      _mul.apply(rx, rb, rz);
   }

   public static void mul(float[][] rx, float[][] ry, float[][] rz) {
      _mul.apply(rx, ry, rz);
   }

   public static void mul(float ra, float[][] ry, float[][] rz) {
      _mul.apply(ra, ry, rz);
   }

   public static void mul(float[][] rx, float rb, float[][] rz) {
      _mul.apply(rx, rb, rz);
   }

   public static void mul(float[][][] rx, float[][][] ry, float[][][] rz) {
      _mul.apply(rx, ry, rz);
   }

   public static void mul(float ra, float[][][] ry, float[][][] rz) {
      _mul.apply(ra, ry, rz);
   }

   public static void mul(float[][][] rx, float rb, float[][][] rz) {
      _mul.apply(rx, rb, rz);
   }

   public static float[] div(float[] rx, float[] ry) {
      return _div.apply(rx, ry);
   }

   public static float[] div(float ra, float[] ry) {
      return _div.apply(ra, ry);
   }

   public static float[] div(float[] rx, float rb) {
      return _div.apply(rx, rb);
   }

   public static float[][] div(float[][] rx, float[][] ry) {
      return _div.apply(rx, ry);
   }

   public static float[][] div(float ra, float[][] ry) {
      return _div.apply(ra, ry);
   }

   public static float[][] div(float[][] rx, float rb) {
      return _div.apply(rx, rb);
   }

   public static float[][][] div(float[][][] rx, float[][][] ry) {
      return _div.apply(rx, ry);
   }

   public static float[][][] div(float ra, float[][][] ry) {
      return _div.apply(ra, ry);
   }

   public static float[][][] div(float[][][] rx, float rb) {
      return _div.apply(rx, rb);
   }

   public static void div(float[] rx, float[] ry, float[] rz) {
      _div.apply(rx, ry, rz);
   }

   public static void div(float ra, float[] ry, float[] rz) {
      _div.apply(ra, ry, rz);
   }

   public static void div(float[] rx, float rb, float[] rz) {
      _div.apply(rx, rb, rz);
   }

   public static void div(float[][] rx, float[][] ry, float[][] rz) {
      _div.apply(rx, ry, rz);
   }

   public static void div(float ra, float[][] ry, float[][] rz) {
      _div.apply(ra, ry, rz);
   }

   public static void div(float[][] rx, float rb, float[][] rz) {
      _div.apply(rx, rb, rz);
   }

   public static void div(float[][][] rx, float[][][] ry, float[][][] rz) {
      _div.apply(rx, ry, rz);
   }

   public static void div(float ra, float[][][] ry, float[][][] rz) {
      _div.apply(ra, ry, rz);
   }

   public static void div(float[][][] rx, float rb, float[][][] rz) {
      _div.apply(rx, rb, rz);
   }

   public static double[] add(double[] rx, double[] ry) {
      return _add.apply(rx, ry);
   }

   public static double[] add(double ra, double[] ry) {
      return _add.apply(ra, ry);
   }

   public static double[] add(double[] rx, double rb) {
      return _add.apply(rx, rb);
   }

   public static double[][] add(double[][] rx, double[][] ry) {
      return _add.apply(rx, ry);
   }

   public static double[][] add(double ra, double[][] ry) {
      return _add.apply(ra, ry);
   }

   public static double[][] add(double[][] rx, double rb) {
      return _add.apply(rx, rb);
   }

   public static double[][][] add(double[][][] rx, double[][][] ry) {
      return _add.apply(rx, ry);
   }

   public static double[][][] add(double ra, double[][][] ry) {
      return _add.apply(ra, ry);
   }

   public static double[][][] add(double[][][] rx, double rb) {
      return _add.apply(rx, rb);
   }

   public static void add(double[] rx, double[] ry, double[] rz) {
      _add.apply(rx, ry, rz);
   }

   public static void add(double ra, double[] ry, double[] rz) {
      _add.apply(ra, ry, rz);
   }

   public static void add(double[] rx, double rb, double[] rz) {
      _add.apply(rx, rb, rz);
   }

   public static void add(double[][] rx, double[][] ry, double[][] rz) {
      _add.apply(rx, ry, rz);
   }

   public static void add(double ra, double[][] ry, double[][] rz) {
      _add.apply(ra, ry, rz);
   }

   public static void add(double[][] rx, double rb, double[][] rz) {
      _add.apply(rx, rb, rz);
   }

   public static void add(double[][][] rx, double[][][] ry, double[][][] rz) {
      _add.apply(rx, ry, rz);
   }

   public static void add(double ra, double[][][] ry, double[][][] rz) {
      _add.apply(ra, ry, rz);
   }

   public static void add(double[][][] rx, double rb, double[][][] rz) {
      _add.apply(rx, rb, rz);
   }

   public static double[] sub(double[] rx, double[] ry) {
      return _sub.apply(rx, ry);
   }

   public static double[] sub(double ra, double[] ry) {
      return _sub.apply(ra, ry);
   }

   public static double[] sub(double[] rx, double rb) {
      return _sub.apply(rx, rb);
   }

   public static double[][] sub(double[][] rx, double[][] ry) {
      return _sub.apply(rx, ry);
   }

   public static double[][] sub(double ra, double[][] ry) {
      return _sub.apply(ra, ry);
   }

   public static double[][] sub(double[][] rx, double rb) {
      return _sub.apply(rx, rb);
   }

   public static double[][][] sub(double[][][] rx, double[][][] ry) {
      return _sub.apply(rx, ry);
   }

   public static double[][][] sub(double ra, double[][][] ry) {
      return _sub.apply(ra, ry);
   }

   public static double[][][] sub(double[][][] rx, double rb) {
      return _sub.apply(rx, rb);
   }

   public static void sub(double[] rx, double[] ry, double[] rz) {
      _sub.apply(rx, ry, rz);
   }

   public static void sub(double ra, double[] ry, double[] rz) {
      _sub.apply(ra, ry, rz);
   }

   public static void sub(double[] rx, double rb, double[] rz) {
      _sub.apply(rx, rb, rz);
   }

   public static void sub(double[][] rx, double[][] ry, double[][] rz) {
      _sub.apply(rx, ry, rz);
   }

   public static void sub(double ra, double[][] ry, double[][] rz) {
      _sub.apply(ra, ry, rz);
   }

   public static void sub(double[][] rx, double rb, double[][] rz) {
      _sub.apply(rx, rb, rz);
   }

   public static void sub(double[][][] rx, double[][][] ry, double[][][] rz) {
      _sub.apply(rx, ry, rz);
   }

   public static void sub(double ra, double[][][] ry, double[][][] rz) {
      _sub.apply(ra, ry, rz);
   }

   public static void sub(double[][][] rx, double rb, double[][][] rz) {
      _sub.apply(rx, rb, rz);
   }

   public static double[] mul(double[] rx, double[] ry) {
      return _mul.apply(rx, ry);
   }

   public static double[] mul(double ra, double[] ry) {
      return _mul.apply(ra, ry);
   }

   public static double[] mul(double[] rx, double rb) {
      return _mul.apply(rx, rb);
   }

   public static double[][] mul(double[][] rx, double[][] ry) {
      return _mul.apply(rx, ry);
   }

   public static double[][] mul(double ra, double[][] ry) {
      return _mul.apply(ra, ry);
   }

   public static double[][] mul(double[][] rx, double rb) {
      return _mul.apply(rx, rb);
   }

   public static double[][][] mul(double[][][] rx, double[][][] ry) {
      return _mul.apply(rx, ry);
   }

   public static double[][][] mul(double ra, double[][][] ry) {
      return _mul.apply(ra, ry);
   }

   public static double[][][] mul(double[][][] rx, double rb) {
      return _mul.apply(rx, rb);
   }

   public static void mul(double[] rx, double[] ry, double[] rz) {
      _mul.apply(rx, ry, rz);
   }

   public static void mul(double ra, double[] ry, double[] rz) {
      _mul.apply(ra, ry, rz);
   }

   public static void mul(double[] rx, double rb, double[] rz) {
      _mul.apply(rx, rb, rz);
   }

   public static void mul(double[][] rx, double[][] ry, double[][] rz) {
      _mul.apply(rx, ry, rz);
   }

   public static void mul(double ra, double[][] ry, double[][] rz) {
      _mul.apply(ra, ry, rz);
   }

   public static void mul(double[][] rx, double rb, double[][] rz) {
      _mul.apply(rx, rb, rz);
   }

   public static void mul(double[][][] rx, double[][][] ry, double[][][] rz) {
      _mul.apply(rx, ry, rz);
   }

   public static void mul(double ra, double[][][] ry, double[][][] rz) {
      _mul.apply(ra, ry, rz);
   }

   public static void mul(double[][][] rx, double rb, double[][][] rz) {
      _mul.apply(rx, rb, rz);
   }

   public static double[] div(double[] rx, double[] ry) {
      return _div.apply(rx, ry);
   }

   public static double[] div(double ra, double[] ry) {
      return _div.apply(ra, ry);
   }

   public static double[] div(double[] rx, double rb) {
      return _div.apply(rx, rb);
   }

   public static double[][] div(double[][] rx, double[][] ry) {
      return _div.apply(rx, ry);
   }

   public static double[][] div(double ra, double[][] ry) {
      return _div.apply(ra, ry);
   }

   public static double[][] div(double[][] rx, double rb) {
      return _div.apply(rx, rb);
   }

   public static double[][][] div(double[][][] rx, double[][][] ry) {
      return _div.apply(rx, ry);
   }

   public static double[][][] div(double ra, double[][][] ry) {
      return _div.apply(ra, ry);
   }

   public static double[][][] div(double[][][] rx, double rb) {
      return _div.apply(rx, rb);
   }

   public static void div(double[] rx, double[] ry, double[] rz) {
      _div.apply(rx, ry, rz);
   }

   public static void div(double ra, double[] ry, double[] rz) {
      _div.apply(ra, ry, rz);
   }

   public static void div(double[] rx, double rb, double[] rz) {
      _div.apply(rx, rb, rz);
   }

   public static void div(double[][] rx, double[][] ry, double[][] rz) {
      _div.apply(rx, ry, rz);
   }

   public static void div(double ra, double[][] ry, double[][] rz) {
      _div.apply(ra, ry, rz);
   }

   public static void div(double[][] rx, double rb, double[][] rz) {
      _div.apply(rx, rb, rz);
   }

   public static void div(double[][][] rx, double[][][] ry, double[][][] rz) {
      _div.apply(rx, ry, rz);
   }

   public static void div(double ra, double[][][] ry, double[][][] rz) {
      _div.apply(ra, ry, rz);
   }

   public static void div(double[][][] rx, double rb, double[][][] rz) {
      _div.apply(rx, rb, rz);
   }

   public static float[] abs(float[] rx) {
      return _abs.apply(rx);
   }

   public static float[][] abs(float[][] rx) {
      return _abs.apply(rx);
   }

   public static float[][][] abs(float[][][] rx) {
      return _abs.apply(rx);
   }

   public static void abs(float[] rx, float[] ry) {
      _abs.apply(rx, ry);
   }

   public static void abs(float[][] rx, float[][] ry) {
      _abs.apply(rx, ry);
   }

   public static void abs(float[][][] rx, float[][][] ry) {
      _abs.apply(rx, ry);
   }

   public static float[] neg(float[] rx) {
      return _neg.apply(rx);
   }

   public static float[][] neg(float[][] rx) {
      return _neg.apply(rx);
   }

   public static float[][][] neg(float[][][] rx) {
      return _neg.apply(rx);
   }

   public static void neg(float[] rx, float[] ry) {
      _neg.apply(rx, ry);
   }

   public static void neg(float[][] rx, float[][] ry) {
      _neg.apply(rx, ry);
   }

   public static void neg(float[][][] rx, float[][][] ry) {
      _neg.apply(rx, ry);
   }

   public static float[] cos(float[] rx) {
      return _cos.apply(rx);
   }

   public static float[][] cos(float[][] rx) {
      return _cos.apply(rx);
   }

   public static float[][][] cos(float[][][] rx) {
      return _cos.apply(rx);
   }

   public static void cos(float[] rx, float[] ry) {
      _cos.apply(rx, ry);
   }

   public static void cos(float[][] rx, float[][] ry) {
      _cos.apply(rx, ry);
   }

   public static void cos(float[][][] rx, float[][][] ry) {
      _cos.apply(rx, ry);
   }

   public static float[] sin(float[] rx) {
      return _sin.apply(rx);
   }

   public static float[][] sin(float[][] rx) {
      return _sin.apply(rx);
   }

   public static float[][][] sin(float[][][] rx) {
      return _sin.apply(rx);
   }

   public static void sin(float[] rx, float[] ry) {
      _sin.apply(rx, ry);
   }

   public static void sin(float[][] rx, float[][] ry) {
      _sin.apply(rx, ry);
   }

   public static void sin(float[][][] rx, float[][][] ry) {
      _sin.apply(rx, ry);
   }

   public static float[] exp(float[] rx) {
      return _exp.apply(rx);
   }

   public static float[][] exp(float[][] rx) {
      return _exp.apply(rx);
   }

   public static float[][][] exp(float[][][] rx) {
      return _exp.apply(rx);
   }

   public static void exp(float[] rx, float[] ry) {
      _exp.apply(rx, ry);
   }

   public static void exp(float[][] rx, float[][] ry) {
      _exp.apply(rx, ry);
   }

   public static void exp(float[][][] rx, float[][][] ry) {
      _exp.apply(rx, ry);
   }

   public static float[] log(float[] rx) {
      return _log.apply(rx);
   }

   public static float[][] log(float[][] rx) {
      return _log.apply(rx);
   }

   public static float[][][] log(float[][][] rx) {
      return _log.apply(rx);
   }

   public static void log(float[] rx, float[] ry) {
      _log.apply(rx, ry);
   }

   public static void log(float[][] rx, float[][] ry) {
      _log.apply(rx, ry);
   }

   public static void log(float[][][] rx, float[][][] ry) {
      _log.apply(rx, ry);
   }

   public static float[] log10(float[] rx) {
      return _log10.apply(rx);
   }

   public static float[][] log10(float[][] rx) {
      return _log10.apply(rx);
   }

   public static float[][][] log10(float[][][] rx) {
      return _log10.apply(rx);
   }

   public static void log10(float[] rx, float[] ry) {
      _log10.apply(rx, ry);
   }

   public static void log10(float[][] rx, float[][] ry) {
      _log10.apply(rx, ry);
   }

   public static void log10(float[][][] rx, float[][][] ry) {
      _log10.apply(rx, ry);
   }

   public static float[] sqrt(float[] rx) {
      return _sqrt.apply(rx);
   }

   public static float[][] sqrt(float[][] rx) {
      return _sqrt.apply(rx);
   }

   public static float[][][] sqrt(float[][][] rx) {
      return _sqrt.apply(rx);
   }

   public static void sqrt(float[] rx, float[] ry) {
      _sqrt.apply(rx, ry);
   }

   public static void sqrt(float[][] rx, float[][] ry) {
      _sqrt.apply(rx, ry);
   }

   public static void sqrt(float[][][] rx, float[][][] ry) {
      _sqrt.apply(rx, ry);
   }

   public static float[] sgn(float[] rx) {
      return _sgn.apply(rx);
   }

   public static float[][] sgn(float[][] rx) {
      return _sgn.apply(rx);
   }

   public static float[][][] sgn(float[][][] rx) {
      return _sgn.apply(rx);
   }

   public static void sgn(float[] rx, float[] ry) {
      _sgn.apply(rx, ry);
   }

   public static void sgn(float[][] rx, float[][] ry) {
      _sgn.apply(rx, ry);
   }

   public static void sgn(float[][][] rx, float[][][] ry) {
      _sgn.apply(rx, ry);
   }

   public static double[] abs(double[] rx) {
      return _abs.apply(rx);
   }

   public static double[][] abs(double[][] rx) {
      return _abs.apply(rx);
   }

   public static double[][][] abs(double[][][] rx) {
      return _abs.apply(rx);
   }

   public static void abs(double[] rx, double[] ry) {
      _abs.apply(rx, ry);
   }

   public static void abs(double[][] rx, double[][] ry) {
      _abs.apply(rx, ry);
   }

   public static void abs(double[][][] rx, double[][][] ry) {
      _abs.apply(rx, ry);
   }

   public static double[] neg(double[] rx) {
      return _neg.apply(rx);
   }

   public static double[][] neg(double[][] rx) {
      return _neg.apply(rx);
   }

   public static double[][][] neg(double[][][] rx) {
      return _neg.apply(rx);
   }

   public static void neg(double[] rx, double[] ry) {
      _neg.apply(rx, ry);
   }

   public static void neg(double[][] rx, double[][] ry) {
      _neg.apply(rx, ry);
   }

   public static void neg(double[][][] rx, double[][][] ry) {
      _neg.apply(rx, ry);
   }

   public static double[] cos(double[] rx) {
      return _cos.apply(rx);
   }

   public static double[][] cos(double[][] rx) {
      return _cos.apply(rx);
   }

   public static double[][][] cos(double[][][] rx) {
      return _cos.apply(rx);
   }

   public static void cos(double[] rx, double[] ry) {
      _cos.apply(rx, ry);
   }

   public static void cos(double[][] rx, double[][] ry) {
      _cos.apply(rx, ry);
   }

   public static void cos(double[][][] rx, double[][][] ry) {
      _cos.apply(rx, ry);
   }

   public static double[] sin(double[] rx) {
      return _sin.apply(rx);
   }

   public static double[][] sin(double[][] rx) {
      return _sin.apply(rx);
   }

   public static double[][][] sin(double[][][] rx) {
      return _sin.apply(rx);
   }

   public static void sin(double[] rx, double[] ry) {
      _sin.apply(rx, ry);
   }

   public static void sin(double[][] rx, double[][] ry) {
      _sin.apply(rx, ry);
   }

   public static void sin(double[][][] rx, double[][][] ry) {
      _sin.apply(rx, ry);
   }

   public static double[] exp(double[] rx) {
      return _exp.apply(rx);
   }

   public static double[][] exp(double[][] rx) {
      return _exp.apply(rx);
   }

   public static double[][][] exp(double[][][] rx) {
      return _exp.apply(rx);
   }

   public static void exp(double[] rx, double[] ry) {
      _exp.apply(rx, ry);
   }

   public static void exp(double[][] rx, double[][] ry) {
      _exp.apply(rx, ry);
   }

   public static void exp(double[][][] rx, double[][][] ry) {
      _exp.apply(rx, ry);
   }

   public static double[] log(double[] rx) {
      return _log.apply(rx);
   }

   public static double[][] log(double[][] rx) {
      return _log.apply(rx);
   }

   public static double[][][] log(double[][][] rx) {
      return _log.apply(rx);
   }

   public static void log(double[] rx, double[] ry) {
      _log.apply(rx, ry);
   }

   public static void log(double[][] rx, double[][] ry) {
      _log.apply(rx, ry);
   }

   public static void log(double[][][] rx, double[][][] ry) {
      _log.apply(rx, ry);
   }

   public static double[] log10(double[] rx) {
      return _log10.apply(rx);
   }

   public static double[][] log10(double[][] rx) {
      return _log10.apply(rx);
   }

   public static double[][][] log10(double[][][] rx) {
      return _log10.apply(rx);
   }

   public static void log10(double[] rx, double[] ry) {
      _log10.apply(rx, ry);
   }

   public static void log10(double[][] rx, double[][] ry) {
      _log10.apply(rx, ry);
   }

   public static void log10(double[][][] rx, double[][][] ry) {
      _log10.apply(rx, ry);
   }

   public static double[] sqrt(double[] rx) {
      return _sqrt.apply(rx);
   }

   public static double[][] sqrt(double[][] rx) {
      return _sqrt.apply(rx);
   }

   public static double[][][] sqrt(double[][][] rx) {
      return _sqrt.apply(rx);
   }

   public static void sqrt(double[] rx, double[] ry) {
      _sqrt.apply(rx, ry);
   }

   public static void sqrt(double[][] rx, double[][] ry) {
      _sqrt.apply(rx, ry);
   }

   public static void sqrt(double[][][] rx, double[][][] ry) {
      _sqrt.apply(rx, ry);
   }

   public static double[] sgn(double[] rx) {
      return _sgn.apply(rx);
   }

   public static double[][] sgn(double[][] rx) {
      return _sgn.apply(rx);
   }

   public static double[][][] sgn(double[][][] rx) {
      return _sgn.apply(rx);
   }

   public static void sgn(double[] rx, double[] ry) {
      _sgn.apply(rx, ry);
   }

   public static void sgn(double[][] rx, double[][] ry) {
      _sgn.apply(rx, ry);
   }

   public static void sgn(double[][][] rx, double[][][] ry) {
      _sgn.apply(rx, ry);
   }

   public static float[] clip(float rxmin, float rxmax, float[] rx) {
      int n1 = rx.length;
      float[] ry = new float[n1];
      clip(rxmin, rxmax, rx, ry);
      return ry;
   }

   public static float[][] clip(float rxmin, float rxmax, float[][] rx) {
      int n2 = rx.length;
      float[][] ry = new float[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         ry[i2] = clip(rxmin, rxmax, rx[i2]);
      }

      return ry;
   }

   public static float[][][] clip(float rxmin, float rxmax, float[][][] rx) {
      int n3 = rx.length;
      float[][][] ry = new float[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         ry[i3] = clip(rxmin, rxmax, rx[i3]);
      }

      return ry;
   }

   public static void clip(float rxmin, float rxmax, float[] rx, float[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         float rxi = rx[i1];
         ry[i1] = rxi < rxmin ? rxmin : (rxi > rxmax ? rxmax : rxi);
      }
   }

   public static void clip(float rxmin, float rxmax, float[][] rx, float[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         clip(rxmin, rxmax, rx[i2], ry[i2]);
      }
   }

   public static void clip(float rxmin, float rxmax, float[][][] rx, float[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         clip(rxmin, rxmax, rx[i3], ry[i3]);
      }
   }

   public static double[] clip(double rxmin, double rxmax, double[] rx) {
      int n1 = rx.length;
      double[] ry = new double[n1];
      clip(rxmin, rxmax, rx, ry);
      return ry;
   }

   public static double[][] clip(double rxmin, double rxmax, double[][] rx) {
      int n2 = rx.length;
      double[][] ry = new double[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         ry[i2] = clip(rxmin, rxmax, rx[i2]);
      }

      return ry;
   }

   public static double[][][] clip(double rxmin, double rxmax, double[][][] rx) {
      int n3 = rx.length;
      double[][][] ry = new double[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         ry[i3] = clip(rxmin, rxmax, rx[i3]);
      }

      return ry;
   }

   public static void clip(double rxmin, double rxmax, double[] rx, double[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         double rxi = rx[i1];
         ry[i1] = rxi < rxmin ? rxmin : (rxi > rxmax ? rxmax : rxi);
      }
   }

   public static void clip(double rxmin, double rxmax, double[][] rx, double[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         clip(rxmin, rxmax, rx[i2], ry[i2]);
      }
   }

   public static void clip(double rxmin, double rxmax, double[][][] rx, double[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         clip(rxmin, rxmax, rx[i3], ry[i3]);
      }
   }

   public static float[] pow(float[] rx, float ra) {
      int n1 = rx.length;
      float[] ry = new float[n1];
      pow(rx, ra, ry);
      return ry;
   }

   public static float[][] pow(float[][] rx, float ra) {
      int n2 = rx.length;
      float[][] ry = new float[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         ry[i2] = pow(rx[i2], ra);
      }

      return ry;
   }

   public static float[][][] pow(float[][][] rx, float ra) {
      int n3 = rx.length;
      float[][][] ry = new float[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         ry[i3] = pow(rx[i3], ra);
      }

      return ry;
   }

   public static void pow(float[] rx, float ra, float[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         ry[i1] = (float)Math.pow((double)rx[i1], (double)ra);
      }
   }

   public static void pow(float[][] rx, float ra, float[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         pow(rx[i2], ra, ry[i2]);
      }
   }

   public static void pow(float[][][] rx, float ra, float[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         pow(rx[i3], ra, ry[i3]);
      }
   }

   public static double[] pow(double[] rx, double ra) {
      int n1 = rx.length;
      double[] ry = new double[n1];
      pow(rx, ra, ry);
      return ry;
   }

   public static double[][] pow(double[][] rx, double ra) {
      int n2 = rx.length;
      double[][] ry = new double[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         ry[i2] = pow(rx[i2], ra);
      }

      return ry;
   }

   public static double[][][] pow(double[][][] rx, double ra) {
      int n3 = rx.length;
      double[][][] ry = new double[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         ry[i3] = pow(rx[i3], ra);
      }

      return ry;
   }

   public static void pow(double[] rx, double ra, double[] ry) {
      int n1 = rx.length;

      for(int i1 = 0; i1 < n1; ++i1) {
         ry[i1] = Math.pow(rx[i1], ra);
      }
   }

   public static void pow(double[][] rx, double ra, double[][] ry) {
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         pow(rx[i2], ra, ry[i2]);
      }
   }

   public static void pow(double[][][] rx, double ra, double[][][] ry) {
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         pow(rx[i3], ra, ry[i3]);
      }
   }

   public static byte sum(byte[] rx) {
      int n1 = rx.length;
      byte sum = 0;

      for(int i1 = 0; i1 < n1; ++i1) {
         sum += rx[i1];
      }

      return sum;
   }

   public static byte sum(byte[][] rx) {
      int n2 = rx.length;
      byte sum = 0;

      for(int i2 = 0; i2 < n2; ++i2) {
         sum += sum(rx[i2]);
      }

      return sum;
   }

   public static byte sum(byte[][][] rx) {
      int n3 = rx.length;
      byte sum = 0;

      for(int i3 = 0; i3 < n3; ++i3) {
         sum += sum(rx[i3]);
      }

      return sum;
   }

   public static short sum(short[] rx) {
      int n1 = rx.length;
      short sum = 0;

      for(int i1 = 0; i1 < n1; ++i1) {
         sum += rx[i1];
      }

      return sum;
   }

   public static short sum(short[][] rx) {
      int n2 = rx.length;
      short sum = 0;

      for(int i2 = 0; i2 < n2; ++i2) {
         sum += sum(rx[i2]);
      }

      return sum;
   }

   public static short sum(short[][][] rx) {
      int n3 = rx.length;
      short sum = 0;

      for(int i3 = 0; i3 < n3; ++i3) {
         sum += sum(rx[i3]);
      }

      return sum;
   }

   public static int sum(int[] rx) {
      int n1 = rx.length;
      int sum = 0;

      for(int i1 = 0; i1 < n1; ++i1) {
         sum += rx[i1];
      }

      return sum;
   }

   public static int sum(int[][] rx) {
      int n2 = rx.length;
      int sum = 0;

      for(int i2 = 0; i2 < n2; ++i2) {
         sum += sum(rx[i2]);
      }

      return sum;
   }

   public static int sum(int[][][] rx) {
      int n3 = rx.length;
      int sum = 0;

      for(int i3 = 0; i3 < n3; ++i3) {
         sum += sum(rx[i3]);
      }

      return sum;
   }

   public static long sum(long[] rx) {
      int n1 = rx.length;
      long sum = 0L;

      for(int i1 = 0; i1 < n1; ++i1) {
         sum += rx[i1];
      }

      return sum;
   }

   public static long sum(long[][] rx) {
      int n2 = rx.length;
      long sum = 0L;

      for(int i2 = 0; i2 < n2; ++i2) {
         sum += sum(rx[i2]);
      }

      return sum;
   }

   public static long sum(long[][][] rx) {
      int n3 = rx.length;
      long sum = 0L;

      for(int i3 = 0; i3 < n3; ++i3) {
         sum += sum(rx[i3]);
      }

      return sum;
   }

   public static float sum(float[] rx) {
      int n1 = rx.length;
      float sum = 0.0F;

      for(int i1 = 0; i1 < n1; ++i1) {
         sum += rx[i1];
      }

      return sum;
   }

   public static float sum(float[][] rx) {
      int n2 = rx.length;
      float sum = 0.0F;

      for(int i2 = 0; i2 < n2; ++i2) {
         sum += sum(rx[i2]);
      }

      return sum;
   }

   public static float sum(float[][][] rx) {
      int n3 = rx.length;
      float sum = 0.0F;

      for(int i3 = 0; i3 < n3; ++i3) {
         sum += sum(rx[i3]);
      }

      return sum;
   }

   public static double sum(double[] rx) {
      int n1 = rx.length;
      double sum = 0.0;

      for(int i1 = 0; i1 < n1; ++i1) {
         sum += rx[i1];
      }

      return sum;
   }

   public static double sum(double[][] rx) {
      int n2 = rx.length;
      double sum = 0.0;

      for(int i2 = 0; i2 < n2; ++i2) {
         sum += sum(rx[i2]);
      }

      return sum;
   }

   public static double sum(double[][][] rx) {
      int n3 = rx.length;
      double sum = 0.0;

      for(int i3 = 0; i3 < n3; ++i3) {
         sum += sum(rx[i3]);
      }

      return sum;
   }

   public static byte max(byte[] rx) {
      return max(rx, null);
   }

   public static byte max(byte[][] rx) {
      return max(rx, null);
   }

   public static byte max(byte[][][] rx) {
      return max(rx, null);
   }

   public static byte max(byte[] rx, int[] index) {
      int i1max = 0;
      byte max = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] > max) {
            max = rx[i1];
            i1max = i1;
         }
      }

      if (index != null) {
         index[0] = i1max;
      }

      return max;
   }

   public static byte max(byte[][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      byte max = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         byte[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] > max) {
               max = rxi2[i1];
               i2max = i2;
               i1max = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
      }

      return max;
   }

   public static byte max(byte[][][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      int i3max = 0;
      byte max = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         byte[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            byte[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] > max) {
                  max = rxi3i2[i1];
                  i1max = i1;
                  i2max = i2;
                  i3max = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
         index[2] = i3max;
      }

      return max;
   }

   public static byte min(byte[] rx) {
      return min(rx, null);
   }

   public static byte min(byte[][] rx) {
      return min(rx, null);
   }

   public static byte min(byte[][][] rx) {
      return min(rx, null);
   }

   public static byte min(byte[] rx, int[] index) {
      int i1min = 0;
      byte min = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] < min) {
            min = rx[i1];
            i1min = i1;
         }
      }

      if (index != null) {
         index[0] = i1min;
      }

      return min;
   }

   public static byte min(byte[][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      byte min = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         byte[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] < min) {
               min = rxi2[i1];
               i2min = i2;
               i1min = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
      }

      return min;
   }

   public static byte min(byte[][][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      int i3min = 0;
      byte min = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         byte[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            byte[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] < min) {
                  min = rxi3i2[i1];
                  i1min = i1;
                  i2min = i2;
                  i3min = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
         index[2] = i3min;
      }

      return min;
   }

   public static short max(short[] rx) {
      return max(rx, null);
   }

   public static short max(short[][] rx) {
      return max(rx, null);
   }

   public static short max(short[][][] rx) {
      return max(rx, null);
   }

   public static short max(short[] rx, int[] index) {
      int i1max = 0;
      short max = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] > max) {
            max = rx[i1];
            i1max = i1;
         }
      }

      if (index != null) {
         index[0] = i1max;
      }

      return max;
   }

   public static short max(short[][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      short max = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         short[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] > max) {
               max = rxi2[i1];
               i2max = i2;
               i1max = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
      }

      return max;
   }

   public static short max(short[][][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      int i3max = 0;
      short max = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         short[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            short[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] > max) {
                  max = rxi3i2[i1];
                  i1max = i1;
                  i2max = i2;
                  i3max = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
         index[2] = i3max;
      }

      return max;
   }

   public static short min(short[] rx) {
      return min(rx, null);
   }

   public static short min(short[][] rx) {
      return min(rx, null);
   }

   public static short min(short[][][] rx) {
      return min(rx, null);
   }

   public static short min(short[] rx, int[] index) {
      int i1min = 0;
      short min = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] < min) {
            min = rx[i1];
            i1min = i1;
         }
      }

      if (index != null) {
         index[0] = i1min;
      }

      return min;
   }

   public static short min(short[][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      short min = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         short[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] < min) {
               min = rxi2[i1];
               i2min = i2;
               i1min = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
      }

      return min;
   }

   public static short min(short[][][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      int i3min = 0;
      short min = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         short[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            short[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] < min) {
                  min = rxi3i2[i1];
                  i1min = i1;
                  i2min = i2;
                  i3min = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
         index[2] = i3min;
      }

      return min;
   }

   public static int max(int[] rx) {
      return max(rx, null);
   }

   public static int max(int[][] rx) {
      return max(rx, null);
   }

   public static int max(int[][][] rx) {
      return max(rx, null);
   }

   public static int max(int[] rx, int[] index) {
      int i1max = 0;
      int max = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] > max) {
            max = rx[i1];
            i1max = i1;
         }
      }

      if (index != null) {
         index[0] = i1max;
      }

      return max;
   }

   public static int max(int[][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      int max = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         int[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] > max) {
               max = rxi2[i1];
               i2max = i2;
               i1max = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
      }

      return max;
   }

   public static int max(int[][][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      int i3max = 0;
      int max = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         int[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            int[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] > max) {
                  max = rxi3i2[i1];
                  i1max = i1;
                  i2max = i2;
                  i3max = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
         index[2] = i3max;
      }

      return max;
   }

   public static int min(int[] rx) {
      return min(rx, null);
   }

   public static int min(int[][] rx) {
      return min(rx, null);
   }

   public static int min(int[][][] rx) {
      return min(rx, null);
   }

   public static int min(int[] rx, int[] index) {
      int i1min = 0;
      int min = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] < min) {
            min = rx[i1];
            i1min = i1;
         }
      }

      if (index != null) {
         index[0] = i1min;
      }

      return min;
   }

   public static int min(int[][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      int min = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         int[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] < min) {
               min = rxi2[i1];
               i2min = i2;
               i1min = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
      }

      return min;
   }

   public static int min(int[][][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      int i3min = 0;
      int min = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         int[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            int[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] < min) {
                  min = rxi3i2[i1];
                  i1min = i1;
                  i2min = i2;
                  i3min = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
         index[2] = i3min;
      }

      return min;
   }

   public static long max(long[] rx) {
      return max(rx, null);
   }

   public static long max(long[][] rx) {
      return max(rx, null);
   }

   public static long max(long[][][] rx) {
      return max(rx, null);
   }

   public static long max(long[] rx, int[] index) {
      int i1max = 0;
      long max = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] > max) {
            max = rx[i1];
            i1max = i1;
         }
      }

      if (index != null) {
         index[0] = i1max;
      }

      return max;
   }

   public static long max(long[][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      long max = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         long[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] > max) {
               max = rxi2[i1];
               i2max = i2;
               i1max = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
      }

      return max;
   }

   public static long max(long[][][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      int i3max = 0;
      long max = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         long[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            long[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] > max) {
                  max = rxi3i2[i1];
                  i1max = i1;
                  i2max = i2;
                  i3max = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
         index[2] = i3max;
      }

      return max;
   }

   public static long min(long[] rx) {
      return min(rx, null);
   }

   public static long min(long[][] rx) {
      return min(rx, null);
   }

   public static long min(long[][][] rx) {
      return min(rx, null);
   }

   public static long min(long[] rx, int[] index) {
      int i1min = 0;
      long min = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] < min) {
            min = rx[i1];
            i1min = i1;
         }
      }

      if (index != null) {
         index[0] = i1min;
      }

      return min;
   }

   public static long min(long[][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      long min = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         long[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] < min) {
               min = rxi2[i1];
               i2min = i2;
               i1min = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
      }

      return min;
   }

   public static long min(long[][][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      int i3min = 0;
      long min = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         long[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            long[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] < min) {
                  min = rxi3i2[i1];
                  i1min = i1;
                  i2min = i2;
                  i3min = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
         index[2] = i3min;
      }

      return min;
   }

   public static float max(float[] rx) {
      return max(rx, null);
   }

   public static float max(float[][] rx) {
      return max(rx, null);
   }

   public static float max(float[][][] rx) {
      return max(rx, null);
   }

   public static float max(float[] rx, int[] index) {
      int i1max = 0;
      float max = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] > max) {
            max = rx[i1];
            i1max = i1;
         }
      }

      if (index != null) {
         index[0] = i1max;
      }

      return max;
   }

   public static float max(float[][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      float max = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         float[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] > max) {
               max = rxi2[i1];
               i2max = i2;
               i1max = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
      }

      return max;
   }

   public static float max(float[][][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      int i3max = 0;
      float max = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         float[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] > max) {
                  max = rxi3i2[i1];
                  i1max = i1;
                  i2max = i2;
                  i3max = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
         index[2] = i3max;
      }

      return max;
   }

   public static float min(float[] rx) {
      return min(rx, null);
   }

   public static float min(float[][] rx) {
      return min(rx, null);
   }

   public static float min(float[][][] rx) {
      return min(rx, null);
   }

   public static float min(float[] rx, int[] index) {
      int i1min = 0;
      float min = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] < min) {
            min = rx[i1];
            i1min = i1;
         }
      }

      if (index != null) {
         index[0] = i1min;
      }

      return min;
   }

   public static float min(float[][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      float min = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         float[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] < min) {
               min = rxi2[i1];
               i2min = i2;
               i1min = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
      }

      return min;
   }

   public static float min(float[][][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      int i3min = 0;
      float min = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         float[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] < min) {
                  min = rxi3i2[i1];
                  i1min = i1;
                  i2min = i2;
                  i3min = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
         index[2] = i3min;
      }

      return min;
   }

   public static double max(double[] rx) {
      return max(rx, null);
   }

   public static double max(double[][] rx) {
      return max(rx, null);
   }

   public static double max(double[][][] rx) {
      return max(rx, null);
   }

   public static double max(double[] rx, int[] index) {
      int i1max = 0;
      double max = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] > max) {
            max = rx[i1];
            i1max = i1;
         }
      }

      if (index != null) {
         index[0] = i1max;
      }

      return max;
   }

   public static double max(double[][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      double max = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         double[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] > max) {
               max = rxi2[i1];
               i2max = i2;
               i1max = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
      }

      return max;
   }

   public static double max(double[][][] rx, int[] index) {
      int i1max = 0;
      int i2max = 0;
      int i3max = 0;
      double max = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         double[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            double[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] > max) {
                  max = rxi3i2[i1];
                  i1max = i1;
                  i2max = i2;
                  i3max = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1max;
         index[1] = i2max;
         index[2] = i3max;
      }

      return max;
   }

   public static double min(double[] rx) {
      return min(rx, null);
   }

   public static double min(double[][] rx) {
      return min(rx, null);
   }

   public static double min(double[][][] rx) {
      return min(rx, null);
   }

   public static double min(double[] rx, int[] index) {
      int i1min = 0;
      double min = rx[0];
      int n1 = rx.length;

      for(int i1 = 1; i1 < n1; ++i1) {
         if (rx[i1] < min) {
            min = rx[i1];
            i1min = i1;
         }
      }

      if (index != null) {
         index[0] = i1min;
      }

      return min;
   }

   public static double min(double[][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      double min = rx[0][0];
      int n2 = rx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         double[] rxi2 = rx[i2];
         int n1 = rxi2.length;

         for(int i1 = 0; i1 < n1; ++i1) {
            if (rxi2[i1] < min) {
               min = rxi2[i1];
               i2min = i2;
               i1min = i1;
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
      }

      return min;
   }

   public static double min(double[][][] rx, int[] index) {
      int i1min = 0;
      int i2min = 0;
      int i3min = 0;
      double min = rx[0][0][0];
      int n3 = rx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         double[][] rxi3 = rx[i3];
         int n2 = rxi3.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            double[] rxi3i2 = rxi3[i2];
            int n1 = rxi3i2.length;

            for(int i1 = 0; i1 < n1; ++i1) {
               if (rxi3i2[i1] < min) {
                  min = rxi3i2[i1];
                  i1min = i1;
                  i2min = i2;
                  i3min = i3;
               }
            }
         }
      }

      if (index != null) {
         index[0] = i1min;
         index[1] = i2min;
         index[2] = i3min;
      }

      return min;
   }

   public static float[] cadd(float[] cx, float[] cy) {
      return _cadd.apply(cx, cy);
   }

   public static float[] cadd(Cfloat ca, float[] cy) {
      return _cadd.apply(ca, cy);
   }

   public static float[] cadd(float[] cx, Cfloat cb) {
      return _cadd.apply(cx, cb);
   }

   public static float[][] cadd(float[][] cx, float[][] cy) {
      return _cadd.apply(cx, cy);
   }

   public static float[][] cadd(Cfloat ca, float[][] cy) {
      return _cadd.apply(ca, cy);
   }

   public static float[][] cadd(float[][] cx, Cfloat cb) {
      return _cadd.apply(cx, cb);
   }

   public static float[][][] cadd(float[][][] cx, float[][][] cy) {
      return _cadd.apply(cx, cy);
   }

   public static float[][][] cadd(Cfloat ca, float[][][] cy) {
      return _cadd.apply(ca, cy);
   }

   public static float[][][] cadd(float[][][] cx, Cfloat cb) {
      return _cadd.apply(cx, cb);
   }

   public static void cadd(float[] cx, float[] cy, float[] cz) {
      _cadd.apply(cx, cy, cz);
   }

   public static void cadd(Cfloat ca, float[] cy, float[] cz) {
      _cadd.apply(ca, cy, cz);
   }

   public static void cadd(float[] cx, Cfloat cb, float[] cz) {
      _cadd.apply(cx, cb, cz);
   }

   public static void cadd(float[][] cx, float[][] cy, float[][] cz) {
      _cadd.apply(cx, cy, cz);
   }

   public static void cadd(Cfloat ca, float[][] cy, float[][] cz) {
      _cadd.apply(ca, cy, cz);
   }

   public static void cadd(float[][] cx, Cfloat cb, float[][] cz) {
      _cadd.apply(cx, cb, cz);
   }

   public static void cadd(float[][][] cx, float[][][] cy, float[][][] cz) {
      _cadd.apply(cx, cy, cz);
   }

   public static void cadd(Cfloat ca, float[][][] cy, float[][][] cz) {
      _cadd.apply(ca, cy, cz);
   }

   public static void cadd(float[][][] cx, Cfloat cb, float[][][] cz) {
      _cadd.apply(cx, cb, cz);
   }

   public static float[] csub(float[] cx, float[] cy) {
      return _csub.apply(cx, cy);
   }

   public static float[] csub(Cfloat ca, float[] cy) {
      return _csub.apply(ca, cy);
   }

   public static float[] csub(float[] cx, Cfloat cb) {
      return _csub.apply(cx, cb);
   }

   public static float[][] csub(float[][] cx, float[][] cy) {
      return _csub.apply(cx, cy);
   }

   public static float[][] csub(Cfloat ca, float[][] cy) {
      return _csub.apply(ca, cy);
   }

   public static float[][] csub(float[][] cx, Cfloat cb) {
      return _csub.apply(cx, cb);
   }

   public static float[][][] csub(float[][][] cx, float[][][] cy) {
      return _csub.apply(cx, cy);
   }

   public static float[][][] csub(Cfloat ca, float[][][] cy) {
      return _csub.apply(ca, cy);
   }

   public static float[][][] csub(float[][][] cx, Cfloat cb) {
      return _csub.apply(cx, cb);
   }

   public static void csub(float[] cx, float[] cy, float[] cz) {
      _csub.apply(cx, cy, cz);
   }

   public static void csub(Cfloat ca, float[] cy, float[] cz) {
      _csub.apply(ca, cy, cz);
   }

   public static void csub(float[] cx, Cfloat cb, float[] cz) {
      _csub.apply(cx, cb, cz);
   }

   public static void csub(float[][] cx, float[][] cy, float[][] cz) {
      _csub.apply(cx, cy, cz);
   }

   public static void csub(Cfloat ca, float[][] cy, float[][] cz) {
      _csub.apply(ca, cy, cz);
   }

   public static void csub(float[][] cx, Cfloat cb, float[][] cz) {
      _csub.apply(cx, cb, cz);
   }

   public static void csub(float[][][] cx, float[][][] cy, float[][][] cz) {
      _csub.apply(cx, cy, cz);
   }

   public static void csub(Cfloat ca, float[][][] cy, float[][][] cz) {
      _csub.apply(ca, cy, cz);
   }

   public static void csub(float[][][] cx, Cfloat cb, float[][][] cz) {
      _csub.apply(cx, cb, cz);
   }

   public static float[] cmul(float[] cx, float[] cy) {
      return _cmul.apply(cx, cy);
   }

   public static float[] cmul(Cfloat ca, float[] cy) {
      return _cmul.apply(ca, cy);
   }

   public static float[] cmul(float[] cx, Cfloat cb) {
      return _cmul.apply(cx, cb);
   }

   public static float[][] cmul(float[][] cx, float[][] cy) {
      return _cmul.apply(cx, cy);
   }

   public static float[][] cmul(Cfloat ca, float[][] cy) {
      return _cmul.apply(ca, cy);
   }

   public static float[][] cmul(float[][] cx, Cfloat cb) {
      return _cmul.apply(cx, cb);
   }

   public static float[][][] cmul(float[][][] cx, float[][][] cy) {
      return _cmul.apply(cx, cy);
   }

   public static float[][][] cmul(Cfloat ca, float[][][] cy) {
      return _cmul.apply(ca, cy);
   }

   public static float[][][] cmul(float[][][] cx, Cfloat cb) {
      return _cmul.apply(cx, cb);
   }

   public static void cmul(float[] cx, float[] cy, float[] cz) {
      _cmul.apply(cx, cy, cz);
   }

   public static void cmul(Cfloat ca, float[] cy, float[] cz) {
      _cmul.apply(ca, cy, cz);
   }

   public static void cmul(float[] cx, Cfloat cb, float[] cz) {
      _cmul.apply(cx, cb, cz);
   }

   public static void cmul(float[][] cx, float[][] cy, float[][] cz) {
      _cmul.apply(cx, cy, cz);
   }

   public static void cmul(Cfloat ca, float[][] cy, float[][] cz) {
      _cmul.apply(ca, cy, cz);
   }

   public static void cmul(float[][] cx, Cfloat cb, float[][] cz) {
      _cmul.apply(cx, cb, cz);
   }

   public static void cmul(float[][][] cx, float[][][] cy, float[][][] cz) {
      _cmul.apply(cx, cy, cz);
   }

   public static void cmul(Cfloat ca, float[][][] cy, float[][][] cz) {
      _cmul.apply(ca, cy, cz);
   }

   public static void cmul(float[][][] cx, Cfloat cb, float[][][] cz) {
      _cmul.apply(cx, cb, cz);
   }

   public static float[] cdiv(float[] cx, float[] cy) {
      return _cdiv.apply(cx, cy);
   }

   public static float[] cdiv(Cfloat ca, float[] cy) {
      return _cdiv.apply(ca, cy);
   }

   public static float[] cdiv(float[] cx, Cfloat cb) {
      return _cdiv.apply(cx, cb);
   }

   public static float[][] cdiv(float[][] cx, float[][] cy) {
      return _cdiv.apply(cx, cy);
   }

   public static float[][] cdiv(Cfloat ca, float[][] cy) {
      return _cdiv.apply(ca, cy);
   }

   public static float[][] cdiv(float[][] cx, Cfloat cb) {
      return _cdiv.apply(cx, cb);
   }

   public static float[][][] cdiv(float[][][] cx, float[][][] cy) {
      return _cdiv.apply(cx, cy);
   }

   public static float[][][] cdiv(Cfloat ca, float[][][] cy) {
      return _cdiv.apply(ca, cy);
   }

   public static float[][][] cdiv(float[][][] cx, Cfloat cb) {
      return _cdiv.apply(cx, cb);
   }

   public static void cdiv(float[] cx, float[] cy, float[] cz) {
      _cdiv.apply(cx, cy, cz);
   }

   public static void cdiv(Cfloat ca, float[] cy, float[] cz) {
      _cdiv.apply(ca, cy, cz);
   }

   public static void cdiv(float[] cx, Cfloat cb, float[] cz) {
      _cdiv.apply(cx, cb, cz);
   }

   public static void cdiv(float[][] cx, float[][] cy, float[][] cz) {
      _cdiv.apply(cx, cy, cz);
   }

   public static void cdiv(Cfloat ca, float[][] cy, float[][] cz) {
      _cdiv.apply(ca, cy, cz);
   }

   public static void cdiv(float[][] cx, Cfloat cb, float[][] cz) {
      _cdiv.apply(cx, cb, cz);
   }

   public static void cdiv(float[][][] cx, float[][][] cy, float[][][] cz) {
      _cdiv.apply(cx, cy, cz);
   }

   public static void cdiv(Cfloat ca, float[][][] cy, float[][][] cz) {
      _cdiv.apply(ca, cy, cz);
   }

   public static void cdiv(float[][][] cx, Cfloat cb, float[][][] cz) {
      _cdiv.apply(cx, cb, cz);
   }

   public static double[] cadd(double[] cx, double[] cy) {
      return _cadd.apply(cx, cy);
   }

   public static double[] cadd(Cdouble ca, double[] cy) {
      return _cadd.apply(ca, cy);
   }

   public static double[] cadd(double[] cx, Cdouble cb) {
      return _cadd.apply(cx, cb);
   }

   public static double[][] cadd(double[][] cx, double[][] cy) {
      return _cadd.apply(cx, cy);
   }

   public static double[][] cadd(Cdouble ca, double[][] cy) {
      return _cadd.apply(ca, cy);
   }

   public static double[][] cadd(double[][] cx, Cdouble cb) {
      return _cadd.apply(cx, cb);
   }

   public static double[][][] cadd(double[][][] cx, double[][][] cy) {
      return _cadd.apply(cx, cy);
   }

   public static double[][][] cadd(Cdouble ca, double[][][] cy) {
      return _cadd.apply(ca, cy);
   }

   public static double[][][] cadd(double[][][] cx, Cdouble cb) {
      return _cadd.apply(cx, cb);
   }

   public static void cadd(double[] cx, double[] cy, double[] cz) {
      _cadd.apply(cx, cy, cz);
   }

   public static void cadd(Cdouble ca, double[] cy, double[] cz) {
      _cadd.apply(ca, cy, cz);
   }

   public static void cadd(double[] cx, Cdouble cb, double[] cz) {
      _cadd.apply(cx, cb, cz);
   }

   public static void cadd(double[][] cx, double[][] cy, double[][] cz) {
      _cadd.apply(cx, cy, cz);
   }

   public static void cadd(Cdouble ca, double[][] cy, double[][] cz) {
      _cadd.apply(ca, cy, cz);
   }

   public static void cadd(double[][] cx, Cdouble cb, double[][] cz) {
      _cadd.apply(cx, cb, cz);
   }

   public static void cadd(double[][][] cx, double[][][] cy, double[][][] cz) {
      _cadd.apply(cx, cy, cz);
   }

   public static void cadd(Cdouble ca, double[][][] cy, double[][][] cz) {
      _cadd.apply(ca, cy, cz);
   }

   public static void cadd(double[][][] cx, Cdouble cb, double[][][] cz) {
      _cadd.apply(cx, cb, cz);
   }

   public static double[] csub(double[] cx, double[] cy) {
      return _csub.apply(cx, cy);
   }

   public static double[] csub(Cdouble ca, double[] cy) {
      return _csub.apply(ca, cy);
   }

   public static double[] csub(double[] cx, Cdouble cb) {
      return _csub.apply(cx, cb);
   }

   public static double[][] csub(double[][] cx, double[][] cy) {
      return _csub.apply(cx, cy);
   }

   public static double[][] csub(Cdouble ca, double[][] cy) {
      return _csub.apply(ca, cy);
   }

   public static double[][] csub(double[][] cx, Cdouble cb) {
      return _csub.apply(cx, cb);
   }

   public static double[][][] csub(double[][][] cx, double[][][] cy) {
      return _csub.apply(cx, cy);
   }

   public static double[][][] csub(Cdouble ca, double[][][] cy) {
      return _csub.apply(ca, cy);
   }

   public static double[][][] csub(double[][][] cx, Cdouble cb) {
      return _csub.apply(cx, cb);
   }

   public static void csub(double[] cx, double[] cy, double[] cz) {
      _csub.apply(cx, cy, cz);
   }

   public static void csub(Cdouble ca, double[] cy, double[] cz) {
      _csub.apply(ca, cy, cz);
   }

   public static void csub(double[] cx, Cdouble cb, double[] cz) {
      _csub.apply(cx, cb, cz);
   }

   public static void csub(double[][] cx, double[][] cy, double[][] cz) {
      _csub.apply(cx, cy, cz);
   }

   public static void csub(Cdouble ca, double[][] cy, double[][] cz) {
      _csub.apply(ca, cy, cz);
   }

   public static void csub(double[][] cx, Cdouble cb, double[][] cz) {
      _csub.apply(cx, cb, cz);
   }

   public static void csub(double[][][] cx, double[][][] cy, double[][][] cz) {
      _csub.apply(cx, cy, cz);
   }

   public static void csub(Cdouble ca, double[][][] cy, double[][][] cz) {
      _csub.apply(ca, cy, cz);
   }

   public static void csub(double[][][] cx, Cdouble cb, double[][][] cz) {
      _csub.apply(cx, cb, cz);
   }

   public static double[] cmul(double[] cx, double[] cy) {
      return _cmul.apply(cx, cy);
   }

   public static double[] cmul(Cdouble ca, double[] cy) {
      return _cmul.apply(ca, cy);
   }

   public static double[] cmul(double[] cx, Cdouble cb) {
      return _cmul.apply(cx, cb);
   }

   public static double[][] cmul(double[][] cx, double[][] cy) {
      return _cmul.apply(cx, cy);
   }

   public static double[][] cmul(Cdouble ca, double[][] cy) {
      return _cmul.apply(ca, cy);
   }

   public static double[][] cmul(double[][] cx, Cdouble cb) {
      return _cmul.apply(cx, cb);
   }

   public static double[][][] cmul(double[][][] cx, double[][][] cy) {
      return _cmul.apply(cx, cy);
   }

   public static double[][][] cmul(Cdouble ca, double[][][] cy) {
      return _cmul.apply(ca, cy);
   }

   public static double[][][] cmul(double[][][] cx, Cdouble cb) {
      return _cmul.apply(cx, cb);
   }

   public static void cmul(double[] cx, double[] cy, double[] cz) {
      _cmul.apply(cx, cy, cz);
   }

   public static void cmul(Cdouble ca, double[] cy, double[] cz) {
      _cmul.apply(ca, cy, cz);
   }

   public static void cmul(double[] cx, Cdouble cb, double[] cz) {
      _cmul.apply(cx, cb, cz);
   }

   public static void cmul(double[][] cx, double[][] cy, double[][] cz) {
      _cmul.apply(cx, cy, cz);
   }

   public static void cmul(Cdouble ca, double[][] cy, double[][] cz) {
      _cmul.apply(ca, cy, cz);
   }

   public static void cmul(double[][] cx, Cdouble cb, double[][] cz) {
      _cmul.apply(cx, cb, cz);
   }

   public static void cmul(double[][][] cx, double[][][] cy, double[][][] cz) {
      _cmul.apply(cx, cy, cz);
   }

   public static void cmul(Cdouble ca, double[][][] cy, double[][][] cz) {
      _cmul.apply(ca, cy, cz);
   }

   public static void cmul(double[][][] cx, Cdouble cb, double[][][] cz) {
      _cmul.apply(cx, cb, cz);
   }

   public static double[] cdiv(double[] cx, double[] cy) {
      return _cdiv.apply(cx, cy);
   }

   public static double[] cdiv(Cdouble ca, double[] cy) {
      return _cdiv.apply(ca, cy);
   }

   public static double[] cdiv(double[] cx, Cdouble cb) {
      return _cdiv.apply(cx, cb);
   }

   public static double[][] cdiv(double[][] cx, double[][] cy) {
      return _cdiv.apply(cx, cy);
   }

   public static double[][] cdiv(Cdouble ca, double[][] cy) {
      return _cdiv.apply(ca, cy);
   }

   public static double[][] cdiv(double[][] cx, Cdouble cb) {
      return _cdiv.apply(cx, cb);
   }

   public static double[][][] cdiv(double[][][] cx, double[][][] cy) {
      return _cdiv.apply(cx, cy);
   }

   public static double[][][] cdiv(Cdouble ca, double[][][] cy) {
      return _cdiv.apply(ca, cy);
   }

   public static double[][][] cdiv(double[][][] cx, Cdouble cb) {
      return _cdiv.apply(cx, cb);
   }

   public static void cdiv(double[] cx, double[] cy, double[] cz) {
      _cdiv.apply(cx, cy, cz);
   }

   public static void cdiv(Cdouble ca, double[] cy, double[] cz) {
      _cdiv.apply(ca, cy, cz);
   }

   public static void cdiv(double[] cx, Cdouble cb, double[] cz) {
      _cdiv.apply(cx, cb, cz);
   }

   public static void cdiv(double[][] cx, double[][] cy, double[][] cz) {
      _cdiv.apply(cx, cy, cz);
   }

   public static void cdiv(Cdouble ca, double[][] cy, double[][] cz) {
      _cdiv.apply(ca, cy, cz);
   }

   public static void cdiv(double[][] cx, Cdouble cb, double[][] cz) {
      _cdiv.apply(cx, cb, cz);
   }

   public static void cdiv(double[][][] cx, double[][][] cy, double[][][] cz) {
      _cdiv.apply(cx, cy, cz);
   }

   public static void cdiv(Cdouble ca, double[][][] cy, double[][][] cz) {
      _cdiv.apply(ca, cy, cz);
   }

   public static void cdiv(double[][][] cx, Cdouble cb, double[][][] cz) {
      _cdiv.apply(cx, cb, cz);
   }

   public static float[] cneg(float[] cx) {
      return _cneg.apply(cx);
   }

   public static float[][] cneg(float[][] cx) {
      return _cneg.apply(cx);
   }

   public static float[][][] cneg(float[][][] cx) {
      return _cneg.apply(cx);
   }

   public static void cneg(float[] cx, float[] cy) {
      _cneg.apply(cx, cy);
   }

   public static void cneg(float[][] cx, float[][] cy) {
      _cneg.apply(cx, cy);
   }

   public static void cneg(float[][][] cx, float[][][] cy) {
      _cneg.apply(cx, cy);
   }

   public static float[] cconj(float[] cx) {
      return _cconj.apply(cx);
   }

   public static float[][] cconj(float[][] cx) {
      return _cconj.apply(cx);
   }

   public static float[][][] cconj(float[][][] cx) {
      return _cconj.apply(cx);
   }

   public static void cconj(float[] cx, float[] cy) {
      _cconj.apply(cx, cy);
   }

   public static void cconj(float[][] cx, float[][] cy) {
      _cconj.apply(cx, cy);
   }

   public static void cconj(float[][][] cx, float[][][] cy) {
      _cconj.apply(cx, cy);
   }

   public static float[] ccos(float[] cx) {
      return _ccos.apply(cx);
   }

   public static float[][] ccos(float[][] cx) {
      return _ccos.apply(cx);
   }

   public static float[][][] ccos(float[][][] cx) {
      return _ccos.apply(cx);
   }

   public static void ccos(float[] cx, float[] cy) {
      _ccos.apply(cx, cy);
   }

   public static void ccos(float[][] cx, float[][] cy) {
      _ccos.apply(cx, cy);
   }

   public static void ccos(float[][][] cx, float[][][] cy) {
      _ccos.apply(cx, cy);
   }

   public static float[] csin(float[] cx) {
      return _csin.apply(cx);
   }

   public static float[][] csin(float[][] cx) {
      return _csin.apply(cx);
   }

   public static float[][][] csin(float[][][] cx) {
      return _csin.apply(cx);
   }

   public static void csin(float[] cx, float[] cy) {
      _csin.apply(cx, cy);
   }

   public static void csin(float[][] cx, float[][] cy) {
      _csin.apply(cx, cy);
   }

   public static void csin(float[][][] cx, float[][][] cy) {
      _csin.apply(cx, cy);
   }

   public static float[] csqrt(float[] cx) {
      return _csqrt.apply(cx);
   }

   public static float[][] csqrt(float[][] cx) {
      return _csqrt.apply(cx);
   }

   public static float[][][] csqrt(float[][][] cx) {
      return _csqrt.apply(cx);
   }

   public static void csqrt(float[] cx, float[] cy) {
      _csqrt.apply(cx, cy);
   }

   public static void csqrt(float[][] cx, float[][] cy) {
      _csqrt.apply(cx, cy);
   }

   public static void csqrt(float[][][] cx, float[][][] cy) {
      _csqrt.apply(cx, cy);
   }

   public static float[] cexp(float[] cx) {
      return _cexp.apply(cx);
   }

   public static float[][] cexp(float[][] cx) {
      return _cexp.apply(cx);
   }

   public static float[][][] cexp(float[][][] cx) {
      return _cexp.apply(cx);
   }

   public static void cexp(float[] cx, float[] cy) {
      _cexp.apply(cx, cy);
   }

   public static void cexp(float[][] cx, float[][] cy) {
      _cexp.apply(cx, cy);
   }

   public static void cexp(float[][][] cx, float[][][] cy) {
      _cexp.apply(cx, cy);
   }

   public static float[] clog(float[] cx) {
      return _clog.apply(cx);
   }

   public static float[][] clog(float[][] cx) {
      return _clog.apply(cx);
   }

   public static float[][][] clog(float[][][] cx) {
      return _clog.apply(cx);
   }

   public static void clog(float[] cx, float[] cy) {
      _clog.apply(cx, cy);
   }

   public static void clog(float[][] cx, float[][] cy) {
      _clog.apply(cx, cy);
   }

   public static void clog(float[][][] cx, float[][][] cy) {
      _clog.apply(cx, cy);
   }

   public static float[] clog10(float[] cx) {
      return _clog10.apply(cx);
   }

   public static float[][] clog10(float[][] cx) {
      return _clog10.apply(cx);
   }

   public static float[][][] clog10(float[][][] cx) {
      return _clog10.apply(cx);
   }

   public static void clog10(float[] cx, float[] cy) {
      _clog10.apply(cx, cy);
   }

   public static void clog10(float[][] cx, float[][] cy) {
      _clog10.apply(cx, cy);
   }

   public static void clog10(float[][][] cx, float[][][] cy) {
      _clog10.apply(cx, cy);
   }

   public static double[] cneg(double[] cx) {
      return _cneg.apply(cx);
   }

   public static double[][] cneg(double[][] cx) {
      return _cneg.apply(cx);
   }

   public static double[][][] cneg(double[][][] cx) {
      return _cneg.apply(cx);
   }

   public static void cneg(double[] cx, double[] cy) {
      _cneg.apply(cx, cy);
   }

   public static void cneg(double[][] cx, double[][] cy) {
      _cneg.apply(cx, cy);
   }

   public static void cneg(double[][][] cx, double[][][] cy) {
      _cneg.apply(cx, cy);
   }

   public static double[] cconj(double[] cx) {
      return _cconj.apply(cx);
   }

   public static double[][] cconj(double[][] cx) {
      return _cconj.apply(cx);
   }

   public static double[][][] cconj(double[][][] cx) {
      return _cconj.apply(cx);
   }

   public static void cconj(double[] cx, double[] cy) {
      _cconj.apply(cx, cy);
   }

   public static void cconj(double[][] cx, double[][] cy) {
      _cconj.apply(cx, cy);
   }

   public static void cconj(double[][][] cx, double[][][] cy) {
      _cconj.apply(cx, cy);
   }

   public static double[] ccos(double[] cx) {
      return _ccos.apply(cx);
   }

   public static double[][] ccos(double[][] cx) {
      return _ccos.apply(cx);
   }

   public static double[][][] ccos(double[][][] cx) {
      return _ccos.apply(cx);
   }

   public static void ccos(double[] cx, double[] cy) {
      _ccos.apply(cx, cy);
   }

   public static void ccos(double[][] cx, double[][] cy) {
      _ccos.apply(cx, cy);
   }

   public static void ccos(double[][][] cx, double[][][] cy) {
      _ccos.apply(cx, cy);
   }

   public static double[] csin(double[] cx) {
      return _csin.apply(cx);
   }

   public static double[][] csin(double[][] cx) {
      return _csin.apply(cx);
   }

   public static double[][][] csin(double[][][] cx) {
      return _csin.apply(cx);
   }

   public static void csin(double[] cx, double[] cy) {
      _csin.apply(cx, cy);
   }

   public static void csin(double[][] cx, double[][] cy) {
      _csin.apply(cx, cy);
   }

   public static void csin(double[][][] cx, double[][][] cy) {
      _csin.apply(cx, cy);
   }

   public static double[] csqrt(double[] cx) {
      return _csqrt.apply(cx);
   }

   public static double[][] csqrt(double[][] cx) {
      return _csqrt.apply(cx);
   }

   public static double[][][] csqrt(double[][][] cx) {
      return _csqrt.apply(cx);
   }

   public static void csqrt(double[] cx, double[] cy) {
      _csqrt.apply(cx, cy);
   }

   public static void csqrt(double[][] cx, double[][] cy) {
      _csqrt.apply(cx, cy);
   }

   public static void csqrt(double[][][] cx, double[][][] cy) {
      _csqrt.apply(cx, cy);
   }

   public static double[] cexp(double[] cx) {
      return _cexp.apply(cx);
   }

   public static double[][] cexp(double[][] cx) {
      return _cexp.apply(cx);
   }

   public static double[][][] cexp(double[][][] cx) {
      return _cexp.apply(cx);
   }

   public static void cexp(double[] cx, double[] cy) {
      _cexp.apply(cx, cy);
   }

   public static void cexp(double[][] cx, double[][] cy) {
      _cexp.apply(cx, cy);
   }

   public static void cexp(double[][][] cx, double[][][] cy) {
      _cexp.apply(cx, cy);
   }

   public static double[] clog(double[] cx) {
      return _clog.apply(cx);
   }

   public static double[][] clog(double[][] cx) {
      return _clog.apply(cx);
   }

   public static double[][][] clog(double[][][] cx) {
      return _clog.apply(cx);
   }

   public static void clog(double[] cx, double[] cy) {
      _clog.apply(cx, cy);
   }

   public static void clog(double[][] cx, double[][] cy) {
      _clog.apply(cx, cy);
   }

   public static void clog(double[][][] cx, double[][][] cy) {
      _clog.apply(cx, cy);
   }

   public static double[] clog10(double[] cx) {
      return _clog10.apply(cx);
   }

   public static double[][] clog10(double[][] cx) {
      return _clog10.apply(cx);
   }

   public static double[][][] clog10(double[][][] cx) {
      return _clog10.apply(cx);
   }

   public static void clog10(double[] cx, double[] cy) {
      _clog10.apply(cx, cy);
   }

   public static void clog10(double[][] cx, double[][] cy) {
      _clog10.apply(cx, cy);
   }

   public static void clog10(double[][][] cx, double[][][] cy) {
      _clog10.apply(cx, cy);
   }

   public static float[] cpow(float[] cx, float ra) {
      int n1 = cx.length / 2;
      float[] cy = new float[2 * n1];
      cpow(cx, ra, cy);
      return cy;
   }

   public static float[][] cpow(float[][] cx, float ra) {
      int n2 = cx.length;
      float[][] cy = new float[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         cy[i2] = cpow(cx[i2], ra);
      }

      return cy;
   }

   public static float[][][] cpow(float[][][] cx, float ra) {
      int n3 = cx.length;
      float[][][] cy = new float[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         cy[i3] = cpow(cx[i3], ra);
      }

      return cy;
   }

   public static void cpow(float[] cx, float ra, float[] cy) {
      Cfloat ct = new Cfloat();
      int n1 = cx.length / 2;
      int ir = 0;
      int ii = 1;

      for(int nn = 2 * n1; ir < nn; ii += 2) {
         ct.r = cx[ir];
         ct.i = cx[ii];
         Cfloat ce = Cfloat.pow(ct, ra);
         cy[ir] = ce.r;
         cy[ii] = ce.i;
         ir += 2;
      }
   }

   public static void cpow(float[][] cx, float ra, float[][] cy) {
      int n2 = cx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         cpow(cx[i2], ra, cy[i2]);
      }
   }

   public static void cpow(float[][][] cx, float ra, float[][][] cy) {
      int n3 = cx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         cpow(cx[i3], ra, cy[i3]);
      }
   }

   public static float[] cpow(float[] cx, Cfloat ca) {
      int n1 = cx.length / 2;
      float[] cy = new float[2 * n1];
      cpow(cx, ca, cy);
      return cy;
   }

   public static float[][] cpow(float[][] cx, Cfloat ca) {
      int n2 = cx.length;
      float[][] cy = new float[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         cy[i2] = cpow(cx[i2], ca);
      }

      return cy;
   }

   public static float[][][] cpow(float[][][] cx, Cfloat ca) {
      int n3 = cx.length;
      float[][][] cy = new float[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         cy[i3] = cpow(cx[i3], ca);
      }

      return cy;
   }

   public static void cpow(float[] cx, Cfloat ca, float[] cy) {
      Cfloat ct = new Cfloat();
      int n1 = cx.length / 2;
      int ir = 0;
      int ii = 1;

      for(int nn = 2 * n1; ir < nn; ii += 2) {
         ct.r = cx[ir];
         ct.i = cx[ii];
         Cfloat ce = Cfloat.pow(ct, ca);
         cy[ir] = ce.r;
         cy[ii] = ce.i;
         ir += 2;
      }
   }

   public static void cpow(float[][] cx, Cfloat ca, float[][] cy) {
      int n2 = cx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         cpow(cx[i2], ca, cy[i2]);
      }
   }

   public static void cpow(float[][][] cx, Cfloat ca, float[][][] cy) {
      int n3 = cx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         cpow(cx[i3], ca, cy[i3]);
      }
   }

   public static double[] cpow(double[] cx, double ra) {
      int n1 = cx.length / 2;
      double[] cy = new double[2 * n1];
      cpow(cx, ra, cy);
      return cy;
   }

   public static double[][] cpow(double[][] cx, double ra) {
      int n2 = cx.length;
      double[][] cy = new double[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         cy[i2] = cpow(cx[i2], ra);
      }

      return cy;
   }

   public static double[][][] cpow(double[][][] cx, double ra) {
      int n3 = cx.length;
      double[][][] cy = new double[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         cy[i3] = cpow(cx[i3], ra);
      }

      return cy;
   }

   public static void cpow(double[] cx, double ra, double[] cy) {
      Cdouble ct = new Cdouble();
      int n1 = cx.length / 2;
      int ir = 0;
      int ii = 1;

      for(int nn = 2 * n1; ir < nn; ii += 2) {
         ct.r = cx[ir];
         ct.i = cx[ii];
         Cdouble ce = Cdouble.pow(ct, ra);
         cy[ir] = ce.r;
         cy[ii] = ce.i;
         ir += 2;
      }
   }

   public static void cpow(double[][] cx, double ra, double[][] cy) {
      int n2 = cx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         cpow(cx[i2], ra, cy[i2]);
      }
   }

   public static void cpow(double[][][] cx, double ra, double[][][] cy) {
      int n3 = cx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         cpow(cx[i3], ra, cy[i3]);
      }
   }

   public static double[] cpow(double[] cx, Cdouble ca) {
      int n1 = cx.length / 2;
      double[] cy = new double[2 * n1];
      cpow(cx, ca, cy);
      return cy;
   }

   public static double[][] cpow(double[][] cx, Cdouble ca) {
      int n2 = cx.length;
      double[][] cy = new double[n2][];

      for(int i2 = 0; i2 < n2; ++i2) {
         cy[i2] = cpow(cx[i2], ca);
      }

      return cy;
   }

   public static double[][][] cpow(double[][][] cx, Cdouble ca) {
      int n3 = cx.length;
      double[][][] cy = new double[n3][][];

      for(int i3 = 0; i3 < n3; ++i3) {
         cy[i3] = cpow(cx[i3], ca);
      }

      return cy;
   }

   public static void cpow(double[] cx, Cdouble ca, double[] cy) {
      Cdouble ct = new Cdouble();
      int n1 = cx.length / 2;
      int ir = 0;
      int ii = 1;

      for(int nn = 2 * n1; ir < nn; ii += 2) {
         ct.r = cx[ir];
         ct.i = cx[ii];
         Cdouble ce = Cdouble.pow(ct, ca);
         cy[ir] = ce.r;
         cy[ii] = ce.i;
         ir += 2;
      }
   }

   public static void cpow(double[][] cx, Cdouble ca, double[][] cy) {
      int n2 = cx.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         cpow(cx[i2], ca, cy[i2]);
      }
   }

   public static void cpow(double[][][] cx, Cdouble ca, double[][][] cy) {
      int n3 = cx.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         cpow(cx[i3], ca, cy[i3]);
      }
   }

   public static float[] creal(float[] cx) {
      return _creal.apply(cx);
   }

   public static float[][] creal(float[][] cx) {
      return _creal.apply(cx);
   }

   public static float[][][] creal(float[][][] cx) {
      return _creal.apply(cx);
   }

   public static void creal(float[] cx, float[] cy) {
      _creal.apply(cx, cy);
   }

   public static void creal(float[][] cx, float[][] cy) {
      _creal.apply(cx, cy);
   }

   public static void creal(float[][][] cx, float[][][] cy) {
      _creal.apply(cx, cy);
   }

   public static float[] cimag(float[] cx) {
      return _cimag.apply(cx);
   }

   public static float[][] cimag(float[][] cx) {
      return _cimag.apply(cx);
   }

   public static float[][][] cimag(float[][][] cx) {
      return _cimag.apply(cx);
   }

   public static void cimag(float[] cx, float[] cy) {
      _cimag.apply(cx, cy);
   }

   public static void cimag(float[][] cx, float[][] cy) {
      _cimag.apply(cx, cy);
   }

   public static void cimag(float[][][] cx, float[][][] cy) {
      _cimag.apply(cx, cy);
   }

   public static float[] cabs(float[] cx) {
      return _cabs.apply(cx);
   }

   public static float[][] cabs(float[][] cx) {
      return _cabs.apply(cx);
   }

   public static float[][][] cabs(float[][][] cx) {
      return _cabs.apply(cx);
   }

   public static void cabs(float[] cx, float[] cy) {
      _cabs.apply(cx, cy);
   }

   public static void cabs(float[][] cx, float[][] cy) {
      _cabs.apply(cx, cy);
   }

   public static void cabs(float[][][] cx, float[][][] cy) {
      _cabs.apply(cx, cy);
   }

   public static float[] carg(float[] cx) {
      return _carg.apply(cx);
   }

   public static float[][] carg(float[][] cx) {
      return _carg.apply(cx);
   }

   public static float[][][] carg(float[][][] cx) {
      return _carg.apply(cx);
   }

   public static void carg(float[] cx, float[] cy) {
      _carg.apply(cx, cy);
   }

   public static void carg(float[][] cx, float[][] cy) {
      _carg.apply(cx, cy);
   }

   public static void carg(float[][][] cx, float[][][] cy) {
      _carg.apply(cx, cy);
   }

   public static float[] cnorm(float[] cx) {
      return _cnorm.apply(cx);
   }

   public static float[][] cnorm(float[][] cx) {
      return _cnorm.apply(cx);
   }

   public static float[][][] cnorm(float[][][] cx) {
      return _cnorm.apply(cx);
   }

   public static void cnorm(float[] cx, float[] cy) {
      _cnorm.apply(cx, cy);
   }

   public static void cnorm(float[][] cx, float[][] cy) {
      _cnorm.apply(cx, cy);
   }

   public static void cnorm(float[][][] cx, float[][][] cy) {
      _cnorm.apply(cx, cy);
   }

   public static double[] creal(double[] cx) {
      return _creal.apply(cx);
   }

   public static double[][] creal(double[][] cx) {
      return _creal.apply(cx);
   }

   public static double[][][] creal(double[][][] cx) {
      return _creal.apply(cx);
   }

   public static void creal(double[] cx, double[] cy) {
      _creal.apply(cx, cy);
   }

   public static void creal(double[][] cx, double[][] cy) {
      _creal.apply(cx, cy);
   }

   public static void creal(double[][][] cx, double[][][] cy) {
      _creal.apply(cx, cy);
   }

   public static double[] cimag(double[] cx) {
      return _cimag.apply(cx);
   }

   public static double[][] cimag(double[][] cx) {
      return _cimag.apply(cx);
   }

   public static double[][][] cimag(double[][][] cx) {
      return _cimag.apply(cx);
   }

   public static void cimag(double[] cx, double[] cy) {
      _cimag.apply(cx, cy);
   }

   public static void cimag(double[][] cx, double[][] cy) {
      _cimag.apply(cx, cy);
   }

   public static void cimag(double[][][] cx, double[][][] cy) {
      _cimag.apply(cx, cy);
   }

   public static double[] cabs(double[] cx) {
      return _cabs.apply(cx);
   }

   public static double[][] cabs(double[][] cx) {
      return _cabs.apply(cx);
   }

   public static double[][][] cabs(double[][][] cx) {
      return _cabs.apply(cx);
   }

   public static void cabs(double[] cx, double[] cy) {
      _cabs.apply(cx, cy);
   }

   public static void cabs(double[][] cx, double[][] cy) {
      _cabs.apply(cx, cy);
   }

   public static void cabs(double[][][] cx, double[][][] cy) {
      _cabs.apply(cx, cy);
   }

   public static double[] carg(double[] cx) {
      return _carg.apply(cx);
   }

   public static double[][] carg(double[][] cx) {
      return _carg.apply(cx);
   }

   public static double[][][] carg(double[][][] cx) {
      return _carg.apply(cx);
   }

   public static void carg(double[] cx, double[] cy) {
      _carg.apply(cx, cy);
   }

   public static void carg(double[][] cx, double[][] cy) {
      _carg.apply(cx, cy);
   }

   public static void carg(double[][][] cx, double[][][] cy) {
      _carg.apply(cx, cy);
   }

   public static double[] cnorm(double[] cx) {
      return _cnorm.apply(cx);
   }

   public static double[][] cnorm(double[][] cx) {
      return _cnorm.apply(cx);
   }

   public static double[][][] cnorm(double[][][] cx) {
      return _cnorm.apply(cx);
   }

   public static void cnorm(double[] cx, double[] cy) {
      _cnorm.apply(cx, cy);
   }

   public static void cnorm(double[][] cx, double[][] cy) {
      _cnorm.apply(cx, cy);
   }

   public static void cnorm(double[][][] cx, double[][][] cy) {
      _cnorm.apply(cx, cy);
   }

   public static float[] cmplx(float[] rx, float[] ry) {
      return _cmplx.apply(rx, ry);
   }

   public static float[][] cmplx(float[][] rx, float[][] ry) {
      return _cmplx.apply(rx, ry);
   }

   public static float[][][] cmplx(float[][][] rx, float[][][] ry) {
      return _cmplx.apply(rx, ry);
   }

   public static void cmplx(float[] rx, float[] ry, float[] cz) {
      _cmplx.apply(rx, ry, cz);
   }

   public static void cmplx(float[][] rx, float[][] ry, float[][] cz) {
      _cmplx.apply(rx, ry, cz);
   }

   public static void cmplx(float[][][] rx, float[][][] ry, float[][][] cz) {
      _cmplx.apply(rx, ry, cz);
   }

   public static float[] polar(float[] rx, float[] ry) {
      return _polar.apply(rx, ry);
   }

   public static float[][] polar(float[][] rx, float[][] ry) {
      return _polar.apply(rx, ry);
   }

   public static float[][][] polar(float[][][] rx, float[][][] ry) {
      return _polar.apply(rx, ry);
   }

   public static void polar(float[] rx, float[] ry, float[] cz) {
      _polar.apply(rx, ry, cz);
   }

   public static void polar(float[][] rx, float[][] ry, float[][] cz) {
      _polar.apply(rx, ry, cz);
   }

   public static void polar(float[][][] rx, float[][][] ry, float[][][] cz) {
      _polar.apply(rx, ry, cz);
   }

   public static double[] cmplx(double[] rx, double[] ry) {
      return _cmplx.apply(rx, ry);
   }

   public static double[][] cmplx(double[][] rx, double[][] ry) {
      return _cmplx.apply(rx, ry);
   }

   public static double[][][] cmplx(double[][][] rx, double[][][] ry) {
      return _cmplx.apply(rx, ry);
   }

   public static void cmplx(double[] rx, double[] ry, double[] cz) {
      _cmplx.apply(rx, ry, cz);
   }

   public static void cmplx(double[][] rx, double[][] ry, double[][] cz) {
      _cmplx.apply(rx, ry, cz);
   }

   public static void cmplx(double[][][] rx, double[][][] ry, double[][][] cz) {
      _cmplx.apply(rx, ry, cz);
   }

   public static double[] polar(double[] rx, double[] ry) {
      return _polar.apply(rx, ry);
   }

   public static double[][] polar(double[][] rx, double[][] ry) {
      return _polar.apply(rx, ry);
   }

   public static double[][][] polar(double[][][] rx, double[][][] ry) {
      return _polar.apply(rx, ry);
   }

   public static void polar(double[] rx, double[] ry, double[] cz) {
      _polar.apply(rx, ry, cz);
   }

   public static void polar(double[][] rx, double[][] ry, double[][] cz) {
      _polar.apply(rx, ry, cz);
   }

   public static void polar(double[][][] rx, double[][][] ry, double[][][] cz) {
      _polar.apply(rx, ry, cz);
   }

   public static Cfloat csum(float[] cx) {
      int n1 = cx.length / 2;
      float sr = 0.0F;
      float si = 0.0F;
      int i1 = 0;
      int ir = 0;

      for(int ii = 1; i1 < n1; ii += 2) {
         sr += cx[ir];
         si += cx[ii];
         ++i1;
         ir += 2;
      }

      return new Cfloat(sr, si);
   }

   public static Cfloat csum(float[][] cx) {
      int n2 = cx.length;
      Cfloat s = new Cfloat();

      for(int i2 = 0; i2 < n2; ++i2) {
         s.plusEquals(csum(cx[i2]));
      }

      return s;
   }

   public static Cfloat csum(float[][][] cx) {
      int n3 = cx.length;
      Cfloat s = new Cfloat();

      for(int i3 = 0; i3 < n3; ++i3) {
         s.plusEquals(csum(cx[i3]));
      }

      return s;
   }

   public static Cdouble csum(double[] cx) {
      int n1 = cx.length / 2;
      double sr = 0.0;
      double si = 0.0;
      int i1 = 0;
      int ir = 0;

      for(int ii = 1; i1 < n1; ii += 2) {
         sr += cx[ir];
         si += cx[ii];
         ++i1;
         ir += 2;
      }

      return new Cdouble(sr, si);
   }

   public static Cdouble csum(double[][] cx) {
      int n2 = cx.length;
      Cdouble s = new Cdouble();

      for(int i2 = 0; i2 < n2; ++i2) {
         s.plusEquals(csum(cx[i2]));
      }

      return s;
   }

   public static Cdouble csum(double[][][] cx) {
      int n3 = cx.length;
      Cdouble s = new Cdouble();

      for(int i3 = 0; i3 < n3; ++i3) {
         s.plusEquals(csum(cx[i3]));
      }

      return s;
   }

   public static float[] tofloat(byte[] rx) {
      int n1 = rx.length;
      float[] ry = new float[n1];

      for(int i1 = 0; i1 < n1; ++i1) {
         ry[i1] = (float)rx[i1];
      }

      return ry;
   }

   public static float[][] tofloat(byte[][] rx) {
      int n1 = rx[0].length;
      int n2 = rx.length;
      float[][] ry = new float[n2][n1];

      for(int i2 = 0; i2 < n2; ++i2) {
         for(int i1 = 0; i1 < n1; ++i1) {
            ry[i2][i1] = (float)rx[i2][i1];
         }
      }

      return ry;
   }

   public static float[][][] tofloat(byte[][][] rx) {
      int n1 = rx[0][0].length;
      int n2 = rx[0].length;
      int n3 = rx.length;
      float[][][] ry = new float[n3][n2][n1];

      for(int i3 = 0; i3 < n3; ++i3) {
         for(int i2 = 0; i2 < n2; ++i2) {
            for(int i1 = 0; i1 < n1; ++i1) {
               ry[i3][i2][i1] = (float)rx[i3][i2][i1];
            }
         }
      }

      return ry;
   }

   public static void dump(byte[] rx) {
      ArrayMath.ByteIterator li = new ArrayMath.ByteIterator(rx);
      String[] s = format(li);
      dump(s);
   }

   public static void dump(byte[][] rx) {
      ArrayMath.ByteIterator li = new ArrayMath.ByteIterator(rx);
      String[] s = format(li);
      int n2 = rx.length;
      int[] n = new int[n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         n[i2] = rx[i2].length;
      }

      dump(n, s);
   }

   public static void dump(byte[][][] rx) {
      ArrayMath.ByteIterator li = new ArrayMath.ByteIterator(rx);
      String[] s = format(li);
      int n3 = rx.length;
      int[][] n = new int[n3][];

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;
         n[i3] = new int[n2];

         for(int i2 = 0; i2 < n2; ++i2) {
            n[i3][i2] = rx[i3][i2].length;
         }
      }

      dump(n, s);
   }

   public static void dump(short[] rx) {
      ArrayMath.ShortIterator li = new ArrayMath.ShortIterator(rx);
      String[] s = format(li);
      dump(s);
   }

   public static void dump(short[][] rx) {
      ArrayMath.ShortIterator li = new ArrayMath.ShortIterator(rx);
      String[] s = format(li);
      int n2 = rx.length;
      int[] n = new int[n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         n[i2] = rx[i2].length;
      }

      dump(n, s);
   }

   public static void dump(short[][][] rx) {
      ArrayMath.ShortIterator li = new ArrayMath.ShortIterator(rx);
      String[] s = format(li);
      int n3 = rx.length;
      int[][] n = new int[n3][];

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;
         n[i3] = new int[n2];

         for(int i2 = 0; i2 < n2; ++i2) {
            n[i3][i2] = rx[i3][i2].length;
         }
      }

      dump(n, s);
   }

   public static void dump(int[] rx) {
      ArrayMath.IntIterator li = new ArrayMath.IntIterator(rx);
      String[] s = format(li);
      dump(s);
   }

   public static void dump(int[][] rx) {
      ArrayMath.IntIterator li = new ArrayMath.IntIterator(rx);
      String[] s = format(li);
      int n2 = rx.length;
      int[] n = new int[n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         n[i2] = rx[i2].length;
      }

      dump(n, s);
   }

   public static void dump(int[][][] rx) {
      ArrayMath.IntIterator li = new ArrayMath.IntIterator(rx);
      String[] s = format(li);
      int n3 = rx.length;
      int[][] n = new int[n3][];

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;
         n[i3] = new int[n2];

         for(int i2 = 0; i2 < n2; ++i2) {
            n[i3][i2] = rx[i3][i2].length;
         }
      }

      dump(n, s);
   }

   public static void dump(long[] rx) {
      ArrayMath.LongIterator li = new ArrayMath.LongIterator(rx);
      String[] s = format(li);
      dump(s);
   }

   public static void dump(long[][] rx) {
      ArrayMath.LongIterator li = new ArrayMath.LongIterator(rx);
      String[] s = format(li);
      int n2 = rx.length;
      int[] n = new int[n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         n[i2] = rx[i2].length;
      }

      dump(n, s);
   }

   public static void dump(long[][][] rx) {
      ArrayMath.LongIterator li = new ArrayMath.LongIterator(rx);
      String[] s = format(li);
      int n3 = rx.length;
      int[][] n = new int[n3][];

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;
         n[i3] = new int[n2];

         for(int i2 = 0; i2 < n2; ++i2) {
            n[i3][i2] = rx[i3][i2].length;
         }
      }

      dump(n, s);
   }

   public static void dump(float[] rx) {
      ArrayMath.FloatIterator di = new ArrayMath.FloatIterator(rx);
      String[] s = format(di);
      dump(s);
   }

   public static void dump(float[][] rx) {
      ArrayMath.FloatIterator di = new ArrayMath.FloatIterator(rx);
      String[] s = format(di);
      int n2 = rx.length;
      int[] n = new int[n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         n[i2] = rx[i2].length;
      }

      dump(n, s);
   }

   public static void dump(float[][][] rx) {
      ArrayMath.FloatIterator di = new ArrayMath.FloatIterator(rx);
      String[] s = format(di);
      int n3 = rx.length;
      int[][] n = new int[n3][];

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;
         n[i3] = new int[n2];

         for(int i2 = 0; i2 < n2; ++i2) {
            n[i3][i2] = rx[i3][i2].length;
         }
      }

      dump(n, s);
   }

   public static void cdump(float[] cx) {
      ArrayMath.FloatIterator di = new ArrayMath.FloatIterator(cx);
      String[] s = format(di);
      cdump(s);
   }

   public static void cdump(float[][] cx) {
      ArrayMath.FloatIterator di = new ArrayMath.FloatIterator(cx);
      String[] s = format(di);
      int n2 = cx.length;
      int[] n = new int[n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         n[i2] = cx[i2].length / 2;
      }

      cdump(n, s);
   }

   public static void cdump(float[][][] cx) {
      ArrayMath.FloatIterator di = new ArrayMath.FloatIterator(cx);
      String[] s = format(di);
      int n3 = cx.length;
      int[][] n = new int[n3][];

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = cx[i3].length;
         n[i3] = new int[n2];

         for(int i2 = 0; i2 < n2; ++i2) {
            n[i3][i2] = cx[i3][i2].length / 2;
         }
      }

      cdump(n, s);
   }

   public static void dump(double[] rx) {
      ArrayMath.DoubleIterator di = new ArrayMath.DoubleIterator(rx);
      String[] s = format(di);
      dump(s);
   }

   public static void dump(double[][] rx) {
      ArrayMath.DoubleIterator di = new ArrayMath.DoubleIterator(rx);
      String[] s = format(di);
      int n2 = rx.length;
      int[] n = new int[n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         n[i2] = rx[i2].length;
      }

      dump(n, s);
   }

   public static void dump(double[][][] rx) {
      ArrayMath.DoubleIterator di = new ArrayMath.DoubleIterator(rx);
      String[] s = format(di);
      int n3 = rx.length;
      int[][] n = new int[n3][];

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = rx[i3].length;
         n[i3] = new int[n2];

         for(int i2 = 0; i2 < n2; ++i2) {
            n[i3][i2] = rx[i3][i2].length;
         }
      }

      dump(n, s);
   }

   public static void cdump(double[] cx) {
      ArrayMath.DoubleIterator di = new ArrayMath.DoubleIterator(cx);
      String[] s = format(di);
      cdump(s);
   }

   public static void cdump(double[][] cx) {
      ArrayMath.DoubleIterator di = new ArrayMath.DoubleIterator(cx);
      String[] s = format(di);
      int n2 = cx.length;
      int[] n = new int[n2];

      for(int i2 = 0; i2 < n2; ++i2) {
         n[i2] = cx[i2].length / 2;
      }

      cdump(n, s);
   }

   public static void cdump(double[][][] cx) {
      ArrayMath.DoubleIterator di = new ArrayMath.DoubleIterator(cx);
      String[] s = format(di);
      int n3 = cx.length;
      int[][] n = new int[n3][];

      for(int i3 = 0; i3 < n3; ++i3) {
         int n2 = cx[i3].length;
         n[i3] = new int[n2];

         for(int i2 = 0; i2 < n2; ++i2) {
            n[i3][i2] = cx[i3][i2].length / 2;
         }
      }

      cdump(n, s);
   }

   private static int maxlen(String[] s) {
      int max = 0;
      int n = s.length;

      for(int i = 0; i < n; ++i) {
         int len = s[i].length();
         if (max < len) {
            max = len;
         }
      }

      return max;
   }

   private static void dump(String[] s) {
      int max = maxlen(s);
      int n1 = s.length;
      String format = "%" + max + "s";
      System.out.print("{");
      int ncol = 78 / (max + 2);
      int nrow = 1 + (n1 - 1) / ncol;
      if (nrow > 1 && ncol >= 5) {
         ncol = ncol / 5 * 5;
         nrow = 1 + (n1 - 1) / ncol;
      }

      int irow = 0;

      for(int i1 = 0; irow < nrow; ++irow) {
         for(int icol = 0; icol < ncol && i1 < n1; ++i1) {
            System.out.printf(format, s[i1]);
            if (i1 < n1 - 1) {
               System.out.print(", ");
            }

            ++icol;
         }

         if (i1 < n1) {
            System.out.println();
            System.out.print(" ");
         } else {
            System.out.println("}");
         }
      }
   }

   private static void dump(int[] n, String[] s) {
      int max = maxlen(s);
      int n2 = n.length;
      String format = "%" + max + "s";
      System.out.print("{{");
      int ncol = 77 / (max + 2);
      if (ncol >= 5) {
         ncol = ncol / 5 * 5;
      }

      int i2 = 0;

      for(int i = 0; i2 < n2; ++i2) {
         int n1 = n[i2];
         int nrow = 1 + (n1 - 1) / ncol;
         if (i2 > 0) {
            System.out.print(" {");
         }

         int irow = 0;

         for(int i1 = 0; irow < nrow; ++irow) {
            for(int icol = 0; icol < ncol && i1 < n1; ++i) {
               System.out.printf(format, s[i]);
               if (i1 < n1 - 1) {
                  System.out.print(", ");
               }

               ++icol;
               ++i1;
            }

            if (i1 < n1) {
               System.out.println();
               System.out.print("  ");
            } else if (i2 < n2 - 1) {
               System.out.println("},");
            } else {
               System.out.println("}}");
            }
         }
      }
   }

   private static void dump(int[][] n, String[] s) {
      int max = maxlen(s);
      int n3 = n.length;
      String format = "%" + max + "s";
      System.out.print("{{{");
      int ncol = 76 / (max + 2);
      if (ncol >= 5) {
         ncol = ncol / 5 * 5;
      }

      int i3 = 0;

      for(int i = 0; i3 < n3; ++i3) {
         if (i3 > 0) {
            System.out.print(" {{");
         }

         int n2 = n[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            int n1 = n[i3][i2];
            int nrow = 1 + (n1 - 1) / ncol;
            if (i2 > 0) {
               System.out.print("  {");
            }

            int irow = 0;

            for(int i1 = 0; irow < nrow; ++irow) {
               for(int icol = 0; icol < ncol && i1 < n1; ++i) {
                  System.out.printf(format, s[i]);
                  if (i1 < n1 - 1) {
                     System.out.print(", ");
                  }

                  ++icol;
                  ++i1;
               }

               if (i1 < n1) {
                  System.out.println();
                  System.out.print("   ");
               } else if (i2 < n2 - 1) {
                  System.out.println("},");
               } else if (i3 < n3 - 1) {
                  System.out.println("}},");
               } else {
                  System.out.println("}}}");
               }
            }
         }
      }
   }

   private static void format(String sr, String si, StringBuilder sb) {
      sb.delete(0, sb.length());
      sb.append('(');
      if (sr.charAt(0) == ' ') {
         sb.append(sr.substring(1, sr.length()));
      } else {
         sb.append(sr);
      }

      if (si.charAt(0) == ' ') {
         sb.append('+');
         sb.append(si.substring(1, si.length()));
      } else {
         sb.append(si);
      }

      sb.append('i');
      sb.append(')');
   }

   private static void cdump(String[] s) {
      int max = 2 * maxlen(s) + 3;
      StringBuilder sb = new StringBuilder(max);
      int n1 = s.length / 2;
      String format = "%" + max + "s";
      System.out.print("{");
      int ncol = 78 / (max + 2);
      int nrow = 1 + (n1 - 1) / ncol;
      if (nrow > 1 && ncol >= 5) {
         ncol = ncol / 5 * 5;
         nrow = 1 + (n1 - 1) / ncol;
      }

      int irow = 0;
      int i1 = 0;
      int ir = 0;

      for(int ii = 1; irow < nrow; ++irow) {
         for(int icol = 0; icol < ncol && i1 < n1; ii += 2) {
            format(s[ir], s[ii], sb);
            System.out.printf(format, sb);
            if (i1 < n1 - 1) {
               System.out.print(", ");
            }

            ++icol;
            ++i1;
            ir += 2;
         }

         if (i1 < n1) {
            System.out.println();
            System.out.print(" ");
         } else {
            System.out.println("}");
         }
      }
   }

   private static void cdump(int[] n, String[] s) {
      int max = 2 * maxlen(s) + 3;
      StringBuilder sb = new StringBuilder(max);
      int n2 = n.length;
      String format = "%" + max + "s";
      System.out.print("{{");
      int ncol = 77 / (max + 2);
      if (ncol >= 5) {
         ncol = ncol / 5 * 5;
      }

      int i2 = 0;
      int ir = 0;

      for(int ii = 1; i2 < n2; ++i2) {
         int n1 = n[i2];
         int nrow = 1 + (n1 - 1) / ncol;
         if (i2 > 0) {
            System.out.print(" {");
         }

         int irow = 0;

         for(int i1 = 0; irow < nrow; ++irow) {
            for(int icol = 0; icol < ncol && i1 < n1; ii += 2) {
               format(s[ir], s[ii], sb);
               System.out.printf(format, sb);
               if (i1 < n1 - 1) {
                  System.out.print(", ");
               }

               ++icol;
               ++i1;
               ir += 2;
            }

            if (i1 < n1) {
               System.out.println();
               System.out.print("  ");
            } else if (i2 < n2 - 1) {
               System.out.println("},");
            } else {
               System.out.println("}}");
            }
         }
      }
   }

   private static void cdump(int[][] n, String[] s) {
      int max = 2 * maxlen(s) + 3;
      StringBuilder sb = new StringBuilder(max);
      int n3 = n.length;
      String format = "%" + max + "s";
      System.out.print("{{{");
      int ncol = 76 / (max + 2);
      if (ncol >= 5) {
         ncol = ncol / 5 * 5;
      }

      int i3 = 0;
      int ir = 0;

      for(int ii = 1; i3 < n3; ++i3) {
         if (i3 > 0) {
            System.out.print(" {{");
         }

         int n2 = n[i3].length;

         for(int i2 = 0; i2 < n2; ++i2) {
            int n1 = n[i3][i2];
            int nrow = 1 + (n1 - 1) / ncol;
            if (i2 > 0) {
               System.out.print("  {");
            }

            int irow = 0;

            for(int i1 = 0; irow < nrow; ++irow) {
               for(int icol = 0; icol < ncol && i1 < n1; ii += 2) {
                  format(s[ir], s[ii], sb);
                  System.out.printf(format, sb);
                  if (i1 < n1 - 1) {
                     System.out.print(", ");
                  }

                  ++icol;
                  ++i1;
                  ir += 2;
               }

               if (i1 < n1) {
                  System.out.println();
                  System.out.print("   ");
               } else if (i2 < n2 - 1) {
                  System.out.println("},");
               } else if (i3 < n3 - 1) {
                  System.out.println("}},");
               } else {
                  System.out.println("}}}");
               }
            }
         }
      }
   }

   private static String[] format(ArrayMath.ByteIterator li) {
      int n = li.count();
      String[] s = new String[n];

      for(int i = 0; i < n; ++i) {
         s[i] = String.format("% d", li.get());
      }

      return s;
   }

   private static String[] format(ArrayMath.ShortIterator li) {
      int n = li.count();
      String[] s = new String[n];

      for(int i = 0; i < n; ++i) {
         s[i] = String.format("% d", li.get());
      }

      return s;
   }

   private static String[] format(ArrayMath.IntIterator li) {
      int n = li.count();
      String[] s = new String[n];

      for(int i = 0; i < n; ++i) {
         s[i] = String.format("% d", li.get());
      }

      return s;
   }

   private static String[] format(ArrayMath.LongIterator li) {
      int n = li.count();
      String[] s = new String[n];

      for(int i = 0; i < n; ++i) {
         s[i] = String.format("% d", li.get());
      }

      return s;
   }

   private static String[] format(ArrayMath.DoubleIterator di) {
      int pg = 6;
      String fg = "% ." + pg + "g";
      int pemax = -1;
      int pfmax = -1;
      int n = di.count();

      for(int i = 0; i < n; ++i) {
         String s = String.format(fg, di.get());
         s = clean(s);
         int ls = s.length();
         if (s.contains("e")) {
            int pe = ls > 7 ? ls - 7 : 0;
            if (pemax < pe) {
               pemax = pe;
            }
         } else {
            int ip = s.indexOf(46);
            int pf = ip >= 0 ? ls - 1 - ip : 0;
            if (pfmax < pf) {
               pfmax = pf;
            }
         }
      }

      String[] s = new String[n];
      String f;
      if (pemax >= 0) {
         if (pfmax > pg - 1) {
            pfmax = pg - 1;
         }

         int pe = pemax > pfmax ? pemax : pfmax;
         f = "% ." + pe + "e";
      } else {
         f = "% ." + pfmax + "f";
      }

      di.reset();

      for(int i = 0; i < n; ++i) {
         s[i] = String.format(f, di.get());
      }

      return s;
   }

   private static String[] format(ArrayMath.FloatIterator di) {
      int pg = 6;
      String fg = "% ." + pg + "g";
      int pemax = -1;
      int pfmax = -1;
      int n = di.count();

      for(int i = 0; i < n; ++i) {
         String s = String.format(fg, di.get());
         s = clean(s);
         int ls = s.length();
         if (s.contains("e")) {
            int pe = ls > 7 ? ls - 7 : 0;
            if (pemax < pe) {
               pemax = pe;
            }
         } else {
            int ip = s.indexOf(46);
            int pf = ip >= 0 ? ls - 1 - ip : 0;
            if (pfmax < pf) {
               pfmax = pf;
            }
         }
      }

      String[] s = new String[n];
      String f;
      if (pemax >= 0) {
         if (pfmax > pg - 1) {
            pfmax = pg - 1;
         }

         int pe = pemax > pfmax ? pemax : pfmax;
         f = "% ." + pe + "e";
      } else {
         f = "% ." + pfmax + "f";
      }

      di.reset();

      for(int i = 0; i < n; ++i) {
         s[i] = String.format(f, di.get());
      }

      return s;
   }

   private static String clean(String s) {
      int len = s.length();
      int iend = s.indexOf(101);
      if (iend < 0) {
         iend = s.indexOf(69);
      }

      if (iend < 0) {
         iend = len;
      }

      int ibeg = iend;
      if (s.indexOf(46) > 0) {
         while(ibeg > 0 && s.charAt(ibeg - 1) == '0') {
            --ibeg;
         }

         if (ibeg > 0 && s.charAt(ibeg - 1) == '.') {
            --ibeg;
         }
      }

      if (ibeg < iend) {
         String sb = s.substring(0, ibeg);
         s = iend < len ? sb + s.substring(iend, len) : sb;
      }

      return s;
   }

   private ArrayMath() {
   }

   private abstract static class Binary {
      private Binary() {
      }

      float[] apply(float[] rx, float[] ry) {
         int n1 = rx.length;
         float[] rz = new float[n1];
         this.apply(rx, ry, rz);
         return rz;
      }

      float[] apply(float ra, float[] ry) {
         int n1 = ry.length;
         float[] rz = new float[n1];
         this.apply(ra, ry, rz);
         return rz;
      }

      float[] apply(float[] rx, float rb) {
         int n1 = rx.length;
         float[] rz = new float[n1];
         this.apply(rx, rb, rz);
         return rz;
      }

      float[][] apply(float[][] rx, float[][] ry) {
         int n2 = rx.length;
         float[][] rz = new float[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            rz[i2] = this.apply(rx[i2], ry[i2]);
         }

         return rz;
      }

      float[][] apply(float ra, float[][] ry) {
         int n2 = ry.length;
         float[][] rz = new float[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            rz[i2] = this.apply(ra, ry[i2]);
         }

         return rz;
      }

      float[][] apply(float[][] rx, float rb) {
         int n2 = rx.length;
         float[][] rz = new float[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            rz[i2] = this.apply(rx[i2], rb);
         }

         return rz;
      }

      float[][][] apply(float[][][] rx, float[][][] ry) {
         int n3 = rx.length;
         float[][][] rz = new float[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            rz[i3] = this.apply(rx[i3], ry[i3]);
         }

         return rz;
      }

      float[][][] apply(float ra, float[][][] ry) {
         int n3 = ry.length;
         float[][][] rz = new float[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            rz[i3] = this.apply(ra, ry[i3]);
         }

         return rz;
      }

      float[][][] apply(float[][][] rx, float rb) {
         int n3 = rx.length;
         float[][][] rz = new float[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            rz[i3] = this.apply(rx[i3], rb);
         }

         return rz;
      }

      abstract void apply(float[] var1, float[] var2, float[] var3);

      abstract void apply(float var1, float[] var2, float[] var3);

      abstract void apply(float[] var1, float var2, float[] var3);

      void apply(float[][] rx, float[][] ry, float[][] rz) {
         int n2 = rx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(rx[i2], ry[i2], rz[i2]);
         }
      }

      void apply(float ra, float[][] ry, float[][] rz) {
         int n2 = ry.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(ra, ry[i2], rz[i2]);
         }
      }

      void apply(float[][] rx, float rb, float[][] rz) {
         int n2 = rx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(rx[i2], rb, rz[i2]);
         }
      }

      void apply(float[][][] rx, float[][][] ry, float[][][] rz) {
         int n3 = rx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(rx[i3], ry[i3], rz[i3]);
         }
      }

      void apply(float ra, float[][][] ry, float[][][] rz) {
         int n3 = ry.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(ra, ry[i3], rz[i3]);
         }
      }

      void apply(float[][][] rx, float rb, float[][][] rz) {
         int n3 = rx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(rx[i3], rb, rz[i3]);
         }
      }

      double[] apply(double[] rx, double[] ry) {
         int n1 = rx.length;
         double[] rz = new double[n1];
         this.apply(rx, ry, rz);
         return rz;
      }

      double[] apply(double ra, double[] ry) {
         int n1 = ry.length;
         double[] rz = new double[n1];
         this.apply(ra, ry, rz);
         return rz;
      }

      double[] apply(double[] rx, double rb) {
         int n1 = rx.length;
         double[] rz = new double[n1];
         this.apply(rx, rb, rz);
         return rz;
      }

      double[][] apply(double[][] rx, double[][] ry) {
         int n2 = rx.length;
         double[][] rz = new double[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            rz[i2] = this.apply(rx[i2], ry[i2]);
         }

         return rz;
      }

      double[][] apply(double ra, double[][] ry) {
         int n2 = ry.length;
         double[][] rz = new double[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            rz[i2] = this.apply(ra, ry[i2]);
         }

         return rz;
      }

      double[][] apply(double[][] rx, double rb) {
         int n2 = rx.length;
         double[][] rz = new double[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            rz[i2] = this.apply(rx[i2], rb);
         }

         return rz;
      }

      double[][][] apply(double[][][] rx, double[][][] ry) {
         int n3 = rx.length;
         double[][][] rz = new double[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            rz[i3] = this.apply(rx[i3], ry[i3]);
         }

         return rz;
      }

      double[][][] apply(double ra, double[][][] ry) {
         int n3 = ry.length;
         double[][][] rz = new double[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            rz[i3] = this.apply(ra, ry[i3]);
         }

         return rz;
      }

      double[][][] apply(double[][][] rx, double rb) {
         int n3 = rx.length;
         double[][][] rz = new double[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            rz[i3] = this.apply(rx[i3], rb);
         }

         return rz;
      }

      abstract void apply(double[] var1, double[] var2, double[] var3);

      abstract void apply(double var1, double[] var3, double[] var4);

      abstract void apply(double[] var1, double var2, double[] var4);

      void apply(double[][] rx, double[][] ry, double[][] rz) {
         int n2 = rx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(rx[i2], ry[i2], rz[i2]);
         }
      }

      void apply(double ra, double[][] ry, double[][] rz) {
         int n2 = ry.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(ra, ry[i2], rz[i2]);
         }
      }

      void apply(double[][] rx, double rb, double[][] rz) {
         int n2 = rx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(rx[i2], rb, rz[i2]);
         }
      }

      void apply(double[][][] rx, double[][][] ry, double[][][] rz) {
         int n3 = rx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(rx[i3], ry[i3], rz[i3]);
         }
      }

      void apply(double ra, double[][][] ry, double[][][] rz) {
         int n3 = ry.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(ra, ry[i3], rz[i3]);
         }
      }

      void apply(double[][][] rx, double rb, double[][][] rz) {
         int n3 = rx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(rx[i3], rb, rz[i3]);
         }
      }
   }

   private static class ByteIterator {
      private byte[] _a1;
      private byte[][] _a2;
      private byte[][][] _a3;
      private int _n;
      private int _i;
      private int _i1;
      private int _i2;
      private int _i3;

      ByteIterator(byte[] a) {
         this._n = a.length;
         this._i = 0;
         this._i1 = 0;
         this._a1 = a;
      }

      ByteIterator(byte[][] a) {
         this._n = 0;
         int n2 = a.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this._n += a[i2].length;
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._a2 = a;
      }

      ByteIterator(byte[][][] a) {
         this._n = 0;
         int n3 = a.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            int n2 = a[i3].length;

            for(int i2 = 0; i2 < n2; ++i2) {
               this._n += a[i3][i2].length;
            }
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._i3 = 0;
         this._a3 = a;
      }

      int count() {
         return this._n;
      }

      byte get() {
         assert this._i < this._n;

         byte a = 0;
         if (this._a1 != null) {
            a = this._a1[this._i1++];
         } else if (this._a2 == null) {
            if (this._a3 != null) {
               while(this._i1 >= this._a3[this._i3][this._i2].length) {
                  this._i1 = 0;
                  ++this._i2;

                  while(this._i2 >= this._a3[this._i3].length) {
                     this._i1 = 0;
                     this._i2 = 0;
                     ++this._i3;
                  }
               }

               a = this._a3[this._i3][this._i2][this._i1++];
            }
         } else {
            while(this._i1 >= this._a2[this._i2].length) {
               this._i1 = 0;
               ++this._i2;
            }

            a = this._a2[this._i2][this._i1++];
         }

         ++this._i;
         return a;
      }

      void reset() {
         this._i = this._i1 = this._i2 = this._i3 = 0;
      }
   }

   private abstract static class CBinary {
      private CBinary() {
      }

      float[] apply(float[] cx, float[] cy) {
         int n1 = cx.length / 2;
         float[] cz = new float[2 * n1];
         this.apply(cx, cy, cz);
         return cz;
      }

      float[] apply(Cfloat ca, float[] cy) {
         int n1 = cy.length / 2;
         float[] cz = new float[2 * n1];
         this.apply(ca, cy, cz);
         return cz;
      }

      float[] apply(float[] cx, Cfloat cb) {
         int n1 = cx.length / 2;
         float[] cz = new float[2 * n1];
         this.apply(cx, cb, cz);
         return cz;
      }

      float[][] apply(float[][] cx, float[][] cy) {
         int n2 = cx.length;
         float[][] cz = new float[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cz[i2] = this.apply(cx[i2], cy[i2]);
         }

         return cz;
      }

      float[][] apply(Cfloat ca, float[][] cy) {
         int n2 = cy.length;
         float[][] cz = new float[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cz[i2] = this.apply(ca, cy[i2]);
         }

         return cz;
      }

      float[][] apply(float[][] cx, Cfloat cb) {
         int n2 = cx.length;
         float[][] cz = new float[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cz[i2] = this.apply(cx[i2], cb);
         }

         return cz;
      }

      float[][][] apply(float[][][] cx, float[][][] cy) {
         int n3 = cx.length;
         float[][][] cz = new float[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cz[i3] = this.apply(cx[i3], cy[i3]);
         }

         return cz;
      }

      float[][][] apply(Cfloat ca, float[][][] cy) {
         int n3 = cy.length;
         float[][][] cz = new float[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cz[i3] = this.apply(ca, cy[i3]);
         }

         return cz;
      }

      float[][][] apply(float[][][] cx, Cfloat cb) {
         int n3 = cx.length;
         float[][][] cz = new float[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cz[i3] = this.apply(cx[i3], cb);
         }

         return cz;
      }

      abstract void apply(float[] var1, float[] var2, float[] var3);

      abstract void apply(Cfloat var1, float[] var2, float[] var3);

      abstract void apply(float[] var1, Cfloat var2, float[] var3);

      void apply(float[][] cx, float[][] cy, float[][] cz) {
         int n2 = cx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(cx[i2], cy[i2], cz[i2]);
         }
      }

      void apply(Cfloat ca, float[][] cy, float[][] cz) {
         int n2 = cy.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(ca, cy[i2], cz[i2]);
         }
      }

      void apply(float[][] cx, Cfloat cb, float[][] cz) {
         int n2 = cx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(cx[i2], cb, cz[i2]);
         }
      }

      void apply(float[][][] cx, float[][][] cy, float[][][] cz) {
         int n3 = cx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(cx[i3], cy[i3], cz[i3]);
         }
      }

      void apply(Cfloat ca, float[][][] cy, float[][][] cz) {
         int n3 = cy.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(ca, cy[i3], cz[i3]);
         }
      }

      void apply(float[][][] cx, Cfloat cb, float[][][] cz) {
         int n3 = cx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(cx[i3], cb, cz[i3]);
         }
      }

      double[] apply(double[] cx, double[] cy) {
         int n1 = cx.length / 2;
         double[] cz = new double[2 * n1];
         this.apply(cx, cy, cz);
         return cz;
      }

      double[] apply(Cdouble ca, double[] cy) {
         int n1 = cy.length / 2;
         double[] cz = new double[2 * n1];
         this.apply(ca, cy, cz);
         return cz;
      }

      double[] apply(double[] cx, Cdouble cb) {
         int n1 = cx.length / 2;
         double[] cz = new double[2 * n1];
         this.apply(cx, cb, cz);
         return cz;
      }

      double[][] apply(double[][] cx, double[][] cy) {
         int n2 = cx.length;
         double[][] cz = new double[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cz[i2] = this.apply(cx[i2], cy[i2]);
         }

         return cz;
      }

      double[][] apply(Cdouble ca, double[][] cy) {
         int n2 = cy.length;
         double[][] cz = new double[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cz[i2] = this.apply(ca, cy[i2]);
         }

         return cz;
      }

      double[][] apply(double[][] cx, Cdouble cb) {
         int n2 = cx.length;
         double[][] cz = new double[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cz[i2] = this.apply(cx[i2], cb);
         }

         return cz;
      }

      double[][][] apply(double[][][] cx, double[][][] cy) {
         int n3 = cx.length;
         double[][][] cz = new double[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cz[i3] = this.apply(cx[i3], cy[i3]);
         }

         return cz;
      }

      double[][][] apply(Cdouble ca, double[][][] cy) {
         int n3 = cy.length;
         double[][][] cz = new double[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cz[i3] = this.apply(ca, cy[i3]);
         }

         return cz;
      }

      double[][][] apply(double[][][] cx, Cdouble cb) {
         int n3 = cx.length;
         double[][][] cz = new double[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cz[i3] = this.apply(cx[i3], cb);
         }

         return cz;
      }

      abstract void apply(double[] var1, double[] var2, double[] var3);

      abstract void apply(Cdouble var1, double[] var2, double[] var3);

      abstract void apply(double[] var1, Cdouble var2, double[] var3);

      void apply(double[][] cx, double[][] cy, double[][] cz) {
         int n2 = cx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(cx[i2], cy[i2], cz[i2]);
         }
      }

      void apply(Cdouble ca, double[][] cy, double[][] cz) {
         int n2 = cy.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(ca, cy[i2], cz[i2]);
         }
      }

      void apply(double[][] cx, Cdouble cb, double[][] cz) {
         int n2 = cx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(cx[i2], cb, cz[i2]);
         }
      }

      void apply(double[][][] cx, double[][][] cy, double[][][] cz) {
         int n3 = cx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(cx[i3], cy[i3], cz[i3]);
         }
      }

      void apply(Cdouble ca, double[][][] cy, double[][][] cz) {
         int n3 = cy.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(ca, cy[i3], cz[i3]);
         }
      }

      void apply(double[][][] cx, Cdouble cb, double[][][] cz) {
         int n3 = cx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(cx[i3], cb, cz[i3]);
         }
      }
   }

   private abstract static class ComplexToComplex {
      private ComplexToComplex() {
      }

      float[] apply(float[] cx) {
         int n1 = cx.length / 2;
         float[] cy = new float[2 * n1];
         this.apply(cx, cy);
         return cy;
      }

      float[][] apply(float[][] cx) {
         int n2 = cx.length;
         float[][] cy = new float[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cy[i2] = this.apply(cx[i2]);
         }

         return cy;
      }

      float[][][] apply(float[][][] cx) {
         int n3 = cx.length;
         float[][][] cy = new float[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cy[i3] = this.apply(cx[i3]);
         }

         return cy;
      }

      abstract void apply(float[] var1, float[] var2);

      void apply(float[][] cx, float[][] cy) {
         int n2 = cx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(cx[i2], cy[i2]);
         }
      }

      void apply(float[][][] cx, float[][][] cy) {
         int n3 = cx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(cx[i3], cy[i3]);
         }
      }

      double[] apply(double[] cx) {
         int n1 = cx.length / 2;
         double[] cy = new double[2 * n1];
         this.apply(cx, cy);
         return cy;
      }

      double[][] apply(double[][] cx) {
         int n2 = cx.length;
         double[][] cy = new double[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cy[i2] = this.apply(cx[i2]);
         }

         return cy;
      }

      double[][][] apply(double[][][] cx) {
         int n3 = cx.length;
         double[][][] cy = new double[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cy[i3] = this.apply(cx[i3]);
         }

         return cy;
      }

      abstract void apply(double[] var1, double[] var2);

      void apply(double[][] cx, double[][] cy) {
         int n2 = cx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(cx[i2], cy[i2]);
         }
      }

      void apply(double[][][] cx, double[][][] cy) {
         int n3 = cx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(cx[i3], cy[i3]);
         }
      }
   }

   private abstract static class ComplexToReal {
      private ComplexToReal() {
      }

      float[] apply(float[] cx) {
         int n1 = cx.length / 2;
         float[] cy = new float[n1];
         this.apply(cx, cy);
         return cy;
      }

      float[][] apply(float[][] cx) {
         int n2 = cx.length;
         float[][] cy = new float[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cy[i2] = this.apply(cx[i2]);
         }

         return cy;
      }

      float[][][] apply(float[][][] cx) {
         int n3 = cx.length;
         float[][][] cy = new float[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cy[i3] = this.apply(cx[i3]);
         }

         return cy;
      }

      abstract void apply(float[] var1, float[] var2);

      void apply(float[][] cx, float[][] ry) {
         int n2 = cx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(cx[i2], ry[i2]);
         }
      }

      void apply(float[][][] cx, float[][][] ry) {
         int n3 = cx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(cx[i3], ry[i3]);
         }
      }

      double[] apply(double[] cx) {
         int n1 = cx.length / 2;
         double[] cy = new double[n1];
         this.apply(cx, cy);
         return cy;
      }

      double[][] apply(double[][] cx) {
         int n2 = cx.length;
         double[][] cy = new double[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cy[i2] = this.apply(cx[i2]);
         }

         return cy;
      }

      double[][][] apply(double[][][] cx) {
         int n3 = cx.length;
         double[][][] cy = new double[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cy[i3] = this.apply(cx[i3]);
         }

         return cy;
      }

      abstract void apply(double[] var1, double[] var2);

      void apply(double[][] cx, double[][] ry) {
         int n2 = cx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(cx[i2], ry[i2]);
         }
      }

      void apply(double[][][] cx, double[][][] ry) {
         int n3 = cx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(cx[i3], ry[i3]);
         }
      }
   }

   private static class DoubleIterator {
      private double[] _a1;
      private double[][] _a2;
      private double[][][] _a3;
      private int _n;
      private int _i;
      private int _i1;
      private int _i2;
      private int _i3;

      DoubleIterator(double[] a) {
         this._n = a.length;
         this._i = 0;
         this._i1 = 0;
         this._a1 = a;
      }

      DoubleIterator(double[][] a) {
         this._n = 0;
         int n2 = a.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this._n += a[i2].length;
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._a2 = a;
      }

      DoubleIterator(double[][][] a) {
         this._n = 0;
         int n3 = a.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            int n2 = a[i3].length;

            for(int i2 = 0; i2 < n2; ++i2) {
               this._n += a[i3][i2].length;
            }
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._i3 = 0;
         this._a3 = a;
      }

      int count() {
         return this._n;
      }

      double get() {
         assert this._i < this._n;

         double a = 0.0;
         if (this._a1 != null) {
            a = this._a1[this._i1++];
         } else if (this._a2 == null) {
            if (this._a3 != null) {
               while(this._i1 >= this._a3[this._i3][this._i2].length) {
                  this._i1 = 0;
                  ++this._i2;

                  while(this._i2 >= this._a3[this._i3].length) {
                     this._i1 = 0;
                     this._i2 = 0;
                     ++this._i3;
                  }
               }

               a = this._a3[this._i3][this._i2][this._i1++];
            }
         } else {
            while(this._i1 >= this._a2[this._i2].length) {
               this._i1 = 0;
               ++this._i2;
            }

            a = this._a2[this._i2][this._i1++];
         }

         ++this._i;
         return a;
      }

      void reset() {
         this._i = this._i1 = this._i2 = this._i3 = 0;
      }
   }

   private static class FloatIterator {
      private float[] _a1;
      private float[][] _a2;
      private float[][][] _a3;
      private int _n;
      private int _i;
      private int _i1;
      private int _i2;
      private int _i3;

      FloatIterator(float[] a) {
         this._n = a.length;
         this._i = 0;
         this._i1 = 0;
         this._a1 = a;
      }

      FloatIterator(float[][] a) {
         this._n = 0;
         int n2 = a.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this._n += a[i2].length;
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._a2 = a;
      }

      FloatIterator(float[][][] a) {
         this._n = 0;
         int n3 = a.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            int n2 = a[i3].length;

            for(int i2 = 0; i2 < n2; ++i2) {
               this._n += a[i3][i2].length;
            }
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._i3 = 0;
         this._a3 = a;
      }

      int count() {
         return this._n;
      }

      float get() {
         assert this._i < this._n;

         float a = 0.0F;
         if (this._a1 != null) {
            a = this._a1[this._i1++];
         } else if (this._a2 == null) {
            if (this._a3 != null) {
               while(this._i1 >= this._a3[this._i3][this._i2].length) {
                  this._i1 = 0;
                  ++this._i2;

                  while(this._i2 >= this._a3[this._i3].length) {
                     this._i1 = 0;
                     this._i2 = 0;
                     ++this._i3;
                  }
               }

               a = this._a3[this._i3][this._i2][this._i1++];
            }
         } else {
            while(this._i1 >= this._a2[this._i2].length) {
               this._i1 = 0;
               ++this._i2;
            }

            a = this._a2[this._i2][this._i1++];
         }

         ++this._i;
         return a;
      }

      void reset() {
         this._i = this._i1 = this._i2 = this._i3 = 0;
      }
   }

   private static class IntIterator {
      private int[] _a1;
      private int[][] _a2;
      private int[][][] _a3;
      private int _n;
      private int _i;
      private int _i1;
      private int _i2;
      private int _i3;

      IntIterator(int[] a) {
         this._n = a.length;
         this._i = 0;
         this._i1 = 0;
         this._a1 = a;
      }

      IntIterator(int[][] a) {
         this._n = 0;
         int n2 = a.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this._n += a[i2].length;
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._a2 = a;
      }

      IntIterator(int[][][] a) {
         this._n = 0;
         int n3 = a.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            int n2 = a[i3].length;

            for(int i2 = 0; i2 < n2; ++i2) {
               this._n += a[i3][i2].length;
            }
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._i3 = 0;
         this._a3 = a;
      }

      int count() {
         return this._n;
      }

      int get() {
         assert this._i < this._n;

         int a = 0;
         if (this._a1 != null) {
            a = this._a1[this._i1++];
         } else if (this._a2 == null) {
            if (this._a3 != null) {
               while(this._i1 >= this._a3[this._i3][this._i2].length) {
                  this._i1 = 0;
                  ++this._i2;

                  while(this._i2 >= this._a3[this._i3].length) {
                     this._i1 = 0;
                     this._i2 = 0;
                     ++this._i3;
                  }
               }

               a = this._a3[this._i3][this._i2][this._i1++];
            }
         } else {
            while(this._i1 >= this._a2[this._i2].length) {
               this._i1 = 0;
               ++this._i2;
            }

            a = this._a2[this._i2][this._i1++];
         }

         ++this._i;
         return a;
      }

      void reset() {
         this._i = this._i1 = this._i2 = this._i3 = 0;
      }
   }

   private static class LongIterator {
      private long[] _a1;
      private long[][] _a2;
      private long[][][] _a3;
      private int _n;
      private int _i;
      private int _i1;
      private int _i2;
      private int _i3;

      LongIterator(long[] a) {
         this._n = a.length;
         this._i = 0;
         this._i1 = 0;
         this._a1 = a;
      }

      LongIterator(long[][] a) {
         this._n = 0;
         int n2 = a.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this._n += a[i2].length;
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._a2 = a;
      }

      LongIterator(long[][][] a) {
         this._n = 0;
         int n3 = a.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            int n2 = a[i3].length;

            for(int i2 = 0; i2 < n2; ++i2) {
               this._n += a[i3][i2].length;
            }
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._i3 = 0;
         this._a3 = a;
      }

      int count() {
         return this._n;
      }

      long get() {
         assert this._i < this._n;

         long a = 0L;
         if (this._a1 != null) {
            a = this._a1[this._i1++];
         } else if (this._a2 == null) {
            if (this._a3 != null) {
               while(this._i1 >= this._a3[this._i3][this._i2].length) {
                  this._i1 = 0;
                  ++this._i2;

                  while(this._i2 >= this._a3[this._i3].length) {
                     this._i1 = 0;
                     this._i2 = 0;
                     ++this._i3;
                  }
               }

               a = this._a3[this._i3][this._i2][this._i1++];
            }
         } else {
            while(this._i1 >= this._a2[this._i2].length) {
               this._i1 = 0;
               ++this._i2;
            }

            a = this._a2[this._i2][this._i1++];
         }

         ++this._i;
         return a;
      }

      void reset() {
         this._i = this._i1 = this._i2 = this._i3 = 0;
      }
   }

   private abstract static class RealToComplex {
      private RealToComplex() {
      }

      float[] apply(float[] rx, float[] ry) {
         int n1 = rx.length;
         float[] cz = new float[2 * n1];
         this.apply(rx, ry, cz);
         return cz;
      }

      float[][] apply(float[][] rx, float[][] ry) {
         int n2 = rx.length;
         float[][] cz = new float[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cz[i2] = this.apply(rx[i2], ry[i2]);
         }

         return cz;
      }

      float[][][] apply(float[][][] rx, float[][][] ry) {
         int n3 = rx.length;
         float[][][] cz = new float[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cz[i3] = this.apply(rx[i3], ry[i3]);
         }

         return cz;
      }

      abstract void apply(float[] var1, float[] var2, float[] var3);

      void apply(float[][] rx, float[][] ry, float[][] cz) {
         int n2 = rx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(rx[i2], ry[i2], cz[i2]);
         }
      }

      void apply(float[][][] rx, float[][][] ry, float[][][] cz) {
         int n3 = cz.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(rx[i3], ry[i3], cz[i3]);
         }
      }

      double[] apply(double[] rx, double[] ry) {
         int n1 = rx.length;
         double[] cz = new double[2 * n1];
         this.apply(rx, ry, cz);
         return cz;
      }

      double[][] apply(double[][] rx, double[][] ry) {
         int n2 = rx.length;
         double[][] cz = new double[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            cz[i2] = this.apply(rx[i2], ry[i2]);
         }

         return cz;
      }

      double[][][] apply(double[][][] rx, double[][][] ry) {
         int n3 = rx.length;
         double[][][] cz = new double[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            cz[i3] = this.apply(rx[i3], ry[i3]);
         }

         return cz;
      }

      abstract void apply(double[] var1, double[] var2, double[] var3);

      void apply(double[][] rx, double[][] ry, double[][] cz) {
         int n2 = rx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(rx[i2], ry[i2], cz[i2]);
         }
      }

      void apply(double[][][] rx, double[][][] ry, double[][][] cz) {
         int n3 = cz.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(rx[i3], ry[i3], cz[i3]);
         }
      }
   }

   private static class ShortIterator {
      private short[] _a1;
      private short[][] _a2;
      private short[][][] _a3;
      private int _n;
      private int _i;
      private int _i1;
      private int _i2;
      private int _i3;

      ShortIterator(short[] a) {
         this._n = a.length;
         this._i = 0;
         this._i1 = 0;
         this._a1 = a;
      }

      ShortIterator(short[][] a) {
         this._n = 0;
         int n2 = a.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this._n += a[i2].length;
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._a2 = a;
      }

      ShortIterator(short[][][] a) {
         this._n = 0;
         int n3 = a.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            int n2 = a[i3].length;

            for(int i2 = 0; i2 < n2; ++i2) {
               this._n += a[i3][i2].length;
            }
         }

         this._i = 0;
         this._i1 = 0;
         this._i2 = 0;
         this._i3 = 0;
         this._a3 = a;
      }

      int count() {
         return this._n;
      }

      short get() {
         assert this._i < this._n;

         short a = 0;
         if (this._a1 != null) {
            a = this._a1[this._i1++];
         } else if (this._a2 == null) {
            if (this._a3 != null) {
               while(this._i1 >= this._a3[this._i3][this._i2].length) {
                  this._i1 = 0;
                  ++this._i2;

                  while(this._i2 >= this._a3[this._i3].length) {
                     this._i1 = 0;
                     this._i2 = 0;
                     ++this._i3;
                  }
               }

               a = this._a3[this._i3][this._i2][this._i1++];
            }
         } else {
            while(this._i1 >= this._a2[this._i2].length) {
               this._i1 = 0;
               ++this._i2;
            }

            a = this._a2[this._i2][this._i1++];
         }

         ++this._i;
         return a;
      }

      void reset() {
         this._i = this._i1 = this._i2 = this._i3 = 0;
      }
   }

   private abstract static class Unary {
      private Unary() {
      }

      float[] apply(float[] rx) {
         int n1 = rx.length;
         float[] ry = new float[n1];
         this.apply(rx, ry);
         return ry;
      }

      float[][] apply(float[][] rx) {
         int n2 = rx.length;
         float[][] ry = new float[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            ry[i2] = this.apply(rx[i2]);
         }

         return ry;
      }

      float[][][] apply(float[][][] rx) {
         int n3 = rx.length;
         float[][][] ry = new float[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            ry[i3] = this.apply(rx[i3]);
         }

         return ry;
      }

      abstract void apply(float[] var1, float[] var2);

      void apply(float[][] rx, float[][] ry) {
         int n2 = rx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(rx[i2], ry[i2]);
         }
      }

      void apply(float[][][] rx, float[][][] ry) {
         int n3 = rx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(rx[i3], ry[i3]);
         }
      }

      double[] apply(double[] rx) {
         int n1 = rx.length;
         double[] ry = new double[n1];
         this.apply(rx, ry);
         return ry;
      }

      double[][] apply(double[][] rx) {
         int n2 = rx.length;
         double[][] ry = new double[n2][];

         for(int i2 = 0; i2 < n2; ++i2) {
            ry[i2] = this.apply(rx[i2]);
         }

         return ry;
      }

      double[][][] apply(double[][][] rx) {
         int n3 = rx.length;
         double[][][] ry = new double[n3][][];

         for(int i3 = 0; i3 < n3; ++i3) {
            ry[i3] = this.apply(rx[i3]);
         }

         return ry;
      }

      abstract void apply(double[] var1, double[] var2);

      void apply(double[][] rx, double[][] ry) {
         int n2 = rx.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            this.apply(rx[i2], ry[i2]);
         }
      }

      void apply(double[][][] rx, double[][][] ry) {
         int n3 = rx.length;

         for(int i3 = 0; i3 < n3; ++i3) {
            this.apply(rx[i3], ry[i3]);
         }
      }
   }
}
