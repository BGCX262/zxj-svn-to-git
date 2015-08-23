<%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><zx:zx/><% 
ZX objZX = null;
PageBuilder objPage = null; 
try {
	objZX = ThreadLocalZX.getZX();
	objPage = new PageBuilder(); 
	
	//----
	// Create html and pageflow objects
	//----
	Pageflow objPageflow = objPage.getPageflow(request, response, "");
%>
<html>
<head>
	<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">
	
	<script type="text/javascript" language="JavaScript" src="../javascript/zX.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXForm.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXList.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/spellChecker.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXTree.js"></script>
	
	<!-- 
	<style type="text/css">@import url(../includes/calendar-system.css);</style>
	<script type="text/javascript" src="../javascript/calendar.js"></script>
	<script type="text/javascript" src="../javascript/lang/calendar-en.js"></script>
	<script type="text/javascript" src="../javascript/calendar-setup.js"></script>
	 -->
	 
	<%=objPageflow.processPFIncludes()%>
	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>

<body background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed">
<%
	String strAction = "";
	int intLoopCounter = 0;
	int intRC;
	
	do {
		//----
		// Execute a named pageflow action :
		//----
		intRC = objPageflow.go(strAction).pos;
	 	if (intRC == zXType.rc.rcOK.pos) {
	 		//----
	 		// Action was performed correctly move to next action
	 		//----
	 		out.write(objPage.flush());
	 		out.flush();
	 		strAction = objPageflow.getAction();
	 		
	 	} else if (intRC == zXType.rc.rcWarning.pos) {
	 		//----
	 		// Action not defined in pageflow definition file
	 		//----
			strAction = objPageflow.getAction();
	 		
	 	} else if (intRC == zXType.rc.rcError.pos) {
	 		//----
	 		// Something went wrong in the pageflow action.
	 		//----
	 		out.flush();
			out.write(objPage.fatalError("Go " + strAction));
			
        }
		      
		intLoopCounter++;
	} while (StringUtil.len(strAction) > 0 && intLoopCounter < 20);    
	
	if (intLoopCounter >= 20) {
		out.write(objPage.fatalError("Unprocessed action : " + strAction));
	}
	
	//----
	// And store the session if not logged-off
	//----
	if (StringUtil.len(objZX.getSession().getSessionid()) > 0) {
		objZX.getSession().store();
	}
		
	//----
	// Dump any major errors still lingering
	//----
	objPageflow.dumpAllErrors();
	
%><zx:timer action="stop"/>
</body>
</html>
<%
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>