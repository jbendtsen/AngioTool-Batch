package AngioTool;

import Pixels.Rgb;
import Utils.BatchUtils;
import Utils.RefVector;

public class AnalyzerParameters {
    public boolean shouldResizeImage;
    public double resizingFactor;
    public boolean shouldRemoveSmallParticles;
    public double removeSmallParticlesThreshold;
    public boolean shouldFillHoles;
    public double fillHolesValue;
    public double[] sigmas;
    public int thresholdHigh;
    public int thresholdLow;
    public boolean shouldUseFastSkeletonizer;
    public boolean shouldApplyLinearScale;
    public double linearScalingFactor;
    public boolean shouldShowOverlayOrGallery;
    public boolean shouldDrawOutline;
    public Rgb outlineColor;
    public double outlineSize;
    public boolean shouldDrawSkeleton;
    public Rgb skeletonColor;
    public double skeletonSize;
    public boolean shouldDrawBranchPoints;
    public Rgb branchingPointsColor;
    public double branchingPointsSize;
    public boolean shouldDrawConvexHull;
    public Rgb convexHullColor;
    public double convexHullSize;
    public boolean shouldScalePixelValues; // doScaling, ie. pixels that had values between min-max become between 0-255
    public boolean shouldIsolateBrightestChannelInOutput;
    public boolean shouldExpandOutputToGrayScale;
    public boolean shouldComputeLacunarity;
    public boolean shouldComputeThickness;

    public AnalyzerParameters() {}

    public AnalyzerParameters(
        boolean shouldResizeImage,
        double resizingFactor,
        boolean shouldRemoveSmallParticles,
        double removeSmallParticlesThreshold,
        boolean shouldFillHoles,
        double fillHolesValue,
        double[] sigmas,
        int thresholdHigh,
        int thresholdLow,
        boolean shouldUseFastSkeletonizer,
        boolean shouldApplyLinearScale,
        double linearScalingFactor,
        boolean shouldShowOverlayOrGallery,
        boolean shouldDrawOutline,
        Rgb outlineColor,
        double outlineSize,
        boolean shouldDrawSkeleton,
        Rgb skeletonColor,
        double skeletonSize,
        boolean shouldDrawBranchPoints,
        Rgb branchingPointsColor,
        double branchingPointsSize,
        boolean shouldDrawConvexHull,
        Rgb convexHullColor,
        double convexHullSize,
        boolean shouldScalePixelValues,
        boolean shouldIsolateBrightestChannelInOutput,
        boolean shouldExpandOutputToGrayScale,
        boolean shouldComputeLacunarity,
        boolean shouldComputeThickness
    ) {
        this.shouldResizeImage = shouldResizeImage;
        this.resizingFactor = resizingFactor;
        this.shouldRemoveSmallParticles = shouldRemoveSmallParticles;
        this.removeSmallParticlesThreshold = removeSmallParticlesThreshold;
        this.shouldFillHoles = shouldFillHoles;
        this.fillHolesValue = fillHolesValue;
        this.sigmas = sigmas;
        this.thresholdHigh = thresholdHigh;
        this.thresholdLow = thresholdLow;
        this.shouldUseFastSkeletonizer = shouldUseFastSkeletonizer;
        this.shouldApplyLinearScale = shouldApplyLinearScale;
        this.linearScalingFactor = linearScalingFactor;
        this.shouldShowOverlayOrGallery = shouldShowOverlayOrGallery;
        this.shouldDrawOutline = shouldDrawOutline;
        this.outlineColor = outlineColor;
        this.outlineSize = outlineSize;
        this.shouldDrawSkeleton = shouldDrawSkeleton;
        this.skeletonColor = skeletonColor;
        this.skeletonSize = skeletonSize;
        this.shouldDrawBranchPoints = shouldDrawBranchPoints;
        this.branchingPointsColor = branchingPointsColor;
        this.branchingPointsSize = branchingPointsSize;
        this.shouldDrawConvexHull = shouldDrawConvexHull;
        this.convexHullColor = convexHullColor;
        this.convexHullSize = convexHullSize;
        this.shouldScalePixelValues = shouldScalePixelValues;
        this.shouldIsolateBrightestChannelInOutput = shouldIsolateBrightestChannelInOutput;
        this.shouldExpandOutputToGrayScale = shouldExpandOutputToGrayScale;
        this.shouldComputeLacunarity = shouldComputeLacunarity;
        this.shouldComputeThickness = shouldComputeThickness;
    }

