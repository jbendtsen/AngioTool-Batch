package Pixels;

import static Pixels.BufferConverter.*;

import Utils.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.zip.Inflater;

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
    static final int TAG_PLANAR_CONFIGURATION = 284;
    static final int TAG_PREDICTOR = 317;
    static final int TAG_SAMPLE_FORMAT = 339;
    static final int TAG_MIN_VALUE = 340;
    static final int TAG_MAX_VALUE = 341;

    static final int COMPRESSION_LZW = 5;
    static final int COMPRESSION_ADOBE_DEFLATE = 8;
    static final int COMPRESSION_PACKBITS = 32773;
    static final int COMPRESSION_DEFLATE = 32946;

    static final int DC_NONE = 0;
    static final int DC_LZW = 1;
    static final int DC_PACKBITS = 2;
    static final int DC_DEFLATE = 3;

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
            ifdOffset = readImage(images, ifdOffset, isLittleEndian, fc, bb, buf, shouldAllocateWithRecycler);
            if (ifdOffset == 0)
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
        int trueTagCount = getShort(buf, 0, isLittleEndian);
        //System.out.println("trueTagCount = " + trueTagCount);

        int width = 0;
        int height = 0;
        int sampleType = TYPE_NONE;
        int sampleLen = 0;
        int channels = 0;
        int compression = 0;
        int firstStripOffset = 0;
        int[] stripOffsets = null;
        int firstStripCount = 0;
        int[] stripCounts = null;
        float minValue = 0f;
        float maxValue = 0f;
        boolean isFloat = false;
        boolean isPlanar = false;
        boolean shouldDiffOrInvert = false;

        int tagIndex = 0;
        while (tagIndex < trueTagCount) {
            int nTags = Math.min(trueTagCount - tagIndex, extraOff / 12);

            bb.position(0);
            bb.limit(nTags * 12);
            fc.read(bb);

            for (int i = 0, t = 0; i < nTags; i++, t += 12) {
                int attr = getShort(buf, t, isLittleEndian);
                int type = getShort(buf, t+2, isLittleEndian);
                int count = getInt(buf, t+4, isLittleEndian);
                //System.out.println("attr = " + attr + ", type = " + type + ", count = " + count);

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
                    else if (attr == TAG_PLANAR_CONFIGURATION)
                        isPlanar = value == 2;
                    else if (attr == TAG_PHOTOMETRIC_INTERPRETATION)
                        shouldDiffOrInvert = value == 0;
                    else if (attr == TAG_COMPRESSION)
                        compression = value;
                    else if (attr == TAG_PREDICTOR)
                        shouldDiffOrInvert = value == 2;
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
                        bb.limit(Math.min(extraOff + size, buf.length));
                        bb.position(extraOff);
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
                    }
                    else if (attr == TAG_STRIP_OFFSETS) {
                        stripOffsets = getArray(buf, extraOff, isLittleEndian, type, count);
                        firstStripOffset = stripOffsets[0];
                    }
                    else if (attr == TAG_STRIP_COUNTS) {
                        stripCounts = getArray(buf, extraOff, isLittleEndian, type, count);
                        firstStripCount = stripCounts[0];
                    }
                }
            }

            tagIndex += nTags;
        }

        System.out.println("TiffReader: " +
            "width = " + width +
            ", height = " + height +
            ", sampleType = " + sampleType +
            ", sampleLen = " + sampleLen +
            ", channels = " + channels +
            ", compression = " + compression +
            ", firstStripOffset = " + firstStripOffset +
            ", firstStripCount = " + firstStripCount +
            ", minValue = " + minValue +
            ", maxValue = " + maxValue +
            ", isFloat = " + isFloat +
            ", isPlanar = " + isPlanar +
            ", shouldDiffOrInvert = " + shouldDiffOrInvert
        );

        System.out.println("stripOffsets: " + BatchUtils.formatIntArray(stripOffsets, "" + firstStripOffset));
        System.out.println("stripCounts: " + BatchUtils.formatIntArray(stripCounts, "" + firstStripCount));

        fc.position(ifdOffset + trueTagCount * 12 + 2);
        bb.position(0);
        bb.limit(4);
        fc.read(bb);

        int nextIfdOffset = getInt(buf, 0, isLittleEndian);

        if (width <= 0 || height <= 0)
            return nextIfdOffset;

        int dcMode = DC_NONE;
        switch (compression) {
            case 0:
            case 1:
                break;
            case COMPRESSION_LZW:
                dcMode = DC_LZW;
                break;
            case COMPRESSION_PACKBITS:
                dcMode = DC_PACKBITS;
                break;
            case COMPRESSION_DEFLATE:
            case COMPRESSION_ADOBE_DEFLATE:
                dcMode = DC_DEFLATE;
                break;
            default:
                throw new IOException("TiffReader: unsupported compression type (" + compression + ")");
        }

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

        if (dcMode == DC_NONE) {
            fc.position(firstStripOffset);
            fc.read(ByteBuffer.wrap(imageData, 0, size));
        }
        else {
            decompress(dcMode, fc, imageData, size, stripOffsets, stripCounts);
        }

        int[] pixels = shouldAllocateWithRecycler ?
            IntBufferPool.acquireAsIs(area + 2) :
            new int[area + 2];

        convertToPackedArgb(
            sampleType,
            isLittleEndian,
            isPlanar,
            shouldDiffOrInvert,
            pixels,
            imageData,
            size,
            width,
            height,
            channels,
            maxValue
        );

        ByteBufferPool.release(imageData);

        // When obtaining this buffer for the first time, we don't know the width or height,
        // which means we don't know how long the buffer is supposed to be.
        // That's why we write width and height to [length - 2] and [length - 1] instead of [area] and [area + 1]
        pixels[pixels.length - 2] = width;
        pixels[pixels.length - 1] = height;
        images.add(pixels);

        return nextIfdOffset;
    }

    static void decompress(
        int mode,
        FileChannel fc,
        byte[] outData,
        int outSize,
        int[] offsets,
        int[] lengths
    ) throws IOException
    {
        int nStrips = Math.min(offsets.length, lengths.length);
        int inputSize = getLargestElement(lengths, nStrips);
        byte[] strip = ByteBufferPool.acquireAsIs(inputSize);
        ByteBuffer bb = ByteBuffer.wrap(strip);

        BitReader br = new BitReader();
        IntVector lzwTableAndHeap = new IntVector();

        int outOffset = 0;

        for (int i = 0; i < nStrips; i++) {
            bb.position(0);
            bb.limit(lengths[i]);
            fc.position(offsets[i]);
            fc.read(bb);

            if (mode == DC_LZW)
                outOffset = lzwDecompressStrip(outData, outOffset, outSize, strip, lengths[i], br, lzwTableAndHeap);
            if (mode == DC_PACKBITS)
                outOffset = packbitsDecompressStrip(outData, outOffset, outSize, strip, lengths[i]);
            if (mode == DC_DEFLATE)
                outOffset = deflateDecompressStrip(outData, outOffset, outSize, strip, lengths[i]);
        }

        ByteBufferPool.release(strip);
    }

    static int lzwDecompressStrip(byte[] outData, int outOffset, int outSize, byte[] strip, int length, BitReader br, IntVector lzwTableAndHeap)
    {
        br.reset(strip, 0, length);

        final int lzwTableSize = 2 * 16384;
        int lzwHeapSize = 256;
        lzwTableAndHeap.resize(lzwTableSize + lzwHeapSize);
        Arrays.fill(lzwTableAndHeap.buf, 0, lzwTableSize + lzwHeapSize, 0);

        int oldCode = 0;
        int bitsToRead = 9;
        int nextSymbol = 258;

        while (true) {
            int code = br.getBits(bitsToRead);
            //System.out.println("LZW code: " + code);
            if (code == 257 || code == -1)
                break;

            boolean isClear = code == 256;
            if (isClear) {
                for (int i = 0; i < 512; i += 2) {
                    lzwTableAndHeap.buf[i] = lzwTableSize + (i >> 1);
                    lzwTableAndHeap.buf[i+1] = 1;
                }
                for (int i = 0; i < 256; i++)
                    lzwTableAndHeap.buf[lzwTableSize + i] = i;

                nextSymbol = 258;
                bitsToRead = 9;
                oldCode = code = br.getBits(bitsToRead);
                if (code == 257 || code == -1)
                    break;
            }

            if (code < nextSymbol || isClear) {
                int[] th = lzwTableAndHeap.buf;
                int off = th[2*code];
                int len = th[2*code+1];
                for (int i = 0; i < len && outOffset+i < outSize; i++)
                    outData[outOffset+i] = (byte)(th[off + (i>>2)] >>> ((i&3)*8));

                outOffset += len;
                if (outOffset >= outSize)
                    break;

                if (isClear)
                    continue;

                int oldOff = th[2*oldCode];
                int oldLen = th[2*oldCode+1];
                int firstCurrent = th[off] & 0xff;

                int lastElem = oldOff + (oldLen >> 2);
                int nextSymbolOffset = lzwTableAndHeap.addFromSelf(oldOff, (oldLen + 3) >> 2);

                if ((oldLen & 3) == 0)
                    lzwTableAndHeap.add(firstCurrent);
                else
                    lzwTableAndHeap.buf[lastElem] |= firstCurrent << (8*(oldLen & 3));

                lzwTableAndHeap.buf[2*nextSymbol] = nextSymbolOffset;
                lzwTableAndHeap.buf[2*nextSymbol+1] = oldLen + 1;
            }
            else {
                int oldOff = lzwTableAndHeap.buf[2*oldCode];
                int oldLen = lzwTableAndHeap.buf[2*oldCode+1];
                int firstOld = lzwTableAndHeap.buf[oldOff] & 0xff;

                int lastElem = oldOff + (oldLen >> 2);
                int nextSymbolOffset = lzwTableAndHeap.addFromSelf(oldOff, (oldLen + 3) >> 2);

                if ((oldLen & 3) == 0)
                    lzwTableAndHeap.add(firstOld);
                else
                    lzwTableAndHeap.buf[lastElem] |= firstOld << (8*(oldLen & 3));

                int[] th = lzwTableAndHeap.buf;
                th[2*nextSymbol] = nextSymbolOffset;
                th[2*nextSymbol+1] = oldLen + 1;

                int len = oldLen + 1;
                for (int i = 0; i < len && outOffset+i < outSize; i++)
                    outData[outOffset+i] = (byte)(th[nextSymbolOffset + (i>>2)] >>> ((i&3)*8));

                outOffset += len;
                if (outOffset >= outSize)
                    break;
            }

            bitsToRead = 32 - Integer.numberOfLeadingZeros(++nextSymbol + 1);
            oldCode = code;
        }

        return outOffset;
    }

    static int packbitsDecompressStrip(byte[] outData, int outOffset, int outSize, byte[] strip, int length)
    {
        boolean isRepeat = false;
        int left = 0;
        for (int i = 0; i < length && outOffset < outSize; i++) {
            if (left == 0) {
                int mode = strip[i];
                isRepeat = mode < 0;
                left = Math.abs(mode) + 1;
                continue;
            }

            if (isRepeat) {
                byte b = strip[i];
                for (int j = 0; j < left && outOffset+j < outSize; j++)
                    outData[outOffset+j] = b;
                outOffset += left;
                left = 0;
            }
            else {
                outData[outOffset++] = strip[i];
                left--;
            }
        }

        return outOffset;
    }

    static int deflateDecompressStrip(byte[] outData, int outOffset, int outSize, byte[] strip, int length)
    {
        Inflater inflater = new Inflater();
        inflater.setInput(strip, 0, length);

        try {
            while (!inflater.finished() && outOffset < outSize) {
                int res = inflater.inflate(outData, outOffset, outSize - outOffset);
                if (res <= 0)
                    break;
                outOffset += res;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        inflater.end();
        return outOffset;
    }

    static int getLargestElement(int[] array, int count)
    {
        int result = 0;
        for (int i = 0; i < count; i++)
            result = Math.max(result, array[i]);
        return result;
    }

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

    static int[] getArray(byte[] buf, int off, boolean isLittleEndian, int type, int count)
    {
        int[] array = new int[count];
        if (type == 3) {
            for (int i = 0; i < count; i++)
                array[i] = getShort(buf, off + i * 2, isLittleEndian);
        }
        else {
            for (int i = 0; i < count; i++)
                array[i] = getInt(buf, off + i * 4, isLittleEndian);
        }
        return array;
    }
}
