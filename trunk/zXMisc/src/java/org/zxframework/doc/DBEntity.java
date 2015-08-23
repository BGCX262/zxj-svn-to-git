/*
 * Created on Jun 9, 2005
 * $Id: DBEntity.java,v 1.1.2.12 2006/07/17 16:10:54 mike Exp $
 */
package org.zxframework.doc;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zxframework.Tuple;
import org.zxframework.ZXBO;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHandler;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.DomElementUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * Doc Builder Entity.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 17 May 2003
 * 
 * Change    : DGS07MAY2004
 * Why       : New ref method of 'context'
 *
 * Change    : BD5APR05 - V1.5:1
 * Why       : Added support for data-sources
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBEntity extends ZXObject {
	
	private static Log log = LogFactory.getLog(DBEntity.class);
	
	//------------------------ Members
	
	private String name;
	private String comment;
	private String alias;
	private String pk;
	private String loadgroup;
	private String usegroup;
	private String orderbygroup;
	private String pkwheregroup;
	private List attrvalues;
	private String entity;
	private zXType.docBuilderEntityRefMethod refMethod;
	
	//------------------------ Runtime members
	
	private ZXBO bo; 
	private String resolvedLoadGroup;
	private String resolvedUseGroup;
	private DSHandler dsHandler;
	
	//------------------------ XML Element constants
	
	private static final String DBENTITY_STARTTAG = "entity";
	private static final String DBENTITY_NAME = "name";
	private static final String DBENTITY_ENTITY = "entity";
	private static final String DBENTITY_COMMENT = "comment";
	private static final String DBENTITY_ALIAS = "alias";
	private static final String DBENTITY_PK = "pk";
	private static final String DBENTITY_LOADGROUP = "loadgroup";
	private static final String DBENTITY_USEGROUP = "usegroup";
	private static final String DBENTITY_ORDERBYGROUP = "orderbygroup";
	private static final String DBENTITY_PKWHEREGROUP = "pkwheregroup";
	private static final String DBENTITY_REFMETHOD = "refmethod";
	private static final String DBENTITY_ATTRVALUES = "attrvalues";
	
	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the alias.
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * @param alias The alias to set.
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	/**
	 * A collection (ArrayList)(Tuple) of attribute value.
	 * 
	 * @return Returns the attrvalues.
	 */
	public List getAttrvalues() {
		return attrvalues;
	}
	
	/**
	 * @param attrvalues The attrvalues to set.
	 */
	public void setAttrvalues(List attrvalues) {
		this.attrvalues = attrvalues;
	}
	
	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/**
	 * @return Returns the entity.
	 */
	public String getEntity() {
		return entity;
	}
	
	/**
	 * @param entity The entity to set.
	 */
	public void setEntity(String entity) {
		this.entity = entity;
	}
	
	/**
	 * @return Returns the loadgroup.
	 */
	public String getLoadgroup() {
		return loadgroup;
	}
	
	/**
	 * @param loadgroup The loadgroup to set.
	 */
	public void setLoadgroup(String loadgroup) {
		this.loadgroup = loadgroup;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Returns the orderbygroup.
	 */
	public String getOrderbygroup() {
		return orderbygroup;
	}
	
	/**
	 * @param orderbygroup The orderbygroup to set.
	 */
	public void setOrderbygroup(String orderbygroup) {
		this.orderbygroup = orderbygroup;
	}
	
	/**
	 * @return Returns the pk.
	 */
	public String getPk() {
		return pk;
	}
	
	/**
	 * @param pk The pk to set.
	 */
	public void setPk(String pk) {
		this.pk = pk;
	}
	
	/**
	 * @return Returns the pkwheregroup.
	 */
	public String getPkwheregroup() {
		return pkwheregroup;
	}
	
	/**
	 * @param pkwheregroup The pkwheregroup to set.
	 */
	public void setPkwheregroup(String pkwheregroup) {
		this.pkwheregroup = pkwheregroup;
	}
	
	/**
	 * @return Returns the refMethod.
	 */
	public zXType.docBuilderEntityRefMethod getRefMethod() {
		return refMethod;
	}
	
	/**
	 * @param refMethod The refMethod to set.
	 */
	public void setRefMethod(zXType.docBuilderEntityRefMethod refMethod) {
		this.refMethod = refMethod;
	}
	
	/**
	 * @return Returns the usegroup.
	 */
	public String getUsegroup() {
		return usegroup;
	}
	
	/**
	 * @param usegroup The usegroup to set.
	 */
	public void setUsegroup(String usegroup) {
		this.usegroup = usegroup;
	}
	
	//------------------------ Digester helper methods.
	
	/**
	 * @param refMethod The refMethod to set.
	 */
	public void setRefmethod(String refMethod) {
		this.refMethod = zXType.docBuilderEntityRefMethod.getEnum(refMethod);
	}
	
	//------------------------ Runtime getters/setters
	
	/**
	 * @return Returns the bo.
	 */
	public ZXBO getBo() {
		return bo;
	}
	
	/**
	 * @param bo The bo to set.
	 */
	public void setBo(ZXBO bo) {
		this.bo = bo;
	}
	
	/**
	 * @return Returns the resolvedLoadGroup.
	 */
	public String getResolvedLoadGroup() {
		return resolvedLoadGroup;
	}
	
	/**
	 * @param resolvedLoadGroup The resolvedLoadGroup to set.
	 */
	public void setResolvedLoadGroup(String resolvedLoadGroup) {
		this.resolvedLoadGroup = resolvedLoadGroup;
	}
	
	/**
	 * @return Returns the resolvedUseGroup.
	 */
	public String getResolvedUseGroup() {
		return resolvedUseGroup;
	}
	
	/**
	 * @param resolvedUseGroup The resolvedUseGroup to set.
	 */
	public void setResolvedUseGroup(String resolvedUseGroup) {
		this.resolvedUseGroup = resolvedUseGroup;
	}

	/**
	 * @return Returns the dsHandler.
	 */
	public DSHandler getDsHandler() {
		return dsHandler;
	}

	/**
	 * @param dsHandler The dsHandler to set.
	 */
	public void setDsHandler(DSHandler dsHandler) {
		this.dsHandler = dsHandler;
	}
	
	//------------------------ Repository methods.
	
	/**
	 * Dump XML.
	 * 
	 * @param pobjXMLGen XML generator.
	 */
	public void dumpAsXML(XMLGen pobjXMLGen) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        try {
        	
        	pobjXMLGen.openTag(DBENTITY_STARTTAG);
        	
        	pobjXMLGen.taggedValue(DBENTITY_NAME, getName());
        	pobjXMLGen.taggedCData(DBENTITY_COMMENT, getComment(), false);
        	pobjXMLGen.taggedCData(DBENTITY_ENTITY, getEntity(), false);
        	pobjXMLGen.taggedCData(DBENTITY_PK, getPk(), false);
        	pobjXMLGen.taggedCData(DBENTITY_ALIAS, getAlias(), false);
        	pobjXMLGen.taggedCData(DBENTITY_LOADGROUP, getLoadgroup(), false);
        	pobjXMLGen.taggedCData(DBENTITY_USEGROUP, getUsegroup(), false);
        	pobjXMLGen.taggedCData(DBENTITY_ORDERBYGROUP, getOrderbygroup(), false);
        	pobjXMLGen.taggedCData(DBENTITY_PKWHEREGROUP, getPkwheregroup(), false);
        	pobjXMLGen.taggedCData(DBENTITY_REFMETHOD, zXType.valueOf(getRefMethod()), false);
        	
        	if (getAttrvalues() != null) {
        		pobjXMLGen.openTag(DBENTITY_ATTRVALUES);
        		
        		Tuple objTuple;
        		for (int i = 0; i < getAttrvalues().size(); i++) {
        			objTuple = (Tuple)getAttrvalues().get(i);
        			
        			pobjXMLGen.openTag("attrvalue");
        			pobjXMLGen.taggedCData(DBAction.DBACTION_TUPLE_NAME, objTuple.getName());
        			pobjXMLGen.taggedCData(DBAction.DBACTION_TUPLE_VALUE, objTuple.getValue());
        			pobjXMLGen.closeTag("attrvalue");
				}
        		
        		pobjXMLGen.closeTag(DBENTITY_ATTRVALUES);
        	}
        	
        	pobjXMLGen.closeTag(DBENTITY_STARTTAG);
        	
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Dump xml", e);
            throw new NestableRuntimeException("Failed to : Dump xml", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
	
	//------------------------ Parsing code
	
	/**
	 * Parse DB Entity.
	 * 
	 * @param pobjXMLNode XML Node with the DBEntity definition
	 * @return Returns the return code for the method.
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
					
					if (DBENTITY_NAME.equalsIgnoreCase(nodeName)) {
						setName(element.getText());
						
					} else if (DBENTITY_ENTITY.equalsIgnoreCase(nodeName)) {
						setEntity(element.getText());
						
					} else if (DBENTITY_COMMENT.equalsIgnoreCase(nodeName)) {
						setComment(element.getText());
						
					} else if (DBENTITY_ALIAS.equalsIgnoreCase(nodeName)) {
						setAlias(element.getText());
						
					} else if (DBENTITY_PK.equalsIgnoreCase(nodeName)) {
						setPk(element.getText());
						
					} else if (DBENTITY_LOADGROUP.equalsIgnoreCase(nodeName)) {
						setLoadgroup(element.getText());
						
					} else if (DBENTITY_USEGROUP.equalsIgnoreCase(nodeName)) {
						setUsegroup(element.getText());
						
					} else if (DBENTITY_ORDERBYGROUP.equalsIgnoreCase(nodeName)) {
						setOrderbygroup(element.getText());
						
					} else if (DBENTITY_PKWHEREGROUP.equalsIgnoreCase(nodeName)) {
						setPkwheregroup(element.getText());
						
					} else if (DBENTITY_REFMETHOD.equalsIgnoreCase(nodeName)) {
						setRefmethod(element.getText());
						
					} else if (DBENTITY_ATTRVALUES.equalsIgnoreCase(nodeName)) {
						setAttrvalues(DBAction.parseTuples((Element)node));
						
					}
				}
			}
        	
        	return parse;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Parse DBEntity", e);
            throw new NestableRuntimeException("Failed to : Parse DBEntity", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(parse);
                getZx().trace.exitMethod();
            }
        }    
    }
    
	//------------------------ Object implemented methods
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		DBEntity objDBEntity = null;
		
		try {
			objDBEntity = new DBEntity();
			
			objDBEntity.setName(getName());
			objDBEntity.setComment(getComment());
			objDBEntity.setAlias(getAlias());
			objDBEntity.setPk(getPk());
			objDBEntity.setLoadgroup(getLoadgroup());
			objDBEntity.setUsegroup(getUsegroup());
			objDBEntity.setOrderbygroup(getOrderbygroup());
			objDBEntity.setPkwheregroup(getPkwheregroup());
			
			if (getAttrvalues() != null && getAttrvalues().size() > 0) {
				objDBEntity.setAttrvalues(CloneUtil.clone((ArrayList)getAttrvalues()));
			}
			
			objDBEntity.setEntity(getEntity());
			objDBEntity.setRefMethod(getRefMethod());
			
        } catch (Exception e) {
            log.error("Failed to clone object", e);
        }
        
        return objDBEntity;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
		toString.append(DBENTITY_ALIAS, getAlias());
		toString.append(DBENTITY_ATTRVALUES, getAttrvalues());
		toString.append(DBENTITY_COMMENT, getComment());
		toString.append(DBENTITY_ENTITY, getEntity());
		toString.append(DBENTITY_LOADGROUP, getLoadgroup());
		toString.append(DBENTITY_NAME, getName());
		toString.append(DBENTITY_ORDERBYGROUP, getOrderbygroup());
		toString.append(DBENTITY_PK, getPk());
		toString.append(DBENTITY_PKWHEREGROUP, getPkwheregroup());
		toString.append(DBENTITY_REFMETHOD, zXType.valueOf(getRefMethod()));
		toString.append(DBENTITY_USEGROUP, getUsegroup());
		
		return toString.toString();
	}
}