    public static AnalyzerParameters defaults()
    {
        AnalyzerParameters p = new AnalyzerParameters();
        p.outlineColor = new Rgb("FFFF00");
        p.skeletonColor = new Rgb("FF0000");
        p.branchingPointsColor = new Rgb("0099FF");
        p.convexHullColor = new Rgb("CCFFFF");
        p.outlineSize = 1;
        p.skeletonSize = 5;
        p.branchingPointsSize = 8;
        p.convexHullSize = 1;
        p.shouldShowOverlayOrGallery = true;
        p.shouldDrawOutline = true;
        p.shouldDrawSkeleton = true;
        p.shouldDrawBranchPoints = true;
        p.shouldDrawConvexHull = true;
        p.shouldScalePixelValues = false;
        p.shouldFillHoles = false;
        p.shouldRemoveSmallParticles = false;
        p.resizingFactor = 1.0;
        p.shouldIsolateBrightestChannelInOutput = true;
        p.shouldComputeLacunarity = true;
        p.shouldComputeThickness = true;
        p.linearScalingFactor = 0.0;
        p.sigmas = new double[] {5.0};
        p.thresholdLow = 10;
        p.thresholdHigh = 50;
        return p;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AnalyzerParameters))
            return false;

        AnalyzerParameters other = (AnalyzerParameters)obj;
        return
            other.shouldResizeImage == shouldResizeImage &&
            other.resizingFactor == resizingFactor &&
            other.shouldRemoveSmallParticles == shouldRemoveSmallParticles &&
            other.removeSmallParticlesThreshold == removeSmallParticlesThreshold &&
            other.shouldFillHoles == shouldFillHoles &&
            other.fillHolesValue == fillHolesValue &&
            BatchUtils.isDoubleArraySimilar(other.sigmas, sigmas) &&
            other.thresholdHigh == thresholdHigh &&
            other.thresholdLow == thresholdLow &&
            other.shouldUseFastSkeletonizer == shouldUseFastSkeletonizer &&
            other.shouldApplyLinearScale == shouldApplyLinearScale &&
            other.linearScalingFactor == linearScalingFactor &&
            other.shouldShowOverlayOrGallery == shouldShowOverlayOrGallery &&
            other.shouldDrawOutline == shouldDrawOutline &&
            other.outlineColor.value == outlineColor.value &&
            other.outlineSize == outlineSize &&
            other.shouldDrawSkeleton == shouldDrawSkeleton &&
            other.skeletonColor.value == skeletonColor.value &&
            other.skeletonSize == skeletonSize &&
            other.shouldDrawBranchPoints == shouldDrawBranchPoints &&
            other.branchingPointsColor.value == branchingPointsColor.value &&
            other.branchingPointsSize == branchingPointsSize &&
            other.shouldDrawConvexHull == shouldDrawConvexHull &&
            other.convexHullColor.value == convexHullColor.value &&
            other.convexHullSize == convexHullSize &&
            other.shouldScalePixelValues == shouldScalePixelValues &&
            other.shouldIsolateBrightestChannelInOutput == shouldIsolateBrightestChannelInOutput &&
            other.shouldExpandOutputToGrayScale == shouldExpandOutputToGrayScale &&
            other.shouldComputeLacunarity == shouldComputeLacunarity &&
            other.shouldComputeThickness == shouldComputeThickness
        ;
    }

    public RefVector<String> validate()
    {
        RefVector<String> errors = new RefVector<>(String.class);

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
        if (shouldDrawConvexHull && convexHullColor == null)
            errors.add("Boundary color is missing");
        if (shouldDrawConvexHull && convexHullSize <= 0)
            errors.add("Boundary size must be >0 (not " + convexHullSize + ")");

        return errors;
    }
}
