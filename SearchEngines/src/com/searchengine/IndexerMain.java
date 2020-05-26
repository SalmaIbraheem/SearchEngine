package com.searchengine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IndexerMain {

	private static UrlList list;
	public static void main(String[] args) throws IOException, SQLException {
		//read stopwords file
		List<String>stopwords = Files.readAllLines(Paths.get("stop_words.txt"));
		DBManager mDB = new DBManager();
		list = new UrlList(mDB,0,1);
		int num_indexers = 5;
		ArrayList <Thread> threads = new ArrayList<Thread>();
		//while(true)
		{
			for(int i=0;i<num_indexers;i++)
			{
				Thread t = new Thread(new Indexer(list, stopwords));
				t.start();
				threads.add(t);
			}
			for(Thread s :threads)
			{
				try {
					s.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//notify ranker somehow
			//change indexed column if needed
			System.out.println("end");
		}
		
	}

}
