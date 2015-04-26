package com.witrov.joystick;

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

        //Set text if device is null make that the choose device
        HIDDeviceInfo dev = (HIDDeviceInfo) value;
        
        if(dev == null)
        {
        	setText("Please choose a joystick");
        }
        else
        {
        	setText(dev.getProduct_string());
        }

        return this;
    }
}
