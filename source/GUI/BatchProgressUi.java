package GUI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatchProgressUi
{
    JFrame parentFrame;
    JDialog jdialog;
    JPanel progressPanel;
    JPanel overallPanel;
    JPanel imagePanel;
    JLabel overallLabel;
    JProgressBar overallProgress;
    JLabel imageLabel;
    JProgressBar imageProgress;
    JButton cancelBtn;
    int nErrors = 0;

    public AtomicBoolean isClosed;

    public BatchProgressUi(JFrame uiFrame)
    {
        this.isClosed = new AtomicBoolean(false);
        this.parentFrame = uiFrame;
        this.overallPanel = new JPanel(makeBorderLayout(2));
        this.overallLabel = new JLabel();
        this.overallProgress = new JProgressBar();
        this.imagePanel = new JPanel(makeBorderLayout(2));
        this.imageLabel = new JLabel();
        this.imageProgress = new JProgressBar();
        this.progressPanel = new JPanel(makeBorderLayout(5));
        this.cancelBtn = new JButton();
        this.jdialog = new JDialog(parentFrame, "Analyzing Images", true);
    }

    static BorderLayout makeBorderLayout(int vGap) {
        BorderLayout layout = new BorderLayout();
        layout.setVgap(vGap);
        return layout;
    }

    public void build()
    {
        overallLabel.setText("Enumerating input images...");

        overallProgress.setValue(0);
        overallProgress.setStringPainted(true);

        overallPanel.add(BorderLayout.NORTH, overallLabel);
        overallPanel.add(BorderLayout.SOUTH, overallProgress);

        imageLabel.setText(" ");

        imageProgress.setValue(0);
        imageProgress.setStringPainted(true);

        imagePanel.add(BorderLayout.NORTH, imageLabel);
        imagePanel.add(BorderLayout.SOUTH, imageProgress);

        progressPanel.add(BorderLayout.NORTH, overallPanel);
        progressPanel.add(BorderLayout.SOUTH, imagePanel);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener((ActionEvent e) -> { BatchProgressUi.this.close(); });

        JPanel cancelPanel = new JPanel(new BorderLayout());
        cancelPanel.add(BorderLayout.NORTH, Box.createVerticalStrut(20));
        cancelPanel.add(BorderLayout.EAST, cancelBtn);

        jdialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        jdialog.add(BorderLayout.NORTH, progressPanel);
        jdialog.add(BorderLayout.SOUTH, cancelPanel);
        //jdialog.pack();

        jdialog.setSize(400, 180);
        jdialog.setLocationRelativeTo(parentFrame);
        jdialog.setVisible(true);
    }

    public void notifyNoImages()
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            overallLabel.setText("No images were found!");
            cancelBtn.setText("OK");
        });
    }

    public void startProgressBars(int nImages, int maxProgressPerImage)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            overallLabel.setText("Analyzing images...");
            overallProgress.setValue(0);
            overallProgress.setMaximum(nImages);

            imageProgress.setValue(0);
            imageProgress.setMaximum(maxProgressPerImage);
        });
    }

    public void onStartImage(String absPath)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            int pathLen = absPath.length();
            String partialFileName = pathLen > 60 ?
                absPath.substring(0, 29) + "..." + absPath.substring(pathLen - 29) :
                absPath;

            int current = overallProgress.getValue() + 1;
            String status = "" + current + "/" + overallProgress.getMaximum();
            if (nErrors > 0)
                status += ", " + nErrors + (nErrors == 1 ? " error." : " errors.");

            status += " Processing " + partialFileName + "...";
            overallLabel.setText(status);
        });
    }

    public void updateImageProgress(int newProgress, String statusMsg)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            imageLabel.setText(statusMsg);
            imageProgress.setValue(newProgress);
        });
    }

    public void onImageDone(Throwable error)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            if (error != null)
                nErrors++;

            overallProgress.setValue(overallProgress.getValue() + 1);
        });
    }

    public void onFinished(String sheetFileName)
    {
        SwingUtilities.invokeLater(() -> {
            if (isClosed.get())
                return;

            int nImages = overallProgress.getMaximum();
            String status = "" + nImages + "/" + nImages;
            if (nErrors > 0) {
                status += " Finished with " + nErrors + " error";
                if (nErrors != 1)
                    status += "s";
            }
            else {
                status += " Done!";
            }

            overallLabel.setText(status);
            imageLabel.setText("Saved Excel results to " + sheetFileName);
            imageProgress.setValue(imageProgress.getMaximum());
            cancelBtn.setText("OK");
        });
    }

    public void close()
    {
        SwingUtilities.invokeLater(() -> {
            if (!isClosed.getAndSet(true))
                jdialog.dispose();
        });
    }
}
