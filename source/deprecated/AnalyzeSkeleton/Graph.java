package AnalyzeSkeleton;

import java.util.ArrayList;
import java.util.Stack;

public class Graph {
   private ArrayList<Edge> edges = null;
   private ArrayList<Vertex> vertices = null;
   private Vertex root = null;

   public Graph() {
      this.edges = new ArrayList<>();
      this.vertices = new ArrayList<>();
   }

   public boolean addEdge(Edge e) {
      if (this.edges.contains(e)) {
         return false;
      } else {
         e.getV1().setBranch(e);
         if (!e.getV1().equals(e.getV2())) {
            e.getV2().setBranch(e);
         }

         this.edges.add(e);
         return true;
      }
   }

   public boolean addVertex(Vertex v) {
      if (this.vertices.contains(v)) {
         return false;
      } else {
         this.vertices.add(v);
         return true;
      }
   }

   public ArrayList<Vertex> getVertices() {
      return this.vertices;
   }

   public ArrayList<Edge> getEdges() {
      return this.edges;
   }

   void setRoot(Vertex v) {
      this.root = v;
   }

   ArrayList<Edge> depthFirstSearch() {
      ArrayList<Edge> backEdges = new ArrayList<>();
      Stack<Vertex> stack = new Stack<>();

      for(Vertex v : this.vertices) {
         v.setVisited(false);
      }

      stack.push(this.root);
      int visitOrder = 0;

      while(!stack.empty()) {
         Vertex u = stack.pop();
         if (!u.isVisited()) {
            if (u.getPredecessor() != null) {
               u.getPredecessor().setType(0);
            }

            u.setVisited(true, visitOrder++);

            for(Edge e : u.getBranches()) {
               if (e.getType() == -1) {
                  Vertex ov = e.getOppositeVertex(u);
                  if (!ov.isVisited()) {
                     stack.push(ov);
                     ov.setPredecessor(e);
                  } else {
                     e.setType(1);
                     backEdges.add(e);
                  }
               }
            }
         }
      }

      return backEdges;
   }
}
