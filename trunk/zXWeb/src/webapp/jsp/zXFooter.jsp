<%
//==========================================================
// Footer page
//
// -s - Session-id
// -ss (optional) - subsession-id
// -popup (optional) - Indicates that it is called for popup reasons. Y indicates
//			popup
//==========================================================
%><%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:zx/><%
ZX objZX = null;
PageBuilder objPage = null;
try {
	objZX = ThreadLocalZX.getZX();
	objPage = new PageBuilder();
	
	boolean blnPopup = StringUtil.booleanValue(request.getParameter("-popup"));
	
	// Get and store session id locally.
	String strSessionID = objZX.getSession().getSessionid();
	String subSessionID = objZX.getQuickContext().getEntry("-ss");
%>
<html>
<head>
	<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">

	<script type="text/javascript" language="JavaScript" src="../javascript/zX.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXFooter.js"></script>
	
	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>
<body bgcolor="indigo"
	  onUnload="javascript:zXCloseSubSession('<%=strSessionID%>','<%=subSessionID%>');">
<table width="100%" cellspacing=0>
<tr height="*">
	<td width="3%">
		<img src="../images/topOfPage.gif" 
				id="top"
				width="29" height="29" 
				title="top of page"
				onMouseOver="javascript:zXIconFocus(this, 'topOfPageOver.gif');"
				onMouseOut="javascript:zXIconFocus(this, 'topOfPage.gif');"
				onMouseDown="zXScrollFrames(0,0);"
				align="texttop">
	</td>
	<td width="3%">
		<img src="../images/bottomOfPage.gif"  
				id="bottom"
				width="29" height="29"
				title="bottom of page"
				onMouseOver="javascript:zXIconFocus(this, 'bottomOfPageOver.gif');"
				onMouseOut="javascript:zXIconFocus(this, 'bottomOfPage.gif');"
				onMouseDown="zXScrollFrames(0,10000);"
				align="absbottom">
	</td>
	<td width="10">
		<img src="../images/spacer.gif">
	</td>

<%
if(!blnPopup) {
%>
	<td width="4%" valign="absmiddle">
		<img src="../images/home.gif" 
				id="home"
				width="37" height="37" 
				title="Main menu"
				onMouseOver="javascript:zXIconFocus(this, 'homeOver.gif');"
				onMouseDown="javascript:zXMMShow('<%=strSessionID%>');"
				onMouseOut="javascript:zXIconFocus(this, 'home.gif');">
	</td>
<%
}

String strTmp = objZX.configValue("//zX/helpFile");
%>
	<td width="4%" valign="absmiddle">

		<img src="../images/help.gif" 
				id="help"
				width="37" height="37" 
				title="Help"
				onMouseOver="javascript:zXIconFocus(this, 'helpOver.gif');"
				onMouseDown="javascript:zXPopup('<%=strTmp%>');"
				onMouseOut="javascript:zXIconFocus(this, 'help.gif');">
	</td>
	<td width="4%" valign="absmiddle">
<%
//----
// Show a close image when in a popup otherwise show a logoff image.
//----
if (blnPopup) {
%>
		<img src="../images/exit.gif" 
			 id="exit"
			 width="37" height="37" 
			 onMouseOver="javascript:zXIconFocus(this, 'exitOver.gif');"
			 onMouseDown="javascript:parent.window.close();"
			 title="Close"
			 onMouseOut="javascript:zXIconFocus(this, 'exit.gif');">
<%
} else {
%>
		<img src="../images/exit.gif" 
			 id="exit"
			 width="37" height="37" 
			 onMouseOver="javascript:zXIconFocus(this, 'exitOver.gif');"
			 onMouseDown="javascript:zXDisconnect('<%=strSessionID%>');"
			 title="Logout"
			 onMouseOut="javascript:zXIconFocus(this, 'exit.gif');">
<%
}
%>
	</td>
<%
//----
// DGS24JAN2003: If the popup was originally requested with a download button, show it. Note that
// the href of the anchor will be overridden later by the code that prepares the file:
//----
if (blnPopup && StringUtil.len(request.getParameter("-downLoad")) > 0) {
%>
	<td width="4%" valign="absmiddle">
		<a id="anchorDownLoad" name="anchorDownLoad" href="javascript:alert('No file to download');" >
		<img src="../images/saveAttachment.gif" 
				id="imgDownLoad"
				width="37" height="37"
				border="0" 
				title="Right-click and 'Save target' to download file"
				onMouseOver="javascript:zXIconFocus(this, 'saveAttachmentOver.gif');"
				onMouseOut="javascript:zXIconFocus(this, 'saveAttachment.gif');"
				onClick="javascript:if(event.button == 0) zXSetFooter('Use right button click to download the file'); return false;"
		>
		</a>
<%
}
%>
	<td width="5%">
		<img src="../images/spacer.gif">
	</td>
	<td width="*">
		<SPAN ID="spnFooter" class="zxFooter"></SPAN>
	</td>
	<td width="*">
	</td>

<%
//----
// Show Quick Search when not a popup window.
//----
if (!blnPopup) {
%>
	<td width="350">
		<div align="center">
			<input 
				  id="qs"
				  type="text"
				  name="qs" 
				  onKeypress="javascript:eval('zXStdOnKeyPress(event,' + getQSTypeValue() + ')');"
				  >
			
			<select 
					id ="qsType" 
					name="qsType" 
					onKeypress="javascript:eval('zXStdOnKeyPress(event,' + getQSTypeValue() + ')');"
					>
			<option value='"<%=strSessionID%>", "af/crnkshft", getQSControl(), "af/crnkView", "veiw.crnkshft.start", ""'>Crankshaft</option>
			<option value='"<%=strSessionID%>", "af/wrksttn", getQSControl(), "af/sttnView", "view.sttn.start", ""'>Workstation</option>
			<option value='"<%=strSessionID%>", "test/wtch", getQSControl(), "test/wtch", "wtch.popup.start.null", ""'>Watch</option>
			</select> 
			
			<img 
				id="search"
				align="absmiddle"
				src="../images/search.gif" 
				width="37" height="37"
				title="Quick search"
				onMouseDown="javascript:eval('zXStdQS(' + getQSTypeValue() + ')');"
				onMouseOver="javascript:zXIconFocus(this, 'searchOver.gif');"
				onMouseOut="javascript:zXIconFocus(this, 'search.gif');"
				>
		</div>
	</td>
<%
}
%>
</tr>

<tr height="15">
	<td width="10">
		<img src="../images/spacer.gif">
		<p>
	</td>
</tr>
</table>

</body>
</html>
<%
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>