package GUI;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ImageFilter extends FileFilter {
   public static final String bmp = "bmp";
   public static final String BMP = "BMP";
   public static final String dcm = "dcm";
   public static final String DCM = "DCM";
   public static final String gif = "gif";
   public static final String GIF = "GIF";
   public static final String jpeg = "jpeg";
   public static final String JPEG = "JPEG";
   public static final String jpg = "jpg";
   public static final String JPG = "JPG";
   public static final String png = "png";
   public static final String PNG = "PNG";
   public static final String tiff = "tiff";
   public static final String TIFF = "TIFF";
   public static final String tif = "tif";
   public static final String TIF = "TIF";

   @Override
   public boolean accept(File f) {
      if (f.isDirectory()) {
         return true;
      } else {
         String extension = getExtension(f);
         if (extension != null) {
            return extension.equals("dcm")
               || extension.equals("DCM")
               || extension.equals("tiff")
               || extension.equals("TIFF")
               || extension.equals("tif")
               || extension.equals("TIF")
               || extension.equals("bmp")
               || extension.equals("BMP")
               || extension.equals("gif")
               || extension.equals("GIF")
               || extension.equals("jpeg")
               || extension.equals("JPEG")
               || extension.equals("jpg")
               || extension.equals("JPG")
               || extension.equals("png")
               || extension.equals("PNG");
         } else {
            return false;
         }
      }
   }

   @Override
   public String getDescription() {
      return "bmp, dcm, gif, jpg, jpeg, png, tif, tiff";
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
}
