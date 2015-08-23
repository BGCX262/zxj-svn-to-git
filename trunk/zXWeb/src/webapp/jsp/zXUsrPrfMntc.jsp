<%
//====================================================
// File	:	zXUsrPrfMntc.asp
// By	:	Bertus Dispa
// Date	:	11SEP02
//
// User profile maintenance routines
//
// Change	: BD18DEC02
// Why		: Fixed problem with reset password; use getBO rather than getEntity
// to make sure that we get handle to correct inter-face
// 
// Change	: DGS24JAN2003
// Why		: When creating a new user, automatically assign to the ALL group.
//====================================================
%><%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@ page import="org.zxframework.datasources.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><zx:zx/><%
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

<body background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed" scroll="auto">
<%
		Pageflow objPageflow = objPage.getPageflow(request, response, "zXUsrPrfMntc");
		String strAction = "";
		int intLoopCounter = 0;
		zXType.rc rc = null;
		UserProfile objUserProfile;
		
		do {
			if (strAction.equalsIgnoreCase("aspProcessSetPassword")) {
				// Use has entered password and confirmation password
				
				// Get handle to userProfile
				objUserProfile = (UserProfile)objPageflow.getEntity("setPassword", "zxUsrPrf").getBo();
				
				// Try to reset password
				try {
					objUserProfile.resetPassword();
					// Success!
					//Goto action that confirms success to user and allow
					//him / her to go back to main menu				
					strAction = "setPasswordDone";
					
				} catch (Exception e) {
					// Failure!
					objPage.errorMsg(objZX.trace.formatStack(false));

					//Set -err QS entry to tell clsPFEditForm.go that there
					//is no need to load the BO again as it can be used
					//as it is
					objPageflow.getQs().setEntry("-err", "Y");
					strAction = "setPassword";
				}
				
			} else if (strAction.equalsIgnoreCase("aspDeleteUsrPrf")) {
				// User wants to delete a user profile
				objUserProfile = (UserProfile)objZX.createBO("zxUsrPrf");
				
				// Set PK and try to delete
				objUserProfile.setPKValue(request.getParameter("-pk"));
				try {
					objUserProfile.delete();					
					strAction = "listUsrPrf";
				} catch (Exception e) {
					objPage.errorMsg(objZX.trace.formatStack(false));
					strAction = "editUsrPrf";
				}
				
			} else if (strAction.equalsIgnoreCase("aspInsertDefaultUsrGrps")) {
				// User has inserted a new user profile - now create default group membership(s)
				// Get handle to userProfile
				objUserProfile = (UserProfile)objPageflow.getEntity("createUpdateCreateUsrPrf", "zxUsrPrf").getBo();
				ZXBO objUsrGrpMmbr = objZX.createBO("zXUsrGrpMmbr");
				
				// Insert membership of ALL group
				DSHandler objDSHandler = objUsrGrpMmbr.getDS();
				
				objDSHandler.beginTx();
				objUsrGrpMmbr.setAutomatics();
				objUsrGrpMmbr.setValue("usrGrp", "ALL");
				objUsrGrpMmbr.setValue("usrPrf", objUserProfile.getValue("id"));
				if (!objUsrGrpMmbr.insertBO().equals(zXType.rc.rcOK)) {
					objPage.errorMsg(objZX.trace.formatStack(false));
					objDSHandler.rollbackTx();
				}	
				objDSHandler.commitTx();
				strAction = "EDITUSRPRF";
				
			} else {
				rc = objPageflow.go(strAction);    
				if (rc.equals(zXType.rc.rcOK)) {
			 		out.write(objPage.flush());
					out.flush();
					strAction = objPageflow.getAction();
				} else {
					//Action not defined in pageflow definition file
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
		
	} catch (Exception e) {
		PageBuilder.handleJSPException(objPage, out, e);
	} finally {
		if (objZX != null) objZX.cleanup();
	}
%>
<zx:timer action="stop"/>
</body>
</html>