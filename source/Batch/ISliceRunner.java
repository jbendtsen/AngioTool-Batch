package Batch;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public interface ISliceRunner
{
    void runSlices(ISliceCompute work, int maxWorkers, int length, int largestAtom) throws Throwable;
    int countSlices(int maxWorkers, int length, int largestAtom);

    public class Series implements ISliceRunner
    {
        @Override
        public void runSlices(ISliceCompute work, int maxWorkers, int length, int largestAtom) throws Throwable
        {
            ISliceCompute.Result res = new ISliceCompute.Result();

            int idx = 0;
            for (int i = 0; i < length; i += largestAtom) {
                res.idx = idx;
                res.result = work.computeSlice(idx, i, Math.min(length - i, largestAtom));
                work.finishSlice(res);
                idx++;
            }
        }

        @Override
        public int countSlices(int maxWorkers, int length, int largestAtom)
        {
            return (length + largestAtom - 1) / largestAtom;
        }
    }

    public class Parallel implements ISliceRunner
    {
        public ThreadPoolExecutor threadPool;

        public Parallel(ThreadPoolExecutor threadPool)
        {
            this.threadPool = threadPool;
        }

        @Override
        public void runSlices(ISliceCompute work, int maxWorkers, int length, int largestAtom) throws Throwable
        {
            final int nWorkers = Math.max(maxWorkers, 1);
            final int atomSize = Math.max(largestAtom, 1);
            final int workJump = nWorkers * atomSize;
            LinkedBlockingQueue<ISliceCompute.Result> resultQueue = new LinkedBlockingQueue<>();

            int offset = 0;
            int nSlices = 0;

            for (int i = 0; i < maxWorkers; i++) {
                final int off = offset;
                threadPool.submit(() -> {
                    for (int j = off; j < length; j += workJump) {
                        int idx = j / atomSize;
                        int chunkSize = Math.min(atomSize, length - off);

                        ISliceCompute.Result res = new ISliceCompute.Result();
                        res.idx = idx;
                        try {
                            res.result = work.computeSlice(idx, j, chunkSize);
                        }
                        catch (Throwable ex) {
                            res.ex = ex;
                        }
                        finally {
                            resultQueue.add(res);
                        }
                    }
                });

                nSlices += (length - offset + workJump - 1) / workJump;
                offset += atomSize;
                if (offset >= length)
                    break;
            }

            for (int i = 0; i < nSlices; i++) {
                ISliceCompute.Result res = resultQueue.take();
                if (res.ex != null)
                    throw res.ex;
                work.finishSlice(res);
            }
        }

        @Override
        public int countSlices(int maxWorkers, int length, int largestAtom)
        {
            final int nWorkers = Math.max(maxWorkers, 1);
            final int atomSize = Math.max(largestAtom, 1);
            final int workJump = nWorkers * atomSize;

            int nSlices = 0;
            int offset = 0;

            for (int i = 0; i < maxWorkers; i++) {
                nSlices += (length - offset + workJump - 1) / workJump;
                offset += atomSize;
                if (offset >= length)
                    break;
            }

            return nSlices;
        }
    }
}