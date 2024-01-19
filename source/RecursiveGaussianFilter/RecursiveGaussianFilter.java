package RecursiveGaussianFilter;

import java.util.concurrent.atomic.AtomicInteger;

public class RecursiveGaussianFilter {
   private RecursiveGaussianFilter.Filter _filter;

   public RecursiveGaussianFilter(double sigma, RecursiveGaussianFilter.Method method) {
      Check.argument(sigma >= 1.0, "sigma>=1.0");
      this._filter = (RecursiveGaussianFilter.Filter)(method == RecursiveGaussianFilter.Method.DERICHE
         ? new RecursiveGaussianFilter.DericheFilter(sigma)
         : new RecursiveGaussianFilter.VanVlietFilter(sigma));
   }

   public RecursiveGaussianFilter(double sigma) {
      Check.argument(sigma >= 1.0, "sigma>=1.0");
      this._filter = (RecursiveGaussianFilter.Filter)(sigma < 32.0
         ? new RecursiveGaussianFilter.DericheFilter(sigma)
         : new RecursiveGaussianFilter.VanVlietFilter(sigma));
   }

   public void apply0(float[] x, float[] y) {
      this._filter.applyN(0, x, y);
   }

   public void apply1(float[] x, float[] y) {
      this._filter.applyN(1, x, y);
   }

   public void apply2(float[] x, float[] y) {
      this._filter.applyN(2, x, y);
   }

   public void apply0X(float[][] x, float[][] y) {
      this._filter.applyNX(0, x, y);
   }

   public void apply1X(float[][] x, float[][] y) {
      this._filter.applyNX(1, x, y);
   }

   public void apply2X(float[][] x, float[][] y) {
      this._filter.applyNX(2, x, y);
   }

   public void applyX0(float[][] x, float[][] y) {
      this._filter.applyXN(0, x, y);
   }

   public void applyX1(float[][] x, float[][] y) {
      this._filter.applyXN(1, x, y);
   }

   public void applyX2(float[][] x, float[][] y) {
      this._filter.applyXN(2, x, y);
   }

   public void apply0XX(float[][][] x, float[][][] y) {
      this._filter.applyNXX(0, x, y);
   }

   public void apply1XX(float[][][] x, float[][][] y) {
      this._filter.applyNXX(1, x, y);
   }

   public void apply2XX(float[][][] x, float[][][] y) {
      this._filter.applyNXX(2, x, y);
   }

   public void applyX0X(float[][][] x, float[][][] y) {
      this._filter.applyXNX(0, x, y);
   }

   public void applyX1X(float[][][] x, float[][][] y) {
      this._filter.applyXNX(1, x, y);
   }

   public void applyX2X(float[][][] x, float[][][] y) {
      this._filter.applyXNX(2, x, y);
   }

   public void applyXX0(float[][][] x, float[][][] y) {
      this._filter.applyXXN(0, x, y);
   }

   public void applyXX1(float[][][] x, float[][][] y) {
      this._filter.applyXXN(1, x, y);
   }

   public void applyXX2(float[][][] x, float[][][] y) {
      this._filter.applyXXN(2, x, y);
   }

   public void apply00(float[][] x, float[][] y) {
      this._filter.applyXN(0, x, y);
      this._filter.applyNX(0, y, y);
   }

   public void apply10(float[][] x, float[][] y) {
      this._filter.applyXN(0, x, y);
      this._filter.applyNX(1, y, y);
   }

   public void apply01(float[][] x, float[][] y) {
      this._filter.applyXN(1, x, y);
      this._filter.applyNX(0, y, y);
   }

   public void apply11(float[][] x, float[][] y) {
      this._filter.applyXN(1, x, y);
      this._filter.applyNX(1, y, y);
   }

   public void apply20(float[][] x, float[][] y) {
      this._filter.applyXN(0, x, y);
      this._filter.applyNX(2, y, y);
   }

   public void apply02(float[][] x, float[][] y) {
      this._filter.applyXN(2, x, y);
      this._filter.applyNX(0, y, y);
   }

