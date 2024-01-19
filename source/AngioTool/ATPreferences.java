package AngioTool;

import GUI.AngioToolGUI;
import Utils.Utils;
import ij.IJ;
import ij.util.Tools;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

public class ATPreferences {
   public static final String PROPS_NAME = "AT_Prefs.txt";
   public static final String PREFS_NAME = "AT_Prefs.txt";
   public static final String KEY_PREFIX = ".";
   public static ATPreferences.Setting ATCurrentDir;
   public static ATPreferences.Setting atLoc;
   public static ATPreferences.Setting atHelpLoc;
   public static ATPreferences.Setting doScaling;
   public static ATPreferences.Setting fillHoles;
   public static ATPreferences.Setting smallParticles;
   public static ATPreferences.Setting resizingFactor;
   public static ATPreferences.Setting showOverlay;
   public static ATPreferences.Setting showOutline;
   public static ATPreferences.Setting showSkeleton;
   public static ATPreferences.Setting showBranchingPoints;
   public static ATPreferences.Setting showConvexHull;
   public static ATPreferences.Setting OutlineStrokeWidth;
   public static ATPreferences.Setting SkeletonStrokeWidth;
   public static ATPreferences.Setting BranchingPointsStrokeWidth;
   public static ATPreferences.Setting ConvexHullStrokeWidth;
   public static ATPreferences.Setting OutlineColor;
   public static ATPreferences.Setting SkeletonColor;
   public static ATPreferences.Setting BranchingPointsColor;
   public static ATPreferences.Setting ConvexHullColor;
   public static ATPreferences.Setting imageResultFormat;
   public static ATPreferences.Setting computeLacunarity;
   public static ATPreferences.Setting computeThickness;
   public static ATPreferences.Setting LinearScalingFactor;
   public static ATPreferences.Setting AreaScalingFactor;
   public static ATPreferences.Setting currentSigmas;
   public static ATPreferences.Setting thresholdLow;
   public static ATPreferences.Setting thresholdHigh;
   public static ATPreferences.Setting numBoxSizes;
   public static ATPreferences.Setting minBoxSize;
   public static ATPreferences.Setting slidingX;
   public static ATPreferences.Setting slidingY;
   public static final Point defaultATLoc = new Point(100, 0);
   public static final Point defaultATHelpLoc = new Point(200, 0);
   public static final String defaultATCurrentDir = "C:/";
   public static final String defaultOutlineColor = "FFFF00";
   public static final String defaultSkeletonColor = "FF0000";
   public static final String defaultBranchingPointsColor = "0099FF";
   public static final String defaultConvexHullColor = "CCFFFF";
   public static final int defaultOutlineStrokeWidth = 1;
   public static final int defaultSkeletonStrokeWidth = 5;
   public static final int defaultBranchingPointsStrokeWidth = 8;
   public static final int defaultConvexHullStrokeWidth = 1;
   public static final String defaultImageResultFormat = "jpg";
   public static final boolean defaultShowOverlay = true;
   public static final boolean defaultShowOutline = true;
   public static final boolean defaultShowSkeleton = true;
   public static final boolean defaultShowBranchingPoints = true;
   public static final boolean defaultShowConvexHull = true;
   public static final boolean defaultDoScaling = false;
   public static final boolean defaultFillHoles = false;
   public static final boolean defaultSmallParticles = false;
   public static final double defaultResizingFactor = 1.0;
   public static final boolean defaultComputeLacunarity = true;
   public static final boolean defaultComputeThickness = true;
   public static final double defaultLinearScalingFactor = 0.0;
   public static final double defaultAreaScalingFactor = 0.0;
   public static final double defaultCurrentSigmas = 5.0;
   public static final int defaultThresholdLow = 10;
   public static final int defaultThresholdHigh = 50;
   public static String separator = System.getProperty("file.separator");
   static Properties atPrefs = new Properties();
   static Properties props = new Properties(atPrefs);
   static String imagesURL;

