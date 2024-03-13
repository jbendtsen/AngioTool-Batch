package AngioTool;

import Utils.BatchUtils;
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
        tf.setText(BatchUtils.formatDouble(value));
        tf.setEnabled(enabled);

        cb.setSelected(enabled);
    }

    public double getValue() {
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
            .addComponent(tf);
    }

    public GroupLayout.Group addToParaGroup(GroupLayout.Group paraGroup)
    {
        return paraGroup
            .addComponent(cb)
            .addComponent(units)
            .addComponent(tf);
    }

    public void toggleCheckbox()
    {
        boolean enabled = cb.isSelected();
        double value = getValue();
        tf.setText(BatchUtils.formatDouble(value));
        tf.setEnabled(enabled);
    }
}
