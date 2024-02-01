package Utils;

import AngioTool.AngioTool;
import AngioTool.PolygonPlus;
import AngioTool.ThresholdToSelection;
import Batch.DoubleBufferPool;
import Batch.ISliceCompute;
import Batch.IntVector;
import features.Tubeness;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.Wand;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

public class Utils {
   //public static final String URL = "https://ccrod.cancer.gov/confluence/";
   //public static final String Updates_URL = "https://ccrod.cancer.gov/confluence/display/ROB2/updates@232014";
   public static boolean isReleaseVersion = true;
   public static String osName;
   public static String osArch;
   public static String osVersion;
   public static String javaVersion;
   public static String javaVmVersion;
   public static String javaVmName;
   //public static String ijVersion;
   public static String javamailVersion;
   public static String poiVersion;
   public static final String LOOKANDFEEL = "System";
   public static final String THEME = "Test";
   public static Dimension screenDim;
   public static String ATDir;
   public static String prefsDir;
   public static String currentDir;
   public static String resultsPath;
   public static String ATClassCanonicalName;
   public static ImageIcon ATIcon;
   public static ImageIcon ATOpenImage;
   public static ImageIcon ATRunAnalysis;
   public static ImageIcon ATBatch;
   public static ImageIcon ATExit;
   public static ImageIcon ATHelp;
   public static ImageIcon ATExcel;
   public static ImageIcon ATOpenImageSmall;
   public static ImageIcon ATExcelSmall;
   public static final String jpeg = "jpeg";
   public static final String jpg = "jpg";
   public static final String gif = "gif";
   public static final String tiff = "tiff";
   public static final String TIFF = "TIFF";
   public static final String tif = "tif";
   public static final String png = "png";
   public static final String bmp = "bmp";
   public static final String xls = "xls";
   public static final String xlsx = "xlsx";

   public static double computeMedianThickness(IntVector slabList, ImagePlus distanceMap) {
      int nPoints = slabList.size / 3;
      double[] vesselThickness = DoubleBufferPool.acquireAsIs(nPoints);
      ImageProcessor distanceMapProcessor = distanceMap.getProcessor();

      for (int i = 0; i < slabList.size; i += 3) {
         int x = slabList.buf[i];
         int y = slabList.buf[i+1];
         vesselThickness[i/3] = distanceMapProcessor.getPixelValue(x, y) * 2.0F;
      }

      Arrays.sort(vesselThickness, 0, nPoints);
      int middle = nPoints / 2;

      double thickness = nPoints % 2 == 1 ?
         vesselThickness[middle] :
         (vesselThickness[middle - 1] + vesselThickness[middle]) / 2.0;

      DoubleBufferPool.release(vesselThickness);
      return thickness;
   }

   /*
   public static double computeMedianThickness(Graph[] graph, ImagePlus distanceMap) {
      ArrayList<Float> vesselThickness = computeTickness(graph, distanceMap);
      Collections.sort(vesselThickness);
      int middle = vesselThickness.size() / 2;
      return vesselThickness.size() % 2 == 1
         ? (double)vesselThickness.get(middle).floatValue()
         : (double)(vesselThickness.get(middle - 1) + vesselThickness.get(middle)) / 2.0;
   }

   public static ImageProcessor skeletonize(ImageProcessor ip, String algorithm) {
      if (ip.isBinary()) {
         ImagePlus iplusThresholded = new ImagePlus("iplusThresholded Kike", ip);
         if (algorithm.equals("ImageJ")) {
            ByteProcessor ipThresholded2 = (ByteProcessor)iplusThresholded.getProcessor();
            ipThresholded2.invert();
            ipThresholded2.skeletonize();
         } else if (algorithm.equals("itk")) {
            Skeletonize3D s3d = new Skeletonize3D();
            s3d.setup("", iplusThresholded);
            return s3d.run(ip);
         }
      }

      return null;
   }

   private static ArrayList<Float> computeTickness(Graph[] graph, ImagePlus distanceMap) {
      ArrayList<Float> vesselThickness = new ArrayList<>();
      ImageProcessor distanceMapProcessor = distanceMap.getProcessor();

      for(int g = 0; g < graph.length; ++g) {
         ArrayList<Edge> edges = graph[g].getEdges();

         for(int e = 0; e < edges.size(); ++e) {
            Edge edge = edges.get(e);
            ArrayList<Point> points = edge.getSlabs();

            for(int p1 = 0; p1 < points.size(); ++p1) {
               vesselThickness.add(distanceMapProcessor.getPixelValue(points.get(p1).x, points.get(p1).y) * 2.0F);
            }
         }
      }

      return vesselThickness;
   }
   
   public static ArrayList<Point> computeActualJunctions(ArrayList<Point> jv) {
      ArrayList<Point> removed = new ArrayList<>();

      for(int i = 0; i < jv.size(); ++i) {
         Point jv1 = jv.get(i);

         for(int ii = 0; ii < jv.size(); ++ii) {
            Point jv2 = jv.get(ii);
            if (isNeighbor(jv1, jv2)) {
               removed.add(jv2);
               jv.remove(ii);
            }
         }
      }

      return removed;
   }

   private static boolean isNeighbor(Point p1, Point p2) {
      return p1.x == p2.x - 1 && p1.y == p2.y - 1
         || p1.x == p2.x - 1 && p1.y == p2.y
         || p1.x == p2.x - 1 && p1.y == p2.y + 1
         || p1.x == p2.x && p1.y == p2.y - 1
         || p1.x == p2.x && p1.y == p2.y + 1
         || p1.x == p2.x + 1 && p1.y == p2.y - 1
         || p1.x == p2.x + 1 && p1.y == p2.y
         || p1.x == p2.x + 1 && p1.y == p2.y + 1;
   }
   */

