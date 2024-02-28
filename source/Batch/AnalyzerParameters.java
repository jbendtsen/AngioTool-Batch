package Batch;

import Pixels.Rgb;
import Utils.RefVector;
import java.lang.reflect.Field;

public class AnalyzerParameters {
    public String defaultPath;
    public String[] inputImagePaths;
    public String excelFilePath;
    public boolean shouldSaveResultImages;
    public boolean shouldSaveImagesToSpecificFolder;
    public String resultImagesPath;
    public String resultImageFormat;
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
    public boolean shouldComputeLacunarity;
    public boolean shouldComputeThickness;

    private AnalyzerParameters() {}

    public AnalyzerParameters(
        String defaultPath,
        String[] inputImagePaths,
        String excelFilePath,
        boolean shouldSaveResultImages,
        boolean shouldSaveImagesToSpecificFolder,
        String resultImagesPath,
        String resultImageFormat,
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
        boolean shouldComputeLacunarity,
        boolean shouldComputeThickness
    ) {
        this.defaultPath = defaultPath;
        this.inputImagePaths = inputImagePaths;
        this.excelFilePath = excelFilePath;
        this.shouldSaveResultImages = shouldSaveResultImages;
        this.shouldSaveImagesToSpecificFolder = shouldSaveImagesToSpecificFolder;
        this.resultImagesPath = resultImagesPath;
        this.resultImageFormat = resultImageFormat;
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
        this.shouldComputeLacunarity = shouldComputeLacunarity;
        this.shouldComputeThickness = shouldComputeThickness;
    }

    public static AnalyzerParameters defaults() {
        AnalyzerParameters p = new AnalyzerParameters();
        p.defaultPath = "C:/";
        p.outlineColor = new Rgb("FFFF00");
        p.skeletonColor = new Rgb("FF0000");
        p.branchingPointsColor = new Rgb("0099FF");
        p.convexHullColor = new Rgb("CCFFFF");
        p.outlineSize = 1;
        p.skeletonSize = 5;
        p.branchingPointsSize = 8;
        p.convexHullSize = 1;
        p.resultImageFormat = "jpg";
        p.shouldShowOverlayOrGallery = true;
        p.shouldDrawOutline = true;
        p.shouldDrawSkeleton = true;
        p.shouldDrawBranchPoints = true;
        p.shouldDrawConvexHull = true;
        p.shouldScalePixelValues = false;
        p.shouldFillHoles = false;
        p.shouldRemoveSmallParticles = false;
        p.resizingFactor = 1.0;
        p.shouldComputeLacunarity = true;
        p.shouldComputeThickness = true;
        p.linearScalingFactor = 0.0;
        p.sigmas = new double[] {5.0};
        p.thresholdLow = 10;
        p.thresholdHigh = 50;
        return p;
    }

    // TODO: something more sophisticated
    public static boolean shouldPersistField(Field f) {
        return !f.getName().equals("inputImagePaths");
    }

    // TODO: also something more sophisticated
    public static boolean isValidPath(String path) {
        return path != null && path.length() > 0;
    }

    public RefVector<String> validate() {
        RefVector<String> errors = new RefVector<>(String.class);
        try {
            Analyzer.resolveImageFormat(resultImageFormat);
        }
        catch (Exception ex) {
            errors.add("Result image format: " + ex.getMessage());
        }

        if (inputImagePaths == null || inputImagePaths.length == 0)
            errors.add("At least one input folder is required");
        if (!isValidPath(excelFilePath))
            errors.add("Path to spreadsheet is missing");
        if (shouldSaveImagesToSpecificFolder && !isValidPath(resultImagesPath))
            errors.add("Specific output folder was selected but not provided");
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
