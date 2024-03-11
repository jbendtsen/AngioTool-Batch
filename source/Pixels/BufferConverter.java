package Pixels;

public class BufferConverter
{
    static final int TYPE_NONE = 0;
    static final int TYPE_BIT = 1;
    static final int TYPE_BYTE = 2;
    static final int TYPE_SHORT = 3;
    static final int TYPE_FLOAT = 4;

    public static void convertToPackedArgb(
        int sampleType,
        boolean isLittleEndian,
        int[] pixels,
        byte[] imageBuffer,
        int size,
        int width,
        int height,
        int channels,
        float maxval,
        boolean shouldInvert
    ) {
        if (sampleType == TYPE_BIT)
            BufferConverter.getPackedArgbFromBits(pixels, imageBuffer, size, width, height, channels, shouldInvert);
        else if (sampleType == TYPE_BYTE)
            BufferConverter.getPackedArgbFromBytes(pixels, imageBuffer, size, width, height, channels, (int)maxval);
        else if (sampleType == TYPE_SHORT && isLittleEndian)
            BufferConverter.getPackedArgbFromLittleEndianShorts(pixels, imageBuffer, size, width, height, channels, (int)maxval);
        else if (sampleType == TYPE_SHORT)
            BufferConverter.getPackedArgbFromShorts(pixels, imageBuffer, size, width, height, channels, (int)maxval);
        else if (sampleType == TYPE_FLOAT && isLittleEndian)
            BufferConverter.getPackedArgbFromLittleEndianFloats(pixels, imageBuffer, size, width, height, channels, maxval);
        else if (sampleType == TYPE_FLOAT)
            BufferConverter.getPackedArgbFromFloats(pixels, imageBuffer, size, width, height, channels, maxval);
    }

    public static void getPackedArgbFromBits(
        int[] pixels,
        byte[] imageBuffer,
        int size,
        int width,
        int height,
        int channels,
        boolean shouldInvert
    ) {
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

    public static void getPackedArgbFromBytes(
        int[] pixels,
        byte[] imageBuffer,
        int size,
        int width,
        int height,
        int channels,
        int maxval
    ) {
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

    public static void getPackedArgbFromShorts(
        int[] pixels,
        byte[] imageBuffer,
        int size,
        int width,
        int height,
        int channels,
        int maxval
    ) {
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

    public static void getPackedArgbFromLittleEndianShorts(
        int[] pixels,
        byte[] imageBuffer,
        int size,
        int width,
        int height,
        int channels,
        int maxval
    ) {
        maxval = maxval <= 0 ? 65535 : maxval;
        int area = width * height;
        if (channels <= 1) {
            int len = Math.min(size / 2, area);
            if (maxval == 65535) {
                for (int i = 0; i < len; i++) {
                    int lum = imageBuffer[2*i+1] & 0xff;
                    pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                }
            }
            else {
                int factor = (maxval + 1) / 256;
                for (int i = 0; i < len; i++) {
                    int value = (imageBuffer[2*i+1] & 0xff) << 8 | (imageBuffer[2*i] & 0xff);
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
                        int lum = imageBuffer[i+1] & 0xff;
                        argb |= lum << (ch*8);
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
                        int value = (imageBuffer[i+1] & 0xff) << 8 | (imageBuffer[i] & 0xff);
                        int lum = Math.min(Math.max(value / factor, 0), 255);
                        argb |= lum << (ch*8);
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

    public static void getPackedArgbFromFloats(
        int[] pixels,
        byte[] imageBuffer,
        int size,
        int width,
        int height,
        int channels,
        double maxval
    ) {
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

    public static void getPackedArgbFromLittleEndianFloats(
        int[] pixels,
        byte[] imageBuffer,
        int size,
        int width,
        int height,
        int channels,
        double maxval
    ) {
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
