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
        byte[] inputImage,
        int width,
        int height,
        int maxSkelIterations
    ) {
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
                for (int i = 1; i < height-1; i++) {
                    for (int j = 1; j < width-1; j++) {
                        int idx = j + width*i;
                        int shouldKeep = 1;

                        if (a[idx] != 0) {
                            int value = lut[
                                a[idx-width-1] |
                                a[idx-width] << 1 |
                                a[idx-width+1] << 2 |
                                a[idx+1] << 3 |
                                a[idx+width+1] << 4 |
                                a[idx+width] << 5 |
                                a[idx+width-1] << 6 |
                                a[idx-1] << 7
                            ];

                            //boolean shouldKeep = value != 3 && value != pass;
                            shouldKeep =
                                ((value-3)    >>> 31 | -(value-3)    >>> 31) &
                                ((value-pass) >>> 31 | -(value-pass) >>> 31);
                        }

                        b[idx] = (byte)(shouldKeep * a[idx]);
                        nRemovals += shouldKeep ^ 1;
                    }
                }

                byte[] temp = a;
                a = b;
                b = temp;
            }
            //System.out.println("nRemovals: " + nRemovals);
        } while (nRemovals != 0 && (maxSkelIterations <= 0 || ++step < maxSkelIterations));
    }
}
