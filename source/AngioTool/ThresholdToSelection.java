package AngioTool;

import Utils.Utils;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.ImageProcessor;
import java.awt.Polygon;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

public class ThresholdToSelection {
   ImageProcessor ip;
   public float min;
   public float max;
   int w;
   int h;

   public Roi convert(ImageProcessor ip) {
      this.ip = ip;
      this.min = (float)ip.getMinThreshold();
      this.max = (float)ip.getMaxThreshold();
      this.w = ip.getWidth();
      this.h = ip.getHeight();
      return this.getRoi();
   }

   final boolean selected(int x, int y) {
      float v = this.ip.getf(x, y);
      return v >= this.min && v <= this.max;
   }

   Roi getRoi() {
      ArrayList polygons = new ArrayList();
      int progressInc = Math.max(this.h / 50, 1);
      boolean[] prevRow = new boolean[this.w + 2];
      boolean[] thisRow = new boolean[this.w + 2];
      ThresholdToSelection.Outline[] outline = new ThresholdToSelection.Outline[this.w + 1];

      for(int y = 0; y <= this.h; ++y) {
         boolean[] b = prevRow;
         prevRow = thisRow;
         thisRow = b;

         for(int x = 0; x <= this.w; ++x) {
            if (y < this.h && x < this.w) {
               thisRow[x + 1] = this.selected(x, y);
            } else {
               thisRow[x + 1] = false;
            }

            if (thisRow[x + 1]) {
               if (!prevRow[x + 1]) {
                  if (outline[x] == null) {
                     if (outline[x + 1] == null) {
                        outline[x + 1] = outline[x] = new ThresholdToSelection.Outline();
                        outline[x].push(x + 1, y);
                        outline[x].push(x, y);
                     } else {
                        outline[x] = outline[x + 1];
                        outline[x + 1] = null;
                        outline[x].push(x, y);
                     }
                  } else if (outline[x + 1] == null) {
                     outline[x + 1] = outline[x];
                     outline[x] = null;
                     outline[x + 1].shift(x + 1, y);
                  } else if (outline[x + 1] == outline[x]) {
                     polygons.add(outline[x].getPolygon());
                     outline[x] = outline[x + 1] = null;
                  } else {
                     outline[x].shift(outline[x + 1]);

                     for(int x1 = 0; x1 <= this.w; ++x1) {
                        if (x1 != x + 1 && outline[x1] == outline[x + 1]) {
                           outline[x1] = outline[x];
                           outline[x] = outline[x + 1] = null;
                           break;
                        }
                     }

                     if (outline[x] != null) {
                        throw new RuntimeException("assertion failed");
                     }
                  }
               }

               if (!thisRow[x]) {
                  if (outline[x] == null) {
                     throw new RuntimeException("assertion failed!");
                  }

                  outline[x].push(x, y + 1);
               }
            } else {
               if (prevRow[x + 1]) {
                  if (outline[x] == null) {
                     if (outline[x + 1] == null) {
                        outline[x] = outline[x + 1] = new ThresholdToSelection.Outline();
                        outline[x].push(x, y);
                        outline[x].push(x + 1, y);
                     } else {
                        outline[x] = outline[x + 1];
                        outline[x + 1] = null;
                        outline[x].shift(x, y);
                     }
                  } else if (outline[x + 1] == null) {
                     outline[x + 1] = outline[x];
                     outline[x] = null;
                     outline[x + 1].push(x + 1, y);
                  } else if (outline[x + 1] == outline[x]) {
                     polygons.add(outline[x].getPolygon());
                     outline[x] = outline[x + 1] = null;
                  } else {
                     outline[x].push(outline[x + 1]);

                     for(int x1 = 0; x1 <= this.w; ++x1) {
                        if (x1 != x + 1 && outline[x1] == outline[x + 1]) {
                           outline[x1] = outline[x];
                           outline[x] = outline[x + 1] = null;
                           break;
                        }
                     }

                     if (outline[x] != null) {
                        throw new RuntimeException("assertion failed");
                     }
                  }
               }

               if (thisRow[x]) {
                  if (outline[x] == null) {
                     throw new RuntimeException("assertion failed");
                  }

                  outline[x].shift(x, y + 1);
               }
            }
         }

         if ((y & progressInc) == 0) {
         }
      }

      GeneralPath path = new GeneralPath(0);

      for(int i = 0; i < polygons.size(); ++i) {
         path.append((Polygon)polygons.get(i), false);
      }

      ShapeRoi shape = new ShapeRoi(path);
      Roi roi = shape != null ? shape.shapeToRoi() : null;
      return (Roi)(roi != null ? roi : shape);
   }

