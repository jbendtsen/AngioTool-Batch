package GUI;

import AnalyzeSkeleton.AnalyzeSkeleton;
import AnalyzeSkeleton.Edge;
import AnalyzeSkeleton.Graph;
import AnalyzeSkeleton.Point;
import AnalyzeSkeleton.SkeletonResult;
import AngioTool.AngioTool;
import AngioTool.ATPreferences;
import AngioTool.MemoryMonitor;
import AngioTool.PolygonPlus;
import AngioTool.RGBStackSplitter;
import AngioTool.ReachableTest;
import AngioTool.Results;
import AngioTool.SaveToExcel;
import Batch.Analyzer;
import Batch.AnalyzerParameters;
import Batch.BatchAnalysisUi;
import Lacunarity.Lacunarity;
import Utils.ForkShapeRoiSplines;
import Utils.Utils;
//import com.jidesoft.swing.RangeSlider;
import features.Tubeness;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import kz.swing.markSlider;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;
import vesselThickness.EDT_S1D;

public class AngioToolGUI extends JFrame implements KeyListener, MouseListener {
   private Image imgIcon;
   private File currentDir;
   private File imageFile;
   private File[] imageFiles;
   private SaveToExcel ste;
   private boolean doScaling = false;
   private boolean fillHoles = false;
   private boolean smallParticles = false;
   private double resizingFactor = 1.0;
   private boolean sigmaIsChanged = false;
   private boolean fillHolesIsChanged = false;
   private boolean smallParticlesIsChanged = false;
   private boolean hideOverlay = false;
   private ImagePlus imageOriginal;
   private ImagePlus imageResult;
   private ImagePlus imageThresholded;
   private ImagePlus imageTubeness;
   private ImageProcessor ipOriginal;
   private ImageProcessor ipThresholded;
   private ImageProcessor tubenessIp;
   private int minSigma;
   private int maxSigma;
   private double[] firstSigma = new double[]{5.0};
   private ArrayList<Double> allSigmas;
   private ArrayList<Double> currentSigmas;
   private ArrayList<AngioToolGUI.sigmaImages> sI;
   private ArrayList<int[]> al;
   private ArrayList<Point> al2;
   private ArrayList<Point> removedJunctions;
   private ArrayList<Point> endPoints;
   private PolygonPlus convexHull;
   private double convexHullArea;
   private Overlay allantoisOverlay;
   private Roi outlineRoi;
   private PolygonRoi convexHullRoi;
   private ArrayList<Roi> skeletonRoi;
   private ArrayList<Roi> junctionsRoi;
   private long thresholdedPixelArea = 0L;
   private Graph[] graph;
   private boolean showOverlay;
   private boolean showOutline;
   private boolean showSkeleton;
   private boolean showBranchingPoints;
   private boolean showConvexHull;
   private int OutlineStrokeWidth;
   private int SkeletonStrokeWidth;
   private int BranchingPointsStrokeWidth;
   private int ConvexHullStrokeWidth;
   private Color OutlineColor;
   private Color SkeletonColor;
   private Color BranchingPointColor;
   private Color ConvexHullColor;
   private String imageResultFormat = "jpg";
   private AngioToolAboutBox aboutBox;
   public static java.awt.Point ATAboutBoxLoc;
   private Icon lockedIcon;
   private Icon unlockedIcon;
   Date startDate;
   Date stopDate;
   ImagePlus imageThickness;
   SkeletonResult skelResult;
   private boolean computeLacunarity = true;
   private double ElSlope;
   private double medialELacunarity;
   private double FlSlope;
   private double medialFLacunarity;
   private int[] lacunarityBoxes;
   private ArrayList<Double> Elamdas = new ArrayList<>();
   private ArrayList<Double> Flamdas = new ArrayList<>();
   private double meanEl;
   private double meanFl;
   private boolean computeThickness = true;
   private double LinearScalingFactor = 0.0;
   private double AreaScalingFactor = 0.0;
   private String outputString;
   private Results results;
   private JLabel batchStatusLabel;
   private JPanel AnalysisTabPanel;
   private JButton BatchButton;
   private JButton AnalyzeButton;
   //private JButton ExitButton;
   private JTabbedPane TabbedPane;
   private JPanel backgroundParticlesPanel;
   private JButton branchinPointsColorButton;
   private JLabel branchingPointsLabel;
   private RoundedPanel branchingPointsRoundedPanel;
   private JSpinner branchingPointsSpinner;
   private JButton clearCalibrationButton;
   private JButton convexHullColorButton;
   private JLabel convexHullLabel;
   private RoundedPanel convexHullRoundedPanel;
   private JSpinner convexHullSizeSpinner;
   private JLabel distanceInMMLabel;
   private JNumberTextField distanceInMMNumberTextField;
   private JLabel distanceInPixelsLabel;
   private JNumberTextField distanceInPixelsNumberTextField;
   private JCheckBox fillHolesCheckBox;
   //private RangeSlider fillHolesRangeSlider;
   private JSlider fillHolesRangeSliderLow;
   private JSlider fillHolesRangeSliderHigh;
   private JSlider fillHolesRangeSlider2;
   private JSpinner fillHolesSpinner;
   private JButton helpButton;
   private JTextField highThresholdTextField;
   private JComboBox imageResulFormatComboBox;
   private JTextField lowThresholdTextField;
   private TransparentTextField memTransparentTextField;
   private JButton openImageButton;
   private JButton outlineColorButton;
   private JLabel outlineLabel;
   private RoundedPanel outlineRoundedPanel;
   private JSpinner outlineSpinner;
   private JPanel overlaySettingsPanel;
   private static JProgressBar progressBar;
   private JSpinner removeSmallParticlesSpinner;
   private JCheckBox resizeImageCheckBox;
   private JPanel resizeImagePanel;
   private JLabel resizingFactorLabel;
   private JSpinner resizingFactorSpinner;
   private JButton saveImageButton;
   private JCheckBox saveResultImageCheckBox;
   private JButton saveResultsToButton;
   private JTextField saveResultsToTextField;
   private JPanel savingPreferencesPanel;
   private JLabel scaleLabel;
   private JTextField scaleTextField;
   private JPanel setScalePanel;
   private JPanel settingsTabPanel;
   private JCheckBox showBranchingPointsCheckBox;
   private JCheckBox showConvexHullCheckBox;
   private JCheckBox showOutlineCheckBox;
   private JCheckBox showOverlayCheckBox;
   private JCheckBox showSkeletonCheckBox;
   private markSlider sigmasMarkSlider;
   private JSpinner sigmasSpinner;
   private JButton skeletonColorButton;
   private RoundedPanel skeletonColorRoundedPanel;
   private JLabel skeletonLabel;
   private JSpinner skeletonSpinner;
   private JCheckBox smallParticlesCheckBox;
   //private RangeSlider smallParticlesRangeSlider;
   private JSlider smallParticlesRangeSliderLow;
   private JSlider smallParticlesRangeSliderHigh;
   private JSlider smallParticlesRangeSlider2;
   private JPanel thicknessIntensityPanel;
   //private RangeSlider thresholdRangeSlider;
   private JSlider thresholdRangeSliderLow;
   private JSlider thresholdRangeSliderHigh;
   private JToggleButton toggleOverlayToggleButton;
   private JButton unlockButton;

   public static int getRangeValueLow(JSlider lowSlider, JSlider highSlider) {
      return Math.min(lowSlider.getValue(), highSlider.getValue());
   }

   public static int getRangeValueHigh(JSlider lowSlider, JSlider highSlider) {
      return Math.max(lowSlider.getValue(), highSlider.getValue());
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }

   @Override
   public void keyPressed(KeyEvent e) {
   }

   @Override
   public void keyReleased(KeyEvent e) {
   }

   @Override
   public void mouseClicked(MouseEvent e) {
   }

   @Override
   public void mousePressed(MouseEvent e) {
      this.requestFocus();
   }

   @Override
   public void mouseReleased(MouseEvent e) {
   }

   @Override
   public void mouseEntered(MouseEvent e) {
   }

   @Override
   public void mouseExited(MouseEvent e) {
   }

   public AngioToolGUI() {
      this.initLookAndFeel();
      //Utils.NAME = "AngioTool-Batch";

      //Utils.checkJavaVersion(1, 7, 0);
      //Utils.checkImageJVersion(1, 47, "s");
      this.setIconImage(Utils.ATIcon.getImage());
      ATAboutBoxLoc = (java.awt.Point)ATPreferences.settings.atHelpLoc;
      this.currentDir = new File((String)ATPreferences.settings.currentDir);
      this.showOverlay = ATPreferences.settings.showOverlay;
      this.showOutline = ATPreferences.settings.showOutline;
      this.showSkeleton = ATPreferences.settings.showSkeleton;
      this.showBranchingPoints = ATPreferences.settings.showBranchingPoints;
      this.showConvexHull = ATPreferences.settings.showConvexHull;
      this.OutlineStrokeWidth = ATPreferences.settings.OutlineStrokeWidth;
      this.SkeletonStrokeWidth = ATPreferences.settings.SkeletonStrokeWidth;
      this.BranchingPointsStrokeWidth = ATPreferences.settings.BranchingPointsStrokeWidth;
      this.ConvexHullStrokeWidth = ATPreferences.settings.ConvexHullStrokeWidth;
      this.OutlineColor = Utils.Hex2Color((String)ATPreferences.settings.OutlineColor);
      this.SkeletonColor = Utils.Hex2Color((String)ATPreferences.settings.SkeletonColor);
      this.BranchingPointColor = Utils.Hex2Color((String)ATPreferences.settings.BranchingPointsColor);
      this.ConvexHullColor = Utils.Hex2Color((String)ATPreferences.settings.ConvexHullColor);
      this.imageResultFormat = (String)ATPreferences.settings.imageResultFormat;
      this.initComponents();
      MemoryMonitor mm = new MemoryMonitor(1000, this.memTransparentTextField);
      mm.start();
      this.ste = new SaveToExcel();
      this.aboutBox = new AngioToolAboutBox(this, false);
      this.addKeyListener(this);
      this.addMouseListener(this);
      this.setFocusable(true);
      Utils.isInternetActive = new ReachableTest().test();
      this.startDate = new Date();
   }

