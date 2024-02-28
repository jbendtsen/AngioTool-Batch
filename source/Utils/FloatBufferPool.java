package Utils;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FloatBufferPool
{
    private static final ConcurrentLinkedQueue<float[]> q = new ConcurrentLinkedQueue<>();

    public static float[] acquireAsIs(int minSize)
    {
        if (minSize <= 0)
            return null;

        float[] buffer;
        do {
            buffer = q.poll();
            if (buffer == null)
                return new float[minSize];
        }
        while (buffer.length < minSize);

        return buffer;
    }

    public static float[] acquireZeroed(int minSize)
    {
        if (minSize <= 0)
            return null;
        float[] buffer = acquireAsIs(minSize);
        Arrays.fill(buffer, 0, minSize, 0.0f);
        return buffer;
    }

    public static float[] release(float[] buffer)
    {
        if (buffer != null)
            q.add(buffer);
        return null;
    }
}
