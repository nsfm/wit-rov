package com.witrov.joystick;

import java.io.IOException;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

public class LogitechJoystick extends Controller{
	
    //Info for Logitech Extreme 3D JoyStick
    private int vendorId;
    private int productId;
	
	private int x;				//stores the value for the joysticks x position
	private int y;				//stores the value for the joysticks y position
	
	//This stores the values for the joysticks button states
	//true is pressed false is not pressed
	private boolean[] buttons;
	
	//This stores the values of the strafe option as well as the 
	//turn knob
	private int[] misc;
	
	private int analog;			//stores the direct of the joysticks analog stick
	
	private int numberOfButtons = 12;
	private int numberOfMisc = 2;
	
	private boolean isRunning = true;
	
	
	public LogitechJoystick(int vendorId, int productId)
	{
		this.vendorId = vendorId;
		this.productId = productId;
		buttons = new boolean[numberOfButtons];
		for(int i = 0; i < numberOfButtons; i++)
		{
			buttons[i] = false;
		}
		
		misc = new int[numberOfMisc];
		for(int i = 0; i < numberOfMisc; i++)
		{
			misc[i] = 0;
		}
		Controller.setCurrentDevice(this);
		
	}        
    
    /**
     * Will read the device and update the
     * readings to be able to be grabbed at any second
     */
	@Override
    public void run()
    {
        HIDDevice dev;
    	HIDManager hid_mgr;
		try {
			hid_mgr = HIDManager.getInstance();
	        dev = hid_mgr.openById(vendorId, productId, null);
	        try
	        {
	            byte[] buf = new byte[BUFSIZE];
	            if(dev == null)
	            {
	            	System.out.println("Dev was null");
	            	isRunning = false;
	            }
	            else
	            {
	            	dev.disableBlocking();
	            	dev.enableBlocking();
	            }
	            
	            while(isRunning)
	            {
	            	//read the buffer
	            	if(dev == null)
	            	{
	            		isRunning = false;
	            		break;
	            	}
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
	                this.misc[0] = this.checkDeviceValue(buf[3]);
	                this.misc[1] = this.checkDeviceValue(buf[5]);
	                
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
	                //			0
	                //    7           1
	                //6 		8  	       2
	                //    5           3
	                //			4
	                //
	                
	                //Get the two sets of button data for the joy stick
	                
	                this.analog = Integer.parseInt(analogDirection);
	                this.analog += 1;
	                if(analog == 9)
	                {
	                	analog = 0;
	                }
	                
                    int buttonSetOne = this.checkDeviceValue(buf[4]);
                    int buttonSetTwo = this.checkDeviceValue(buf[6]);

                    super.processButtonSet(buttonSetTwo, 11, 8,  8, buttons);
                    super.processButtonSet(buttonSetOne, 7, 0, 128, buttons);
		                
	                                       
                    
            	    for(int i=0; i<n; i++)
            	    {
            	        int v = buf[i];
            	        System.err.print(v + " ");
            	    }
            	    System.err.println("");
            	    
                    
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
	        	System.out.println("HERE");
	        	e.printStackTrace();
	        	isRunning = false;
	        }
	        
    		dev.close();
	        hid_mgr.release();    
	        System.gc();
	        
		} catch (IOException e1) {
			isRunning = false;
			e1.printStackTrace();
		}
    }
    
    private int checkDeviceValue(int v)
    {
    	if (v < 0)
    	{
    		v += 256;
    	}
    	return v;
    }    
    
    public int getVendorId()
    {
    	return this.vendorId;
    }
    public void setVendorId(int venderId)
    {
    	this.vendorId = venderId;
    }
    public int getProductId()
    {
    	return this.productId;
    }
    public void setProductId(int productId)
    {
    	this.productId = productId;
    }
    
    public void kill()
    {
    	isRunning = false;
    }
    public boolean isRunning()
    {
    	return isRunning;
    }

	@Override
	public int getJoystick1X() {
		return x;
	}

	@Override
	public int getJoystick1Y() {
		return y;
	}

	////////////////////////////////////////////////////////////
	//return -1 for joystick2 because there is no    	////////
	// joystick 2on the Logitech Extreme 3D Joystick	////////
	@Override											////////
	public int getJoystick2X() {						////////
		return -1;										////////
	}													////////
														////////
	@Override											////////
	public int getJoystick2Y() {						////////
		return -1;										////////
	}													////////
	////////////////////////////////////////////////////////////
	@Override
	public boolean[] getButtons() {
		return buttons;
	}

	@Override
	public int getDPad() {
		return analog;
	}

	@Override
	public int[] getMisc() {
		return misc;
	}
}
