package AnalyzeSkeleton;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;

public class AnalyzeSkeleton implements PlugInFilter {
   public static byte END_POINT = 30;
   public static byte JUNCTION = 70;
   public static byte SLAB = 127;
   public static byte SHORTEST_PATH = 96;
   private ImagePlus imRef;
   private int width = 0;
   private int height = 0;
   private int depth = 0;
   private ImageStack inputImage = null;
   private boolean[][][] visited = (boolean[][][])null;
   private int totalNumberOfEndPoints = 0;
   private int totalNumberOfJunctionVoxels = 0;
   private int totalNumberOfSlabs = 0;
   private double shortestPath = 0.0;
   private ArrayList<Double> shortestPathList;
   private ArrayList<Point> shortestPathPoints;
   private int spx = 0;
   private int spy = 0;
   private int spz = 0;
   private double[][] spStartPosition;
   private ImageStack shortPathImage = null;
   private int[] numberOfBranches = null;
   private int[] numberOfEndPoints = null;
   private int[] numberOfJunctionVoxels = null;
   private int[] numberOfSlabs = null;
   private int[] numberOfJunctions = null;
   private int[] numberOfTriplePoints = null;
   private int[] numberOfQuadruplePoints = null;
   private ArrayList<Point>[] endPointsTree = null;
   private ArrayList<Point>[] junctionVoxelTree = null;
   private ArrayList<Point>[] startingSlabTree = null;
   private double[] averageBranchLength = null;
   private double[] maximumBranchLength = null;
   private ArrayList<Point> listOfEndPoints = null;
   private ArrayList<Point> listOfJunctionVoxels = null;
   private ArrayList<Point> listOfSlabVoxels = null;
   private ArrayList<Point> listOfStartingSlabVoxels = null;
   private ArrayList<ArrayList<Point>>[] listOfSingleJunctions = null;
   private Vertex[][] junctionVertex = (Vertex[][])null;
   private ImageStack taggedImage = null;
   private Point auxPoint = null;
   private int numOfTrees = 0;
   private boolean bPruneCycles = true;
   public static boolean pruneEnds = false;
   public static boolean calculateShortestPath = false;
   private Graph[] graph = null;
   private ArrayList<Point> slabList = null;
   private Vertex auxFinalVertex = null;
   public static final String[] pruneCyclesModes = new String[]{"none", "shortest branch", "lowest intensity voxel", "lowest intensity branch"};
   public static final int NONE = 0;
   public static final int SHORTEST_BRANCH = 1;
   public static final int LOWEST_INTENSITY_VOXEL = 2;
   public static final int LOWEST_INTENSITY_BRANCH = 3;
   private ImageStack originalImage = null;
   public static int pruneIndex = 0;
   private int x_offset = 1;
   private int y_offset = 1;
   private int z_offset = 1;
   public static boolean verbose = false;
   protected boolean silent = false;
   private static final boolean debug = false;

   public int setup(String arg, ImagePlus imp) {
      this.imRef = imp;
      if (arg.equals("about")) {
         this.showAbout();
         return 4096;
      } else {
         return 1;
      }
   }

   public void run(ImageProcessor ip) {
      GenericDialog gd = new GenericDialog("Analyze Skeleton");
      gd.addChoice("Prune cycle method: ", pruneCyclesModes, pruneCyclesModes[pruneIndex]);
      gd.addCheckbox("Prune ends", pruneEnds);
      gd.addCheckbox("Calculate largest shortest path", calculateShortestPath);
      gd.addCheckbox("Show detailed info", verbose);
      gd.showDialog();
      if (!gd.wasCanceled()) {
         pruneIndex = gd.getNextChoiceIndex();
         pruneEnds = gd.getNextBoolean();
         calculateShortestPath = gd.getNextBoolean();
         verbose = gd.getNextBoolean();
         ImagePlus origIP = null;
         switch(pruneIndex) {
            case 0:
               this.bPruneCycles = false;
               break;
            case 1:
               this.bPruneCycles = true;
               break;
            case 2:
            case 3:
               int[] ids = WindowManager.getIDList();
               if (ids == null || ids.length < 1) {
                  IJ.showMessage("You should have at least one image open.");
                  return;
               }

               String[] titles = new String[ids.length];

               for(int i = 0; i < ids.length; ++i) {
                  titles[i] = WindowManager.getImage(ids[i]).getTitle();
               }

               GenericDialog gd2 = new GenericDialog("Image selection");
               gd2.addMessage("Select original grayscale image:");
               String current = WindowManager.getCurrentImage().getTitle();
               gd2.addChoice("original_image", titles, current);
               gd2.showDialog();
               if (gd2.wasCanceled()) {
                  return;
               }

               origIP = WindowManager.getImage(ids[gd2.getNextChoiceIndex()]);
               this.bPruneCycles = true;
         }

         this.run(pruneIndex, pruneEnds, calculateShortestPath, origIP, false, verbose);
         this.showResults();
      }
   }

   public SkeletonResult run(int pruneIndex, boolean pruneEnds, boolean shortPath, ImagePlus origIP, boolean silent, boolean verbose) {
      AnalyzeSkeleton.pruneIndex = pruneIndex;
      this.silent = silent;
      AnalyzeSkeleton.pruneEnds = pruneEnds;
      calculateShortestPath = shortPath;
      AnalyzeSkeleton.verbose = verbose;
      switch(pruneIndex) {
         case 0:
            this.bPruneCycles = false;
            break;
         case 1:
            this.bPruneCycles = true;
            break;
         case 2:
         case 3:
            this.calculateNeighborhoodOffsets(origIP.getCalibration());
            this.originalImage = origIP.getStack();
            this.bPruneCycles = true;
      }

      this.width = this.imRef.getWidth();
      this.height = this.imRef.getHeight();
      this.depth = this.imRef.getStackSize();
      this.inputImage = this.imRef.getStack();
      this.resetVisited();
      this.processSkeleton(this.inputImage);
      if (pruneEnds) {
         this.pruneEndBranches(this.inputImage, this.taggedImage);
      }

      if (this.bPruneCycles && this.pruneCycles(this.inputImage, this.originalImage, AnalyzeSkeleton.pruneIndex)) {
         this.resetVisited();
         this.bPruneCycles = false;
         this.processSkeleton(this.inputImage);
      }

      this.calculateTripleAndQuadruplePoints();
      if (shortPath) {
         this.shortPathImage = new ImageStack(this.width, this.height, this.inputImage.getColorModel());

         for(int i = 1; i <= this.inputImage.getSize(); ++i) {
            this.shortPathImage.addSlice(this.inputImage.getSliceLabel(i), this.inputImage.getProcessor(i).duplicate());
         }

         this.shortestPathList = new ArrayList<>();
         this.spStartPosition = new double[this.numOfTrees][3];

         for(int i = 0; i < this.numOfTrees; ++i) {
            this.shortestPath = this.warshallAlgorithm(this.graph[i]);
            this.shortestPathList.add(this.shortestPath);
            this.spStartPosition[i][0] = (double)this.spx * this.imRef.getCalibration().pixelWidth;
            this.spStartPosition[i][1] = (double)this.spy * this.imRef.getCalibration().pixelHeight;
            this.spStartPosition[i][2] = (double)this.spz * this.imRef.getCalibration().pixelDepth;
         }

         ImagePlus shortIP = new ImagePlus("Longest shortest paths", this.shortPathImage);
         shortIP.show();
         shortIP.setCalibration(this.imRef.getCalibration());
         IJ.run(shortIP, "Fire", null);
         shortIP.resetDisplayRange();
         shortIP.updateAndDraw();
      }

      return this.assembleResults();
   }

