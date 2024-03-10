package Pixels;

import Utils.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TiffReader
{
    public static RefVector<int[]> readArgbImages(
        FileChannel fc,
        int maxImages,
        boolean shouldAllocateWithRecycler
    ) throws IOException
    {
        byte buf = new byte[4096];
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

        int ifdOffset = getInt(buf, 4, isLittleEndian);
        fc.position(ifdOffset);
        bb.limit(10);
        fc.read(bb);

        int nTags = Math.min(getShort(buf, 8, isLittleEndian), buf.length / (2 * 12));

        bb.position(0);
        bb.limit(nTags * 12);
        fc.read(bb);

        int width = 0;
        int height = 0;
        int sampleType = TYPE_NONE;
        int channels = 0;

        for (int i = 0, t = 0; i < nTags; i++, t += 12) {
            int attr = getShort(buf, t, isLittleEndian);
            int type = getShort(buf, t+2, isLittleEndian);
            int count = getInt(buf, t+4, isLittleEndian);
            if (count == 1) {
                int value = type == 3 ? getShort(buf, t+8, isLittleEndian) : getInt(buf, t+8, isLittleEndian);
                if (attr == ATTR_WIDTH)
            }
            else {
            
            }
        }
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
}
