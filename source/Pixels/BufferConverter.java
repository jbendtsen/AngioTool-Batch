package Pixels;

public class BufferConverter
{
    static final int TYPE_NONE = 0;
    static final int TYPE_BIT = 1;
    static final int TYPE_BYTE = 2;
    static final int TYPE_SHORT = 3;
    static final int TYPE_FLOAT = 4;

    public static int convertToPackedArgb(
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
                return getPackedArgbFromBitsPlanar(pixels, imageBuffer, size, width, height, channels, shouldDiffOrInvert);
            else if (sampleType == TYPE_BYTE)
                return getPackedArgbFromBytesPlanar(pixels, imageBuffer, size, width, height, channels, (int)maxval);
            else if (sampleType == TYPE_SHORT)
                return getPackedArgbFromShortsPlanar(pixels, imageBuffer, size, width, height, channels, (int)maxval, isLittleEndian);
            else if (sampleType == TYPE_FLOAT)
                return getPackedArgbFromFloatsPlanar(pixels, imageBuffer, size, width, height, channels, maxval, isLittleEndian);
        }
        else {
            if (sampleType == TYPE_BIT)
                return getPackedArgbFromBits(pixels, imageBuffer, size, width, height, channels, shouldDiffOrInvert);
            else if (sampleType == TYPE_BYTE)
                return getPackedArgbFromBytes(pixels, imageBuffer, size, width, height, channels, (int)maxval);
            else if (sampleType == TYPE_SHORT)
                return getPackedArgbFromShorts(pixels, imageBuffer, size, width, height, channels, (int)maxval, isLittleEndian);
            else if (sampleType == TYPE_FLOAT)
                return getPackedArgbFromFloats(pixels, imageBuffer, size, width, height, channels, maxval, isLittleEndian);
        }

        return 0;
    }

    public static void diffSamplesHorizontallyAsBytes(
        byte[] imageBuffer,
        int size,
        int width,
        int nRows,
        int sampleGap
    ) {
        int stride = sampleGap * width;
        if (stride <= 0)
            return;

        int rows = Math.min(nRows, size / stride);

        for (int y = 0; y < rows; y++) {
            for (int p = sampleGap; p < stride; p++)
                imageBuffer[p+stride*y] += imageBuffer[p-sampleGap+stride*y];
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
        int bytesPerPixel = 2 * sampleGap;
        int stride = bytesPerPixel * width;
        if (stride <= 0)
            return;

        int rows = Math.min(nRows, size / stride);

        for (int y = 0; y < rows; y++) {
            for (int p = bytesPerPixel; p < stride; p += 2) {
                short cur  = (short)(
                    (imageBuffer[p+stride*y + endian] & 0xff) << 8 |
                    (imageBuffer[p+stride*y + (endian^1)] & 0xff)
                );
                short prev = (short)(
                    (imageBuffer[p-bytesPerPixel+stride*y + endian] & 0xff) << 8 |
                    (imageBuffer[p-bytesPerPixel+stride*y + (endian^1)] & 0xff)
                );

                cur += prev;

                imageBuffer[p+stride*y + endian]     = (byte)(cur >> 8);
                imageBuffer[p+stride*y + (endian^1)] = (byte)cur;
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

        int bytesPerPixel = 4 * sampleGap;
        int stride = bytesPerPixel * width;
        if (stride <= 0)
            return;

        int rows = Math.min(nRows, size / stride);

        for (int y = 0; y < rows; y++) {
            for (int p = bytesPerPixel; p < stride; p += 4) {
                int pos = p+stride*y;
                float cur = Float.intBitsToFloat(
                    (imageBuffer[pos] & 0xff) << s0 |
                    (imageBuffer[pos+1] & 0xff) << s1 |
                    (imageBuffer[pos+2] & 0xff) << s2 |
                    (imageBuffer[pos+3] & 0xff) << s3
                );
                float prev = Float.intBitsToFloat(
                    (imageBuffer[pos-bytesPerPixel] & 0xff) << s0 |
                    (imageBuffer[pos-bytesPerPixel+1] & 0xff) << s1 |
                    (imageBuffer[pos-bytesPerPixel+2] & 0xff) << s2 |
                    (imageBuffer[pos-bytesPerPixel+3] & 0xff) << s3
                );

                int bits = Float.floatToRawIntBits(cur + prev);
                imageBuffer[pos]   = (byte)(bits >> s0);
                imageBuffer[pos+1] = (byte)(bits >> s1);
                imageBuffer[pos+2] = (byte)(bits >> s2);
                imageBuffer[pos+3] = (byte)(bits >> s3);
            }
        }
    }

    public static int getPackedArgbFromBits(
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
        int trueSize = Math.min(size, Math.max(channels, 1) * (area + 7) / 8);

        if (channels <= 1) {
            for (int i = 0; i < trueSize; i++) {
                byte c = imageBuffer[i];
                for (int j = 0; j < 8 && i*8+j < area; j++)
                    pixels[i*8+j] = 0xff000000 | (xorValue ^ -((c >> (7-j)) & 1));
            }
        }
        else if (channels <= 3) {
            int ch = 0;
            int idx = 0;
            int argb = 0xff000000;
            for (int i = 0; i < trueSize; i++) {
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
            for (int i = 0; i < trueSize; i++) {
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

        return Math.min(area, (size * 8) / Math.max(channels, 1));
    }

    public static int getPackedArgbFromBitsPlanar(
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
        int planeSize = Math.min(size, (area + 7) / 8);

        for (int i = 0; i < planeSize; i++) {
            byte v = imageBuffer[i];
            for (int j = 0; j < 8 && i*8+j < area; j++)
                pixels[i*8+j] = 0xff000000 | (xorValue ^ -((v >> (7-j)) & 1));
        }

        for (int ch = 1; ch < channels; ch++) {
            int mask = 0xff << (8*ch);
            for (int i = 0; i < planeSize && ch*planeSize+i < size; i++) {
                byte v = imageBuffer[ch*planeSize+i];
                for (int j = 0; j < 8 && i*8+j < area; j++)
                    pixels[i*8+j] = (pixels[i*8+j] & ~mask) | ((xorValue ^ -((v >> (7-j)) & 1)) & mask);
            }
        }

        return Math.min(area, size * 8);
    }

    public static int getPackedArgbFromBytes(
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

        return Math.min(area, size / Math.min(Math.max(channels, 1), 4));
    }

    public static int getPackedArgbFromBytesPlanar(
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

        return Math.min(size, area);
    }

    public static int getPackedArgbFromShorts(
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

        return Math.min(area, size / (2 * Math.min(Math.max(channels, 1), 4)));
    }

    public static int getPackedArgbFromShortsPlanar(
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

        return Math.min(size / 2, area);
    }

    public static int getPackedArgbFromFloats(
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

        return Math.min(area, size / (4 * Math.min(Math.max(channels, 1), 4)));
    }

    public static int getPackedArgbFromFloatsPlanar(
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

        return Math.min(size / 4, area);
    }
}
