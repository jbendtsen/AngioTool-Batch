package Batch;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import java.io.InputStream;
import java.util.concurrent.ThreadPoolExecutor;

public class Lee94 {
    static final int IN_PLACE_THRESHOLD = 250;
    static final int BLOCK_SIZE = 64; // for optimal cache utilization
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

    public static void skeletonize(ThreadPoolExecutor threadPool, int maxWorkers, ImagePlus image) {
        if (image.getStackSize() == 1) {
            skeletonize(threadPool, maxWorkers, image.getProcessor());
        }
        else {
            ImageStack stack = image.getStack();
            skeletonize(threadPool, maxWorkers, stack);
            stack.update(image.getProcessor());
        }
    }

    public static void skeletonize(ThreadPoolExecutor threadPool, int maxWorkers, ImageStack stack) {
        int width = stack.getWidth();
        int height = stack.getHeight();
        int breadth = Math.min(stack.getSize(), MAX_BREADTH);
        int bitDepth = stack.getBitDepth();

        Object[] layers = new Object[breadth];
        for (int i = 0; i < breadth; i++)
            layers[i] = stack.getPixels(i + 1);

        skeletonize(threadPool, maxWorkers, layers, width, height, bitDepth);
    }

    public static void skeletonize(ThreadPoolExecutor threadPool, int maxWorkers, ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int bitDepth = ip.getBitDepth();

        Object[] layers = new Object[1];
        layers[0] = ip.getPixels();

        skeletonize(threadPool, maxWorkers, layers, width, height, bitDepth);
    }

    public static void skeletonize(ThreadPoolExecutor threadPool, int maxWorkers, Object[] layersObj, int width, int height, int bitDepth) {
        final int breadth = layersObj.length;

        byte[][]  layersByte  = null;
        short[][] layersShort = null;
        int[][]   layersInt   = null;
        float[][] layersFloat = null;

        // since we have to reduce each pixel to a single bit anyway,
        // we might as well combine each channel/layer into a single buffer to improve memory locality
        byte[] planes = new byte[width * height];
        switch (bitDepth) {
            case 8:
                layersByte = getPlanes8(planes, layersObj);
                break;
            case 16:
                layersShort = getPlanes16(planes, layersObj);
                break;
            case 24:
                layersInt = getPlanesRgb(planes, layersObj);
                break;
            case 32:
                layersFloat = getPlanes32(planes, layersObj);
                break;
            default:
                throw new RuntimeException("Unexpected bit depth (" + bitDepth + ")");
        }

        //writePgm(layersByte[0], width, height, "before.pgm");

        IntVector offsetLengthPairs = ParallelUtils.makeBinaryTreeOfSlices(width, IN_PLACE_THRESHOLD - 1);
        Params params = new Params(offsetLengthPairs.size / 2, planes, width, height, breadth);

        boolean anyChanged;
        do {
            anyChanged = false;
            for (int border = 1; border <= 6; border++) {
                boolean wasThinned = thin(threadPool, maxWorkers, offsetLengthPairs, params, border);
                anyChanged = anyChanged || wasThinned;
            }
        } while (anyChanged);

        switch (bitDepth) {
            case 8:
                setPlanes8(layersByte, planes);
                break;
            case 16:
                setPlanes16(layersShort, planes);
                break;
            case 24:
                setPlanesRgb(layersInt, planes);
                break;
            case 32:
                setPlanes32(layersFloat, planes, new float[2]);
                break;
            default:
                throw new RuntimeException("Unexpected bit depth (" + bitDepth + ")");
        }

        //writePgm(layersByte[0], width, height, "after.pgm");
    }

