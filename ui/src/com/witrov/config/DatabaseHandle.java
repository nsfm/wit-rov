package com.witrov.config;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseHandle {
	
	private String dbName = "wit_rov.db";
    private String dbPath;
	private Connection c;
	public DatabaseHandle()
	{
	    
            String OS = System.getProperty("os.name").toLowerCase();
            if (OS.indexOf("windows") >= 0) 
            {
                 // Windows
            	dbPath = System.getProperty("user.home")+"\\AppData\\Roaming\\WIT_ROV\\DB\\";
            } 
            else
            {
                 // Linux
                 //Mac
                 dbPath = System.getProperty("user.home")+"/WIT_ROV/DB/";
            }
	    try {
	      Class.forName("org.sqlite.JDBC");
              
	      File f = new File(dbPath);
	      f.mkdirs();
	      c = DriverManager.getConnection("jdbc:sqlite:"+dbPath+dbName);
            
	      Statement stmt = c.createStatement();
	      String config = "CREATE TABLE IF NOT EXISTS config " +
	                   "(key VARCHAR(16) PRIMARY KEY     NOT NULL," +
	                   " value    TEXT    NOT NULL)"; 
	      
	      stmt.executeUpdate(config);
	      
	      //value is used for thrusters
	      String pinConfig = "CREATE TABLE IF NOT EXISTS pinConfig " +
                  "(pinNumber int(11) PRIMARY KEY     NOT NULL," +
                  " pinNumberTwo   int(11)," +
                  " pinMode   int(11)    NOT NULL," +
                  " value	  int(11))";
	      
	      stmt.executeUpdate(pinConfig);
	      
	      stmt.close();
	    } catch ( Exception e ) {
	      	e.printStackTrace();
		System.exit(0);
	    }
	}
	
	public boolean insertConfig(String key, String value)
	{
		String query = "INSERT INTO config (key, value) VALUES('"+key+"','"+value+"')";
		try {
			Statement stmt = c.createStatement();
			stmt.execute(query);
			stmt.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	public boolean updateConfig(String key, String value)
	{
		String query = "UPDATE config SET value='"+value+"' WHERE key='"+key+"'";
		try {
			Statement stmt = c.createStatement();
			stmt.execute(query);
			stmt.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	public void closeConnection()
	{
		try {
			c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String findIp()
	{
		String query = "SELECT value FROM config WHERE key='ip'";
		String ip = null;
		try {
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next())
			{
				ip = rs.getString("value");
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ip;
	}
	public String findPort()
	{
		String query = "SELECT value FROM config WHERE key='port'";
		String sPort = null;
		try {
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next())
			{
				sPort = rs.getString("value");
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sPort;
	}
	public String findBy(String value)
	{
		String query = "SELECT value FROM config WHERE key='"+value+"'";
		String val = null;
		try {
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next())
			{
				val = rs.getString("value");
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return val;	
	}
	public ArduinoPinConfig findPinBy(String column, int value)
	{
		String query = "SELECT * from pinConfig WHERE "+column+"='"+value+"'";
		ArduinoPinConfig pin = null;
		try {
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next())
			{
				pin = new ArduinoPinConfig(rs.getInt("pinNumber"),rs.getInt("pinNumberTwo"),rs.getInt("pinMode"),rs.getInt("value"));
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pin;
	}
	
	public boolean insertPinConfig(ArduinoPinConfig pin)
	{
		String insert = "INSERT INTO pinConfig (pinNumber, pinNumberTwo, pinMode, value) VALUES ('"+pin.getPinNumber()+"', '"+pin.getPinNumberTwo()+"', '"+pin.getPinMode()+"', '"+pin.getValue()+"')";
		try {
			Statement stmt = c.createStatement();
			stmt.execute(insert);
			stmt.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	public boolean updatePinConfig(ArduinoPinConfig pin)
	{
		String update = "UPDATE pinConfig SET pinMode='"+pin.getPinMode()+"', value='"+pin.getValue()+"' WHERE pinNumber='"+pin.getPinNumber()+"'";
		try {
			Statement stmt = c.createStatement();
			stmt.execute(update);
			stmt.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public ArrayList<ArduinoPinConfig> findAllPinConfigs()
	{
		ArrayList<ArduinoPinConfig> list = new ArrayList<ArduinoPinConfig>();
		
		String query = "SELECT * FROM pinConfig";
		try {
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next())
			{
				list.add(new ArduinoPinConfig(rs.getInt("pinNumber"), rs.getInt("pinNumberTwo"), rs.getInt("pinMode"), rs.getInt("value")));
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}

	public boolean removePinConfig(ArduinoPinConfig pin) {
		String delete = "DELETE FROM pinConfig WHERE pinNumber='"+pin.getPinNumber()+"'";
		try {
			Statement stmt = c.createStatement();
			stmt.execute(delete);
			stmt.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
