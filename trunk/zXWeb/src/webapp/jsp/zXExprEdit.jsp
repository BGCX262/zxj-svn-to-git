<%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><zx:zx/><%
 //----
 //File	:	zXDcBldRqstQueue.asp
 // By	:	David Swann
 // Date	:	MAR2005
 //
 // Document Build Request Queue
 //
 // Additional Parameters :
 // -ctr = The name of the form control to populate.
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
	 	if (strAction.equalsIgnoreCase("asp.start")) {
			//----
			// Start the show by populating the exprEdit object function list
			//----
			ZXBO objExprEdit = objPageflow.getBO("edit", "zXExprEdit");
			if (objExprEdit == null) {
				out.write(objPage.fatalError("Unable to get handle to object edit.zXExprEdit"));
				return;
			}
	 		
	 		if (objZX.getExpressionHandler().populateFunctions(objExprEdit).pos != zXType.rc.rcOK.pos) {
	 			out.write(objPage.fatalError("Unable to get populate functions"));
	 			return;
	 		}
	 		
	 		//----
	 		// Tell edit form not to do any database stuff
	 		//----
	 		objZX.getQuickContext().setEntry("-sa", "editNoDB");
			
			strAction = "edit";
			
		} else if (strAction.equalsIgnoreCase("asp.settext")) {
			//----
			// Set field from javascript clipboard
			//----
			ZXBO objExprEdit = objPageflow.getBO("edit", "zXExprEdit");
			String strControl = objPage.controlName(objExprEdit, objExprEdit.getDescriptor().getAttribute("expr"));
			
			out.write(objPage.flush());
			
			out.write("<script type='text/javascript' language='JavaScript'>\n");
			// Does not work in Firefox and can coz confusion.
			// out.write("	varText = window.clipboardData.getData(\"text\");\n");
			// Read in the value of the form control.
			out.write("	varText = findObj('" + request.getParameter("-ctr") + "', top.window.opener).value;\n");
			out.write("	if (varText != null)\n");
			out.write("	    window.document.forms[0]." + strControl + ".value = varText;\n");
			out.write("</script>\n");
			
			strAction = "";
			
		} else if (strAction.equalsIgnoreCase("asp.process")) {
			//----
			// Either compress / uncompress / verbose / done; whatever is in -what
			//----
			ZXBO objExprEdit = objPageflow.getBO("edit", "zXExprEdit");
			
			if (objExprEdit == null) {
				out.write(objPage.fatalError("Unable to get handle to object edit.zXExprEdit"));
				return;
			}
			String strWhat = objZX.getQuickContext().getEntry("-what");
			
			String strTmp = "";
			if (strWhat.equalsIgnoreCase("compress")) {
				strTmp = objZX.getExpressionHandler().compress(objExprEdit.getValue("expr").getStringValue());
				
			} else if (strWhat.equalsIgnoreCase("uncompress")) {
				strTmp = objZX.getExpressionHandler().unCompress(objExprEdit.getValue("expr").getStringValue(),
																 "\n","\t");
				
			} else if (strWhat.equalsIgnoreCase("verbose") 
					   || strWhat.equalsIgnoreCase("done")) {
				strTmp = objZX.getExpressionHandler().describe(objExprEdit.getValue("expr").getStringValue());
			
			}
			
			if (StringUtil.len(strTmp) == 0) {
				objPageflow.setErrorMsg(objZX.trace.formatStack(false));
			} else {
				if (strWhat.equalsIgnoreCase("compress") || strWhat.equalsIgnoreCase("uncompress")) {
					objExprEdit.setValue("expr", new org.zxframework.property.StringProperty(strTmp + ""));
					objZX.getQuickContext().setEntry("-nextAction", "");
					
				} else if (strWhat.equalsIgnoreCase("verbose")) {
					objExprEdit.setValue("vrbse", new org.zxframework.property.StringProperty(strTmp + ""));
					objZX.getQuickContext().setEntry("-nextAction", "");
					
				} else if (strWhat.equalsIgnoreCase("done")) {
					objZX.getQuickContext().setEntry("-nextAction", "done");
				}
				
			}
			
			strAction = "asp.start";
			
	 	} else {
	 		//----
	 		// Perform a action defined in the pageflow.
	 		//----
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