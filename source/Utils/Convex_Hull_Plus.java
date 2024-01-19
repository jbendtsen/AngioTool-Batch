package Utils;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.measure.Measurements;
import ij.process.ImageProcessor;

public class Convex_Hull_Plus implements Measurements {
   ImagePlus imp;
   int counter = 0;
   private PolygonRoi convexHullPolygonRoi = null;

   public void run(ImageProcessor ip) {
      this.imp = new ImagePlus("", ip);
      String myMode = "Convex Hull selection";
      boolean white = true;
      int k = 0;
      int colour = 0;
      if (white) {
         colour = 255;
      }

      for(int j = 0; j < this.imp.getHeight(); ++j) {
         for(int i = 0; i < this.imp.getWidth(); ++i) {
            if (ip.getPixel(i, j) == colour) {
               ++this.counter;
            }
         }
      }

      int[] x = new int[this.counter + 1];
      int[] y = new int[this.counter + 1];

      for(int var74 = 0; var74 < this.imp.getHeight(); ++var74) {
         for(int i = 0; i < this.imp.getWidth(); ++i) {
            if (ip.getPixel(i, var74) == colour) {
               x[k] = i;
               y[k] = var74;
               ++k;
            }
         }
      }

      int n = this.counter;
      int min = 0;
      int ney = 0;
      double zxmi = 0.0;

      for(int i = 1; i < n; ++i) {
         if (y[i] < y[min]) {
            min = i;
         }
      }

      int temp = x[0];
      x[0] = x[min];
      x[min] = temp;
      temp = y[0];
      y[0] = y[min];
      y[min] = temp;
      min = 0;

      for(int var67 = 1; var67 < n; ++var67) {
         if (y[var67] == y[0]) {
            ++ney;
            if (x[var67] < x[min]) {
               min = var67;
            }
         }
      }

      temp = x[0];
      x[0] = x[min];
      x[min] = temp;
      temp = y[0];
      y[0] = y[min];
      y[min] = temp;
      ip.setColor(127);
      int px = x[0];
      int py = y[0];
      min = 0;
      int m = -1;
      x[n] = x[min];
      y[n] = y[min];
      double minangle;
      if (ney > 0) {
         minangle = -1.0;
      } else {
         minangle = 0.0;
      }

      while(min != n + 0) {
         temp = x[++m];
         x[m] = x[min];
         x[min] = temp;
         temp = y[m];
         y[m] = y[min];
         y[min] = temp;
         min = n;
         double v = minangle;
         minangle = 360.0;
         int h2 = 0;

         for(int var68 = m + 1; var68 < n + 1; ++var68) {
            int dx = x[var68] - x[m];
            int ax = Math.abs(dx);
            int dy = y[var68] - y[m];
            int ay = Math.abs(dy);
            double t;
            if (dx == 0 && dy == 0) {
               t = 0.0;
            } else {
               t = (double)dy / (double)(ax + ay);
            }

            if (dx < 0) {
               t = 2.0 - t;
            } else if (dy < 0) {
               t += 4.0;
            }

            double th = t * 90.0;
            if (th > v) {
               if (th < minangle) {
                  min = var68;
                  minangle = th;
                  h2 = dx * dx + dy * dy;
               } else if (th == minangle) {
                  int h = dx * dx + dy * dy;
                  if (h > h2) {
                     min = var68;
                     h2 = h;
                  }
               }
            }
         }

         if (myMode.equals("Draw Convex Hull") || myMode.equals("Draw both")) {
            ip.drawLine(px, py, x[min], y[min]);
         }

         px = x[min];
         py = y[min];
         zxmi += Math.sqrt((double)h2);
      }

      int[] hx = new int[++m];
      int[] hy = new int[m];

      for(int var69 = 0; var69 < m; ++var69) {
         hx[var69] = x[var69];
         hy[var69] = y[var69];
      }

      if (myMode.equals("Convex Hull selection")) {
         this.convexHullPolygonRoi = new PolygonRoi(hx, hy, hx.length, 2);
         this.imp.setRoi(this.convexHullPolygonRoi);
      }

      double[] d = new double[m * (m - 1) / 2];
      int[] p1 = new int[m * (m - 1) / 2];
      int[] p2 = new int[m * (m - 1) / 2];
      k = 0;

      for(int var70 = 0; var70 < m - 1; ++var70) {
         for(int var75 = var70 + 1; var75 < m; ++var75) {
            d[k] = Math.sqrt(Math.pow((double)(hx[var70] - hx[var75]), 2.0) + Math.pow((double)(hy[var70] - hy[var75]), 2.0));
            p1[k] = var70;
            p2[k] = var75;
            ++k;
         }
      }

      --k;
      boolean sw = true;

      while(sw) {
         sw = false;

         for(int var71 = 0; var71 < k - 1; ++var71) {
            if (d[var71] < d[var71 + 1]) {
               double tempd = d[var71];
               d[var71] = d[var71 + 1];
               d[var71 + 1] = tempd;
               temp = p1[var71];
               p1[var71] = p1[var71 + 1];
               p1[var71 + 1] = temp;
               temp = p2[var71];
               p2[var71] = p2[var71 + 1];
               p2[var71 + 1] = temp;
               sw = true;
            }
         }
      }

      double radius = d[0] / 2.0;
      double cx = (double)(hx[p1[0]] + hx[p2[0]]) / 2.0;
      double cy = (double)(hy[p1[0]] + hy[p2[0]]) / 2.0;
      int p3 = -1;
      double tt = radius;

      for(int var72 = 0; var72 < m; ++var72) {
         double tttemp = Math.sqrt(Math.pow((double)hx[var72] - cx, 2.0) + Math.pow((double)hy[var72] - cy, 2.0));
         if (tttemp > tt) {
            tt = tttemp;
            p3 = var72;
         }
      }

      if (p3 > -1) {
         double[] op1 = new double[2];
         double[] op2 = new double[2];
         double[] op3 = new double[2];
         double[] circ = new double[3];
         double tD = Double.MAX_VALUE;
         int tp1 = 0;
         int tp2 = 0;
         int tp3 = 0;

         for(int var73 = 0; var73 < m - 2; ++var73) {
            for(int var76 = var73 + 1; var76 < m - 1; ++var76) {
               for(int var79 = var76 + 1; var79 < m; ++var79) {
                  op1[0] = (double)hx[var73];
                  op1[1] = (double)hy[var73];
                  op2[0] = (double)hx[var76];
                  op2[1] = (double)hy[var76];
                  op3[0] = (double)hx[var79];
                  op3[1] = (double)hy[var79];
                  this.osculating(op1, op2, op3, circ);
                  if (circ[2] > 0.0) {
                     sw = true;

                     for(int z = 0; z < m; ++z) {
                        double tttemp = (double)((float)Math.sqrt(Math.pow((double)hx[z] - circ[0], 2.0) + Math.pow((double)hy[z] - circ[1], 2.0)));
                        if (tttemp > circ[2]) {
                           sw = false;
                           break;
                        }
                     }

                     if (sw && circ[2] < tD) {
                        tp1 = var73;
                        tp2 = var76;
                        tp3 = var79;
                        tD = circ[2];
                     }
                  }
               }
            }
         }

         op1[0] = (double)hx[tp1];
         op1[1] = (double)hy[tp1];
         op2[0] = (double)hx[tp2];
         op2[1] = (double)hy[tp2];
         op3[0] = (double)hx[tp3];
         op3[1] = (double)hy[tp3];
         this.osculating(op1, op2, op3, circ);
         radius = circ[2];
         if (myMode.equals("Minimal Bounding Circle selection") && circ[2] > 0.0) {
            IJ.makeOval(
               (int)Math.floor(circ[0] - circ[2] + 0.5),
               (int)Math.floor(circ[1] - circ[2] + 0.5),
               (int)Math.floor(radius * 2.0 + 0.5),
               (int)Math.floor(radius * 2.0 + 0.5)
            );
         }

         if ((myMode.equals("Draw Minimal Bounding Circle") || myMode.equals("Draw both")) && circ[2] > 0.0) {
            IJ.makeOval(
               (int)Math.floor(circ[0] - circ[2] + 0.5),
               (int)Math.floor(circ[1] - circ[2] + 0.5),
               (int)Math.floor(radius * 2.0 + 0.5),
               (int)Math.floor(radius * 2.0 + 0.5)
            );
            IJ.run("Unlock Image");
            IJ.run("Draw");
         }
      } else {
         if (myMode.equals("Minimal Bounding Circle selection")) {
            IJ.makeOval((int)Math.floor(cx - radius + 0.5), (int)Math.floor(cy - radius + 0.5), (int)Math.floor(d[0] + 0.5), (int)Math.floor(d[0] + 0.5));
         }

         if (myMode.equals("Draw Minimal Bounding Circle") || myMode.equals("Draw both")) {
            IJ.makeOval((int)(cx - radius), (int)(cy - radius), (int)Math.floor(d[0] + 0.5), (int)Math.floor(d[0] + 0.5));
            IJ.run("Unlock Image");
            IJ.run("Draw");
         }
      }
   }

