package RecursiveGaussianFilter;

public class Recursive2ndOrderFilter {
   private float _b0;
   private float _b1;
   private float _b2;
   private float _a1;
   private float _a2;

   public Recursive2ndOrderFilter(float b0, float b1, float b2, float a1, float a2) {
      this._b0 = b0;
      this._b1 = b1;
      this._b2 = b2;
      this._a1 = a1;
      this._a2 = a2;
   }

   public Recursive2ndOrderFilter(double pole, double zero, double gain) {
      this._b0 = (float)gain;
      this._b1 = (float)(-gain * zero);
      this._a1 = (float)(-pole);
   }

   public Recursive2ndOrderFilter(Cdouble pole1, Cdouble pole2, Cdouble zero1, Cdouble zero2, double gain) {
      Check.argument(pole1.i == 0.0 && pole2.i == 0.0 || pole2.r == pole1.r && -pole2.i == pole1.i, "poles are real or conjugate pair");
      Check.argument(zero1.i == 0.0 && zero2.i == 0.0 || zero2.r == zero1.r && -zero2.i == zero1.i, "zeros are real or conjugate pair");
      this._b0 = (float)gain;
      this._b1 = (float)(-(zero1.r + zero2.r) * gain);
      this._b2 = (float)(zero1.times(zero2).r * gain);
      this._a1 = (float)(-(pole1.r + pole2.r));
      this._a2 = (float)pole1.times(pole2).r;
   }

   public void applyForward(float[] x, float[] y) {
      checkArrays(x, y);
      int n = y.length;
      if (this._b1 == 0.0F && this._b2 == 0.0F && this._a2 == 0.0F) {
         float yim1 = 0.0F;

         for(int i = 0; i < n; ++i) {
            float xi = x[i];
            float yi = this._b0 * xi - this._a1 * yim1;
            y[i] = yi;
            yim1 = yi;
         }
      } else if (this._b2 == 0.0F && this._a2 == 0.0F) {
         float yim1 = 0.0F;
         float xim1 = 0.0F;

         for(int i = 0; i < n; ++i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xim1 - this._a1 * yim1;
            y[i] = yi;
            yim1 = yi;
            xim1 = xi;
         }
      } else if (this._b2 == 0.0F) {
         float yim2 = 0.0F;
         float yim1 = 0.0F;
         float xim1 = 0.0F;

         for(int i = 0; i < n; ++i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xim1 - this._a1 * yim1 - this._a2 * yim2;
            y[i] = yi;
            yim2 = yim1;
            yim1 = yi;
            xim1 = xi;
         }
      } else if (this._b0 == 0.0F) {
         float yim2 = 0.0F;
         float yim1 = 0.0F;
         float xim2 = 0.0F;
         float xim1 = 0.0F;

         for(int i = 0; i < n; ++i) {
            float xi = x[i];
            float yi = this._b1 * xim1 + this._b2 * xim2 - this._a1 * yim1 - this._a2 * yim2;
            y[i] = yi;
            yim2 = yim1;
            yim1 = yi;
            xim2 = xim1;
            xim1 = xi;
         }
      } else {
         float yim2 = 0.0F;
         float yim1 = 0.0F;
         float xim2 = 0.0F;
         float xim1 = 0.0F;

         for(int i = 0; i < n; ++i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xim1 + this._b2 * xim2 - this._a1 * yim1 - this._a2 * yim2;
            y[i] = yi;
            yim2 = yim1;
            yim1 = yi;
            xim2 = xim1;
            xim1 = xi;
         }
      }
   }