   public void apply000(float[][][] x, float[][][] y) {
      this._filter.applyXXN(0, x, y);
      this._filter.applyXNX(0, y, y);
      this._filter.applyNXX(0, y, y);
   }

   public void apply100(float[][][] x, float[][][] y) {
      this._filter.applyXXN(0, x, y);
      this._filter.applyXNX(0, y, y);
      this._filter.applyNXX(1, y, y);
   }

   public void apply010(float[][][] x, float[][][] y) {
      this._filter.applyXXN(0, x, y);
      this._filter.applyXNX(1, y, y);
      this._filter.applyNXX(0, y, y);
   }

   public void apply001(float[][][] x, float[][][] y) {
      this._filter.applyXXN(1, x, y);
      this._filter.applyXNX(0, y, y);
      this._filter.applyNXX(0, y, y);
   }

   public void apply110(float[][][] x, float[][][] y) {
      this._filter.applyXXN(0, x, y);
      this._filter.applyXNX(1, y, y);
      this._filter.applyNXX(1, y, y);
   }

   public void apply101(float[][][] x, float[][][] y) {
      this._filter.applyXXN(1, x, y);
      this._filter.applyXNX(0, y, y);
      this._filter.applyNXX(1, y, y);
   }

   public void apply011(float[][][] x, float[][][] y) {
      this._filter.applyXXN(1, x, y);
      this._filter.applyXNX(1, y, y);
      this._filter.applyNXX(0, y, y);
   }

   public void apply200(float[][][] x, float[][][] y) {
      this._filter.applyXXN(0, x, y);
      this._filter.applyXNX(0, y, y);
      this._filter.applyNXX(2, y, y);
   }

   public void apply020(float[][][] x, float[][][] y) {
      this._filter.applyXXN(0, x, y);
      this._filter.applyXNX(2, y, y);
      this._filter.applyNXX(0, y, y);
   }

   public void apply002(float[][][] x, float[][][] y) {
      this._filter.applyXXN(2, x, y);
      this._filter.applyXNX(0, y, y);
      this._filter.applyNXX(0, y, y);
   }

   private static void checkArrays(float[] x, float[] y) {
      Check.argument(x.length == y.length, "x.length==y.length");
   }

   private static void checkArrays(float[][] x, float[][] y) {
      Check.argument(x.length == y.length, "x.length==y.length");
      Check.argument(x[0].length == y[0].length, "x[0].length==y[0].length");
      Check.argument(ArrayMath.isRegular(x), "x is regular");
      Check.argument(ArrayMath.isRegular(y), "y is regular");
   }

   private static void checkArrays(float[][][] x, float[][][] y) {
      Check.argument(x.length == y.length, "x.length==y.length");
      Check.argument(x[0].length == y[0].length, "x[0].length==y[0].length");
      Check.argument(x[0][0].length == y[0][0].length, "x[0][0].length==y[0][0].length");
      Check.argument(ArrayMath.isRegular(x), "x is regular");
      Check.argument(ArrayMath.isRegular(y), "y is regular");
   }

   private static boolean sameArrays(float[] x, float[] y) {
      return x == y;
   }

   private static boolean sameArrays(float[][] x, float[][] y) {
      if (x == y) {
         return true;
      } else {
         int n2 = x.length;

         for(int i2 = 0; i2 < n2; ++i2) {
            if (x[i2] == y[i2]) {
               return true;
            }
         }

         return false;
      }
   }

   private static Thread[] newThreads() {
      int nthread = Runtime.getRuntime().availableProcessors();
      return new Thread[nthread];
   }

   private static void startAndJoin(Thread[] threads) {
      for(Thread thread : threads) {
         thread.start();
      }

      try {
         for(Thread thread : threads) {
            thread.join();
         }
      } catch (InterruptedException var5) {
         throw new RuntimeException(var5);
      }
   }

