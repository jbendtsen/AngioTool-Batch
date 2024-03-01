package AngioTool;

import Utils.BatchUtils;
import Pixels.Rgb;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AngioToolGui2 extends JFrame implements ActionListener
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

    public AngioToolGui2(AnalyzerParameters analyzerParams, BatchParameters batchParams)
    {
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

        textSigmas.setText(BatchUtils.formatDoubleArray(analyzerParams.sigmas));
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

        JPanel dialogPanel = new JPanel();
        GroupLayout layout = new GroupLayout(dialogPanel);
        dialogPanel.setLayout(layout);
        dialogPanel.setBorder(new EmptyBorder(0, 2, 12, 2));
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        arrangeUi(layout);

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
        this.setSize(new Dimension(minSize.width + 50, minSize.height));
    }

    private void initButton(JButton button, ImageIcon icon, String text)
    {
        button.setIcon(icon);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setText(text);
        button.addActionListener(this);
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
                    .addGroup(elemResizeInputs.addToGroup(layout.createSequentialGroup()))
                    .addGroup(elemFillHoles.addToGroup(layout.createSequentialGroup()))
                )
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup()
                    .addComponent(labelSkeletonizer)
                    .addComponent(rbSkelFast)
                    .addComponent(rbSkelThorough)
                    .addGroup(elemLinearScaleFactor.addToGroup(layout.createSequentialGroup()))
                    .addGroup(elemRemoveParticles.addToGroup(layout.createSequentialGroup()))
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
        );

        final int MIN_PATH_WIDTH = 18;
        final int PATH_WIDTH = 30;

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
                elemLinearScaleFactor.addToGroup(
                    elemResizeInputs.addToGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGroup(
                elemFillHoles.addToGroup(
                    elemRemoveParticles.addToGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
                )
            )
            .addGap(12)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelSigmas)
                .addComponent(labelIntensity)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(textSigmas)
                .addComponent(textMinIntensity)
                .addComponent(textMaxIntensity)
            )
            .addGap(20)
            .addComponent(labelOverlay)
            .addGap(12)
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
        );
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        Object source = evt.getSource();
        if (source == btnLoadImage)
            openImage();
        else if (source == btnStartBatch)
            openBatchWindow();
        else if (source == btnHelp)
            openHelpWindow();
    }

    void openImage()
    {
        BatchUtils.showDialogBox("View", "Open image");
    }

    void openBatchWindow()
    {
        BatchUtils.showDialogBox("Batch", "Open batch window");
    }

    void openHelpWindow()
    {
        BatchUtils.showDialogBox("Help", "Content goes here");
    }

    public AnalyzerParameters buildAnalyzerParamsFromUi()
    {
        ButtonModel skelType = groupSkeletonizer.getSelection();
        boolean shouldUseFastSkel = skelType == rbSkelFast.getModel();

        return new AnalyzerParameters(
            elemResizeInputs.cb.isSelected(),
            elemResizeInputs.getValue(),
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
            true,  // shouldIsolateBrightestChannelInOutput
            cbComputeLacunarity.isSelected(),
            cbComputeThickness.isSelected()
        );
    }
}
