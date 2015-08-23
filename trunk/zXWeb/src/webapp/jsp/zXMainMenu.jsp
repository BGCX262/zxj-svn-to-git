<%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><%

String strAction = request.getParameter("-a");
if (StringUtil.len(strAction) == 0) {
	// Default action is to show the menu.
	strAction = "show"; 
}

// Only print the js header if we are going to show the menu.
String strActionTmp = strAction;
if (strAction.equalsIgnoreCase("show")) {
%><zx:zx/><%
} else {
%><zx:zx jsheader="false"/><%
}

ZX objZX = null;
PageBuilder objPage = null;
try {
	objZX = ThreadLocalZX.getZX();
	objPage = new PageBuilder();
	
	if (strAction.equalsIgnoreCase("show")) {
%>
<html>
<head>
	<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">
	<script type="text/javascript" language="JavaScript" src="../javascript/zX.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXMenu.js"></script>
	<title>Main Menu</title>
</head>
<body background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed" scroll="auto">
<script language="javascript">			
	zXOnPageStart();
</script><% 


// ---------------------------------- DEMO CODE - CLIENT SIDE
	if(false) { %>
<table width="100%" border="0">
<tr>
	<td width="75">&nbsp;</td>
	<td>
		<span class="zXDetailsBold">
			Demo :&nbsp;&nbsp;
			<input autocomplete="off" 
				   id="_ctrSearch"  name="_ctrSearch" 
				   size="35" maxlength="255" 
				   type="text" class="zXMenuSearch" 
				   title="Type in the title of the menu item you want to execute"/>
			<div id="_popup_Results" class="zxfklookup" ><ul></ul></div>
		</span>
	</td>
</tr>
</table>
<script type="text/javascript">
menu_autocomplete('<%=objZX.getSession().getSessionid()%>', '_ctrSearch', '_popup_Results');
</script>
<% 
		}
// ---------------------------------- DEMO CODE - CLIENT SIDE


	}
	 //-----------------------------------------------------------------
	 // Not very enterprise like:
	 // Cleanup the session table (ie, delete all sessions that have not
	 // been used for a while).
	 // This should actually be a scheduled task but doing it in  routine
	 // that is a) called frequently but b) not too often is a very
	 // pragmatic solution
	 //-----------------------------------------------------------------
	 try {
		objZX.getSession().cleanup();
	 } catch (Exception e) {
	 	objZX.log.error("Failed to cleanup session data.", e);
	 }
	
	 // Safeguard against development errors
	 int intLoopCounter = 0;
	
	HierMenu objMenu = null;
	if (objZX.getRunMode().pos == zXType.runMode.rmProduction.pos) {
			objMenu = (HierMenu)objZX.getCachedValue("menu");
			if (objMenu != null) {
				objMenu.setPage(objPage);
			} else {
				objMenu = objPage.getHierMenu();
	 			objMenu.readMenuFile("mainMenu.xml");
	 			objZX.setCachedValue("menu", objMenu);
			}
	} else {
		objMenu = objPage.getHierMenu();
 		objMenu.readMenuFile("mainMenu.xml");
	}
	
	 zXType.rc rc = null;
	 do {
 		intLoopCounter++;
 		
	 	if (strAction.equalsIgnoreCase("show")) {
	 		// out.write("<br/><br/>\n");

	 		objMenu.generateMenu();
	 		out.write(objPage.flush());
%>
<script language="javascript">			
	zXSetFooter('Make selection');
</script>
<%
	 		// And done...
	 		strAction = "";


// ---------------------------------- DEMO CODE - SERVER SIDE
	 	} else if (strAction.equalsIgnoreCase("seachmenu")) {
	 		String strQury = request.getParameter("-qury");
	 		if (StringUtil.len(strQury) > 0) {
		 		out.write(objMenu.searchHierMenu(strQury));
	 		}
	 		
	 		strAction = "";
// ---------------------------------- DEMO CODE - SERVER SIDE



	 		
	 	} else {
	 		out.write(strAction);
	 		strAction = "";
	 	}
	 	
		// Exit early is there is over 10 linked actions :
		if (intLoopCounter > 10) {
			break;
		}
		
	} while (StringUtil.len(strAction) > 0);    
	
	if (strActionTmp.equalsIgnoreCase("show")) {
%>
<script language="javascript">
	zXOnPageEnd();
</script>
<zx:timer action="stop"/>
</body>
</html>
<%
	}
	
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>