package Batch;

import Tiff.ImageInfo;
import Tiff.ImageReader;
import Tiff.TiffDecoder;
import Tiff.TiffEncoder;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import javax.imageio.ImageIO;

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
        int originalWidth = 0;
        int originalHeight = 0;
        int[] inputPixels = null;
        File file = new File(absPath);
        BufferedImage javaImage = ImageIO.read(file);

        if (javaImage != null) {
            originalWidth = javaImage.getWidth();
            originalHeight = javaImage.getHeight();
            if (originalWidth <= 0 || originalHeight <= 0)
                return null;

            inputPixels = new int[originalWidth * originalHeight];
            javaImage.getRGB(0, 0, originalWidth, originalHeight, inputPixels, 0, originalWidth);
        }
        else {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            ByteBuffer magic = ByteBuffer.allocate(2);
            fc.read(magic);
            byte m0 = magic.get();
            byte m1 = magic.get();
            fc.position(0);

            if (m0 == 'P' && ((m1 >= '0' && m1 <= '7') || m1 == 'F' || m1 == 'f')) {
                RefVector<int[]> images = NetpbmReader.readArgbImages(fc);
                if (images == null || images.size <= 0)
                    return null;

                inputPixels = images.buf[0];
                originalWidth  = inputPixels[inputPixels.length - 2];
                originalHeight = inputPixels[inputPixels.length - 1];
                if (originalWidth <= 0 || originalHeight <= 0)
                    return null;
            }
            else if ((m0 == 'M' && m1 == 'M') || (m0 == 'I' && m1 == 'I')) {
                MappedByteBuffer mapped = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                TiffDecoder td = new TiffDecoder(file, mapped);
                ArrayList<ImageInfo> images = td.getTiffImages();
                if (images == null || images.size() == 0)
                    return null;

                ImageInfo info = images.get(0);
                originalWidth = info.width;
                originalHeight = info.height;
                if (originalWidth <= 0 || originalHeight <= 0)
                    return null;

                fc.position(0);
                Object pixelData = new ImageReader(info).readPixels(fis);
                if (pixelData instanceof int[]) {
                    inputPixels = (int[])pixelData;
                }
                else if (pixelData instanceof byte[]) {
                    inputPixels = new int[originalWidth * originalHeight];
                    byte[] byteData = (byte[])pixelData;
                    for (int i = 0; i < inputPixels.length; i++) {
                        int lum = byteData[i] & 0xff;
                        inputPixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                    }
                }
                else if (pixelData instanceof short[]) {
                    inputPixels = new int[originalWidth * originalHeight];
                    short[] shortData = (short[])pixelData;
                    for (int i = 0; i < inputPixels.length; i++) {
                        int lum = shortData[i] >>> 8;
                        inputPixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                    }
                }
                else if (pixelData instanceof float[]) {
                    inputPixels = new int[originalWidth * originalHeight];
                    float[] floatData = (float[])pixelData;
                    for (int i = 0; i < inputPixels.length; i++) {
                        int lum = (int)(floatData[i] * 255.0f);
                        inputPixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                    }
                }
            }
            else {
                return null;
            }
        }

        int width  = Math.max((int)(originalWidth * resizeFactor), 1);
        int height = Math.max((int)(originalHeight * resizeFactor), 1);
        int area = width * height;

        Bitmap.SplitLayer layer = (Bitmap.SplitLayer)image.reallocate(new Bitmap.SplitLayer(), width, height);

        byte[] red   = layer.red.buf;
        byte[] green = layer.green.buf;
        byte[] blue  = layer.blue.buf;

        long redTally = 0;
        long greenTally = 0;
        long blueTally = 0;

        if (width == originalWidth && height == originalHeight) {
            for (int i = 0; i < area; i++) {
                int r = (inputPixels[i] >> 16) & 0xff;
                int g = (inputPixels[i] >> 8) & 0xff;
                int b = inputPixels[i] & 0xff;
                redTally += r;
                greenTally += g;
                blueTally += b;
                red[i] = (byte)r;
                green[i] = (byte)g;
                blue[i] = (byte)b;
            }
        }
        else if (resizeFactor < 1.0) {
            float[] samples = FloatBufferPool.acquireZeroed(width * height * 4);

            double xAdv = width > 1 ? (double)(width - 1) / (double)(originalWidth - 1) : 0.0;
            double yAdv = height > 1 ? (double)(height - 1) / (double)(originalHeight - 1) : 0.0;

            for (int y = 0; y < originalHeight; y++) {
                for (int x = 0; x < originalWidth; x++) {
                    double xx = 0.5 + (xAdv * x);
                    double yy = 0.5 + (yAdv * y);
                    double xFrac = xx - Math.floor(xx);
                    double yFrac = yy - Math.floor(yy);
                    float xNeigh = (float)Math.abs(xFrac - 0.5);
                    float yNeigh = (float)Math.abs(yFrac - 0.5);

                    int x2 = (int)(xFrac - 0.5) * 2 + 1 + (int)xx;
                    int y2 = (int)(yFrac - 0.5) * 2 + 1 + (int)yy;
                    int a = (int)xx + width * (int)yy;
                    int b = x2 + width * (int)yy;
                    int c = (int)xx + width * y2;
                    int d = x2 + width * y2;

                    float redValue   = (float)((inputPixels[x + width * y] >>> 16) & 0xff);
                    float greenValue = (float)((inputPixels[x + width * y] >>> 8) & 0xff);
                    float blueValue  = (float)(inputPixels[x + width * y] & 0xff);

                    samples[a*4]   += redValue   * (1.0f - xNeigh) * (1.0f - yNeigh);
                    samples[a*4+1] += greenValue * (1.0f - xNeigh) * (1.0f - yNeigh);
                    samples[a*4+2] += blueValue  * (1.0f - xNeigh) * (1.0f - yNeigh);
                    samples[a*4+3] += 1.0f;

                    samples[b*4]   += redValue   * xNeigh * (1.0f - yNeigh);
                    samples[b*4+1] += greenValue * xNeigh * (1.0f - yNeigh);
                    samples[b*4+2] += blueValue  * xNeigh * (1.0f - yNeigh);
                    samples[b*4+3] += 1.0f;

                    samples[c*4]   += redValue   * (1.0f - xNeigh) * yNeigh;
                    samples[c*4+1] += greenValue * (1.0f - xNeigh) * yNeigh;
                    samples[c*4+2] += blueValue  * (1.0f - xNeigh) * yNeigh;
                    samples[c*4+3] += 1.0f;

                    samples[d*4]   += redValue   * xNeigh * yNeigh;
                    samples[d*4+1] += greenValue * xNeigh * yNeigh;
                    samples[d*4+2] += blueValue  * xNeigh * yNeigh;
                    samples[d*4+3] += 1.0f;
                }
            }

            for (int i = 0; i < width * height * 4; i += 4) {
                float weight = 1.0f / samples[i+3];
                int r = Math.min(Math.max((int)(samples[i]   * weight), 0), 255);
                int g = Math.min(Math.max((int)(samples[i+1] * weight), 0), 255);
                int b = Math.min(Math.max((int)(samples[i+2] * weight), 0), 255);
                redTally += r;
                greenTally += g;
                blueTally += b;
                red[i] = (byte)r;
                green[i] = (byte)g;
                blue[i] = (byte)b;
            }

            FloatBufferPool.release(samples);
        }
        else {
            double xAdv = width > 1 ? (double)(originalWidth - 1) / (double)(width - 1) : 0.0;
            double yAdv = height > 1 ? (double)(originalHeight - 1) / (double)(height - 1) : 0.0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double xx = 0.5 + (xAdv * x);
                    double yy = 0.5 + (yAdv * y);
                    double xFrac = xx - Math.floor(xx);
                    double yFrac = yy - Math.floor(yy);
                    float xNeigh = (float)Math.abs(xFrac - 0.5);
                    float yNeigh = (float)Math.abs(yFrac - 0.5);

                    int x2 = (int)(xFrac - 0.5) * 2 + 1 + (int)xx;
                    int y2 = (int)(yFrac - 0.5) * 2 + 1 + (int)yy;
                    int a = inputPixels[(int)xx + originalWidth * (int)yy];
                    int b = inputPixels[x2 + originalWidth * (int)yy];
                    int c = inputPixels[(int)xx + originalWidth * y2];
                    int d = inputPixels[x2 + originalWidth * y2];

                    float redValue =
                        (1.0f - xNeigh) * (1.0f - yNeigh) * ((a >> 16) & 0xff) +
                        xNeigh * (1.0f - yNeigh) * ((b >> 16) & 0xff) +
                        (1.0f - xNeigh) * yNeigh * ((c >> 16) & 0xff) +
                        xNeigh * yNeigh * ((d >> 16) & 0xff);
                    float blueValue =
                        (1.0f - xNeigh) * (1.0f - yNeigh) * ((a >> 8) & 0xff) +
                        xNeigh * (1.0f - yNeigh) * ((b >> 8) & 0xff) +
                        (1.0f - xNeigh) * yNeigh * ((c >> 8) & 0xff) +
                        xNeigh * yNeigh * ((d >> 8) & 0xff);
                    float greenValue =
                        (1.0f - xNeigh) * (1.0f - yNeigh) * (a & 0xff) +
                        xNeigh * (1.0f - yNeigh) * (b & 0xff) +
                        (1.0f - xNeigh) * yNeigh * (c & 0xff) +
                        xNeigh * yNeigh * (d & 0xff);

                    int rr = Math.min(Math.max((int)redValue, 0), 255);
                    int gg = Math.min(Math.max((int)greenValue, 0), 255);
                    int bb = Math.min(Math.max((int)blueValue, 0), 255);
                    redTally += rr;
                    greenTally += gg;
                    blueTally += bb;
                    red[x+width*y] = (byte)rr;
                    green[x+width*y] = (byte)gg;
                    blue[x+width*y] = (byte)bb;
                }
            }
        }

        if (redTally >= greenTally && redTally >= blueTally)
            layer.selectedChannelIdx = 0;
        else if (greenTally >= blueTally)
            layer.selectedChannelIdx = 1;
        else
            layer.selectedChannelIdx = 2;

        if (outCombinedCopy != null) {
            if (width == originalWidth && height == originalHeight) {
                outCombinedCopy.setFirstCombinedBuffer(inputPixels, width, height);

                int mask = outUseSingleChannel ? (0xff << ((2-layer.selectedChannelIdx) * 8)) : 0xfffFFF;
                for (int i = 0; i < area; i++)
                    inputPixels[i] = 0xff000000 | (inputPixels[i] & mask);
            }
            else {
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
                        rgb[i] = 0xff000000 | inputPixels[i];
                }
            }
        }

        inputPixels = null;

        image.width = width;
        image.height = height;
        return image;
    }

    public static void saveImage(Bitmap image, int layerIdx, String format, String absPath) throws IOException
    {
        Bitmap.Layer layer = image.layers.buf[layerIdx];

        if (format.length() == 3 && format.charAt(0) == 'p' && format.charAt(2) == 'm') {
            if (layer instanceof Bitmap.SplitLayer) {
                Bitmap.SplitLayer sl = (Bitmap.SplitLayer)layer;
                writePpm24(sl.red.buf, sl.green.buf, sl.blue.buf, image.width, image.height, absPath);
            }
            else {
                int[] rgb = layer.getRgb();
                writePpm24(rgb, image.width, image.height, absPath);
            }
        }
        else if (format == "tif" || format == "tiff") {
            OutputStream out = Files.newOutputStream(
                FileSystems.getDefault().getPath("", absPath),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            );

            ImageInfo fi = new ImageInfo();
            fi.nImages = 1;
            fi.width = image.width;
            fi.height = image.height;

            if (layer instanceof Bitmap.SplitLayer) {
                Bitmap.SplitLayer sl = (Bitmap.SplitLayer)layer;
                fi.fileType = ImageInfo.RGB_SPLIT;
                fi.reds = sl.red.buf;
                fi.greens = sl.green.buf;
                fi.blues = sl.blue.buf;
            }
            else {
                fi.fileType = ImageInfo.RGB;
                fi.pixels = layer.getRgb();
            }

            TiffEncoder te = new TiffEncoder(fi);

            try {
                te.write(out);
            }
            finally {
                out.close();
            }
        }
        else {
            BufferedImage javaImage = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB);
            int[] outPixels = ((DataBufferInt)javaImage.getData().getDataBuffer()).getData();

            if (layer instanceof Bitmap.SplitLayer) {
                Bitmap.SplitLayer sl = (Bitmap.SplitLayer)layer;
                int area = image.width * image.height;
                byte[] reds = sl.red.buf;
                byte[] greens = sl.green.buf;
                byte[] blues = sl.blue.buf;
                for (int i = 0; i < area; i++)
                    outPixels[i] = ((reds[i] & 0xff) << 16) | ((greens[i] & 0xff) << 8) | (blues[i] & 0xff);
            }
            else {
                System.arraycopy(layer.getRgb(), 0, outPixels, 0, image.width * image.height);
            }

            ImageIO.write(javaImage, format, new File(absPath));
        }
    }

    public static BufferedImage openAsJavaImage(String absolutePath)
    {
        return ImageIO.read(new File(absolutePath));
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
