package Batch;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

public class ParallelUtils {
    public static PointVectorInt makeBinaryTreeOfSlices(int length, int largestAtom) {
        PointVectorInt offsetLengthPairs = new PointVectorInt();
        makeBinaryTreeOfSlices(offsetLengthPairs, 0, length, largestAtom);
        return offsetLengthPairs;
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

    public static boolean computeSlicesInParallel(
        ThreadPoolExecutor threadPool,
        int maxWorkers,
        PointVectorInt offsetLengthPairs,
        ISliceCompute params
    ) {
        final int nSlices = offsetLengthPairs.size;
        ArrayBlockingQueue<ISliceCompute.Result> resultQueue = new ArrayBlockingQueue<>(nSlices);

        if (nSlices <= maxWorkers) {
            for (int i = 0; i < nSlices; i++) {
                final int idx = i;
                final int offset = offsetLengthPairs.buf[2*i];
                final int length = offsetLengthPairs.buf[2*i+1];
                threadPool.submit(() -> {
                    ISliceCompute.Result res = new ISliceCompute.Result();
                    try {
                        res.result = params.computeSlice(idx, offset, length);
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
                threadPool.submit(() -> {
                    for (int j = idx; j < nSlices; j += maxWorkers) {
                        ISliceCompute.Result res = new ISliceCompute.Result();
                        try {
                            int offset = offsetLengthPairs.buf[2*j];
                            int length = offsetLengthPairs.buf[2*j+1];
                            res.result = params.computeSlice(j, offset, length);
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

        boolean wasInterrupted = false;
        boolean anyFailures = false;

        for (int i = 0; i < nSlices; i++) {
            try {
                ISliceCompute.Result res = resultQueue.take();
                anyFailures = anyFailures || res.ex != null;
                params.finishSlice(res);
            }
            catch (InterruptedException ex) {
                wasInterrupted = true;
                break;
            }
        }

        return !anyFailures && !wasInterrupted;
    }
}
