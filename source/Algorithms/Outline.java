package Algorithms;

import Utils.IntVector;
import java.util.Arrays;

public class Outline
{
    static final int[] firstPointIsTopLeft = new int[] {
        0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, -1, 0
    };
    static final int[] firstPointIsTopRight = new int[] {
        0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, 0, 0
    };
    static final int[] firstPointIsBottomLeft = new int[] {
        0, -1, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    static final int[] firstPointIsBottomRight = new int[] {
        0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    static final int[] secondPointIsTopRight = new int[] {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0
    };
    static final int[] secondPointIsBottomRight = new int[] {
        0, 0, 0, -1, 0, 0, -1, 0, 0, 0, -1, -1, 0, 0, -1, 0
    };
    static final int[] secondPointIsBottomLeft = new int[] {
        0, 0, 0, 0, 0, -1, 0, -1, 0, -1, 0, 0, 0, -1, 0, 0
    };

    public static void drawOutline(
        int[] outline,
        int[] outlineScratch1,
        int[] outlineScratch2,
        int rgbColor,
        double strokeWidth,
        byte[] image,
        int width,
        int height
    ) {
        int area = width * height;
        int iterations = (int)Math.ceil(strokeWidth);

        int[] readingScratch = outlineScratch1;
        int[] writingScratch = outlineScratch2;

        Arrays.fill(writingScratch, 0, area, 0);

        byte firstInputPixel = image[0];
        int firstOutputPixel = outline[0];
        image[0] = 0;
        outline[0] = 0;
        readingScratch[0] = 0;

        for (int i = 0; i < iterations; i++) {
            int shouldReadMask = (-iterations) >> 31;

            for (int y = -1; y < height; y++) {
                int topLeft = 0, topRight = 0, bottomLeft = 0, bottomRight = 0;
                int topLeftIdx = 0, topRightIdx = 0, bottomLeftIdx = 0, bottomRightIdx = 0;
                readingScratch[0] = 0;

                for (int x = -1; x < width; x++) {
                    topLeft = topRight;
                    topLeftIdx = topRightIdx;
                    bottomLeft = bottomRight;
                    bottomLeftIdx = bottomRightIdx;

                    topRightIdx    = (x < width-1 && y >= 0)       ? (x+1) + width * y     : 0;
                    bottomRightIdx = (x < width-1 && y < height-1) ? (x+1) + width * (y+1) : 0;

                    topRight    = (image[topRightIdx]    ^ (-readingScratch[topRightIdx]    & shouldReadMask)) >> 31;
                    bottomRight = (image[bottomRightIdx] ^ (-readingScratch[bottomRightIdx] & shouldReadMask)) >> 31;

                    int type = (topLeft & 8) | (topRight & 4) | (bottomRight & 2) | (bottomLeft & 1);

                    if (type == 0 || type == 15)
                        continue;

                    int p1 =
                        (topLeftIdx & firstPointIsTopLeft[type]) |
                        (topRightIdx & firstPointIsTopRight[type]) |
                        (bottomRightIdx & firstPointIsBottomRight[type]) |
                        (bottomLeftIdx & firstPointIsBottomLeft[type]);

                    int p2 =
                        (topRightIdx & secondPointIsTopRight[type]) |
                        (bottomRightIdx & secondPointIsBottomRight[type]) |
                        (bottomLeftIdx & secondPointIsBottomLeft[type]);

                    int p3 =
                        (topLeftIdx & (-(((type - 13) | (13 - type)) >> 31) - 1)) |
                        (topRightIdx & (-(((type - 14) | (14 - type)) >> 31) - 1)) |
                        (bottomRightIdx & (-(((type - 7) | (7 - type)) >> 31) - 1)) |
                        (bottomLeftIdx & (-(((type - 11) | (11 - type)) >> 31) - 1));

                    outline[p1] = rgbColor;
                    outline[p2] = rgbColor;
                    outline[p3] = rgbColor;
                    writingScratch[p1] = 1;
                    writingScratch[p2] = 1;
                    writingScratch[p3] = 1;
                }
            }

            int[] temp = readingScratch;
            readingScratch = writingScratch;
            writingScratch = temp;
        }

        image[0] = firstInputPixel;
        outline[0] = firstOutputPixel;
    }
}
