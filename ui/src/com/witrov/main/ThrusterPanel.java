package com.witrov.main;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class ThrusterPanel extends JPanel{

	private JLabel vLabel, lLabel;
	private JLabel velocity, location;
	
	public ThrusterPanel(String location, String title)
	{
		init(location, title);
	}
	public void init(String location, String title)
	{
		
		this.vLabel = new JLabel("Velocity: ");
		this.lLabel = new JLabel("Location: ");
		
		this.velocity = new JLabel("NULL");
		this.location = new JLabel(location);
		
		this.setLayout(new GridLayout(2,1));
		
		this.add(this.vLabel);
		this.add(this.velocity);
		this.add(this.lLabel);
		this.add(this.location);
		
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), title));
	}
	
	public void setVelocity(int velocity)
	{
		this.velocity.setText(" " + velocity);
	}
	
	public int getVelocity()
	{
		return Integer.parseInt(this.velocity.getText().trim());
	}
	
	public void setLocation(String location)
	{
		this.location.setText(location);
	}
}
