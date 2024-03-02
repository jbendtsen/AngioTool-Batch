package AngioTool;

import Utils.BatchUtils;
import Utils.RefVector;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;

public class AngioTool
{
    public static final String VERSION = "AngioTool-Batch 2.0 r8 (22.02.24)";
    public static final String PREFS_TXT = "AT_Prefs.txt";
    public static final String BATCH_TXT = "AT_BatchPrefs.txt";

    public static String osName;
    public static String osArch;
    public static String osVersion;
    public static String javaVersion;
    public static String javaVmVersion;
    public static String javaVmName;
    public static final String LOOKANDFEEL = "System";
    public static final String THEME = "Test";
    //public static String ATDir;
    //public static String prefsDir;
    //public static String currentDir;
    //public static String resultsPath;
    //public static String ATClassCanonicalName;
    public static ImageIcon ATIcon;
    public static ImageIcon ATImage;
    public static ImageIcon ATFolder;
    public static ImageIcon ATBatch;
    public static ImageIcon ATExit;
    public static ImageIcon ATHelp;
    public static ImageIcon ATExcel;
    public static ImageIcon ATFolderSmall;
    public static ImageIcon ATExcelSmall;

    public static void main(String[] args)
    {
        // Load Lee94 lookup table on program start
        try {
            Class.forName("Algorithms.Lee94");
        }
        catch (Throwable t) {
            t.printStackTrace();
            BatchUtils.showExceptionInDialogBox(t.getCause());
            return;
        }

        EventQueue.invokeLater(AngioTool::initializeGui);
    }

    public static void initializeGui()
    {
        Class at = AngioTool.class;
        String canonicalName = at.getCanonicalName();

        osName = System.getProperty("os.name");
        osArch = System.getProperty("os.arch");
        osVersion = System.getProperty("os.version");
        javaVersion = System.getProperty("java.version");
        javaVmName = System.getProperty("java.vm.name");
        javaVmVersion = System.getProperty("java.vm.version");
        //ATDir = System.getProperty("user.dir").replace("\\", "/");
        URL url = AngioToolGui2.class.getProtectionDomain().getCodeSource().getLocation();

        ATIcon = createImageIcon(at, "/images/icon.gif");
        ATFolder = createImageIcon(at, "/images/folder.png");
        ATBatch = createImageIcon(at, "/images/batch.png");
        ATHelp = createImageIcon(at, "/images/help.png");
        ATExcel = createImageIcon(at, "/images/excel.png");

        ATFolderSmall = createResizedIcon(ATFolder, 24, 24);
        ATExcelSmall = createResizedIcon(ATExcel, 24, 24);

        RefVector<String> errors = new RefVector<>(String.class);

        AnalyzerParameters analyzerParams = new AnalyzerParameters();
        try {
            errors.add(ATPreferences.load(analyzerParams, at, PREFS_TXT));
        }
        catch (Exception ex) {
            errors.add(BatchUtils.buildDialogMessageFromException(ex));
            analyzerParams = AnalyzerParameters.defaults();
        }

        BatchParameters batchParams = new BatchParameters();
        try {
            errors.add(ATPreferences.load(batchParams, at, BATCH_TXT));
        }
        catch (Exception ex) {
            errors.add(BatchUtils.buildDialogMessageFromException(ex));
            batchParams = BatchParameters.defaults();
        }

        if (errors.size > 0)
            BatchUtils.showDialogBox("Configuration parsing error", String.join("\n", errors));

        AngioToolGui2 angioToolGui = new AngioToolGui2(analyzerParams, batchParams);
        angioToolGui.setLocation(new Point(200, 250));
        angioToolGui.setVisible(true);
    }

    static ImageIcon createImageIcon(Class c, String path)
    {
        URL imgURL = c.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    static ImageIcon createResizedIcon(ImageIcon original, int w, int h)
    {
        return new ImageIcon(original.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT));
    }
}
