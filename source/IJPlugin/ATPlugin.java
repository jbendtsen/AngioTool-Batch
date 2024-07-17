package IJPlugin;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.plugin.filter.PlugInFilter;

public class ATPlugin implements PlugInFilter
{
    @Override
    public int setup(String arg, ImagePlus imp)
    {
        return 31; // PlugInFilter.DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip)
    {
        
    }
}