   static class Outline {
      int[] x;
      int[] y;
      int first;
      int last;
      int reserved;
      final int GROW = 10;

      public Outline() {
         this.reserved = 10;
         this.x = new int[this.reserved];
         this.y = new int[this.reserved];
         this.first = this.last = 5;
      }

      private void needs(int newCount, int offset) {
         if (newCount > this.reserved || offset > this.first) {
            if (newCount < this.reserved + 10 + 1) {
               newCount = this.reserved + 10 + 1;
            }

            int[] newX = new int[newCount];
            int[] newY = new int[newCount];
            System.arraycopy(this.x, 0, newX, offset, this.last);
            System.arraycopy(this.y, 0, newY, offset, this.last);
            this.x = newX;
            this.y = newY;
            this.first += offset;
            this.last += offset;
            this.reserved = newCount;
         }
      }

      public void push(int x, int y) {
         this.needs(this.last + 1, 0);
         this.x[this.last] = x;
         this.y[this.last] = y;
         ++this.last;
      }

      public void shift(int x, int y) {
         this.needs(this.last + 1, 10);
         --this.first;
         this.x[this.first] = x;
         this.y[this.first] = y;
      }

      public void push(ThresholdToSelection.Outline o) {
         int count = o.last - o.first;
         this.needs(this.last + count, 0);
         System.arraycopy(o.x, o.first, this.x, this.last, count);
         System.arraycopy(o.y, o.first, this.y, this.last, count);
         this.last += count;
      }

      public void shift(ThresholdToSelection.Outline o) {
         int count = o.last - o.first;
         this.needs(this.last + count + 10, count + 10);
         this.first -= count;
         System.arraycopy(o.x, o.first, this.x, this.first, count);
         System.arraycopy(o.y, o.first, this.y, this.first, count);
      }

      public Polygon getPolygon() {
         int j = this.first + 1;

         int i;
         for(i = this.first + 1; i + 1 < this.last; ++j) {
            int x1 = this.x[j] - this.x[j - 1];
            int y1 = this.y[j] - this.y[j - 1];
            int x2 = this.x[j + 1] - this.x[j];
            int y2 = this.y[j + 1] - this.y[j];
            if (x1 * y2 == x2 * y1) {
               --this.last;
            } else {
               if (i != j) {
                  this.x[i] = this.x[j];
                  this.y[i] = this.y[j];
               }

               ++i;
            }
         }

         int x1 = this.x[j] - this.x[j - 1];
         int y1 = this.y[j] - this.y[j - 1];
         int x2 = this.x[this.first] - this.x[j];
         int y2 = this.y[this.first] - this.y[j];
         if (x1 * y2 == x2 * y1) {
            --this.last;
         } else {
            this.x[i] = this.x[j];
            this.y[i] = this.y[j];
         }

         int count = this.last - this.first;
         int[] xNew = new int[count];
         int[] yNew = new int[count];
         System.arraycopy(this.x, this.first, xNew, 0, count);
         System.arraycopy(this.y, this.first, yNew, 0, count);
         return new Polygon(xNew, yNew, count);
      }

      @Override
      public String toString() {
         String res = "(first:" + this.first + ",last:" + this.last + ",reserved:" + this.reserved + ":";
         if (this.last > this.x.length && !Utils.isReleaseVersion) {
            System.err.println("ERROR!");
         }

         for(int i = this.first; i < this.last && i < this.x.length; ++i) {
            res = res + "(" + this.x[i] + "," + this.y[i] + ")";
         }

         return res + ")";
      }
   }
}
