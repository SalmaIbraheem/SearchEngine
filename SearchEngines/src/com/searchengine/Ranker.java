package com.searchengine;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class Ranker  {
	//calculation of tf-idf
	
	
	
	
	public static void main(String[] args) throws IOException, SQLException {

		DBManager mDB = new DBManager();
		//mDB.rankTf();
		//pageRank();
		//String[] strArray1 = new String[] {"liked","man"};
		//mDB.getSearchResult(strArray1);
		PopularityRank p = new PopularityRank(mDB);
		p.pageRank();
		//RelvenceRank r= new RelvenceRank(mDB);
		//r.rankTf();
		
	
	}
}
