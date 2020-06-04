package com.servlets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.myclasses.DBManager;
import  com.myclasses.Result;
import  com.myclasses.Stemmer;
  

/**
 * Servlet implementation class SearchRequest
 */
@WebServlet("/SearchRequest")
public class SearchRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static ArrayList<String> queryWords;
	public static String query;
	public static String phrase;
	public static boolean phraseFlag;
	Stemmer stemmer = new Stemmer();
	public static String country = "eg";

    /**
     * Default constructor. 
     * @throws IOException 
     */
    public SearchRequest() throws IOException,SQLException {
    	
    	
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		query = request.getParameter("query");
		
		if(request.getParameter("country") != null)
			country = request.getParameter("country");
		
		String line = query;
		Pattern p = Pattern.compile("\"([^\"]*)\"");
		Matcher m = p.matcher(line);
		
		if(m.find()) 
		{
			query = query.replaceAll("\"", "");
			phraseFlag = true;
			
			phrase = m.group(1);
		
			line  = line.replaceAll(m.group(1),"");
			line  = line.replace("\"","");
			queryWords =  Stream.of(line.split("\\s+")).collect(Collectors.toCollection(ArrayList<String>::new));
			queryWords.addAll(Stream.of(phrase.split("\\s+")).collect(Collectors.toCollection(ArrayList<String>::new)));
			
			for(int i=0; i<queryWords.size(); i++)
				queryWords.set(i, queryWords.get(i).toLowerCase());
			
			queryWords = stemmer.stem(queryWords);
			
			phrase = phrase.replaceAll("\\s+","");
		}
		else {
			phraseFlag = false;
			queryWords =  Stream.of(query.toLowerCase().split(" ")).collect(Collectors.toCollection(ArrayList<String>::new));
			queryWords = stemmer.stem(queryWords);
		
		}
	
		response.sendRedirect(request.getContextPath()+"/searchResult.jsp?page=1");
	}

}
