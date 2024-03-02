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
import java.io.File;
import javax.swing.*;

public class ImagingWindow extends JFrame implements ActionListener
{
    public static class ImagingDisplay extends JPanel
    {
        public ArgbBuffer source;
        public BufferedImage drawingImage;
        public Color backgroundColor;
        public Rectangle areaRect = new Rectangle();

        public ImagingDisplay(ArgbBuffer source)
        {
            this.source = source;
            this.backgroundColor = new Color(0);
        }

        @Override
        public Dimension getPreferredSize()
        {
            return new Dimension(source.width, source.height);
        }

        @Override
        public void paintComponent(Graphics g)
        {
            // determine whether the empty space should be above and below or left and right of the image
            g.getClipBounds(areaRect);

            if (areaRect.width <= 0 || areaRect.height <= 0) {
                super.paintComponent(g);
                return;
            }

            double wRatio = (double)source.width / (double)areaRect.width;
            double hRatio = (double)source.height / (double)areaRect.height;

            if (wRatio > hRatio && wRatio > 0.0) {
                int imgH = Math.min((int)(source.height / wRatio + 0.5), areaRect.height);
                int dH = areaRect.height - imgH;
                int imgY = (dH / 2) + (dH % 2);

                g.drawImage(drawingImage, 0, imgY, areaRect.width, imgH, backgroundColor, null);

                g.setColor(backgroundColor);
                g.fillRect(0, 0, areaRect.width, imgY);
                g.fillRect(0, imgY + imgH, areaRect.width, dH / 2);
            }
            else if (wRatio < hRatio && hRatio > 0.0) {
                int imgW = Math.min((int)(source.width / hRatio + 0.5), areaRect.width);
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

        public void updateSurface()
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
}
