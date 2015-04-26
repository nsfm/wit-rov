package com.witrov.main;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import com.witrov.config.ConfigPanel;
import com.witrov.config.DatabaseHandle;
import com.witrov.joystick.Controller;
import com.witrov.joystick.ControllerPanel;
import com.witrov.joystick.XboxController;
import com.witrov.helpers.Vector;


public class MainFrame extends JFrame implements ActionListener{
	
	private Client robot;						//handles main communcation with robot	
	private JButton execute, 					//buttons to handle neccessary events
				listDevices; 		
	private JPanel main,						//panels to hold and format the view 
				buttons,
				stats;
	private JLabel cLabel,
					depthLabel;						//labels to show what is what
	private JTextField command;					//input fields to get input from the user
	private LogPanel log;						//Panel to print out log information
	private JTabbedPane tabs;					//Tab pane to handle tabbed content
	private ControllerPanel controllerPanel;		//panel for choosing and managing joysticks
	private Controller joystick;					//Object to handle joystick input
	private ConfigPanel configPanel;
	private DatabaseHandle db;
	private boolean loggedJoystickError = false;	//This is just so we dont flood the log 
													//when we don't have a connected joystick
	private int joystickThreshold = 20;
	private int lastJoystick[];
	private boolean lastButton[];
	private int lastMisc[];
	
