package AngioTool;

import Utils.Misc;
import java.awt.event.*;
import javax.swing.*;

public class NumberEntry
{
    public JCheckBox cb;
    public JLabel units;
    public JTextField tf;

    public String name;
    public final double originalValue;

    public NumberEntry(String name, boolean enabled, double value, String unitsStr)
    {
        this.name = name;
        this.originalValue = value;

        cb = new JCheckBox();
        cb.addActionListener((ActionEvent e) -> toggleCheckbox());
        cb.setText(name);

        units = new JLabel(unitsStr, SwingConstants.RIGHT);

        tf = new JTextField();
        tf.setText(Misc.formatDouble(value));
        tf.setEnabled(enabled);

        cb.setSelected(enabled);
    }

    public void update(boolean enabled, double value)
    {
        tf.setText(Misc.formatDouble(value));
        tf.setEnabled(enabled);
        cb.setSelected(enabled);
    }

    public double getValue()
    {
        String str = tf.getText();
        try {
            return Double.parseDouble(str);
        }
        catch (Exception ex) {
            return originalValue;
        }
    }

    public GroupLayout.Group addToSeqGroup(GroupLayout.Group seqGroup)
    {
        return seqGroup
            .addComponent(cb)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(units)
            .addGap(4)
            .addComponent(tf, 64, 64, 64);
    }

    public GroupLayout.Group addToParaGroup(GroupLayout.Group paraGroup)
    {
        final int MIN_TEXT_HEIGHT = 18;
        final int TEXT_HEIGHT = 24;
        return paraGroup
            .addComponent(cb, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT)
            .addComponent(units)
            .addComponent(tf, MIN_TEXT_HEIGHT, TEXT_HEIGHT, TEXT_HEIGHT);
    }

    public void toggleCheckbox()
    {
        boolean enabled = cb.isSelected();
        double value = getValue();
        tf.setText(Misc.formatDouble(value));
        tf.setEnabled(enabled);
    }
}
