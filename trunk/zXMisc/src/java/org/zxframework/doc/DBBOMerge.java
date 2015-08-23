/*
 * Created on Jun 9, 2005
 * $Id: DBBOMerge.java,v 1.1.2.13 2005/09/02 09:41:11 mike Exp $
 */
package org.zxframework.doc;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.DomElementUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;
import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.text.XText;
import com.sun.star.text.XTextTable;

/**
 * The BO Merge action implementation.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 17 May 2003
 * 
 * Change    : BD20OCT03
 * Why       : Support for macros
 *
 * Change    : BD21NOV03
 * Why       : Allow for multi-line fields
 *
 * Change    : BD18MAY04
 * Why       : Multi-line fields in Word caused major pain, now
 *             replace CrLf with ^p and let Word handle new lines
 *
 * Change    : DGS27SEP2004
 * Why       : Multi-line fields in Word again: also replace single LF by ^p
 * </pre>
 *  
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBBOMerge extends DBAction {
	
	private static Log log = LogFactory.getLog(DBBOMerge.class);
	
	//------------------------ Members
	
	private DBObject refobject;
	private String opentag;
	private String closetag;
	
	//------------------------ XML Element Constants
	
	private static final String DBBOMERGE_REFOBJECT = "refobject";
	private static final String DBBOMERGE_OPENTAG = "opentag";
	private static final String DBBOMERGE_CLOSETAG = "closetag";
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public DBBOMerge() {
		super();
	}
	
	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the closetag.
	 */
	public String getClosetag() {
		return closetag;
	}
	
	/**
	 * @param closetag The closetag to set.
	 */
	public void setClosetag(String closetag) {
		this.closetag = closetag;
	}
	
	/**
	 * @return Returns the opentag.
	 */
	public String getOpentag() {
		return opentag;
	}
	
	/**
	 * @param opentag The opentag to set.
	 */
	public void setOpentag(String opentag) {
		this.opentag = opentag;
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
            	objXMLGen.openTag(DBBOMERGE_REFOBJECT);
            	getRefobject().dumpAsXML(objXMLGen);
            	objXMLGen.closeTag(DBBOMERGE_REFOBJECT);
            }
            
            objXMLGen.taggedCData(DBBOMERGE_OPENTAG, getOpentag(), false);
            objXMLGen.taggedCData(DBBOMERGE_CLOSETAG, getClosetag(), false);
            
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
			
			setOpentag("<");
			setClosetag(">");
			
			NodeList nodeList = pobjXMLNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				if (node instanceof Element) {
					element = new DomElementUtil((Element)node);
					nodeName = node.getNodeName();
					
					if (DBBOMERGE_REFOBJECT.equalsIgnoreCase(nodeName)) {
						this.refobject = new DBObject();
						this.refobject.parse((Element)node);
						
					} else if (DBBOMERGE_OPENTAG.equalsIgnoreCase(nodeName)) {
						setOpentag(element.getText());
						
					} else if (DBBOMERGE_CLOSETAG.equalsIgnoreCase(nodeName)) {
						setClosetag(element.getText());
						
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
        	 * Get and load entities
        	 */
        	Map colEntities = pobjDocBuilder.resolveEntities(this);
        	if (colEntities == null) {
        		throw new ZXException("Failed to get entities");
        	}
        	
        	pobjDocBuilder.loadEntities(colEntities);
        	
        	/**
        	 * Get object
        	 */
        	if (pobjDocBuilder.getObject(getRefobject(), false).pos != zXType.rc.rcOK.pos) {
        		throw new ZXException("Failed to getObject for refObject");
        	}
        	
        	if (pobjDocBuilder.getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
        		/**
        		 * Get cell that we need
        		 */
        		String strRow = getZx().resolveDirector(getRefobject().getRow());
                String strColumn = getZx().resolveDirector(getRefobject().getColumn());
                
                Object objWordRange = null;
                
    			XTextTable xTable = QI.XTextTable(getRefobject().getWordobject());
                if (StringUtil.isNumeric(strRow) && StringUtil.isNumeric(strColumn)) {
                	// Openoffice Index correction.
                	int intRow = Integer.parseInt(strRow) - 1;
                	int intColumn = Integer.parseInt(strColumn) - 1;
                	
        			XCellRange xCellRange = QI.XCellRange(xTable);
                    XCell xCell = xCellRange.getCellByPosition(intColumn, intRow);
                    objWordRange = QI.XText(xCell);
                    
                } else {
                	objWordRange = QI.XCellRange(xTable);
                }
        		
                String strUseGroup;
                DBEntity objEntity;
                
                Iterator iter = colEntities.values().iterator();
                while (iter.hasNext()) {
                	objEntity = (DBEntity)iter.next();
                	
                	strUseGroup = getZx().resolveDirector(objEntity.getUsegroup());
                	
                	if (StringUtil.len(strUseGroup) > 0) {
                		AttributeCollection colAttr = objEntity.getBo().getDescriptor().getGroup(strUseGroup);
                		
                		Iterator iterAttr = colAttr.values().iterator();
                		while (iterAttr.hasNext()) {
                			Attribute objAttr = (Attribute)iterAttr.next();
                			
                			String strSearch = getOpentag() + objEntity.getName() + "." + objAttr.getName() + getClosetag();
    						String strReplace = objEntity.getBo().getValue(objAttr.getName()).formattedValue();
                			
                			/**
                			 * Make generic
                			 */
        					if (objWordRange instanceof XText) {
        						/**
        						 * We have specified a specific cell
        						 */
        						XText xTextCell = (XText)objWordRange;
        						xTextCell.setString(StringUtil.replaceAll(xTextCell.getString(), 
        																  strSearch, 
        																  strReplace));
        						
        					} else {
        						/**
        						 * Do a search inside a whole table
        						 */
        						XCellRange xCellRange = (XCellRange)objWordRange;
        						int intRows = xTable.getRows().getCount();
        						int intColumns = xTable.getColumns().getCount();
        						for (int row = 0; row < intRows; row++) {
        							for (int column = 0; column < intColumns; column++) {
        			                    XCell xCell = xCellRange.getCellByPosition(column, row);
        			                    XText xTextCell = QI.XText(xCell);
        								xTextCell.setString(StringUtil.replaceAll(xTextCell.getString(), 
        																		  strSearch,
        																		  strReplace));
        							}
        						}
        					}
                		}
                	}
                	
                }
                
        	}
        	
        	return go;
        } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process DBBOMerge action.", e);
	        
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
		DBBOMerge objDBAction = null;
		
		try {
			objDBAction = (DBBOMerge)super.clone();
			
			if (getRefobject() != null) {
				objDBAction.setRefobject((DBObject)getRefobject().clone());
			}
			
			objDBAction.setOpentag(getOpentag());
			objDBAction.setClosetag(getClosetag());
			
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
		
		toString.append(DBBOMERGE_CLOSETAG, getClosetag());
		toString.append(DBBOMERGE_OPENTAG, getOpentag());
		toString.append(DBBOMERGE_REFOBJECT, getRefobject());
		
		return toString.toString();
	}
}