/**
 * 
 */
package com.searchengine;

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
	
	public WebCrawler(UrlList list,DBManager mDB,int mID) {
		this.list = list;
		this.mDB = mDB;
		this.mID = mID;
	}
	@Override
	public void run() {
		System.out.println("Thread "+mID+" start working");
		for(int i = 0; i< 500;i++) {
			String page;
			try {
				page = list.getNewUrl();
				//System.out.println(page);
				if(!crawlHyberlinks(page)) {
					return;
				}
				System.out.println("Thread "+mID+" crawling page "+page);
			} catch (SQLException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		if(robot.getmDissallowed().size() != 0) {
			for(Element link: hyberLinks){
				//To check 404 not found
				try {
					doc = Jsoup.connect(link.attr("abs:href")).get();
					//access only web site with type "HTML"
					//and check permissions
					if(list.getCrawledLinks() < 50000) {
						if(robot.checkPermission(link.attr("abs:href")) && 
								doc.documentType().name().equals("html")) {
	
								String text = doc.body().text();
								text = text.replaceAll("\\s+","");
								text = text.replaceAll("'","");
								text = text.replaceAll("\\?", "");
							
								mDB.addLink(page,link.attr("abs:href"),Jsoup.connect(link.attr("abs:href")).get().select("a[href]").size(),text);
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
	

}

