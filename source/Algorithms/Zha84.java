package Algorithms;

import java.util.Arrays;

public class Zha84
{
    static final byte[] lut = stringToLut(
        "00010013003110130000000020203033" +
        "00000000300000000000000020003022" +
        "00000000000000000000000000000000" +
        "20000000200020003000000030003020" +
        "00310013000000010000000000000001" +
        "31000000000000002000000000000000" +
        "23130013000000010000000000000000" +
        "23010001000000003301000022002000"
    );

    static byte[] stringToLut(String str)
    {
        byte[] out = new byte[256];
        for (int i = 0; i < out.length; i++)
            out[i] = (byte)(str.charAt(i) - '0');
        return out;
    }

    public static void skeletonize(
        byte[] outSkeletonImage,
        byte[] scratchSkeletonImage,
        byte[] iterationsImage,
        byte[] inputImage,
        int width,
        int height,
        int maxSkelIterations,
        int maxVesselThickness
    ) {
        if (iterationsImage != null)
            Arrays.fill(iterationsImage, 0, width * height, (byte)0);

        Arrays.fill(outSkeletonImage, 0, width, (byte)0);
        Arrays.fill(scratchSkeletonImage, 0, width, (byte)0);

        for (int i = 1; i < height - 1; i++) {
            outSkeletonImage[i*width] = 0;
            scratchSkeletonImage[i*width] = 0;
            for (int j = 1; j < width - 1; j++) {
                int idx = j + width*i;
                outSkeletonImage[idx] = (byte)(inputImage[idx] >>> 31);
            }
            outSkeletonImage[(i+1)*width-1] = 0;
            scratchSkeletonImage[(i+1)*width-1] = 0;
        }

        int lastRow = (height-1) * width;
        Arrays.fill(outSkeletonImage, lastRow, lastRow + width, (byte)0);
        Arrays.fill(scratchSkeletonImage, lastRow, lastRow + width, (byte)0);

        byte[] a = null, b = null;

        int nRemovals;
        int step = 0;
        do {
            nRemovals = 0;
            a = outSkeletonImage;
            b = scratchSkeletonImage;

            for (int pass = 1; pass <= 2; pass++) {
                if (iterationsImage != null)
                    nRemovals += trimAndCount(iterationsImage, a, b, width, height, pass);
                else
                    nRemovals += trimSimple(a, b, width, height, pass);

                byte[] temp = a;
                a = b;
                b = temp;
            }
            //System.out.println("nRemovals: " + nRemovals);
        } while (nRemovals != 0 && (maxSkelIterations <= 0 || ++step < maxSkelIterations));

        if (iterationsImage != null) {
            //maxVesselThickness = 4;
            System.out.println("trimming excess: " + maxVesselThickness);
            int area = width * height;
            for (int i = 0; i < area; i++) {
                //totalIters += iterationsImage[i] & 0xff;
                int mask = ((iterationsImage[i] & 0xff) - maxVesselThickness) >> 31;
                //nTrims += outSkeletonImage[i] & ~mask;
                outSkeletonImage[i] &= mask;
            }
        }
    }

    final static int trimSimple(byte[] src, byte[] dst, int width, int height, int pass)
    {
        int nRemovals = 0;

        for (int i = 1; i < height-1; i++) {
            for (int j = 1; j < width-1; j++) {
                int idx = j + width*i;
                int shouldKeep = 1;

                if (src[idx] != 0) {
                    int value = lut[
                        src[idx-width-1] |
                        src[idx-width] << 1 |
                        src[idx-width+1] << 2 |
                        src[idx+1] << 3 |
                        src[idx+width+1] << 4 |
                        src[idx+width] << 5 |
                        src[idx+width-1] << 6 |
                        src[idx-1] << 7
                    ];

                    //boolean shouldKeep = value != 3 && value != pass;
                    shouldKeep =
                        ((value-3)    >>> 31 | -(value-3)    >>> 31) &
                        ((value-pass) >>> 31 | -(value-pass) >>> 31);
                }

                dst[idx] = (byte)(shouldKeep * src[idx]);
                nRemovals += shouldKeep ^ 1;
            }
        }

        return nRemovals;
    }

    final static int trimAndCount(byte[] iterationImage, byte[] src, byte[] dst, int width, int height, int pass)
    {
        int nRemovals = 0;

        for (int i = 1; i < height-1; i++) {
            for (int j = 1; j < width-1; j++) {
                int idx = j + width*i;
                int shouldKeep = 1;

                if (src[idx] != 0) {
                    int neighbors =
                        src[idx-width-1] |
                        src[idx-width] << 1 |
                        src[idx-width+1] << 2 |
                        src[idx+1] << 3 |
                        src[idx+width+1] << 4 |
                        src[idx+width] << 5 |
                        src[idx+width-1] << 6 |
                        src[idx-1] << 7;

                    int value = lut[neighbors];

                    //boolean shouldKeep = value != 3 && value != pass;
                    shouldKeep =
                        ((value-3)    >>> 31 | -(value-3)    >>> 31) &
                        ((value-pass) >>> 31 | -(value-pass) >>> 31);

                    iterationImage[idx] = (byte)Math.min((iterationImage[idx] & 0xff) + ((neighbors + 1) >> 8), 0xff);
                }

                dst[idx] = (byte)(shouldKeep * src[idx]);
                nRemovals += shouldKeep ^ 1;
            }
        }

        return nRemovals;
    }
}
