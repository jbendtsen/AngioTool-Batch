package Batch;

import AngioTool.PolygonPlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import java.util.concurrent.ThreadPoolExecutor;

public class ComputeShapeRoiSplines implements ISliceCompute {
    static final int IN_PLACE_THRESHOLD = 60;

    private int fraction;
    private ShapeRoi originalShapeRoi;
    private Roi[] originalRoi;
    private ShapeRoi src;
    private ShapeRoi finalShapeRoi;

    public ComputeShapeRoiSplines(ShapeRoi src, int fraction) {
        this.src = src;
        this.originalShapeRoi = src;
        this.originalRoi = src.getRois();
        this.fraction = fraction;
        this.finalShapeRoi = null;
    }

    @Override
    public Object computeSlice(int sliceIdx, int start, int length) {
        ShapeRoi first = new ShapeRoi(new Roi(this.src.getBounds()));
        ShapeRoi result = new ShapeRoi(first);

        for(int i = start; i < start + length; ++i) {
            PolygonRoi pr = new PolygonRoi(this.originalRoi[i].getPolygon(), 2);
            int coordinates = pr.getNCoordinates();
            double area = new PolygonPlus(pr.getPolygon()).area();
            pr.fitSpline(pr.getNCoordinates() / this.fraction);
            result.xor(new ShapeRoi(pr));
        }

        result.xor(first);
        return result;
    }

    @Override
    public void finishSlice(ISliceCompute.Result slice) {
        ShapeRoi s = (ShapeRoi)slice.result;
        if (finalShapeRoi == null)
            finalShapeRoi = s;
        else
            finalShapeRoi.xor(s);
    }

    public static ShapeRoi computeSplines(ThreadPoolExecutor threadPool, int maxWorkers, ShapeRoi sr, int fraction) {
        ComputeShapeRoiSplines splines = new ComputeShapeRoiSplines(sr, fraction);
        ParallelUtils.computeSlicesInParallel(
            threadPool,
            maxWorkers,
            ParallelUtils.makeBinaryTreeOfSlices(sr.getRois().length, IN_PLACE_THRESHOLD - 1),
            splines
        );
        return splines.finalShapeRoi;
    }
}
