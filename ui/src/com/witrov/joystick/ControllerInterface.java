package com.witrov.joystick;


public interface ControllerInterface {

	public int numberOfMisc = 0;
	public int numberOfButtons = 0;
	
	/**
	 * @var integer
	 * 
	 * Returns the x value for the first joystick
	 * or -1 if there isn't a joystick
	 */
    public int getJoystick1X();

	/**
	 * @var integer
	 * 
	 * Returns the y value for the first joystick
	 * or -1 if there isn't a joystick
	 */
    public int getJoystick1Y();
    
	/**
	 * @var integer
	 * 
	 * Returns the x value for the second joystick
	 * or -1 if there isn't a joystick
	 */
    public int getJoystick2X();
    
	/**
	 * @var integer
	 * 
	 * Returns the y value for the second joystick
	 * or -1 is there isn't a joystick
	 */
    public int getJoystick2Y();
    
	/**
	 * @var boolean[]
	 * 
	 * Returns a boolean array of the buttons on the device
	 * the array returns true if the button is clicked or false if 
	 * the button is not clicked.  Will return null if there are no buttons.
	 */
    public boolean[] getButtons();
    
    /**
     * @var integer
     * 
     * Returns the value of the directional pad or -1 if one 
     * does not exist
     */
	public int getDPad();
	
	
	/**
	 * @var integer
	 * 
	 * Returns an array of misc items such as pressure sensitive buttons
	 * and control knobs and the value they are at
	 */
	public int[] getMisc();
	
	
	public boolean isRunning();
	public void kill();
	
	public int getVendorId();
	public void setVendorId(int venderId);
	public int getProductId();
	public void setProductId(int productId);
	
	public void run();
}
