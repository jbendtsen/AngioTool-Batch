package Batch;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ParallelUtils {
    static class SliceResult {
        Object result = null;
        Throwable ex = null;
        int idx = -1;
    }

    public static void makeBinaryTreeOfSlices(PointVectorInt offsetLengthPairs, int start, int length, int largestAtom) {
        if (length <= largestAtom) {
            offsetLengthPairs.add(start, length);
        }
        else {
            int split = length / 2;
            int remainder = length % 2;
            int secondHalf = split + remainder;

            makeBinaryTreeOfSlices(offsetLengthPairs, start, split, largestAtom);
            makeBinaryTreeOfSlices(offsetLengthPairs, start + split, secondHalf, largestAtom);
        }
    }

    public static boolean computeSlicesInParallel(ThreadPoolExecutor threadPool, int maxWorkers, PointVectorInt offsetLengthPairs, ISliceCompute params) {
        final int nSlices = offsetLengthPairs.size;
        //Future[] futures = new Future[nSlices <= maxWorkers ? nSlices : maxWorkers];
        ArrayBlockingQueue<SliceResult> resultQueue = new ArrayBlockingQueue<>(nSlices);

        if (nSlices <= maxWorkers) {
            for (int i = 0; i < nSlices; i++) {
                final int idx = i;
                final int offset = offsetLengthPairs.buf[2*i];
                final int length = offsetLengthPairs.buf[2*i+1];
                futures[i] = threadPool.submit(() -> {
                    SliceResult res = new SliceResult();
                    try {
                        res.result = params.computeSlice(offset, length);
                    }
                    catch (Throwable ex) {
                        res.ex = ex;
                    }
                    finally {
                        res.idx = idx;
                        resultQueue.add(res);
                    }
                });
            }
        }
        else {
            for (int i = 0; i < maxWorkers; i++) {
                final int idx = i;
                futures[i] = threadPool.submit(() -> {
                    for (int j = idx; j < nSlices; j += maxWorkers) {
                        SliceResult res = new SliceResult();
                        try {
                            int offset = offsetLengthPairs.buf[2*j];
                            int length = offsetLengthPairs.buf[2*j+1];
                            res.result = params.computeSlice(offset, length);
                        }
                        catch (Throwable ex) {
                            res.ex = ex;
                        }
                        finally {
                            res.idx = j;
                            resultQueue.add(res);
                        }
                    }
                });
            }
        }

        boolean shouldInterrupt = false;
        boolean anyFailures = false;

        for (int i = 0; i < nSlices; i++) {
            SliceResult res = resultQueue.take();
            // ...
        }

        /*
        for (int i = 0; i < futures.length; i++) {
            Future<Object> f = (Future<Object>)futures[i];
            if (shouldInterrupt) {
                f.cancel(true);
            }
            else {
                try {
                    f.get();
                }
                catch (ExecutionException ex) {
                    anyFailures = true;
                }
                catch (InterruptedException ex) {
                    shouldInterrupt = true;
                    f.cancel(true);
                }
            }
        }
        */

        return !anyFailures && !shouldInterrupt;
    }
}
