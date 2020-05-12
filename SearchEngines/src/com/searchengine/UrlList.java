package com.searchengine;

import java.sql.SQLException;
import java.util.ArrayList;

public class UrlList {
	
	private ArrayList<String> urls; 
	private int lastPosition;
	public DBManager dbMan;
	
	public UrlList(DBManager dbMan) {
		this.dbMan = dbMan;
		lastPosition = -1;
		urls = new ArrayList<String>();
	}
	
	public String getNewUrl() throws SQLException{
		synchronized((Object)lastPosition) {
			lastPosition++;
			if(urls.size() == lastPosition ) {
				if(!Update()) {	//reaching stopping criteria
					System.out.println("Crawling is finished");
					return null;
				}
			}
			//have other links in list or database not visit
			System.out.println(lastPosition);
			return urls.get(lastPosition);
		}
	}
	
	public Boolean Update() throws SQLException{
		synchronized((Object)lastPosition) {
			lastPosition = 0;
			//we take all urls in the list retrieve from database
			urls = dbMan.getUrls();
			if(urls.size() == 0) {
				return false;
			}
			return true;
		}
	}
	
}