   private static class DericheFilter extends RecursiveGaussianFilter.Filter {
      private static double a00 = 1.6797292232361107;
      private static double a10 = 3.734829826910358;
      private static double b00 = 1.7831906544515104;
      private static double b10 = 1.7228297663338028;
      private static double c00 = -0.6802783501806897;
      private static double c10 = -0.2598300478959625;
      private static double w00 = 0.6318113174569493;
      private static double w10 = 1.996927683248777;
      private static double a01 = 0.649402400844062;
      private static double a11 = 0.9557370760729773;
      private static double b01 = 1.5159726670750566;
      private static double b11 = 1.526760873479114;
      private static double c01 = -0.6472105276644291;
      private static double c11 = -4.530692304457076;
      private static double w01 = 2.071895365878265;
      private static double w11 = 0.6719055957689513;
      private static double a02 = 0.3224570510072559;
      private static double a12 = -1.7382843963561239;
      private static double b02 = 1.313805492651688;
      private static double b12 = 1.2402181393295362;
      private static double c02 = -1.3312275593739595;
      private static double c12 = 3.6607035671974897;
      private static double w02 = 2.1656041357418863;
      private static double w12 = 0.7479888745408682;
      private static double[] a0 = new double[]{a00, a01, a02};
      private static double[] a1 = new double[]{a10, a11, a12};
      private static double[] b0 = new double[]{b00, b01, b02};
      private static double[] b1 = new double[]{b10, b11, b12};
      private static double[] c0 = new double[]{c00, c01, c02};
      private static double[] c1 = new double[]{c10, c11, c12};
      private static double[] w0 = new double[]{w00, w01, w02};
      private static double[] w1 = new double[]{w10, w11, w12};
      private float[] _n0;
      private float[] _n1;
      private float[] _n2;
      private float[] _n3;
      private float[] _d1;
      private float[] _d2;
      private float[] _d3;
      private float[] _d4;

      DericheFilter(double sigma) {
         this.makeND(sigma);
      }

      @Override
      void applyN(int nd, float[] x, float[] y) {
         RecursiveGaussianFilter.checkArrays(x, y);
         if (RecursiveGaussianFilter.sameArrays(x, y)) {
            x = ArrayMath.copy(x);
         }

         int m = y.length;
         float n0 = this._n0[nd];
         float n1 = this._n1[nd];
         float n2 = this._n2[nd];
         float n3 = this._n3[nd];
         float d1 = this._d1[nd];
         float d2 = this._d2[nd];
         float d3 = this._d3[nd];
         float d4 = this._d4[nd];
         float yim4 = 0.0F;
         float yim3 = 0.0F;
         float yim2 = 0.0F;
         float yim1 = 0.0F;
         float xim3 = 0.0F;
         float xim2 = 0.0F;
         float xim1 = 0.0F;

         for(int i = 0; i < m; ++i) {
            float xi = x[i];
            float yi = n0 * xi + n1 * xim1 + n2 * xim2 + n3 * xim3 - d1 * yim1 - d2 * yim2 - d3 * yim3 - d4 * yim4;
            y[i] = yi;
            yim4 = yim3;
            yim3 = yim2;
            yim2 = yim1;
            yim1 = yi;
            xim3 = xim2;
            xim2 = xim1;
            xim1 = xi;
         }

         n1 -= d1 * n0;
         n2 -= d2 * n0;
         n3 -= d3 * n0;
         float n4 = -d4 * n0;
         if (nd % 2 != 0) {
            n1 = -n1;
            n2 = -n2;
            n3 = -n3;
            n4 = -n4;
         }

         float yip4 = 0.0F;
         float yip3 = 0.0F;
         float yip2 = 0.0F;
         float yip1 = 0.0F;
         float xip4 = 0.0F;
         float xip3 = 0.0F;
         float xip2 = 0.0F;
         float xip1 = 0.0F;

         for(int i = m - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = n1 * xip1 + n2 * xip2 + n3 * xip3 + n4 * xip4 - d1 * yip1 - d2 * yip2 - d3 * yip3 - d4 * yip4;
            y[i] += yi;
            yip4 = yip3;
            yip3 = yip2;
            yip2 = yip1;
            yip1 = yi;
            xip4 = xip3;
            xip3 = xip2;
            xip2 = xip1;
            xip1 = xi;
         }
      }

