package Batch;

import ij.process.ImagePlus;
import ij.process.ImageStack;
import ij.process.ImageProcessor;
import java.util.concurrent.ThreadPoolExecutor;

public class Lee94 {
    static final int BLOCK_SIZE = 64;
    static final int MAX_BREADTH = 8;

    static final byte[] simplePointsLut = loadSimplePointsLut();

    static final int[] eulerLut1 = new int[] {
        23, 24, 14, 15, 20, 21, 12,
        25, 22, 16, 13, 24, 21, 15,
        17, 20, 9, 12, 18, 21, 10,
        19, 22, 18, 21, 11, 13, 10,
        6, 14, 7, 15, 3, 12, 4,
        8, 7, 16, 15, 5, 4, 13,
        0, 9, 3, 12, 1, 10, 4,
        2, 1, 11, 10, 5, 4, 13
    };

    static final int[] eulerLut2 = new int[] {
        1, -1, -1, 1, -3, -1, -1, 1,
        -1, 1, 1, -1, 3, 1, 1, -1,
        -3, -1, 3, 1, 1, -1, 3, 1,
        -1, 1, 1, -1, 3, 1, 1, -1,
        -3, 3, -1, 1, 1, 3, -1, 1,
        -1, 1, 1, -1, 3, 1, 1, -1,
        1, 3, 3, 1, 5, 3, 3, 1,
        -1, 1, 1, -1, 3, 1, 1, -1,
        -7, -1, -1, 1, -3, -1, -1, 1,
        -1, 1, 1, -1, 3, 1, 1, -1,
        -3, -1, 3, 1, 1, -1, 3, 1,
        -1, 1, 1, -1, 3, 1, 1, -1,
        -3, 3, -1, 1, 1, 3, -1, 1,
        -1, 1, 1, -1, 3, 1, 1, -1,
        1, 3, 3, 1, 5, 3, 3, 1,
        -1, 1, 1, -1, 3, 1, 1, -1
    };

    static class Params implements ISliceCompute {
        public final IntVector finalSimplePoints;
        public final IntVector[] points3d;
        public final int[] offs;
        public final byte[] planes;
        public final int width;
        public final int height;
        public final int breadth;

        public Params(int nSlices, byte[] planes, int width, int height, int breadth) {
            this.planes = planes;
            this.width = width;
            this.height = height;
            this.breadth = breadth;
            this.offs = new int[3];

            int reservedCap = Math.max(width * height / 32, 384);
            this.finalSimplePoints = new IntVector(reservedCap);

            this.points3d = new IntVector[nSlices];
            for (int i = 0; i < nSlices; i++)
                points3d[i] = new IntVector(reservedCap / nSlices);
        }

        public void setBorder(int border) {
            offs[0] = 0;
            offs[1] = 0;
            offs[2] = 0;

            if (border == 1 || border == 2)
                offs[1] = ((border-1) * 2) - 1;
            else if (border == 3 || border == 4)
                offs[0] = 1 - ((border-3) * 2);
            else if (border == 5 || border == 6)
                offs[2] = 1 - ((border-5) * 2);
        }

        @Override
        public Object computeSlice(int sliceIdx, int start, int length) {
            IntVector vertices = points3d[sliceIdx];
            vertices.size = 0;

            for (int z = 0; z < breadth; z++) {
                boolean zIsBorder = (z == 0 && offs[2] == -1) || (z == breadth-1 && offs[2] == 1);

                for (int y = 0; y < height; y++) {
                    boolean yIsBorder = (y == 0 && offs[1] == -1) || (y == height-1 && offs[1] == 1);

                    for (int x = start; x < start+length; x++) {
                        int idx = x + width*y;
                        if (((planes[idx] >>> z) & 1) == 0)
                            continue;

                        boolean xIsBorder = (x == 0 && offs[0] == -1) || (x == width-1 && offs[0] == 1);

                        if (
                            xIsBorder || yIsBorder || zIsBorder ||
                            ((planes[(x + offs[0]) + width * (y + offs[1])] >>> (z + offs[2])) & 1) == 0
                        ) {
                            int neighborBits = getNeighborBits(planes, x, y, z);
                            if (Integer.bitCount(neighborBits) != 1 && isSimplePoint(neighborBits) && isEulerInvariant(neighborBits)) {
                                vertices.add(x);
                                vertices.add(y);
                                vertices.add(z);
                            }
                        }
                    }
                }
            }

            return null;
        }

        @Override
        public void finishSlice(ISliceCompute.Result result) {
            IntVector vec = points3d[result.idx];
            finalSimplePoints.add(vec.buf, 0, vec.size);
        }
    }