   public static void InitVariables() {
      atLoc = new ATPreferences.Setting(".atLoc.Point", "atLoc.Point");
      atLoc.value = getLocation(atLoc.key, defaultATLoc);
      atHelpLoc = new ATPreferences.Setting(".atHelpLoc.Point", "atHelpLoc.Point");
      atHelpLoc.value = getLocation(atHelpLoc.key, defaultATHelpLoc);
      ATCurrentDir = new ATPreferences.Setting(".currentDir.string", "currentDir.string");
      ATCurrentDir.value = getString(ATCurrentDir.key, "C:/");
      doScaling = new ATPreferences.Setting(".doScaling.boolean", "doScaling.boolean");
      doScaling.value = getBoolean(doScaling.key, false);
      fillHoles = new ATPreferences.Setting(".fillHoles.boolean", "fillHoles.boolean");
      fillHoles.value = getBoolean(fillHoles.key, false);
      smallParticles = new ATPreferences.Setting(".smallParticles.boolean", "smallParticles.boolean");
      smallParticles.value = getBoolean(smallParticles.key, false);
      resizingFactor = new ATPreferences.Setting(".resizingFactor.double", "resizingFactor.double");
      resizingFactor.value = getDouble(resizingFactor.key, 1.0);
      OutlineStrokeWidth = new ATPreferences.Setting(".OutlineStrokeWidth.int", "OutlineStrokeWidth.int");
      OutlineStrokeWidth.value = getInt(OutlineStrokeWidth.key, 1);
      SkeletonStrokeWidth = new ATPreferences.Setting(".SkeletonStrokeWidth.int", "SkeletonStrokeWidth.int");
      SkeletonStrokeWidth.value = getInt(SkeletonStrokeWidth.key, 5);
      BranchingPointsStrokeWidth = new ATPreferences.Setting(".BranchingPointsStrokeWidth.int", "BranchingPointsStrokeWidth.int");
      BranchingPointsStrokeWidth.value = getInt(BranchingPointsStrokeWidth.key, 8);
      ConvexHullStrokeWidth = new ATPreferences.Setting(".ConvexHullStrokeWidth.int", "ConvexHullStrokeWidth.int");
      ConvexHullStrokeWidth.value = getInt(ConvexHullStrokeWidth.key, 1);
      showOverlay = new ATPreferences.Setting(".showOverlay.boolean", "showOverlay.boolean");
      showOverlay.value = getBoolean(showOverlay.key, true);
      showOutline = new ATPreferences.Setting(".showOutline.boolean", "showOutline.boolean");
      showOutline.value = getBoolean(showOutline.key, true);
      showSkeleton = new ATPreferences.Setting(".showSkeleton.boolean", "showSkeleton.boolean");
      showSkeleton.value = getBoolean(showSkeleton.key, true);
      showBranchingPoints = new ATPreferences.Setting(".showBranchingPoints.boolean", "showBranchingPoints.boolean");
      showBranchingPoints.value = getBoolean(showBranchingPoints.key, true);
      showConvexHull = new ATPreferences.Setting(".showConvexHull.boolean", "showConvexHull.boolean");
      showConvexHull.value = getBoolean(showConvexHull.key, true);
      OutlineColor = new ATPreferences.Setting(".OutlineColor.Color", "OutlineColor.Color");
      OutlineColor.value = getString(OutlineColor.key, "FFFF00");
      SkeletonColor = new ATPreferences.Setting(".SkeletonColor.Color", "SkeletonColor.Color");
      SkeletonColor.value = getString(SkeletonColor.key, "FF0000");
      BranchingPointsColor = new ATPreferences.Setting(".BranchingPointsColor.Color", "BranchingPointsColor.Color");
      BranchingPointsColor.value = getString(BranchingPointsColor.key, "0099FF");
      ConvexHullColor = new ATPreferences.Setting(".ConvexHullColor.Color", "ConvexHullColor.Color");
      ConvexHullColor.value = getString(ConvexHullColor.key, "CCFFFF");
      imageResultFormat = new ATPreferences.Setting(".imageResultFormat.string", "imageResultFormat.string");
      imageResultFormat.value = getString(imageResultFormat.key, "jpg");
      computeLacunarity = new ATPreferences.Setting(".computeLacunarity.boolean", "computeLacunarity.boolean");
      computeLacunarity.value = getBoolean(computeLacunarity.key, true);
      computeThickness = new ATPreferences.Setting(".computeThickness.boolean", "computeThickness.boolean");
      computeThickness.value = getBoolean(computeThickness.key, true);
      LinearScalingFactor = new ATPreferences.Setting(".LinearScalingFactor.double", "LinearScalingFactor.double");
      LinearScalingFactor.value = getDouble(LinearScalingFactor.key, 0.0);
      AreaScalingFactor = new ATPreferences.Setting(".AreaScalingFactor.double", "AreaScalingFactor.double");
      AreaScalingFactor.value = getDouble(AreaScalingFactor.key, 0.0);
      currentSigmas = new ATPreferences.Setting(".currentSigmas.array", "currentSigmas.array");
      currentSigmas.value = getArrayList(currentSigmas.key, 5.0);
      thresholdLow = new ATPreferences.Setting(".thresholdLow.int", "thresholdLow.int");
      thresholdLow.value = getInt(thresholdLow.key, 10);
      thresholdHigh = new ATPreferences.Setting(".thresholdHigh.int", "thresholdHigh.int");
      thresholdHigh.value = getInt(thresholdHigh.key, 50);
   }