   public Graph[] getGraphs() {
      return this.graph;
   }

   public SkeletonResult run() {
      return this.run(0, false, false, null, true, false);
   }

   private void pruneEndBranches(ImageStack stack, ImageStack taggedImage) {
      for(int t = 0; t < this.numOfTrees; ++t) {
         Graph g = this.graph[t];
         ArrayList<Vertex> vertices = g.getVertices();
         ListIterator<Vertex> vit = vertices.listIterator();

         while(vit.hasNext()) {
            Vertex v = vit.next();
            if (v.getBranches().size() == 1) {
               ArrayList<Point> points = v.getPoints();
               int nPoints = points.size();

               for(int i = 0; i < nPoints; ++i) {
                  Point p = points.get(i);
                  this.setPixel(stack, p.x, p.y, p.z, (byte)0);
                  this.setPixel(taggedImage, p.x, p.y, p.z, (byte)0);
                  this.numberOfEndPoints[t]--;
                  --this.totalNumberOfEndPoints;
                  Iterator<Point> pit = this.listOfEndPoints.listIterator();

                  while(pit.hasNext()) {
                     Point ep = pit.next();
                     if (ep.equals(p)) {
                        pit.remove();
                        break;
                     }
                  }
               }

               Edge branch = v.getBranches().get(0);
               points = branch.getSlabs();
               int nSlabs = points.size();

               for(int i = 0; i < nSlabs; ++i) {
                  Point p = points.get(i);
                  this.setPixel(stack, p.x, p.y, p.z, (byte)0);
                  this.setPixel(taggedImage, p.x, p.y, p.z, (byte)0);
                  this.numberOfSlabs[t]--;
                  --this.totalNumberOfSlabs;
                  Iterator<Point> pit = this.listOfSlabVoxels.listIterator();

                  while(pit.hasNext()) {
                     Point ep = pit.next();
                     if (ep.equals(p)) {
                        pit.remove();
                        break;
                     }
                  }
               }

               ArrayList<Edge> gEdges = this.graph[t].getEdges();
               Iterator<Edge> git = gEdges.listIterator();

               while(git.hasNext()) {
                  Edge e = git.next();
                  if (e.equals(branch)) {
                     git.remove();
                     break;
                  }
               }

               Vertex opp = branch.getOppositeVertex(v);
               ArrayList<Edge> oppBranches = opp.getBranches();
               Iterator<Edge> oppIt = oppBranches.listIterator();

               while(oppIt.hasNext()) {
                  Edge oppBranch = oppIt.next();
                  if (oppBranch.equals(branch)) {
                     oppIt.remove();
                     break;
                  }
               }

               v.getBranches().remove(0);
               vit.remove();
            }
         }
      }
   }

   private void calculateNeighborhoodOffsets(Calibration calibration) {
      double max = calibration.pixelDepth;
      if (calibration.pixelHeight > max) {
         max = calibration.pixelHeight;
      }

      if (calibration.pixelWidth > max) {
         max = calibration.pixelWidth;
      }

      this.x_offset = (int)Math.round(max / calibration.pixelWidth) > 1 ? (int)Math.round(max / calibration.pixelWidth) : 1;
      this.y_offset = (int)Math.round(max / calibration.pixelHeight) > 1 ? (int)Math.round(max / calibration.pixelHeight) : 1;
      this.z_offset = (int)Math.round(max / calibration.pixelDepth) > 1 ? (int)Math.round(max / calibration.pixelDepth) : 1;
   }

   public void processSkeleton(ImageStack inputImage2) {
      this.listOfEndPoints = new ArrayList<>();
      this.listOfJunctionVoxels = new ArrayList<>();
      this.listOfSlabVoxels = new ArrayList<>();
      this.listOfStartingSlabVoxels = new ArrayList<>();
      this.totalNumberOfEndPoints = 0;
      this.totalNumberOfJunctionVoxels = 0;
      this.totalNumberOfSlabs = 0;
      this.taggedImage = this.tagImage(inputImage2);
      if (!this.bPruneCycles && !this.silent) {
         this.displayTagImage(this.taggedImage);
      }

      ImageStack treeIS = this.markTrees(this.taggedImage);
      this.initializeTrees();
      if (this.numOfTrees > 1) {
         this.divideVoxelsByTrees(treeIS);
      } else {
         this.endPointsTree[0] = this.listOfEndPoints;
         this.numberOfEndPoints[0] = this.listOfEndPoints.size();
         this.junctionVoxelTree[0] = this.listOfJunctionVoxels;
         this.numberOfJunctionVoxels[0] = this.listOfJunctionVoxels.size();
         this.startingSlabTree[0] = this.listOfStartingSlabVoxels;
      }

      this.groupJunctions(treeIS);
      this.resetVisited();

      for(int i = 0; i < this.numOfTrees; ++i) {
         this.visitSkeleton(this.taggedImage, treeIS, i + 1);
      }
   }

   private boolean pruneCycles(ImageStack inputImage, ImageStack originalImage, int pruningMode) {
      boolean pruned = false;

      for(int iTree = 0; iTree < this.numOfTrees; ++iTree) {
         if (this.startingSlabTree[iTree].size() == 1) {
            this.setPixel(inputImage, this.startingSlabTree[iTree].get(0), (byte)0);
            pruned = true;
         } else {
            ArrayList<Edge> backEdges = this.graph[iTree].depthFirstSearch();
            if (backEdges.size() > 0) {
               for(Edge e : backEdges) {
                  ArrayList<Edge> loopEdges = new ArrayList<>();
                  loopEdges.add(e);
                  Edge minEdge = e;
                  Vertex finalLoopVertex = e.getV1().getVisitOrder() < e.getV2().getVisitOrder() ? e.getV1() : e.getV2();

                  Edge pre;
                  for(Vertex backtrackVertex = e.getV1().getVisitOrder() < e.getV2().getVisitOrder() ? e.getV2() : e.getV1();
                     !finalLoopVertex.equals(backtrackVertex);
                     backtrackVertex = pre.getV1().equals(backtrackVertex) ? pre.getV2() : pre.getV1()
                  ) {
                     pre = backtrackVertex.getPredecessor();
                     if (pruningMode == 1 && pre.getSlabs().size() < minEdge.getSlabs().size()) {
                        minEdge = pre;
                     }

                     loopEdges.add(pre);
                  }

                  if (pruningMode == 1) {
                     Point removeCoords = null;
                     Point var15;
                     if (minEdge.getSlabs().size() > 0) {
                        var15 = minEdge.getSlabs().get(minEdge.getSlabs().size() / 2);
                     } else {
                        var15 = minEdge.getV1().getPoints().get(0);
                     }

                     this.setPixel(inputImage, var15, (byte)0);
                  } else if (pruningMode == 2) {
                     this.removeLowestIntensityVoxel(loopEdges, inputImage, originalImage);
                  } else if (pruningMode == 3) {
                     this.cutLowestIntensityBranch(loopEdges, inputImage, originalImage);
                  }
               }

               pruned = true;
            }
         }
      }

      return pruned;
   }

