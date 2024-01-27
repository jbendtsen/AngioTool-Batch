package Batch;

public class AnalyzeSkeleton2
{
    public static final int NONE = 0;
    public static final int END_POINT = 1;
    public static final int JUNCTION = 2;
    public static final int SLAB = 3;

    public static final int MAX_BREADTH = 8;

    public static final int JUNC_VISIT = 16;
    public static final int SKEL_VISIT = 24;

    // returns a 26-bit number containing each neighbor within a 3x3x3 vicinity (-1 to exclude the point itself)
    public static int getBooleanNeighborBits(byte[] planes, int width, int height, int breadth, int x, int y, int z)
    {
        int bits = 0;
        for (int i = 0; i < 27; i++) {
            if (i == 13)
                continue;
            int xx = x + (i % 3) - 1;
            int yy = y + ((i / 3) % 3) - 1;
            int zz = z + (i / 9) - 1;
            int p = 0;
            if (xx >= 0 && xx < width && yy >= 0 && yy < height && zz >= 0 && zz < breadth)
                p = (planes[xx + width * yy] >> zz) & 1;
            bits = (bits << 1) | p;
        }

        return bits;
    }

    public static void resetSkeletonResult2(SkeletonResult2 result, int newBreadth)
    {
        if (result.pointVectors == null)
            result.pointVectors = new IntVector[4];

        for (int i = 0; i < result.breadth; i++) {
            result.markedImages[i] = BufferPool.intPool.release(result.markedImages[i]);
            result.junctionVertexMap[i] = BufferPool.intPool.release(result.junctionVertexMap[i]);
        }

        result.triplePointCounts = BufferPool.intPool.release(result.triplePointCounts);
        result.quadruplePointCounts = BufferPool.intPool.release(result.quadruplePointCounts);
        result.maximumBranchLength = BufferPool.doublePool.release(result.maximumBranchLength);
        result.numberOfSlabs = BufferPool.intPool.release(result.numberOfSlabs);

        if (newBreadth > result.breadth) {
            result.markedImages = new int[newBreadth][];
            result.junctionVertexMap = new int[newBreadth][];
        }

        result.breadth = newBreadth;
    }

    public static void analyze(
        SkeletonResult2 result,
        PixelCalibration calibration,
        byte[] skeletonImages,
        int width,
        int height,
        int breadth
    ) {
        breadth = Math.min(Math.max(1, breadth), MAX_BREADTH);
        resetSkeletonResult2(result, breadth);

        int[] imageInfo = BufferPool.intPool.acquireAsIs(width * height);
        tagImages(result, skeletonImages, width, height, breadth, imageInfo);

        for (int z = 0; z < breadth; z++)
            result.markedImages[z] = BufferPool.intPool.acquireZeroed(width * height);

        int nTrees = markTreesOnImages(result, skeletonImage, width, height, breadth, imageInfo);
        if (nTrees <= 0)
            nTrees = 1;

        /*
        if (nTrees > 1) {
            divideVoxelsByTrees();
        }
        else {
            this.endPointsTree[0] = this.listOfEndPoints;
            this.numberOfEndPoints[0] = this.listOfEndPoints.size();
            this.junctionVoxelTree[0] = this.listOfJunctionVoxels;
            this.numberOfJunctionVoxels[0] = this.listOfJunctionVoxels.size();
            this.startingSlabTree[0] = this.listOfStartingSlabVoxels;
        }
        */

        result.triplePointCounts = BufferPool.intPool.acquireZeroed(nTrees);
        result.quadruplePointCounts = BufferPool.intPool.acquireZeroed(nTrees);

        for (int z = 0; z < breadth; z++)
            result.junctionVertexMap[z] = BufferPool.intPool.acquireZeroed(width * height);

        groupJunctions(result, skeletonImage, width, height, breadth, nTrees, imageInfo);

        /*
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
        */

        result.maximumBranchLength = BufferPool.doublePool.acquireZeroed(nTrees);
        result.numberOfSlabs = BufferPool.intPool.acquireZeroed(nTrees);

        buildSkeletonGraphs(result, calibration, skeletonImage, width, height, breadth, nTrees, imageInfo);

        BufferPool.intPool.release(imageInfo);
    }

