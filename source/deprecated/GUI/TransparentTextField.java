package GUI;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JTextField;

public class TransparentTextField extends JTextField {
   float alpha;

   public TransparentTextField() {
      this.setOpaque(false);
   }

   public TransparentTextField(String str) {
      super(str);
      this.setOpaque(false);
   }

   public void setAlpha(float alpha) {
      this.alpha = alpha;
      this.repaint();
   }

   @Override
   public void paint(Graphics g) {
      Graphics2D g2 = (Graphics2D)g.create();
      g2.setComposite(AlphaComposite.getInstance(3, this.alpha));
      super.paint(g2);
      g2.dispose();
   }

   @Override
   protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D)g.create();
      g2.setComposite(AlphaComposite.getInstance(3, this.alpha));
      super.paintComponent(g2);
   }
}
