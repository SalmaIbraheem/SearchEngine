package com.servlets;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.myclasses.DBManager;

/**
 * Servlet implementation class PersonalizeServlet
 */
@WebServlet("/PersonalizeServlet")
public class PersonalizeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DBManager mDB;
       
    /**
     * @throws SQLException 
     * @see HttpServlet#HttpServlet()
     */
    public PersonalizeServlet() throws IOException, SQLException {
    	super();
    	mDB = new DBManager();
        
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String link = request.getParameter("link");
		
		URL web = new URL(link);
		String temp;
		temp= web.getHost();
		
		mDB.setVisits(temp);
		response.getWriter().write("yes");
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
	}

}
