/*
 * Created on Jun 9, 2005
 * $Id: DBMerge.java,v 1.1.2.13 2005/11/21 15:15:35 mike Exp $
 */
package org.zxframework.doc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.DomElementUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;
import org.zxframework.Tuple;
import org.zxframework.ZXException;
import org.zxframework.zXType;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.text.XText;
import com.sun.star.text.XTextTable;

/**
 * The implementation of the merge doc builder action
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 18 May 2003
 * 
 * Change    : BD20OCT03
 * Why       : Added support for macros
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
public class DBMerge extends DBAction {
	
	private static Log log = LogFactory.getLog(DBMerge.class);
	
	//------------------------ Members
	
	private DBObject refobject;
	private String opentag;
	private String closetag;
	private List values;
	
	//------------------------ XML Element Constants
	
	private static final String DBMERGE_REFOBJECT = "refobject";
	private static final String DBMERGE_OPENTAG = "opentag";
	private static final String DBMERGE_CLOSETAG = "closetag";
	private static final String DBMERGE_VALUES = "values";
	
	//------------------------ Constructors
	
	/**
	 * Default constructors.
	 */
	public DBMerge() {
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
	
	/**
	 * A collection (ArrayList)(Tuple) of values.
	 * 
	 * @return Returns the values.
	 */
	public List getValues() {
		if (this.values == null) {
			this.values = new ArrayList();
		}
		return values;
	}
	
	/**
	 * @param values The values to set.
	 */
	public void setValues(List values) {
		this.values = values;
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
            	objXMLGen.openTag(DBMERGE_REFOBJECT);
            	getRefobject().dumpAsXML(objXMLGen);
            	objXMLGen.closeTag(DBMERGE_REFOBJECT);
            }
            
            objXMLGen.taggedCData(DBMERGE_OPENTAG, getOpentag(), false);
            objXMLGen.taggedCData(DBMERGE_CLOSETAG, getClosetag(), false);
            
            if (getValues() != null) {
            	objXMLGen.openTag(DBMERGE_VALUES);
            	
            	Tuple objTuple;
            	int intValues = getValues().size();
            	for (int i = 0; i < intValues; i++) {
            		objTuple = (Tuple)getValues().get(i);
            		
            		objXMLGen.openTag("value");
            		objXMLGen.taggedCData("source", objTuple.getName());
            		objXMLGen.taggedCData("destination", objTuple.getValue());
            		objXMLGen.closeTag("value");
            	}
            	objXMLGen.closeTag(DBMERGE_VALUES);
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
					
					if (DBMERGE_REFOBJECT.equalsIgnoreCase(nodeName)) {
						this.refobject = new DBObject();
						this.refobject.parse((Element)node);
						
					} else if (DBMERGE_OPENTAG.equalsIgnoreCase(nodeName)) {
						setOpentag(element.getText());
						
					} else if (DBMERGE_CLOSETAG.equalsIgnoreCase(nodeName)) {
						setClosetag(element.getText());
						
					} else if (DBMERGE_VALUES.equalsIgnoreCase(nodeName)) {
						setValues(DBAction.parseTuples((Element)node));
						
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
             * Early exit.
             */
            if (this.values == null) {
            	return go;
            }
            
        	/**
        	 * Get and load entities
        	 */
        	Map colEntities = pobjDocBuilder.resolveEntities(this);
        	if (colEntities == null) {
        		throw new ZXException("Failed to get entities");
        	}
        	
        	/**
        	 * And load
        	 */
        	pobjDocBuilder.loadEntities(colEntities);
        	
        	if (pobjDocBuilder.getObject(getRefobject(), false).pos != zXType.rc.rcOK.pos) {
        		go = zXType.rc.rcWarning;
        		return go;
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
                
                Tuple objValue;
                
                for (int i = 0; i < this.values.size(); i++) {
                	objValue = (Tuple)this.values.get(i);
                	
					String strSearch = this.opentag + getZx().resolveDirector(objValue.getValue()) + this.closetag;
					
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
																  objValue.getName()));
						
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
																		  objValue.getName()));
							}
						}
						
					}
					
					/**
					 * BD18MAY04 Impotrant change: ^p seems to create a better result
					 * 27SEP2004 ^p also needs to replace a single LF, not just CR/LF
					 */
					//pobjDocBuilder.rangeReplace(objWordRange,
					//						strSearch,
					//						objValue.getName());
					// Replace$(Replace$(getzX().resolveDirector(objValue.getname()), vbCrLf, "^p"), Chr$(10), "^p")					
				}
        		
        	}
        	
        	return go;
        } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process DBMerge action.", e);
	        
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
		DBMerge objDBAction = null;
		
		try {
			objDBAction = (DBMerge)super.clone();
			
			if (getRefobject() != null) {
				objDBAction.setRefobject((DBObject)getRefobject().clone());
			}
			
			objDBAction.setOpentag(getOpentag());
			objDBAction.setClosetag(getClosetag());
			
			if (this.values != null && this.values.size() > 0) {
				objDBAction.setValues(CloneUtil.clone((ArrayList)this.values));
			}
			
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
		
		toString.append(DBMERGE_CLOSETAG, getClosetag());
		toString.append(DBMERGE_OPENTAG, getOpentag());
		toString.append(DBMERGE_REFOBJECT, getRefobject());
		toString.append(DBMERGE_VALUES, getValues());
		
		return toString.toString();
	}
}