package com.myclasses;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL; 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.microsoft.sqlserver.jdbc.SQLServerException;

import opennlp.tools.stemmer.PorterStemmer;

public class DBManager {

	
	private JDataBase mDB;

	private static final String[] mSeeds = {"https://www.techmeme.com/","https://us.wikihow.com/","https://www.pricerunner.com/","https://www.wikipedia.org/","https://www.pinterest.com/","https://www.imdb.com/","https://us.yahoo.com/","https://edition.cnn.com/", "https://bookpage.com//"};

	private static final int mNumberOfSeeds = mSeeds.length;
	private boolean interrupt = false;
	private String insertQuery = "";
	private PorterStemmer porter = new PorterStemmer();  
	  
	public DBManager() throws SQLException, IOException {
		mDB = new JDataBase();
		
		String queryString= "if (object_id('websites', 'U') is null) \r\n" + 
				"begin \r\n" + 
				"CREATE TABLE websites( id int not null IDENTITY(1,1) , \r\n" + 
				"						URL varchar(3000) not null,\r\n" + 
				"						crawled int DEFAULT 0,\r\n" + 
				"						interupt int DEFAULT 0,\r\n" + 
				"						recrawl int DEFAULT 5,\r\n" + 
				"						content varchar(max) not null,\r\n" + 
				"						done_indexed int DEFAULT 1,\r\n" +
				"						size int DEFAULT 0,\r\n" + 
				"						childern int DEFAULT 0,\r\n" + 
				"						geograph varchar(20),\r\n" +  
				"						visited float DEFAULT 1,\r\n" + 
				"						PR float DEFAULT 0,\r\n" + 
				"						indexed int DEFAULT 0,\r\n"+
				"						primary key (URL)); \r\n" + 
				"end;\r\n";
		
		queryString += "if (object_id('Pointers', 'U') is null) \r\n" + 
				"begin  \r\n" + 
				"CREATE TABLE Pointers(url1_id varchar(3000) not null,\r\n" + 
				"					  url2_id varchar(3000) not null,\r\n" + 
				"					  foreign key (url2_id) references websites(URL));\r\n" + 
				"end;\r\n";

		
		//words table
		queryString += "if(object_id('words','U') is null)\r\n"+
		"begin\r\n"+
		"create table words (id int not null IDENTITY(1,1) primary key,stem varchar(50) not null);\r\n "+
		"end;\r\n";
		mDB.executeQuery(queryString);
		
		//words_websites table
		queryString = "if(object_id('words_websites','U') is null)\r\n"+
		"begin\r\n"+
		"create table words_websites (word_id int not null,URL varchar(3000) not null,score float DEFAULT 0,"+
		"total_occur int DEFAULT 1"+
		",FOREIGN KEY (word_id) REFERENCES words(id) "+
		" ,FOREIGN KEY (URL) REFERENCES websites(URL)"+
		", CONSTRAINT p_key PRIMARY KEY(word_id,URL))\r\n"+
		"end;\r\n";
		
		queryString += "if(object_id('queryTable','U') is null)\r\n" +
				"begin create table queryTable(id int not null IDENTITY(1,1) primary key,query varchar(max)) end";
		mDB.executeQuery(queryString);

		//make sure database if empty to insert seeds (in case of interrupt)
		ArrayList<String> table = getUrls(0,0);
		if(table.size() == 0) {
			insertSeeds();
		}
	}
	private void insertSeeds() throws IOException {
		for(int i = 0; i < mNumberOfSeeds; i++) {
			Document doc = Jsoup.connect(mSeeds[i]).get();
			String text = doc.body().text();
			text = text.replaceAll("\\s+","");
			text = text.replaceAll("'","");
			text = text.replaceAll("\\?","");
			
			//System.out.println(text);
			mDB.executeQuery("IF NOT EXISTS (Select* FROM websites WHERE (URL = '"+mSeeds[i]+"'))\r\n" + 
					"BEGIN\r\n" + 
					"INSERT INTO websites (\"URL\",\"childern\",\"content\",\"recrawl\",\"geograph\")VALUES ('"+mSeeds[i]+"',"+doc.select("a[href]").size()+",'"+text+"', 10,'"+getGeo(mSeeds[i])+"');" + 
					"END;");
		}
	}

