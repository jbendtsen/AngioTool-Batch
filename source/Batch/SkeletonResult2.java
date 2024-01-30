package Batch;

public class SkeletonResult2 {
    public int imageBreadth;

    public IntVector[] pointVectors;

    public IntVector endPoints;
    public IntVector junctionVoxels;
    public IntVector singleJunctions;
    public IntVector slabVoxels;
    public IntVector startingSlabVoxels;
    public IntVector toRevisit;

    public IntVector isolatedJunctions;
    public IntVector removedJunctions;
    public IntVector slabList;
    public IntVector edgesTrees;
    public IntVector edgesVerts;
    public IntVector edgesPoints;
    public DoubleVector edgesLengths;

    public int[][] markedImages;
    public int[][] endPointVertexMap;
    public int[][] junctionVertexMap;

    public int treeCount;
    public int[] triplePointCounts;
    public int[] quadruplePointCounts;
    public double[] totalBranchLengths;
    public double[] maximumBranchLengths;
    public int[] numberOfBranches;
    public int[] numberOfSlabs;

    /*
    public int numOfTrees;
    public int[] numberOfBranches = null;
    public int[] numberOfEndPoints = null;
    public int[] numberOfJunctionVoxels = null;
    public int[] numberOfSlabs = null;
    public int[] numberOfJunctions = null;
    public int[] numberOfTriplePoints = null;
    public int[] numberOfQuadruplePoints = null;
    public double[] averageBranchLength = null;
    public double[] maximumBranchLength = null;
    public int[] numberOfVoxels = null;
    public IntVector listOfEndPointsXyz = null;
    public IntVector listOfJunctionVoxelsXyz = null;
    public IntVector listOfSlabVoxelsXyz = null;
    public IntVector listOfStartingSlabVoxelsXyz = null;
    public Graph[] graph = null;
    */

    private static IntVector resetIntVector(IntVector vec) {
        if (vec == null)
            return new IntVector();
        vec.size = 0;
        return vec;
    }
    private static DoubleVector resetDoubleVector(DoubleVector vec) {
        if (vec == null)
            return new DoubleVector();
        vec.size = 0;
        return vec;
    }

    public void reset(int newBreadth)
    {
        if (pointVectors == null)
            pointVectors = new IntVector[3];

        endPoints = resetIntVector(endPoints);
        junctionVoxels = resetIntVector(junctionVoxels);
        singleJunctions = resetIntVector(singleJunctions);
        slabVoxels = resetIntVector(slabVoxels);
        startingSlabVoxels = resetIntVector(startingSlabVoxels);
        toRevisit = resetIntVector(toRevisit);
        isolatedJunctions = resetIntVector(isolatedJunctions);
        removedJunctions = resetIntVector(removedJunctions);
        slabList = resetIntVector(slabList);
        edgesTrees = resetIntVector(edgesTrees);
        edgesVerts = resetIntVector(edgesVerts);
        edgesPoints = resetIntVector(edgesPoints);
        edgesLengths = resetDoubleVector(edgesLengths);

        treeCount = 0;
        triplePointCounts = BufferPool.intPool.release(triplePointCounts);
        quadruplePointCounts = BufferPool.intPool.release(quadruplePointCounts);
        totalBranchLengths = BufferPool.doublePool.release(totalBranchLengths);
        maximumBranchLengths = BufferPool.doublePool.release(maximumBranchLengths);
        numberOfBranches = BufferPool.intPool.release(numberOfBranches);
        numberOfSlabs = BufferPool.intPool.release(numberOfSlabs);

        for (int i = 0; i < imageBreadth; i++) {
            markedImages[i] = BufferPool.intPool.release(markedImages[i]);
            endPointVertexMap[i] = BufferPool.intPool.release(endPointVertexMap[i]);
            junctionVertexMap[i] = BufferPool.intPool.release(junctionVertexMap[i]);
        }

        if (newBreadth > imageBreadth) {
            markedImages = new int[newBreadth][];
            endPointVertexMap = new int[newBreadth][];
            junctionVertexMap = new int[newBreadth][];
        }

        imageBreadth = newBreadth;
    }
}
