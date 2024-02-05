package AngioTool;

import Batch.AnalyzerParameters;
import Batch.BatchUtils;
import GUI.AngioToolGUI;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;

public class AngioTool {
   public static final String VERSION = "AngioTool-Batch 0.6a r6 (30.01.24)";
   public static final String PREFS_TXT = "AT_Prefs.txt";
   public static final String BATCH_TXT = "AT_BatchPrefs.txt";
   public static AngioToolGUI angioToolGUI;

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

   AngioTool() {
      this.getSystemInfo();

      AnalyzerParameters initialParams;
      try {
         initialParams = ATPreferences.load(this, PREFS_TXT);
      }
      catch (Exception ex) {
         BatchUtils.showExceptionInDialogBox(ex);
         initialParams = AnalyzerParameters.defaults();
      }

      angioToolGUI = new AngioToolGUI(initialParams);
      //angioToolGUI.setLocation(new Point(100, 50));
      angioToolGUI.setVisible(true);
   }

   public void getSystemInfo() {
      ATClassCanonicalName = this.getClass().getCanonicalName();
      osName = System.getProperty("os.name");
      osArch = System.getProperty("os.arch");
      osVersion = System.getProperty("os.version");
      javaVersion = System.getProperty("java.version");
      javaVmName = System.getProperty("java.vm.name");
      javaVmVersion = System.getProperty("java.vm.version");
      ATDir = System.getProperty("user.dir").replace("\\", "/");
      URL url = AngioToolGUI.class.getProtectionDomain().getCodeSource().getLocation();
      prefsDir = url.toString();

      /*
      isInternetActive = new ReachableTest().test();

      try {
         InetAddress addr = InetAddress.getLocalHost();
         myIP = InetAddress.getLocalHost();
         myIPAddr = addr.getHostAddress();
         myIPHostName = addr.getHostName();
      } catch (UnknownHostException var3) {
      }
      */

      //ijVersion = IJ.getVersion();
      screenDim = Toolkit.getDefaultToolkit().getScreenSize();

      Class at = this.getClass();
      ATIcon = this.createImageIcon(at, "/images/ATIcon20 64x64.gif");
      ATOpenImage = this.createImageIcon(at, "/images/OpenImages642.png");
      ATRunAnalysis = this.createImageIcon(at, "/images/RunAnalysis64-2.png");
      ATBatch = this.createImageIcon(at, "/images/batch64.png");
      ATExit = this.createImageIcon(at, "/images/Close64.png");
      ATHelp = this.createImageIcon(at, "/images/help64.png");
      ATExcel = this.createImageIcon(at, "/images/Excel64.png");

      ATOpenImageSmall = this.createResizedIcon(ATOpenImage, 24, 24);
      ATExcelSmall = this.createResizedIcon(ATExcel, 24, 24);
   }

   protected ImageIcon createImageIcon(String path) {
      URL imgURL = this.getClass().getResource(path);
      if (imgURL != null) {
         return new ImageIcon(imgURL);
      } else {
         System.err.println("Couldn't find file: " + path);
         return null;
      }
   }

   protected ImageIcon createImageIcon(Class c, String path) {
      URL imgURL = c.getResource(path);
      if (imgURL != null) {
         return new ImageIcon(imgURL);
      } else {
         System.err.println("Couldn't find file: " + path);
         return null;
      }
   }

   static ImageIcon createResizedIcon(ImageIcon original, int w, int h) {
      return new ImageIcon(original.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT));
   }
}