    public static ImageProcessor skeletonize(ThreadPoolExecutor threadPool, int maxWorkers, ImagePlus image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int breadth = Math.min(image.getSize(), MAX_BREADTH);
        int bitDepth = stack.getBitDepth();
        ImageStack stack = image.getStack();

        Object[] layers = new Object[breadth];
        for (int i = 0; i < breadth; i++)
            layers[i] = stack.getPixels(i + 1);

        byte[] planes = new byte[width * height];
        switch (bitDepth) {
            case 8:
                combinePlanes8(planes, (byte[][])layers);
                break;
            case 16:
                combinePlanes16(planes, (short[][])layers);
                break;
            case 24:
                combinePlanesRgb(planes, (int[][])layers);
                break;
            case 32:
                combinePlanes32(planes, (float[][])layers);
                break;
            default:
                throw new RuntimeException("Unexpected bit depth (" + bitDepth + ")");
        }

        PointVectorInt offsetLengthPairs = ParallelUtils.makeBinaryTreeOfSlices();
        Params params = new Params(offsetLengthPairs.size, planes, width, height, breadth);

        boolean anyChanged;
        do {
            anyChanged = false;
            for (int border = 1; border <= 6; border++) {
                boolean wasThinned = thin(threadPool, maxWorkers, offsetLengthPairs, params, border);
                anyChanged = anyChanged || wasThinned;
            }
        } while (anyChanged);
    }

    static boolean thin(
        ThreadPoolExecutor threadPool,
        int maxWorkers,
        PointVectorInt offsetLengthPairs,
        Params params,
        int border
    ) {
        params.setBorder(border);
        params.finalSimplePoints.size = 0;

        ParallelUtils.computeSlicesInParallel(
            threadPool,
            maxWorkers,
            offsetLengthPairs,
            params
        );

        boolean anyChange = false;
        for (int i = 0; i < params.finalSimplePoints.size; i += 3) {
            int x = params.finalSimplePoints.buf[i];
            int y = params.finalSimplePoints.buf[i+1];
            int z = params.finalSimplePoints.buf[i+2];
            int pos = x + y*params.width;
            boolean isSimple = isSimplePoint(getNeighborBits(params.planes, x, y, z));

            params.planes[pos] = (params.planes[pos] & ~(1 << z)) | (byte)(isSimple ? 0 : (1 << z));
            anyChange = anyChange || isSimple;
        }

        return anyChange;
    }

    // returns a 26-bit number containing each neighbor within a 3x3x3 vicinity (-1 to exclude the point itself)
    static int getNeighborBits(byte[] planes, int width, int height, int breadth, int x, int y, int z) {
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

    static boolean isSimplePoint(int neighborBits) {
        return ((simplePointsLut[neighborBits >> 3] >> (7 - (neighborBits & 7))) & 1) == 1;
    }

    static boolean isEulerInvariant(int neighborBits) {
        int euler = 0;
        int idx = 0;
        for (int i = 0; i < eulerLut1.length; i++) {
            idx = (idx << 1) | ((neighborBits >> eulerLut1[i]) & 1);
            if (i % 7 == 6) {
                euler += eulerLut2[idx];
                idx = 0;
            }
        }

        return euler == 0;
    }

    static void combinePlanes8(byte[] output, byte[][] slices) {
        for (int i = 0; i < output.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < slices.length; j++) {
                for (int k = 0; k < block; k++)
                    output[i+k] |= (slices[j][i+k] >>> 31 | -slices[j][i+k] >>> 31) << j;
            }
        }
    }

    static void combinePlanes16(byte[] output, short[][] slices) {
        for (int i = 0; i < output.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < slices.length; j++) {
                for (int k = 0; k < block; k++)
                    output[i+k] |= (slices[j][i+k] >>> 31 | -slices[j][i+k] >>> 31) << j;
            }
        }
    }

    static void combinePlanesRgb(byte[] output, int[][] slices) {
        for (int i = 0; i < output.length; i++) {
            int rgb = slices[0][i];
            output[i] =
                (byte)((rgb & 0xff0000) >>> 31 | -(rgb & 0xff0000) >>> 31) |
                (byte)((rgb &   0xff00) >>> 31 | -(rgb &   0xff00) >>> 31) << 1 |
                (byte)((rgb &     0xff) >>> 31 | -(rgb &     0xff) >>> 31) << 2;
        }
    }

    static void combinePlanes32(byte[] output, float[][] slices) {
        for (int i = 0; i < output.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < slices.length; j++) {
                for (int k = 0; k < block; k++) {
                    int exp = (Float.floatToRawIntBits(slices[j][i+k]) >> 23) & 0xff;
                    output[i+k] |= (-(exp - 0x76) >>> 31) << j;
                }
            }
        }
    }
}
