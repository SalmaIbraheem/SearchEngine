package com.searchengine;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class PopularityRank {
	
	private static float dampFactor;
	private static  HashMap<String,Float> ranks;
	private static DBManager mDB;
	private static int multiplicationFactor;
	
	public PopularityRank(DBManager mDB)
	{
		this.mDB=mDB;
		ranks=new HashMap<String,Float>();
		dampFactor=(float) .85;
		multiplicationFactor = 10000000;
	}
	
	
	private static void setInit() throws SQLException {
		int count= mDB.getTotPages();
		mDB.setInitPR(count);
	}
	public static boolean didConverge(float newVal,float oldVal)
	{
		if((int)Math.floor(newVal*multiplicationFactor) != (int)Math.floor(oldVal*multiplicationFactor)) {
			return false;
		}
		else
			return true;
			
		
		
	}
	
	public static void pageRank() throws SQLException, IOException {

		ResultSet pr;
		Float tempRank=(float) 0.0;
		Float oldValue=(float) 0.0;
		int i=0;
		boolean flag=true;
		boolean converged;
		String url= "";
		setInit();
		while(i< 50 && flag)
		{
			pr = mDB.getPR();
			if(i ==0 )
				converged=false;
			else
				converged=true;

			while(pr.next()) {
				tempRank=Float.parseFloat(pr.getString("r"));
				tempRank= (1-dampFactor)+ (dampFactor*tempRank);
				url=pr.getString("url2_id");
				if(converged)
				{
					oldValue= ranks.get(url);
					converged= didConverge(tempRank,oldValue);
				}
				
				ranks.put(url,tempRank);
				
			}
			if(converged)
			{
				flag=false;
			}
			else
			{
				mDB.setPR(ranks);
				i++;
			}
			
			
			System.out.println(i);
		}
	}
}
