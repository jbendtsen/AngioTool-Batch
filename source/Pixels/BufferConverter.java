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
        boolean isPlanar,
        boolean shouldDiffOrInvert,
        int[] pixels,
        byte[] imageBuffer,
        int size,
        int width,
        int height,
        int channels,
        float maxval
    ) {
        if (shouldDiffOrInvert && sampleType != TYPE_BIT) {
            int sampleGap;
            int nRows;
            if (isPlanar) {
                sampleGap = 1;
                nRows = height * channels; 
            }
            else {
                sampleGap = channels;
                nRows = height;
            }

            if (sampleType == TYPE_BYTE)
                diffSamplesHorizontallyAsBytes(imageBuffer, size, width, nRows, sampleGap);
            else if (sampleType == TYPE_SHORT)
                diffSamplesHorizontallyAsShorts(imageBuffer, size, width, nRows, sampleGap, isLittleEndian);
            else
                diffSamplesHorizontallyAsFloats(imageBuffer, size, width, nRows, sampleGap, isLittleEndian);
        }

        if (isPlanar) {
            if (sampleType == TYPE_BIT)
                getPackedArgbFromBitsPlanar(pixels, imageBuffer, size, width, height, channels, shouldDiffOrInvert);
            else if (sampleType == TYPE_BYTE)
                getPackedArgbFromBytesPlanar(pixels, imageBuffer, size, width, height, channels, (int)maxval);
            else if (sampleType == TYPE_SHORT)
                getPackedArgbFromShortsPlanar(pixels, imageBuffer, size, width, height, channels, (int)maxval, isLittleEndian);
            else if (sampleType == TYPE_FLOAT)
                getPackedArgbFromFloatsPlanar(pixels, imageBuffer, size, width, height, channels, maxval, isLittleEndian);
        }
        else {
            if (sampleType == TYPE_BIT)
                getPackedArgbFromBits(pixels, imageBuffer, size, width, height, channels, shouldDiffOrInvert);
            else if (sampleType == TYPE_BYTE)
                getPackedArgbFromBytes(pixels, imageBuffer, size, width, height, channels, (int)maxval);
            else if (sampleType == TYPE_SHORT)
                getPackedArgbFromShorts(pixels, imageBuffer, size, width, height, channels, (int)maxval, isLittleEndian);
            else if (sampleType == TYPE_FLOAT)
                getPackedArgbFromFloats(pixels, imageBuffer, size, width, height, channels, maxval, isLittleEndian);
        }
    }

    public static void diffSamplesHorizontallyAsBytes(
        byte[] imageBuffer,
        int size,
        int width,
        int nRows,
        int sampleGap
    ) {
        for (int y = 0; y < nRows; y++) {
            for (int x = 1; x < width; x++)
                imageBuffer[sampleGap*(x+width*y)] += imageBuffer[sampleGap*(x-1+width*y)];
        }
    }

    public static void diffSamplesHorizontallyAsShorts(
        byte[] imageBuffer,
        int size,
        int width,
        int nRows,
        int sampleGap,
        boolean isLittleEndian
    ) {
        int endian = isLittleEndian ? 1 : 0;

        for (int y = 0; y < nRows; y++) {
            for (int x = 1; x < width; x++) {
                short cur  = (short)(
                    (imageBuffer[2*sampleGap*(x + width*y)+endian] & 0xff) << 8 |
                    (imageBuffer[2*sampleGap*(x + width*y)+(endian^1)] & 0xff)
                );
                short prev = (short)(
                    (imageBuffer[2*sampleGap*(x-1+width*y)+endian] & 0xff) << 8 |
                    (imageBuffer[2*sampleGap*(x-1+width*y)+(endian^1)] & 0xff)
                );

                cur += prev;

                imageBuffer[2*sampleGap*(x+width*y)+endian]     = (byte)(cur >> 8);
                imageBuffer[2*sampleGap*(x+width*y)+(endian^1)] = (byte)cur;
            }
        }
    }

    public static void diffSamplesHorizontallyAsFloats(
        byte[] imageBuffer,
        int size,
        int width,
        int nRows,
        int sampleGap,
        boolean isLittleEndian
    ) {
        int s0, s1, s2, s3;
        if (isLittleEndian) {
            s0 = 0;
            s1 = 8;
            s2 = 16;
            s3 = 24;
        }
        else {
            s0 = 24;
            s1 = 16;
            s2 = 8;
            s3 = 0;
        }

        for (int y = 0; y < nRows; y++) {
            for (int x = 4; x < 4*width; x += 4) {
                float cur = Float.intBitsToFloat(
                    (imageBuffer[x] & 0xff) << s0 |
                    (imageBuffer[x+1] & 0xff) << s1 |
                    (imageBuffer[x+2] & 0xff) << s2 |
                    (imageBuffer[x+3] & 0xff) << s3
                );
                float prev = Float.intBitsToFloat(
                    (imageBuffer[x-4] & 0xff) << s0 |
                    (imageBuffer[x-3] & 0xff) << s1 |
                    (imageBuffer[x-2] & 0xff) << s2 |
                    (imageBuffer[x-1] & 0xff) << s3
                );

                int bits = Float.floatToRawIntBits(cur + prev);
                imageBuffer[x]   = (byte)(bits >> s0);
                imageBuffer[x+1] = (byte)(bits >> s1);
                imageBuffer[x+2] = (byte)(bits >> s2);
                imageBuffer[x+3] = (byte)(bits >> s3);
            }
        }
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

    public static void getPackedArgbFromBitsPlanar(
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
        int planeSize = (area + 7) / 8;

        for (int i = 0; i < size; i++) {
            byte v = imageBuffer[i];
            for (int j = 0; j < 8 && i*8+j < area; j++)
                pixels[i*8+j] = 0xff000000 | (xorValue ^ -((v >> (7-j)) & 1));
        }

        for (int ch = 1; ch < channels; ch++) {
            int mask = 0xff << (8*ch);
            for (int i = 0; i < size; i++) {
                byte v = imageBuffer[ch*planeSize+i];
                for (int j = 0; j < 8 && i*8+j < area; j++)
                    pixels[i*8+j] = (pixels[i*8+j] & ~mask) | ((xorValue ^ -((v >> (7-j)) & 1)) & mask);
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
        else if (channels == 2) {
            int len = Math.min(size, area * 2);
            if (maxval == 255) {
                for (int in = 0, out = 0; in < len; in += 2, out++) {
                    int a = imageBuffer[in] & 0xff;
                    int b = imageBuffer[in+1] & 0xff;
                    pixels[out] = 0xff000000 | (a << 8) | b;
                }
            }
            else {
                int factor = 256 / (maxval + 1);
                for (int in = 0, out = 0; in < len; in += 2, out++) {
                    int a = Math.min(Math.max(factor * (imageBuffer[in] & 0xff), 0), 255);
                    int b = Math.min(Math.max(factor * (imageBuffer[in+1] & 0xff), 0), 255);
                    pixels[out] = 0xff000000 | (a << 8) | b;
                }
            }
        }
        else if (channels == 3) {
            int len = Math.min(size, area * 3);
            if (maxval == 255) {
                for (int in = 0, out = 0; in < len; in += 3, out++) {
                    int r = imageBuffer[in] & 0xff;
                    int g = imageBuffer[in+1] & 0xff;
                    int b = imageBuffer[in+2] & 0xff;
                    pixels[out] = 0xff000000 | (r << 16) | (g << 8) | b;
                }
            }
            else {
                int factor = 256 / (maxval + 1);
                for (int in = 0, out = 0; in < len; in += 3, out++) {
                    int r = Math.min(Math.max(factor * (imageBuffer[in] & 0xff), 0), 255);
                    int g = Math.min(Math.max(factor * (imageBuffer[in+1] & 0xff), 0), 255);
                    int b = Math.min(Math.max(factor * (imageBuffer[in+2] & 0xff), 0), 255);
                    pixels[out] = 0xff000000 | (r << 16) | (g << 8) | b;
                }
            }
        }
        else if (channels == 4) {
            int len = Math.min(size, area * 4);
            if (maxval == 255) {
                for (int in = 0, out = 0; in < len; in += 4, out++) {
                    int a = imageBuffer[in] & 0xff;
                    int r = imageBuffer[in+1] & 0xff;
                    int g = imageBuffer[in+2] & 0xff;
                    int b = imageBuffer[in+3] & 0xff;
                    pixels[out] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
            else {
                int factor = 256 / (maxval + 1);
                for (int in = 0, out = 0; in < len; in += 4, out++) {
                    int a = Math.min(Math.max(factor * (imageBuffer[in] & 0xff), 0), 255);
                    int r = Math.min(Math.max(factor * (imageBuffer[in+1] & 0xff), 0), 255);
                    int g = Math.min(Math.max(factor * (imageBuffer[in+2] & 0xff), 0), 255);
                    int b = Math.min(Math.max(factor * (imageBuffer[in+3] & 0xff), 0), 255);
                    pixels[out] = (a << 24) | (r << 16) | (g << 8) | b;
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

    public static void getPackedArgbFromBytesPlanar(
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
        int len = Math.min(size, area);
        if (maxval == 255) {
            for (int i = 0; i < len; i++) {
                int lum = imageBuffer[i] & 0xff;
                pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
            }
            for (int ch = 1; ch < channels; ch++) {
                len = Math.min(size - ch*area, area);
                int mask = ~(0xff << (8*ch));
                for (int i = 0; i < len; i++) {
                    int lum = imageBuffer[ch*area+i] & 0xff;
                    pixels[i] = (pixels[i] & mask) | (lum << (8*ch));
                }
            }
        }
        else {
            int factor = 256 / (maxval + 1);
            for (int i = 0; i < len; i++) {
                int lum = Math.min(Math.max(factor * (imageBuffer[i] & 0xff), 0), 255);
                pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
            }
            for (int ch = 1; ch < channels; ch++) {
                len = Math.min(size - ch*area, area);
                int mask = ~(0xff << (8*ch));
                for (int i = 0; i < len; i++) {
                    int lum = Math.min(Math.max(factor * (imageBuffer[ch*area+i] & 0xff), 0), 255);
                    pixels[i] = (pixels[i] & mask) | (lum << (8*ch));
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
        int maxval,
        boolean isLittleEndian
    ) {
        maxval = Math.max(maxval <= 0 ? 65535 : maxval, 255);
        int endian = isLittleEndian ? 1 : 0;
        int area = width * height;

        if (channels <= 1) {
            int len = Math.min(size / 2, area);
            if (maxval == 65535) {
                for (int i = 0; i < len; i++) {
                    int lum = imageBuffer[2*i+endian] & 0xff;
                    pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                }
            }
            else {
                int factor = (maxval + 1) / 256;
                for (int i = 0; i < len; i++) {
                    int value = (imageBuffer[2*i+endian] & 0xff) << 8 | (imageBuffer[2*i+(endian^1)] & 0xff);
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
                        int lum = imageBuffer[i+endian] & 0xff;
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
                        int value = (imageBuffer[i+endian] & 0xff) << 8 | (imageBuffer[i+(endian^1)] & 0xff);
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

    public static void getPackedArgbFromShortsPlanar(
        int[] pixels,
        byte[] imageBuffer,
        int size,
        int width,
        int height,
        int channels,
        int maxval,
        boolean isLittleEndian
    ) {
        maxval = Math.max(maxval <= 0 ? 65535 : maxval, 255);
        int endian = isLittleEndian ? 1 : 0;
        int area = width * height;

        int len = Math.min(size / 2, area);
        if (maxval == 65535) {
            for (int i = 0; i < len; i++) {
                int lum = imageBuffer[2*i+endian] & 0xff;
                pixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
            }
            for (int ch = 1; ch < channels; ch++) {
                len = Math.min(size / 2 - ch*area, area);
                int mask = ~(0xff << (8*ch));
                for (int i = 0; i < len; i++) {
                    int lum = imageBuffer[2*(ch*area+i) + endian] & 0xff;
                    pixels[i] = (pixels[i] & mask) | (lum << (8*ch));
                }
            }
        }
        else {
            int factor = (maxval + 1) / 256;
            for (int in = 0, out = 0; out < len; in += 2, out++) {
                int value = (imageBuffer[in+endian] & 0xff) << 8 | (imageBuffer[in+(endian^1)] & 0xff);
                int lum = Math.min(Math.max(value / factor, 0), 255);
                pixels[out] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
            }
            for (int ch = 1; ch < channels; ch++) {
                len = Math.min(size / 2 - ch*area, area);
                int mask = ~(0xff << (8*ch));
                for (int in = 2*ch*area, out = 0; out < len; in += 2, out++) {
                    int value = (imageBuffer[in+endian] & 0xff) << 8 | (imageBuffer[in+(endian^1)] & 0xff);
                    int lum = Math.min(Math.max(value / factor, 0), 255);
                    pixels[out] = (pixels[out] & mask) | (lum << (8*ch));
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
        double maxval,
        boolean isLittleEndian
    ) {
        int s0, s1, s2, s3;
        if (isLittleEndian) {
            s0 = 0;
            s1 = 8;
            s2 = 16;
            s3 = 24;
        }
        else {
            s0 = 24;
            s1 = 16;
            s2 = 8;
            s3 = 0;
        }

        maxval = maxval <= 0.0 ? 1.0 : maxval;
        float factor = 256.0f / (float)maxval;
        int area = width * height;
        if (channels <= 1) {
            int len = Math.min(size / 4, area);
            for (int i = 0; i < len; i++) {
                float value = Float.intBitsToFloat(
                    (imageBuffer[4*i] & 0xff) << s0 |
                    (imageBuffer[4*i+1] & 0xff) << s1 |
                    (imageBuffer[4*i+2] & 0xff) << s2 |
                    (imageBuffer[4*i+3] & 0xff) << s3
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
                        (imageBuffer[4*i] & 0xff) << s0 |
                        (imageBuffer[4*i+1] & 0xff) << s1 |
                        (imageBuffer[4*i+2] & 0xff) << s2 |
                        (imageBuffer[4*i+3] & 0xff) << s3
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

    public static void getPackedArgbFromFloatsPlanar(
        int[] pixels,
        byte[] imageBuffer,
        int size,
        int width,
        int height,
        int channels,
        double maxval,
        boolean isLittleEndian
    ) {
        int s0, s1, s2, s3;
        if (isLittleEndian) {
            s0 = 0;
            s1 = 8;
            s2 = 16;
            s3 = 24;
        }
        else {
            s0 = 24;
            s1 = 16;
            s2 = 8;
            s3 = 0;
        }

        maxval = maxval <= 0.0 ? 1.0 : maxval;
        float factor = 256.0f / (float)maxval;
        int area = width * height;

        int len = Math.min(size / 4, area);
        for (int in = 0, out = 0; out < len; in += 4, out++) {
            float value = Float.intBitsToFloat(
                (imageBuffer[in] & 0xff) << s0 |
                (imageBuffer[in+1] & 0xff) << s1 |
                (imageBuffer[in+2] & 0xff) << s2 |
                (imageBuffer[in+3] & 0xff) << s3
            );
            int lum = Math.min(Math.max((int)(factor * value), 0), 255);
            pixels[out] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
        }
        for (int ch = 1; ch < channels; ch++) {
            len = Math.min(size / 2 - ch*area, area);
            int mask = ~(0xff << (8*ch));
            for (int in = 4*ch*area, out = 0; out < len; in += 4, out++) {
                float value = Float.intBitsToFloat(
                    (imageBuffer[in] & 0xff) << s0 |
                    (imageBuffer[in+1] & 0xff) << s1 |
                    (imageBuffer[in+2] & 0xff) << s2 |
                    (imageBuffer[in+3] & 0xff) << s3
                );
                int lum = Math.min(Math.max((int)(factor * value), 0), 255);
                pixels[out] = (pixels[out] & mask) | (lum << (8*ch));
            }
        }
    }
}
