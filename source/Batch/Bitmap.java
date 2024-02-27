package Batch;

public class Bitmap
{
    public static final int LAYER_NONE = 0;
    public static final int LAYER_SPLIT = 1;
    public static final int LAYER_COMBINED = 2;

    public static final int RESIZE_ROUNDING = 4 * 1024;

    public interface Layer
    {
        int getLayerType();
        byte[] getSelectedChannel();
        int[] getRgb();
        void exportRgb(int[] buf, int width, int height);
        void reallocate(int width, int height);
    }

    public static class SplitLayer implements Layer
    {
        public ByteVectorOutputStream red = new ByteVectorOutputStream();
        public ByteVectorOutputStream green = new ByteVectorOutputStream();
        public ByteVectorOutputStream blue = new ByteVectorOutputStream();
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
                    return red.buf;
                case 2:
                    return blue.buf;
            }
            return green.buf;
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
                buf[i] = (red.buf[i] & 0xff) << 16;
            for (int i = 0; i < area; i++)
                buf[i] |= (green.buf[i] & 0xff) << 8;
            for (int i = 0; i < area; i++)
                buf[i] |= blue.buf[i] & 0xff;
        }

        @Override
        public void reallocate(int width, int height)
        {
            int area = width * height;
            red.resize(area);
            green.resize(area);
            blue.resize(area);
        }
    }

    public static class CombinedLayer implements Layer
    {
        public IntVector rgb = new IntVector();

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
            return rgb.buf;
        }

        @Override
        public void exportRgb(int[] buf, int width, int height)
        {
            int area = width * height;
            System.arraycopy(rgb.buf, 0, buf, 0, area);
        }

        @Override
        public void reallocate(int width, int height)
        {
            rgb.resize(width * height);
        }
    }

    public RefVector<Layer> layers = new RefVector<Layer>(Layer.class);
    public int width;
    public int height;

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

    public Layer reallocate(Layer tpl, int width, int height)
    {
        this.width = width;
        this.height = height;

        Layer layer;
        if (layers.size == 0) {
            layers.add(tpl);
            layer = tpl;
        }
        else {
            layer = layers.buf[0];
        }

        layer.reallocate(width, height);
        return layer;
    }

    public void setFirstCombinedBuffer(int[] rgb, int width, int height)
    {
        this.width = width;
        this.height = height;

        CombinedLayer cl;
        if (layers.size == 0) {
            cl = new CombinedLayer();
            layers.add(cl);
        }
        else {
            cl = (CombinedLayer)layers.buf[0];
        }

        cl.rgb.buf = rgb;
        cl.rgb.size = width * height;
    }
}
