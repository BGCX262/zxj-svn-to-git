<%
//----
// File	:	zXStdPopup.jsp
// By	:	Bertus Dispa
// Date	:	21MAR03
//
// Support for framework standard popup view
// 
// -s = session
// -ss = sub-session
// -a = action   (frameset, button)
// -pf = pageflow
//
// Change	: BD28MAR03
// Why		: - Forgot to retrieve session in unLock action, so the closeSubsession
//				would not work....
//			  - Include zXForm.js so we can call functions that are defined in there
//
// Change	: DGS15MAY03
// Why		: Uses -zxpk rather than -pk to avoid conflict, esp. when used inline rather than as a popup.
//
// Change	: DGS10OCT2003
// Why		: When generating the footer, include a refresh of the navigation frame as the last thing
//             in the onBeforeUnload. Without this it sometimes fails to execute the unlock javascript.
//
// Change	: BD16FEB04
// Why		: Support a preceding action for the stdPopup; this feature can be used to 
//             add some stuff to the querystring before loading the popup
//
// Change	: DGS22JUN2004
// Why		: After unlocking, if necessary (defined by QS parameter), close this window
//
// Change	: BD26AUG04
// Why		: Add support for -zXPopupDef to force a popup 
//
// Change	: MB01NOV04
// Why		: Do not include zXForm or zXTree js in the frameset html
//
// Change	: MB02NOV04
// Why		: Small tweak to allow for browser independence.
//
// Change	: BD15NOV04
// Why		: Refresh of navigation frame on close of standard popup is now configurable
//
// Change	: BD20JAN05 - V1.4:24
// Why		: Prompt user for confirmation if any of the tab pages was marked as 
//			  dirty
//----
%><%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.property.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:zx/><%
String strAction = request.getParameter("-a");
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
<%
// Hide non frameset javascripts
if (!strAction.equalsIgnoreCase("aspframeset")) {
%>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXForm.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXTree.js"></script>
<%} %>
	
	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>
