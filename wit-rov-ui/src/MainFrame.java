import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class MainFrame extends JFrame implements ActionListener{
	
	private Client robot;			//handles main communcation with robot
	private JButton execute; 		//buttons to handle neccessary events
	private JPanel buttons;			//panels to hold and format the view
	private JLabel cLabel;			//labels to show what is what
	private JTextField command;		//input fields to get input from the user
	
	/*
	 * Constructor
	 * Creates the Client object and establishes
	 * a connection with the arduino on the given
	 * IP Address and port. Also sets the title of the UI.
	 */
	public MainFrame(String ip, String title, int port) throws UnknownHostException, IOException
	{
			//Handles all communication to the Arduino on 
			//The given IP Address
			robot = new Client(ip, port);
			this.setTitle(title);
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
		
		//Initializes the Execute button 
		//and sets the Click listener to this class
		execute = new JButton("Execute Command");
		//method is this.actionPerformed
		execute.addActionListener(this);
		
		//Initializes a label for the text field
		cLabel = new JLabel("Command");
		
		//Initializes a text field for command input
		command = new JTextField(5);
		
		//adds the execute command button, label, and textfield
		//to a panel to be displayed and formatted
		buttons.add(cLabel);
		buttons.add(command);
		buttons.add(execute);
	
		//adds the panels to the screen to display components
		this.add(buttons);
		
		//Packs the frame meaning it shrinks the window
		//to the smallest size needed for the components it holds
		this.pack();
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
			if(!robot.sendCode(command.getText()))
			{
				command.setText("There was an error executing the opcode");
			}
		}
		
	}
	
	
	public static void main(String[] args)
	{
		String ip = "192.168.0.2"; 		//ip of arduino
		String title = "WIT-ROV"; 		//title for UI
		int port = 23; 					//port of arduino
		
		try
		{
			//Creates a new MainFrame object and passes the 
			//IP Address of the Arduino this is currently hardcoded in
			//the arduino.cpp file.  Also passes the title for the UI.
			MainFrame m = new MainFrame(ip, title, port);
			
			//Displays the UI
			m.setVisible(true);
			
			//Sets the close operation to kill the connection to the arduino
			//when the UI closes
			m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
		} catch (Exception e){
			JOptionPane.showMessageDialog(null, "There was an error estabilishing a connection to "+ip, title, JOptionPane.ERROR_MESSAGE);	
		}		
	}

}
