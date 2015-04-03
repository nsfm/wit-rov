package com.witrov.main.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.codeminders.hidapi.HIDDeviceInfo;

public class HIDInfoRenderer extends JLabel implements ListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		// TODO Auto-generated method stub
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        //Set the icon and text.  If icon was null, say so.
        HIDDeviceInfo dev = (HIDDeviceInfo) value;
        setText(dev.getProduct_string());

        return this;
    }
}
