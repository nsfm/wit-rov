package com.witrov.main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {

	private Socket socket;			//the socket connection to the robot
	private BufferedReader in;		//the reader to read the responses from the robot
	private PrintWriter out;		//the writer to send data tot the robot
	private boolean debug;			//boolean to set whether to print debug data or not
	
	/*
	 * Creates connection to ip on given port and opens a line of 
	 * communication using the Buffered reader to read responses
	 * and the Printwriter to send commands
	 */
	public Client(String ip, int port) throws UnknownHostException, IOException
	{
		socket = new Socket(ip, port);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream());
	}
	
	/*
	 * returns true if the opcode was sent and executed successfully and false otherwise
	 * 
	 * Sends an op code to the robot and waits for a response once the response is given
	 * it parses the response looking for an '!' (success) or a '?' failure
	 */
	public boolean sendCode(String code)
	{
		//does some small verification to make sure that
		//that the opcode isn't to long to kill the buffer 
		//on the arduino side.  The buffer cna only handle 8 bit 
		//op codes 2 are taken by /n so we are left with 6
		if(code.length() > 6)
		{
			return false;
		}
		
		//debug to print code being sent
		print("Sending: " + code);
		
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
		
		//debug message
		print("~ " + message);
		
		//parses the message to see if we received a 
		//success response
		if(message.contains("!"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/*
	 * returns whether the socket is connected
	 */
	public boolean isConnected()
	{
		return this.socket.isConnected();
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
}