   private void removeLowestIntensityVoxel(ArrayList<Edge> loopEdges, ImageStack inputImage2, ImageStack originalGrayImage) {
      Point lowestIntensityVoxel = null;
      double lowestIntensityValue = Double.MAX_VALUE;

      for(Edge e : loopEdges) {
         for(Point p : e.getSlabs()) {
            double avg = getAverageNeighborhoodValue(originalGrayImage, p, this.x_offset, this.y_offset, this.z_offset);
            if (avg < lowestIntensityValue) {
               lowestIntensityValue = avg;
               lowestIntensityVoxel = p;
            }
         }
      }

      this.setPixel(inputImage2, lowestIntensityVoxel, (byte)0);
   }

   private void cutLowestIntensityBranch(ArrayList<Edge> loopEdges, ImageStack inputImage2, ImageStack originalGrayImage) {
      Edge lowestIntensityEdge = null;
      double lowestIntensityValue = Double.MAX_VALUE;
      Point cutPoint = null;

      for(Edge e : loopEdges) {
         double min_val = Double.MAX_VALUE;
         Point darkestPoint = null;
         double edgeIntensity = 0.0;
         double n_vox = 0.0;

         for(Point p : e.getSlabs()) {
            double avg = getAverageNeighborhoodValue(originalGrayImage, p, this.x_offset, this.y_offset, this.z_offset);
            if (avg < min_val) {
               min_val = avg;
               darkestPoint = p;
            }

            edgeIntensity += avg;
            ++n_vox;
         }

         for(Point p : e.getV1().getPoints()) {
            edgeIntensity += getAverageNeighborhoodValue(originalGrayImage, p, this.x_offset, this.y_offset, this.z_offset);
            ++n_vox;
         }

         for(Point p : e.getV2().getPoints()) {
            edgeIntensity += getAverageNeighborhoodValue(originalGrayImage, p, this.x_offset, this.y_offset, this.z_offset);
            ++n_vox;
         }

         if (n_vox != 0.0) {
            edgeIntensity /= n_vox;
         }

         if (edgeIntensity < lowestIntensityValue) {
            lowestIntensityEdge = e;
            lowestIntensityValue = edgeIntensity;
            cutPoint = darkestPoint;
         }
      }

      Point removeCoords = null;
      if (lowestIntensityEdge.getSlabs().size() > 0) {
         removeCoords = cutPoint;
      } else {
         IJ.error("Lowest intensity branch without slabs?!: vertex " + lowestIntensityEdge.getV1().getPoints().get(0));
         removeCoords = lowestIntensityEdge.getV1().getPoints().get(0);
      }

      this.setPixel(inputImage2, removeCoords, (byte)0);
   }

   void displayTagImage(ImageStack taggedImage) {
      ImagePlus tagIP = new ImagePlus("Tagged skeleton", taggedImage);
      tagIP.setCalibration(this.imRef.getCalibration());
      IJ.run(tagIP, "Fire", null);
      tagIP.resetDisplayRange();
      tagIP.updateAndDraw();
   }

   private void divideVoxelsByTrees(ImageStack treeIS) {
      for(int i = 0; i < this.totalNumberOfEndPoints; ++i) {
         Point p = this.listOfEndPoints.get(i);
         this.endPointsTree[this.getShortPixel(treeIS, p) - 1].add(p);
      }

      for(int i = 0; i < this.totalNumberOfJunctionVoxels; ++i) {
         Point p = this.listOfJunctionVoxels.get(i);
         this.junctionVoxelTree[this.getShortPixel(treeIS, p) - 1].add(p);
      }

      for(int i = 0; i < this.listOfStartingSlabVoxels.size(); ++i) {
         Point p = this.listOfStartingSlabVoxels.get(i);
         this.startingSlabTree[this.getShortPixel(treeIS, p) - 1].add(p);
      }

      for(int iTree = 0; iTree < this.numOfTrees; ++iTree) {
         this.numberOfEndPoints[iTree] = this.endPointsTree[iTree].size();
         this.numberOfJunctionVoxels[iTree] = this.junctionVoxelTree[iTree].size();
      }
   }

   private void initializeTrees() {
      this.numberOfBranches = new int[this.numOfTrees];
      this.numberOfEndPoints = new int[this.numOfTrees];
      this.numberOfJunctionVoxels = new int[this.numOfTrees];
      this.numberOfJunctions = new int[this.numOfTrees];
      this.numberOfSlabs = new int[this.numOfTrees];
      this.numberOfTriplePoints = new int[this.numOfTrees];
      this.numberOfQuadruplePoints = new int[this.numOfTrees];
      this.averageBranchLength = new double[this.numOfTrees];
      this.maximumBranchLength = new double[this.numOfTrees];
      this.endPointsTree = new ArrayList[this.numOfTrees];
      this.junctionVoxelTree = new ArrayList[this.numOfTrees];
      this.startingSlabTree = new ArrayList[this.numOfTrees];
      this.listOfSingleJunctions = new ArrayList[this.numOfTrees];
      this.graph = new Graph[this.numOfTrees];

      for(int i = 0; i < this.numOfTrees; ++i) {
         this.endPointsTree[i] = new ArrayList<>();
         this.junctionVoxelTree[i] = new ArrayList<>();
         this.startingSlabTree[i] = new ArrayList<>();
         this.listOfSingleJunctions[i] = new ArrayList<>();
      }

      this.junctionVertex = new Vertex[this.numOfTrees][];
   }

