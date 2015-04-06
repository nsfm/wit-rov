package com.witrov.joystick;

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
import com.witrov.main.MainFrame;

public class ControllerPanel extends JPanel implements ActionListener, Runnable{
	
	private JComboBox<HIDDeviceInfo> devices;				//a drop down to select a device
	private MainFrame main;
	private JLabel noDevices;
	public ControllerPanel(int height, int width, MainFrame main)
	{
		this.main = main;
		this.init(true);
		
	}
	private void init(boolean create)
	{
		if(!create)
		{
			this.removeAll();
		}
		ArrayList<HIDDeviceInfo> devs = Controller.getDevices(false);
		HIDDeviceInfo[] comboList = new HIDDeviceInfo[devs.size()];
		devs.toArray(comboList);
		
		if(comboList.length > 0)
		{
			devices = new JComboBox<HIDDeviceInfo>(comboList);
			HIDInfoRenderer r = new HIDInfoRenderer();
			devices.setRenderer(r);
			devices.addActionListener(this);
			if(noDevices != null)
			{
				this.remove(noDevices);
				noDevices = null;
				
			}
			this.add(devices);
			
		}
		else
		{
			if(this.devices != null)
			{
				this.devices = null;
			}
			
			if(noDevices == null)
			{
				noDevices = new JLabel("There are no devices connected.");
			}
			else
			{
				this.removeAll();
			}
			this.add(noDevices);
		}
		main.repaint();
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
		if(this.devices.getModel().getSize() > 0)
		{
			main.setJoyStick(new LogitechJoystick(dev.getVendor_id(), dev.getProduct_id()));
		}
		else
		{
			main.setJoyStick(null);
		}
	}
	
	@Override
	public void run() {
	
		while(true)
		{
			try
			{
				if(this.devices != null)
				{
					ArrayList<HIDDeviceInfo> devs = Controller.getDevices(false);
					if(devs.size() != this.devices.getModel().getSize())
					{
						this.init(false);
					}
				}
				else
				{
					init(true);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
}
