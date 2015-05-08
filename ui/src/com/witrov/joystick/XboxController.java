package com.witrov.joystick;

import java.io.IOException;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

public class XboxController extends Controller {
	 //Info for Xbox 360 Controller
    private int vendorId;
    private int productId;
	
	private int x1;				//stores the value for the joysticks x position
	private int y1;				//stores the value for the joysticks y position
	private int x2;				//stores the value for the joysticks x position
	private int y2;				//stores the value for the joysticks y position
	
	private HIDDevice dev;
	//This stores the values for the joysticks button states
	//true is pressed false is not pressed
	private boolean[] buttons;
	
	//This stores the values of the strafe option as well as the 
	//turn knob
	private int[] misc;
	
	private int dpad;			//stores the value of the dpad
	
	private int numberOfButtons = 8;
	private int numberOfMisc = 6;
	
	private boolean isRunning = true;
	
	private int joystickNumber;
	
	public XboxController(int vendorId, int productId, int joystickNumber)
	{
		this.vendorId = vendorId;
		this.productId = productId;
		this.joystickNumber = joystickNumber;
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
		Controller.setCurrentDevice(this, this.joystickNumber);
	}        
    
    /**
     * Will read the device and update the
     * readings to be able to be grabbed at any second
     */
	@Override
    public void run()
    {
    	HIDManager hid_mgr;
		try {
			hid_mgr = HIDManager.getInstance();
	        dev = hid_mgr.openById(vendorId, productId, null);
	        try
	        {
	            byte[] buf = new byte[BUFSIZE];
	            dev.enableBlocking();	            
	            
	            while(isRunning)
	            {
	            	//read the buffer
	            	if(dev == null)
	            	{
	            		isRunning = false;
	            		break;
	            	}
	                int n = dev.read(buf);
	                
	                //both joysticks index 0,1,2,3,4,5,6,7
	                x1 = this.checkDeviceValue(buf[1]);
	                y1 = this.checkDeviceValue(buf[3]);
	                x2 = this.checkDeviceValue(buf[5]);
	                y2 = this.checkDeviceValue(buf[7]);
	                
	                
	                //index 10-a, b, x, y, start, select, rb, lb
	                int buttonSetOne = buf[10];
	                super.processButtonSet(buttonSetOne, 7, 0, 128, buttons);
	                
	                
	                this.misc[0] = 128 - this.checkDeviceValue(buf[9]);
	                //process dpad index 11
	                this.dpad = (this.checkDeviceValue(buf[11]) - 64) / 4;
	                
	                
//	                for(int i = 0; i < n; i++)
//	                {
//	                	int v = this.checkDeviceValue(buf[i]);
//	                	if(v < 10)
//	                	{
//	                		System.err.print("0");
//	                	}
//	                	if(v < 100)
//	                	{
//	                		System.err.print("0");
//	                	}
//	                	System.err.print(v+ " ");
//	                }
//	                System.err.println("");
	                
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
	        }
	        if(dev != null)
	        {
	        	dev.close();
	        }
            hid_mgr.release();    
            System.gc();

		} catch (IOException e1) {
			isRunning = false;
			e1.printStackTrace();
		}
		
		Controller.setCurrentDevice(null, this.joystickNumber);
    }
    
    private int checkDeviceValue(int v)
    {
    	if (v<0) v = v+256;
    	return v;
    }
	
	@Override
	public int getJoystick1X() {
		// TODO Auto-generated method stub
		return x1;
	}

	@Override
	public int getJoystick1Y() {
		// TODO Auto-generated method stub
		return y1;
	}

	@Override
	public int getJoystick2X() {
		// TODO Auto-generated method stub
		return x2;
	}

	@Override
	public int getJoystick2Y() {
		// TODO Auto-generated method stub
		return y2;
	}

	@Override
	public boolean[] getButtons() {
		// TODO Auto-generated method stub
		return buttons;
	}

	@Override
	public int getDPad() {
		// TODO Auto-generated method stub
		return dpad;
	}

	@Override
	public int[] getMisc() {
		// TODO Auto-generated method stub
		return misc;
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return isRunning;
	}

	@Override
	public void kill() {
		this.isRunning = false;
	}

	@Override
	public int getVendorId() {
		// TODO Auto-generated method stub
		return vendorId;
	}

	@Override
	public void setVendorId(int vendorId) {
		// TODO Auto-generated method stub
		this.vendorId = vendorId;
	}

	@Override
	public int getProductId() {
		// TODO Auto-generated method stub
		return productId;
	}

	@Override
	public void setProductId(int productId) {
		// TODO Auto-generated method stub
		this.productId = productId;
	}

	public HIDDevice getDev()
	{
		return this.dev;
	}

	public int getJoystickNumber()
	{
		return this.joystickNumber;
	}
}
