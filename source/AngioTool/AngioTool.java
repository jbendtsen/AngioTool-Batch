package AngioTool;

import GUI.AngioToolGUI;
import Utils.Utils;
import ij.IJ;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import javax.swing.ImageIcon;

public class AngioTool {
   public static AngioToolGUI angioToolGUI;

   AngioTool() {
      this.getSystemInfo();
      String err1 = ATPreferences.load(this, null);
      if (err1 != null) {
         System.err.println("err1=" + err1);
      }

      ATPreferences.InitVariables();
      angioToolGUI = new AngioToolGUI();
      angioToolGUI.setLocation((Point)ATPreferences.atLoc.value);
      angioToolGUI.setVisible(true);
   }

   public void getSystemInfo() {
      Utils.ATClassCanonicalName = this.getClass().getCanonicalName();
      Utils.osName = System.getProperty("os.name");
      Utils.osArch = System.getProperty("os.arch");
      Utils.osVersion = System.getProperty("os.version");
      Utils.javaVersion = System.getProperty("java.version");
      Utils.javaVmName = System.getProperty("java.vm.name");
      Utils.javaVmVersion = System.getProperty("java.vm.version");
      Utils.ATDir = System.getProperty("user.dir");
      Utils.ATDir = Utils.string2path(Utils.ATDir);
      URL url = AngioToolGUI.class.getProtectionDomain().getCodeSource().getLocation();
      Utils.prefsDir = url.toString();
      Utils.isInternetActive = new ReachableTest().test();

      try {
         InetAddress addr = InetAddress.getLocalHost();
         Utils.myIP = InetAddress.getLocalHost();
         Utils.myIPAddr = addr.getHostAddress();
         Utils.myIPHostName = addr.getHostName();
      } catch (UnknownHostException var3) {
      }

      Utils.ijVersion = IJ.getVersion();
      Utils.screenDim = Toolkit.getDefaultToolkit().getScreenSize();
      Class at = this.getClass();
      Utils.ATIcon = this.createImageIcon(at, "/images/ATIcon20 64x64.gif");
      Utils.ATOpenImage = this.createImageIcon(at, "/images/OpenImages642.png");
      Utils.ATRunAnalysis = this.createImageIcon(at, "/images/RunAnalysis64-2.png");
      Utils.ATExit = this.createImageIcon(at, "/images/Close64.png");
      Utils.ATHelp = this.createImageIcon(at, "/images/help64.png");
      Utils.ATExcel = this.createImageIcon(at, "/images/Excel64.png");
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
}
