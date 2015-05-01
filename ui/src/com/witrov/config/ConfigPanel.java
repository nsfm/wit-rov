package com.witrov.config;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.witrov.main.MainFrame;

public class ConfigPanel extends JPanel implements ActionListener{
	
	private MainFrame main;
	private JLabel ipLabel, portLabel;
	private JTextField ipField, portField;
	private JButton save;
	private JPanel ipPanel, portPanel, buttonsPanel;
	private PinConfigPanel pinConfigPanel;
	
	public ConfigPanel(int height, int width, MainFrame main)
	{
		this.main = main;
		this.init(height, width);
		
	}
	private void init(int height, int width)
	{
		DatabaseHandle db = new DatabaseHandle();
		
		buttonsPanel = new JPanel();
		
		pinConfigPanel = new PinConfigPanel(this.main);
		
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
		
		buttonsPanel.add(save);
		
		
		this.setLayout(new GridLayout(0,1));
		
		this.add(ipPanel);
		this.add(portPanel);
		this.add(pinConfigPanel);
		this.add(buttonsPanel);
		
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
