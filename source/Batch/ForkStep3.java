package Batch;

import Utils.Utils;
import java.util.concurrent.ThreadPoolExecutor;

public class ForkStep3 implements ISliceCompute {
    static final int IN_PLACE_THRESHOLD = 250;

    private final int depth = 1;
    private final int width;
    private final int height;
    private final float[] src_;

    public ForkStep3(float[] src_, int width, int height) {
        this.src_ = src_;
        this.width = width;
        this.height = height;
    }

    @Override
    public void computeSlice(int start, int length) {
        int w = this.width;
        int h = this.height;
        int d = this.depth;
        int n = w;
        if (h > w) {
            n = h;
        }

        if (d > n) {
            n = d;
        }

        int[] tempS = new int[n];
        int[] tempInt = new int[n];
        int noResult = 3 * (n + 1) * (n + 1);

        for(int z = 0; z < this.depth; ++z) {
            float[] sk = this.src_;

            for(int x = start; x < start + length; ++x) {
                boolean nonempty = false;

                for(int y = 0; y < this.height; ++y) {
                    tempS[y] = (int)sk[x + w * y];
                    if (tempS[y] > 0) {
                        nonempty = true;
                    }
                }

                if (nonempty) {
                    for(int y = 0; y < h; ++y) {
                        int min = noResult;
                        int delta = y;

                        for(int y_ = 0; y_ < h; ++y_) {
                            int test = tempS[y_] + delta * delta--;
                            if (test < min) {
                                min = test;
                            }
                        }

                        tempInt[y] = min;
                    }

                    for(int y = 0; y < h; ++y) {
                        sk[x + w * y] = (float)tempInt[y];
                    }
                }
            }
        }
    }

    public static void thin(ThreadPoolExecutor threadPool, int maxWorkers, float[] src, int width, int height) {
        PointVectorInt offsetLengthPairs = new PointVectorInt();
        Utils.makeBinaryTreeOfSlices(offsetLengthPairs, 0, width, IN_PLACE_THRESHOLD - 1);

        Utils.computeSlicesInParallel(
            threadPool,
            maxWorkers,
            offsetLengthPairs,
            new ForkStep3(src, width, height)
        );
    }
}