   private void showResults() {
      ResultsTable rt = new ResultsTable();
      String[] head = new String[]{
         "Skeleton",
         "# Branches",
         "# Junctions",
         "# End-point voxels",
         "# Junction voxels",
         "# Slab voxels",
         "Average Branch Length",
         "# Triple points",
         "# Quadruple points",
         "Maximum Branch Length",
         "Longest Shortest Path",
         "spx",
         "spy",
         "spz"
      };

      for(int i = 0; i < this.numOfTrees; ++i) {
         rt.incrementCounter();
         rt.addValue(head[1], (double)this.numberOfBranches[i]);
         rt.addValue(head[2], (double)this.numberOfJunctions[i]);
         rt.addValue(head[3], (double)this.numberOfEndPoints[i]);
         rt.addValue(head[4], (double)this.numberOfJunctionVoxels[i]);
         rt.addValue(head[5], (double)this.numberOfSlabs[i]);
         rt.addValue(head[6], this.averageBranchLength[i]);
         rt.addValue(head[7], (double)this.numberOfTriplePoints[i]);
         rt.addValue(head[8], (double)this.numberOfQuadruplePoints[i]);
         rt.addValue(head[9], this.maximumBranchLength[i]);
         if (null != this.shortestPathList) {
            rt.addValue(head[10], this.shortestPathList.get(i));
            rt.addValue(head[11], this.spStartPosition[i][0]);
            rt.addValue(head[12], this.spStartPosition[i][1]);
            rt.addValue(head[13], this.spStartPosition[i][2]);
         }

         if (0 == i % 100) {
            rt.show("Results");
         }
      }

      rt.show("Results");
      if (verbose) {
         ResultsTable extra_rt = new ResultsTable();
         String[] extra_head = new String[]{"Branch", "Skeleton ID", "Branch length", "V1 x", "V1 y", "V1 z", "V2 x", "V2 y", "V2 z", "Euclidean distance"};
         Comparator<Edge> comp = new Comparator<Edge>() {
            public int compare(Edge o1, Edge o2) {
               double diff = o1.getLength() - o2.getLength();
               if (diff < 0.0) {
                  return 1;
               } else {
                  return diff == 0.0 ? 0 : -1;
               }
            }

            @Override
            public boolean equals(Object o) {
               return false;
            }
         };

         for(int i = 0; i < this.numOfTrees; ++i) {
            ArrayList<Edge> listEdges = this.graph[i].getEdges();
            Collections.sort(listEdges, comp);

            for(Edge e : listEdges) {
               extra_rt.incrementCounter();
               extra_rt.addValue(extra_head[1], (double)(i + 1));
               extra_rt.addValue(extra_head[2], e.getLength());
               extra_rt.addValue(extra_head[3], (double)e.getV1().getPoints().get(0).x * this.imRef.getCalibration().pixelWidth);
               extra_rt.addValue(extra_head[4], (double)e.getV1().getPoints().get(0).y * this.imRef.getCalibration().pixelHeight);
               extra_rt.addValue(extra_head[5], (double)e.getV1().getPoints().get(0).z * this.imRef.getCalibration().pixelDepth);
               extra_rt.addValue(extra_head[6], (double)e.getV2().getPoints().get(0).x * this.imRef.getCalibration().pixelWidth);
               extra_rt.addValue(extra_head[7], (double)e.getV2().getPoints().get(0).y * this.imRef.getCalibration().pixelHeight);
               extra_rt.addValue(extra_head[8], (double)e.getV2().getPoints().get(0).z * this.imRef.getCalibration().pixelDepth);
               extra_rt.addValue(extra_head[9], this.calculateDistance(e.getV1().getPoints().get(0), e.getV2().getPoints().get(0)));
            }
         }

         extra_rt.show("Branch information");
      }
   }

   protected SkeletonResult assembleResults() {
      SkeletonResult result = new SkeletonResult(this.numOfTrees);
      result.setBranches(this.numberOfBranches);
      result.setJunctions(this.numberOfJunctions);
      result.setEndPoints(this.numberOfEndPoints);
      result.setJunctionVoxels(this.numberOfJunctionVoxels);
      result.setSlabs(this.numberOfSlabs);
      result.setAverageBranchLength(this.averageBranchLength);
      result.setTriples(this.numberOfTriplePoints);
      result.setQuadruples(this.numberOfQuadruplePoints);
      result.setMaximumBranchLength(this.maximumBranchLength);
      result.setListOfEndPoints(this.listOfEndPoints);
      result.setListOfJunctionVoxels(this.listOfJunctionVoxels);
      result.setListOfSlabVoxels(this.listOfSlabVoxels);
      result.setListOfStartingSlabVoxels(this.listOfStartingSlabVoxels);
      result.setGraph(this.graph);
      result.calculateNumberOfVoxels();
      return result;
   }

   /** @deprecated */
   private void visitSkeleton(ImageStack taggedImage) {
      double branchLength = 0.0;
      int numberOfBranches = 0;
      double maximumBranchLength = 0.0;
      double averageBranchLength = 0.0;
      Point initialPoint = null;
      Point finalPoint = null;

      for(int i = 0; i < this.totalNumberOfEndPoints; ++i) {
         Point endPointCoord = this.listOfEndPoints.get(i);
         double length = this.visitBranch(endPointCoord);
         if (length != 0.0) {
            ++numberOfBranches;
            branchLength += length;
            if (length > maximumBranchLength) {
               maximumBranchLength = length;
               finalPoint = this.auxPoint;
            }
         }
      }

      for(int i = 0; i < this.totalNumberOfJunctionVoxels; ++i) {
         Point junctionCoord = this.listOfJunctionVoxels.get(i);
         this.setVisited(junctionCoord, true);

         for(Point nextPoint = this.getNextUnvisitedVoxel(junctionCoord); nextPoint != null; nextPoint = this.getNextUnvisitedVoxel(junctionCoord)) {
            branchLength += this.calculateDistance(junctionCoord, nextPoint);
            double length = this.visitBranch(nextPoint);
            branchLength += length;
            if (length != 0.0) {
               ++numberOfBranches;
               if (length > maximumBranchLength) {
                  maximumBranchLength = length;
                  finalPoint = this.auxPoint;
               }
            }
         }
      }

      averageBranchLength = branchLength / (double)numberOfBranches;
   }

