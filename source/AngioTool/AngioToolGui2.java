package AngioTool;

import Utils.BatchUtils;
import Pixels.Rgb;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;

public class AngioToolGui2 extends JFrame
{
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

    public AngioToolGui2(AnalyzerParameters analyzerParams, BatchParameters batchParams)
    {
        labelAnalysis.setText("Analysis");
        BatchUtils.setNewFontSizeOn(labelAnalysis, 20);

        labelSkeletonizer.setText("Skeletonizer:");

        rbSkelFast.setText("Fast (Zha84)");
        rbSkelFast.setSelected(analyzerParams.shouldUseFastSkeletonizer);

        rbSkelThorough.setText("Thorough (Lee94)");
        rbSkelThorough.setSelected(!analyzerParams.shouldUseFastSkeletonizer);

        groupSkeletonizer.add(rbSkelFast);
        groupSkeletonizer.add(rbSkelThorough);

        /*{
            ButtonModel m = params.shouldUseFastSkeletonizer ? rbSkelFast.getModel() : rbSkelThorough.getModel();
            groupSaveResults.setSelected(m, true);
        }*/

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
        this.setSize(new Dimension(minSize.width + 32, minSize.height));
    }

    private void arrangeUi(GroupLayout layout)
    {
        layout.setHorizontalGroup(layout.createParallelGroup()
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
                BatchUtils.arrangeParallelEntries(
                    elemLinearScaleFactor, elemRemoveParticles, layout, BatchUtils.arrangeParallelEntries(
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
        );
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
