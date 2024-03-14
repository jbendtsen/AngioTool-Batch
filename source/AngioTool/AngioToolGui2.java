package AngioTool;

import Utils.BatchUtils;
import Pixels.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AngioToolGui2 extends JFrame implements ActionListener, FocusListener, KeyListener
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
    final NumberEntry elemFillBrightShapes;
    final ButtonGroup groupImageRecolor = new ButtonGroup();
    final JRadioButton rbImageOriginal = new JRadioButton();
    final JRadioButton rbImageIsolated = new JRadioButton();
    final JRadioButton rbImageGray = new JRadioButton();

    final JLabel labelMemory = new JLabel();

    AnalyzerParameters latestAnalyzerParams = null;
    String defaultPath;

    public final LinkedList<ImagingWindow> imagingWindows = new LinkedList<>();

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

        elemResizeInputs = new NumberEntry("Resize inputs by:", analyzerParams.shouldResizeImage, analyzerParams.resizingFactor, "x");

        elemLinearScaleFactor = new NumberEntry("Measurement Scale:", analyzerParams.shouldApplyLinearScale, analyzerParams.linearScalingFactor, "x");

        elemRemoveParticles = new NumberEntry("Remove Particles:", analyzerParams.shouldRemoveSmallParticles, analyzerParams.removeSmallParticlesThreshold, "px");

        elemFillHoles = new NumberEntry("Fill Holes:", analyzerParams.shouldFillHoles, analyzerParams.fillHolesValue, "px");

        labelSigmas.setText("Vessel Diameters list");

        textSigmas.setText(BatchUtils.formatDoubleArray(analyzerParams.sigmas, "12"));
        textSigmas.setToolTipText("List of sigmas (numbers)");

        labelIntensity.setText("Vessel Intensity range");

        textMinIntensity.setText("" + analyzerParams.thresholdLow);
        textMaxIntensity.setText("" + analyzerParams.thresholdHigh);

        labelOverlay.setText("Overlay");
        BatchUtils.setNewFontSizeOn(labelOverlay, 20);

        elemOutline = new ColorSizeEntry("Outline:", analyzerParams.shouldDrawOutline, analyzerParams.outlineSize, analyzerParams.outlineColor);
        elemBranches = new ColorSizeEntry("Branches:", analyzerParams.shouldDrawBranchPoints, analyzerParams.branchingPointsSize, analyzerParams.branchingPointsColor);
        elemSkeleton = new ColorSizeEntry("Skeleton:", analyzerParams.shouldDrawSkeleton, analyzerParams.skeletonSize, analyzerParams.skeletonColor);
        elemConvexHull = new ColorSizeEntry("Convex Hull:", analyzerParams.shouldDrawConvexHull, analyzerParams.convexHullSize, analyzerParams.convexHullColor);

        elemFillBrightShapes = new NumberEntry("Max Hole Level:", analyzerParams.shouldFillBrightShapes, analyzerParams.brightShapeThresholdFactor, "x");

        rbImageOriginal.setText("Keep Original Colors");
        rbImageOriginal.setSelected(!analyzerParams.shouldIsolateBrightestChannelInOutput);

        rbImageIsolated.setText("Isolate Brightest Channel");
        rbImageIsolated.setSelected(analyzerParams.shouldIsolateBrightestChannelInOutput && !analyzerParams.shouldExpandOutputToGrayScale);

        rbImageGray.setText("Convert to Grayscale");
        rbImageGray.setSelected(analyzerParams.shouldIsolateBrightestChannelInOutput && analyzerParams.shouldExpandOutputToGrayScale);

        groupImageRecolor.add(rbImageOriginal);
        groupImageRecolor.add(rbImageIsolated);
        groupImageRecolor.add(rbImageGray);

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
            .addComponent(labelAnalysis)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(cbComputeLacunarity)
                    .addComponent(cbComputeThickness)
                    .addGroup(elemFillHoles.addToSeqGroup(layout.createSequentialGroup()))
                    .addGroup(elemRemoveParticles.addToSeqGroup(layout.createSequentialGroup()))
                    .addGroup(elemFillBrightShapes.addToSeqGroup(layout.createSequentialGroup()))
                )
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup()
                    .addComponent(labelSkeletonizer)
                    .addComponent(rbSkelFast)
                    .addComponent(rbSkelThorough)
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
                .addComponent(labelMemory)
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
            .addComponent(labelAnalysis)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelSkeletonizer)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(cbComputeLacunarity)
                .addComponent(rbSkelFast)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(cbComputeThickness)
                .addComponent(rbSkelThorough)
            )
            .addGap(12)
            .addGroup(
                elemResizeInputs.addToParaGroup(
                    elemFillHoles.addToParaGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGroup(
                elemLinearScaleFactor.addToParaGroup(
                    elemRemoveParticles.addToParaGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGroup(
                elemFillBrightShapes.addToParaGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
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
            .addComponent(rbImageIsolated)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelMemory)
                .addComponent(rbImageGray)
            )
        );
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
                null,
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
        BatchParameters batchParams = new BatchParameters();
        try {
            ATPreferences.load(batchParams, AngioTool.class, AngioTool.BATCH_TXT);
        }
        catch (Exception ex) {
            batchParams = BatchParameters.defaults();
        }

        new BatchWindow(this, batchParams).showDialog();
        updateMemoryMonitor();
    }

    void openHelpWindow()
    {
        BatchUtils.showDialogBox("Help", "Content goes here");
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
        imagingWindows.remove(window);
        window.release();
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
            elemFillBrightShapes.cb.isSelected(),
            elemFillBrightShapes.getValue(),
            elemRemoveParticles.cb.isSelected(),
            elemRemoveParticles.getValue(),
            elemFillHoles.cb.isSelected(),
            elemFillHoles.getValue(),
            BatchUtils.getSomeDoubles(textSigmas.getText()),
            Integer.parseInt(textMaxIntensity.getText()),
            Integer.parseInt(textMinIntensity.getText()),
            shouldUseFastSkel,
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
