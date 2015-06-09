package com.witrov.config;

public class ArduinoPinConfig {

	private int pinNumber;
	private int pinNumberTwo; //only user for stepper motor
	private int pinMode; //0 = INPUT 1 = OUTPUT 2 INPUT_PULLUP
	private int value;   //only used for thruster number
	
	public static int INPUT = 0;
	public static int OUTPUT = 1;
	public static int INPUT_PULLUP = 2;
	public static int THRUSTER = 3;
	public static int STEPPER = 4;
	public static String[] modes = new String[] {"INPUT", "OUTPUT", "INPUT_PULLUP", "THRUSTER", "STEPPER"};
	public ArduinoPinConfig()
	{
		
	}
	public ArduinoPinConfig(int pinNumber, int pinNumberTwo, int pinMode, int value)
	{
		this.setPinNumber(pinNumber);
		this.setPinMode(pinMode);
		this.setValue(value);
		this.setPinNumberTwo(pinNumberTwo);
	}
	
	public int getPinNumber()
	{
		return this.pinNumber;
	}
	public int getPinMode()
	{
		return this.pinMode;
	}
	
	public void setPinNumber(int pinNumber)
	{
		this.pinNumber = pinNumber;
	}
	public void setPinMode(int pinMode)
	{
		if(pinMode < 0 || pinMode > ArduinoPinConfig.modes.length)
		{
			throw new IllegalArgumentException("Invalid Pin Mode.");
		}
		
		this.pinMode = pinMode;
	}
	public void setValue(int value)
	{
		this.value = value;
	}
	
	public String toString()
	{
		return this.pinNumberToString()+" - "+this.pinModeToString();
	}
	public String pinModeToString()
	{
		return modes[this.pinMode];
	}
	public String pinNumberToString() {
		
		String r = "";
		if(this.pinNumber < 10)
		{
			r += "0";
		}
		r += this.pinNumber;
		
		return r;
	}
	
	public int getValue()
	{
		return this.value;
	}
	
	public static String[] getModes()
	{
		return modes;
	}
	public void setPinNumberTwo(int pinNumberTwo)
	{
		this.pinNumberTwo = pinNumberTwo;
	}
	public int getPinNumberTwo() 
	{
		return pinNumberTwo;
	}
	public String pinNumberTwoToString() {
		String r = "";
		if(this.pinNumberTwo < 10)
		{
			r += "0";
		}
		r += this.pinNumberTwo;
		
		return r;
	}
}
