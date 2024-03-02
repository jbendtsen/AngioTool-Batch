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
            int beforeX = 0, beforeY = 0;
            int afterX = 0, afterY = 0;
            int blankW = 0, blankH = 0;
            int imgX = 0, imgY = 0;
            int imgW = 0, imgH = 0;

            g.drawImage(drawingImage, imgX, imgY, imgW, imgH, backgroundColor, null);

            g.setColor(backgroundColor);
            g.fillRect(beforeX, beforeY, blankW, blankH);
            g.fillRect(afterX, afterY, blankW, blankH);
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
        this.labelSaveSpreadsheet.setText("Save spreadsheet");
        this.btnSaveSpreadsheet.addActionListener(this);
        this.btnSaveSpreadsheet.setIcon(AngioTool.ATExcelSmall);
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