   public void applyReverse(float[] x, float[] y) {
      checkArrays(x, y);
      int n = y.length;
      if (this._b1 == 0.0F && this._b2 == 0.0F && this._a2 == 0.0F) {
         float yip1 = 0.0F;

         for(int i = n - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = this._b0 * xi - this._a1 * yip1;
            y[i] = yi;
            yip1 = yi;
         }
      } else if (this._b2 == 0.0F && this._a2 == 0.0F) {
         float xip1 = 0.0F;
         float yip1 = 0.0F;

         for(int i = n - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xip1 - this._a1 * yip1;
            y[i] = yi;
            yip1 = yi;
            xip1 = xi;
         }
      } else if (this._b2 == 0.0F) {
         float xip1 = 0.0F;
         float yip1 = 0.0F;
         float yip2 = 0.0F;

         for(int i = n - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xip1 - this._a1 * yip1 - this._a2 * yip2;
            y[i] = yi;
            yip2 = yip1;
            yip1 = yi;
            xip1 = xi;
         }
      } else if (this._b0 == 0.0F) {
         float xip1 = 0.0F;
         float xip2 = 0.0F;
         float yip1 = 0.0F;
         float yip2 = 0.0F;

         for(int i = n - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = this._b1 * xip1 + this._b2 * xip2 - this._a1 * yip1 - this._a2 * yip2;
            y[i] = yi;
            yip2 = yip1;
            yip1 = yi;
            xip2 = xip1;
            xip1 = xi;
         }
      } else {
         float xip1 = 0.0F;
         float xip2 = 0.0F;
         float yip1 = 0.0F;
         float yip2 = 0.0F;

         for(int i = n - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xip1 + this._b2 * xip2 - this._a1 * yip1 - this._a2 * yip2;
            y[i] = yi;
            yip2 = yip1;
            yip1 = yi;
            xip2 = xip1;
            xip1 = xi;
         }
      }
   }

   public void accumulateForward(float[] x, float[] y) {
      checkArrays(x, y);
      int n = y.length;
      if (this._b1 == 0.0F && this._b2 == 0.0F && this._a2 == 0.0F) {
         float yim1 = 0.0F;

         for(int i = 0; i < n; ++i) {
            float xi = x[i];
            float yi = this._b0 * xi - this._a1 * yim1;
            y[i] += yi;
            yim1 = yi;
         }
      } else if (this._b2 == 0.0F && this._a2 == 0.0F) {
         float yim1 = 0.0F;
         float xim1 = 0.0F;

         for(int i = 0; i < n; ++i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xim1 - this._a1 * yim1;
            y[i] += yi;
            yim1 = yi;
            xim1 = xi;
         }
      } else if (this._b2 == 0.0F) {
         float yim2 = 0.0F;
         float yim1 = 0.0F;
         float xim1 = 0.0F;

         for(int i = 0; i < n; ++i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xim1 - this._a1 * yim1 - this._a2 * yim2;
            y[i] += yi;
            yim2 = yim1;
            yim1 = yi;
            xim1 = xi;
         }
      } else if (this._b0 == 0.0F) {
         float yim2 = 0.0F;
         float yim1 = 0.0F;
         float xim2 = 0.0F;
         float xim1 = 0.0F;

         for(int i = 0; i < n; ++i) {
            float xi = x[i];
            float yi = this._b1 * xim1 + this._b2 * xim2 - this._a1 * yim1 - this._a2 * yim2;
            y[i] += yi;
            yim2 = yim1;
            yim1 = yi;
            xim2 = xim1;
            xim1 = xi;
         }
      } else {
         float yim2 = 0.0F;
         float yim1 = 0.0F;
         float xim2 = 0.0F;
         float xim1 = 0.0F;

         for(int i = 0; i < n; ++i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xim1 + this._b2 * xim2 - this._a1 * yim1 - this._a2 * yim2;
            y[i] += yi;
            yim2 = yim1;
            yim1 = yi;
            xim2 = xim1;
            xim1 = xi;
         }
      }
   }

