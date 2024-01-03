package AngioTool;

import java.awt.Color;
import java.util.ArrayList;

public class AnalyzerParameters {
    public String[] inputPaths;
    public String excelFilePath;
    public boolean shouldSaveResultImages;
    public String resultImagesPath;
    public boolean shouldResizeImage;
    public double resizingFactor;
    public boolean shouldRemoveSmallParticles;
    public double removeSmallParticlesThreshold;
    public boolean shouldFillHoles;
    public double fillHolesValue;
    public int[] sigmas;
    public int thresholdHigh;
    public int thresholdLow;
    public boolean shouldApplyLinearScale;
    public double linearScalingFactor;
    public boolean shouldDrawOutline;
    public Color outlineColor;
    public double outlineSize;
    public boolean shouldDrawSkeleton;
    public Color skeletonColor;
    public double skeletonSize;
    public boolean shouldDrawBranchPoints;
    public Color branchingPointsColor;
    public double branchingPointsSize;
    public boolean shouldDrawBoundary;
    public Color boundaryColor;
    public double boundarySize;
    public boolean shouldComputeLacunarity;
    public boolean shouldComputeThickness;

    public AnalyzerParameters(
        String[] inputPaths,
        String excelFilePath,
        boolean shouldSaveResultImages,
        String resultImagesPath,
        boolean shouldResizeImage,
        double resizingFactor,
        boolean shouldRemoveSmallParticles,
        double removeSmallParticlesThreshold,
        boolean shouldFillHoles,
        double fillHolesValue,
        int[] sigmas,
        int thresholdHigh,
        int thresholdLow,
        boolean shouldApplyLinearScale,
        double linearScalingFactor,
        boolean shouldDrawOutline,
        Color outlineColor,
        double outlineSize,
        boolean shouldDrawSkeleton,
        Color skeletonColor,
        double skeletonSize,
        boolean shouldDrawBranchPoints,
        Color branchingPointsColor,
        double branchingPointsSize,
        boolean shouldDrawBoundary,
        Color boundaryColor,
        double boundarySize,
        boolean shouldComputeLacunarity,
        boolean shouldComputeThickness
    ) {
        this.inputPaths = inputPaths;
        this.excelFilePath = excelFilePath;
        this.shouldSaveResultImages = shouldSaveResultImages;
        this.resultImagesPath = resultImagesPath;
        this.shouldResizeImage = shouldResizeImage;
        this.resizingFactor = resizingFactor;
        this.shouldRemoveSmallParticles = shouldRemoveSmallParticles;
        this.removeSmallParticlesThreshold = removeSmallParticlesThreshold;
        this.shouldFillHoles = shouldFillHoles;
        this.fillHolesValue = fillHolesValue;
        this.sigmas = sigmas;
        this.thresholdHigh = thresholdHigh;
        this.thresholdLow = thresholdLow;
        this.shouldApplyLinearScale = shouldApplyLinearScale;
        this.linearScalingFactor = linearScalingFactor;
        this.shouldDrawOutline = shouldDrawOutline;
        this.outlineColor = outlineColor;
        this.outlineSize = outlineSize;
        this.shouldDrawSkeleton = shouldDrawSkeleton;
        this.skeletonColor = skeletonColor;
        this.skeletonSize = skeletonSize;
        this.shouldDrawBranchPoints = shouldDrawBranchPoints;
        this.branchingPointsColor = branchingPointsColor;
        this.branchingPointsSize = branchingPointsSize;
        this.shouldDrawBoundary = shouldDrawBoundary;
        this.boundaryColor = boundaryColor;
        this.boundarySize = boundarySize;
        this.shouldComputeLacunarity = shouldComputeLacunarity;
        this.shouldComputeThickness = shouldComputeThickness;
    }

    public ArrayList<String> validate() {
        ArrayList<String> errors = new ArrayList<>();
        if (inputPaths == null || inputPaths.length == 0)
            errors.add("At least one input folder is required");
        if (excelFilePath == null || excelFilePath.length() == 0)
            errors.add("Path to spreadsheet is missing");
        //resultImagesPath
        if (shouldResizeImage && resizingFactor <= 0.0)
            errors.add("Image resize factor must be >0 (not " + resizingFactor + ")");
        if (shouldRemoveSmallParticles && removeSmallParticlesThreshold <= 0.0)
            errors.add("Remove particles threshold must be >0 (not " + removeSmallParticlesThreshold + ")");
        if (shouldFillHoles && fillHolesValue <= 0.0)
            errors.add("Fill holes value must be >0 (not " + fillHolesValue + ")");
        if (sigmas == null || sigmas.length == 0)
            errors.add("At least one vessel diameter is required (eg. 5, 12)");
        if (thresholdLow < 0 || thresholdLow > 255)
            errors.add("Minimum vessel intensity must be within [0-255] (not " + thresholdLow + ")");
        if (thresholdHigh < 0 || thresholdHigh > 255)
            errors.add("Maximum vessel intensity must be within [0-255] (not " + thresholdHigh + ")");
        if (thresholdLow > thresholdHigh)
            errors.add("Minimum vessel intensity must be <= to the maximum (not " + thresholdLow + "-" + thresholdHigh + ")");
        if (shouldApplyLinearScale && linearScalingFactor <= 0.0)
            errors.add("Measurement scale factor must be >0 (not " + linearScalingFactor + ")");
        if (shouldDrawOutline && outlineColor == null)
            errors.add("Outline color is missing");
        if (shouldDrawOutline && outlineSize <= 0)
            errors.add("Outline size must be >0 (not " + outlineSize + ")");
        if (shouldDrawSkeleton && skeletonColor == null)
            errors.add("Skeleton color is missing");
        if (shouldDrawSkeleton && skeletonSize <= 0)
            errors.add("Skeleton size must be >0 (not " + skeletonSize + ")");
        if (shouldDrawBranchPoints && branchingPointsColor == null)
            errors.add("Branch color is missing");
        if (shouldDrawBranchPoints && branchingPointsSize <= 0)
            errors.add("Branch size must be >0 (not " + branchingPointsSize + ")");
        if (shouldDrawBoundary && boundaryColor == null)
            errors.add("Boundary color is missing");
        if (shouldDrawBoundary && boundarySize <= 0)
            errors.add("Boundary size must be >0 (not " + boundarySize + ")");
        return errors;
    }
}
