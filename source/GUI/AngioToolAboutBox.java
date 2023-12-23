package GUI;

import AngioTool.AngioTool;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

public class AngioToolAboutBox extends JDialog implements MouseListener {
   Point location;
   MouseEvent pressed;
   String filename = "";
   InputStream is = this.getClass().getClassLoader().getResourceAsStream("doc/AngioTool.html");
   private JPanel aboutBoxPanel;
   private JButton closeAboutBox;
   private JEditorPane helpEditorPane;
   private JScrollPane jScrollPane1;

   public AngioToolAboutBox(Frame parent, boolean modal) {
      super(parent, modal);
      MyHTMLEditorKit kit = new MyHTMLEditorKit();
      kit.setJar(this.getClass());
      this.initComponents();
      Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension AboutBoxDimension = this.getSize();
      this.setLocation(screenDimension.width / 2 - AboutBoxDimension.width / 2, screenDimension.height / 2 - AboutBoxDimension.height / 2);
      this.helpEditorPane.setEditorKit(kit);
      this.helpEditorPane.addHyperlinkListener(new AngioToolAboutBox.AboutBoxHyperlinkListener());
      this.addMouseListener(this);
      FileReader reader = null;

      try {
         this.helpEditorPane.read(this.is, this.filename);
      } catch (IOException var8) {
         Logger.getLogger(AngioToolAboutBox.class.getName()).log(Level.SEVERE, null, var8);
      }
   }

   public void closeAboutBox() {
      AngioToolGUI.ATAboutBoxLoc = this.getLocation();
      this.dispose();
   }

