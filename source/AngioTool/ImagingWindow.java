package AngioTool;

import Pixels.ArgbBuffer;
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
import javax.swing.*;

public class ImagingWindow extends JFrame implements ActionListener
{
    public static class ImagingDisplay extends JPanel
    {
        public boolean waiting_;
        public ArgbBuffer source_;
        int imgWidth;
        int imgHeight;

        public BufferedImage drawingImage;
        public Color backgroundColor;
        public Rectangle areaRect = new Rectangle();

        private float[] kernel = build5x5Kernel();

        static float[] build5x5Kernel()
        {
            float[] k = new float[25];
            final float s = 1.0f / 256.0f;
            k[0] = k[4] = k[24] = k[20]  = s *  1;
            k[1] = k[9] = k[23] = k[15]  = s *  4;
            k[2] = k[14] = k[22] = k[10] = s *  6;
            k[5] = k[3] = k[19] = k[21]  = s *  4;
            k[6] = k[8] = k[18] = k[16]  = s *  9;
            k[7] = k[13] = k[17] = k[11] = s * 20;
            k[12] = s * 80;
            return k;
        }

        public ImagingDisplay(ArgbBuffer source)
        {
            this.source_ = source;
            this.imgWidth = source.width;
            this.imgHeight = source.height;
            this.drawingImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
            this.backgroundColor = new Color(0);
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

        public void onImageWaiting()
        {
            int[] outPixels = ((DataBufferInt)drawingImage.getRaster().getDataBuffer()).getData();
            float[] blurWnd = new float[15 * 3];
            int[] coords = new int[2];

            synchronized (source_) {

            }

            try {
                javax.imageio.ImageIO.write(drawingImage, "png", new File("test.png"));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void onImageFinished()
        {
            
        }
    }

    AngioToolGui2 parentFrame;
    File inputFile;

    ImagingDisplay imageUi;

    JLabel labelSaveImage = new JLabel();
    JButton btnSaveImage = new JButton();
    JTextField textSaveImage = new JTextField();

    JLabel labelSaveSpreadsheet = new JLabel();
    JButton btnSaveSpreadsheet = new JButton();
    JTextField textSaveSpreadsheet = new JTextField();

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

    public void showDialog()
    {
        imageUi.onImageWaiting();
        dispatchAnalysisTask();

        JPanel dialogPanel = new JPanel();
        GroupLayout layout = new GroupLayout(dialogPanel);
        dialogPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        arrangeUi(layout);

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

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        
    }

    void dispatchAnalysisTask()
    {
    
    }
}