      @Override
      void applyXN(int nd, float[][] x, float[][] y) {
         RecursiveGaussianFilter.checkArrays(x, y);
         if (RecursiveGaussianFilter.sameArrays(x, y)) {
            x = ArrayMath.copy(x);
         }

         int m2 = y.length;
         int m1 = y[0].length;
         float n0 = this._n0[nd];
         float n1 = this._n1[nd];
         float n2 = this._n2[nd];
         float n3 = this._n3[nd];
         float d1 = this._d1[nd];
         float d2 = this._d2[nd];
         float d3 = this._d3[nd];
         float d4 = this._d4[nd];
         float[] yim4 = new float[m1];
         float[] yim3 = new float[m1];
         float[] yim2 = new float[m1];
         float[] yim1 = new float[m1];
         float[] xim4 = new float[m1];
         float[] xim3 = new float[m1];
         float[] xim2 = new float[m1];
         float[] xim1 = new float[m1];
         float[] yi = new float[m1];
         float[] xi = new float[m1];

         for(int i2 = 0; i2 < m2; ++i2) {
            float[] x2 = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < m1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = n0 * xi[i1] + n1 * xim1[i1] + n2 * xim2[i1] + n3 * xim3[i1] - d1 * yim1[i1] - d2 * yim2[i1] - d3 * yim3[i1] - d4 * yim4[i1];
               y2[i1] = yi[i1];
            }

            float[] yt = yim4;
            yim4 = yim3;
            yim3 = yim2;
            yim2 = yim1;
            yim1 = yi;
            yi = yt;
            float[] xt = xim3;
            xim3 = xim2;
            xim2 = xim1;
            xim1 = xi;
            xi = xt;
         }

         n1 -= d1 * n0;
         n2 -= d2 * n0;
         n3 -= d3 * n0;
         float n4 = -d4 * n0;
         if (nd % 2 != 0) {
            n1 = -n1;
            n2 = -n2;
            n3 = -n3;
            n4 = -n4;
         }

         float[] yip4 = yim4;
         float[] yip3 = yim3;
         float[] yip2 = yim2;
         float[] yip1 = yim1;
         float[] xip4 = xim4;
         float[] xip3 = xim3;
         float[] xip2 = xim2;
         float[] xip1 = xim1;

         for(int i1 = 0; i1 < m1; ++i1) {
            yip4[i1] = 0.0F;
            yip3[i1] = 0.0F;
            yip2[i1] = 0.0F;
            yip1[i1] = 0.0F;
            xip4[i1] = 0.0F;
            xip3[i1] = 0.0F;
            xip2[i1] = 0.0F;
            xip1[i1] = 0.0F;
         }

         for(int i2 = m2 - 1; i2 >= 0; --i2) {
            float[] x2 = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < m1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = n1 * xip1[i1] + n2 * xip2[i1] + n3 * xip3[i1] + n4 * xip4[i1] - d1 * yip1[i1] - d2 * yip2[i1] - d3 * yip3[i1] - d4 * yip4[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yip4;
            yip4 = yip3;
            yip3 = yip2;
            yip2 = yip1;
            yip1 = yi;
            yi = yt;
            float[] xt = xip4;
            xip4 = xip3;
            xip3 = xip2;
            xip2 = xip1;
            xip1 = xi;
            xi = xt;
         }
      }

