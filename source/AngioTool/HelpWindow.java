package AngioTool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;

public class HelpWindow extends JDialog
{
    JFrame parentFrame;
    JEditorPane helpContent;
    JScrollPane scrollView;

    public HelpWindow(JFrame uiFrame, byte[] htmlData)
    {
        super(uiFrame, true);
        this.setTitle("About");
        this.parentFrame = uiFrame;

        this.helpContent = new JEditorPane();
        helpContent.setEditorKit(new HTMLEditorKit());

        try { helpContent.read(new ByteArrayInputStream(htmlData), ""); }
        catch (IOException ignored) {}

        helpContent.setEditable(false);

        this.scrollView = new JScrollPane(helpContent);
    }

    public void showDialog()
    {
        this.getContentPane().add(scrollView);
        this.pack();
        this.setSize(700, 700);
        this.setLocationRelativeTo(parentFrame);
        this.setVisible(true);
    }
}
