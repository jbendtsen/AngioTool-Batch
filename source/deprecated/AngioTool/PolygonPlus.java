package AngioTool;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

public class PolygonPlus {
   private final int N;
   private final Point[] points;
   private Polygon p;

   public PolygonPlus(Point[] points) {
      this.N = points.length;
      this.points = new Point[this.N + 1];

      for(int i = 0; i < this.N; ++i) {
         this.points[i] = points[i];
      }

      this.points[this.N] = points[0];
      this.p = new Polygon();

      for(int i = 0; i < this.points.length; ++i) {
         this.p.addPoint(this.points[i].x, this.points[i].y);
      }
   }

   public PolygonPlus(Polygon p) {
      this.p = p;
      this.N = p.npoints;
      this.points = new Point[this.N + 1];

      for(int i = 0; i < this.N; ++i) {
         this.points[i] = new Point(p.xpoints[i], p.ypoints[i]);
      }

      this.points[this.N] = this.points[0];
   }

   public Polygon polygon() {
      return this.p;
   }

   public double area() {
      return Math.abs(this.signedArea());
   }

   public double signedArea() {
      double sum = 0.0;

      for(int i = 0; i < this.N; ++i) {
         sum = sum + (double)(this.points[i].x * this.points[i + 1].y) - (double)(this.points[i].y * this.points[i + 1].x);
      }

      return 0.5 * sum;
   }

   public boolean contains(int x, int y) {
      return this.p.contains(x, y);
   }

   public boolean contains(Point p) {
      return this.p.contains(p);
   }

   public boolean intersects(Rectangle r) {
      return this.p.intersects(r);
   }

   public double perimeter() {
      double sum = 0.0;

      for(int i = 0; i < this.N; ++i) {
         sum += this.points[i].distance(this.points[i + 1]);
      }

      return sum;
   }

   @Override
   public String toString() {
      String out = "";
      int[] xpoints = this.p.xpoints;
      int[] ypoints = this.p.ypoints;
      int npoints = this.p.npoints;

      for(int i = 0; i < npoints; ++i) {
         String concat = "[" + xpoints[i] + "," + ypoints[i] + "], ";
         out.concat(concat);
      }

      return out;
   }
}