   private void visitSkeleton(ImageStack taggedImage, ImageStack treeImage, int currentTree) {
      int iTree = currentTree - 1;
      this.graph[iTree] = new Graph();

      for(int i = 0; i < this.junctionVertex[iTree].length; ++i) {
         this.graph[iTree].addVertex(this.junctionVertex[iTree][i]);
      }

      double branchLength = 0.0;
      this.maximumBranchLength[iTree] = 0.0;
      this.numberOfSlabs[iTree] = 0;

      for(int i = 0; i < this.numberOfEndPoints[iTree]; ++i) {
         Point endPointCoord = this.endPointsTree[iTree].get(i);
         if (!this.isVisited(endPointCoord)) {
            Vertex v1 = new Vertex();
            v1.addPoint(endPointCoord);
            this.graph[iTree].addVertex(v1);
            if (i == 0) {
               this.graph[iTree].setRoot(v1);
            }

            this.slabList = new ArrayList<>();
            double length = this.visitBranch(endPointCoord, iTree);
            if (length != 0.0) {
               if (this.isSlab(this.auxPoint)) {
                  Point aux = this.auxPoint;
                  this.auxPoint = this.getVisitedJunctionNeighbor(this.auxPoint, v1);
                  this.auxFinalVertex = this.findPointVertex(this.junctionVertex[iTree], this.auxPoint);
                  if (this.auxPoint == null) {
                     this.auxFinalVertex = v1;
                     this.auxPoint = aux;
                  }

                  length += this.calculateDistance(this.auxPoint, aux);
               }

               this.graph[iTree].addVertex(this.auxFinalVertex);
               this.graph[iTree].addEdge(new Edge(v1, this.auxFinalVertex, this.slabList, length));
               this.numberOfBranches[iTree]++;
               branchLength += length;
               if (length > this.maximumBranchLength[iTree]) {
                  this.maximumBranchLength[iTree] = length;
               }
            }
         }
      }

      if (this.numberOfEndPoints[iTree] == 0 && this.junctionVoxelTree[iTree].size() > 0) {
         this.graph[iTree].setRoot(this.junctionVertex[iTree][0]);
      }

      for(int i = 0; i < this.junctionVertex[iTree].length; ++i) {
         for(int j = 0; j < this.junctionVertex[iTree][i].getPoints().size(); ++j) {
            Point junctionCoord = this.junctionVertex[iTree][i].getPoints().get(j);
            this.setVisited(junctionCoord, true);

            for(Point nextPoint = this.getNextUnvisitedVoxel(junctionCoord); nextPoint != null; nextPoint = this.getNextUnvisitedVoxel(junctionCoord)) {
               if (this.isJunction(nextPoint)) {
                  this.setVisited(nextPoint, true);
               } else {
                  this.slabList = new ArrayList<>();
                  this.slabList.add(nextPoint);
                  double length = this.calculateDistance(junctionCoord, nextPoint);
                  this.auxPoint = null;
                  length += this.visitBranch(nextPoint, iTree);
                  branchLength += length;
                  if (length != 0.0) {
                     if (this.auxPoint == null) {
                        this.auxPoint = nextPoint;
                     }

                     this.numberOfBranches[iTree]++;
                     Vertex initialVertex = null;

                     for(int k = 0; k < this.junctionVertex[iTree].length; ++k) {
                        if (this.junctionVertex[iTree][k].isVertexPoint(junctionCoord)) {
                           initialVertex = this.junctionVertex[iTree][k];
                           break;
                        }
                     }

                     if (this.isSlab(this.auxPoint)) {
                        Point aux = this.auxPoint;
                        this.auxPoint = this.getVisitedJunctionNeighbor(this.auxPoint, initialVertex);
                        this.auxFinalVertex = this.findPointVertex(this.junctionVertex[iTree], this.auxPoint);
                        if (this.auxPoint == null) {
                           this.auxFinalVertex = initialVertex;
                           this.auxPoint = aux;
                        }

                        length += this.calculateDistance(this.auxPoint, aux);
                     }

                     if (length > this.maximumBranchLength[iTree]) {
                        this.maximumBranchLength[iTree] = length;
                     }

                     this.graph[iTree].addEdge(new Edge(initialVertex, this.auxFinalVertex, this.slabList, length));
                  }
               }
            }
         }
      }

      if (this.startingSlabTree[iTree].size() == 1) {
         Point startCoord = this.startingSlabTree[iTree].get(0);
         Vertex v1 = new Vertex();
         v1.addPoint(startCoord);
         this.graph[iTree].addVertex(v1);
         this.slabList = new ArrayList<>();
         this.slabList.add(startCoord);
         this.numberOfSlabs[iTree]++;
         double length = this.visitBranch(startCoord, iTree);
         if (length != 0.0) {
            this.numberOfBranches[iTree]++;
            branchLength += length;
            if (length > this.maximumBranchLength[iTree]) {
               this.maximumBranchLength[iTree] = length;
            }
         }

         this.graph[iTree].addEdge(new Edge(v1, v1, this.slabList, length));
      }

      if (this.numberOfBranches[iTree] != 0) {
         this.averageBranchLength[iTree] = branchLength / (double)this.numberOfBranches[iTree];
      }
   }

   private ImageStack markTrees(ImageStack taggedImage) {
      ImageStack outputImage = new ImageStack(this.width, this.height, taggedImage.getColorModel());

      for(int z = 0; z < this.depth; ++z) {
         outputImage.addSlice(taggedImage.getSliceLabel(z + 1), new ShortProcessor(this.width, this.height));
      }

      this.numOfTrees = 0;
      short color = 0;

      for(int i = 0; i < this.totalNumberOfEndPoints; ++i) {
         Point endPointCoord = this.listOfEndPoints.get(i);
         if (!this.isVisited(endPointCoord)) {
            if (++color == 32767) {
               IJ.error("More than 32766 skeletons in the image. AnalyzeSkeleton can only process up to 32766");
               return null;
            }

            int numOfVoxelsInTree = this.visitTree(endPointCoord, outputImage, color);
            ++this.numOfTrees;
         }
      }

      for(int i = 0; i < this.totalNumberOfJunctionVoxels; ++i) {
         Point junctionCoord = this.listOfJunctionVoxels.get(i);
         if (!this.isVisited(junctionCoord)) {
            if (++color == 32767) {
               IJ.error("More than 32766 skeletons in the image. AnalyzeSkeleton can only process up to 255");
               return null;
            }

            int length = this.visitTree(junctionCoord, outputImage, color);
            if (length == 0) {
               --color;
            } else {
               ++this.numOfTrees;
            }
         }
      }

      for(int i = 0; i < this.listOfSlabVoxels.size(); ++i) {
         Point p = this.listOfSlabVoxels.get(i);
         if (!this.isVisited(p)) {
            this.listOfStartingSlabVoxels.add(p);
            if (++color == 32767) {
               IJ.error("More than 32766 skeletons in the image. AnalyzeSkeleton can only process up to 255");
               return null;
            }

            int length = this.visitTree(p, outputImage, color);
            if (length == 0) {
               --color;
            } else {
               ++this.numOfTrees;
            }
         }
      }

      this.numOfTrees = this.numOfTrees > 0 ? this.numOfTrees : 1;

      this.resetVisited();
      return outputImage;
   }

   private int visitTree(Point startingPoint, ImageStack outputImage, short color) {
      int numOfVoxels = 0;
      if (this.isVisited(startingPoint)) {
         return 0;
      } else {
         this.setPixel(outputImage, startingPoint.x, startingPoint.y, startingPoint.z, color);
         this.setVisited(startingPoint, true);
         ArrayList<Point> toRevisit = new ArrayList<>();
         if (this.isJunction(startingPoint)) {
            toRevisit.add(startingPoint);
         }

         Point nextPoint = this.getNextUnvisitedVoxel(startingPoint);

         while(nextPoint != null || toRevisit.size() != 0) {
            if (nextPoint != null) {
               if (!this.isVisited(nextPoint)) {
                  ++numOfVoxels;
                  this.setPixel(outputImage, nextPoint.x, nextPoint.y, nextPoint.z, color);
                  this.setVisited(nextPoint, true);
                  if (this.isJunction(nextPoint)) {
                     toRevisit.add(nextPoint);
                  }

                  nextPoint = this.getNextUnvisitedVoxel(nextPoint);
               }
            } else {
               nextPoint = toRevisit.get(0);
               nextPoint = this.getNextUnvisitedVoxel(nextPoint);
               if (nextPoint == null) {
                  toRevisit.remove(0);
               }
            }
         }

         return numOfVoxels;
      }
   }

