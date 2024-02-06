package GUI;

import Batch.ImageUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class ImagePreview extends JComponent implements PropertyChangeListener {
   ImageIcon thumbnail = null;
   File file = null;
   int thumbnailSize = 450;
   private JComponent parent;
   Dimension parentSize;

   public ImagePreview(JFileChooser fc) {
      this.setPreferredSize(new Dimension(500, 700));
      Border blackline = BorderFactory.createLineBorder(Color.black);
      TitledBorder title = BorderFactory.createTitledBorder(blackline, "Image Preview");
      title.setTitleJustification(2);
      this.setBorder(title);
      this.parent = fc;
      fc.addPropertyChangeListener(this);
   }

   public void loadImage() {
      if (this.file == null) {
         this.thumbnail = null;
      } else {
         ImageIcon tmpIcon = ImageUtils.openAsImageIcon(this.file.getPath());
         if (tmpIcon != null) {
            int tmpIconWidth = tmpIcon.getIconWidth();
            int tmpIconHeight = tmpIcon.getIconHeight();
            if (tmpIconWidth > tmpIconHeight) {
               this.thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(this.thumbnailSize, tmpIconHeight * this.thumbnailSize / tmpIconWidth, 1));
            }

            if (tmpIconHeight > tmpIconWidth) {
               this.thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(tmpIconWidth * this.thumbnailSize / tmpIconHeight, this.thumbnailSize, 1));
            } else {
               this.thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(this.thumbnailSize, this.thumbnailSize, 1));
            }
         }
      }
   }

   @Override
   public void propertyChange(PropertyChangeEvent e) {
      boolean update = false;
      String prop = e.getPropertyName();
      if ("directoryChanged".equals(prop)) {
         this.file = null;
         update = true;
      } else if ("SelectedFileChangedProperty".equals(prop)) {
         this.file = (File)e.getNewValue();
         if (this.file == null) {
            update = false;
         } else if (this.file.isFile()) {
            update = true;
         } else {
            update = false;
         }
      }

      if (update) {
         this.thumbnail = null;
         if (this.isShowing()) {
            this.loadImage();
            this.repaint();
         }
      }
   }

   @Override
   protected void paintComponent(Graphics g) {
      if (this.thumbnail == null) {
         this.loadImage();
      }

      if (this.thumbnail != null) {
         int x = this.getWidth() / 2 - this.thumbnail.getIconWidth() / 2;
         int y = this.getHeight() / 2 - this.thumbnail.getIconHeight() / 2;
         if (y < 0) {
            y = 2;
         }

         if (x < 5) {
            x = 5;
         }

         this.thumbnail.paintIcon(this, g, x, y);
      }
   }
}