    static void writePgm(byte[] pixels, int width, int height, String title) {
        byte[] header = ("P5\n" + width + " " + height + "\n255\n").getBytes();
        ByteVector out = new ByteVector(header.length + pixels.length);
        out.add(header);
        out.add(pixels);
        try {
            java.nio.file.Files.write(
                java.nio.file.FileSystems.getDefault().getPath("", title),
                out.buf,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.WRITE
            );
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static boolean thin(
        ThreadPoolExecutor threadPool,
        int maxWorkers,
        IntVector offsetLengthPairs,
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
            boolean isSimple = isSimplePoint(AnalyzeSkeleton2.getBooleanNeighborBits(params.planes, params.width, params.height, params.breadth, x, y, z));

            params.planes[pos] = (byte)((params.planes[pos] & ~(1 << z)) | (isSimple ? 0 : (1 << z)));
            anyChange = anyChange || isSimple;
        }

        return anyChange;
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

    static byte[][] getPlanes8(byte[] output, Object[] slicesObj) {
        byte[][] slices = new byte[slicesObj.length][];
        for (int i = 0; i < slicesObj.length; i++)
            slices[i] = (byte[])slicesObj[i];

        for (int i = 0; i < output.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < slices.length; j++) {
                for (int k = 0; k < block; k++)
                    output[i+k] |= (slices[j][i+k] >>> 31 | -slices[j][i+k] >>> 31) << j;
            }
        }

        return slices;
    }

    static short[][] getPlanes16(byte[] output, Object[] slicesObj) {
        short[][] slices = new short[slicesObj.length][];
        for (int i = 0; i < slicesObj.length; i++)
            slices[i] = (short[])slicesObj[i];

        for (int i = 0; i < output.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < slices.length; j++) {
                for (int k = 0; k < block; k++)
                    output[i+k] |= (slices[j][i+k] >>> 31 | -slices[j][i+k] >>> 31) << j;
            }
        }

        return slices;
    }

    static int[][] getPlanesRgb(byte[] output, Object[] slicesObj) {
        int[][] slices = new int[slicesObj.length][];
        for (int i = 0; i < slicesObj.length; i++)
            slices[i] = (int[])slicesObj[i];

        for (int i = 0; i < output.length; i++) {
            int rgb = slices[0][i];
            output[i] = (byte)(
                ((rgb & 0xff0000) >>> 31 | -(rgb & 0xff0000) >>> 31) |
                ((rgb &   0xff00) >>> 31 | -(rgb &   0xff00) >>> 31) << 1 |
                ((rgb &     0xff) >>> 31 | -(rgb &     0xff) >>> 31) << 2
            );
        }

        return slices;
    }

    static float[][] getPlanes32(byte[] output, Object[] slicesObj) {
        float[][] slices = new float[slicesObj.length][];
        for (int i = 0; i < slicesObj.length; i++)
            slices[i] = (float[])slicesObj[i];

        for (int i = 0; i < output.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, output.length - i);
            for (int j = 0; j < slices.length; j++) {
                for (int k = 0; k < block; k++) {
                    int exp = (Float.floatToRawIntBits(slices[j][i+k]) >> 23) & 0xff;
                    output[i+k] |= (-(exp - 0x76) >>> 31) << j;
                }
            }
        }

        return slices;
    }

    static void setPlanes8(byte[][] output, byte[] planes) {
        for (int i = 0; i < planes.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, planes.length - i);
            for (int j = 0; j < output.length; j++) {
                for (int k = 0; k < block; k++)
                    output[j][i+k] = (byte)-((planes[i+k] >>> j) & 1);
            }
        }
    }

    static void setPlanes16(short[][] output, byte[] planes) {
        for (int i = 0; i < planes.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, planes.length - i);
            for (int j = 0; j < output.length; j++) {
                for (int k = 0; k < block; k++)
                    output[j][i+k] = (short)-((planes[i+k] >>> j) & 1);
            }
        }
    }

    static void setPlanesRgb(int[][] output, byte[] planes) {
        for (int i = 0; i < planes.length; i++) {;
            int r = -(planes[i] & 1) & 0xff;
            int g = -((planes[i] >>> 1) & 1) & 0xff;
            int b = -((planes[i] >>> 2) & 1) & 0xff;
            output[0][i] = (r << 16) | (g << 8) | b;
        }
    }

    // using the lookup table "float[] {0.0f, 255.0f}" is 5x faster than using a ternary (ie. bit == 1 ? 255.0f : 0.0f)
    // see FloatManipBenchmark.java
    static void setPlanes32(float[][] output, byte[] planes, float[] lut) {
        lut[0] = 0.0f;
        lut[1] = 255.0f;
        for (int i = 0; i < planes.length; i += BLOCK_SIZE) {
            int block = Math.min(BLOCK_SIZE, planes.length - i);
            for (int j = 0; j < output.length; j++) {
                for (int k = 0; k < block; k++)
                    output[j][i+k] = lut[(planes[i+k] >>> j) & 1];
            }
        }
    }
}