      private void makeND(double sigma) {
         this._n0 = new float[3];
         this._n1 = new float[3];
         this._n2 = new float[3];
         this._n3 = new float[3];
         this._d1 = new float[3];
         this._d2 = new float[3];
         this._d3 = new float[3];
         this._d4 = new float[3];

         for(int i = 0; i < 3; ++i) {
            double n0 = i % 2 == 0 ? a0[i] + c0[i] : 0.0;
            double n1 = MathPlus.exp(-b1[i] / sigma) * (c1[i] * MathPlus.sin(w1[i] / sigma) - (c0[i] + 2.0 * a0[i]) * MathPlus.cos(w1[i] / sigma))
               + MathPlus.exp(-b0[i] / sigma) * (a1[i] * MathPlus.sin(w0[i] / sigma) - (2.0 * c0[i] + a0[i]) * MathPlus.cos(w0[i] / sigma));
            double n2 = 2.0
                  * MathPlus.exp(-(b0[i] + b1[i]) / sigma)
                  * (
                     (a0[i] + c0[i]) * MathPlus.cos(w1[i] / sigma) * MathPlus.cos(w0[i] / sigma)
                        - a1[i] * MathPlus.cos(w1[i] / sigma) * MathPlus.sin(w0[i] / sigma)
                        - c1[i] * MathPlus.cos(w0[i] / sigma) * MathPlus.sin(w1[i] / sigma)
                  )
               + c0[i] * MathPlus.exp(-2.0 * b0[i] / sigma)
               + a0[i] * MathPlus.exp(-2.0 * b1[i] / sigma);
            double n3 = MathPlus.exp(-(b1[i] + 2.0 * b0[i]) / sigma) * (c1[i] * MathPlus.sin(w1[i] / sigma) - c0[i] * MathPlus.cos(w1[i] / sigma))
               + MathPlus.exp(-(b0[i] + 2.0 * b1[i]) / sigma) * (a1[i] * MathPlus.sin(w0[i] / sigma) - a0[i] * MathPlus.cos(w0[i] / sigma));
            double d1 = -2.0 * MathPlus.exp(-b0[i] / sigma) * MathPlus.cos(w0[i] / sigma) - 2.0 * MathPlus.exp(-b1[i] / sigma) * MathPlus.cos(w1[i] / sigma);
            double d2 = 4.0 * MathPlus.exp(-(b0[i] + b1[i]) / sigma) * MathPlus.cos(w0[i] / sigma) * MathPlus.cos(w1[i] / sigma)
               + MathPlus.exp(-2.0 * b0[i] / sigma)
               + MathPlus.exp(-2.0 * b1[i] / sigma);
            double d3 = -2.0 * MathPlus.exp(-(b0[i] + 2.0 * b1[i]) / sigma) * MathPlus.cos(w0[i] / sigma)
               - 2.0 * MathPlus.exp(-(b1[i] + 2.0 * b0[i]) / sigma) * MathPlus.cos(w1[i] / sigma);
            double d4 = MathPlus.exp(-2.0 * (b0[i] + b1[i]) / sigma);
            this._n0[i] = (float)n0;
            this._n1[i] = (float)n1;
            this._n2[i] = (float)n2;
            this._n3[i] = (float)n3;
            this._d1[i] = (float)d1;
            this._d2[i] = (float)d2;
            this._d3[i] = (float)d3;
            this._d4[i] = (float)d4;
         }

         this.scaleN(sigma);
      }

      private void scaleN(double sigma) {
         int n = 1 + 2 * (int)(10.0 * sigma);
         float[] x = new float[n];
         float[] y0 = new float[n];
         float[] y1 = new float[n];
         float[] y2 = new float[n];
         int m = (n - 1) / 2;
         x[m] = 1.0F;
         this.applyN(0, x, y0);
         this.applyN(1, x, y1);
         this.applyN(2, x, y2);
         double[] s = new double[3];
         int i = 0;

         for(int j = n - 1; i < j; --j) {
            double t = (double)(i - m);
            s[0] += (double)(y0[j] + y0[i]);
            s[1] += MathPlus.sin(t / sigma) * (double)(y1[j] - y1[i]);
            s[2] += MathPlus.cos(t * MathPlus.sqrt(2.0) / sigma) * (double)(y2[j] + y2[i]);
            ++i;
         }

         s[0] += (double)y0[m];
         s[2] += (double)y2[m];
         s[1] *= sigma * MathPlus.exp(0.5);
         s[2] *= -(sigma * sigma) / 2.0 * MathPlus.exp(1.0);

         for(int ix = 0; ix < 3; ++ix) {
            this._n0[ix] = (float)((double)this._n0[ix] / s[ix]);
            this._n1[ix] = (float)((double)this._n1[ix] / s[ix]);
            this._n2[ix] = (float)((double)this._n2[ix] / s[ix]);
            this._n3[ix] = (float)((double)this._n3[ix] / s[ix]);
         }
      }
   }

   private abstract static class Filter {
      private Filter() {
      }

      abstract void applyN(int var1, float[] var2, float[] var3);

