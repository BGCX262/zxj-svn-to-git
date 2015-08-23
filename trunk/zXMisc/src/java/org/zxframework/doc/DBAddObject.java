/*
 * Created on Jun 9, 2005
 * $Id: DBAddObject.java,v 1.1.2.12 2005/09/02 08:07:44 mike Exp $
 */
package org.zxframework.doc;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.DomElementUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * The implementation of the add-object doc builder action.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 18 May 2003
 * 
 * Change    : BD27OCT04
 * Why       : Fixed problem with addObject when used to insert a table rather than
 *             append to the end
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBAddObject extends DBAction {
	
	private static Log log = LogFactory.getLog(DBAddObject.class);
	
	//------------------------ Members
	
	private DBObject refobject;
	private DBObject copyobject;
	private zXType.docBuilderDisposition disPosition;
	private zXType.docBuilderPageBreak pageBreak;
	
	//------------------------ XML Element Constants
	
	private static final String DBADDOBJECT_REFOBJECT = "refobject";
	private static final String DBADDOBJECT_COPYOBJECT = "copyobject";
	private static final String DBADDOBJECT_DISPOSITION = "disposition";
	private static final String DBADDOBJECT_PAGEBREAK = "pagebreak";
	
	//------------------------ Default constructor.
	
	/**
	 * Default constructor.
	 */
	public DBAddObject() {
		super();
	}
	
	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the copyobject.
	 */
	public DBObject getCopyobject() {
		return copyobject;
	}
	
	/**
	 * @param copyobject The copyobject to set.
	 */
	public void setCopyobject(DBObject copyobject) {
		this.copyobject = copyobject;
	}
	
	/**
	 * @return Returns the disPosition.
	 */
	public zXType.docBuilderDisposition getDisPosition() {
		return disPosition;
	}
	
	/**
	 * @param disPosition The disPosition to set.
	 */
	public void setDisPosition(zXType.docBuilderDisposition disPosition) {
		this.disPosition = disPosition;
	}

	/**
	 * @return Returns the pageBreak.
	 */
	public zXType.docBuilderPageBreak getPageBreak() {
		return pageBreak;
	}
	
	/**
	 * @param pageBreak The pageBreak to set.
	 */
	public void setPageBreak(zXType.docBuilderPageBreak pageBreak) {
		this.pageBreak = pageBreak;
	}
	
	/**
	 * @return Returns the refobject.
	 */
	public DBObject getRefobject() {
		return refobject;
	}
	
	/**
	 * @param refobject The refobject to set.
	 */
	public void setRefobject(DBObject refobject) {
		this.refobject = refobject;
	}
	
	//------------------------ Digester util methods
	
	/**
	 * @param disPosition The disPosition to set.
	 */
	public void setDisposition(String disPosition) {
		this.disPosition = zXType.docBuilderDisposition.getEnum(disPosition);
	}
	
	/**
	 * @param pageBreak The pageBreak to set.
	 */
	public void setPagebreak(String pageBreak) {
		this.pageBreak = zXType.docBuilderPageBreak.getEnum(pageBreak);
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
            
            if (getRefobject() != null) {
                objXMLGen.openTag(DBADDOBJECT_REFOBJECT);
                getRefobject().dumpAsXML(objXMLGen);
                objXMLGen.closeTag(DBADDOBJECT_REFOBJECT);
            }
            
            if (getCopyobject() != null) {
            	objXMLGen.openTag(DBADDOBJECT_COPYOBJECT);
                getCopyobject().dumpAsXML(objXMLGen);
                objXMLGen.closeTag(DBADDOBJECT_COPYOBJECT);
            }
            
            objXMLGen.taggedValue(DBADDOBJECT_DISPOSITION, zXType.valueOf(getDisPosition()));
            objXMLGen.taggedValue(DBADDOBJECT_PAGEBREAK, zXType.valueOf(getPageBreak()));
            
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
					
					if (DBADDOBJECT_REFOBJECT.equalsIgnoreCase(nodeName)) {
						this.refobject = new DBObject();
						this.refobject.parse((Element)node);
						
					} else if (DBADDOBJECT_COPYOBJECT.equalsIgnoreCase(nodeName)) {
						this.copyobject = new DBObject();
						this.copyobject.parse((Element)node);
						
					} else if (DBADDOBJECT_DISPOSITION.equalsIgnoreCase(nodeName)) {
						setDisposition(element.getText());
						
					} else if (DBADDOBJECT_PAGEBREAK.equalsIgnoreCase(nodeName)) {
						setPagebreak(element.getText());
						
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
	 * @see org.zxframework.doc.DBAction#go(org.zxframework.doc.DocBuilder)
	 */
	public zXType.rc go(DocBuilder pobjDocBuilder) throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        zXType.rc go = zXType.rc.rcOK;
        
        try {
        	
        	/**
        	 * Load entities as we may refer to them in one of the directors
        	 */
        	
        	/**
        	 * Get and load entities
        	 */
        	Map colEntities = pobjDocBuilder.resolveEntities(this);
        	if (colEntities == null) {
        		throw new ZXException("Failed to get entities");
        	}
        	
        	pobjDocBuilder.loadEntities(colEntities);
        	
        	/**
        	 * Get object to copy and the object to copy to
        	 */
        	if (pobjDocBuilder.getObject(getRefobject(), false).pos != zXType.rc.rcOK.pos) {
        		throw new ZXException("Failed to getObject for refObject");
        	}
        	
        	if (pobjDocBuilder.getObject(getCopyobject(), true).pos != zXType.rc.rcOK.pos) {
        		throw new ZXException("Failed to getObject for copyObject");
        	}
        	
        	if (getDocBuilder().getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
        		
        		if (getDisPosition().equals(zXType.docBuilderDisposition.dbdpAfter)) {
        			/**
        			 * Copy to clipboard
        			 */
        			// TODO : Openoffice
        			
        		} else if (getDisPosition().equals(zXType.docBuilderDisposition.dbdpBefore)) {
        			/**
        			 * Copy to clipboard
        			 */
        			// TODO : Openoffice
        			
        		} else if (getDisPosition().equals(zXType.docBuilderDisposition.dbdpReplace)) {
        			/**
        			 * Replace current table
        			 */
        			// TODO : Openoffice
        			
        		} // disposition
        		
        	} // engine type
        	
        	return go;
        } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process DBAddObject action.", e);
	        
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
	
	//------------------------ Object implemeted methods
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		DBAddObject objDBAction = null;
		
		try {
			objDBAction = (DBAddObject)super.clone();
			
			if (getRefobject() != null) {
				objDBAction.setRefobject((DBObject)getRefobject().clone());
			}
			
			if (getCopyobject() != null) {
				objDBAction.setRefobject((DBObject)getCopyobject().clone());
			}
			
			objDBAction.setDisPosition(getDisPosition());
			objDBAction.setPageBreak(getPageBreak());
			
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
		
		toString.append(DBADDOBJECT_COPYOBJECT, getCopyobject());
		toString.append(DBADDOBJECT_DISPOSITION, zXType.valueOf(getDisPosition()));
		toString.append(DBADDOBJECT_PAGEBREAK, zXType.valueOf(getPageBreak()));
		toString.append(DBADDOBJECT_REFOBJECT, getRefobject());
		
		return toString.toString();
	}
}