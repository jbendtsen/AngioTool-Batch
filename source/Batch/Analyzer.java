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

public class Analyzer {
    public static class Result {
        static class Stats {
            public String imageFileName;
            public String imageAbsolutePath;
            public int thresholdLow;
            public int thresholdHigh;
            public int[] sigmas;
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
        }

        public Exception exception;

        public Stats stats = new Stats();

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
        public AnalyzeSkeleton skeleton;
        public ArrayList<Roi> junctionsRoi;
        public Lacunarity lacunarity;
        public Roi outlineRoi;
        public ArrayList<Point> removedJunctions;
        //public ArrayList<AngioToolGUI.sigmaImages> sI;
        public SkeletonResult skelResult;
        public ArrayList<Roi> skeletonRoi;
        public ImageProcessor tubenessIp;
        public long vesselPixelArea;

        public void cleanup()
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
            skelResult = null;
            skeletonRoi = null;
            tubenessIp = null;
            stats.sigmas = null;
            stats = null;

            System.gc();
        }
    }

    public static void doBatchAnalysis(
        AnalyzerParameters params,
        BatchAnalysisUi uiToken,
        ArrayList<XlsxReader.SheetCells> originalSheets
    ) {
        ArrayList<File> inputs = new ArrayList<>();
        for (String path : params.inputPaths) {
            enumerateImageFilesRecursively(inputs, new File(path), uiToken);
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

        uiToken.startProgressBars(inputs.size(), 120);
        boolean startedAnyImages = false;

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

            Result result = null;
            Throwable exception = null;
            boolean analyzeSucceeded = false;
            try {
                result = analyze(inFile, image, params, linearScalingFactor, uiToken);
                analyzeSucceeded = true;
                saveResult(writer, result, inFile, params, linearScalingFactor, uiToken);
            }
            catch (Throwable ex) {
                ex.printStackTrace();
                exception = ex;
            }

            if (exception == null) {
                uiToken.updateImageProgress(110, "Saving result image...");
     
                if (params.shouldSaveResultImages)
                    IJ.saveAs(result.imageResult.flatten(), "jpg", inFile.getAbsolutePath() + " result.jpg");
            }
            else if (!analyzeSucceeded) {
                try {
                    writeError(writer, exception, inFile);
                }
                catch (Exception ignored) {}
            }

            if (exception != null)
                Utils.showExceptionInDialogBox(exception);

            if (result != null)
                result.cleanup();

            uiToken.onImageDone(exception);
        }

        if (!startedAnyImages)
            uiToken.notifyNoImages();
        else
            uiToken.onFinished(writer.fileName);
    }

    static void enumerateImageFilesRecursively(ArrayList<File> images, File currentFolder, BatchAnalysisUi uiToken)
    {
        File[] list = currentFolder.listFiles();
        for (File f : list) {
            if (f.isDirectory()) {
                enumerateImageFilesRecursively(images, f, uiToken);
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

                images.add(f);
            }
        }
    }

    static Result analyze(File inFile, ImagePlus inputImage, AnalyzerParameters params, double linearScalingFactor, BatchAnalysisUi uiToken)
    {
        uiToken.updateImageProgress(0, "Loading image...");

        Result result = new Result();
        result.allantoisOverlay = new Overlay();
        result.imageThresholded = new ImagePlus();
        result.imageResult = inputImage;

        if (result.imageResult.getType() == 4)
            result.imageResult = RGBStackSplitter.split(result.imageResult, "green");

        if (params.shouldResizeImage) {
            ImageProcessor resized = result.imageResult.getProcessor().resize((int)((double)result.imageResult.getWidth() / params.resizingFactor));
            result.imageResult.setProcessor(resized);
        }

        result.ipOriginal = result.imageResult.getProcessor().convertToByte(false);

        uiToken.updateImageProgress(10, "Calculating tubeness...");

        //for (Integer s : params.sigmasMarks)

        double[] sigmasDouble = new double[] {(double)params.sigmas[0]};
        result.tubenessProcessor = new TubenessProcessor(100, sigmasDouble);
        result.imageCopy = new ImagePlus("imageTubeness", result.ipOriginal);
        result.imageTubeness = result.tubenessProcessor.generateImage(result.imageCopy);
        result.tubenessIp = result.imageTubeness.getProcessor();
        //result.sI.add(new AngioToolGUI.sigmaImages(sigma, result.imageTubeness.getProcessor()));
        /*
        this.sI.add(new AngioToolGUI.sigmaImages(sigmas[0], this.tubenessIp));
        this.sigmasMarkSlider.addMark((int)sigmas[0]);
        this.allSigmas.add(sigmas[0]);
        this.currentSigmas.add(sigmas[0]);
        */
        result.ipThresholded = result.tubenessIp.duplicate().convertToByte(false);
        result.imageThresholded.setProcessor(result.ipThresholded);
        Utils.thresholdFlexible(result.ipThresholded, params.thresholdLow, params.thresholdHigh);
        result.ipThresholded.setThreshold(255.0, 255.0, 2);
        /*
        this.sigmaIsChanged = false;
        this.fillHolesIsChanged = false;
        this.smallParticlesIsChanged = false;
        */
        // load ImagePlus and ImageProcessor for this image

        /*
        boolean sigmaIsChanged = false;
        if (sigmaIsChanged) {
            ImageProcessor ip = new ByteProcessor(result.tubenessIp.getWidth(), result.tubenessIp.getHeight());

            for(int i = 0; i < result.currentSigmas.size(); ++i) {
                double s = result.currentSigmas.get(i);

                for(int si = 0; si < result.sI.size(); ++si) {
                    AngioToolGUI.sigmaImages siTemp = result.sI.get(si);
                    if (siTemp.sigma == s) {
                        ImageProcessor tempIp = siTemp.tubenessImage.duplicate();
                        tempIp.copyBits(ip, 0, 0, 13);
                        ip = tempIp;
                        break;
                    }
                }
            }

            result.tubenessIp = ip.duplicate();
        }
        */

        uiToken.updateImageProgress(20, "Filtering image...");

        result.tempProcessor1 = result.tubenessIp.duplicate().convertToByte(true);
        Utils.thresholdFlexible(result.tempProcessor1, params.thresholdLow, params.thresholdHigh);
        result.imageThresholded.setProcessor(result.tempProcessor1);
        result.tempProcessor1.setThreshold(255.0, 255.0, 2);

        int iterations = 2;

        for (int i = 0; i < iterations; ++i)
            result.imageThresholded.getProcessor().erode();

        for (int i = 0; i < iterations; ++i)
            result.imageThresholded.getProcessor().dilate();

        if (params.shouldRemoveSmallParticles)
            Utils.fillHoles(result.imageThresholded, 0.0, params.removeSmallParticlesThreshold, 0.0, 1.0, 0);

        if (params.shouldFillHoles) {
            result.imageThresholded.killRoi();
            result.tempProcessor2 = result.imageThresholded.getProcessor();
            result.tempProcessor2.invert();
            Utils.fillHoles(result.imageThresholded, 0.0, params.fillHolesValue, 0.0, 1.0, 0);
            result.tempProcessor2.invert();
        }

        uiToken.updateImageProgress(30, "Drawing Allantois overlay...");

        result.iplus = new ImagePlus("tubenessIp", result.imageThresholded.getProcessor());
        result.outlineRoi = Utils.thresholdToSelection(result.iplus);
        result.outlineRoi.setStrokeWidth(params.outlineSize);
        result.allantoisOverlay.clear();
        result.allantoisOverlay.add(result.outlineRoi);
        result.allantoisOverlay.setStrokeColor(params.outlineColor);
        result.imageResult.setOverlay(result.allantoisOverlay);

        uiToken.updateImageProgress(40, "Computing lacunarity...");

        result.vesselPixelArea = Utils.thresholdedPixelArea(result.imageThresholded.getProcessor());
        if (params.shouldComputeLacunarity) {
            result.tempProcessor3 = result.imageThresholded.getProcessor().duplicate();
            result.iplusTemp = new ImagePlus("iplusTemp", result.tempProcessor3);
            result.lacunarity = new Lacunarity(result.iplusTemp, 10, 10, 5, true);
        }

        uiToken.updateImageProgress(50, "Computing convex hull...");

        result.convexHull = Utils.computeConvexHull(result.imageThresholded.getProcessor());
        result.convexHullArea = result.convexHull.area();
        result.convexHullRoi = new PolygonRoi(result.convexHull.polygon(), 2);

        uiToken.updateImageProgress(60, "Computing skeleton...");

        // TODO: params.useFastSkeletonizer ? Zha84.skeletonizeZha84 : Lee94.skeletonizeLee94

        result.ipThresholded = Zha84.skeletonizeZha84(result.imageThresholded.getProcessor());
        result.ipSkeleton = result.ipThresholded.duplicate();
        result.iplusSkeleton = new ImagePlus("iplusSkeleton", result.ipSkeleton);
        result.skeleton = new AnalyzeSkeleton();
        result.skeleton.setup("", result.iplusSkeleton);
        result.skelResult = result.skeleton.run(0, false, false, result.iplusSkeleton, false, false);
        result.graphs = result.skeleton.getGraphs();

        uiToken.updateImageProgress(70, "Computing thickness...");

        if (params.shouldComputeThickness) {
            EDT_S1D ed = new EDT_S1D(AngioToolMain.threadPool);
            ed.setup(null, result.imageThresholded);
            ed.run(result.imageThresholded.getProcessor());
            result.imageThickness = ed.getImageResult();

            double thickness = Utils.computeMedianThickness(result.graphs, result.imageThickness);
            result.stats.averageVesselDiameter = thickness * linearScalingFactor;
        }

        uiToken.updateImageProgress(80, "Generating skeleton points...");

        result.skeletonRoi = new ArrayList();

        for(int g = 0; g < result.graphs.length; ++g) {
            ArrayList<Edge> edges = result.graphs[g].getEdges();

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
                    result.skeletonRoi.add(or);
                }
            }
        }

        uiToken.updateImageProgress(90, "Computing junctions...");

        result.al2 = result.skelResult.getListOfJunctionVoxels();
        result.removedJunctions = Utils.computeActualJunctions(result.al2);
        result.junctionsRoi = new ArrayList<Roi>();

        for (Point p : result.al2) {
            OvalRoi or = new OvalRoi(
                p.x - params.branchingPointsSize / 2,
                p.y - params.branchingPointsSize / 2,
                params.branchingPointsSize,
                params.branchingPointsSize
            );
            result.junctionsRoi.add(or);
        }

        //updateOverlay()
        //populateResults()

        return result;
    }

    static void saveResult(
        SpreadsheetWriter sheet,
        Result result,
        File inFile,
        AnalyzerParameters params,
        double linearScalingFactor,
        BatchAnalysisUi uiToken
    ) {
        uiToken.updateImageProgress(100, "Saving image stats to Excel");

        double areaScalingFactor = linearScalingFactor * linearScalingFactor;

        result.stats.imageFileName = inFile.getName();
        result.stats.imageAbsolutePath = inFile.getAbsolutePath();
        result.stats.thresholdLow = params.thresholdLow;
        result.stats.thresholdHigh = params.thresholdHigh;
        result.stats.sigmas = params.sigmas;
        result.stats.removeSmallParticlesThreshold = params.removeSmallParticlesThreshold;
        result.stats.fillHolesValue = (int)params.fillHolesValue;
        result.stats.linearScalingFactor = linearScalingFactor;
        //result.stats.allantoisPixelsArea = result.convexHullArea;
        result.stats.allantoisMMArea = result.convexHullArea * areaScalingFactor;
        result.stats.totalNJunctions = result.al2.size();
        //result.stats.junctionsPerArea = (double)result.al2.size() / result.convexHullArea;
        result.stats.junctionsPerScaledArea = (double)result.al2.size() / result.stats.allantoisMMArea;
        result.stats.vesselMMArea = (double)result.vesselPixelArea * areaScalingFactor;
        result.stats.vesselPercentageArea = result.stats.vesselMMArea * 100.0 / result.stats.allantoisMMArea;
        result.stats.ELacunaritySlope = result.lacunarity.getEl3Slope();
        result.stats.ELacunarity = result.lacunarity.getMedialELacunarity();
        result.stats.FLacunaritySlope = result.lacunarity.getFl3Slope();
        result.stats.FLacuanrity = result.lacunarity.getMedialFLacunarity();
        result.stats.meanEl = result.lacunarity.getMeanEl();
        result.stats.meanFl = result.lacunarity.getMeanFl();

        double[] branchLengths = result.skelResult.getAverageBranchLength();
        int[] branchNumbers = result.skelResult.getBranches();
        double totalLength = 0.0;
        double averageLength = 0.0;

        for (int i = 0; i < branchNumbers.length; ++i) {
            totalLength += (double)branchNumbers[i] * branchLengths[i];
        }

        result.stats.totalLength = totalLength * linearScalingFactor;
        result.stats.averageBranchLength = totalLength / (double)branchNumbers.length * linearScalingFactor;
        result.stats.totalNEndPoints = result.skelResult.getListOfEndPoints().size();

        try {
            writeResultToSheet(sheet, result.stats);
        }
        catch (IOException ignored) {}
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
            "Elacunarity",
            "Elacunarity Slope",
            "Flacunarity",
            "Flacunarity Slope",
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

        StringBuilder sigmasSb = new StringBuilder();
        boolean empty = true;
        for (Integer s : stats.sigmas) {
            if (!empty) sigmasSb.append(",");
            sigmasSb.append(s);
            empty = false;
        }

        sw.writeRow(
            stats.imageFileName,
            dateOut,
            timeOut,
            stats.imageAbsolutePath,
            stats.thresholdLow,
            stats.thresholdHigh,
            sigmasSb.toString(),
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
