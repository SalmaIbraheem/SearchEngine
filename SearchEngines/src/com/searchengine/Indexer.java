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

public class Indexer {
	public static void index(String url) throws IOException, SQLException
	{
		DBManager d = new DBManager();
		Document doc = Jsoup.connect(url).get();
		String data = doc.body().text();
		//System.out.println(data);
		
		//split words and remove stop words
		List<String>stopwords = Files.readAllLines(Paths.get("stop_words.txt"));
		String REGEX = "\\s+|\\s*\\,\\s*|\\s*\\.\\s*|\\s*\\&\\s*|\\s*\\$\\s*|\\s*\\;\\s*|\\s*\\:\\s*|\\s*\\(\\s*|\\s*\\)\\s*"+
		"|\\%|\\^|\\*|\\!\\?|\\>|\\<|\\=|\\+|\\-|\\±|\\\\|\\\"|\\[|\\]|\\{|\\}|\\/|\\'"; 
		ArrayList<String> allWords = 
			      Stream.of(data.toLowerCase().split(REGEX))
			            .collect(Collectors.toCollection(ArrayList<String>::new));
		d.insert_words_count(url, allWords.size());
		
		allWords.removeAll(stopwords);
		allWords.removeIf(s->(s.isEmpty()||s.matches("\\d+")));
		
		d.insert_words(allWords, url);
		/*for(String s :allWords)
		{
			System.out.println(s);
		}*/
	}

	public static void main(String[] args) throws IOException, SQLException {
		String u = "http://calendar.mit.edu/";
		index(u);
		/*DBManager d = new DBManager();
		ArrayList<String>w = new ArrayList<String>();
		w.add("play");
		w.add("home");
		w.add("play");
		d.insert_words(w, u);*/
		
	}
}
