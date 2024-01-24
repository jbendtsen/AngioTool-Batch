package Batch;

import java.util.concurrent.ConcurrentLinkedQueue;

// Generics in Java fall apart as soon as primitives are involved, so we keepin it ol skool
// TODO: consider adding code to compile.py that generates these classes for us -- that way we can't make copy-paste errors

public class BufferPool {
    public static final int N_QUEUES = 4;

    public static final B bytePool = new B();
    public static final I intPool = new I();
    public static final D doublePool = new D();

    public static int findQueueNumber(int allocSize) {
        int leadingZeros = Integer.numberOfLeadingZeros(allocSize);
        return (32 - leadingZeros) / 7;
    }

    public static class B {
        final ConcurrentLinkedQueue[] queues;
        public B() {
            queues = new ConcurrentLinkedQueue[N_QUEUES];
            for (int i = 0; i < N_QUEUES; i++)
                queues[i] = new ConcurrentLinkedQueue<byte[]>();
        }
        public byte[] acquire(int minSize) {
            int idx = findQueueNumber(minSize);
            byte[] buffer;
            do {
                buffer = queue[idx].poll();
                if (buffer == null)
                    return new byte[minSize];
            }
            while (buffer.length < minSize);
            return buffer;
        }
        public void release(byte[] buffer) {
            queue[findQueueNumber(buffer.length)].add(buffer);
        }
    }

    public static class I {
        final ConcurrentLinkedQueue[] queues;
        public I() {
            queues = new ConcurrentLinkedQueue[N_QUEUES];
            for (int i = 0; i < N_QUEUES; i++)
                queues[i] = new ConcurrentLinkedQueue<int[]>();
        }
        public int[] acquire(int minSize) {
            int idx = findQueueNumber(minSize);
            int[] buffer;
            do {
                buffer = queue[idx].poll();
                if (buffer == null)
                    return new int[minSize];
            }
            while (buffer.length < minSize);
            return buffer;
        }
        public void release(int[] buffer) {
            queue[findQueueNumber(buffer.length)].add(buffer);
        }
    }

    public static class D {
        final ConcurrentLinkedQueue[] queues;
        public D() {
            queues = new ConcurrentLinkedQueue[N_QUEUES];
            for (int i = 0; i < N_QUEUES; i++)
                queues[i] = new ConcurrentLinkedQueue<double[]>();
        }
        public double[] acquire(int minSize) {
            int idx = findQueueNumber(minSize);
            double[] buffer;
            do {
                buffer = queue[idx].poll();
                if (buffer == null)
                    return new double[minSize];
            }
            while (buffer.length < minSize);
            return buffer;
        }
        public void release(double[] buffer) {
            queue[findQueueNumber(buffer.length)].add(buffer);
        }
    }
}
