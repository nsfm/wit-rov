package com.witrov.joystick;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.witrov.config.DatabaseHandle;
import com.witrov.main.MainFrame;

public class ControllerPanel extends JPanel implements ActionListener, Runnable{
	
	private JComboBox<HIDDeviceInfo> devices;				//a drop down to select a device
	private MainFrame main;
	private JLabel noDevices, tLabel;
	private JTextField threshold;
	private JButton save;
	private int joystickNumber = -1;								//keeps track of what joystick this panel is for
		
	public static String JOYSTICK_KEY = "joystick_threshold_";
	public ControllerPanel(int height, int width, MainFrame main, int joystickNumber)
	{
		this.joystickNumber = joystickNumber;
		this.main = main;
		this.init(true);
		
	}
	private void init(boolean create)
	{
		if(!create)
		{
			if(devices != null)
			{
				this.remove(devices);
			}
			if(noDevices != null)
			{
				this.remove(noDevices);
			}
		}
		else
		{
			//create the threshold label and text box
			this.tLabel = new JLabel("Threshold: ");
			this.threshold = new JTextField(5);
			
			//find the saved threshold
			DatabaseHandle db = new DatabaseHandle();
			String t = db.findBy(JOYSTICK_KEY+ this.joystickNumber);
			if(t == null)
			{
				t = "10";
				db.insertConfig(JOYSTICK_KEY+ this.joystickNumber, t);
			}
			this.threshold.setText(t);
			db.closeConnection();
			
			//add the label text box and button
			this.add(tLabel);
			this.add(threshold);
			
			//create the save button
			this.save = new JButton("Save");
			this.save.addActionListener(this);
			
			this.add(save);
		}
		ArrayList<HIDDeviceInfo> devs = new ArrayList<HIDDeviceInfo>(Controller.getDevices());
		devs.add(0, null);
		HIDDeviceInfo[] comboList = new HIDDeviceInfo[devs.size()];
		devs.toArray(comboList);
		
		//if there are devices
		if(comboList.length > 1)
		{
			//create the devices drop down
			this.devices = new JComboBox<HIDDeviceInfo>(comboList);
			HIDInfoRenderer r = new HIDInfoRenderer();
			this.devices.setRenderer(r);
			this.devices.addActionListener(this);
			//remove the noDevices Message
			if(noDevices != null)
			{
				this.remove(noDevices);
				noDevices = null;
				
			}
			
			//add the device drop down
			this.add(this.devices);
			
		}
		//if there are no devices
		else
		{
			//create the no devices message
			noDevices = new JLabel("There are no devices connected.");
			
			//remove the devices dropdown
			if(this.devices != null)
			{
				this.remove(this.devices);
				this.devices = null;
			}
			
			//add the no devices label
			this.add(noDevices);
		}
		
		//create the border
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Joystick "+this.joystickNumber+" Settings"));
		
		//refresh the main screen
		main.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.devices)
		{
			createJoystick();
		}
		else if(e.getSource() == save)
		{
			DatabaseHandle db = new DatabaseHandle();
			int oldT = Integer.parseInt(db.findBy(JOYSTICK_KEY+ this.joystickNumber));
			try{
				int t = Integer.parseInt(threshold.getText());
				if(t != oldT)
				{
					db.updateConfig(JOYSTICK_KEY+ this.joystickNumber, ""+t);
					this.main.getLog().info("Joystick "+this.joystickNumber+" Threshold Updated To: " + threshold.getText());
				}
			}catch(Exception e1)
			{
				this.main.getLog().info("Invaid value '" + threshold.getText() + "'");
				this.threshold.setText(""+oldT);
			}
			db.closeConnection();
		}
	}
	
	private void createJoystick()
	{
		//Find selected item
		HIDDeviceInfo dev = this.devices.getItemAt(this.devices.getSelectedIndex());
		//if it was the "Please Chose a Joystick" element return
		if(dev == null)
		{
			return;
		}
		
		//check if this device is in use
		Controller[] selected = Controller.getCurrentDevices();
		boolean quit = false;
		for(int i = 0; i < selected.length; i++)
		{
			if(selected[i] != null)
			{
				if(selected[i].getProductId() == dev.getProduct_id() && selected[i].getVendorId() == dev.getVendor_id())
				{
					this.main.getLog().error("That device is already chosen for Joystick "+selected[i].getJoystickNumber());
					quit = true;
					break;
				}
			}
		}
		
		if(quit)
		{
			this.devices.setSelectedIndex(0);
			return;
		}
		
		//if there were devices to select and a ghost isnt sending funky data
		if(this.devices.getModel().getSize() > 0)
		{
			//if they selected a new device
			if(Controller.getCurrentDevice(this.joystickNumber) == null || dev.getProduct_id() != Controller.getCurrentDevice(this.joystickNumber).getProductId())
			{
				//If it is an xbox controller 
				if(dev.getProduct_string().toLowerCase().contains("xbox"))
				{
					main.setJoyStick(new XboxController(dev.getVendor_id(), dev.getProduct_id(), this.joystickNumber), this.joystickNumber);
				}	
				//if it is a logitech controller
				else
				{
					main.setJoyStick(new LogitechJoystick(dev.getVendor_id(), dev.getProduct_id(), this.joystickNumber), this.joystickNumber);
				}
			}
		}
		else
		{
			main.setJoyStick(null, this.joystickNumber);
		}
	}
	
	@Override
	public void run() {
	
		while(true)
		{
			try
			{
				//find list of devices
				ArrayList<HIDDeviceInfo> devs = Controller.getDevices();
				
				//if there were devices connected last time get the current size
				if(this.devices != null)
				{
					boolean refresh = false;
					//remove other selected joysticks
					
					if(!refresh)
					{
						//check if new devices were added
						for(int i = 0; i < devs.size(); i++)
						{
							for(int j = 0; j < this.devices.getModel().getSize(); j++)
							{
								refresh = true;
								if(this.devices.getModel().getElementAt(j) == null || devs.size() == 0 || this.devices.getModel().getSize() == 0)
								{
									refresh = false;
									continue;
								}
								
								if(devs.get(i).getProduct_id() == this.devices.getModel().getElementAt(j).getProduct_id() && devs.get(i).getVendor_id() == this.devices.getModel().getElementAt(j).getVendor_id() )
								{
									refresh = false;
									break;
								}
							}
							if(refresh)
							{
								break;
							}
						}
					}
					if(!refresh)
					{
						//if there are things in the current list that arent in the devs then we need to refresh
						for(int i = 0; i < this.devices.getModel().getSize(); i++)
						{
							if(this.devices.getModel().getElementAt(i) != null)
							{
								boolean found = false;
								HIDDeviceInfo a = this.devices.getModel().getElementAt(i);
								for(int j = 0; j < devs.size(); j++)
								{
									if(devs.get(j) != null)
									{
										HIDDeviceInfo b = devs.get(j);
										if(a.getProduct_id() == b.getProduct_id() && a.getVendor_id() == b.getVendor_id())
										{
											found = true;
										}
									}
								}
								if(!found)
								{
									refresh = true;
									break;
								}
							}
						}
					}
					
					if(refresh)
					{
						this.init(false);
					}
				}
				else if(devs.size() > 0)
				{
					this.init(false);
				}
				Thread.sleep(100);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	public int getThreshold()
	{
		return Integer.parseInt(this.threshold.getText());
	}
	
}
