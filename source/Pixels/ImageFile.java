package Pixels;

import Tiff.ImageInfo;
import Tiff.ImageReader;
import Tiff.TiffDecoder;
import Tiff.TiffEncoder;
import Utils.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class ImageFile
{
    public static ArgbBuffer openImageForAnalysis(
        ArgbBuffer image,
        String absPath,
        double resizeFactor
    ) throws IOException
    {
        boolean shouldResize = resizeFactor != 1.0;

        File file = new File(absPath);
        image = loadImage(image, file, shouldResize);
        if (image == null)
            return null;

        int width  = Math.max((int)(image.width * resizeFactor), 1);
        int height = Math.max((int)(image.height * resizeFactor), 1);
        int area = width * height;

        long redTally = 0;
        long greenTally = 0;
        long blueTally = 0;

        int[] resizedPixels = null;
        int[] inputPixels = image.pixels;
        int originalWidth = image.width;
        int originalHeight = image.height;

        if (!shouldResize) {
            for (int i = 0; i < area; i++) {
                int r = (inputPixels[i] >> 16) & 0xff;
                int g = (inputPixels[i] >> 8) & 0xff;
                int b = inputPixels[i] & 0xff;
                redTally += r;
                greenTally += g;
                blueTally += b;
                inputPixels[i] |= 0xff000000;
            }
        }
        else if (resizeFactor < 1.0) {
            float[] samples = FloatBufferPool.acquireZeroed((width + 1) * (height + 1) * 4);

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

                    float redValue   = (float)((inputPixels[x + originalWidth * y] >>> 16) & 0xff);
                    float greenValue = (float)((inputPixels[x + originalWidth * y] >>> 8) & 0xff);
                    float blueValue  = (float)(inputPixels[x + originalWidth * y] & 0xff);

                    float fA = (1.0f - xNeigh) * (1.0f - yNeigh);
                    samples[a*4]   += redValue   * fA;
                    samples[a*4+1] += greenValue * fA;
                    samples[a*4+2] += blueValue  * fA;
                    samples[a*4+3] += fA;

                    float fB = xNeigh * (1.0f - yNeigh);
                    samples[b*4]   += redValue   * fB;
                    samples[b*4+1] += greenValue * fB;
                    samples[b*4+2] += blueValue  * fB;
                    samples[b*4+3] += fB;

                    float fC = (1.0f - xNeigh) * yNeigh;
                    samples[c*4]   += redValue   * fC;
                    samples[c*4+1] += greenValue * fC;
                    samples[c*4+2] += blueValue  * fC;
                    samples[c*4+3] += fC;

                    float fD = xNeigh * yNeigh;
                    samples[d*4]   += redValue   * fD;
                    samples[d*4+1] += greenValue * fD;
                    samples[d*4+2] += blueValue  * fD;
                    samples[d*4+3] += fD;
                }
            }

            resizedPixels = new int[area];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = 4 * (x + width * y);
                    float weight = 1.0f / samples[idx+3];
                    int r = Math.min(Math.max((int)(samples[idx]   * weight), 0), 255);
                    int g = Math.min(Math.max((int)(samples[idx+1] * weight), 0), 255);
                    int b = Math.min(Math.max((int)(samples[idx+2] * weight), 0), 255);
                    redTally += r;
                    greenTally += g;
                    blueTally += b;
                    resizedPixels[idx >> 2] = 0xff000000 | (r << 16) | (g << 8) | b;
                }
            }

            FloatBufferPool.release(samples);
        }
        else {
            resizedPixels = new int[area];

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

                    int x1 = Math.min(Math.max((int)xx, 0), originalWidth-1);
                    int y1 = Math.min(Math.max((int)yy, 0), originalHeight-1);
                    int x2 = Math.min(Math.max((int)(xFrac - 0.5) * 2 + 1 + (int)xx, 0), originalWidth-1);
                    int y2 = Math.min(Math.max((int)(yFrac - 0.5) * 2 + 1 + (int)yy, 0), originalHeight-1);
                    int a = inputPixels[x1 + originalWidth * y1];
                    int b = inputPixels[x2 + originalWidth * y1];
                    int c = inputPixels[x1 + originalWidth * y2];
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
                    resizedPixels[x+width*y] = 0xff000000 | (rr << 16) | (gg << 8) | bb;
                }
            }
        }

        int brightestChannel;
        if (redTally >= greenTally && redTally >= blueTally)
            brightestChannel = 0;
        else if (greenTally >= blueTally)
            brightestChannel = 1;
        else
            brightestChannel = 2;

        if (shouldResize) {
            IntBufferPool.release(image.pixels);
            image.pixels = resizedPixels;
        }

        image.width = width;
        image.height = height;
        image.brightestChannel = brightestChannel;

        return image;
    }

    public static ArgbBuffer loadImage(ArgbBuffer existingImage, File file, boolean shouldAllocateWithRecycler) throws IOException
    {
        ArgbBuffer input = null;
        IOException firstException = null;
        String fileName = file.getName();
        int fileNameLen = fileName.length();

        if (fileName.endsWith(".tif") || fileName.endsWith(".tiff") || (fileName.charAt(fileNameLen - 3) == 'p' && fileName.charAt(fileNameLen - 1) == 'm')) {
            try {
                input = loadTiffOrNetpbm(existingImage, file, shouldAllocateWithRecycler);
            }
            catch (IOException ex) {
                firstException = ex;
            }
            if (input == null)
                input = loadFromImageIO(existingImage, file, shouldAllocateWithRecycler);
        }
        else {
            try {
                input = loadFromImageIO(existingImage, file, shouldAllocateWithRecycler);
            }
            catch (IOException ex) {
                firstException = ex;
            }
            if (input == null)
                input = loadTiffOrNetpbm(existingImage, file, shouldAllocateWithRecycler);
        }

        if (input != null)
            return input;

        if (firstException != null)
            throw firstException;

        return null;
    }

    static ArgbBuffer loadFromImageIO(ArgbBuffer existingImage, File file, boolean shouldAllocateWithRecycler) throws IOException
    {
        BufferedImage javaImage = ImageIO.read(file);
        if (javaImage == null)
            return null;

        int width = javaImage.getWidth();
        int height = javaImage.getHeight();
        if (width <= 0 || height <= 0)
            return null;

        int[] pixels = shouldAllocateWithRecycler ?
            IntBufferPool.acquireAsIs(width * height) :
            new int[width * height];

        javaImage.getRGB(0, 0, width, height, pixels, 0, width);

        if (existingImage == null)
            return new ArgbBuffer(pixels, width, height);

        existingImage.pixels = pixels;
        existingImage.width = width;
        existingImage.height = height;
        return existingImage;
    }

    static ArgbBuffer loadTiffOrNetpbm(ArgbBuffer existingImage, File file, boolean shouldAllocateWithRecycler) throws IOException
    {
        int[] inputPixels = null;
        int width = 0;
        int height = 0;

        FileInputStream fis = null;
        FileChannel fc = null;
        try {
            fis = new FileInputStream(file);
            fc = fis.getChannel();
            ByteBuffer magic = ByteBuffer.allocate(2);
            fc.read(magic);
            magic.position(0);
            byte m0 = magic.get();
            byte m1 = magic.get();
            fc.position(0);

            if (m0 == 'P' && ((m1 >= '0' && m1 <= '7') || m1 == 'F' || m1 == 'f')) {
                RefVector<int[]> images = NetpbmReader.readArgbImages(fc, 1, shouldAllocateWithRecycler);
                if (images == null || images.size <= 0)
                    return null;

                inputPixels = images.buf[0];
                width  = inputPixels[inputPixels.length - 2];
                height = inputPixels[inputPixels.length - 1];
                if (width <= 0 || height <= 0)
                    return null;
            }
            else if ((m0 == 'M' && m1 == 'M') || (m0 == 'I' && m1 == 'I')) {
                MappedByteBuffer mapped = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                TiffDecoder td = new TiffDecoder(file, mapped);
                ArrayList<ImageInfo> images = td.getTiffImages();
                if (images == null || images.size() == 0)
                    return null;

                ImageInfo info = images.get(0);
                width = info.width;
                height = info.height;
                if (width <= 0 || height <= 0)
                    return null;

                inputPixels = shouldAllocateWithRecycler ?
                    IntBufferPool.acquireAsIs(width * height) :
                    new int[width * height];

                fc.position(0);
                Object pixelData = new ImageReader(info).readPixels(fis, inputPixels);

                // If pixelData is an int[], then it will already be filled with the data we want.
                // Otherwise we must convert the data to an int[] ourselves.

                if (pixelData instanceof byte[]) {
                    byte[] byteData = (byte[])pixelData;
                    for (int i = 0; i < inputPixels.length; i++) {
                        int lum = byteData[i] & 0xff;
                        inputPixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                    }
                }
                else if (pixelData instanceof short[]) {
                    short[] shortData = (short[])pixelData;
                    for (int i = 0; i < inputPixels.length; i++) {
                        int lum = shortData[i] >>> 8;
                        inputPixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                    }
                }
                else if (pixelData instanceof float[]) {
                    float[] floatData = (float[])pixelData;
                    for (int i = 0; i < inputPixels.length; i++) {
                        int lum = (int)(floatData[i] * 255.0f);
                        inputPixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                    }
                }
            }
        }
        finally {
            if (fc != null) {
                try { fc.close(); }
                catch (IOException ignored) {}
            }
            if (fis != null) {
                try { fis.close(); }
                catch (IOException ignored) {}
            }
        }

        if (inputPixels == null)
            return null;

        if (existingImage == null)
            return new ArgbBuffer(inputPixels, width, height);

        existingImage.pixels = inputPixels;
        existingImage.width = width;
        existingImage.height = height;
        return existingImage;
    }

    public static void saveImage(ArgbBuffer image, String format, String absPath) throws IOException
    {
        if (format.length() == 3 && format.charAt(0) == 'p' && format.charAt(2) == 'm') {
            writePpm24(image.pixels, image.width, image.height, absPath);
        }
        else if ("tif".equals(format) || "tiff".equals(format)) {
            writeUncompressedTiff(image.pixels, image.width, image.height, absPath);
        }
        else {
            BufferedImage javaImage = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB);
            int[] outPixels = ((DataBufferInt)javaImage.getRaster().getDataBuffer()).getData();
            System.arraycopy(image.pixels, 0, outPixels, 0, image.width * image.height);
            boolean foundWriter = ImageIO.write(javaImage, format, new File(absPath));
            if (!foundWriter)
                throw new UnsupportedEncodingException("Failed to write image: unsupported format type \"" + format + "\"");
        }
    }

    public static void saveImage(
        BufferedImage javaImage,
        DataBufferInt dataBuffer,
        String format,
        String absPath
    ) throws IOException
    {
        if (format.length() == 3 && format.charAt(0) == 'p' && format.charAt(2) == 'm') {
            int[] pixels = dataBuffer.getData();
            writePpm24(pixels, javaImage.getWidth(), javaImage.getHeight(), absPath);
        }
        else if ("tif".equals(format) || "tiff".equals(format)) {
            int[] pixels = dataBuffer.getData();
            writeUncompressedTiff(pixels, javaImage.getWidth(), javaImage.getHeight(), absPath);
        }
        else {
            boolean foundWriter = ImageIO.write(javaImage, format, new File(absPath));
            if (!foundWriter)
                throw new UnsupportedEncodingException("Failed to write image: unsupported format type \"" + format + "\"");
        }
    }

    public static BufferedImage openAsJavaImage(String absolutePath)
    {
        try {
            return ImageIO.read(new File(absolutePath));
        }
        catch (IOException ignored) {}
        return null;
    }

    public static void writeUncompressedTiff(
        int[] pixels,
        int width,
        int height,
        String absPath
    ) throws IOException
    {
        OutputStream out = Files.newOutputStream(
            FileSystems.getDefault().getPath("", absPath),
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        );

        try {
            TiffWriter.writeUncompressedImage(out, pixels, width, height);
        }
        finally {
            out.close();
        }
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

    public static void writePpm24(int[] argb, int width, int height, String title)
    {
        int area = width * height;
        byte[] header = ("P6\n" + width + " " + height + "\n255\n").getBytes();
        int size = header.length + area * 3;
        byte[] buffer = ByteBufferPool.acquireAsIs(size);
        System.arraycopy(header, 0, buffer, 0, header.length);

        for (int p = 0, b = header.length; p < area; p++, b += 3) {
            int color = argb[p];
            buffer[b]   = (byte)((color >>> 16) & 0xff);
            buffer[b+1] = (byte)((color >>> 8) & 0xff);
            buffer[b+2] = (byte)(color & 0xff);
        }

        try {
            OutputStream out = Files.newOutputStream(
                FileSystems.getDefault().getPath("", title),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            );
            try {
                out.write(buffer, 0, size);
            }
            finally {
                out.close();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            ByteBufferPool.release(buffer);
        }
    }
}
