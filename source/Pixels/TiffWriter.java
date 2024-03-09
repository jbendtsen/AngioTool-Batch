package Pixels;

import java.io.IOException;
import java.nio.channels.FileChannel;

public class TiffWriter
{
    static final int HEADER_SIZE = 0x8c;

    public static void writeUncompressedImage(
        FileChannel outFile,
        int[] inputPixels,
        int width,
        int height
    ) throws IOException
    {
        if (width <= 0 || height <= 0)
            throw new InvalidArgumentException("Invalid dimensions " + width + " x " + height);

        int bytesPerPixel = 3;
        int outDataSize = width * height * bytesPerPixel;
        byte[] outputPixels = ByteBufferPool.acquireAsIs(HEADER_SIZE + outDataSize);

        try {
            writeHeader(outputPixels, width, height, false);
            writeUncompressedPixelData(outputPixels, HEADER_SIZE, width, height);
            outFile.write(ByteBuffer.wrap(outputPixels, 0, HEADER_SIZE + outDataSize));
        }
        finally {
            ByteBufferPool.release(outputPixels);
        }
    }

    private static void writeHeader(
        byte[] b,
        int width,
        int height,
        boolean usingCompression
    ) {
        int p = 0;

        // Tiff Type
        b[p++] = 'I';
        b[p++] = 'I';
        b[p++] = 0x2a;
        b[p++] = 0;

        // First IFD offset (immediately after this field)
        b[p++] = 8;
        b[p++] = 0;
        b[p++] = 0;
        b[p++] = 0;

        // New Subfile Type: 00fe 0004 00000001 00000000
        // Width:            0100 0004 00000001 width
        //

        // Number of tags = 10
        b[p++] = 0xa;
        b[p++] = 0;

        fe 00 04 00 01 00 00 00 00 00 00 00 // New Subfile Type = 0
        00 01 04 00 01 00 00 00 00 03 00 00 // Width
        01 01 04 00 01 00 00 00 64 03 00 00 // Height
        02 01 03 00 03 00 00 00 86 00 00 00 // Bits Per Sample = points to 0x86, 4 bytes after the last tag
        03 01 03 00 01 00 00 00 01 00 00 00 // Compression = uncompressed
        06 01 03 00 01 00 00 00 02 00 00 00 // Photometric Interpretation = 2 (RGB)
        11 01 04 00 01 00 00 00 8c 00 00 00 // Strip Offsets (aka image offset)
        15 01 03 00 01 00 00 00 03 00 00 00 // Samples Per Pixel = 3
        16 01 04 00 01 00 00 00 64 03 00 00 // Rows Per Strip = Height
        17 01 04 00 01 00 00 00 00 84 1e 00 // Strip Byte Counts = image data size, ie. width*height*3

        00 00 00 00 08 00 08 00 08 00 // 4 bytes, then "short[] {8, 8, 8}"
    }
}
