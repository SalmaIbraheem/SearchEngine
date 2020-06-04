package com.servlets;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.myclasses.DBManager;
/**
 * Servlet implementation class SuggestServlet
 */
@WebServlet("/SuggestServlet")
public class SuggestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DBManager dbM = new DBManager() ;   
    /**
     * @throws IOException 
     * @throws SQLException 
     * @see HttpServlet#HttpServlet()
     */
    public SuggestServlet() throws SQLException, IOException {
        super();
        System.out.println("cons1");
        
        System.out.println("cons2");
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONArray jarray = new JSONArray();
		String str = request.getParameter("query").toLowerCase();
		String tmp = str.replaceAll(" ", "");
		
		
		try {
			jarray = dbM.getQueries(str);
			String JsonString = jarray.toJSONString();
			
			response.setContentType("application/json");
			response.getWriter().write(JsonString);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
