package Algorithms;

import Pixels.Planes;
import Utils.*;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class Lee94 {
    public static final int MAX_BREADTH = 8;

    static final int IN_PLACE_THRESHOLD = 250;

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

    public static class Params implements ISliceCompute {
        public final IntVector finalSimplePoints;
        public final RefVector<IntVector> points3d;
        public final int[] offs;

        public byte[] planes;
        public int width;
        public int height;
        public int breadth;

        public Params() {
            this.finalSimplePoints = new IntVector();
            this.points3d = new RefVector<IntVector>(IntVector.class);
            this.offs = new int[3];
        }

        public void setup(byte[] planes, int width, int height, int breadth) {
            this.planes = planes;
            this.width = width;
            this.height = height;
            this.breadth = breadth;

            int reservedCap = Math.max(width * height / 32, 384);
            if (finalSimplePoints.buf == null)
                finalSimplePoints.buf = new int[reservedCap];
        }

        public void prepare(int border) {
            offs[0] = 0;
            offs[1] = 0;
            offs[2] = 0;

            if (border == 1 || border == 2)
                offs[1] = ((border-1) * 2) - 1;
            else if (border == 3 || border == 4)
                offs[0] = 1 - ((border-3) * 2);
            else if (border == 5 || border == 6)
                offs[2] = 1 - ((border-5) * 2);

            finalSimplePoints.size = 0;
        }

        @Override
        public void initSlices(int nSlices) {
            points3d.resize(nSlices);

            int reservedCap = Math.max(width * height / 32, 384) / nSlices;
            for (int i = 0; i < nSlices; i++) {
                if (points3d.buf[i] == null)
                    points3d.buf[i] = new IntVector(reservedCap);
                points3d.buf[i].size = 0;
            }
        }

        @Override
        public Object computeSlice(int sliceIdx, int start, int length) {
            IntVector vertices = points3d.buf[sliceIdx];
            vertices.size = 0;

            for (int y = 0; y < height; y++) {
                boolean yIsBorder = (y == 0 && offs[1] == -1) || (y == height-1 && offs[1] == 1);

                for (int x = start; x < start+length; x++) {
                    boolean xIsBorder = (x == 0 && offs[0] == -1) || (x == width-1 && offs[0] == 1);

                    for (int z = 0; z < breadth; z++) {
                        if (((planes[x + width*y] >>> z) & 1) == 0)
                            continue;

                        boolean zIsBorder = (z == 0 && offs[2] == -1) || (z == breadth-1 && offs[2] == 1);

                        if (
                            xIsBorder || yIsBorder || zIsBorder ||
                            ((planes[(x + offs[0]) + width * (y + offs[1])] >>> (z + offs[2])) & 1) == 0
                        ) {
                            int neighborBits = AnalyzeSkeleton2.getBooleanNeighborBits(planes, width, height, breadth, x, y, z);
                            if (Integer.bitCount(neighborBits) != 1 && isSimplePoint(neighborBits) == 1 && isEulerInvariant(neighborBits)) {
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
            IntVector vec = points3d.buf[result.idx];
            finalSimplePoints.add(vec.buf, 0, vec.size);
        }
    }

    public static void skeletonize(
        byte[] planes,
        ISliceRunner runner,
        int maxWorkers,
        Object[] layersObj,
        int width,
        int height,
        int bitDepth,
        int maxSkelIterations
    ) throws ExecutionException
    {
        final int breadth = layersObj.length;

        byte[][]  layersByte  = null;
        short[][] layersShort = null;
        int[][]   layersInt   = null;
        float[][] layersFloat = null;

        switch (bitDepth) {
            case 8:
                layersByte = Planes.combine8(planes, width, height, layersObj);
                break;
            case 16:
                layersShort = Planes.combine16(planes, width, height, layersObj);
                break;
            case 24:
                layersInt = Planes.combineRgb(planes, width, height, layersObj);
                break;
            case 32:
                layersFloat = Planes.combine32(planes, width, height, layersObj);
                break;
            default:
                throw new RuntimeException("Unexpected bit depth (" + bitDepth + ")");
        }

        Params params = new Params();
        params.setup(planes, width, height, breadth);

        boolean anyChanged;
        int step = 0;
        do {
            anyChanged = false;
            for (int border = 1; border <= 6; border++) {
                boolean wasThinned = thin(params, runner, maxWorkers, border);
                anyChanged = anyChanged || wasThinned;
            }
        } while (anyChanged && (maxSkelIterations <= 0 || ++step < maxSkelIterations));

        params = null;
    }

    static boolean thin(
        Params params,
        ISliceRunner runner,
        int maxWorkers,
        int border
    ) throws ExecutionException
    {
        params.prepare(border);

        runner.runSlices(
            params,
            maxWorkers,
            params.width,
            IN_PLACE_THRESHOLD - 1
        );

        final IntVector results = params.finalSimplePoints;
        final int width = params.width;
        final int height = params.height;
        final int breadth = params.breadth;
        final byte[] planes = params.planes;

        boolean anyChange = false;
        for (int i = 0; i < results.size; i += 3) {
            int x = results.buf[i];
            int y = results.buf[i+1];
            int z = results.buf[i+2];
            int pos = x + y*width;
            int simple = isSimplePoint(AnalyzeSkeleton2.getBooleanNeighborBits(planes, width, height, breadth, x, y, z));

            planes[pos] = (byte)((planes[pos] & ~(1 << z)) | ((simple ^ 1) << z));
            anyChange = anyChange || (simple == 1);
        }

        return anyChange;
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

    static int isSimplePoint(int neighborBits) {
        return (simplePointsLut[neighborBits >> 3] >> (7 - (neighborBits & 7))) & 1;
    }

    static byte[] loadSimplePointsLut() {
        byte[] buffer = new byte[1 << 23];
        try (InputStream in = Lee94.class.getResourceAsStream("/lee94-simple-points.bin")) {
            if (in == null)
                throw new Exception("Error: could not open lee94-simple-points.bin");
            int off = 0;
            while (off < buffer.length) {
                int res = in.read(buffer, off, buffer.length - off);
                if (res <= 0)
                    break;
                off += res;
            }
            if (off < buffer.length)
                throw new Exception("Error: only read " + off + " / " + buffer.length + " bytes from lee94-simple-points.bin");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return buffer;
    }
}
