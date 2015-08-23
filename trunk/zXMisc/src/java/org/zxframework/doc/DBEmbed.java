/*
 * Created on Jun 9, 2005
 * $Id$
 */
package org.zxframework.doc;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

import com.sun.star.table.XCell;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;

/**
 * The implementation of the embed doc builder action.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 18 May 2003
 * 
 * Change    : DGS06AUG2003
 * Why       : Added property dispToCell, and use this to decide whether to include
 *             the embedded object before, after or inside the cell.
 * </pre>
 *  
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBEmbed extends DBAction {
	
	private static Log log = LogFactory.getLog(DBEmbed.class);
	
	//------------------------ Members
	
	private DBObject refobject;
	private String filename;
	private boolean useCellSize;
	private zXType.docBuilderDisposition dispToCell;
	
	//------------------------ XML Element Constants
	
	private static final String DBEMBED_REFOBJECT = "refobject";
	private static final String DBEMBED_FILENAME = "filename";
	private static final String DBEMBED_USECELLSIZE = "usecellsize";
	private static final String DBEMBED_DISPTOCELL = "disptocell";
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public DBEmbed() {
		super();
	}
	
	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the dispToCell.
	 */
	public zXType.docBuilderDisposition getDispToCell() {
		return dispToCell;
	}
	
	/**
	 * @param dispToCell The dispToCell to set.
	 */
	public void setDispToCell(zXType.docBuilderDisposition dispToCell) {
		this.dispToCell = dispToCell;
	}
	
	/**
	 * @return Returns the filename.
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * @param filename The filename to set.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
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
	 * @return Returns the usecellsize.
	 */
	public boolean isUseCellSize() {
		return useCellSize;
	}
	
	/**
	 * @param usecellsize The usecellsize to set.
	 */
	public void setUseCellSize(boolean usecellsize) {
		this.useCellSize = usecellsize;
	}
	
	/**
	 * @param usecellsize The usecellsize to set.
	 */
	public void setUsecellsize(String usecellsize) {
		this.useCellSize = StringUtil.booleanValue(usecellsize);
	}
	
	//------------------------ Digester helper methods.
	
	/**
	 * @param dispToCell The dispToCell to set.
	 */
	public void setDisptocell(String dispToCell) {
		this.dispToCell = zXType.docBuilderDisposition.getEnum(dispToCell);
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
            	objXMLGen.openTag(DBEMBED_REFOBJECT);
            	getRefobject().dumpAsXML(objXMLGen);
            	objXMLGen.closeTag(DBEMBED_REFOBJECT);
            }
            
            objXMLGen.taggedCData(DBEMBED_FILENAME, getFilename(), false);
            objXMLGen.taggedValue(DBEMBED_USECELLSIZE, isUseCellSize());
        	objXMLGen.taggedValue(DBEMBED_DISPTOCELL, zXType.valueOf(getDispToCell()));
        	
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
        	Element element;
			String nodeName;
			Node node;
			
			NodeList nodeList = pobjXMLNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				if (node instanceof Element) {
					element = (Element)node;
					nodeName = element.getNodeName();
					
					if (DBEMBED_REFOBJECT.equalsIgnoreCase(nodeName)) {
						this.refobject = new DBObject();
						this.refobject.parse(element);
						
					} else if (DBEMBED_FILENAME.equalsIgnoreCase(nodeName)) {
						setFilename(element.getFirstChild().getNodeValue());
						
					} else if (DBEMBED_USECELLSIZE.equalsIgnoreCase(nodeName)) {
						setUsecellsize(element.getFirstChild().getNodeValue());
						
					} else if (DBEMBED_DISPTOCELL.equalsIgnoreCase(nodeName)) {
						setDisptocell(element.getFirstChild().getNodeValue());
						
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
        	 * Load entities as we may refer to them ni one of the directors
        	 */
        	Map colEntities = pobjDocBuilder.resolveEntities(this);
        	if (colEntities == null) {
        		throw new ZXException("Failed to get entities");
        	}
        	
        	pobjDocBuilder.loadEntities(colEntities);
        	
        	/**
        	 * Get object
        	 */
        	if (pobjDocBuilder.getObject(this.refobject, false).pos != zXType.rc.rcOK.pos) {
        		throw new ZXException("Failed to getObject for refObject");
        	}
        	
        	if (pobjDocBuilder.getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
        		/**
        		 * Get cell that we need
        		 */
        		String strRow = getZx().resolveDirector(this.refobject.getRow());
                String strColumn = getZx().resolveDirector(this.refobject.getColumn());
                
                if (!StringUtil.isNumeric(strRow)) {
                	throw new ZXException("Row is not numeric", strRow);
                }
                
                if (!StringUtil.isNumeric(strColumn)) {
                	throw new ZXException("Column is not numeric", strColumn);
                }
                
                // Openoffice Index correction.
                int intRow = Integer.parseInt(strRow) - 1;
                int intColumn = Integer.parseInt(strColumn) - 1;
                
                XCell objWordCell = (XCell)OpenOfficeDocument.getTableCell(this.refobject.getWordobject(), 
                														   intRow, 
                											  			   intColumn);
                XText objCellText = QI.XText(objWordCell);

                if (this.dispToCell.equals(zXType.docBuilderDisposition.dbdpBefore)) {
                	XTextCursor xTextCursor = objCellText.createTextCursor();
                	xTextCursor.gotoStart(false);
                	
                	// objRange.End = objRange.Start - 1
                    // objRange.Start = objRange.End - 1
                	
                } else if (this.dispToCell.equals(zXType.docBuilderDisposition.dbdpAfter)) {
                	XTextCursor xTextCursor = objCellText.createTextCursor();
                	xTextCursor.gotoEnd(false);
                	
                	// objRange.Start = objRange.End + 1
                    // objRange.End = objRange.Start + 1
                	
                } else {
                	// In the cell - no change to range
                	
                }
                
                /**
                 * Actual way of embedding depends on file type
                 */
                String strFileName = getZx().resolveDirector(this.filename);
                String extension = OpenOfficeDocument.getFileExtension(strFileName);
                
                if (StringUtil.equalsAnyOf("xls,xxx".split(","), extension)) {
                	/**
                	 * Assume readable file that can be inserted
                	 */
                	go = OpenOfficeDocument.insertOLEObject(((Document)pobjDocBuilder.getWordNewDoc()).wordDoc(),
                											objCellText, 
                											strFileName);
                	
                } else if (StringUtil.equalsAnyOf("doc,txt,csv,htm,xml,rtf,xls".split(","), extension)) {
                	/**
                	 * Assume readable file that can be inserted
                	 */
                	go = OpenOfficeDocument.insertFile(objCellText, strFileName);
                
                /**
                 * Insert a graphic.
                 */
                //} else if (extension.equals("tif")) {
                // Do not use for now.
                
                } else if (StringUtil.equalsAnyOf("jpeg,jpg,png,gif,bmp,tif".split(","), extension)) {
                	/**
                	 * Get the height and width of the cell if needed
                	 */
                	go = OpenOfficeDocument.insertGraphic(((Document)pobjDocBuilder.getWordNewDoc()).wordDoc(),
							  objCellText, 
							  strFileName,
							  this.useCellSize);
                	
                } else {
                	objCellText.setString("Unsupported file type - please print manually");
                	
                } // File type
                
                if (!getDispToCell().equals(zXType.docBuilderDisposition.dbdpReplace)) {
                	// objWordCell.Delete
                }
                
                /**
                 * Recalc if needed
                 */
                if (getRefobject().isReCalc()) {
                	// Me.refObject.wordObject.Range.fields.Update
                	OpenOfficeDocument.updateFields(((Document)pobjDocBuilder.getWordNewDoc()).wordDoc());
                }
                
        	} // engine type
        	
        	return go;
        } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process DBEmbed action.", e);
	        
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
		DBEmbed objDBAction = null;
		
		try {
			objDBAction = (DBEmbed)super.clone();
			
			if (getRefobject() != null) {
				objDBAction.setRefobject((DBObject)getRefobject().clone());
			}
			
			objDBAction.setFilename(getFilename());
			objDBAction.setUseCellSize(isUseCellSize());
			objDBAction.setDispToCell(getDispToCell());
			
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
		
		toString.append(DBEMBED_DISPTOCELL, zXType.valueOf(getDispToCell()));
		toString.append(DBEMBED_FILENAME, getFilename());
		toString.append(DBEMBED_REFOBJECT, getRefobject());
		toString.append(DBEMBED_USECELLSIZE, isUseCellSize());
		
		return toString.toString();
	}
}