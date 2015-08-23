<%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><zx:zx/><%
 //----
 // File	:	zXStdPrntRqst.asp
 // By	:	Bertus Dispa
 // Date	:	22MAY03
 // 
 // General print request handler
 // 
 // _s = session
 // _pk - primary key of what needs to be printed
 // _tpe - Type of print (as understood by document builder)
 // _tmplt - Template to use
 // _prntr - One of the following values:
 // 			1 - Use printer 1 of user (and ask if not set)
 // 			2 - Use printer 2 of user (and ask if not set)
 // 			- - No print required
 // 			Non-blank - Use that printer 
 // 			Blank - Assume the docBuilder knows what printer to use
 // _saveAs - Either numeric (save as file indicated by zXDoc entry with this id)
 // 		    Non-numeric: save as that
 // _addInfo1 .. 5 - Additional information as used by doc builder
 // 
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
	Pageflow objPageflow = objPage.getPageflow(request, response, "zXStdPrntRqst");
	
	String strAction = request.getParameter("-a");
	if (strAction != null) {
		strAction = strAction.toLowerCase();
	} else {
		strAction = "";
	}
	
	//----
	// Safeguard against development errors
	//----
	int intLoopCounter = 0;
	
	int intRC;
	
	 do {
	 	if (strAction.equals("asp.determinefirstaction")) {
	 		//----
	 		// The first action depends on a number of things:
	 		// - The printer is either 1 or 2  ==> prompt user for printer 1 or 2
	 		// - A '-'; no print is required
	 		// - The doc builder will know what printer to use
	 		//----
	 		String strPrinter = request.getParameter("-prntr");
	 		if (strPrinter.equals("1") || strPrinter.equals("2")) {
	 			//----
	 			//  See if we already know what the printer is
	 			// Now do NOT use zx.usrPrf as this is cached and thus does not reflect the
	 			// most up2date values
	 			//----
	 			ZXBO objUsrPrf = objZX.getBos().quickLoad("zXUsrPrf", objZX.getUserProfile().getValue("id"));
	 			if (objUsrPrf == null) {
	 				out.write(objPage.fatalError("Unable to load user profile"));
	 				return;
	 			}
	 			
	 			objZX.getQuickContext().setEntry("-group", "id,prntr" + strPrinter);
				strAction = "setmyprinter.edit";
				
	 		} else {
	 			strAction = "createrequest.dbaction";
	 			
	 		}
	 		
	 	} else {
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