      abstract void applyXN(int var1, float[][] var2, float[][] var3);

      void applyNX(int nd, float[][] x, float[][] y) {
         int m2 = y.length;

         for(int i2 = 0; i2 < m2; ++i2) {
            this.applyN(nd, x[i2], y[i2]);
         }
      }

      void applyNXX(final int nd, final float[][][] x, final float[][][] y) {
         int m3 = y.length;
         Parallel.loop(m3, new Parallel.LoopInt() {
            @Override
            public void compute(int i3) {
               Filter.this.applyNX(nd, x[i3], y[i3]);
            }
         });
      }

      void xapplyNXX(final int nd, final float[][][] x, final float[][][] y) {
         final int m3 = y.length;
         final AtomicInteger ai = new AtomicInteger();
         Thread[] threads = RecursiveGaussianFilter.newThreads();

         for(int ithread = 0; ithread < threads.length; ++ithread) {
            threads[ithread] = new Thread(new Runnable() {
               @Override
               public void run() {
                  for(int i3 = ai.getAndIncrement(); i3 < m3; i3 = ai.getAndIncrement()) {
                     Filter.this.applyNX(nd, x[i3], y[i3]);
                  }
               }
            });
         }

         RecursiveGaussianFilter.startAndJoin(threads);
      }

      void applyXNX(final int nd, final float[][][] x, final float[][][] y) {
         int m3 = y.length;
         Parallel.loop(m3, new Parallel.LoopInt() {
            @Override
            public void compute(int i3) {
               Filter.this.applyXN(nd, x[i3], y[i3]);
            }
         });
      }

      void xapplyXNX(final int nd, final float[][][] x, final float[][][] y) {
         final int m3 = y.length;
         final AtomicInteger ai = new AtomicInteger();
         Thread[] threads = RecursiveGaussianFilter.newThreads();

         for(int ithread = 0; ithread < threads.length; ++ithread) {
            threads[ithread] = new Thread(new Runnable() {
               @Override
               public void run() {
                  for(int i3 = ai.getAndIncrement(); i3 < m3; i3 = ai.getAndIncrement()) {
                     Filter.this.applyXN(nd, x[i3], y[i3]);
                  }
               }
            });
         }

         RecursiveGaussianFilter.startAndJoin(threads);
      }

      void applyXXN(final int nd, float[][][] x, float[][][] y) {
         RecursiveGaussianFilter.checkArrays(x, y);
         int m3 = y.length;
         int m2 = y[0].length;
         final float[][][] tx = new float[m2][m3][];
         final float[][][] ty = new float[m2][m3][];

         for(int i3 = 0; i3 < m3; ++i3) {
            for(int i2 = 0; i2 < m2; ++i2) {
               tx[i2][i3] = x[i3][i2];
               ty[i2][i3] = y[i3][i2];
            }
         }

         Parallel.loop(m2, new Parallel.LoopInt() {
            @Override
            public void compute(int i2) {
               Filter.this.applyXN(nd, tx[i2], ty[i2]);
            }
         });
      }

      void xapplyXXN(final int nd, final float[][][] x, final float[][][] y) {
         RecursiveGaussianFilter.checkArrays(x, y);
         final int m3 = y.length;
         final int m2 = y[0].length;
         final AtomicInteger ai = new AtomicInteger();
         Thread[] threads = RecursiveGaussianFilter.newThreads();

         for(int ithread = 0; ithread < threads.length; ++ithread) {
            threads[ithread] = new Thread(new Runnable() {
               @Override
               public void run() {
                  float[][] x2 = new float[m3][];
                  float[][] y2 = new float[m3][];

                  for(int i2 = ai.getAndIncrement(); i2 < m2; i2 = ai.getAndIncrement()) {
                     for(int i3 = 0; i3 < m3; ++i3) {
                        x2[i3] = x[i3][i2];
                        y2[i3] = y[i3][i2];
                     }

                     Filter.this.applyXN(nd, x2, y2);
                  }
               }
            });
         }

         RecursiveGaussianFilter.startAndJoin(threads);
      }

