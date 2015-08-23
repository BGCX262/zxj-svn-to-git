<%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><zx:zx/><%
 //----
 // File	:	zXPrcssMntr.asp
 // By	:	David Swann
 // Date	:	19NOV2002
 //
 // Process Monitor
 //
 // Change	: DGS10SEP2003
 // Why		: Multi-language: must use pageflow.getLabel when getting from a language-specific collection.
 //
 // Change	: BD19JAN04
 // Why		: Added search form
 //
 // Change	: TM2 DGS01MAR2005
 // Why		: New logic for process monitor
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
	 	if (strAction.equals("aspreseterror")) {
	 		//----
	 		// Get the business object...
	 		//----
	 		org.zxframework.misc.PrcssMntr objPrcssMntr = (org.zxframework.misc.PrcssMntr)objPageflow.getBO("view","zxPrcssMntr");
	 		if (objPrcssMntr == null) {
	 			objPageflow.setErrorMsg(objZX.trace.formatStack(false));
	 		} else {
	 			//----
	 			// ...Set the primary key...
	 			//----
	 			if (objPrcssMntr.setPKValue(request.getParameter("-pk")).pos != zXType.rc.rcOK.pos) {
	 				objPageflow.setErrorMsg(objZX.trace.formatStack(false));
	 			} else {
		 			//----
		 			// ...and clear the error message:
		 			//----
		 			if (objPrcssMntr.resetError().pos != zXType.rc.rcOK.pos) {
		 				objPageflow.setErrorMsg(objZX.trace.formatStack(false));
		 			} else {
		 				//---
		 				// Done ok - the info message is set in pageflow XML against a 
		 				// null action for language independence. 
		 				// DGS10SEP2003: Multi-language: must use pageflow.getLabel when getting from a 
		 				// language-specific collection.
		 				// objPageflow.infoMsg = objPageflow.PFDesc.actions("ResetMsg").infoMsg(objzX.language).label
		 				//----
		 				objPageflow.setInfoMsg(
		 						objPageflow.getLabel(
		 								((PFAction)objPageflow.getPFDesc().getActions().get("resetmsg")).getInfomsg())); 
		 			}
				}	 			
	 		}
	 		
	 		strAction = "view";
	 	
	 	//----
	 	// DGS24JAN2003: Allow multilist of processes to be updated in one go. Now use submitted
		// ref buttons with a different qs -stts parameter
	 	//----
	 	} else if (strAction.equals("aspmultirequest")) {
	 		int lStts = Integer.parseInt(request.getParameter("-stts"));
	 		
	 		//----
	 		// Get the business object:
	 		//----
	 		org.zxframework.misc.PrcssMntr objPrcssMntr = (org.zxframework.misc.PrcssMntr)objPageflow.getBO("List","zxPrcssMntr");
	 		if (objPrcssMntr == null) {
	 			objPageflow.setErrorMsg(objZX.trace.formatStack(false));
	 		} else {
	 			//----
	 			// Get the concatenated string of primary keys of each selected item from the list
	 			//----
	 			String strKeys = objPage.processMultiListNonSQL(request, objPrcssMntr, "|", "", 1);
	 			if (StringUtil.len(strKeys) == 0) {
	 				//----
	 				// No items selected from the list - put a message out:
	 				// DGS10SEP2003: Multi-language: must use pageflow.getLabel when getting from a 
	 				// language-specific collection.
	 				// objPageflow.infoMsg = objPageflow.PFDesc.actions("NoneSelectedMsg").infoMsg(objzX.language).label
	 				//----
	 				objPageflow.setInfoMsg(objPageflow.getLabel(((PFAction)objPageflow.getPFDesc().getActions().get("noneselectedmsg")).getInfomsg()));
	 				
	 			} else {
	 				String[] astrKey = strKeys.split("|");
	 				for (int j=0; j<astrKey.length; j++) {
	 					//----
	 					// Issue a status change for each selected item:
	 					//----
	 					if (objPrcssMntr.setPKValue(astrKey[j]).pos != zXType.rc.rcOK.pos) {
	 						objPageflow.setErrorMsg(objZX.trace.formatStack(false));
	 					} else {
	 						if (objPrcssMntr.statusChange(zXType.processMonitorStatus.getEnum(lStts)).pos != zXType.rc.rcOK.pos) {
	 							objPageflow.setErrorMsg(objPageflow.getErrorMsg() + "\n" + objZX.trace.formatStack(false));
	 						}
	 					}
	 				}
	 				
	 				//----
	 				// Confirmation message is from null action named 'MultiMsgN' where N = 0 to 3 (status):
	 				// DGS10SEP2003: Multi-language: must use pageflow.getLabel when getting from a 
	 				// language-specific collection.
	 				// objPageflow.infoMsg = objPageflow.PFDesc.actions("MultiMsg" & lStts).infoMsg(objzX.language).label
	 				//----
	 				objPageflow.setInfoMsg(objPageflow.getLabel(((PFAction)objPageflow.getPFDesc().getActions().get("multimsg" + lStts)).getInfomsg()));
	 			}
	 		}
	 		
	 		strAction = "search";
	 		
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