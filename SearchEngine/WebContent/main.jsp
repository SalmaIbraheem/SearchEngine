<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
		<link href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk" crossorigin="anonymous">
		<link href='https://fonts.googleapis.com/css?family=Caveat Brush' rel='stylesheet'>
		<link href="css/main.css" rel="stylesheet" type="text/css" >
		<title>angelica</title>
		<script src="https://code.jquery.com/jquery-3.5.1.js" integrity="sha256-QWo7LDvxbWT2tbbQ97B53yJnYU3WhH/C8ycbRAkjPDc=" crossorigin="anonymous"></script>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
		<script type="text/javascript" src="http://code.jquery.com/jquery-latest.js"></script>
		<script type="text/javascript" src="js/suggest.js?newversion"></script>
		<script type="text/javascript" src="js/speechRecognition.js"></script>
</head>
<body>
<img src="https://fontmeme.com/permalink/200602/b60bcb254a593cb4b8772f2b96601afc.png">
 <form method="get" action="<%= request.getContextPath()%>/SearchRequest">
 		
			<div class="autocomplete">
				<button  type="button" class="btn btn-primary btn-circle" id="mic"><i class="fa fa-microphone"></i> <i class="glyphicon glyphicon-list"></i></button>
			    <input autocomplete="off" id="myInput" type="text" name="query" placeholder="what are you looking for?">
			    
			    <div id='autocomplete-list' class='autocomplete-items'></div>
			</div>
  			<button class="button" class="searchButton" placeholder="country">search</button>
  			<select id="country" name="country">
  			<option value="none" selected disabled hidden> 
          country
      </option> 
   <option value="dz">Algeria</option>
   <option value="bh">Bahrain</option>
   <option value="br">Brazil</option>
   <option value="ca">Canada</option>
   <option value="td">Chad</option>
   <option value="cr">Costa Rica</option>
   <option value="cu">Cuba</option>
   <option value="eg">Egypt</option>
   <option value="ee">Estonia</option>
   <option value="et">Ethiopia</option>
   <option value="hk">Hong Kong</option>
   <option value="hu">Hungary</option>
   <option value="is">Iceland</option>
   <option value="ir">Iran</option>
   <option value="iq">Iraq</option>
   <option value="jp">Japan</option>
   <option value="jo">Jordan</option>
   <option value="kp">Korea North</option>
   <option value="kr">Korea South</option>
   <option value="kw">Kuwait</option>
   <option value="lb">Lebanon</option>
   <option value="ly">Libya</option>
   <option value="om">Oman</option>
   <option value="pk">Pakistan</option>
   <option value="pt">Portugal</option>
   <option value="pr">Puerto Rico</option>
   <option value="qa">Qatar</option>
   <option value="ro">Romania</option>
   <option value="ru">Russia</option>
   <option value="se">Sweden</option>
   <option value="sy">Syria</option>
   <option value="tn">Tunisia</option>
   <option value="tr">Turkey</option>
   <option value="uk">United Kingdom</option>
   <option value="ua">Ukraine</option>
   <option value="us">United States of America</option>
</select>
  			
  			
</form>

</body>
</html>