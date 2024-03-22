package AngioTool;

import Algorithms.PreprocessColor;
import Pixels.Rgb;
import Utils.Misc;
import Utils.RefVector;

public class AnalyzerParameters {
    public boolean shouldResizeImage;
    public double resizingFactor;
    public boolean shouldRemapColors;
    public double hueTransformWeight;
    public double brightnessTransformWeight;
    public Rgb targetRemapColor;
    public Rgb voidRemapColor;
    public double saturationFactor;
    public int[] brightnessLineSegments;
    public boolean shouldFillBrightShapes;
    public double brightShapeThresholdFactor;
    public boolean shouldApplyMinBoxness;
    public double minBoxness;
    public boolean shouldApplyMinAreaLength;
    public double minAreaLengthRatio;
    public boolean shouldRemoveSmallParticles;
    public double removeSmallParticlesThreshold;
    public boolean shouldFillHoles;
    public double fillHolesValue;
    public double[] sigmas;
    public int thresholdHigh;
    public int thresholdLow;
    public boolean shouldUseFastSkeletonizer;
    public boolean shouldCapSkelIterations;
    public int maxSkelIterations;
    public boolean shouldApplyLinearScale;
    public double linearScalingFactor;
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
    public boolean shouldIsolateBrightestChannelInOutput;
    public boolean shouldExpandOutputToGrayScale;
    public boolean shouldComputeLacunarity;
    public boolean shouldComputeThickness;

    private AnalyzerParameters() {}

