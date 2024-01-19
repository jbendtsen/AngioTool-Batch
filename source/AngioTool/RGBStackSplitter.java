package AngioTool;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.measure.Calibration;
import ij.plugin.HyperStackReducer;
import ij.process.ColorProcessor;
import ij.process.ImageStatistics;
import java.awt.image.IndexColorModel;

public class RGBStackSplitter {
   private static final int BLUE = 1;
   private static final int GREEN = 2;
   private static final int CYAN = 3;
   private static final int RED = 4;
   private static final int MAGENTA = 5;
   private static final int YELLOW = 6;
   private static int primaryColor = 0;
   private static ImagePlus imp;
   public static ImageStack red;
   public static ImageStack green;
   public static ImageStack blue;

   public static ImagePlus split(ImagePlus imp, String channel) {
      boolean keepSource = IJ.altKeyDown();
      String title = imp.getTitle();
      Calibration cal = imp.getCalibration();
      split(imp.getStack(), keepSource);
      if (!keepSource) {
         imp.unlock();
         imp.changes = false;
         imp.close();
      }

      ImageStatistics redStats = ImageStatistics.getStatistics(red.getProcessor(1), 2, cal);
      ImageStatistics greenStats = ImageStatistics.getStatistics(green.getProcessor(1), 2, cal);
      ImageStatistics blueStats = ImageStatistics.getStatistics(blue.getProcessor(1), 2, cal);
      if (redStats.mean > greenStats.mean && redStats.mean > blueStats.mean) {
         channel = "red";
      }

      if (greenStats.mean > redStats.mean && greenStats.mean > blueStats.mean) {
         channel = "green";
      }

      if (blueStats.mean > greenStats.mean && blueStats.mean > redStats.mean) {
         channel = "blue";
      }

      ImagePlus rImp = null;
      if (channel.equals("red")) {
         rImp = new ImagePlus(title, red);
         primaryColor = 4;
      } else if (channel.equals("green")) {
         rImp = new ImagePlus(title, green);
         primaryColor = 2;
      } else if (channel.equals("blue")) {
         rImp = new ImagePlus(title, blue);
         primaryColor = 1;
      }

      rImp.setCalibration(cal);
      FileInfo fi = new FileInfo();
      fi.reds = new byte[256];
      fi.greens = new byte[256];
      fi.blues = new byte[256];
      fi.lutSize = 256;
      int nColors = 0;
      nColors = primaryColor(primaryColor, fi.reds, fi.greens, fi.blues);
      IndexColorModel cm = new IndexColorModel(8, 256, fi.reds, fi.greens, fi.blues);
      rImp.getProcessor().setColorModel(cm);
      return rImp;
   }

   private static int primaryColor(int color, byte[] reds, byte[] greens, byte[] blues) {
      for(int i = 0; i < 256; ++i) {
         if ((color & 4) != 0) {
            reds[i] = (byte)i;
         }

         if ((color & 2) != 0) {
            greens[i] = (byte)i;
         }

         if ((color & 1) != 0) {
            blues[i] = (byte)i;
         }
      }

      return 256;
   }

   public void split(ImagePlus imp) {
      boolean keepSource = IJ.altKeyDown();
      String title = imp.getTitle();
      Calibration cal = imp.getCalibration();
      split(imp.getStack(), keepSource);
      if (!keepSource) {
         imp.unlock();
         imp.changes = false;
         imp.close();
      }

      ImagePlus rImp = new ImagePlus(title + " (red)", red);
      rImp.setCalibration(cal);
      ImagePlus gImp = new ImagePlus(title + " (green)", green);
      gImp.setCalibration(cal);
      if (IJ.isMacOSX()) {
         IJ.wait(500);
      }

      ImagePlus bImp = new ImagePlus(title + " (blue)", blue);
      bImp.setCalibration(cal);
   }

   public static void split(ImageStack rgb, boolean keepSource) {
      int w = rgb.getWidth();
      int h = rgb.getHeight();
      red = new ImageStack(w, h);
      green = new ImageStack(w, h);
      blue = new ImageStack(w, h);
      int slice = 1;
      int inc = keepSource ? 1 : 0;
      int n = rgb.getSize();

      for(int i = 1; i <= n; ++i) {
         IJ.showStatus(i + "/" + n);
         byte[] r = new byte[w * h];
         byte[] g = new byte[w * h];
         byte[] b = new byte[w * h];
         ColorProcessor cp = (ColorProcessor)rgb.getProcessor(slice);
         slice += inc;
         cp.getRGB(r, g, b);
         if (!keepSource) {
            rgb.deleteSlice(1);
         }

         red.addSlice(null, r);
         green.addSlice(null, g);
         blue.addSlice(null, b);
         IJ.showProgress((double)i / (double)n);
      }
   }

   void splitChannels(ImagePlus imp) {
      int width = imp.getWidth();
      int height = imp.getHeight();
      int channels = imp.getNChannels();
      int slices = imp.getNSlices();
      int frames = imp.getNFrames();
      int bitDepth = imp.getBitDepth();
      int size = slices * frames;
      HyperStackReducer reducer = new HyperStackReducer(imp);

      for(int c = 1; c <= channels; ++c) {
         ImageStack stack2 = new ImageStack(width, height, size);
         stack2.setPixels(imp.getProcessor().getPixels(), 1);
         ImagePlus imp2 = new ImagePlus("C" + c + "-" + imp.getTitle(), stack2);
         stack2.setPixels(null, 1);
         imp.setPosition(c, 1, 1);
         imp2.setDimensions(1, slices, frames);
         imp2.setCalibration(imp.getCalibration());
         reducer.reduce(imp2);
         if (imp2.getNDimensions() > 3) {
            imp2.setOpenAsHyperStack(true);
         }

         imp2.show();
      }

      imp.changes = false;
      imp.close();
   }
}
