<?xml version="1.0" encoding="UTF-8" ?>
<%@page import="javax.ejb.EJB"%>
<%@page import="at.tuwien.aic.raid.sessionbean.RaidSessionBeanInterface"%>

<%!at.tuwien.aic.raid.web.Controller c = new at.tuwien.aic.raid.web.Controller();%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<link rel="stylesheet" type="text/css" href="/web/pic/navbar.css" />
<link rel="stylesheet" type="text/css"	href="/web/pic/bootstrap.css" />
<link rel="stylesheet" type="text/css"	href="/web/pic/bootstrap-icons.css" />

<script type="text/javascript" src="/resources/js/bootstrap.js"></script>

<script
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>

<STYLE type="text/css">
a {
	text-decoration: none;
	color: black
}






</STYLE>

<SCRIPT type="text/javascript" src="script.js">
	
</script>




</head>
<BODY>

	<DIV id="content" class="container">
	<div class="alert alert-info-transparent ">

		<ul class="nav nav-tabs" id="myTab" >
		<li class="raid1li"><a class="btn-primary btn-sm" href="#raid1Div" data-toggle="tab">RAID5</a></li> 
			<!--  li id='raid1li'><a href='javascript:void(0)'
				onclick="showRaid1()" title="RAID1">RAID1</a></li> -->
		
			<!--li id='raid5li'><a href='javascript:void(0)'
				onclick="showRaid5()" title="RAID5.3">RAID5</a></li -->
		</ul>

		<DIV>
		<br /><br />
			<div id='raid1Div' class="row tab-pane active top-bottom-distance" >
				<div class="col-md-3 uploadDiv" align="center">
				
				<a href="javascript:void" class="btn btn-lg btn-warning top-bottom-distance" onclick="reloadRaid1();"  ><img src="/web/pic/sync.png" alt="reload"/>&nbsp;RELOAD&nbsp;</a>
					<br /><br />
					
				

					<form class="top-bottom-distance" action="raid1?task=upload" method="post"
						enctype="multipart/form-data">
						<div class="form-group">
						 <div class="btn btn-info btn-sm top-bottom-distance">
						<input id="raid1FileInput" type="file" name="file"  /> 
						<input type="submit" class="btn btn-success" value="Upload File" />
						</div>
						 </div>
					</form>
					
					
					
				</div>
				
				<div class="col-md-9 ">

				<%=c.listFiles()%>
				.
				</div>
			</div>

			<!-- div id='raid5Div' class="tab-pane" id="kitchen_bathroom">
				<div class="uploadDiv">

					<form action="raid5?task=upload" method="post"
						enctype="multipart/form-data">
						<input id="raid5FileInput" type="file" name="file" size="50" /> <br />
						<input type="submit" value="Upload File" />
					</form>
<a href="javascript:void" onclick="reloadRaid5();" ><img src="/web/pic/sync.png" alt="reload"/>&nbsp;RELOAD&nbsp;</a>
					<br /><br />
					<table border="1">
						<colgroup>
							<col width="200">
							<col width="100">
							<col width="100">
						</colgroup>
						<thead>
							<tr>
								<td><strong>FileName</strong></td>
							</tr>
						</thead>
						<tbody>
						
						</tbody>
					</table>
				</div>


			</div -->

		</div>
		</div>
	</DIV>

</BODY>

</html>
