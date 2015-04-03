package com.witrov.joystick;

import java.io.IOException;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

public class Joystick extends Controller{
	
    //Info for Logitech Extreme 3D JoyStick
    private int vendorId;
    private int productId;
	
	private int x;				//stores the value for the joysticks x position
	private int y;				//stores the value for the joysticks y position
	private int strafe;			//stores the value for the joysticks x strafe position
	
	//These store the values for the joysticks button states
	//true is pressed false is not pressed
	private boolean trigger;	
	private boolean button2;	
	private boolean button5;
	private boolean button3;
	private boolean button6;
	private boolean button4;
	private boolean button7;
	private boolean button8;
	private boolean button9;
	private boolean button10;
	private boolean button11;
	private boolean button12;
	
	
	private int knob;			//stores the value for the joysticks knob position
	private int analog;			//stores the direct of the joysticks analog stick
								//these are in degrees of a circle so 90 is straight up
								// 270 is straight down and so on
	
	private boolean isRunning = true;
	
	public Joystick(int vendorId, int productId)
	{
		this.vendorId = vendorId;
		this.productId = productId;
	}        
    
    /**
     * Will read the device and update the
     * readings to be able to be grabbed at any second
     */
    public void run()
    {
        HIDDevice dev;
        try
        {
        	HIDManager hid_mgr = HIDManager.getInstance();
            dev = hid_mgr.openById(vendorId, productId, null);
            try
            {
                byte[] buf = new byte[BUFSIZE];
                dev.enableBlocking();
                
                
                while(isRunning)
                {
                	//read the buffer
                    int n = dev.read(buf);
                	//There are six different indexes for the joystick
                	//0: x direction
                	//1: y direction
                	//2: first number does analog stick and second is the y direction of the joystick
                	//3: strafing
                	//5: knob
                	//4,6: button inputs
                	//  button2: 4-2
                	//	button3: 4-4
                	// 	button4: 4-8
                	//	button5: 4-16
                	//  button6: 4-32
                	//  button7: 4-64
                	//  button8: 4-128
                	//  button9: 6-1
                	//  button10: 6-2
                	//  button11: 6-4
                	//  button12: 6-8
                    
                    this.x = this.checkDeviceValue(buf[0]);
                    this.y = this.checkDeviceValue(buf[1]);
                    this.strafe = this.checkDeviceValue(buf[3]);
                    this.knob = this.checkDeviceValue(buf[5]);
                    
                    //this is a weird value it gives you the value of 
                    //the analogDirection as well as the y joystick direction
                    //in one value.  So instead of using an integer
                    //we are going to convert it to a hex string and use only the first digit.
                    //this gives us the direction we are pointing the analog stick
                    int analogValue = this.checkDeviceValue(buf[2]);
                    String analogDirection = Integer.toHexString(analogValue);
                    if(analogValue < 16)
                    {
                    	analogDirection = "0"+analogDirection;
                    }
                    analogDirection = analogDirection.substring(0,1);
                   
                    //parse analogDirection for angle
                    //			0-90
                    //    7-135	      1-45
                    //6-180		8-0  	   2-360
                    //    5-225       3-315
                    //			4-270
                    //
                    
                    if(analogDirection.equals("0"))
                    {
                    	this.analog = 90;
                    }
                    else if(analogDirection.equals("1"))
                    {
                    	this.analog = 45;
                    }
                    else if(analogDirection.equals("2"))
                    {
                    	this.analog = 360;
                    }
                    else if(analogDirection.equals("3"))
                    {
                    	this.analog = 315;
                    }
                    else if(analogDirection.equals("4"))
                    {
                    	this.analog = 270;
                    }
                    else if(analogDirection.equals("5"))
                    {
                    	this.analog = 225;
                    }
                    else if(analogDirection.equals("6"))
                    {
                    	this.analog = 180;
                    }
                    else if(analogDirection.equals("7"))
                    {
                    	this.analog = 135;
                    }
                    else
                    {
                    	this.analog = 0;
                    }
                    
                    
                    //Get the two sets of button data for the joy stick
                    int buttonSetOne = this.checkDeviceValue(buf[4]);
                    int buttonSetTwo = this.checkDeviceValue(buf[6]);
                    
                    
                    if(buttonSetTwo-8 >= 0)
                    {
                    	this.button12 = true;
                    	buttonSetTwo -= 8;
                    }
                    else
                    {
                    	this.button12 = false;
                    }
                    
                    if(buttonSetTwo-4 >= 0)
                    {
                    	this.button11 = true;
                    	buttonSetTwo -= 4;
                    }
                    else
                    {
                    	this.button11 = false;
                    }
                    
                    if(buttonSetTwo-2 >= 0)
                    {
                    	this.button10 = true;
                    	buttonSetTwo -= 2;
                    }
                    else
                    {
                    	this.button10 = false;
                    }
                    
                    if(buttonSetTwo-1 >= 0)
                    {
                    	this.button9 = true;
                    	buttonSetTwo -= 1;
                    }
                    else{
                    	this.button9 = false;
                    }
                    
                    if(buttonSetOne-128 >= 0)
                    {
                    	this.button8 = true;
                    	buttonSetOne -= 128;
                    }
                    else
                    {
                    	this.button8 = false;
                    }
                    
                    if(buttonSetOne-64 >= 0)
                    {
                    	this.button7 = true;
                    	buttonSetOne -= 64;
                    }
                    else
                    {
                    	this.button7 = false;
                    }
                    
                    if(buttonSetOne-32 >= 0)
                    {
                    	this.button6 = true;
                    	buttonSetOne -= 32;
                    }
                    else
                    {
                    	this.button6 = false;
                    }
                    
                    if(buttonSetOne-16 >= 0)
                    {
                    	this.button5 = true;
                    	buttonSetOne -= 16;
                    }
                    else
                    {
                    	this.button5 = false;
                    }
                    
                    if(buttonSetOne-8 >= 0)
                    {
                    	this.button4 = true;
                    	buttonSetOne -= 8;
                    }
                    else
                    {
                    	this.button4 = false;
                    }
                    
                    if(buttonSetOne-4 >= 0)
                    {
                    	this.button3 = true;
                    	buttonSetOne -= 4;
                    }
                    else
                    {
                    	this.button3 = false;
                    }
                    
                    if(buttonSetOne-2 >= 0)
                    {
                    	this.button2 = true;
                    	buttonSetOne -= 2;
                    }
                    else
                    {
                    	this.button2 = false;
                    }
                    
                    if(buttonSetOne-1 >= 0)
                    {
                    	this.trigger = true;
                    	buttonSetOne -= 1;
                    }
                    else
                    {
                    	this.trigger = false;
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
                    try
                    {
                        Thread.sleep(READ_UPDATE_DELAY_MS);
                    } catch(InterruptedException e)
                    {
                        //Ignore
                        e.printStackTrace();
                    }
                }
            } catch(Exception e)
            {
            	e.printStackTrace();
            	isRunning = false;
            }finally
            {
            	if(dev != null)
            	{
            		dev.close();
            	}
                hid_mgr.release();    
                System.gc();
                isRunning = false;
            }
            
            
        } 
        catch(IOException e)
        {
        	isRunning = false;
            e.printStackTrace();
        }
    }
    
    private int checkDeviceValue(int v)
    {
    	if (v<0) v = v+256;
    	return v;
    }
    
    public int getX()
    {
    	return this.x;
    }
    public int getY()
    {
    	return this.y;
    }
    public int getStrafe()
    {
    	return this.strafe;
    }
    public int getKnob()
    {
    	return this.knob;
    }
    public boolean getTrigger()
    {
    	return this.trigger;
    }
    public boolean getButton2()
    {
    	return this.button2;
    }
    public boolean getButton3()
    {
    	return this.button3;
    }
    public boolean getButton4()
    {
    	return this.button4;
    }
    public boolean getButton5()
    {
    	return this.button5;
    }
    public boolean getButton6()
    {
    	return this.button6;
    }
    public boolean getButton7()
    {
    	return this.button7;
    }
    public boolean getButton8()
    {
    	return this.button8;
    }
    public boolean getButton9()
    {
    	return this.button9;
    }
    public boolean getButton10()
    {
    	return this.button10;
    }
    public boolean getButton11()
    {
    	return this.button11;
    }
    public boolean getButton12()
    {
    	return this.button12;
    }
    public int getAnalog()
    {
    	return this.analog;
    }
    public int getVendorId()
    {
    	return this.vendorId;
    }
    public void setVendorId(int id)
    {
    	this.vendorId = id;
    }
    public int getProductId()
    {
    	return this.productId;
    }
    public void setProductId(int id)
    {
    	this.productId = id;
    }
    
    public void kill()
    {
    	isRunning = false;
    }
    public boolean isRunning()
    {
    	return isRunning;
    }
}
