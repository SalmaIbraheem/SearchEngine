<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.util.ArrayList,com.myclasses.Result,com.myclasses.DataHandler,com.myclasses.JDataBase" %>
<!DOCTYPE html>
<%
DataHandler dataHandler = new DataHandler();
String query = dataHandler.getQuery(); %>
<html>
	<head>
		<meta charset="ISO-8859-1">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
		<link href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk" crossorigin="anonymous">
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
		<link href='https://fonts.googleapis.com/css?family=Caveat Brush' rel='stylesheet'>
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
		<link href="css/searchResult.css?newversion" rel="stylesheet" type="text/css" />
		<script src="https://code.jquery.com/jquery-3.5.1.js" integrity="sha256-QWo7LDvxbWT2tbbQ97B53yJnYU3WhH/C8ycbRAkjPDc=" crossorigin="anonymous"></script>
		<script type="text/javascript" src="http://code.jquery.com/jquery-latest.js"></script>
		<script type="text/javascript" src="js/suggest.js?newversion"></script>
		<script type="text/javascript" src="js/speechRecognition.js"></script>
		<script type="text/javascript" src="js/personalize.js"></script>
		<title><%=query%></title>
	</head>
	<body>	
	
		<div class="searchBar">
		    <a href="main.jsp" id="logo"><img src="https://fontmeme.com/permalink/200603/ff1c3097cde20ffd0bc903b76c22fadd.png"></a>
		    <form class="form-inline" method="get" action="<%= request.getContextPath()%>/SearchRequest">
		    	<div class="autocomplete">
		    		<button  type="button" class="btn btn-primary btn-circle" id="mic"><i class="fa fa-microphone"></i> <i class="glyphicon glyphicon-list"></i></button>
				    <input autocomplete="off" id="myInput" type="search" name="query" value="<%=query%>" aria-label="Search">
				    <div id='autocomplete-list' class='autocomplete-items'></div>
			    </div>
			    <button class="button">search</button>
		    </form>
   		</div>
   		
   		<br style="clear: both;" /> 
   		<hr>
		<div class="container">
			<%
			int pageNo = Integer.parseInt(request.getParameter("page"));
			int limit = 10;
			JDataBase j = new JDataBase();
			ArrayList<Result> list = dataHandler.getResultList(pageNo);
			int resultLen = dataHandler.getNoOfResults(pageNo);
			int noOfPages = (int) Math.ceil(resultLen * 1.0 / limit);
			
			if(resultLen>0) {
				  for (Result post: list) {
			%>	
		<div class="result">
			
			    <h4><a onClick="callServlet(this);" href="<%=post.url%>"><%=post.title%></a></h4>
			    <p><%=post.text%></p>
			   
		</div>
		<%}} else {%>
		<p>your search didn't match any documents</p>
		<%}%>
		</div> 
		<% dataHandler.insertQuery(pageNo); %>
		<div class="pages">
			<% for (int i=1; i <=noOfPages; i++) {  %>
				<a href="searchResult.jsp?page=<%= i %>"><%= i %></a>
			<%} %>
		</div>
	 </body>
</html>