<%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><zx:zx/><%
 //----
 // File	:	zXPrntrMntc.asp
 // By	:	Bertus Dispa
 // Date	:	16JUL03
 //
 // Maintain printer definitions
 //----
 
ZX objZX = null;
PageBuilder objPage = null; 
try {
	objZX = ThreadLocalZX.getZX();
	objPage = new PageBuilder();
	
%>
<html>
<head>
	<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">
	
	<script type="text/javascript" language="JavaScript" src="../javascript/zX.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXForm.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXList.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/spellChecker.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXTree.js"></script>
	
	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>

<body background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed">
<%
	//----
	// Create html and pageflow objects
	//----
	Pageflow objPageflow = objPage.getPageflow(request, response, "");
	
	String strAction = request.getParameter("-a");
	if (strAction != null) {
		strAction.toLowerCase();
	} else {
		strAction = "";
	}
	
	//----
	// Safeguard against development errors
	//----
	int intLoopCounter = 0;
	
	zXType.rc rc = null;
	
	 do {
	 	if (strAction.equals("asp.test")) {
	 		//----
	 		// User wants to test the current rule set-up
	 		//----
	 		Printer objPrntr = (Printer)objPageflow.getBO("search", "zXPrntr");
	 		if (objPrntr == null) {
	 			out.write(objPage.fatalError("Unable to get handle to search.zXPrntr"));
	 			return;
	 		}
	 		
	 		objZX.trace.resetStack();
	 		
	 		if (objPrntr.testPrinter().pos != zXType.rc.rcOK.pos) {
	 			objPageflow.setErrorMsg(objZX.trace.formatStack(false));
	 		} else {
	 			objPageflow.setInfoMsg("Printer worked");
	 		}
	 		
	 		strAction = "edit";
			
	 	} else {
		 	rc = objPageflow.go(strAction);
		 	if (rc.equals(zXType.rc.rcOK)) {
		 		out.write(objPage.flush());
		 		strAction = objPageflow.getAction();
		 	} else {
		 		//----
		 		// Action not defined in pageflow definition file
		 		//----
				strAction = objPageflow.getAction();
	        }
        }
		intLoopCounter++;
	} while (StringUtil.len(strAction) > 0 && intLoopCounter < 20);    
	
	if (intLoopCounter >= 20) {
		out.write(objPage.fatalError("Unprocessed action : " + strAction));
		// return;
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
	
%>
<zx:timer action="stop"/>
</body>
</html>
<%
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>