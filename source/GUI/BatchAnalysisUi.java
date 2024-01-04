package GUI;

import AngioTool.Analyzer;
import AngioTool.AnalyzerParameters;
import AngioTool.ATPreferences;
import AngioTool.SpreadsheetWriter;
import Utils.Utils;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatchAnalysisUi
{
    static int UNITS_GAP = 4;

    AnalyzerParameters params;

    final JFrame parentFrame;
    final JDialog jdialog;

    final JLabel labelData = new JLabel();
    final JLabel labelInputFolders = new JLabel();
    final JButton btnInputFolders = new JButton();
    final JTextField textInputFolders = new JTextField();
    final JLabel labelExcel = new JLabel();
    final JButton btnExcel = new JButton();
    final JTextField textExcel = new JTextField();
    final JCheckBox cbSaveResults = new JCheckBox();
    final JButton btnSaveResultsFolder = new JButton();
    final JTextField textSaveResultsFolder = new JTextField();
     
    final JLabel labelAnalysis = new JLabel();
    final NumberEntry elemResizeInputs;
    final NumberEntry elemLinearScaleFactor;
    final JCheckBox cbComputeLacunarity = new JCheckBox();
    final JCheckBox cbComputeThickness = new JCheckBox();
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
    final ColorSizeEntry elemBoundary;

    final JSeparator sepProgress = new JSeparator();
    final JLabel labelProgress = new JLabel();
    final JLabel overallLabel = new JLabel();
    final JProgressBar overallProgress = new JProgressBar();
    final JLabel imageLabel = new JLabel();
    final JProgressBar imageProgress = new JProgressBar();
    final JButton analyzeBtn = new JButton();
    final JButton cancelBtn = new JButton();

    int nErrors = 0;

    public Thread analysisThread = null;
    public final AtomicBoolean isClosed = new AtomicBoolean(false);

    public BatchAnalysisUi(JFrame uiFrame, AnalyzerParameters params)
    {
        this.parentFrame = uiFrame;
        this.params = params;

        this.jdialog = new JDialog(parentFrame, "Batch Analysis", true);

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

        cbSaveResults.setText("Save result images to:");
        cbSaveResults.setSelected(params.shouldSaveResultImages);
        cbSaveResults.addActionListener((ActionEvent e) -> {
            boolean enabled = cbSaveResults.isSelected();
            btnSaveResultsFolder.setEnabled(enabled);
            textSaveResultsFolder.setEnabled(enabled);
        });

        btnSaveResultsFolder.setIcon(Utils.ATOpenImageSmall);
        btnSaveResultsFolder.setEnabled(params.shouldSaveResultImages);
        btnSaveResultsFolder.addActionListener((ActionEvent e) -> BatchAnalysisUi.this.selectResultFolder());

        textSaveResultsFolder.setEnabled(params.shouldSaveResultImages);

        labelAnalysis.setText("Analysis");
        Utils.setNewFontSizeOn(labelAnalysis, 20);

        elemResizeInputs = new NumberEntry("Resize inputs by:", params.shouldResizeImage, params.resizingFactor, "x");

        elemLinearScaleFactor = new NumberEntry("Measurement Scale:", params.shouldApplyLinearScale, params.linearScalingFactor, "x");

        cbComputeLacunarity.setText("Lacunarity");
        cbComputeLacunarity.setSelected(params.shouldComputeLacunarity);

        cbComputeThickness.setText("Thickness");
        cbComputeThickness.setSelected(params.shouldComputeThickness);

        elemRemoveParticles = new NumberEntry("Remove Particles:", params.shouldRemoveSmallParticles, params.removeSmallParticlesThreshold, "px");

        elemFillHoles = new NumberEntry("Fill Holes:", params.shouldFillHoles, params.fillHolesValue, "px");

        labelSigmas.setText("Vessel Diameters list");

        textSigmas.setText(Utils.formatIntArray(params.sigmas));
        textSigmas.setToolTipText("List of sigmas (numbers)");

        labelIntensity.setText("Vessel Intensity range");

        textMinIntensity.setText("" + params.thresholdLow);
        textMaxIntensity.setText("" + params.thresholdHigh);

        labelOverlay.setText("Overlay");
        Utils.setNewFontSizeOn(labelOverlay, 20);

        elemOutline = new ColorSizeEntry("Outline:", params.shouldDrawOutline, params.outlineSize, params.outlineColor);
        elemBranches = new ColorSizeEntry("Branches:", params.shouldDrawBranchPoints, params.branchingPointsSize, params.branchingPointsColor);
        elemSkeleton = new ColorSizeEntry("Skeleton:", params.shouldDrawSkeleton, params.skeletonSize, params.skeletonColor);
        elemBoundary = new ColorSizeEntry("Boundary:", params.shouldDrawBoundary, params.boundarySize, params.boundaryColor);

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
            .addGroup(layout.createSequentialGroup()
                .addComponent(cbSaveResults)
                .addComponent(btnSaveResultsFolder)
                .addComponent(textSaveResultsFolder)
            )
            .addComponent(labelAnalysis)
            .addComponent(cbComputeLacunarity)
            .addComponent(cbComputeThickness)
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
                    elemBranches, elemBoundary, layout, arrangeParallelEntries(
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
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(cbSaveResults)
                .addComponent(btnSaveResultsFolder)
                .addComponent(textSaveResultsFolder, MIN_PATH_WIDTH, PATH_WIDTH, PATH_WIDTH)
            )
            .addGap(12)
            .addComponent(labelAnalysis)
            .addGap(8)
            .addComponent(cbComputeLacunarity)
            .addComponent(cbComputeThickness)
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
                elemBoundary.addToGroup(
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
        fc.setCurrentDirectory(new File(ATPreferences.settings.currentDir));

        if (fc.showOpenDialog(parentFrame) != 0)
            return;

        File[] folderList = fc.getSelectedFiles();
        if (folderList == null || folderList.length == 0)
            return;

        StringBuilder sb = new StringBuilder();
        for (File f : folderList) {
            if (!sb.isEmpty())
                sb.append(";");
            sb.append(f.getAbsolutePath().replace(";", "" + File.separatorChar + ";"));
        }

        textInputFolders.setText(sb.toString());
    }

    void selectExcelFile() {
        JFileChooser fc = createFileChooser();
        fc.setDialogTitle("Append to Excel spreadsheet");
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setCurrentDirectory(new File(ATPreferences.settings.currentDir));
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

        ATPreferences.settings.currentDir = fc.getCurrentDirectory().getAbsolutePath();

        File xlsxFile = fc.getSelectedFile();
        if (!Utils.hasAnyFileExtension(xlsxFile))
            xlsxFile = new File(xlsxFile.getAbsolutePath() + ".xlsx");

        if (xlsxFile.exists())
            SpreadsheetWriter.maybeBackup(xlsxFile);

        textExcel.setText(xlsxFile.getAbsolutePath());
    }

    void selectResultFolder() {
        JFileChooser fc = createFileChooser();
        fc.setDialogTitle("Select Folders");
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setCurrentDirectory(new File(ATPreferences.settings.currentDir));

        if (fc.showOpenDialog(parentFrame) == 0) {
            File file = fc.getSelectedFile();
            if (file != null)
                textSaveResultsFolder.setText(file.getAbsolutePath());
        }
    }

    AnalyzerParameters buildNewParamsFromUi() {
        return new AnalyzerParameters(
            Utils.splitPaths(textInputFolders.getText(), ';', File.separatorChar),
            textExcel.getText(),
            cbSaveResults.isSelected(),
            textSaveResultsFolder.getText(),
            elemResizeInputs.cb.isSelected(),
            elemResizeInputs.value,
            elemRemoveParticles.cb.isSelected(),
            elemRemoveParticles.value,
            elemFillHoles.cb.isSelected(),
            elemFillHoles.value,
            Utils.getSomeInts(textSigmas.getText()),
            Integer.parseInt(textMaxIntensity.getText()),
            Integer.parseInt(textMinIntensity.getText()),
            elemLinearScaleFactor.cb.isSelected(),
            elemLinearScaleFactor.value,
            elemOutline.cb.isSelected(),
            elemOutline.color,
            elemOutline.value,
            elemSkeleton.cb.isSelected(),
            elemSkeleton.color,
            elemSkeleton.value,
            elemBranches.cb.isSelected(),
            elemBranches.color,
            elemBranches.value,
            elemBoundary.cb.isSelected(),
            elemBoundary.color,
            elemBoundary.value,
            cbComputeLacunarity.isSelected(),
            cbComputeThickness.isSelected()
        );
    }

    public void startAnalysis()
    {
        if (analysisThread != null && analysisThread.isAlive()) {
            Utils.showDialogBox(
                "Analysis Still in Progress",
                "A batch analysis is still running. Either cancel it or wait for it to complete."
            );
            return;
        }

        try {
            params = buildNewParamsFromUi();
        }
        catch (Throwable t) {
            Utils.showDialogBox("Parsing Error", "Invalid data in the form (" + t.getClass().getSimpleName() + ")");
            return;
        }

        ArrayList<String> errors = params.validate();
        if (errors != null && !errors.isEmpty()) {
            int nErrors = errors.size();
            String header = nErrors > 1 ? ("There were " + nErrors + " errors:\n") : "";
            Utils.showDialogBox(
                "Validation Error" + (nErrors > 1 ? "s" : ""),
                header + String.join("\n", errors)
            );
            return;
        }

        analysisThread = new Thread(() -> Analyzer.doBatchAnalysis(params, BatchAnalysisUi.this));
        analysisThread.start();
    }

    public void notifyNoImages()
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            overallLabel.setText("No images were found!");
            cancelBtn.setText("OK");

            jdialog.setMinimumSize(jdialog.getPreferredSize());
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

            jdialog.setMinimumSize(jdialog.getPreferredSize());
        });
    }

    public void onStartImage(String absPath)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

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

            jdialog.setMinimumSize(jdialog.getPreferredSize());
        });
    }

    public void updateImageProgress(int newProgress, String statusMsg)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            imageLabel.setText(statusMsg);
            imageProgress.setValue(newProgress);

            jdialog.setMinimumSize(jdialog.getPreferredSize());
        });
    }

    public void onImageDone(Throwable error)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            if (error != null)
                nErrors++;

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
            cancelBtn.setText("OK");

            jdialog.setMinimumSize(jdialog.getPreferredSize());
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
        public double value;
        public final double originalValue;

        public NumberEntry(String name, boolean enabled, double value, String unitsStr)
        {
            this.name = name;
            this.value = value;
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
            if (enabled) {
                tf.setEnabled(true);
                tf.setText(Utils.formatDouble(value));
            }
            else {
                String str = tf.getText();
                try {
                    value = Double.parseDouble(str);
                }
                catch (Exception ex) {
                    value = originalValue;
                }
                tf.setEnabled(false);
            }
        }
    }

    static class ColorSizeEntry extends NumberEntry
    {
        public RoundedPanel panel;
        //public JButton btn;
        //public GroupLayout panelLayout;

        public Color color;
        public final Color originalColor;

        public ColorSizeEntry(String name, boolean enabled, double value, Color color)
        {
            super(name, enabled, value, "px");
            this.color = color;
            this.originalColor = color;

            //btn = new JButton();
            //btn.setContentAreaFilled(false);
            //btn.addActionListener((ActionEvent e) -> clickColorButton());

            panel = new RoundedPanel();
            panel.setCornerRadius(7);
            panel.setBackground(color);
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
                Color background = JColorChooser.showDialog(null, name, color);
                if (background != null) {
                    color = background;
                    panel.setBackground(color);
                }
            }
        }
    }
}
