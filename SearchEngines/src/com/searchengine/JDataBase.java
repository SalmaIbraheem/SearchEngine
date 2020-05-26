package com.searchengine;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDataBase {
	
	public JDataBase() {}
	
	public void executeQuery(String query) {
		try {
			getConnection().createStatement().executeUpdate(query);
		} catch (Exception e) {
		}
	}

	public ResultSet getResult(String query) {
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	private Connection getConnection() {
		try {
			String url = "jdbc:sqlserver://localhost;databaseName=search_engine;integratedSecurity=true;";
			return DriverManager.getConnection(url);
		} catch (Exception e) {
			return null;
		}
	}
}
