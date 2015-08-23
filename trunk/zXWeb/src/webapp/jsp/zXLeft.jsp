<%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.misc.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:zx/><% 
ZX objZX = null;
PageBuilder objPage = null; 
try {
	objZX = ThreadLocalZX.getZX();
	objPage = new PageBuilder(); 
	String strSessionID = objZX.getSession().getSessionid();
	String strSubSessionID = request.getParameter("-ss");
%>
<html>
<head>
	<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">
	
	<script type="text/javascript" language="JavaScript" src="../javascript/zX.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/im.js"></script>
	<!-- <meta http-equiv="refresh" content="120"> -->
	
	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>

<body bgcolor="white">

<table width="100%" height="100%"  border=0>
	<tr height="7%">
		<td  valign="top" align="center">
			<img src=<%=objPage.getWebSettings().getLogoImg()%>>
		</td>
	</tr>
	
	<tr height="20" align="left">
		<td class="zXDetails" valign="top">
			 User: <%=objZX.getUserProfile().getValue("nme").getStringValue()%>
		</td>
	</tr>

<%
	// Only display buttons when NOT in a popup (ie no -ss)
	if (StringUtil.len(strSubSessionID) == 0) {
%>
		<tr height="10%">
		<td valign="bottom" align="center" >
			<img src="../images/textRead.gif" 
				id="txt"
				title=""
				onMouseOver="javascript:zXIconFocus(this, '../images/textReadOver.gif');"
				onMouseOut="javascript:zXIconFocus(this, '../images/textRead.gif');"
			    onMouseDown="javascript:imActionsMenu('<%=request.getParameter("-s")%>');">
			</td>
		</tr>
		
		<%
		int lngPoll;
		if (StringUtil.len(request.getParameter("-imPoll")) == 0) {
			lngPoll = 5000;
		} else if (!StringUtil.isNumeric(request.getParameter("-imPoll"))) {
			lngPoll = 5000;
		} else if (Integer.parseInt(request.getParameter("-imPoll")) <= 0) {
			lngPoll = 0;
		} else {
			lngPoll = Integer.parseInt(request.getParameter("-imPoll"));
		}
		
		if (lngPoll > 0) {
			out.write("<script language='javascript'>");
			out.write("imPollMessages(findObj('txt'), '" 
					  + objZX.getUserProfile().getValue("id").getStringValue() + "', '" + request.getParameter("-s") + "'," + lngPoll + ");");
			out.write("</script>");
		}
		
		//-----
		// New messaging style. (Did not work)
		//-----
		if (false) {
		%>
			<!--  New message system -->
			<tr height="10%">
				<td valign="bottom" align="center" >
					<div id="zXIMMessages">
					<img src="../images/textRead.gif" 
						 id="zXIMMsg"
						 title="No unread texts"
						 onMouseOver="javascript:zXIconFocus(this, 'textReadOver.gif');"
						 onMouseOut="javascript:zXIconFocus(this, 'textRead.gif');"
					     onMouseDown="javascript:zXPopupMsg('<%=strSessionID%>');">
					</div>
					<script type="text/javascript" language="JavaScript">
					window.setInterval("zXPollIMMsg('<%=objZX.getUserProfile().getPKValue().getStringValue()%>','<%=strSessionID%>')", 100);
					</script>
				</td>
			</tr>
			<%
		}
		
		//-----
		// The old style messaging system.
		//-----
		if (false) {
			Text objTxt = (Text)objZX.createBO("zxTxt");
			if (objTxt == null) {
				objPage.fatalError("Unable to create instance of zxTxt");
			}
		
			int intTxt = objTxt.countUnread();
			
			if (intTxt < 0) {
				objPage.fatalError("Unable to count unread texts");
			}
			
			String strImg;
			String strImgOver;
			String strTitle;
			
			if (intTxt == 0) {
				strImg = "textRead.gif";
				strImgOver = "textReadOver.gif";
				strTitle = "No unread texts";
			} else {
				strImg = "textUnread.gif";
				strImgOver = "textUnreadOver.gif";
				strTitle = "Unread texts available";
			}
	%>		
			<tr height="10%">
				<td valign="bottom" align="center" >
					<div id="zXMessages">
					<img src="../images/<%=strImg%>" 
						 id="zXMsgImg"
						 title="<%=strTitle%>"
						 onMouseOver="javascript:zXIconFocus(this, '<%=strImgOver%>');"
						 onMouseOut="javascript:zXIconFocus(this, '<%=strImg%>');"
					     onMouseDown="javascript:zXPopupTxt('<%=strSessionID%>');">
					</div>
					
					<script type="text/javascript" language="JavaScript">
					window.setInterval("zXPollMessages('<%=strSessionID%>')", 100);
					</script>
				</td>
			</tr>			
	<%
		}
		
	}
%>
	<tr height="*">
		<td >
			<img src="../images/spacer.gif">
		</td>
	</tr>

	<tr height="10%">
		<td align="center" valign="bottom" >
			<img id="calculatorsImg"
				src="../images/abacus.gif" 
				title="Calculators"
				onMouseOver="javascript:zXIconFocus(this, 'abacusOver.gif');"
				onMouseOut="javascript:zXIconFocus(this, 'abacus.gif');">
		</td>
	</tr>
</table>
<%
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>
</body>
</html>