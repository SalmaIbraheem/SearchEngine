package com.searchengine;

import java.sql.SQLException;
import java.util.ArrayList;

public class UrlList {
	
	private ArrayList<String> urls; 
	private int lastPosition;
	public DBManager dbMan;
	private int crawledLinks = 0;
	
	public UrlList(DBManager dbMan) throws SQLException {
		this.dbMan = dbMan;
		this.lastPosition = -1;
		urls = dbMan.getUrls();
	}
	
	public String getNewUrl() throws SQLException{
		synchronized((Object)lastPosition) {
			lastPosition++;
			if(urls.size() == lastPosition ) {
				if(!Update()) {	//reaching stopping criteria
					System.out.println("Crawling is finished");
					return null;
				}else {
					//have other links in list or database not visit
					System.out.println(urls.size());
					System.out.println(lastPosition);
					return urls.get(lastPosition);
				}
			}else if(urls.size() > lastPosition ) {
				//have other links in list or database not visit
				System.out.println(urls.size());
				System.out.println(lastPosition);
				return urls.get(lastPosition);
			}
		}
		System.out.println(urls.size());
		System.out.println(lastPosition);
		return null;
	}
	
	public synchronized Boolean Update() throws SQLException{
		lastPosition = 0;
		//we take all urls in the list retrieve from database
		urls = dbMan.getUrls();
		if(urls.size() == 0) {
			return false;
		}
		return true;
	}

	public int getCrawledLinks() {
		return crawledLinks;
	}

	public synchronized void setCrawledLinks(int crawledLinks) {
		this.crawledLinks = crawledLinks;
		System.out.println(crawledLinks);
	}
	
}
