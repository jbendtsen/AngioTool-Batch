package AngioTool;

import Pixels.ArgbBuffer;
import Pixels.Canvas;
import Utils.ISliceRunner;
import Xlsx.SpreadsheetWriter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.*;

public class ImagingWindow extends JFrame implements ActionListener
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
        int imgWidth;
        int imgHeight;

        BufferedImage drawingImage;
        Color backgroundColor;
        Color[] wheelColors;
        int loadTicks;
        Rectangle areaRect;
        Analyzer.Stats currentStats;

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

        public ImagingDisplay(ArgbBuffer source)
        {
            this.source = source;
            this.imgWidth = source.width;
            this.imgHeight = source.height;
            this.rowScratch = new int[Math.max(imgWidth, imgHeight)];
            this.blurWnd = new float[25 * 3];
            this.drawingImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
            this.backgroundColor = new Color(0);
            this.wheelColors = new Color[8];
            this.loadTicks = 0;
            this.areaRect = new Rectangle();
            this.currentStats = null;

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

            if (!waiting)
                return;

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

        @Override
        public void mouseDragged(MouseEvent evt)
        {
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
            int clicks = evt.getWheelRotation();
            if (clicks != 0) {
                int prevZoom = this.zoomLevels;
                this.zoomLevels = Math.min(Math.max(this.zoomLevels - clicks, -16), 32);

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
                this.repaint();
            }
        }

        public int[] getDrawingBuffer()
        {
            return ((DataBufferInt)drawingImage.getRaster().getDataBuffer()).getData();
        }

        public void notifyImageProcessing(boolean shouldCopyFromSource)
        {
            this.waiting = true;
            this.currentStats = null;

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

        public AnalyzerParameters onImageFinished(AnalyzerParameters params, Analyzer.Scratch data, Analyzer.Stats stats)
        {
            this.currentStats = stats;

            int[] outPixels = getDrawingBuffer();
            Analyzer.drawOverlay(
                params,
                data.convexHull,
                data.skelResult,
                data.analysisImage.buf,
                outPixels,
                source.pixels,
                imgWidth,
                imgHeight,
                source.brightestChannel
            );

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

    ImagingDisplay imageUi;

    JLabel labelSaveImage = new JLabel();
    JButton btnSaveImage = new JButton();
    JTextField textSaveImage = new JTextField();

    JLabel labelSaveSpreadsheet = new JLabel();
    JButton btnSaveSpreadsheet = new JButton();
    JTextField textSaveSpreadsheet = new JTextField();

    final Analyzer.Scratch analyzerScratch = new Analyzer.Scratch();
    final ISliceRunner sliceRunner = new ISliceRunner.Parallel(Analyzer.threadPool);

    public ImagingWindow(AngioToolGui2 uiFrame, ArgbBuffer image, File sourceFile)
    {
        super("Analysis - " + sourceFile.getName());
        this.parentFrame = uiFrame;
        this.imageUi = new ImagingDisplay(image);
        this.inputFile = sourceFile;

        this.labelSaveImage.setText("Save result image");

        this.btnSaveImage.addActionListener(this);
        this.btnSaveImage.setIcon(AngioTool.ATFolderSmall);
        this.btnSaveImage.setEnabled(false);

        this.textSaveImage.setEnabled(false);

        this.labelSaveSpreadsheet.setText("Save stats to spreadsheet");

        this.btnSaveSpreadsheet.addActionListener(this);
        this.btnSaveSpreadsheet.setIcon(AngioTool.ATExcelSmall);
        this.btnSaveSpreadsheet.setEnabled(false);

        this.textSaveSpreadsheet.setEnabled(false);
    }

    public ImagingWindow showDialog()
    {
        imageUi.notifyImageProcessing(true);
        dispatchAnalysisTask(parentFrame.buildAnalyzerParamsFromUi());

        JPanel dialogPanel = new JPanel();
        GroupLayout layout = new GroupLayout(dialogPanel);
        dialogPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        arrangeUi(layout);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                parentFrame.imagingWindows.remove(ImagingWindow.this);
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

    public void updateImage(AnalyzerParameters params)
    {
        if (imageUi.waiting) {
            imageUi.pendingParams = params;
            return;
        }

        imageUi.notifyImageProcessing(false);
        dispatchAnalysisTask(params);
    }

    private void arrangeUi(GroupLayout layout)
    {
        layout.setHorizontalGroup(layout.createParallelGroup()
            .addComponent(imageUi)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(labelSaveImage)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSaveImage)
                        .addComponent(textSaveImage)
                    )
                )
                .addGap(20)
                .addGroup(layout.createParallelGroup()
                    .addComponent(labelSaveSpreadsheet)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSaveSpreadsheet)
                        .addComponent(textSaveSpreadsheet)
                    )
                )
            )
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(imageUi)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelSaveImage)
                .addComponent(labelSaveSpreadsheet)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(btnSaveImage)
                .addComponent(textSaveImage)
                .addComponent(btnSaveSpreadsheet)
                .addComponent(textSaveSpreadsheet)
            )
        );
    }

    void dispatchAnalysisTask(AnalyzerParameters params)
    {
        ImagingWindow.threadPool.submit(() -> {
            Analyzer.Stats stats = Analyzer.analyze(
                analyzerScratch,
                inputFile,
                imageUi.source,
                params,
                sliceRunner,
                null
            );
            SwingUtilities.invokeLater(() -> {
                AnalyzerParameters nextParams = imageUi.onImageFinished(params, analyzerScratch, stats);
                if (nextParams != null)
                    dispatchAnalysisTask(nextParams);
            });
        });
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        
    }
}
