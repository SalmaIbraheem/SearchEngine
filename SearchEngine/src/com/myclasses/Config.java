package com.myclasses;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Config {

	private int numOfCrawlers;
	private int maxPages;
	
	public Config() {
		try {
			File myObj = new File("Config.txt");
		    Scanner myReader = new Scanner(myObj);
		    while (myReader.hasNextLine()) {
		    	numOfCrawlers = Integer.parseInt(myReader.nextLine());
		    	maxPages =  Integer.parseInt(myReader.nextLine());
		    }
		    myReader.close();
		  } catch (FileNotFoundException e) {
			  System.out.println("An error occurred.");
			  e.printStackTrace();
		  }
	}

	public int getNumOfThreads() {
		return numOfCrawlers;
	}

	public int getMaxPages() {
		return maxPages;
	}

}
