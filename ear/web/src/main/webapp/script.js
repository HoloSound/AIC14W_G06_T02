function loadFileInfo(name,td){
	var a= jQuery("#"+td);
	a.empty();
	a.append("loading");
	a.load("raid1?task=fileInfo&fileName="+encodeURIComponent(name));
}


function showRaid1(){
jQuery("#raid5Div").fadeOut('fast',function(){jQuery("#raid1Div").fadeIn();});

jQuery("#raid1li").css("font-weight","800");
jQuery("#raid5li").css("font-weight","initial");
}

function showLoadingInFileList(){
	$('#raid1Div table' ).empty();
	$('#raid1Div table' ).append("<strong>LOADING</strong>")
}
$( document ).ready(function() {
	$( '#raid1Div form' )
	  .submit( function( e ) {
	
        if($("#raid1FileInput").val()==''){
        	alert("Please select a file");
    	    e.preventDefault();
        	return;
        }
    	$("#raid1Div :submit").prop("disabled",true);
	    var url =$(this).attr("action");
	  
	    $.ajax( {
	      url: 'raid1?task=upload',
	      type: 'POST',
	      data: new FormData( this ),
	      processData: false,
	      contentType: false,
	      success: function(data)
	           {
	    	       $("#raid1Div :submit").prop("disabled",false);
	               alert(data); // show response from the php script.
	               reloadRaid1();
	              
	           },
		    error: function(data)
	        {
	 	       $("#raid1Div :submit").prop("disabled",false);
	 	      alert("error "+data);
	        }
  

	    } );
	    e.preventDefault();
	  } );
});

var callback1 = function(dataReceived){

	alert(dataReceived);
	showLoadingInFileList();
	 $('#raid1Div table' ).load("raid1?task=list" );//RELOAD LIST
	 
};


///////////////////////////RAID5

function showLoadingInFileList5(){
	$('#raid5Div table' ).empty();
	$('#raid5Div table' ).append("<strong>LOADING</strong>")
}

function showRaid5(){
	jQuery("#raid1Div").fadeOut('fast',function(){jQuery("#raid5Div").fadeIn();});

	jQuery("#raid1li").css("font-weight","initial");
	jQuery("#raid5li").css("font-weight","800");
	}

$( document ).ready(function() {
	$('#raid5Div table' ).load("raid5?task=list" );//RELOAD LIST in 5
	showRaid1()
	
	$( '#raid5Div form' )
	  .submit( function( e ) {
	
        if($("#raid5FileInput").val()==''){
        	alert("Please select a file");
    	    e.preventDefault();
        	return;
        }
    	$("#raid5Div :submit").prop("disabled",true);
	    var url =$(this).attr("action");
	  
	    $.ajax( {
	      url: 'raid5?task=upload',
	      type: 'POST',
	      data: new FormData( this ),
	      processData: false,
	      contentType: false,
	      success: function(data)
	           {
	    	       $("#raid5Div :submit").prop("disabled",false);
	               alert(data); // show response from the php script.
	               reloadRaid5();
	           },
		    error: function(data)
	        {
	 	       $("#raid5Div :submit").prop("disabled",false);
	 	      alert("error "+data);
	        }
  

	    } );
	    e.preventDefault();
	  } );
	
	
	
});


function reloadRaid5(){
		 showLoadingInFileList5();
	     $('#raid5Div table' ).load("raid5?task=list" );//RELOAD LIST
}
function reloadRaid1(){
	 showLoadingInFileList();
     $('#raid1Div table' ).load("raid1?task=list" );//RELOAD LIST
}
var callback5 = function(dataReceived){

	alert(dataReceived);
	showLoadingInFileList5();
	 $('#raid5Div table' ).load("raid5?task=list" );//RELOAD LIST
	 
};
