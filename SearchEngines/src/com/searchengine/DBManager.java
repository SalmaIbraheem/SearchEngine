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

public class DBManager {

	
	private JDataBase mDB;
	private Tf_idf rank;
	private static final String[] mSeeds = {"http://www.mit.edu/","http://www.mit.edu/#main"};
	private static final int mNumberOfSeeds = 2;
	private boolean interrupt = false;
	private String insertQuery = "";
	private int insertNumbers = 0;
	private int crawledLinks = 0;
	  
	  
	public DBManager() throws SQLException {
		mDB = new JDataBase();
		
		String queryString= "if (object_id('websites', 'U') is null) \r\n" + 
				"begin \r\n" + 
				"CREATE TABLE websites( id int not null IDENTITY(1,1) , \r\n" + 
				"						URL varchar(3000) not null,\r\n" + 
				"						crawled int DEFAULT 0,\r\n" + 
				"						size int not null,\r\n" + 
				"						childern int DEFAULT 0,\r\n" + 
				"						primary key (URL)); \r\n" + 
				"end;\r\n";
		
		queryString += "if (object_id('Pointers', 'U') is null) \r\n" + 
				"begin  \r\n" + 
				"CREATE TABLE Pointers(url1_id varchar(3000) not null,\r\n" + 
				"					  url2_id varchar(3000) not null,\r\n" + 
				"					  foreign key (url2_id) references websites(URL));\r\n" + 
				"end;";
		
		mDB.executeQuery(queryString);
		//words table
		queryString = "if(object_id('words','U') is null)"+
		"begin \r\n"+
		"create table words (id int not null IDENTITY(1,1) primary key,stem varchar(50) not null ,word varchar(50) not null); "+
		"end;";
		mDB.executeQuery(queryString);
		
		//words_websites table
		queryString = "if(object_id('words_websites','U') is null)"+
		"begin \r\n"+
		"create table words_websites (word_id int not null,URL varchar(3000) not null,score float DEFAULT 0,"+
		"total_occur int DEFAULT 0,first_position int DEFAULT 0 "+
		",FOREIGN KEY (word_id) REFERENCES words(id) "+
		" ,FOREIGN KEY (URL) REFERENCES websites(URL)"+
		", CONSTRAINT p_key PRIMARY KEY(word_id,URL))"+
		"end;";
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
	private void insertSeeds() {
		for(int i = 0; i < mNumberOfSeeds; i++) {
			mDB.executeQuery("IF NOT EXISTS (Select* FROM websites WHERE (URL = '"+mSeeds[i]+"'))\r\n" + 
					"BEGIN\r\n" + 
					"	\r\n" + 
					"	INSERT INTO websites (\"URL\",\"size\")VALUES ('"+mSeeds[i]+"',"+mSeeds[i].length()+");\r\n" + 
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
	public void crawlPage(String URL) throws SQLException, IOException{
		Document doc = Jsoup.connect(URL).get();
		int rejectedLinks = 0;					//links has no permissions to be showed
		
		//get all links and recursively call crawlPage
		Elements hyberLinks = doc.select("a[href]");
		System.out.println(hyberLinks.size());
		//sign it as crawled and insert number of its hyberlinks - websites with no permission
		String query = "UPDATE websites SET childern ="+hyberLinks.size()+" WHERE (URL = '"+URL+"');";
		mDB.executeQuery(query);
		
		for(Element link: hyberLinks){
			hyberLinks.size();
			if(crawledLinks < 5000) {
				if(checkRobot(link.attr("abs:href"))) {
					addLink(URL,link.attr("abs:href"));
					if(insertNumbers < 10 && insertNumbers != 0)mDB.executeQuery(insertQuery);
					crawledLinks++;
				}else {
					rejectedLinks++;
				}
			}
		}
	}
	
	private void addLink(String parent, String link) throws IOException, SQLException {
		//get permission from robots.txt
		System.out.println(link);
		//access only web site with type "HTML"
		if(Jsoup.connect(link).get().documentType().name().equals("html")) {
			//check if it's already in database
			this.insertQuery +="IF NOT EXISTS (Select* FROM websites WHERE (URL = '"+link+"'))\r\n" + 
					"BEGIN\r\n" + 
					"	INSERT INTO websites (\"URL\",\"size\")VALUES ('"+link+"',"+link.length()+");" + 
					"END;\r\n";
			//add to the relation between urls 
			this.insertQuery += "IF NOT EXISTS (Select* FROM Pointers WHERE (url1_id = '"+parent+"' AND url2_id = '"+link+"')) \r\n" + 
					"BEGIN \r\n" + 
					"	INSERT INTO Pointers (url1_id,url2_id) Values('"+parent+"','"+link+"')\r\n" + 
					"END;\r\n";
			
			insertNumbers++;
			if(insertNumbers == 10) {
				insertNumbers = 0;
				mDB.executeQuery(insertQuery);
			}
		}
	}
	public boolean checkRobot(String URL) {
		
		return true;
		
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
    public void getWords() throws SQLException {
		rank = new Tf_idf();
		
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
	  