	public ArrayList<String> getUrls(int iteration,int type) throws SQLException {
		ArrayList<String> urlsList = new ArrayList<String>();
		String column = "";
		String query = "";
		
		ResultSet duplicate_urls;
		if(type == 0)//crawler
		{
			column = "crawled";
			query = "SELECT * FROM websites WHERE ((crawled = 0 or (interupt = 0 and crawled = 1) ) and "+iteration+"%recrawl = 0) ORDER BY childern ASC;";
		}
		else 
		{
			column = "indexed";
			 query = "SELECT * FROM websites WHERE ("+column+" = 0) ORDER BY childern DESC;";
			 duplicate_urls = mDB.getResult("SELECT * FROM websites WHERE indexed = 1 and done_indexed = 0 ORDER BY childern DESC;");
			 while (duplicate_urls.next()) {
				if( !(urlsList.contains(duplicate_urls.getString("URL"))))
					urlsList.add(duplicate_urls.getString("URL"));
			 }
		}
		
		ResultSet urls = mDB.getResult(query);
		//to concatenate query and execute them all
		String update_query = "";
		while (urls.next()) {
			urlsList.add(urls.getString("URL"));
			update_query += "UPDATE websites SET "+column+"=1 WHERE (URL = '"+urls.getString("URL")+"');\r\n";
		}
		mDB.executeQuery(update_query);
		return urlsList;
	}
	public boolean isInterrupt() {
		return interrupt;
	}
	
	public void addLink(String parent, String link,int hyberLinksSize,String content,int recrawl) throws MalformedURLException{
		//get permission from robots.txt
	
		if(content.length() > 7000) content = content.substring(0,7000);
		//System.out.println(content);
		if(content.length() > 50) {
			//check if it's already in database
			this.insertQuery = "IF NOT EXISTS (Select * FROM websites WHERE (URL = '"+link+"'  or content LIKE '%"+content+"%'))\r\n"+
					"BEGIN\r\n" + 
					"INSERT INTO websites (\"URL\",\"childern\",\"content\",\"recrawl\",\"geograph\")VALUES ('"+link+"',"+hyberLinksSize+",'"+content+"',"+recrawl+",'"+getGeo(link)+"');\r\n" + 
					"END\r\n" +
					"ELSE IF NOT EXISTS(Select * FROM websites WHERE (URL = '"+link+"' and content = '"+content+"'))\r\n" + 
					"BEGIN	\r\n" + 
					"	UPDATE websites SET indexed = 0 , done_indexed = 0 WHERE (URL = '"+link+"'); \r\n" +
					"END ";
	
			//add to the relation between urls 
			this.insertQuery += "IF NOT EXISTS (Select* FROM Pointers WHERE (url1_id = '"+parent+"' AND url2_id = '"+link+"')) \r\n" + 
					"BEGIN \r\n" + 
					"	INSERT INTO Pointers (url1_id,url2_id) Values('"+parent+"','"+link+"');\r\n" + 
					"END;\r\n";
			mDB.executeQuery(this.insertQuery);
		}
	}

	public void updatewebsite(String page) {
		// TODO Auto-generated method stub
		String query = "UPDATE websites SET interupt=1 WHERE (URL = '"+page+"');";
		mDB.executeQuery(query);
	}
	public int getBeforeInt() throws SQLException{
		String query = "select count (*) as size from websites;";
		ResultSet set =  mDB.getResult(query);
		if(set.next()) {
			try {
				return set.getInt("size");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			} 
		}
		return 0;
	}
	
	public String getGeo(String url) throws MalformedURLException {
		URL web = new URL(url);
		String host = web.getHost();
		String[] bits = host.split("\\.");
		return bits[bits.length-1];
	}
    
