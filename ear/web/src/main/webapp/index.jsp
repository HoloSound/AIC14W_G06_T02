<?xml version="1.0" encoding="UTF-8" ?>
<%@page import="javax.ejb.EJB"%>
<%@page import="at.tuwien.aic.raid.sessionbean.RaidSessionBeanInterface"%>

<%!
at.tuwien.aic.raid.web.Controller c=new at.tuwien.aic.raid.web.Controller(); %>
<html	xmlns="http://www.w3.org/1999/xhtml">
<head>

 <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>

<STYLE type="text/css">
a{text-decoration: none;color:black}
#raid1Div{
  float:left;
   width:1024px;
 	min-height:600px;
  border:1x solid black;
  border: 1px solid black;
}
#raid5Div{
   float:left;
  width:1024px;
  min-height:600px;
  border: 1px solid black;
}
ul{
  list-style-type: none;
  padding:  0px;
 }
li{
background-color: gold;
  float:left;
  border: 1px solid black;
  padding:  2px;
  margin: 0px;
    border-bottom: 0px solid;
}
#content{   width:1024px;margin-left:auto;margin-right:auto}
</STYLE>

<SCRIPT type="text/javascript">

function showRaid1(){
jQuery("#raid5Div").hide();
jQuery("#raid1Div").show();
jQuery("#raid1li").css("font-weight","800");
jQuery("#raid5li").css("font-weight","initial");
}
function showRaid5(){
jQuery("#raid1Div").hide();
jQuery("#raid5Div").show();
jQuery("#raid1li").css("font-weight","initial");
jQuery("#raid5li").css("font-weight","800");
}
$( document ).ready(function() {
	showRaid1()
});

</SCRIPT>
</head>
<BODY>

<DIV id="content">


<ul><li id='raid1li'><a href='javascript:void(0)' onclick="showRaid1()">RAID1</a></li> <li id='raid5li'> <a href='javascript:void(0)' onclick="showRaid5()" >RAID5</a></li> </ul>

<DIV>
<div id='raid1Div'>
	<%=c.sayHello()%>
</div>

<div id='raid5Div'>
	TODO	

</div>
</DIV>
</DIV>
</BODY>
	
</html>