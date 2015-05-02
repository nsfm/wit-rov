package com.witrov.joystick;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

public abstract class Controller extends Thread implements ControllerInterface, Runnable{
	
	public static final int BUFSIZE = 2048;
	public static final long READ_UPDATE_DELAY_MS = 10L;
	
	private static Controller[] currentDev = null;
	private static ArrayList<HIDDeviceInfo> devices = new ArrayList<HIDDeviceInfo>();
	//List of supported devices
	//To create a new supported device add the product name here and create a class
	//For that device that extends controller and use the run function to parse the data coming
	//in from the device to get button presses and joystick movements.  Refer to LogitechJoystick and XboxController
	//To get a print of all data form the device use the commented out for loop in this.run
	//Also need to add a case in ControllerPanel class when the device is selected from the dropdown
	
	public static String[] supportedProducts = new String[]{ //this is a list of the supported devices
		//"Logitech Extreme 3D",
		"xbox 360"
	};
	
	
	//Loads in proper native library
    static
    {    
    	try
    	{
    		String winBaseFile = System.getProperty("user.home")+"\\AppData\\Roaming\\WIT_ROV\\lib\\";
	    	if(System.getProperty("os.name").toLowerCase().contains("windows"))
	    	{
	    		if(System.getProperty("os.arch").contains("64"))
	    		{
	    			System.load(winBaseFile + "hidapijni64.dll");
	    		}
	    		else
	    		{
	    			System.load(winBaseFile + "hidapijni32.dll");
	    		}
	    	}
	    	else if(System.getProperty("os.name").toLowerCase().contains("linux") || System.getProperty("os.name").toLowerCase().contains("mac"))
	    	{
	    		if(System.getProperty("os.arch").contains("64"))
	    		{
	    			System.loadLibrary("libhidapijni64");
	    		}
	    		else
	    		{
	    			System.loadLibrary("libhidapijni32");
	    		}
	    	}
	    	else
	    	{
	    		JOptionPane.showMessageDialog(null, "This operating System is not Supported: "+System.getProperty("os.name"), "Unsupported Operating System", JOptionPane.ERROR);
	    		System.exit(0);
	    	}
    	}
    	catch(Exception e)
    	{
    		JOptionPane.showMessageDialog(null, "An Unkown Error Occured: "+ e.getMessage(), "Unkown Error", JOptionPane.ERROR);
    		System.exit(0);
    	}
    }
        
    /**
     * Static function to find the list HID devices
     * attached to the system.
     */
    public static ArrayList<HIDDeviceInfo> getDevices()
    {
    	return devices;
    }
    /**
     * Static function to find the list HID devices
     * attached to the system.
     */
    public static String showDevices(boolean allInfo, boolean allDevices)
    {
    	String list = "";
    	for(HIDDeviceInfo h : devices)
    	{
    		list += "<br>========"+h.getProduct_string()+"=========";
    		list += "<ul><li>PRODUCT ID: "+h.getProduct_id()+"</li>";
    		list += "<li>VENDOR ID: "+h.getVendor_id()+"</li>";
    		if(allInfo)
    		{
    			list += "<li>INTERFACE NUMBER: "+h.getInterface_number()+"</li>";
    			list += "<li>MANUFACTURE NAME: "+h.getManufacturer_string()+"</li>";
    			list += "<li>RELEASE NUMBER: "+h.getRelease_number()+"</li>";
    			list += "<li>SERIAL NUMBER: "+h.getSerial_number()+"</li>";
    			list += "<li>USAGE: "+h.getUsage()+"</li>";
    			list += "<li>USAGE PAGE: "+h.getUsage_page()+"</li>";
    		}
    		list += "</ul>";
    		list += "=================================<br>";
    	}
        
    	System.gc();
        
    	return list;
    }
    public static boolean isSupported(String productName)
    {
    	if(productName != null)
    	{
	    	for(int i = 0; i < supportedProducts.length; i++)
	    	{
	    		if(productName.toLowerCase().contains(supportedProducts[i]))
	    		{
	    			return true;
	    		}
	    	}
    	}
    	return false;
    }
    