    ///////////////////////////////ranking//////////////////////////////////
    
    
   
    public void setInitPR(float n)throws SQLException {
    	String query="update websites  set PR="+ 1.0/n+";";
    	mDB.executeQuery(query);
    }
    
    public ResultSet pagesUrl()throws SQLException{
    	String query="select URL,PR from websites;";
    	ResultSet c= mDB.getResult(query);
    	return c;
    }
    
    public ResultSet getLinkedPages(String page)throws SQLException{
    	String query="Select url1_id,childern,PR FROM Pointers T1  JOIN websites T2 ON T1.url1_id = T2.URL where (T1.url2_id = '"+page+"');";
    	ResultSet c= mDB.getResult(query);
    	return c;
    }
    public ResultSet getPR()throws SQLException{
    	String query="with temp as (select (PR/childern) as x ,URL,url2_id\n" + 
    			"FROM Pointers T1  JOIN websites T2 ON T1.url1_id = T2.URL\n" + 
    			" )\n" + 
    			"select sum(x) as r,url2_id from temp group by url2_id;";
    	ResultSet c= mDB.getResult(query);
    	return c;
    }
    
    public void setPR(HashMap<String, Float> ranks)throws SQLException {
    	String query= "";
    	int i=0;
    	int j=1;
    	for (Entry<String, Float> entry : ranks.entrySet()) {
		   
		    query+="update websites set PR="+entry.getValue()+" where URL= '"+entry.getKey()+"';\r\n";
		    if(i== j*600 )
		    {
		    	mDB.executeQuery(query);
		    	query="";
		    	j++;
		    }
		    i++;	
		    
		}
    
    	mDB.executeQuery(query);
    }
    
    ////////////////////////////////////////////Relvence Ranking///////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public ResultSet getWordsOfPages() {
    	String query = "select total_occur,size,websites.URL,word_id,\n" + 
    			"count(*)over (partition by word_id) as c\n" + 
    			" from websites join words_websites \n" + 
    			" on websites.URL = words_websites.URL ;\n" ;
    	
    	ResultSet words = mDB.getResult(query);
    	return words;
    }
    
    public int getTotPages()throws SQLException {
    	
    	String query ="select count (*) from websites;";
		ResultSet c= mDB.getResult(query);
		int totDoc=0;
		while(c.next())
		{
			 totDoc =  Integer.parseInt(c.getString(""));
		}
		return totDoc;
		
    	
    }
    
    public void setTf(ArrayList<String>url,ArrayList<String>wordId,ArrayList<Float>rank)throws SQLException {
    	String query= "";
    	//String tempQ="";
        int j=1;
    	for (int i=0;i<url.size();i++) {
		   
		    query += "UPDATE words_websites SET score="+ rank.get(i) +" WHERE (URL = '"+url.get(i)+"' and word_id = "+Integer.parseInt(wordId.get(i))+");\r\n";
    		//System.out.println(query);
		    if(i== j*600 )
		    {
		    	mDB.executeQuery(query);
		    	query="";
		    	j++;
		    }
		    	
		  
		}
        //System.out.println(query);
    	mDB.executeQuery(query);
    	
    } 
    //////////////////////////////Changes////////////////////////////
    public void setVisits(String url) {
    	String query = "update websites set visited = visited*1.05 where url like '%"+url+"%';";
    	mDB.executeQuery(query);
    }
    
    

		    
