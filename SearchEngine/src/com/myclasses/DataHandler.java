package com.myclasses;
import com.servlets.SearchRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DataHandler {
	
	public DBManager dbM;
	private ArrayList<String> urls;
	
	
	public DataHandler() throws SQLException, IOException
	{
		dbM = new DBManager();
		ArrayList<String> urls = new ArrayList<String>();
	}
	
	public ArrayList<String> getUrls(int page) throws SQLException
	{
		if(SearchRequest.phraseFlag == true)
		{
			
			urls = dbM.getPharseSearch(SearchRequest.phrase, SearchRequest.queryWords, page,SearchRequest.country);	
		}
		else 
		{
			urls = dbM.getSearchResult(SearchRequest.queryWords,page, SearchRequest.country);
		
		}
		
        return urls;
	}
	
	
	public int getNoOfResults(int page) throws SQLException
	{
		return urls.size();
	}
	
	public ArrayList<Result> getResultList(int page) throws IOException, SQLException
	{
		getUrls(page);
		
		ArrayList<Result> list = new ArrayList<Result>();
		for(int i=0; i<urls.size(); i++)
		{
			//get doc title
			Result tmp = new Result();
			Document doc = Jsoup.connect(urls.get(i)).get();
			tmp.title = doc.title();
			
			//set url
			tmp.url = urls.get(i);
			
			//set paragraph
			 String p = "";
			 for(int j=0; j<SearchRequest.queryWords.size() && p.length()<80; j++)
			{
				Elements paragraphs = doc.select("p:contains("+ SearchRequest.queryWords.get(j) + ")");
				if(paragraphs.size() > 0)
				{	
					p += Jsoup.parse(paragraphs.get(0) .toString()).text();
					if(p.length()< 10)
					{
						if(paragraphs.size() > 1)
						{
							p += Jsoup.parse(paragraphs.get(1) .toString()).text();
						}
					}
				}
			}
			
			String desc;
			if(p == "")
			{
				desc = getMetaTag(doc,"description");
				if(desc == null) 
				{
					desc = getMetaTag(doc,"og:description");
				}
				if(desc == null || desc == "")
				{
					desc = "vist our website for more information";
				}
				tmp.text = desc;
				
			}
			else
				tmp.text = p;
			
			list.add(tmp);	
		}
		return list;
	}
	
	private String getMetaTag(Document doc, String param)
	{
		Elements els = doc.select("meta[name="+param+"]");
		for(Element e : els) {
			String str = e.attr("content");
			if(str!= null) return str;
		}
		
		els = doc.select("meta[property="+param+"]");
		for(Element e : els) {
			String str = e.attr("content");
			if(str!= null) return str;
		}
		return null;
	}
	public String getQuery()
	{
		return SearchRequest.query;
	}

	public void insertQuery(int page) 
	{
		if(page == 1)
			dbM.insertQuery(SearchRequest.query);
	}
	
	
	

}
