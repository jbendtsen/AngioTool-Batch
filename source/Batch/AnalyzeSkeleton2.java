package Batch;

public class AnalyzeSkeleton2
{
    public static final int NONE = 0;
    public static final int END_POINT = 1;
    public static final int JUNCTION = 2;
    public static final int SLAB = 3;

    public static final int MAX_BREADTH = 8;

    // returns a 26-bit number containing each neighbor within a 3x3x3 vicinity (-1 to exclude the point itself)
    public static int getBooleanNeighborBits(byte[] planes, int width, int height, int breadth, int x, int y, int z) {
        int bits = 0;
        for (int i = 0; i < 27; i++) {
            if (i == 13)
                continue;
            int xx = x + (i % 3) - 1;
            int yy = y + ((i / 3) % 3) - 1;
            int zz = z + ((i / 9) % 3) - 1;
            int p = 0;
            if (xx >= 0 && xx < width && yy >= 0 && yy < height && zz >= 0 && zz < breadth)
                p = (planes[xx + width * yy] >> zz) & 1;
            bits = (bits << 1) | p;
        }

        return bits;
    }

    public static void analyze(SkeletonResult2 result, byte[] skeletonImage, int width, int height, int breadth)
    {
        breadth = Math.min(Math.max(1, breadth), MAX_BREADTH);

        result.reset();
        int[] taggedImage = BufferPool.intPool.acquireAsIs(width * height);

        IntVector[] pointVectors = new IntVector[4];
        pointVectors[NONE] = null;
        pointVectors[END_POINT] = result.listOfEndPoints;
        pointVectors[JUNCTION] = result.listOfJunctionVoxels;
        pointVectors[SLAB] = result.listOfSlabVoxels;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int idx = x + width * y;
                int value = 0;
                for (int z = 0; z < breadth; ++z) {
                    int type = NONE;
                    if (((skeletonImage[idx] >>> z) & 1) != 0) {
                        int numOfNeighbors = Integer.bitCount(getBooleanNeighborBits(skeletonImage, width, height, breadth, x, y, z));

                        if (numOfNeighbors < 2)
                            type = END_POINT;
                        else if (numOfNeighbors > 2)
                            type = JUNCTION;
                        else
                            type = SLAB;

                        pointVectors[type].add(x);
                        pointVectors[type].add(y);
                        pointVectors[type].add(z);
                    }
                    value = (value << 2) | type;
                }
                taggedImage[idx] = value;
            }
        }

        int nTrees = 0;
        int[][] markedImages = new int[depth][];
        for(int z = 0; z < depth; ++z)
            markedImages[z] = BufferPool.intPool.acquireZeroed(width * height);

        for (int type = END_POINT; type <= SLAB; type++) {
            int n = pointVectors[type].size;
            for (int i = 0; i < n; i += 3) {
                int x = pointVectors[type].buf[i];
                int y = pointVectors[type].buf[i+1];
                int z = pointVectors[type].buf[i+2];
                if (markedImages[z][x + width * y] != 0)
                    continue;

                if (type == SLAB) {
                    result.listOfStartingSlabVoxels.add(x);
                    result.listOfStartingSlabVoxels.add(y);
                    result.listOfStartingSlabVoxels.add(z);
                }

                int numOfVoxels = 0;
                markedImages[z][x + width * y] = nTrees + 1;

                result.toRevisit.size = 0;
                if (isJunction(x, y, z)) {
                    result.toRevisit.add(x);
                    result.toRevisit.add(y);
                    result.toRevisit.add(z);
                }

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
                        if (
                            ((skeletonImage[xx + width * yy] >>> zz) & 1) != 0 &&
                            markedImages[zz][xx + width * yy] == 0
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
                        markedImages[z][x + width * y] = nTrees + 1;

                        if (((taggedImage[x + width * y] >>> (z << 1)) & 3) == JUNCTION) {
                            result.toRevisit.add(x);
                            result.toRevisit.add(y);
                            result.toRevisit.add(z);
                        }
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

        nTrees = Math.max(nTrees, 1);

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

        //this.resetVisited();

        int[] visitMap = BufferPool.intPool.acquireZeroed(width * height);
        result.triplePointCounts = BufferPool.intPool.acquireZeroed(nTrees);
        result.quadruplePointCounts = BufferPool.intPool.acquireZeroed(nTrees);

        for (int i = 0; i < listOfJunctionVoxels.size; i += 3) {
            int x = listOfJunctionVoxels.buf[i];
            int y = listOfJunctionVoxels.buf[i+1];
            int z = listOfJunctionVoxels.buf[i+2];
            if (((visitMap[x + width * y] >>> z) & 1) != 0)
                continue;

            visitMap[x + width * y] |= 1 << z;
            int treeIdx = markedImages[z][x * width + y] - 1;

            result.toRevisit.size = 0;
            result.toRevisit.add(x);
            result.toRevisit.add(y);
            result.toRevisit.add(z);

            int revisitIdx = 0;
            boolean wasRevisit = false;
            boolean didFindPoint;
            do {
                didFindPoint = false;
                int nBranches = 0;
                for (int j = 0; j < 27; j++) {
                    if (j == 13)
                        continue;

                    int xx = x + (j / 9) - 1;
                    int yy = y + ((j / 3) % 3) - 1;
                    int zz = z + (j % 3) - 1;
                    int type = (taggedImage[xx + width * yy] >>> (zz << 1)) & 3;
                    if (
                        !didFindPoint &&
                        ((skeletonImage[xx + width * yy] >>> zz) & 1) != 0 &&
                        ((visitMap[xx + width * yy] >>> zz) & 1) == 0 &&
                        type == JUNCTION
                    ) {
                        x = xx;
                        y = yy;
                        z = zz;
                        didFindPoint = true;
                    }

                    if (type == END_POINT || type == SLAB)
                        nBranches++;
                }

                if (didFindPoint) {
                    visitMap[x + width * y] |= 1 << z;
                    listOfSingleJunctions.add(x);
                    listOfSingleJunctions.add(y);
                    listOfSingleJunctions.add(z);
                    result.toRevisit.add(x);
                    result.toRevisit.add(y);
                    result.toRevisit.add(z);
                    if (nBranches == 3)
                        result.triplePointCounts[treeIdx]++;
                    else if (nBranches == 4)
                        result.quadruplePointCounts[treeIdx]++;
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
                wasRevisit = !didFindPoint;
            }
            while (didFindPoint || revisitIdx < result.toRevisit.size);

            // ...
        }

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

        //this.resetVisited();
        int endPointStart = 0;

        for (int t = 0; t < nTrees; t++) {
            //this.graph[t] = new Graph();

            /*
            for(int i = 0; i < this.junctionVertex[iTree].length; ++i) {
                this.graph[t].addVertex(this.junctionVertex[iTree][i]);
            }
            */

            double branchLength = 0.0;
            this.maximumBranchLength[t] = 0.0;
            this.numberOfSlabs[t] = 0;

            for (int i = endPointStart; i < result.listOfEndPoints.size; i += 3) {
                int x = result.listOfEndPoints.buf[i];
                int y = result.listOfEndPoints.buf[i+1];
                int z = result.listOfEndPoints.buf[i+2];
                if (markedImages[z][x + width * y] != t + 1) {
                    endPointStart = i;
                    break;
                }
                if (((visitMap[x + width * y] >>> (MAX_BREADTH + z)) & 1) != 0)
                    continue;

                
            }

            for(int i = 0; i < this.numberOfEndPoints[iTree]; ++i) {
                Point endPointCoord = this.endPointsTree[iTree].get(i);
                if (this.isVisited(endPointCoord))
                    continue;

                Vertex v1 = new Vertex();
                v1.addPoint(endPointCoord);
                this.graph[iTree].addVertex(v1);
                if (i == 0) {
                    this.graph[iTree].setRoot(v1);
                }

                this.slabList = new ArrayList<>();
                double length = this.visitBranch(endPointCoord, iTree);
                if (length == 0.0)
                    continue;

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

            for(int i = 0; i < this.junctionVertex[iTree].length; ++i) {
                for(int j = 0; j < this.junctionVertex[iTree][i].getPoints().size(); ++j) {
                    Point junctionCoord = this.junctionVertex[iTree][i].getPoints().get(j);
                    this.setVisited(junctionCoord, true);

                    for(Point nextPoint = this.getNextUnvisitedVoxel(junctionCoord); nextPoint != null; nextPoint = this.getNextUnvisitedVoxel(junctionCoord)) {
                        if (this.isJunction(nextPoint)) {
                            this.setVisited(nextPoint, true);
                            continue;
                        }
                        this.slabList = new ArrayList<>();
                        this.slabList.add(nextPoint);
                        double length = this.calculateDistance(junctionCoord, nextPoint);
                        this.auxPoint = null;
                        length += this.visitBranch(nextPoint, iTree);
                        if (length == 0.0)
                            continue;

                        branchLength += length;
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

        return assembleResults();
    }
}
