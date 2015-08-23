<%
//----
// File	:	zxfk.jsp
// By	:	Bertus Dispa
// Date	:	21MAR03
// 
// Support for framework standard popup view
// 
// -s = session
// -ss = sub-session
// -a = action   (frameset, button)
// 
// Change	: BD16JUN04
// Why		: Add order by to medium
// 
// Change	: DGS14JUL2004 Domarque Final push no. 72
// Why		: Add filter to listform
// 
// Change	: BD4AUG04 
// Why		: New way of working: no longer a frameset for speed of loading and also
// 				large and medium are now effectively the same
// 
// Change	: MB10NOV04 
// Why		: Do not add zXForm javascript when printing out the frameset.
// 
// Change	: BD11NOV04
// Why		: Load attribute group description.load rather than description to cater for
// 			dynamic values; this requires latest version of zX.clsDescriptor (see
// 				method getSingleGroup)
// Change	: MB/DGS24MAR2005
// Why		: Revised to fix problem where subsession lost
// 
// Change	: BD19APR05 - V1.5:4
// Why		: Made data-source aware
//-----
%><%@ page autoFlush="true" 
%><%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.property.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@ page import="org.zxframework.datasources.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:zx/><%
ZX objZX = null;
PageBuilder objPage = null;
String strQry;

String strAction = request.getParameter("-a");
if (strAction != null) {
	strAction.toLowerCase();
} else {
	strAction = "";
}

