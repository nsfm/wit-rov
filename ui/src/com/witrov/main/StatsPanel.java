package com.witrov.main;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

public class StatsPanel extends JPanel {
	
	private ThrusterPanel t1, t2, t3, t4;
	
	private JLabel depth, heading, pitch, roll, headingToKeepLabel;
	private JPanel depthPanel, headingPanel, keepHeading;
	private JTextField headingToKeep;
	
	
	public StatsPanel()
	{
		init();
	}
	
	public void init()
	{		
		t1 = new ThrusterPanel("Front Left", "Thruster 1");
		t2 = new ThrusterPanel("Front Right", "Thruster 2");
		t3 = new ThrusterPanel("Back Left", "Thruster 3");
		t4 = new ThrusterPanel("Back Right", "Thruster 4");
		
		depthPanel = new JPanel();
		depthPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Depth"));
		
		headingPanel = new JPanel();
		headingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Headings"));
		
		heading = new JLabel("Heading: 0");
		headingToKeepLabel = new JLabel("Keep Heading: ");
		pitch = new JLabel("Pitch: 0");
		roll = new JLabel("Roll: 0");
		depth = new JLabel("0");
		
		headingToKeep = new JTextField(5);
		
		keepHeading = new JPanel();
		keepHeading.add(headingToKeepLabel);
		keepHeading.add(headingToKeep);
		
		this.setLayout(new GridLayout(0,1));
		this.add(t1);
		this.add(t2);
		this.add(t3);
		this.add(t4);
		
		
		
		depthPanel.add(depth);
		headingPanel.add(heading);
		headingPanel.add(pitch);
		headingPanel.add(roll);
		
		this.add(depthPanel);
		this.add(headingPanel);
		this.add(keepHeading);
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Stats"));
	}
	

	public ThrusterPanel getThruster(int thruster)
	{
		switch(thruster)
		{
			case 1:
				return t1;
			case 2:
				return t2;
			case 3:
				return t3;
			case 4:
				return t4;
			default:
				return null;
		}
	}
	
	public void setDepth(int depth)
	{
		this.depth.setText("" + depth);
	}
	public void setDepth(String depth)
	{
		this.depth.setText(depth);
	}
	
	public void setHeading(float heading)
	{
		this.heading.setText("Heading: "+heading);
	}
	public void setPitch(float pitch)
	{
		this.pitch.setText("Pitch: "+pitch);
	}
	public void setRoll(float roll)
	{
		this.roll.setText("Roll: "+roll);
	}

	public int getHeading() {
		String heading = this.heading.getText();
		heading = heading.substring(8, heading.length());
		return (int)Float.parseFloat(heading);
	}

	public int getHeadingToKeep() {
		return Integer.parseInt(this.headingToKeep.getText());
	}

	public void setHeadingToKeep(int heading) {
		this.headingToKeep.setText(""+heading);
		
	}

}
