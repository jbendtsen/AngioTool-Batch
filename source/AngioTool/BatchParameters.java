package AngioTool;

import Utils.Misc;
import Utils.RefVector;

public class BatchParameters
{
    public String defaultPath;
    public String[] inputImagePaths;
    public String excelFilePath;
    public boolean shouldSaveResultImages;
    public boolean shouldSaveImagesToSpecificFolder;
    public String resultImagesPath;
    public String resultImageFormat;
    public boolean shouldOverrideWorkerCount;
    public int workerCount;

    private BatchParameters() {}

    public BatchParameters(
        String defaultPath,
        String[] inputImagePaths,
        String excelFilePath,
        boolean shouldSaveResultImages,
        boolean shouldSaveImagesToSpecificFolder,
        String resultImagesPath,
        String resultImageFormat,
        boolean shouldOverrideWorkerCount,
        int workerCount
    ) {
        this.defaultPath = defaultPath;
        this.inputImagePaths = inputImagePaths;
        this.excelFilePath = excelFilePath;
        this.shouldSaveResultImages = shouldSaveResultImages;
        this.shouldSaveImagesToSpecificFolder = shouldSaveImagesToSpecificFolder;
        this.resultImagesPath = resultImagesPath;
        this.resultImageFormat = resultImageFormat;
        this.shouldOverrideWorkerCount = shouldOverrideWorkerCount;
        this.workerCount = workerCount;
    }

    public static BatchParameters defaults()
    {
        BatchParameters params = new BatchParameters();
        params.defaultPath = "C:\\";
        params.resultImageFormat = "jpg";
        params.workerCount = 4;
        return params;
    }

    public RefVector<String> validate()
    {
        RefVector<String> errors = new RefVector<>(String.class);

        try {
            BatchProcessing.resolveImageFormat(resultImageFormat);
        }
        catch (Exception ex) {
            errors.add("Result image format: " + ex.getMessage());
        }

        if (inputImagePaths == null || inputImagePaths.length == 0)
            errors.add("At least one input folder is required");
        if (!Misc.isValidPath(excelFilePath))
            errors.add("Path to spreadsheet is missing");
        if (shouldSaveImagesToSpecificFolder && !Misc.isValidPath(resultImagesPath))
            errors.add("Specific output folder was selected but not provided");
        if (workerCount <= 0)
            errors.add(
                "Number of workers should be at least 1, optimally matching the number of processors (" +
                Runtime.getRuntime().availableProcessors() + ")"
            );

        return errors;
    }
}
