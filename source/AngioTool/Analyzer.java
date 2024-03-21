package AngioTool;

import Algorithms.*;
import Pixels.*;
import Utils.*;
import Xlsx.*;
import java.awt.Color;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;

public class Analyzer
{
    public static final int MAX_WORKERS = 24;

    public static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
        /* corePoolSize */ MAX_WORKERS,
        /* maximumPoolSize */ MAX_WORKERS,
        /* keepAliveTime */ 30,
        /* unit */ TimeUnit.SECONDS,
        /* workQueue */ new LinkedBlockingQueue<>()
    );

    public static class Stats
    {
        public String imageFileName;
        public String imageAbsolutePath;
        public int imageWidth;
        public int imageHeight;
        public int thresholdLow;
        public int thresholdHigh;
        public double[] sigmas;
        public double removeSmallParticlesThreshold;
        public double fillHolesValue;
        public double brightShapeThresholdFactor;
        public double minBoxness;
        public double minAreaLengthRatio;
        public boolean usedFastSkeletonizer;
        public int maxSkelIterations;
        public double linearScalingFactor;
        public double allantoisMMArea;
        public double vesselMMArea;
        public double vesselPercentageArea;
        public double totalNJunctions;
        public double junctionsPerScaledArea;
        public double totalLength;
        public double averageBranchLength;
        public int totalNEndPoints;
        public double averageVesselDiameter;
        public double ELacunarityMedial;
        public double ELacunarityCurve;
        public double FLacunarityMedial;
        public double FLacunarityCurve;
        public double meanFl;
        public double meanEl;
    }

    public static class Data
    {
        public double convexHullArea;
        public long vesselPixelArea;

        // Recycling resources
        public ByteVectorOutputStream analysisImage = new ByteVectorOutputStream();
        public AnalyzeSkeleton2.Result skelResult = new AnalyzeSkeleton2.Result();
        public Particles.Data particles = new Particles.Data();
        public Lacunarity2.Statistics lacunarity = new Lacunarity2.Statistics();
        public IntVector convexHull = new IntVector();

        public final float[] brightnessRemapTable;

        public Data(int[] lineSegments, int nPoints)
        {
            this.brightnessRemapTable = new float[255];
            PreprocessColor.computeBrightnessTable(brightnessRemapTable, lineSegments, nPoints);
        }

        public void updateBrightnessTable(int[] lineSegments, int nPoints)
        {
            PreprocessColor.computeBrightnessTable(brightnessRemapTable, lineSegments, nPoints);
        }

        public void restart()
        {
            if (analysisImage == null)
                analysisImage = new ByteVectorOutputStream();
            if (skelResult == null)
                skelResult = new AnalyzeSkeleton2.Result();
            if (particles == null)
                particles = new Particles.Data();
            if (lacunarity == null)
                lacunarity = new Lacunarity2.Statistics();
            if (convexHull == null)
                convexHull = new IntVector();
        }

        public void nullify()
        {
            analysisImage = null;
            skelResult = null;
            particles = null;
            lacunarity = null;
            convexHull = null;
        }
    }

    public static Stats analyze(
        Data data,
        File inFile,
        ArgbBuffer inputImage,
        AnalyzerParameters params,
        ISliceRunner sliceRunner
    ) throws ExecutionException
    {
        data.analysisImage.resize(inputImage.width * inputImage.height);
        byte[] analysisImage = data.analysisImage.buf;

        float[] tubenessInput = FloatBufferPool.acquireAsIs(inputImage.width * inputImage.height);

        if (params.shouldRemapColors)
            PreprocessColor.transformToMonoFloatArray(
                tubenessInput,
                inputImage.pixels,
                inputImage.width,
                inputImage.height,
                (float)params.hueTransformWeight,
                (float)params.brightnessTransformWeight,
                params.targetRemapColor.getRGB(),
                params.voidRemapColor.getRGB(),
                data.brightnessRemapTable
            );
        else
            Planes.copySingleChannelToMonoFloatArray(
                tubenessInput,
                inputImage.pixels,
                inputImage.width,
                inputImage.height,
                inputImage.brightestChannel
            );

        Tubeness.computeTubenessImage(
            sliceRunner,
            MAX_WORKERS,
            analysisImage,
            tubenessInput,
            inputImage.width,
            inputImage.height,
            inputImage.brightestChannel,
            params.sigmas,
            params.sigmas.length
        );

        FloatBufferPool.release(tubenessInput);

        //ImageFile.writePgm(analysisImage, inputImage.width, inputImage.height, inFile.getAbsolutePath() + " tubeness.pgm");

        Misc.thresholdFlexible(
            analysisImage,
            inputImage.width,
            inputImage.height,
            params.thresholdLow,
            params.thresholdHigh
        );

        //ImageFile.writePgm(analysisImage, inputImage.width, inputImage.height, inFile.getAbsolutePath() + " thresholded.pgm");

        byte[] tempImage = ByteBufferPool.acquireAsIs(inputImage.width * inputImage.height);

        Filters.filterMax(tempImage, analysisImage, inputImage.width, inputImage.height); // erode
        Filters.filterMax(analysisImage, tempImage, inputImage.width, inputImage.height); // erode
        Filters.filterMin(tempImage, analysisImage, inputImage.width, inputImage.height); // dilate
        Filters.filterMin(analysisImage, tempImage, inputImage.width, inputImage.height); // dilate

        ByteBufferPool.release(tempImage);

        //ImageFile.writePgm(analysisImage, inputImage.width, inputImage.height, "filtered.pgm");

        int[] particleBuf = IntBufferPool.acquireAsIs(inputImage.width * inputImage.height);

        Particles.computeShapes(
            data.particles,
            particleBuf,
            analysisImage,
            inputImage.pixels,
            inputImage.width,
            inputImage.height
        );

        double maxHoleLevel = params.shouldFillBrightShapes ? params.brightShapeThresholdFactor : 0.0;
        double minBoxness = params.shouldApplyMinBoxness ? params.minBoxness : 0.0;
        double minAreaLengthRatio = params.shouldApplyMinAreaLength ? params.minAreaLengthRatio : 0.0;

        Particles.removeVesselVoids(
            data.particles,
            particleBuf,
            analysisImage,
            inputImage.width,
            inputImage.height,
            maxHoleLevel,
            minBoxness,
            minAreaLengthRatio
        );

        if (params.shouldRemoveSmallParticles)
            Particles.fillShapes(
                data.particles,
                particleBuf,
                analysisImage,
                inputImage.width,
                inputImage.height,
                params.removeSmallParticlesThreshold,
                true
            );

        if (params.shouldFillHoles)
            Particles.fillShapes(
                data.particles,
                particleBuf,
                analysisImage,
                inputImage.width,
                inputImage.height,
                params.fillHolesValue,
                false
            );

        IntBufferPool.release(particleBuf);

        data.vesselPixelArea = Misc.countForegroundPixels(analysisImage, inputImage.width, inputImage.height);

        if (params.shouldComputeLacunarity)
            Lacunarity2.computeLacunarity(data.lacunarity, analysisImage, inputImage.width, inputImage.height, 10, 10, 5);

        data.convexHullArea = ConvexHull.findConvexHull(data.convexHull, analysisImage, inputImage.width, inputImage.height);

        byte[] skeletonImage = ByteBufferPool.acquireAsIs(inputImage.width * inputImage.height);
        int maxSkelIterations = params.shouldCapSkelIterations ? params.maxSkelIterations : 0;

        if (params.shouldUseFastSkeletonizer) {
            byte[] zha84ScratchImage = ByteBufferPool.acquireAsIs(inputImage.width * inputImage.height);
            Zha84.skeletonize(skeletonImage, zha84ScratchImage, analysisImage, inputImage.width, inputImage.height, maxSkelIterations);
            ByteBufferPool.release(zha84ScratchImage);
        }
        else {
            int bitDepth = 8;
            Object[] layers = new Object[1];
            layers[0] = analysisImage;

            Lee94.skeletonize(
                skeletonImage,
                sliceRunner,
                MAX_WORKERS,
                layers,
                inputImage.width,
                inputImage.height,
                bitDepth,
                maxSkelIterations
            );
        }

        if (maxSkelIterations > 0) {
            byte[] filteredSkeletonImage = ByteBufferPool.acquireAsIs(inputImage.width * inputImage.height);
            Filters.removeInsulatedBrightPixels(filteredSkeletonImage, skeletonImage, inputImage.width, inputImage.height, 1);
            ByteBufferPool.release(skeletonImage);
            skeletonImage = filteredSkeletonImage;
        }

        AnalyzeSkeleton2.analyze(
            data.skelResult,
            skeletonImage,
            inputImage.width,
            inputImage.height,
            1
        );

        ByteBufferPool.release(skeletonImage);

        double averageVesselDiameter = 0.0;
        double linearScalingFactor = params.shouldApplyLinearScale ? params.linearScalingFactor : 1.0;

        if (params.shouldComputeThickness)
        {
            int[] thicknessScratch = IntBufferPool.acquireAsIs(inputImage.width * inputImage.height);
            averageVesselDiameter = linearScalingFactor * VesselThickness.computeMedianVesselThickness(
                sliceRunner,
                MAX_WORKERS,
                data.skelResult.slabList.buf,
                data.skelResult.slabList.size,
                3,
                thicknessScratch,
                analysisImage,
                inputImage.width,
                inputImage.height
            );
            IntBufferPool.release(thicknessScratch);
        }

        double areaScalingFactor = linearScalingFactor * linearScalingFactor;

        Stats stats = new Stats();
        stats.imageFileName = inFile.getName();
        stats.imageAbsolutePath = inFile.getAbsolutePath();
        stats.imageWidth = inputImage.width;
        stats.imageHeight = inputImage.height;
        stats.thresholdLow = params.thresholdLow;
        stats.thresholdHigh = params.thresholdHigh;
        stats.sigmas = params.sigmas;
        stats.removeSmallParticlesThreshold = params.shouldRemoveSmallParticles ? params.removeSmallParticlesThreshold : 0.0;
        stats.fillHolesValue = params.shouldFillHoles ? params.fillHolesValue : 0.0;
        stats.brightShapeThresholdFactor = maxHoleLevel;
        stats.minBoxness = minBoxness;
        stats.minAreaLengthRatio = minAreaLengthRatio;
        stats.usedFastSkeletonizer = params.shouldUseFastSkeletonizer;
        stats.maxSkelIterations = maxSkelIterations;
        stats.linearScalingFactor = linearScalingFactor;
        //stats.allantoisPixelsArea = data.convexHullArea;
        stats.allantoisMMArea = data.convexHullArea * areaScalingFactor;
        stats.totalNJunctions = data.skelResult.isolatedJunctions.size;
        //stats.junctionsPerArea = (double)data.skelResult.isolatedJunctions.size / data.convexHullArea;
        stats.junctionsPerScaledArea = (double)data.skelResult.isolatedJunctions.size / stats.allantoisMMArea;
        stats.vesselMMArea = (double)data.vesselPixelArea * areaScalingFactor;
        stats.vesselPercentageArea = stats.vesselMMArea * 100.0 / stats.allantoisMMArea;
        stats.averageVesselDiameter = averageVesselDiameter;
        stats.ELacunarityCurve = data.lacunarity.elCurve;
        stats.ELacunarityMedial = data.lacunarity.elMedial;
        stats.FLacunarityCurve = data.lacunarity.flCurve;
        stats.FLacunarityMedial = data.lacunarity.flMedial;
        stats.meanEl = data.lacunarity.elMean;
        stats.meanFl = data.lacunarity.flMean;

        //double[] branchLengths = data.skelResult.getAverageBranchLength();
        //int[] branchNumbers = data.skelResult.getBranches();

        double totalLength = 0.0;
        int nTrees = data.skelResult.treeCount;
        for (int i = 0; i < nTrees; i++)
            totalLength += (double)data.skelResult.totalBranchLengths.buf[i];

        double averageLength = 0.0;
        if (nTrees > 0)
            averageLength = totalLength * linearScalingFactor / (double)nTrees;

        stats.totalLength = totalLength * linearScalingFactor;
        stats.averageBranchLength = averageLength;
        stats.totalNEndPoints = data.skelResult.endPoints.size / 3;

        return stats;
    }

    public static void drawOverlay(
        AnalyzerParameters params,
        IntVector convexHull,
        AnalyzeSkeleton2.Result skelResult,
        byte[] analysisImage,
        int[] outputImage,
        int[] inputImage,
        float[] luminanceImage,
        int width,
        int height,
        int brightestChannel
    ) {
        int area = width * height;

        if (luminanceImage != null) {
            for (int i = 0; i < area; i++) {
                int v = Math.min(Math.max((int)luminanceImage[i], 0), 255);
                outputImage[i] = 0xff000000 | (v << 16) | (v << 8) | v;
            }
        }
        else {
            if (params.shouldIsolateBrightestChannelInOutput) {
                if (params.shouldExpandOutputToGrayScale) {
                    int shift = 8 * (2 - brightestChannel);
                    for (int i = 0; i < area; i++) {
                        int lum = (inputImage[i] >>> shift) & 0xff;
                        outputImage[i] = 0xff000000 | (lum << 16) | (lum << 8) | lum;
                    }
                }
                else {
                    int mask = 0xff << (8 * (2 - brightestChannel));
                    for (int i = 0; i < area; i++)
                        outputImage[i] = 0xff000000 | (inputImage[i] & mask);
                }
            }
            else {
                for (int i = 0; i < area; i++)
                    outputImage[i] = inputImage[i] | 0xff000000;
            }
        }

        if (params.shouldDrawOutline)
        {
            int[] outlineScratch1 = IntBufferPool.acquireAsIs(area);
            int[] outlineScratch2 = IntBufferPool.acquireAsIs(area);

            Outline.drawOutline(
                outputImage,
                outlineScratch1,
                outlineScratch2,
                params.outlineColor.getARGB(),
                params.outlineSize,
                analysisImage,
                width,
                height
            );

            IntBufferPool.release(outlineScratch1);
            IntBufferPool.release(outlineScratch2);
        }

        if (params.shouldDrawConvexHull)
        {
            Canvas.drawLines(
                outputImage,
                width,
                height,
                null,
                convexHull.buf,
                convexHull.size,
                2,
                params.convexHullColor.getARGB(),
                params.convexHullSize
            );
        }

        if (params.shouldDrawSkeleton)
        {
            Canvas.drawCircles(
                outputImage,
                width,
                height,
                null,
                skelResult.slabList.buf,
                skelResult.slabList.size,
                3,
                params.skeletonColor.getARGB(),
                params.skeletonSize
            );
            Canvas.drawCircles(
                outputImage,
                width,
                height,
                skelResult.removedJunctions.buf,
                skelResult.junctionVoxels.buf,
                skelResult.removedJunctions.size,
                3,
                params.skeletonColor.getARGB(),
                params.skeletonSize
            );
        }

        if (params.shouldDrawBranchPoints)
        {
            Canvas.drawCircles(
                outputImage,
                width,
                height,
                skelResult.isolatedJunctions.buf,
                skelResult.junctionVoxels.buf,
                skelResult.isolatedJunctions.size,
                3,
                params.branchingPointsColor.getARGB(),
                params.branchingPointsSize
            );
        }
    }

    public static SpreadsheetWriter createWriterWithNewSheet(
        ArrayList<XlsxReader.SheetCells> originalSheets,
        File folder,
        String sheetName
    ) throws IOException
    {
        SpreadsheetWriter writer = new SpreadsheetWriter(folder, sheetName);
        writer.addSheets(originalSheets);

        writer.writeRow(
            "Image Name",
            "Date",
            "Time",
            "Image Location",
            "Width",
            "Height",
            "Low Threshold",
            "High Threshold",
            "Vessel Thickness",
            "Small Particles",
            "Fill Holes",
            "Max Hole Level",
            "Min Boxness",
            "Min Area Length Ratio",
            "Skeletonizer",
            "Max Skeleton Steps",
            "Scaling Factor",
            "Explant Area",
            "Vessels Area",
            "Vessels Percentage Area",
            "Total Number of Junctions",
            "Junctions Density",
            "Total Vessels Length",
            "Average Vessels Length",
            "Total Number of End Points",
            "Average Vessel Diameter",
            "Medial E Lacunarity",
            "Mean E Lacunarity",
            "E Lacunarity Curve",
            "Medial F Lacunarity",
            "Mean F Lacunarity",
            "F Lacunarity Curve"
        );

        return writer;
    }

    public static void writeResultToSheet(SpreadsheetWriter sw, Stats stats) throws IOException
    {
        Date today = new Date();
        String dateOut = sw.dateFormatter.format(today);
        String timeOut = sw.timeFormatter.format(today);

        sw.writeRow(
            stats.imageFileName,
            dateOut,
            timeOut,
            stats.imageAbsolutePath,
            stats.imageWidth,
            stats.imageHeight,
            stats.thresholdLow,
            stats.thresholdHigh,
            Misc.formatDoubleArray(stats.sigmas, ""),
            stats.removeSmallParticlesThreshold,
            stats.fillHolesValue,
            stats.brightShapeThresholdFactor,
            stats.minBoxness,
            stats.minAreaLengthRatio,
            stats.usedFastSkeletonizer ? "Fast" : "Thorough",
            stats.maxSkelIterations,
            stats.linearScalingFactor,
            stats.allantoisMMArea,
            stats.vesselMMArea,
            stats.vesselPercentageArea,
            stats.totalNJunctions,
            stats.junctionsPerScaledArea,
            stats.totalLength,
            stats.averageBranchLength,
            stats.totalNEndPoints,
            stats.averageVesselDiameter,
            stats.ELacunarityMedial,
            stats.meanEl,
            stats.ELacunarityCurve,
            stats.FLacunarityMedial,
            stats.meanFl,
            stats.FLacunarityCurve
        );
    }

    public static void writeError(SpreadsheetWriter sheet, Throwable exception, File inFile) throws IOException
    {
        Throwable cause = exception.getCause();
        cause = cause == null ? exception : cause;

        String imageFileName = inFile.getName();
        String imageAbsolutePath = inFile.getAbsolutePath();

        Date today = new Date();
        String dateOut = sheet.dateFormatter.format(today);
        String timeOut = sheet.timeFormatter.format(today);

        sheet.writeRow(
            imageFileName,
            dateOut,
            timeOut,
            imageAbsolutePath,
            "Error",
            cause.getClass().getName(),
            exception.getMessage()
        );
    }
}
