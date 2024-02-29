package AngioTool;

import Utils.BatchUtils;
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

    public BatchParameters() {}

    public BatchParameters(
        String defaultPath,
        String[] inputImagePaths,
        String excelFilePath,
        boolean shouldSaveResultImages,
        boolean shouldSaveImagesToSpecificFolder,
        String resultImagesPath,
        String resultImageFormat
    ) {
        this.defaultPath = defaultPath;
        this.inputImagePaths = inputImagePaths;
        this.excelFilePath = excelFilePath;
        this.shouldSaveResultImages = shouldSaveResultImages;
        this.shouldSaveImagesToSpecificFolder = shouldSaveImagesToSpecificFolder;
        this.resultImagesPath = resultImagesPath;
        this.resultImageFormat = resultImageFormat;
    }

    public static BatchParameters defaults()
    {
        BatchParameters params = new BatchParameters();
        params.defaultPath = "C:\\";
        params.resultImageFormat = "jpg";
        return params;
    }

    public RefVector<String> validate()
    {
        RefVector<String> errors = new RefVector<>(String.class);

        try {
            Analyzer.resolveImageFormat(resultImageFormat);
        }
        catch (Exception ex) {
            errors.add("Result image format: " + ex.getMessage());
        }

        if (inputImagePaths == null || inputImagePaths.length == 0)
            errors.add("At least one input folder is required");
        if (!BatchUtils.isValidPath(excelFilePath))
            errors.add("Path to spreadsheet is missing");
        if (shouldSaveImagesToSpecificFolder && !BatchUtils.isValidPath(resultImagesPath))
            errors.add("Specific output folder was selected but not provided");

        return errors;
    }
}
