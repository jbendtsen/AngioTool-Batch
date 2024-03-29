package AngioTool;

import Pixels.Rgb;
import Utils.*;
import Xlsx.*;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatchWindow extends JFrame implements BatchProcessing.IProgressToken
{
    static final int UNITS_GAP = 4;
    static final double BALANCED_FACTOR = 0.75;

    final AngioToolGui2 mainWindow;

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
    final JCheckBox cbWorkerCount = new JCheckBox();
    final JTextField textWorkerCount = new JTextField();
    final JLabel labelWorkerCountHelp = new JLabel();

    final JSeparator sepProgress = new JSeparator();
    final JLabel labelProgress = new JLabel();
    final JLabel overallLabel = new JLabel();
    final JProgressBar overallProgress = new JProgressBar();
    final JLabel labelCurrentFile = new JLabel();
    final JButton analyzeBtn = new JButton();
    final JButton cancelBtn = new JButton();

    final String workerCountHelpString;
    int storedWorkerCount;

    String defaultPath;
    ArrayList<XlsxReader.SheetCells> originalSheets = new ArrayList<>();
    int nErrors = 0;

    public Future<Void> analysisTaskFuture = null;
    public final AtomicBoolean isClosed = new AtomicBoolean(false);

    public BatchWindow(AngioToolGui2 mainWindow, BatchParameters params)
    {
        super("Batch Analysis");
        this.mainWindow = mainWindow;
        this.defaultPath = params.defaultPath;

        this.setIconImage(AngioTool.ATIcon.getImage());

        labelData.setText("Data");
        Misc.setNewFontSizeOn(labelData, 20);

        labelInputFolders.setText("Select input folders:");

        btnInputFolders.setIcon(AngioTool.ATFolderSmall);
        btnInputFolders.addActionListener((ActionEvent e) -> BatchWindow.this.selectInputFolders());
        //textInputFolders

        labelExcel.setText("Excel spreadsheet:");

        btnExcel.setIcon(AngioTool.ATExcelSmall);
        btnExcel.addActionListener((ActionEvent e) -> BatchWindow.this.selectExcelFile());
        //textExcel

        rbNoOutput.setText("No output");
        rbNoOutput.addActionListener((ActionEvent e) -> BatchWindow.this.toggleSaveResults());
        rbNoOutput.setSelected(!params.shouldSaveResultImages);

        rbSameOutput.setText("Same folders as inputs");
        rbSameOutput.addActionListener((ActionEvent e) -> BatchWindow.this.toggleSaveResults());
        rbSameOutput.setSelected(params.shouldSaveResultImages && !params.shouldSaveImagesToSpecificFolder);

        rbSaveResultsTo.setText("Save result images to:");
        rbSaveResultsTo.addActionListener((ActionEvent e) -> BatchWindow.this.toggleSaveResults());
        rbSaveResultsTo.setSelected(params.shouldSaveResultImages && params.shouldSaveImagesToSpecificFolder);

        groupSaveResults.add(rbNoOutput);
        groupSaveResults.add(rbSameOutput);
        groupSaveResults.add(rbSaveResultsTo);

        btnSaveResultsFolder.setIcon(AngioTool.ATFolderSmall);
        btnSaveResultsFolder.addActionListener((ActionEvent e) -> BatchWindow.this.selectResultFolder());
        //textSaveResultsFolder

        toggleSaveResults();

        labelResultsImageFormat.setText("Result image format: ");
        textResultsImageFormat.setText(params.resultImageFormat);

        cbWorkerCount.setText("Override job count: ");
        cbWorkerCount.setSelected(params.shouldOverrideWorkerCount);
        cbWorkerCount.addActionListener((ActionEvent e) -> BatchWindow.this.toggleWorkerCount());

        int nProcessors = Runtime.getRuntime().availableProcessors();
        int defaultWorkerCount = (int)(nProcessors * BALANCED_FACTOR);
        this.workerCountHelpString =
            "<html>Recommended job count:<br>" + nProcessors + " for maximum throughput,<br>" +
             defaultWorkerCount + " (default) for balancing resources</html>";

        Misc.setNewFontStyleOn(labelWorkerCountHelp, Font.PLAIN);

        if (params.shouldOverrideWorkerCount) {
            textWorkerCount.setText("" + params.workerCount);
            labelWorkerCountHelp.setText(workerCountHelpString);
        }
        else {
            textWorkerCount.setText("");
        }

        storedWorkerCount = params.workerCount > 0 ? params.workerCount : defaultWorkerCount;

        //sepProgress

        labelProgress.setText("Progress");
        Misc.setNewFontSizeOn(labelProgress, 20);

        //overallLabel
        overallProgress.setValue(0);
        overallProgress.setStringPainted(true);

        Misc.setNewFontStyleOn(labelCurrentFile, Font.ITALIC);

        analyzeBtn.setText("Run");
        analyzeBtn.addActionListener((ActionEvent e) -> BatchWindow.this.startAnalysis());

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener((ActionEvent e) -> BatchWindow.this.cancel());
        cancelBtn.setEnabled(false);
    }

    public void showDialog()
    {
        JPanel dialogPanel = new JPanel();
        GroupLayout layout = new GroupLayout(dialogPanel);
        dialogPanel.setLayout(layout);
        dialogPanel.setBorder(new EmptyBorder(0, 2, 12, 2));
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        arrangeUi(layout);

        this.getContentPane().add(dialogPanel);
        this.pack();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                ATPreferences.savePreferences(buildBatchParamsFromUi(), AngioTool.BATCH_TXT);
            }
        });

        int extraWidth = cbWorkerCount.isSelected() ? 150 : 250;

        Dimension preferredSize = this.getPreferredSize();
        this.setMinimumSize(preferredSize);
        this.setSize(new Dimension(preferredSize.width + extraWidth, preferredSize.height + 10));

        this.setLocation(700, 300);
        this.setVisible(true);
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
                .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelResultsImageFormat)
                        .addComponent(textResultsImageFormat, 0, 0, 80)
                    )
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbWorkerCount)
                        .addComponent(textWorkerCount, 0, 0, 80)
                    )
                )
                .addComponent(labelWorkerCountHelp)
            )
            .addComponent(sepProgress)
            .addComponent(labelProgress)
            .addComponent(overallLabel)
            .addComponent(overallProgress)
            .addComponent(labelCurrentFile)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(analyzeBtn)
                .addComponent(cancelBtn)
            )
        );

        final int MIN_TEXT_HEIGHT = 18;
        final int TEXT_HEIGHT = 30;

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(labelData)
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(labelInputFolders)
                .addComponent(btnInputFolders)
                .addComponent(textInputFolders, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(labelExcel)
                .addComponent(btnExcel)
                .addComponent(textExcel, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
            )
            .addComponent(rbNoOutput)
            .addComponent(rbSameOutput)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(rbSaveResultsTo)
                .addComponent(btnSaveResultsFolder)
                .addComponent(textSaveResultsFolder, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
            )
            .addGap(12)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(labelResultsImageFormat, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
                    .addComponent(cbWorkerCount, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
                )
                .addGroup(layout.createSequentialGroup()
                    .addComponent(textResultsImageFormat, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
                    .addComponent(textWorkerCount, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
                )
                .addComponent(labelWorkerCountHelp)
            )
            .addGap(12)
            .addComponent(sepProgress)
            .addComponent(labelProgress)
            .addComponent(overallLabel)
            .addComponent(overallProgress)
            .addComponent(labelCurrentFile, 48, 48, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup()
                .addComponent(analyzeBtn)
                .addComponent(cancelBtn)
            )
        );
    }

    void toggleSaveResults() {
        boolean enabled = rbSaveResultsTo.isSelected();
        btnSaveResultsFolder.setEnabled(enabled);
        textSaveResultsFolder.setEnabled(enabled);
    }

    void toggleWorkerCount() {
        boolean enabled = cbWorkerCount.isSelected();
        labelWorkerCountHelp.setText(workerCountHelpString);
        if (enabled) {
            int workerCount = storedWorkerCount > 0 ? storedWorkerCount : getDefaultWorkerCount();
            textWorkerCount.setText("" + workerCount);
        }
        else {
            try {
                storedWorkerCount = Integer.parseInt(textWorkerCount.getText());
            }
            catch (Exception ignored) {
                storedWorkerCount = 0;
            }
            textWorkerCount.setText("");
        }
    }

    void selectInputFolders() {
        JFileChooser fc = Misc.createFileChooser();
        fc.setDialogTitle("Select Folders");
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(true);
        fc.setCurrentDirectory(new File(defaultPath));

        if (fc.showOpenDialog(this) != 0)
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
        String[] outStrings = new String[2];
        ArrayList<XlsxReader.SheetCells> sheets = Misc.openSpreadsheetForAppending(outStrings, null, defaultPath, this);
        if (sheets != null) {
            originalSheets = sheets;
            defaultPath = outStrings[1];
            textExcel.setText(outStrings[0]);
        }
    }

    void selectResultFolder() {
        JFileChooser fc = Misc.createFileChooser();
        fc.setDialogTitle("Select Folders");
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setCurrentDirectory(new File(defaultPath));

        if (fc.showOpenDialog(this) == 0) {
            File file = fc.getSelectedFile();
            if (file != null)
                textSaveResultsFolder.setText(file.getAbsolutePath());
        }
    }

    public BatchParameters buildBatchParamsFromUi()
    {
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

        boolean shouldOverrideWorkerCount = cbWorkerCount.isSelected();
        int workerCount = shouldOverrideWorkerCount ?
            Integer.parseInt(textWorkerCount.getText()) :
            getDefaultWorkerCount();

        return new BatchParameters(
            defaultPath,
            Misc.splitPaths(textInputFolders.getText(), ';', File.separatorChar),
            textExcel.getText(),
            shouldSaveImages,
            shouldUseSpecificOutputFolder,
            textSaveResultsFolder.getText(),
            textResultsImageFormat.getText(),
            shouldOverrideWorkerCount,
            workerCount
        );
    }

    public void startAnalysis()
    {
        if (analysisTaskFuture != null && !analysisTaskFuture.isDone()) {
            Misc.showDialogBox(
                "Analysis Still in Progress",
                "A batch analysis is still running. Either cancel it or wait for it to complete."
            );
            return;
        }

        nErrors = 0;

        AnalyzerParameters params;
        try {
            params = mainWindow.buildAnalyzerParamsFromUi();
        }
        catch (Throwable t) {
            Misc.showDialogBox("Parsing Error", "Invalid data in the analysis form (" + t.getClass().getSimpleName() + ")");
            return;
        }

        RefVector<String> errors = params.validate();

        BatchParameters batchParams;
        try {
            batchParams = buildBatchParamsFromUi();
        }
        catch (Throwable t) {
            Misc.showDialogBox("Parsing Error", "Invalid data in the batch form (" + t.getClass().getSimpleName() + ")");
            return;
        }

        RefVector<String> batchErrors = batchParams.validate();
        errors.extend(batchErrors);

        if (errors.size > 0) {
            int nErrors = errors.size;
            String header = nErrors > 1 ? ("There were " + nErrors + " errors:\n") : "";
            Misc.showDialogBox(
                "Validation Error" + (nErrors > 1 ? "s" : ""),
                header + errors.makeJoinedString("\n")
            );
            return;
        }

        cancelBtn.setEnabled(true);

        ATPreferences.savePreferences(params, AngioTool.PREFS_TXT);
        ATPreferences.savePreferences(batchParams, AngioTool.BATCH_TXT);

        analysisTaskFuture = (Future<Void>)Analyzer.threadPool.submit(
            () -> BatchProcessing.doBatchAnalysis(params, batchParams, BatchWindow.this, originalSheets)
        );
    }

    void updateWindowSize() {
        Dimension preferred = this.getPreferredSize();
        Dimension curSize = this.getSize();

        if (preferred.height > curSize.height)
            this.setSize(new Dimension(curSize.width, preferred.height));
    }

    @Override
    public boolean isClosed()
    {
        return this.isClosed.get();
    }

    @Override
    public void notifyNoImages()
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            overallLabel.setText("No images were found!");
            cancelBtn.setEnabled(false);

            updateWindowSize();
        });
    }

    @Override
    public void onEnumerationStart()
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            overallLabel.setText("Finding every image to be analyzed...");

            updateWindowSize();
        });
    }

    @Override
    public void onBatchStatsKnown(int nImages)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            overallLabel.setText("Analyzing images...");
            overallProgress.setValue(0);
            overallProgress.setMaximum(nImages);

            updateWindowSize();
        });
    }

    @Override
    public void notifyImageWasInvalid()
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            int nImages = overallProgress.getMaximum();
            overallProgress.setMaximum(nImages > 1 ? (nImages-1) : 1);
        });
    }

    @Override
    public void onImageDone(String path, Throwable error)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            if (error != null)
                nErrors++;

            int current = overallProgress.getValue() + 1;
            String status = "" + current + "/" + overallProgress.getMaximum();
            if (nErrors > 0)
                status += ", " + nErrors + (nErrors == 1 ? " error." : " errors.");

            overallLabel.setText(status);
            overallProgress.setValue(overallProgress.getValue() + 1);
            labelCurrentFile.setText("<html><p>" + path + "</p></html>");

            updateWindowSize();
        });
    }

    @Override
    public void onFinished(SpreadsheetWriter sw)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            if (sw.currentSheetIdx >= 0 && sw.currentSheetIdx < sw.sheets.size)
                originalSheets.add(new XlsxReader.SheetCells(sw.sheets.buf[sw.currentSheetIdx].valueRows));

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
            cancelBtn.setEnabled(false);

            updateWindowSize();

            final File xlsxFile = new File(sw.parentFolder, sw.fileName);
            Analyzer.threadPool.submit(() -> {
                try { Desktop.getDesktop().open(xlsxFile); }
                catch (IOException ex) { ex.printStackTrace(); }
            });
        });
    }

    public void cancel()
    {
        SwingUtilities.invokeLater(() -> {
            if (!isClosed.getAndSet(true))
                this.dispose();
        });
    }

    public static int getDefaultWorkerCount()
    {
        return (int)(Runtime.getRuntime().availableProcessors() * BALANCED_FACTOR);
    }
}