    public AnalyzerParameters(
        boolean shouldResizeImage,
        double resizingFactor,
        boolean shouldRemapColors,
        double hueTransformWeight,
        double brightnessTransformWeight,
        Rgb targetRemapColor,
        Rgb voidRemapColor,
        double saturationFactor,
        int[] brightnessLineSegments,
        boolean shouldFillBrightShapes,
        double brightShapeThresholdFactor,
        boolean shouldApplyMinBoxness,
        double minBoxness,
        boolean shouldApplyMinAreaLength,
        double minAreaLengthRatio,
        boolean shouldRemoveSmallParticles,
        double removeSmallParticlesThreshold,
        boolean shouldFillHoles,
        double fillHolesValue,
        double[] sigmas,
        int thresholdHigh,
        int thresholdLow,
        boolean shouldUseFastSkeletonizer,
        boolean shouldCapSkelIterations,
        int maxSkelIterations,
        boolean shouldApplyLinearScale,
        double linearScalingFactor,
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
        boolean shouldIsolateBrightestChannelInOutput,
        boolean shouldExpandOutputToGrayScale,
        boolean shouldComputeLacunarity,
        boolean shouldComputeThickness
    ) {
        this.shouldResizeImage = shouldResizeImage;
        this.resizingFactor = resizingFactor;
        this.shouldRemapColors = shouldRemapColors;
        this.hueTransformWeight = hueTransformWeight;
        this.brightnessTransformWeight = brightnessTransformWeight;
        this.targetRemapColor = targetRemapColor;
        this.voidRemapColor = voidRemapColor;
        this.saturationFactor = saturationFactor;
        this.brightnessLineSegments = brightnessLineSegments;
        this.shouldFillBrightShapes = shouldFillBrightShapes;
        this.brightShapeThresholdFactor = brightShapeThresholdFactor;
        this.shouldApplyMinBoxness = shouldApplyMinBoxness;
        this.minBoxness = minBoxness;
        this.shouldApplyMinAreaLength = shouldApplyMinAreaLength;
        this.minAreaLengthRatio = minAreaLengthRatio;
        this.shouldRemoveSmallParticles = shouldRemoveSmallParticles;
        this.removeSmallParticlesThreshold = removeSmallParticlesThreshold;
        this.shouldFillHoles = shouldFillHoles;
        this.fillHolesValue = fillHolesValue;
        this.sigmas = sigmas;
        this.thresholdHigh = thresholdHigh;
        this.thresholdLow = thresholdLow;
        this.shouldUseFastSkeletonizer = shouldUseFastSkeletonizer;
        this.shouldCapSkelIterations = shouldCapSkelIterations;
        this.maxSkelIterations = maxSkelIterations;
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
        this.shouldDrawConvexHull = shouldDrawConvexHull;
        this.convexHullColor = convexHullColor;
        this.convexHullSize = convexHullSize;
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
        p.skeletonSize = 2;
        p.branchingPointsSize = 4;
        p.convexHullSize = 1;
        p.shouldDrawOutline = true;
        p.shouldDrawSkeleton = true;
        p.shouldDrawBranchPoints = true;
        p.shouldDrawConvexHull = true;
        p.shouldCapSkelIterations = true;
        p.maxSkelIterations = 25;
        p.shouldFillBrightShapes = false;
        p.brightShapeThresholdFactor = 1.5;
        p.shouldApplyMinBoxness = true;
        p.minBoxness = 0.09375;
        p.shouldApplyMinAreaLength = true;
        p.minAreaLengthRatio = 16.0;
        p.shouldFillHoles = false;
        p.shouldRemoveSmallParticles = false;
        p.shouldRemapColors = false;
        p.targetRemapColor = new Rgb("FF0000");
        p.voidRemapColor = new Rgb("FF9900");
        p.saturationFactor = 1.0;
        p.hueTransformWeight = 1.0;
        p.brightnessTransformWeight = 1.0;
        p.brightnessLineSegments = new int[] {0, 0, 100, 100};
        p.resizingFactor = 1.0;
        p.shouldIsolateBrightestChannelInOutput = true;
        p.shouldExpandOutputToGrayScale = true;
        p.shouldComputeLacunarity = true;
        p.shouldComputeThickness = true;
        p.linearScalingFactor = 1.0;
        p.sigmas = new double[] {12.0};
        p.thresholdLow = 15;
        p.thresholdHigh = 255;
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
            other.shouldRemapColors == shouldRemapColors &&
            other.hueTransformWeight == hueTransformWeight &&
            other.brightnessTransformWeight == brightnessTransformWeight &&
            other.targetRemapColor.value == targetRemapColor.value &&
            other.voidRemapColor.value == voidRemapColor.value &&
            other.saturationFactor == saturationFactor &&
            Misc.isIntArrayIdentical(other.brightnessLineSegments, brightnessLineSegments) &&
            other.shouldFillBrightShapes == shouldFillBrightShapes &&
            other.brightShapeThresholdFactor == brightShapeThresholdFactor &&
            other.shouldApplyMinBoxness == shouldApplyMinBoxness &&
            other.minBoxness == minBoxness &&
            other.shouldApplyMinAreaLength == shouldApplyMinAreaLength &&
            other.minAreaLengthRatio == minAreaLengthRatio &&
            other.shouldRemoveSmallParticles == shouldRemoveSmallParticles &&
            other.removeSmallParticlesThreshold == removeSmallParticlesThreshold &&
            other.shouldFillHoles == shouldFillHoles &&
            other.fillHolesValue == fillHolesValue &&
            Misc.isDoubleArraySimilar(other.sigmas, sigmas) &&
            other.thresholdHigh == thresholdHigh &&
            other.thresholdLow == thresholdLow &&
            other.shouldUseFastSkeletonizer == shouldUseFastSkeletonizer &&
            other.shouldCapSkelIterations == shouldCapSkelIterations &&
            other.maxSkelIterations == maxSkelIterations &&
            other.shouldApplyLinearScale == shouldApplyLinearScale &&
            other.linearScalingFactor == linearScalingFactor &&
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
            errors.add("Image resize factor must be >0% (not " + (resizingFactor * 100.0) + "%)");
        if (shouldRemapColors && hueTransformWeight <= 0.0 && brightnessTransformWeight <= 0.0)
            errors.add("Color transform weights must combine to have a positive weight (not " + hueTransformWeight + " and " + brightnessTransformWeight + ")");
        if (shouldRemapColors && Misc.isGrayscale(targetRemapColor.getRGB()))
            errors.add("Target color must not be on the gray scale, ie. it must have a hue (not " + targetRemapColor.toString() + ")");
        if (shouldRemapColors && Misc.isGrayscale(voidRemapColor.getRGB()))
            errors.add("Off color must not be on the gray scale, ie. it must have a hue (not " + targetRemapColor.toString() + ")");
        if (shouldRemapColors && Misc.getHue(targetRemapColor.getRGB()) == Misc.getHue(voidRemapColor.getRGB()))
            errors.add("Target and off colors must have a different hue");
        if (shouldRemapColors && (saturationFactor < 0.0 || saturationFactor > 1.0))
            errors.add("Saturation factor must be between 0% and 100% inclusive (not " + (saturationFactor * 100.0) + "%)");
        if (shouldRemapColors && !PreprocessColor.computeBrightnessTable(null, brightnessLineSegments, brightnessLineSegments.length / 2))
            errors.add("Invalid brightness segment list. Try changing every X coordinate to be unique");
        if (shouldFillBrightShapes && brightShapeThresholdFactor <= 0.0)
            errors.add("Shape fill threshold must be >0% (not " + (brightShapeThresholdFactor * 100.0) + "%)");
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
            errors.add("Measurement scale factor must be >0% (not " + (linearScalingFactor * 100.0) + "%)");
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
