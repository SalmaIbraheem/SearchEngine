package com.searchengine;

import java.io.IOException;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.microsoft.sqlserver.jdbc.SQLServerException;

import opennlp.tools.stemmer.PorterStemmer;

public class DBManager {

	
	private JDataBase mDB;

	private static final String[] mSeeds = {"https://www.techmeme.com/","https://www.wikipedia.org/","https://dmoz-odp.org/","https://www.imdb.com/","https://medium.com/"};

	private static final int mNumberOfSeeds = 5;
	private boolean interrupt = false;
	private String insertQuery = "";
	  
	  
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
				"						size int DEFAULT 0,\r\n" + 
				"						childern int DEFAULT 0,\r\n" + 
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
		"create table words (id int not null IDENTITY(1,1) primary key,stem varchar(50) not null ,word varchar(50) not null);\r\n "+
		"end;\r\n";
		mDB.executeQuery(queryString);
		
		//words_websites table
		queryString += "if(object_id('words_websites','U') is null)\r\n"+
		"begin\r\n"+
		"create table words_websites (word_id int not null,URL varchar(3000) not null,score float DEFAULT 0,"+
		"total_occur int DEFAULT 1,first_position int DEFAULT 0 "+
		",FOREIGN KEY (word_id) REFERENCES words(id) "+
		" ,FOREIGN KEY (URL) REFERENCES websites(URL)"+
		", CONSTRAINT p_key PRIMARY KEY(word_id,URL))\r\n"+
		"end;\r\n";
		
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
					"INSERT INTO websites (\"URL\",\"childern\",\"content\",\"recrawl\")VALUES ('"+mSeeds[i]+"',"+doc.select("a[href]").size()+",'"+text+"', 10);" + 
					"END;");
		}
	}

	public ArrayList<String> getUrls(int iteration,int type) throws SQLException {
		ArrayList<String> urlsList = new ArrayList<String>();
		String column = "";
		String query = "";
		if(type == 0)//crawler
		{
			column = "crawled";
			query = "SELECT * FROM websites WHERE ((crawled = 0 or (interupt = 0 and crawled = 1)) and "+iteration+"%recrawl = 0) ORDER BY childern DESC;";
		}
		else 
		{
			column = "indexed";
			 query = "SELECT * FROM websites WHERE ("+column+" = 0) ORDER BY childern DESC;";
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
	
	public void addLink(String parent, String link,int hyberLinksSize,String content,int recrawl){
		//get permission from robots.txt
		if(content.length() > 7000) content = content.substring(0,7000);
		//System.out.println(content);
		if(content.length() > 50) {
			//check if it's already in database
			this.insertQuery ="IF NOT EXISTS (Select* FROM websites WHERE (URL = '"+link+"' and content LIKE '%"+content+"%'))\r\n" + 
					"BEGIN\r\n" + 
					"	INSERT INTO websites (\"URL\",\"size\",\"childern\",\"content\",\"recrawl\")VALUES ('"+link+"',"+link.length()+","+hyberLinksSize+",'"+content+"',"+recrawl+");\r\n" +
					"END;\r\n";
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
    
    

		    
//////////////indexer functions /////////////////////
    void insert_words_count(String url,int count)
    {
    	String query = "UPDATE websites SET size = "+count+" WHERE URL = \'"+url+"\'";
    	//System.out.println(count);
    	//System.out.println(query);
    	mDB.executeQuery(query);
    }
    void insert_words(ArrayList<String> words , String url) throws SQLException
    {
    	for(String s :words)
		{
			//System.out.println(s);
    		//check if word exists in words table
			String query = "select id from words where word = \'"+s+"\';";
			//System.out.println(query);
			ResultSet rs = mDB.getResult(query);
			
			if(rs.next()) //word already inserted in words table
			{
				//System.out.println(rs.getInt(1));
				insert_word_website(rs.getInt(1), url);
			}
			else {
				//stem word
				PorterStemmer porter = new PorterStemmer();
				String stem = porter.stem(s);
				query = "INSERT INTO words (stem,word) values (\'"+stem+"\',\'"+s+"\')";
				//System.out.println(query);
				mDB.executeQuery(query);
				//get id of the inserted
				query = "select id from words where word = \'"+s+"\';";
				rs = mDB.getResult(query);
				if(rs.next())
				{
					insert_word_website(rs.getInt(1), url);
				}
			}
		}
    }
    void insert_word_website(int id,String url) throws SQLException
    {
    	String query = "select total_occur from words_websites where word_id = "+Integer.toString(id)+" AND URL = \'"+url+"\'";
    	ResultSet rs = mDB.getResult(query);
    	if(rs.next())
    	{
    		
    		query = "update words_websites set total_occur = " +Integer.toString(rs.getInt(1)+1)+" where word_id = "+Integer.toString(id)+" AND URL = \'"+url+"\'";
    		//System.out.println(query);
    		mDB.executeQuery(query);
    	}
    	else
    	{
    		query = "insert into words_websites (word_id,URL) values (\'"+Integer.toString(id)+"\',\'"+url+"\') ;";
    		//System.out.println(query);
    		mDB.executeQuery(query);
    	}
    	
    	
    }
//////////////indexer functions /////////////////////

    
    

}
	  