   public static String variablesToString() {
      return new String("variables values: ");
   }

   public static void setPreferences() {
      saveLocation(atLoc.key2, AngioToolGUI.getFrames()[0].getLocation());
      saveLocation(atHelpLoc.key2, AngioToolGUI.ATAboutBoxLoc);
      set(ATCurrentDir.key2, (String)ATCurrentDir.value);
      set(doScaling.key2, (boolean)doScaling.value);
      set(fillHoles.key2, (boolean)fillHoles.value);
      set(smallParticles.key2, (boolean)smallParticles.value);
      set(resizingFactor.key2, (double)resizingFactor.value);
      set(showOverlay.key2, (boolean)showOverlay.value);
      System.out.println("showOverlay " + showOverlay.value);
      set(showOutline.key2, (boolean)showOutline.value);
      set(showSkeleton.key2, (boolean)showSkeleton.value);
      set(showBranchingPoints.key2, (boolean)showBranchingPoints.value);
      set(showConvexHull.key2, (boolean)showConvexHull.value);
      set(OutlineStrokeWidth.key2, (int)OutlineStrokeWidth.value);
      set(SkeletonStrokeWidth.key2, (int)SkeletonStrokeWidth.value);
      set(BranchingPointsStrokeWidth.key2, (int)BranchingPointsStrokeWidth.value);
      set(ConvexHullStrokeWidth.key2, (int)ConvexHullStrokeWidth.value);
      set(OutlineColor.key2, (String)OutlineColor.value);
      set(SkeletonColor.key2, (String)SkeletonColor.value);
      set(BranchingPointsColor.key2, (String)BranchingPointsColor.value);
      set(ConvexHullColor.key2, (String)ConvexHullColor.value);
      set(imageResultFormat.key2, (String)imageResultFormat.value);
      set(computeLacunarity.key2, (boolean)computeLacunarity.value);
      set(computeThickness.key2, (boolean)computeThickness.value);
      set(LinearScalingFactor.key2, (double)LinearScalingFactor.value);
      set(AreaScalingFactor.key2, (double)AreaScalingFactor.value);
      set(currentSigmas.key2, (ArrayList)currentSigmas.value);
      set(thresholdLow.key2, (int)thresholdLow.value);
      set(thresholdHigh.key2, (int)thresholdHigh.value);
      savePreferences();
   }

