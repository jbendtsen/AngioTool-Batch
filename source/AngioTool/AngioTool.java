package AngioTool;

import Utils.BatchUtils;
import Utils.RefVector;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.net.URL;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class AngioTool
{
    public static final String VERSION = "AngioTool-Batch 2.0 r8 (22.02.24)";
    public static final String PREFS_TXT = "AT_Prefs.txt";
    public static final String BATCH_TXT = "AT_BatchPrefs.txt";

    public static ImageIcon ATIcon;
    public static ImageIcon ATImage;
    public static ImageIcon ATFolder;
    public static ImageIcon ATBatch;
    public static ImageIcon ATExit;
    public static ImageIcon ATHelp;
    public static ImageIcon ATExcel;
    public static ImageIcon ATFolderSmall;
    public static ImageIcon ATExcelSmall;
    public static ImageIcon ATPlus;
    public static ImageIcon ATMinus;
    public static ImageIcon ATLeft;
    public static ImageIcon ATUp;
    public static ImageIcon ATRight;
    public static ImageIcon ATDown;

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

        try {
            BufferedImage arrowImage = ImageIO.read(at.getResource("/images/arrow.png"));
            BufferedImage arrowLeft = new BufferedImage(arrowImage.getWidth(), arrowImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics ctx = arrowLeft.createGraphics();
            ctx.drawImage(arrowImage, 0, 0, null);
            ctx.dispose();

            BufferedImage arrowUp = makeClockwiseQuarterRotatedImage(arrowLeft);
            BufferedImage arrowRight = makeClockwiseQuarterRotatedImage(arrowUp);
            BufferedImage arrowDown = makeClockwiseQuarterRotatedImage(arrowRight);

            ATLeft = new ImageIcon(arrowLeft);
            ATUp = new ImageIcon(arrowUp);
            ATRight = new ImageIcon(arrowRight);
            ATDown = new ImageIcon(arrowDown);

            ATIcon = createImageIcon(at, "/images/icon.gif");
            ATFolder = createImageIcon(at, "/images/folder.png");
            ATBatch = createImageIcon(at, "/images/batch.png");
            ATHelp = createImageIcon(at, "/images/help.png");
            ATExcel = createImageIcon(at, "/images/excel.png");

            ATFolderSmall = createResizedIcon(ATFolder, 24, 24);
            ATExcelSmall = createResizedIcon(ATExcel, 24, 24);

            ATPlus = createImageIcon(at, "/images/plus.png");
            ATMinus = createImageIcon(at, "/images/minus.png");
        }
        catch (Throwable ex) {
            BatchUtils.showExceptionInDialogBox(ex);
        }

        RefVector<String> errors = new RefVector<>(String.class);

        AnalyzerParameters analyzerParams = new AnalyzerParameters();
        try {
            errors.extend(ATPreferences.load(analyzerParams, at, PREFS_TXT));
        }
        catch (Exception ex) {
            errors.add(BatchUtils.buildDialogMessageFromException(ex));
            analyzerParams = AnalyzerParameters.defaults();
        }

        BatchParameters batchParams = new BatchParameters();
        try {
            errors.extend(ATPreferences.load(batchParams, at, BATCH_TXT));
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

    static BufferedImage makeClockwiseQuarterRotatedImage(BufferedImage original)
    {
        int w = original.getWidth();
        int h = original.getHeight();
        // width and height are flipped in the output
        BufferedImage result = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);

        int[] srcPixels = ((DataBufferInt)original.getRaster().getDataBuffer()).getData();
        int[] dstPixels = ((DataBufferInt)result.getRaster().getDataBuffer()).getData();

        for (int y1 = 0; y1 < h; y1++) {
            for (int x1 = 0; x1 < w; x1++) {
                int x2 = h - y1 - 1;
                int y2 = x1;
                dstPixels[x2 + h * y2] = srcPixels[x1 + w * y1];
            }
        }

        return result;
    }
}
