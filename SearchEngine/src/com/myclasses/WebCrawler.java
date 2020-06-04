/**
 * 
 */
package com.myclasses;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Salma Ibrahim
 *
 */

public class WebCrawler implements Runnable  {

	private UrlList list;
	private DBManager mDB;
	private int mID;
	private int numberThreads;
	private int iteration;
	
	public WebCrawler(UrlList list,DBManager mDB,int mID, int numberThreads, int iteration) {
		this.list = list;
		this.mDB = mDB;
		this.mID = mID;
		this.numberThreads = numberThreads;
		this.iteration = iteration;
	}
	@Override
	public void run() {
		System.out.println("Thread "+mID+" start working");
		for(int i = 0; i< (5000+this.iteration*200)/numberThreads;i++) {
			String page = "";
			try {
				page = list.getNewUrl();
				System.out.println(page);
				if(!crawlHyberlinks(page)) {
					return;
				}
				mDB.updatewebsite(page);
				System.out.println("Thread "+mID+" crawling page "+page);
			} catch (SQLException | IOException e) {
			}
			//links has no permissions to be showed	
		}
	}
		
	private boolean crawlHyberlinks(String page) throws IOException, SQLException {
		if(page == null) return false;
		RobotParser robot = new RobotParser(page);
		int rejectedLinks = 0;	
		
		Document doc = Jsoup.connect(page).get();
		//get all links and recursively call crawlPage
		Elements hyberLinks = doc.select("a[href]");
		int firstNum = list.getCrawledLinks();
		if(robot.getmDissallowed().size() != 0) {
			for(Element link: hyberLinks){
				//To check 404 not found
				try {
					doc = Jsoup.connect(link.attr("abs:href")).get();
					//access only web site with type "HTML"
					//and check permissions
					int numWebsites = list.getCrawledLinks();
					System.out.println(numWebsites);
					if(numWebsites< getlimits(firstNum)) {
						if(robot.checkPermission(link.attr("abs:href")) && 
								doc.documentType().name().equals("html")) {
	
								String text = doc.body().text();
								//System.out.println(text);
								text = text.replaceAll("\\s+","");
								text = text.replaceAll("'","");
								text = text.replaceAll("\\?", "");
								int recrawl = getRecrawl(page,text);
								mDB.addLink(page,link.attr("abs:href"),Jsoup.connect(link.attr("abs:href")).get().select("a[href]").size(),text,recrawl);
								//Increasing number of pages in database and stop when its 50000
								list.setCrawledLinks(list.getCrawledLinks()+1);
								
						}else {
							rejectedLinks++;
						}
					}else {
						return false;
					}
				}catch(IOException | IllegalArgumentException | NullPointerException e) {
					//throw exception because 404 not found not insert in database
					rejectedLinks++;
				}
			}
			
		}
		return true;
	}
	private int getRecrawl(String page, String text) {
		page = page.toLowerCase();
		if(page.contains("news") || page.contains("covid")) {
			return 1;
		}if(page.contains("shop")) {
			return 2;
		}if(page.contains("movie") || page.contains("music") || page.contains("art") || page.contains("sports")  ) {
			return 3;
		}
		return 5;
	}
	
	private int getlimits(int numWebsites) {
		if(numWebsites < 5000)
			return 5000;
		else
			return numWebsites+200;
	}
	

}

