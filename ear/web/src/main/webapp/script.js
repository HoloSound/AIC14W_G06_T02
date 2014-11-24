
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
});


var callback = function(dataReceived){

	alert(dataReceived);
};
