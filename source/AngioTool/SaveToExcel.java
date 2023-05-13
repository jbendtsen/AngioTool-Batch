package AngioTool;

import Utils.Utils;
import ij.Prefs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.JOptionPane;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/** @deprecated */
public class SaveToExcel {
   private FileOutputStream out;
   private HSSFWorkbook wb;
   private HSSFSheet s;
   private HSSFRow r;
   private HSSFCellStyle headingCellStyle;
   private HSSFCellStyle plainCellStyle;
   private String filePath = null;
   private boolean _workBookChoice;
   private String workSheetName = "Results";
   private static final String FILE_SEPARATOR = System.getProperty("file.separator");
   private static String DEFAULT_PATH = Prefs.getHomeDir() + FILE_SEPARATOR;
   private Date today;
   private String dateOut;
   private DateFormat dateFormatter;
   private DateFormat timeFormatter;
   private String timeOut;
   private boolean areHeadingsWritten = false;
   private boolean excelFileExists = false;
   String fileName;

   public SaveToExcel(String path, boolean workBookChoice) {
      this.dateFormatter = DateFormat.getDateInstance(2, new Locale("en", "US"));
      this.timeFormatter = DateFormat.getTimeInstance(2, new Locale("en", "US"));
      this.today = new Date();
      this.dateOut = this.dateFormatter.format(this.today);
      this.timeOut = this.timeFormatter.format(this.today);
      this.excelFileExists = this.checkFile(path);
      if (!Utils.isReleaseVersion) {
         System.out.println("excelFileExists= " + this.excelFileExists);
      }

      this.setFilePath(path);
      this.setWorkBookChoice(workBookChoice);
   }

   public SaveToExcel() {
      this.dateFormatter = DateFormat.getDateInstance(2, new Locale("en", "US"));
      this.timeFormatter = DateFormat.getTimeInstance(2, new Locale("en", "US"));
      this.today = new Date();
      this.dateOut = this.dateFormatter.format(this.today);
      this.timeOut = this.timeFormatter.format(this.today);
      this.setWorkBookChoice(true);
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }

   public void setFilePath(String path) {
      if (!path.endsWith(".xls")) {
         path = path + ".xls";
      }

      this.filePath = path;
   }

   public void setWorkBookChoice(boolean workBookChoice) {
      this._workBookChoice = workBookChoice;
   }

   private boolean initializeExcel2() {
      boolean result = false;
      result = this.initializeHSSF();
      this.headingCellStyle = this.headingCellStyle();
      this.plainCellStyle = this.plainCellStyle();

      try {
         this.out = new FileOutputStream(this.filePath);
      } catch (IOException var3) {
      }

      return result;
   }

   private boolean initializeHSSF() {
      if (this._workBookChoice) {
         if (this.checkFile(this.filePath)) {
            try {
               POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(this.filePath));
               this.wb = new HSSFWorkbook(fs);
            } catch (IOException var2) {
            }

            this.s = this.wb.getSheetAt(0);
         } else {
            this.wb = new HSSFWorkbook();
            this.s = this.wb.createSheet(this.workSheetName);
         }
      }

