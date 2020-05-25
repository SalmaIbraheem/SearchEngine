package com.searchengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;

public class RobotParser {
	private ArrayList<String> mDissallowed;
	private String mParent;
	
	public RobotParser(String mParent) throws MalformedURLException {
		mDissallowed = new ArrayList<String>();
		URL web = new URL(mParent);
		this.mParent = web.getHost();
		findDisallowed();
	}

	private void findDisallowed() {
		int user = 0;
		String pages;
		System.out.println("https://"+this.mParent+"/robots.txt");
		try{BufferedReader in = new BufferedReader(
	            new InputStreamReader(new URL("https://"+this.mParent+"/robots.txt").openStream())); 
	        String line = null;
	        while((line = in.readLine()) != null) {
	        	//some robots.txt url doesn't work and shows html
	        	if(line.contains("DOCTYPE HTML"))return;
	        	if(line.contains("User-agent")) {
	        		user = 1;
	        	}else if(line.contains("User-agent") && line.contains("*")){
	        		user = 0;
	        	}if((line.contains("Disallow") || line.contains("disallow")) && user == 1) {
	        		pages = "/"+line.substring(line.indexOf("/") + 1);
	        		mDissallowed.add(pages);
		            //System.out.println(pages);
	        	}
	            //System.out.println(line);
        	}
	            
	    } catch (IOException | IllegalArgumentException | NullPointerException e) {
	    	System.out.println("Robot of "+this.mParent+"Doesn't exist");
	    	return;
	    }
		
	}

	public boolean checkPermission(String Url) {
		//if cannot access robots.txt stop crawling it
		if(mDissallowed.size() == 0)return false;
		for	(int i = 0; i<this.mDissallowed.size();i++) {
			if(Url.contains(this.mDissallowed.get(i))) {
				return false;
			}
		}
		return true;
	}
	public  ArrayList<String> getmDissallowed(){
		return mDissallowed;
	}
}
