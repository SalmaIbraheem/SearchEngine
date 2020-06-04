package com.myclasses;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RelvenceRank {
	
	
	private static DBManager mDB;
	private static ArrayList<String>url,wordId;
	private static ArrayList<Float>ranks;
	private static int totalDoc;
	
	public RelvenceRank(DBManager mDB){
		
		this.mDB=mDB;
		url=new ArrayList<String>();
		wordId=new ArrayList<String>();
		ranks=new ArrayList<Float>();
		totalDoc=0;
		
	}
	
	public void setTotDocs() throws SQLException
	{
		totalDoc=mDB.getTotPages();
	}
	
	public void rankTf(int type) throws NumberFormatException, SQLException
	{
		setTotDocs();
		ResultSet words;
		//if type 1 rank all rows of table if 2 then rank only the rows that have not been indexed before
		if(type == 1)
			words= mDB.getWordsOfPages();
		else 
			words= mDB.getWordsOfNotIndexPages();
		
		int occur=0;
		int totWord = 0,docOccur = 0;
		
		float score;
		int i=0;
		while (words.next()) {
			//System.out.println(i);
			occur = Integer.parseInt(words.getString("total_occur"));//number of occurnces of word in page
			totWord= Integer.parseInt(words.getString("size"));//number of total words in page
			docOccur= Integer.parseInt(words.getString("c"));//number of pages that have this word
			score = tfIdf(occur, totWord, totalDoc, docOccur);
			//System.out.println(words.getString("word_id"));
			
			url.add(words.getString("URL"));
			wordId.add(words.getString("word_id"));
			ranks.add(score);
			System.out.println("rank");
			i++;
		}
		mDB.setTf(url, wordId, ranks);
		
		
	}
	
	
	
	
	public double tf(int occ,int tot){
		double o=occ;
		if (tot == 0)
			return .1;
		return o/tot;
		
	}
	
	public double idf(long totDoc,int docOccur) {
		double t=totDoc;
		return Math.log(t/docOccur);
	}
	
	public float tfIdf(int occ,int tot,long totDoc,int docOccur)
	{
		return (float) (tf(occ,tot)*idf(totDoc,docOccur));
	}

}