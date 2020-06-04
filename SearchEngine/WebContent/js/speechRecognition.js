$(function () {
	  try {
	    var recognition = new webkitSpeechRecognition();
	  } catch (e) {
	    var recognition = Object;
	  }
	  recognition.continuous = true;
	  recognition.interimResults = true;
	  recognition.onresult = function (event) {
	    var txtRec = '';
	    for (var i = event.resultIndex; i < event.results.length; ++i) {
	      txtRec += event.results[i][0].transcript;
	    }
	    $('#myInput').val(txtRec);
	  };
	  $('#mic').click(function () {
	    $('#myInput').focus();
	    recognition.start();
	  });
	  $('#stop').click(function () {
	    recognition.stop();
	  });
	});