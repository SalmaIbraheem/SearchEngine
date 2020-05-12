package com.searchengine;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DBManager {

	
	private JDataBase mDB;
	private static final String[] mSeeds = {"http://www.mit.edu/"};
	private static final int mNumberOfSeeds = 1;
	private boolean interrupt = false;
	private String insertQuery = "";
	private int insertNumbers = 0;
	  
	  
	public DBManager() throws SQLException {
		mDB = new JDataBase();
		String queryString= "if (object_id('websites', 'U') is null) \r\n" + 
				"begin \r\n" + 
				"CREATE TABLE websites( id int not null IDENTITY(1,1) , \r\n" + 
				"						URL varchar(3000) not null,\r\n" + 
				"						crawled int DEFAULT 0,\r\n" + 
				"						content varchar(max),\r\n" + 
				"						pointers int DEFAULT 0,\r\n" + 
				"						primary key (URL)); \r\n" + 
				"end;";
		mDB.executeQuery(queryString);
		//make sure database if empty to insert seeds (in case of interrupt)
		ArrayList<String> table = getUrls();
		if(table.size() == 0) {
			insertSeeds();
		}else {
			//come back from interruption
			this.interrupt = true;
		}
	}
	
	public void crawlPage(String URL) throws SQLException, IOException{
		Document doc = Jsoup.connect(URL).userAgent("Mozilla").get();
		String query = "UPDATE websites SET crawled = 1 WHERE (URL = '"+URL+"');";
		mDB.executeQuery(query);
		//get all links and recursively call crawlPage
		Elements hyberLinks = doc.select("a[href]");
		int i = 0;
		for(Element link: hyberLinks){
			if(i < 5000) {
				addLink(URL,link.attr("abs:href"));
				if(insertNumbers < 10 && insertNumbers != 0)mDB.executeQuery(insertQuery);
				i++;
			}
		}
	}
	public ArrayList<String> getUrls() throws SQLException {
		ArrayList<String> urlsList = new ArrayList<String>();
		String query = "Select URL FROM websites WHERE (crawled = 0);";
		ResultSet urls = mDB.getResult(query);
		while (urls.next()) {
			urlsList.add(urls.getString("URL"));
		}
		return urlsList;
	}
	public boolean checkRobot(String URL) {
		
		return false;
		
	}
	private void insertSeeds() {
		for(int i = 0; i < mNumberOfSeeds; i++) {
			mDB.executeQuery("INSERT INTO websites (\"URL\")VALUES ('"+mSeeds[i]+"');");
		}
	}

	public boolean isInterrupt() {
		return interrupt;
	}
	private void addLink(String Patrent, String link) throws IOException, SQLException {
		//get permission from robots.txt
		
		//access only web site with type "HTML"
		if(Jsoup.connect(link).userAgent("Mozilla").get().documentType().name().equals("html")) {
			//check if it's already in database
			this.insertQuery += "IF not EXISTS (Select* FROM websites WHERE (URL = '"+link+"'))\r\n" + 
					"BEGIN\r\n" + 
					"INSERT INTO websites (\"URL\")VALUES ('"+link+"')\r\n" + 
					"end;";
			insertNumbers++;
			if(insertNumbers == 10) {
				insertNumbers = 0;
				mDB.executeQuery(insertQuery);
			}
		}
	}

}
	  
