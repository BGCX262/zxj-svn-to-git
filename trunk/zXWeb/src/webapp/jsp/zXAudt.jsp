<%
//---
// File	:	zXAudt.asp
// By	:	David Swann
// Date	:	MAR2004
//
// Business Object Audit ASP
//
// Change	: BD6AUG04
// Why		: Promoted from Domarque to zX 
//
// Change	: BD28FEB05 - V1.4:51
// Why		: Also handle id to be part of the audit group 
//----
%><%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@ page import="org.zxframework.property.*" 
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
	<script type="text/javascript" language="JavaScript" src="../javascript/zXTree.js"></script>
	
	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>

<body background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed" scroll="auto">
<%
	//Create html and pageflow objects
	Pageflow objPageflow = objPage.getPageflow(request, response, "");
	
	// Standard pageflow include file zXInclude.asp is included next. It includes standard
	// variables and subroutines and initializes zX, HTML and pageflow objects.
	
	// Safeguard against development errors
	int intLoopCounter = 0;
	
	int intRC;
	String strAction = "";
	do {
		if (strAction.equalsIgnoreCase("audt.compare.asp")) {
			//--- 
			// The user wants to compare two audit entries so we have to prepare the
			// BOs in of the related edit form
			//
			// In:
			// -pk.audt = The main audit entry
			// -pk.otherAudt = The other audit
			// 
			// Out:
			// -compareGroup: group to use with fields that are different
			//--- 
			ZXBO objBO1 = objPageflow.getEntity("compare.edit", "entity1").getBo();
			if (objBO1 == null) {
				out.write(objPage.fatalError("Unable to get handle to audited business object"));
				return;
			}
			
			ZXBO objBO2 = objPageflow.getEntity("compare.edit", "entity2").getBo();
			if (objBO2 == null) {
				out.write(objPage.fatalError("Unable to get handle to audited business object"));
				return;
			}
			
			Audit objAudt1 = (Audit)objZX.quickLoad("zXAudt", new LongProperty(new Long(request.getParameter("-pk.audt")), false) );
			if (objAudt1 == null) {
				out.write(objPage.fatalError("Unable to get handle to audit"));
				return;
			}
			
			ZXBO objAudtTpe = objAudt1.quickFKLoad("audtTpe");
			if (objAudtTpe == null) {
				out.write(objPage.fatalError("Unable to get related audit type"));
				return;
			}
			
			objBO1.xml2bo(objAudt1.getValue("dscrptn").getStringValue(), 
						  objAudtTpe.getValue("audtGrp").getStringValue());
			
			ZXBO objAudt2 = objZX.quickLoad("zXAudt", new LongProperty(new Long(request.getParameter("-pk.otherAudt")), false));
			if (objAudt2 == null) {
				out.write(objPage.fatalError("Unable to get handle to audit"));
				return;
			}
			
			objBO2.xml2bo(objAudt2.getValue("dscrptn").getStringValue(), objAudtTpe.getValue("audtGrp").getStringValue());
			
			objAudt1.createDifferenceGroup(objBO1, objBO2, 
										   objAudtTpe.getValue("audtGrp").getStringValue(), "__difference");
			
			objZX.getBOContext().setEntry("audt", objAudt1);
			objZX.getBOContext().setEntry("otherAudt", objAudt2);
			
			objZX.getQuickContext().setEntry("-sa", "editNODB");
			
			if(objBO1.getDescriptor().getGroup("__difference").count() == 0) {
				objPageflow.setInfoMsg("No differences");
			}
			
			strAction = "compare.edit";
			
		} else if (strAction.equalsIgnoreCase("audt.view.asp")) {
			Audit objAudt = (Audit)objZX.quickLoad("zXAudt", new LongProperty( new Long(request.getParameter("-pk.audt")), false));
			if (objAudt == null) {
				out.write(objPage.fatalError("Unable to get handle to audit"));
				return;
			}

			ZXBO objAudtTpe = objAudt.quickFKLoad("audtTpe");
			if (objAudtTpe == null) {
				out.write(objPage.fatalError("Unable to get related audit type"));
				return;
			}
			
			//----
			// If the audit type has an audit attr group, the view of the audit row will link to
			// another editform action where the contents of the inserted or updated bo are shown.
			// This will not be the case if there is nothing in that attr group, nor if the
			// persist action is deleted (2).
			//----
			if (objAudtTpe.getValue("audtGrp").isNull || objAudt.getValue("prsstActn").longValue() == 2) {
				ZXBO objAudtView = objPageflow.getEntity("audt.query", "audt").getBo();
				objAudt.bo2bo(objAudtView);
				
				strAction = "audt.view.edit";
			
			} else {
				objZX.getQuickContext().setEntry("-entty", objAudtTpe.getValue("entty").getStringValue());
				objZX.getQuickContext().setEntry("-audtGrp", objAudtTpe.getValue("audtGrp").getStringValue());
				
				ZXBO objAudtView = objPageflow.getEntity("audt.view.bo.edit", "audt").getBo();
				objAudt.bo2bo(objAudtView);
				
				ZXBO objBO = objPageflow.getEntity("audt.view.bo.edit", "entity").getBo();
				if (objBO == null) {
					throw new Exception("Unable to get handle to audited business object " 
							            + objAudtTpe.getValue("entty").getStringValue());
				}

				objBO.xml2bo(objAudt.getValue("dscrptn").getStringValue(), 
							 objAudtTpe.getValue("audtGrp").getStringValue());
				
				//-----
				//  Pass the PK in the query string so this can be used as the PK of the
				// edit form
				//-----
				objZX.getQuickContext().setEntry("-pk.entity", objBO.getPKValue().getStringValue());
				
				strAction = "audt.view.bo.edit";
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
	if (objPageflow != null) objPageflow.dumpAllErrors();
	
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