      return this.wb != null && this.s != null;
   }

   public void writeResultsToExcel(Results results) {
      this.today = new Date();
      this.dateOut = this.dateFormatter.format(this.today);
      this.timeOut = this.timeFormatter.format(this.today);
      if (this.filePath == null) {
         this.filePath = results.getImageFilePath();
         this.setFilePath(this.filePath + "Results " + System.currentTimeMillis());
      }

      if (this.initializeExcel2()) {
         try {
            if (!this.areHeadingsWritten) {
               this.writeHeadingAnalysisInfo(results);
            }

            this.r = this.s.createRow((short)(this.s.getLastRowNum() + 1));
            this.createCell(this.r.getLastCellNum(), results.image.getName(), this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), this.dateOut, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), this.timeOut, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.image.getAbsolutePath(), this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.thresholdLow, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.thresholdHigh, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.getSigmas(), this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.removeSmallParticles, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.fillHoles, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.LinearScalingFactor, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), "", this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.allantoisMMArea, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.vesselMMArea, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.vesselPercentageArea, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.totalNJunctions, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.JunctionsPerScaledArea, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.totalLength, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.averageBranchLength, this.plainCellStyle);
            this.createCell(this.r.getLastCellNum(), results.totalNEndPoints, this.plainCellStyle);
            if (!Utils.isReleaseVersion) {
               this.createCell(this.r.getLastCellNum(), results.averageVesselDiameter, this.plainCellStyle);
            }

            if (results.computeLacunarity) {
               if (!Utils.isReleaseVersion) {
                  this.createCell(this.r.getLastCellNum(), results.ELacunarity, this.plainCellStyle);
                  this.createCell(this.r.getLastCellNum(), results.ELacunaritySlope, this.plainCellStyle);
                  this.createCell(this.r.getLastCellNum(), results.FLacuanrity, this.plainCellStyle);
                  this.createCell(this.r.getLastCellNum(), results.FLacunaritySlope, this.plainCellStyle);
                  this.createCell(this.r.getLastCellNum(), results.meanFl, this.plainCellStyle);
               }

               this.createCell(this.r.getLastCellNum(), results.meanEl, this.plainCellStyle);
            }
         } catch (Exception var4) {
         }

         try {
            this.out = new FileOutputStream(this.filePath);
            this.wb.write(this.out);
         } catch (IOException var3) {
            JOptionPane.showMessageDialog(
               JOptionPane.getRootFrame(),
               "Sorry, I could not write your results\nThe file " + this.filePath + " is being used by another application",
               "Error",
               0
            );
            var3.printStackTrace();
         }
      }
   }

   private int writeHeadingAnalysisInfo(Results results) {
      this.s.setColumnWidth(0, 8000);
      this.r = this.s.createRow(0);
      this.createCell((short)(this.r.getLastCellNum() + 1), "AngioTool v 0.6a (02.18.14)", this.headingCellStyle);
      this.r = this.s.createRow((short)this.s.getLastRowNum() + 2);
      this.r = this.s.createRow((short)this.s.getLastRowNum() + 1);
      this.createCell(this.r.getLastCellNum() + 1, "Image Name", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Date", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Time", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Image Location", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Low Threshold", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "High Threshold", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Vessel Thickness", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Small Particles", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Fill Holes", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Scaling factor", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "", this.plainCellStyle);
      this.createCell(this.r.getLastCellNum(), "Explant area", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Vessels area", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Vessels percentage area", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Total Number of Junctions", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Junctions density", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Total Vessels Length", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Average Vessels Length", this.headingCellStyle);
      this.createCell(this.r.getLastCellNum(), "Total Number of End Points", this.headingCellStyle);
      if (!Utils.isReleaseVersion) {
         this.createCell(this.r.getLastCellNum(), "Average Vessel diameter", this.headingCellStyle);
      }

      if (results.computeLacunarity) {
         if (!Utils.isReleaseVersion) {
            this.createCell(this.r.getLastCellNum(), "Elacunarity", this.headingCellStyle);
            this.createCell(this.r.getLastCellNum(), "Elacunarity Slope", this.headingCellStyle);
            this.createCell(this.r.getLastCellNum(), "Flacunarity", this.headingCellStyle);
            this.createCell(this.r.getLastCellNum(), "Flacunarity Slope", this.headingCellStyle);
            this.createCell(this.r.getLastCellNum(), "Mean F Lacunarity", this.headingCellStyle);
         }

         this.createCell(this.r.getLastCellNum(), "Mean E Lacunarity", this.headingCellStyle);
      }

      this.areHeadingsWritten = true;
      return this.s.getLastRowNum();
   }

   private HSSFCell createCell(int column, Object obj, HSSFCellStyle cellStyle) {
      if (column < 0) {
         column = 0;
      }

      HSSFCell cell = this.r.createCell(column);
      if (obj instanceof String) {
         cell.setCellValue(new HSSFRichTextString((String)obj));
      } else if (obj instanceof Double) {
         cell.setCellValue((Double)obj);
      } else if (obj instanceof Integer) {
         cell.setCellValue((double)((Integer)obj).intValue());
      } else if (obj instanceof Long) {
         cell.setCellValue((double)((Long)obj).longValue());
      }

      cell.setCellStyle(cellStyle);
      return cell;
   }

   private boolean checkFile(String path) {
      return new File(path).isFile();
   }

   private HSSFCellStyle headingCellStyle() {
      HSSFFont font = this.wb.createFont();
      font.setBoldweight((short)700);
      HSSFCellStyle style = this.wb.createCellStyle();
      style.setFont(font);
      style.setAlignment((short)2);
      return style;
   }

   private HSSFCellStyle plainCellStyle() {
      HSSFFont font = this.wb.createFont();
      font.setBoldweight((short)1);
      HSSFCellStyle style = this.wb.createCellStyle();
      style.setFont(font);
      style.setAlignment((short)1);
      return style;
   }
}