   private void initComponents() {
      //this.fillHolesRangeSlider = new RangeSlider();
      this.fillHolesRangeSliderLow = new JSlider();
      this.fillHolesRangeSliderHigh = new JSlider();
      //this.smallParticlesRangeSlider = new RangeSlider();
      this.smallParticlesRangeSliderLow = new JSlider();
      this.smallParticlesRangeSliderHigh = new JSlider();
      this.openImageButton = new JButton();
      this.BatchButton = new JButton();
      this.AnalyzeButton = new JButton();
      progressBar = new JProgressBar();
      this.TabbedPane = new JTabbedPane();
      this.AnalysisTabPanel = new JPanel();
      this.thicknessIntensityPanel = new JPanel();
      this.sigmasMarkSlider = new markSlider();
      this.sigmasSpinner = new JSpinner();
      //this.thresholdRangeSlider = new RangeSlider();
      this.thresholdRangeSliderLow = new JSlider();
      this.thresholdRangeSliderHigh = new JSlider();
      this.lowThresholdTextField = new JTextField();
      this.highThresholdTextField = new JTextField();
      this.backgroundParticlesPanel = new JPanel();
      this.smallParticlesCheckBox = new JCheckBox();
      this.fillHolesCheckBox = new JCheckBox();
      this.fillHolesSpinner = new JSpinner();
      this.removeSmallParticlesSpinner = new JSpinner();
      this.fillHolesRangeSlider2 = new JSlider();
      this.smallParticlesRangeSlider2 = new JSlider();
      this.savingPreferencesPanel = new JPanel();
      this.saveResultsToButton = new JButton();
      this.saveResultsToTextField = new JTextField();
      this.saveResultImageCheckBox = new JCheckBox();
      this.toggleOverlayToggleButton = new JToggleButton();
      this.settingsTabPanel = new JPanel();
      this.resizeImagePanel = new JPanel();
      this.resizeImageCheckBox = new JCheckBox();
      this.resizingFactorLabel = new JLabel();
      this.resizingFactorSpinner = new JSpinner();
      this.unlockButton = new JButton();
      this.setScalePanel = new JPanel();
      this.distanceInPixelsLabel = new JLabel();
      this.distanceInMMLabel = new JLabel();
      this.scaleLabel = new JLabel();
      this.scaleTextField = new JTextField();
      this.distanceInPixelsNumberTextField = new JNumberTextField();
      this.distanceInPixelsNumberTextField.setFormat(2);
      this.distanceInMMNumberTextField = new JNumberTextField();
      this.clearCalibrationButton = new JButton();
      this.overlaySettingsPanel = new JPanel();
      this.showOutlineCheckBox = new JCheckBox();
      this.showBranchingPointsCheckBox = new JCheckBox();
      this.showConvexHullCheckBox = new JCheckBox();
      this.showSkeletonCheckBox = new JCheckBox();
      this.outlineSpinner = new JSpinner();
      this.skeletonSpinner = new JSpinner();
      this.branchingPointsSpinner = new JSpinner();
      this.convexHullSizeSpinner = new JSpinner();
      this.outlineColorButton = new JButton();
      this.skeletonColorButton = new JButton();
      this.branchinPointsColorButton = new JButton();
      this.convexHullColorButton = new JButton();
      this.branchingPointsLabel = new JLabel();
      this.convexHullLabel = new JLabel();
      this.skeletonLabel = new JLabel();
      this.outlineLabel = new JLabel();
      this.skeletonColorRoundedPanel = new RoundedPanel();
      this.branchingPointsRoundedPanel = new RoundedPanel();
      this.convexHullRoundedPanel = new RoundedPanel();
      this.outlineRoundedPanel = new RoundedPanel();
      this.showOverlayCheckBox = new JCheckBox();
      this.saveImageButton = new JButton();
      this.imageResulFormatComboBox = new JComboBox();
      //this.ExitButton = new JButton();
      this.helpButton = new JButton();
      this.memTransparentTextField = new TransparentTextField();
      this.fillHolesRangeSliderLow.setPaintLabels(true);
      this.fillHolesRangeSliderLow.setPaintTicks(true);
      this.fillHolesRangeSliderLow.setEnabled(false);
      this.fillHolesRangeSliderLow.setValue(0);
      this.fillHolesRangeSliderLow.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.fillHolesRangeSliderStateChanged(fillHolesRangeSliderLow, fillHolesRangeSliderHigh);
         }
      });
      this.fillHolesRangeSliderHigh.setPaintLabels(true);
      this.fillHolesRangeSliderHigh.setPaintTicks(true);
      this.fillHolesRangeSliderHigh.setEnabled(false);
      this.fillHolesRangeSliderHigh.setValue(100);
      this.fillHolesRangeSliderHigh.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.fillHolesRangeSliderStateChanged(fillHolesRangeSliderLow, fillHolesRangeSliderHigh);
         }
      });
      this.smallParticlesRangeSliderLow.setPaintLabels(true);
      this.smallParticlesRangeSliderLow.setPaintTicks(true);
      this.smallParticlesRangeSliderLow.setEnabled(false);
      this.smallParticlesRangeSliderLow.setValue(0);
      this.smallParticlesRangeSliderLow.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.smallParticlesRangeSliderStateChanged(smallParticlesRangeSliderLow, smallParticlesRangeSliderHigh);
         }
      });
      this.smallParticlesRangeSliderHigh.setPaintLabels(true);
      this.smallParticlesRangeSliderHigh.setPaintTicks(true);
      this.smallParticlesRangeSliderHigh.setEnabled(false);
      this.smallParticlesRangeSliderHigh.setValue(100);
      this.smallParticlesRangeSliderHigh.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.smallParticlesRangeSliderStateChanged(smallParticlesRangeSliderLow, smallParticlesRangeSliderHigh);
         }
      });
      this.setDefaultCloseOperation(0);
      this.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent evt) {
            ExitButtonActionPerformed(null);
         }
      });
      this.setTitle(AngioTool.VERSION); // Utils.NAME + " " + "0.6a"
      this.setCursor(new Cursor(0));
      this.setMinimumSize(new Dimension(540, 790));
      this.setName("mainFrame");
      this.setResizable(false);
      this.getContentPane().setLayout(new AbsoluteLayout());
      Font buttonFont = new Font("Tahoma", 1, 11);
      this.openImageButton.setFont(buttonFont);
      this.openImageButton.setIcon(Utils.ATOpenImage);
      this.openImageButton.setText("Open Image");
      this.openImageButton.setToolTipText("Open Image");
      this.openImageButton.setHorizontalTextPosition(0);
      this.openImageButton.setVerticalTextPosition(3);
      this.openImageButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.openImageButtonActionPerformed(evt);
         }
      });
      this.getContentPane().add(this.openImageButton, new AbsoluteConstraints(10, 10, 105, 100));
      this.AnalyzeButton.setFont(buttonFont);
      this.AnalyzeButton.setIcon(Utils.ATRunAnalysis);
      this.AnalyzeButton.setText("Run Analysis");
      this.AnalyzeButton.setToolTipText("Run analysis");
      this.AnalyzeButton.setEnabled(false);
      this.AnalyzeButton.setHorizontalTextPosition(0);
      this.AnalyzeButton.setVerticalTextPosition(3);
      this.AnalyzeButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.AnalyzeButtonActionPerformed(evt);
         }
      });
      this.getContentPane().add(this.AnalyzeButton, new AbsoluteConstraints(130, 10, 105, 100));
      this.BatchButton.setFont(buttonFont);
      this.BatchButton.setIcon(Utils.ATBatch);
      this.BatchButton.setText("Batch");
      this.BatchButton.setToolTipText("For every image in a folder (recursively), run analysis");
      this.BatchButton.setHorizontalTextPosition(0);
      this.BatchButton.setVerticalTextPosition(3);
      this.BatchButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.BatchButtonActionPerformed(evt);
         }
      });
      this.getContentPane().add(this.BatchButton, new AbsoluteConstraints(250, 10, 105, 100));
      progressBar.setBorder(new SoftBevelBorder(1));
      progressBar.setPreferredSize(new Dimension(152, 20));
      this.getContentPane().add(progressBar, new AbsoluteConstraints(125, 730, 400, 21));
      this.batchStatusLabel = new JLabel();
      this.batchStatusLabel.setFont(new Font("Tahoma", 0, 11));
      this.getContentPane().add(batchStatusLabel, new AbsoluteConstraints(16, 115, -1, -1));
      this.TabbedPane.setToolTipText("Settings");
      this.TabbedPane.setFont(new Font("Tahoma", 0, 14));
      this.AnalysisTabPanel.setFont(new Font("Tahoma", 1, 14));
      this.AnalysisTabPanel.setLayout(new AbsoluteLayout());
      this.thicknessIntensityPanel.setBorder(BorderFactory.createTitledBorder(null, "Vessel diameter and intensity", 0, 0, new Font("Tahoma", 1, 14)));
      this.thicknessIntensityPanel.setLayout(new AbsoluteLayout());
      this.sigmasMarkSlider.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent evt) {
            AngioToolGUI.this.sigmasMarkSliderMouseClicked(evt);
         }
      });
      this.thicknessIntensityPanel.add(this.sigmasMarkSlider, new AbsoluteConstraints(10, 30, 400, -1));
      this.sigmasSpinner.setFont(new Font("Tahoma", 0, 14));
      this.sigmasSpinner.setModel(new SpinnerNumberModel(0, null, null, 10));
      this.sigmasSpinner.setToolTipText("Adjust Maximum Vessel Thickness");
      this.sigmasSpinner.setBorder(BorderFactory.createCompoundBorder(null, new SoftBevelBorder(0)));
      this.sigmasSpinner.setEnabled(false);
      this.sigmasSpinner.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.sigmasSpinnerStateChanged(evt);
         }
      });
      this.thicknessIntensityPanel.add(this.sigmasSpinner, new AbsoluteConstraints(420, 20, 56, 49));
      this.thresholdRangeSliderLow.setMajorTickSpacing(20);
      this.thresholdRangeSliderLow.setMaximum(255);
      this.thresholdRangeSliderLow.setPaintLabels(true);
      this.thresholdRangeSliderLow.setPaintTicks(true);
      this.thresholdRangeSliderLow.setEnabled(false);
      this.thresholdRangeSliderLow.setValue(0);
      this.thresholdRangeSliderLow.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.thresholdRangeSliderStateChanged(thresholdRangeSliderLow, thresholdRangeSliderHigh);
         }
      });
      this.thresholdRangeSliderHigh.setMajorTickSpacing(20);
      this.thresholdRangeSliderHigh.setMaximum(255);
      this.thresholdRangeSliderHigh.setPaintLabels(true);
      this.thresholdRangeSliderHigh.setPaintTicks(true);
      this.thresholdRangeSliderHigh.setEnabled(false);
      this.thresholdRangeSliderHigh.setValue(255);
      this.thresholdRangeSliderHigh.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.thresholdRangeSliderStateChanged(thresholdRangeSliderLow, thresholdRangeSliderHigh);
         }
      });
      this.thicknessIntensityPanel.add(this.thresholdRangeSliderLow, new AbsoluteConstraints(50, 90, 380, 70));
      this.thicknessIntensityPanel.add(this.thresholdRangeSliderHigh, new AbsoluteConstraints(50, 160, 380, 70));
      this.lowThresholdTextField.setEditable(false);
      this.lowThresholdTextField.setHorizontalAlignment(0);
      this.lowThresholdTextField.setText("0");
      this.thicknessIntensityPanel.add(this.lowThresholdTextField, new AbsoluteConstraints(10, 110, 25, -1));
      this.highThresholdTextField.setEditable(false);
      this.highThresholdTextField.setHorizontalAlignment(0);
      this.highThresholdTextField.setText("0");
      this.thicknessIntensityPanel.add(this.highThresholdTextField, new AbsoluteConstraints(450, 150, 29, -1));
      this.AnalysisTabPanel.add(this.thicknessIntensityPanel, new AbsoluteConstraints(10, 50, 490, 255));
      this.backgroundParticlesPanel
         .setBorder(BorderFactory.createTitledBorder(null, "Eliminate foreground and background small particles", 0, 0, new Font("Tahoma", 1, 14)));
      this.backgroundParticlesPanel.setToolTipText("Eliminate Background and Foreground small particles");
      this.backgroundParticlesPanel.setLayout(new AbsoluteLayout());
      this.smallParticlesCheckBox.setFont(new Font("Tahoma", 0, 15));
      this.smallParticlesCheckBox.setText("Remove small particles");
      this.smallParticlesCheckBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.smallParticlesCheckBoxActionPerformed(evt);
         }
      });
      this.backgroundParticlesPanel.add(this.smallParticlesCheckBox, new AbsoluteConstraints(14, 28, -1, -1));
      this.fillHolesCheckBox.setFont(new Font("Tahoma", 0, 15));
      this.fillHolesCheckBox.setText("Fill holes");
      this.fillHolesCheckBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.fillHolesCheckBoxActionPerformed(evt);
         }
      });
      this.backgroundParticlesPanel.add(this.fillHolesCheckBox, new AbsoluteConstraints(14, 95, -1, -1));
      this.fillHolesSpinner.setFont(new Font("Tahoma", 0, 14));
      this.fillHolesSpinner.setModel(new SpinnerNumberModel(0, null, null, 10));
      this.fillHolesSpinner.setToolTipText("Adjust Maximum Vessel Thickness");
      this.fillHolesSpinner.setBorder(BorderFactory.createCompoundBorder(null, new SoftBevelBorder(0)));
      this.fillHolesSpinner.setEnabled(false);
      this.fillHolesSpinner.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.fillHolesSpinnerStateChanged(evt);
         }
      });
      this.backgroundParticlesPanel.add(this.fillHolesSpinner, new AbsoluteConstraints(400, 120, 75, 35));
      this.removeSmallParticlesSpinner.setFont(new Font("Tahoma", 0, 14));
      this.removeSmallParticlesSpinner.setModel(new SpinnerNumberModel(0, null, null, 10));
      this.removeSmallParticlesSpinner.setToolTipText("Adjust Maximum Vessel Thickness");
      this.removeSmallParticlesSpinner.setBorder(BorderFactory.createCompoundBorder(null, new SoftBevelBorder(0)));
      this.removeSmallParticlesSpinner.setEnabled(false);
      this.removeSmallParticlesSpinner.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.removeSmallParticlesSpinnerStateChanged(evt);
         }
      });
      this.backgroundParticlesPanel.add(this.removeSmallParticlesSpinner, new AbsoluteConstraints(400, 55, 75, 35));
      this.fillHolesRangeSlider2.setPaintLabels(true);
      this.fillHolesRangeSlider2.setPaintTicks(true);
      this.fillHolesRangeSlider2.setValue(0);
      this.fillHolesRangeSlider2.setEnabled(false);
      this.fillHolesRangeSlider2.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.fillHolesRangeSlider2StateChanged(evt);
         }
      });
      this.backgroundParticlesPanel.add(this.fillHolesRangeSlider2, new AbsoluteConstraints(50, 117, 340, 50));
      this.smallParticlesRangeSlider2.setPaintLabels(true);
      this.smallParticlesRangeSlider2.setPaintTicks(true);
      this.smallParticlesRangeSlider2.setValue(0);
      this.smallParticlesRangeSlider2.setEnabled(false);
      this.smallParticlesRangeSlider2.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.smallParticlesRangeSlider2StateChanged(evt);
         }
      });
      this.backgroundParticlesPanel.add(this.smallParticlesRangeSlider2, new AbsoluteConstraints(50, 50, 340, 50));
      this.AnalysisTabPanel.add(this.backgroundParticlesPanel, new AbsoluteConstraints(10, 310, 490, 170));
      this.toggleOverlayToggleButton.setSelected(!this.showOverlay);
      this.toggleOverlayToggleButton.setText(this.showOverlay ? "Hide Overlay" : "Show Overlay");
      this.toggleOverlayToggleButton.setEnabled(false);
      this.toggleOverlayToggleButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.toggleOverlayToggleButtonActionPerformed(evt);
         }
      });
      this.AnalysisTabPanel.add(this.toggleOverlayToggleButton, new AbsoluteConstraints(400, 10, -1, -1));
      this.TabbedPane.addTab("Analysis", this.AnalysisTabPanel);
      this.settingsTabPanel.setFont(new Font("Tahoma", 1, 14));
      this.settingsTabPanel.setLayout(new AbsoluteLayout());
      this.resizeImagePanel.setBorder(BorderFactory.createTitledBorder(null, "Resize Image", 0, 0, new Font("Tahoma", 1, 14)));
      this.resizeImagePanel.setLayout(new AbsoluteLayout());
      this.resizeImageCheckBox.setFont(new Font("Tahoma", 0, 14));
      this.resizeImageCheckBox.setText("Resize image");
      this.resizeImageCheckBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.resizeImageCheckBoxActionPerformed(evt);
         }
      });
      this.resizeImagePanel.add(this.resizeImageCheckBox, new AbsoluteConstraints(50, 40, 131, -1));
      this.resizingFactorLabel.setFont(new Font("Tahoma", 0, 14));
      this.resizingFactorLabel.setText("Resizing factor");
      this.resizingFactorLabel.setEnabled(false);
      this.resizeImagePanel.add(this.resizingFactorLabel, new AbsoluteConstraints(190, 45, -1, -1));
      this.resizingFactorSpinner.setModel(new SpinnerNumberModel(1.0, 1.0, 10.0, 0.5));
      this.resizingFactorSpinner.setToolTipText("Set resizing factor");
      this.resizingFactorSpinner.setEnabled(false);
      this.resizingFactorSpinner.setValue(1.0); // must be a Double
      this.resizingFactorSpinner.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.resizingFactorSpinnerStateChanged(evt);
         }
      });
      this.resizeImagePanel.add(this.resizingFactorSpinner, new AbsoluteConstraints(290, 40, 52, 30));
      this.unlockButton.setFont(buttonFont);
      this.unlockButton.setIcon(new ImageIcon(this.getClass().getResource("/images/unlocked.png")));
      this.unlockButton.setText("Lock");
      this.unlockButton.setHorizontalTextPosition(0);
      this.unlockButton.setPressedIcon(new ImageIcon(this.getClass().getResource("/images/unlocked.png")));
      this.unlockButton.setVerticalTextPosition(3);
      this.unlockButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.unlockButtonActionPerformed(evt);
         }
      });
      this.resizeImagePanel.add(this.unlockButton, new AbsoluteConstraints(395, 25, 70, -1));
      this.settingsTabPanel.add(this.resizeImagePanel, new AbsoluteConstraints(10, 11, 490, 100));
      this.setScalePanel.setBorder(BorderFactory.createTitledBorder(null, "Calibration", 0, 0, new Font("Tahoma", 1, 14)));
      this.setScalePanel.setLayout(new AbsoluteLayout());
      this.distanceInPixelsLabel.setFont(new Font("Tahoma", 0, 14));
      this.distanceInPixelsLabel.setText("Distance in pixels");
      this.setScalePanel.add(this.distanceInPixelsLabel, new AbsoluteConstraints(15, 30, -1, -1));
      this.distanceInMMLabel.setFont(new Font("Tahoma", 0, 14));
      this.distanceInMMLabel.setText("Distance in mm");
      this.setScalePanel.add(this.distanceInMMLabel, new AbsoluteConstraints(280, 30, -1, -1));
      this.scaleLabel.setFont(new Font("Tahoma", 0, 14));
      this.scaleLabel.setText("Scale");
      this.setScalePanel.add(this.scaleLabel, new AbsoluteConstraints(120, 70, -1, -1));
      this.scaleTextField.setEditable(false);
      this.scaleTextField.setFont(new Font("Tahoma", 1, 12));
      this.scaleTextField.setHorizontalAlignment(0);
      this.scaleTextField.setToolTipText("Scale");
      this.scaleTextField.setBorder(null);
      this.setScalePanel.add(this.scaleTextField, new AbsoluteConstraints(160, 73, 180, -1));
      this.distanceInPixelsNumberTextField.setHorizontalAlignment(0);
      this.distanceInPixelsNumberTextField.addKeyListener(new KeyAdapter() {
         @Override
         public void keyReleased(KeyEvent evt) {
            AngioToolGUI.this.distanceInPixelsNumberTextFieldKeyReleased(evt);
         }
      });
      this.setScalePanel.add(this.distanceInPixelsNumberTextField, new AbsoluteConstraints(125, 30, 120, -1));
      this.distanceInMMNumberTextField.setHorizontalAlignment(0);
      this.distanceInMMNumberTextField.addKeyListener(new KeyAdapter() {
         @Override
         public void keyReleased(KeyEvent evt) {
            AngioToolGUI.this.distanceInMMNumberTextFieldKeyReleased(evt);
         }
      });
      this.setScalePanel.add(this.distanceInMMNumberTextField, new AbsoluteConstraints(380, 30, 60, -1));
      this.clearCalibrationButton.setText("Clear Calibration");
      this.clearCalibrationButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.clearCalibrationButtonActionPerformed(evt);
         }
      });
      this.setScalePanel.add(this.clearCalibrationButton, new AbsoluteConstraints(350, 70, -1, -1));
      this.settingsTabPanel.add(this.setScalePanel, new AbsoluteConstraints(10, 120, 490, 107));
      this.overlaySettingsPanel.setBorder(BorderFactory.createTitledBorder(null, "Overlay Settings", 0, 0, new Font("Tahoma", 1, 14)));
      this.overlaySettingsPanel.setLayout(new AbsoluteLayout());
      this.showOutlineCheckBox.setFont(new Font("Tahoma", 0, 14));
      this.showOutlineCheckBox.setSelected(true);
      this.showOutlineCheckBox.setText("Show outline");
      this.showOutlineCheckBox.setToolTipText("Show outline");
      this.showOutlineCheckBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.showOutlineCheckBoxActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.showOutlineCheckBox, new AbsoluteConstraints(20, 60, -1, -1));
      this.showBranchingPointsCheckBox.setFont(new Font("Tahoma", 0, 14));
      this.showBranchingPointsCheckBox.setSelected(true);
      this.showBranchingPointsCheckBox.setText("Branching points");
      this.showBranchingPointsCheckBox.setToolTipText("Show branching points");
      this.showBranchingPointsCheckBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.showBranchingPointsCheckBoxActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.showBranchingPointsCheckBox, new AbsoluteConstraints(240, 60, -1, -1));
      this.showConvexHullCheckBox.setFont(new Font("Tahoma", 0, 14));
      this.showConvexHullCheckBox.setSelected(true);
      this.showConvexHullCheckBox.setText("Show boundary");
      this.showConvexHullCheckBox.setToolTipText("Show boundary");
      this.showConvexHullCheckBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.showConvexHullCheckBoxActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.showConvexHullCheckBox, new AbsoluteConstraints(240, 90, -1, -1));
      this.showSkeletonCheckBox.setFont(new Font("Tahoma", 0, 14));
      this.showSkeletonCheckBox.setSelected(true);
      this.showSkeletonCheckBox.setText("Show skeleton");
      this.showSkeletonCheckBox.setToolTipText("Show skeleton");
      this.showSkeletonCheckBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.showSkeletonCheckBoxActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.showSkeletonCheckBox, new AbsoluteConstraints(20, 90, -1, -1));
      this.outlineSpinner.setModel(new SpinnerNumberModel(1, 1, null, 1));
      this.outlineSpinner.setValue(this.OutlineStrokeWidth);
      this.outlineSpinner.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.outlineSpinnerStateChanged(evt);
         }
      });
      this.overlaySettingsPanel.add(this.outlineSpinner, new AbsoluteConstraints(180, 60, 50, 23));
      this.skeletonSpinner.setModel(new SpinnerNumberModel(1, 1, null, 1));
      this.skeletonSpinner.setValue(this.SkeletonStrokeWidth);
      this.skeletonSpinner.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.skeletonSpinnerStateChanged(evt);
         }
      });
      this.overlaySettingsPanel.add(this.skeletonSpinner, new AbsoluteConstraints(180, 90, 50, 23));
      this.branchingPointsSpinner.setModel(new SpinnerNumberModel(1, 1, null, 1));
      this.branchingPointsSpinner.setValue(this.BranchingPointsStrokeWidth);
      this.branchingPointsSpinner.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.branchingPointsSpinnerStateChanged(evt);
         }
      });
      this.overlaySettingsPanel.add(this.branchingPointsSpinner, new AbsoluteConstraints(420, 60, 50, 23));
      this.convexHullSizeSpinner.setModel(new SpinnerNumberModel(1, 1, null, 1));
      this.convexHullSizeSpinner.setValue(this.ConvexHullStrokeWidth);
      this.convexHullSizeSpinner.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent evt) {
            AngioToolGUI.this.convexHullSizeSpinnerStateChanged(evt);
         }
      });
      this.overlaySettingsPanel.add(this.convexHullSizeSpinner, new AbsoluteConstraints(420, 90, 50, 23));
      this.outlineColorButton.setContentAreaFilled(false);
      //this.outlineColorButton.setBackground(OutlineColor);
      this.outlineColorButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.outlineColorButtonActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.outlineColorButton, new AbsoluteConstraints(150, 60, 26, 23));
      this.skeletonColorButton.setToolTipText("Skeleton Color");
      this.skeletonColorButton.setContentAreaFilled(false);
      //this.skeletonColorButton.setBackground(SkeletonColor);
      this.skeletonColorButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.skeletonColorButtonActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.skeletonColorButton, new AbsoluteConstraints(150, 90, 26, 23));
      this.branchinPointsColorButton.setContentAreaFilled(false);
      //this.branchinPointsColorButton.setBackground(BranchingPointColor);
      this.branchinPointsColorButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.branchinPointsColorButtonActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.branchinPointsColorButton, new AbsoluteConstraints(390, 60, 26, 23));
      this.convexHullColorButton.setContentAreaFilled(false);
      //this.convexHullColorButton.setBackground(ConvexHullColor);
      this.convexHullColorButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.convexHullColorButtonActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.convexHullColorButton, new AbsoluteConstraints(390, 90, 26, 23));
      /*
      this.branchingPointsLabel.setFont(new Font("Tahoma", 0, 14));
      this.branchingPointsLabel.setText("Width");
      this.overlaySettingsPanel.add(this.branchingPointsLabel, new AbsoluteConstraints(320, 120, -1, -1));
      this.convexHullLabel.setFont(new Font("Tahoma", 0, 14));
      this.convexHullLabel.setText("Width");
      this.overlaySettingsPanel.add(this.convexHullLabel, new AbsoluteConstraints(320, 150, -1, -1));
      this.skeletonLabel.setFont(new Font("Tahoma", 0, 14));
      this.skeletonLabel.setText("Width");
      this.overlaySettingsPanel.add(this.skeletonLabel, new AbsoluteConstraints(320, 90, -1, -1));
      this.outlineLabel.setFont(new Font("Tahoma", 0, 14));
      this.outlineLabel.setText("Width");
      this.overlaySettingsPanel.add(this.outlineLabel, new AbsoluteConstraints(320, 60, -1, -1));
      */
      this.skeletonColorRoundedPanel.setBackground(this.SkeletonColor);
      this.skeletonColorRoundedPanel.setCornerRadius(7);
      GroupLayout skeletonColorRoundedPanelLayout = new GroupLayout(this.skeletonColorRoundedPanel);
      this.skeletonColorRoundedPanel.setLayout(skeletonColorRoundedPanelLayout);
      skeletonColorRoundedPanelLayout.setHorizontalGroup(skeletonColorRoundedPanelLayout.createParallelGroup(Alignment.LEADING).addGap(0, 30, 32767));
      skeletonColorRoundedPanelLayout.setVerticalGroup(skeletonColorRoundedPanelLayout.createParallelGroup(Alignment.LEADING).addGap(0, 23, 32767));
      this.overlaySettingsPanel.add(this.skeletonColorRoundedPanel, new AbsoluteConstraints(148, 90, 30, 23));
      this.branchingPointsRoundedPanel.setBackground(this.BranchingPointColor);
      this.branchingPointsRoundedPanel.setCornerRadius(7);
      GroupLayout branchingPointsRoundedPanelLayout = new GroupLayout(this.branchingPointsRoundedPanel);
      this.branchingPointsRoundedPanel.setLayout(branchingPointsRoundedPanelLayout);
      branchingPointsRoundedPanelLayout.setHorizontalGroup(branchingPointsRoundedPanelLayout.createParallelGroup(Alignment.LEADING).addGap(0, 30, 32767));
      branchingPointsRoundedPanelLayout.setVerticalGroup(branchingPointsRoundedPanelLayout.createParallelGroup(Alignment.LEADING).addGap(0, 23, 32767));
      this.overlaySettingsPanel.add(this.branchingPointsRoundedPanel, new AbsoluteConstraints(388, 60, 30, 23));
      this.convexHullRoundedPanel.setBackground(this.ConvexHullColor);
      this.convexHullRoundedPanel.setCornerRadius(7);
      GroupLayout convexHullRoundedPanelLayout = new GroupLayout(this.convexHullRoundedPanel);
      this.convexHullRoundedPanel.setLayout(convexHullRoundedPanelLayout);
      convexHullRoundedPanelLayout.setHorizontalGroup(convexHullRoundedPanelLayout.createParallelGroup(Alignment.LEADING).addGap(0, 30, 32767));
      convexHullRoundedPanelLayout.setVerticalGroup(convexHullRoundedPanelLayout.createParallelGroup(Alignment.LEADING).addGap(0, 23, 32767));
      this.overlaySettingsPanel.add(this.convexHullRoundedPanel, new AbsoluteConstraints(388, 90, 30, 23));
      this.outlineRoundedPanel.setBackground(this.OutlineColor);
      this.outlineRoundedPanel.setCornerRadius(7);
      GroupLayout outlineRoundedPanelLayout = new GroupLayout(this.outlineRoundedPanel);
      this.outlineRoundedPanel.setLayout(outlineRoundedPanelLayout);
      outlineRoundedPanelLayout.setHorizontalGroup(outlineRoundedPanelLayout.createParallelGroup(Alignment.LEADING).addGap(0, 30, 32767));
      outlineRoundedPanelLayout.setVerticalGroup(outlineRoundedPanelLayout.createParallelGroup(Alignment.LEADING).addGap(0, 23, 32767));
      this.overlaySettingsPanel.add(this.outlineRoundedPanel, new AbsoluteConstraints(148, 60, 30, 23));
      this.showOverlayCheckBox.setFont(new Font("Tahoma", 0, 14));
      this.showOverlayCheckBox.setSelected(this.showOverlay);
      this.showOverlayCheckBox.setText("Show overlay");
      this.showOverlayCheckBox.setToolTipText("Show overlay");
      this.showOverlayCheckBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.showOverlayCheckBoxActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.showOverlayCheckBox, new AbsoluteConstraints(10, 30, -1, -1));
      this.saveImageButton.setFont(new Font("Tahoma", 1, 14));
      this.saveImageButton.setText("Save Image");
      this.saveImageButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.saveImageButtonActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.saveImageButton, new AbsoluteConstraints(170, 130, 125, 25));
      this.imageResulFormatComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"jpg", "tiff", "png", "bmp"}));
      this.imageResulFormatComboBox.setSelectedItem(this.imageResultFormat);
      this.imageResulFormatComboBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.imageResulFormatComboBoxActionPerformed(evt);
         }
      });
      this.overlaySettingsPanel.add(this.imageResulFormatComboBox, new AbsoluteConstraints(310, 130, 50, 25));
      this.settingsTabPanel.add(this.overlaySettingsPanel, new AbsoluteConstraints(10, 230, 490, 170));
      this.savingPreferencesPanel.setBorder(BorderFactory.createTitledBorder(null, "Saving Preferences", 0, 0, new Font("Tahoma", 1, 14)));
      this.savingPreferencesPanel.setLayout(new AbsoluteLayout());
      this.saveResultsToButton.setFont(buttonFont);
      this.saveResultsToButton.setIcon(Utils.ATExcel);
      this.saveResultsToButton.setText("Save To");
      this.saveResultsToButton.setToolTipText("Save To");
      this.saveResultsToButton.setHorizontalTextPosition(0);
      this.saveResultsToButton.setVerticalTextPosition(3);
      this.saveResultsToButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.saveResultsToButtonActionPerformed(evt);
         }
      });
      this.savingPreferencesPanel.add(this.saveResultsToButton, new AbsoluteConstraints(10, 30, -1, 100));
      this.saveResultsToTextField.setFont(new Font("Tahoma", 0, 14));
      this.savingPreferencesPanel.add(this.saveResultsToTextField, new AbsoluteConstraints(120, 50, 360, 28));
      this.saveResultImageCheckBox.setFont(new Font("Tahoma", 0, 14));
      this.saveResultImageCheckBox.setSelected(true);
      this.saveResultImageCheckBox.setText("Save result image");
      this.saveResultImageCheckBox.setToolTipText("Save the result Image");
      this.savingPreferencesPanel.add(this.saveResultImageCheckBox, new AbsoluteConstraints(190, 90, -1, 37));
      this.settingsTabPanel.add(this.savingPreferencesPanel, new AbsoluteConstraints(10, 405, 490, 140));
      this.TabbedPane.addTab("Settings", this.settingsTabPanel);
      this.getContentPane().add(this.TabbedPane, new AbsoluteConstraints(10, 129, 515, 590));
      /*
      this.ExitButton.setFont(buttonFont);
      this.ExitButton.setIcon(Utils.ATExit);
      this.ExitButton.setText("Exit");
      this.ExitButton.setToolTipText("Exit");
      this.ExitButton.setHorizontalTextPosition(0);
      this.ExitButton.setVerticalTextPosition(3);
      this.ExitButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.ExitButtonActionPerformed(evt);
         }
      });
      this.getContentPane().add(this.ExitButton, new AbsoluteConstraints(370, 10, 105, 100));
      */
      this.helpButton.setFont(buttonFont);
      this.helpButton.setIcon(Utils.ATHelp);
      this.helpButton.setText("Help");
      this.helpButton.setHorizontalTextPosition(0);
      this.helpButton.setVerticalTextPosition(3);
      this.helpButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolGUI.this.helpButtonActionPerformed(evt);
         }
      });
      this.getContentPane().add(this.helpButton, new AbsoluteConstraints(420, 10, 105, 100));
      this.memTransparentTextField.setBorder(new SoftBevelBorder(1));
      this.memTransparentTextField.setEditable(false);
      this.memTransparentTextField.setHorizontalAlignment(0);
      this.memTransparentTextField.setFont(buttonFont);
      this.getContentPane().add(this.memTransparentTextField, new AbsoluteConstraints(10, 730, 100, 20));
      this.pack();
   }

   private void openImageButtonActionPerformed(ActionEvent evt) {
      JFileChooser fc = new JFileChooser();
      fc.setPreferredSize(new Dimension(1000, 600));
      fc.setFileView(new ImageFileView());
      fc.setAccessory(new ImagePreview(fc));
      fc.setMultiSelectionEnabled(false);
      fc.setCurrentDirectory(this.currentDir);
      fc.setFileFilter(new ImageFilter());
      int returnVal = fc.showOpenDialog(this);
      if (returnVal == 0) {
         this.initVariables();
         this.imageFile = fc.getSelectedFile();
         this.currentDir = fc.getCurrentDirectory();
         if (this.imageFile != null) {
            this.results.image = this.imageFile;
            this.imageOriginal = IJ.openImage(this.imageFile.getAbsolutePath());
            if (this.imageOriginal != null) {
               if (this.imageOriginal.getType() == 4) {
                  if (!Utils.isReleaseVersion) {
                     System.out.println("Running convertRGBtoIndexedColor..." + this.imageOriginal.getBitDepth() + "\t" + (this.imageOriginal.getType() == 4));
                  }

                  this.imageOriginal = RGBStackSplitter.split(this.imageOriginal, "green");
               }

               if (this.resizeImageCheckBox.isSelected()) {
                  ImageProcessor resized = this.imageOriginal.getProcessor().resize((int)((double)this.imageOriginal.getWidth() / this.resizingFactor));
                  this.imageOriginal.setProcessor(resized);
               }

               this.resizeImageCheckBox.setEnabled(false);
               this.resizingFactorSpinner.setEnabled(false);
               this.resizingFactorLabel.setEnabled(false);
               this.unlockButton.setText(this.resizeImageCheckBox.isEnabled() ? "Lock" : "Unlock");
               this.unlockButton.setSelected(true);
               this.AnalyzeButton.setEnabled(true);
               this.imageOriginal.show();
               this.ipOriginal = this.imageOriginal.getProcessor().convertToByte(this.doScaling);
               this.imageResult = this.imageOriginal;
               this.imageResult.getWindow().setLocation(this.getX() + this.getWidth(), this.getY());
               this.imageResult.getWindow().setIconImage(this.imgIcon);
               this.initControls();
               this.computeFirstOutline(this.firstSigma);
               this.updateOverlay();
            }
         }
      }
   }

   private void thresholdRangeSliderStateChanged(JSlider lowSlider, JSlider highSlider) {
      if (!lowSlider.getValueIsAdjusting() && !highSlider.getValueIsAdjusting()) {
         int lowValue = lowSlider.getValue();
         int highValue = highSlider.getValue();
         this.lowThresholdTextField.setText("" + lowValue);
         this.highThresholdTextField.setText("" + highValue);
         this.updateOutline();
      }
   }

   private void fillHolesCheckBoxActionPerformed(ActionEvent evt) {
      this.fillHoles = !this.fillHoles;
      this.fillHolesRangeSlider2.setEnabled(this.fillHoles);
      this.fillHolesIsChanged = this.fillHoles;
      this.fillHolesSpinner.setEnabled(this.fillHoles);
      this.updateOutline();
   }

   private void fillHolesRangeSliderStateChanged(JSlider lowSlider, JSlider highSlider) {
      if (this.fillHoles) {
         if (!lowSlider.getValueIsAdjusting() && !highSlider.getValueIsAdjusting()) {
            this.fillHolesIsChanged = true;
            this.updateOutline();
         }
      }
   }

   private void sigmasMarkSliderMouseClicked(MouseEvent evt) {
      markSlider ms = (markSlider)evt.getSource();
      ArrayList<Integer> s = ms.getMarks();

      if (s.size() == 0) {
         int defaultSigma = (int)this.firstSigma[0];
         this.sigmasMarkSlider.addMark(defaultSigma);
         s.add(defaultSigma);
      }

      try {
         this.updateSigmas(s);
      } catch (OutOfMemoryError var5) {
         if (JOptionPane.showConfirmDialog(null, "Your system has run out of memory\n Reinitializing all variables...", "AngioTool", 0, 0, null) == 0) {
            this.initVariables();
         }
      }

      this.sigmaIsChanged = true;
      this.updateOutline();
   }

   private void smallParticlesCheckBoxActionPerformed(ActionEvent evt) {
      this.smallParticles = !this.smallParticles;
      this.smallParticlesRangeSlider2.setEnabled(this.smallParticles);
      this.smallParticlesIsChanged = this.smallParticles;
      this.removeSmallParticlesSpinner.setEnabled(this.smallParticles);
      this.updateOutline();
   }

   private void smallParticlesRangeSliderStateChanged(JSlider lowSlider, JSlider highSlider) {
      if (this.smallParticles) {
         if (!lowSlider.getValueIsAdjusting() && !highSlider.getValueIsAdjusting()) {
            this.smallParticlesIsChanged = true;
            this.updateOutline();
         }
      }
   }

   private void BatchButtonActionPerformed(ActionEvent evt) {
      new BatchAnalysisUi(this, procureAnalyzerSettings()).showDialog();
   }

   private void AnalyzeButtonActionPerformed(ActionEvent evt) {
      new AngioToolGUI.AngioToolWorker().execute();
   }

   private void resizeImageCheckBoxActionPerformed(ActionEvent evt) {
      this.resizingFactorSpinner.setEnabled(this.resizeImageCheckBox.isSelected());
      this.resizingFactorLabel.setEnabled(this.resizeImageCheckBox.isSelected());
   }

   private void toggleOverlayToggleButtonActionPerformed(ActionEvent evt) {
      this.showOverlay = !this.showOverlay;
      String title = this.showOverlay ? "Hide Overlay" : "Show Overlay";
      this.toggleOverlayToggleButton.setText(title);
      this.showOverlayCheckBox.setSelected(this.showOverlay);
      this.updateOverlay();
   }

   private void sigmasSpinnerStateChanged(ChangeEvent evt) {
      if ((int)this.sigmasSpinner.getValue() <= 0) {
         this.sigmasSpinner.setValue(0);
      }

      this.sigmasMarkSlider.setMaximum((int)this.sigmasSpinner.getValue());
   }

   private void saveResultsToButtonActionPerformed(ActionEvent evt) {
      JFileChooser fc = new JFileChooser();
      fc.setDialogTitle("Save As");
      fc.setDialogType(1);
      fc.setApproveButtonToolTipText("Set the Excel file name where the data will be saved");
      fc.setCurrentDirectory(this.currentDir);
      fc.setFileFilter(new ExcelFilter());
      int returnVal = fc.showOpenDialog(this);
      if (returnVal == 0) {
         File resultsFile = fc.getSelectedFile();
         String extension = null;
         extension = Utils.getExtension(resultsFile);
         if (extension != null) {
            if (!extension.equals("xls") && !extension.equals("xlsx")) {
               this.saveResultsToTextField.setText(resultsFile.getPath() + ".xls");
            } else {
               this.saveResultsToTextField.setText(resultsFile.getPath());
            }
         } else {
            this.saveResultsToTextField.setText(resultsFile.getPath() + ".xls");
         }

         if (!Utils.isReleaseVersion) {
            System.out.println("getPath= " + resultsFile.getPath());
         }

         this.ste = new SaveToExcel(resultsFile.getPath(), true);
      }
   }

   private void resizingFactorSpinnerStateChanged(ChangeEvent evt) {
      this.resizingFactor = (float)this.resizingFactorSpinner.getValue();
   }

   private void skeletonColorButtonActionPerformed(ActionEvent evt) {
      JButton button = (JButton)evt.getSource();
      Color background = JColorChooser.showDialog(null, button.getToolTipText(), this.SkeletonColor);
      if (background != null) {
         this.skeletonColorRoundedPanel.setBackground(background);
         this.updateOverlay();
      }
   }

   private void branchinPointsColorButtonActionPerformed(ActionEvent evt) {
      JButton button = (JButton)evt.getSource();
      Color background = JColorChooser.showDialog(null, button.getToolTipText(), this.BranchingPointColor);
      if (background != null) {
         this.branchingPointsRoundedPanel.setBackground(background);
         this.updateOverlay();
      }
   }

   private void convexHullColorButtonActionPerformed(ActionEvent evt) {
      JButton button = (JButton)evt.getSource();
      Color background = JColorChooser.showDialog(null, button.getToolTipText(), this.ConvexHullColor);
      if (background != null) {
         this.convexHullRoundedPanel.setBackground(background);
         this.updateOverlay();
      }
   }

   private void outlineColorButtonActionPerformed(ActionEvent evt) {
      JButton button = (JButton)evt.getSource();
      Color background = JColorChooser.showDialog(null, button.getToolTipText(), this.OutlineColor);
      if (background != null) {
         this.outlineRoundedPanel.setBackground(background);
         this.updateOverlay();
      }
   }

   private void distanceInPixelsNumberTextFieldKeyReleased(KeyEvent evt) {
      if (!this.distanceInMMNumberTextField.getText().equals("") && !this.distanceInPixelsNumberTextField.getText().equals("")) {
         this.scaleTextField.setText("" + (double)this.distanceInPixelsNumberTextField.getInt() / this.distanceInMMNumberTextField.getDouble() + " pixels/mm");
      }
   }

   private void distanceInMMNumberTextFieldKeyReleased(KeyEvent evt) {
      if (!this.distanceInMMNumberTextField.getText().equals("") && !this.distanceInPixelsNumberTextField.getText().equals("")) {
         this.scaleTextField.setText("" + (double)this.distanceInPixelsNumberTextField.getInt() / this.distanceInMMNumberTextField.getDouble() + " pixels/mm");
      }
   }

   private void fillHolesSpinnerStateChanged(ChangeEvent evt) {
      if ((int)this.fillHolesSpinner.getValue() <= 0) {
         this.fillHolesSpinner.setValue(0);
      }

      this.fillHolesRangeSlider2.setMaximum((int)this.fillHolesSpinner.getValue());
      this.fillHolesRangeSlider2.setMajorTickSpacing(this.fillHolesRangeSlider2.getMaximum() / 10);
      Hashtable h = this.fillHolesRangeSlider2.createStandardLabels(this.fillHolesRangeSlider2.getMaximum() / 10);
      this.fillHolesRangeSlider2.setLabelTable(h);
   }

   private void removeSmallParticlesSpinnerStateChanged(ChangeEvent evt) {
      if ((int)this.removeSmallParticlesSpinner.getValue() <= 0) {
         this.removeSmallParticlesSpinner.setValue(0);
      }

      this.smallParticlesRangeSlider2.setMaximum((int)this.removeSmallParticlesSpinner.getValue());
      this.smallParticlesRangeSlider2.setMajorTickSpacing(this.smallParticlesRangeSlider2.getMaximum() / 10);
      Hashtable h = this.smallParticlesRangeSlider2.createStandardLabels(this.smallParticlesRangeSlider2.getMaximum() / 10);
      this.smallParticlesRangeSlider2.setLabelTable(h);
   }

   private void ExitButtonActionPerformed(ActionEvent evt) {
      if (JOptionPane.showConfirmDialog(null, "Do you really want to exit AngioTool?", "AngioTool", 0, 3, null) == 0) {
         if (this.imageResult != null) {
            this.imageResult.close();
         }

         this.setVisible(false);
         this.exit();
         System.exit(0);
      }
   }

   private void showConvexHullCheckBoxActionPerformed(ActionEvent evt) {
      this.updateOverlay();
   }

   private void showSkeletonCheckBoxActionPerformed(ActionEvent evt) {
      this.updateOverlay();
   }

   private void skeletonSpinnerStateChanged(ChangeEvent evt) {
      this.updateOverlay();
   }

   private void outlineSpinnerStateChanged(ChangeEvent evt) {
      this.updateOverlay();
   }

   private void branchingPointsSpinnerStateChanged(ChangeEvent evt) {
      this.updateOverlay();
   }

   private void convexHullSizeSpinnerStateChanged(ChangeEvent evt) {
      this.updateOverlay();
   }

   private void showOutlineCheckBoxActionPerformed(ActionEvent evt) {
      this.updateOverlay();
   }

   private void showBranchingPointsCheckBoxActionPerformed(ActionEvent evt) {
      this.updateOverlay();
   }

   private void showOverlayCheckBoxActionPerformed(ActionEvent evt) {
      if (this.imageResult != null) {
         this.updateOverlay();
         String title = !this.showOverlayCheckBox.isSelected() ? "Show Overlay" : "Hide Overlay";
         this.toggleOverlayToggleButton.setText(title);
         this.toggleOverlayToggleButton.setSelected(!this.showOverlayCheckBox.isSelected());
      }
   }

   private void saveImageButtonActionPerformed(ActionEvent evt) {
      if (this.imageResult != null) {
         ImagePlus imageResultFlattenen = this.imageResult.flatten();
         IJ.saveAs(imageResultFlattenen, this.imageResultFormat, this.imageFile.getAbsolutePath() + " result." + this.imageResultFormat);
      } else if (JOptionPane.showConfirmDialog(null, "No image to save", "AngioTool", -1, 2, null) == 0) {
         System.gc();
      }
   }

   private void unlockButtonActionPerformed(ActionEvent evt) {
      this.resizeImageCheckBox.setEnabled(!this.resizeImageCheckBox.isEnabled());
      this.resizingFactorLabel.setEnabled(this.resizeImageCheckBox.isSelected() && this.resizeImageCheckBox.isEnabled());
      this.resizingFactorSpinner.setEnabled(this.resizeImageCheckBox.isSelected() && this.resizeImageCheckBox.isEnabled());
      this.unlockButton.setText(this.resizeImageCheckBox.isEnabled() ? "Lock" : "Unlock");
      this.unlockButton.setSelected(!this.resizeImageCheckBox.isEnabled());
   }

   private void helpButtonActionPerformed(ActionEvent evt) {
      this.aboutBox.setLocation(ATAboutBoxLoc);
      this.aboutBox.setVisible(true);
   }

   private void fillHolesRangeSlider2StateChanged(ChangeEvent evt) {
      if (this.fillHoles) {
         JSlider source = (JSlider)evt.getSource();
         if (!source.getValueIsAdjusting()) {
            this.fillHolesIsChanged = true;
            this.updateOutline();
         }
      }
   }

   private void smallParticlesRangeSlider2StateChanged(ChangeEvent evt) {
      if (this.smallParticles) {
         JSlider source = (JSlider)evt.getSource();
         if (!source.getValueIsAdjusting()) {
            this.smallParticlesIsChanged = true;
            this.updateOutline();
         }
      }
   }

   private void imageResulFormatComboBoxActionPerformed(ActionEvent evt) {
      this.imageResultFormat = (String)this.imageResulFormatComboBox.getSelectedItem();
   }

   private void clearCalibrationButtonActionPerformed(ActionEvent evt) {
      this.distanceInMMNumberTextField.setText("");
      this.distanceInPixelsNumberTextField.setText("");
      this.scaleTextField.setText("");
      this.LinearScalingFactor = 1.0;
      this.AreaScalingFactor = 1.0;
   }

   private void populateResults() {
      if (!this.distanceInMMNumberTextField.getText().equals("") && !this.distanceInPixelsNumberTextField.getText().equals("")) {
         this.LinearScalingFactor = this.distanceInMMNumberTextField.getDouble() / (double)this.distanceInPixelsNumberTextField.getInt();
         this.AreaScalingFactor = this.distanceInMMNumberTextField.getDouble()
            * this.distanceInMMNumberTextField.getDouble()
            / (double)(this.distanceInPixelsNumberTextField.getInt() * this.distanceInPixelsNumberTextField.getInt());
         this.AreaScalingFactor = this.LinearScalingFactor * this.LinearScalingFactor;
      } else {
         this.LinearScalingFactor = 1.0;
         this.AreaScalingFactor = 1.0;
      }

      this.results.image = this.imageFile;
      this.results.thresholdLow = this.thresholdRangeSliderLow.getValue();
      this.results.thresholdHigh = this.thresholdRangeSliderHigh.getValue();
      this.results.sigmas = this.sigmasMarkSlider.getMarks();
      this.results.removeSmallParticles = (int)this.smallParticlesRangeSlider2.getValue();
      this.results.fillHoles = (int)this.fillHolesRangeSlider2.getValue();
      this.results.LinearScalingFactor = this.LinearScalingFactor;
      this.results.AreaScalingFactor = this.AreaScalingFactor;
      this.results.allantoisPixelsArea = this.convexHullArea;
      this.results.allantoisMMArea = this.convexHullArea * this.AreaScalingFactor;
      this.results.totalNJunctions = this.al2.size();
      this.results.JunctionsPerArea = (double)this.al2.size() / this.convexHullArea;
      this.results.JunctionsPerScaledArea = (double)this.al2.size() / this.results.allantoisMMArea;
      this.results.vesselMMArea = (double)this.results.vesselPixelArea * this.AreaScalingFactor;
      this.results.vesselPercentageArea = this.results.vesselMMArea * 100.0 / this.results.allantoisMMArea;
      if (this.computeThickness) {
         this.results.averageVesselDiameter = Utils.computeMedianThickness(this.graph, this.imageThickness) * this.LinearScalingFactor;
      }

      double[] branchLengths = this.skelResult.getAverageBranchLength();
      int[] branchNumbers = this.skelResult.getBranches();
      double totalLength = 0.0;
      double averageLength = 0.0;

      for(int i = 0; i < branchNumbers.length; ++i) {
         totalLength += (double)branchNumbers[i] * branchLengths[i];
      }

      this.results.totalLength = totalLength * this.LinearScalingFactor;
      this.results.averageBranchLength = totalLength / (double)branchNumbers.length * this.LinearScalingFactor;
      this.results.totalNEndPoints = this.skelResult.getListOfEndPoints().size();
      String name = this.imageFile.getName();
      int fileExtensionLength = Utils.getExtension(this.imageFile).length() + 1;
      this.ste.setFileName(name.substring(0, name.length() - fileExtensionLength));
      this.ste.writeResultsToExcel(this.results);
      if (this.saveResultImageCheckBox.isSelected()) {
         ImagePlus imageResultFlattenen = this.imageResult.flatten();
         IJ.saveAs(imageResultFlattenen, "jpg", this.imageFile.getAbsolutePath() + " result.jpg");
      }
   }

   public static void updateStatus(final int i, final String s) {
      Runnable doSetProgressBarValue = new Runnable() {
         @Override
         public void run() {
            AngioToolGUI.progressBar.setValue(i);
            AngioToolGUI.progressBar.setString(s + " " + i + "%");
         }
      };
      SwingUtilities.invokeLater(doSetProgressBarValue);
   }

   public void updateOverlay() {
      if (this.imageResult != null) {
         this.allantoisOverlay.clear();
         this.showOverlay = this.showOverlayCheckBox.isSelected();
         this.showOutline = this.showOutlineCheckBox.isSelected();
         this.showSkeleton = this.showSkeletonCheckBox.isSelected();
         this.showBranchingPoints = this.showBranchingPointsCheckBox.isSelected();
         this.showConvexHull = this.showConvexHullCheckBox.isSelected();
         this.OutlineColor = this.outlineRoundedPanel.getBackground();
         this.SkeletonColor = this.skeletonColorRoundedPanel.getBackground();
         this.BranchingPointColor = this.branchingPointsRoundedPanel.getBackground();
         this.ConvexHullColor = this.convexHullRoundedPanel.getBackground();
         if (this.showOverlay) {
            if (this.outlineRoi != null && this.showOutline) {
               this.outlineRoi.setStrokeColor(this.OutlineColor);
               this.OutlineStrokeWidth = (int)this.outlineSpinner.getValue();
               this.outlineRoi.setStrokeWidth((float)this.OutlineStrokeWidth);
               this.allantoisOverlay.add(this.outlineRoi);
            }

            if (this.graph != null) {
               this.SkeletonStrokeWidth = (int)this.skeletonSpinner.getValue();
               this.skeletonRoi = this.computeSkeletonRoi(this.graph, this.SkeletonColor, this.SkeletonStrokeWidth);
            }

            if (this.skeletonRoi != null && this.showSkeleton) {
               this.SkeletonStrokeWidth = (int)this.skeletonSpinner.getValue();

               for(int i = 0; i < this.skeletonRoi.size(); ++i) {
                  Roi r = (Roi)this.skeletonRoi.get(i);
                  r.setStrokeWidth((float)this.SkeletonStrokeWidth);
                  r.setStrokeColor(this.SkeletonColor);
                  this.allantoisOverlay.add(r);
               }

               for(int i = 0; i < this.removedJunctions.size(); ++i) {
                  Point p = this.removedJunctions.get(i);
                  OvalRoi r = new OvalRoi(p.x, p.y, 1, 1);
                  r.setStrokeWidth((float)this.SkeletonStrokeWidth);
                  r.setStrokeColor(this.SkeletonColor);
                  this.allantoisOverlay.add(r);
               }
            }

            if (this.al2 != null) {
               this.junctionsRoi = this.computeJunctionsRoi(this.al2, this.branchingPointsRoundedPanel.getBackground(), (int)this.branchingPointsSpinner.getValue());
            }

            if (this.junctionsRoi != null && this.showBranchingPoints) {
               this.BranchingPointsStrokeWidth = (int)this.branchingPointsSpinner.getValue();

               for(int i = 0; i < this.junctionsRoi.size(); ++i) {
                  Roi r = (Roi)this.junctionsRoi.get(i);
                  r.setStrokeWidth((float)this.BranchingPointsStrokeWidth);
                  r.setStrokeColor(this.BranchingPointColor);
                  this.allantoisOverlay.add(r);
               }
            }

            if (this.convexHullRoi != null && this.showConvexHull) {
               this.convexHullRoi.setStrokeColor(this.ConvexHullColor);
               this.ConvexHullStrokeWidth = (int)this.convexHullSizeSpinner.getValue();
               this.convexHullRoi.setStrokeWidth((float)this.ConvexHullStrokeWidth);
               this.allantoisOverlay.add(this.convexHullRoi);
            }
         }

         this.imageResult.setOverlay(this.allantoisOverlay);
      }
   }

   private void updatePreferences() {
      ATPreferences.settings.showOverlay = this.showOverlay;
      ATPreferences.settings.showOutline = this.showOutline;
      ATPreferences.settings.showSkeleton = this.showSkeleton;
      ATPreferences.settings.showBranchingPoints = this.showBranchingPoints;
      ATPreferences.settings.showConvexHull = this.showConvexHull;
      ATPreferences.settings.OutlineStrokeWidth = this.OutlineStrokeWidth;
      ATPreferences.settings.SkeletonStrokeWidth = this.SkeletonStrokeWidth;
      ATPreferences.settings.BranchingPointsStrokeWidth = this.BranchingPointsStrokeWidth;
      ATPreferences.settings.ConvexHullStrokeWidth = this.ConvexHullStrokeWidth;
      ATPreferences.settings.OutlineColor = Utils.Color2Hex(this.OutlineColor);
      ATPreferences.settings.SkeletonColor = Utils.Color2Hex(this.SkeletonColor);
      ATPreferences.settings.BranchingPointsColor = Utils.Color2Hex(this.BranchingPointColor);
      ATPreferences.settings.ConvexHullColor = Utils.Color2Hex(this.ConvexHullColor);
      ATPreferences.settings.currentDir = this.currentDir.getAbsolutePath();
      ATPreferences.settings.imageResultFormat = this.imageResultFormat;
      ATPreferences.settings.computeLacunarity = this.computeLacunarity;
      ATPreferences.settings.computeThickness = this.computeThickness;
   }

   private void updateOutline() {
      if (this.tubenessIp == null)
         return;

      if (this.sigmaIsChanged) {
         ImageProcessor ip = new ByteProcessor(this.tubenessIp.getWidth(), this.tubenessIp.getHeight());

         for(int i = 0; i < this.currentSigmas.size(); ++i) {
            double s = this.currentSigmas.get(i);

            for(int si = 0; si < this.sI.size(); ++si) {
               AngioToolGUI.sigmaImages siTemp = this.sI.get(si);
               if (siTemp.sigma == s) {
                  ImageProcessor tempIp = siTemp.tubenessImage.duplicate();
                  tempIp.copyBits(ip, 0, 0, 13);
                  ip = tempIp;
                  break;
               }
            }
         }

         this.tubenessIp = ip.duplicate();
      }

      ImageProcessor temp = this.tubenessIp.duplicate();
      temp = temp.convertToByte(true);
      Utils.thresholdFlexible(temp, this.thresholdRangeSliderLow.getValue(), this.thresholdRangeSliderHigh.getValue());
      this.imageThresholded.setProcessor(temp);
      temp.setThreshold(255.0, 255.0, 2);
      ImageProcessor check = this.imageThresholded.getProcessor().duplicate();
      int iterations = 2;
      if (!Utils.isReleaseVersion) {
         System.out.println("Closing with " + iterations + " iterations");
      }

      for(int i = 0; i < iterations; ++i) {
         this.imageThresholded.getProcessor().erode();
      }

      for(int i = 0; i < iterations; ++i) {
         this.imageThresholded.getProcessor().dilate();
      }

      if (this.smallParticlesCheckBox.isSelected()) {
         Utils.fillHoles(this.imageThresholded, 0, (int)this.smallParticlesRangeSlider2.getValue(), 0.0, 1.0, 0);
      }

      if (this.fillHolesCheckBox.isSelected()) {
         this.imageThresholded.killRoi();
         ImageProcessor temp1 = this.imageThresholded.getProcessor();
         temp1.invert();
         Utils.fillHoles(this.imageThresholded, 0, (int)this.fillHolesRangeSlider2.getValue(), 0.0, 1.0, 0);
         temp1.invert();
      }

      ImagePlus iplus = new ImagePlus("tubenessIp", this.imageThresholded.getProcessor());
      this.outlineRoi = Utils.thresholdToSelection(iplus);
      this.outlineRoi.setStrokeWidth((float)((Integer)this.outlineSpinner.getValue()).intValue());
      this.allantoisOverlay.clear();
      this.allantoisOverlay.add(this.outlineRoi);
      this.allantoisOverlay.setStrokeColor(this.outlineRoundedPanel.getBackground());
      this.imageResult.setOverlay(this.allantoisOverlay);
      this.sigmaIsChanged = false;
      this.fillHolesIsChanged = false;
   }

   private void initVariables() {
      if (this.imageFile != null) {
         this.imageFile = null;
         this.imageFile = new File("");
      }

      this.sigmasMarkSlider.resetAll();
      this.sigmasMarkSlider.setMaximum(100);
      this.sigmasMarkSlider.setEnabled(false);
      this.sigmasSpinner.setValue(100);
      this.sigmaIsChanged = false;
      this.minSigma = Integer.MAX_VALUE;
      this.maxSigma = Integer.MIN_VALUE;
      this.allSigmas = new ArrayList<>();
      this.currentSigmas = new ArrayList<>();
      this.thresholdRangeSliderLow.setEnabled(false);
      this.thresholdRangeSliderHigh.setEnabled(false);
      this.thresholdRangeSliderLow.setValue(15);
      this.thresholdRangeSliderHigh.setValue(255);
      this.doScaling = false;
      this.fillHoles = false;
      this.fillHolesCheckBox.setSelected(this.fillHoles);
      this.fillHolesCheckBox.setEnabled(false);
      this.fillHolesIsChanged = false;
      this.smallParticles = false;
      this.smallParticlesCheckBox.setSelected(false);
      this.smallParticlesCheckBox.setEnabled(this.smallParticles);
      this.smallParticlesIsChanged = false;
      if (this.imageOriginal != null) {
         this.imageOriginal.close();
         this.imageOriginal.flush();
      }

      if (this.imageResult != null) {
         this.imageResult.close();
         this.imageResult.flush();
      }

      this.imageResult = new ImagePlus();
      this.imageResult.setTitle("Result");
      if (this.imageThresholded != null) {
         this.imageThresholded.close();
         this.imageThresholded.flush();
      }

      this.imageThresholded = new ImagePlus();
      this.imageThresholded.setTitle("Thresholded");
      if (this.imageTubeness != null) {
         this.imageTubeness.close();
         this.imageTubeness.flush();
      }

      this.imageTubeness = new ImagePlus();
      this.imageTubeness.setTitle("Tubeness");
      this.sI = new ArrayList<>();
      this.allantoisOverlay = new Overlay();
      this.results = new Results();
      this.results.computeLacunarity = this.computeLacunarity;
      this.results.computeThickness = this.computeThickness;

      for(int i = 0; i < 10; ++i) {
         System.gc();
      }
   }

        

   private void smoothROIs(int fraction) {
      ShapeRoi sr = (ShapeRoi)Utils.thresholdToSelection(this.imageThresholded);
      ForkShapeRoiSplines fs = new ForkShapeRoiSplines();
      ShapeRoi tempSr = fs.computeSplines(sr, 5);
      this.imageThresholded.getProcessor().setColor(Color.black);
      this.imageThresholded.getProcessor().fill();
      Utils.selectionToThreshold(tempSr, this.imageThresholded);
      this.outlineRoi = Utils.thresholdToSelection(this.imageThresholded);
      this.allantoisOverlay.clear();
      this.allantoisOverlay.add(tempSr);
      this.allantoisOverlay.setStrokeColor(Color.yellow);
      this.imageThresholded.setOverlay(this.allantoisOverlay);
      this.imageResult.setOverlay(this.allantoisOverlay);
   }

   public void computeLacunarity(ImagePlus iplus, int numBoxes, int minBoxSize, int slideXY) {
      Lacunarity l = new Lacunarity(iplus, numBoxes, minBoxSize, slideXY, true);
      this.ElSlope = l.getEl3Slope();
      this.FlSlope = l.getFl3Slope();
      this.lacunarityBoxes = l.getBoxes();
      this.Elamdas = l.getEoneplusl3();
      this.medialELacunarity = l.getMedialELacunarity();
      this.meanEl = l.getMeanEl();
      this.Flamdas = l.getFoneplusl3();
      this.medialFLacunarity = l.getMedialFLacunarity();
      this.meanFl = l.getMeanFl();
   }

   private ArrayList<Roi> computeSkeletonRoi(Graph[] graph, Color color, int size) {
      ArrayList<Roi> r = new ArrayList();

      for(int g = 0; g < graph.length; ++g) {
         ArrayList<Edge> edges = graph[g].getEdges();

         for(int e = 0; e < edges.size(); ++e) {
            Edge edge = edges.get(e);
            ArrayList<Point> points = edge.getSlabs();

            for(int p1 = 0; p1 < points.size(); ++p1) {
               OvalRoi or = new OvalRoi(points.get(p1).x - size / 2, points.get(p1).y - size / 2, size, size);
               r.add(or);
            }
         }
      }

      return r;
   }

   private ArrayList<Roi> computeJunctionsRoi(ArrayList<Point> al, Color color, int size) {
      ArrayList<Roi> r = new ArrayList();

      for(int i = 0; i < al.size(); ++i) {
         Point p = al.get(i);
         OvalRoi or = new OvalRoi(p.x - size / 2, p.y - size / 2, size, size);
         r.add(or);
      }

      return r;
   }

   private Object updateSigmas(ArrayList<Integer> s) {
      this.upateAllSigmas(s);
      this.updateCurrentSimgas(s);
      return "";
   }

   private void upateAllSigmas(ArrayList<Integer> s) {
      if (this.ipOriginal == null)
         return;

      for(int i = 0; i < s.size(); ++i) {
         double sigma = (double)s.get(i).intValue();
         if (!this.allSigmas.contains(sigma)) {
            this.allSigmas.add(sigma);
            Tubeness t = new Tubeness();
            double[] sigmaDouble = new double[]{sigma};
            this.imageTubeness = t.runTubeness(new ImagePlus("", this.ipOriginal), 100, sigmaDouble, false);
            this.sI.add(new AngioToolGUI.sigmaImages(sigma, this.imageTubeness.getProcessor()));
         }

         updateStatus(i / s.size() * 100, "computing outline... ");
      }
   }

   private void updateCurrentSimgas(ArrayList<Integer> s) {
      this.currentSigmas.clear();

      for(int i = 0; i < s.size(); ++i) {
         this.currentSigmas.add((double)s.get(i).intValue());
      }
   }

   private void updateSigmas(int low, int high) {
      this.upateAllSigmas(low, high);
      this.updateCurrentSimgas(low, high);
   }

   private void upateAllSigmas(int low, int high) {
      if (this.ipOriginal == null)
         return;

      if (!this.allSigmas.contains((double)low)) {
         this.allSigmas.add((double)low);
         Tubeness t = new Tubeness();
         double[] s = new double[]{(double)low};
         this.imageTubeness = t.runTubeness(new ImagePlus("", this.ipOriginal), 100, s, false);
         this.sI.add(new AngioToolGUI.sigmaImages((double)low, this.imageTubeness.getProcessor()));
      }

      if (!this.allSigmas.contains((double)high)) {
         this.allSigmas.add((double)high);
         Tubeness t = new Tubeness();
         double[] s = new double[]{(double)high};
         this.imageTubeness = t.runTubeness(new ImagePlus("", this.ipOriginal), 100, s, false);
         this.sI.add(new AngioToolGUI.sigmaImages((double)high, this.imageTubeness.getProcessor()));
      }

      Collections.sort(this.allSigmas);
   }

   private void updateCurrentSimgas(int low, int high) {
      this.currentSigmas.clear();

      for(int i = 0; i < this.allSigmas.size(); ++i) {
         double s = this.allSigmas.get(i);
         if (s >= (double)low && s <= (double)high && !this.currentSigmas.contains(s)) {
            this.currentSigmas.add(s);
         }
      }
   }

   private void preCompute() {
      this.initControls();
      this.computeFirstOutline(this.firstSigma);
   }

   private void initControls() {
      if (this.ipOriginal != null) {
         int width = this.ipOriginal.getWidth();
         int height = this.ipOriginal.getHeight();
         this.minSigma = 1;
         this.maxSigma = (int)Math.sqrt((double)(width * width + height * height)) / 70;
         this.maxSigma = Utils.roundIntegerToNearestUpperTenth(this.maxSigma);
      }
      else {
         this.minSigma = 1;
         this.maxSigma = 50;
      }
      this.sigmasMarkSlider.setEnabled(true);
      this.sigmasMarkSlider.resetAll();
      this.sigmasMarkSlider.setMaximum(this.minSigma);
      this.sigmasMarkSlider.setMaximum(this.maxSigma);
      this.sigmasSpinner.setEnabled(true);
      this.sigmasSpinner.setValue(this.maxSigma);
      this.thresholdRangeSliderLow.setEnabled(true);
      this.thresholdRangeSliderHigh.setEnabled(true);
      this.thresholdRangeSliderLow.setValue(15);
      this.lowThresholdTextField.setText("" + this.thresholdRangeSliderLow.getValue());
      this.highThresholdTextField.setText("" + this.thresholdRangeSliderHigh.getValue());
      this.fillHolesCheckBox.setEnabled(true);
      this.fillHolesCheckBox.setSelected(false);
      this.fillHolesRangeSlider2.setEnabled(this.fillHolesCheckBox.isSelected());
      this.fillHolesRangeSlider2.setMinimum(this.minSigma - 1);
      this.fillHolesRangeSlider2.setMaximum(30 * this.maxSigma);
      this.fillHolesRangeSlider2.setMajorTickSpacing(30 * this.maxSigma / 10);
      this.fillHolesSpinner.setEnabled(this.fillHolesCheckBox.isSelected());
      this.fillHolesSpinner.setValue(30 * this.maxSigma);
      this.smallParticlesCheckBox.setEnabled(true);
      this.smallParticlesCheckBox.setSelected(false);
      this.smallParticlesRangeSlider2.setEnabled(this.smallParticlesCheckBox.isSelected());
      this.smallParticlesRangeSlider2.setMinimum(this.minSigma - 1);
      this.smallParticlesRangeSlider2.setMaximum(40 * this.maxSigma);
      this.smallParticlesRangeSlider2.setMajorTickSpacing(40 * this.maxSigma / 10);
      this.removeSmallParticlesSpinner.setEnabled(this.smallParticlesCheckBox.isSelected());
      this.removeSmallParticlesSpinner.setValue(40 * this.maxSigma / 10);
      this.firstSigma[0] = (double)Math.round((float)(this.maxSigma / 4));
      this.toggleOverlayToggleButton.setEnabled(true);
      this.outlineSpinner.setValue(1);
      this.skeletonSpinner.setValue(this.maxSigma / 8);
      this.branchingPointsSpinner.setValue(this.maxSigma / 5);
      this.convexHullSizeSpinner.setValue(1);
   }

   private void computeFirstOutline(double[] sigmas) {
      Tubeness t = new Tubeness();
      this.imageTubeness = t.runTubeness(new ImagePlus("imageTubeness", this.ipOriginal), 100, sigmas, false);
      this.tubenessIp = this.imageTubeness.getProcessor();
      this.sI.add(new AngioToolGUI.sigmaImages(sigmas[0], this.tubenessIp));
      this.sigmasMarkSlider.addMark((int)sigmas[0]);
      this.allSigmas.add(sigmas[0]);
      this.currentSigmas.add(sigmas[0]);
      this.ipThresholded = this.tubenessIp.duplicate();
      this.ipThresholded = this.ipThresholded.convertToByte(this.doScaling);
      this.imageThresholded.setProcessor(this.ipThresholded);
      Utils.thresholdFlexible(this.ipThresholded, this.thresholdRangeSliderLow.getValue(), this.thresholdRangeSliderHigh.getValue());
      this.ipThresholded.setThreshold(255.0, 255.0, 2);
      this.sigmaIsChanged = false;
      this.fillHolesIsChanged = false;
      this.smallParticlesIsChanged = false;
      this.updateOutline();
   }

   private void initLookAndFeel() {
      String lookAndFeel = null;
      if ("System" != null) {
         if ("System".equals("Metal")) {
            lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
         } else if ("System".equals("System")) {
            lookAndFeel = UIManager.getSystemLookAndFeelClassName();
         } else if ("System".equals("Motif")) {
            lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
         } else if ("System".equals("GTK")) {
            lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
         } else if ("System".equals("Nimbus")) {
            lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
         } else {
            lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
         }

         try {
            UIManager.setLookAndFeel(lookAndFeel);
            if ("System".equals("Metal")) {
               if ("Test".equals("DefaultMetal")) {
                  MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
               } else if ("Test".equals("Ocean")) {
                  MetalLookAndFeel.setCurrentTheme(new OceanTheme());
               }

               UIManager.setLookAndFeel(new MetalLookAndFeel());
            }
         } catch (ClassNotFoundException var3) {
            var3.printStackTrace();
         } catch (UnsupportedLookAndFeelException var4) {
            var4.printStackTrace();
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

      SwingUtilities.updateComponentTreeUI(this);
   }

   private void exit() {
      this.stopDate = new Date();
      this.updatePreferences();
      ATPreferences.setPreferences();
   }

   private AnalyzerParameters procureAnalyzerSettings() {
      double scale = 1.0;
      if (!this.distanceInMMNumberTextField.getText().equals("") && !AngioToolGUI.this.distanceInPixelsNumberTextField.getText().equals("")) {
         int pixelDistance = Math.max(AngioToolGUI.this.distanceInPixelsNumberTextField.getInt(), 1);
         scale = this.distanceInMMNumberTextField.getDouble() / (double)pixelDistance;
      }

      ArrayList<Integer> marks = this.sigmasMarkSlider.getMarks();
      if (marks.isEmpty())
         marks.add((int)this.firstSigma[0]);

      int[] marksArray = new int[marks.size()];
      for (int i = 0; i < marksArray.length; i++)
         marksArray[i] = marks.get(i);

      AnalyzerParameters params = new AnalyzerParameters(
         null,
         "",
         this.saveResultImageCheckBox.isSelected(),
         "",
         this.resizeImageCheckBox.isSelected(),
         this.resizingFactor,
         this.smallParticles,
         (int)this.smallParticlesRangeSlider2.getValue(),
         this.fillHoles,
         (int)this.fillHolesRangeSlider2.getValue(),
         marksArray,
         this.thresholdRangeSliderHigh.getValue(),
         this.thresholdRangeSliderLow.getValue(),
         true,
         scale,
         this.showOutlineCheckBox.isSelected(),
         this.outlineRoundedPanel.getBackground(),
         (float)((Integer)this.outlineSpinner.getValue()).intValue(),
         this.showSkeletonCheckBox.isSelected(),
         this.skeletonColorRoundedPanel.getBackground(),
         (int)this.skeletonSpinner.getValue(),
         this.showBranchingPointsCheckBox.isSelected(),
         this.branchingPointsRoundedPanel.getBackground(),
         (int)this.branchingPointsSpinner.getValue(),
         this.showConvexHullCheckBox.isSelected(),
         this.convexHullRoundedPanel.getBackground(),
         (int)this.convexHullSizeSpinner.getValue(),
         this.computeLacunarity,
         false
      );

      return params;
   }

   private String doSingleAnalysis() {
        int progress = 0;
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
        updateStatus(progress, "");
        this.updateOutline();
        int splineIterations = 0;
        int fraction = 1;

        for(int i = 0; i < splineIterations; ++i) {
            this.smoothROIs(fraction);
            progress += 11;
            updateStatus(progress, "smooth ROIs");
        }

        this.results.vesselPixelArea = Utils.thresholdedPixelArea(this.imageThresholded.getProcessor());
        if (this.computeLacunarity) {
            updateStatus(progress, "Computing lacunarity...");
            ImageProcessor ipTemp = this.imageThresholded.getProcessor().duplicate();
            ImagePlus iplusTemp = new ImagePlus("iplusTemp", ipTemp);
            if (!Utils.isReleaseVersion) {
                IJ.saveAs(iplusTemp, "tiff", this.imageFile.getAbsolutePath() + " lacunarity.tif");
            }

            this.computeLacunarity(iplusTemp, 10, 10, 5);
            this.results.ELacunaritySlope = this.ElSlope;
            this.results.ELacunarity = this.medialELacunarity;
            this.results.FLacunaritySlope = this.FlSlope;
            this.results.FLacuanrity = this.medialFLacunarity;
            this.results.meanEl = this.meanEl;
            this.results.meanFl = this.meanFl;
            if (!Utils.isReleaseVersion) {
                for(int i = 0; i < this.lacunarityBoxes.length; ++i) {
                    System.out.println(this.lacunarityBoxes[i] + "\t" + this.Elamdas.get(i) + "\t" + this.Flamdas.get(i));
                }

                System.out.println(
                    "ElSlope =  "
                    + this.ElSlope
                    + "\tFlSlope = "
                    + this.FlSlope
                    + "\tmedialELacuanrity = "
                    + this.medialELacunarity
                    + "\tmeanEl= "
                    + this.meanEl
                    + "\tmeanFl= "
                    + this.meanFl
                );
            }
        }

        this.computeThickness = !Utils.isReleaseVersion;
        if (this.computeThickness) {
            updateStatus(progress, "vessel thickness");
            EDT_S1D ed = new EDT_S1D();
            ed.setup(null, this.imageThresholded);
            ed.run(this.imageThresholded.getProcessor());
            this.imageThickness = ed.getImageResult();
            if (!Utils.isReleaseVersion) {
                IJ.saveAs(this.imageThickness, "tif", this.imageFile.getAbsolutePath() + " thickness.tif");
            }
        }

        this.convexHull = Utils.computeConvexHull(this.imageThresholded.getProcessor());
        this.convexHullArea = this.convexHull.area();
        this.convexHullRoi = new PolygonRoi(this.convexHull.polygon(), 2);

        progress += 5;
        updateStatus(progress, "Analyzing skeleton... ");
        updateStatus(progress, "Skeletonize");

        ImageProcessor ip = this.imageThresholded.getProcessor();
        ip = Utils.skeletonize(ip, "itk");
        this.ipThresholded = ip;
        progress += 33;
        updateStatus(progress, "Computing convex hull... ");
        new AnalyzeSkeleton();
        ImageProcessor ipSkeleton = this.ipThresholded.duplicate();
        ImagePlus iplusSkeleton = new ImagePlus("iplusSkeleton", ipSkeleton);
        AnalyzeSkeleton var16 = new AnalyzeSkeleton();
        var16.setup("", iplusSkeleton);
        this.skelResult = var16.run(0, false, false, iplusSkeleton, false, false);
        this.graph = var16.getGraphs();
        this.skeletonRoi = this.computeSkeletonRoi(this.graph, this.skeletonColorRoundedPanel.getBackground(), (int)this.skeletonSpinner.getValue());
        this.al2 = this.skelResult.getListOfJunctionVoxels();
        this.removedJunctions = Utils.computeActualJunctions(this.al2);
        this.junctionsRoi = this.computeJunctionsRoi(this.al2, this.branchingPointsRoundedPanel.getBackground(), (int)this.branchingPointsSpinner.getValue());
        this.updateOverlay();
        updateStatus(95, " Saving result image... ");
        updateStatus(100, "Done... ");
        return "Good";
   }

   class AngioToolWorker extends SwingWorker<Object, Object> {

      @Override
      public Object doInBackground() {
         return doSingleAnalysis();
      }

      @Override
      protected void done() {
         try {
            if (!Utils.isReleaseVersion) {
               System.out.println("Done starting worker");
            }

            AngioToolGUI.this.populateResults();
         } catch (Exception var2) {
         }
      }
   }

   private class sigmaImages {
      double sigma;
      ImageProcessor tubenessImage;

      public sigmaImages(double sigma, ImageProcessor tubenessImage) {
         this.sigma = sigma;
         this.tubenessImage = tubenessImage;
      }

      public void showImage() {
         ImagePlus iplus = new ImagePlus("sigma " + this.sigma, this.tubenessImage);
         iplus.show();
      }

      @Override
      public String toString() {
         return "sigma " + this.sigma + " tubenessIamge= " + this.tubenessImage;
      }
   }
}
