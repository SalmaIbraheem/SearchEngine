$(document).ready(function(){
	
	$("#myInput").keyup(function(){
		if ($(this).val.length > 0) {
			$.ajax({
			type: "GET",
			url: "SuggestServlet",
			contentType : "application/json", dataType : 'json',
	        data:{"query":$("#myInput").val()},
			success: function(data){
				closeAllLists();
	            $.each(data, function(index, item) {
	            	var $select = $("#autocomplete-list");
	            	$("<input disabled>").val(item.query).appendTo($select);	
	            });     	
			},
			});
		}
		else {
			alert("hello");
			closeAllLists();
		}
		
	}); 
	
	document.querySelector('#autocomplete-list').addEventListener('click', function(event) {
		  if (event.target.tagName.toLowerCase() === 'input') {
			  document.querySelector('#myInput').value = event.target.value;
			  closeAllLists();
		  }
		});	

		document.querySelector('html').addEventListener('click', function(event) {
			  if (event.target.tagName.toLowerCase() != 'input') {
				  closeAllLists();
			  }
		});	

});


function closeAllLists() {
	$("#autocomplete-list input").remove(); 
}
