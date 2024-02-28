package Batch;

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
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;

public class Analyzer
{
    public static final int MAX_WORKERS = 24;

    public static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
        /* corePoolSize */ 2,
        /* maximumPoolSize */ MAX_WORKERS + 4,
        /* keepAliveTime */ 30,
        /* unit */ TimeUnit.SECONDS,
        /* workQueue */ new LinkedBlockingQueue<>()
    );

    static final HashSet<String> suffixesToSkip = BatchUtils.makeHashSetFromStringArray(new String[] {
        "result",
        "tubeness",
        "thresholded",
        "filtered",
        "overlay"
    });

    static class Stats
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
        public double FLacuanrityMedial;
        public double FLacunarityCurve;
        public double meanFl;
        public double meanEl;

        //public Exception exception;
    }

    public static class Scratch
    {
        static final int RESIZE_ROUNDING = 4 * 1024;

        public double convexHullArea;
        public long vesselPixelArea;

        // Vectors with sizes equal to image area
        FloatVector f1 = new FloatVector();
        FloatVector f2 = new FloatVector();
        FloatVector f3 = new FloatVector();
        FloatVector f4 = new FloatVector();
        IntVector i1 = new IntVector();
        IntVector i2 = new IntVector();
        IntVector iv1 = new IntVector();
        IntVector iv2 = new IntVector();
        IntVector iv3 = new IntVector();
        ByteVectorOutputStream b1 = new ByteVectorOutputStream();
        ByteVectorOutputStream b2 = new ByteVectorOutputStream();
        ByteVectorOutputStream b3 = new ByteVectorOutputStream();

        // Recycling resources
        public Tubeness.Scratch tubeness = new Tubeness.Scratch();
        public AnalyzeSkeleton2.Result skelResult = new AnalyzeSkeleton2.Result();
        public Particles.Scratch particleScratch = new Particles.Scratch();
        public Lee94.Scratch lee94Scratch = new Lee94.Scratch();
        public Lacunarity2.Statistics lacunarity = new Lacunarity2.Statistics();
        public IntVector convexHull = new IntVector();

        public void reallocate(AnalyzerParameters params, int width, int height, int breadth)
        {
            int area = width * height;
            breadth = Math.max(breadth, 1);

            int oldCap = f1.buf != null ? f1.buf.length : 0;

            f1.resize(area);
            f2.resize(area);
            f3.resize(area);
            f4.resize(area);

            i1.resize(area);
            i2.resize(area);

            iv1.resize(area * breadth);
            iv2.resize(area * breadth);
            iv3.resize(area * breadth);

            b1.resize(area);
            b2.resize(area);

            if (params.shouldUseFastSkeletonizer)
                b3.resize(area);
            else
                b3.buf = null;

            tubeness.useBuffers(f1.buf, f2.buf, f3.buf, f4.buf);
            skelResult.useBuffers(i1.buf, i2.buf, iv1.buf, iv2.buf, iv3.buf);

            int newCap = f1.buf.length;
            if (newCap > oldCap)
                System.gc();
        }

        public void close()
        {
            if (skelResult != null) {
                skelResult.reset(0);
                skelResult = null;
            }

            if (f1 != null) { f1.buf = null; } f1 = null;
            if (f2 != null) { f2.buf = null; } f2 = null;
            if (f3 != null) { f3.buf = null; } f3 = null;
            if (f4 != null) { f4.buf = null; } f4 = null;
            if (i1 != null) { i1.buf = null; } i1 = null;
            if (i2 != null) { i2.buf = null; } i2 = null;
            if (iv1 != null) { iv1.buf = null; } iv1 = null;
            if (iv2 != null) { iv2.buf = null; } iv2 = null;
            if (iv3 != null) { iv3.buf = null; } iv3 = null;
            if (b1 != null) { b1.buf = null; } b1 = null;
            if (b2 != null) { b2.buf = null; } b2 = null;
            if (b3 != null) { b3.buf = null; } b3 = null;

            tubeness = null;
            particleScratch = null;
            lee94Scratch = null;
            lacunarity = null;
            convexHull = null;
        }
    }

    static int determineUpdateCountPerImage(AnalyzerParameters params) {
        int count = 6;
        if (params.shouldDrawOutline && params.shouldSaveResultImages)
            count++;
        if (params.shouldComputeLacunarity)
            count++;
        if (params.shouldDrawConvexHull && params.shouldSaveResultImages)
            count++;
        if (params.shouldComputeThickness)
            count++;
        if (params.shouldDrawSkeleton && params.shouldSaveResultImages)
            count++;
        if (params.shouldDrawBranchPoints && params.shouldSaveResultImages)
            count++;
        if (params.shouldSaveResultImages)
            count++;
        return count;
    }

    public static void doBatchAnalysis(
        AnalyzerParameters params,
        BatchAnalysisUi uiToken,
        ArrayList<XlsxReader.SheetCells> originalSheets
    ) {
        uiToken.onEnumerationStart();

        ArrayList<File> inputs = new ArrayList<>();
        BidirectionalMap<String, String> outputPathMap = new BidirectionalMap<>();
        for (String path : params.inputImagePaths) {
            enumerateImageFilesRecursively(inputs, new File(path), outputPathMap, uiToken);
            if (uiToken.isClosed.get())
                return;
        }

        if (inputs.isEmpty()) {
            uiToken.notifyNoImages();
            return;
        }

        File excelPath = new File(params.excelFilePath);
        SpreadsheetWriter writer;
        try {
            writer = createWriterWithNewSheet(originalSheets, excelPath.getParentFile(), excelPath.getName());
        }
        catch (IOException ex) {
            BatchUtils.showExceptionInDialogBox(ex);
            return;
        }

        double linearScalingFactor = params.shouldApplyLinearScale ? params.linearScalingFactor : 1.0;
        double imageResizeFactor = params.shouldResizeImage ? params.resizingFactor : 1.0;

        uiToken.startProgressBars(inputs.size(), determineUpdateCountPerImage(params));
        boolean startedAnyImages = false;

        ISliceRunner sliceRunner = new ISliceRunner.Parallel(threadPool);

        Scratch data = new Scratch();
        Bitmap outputImage = params.shouldSaveResultImages ? new Bitmap() : null;
        Bitmap inputImage = new Bitmap();

        for (File inFile : inputs) {
            if (uiToken.isClosed.get())
                return;

            boolean useSingleChannelInOutputImage = true; // TODO: add option in params

            try {
                ImageUtils.openImage(
                    inputImage,
                    inFile.getAbsolutePath(),
                    imageResizeFactor,
                    outputImage,
                    useSingleChannelInOutputImage
                );
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }

            if (inputImage == null) {
                uiToken.notifyImageWasInvalid();
                continue;
            }

            uiToken.onStartImage(inFile.getAbsolutePath());
            startedAnyImages = true;

            Stats result = null;
            Throwable exception = null;
            boolean analyzeSucceeded = false;
            try {
                result = analyze(data, inFile, inputImage, outputImage, params, linearScalingFactor, sliceRunner, uiToken);
                analyzeSucceeded = true;
                uiToken.updateImageProgress("Saving image stats to Excel");
                writeResultToSheet(writer, result);
            }
            catch (Throwable ex) {
                ex.printStackTrace();
                exception = ex;
            }

            if (exception == null) {
                if (params.shouldSaveResultImages) {
                    try {
                        uiToken.updateImageProgress("Saving result image...");

                        String basePath = params.shouldSaveImagesToSpecificFolder ?
                            resolveOutputPath(params.resultImagesPath, outputPathMap, inFile) :
                            inFile.getAbsolutePath();
                        String format = resolveImageFormat(params.resultImageFormat);

                        // data.imageResult.flatten()
                        ImageUtils.saveImage(outputImage, 0, format, basePath + " result." + format);
                    }
                    catch (Throwable ex) {
                        exception = ex;
                    }
                }
            }
            else if (!analyzeSucceeded) {
                try {
                    writeError(writer, exception, inFile);
                }
                catch (Exception ignored) {}
            }

            if (exception != null)
                BatchUtils.showExceptionInDialogBox(exception);

            uiToken.onImageDone(exception);

            if (exception != null)
                break;
        }

        data.close();

        if (!startedAnyImages)
            uiToken.notifyNoImages();
        else
            uiToken.onFinished(writer);
    }

    static String resolveOutputPath(
        String resultImagesPath,
        BidirectionalMap<String, String> outputPathMap,
        File inFile
    ) {
        String outFolder = outputPathMap.getBack(inFile.getParent());
        File outFolderPath = new File(resultImagesPath, outFolder);
        boolean shouldCreateFolder = false;

        if (outFolderPath.exists()) {
            if (outFolderPath.isFile()) {
                outFolderPath.delete();
                shouldCreateFolder = true;
            }
        }
        else {
            shouldCreateFolder = true;
        }

        if (shouldCreateFolder)
            outFolderPath.mkdirs();

        return new File(outFolderPath, inFile.getName()).getAbsolutePath();
    }

    public static String resolveImageFormat(String str) throws Exception {
        if (str == null || str.length() == 0)
            throw new Exception("File extension was null");

        byte[] bytes = str.getBytes();
        int start = 0;
        for (int i = bytes.length - 1; i >= 0; i--) {
            byte c = bytes[i];
            c = (byte)(c >= 'A' && c <= 'Z' ? c + 0x20 : c);
            if (c == '.') {
                start = i + 1;
                break;
            }
            if (c >= 0x7f)
                throw new Exception("File extension contains non-ASCII characters");
            bytes[i] = c;
        }

        if (start >= bytes.length)
            throw new Exception("Invalid file extension: " + str);

        return new String(bytes, start, bytes.length - start);
    }

    static void enumerateImageFilesRecursively(
        ArrayList<File> images,
        File currentFolder,
        BidirectionalMap<String, String> outputPathMap,
        BatchAnalysisUi uiToken
    ) {
        String curAbsPath = currentFolder.getAbsolutePath();
        String outFolder = currentFolder.getName();
        outFolder = outFolder == null || outFolder.length() == 0 ? "root" : outFolder;

        boolean addedParent = false;
        int retries = 0;
        String retryStr = "";

        while (true) {
            int status = outputPathMap.maybeAdd(curAbsPath, outFolder + retryStr);
            if (status == BidirectionalMap.SUCCESS || (status & BidirectionalMap.FAIL_FRONT) != 0)
                break;

            if (!addedParent) {
                addedParent = true;
                File parent = currentFolder.getParentFile();
                if (parent != null) {
                    String pName = parent.getName();
                    if (pName != null && pName.length() > 0) {
                        outFolder += "_" + pName;
                        continue;
                    }
                }
            }

            retries++;
            retryStr = "_" + (retries + 1);
        }

        File[] list = currentFolder.listFiles();
        for (File f : list) {
            if (f.isDirectory()) {
                enumerateImageFilesRecursively(images, f, outputPathMap, uiToken);
                if (uiToken.isClosed.get())
                    return;
            }
            else if (f.isFile()) {
                String name = f.getName().toLowerCase();
                int extIdx = name.lastIndexOf('.');
                if (extIdx <= 0)
                    continue;

                int spaceIdx = name.lastIndexOf(' ');
                if (spaceIdx > 0 && spaceIdx < extIdx-1) {
                    String lastWordInFileName = name.substring(spaceIdx + 1, extIdx);
                    if (suffixesToSkip.contains(lastWordInFileName))
                        continue;
                }

                String ext = name.substring(extIdx).toLowerCase();
                if (ext.equals(".txt") || ext.equals(".zip") || ext.equals(".xls") || ext.equals(".xlsx"))
                    continue;

                images.add(f);
            }
        }
    }

    static Stats analyze(
        Scratch data,
        File inFile,
        Bitmap inputImage,
        Bitmap outputImage,
        AnalyzerParameters params,
        double linearScalingFactor,
        ISliceRunner sliceRunner,
        BatchAnalysisUi uiToken
    ) {
        data.reallocate(params, inputImage.width, inputImage.height, 1);

        int[] overlayImage = outputImage != null ? outputImage.getDefaultRgb() : null;
        byte[] analysisImage = data.b1.buf;

        uiToken.updateImageProgress("Calculating tubeness...");

        Tubeness.computeTubenessImage(
            data.tubeness,
            sliceRunner,
            MAX_WORKERS,
            analysisImage,
            inputImage.getDefaultChannel(),
            inputImage.width,
            inputImage.height,
            params.sigmas,
            params.sigmas.length
        );

        //ImageUtils.writePgm(analysisImage, inputImage.width, inputImage.height, inFile.getAbsolutePath() + " tubeness.pgm");

        uiToken.updateImageProgress("Filtering image...");

        BatchUtils.thresholdFlexible(
            analysisImage,
            inputImage.width,
            inputImage.height,
            params.thresholdLow,
            params.thresholdHigh
        );

        //ImageUtils.writePgm(analysisImage, inputImage.width, inputImage.height, inFile.getAbsolutePath() + " thresholded.pgm");

        byte[] skeletonImage = data.b2.buf;

        Filters.filterMax(skeletonImage, analysisImage, inputImage.width, inputImage.height); // erode
        Filters.filterMax(analysisImage, skeletonImage, inputImage.width, inputImage.height); // erode
        Filters.filterMin(skeletonImage, analysisImage, inputImage.width, inputImage.height); // dilate
        Filters.filterMin(analysisImage, skeletonImage, inputImage.width, inputImage.height); // dilate

        //ImageUtils.writePgm(analysisImage, inputImage.width, inputImage.height, "filtered.pgm");

        int[] particleBuf = data.i1.buf;
        uiToken.updateImageProgress("Identifying shapes...");

        Particles.computeShapes(
            data.particleScratch,
            particleBuf,
            analysisImage,
            inputImage.width,
            inputImage.height
        );

        Particles.removeVesselVoids(
            data.particleScratch,
            particleBuf,
            analysisImage,
            inputImage.width,
            inputImage.height
        );

        if (params.shouldRemoveSmallParticles)
            Particles.fillShapes(
                data.particleScratch,
                particleBuf,
                analysisImage,
                inputImage.width,
                inputImage.height,
                params.removeSmallParticlesThreshold,
                true
            );

        if (params.shouldFillHoles)
            Particles.fillShapes(
                data.particleScratch,
                particleBuf,
                analysisImage,
                inputImage.width,
                inputImage.height,
                params.fillHolesValue,
                false
            );

        //ImageUtils.writePgm(analysisImage, inputImage.width, inputImage.height, "clarified.pgm");

        if (params.shouldDrawOutline && params.shouldSaveResultImages)
        {
            uiToken.updateImageProgress("Drawing outline...");
            int[] outlineScratch1 = data.i2.buf;
            int[] outlineScratch2 = data.iv1.buf;

            // TODO: implement strokeWidth
            Outline.drawOutline(
                overlayImage,
                outlineScratch1,
                outlineScratch2,
                params.outlineColor.getARGB(),
                params.outlineSize,
                data.particleScratch.shapes,
                particleBuf,
                analysisImage,
                inputImage.width,
                inputImage.height
            );
        }

        data.vesselPixelArea = BatchUtils.countForegroundPixels(analysisImage, inputImage.width, inputImage.height);

        if (params.shouldComputeLacunarity)
        {
            uiToken.updateImageProgress("Computing lacunarity...");

            Lacunarity2.computeLacunarity(data.lacunarity, analysisImage, inputImage.width, inputImage.height, 10, 10, 5);
        }

        uiToken.updateImageProgress("Building convex hull...");

        data.convexHullArea = ConvexHull.findConvexHull(data.convexHull, analysisImage, inputImage.width, inputImage.height);

        if (params.shouldDrawConvexHull && params.shouldSaveResultImages)
        {
            uiToken.updateImageProgress("Drawing convex hull...");

            Canvas.drawLines(
                overlayImage,
                inputImage.width,
                inputImage.height,
                null,
                data.convexHull.buf,
                data.convexHull.size,
                2,
                params.convexHullColor.getARGB(),
                params.convexHullSize
            );
        }

        //ImageUtils.writePgm(analysisImage, inputImage.width, inputImage.height, inFile.getAbsolutePath() + " b4skel.pgm");

        uiToken.updateImageProgress("Computing skeleton...");

        if (params.shouldUseFastSkeletonizer) {
            byte[] zha84ScratchImage = data.b3.buf;
            Zha84.skeletonize(skeletonImage, zha84ScratchImage, analysisImage, inputImage.width, inputImage.height);
        }
        else {
            int bitDepth = 8;
            Object[] layers = new Object[1];
            layers[0] = analysisImage;

            Lee94.skeletonize(
                data.lee94Scratch,
                skeletonImage,
                sliceRunner,
                MAX_WORKERS,
                layers,
                inputImage.width,
                inputImage.height,
                bitDepth
            );
        }

        AnalyzeSkeleton2.analyze(
            data.skelResult,
            skeletonImage,
            inputImage.width,
            inputImage.height,
            1
        );

        double averageVesselDiameter = 0.0;

        if (params.shouldComputeThickness)
        {
            uiToken.updateImageProgress("Computing thickness...");

            int[] thicknessScratch = data.i2.buf;
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
        }

        //uiToken.updateImageProgress("Generating skeleton points...");
        //uiToken.updateImageProgress("Computing junctions...");

        if (params.shouldDrawSkeleton && params.shouldSaveResultImages)
        {
            uiToken.updateImageProgress("Drawing skeleton...");

            Canvas.drawCircles(
                overlayImage,
                inputImage.width,
                inputImage.height,
                null,
                data.skelResult.slabList.buf,
                data.skelResult.slabList.size,
                3,
                params.skeletonColor.getARGB(),
                params.skeletonSize
            );
            Canvas.drawCircles(
                overlayImage,
                inputImage.width,
                inputImage.height,
                data.skelResult.removedJunctions.buf,
                data.skelResult.junctionVoxels.buf,
                data.skelResult.removedJunctions.size,
                3,
                params.skeletonColor.getARGB(),
                params.skeletonSize
            );
        }

        if (params.shouldDrawBranchPoints && params.shouldSaveResultImages)
        {
            uiToken.updateImageProgress("Drawing branch points...");

            Canvas.drawCircles(
                overlayImage,
                inputImage.width,
                inputImage.height,
                data.skelResult.isolatedJunctions.buf,
                data.skelResult.junctionVoxels.buf,
                data.skelResult.isolatedJunctions.size,
                3,
                params.branchingPointsColor.getARGB(),
                params.branchingPointsSize
            );
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
        stats.removeSmallParticlesThreshold = params.shouldRemoveSmallParticles ? params.removeSmallParticlesThreshold : 0;
        stats.fillHolesValue = params.shouldFillHoles ? (int)params.fillHolesValue : 0;
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
        stats.FLacuanrityMedial = data.lacunarity.flMedial;
        stats.meanEl = data.lacunarity.elMean;
        stats.meanFl = data.lacunarity.flMean;

        //double[] branchLengths = data.skelResult.getAverageBranchLength();
        //int[] branchNumbers = data.skelResult.getBranches();

        double totalLength = 0.0;
        int nTrees = data.skelResult.treeCount;
        for (int i = 0; i < nTrees; i++)
            totalLength += (double)data.skelResult.totalBranchLengths[i];

        double averageLength = 0.0;
        if (nTrees > 0)
            averageLength = totalLength * linearScalingFactor / (double)nTrees;

        stats.totalLength = totalLength * linearScalingFactor;
        stats.averageBranchLength = averageLength;
        stats.totalNEndPoints = data.skelResult.endPoints.size / 3;

        return stats;
    }

    static SpreadsheetWriter createWriterWithNewSheet(
        ArrayList<XlsxReader.SheetCells> originalSheets,
        File folder,
        String sheetName
    ) throws IOException {
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
            "Scaling factor",
            "Explant area",
            "Vessels area",
            "Vessels percentage area",
            "Total Number of Junctions",
            "Junctions density",
            "Total Vessels Length",
            "Average Vessels Length",
            "Total Number of End Points",
            "Average Vessel Diameter",
            "Medial E Lacunarity",
            "E Lacunarity Curve",
            "Medial F Lacunarity",
            "F Lacunarity Curve",
            "Mean F Lacunarity",
            "Mean E Lacunarity"
        );

        return writer;
    }

    static void writeResultToSheet(SpreadsheetWriter sw, Stats stats) throws IOException
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
            BatchUtils.formatDoubleArray(stats.sigmas),
            stats.removeSmallParticlesThreshold,
            stats.fillHolesValue,
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
            stats.ELacunarityCurve,
            stats.FLacuanrityMedial,
            stats.FLacunarityCurve,
            stats.meanFl,
            stats.meanEl
        );
    }

    static void writeError(SpreadsheetWriter sheet, Throwable exception, File inFile) throws IOException
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
