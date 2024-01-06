package Batch;

public class Zha84 {
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

    public static void skeletonizeZha84(byte[] image, int width, int height)
    {
        byte[] pages = new byte[width * height * 2];
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                int idx = j + width*i;
                pages[idx] = (byte)(image[idx] >>> 31 | -image[idx] >>> 31);
            }
        }

        int nRemovals;
        do {
            nRemovals = 0;

            int p1 = 0;
            int p2 = width * height;
            for (int pass = 1; pass <= 2; pass++) {
                for (int i = 1; i < height-1; i++) {
                    for (int j = 1; j < width-1; j++) {
                        int a = p1 + j + width*i;
                        int shouldKeep = 1;
                        if (pages[a] != 0) {
                            int value = lut[
                                pages[a-width-1] |
                                pages[a-width] << 1 |
                                pages[a-width+1] << 2 |
                                pages[a+1] << 3 |
                                pages[a+width+1] << 4 |
                                pages[a+width] << 5 |
                                pages[a+width-1] << 6 |
                                pages[a-1] << 7
                            ];

                            //boolean shouldKeep = value != 3 && value != pass;
                            shouldKeep =
                                ((value-3)    >>> 31 | -(value-3)    >>> 31) &
                                ((value-pass) >>> 31 | -(value-pass) >>> 31);
                        }

                        int b = p2 + j + width*i;
                        pages[b] = (byte)(shouldKeep * pages[a]);
                        nRemovals += shouldKeep ^ 1;
                    }
                }
                int temp = p1;
                p1 = p2;
                p2 = temp;
            }
            //System.out.println("nRemovals: " + nRemovals);
        } while (nRemovals != 0);

        System.arraycopy(pages, 0, image, 0, width * height);
    }
}
