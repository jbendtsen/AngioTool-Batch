package AngioTool;

import GUI.TransparentTextField;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.Timer;

public class MemoryMonitor extends Timer implements ActionListener {
   private TransparentTextField textField;
   private Color OK;
   private Color NO_OK;

   public MemoryMonitor(int interval, TransparentTextField textField) {
      super(interval, null);
      this.textField = textField;
      this.textField.setOpaque(true);
      this.textField.setAlpha(1.0F);
      this.OK = new Color(0.0F, 1.0F, 0.0F);
      this.NO_OK = new Color(1.0F, 0.0F, 0.0F);
      this.addActionListener(this);
   }

   @Override
   public void actionPerformed(ActionEvent event) {
      double m = (double)Runtime.getRuntime().maxMemory() / 1048576.0;
      double f = (double)Runtime.getRuntime().freeMemory() / 1048576.0;
      double t = (double)Runtime.getRuntime().totalMemory() / 1048576.0;
      double u = t - f;
      NumberFormat formatter = new DecimalFormat("#.##");
      String free = formatter.format(f);
      String total = formatter.format(t);
      String maximum = formatter.format(m);
      String used = formatter.format(u);
      this.textField.setText("");
      if (u * 100.0 / m <= 90.0) {
         this.textField.setBackground(this.OK);
      } else {
         this.textField.setBackground(this.NO_OK);
      }

      this.textField.setText(used + "/" + maximum);
   }
}