   public void accumulateReverse(float[] x, float[] y) {
      checkArrays(x, y);
      int n = y.length;
      if (this._b1 == 0.0F && this._b2 == 0.0F && this._a2 == 0.0F) {
         float yip1 = 0.0F;

         for(int i = n - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = this._b0 * xi - this._a1 * yip1;
            y[i] += yi;
            yip1 = yi;
         }
      } else if (this._b2 == 0.0F && this._a2 == 0.0F) {
         float xip1 = 0.0F;
         float yip1 = 0.0F;

         for(int i = n - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xip1 - this._a1 * yip1;
            y[i] += yi;
            yip1 = yi;
            xip1 = xi;
         }
      } else if (this._b2 == 0.0F) {
         float xip1 = 0.0F;
         float yip1 = 0.0F;
         float yip2 = 0.0F;

         for(int i = n - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xip1 - this._a1 * yip1 - this._a2 * yip2;
            y[i] += yi;
            yip2 = yip1;
            yip1 = yi;
            xip1 = xi;
         }
      } else if (this._b0 == 0.0F) {
         float xip1 = 0.0F;
         float xip2 = 0.0F;
         float yip1 = 0.0F;
         float yip2 = 0.0F;

         for(int i = n - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = this._b1 * xip1 + this._b2 * xip2 - this._a1 * yip1 - this._a2 * yip2;
            y[i] += yi;
            yip2 = yip1;
            yip1 = yi;
            xip2 = xip1;
            xip1 = xi;
         }
      } else {
         float xip1 = 0.0F;
         float xip2 = 0.0F;
         float yip1 = 0.0F;
         float yip2 = 0.0F;

         for(int i = n - 1; i >= 0; --i) {
            float xi = x[i];
            float yi = this._b0 * xi + this._b1 * xip1 + this._b2 * xip2 - this._a1 * yip1 - this._a2 * yip2;
            y[i] += yi;
            yip2 = yip1;
            yip1 = yi;
            xip2 = xip1;
            xip1 = xi;
         }
      }
   }

