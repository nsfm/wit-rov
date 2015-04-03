package com.witrov.main;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.witrov.config.DatabaseHandle;
import com.witrov.joystick.Controller;
import com.witrov.joystick.Joystick;
import com.witrov.main.ui.ConfigPanel;
import com.witrov.main.ui.JoyStickPanel;
import com.witrov.main.ui.LogPanel;


public class MainFrame extends JFrame implements ActionListener{
	
	private Client robot;						//handles main communcation with robot	
	private JButton execute, 					//buttons to handle neccessary events
				listDevices; 		
	private JPanel main,						//panels to hold and format the view 
				buttons;
	private JLabel cLabel;						//labels to show what is what
	private JTextField command;					//input fields to get input from the user
	private LogPanel log;						//Panel to print out log information
	private JTabbedPane tabs;					//Tab pane to handle tabbed content
	private JoyStickPanel joystickPanel;		//panel for choosing and managing joysticks
	private Joystick joystick;					//Object to handle joystick input
	private ConfigPanel configPanel;
	private DatabaseHandle db;
	private int joystickThreshold = 25;
	private int lastJoystick[];
	private boolean lastButton[];
	/*
	 * Constructor
	 * Creates the Client object and establishes
	 * a connection with the arduino on the given
	 * IP Address and port. Also sets the title of the UI.
	 */
	public MainFrame(String ip, String title, int port, boolean debug) throws UnknownHostException, IOException
	{
			//Handles all communication to the Arduino on 
			//The given IP Address
			if(!debug)
			{
				robot = new Client(ip, port);
			}
			this.setTitle(title);
			this.db = new DatabaseHandle();
			init();
	}
	
