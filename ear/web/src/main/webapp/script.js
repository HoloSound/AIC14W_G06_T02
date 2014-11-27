
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
$( document ).ready(function() {
	showRaid1()
	
	$( 'form' )
	  .submit( function( e ) {

	    var url =$(this).attr("action");

	    $.ajax( {
	      url: 'raid1?task=upload',
	      type: 'POST',
	      data: new FormData( this ),
	      processData: false,
	      contentType: false,
	      success: function(data)
	           {
	               alert(data); // show response from the php script.
	               $('#raid1Div table' ).load("raid1?task=list" );//RELOAD LIST
	           }


	    } );
	    e.preventDefault();
	  } );
});


var callback = function(dataReceived){

	alert(dataReceived);
	 $('#raid1Div table' ).load("raid1?task=list" );//RELOAD LIST
};
