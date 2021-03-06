package com.witrov.main;
import java.awt.BorderLayout;
import java.awt.GridLayout;
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

import com.witrov.config.ArduinoPinConfig;
import com.witrov.config.ConfigPanel;
import com.witrov.config.DatabaseHandle;
import com.witrov.joystick.Controller;
import com.witrov.joystick.ControllerPanel;
import com.witrov.joystick.XboxController;

public class MainFrame extends JFrame implements ActionListener{
	
	final private int HEADINGBUFFER = 20;
	private Client robot;						//handles main communcation with robot	
	private JButton execute, 					//buttons to handle neccessary events
				listDevices,
				reconnect; 		
	private JPanel main,						//panels to hold and format the view 
				buttons;
	private JLabel cLabel;						//labels to show what is what
	private StatsPanel stats;
	private JTextField command;					//input fields to get input from the user
	private LogPanel log;						//Panel to print out log information
	private JTabbedPane tabs1, tabs2;					//Tab pane to handle tabbed content
	private ControllerPanel[] controllerPanels;		//panel for choosing and managing joysticks
	private Controller[] joysticks;					//Object to handle joystick 1 input
	private ConfigPanel configPanel;
	private DatabaseHandle db;
	private boolean loggedJoystickError[];	//This is just so we dont flood the log 
													//when we don't have a connected joystick
	private int lastJoystick[][];
	private boolean lastButton[][];
	private int lastMisc[][];
	
	private int numberOfJoysticks;
	
	private int depth = 0;							//Value to set for the depth of the robot
	
	private int[] thrusterConfig;					//0 = front left, 1 = front right, 2 = back left, 3 = back right
	
	private DisplayPanel disp;
	
	private HelpPanel help;
	
	private boolean keepHeading = false;
	private int headingToKeep = 0;
	private int[] headingAvg = new int[this.HEADINGBUFFER];
	private int headingCount = 0;
	
	private int stepsPerSecond;
	private boolean thrustersMoving = false;
	/*
	 * Constructor
	 * Creates the Client object and establishes
	 * a connection with the arduino on the given
	 * IP Address and port. Also sets the title of the UI.
	 */
	public MainFrame(String title, int numberOfJoysticks)
	{

			this.setTitle(title);
			this.db = new DatabaseHandle();
			this.numberOfJoysticks = numberOfJoysticks;
			init();
	}
	
