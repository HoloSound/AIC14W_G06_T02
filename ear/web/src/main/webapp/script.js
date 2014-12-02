
function showRaid1(){
jQuery("#raid5Div").fadeOut('fast',function(){jQuery("#raid1Div").fadeIn();});

jQuery("#raid1li").css("font-weight","800");
jQuery("#raid5li").css("font-weight","initial");
}
function showRaid5(){
jQuery("#raid1Div").fadeOut('fast',function(){jQuery("#raid5Div").fadeIn();});

jQuery("#raid1li").css("font-weight","initial");
jQuery("#raid5li").css("font-weight","800");
}


function showLoadingInFileList(){
	$('#raid1Div table' ).empty();
	$('#raid1Div table' ).append("<strong>LOADING</strong>")
}
$( document ).ready(function() {
	showRaid1()
	
	$( 'form' )
	  .submit( function( e ) {
		$("#raid1Div :submit").prop("disabled",true);
        if($("#raid1FileInput").val()==''){
        	alert("Please select a file");
    	    e.preventDefault();
        	return;
        }
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
	               showLoadingInFileList();
	               $('#raid1Div table' ).load("raid1?task=list" );//RELOAD LIST
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


var callback = function(dataReceived){

	alert(dataReceived);
	showLoadingInFileList();
	 $('#raid1Div table' ).load("raid1?task=list" );//RELOAD LIST
};
