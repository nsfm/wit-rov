package com.witrov.main.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javafx.scene.layout.Border;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.witrov.joystick.Controller;
import com.witrov.joystick.Joystick;
import com.witrov.main.MainFrame;

public class JoyStickPanel extends JPanel implements ActionListener{
	
	private JComboBox<HIDDeviceInfo> devices;				//a drop down to select a device
	private MainFrame main;
	
	public JoyStickPanel(int height, int width, MainFrame main)
	{
		this.main = main;
		this.init(height, width);
		
	}
	private void init(int height, int width)
	{
		ArrayList<HIDDeviceInfo> devs = Controller.getDevices(false);
		HIDDeviceInfo[] comboList = new HIDDeviceInfo[devs.size()];
		devs.toArray(comboList);
		
		devices = new JComboBox<HIDDeviceInfo>(comboList);
		HIDInfoRenderer r = new HIDInfoRenderer();
		devices.setRenderer(r);
		devices.addActionListener(this);

		createJoystick();
		
		this.add(devices);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == devices)
		{
			createJoystick();
		}
	}
	
	private void createJoystick()
	{
		HIDDeviceInfo dev = devices.getItemAt(devices.getSelectedIndex());
		Joystick j = new Joystick(dev.getVendor_id(), dev.getProduct_id());
		main.setJoyStick(j);
	}
	
}
