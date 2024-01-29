package Batch;

import java.awt.Color;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import AngioTool.AngioToolMain;
import AngioTool.RGBStackSplitter;
import AngioTool.PolygonPlus;
import AnalyzeSkeleton.AnalyzeSkeleton;
import AnalyzeSkeleton.Edge;
import AnalyzeSkeleton.Graph;
import AnalyzeSkeleton.Point;
import AnalyzeSkeleton.SkeletonResult;
import features.TubenessProcessor;
import Lacunarity.Lacunarity;
import Utils.Utils;
import vesselThickness.EDT_S1D;

public class Analyzer
{
    static class Stats
    {
        public String imageFileName;
        public String imageAbsolutePath;
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
        public double ELacunarity;
        public double ELacunaritySlope;
        public double FLacuanrity;
        public double FLacunaritySlope;
        public double meanFl;
        public double meanEl;

        public Exception exception;
    }

    public static class Scratch
    {
        public ArrayList<Point> al2;
        public Overlay allantoisOverlay;
        public PolygonPlus convexHull;
        public double convexHullArea;
        public PolygonRoi convexHullRoi;
        public ArrayList<Double> currentSigmas;
        public Graph[] graphs;
        public TubenessProcessor tubenessProcessor;
        public ImagePlus imageCopy;
        public ImagePlus imageResult;
        public ImagePlus imageThickness;
        public ImagePlus imageThresholded;
        public ImagePlus imageTubeness;
        public ImagePlus iplus;
        public ImagePlus iplusTemp;
        public ImagePlus iplusSkeleton;
        public ImageProcessor ipOriginal;
        public ImageProcessor ipThresholded;
        public ImageProcessor ipSkeleton;
        public ImageProcessor tempProcessor1;
        public ImageProcessor tempProcessor2;
        public ImageProcessor tempProcessor3;
        public ArrayList<Roi> junctionsRoi;
        public Lacunarity lacunarity;
        public Roi outlineRoi;
        public ArrayList<Point> removedJunctions;
        //public ArrayList<AngioToolGUI.sigmaImages> sI;
        public ArrayList<Roi> skeletonRoi;
        public ImageProcessor tubenessIp;
        public long vesselPixelArea;

        // Recycling resources
        public SkeletonResult2 skelResult;
        public byte[] skeletonImagePlanes;
        public byte[] zha84ScratchImage;
        public Lee94.Scratch lee94Scratch;

        public void reset()
        {
            exception = null;
            allantoisOverlay = null;
            convexHull = null;
            convexHullRoi = null;
            currentSigmas = null;
            graphs = null;
            tubenessProcessor = null;
            imageCopy = null;
            imageResult = null;
            imageThickness = null;
            imageThresholded = null;
            imageTubeness = null;
            iplus = null;
            iplusTemp = null;
            iplusSkeleton = null;
            ipOriginal = null;
            ipThresholded = null;
            ipSkeleton = null;
            tempProcessor1 = null;
            tempProcessor2 = null;
            tempProcessor3 = null;
            skeleton = null;
            junctionsRoi = null;
            lacunarity = null;
            outlineRoi = null;
            removedJunctions = null;
            //skelResult = null;
            skeletonRoi = null;
            tubenessIp = null;
            //stats.sigmas = null;
            //stats = null;

            System.gc();
        }

        public void close()
        {
            if (skelResult != null)
                skelResult.reset(0);

            skeletonImagePlanes = BufferPool.bytePool.release(skeletonImagePlanes);
            zha84ScratchImage = BufferPool.bytePool.release(zha84ScratchImage);
            lee94Scratch.reset(0);

            reset();
        }
    }

    static int calculateUpdateCountPerImage(AnalyzerParameters params) {
        int count = 8;
        if (params.shouldDrawOutline)
            count++;
        if (params.shouldComputeLacunarity)
            count++;
        if (params.shouldDrawConvexHull)
            count++;
        if (params.shouldComputeThickness)
            count++;
        if (params.shouldDrawSkeleton)
            count++;
        if (params.shouldDrawBranchPoints)
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
            Utils.showExceptionInDialogBox(ex);
            return;
        }

