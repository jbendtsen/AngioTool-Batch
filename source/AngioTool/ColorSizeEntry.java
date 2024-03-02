package AngioTool;

import Pixels.Rgb;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;

public class ColorSizeEntry extends NumberEntry
{
    public RoundedPanel panel;
    public Rgb color;

    public ColorSizeEntry(String name, boolean enabled, double value, Rgb color)
    {
        super(name, enabled, value, "px");
        this.color = color == null ? new Rgb() : color;

        panel = new RoundedPanel();
        panel.setCornerRadius(7);
        panel.setBackground(this.color.toColor());
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectColor();
            }
        });
    }

    @Override
    public GroupLayout.Group addToGroup(GroupLayout.Group group)
    {
        group.addComponent(cb).addComponent(units);
        if (group instanceof GroupLayout.SequentialGroup)
            ((GroupLayout.SequentialGroup)group).addGap(4);
        return group
            .addComponent(tf)
            .addComponent(panel, 20, 25, 25);
    }

    void selectColor()
    {
        if (cb.isSelected()) {
            Color background = JColorChooser.showDialog(null, name, color.toColor());
            if (background != null) {
                color = new Rgb(background);
                panel.setBackground(background);
            }
        }
    }
}
