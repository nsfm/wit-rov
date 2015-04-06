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
import com.witrov.joystick.LogitechJoystick;
import com.witrov.main.MainFrame;

public class ConfigPanel extends JPanel implements ActionListener{
	
	private MainFrame main;
	private JLabel ipLabel, portLabel;
	private JTextField ipField, portField;
	private JButton save;
	private JPanel ipPanel, portPanel;
	
	public ConfigPanel(int height, int width, MainFrame main)
	{
		this.main = main;
		this.init(height, width);
		
	}
	private void init(int height, int width)
	{
		DatabaseHandle db = new DatabaseHandle();
		
		ipPanel = new JPanel();
		ipLabel = new JLabel("Arduino IP:");
		ipField = new JTextField(10);
		ipField.setText(db.findIp());
		ipPanel.add(ipLabel);
		ipPanel.add(ipField);
		
		portPanel = new JPanel();
		portLabel = new JLabel("Arduino Port:");
		portField = new JTextField(5);
		portField.setText(db.findPort());
		portPanel.add(portLabel);
		portPanel.add(portField);
		
		
		save = new JButton("Save Changes");
		save.addActionListener(this);
		this.add(ipPanel);
		this.add(portPanel);
		this.add(save);
		db.closeConnection();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == save)
		{
			checkIpPort();
		}
	}
	
	public void checkIpPort()
	{
		String ip = ipField.getText();
		String port = portField.getText();
		DatabaseHandle db = new DatabaseHandle();
		try {
			
			if(!ip.equals(db.findIp()) && port.equals(db.findPort()))
			{
				Socket s = new Socket(ip, Integer.parseInt(port));
				main.getLog().info("Connection successful");
				db.updateConfig("ip", ip);
				db.closeConnection();
				main.resetClient();
				main.getLog().info("Arduino IP Updated: "+ip);
			}
			else if(!port.equals(db.findPort()) && ip.equals(db.findIp()))
			{
				Socket s = new Socket(ip, Integer.parseInt(port));
				main.getLog().info("Connection successful");
				db.updateConfig("port", port);
				db.closeConnection();
				main.resetClient();
				main.getLog().info("Arduino Port Updated: "+port);
			}
			else if(!port.equals(db.findPort()) && !ip.equals(db.findIp()))
			{
				Socket s = new Socket(ip, Integer.parseInt(port));
				main.getLog().info("Connection successful");
				db.updateConfig("port", port);
				db.updateConfig("ip", ip);
				db.closeConnection();
				main.resetClient();
				main.getLog().info("Arduino Port Updated: "+port);
				main.getLog().info("Arduino IP Updated: "+ip);
			}
			
		} catch (UnknownHostException e1) {
			ipField.setText(db.findIp()); 
			portField.setText(db.findPort()); 
			main.getLog().error("Could not connect to ip: "+ip);
		} catch (IOException e1) {
			ipField.setText(db.findIp());
			portField.setText(db.findPort());
			main.getLog().error("Could not connect to IP: "+ip+" on Port: "+port);
		}
	}
}
