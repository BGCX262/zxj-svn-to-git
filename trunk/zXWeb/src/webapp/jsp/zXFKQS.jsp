<%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.property.*" 
%><%@ page import="org.zxframework.util.*"
%><%@ page import="org.zxframework.datasources.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:zx jsheader="false"/><%
// FKLoopup AJAX Service
//
// Parameters passed :
// -s 		: Session ID
// -fke		: Foreign entity
// -e 		: Original entity with the foriegn key
// -attr 	: FK attribute
// -fkval	: Starting value. What we are looking for.
// -fka		: Foreign entity attr
// -fkta	: Foreign entity attr to
// -fkwhere : Optional where clause retriction
ZX objZX = null;
DSRS objRS = null;
try {
	objZX = ThreadLocalZX.getZX();
	
	// What we are searching for.
	String strSearch = request.getParameter("-fkval");
	// The entity we are doing the search on.
	String strFKEntity = request.getParameter("-fke");
	
	// The entity with the foriegn key
	String strEntity = request.getParameter("-e");
	// The attribute with the foriegn key
	String strAttr = request.getParameter("-attr");
	
	// What the where clause filter is
	String strFKWhere = request.getParameter("-fkwhere");
	
	if (StringUtil.len(strSearch) > 0 && StringUtil.len(strFKEntity)> 0) {
		
		// Maximum number of results to return.
		int i = 0;
		int intFKMaxSize = objZX.getSettings().getWebSettings().getFkMaxSize();
		
		// Resolve the FK Label 
		ZXBO objBO = objZX.createBO(strEntity);
		Attribute objAttr = objBO.getDescriptor().getAttribute(strAttr);
		String strFKLabelGroup;
		if (StringUtil.len(objAttr.getFkLabelGroup()) > 0) {
			strFKLabelGroup = objAttr.getFkLabelGroup();
		} else {
			strFKLabelGroup = "label";
		}
		
		String strFKLabelExpression = objAttr.getFkLabelExpression();
		boolean blnFKLabelExpression = StringUtil.len(strFKLabelExpression) > 0;
		
		// Do this manually to make this very light weight
		ZXBO objFKBO = objZX.createBO(strFKEntity);
		
		String strQS = "QS";
		if (objFKBO.getDescriptor().getGroup(strQS).size() == 0) {
			// Handle a empty QS attribute group :
			strQS = "+," + strFKLabelGroup;
		}
		
		String strQry;
		if (objFKBO.getDS().getDsType().pos == zXType.dsType.dstChannel.pos) {
			//----
			// Build where clause.
			//----
			DSWhereClause objDSWhereClause = new DSWhereClause();
			objDSWhereClause.QSClause(objFKBO, strSearch, strQS);
			
			//----
			// Add a optional where clause filter
			//----
			if (StringUtil.len(strFKWhere) > 0) {
				objDSWhereClause.addClauseWithAND(objFKBO, strFKWhere);
			}
			
			strQry = objDSWhereClause.getAsCompleteWhereClause();
			
			objRS = objFKBO.getDS().boRS(objFKBO,
                                       "+," + strFKLabelGroup,
                                       strQry,
                                       true,
                                       strFKLabelGroup, false,
                                       0, intFKMaxSize);
			
		} else {
			//----
			// Main select query.
			//----
    		strQry = objZX.getSql().selectQuery(objFKBO, "+," + strFKLabelGroup, true) + " AND ";
    		
    		//----
    		// And where clause
    		//----
			String strQSQry = objZX.getSql().QSWhereClause(objFKBO, new StringProperty(strSearch),  strQS);
			
			//----
			// ... well maybe the QS group is invalid
			//----
			if (StringUtil.len(strQSQry) == 0) {
				strQSQry = objZX.getSql().QSWhereClause(objFKBO, new StringProperty(strSearch),  "+," + strFKLabelGroup);
			}
			
			//----
			// Add a optional where clause filter
			//----
			if (StringUtil.len(strFKWhere) > 0) {
				DSWhereClause objDSWhereClause = new DSWhereClause();
				objDSWhereClause.addClauseWithAND(objFKBO, strFKWhere);
				String strSQL = objDSWhereClause.getAsSQL();
				if (StringUtil.len(strSQL) > 0) {
					if (StringUtil.len(strQSQry) > 0) {
						strQSQry = strQSQry + " AND ";
					}
					strQSQry = strQSQry + strSQL;
				}
			}
			
			strQry += strQSQry;
			
			//----
			// And order by clause
			//----
			strQry += objZX.getSql().orderByClause(objFKBO, strFKLabelGroup, false);
			
			objRS = ((DSHRdbms)objFKBO.getDS()).sqlRS(strQry, 0, intFKMaxSize);
			
		} // Channel or RDBMS
		
		out.write("<ul>");
		
		String strFKPKValue;
		
		while (!objRS.eof() && i < intFKMaxSize) {
			objRS.rs2obj(objFKBO, "+," + strFKLabelGroup);
			strFKPKValue = objFKBO.getPKValue().getStringValue();
			
			// The id used by the callee form.
			out.write("<li id='" + strFKPKValue + "'>"); 
			
			if (blnFKLabelExpression) {
				objBO.setValue(strAttr, strFKPKValue);
				objBO.getValue(strAttr).resolveFKLabel(false);
				
				out.write(objBO.getValue(strAttr).getFkLabel());
				
			} else {
				out.write(objFKBO.formattedString(strFKLabelGroup));
				
			}
			
			out.write("</li>");
						
			i++;
			objRS.moveNext();
		}
		
		objRS.RSClose();
		objRS = null;
		
		// This is to open up the search window
		if (intFKMaxSize <= i || i == 0) {
			out.write("<li id='...'>...</li>");
		}
		
		out.write("</ul>");
		
	} else {
		//----
		// No value has been sent.
		//----
		out.write("<ul>");
		out.write("<li id='...'>...</li>");
		out.write("</ul>");
		
	} 
	
} catch (Exception e) {
	try {
		if (objZX != null){
			out.write(objZX.trace.formatStack(true));
		} else {
			out.write(e.getMessage());
		}
	} catch (Exception e1) {}
	
} finally {
	if (objRS != null) objRS.RSClose();
	if (objZX != null) objZX.cleanup();
}
%>