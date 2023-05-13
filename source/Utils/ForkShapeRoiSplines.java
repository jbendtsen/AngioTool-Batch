package Utils;

import AngioTool.PolygonPlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkShapeRoiSplines extends RecursiveTask<ShapeRoi> {
   private int mLength;
   private int mStart;
   private int sThreshold = 60;
   private int fraction;
   private ShapeRoi originalShapeRoi;
   private Roi[] originalRoi;
   private ShapeRoi src;

   public ForkShapeRoiSplines() {
   }

   public ForkShapeRoiSplines(ShapeRoi sr, int fraction, int start, int length) {
      this.src = sr;
      this.originalShapeRoi = sr;
      this.originalRoi = sr.getRois();
      this.mStart = start;
      this.mLength = length;
      this.fraction = fraction;
   }

   public ShapeRoi computeDirectly() {
      ShapeRoi first = new ShapeRoi(new Roi(this.src.getBounds()));
      ShapeRoi result = new ShapeRoi(first);

      for(int i = this.mStart; i < this.mStart + this.mLength; ++i) {
         PolygonRoi pr = new PolygonRoi(this.originalRoi[i].getPolygon(), 2);
         int coordinates = pr.getNCoordinates();
         double area = new PolygonPlus(pr.getPolygon()).area();
         pr.fitSpline(pr.getNCoordinates() / this.fraction);
         result.xor(new ShapeRoi(pr));
      }

      result.xor(first);
      return result;
   }

   protected ShapeRoi compute() {
      if (this.mLength < this.sThreshold) {
         return this.computeDirectly();
      } else {
         int split = this.mLength / 2;
         int remainder = this.mLength % 2;
         int secondHalf = split + remainder;
         ForkShapeRoiSplines left = new ForkShapeRoiSplines(this.src, this.fraction, this.mStart, split);
         ForkShapeRoiSplines right = new ForkShapeRoiSplines(this.src, this.fraction, this.mStart + split, secondHalf);
         right.fork();
         ShapeRoi a = left.compute();
         a.xor(right.join());
         return a;
      }
   }

   public ShapeRoi computeSplines(ShapeRoi sr, int fraction) {
      new ShapeRoi(new Roi(0, 0, 0, 0));
      this.originalShapeRoi = sr;
      ForkShapeRoiSplines fs = new ForkShapeRoiSplines(sr, fraction, 0, sr.getRois().length);
      ForkJoinPool pool = new ForkJoinPool();
      return pool.invoke(fs);
   }
}