   public PolygonRoi getConvexHullPolygonRoi() {
      return this.convexHullPolygonRoi;
   }

   void osculating(double[] pa, double[] pb, double[] pc, double[] centrad) {
      if ((pa[0] != pb[0] || pb[0] != pc[0]) && (pa[1] != pb[1] || pb[1] != pc[1])) {
         double a = pb[0] - pa[0];
         double b = pb[1] - pa[1];
         double c = pc[0] - pa[0];
         double d = pc[1] - pa[1];
         double e = a * (pa[0] + pb[0]) + b * (pa[1] + pb[1]);
         double f = c * (pa[0] + pc[0]) + d * (pa[1] + pc[1]);
         double g = 2.0 * (a * (pc[1] - pb[1]) - b * (pc[0] - pb[0]));
         if (g == 0.0) {
            centrad[0] = 0.0;
            centrad[1] = 0.0;
            centrad[2] = -1.0;
         } else {
            centrad[0] = (d * e - b * f) / g;
            centrad[1] = (a * f - c * e) / g;
            centrad[2] = (double)((float)Math.sqrt(Math.pow(pa[0] - centrad[0], 2.0) + Math.pow(pa[1] - centrad[1], 2.0)));
         }
      } else {
         centrad[0] = 0.0;
         centrad[1] = 0.0;
         centrad[2] = -1.0;
      }
   }
}
