<%
//----
// File	:	zXStdQS.asp
// By	:	Bertus Dispa
// Date	:	24OCT03
//
// Standard zX quick search
// _s - session
// _qs - quick search
// _entity - entity to QS on
// _pageflow - pageflow that contains standard popup definition 
// _acion - action within pageflow that is the stdPopup definition
// _whereGroup - optional wheregroup to add to search; assumed to start with :
//
// Change	: BD15FEB04
// Why		: Added support for whereGroup
//
// Change	: BD11NOV04
// Why		: Load attribute group description.load rather than description to cater for
//				dynamic values; this requires latest version of zX.clsDescriptor (see
//				method getSingleGroup)
//
// Change	: BD21APR05 - V1.5:4
// Why		: Added support for data sources
//
// Change	: BD23MAY05 - V1.5:17
// Why		: Fixed bug introduced with V1.5:4; see comment
//
// Change	: V1.4:80 DGS17MAY2005
// Why		: If the generated QS where clause is empty go straight to the search form
//
//----
%><%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.property.*" 
%><%@ page import="org.zxframework.util.*"
%><%@ page import="org.zxframework.datasources.*"
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><zx:zx/><% 
ZX objZX = null;
PageBuilder objPage = null;
DSRS objRS = null;

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

	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>

<body  background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed">
<%
	Pageflow objPageflow = objPage.getPageflow(request, response, "");
	
	String strAction = "";
	String strTmp = "";
	
	// Safeguard against development errors
	 int intLoopCounter = 0;
	 zXType.rc rc = null;
	 
	 do {
		if (strAction.equalsIgnoreCase("asp.start")) {
			//----
			// Start the show
			// If QS criteria has been passed:
			//	Create and execute QS query
			//   1 row: show
			//	> 1 row: list
			//   0 rows: search
			// If no QS criteria has been passed: search form
			//----
			String strWhereClause;
			String strQS = request.getParameter("-qs");
			if (StringUtil.len(strQS)> 0) {
				//----
				// User has entered a quick search
				// Construct query
				//----
				ZXBO objBO = objZX.createBO( request.getParameter("-entity"));
				if (objBO.getDS().getDsType().pos == zXType.dsType.dstChannel.pos) {
					//-----
					// Channel
					//-----
					String strSelectClause = "+,description.load";
					objPageflow.saveQuerySelectClause("", strSelectClause);
					
					//-----
					// And where clause
					//-----
					DSWhereClause objDSWhereClause = new DSWhereClause();
					objDSWhereClause.QSClause(objBO, request.getParameter("-qs"), "QS");
					
					if (StringUtil.len(request.getParameter("-whereGroup")) > 0) {
						objDSWhereClause.addClauseWithAND(objBO, request.getParameter("-whereGroup"));	
					}
					
					strWhereClause = objDSWhereClause.getAsWhereClause();
					
					objPageflow.saveQueryWhereClause("", strWhereClause);
					
					//----
					// And order by
					//----
					objPageflow.saveQueryOrderByClause("", "");
					
					//----
					// Execute query; if there is only one row: load that straight away
					// V1.4:80 DGS17MAY2005: If the QS where clause is empty (which can happen in various
					// circumstances, such as having an alphabetic string entered and all QS attrs being
					// numeric), go straight to the search form
					//----
					if (StringUtil.len(strWhereClause) > 0) {
						objRS = objBO.getDS().boRS(objBO, "+,description.load", ":" + strWhereClause, true);
					}
					
				} else {
					//----
					// RDBMS
					//-----
					String strSelectClause = objZX.getSql().loadQuery(objBO, "+,description.load", true, false);
					objPageflow.saveQuerySelectClause("", strSelectClause);
					
					//----
					// And where clause
					//----
					strWhereClause = objZX.getSql().QSWhereClause(objBO,
																  new StringProperty(strQS),
																  "QS");
					//----
					//  V1.5:17 - Need to enclose in '(' and ')' to avoid queries like
					//
					//	where order.client = client.id and 
					//				client.name like ?%dispa%? or order.summary like ?%dispa%'
					//
					//	where it should be:
					//
					//	where order.client = client.id and 
					//				(client.name like ?%dispa%? or order.summary like ?%dispa?%)
					//----
					if (StringUtil.len(strWhereClause) > 0) {
						strWhereClause = "(" + strWhereClause + ")";	
					}
					
					String strWhereGroup = request.getParameter("-whereGroup");
					if (StringUtil.len(strWhereGroup) > 0){
						strTmp = objZX.getSql().whereCondition(objBO, strWhereGroup);
						
						if (StringUtil.len(strTmp) > 0) {
							if (StringUtil.len(strWhereClause) > 0) {
								strWhereClause = strWhereClause + " AND " + strTmp;
							} else {
								strWhereClause = strTmp;
							}
						}
					}
					
					objPageflow.saveQueryWhereClause("", strWhereClause);
					
					//----
					// And order by
					//----
					objPageflow.saveQueryOrderByClause("", "");
					
					//----
					// Execute query; if there is only one row: load that straight away
					// V1.4:80 DGS17MAY2005: If the QS where clause is empty (which can happen in various
					// circumstances, such as having an alphabetic string entered and all QS attrs being
					// numeric), go straight to the search form
					//----
					if (StringUtil.len(strWhereClause) > 0) {
						objRS = ((DSHRdbms)objBO.getDS()).sqlRS(strSelectClause + " AND " + strWhereClause);
					}
					
				} // Channel or RDBMS
				
				if (StringUtil.len(strWhereClause) == 0) {
					objPageflow.setInfoMsg("Not able to match using quick search criteria '" + strQS + "'");
					strAction = "search";
					
				} else if (objRS == null) {
					out.write(objPage.fatalError("Unable to execute query"));
					return;
					
				} else {
					// Try and see how many records returned : 
					if (!objRS.eof()) {
						objRS.rs2obj(objBO, "+", true);
						objRS.moveNext();
						
						if (!objRS.eof()) {
							//----
							// More than one entry
							//----
							strAction = "list";
							
						} else {
							//----
							// Only one row
							//----
							objZX.getQuickContext().setEntry("-pk", objBO.getPKValue().getStringValue());
							
							strAction = "show";
						}
						
					} else {
						//----
						// No rows found
						//----
						objPageflow.setInfoMsg("No records found that match search criteria '" + strQS + "'");
						
						strAction = "search";
						
					}
				}
				
			} else {
				//----
				// No QS criteria given
				//----
				strAction = "search";
			}
			
		} else {
			
		 	rc = objPageflow.go(strAction);    
		 	if (rc.equals(zXType.rc.rcOK)) {
		 		out.write(objPage.flush());
				out.flush();
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
	
	//----
	// And store the session if not logged-off
	//----
	if (StringUtil.len(objZX.getSession().getSessionid()) > 0) {
		objZX.getSession().store();
	}
	
	objPageflow.dumpAllErrors();	
	
	if (intLoopCounter >= 20) { 
		out.write(objPage.fatalError("Unprocessed action : " + strAction));	
		// return;
	}
	
%>
<zx:timer action="stop"/>
</body>
</html>
<%
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objRS != null) objRS.RSClose();
	if (objZX != null) objZX.cleanup();
}
%>