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
import java.util.HashMap;
import java.util.Date;
import java.lang.reflect.Field;

public class ATPreferences {
    public static class Settings {
        public Point atLoc = new Point(100, 0);
        public Point atHelpLoc = new Point(200, 0);
        public String currentDir = "C:/";
        public String OutlineColor = "FFFF00";
        public String SkeletonColor = "FF0000";
        public String BranchingPointsColor = "0099FF";
        public String ConvexHullColor = "CCFFFF";
        public int OutlineStrokeWidth = 1;
        public int SkeletonStrokeWidth = 5;
        public int BranchingPointsStrokeWidth = 8;
        public int ConvexHullStrokeWidth = 1;
        public String imageResultFormat = "jpg";
        public boolean showOverlay = true;
        public boolean showOutline = true;
        public boolean showSkeleton = true;
        public boolean showBranchingPoints = true;
        public boolean showConvexHull = true;
        public boolean doScaling = false;
        public boolean fillHoles = false;
        public boolean smallParticles = false;
        public double resizingFactor = 1.0;
        public boolean computeLacunarity = true;
        public boolean computeThickness = true;
        public double LinearScalingFactor = 0.0;
        public double AreaScalingFactor = 0.0;
        public double currentSigmas = 5.0;
        public int thresholdLow = 10;
        public int thresholdHigh = 50;
    }

    private static final Settings defaults = new Settings();
    public static Settings settings = new Settings();

    public static String separator = System.getProperty("file.separator");

    public static void InitVariables() {

    }

    public static void setPreferences() {
        StringBuilder sb = new StringBuilder();
        sb.append("# AngioTool-Batch 0.6a (14.05.23) Preferences\n");
        sb.append("# " + new Date() + "\n\n");

        Field[] fields = settings.getClass().getDeclaredFields();
        try {
            for (Field f : fields) {
                String type = f.getType().getName();
                if (type.equals("java.lang.String")) type = "string";
                else if (type.equals("java.awt.Point")) type = "Point";

                sb.append('.');
                sb.append(f.getName());
                sb.append('.');
                sb.append(type);
                sb.append('=');
                if (type.equals("Point")) {
                    Point p = (Point)f.get(settings);
                    sb.append(p.x);
                    sb.append(',');
                    sb.append(p.y);
                }
                else {
                    sb.append(f.get(settings));
                }
                sb.append('\n');
            }
        }
        catch (IllegalAccessException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        try {
            File path = new File(Utils.prefsDir, "AT_Prefs.txt");
            FileOutputStream out = new FileOutputStream(path);
            out.write(sb.toString().getBytes());
            out.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String load(Object at, Applet applet) {
        InputStream f = at.getClass().getResourceAsStream("/AT_Prefs.txt");
        if (Utils.ATDir == null) {
            Utils.ATDir = System.getProperty("user.dir");
        }

        String userHome = System.getProperty("user.home");
        File atFolder = new File(userHome, "AngioTool-Batch");
        if (!atFolder.exists())
            atFolder.mkdir();

        /*
        if (Utils.osName.indexOf("Windows", 0) > -1) {
            Utils.prefsDir = Utils.ATDir;
        } else {
            Utils.prefsDir = userHome;
            if (IJ.isMacOSX()) {
                Utils.prefsDir = Utils.prefsDir + "/Library/Preferences";
            }
        }
        */

        Utils.prefsDir = atFolder.getAbsolutePath();

        if (f == null) {
            try {
                File prefsPath = new File(atFolder, "AT_Prefs.txt");
                f = new FileInputStream(prefsPath);
            } catch (FileNotFoundException var6) {
                f = null;
            }
        }

        if (f == null) {
            return "AT_Prefs.txt not found in AngioTool.jar or in " + Utils.prefsDir;
        }
        else {
            StringBuilder sb = new StringBuilder();
            byte[] buf = new byte[512];

            try {
                while (true) {
                    int res = f.read(buf);
                    if (res <= 0)
                        break;
                    sb.append(new String(buf, 0, res));
                }
            }
            catch (IOException ex) {
                String msg = ex.getMessage();
                return msg != null ? msg : "Failed to read from AT_Prefs.txt";
            }

            populatePreferences(sb.toString());
        }

        return null;
    }

    public static void populatePreferences(String text) {
        HashMap<String, Field> map = new HashMap<>();
        Field[] fields = settings.getClass().getDeclaredFields();
        for (Field f : fields)
            map.put(f.getName(), f);

        String[] lines = text.split("\n");
        try {
            for (String l : lines) {
                if (l.length() < 2 || l.charAt(0) == '#')
                    continue;

                int nameStartIdx = l.charAt(0) == '.' ? 1 : 0;
                int typeStartIdx = l.indexOf('.', nameStartIdx) + 1;
                int valueStartIdx = l.indexOf('=') + 1;

                if (nameStartIdx < 0 || typeStartIdx <= 0 || valueStartIdx <= 0)
                    continue;

                String name = l.substring(1, typeStartIdx - 1);
                String type = l.substring(typeStartIdx, valueStartIdx - 1);
                String valueStr = l.substring(valueStartIdx);

                Field f = map.get(name);
                if (f != null) {
                    Object value;

                    if (type.equals("Point"))
                        value = parsePoint(valueStr);
                    else if (type.equals("boolean") || type.equals("bool"))
                        value = parseBool(valueStr);
                    else if (type.equals("int"))
                        value = parseInt(valueStr);
                    else if (type.equals("float"))
                        value = parseFloat(valueStr);
                    else if (type.equals("double"))
                        value = parseDouble(valueStr);
                    else
                        value = valueStr;

                    f.set(settings, value);
                }
            }
        }
        catch (IllegalAccessException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static Point parsePoint(String value) {
        String[] parts = value.split(",");
        try {
            int x = Integer.parseInt(parts[0].strip());
            int y = Integer.parseInt(parts[1].strip());
            return new Point(x, y);
        }
        catch (Exception ignored) {throw ignored;}
        //return new Point(0, 0);
    }

    public static Boolean parseBool(String value) {
        char c = value.charAt(0);
        c = c >= 'A' && c <= 'Z' ? (char)(c + 0x20) : c;
        return c == 't' || c == 'y';
    }

    public static Integer parseInt(String value) {
        Integer n = 0;
        try { n = Integer.parseInt(value); }
        catch (Exception ignored) {throw ignored;}
        return n;
    }

    public static Float parseFloat(String value) {
        Float n = 0.0f;
        try { n = Float.parseFloat(value); }
        catch (Exception ignored) {throw ignored;}
        return n;
    }

    public static Double parseDouble(String value) {
        Double n = 0.0;
        try { n = Double.parseDouble(value); }
        catch (Exception ignored) {throw ignored;}
        return n;
    }

    public static String getHomeDir() {
        return Utils.ATDir;
    }

    public static String getFileSeparator() {
        return separator;
    }
}
