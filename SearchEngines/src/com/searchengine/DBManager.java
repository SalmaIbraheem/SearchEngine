package com.searchengine;

import java.io.IOException;
import java.net.URL; 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.microsoft.sqlserver.jdbc.SQLServerException;

public class DBManager {

	
	private JDataBase mDB;
	private Ranker rank;
	private static final String[] mSeeds = {"http://www.mit.edu/","http://www.mit.edu/#main"};
	private static final int mNumberOfSeeds = 2;
	private boolean interrupt = false;
	private String insertQuery = "";
	  
	  
	public DBManager() throws SQLException, IOException {
		mDB = new JDataBase();
		
		String queryString= "if (object_id('websites', 'U') is null) \r\n" + 
				"begin \r\n" + 
				"CREATE TABLE websites( id int not null IDENTITY(1,1) , \r\n" + 
				"						URL varchar(3000) not null,\r\n" + 
				"						crawled int DEFAULT 0,\r\n" + 
				"						content varchar(max) not null,\r\n" + 
				"						size int DEFAULT 0,\r\n" + 
				"						childern int DEFAULT 0,\r\n" + 
				"						PR float DEFAULT 0,\r\n" + 
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
		"create table words_websites (word_id int not null,URL varchar(3000) not null,score int DEFAULT 0,"+
		"total_occur int DEFAULT 0,first_position int DEFAULT 0 "+
		",FOREIGN KEY (word_id) REFERENCES words(id) "+
		" ,FOREIGN KEY (URL) REFERENCES websites(URL)"+
		", CONSTRAINT p_key PRIMARY KEY(word_id,URL))\r\n"+
		"end;\r\n";
		
		mDB.executeQuery(queryString);

		//make sure database if empty to insert seeds (in case of interrupt)
		ArrayList<String> table = getUrls();
		if(table.size() == 0) {
			insertSeeds();
		}else {
			//come back from interruption
			this.interrupt = true;
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
					"INSERT INTO websites (\"URL\",\"childern\",\"content\")VALUES ('"+mSeeds[i]+"',"+doc.select("a[href]").size()+",'"+text+"');" + 
					"END;");
		}
	}

	public ArrayList<String> getUrls() throws SQLException {
		ArrayList<String> urlsList = new ArrayList<String>();
		
		String query = "SELECT * FROM websites WHERE (crawled = 0) ORDER BY childern DESC;";
		ResultSet urls = mDB.getResult(query);
		//to concatenate query and execute them all
		String update_query = "";
		while (urls.next()) {
			urlsList.add(urls.getString("URL"));
			update_query += "UPDATE websites SET crawled=1 WHERE (URL = '"+urls.getString("URL")+"');\r\n";
		}
		mDB.executeQuery(update_query);
		return urlsList;
	}
	public boolean isInterrupt() {
		return interrupt;
	}
	
	public void addLink(String parent, String link,int hyberLinksSize,String content) throws IOException, SQLException {
		//get permission from robots.txt
		if(content.length() > 7000) content = content.substring(0,7000);
		//System.out.println(content);
		
		//check if it's already in database
		this.insertQuery ="IF NOT EXISTS (Select* FROM websites WHERE (URL = '"+link+"'))\r\n" + 
				"BEGIN\r\n" + 
				"	INSERT INTO websites (\"URL\",\"size\",\"childern\",\"content\")VALUES ('"+link+"',"+link.length()+","+hyberLinksSize+",'"+content+"');\r\n" ;
		//add to the relation between urls 
		this.insertQuery += "IF NOT EXISTS (Select* FROM Pointers WHERE (url1_id = '"+parent+"' AND url2_id = '"+link+"')) \r\n" + 
				"BEGIN \r\n" + 
				"	INSERT INTO Pointers (url1_id,url2_id) Values('"+parent+"','"+link+"');\r\n" + 
				"END;\r\n"+ 
				"END;\r\n";;
		mDB.executeQuery(this.insertQuery);
	}

 
    public static boolean isValid(String url) 
    { 
        /* Try creating a valid URL */
        try { 
            new URL(url).toURI(); 
            return true; 
        } 
          
        // If there was an Exception 
        // while creating URL object 
        catch (Exception e) { 
            return false; 
        } 
    }
    
    ///////////////////////////////ranking//////////////////////////////////
    
    
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
    
    public void setPR(ArrayList<Float> Pr,ArrayList<String> url)throws SQLException {
    	String query= "";
    	System.out.println(Pr.size());
    	for(int i=0;i<Pr.size();i++) {
    	 query+="update websites set PR="+Pr.get(i)+" where URL= '"+url.get(i)+"';\r\n";
    	
    	}
    	mDB.executeQuery(query);
    }
    
    public void rankTf() throws SQLException {
		rank = new Ranker();
		
		String query = "select *\r\n" + 
				"	from websites join words_websites\r\n" + 
				"	on websites.URL = words_websites.URL;";
		
		ResultSet words = mDB.getResult(query);
		query ="select count (*) from websites;";
		ResultSet c= mDB.getResult(query);
		int totDoc=0;
		while(c.next())
		{
			 totDoc =  Integer.parseInt(c.getString(""));
		}
		
		//to concatenate query and execute them all
		String update_query = "";
		int occur=0;
		int totWord = 0,docOccur = 0;
		double score;
		
		while (words.next()) {
			occur = Integer.parseInt(words.getString("total_occur"));
			totWord= Integer.parseInt(words.getString("size"));
			query= "select count(*) from words_websites where word_id="+ words.getString("word_id")+";";
			c = mDB.getResult(query);			
			while(c.next())
			{
				 docOccur =  Integer.parseInt(c.getString(""));
			}
			score = rank.tfIdf(occur, totWord, totDoc, docOccur);
			System.out.println(score);
			update_query += "UPDATE words_websites SET score="+ score +"WHERE (URL = '"+words.getString("URL")+"' and word_id = "+words.getString("word_id")+");\r\n";
		}
		mDB.executeQuery(update_query);
		
	}
    
    
    

}
	  
