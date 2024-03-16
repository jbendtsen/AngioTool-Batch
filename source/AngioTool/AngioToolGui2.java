package AngioTool;

import Utils.*;
import Pixels.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AngioToolGui2 extends JFrame implements ColorSizeEntry.Listener, ActionListener, FocusListener, KeyListener
{
    final JButton btnLoadImage = new JButton();
    final JButton btnStartBatch = new JButton();
    final JButton btnHelp = new JButton();

    final JLabel labelAnalysis = new JLabel();
    final JCheckBox cbComputeLacunarity = new JCheckBox();
    final JCheckBox cbComputeThickness = new JCheckBox();
    final JLabel labelSkeletonizer = new JLabel();
    final ButtonGroup groupSkeletonizer = new ButtonGroup();
    final JRadioButton rbSkelFast = new JRadioButton();
    final JRadioButton rbSkelThorough = new JRadioButton();
    final NumberEntry elemMaxSkelIterations;
    final NumberEntry elemResizeInputs;
    final NumberEntry elemLinearScaleFactor;
    final NumberEntry elemRemoveParticles;
    final NumberEntry elemFillHoles;
    final NumberEntry elemMaxHoleLevelPercent;
    final NumberEntry elemMinBoxnessPercent;
    final NumberEntry elemMinAreaLengthRatio;
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
    final ButtonGroup groupImageRecolor = new ButtonGroup();
    final JRadioButton rbImageOriginal = new JRadioButton();
    final JRadioButton rbImageIsolated = new JRadioButton();
    final JRadioButton rbImageGray = new JRadioButton();
    final JButton btnReset = new JButton();
    final JLabel labelMemory = new JLabel();

    AnalyzerParameters latestAnalyzerParams = null;
    String defaultPath;

    public final LinkedList<ImagingWindow> imagingWindows = new LinkedList<>();
    byte[] helpHtmlData;

    public AngioToolGui2(AnalyzerParameters analyzerParams, String defaultPath)
    {
        super(AngioTool.VERSION);
        this.defaultPath = defaultPath;

        this.setIconImage(AngioTool.ATIcon.getImage());

        initButton(btnLoadImage, AngioTool.ATFolder, "View");
        initButton(btnStartBatch, AngioTool.ATBatch, "Batch");
        initButton(btnHelp, AngioTool.ATHelp, "Help");

        labelAnalysis.setText("Analysis");
        BatchUtils.setNewFontSizeOn(labelAnalysis, 20);

        labelSkeletonizer.setText("Skeletonizer:");

        rbSkelFast.setText("Fast (Zha84)");
        rbSkelFast.setSelected(analyzerParams.shouldUseFastSkeletonizer);

        rbSkelThorough.setText("Thorough (Lee94)");
        rbSkelThorough.setSelected(!analyzerParams.shouldUseFastSkeletonizer);

        groupSkeletonizer.add(rbSkelFast);
        groupSkeletonizer.add(rbSkelThorough);

        cbComputeLacunarity.setText("Lacunarity");
        cbComputeLacunarity.setSelected(analyzerParams.shouldComputeLacunarity);

        cbComputeThickness.setText("Thickness");
        cbComputeThickness.setSelected(analyzerParams.shouldComputeThickness);

        elemMaxSkelIterations = new NumberEntry("Max Skeleton Steps:", analyzerParams.shouldCapSkelIterations, analyzerParams.maxSkelIterations, "");

        elemResizeInputs = new NumberEntry("Resize Inputs by:", analyzerParams.shouldResizeImage, analyzerParams.resizingFactor, "x");

        elemLinearScaleFactor = new NumberEntry("Measurement Scale:", analyzerParams.shouldApplyLinearScale, analyzerParams.linearScalingFactor, "x");

        elemRemoveParticles = new NumberEntry("Remove Particles:", analyzerParams.shouldRemoveSmallParticles, analyzerParams.removeSmallParticlesThreshold, "px");

        elemFillHoles = new NumberEntry("Fill Holes:", analyzerParams.shouldFillHoles, analyzerParams.fillHolesValue, "px");

        labelSigmas.setText("Vessel Diameters list");

        textSigmas.setText(BatchUtils.formatDoubleArray(analyzerParams.sigmas, "12"));
        textSigmas.setToolTipText("List of Sigmas (numbers)");

        labelIntensity.setText("Vessel Intensity range");

        textMinIntensity.setText("" + analyzerParams.thresholdLow);
        textMaxIntensity.setText("" + analyzerParams.thresholdHigh);

        labelOverlay.setText("Overlay");
        BatchUtils.setNewFontSizeOn(labelOverlay, 20);

        elemOutline = new ColorSizeEntry("Outline:", analyzerParams.shouldDrawOutline, analyzerParams.outlineSize, analyzerParams.outlineColor);
        elemBranches = new ColorSizeEntry("Branches:", analyzerParams.shouldDrawBranchPoints, analyzerParams.branchingPointsSize, analyzerParams.branchingPointsColor);
        elemSkeleton = new ColorSizeEntry("Skeleton:", analyzerParams.shouldDrawSkeleton, analyzerParams.skeletonSize, analyzerParams.skeletonColor);
        elemConvexHull = new ColorSizeEntry("Convex Hull:", analyzerParams.shouldDrawConvexHull, analyzerParams.convexHullSize, analyzerParams.convexHullColor);

        elemOutline.setColorChangeListener(this);
        elemBranches.setColorChangeListener(this);
        elemSkeleton.setColorChangeListener(this);
        elemConvexHull.setColorChangeListener(this);

        elemMaxHoleLevelPercent = new NumberEntry("Max Hole Level:", analyzerParams.shouldFillBrightShapes, 100.0 * analyzerParams.brightShapeThresholdFactor, "%");
        elemMinBoxnessPercent = new NumberEntry("Min Boxness:", analyzerParams.shouldApplyMinBoxness, 100.0 * analyzerParams.minBoxness, "%");
        elemMinAreaLengthRatio = new NumberEntry("Min Length : Area:", analyzerParams.shouldApplyMinAreaLength, analyzerParams.minAreaLengthRatio, "1 :");

        rbImageOriginal.setText("Keep Original Colors");
        rbImageOriginal.setSelected(!analyzerParams.shouldIsolateBrightestChannelInOutput);

        rbImageIsolated.setText("Isolate Brightest Channel");
        rbImageIsolated.setSelected(analyzerParams.shouldIsolateBrightestChannelInOutput && !analyzerParams.shouldExpandOutputToGrayScale);

        rbImageGray.setText("Convert to Grayscale");
        rbImageGray.setSelected(analyzerParams.shouldIsolateBrightestChannelInOutput && analyzerParams.shouldExpandOutputToGrayScale);

        groupImageRecolor.add(rbImageOriginal);
        groupImageRecolor.add(rbImageIsolated);
        groupImageRecolor.add(rbImageGray);

        btnReset.setText("Reset To Defaults");

        updateMemoryMonitor();

        JPanel dialogPanel = new JPanel();
        GroupLayout layout = new GroupLayout(dialogPanel);
        dialogPanel.setLayout(layout);
        dialogPanel.setBorder(new EmptyBorder(0, 2, 4, 2));
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        arrangeUi(layout);

        int nComponents = dialogPanel.getComponentCount();
        for (int i = 0; i < nComponents; i++) {
            Component elem = dialogPanel.getComponent(i);
            if (elem instanceof AbstractButton)
                ((AbstractButton)elem).addActionListener(this);
            else
                elem.addFocusListener(this);

            if (elem instanceof JTextField)
                elem.addKeyListener(this);
        }

        dialogPanel.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent evt) {
                maybeUpdateImagingWindows();
            }

            @Override public void mouseClicked(MouseEvent evt) {}
            @Override public void mouseEntered(MouseEvent evt) {}
            @Override public void mouseExited(MouseEvent evt) {}
            @Override public void mouseReleased(MouseEvent evt) {}
        });

        Container container = getContentPane();
        container.add(dialogPanel);
        this.pack();

        this.setDefaultCloseOperation(0);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                ATPreferences.savePreferences(buildAnalyzerParamsFromUi(), AngioTool.PREFS_TXT);
                AngioToolGui2.this.setVisible(false);
                System.exit(0);
            }
        });

        Dimension minSize = this.getPreferredSize();
        this.setMinimumSize(minSize);
        this.setSize(new Dimension(minSize.width + 50, minSize.height + 10));
    }

    void setAnalysisUi(AnalyzerParameters analyzerParams)
    {
        rbSkelFast.setSelected(analyzerParams.shouldUseFastSkeletonizer);
        rbSkelThorough.setSelected(!analyzerParams.shouldUseFastSkeletonizer);
        cbComputeLacunarity.setSelected(analyzerParams.shouldComputeLacunarity);
        cbComputeThickness.setSelected(analyzerParams.shouldComputeThickness);

        elemMaxSkelIterations.update(analyzerParams.shouldCapSkelIterations, analyzerParams.maxSkelIterations);
        elemResizeInputs.update(analyzerParams.shouldResizeImage, analyzerParams.resizingFactor);
        elemLinearScaleFactor.update(analyzerParams.shouldApplyLinearScale, analyzerParams.linearScalingFactor);
        elemRemoveParticles.update(analyzerParams.shouldRemoveSmallParticles, analyzerParams.removeSmallParticlesThreshold);
        elemFillHoles.update(analyzerParams.shouldFillHoles, analyzerParams.fillHolesValue);

        textSigmas.setText(BatchUtils.formatDoubleArray(analyzerParams.sigmas, "12"));
        textMinIntensity.setText("" + analyzerParams.thresholdLow);
        textMaxIntensity.setText("" + analyzerParams.thresholdHigh);

        elemOutline.update(analyzerParams.shouldDrawOutline, analyzerParams.outlineSize, analyzerParams.outlineColor);
        elemBranches.update(analyzerParams.shouldDrawBranchPoints, analyzerParams.branchingPointsSize, analyzerParams.branchingPointsColor);
        elemSkeleton.update(analyzerParams.shouldDrawSkeleton, analyzerParams.skeletonSize, analyzerParams.skeletonColor);
        elemConvexHull.update(analyzerParams.shouldDrawConvexHull, analyzerParams.convexHullSize, analyzerParams.convexHullColor);

        elemMaxHoleLevelPercent.update(analyzerParams.shouldFillBrightShapes, 100.0 * analyzerParams.brightShapeThresholdFactor);
        elemMinBoxnessPercent.update(analyzerParams.shouldApplyMinBoxness, 100.0 * analyzerParams.minBoxness);
        elemMinAreaLengthRatio.update(analyzerParams.shouldApplyMinAreaLength, analyzerParams.minAreaLengthRatio);

        rbImageOriginal.setSelected(!analyzerParams.shouldIsolateBrightestChannelInOutput);
        rbImageIsolated.setSelected(analyzerParams.shouldIsolateBrightestChannelInOutput && !analyzerParams.shouldExpandOutputToGrayScale);
        rbImageGray.setSelected(analyzerParams.shouldIsolateBrightestChannelInOutput && analyzerParams.shouldExpandOutputToGrayScale);

        maybeUpdateImagingWindows();
    }

    private void initButton(JButton button, ImageIcon icon, String text)
    {
        button.setIcon(icon);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setText(text);
    }

    private void arrangeUi(GroupLayout layout)
    {
        layout.setHorizontalGroup(layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup()
                .addComponent(btnLoadImage)
                .addComponent(btnStartBatch)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnHelp)
            )
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(labelAnalysis)
                    .addGroup(elemFillHoles.addToSeqGroup(layout.createSequentialGroup()))
                    .addGroup(elemRemoveParticles.addToSeqGroup(layout.createSequentialGroup()))
                    .addGroup(elemMaxHoleLevelPercent.addToSeqGroup(layout.createSequentialGroup()))
                    .addGroup(elemMinBoxnessPercent.addToSeqGroup(layout.createSequentialGroup()))
                    .addGroup(elemMinAreaLengthRatio.addToSeqGroup(layout.createSequentialGroup()))
                )
                .addGap(16, 16, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbComputeLacunarity)
                        .addGap(16)
                        .addComponent(cbComputeThickness)
                    )
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4)
                        .addComponent(labelSkeletonizer)
                        .addGroup(layout.createParallelGroup()
                            .addComponent(rbSkelFast)
                            .addComponent(rbSkelThorough)
                        )
                    )
                    .addGroup(elemMaxSkelIterations.addToSeqGroup(layout.createSequentialGroup()))
                    .addGroup(elemResizeInputs.addToSeqGroup(layout.createSequentialGroup()))
                    .addGroup(elemLinearScaleFactor.addToSeqGroup(layout.createSequentialGroup()))
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
                BatchUtils.arrangeParallelEntries(
                    elemBranches, elemConvexHull, layout, BatchUtils.arrangeParallelEntries(
                        elemOutline, elemSkeleton, layout, layout.createSequentialGroup()
                    ).addGap(20)
                )
            )
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(btnReset)
                    .addComponent(labelMemory)
                )
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup()
                    .addComponent(rbImageOriginal)
                    .addComponent(rbImageIsolated)
                    .addComponent(rbImageGray)
                )
            )
        );

        final int MIN_TEXT_HEIGHT = 18;
        final int TEXT_HEIGHT = 24;

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(btnLoadImage)
                .addComponent(btnStartBatch)
                .addComponent(btnHelp)
            )
            .addGap(20)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelAnalysis)
                .addComponent(cbComputeLacunarity)
                .addComponent(cbComputeThickness)
            )
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(elemFillHoles.addToParaGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)))
                    .addGroup(elemRemoveParticles.addToParaGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)))
                )
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 16)
                    .addComponent(labelSkeletonizer)
                    .addGap(0, 0, 16)
                )
                .addGroup(layout.createSequentialGroup()
                    .addComponent(rbSkelFast)
                    .addComponent(rbSkelThorough)
                )
            )
            .addGroup(
                elemMaxSkelIterations.addToParaGroup(
                    elemMaxHoleLevelPercent.addToParaGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGroup(
                elemResizeInputs.addToParaGroup(
                    elemMinBoxnessPercent.addToParaGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGroup(
                elemLinearScaleFactor.addToParaGroup(
                    elemMinAreaLengthRatio.addToParaGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGap(12)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelSigmas)
                .addComponent(labelIntensity)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(textSigmas, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
                .addComponent(textMinIntensity, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
                .addComponent(textMaxIntensity, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
            )
            .addGap(20)
            .addComponent(labelOverlay)
            .addGap(12)
            .addGroup(
                elemBranches.addToParaGroup(
                    elemOutline.addToParaGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGroup(
                elemConvexHull.addToParaGroup(
                    elemSkeleton.addToParaGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGap(8)
            .addComponent(rbImageOriginal)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(btnReset)
                .addComponent(rbImageIsolated)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelMemory)
                .addComponent(rbImageGray)
            )
        );
    }

    @Override
    public void onColorChanged(ColorSizeEntry colorElem)
    {
        maybeUpdateImagingWindows();
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        Object source = evt.getSource();
        if (source == btnLoadImage)
            openImageThenImagingWindow();
        else if (source == btnStartBatch)
            openBatchWindow();
        else if (source == btnHelp)
            openHelpWindow();
        else if (source == btnReset)
            setAnalysisUi(AnalyzerParameters.defaults());
        else
            maybeUpdateImagingWindows();
    }

    @Override
    public void focusGained(FocusEvent evt)
    {
        // ...
    }

    @Override
    public void focusLost(FocusEvent evt)
    {
        maybeUpdateImagingWindows();
    }

    @Override
    public void keyPressed(KeyEvent evt)
    {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
            maybeUpdateImagingWindows();
    }

    @Override public void keyReleased(KeyEvent evt) {}
    @Override public void keyTyped(KeyEvent evt) {}

    void openImageThenImagingWindow()
    {
        File imageFile = openImageFile();
        if (imageFile == null)
            return;

        ArgbBuffer image = null;
        Exception error = null;
        try {
            image = ImageFile.acquireImageForAnalysis(
                imageFile.getAbsolutePath(),
                elemResizeInputs.cb.isSelected() ? elemResizeInputs.getValue() : 1.0
            );
        }
        catch (Exception ex) {
            error = ex;
        }

        if (image == null) {
            BatchUtils.showDialogBox(
                "Failed to analyze image",
                error != null ?
                    BatchUtils.buildDialogMessageFromException(error) :
                    "Image file could not be read"
            );
            return;
        }

        imagingWindows.add(new ImagingWindow(this, image, imageFile, defaultPath).showDialog());

        updateMemoryMonitor();
    }

    void openBatchWindow()
    {
        BatchParameters batchParams = BatchParameters.defaults();
        try {
            ATPreferences.loadPreferences(batchParams, AngioTool.class, AngioTool.BATCH_TXT);
        }
        catch (Exception ignored) {}

        new BatchWindow(this, batchParams).showDialog();
        updateMemoryMonitor();
    }

    void openHelpWindow()
    {
        if (helpHtmlData == null) {
            try (InputStream in = getClass().getResourceAsStream("/manual.html")) {
                ByteVectorOutputStream vec = BatchUtils.readFullyAsVector(in);
                this.helpHtmlData = vec.copy();
            }
            catch (Exception ex) {
                BatchUtils.showExceptionInDialogBox(ex);
                return;
            }
        }

        new HelpWindow(this, this.helpHtmlData).showDialog();
        updateMemoryMonitor();
    }

    File openImageFile()
    {
        JFileChooser fc = BatchUtils.createFileChooser();
        fc.setDialogTitle("Open Image to View/Analyze");
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setCurrentDirectory(new File(defaultPath));
        return fc.showOpenDialog(this) == 0 ? fc.getSelectedFile() : null;
    }

    void maybeUpdateImagingWindows()
    {
        AnalyzerParameters params = buildAnalyzerParamsFromUi();

        if (latestAnalyzerParams != null && params.equals(latestAnalyzerParams)) {
            updateMemoryMonitor();
            return;
        }

        latestAnalyzerParams = params;

        for (ImagingWindow iw : imagingWindows)
            iw.updateImage(params);

        updateMemoryMonitor();
    }

    public void closeImagingWindow(ImagingWindow window)
    {
        window.release();
        imagingWindows.remove(window);
    }

    public AnalyzerParameters buildAnalyzerParamsFromUi()
    {
        ButtonModel skelType = groupSkeletonizer.getSelection();
        boolean shouldUseFastSkel = skelType == rbSkelFast.getModel();

        ButtonModel recolorType = groupImageRecolor.getSelection();
        boolean shouldIsolateChannel;
        boolean shouldExpandToGrayScale;
        if (recolorType == rbImageOriginal.getModel()) {
            shouldIsolateChannel = false;
            shouldExpandToGrayScale = false;
        }
        else if (recolorType == rbImageGray.getModel()) {
            shouldIsolateChannel = true;
            shouldExpandToGrayScale = true;
        }
        else {
            shouldIsolateChannel = true;
            shouldExpandToGrayScale = false;
        }

        return new AnalyzerParameters(
            elemResizeInputs.cb.isSelected(),
            elemResizeInputs.getValue(),
            elemMaxHoleLevelPercent.cb.isSelected(),
            elemMaxHoleLevelPercent.getValue() / 100.0,
            elemMinBoxnessPercent.cb.isSelected(),
            elemMinBoxnessPercent.getValue() / 100.0,
            elemMinAreaLengthRatio.cb.isSelected(),
            elemMinAreaLengthRatio.getValue(),
            elemRemoveParticles.cb.isSelected(),
            elemRemoveParticles.getValue(),
            elemFillHoles.cb.isSelected(),
            elemFillHoles.getValue(),
            BatchUtils.getSomeDoubles(textSigmas.getText()),
            Integer.parseInt(textMaxIntensity.getText()),
            Integer.parseInt(textMinIntensity.getText()),
            shouldUseFastSkel,
            elemMaxSkelIterations.cb.isSelected(),
            (int)elemMaxSkelIterations.getValue(),
            elemLinearScaleFactor.cb.isSelected(),
            elemLinearScaleFactor.getValue(),
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
            shouldIsolateChannel,
            shouldExpandToGrayScale,
            cbComputeLacunarity.isSelected(),
            cbComputeThickness.isSelected()
        );
    }

    public void updateMemoryMonitor()
    {
        Runtime rt = Runtime.getRuntime();
        long maxMB  = (rt.maxMemory() + (1L << 19)) >> 20L;
        long usedMB = (rt.totalMemory() - rt.freeMemory() + (1L << 19)) >> 20L;

        labelMemory.setText("Used MB: " + usedMB + " / " + maxMB);
    }
}
