package Pixels;

public class ArgbBuffer
{
    public int[] pixels;
    public int width;
    public int height;

    public int brightestChannel;

    public ArgbBuffer() {}

    public ArgbBuffer(int[] pixels, int width, int height)
    {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
    }
}
