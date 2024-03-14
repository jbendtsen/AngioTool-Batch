package Pixels;

import static Pixels.BufferConverter.*;

import Utils.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class NetpbmReader
{
    static final int ATTR_NONE = 0;
    static final int ATTR_WIDTH = 1;
    static final int ATTR_HEIGHT = 2;
    static final int ATTR_DEPTH = 3;
    static final int ATTR_MAXVAL = 4;
    static final int ATTR_TUPLTYPE = 5;

    public static RefVector<int[]> acquireArgbImages(FileChannel fc, int maxImages) throws IOException
    {
        RefVector<int[]> images = new RefVector<int[]>(int[].class);

        byte[] header = new byte[4096];
        ByteBuffer hbb = ByteBuffer.wrap(header);
        int res;

        boolean finished = false;
        do {
            boolean isComment = false;
            boolean isAscii = false;
            boolean shouldInvert = false;
            byte pbmType = 0;
            String tupleType = null;
            int width = 0;
            int height = 0;
            double maxval = 255.0;
            int channels = 1;
            int sampleType = TYPE_NONE;
            int attrType = ATTR_NONE;
            int field = 0;
            int dataOffset = 0;
            int pos = 0;

            String token = "";
            do {
                int start = 0;

                res = fc.read(hbb);

                for (int i = 0; i < res && dataOffset == 0; i++) {
                    byte c = header[i];
                    if (pos + i == 0) {
                        if (c != 'P') {
                            finished = true;
                            break;
                        }
                        continue;
                    }
                    if (pos + i == 1) {
                        pbmType = c;
                        continue;
                    }

                    if (c == '#')
                        isComment = true;
                    if (c == '\n')
                        isComment = false;
                    if (isComment)
                        continue;

                    if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                        if (start < i) {
                            token += new String(header, start, i - start).toLowerCase();
                            field++;
                            if (pbmType == '7') {
                                if (attrType != ATTR_NONE) {
                                    switch (attrType) {
                                        case ATTR_WIDTH:
                                            width = BatchUtils.parseInt(token, 0);
                                            break;
                                        case ATTR_HEIGHT:
                                            height = BatchUtils.parseInt(token, 0);
                                            break;
                                        case ATTR_DEPTH:
                                            channels = BatchUtils.parseInt(token, 0);
                                            break;
                                        case ATTR_MAXVAL:
                                            maxval = BatchUtils.parseDouble(token, 255.0);
                                            break;
                                        case ATTR_TUPLTYPE:
                                            tupleType = token;
                                            break;
                                    }
                                    attrType = ATTR_NONE;
                                }
                                else {
                                    if ("endhdr".equals(token))
                                        dataOffset = i + 1;
                                    else if ("width".equals(token))
                                        attrType = ATTR_WIDTH;
                                    else if ("height".equals(token))
                                        attrType = ATTR_HEIGHT;
                                    else if ("depth".equals(token))
                                        attrType = ATTR_DEPTH;
                                    else if ("maxval".equals(token))
                                        attrType = ATTR_MAXVAL;
                                    else if ("tupltype".equals(token))
                                        attrType = ATTR_TUPLTYPE;
                                }
                            }
                            else {
                                if (field == 2)
                                    width = BatchUtils.parseInt(token, 0);
                                else if (field == 3)
                                    height = BatchUtils.parseInt(token, 0);
                                else if (field == 4)
                                    maxval = BatchUtils.parseDouble(token, 255.0);

                                if (field >= 4 || (field == 3 && (pbmType == '1' || pbmType == '4')))
                                    dataOffset = i + 1;
                            }
                            token = "";
                        }
                        start = i + 1;
                    }
                }

                if (res > 0)
                    pos += res;

                if (dataOffset == 0 && start < res && !isComment)
                    token += new String(header, start, res - start);

            } while (!finished && res > 0 && dataOffset == 0);

            if (dataOffset <= 0)
                break;

            if (pbmType >= '1' && pbmType <= '3')
                isAscii = true;

            if (pbmType == '1' || pbmType == '4') {
                channels = 1;
                shouldInvert = true;
                sampleType = TYPE_BIT;
            }
            else if (pbmType == '2' || pbmType == '5') {
                channels = 1;
                sampleType = TYPE_BYTE;
            }
            else if (pbmType == '3' || pbmType == '6') {
                channels = 3;
                sampleType = TYPE_BYTE;
            }
            else if (pbmType == 'F') {
                channels = 3;
                sampleType = TYPE_FLOAT;
            }
            else if (pbmType == 'f') {
                channels = 1;
                sampleType = TYPE_FLOAT;
            }

            if (tupleType != null) {
                if ("grayscale".equals(tupleType) || "rgb".equals(tupleType) || "rgb_alpha".equals(tupleType)) {
                    sampleType = TYPE_BYTE;
                }
                else if ("blackandwhite".equals(tupleType)) {
                    sampleType = TYPE_BIT;
                }

                // non-standard
                if (tupleType.contains("float")) {
                    sampleType = TYPE_FLOAT;
                }
                if (tupleType.contains("text") || tupleType.contains("ascii")) {
                    isAscii = true;
                }
            }

            if (sampleType == TYPE_BYTE) {
                if (maxval > 255)
                    sampleType = TYPE_SHORT;
                else if (maxval == 1)
                    sampleType = TYPE_BIT;
            }

            /*
            System.out.println(
                "isComment = " + isComment +
                ", isAscii = " + isAscii +
                ", shouldInvert = " + shouldInvert +
                ", pbmType = " + pbmType +
                ", tupleType = " + tupleType +
                ", width = " + width +
                ", height = " + height +
                ", maxval = " + maxval +
                ", channels = " + channels +
                ", sampleType = " + sampleType +
                ", attrType = " + attrType +
                ", field = " + field +
                ", dataOffset = " + dataOffset +
                ", pos = " + pos
            );
            */

            int area = width * height;
            int[] pixels = IntBufferPool.acquireAsIs(area + 2);

            if (isAscii) {
                if (sampleType == TYPE_FLOAT)
                    readPackedArgbFromAsciiFloat(header, dataOffset, fc, pixels, width, height, channels, Math.abs(maxval));
                else
                    readPackedArgbFromAscii(header, dataOffset, fc, pixels, width, height, channels, (int)maxval);
            }
            else {
                int size = area;
                if (channels > 1)
                    size *= channels;

                if (sampleType == TYPE_BIT)
                    size = (size + 7) / 8;
                else if (sampleType == TYPE_SHORT)
                    size *= 2;
                else if (sampleType == TYPE_FLOAT)
                    size *= 4;

                byte[] imageBuffer = ByteBufferPool.acquireAsIs(size);

                int leftover = header.length - dataOffset;
                System.arraycopy(header, dataOffset, imageBuffer, 0, Math.min(leftover, size));

                if (size > leftover) {
                    ByteBuffer tempBb = ByteBuffer.wrap(imageBuffer, leftover, size - leftover);
                    while (res > 0)
                        res = fc.read(tempBb);
                }

                boolean isLittleEndian = false;
                if (maxval < 0.0) {
                    isLittleEndian = true;
                    maxval = -maxval;
                }

                int pixelsFilled = convertToPackedArgb(
                    sampleType,
                    isLittleEndian,
                    false,
                    shouldInvert && sampleType == TYPE_BIT,
                    pixels,
                    imageBuffer,
                    size,
                    width,
                    height,
                    channels,
                    (float)maxval
                );

                if (pixelsFilled < area)
                    Arrays.fill(pixels, pixelsFilled, area, 0);

                ByteBufferPool.release(imageBuffer);
            }

            // When obtaining this buffer for the first time, we don't know the width or height,
            // which means we don't know how long the buffer is supposed to be.
            // That's why we write width and height to [length - 2] and [length - 1] instead of [area] and [area + 1]
            pixels[pixels.length - 2] = width;
            pixels[pixels.length - 1] = height;
            images.add(pixels);
        } while (res > 0 && images.size < maxImages);

        return images;
    }

    static void readPackedArgbFromAsciiFloat(
        byte[] array,
        int offset,
        FileChannel fc,
        int[] pixels,
        int width,
        int height,
        int channels,
        double maxval
    ) throws IOException
    {
        maxval = maxval == 0.0 ? 1.0 : maxval;
        ByteBuffer bb = ByteBuffer.wrap(array);

        int idx = 0;
        int ch = 0;
        int whole = 0;
        int frac = 0;
        int exp = 0;
        int digitsWhole = 0;
        int digitsFrac = 0;
        int mode = 0;
        int isNeg = 0;

        while (true) {
            if (offset == 0) {
                bb.position(0);
                int res;
                do {
                    res = fc.read(bb);
                } while (res > 0);

                if (res == -1)
                    break;
            }
            byte c = array[offset];

            if (c >= '0' && c <= '9') {
                if (mode == 0) {
                    whole = whole * 10 + c - '0';
                    digitsWhole++;
                }
                else if (mode == 1) {
                    frac = frac * 10 + c - '0';
                    digitsFrac++;
                }
                else {
                    exp = exp * 10 + c - '0';
                }
            }
            else if (c == '.' && mode == 0) {
                mode = 1;
            }
            else if (c == 'e' || c == 'E') {
                mode = 2;
            }
            else if (c == '-') {
                isNeg |= (mode >> 1) + 1;
            }
            else if (c != '+') {
                if (digitsWhole + digitsFrac > 0) {
                    double f = frac * Math.pow(10, -digitsFrac);
                    f += whole;
                    if (exp > 0)
                        f *= Math.pow(10, (isNeg & 2) == 2 ? -exp : exp);
                    if ((isNeg & 1) == 1)
                        f = -f;

                    int value = 0;
                    if (f >= 0.0 && f < maxval)
                        value = Math.min(Math.max((int)(256.0 * f / maxval), 0), 255);
                    else if (f >= maxval)
                        value = 255;

                    if (channels <= 1) {
                        pixels[idx++] = 0xff000000 | (value << 16) | (value << 8) | value;
                    }
                    else if (channels <= 3) {
                        pixels[idx] = 0xff000000 | (pixels[idx] << 8) | value;
                        if (++ch >= channels) {
                            idx++;
                            ch = 0;
                        }
                    }
                    else {
                        if (ch < 4)
                            pixels[idx] = (pixels[idx] << 8) | value;
                        if (++ch >= channels) {
                            idx++;
                            ch = 0;
                        }
                    }

                    if (idx >= width * height)
                        break;

                    whole = 0;
                    frac = 0;
                    digitsWhole = 0;
                    digitsFrac = 0;
                }
                exp = 0;
                isNeg = 0;
                mode = 0;
            }

            offset = (offset + 1) % array.length;
        }
    }

    static void readPackedArgbFromAscii(
        byte[] array,
        int offset,
        FileChannel fc,
        int[] pixels,
        int width,
        int height,
        int channels,
        int maxval
    ) throws IOException
    {
        maxval = maxval == 0 ? 255 : maxval;
        ByteBuffer bb = ByteBuffer.wrap(array);

        int argb = 0xff000000;
        int idx = 0;
        int ch = 0;
        int n = 0;
        boolean isNeg = false;

        while (true) {
            if (offset == 0) {
                bb.position(0);
                int res;
                do {
                    res = fc.read(bb);
                } while (res > 0);

                if (res == -1)
                    break;
            }

            byte c = array[offset];
            if (c == '-') {
                isNeg = true;
            }
            else if (c >= '0' && c <= '9') {
                n = n * 10 + c - '0';
            }
            else if (c != '+') {
                if (isNeg || n < 0)
                    n = 0;

                if (n >= maxval)
                    n = 255;
                else if (maxval != 255)
                    n = Math.min(Math.max((int)(256.0 * n / maxval), 0), 255);

                if (channels <= 1) {
                    pixels[idx++] = 0xff000000 | (n << 16) | (n << 8) | n;
                }
                else if (channels <= 3) {
                    argb = (argb << 8) | n;
                    if (++ch >= channels) {
                        ch = 0;
                        pixels[idx++] = argb;
                        argb = 0xff000000;
                    }
                }
                else {
                    if (ch < 4)
                        argb = (argb << 8) | n;
                    if (++ch >= channels) {
                        ch = 0;
                        pixels[idx++] = argb;
                    }
                }

                if (idx >= width * height)
                    break;

                n = 0;
                isNeg = false;
            }

            offset = (offset + 1) % array.length;
        }
    }
}
