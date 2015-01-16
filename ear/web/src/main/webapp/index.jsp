<?xml version="1.0" encoding="UTF-8" ?>
<%@page import="javax.ejb.EJB"%>
<%@page import="at.tuwien.aic.raid.sessionbean.RaidSessionBeanInterface"%>

<%!at.tuwien.aic.raid.web.Controller c = new at.tuwien.aic.raid.web.Controller();%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<script
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>

<STYLE type="text/css">
a {
	text-decoration: none;
	color: black
}

#raid1Div {
	float: left;
	width: 1024px;
	min-height: 600px;
	border: 1x solid black;
	border: 1px solid black;
}

#raid5Div {
	float: left;
	width: 1024px;
	min-height: 600px;
	border: 1px solid black;
}

ul {
	list-style-type: none;
	padding: 0px;
}

li {
	background-color: gold;
	float: left;
	border: 1px solid black;
	padding: 2px;
	margin: 0px;
	border-bottom: 0px solid;
}

#content {
	width: 1024px;
	margin-left: auto;
	margin-right: auto
}
</STYLE>

<SCRIPT type="text/javascript" src="script.js">
	
</script>




</head>
<BODY>

	<DIV id="content">

		<ul>
			<li id='raid1li'><a href='javascript:void(0)'
				onclick="showRaid1()" title="RAID1">RAID1</a></li>
			<li id='raid5li'><a href='javascript:void(0)'
				onclick="showRaid5()" title="RAID5.3">RAID5</a></li>
		</ul>

		<DIV>
			<div id='raid1Div'>
				<div class="uploadDiv">
					
					<form action="raid1?task=upload" method="post"
						enctype="multipart/form-data">
						<input id="raid1FileInput" type="file" name="file" size="50" /> <br />
						<input type="submit" value="Upload File" />
					</form>
					<a href="javascript:void" onclick="reloadRaid1();" ><img src="/web/pic/reload.png" alt="reload"/>&nbsp;RELOAD&nbsp;</a>
					<br /><br />
				</div>

				<%=c.listFiles()%>
			</div>

			<div id='raid5Div'>
				<div class="uploadDiv">

					<form action="raid5?task=upload" method="post"
						enctype="multipart/form-data">
						<input id="raid5FileInput" type="file" name="file" size="50" /> <br />
						<input type="submit" value="Upload File" />
					</form>
<a href="javascript:void" onclick="reloadRaid5();" ><img src="/web/pic/reload.png" alt="reload"/>&nbsp;RELOAD&nbsp;</a>
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


			</div>

		</div>
	</DIV>

</BODY>

</html>
