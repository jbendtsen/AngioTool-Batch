package Batch;

import java.util.Arrays;

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
        byte[] skeletonImages,
        int width,
        int height,
        int breadth,
        double pixelWidth,
        double pixelHeight,
        double pixelBreadth
    ) {
        breadth = Math.min(Math.max(1, breadth), MAX_BREADTH);
        result.reset(breadth);

        Arrays.fill(result.junctionMap2d, 0, width * height, 0);
        Arrays.fill(result.endPointVertexMap, 0, width * height * breadth, 0);

        tagImages(result, skeletonImages, width, height, breadth);

        Arrays.fill(result.markedImages, 0, width * height * breadth, 0);

        int nTrees = markTreesOnImages(result, skeletonImages, width, height, breadth);
        if (nTrees <= 0)
            nTrees = 1;

        result.treeCount = nTrees;
        result.triplePointCounts = IntBufferPool.acquireZeroed(nTrees);
        result.quadruplePointCounts = IntBufferPool.acquireZeroed(nTrees);

        Arrays.fill(result.junctionVertexMap, 0, width * height * breadth, 0);

        groupJunctions(result, skeletonImages, width, height, breadth, nTrees);

        result.totalBranchLengths = DoubleBufferPool.acquireZeroed(nTrees);
        result.maximumBranchLengths = DoubleBufferPool.acquireZeroed(nTrees);
        result.numberOfBranches = IntBufferPool.acquireZeroed(nTrees);
        result.numberOfSlabs = IntBufferPool.acquireZeroed(nTrees);

        buildSkeletonGraphs(result, skeletonImages, width, height, breadth, pixelWidth, pixelHeight, pixelBreadth, nTrees);

        isolateDominantJunctions(result, width, height);
    }

    static void tagImages(
        SkeletonResult2 result,
        byte[] skeletonImages,
        int width,
        int height,
        int breadth
    ) {
        result.pointVectors[END_POINT-1] = result.endPoints;
        result.pointVectors[JUNCTION-1] = result.junctionVoxels;
        result.pointVectors[SLAB-1] = result.slabVoxels;

        int area = width * height;
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

                        if (numOfNeighbors < 2) {
                            type = END_POINT;
                            int pos = result.pointVectors[type-1].addThree(x, y, z);
                            result.endPointVertexMap[idx + area * z] = pos;
                        }
                        else if (numOfNeighbors > 2) {
                            type = JUNCTION;
                            int pos = result.pointVectors[type-1].addThree(x, y, z);
                            result.junctionMap2d[idx] = pos + 1;
                        }
                        else {
                            type = SLAB;
                            result.pointVectors[type-1].addThree(x, y, z);
                        }
                    }
                    value = (value << 2) | type;
                }
                result.imageInfo[idx] = value;
            }
        }
    }

    static int markTreesOnImages(
        SkeletonResult2 result,
        byte[] skeletonImages,
        int width,
        int height,
        int breadth
    ) {
        final int area = width * height;
        int nTrees = 0;
        for (int type = END_POINT; type <= SLAB; type++) {
            int n = result.pointVectors[type-1].size;
            for (int i = 0; i < n; i += 3) {
                int x = result.pointVectors[type-1].buf[i];
                int y = result.pointVectors[type-1].buf[i+1];
                int z = result.pointVectors[type-1].buf[i+2];
                if (result.markedImages[x + width * y + area * z] != 0)
                    continue;

                if (type == SLAB)
                    result.startingSlabVoxels.addThree(x, y, z);

                int numOfVoxels = 0;
                result.markedImages[x + width * y + area * z] = nTrees + 1;

                result.toRevisit.size = 0;
                if (((result.imageInfo[x + width * y] >>> (z << 1)) & 3) == JUNCTION)
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
                            result.markedImages[xx + width * yy + area * zz] == 0
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
                        result.markedImages[x + width * y + area * z] = nTrees + 1;

                        if (((result.imageInfo[x + width * y] >>> (z << 1)) & 3) == JUNCTION)
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
        int nTrees
    ) {
        final int area = width * height;
        int vertexCount = 0;

        for (int i = 0; i < result.junctionVoxels.size; i += 3) {
            int x = result.junctionVoxels.buf[i];
            int y = result.junctionVoxels.buf[i+1];
            int z = result.junctionVoxels.buf[i+2];
            if (((result.imageInfo[x + width * y] >>> (JUNC_VISIT + z)) & 1) != 0)
                continue;
            
            result.imageInfo[x + width * y] |= 1 << (JUNC_VISIT + z);
            int treeIdx = result.markedImages[x * width + y + area * z] - 1;

            vertexCount++;
            result.junctionVertexMap[x + width * y + area * z] = vertexCount;
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

                    int type = (result.imageInfo[xx + width * yy] >>> (zz << 1)) & 3;
                    if (
                        pointFound == -1 &&
                        ((skeletonImages[xx + width * yy] >>> zz) & 1) != 0 &&
                        ((result.imageInfo[xx + width * yy] >>> (JUNC_VISIT + zz)) & 1) == 0 &&
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

                    result.imageInfo[x + width * y] |= 1 << (JUNC_VISIT + z);
                    result.junctionVertexMap[x + width * y + area * z] = vertexCount;
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
        byte[] skeletonImages,
        int width,
        int height,
        int breadth,
        double pixelWidth,
        double pixelHeight,
        double pixelBreadth,
        int nTrees
    ) {
        int area = width * height;

        for (int i = 0; i < result.endPoints.size; i += 3) {
            int x = result.endPoints.buf[i];
            int y = result.endPoints.buf[i+1];
            int z = result.endPoints.buf[i+2];
            int t = result.markedImages[x + width * y + area * z] - 1;

            if (((result.imageInfo[x + width * y] >>> (SKEL_VISIT + z)) & 1) != 0)
                continue;

            result.imageInfo[x + width * y] |= 1 << (SKEL_VISIT + z);
            int slabListIdx = result.slabList.size;

            visitBranch(
                result, skeletonImages, width, height, breadth, pixelWidth, pixelHeight, pixelBreadth, t,
                END_POINT, i, slabListIdx, 0.0, x, y, z
            );
        }

        for (int i = 0; i < result.singleJunctions.size; i += 3) {
            int x = result.singleJunctions.buf[i];
            int y = result.singleJunctions.buf[i+1];
            int z = result.singleJunctions.buf[i+2];
            int t = result.markedImages[x + width * y + area * z] - 1;

            // no check
            result.imageInfo[x + width * y] |= 1 << (SKEL_VISIT + z);
            int vertexIdx = result.junctionVertexMap[x + width * y + area * z] - 1;

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
                    ((result.imageInfo[xx + width * yy] >>> (SKEL_VISIT + zz)) & 1) == 0
                ) {
                    //didFindPoint = true;
                    if (((result.imageInfo[xx + width * yy] >>> (zz << 1)) & 3) == JUNCTION) {
                        result.imageInfo[xx + width * yy] |= 1 << (SKEL_VISIT + zz);
                        continue;
                    }

                    double initialLength = calculateDistance(x, y, z, xx, yy, zz, pixelWidth, pixelHeight, pixelBreadth);
                    int slabListIdx = result.slabList.addThree(xx, yy, zz);

                    visitBranch(
                        result, skeletonImages, width, height, breadth, pixelWidth, pixelHeight, pixelBreadth, t,
                        JUNCTION, vertexIdx, slabListIdx, initialLength, xx, yy, zz
                    );
                }
            }
        }

        int startingSlabStart = 0;
        while (startingSlabStart + 2 < result.startingSlabVoxels.size) {
            int s = startingSlabStart;
            int x = result.startingSlabVoxels.buf[s];
            int y = result.startingSlabVoxels.buf[s+1];
            int z = result.startingSlabVoxels.buf[s+2];
            int t = result.markedImages[x + width * y + area * z] - 1;

            boolean isSingle = true;
            startingSlabStart += 3;

            while (startingSlabStart + 2 < result.startingSlabVoxels.size) {
                int xx = result.startingSlabVoxels.buf[startingSlabStart];
                int yy = result.startingSlabVoxels.buf[startingSlabStart+1];
                int zz = result.startingSlabVoxels.buf[startingSlabStart+2];
                if (result.markedImages[xx + width * yy + area * zz] != t + 1)
                    break;

                isSingle = false;
                startingSlabStart += 3;
            }

            if (isSingle) {
                //System.out.println("slab visit on tree " + (t+1) + " / " + nTrees);
                result.numberOfSlabs[t]++;
                int slabListIdx = result.slabList.addThree(x, y, z);

                visitBranch(
                    result, skeletonImages, width, height, breadth, pixelWidth, pixelHeight, pixelBreadth, t,
                    SLAB, s, slabListIdx, 0.0, x, y, z
                );
            }
        }
    }

    static void visitBranch(
        SkeletonResult2 result,
        byte[] skeletonImages,
        int width,
        int height,
        int breadth,
        double pixelWidth,
        double pixelHeight,
        double pixelBreadth,
        int iTree,
        int mode,
        int initialVertIdx,
        int initialSlabListIdx,
        double initialLength,
        int xStart,
        int yStart,
        int zStart
    ) {
        final int area = width * height;
        double length = initialLength;
        int type = NONE;
        int finalVertIdx = initialVertIdx;
        int x = xStart;
        int y = yStart;
        int z = zStart;

        result.imageInfo[x + width * y] |= 1 << (SKEL_VISIT + z);

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
                    ((result.imageInfo[xx + width * yy] >>> (SKEL_VISIT + zz)) & 1) == 0
                ) {
                    length += calculateDistance(x, y, z, xx, yy, zz, pixelWidth, pixelHeight, pixelBreadth);
                    result.imageInfo[xx + width * yy] |= 1 << (SKEL_VISIT + zz);
                    type = (result.imageInfo[xx + width * yy] >>> (zz << 1)) & 3;
                    x = xx;
                    y = yy;
                    z = zz;

                    switch (type) {
                        case END_POINT:
                            finalVertIdx = result.endPointVertexMap[x + width * y + area * z] - 1;
                            break;
                        case JUNCTION:
                            finalVertIdx = result.junctionVertexMap[x + width * y + area * z] - 1;
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

                int info = result.imageInfo[xx + width * yy];
                int vert = result.junctionVertexMap[xx + width * yy + area * zz];
                if (
                    ((skeletonImages[xx + width * yy] >>> zz) & 1) != 0 &&
                    ((info >>> (SKEL_VISIT + zz)) & 1) != 0 &&
                    ((info >>> (zz << 1)) & 3) == JUNCTION &&
                    vert != vertExclude &&
                    !(xx == xExclude && yy == yExclude && zz == zExclude)
                ) {
                    finalVertIdx = vert;
                    modeEnd = JUNCTION;
                    length += calculateDistance(x, y, z, xx, yy, zz, pixelWidth, pixelHeight, pixelBreadth);
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

    static void isolateDominantJunctions(SkeletonResult2 result, int width, int height)
    {
        for (int i = 0; i < result.junctionVoxels.size; i += 3) {
            int x = result.junctionVoxels.buf[i];
            int y = result.junctionVoxels.buf[i+1];
            int pos = result.junctionMap2d[x + width * y];
            if (pos <= 0)
                continue;

            result.isolatedJunctions.add(pos - 1);
            result.junctionMap2d[x + width * y] = 0;

            for (int j = 0; j < 9; j++) {
                if (j == 4)
                    continue;

                int xx = x + (j % 3) - 1;
                int yy = y + (j / 3) - 1;
                int p = result.junctionMap2d[xx + width * yy];
                if (p > 0) {
                    result.removedJunctions.add(p - 1);
                    result.junctionMap2d[xx + width * yy] = 0;
                }
            }
        }
    }

    static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, double pixelWidth, double pixelHeight, double pixelBreadth)
    {
        double dx = (double)(x2 - x1) * pixelWidth;
        double dy = (double)(y2 - y1) * pixelHeight;
        double dz = (double)(z2 - z1) * pixelBreadth;
        return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    }
}
