package Batch;

public interface ISliceCompute {
    public class Result {
        Object result = null;
        Throwable ex = null;
        int idx = -1;
    }

    Object computeSlice(int sliceIdx, int start, int length);
    default void finishSlice(Result res) {}
}
