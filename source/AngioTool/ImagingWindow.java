package AngioTool;

import Pixels.ArgbBuffer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class ImagingWindow
{
    public static class ImagingDisplay extends JPanel
    {
        public ArgbBuffer source;
        public BufferedImage drawingImage;
        public Color backgroundColor;
        public Rectangle areaRect = null;

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
            areaRect = g.getClipBounds(areaRect);
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

    ImagingDisplay imageUi;

    JLabel labelSaveImage;
    JButton btnSaveImage;
    JTextField textSaveImage;

    JLabel labelSaveSpreadsheet;
    JButton btnSaveSpreadsheet;
    JTextField textSaveSpreadsheet;

    public ImagingWindow(AngioToolGui2 uiFrame, ArgbBuffer image)
    {
        this.parentFrame = uiFrame;
        this.imageUi = new ImagingDisplay(image);
    }

    public void showDialog()
    {
        
    }
}
