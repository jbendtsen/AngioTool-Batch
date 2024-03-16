package Utils;

import AngioTool.NumberEntry;
import AngioTool.ColorSizeEntry;
import AngioTool.SimpleFileFilter;
import Xlsx.*;
import java.awt.Dimension;
import java.awt.Font;
import java.lang.reflect.Field;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class BatchUtils
{
    public static final double EPSILON = Math.pow(2.0, -23);

    public static void thresholdFlexible(byte[] image, int width, int height, int low, int high)
    {
        if (low > high) {
            int temp = low;
            low = high;
            high = temp;
        }

        int area = width * height;
        for (int i = 0; i < area; i++) {
            int pixel = image[i];
            // pixel > low && pixel <= high -> white
            // pixel <= low || pixel > high -> black
            // this should probably [low, high] not (low, high], but the original behaviour must stay the same
            image[i] = (byte)(((pixel - (high+1)) >> 31) & ((low - pixel) >> 31));
        }
    }

    public static long countForegroundPixels(byte[] image, int width, int height)
    {
        long count = 0;
        final int area = width * height;
        for (int i = 0; i < area; i++) {
            long pixel = image[i];
            count -= (image[i] ^ (image[i] + 1L)) >> 63L;
        }
        return count;
    }

    public static int roundIntegerToNearestUpperTenth(int a)
    {
        int remainder = a % 10;

        while(remainder != 0)
            remainder = ++a % 10;

        return a;
    }

    public static int getAnInt(String str)
    {
        int n = 0;
        int len = str.length();

        for (int i = 0; i < len; i++) {
            int c = str.codePointAt(i);
            if (c >= 0x30 && c <= 0x39)
                n = n * 10 + (c - 0x30);
            else
                break;
        }

        return n;
    }

    public static int[] getSomeInts(String str)
    {
        IntVector numbers = new IntVector();
        boolean wasNum = false;
        boolean isNeg = false;
        int n = 0;
        int len = str.length();

        for (int i = 0; i < len; i++) {
            int c = str.codePointAt(i);
            if (c >= 0x30 && c <= 0x39) {
                n = n * 10 + (c - 0x30);
                wasNum = true;
            }
            else if (!wasNum && c == '-') {
                isNeg = true;
            }
            else {
                if (wasNum)
                    numbers.add(isNeg ? -n : n);
                n = 0;
                wasNum = false;
                isNeg = false;
            }
        }
        if (wasNum)
            numbers.add(isNeg ? -n : n);

        return numbers.copy();
    }

    public static double[] getSomeDoubles(String str)
    {
        IntVector numbers = new IntVector();
        boolean wasNum = false;
        boolean isNeg = false;
        int mode = 0;
        int[] nums = new int[3];
        int len = str.length();

        for (int i = 0; i < len; i++) {
            int c = str.codePointAt(i);
            if (c >= 0x30 && c <= 0x39) {
                if (nums[mode] <= 214748363)
                    nums[mode] = nums[mode] * 10 + (c - 0x30);
                wasNum = true;
            }
            else if (!wasNum && c == '-') {
                isNeg = true;
            }
            else {
                if (wasNum) {
                    nums[mode] = nums[mode] * 2 + (isNeg ? 1 : 0);
                    isNeg = false;
                    if (mode >= 2 || (mode == 1 && c != 'e' && c != 'E') || (mode == 0 && c != '.')) {
                        numbers.add(nums);
                        nums[0] = 0;
                        nums[1] = 0;
                        nums[2] = 0;
                        mode = 0;
                    }
                    else {
                        mode++;
                    }
                }
                wasNum = false;
            }
        }
        if (wasNum) {
            nums[mode] = nums[mode] * 2 + (isNeg ? 1 : 0);
            numbers.add(nums);
        }

        double[] values = new double[numbers.size / 3];
        for (int i = 0; i < numbers.size-2; i += 3) {
            boolean isNegValue = ((numbers.buf[i] | numbers.buf[i+1]) & 1) != 0;
            boolean isNegExp = (numbers.buf[i+2] & 1) != 0;

            int frac = numbers.buf[i+1] >> 1;
            int f = frac;
            int fDigits = 0;
            boolean seenNonZero = false;
            while (f > 0) {
                if (f % 10 != 0)
                    seenNonZero = true;
                if (!seenNonZero)
                    frac /= 10;
                else
                    fDigits++;
                f /= 10;
            }

            double v = (double)(numbers.buf[i] >> 1);
            v += (double)frac * Math.pow(10.0, -fDigits);
            v *= isNegValue ? -1.0 : 1.0;

            double exp = (double)(numbers.buf[i+2] >> 1);
            v *= Math.pow(10.0, isNegExp ? -exp : exp);

            values[i/3] = v;
        }

        return values;
    }

    public static String formatIntArray(int[] array, String defaultValue)
    {
        if (array == null || array.length == 0)
            return defaultValue;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i != 0)
                sb.append(", ");
            sb.append("" + array[i]);
        }
        return sb.toString();
    }

    public static String formatDoubleArray(double[] array, String defaultValue)
    {
        if (array == null || array.length == 0)
            return defaultValue;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i != 0)
                sb.append(", ");
            sb.append(formatDouble(array[i]));
        }
        return sb.toString();
    }

    public static String formatDouble(double value)
    {
        String str = "" + value;
        if (str.endsWith(".0"))
            return str.substring(0, str.length() - 2);
        return str;
    }

    public static String formatDouble(double value, int nFracDigits)
    {
        String str = "" + value;
        if (str.endsWith(".0"))
            return str.substring(0, str.length() - 2);

        int dotIndex = str.indexOf('.');
        if (dotIndex < 0)
            return str;

        int strLen = str.length();
        if (dotIndex + nFracDigits + 1 < strLen)
            return str.substring(0, dotIndex + nFracDigits + 1);

        return str;
    }

    public static HashSet<String> makeHashSetFromStringArray(String[] array)
    {
        HashSet<String> c = new HashSet<>();
        for (String s : array)
            c.add(s);
        return c;
    }

    // TODO: something more sophisticated
    public static boolean shouldPersistField(Field f)
    {
        return !f.getName().equals("inputImagePaths");
    }

    // TODO: also something more sophisticated
    public static boolean isValidPath(String path)
    {
        return path != null && path.length() > 0;
    }

    public static int getFileNameOffset(String path)
    {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0)
            lastSlash = path.lastIndexOf('\\');

        // if 'path' contains neither a '/' nor a '\', then lastSlash will be -1, which suits us nicely.
        return lastSlash + 1;
    }

    public static String getExtension(File f)
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }

        return ext;
    }

    public static String[] splitPaths(String blob, char charSplit, char charEscape)
    {
        ArrayList<String> paths = new ArrayList<>();
        int len = blob.length();
        int start = 0;
        char prev = '\0';
        for (int i = 0; i < len; i++) {
            char c = blob.charAt(i);
            if (c == charSplit && prev != charEscape) {
                paths.add(blob.substring(start, i));
                start = i+1;
            }
        }
        if (start != len)
            paths.add(blob.substring(start, len));

        return paths.toArray(new String[0]);
    }

    public static String decideBackupFileName(String absPath, String ext)
    {
        int lastDot = absPath.lastIndexOf('.');
        String path = (lastDot > 0 && (lastDot > absPath.lastIndexOf('/') || lastDot > absPath.lastIndexOf('\\'))) ?
            absPath.substring(0, lastDot) :
            absPath;

        ext = ext.charAt(0) == '.' ? ext.substring(1) : ext;
        String newPath = path + ".bak." + ext;
        int counter = 1;
        while (new File(newPath).exists())
            newPath = path + ".bak" + (++counter) + "." + ext;

        return newPath;
    }

    public static String addSeparator(String path)
    {
        if (path == null || path.length() == 0)
            return path;

        // Not sure why this method was designed to modify the input then return it as the output.
        // Since String is an Object, thus a reference type,
        // the return value will always be the same as the input after this method is called, because it *is* the input, now modified.
        if (!path.endsWith(File.separator) && !path.endsWith("/"))
            path += path.contains(File.separator) ? File.separator : "/";

        return path;
    }

    public static double parseDouble(String text, double defaultValue)
    {
        try {
            return Double.parseDouble(text);
        }
        catch (Exception ignored) {}
        return defaultValue;
    }

    public static int parseInt(String text, int defaultValue)
    {
        try {
            return Integer.parseInt(text);
        }
        catch (Exception ignored) {}
        return defaultValue;
    }

    public static boolean isDoubleArraySimilar(double[] a, double[] b)
    {
        boolean aIsEmpty = a == null || a.length == 0;
        boolean bIsEmpty = b == null || b.length == 0;
        if (aIsEmpty)
            return bIsEmpty;
        if (bIsEmpty)
            return aIsEmpty;
        if (a.length != b.length)
            return false;

        int len = a.length;
        for (int i = 0; i < len; i++)
            if (Math.abs(a[i] - b[i]) >= EPSILON)
                return false;

        return true;
    }

    public static ByteVectorOutputStream readFullyAsVector(InputStream stream) throws IOException
    {
        final int chunkSize = 4096;
        ByteVectorOutputStream vec = new ByteVectorOutputStream(chunkSize);
        int off = 0;
        while (true) {
            int res = stream.read(vec.buf, off, chunkSize);
            if (res <= 0)
                break;
            off += res;
            vec.resize(off + chunkSize);
        }
        vec.resize(off);
        return vec;
    }

    public static void setNewFontSizeOn(JComponent ui, int newSize)
    {
        Font font = ui.getFont();
        ui.setFont(new Font(font.getName(), font.getStyle(), newSize));
    }

    public static void setNewFontStyleOn(JComponent ui, int newStyle)
    {
        Font font = ui.getFont();
        ui.setFont(new Font(font.getName(), newStyle, font.getSize()));
    }

    public static JFileChooser createFileChooser()
    {
        JFileChooser fc = new JFileChooser();
        fc.setPreferredSize(new Dimension(800, 500));
        return fc;
    }

    public static void addImageFileFilters(JFileChooser fc)
    {
        fc.addChoosableFileFilter(new SimpleFileFilter("JPEG file", "jpg", "jpeg"));
        fc.addChoosableFileFilter(new SimpleFileFilter("GIF file", "gif"));
        fc.addChoosableFileFilter(new SimpleFileFilter("PNG file", "png"));
        fc.addChoosableFileFilter(new SimpleFileFilter("BMP file", "bmp"));
        fc.addChoosableFileFilter(new SimpleFileFilter("TIFF file", "tiff", "tif"));
        fc.addChoosableFileFilter(new SimpleFileFilter("PPM file", "ppm"));
    }

    public static void showDialogBox(String title, String message)
    {
        JOptionPane.showMessageDialog(
            JOptionPane.getRootFrame(),
            message,
            title,
            0
        );
    }

    public static void showExceptionInDialogBox(Throwable t)
    {
        if (t == null)
            return;

        String name = t.getClass().getName();
        String message = buildDialogMessageFromException(t);
        showDialogBox(name, message);
    }

    public static String buildDialogMessageFromException(Throwable t)
    {
        String message = t.getMessage();

        StackTraceElement[] st = t.getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < st.length; i++) {
            String className = st[i].getClassName();
            if (!className.startsWith("java.") && !className.startsWith("javax.")) {
                sb.append("\n");
                sb.append(st[i].toString());
            }
        }

        String exSource = sb.toString();
        if (message == null || message.length() == 0)
            message = exSource;
        else
            message += exSource;

        return message;
    }

    public static GroupLayout.SequentialGroup arrangeParallelEntries(
        NumberEntry a,
        NumberEntry b,
        GroupLayout layout,
        GroupLayout.SequentialGroup sequentialGroup
    ) {
        sequentialGroup
            .addGroup(layout.createParallelGroup()
                .addComponent(a.cb)
                .addComponent(b.cb)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(a.units)
                .addComponent(b.units)
            )
            .addGap(4)
            .addGroup(layout.createParallelGroup()
                .addComponent(a.tf)
                .addComponent(b.tf)
            );

        if (a instanceof ColorSizeEntry && b instanceof ColorSizeEntry)
            sequentialGroup.addGroup(layout.createParallelGroup()
                .addComponent(((ColorSizeEntry)a).panel, 20, 30, 30)
                .addComponent(((ColorSizeEntry)b).panel, 20, 30, 30)
            );

        return sequentialGroup;
    }

    public static ArrayList<XlsxReader.SheetCells> openSpreadsheetForAppending(
        String[] outStrings,
        String existingXlsxFile,
        String defaultPath,
        JFrame parentFrame
    ) {
        File xlsxFile;
        if (existingXlsxFile == null || existingXlsxFile.length() == 0) {
            JFileChooser fc = BatchUtils.createFileChooser();
            fc.setDialogTitle("Append to Excel spreadsheet");
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            fc.setCurrentDirectory(new File(defaultPath));
            fc.setFileFilter(new SimpleFileFilter("Excel Spreadsheet", "xls", "xlsx"));

            if (fc.showSaveDialog(parentFrame) != 0) {
                outStrings[0] = null;
                return null;
            }

            xlsxFile = fc.getSelectedFile();
            if (!xlsxFile.getName().contains("."))
                xlsxFile = new File(xlsxFile.getAbsolutePath() + ".xlsx");

            defaultPath = fc.getCurrentDirectory().getAbsolutePath();
        }
        else {
            if (!existingXlsxFile.contains("."))
                existingXlsxFile += ".xlsx";

            xlsxFile = new File(existingXlsxFile);
            defaultPath = xlsxFile.getParent();
        }

        String xlsxPath = xlsxFile.getAbsolutePath();
        outStrings[0] = xlsxPath;
        outStrings[1] = defaultPath;

        ArrayList<XlsxReader.SheetCells> sheets = null;
        if (xlsxFile.exists()) {
            try { sheets = XlsxReader.loadXlsxFromFile(xlsxPath); }
            catch (IOException ignored) {}

            if (sheets == null || sheets.isEmpty() || (sheets.get(0).flags & (1 << 31)) == 0) {
                try {
                    Files.copy(
                        xlsxFile.toPath(),
                        new File(BatchUtils.decideBackupFileName(xlsxPath, "xlsx")).toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                    );
                }
                catch (IOException ignored) {}
            }
        }

        if (sheets == null)
            sheets = new ArrayList<XlsxReader.SheetCells>();

        return sheets;
    }
}
