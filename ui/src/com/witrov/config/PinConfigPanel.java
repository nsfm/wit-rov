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
		if(e.getSource() == editPinConfig)
		{
			//Get selected pin
			ArduinoPinConfig pin = pinConfigList.getSelectedValue();
			
			//Create the edit panel
			JPanel ePanel = new JPanel();
			//add the fields and labels for the pin number and pin mode
			ePanel.add(new JLabel("Pin Mode:"));
			//create the modes drop down
			JComboBox<String> mode = new JComboBox<String>(ArduinoPinConfig.modes);
			mode.setSelectedIndex(pin.getPinMode());
			ePanel.add(mode);
			
			JTextField vField = new JTextField(5);
			JLabel vLabel = new JLabel("Thruster:");
			vField.setText("-1");
			if(pin.getValue() != -1)
			{
				ePanel.add(vLabel);
				ePanel.add(vField);
				vField.setText(pin.getValue()+"");
			}
			
			//display the pop up and get the result of the button clicked
			int result = JOptionPane.showConfirmDialog(null, ePanel, "Edit Pin "+pin.getPinNumber()+" Config", JOptionPane.OK_CANCEL_OPTION);
			
			//if they clicked ok
			if(result == JOptionPane.OK_OPTION)
			{
				//create a new pin to hold the new data
				//we do this in case the database fails we dont
				//need a new query to revert the old pin in the list
				ArduinoPinConfig newPin = new ArduinoPinConfig();
				newPin.setPinNumber(pin.getPinNumber());
				newPin.setPinMode(mode.getSelectedIndex());
				
				if(newPin.getPinMode() == 3)
				{
					String value = vField.getText();
					if(pin.getPinMode() != 3)
					{
						value = JOptionPane.showInputDialog(null, "What thruster is this for?");
					}
					newPin.setValue(Integer.parseInt(value));
				}
				else
				{
					newPin.setValue(-1);
				}
				DatabaseHandle db = new DatabaseHandle();
				
				//update the pin data in the database
				if(db.updatePinConfig(newPin))
				{
					//copy new mode to current pin in the list
					pin.setPinMode(newPin.getPinMode());
					pin.setValue(newPin.getValue());
					if(pin.getPinMode() == 3)
					{
						this.main.getRobot().setThruster(pin);
					}
					else
					{
						//set the pin mode on the robot
						this.main.getRobot().setPinMode(pin);
					}
					this.main.getLog().info("Pin "+pin.getPinNumber()+" set to " + pin.pinModeToString());
				}
				//update failed
				else 
				{
					this.main.getLog().error("There was an error updating pin "+ pin.getPinNumber());
				}
				//close the connection
				db.closeConnection();
				this.pinConfigList.repaint();
			}
		}
		else if(e.getSource() == addPinConfig)
		{
			//Create the add pin panel
			JPanel ePanel = new JPanel();
			//create the labels and fields for the pin number and modes
			ePanel.add(new JLabel("Pin Number:"));
			JTextField nField = new JTextField(5);
			ePanel.add(nField);
			ePanel.add(new JLabel("Pin Mode:"));
			//create the mode drop down
			JComboBox<String> mode = new JComboBox<String>(ArduinoPinConfig.modes);
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
				DatabaseHandle db = new DatabaseHandle();
				if(pin.getPinNumber() > 9)
				{
					//this.main.getLog().info("An unknown error occured");
				//	return;
				}
				
				if(pin.getPinMode() == 3)
				{
					String value = JOptionPane.showInputDialog(null, "What thruster is this for?");
					pin.setValue(Integer.parseInt(value));
				}
				else
				{
					pin.setValue(-1);
				}
				
				//add the pin config to the database
				if(db.insertPinConfig(pin))
				{
					if(pin.getPinMode() == 3)
					{
						this.main.getRobot().setThruster(pin);
					}
					else
					{
						//set the pin mode on the robot
						this.main.getRobot().setPinMode(pin);
					}
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
		else if(e.getSource() == deletePinConfig)
		{
			DatabaseHandle db = new DatabaseHandle();
			
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
	}
}
