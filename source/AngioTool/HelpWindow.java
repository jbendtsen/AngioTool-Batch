package AngioTool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.awt.Window;
import java.awt.Dialog;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;

public class HelpWindow extends JDialog
{
    Window parentWindow;
    JEditorPane helpContent;
    JScrollPane scrollView;

    public HelpWindow(Window uiWindow, byte[] htmlData)
    {
        super(uiWindow, Dialog.ModalityType.APPLICATION_MODAL);
        this.setTitle("About - " + AngioTool.VERSION);
        this.parentWindow = uiWindow;

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
        this.setLocationRelativeTo(parentWindow);
        this.setVisible(true);
    }
}
