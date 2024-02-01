package Lacunarity;

import Utils.Utils;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Lacunarity {
   private int boxmov = 5;
   private int numberOfBins = 10;
   private int minSize = 10;
   private int imgWidth;
   private int imgHeight;
   private int smallDimension;
   private int rWidth;
   private int rHeight;
   private ArrayList<Integer> Fl3 = new ArrayList<>();
   private ArrayList<Integer> El3 = new ArrayList<>();
   private ArrayList<Double> Elamda3 = new ArrayList<>();
   private ArrayList<Double> Flamda3 = new ArrayList<>();
   private ArrayList<Double> Eonepluslamda3 = new ArrayList<>();
   private ArrayList<Double> Fonepluslamda3 = new ArrayList<>();
   private ArrayList<Integer> epsilon3 = new ArrayList<>();
   private ImagePlus imp;
   private ImageProcessor ip;
   private Rectangle r;
   private double averageEl;
   private double averageFl;
   private double stdevEl;
   private double stdevFl;
   private boolean whiteForeground = true;
   private int[] boxS;

   public Lacunarity(ImagePlus imp, int numberOfBins, int minSize, int boxmov, boolean whiteForeGround) {
      this.imp = imp;
      this.ip = imp.getProcessor();
      this.numberOfBins = numberOfBins;
      this.minSize = minSize;
      this.boxmov = boxmov;
      this.whiteForeground = whiteForeGround;
      int[][] img = imp.getProcessor().getIntArray();
      ArrayList<Point> array = new ArrayList<>();

      for(int i = 0; i < img.length; ++i) {
         for(int ii = 0; ii < img[0].length; ++ii) {
            if (this.whiteForeground) {
               if (img[i][ii] == 255) {
                  array.add(new Point(i, ii));
               }
            } else if (img[i][ii] == 0) {
               array.add(new Point(i, ii));
            }
         }
      }

      Collections.sort(array, new Lacunarity.PointCompareY());
      int minY = array.get(0).y;
      int maxY = array.get(array.size() - 1).y;
      Collections.sort(array, new Lacunarity.PointCompareX());
      int minX = array.get(0).x;
      int maxX = array.get(array.size() - 1).x;
      Rectangle r2 = new Rectangle(minX, minY, maxX - minX, maxY - minY);
      this.imgWidth = this.ip.getWidth();
      this.imgHeight = this.ip.getHeight();
      this.rWidth = maxX - minX;
      this.rHeight = maxY - minY;
      this.boxS = this.computeBoxesBins2(this.rWidth, this.rHeight, minSize);

      for(int i = 0; i < this.boxS.length; ++i) {
         int boxsize = this.boxS[i];
         this.El3.clear();
         this.Fl3.clear();
         long step2 = System.currentTimeMillis();
         long step3 = System.currentTimeMillis();
         this.slidingBox5(img, r2, boxsize, boxmov);
         long step4 = System.currentTimeMillis();
         long AT3 = step3 - step2;
         long AT4 = step4 - step3;
         if (AT3 == 0L) {
            AT3 = 1L;
         }

         if (AT4 == 0L) {
            AT4 = 1L;
         }

         this.Elamda3.add(this.lacunarity(this.El3));
         this.Flamda3.add(this.lacunarity(this.Fl3));
         this.Eonepluslamda3.add(this.onePlusLacunarity(this.El3));
         this.Fonepluslamda3.add(this.onePlusLacunarity(this.Fl3));
      }
   }

   private int[] computeBoxesBins2(int width, int height, int minSize) {
      int[] bins = new int[this.numberOfBins];
      this.smallDimension = width < height ? width : height;
      int factor = (int)Math.floor((double)((this.smallDimension - minSize) / this.numberOfBins));
      bins[0] = minSize;

      for(int i = 1; i < this.numberOfBins - 1; ++i) {
         bins[i] = bins[i - 1] + factor;
      }

      bins[this.numberOfBins - 1] = this.smallDimension;
      return bins;
   }

   public void slidingBox5(int[][] imageArray, Rectangle r, int boxsize, int boxmov) {
      int numXBox = computeNumXBox2(r.width, boxmov, boxsize);
      int numYBox = computeNumYBox2(r.height, boxmov, boxsize);
      Box[][] b = new Box[numXBox][numYBox];
      b[0][0] = this.firstBoxPixelMass(imageArray, r.x + 0 * boxmov, r.y + 0 * boxmov, boxsize);

      for(int i = 0; i < numXBox; ++i) {
         if (i == 0) {
            b[i][0] = this.firstBoxPixelMass(imageArray, r.x + i * boxmov, r.y + 0 * boxmov, boxsize);
         } else {
            b[i][0] = this.nextHorizontalBoxPixelMass(imageArray, b[i - 1][0], r.x + i * boxmov, r.y + 0 * boxmov, boxsize);
         }

         for(int ii = 0; ii < numYBox - 1; ++ii) {
            b[i][ii + 1] = this.nextBoxPixelMass(imageArray, b[i][ii], r.x + i * boxmov, r.y + (ii + 1) * boxmov, boxsize);
         }
      }

      for(int i = 0; i < numXBox; ++i) {
         for(int ii = 0; ii < numYBox; ++ii) {
            int count = b[i][ii].pixelCount;
            if (count > 0) {
               this.El3.add(count);
               this.Fl3.add(count);
            } else {
               this.El3.add(count);
            }
         }
      }
   }

   public Box firstBoxPixelMass(int[][] imageArray, int xStart, int yStart, int boxsize) {
      int count = 0;
      int firstColumnPixelCount = 0;
      int firstRawPixelCount = 0;
      int xBorder = xStart + boxsize > this.imgWidth ? this.imgWidth : xStart + boxsize;
      int yBorder = yStart + boxsize > this.imgHeight ? this.imgHeight : yStart + boxsize;
      Box b = new Box();

      for(int x = xStart; x < xBorder; ++x) {
         for(int y = yStart; y < yBorder; ++y) {
            if (this.whiteForeground) {
               if (imageArray[x][y] == 255) {
                  ++count;
               }
            } else if (imageArray[x][y] == 0) {
               ++count;
            }

            if (this.whiteForeground) {
               if (x < this.boxmov && imageArray[x][y] == 255) {
                  ++firstColumnPixelCount;
               }

               if (y < this.boxmov && imageArray[x][y] == 255) {
                  ++firstRawPixelCount;
               }
            } else {
               if (x < this.boxmov && imageArray[x][y] == 0) {
                  ++firstColumnPixelCount;
               }

               if (y < this.boxmov && imageArray[x][y] == 0) {
                  ++firstRawPixelCount;
               }
            }
         }
      }

      b.pixelCount = count;
      b.xStart = xStart;
      b.yStart = yStart;
      b.xBorder = xBorder;
      b.yBorder = yBorder;
      b.firstColumnPixelCount = firstColumnPixelCount;
      b.firstRawPixelCount = firstRawPixelCount;
      return b;
   }

   public Box nextHorizontalBoxPixelMass(int[][] imageArray, Box previousBox, int xStart, int yStart, int boxsize) {
      int count = 0;
      int firstColumnPixelCount = 0;
      int firstRawPixelCount = 0;
      int lastColumnPixelCount = 0;
      int xBorder = xStart + boxsize > this.imgWidth ? this.imgWidth : xStart + boxsize;
      int yBorder = yStart + boxsize > this.imgHeight ? this.imgHeight : yStart + boxsize;
      Box b = new Box();
      int rxWidthF = xStart + boxsize > this.imgWidth ? boxsize - (xStart + boxsize - this.imgWidth) : boxsize;
      int ryWidthF = yStart + this.boxmov > this.imgHeight ? this.boxmov - (yStart + this.boxmov - this.imgHeight) : this.boxmov;
      firstRawPixelCount = this.pixelMassInRectangle(imageArray, new Rectangle(xStart, yStart, rxWidthF, ryWidthF));
      rxWidthF = xStart + this.boxmov > this.imgWidth ? this.boxmov - (xStart + this.boxmov - this.imgWidth) : this.boxmov;
      ryWidthF = yStart + boxsize > this.imgHeight ? boxsize - (yStart + boxsize - this.imgHeight) : boxsize;
      firstColumnPixelCount = this.pixelMassInRectangle(imageArray, new Rectangle(xStart, yStart, rxWidthF, ryWidthF));
      int rxStartL = xStart + boxsize - this.boxmov;
      int rxWidthL = rxStartL + this.boxmov > this.imgWidth ? this.boxmov - (rxStartL + this.boxmov - this.imgWidth) : this.boxmov;
      lastColumnPixelCount = this.pixelMassInRectangle(imageArray, new Rectangle(rxStartL, yStart, rxWidthL, ryWidthF));
      count = previousBox.pixelCount - previousBox.firstColumnPixelCount + lastColumnPixelCount;
      b.pixelCount = count;
      b.xStart = xStart;
      b.yStart = yStart;
      b.xBorder = xBorder;
      b.yBorder = yBorder;
      b.firstColumnPixelCount = firstColumnPixelCount;
      b.firstRawPixelCount = firstRawPixelCount;
      return b;
   }

   public Box nextBoxPixelMass(int[][] imageArray, Box previousBox, int xStart, int yStart, int boxsize) {
      int count = 0;
      int firstColumnPixelCount = 0;
      int firstRawPixelCount = 0;
      int lastRawPixelCount = 0;
      int xBorder = xStart + boxsize > this.imgWidth ? this.imgWidth : xStart + boxsize;
      int yBorder = yStart + boxsize > this.imgHeight ? this.imgHeight : yStart + boxsize;
      Box b = new Box();
      int rxWidthF = xStart + boxsize > this.imgWidth ? boxsize - (xStart + boxsize - this.imgWidth) : boxsize;
      int ryWidthF = yStart + this.boxmov > this.imgHeight ? this.boxmov - (yStart + this.boxmov - this.imgHeight) : this.boxmov;
      firstRawPixelCount = this.pixelMassInRectangle(imageArray, new Rectangle(xStart, yStart, rxWidthF, ryWidthF));
      int ryStartL = yStart + boxsize - this.boxmov;
      int ryWidthL = ryStartL + this.boxmov > this.imgHeight ? this.boxmov - (ryStartL + this.boxmov - this.imgHeight) : this.boxmov;
      lastRawPixelCount = this.pixelMassInRectangle(imageArray, new Rectangle(xStart, ryStartL, rxWidthF, ryWidthL));
      count = previousBox.pixelCount - previousBox.firstRawPixelCount + lastRawPixelCount;
      b.pixelCount = count;
      b.xStart = xStart;
      b.yStart = yStart;
      b.xBorder = xBorder;
      b.yBorder = yBorder;
      b.firstColumnPixelCount = firstColumnPixelCount;
      b.firstRawPixelCount = firstRawPixelCount;
      return b;
   }

   public int pixelMassInRectangle(int[][] imageArray, Rectangle r) {
      int count = 0;
      if (!Utils.isReleaseVersion) {
         if (r.x + r.width > imageArray.length) {
            System.out.println("out in Xs");
         }

         if (r.y + r.height > imageArray[0].length) {
            System.out.println("out in Ys");
         }
      }

      for(int x = r.x; x < r.x + r.width; ++x) {
         for(int y = r.y; y < r.y + r.height; ++y) {
            if (this.whiteForeground) {
               if (imageArray[x][y] == 255) {
                  ++count;
               }
            } else if (imageArray[x][y] == 0) {
               ++count;
            }
         }
      }

      return count;
   }

   public static int computeNumXBox2(int width, int boxmov, int boxsize) {
      double numXBox = (double)width / (double)boxmov;
      double factor = (double)boxsize / (double)boxmov;
      return (int)Math.ceil(numXBox - factor + 1.0);
   }

   public static int computeNumYBox2(int height, int boxmov, int boxsize) {
      double numYBox = (double)height / (double)boxmov;
      double factor = (double)boxsize / (double)boxmov;
      return (int)Math.ceil(numYBox - factor + 1.0);
   }

   public double average(ArrayList l) {
      double average = 0.0;

      for(int i = 0; i < l.size(); ++i) {
         average += (double)((Integer)l.get(i)).intValue();
      }

      return average / (double)l.size();
   }

   public double averageDouble(ArrayList<Double> l) {
      double average = 0.0;

      for(int i = 0; i < l.size(); ++i) {
         average += l.get(i);
      }

      return average / (double)l.size();
   }

   public double stdev2(ArrayList l, double average) {
      double stdev = 0.0;

      for(int i = 0; i < l.size(); ++i) {
         stdev += Math.pow((double)((Integer)l.get(i)).intValue() - average, 2.0);
      }

      return Math.sqrt(1.0 / (double)l.size() * stdev);
   }

   public double stdev(ArrayList l) {
      double average = this.average(l);
      double stdev = 0.0;

      for(int i = 0; i < l.size(); ++i) {
         stdev += Math.pow((double)((Integer)l.get(i)).intValue() - average, 2.0);
      }

      return Math.sqrt(1.0 / (double)l.size() * stdev);
   }

   private double lacunarity(ArrayList l) {
      return 0.0 + Math.pow(this.stdev(l) / this.average(l), 2.0);
   }

   private double onePlusLacunarity(ArrayList l) {
      return 1.0 + Math.pow(this.stdev(l) / this.average(l), 2.0);
   }

   public ArrayList<Double> getEl3() {
      return this.Elamda3;
   }

   public ArrayList<Double> getFl3() {
      return this.Flamda3;
   }

   public ArrayList<Double> getEoneplusl3() {
      return this.Eonepluslamda3;
   }

   public ArrayList<Double> getFoneplusl3() {
      return this.Fonepluslamda3;
   }

   public int[] getBoxes() {
      return this.boxS;
   }

   public double getEl3Slope() {
      ArrayList<Double> alEl = this.getEoneplusl3();
      int[] _boxes = this.getBoxes();
      double[] el = new double[alEl.size()];
      double[] boxes = new double[_boxes.length];

      for(int i = 0; i < el.length; ++i) {
         el[i] = Math.log(alEl.get(i));
         boxes[i] = Math.log((double)_boxes[i]);
      }

      return findLinearRegressionFactor(boxes, el);
   }

   public double getFl3Slope() {
      ArrayList<Double> alFl = this.getFoneplusl3();
      int[] _boxes = this.getBoxes();
      double[] fl = new double[alFl.size()];
      double[] boxes = new double[_boxes.length];

      for(int i = 0; i < fl.length; ++i) {
         fl[i] = Math.log(alFl.get(i));
         boxes[i] = Math.log((double)_boxes[i]);
      }

      return findLinearRegressionFactor(boxes, fl);
   }

   public double getMeanEl() {
      return this.averageDouble(this.getEl3());
   }

   public double getMeanFl() {
      return this.averageDouble(this.getFl3());
   }

   public double getMedialFLacunarity() {
      double halfSmallDimension = (double)(this.smallDimension / 2);
      double min = Double.MAX_VALUE;
      int medialBox = 0;
      double diff = Double.MAX_VALUE;

      for(int i = 0; i < this.boxS.length; ++i) {
         diff = Math.abs((double)this.boxS[i] - halfSmallDimension);
         if (diff < min) {
            min = diff;
            medialBox = i;
         }
      }

      return this.getFl3().get(medialBox);
   }

   public double getMedialELacunarity() {
      double halfSmallDimension = (double)(this.smallDimension / 2);
      double min = Double.MAX_VALUE;
      int medialBox = 0;
      double diff = Double.MAX_VALUE;

      for(int i = 0; i < this.boxS.length; ++i) {
         diff = Math.abs((double)this.boxS[i] - halfSmallDimension);
         if (diff < min) {
            min = diff;
            medialBox = i;
         }
      }

      return this.getEl3().get(medialBox);
   }

   static double findLinearRegressionFactor(double[] xData, double[] yData) {
      double[] params = new double[3];
      doLinearFit(xData, yData, params);
      return params[1];
   }

   /** Determine sum of squared residuals with linear regression.
   * The sum of squared residuals is written to the array element with index 'numParams',
   * the offset and factor params (if any) are written to their proper positions in the
   * params array */
   static void doLinearFit(double[] xData, double[] yData, double[] params) {
      double sumX=0, sumX2=0, sumXY=0; // sums for regression; here 'x' are function values
      double sumY=0, sumY2=0;          // only calculated for 'slope', otherwise we use the values calculated already
      final int numPoints = xData.length;

      for (int i=0; i<numPoints; i++) {
         double x = xData[i];
         double y = yData[i];
         sumX += x;
         sumX2 += x*x;
         sumXY += x*y;
         sumY2 += y*y;
         sumY += y;
      }

      final double sumWeights = (double)numPoints;

      // full linear regression or offset only. Slope is named 'factor' here
      double factor = (sumXY-sumX*sumY/sumWeights)/(sumX2-sumX*sumX/sumWeights);
      if (Double.isNaN(factor) || Double.isInfinite(factor))
         factor = 0; // all 'x' values are equal, any factor (slope) will fit

      double offset = (sumY-factor*sumX)/sumWeights;

      double factorSqrSumX2 = factor*factor*sumX2;
      double offsetSqrSumWeights = sumWeights*offset*offset;
      double sumResidualsSqr = factorSqrSumX2 + offsetSqrSumWeights + sumY2 + 2*factor*offset*sumX - 2*factor*sumXY - 2*offset*sumY;

      // check for accuracy problem: large difference of small numbers?
      // Don't report unrealistic or even negative values, otherwise minimization could lead
      // into parameters where we have a numeric problem
      sumResidualsSqr = Math.max(sumResidualsSqr, 2e-15*(factorSqrSumX2 + offsetSqrSumWeights + sumY2));

      params[0] = offset;
      params[1] = factor;
      params[2] = sumResidualsSqr;
   }

   public class PointCompareX implements Comparator<Point> {
      public int compare(Point a, Point b) {
         return a.x < b.x ? -1 : (a.x == b.x ? 0 : 1);
      }
   }

   public class PointCompareY implements Comparator<Point> {
      public int compare(Point a, Point b) {
         return a.y < b.y ? -1 : (a.y == b.y ? 0 : 1);
      }
   }
}
