package com.myclasses;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.stemmer.PorterStemmer;   

public class Stemmer {
	PorterStemmer porterStemmer;
	private static List<String> stopWords;
	
	public Stemmer() throws IOException
	{
		porterStemmer = new PorterStemmer();
		Path pathToFile = Paths.get("stop_words.txt");
	    System.out.println(pathToFile.toAbsolutePath());
		stopWords = Files.readAllLines(Paths.get("stop_words.txt"));
	}
	
    
	public ArrayList<String> stem(ArrayList<String> str)
	{
		str.removeAll(stopWords);
		str.removeAll(Arrays.asList("", null));
		
		for(int i=0; i<str.size(); i++)
		{
			str.get(i).replaceAll("[-+.^:,!?]","");
		}
	
		
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0; i<str.size(); i++)
		{
			String w = porterStemmer.stem(str.get(i));
			result.add(w);
		}
		
		return result;
	}
	
}
