package Utils;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DoubleBufferPool
{
    private static final ConcurrentLinkedQueue<double[]> q = new ConcurrentLinkedQueue<>();

    public static double[] acquireAsIs(int minSize)
    {
        if (minSize <= 0)
            return null;

        double[] buffer;
        do {
            buffer = q.poll();
            if (buffer == null)
                return new double[minSize];
        }
        while (buffer.length < minSize);

        return buffer;
    }

    public static double[] acquireZeroed(int minSize)
    {
        if (minSize <= 0)
            return null;
        double[] buffer = acquireAsIs(minSize);
        Arrays.fill(buffer, 0, minSize, 0.0);
        return buffer;
    }

    public static double[] release(double[] buffer)
    {
        if (buffer != null)
            q.add(buffer);
        return null;
    }
}
