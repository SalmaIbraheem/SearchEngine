package com.myclasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerMain {

	private static Config crawlerConfig;
	private static UrlList list;
	
	public static void main(String[] args) throws IOException, SQLException, InterruptedException {

		DBManager mDB = new DBManager();
		IndexerMain indexer = new IndexerMain(mDB);
		PopularityRank p = new PopularityRank(mDB);
		RelvenceRank r= new RelvenceRank(mDB);
		new Thread(new RankThread(r)).start();
		int k = 0;
		while (true) {
			crawlerConfig = new Config();
			list = new UrlList(mDB,k,0);
			ArrayList <Thread> threads = new ArrayList<Thread>();
			for(int i = 0; i < 20; i++) {
				Thread t =new Thread(new WebCrawler(list, mDB,i,crawlerConfig.getNumOfThreads(),k));
				t.start();
				threads.add(t);
			}
			indexer.Indexeing();
			for(Thread s :threads)
			{
				try {
					s.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			p.pageRank();
			indexer.endJoin();
			k++;
			Thread.sleep(20*60*1000);
			r.rankTf(1);
		}
		



		/*JDataBase mDB = new JDataBase();
		String query = "CREATE TABLE userid(\n" + 
				"    id varchar(30) NOT NULL PRIMARY KEY,\n" + 
				"    pwd varchar(30) NOT NULL,\n" + 
				"    fullname varchar(50),\n" + 
				"    email varchar(50)\n" + 
				");";
		mDB.executeQuery(query);*/
		///////////////////////////////////////////////////////////////s/////////	
		/*String USER_AGENT = "WhateverBot";
		String url = "http://www.mit.edu/";
		URL urlObj = new URL(url);
		String hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
		                + (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
		//Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
		//BaseRobotRules rules = robotsTxtRules.get(hostId);
		
		
		Document doc = Jsoup.connect("http://www.mit.edu/").get();
		Document access = Jsoup.connect("http://www.mit.edu/robots.txt").get();
		System.out.println(access.select("Disallow"));
		 if(doc.documentType().name().equals("html")) {
			 System.out.println("yes");
		 }else {
			 System.out.println("No");
		 }
		//get all links and recursively call the processPage method
		Elements questions = doc.select("a[href]");
		for(Element link: questions){
			//System.out.println(link.attr("abs:href"));
		}*/
		/*
		Document doc = Jsoup.connect("http://www.mit.edu/").get();
		String text = doc.body().text();
		text = "???????????????????????????????????????????????????????????????????????????????????????????????????????????????ï¿½2020GoogleLLCMassach";
		text = text.replaceAll("\\s+","");
		text = text.replaceAll("'","");
		text = text.replaceAll("\\?","");
		text = text.replaceAll("\\?","");
		System.out.println(text);
	*/
		/*String url = "http://calendar.mit.edu/browse/places";
		URL web = new URL(url);
		System.out.println(web.getHost());*/
		
	}

}