   /** @deprecated */
   private double visitBranch(Point startingPoint) {
      double length = 0.0;
      this.setVisited(startingPoint, true);
      Point nextPoint = this.getNextUnvisitedVoxel(startingPoint);
      if (nextPoint == null) {
         return 0.0;
      } else {
         Point previousPoint;
         for(previousPoint = startingPoint; nextPoint != null && this.isSlab(nextPoint); nextPoint = this.getNextUnvisitedVoxel(nextPoint)) {
            length += this.calculateDistance(previousPoint, nextPoint);
            this.setVisited(nextPoint, true);
            previousPoint = nextPoint;
         }

         if (nextPoint != null) {
            length += this.calculateDistance(previousPoint, nextPoint);
            this.setVisited(nextPoint, true);
         }

         this.auxPoint = previousPoint;
         return length;
      }
   }

   private double visitBranch(Point startingPoint, int iTree) {
      double length = 0.0;
      this.setVisited(startingPoint, true);
      Point nextPoint = this.getNextUnvisitedVoxel(startingPoint);
      if (nextPoint == null) {
         return 0.0;
      } else {
         Point previousPoint;
         for(previousPoint = startingPoint; nextPoint != null && this.isSlab(nextPoint); nextPoint = this.getNextUnvisitedVoxel(nextPoint)) {
            this.numberOfSlabs[iTree]++;
            this.slabList.add(nextPoint);
            length += this.calculateDistance(previousPoint, nextPoint);
            this.setVisited(nextPoint, true);
            previousPoint = nextPoint;
         }

         if (nextPoint != null) {
            length += this.calculateDistance(previousPoint, nextPoint);
            this.setVisited(nextPoint, true);
            if (this.isEndPoint(nextPoint)) {
               this.auxFinalVertex = new Vertex();
               this.auxFinalVertex.addPoint(nextPoint);
            } else if (this.isJunction(nextPoint)) {
               this.auxFinalVertex = this.findPointVertex(this.junctionVertex[iTree], nextPoint);
            }

            this.auxPoint = nextPoint;
         } else {
            this.auxPoint = previousPoint;
         }

         return length;
      }
   }

   public Vertex findPointVertex(Vertex[] vertex, Point p) {
      int j = 0;

      for(int var4 = 0; var4 < vertex.length; ++var4) {
         if (vertex[var4].isVertexPoint(p)) {
            return vertex[var4];
         }
      }

      return null;
   }

   private double calculateDistance(Point point1, Point point2) {
      return Math.sqrt(
         Math.pow((double)(point1.x - point2.x) * this.imRef.getCalibration().pixelWidth, 2.0)
            + Math.pow((double)(point1.y - point2.y) * this.imRef.getCalibration().pixelHeight, 2.0)
            + Math.pow((double)(point1.z - point2.z) * this.imRef.getCalibration().pixelDepth, 2.0)
      );
   }

   private void groupJunctions(ImageStack treeIS) {
      this.resetVisited();

      for(int iTree = 0; iTree < this.numOfTrees; ++iTree) {
         for(int i = 0; i < this.numberOfJunctionVoxels[iTree]; ++i) {
            Point pi = this.junctionVoxelTree[iTree].get(i);
            if (!this.isVisited(pi)) {
               this.fusionNeighborJunction(pi, this.listOfSingleJunctions[iTree]);
            }
         }
      }

      for(int iTree = 0; iTree < this.numOfTrees; ++iTree) {
         this.numberOfJunctions[iTree] = this.listOfSingleJunctions[iTree].size();
         this.junctionVertex[iTree] = new Vertex[this.listOfSingleJunctions[iTree].size()];

         for(int j = 0; j < this.listOfSingleJunctions[iTree].size(); ++j) {
            ArrayList<Point> list = this.listOfSingleJunctions[iTree].get(j);
            this.junctionVertex[iTree][j] = new Vertex();

            for(Point p : list) {
               this.junctionVertex[iTree][j].addPoint(p);
            }
         }
      }

      this.resetVisited();
   }

   private void resetVisited() {
      this.visited = (boolean[][][])null;
      this.visited = new boolean[this.width][this.height][this.depth];
   }

   private void fusionNeighborJunction(Point startingPoint, ArrayList<ArrayList<Point>> singleJunctionsList) {
      ArrayList<Point> newGroup = new ArrayList<>();
      newGroup.add(startingPoint);
      this.setVisited(startingPoint, true);
      ArrayList<Point> toRevisit = new ArrayList<>();
      toRevisit.add(startingPoint);
      Point nextPoint = this.getNextUnvisitedJunctionVoxel(startingPoint);

      while(nextPoint != null || toRevisit.size() != 0) {
         if (nextPoint != null && !this.isVisited(nextPoint)) {
            newGroup.add(nextPoint);
            this.setVisited(nextPoint, true);
            toRevisit.add(nextPoint);
            nextPoint = this.getNextUnvisitedJunctionVoxel(nextPoint);
         } else {
            nextPoint = toRevisit.get(0);
            nextPoint = this.getNextUnvisitedJunctionVoxel(nextPoint);
            if (nextPoint == null) {
               toRevisit.remove(0);
            }
         }
      }

      singleJunctionsList.add(newGroup);
   }

   boolean checkNeighborGroups(ArrayList<Point> g1, ArrayList<Point> g2) {
      for(int i = 0; i < g1.size(); ++i) {
         Point pi = g1.get(i);

         for(int j = 0; j < g2.size(); ++j) {
            Point pj = g2.get(j);
            if (this.isNeighbor(pi, pj)) {
               return true;
            }
         }
      }

      return false;
   }

   private void calculateTripleAndQuadruplePoints() {
      for(int iTree = 0; iTree < this.numOfTrees; ++iTree) {
         for(int i = 0; i < this.numberOfJunctions[iTree]; ++i) {
            ArrayList<Point> groupOfJunctions = this.listOfSingleJunctions[iTree].get(i);
            int nBranch = 0;

            for(int j = 0; j < groupOfJunctions.size(); ++j) {
               Point pj = groupOfJunctions.get(j);
               byte[] neighborhood = this.getNeighborhood(this.taggedImage, pj.x, pj.y, pj.z);

               for(int k = 0; k < 27; ++k) {
                  if (neighborhood[k] == SLAB || neighborhood[k] == END_POINT) {
                     ++nBranch;
                  }
               }
            }

            if (nBranch == 3) {
               this.numberOfTriplePoints[iTree]++;
            } else if (nBranch == 4) {
               this.numberOfQuadruplePoints[iTree]++;
            }
         }
      }
   }

   private boolean isNeighbor(Point point1, Point point2) {
      return Math.sqrt(
            Math.pow((double)(point1.x - point2.x), 2.0) + Math.pow((double)(point1.y - point2.y), 2.0) + Math.pow((double)(point1.z - point2.z), 2.0)
         )
         <= Math.sqrt(3.0);
   }

   private boolean isSlab(Point point) {
      return getPixel(this.taggedImage, point.x, point.y, point.z) == SLAB;
   }