   private void initComponents() {
      this.aboutBoxPanel = new JPanel();
      JLabel appTitleLabel = new JLabel();
      JLabel appDescLabel = new JLabel();
      JLabel versionLabel = new JLabel();
      JLabel homepageLabel = new JLabel();
      JLabel imageLabel = new JLabel();
      JLabel homepageLabel1 = new JLabel();
      JLabel homepageLabel2 = new JLabel();
      JLabel homepageLabel3 = new JLabel();
      JLabel homepageLabel4 = new JLabel();
      JLabel homepageLabel5 = new JLabel();
      JLabel homepageLabel6 = new JLabel();
      this.jScrollPane1 = new JScrollPane();
      this.helpEditorPane = new JEditorPane();
      this.closeAboutBox = new JButton();
      this.setDefaultCloseOperation(2);
      this.setMinimumSize(new Dimension(620, 180));
      this.setResizable(false);
      this.setUndecorated(true);
      this.getContentPane().setLayout(new AbsoluteLayout());
      this.aboutBoxPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 2));
      this.aboutBoxPanel.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent evt) {
            AngioToolAboutBox.this.aboutBoxPanelMousePressed(evt);
         }
      });
      this.aboutBoxPanel.addMouseMotionListener(new MouseMotionAdapter() {
         @Override
         public void mouseDragged(MouseEvent evt) {
            AngioToolAboutBox.this.aboutBoxPanelMouseDragged(evt);
         }
      });
      this.aboutBoxPanel.setLayout(new AbsoluteLayout());
      appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | 1, (float)(appTitleLabel.getFont().getSize() + 19)));
      appTitleLabel.setHorizontalAlignment(0);
      appTitleLabel.setText(AngioTool.VERSION);
      this.aboutBoxPanel.add(appTitleLabel, new AbsoluteConstraints(310, 20, -1, -1));
      this.aboutBoxPanel.add(appDescLabel, new AbsoluteConstraints(160, 40, 270, -1));
      versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | 1, (float)(versionLabel.getFont().getSize() + 3)));
      versionLabel.setHorizontalAlignment(0);
      versionLabel.setText("Version 0.6a Batch Edition (14.05.23)");
      this.aboutBoxPanel.add(versionLabel, new AbsoluteConstraints(260, 70, -1, -1));
      homepageLabel.setFont(homepageLabel.getFont().deriveFont(homepageLabel.getFont().getStyle() | 1, (float)(homepageLabel.getFont().getSize() + 3)));
      homepageLabel.setText("Laure Gambardella");
      this.aboutBoxPanel.add(homepageLabel, new AbsoluteConstraints(320, 190, -1, -1));
      imageLabel.setIcon(new ImageIcon(this.getClass().getResource("/images/ATIcon20 128x128.gif")));
      this.aboutBoxPanel.add(imageLabel, new AbsoluteConstraints(10, 10, -1, 130));
      homepageLabel1.setFont(homepageLabel1.getFont().deriveFont(homepageLabel1.getFont().getStyle() | 1, (float)(homepageLabel1.getFont().getSize() + 3)));
      homepageLabel1.setText("Contributors:");
      this.aboutBoxPanel.add(homepageLabel1, new AbsoluteConstraints(280, 110, -1, -1));
      homepageLabel2.setFont(homepageLabel2.getFont().deriveFont(homepageLabel2.getFont().getStyle() | 1, (float)(homepageLabel2.getFont().getSize() + 3)));
      homepageLabel2.setText("Enrique Zudaire");
      this.aboutBoxPanel.add(homepageLabel2, new AbsoluteConstraints(320, 130, -1, -1));
      homepageLabel3.setFont(homepageLabel3.getFont().deriveFont(homepageLabel3.getFont().getStyle() | 1, (float)(homepageLabel3.getFont().getSize() + 3)));
      homepageLabel3.setText("Chris Kurcz");
      this.aboutBoxPanel.add(homepageLabel3, new AbsoluteConstraints(320, 150, -1, -1));
      homepageLabel4.setFont(homepageLabel4.getFont().deriveFont(homepageLabel4.getFont().getStyle() | 1, (float)(homepageLabel4.getFont().getSize() + 3)));
      homepageLabel4.setText("Sonja Vermeren");
      this.aboutBoxPanel.add(homepageLabel4, new AbsoluteConstraints(320, 170, -1, -1));
      homepageLabel5.setFont(homepageLabel5.getFont().deriveFont(homepageLabel5.getFont().getStyle() | 1, (float)(homepageLabel5.getFont().getSize() + 3)));
      homepageLabel5.setText("Batch analysis feature:");
      this.aboutBoxPanel.add(homepageLabel5, new AbsoluteConstraints(280, 210, -1, -1));
      homepageLabel6.setFont(homepageLabel6.getFont().deriveFont(homepageLabel6.getFont().getStyle() | 1, (float)(homepageLabel6.getFont().getSize() + 3)));
      homepageLabel6.setText("Jack Bendtsen");
      this.aboutBoxPanel.add(homepageLabel6, new AbsoluteConstraints(320, 230, -1, -1));
      this.jScrollPane1.setBorder(BorderFactory.createBevelBorder(1));
      this.helpEditorPane.setEditable(false);
      this.helpEditorPane.setMargin(new Insets(4, 4, 4, 4));
      this.jScrollPane1.setViewportView(this.helpEditorPane);
      this.aboutBoxPanel.add(this.jScrollPane1, new AbsoluteConstraints(10, 270, 760, 430));
      this.getContentPane().add(this.aboutBoxPanel, new AbsoluteConstraints(0, 0, 785, 710));
      this.closeAboutBox.setFont(new Font("Tahoma", 1, 18));
      this.closeAboutBox.setText("Close");
      this.closeAboutBox.setOpaque(false);
      this.closeAboutBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            AngioToolAboutBox.this.closeAboutBoxActionPerformed(evt);
         }
      });
      this.getContentPane().add(this.closeAboutBox, new AbsoluteConstraints(3, 715, 780, 30));
      this.pack();
   }

   private void aboutBoxPanelMouseDragged(MouseEvent evt) {
      this.location = this.getLocation(this.location);
      int x = this.location.x - this.pressed.getX() + evt.getX();
      int y = this.location.y - this.pressed.getY() + evt.getY();
      this.setLocation(x, y);
   }

   private void aboutBoxPanelMousePressed(MouseEvent evt) {
      this.pressed = evt;
   }

   private void closeAboutBoxActionPerformed(ActionEvent evt) {
      this.closeAboutBox();
   }

   public static void main(String[] args) {
      EventQueue.invokeLater(new Runnable() {
         @Override
         public void run() {
            AngioToolAboutBox dialog = new AngioToolAboutBox(new JFrame(), true);
            dialog.addWindowListener(new WindowAdapter() {
               @Override
               public void windowClosing(WindowEvent e) {
                  System.exit(0);
               }
            });
            dialog.setVisible(true);
         }
      });
   }

   @Override
   public void mouseClicked(MouseEvent e) {
      this.closeAboutBox();
   }

   @Override
   public void mousePressed(MouseEvent e) {
   }

   @Override
   public void mouseReleased(MouseEvent e) {
   }

   @Override
   public void mouseEntered(MouseEvent e) {
   }

   @Override
   public void mouseExited(MouseEvent e) {
   }

   class AboutBoxHyperlinkListener implements HyperlinkListener {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent evt) {
         if (evt.getEventType() == EventType.ACTIVATED) {
            try {
               try {
                  Desktop.getDesktop().browse(evt.getURL().toURI());
               } catch (URISyntaxException var3) {
                  Logger.getLogger(AngioToolAboutBox.class.getName()).log(Level.SEVERE, null, var3);
               }
            } catch (IOException var4) {
            }
         }
      }
   }
}
