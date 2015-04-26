package com.witrov.joystick;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.witrov.main.MainFrame;

public class ControllerPanel extends JPanel implements ActionListener, Runnable{
	
	private JComboBox<HIDDeviceInfo> devices;				//a drop down to select a device
	private MainFrame main;
	private JLabel noDevices, tLabel, devLabel;
	private JTextArea threshold;
	
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
		devs.add(0, null);
		HIDDeviceInfo[] comboList = new HIDDeviceInfo[devs.size()];
		devs.toArray(comboList);
		
		if(comboList.length > 1)
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
		
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Joystick Settings"));
		
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
		
		if(dev == null)
		{
			return;
		}
		
		if(this.devices.getModel().getSize() > 0)
		{
			if(Controller.getCurrentDevice() == null || dev.getProduct_id() != Controller.getCurrentDevice().getProductId())
			{
				if(dev.getProduct_string().toLowerCase().contains("xbox"))
				{
					main.setJoyStick(new XboxController(dev.getVendor_id(), dev.getProduct_id()));
				}	
				else
				{
					main.setJoyStick(new LogitechJoystick(dev.getVendor_id(), dev.getProduct_id()));
				}
			}
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
					if(devs.size() != this.devices.getModel().getSize() -1)
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