      void xxapplyXXN(int nd, float[][][] x, float[][][] y) {
         RecursiveGaussianFilter.checkArrays(x, y);
         int m3 = y.length;
         int m2 = y[0].length;
         float[][] x2 = new float[m3][];
         float[][] y2 = new float[m3][];

         for(int i2 = 0; i2 < m2; ++i2) {
            for(int i3 = 0; i3 < m3; ++i3) {
               x2[i3] = x[i3][i2];
               y2[i3] = y[i3][i2];
            }

            this.applyXN(nd, x2, y2);
         }
      }
   }

   public static enum Method {
      DERICHE,
      VAN_VLIET;
   }

   private static class VanVlietFilter extends RecursiveGaussianFilter.Filter {
      private static Cdouble[][] POLES = new Cdouble[][]{
         {new Cdouble(1.12075, 1.27788), new Cdouble(1.12075, -1.27788), new Cdouble(1.76952, 0.46611), new Cdouble(1.76952, -0.46611)},
         {new Cdouble(1.04185, 1.24034), new Cdouble(1.04185, -1.24034), new Cdouble(1.69747, 0.4479), new Cdouble(1.69747, -0.4479)},
         {new Cdouble(0.9457, 1.21064), new Cdouble(0.9457, -1.21064), new Cdouble(1.60161, 0.42647), new Cdouble(1.60161, -0.42647)}
      };
      private Recursive2ndOrderFilter[][][] _g;

      VanVlietFilter(double sigma) {
         this.makeG(sigma);
      }

      @Override
      void applyN(int nd, float[] x, float[] y) {
         RecursiveGaussianFilter.checkArrays(x, y);
         if (RecursiveGaussianFilter.sameArrays(x, y)) {
            x = ArrayMath.copy(x);
         }

         this._g[nd][0][0].applyForward(x, y);
         this._g[nd][0][1].accumulateReverse(x, y);
         this._g[nd][1][0].accumulateForward(x, y);
         this._g[nd][1][1].accumulateReverse(x, y);
      }

      @Override
      void applyXN(int nd, float[][] x, float[][] y) {
         RecursiveGaussianFilter.checkArrays(x, y);
         if (RecursiveGaussianFilter.sameArrays(x, y)) {
            x = ArrayMath.copy(x);
         }

         this._g[nd][0][0].apply2Forward(x, y);
         this._g[nd][0][1].accumulate2Reverse(x, y);
         this._g[nd][1][0].accumulate2Forward(x, y);
         this._g[nd][1][1].accumulate2Reverse(x, y);
      }

      private void makeG(double sigma) {
         this._g = new Recursive2ndOrderFilter[3][2][2];

         for(int nd = 0; nd < 3; ++nd) {
            Cdouble[] poles = adjustPoles(sigma, POLES[nd]);
            double gain = computeGain(poles);
            double gg = gain * gain;
            Cdouble d0 = new Cdouble(poles[0]);
            Cdouble d1 = new Cdouble(poles[2]);
            Cdouble e0 = d0.inv();
            Cdouble e1 = d1.inv();
            Cdouble g0 = this.gr(nd, d0, poles, gg);
            Cdouble g1 = this.gr(nd, d1, poles, gg);
            double a10 = -2.0 * d0.r;
            double a11 = -2.0 * d1.r;
            double a20 = d0.norm();
            double a21 = d1.norm();
            if (nd == 0 || nd == 2) {
               double b10 = g0.i / e0.i;
               double b11 = g1.i / e1.i;
               double b00 = g0.r - b10 * e0.r;
               double b01 = g1.r - b11 * e1.r;
               double b20 = 0.0;
               double b21 = 0.0;
               this._g[nd][0][0] = this.makeFilter(b00, b10, b20, a10, a20);
               this._g[nd][1][0] = this.makeFilter(b01, b11, b21, a11, a21);
               b20 -= b00 * a20;
               b21 -= b01 * a21;
               b10 -= b00 * a10;
               b11 -= b01 * a11;
               b00 = 0.0;
               b01 = 0.0;
               this._g[nd][0][1] = this.makeFilter(b00, b10, b20, a10, a20);
               this._g[nd][1][1] = this.makeFilter(b01, b11, b21, a11, a21);
            } else if (nd == 1) {
               double b20 = g0.i / e0.i;
               double b21 = g1.i / e1.i;
               double b10 = g0.r - b20 * e0.r;
               double b11 = g1.r - b21 * e1.r;
               double b00 = 0.0;
               double b01 = 0.0;
               this._g[nd][0][0] = this.makeFilter(b00, b10, b20, a10, a20);
               this._g[nd][1][0] = this.makeFilter(b01, b11, b21, a11, a21);
               b20 = -b20;
               b21 = -b21;
               b10 = -b10;
               b11 = -b11;
               b00 = 0.0;
               b01 = 0.0;
               this._g[nd][0][1] = this.makeFilter(b00, b10, b20, a10, a20);
               this._g[nd][1][1] = this.makeFilter(b01, b11, b21, a11, a21);
            }
         }
      }