//////////////indexer functions /////////////////////
    void insert_words(ArrayList<String> words , String url,int count) throws SQLException
    {
    	String concatenated_query="UPDATE websites SET size = "+count+" WHERE URL = \'"+url+"\';\r\n";
    	String id_query ="";
    	String temp ="";
    	//delete from words_websites
		concatenated_query+="delete from words_websites where URL = \'"+url+"\'\r\n";
		int i =2;
    	for(String s :words)
    	{
    		if(i > 590)
    		{
    			mDB.executeQuery(concatenated_query);
    			concatenated_query = "";	
    			i=0;
    		}
			String stem = porter.stem(s);
    		//temp = "select id from words where stem = \'"+stem+"\'";
    		//insert in words table
    		
    		id_query = "(select id from words where stem = \'"+stem+"\')";
    		concatenated_query+="if not exists ( "+id_query+") begin insert into words (stem)"
    				+ " values (\'"+stem+"\') end;\r\n";
    		i++;
    		//System.out.println("if not exists ( "+id_query+") begin insert into words (stem)"
    			//	+ " values (\'"+stem+"\') end;\r\n");
    		//insert in words_websites
    		
    		temp = "select total_occur from words_websites where word_id = ("+id_query+") AND URL = \'"+url+"\'";
    		concatenated_query+="if not exists ("+temp+")"
    				+ " begin insert into words_websites (word_id,URL) values ("+id_query+",\'"+url+"\') "
    						+ "end else begin "
    						+  "update words_websites set total_occur = ((" +temp+")+1) where word_id = ("+id_query+") AND URL = \'"+url+"\' end\r\n";
    		
    	i++;		
    	}
    	concatenated_query+="UPDATE websites SET done_indexed = 1 WHERE URL = \'"+url+"\';\r\n";
    	//System.out.println("UPDATE websites SET done_indexed = 1 WHERE URL = \'"+url+"\';\r\n");
    	
    	mDB.executeQuery(concatenated_query);
    	System.out.println("done");
    	
    }  
    
    
    public void insertQuery(String str) {
    	String tmp = "select id from queryTable where query = \'"+str+"\'";
    	String query = "if not exists ( "+tmp+") begin insert into queryTable (query) values (\'" + str +"\') end;";
    	
    	mDB.executeQuery(query);
    	
    }
    
    public JSONArray getQueries(String str) throws SQLException
    {
    	System.out.println("get query11111");
    	String query = "select TOP 5 query from queryTable where query like \'%" +str + "%\';";
    	ResultSet result = mDB.getResult(query);
    	System.out.println("get query");
		
    	JSONArray jarray = new JSONArray();
    	while(result.next())
    	{
    		JSONObject jobj = new JSONObject();
    		jobj.put("query", result.getString("query"));
    		jarray.add(jobj);
    	}
    	return jarray;
    }
    
    public int getNoOfResults(ArrayList<String> queryWords) throws SQLException
    {
    	int n=0;
	    	if(queryWords.size() > 0) {
	    	String query = "SELECT count(*) as cnt\r\n" + 
	    			"  FROM words_websites JOIN words\r\n" + 
	    			"    ON words_websites.word_id = words.id\r\n" + 
	    			" WHERE words.stem = " + "'"+queryWords.get(0)+"'";
	    	for(int i=1; i<queryWords.size(); i++)
	    		query+= " or words.stem = " + "'"+queryWords.get(i)+"'";
	    	
	    	ResultSet result = mDB.getResult(query);
	    	
	    	while(result.next())
	    	{
	    		n = result.getInt("cnt");
	    	}
    	}
    	
    	return n;
    }
    
    public ArrayList<String> getSearchResult(ArrayList<String> queryWords, int page, String country) throws SQLException
    {
    	ArrayList<String> output = new ArrayList<String>();
    	if(queryWords.size() > 0) {
    	String tmp = "SELECT *\r\n" + 
    			"  FROM words_websites JOIN words\r\n" + 
    			"    ON words_websites.word_id = words.id\r\n" + 
    			" WHERE words.stem = " + "'"+queryWords.get(0)+"'";
    	for(int i=1; i<queryWords.size(); i++)
    		tmp+= " or words.stem = " + "'"+queryWords.get(i)+"'";
    	
    	String query = "select t2.Url, \r\n" + 
    			"			CASE\r\n" + 
    			"			    WHEN geo='" + country +"' THEN tot*1.2\r\n" + 
    			"			    ELSE tot*1\r\n" + 
    			"			END AS newt \r\n"+
    			"from\r\n" +
    			"(select  websites.URL, (websites.PR)*sum(score)*visited as tot , (geograph) as geo\r\n" +
    			"from(" + tmp +") as t1 join websites\r\n" + 
    					"			   on websites.URL = t1.URL\r\n" + 
    					"			group by websites.URL,PR,geograph,visited)  as t2\r\n" + 
    					"			 order by newt desc\r\n" +
    					"offset "+ Integer.toString((page-1)*10)+" rows\r\n" +
    	    			"fetch next 10 rows only;";
    			
    	
    	/*String query = "select  t1.URL, max(websites.PR)*sum(score) as tot \r\n" + 
    			"from( "+tmp+") as t1 join websites\r\n" + 
    			"on websites.URL = t1.URL\r\n" + 
    			"group by t1.URL\r\n" + 
    			"order by tot DESC\r\n" +
    			"offset "+ Integer.toString((page-1)*10)+" rows\r\n" +
    			"fetch next 10 rows only;";*/
    
    	System.out.println(query);
    	ResultSet result = mDB.getResult(query);
    	 
    	
    	
    	while(result.next())
    	{
    		String str = result.getString("URL");
    		output.add(str);
    	}
    	}
    	return output; 
    	
    }
    
    public ArrayList<String> getPharseSearch(String phrase,ArrayList<String> words, int page,String country) throws SQLException{
    	/*
    	 * select t2.Url , 
		CASE
		    WHEN geo='eg' THEN tot*1.2
		    ELSE tot*1
		END AS newt
		from
		(SELECT words_websites.url, sum(score)*(PR)*visited as tot , (geograph) as geo
		  FROM words_websites JOIN words 		
		 ON words_websites.word_id = words.id
		 join (select * FROM websites where (content like '%skipto%')  ) as t1 on t1.URL= words_websites.URL
		 where words.stem='skip' or words.stem='to'
		 group by words_websites.URL,PR,visited,geograph) as t2
		 order by newt desc
 ;*/
    	
    	String query = "";
    	System.out.println("im phrasee!!!! "+phrase);
    	phrase = phrase.replaceAll("\\s+","");
    	ArrayList<String> urlsList = new ArrayList<String>();
		query += "select t2.Url , \r\n" + 
				"		CASE\r\n" + 
				"		    WHEN geo='"+country+"' THEN tot*1.2\r\n" + 
				"		    ELSE tot*1\r\n" + 
				"		END AS newt\r\n" + 
				"		from\r\n" + 
				"		(SELECT words_websites.url, sum(score)*(PR)*visited as tot , (geograph) as geo\r\n" + 
				"		  FROM words_websites JOIN words 		\r\n" + 
				"		 ON words_websites.word_id = words.id\r\n" + 
				"		 join (select * FROM websites where (content like '%" +phrase+"%')  ) as t1 on t1.URL= words_websites.URL\r\n"+
				"   where";
		if(words.size() > 0) {
			query += " words.stem='"+words.get(0)+"'";
		}
		for(int i = 1; i<words.size();i++) {
			query += " or words.stem='"+words.get(i)+"'";
		}
		query += "  group by words_websites.URL,PR,visited,geograph) as t2\r\n" + 
				" order by newt desc\r\n" + 
				 "offset "+ Integer.toString((page-1)*10)+" rows\r\n" +
	    		 "fetch next 10 rows only;";
		ResultSet web =  mDB.getResult(query);
		while(web.next())
		{
			urlsList.add(web.getString("Url"));
		}
		return urlsList;
    }
    
    public ResultSet getWordsOfNotIndexPages() {
    	String query = "select total_occur,size,websites.URL,word_id,\n" + 
    			"count(*)over (partition by word_id) as c\n" + 
    			" from websites join words_websites \n" + 
    			" on websites.URL = words_websites.URL \n"+
    			" where score = 0; "
    			;
    	
    	ResultSet words = mDB.getResult(query);
    	return words;
    }
    
    

}
	  
