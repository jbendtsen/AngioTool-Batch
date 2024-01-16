package Batch;

import AngioTool.AngioTool;
import AngioTool.AngioToolMain;
import AngioTool.ATPreferences;
import GUI.RoundedPanel;
import Utils.Utils;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatchAnalysisUi
{
    static int UNITS_GAP = 4;

    final JFrame parentFrame;
    final JDialog jdialog;

    final JLabel labelData = new JLabel();
    final JLabel labelInputFolders = new JLabel();
    final JButton btnInputFolders = new JButton();
    final JTextField textInputFolders = new JTextField();
    final JLabel labelExcel = new JLabel();
    final JButton btnExcel = new JButton();
    final JTextField textExcel = new JTextField();
    final ButtonGroup groupSaveResults = new ButtonGroup();
    final JRadioButton rbNoOutput = new JRadioButton();
    final JRadioButton rbSameOutput = new JRadioButton();
    final JRadioButton rbSaveResultsTo = new JRadioButton();
    final JButton btnSaveResultsFolder = new JButton();
    final JTextField textSaveResultsFolder = new JTextField();
    final JLabel labelResultsImageFormat = new JLabel();
    final JTextField textResultsImageFormat = new JTextField();

    final JLabel labelAnalysis = new JLabel();
    final JCheckBox cbComputeLacunarity = new JCheckBox();
    final JCheckBox cbComputeThickness = new JCheckBox();
    final JSeparator sepAnalysis = new JSeparator(SwingConstants.VERTICAL);
    final JLabel labelSkeletonizer = new JLabel();
    final ButtonGroup groupSkeletonizer = new ButtonGroup();
    final JRadioButton rbSkelFast = new JRadioButton();
    final JRadioButton rbSkelThorough = new JRadioButton();
    final NumberEntry elemResizeInputs;
    final NumberEntry elemLinearScaleFactor;
    final NumberEntry elemRemoveParticles;
    final NumberEntry elemFillHoles;
    final JLabel labelSigmas = new JLabel();
    final JTextField textSigmas = new JTextField();
    final JLabel labelIntensity = new JLabel();
    final JTextField textMinIntensity = new JTextField();
    final JTextField textMaxIntensity = new JTextField();

    final JLabel labelOverlay = new JLabel();
    final ColorSizeEntry elemOutline;
    final ColorSizeEntry elemBranches;
    final ColorSizeEntry elemSkeleton;
    final ColorSizeEntry elemConvexHull;

    final JSeparator sepProgress = new JSeparator();
    final JLabel labelProgress = new JLabel();
    final JLabel overallLabel = new JLabel();
    final JProgressBar overallProgress = new JProgressBar();
    final JLabel imageLabel = new JLabel();
    final JProgressBar imageProgress = new JProgressBar();
    final JButton analyzeBtn = new JButton();
    final JButton cancelBtn = new JButton();

    String defaultPath;
    ArrayList<XlsxReader.SheetCells> originalSheets = new ArrayList<>();
    int nErrors = 0;

    public Future<Void> analysisTaskFuture = null;
    public final AtomicBoolean isClosed = new AtomicBoolean(false);

    public BatchAnalysisUi(JFrame uiFrame, AnalyzerParameters params)
    {
        this.parentFrame = uiFrame;

        this.jdialog = new JDialog(parentFrame, "Batch Analysis", true);

        defaultPath = params.defaultPath;

        labelData.setText("Data");
        Utils.setNewFontSizeOn(labelData, 20);

        labelInputFolders.setText("Select input folders:");

        btnInputFolders.setIcon(Utils.ATOpenImageSmall);
        btnInputFolders.addActionListener((ActionEvent e) -> BatchAnalysisUi.this.selectInputFolders());
        //textInputFolders

        labelExcel.setText("Excel spreadsheet:");

        btnExcel.setIcon(Utils.ATExcelSmall);
        btnExcel.addActionListener((ActionEvent e) -> BatchAnalysisUi.this.selectExcelFile());
        //textExcel

        rbNoOutput.setText("No output");
        rbNoOutput.addActionListener((ActionEvent e) -> BatchAnalysisUi.this.toggleSaveResults());
        rbNoOutput.setSelected(!params.shouldSaveResultImages);

        rbSameOutput.setText("Same folders as inputs");
        rbSameOutput.addActionListener((ActionEvent e) -> BatchAnalysisUi.this.toggleSaveResults());
        rbSameOutput.setSelected(params.shouldSaveResultImages && !params.shouldSaveImagesToSpecificFolder);

        rbSaveResultsTo.setText("Save result images to:");
        rbSaveResultsTo.addActionListener((ActionEvent e) -> BatchAnalysisUi.this.toggleSaveResults());
        rbSaveResultsTo.setSelected(params.shouldSaveResultImages && params.shouldSaveImagesToSpecificFolder);

        groupSaveResults.add(rbNoOutput);
        groupSaveResults.add(rbSameOutput);
        groupSaveResults.add(rbSaveResultsTo);

        btnSaveResultsFolder.setIcon(Utils.ATOpenImageSmall);
        btnSaveResultsFolder.addActionListener((ActionEvent e) -> BatchAnalysisUi.this.selectResultFolder());
        //textSaveResultsFolder

        toggleSaveResults();

        labelResultsImageFormat.setText("Result image format: ");
        textResultsImageFormat.setText(params.resultImageFormat);

        labelAnalysis.setText("Analysis");
        Utils.setNewFontSizeOn(labelAnalysis, 20);

        labelSkeletonizer.setText("Skeletonizer:");

        rbSkelFast.setText("Fast (Zha84)");
        rbSkelFast.setSelected(params.shouldUseFastSkeletonizer);

        rbSkelThorough.setText("Thorough (Lee94)");
        rbSkelThorough.setSelected(!params.shouldUseFastSkeletonizer);

        groupSkeletonizer.add(rbSkelFast);
        groupSkeletonizer.add(rbSkelThorough);

        /*{
            ButtonModel m = params.shouldUseFastSkeletonizer ? rbSkelFast.getModel() : rbSkelThorough.getModel();
            groupSaveResults.setSelected(m, true);
        }*/

        cbComputeLacunarity.setText("Lacunarity");
        cbComputeLacunarity.setSelected(params.shouldComputeLacunarity);

        cbComputeThickness.setText("Thickness");
        cbComputeThickness.setSelected(params.shouldComputeThickness);

        elemResizeInputs = new NumberEntry("Resize inputs by:", params.shouldResizeImage, params.resizingFactor, "x");

        elemLinearScaleFactor = new NumberEntry("Measurement Scale:", params.shouldApplyLinearScale, params.linearScalingFactor, "x");

        elemRemoveParticles = new NumberEntry("Remove Particles:", params.shouldRemoveSmallParticles, params.removeSmallParticlesThreshold, "px");

        elemFillHoles = new NumberEntry("Fill Holes:", params.shouldFillHoles, params.fillHolesValue, "px");

        labelSigmas.setText("Vessel Diameters list");

        textSigmas.setText(Utils.formatDoubleArray(params.sigmas));
        textSigmas.setToolTipText("List of sigmas (numbers)");

        labelIntensity.setText("Vessel Intensity range");

        textMinIntensity.setText("" + params.thresholdLow);
        textMaxIntensity.setText("" + params.thresholdHigh);

        labelOverlay.setText("Overlay");
        Utils.setNewFontSizeOn(labelOverlay, 20);

        elemOutline = new ColorSizeEntry("Outline:", params.shouldDrawOutline, params.outlineSize, params.outlineColor);
        elemBranches = new ColorSizeEntry("Branches:", params.shouldDrawBranchPoints, params.branchingPointsSize, params.branchingPointsColor);
        elemSkeleton = new ColorSizeEntry("Skeleton:", params.shouldDrawSkeleton, params.skeletonSize, params.skeletonColor);
        elemConvexHull = new ColorSizeEntry("Convex Hull:", params.shouldDrawConvexHull, params.convexHullSize, params.convexHullColor);

        //sepProgress

        labelProgress.setText("Progress");
        Utils.setNewFontSizeOn(labelProgress, 20);

        //overallLabel
        overallProgress.setValue(0);
        overallProgress.setStringPainted(true);

        //imageLabel
        imageProgress.setValue(0);
        imageProgress.setStringPainted(true);

        analyzeBtn.setText("Run");
        analyzeBtn.addActionListener((ActionEvent e) -> BatchAnalysisUi.this.startAnalysis());

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener((ActionEvent e) -> BatchAnalysisUi.this.close());
        cancelBtn.setEnabled(false);
    }

    public void showDialog()
    {
        SwingUtilities.invokeLater(() -> {
            JPanel dialogPanel = new JPanel();
            GroupLayout layout = new GroupLayout(dialogPanel);
            dialogPanel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            arrangeUi(layout);

            jdialog.add(dialogPanel);
            jdialog.pack();

            jdialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evt) {
                    ATPreferences.savePreferences(buildNewParamsFromUi(), AngioTool.BATCH_TXT);
                }
            });

            jdialog.setMinimumSize(jdialog.getPreferredSize());
            jdialog.setLocationRelativeTo(parentFrame);
            jdialog.setVisible(true);
        });
    }

    private void arrangeUi(GroupLayout layout)
    {
        layout.setHorizontalGroup(layout.createParallelGroup()
            .addComponent(labelData)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(labelInputFolders)
                    .addComponent(labelExcel)
                )
                .addGroup(layout.createParallelGroup()
                    .addComponent(btnInputFolders)
                    .addComponent(btnExcel)
                )
                .addGroup(layout.createParallelGroup()
                    .addComponent(textInputFolders)
                    .addComponent(textExcel)
                )
            )
            .addComponent(rbNoOutput)
            .addComponent(rbSameOutput)
            .addGroup(layout.createSequentialGroup()
                .addComponent(rbSaveResultsTo)
                .addComponent(btnSaveResultsFolder)
                .addComponent(textSaveResultsFolder)
            )
            .addGroup(layout.createSequentialGroup()
                .addComponent(labelResultsImageFormat)
                .addComponent(textResultsImageFormat, 40, 60, 80)
            )
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(labelAnalysis)
                    .addComponent(cbComputeLacunarity)
                    .addComponent(cbComputeThickness)
                )
                .addComponent(sepAnalysis)
                .addGroup(layout.createParallelGroup()
                    .addComponent(labelSkeletonizer)
                    .addComponent(rbSkelFast)
                    .addComponent(rbSkelThorough)
                )
            )
            .addGroup(
                arrangeParallelEntries(
                    elemLinearScaleFactor, elemRemoveParticles, layout, arrangeParallelEntries(
                        elemResizeInputs, elemFillHoles, layout, layout.createSequentialGroup()
                    ).addGap(20)
                )
            )
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(labelSigmas)
                    .addComponent(textSigmas)
                )
                .addGroup(layout.createParallelGroup()
                    .addComponent(labelIntensity)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(textMinIntensity)
                        .addComponent(textMaxIntensity)
                    )
                )
            )
            .addComponent(labelOverlay)
            .addGroup(
                arrangeParallelEntries(
                    elemBranches, elemConvexHull, layout, arrangeParallelEntries(
                        elemOutline, elemSkeleton, layout, layout.createSequentialGroup()
                    ).addGap(20)
                )
            )
            .addComponent(sepProgress)
            .addComponent(labelProgress)
            .addComponent(overallLabel)
            .addComponent(overallProgress)
            .addComponent(imageLabel)
            .addComponent(imageProgress)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(analyzeBtn)
                .addComponent(cancelBtn)
            )
        );

        final int MIN_PATH_WIDTH = 18;
        final int PATH_WIDTH = 30;

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(labelData)
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(labelInputFolders)
                .addComponent(btnInputFolders)
                .addComponent(textInputFolders, MIN_PATH_WIDTH, PATH_WIDTH, PATH_WIDTH)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(labelExcel)
                .addComponent(btnExcel)
                .addComponent(textExcel, MIN_PATH_WIDTH, PATH_WIDTH, PATH_WIDTH)
            )
            .addComponent(rbNoOutput)
            .addComponent(rbSameOutput)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(rbSaveResultsTo)
                .addComponent(btnSaveResultsFolder)
                .addComponent(textSaveResultsFolder, MIN_PATH_WIDTH, PATH_WIDTH, PATH_WIDTH)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(labelResultsImageFormat)
                .addComponent(textResultsImageFormat)
            )
            .addGap(12)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelAnalysis)
                .addComponent(sepAnalysis)
                .addComponent(labelSkeletonizer)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(cbComputeLacunarity)
                .addComponent(sepAnalysis)
                .addComponent(rbSkelFast)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(cbComputeThickness)
                .addComponent(sepAnalysis)
                .addComponent(rbSkelThorough)
            )
            .addGroup(
                elemLinearScaleFactor.addToGroup(
                    elemResizeInputs.addToGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGroup(
                elemFillHoles.addToGroup(
                    elemRemoveParticles.addToGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelSigmas)
                .addComponent(labelIntensity)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(textSigmas)
                .addComponent(textMinIntensity)
                .addComponent(textMaxIntensity)
            )
            .addGap(12)
            .addComponent(labelOverlay)
            .addGap(8)
            .addGroup(
                elemBranches.addToGroup(
                    elemOutline.addToGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGroup(
                elemConvexHull.addToGroup(
                    elemSkeleton.addToGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addComponent(sepProgress)
            .addComponent(labelProgress)
            .addComponent(overallLabel)
            .addComponent(overallProgress)
            .addComponent(imageLabel)
            .addComponent(imageProgress)
            .addGroup(layout.createParallelGroup()
                .addComponent(analyzeBtn)
                .addComponent(cancelBtn)
            )
        );
    }

    private static GroupLayout.SequentialGroup arrangeParallelEntries(
        NumberEntry a,
        NumberEntry b,
        GroupLayout layout,
        GroupLayout.SequentialGroup sequentialGroup
    ) {
        sequentialGroup
            .addGroup(layout.createParallelGroup()
                .addComponent(a.cb)
                .addComponent(b.cb)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(a.units)
                .addComponent(b.units)
            )
            .addGap(UNITS_GAP)
            .addGroup(layout.createParallelGroup()
                .addComponent(a.tf)
                .addComponent(b.tf)
            );

        if (a instanceof ColorSizeEntry && b instanceof ColorSizeEntry)
            sequentialGroup.addGroup(layout.createParallelGroup()
                .addComponent(((ColorSizeEntry)a).panel, 20, 30, 30)
                .addComponent(((ColorSizeEntry)b).panel, 20, 30, 30)
            );

        return sequentialGroup;
    }

    void toggleSaveResults() {
        boolean enabled = rbSaveResultsTo.isSelected();
        btnSaveResultsFolder.setEnabled(enabled);
        textSaveResultsFolder.setEnabled(enabled);
    }

    static JFileChooser createFileChooser() {
        JFileChooser fc = new JFileChooser();
        fc.setPreferredSize(new Dimension(800, 500));
        return fc;
    }

    void selectInputFolders() {
        JFileChooser fc = createFileChooser();
        fc.setDialogTitle("Select Folders");
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(true);
        fc.setCurrentDirectory(new File(defaultPath));

        if (fc.showOpenDialog(parentFrame) != 0)
            return;

        File[] folderList = fc.getSelectedFiles();
        if (folderList == null || folderList.length == 0)
            return;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < folderList.length; i++) {
            if (i > 0)
                sb.append(";");
            sb.append(folderList[i].getAbsolutePath().replace(";", "" + File.separatorChar + ";"));
        }

        textInputFolders.setText(sb.toString());
    }

    void selectExcelFile() {
        JFileChooser fc = createFileChooser();
        fc.setDialogTitle("Append to Excel spreadsheet");
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setCurrentDirectory(new File(defaultPath));
        fc.setFileFilter(new FileFilter() {
            @Override public boolean accept(File f) {
                String name = f.getName();
                return !f.isFile() || name.endsWith(".xls") || name.endsWith(".xlsx");
            }
            @Override public String getDescription() {
                return "Excel Spreadsheet (.xls, .xlsx)";
            }
        });

        if (fc.showOpenDialog(parentFrame) != 0)
            return;

        defaultPath = fc.getCurrentDirectory().getAbsolutePath();

        File xlsxFile = fc.getSelectedFile();
        if (!Utils.hasAnyFileExtension(xlsxFile))
            xlsxFile = new File(xlsxFile.getAbsolutePath() + ".xlsx");

        String xlsxPath = xlsxFile.getAbsolutePath();

        ArrayList<XlsxReader.SheetCells> sheets = null;
        if (xlsxFile.exists()) {
            try { sheets = XlsxReader.loadXlsxFromFile(xlsxPath); }
            catch (IOException ignored) {}

            if (sheets == null || sheets.isEmpty() || (sheets.get(0).flags & (1 << 31)) == 0) {
                try {
                    Files.copy(
                        xlsxFile.toPath(),
                        new File(Utils.decideBackupFileName(xlsxPath, "xlsx")).toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                    );
                }
                catch (IOException ignored) {}
            }
        }

        if (sheets == null)
            sheets = new ArrayList<XlsxReader.SheetCells>();

        originalSheets = sheets;

        textExcel.setText(xlsxFile.getAbsolutePath());
    }

    void selectResultFolder() {
        JFileChooser fc = createFileChooser();
        fc.setDialogTitle("Select Folders");
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setCurrentDirectory(new File(defaultPath));

        if (fc.showOpenDialog(parentFrame) == 0) {
            File file = fc.getSelectedFile();
            if (file != null)
                textSaveResultsFolder.setText(file.getAbsolutePath());
        }
    }

    AnalyzerParameters buildNewParamsFromUi() {
        boolean shouldSaveImages;
        boolean shouldUseSpecificOutputFolder;
        ButtonModel saveImageType = groupSaveResults.getSelection();
        if (saveImageType == rbSaveResultsTo.getModel()) {
            shouldSaveImages = true;
            shouldUseSpecificOutputFolder = true;
        }
        else if (saveImageType == rbNoOutput.getModel()) {
            shouldSaveImages = false;
            shouldUseSpecificOutputFolder = false;
        }
        else {
            shouldSaveImages = true;
            shouldUseSpecificOutputFolder = false;
        }

        ButtonModel skelType = groupSkeletonizer.getSelection();
        boolean shouldUseFastSkel = skelType == rbSkelFast.getModel();

        return new AnalyzerParameters(
            defaultPath,
            Utils.splitPaths(textInputFolders.getText(), ';', File.separatorChar),
            textExcel.getText(),
            shouldSaveImages,
            shouldUseSpecificOutputFolder,
            textSaveResultsFolder.getText(),
            textResultsImageFormat.getText(),
            elemResizeInputs.cb.isSelected(),
            elemResizeInputs.getValue(),
            elemRemoveParticles.cb.isSelected(),
            elemRemoveParticles.getValue(),
            elemFillHoles.cb.isSelected(),
            elemFillHoles.getValue(),
            Utils.getSomeDoubles(textSigmas.getText()),
            Integer.parseInt(textMaxIntensity.getText()),
            Integer.parseInt(textMinIntensity.getText()),
            shouldUseFastSkel,
            elemLinearScaleFactor.cb.isSelected(),
            elemLinearScaleFactor.getValue(),
            true, // shouldShowOverlayOrGallery
            elemOutline.cb.isSelected(),
            elemOutline.color,
            elemOutline.getValue(),
            elemSkeleton.cb.isSelected(),
            elemSkeleton.color,
            elemSkeleton.getValue(),
            elemBranches.cb.isSelected(),
            elemBranches.color,
            elemBranches.getValue(),
            elemConvexHull.cb.isSelected(),
            elemConvexHull.color,
            elemConvexHull.getValue(),
            false, // shouldScalePixelValues
            cbComputeLacunarity.isSelected(),
            cbComputeThickness.isSelected()
        );
    }

    public void startAnalysis()
    {
        if (analysisTaskFuture != null && !analysisTaskFuture.isDone()) {
            Utils.showDialogBox(
                "Analysis Still in Progress",
                "A batch analysis is still running. Either cancel it or wait for it to complete."
            );
            return;
        }

        nErrors = 0;

        AnalyzerParameters params;
        try {
            params = buildNewParamsFromUi();
        }
        catch (Throwable t) {
            Utils.showDialogBox("Parsing Error", "Invalid data in the form (" + t.getClass().getSimpleName() + ")");
            return;
        }

        RefVector<String> errors = params.validate();
        if (errors != null && errors.size > 0) {
            int nErrors = errors.size;
            String header = nErrors > 1 ? ("There were " + nErrors + " errors:\n") : "";
            Utils.showDialogBox(
                "Validation Error" + (nErrors > 1 ? "s" : ""),
                header + errors.makeJoinedString("\n")
            );
            return;
        }

        cancelBtn.setEnabled(true);

        ATPreferences.savePreferences(params, AngioTool.BATCH_TXT);

        analysisTaskFuture = (Future<Void>)AngioToolMain.threadPool.submit(
            () -> Analyzer.doBatchAnalysis(params, BatchAnalysisUi.this, originalSheets)
        );
    }

    static void updateDialogSize(JDialog dlg) {
        Dimension preferred = dlg.getPreferredSize();
        Dimension curSize = dlg.getSize();

        if (preferred.height > curSize.height)
            dlg.setSize(new Dimension(curSize.width, preferred.height));
    }

    public void notifyNoImages()
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            overallLabel.setText("No images were found!");
            imageLabel.setText("");
            cancelBtn.setEnabled(false);

            updateDialogSize(jdialog);
        });
    }

    public void startProgressBars(int nImages, int maxProgressPerImage)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            overallLabel.setText("Analyzing images...");
            overallProgress.setValue(0);
            overallProgress.setMaximum(nImages);

            imageProgress.setValue(0);
            imageProgress.setMaximum(maxProgressPerImage);

            updateDialogSize(jdialog);
        });
    }

    public void notifyImageWasInvalid()
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            int nImages = overallProgress.getMaximum();
            overallProgress.setMaximum(nImages > 1 ? (nImages-1) : 1);
        });
    }

    boolean wasStartImageJustCalled = false;

    public void onStartImage(String absPath)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            wasStartImageJustCalled = true;

            int pathLen = absPath.length();
            String partialFileName = pathLen > 60 ?
                absPath.substring(0, 29) + "..." + absPath.substring(pathLen - 29) :
                absPath;

            int current = overallProgress.getValue() + 1;
            String status = "" + current + "/" + overallProgress.getMaximum();
            if (nErrors > 0)
                status += ", " + nErrors + (nErrors == 1 ? " error." : " errors.");

            status += " Processing " + partialFileName + "...";
            overallLabel.setText(status);

            imageProgress.setValue(0);

            updateDialogSize(jdialog);
        });
    }

    public void updateImageProgress(String statusMsg)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            int progress = wasStartImageJustCalled ? 0 : (imageProgress.getValue() + 1);
            wasStartImageJustCalled = false;

            imageLabel.setText(statusMsg);
            imageProgress.setValue(progress);

            updateDialogSize(jdialog);
        });
    }

    public void onImageDone(Throwable error)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            if (error != null)
                nErrors++;

            imageProgress.setValue(imageProgress.getMaximum());
            overallProgress.setValue(overallProgress.getValue() + 1);
        });
    }

    public void onFinished(String sheetFileName)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            int nImages = overallProgress.getMaximum();
            String status = "" + nImages + "/" + nImages;
            if (nErrors > 0) {
                status += " Finished with " + nErrors + " error";
                if (nErrors != 1)
                    status += "s";
            }
            else {
                status += " Done!";
            }

            overallLabel.setText(status);
            imageLabel.setText("Saved Excel results to " + sheetFileName);
            imageProgress.setValue(imageProgress.getMaximum());
            cancelBtn.setEnabled(false);

            updateDialogSize(jdialog);
        });
    }

    public void close()
    {
        SwingUtilities.invokeLater(() -> {
            if (!isClosed.getAndSet(true))
                jdialog.dispose();
        });
    }

    static class NumberEntry
    {
        public JCheckBox cb;
        public JLabel units;
        public JTextField tf;

        public String name;
        public final double originalValue;

        public NumberEntry(String name, boolean enabled, double value, String unitsStr)
        {
            this.name = name;
            this.originalValue = value;

            cb = new JCheckBox();
            cb.addActionListener((ActionEvent e) -> toggleCheckbox());
            cb.setText(name);

            units = new JLabel(unitsStr, SwingConstants.RIGHT);

            tf = new JTextField();
            tf.setText(Utils.formatDouble(value));
            tf.setEnabled(enabled);

            cb.setSelected(enabled);
        }

        public double getValue() {
            String str = tf.getText();
            try {
                return Double.parseDouble(str);
            }
            catch (Exception ex) {
                return originalValue;
            }
        }

        public GroupLayout.Group addToGroup(GroupLayout.Group group)
        {
            group.addComponent(cb).addComponent(units);
            if (group instanceof GroupLayout.SequentialGroup)
                ((GroupLayout.SequentialGroup)group).addGap(UNITS_GAP);
            return group.addComponent(tf);
        }

        public void toggleCheckbox()
        {
            boolean enabled = cb.isSelected();
            double value = getValue();
            tf.setText(Utils.formatDouble(value));
            tf.setEnabled(enabled);
        }
    }

    static class ColorSizeEntry extends NumberEntry
    {
        public RoundedPanel panel;
        //public JButton btn;
        //public GroupLayout panelLayout;

        public Rgb color;
        public final Rgb originalColor;

        public ColorSizeEntry(String name, boolean enabled, double value, Rgb color)
        {
            super(name, enabled, value, "px");
            this.color = color;
            this.originalColor = color;

            //btn = new JButton();
            //btn.setContentAreaFilled(false);
            //btn.addActionListener((ActionEvent e) -> clickColorButton());

            panel = new RoundedPanel();
            panel.setCornerRadius(7);
            panel.setBackground(color.toColor());
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectColor();
                }
            });
        }

        @Override
        public GroupLayout.Group addToGroup(GroupLayout.Group group)
        {
            group.addComponent(cb).addComponent(units);
            if (group instanceof GroupLayout.SequentialGroup)
                ((GroupLayout.SequentialGroup)group).addGap(UNITS_GAP);
            return group
                .addComponent(tf)
                .addComponent(panel, 20, 25, 25);
        }

        void selectColor()
        {
            if (cb.isSelected()) {
                Color background = JColorChooser.showDialog(null, name, color.toColor());
                if (background != null) {
                    color = new Rgb(background);
                    panel.setBackground(background);
                }
            }
        }
    }
}
