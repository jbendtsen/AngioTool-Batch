package Batch;

public class Outline
{
    // TODO: Add LUT for stroke direction
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

    // TODO: implement strokeWidth
    public static void drawOutline(
        int[] outline,
        int rgbColor,
        double strokeWidth,
        IntVector shapes,
        int[] shapeRegions,
        byte[] image,
        int width,
        int height
    ) {
        int area = width * height;
        double holeThreshold = area / 50.0;

        byte firstInputPixel = image[0];
        int firstOutputPixel = outline[0];
        image[0] = 0;
        outline[0] = 0;

        for (int y = -1; y < height; y++) {
            int topLeft = 0, topRight = 0, bottomLeft = 0, bottomRight = 0;
            int topLeftIdx = 0, topRightIdx = 0, bottomLeftIdx = 0, bottomRightIdx = 0;
            for (int x = -1; x < width; x++) {
                topLeft = topRight;
                topLeftIdx = topRightIdx;
                bottomLeft = bottomRight;
                bottomLeftIdx = bottomRightIdx;

                topRightIdx    = (x < width-1 && y >= 0)       ? (x+1) + width * y     : 0;
                bottomRightIdx = (x < width-1 && y < height-1) ? (x+1) + width * (y+1) : 0;

                topRight    = image[topRightIdx] >> 31;
                bottomRight = image[bottomRightIdx] >> 31;

                // If this pixel is black and it belongs to a thin shape,
                // it is considered white such that it can be ignored.
                // Thickness = skelIterations / sqrt(area)
                if (topRight == 0 && x < width-1 && y >= 0) {
                    int region = Math.abs(shapeRegions[(x+1) + width * y]);
                    if (region != 0) {
                        int idx = Particles.N_SHAPE_MEMBERS * (region - 1);
                        double shapeArea = shapes.buf[idx+1];
                        double thickness = (double)(shapes.buf[idx+2] + 1);
                        double thinness = Math.sqrt(shapeArea) / (thickness*thickness);
                        //double thinness = (double)shapes.buf[idx] / Math.sqrt(shapeArea);
                        //if (shapeArea < holeThreshold && (thinness <= 6.0 || shapeArea <= 9.0))
                        if (thinness > 0.5 && thickness < Particles.MAX_SKEL_ITERATIONS)
                            topRight = -1;
                    }
                }
                if (bottomRight == 0 && x < width-1 && y < height-1) {
                    int region = Math.abs(shapeRegions[(x+1) + width * (y+1)]);
                    if (region != 0) {
                        int idx = Particles.N_SHAPE_MEMBERS * (region - 1);
                        double shapeArea = shapes.buf[idx+1];
                        double thickness = (double)(shapes.buf[idx+2] + 1);
                        double thinness = Math.sqrt(shapeArea) / (thickness*thickness);
                        //double thinness = (double)shapes.buf[idx] / Math.sqrt(shapeArea);
                        //if (shapeArea < holeThreshold && (thinness <= 6.0 || shapeArea <= 9.0))
                        if (thinness > 0.5 && thickness < Particles.MAX_SKEL_ITERATIONS)
                            bottomRight = -1;
                    }
                }

                int type = (topLeft & 8) | (topRight & 4) | (bottomRight & 2) | (bottomLeft & 1);

                /*
                outline[topLeft]     = type >= 1 && type <= 14 && (type & 8) == 0 ? rgbColor : 0;
                outline[topRight]    = type >= 1 && type <= 14 && (type & 4) == 0 ? rgbColor : 0;
                outline[bottomRight] = type >= 1 && type <= 14 && (type & 2) == 0 ? rgbColor : 0;
                outline[bottomLeft]  = type >= 1 && type <= 14 && (type & 1) == 0 ? rgbColor : 0;
                */

                int p1 =
                    (topLeftIdx & firstPointIsTopLeft[type]) |
                    (topRightIdx & firstPointIsTopRight[type]) |
                    (bottomRightIdx & firstPointIsBottomRight[type]) |
                    (bottomLeftIdx & firstPointIsBottomLeft[type]);

                int p2 =
                    (topRightIdx & secondPointIsTopRight[type]) |
                    (bottomRightIdx & secondPointIsBottomRight[type]) |
                    (bottomLeftIdx & secondPointIsBottomLeft[type]);

                if (p1 == 0 && p2 == 0)
                    continue;

                outline[p1] = rgbColor;
                outline[p2] = rgbColor;
            }
        }

        image[0] = firstInputPixel;
        outline[0] = firstOutputPixel;
    }
}
