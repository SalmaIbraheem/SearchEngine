  function callServlet(ele) {
	  var linkClicked = ele.href; 
	  $.ajax({
			type: "GET",
			url: "PersonalizeServlet",
	        data:{"link":linkClicked},
			success: function(data){
			}
			});
}