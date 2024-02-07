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
    public static void drawOutline(int[] outline, int rgbColor, double strokeWidth, byte[] image, int width, int height)
    {
        // this cuts out the need for branching
        byte firstInputPixel = image[0];
        int firstOutputPixel = outline[0];
        image[0] = 0;
        outline[0] = 0;

        for (int y = -1; y < height; y += 2) {
            for (int x = -1; x < width; x += 2) {
                int topLeft     = (x   + width * y)     & ~((x | y) >> 31);
                int topRight    = (x+1 + width * y)     & ~(((width-2-x) | y)  >> 31);
                int bottomLeft  = (x   + width * (y+1)) & ~((x | (height-2-y)) >> 31);
                int bottomRight = (x+1 + width * (y+1)) & ~(((width-2-x) | (height-2-y)) >> 31);

                int type =
                    ((image[topLeft]     >> 31) & 8) |
                    ((image[topRight]    >> 31) & 4) |
                    ((image[bottomRight] >> 31) & 2) |
                    ((image[bottomLeft]  >> 31) & 1);

                int p1 =
                    (topLeft & firstPointIsTopLeft[type]) |
                    (topRight & firstPointIsTopRight[type]) |
                    (bottomRight & firstPointIsBottomRight[type]) |
                    (bottomLeft & firstPointIsBottomLeft[type]);

                int p2 =
                    (topRight & secondPointIsTopRight[type]) |
                    (bottomRight & secondPointIsBottomRight[type]) |
                    (bottomLeft & secondPointIsBottomLeft[type]);

                /*
                if (p1 == 0 && p2 == 0)
                    continue;
                */

                outline[p1] = rgbColor;
                outline[p2] = rgbColor;
            }
        }

        image[0] = firstInputPixel;
        outline[0] = firstOutputPixel;
    }
}
