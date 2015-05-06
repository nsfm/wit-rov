package com.witrov.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

public class LogPanel extends JPanel implements ActionListener{
	
	public static final int APPEND_BOTTOM = 0;
	public static final int APPEND_TOP = 1;
	private static final int TEXT_SIZE = 18;
	
	
	private JButton clear;
	private JTextPane log;
	private String logInit = "<html>", logList = "", logEnd = "</html>";
	private JScrollPane logScroll;
	private int appendStyle = APPEND_BOTTOM;
	private int logCount = 0;
	
	public LogPanel(int height, int width)
	{
		this.init(height, width);
	}

	public LogPanel(int height, int width, int appendStyle)
	{
		if(appendStyle == APPEND_TOP || appendStyle == APPEND_BOTTOM)
		{
			this.appendStyle = appendStyle;
		}
		this.init(height, width);
	}
	
	private void init(int height, int width)
	{
		this.log = new JTextPane();
		this.log.setContentType("text/html");
		this.log.setText(this.logInit+this.logEnd);
		this.log.setPreferredSize(new Dimension(width, height));
		this.log.setEditable(false);
		
		this.logScroll = new JScrollPane(this.log);
		this.logScroll.setPreferredSize(new Dimension(width,height));
		this.logScroll.getVerticalScrollBar().setUnitIncrement(15);
						
		clear = new JButton("Clear Log");
		clear.addActionListener(this);
		
		BorderLayout layout = new BorderLayout();
		
		this.setLayout(layout);

		this.add(clear, BorderLayout.NORTH);
		this.add(logScroll);
		
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Log"));
	}

	public void error(String message)
	{
		this.append("ERROR", message);
	}

	public void debug(String message)
	{
		this.append("DEBUG", message);
	}
	
	public void info(String message)
	{
		this.append("INFO", message);
	}
	
	private void append(String type, String message)
	{
		if(logCount > 100)
		{
			logCount = 0;
			this.clearLog();
		}
		checkLogScrollLength();
		String timeStamp = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date());
		String color = "black";
		if(type.equals("ERROR"))
		{
			color = "red";
		}
		else if(type.equals("DEBUG"))
		{
			color = "green";
		}
		if(this.appendStyle == APPEND_TOP)
		{
			this.logList += "<div style='color: "+color+"'>"+timeStamp+" "+type+": "+message+"</div>" + this.logList;
		}
		else
		{
			this.logList += "<div style='color: "+color+"'>"+timeStamp+" "+type+": "+message+"</div>";
		}
		
		this.logCount++;
		
		this.log.setText(this.logInit+this.logList+this.logEnd);
		checkLogScrollLength();
		
	}
	
	//gets how many logs before we need to make the scroll log bigger
	private void checkLogScrollLength()
	{
		int size = this.log.getHeight()/TEXT_SIZE;
		if(size < logCount)
		{
			this.log.setPreferredSize(new Dimension(this.log.getWidth(), this.log.getHeight() + TEXT_SIZE));
			this.log.setCaretPosition(this.log.getDocument().getLength());
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == clear)
		{
			clearLog();
		}
		
	}
	
	public void clearLog()
	{
		this.log.setText("");
		this.logList = "";
	}
}