try {
	objZX = ThreadLocalZX.getZX();
	objPage = new PageBuilder();
	
	// The zX Session id :
	String strSession = objZX.getSession().getSessionid();
	
	// The Sub session id used to track actions in the popup ?
	String strSubSession = objZX.getQuickContext().getEntry("-ss");
	if (strSubSession == null) {
		strSubSession = "";
	}
%>
<html>
<head>
	<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">
	<script type="text/javascript" language="JavaScript" src="../javascript/zX.js"></script>
	
<%
	if (!strAction.equalsIgnoreCase("framesetSearchForm") && !strAction.equalsIgnoreCase("framesetShowForm")) {
%>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXForm.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXTree.js"></script>
<% 
	} %>

	<script type="text/javascript" language="JavaScript" src="../javascript/zXFooter.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXList.js"></script>
	<title>ZX Java Port :</title>
</head>

<%
	if (strAction.equalsIgnoreCase("framesetSearchForm") || strAction.equalsIgnoreCase("framesetShowForm")) {
		//---
		// When the action is framset; a frameset is defined and thus no page body
		// is allowed
		//---
	} else if (strAction.equalsIgnoreCase("aspFooter")) {
		//---
		// When the action is footer we want a background color rather than an image
		//---
%>
<body bgcolor="indigo"
		onUnload="javascript:zXCloseSubSession('<%=strSession%>','<%=strSubSession%>');" scroll="auto">
<%
	} else {
		//---
		// The actual filter page.
		//---
%>
<body background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed">
<%
	}
	
	Pageflow objPageflow = null;
	int intRC;

	//---
	// Safeguard against development errors
	//---
	int intLoopCounter = 0;
	
	do {
		if (strAction.equalsIgnoreCase("XXXframesetSearchForm")) {
			strAction = "aspPreSearchForm";
			
		} else if (strAction.equalsIgnoreCase("XXXframesetShowForm")) {
			strAction = "showForm";
			
		} else if ( 
					strAction.equalsIgnoreCase("framesetSearchForm") ||
					strAction.equalsIgnoreCase("framesetShowForm")
		 		   ) {
			//---
			// Frameset definition
			//---
%>
<frameset rows="*,0" name="top" frameborder="0" border="0" framespacing="0"> 
<%
			String strUrl = new StringBuffer("&-s=").append(strSession)
							.append("&-ss=").append(strSubSession)
							.append("&-pk=").append(request.getParameter("-pk") != null?request.getParameter("-pk"):"")
							.append("&-fke=").append(request.getParameter("-fke") != null?request.getParameter("-fke"):"")
							.append("&-fka=").append(request.getParameter("-fka") != null?request.getParameter("-fka"):"")
							.append("&-fkta=").append(request.getParameter("-fkta") != null?request.getParameter("-fkta"):"")
							.append("&-ctr=").append(request.getParameter("-ctr") != null?request.getParameter("-ctr"):"")
							.append("&-e=").append(request.getParameter("-e") != null?request.getParameter("-e"):"")
							.append("&-attr=").append(request.getParameter("-attr") != null?request.getParameter("-attr"):"")
							.append("&-esize=").append(request.getParameter("-esize") != null?request.getParameter("-esize"):"")
							.append("&-fklookup=").append(request.getParameter("-fklookup") != null?request.getParameter("-fklookup"):"")
							.append("&-fkfilterval=").append(request.getParameter("-fkfilterval") != null?request.getParameter("-fkfilterval"):"")
							.append("&-fkwhere=").append(request.getParameter("-fkwhere") != null?request.getParameter("-fkwhere"):"")
							.append("&-fkval=").append(request.getParameter("-fkval") != null?request.getParameter("-fkval"):"").toString();
			
			// Where do we start? At the search form or the list form?			
			if (strAction.equalsIgnoreCase("framesetSearchForm")) {
%>
	<frame name="fraDetails1" src="../jsp/zXFK.jsp?-a=aspPreSearchForm<%=strUrl%>"  marginwidth="10" marginheight="10" scrolling="auto" frameborder="0">
<% 
			} else {
%>
	<frame name="fraDetails1" src="../jsp/zXFK.jsp?-a=showForm<%=strUrl%>"  marginwidth="10" marginheight="10" scrolling="auto" frameborder="0">
<% 
			}
%>
	
	<frame name="fraFooter" src="../jsp/zXFK.jsp?-a=aspFooter&-s=<%=strSession%>&-ss=<%=strSubSession%>" vspace="0" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
</frameset>

<noframes>
	<body bgcolor="#FFFFFF">
	<H1>Your browser does not support framesets</H1>
	</body>
</noframes>
<%
			strAction = "";
			
	    } else if (strAction.equalsIgnoreCase("YYYaspFooter")) {
	    	//---
	    	// Generate footer bit
	    	//---
%>
<table width="100%"  cellspacing=0>
<tr height="*" valign="top">
	<td width="31">
		<img src="../images/topOfPage.gif" 
				id="top"
				width="29" height="29" 
				title="top of page"
				onMouseOver="javascript:zXIconFocus(this, 'topOfPageOver.gif');"
				onMouseOut="javascript:zXIconFocus(this, 'topOfPage.gif');"
				onMouseDown="zXScrollFrames(0,0);"
				align="texttop">
	</td>
	<td width="38">
		<img src="../images/bottomOfPage.gif"  
				id="bottom"
				width="29" height="29"
				title="bottom of page"
				onMouseOver="javascript:zXIconFocus(this, 'bottomOfPageOver.gif');"
				onMouseOut="javascript:zXIconFocus(this, 'bottomOfPage.gif');"
				onMouseDown="zXScrollFrames(0,10000);"
				align="absbottom">
	</td>
	<td width="10">
		<img src="../images/spacer.gif">
	</td><%
String strTmp = objZX.configValue("//zX/helpFile"); 
%>
	<td width="39" valign="absmiddle">
		<img src="../images/help.gif" 
			 id="help"
			 width="37" height="37" 
			 title="Help"
			 onMouseOver="javascript:zXIconFocus(this, 'helpOver.gif');"
			 onMouseDown="javascript:zXPopup('<%=strTmp%>');"
			 onMouseOut="javascript:zXIconFocus(this, 'help.gif');">
	</td>
	<td width="39" valign="absmiddle">
		<img src="../images/exit.gif" 
				id="exit"
				width="37" height="37" 
				title="Close"
				onMouseOver="javascript:zXIconFocus(this, 'exitOver.gif');"
				onMouseDown="javascript:parent.window.close();"
				onMouseOut="javascript:zXIconFocus(this, 'exit.gif');"
				>
	</td>
	<td width="50">
		<img src="../images/spacer.gif">
	</td>
	<td width="43%">
		<SPAN ID="spnFooter" class="zxFooter"></SPAN>
	</td>
	<td width="*">
	</td>
</tr>
<tr height="9">
	<td width="10">
		<img src="../images/spacer.gif">
		<p>
	</td>
</tr>
</table>
<%			
			strAction = "";
			
	    } else if (strAction.equalsIgnoreCase("aspFooter")) {
	    	//---
	    	// A light weight version of the footer
	    	//---
			strAction = "";
			
	    } else if (strAction.equalsIgnoreCase("aspPreSearchForm")) {
			if (objPageflow == null) {
	    		objPageflow = objPage.getPageflow(request, response, "zXFK");
	    	}
			
	    	//---
	    	// Check whether to give a list or a search form
	    	//---
			//---
			// BD3AUG04 - Now also for medium and large
	    	// if (request.getParameter("-esize").equalsIgnoreCase("m")) {
			//---
			if (true) {
				String strFilterValue = request.getParameter("-fkfilterval");
				String strFKWhere = request.getParameter("-fkwhere");
				
	    		if (StringUtil.len(strFilterValue) > 0 || StringUtil.len(strFKWhere) > 0) {
	    			//---
	    			// We have a special filter value.
	    			//---
					PFEntity objPFEntity = objPageflow.getEntity("searchForm", "entity");
					ZXBO objBO = objPFEntity.getBo();
					String strLoadGroup = objPFEntity.getSelectlistgroup();
					ZXBO objFilter = objPageflow.getEntity("filter", "zXFKFltr").getBo();
					
					if (StringUtil.len(strFilterValue) > 0) {
						objFilter.setValue("fltr", strFilterValue);
					}
					
					if (objBO.getDS().getDsType().pos == zXType.dsType.dstChannel.pos) {
		    			strQry = strLoadGroup;
						objPageflow.addToContext(Pageflow.QRYSELECTCLAUSE, strQry);
						
						DSWhereClause objDSWhereClause = new DSWhereClause();
						
						//----
			    		// Optional add the filter value restriction
			    		//----
						if (StringUtil.len(strFilterValue) > 0) {
							objDSWhereClause.QSClause(objBO, objFilter.getValue("fltr").getStringValue(), "QS");
						}
						
						//----
			    		// Optional add the fkwhere clause restriction
			    		//----
						if (StringUtil.len(strFKWhere) > 0) {
							objDSWhereClause.addClauseWithAND(objBO, strFKWhere);
						}
						
						strQry = objDSWhereClause.getAsWhereClause();
						objPageflow.addToContext(Pageflow.QRYWHERECLAUSE, strQry);
						
						//-----
						// And order by clause
						//-----
						strQry = "qsOrder";
						objPageflow.addToContext(Pageflow.QRYORDERBYCLAUSE, strQry);
						
					} else {
						strQry = objZX.getSql().loadQuery(objBO, strLoadGroup, true, true);
			    		objPageflow.addToContext(Pageflow.QRYSELECTCLAUSE, strQry);
			    		
			    		//----
			    		// Build the where clause.
			    		//----
						// Reset Query
			    		strQry = "";
			    		
						//----
			    		// Optional add the filter value restriction
			    		//----
			    		if (StringUtil.len(strFilterValue) > 0) {
			    			strQry = objZX.getSql().QSWhereClause(objBO, objFilter.getValue("fltr"), "QS");
			    		}
			    		
			    		//----
			    		// Optional add the fkwhere clause restriction
			    		//----
			    		if (StringUtil.len(strFKWhere) > 0) {
			    			DSWhereClause objDSWhereClause = new DSWhereClause();
							objDSWhereClause.addClauseWithAND(objBO, strFKWhere);
							
							String strFKWhereSQL = objDSWhereClause.getAsSQL();
							if (StringUtil.len(strFKWhereSQL) > 0) {
								//----
								// Only add AND when needed
								//----
								if (StringUtil.len(strQry) > 0) {
									strQry = strQry + " AND ";
								}
								
								strQry = strQry + strFKWhereSQL;
							}
						}
			    		
						objPageflow.addToContext(Pageflow.QRYWHERECLAUSE, strQry);
						
						//---
						// And order by clause
						//---
						strQry = objZX.getSql().orderByClause(objBO, "qsOrder", false);
						objPageflow.addToContext(Pageflow.QRYORDERBYCLAUSE, strQry);
						
					} // Channel or RDBMS
					
	    		} else {
					//---
		    		// Medium sized - go direct to list - construct query
		    		//---
					PFEntity objPFEntity = objPageflow.getEntity("searchForm", "entity");
					ZXBO objBO = objPFEntity.getBo();
					String strLoadGroup = objPFEntity.getSelectlistgroup();
					String strFKVal = request.getParameter("-fkval");
					String strFKA = request.getParameter("-fka");
					
		    		if (objBO.getDS().getDsType().pos == zXType.dsType.dstChannel.pos) {
		    			strQry = strLoadGroup;
						objPageflow.addToContext(Pageflow.QRYSELECTCLAUSE, strQry);
						
						//-----
						// And where clause
						//-----
						strQry = "";
						if (StringUtil.len(strFKVal) > 0) {
							DSWhereClause objDSWhereClause = new DSWhereClause();
							objBO.setValue(strFKA,new StringProperty(strFKVal));
							
							objDSWhereClause.parse(objBO, strFKA);
							
							strQry = objDSWhereClause.getAsWhereClause();
						}
						
						objPageflow.addToContext(Pageflow.QRYWHERECLAUSE, strQry);
						
						//-----
						// And order by clause
						//-----
						strQry = "qsOrder";
						objPageflow.addToContext(Pageflow.QRYORDERBYCLAUSE, strQry);
						
		    		} else {
			    		strQry = objZX.getSql().loadQuery(objBO, strLoadGroup, true, true);
			    		objPageflow.addToContext(Pageflow.QRYSELECTCLAUSE, strQry);
			    		 
			    		//---
			    		// And where clause
			    		//---
			    		if (StringUtil.len(strFKVal) != 0) {
			    			objBO.setValue(strFKA, new StringProperty(strFKVal));
							strQry = objZX.getSql().whereCondition(objBO, strFKA);
							objPageflow.addToContext(Pageflow.QRYWHERECLAUSE, strQry);
			    		}
		
						//---
						// And order by clause
						//---
						strQry = objZX.getSql().orderByClause(objBO, "qsOrder", false);
						objPageflow.addToContext(Pageflow.QRYORDERBYCLAUSE, strQry);
						
					} // Channel or RDBMS
					
	    		} // Filter search or regular search
	    		
				objZX.getQuickContext().setEntry("-sa", "editnodb");
				strAction = "filter";

	    	} else {
	    		//---
	    		// Large sized - present a search form first
	    		//---
	    		strAction = "searchForm";
	    		
	    	}
	    	
	    } else if (strAction.equalsIgnoreCase("aspPreListForm")) {
			if (objPageflow == null) {
	    		objPageflow = objPage.getPageflow(request, response, "zXFK");
			}
			
			if (StringUtil.len(request.getParameter("-fkval")) > 0) {
				
				ZXBO objBO = objPageflow.getEntity("searchForm", "entity").getBo();
				if (objBO.getDS().getDsType().pos == zXType.dsType.dstChannel.pos) {
					DSWhereClause objDSWhereClause = new DSWhereClause();
					
					objBO.setValue(request.getParameter("-fka"),new StringProperty(request.getParameter("-fkval")));
						
					objDSWhereClause.parse(objBO, request.getParameter("-fka"));

					String strOrigQry = objPageflow.getFromContext(Pageflow.QRYWHERECLAUSE);
						
					objDSWhereClause.addClauseWithAND(objBO, strOrigQry);
					
					strQry = objDSWhereClause.getAsWhereClause();

					objPageflow.addToContext(Pageflow.QRYWHERECLAUSE, strQry);
					
				} else {
					objBO.setValue(request.getParameter("-fka"),new StringProperty(request.getParameter("-fkval")));
					strQry = objZX.getSql().whereCondition(objBO, request.getParameter("-fka"));
					String strOrigQry = objPageflow.getFromContext(Pageflow.QRYWHERECLAUSE);
					
					if (StringUtil.len(strOrigQry) > 0) {
						strQry = strOrigQry + " AND " + strQry;
					}
					
					objPageflow.addToContext(Pageflow.QRYWHERECLAUSE, strQry);
				}
				
			} // Has fkval?
			
	    	// Now go to filter instead.
			objZX.getQuickContext().setEntry("-sa", "editnodb");
			strAction = "filter";
			
	    } else if (strAction.equalsIgnoreCase("aspFilter")) {
			ZXBO objFilter = objPageflow.getEntity("filter", "zXFKFltr").getBo();
			ZXBO objBO = objPageflow.getEntity("searchForm", "entity").getBo();
			
			if (objBO.getDS().getDsType().pos == zXType.dsType.dstChannel.pos) {
				DSWhereClause objDSWhereClause = new DSWhereClause();
				objDSWhereClause.QSClause(objBO, objFilter.getValue("fltr").getStringValue(), "QS");
				
				//----
				// Preserve the optional fkwhere restriction
				//----
				String strFKWhere = request.getParameter("-fkwhere");
				if (StringUtil.len(strFKWhere) > 0) {
					objDSWhereClause.addClauseWithAND(objBO, strFKWhere);
				}
				
				strQry = objDSWhereClause.getAsWhereClause();
			} else {
				strQry = objZX.getSql().QSWhereClause(objBO, objFilter.getValue("fltr"), "QS");
				
				//----
				// Preserve the optional fkwhere restriction
				//----
				String strFKWhere = request.getParameter("-fkwhere");
				if (StringUtil.len(strFKWhere) > 0) {
					DSWhereClause objDSWhereClause = new DSWhereClause();
					objDSWhereClause.addClauseWithAND(objBO, strFKWhere);
					String strFKWhereSQL = objDSWhereClause.getAsSQL();
					
					if (StringUtil.len(strQry) > 0 && StringUtil.len(strFKWhereSQL) > 0) {
						strQry = strQry + " AND ";
					}
					
					if (StringUtil.len(strFKWhereSQL) > 0) {
						strQry = strQry + strFKWhereSQL;
					}
				}
				
			} // Channel or RDBMS
			
			objPageflow.addToContext(Pageflow.QRYWHERECLAUSE, strQry);
			
			strAction = "filter";
			
	    } else {
			if (objPageflow == null) {
	    		objPageflow = objPage.getPageflow(request, response, "zXFK");
	    	}
	    	
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
	    
		//---
		// Exit early is there is over 10 linked actions :
		//---
		if (intLoopCounter > 10) {
			out.write("Loop counter exceeded 10");
			break;
		}
		intLoopCounter++;

	} while (StringUtil.len(strAction) > 0);    
	
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}

//-----------
// Important:
// When the action is framset; a framset is defined and thus no page body is allowed.
//-----------
if (!strAction.equalsIgnoreCase("framesetSearchForm") && !strAction.equalsIgnoreCase("framesetShowForm")) {
	out.write("</body>"); 
} 
%>
</html>