package com.myclasses;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IndexerMain {

	private static UrlList list;
	DBManager mDB;
	ArrayList <Thread> threads ;
	public IndexerMain(DBManager mDB) throws SQLException {
		this.mDB=mDB;
		list = new UrlList(mDB,0,1);
		this.threads = new ArrayList<Thread>();
	}
	public void  Indexeing() throws IOException, SQLException {
		//read stopwords file
		List<String>stopwords = Files.readAllLines(Paths.get("stop_words.txt"));
		//DBManager mDB = new DBManager();
		
		int num_indexers = 40;
		
		ArrayList <Thread> threads = new ArrayList<Thread>();
		//while(true)
		{
			for(int i=0;i<num_indexers;i++)
			{
				Thread t = new Thread(new Indexer(list, stopwords, mDB));
				t.start();
				threads.add(t);
			}
			
			//notify ranker somehow
			//change indexed column if needed
			//System.out.println("end");
		}
		
	}
	public void endJoin() {
		for(Thread s :threads)
		{
			try {
				s.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
