/**
 * 
 */
package com.searchengine;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Salma Ibrahim
 *
 */

public class WebCrawler implements Runnable  {

	private UrlList list;
	private DBManager mDB;
	private int mID;
	
	public WebCrawler(UrlList list,DBManager mDB,int mID) {
		this.list = list;
		this.mDB = mDB;
		this.mID = mID;
	}
	@Override
	public void run() {
		for(int i = 0; i< 5;i++) {
			System.out.println("Thread "+mID+" start working");
			try {
				String page = list.getNewUrl();
				mDB.crawlPage(page);
				System.out.println("Thread "+mID+" crawling page "+page);
			} catch (SQLException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}

