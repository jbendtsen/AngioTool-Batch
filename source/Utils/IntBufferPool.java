package Utils;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IntBufferPool
{
    private static final ConcurrentLinkedQueue<int[]> q = new ConcurrentLinkedQueue<>();

    public static int[] acquireAsIs(int minSize)
    {
        if (minSize <= 0)
            return null;

        int[] buffer;
        do {
            buffer = q.poll();
            if (buffer == null)
                return new int[minSize];
        }
        while (buffer.length < minSize);

        return buffer;
    }

    public static int[] acquireZeroed(int minSize)
    {
        if (minSize <= 0)
            return null;
        int[] buffer = acquireAsIs(minSize);
        Arrays.fill(buffer, 0, minSize, 0);
        return buffer;
    }

    public static int[] release(int[] buffer)
    {
        if (buffer != null)
            q.add(buffer);
        return null;
    }
}
