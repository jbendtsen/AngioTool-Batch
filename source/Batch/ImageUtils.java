package Batch;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;

public class ImageUtils
{
    public static Bitmap openAndAcquireImage(String absPath, double resizeFactor, Bitmap outCombinedCopy, boolean outUseSingleChannel)
    {
        ImagePlus iplus = IJ.openImage(absPath);
        if (iplus == null)
            return null;

        int width = iplus.getWidth();
        int height = iplus.getHeight();
        int area = width * height;
        if (width <= 0 || height <= 0)
            return null;

        Calibration calibration = iplus.getCalibration();
        double pixelWidth = calibration.pixelWidth;
        double pixelHeight = calibration.pixelHeight;
        double pixelBreadth = calibration.pixelDepth;

        ImageProcessor ip = iplus.getProcessor();
        ColorProcessor fullScaleProc = ip instanceof ColorProcessor ? (ColorProcessor)ip : (ColorProcessor)ip.convertToRGB();
        ColorProcessor proc = resizeFactor == 1.0 ? fullScaleProc : (ColorProcessor)fullScaleProc.resize((int)((double)width / resizeFactor));
        int[] pixels = (int[])proc.getPixels();

        byte[] red   = ByteBufferPool.acquireAsIs(area);
        byte[] green = ByteBufferPool.acquireAsIs(area);
        byte[] blue  = ByteBufferPool.acquireAsIs(area);

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

        Bitmap.SplitLayer layer = new Bitmap.SplitLayer();
        layer.red = red;
        layer.green = green;
        layer.blue = blue;

        if (redTally >= greenTally && redTally >= blueTally)
            layer.selectedChannelIdx = 0;
        else if (greenTally >= blueTally)
            layer.selectedChannelIdx = 1;
        else
            layer.selectedChannelIdx = 2;

        if (outCombinedCopy != null) {
            int[] rgb = IntBufferPool.acquireAsIs(area);
            if (outUseSingleChannel) {
                byte[] channel = layer.getSelectedChannel();
                for (int i = 0; i < area; i++) {
                    int p = channel[i] & 0xff;
                    rgb[i] = (p << 16) | (p << 8) | p;
                }
            }
            else {
                System.arraycopy(pixels, 0, rgb, 0, area);
            }

            Bitmap.CombinedLayer outLayer = new Bitmap.CombinedLayer();
            outLayer.rgb = rgb;

            outCombinedCopy.layers.add(outLayer);
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
        calibration = null;
        iplus = null;

        Bitmap image = new Bitmap();
        image.layers.add(layer);
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

        if (rgbOriginal == null)
            rgbCopy = layer.acquireRgbCopy(image.width, image.height);

        ColorProcessor proc = new ColorProcessor(image.width, image.height, rgbOriginal != null ? rgbOriginal : rgbCopy);
        ImagePlus iplus = new ImagePlus(null, proc);
        IJ.saveAs(iplus, format, absPath);

        IntBufferPool.release(rgbCopy);
    }

    public static Bitmap releaseImage(Bitmap image)
    {
        for (int i = 0; i < image.layers.size; i++)
            image.layers.buf[i].releaseBuffers();
    }
}
