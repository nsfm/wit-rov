package com.witrov.config;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
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
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.witrov.main.MainFrame;

public class PinConfigPanel extends JPanel implements ActionListener, ListSelectionListener{
	
	private JList<ArduinoPinConfig> pinConfigList;					//list to hold the pin configs
	private JScrollPane pinConfigScroll;							//scroll bar for the list
	private DefaultListModel<ArduinoPinConfig> pinsModel;			//model to hold the list data
	private JButton addPinConfig, editPinConfig, deletePinConfig;	//buttons for pin manipulation
	private MainFrame main;											//reference back to the main frame
	private JPanel buttonPanel;										//panel to hold the buttons so that we can stack them
	
	
	public PinConfigPanel(MainFrame main)
	{
		this.main = main;
		init();
	}
	
	//Initializes the components for the view
	public void init()
	{
		DatabaseHandle db = new DatabaseHandle();
		
		pinsModel = new DefaultListModel<ArduinoPinConfig>();		
		
		//Get all previously saved pin configs in the database
		ArrayList<ArduinoPinConfig> p = db.findAllPinConfigs();
		
		for(ArduinoPinConfig a : p)
		{
			pinsModel.addElement(a);
		}
				
		//create the pin config viewing list
		pinConfigList = new JList<ArduinoPinConfig>(pinsModel);
		pinConfigList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		pinConfigList.setLayoutOrientation(JList.VERTICAL);
		pinConfigList.setVisibleRowCount(-1);
		pinConfigList.addListSelectionListener(this);
		
		//create the scroll bar for the list
		pinConfigScroll = new JScrollPane(pinConfigList);
		pinConfigScroll.setPreferredSize(new Dimension(250, 100));
		
		//create the add button
		addPinConfig = new JButton("Add");
		addPinConfig.addActionListener(this);
		
		//create the edit button
		editPinConfig = new JButton("Edit");
		editPinConfig.setEnabled(false);
		editPinConfig.addActionListener(this);
		
		//create the delete button
		deletePinConfig = new JButton("Delete");
		deletePinConfig.setEnabled(false);
		deletePinConfig.addActionListener(this);
		
		//create the panel to stack the buttons
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0,1));
		
		//add the buttons to panel in a stack
		buttonPanel.add(addPinConfig);
		buttonPanel.add(editPinConfig);
		buttonPanel.add(deletePinConfig);
		
		//add everything to the view
		this.add(pinConfigScroll);
		this.add(buttonPanel);
		
		//create the border
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Pin Mode Configurations"));
		
		//close the connection to the databse
		db.closeConnection();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting() == false)
		{
			//if there is no selected element
			if(this.pinConfigList.getSelectedIndex() == -1)
			{
				//disable edit and delete because they 
				//require an element to be selected
				this.editPinConfig.setEnabled(false);
				this.deletePinConfig.setEnabled(false);
			}
			else
			{
				//enable the edit and delete in order
				//to edit or delete the element
				this.editPinConfig.setEnabled(true);
				this.deletePinConfig.setEnabled(true);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DatabaseHandle db = new DatabaseHandle();
		if(e.getSource() == editPinConfig)
		{
			//Get selected pin
			ArduinoPinConfig pin = pinConfigList.getSelectedValue();
			
			JPanel editPanel = new JPanel();
			JTextField pin1 = new JTextField(5);
			JTextField pin2 = new JTextField(5);
			JTextField value = new JTextField(5);
			JComboBox<String> mode = new JComboBox<String>(new String[]{"INPUT", "OUTPUT", "INPUT_PULLUP"});
			
			if(pin.getPinMode() == ArduinoPinConfig.STEPPER)
			{
				editPanel.add(new JLabel("Dir Pin: "));
				pin1.setText(""+pin.getPinNumber());
				editPanel.add(pin1);
				editPanel.add(new JLabel("Step Pin: "));
				editPanel.add(pin2);
				pin2.setText(""+pin.getPinNumberTwo());
				editPanel.add(new JLabel("Stepper Number: "));
				value.setText(""+pin.getValue());
				editPanel.add(value);
			}
			else if(pin.getPinMode() == ArduinoPinConfig.THRUSTER)
			{
				editPanel.add(new JLabel("Pin: "));
				pin1.setText(""+pin.getPinNumber());
				editPanel.add(pin1);
				editPanel.add(new JLabel("Thruster Number: "));
				value.setText(""+pin.getValue());
				editPanel.add(value);
			}
			else
			{
				editPanel.add(new JLabel("Pin Mode: "));
				editPanel.add(mode);
				mode.setSelectedIndex(pin.getPinMode());
			}
			
			int result = JOptionPane.showConfirmDialog(null, editPanel, "Edit Pin Config", JOptionPane.OK_OPTION);
			
			if(result == JOptionPane.OK_OPTION)
			{
				ArduinoPinConfig temp = new ArduinoPinConfig(pin.getPinNumber(), pin.getPinNumberTwo(), pin.getPinMode(), pin.getValue());
							
				
				if(pin.getPinMode() == ArduinoPinConfig.STEPPER)
				{
					if(temp.getPinNumber() != Integer.parseInt(pin1.getText()) && db.findPinBy("pinNumber", Integer.parseInt(pin1.getText())) == null)
					{
						pin.setPinNumber(Integer.parseInt(pin1.getText()));
					}
					else if(temp.getPinNumber() != Integer.parseInt(pin1.getText()))
					{
						this.main.getLog().error("Pin '"+pin1.getText()+"' already has a config value");
						return;
					}
					if(temp.getPinNumberTwo() != Integer.parseInt(pin2.getText()) && db.findPinBy("pinNumber", Integer.parseInt(pin2.getText())) == null)
					{
						pin.setPinNumberTwo(Integer.parseInt(pin2.getText()));
					}
					else if(temp.getPinNumberTwo() != Integer.parseInt(pin2.getText()))
					{
						this.main.getLog().error("Pin '"+pin2.getText()+"' already has a config value");
						return;
					}
					
					if(temp.getValue() != Integer.parseInt(value.getText()))
					{
						pin.setValue(Integer.parseInt(value.getText()));
					}
					
					if(db.updatePinConfig(pin))
					{
						this.main.getLog().info("Stepper config updated.");
						this.main.getRobot().setStepper(pin);
					}
				}
				else if(pin.getPinMode() == ArduinoPinConfig.THRUSTER)
				{
					if(temp.getPinNumber() != Integer.parseInt(pin1.getText()) && db.findPinBy("pinNumber", Integer.parseInt(pin1.getText())) == null)
					{
						pin.setPinNumber(Integer.parseInt(pin1.getText()));
					}
					else if(temp.getPinNumber() != Integer.parseInt(pin1.getText()))
					{
						this.main.getLog().error("Pin '"+pin1.getText()+"' already has a config value");
						return;
					}
					if(temp.getValue() != Integer.parseInt(value.getText()))
					{
						pin.setValue(Integer.parseInt(value.getText()));
					}
					
					if(db.updatePinConfig(pin))
					{
						this.main.getLog().info("Thruster config updated.");
						this.main.getRobot().setThruster(pin);
					}
				}
				else
				{
					if(temp.getPinMode() != mode.getSelectedIndex())
					{
						pin.setPinMode(mode.getSelectedIndex());
					}
					
					if(db.updatePinConfig(pin))
					{
						this.main.getLog().info("Pin config updated.");
						this.main.getRobot().setPinMode(pin);
					}
					
				}
			}
			this.pinConfigList.repaint();
			
		}
		else if(e.getSource() == addPinConfig)
		{
			//check if they want a thruster, stepper, or other
			JPanel checkPanel = new JPanel();
			JComboBox<String> type= new JComboBox<String>(new String[]{"Thruster", "Stepper", "Other"});
			
			checkPanel.add(type);
			
			int response = JOptionPane.showConfirmDialog(null, checkPanel, "What pin are you adding?", JOptionPane.CANCEL_OPTION);
			
			if(response == JOptionPane.CANCEL_OPTION)
			{
				return;
			}
			else if(type.getSelectedItem().equals("Stepper"))
			{
				JPanel stepperPanel = new JPanel();
				stepperPanel.add(new JLabel("Dir Pin: "));
				JTextField dirPinField = new JTextField(5);
				stepperPanel.add(dirPinField);
				stepperPanel.add(new JLabel("Step Pin: "));
				JTextField stepPinField = new JTextField(5);
				stepperPanel.add(stepPinField);
				stepperPanel.add(new JLabel("Stepper Number: "));
				JTextField stepperNumber = new JTextField(5);
				stepperPanel.add(stepperNumber);
				
				int result =JOptionPane.showConfirmDialog(null, stepperPanel, "Add Stepper Config", JOptionPane.OK_CANCEL_OPTION);
				
				if(result == JOptionPane.OK_OPTION)
				{
					ArduinoPinConfig pin = new ArduinoPinConfig();
					
					pin.setPinMode(ArduinoPinConfig.STEPPER);
					pin.setPinNumber(Integer.parseInt(dirPinField.getText()));
					pin.setValue(Integer.parseInt(stepperNumber.getText()));
					pin.setPinNumberTwo(Integer.parseInt(stepPinField.getText()));
					
					if(db.insertPinConfig(pin))
					{
							this.pinsModel.addElement(pin);
							this.main.getLog().info("Stepper " + pin.getValue() + " set to dir: " + pin.getPinNumber() + " and step: " + pin.getPinNumberTwo());
							this.pinConfigList.repaint();
					}
				}
			}
			else if(type.getSelectedItem().equals("Thruster"))
			{
				JPanel thrusterPanel = new JPanel();
				thrusterPanel.add(new JLabel("Pin Number: "));
				JTextField pinNumber = new JTextField(5);
				thrusterPanel.add(pinNumber);
				thrusterPanel.add(new JLabel("Thruster Number: "));
				JTextField thrusterNumber = new JTextField(5);
				thrusterPanel.add(thrusterNumber);
				
				int result = JOptionPane.showConfirmDialog(null, thrusterPanel, "Add Thruster Config", JOptionPane.OK_CANCEL_OPTION);
				
				if(result == JOptionPane.OK_OPTION)
				{
					ArduinoPinConfig pin = new ArduinoPinConfig();
					pin.setPinMode(ArduinoPinConfig.THRUSTER);
					pin.setPinNumber(Integer.parseInt(pinNumber.getText()));
					pin.setValue(Integer.parseInt(thrusterNumber.getText()));
					if(db.insertPinConfig(pin))
					{
						this.main.getRobot().setThruster(pin);
						this.pinsModel.addElement(pin);
						this.main.getLog().info("Thruster " + pin.getValue() + " Set to Pin " + pin.getPinNumber());
						this.pinConfigList.repaint();
					}
				}
			}
			else
			{
				//Create the add pin panel
				JPanel ePanel = new JPanel();
				//create the labels and fields for the pin number and modes
				ePanel.add(new JLabel("Pin Number:"));
				JTextField nField = new JTextField(5);
				ePanel.add(nField);
				ePanel.add(new JLabel("Pin Mode:"));
				//create the mode drop down
				JComboBox<String> mode = new JComboBox<String>(new String[]{"INPUT", "OUTPUT", "INPUT_PULLUP"});
				ePanel.add(mode);
				//display the add pop up and get the button result
				int result = JOptionPane.showConfirmDialog(null, ePanel, "Add Pin Config", JOptionPane.OK_CANCEL_OPTION);
				//if they clicked the okay button
				if(result == JOptionPane.OK_OPTION)
				{
					//create a new pin
					ArduinoPinConfig pin = new ArduinoPinConfig();
					pin.setPinMode(mode.getSelectedIndex());
					pin.setPinNumber(Integer.parseInt(nField.getText()));
					pin.setValue(-1);
					//add the pin config to the database
					if(db.insertPinConfig(pin))
					{
						//set the pin mode on the robot
						this.main.getRobot().setPinMode(pin);
						//add the pin config to the list
						this.pinsModel.addElement(pin);
						this.main.getLog().info("Pin "+pin.getPinNumber()+" set to " + pin.pinModeToString());
						this.pinConfigList.repaint();
					}
					//There was an error inserting the pin config
					else
					{
						this.main.getLog().error("There was an error creating configuration for pin "+ pin.getPinNumber());
					}
				}
			}
		}
		else if(e.getSource() == deletePinConfig)
		{
			//get the selected value
			ArduinoPinConfig pin = this.pinConfigList.getSelectedValue();
			
			//delete the config from the database
			if(db.removePinConfig(pin))
			{
				this.main.getLog().info("Pin "+pin.getPinNumber()+" configuration removed");
				//remove the pin config from the list
				this.pinsModel.remove(this.pinConfigList.getSelectedIndex());
				this.pinConfigList.repaint();
			}
			//there was an error removing the pin config from the database
			else
			{
				this.main.getLog().error("There was an error removing the configuration for pin "+ pin.getPinNumber());
			}
		}
		db.closeConnection();
	}
}