      Recursive2ndOrderFilter makeFilter(double b0, double b1, double b2, double a1, double a2) {
         return new Recursive2ndOrderFilter((float)b0, (float)b1, (float)b2, (float)a1, (float)a2);
      }

      private Cdouble gr(int nd, Cdouble polej, Cdouble[] poles, double gain) {
         Cdouble pj = polej;
         Cdouble qj = polej.inv();
         Cdouble c1 = new Cdouble(1.0, 0.0);
         Cdouble gz = new Cdouble(c1);
         if (nd == 1) {
            gz.timesEquals(c1.minus(qj));
            gz.timesEquals(c1.plus(polej));
            gz.timesEquals(polej);
            gz.timesEquals(0.5);
         } else if (nd == 2) {
            gz.timesEquals(c1.minus(qj));
            gz.timesEquals(c1.minus(polej));
            gz.timesEquals(-1.0);
         }

         Cdouble gp = new Cdouble(c1);

         for(Cdouble pi : poles) {
            if (!pi.equals(pj) && !pi.equals(pj.conj())) {
               gp.timesEquals(c1.minus(pi.times(qj)));
            }

            gp.timesEquals(c1.minus(pi.times(pj)));
         }

         return gz.over(gp).times(gain);
      }

      private static Cdouble[] adjustPoles(double sigma, Cdouble[] poles) {
         double q = sigma;
         double s = computeSigma(sigma, poles);

         for(int iter = 0; MathPlus.abs(sigma - s) > sigma * 1.0E-8; ++iter) {
            Check.state(iter < 100, "number of iterations less than 100");
            s = computeSigma(q, poles);
            q *= sigma / s;
         }

         int npole = poles.length;
         Cdouble[] apoles = new Cdouble[npole];

         for(int ipole = 0; ipole < npole; ++ipole) {
            Cdouble pi = poles[ipole];
            double a = MathPlus.pow(pi.abs(), 2.0 / q);
            double t = MathPlus.atan2(pi.i, pi.r) * 2.0 / q;
            apoles[ipole] = Cdouble.polar(a, t).inv();
         }

         return apoles;
      }

      private static double computeGain(Cdouble[] poles) {
         int npole = poles.length;
         Cdouble c1 = new Cdouble(1.0, 0.0);
         Cdouble cg = new Cdouble(c1);

         for(int ipole = 0; ipole < npole; ++ipole) {
            cg.timesEquals(c1.minus(poles[ipole]));
         }

         return cg.r;
      }

      private static double computeSigma(double sigma, Cdouble[] poles) {
         int npole = poles.length;
         double q = sigma / 2.0;
         Cdouble c1 = new Cdouble(1.0);
         Cdouble cs = new Cdouble();

         for(int ipole = 0; ipole < npole; ++ipole) {
            Cdouble pi = poles[ipole];
            double a = MathPlus.pow(pi.abs(), -1.0 / q);
            double t = MathPlus.atan2(pi.i, pi.r) / q;
            Cdouble b = Cdouble.polar(a, t);
            Cdouble c = c1.minus(b);
            Cdouble d = c.times(c);
            cs.plusEquals(b.times(2.0).over(d));
         }

         return MathPlus.sqrt(cs.r);
      }
   }
}
