package Batch;

public class AnalyzeSkeleton2
{
    public static SkeletonResult analyze(ImagePlus origIP)
    {
        int width = this.imRef.getWidth();
        int height = this.imRef.getHeight();
        int depth = this.imRef.getStackSize();
        inputImage = this.imRef.getStack();

        processSkeleton(inputImage);

        calculateTripleAndQuadruplePoints();

        return assembleResults();
    }
}