   public static String load(Object at, Applet applet) {
      InputStream f = at.getClass().getResourceAsStream("/AT_Prefs.txt");
      if (Utils.ATDir == null) {
         Utils.ATDir = System.getProperty("user.dir");
      }

      String userHome = System.getProperty("user.home");
      if (Utils.osName.indexOf("Windows", 0) > -1) {
         Utils.prefsDir = Utils.ATDir;
      } else {
         Utils.prefsDir = userHome;
         if (IJ.isMacOSX()) {
            Utils.prefsDir = Utils.prefsDir + "/Library/Preferences";
         }
      }

      if (f == null) {
         try {
            f = new FileInputStream(Utils.ATDir + "/" + "AT_Prefs.txt");
         } catch (FileNotFoundException var6) {
            f = null;
         }
      }

      if (f == null) {
         return "AT_Prefs.txt not found in AngioTool.jar or in " + Utils.ATDir;
      } else {
         f = new BufferedInputStream(f);

         try {
            props.load(f);
            f.close();
         } catch (IOException var5) {
            return "Error loading AT_Prefs.txt";
         }

         loadPreferences();
         return null;
      }
   }

   public static String get(String key, String defaultValue) {
      String value = atPrefs.getProperty("." + key);
      return value == null ? defaultValue : value;
   }

   public static double get(String key, double defaultValue) {
      String s = atPrefs.getProperty("." + key);
      Double d = null;
      if (s != null) {
         try {
            d = Double.parseDouble(s);
         } catch (NumberFormatException var6) {
            d = null;
         }

         if (d != null) {
            return d;
         }
      }

      return defaultValue;
   }

   public static boolean get(String key, boolean defaultValue) {
      String value = atPrefs.getProperty("." + key);
      return value == null ? defaultValue : value.equals("true");
   }

   public static String getString(String key) {
      return props.getProperty(key);
   }

   public static String getString(String key, String defaultString) {
      if (props == null) {
         return defaultString;
      } else {
         String s = props.getProperty(key);
         return s == null ? defaultString : s;
      }
   }

   public static boolean getBoolean(String key, boolean defaultValue) {
      if (props == null) {
         return defaultValue;
      } else {
         String s = props.getProperty(key);
         return s == null ? defaultValue : s.equals("true");
      }
   }

   public static int getInt(String key, int defaultValue) {
      if (props == null) {
         return defaultValue;
      } else {
         String s = props.getProperty(key);
         if (s != null) {
            try {
               return Integer.decode(s);
            } catch (NumberFormatException var4) {
            }
         }

         return defaultValue;
      }
   }

   public static double getDouble(String key, double defaultValue) {
      if (props == null) {
         return defaultValue;
      } else {
         String s = props.getProperty(key);
         Double d = null;
         if (s != null) {
            try {
               d = Double.parseDouble(s);
            } catch (NumberFormatException var6) {
               d = null;
            }

            if (d != null) {
               return d;
            }
         }

         return defaultValue;
      }
   }

   public static float getFloat(String key, float defaultValue) {
      if (props == null) {
         return defaultValue;
      } else {
         String s = props.getProperty(key);
         if (s != null) {
            try {
               return (float)Integer.decode(s).intValue();
            } catch (NumberFormatException var4) {
            }
         }

         return defaultValue;
      }
   }

   public static String getColor(String key, String defaultColor) {
      return getString(key, "ff3333");
   }

   public static Color getColor(String key, Color defaultColor) {
      System.err.println("inside getcolor");
      int i = getInt(key, 2730);
      return i == 2730 ? defaultColor : new Color(i >> 16 & 0xFF, i >> 8 & 0xFF, i & 0xFF);
   }

   public static Point getLocation(String key, Point loc) {
      String value = atPrefs.getProperty(key);
      if (value == null) {
         return loc;
      } else {
         int index = value.indexOf(",");
         if (index == -1) {
            return null;
         } else {
            double xloc = Tools.parseDouble(value.substring(0, index));
            if (!Double.isNaN(xloc) && index != value.length() - 1) {
               double yloc = Tools.parseDouble(value.substring(index + 1));
               if (Double.isNaN(yloc)) {
                  return null;
               } else {
                  Point p = new Point((int)xloc, (int)yloc);
                  Dimension screen = Utils.screenDim;
                  return p.x <= screen.width - 100 && p.y <= screen.height - 40 ? p : loc;
               }
            } else {
               return null;
            }
         }
      }
   }

