/*
 * Created on Jun 9, 2005
 * $Id: DBObject.java,v 1.1.2.8 2005/09/02 08:15:37 mike Exp $
 */
package org.zxframework.doc;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.DomElementUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * The docbuilder object class.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 17 May 2003
 * </pre>
 *  
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBObject extends ZXObject {
	
	private static Log log = LogFactory.getLog(DBObject.class);
	
	//------------------------ Members
	
	private zXType.docBuilderObjectrefMethod refMethod;
	private String ref;
	private String section;
	private zXType.docBuilderPagePart pagePart;
	private boolean reCalc;
	private boolean saveAsLastused;
	private String saveas;
	private String comment;
	private String row;
	private String column;
	
	//------------------------ XML Element constants
	
	private static final String DBOBJECT_REFMETHOD = "refmethod";
	private static final String DBOBJECT_REF = "ref";
	private static final String DBOBJECT_ROW = "row";
	private static final String DBOBJECT_COLUMN = "column";
	private static final String DBOBJECT_COMMENT = "comment";
	private static final String DBOBJECT_SECTION = "section";
	private static final String DBOBJECT_PAGEPART = "pagepart";
	private static final String DBOBJECT_SAVEAS = "saveas";
	private static final String DBOBJECT_RECALC = "recalc";
	private static final String DBOBJECT_SAVEASLASTUSED = "saveaslastused";
	
	//------------------------ Runtime members
	
	private Object wordobject;
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public DBObject() {
		super();
	}
	
	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the column.
	 */
	public String getColumn() {
		return column;
	}
	
	/**
	 * @param column The column to set.
	 */
	public void setColumn(String column) {
		this.column = column;
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
	 * @return Returns the pagePart.
	 */
	public zXType.docBuilderPagePart getPagePart() {
		return pagePart;
	}
	
	/**
	 * @param pagePart The pagePart to set.
	 */
	public void setPagePart(zXType.docBuilderPagePart pagePart) {
		this.pagePart = pagePart;
	}
	
	/**
	 * @return Returns the recalc.
	 */
	public boolean isReCalc() {
		return reCalc;
	}
	
	/**
	 * @param recalc The recalc to set.
	 */
	public void setReCalc(boolean recalc) {
		this.reCalc = recalc;
	}
	
	/**
	 * @param recalc The recalc to set.
	 */
	public void setRecalc(String recalc) {
		this.reCalc = StringUtil.booleanValue(recalc);
	}
	
	/**
	 * @return Returns the ref.
	 */
	public String getRef() {
		return ref;
	}
	
	/**
	 * @param ref The ref to set.
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}
	
	/**
	 * @return Returns the refMethod.
	 */
	public zXType.docBuilderObjectrefMethod getRefMethod() {
		return refMethod;
	}
	
	/**
	 * @param refMethod The refMethod to set.
	 */
	public void setRefMethod(zXType.docBuilderObjectrefMethod refMethod) {
		this.refMethod = refMethod;
	}
	
	/**
	 * @return Returns the row.
	 */
	public String getRow() {
		return row;
	}
	
	/**
	 * @param row The row to set.
	 */
	public void setRow(String row) {
		this.row = row;
	}
	
	/**
	 * @return Returns the saveas.
	 */
	public String getSaveas() {
		return saveas;
	}
	
	/**
	 * @param saveas The saveas to set.
	 */
	public void setSaveas(String saveas) {
		this.saveas = saveas;
	}
	
	/**
	 * @return Returns the saveaslastused.
	 */
	public boolean isSaveAsLastused() {
		return saveAsLastused;
	}
	
	/**
	 * @param saveaslastused The saveaslastused to set.
	 */
	public void setSaveAsLastused(boolean saveaslastused) {
		this.saveAsLastused = saveaslastused;
	}
	
	/**
	 * @param saveaslastused The saveaslastused to set.
	 */
	public void setSaveaslastused(String saveaslastused) {
		this.saveAsLastused = StringUtil.booleanValue(saveaslastused);
	}
	
	/**
	 * @return Returns the section.
	 */
	public String getSection() {
		return section;
	}
	
	/**
	 * @param section The section to set.
	 */
	public void setSection(String section) {
		this.section = section;
	}
	
	/**
	 * @return Returns the wordobject.
	 */
	public Object getWordobject() {
		return wordobject;
	}
	
	/**
	 * @param wordobject The wordobject to set.
	 */
	public void setWordobject(Object wordobject) {
		this.wordobject = wordobject;
	}
	
	//------------------------ Digester helper methods
	
	/**
	 * @param refMethod The refMethod to set.
	 */
	public void setRefmethod(String refMethod) {
		this.refMethod = zXType.docBuilderObjectrefMethod.getEnum(refMethod);
	}
	
	/**
	 * @param pagePart The pagePart to set.
	 */
	public void setPagepart(String pagePart) {
		this.pagePart = zXType.docBuilderPagePart.getEnum(pagePart);
	}
	
	//------------------------ Parsing methods
	
	/**
	 * Parse DBObject XML.
	 * 
	 * @param pobjXMLNode The xml node containing the definition.
	 * @return Returns the return code of the method.
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
					nodeName = node.getNodeName().toLowerCase();
					
					if (DBOBJECT_REFMETHOD.equals(nodeName)) {
						setRefmethod(element.getText());
						
					} else if (DBOBJECT_REF.equals(nodeName)) {
						setRef(element.getText());
						
					} else if (DBOBJECT_ROW.equals(nodeName)) {
						setRow(element.getText());
						
					} else if (DBOBJECT_COLUMN.equals(nodeName)) {
						setColumn(element.getText());
						
					} else if (DBOBJECT_COMMENT.equals(nodeName)) {
						setComment(element.getText());
						
					} else if (DBOBJECT_SECTION.equals(nodeName)) {
						setSection(element.getText());
						
					} else if (DBOBJECT_PAGEPART.equals(nodeName)) {
						setPagepart(element.getText());
						
					} else if (DBOBJECT_SAVEAS.equals(nodeName)) {
						setSaveas(element.getText());
						
					} else if (DBOBJECT_RECALC.equals(nodeName)) {
						setRecalc(element.getText());
						
					} else if (DBOBJECT_SAVEASLASTUSED.equals(nodeName)) {
						setSaveaslastused(element.getText());
						
					}
				}
			}
        	
        	return parse;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Parse DBObject", e);
            throw new NestableRuntimeException("Failed to : Parse DBObject", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(parse);
                getZx().trace.exitMethod();
            }
        }    
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
        	
        	pobjXMLGen.taggedCData(DBOBJECT_COMMENT, getComment(), false);
        	pobjXMLGen.taggedValue(DBOBJECT_REFMETHOD, zXType.valueOf(getRefMethod()));
        	pobjXMLGen.taggedCData(DBOBJECT_REF, getRef(), false);
        	pobjXMLGen.taggedCData(DBOBJECT_ROW, getRow(), false);
        	pobjXMLGen.taggedCData(DBOBJECT_COLUMN, getColumn(), false);
        	pobjXMLGen.taggedCData(DBOBJECT_SECTION, getSection(), false);
        	pobjXMLGen.taggedValue(DBOBJECT_PAGEPART, zXType.valueOf(getPagePart()));
        	pobjXMLGen.taggedCData(DBOBJECT_SAVEAS, getSaveas());
        	pobjXMLGen.taggedValue(DBOBJECT_RECALC, isReCalc());
        	pobjXMLGen.taggedValue(DBOBJECT_SAVEASLASTUSED, isSaveAsLastused());
        	
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Dump xml", e);
            throw new NestableRuntimeException("Failed to : Dump xml", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
	
	//------------------------ Object implemeted methods
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		DBObject objDBObject = null;
		
		try {
			objDBObject = new DBObject();
			
			objDBObject.setRefMethod(getRefMethod());
			objDBObject.setRef(getRef());
			objDBObject.setSection(getSection());
			objDBObject.setPagePart(getPagePart());
			objDBObject.setReCalc(isReCalc());
			objDBObject.setSaveAsLastused(isSaveAsLastused());
			objDBObject.setSaveas(getSaveas());
			objDBObject.setComment(getComment());
			objDBObject.setRow(getRow());
			objDBObject.setColumn(getColumn());
			
        } catch (Exception e) {
            log.error("Failed to clone object", e);
        }
        return objDBObject;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
		toString.append(DBOBJECT_COLUMN, getColumn());
		toString.append(DBOBJECT_COMMENT, getComment());
		toString.append(DBOBJECT_PAGEPART, zXType.valueOf(getPagePart()));
		toString.append(DBOBJECT_RECALC, isReCalc());
		toString.append(DBOBJECT_REF, getRef());
		toString.append(DBOBJECT_REFMETHOD, zXType.valueOf(getRefMethod()));
		toString.append(DBOBJECT_ROW, getRow());
		toString.append(DBOBJECT_SAVEAS, getSaveas());
		toString.append(DBOBJECT_SAVEASLASTUSED, isSaveAsLastused());
		toString.append(DBOBJECT_SECTION, getSection());
		
		return toString.toString();
	}
}