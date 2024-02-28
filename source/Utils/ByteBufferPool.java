package Utils;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ByteBufferPool
{
    private static final ConcurrentLinkedQueue<byte[]> q = new ConcurrentLinkedQueue<>();

    public static byte[] acquireAsIs(int minSize)
    {
        if (minSize <= 0)
            return null;

        byte[] buffer;
        do {
            buffer = q.poll();
            if (buffer == null)
                return new byte[minSize];
        }
        while (buffer.length < minSize);

        return buffer;
    }

    public static byte[] acquireZeroed(int minSize)
    {
        if (minSize <= 0)
            return null;
        byte[] buffer = acquireAsIs(minSize);
        Arrays.fill(buffer, 0, minSize, (byte)0);
        return buffer;
    }

    public static byte[] release(byte[] buffer)
    {
        if (buffer != null)
            q.add(buffer);
        return null;
    }
}
