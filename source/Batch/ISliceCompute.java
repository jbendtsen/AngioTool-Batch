package Batch;

public interface ISliceCompute {
    public class Result {
        Object result = null;
        Throwable ex = null;
        int idx = -1;
    }

    void initSlices(int nSlices);
    Object computeSlice(int sliceIdx, int start, int length);
    void finishSlice(Result res);
}
