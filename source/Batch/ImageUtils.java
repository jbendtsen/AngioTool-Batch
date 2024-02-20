package Batch;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import javax.swing.ImageIcon;

public class ImageUtils
{
    public static Bitmap openImage(Bitmap image, String absPath, double resizeFactor, Bitmap outCombinedCopy, boolean outUseSingleChannel)
    {
        ImagePlus iplus = IJ.openImage(absPath);
        if (iplus == null)
            return null;

        int originalWidth = iplus.getWidth();
        int originalHeight = iplus.getHeight();
        if (originalWidth <= 0 || originalHeight <= 0)
            return null;

        //Calibration calibration = iplus.getCalibration();
        double pixelWidth = 1.0; // calibration.pixelWidth;
        double pixelHeight = 1.0; // calibration.pixelHeight;
        double pixelBreadth = 1.0; // calibration.pixelDepth;

        ImageProcessor ip = iplus.getProcessor();
        ColorProcessor fullScaleProc = ip instanceof ColorProcessor ? (ColorProcessor)ip : (ColorProcessor)ip.convertToRGB();
        ColorProcessor proc = resizeFactor == 1.0 ?
            fullScaleProc :
            (ColorProcessor)fullScaleProc.resize((int)((double)originalWidth * resizeFactor));

        int width = proc.getWidth();
        int height = proc.getHeight();
        int area = width * height;
        int[] pixels = (int[])proc.getPixels();

        Bitmap.SplitLayer layer = (Bitmap.SplitLayer)image.reallocate(new Bitmap.SplitLayer(), width, height);

        byte[] red   = layer.red.buf;
        byte[] green = layer.green.buf;
        byte[] blue  = layer.blue.buf;

        long redTally = 0;
        long greenTally = 0;
        long blueTally = 0;
        for (int i = 0; i < area; i++) {
            int r = (pixels[i] >> 16) & 0xff;
            int g = (pixels[i] >> 8) & 0xff;
            int b = pixels[i] & 0xff;
            redTally += r;
            greenTally += g;
            blueTally += b;
            red[i] = (byte)r;
            green[i] = (byte)g;
            blue[i] = (byte)b;
        }

        if (redTally >= greenTally && redTally >= blueTally)
            layer.selectedChannelIdx = 0;
        else if (greenTally >= blueTally)
            layer.selectedChannelIdx = 1;
        else
            layer.selectedChannelIdx = 2;

        if (outCombinedCopy != null) {
            outCombinedCopy.reallocate(new Bitmap.CombinedLayer(), width, height);
            int[] rgb = outCombinedCopy.getDefaultRgb();

            if (outUseSingleChannel) {
                byte[] channel = layer.getSelectedChannel();
                int shift = (2-layer.selectedChannelIdx) * 8;
                for (int i = 0; i < area; i++) {
                    int p = channel[i] & 0xff;
                    rgb[i] = 0xff000000 | (p << shift);
                }
            }
            else {
                for (int i = 0; i < area; i++)
                    rgb[i] = 0xff000000 | pixels[i];
            }

            outCombinedCopy.width = width;
            outCombinedCopy.height = height;
            outCombinedCopy.pixelWidth = pixelWidth;
            outCombinedCopy.pixelHeight = pixelHeight;
            outCombinedCopy.pixelBreadth = pixelBreadth;
        }

        pixels = null;
        proc = null;
        fullScaleProc = null;
        ip = null;
        //calibration = null;
        iplus = null;

        image.width = width;
        image.height = height;
        image.pixelWidth = pixelWidth;
        image.pixelHeight = pixelHeight;
        image.pixelBreadth = pixelBreadth;
        return image;
    }

    public static void saveImage(Bitmap image, int layerIdx, String format, String absPath)
    {
        Bitmap.Layer layer = image.layers.buf[layerIdx];
        int[] rgbCopy = null;
        int[] rgbOriginal = layer.getRgb();

        // ImageJ requires that a buffer passed into the ColorProcessor constructor must have a length equal to width*height.
        // What if it's bigger? That should be fine, right? ImageJ disagrees.
        // This is unfortunate, since our BufferPool design allows for buffers larger than what is requested to be returned.
        if (rgbOriginal == null || rgbOriginal.length != image.width * image.height) {
            rgbCopy = new int[image.width * image.height];
            layer.exportRgb(rgbCopy, image.width, image.height);
        }

        ColorProcessor proc = new ColorProcessor(image.width, image.height, rgbCopy != null ? rgbCopy : rgbOriginal);
        ImagePlus iplus = new ImagePlus(null, proc);
        IJ.saveAs(iplus, format, absPath);
    }

    public static ImageIcon openAsImageIcon(String absolutePath)
    {
        ImagePlus ip = IJ.openImage(absolutePath);
        if (ip == null)
            return null;

        return new ImageIcon(ip.getImage());
    }

    public static void writePgm(byte[] pixels, int width, int height, String title) {
        byte[] header = ("P5\n" + width + " " + height + "\n255\n").getBytes();
        ByteVectorOutputStream out = new ByteVectorOutputStream(header.length + pixels.length);
        out.add(header);
        out.add(pixels);
        try {
            java.nio.file.Files.write(
                java.nio.file.FileSystems.getDefault().getPath("", title),
                out.buf,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.WRITE
            );
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writePpm24(byte[] pixels24, int width, int height, String title)
    {
        byte[] header = ("P6\n" + width + " " + height + "\n255\n").getBytes();
        try {
            OutputStream out = Files.newOutputStream(
                FileSystems.getDefault().getPath("", title),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            );
            try {
                out.write(header);
                out.write(pixels24, 0, width * height * 3);
            }
            finally {
                out.close();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
