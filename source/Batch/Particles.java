package Batch;

public class Particles
{
    public static void fillHoles(byte[] image, int width, int height, double maxSize, byte pixelFind, byte pixelReplace)
    {
        int[] buf = IntBufferPool.acquireZeroed(width * height);
        int occ = 0;

        for (int y = 0; y < height; y++) {
            byte prev = pixelFind;
            for (int x = 0; x < width; x++) {
                byte cur = image[x + width * y];
                if (cur != pixelFind || buf[x + width * y] != 0) {
                    prev = cur;
                    continue;
                }

                occ++;
                int upCount = 0;
                int downCount = 0;
                int leftCount = 0;
                int rightCount = 0;
                int count = 0;
                while (true) {
                    // ...
                    break;
                }
            }
        }

        IntBufferPool.release(buf);
    }
}
