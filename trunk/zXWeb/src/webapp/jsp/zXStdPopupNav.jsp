<% 
//----
// File	:	zXStdPopupNav.asp
// By	:	David Swann
// Date	:	FEB2004
//
// Navigation for standard popups
//
// -title - Text (shown vertically)
//
//----
%>
<html>
<head>
	<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">
	<title></title>
</head>
<body class="zxVertical">
<table width="100%" height="100%" border=0>
	<tr height="*">
		<td class="zxVertical" style="writing-mode: tb-rl; filter:flipH() flipV();" align="right" >
<%=request.getParameter("-title")%>
		</td>
	</tr>
</table>
</body>
</html>