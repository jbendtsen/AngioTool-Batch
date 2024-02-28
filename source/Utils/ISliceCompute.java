package Utils;

public interface ISliceCompute {
    public class Result {
        public Object result = null;
        public Throwable ex = null;
        public int idx = -1;
    }

    void initSlices(int nSlices);
    Object computeSlice(int sliceIdx, int start, int length);
    void finishSlice(Result res);
}
