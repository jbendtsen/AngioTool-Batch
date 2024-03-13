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
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

public class Analyzer
{
    public static final int MAX_WORKERS = 24;
    public static final long NANOS_PER_SEC = 1000000000L;

    public static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
        /* corePoolSize */ MAX_WORKERS,
        /* maximumPoolSize */ MAX_WORKERS,
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

    public interface IProgressToken
    {
        boolean isClosed();
        void notifyNoImages();
        void onEnumerationStart();
        void onBatchStatsKnown(int nImages);
        void notifyImageWasInvalid();
        void onImageDone(String absPath, Throwable error);
        void onFinished(SpreadsheetWriter sw);
    }

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

        ByteVectorOutputStream b2 = new ByteVectorOutputStream();
        ByteVectorOutputStream b3 = new ByteVectorOutputStream();

        ByteVectorOutputStream analysisImage = new ByteVectorOutputStream();

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

            analysisImage.resize(area);
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
            if (analysisImage != null) { analysisImage.buf = null; } analysisImage = null;
            if (b2 != null) { b2.buf = null; } b2 = null;
            if (b3 != null) { b3.buf = null; } b3 = null;

            tubeness = null;
            particleScratch = null;
            lee94Scratch = null;
            lacunarity = null;
            convexHull = null;
        }
    }

    static class AnalysisInput
    {
        File file;
        String baseOutputPath;

        AnalysisInput(File file, String baseOutputPath)
        {
            this.file = file;
            this.baseOutputPath = baseOutputPath;
        }
    }

    static class AnalysisResult
    {
        File file;
        Stats stats;
        Throwable error;

        AnalysisResult(File file, Stats stats, Throwable error)
        {
            this.file = file;
            this.stats = stats;
            this.error = error;
        }
    }

    static class AnalysisWorkers
    {
        class Worker implements Runnable
        {
            @Override
            public void run() {
                ISliceRunner sliceRunner = new ISliceRunner.Series();
                Scratch data = new Scratch();
                ArgbBuffer inputImage = new ArgbBuffer();

                double imageResizeFactor = params.shouldResizeImage ? params.resizingFactor : 1.0;

                AnalysisInput input;
                while ((input = inQueue.poll()) != null) {
                    if (cancellationToken.get())
                        break;

                    try {
                        inputImage = ImageFile.openImageForAnalysis(
                            inputImage,
                            input.file.getAbsolutePath(),
                            imageResizeFactor
                        );
                    }
                    catch (Throwable ex) {
                        inputImage = null;
                        ex.printStackTrace();
                    }

                    if (inputImage == null) {
                        outQueue.add(new AnalysisResult(input.file, null, null));
                        continue;
                    }

                    Stats result = null;
                    Throwable error = null;
                    boolean analyzeSucceeded = false;
                    try {
                        result = analyze(data, input.file, inputImage, params, sliceRunner);
                        analyzeSucceeded = true;
                        if (batchParams.shouldSaveResultImages)
                            saveResultImage(params, batchParams, data, inputImage, input.baseOutputPath, imageFormat);
                    }
                    catch (Throwable ex) {
                        cancellationToken.set(true);
                        ex.printStackTrace();
                        error = ex;
                    }

                    outQueue.add(new AnalysisResult(input.file, result, error));

                    if (error != null)
                        break;
                }

                data.close();
            }
        }

        final ConcurrentLinkedQueue<AnalysisInput> inQueue;
        final LinkedBlockingQueue<AnalysisResult> outQueue;
        final AnalyzerParameters params;
        final BatchParameters batchParams;
        final IProgressToken uiToken;
        final AtomicBoolean cancellationToken;
        final String imageFormat;

        RefVector<Worker> workers = new RefVector<>(Worker.class);

        AnalysisWorkers(AnalysisInput[] inputs, String format, AnalyzerParameters params, BatchParameters batchParams, IProgressToken uiToken)
        {
            this.inQueue = new ConcurrentLinkedQueue<>();
            this.outQueue = new LinkedBlockingQueue<>();
            this.imageFormat = format;
            this.params = params;
            this.batchParams = batchParams;
            this.uiToken = uiToken;
            this.cancellationToken = new AtomicBoolean(false);

            for (int i = 0; i < inputs.length; i++)
                inQueue.add(inputs[i]);
        }

        Worker addWorker()
        {
            Worker w = new Worker();
            workers.add(w);
            return w;
        }
    }

    public static void doBatchAnalysis(
        AnalyzerParameters params,
        BatchParameters batchParams,
        IProgressToken uiToken,
        ArrayList<XlsxReader.SheetCells> originalSheets
    ) {
        if (batchParams.workerCount <= 0)
            return;

        String imageFormat;
        try {
            imageFormat = resolveImageFormat(batchParams.resultImageFormat);
        }
        catch (Exception ex) {
            BatchUtils.showExceptionInDialogBox(ex);
            return;
        }

        uiToken.onEnumerationStart();

        ArrayList<File> inputs = new ArrayList<>();
        BidirectionalMap<String, String> outputPathMap = new BidirectionalMap<>();
        for (String path : batchParams.inputImagePaths) {
            enumerateImageFilesRecursively(inputs, new File(path), outputPathMap, uiToken);
            if (uiToken.isClosed())
                return;
        }

        if (inputs.isEmpty()) {
            uiToken.notifyNoImages();
            return;
        }

        File excelPath = new File(batchParams.excelFilePath);
        SpreadsheetWriter writer;
        try {
            writer = createWriterWithNewSheet(originalSheets, excelPath.getParentFile(), excelPath.getName());
        }
        catch (IOException ex) {
            BatchUtils.showExceptionInDialogBox(ex);
            return;
        }

        final int nImages = inputs.size();
        uiToken.onBatchStatsKnown(nImages);

        AnalysisInput[] analysisInputs = new AnalysisInput[nImages];

        if (batchParams.shouldSaveImagesToSpecificFolder) {
            for (int i = 0; i < nImages; i++) {
                File f = inputs.get(i);
                analysisInputs[i] = new AnalysisInput(f, resolveOutputPath(batchParams.resultImagesPath, outputPathMap, f));
            }
        }
        else {
            for (int i = 0; i < nImages; i++) {
                File f = inputs.get(i);
                analysisInputs[i] = new AnalysisInput(f, f.getAbsolutePath());
            }
        }

        AnalysisWorkers workers = new AnalysisWorkers(
            analysisInputs,
            imageFormat,
            params,
            batchParams,
            uiToken
        );

        for (int i = 0; i < batchParams.workerCount; i++)
            threadPool.submit(workers.addWorker());

        writer.shouldSaveAfterEveryRow = false;

        long lastSpreadsheetTime = 0L;
        boolean justSaved = false;
        boolean wasInputQueueEmpty = false;
        int nResults = 0;

        while (nResults < nImages && !workers.cancellationToken.get()) {
            if (uiToken.isClosed()) {
                workers.cancellationToken.set(true);
                break;
            }

            AnalysisResult result = null;
            try {
                result = workers.outQueue.poll(10, TimeUnit.SECONDS);
            }
            catch (InterruptedException ex) {
                workers.cancellationToken.set(true);
                break;
            }

            if (result == null) {
                if (wasInputQueueEmpty)
                    break;

                wasInputQueueEmpty = workers.inQueue.peek() == null;
                continue;
            }

            nResults++;

            // if an error is detected here, then 'cancellationToken' has already been set
            if (result.error != null) {
                writer.shouldSaveAfterEveryRow = true;

                try { writeError(writer, result.error, result.file); }
                catch (Exception ignored) {}

                uiToken.onImageDone(result.file.getAbsolutePath(), result.error);
                BatchUtils.showExceptionInDialogBox(result.error);
                break;
            }
            else if (result.stats == null) {
                uiToken.notifyImageWasInvalid();
                continue;
            }

            justSaved = false;

            long resultTime = System.nanoTime();
            if (lastSpreadsheetTime == 0L || resultTime - lastSpreadsheetTime < 5 * NANOS_PER_SEC) {
                writer.shouldSaveAfterEveryRow = true;
                lastSpreadsheetTime = resultTime;
                justSaved = true;
            }

            try {
                writeResultToSheet(writer, result.stats);
            }
            catch (IOException ex) {
                workers.cancellationToken.set(true);
                uiToken.onImageDone(result.file.getAbsolutePath(), ex);
                BatchUtils.showExceptionInDialogBox(ex);
                break;
            }

            writer.shouldSaveAfterEveryRow = false;
            uiToken.onImageDone(result.file.getAbsolutePath(), null);
        }

        if (lastSpreadsheetTime == 0L) {
            uiToken.notifyNoImages();
            return;
        }

        if (!justSaved) {
            try {
                writer.save();
            }
            catch (IOException ex) {
                BatchUtils.showExceptionInDialogBox(ex);
            }
        }

        writer.shouldSaveAfterEveryRow = true;
        uiToken.onFinished(writer);
    }

    static void saveResultImage(
        AnalyzerParameters params,
        BatchParameters batchParams,
        Scratch data,
        ArgbBuffer inputImage,
        String basePath,
        String format
    ) throws IOException
    {
        drawOverlay(
            params,
            data.convexHull,
            data.skelResult,
            data.analysisImage.buf,
            inputImage.pixels,
            inputImage.pixels,
            inputImage.width,
            inputImage.height,
            inputImage.brightestChannel
        );

        ImageFile.saveImage(inputImage, format, basePath + " result." + format);
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

    public static String resolveImageFormat(String str) throws Exception
    {
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
        IProgressToken uiToken
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
                if (uiToken.isClosed())
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

    public static Stats analyze(
        Scratch data,
        File inFile,
        ArgbBuffer inputImage,
        AnalyzerParameters params,
        ISliceRunner sliceRunner
    ) {
        data.reallocate(params, inputImage.width, inputImage.height, 1);

        byte[] analysisImage = data.analysisImage.buf;

        Tubeness.computeTubenessImage(
            data.tubeness,
            sliceRunner,
            MAX_WORKERS,
            analysisImage,
            inputImage.pixels,
            inputImage.width,
            inputImage.height,
            inputImage.brightestChannel,
            params.sigmas,
            params.sigmas.length
        );

        //ImageFile.writePgm(analysisImage, inputImage.width, inputImage.height, inFile.getAbsolutePath() + " tubeness.pgm");

        BatchUtils.thresholdFlexible(
            analysisImage,
            inputImage.width,
            inputImage.height,
            params.thresholdLow,
            params.thresholdHigh
        );

        //ImageFile.writePgm(analysisImage, inputImage.width, inputImage.height, inFile.getAbsolutePath() + " thresholded.pgm");

        byte[] skeletonImage = data.b2.buf;

        Filters.filterMax(skeletonImage, analysisImage, inputImage.width, inputImage.height); // erode
        Filters.filterMax(analysisImage, skeletonImage, inputImage.width, inputImage.height); // erode
        Filters.filterMin(skeletonImage, analysisImage, inputImage.width, inputImage.height); // dilate
        Filters.filterMin(analysisImage, skeletonImage, inputImage.width, inputImage.height); // dilate

        //ImageFile.writePgm(analysisImage, inputImage.width, inputImage.height, "filtered.pgm");

        int[] particleBuf = data.i1.buf;

        Particles.computeShapes(
            data.particleScratch,
            particleBuf,
            analysisImage,
            inputImage.pixels,
            inputImage.width,
            inputImage.height
        );

        Particles.removeVesselVoids(
            data.particleScratch,
            particleBuf,
            analysisImage,
            inputImage.width,
            inputImage.height,
            params.shouldFillBrightShapes ? params.brightShapeThresholdFactor : 0.0
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

        data.vesselPixelArea = BatchUtils.countForegroundPixels(analysisImage, inputImage.width, inputImage.height);

        if (params.shouldComputeLacunarity)
            Lacunarity2.computeLacunarity(data.lacunarity, analysisImage, inputImage.width, inputImage.height, 10, 10, 5);

        data.convexHullArea = ConvexHull.findConvexHull(data.convexHull, analysisImage, inputImage.width, inputImage.height);

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
        double linearScalingFactor = params.shouldApplyLinearScale ? params.linearScalingFactor : 1.0;

        if (params.shouldComputeThickness)
        {
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
        stats.brightShapeThresholdFactor = params.shouldFillBrightShapes ? params.brightShapeThresholdFactor : 0.0;
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
            totalLength += (double)data.skelResult.totalBranchLengths[i];

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
        int width,
        int height,
        int brightestChannel
    ) {
        int area = width * height;

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
            BatchUtils.formatDoubleArray(stats.sigmas, ""),
            stats.removeSmallParticlesThreshold,
            stats.fillHolesValue,
            stats.brightShapeThresholdFactor,
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
