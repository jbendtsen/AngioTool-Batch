package Batch;

public class Bitmap
{
    public static final int LAYER_NONE = 0;
    public static final int LAYER_SPLIT = 1;
    public static final int LAYER_COMBINED = 2;

    public interface Layer
    {
        int getLayerType();
        byte[] getSelectedChannel();
        int[] getRgb();
        void exportRgb(int[] buf, int width, int height);
        void releaseBuffers();
    }

    public static class SplitLayer implements Layer
    {
        public byte[] red;
        public byte[] green;
        public byte[] blue;
        public int selectedChannelIdx;

        @Override
        public int getLayerType()
        {
            return LAYER_SPLIT;
        }

        @Override
        public byte[] getSelectedChannel()
        {
            switch (selectedChannelIdx) {
                case 0:
                    return red;
                case 2:
                    return blue;
            }
            return green;
        }

        @Override
        public int[] getRgb()
        {
            return null;
        }

        @Override
        public void exportRgb(int[] buf, int width, int height)
        {
            int area = width * height;
            for (int i = 0; i < area; i++)
                buf[i] = (red[i] & 0xff) << 16;
            for (int i = 0; i < area; i++)
                buf[i] |= (green[i] & 0xff) << 8;
            for (int i = 0; i < area; i++)
                buf[i] |= blue[i] & 0xff;
        }

        @Override
        public void releaseBuffers()
        {
            red = ByteBufferPool.release(red);
            green = ByteBufferPool.release(green);
            blue = ByteBufferPool.release(blue);
        }
    }

    public static class CombinedLayer implements Layer
    {
        public int[] rgb;

        @Override
        public int getLayerType()
        {
            return LAYER_COMBINED;
        }

        @Override
        public byte[] getSelectedChannel()
        {
            return null;
        }

        @Override
        public int[] getRgb()
        {
            return rgb;
        }

        @Override
        public void exportRgb(int[] buf, int width, int height)
        {
            int area = width * height;
            System.arraycopy(rgb, 0, buf, 0, area);
        }

        @Override
        public void releaseBuffers()
        {
            rgb = IntBufferPool.release(rgb);
        }
    }

    public RefVector<Layer> layers = new RefVector<Layer>(Layer.class);
    public int width;
    public int height;
    public double pixelWidth;
    public double pixelHeight;
    public double pixelBreadth;

    public byte[] getDefaultChannel()
    {
        return layers.buf[0].getSelectedChannel();
    }

    public int[] getDefaultRgb()
    {
        return layers.buf[0].getRgb();
    }

    public int getBreadth()
    {
        return layers.size;
    }
}
