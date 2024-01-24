package Batch;

public class AnalyzeSkeleton2
{
    public static final int NONE = 0;
    public static final int END_POINT = 1;
    public static final int JUNCTION = 2;
    public static final int SLAB = 3;

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

    public static void analyze(SkeletonResult2 result, byte[] skeletonImage, int width, int height, int depth)
    {
        result.reset();
        int[] taggedImage = BufferPool.intPool.acquire(width * height);

        IntVector[] pointVectors = new IntVector[4];
        pointVectors[NONE] = null;
        pointVectors[END_POINT] = result.listOfEndPoints;
        pointVectors[JUNCTION] = result.listOfJunctionVoxels;
        pointVectors[SLAB] = result.listOfSlabVoxels;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int idx = x + width * y;
                int value = 0;
                for (int z = 0; z < depth; ++z) {
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
            markedImages[z] = BufferPool.intPool.acquire(width * height);

        int[] visitMap = BufferPool.intPool.acquire(width * height);
        int[] point = new int[3];

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

                boolean didFindPoint = getNextUnvisitedVoxel(point);
                int revisitIdx = 0;

                while (didFindPoint || revistIdx < toRevist.size) {
                    if (didFindPoint) {
                        //if (!this.isVisited(nextPoint)) { // infinite loop if this if doesn't trigger
                        ++numOfVoxels;

                        x = point[0];
                        y = point[1];
                        z = point[2];
                        markedImages[z][x + width * y] = nTrees + 1;
                        if (isJunction(x, y, z)) {
                            result.toRevisit.add(x);
                            result.toRevisit.add(y);
                            result.toRevisit.add(z);
                        }

                        didFindPoint = getNextUnvisitedVoxel(point);
                        //}
                    }
                    else {
                        point[0] = result.toRevisit.buf[revisitIdx];
                        point[1] = result.toRevisit.buf[revisitIdx+1];
                        point[2] = result.toRevisit.buf[revisitIdx+2];
                        didFindPoint = getNextUnvisitedVoxel(point);
                        if (!didFindPoint)
                            revistIdx += 3;
                    }
                }

                return numOfVoxels;

                if (type == END_POINT || length != 0)
                    nTrees++;
            }
        }

        nTrees = Math.max(nTrees, 1);

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

        this.resetVisited();

        for(int iTree = 0; iTree < this.numOfTrees; ++iTree) {
            for(int i = 0; i < this.numberOfJunctionVoxels[iTree]; ++i) {
                Point startingPoint = this.junctionVoxelTree[iTree].get(i);
                if (!this.isVisited(startingPoint)) {
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

                    listOfSingleJunctions[iTree].add(newGroup);
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

        for (int t = 0; t < nTrees; t++) {
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

            for(int i = 0; i < this.numberOfJunctions[t]; ++i) {
                ArrayList<Point> groupOfJunctions = this.listOfSingleJunctions[t].get(i);
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
                    this.numberOfTriplePoints[t]++;
                } else if (nBranch == 4) {
                    this.numberOfQuadruplePoints[t]++;
                }
            }
        }

        return assembleResults();
    }
}
