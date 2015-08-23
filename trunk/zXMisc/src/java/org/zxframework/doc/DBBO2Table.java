/*
 * Created on Jun 9, 2005
 * $Id: DBBO2Table.java,v 1.1.2.13 2005/10/17 09:50:34 mike Exp $
 */
package org.zxframework.doc;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.DomElementUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * The implementation of the BO2Table doc builder action.
 * 
 * <pre>
 * 
 * Builds a table based on a business object.
 * 
 * 
 * Who    : Bertus Dispa
 * When   : 18 May 2003
 * 
 * Change    : BD9JUN03
 * Why       : Forgot to implement bespoke table
 *
 * Change    : BD20OCT03
 * Why       : Support for macros
 *
 * Change    : BD21NOV03
 * Why       : Allow for multi-line fields
 * </pre>
 *  
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBBO2Table extends DBAction {
	
	private static Log log = LogFactory.getLog(DBBO2Table.class);
	
	//------------------------ Members
	
	private DBObject refobject;
	private String fixedrows;
	private String fixedcols;
	private zXType.docBuilderGridType tableType;
	private boolean includeEntityName;
	private boolean noLabel;
	
	//------------------------ XML Element Constants
	
	private static final String DBBO2TABLE_REFOBJECT = "refobject";
	private static final String DBBO2TABLE_FIXEDROWS = "fixedrows";
	private static final String DBBO2TABLE_FIXEDCOLS = "fixedcols";
	private static final String DBBO2TABLE_INCLUDEENTITYNAME = "includeentityname";
	private static final String DBBO2TABLE_NOLABEL = "nolabel";
	private static final String DBBO2TABLE_TABLETYPE = "tabletype";
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public DBBO2Table() {
		super();
	}
	
	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the fixedcols.
	 */
	public String getFixedcols() {
		return fixedcols;
	}
	
	/**
	 * @param fixedcols The fixedcols to set.
	 */
	public void setFixedcols(String fixedcols) {
		this.fixedcols = fixedcols;
	}
	
	/**
	 * @return Returns the fixedrows.
	 */
	public String getFixedrows() {
		return fixedrows;
	}
	
	/**
	 * @param fixedrows The fixedrows to set.
	 */
	public void setFixedrows(String fixedrows) {
		this.fixedrows = fixedrows;
	}
	
	/**
	 * @return Returns the includeentityname.
	 */
	public boolean isIncludeEntityName() {
		return includeEntityName;
	}
	
	/**
	 * @param includeentityname The includeentityname to set.
	 */
	public void setIncludeEntityName(boolean includeentityname) {
		this.includeEntityName = includeentityname;
	}
	
	/**
	 * @param includeentityname The includeentityname to set.
	 */
	public void setIncludeentityname(String includeentityname) {
		this.includeEntityName = StringUtil.booleanValue(includeentityname);
	}
	
	/**
	 * @return Returns the nolabel.
	 */
	public boolean isNoLabel() {
		return noLabel;
	}
	
	/**
	 * @param nolabel The nolabel to set.
	 */
	public void setNoLabel(boolean nolabel) {
		this.noLabel = nolabel;
	}
	
	/**
	 * @param nolabel The nolabel to set.
	 */
	public void setNolabel(String nolabel) {
		this.noLabel = StringUtil.booleanValue(nolabel);
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
	 * @return Returns the tableType.
	 */
	public zXType.docBuilderGridType getTableType() {
		return tableType;
	}
	
	/**
	 * @param tableType The tableType to set.
	 */
	public void setTableType(zXType.docBuilderGridType tableType) {
		this.tableType = tableType;
	}
	
	//------------------------ Digester helper methods.
	
	/**
	 * @param tableType The tableType to set.
	 */
	public void setTabletype(String tableType) {
		this.tableType = zXType.docBuilderGridType.getEnum(tableType);
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
            	objXMLGen.openTag(DBBO2TABLE_REFOBJECT);
            	getRefobject().dumpAsXML(objXMLGen);
            	objXMLGen.closeTag(DBBO2TABLE_REFOBJECT);
            }
            
            objXMLGen.taggedCData(DBBO2TABLE_FIXEDROWS, getFixedrows(), false);
            objXMLGen.taggedCData(DBBO2TABLE_FIXEDCOLS, getFixedcols(), false);
            objXMLGen.taggedValue(DBBO2TABLE_INCLUDEENTITYNAME, isIncludeEntityName());
            objXMLGen.taggedValue(DBBO2TABLE_NOLABEL, isNoLabel());
        	objXMLGen.taggedValue(DBBO2TABLE_TABLETYPE, zXType.valueOf(getTableType()));
            
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
					
					if (DBBO2TABLE_REFOBJECT.equalsIgnoreCase(nodeName)) {
						this.refobject = new DBObject();
						this.refobject.parse((Element)node);
						
					} else if (DBBO2TABLE_FIXEDROWS.equalsIgnoreCase(nodeName)) {
						setFixedrows(element.getText());
						
					} else if (DBBO2TABLE_FIXEDCOLS.equalsIgnoreCase(nodeName)) {
						setFixedcols(element.getText());
						
					} else if (DBBO2TABLE_INCLUDEENTITYNAME.equalsIgnoreCase(nodeName)) {
						setIncludeentityname(element.getText());
						
					} else if (DBBO2TABLE_NOLABEL.equalsIgnoreCase(nodeName)) {
						setNolabel(element.getText());
						
					} else if (DBBO2TABLE_TABLETYPE.equalsIgnoreCase(nodeName)) {
						setTabletype(element.getText());
						
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
        	 * Get entities
        	 */
        	Map colEntities = pobjDocBuilder.resolveEntities(this);
        	if (colEntities == null) {
        		throw new ZXException("Failed to get entities");
        	}
        	
        	/**
        	 * And load
        	 */
        	pobjDocBuilder.loadEntities(colEntities);
        	
        	/**
        	 * Get object
        	 */
        	if (pobjDocBuilder.getObject(getRefobject(), false).pos != zXType.rc.rcOK.pos) {
        		throw new ZXException("Failed to getObject for refObject");
        	}
        	
        	/**
        	 * Get fixed rows / fixed cols
        	 */
        	int intFixedRows = 0;
        	String strTmp = getZx().resolveDirector(getFixedrows());
            if (StringUtil.isNumeric(strTmp)) { 
            	intFixedRows = Integer.parseInt(strTmp);
            }
            
            int intFixedCols = 0;
            strTmp = getZx().resolveDirector(getFixedcols());
            if (StringUtil.isNumeric(strTmp)) { 
            	intFixedCols = Integer.parseInt(strTmp);
            }
            
            Iterator iter;
            DBEntity objEntity;
            AttributeCollection colAttr;
            Attribute objAttr;
            int intRow = 0;
            
        	/**
        	 * See what we need to do about the table
        	 */
            if (getTableType().equals(zXType.docBuilderGridType.dbgtStandardBO)) {
            	/**
            	 * Labels in column 1 and values in column 2
            	 */
            	if (getDocBuilder().getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
            		iter = colEntities.values().iterator();
            		while (iter.hasNext()) {
            			objEntity = (DBEntity)iter.next();
            			
            			colAttr = objEntity.getBo().getDescriptor().getGroup(objEntity.getResolvedUseGroup());
            			Iterator iterAttr = colAttr.values().iterator();
            			while (iterAttr.hasNext()) {
            				objAttr = (Attribute)iterAttr.next();
            				
            				if (intRow > 0) {
            					pobjDocBuilder.addRow(getRefobject());
            				}
            				
            				String strValue;
            				if (objAttr.isMultiLine()) {
            					strValue = objEntity.getBo().getValue(objAttr.getName()).formattedValue();
            				} else {
            					strValue = objEntity.getBo().getValue(objAttr.getName()).formattedValue();
            				}
            				
            				if (isNoLabel()) {
            					pobjDocBuilder.setCell(getRefobject(), 
            										   intFixedRows + intRow, 
            										   intFixedCols + 1, 
            										   strValue);
                                
            				} else {
            					pobjDocBuilder
            					.setCell(getRefobject(), 
            							 intFixedRows + intRow, 
            							 intFixedCols + 1, 
            							 (isIncludeEntityName()?
            							 		objEntity.getBo().getDescriptor().getLabel().getLabel():"") 
            							 + objAttr.getLabel().getLabel());
            					
                                pobjDocBuilder.setCell(getRefobject(), 
                                					   intFixedRows + intRow, 
                                					   intFixedCols + 2, 
                                					   strValue);
                                
            				}
            				
            				intRow++;
            			}
            		}
            		
            	} // Engine
            	
            } else if (getTableType().equals(zXType.docBuilderGridType.dbgtBespoke)) {
            	/**
            	 * Assume labels in column 1 and values in column 2 but assume
            	 * all rows in place
            	 */
            	if (getDocBuilder().getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
            		
            		iter = colEntities.values().iterator();
            		while (iter.hasNext()) {
            			objEntity = (DBEntity)iter.next();
            			
            			colAttr = objEntity.getBo().getDescriptor().getGroup(objEntity.getResolvedUseGroup());
            			Iterator iterAttr = colAttr.values().iterator();
            			while (iterAttr.hasNext()) {
            				objAttr = (Attribute)iterAttr.next();
            				
            				String strValue;
            				if (objAttr.isMultiLine()) {
            					// strValue = Replace$(objEntity.BO.GetAttr(objAttr.name).formattedValue, vbCrLf, vbCr)
            					strValue = objEntity.getBo().getValue(objAttr.getName()).formattedValue();
            					
            				} else {
            					strValue = objEntity.getBo().getValue(objAttr.getName()).formattedValue();
            					
            				}
            				
            				if (isNoLabel()) {
            					pobjDocBuilder.setCell(getRefobject(), 
            										   intFixedRows + intRow, 
            										   intFixedCols + 1, 
            										   strValue);
                                
            				} else {
            					pobjDocBuilder
            					.setCell(getRefobject(), 
            							 intFixedRows + intRow, 
            							 intFixedCols + 1, 
            							 (isIncludeEntityName()?
            							 		objEntity.getBo().getDescriptor().getLabel().getLabel():"") 
            							 + objAttr.getLabel().getLabel());
            					
                                pobjDocBuilder.setCell(getRefobject(), 
                                					   intFixedRows + intRow, 
                                					   intFixedCols + 2, 
                                					   strValue);
                                
            				}
            				
            				intRow++;
            			}
            		}
            		
            	} // Engine
            	
            } // grid type
            
            /**
             * Recalc if needed
             */
            if (getRefobject().isReCalc()) {
            	if (getDocBuilder().getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
            		// Note : this will update all of the fields for this object.
            		OpenOfficeDocument.updateFields(((Document)pobjDocBuilder.getWordNewDoc()).wordDoc());
            		// , getRefobject().getWordobject());
            	}
            	
            }
            
        	return go;
        } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process DBBO2Table action.", e);
	        
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
		DBBO2Table objDBAction = null;
		
		try {
			objDBAction = (DBBO2Table)super.clone();
			
			if (getRefobject() != null) {
				objDBAction.setRefobject((DBObject)getRefobject().clone());
			}
			
			objDBAction.setFixedrows(getFixedrows());
			objDBAction.setFixedcols(getFixedcols());
			objDBAction.setTableType(getTableType());
			objDBAction.setIncludeEntityName(isIncludeEntityName());
			objDBAction.setNoLabel(isNoLabel());
			
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
		
		toString.append(DBBO2TABLE_FIXEDCOLS, getFixedcols());
		toString.append(DBBO2TABLE_FIXEDROWS, getFixedrows());
		toString.append(DBBO2TABLE_INCLUDEENTITYNAME, isIncludeEntityName());
		toString.append(DBBO2TABLE_NOLABEL, isNoLabel());
		toString.append(DBBO2TABLE_REFOBJECT, getRefobject());
		toString.append(DBBO2TABLE_TABLETYPE, zXType.valueOf(getTableType()));
		
		return toString.toString();
	}
}