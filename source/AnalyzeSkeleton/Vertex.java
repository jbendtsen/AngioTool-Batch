package AnalyzeSkeleton;

import java.util.ArrayList;

public class Vertex {
   private ArrayList<Point> points = null;
   private ArrayList<Edge> branches = null;
   private boolean visited = false;
   private Edge precedessor = null;
   private int visitOrder = -1;

   public Vertex() {
      this.points = new ArrayList<>();
      this.branches = new ArrayList<>();
   }

   public void addPoint(Point p) {
      this.points.add(p);
   }

   public boolean isVertexPoint(Point p) {
      return this.points == null ? false : this.points.contains(p);
   }

   public String pointsToString() {
      StringBuilder sb = new StringBuilder();

      for(Point p : this.points) {
         sb.append(p.toString() + " ");
      }

      return sb.toString();
   }

   public ArrayList<Point> getPoints() {
      return this.points;
   }

   public void setBranch(Edge e) {
      this.branches.add(e);
   }

   public ArrayList<Edge> getBranches() {
      return this.branches;
   }

   public void setVisited(boolean b) {
      this.visited = b;
   }

   public void setVisited(boolean b, int visitOrder) {
      this.visited = b;
      this.visitOrder = visitOrder;
   }

   public boolean isVisited() {
      return this.visited;
   }

   public void setPredecessor(Edge pred) {
      this.precedessor = pred;
   }

   public Edge getPredecessor() {
      return this.precedessor;
   }

   public int getVisitOrder() {
      return this.visitOrder;
   }
}
