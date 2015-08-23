<%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@ page import="org.zxframework.property.LongProperty"
%><%@ taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><zx:zx/><%
//----
//File	:	zXDcBldRqstQueue.asp
// By	:	David Swann
// Date	:	MAR2005
//
// Document Build Request Queue
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
	
	do {
	 	if (strAction.equals("view.asp")) {
	 		//----
	 		// At this point the basic doc build request details have been shown in an edit form.
			// Now see if there is a specialist pageflow to show the subject fo the request in
			// more details (e.g. a task print would show task details and have a button to click
			// to open the task popup)
			//----
	 		ZXBO objDcBldRqst = objZX.getBos().quickLoad("zXDcBldRqst", 
	 													 new LongProperty(Integer.parseInt(request.getParameter("-pk.zXDcBldRqst")), false));
	 		if (objDcBldRqst == null) {
	 			out.write(objPage.fatalError("Unable to load doc build request"));
	 			return;
	 		}
	 		
			ZXBO objDcBldRqstTpe = objZX.getBos().quickLoad("zXDcBldRqstTpe", objDcBldRqst.getValue("tpe"));
	 		
	 		//----
	 		// Assume will present the generic view details, unless we find a specialist pageflow
	 		//----
	 		strAction = "generic.view";
	 		
	 		if (objDcBldRqstTpe != null) {
	 			if (!objDcBldRqstTpe.getValue("url").isNull) {
	 				objZX.getQuickContext().setEntry("-externalpf", 
	 												 objDcBldRqstTpe.getValue("url").getStringValue());
					strAction = "externalpf.null";
	 			}
	 		}
	 		
	 	} else {
	 		//----
	 		// Perform pageflow define actions
	 		//----
	 		int intRC = objPageflow.go(strAction).pos;
		 	if (intRC == zXType.rc.rcOK.pos) {
		 		out.write(objPage.flush());
		 		strAction = objPageflow.getAction();
		 		
		 	} else if (intRC == zXType.rc.rcWarning.pos){
		 		//----
		 		// Action not defined in pageflow definition file
		 		//----
				strAction = objPageflow.getAction();
		 		
	        } else {
	        	objPage.fatalError("Failed to perform acion : " + strAction);
	        	return;
	        }		 	
        }
        
		intLoopCounter++;
	} while (StringUtil.len(strAction) > 0 && intLoopCounter < 20);
	
	if (intLoopCounter >= 20) {
		out.write(objPage.fatalError("Unprocessed action : " + strAction));
		// return;
	}
	
	//----
	// Dump any major errors still lingering
	//----
	objPageflow.dumpAllErrors();
		
	//----
	// And store the session if not logged-off
	//----
	if (StringUtil.len(objZX.getSession().getSessionid()) > 0) {
		objZX.getSession().store();
	}
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