	/*
	 * Initializes UI Components
	 */
	public void init()
	{
		//Initializes first set of buttons
		buttons = new JPanel();
		buttons.setSize(new Dimension(500,500));
		
		main = new JPanel();
		
		//Initializes the Execute button 
		//and sets the Click listener to this class
		execute = new JButton("Execute Command");
		//method is this.actionPerformed
		execute.addActionListener(this);
		
		listDevices = new JButton("List Controllers");
		listDevices.addActionListener(this);
		
		//Initializes a label for the text field
		cLabel = new JLabel("Command");
		
		//Initializes a text field for command input
		command = new JTextField(5);
		
		//adds the execute command button, label, and textfield
		//to a panel to be displayed and formatted
		buttons.add(cLabel);
		buttons.add(command);
		buttons.add(execute);
		buttons.add(listDevices);
		
	
		//initializes log panel for use in main frame
		log = new LogPanel(750,1000, LogPanel.APPEND_BOTTOM);
		
		//Initialize JoyStickPanel
		joystickPanel = new JoyStickPanel(500,500, this);
		
		//initalize config panel
		configPanel = new ConfigPanel(500,500,this);
		
		//adds the panels to the screen to display components
		main.add(buttons, BorderLayout.WEST);
	
		tabs = new JTabbedPane();
		
		tabs.addTab("Main",null, main, "Main display");
		tabs.addTab("Joystick", null, joystickPanel, "Joystick Configurations");		
		tabs.addTab("Config", null, configPanel, "Main Configurations");
		this.add(tabs, BorderLayout.WEST);
		this.add(log, BorderLayout.EAST);
		
		//Packs the frame meaning it shrinks the window
		//to the smallest size needed for the components it holds
		this.pack();
		
		this.setMinimumSize(this.getSize());
	}
	

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 * 
	 * Handles all Button clicks for the main view
	 * 
	 * 		e is the click event and the source will be the button
	 * 			that was clicked
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == execute)
		{
			//sends whatever text is in the command text field to the robot
			try
			{
				if(!robot.sendCode(command.getText()))
				{
					log.error("There was an error executing the opcode");
				}
				else
				{
					this.log.info(command.getText()+" Successfully executed");
				}
			}catch(Exception er)
			{
				log.error("The following error occured while executing the opcode:");
				log.error(er.getMessage());
			}
		}
		else if(e.getSource() == listDevices)
		{
			this.log.info(Controller.showDevices(true, false));
		}

	}
	
	public void showJoyStickValues()
	{
		if(!this.checkJoyStick())
		{
			return;
		}
		int x = this.joystick.getX();
		int y = this.joystick.getY();
		int s = this.joystick.getStrafe();
		
		if(Math.abs(x - this.lastJoystick[0]) > this.joystickThreshold)
		{
			if(x >= 0 && x <= this.joystickThreshold)
			{
				this.log.debug("MOVING LEFT: "+x);
				this.lastJoystick[0] = x;
			}
			else if( x >= (255-this.joystickThreshold) && x <= 255)
			{
				this.log.debug("MOVING RIGHT: "+x);
				this.lastJoystick[0] = x;
			}
			else if( x >= 113 && x <= (113+this.joystickThreshold))
			{
				this.log.debug("STOPPING LR: "+x);
				this.lastJoystick[0] = x;
			}
		}
		
		if(Math.abs(y - this.lastJoystick[1]) > this.joystickThreshold)
		{
			if(y >= 0 && y <= this.joystickThreshold)
			{
				this.log.debug("MOVING FORWARD: "+y);
				this.lastJoystick[1] = y;
			}
			else if( y >= (255-this.joystickThreshold) && y <= 255)
			{
				this.log.debug("MOVING BACKWARD: "+y);
				this.lastJoystick[1] = y;
			}
			else if( y >= 113 && y <= (113+this.joystickThreshold))
			{
				this.log.debug("STOPPING FB: "+y);
				this.lastJoystick[1] = y;
			}
		}
		
		if(Math.abs(s - this.lastJoystick[3]) > this.joystickThreshold)
		{
			if(s >= 0 && s <= this.joystickThreshold)
			{
				this.log.debug("TURNING LEFT: "+s);
				this.lastJoystick[3] = s;
			}
			else if( s >= (255-this.joystickThreshold) && s <= 255)
			{
				this.log.debug("TURNING RIGHT: "+s);
				this.lastJoystick[3] = s;
			}
			else if( s >= 113 && s <= (113+this.joystickThreshold))
			{
				this.log.debug("STOPPING STRAFE: "+s);
				this.lastJoystick[3] = s;
			}
		}
	}
	
	public void showButtonValues()
	{
		if(this.joystick == null)
		{
			return;
		}
		boolean trigger = this.joystick.getTrigger();
		boolean button2 = this.joystick.getButton2();
		if(trigger && !this.lastButton[0])
		{
			if(button2)
			{
				robot.sendCode("d060");
				this.lastButton[1] = button2;
			}
			else
			{
				robot.sendCode("d061");
				this.lastButton[1] = false;
			}
			this.lastButton[0] = trigger;
		}
		else if(!trigger && this.lastButton[0])
		{
			this.lastButton[0] = false;
		}
	}
	
	public void showKnobValues()
	{
		if(this.joystick == null)
		{
			return;
		}
		int knob = this.joystick.getKnob();
		if(knob != this.lastJoystick[4])
		{
			this.log.debug("KNOB CHANGE: "+knob);
			this.lastJoystick[4] = knob;
		}
	}
	
	public LogPanel getLog()
	{
		return this.log;
	}
	
	public boolean checkJoyStick()
	{
		if(this.joystick == null)
		{
			this.log.error("Joystick is null. Please choose one from the Joystick tab.");
			return false;
		}
		else if(!this.joystick.isRunning())
		{
			this.log.error("Joystick encountered an error.  Please choose Joystick again in Joystick Tab");
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public void setJoyStick(Joystick joystick)
	{
		if(this.joystick != null)
		{
			this.joystick.kill();
		}
		this.joystick = joystick;
		this.log.info("Joystick initialized");
		joystick.start();
		
		lastJoystick = new int[5];
		//set initial starting points for joystick
		lastJoystick[0] = this.joystick.getX();
		lastJoystick[1] = this.joystick.getY();
		lastJoystick[3] = this.joystick.getStrafe();
		lastJoystick[4] = this.joystick.getKnob();
		
		this.lastButton = new boolean[12];
		//set initial button points to false
		for(int i = 0; i < 12; i++)
		{
			this.lastButton[i] = false;
		}
	}
	public Joystick getJoystick()
	{
		return this.joystick;
	}
	
	public void resetClient()
	{
		
		try {
			robot = new Client(db.findIp(), 23);
		} catch (UnknownHostException e) {
			System.exit(0);
		} catch (IOException e) {
			System.exit(0);
		}
	}
	
	public static void main(String[] args)
	{
		String ip = "null";
		String title = "WIT-ROV"; 		//title for UI
		int port = 23; 					//port of arduino
		boolean debug = false;
		
		try
		{
			
			DatabaseHandle db = new DatabaseHandle();
			ip = db.findIp();
			if(ip == null)
			{
				//Get IP Address if we couldn't find one in the Database
				String result = JOptionPane.showInputDialog("Please enter the IP of the Arduino or -1 from Debug.");
				ip = result;
				if(result == null) //User pressed Cancel
				{
					System.exit(0);
				}
				else if(ip.equals("-1")) //User wants to enter debug mode
				{
					debug = true;
				}
				else
				{
					db.insertConfig("ip", ip);
				}
			}
			//Creates a new MainFrame object and passes the 
			//IP Address of the Arduino this is currently hardcoded in
			//the arduino.cpp file.  Also passes the title for the UI.
			MainFrame m = new MainFrame(ip, title, port, debug);
			
			m.setState(JFrame.MAXIMIZED_BOTH);
			
			//Displays the UI
			m.setVisible(true);
			
			//Sets the close operation to kill the connection to the arduino
			//when the UI closes
			m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			while(true)
			{
				m.showButtonValues();
				m.showKnobValues();
				m.showJoyStickValues();
				Thread.sleep(100);
				
			}
			
		} catch (Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "There was an error estabilishing a connection to "+ip, title, JOptionPane.ERROR_MESSAGE);	
		}		
	}

}