	private int depth = 0;							//Value to set for the depth of the robot
	
	
	/*
	 * Constructor
	 * Creates the Client object and establishes
	 * a connection with the arduino on the given
	 * IP Address and port. Also sets the title of the UI.
	 */
	public MainFrame(String title)
	{

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
		buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Extras"));
		
		main = new JPanel();
		
		stats = new JPanel();
		stats.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Stats"));
		
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
		
		//Adds the label for the depth
		depthLabel = new JLabel("Depth: " + this.depth);
		
		//adds the execute command button, label, and textfield
		//to a panel to be displayed and formatted
		buttons.add(cLabel);
		buttons.add(command);
		buttons.add(execute);
		buttons.add(listDevices);
		stats.add(depthLabel);
		
	
		//initializes log panel for use in main frame
		log = new LogPanel(750,1000, LogPanel.APPEND_BOTTOM);
		
		//Initialize controllerPanel
		controllerPanel = new ControllerPanel(500,500, this);
		Thread updateController = new Thread(controllerPanel);
		
		updateController.start();
		
		//initalize config panel
		configPanel = new ConfigPanel(500,500,this);
		
		//adds the panels to the screen to display components
		main.setLayout(new GridLayout(0,1));
		main.add(controllerPanel);
		main.add(stats);
		main.add(buttons);
		
	
		tabs = new JTabbedPane();
		
		tabs.addTab("Main",null, main, "Main display");
		//tabs.addTab("Controllers", null, controllerPanel, "Controller Configurations");		
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
				robot.sendCode(command.getText());
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
	
	public void initConnection()
	{
		String ip;
		ip = db.findIp();
		String port = null;
		port = db.findPort();
		if(port == null)
		{
			//attempt connection on default port of 23
			port = "23";
			db.insertConfig("port", port);
		}
		boolean debug = false;
		if(ip == null)
		{
			//Get IP Address if we couldn't find one in the Database
			String result = JOptionPane.showInputDialog("Please enter the IP of the Arduino or -1 for Debug.");
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
		
		//Handles all communication to the Arduino on 
		//The given IP Address
		if(!debug)
		{
			robot = new Client(ip, Integer.parseInt(port), this);
		}
	}
	
	public void handleJoystick()
	{
		if(!this.checkJoyStick())
		{
			return;
		}
		int x = this.joystick.getJoystick1X();
		int y = this.joystick.getJoystick1Y();
		
		int r = this.joystick.getJoystick2X();
		
		//add -128 to the value so we get a scale of:
		//               128
		//                |
		//                |
		//                |
		// -128 ----------0---------- 128
		//                |
		//                |
		//                |
		//              -128
		
		x += -128;
		y += -128;
		r += -128;
		
		//We multiple the y axis by -1 just to get a more 
		//familiar system.  i.e (forward is positive backward is negative)
		y*= -1;
		
		//Check thresholds
		x = this.checkThreshold(x);
		y = this.checkThreshold(y);
		r = this.checkThreshold(r);
		
		
		//check if there was a change
		if(this.lastJoystick != null && (x != this.lastJoystick[0] || y != this.lastJoystick[1] || r != this.lastJoystick[2]))
		{	
			//remember current values
			this.lastJoystick[0] = x;
			this.lastJoystick[1] = y;
			this.lastJoystick[2] = r;
			
			//Only change if either coordinate is out side of the threshold
			if(x != 0 || y != 0 || r != 0)
			{
				// t1               t2
				//
				//
				//
				//
				//
				// t3               t4
				int thruster1V = this.checkValue((-1 * y) + (-1 * x) + r);
				int thruster2V = this.checkValue(y + (-1 * x) + r);
				int thruster3V = this.checkValue((-1 * y) + x + r);
				int thruster4V = this.checkValue(y + x + r);
				
				System.out.println("X: " + x + "Y: " + y + " R: " + r + " T1: " + thruster1V + " T2: " + thruster2V + " T3: " + thruster3V + " T4: " + thruster4V);
				
			}
		}
	}

	private int checkValue(int x)
	{
		if(x >= 127)
		{
			return 127;
		}
		else if( x <= -127)
		{
			return -127;
		}
		else 
		{
			return x;
		}
	}
	
	private int checkThreshold(int x)
	{
		if(Math.abs(x) < this.joystickThreshold)
		{
			return 0;
		}
		
		return x;
	}
	
	public LogPanel getLog()
	{
		return this.log;
	}
	
	private boolean checkJoyStick()
	{
		
		if(this.joystick == null && !this.loggedJoystickError)
		{
			if(Controller.getDevices(false).size() <= 0)
			{
				this.log.error("There are no Joysticks attached to the computer");
				this.loggedJoystickError = true;
			}
			else
			{
				this.log.error("Joystick is null. Please choose one from the Joystick tab.");
				this.loggedJoystickError = true;
			}
			return false;
		}
		else if(!this.loggedJoystickError && !this.joystick.isRunning())
		{
			this.log.error("Joystick encountered an error.  Please choose Joystick again in Joystick Tab");
			this.loggedJoystickError = true;
			return false;
		}
		else if(this.joystick != null && this.joystick.isRunning() && this.joystick.getProductId() == Controller.getCurrentDevice().getProductId())
		{
			this.loggedJoystickError = false;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void setJoyStick(Controller joystick)
	{
		if(this.joystick != null)
		{
			this.joystick.kill();
			this.joystick = null;
		}
		if(joystick == null)
		{
			this.log.info("Joystick is not connected");
			return;
		}
		
		this.joystick = joystick;
		this.joystick.start();
		
		if(this.joystick.isRunning())
		{
			this.log.info("Joystick initialized");
		}
		else
		{
			this.log.error("Device not connected");
		}
		
		initLastArrays(true, true, true);
	}
	
	public void initLastArrays(boolean joystick, boolean misc, boolean button)
	{
		if(joystick)
		{
			lastJoystick = new int[5];
			//set initial starting points for joystick
			for(int i = 0; i < this.lastJoystick.length; i++)
			{
				this.lastJoystick[i] = 0;
			}
		}
		
		if(misc)
		{
			this.lastMisc = new int[this.joystick.getMisc().length];
			//set initial misc values
			for(int i = 0; i < this.lastMisc.length; i++)
			{
				this.lastMisc[i] = 0;
			}
		}
		
		if(button)
		{
			this.lastButton = new boolean[this.joystick.getButtons().length];
			//set initial button points to false
			for(int i = 0; i < this.lastButton.length; i++)
			{
				this.lastButton[i] = false;
			}
		}
	}
	
	public Controller getJoystick()
	{
		return this.joystick;
	}
	
	public void resetClient()
	{
		robot = new Client(db.findIp(), Integer.parseInt(db.findPort()), this);
	}
	
	public void handleButtons()
	{
		if(!this.checkJoyStick())
		{
			return;
		}
		
		boolean[] buttons = this.joystick.getButtons();
		
		if(buttons == null)
		{
			return;
		}
		
		for(int i = 0; i < buttons.length; i++)
		{
			if(buttons[i] && !this.lastButton[i])
			{
				switch(i)
				{
					case 1:
						//This doesn't work
						XboxController c = (XboxController)this.joystick;
						byte[] report = new byte[]{(byte)0x00, (byte)0x06, (byte) 0x00,(byte)0xFF,(byte)0x00, (byte)0xFF};
						try {
							c.getDev().sendFeatureReport(report);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							this.log.error("INVALID REPORT");
							e.printStackTrace();
						}
						break;
					default:
						this.log.debug("Button '"+i+"' is pushed.");
						break;
				}
			}
			else if(!buttons[i] && this.lastButton[i])
			{
				this.log.debug("Button '"+i+"' released.");
			}
			this.lastButton[i] = buttons[i];
		}
	}
	
	public void checkDepth()
	{
		if(!this.checkJoyStick())
		{
			return;
		}
		
		if(this.lastButton == null)
		{
			initLastArrays(false, false, true);
		}
		
		boolean[] buttons = this.joystick.getButtons();
		boolean sink = buttons[4];
		boolean rise = buttons[5];
		boolean fast = buttons[0];
		
		int newDepth = this.depth;
		
		if(sink && (fast || !this.lastButton[4]))
		{
			newDepth += 1;
		}
		if(rise && (fast || !this.lastButton[5]))
		{
			newDepth -= 1f;
		}
		
		if(newDepth < 0)
		{
			newDepth = 0;
		}
		this.lastButton[4] = sink;
		this.lastButton[5] = rise;
		this.lastButton[0] = fast;
		if(newDepth != this.depth)
		{
			this.depth = (int) newDepth;
			this.depthLabel.setText("Depth: " + this.depth);
		}
	}
	
	public void handleMiscs()
	{
		if(!this.checkJoyStick())
		{
			return;
		}
		
		int[] miscs = this.joystick.getMisc();
		
		if(miscs == null)
		{
			return;
		}
		
		for(int i = 0; i < miscs.length; i++)
		{
			if(miscs[i] != this.lastMisc[i])
			{
				switch(i)
				{
					default:
						this.log.debug("Misc '"+i+"' value = "+miscs[i]);
				}
			}
			this.lastMisc[i] = miscs[i];
		}
		
	}
	
	public static void main(String[] args)
	{
		String title = "WIT-ROV"; 		//title for UI
		
		//Creates a new MainFrame object and passes the 
		//IP Address of the Arduino this is currently hardcoded in
		//the arduino.cpp file.  Also passes the title for the UI.
		MainFrame m = new MainFrame(title);
		
		m.setState(JFrame.MAXIMIZED_BOTH);
		
		//Displays the UI
		m.setVisible(true);
		
		//Sets the close operation to kill the connection to the arduino
		//when the UI closes
		m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create connection to arduino
		//m.initConnection();
		
		while(true)
		{
			try
			{
				m.checkDepth();
				m.handleButtons();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}	
	}

}