   public static ArrayList<Double> getArrayList(String key, double defaultValue) {
      ArrayList<Double> returnArray = new ArrayList<>();
      returnArray.add(defaultValue);
      if (props == null) {
         return returnArray;
      } else {
         String s = props.getProperty(key);
         Double d = null;
         if (s != null) {
            try {
               d = Double.parseDouble(s);
            } catch (NumberFormatException var7) {
               d = null;
            }

            if (d != null) {
            }
         }

         return returnArray;
      }
   }

   public static String getHomeDir() {
      return Utils.ATDir;
   }

   public static String getFileSeparator() {
      return separator;
   }

   static void loadPreferences() {
      String path = Utils.prefsDir + separator + "AT_Prefs.txt";
      boolean ok = loadPrefs(path);
      if (!ok && IJ.isMacOSX()) {
         path = System.getProperty("user.home") + separator + "AT_Prefs.txt";
         ok = loadPrefs(path);
         if (ok) {
            new File(path).delete();
         }
      }
   }

   static boolean loadPrefs(String path) {
      try {
         InputStream is = new BufferedInputStream(new FileInputStream(path));
         atPrefs.load(is);
         is.close();
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   public static void savePreferences() {
      try {
         Properties prefs = new Properties();
         savePluginPrefs(prefs);
         String path = Utils.prefsDir + separator + "AT_Prefs.txt";
         savePrefs(prefs, path);
      } catch (Exception var2) {
      }
   }

   static void setHomeDir(String path) {
      Utils.ATDir = path;
   }

   public static void set(String key, String text) {
      if (key.indexOf(46) < 1) {
         throw new IllegalArgumentException("Key must have a prefix");
      } else {
         atPrefs.put("." + key, text);
      }
   }

   public static void set(String key, int value) {
      set(key, "" + value);
   }

   public static void set(String key, double value) {
      set(key, "" + value);
   }

   public static void set(String key, float value) {
      set(key, "" + value);
   }

   public static void set(String key, boolean value) {
      set(key, "" + value);
   }

   public static void set(String key, Color value) {
      set(key, "" + value);
   }

   public static void set(String key, ArrayList value) {
      set(key, "" + value.toString());
   }

   static void savePluginPrefs(Properties prefs) {
      Enumeration e = atPrefs.keys();

      while(e.hasMoreElements()) {
         String key = (String)e.nextElement();
         if (key.indexOf(".") == 0) {
            prefs.put(key, escapeBackSlashes(atPrefs.getProperty(key)));
         }
      }
   }

   public static void savePrefs(Properties prefs, String path) throws IOException {
      FileOutputStream fos = new FileOutputStream(path);
      BufferedOutputStream bos = new BufferedOutputStream(fos);
      PrintWriter pw = new PrintWriter(bos);
      pw.println("# AngioTool 0.6a (02.18.14) Preferences");
      pw.println("# " + new Date());
      pw.println("");
      Enumeration e = prefs.keys();

      while(e.hasMoreElements()) {
         String key = (String)e.nextElement();
         pw.print(key);
         pw.write(61);
         pw.println((String)prefs.get(key));
      }

      pw.close();
   }

   public static void saveLocation(String key, Point loc) {
      set(key, loc.x + "," + loc.y);
   }

   static String escapeBackSlashes(String s) {
      if (s.indexOf(92) == -1) {
         return s;
      } else {
         StringBuffer sb = new StringBuffer(s.length() + 10);
         char[] chars = s.toCharArray();

         for(int i = 0; i < chars.length; ++i) {
            sb.append(chars[i]);
            if (chars[i] == '\\') {
               sb.append('\\');
            }
         }

         return sb.toString();
      }
   }

   public static class Setting {
      String key;
      String key2;
      public Object value;
      public Object oldValue;

      public Setting(String key, String key2) {
         this.key = key;
         this.key2 = key2;
      }

      public void setValue(Number value) {
         this.value = value;
      }

      public void setOldValue(Number oldValue) {
         this.oldValue = oldValue;
      }
   }
}
