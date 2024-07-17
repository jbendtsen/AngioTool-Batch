package ij.plugin.filter;

import ij.ImagePlus;
import ij.process.ImageProcessor;

public interface PlugInFilter
{
    int setup(String arg, ImagePlus imp);
    void run(ImageProcessor ip);
}
