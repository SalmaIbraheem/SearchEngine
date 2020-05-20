package com.searchengine;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class Ranker  {
	//calculation of tf-idf
	
	public double tf(int occ,int tot){
		double o=occ;
		return o/tot;
		
	}
	
	public double idf(long totDoc,int docOccur) {
		double t=totDoc;
		return Math.log(t/docOccur);
	}
	
	public double tfIdf(int occ,int tot,long totDoc,int docOccur)
	{
		return tf(occ,tot)*idf(totDoc,docOccur);
	}
	////////////////////////////////////////////////////////
	////////calculation of popularity///////////////////////
	public static void pageRank() throws SQLException, IOException {
		DBManager mDB = new DBManager();
		int count= mDB.getTotPages();
		mDB.setInitPR(count);
		float dampFactor= (float) .85;
		ResultSet pr;
		ArrayList<Float> ranks= new ArrayList<Float>();
		ArrayList<String> url= new ArrayList<String>();
		Float tempRank=(float) 0.0;
		int i=0;
		boolean flag=true;
		while(i< 20 && flag)
		{
			pr = mDB.getPR();
			ranks.clear();
			url.clear();
			while(pr.next()) {
				tempRank=Float.parseFloat(pr.getString("r"));
				tempRank= (1-dampFactor)+ (dampFactor*tempRank);
				ranks.add(tempRank);
				url.add(pr.getString("url2_id"));
				
			}
			
			mDB.setPR(ranks, url);
			i++;
			System.out.println(i);
		}
		
		
	}
	
	
	public static void main(String[] args) throws IOException, SQLException {

		//DBManager mDB = new DBManager();
		//mDB.rankTf();
		pageRank();
		//String[] strArray1 = new String[] {"liked","man"};
		//mDB.getSearchResult(strArray1);
	
	}
}
