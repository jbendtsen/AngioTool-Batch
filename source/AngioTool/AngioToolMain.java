package AngioTool;

import java.awt.EventQueue;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import Utils.Utils;

public class AngioToolMain {
   /*
   private static final int yearExpiration = 3014;
   private static final int monthExpiration = 12;
   private static final int dayExpiration = 31;
   private static Date limitDate = null;
   private static final Date today = new Date();
   */

   public static void main(String[] args) {
      /*
      try {
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
         limitDate = formatter.parse("3014/12/31");
      } catch (ParseException var2) {
         Logger.getLogger(AngioToolMain.class.getName()).log(Level.SEVERE, null, var2);
      }

      if (today.compareTo(limitDate) > 0) {
         JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Your application license has expired!", "Error", 0);
         System.exit(0);
      }

      if (!ApplicationInstanceManager.registerInstance()) {
         JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Another instance of this application is already running.  Exiting...", "Error", 0);
         System.exit(0);
      }
      */
      
      try {
         Class.forName("Batch.Lee94");
      }
      catch (Throwable t) {
         t.printStackTrace();
         Utils.showExceptionInDialogBox(t.getCause());
         return;
      }

      EventQueue.invokeLater(new Runnable() {
         @Override
         public void run() {
            new AngioTool();
         }
      });
   }
}
