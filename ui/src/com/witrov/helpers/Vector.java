package com.witrov.helpers;

public class Vector {

	private double x;
	private double y;
	private double magnitude;
	private double angle;
	
	public Vector(double x, double y)
	{
		this.setVector(x, y);
	}
	public double getX()
	{
		return this.x;
	}
	public double getY()
	{
		return this.y;
	}
	public double getMagnitude()
	{
		return this.magnitude;
	}
	public double getAngle()
	{
		return this.angle;
	}
	
	public void setVector(double x, double y)
	{
		this.x = x;
		this.y = y;
		
		this.magnitude = Vector.calculateMagnitude(x, y);
		this.angle = Vector.calculateAngle(x, y);
	}
	
	//Calculates the magnitude of the vector
	public static double calculateMagnitude(double x, double y)
	{
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	//Calculates the angle of the vector
	public static double calculateAngle(double x, double y)
	{
		return (180 * Math.atan(y/x))/Math.PI;
	}
	
	//Projects vector b onto vector a
	public static Vector project(Vector a, Vector b)
	{
		Vector c;
		
		double scalar = (Vector.calculateDotProduct(a, b))/Math.pow(a.getMagnitude(), 2);
		
		c = new Vector(scalar * a.getX(), scalar * a.getY());
		
		return c;
	}
	
	//calculates the dot product of vector a and vector b
	public static double calculateDotProduct(Vector a, Vector b)
	{
		return (a.getX()*b.getX()) + (a.getY()*b.getY());
	}
}
