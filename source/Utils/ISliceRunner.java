package Utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public interface ISliceRunner
{
    void runSlices(ISliceCompute work, int maxWorkers, int length, int largestAtom) throws Throwable;
    //int countSlices(int maxWorkers, int length, int largestAtom);

    public class Series implements ISliceRunner
    {
        @Override
        public void runSlices(ISliceCompute work, int maxWorkers, int length, int largestAtom) throws Throwable
        {
            work.initSlices(1);

            ISliceCompute.Result res = new ISliceCompute.Result();
            res.idx = 0;
            res.result = work.computeSlice(0, 0, length);
            work.finishSlice(res);
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
            work.initSlices(countSlices(maxWorkers, length, largestAtom));

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
                        int chunkSize = Math.min(atomSize, length - j);

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

        private static int countSlices(int maxWorkers, int length, int largestAtom)
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