   public static String getSystemInfo() {
      Runtime runtime = Runtime.getRuntime();
      String str = AngioTool.VERSION + "\nOperative system:\t"
         + System.getProperty("os.name")
         + "\nNumber of Processor:\t"
         + runtime.availableProcessors()
         + "\n";
      NumberFormat format = NumberFormat.getInstance();
      StringBuilder sb = new StringBuilder();
      long maxMemory = runtime.maxMemory();
      long allocatedMemory = runtime.totalMemory();
      long freeMemory = runtime.freeMemory();
      sb.append("Free memory: " + format.format(freeMemory / 1024L) + " kb\n");
      sb.append("Allocated memory: " + format.format(allocatedMemory / 1024L) + " kb\n");
      sb.append("Max memory: " + format.format(maxMemory / 1024L) + " kb\n");
      sb.append("Total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024L) + " kb\n");
      sb.append("\n\n");
      str = str + sb;
      str = str + "******* System Properties *******\n";
      Properties pr = System.getProperties();

      //TreeSet<String> keys = new TreeSet<>((Set<String>));
      for(Object key : pr.keySet()) {
         str = str + "" + key + "=" + pr.get(key) + "\n";
      }

      return str;
   }

   public static int getAnInt(String str) {
      int n = 0;
      int len = str.length();

      for (int i = 0; i < len; i++) {
         int c = str.codePointAt(i);
         if (c >= 0x30 && c <= 0x39)
            n = n * 10 + (c - 0x30);
         else
            break;
      }

      return n;
   }

   public static int[] getSomeInts(String str) {
      IntVector numbers = new IntVector();
      boolean wasNum = false;
      boolean isNeg = false;
      int n = 0;
      int len = str.length();

      for (int i = 0; i < len; i++) {
         int c = str.codePointAt(i);
         if (c >= 0x30 && c <= 0x39) {
            n = n * 10 + (c - 0x30);
            wasNum = true;
         }
         else if (!wasNum && c == '-') {
            isNeg = true;
         }
         else {
            if (wasNum)
               numbers.add(isNeg ? -n : n);
            n = 0;
            wasNum = false;
            isNeg = false;
         }
      }
      if (wasNum)
         numbers.add(isNeg ? -n : n);

      return numbers.copy();
   }

   public static double[] getSomeDoubles(String str) {
      IntVector numbers = new IntVector();
      boolean wasNum = false;
      boolean isNeg = false;
      int mode = 0;
      int[] nums = new int[3];
      int len = str.length();

      for (int i = 0; i < len; i++) {
         int c = str.codePointAt(i);
         if (c >= 0x30 && c <= 0x39) {
            if (nums[mode] <= 214748363)
                nums[mode] = nums[mode] * 10 + (c - 0x30);
            wasNum = true;
         }
         else if (!wasNum && c == '-') {
            isNeg = true;
         }
         else {
            if (wasNum) {
               nums[mode] = nums[mode] * 2 + (isNeg ? 1 : 0);
               isNeg = false;
               if (mode >= 2 || (mode == 1 && c != 'e' && c != 'E') || (mode == 0 && c != '.')) {
                  numbers.add(nums);
                  nums[0] = 0;
                  nums[1] = 0;
                  nums[2] = 0;
                  mode = 0;
               }
               else {
                  mode++;
               }
            }
            wasNum = false;
         }
      }
      if (wasNum) {
         nums[mode] = nums[mode] * 2 + (isNeg ? 1 : 0);
         numbers.add(nums);
      }

      double[] values = new double[numbers.size / 3];
      for (int i = 0; i < numbers.size-2; i += 3) {
         boolean isNegValue = ((numbers.buf[i] | numbers.buf[i+1]) & 1) != 0;
         boolean isNegExp = (numbers.buf[i+2] & 1) != 0;

         int frac = numbers.buf[i+1] >> 1;
         int f = frac;
         int fDigits = 0;
         boolean seenNonZero = false;
         while (f > 0) {
            if (f % 10 != 0)
               seenNonZero = true;
            if (!seenNonZero)
               frac /= 10;
            else
               fDigits++;
            f /= 10;
         }

         double v = (double)(numbers.buf[i] >> 1);
         v += (double)frac * Math.pow(10.0, -fDigits);
         v *= isNegValue ? -1.0 : 1.0;

         double exp = (double)(numbers.buf[i+2] >> 1);
         v *= Math.pow(10.0, isNegExp ? -exp : exp);

         values[i/3] = v;
      }

      return values;
   }

   public static String formatDoubleArray(double[] array) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < array.length; i++) {
         if (i != 0)
            sb.append(", ");
         sb.append(formatDouble(array[i]));
      }
      return sb.toString();
   }

   public static String formatDouble(double value) {
      String str = "" + value;
      if (str.endsWith(".0"))
         str = str.substring(0, str.length() - 2);
      return str;
   }

   public static boolean hasAnyFileExtension(File f) {
      return f.getName().contains(".");
   }

   public static String[] splitPaths(String blob, char charSplit, char charEscape) {
      ArrayList<String> paths = new ArrayList<>();
      int len = blob.length();
      int start = 0;
      char prev = '\0';
      for (int i = 0; i < len; i++) {
         char c = blob.charAt(i);
         if (c == charSplit && prev != charEscape) {
            paths.add(blob.substring(start, i));
            start = i+1;
         }
      }
      if (start != len)
         paths.add(blob.substring(start, len));
      return paths.toArray(new String[0]);
   }

   public static String decideBackupFileName(String absPath, String ext) {
      int lastDot = absPath.lastIndexOf('.');
      String path = (lastDot > 0 && (lastDot > absPath.lastIndexOf('/') || lastDot > absPath.lastIndexOf('\\'))) ?
         absPath.substring(0, lastDot) :
         absPath;

      ext = ext.charAt(0) == '.' ? ext.substring(1) : ext;
      String newPath = path + ".bak." + ext;
      int counter = 1;
      while (new File(newPath).exists())
         newPath = path + ".bak" + (++counter) + "." + ext;

      return newPath;
   }

   public static void setNewFontSizeOn(JComponent ui, int newSize) {
      Font font = ui.getFont();
      ui.setFont(new Font(font.getName(), font.getStyle(), newSize));
   }

   public static void showDialogBox(String title, String message) {
      JOptionPane.showMessageDialog(
         JOptionPane.getRootFrame(),
         message,
         title,
         0
      );
   }

   public static void showExceptionInDialogBox(Throwable t) {
      if (t == null)
         return;

      String name = t.getClass().getName();
      String message = t.getMessage();

      StackTraceElement[] st = t.getStackTrace();
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < st.length; i++) {
         String className = st[i].getClassName();
         if (!className.startsWith("java.") && !className.startsWith("javax.")) {
            sb.append("\n");
            sb.append(st[i].toString());
         }
      }

      String exSource = sb.toString();
      if (message == null || message.length() == 0)
         message = exSource;
      else
         message += exSource;

      showDialogBox(name, message);
   }

   public static boolean checkJavaVersion(int Major, int minor, int point) {
      /*
      String version = System.getProperty("java.version");
      int M = Integer.parseInt(version.substring(0, 1));
      int m = Integer.parseInt(version.substring(2, 3));
      int p = Integer.parseInt(version.substring(4, 5));
      if (M < Major || m < minor || p < point) {
         JOptionPane.showMessageDialog(
            JOptionPane.getRootFrame(),
            "JRE "
               + Major
               + "."
               + minor
               + "."
               + point
               + " or higher is required to run AngioTool. "
               + "Download the last JRE version from http://dlc.sun.com.edgesuite.net/jdk7/binaries/index.html\n"
               + "",
            "Error",
            0
         );
         System.exit(0);
      }
      */

      return true;
   }

   /*
   public static boolean checkImageJVersion(int Major, int minor, String character) {
      String version = IJ.getVersion();
      int M = Integer.parseInt(version.substring(0, 1));
      int m = Integer.parseInt(version.substring(2, 4));
      String p = version.substring(4, 5);
      if (M < Major || m < minor) {
         JOptionPane.showMessageDialog(
            JOptionPane.getRootFrame(),
            "ImageJ "
               + Major
               + "."
               + minor
               + ""
               + character
               + " or higher is required to run AngioTool. "
               + "Download the last ImageJ versio from http://rsbweb.nih.gov/ij/index.html",
            "Error",
            0
         );
         System.exit(0);
      }

      return true;
   }
   */

   public static long thresholdedPixelArea(ImageProcessor ip) {
      if (!ip.isBinary()) {
         return Long.MIN_VALUE;
      } else {
         long countArea = 0L;

         for(int i = 0; i < ip.getWidth(); ++i) {
            for(int ii = 0; ii < ip.getHeight(); ++ii) {
               if (ip.getPixel(i, ii) == 255) {
                  ++countArea;
               }
            }
         }

         return countArea;
      }
   }

   public int thresholdedPixelArea(ImageProcessor ip, int foregroundColor) {
      if (!ip.isBinary()) {
         return Integer.MIN_VALUE;
      } else {
         int countArea = 0;

         for(int i = 0; i < ip.getWidth(); ++i) {
            for(int ii = 0; ii < ip.getHeight(); ++ii) {
               if (ip.getPixel(ii, ii) == foregroundColor) {
                  ++countArea;
               }
            }
         }

         return countArea;
      }
   }

   public static PolygonPlus computeConvexHull(ImageProcessor thresholded) {
      ImageProcessor ipConvex = thresholded.duplicate();
      Convex_Hull_Plus chp = new Convex_Hull_Plus();
      chp.run(ipConvex);
      PolygonRoi convexHullPolygonRoi = chp.getConvexHullPolygonRoi();
      return new PolygonPlus(convexHullPolygonRoi.getPolygon());
   }

   public static int roundIntegerToNearestUpperTenth(int a) {
      int remainder = a % 10;

      while(remainder != 0) {
         remainder = ++a % 10;
      }

      return a;
   }

   public static ImageProcessor addSigma(double[] sigmas, ImageProcessor ipOriginal, ImageProcessor ip) {
      Tubeness t = new Tubeness();
      ImagePlus iplus = t.runTubeness(new ImagePlus("", ipOriginal), 100, sigmas, false);
      ImageProcessor ip2 = iplus.getProcessor();
      ip2.copyBits(ip, 0, 0, 13);
      return ip2;
   }

   public static ImageProcessor addSigma(double[] sigmas, ImageProcessor ipOriginal, ImageProcessor ip, Tubeness t) {
      ImagePlus iplus = t.runTubeness(new ImagePlus("", ipOriginal), 100, sigmas, false);
      ImageProcessor ip2 = iplus.getProcessor();
      ip2.copyBits(ip, 0, 0, 13);
      return ip2;
   }

   public static int[] getFloatHistogram2(ImagePlus iplus) {
      int nBins = 256;
      int[] histogram = new int[nBins];
      ImageProcessor ip = iplus.getProcessor();
      float histMin = Float.MAX_VALUE;
      float histMax = Float.MIN_VALUE;

      for(int y = 0; y < iplus.getHeight(); ++y) {
         for(int x = 0; x < iplus.getWidth(); ++x) {
            float v = ip.getPixelValue(x, y);
            if (v > histMax) {
               histMax = v;
            } else if (v < histMin) {
               histMin = v;
            }
         }
      }

      double scale = (double)((float)nBins / (histMax - histMin));

      for(int y = 0; y < iplus.getHeight(); ++y) {
         for(int x = 0; x < iplus.getWidth(); ++x) {
            float v = ip.getPixelValue(x, y);
            int index = (int)(scale * (double)(v - histMin));
            if (index >= nBins) {
               index = nBins - 1;
            }

            histogram[index]++;
         }
      }

      if (!isReleaseVersion) {
         System.out.println("min= " + histMin + "\tmax= " + histMax);
      }

      return histogram;
   }

   public static int[] getFloatHistogram2(float[] data, int width, int height) {
      int nBins = 256;
      int[] histogram = new int[nBins];
      float histMin = Float.MAX_VALUE;
      float histMax = Float.MIN_VALUE;

      for(int y = 0; y < height; ++y) {
         for(int x = 0; x < width; ++x) {
            float v = data[x + width*y];
            if (v > histMax) {
               histMax = v;
            } else if (v < histMin) {
               histMin = v;
            }
         }
      }

      double scale = (double)((float)nBins / (histMax - histMin));

      for(int y = 0; y < height; ++y) {
         for(int x = 0; x < width; ++x) {
            float v = data[x + width*y];
            int index = (int)(scale * (double)(v - histMin));
            if (index >= nBins) {
               index = nBins - 1;
            }

            histogram[index]++;
         }
      }

      if (!isReleaseVersion) {
         System.out.println("min= " + histMin + "\tmax= " + histMax);
      }

      return histogram;
   }

   public static ShapeRoi shapeRoiSplines2(ShapeRoi sr, int fraction) {
      Roi[] r = sr.getRois();
      ShapeRoi first = new ShapeRoi(new Roi(sr.getBounds()));
      ShapeRoi result = new ShapeRoi(first);

      for(int i = 0; i < r.length; ++i) {
         PolygonRoi pr = new PolygonRoi(r[i].getPolygon(), 2);
         int coordinates = pr.getNCoordinates();
         double area = new PolygonPlus(pr.getPolygon()).area();
         if (!isReleaseVersion) {
            System.out
               .println(
                  "This roi (" + i + "/" + r.length + ") has " + coordinates + " coordinates and length " + area + ". Ratio= " + (double)coordinates / area
               );
         }

         pr.fitSpline(pr.getNCoordinates() / fraction);
         result.xor(new ShapeRoi(pr));
      }

      result.xor(first);
      return result;
   }

   public static int findHistogramMax(int[] histogram) {
      double max = 0.0;
      int index = 0;

      for(int i = 0; i < histogram.length; ++i) {
         if (max < (double)histogram[i]) {
            max = (double)histogram[i];
            index = i;
         }
      }

      return index;
   }

   public static int findHistogramMax(ImageProcessor ip) {
      int[] histogram = ip.getHistogram();
      double max = 0.0;
      int index = 0;

      for(int i = 0; i < histogram.length; ++i) {
         if (max < (double)histogram[i]) {
            max = (double)histogram[i];
            index = i;
         }
      }

      return index;
   }

   // minSize and maxSize were ints
   public static void fillHoles(ImagePlus iplus, double minSize, double maxSize, double minCircularity, double maxCircularity, int color) {
      ImageProcessor result = iplus.getProcessor();
      if (!result.isBinary() && !isReleaseVersion) {
         System.err.println("fillHoles requires a binary image");
      }

      PolygonRoi[] pr = findAndAnalyzeObjects(iplus, minSize, maxSize, minCircularity, maxCircularity, result);
      if (pr != null) {
         result.setColor(color);

         for(int i = 0; i < pr.length; ++i) {
            result.fill(pr[i]);
         }

         iplus.setProcessor(result);
      }
   }

   public static ShapeRoi fillHoles2(ImagePlus iplus, int minSize, int maxSize) {
      ShapeRoi sr = (ShapeRoi)thresholdToSelection(iplus);
      Roi[] rx = sr.getRois();

      for(int i = 0; i < rx.length; ++i) {
         Rectangle r = rx[i].getBounds();
         int rArea = (int)(r.getWidth() * r.getHeight());
         if (rArea > minSize && rArea < maxSize) {
            PolygonPlus vqpol = new PolygonPlus(rx[i].getPolygon());
            int area = (int)vqpol.area();
            if (area > minSize && area < maxSize) {
               sr.or(new ShapeRoi(rx[i]));
            }
         }
      }

      return sr;
   }

   public static Roi thresholdToSelection(ImagePlus iplus) {
      if (iplus.getProcessor().getMaxThreshold() == -808080.0) {
         if (!isReleaseVersion) {
            System.err.println("In thresholdToSelection. ImagePlus " + iplus.toString() + " does not have threshold levels defined");
         }

         return null;
      } else {
         ThresholdToSelection tts = new ThresholdToSelection();
         Roi r = tts.convert(iplus.getProcessor());
         iplus.setRoi(r);
         return r;
      }
   }

   public static void thresholdFlexible(ImageProcessor ip, int minLevel, int maxLevel) {
      if (minLevel > maxLevel)
         threshold(ip, maxLevel, minLevel);
      else
         threshold(ip, minLevel, maxLevel);
   }

   public static void threshold(ImageProcessor ip, int minLevel, int maxLevel) {
      if (!isReleaseVersion) {
         System.out.println("isPseudoColorLut? " + ip.isPseudoColorLut() + "\tmilevel=" + minLevel + "\tmaxLevel=" + maxLevel);
      }

      if (!(ip instanceof ByteProcessor)) {
         throw new IllegalArgumentException("ByteProcessor required");
      } else {
         byte[] pixels = (byte[])ip.getPixels();
         int width = ip.getWidth();
         int height = ip.getHeight();

         for(int i = 0; i < width * height; ++i) {
            if ((pixels[i] & 255) > minLevel && (pixels[i] & 255) <= maxLevel) {
               pixels[i] = -1;
            } else {
               pixels[i] = 0;
            }
         }
      }
   }

   public static void selectionToThreshold(Roi r, ImagePlus iplus) {
      ImageProcessor ip = iplus.getProcessor();
      ip.setColor(255);
      ip.fill(r);
   }

   // _minSize and _maxSize were ints
   public static PolygonRoi[] findAndAnalyzeObjects(
      ImagePlus _iplus, double _minSize, double _maxSize, double _minCircularity, double _maxCircularity, ImageProcessor _ip
   ) {
      int options = 160;
      int measurements = 1;
      ResultsTable rt = new ResultsTable();
      ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, _minSize, _maxSize, _minCircularity, _maxCircularity);
      pa.analyze(new ImagePlus("findAndAnalyzeObjects", _ip), _ip);
      int count = rt.getCounter();
      if (count <= 0) {
         return null;
      } else if (rt.getValueAsDouble(0, 0) == (double)(_ip.getWidth() * _ip.getHeight())) {
         return null;
      } else {
         float[] Xstart = rt.getColumn(rt.getColumnIndex("XStart"));
         float[] Ystart = rt.getColumn(rt.getColumnIndex("YStart"));
         int DataArrayLength = Xstart.length;
         PolygonRoi[] pr = new PolygonRoi[DataArrayLength];

         for(int i = 0; i < pr.length; ++i) {
            Wand w = new Wand(_ip);
            w.autoOutline((int)Xstart[i], (int)Ystart[i], 254, 255);
            pr[i] = new PolygonRoi(w.xpoints, w.ypoints, w.npoints, 2);
         }

         return pr;
      }
   }

   // _minSize and _maxSize were ints
   public static PolygonRoi[] findAndAnalyzeObjects(ImagePlus _iplus, double _minSize, double _maxSize, ImageProcessor _ip) {
      return findAndAnalyzeObjects(_iplus, _minSize, _maxSize, 0.0, 1.0, _ip);
   }

   public static String getExtension(File f) {
      String ext = null;
      String s = f.getName();
      int i = s.lastIndexOf(46);
      if (i > 0 && i < s.length() - 1) {
         ext = s.substring(i + 1).toLowerCase();
      }

      return ext;
   }

   public static String getRuntimeMXBean() {
      RuntimeMXBean rbean = ManagementFactory.getRuntimeMXBean();
      List args2 = rbean.getInputArguments();
      String str = "";

      for(int i = 0; i < args2.size(); ++i) {
         System.out.println(args2.get(i));
         str = str + "\n" + args2.get(i);
      }

      return str;
   }

   public static String string2path(String s) {
      String path = null;
      String a = "/";
      String b = "\\";
      return s.replace(b, a);
   }

   public static String cutHex(String h) {
      return h.charAt(0) == '#' ? h.substring(1, 7) : h;
   }

   public static Color Hex2Color(String hex) {
      int r = Integer.parseInt(cutHex(hex).substring(0, 2), 16);
      int g = Integer.parseInt(cutHex(hex).substring(2, 4), 16);
      int b = Integer.parseInt(cutHex(hex).substring(4, 6), 16);
      return new Color(r, g, b);
   }

   public static String Color2Hex(Color color) {
      String rgb = Integer.toHexString(color.getRGB());
      return "#" + rgb.substring(2, rgb.length());
   }
}
