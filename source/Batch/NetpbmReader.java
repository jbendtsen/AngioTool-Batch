package Batch;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NetpbmReader
{
    static final int TYPE_NONE = 0;
    static final int TYPE_BIT = 1;
    static final int TYPE_BYTE = 2;
    static final int TYPE_SHORT = 3;
    static final int TYPE_INT = 4;
    static final int TYPE_FLOAT = 5;

    static final int ATTR_NONE = 0;
    static final int ATTR_WIDTH = 1;
    static final int ATTR_HEIGHT = 2;
    static final int ATTR_DEPTH = 3;
    static final int ATTR_MAXVAL = 4;
    static final int ATTR_TUPLTYPE = 5;

    public static RefVector<int[]> readArgbImages(FileChannel fc)
    {
        RefVector<int[]> images = new RefVector<int[]>(int[].class);
        ByteVectorOutputStream imageBuffer = new ByteVectorOutputStream();

        byte[] header = new byte[256];
        ByteBuffer hbb = ByteBuffer.wrap(header);
        int res;

        boolean finished = false;
        do {
            boolean isComment = false;
            boolean isAscii = false;
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

            if (sampleType == TYPE_BYTE && maxval > 255)
                sampleType = TYPE_SHORT;

            int leftover = pos - dataOffset;
            int[] pixels = new int[width * height + 2];

            if (isAscii) {
                if (sampleType == TYPE_FLOAT)
                    readPackedArgbFromAsciiFloat(header, leftover, fc, pixels, width, height, channels, Math.abs(maxval));
                else
                    readPackedArgbFromAscii(header, leftover, fc, pixels, width, height, channels, (int)maxval);
            }
            else {
                int size = width * height;
                if (channels > 1)
                    size *= channels;

                if (sampleType == TYPE_BIT)
                    size = (size + 7) / 8;
                else if (sampleType == TYPE_SHORT)
                    size *= 2;
                else if (sampleType == TYPE_INT || sampleType == TYPE_FLOAT)
                    size *= 4;

                imageBuffer.resize(size);
                System.arraycopy(header, header.length - leftover, imageBuffer.buf, 0, Math.min(leftover, size));

                if (size > leftover) {
                    ByteBuffer tempBb = ByteBuffer.wrap(imageBuffer.buf, leftover, size - leftover);
                    while (res > 0)
                        res = fc.read(tempBb);
                }

                if (sampleType == TYPE_BIT)
                    getPackedArgbFromBits(pixels, imageBuffer.buf, width, height, channels);
                else if (sampleType == TYPE_BYTE)
                    getPackedArgbFromBytes(pixels, imageBuffer.buf, width, height, channels);
                else if (sampleType == TYPE_SHORT)
                    getPackedArgbFromShorts(pixels, imageBuffer.buf, width, height, channels);
                else if (sampleType == TYPE_INT)
                    getPackedArgbFromInts(pixels, imageBuffer.buf, width, height, channels);
                else if (sampleType == TYPE_FLOAT && maxval < 0.0)
                    getPackedArgbFromLittleEndianFloats(pixels, imageBuffer.buf, width, height, channels, -maxval);
                else if (sampleType == TYPE_FLOAT)
                    getPackedArgbFromFloats(pixels, imageBuffer.buf, width, height, channels, maxval);
            }

            pixels[pixels.length - 2] = width;
            pixels[pixels.length - 1] = height;
            images.add(pixels);
        } while (res > 0);

        return images;
    }
}
