package AnalyzeSkeleton;

import java.util.ArrayList;

public class Edge {
   public static final int TREE = 0;
   public static final int BACK = 1;
   public static final int UNDEFINED = -1;
   private int type = -1;
   private Vertex v1 = null;
   private Vertex v2 = null;
   private ArrayList<Point> slabs = null;
   private double length = 0.0;

   public Edge(Vertex v1, Vertex v2, ArrayList<Point> slabs, double length) {
      this.v1 = v1;
      this.v2 = v2;
      this.slabs = slabs;
      this.length = length;
   }

   public Vertex getV1() {
      return this.v1;
   }

   public Vertex getV2() {
      return this.v2;
   }

   public ArrayList<Point> getSlabs() {
      return this.slabs;
   }

   public void setType(int type) {
      this.type = type;
   }

   public int getType() {
      return this.type;
   }

   public Vertex getOppositeVertex(Vertex v) {
      if (this.v1.equals(v)) {
         return this.v2;
      } else {
         return this.v2.equals(v) ? this.v1 : null;
      }
   }

   public void setLength(double length) {
      this.length = length;
   }

   public double getLength() {
      return this.length;
   }
}
