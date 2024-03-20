package AngioTool;

import Pixels.Rgb;
import java.awt.Color;
import javax.swing.*;

public class SizeColorEntry extends NumberEntry
{
    public final ColorElement colorElem;

    public SizeColorEntry(String name, boolean enabled, double value, Rgb color)
    {
        super(name, enabled, value, "px");
        this.colorElem = new ColorElement(color);
    }

    public Rgb getColor()
    {
        return colorElem.color;
    }

    public void setColorChangeListener(ColorElement.Listener ls)
    {
        this.colorElem.setColorChangeListener(ls);
    }

    public void update(boolean enabled, double value, Rgb newColor)
    {
        super.update(enabled, value);
        this.colorElem.update(newColor);
    }

    @Override
    public GroupLayout.Group addToSeqGroup(GroupLayout.Group seqGroup)
    {
        return super.addToSeqGroup(seqGroup).addComponent(colorElem.panel, 20, 30, 30);
    }

    @Override
    public GroupLayout.Group addToParaGroup(GroupLayout.Group paraGroup)
    {
        return super.addToParaGroup(paraGroup).addComponent(colorElem.panel, 20, 25, 25);
    }
}
