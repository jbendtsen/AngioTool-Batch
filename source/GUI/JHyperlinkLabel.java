package GUI;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

public class JHyperlinkLabel extends JLabel {
   private Color underlineColor = null;

   public JHyperlinkLabel(String label) {
      super(label);
      this.setForeground(Color.BLUE.darker());
      this.setCursor(new Cursor(12));
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.setColor(this.underlineColor == null ? this.getForeground() : this.underlineColor);
      Insets insets = this.getInsets();
      int left = insets.left;
      if (this.getIcon() != null) {
         left += this.getIcon().getIconWidth() + this.getIconTextGap();
      }

      g.drawLine(left, this.getHeight() - 1 - insets.bottom, (int)this.getPreferredSize().getWidth() - insets.right, this.getHeight() - 1 - insets.bottom);
   }

   private static void open(URI uri) throws URISyntaxException {
      if (Desktop.isDesktopSupported()) {
         Desktop desktop = Desktop.getDesktop();

         try {
            desktop.browse(uri);
         } catch (IOException var3) {
         }
      }
   }

   public Color getUnderlineColor() {
      return this.underlineColor;
   }

   public void setUnderlineColor(Color underlineColor) {
      this.underlineColor = underlineColor;
   }

   public class HyperlinkLabelMouseAdapter extends MouseAdapter {
      @Override
      public void mouseClicked(MouseEvent e) {
         System.out.println(JHyperlinkLabel.this.getText());

         try {
            JHyperlinkLabel.open(new URI("http://java.sun.com"));
         } catch (URISyntaxException var3) {
            Logger.getLogger(JHyperlinkLabel.class.getName()).log(Level.SEVERE, null, var3);
         }
      }
   }
}
