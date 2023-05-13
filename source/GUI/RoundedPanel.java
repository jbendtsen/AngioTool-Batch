package GUI;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

public class RoundedPanel extends JPanel {
   private int radius;

   public RoundedPanel() {
   }

   public RoundedPanel(int cornerRadius) {
      this.radius = cornerRadius;
   }

   public void setCornerRadius(int cornerRadius) {
      this.radius = cornerRadius;
   }

   @Override
   public void paintComponent(Graphics g) {
      Color bg = this.getBackground();
      g.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue()));
      g.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, this.radius, this.radius);
      g.setColor(new Color(0, 0, 0, 70));
      g.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, this.radius, this.radius);
   }
}
