/*
 * Created on Jun 9, 2005
 * $Id: DBQuery.java,v 1.1.2.16 2006/07/17 16:10:54 mike Exp $
 */
package org.zxframework.doc;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zxframework.Attribute;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSWhereClause;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.sql.QueryDef;
import org.zxframework.util.DateUtil;
import org.zxframework.util.DomElementUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * The doc builder query action.
 * 
 * <pre>
 * 
 * TODO : DumpAsXML does not match parsing code : query/queydef for example
 * 
 * Who    : Bertus Dispa
 * When   : 17 May 2003
 * 
 * Change    : BD1AUG03
 * Why       : Implemented query definition
 *
 * Change    : BD28NOV03
 * Why       : Fixed bug: alow for empty where group
 *
 * Change    : BD5APR05 - V1.5:1
 * Why       : Added support for data sources
 * </pre>
 *  
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBQuery extends DBAction {
	
	private static Log log = LogFactory.getLog(DBQuery.class);
	
	//------------------------ Members
	
	private zXType.docBuilderQueryType queryType;
	private String querydef;
	private String entityl;
	private String entitym;
	private String entityr;
	private boolean disTinct;
	private boolean outerJoin;
	private boolean resolveFK;
	private DBQueryExpr queryexpr;
	private String queryname;
	
	//------------------------ XML Element constants
	
	private static final String DBQUERY_QUERYNAME = "queryname";
	private static final String DBQUERY_RESOLVEFK = "resolvefk";
	private static final String DBQUERY_DISTINCT = "distinct";
	private static final String DBQUERY_OUTERJOIN = "outerjoin";
	private static final String DBQUERY_QUERYTYPE = "type";
	private static final String DBQUERY_ENTITYL = "entityl";
	private static final String DBQUERY_ENTITYM = "entitym";
	private static final String DBQUERY_ENTITYR = "entityr";
	private static final String DBQUERY_QUERY = "query";
	private static final String DBQUERY_QUERYEXPR = "queryexpr";
	
	private static final String DBQUERYEXPR_LHS = "lhs";
	private static final String DBQUERYEXPR_RHS = "rhs";
	private static final String DBQUERYEXPR_ATTRLHS = "attrlhs";
	private static final String DBQUERYEXPR_ATTRRHS = "attrrhs";
	private static final String DBQUERYEXPR_OPERATOR = "operator";
	private static final String DBQUERYEXPR_QUERYEXPRLHS = "queryexprlhs";
	private static final String DBQUERYEXPR_QUERYEXPRRHS = "queryexprrhs";
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public DBQuery() {
		super();
	}
	
	//------------------------ Members
	
	/**
	 * @return Returns the distinct.
	 */
	public boolean isDisTinct() {
		return disTinct;
	}
	
	/**
	 * @param distinct The distinct to set.
	 */
	public void setDisTinct(boolean distinct) {
		this.disTinct = distinct;
	}
	
	/**
	 * @param distinct The distinct to set.
	 */
	public void setDistinct(String distinct) {
		this.disTinct = StringUtil.booleanValue(distinct);
	}
	
	/**
	 * @return Returns the entityl.
	 */
	public String getEntityl() {
		return entityl;
	}
	
	/**
	 * @param entityl The entityl to set.
	 */
	public void setEntityl(String entityl) {
		this.entityl = entityl;
	}
	
	/**
	 * @return Returns the entitym.
	 */
	public String getEntitym() {
		return entitym;
	}
	
	/**
	 * @param entitym The entitym to set.
	 */
	public void setEntitym(String entitym) {
		this.entitym = entitym;
	}
	
	/**
	 * @return Returns the entityr.
	 */
	public String getEntityr() {
		return entityr;
	}
	
	/**
	 * @param entityr The entityr to set.
	 */
	public void setEntityr(String entityr) {
		this.entityr = entityr;
	}
	
	/**
	 * @return Returns the outerjoin.
	 */
	public boolean isOuterJoin() {
		return outerJoin;
	}
	
	/**
	 * @param outerjoin The outerjoin to set.
	 */
	public void setOuterJoin(boolean outerjoin) {
		this.outerJoin = outerjoin;
	}
	
	/**
	 * @param outerjoin The outerjoin to set.
	 */
	public void setOuterjoin(String outerjoin) {
		this.outerJoin = StringUtil.booleanValue(outerjoin);
	}
	
	/**
	 * @return Returns the querydef.
	 */
	public String getQuerydef() {
		return querydef;
	}
	
	/**
	 * @param querydef The querydef to set.
	 */
	public void setQuerydef(String querydef) {
		this.querydef = querydef;
	}
	
	/**
	 * @return Returns the queryexpr.
	 */
	public DBQueryExpr getQueryexpr() {
		return queryexpr;
	}
	
	/**
	 * @param queryexpr The queryexpr to set.
	 */
	public void setQueryexpr(DBQueryExpr queryexpr) {
		this.queryexpr = queryexpr;
	}
	
	/**
	 * @return Returns the queryname.
	 */
	public String getQueryname() {
		return queryname;
	}
	
	/**
	 * @param queryname The queryname to set.
	 */
	public void setQueryname(String queryname) {
		this.queryname = queryname;
	}
	
	/**
	 * @return Returns the queryType.
	 */
	public zXType.docBuilderQueryType getQueryType() {
		return queryType;
	}
	
	/**
	 * @param queryType The queryType to set.
	 */
	public void setQueryType(zXType.docBuilderQueryType queryType) {
		this.queryType = queryType;
	}
	
	/**
	 * @return Returns the resolveFK.
	 */
	public boolean isResolveFK() {
		return resolveFK;
	}
	
	/**
	 * @param resolveFK The resolveFK to set.
	 */
	public void setResolveFK(boolean resolveFK) {
		this.resolveFK = resolveFK;
	}
	
	/**
	 * @param resolveFK The resolveFK to set.
	 */
	public void setResolvefk(String resolveFK) {
		this.resolveFK = StringUtil.booleanValue(resolveFK);
	}
	
	//------------------------ Digester helper methods
	
	/**
	 * @param queryType The queryType to set.
	 */
	public void setQuerytype(String queryType) {
		this.queryType = zXType.docBuilderQueryType.getEnum(queryType);
	}
	
	//------------------------ DBAction implemented methods.
	
    /**
     * @see org.zxframework.doc.DBAction#dumpAsXML(XMLGen)
     */
    public void dumpAsXML(XMLGen objXMLGen) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        try {
        	// Call the super to get the first generic parts of the xml.
            super.dumpAsXML(objXMLGen);
            
            objXMLGen.taggedCData(DBQUERY_QUERYNAME, getQueryname(), false);
        	objXMLGen.taggedValue(DBQUERY_QUERYTYPE, zXType.valueOf(getQueryType()));
            objXMLGen.taggedCData(DBQUERY_QUERY, getQuerydef(), false);
            objXMLGen.taggedCData(DBQUERY_ENTITYL, getEntityl(), false);
            objXMLGen.taggedCData(DBQUERY_ENTITYM, getEntitym(), false);
            objXMLGen.taggedCData(DBQUERY_ENTITYR, getEntityr(), false);
            objXMLGen.taggedValue(DBQUERY_DISTINCT, isDisTinct());
            objXMLGen.taggedValue(DBQUERY_OUTERJOIN, isOuterJoin());
            objXMLGen.taggedValue(DBQUERY_RESOLVEFK, isResolveFK());
            
            if (getQueryexpr() != null) {
            	dumpQueryExprAsXML(DBQUERY_QUERYEXPR, getQueryexpr());
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Dump xml", e);
            throw new NestableRuntimeException("Failed to : Dump xml", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * @param pstrName The name of the xml tag
     * @param pobjQryExpr The QueryExpr
     * @return Returns the return code of the method.
     */
    public zXType.rc dumpQueryExprAsXML(String pstrName, DBQueryExpr pobjQryExpr) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc dumpQueryExprAsXML = zXType.rc.rcOK;
        
        try {
        	if (pobjQryExpr == null) {
        		return dumpQueryExprAsXML;
        	}
        	
    		/**
    		 * Don't generate anything if not recursive and nothing in either side - otherwise
    		 * get pointless tag with just the operator in, which means nothing.
    		 */
    		if (!pobjQryExpr.isOperatorRecursive() 
    		    && (pobjQryExpr.getLhs() == null && pobjQryExpr.getRhs() == null) ) {
        		return dumpQueryExprAsXML;
    		}
    		
			XMLGen objXMLGen = getDocBuilder().getDescriptor().getXMLGen();
			
			objXMLGen.openTag(pstrName);
			objXMLGen.taggedCData(DBQUERYEXPR_OPERATOR, zXType.valueOf(pobjQryExpr.getOpeRator()));
			objXMLGen.taggedCData(DBQUERYEXPR_LHS, pobjQryExpr.getLhs());
			objXMLGen.taggedCData(DBQUERYEXPR_RHS, pobjQryExpr.getLhs());
			objXMLGen.taggedCData(DBQUERYEXPR_ATTRLHS, pobjQryExpr.getAttrlhs());
			objXMLGen.taggedCData(DBQUERYEXPR_ATTRRHS, pobjQryExpr.getAttrrhs());
			
			if (pobjQryExpr.isOperatorRecursive()) {
				dumpQueryExprAsXML(DBQUERYEXPR_QUERYEXPRLHS, pobjQryExpr.getQryexprlhs());
				dumpQueryExprAsXML(DBQUERYEXPR_QUERYEXPRRHS, pobjQryExpr.getQryexprrhs());
			}
			
			/**
			 * DGS09JUN2003: Was not prepending strReturn to this, so lost all previous lines' 
			 * good work and returned invalid XML of just the closing tag:
			 */
			objXMLGen.closeTag(pstrName);
			
			return dumpQueryExprAsXML;
			
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Dump xml", e);
            throw new NestableRuntimeException("Failed to : Dump xml", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * @see org.zxframework.doc.DBAction#parse(Element)
     */
    public zXType.rc parse(Element pobjXMLNode) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc parse = zXType.rc.rcOK;
        
        try {
        	DomElementUtil element;
			String nodeName;
			Node node;
			
			NodeList nodeList = pobjXMLNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				if (node instanceof Element) {
					element = new DomElementUtil((Element)node);
					nodeName = node.getNodeName();
					
					if (DBQUERY_QUERYNAME.equalsIgnoreCase(nodeName)) {
						setQueryname(element.getText());
						
					} else if (DBQUERY_RESOLVEFK.equalsIgnoreCase(nodeName)) {
						setResolvefk(element.getText());
						
					} else if (DBQUERY_DISTINCT.equalsIgnoreCase(nodeName)) {
						setDistinct(element.getText());
						
					} else if (DBQUERY_OUTERJOIN.equalsIgnoreCase(nodeName)) {
						setOuterjoin(element.getText());
						
					} else if (DBQUERY_QUERYTYPE.equalsIgnoreCase(nodeName)) {
						setQuerytype(element.getText());
						
					} else if (DBQUERY_ENTITYL.equalsIgnoreCase(nodeName)) {
						setEntityl(element.getText());
						
					} else if (DBQUERY_ENTITYM.equalsIgnoreCase(nodeName)) {
						setEntitym(element.getText());
						
					} else if (DBQUERY_ENTITYR.equalsIgnoreCase(nodeName)) {
						setEntityr(element.getText());
						
					} else if (DBQUERY_QUERY.equalsIgnoreCase(nodeName)) {
						setQuerydef(element.getText());
						
					} else if (DBQUERY_QUERYEXPR.equalsIgnoreCase(nodeName)) {
						this.queryexpr = new DBQueryExpr();
						parseQueryExpr((Element)node, this.queryexpr);
						
					}
				}
			}
        	
        	return parse;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Parse Action", e);
            throw new NestableRuntimeException("Failed to : Parse Action", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(parse);
                getZx().trace.exitMethod();
            }
        }    
    }
    
    /**
     * Parse QueryExpr.
     * 
     * @param pobjXMLNode XML definition
     * @param pobjQryExpr The DBQueryExpr
     * @return Return the return code the method.
     */
    public zXType.rc parseQueryExpr(Element pobjXMLNode, DBQueryExpr pobjQryExpr) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc parse = zXType.rc.rcOK;
        
        try {
        	DomElementUtil element;
			String nodeName;
			Node node;
			
			NodeList nodeList = pobjXMLNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				if (node instanceof Element) {
					element = new DomElementUtil((Element)node);
					nodeName = node.getNodeName();
					
					if (DBQUERYEXPR_LHS.equalsIgnoreCase(nodeName)) {
						pobjQryExpr.setLhs(element.getText());
						
					} else if (DBQUERYEXPR_RHS.equalsIgnoreCase(nodeName)) {
						pobjQryExpr.setRhs(element.getText());
						
					} else if (DBQUERYEXPR_ATTRLHS.equalsIgnoreCase(nodeName)) {
						pobjQryExpr.setAttrlhs(element.getText());
						
					} else if (DBQUERYEXPR_ATTRRHS.equalsIgnoreCase(nodeName)) {
						pobjQryExpr.setAttrrhs(element.getText());
						
					} else if (DBQUERYEXPR_OPERATOR.equalsIgnoreCase(nodeName)) {
						pobjQryExpr.setOperator(element.getText());
						
					} else if (DBQUERYEXPR_QUERYEXPRLHS.equalsIgnoreCase(nodeName)) {
						pobjQryExpr.setQryexprlhs(new DBQueryExpr());
						parseQueryExpr((Element)node, pobjQryExpr.getQryexprlhs());
						
					} else if (DBQUERYEXPR_QUERYEXPRRHS.equalsIgnoreCase(nodeName)) {
						pobjQryExpr.setQryexprrhs(new DBQueryExpr());
						parseQueryExpr((Element)node, pobjQryExpr.getQryexprlhs());
						
					}
				}
			}
        	
        	return parse;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Parse QueryExpr", e);
            throw new NestableRuntimeException("Failed to : Parse QueryExpr", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(parse);
                getZx().trace.exitMethod();
            }
        }    
    }
    
	/**
	 * @see org.zxframework.doc.DBAction#go(org.zxframework.doc.DocBuilder)
	 */
	public zXType.rc go(DocBuilder pobjDocBuilder) throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        zXType.rc go = zXType.rc.rcOK;
        
        try {
        	
        	if (this.queryType.equals(zXType.docBuilderQueryType.dbqtAll)) {
        		go = goAll(pobjDocBuilder);
        		
        	} else if (this.queryType.equals(zXType.docBuilderQueryType.dbqtAll)) {
        		go = goQueryDef(pobjDocBuilder);
        	}
        	
        	return go;
        } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process DBQuery action.", e);
	        
	        if (getZx().throwException) throw new ZXException(e);
	        go = zXType.rc.rcError;
	        return go;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(go);
                getZx().trace.exitMethod();
            }
        } 
	}
	
	/**
	 * Build query for type = all.
	 * 
	 * @param pobjDocBuilder The doc builder
	 * @return Returns the return code of the method
	 * @throws ZXException Thrown if goAll fails.
	 */
	private zXType.rc goAll(DocBuilder pobjDocBuilder) throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        zXType.rc goAll = zXType.rc.rcOK;
        
        try {
        	DSWhereClause objDSWhereClause = new DSWhereClause();
        	String strTmp;
        	Iterator iter;
    		DBEntity objEntity;
        	
        	/**
        	 * Get entities
        	 */
        	Map colEntities = pobjDocBuilder.resolveEntities(this);
        	if (colEntities == null) {
        		throw new ZXException("Failed to get entities");
        	}
        	
        	/**
        	 * See if we do not break any rules
        	 */
        	if (pobjDocBuilder.validDataSourceEntities(colEntities)) {
        		throw new ZXException("Unsupported combination of data-source handlers");
        	}
        	
        	DBEntity objTheEntity = (DBEntity)colEntities.values().iterator().next();
        	DSHandler objDSHandler = objTheEntity.getDsHandler();
        	
        	String strSelect = "";
        	if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
        		strSelect = getZx().resolveDirector(objTheEntity.getLoadgroup());
        		
        	} else {
        		/**
        		 * Prepare array that is used as a convenient way to pass parameters
        		 * to query builder
        		 */
                int arrSize = colEntities.size();
                ZXBO[] arrZXBO = new ZXBO[arrSize];
                String[] arrLoadGroupStr = new String[arrSize];
                boolean[] arrResolveBln = new boolean[arrSize];
                int j = 0;
                
                iter = colEntities.values().iterator();
                while(iter.hasNext()) {
                    objEntity = (DBEntity)iter.next();
                    
                    String strLoadGroup = getZx().resolveDirector(objEntity.getLoadgroup());
                    if (StringUtil.len(strLoadGroup) > 0) {
                        arrZXBO[j] = objEntity.getBo();
                        arrLoadGroupStr[j] = strLoadGroup;
                        arrResolveBln[j] = isResolveFK();
                        j++;
                    }
                }
                
                strSelect = getZx().getSql().selectQuery(arrZXBO, 
                										 arrLoadGroupStr, 
                										 arrResolveBln, 
                										 this.disTinct, this.outerJoin, 
                										 false, false, false);
                
                if (StringUtil.len(strSelect) == 0) {
                	throw new Exception("Unable to generate select query");
                }
        		
        	} // Channel versus RDBMS
        	
        	/**
        	 * And store it in the context
        	 */
        	String strQueryName = getZx().resolveDirector(this.queryname);
        	pobjDocBuilder.setInContext(strQueryName + DocBuilder.QRYSELECTCLAUSE,
        								strSelect);
        	
        	/**
        	 * Load all instances in case we refer to them in any director
        	 */
        	pobjDocBuilder.loadEntities(colEntities);
        	
        	/**
        	 * Add any conditions to whereclause based on pkWhereGroup
        	 */
        	StringBuffer strWhere = new StringBuffer();
        	String strWhereGroup;
        	if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
        		strWhereGroup = getZx().resolveDirector(objTheEntity.getPkwheregroup());
        		if (StringUtil.len(strWhereGroup) > 0) {
        			pobjDocBuilder.handleAttrValues(objTheEntity);
        			
        			objDSWhereClause.addClauseWithAND(objTheEntity.getBo(), ":" + strWhereGroup);
        			
        		} // Has where group
        		
        	} else {
        		iter = colEntities.values().iterator();
        		while(iter.hasNext()) {
        			objEntity = (DBEntity)iter.next();
        			
        			strWhereGroup = getZx().resolveDirector(objEntity.getPkwheregroup());
        			if (StringUtil.len(strWhereGroup) > 0) {
        				pobjDocBuilder.handleAttrValues(objEntity);
        				
        				strTmp = getZx().getSql().processWhereGroup(objEntity.getBo(), strWhereGroup);
        				if (StringUtil.len(strTmp) > 0) {
        					if (strWhere.length() > 0) {
        						strWhere.append(" AND ");
        					}
        					strWhere.append(strTmp);
        					
        				}
        				
        			} // Has where group
        			
        		} // Loop over entieis
        		
        	} // Channel or RDBMS
        	
        	/**
        	 * And query expression stuff
        	 */
        	strTmp = processQueryExpr(this.queryexpr, colEntities);
        	if (StringUtil.len(strTmp) > 0) {
        		if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
        			objDSWhereClause.addClauseWithAND(objTheEntity.getBo(), ":" + strTmp);
        			
        		} else {
        			if (strWhere.length() > 0) {
        				strWhere.append(" AND ( ").append(strTmp).append(" ) ");
        			} else {
        				strWhere.append(strTmp);
        			}
        			
        		} // Channel or RDBMS
        		
        	} // Has result of query expression
        	
        	/**
        	 * And store it in the context
        	 */
        	pobjDocBuilder.setInContext(strQueryName + DocBuilder.QRYWHERECLAUSE,
        								strWhere.toString());
        	
        	/**
        	 * And the order by stuff
        	 */
        	StringBuffer strOrderBy = new StringBuffer();
        	iter = colEntities.values().iterator();
        	while (iter.hasNext()) {
        		objEntity = (DBEntity)iter.next();
        		
        		String strOrderByGroup = getZx().resolveDirector(objEntity.getOrderbygroup());
        		if (StringUtil.len(strOrderByGroup) > 0) {
        			if (strOrderByGroup.charAt(0) == '-') {
        				strTmp = getZx().getSql().orderByClause(
        							objEntity.getBo(),
        							strOrderByGroup.substring(1),
	                                true, true);
        				
        			} else {
        				strTmp = getZx().getSql().orderByClause(
        							objEntity.getBo(),
        							strOrderByGroup,
        							false, true);
        				
        			}
        			
        			if (StringUtil.len(strTmp) > 0) {
        				if (strOrderBy.length() > 0) {
        					strOrderBy.append(", ");
        				}
        				strOrderBy.append(strTmp);
        			}
        		}
        		
        	}
        	
        	/**
        	 * And store it in the context
        	 */
        	pobjDocBuilder.setInContext(strQueryName + DocBuilder.QRYORDERBYCLAUSE,
        								strOrderBy.toString());
        	
        	return goAll;
        } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process goAll.", e);
	        
	        if (getZx().throwException) throw new ZXException(e);
	        goAll = zXType.rc.rcError;
	        return goAll;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(goAll);
                getZx().trace.exitMethod();
            }
        } 
	}
	
	/**
	 * Build query for type = queryDef.
	 * 
	 * @param pobjDocBuilder The doc builder
	 * @return Returns the return code of the method
	 * @throws ZXException Thrown if goAll fails.
	 */
	private zXType.rc goQueryDef(DocBuilder pobjDocBuilder) throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc goQueryDef = zXType.rc.rcOK;
        String strTmp;
        
        try {
        	
        	/**
        	 * Get entities
        	 */
        	Map colEntities = pobjDocBuilder.resolveEntities(this);
        	if (colEntities == null) {
        		throw new ZXException("Failed to get entities");
        	}
        	
        	/**
        	 * See if we do not break any rules
        	 */
        	if (pobjDocBuilder.validDataSourceEntities(colEntities)) {
        		throw new ZXException("Unsupported combination of data-source handlers");
        	}
        	
        	DBEntity objTheEntity = (DBEntity)colEntities.values().iterator().next();
        	// TODO : Bug in 1.5
        	DSHandler objDSHandler = objTheEntity.getDsHandler();
        	
        	/**
        	 * Not supported for channels
        	 */
        	if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
        		throw new ZXException("Query-def not supported for channel data-source",
        							  objDSHandler.getName());
        	}
        	
        	/**
        	 * Load all instances in case we refer to them in any director
        	 */
        	if (pobjDocBuilder.loadEntities(colEntities).pos != zXType.rc.rcOK.pos) {
        		throw new ZXException("Failed to load entities");
        	}
        	
        	/**
        	 * Init query definition
        	 */
        	QueryDef objQryDef = new QueryDef();
        	objQryDef.init(getZx().resolveDirector(this.querydef));
        	
        	String strSelect = objQryDef.qry(zXType.queryDefPageFlowScope.qdpfsSelect, null);
        	if (StringUtil.len(strSelect) == 0) {
        		throw new ZXException("Failed to load select query");
        	}
        	
        	/**
        	 * And store it in the context
        	 */
        	String strQueryName = getZx().resolveDirector(this.queryname);
        	pobjDocBuilder.setInContext(strQueryName + DocBuilder.QRYSELECTCLAUSE,
        								strSelect);
        	
        	/**
        	 * Add any conditions to whereclause based on pkWhereGroup
        	 */
        	DBEntity objEntity;
        	StringBuffer strWhere = new StringBuffer();
        	Iterator iter = colEntities.values().iterator();
        	while(iter.hasNext()) {
        		objEntity = (DBEntity)iter.next();
        		
        		String strWhereGroup = getZx().resolveDirector(objEntity.getPkwheregroup());
        		
        		if (StringUtil.len(strWhereGroup) > 0) {
        			pobjDocBuilder.handleAttrValues(objEntity);
                    
                    strTmp = getZx().getSql().processWhereGroup(objEntity.getBo(), 
                    										    strWhereGroup);
                    
                    if (StringUtil.len(strWhere) > 0) {
                    	strWhere.append(" AND ");
                    }
        			strWhere.append(strTmp);
        		}
        		
        	}
        	
        	/**
        	 * And query expression stuff
        	 */
        	strTmp = processQueryExpr(this.queryexpr, colEntities);
        	if (StringUtil.len(strTmp) > 0) {
        		if (strWhere.length() > 0) {
        			strWhere.append(" AND ( ").append(strTmp).append(" ) ");
        		} else {
        			strWhere.append(strTmp);
        		}
        		
        	}
        	
        	/**
        	 * And store it in the context
        	 */
        	pobjDocBuilder.setInContext(strQueryName + DocBuilder.QRYWHERECLAUSE,
        								strWhere.toString());
        	
        	/**
        	 * And the order by stuff
        	 */
        	StringBuffer strOrderBy = new StringBuffer();
        	iter = colEntities.values().iterator();
        	while (iter.hasNext()) {
        		objEntity = (DBEntity)iter.next();
        		
        		String strOrderByGroup = getZx().resolveDirector(objEntity.getOrderbygroup());
        		if (StringUtil.len(strOrderByGroup) > 0) {
        			if (strOrderByGroup.charAt(0) == '-') {
        				strTmp = getZx().getSql().orderByClause(
        							objEntity.getBo(),
        							strOrderByGroup.substring(1),
	                                true, true);
        				
        			} else {
        				strTmp = getZx().getSql().orderByClause(
        							objEntity.getBo(),
        							strOrderByGroup,
        							false, true);
        				
        			}
        			
        			if (StringUtil.len(strTmp) > 0) {
        				if (strOrderBy.length() > 0) {
        					strOrderBy.append(", ");
        				}
        				strOrderBy.append(strTmp);
        			}
        		}
        		
        	}
        	
        	/**
        	 * And store it in the context
        	 */
        	pobjDocBuilder.setInContext(strQueryName + DocBuilder.QRYORDERBYCLAUSE,
        								strOrderBy.toString());
        	
        	return goQueryDef;
        } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process goQueryDef.", e);
	        
	        if (getZx().throwException) throw new ZXException(e);
	        goQueryDef = zXType.rc.rcError;
	        return goQueryDef;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(goQueryDef);
                getZx().trace.exitMethod();
            }
        } 
	}
	
    /**
     * Handle QueryDef query type.
     *
     * @param pobjQryExpr The Query Definition to use. 
     * @param pcolEntities A collection of linked form entities. 
     * @return Returns the return code of the method.
     * @throws ZXException  Thrown if processQueryExpr fails. 
     */
    public String processQueryExpr(DBQueryExpr pobjQryExpr, Map pcolEntities) throws ZXException{
        String processQueryExpr = ""; 
        
        if (pobjQryExpr == null) {
            return processQueryExpr;
        }
        
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjQryExpr", pobjQryExpr);
            getZx().trace.traceParam("pcolEntities", pcolEntities);
        }
        
        try {
            Iterator iter;
            DBEntity objEntity;
            Attribute objAttr;
            String strAttr;
            String strLHS;
            String strRHS;
            
            objEntity = (DBEntity)pcolEntities.values().iterator().next();
            DSHandler objDSHandler = objEntity.getDsHandler();
            boolean blnIsDSChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            zXType.databaseType enmDBType = zXType.databaseType.dbAny;
            if (blnIsDSChannel) {
                enmDBType = ((DSHRdbms)objDSHandler).getDbType();
            }
            
            /**
             * Actually should never be 'nothing' because is preallocated, but just in case someone
             * changes that leave the above in for safety. The real test is here: if not recursive
             * and both sides are blank, there is no query expression
             */
            if (!pobjQryExpr.isOperatorRecursive() 
                && StringUtil.len(pobjQryExpr.getRhs()) == 0 && StringUtil.len(pobjQryExpr.getLhs()) == 0) {
                return processQueryExpr;
            }
            
            if (pobjQryExpr.isOperatorRecursive()) {
                /**
                 * Recursive - call to get next level down LHS and RHS
                 */
                String strTmp = processQueryExpr(pobjQryExpr.getQryexprlhs(), pcolEntities);
                if (StringUtil.len(strTmp) > 0) {
                    strLHS = " ( " + strTmp + " )";
                } else {
                	strLHS = "";
                }
                
                strTmp = processQueryExpr(pobjQryExpr.getQryexprrhs(), pcolEntities);
                if (StringUtil.len(strTmp) > 0) {
                    strRHS = " ( " + strTmp + " )";
                } else {
                	strRHS = "";
                }
                
            } else {
                /**
                 * Not recursive
                 */
            	
                /**
                 * Datesformats to use for testing
                 */
                DateFormat arrDateFormats[] = {getZx().getTimestampFormat(),
                							   getZx().getDateFormat(),
                							   getZx().getTimeFormat()};
                
                /**
                 * LHS : Resolve the left handside expression :
                 */
                strLHS = getZx().resolveDirector(pobjQryExpr.getLhs());
                strAttr = getZx().resolveDirector(pobjQryExpr.getAttrlhs());
                
                if (StringUtil.len(strAttr) == 0) {
                    /** 
                     * No attribute - must be a simple director. Convert to best guess datatype 
                     **/
                    if (blnIsDSChannel) {
                        if (StringUtil.isNumeric(strLHS) || StringUtil.isDouble(strLHS)) {
                            /**
                             * No need to modify lhs value for numeric values.
                             */
                        } else if (DateUtil.isValid(arrDateFormats, strLHS)) {
                            strLHS = "#" + strLHS + "#";
                        } else {
                             strLHS = "'" + StringUtil.encodezXString(strLHS) + "'";
                        }
                        
                    } else {
                        if (StringUtil.isNumeric(strLHS)) {
                            strLHS = getZx().getSql().dbStrValue(zXType.dataType.dtLong.pos, strLHS, enmDBType);
                        } else if (StringUtil.isDouble(strLHS)) {
                            strLHS = getZx().getSql().dbStrValue(zXType.dataType.dtDouble.pos, strLHS , enmDBType);
                        } else if (DateUtil.isValid(arrDateFormats, strLHS)) {
                            java.util.Date objDate = DateUtil.parse(arrDateFormats, strLHS);
                            strLHS = getZx().getSql().dbStrValue(zXType.dataType.dtDate.pos, objDate, enmDBType);
                        } else {
                            strLHS = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, strLHS, enmDBType);
                        }
                        
                    }
                    
                } else {
                    /**
                     * Have an attribute - is a database column name.
                     */
                    iter = pcolEntities.values().iterator();
                    while (iter.hasNext()) {
                        objEntity = (DBEntity)iter.next();
                        
                        if (objEntity.getName().equals(strLHS)) {
                            if (blnIsDSChannel){
                                strLHS = strAttr;
                                
                            } else {
                                objAttr = objEntity.getBo().getDescriptor().getAttribute(strAttr);
                                strLHS = getZx().getSql().columnName(objEntity.getBo(), 
                                                                     objAttr, 
                                                                     zXType.sqlObjectName.sonName);
                            } // Channel or RDBMS
                            break;
                            
                        }
                        
                    } // Loop over entities
                    
                } // Attribute provided?
                
                /**
                 * RHS :  Resolve the right handside expression :
                 */
                strRHS = getZx().resolveDirector(pobjQryExpr.getRhs());
                strAttr = getZx().resolveDirector(pobjQryExpr.getAttrrhs());
                
                if (StringUtil.len(strAttr) == 0) {
                    /** 
                     * No attribute - must be a simple director. Convert to best guess datatype 
                     */
                    if (blnIsDSChannel) {
                        /**
                         * DGS25FEB2004: If a null-type operator, the right hand operand is not used
                         */
                        if (pobjQryExpr.getOpeRator().equals(zXType.pageflowQueryExprOperator.pqeoNULL)
                            || pobjQryExpr.getOpeRator().equals(zXType.pageflowQueryExprOperator.pqeoNOTNULL)) {
                            strRHS = "";
                            
                        } else if (StringUtil.isNumeric(strRHS) || StringUtil.isDouble(strRHS)) {
                            /**
                             * No need to modify lhs value for numeric values.
                             */
                        	
                        } else if (DateUtil.isValid(arrDateFormats, strRHS)) {
                            strRHS = "#" + strRHS + "#";
                            
                        } else {
                            strRHS = "'" + StringUtil.encodezXString(strRHS) + "'";
                            
                        }
                        
                    } else {
                        /**
                         * DGS25FEB2004: If a null-type operator, the right hand operand is not used.
                         */
                        if (pobjQryExpr.getOpeRator().equals(zXType.pageflowQueryExprOperator.pqeoNULL) 
                           || pobjQryExpr.getOpeRator().equals(zXType.pageflowQueryExprOperator.pqeoNOTNULL)) {
                            strRHS = "";
                            
                        } else if (StringUtil.isNumeric(strRHS)) {
                            strRHS = getZx().getSql().dbStrValue(zXType.dataType.dtLong.pos, strRHS, enmDBType);
                            
                        }else if (StringUtil.isDouble(strRHS)) {
                            strRHS = getZx().getSql().dbStrValue(zXType.dataType.dtDouble.pos, strRHS, enmDBType);
                            
                        } else if (DateUtil.isValid(arrDateFormats, strRHS)) {
                        	java.util.Date objDate = DateUtil.parse(arrDateFormats, strRHS);
                            strRHS = getZx().getSql().dbStrValue(zXType.dataType.dtDate.pos, objDate, enmDBType);
                            
                        } else {
                            strRHS = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, strRHS, enmDBType);
                            
                        }
                        
                    } // Channel or RDBMS
                    
                } else {
                    /**
                     * Have an attribute - is a database column name.
                     */
                    iter = pcolEntities.values().iterator();
                    while (iter.hasNext()) {
                        objEntity = (DBEntity)iter.next();
                        if (objEntity.getName().equals(strRHS)) {
                            if (blnIsDSChannel) {
                                strRHS = strAttr;
                                
                            } else {
                                objAttr = objEntity.getBo().getDescriptor().getAttribute(strAttr);
                                strRHS = getZx().getSql().columnName(objEntity.getBo(), 
                                                                     objAttr, 
                                                                     zXType.sqlObjectName.sonName);
                                
                            } // Channel or RDBMS
                            break;
                            
                        }
                        
                    } // Loop over entities
                    
                } // Attribute provided?
                
            } // Recursive ?
            
            /**
             * If the result of all this is nothing on either side, don't put an operator
             * in the middle - just exit with nothing.
             */
            if (StringUtil.len(strLHS) == 0 && StringUtil.len(strRHS) == 0) {
                return processQueryExpr;
            }
            
            if (blnIsDSChannel) {
                int intOperator = pobjQryExpr.getOpeRator().pos;
                if (intOperator ==  zXType.pageflowQueryExprOperator.pqeoEQ.pos) {
                    processQueryExpr = strLHS + "=" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoGE.pos) {
                    processQueryExpr = strLHS + ">=" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoGT.pos) {
                    processQueryExpr = strLHS + ">" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoLE.pos) {
                    processQueryExpr = strLHS + "<=" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoLT.pos) {
                    processQueryExpr = strLHS + "<" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoNE.pos) {
                    processQueryExpr = strLHS + "<>" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoSW.pos) {
                    processQueryExpr = strLHS + "%" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoCNT.pos) {
                    processQueryExpr = strLHS + "%%" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoNOTNULL.pos) {
                    processQueryExpr = strLHS + "<>#null";
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoNULL.pos) {
                    processQueryExpr = strLHS + "=#null";
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoAND.pos) {
                    processQueryExpr = strLHS + "&" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoOR.pos) {
                    processQueryExpr = strLHS + "|" + strRHS;
                    
                }
                
            } else {
                String strOperator = zXType.pageflowQueryExprOperator.getStringValue(pobjQryExpr.getOpeRator());
                processQueryExpr = strLHS + " " + strOperator + " " + strRHS;
                
            } // RDBMS or channel?
            
            return processQueryExpr;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Handle QueryDef query type.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pobjQryExpr = "+ pobjQryExpr);
                log.error("Parameter : pcolEntities = "+ pcolEntities);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return processQueryExpr;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processQueryExpr);
                getZx().trace.exitMethod();
            }
        }
    }
	
	//------------------------ Object implemeted methods
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		DBQuery objDBAction = null;
		
		try {
			objDBAction = (DBQuery)super.clone();
			
			objDBAction.setQueryType(getQueryType());
			objDBAction.setQuerydef(getQuerydef());
			objDBAction.setEntityl(getEntityl());
			objDBAction.setEntitym(getEntitym());
			objDBAction.setEntityr(getEntityr());
			objDBAction.setDisTinct(isDisTinct());
			objDBAction.setOuterJoin(isOuterJoin());
			objDBAction.setResolveFK(isResolveFK());
			
			if (getQueryexpr() != null) {
				objDBAction.setQueryexpr((DBQueryExpr)getQueryexpr().clone());
			}
			
			objDBAction.setQueryname(getQueryname());
			
        } catch (Exception e) {
            log.error("Failed to clone object", e);
        }
        
        return objDBAction;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		toString.appendSuper(super.toString());
		
		toString.append(DBQUERY_DISTINCT, isDisTinct());
		toString.append(DBQUERY_ENTITYL, getEntityl());
		toString.append(DBQUERY_ENTITYM, getEntitym());
		toString.append(DBQUERY_ENTITYR, getEntityr());
		toString.append(DBQUERY_OUTERJOIN, isOuterJoin());
		toString.append(DBQUERY_QUERYEXPR, getQueryexpr());
		toString.append(DBQUERY_QUERYNAME, getQueryname());
		toString.append(DBQUERY_QUERYTYPE, zXType.valueOf(getQueryType()));
		toString.append(DBQUERY_RESOLVEFK, isResolveFK());
		
		return toString.toString();
	}
}