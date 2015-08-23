<%
//======================================================================================
//
// File	:	zXMany2Many.xsp
// By	:	Bertus Dispa
// Date	:	24FEB04
// 
// Change	: BD9MAR04
// Why		: If right hand entity (-re) is large, than do not attempt to
//				show straight away but have search form
//
//  This is a generic pageflow to maintain 'poor' many to many relations (i.e.
//  the middle entity has nothing but a PK, a FK to the left entity and an FK   
//  to the right entity).
//
//  The entities are referred to as -le, -me and -re (left, middle, right) and the left
//  entity is supposed to be 'pinned' (i.e. you have one instance in mind).
//  So for example: -le = team, -me = member and -re = user
//  and you would like to use zXMany2Many to maintain the members of a specific team
//  (i.e. -le is 'pinned').
// 
//  -le = Name of left entity
//  -me = Name of middle entity
//  -re = Name of right entity
//  -pk.le = PK of pinned left entity (e.g. the id of the team as for the example)
// 
//  Alternatively you can define the entities in a pageflow action (probaby a null
//  action) and you pass the name of this action as
// 
//  -entityAction = Name of action where entities are defined; this replaces -le, -me, -re
// 		and -pk.le
// 
//  The entity names MUST be le, me and re and the following must have been set:
//  -le - Must have pk of #qs.-pk.le
//  -me - Must have pk of #qs.-pk.me, wheregroup of FK to le (and the attribute value to
// 				set value)
//  -re - Must have pk of #qs.-pk.re
// 
//  Other parameters are:
// 
//  -queryName - Optional; is used to make the saved queries unique; very usefull when
// 			zXMany2Many is used in multiple tabs in a popup (otherwise the queries in
// 			one tab replace the queries in another)
//  -noFormStart - Optional; if 1 there is no <form> tag; this can be usefull when using
// 			zXMany2Many as a link action to an page that has its own <form> (this action
// 			should than NOT have a </form> tag)
//  -noFormEnd - Optional; if 1 there is no </form> tag; usefull if the -nextAction parameter
// 			is used and points to an action that has an </form> (this action should than
// 			NOT have a <form> tag)
//  -nextAction - Optional; name of action to do next; in pageflow:action format
//  -startAction - Optional; name of action to start (e.g. after submitting form); in
// 			pageflow:action format
// -viewOnly - Optional; if set to 1 all is view only
//======================================================================================

%><%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
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
	<script type="text/javascript" language="JavaScript" src="../javascript/zXList.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXForm.js"></script>
	
	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>

<body background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed" scroll="auto">
<%
	
	// Get handle to the pageflow object
	Pageflow objPageflow = objPage.getPageflow(request, response, "zXMany2Many");
	if (objPageflow == null) {
		throw new Exception("Unable to initialise pageflow");
	}
	
	// Safeguard against development errors
	int intLoopCounter = 0;

	String strAction = request.getParameter("-a");
	if (strAction != null) {
		strAction.toLowerCase();
	} else {
		strAction = "";
	}
	
	do {
		if (strAction.equals("asp.start")) {
			//----
			//  This is the very first action that is executed
			// Check entity size of right-hand-side in context
			// 0 - small
			// 1 - medium
			// 2 - large
			//----
			ZXBO objBO = objPageflow.getEntity("listNotInGroups.query", "re").getBo();
			if (objBO == null) {
				out.write(objPage.fatalError("Unable to get handle to listNotInGroups.query/re"));
				return;
				
			}
			
			if (objBO.getDescriptor().getSize().pos == zXType.entitySize.esLarge.pos) {
				strAction = "reLarge.listInGroups.query";
			} else {
				strAction = "listInGroups.query";
			}
			
		} else {
			
			zXType.rc enmRC = objPageflow.go(strAction);
			if (enmRC.pos == zXType.rc.rcWarning.pos) {
				// Action not defined in pageflow definition file
				strAction = objPageflow.getAction();	
							
			} else {
		 		out.write(objPage.flush());
				out.flush();
				strAction = objPageflow.getAction();
			}
		}
		
		intLoopCounter ++;
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

} catch (ZXException e) {
	PageBuilder.handleJSPException(objPage, out, e);
	
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>