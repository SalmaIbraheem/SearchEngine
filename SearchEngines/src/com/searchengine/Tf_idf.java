package com.searchengine;

import java.io.IOException;
import java.sql.SQLException;

public class Tf_idf {
	
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
	public static void main(String[] args) throws IOException, SQLException {

		DBManager mDB = new DBManager();
		mDB.getWords();
	
	}
}
