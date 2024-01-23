package AnalyzeSkeleton;

import java.util.ArrayList;

public class SkeletonResult {
   int numOfTrees;
   int[] numberOfBranches = null;
   int[] numberOfEndPoints = null;
   int[] numberOfJunctionVoxels = null;
   int[] numberOfSlabs = null;
   int[] numberOfJunctions = null;
   int[] numberOfTriplePoints = null;
   int[] numberOfQuadruplePoints = null;
   double[] averageBranchLength = null;
   double[] maximumBranchLength = null;
   int[] numberOfVoxels = null;
   ArrayList<Point> listOfEndPoints = null;
   ArrayList<Point> listOfJunctionVoxels = null;
   ArrayList<Point> listOfSlabVoxels = null;
   ArrayList<Point> listOfStartingSlabVoxels = null;
   private Graph[] graph = null;

   public SkeletonResult(int numOfTrees) {
      this.numOfTrees = numOfTrees;
   }

   public void setNumOfTrees(int numOfTrees) {
      this.numOfTrees = numOfTrees;
   }

   public void setBranches(int[] numberOfBranches) {
      this.numberOfBranches = numberOfBranches;
   }

   public void setJunctions(int[] numberOfJunctions) {
      this.numberOfJunctions = numberOfJunctions;
   }

   public void setEndPoints(int[] numberOfEndPoints) {
      this.numberOfEndPoints = numberOfEndPoints;
   }

   public void setJunctionVoxels(int[] numberOfJunctionVoxels) {
      this.numberOfJunctionVoxels = numberOfJunctionVoxels;
   }

   public void setSlabs(int[] numberOfSlabs) {
      this.numberOfSlabs = numberOfSlabs;
   }

   public void setNumberOfVoxels(int[] numberOfVoxels) {
      this.numberOfVoxels = numberOfVoxels;
   }

   public void setTriples(int[] numberOfTriplePoints) {
      this.numberOfTriplePoints = numberOfTriplePoints;
   }

   public void setQuadruples(int[] numberOfQuadruplePoints) {
      this.numberOfQuadruplePoints = numberOfQuadruplePoints;
   }

   public void setAverageBranchLength(double[] averageBranchLength) {
      this.averageBranchLength = averageBranchLength;
   }

   public void setMaximumBranchLength(double[] maximumBranchLength) {
      this.maximumBranchLength = maximumBranchLength;
   }

   public void setListOfEndPoints(ArrayList<Point> listOfEndPoints) {
      this.listOfEndPoints = listOfEndPoints;
   }

   public void setListOfJunctionVoxels(ArrayList<Point> listOfJunctionVoxels) {
      this.listOfJunctionVoxels = listOfJunctionVoxels;
   }

   public void setListOfSlabVoxels(ArrayList<Point> listOfSlabVoxels) {
      this.listOfSlabVoxels = listOfSlabVoxels;
   }

   public void setListOfStartingSlabVoxels(ArrayList<Point> listOfStartingSlabVoxels) {
      this.listOfStartingSlabVoxels = listOfStartingSlabVoxels;
   }

   public void setGraph(Graph[] graph) {
      this.graph = graph;
   }

   public int getNumOfTrees() {
      return this.numOfTrees;
   }

   public int[] getBranches() {
      return this.numberOfBranches;
   }

   public int[] getJunctions() {
      return this.numberOfJunctions;
   }

   public int[] getEndPoints() {
      return this.numberOfEndPoints;
   }

   public int[] getJunctionVoxels() {
      return this.numberOfJunctionVoxels;
   }

   public int[] getSlabs() {
      return this.numberOfSlabs;
   }

   public int[] getTriples() {
      return this.numberOfTriplePoints;
   }

   public int[] getQuadruples() {
      return this.numberOfQuadruplePoints;
   }

   public double[] getAverageBranchLength() {
      return this.averageBranchLength;
   }

   public double[] getMaximumBranchLength() {
      return this.maximumBranchLength;
   }

   public int[] getNumberOfVoxels() {
      return this.numberOfVoxels;
   }

   public ArrayList<Point> getListOfEndPoints() {
      return this.listOfEndPoints;
   }

   public ArrayList<Point> getListOfJunctionVoxels() {
      return this.listOfJunctionVoxels;
   }

   public ArrayList<Point> getListOfSlabVoxels() {
      return this.listOfSlabVoxels;
   }

   public ArrayList<Point> getListOfStartingSlabVoxels() {
      return this.listOfStartingSlabVoxels;
   }

   public Graph[] getGraph() {
      return this.graph;
   }

   public int[] calculateNumberOfVoxels() {
      this.numberOfVoxels = new int[this.numOfTrees];

      for(int i = 0; i < this.numOfTrees; ++i) {
         this.numberOfVoxels[i] = this.numberOfEndPoints[i] + this.numberOfJunctionVoxels[i] + this.numberOfSlabs[i];
      }

      return this.numberOfVoxels;
   }
}