   private boolean isJunction(Point point) {
      return getPixel(this.taggedImage, point.x, point.y, point.z) == JUNCTION;
   }

   private boolean isEndPoint(Point point) {
      return getPixel(this.taggedImage, point.x, point.y, point.z) == END_POINT;
   }

   private boolean isJunction(int x, int y, int z) {
      return getPixel(this.taggedImage, x, y, z) == JUNCTION;
   }

   private Point getNextUnvisitedVoxel(Point point) {
      Point unvisitedNeighbor = null;

      for(int x = -1; x < 2; ++x) {
         for(int y = -1; y < 2; ++y) {
            for(int z = -1; z < 2; ++z) {
               if ((x != 0 || y != 0 || z != 0)
                  && getPixel(this.inputImage, point.x + x, point.y + y, point.z + z) != 0
                  && !this.isVisited(point.x + x, point.y + y, point.z + z)) {
                  unvisitedNeighbor = new Point(point.x + x, point.y + y, point.z + z);
                  break;
               }
            }
         }
      }

      return unvisitedNeighbor;
   }

   private Point getNextUnvisitedJunctionVoxel(Point point) {
      Point unvisitedNeighbor = null;

      for(int x = -1; x < 2; ++x) {
         for(int y = -1; y < 2; ++y) {
            for(int z = -1; z < 2; ++z) {
               if ((x != 0 || y != 0 || z != 0)
                  && getPixel(this.inputImage, point.x + x, point.y + y, point.z + z) != 0
                  && !this.isVisited(point.x + x, point.y + y, point.z + z)
                  && this.isJunction(point.x + x, point.y + y, point.z + z)) {
                  unvisitedNeighbor = new Point(point.x + x, point.y + y, point.z + z);
                  break;
               }
            }
         }
      }

      return unvisitedNeighbor;
   }

   private Point getVisitedJunctionNeighbor(Point point, Vertex exclude) {
      Point finalNeighbor = null;

      for(int x = -1; x < 2; ++x) {
         for(int y = -1; y < 2; ++y) {
            for(int z = -1; z < 2; ++z) {
               if (x != 0 || y != 0 || z != 0) {
                  Point neighbor = new Point(point.x + x, point.y + y, point.z + z);
                  if (this.getPixel(this.inputImage, neighbor) != 0
                     && this.isVisited(neighbor)
                     && this.isJunction(neighbor)
                     && !exclude.getPoints().contains(neighbor)) {
                     finalNeighbor = neighbor;
                     break;
                  }
               }
            }
         }
      }

      return finalNeighbor;
   }

   private boolean isVisited(Point point) {
      return this.isVisited(point.x, point.y, point.z);
   }

   private boolean isVisited(int x, int y, int z) {
      return x >= 0 && x < this.width && y >= 0 && y < this.height && z >= 0 && z < this.depth ? this.visited[x][y][z] : true;
   }

   private void setVisited(int x, int y, int z, boolean b) {
      if (x >= 0 && x < this.width && y >= 0 && y < this.height && z >= 0 && z < this.depth) {
         this.visited[x][y][z] = b;
      }
   }

   private void setVisited(Point point, boolean b) {
      int x = point.x;
      int y = point.y;
      int z = point.z;
      this.setVisited(x, y, z, b);
   }

