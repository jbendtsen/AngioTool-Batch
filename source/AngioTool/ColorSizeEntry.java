package AngioTool;

import Pixels.Rgb;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;

public class ColorSizeEntry extends NumberEntry
{
    public interface Listener
    {
        void onColorChanged(ColorSizeEntry colorElem);
    }

    public Listener listener;
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

    public void setColorChangeListener(Listener ls)
    {
        this.listener = ls;
    }

    public void update(boolean enabled, double value, Rgb newColor)
    {
        super.update(enabled, value);
        this.color = newColor;
        panel.setBackground(this.color.toColor());
    }

    @Override
    public GroupLayout.Group addToSeqGroup(GroupLayout.Group seqGroup)
    {
        return super.addToSeqGroup(seqGroup).addComponent(panel, 20, 25, 25);
    }

    @Override
    public GroupLayout.Group addToParaGroup(GroupLayout.Group paraGroup)
    {
        return super.addToParaGroup(paraGroup).addComponent(panel, 20, 25, 25);
    }

    void selectColor()
    {
        if (cb.isSelected()) {
            Color background = JColorChooser.showDialog(null, name, color.toColor());
            if (background != null) {
                color = new Rgb(background);
                panel.setBackground(background);
                if (listener != null)
                    listener.onColorChanged(this);
            }
        }
    }
}
