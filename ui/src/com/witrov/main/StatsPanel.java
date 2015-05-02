package com.witrov.main;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class StatsPanel extends JPanel {
	
	private ThrusterPanel t1, t2, t3, t4;
	
	private JLabel depth;
	private JPanel depthPanel;
	
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
		
		depth = new JLabel("0");
		
		this.setLayout(new GridLayout(0,1));
		this.add(t1);
		this.add(t2);
		this.add(t3);
		this.add(t4);
		
		depthPanel.add(depth);
		
		this.add(depthPanel);
		
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

}
