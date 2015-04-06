package com.witrov.joystick;

import java.io.IOException;
import java.util.ArrayList;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

public abstract class Controller extends Thread implements ControllerInterface{
	
	public static final int BUFSIZE = 2048;
	public static final long READ_UPDATE_DELAY_MS = 50L;
	
	public static String[] supportedProducts = new String[]{ //this is a list of the supported devices
		"Logitech Extreme 3D",
		"XBOX 360"
	};
	
    static
    {
    	if(System.getProperty("os.name").toLowerCase().contains("windows"))
    	{
    		if(System.getProperty("os.arch").contains("64"))
    		{
    			System.loadLibrary("hidapi-jni-64");
    		}
    		else
    		{
    			System.loadLibrary("hidapi-jni-32");
    		}
    	}
    	else if(System.getProperty("os.name").toLowerCase().contains("linux") || System.getProperty("os.name").toLowerCase().contains("mac"))
    	{
    		if(System.getProperty("os.arch").contains("64"))
    		{
    			System.loadLibrary("libhidapi-jni-64");
    		}
    		else
    		{
    			System.loadLibrary("libhidapi-jni-32");
    		}
    	}
    	else
    	{
    		System.err.println("This Operating System is not Supported: "+ System.getProperty("os.name"));
    	}
        
    }
        
    /**
     * Static function to find the list HID devices
     * attached to the system.
     */
    public static ArrayList<HIDDeviceInfo> getDevices(boolean allDevices)
    {
    	ArrayList<HIDDeviceInfo> list = new ArrayList<HIDDeviceInfo>();
        try
        {
            HIDManager manager = HIDManager.getInstance();
            HIDDeviceInfo[] allDevs = manager.listDevices();
            
            for(int i=0;i<allDevs.length;i++)
            {
            	if(isSupported(allDevs[i].getProduct_string()) || allDevices)
            	{
            		list.add(allDevs[i]);
            	}
            }
            
            System.gc();
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    /**
     * Static function to find the list HID devices
     * attached to the system.
     */
    public static String showDevices(boolean allInfo, boolean allDevices)
    {
    	String list = "";
       
    	ArrayList<HIDDeviceInfo> devs = getDevices(allDevices);
    	
    	for(HIDDeviceInfo h : devs)
    	{
    		list += "========"+h.getProduct_string()+"=========";
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
    		list += "=================================<br><br>";
    	}
        
    	System.gc();
        
    	return list;
    }
    private static boolean isSupported(String productName)
    {
    	if(productName != null)
    	{
	    	for(int i = 0; i < supportedProducts.length; i++)
	    	{
	    		if(productName.contains(supportedProducts[i]))
	    		{
	    			return true;
	    		}
	    	}
    	}
    	return false;
    }
    
    public void processButtonSet(int buttonSet, int startIndex, int endIndex, int startValue, boolean[] buttons)
    {
    	if(buttonSet > 0)
    	{
		    while(buttonSet > 0)
		    {
		    	if(buttonSet - startValue >= 0)
		    	{
		    		buttons[startIndex] = true;
		    		buttonSet -= startValue;
		    	}
		    	else
		    	{
		    		buttons[startIndex] = false;
		    	}
		    	startValue /= 2;
		    	startIndex--;
		    }
    	}
    	else
    	{
    		for(int i = startIndex; i >= endIndex; i--)
    		{
    			buttons[i] = false;
    		}
    	}
        
    }    
    public void run()
    {

	        /*
	    for(int i=0; i<n; i++)
	    {
	        int v = buf[i];
	        if (v<0) v = v+256;
	        System.err.print(v + " ");
	    }
	    System.err.println("");
	    */
    	throw new NotImplementedException();
    }
}
