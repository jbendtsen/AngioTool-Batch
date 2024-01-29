package Batch;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

// Generics in Java fall apart as soon as primitives are involved, so we keepin it ol skool
// TODO: consider adding code to compile.py that generates these classes. This would eliminate potential copy-paste errors

public class BufferPool {
    public static final int N_QUEUES = 4;

    public static final B bytePool = new B();
    public static final I intPool = new I();
    public static final D doublePool = new D();

    public static int findQueueNumber(int allocSize) {
        int leadingZeros = Integer.numberOfLeadingZeros(allocSize);
        return Math.min((32 - leadingZeros) / 6, N_QUEUES - 1);
    }

    public static class B {
        final ConcurrentLinkedQueue[] queues;
        public B() {
            queues = new ConcurrentLinkedQueue[N_QUEUES];
            for (int i = 0; i < N_QUEUES; i++)
                queues[i] = new ConcurrentLinkedQueue<byte[]>();
        }
        public byte[] acquireAsIs(int minSize) {
            if (minSize <= 0)
                return null;
            int idx = findQueueNumber(minSize);
            byte[] buffer;
            do {
                buffer = ((ConcurrentLinkedQueue<byte[]>)queues[idx]).poll();
                if (buffer == null)
                    return new byte[minSize];
            }
            while (buffer.length < minSize);
            return buffer;
        }
        public byte[] acquireZeroed(int minSize) {
            if (minSize <= 0)
                return null;
            byte[] buffer = acquireAsIs(minSize);
            Arrays.fill(buffer, 0, minSize, (byte)0);
            return buffer;
        }
        public byte[] release(byte[] buffer) {
            if (buffer != null)
                queues[findQueueNumber(buffer.length)].add(buffer);
            return null;
        }
    }

    public static class I {
        final ConcurrentLinkedQueue[] queues;
        public I() {
            queues = new ConcurrentLinkedQueue[N_QUEUES];
            for (int i = 0; i < N_QUEUES; i++)
                queues[i] = new ConcurrentLinkedQueue<int[]>();
        }
        public int[] acquireAsIs(int minSize) {
            if (minSize <= 0)
                return null;
            int idx = findQueueNumber(minSize);
            int[] buffer;
            do {
                buffer = ((ConcurrentLinkedQueue<int[]>)queues[idx]).poll();
                if (buffer == null)
                    return new int[minSize];
            }
            while (buffer.length < minSize);
            return buffer;
        }
        public int[] acquireZeroed(int minSize) {
            if (minSize <= 0)
                return null;
            int[] buffer = acquireAsIs(minSize);
            Arrays.fill(buffer, 0, minSize, 0);
            return buffer;
        }
        public int[] release(int[] buffer) {
            if (buffer != null)
                queues[findQueueNumber(buffer.length)].add(buffer);
            return null;
        }
    }

    public static class D {
        final ConcurrentLinkedQueue[] queues;
        public D() {
            queues = new ConcurrentLinkedQueue[N_QUEUES];
            for (int i = 0; i < N_QUEUES; i++)
                queues[i] = new ConcurrentLinkedQueue<double[]>();
        }
        public double[] acquireAsIs(int minSize) {
            if (minSize <= 0)
                return null;
            int idx = findQueueNumber(minSize);
            double[] buffer;
            do {
                buffer = ((ConcurrentLinkedQueue<double[]>)queues[idx]).poll();
                if (buffer == null)
                    return new double[minSize];
            }
            while (buffer.length < minSize);
            return buffer;
        }
        public double[] acquireZeroed(int minSize) {
            if (minSize <= 0)
                return null;
            double[] buffer = acquireAsIs(minSize);
            Arrays.fill(buffer, 0, minSize, 0.0);
            return buffer;
        }
        public double[] release(double[] buffer) {
            if (buffer != null)
                queues[findQueueNumber(buffer.length)].add(buffer);
            return null;
        }
    }
}
