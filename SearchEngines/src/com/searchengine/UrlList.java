package com.searchengine;

import java.sql.SQLException;
import java.util.ArrayList;

public class UrlList {
	
	private ArrayList<String> urls; 
	private int lastPosition;
	public DBManager dbMan;
	private int crawledLinks = 0;
	private int iteration;
	
	public UrlList(DBManager dbMan,int iteration) throws SQLException {
		this.dbMan = dbMan;
		this.lastPosition = -1;
		this.crawledLinks = dbMan.getBeforeInt();
		this.iteration = iteration;
		System.out.println("previous crawled paged = "+dbMan.getBeforeInt());
		urls = dbMan.getUrls(this.iteration);
	}
	
	public String getNewUrl() throws SQLException{
		synchronized((Object)lastPosition) {
			lastPosition++;
			if(urls.size() == lastPosition ) {
				if(!Update()) {	//reaching stopping criteria
					//System.out.println("Crawling is finished");
					return null;
				}else {
					//have other links in list or database not visit
					//System.out.println(urls.size());
					//System.out.println(lastPosition);
					return urls.get(lastPosition);
				}
			}else if(urls.size() > lastPosition ) {
				//have other links in list or database not visit
				//System.out.println(urls.size());
				System.out.println(urls.get(lastPosition));
				return urls.get(lastPosition);
			}
		}
		Update();
		//System.out.println(urls.size());
		//System.out.println(lastPosition);
		return null;
	}
	
	public synchronized Boolean Update() throws SQLException{
		lastPosition = 0;
		//we take all urls in the list retrieve from database
		urls = dbMan.getUrls(this.iteration);
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