<%
	String sessionID = objZX.getSession().getSessionid();
	String subsessionID = objZX.getQuickContext().getEntry("-ss");
	PFStdPopup objStdAction; 
	int intRC;
	
	// Get the action to execute : 
	if (StringUtil.isEmpty(strAction)) {
		strAction = "";
	}
		
	//----
	// Only initialise the pageflow object when we need it (e.g. we don't need
	// it when the action is aspUnlock
	//----
	Pageflow objPageflow = null;
	if (!strAction.equalsIgnoreCase("aspunlock")) {
		objPageflow = objPage.getPageflow(request, response, "");
	}
	
	//----
	// Safeguard against development errors
	//----
	int intLoopCounter = 0;
	
	do {
		if (strAction.equalsIgnoreCase("aspframeset")) {
			//----
			// Create the frameset definition; all the hard work is done in the go
			// of the stdPopup
			//----
			strAction = request.getParameter("-action");
			
		} else if (strAction.equalsIgnoreCase("aspunlock")) {
			//----
			// Unlock an entity that has been locked as part of the standard popup.
			// 
			// -e = entity
			// -zxpk = The primary key of the entity to unlock.
			//----
			String strEntity = request.getParameter("-entity");
			String strClose = request.getParameter("-close");
			String strPK = request.getParameter("-zxpk");
			
			if (StringUtil.len(strEntity) > 0) {
				if (objZX.getBOLock().haveLock(strEntity,new StringProperty(strPK),subsessionID)) {
					try {
						objZX.getBOLock().releaseLock(strEntity, new StringProperty(strPK));
					} catch (Exception e) {
						objZX.log.error("Did not release lock for pk " + strPK + " entity " + strEntity);
					}
				}
			}
	    	
			//----
			// Close subsession
			//----
			objZX.getSession().closeSubsession(subsessionID);
	    		
			//----
			// DGS22JUN2004: Changes to solve unlocking problems. 
			// This is usually done in a hidden frame of the main CPAAdvance window, so no
			// closing. However, if the main window did not exist, we will be running this  
			// in a window we have just popped up (and will have a QS value -close), so close  
			// it afterwards.
			//----
			if (strClose != null && strClose.equals("1")) {
				// Build close script
%>
<body>
	<script Language=Javascript>
	top.window.close();
	</script>
</body>
<%
			}
			
			strAction = "";
	    	
		} else if (strAction.equalsIgnoreCase("aspbuttons")) {
			//----
			// Create the buttons/tabs :
			//----
			out.write("<body bgcolor=\"white\">");
			
			//----
			// Retrieve the definition action
			//----
			objStdAction = objPageflow.getStdPopupAction(request.getParameter("-action"), 
														 request.getParameter("-zXPopupDef")); 
			objStdAction.generateButtons();
			out.write(objPage.flush());
			out.write("</body>");
			out.flush();
			
			strAction = "";
			
		} else if (strAction.equalsIgnoreCase("aspfooter")) {
			//----
			// Create the footer section
			//----
			
			//----
			// Retrieve the definition action
			//----
			objStdAction = objPageflow.getStdPopupAction(request.getParameter("-action"), request.getParameter("-zXPopupDef")); 
			
			//----
			// What happens on the onClose
			// 10OCT2003: Include a refresh of the navigation frame after the unlock - seems to solve
			// problem where unlock never gets to happen.
			//----
			StringBuffer strTmp = new StringBuffer(64);
			strTmp.append("javascript:zXStdPopupUnlock(")
				  .append("'").append(sessionID).append("'")
				  .append(",'").append(subsessionID).append("'")
				  .append(", '").append(objPageflow.resolveDirector(objStdAction.getLockentity())).append("'")
				  .append(", '").append(request.getParameter(objPageflow.resolveDirector(objStdAction.getQspk()))).append("');");
			
			//strTmp = new StringBuffer("javascript:zXCloseSubSession('" + sessionID + "','" + subsessionID + "');");
	    		
			//----
			// onBeforeUnload - Check whether the popup is dirty.
			// It may be that refresh of navigation window is de-activated
			//----
			String onBeforeUnload = "javascript:var strReturn = zXStdPopupCheck();if (strReturn != undefined && strReturn != '') event.returnValue = strReturn;";
			if (!objZX.configValue("//zX/refreshNavigationOnPopupClose").equalsIgnoreCase("no")) {
				onBeforeUnload = onBeforeUnload + "refreshNavigationFrame();";
			}
						
%>
<body bgcolor="<%=objPage.getWebSettings().getBgColorDark()%>" onBeforeUnload="<%=onBeforeUnload%>" onUnload="<%=strTmp.toString()%>">
	    	
			<table width="100%"  cellspacing=0>
			<tr height="*">

				<td width="3%">
					<img src="../images/topOfPage.gif" 
							id="top"
							width="29" height="29" 
							title="top of page"
							onMouseOver="javascript:zXIconFocus(this, 'topOfPageOver.gif');"
							onMouseOut="javascript:zXIconFocus(this, 'topOfPage.gif');"
							onMouseDown="javascript:zXStdPopupScroll(0,0);"
							align="texttop">
				</td>

				<td width="3%">
					<img src="../images/bottomOfPage.gif"  
							id="bottom"
							width="29" height="29"
							title="bottom of page"
							onMouseOver="javascript:zXIconFocus(this, 'bottomOfPageOver.gif');"
							onMouseOut="javascript:zXIconFocus(this, 'bottomOfPage.gif');"
							onMouseDown="javascript:zXStdPopupScroll(0,100000);"
							align="absbottom">
				</td>

				<td width="10">
					<img src="../images/spacer.gif">
				</td>

				<td width="4%" valign="absmiddle">
					<img src="../images/help.gif" 
							id="help"
							width="37" height="37" 
							title="Help"
							onMouseOver="javascript:zXIconFocus(this, 'helpOver.gif');"
					<%
					out.write(" onMouseDown=\"zXPopup('" + objZX.configValue("//zX/helpFile") + "');\"");
					%>
							onMouseOut="javascript:zXIconFocus(this, 'help.gif');"
							>
				</td>

				<td width="4%" valign="absmiddle">
					<img src="../images/exit.gif" 
							id="exit"
							width="37" height="37" 
							onMouseOver="javascript:zXIconFocus(this, 'exitOver.gif');"
					<%
					out.write(" onMouseDown=\"javascript:zXStdPopupClose();\" ");			
					%>
							onMouseOut="javascript:zXIconFocus(this, 'exit.gif');"
							>
				</td>

				<td width="5%">
					<img src="../images/spacer.gif">
				</td>

				<td width="*">
					<SPAN ID="spnFooter" class="zxFooter"></SPAN>
				</td>

				<td width="*">
				</td>
			</tr>
			<tr height="15">
				<td width="10">
					<img src="../images/spacer.gif">
					<p>
				</td>
			</tr>
			</table>
			</body>
			<%
			
			strAction = "";
			
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
	if (objPageflow != null) objPageflow.dumpAllErrors();
	
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>
</html>