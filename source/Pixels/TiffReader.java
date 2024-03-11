package Pixels;

import static Pixels.BufferConverter.*;

import Utils.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TiffReader
{
    static final int TAG_WIDTH = 256;
    static final int TAG_HEIGHT = 257;
    static final int TAG_BITS_PER_SAMPLE = 258;
    static final int TAG_COMPRESSION = 259;
    static final int TAG_PHOTOMETRIC_INTERPRETATION = 262;
    static final int TAG_STRIP_OFFSETS = 273;
    static final int TAG_SAMPLES_PER_PIXEL = 277;
    static final int TAG_STRIP_COUNTS = 279;
    static final int TAG_PREDICTOR = 317;
    static final int TAG_SAMPLE_FORMAT = 339;
    static final int TAG_MIN_VALUE = 340;
    static final int TAG_MAX_VALUE = 341;

    static final int COMPRESSION_LZW = 5;
    static final int COMPRESSION_ADOBE_DEFLATE = 8;
    static final int COMPRESSION_PACKBITS = 32773;
    static final int COMPRESSION_DEFLATE = 32946;

    public static RefVector<int[]> readArgbImages(
        FileChannel fc,
        int maxImages,
        boolean shouldAllocateWithRecycler
    ) throws IOException
    {
        byte[] buf = new byte[4096];
        ByteBuffer bb = ByteBuffer.wrap(buf);

        fc.position(0);
        bb.limit(8);
        fc.read(bb);

        boolean isLittleEndian;
        if (buf[0] == 'I' && buf[1] == 'I')
            isLittleEndian = true;
        else if (buf[1] == 'M' && buf[1] == 'M')
            isLittleEndian = false;
        else
            return null;

        RefVector<int[]> images = new RefVector<>(int[].class);
        int ifdOffset = getInt(buf, 4, isLittleEndian);

        for (int i = 0; i < maxImages; i++) {
            int nextIfdOffset = readImage(images, ifdOffset, isLittleEndian, fc, bb, buf, shouldAllocateWithRecycler);
            if (nextIfdOffset == 0)
                break;
        }

        return images;
    }

    static int readImage(
        RefVector<int[]> images,
        int ifdOffset,
        boolean isLittleEndian,
        FileChannel fc,
        ByteBuffer bb,
        byte[] buf,
        boolean shouldAllocateWithRecycler
    ) throws IOException
    {
        fc.position(ifdOffset);
        bb.position(0);
        bb.limit(2);
        fc.read(bb);

        final int extraOff = buf.length / 2;
        int trueTagCount = getShort(buf, 8, isLittleEndian);
        int nTags = Math.min(trueTagCount, extraOff / 12);

        bb.position(0);
        bb.limit(nTags * 12);
        fc.read(bb);

        int width = 0;
        int height = 0;
        int sampleType = TYPE_NONE;
        int sampleLen = 0;
        int channels = 0;
        int compression = 0;
        int firstStripOffset = 0;
        int firstStripCount = 0;
        float minValue = 0f;
        float maxValue = 0f;
        boolean isFloat = false;
        boolean shouldDiff = false;
        boolean shouldInvert = false;

        for (int i = 0, t = 0; i < nTags; i++, t += 12) {
            int attr = getShort(buf, t, isLittleEndian);
            int type = getShort(buf, t+2, isLittleEndian);
            int count = getInt(buf, t+4, isLittleEndian);
            if (count == 1) {
                int value = type == 3 ? getShort(buf, t+8, isLittleEndian) : getInt(buf, t+8, isLittleEndian);
                if (attr == TAG_WIDTH)
                    width = value;
                else if (attr == TAG_HEIGHT)
                    height = value;
                else if (attr == TAG_BITS_PER_SAMPLE)
                    sampleLen = value;
                else if (attr == TAG_SAMPLES_PER_PIXEL)
                    channels = value;
                else if (attr == TAG_PHOTOMETRIC_INTERPRETATION)
                    shouldInvert = value == 0;
                else if (attr == TAG_COMPRESSION)
                    compression = value;
                else if (attr == TAG_PREDICTOR)
                    shouldDiff = value == 2;
                else if (attr == TAG_SAMPLE_FORMAT)
                    isFloat = value == 3;
                else if (attr == TAG_MIN_VALUE)
                    minValue = Float.intBitsToFloat(value);
                else if (attr == TAG_MAX_VALUE)
                    maxValue = Float.intBitsToFloat(value);
                else if (attr == TAG_STRIP_OFFSETS)
                    firstStripOffset = value;
                else if (attr == TAG_STRIP_COUNTS)
                    firstStripCount = value;
            }
            else if (
                attr == TAG_BITS_PER_SAMPLE ||
                attr == TAG_STRIP_OFFSETS ||
                attr == TAG_STRIP_COUNTS ||
                attr == TAG_MIN_VALUE ||
                attr == TAG_MAX_VALUE
            ) {
                int offset = getInt(buf, t+8, isLittleEndian);
                int size = count * (type == 3 ? 2 : 4);

                long oldPos = fc.position();
                try {
                    fc.position(offset);
                    bb.position(extraOff);
                    bb.limit(Math.min(size, buf.length - extraOff));
                    fc.read(bb);
                }
                catch (IOException ex) {
                    continue;
                }
                finally {
                    fc.position(oldPos);
                }

                if (attr == TAG_BITS_PER_SAMPLE) {
                    int firstSampleLen = type == 3 ? getShort(buf, extraOff, isLittleEndian) : getInt(buf, extraOff, isLittleEndian);

                    sampleLen = firstSampleLen;
                    channels = count;

                    System.out.println("sampleLen: " + firstSampleLen + ", channels: " + count);
                }
            }
        }

        fc.position(ifdOffset + trueTagCount * 12 + 2);
        bb.position(0);
        bb.limit(4);
        fc.read(bb);

        int nextIfdOffset = getInt(buf, 0, isLittleEndian);

        if (width <= 0 || height <= 0)
            return nextIfdOffset;

        if (sampleLen == 1)
            sampleType = TYPE_BIT;
        else if (sampleLen == 8)
            sampleType = TYPE_BYTE;
        else if (sampleLen == 16)
            sampleType = TYPE_SHORT;
        else
            sampleType = isFloat ? TYPE_FLOAT : TYPE_BYTE;

        int area = width * height;
        int size = area;
        if (sampleType == TYPE_BIT)
            size = (size + 7) / 8;
        else if (sampleType == TYPE_SHORT)
            size *= 2;
        else if (sampleType == TYPE_FLOAT)
            size *= 4;

        channels = Math.max(channels, 1);
        size *= channels;

        byte[] imageData = ByteBufferPool.acquireAsIs(size);
        fc.position(firstStripOffset);

        if (compression == COMPRESSION_LZW) {
            lzwDecompress();
        }
        else if (compression == COMPRESSION_PACKBITS) {
            packbitsDecompress();
        }
        else if (compression == COMPRESSION_DEFLATE || compression == COMPRESSION_ADOBE_DEFLATE) {
            deflateDecompress();
        }
        else {
            ByteBuffer dataBb = ByteBuffer.wrap(imageData);
            dataBb.limit(size);
            fc.read(dataBb);
        }

        int[] pixels = shouldAllocateWithRecycler ?
            IntBufferPool.acquireAsIs(area + 2) :
            new int[area + 2];

        convertToPackedArgb(
            sampleType,
            isLittleEndian,
            pixels,
            imageData,
            size,
            width,
            height,
            channels,
            maxValue,
            shouldInvert
        );

        pixels[area] = width;
        pixels[area+1] = height;
        images.add(pixels);

        return nextIfdOffset;
    }

    static void lzwDecompress() {}
    static void packbitsDecompress() {}
    static void deflateDecompress() {}

    static int getShort(byte[] buf, int off, boolean isLittleEndian)
    {
        if (isLittleEndian) {
            return
                (buf[off] & 0xff) |
                (buf[off+1] & 0xff) << 8;
        }
        return
            (buf[off] & 0xff) << 8 |
            (buf[off+1] & 0xff);
    }

    static int getInt(byte[] buf, int off, boolean isLittleEndian)
    {
        if (isLittleEndian) {
            return
                (buf[off] & 0xff) |
                (buf[off+1] & 0xff) << 8 |
                (buf[off+2] & 0xff) << 16 |
                (buf[off+3] & 0xff) << 24;
        }
        return
            (buf[off] & 0xff) << 24 |
            (buf[off+1] & 0xff) << 16 |
            (buf[off+2] & 0xff) << 8 |
            (buf[off+3] & 0xff);
    }
}
