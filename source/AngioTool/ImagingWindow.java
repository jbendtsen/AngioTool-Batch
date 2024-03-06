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
    public static class ImagingDisplay extends JPanel
    {
        boolean waiting = false;
        AnalyzerParameters pendingParams = null;

        ArgbBuffer source;
        int[] rowScratch;
        float[] blurWnd;
        int imgWidth;
        int imgHeight;

        BufferedImage drawingImage;
        Color backgroundColor;
        Rectangle areaRect;
        Analyzer.Stats currentStats;

        public ImagingDisplay(ArgbBuffer source)
        {
            this.source = source;
            this.imgWidth = source.width;
            this.imgHeight = source.height;
            this.rowScratch = new int[Math.max(imgWidth, imgHeight)];
            this.blurWnd = new float[25 * 3];
            this.drawingImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
            this.backgroundColor = new Color(0);
            this.areaRect = new Rectangle();
            this.currentStats = null;
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

            if (areaRect.width <= 0 || areaRect.height <= 0) {
                super.paintComponent(g);
                return;
            }

            double wRatio = (double)imgWidth / (double)areaRect.width;
            double hRatio = (double)imgHeight / (double)areaRect.height;

            if (wRatio > hRatio && wRatio > 0.0) {
                int imgH = Math.min((int)(imgHeight / wRatio + 0.5), areaRect.height);
                int dH = areaRect.height - imgH;
                int imgY = (dH / 2) + (dH % 2);

                g.drawImage(drawingImage, 0, imgY, areaRect.width, imgH, backgroundColor, null);

                g.setColor(backgroundColor);
                g.fillRect(0, 0, areaRect.width, imgY);
                g.fillRect(0, imgY + imgH, areaRect.width, dH / 2);
            }
            else if (wRatio < hRatio && hRatio > 0.0) {
                int imgW = Math.min((int)(imgWidth / hRatio + 0.5), areaRect.width);
                int dW = areaRect.width - imgW;
                int imgX = (dW / 2) + (dW % 2);

                g.drawImage(drawingImage, imgX, 0, imgW, areaRect.height, backgroundColor, null);

                g.setColor(backgroundColor);
                g.fillRect(0, 0, imgX, areaRect.height);
                g.fillRect(imgX + imgW, 0, dW / 2, areaRect.height);
            }
            else {
                g.drawImage(drawingImage, 0, 0, areaRect.width, areaRect.height, backgroundColor, null);
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
