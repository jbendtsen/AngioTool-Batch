package AngioTool;

import Pixels.Rgb;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;

public class ColorElement
{
    public interface Listener
    {
        void onColorChanged(ColorElement colorElem);
    }

    public Listener listener;
    public String name;
    public RoundedPanel panel;
    public Rgb color;

    public ColorElement(String name, Rgb color)
    {
        this.name = name;
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

    public void setColorChangeListener(Listener ls)
    {
        this.listener = ls;
    }

    public void update(Rgb newColor)
    {
        this.color = newColor;
        panel.setBackground(this.color.toColor());
    }

    public GroupLayout.Group addWithWidth(GroupLayout.Group group)
    {
        return group.addComponent(panel, 20, 30, 30);
    }

    public GroupLayout.Group addWithHeight(GroupLayout.Group group)
    {
        return group.addComponent(panel, 20, 25, 25);
    }

    void selectColor()
    {
        Color background = JColorChooser.showDialog(null, name, color.toColor());
        if (background != null) {
            color = new Rgb(background);
            panel.setBackground(background);
            if (listener != null)
                listener.onColorChanged(this);
        }
    }
}
