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

    public static void analyze(
        SkeletonResult2 result,
        PixelCalibration calibration,
        byte[] skeletonImages,
        int width,
        int height,
        int breadth
    ) {
        breadth = Math.min(Math.max(1, breadth), MAX_BREADTH);
        result.reset(breadth);

        int[] imageInfo = BufferPool.intPool.acquireAsIs(width * height);
        int[] junctionMap2d = BufferPool.intPool.acquireZeroed(width * height);

        for (int z = 0; z < breadth; z++)
            result.endPointVertexMap[z] = BufferPool.intPool.acquireZeroed(width * height);

        tagImages(result, skeletonImages, width, height, breadth, imageInfo, junctionMap2d);

        for (int z = 0; z < breadth; z++)
            result.markedImages[z] = BufferPool.intPool.acquireZeroed(width * height);

        int nTrees = markTreesOnImages(result, skeletonImages, width, height, breadth, imageInfo);
        if (nTrees <= 0)
            nTrees = 1;

        result.treeCount = nTrees;
        result.triplePointCounts = BufferPool.intPool.acquireZeroed(nTrees);
        result.quadruplePointCounts = BufferPool.intPool.acquireZeroed(nTrees);

        for (int z = 0; z < breadth; z++)
            result.junctionVertexMap[z] = BufferPool.intPool.acquireZeroed(width * height);

        groupJunctions(result, skeletonImages, width, height, breadth, imageInfo, nTrees);

        result.maximumBranchLengths = BufferPool.doublePool.acquireZeroed(nTrees);
        result.numberOfSlabs = BufferPool.intPool.acquireZeroed(nTrees);

        buildSkeletonGraphs(result, calibration, skeletonImages, width, height, breadth, imageInfo, nTrees);

        isolateDominantJunctions(result, width, height, junctionMap2d);

        BufferPool.intPool.release(imageInfo);
        BufferPool.intPool.release(junctionMap2d);
    }

    static void tagImages(
        SkeletonResult2 result,
        byte[] skeletonImages,
        int width,
        int height,
        int breadth,
        int[] imageInfo,
        int[] junctionMap2d
    ) {
        result.pointVectors[END_POINT-1] = result.endPoints;
        result.pointVectors[JUNCTION-1] = result.junctionVoxels;
        result.pointVectors[SLAB-1] = result.slabVoxels;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int idx = x + width * y;
                int value = 0;
                for (int z = 0; z < breadth; ++z) {
                    int type = NONE;
                    if (((skeletonImages[idx] >>> z) & 1) != 0) {
                        int numOfNeighbors = Integer.bitCount(
                            getBooleanNeighborBits(skeletonImages, width, height, breadth, x, y, z)
                        );

                        if (numOfNeighbors < 2)
                            type = END_POINT;
                        else if (numOfNeighbors > 2)
                            type = JUNCTION;
                        else
                            type = SLAB;

                        int pos = result.pointVectors[type-1].addThree(x, y, z);
                        if (type == END_POINT)
                            result.endPointVertexMap[z][idx] = pos;
                        else if (type == JUNCTION)
                            junctionMap2d[idx] = pos + 1;
                    }
                    value = (value << 2) | type;
                }
                imageInfo[idx] = value;
            }
        }
    }

    static int markTreesOnImages(
        SkeletonResult2 result,
        byte[] skeletonImages,
        int width,
        int height,
        int breadth,
        int[] imageInfo
    ) {
        int nTrees = 0;
        for (int type = END_POINT; type <= SLAB; type++) {
            int n = result.pointVectors[type-1].size;
            for (int i = 0; i < n; i += 3) {
                int x = result.pointVectors[type-1].buf[i];
                int y = result.pointVectors[type-1].buf[i+1];
                int z = result.pointVectors[type-1].buf[i+2];
                if (result.markedImages[z][x + width * y] != 0)
                    continue;

                if (type == SLAB)
                    result.startingSlabVoxels.addThree(x, y, z);

                int numOfVoxels = 0;
                result.markedImages[z][x + width * y] = nTrees + 1;

                result.toRevisit.size = 0;
                if (((imageInfo[x + width * y] >>> (z << 1)) & 3) == JUNCTION)
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
                            ((skeletonImages[xx + width * yy] >>> zz) & 1) != 0 &&
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

                        if (((imageInfo[x + width * y] >>> (z << 1)) & 3) == JUNCTION)
                            result.toRevisit.addThree(x, y, z);
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

                if (type == END_POINT || numOfVoxels != 0)
                    nTrees++;
            }
        }

        return nTrees;
    }

    static void groupJunctions(
        SkeletonResult2 result,
        byte[] skeletonImages,
        int width,
        int height,
        int breadth,
        int[] imageInfo,
        int nTrees
    ) {
        int vertexCount = 0;

        for (int i = 0; i < result.junctionVoxels.size; i += 3) {
            int x = result.junctionVoxels.buf[i];
            int y = result.junctionVoxels.buf[i+1];
            int z = result.junctionVoxels.buf[i+2];
            if (((imageInfo[x + width * y] >>> (JUNC_VISIT + z)) & 1) != 0)
                continue;
            
            imageInfo[x + width * y] |= 1 << (JUNC_VISIT + z);
            int treeIdx = result.markedImages[z][x * width + y] - 1;

            vertexCount++;
            result.junctionVertexMap[z][x + width * y] = vertexCount;
            result.singleJunctions.addThree(x, y, z);

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
                        ((skeletonImages[xx + width * yy] >>> zz) & 1) != 0 &&
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
                    result.singleJunctions.addThree(x, y, z);
                    result.toRevisit.addThree(x, y, z);

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
        int[] imageInfo,
        int nTrees
    ) {
        int endPointStart = 0;
        int junctionStart = 0;
        int startingSlabStart = 0;

        for (int t = 0; t < nTrees; t++) {
            result.totalBranchLengths[t] = 0.0;
            result.maximumBranchLengths[t] = 0.0;

            for (int i = endPointStart; i < result.endPoints.size; i += 3) {
                int x = result.endPoints.buf[i];
                int y = result.endPoints.buf[i+1];
                int z = result.endPoints.buf[i+2];
                if (result.markedImages[z][x + width * y] != t + 1) {
                    endPointStart = i;
                    break;
                }
                if (((imageInfo[x + width * y] >>> (SKEL_VISIT + z)) & 1) != 0)
                    continue;

                imageInfo[x + width * y] |= 1 << (SKEL_VISIT + z);
                int slabListIdx = result.slabList.size;

                visitBranch(
                    result, calibration, skeletonImages, width, height, breadth, imageInfo, t,
                    END_POINT, i, slabListIdx, 0.0, x, y, z
                );
            }

            for (int i = junctionStart; i < result.singleJunctions.size; i += 3) {
                int x = result.singleJunctions.buf[i];
                int y = result.singleJunctions.buf[i+1];
                int z = result.singleJunctions.buf[i+2];
                if (result.markedImages[z][x + width * y] != t + 1) {
                    junctionStart = i;
                    break;
                }

                // no check
                imageInfo[x + width * y] |= 1 << (SKEL_VISIT + z);
                int vertexIdx = result.junctionVertexMap[z][x + width * y] - 1;

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
                            ((skeletonImages[xx + width * yy] >>> zz) & 1) != 0 &&
                            ((imageInfo[xx + width * yy] >>> (SKEL_VISIT + zz)) & 1) == 0
                        ) {
                            didFindPoint = true;
                            if (((imageInfo[xx + width * yy] >>> (zz << 1)) & 3) == JUNCTION) {
                                imageInfo[xx + width * yy] |= 1 << (SKEL_VISIT + zz);
                                break;
                            }

                            double initialLength = calculateDistance(x, y, z, xx, yy, zz, calibration);
                            int slabListIdx = result.slabList.addThree(xx, yy, zz);

                            visitBranch(
                                result, calibration, skeletonImages, width, height, breadth, imageInfo, t,
                                JUNCTION, vertexIdx, slabListIdx, initialLength, xx, yy, zz
                            );
                        }
                    }
                }
                while (didFindPoint);
            }

            if (startingSlabStart + 2 < result.startingSlabVoxels.size) {
                boolean isSingle = true;
                int s = startingSlabStart;

                startingSlabStart += 3;
                while (startingSlabStart + 2 < result.startingSlabVoxels.size) {
                    int xx = result.startingSlabVoxels.buf[startingSlabStart];
                    int yy = result.startingSlabVoxels.buf[startingSlabStart+1];
                    int zz = result.startingSlabVoxels.buf[startingSlabStart+2];
                    if (result.markedImages[zz][xx + width * yy] != t + 1)
                        break;

                    isSingle = false;
                    startingSlabStart += 3;
                }

                if (isSingle) {
                    int x = result.startingSlabVoxels.buf[s];
                    int y = result.startingSlabVoxels.buf[s+1];
                    int z = result.startingSlabVoxels.buf[s+2];

                    result.numberOfSlabs[t]++;
                    int slabListIdx = result.slabList.addThree(x, y, z);

                    visitBranch(
                        result, calibration, skeletonImages, width, height, breadth, imageInfo, t,
                        SLAB, s, slabListIdx, 0.0, x, y, z
                    );
                }
            }
        }
    }

    static void visitBranch(
        SkeletonResult2 result,
        PixelCalibration calibration,
        byte[] skeletonImages,
        int width,
        int height,
        int breadth,
        int[] imageInfo,
        int iTree,
        int mode,
        int initialVertIdx,
        int initialSlabListIdx,
        double initialLength,
        int xStart,
        int yStart,
        int zStart
    ) {
        double length = initialLength;
        int type = NONE;
        int finalVertIdx = initialVertIdx;
        int x = xStart;
        int y = yStart;
        int z = zStart;

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
                    ((skeletonImages[xx + width * yy] >>> zz) & 1) != 0 &&
                    ((imageInfo[xx + width * yy] >>> (SKEL_VISIT + zz)) & 1) == 0
                ) {
                    length += calculateDistance(x, y, z, xx, yy, zz, calibration);
                    imageInfo[xx + width * yy] |= 1 << (SKEL_VISIT + zz);
                    type = (imageInfo[xx + width * yy] >>> (zz << 1)) & 3;
                    x = xx;
                    y = yy;
                    z = zz;

                    switch (type) {
                        case END_POINT:
                            finalVertIdx = result.endPointVertexMap[z][x + width * y] - 1;
                            break;
                        case JUNCTION:
                            finalVertIdx = result.junctionVertexMap[z][x + width * y] - 1;
                            //this.auxFinalVertex = this.findPointVertex(this.junctionVertex[iTree], nextPoint);
                            break;
                        case SLAB:
                            result.numberOfSlabs[iTree]++;
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
        int modeEnd = mode;

        if (mode == SLAB) {
            finalVertIdx = initialVertIdx;
        }
        else if (type == SLAB) {
            finalVertIdx = initialVertIdx;

            int xExclude = -1;
            int yExclude = -1;
            int zExclude = -1;
            int vertExclude = -1;

            if (mode == END_POINT) {
                xExclude = xStart;
                yExclude = yStart;
                zExclude = zStart;
            }
            else {
                vertExclude = initialVertIdx;
            }

            for (int j = 0; j < 27; j++) {
                if (j == 13)
                    continue;

                int xx = x + (j / 9) - 1;
                int yy = y + ((j / 3) % 3) - 1;
                int zz = z + (j % 3) - 1;
                if (xx < 0 || xx >= width || yy < 0 || yy >= height || zz < 0 || zz >= breadth)
                    continue;

                int info = imageInfo[xx + width * yy];
                int vert = result.junctionVertexMap[zz][xx + width * yy];
                if (
                    ((skeletonImages[xx + width * yy] >>> zz) & 1) != 0 &&
                    ((info >>> (SKEL_VISIT + zz)) & 1) != 0 &&
                    ((info >>> (zz << 1)) & 3) == JUNCTION &&
                    vert != vertExclude &&
                    !(xx == xExclude && yy == yExclude && zz == zExclude)
                ) {
                    finalVertIdx = vert;
                    modeEnd = JUNCTION;
                    length += calculateDistance(x, y, z, xx, yy, zz, calibration);
                    break;
                }
            }
        }

        result.totalBranchLengths[iTree] += mode == JUNCTION ? lengthBeforeSlabs : length;
        if (length > result.maximumBranchLengths[iTree])
            result.maximumBranchLengths[iTree] = length;

        result.numberOfBranches[iTree]++;

        int modeSides = (mode << 2) | modeEnd;
        int nSlabs = result.slabList.size - initialSlabListIdx;
        result.edgesTrees.add(iTree);
        result.edgesVerts.addThree(modeSides, initialVertIdx, finalVertIdx);
        result.edgesPoints.addTwo(initialSlabListIdx, nSlabs);
        result.edgesLengths.add(length);
    }

    static void isolateDominantJunctions(SkeletonResult2 result, int width, int height, int[] junctionMap2d)
    {
        for (int i = 0; i < result.junctionVoxels.size; i += 3) {
            int x = result.junctionVoxels.buf[i];
            int y = result.junctionVoxels.buf[i+1];
            int pos = junctionMap2d[x + width * y];
            if (pos <= 0)
                continue;

            result.isolatedJunctions.add(pos - 1);
            junctionMap2d[x + width * y] = 0;

            for (int j = 0; j < 9; j++) {
                if (j == 4)
                    continue;

                int xx = x + (j % 3) - 1;
                int yy = y + (j / 3) - 1;
                int p = junctionMap2d[xx + width * yy];
                if (p > 0) {
                    result.removedJunctions.add(p - 1);
                    junctionMap2d[xx + width * yy] = 0;
                }
            }
        }
    }

    static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, PixelCalibration calibration)
    {
        double dx = (double)(x2 - x1) * calibration.widthUnits;
        double dy = (double)(y2 - y1) * calibration.heightUnits;
        double dz = (double)(z2 - z1) * calibration.breadthUnits;
        return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    }
}
