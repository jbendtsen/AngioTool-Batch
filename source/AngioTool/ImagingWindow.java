package AngioTool;

import Algorithms.PreprocessColor;
import Pixels.ArgbBuffer;
import Pixels.Canvas;
import Pixels.ImageFile;
import Utils.FloatBufferPool;
import Utils.ISliceRunner;
import Utils.Misc;
import Utils.RefVector;
import Xlsx.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class ImagingWindow extends JFrame implements ActionListener, KeyListener
{
    public static class ImagingDisplay extends JPanel implements MouseMotionListener, MouseWheelListener
    {
        public static final double ZOOM_BASE = 1.2;

        boolean waiting = false;
        boolean isTimerActive = false;
        AnalyzerParameters pendingParams = null;

        ArgbBuffer source;
        int[] rowScratch;
        float[] blurWnd;
        float[] luminanceTable;
        int imgWidth;
        int imgHeight;

        BufferedImage drawingImage;
        Color backgroundColor;
        Color overlayBackColor;
        Color[] wheelColors;
        int loadTicks;
        Rectangle areaRect;

        AnalyzerParameters currentParams;
        Analyzer.Stats currentStats;
        RefVector<String> statsStrings;
        int statsWidth;
        int statsLineHeight;
        boolean shouldShowStats;

        int zoomLevels;
        boolean wasDragging;
        int panStartX;
        int panStartY;
        int panX;
        int panY;
        double imageOriginX;
        double imageOriginY;

        final Timer loadingTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (!waiting) {
                    isTimerActive = false;
                    loadingTimer.stop();
                }
                else {
                    isTimerActive = true;
                    loadTicks++;
                    repaint();
                }
            }
        });

        public ImagingDisplay(ArgbBuffer source, boolean initiallyShowStats)
        {
            this.source = source;
            this.imgWidth = source.width;
            this.imgHeight = source.height;
            this.rowScratch = new int[Math.max(imgWidth, imgHeight)];
            this.blurWnd = new float[25 * 3];
            this.luminanceTable = new float[255];
            this.drawingImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
            this.backgroundColor = new Color(0);
            this.overlayBackColor = new Color(32, 48, 128, 192);
            this.wheelColors = new Color[8];
            this.loadTicks = 0;
            this.areaRect = new Rectangle();

            this.currentParams = null;
            this.currentStats = null;
            this.statsStrings = new RefVector<>(String.class);
            this.statsWidth = 0;
            this.shouldShowStats = initiallyShowStats;

            this.zoomLevels = 0;
            this.wasDragging = false;
            this.panStartX = 0;
            this.panStartY = 0;
            this.panX = 0;
            this.panY = 0;
            this.imageOriginX = 0.5 * imgWidth;
            this.imageOriginY = 0.5 * imgHeight;

            final int dull = 64;
            final int factor = (256 - dull) / (wheelColors.length - 1);
            for (int i = 0; i < wheelColors.length; i++) {
                int lum = dull + (wheelColors.length - i - 1) * factor;
                wheelColors[i] = new Color(lum, lum, lum);
            }

            this.addMouseMotionListener(this);
            this.addMouseWheelListener(this);
        }

        public void setNewImage(ArgbBuffer newImage)
        {
            if (source != null)
                ImageFile.releaseImage(source);

            source = newImage;
            imgWidth = source.width;
            imgHeight = source.height;
            rowScratch = new int[Math.max(imgWidth, imgHeight)];
            drawingImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
            imageOriginX = 0.5 * imgWidth;
            imageOriginY = 0.5 * imgHeight;
        }

        public static double getZoomFactor(int zoomIndex)
        {
            double rounding = (1 << (Math.min(Math.max(-zoomIndex + 2, 2), 16)));
            return Math.floor(Math.pow(ZOOM_BASE, zoomIndex) * rounding + 0.5) / rounding;
        }

        @Override
        public Dimension getPreferredSize()
        {
            return new Dimension(imgWidth, imgHeight);
        }

        @Override
        public void paintComponent(Graphics g)
        {
            g.getClipBounds(areaRect);

            if (areaRect.width <= 0 || areaRect.height <= 0 || imgWidth <= 0 || imgHeight <= 0) {
                super.paintComponent(g);
                return;
            }

            if (!waiting || !isTimerActive) {
                double zoomFactor = getZoomFactor(this.zoomLevels);

                double wRatio = (double)areaRect.width / (double)this.imgWidth;
                double hRatio = (double)areaRect.height / (double)this.imgHeight;

                double scaleFactor = 1.0 / (zoomFactor * Math.min(wRatio, hRatio));

                int dPanX = this.panStartX - this.panX;
                if (dPanX != 0)
                    this.imageOriginX += dPanX * scaleFactor;

                int dPanY = this.panStartY - this.panY;
                if (dPanY != 0)
                    this.imageOriginY += dPanY * scaleFactor;

                this.panStartX = this.panX;
                this.panStartY = this.panY;

                int srcX = (int)(this.imageOriginX - areaRect.width * 0.5 * scaleFactor + 0.5);
                int srcY = (int)(this.imageOriginY - areaRect.height * 0.5 * scaleFactor + 0.5);
                int srcW = (int)(areaRect.width * scaleFactor + 0.5);
                int srcH = (int)(areaRect.height * scaleFactor + 0.5);

                int canvasX1 = Math.max((int)(-srcX / scaleFactor + 0.5), 0);
                int canvasY1 = Math.max((int)(-srcY / scaleFactor + 0.5), 0);
                int canvasX2 = Math.min((int)((this.imgWidth - srcX) / scaleFactor + 0.5), areaRect.width);
                int canvasY2 = Math.min((int)((this.imgHeight - srcY) / scaleFactor + 0.5), areaRect.height);

                g.drawImage(drawingImage,
                    canvasX1,
                    canvasY1,
                    canvasX2,
                    canvasY2,
                    Math.max(srcX, 0),
                    Math.max(srcY, 0),
                    Math.min(srcX + srcW, this.imgWidth),
                    Math.min(srcY + srcH, this.imgHeight),
                    backgroundColor,
                    null
                );

                g.setColor(backgroundColor);
                if (canvasX1 > 0)
                    g.fillRect(0, canvasY1, canvasX1, canvasY2 - canvasY1);
                if (canvasX2 < areaRect.width)
                    g.fillRect(canvasX2, canvasY1, areaRect.width - canvasX2, canvasY2 - canvasY1);
                if (canvasY1 > 0)
                    g.fillRect(0, 0, areaRect.width, canvasY1);
                if (canvasY2 < areaRect.height)
                    g.fillRect(0, canvasY2, areaRect.width, areaRect.height - canvasY2);
            }

            if (waiting) {
                int centerX = areaRect.width / 2;
                int centerY = areaRect.height / 2;

                for (int i = 0; i < wheelColors.length; i++) {
                    double angle = 2.0 * Math.PI * i / wheelColors.length;
                    int x = (int)(20.0 * Math.cos(angle) + 0.5);
                    int y = (int)(20.0 * Math.sin(angle) + 0.5);

                    g.setColor(wheelColors[(loadTicks + i) % wheelColors.length]);
                    g.fillOval(centerX + x - 6, centerY + y - 6, 13, 13);
                }
            }
            else if (shouldShowStats && this.currentParams != null && this.currentStats != null) {
                if (this.statsStrings.size == 0) {
                    double linearScaleFactor = currentParams.linearScalingFactor > 0.0 ? currentParams.linearScalingFactor : 1.0;
                    double areaScaleFactor = linearScaleFactor * linearScaleFactor;
                    double imageArea = currentStats.imageWidth * currentStats.imageHeight * areaScaleFactor;
                    double allantoisPercentage = currentStats.allantoisMMArea * 100.0 / imageArea;
                    double junctionsAreaPercentage = 100.0 * currentStats.junctionsPerScaledArea;

                    statsStrings.add(
                        "Width x Height: " +
                        currentStats.imageWidth + " x " + currentStats.imageHeight
                    );
                    statsStrings.add(
                        "Explant Area, %: " +
                        Misc.formatDouble(currentStats.allantoisMMArea, 2) + ", " +
                        Misc.formatDouble(allantoisPercentage, 3) + "%"
                    );
                    statsStrings.add(
                        "Vessels Area, %: " +
                        Misc.formatDouble(currentStats.vesselMMArea, 2) + ", " +
                        Misc.formatDouble(currentStats.vesselPercentageArea, 3) + "%"
                    );
                    statsStrings.add(
                        "Total Junctions: " +
                        Misc.formatDouble(currentStats.totalNJunctions)
                    );
                    statsStrings.add(
                        "Junctions Density %: " +
                        Misc.formatDouble(junctionsAreaPercentage, 5) + "%"
                    );
                    statsStrings.add(
                        "Vessels Length: Total, Average: " +
                        Misc.formatDouble(currentStats.totalLength, 3) + ", " +
                        Misc.formatDouble(currentStats.averageBranchLength, 3)
                    );
                    statsStrings.add(
                        "End Points: " +
                        currentStats.totalNEndPoints
                    );

                    if (currentParams.shouldComputeThickness) {
                        statsStrings.add(
                            "Average Vessel Diameter: " +
                            Misc.formatDouble(currentStats.averageVesselDiameter, 3)
                        );
                    }
                    if (currentParams.shouldComputeLacunarity) {
                        statsStrings.add(
                            "E Lacunarity: Medial, Mean, Gradient: " +
                            Misc.formatDouble(currentStats.ELacunarityMedial, 4) + ", " +
                            Misc.formatDouble(currentStats.meanEl, 4) + ", " +
                            Misc.formatDouble(currentStats.ELacunarityCurve, 4)
                        );
                        statsStrings.add(
                            "F Lacunarity: Medial, Mean, Gradient: " +
                            Misc.formatDouble(currentStats.FLacunarityMedial, 4) + ", " +
                            Misc.formatDouble(currentStats.meanFl, 4) + ", " +
                            Misc.formatDouble(currentStats.FLacunarityCurve, 4)
                        );
                    }

                    FontMetrics metrics = g.getFontMetrics();
                    int widestWidth = 0;
                    for (int i = 0; i < statsStrings.size; i++)
                        widestWidth = Math.max(widestWidth, metrics.stringWidth(statsStrings.buf[i]));

                    this.statsWidth = widestWidth;
                    this.statsLineHeight = metrics.getHeight();
                }

                g.setColor(overlayBackColor);
                g.fillRect(4, 4, this.statsWidth + 16, this.statsLineHeight * this.statsStrings.size + 16);

                g.setColor(Color.WHITE);
                for (int i = 0; i < this.statsStrings.size; i++)
                    g.drawString(statsStrings.buf[i], 12, (i+1) * this.statsLineHeight + 8);
            }
        }

        @Override
        public void mouseDragged(MouseEvent evt)
        {
            if (waiting)
                return;

            int x = evt.getX();
            int y = evt.getY();
            if (x != this.panX || y != this.panY) {
                if (!wasDragging) {
                    this.panStartX = x;
                    this.panStartY = y;
                }
                this.panX = x;
                this.panY = y;
                this.wasDragging = true;
                this.repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent evt)
        {
            this.wasDragging = false;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent evt)
        {
            if (waiting)
                return;

            int clicks = evt.getWheelRotation();
            if (clicks != 0)
                zoomBy(-clicks, evt);
        }

        public void zoomBy(int levels, MouseWheelEvent evt)
        {
            int prevZoom = this.zoomLevels;
            this.zoomLevels = Math.min(Math.max(this.zoomLevels + levels, -16), 32);

            if (evt != null) {
                int x1 = evt.getX();
                int y1 = evt.getY();
                int halfW = this.areaRect.width / 2;
                int halfH = this.areaRect.height / 2;
                int dx = x1 - halfW;
                int dy = y1 - halfH;
                double dz = getZoomFactor(this.zoomLevels) / getZoomFactor(prevZoom);
                int x2 = halfW + (int)(dx * dz + 0.5);
                int y2 = halfH + (int)(dy * dz + 0.5);

                this.panStartX = x2;
                this.panStartY = y2;
                this.panX = x1;
                this.panY = y1;
            }

            this.repaint();
        }

        public void panBy(int xUnits, int yUnits)
        {
            int size = Math.max(this.areaRect.width, this.areaRect.height) / 8;
            this.panX = this.panStartX - xUnits * size;
            this.panY = this.panStartY - yUnits * size;
            this.repaint();
        }

        public void toggleStats(boolean enabled)
        {
            this.shouldShowStats = enabled;
            this.repaint();
        }

        public int[] getDrawingBuffer()
        {
            return ((DataBufferInt)drawingImage.getRaster().getDataBuffer()).getData();
        }

        public void notifyImageProcessing(boolean shouldCopyFromSource)
        {
            this.waiting = true;
            this.currentParams = null;
            this.currentStats = null;
            this.statsStrings.size = 0;

            int[] outPixels = getDrawingBuffer();
            Canvas.blurArgbImage(
                outPixels,
                shouldCopyFromSource ? source.pixels : outPixels,
                imgWidth,
                imgHeight,
                rowScratch,
                blurWnd
            );

            loadingTimer.start();
            this.repaint();
        }

        public AnalyzerParameters onImageFinished(AnalyzerParameters params, Analyzer.Data data, Analyzer.Stats stats)
        {
            this.currentParams = params;
            this.currentStats = stats;
            this.statsStrings.size = 0;

            int[] outPixels = getDrawingBuffer();

            if (stats == null) {
                System.arraycopy(source.pixels, 0, outPixels, 0, imgWidth * imgHeight);
                this.waiting = false;
                this.repaint();
                return null;
            }

            float[] preprocessedImage = null;
            if (params.shouldRemapColors && params.shouldExpandOutputToGrayScale) {
                preprocessedImage = FloatBufferPool.acquireAsIs(imgWidth * imgHeight);
                PreprocessColor.transformToMonoFloatArray(
                    preprocessedImage,
                    source.pixels,
                    imgWidth,
                    imgHeight,
                    (float)params.hueTransformWeight,
                    (float)params.brightnessTransformWeight,
                    params.targetRemapColor.getRGB(),
                    params.voidRemapColor.getRGB(),
                    (float)params.saturationFactor,
                    luminanceTable
                );
            }

            Analyzer.drawOverlay(
                params,
                data.convexHull,
                data.skelResult,
                data.analysisImage.buf,
                outPixels,
                source.pixels,
                preprocessedImage,
                imgWidth,
                imgHeight,
                source.brightestChannel
            );

            if (preprocessedImage != null)
                FloatBufferPool.release(preprocessedImage);

            if (pendingParams != null) {
                notifyImageProcessing(false);

                AnalyzerParameters temp = pendingParams;
                pendingParams = null;
                return temp;
            }

            this.waiting = false;
            this.repaint();

            return null;
        }
    }

    public static final int MAX_WORKERS = 4;

    public static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
        /* corePoolSize */ MAX_WORKERS,
        /* maximumPoolSize */ MAX_WORKERS,
        /* keepAliveTime */ 30,
        /* unit */ TimeUnit.SECONDS,
        /* workQueue */ new LinkedBlockingQueue<>()
    );

    AngioToolGui2 parentFrame;
    File inputFile;
    String defaultPath;
    double prevImageScale;
    boolean everSaved;

    ImagingDisplay imageUi;

    JLabel labelZoom = new JLabel();
    JButton btnZoomIn = new JButton();
    JButton btnZoomOut = new JButton();
    JLabel labelPan = new JLabel();
    JButton btnPanLeft = new JButton();
    JButton btnPanUp = new JButton();
    JButton btnPanRight = new JButton();
    JButton btnPanDown = new JButton();
    JCheckBox cbShowStats = new JCheckBox();

    JLabel labelSaveImage = new JLabel();
    JLabel labelImageWasSaved = new JLabel();
    JButton btnSaveImage = new JButton();
    JTextField textSaveImage = new JTextField();

    JLabel labelSaveSpreadsheet = new JLabel();
    JLabel labelSpreadsheetWasSaved = new JLabel();
    JButton btnSaveSpreadsheet = new JButton();
    JTextField textSaveSpreadsheet = new JTextField();

    final Analyzer.Data analyzerData = new Analyzer.Data(null, 0);
    final ISliceRunner sliceRunner = new ISliceRunner.Parallel(Analyzer.threadPool);

    public ImagingWindow(AngioToolGui2 uiFrame, ArgbBuffer image, double imageScale, File sourceFile, String defaultPath)
    {
        super(sourceFile != null ? ("Analysis - " + sourceFile.getName()) : ("Analysis (Plugin)"));
        this.parentFrame = uiFrame;
        this.imageUi = new ImagingDisplay(image, true);
        this.prevImageScale = imageScale;
        this.inputFile = sourceFile;
        this.defaultPath = defaultPath;
        this.everSaved = false;

        this.labelZoom.setText("Zoom:");
        this.btnZoomIn.setIcon(AngioTool.ATPlus);
        this.btnZoomOut.setIcon(AngioTool.ATMinus);

        this.labelPan.setText("Move:");
        this.btnPanLeft.setIcon(AngioTool.ATLeft);
        this.btnPanUp.setIcon(AngioTool.ATUp);
        this.btnPanDown.setIcon(AngioTool.ATDown);
        this.btnPanRight.setIcon(AngioTool.ATRight);

        this.cbShowStats.setText("Display Stats");
        this.cbShowStats.setSelected(true);

        this.labelSaveImage.setText("Save result image");
        Misc.setNewFontStyleOn(this.labelImageWasSaved, Font.ITALIC);
        this.labelImageWasSaved.setHorizontalAlignment(SwingConstants.TRAILING);
        this.btnSaveImage.setIcon(AngioTool.ATFolderSmall);
        this.textSaveImage.addKeyListener(this);

        this.labelSaveSpreadsheet.setText("Save stats to spreadsheet");
        Misc.setNewFontStyleOn(this.labelSpreadsheetWasSaved, Font.ITALIC);
        this.labelSpreadsheetWasSaved.setHorizontalAlignment(SwingConstants.TRAILING);
        this.btnSaveSpreadsheet.setIcon(AngioTool.ATExcelSmall);
        this.textSaveSpreadsheet.addKeyListener(this);
    }

    public ImagingWindow showDialog()
    {
        imageUi.notifyImageProcessing(true);
        setUiInteractivity(false);
        dispatchAnalysisTask(parentFrame.buildAnalyzerParamsFromUi());

        JPanel dialogPanel = new JPanel();
        GroupLayout layout = new GroupLayout(dialogPanel);
        dialogPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        arrangeUi(layout);

        int nComponents = dialogPanel.getComponentCount();
        for (int i = 0; i < nComponents; i++) {
            Component elem = dialogPanel.getComponent(i);
            if (elem instanceof AbstractButton)
                ((AbstractButton)elem).addActionListener(this);
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                parentFrame.closeImagingWindow(ImagingWindow.this);
                if (parentFrame.pluginCtx != null)
                    Misc.sendWindowCloseEvent(parentFrame);
            }
        });

        this.getContentPane().add(dialogPanel);
        this.pack();

        Dimension preferredSize = this.getPreferredSize();
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

        this.setSize(new Dimension(
            Math.min(preferredSize.width, (int)(screenDim.width * 0.9)),
            Math.min(preferredSize.height, (int)(screenDim.height * 0.9))
        ));

        this.setLocation(700, 50);
        this.setVisible(true);

        return this;
    }

    public Raster getImageRaster()
    {
        return imageUi.drawingImage.getRaster();
    }

    public void release()
    {
        ImageFile.releaseImage(imageUi.source);
        imageUi.source = null;

        if (imageUi.drawingImage != null) {
            imageUi.drawingImage.flush();
            imageUi.drawingImage = null;
        }
    }

    public void updateImage(AnalyzerParameters params)
    {
        if (imageUi.waiting) {
            imageUi.pendingParams = params;
            return;
        }

        setUiInteractivity(false);

        double newResizeFactor = params.shouldResizeImage ? params.resizingFactor : 1.0;
        if (newResizeFactor == prevImageScale)
            imageUi.notifyImageProcessing(false);

        dispatchAnalysisTask(params);
    }

    private void arrangeUi(GroupLayout layout)
    {
        final int BS = 32;
        final int HS = 64;

        layout.setHorizontalGroup(layout.createParallelGroup()
            .addComponent(imageUi)
            .addGroup(layout.createSequentialGroup()
                .addComponent(labelZoom)
                .addComponent(btnZoomIn, BS, BS, BS)
                .addComponent(btnZoomOut, BS, BS, BS)
                .addComponent(labelPan)
                .addComponent(btnPanLeft, BS, BS, BS)
                .addComponent(btnPanUp, BS, BS, BS)
                .addComponent(btnPanDown, BS, BS, BS)
                .addComponent(btnPanRight, BS, BS, BS)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(cbShowStats)
            )
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelSaveImage)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(labelImageWasSaved, HS, HS, HS)
                    )
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSaveImage)
                        .addComponent(textSaveImage, 0, 0, Short.MAX_VALUE)
                    )
                )
                .addGap(20)
                .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelSaveSpreadsheet)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(labelSpreadsheetWasSaved, HS, HS, HS)
                    )
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSaveSpreadsheet)
                        .addComponent(textSaveSpreadsheet, 0, 0, Short.MAX_VALUE)
                    )
                )
            )
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(imageUi)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(labelZoom)
                .addComponent(btnZoomIn, BS, BS, BS)
                .addComponent(btnZoomOut, BS, BS, BS)
                .addComponent(labelPan)
                .addComponent(btnPanLeft, BS, BS, BS)
                .addComponent(btnPanUp, BS, BS, BS)
                .addComponent(btnPanDown, BS, BS, BS)
                .addComponent(btnPanRight, BS, BS, BS)
                .addComponent(cbShowStats)
            )
            .addGap(12)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelSaveImage)
                .addComponent(labelImageWasSaved)
                .addComponent(labelSaveSpreadsheet)
                .addComponent(labelSpreadsheetWasSaved)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(btnSaveImage, BS, BS, BS)
                .addComponent(textSaveImage, BS, BS, BS)
                .addComponent(btnSaveSpreadsheet, BS, BS, BS)
                .addComponent(textSaveSpreadsheet, BS, BS, BS)
            )
        );
    }

    void dispatchAnalysisTask(AnalyzerParameters params)
    {
        double newResizeFactor = params.shouldResizeImage ? params.resizingFactor : 1.0;
        boolean needsReload = newResizeFactor != prevImageScale;

        if (needsReload) {
            release();
            ArgbBuffer newImage = null;
            try {
                if (inputFile == null) {
                    ArgbBuffer temp = parentFrame.pluginCtx.makeArgbCopy();
                    newImage = ImageFile.acquireImageForAnalysisFromBuffer(temp, newResizeFactor);
                }
                else {
                    newImage = ImageFile.acquireImageForAnalysis(inputFile.getAbsolutePath(), newResizeFactor);
                }

                if (newImage == null)
                    throw new IOException("Failed to load image for analysis");

                imageUi.setNewImage(newImage);
            }
            catch (Exception ex) {
                if (newImage != null)
                    ImageFile.releaseImage(newImage);

                Misc.showExceptionInDialogBox(ex);
                setUiInteractivity(true);
                return;
            }
            imageUi.notifyImageProcessing(true);
        }

        prevImageScale = newResizeFactor;

        if (params.shouldRemapColors)
            PreprocessColor.computeBrightnessTable(
                imageUi.luminanceTable,
                params.brightnessLineSegments,
                params.brightnessLineSegments.length / 2
            );

        ImagingWindow.threadPool.submit(() -> {
            try {
                analyzerData.restart();
                if (params.shouldRemapColors)
                    analyzerData.updateBrightnessTable(params.brightnessLineSegments, params.brightnessLineSegments.length / 2);

                Analyzer.Stats stats = Analyzer.analyze(
                    analyzerData,
                    inputFile,
                    imageUi.source,
                    params,
                    sliceRunner
                );

                SwingUtilities.invokeLater(() -> {
                    AnalyzerParameters nextParams = imageUi.onImageFinished(params, analyzerData, stats);
                    if (nextParams != null) {
                        dispatchAnalysisTask(nextParams);
                    }
                    else {
                        analyzerData.nullify();
                        setUiInteractivity(true);
                    }
                });
            }
            catch (Throwable t) {
                final Throwable error = t;
                SwingUtilities.invokeLater(() -> {
                    imageUi.onImageFinished(params, analyzerData, null);
                    Misc.showExceptionInDialogBox(error);
                });
            }
        });
    }

    void setUiInteractivity(boolean enabled)
    {
        btnZoomIn.setEnabled(enabled);
        btnZoomOut.setEnabled(enabled);
        btnPanLeft.setEnabled(enabled);
        btnPanUp.setEnabled(enabled);
        btnPanRight.setEnabled(enabled);
        btnPanDown.setEnabled(enabled);
        cbShowStats.setEnabled(enabled);
        btnSaveImage.setEnabled(enabled);
        textSaveImage.setEnabled(enabled);
        btnSaveSpreadsheet.setEnabled(enabled);
        textSaveSpreadsheet.setEnabled(enabled);

        labelImageWasSaved.setText("");
        labelSpreadsheetWasSaved.setText("");
    }

    void saveResultImage()
    {
        String filePath = textSaveImage.getText();
        int extIdx = -1;
        while (true) {
            if (filePath == null || filePath.length() == 0) {
                JFileChooser fc = Misc.createFileChooser();
                fc.setDialogTitle("Save Result Image");
                fc.setDialogType(JFileChooser.SAVE_DIALOG);
                fc.setCurrentDirectory(new File(defaultPath));
                Misc.addImageFileFilters(fc);

                if (fc.showSaveDialog(this) != 0)
                    return;

                filePath = fc.getSelectedFile().getAbsolutePath();

                FileFilter filter = fc.getFileFilter();
                if (filter instanceof SimpleFileFilter)
                    filePath = ((SimpleFileFilter)filter).tailorFileName(filePath);
            }

            extIdx = filePath.lastIndexOf('.');
            if (extIdx <= 0 || extIdx < Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\')))
                Misc.showDialogBox("Save Result Image", "No image format was specified.\nPlease select an image format, eg. jpg");
            else
                break;

            filePath = null;
        };

        String format = filePath.substring(extIdx + 1).toLowerCase();

        try {
            DataBufferInt imageBuffer = (DataBufferInt)getImageRaster().getDataBuffer();
            ImageFile.saveImage(imageUi.drawingImage, imageBuffer, format, filePath);
            textSaveImage.setText(filePath);
            labelImageWasSaved.setText("saved");
        }
        catch (Throwable ex) {
            Misc.showExceptionInDialogBox(ex);
        }
    }

    void saveResultSpreadsheet()
    {
        if (imageUi.currentParams == null || imageUi.currentStats == null)
            return;

        String existingXlsxFile = textSaveSpreadsheet.getText();
        String[] outStrings = new String[2];

        ArrayList<XlsxReader.SheetCells> sheets = Misc.openSpreadsheetForAppending(
            outStrings,
            existingXlsxFile,
            defaultPath,
            this
        );
        if (sheets == null)
            return;

        defaultPath = outStrings[1];

        int fileNameOffset = Misc.getFileNameOffset(outStrings[0]);
        File folder = new File(outStrings[0].substring(0, fileNameOffset));
        String sheetName = outStrings[0].substring(fileNameOffset);

        try {
            SpreadsheetWriter sw;
            if (everSaved) {
                sw = new SpreadsheetWriter(folder, sheetName);
                sw.addSheets(sheets);
                sw.currentSheetIdx = Math.max(sw.currentSheetIdx - 1, 0);
            }
            else {
                sw = Analyzer.createWriterWithNewSheet(sheets, folder, sheetName);
            }

            Analyzer.writeResultToSheet(sw, imageUi.currentParams, imageUi.currentStats);
            everSaved = true;

            textSaveSpreadsheet.setText(outStrings[0]);
            labelSpreadsheetWasSaved.setText("saved");
        }
        catch (IOException ex) {
            Misc.showExceptionInDialogBox(ex);
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        Object source = evt.getSource();
        if (source == btnZoomIn)
            imageUi.zoomBy(2, null);
        else if (source == btnZoomOut)
            imageUi.zoomBy(-2, null);
        else if (source == btnPanLeft)
            imageUi.panBy(-1, 0);
        else if (source == btnPanUp)
            imageUi.panBy(0, -1);
        else if (source == btnPanDown)
            imageUi.panBy(0, 1);
        else if (source == btnPanRight)
            imageUi.panBy(1, 0);
        else if (source == cbShowStats)
            imageUi.toggleStats(cbShowStats.isSelected());
        else if (source == btnSaveImage)
            saveResultImage();
        else if (source == btnSaveSpreadsheet)
            saveResultSpreadsheet();
    }

    @Override public void keyPressed(KeyEvent evt) {}
    @Override public void keyReleased(KeyEvent evt) {}

    @Override
    public void keyTyped(KeyEvent evt)
    {
        Object source = evt.getSource();
        if (source == textSaveImage)
            labelImageWasSaved.setText("");
        else if (source == textSaveSpreadsheet)
            labelSpreadsheetWasSaved.setText("");
    }
}
