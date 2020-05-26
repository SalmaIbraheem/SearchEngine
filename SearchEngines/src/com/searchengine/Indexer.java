package com.searchengine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Indexer implements Runnable {
	private  String url="";
	List<String>stopwords;
	UrlList list;
	public Indexer (UrlList u,List<String>s)
	{
		this.list = u;
		this.stopwords = s;
	}
	
	public  void index(String url) throws IOException, SQLException
	{
		//System.out.println(url);
		if(url==null)
			return;
		if(url.isBlank())
			return;
		DBManager d = new DBManager();
		//parse html document
		//System.out.println(url);
		Document doc = Jsoup.connect(url).get();
		//contains body of the document
		String data = doc.body().text(); 
		
		
		//split words and remove stop words
		
		String REGEX = "\\s+|\\s*\\,\\s*|\\s*\\.\\s*|\\s*\\&\\s*|\\s*\\$\\s*|\\s*\\;\\s*|\\s*\\:\\s*|\\s*\\(\\s*|\\s*\\)\\s*"+
		"|\\%|\\^|\\*|\\!\\?|\\>|\\<|\\=|\\+|\\-|\\�|\\\\|\\\"|\\[|\\]|\\{|\\}|\\/|\\'"; 
		ArrayList<String> allWords = 
			      Stream.of(data.toLowerCase().split(REGEX))
			            .collect(Collectors.toCollection(ArrayList<String>::new));
		//add total words count of the url
		d.insert_words_count(url, allWords.size());
		
		//remove stopwords
		allWords.removeAll(stopwords);
		allWords.removeIf(s->(s.isEmpty()||s.matches("\\d+")));
		
		//insert all words to database
		d.insert_words(allWords, url);
		/*for(String s :allWords)
		{
			System.out.println(s);
		}*/
	}

	
	@Override
	public void run() {
		
		try {
			while(!list.get_stopping_criteria())
				index(list.getNewUrl());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
