<%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:zx checklogin="false" jsheader="false"/><%
ZX objZX = null;
PageBuilder objPage = null;
try {
	objZX = ThreadLocalZX.getZX();
	objPage = new PageBuilder();
	boolean blnShowMsg = false;
	
	String strAction = request.getParameter("-a");
	if (StringUtil.len(strAction) == 0) {
		strAction = "showLogin";
	}
	
	//----
	// If action is logoff or reset, we need to retrieve the session. If the session has been deleted
	// due to a time out, don't show an error if the user is trying to logout so that they can log back in
	//----
	if (strAction.equalsIgnoreCase("reset") 
		|| strAction.equalsIgnoreCase("logOff")
		|| (strAction.equalsIgnoreCase("aspCloseSubSession") && StringUtil.len(request.getParameter("-ss")) > 0)
		) {
		//----
		// Get the session for the user and load it.
		//----
		if ( !objZX.getSession().retrieve(request.getParameter("-s")).equals(zXType.rc.rcOK) ) {
			new javax.servlet.jsp.JspException("Severe error: Unable to retrieve session information");
		}
	}

	//----
	// Non visual components of zXLogin.jsp
	//----
	if (strAction.equalsIgnoreCase("doLogin")) {
		//----
		// User Login :
		// Done earlier so that we do not generate any html before adding the cookies.
		//----
		//----
		// Check user login
		//----
		objZX.getUserProfile().setValidate(true);
		
		//----
		// User has submitted the login page
		//----
		if (!objPage.processEditForm(request,  objZX.getUserProfile(), "login").equals(zXType.rc.rcOK)) {
			//----
			// Failed to login due to validation rules
			//----
			blnShowMsg = true;
			strAction = "showLogin";
			
		} else {
			//----
			//Now try to login
			//----
			if (objZX.getSession().connect(
										   objZX.getUserProfile().getValue("id").getStringValue(),
										   objZX.getUserProfile().getValue("pssWrd").getStringValue()
										   ).pos != zXType.rc.rcOK.pos) {
				//----
				// Incorrect username password
				//----
				strAction = "showLogin";
				
				//-----
				// DGS01JUL2004: Don't reset errorstack or add another error. We have now tidied up
				// the zx error messages so that it is best to show them as is.
				//---- 
				blnShowMsg = true;
					    			
			} else {
				javax.servlet.http.Cookie cookie;
				
		    	//----
		    	// Successfull login, now redirect to the client dashboard.
		    	//----
				String strSessionID = objZX.getSession().getSessionid();

				//----
				// Store the session id in a cookie
				//----
	            cookie = new javax.servlet.http.Cookie("-s", strSessionID);
	            ((javax.servlet.http.HttpServletResponse)pageContext.getResponse()).addCookie(cookie);
	            
				//----
				// Determine what the users preferred locale is.
				// Support old style locale settings.
				//----
				String localeName = "-locale";
				String localeValue = objZX.getLanguage();
				if (localeValue.length() == 2) {
					java.util.Locale locale = java.util.Locale.getDefault();
					if (localeValue.equalsIgnoreCase(java.util.Locale.UK.getLanguage())) {
						locale = java.util.Locale.UK;
					} else if (localeValue.equalsIgnoreCase(java.util.Locale.FRANCE.getLanguage())){
						locale = java.util.Locale.FRANCE;
					} else if (localeValue.equalsIgnoreCase(java.util.Locale.GERMANY.getLanguage())){
						locale = java.util.Locale.GERMANY;
					} else if (localeValue.equalsIgnoreCase(java.util.Locale.ITALY.getLanguage())){
						locale = java.util.Locale.ITALY;
					}
					localeValue = locale.toString();
				}
				
				//----
				// Store locale in cookie
				//----
	            cookie = new javax.servlet.http.Cookie(localeName, localeValue);
	            ((javax.servlet.http.HttpServletResponse)pageContext.getResponse()).addCookie(cookie);
	            
				strAction = "startTheShow";
			}
		}
		
	} else if (strAction.equalsIgnoreCase("reset")) {
    	//----
		//Reset the context and display empty page
		//----
		objZX.getSession().resetContext();
		objZX.getSession().addToContext("zXBack", ("zXMMShow('" + objZX.getSession().getSessionid() + "')"));
		
		strAction = ""; 
		
		//----
		// Exit early
		//----
		return;
		
    } else if (strAction.equalsIgnoreCase("aspCloseSubSession")) {
    	//----
		// Close a subsession
		//----
		objZX.getSession().closeSubsession(request.getParameter("-ss"));
		
		strAction = "";
		
		//----
		// Exit early
		//----
		return;
		
    }
    
%>
<html>
<head>
	<script type="text/javascript" language="JavaScript">
		var SCRPT = 'jsp';
	</script>
	
	<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">
	
	<script type="text/javascript" language="JavaScript" src="../javascript/zX.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXForm.js"></script>
	
	<title>Login</title>
</head>
<body background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed">
<%
	// Safeguard against development errors
	int intLoopCounter = 0;
	do {
		if (strAction.equalsIgnoreCase("showLogin")) {
			//----
			// Show the login page, either for first time or
			// again because user has entered wrong data
			// Issue 251BB - show application name and version on login. 
			// Also indicate if not production run mode.
			//----
			String strAppName = StringUtil.len(objZX.getAppName()) == 0 ? "" : objZX.getAppName();
			String strAppVersion = StringUtil.len(objZX.getSettings().getAppVersion()) == 0 ? "" : objZX.getSettings().getAppVersion();
			String strCaption = strAppName + " " + strAppVersion;
			if (objZX.getRunMode().pos != zXType.runMode.rmProduction.pos) {
				strCaption = strCaption + " (" + zXType.valueOf(objZX.getRunMode()) + ")";
			}
			
			objPage.formSubTitle(zXType.webFormType.wftEdit, strCaption);
		    out.write(objPage.flush());
			out.flush();
			
			if (StringUtil.len(objZX.configValue("//zX/loginText")) > 0) {
				out.write("<br>");
				objPage.narrative(objZX.configValue("//zX/loginText"));
				out.write(objPage.flush());
			}
			
			objPage.formTitle(zXType.webFormType.wftEdit, "Login");
%>
<form name="zXLoginForm" method="POST" action="../jsp/zXLogin.jsp?-a=doLogin">
<br>
<%
			//----
			//Display any error messages
			//----
			if (blnShowMsg) {
				objPage.formatErrorStack(false);
				out.write(objPage.flush());
			}
			
			//----
			// Generate form
			//
			// BD17DEC02: dreadful hack and I will probably live to regret:
			// In html.editForm a field is set to locked when the attributes is
			// the PK and the persist status is not new; this is fine except for
			// when the user enters a wrong user-id / password.... 
			//
			// DGS20APR2004 Domarque Issue 289JF: clear previous password
			//----
			objZX.getUserProfile().resetBO("psswrd");
			objZX.getUserProfile().setPersistStatus(zXType.persistStatus.psNew);
			objPage.editForm(objZX.getUserProfile(), "login", "", "*");
			
			// Generate submit button
			objPage.buttonAreaOpen(zXType.webFormType.wftEdit);
			objPage.submitButton("Login", "Submit");
			objPage.buttonAreaClose(zXType.webFormType.wftEdit);
			
			out.write(objPage.flush());
%>
</form>
<script language="Javascript" type="text/javascript">
	zXSetCursorToFirstField();
</script>
<%
			strAction = ""; 
			
	    } else if (strAction.equalsIgnoreCase("startTheShow")) {
	    	//----
	    	// Successfull login, now redirect to the client dashboard.
	    	//----
			String strSessionID = objZX.getSession().getSessionid();
			
	    	out.write("<SCRIPT language=\"Javascript\">\n");
	    	//----
	    	// Set up javascript variables and window names.
	    	//----
	    	out.write("	top.window.name='" + strSessionID + "'; \n");
	    	out.write("	top.gSessionID='" + strSessionID + "'; \n");
	    	
	    	//----
	    	// Do a client ridirect. If there is no frames, when just set the window location to the main menu.
	    	//----
	    	out.write("	if (parent.fraFooter) \n");
	    	out.write("		parent.fraFooter.location='../jsp/zXFooter.jsp?-s=" + strSessionID + "'; \n");
	    	
	    	out.write("	if (top.frames[1]) { \n");
	    	out.write("		top.frames[1].location='../jsp/zXMainMenu.jsp?-a=show&-s=" + strSessionID + "'; \n");
	    	out.write("	} else { \n");
	    	out.write("		window.location='../jsp/zXMainMenu.jsp?-a=show&-s=" + strSessionID + "'; \n");
	    	out.write("	} \n");
	    	
	    	out.write("	if (parent.fraLeft) \n");
	    	out.write("		parent.fraLeft.location='../jsp/zXLeft.jsp?-s=" + strSessionID + "'; \n");
	    	
	    	out.write("</SCRIPT>\n");
	    	
	    	strAction="";
	    	
	    } else if (strAction.equalsIgnoreCase("logOff")) {
		    //----
			// Use wants to log off. If the session does not exist, let the user log back in i.e.
			// don't crash out here.
			//----
			if (objZX.getSession() != null) {
				try {
					objZX.getSession().disconnect();
				} catch (Exception e) {
				}
			}
			
			//----
			// Redirect to the login page.
			// Use timestamp for caching browsers!
			//----
 %>
<script language="javascript">			
	parent.window.location = "../html/zXStart2.html?" + new Date().getTime();
</script>
<%
			strAction = "";
			
	    }
	    
		intLoopCounter++;
	} while (StringUtil.len(strAction) > 0 && intLoopCounter < 20);    
	
	if (intLoopCounter >= 20) {
		out.write(objPage.fatalError("Unprocessed action : " + strAction));
		// return;
	}
	
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>
</body>
</html>