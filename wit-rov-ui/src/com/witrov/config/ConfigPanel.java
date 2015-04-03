package com.witrov.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
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
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.witrov.joystick.Controller;
import com.witrov.joystick.Joystick;
import com.witrov.main.MainFrame;

public class ConfigPanel extends JPanel implements ActionListener{
	
	private MainFrame main;
	private JLabel ipLabel;
	private JTextField ipField;
	private JButton save;
	private JPanel ipPanel;
	
	public ConfigPanel(int height, int width, MainFrame main)
	{
		this.main = main;
		this.init(height, width);
		
	}
	private void init(int height, int width)
	{
		
		ipPanel = new JPanel();
		
		ipLabel = new JLabel("Arduino IP:");
		
		ipField = new JTextField(10);
		
		DatabaseHandle db = new DatabaseHandle();
		ipField.setText(db.findIp());
		db.closeConnection();
		
		save = new JButton("Save Changes");
		save.addActionListener(this);
		
		ipPanel.add(ipLabel);
		ipPanel.add(ipField);
		
		this.add(ipPanel);
		this.add(save);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == save)
		{
			checkIp();
		}
	}
	
	public void checkIp()
	{
		String ip = ipField.getText();
		DatabaseHandle db = new DatabaseHandle();
		try {
			
			if(!ip.equals(db.findIp()))
			{
				Socket s = new Socket(ip, 23);
				main.getLog().info("Connection successful");
				db.updateConfig("ip", ip);
				db.closeConnection();
				main.resetClient();
				main.getLog().info("Arduino IP Updated: "+ip);
			}
			else
			{
				main.getLog().info("Arduino IP not changed");
			}
		} catch (UnknownHostException e1) {
			ipField.setText(db.findIp()); 
			main.getLog().error("Could not connect to ip: "+ip);
		} catch (IOException e1) {
			ipField.setText(db.findIp());
			main.getLog().error("Could not connect to ip: "+ip);
		}
	}
}
