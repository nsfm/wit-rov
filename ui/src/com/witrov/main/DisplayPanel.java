package com.witrov.main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DisplayPanel extends JPanel{
	
	private int horizantalAngle = 0;
	private int depth = 0;
	
	private JTextField dangerZone;
	private JTextField ticks;
	private JTextField threshold;
	
	private JLabel dLabel;
	private JLabel tLabel;
	private JLabel thLabel;
	
	private int dZone = 0;
	private int numberOfTicks = 50;
	private int tHold = 10;
	
	public DisplayPanel(int x, int y)
	{
		init();
		this.setSize(x, y);
	}
	
	public void init()
	{
		dangerZone = new JTextField(5);
		dangerZone.setText(""+dZone);
		ticks = new JTextField(5);
		ticks.setText(""+numberOfTicks);
		threshold = new JTextField(5);
		threshold.setText(""+tHold);

		dLabel = new JLabel("Danger Zone: ");
		tLabel = new JLabel("Ticks: ");
		thLabel = new JLabel("Tick Space: ");
		
		JPanel dPanel = new JPanel();
		dPanel.add(dLabel);
		dPanel.add(dangerZone);
		
		JPanel tPanel = new JPanel();
		tPanel.add(tLabel);
		tPanel.add(ticks);
		
		JPanel thPanel = new JPanel();
		thPanel.add(thLabel);
		thPanel.add(threshold);
		
		this.add(dPanel);
		this.add(tPanel);
		this.add(thPanel);
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(Color.BLACK);
		
		//Draw the Horizonometer
		int x1 = 0, y1 = this.getSize().height/2 + (this.horizantalAngle*3);
		int x2 = this.getSize().width, y2 = this.getSize().height/2 - (this.horizantalAngle*3);
		Random r = new Random();
		
		//this.horizantalAngle = r.nextInt(200)-1;
		this.depth = r.nextInt(this.numberOfTicks)-1;
	
		int xDiff = x2 - x1;
		int yDiff = y2 - y1;
		
		int lineAngle = (int)((180 * Math.atan(1.0*yDiff/xDiff))/Math.PI);
		g.drawLine(x1, y1, x2, y2);
		g.drawArc(this.getSize().width/2 - 50, this.getSize().height/2 - 50, 100, 100, -lineAngle, 180);
		
		
		//Draw the depth Gauge
		try
		{
			this.dZone = Integer.parseInt(this.dangerZone.getText());
		}catch(Exception e)
		{
			this.dZone = -1;
		}
		try {
			this.numberOfTicks = Integer.parseInt(this.ticks.getText());
		}catch(Exception e)
		{
			this.numberOfTicks = 50;
		}
		try
		{
			this.tHold = Integer.parseInt(this.threshold.getText());
		}catch(Exception e)
		{
			this.tHold = 10;
		}
		int depthX = 10, depthY = 10, depthZ = 200, depthW = 30;
		g.drawRect(depthX, depthY, depthW, depthZ);
		if(this.dZone > 0 && this.depth > this.dZone)
		{
			g.setColor(Color.RED);
		}
		else
		{
			g.setColor(Color.BLUE);
		}
		g.fillRect(depthX, depthY, depthW, depthY + (this.depth * (depthZ/this.numberOfTicks))-8);
		if(this.dZone > 0 && this.depth > this.dZone)
		{
			g.setColor(Color.BLUE);
		}
		else
		{
			g.setColor(Color.RED);
		}
		g.drawLine(depthX + depthW/2, depthY, depthX + depthW/2, depthZ);
		g.drawLine(depthX + depthW/4, depthZ - 5, depthX + depthW/2, depthZ);
		g.drawLine(depthX + (int)(depthW * (3*1.0/4)), depthZ - 5, depthX + depthW/2, depthZ);
		
		//draw depth graph		
		g.setColor(Color.BLACK);
		g.drawLine(depthX + depthW + 5, depthY, depthX + depthW + 5, depthZ);
		g.drawLine(depthX + depthW + 2, depthY, depthX + depthW + 7, depthY);
		g.drawString("0", depthX + depthW + 9, depthY + 5);
		
		for(int i = 1; i <= this.numberOfTicks; i++)
		{
			if(i == this.dZone)
			{
				g.setColor(Color.RED);
				g.drawLine(depthX + depthW + 2, depthY + (i * (depthZ/this.numberOfTicks)), depthX + depthW + 7, depthY + (i * (depthZ/this.numberOfTicks)));
				g.drawString(""+(i), depthX + depthW + 9, depthY + (i * (depthZ/this.numberOfTicks)) + 5);
			}
			else if(i % this.tHold == 0)
			{
				g.setColor(Color.BLACK);
				g.drawLine(depthX + depthW + 2, depthY + (i * (depthZ/this.numberOfTicks)), depthX + depthW + 7, depthY + (i * (depthZ/this.numberOfTicks)));
				g.drawString(""+(i), depthX + depthW + 9, depthY + (i * (depthZ/this.numberOfTicks)) + 5);
			}
		}
	}
	
	public void setDepth(int depth)
	{
		this.depth = depth;
	}
	public void setAngle(int angle)
	{
		this.horizantalAngle = angle;
	}
}
