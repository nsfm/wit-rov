package com.witrov.main;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import com.witrov.config.ArduinoPinConfig;


public class Client {

	private Socket socket;			//the socket connection to the robot
	private BufferedReader in;		//the reader to read the responses from the robot
	private PrintWriter out;		//the writer to send data tot the robot
	private boolean debug;			//boolean to set whether to print debug data or not
	private MainFrame main;
	private static boolean connecting = false;
	
	/*
	 * Creates connection to ip on given port and opens a line of 
	 * communication using the Buffered reader to read responses
	 * and the Printwriter to send commands
	 */
	public Client(String ip, int port, MainFrame main)
	{
		
		final JOptionPane optionPane = new JOptionPane("Attempting Connection to "+ip+" on port "+port+"...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);

		final JDialog dialog = new JDialog();
		dialog.setTitle("Connecting...");
		dialog.setModal(true);

		dialog.setContentPane(optionPane);

		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		final JProgressBar progress = new JProgressBar();
		progress.setIndeterminate(true);
		progress.setString("Connecting...");
		progress.setStringPainted(true);
		
		dialog.add(progress);
		
		dialog.pack();
		
		dialog.setLocationRelativeTo(null);
		
		this.main = main;
		final MainFrame maint = main;
		final String ipt = ip;
		final int portt = port;	
		Thread checkClose = new Thread(new Runnable(){
			@Override
			public void run()
			{
				while(socket == null || !socket.isConnected())
				{
					if(!dialog.isValid() && Client.isConnecting())
					{
						System.exit(-1);
					}
					else if(!Client.isConnecting())
					{
						try {
							Client.connecting = true;
							socket = new Socket(ipt, portt);
							in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							out = new PrintWriter(socket.getOutputStream());
							maint.getLog().info("Connected to "+ipt);
							Client.connecting = false;
							dialog.dispose();
						} catch (IOException e) {
							Client.connecting = false;
							dialog.dispose();
							JOptionPane.showMessageDialog(null, "There was an error connecting to "+ipt+" on port "+portt+"", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		
		checkClose.start();
		dialog.setVisible(true);
	}
	
	/*
	 * returns true if the opcode was sent and executed successfully and false otherwise
	 * 
	 * Sends an op code to the robot and waits for a response once the response is given
	 * it parses the response looking for an '!' (success) or a '?' failure
	 */
	public String sendCode(String code)
	{
		if(!this.isConnected())
		{
			return null;
		}
			
		//does some small verification to make sure that
		//that the opcode isn't to long to kill the buffer 
		//on the arduino side.  The buffer cna only handle 8 bit 
		//op codes 2 are taken by /n so we are left with 6
		if(code.length() > 6)
		{
			return null;
		}
		
		
		//queues the code to be sent
		out.println(code);
		//sends the code to the arduino
		out.flush();
		//reads in the response from the arduino 
		String message = "";
		try {
			message = in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//parses the message to see if we received a 
		//success response
		if(message.contains("?"))
		{
			main.getLog().error("Could Not execute Command");
			return message;
		}
		else
		{
			main.getLog().info("Sent "+code);
			return null;
		}
	}
	
	/*
	 * returns whether the socket is connected
	 */
	public boolean isConnected()
	{
		if(this.socket != null)
		{
			return this.socket.isConnected();
		}
		return false;
	}
	
	/*
	 * creates and executes a pin set command
	 */
	public void setPinMode(ArduinoPinConfig pin)
	{
		this.sendCode("p"+pin.pinNumberToString()+pin.getPinMode());
	}
	
	/*
	 * Prints out a message based on the debug variable
	 */
	public void print(String message)
	{
		if(debug)
		{
			System.out.println(message);
		}
	}

	public void setThruster(ArduinoPinConfig pin) {
		
		this.sendCode("s"+pin.pinNumberToString()+""+pin.getValue());
		
	}
	
	public static boolean isConnecting()
	{
		return connecting;
	}
}
