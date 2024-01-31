package Batch;

public class Image
{
    public static final int LAYER_NONE = 0;
    public static final int LAYER_SPLIT = 1;
    public static final int LAYER_COMBINED = 2;

    public interface Layer
    {
        byte[] getSelectedChannel();
        int[] getRgb();
        int[] acquireRgbCopy(int width, int height);
        void releaseBuffers();
    }

    public static class SplitLayer implements Layer
    {
        public byte[] red;
        public byte[] green;
        public byte[] blue;
        public int selectedChannelIdx;

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
        public int[] acquireRgbCopy(int width, int height)
        {
            int area = width * height;
            int[] rgb = IntBufferPool.acquireAsIs(area);
            for (int i = 0; i < area; i++)
                rgb[i] = (red[i] & 0xff) << 16;
            for (int i = 0; i < area; i++)
                rgb[i] |= (green[i] & 0xff) << 8;
            for (int i = 0; i < area; i++)
                rgb[i] |= blue[i] & 0xff;
            return rgb;
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
        public int[] acquireRgbCopy(int width, int height)
        {
            int area = width * height;
            int[] copy = IntBufferPool.acquireAsIs(area);
            System.arraycopy(rgb, 0, copy, 0, area);
            return copy;
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