    /*
     * buttonSet - value from 0-255 for data of pressed buttons
     * startIndex - number of largest button in set
     * endIndex - number of Smallest button in set
     * buttons - array of button states true is pressed false is not pressed
     */
    public void processButtonSet(int buttonSet, int startIndex, int endIndex, int startValue, boolean[] buttons)
    {
    	if(buttonSet > 0)
    	{
		    while(buttonSet > 0)
		    {
		    	if(buttonSet - startValue >= 0) //check if this button is pressed
		    	{
		    		buttons[startIndex] = true; //mark the button as pressed
		    		buttonSet -= startValue; //remove the button value from the buttonSet
		    	}
		    	else
		    	{
		    		buttons[startIndex] = false; //button wasn't pressed mark it as not pressed
		    	}
		    	startValue /= 2; //decrease to next button value
		    	startIndex--; //decrease to next button
		    }
		    for(int i = startIndex; i >= endIndex; i--)  //set buttons that weren't pressed to not pressed
		    {
		    	buttons[i] = false;
		    }
    	}
    	else
    	{
    		for(int i = startIndex; i >= endIndex; i--) // no buttons were pressed update all buttons to not pressed
    		{
    			buttons[i] = false;
    		}
    	}
    }    
        /*
	    for(int i=0; i<n; i++)
	    {
	        int v = buf[i];
	        if (v<0) v = v+256;
	        System.err.print(v + " ");
	    }
	    System.err.println("");
	    */

    public static void setCurrentDevice(Controller c, int joystickNumber)
    {
    	if(currentDev[joystickNumber-1] != null)
    	{
    		currentDev[joystickNumber-1].kill();
    	}
    	currentDev[joystickNumber-1] = c;
    }
    public static Controller getCurrentDevice(int joystickNumber)
    {
    	return currentDev[joystickNumber-1];
    }
    public static Controller[] getCurrentDevices()
    {
    	return currentDev;
    }
    public static void initDevices(int joystickNumber)
    {
    	currentDev = new Controller[joystickNumber];
    }
    
    public static void updateDevices()
    {
	  	//start update devices thread
    	Thread updateDevices = new Thread(new Runnable(){
    		@Override
    		public void run() {
    			while(true)
    			{
    		        try
    		        {
    		            HIDManager manager = HIDManager.getInstance();
    		            HIDDeviceInfo[] allDevs = manager.listDevices();
    		            ArrayList<HIDDeviceInfo> currentDevs = Controller.getDevices();
    		            if(allDevs != null)
    		            {
    		            	//add new devices
    			            for(int i=0;i<allDevs.length;i++)
    			            {
    			            	if(Controller.isSupported(allDevs[i].getProduct_string()))
    			            	{
    			            		boolean found = false;
    			            		
    			            		for(int j = 0; j < currentDevs.size(); j++)
    			            		{
    			            			if(currentDevs.get(j) == null)
    			            			{
    			            				break;
    			            			}
    			            			if(allDevs[i].getProduct_id() == currentDevs.get(j).getProduct_id() && allDevs[i].getVendor_id() == currentDevs.get(j).getVendor_id())
    			            			{
    			            				found = true;
    			            			}
    			            		}
    			            		if(!found)
    			            		{
    			            			currentDevs.add(allDevs[i]);
    			            		}
    			            	}
    			            }
    			            
    			            //remove unplugged devices
    			            for(int i=0;i<currentDevs.size();i++)
    			            {
    			            	boolean found = false;
    			            	for(int j = 0; j < allDevs.length; j++)
    			            	{
    			            		if((allDevs[j].getProduct_id() == currentDevs.get(i).getProduct_id() && allDevs[j].getVendor_id() == currentDevs.get(i).getVendor_id()))
    			            		{
    			            			found = true;
    			            		}
    			            	}
    			            	if(!found)
    			            	{
    			            		currentDevs.remove(i);
    			            	}
    			            }
    		            }
    		            else
    		            {
    		            	//no devices clear array
    		            	currentDevs.clear();
    		            }
    		            System.gc();
    		            try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    		        }
    		        catch(IOException e)
    		        {
    		            System.out.println(e.getMessage());
    		            e.printStackTrace();
    		        }
    			}
    			
    		}
    	});
    	updateDevices.start();
    }
}