   private ImageStack tagImage(ImageStack inputImage2) {
      ImageStack outputImage = new ImageStack(this.width, this.height, inputImage2.getColorModel());

      for(int z = 0; z < this.depth; ++z) {
         outputImage.addSlice(inputImage2.getSliceLabel(z + 1), new ByteProcessor(this.width, this.height));

         for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
               if (getPixel(inputImage2, x, y, z) != 0) {
                  int numOfNeighbors = this.getNumberOfNeighbors(inputImage2, x, y, z);
                  if (numOfNeighbors < 2) {
                     this.setPixel(outputImage, x, y, z, END_POINT);
                     ++this.totalNumberOfEndPoints;
                     Point endPoint = new Point(x, y, z);
                     this.listOfEndPoints.add(endPoint);
                  } else if (numOfNeighbors > 2) {
                     this.setPixel(outputImage, x, y, z, JUNCTION);
                     Point junction = new Point(x, y, z);
                     this.listOfJunctionVoxels.add(junction);
                     ++this.totalNumberOfJunctionVoxels;
                  } else {
                     this.setPixel(outputImage, x, y, z, SLAB);
                     Point slab = new Point(x, y, z);
                     this.listOfSlabVoxels.add(slab);
                     ++this.totalNumberOfSlabs;
                  }
               }
            }
         }
      }

      return outputImage;
   }

   private int getNumberOfNeighbors(ImageStack image, int x, int y, int z) {
      int n = 0;
      byte[] neighborhood = this.getNeighborhood(image, x, y, z);

      for(int i = 0; i < 27; ++i) {
         if (neighborhood[i] != 0) {
            ++n;
         }
      }

      return n - 1;
   }

   private double getAverageNeighborhoodValue(ImageStack image, Point p) {
      byte[] neighborhood = this.getNeighborhood(image, p);
      double avg = 0.0;

      for(int i = 0; i < neighborhood.length; ++i) {
         avg += (double)(neighborhood[i] & 255);
      }

      return neighborhood.length > 0 ? avg / (double)neighborhood.length : 0.0;
   }

   public static double getAverageNeighborhoodValue(ImageStack image, Point p, int x_offset, int y_offset, int z_offset) {
      byte[] neighborhood = getNeighborhood(image, p, x_offset, y_offset, z_offset);
      double avg = 0.0;

      for(int i = 0; i < neighborhood.length; ++i) {
         avg += (double)(neighborhood[i] & 255);
      }

      return neighborhood.length > 0 ? avg / (double)neighborhood.length : 0.0;
   }

   public static byte[] getNeighborhood(ImageStack image, Point p, int x_offset, int y_offset, int z_offset) {
      byte[] neighborhood = new byte[(2 * x_offset + 1) * (2 * y_offset + 1) * (2 * z_offset + 1)];
      int l = 0;

      for(int k = p.z - x_offset; k < p.z + z_offset; ++k) {
         for(int j = p.y - y_offset; j < p.y + y_offset; ++j) {
            for(int i = p.x - x_offset; i < p.x + x_offset; ++l) {
               neighborhood[l] = getPixel(image, i, j, k);
               ++i;
            }
         }
      }

      return neighborhood;
   }

   private byte[] getNeighborhood(ImageStack image, Point p) {
      return this.getNeighborhood(image, p.x, p.y, p.z);
   }

   private byte[] getNeighborhood(ImageStack image, int x, int y, int z) {
      return new byte[]{
         getPixel(image, x - 1, y - 1, z - 1),
         getPixel(image, x, y - 1, z - 1),
         getPixel(image, x + 1, y - 1, z - 1),
         getPixel(image, x - 1, y, z - 1),
         getPixel(image, x, y, z - 1),
         getPixel(image, x + 1, y, z - 1),
         getPixel(image, x - 1, y + 1, z - 1),
         getPixel(image, x, y + 1, z - 1),
         getPixel(image, x + 1, y + 1, z - 1),
         getPixel(image, x - 1, y - 1, z),
         getPixel(image, x, y - 1, z),
         getPixel(image, x + 1, y - 1, z),
         getPixel(image, x - 1, y, z),
         getPixel(image, x, y, z),
         getPixel(image, x + 1, y, z),
         getPixel(image, x - 1, y + 1, z),
         getPixel(image, x, y + 1, z),
         getPixel(image, x + 1, y + 1, z),
         getPixel(image, x - 1, y - 1, z + 1),
         getPixel(image, x, y - 1, z + 1),
         getPixel(image, x + 1, y - 1, z + 1),
         getPixel(image, x - 1, y, z + 1),
         getPixel(image, x, y, z + 1),
         getPixel(image, x + 1, y, z + 1),
         getPixel(image, x - 1, y + 1, z + 1),
         getPixel(image, x, y + 1, z + 1),
         getPixel(image, x + 1, y + 1, z + 1)
      };
   }

   public static byte getPixel(ImageStack image, int x, int y, int z) {
      int width = image.getWidth();
      int height = image.getHeight();
      int depth = image.getSize();
      return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth ? ((byte[])image.getPixels(z + 1))[x + y * width] : 0;
   }

   private short getShortPixel(ImageStack image, int x, int y, int z) {
      return x >= 0 && x < this.width && y >= 0 && y < this.height && z >= 0 && z < this.depth ? ((short[])image.getPixels(z + 1))[x + y * this.width] : 0;
   }

   private short getShortPixel(ImageStack image, Point point) {
      return this.getShortPixel(image, point.x, point.y, point.z);
   }

   private byte getPixel(ImageStack image, Point point) {
      return getPixel(image, point.x, point.y, point.z);
   }

   private void setPixel(ImageStack image, Point p, byte value) {
      if (p.x >= 0 && p.x < this.width && p.y >= 0 && p.y < this.height && p.z >= 0 && p.z < this.depth) {
         ((byte[])image.getPixels(p.z + 1))[p.x + p.y * this.width] = value;
      }
   }

   private void setPixel(ImageStack image, int x, int y, int z, byte value) {
      if (x >= 0 && x < this.width && y >= 0 && y < this.height && z >= 0 && z < this.depth) {
         ((byte[])image.getPixels(z + 1))[x + y * this.width] = value;
      }
   }

   private void setPixel(ImageStack image, int x, int y, int z, short value) {
      if (x >= 0 && x < this.width && y >= 0 && y < this.height && z >= 0 && z < this.depth) {
         ((short[])image.getPixels(z + 1))[x + y * this.width] = value;
      }
   }

   void showAbout() {
      IJ.showMessage("About AnalyzeSkeleton...", "This plug-in filter analyzes a 2D/3D image skeleton.\n");
   }

   private double warshallAlgorithm(Graph graph) {
      Vertex v1 = null;
      Vertex v2 = null;
      int row = 0;
      int column = 0;
      double maxPath = 0.0;
      int a = 0;
      int b = 0;
      ArrayList<Edge> edgeList = graph.getEdges();
      ArrayList<Vertex> vertexList = graph.getVertices();
      double[][] adjacencyMatrix = new double[vertexList.size()][vertexList.size()];
      int[][] predecessorMatrix = new int[vertexList.size()][vertexList.size()];

      for(int i = 0; i < vertexList.size(); ++i) {
         for(int j = 0; j < vertexList.size(); ++j) {
            adjacencyMatrix[i][j] = Double.POSITIVE_INFINITY;
            predecessorMatrix[i][j] = -1;
         }
      }

      for(Edge edge : edgeList) {
         v1 = edge.getV1();
         v2 = edge.getV2();
         row = vertexList.indexOf(v1);
         if (row == -1) {
            IJ.log("Vertex " + v1.getPoints().get(0) + " not found in the list of vertices!");
         } else {
            column = vertexList.indexOf(v2);
            if (column == -1) {
               IJ.log("Vertex " + v2.getPoints().get(0) + " not found in the list of vertices!");
            } else {
               adjacencyMatrix[row][row] = 0.0;
               adjacencyMatrix[column][column] = 0.0;
               adjacencyMatrix[row][column] = edge.getLength();
               adjacencyMatrix[column][row] = edge.getLength();
               predecessorMatrix[row][row] = -1;
               predecessorMatrix[column][column] = -1;
               predecessorMatrix[row][column] = row;
               predecessorMatrix[column][row] = column;
            }
         }
      }

      for(int k = 0; k < vertexList.size(); ++k) {
         for(int i = 0; i < vertexList.size(); ++i) {
            for(int j = 0; j < vertexList.size(); ++j) {
               if (adjacencyMatrix[i][k] + adjacencyMatrix[k][j] < adjacencyMatrix[i][j]) {
                  adjacencyMatrix[i][j] = adjacencyMatrix[i][k] + adjacencyMatrix[k][j];
                  predecessorMatrix[i][j] = predecessorMatrix[k][j];
               }
            }
         }
      }

      for(int i = 0; i < vertexList.size(); ++i) {
         for(int j = 0; j < vertexList.size(); ++j) {
            if (adjacencyMatrix[i][j] > maxPath && adjacencyMatrix[i][j] != Double.POSITIVE_INFINITY) {
               maxPath = adjacencyMatrix[i][j];
               a = i;
               b = j;
            }
         }
      }

      this.reconstructPath(predecessorMatrix, a, b, edgeList, vertexList);
      return maxPath;
   }

   private void reconstructPath(int[][] predecessorMatrix, int startIndex, int endIndex, ArrayList<Edge> edgeList, ArrayList<Vertex> vertexList) {
      this.shortestPathPoints = new ArrayList<>();
      int b = endIndex;

      for(int a = startIndex; b != a; b = predecessorMatrix[a][b]) {
         Vertex predecessor = vertexList.get(predecessorMatrix[a][b]);
         Vertex endvertex = vertexList.get(b);
         ArrayList<Edge> sp_edgeslist = new ArrayList<>();
         Double lengthtest = Double.POSITIVE_INFINITY;
         Edge shortestedge = null;

         for(Edge edge : edgeList) {
            if (edge.getV1() == predecessor && edge.getV2() == endvertex || edge.getV1() == endvertex && edge.getV2() == predecessor) {
               sp_edgeslist.add(edge);
            }
         }

         for(Edge edge : sp_edgeslist) {
            if (edge.getLength() < lengthtest) {
               shortestedge = edge;
               lengthtest = edge.getLength();
            }
         }

         for(Point p : shortestedge.getSlabs()) {
            this.shortestPathPoints.add(p);
            this.setPixel(this.shortPathImage, p.x, p.y, p.z, SHORTEST_PATH);
         }
      }

      if (this.shortestPathPoints.size() != 0) {
         this.spx = this.shortestPathPoints.get(0).x;
         this.spy = this.shortestPathPoints.get(0).y;
         this.spz = this.shortestPathPoints.get(0).z;
      }
   }
}
