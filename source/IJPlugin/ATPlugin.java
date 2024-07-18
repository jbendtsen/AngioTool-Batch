package IJPlugin;

import AngioTool.AngioTool;
import AngioTool.AngioToolGui2;
import Pixels.ArgbBuffer;
import Utils.IntBufferPool;

import java.awt.image.DataBufferInt;
import java.io.File;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.plugin.filter.PlugInFilter;

public class ATPlugin implements PlugInFilter
{
    public class PlugContext
    {
        private ImageProcessor ip;
        public File imageFile;

        PlugContext(ImageProcessor proc)
        {
            this.ip = proc;
            this.imageFile = null;
        }

        public ArgbBuffer makeArgbCopy()
        {
            int width = ip.getWidth();
            int height = ip.getHeight();
            int area = width * height;

            int[] dstPixels = IntBufferPool.acquireAsIs(area);

            Object srcPixelsObj = ip.getPixels();
            if (srcPixelsObj instanceof byte[]) {
                byte[] srcPixels = (byte[])srcPixelsObj;
                for (int i = 0; i < area; i++) {
                    int lum = srcPixels[i] & 0xff;
                    dstPixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                }
            }
            else if (srcPixelsObj instanceof short[]) {
                short[] srcPixels = (short[])srcPixelsObj;
                for (int i = 0; i < area; i++) {
                    int lum = (srcPixels[i] >> 8) & 0xff;
                    dstPixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                }
            }
            else if (srcPixelsObj instanceof float[]) {
                float[] srcPixels = (float[])srcPixelsObj;
                for (int i = 0; i < area; i++) {
                    int lum = (int)(srcPixels[i] / 255.0f) & 0xff;
                    dstPixels[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                }
            }
            else if (srcPixelsObj instanceof int[]) {
                System.arraycopy((int[])srcPixelsObj, 0, dstPixels, 0, area);
            }

            return new ArgbBuffer(dstPixels, width, height);
        }

        public void setNewImage(int[] dstPixels, int dstWidth, int dstHeight)
        {
            int srcWidth = ip.getWidth();
            int srcHeight = ip.getHeight();
            Object srcPixelsObj = ip.getPixels();

            int width = Math.min(srcWidth, dstWidth);
            int height = Math.min(srcHeight, dstHeight);
            int area = width * height;

            if (srcPixelsObj instanceof byte[]) {
                byte[] srcPixels = (byte[])srcPixelsObj;
                for (int i = 0; i < area; i++) {
                    int argb = dstPixels[i];
                    int lum = ((argb >> 16) & 0xff) + ((argb >> 8) & 0xff) + (argb & 0xff);
                    srcPixels[i] = (byte)(lum / 3);
                }
            }
            else if (srcPixelsObj instanceof short[]) {
                short[] srcPixels = (short[])srcPixelsObj;
                for (int i = 0; i < area; i++) {
                    int argb = dstPixels[i];
                    int lum = ((argb >> 16) & 0xff) + ((argb >> 8) & 0xff) + (argb & 0xff);
                    srcPixels[i] = (short)(lum / 3);
                }
            }
            else if (srcPixelsObj instanceof float[]) {
                float[] srcPixels = (float[])srcPixelsObj;
                for (int i = 0; i < area; i++) {
                    int argb = dstPixels[i];
                    int lum = ((argb >> 16) & 0xff) + ((argb >> 8) & 0xff) + (argb & 0xff);
                    srcPixels[i] = (float)lum / 3.0f;
                }
            }
            else if (srcPixelsObj instanceof int[]) {
                if (srcWidth == dstWidth && srcHeight == dstHeight) {
                    // copy back, so dst and src are swapped
                    System.arraycopy(dstPixels, 0, (int[])srcPixelsObj, 0, area);
                }
                else {
                    int[] srcPixels = (int[])srcPixelsObj;
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++)
                            srcPixels[x + srcWidth * y] = dstPixels[x + dstWidth * y];
                    }
                }
            }
        }
    }

    @Override
    public int setup(String arg, ImagePlus imp)
    {
        return 31; // PlugInFilter.DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip)
    {
        AngioTool.preloadLee94();
        PlugContext ctx = new PlugContext(ip);
        AngioToolGui2 gui = AngioTool.initializeGui(ctx);
    }
}