	/*
	 * Initializes UI Components
	 */
	public void init()
	{
		this.loggedJoystickError = new boolean[this.numberOfJoysticks];
		this.joysticks = new Controller[this.numberOfJoysticks];
		Controller.initDevices(this.numberOfJoysticks);
		this.thrusterConfig = new int[]{1,2,3,4};
		//Initializes first set of buttons
		buttons = new JPanel();
		buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Extras"));
		
		main = new JPanel();
		
		stats = new StatsPanel();
		
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
				
		//init reconnect button
		reconnect = new JButton("Reconnect");
		reconnect.addActionListener(this);
		
		//adds the execute command button, label, and textfield
		//to a panel to be displayed and formatted
		buttons.add(cLabel);
		buttons.add(command);
		buttons.add(execute);
		buttons.add(listDevices);
		buttons.add(reconnect);
		
		//initializes log panel for use in main frame
		log = new LogPanel(750,1000, LogPanel.APPEND_BOTTOM);
		
		Controller.updateDevices();
		
		//Initialize controller1Panel for joysticks
		JPanel controllers = new JPanel();
		controllers.setLayout(new GridLayout(0,1));
		controllerPanels = new ControllerPanel[this.numberOfJoysticks];
		for(int i = 0; i < this.numberOfJoysticks; i++)
		{
			controllerPanels[i] = new ControllerPanel(500,500, this, i+1);
			Thread updateController = new Thread(controllerPanels[i]);
			updateController.start();
			controllers.add(controllerPanels[i]);
		}
		
		//initalize config panel
		configPanel = new ConfigPanel(500,500,this);
		
		//adds the panels to the screen to display components
		main.setLayout(new GridLayout(0,1));
		main.add(controllers);
		main.add(stats);
		
	
		tabs1 = new JTabbedPane();
		
		tabs1.addTab("Main",null, main, "Main display");
		//tabs.addTab("Controllers", null, controllerPanel, "Controller Configurations");		
		tabs1.addTab("Config", null, configPanel, "Main Configurations");
		tabs1.addTab("Extras", null, buttons, "Extras");
		
		disp = new DisplayPanel(750, 1000);
		
		help = new HelpPanel();
		
		tabs2 = new JTabbedPane();
		
		tabs2.addTab("Log", null, log, "Log");
		tabs2.addTab("Display", null, disp, "Display");
		tabs2.addTab("Help", null, help, "Help");
		this.add(tabs1, BorderLayout.WEST);
		this.add(tabs2, BorderLayout.EAST);
		
		//Packs the frame meaning it shrinks the window
		//to the smallest size needed for the components it holds
		this.pack();
		
		this.setMinimumSize(this.getSize());
	}

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
				er.printStackTrace();
			}
		}
		else if(e.getSource() == listDevices)
		{
			this.log.info(Controller.showDevices(true, false));
		}
		else if(e.getSource() == reconnect)
		{
			this.log.info("Reconnection in progress...");
			this.initConnection();
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
			
			if(robot.isConnected())
			{
				for(ArduinoPinConfig pin : db.findAllPinConfigs())
				{
					switch(pin.getPinMode())
					{
						case 0:
						case 1:
						case 2:
							robot.setPinMode(pin);
							break;
						case 3:
							robot.setThruster(pin);
							break;
						case 4:
							robot.setStepper(pin);
							break;
					}
					log.info("Pin "+ pin.pinNumberToString() + " set to " +pin.pinModeToString());
				}
			}
		}
	}
	
	public void handleMainMovement(int joystickNumber)
	{
		if(!this.checkJoyStick(joystickNumber))
		{
			return;
		}
		int x = this.joysticks[joystickNumber-1].getJoystick1X();
		int y = this.joysticks[joystickNumber-1].getJoystick1Y();
		
		int z = this.joysticks[joystickNumber-1].getMisc()[0];
		
		int r = this.joysticks[joystickNumber-1].getJoystick2X();
		
		boolean[] buttons = this.joysticks[joystickNumber-1].getButtons();
		int currentHeading = this.stats.getHeading();
		
		if(buttons[3] && !this.lastButton[joystickNumber-1][3])
		{
			this.lastButton[joystickNumber-1][3] = buttons[3];
			keepHeading = !keepHeading;
			if(keepHeading)
			{
				this.stats.setHeadingToKeep(this.stats.getHeading());
			}
		}
		else
		{
			this.lastButton[joystickNumber-1][3] = false;
		}
		
		if(keepHeading)
		{
			try{
				headingToKeep = this.stats.getHeadingToKeep();
				if (headingToKeep > 180)
				{
					headingToKeep = -1 * (360 - headingToKeep);
				}
				if (currentHeading > 180)
				{
					currentHeading = -1 * (360 - currentHeading);
				}
				int error = headingToKeep - currentHeading;
				this.getLog().info("Heading Error: " + error);
				this.getLog().info("Heading to keep: " + headingToKeep);
				if(error != 0)
				{
					if(Math.abs(error) > 15)
					{
						error = -error;
					}
					
					r = error/2;
					this.thrustersMoving = true;
				}
				else
				{
					r = 0;
				}
				this.getLog().info("R: " + r);
			}catch(Exception e)
			{
				this.getLog().error("Heading to keep is an invalid value!");
			}
		}
		
		boolean xyzMult = this.joysticks[joystickNumber-1].getButtons()[9];
		boolean rMult = this.joysticks[joystickNumber-1].getButtons()[8];
		
		
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
		x = this.checkThreshold(x, joystickNumber);
		y = this.checkThreshold(y, joystickNumber);
		r = this.checkThreshold(r, joystickNumber);
		z = this.checkThreshold(z, joystickNumber);
		
		//Check multiplier
		if(xyzMult)
		{
			x *= 2;
			y *= 2;
			z *= 2;
		}
		
		if(rMult && !keepHeading)
		{
			r *= 2;
		}
		
		//check if there was a change
		if(this.lastJoystick[joystickNumber-1] != null && (x != this.lastJoystick[joystickNumber-1][0] || y != this.lastJoystick[joystickNumber-1][1] || r != this.lastJoystick[joystickNumber-1][2]))
		{	
			//remember current values
			this.lastJoystick[joystickNumber-1][0] = x;
			this.lastJoystick[joystickNumber-1][1] = y;
			this.lastJoystick[joystickNumber-1][2] = r;
			
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
				
				//added in ( 1 * var) for spacing 
				int thruster1V = this.checkValue(( 1 * y) + ( 1 * x) + ( 1 * r));
				int thruster2V = this.checkValue(( 1 * y) + (-1 * x) + (-1 * r));
				int thruster3V = this.checkValue((-1 * y) + ( 1 * x) + (-1 * r));
				int thruster4V = this.checkValue(( 1 * y) + ( 1 * x) + (-1 * r));
				
				this.stats.getThruster(this.thrusterConfig[0]).setVelocity(thruster1V);
				this.stats.getThruster(this.thrusterConfig[1]).setVelocity(thruster2V);
				this.stats.getThruster(this.thrusterConfig[2]).setVelocity(thruster3V);
				this.stats.getThruster(this.thrusterConfig[3]).setVelocity(thruster4V);
				
				this.robot.sendCode("t0"+(400 + thruster1V));
				this.robot.sendCode("t1"+(400 + thruster2V));
				this.robot.sendCode("t2"+(400 + thruster3V));
				this.robot.sendCode("t3"+(400 + thruster4V));
				this.thrustersMoving = true;
			}
			else if(this.thrustersMoving)
			{
				this.thrustersMoving = false;
				this.robot.sendCode("t0"+(400));
				this.robot.sendCode("t1"+(400));
				this.robot.sendCode("t2"+(400));
				this.robot.sendCode("t3"+(400));
			}
		}
		
		if(this.lastMisc[joystickNumber - 1] != null && z != this.lastMisc[joystickNumber -1][0])
		{
			this.robot.sendCode("t4"+(400 + z));
			this.robot.sendCode("t5" + (400 - z));
			this.thrustersMoving = true;
		}
		else if(this.thrustersMoving)
		{
			this.thrustersMoving = false;
			this.robot.sendCode("t4"+(400));
			this.robot.sendCode("t5"+(400));
		}
	}

	//Keep the value between -127-127
	private int checkValue(int x)
	{
		if(x >= 400)
		{
			return 400;
		}
		else if( x <= -400)
		{
			return -400;
		}
		else 
		{
			return x;
		}
	}
	
	//dont use the data unless it is outside the range of the threshold
	private int checkThreshold(int x, int joystickNumber)
	{
		if(Math.abs(x) < this.controllerPanels[joystickNumber-1].getThreshold())
		{
			return 0;
		}
		else
		{
			x -= this.controllerPanels[joystickNumber-1].getThreshold();
		}
		
		return x;
	}
	
	//returns the log panel
	public LogPanel getLog()
	{
		return this.log;
	}
	
	//checks if the joystick is in a running and usable state
	private boolean checkJoyStick(int joystickNumber)
	{
		//is there a joystick selected?
		if(this.joysticks[joystickNumber-1] == null && !this.loggedJoystickError[joystickNumber-1])
		{
			//there arent any devices
			if(Controller.getDevices().size() <= 0)
			{
				this.log.error("There are no Joysticks attached to the computer");
				this.loggedJoystickError[joystickNumber-1] = true;
			}
			//User has not picked a joystick
			else
			{
				this.log.error("Joystick is null. Please choose one from the Joystick tab.");
				this.loggedJoystickError[joystickNumber-1] = true;
			}
			return false;
		}
		//if the joystick update thread is not running
		else if(!this.loggedJoystickError[joystickNumber-1] && !this.joysticks[joystickNumber-1].isRunning())
		{
			this.log.error("Joystick encountered an error.  Please choose Joystick again in Joystick Tab");
			this.loggedJoystickError[joystickNumber-1] = true;
			return false;
		}
		//if the joystick is in a fully functional state
		else if(this.joysticks[joystickNumber-1] != null && this.joysticks[joystickNumber-1].isRunning() && this.joysticks[joystickNumber-1].getProductId() == Controller.getCurrentDevice(joystickNumber).getProductId())
		{
			this.loggedJoystickError[joystickNumber-1] = false;
			return true;
		}
		//if the joystick is in a different unknown state (not working right)
		else
		{
			return false;
		}
	}
	//Sets the joystick to a Controller device
	public void setJoyStick(Controller joystick, int joystickNumber)
	{
		//if there was already a joystick we will stop and remove that joystick
		if(this.joysticks[joystickNumber-1] != null)
		{
			this.joysticks[joystickNumber-1].kill();
			this.joysticks[joystickNumber-1] = null;
		}
		
		//If the selected joystick was disconnected between selection and creation
		if(joystick == null)
		{
			this.log.info("Joystick is not connected");
			return;
		}
		
		//set the new joystick
		this.joysticks[joystickNumber-1] = joystick;
		//start the new joystick update function
		this.joysticks[joystickNumber-1].start();
		
		//check if it is running correctly
		if(this.joysticks[joystickNumber-1].isRunning())
		{
			this.log.info("Joystick "+joystickNumber+" initialized");
		}
		//joystick is not running correctly
		else
		{
			this.log.error("Device not connected");
		}
		
		//reinitialize last joystick data
		initLastArrays(true, true, true, joystickNumber);
	}
	
	//Initialize last joystick data
	public void initLastArrays(boolean joystick, boolean misc, boolean button, int joystickNumber)
	{
		//initializes the last joystick (x and y axies) data
		if(joystick)
		{
			lastJoystick = new int[this.numberOfJoysticks][5];
			//set initial starting points for joystick
			for(int i = 0; i < this.lastJoystick[joystickNumber-1].length; i++)
			{
				this.lastJoystick[joystickNumber-1][i] = 0;
			}
		}
		
		//initializes the last misc data (any pressure sensitive button or other non joystick non button)
		if(misc)
		{
			this.lastMisc = new int[this.numberOfJoysticks][this.joysticks[joystickNumber-1].getMisc().length];
			//set initial misc values
			for(int i = 0; i < this.lastMisc[joystickNumber-1].length; i++)
			{
				this.lastMisc[joystickNumber-1][i] = 0;
			}
		}
		
		//initializes the pressed button data
		if(button)
		{
			this.lastButton = new boolean[this.numberOfJoysticks][this.joysticks[joystickNumber-1].getButtons().length];
			//set initial button points to false
			for(int i = 0; i < this.lastButton[joystickNumber-1].length; i++)
			{
				this.lastButton[joystickNumber-1][i] = false;
			}
		}
	}
	
	//returns a Controller object
	public Controller getJoystick(int joystickNumber)
	{
		return this.joysticks[joystickNumber-1];
	}
	
	//resets the connection the the arduino
	public void resetClient()
	{
		robot = new Client(db.findIp(), Integer.parseInt(db.findPort()), this);
	}
	
	//handles button pushes.
	//This should probably only be used for debugging
	public void handleButtons(int joystickNumber)
	{
		//check if joystick is functional
		if(!this.checkJoyStick(joystickNumber))
		{
			return;
		}
		
		////get current button states
		boolean[] buttons = this.joysticks[joystickNumber-1].getButtons();
		
		if(buttons == null)
		{
			return;
		}
		
		//loop through each button and perform a task based on the button
		for(int i = 0; i < buttons.length; i++)
		{
			if(buttons[i] && !this.lastButton[joystickNumber-1][i])
			{
				switch(i)
				{
				//attempt to change the xbox controller LED
					case 1:
						//This doesn't work
						XboxController c = (XboxController)this.joysticks[joystickNumber-1];
						byte[] report = new byte[]{(byte)0x00, (byte)0x06, (byte) 0x00,(byte)0xFF,(byte)0x00, (byte)0xFF};
						try {
							c.getDev().sendFeatureReport(report);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							this.log.error("INVALID REPORT");
							e.printStackTrace();
						}
						break;
					//print out what button is being pressed
					default:
						this.log.debug("Button '"+i+"' is pushed.");
						break;
				}
			}
			//say that the button is released
			else if(!buttons[i] && this.lastButton[joystickNumber-1][i])
			{
				this.log.debug("Button '"+i+"' released.");
			}
			//set the last button state to the current state
			this.lastButton[joystickNumber-1][i] = buttons[i];
		}
	}
	
	public void handleUpAndDown(int joystickNumber)
	{
		
	}
	
	//handle the depth change
	public void checkDepth(int joystickNumber)
	{
		//check if joystick is functioning properly
		if(!this.checkJoyStick(joystickNumber))
		{
			return;
		}
		
		//if lasts arent initialized initilize them
		if(this.lastButton == null)
		{
			initLastArrays(false, false, true, joystickNumber);
		}
		
		//get current button states
		boolean[] buttons = this.joysticks[joystickNumber-1].getButtons();
		//get only the buttons we need
		boolean sink = buttons[4];
		boolean rise = buttons[5];
		boolean fast = buttons[0];
		
		//get the current depth
		int newDepth = this.depth;
		
		//if the sink button is pressed and the fast button is pressed or
		//if the sink button is pressed and last time it wasnt
		//increment the depth by 1
		if(sink && (fast || !this.lastButton[joystickNumber-1][4]))
		{
			newDepth += 1;
		}
		//if the rise button is pressed and the fast button is pressed or
		//if the rise button is pressed and last time it wasnt
		//decrement the depth by 1
		if(rise && (fast || !this.lastButton[joystickNumber-1][5]))
		{
			newDepth -= 1f;
		}
		
		//depth cannot be less than 0
		if(newDepth < 0)
		{
			newDepth = 0;
		}
		
		//track last button states
		this.lastButton[joystickNumber-1][4] = sink;
		this.lastButton[joystickNumber-1][5] = rise;
		this.lastButton[joystickNumber-1][0] = fast;
		
		//if the depth is different update the robot and the view
		if(newDepth != this.depth)
		{
			this.depth = (int) newDepth;
			this.stats.setDepth(this.depth);
		}
		
	}
	
	//handles changing the camera and forward direction
	public void handleCameraChange(int joystickNumber, int switcherNumber)
	{
		if(!this.checkJoyStick(joystickNumber))
		{
			return;
		}
		
		Controller j = this.joysticks[joystickNumber-1];
		
		int camera = j.getDPad();
		switch(camera)
		{
			case 0:
				//hasn't changed
				break;
			case 1:
				//forward
				this.robot.setCamera(switcherNumber, 0, 1, 0);
				//this.setThrusterConfig(1, 2, 3, 4);
				break;
			case 3:
				//right
				this.robot.setCamera(switcherNumber,0,1,1);
				//this.setThrusterConfig(2, 4, 1, 3);
				break;
			case 5:
				//backward
				this.robot.setCamera(switcherNumber, 1, 1, 1);
				//this.setThrusterConfig(4, 3, 2, 1);
				break;
			case 7:
				//left
				this.robot.setCamera(switcherNumber, 1, 1, 0);
				//this.setThrusterConfig(3, 1, 4, 2);
				break;
			default:
				//stopped
				break;
		}
		
	}
	
	public void setThrusterConfig(int a, int b, int c, int d)
	{
		//store old velocities
		int oldV1 = this.stats.getThruster(thrusterConfig[0]).getVelocity();
		int oldV2 = this.stats.getThruster(thrusterConfig[1]).getVelocity();
		int oldV3 = this.stats.getThruster(thrusterConfig[2]).getVelocity();
		int oldV4 = this.stats.getThruster(thrusterConfig[3]).getVelocity();
		
		//set new thruster config
		this.thrusterConfig[0] = a;
		this.thrusterConfig[1] = b;
		this.thrusterConfig[2] = c;
		this.thrusterConfig[3] = d;
		
		this.stats.getThruster(a).setLocation("Front Left");
		this.stats.getThruster(b).setLocation("Front Right");
		this.stats.getThruster(c).setLocation("Back Left");
		this.stats.getThruster(d).setLocation("Back Right");
		
		this.stats.getThruster(a).setVelocity(oldV1);
		this.stats.getThruster(b).setVelocity(oldV2);
		this.stats.getThruster(c).setVelocity(oldV3);
		this.stats.getThruster(d).setVelocity(oldV4);
	}
	
	//handle misc buttons and knobs and such
	//this should only be used for debug or test purposes
	public void handleMiscs(int joystickNumber)
	{
		//check if the joystick is functional
		if(!this.checkJoyStick(joystickNumber))
		{
			return;
		}
		
		//get current misc states
		int[] miscs = this.joysticks[joystickNumber-1].getMisc();
		
		if(miscs == null)
		{
			return;
		}
		
		//loop through each misc object
		for(int i = 0; i < miscs.length; i++)
		{
			//if it has changed
			if(miscs[i] != this.lastMisc[joystickNumber-1][i])
			{
				switch(i)
				{
					//log that the object was changed
					default:
						this.log.debug("Misc '"+i+"' value = "+miscs[i]);
				}
			}
			//record current state
			this.lastMisc[joystickNumber-1][i] = miscs[i];
		}
		
	}
	
	//get the connection to the robot
	public Client getRobot()
	{
		return this.robot;
	}
	
	public double getDepth()
	{
		double density = 1030;
		double gravity = 9.8;
		double atmosphericPressure = 101325;
		double pressure = Double.parseDouble(this.robot.sendCode("u"));
		double depth = (pressure-atmosphericPressure) / ( density * gravity );
		this.disp.setDepth((int) pressure);
		return depth;
	}
	
	public double getTempeture()
	{
		return Double.parseDouble(this.robot.sendCode("i"));
	}
	
	public float getHeading()
	{
		try
		{
			float heading = Float.parseFloat(this.robot.sendCode("ch"));
			this.headingAvg[headingCount%this.HEADINGBUFFER] = (int)heading;
			headingCount++;
			if(headingCount < 0)
			{
				headingCount = this.HEADINGBUFFER;
			}
			if(headingCount >= this.HEADINGBUFFER)
			{
				int avg = 0;
				for(int i = 0; i < headingAvg.length; i++)
				{
					avg += headingAvg[i];
				}
				heading = avg/headingAvg.length;
			}
			this.stats.setHeading(heading);
			return heading;
		}
		catch(Exception e)
		{}
		return -1;
	}
	public float getPitch()
	{
		try
		{
			float pitch = Float.parseFloat(this.robot.sendCode("cr"));
			this.stats.setPitch(pitch);
			return pitch;
		}
		catch(Exception e)
		{}
		return -1;
		}
	public float getRoll()
	{
		try
		{
			float roll = Float.parseFloat(this.robot.sendCode("cp"));
			this.stats.setRoll(roll);
			this.disp.setAngle((int)roll);
			return roll;
		}
		catch(Exception e)
		{}
		return -1;
	}
	
	public void handleClaw(int joystickNumber)
	{
		if(!this.checkJoyStick(joystickNumber))
		{
			return;
		}

		int upDown = this.joysticks[joystickNumber-1].getJoystick1Y();
		int squeeze = this.joysticks[joystickNumber-1].getMisc()[0];
		int rotate = this.joysticks[joystickNumber-1].getJoystick2X();
		
		boolean udMult = this.joysticks[joystickNumber-1].getButtons()[9];
		boolean rMult = this.joysticks[joystickNumber-1].getButtons()[8];
		
		boolean a = this.joysticks[joystickNumber-1].getButtons()[0];
		boolean y = this.joysticks[joystickNumber-1].getButtons()[3];
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
		
		upDown += -128;
		rotate += -128;
		
		//We multiple the y axis by -1 just to get a more 
		//familiar system.  i.e (forward is positive backward is negative)
		upDown *= -1;
		
		//Check thresholds
		rotate = this.checkThreshold(rotate, joystickNumber);
		upDown = this.checkThreshold(upDown, joystickNumber);
		squeeze = this.checkThreshold(squeeze, joystickNumber);
		
		if(a)
		{
			this.robot.actuateStepper(0, 10, 0);
		}
		if(y)
		{
			this.robot.actuateStepper(0, 10, 1);
		}
		
		
		//check if there was a change
		if(this.lastJoystick[joystickNumber-1] != null && (rotate != this.lastJoystick[joystickNumber-1][0] || upDown != this.lastJoystick[joystickNumber-1][1] || squeeze != this.lastJoystick[joystickNumber-1][2]))
		{	
			//remember current values
			this.lastJoystick[joystickNumber-1][0] = rotate;
			this.lastJoystick[joystickNumber-1][1] = upDown;
			this.lastJoystick[joystickNumber-1][2] = squeeze;
			
			//Only change if either coordinate is out side of the threshold
			if(rotate != 0 && Math.abs(rotate) > 100)
			{
				int dir = 1;
				if(rotate < 0)
				{
					dir = 0;
				}
				this.robot.actuateStepper(1, 10, dir);
			}
			if(squeeze != 0 && Math.abs(squeeze) > 100)
			{
				int dir = 1;
				if(squeeze < 0)
				{
					dir = 0;
				}
				this.robot.actuateStepper(2, 10, dir);
			}
			if(upDown != 0 && Math.abs(upDown) > 100)
			{
				int dir = 1;
				if(upDown < 0)
				{
					dir = 0;
				}
				this.robot.actuateStepper(0, 10, dir);
			}
		}		
	}
	
	//main function
	public static void main(String[] args)
	{
		String title = "WIT-ROV"; 		//title for UI
		
		//Creates a new MainFrame object and passes the 
		//IP Address of the Arduino this is currently hardcoded in
		//the arduino.cpp file.  Also passes the title for the UI.
		MainFrame m = new MainFrame(title, 2);
	
		//Create connection to arduino
		m.initConnection();
		
		//Displays the UI
		m.setVisible(true);
		
		//Sets the close operation to kill the connection to the arduino
		//when the UI closes
		m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Maximize window
		m.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		while(true)
		{
			try
			{
				m.handleMainMovement(1);
				m.handleClaw(2);
				m.handleCameraChange(1,1);
				m.handleCameraChange(2,2);
				m.getHeading();
				m.getPitch();
				m.getRoll();
				m.repaint();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}	
	}

}
