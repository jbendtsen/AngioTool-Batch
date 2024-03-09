package Pixels;

import Utils.ByteBufferPool;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class TiffWriter
{
    static final int HEADER_SIZE = 0x8c;

    public static void writeUncompressedImage(
        OutputStream outStream,
        int[] inputPixels,
        int width,
        int height
    ) throws IOException
    {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Invalid dimensions " + width + " x " + height);

        int bytesPerPixel = 3;
        int outDataSize = width * height * bytesPerPixel;
        byte[] outputData = ByteBufferPool.acquireAsIs(HEADER_SIZE + outDataSize);

        try {
            writeUncompressedTiffHeader(outputData, width, height);
            writeUncompressedPixelData(outputData, HEADER_SIZE, inputPixels, width, height);
            outStream.write(outputData, 0, HEADER_SIZE + outDataSize);
        }
        finally {
            ByteBufferPool.release(outputData);
        }
    }

    private static void writeUncompressedTiffHeader(
        byte[] b,
        int width,
        int height
    ) {
        Arrays.fill(b, 0, HEADER_SIZE, (byte)0);
        int p = 0;

        // Tiff Type
        b[p++] = 'I';
        b[p++] = 'I';
        b[p++] = 0x2a;

        // First IFD offset (immediately after this field)
        p = 4;
        b[p++] = 8;

        // Number of tags = 10
        p = 8;
        b[p++] = 0xa;
        p++;

        // 254: New Subfile Type = 0
        b[p] = (byte)0xfe;
        b[p+2] = 4;
        b[p+4] = 1;
        p += 12;

        // 256: Width
        b[p+1] = 1;
        b[p+2] = 4;
        b[p+4] = 1;
        p += 8;
        b[p++] = (byte)width;
        b[p++] = (byte)(width >> 8);
        b[p++] = (byte)(width >> 16);
        b[p++] = (byte)(width >> 24);

        // 257: Height
        b[p] = 1;
        b[p+1] = 1;
        b[p+2] = 4;
        b[p+4] = 1;
        p += 8;
        b[p++] = (byte)height;
        b[p++] = (byte)(height >> 8);
        b[p++] = (byte)(height >> 16);
        b[p++] = (byte)(height >> 24);

        // 258: Bits Per Sample = 8,8,8: points to 0x86, 4 bytes after the last tag
        b[p] = 2;
        b[p+1] = 1;
        b[p+2] = 3;
        b[p+4] = 3;
        b[p+8] = (byte)(HEADER_SIZE - 6);
        p += 12;

        // 259: Compression = uncompressed
        b[p] = 3;
        b[p+1] = 1;
        b[p+2] = 3;
        b[p+4] = 1;
        b[p+8] = 1;
        p += 12;

        // 262: Photometric Interpretation = 2 (RGB)
        b[p] = 6;
        b[p+1] = 1;
        b[p+2] = 3;
        b[p+4] = 1;
        b[p+8] = 2;
        p += 12;

        // 273: Strip Offsets (aka image offset)
        b[p] = 17;
        b[p+1] = 1;
        b[p+2] = 4;
        b[p+4] = 1;
        b[p+8] = (byte)HEADER_SIZE;
        p += 12;

        // 277: Samples Per Pixel = 3
        b[p] = 21;
        b[p+1] = 1;
        b[p+2] = 3;
        b[p+4] = 1;
        b[p+8] = 3;
        p += 12;

        // 278: Rows Per Strip = Height
        b[p] = 22;
        b[p+1] = 1;
        b[p+2] = 4;
        b[p+4] = 1;
        p += 8;
        b[p++] = (byte)height;
        b[p++] = (byte)(height >> 8);
        b[p++] = (byte)(height >> 16);
        b[p++] = (byte)(height >> 24);

        // 279: Strip Byte Counts = image data size, ie. width*height*3
        int imageSize = width * height * 3;
        b[p] = 23;
        b[p+1] = 1;
        b[p+2] = 4;
        b[p+4] = 1;
        p += 8;
        b[p++] = (byte)imageSize;
        b[p++] = (byte)(imageSize >> 8);
        b[p++] = (byte)(imageSize >> 16);
        b[p++] = (byte)(imageSize >> 24);

        // 8,8,8 referenced by the "Bits Per Sample" tag
        b[p+4] = 8;
        b[p+6] = 8;
        b[p+8] = 8;
    }

    private static void writeUncompressedPixelData(
        byte[] outputBuffer,
        int offset,
        int[] inputPixels,
        int width,
        int height
    ) {
        int area = width * height;
        for (int in = 0, out = offset; in < area; in++, out += 3) {
            int rgb = inputPixels[in];
            outputBuffer[out] = (byte)(rgb >> 16);
            outputBuffer[out+1] = (byte)(rgb >> 8);
            outputBuffer[out+2] = (byte)rgb;
        }
    }
}
