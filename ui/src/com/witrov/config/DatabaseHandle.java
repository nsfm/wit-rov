package com.witrov.config;

import java.io.File;
import java.sql.*;

public class DatabaseHandle {
	
	private String dbName = "test.db";
	private String dbPath = System.getProperty("user.home")+"\\WIT_ROV";
	private Connection c;
	public DatabaseHandle()
	{

	    try {
	      Class.forName("org.sqlite.JDBC");

	      File f = new File(dbPath);
	      f.mkdirs();
	      c = DriverManager.getConnection("jdbc:sqlite:"+f.getAbsolutePath()+"/"+dbName);

	      Statement stmt = c.createStatement();
	      String sql = "CREATE TABLE IF NOT EXISTS config " +
	                   "(key VARCHAR(16) PRIMARY KEY     NOT NULL," +
	                   " value    TEXT    NOT NULL)"; 
	      stmt.executeUpdate(sql);
	      stmt.close();
	    } catch ( Exception e ) {
	    System.out.println(e.getMessage());
	      System.exit(0);
	    }
	}
	
	public void insertConfig(String key, String value)
	{
		String query = "INSERT INTO config (key, value) VALUES('"+key+"','"+value+"')";
		try {
			Statement stmt = c.createStatement();
			stmt.execute(query);
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void updateConfig(String key, String value)
	{
		String query = "UPDATE config SET value='"+value+"' WHERE key='"+key+"'";
		try {
			Statement stmt = c.createStatement();
			stmt.execute(query);
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}