    static void tagImage(SkeletonResult2 result, byte[] skeletonImage, int width, int height, int breadth, int[] imageInfo)
    {
        result.pointVectors[NONE] = null;
        result.pointVectors[END_POINT] = result.listOfEndPoints;
        result.pointVectors[JUNCTION] = result.listOfJunctionVoxels;
        result.pointVectors[SLAB] = result.listOfSlabVoxels;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int idx = x + width * y;
                int value = 0;
                for (int z = 0; z < breadth; ++z) {
                    int type = NONE;
                    if (((skeletonImage[idx] >>> z) & 1) != 0) {
                        int numOfNeighbors = Integer.bitCount(
                            getBooleanNeighborBits(skeletonImage, width, height, breadth, x, y, z)
                        );

                        if (numOfNeighbors < 2)
                            type = END_POINT;
                        else if (numOfNeighbors > 2)
                            type = JUNCTION;
                        else
                            type = SLAB;

                        result.pointVectors[type].addThree(x, y, z);
                    }
                    value = (value << 2) | type;
                }
                imageInfo[idx] = value;
            }
        }
    }

    static int markTreesOnImages(SkeletonResult2 result, byte[] skeletonImage, int width, int height, int breadth, int[] imageInfo)
    {
        int nTrees = 0;
        for (int type = END_POINT; type <= SLAB; type++) {
            int n = pointVectors[type].size;
            for (int i = 0; i < n; i += 3) {
                int x = result.pointVectors[type].buf[i];
                int y = result.pointVectors[type].buf[i+1];
                int z = result.pointVectors[type].buf[i+2];
                if (markedImages[z][x + width * y] != 0)
                    continue;

                if (type == SLAB)
                    result.listOfStartingSlabVoxels.addThree(x, y, z);

                int numOfVoxels = 0;
                markedImages[z][x + width * y] = nTrees + 1;

                result.toRevisit.size = 0;
                if (isJunction(x, y, z))
                    result.toRevisit.addThree(x, y, z);

                int revisitIdx = 0;
                boolean wasRevisit = false;
                boolean didFindPoint;
                do {
                    didFindPoint = false;
                    for (int j = 0; j < 27; j++) {
                        if (j == 13)
                            continue;

                        int xx = x + (j / 9) - 1;
                        int yy = y + ((j / 3) % 3) - 1;
                        int zz = z + (j % 3) - 1;
                        if (xx < 0 || xx >= width || yy < 0 || yy >= height || zz < 0 || zz >= breadth)
                            continue;

                        if (
                            ((skeletonImage[xx + width * yy] >>> zz) & 1) != 0 &&
                            result.markedImages[zz][xx + width * yy] == 0
                        ) {
                            x = xx;
                            y = yy;
                            z = zz;
                            didFindPoint = true;
                            break;
                        }
                    }

                    if (didFindPoint) {
                        ++numOfVoxels;
                        result.markedImages[z][x + width * y] = nTrees + 1;

                        if (((taggedImage[x + width * y] >>> (z << 1)) & 3) == JUNCTION)
                            result.toRevisit.addThree(x, y, z);
                    }
                    else {
                        if (wasRevisit)
                            revistIdx += 3;

                        if (revisitIdx < result.toRevist.size) {
                            x = result.toRevisit.buf[revisitIdx];
                            y = result.toRevisit.buf[revisitIdx+1];
                            z = result.toRevisit.buf[revisitIdx+2];
                        }
                    }
                    wasRevisit = !didFindPoint;
                }
                while (didFindPoint || revistIdx < result.toRevist.size);

                if (type == END_POINT || numOfVoxels != 0)
                    nTrees++;
            }
        }

        return nTrees;
    }

    static void groupJunctions(
        SkeletonResult2 result,
        byte[] skeletonImage,
        int width,
        int height,
        int breadth,
        int nTrees,
        int[] imageInfo
    ) {
        int vertexCount = 0;

        for (int i = 0; i < result.listOfJunctionVoxels.size; i += 3) {
            int x = result.listOfJunctionVoxels.buf[i];
            int y = result.listOfJunctionVoxels.buf[i+1];
            int z = result.listOfJunctionVoxels.buf[i+2];
            if (((imageInfo[x + width * y] >>> (JUNC_VISIT + z)) & 1) != 0)
                continue;
            
            imageInfo[x + width * y] |= 1 << (JUNC_VISIT + z);
            int treeIdx = markedImages[z][x * width + y] - 1;

            vertexCount++;
            result.junctionVertexMap[z][x + width * y] = vertexCount;
            result.listOfSingleJunctions.addThree(x, y, z);

            result.toRevisit.size = 0;
            result.toRevisit.addThree(x, y, z);

            int revisitIdx = 0;
            boolean wasRevisit = false;
            int pointFound;
            do {
                pointFound = -1;
                int nBranches = 0;
                for (int j = 0; j < 27; j++) {
                    if (j == 13)
                        continue;

                    int xx = x + (j / 9) - 1;
                    int yy = y + ((j / 3) % 3) - 1;
                    int zz = z + (j % 3) - 1;
                    if (xx < 0 || xx >= width || yy < 0 || yy >= height || zz < 0 || zz >= breadth)
                        continue;

                    int type = (imageInfo[xx + width * yy] >>> (zz << 1)) & 3;
                    if (
                        pointFound == -1 &&
                        ((skeletonImage[xx + width * yy] >>> zz) & 1) != 0 &&
                        ((imageInfo[xx + width * yy] >>> (JUNC_VISIT + zz)) & 1) == 0 &&
                        type == JUNCTION
                    ) {
                        pointFound = j;
                    }

                    if (type == END_POINT || type == SLAB)
                        nBranches++;
                }

                if (pointFound >= 0) {
                    x += (pointFound / 9) - 1;
                    y += ((pointFound / 3) % 3) - 1;
                    z += (pointFound % 3) - 1;

                    imageInfo[x + width * y] |= 1 << (JUNC_VISIT + z);
                    result.junctionVertexMap[z][x + width * y] = vertexCount;
                    result.listOfSingleJunctions.addThree(x, y, z);
                    result.toRevisit.addThree(x, y, z);

                    if (nBranches == 3)
                        result.triplePointCounts.buf[treeIdx]++;
                    else if (nBranches == 4)
                        result.quadruplePointCounts.buf[treeIdx]++;
                }
                else {
                    if (wasRevisit)
                        revisitIdx += 3;

                    if (revisitIdx < result.toRevisit.size) {
                        x = result.toRevisit.buf[revisitIdx];
                        y = result.toRevisit.buf[revisitIdx+1];
                        z = result.toRevisit.buf[revisitIdx+2];
                    }
                }
                wasRevisit = pointFound == -1;
            }
            while (pointFound >= 0 || revisitIdx < result.toRevisit.size);
        }
    }

    static void buildSkeletonGraphs(
        SkeletonResult2 result,
        PixelCalibration calibration,
        byte[] skeletonImages,
        int width,
        int height,
        int breadth,
        int nTrees,
        int[] imageInfo
    ) {
        int endPointStart = 0;
        int junctionStart = 0;
        int startingSlabStart = 0;
        result.slabList.size = 0;

        for (int t = 0; t < nTrees; t++) {
            //this.graph[t] = new Graph();

            /*
            for(int i = 0; i < this.junctionVertex[iTree].length; ++i) {
                this.graph[t].addVertex(this.junctionVertex[iTree][i]);
            }
            */

            result.branchLengths[t] = 0.0;

            //for(int i = 0; i < this.numberOfEndPoints[iTree]; ++i)
            for (int i = endPointStart; i < result.listOfEndPoints.size; i += 3) {
                int x = result.listOfEndPoints.buf[i];
                int y = result.listOfEndPoints.buf[i+1];
                int z = result.listOfEndPoints.buf[i+2];
                if (markedImages[z][x + width * y] != t + 1) {
                    endPointStart = i;
                    break;
                }
                if (((imageInfo[x + width * y] >>> (SKEL_VISIT + z)) & 1) != 0)
                    continue;

                imageInfo[x + width * y] |= 1 << (SKEL_VISIT + z);

                /*
                Vertex v1 = new Vertex();
                v1.addPoint(endPointCoord);
                this.graph[iTree].addVertex(v1);
                */

                int slabListIdx = result.slabList.size;

                visitBranch(slabListIdx, 0.0);

                /*
                this.graph[iTree].addVertex(this.auxFinalVertex);
                this.graph[iTree].addEdge(new Edge(v1, this.auxFinalVertex, this.slabList, length));
                */
            }

            for (int i = junctionStart; i < result.listOfSingleJunctions.size; i++) {
                int x = result.listOfSingleJunctions.buf[i];
                int y = result.listOfSingleJunctions.buf[i+1];
                int z = result.listOfSingleJunctions.buf[i+2];
                if (markedImages[z][x + width * y] != t + 1) {
                    junctionStart = i;
                    break;
                }

                // no check
                imageInfo[x + width * y] |= 1 << (SKEL_VISIT + z);
                int vertexIdx = result.junctionVertexMap[z][x + width * y];

                boolean didFindPoint;
                do {
                    didFindPoint = false;
                    for (int j = 0; j < 27; j++) {
                        if (j == 13)
                            continue;

                        int xx = x + (j / 9) - 1;
                        int yy = y + ((j / 3) % 3) - 1;
                        int zz = z + (j % 3) - 1;
                        if (xx < 0 || xx >= width || yy < 0 || yy >= height || zz < 0 || zz >= breadth)
                            continue;

                        if (
                            ((skeletonImage[xx + width * yy] >>> zz) & 1) != 0 &&
                            ((imageInfo[xx + width * yy] >>> (SKEL_VISIT + zz)) & 1) == 0
                        ) {
                            didFindPoint = true;
                            if (((imageInfo[xx + width * yy] >>> (zz << 1)) & 3) == JUNCTION) {
                                imageInfo[xx + width * yy] |= 1 << (SKEL_VISIT + zz);
                                break;
                            }

                            double initialLength = calculateDistance(x, y, z, xx, yy, zz, calibration);
                            int slabListIdx = result.slabList.addThree(xx, yy, zz);
                            visitBranch(slabListIdx, initialLength);
                        }
                    }
                }
                while (didFindPoint);
            }

            // listOfStartingSlabVoxels
            if (startingSlabStart + 2 < listOfStartingSlabVoxels.size) {
                boolean isSingle = true;
                int s = startingSlabStart;

                startingSlabStart += 3;
                while (startingSlabStart + 2 < listOfStartingSlabVoxels.size) {
                    int xx = listOfStartingSlabVoxels.buf[startingSlabStart];
                    int yy = listOfStartingSlabVoxels.buf[startingSlabStart+1];
                    int zz = listOfStartingSlabVoxels.buf[startingSlabStart+2];
                    if (markedImages[zz][xx + width * yy] != t + 1)
                        break;

                    isSingle = false;
                    startingSlabStart += 3;
                }

                if (isSingle) {
                    int x = listOfStartingSlabVoxels.buf[s];
                    int y = listOfStartingSlabVoxels.buf[s+1];
                    int z = listOfStartingSlabVoxels.buf[s+2];
                    
                    /*
                    Vertex v1 = new Vertex();
                    v1.addPoint(startCoord);
                    this.graph[iTree].addVertex(v1);
                    */

                    result.numberOfSlabs[iTree]++;
                    int slabListIdx = result.slabList.addThree(x, y, z);
                    visitBranch(slabListIdx, 0.0);

                    //this.graph[iTree].addEdge(new Edge(v1, v1, this.slabList, length));
                }
            }

            /*
            if (this.numberOfBranches[iTree] != 0) {
                this.averageBranchLength[iTree] = branchLength / (double)this.numberOfBranches[iTree];
            }
            */
        }
    }

    public static double visitBranch(int mode, int initialSlabListIdx, double initialLength)
    {
        double length = initialLength;
        int type = NONE;
        boolean didFindSlabPoint;
        do {
            didFindSlabPoint = false;
            for (int j = 0; j < 27; j++) {
                if (j == 13)
                    continue;

                int xx = x + (j / 9) - 1;
                int yy = y + ((j / 3) % 3) - 1;
                int zz = z + (j % 3) - 1;
                if (xx < 0 || xx >= width || yy < 0 || yy >= height || zz < 0 || zz >= breadth)
                    continue;

                if (
                    ((skeletonImage[xx + width * yy] >>> zz) & 1) != 0 &&
                    ((imageInfo[xx + width * yy] >>> (SKEL_VISIT + zz)) & 1) == 0
                ) {
                    length += calculateDistance();
                    imageInfo[xx + width * yy] |= 1 << (SKEL_VISIT + zz);
                    type = (imageInfo[xx + width * yy] >>> (zz << 1)) & 3;
                    x = xx;
                    y = yy;
                    z = zz;

                    switch (type) {
                        case END_POINT:
                            this.auxFinalVertex = new Vertex();
                            this.auxFinalVertex.addPoint(nextPoint);
                            break;
                        case JUNCTION:
                            this.auxFinalVertex = this.findPointVertex(this.junctionVertex[iTree], nextPoint);
                            break;
                        case SLAB:
                            result.numberOfSlabs[t]++;
                            result.slabList.addThree(x, y, z);
                            didFindSlabPoint = true;
                            break;
                    }

                    break;
                }
            }
        }
        while (didFindSlabPoint);

        double lengthBeforeSlabs = length;
        if (mode != SLAB && type == SLAB) {
            Point aux = this.auxPoint;

            //this.auxPoint = this.getVisitedJunctionNeighbor(this.auxPoint, initialVertex);
            for (int j = 0; j < 27; j++) {
                if (j == 13)
                    continue;

                int xx = x + (j / 9) - 1;
                int yy = y + ((j / 3) % 3) - 1;
                int zz = z + (j % 3) - 1;
                if (xx < 0 || xx >= width || yy < 0 || yy >= height || zz < 0 || zz >= breadth)
                    continue;

                int info = imageInfo[xx + width * yy];
                if (
                    ((skeletonImage[xx + width * yy] >>> zz) & 1) != 0 &&
                    ((info >>> (SKEL_VISIT + zz)) & 1) != 0 &&
                    ((info >>> (zz << 1)) & 3) == JUNCTION &&
                    result.junctionVertexMap[zz][xx + width * yy] != idxExclude
                ) {
                    finalNeighbor = neighbor;
                    break;
                }
            }

            this.auxFinalVertex = this.findPointVertex(this.junctionVertex[iTree], this.auxPoint);
            if (this.auxPoint == null) {
                this.auxFinalVertex = initialVertex;
                this.auxPoint = aux;
            }

            length += this.calculateDistance(this.auxPoint, aux);
        }

        result.branchLength[iTree] += mode == JUNCTION ? lengthBeforeSlabs : length;
        if (length > result.maximumBranchLength[iTree])
            result.maximumBranchLength[iTree] = length;

        result.numberOfBranches[iTree]++;

        int nSlabs = result.slabList.size - initialSlabListIdx;
        result.edgesTrees.add(t);
        result.edgesVerts.addTwo(initialVertIdx, finalVertIdx);
        result.edgesPoints.addTwo(initialSlabListIdx, nSlabs);
        result.edgesLengths.add(length);

        return length;
    }
}
