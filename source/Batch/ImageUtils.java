package Batch;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import javax.swing.ImageIcon;

public class ImageUtils
{
    public static Bitmap openImage(
        Bitmap image,
        String absPath,
        double resizeFactor,
        Bitmap outCombinedCopy,
        boolean outUseSingleChannel
    ) throws Exception
    {
        int width = 0;
        int height = 0;
        int[] inputPixels = null;
        File file = new File(absPath);
        Image javaImage = ImageIO.read(file);

        if (javaImage != null) {
            width = javaImage.getWidth();
            height = javaImage.getHeight();
            if (width <= 0 || height <= 0)
                return null;

            inputPixels = new int[width * height];
            image.getRGB(0, 0, width, height, inputPixels, 0, width);
        }
        else {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            byte[] magic = new byte[2];
            fc.read(magic);
            fc.position(0);

            if (magic[0] == 'P' && magic[1] >= '0' && magic[1] <= '6') {
                inputPixels = PgmReader.read(fc);
                width  = inputPixels[inputPixels.length - 2];
                height = inputPixels[inputPixels.length - 1];
            }
            else if ((magic[0] == 'M' && magic[1] == 'M') || (magic[0] == 'I' && magic[1] == 'I')) {
                TiffDecoder td = new TiffDecoder();
            }
        }

        int originalWidth = iplus.getWidth();
        int originalHeight = iplus.getHeight();
        if (originalWidth <= 0 || originalHeight <= 0)
            return null;

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

    public static void writePpm24(byte[] reds, byte[] greens, byte[] blues, int width, int height, String title)
    {
        byte[] header = ("P6\n" + width + " " + height + "\n255\n").getBytes();
        byte[] buffer = new byte[12288];
        try {
            OutputStream out = Files.newOutputStream(
                FileSystems.getDefault().getPath("", title),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            );
            try {
                out.write(header);

                int offset = 0;
                do {
                    int chunkSize = Math.min(4096, width * height - offset);
                    for (int b = 0, p = 0; p < chunkSize; b += 3, p++) {
                        buffer[b]   = reds[offset + p];
                        buffer[b+1] = greens[offset + p];
                        buffer[b+2] = blues[offset + p];
                    }
                    out.write(buffer, 0, chunkSize * 3);
                    offset += chunkSize;
                } while (offset < width * height);
            }
            finally {
                out.close();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writePpm24(int[] argb, int width, int height, String title)
    {
        byte[] header = ("P6\n" + width + " " + height + "\n255\n").getBytes();
        byte[] buffer = new byte[12288];
        try {
            OutputStream out = Files.newOutputStream(
                FileSystems.getDefault().getPath("", title),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            );
            try {
                out.write(header);

                int offset = 0;
                do {
                    int chunkSize = Math.min(4096, width * height - offset);
                    for (int b = 0, p = 0; p < chunkSize; b += 3, p++) {
                        int color = argb[offset + p];
                        buffer[b]   = (byte)((color >>> 16) & 0xff);
                        buffer[b+1] = (byte)((color >>> 8) & 0xff);
                        buffer[b+2] = (byte)(color & 0xff);
                    }
                    out.write(buffer, 0, chunkSize * 3);
                    offset += chunkSize;
                } while (offset < width * height);
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
