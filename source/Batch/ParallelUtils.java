package Batch;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

public class ParallelUtils {
    public static IntVector makeBinaryTreeOfSlices(int length, int largestAtom) {
        IntVector offsetLengthPairs = new IntVector();
        makeBinaryTreeOfSlices(offsetLengthPairs, 0, length, largestAtom);
        return offsetLengthPairs;
    }

    public static void makeBinaryTreeOfSlices(IntVector offsetLengthPairs, int start, int length, int largestAtom) {
        if (length <= largestAtom) {
            offsetLengthPairs.addTwo(start, length);
        }
        else {
            int split = length / 2;
            int remainder = length % 2;
            int secondHalf = split + remainder;

            makeBinaryTreeOfSlices(offsetLengthPairs, start, split, largestAtom);
            makeBinaryTreeOfSlices(offsetLengthPairs, start + split, secondHalf, largestAtom);
        }
    }

    public static void computeSlicesInParallel(
        ThreadPoolExecutor threadPool,
        int maxWorkers,
        IntVector offsetLengthPairs,
        ISliceCompute params
    ) throws Throwable {
        if (maxWorkers <= 0) {
            computeSlicesInSeries(offsetLengthPairs, params);
            return;
        }

        final int nSlices = offsetLengthPairs.size / 2;
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

        for (int i = 0; i < nSlices; i++) {
            ISliceCompute.Result res = resultQueue.take();
            if (res.ex != null)
                throw res.ex;
            params.finishSlice(res);
        }
    }

    public static void computeSlicesInSeries(IntVector offsetLengthPairs, ISliceCompute params) throws Throwable
    {
        boolean wasInterrupted = false;
        boolean anyFailures = false;

        final int nSlices = offsetLengthPairs.size / 2;
        ISliceCompute.Result res = new ISliceCompute.Result();

        for (int i = 0; i < nSlices; i++) {
            int offset = offsetLengthPairs.buf[2*i];
            int length = offsetLengthPairs.buf[2*i+1];
            res.result = params.computeSlice(i, offset, length);
            res.idx = i;
            params.finishSlice(res);
        }
    }
}