        double linearScalingFactor = params.shouldApplyLinearScale ? params.linearScalingFactor : 1.0;

        uiToken.startProgressBars(inputs.size(), calculateUpdateCountPerImage(params));
        boolean startedAnyImages = false;

        Scratch data = new Scratch();

        for (File inFile : inputs) {
            if (uiToken.isClosed.get())
                return;

            ImagePlus image = null;
            try { image = IJ.openImage(inFile.getAbsolutePath()); }
            catch (Throwable ignored) {}

            if (image == null || image.getWidth() == 0 || image.getHeight() == 0) {
                uiToken.notifyImageWasInvalid();
                continue;
            }

            uiToken.onStartImage(inFile.getAbsolutePath());
            startedAnyImages = true;

            Stats result = null;
            Throwable exception = null;
            boolean analyzeSucceeded = false;
            try {
                result = analyze(data, inFile, image, params, linearScalingFactor, uiToken);
                analyzeSucceeded = true;
                uiToken.updateImageProgress("Saving image stats to Excel");
                writeResultToSheet(sheet, result);
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

                        IJ.saveAs(data.imageResult.flatten(), format, basePath + " data." + format);
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
                Utils.showExceptionInDialogBox(exception);

            data.cleanup();

            uiToken.onImageDone(exception);
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
        if (outFolderPath.exists()) {
            if (outFolderPath.isFile())
                outFolderPath.delete();
        }
        else {
            outFolderPath.mkdirs();
        }
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

                String baseName = name.substring(0, extIdx);
                if (baseName.endsWith("result") || baseName.endsWith("tubeness") || baseName.endsWith("filtered") || baseName.endsWith("overlay"))
                    continue;

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
        ImagePlus inputImage,
        AnalyzerParameters params,
        double linearScalingFactor,
        BatchAnalysisUi uiToken
    ) {
        uiToken.updateImageProgress("Loading image...");

        data.allantoisOverlay = new Overlay();
        data.imageThresholded = new ImagePlus();
        data.imageResult = inputImage;

        if (data.imageResult.getType() == 4)
            data.imageResult = RGBStackSplitter.split(data.imageResult, "green");

        if (params.shouldResizeImage) {
            ImageProcessor resized = data.imageResult.getProcessor().resize((int)((double)data.imageResult.getWidth() / params.resizingFactor));
            data.imageResult.setProcessor(resized);
        }

        data.ipOriginal = data.imageResult.getProcessor().convertToByte(false);

        uiToken.updateImageProgress("Calculating tubeness...");

        //for (Integer s : params.sigmasMarks)

        double[] sigmasDouble = new double[] {(double)params.sigmas[0]};
        data.tubenessProcessor = new TubenessProcessor(100, sigmasDouble);
        data.imageCopy = new ImagePlus("imageTubeness", data.ipOriginal);
        data.imageTubeness = data.tubenessProcessor.generateImage(data.imageCopy);
        data.tubenessIp = data.imageTubeness.getProcessor();
        //data.sI.add(new AngioToolGUI.sigmaImages(sigma, data.imageTubeness.getProcessor()));
        /*
        this.sI.add(new AngioToolGUI.sigmaImages(sigmas[0], this.tubenessIp));
        this.sigmasMarkSlider.addMark((int)sigmas[0]);
        this.allSigmas.add(sigmas[0]);
        this.currentSigmas.add(sigmas[0]);
        */
        data.ipThresholded = data.tubenessIp.duplicate().convertToByte(false);
        data.imageThresholded.setProcessor(data.ipThresholded);
        Utils.thresholdFlexible(data.ipThresholded, params.thresholdLow, params.thresholdHigh);
        data.ipThresholded.setThreshold(255.0, 255.0, 2);
        /*
        this.sigmaIsChanged = false;
        this.fillHolesIsChanged = false;
        this.smallParticlesIsChanged = false;
        */
        // load ImagePlus and ImageProcessor for this image

        /*
        boolean sigmaIsChanged = false;
        if (sigmaIsChanged) {
            ImageProcessor ip = new ByteProcessor(data.tubenessIp.getWidth(), data.tubenessIp.getHeight());

            for(int i = 0; i < data.currentSigmas.size(); ++i) {
                double s = data.currentSigmas.get(i);

                for(int si = 0; si < data.sI.size(); ++si) {
                    AngioToolGUI.sigmaImages siTemp = data.sI.get(si);
                    if (siTemp.sigma == s) {
                        ImageProcessor tempIp = siTemp.tubenessImage.duplicate();
                        tempIp.copyBits(ip, 0, 0, 13);
                        ip = tempIp;
                        break;
                    }
                }
            }

            data.tubenessIp = ip.duplicate();
        }
        */

        uiToken.updateImageProgress("Filtering image...");

        data.tempProcessor1 = data.tubenessIp.duplicate().convertToByte(true);
        Utils.thresholdFlexible(data.tempProcessor1, params.thresholdLow, params.thresholdHigh);
        data.imageThresholded.setProcessor(data.tempProcessor1);
        data.tempProcessor1.setThreshold(255.0, 255.0, 2);

        int iterations = 2;

        for (int i = 0; i < iterations; ++i)
            data.imageThresholded.getProcessor().erode();

        for (int i = 0; i < iterations; ++i)
            data.imageThresholded.getProcessor().dilate();

        if (params.shouldRemoveSmallParticles)
            Utils.fillHoles(data.imageThresholded, 0.0, params.removeSmallParticlesThreshold, 0.0, 1.0, 0);

        if (params.shouldFillHoles) {
            data.imageThresholded.killRoi();
            data.tempProcessor2 = data.imageThresholded.getProcessor();
            data.tempProcessor2.invert();
            Utils.fillHoles(data.imageThresholded, 0.0, params.fillHolesValue, 0.0, 1.0, 0);
            data.tempProcessor2.invert();
        }

        if (params.shouldDrawOutline) {
            uiToken.updateImageProgress("Drawing outltine...");

            data.iplus = new ImagePlus("tubenessIp", data.imageThresholded.getProcessor());
            data.outlineRoi = Utils.thresholdToSelection(data.iplus);
            data.outlineRoi.setStrokeColor(params.outlineColor.toColor());
            data.outlineRoi.setStrokeWidth(params.outlineSize);
            data.allantoisOverlay.add(data.outlineRoi);
        }

        data.vesselPixelArea = Utils.thresholdedPixelArea(data.imageThresholded.getProcessor());

        if (params.shouldComputeLacunarity) {
            uiToken.updateImageProgress("Computing lacunarity...");
            data.tempProcessor3 = data.imageThresholded.getProcessor().duplicate();
            data.iplusTemp = new ImagePlus("iplusTemp", data.tempProcessor3);
            data.lacunarity = new Lacunarity(data.iplusTemp, 10, 10, 5, true);
        }

        uiToken.updateImageProgress("Computing convex hull...");

        data.convexHull = Utils.computeConvexHull(data.imageThresholded.getProcessor());
        data.convexHullArea = data.convexHull.area();
        data.convexHullRoi = new PolygonRoi(data.convexHull.polygon(), 2);

        if (params.shouldDrawConvexHull) {
            uiToken.updateImageProgress("Drawing convex hull...");
            data.convexHullRoi.setStrokeColor(params.convexHullColor.toColor());
            data.convexHullRoi.setStrokeWidth(params.convexHullSize);
            data.allantoisOverlay.add(data.convexHullRoi);
        }

        uiToken.updateImageProgress("Computing skeleton...");

        data.iplusSkeleton = data.imageThresholded.duplicate();
        data.iplusSkeleton.setTitle("iplusSkeleton");

        int skelWidth = data.iplusSkeleton.getWidth();
        int skelHeight = data.iplusSkeleton.getHeight();
        int skelBreadth = data.iplusSkeleton.getStackSize();

        //BufferPool.bytePool.release(data.skeletonImagePlanes);
        data.skeletonImagePlanes = BufferPool.bytePool.acquire(skelWidth * skelHeight);

        if (params.shouldUseFastSkeletonizer) {
            data.zha84ScratchImage = BufferPool.bytePool.acquire(skelWidth * skelHeight);
            Zha84.skeletonize(data.skeletonImagePlanes, data.zha84ScratchImage, data.iplusSkeleton);
            data.zha84ScratchImage = BufferPool.bytePool.release(data.zha84ScratchImage);
        }
        else {
            Lee94.skeletonize(
                data.lee94Scratch,
                AngioToolMain.threadPool,
                AngioToolMain.MAX_WORKERS,
                data.skeletonImagePlanes,
                data.iplusSkeleton
            );
        }

        /*
        data.skeleton = new AnalyzeSkeleton();
        data.skeleton.setup("", data.iplusSkeleton);
        data.skelResult = data.skeleton.run(0, false, false, data.iplusSkeleton, false, false);
        data.graphs = data.skeleton.getGraphs();
        */
        AnalyzeSkeleton2.analyze(
            data.skelResult,
            calibration,
            data.skeletonImagePlanes,
            skelWidth,
            skelHeight,
            skelBreadth
        );

        skeletonImagePlanes = BufferPool.bytePool.release(skeletonImagePlanes);

        if (params.shouldComputeThickness) {
            uiToken.updateImageProgress("Computing thickness...");

            EDT_S1D ed = new EDT_S1D(AngioToolMain.threadPool);
            ed.setup(null, data.imageThresholded);
            ed.run(data.imageThresholded.getProcessor());
            data.imageThickness = ed.getImageResult();

            double thickness = Utils.computeMedianThickness(data.graphs, data.imageThickness);
            data.stats.averageVesselDiameter = thickness * linearScalingFactor;
        }

        uiToken.updateImageProgress("Generating skeleton points...");

        data.skeletonRoi = new ArrayList();

        for(int g = 0; g < data.graphs.length; ++g) {
            ArrayList<Edge> edges = data.graphs[g].getEdges();

            for(int e = 0; e < edges.size(); ++e) {
                Edge edge = edges.get(e);
                ArrayList<Point> points = edge.getSlabs();

                for (Point p : points) {
                    OvalRoi or = new OvalRoi(
                        p.x - params.skeletonSize / 2,
                        p.y - params.skeletonSize / 2,
                        params.skeletonSize,
                        params.skeletonSize
                    );
                    data.skeletonRoi.add(or);
                }
            }
        }

        uiToken.updateImageProgress("Computing junctions...");

        data.al2 = data.skelResult.getListOfJunctionVoxels();
        data.removedJunctions = Utils.computeActualJunctions(data.al2);
        data.junctionsRoi = new ArrayList<Roi>();

        for (Point p : data.al2) {
            OvalRoi or = new OvalRoi(
                p.x - params.branchingPointsSize / 2,
                p.y - params.branchingPointsSize / 2,
                params.branchingPointsSize,
                params.branchingPointsSize
            );
            data.junctionsRoi.add(or);
        }

        if (params.shouldDrawSkeleton) {
            uiToken.updateImageProgress("Drawing skeleton...");
            Color skelColor = params.skeletonColor.toColor();

            for(int i = 0; i < data.skeletonRoi.size(); ++i) {
                Roi r = (Roi)data.skeletonRoi.get(i);
                r.setStrokeWidth((float)params.skeletonSize);
                r.setStrokeColor(skelColor);
                data.allantoisOverlay.add(r);
            }

            for(int i = 0; i < data.removedJunctions.size(); ++i) {
                Point p = data.removedJunctions.get(i);
                OvalRoi r = new OvalRoi(p.x, p.y, 1, 1);
                r.setStrokeWidth((float)params.skeletonSize);
                r.setStrokeColor(skelColor);
                data.allantoisOverlay.add(r);
            }
        }

        if (params.shouldDrawBranchPoints) {
            uiToken.updateImageProgress("Drawing branch points...");
            Color branchColor = params.branchingPointsColor.toColor();

            for(int i = 0; i < data.junctionsRoi.size(); ++i) {
                Roi r = (Roi)data.junctionsRoi.get(i);
                r.setStrokeWidth(params.branchingPointsSize);
                r.setStrokeColor(branchColor);
                data.allantoisOverlay.add(r);
            }
        }

        data.imageResult.setOverlay(data.allantoisOverlay);

        //updateOverlay()
        //populateResults()

        double areaScalingFactor = linearScalingFactor * linearScalingFactor;

        Stats stats = new Stats();
        stats.imageFileName = inFile.getName();
        stats.imageAbsolutePath = inFile.getAbsolutePath();
        stats.thresholdLow = params.thresholdLow;
        stats.thresholdHigh = params.thresholdHigh;
        stats.sigmas = params.sigmas;
        stats.removeSmallParticlesThreshold = params.removeSmallParticlesThreshold;
        stats.fillHolesValue = (int)params.fillHolesValue;
        stats.linearScalingFactor = linearScalingFactor;
        //stats.allantoisPixelsArea = data.convexHullArea;
        stats.allantoisMMArea = data.convexHullArea * areaScalingFactor;
        stats.totalNJunctions = data.al2.size();
        //stats.junctionsPerArea = (double)data.al2.size() / data.convexHullArea;
        stats.junctionsPerScaledArea = (double)data.al2.size() / data.stats.allantoisMMArea;
        stats.vesselMMArea = (double)data.vesselPixelArea * areaScalingFactor;
        stats.vesselPercentageArea = data.stats.vesselMMArea * 100.0 / data.stats.allantoisMMArea;
        stats.ELacunaritySlope = data.lacunarity.getEl3Slope();
        stats.ELacunarity = data.lacunarity.getMedialELacunarity();
        stats.FLacunaritySlope = data.lacunarity.getFl3Slope();
        stats.FLacuanrity = data.lacunarity.getMedialFLacunarity();
        stats.meanEl = data.lacunarity.getMeanEl();
        stats.meanFl = data.lacunarity.getMeanFl();

        double[] branchLengths = data.skelResult.getAverageBranchLength();
        int[] branchNumbers = data.skelResult.getBranches();
        double totalLength = 0.0;
        double averageLength = 0.0;

        for (int i = 0; i < branchNumbers.length; ++i) {
            totalLength += (double)branchNumbers[i] * branchLengths[i];
        }

        stats.totalLength = totalLength * linearScalingFactor;
        stats.averageBranchLength = totalLength / (double)branchNumbers.length * linearScalingFactor;
        stats.totalNEndPoints = data.skelResult.getListOfEndPoints().size();

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
            "E Lacunarity",
            "E Lacunarity Slope",
            "F Lacunarity",
            "F Lacunarity Slope",
            "Mean F Lacunarity",
            "Mean E Lacunarity"
        );

        return writer;
    }

    static void writeResultToSheet(SpreadsheetWriter sw, Result.Stats stats) throws IOException
    {
        Date today = new Date();
        String dateOut = sw.dateFormatter.format(today);
        String timeOut = sw.timeFormatter.format(today);

        sw.writeRow(
            stats.imageFileName,
            dateOut,
            timeOut,
            stats.imageAbsolutePath,
            stats.thresholdLow,
            stats.thresholdHigh,
            Utils.formatDoubleArray(stats.sigmas),
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
            stats.ELacunarity,
            stats.ELacunaritySlope,
            stats.FLacuanrity,
            stats.FLacunaritySlope,
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