   public void apply1Forward(float[][] x, float[][] y) {
      checkArrays(x, y);
      int n2 = y.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         this.applyForward(x[i2], y[i2]);
      }
   }

   public void apply1Reverse(float[][] x, float[][] y) {
      checkArrays(x, y);
      int n2 = y.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         this.applyReverse(x[i2], y[i2]);
      }
   }

   public void apply2Forward(float[][] x, float[][] y) {
      checkArrays(x, y);
      int n2 = y.length;
      int n1 = y[0].length;
      if (this._b1 == 0.0F && this._b2 == 0.0F && this._a2 == 0.0F) {
         float[] yim1 = new float[n1];

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] xi = x[i2];
            float[] yi = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               yi[i1] = this._b0 * xi[i1] - this._a1 * yim1[i1];
            }

            yim1 = yi;
         }
      } else if (this._b2 == 0.0F && this._a2 == 0.0F) {
         float[] yim1 = new float[n1];
         float[] xim1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] x2 = x[i2];
            float[] yi = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xim1[i1] - this._a1 * yim1[i1];
            }

            yim1 = yi;
            float[] xt = xim1;
            xim1 = xi;
            xi = xt;
         }
      } else if (this._b2 == 0.0F) {
         float[] yim2 = new float[n1];
         float[] yim1 = new float[n1];
         float[] xim1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] x2 = x[i2];
            float[] yi = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xim1[i1] - this._a1 * yim1[i1] - this._a2 * yim2[i1];
            }

            yim2 = yim1;
            yim1 = yi;
            float[] xt = xim1;
            xim1 = xi;
            xi = xt;
         }
      } else if (this._b0 == 0.0F) {
         float[] yim2 = new float[n1];
         float[] yim1 = new float[n1];
         float[] xim2 = new float[n1];
         float[] xim1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] x2 = x[i2];
            float[] yi = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b1 * xim1[i1] + this._b2 * xim2[i1] - this._a1 * yim1[i1] - this._a2 * yim2[i1];
            }

            yim2 = yim1;
            yim1 = yi;
            float[] xt = xim2;
            xim2 = xim1;
            xim1 = xi;
            xi = xt;
         }
      } else {
         float[] yim2 = new float[n1];
         float[] yim1 = new float[n1];
         float[] xim2 = new float[n1];
         float[] xim1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] x2 = x[i2];
            float[] yi = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xim1[i1] + this._b2 * xim2[i1] - this._a1 * yim1[i1] - this._a2 * yim2[i1];
            }

            yim2 = yim1;
            yim1 = yi;
            float[] xt = xim2;
            xim2 = xim1;
            xim1 = xi;
            xi = xt;
         }
      }
   }

   public void apply2Reverse(float[][] x, float[][] y) {
      checkArrays(x, y);
      int n2 = y.length;
      int n1 = y[0].length;
      if (this._b1 == 0.0F && this._b2 == 0.0F && this._a2 == 0.0F) {
         float[] yip1 = new float[n1];

         for(int i2 = n2 - 1; i2 >= 0; --i2) {
            float[] xi = x[i2];
            float[] yi = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               yi[i1] = this._b0 * xi[i1] - this._a1 * yip1[i1];
            }

            yip1 = yi;
         }
      } else if (this._b2 == 0.0F && this._a2 == 0.0F) {
         float[] yip1 = new float[n1];
         float[] xip1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = n2 - 1; i2 >= 0; --i2) {
            float[] x2 = x[i2];
            float[] yi = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xip1[i1] - this._a1 * yip1[i1];
            }

            yip1 = yi;
            float[] xt = xip1;
            xip1 = xi;
            xi = xt;
         }
      } else if (this._b2 == 0.0F) {
         float[] yip2 = new float[n1];
         float[] yip1 = new float[n1];
         float[] xip1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = n2 - 1; i2 >= 0; --i2) {
            float[] x2 = x[i2];
            float[] yi = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xip1[i1] - this._a1 * yip1[i1] - this._a2 * yip2[i1];
            }

            yip2 = yip1;
            yip1 = yi;
            float[] xt = xip1;
            xip1 = xi;
            xi = xt;
         }
      } else if (this._b0 == 0.0F) {
         float[] yip2 = new float[n1];
         float[] yip1 = new float[n1];
         float[] xip2 = new float[n1];
         float[] xip1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = n2 - 1; i2 >= 0; --i2) {
            float[] x2 = x[i2];
            float[] yi = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b1 * xip1[i1] + this._b2 * xip2[i1] - this._a1 * yip1[i1] - this._a2 * yip2[i1];
            }

            yip2 = yip1;
            yip1 = yi;
            float[] xt = xip2;
            xip2 = xip1;
            xip1 = xi;
            xi = xt;
         }
      } else {
         float[] yip2 = new float[n1];
         float[] yip1 = new float[n1];
         float[] xip2 = new float[n1];
         float[] xip1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = n2 - 1; i2 >= 0; --i2) {
            float[] x2 = x[i2];
            float[] yi = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xip1[i1] + this._b2 * xip2[i1] - this._a1 * yip1[i1] - this._a2 * yip2[i1];
            }

            yip2 = yip1;
            yip1 = yi;
            float[] xt = xip2;
            xip2 = xip1;
            xip1 = xi;
            xi = xt;
         }
      }
   }

   public void accumulate1Forward(float[][] x, float[][] y) {
      checkArrays(x, y);
      int n2 = y.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         this.accumulateForward(x[i2], y[i2]);
      }
   }

   public void accumulate1Reverse(float[][] x, float[][] y) {
      checkArrays(x, y);
      int n2 = y.length;

      for(int i2 = 0; i2 < n2; ++i2) {
         this.accumulateReverse(x[i2], y[i2]);
      }
   }

   public void accumulate2Forward(float[][] x, float[][] y) {
      checkArrays(x, y);
      int n2 = y.length;
      int n1 = y[0].length;
      if (this._b1 == 0.0F && this._b2 == 0.0F && this._a2 == 0.0F) {
         float[] yim1 = new float[n1];
         float[] yi = new float[n1];

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] xi = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               yi[i1] = this._b0 * xi[i1] - this._a1 * yim1[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yim1;
            yim1 = yi;
            yi = yt;
         }
      } else if (this._b2 == 0.0F && this._a2 == 0.0F) {
         float[] yim1 = new float[n1];
         float[] yi = new float[n1];
         float[] xim1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] x2 = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xim1[i1] - this._a1 * yim1[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yim1;
            yim1 = yi;
            yi = yt;
            float[] xt = xim1;
            xim1 = xi;
            xi = xt;
         }
      } else if (this._b2 == 0.0F) {
         float[] yim2 = new float[n1];
         float[] yim1 = new float[n1];
         float[] yi = new float[n1];
         float[] xim1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] x2 = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xim1[i1] - this._a1 * yim1[i1] - this._a2 * yim2[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yim2;
            yim2 = yim1;
            yim1 = yi;
            yi = yt;
            float[] xt = xim1;
            xim1 = xi;
            xi = xt;
         }
      } else if (this._b0 == 0.0F) {
         float[] yim2 = new float[n1];
         float[] yim1 = new float[n1];
         float[] yi = new float[n1];
         float[] xim2 = new float[n1];
         float[] xim1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] x2 = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b1 * xim1[i1] + this._b2 * xim2[i1] - this._a1 * yim1[i1] - this._a2 * yim2[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yim2;
            yim2 = yim1;
            yim1 = yi;
            yi = yt;
            float[] xt = xim2;
            xim2 = xim1;
            xim1 = xi;
            xi = xt;
         }
      } else {
         float[] yim2 = new float[n1];
         float[] yim1 = new float[n1];
         float[] yi = new float[n1];
         float[] xim2 = new float[n1];
         float[] xim1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = 0; i2 < n2; ++i2) {
            float[] x2 = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xim1[i1] + this._b2 * xim2[i1] - this._a1 * yim1[i1] - this._a2 * yim2[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yim2;
            yim2 = yim1;
            yim1 = yi;
            yi = yt;
            float[] xt = xim2;
            xim2 = xim1;
            xim1 = xi;
            xi = xt;
         }
      }
   }

   public void accumulate2Reverse(float[][] x, float[][] y) {
      checkArrays(x, y);
      int n2 = y.length;
      int n1 = y[0].length;
      if (this._b1 == 0.0F && this._b2 == 0.0F && this._a2 == 0.0F) {
         float[] yip1 = new float[n1];
         float[] yi = new float[n1];

         for(int i2 = n2 - 1; i2 >= 0; --i2) {
            float[] xi = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               yi[i1] = this._b0 * xi[i1] - this._a1 * yip1[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yip1;
            yip1 = yi;
            yi = yt;
         }
      } else if (this._b2 == 0.0F && this._a2 == 0.0F) {
         float[] yip1 = new float[n1];
         float[] yi = new float[n1];
         float[] xip1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = n2 - 1; i2 >= 0; --i2) {
            float[] x2 = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xip1[i1] - this._a1 * yip1[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yip1;
            yip1 = yi;
            yi = yt;
            float[] xt = xip1;
            xip1 = xi;
            xi = xt;
         }
      } else if (this._b2 == 0.0F) {
         float[] yip2 = new float[n1];
         float[] yip1 = new float[n1];
         float[] yi = new float[n1];
         float[] xip1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = n2 - 1; i2 >= 0; --i2) {
            float[] x2 = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xip1[i1] - this._a1 * yip1[i1] - this._a2 * yip2[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yip2;
            yip2 = yip1;
            yip1 = yi;
            yi = yt;
            float[] xt = xip1;
            xip1 = xi;
            xi = xt;
         }
      } else if (this._b0 == 0.0F) {
         float[] yip2 = new float[n1];
         float[] yip1 = new float[n1];
         float[] yi = new float[n1];
         float[] xip2 = new float[n1];
         float[] xip1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = n2 - 1; i2 >= 0; --i2) {
            float[] x2 = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b1 * xip1[i1] + this._b2 * xip2[i1] - this._a1 * yip1[i1] - this._a2 * yip2[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yip2;
            yip2 = yip1;
            yip1 = yi;
            yi = yt;
            float[] xt = xip2;
            xip2 = xip1;
            xip1 = xi;
            xi = xt;
         }
      } else {
         float[] yip2 = new float[n1];
         float[] yip1 = new float[n1];
         float[] yi = new float[n1];
         float[] xip2 = new float[n1];
         float[] xip1 = new float[n1];
         float[] xi = new float[n1];

         for(int i2 = n2 - 1; i2 >= 0; --i2) {
            float[] x2 = x[i2];
            float[] y2 = y[i2];

            for(int i1 = 0; i1 < n1; ++i1) {
               xi[i1] = x2[i1];
               yi[i1] = this._b0 * xi[i1] + this._b1 * xip1[i1] + this._b2 * xip2[i1] - this._a1 * yip1[i1] - this._a2 * yip2[i1];
               y2[i1] += yi[i1];
            }

            float[] yt = yip2;
            yip2 = yip1;
            yip1 = yi;
            yi = yt;
            float[] xt = xip2;
            xip2 = xip1;
            xip1 = xi;
            xi = xt;
         }
      }
   }

   public void apply1Forward(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         this.apply1Forward(x[i3], y[i3]);
      }
   }

   public void apply1Reverse(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         this.apply1Reverse(x[i3], y[i3]);
      }
   }

   public void apply2Forward(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         this.apply2Forward(x[i3], y[i3]);
      }
   }

   public void apply2Reverse(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         this.apply2Reverse(x[i3], y[i3]);
      }
   }

   public void apply3Forward(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;
      int n2 = y[0].length;
      int n1 = y[0][0].length;
      float[][] xy = new float[n3][n1];

      for(int i2 = 0; i2 < n2; ++i2) {
         this.get2(i2, x, xy);
         this.apply2Forward(xy, xy);
         this.set2(i2, xy, y);
      }
   }

   public void apply3Reverse(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;
      int n2 = y[0].length;
      int n1 = y[0][0].length;
      float[][] xy = new float[n3][n1];

      for(int i2 = 0; i2 < n2; ++i2) {
         this.get2(i2, x, xy);
         this.apply2Reverse(xy, xy);
         this.set2(i2, xy, y);
      }
   }

   public void accumulate1Forward(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         this.accumulate1Forward(x[i3], y[i3]);
      }
   }

   public void accumulate1Reverse(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         this.accumulate1Reverse(x[i3], y[i3]);
      }
   }

   public void accumulate2Forward(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         this.accumulate2Forward(x[i3], y[i3]);
      }
   }

   public void accumulate2Reverse(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;

      for(int i3 = 0; i3 < n3; ++i3) {
         this.accumulate2Reverse(x[i3], y[i3]);
      }
   }

   public void accumulate3Forward(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;
      int n2 = y[0].length;
      int n1 = y[0][0].length;
      float[][] xy = new float[n3][n1];

      for(int i2 = 0; i2 < n2; ++i2) {
         this.get2(i2, x, xy);
         this.apply2Forward(xy, xy);
         this.acc2(i2, xy, y);
      }
   }

   public void accumulate3Reverse(float[][][] x, float[][][] y) {
      checkArrays(x, y);
      int n3 = y.length;
      int n2 = y[0].length;
      int n1 = y[0][0].length;
      float[][] xy = new float[n3][n1];

      for(int i2 = 0; i2 < n2; ++i2) {
         this.get2(i2, x, xy);
         this.apply2Reverse(xy, xy);
         this.acc2(i2, xy, y);
      }
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

   private void get2(int i2, float[][][] x, float[][] x2) {
      int n3 = x2.length;
      int n1 = x2[0].length;

      for(int i3 = 0; i3 < n3; ++i3) {
         float[] x32 = x[i3][i2];
         float[] x23 = x2[i3];

         for(int i1 = 0; i1 < n1; ++i1) {
            x23[i1] = x32[i1];
         }
      }
   }

   private void set2(int i2, float[][] x2, float[][][] x) {
      int n3 = x2.length;
      int n1 = x2[0].length;

      for(int i3 = 0; i3 < n3; ++i3) {
         float[] x32 = x[i3][i2];
         float[] x23 = x2[i3];

         for(int i1 = 0; i1 < n1; ++i1) {
            x32[i1] = x23[i1];
         }
      }
   }

   private void acc2(int i2, float[][] x2, float[][][] x) {
      int n3 = x2.length;
      int n1 = x2[0].length;

      for(int i3 = 0; i3 < n3; ++i3) {
         float[] x32 = x[i3][i2];
         float[] x23 = x2[i3];

         for(int i1 = 0; i1 < n1; ++i1) {
            x32[i1] += x23[i1];
         }
      }
   }
}
