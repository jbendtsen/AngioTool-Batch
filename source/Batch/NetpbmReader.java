package Batch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NetpbmReader
{
    static final int TYPE_NONE = 0;
    static final int TYPE_BIT = 1;
    static final int TYPE_BYTE = 2;
    static final int TYPE_SHORT = 3;
    static final int TYPE_FLOAT = 4;

    static final int ATTR_NONE = 0;
    static final int ATTR_WIDTH = 1;
    static final int ATTR_HEIGHT = 2;
    static final int ATTR_DEPTH = 3;
    static final int ATTR_MAXVAL = 4;
    static final int ATTR_TUPLTYPE = 5;

    public static RefVector<int[]> readArgbImages(FileChannel fc, int maxImages) throws IOException
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

                    if (c == '\n') {
                        isComment = false;
                        start = i + 1;
                    }

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
                                    if (token == "endhdr")
                                        dataOffset = i + 1;
                                    else if (token == "width")
                                        attrType = ATTR_WIDTH;
                                    else if (token == "height")
                                        attrType = ATTR_HEIGHT;
                                    else if (token == "depth")
                                        attrType = ATTR_DEPTH;
                                    else if (token == "maxval")
                                        attrType = ATTR_MAXVAL;
                                    else if (token == "tupltype")
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
                if (tupleType == "grayscale" || tupleType == "rgb" || tupleType == "rgb_alpha") {
                    sampleType = TYPE_BYTE;
                }
                else if (tupleType == "blackandwhite") {
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

            int[] pixels = new int[width * height + 2];

            if (isAscii) {
                if (sampleType == TYPE_FLOAT)
                    readPackedArgbFromAsciiFloat(header, dataOffset, fc, pixels, width, height, channels, Math.abs(maxval));
                else
                    readPackedArgbFromAscii(header, dataOffset, fc, pixels, width, height, channels, (int)maxval);
            }
            else {
                int size = width * height;
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

                if (sampleType == TYPE_BIT)
                    getPackedArgbFromBits(pixels, imageBuffer, size, width, height, channels, shouldInvert);
                else if (sampleType == TYPE_BYTE)
                    getPackedArgbFromBytes(pixels, imageBuffer, size, width, height, channels, (int)maxval);
                else if (sampleType == TYPE_SHORT)
                    getPackedArgbFromShorts(pixels, imageBuffer, size, width, height, channels, (int)maxval);
                else if (sampleType == TYPE_FLOAT && maxval < 0.0)
                    getPackedArgbFromLittleEndianFloats(pixels, imageBuffer, size, width, height, channels, -maxval);
                else if (sampleType == TYPE_FLOAT)
                    getPackedArgbFromFloats(pixels, imageBuffer, size, width, height, channels, maxval);

                ByteBufferPool.release(imageBuffer);
            }

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

    static void getPackedArgbFromBits(int[] pixels, byte[] imageBuffer, int size, int width, int height, int channels, boolean shouldInvert)
    {
        int xorValue = shouldInvert ? (1 << 24) - 1 : 0;
        int area = width * height;
        if (channels <= 1) {
            for (int i = 0; i < size; i++) {
                byte c = imageBuffer[i];
                for (int j = 0; j < 8 && i*8+j < area; j++)
                    pixels[i*8+j] = 0xff000000 | (xorValue ^ -((c >> (7-j)) & 1));
            }
        }
        else if (channels <= 3) {
            int ch = 0;
            int idx = 0;
            int argb = 0xff000000;
            for (int i = 0; i < size; i++) {
                byte c = imageBuffer[i];
                for (int j = 0; j < 8; j++) {
                    argb = (argb << 8) | (xorValue ^ -((c >> (7-j)) & 1));
                    if (++ch >= channels) {
                        ch = 0;
                        pixels[idx++] = argb;
                        if (idx >= area)
                            break;
                        argb = 0xff000000;
                    }
                }
            }
        }
        else {
            int ch = 0;
            int idx = 0;
            int argb = 0;
            for (int i = 0; i < size; i++) {
                byte c = imageBuffer[i];
                for (int j = 0; j < 8; j++) {
                    if (ch < 4)
                        argb = (argb << 8) | (xorValue ^ -((c >> (7-j)) & 1));
                    if (++ch >= channels) {
                        ch = 0;
                        pixels[idx++] = argb;
                        if (idx >= area)
                            break;
                    }
                }
            }
        }
    }

    static void getPackedArgbFromBytes(int[] pixels, byte[] imageBuffer, int size, int width, int height, int channels, int maxval)
    {
        maxval = maxval <= 0 ? 255 : maxval;
        int area = width * height;
        if (channels <= 1) {
            int len = Math.min(size, area);
            if (maxval == 255) {
                for (int i = 0; i < len; i++) {
                    int lum = imageBuffer[i] & 0xff;
                    pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                }
            }
            else {
                int factor = 256 / (maxval + 1);
                for (int i = 0; i < len; i++) {
                    int lum = Math.min(Math.max(factor * (imageBuffer[i] & 0xff), 0), 255);
                    pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                }
            }
        }
        else {
            int argb = 0xff000000;
            int ch = 0;
            int idx = 0;
            if (maxval == 255) {
                for (int i = 0; i < size; i++) {
                    if (ch < 4) {
                        int lum = imageBuffer[i] & 0xff;
                        argb = (argb << 8) | lum;
                    }
                    if (++ch >= channels) {
                        ch = 0;
                        pixels[idx++] = argb;
                        if (idx >= area)
                            break;
                        argb = 0xff000000;
                    }
                }
            }
            else {
                int factor = 256 / (maxval + 1);
                for (int i = 0; i < size; i++) {
                    if (ch < 4) {
                        int lum = Math.min(Math.max(factor * (imageBuffer[i] & 0xff), 0), 255);
                        argb = (argb << 8) | lum;
                    }
                    if (++ch >= channels) {
                        ch = 0;
                        pixels[idx++] = argb;
                        if (idx >= area)
                            break;
                        argb = 0xff000000;
                    }
                }
            }
        }
    }

    static void getPackedArgbFromShorts(int[] pixels, byte[] imageBuffer, int size, int width, int height, int channels, int maxval)
    {
        maxval = maxval <= 0 ? 65535 : maxval;
        int area = width * height;
        if (channels <= 1) {
            int len = Math.min(size / 2, area);
            if (maxval == 65535) {
                for (int i = 0; i < len; i++) {
                    int lum = imageBuffer[2*i] & 0xff;
                    pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                }
            }
            else {
                int factor = (maxval + 1) / 256;
                for (int i = 0; i < len; i++) {
                    int value = (imageBuffer[2*i] & 0xff) << 8 | (imageBuffer[2*i+1] & 0xff);
                    int lum = Math.min(Math.max(value / factor, 0), 255);
                    pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                }
            }
        }
        else {
            int argb = 0xff000000;
            int ch = 0;
            int idx = 0;
            if (maxval == 65535) {
                for (int i = 0; i < size; i += 2) {
                    if (ch < 4) {
                        int lum = imageBuffer[i] & 0xff;
                        argb = (argb << 8) | lum;
                    }
                    if (++ch >= channels) {
                        ch = 0;
                        pixels[idx++] = argb;
                        if (idx >= area)
                            break;
                        argb = 0xff000000;
                    }
                }
            }
            else {
                int factor = (maxval + 1) / 256;
                for (int i = 0; i < size; i += 2) {
                    if (ch < 4) {
                        int value = (imageBuffer[i] & 0xff) << 8 | (imageBuffer[i+1] & 0xff);
                        int lum = Math.min(Math.max(value / factor, 0), 255);
                        argb = (argb << 8) | lum;
                    }
                    if (++ch >= channels) {
                        ch = 0;
                        pixels[idx++] = argb;
                        if (idx >= area)
                            break;
                        argb = 0xff000000;
                    }
                }
            }
        }
    }

    static void getPackedArgbFromFloats(int[] pixels, byte[] imageBuffer, int size, int width, int height, int channels, double maxval)
    {
        maxval = maxval <= 0.0 ? 1.0 : maxval;
        float factor = 256.0f / (float)maxval;
        int area = width * height;
        if (channels <= 1) {
            int len = Math.min(size / 4, area);
            for (int i = 0; i < len; i++) {
                float value = Float.intBitsToFloat(
                    (imageBuffer[4*i] & 0xff) << 24 |
                    (imageBuffer[4*i+1] & 0xff) << 16 |
                    (imageBuffer[4*i+2] & 0xff) << 8 |
                    (imageBuffer[4*i+3] & 0xff)
                );
                int lum = Math.min(Math.max((int)(factor * value), 0), 255);
                pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
            }
        }
        else {
            int argb = 0xff000000;
            int ch = 0;
            int idx = 0;
            for (int i = 0; i < size; i += 4) {
                if (ch < 4) {
                    float value = Float.intBitsToFloat(
                        (imageBuffer[4*i] & 0xff) << 24 |
                        (imageBuffer[4*i+1] & 0xff) << 16 |
                        (imageBuffer[4*i+2] & 0xff) << 8 |
                        (imageBuffer[4*i+3] & 0xff)
                    );
                    int lum = Math.min(Math.max((int)(factor * value), 0), 255);
                    argb = (argb << 8) | lum;
                }
                if (++ch >= channels) {
                    ch = 0;
                    pixels[idx++] = argb;
                    if (idx >= area)
                        break;
                    argb = 0xff000000;
                }
            }
        }
    }

    static void getPackedArgbFromLittleEndianFloats(int[] pixels, byte[] imageBuffer, int size, int width, int height, int channels, double maxval)
    {
        maxval = maxval <= 0.0 ? 1.0 : maxval;
        float factor = 256.0f / (float)maxval;
        int area = width * height;
        if (channels <= 1) {
            int len = Math.min(size / 4, area);
            for (int i = 0; i < len; i++) {
                float value = Float.intBitsToFloat(
                    (imageBuffer[4*i] & 0xff) |
                    (imageBuffer[4*i+1] & 0xff) << 8 |
                    (imageBuffer[4*i+2] & 0xff) << 16 |
                    (imageBuffer[4*i+3] & 0xff) << 24
                );
                int lum = Math.min(Math.max((int)(factor * value), 0), 255);
                pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
            }
        }
        else {
            int argb = 0xff000000;
            int ch = 0;
            int idx = 0;
            for (int i = 0; i < size; i += 4) {
                if (ch < 4) {
                    float value = Float.intBitsToFloat(
                        (imageBuffer[4*i] & 0xff) |
                        (imageBuffer[4*i+1] & 0xff) << 8 |
                        (imageBuffer[4*i+2] & 0xff) << 16 |
                        (imageBuffer[4*i+3] & 0xff) << 24
                    );
                    int lum = Math.min(Math.max((int)(factor * value), 0), 255);
                    argb = (argb << 8) | lum;
                }
                if (++ch >= channels) {
                    ch = 0;
                    pixels[idx++] = argb;
                    if (idx >= area)
                        break;
                    argb = 0xff000000;
                }
            }
        }
    }
}
