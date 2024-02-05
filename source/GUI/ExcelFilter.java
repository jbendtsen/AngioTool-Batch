package GUI;

import Batch.BatchUtils;
import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ExcelFilter extends FileFilter {
   @Override
   public boolean accept(File f) {
      if (f.isDirectory()) {
         return true;
      } else {
         String extension = BatchUtils.getExtension(f);
         if (extension != null) {
            return extension.equals("xls");
         } else {
            return false;
         }
      }
   }

   @Override
   public String getDescription() {
      return "xls";
   }
}
