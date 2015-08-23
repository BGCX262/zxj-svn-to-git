<%
//====================================================================
// Standard popup page
//
// -s - Session-id
// -ss (optional) - subsession-id
// -noNavigationFrame - Whether to have a navigation frame.
// -downLoad - whether there is a downLoad ie : a word attachment or something in the frame.
//====================================================================
%><%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><zx:zx/><% 
ZX objZX = null;
PageBuilder objPage = null; 
try {
	objZX = ThreadLocalZX.getZX();
	objPage = new PageBuilder(); 
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>
<%
	String strDownload = request.getParameter("-downLoad");
	if (org.zxframework.util.StringUtil.len(strDownload) == 0) {
		strDownload = "";
	} else {
		strDownload = "&-downLoad=" + strDownload;
	}
	
	if (StringUtil.len(request.getParameter("-noNavigationFrame")) == 0) {
		/**
		 * DGS24JAN2003: fraFooter also has a querystring item for download. Same for the 'else' too.
		 */
%>
<frameset rows="*,55" name="fraTop" frameborder="NO" border="1" framespacing="0"> 
	<frameset cols="10%,*" name="fraDetailsOuterSet" >
		<frame name="fraLeft" src="../html/zXLeft.html" scrolling="no" frameborder="0">
		<frameset rows="*,1,1,1,1,1" name="fraDetailsInnerSet" frameborder="no" border="0" framespacing="0">
		    <frame name="fraDetailsMenu" src="../html/zXPopup.html" marginwidth="10" marginheight="10" scrolling="auto" frameborder="0">
			<frame name="fraDetails1" src="../html/zXBlank.html" scrolling="auto" frameborder="0" bordercolor="White">
			<frame name="fraDetails2" src="../html/zXBlank.html" scrolling="auto" frameborder="0" bordercolor="White">
			<frame name="fraDetails3" src="../html/zXBlank.html" scrolling="auto" frameborder="0" bordercolor="White">
			<frame name="fraDetails4" src="../html/zXBlank.html" scrolling="auto" frameborder="0" bordercolor="White">
			<frame name="fraDetails5" src="../html/zXBlank.html" scrolling="auto" frameborder="0" bordercolor="White">
		</frameset>
	</frameset>
	<frame name="fraFooter" src="../jsp/zXFooter.jsp?-popup=Y&-s=<%=request.getParameter("-s")%>&-ss=<%=request.getParameter("-ss")%><%=strDownload%>" vspace="10" marginwidth="10" marginheight="10" scrolling="no" frameborder="0" noresize>
</frameset>
<%
	} else {
%>
<frameset rows="*,55" name="fraTop" frameborder="NO" border="1" framespacing="0"> 
	<frameset cols="1,*" name="fraDetailsOuterSet" >
		<frame name="fraLeft" src="../html/zXBlank.html" scrolling="no" frameborder="0">
		<frameset rows="*,1,1,1,1,1" name="fraDetailsInnerSet" frameborder="no" border="0" framespacing="0">
		    <frame name="fraDetailsMenu" src="../html/zXPopup.html" marginwidth="10" marginheight="10" scrolling="auto" frameborder="0">
			<frame name="fraDetails1" src="../html/zXBlank.html" scrolling="auto" frameborder="0" bordercolor="White">
			<frame name="fraDetails2" src="../html/zXBlank.html" scrolling="auto" frameborder="0" bordercolor="White">
			<frame name="fraDetails3" src="../html/zXBlank.html" scrolling="auto" frameborder="0" bordercolor="White">
			<frame name="fraDetails4" src="../html/zXBlank.html" scrolling="auto" frameborder="0" bordercolor="White">
			<frame name="fraDetails5" src="../html/zXBlank.html" scrolling="auto" frameborder="0" bordercolor="White">
		</frameset>
	</frameset>
	<frame name="fraFooter" src="../jsp/zXFooter.jsp?-popup=Y&-s=<%=request.getParameter("-s")%>&-ss=<%=request.getParameter("-ss")%><%=strDownload%>" vspace="10" marginwidth="10" marginheight="10" scrolling="no" frameborder="0" noresize>
</frameset>

<%
	}
%>
<noframes>
	<body bgcolor="#FFFFFF">
		<H1>Your browser does not support framesets.</H1>
	</body>
</noframes>
</html>
<%
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>