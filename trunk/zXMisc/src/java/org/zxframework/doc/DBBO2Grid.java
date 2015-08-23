/*
 * Created on Jun 9, 2005
 * $Id: DBBO2Grid.java,v 1.1.2.14 2006/07/17 16:10:54 mike Exp $
 */
package org.zxframework.doc;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.DomElementUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * The implementation of the bo2grid doc builder action.
 * 
 * <pre>
 * 
 * TODO : Openoffice :
 * 1) Text Alignment for a Table cell.
 * 2) Split table cell.
 * 
 * 
 * Who    : Bertus Dispa
 * When   : 18 May 2003
 * 
 * Change    : BD20OCT03
 * Why       : Support for macros
 *
 * Change    : BD21NOV03
 * Why       : - Allow for multi-line fields
 *             - Fixed problem with adding phantom rows to bespoke grids
 *
 * Change    : BD21NOV03
 * Why       : Fixed fix with problem with adding phantom rows to bespoke grids (see 21NOV03)
 *
 * Change    : BD10MAR04
 * Why       : Better handling of standard grid
 *
 * Change    : BD25MAR04
 * Why       : Fixed bug with skipping 1 row in case of fixed rows
 *
 * Change    : BD5APR05 - V1.5:1
 * Why       : Added support for data-sources
 * 
 * </pre>
 *  
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBBO2Grid extends DBAction {
	
	private static Log log = LogFactory.getLog(DBBO2Grid.class);
	
	//------------------------ Members
	
	private String queryname;
	private DBObject refobject;
	private String fixedrows;
	private String fixedcols;
	private int maxrows;
	private boolean showNumrows;
	private zXType.docBuilderGridType gridType;
	private String active;
	
	//------------------------ XML Elements Constants
	
	private static final String DBBO2GRID_QUERYNAME = "queryname";
	private static final String DBBO2GRID_REFOBJECT = "refobject";
	private static final String DBBO2GRID_FIXEDROWS = "fixedrows";
	private static final String DBBO2GRID_FIXEDCOLS = "fixedcols";
	private static final String DBBO2GRID_ACTIVE = "active";
	private static final String DBBO2GRID_MAXROWS = "maxrows";
	private static final String DBBO2GRID_SHOWNUMROWS = "shownumrows";
	private static final String DBBO2GRID_GRIDTYPE = "gridtype";
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public DBBO2Grid() {
		super();
	}

	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the active.
	 */
	public String getActive() {
		return active;
	}
	
	/**
	 * @param active The active to set.
	 */
	public void setActive(String active) {
		this.active = active;
	}
	
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
	 * @return Returns the gridType.
	 */
	public zXType.docBuilderGridType getGridType() {
		return gridType;
	}
	
	/**
	 * @param gridType The gridType to set.
	 */
	public void setGridType(zXType.docBuilderGridType gridType) {
		this.gridType = gridType;
	}
	
	/**
	 * @return Returns the maxrows.
	 */
	public int getMaxrows() {
		return maxrows;
	}
	
	/**
	 * @param maxrows The maxrows to set.
	 */
	public void setMaxrows(int maxrows) {
		this.maxrows = maxrows;
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
	 * @return Returns the shownumrows.
	 */
	public boolean isShowNumrows() {
		return showNumrows;
	}
	
	/**
	 * @param shownumrows The shownumrows to set.
	 */
	public void setShowNumrows(boolean shownumrows) {
		this.showNumrows = shownumrows;
	}
	
	/**
	 * @param shownumrows The shownumrows to set.
	 */
	public void setShownumrows(String shownumrows) {
		this.showNumrows = StringUtil.booleanValue(shownumrows);
	}
	
	//------------------------ Digester helper methods
	
	/**
	 * @param gridType The gridType to set.
	 */
	public void setGridtype(String gridType) {
		this.gridType = zXType.docBuilderGridType.getEnum(gridType);
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
            
            objXMLGen.taggedCData(DBBO2GRID_QUERYNAME, getQueryname());
            
            if (getRefobject() != null) {
            	objXMLGen.openTag(DBBO2GRID_REFOBJECT);
            	getRefobject().dumpAsXML(objXMLGen);
            	objXMLGen.closeTag(DBBO2GRID_REFOBJECT);
            }
            
            objXMLGen.taggedCData(DBBO2GRID_FIXEDROWS, getFixedrows(), false);
            objXMLGen.taggedCData(DBBO2GRID_FIXEDCOLS, getFixedcols(), false);
            objXMLGen.taggedValue(DBBO2GRID_MAXROWS, String.valueOf(getMaxrows()));
            objXMLGen.taggedValue(DBBO2GRID_SHOWNUMROWS, isShowNumrows());
        	objXMLGen.taggedValue(DBBO2GRID_GRIDTYPE, zXType.valueOf(getGridType()));
            objXMLGen.taggedCData(DBBO2GRID_ACTIVE, getActive(), false);
            
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
     * @see DBAction#parse(org.w3c.dom.Element)
     */
    public zXType.rc parse(org.w3c.dom.Element pobjXMLNode) {
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
				if (node instanceof org.w3c.dom.Element) {
					element = new DomElementUtil((org.w3c.dom.Element)node);
					nodeName = node.getNodeName();
					
					if (DBBO2GRID_QUERYNAME.equalsIgnoreCase(nodeName)) {
						setQueryname(element.getText());
						
					} else if (DBBO2GRID_REFOBJECT.equalsIgnoreCase(nodeName)) {
						this.refobject = new DBObject();
						this.refobject.parse((org.w3c.dom.Element)node);
						
					} else if (DBBO2GRID_FIXEDROWS.equalsIgnoreCase(nodeName)) {
						setFixedrows(element.getText());
						
					} else if (DBBO2GRID_FIXEDCOLS.equalsIgnoreCase(nodeName)) {
						setFixedcols(element.getText());
						
					} else if (DBBO2GRID_ACTIVE.equalsIgnoreCase(nodeName)) {
						setActive(element.getText());
						
					} else if (DBBO2GRID_MAXROWS.equalsIgnoreCase(nodeName)) {
						setMaxrows(Integer.parseInt(element.getText()));
						
					} else if (DBBO2GRID_SHOWNUMROWS.equalsIgnoreCase(nodeName)) {
						setShownumrows(element.getText());
						
					} else if (DBBO2GRID_GRIDTYPE.equalsIgnoreCase(nodeName)) {
						setGridtype(element.getText());
						
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
        
        DSRS objRS = null;
        
        try {
        	
        	/**
        	 * Get entities for this DBAction.
        	 */
        	Map colEntities = pobjDocBuilder.resolveEntities(this);
        	if (colEntities == null) {
        		throw new ZXException("Failed to get entities");
        	}
        	
        	/**
        	 * See if we do not break any datasource rules
        	 */
        	if (pobjDocBuilder.validDataSourceEntities(colEntities)) {
        		throw new ZXException("Unsupported combination of data-source handlers");
        	}
        	
        	/** The first entity in the entity collection. */
        	DBEntity objTheEntity = (DBEntity)colEntities.values().iterator().next();
        	DSHandler objDSHandler = objTheEntity.getDsHandler();
        	
        	/**
        	 * Get object
        	 */
        	if (pobjDocBuilder.getObject(getRefobject(), false).pos != zXType.rc.rcOK.pos) {
        		throw new ZXException("Failed to getObject for refObject");
        	}
        	
        	/**
        	 * Construct query
        	 */
        	String strWhereClause = "";
        	String strOrderByClause = "";
        	boolean blnReverse = false;
        	
        	String strSQLQry = "";
        	
        	/**
        	 * Build and execute query
        	 */
        	if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
        		String strTmp = getZx().resolveDirector(getQueryname());
        		
                strWhereClause = pobjDocBuilder.getFromContext(strTmp + DocBuilder.QRYWHERECLAUSE);
                strOrderByClause = pobjDocBuilder.getFromContext(strTmp + DocBuilder.QRYORDERBYCLAUSE);
                
                if (StringUtil.len(strWhereClause) > 0) {
                	strWhereClause = ":" + strWhereClause;
                }
                
                if (StringUtil.len(strOrderByClause) > 1 && strOrderByClause.charAt(0) == '-') {
                	strOrderByClause = strOrderByClause.substring(1);
                	blnReverse = true;
                }
                
        	} else {
        		strSQLQry = pobjDocBuilder.constructQuery(getZx().resolveDirector(getQueryname()));
        		if (StringUtil.len(strSQLQry) == 0) {
        			throw new ZXException("Failed to get SQL query");
        		}
        		
        	}
        	
        	/**
        	 * Get fixed rows / fixed cols
        	 */
        	/** Number of fixed rows that cannot be altered. */
        	int intFixedRows = 0;
        	String strTmp = getZx().resolveDirector(this.fixedrows);
            if (StringUtil.isNumeric(strTmp)) {
            	intFixedRows = Integer.parseInt(strTmp);
            }
            
            /** Number of fixed columns that can not be altered. */
            int intFixedCols = 0;
            strTmp = getZx().resolveDirector(this.fixedcols);
            if (StringUtil.isNumeric(strTmp)) { 
            	intFixedCols = Integer.parseInt(strTmp);
            }
        	
            /**
             * Count the number of columns we will have and length
             */
            /** Total number of columns */
            int intNumColumns = intFixedCols;
            int intTotalLength = 0;
            DBEntity objEntity;
        	Attribute objAttr;
        	AttributeCollection colAttr;
        	Iterator iterAttr;
        	
            Iterator iter = colEntities.values().iterator();
            while (iter.hasNext()) {
            	objEntity = (DBEntity)iter.next();
            	
            	objEntity.setResolvedUseGroup(getZx().resolveDirector(objEntity.getUsegroup()));
                objEntity.setResolvedLoadGroup(getZx().resolveDirector(objEntity.getLoadgroup()));
                
                if (StringUtil.len(objEntity.getResolvedUseGroup()) > 0) {
                	colAttr = objEntity.getBo().getDescriptor().getGroup(objEntity.getResolvedUseGroup());
                	if (colAttr == null) {
                		throw new ZXException("Failed to get attribute group", objEntity.getResolvedUseGroup());
                	}
                	
                	intNumColumns = intNumColumns + colAttr.size();
                	
                	iterAttr = colAttr.iterator();
                	while (iterAttr.hasNext()) {
                		objAttr = (Attribute)iterAttr.next();
                		
                		intTotalLength = intTotalLength + objAttr.getOutputLength();
                	}
                }
            }
            
            /**
             * See what we need to do about the table
             */
            if (getGridType().equals(zXType.docBuilderGridType.dbgtStandardBO)) {
            	/**
            	 * We need to 'build' a grid
            	 */
            	if (pobjDocBuilder.getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
            		/**
            		 * Assume that table has 2 rows : first is the header row
            		 * and 2nd is the first data row
            		 * 
            		 * We basically split the table into the number of columns
            		 * that we need
            		 */
            		// TODO : 2
            		// Me.refObject.wordObject.Range.Cells.Split 1, intNumColumns
            		
            		/**
            		 * And set label in the header columns
            		 */
            		int intCol = intFixedCols;
                 int intRow = intFixedRows + 1;
                    
                 iter = colEntities.values().iterator();
                 while (iter.hasNext()) {
                    	objEntity = (DBEntity)iter.next();
                    	
                    	if (StringUtil.len(objEntity.getResolvedUseGroup()) > 0) {
                    		colAttr = objEntity.getBo().getDescriptor().getGroup(objEntity.getResolvedUseGroup());
                    		
                        	iterAttr = colAttr.iterator();
                        	while (iterAttr.hasNext()) {
                        		objAttr = (Attribute)iterAttr.next();
                        		
                        		intCol++;
                        		
                        		pobjDocBuilder.setCell(getRefobject(),
                        							   intRow, 
                        							   intCol, 
                        							   objAttr.getLabel().getLabel());
                        		
                        		/**
                        		 * Set alignment to centre as we are in header
                        		 */
                        		//TODO : 1
                        		// Me.refObject.wordObject.Cell(intRow, intCol).Range.ParagraphFormat.Alignment = wdAlignParagraphCenter
                        		
                        		/**
                        		 * And give nice colour
                        		 */
                        		// TODO : Background color ?
                        		// Me.refObject.wordObject.Cell(intRow, intCol).Shading.BackgroundPatternColor = wdColorGray35
                        		
                        		/**
                        		 * And best shot at width
                        		 */
                        		// TODO : Set table cell width.
                        		// Me.refObject.wordObject.Columns(intCol).PreferredWidthType = wdPreferredWidthPercent
                                // Me.refObject.wordObject.Columns(intCol).PreferredWidth = (objAttr.outputLength / intTotalLength) * 100
                        		
                        	}
                    		
                    	}
                    	
                    }
            		
            	} // Engine
            	
            } // grid type
            
            /**
             * In case of bespoke table, assume that developer has set allignment
             */
            boolean blnAllignmentSet = false;
            if (getGridType().equals(zXType.docBuilderGridType.dbgtBespoke)) {
            	blnAllignmentSet = true;
            }
            
            /**
             * Execute the query
             */
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
            	objRS = objDSHandler.boRS(objTheEntity.getBo(),
            												  objTheEntity.getResolvedLoadGroup(),
            												  strWhereClause,
            												  true,
            												  strOrderByClause,
            												  blnReverse,
            												  0, 0);
            	
            } else {
            	objRS = ((DSHRdbms)objDSHandler).sqlRS(strSQLQry);
            	
            }
            
            int intRow = intFixedRows + 1;
            int intCol;
            
            doneRS : while (!objRS.eof()) {
            	/**
            	 * Populate business objects
            	 */
            	iter = colEntities.values().iterator();
            	while (iter.hasNext()) {
            		objEntity = (DBEntity)iter.next();
            	}
            	
            	/**
            	 * And add to grid if needed
            	 */
            	if (pobjDocBuilder.isActive(getActive())) {
            		intRow++;
            		
            		/**
            		 * See if we are done..
            		 */
            		if (getMaxrows() > 0 && (intRow - intFixedRows) > getMaxrows()) {
            			break doneRS;
            		}
            		
            		if (intRow > 2 + intFixedRows) {
            			pobjDocBuilder.addRow(getRefobject());
            		}
            		
            		/**
            		 * Populate columns
            		 */
            		intCol = intFixedCols;
            		
            		iter = colEntities.values().iterator();
            		while (iter.hasNext()) {
            			objEntity = (DBEntity)iter.next();
            			
            			if (StringUtil.len(objEntity.getResolvedUseGroup()) > 0) {
            				colAttr = objEntity.getBo().getDescriptor().getGroup(objEntity.getResolvedUseGroup());
            				
            				iterAttr = colAttr.values().iterator();
            				while (iterAttr.hasNext()) {
            					objAttr = (Attribute)iterAttr.next();
            					
            					intCol = intCol + 1;
            					
            					/**
            					 * Set alignment if not been set before; each next
            					 * row will 'inherit' allignment from previous row
            					 * so no need to make things slower than needed
            					 */
            					if (!blnAllignmentSet) {
            						int intDataType = objAttr.getDataType().pos;
            						
            						if (intDataType == zXType.dataType.dtLong.pos
            						 	|| intDataType == zXType.dataType.dtDouble.pos) {
            							if (StringUtil.len(objAttr.getForeignKey()) == 0 
            								&& (objAttr.getOptions() == null || objAttr.getOptions().size() == 0)) {
            								// TODO : 1
            								// Me.refObject.wordObject.Cell(intRow, intCol).Range.ParagraphFormat.Alignment = wdAlignParagraphRight
            								
            							} else {
            								// TODO : 1
            								// Me.refObject.wordObject.Cell(intRow, intCol).Range.ParagraphFormat.Alignment = wdAlignParagraphLeft
            								
            							}
            							
            						} else {
            							// TODO : 1
            							//Me.refObject.wordObject.Cell(intRow, intCol).Range.ParagraphFormat.Alignment = wdAlignParagraphLeft
            							
            						}
            						
            						/**
            						 * And reset colour
            						 */
            						// Me.refObject.wordObject.Cell(intRow, intCol).Shading.BackgroundPatternColor = wdColorAutomatic
            						
            					}
            					
            					/**
            					 * In case of multi-line replace VbCrLf with VbCr because
            					 * Word does not seem to like this
            					 */
            					String strValue;
            					if (objAttr.isMultiLine()) {
            						// Replace$(objEntity.BO.GetAttr(objAttr.name).formattedValue, vbCrLf, vbCr)
            						strValue = objEntity.getBo().getValue(objAttr.getName()).formattedValue();
            					} else {
            						strValue = objEntity.getBo().getValue(objAttr.getName()).formattedValue();
            					}
            					
        						pobjDocBuilder.setCell(getRefobject(),
							   			   			   intRow,
							   			   			   intCol,
							   			   			   strValue);
            				}
            			}
            		}
            		
            	} // active
            	
            	blnAllignmentSet = true;
            	
            	objRS.moveNext();
            }
            
// doneRS:
            if (isShowNumrows()) {
            	strTmp = ((intRow - intFixedRows - 1)) 
            			 + " row" + (intRow - intFixedRows - 1 == 1?"":"s") + " displayed";
            	
            	if (!objRS.eof()) {
            		strTmp = strTmp + " (result-set truncated)";
            	}
            	
            	if (pobjDocBuilder.getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
            		// TODO : Add
            		
//                    With Me.refObject.wordObject
//                    .Rows.Add
//                    
//                    .Rows(.Rows.Count).Cells.Merge
//                    .Cell(.Rows.Count, 1).Range.Borders(Word.wdBorderBottom).Visible = False
//                    .Cell(.Rows.Count, 1).Range.Borders(Word.wdBorderLeft).Visible = False
//                    .Cell(.Rows.Count, 1).Range.Borders(Word.wdBorderRight).Visible = False
//                    .Cell(.Rows.Count, 1).Range.ParagraphFormat.Alignment = wdAlignParagraphRight
//                    .Cell(.Rows.Count, 1).Range.Font.Italic = True
//                    .Cell(.Rows.Count, 1).Range.Text = strTmp
//                    End With
            		
            	}
            	
            } // show numrows
            
            /**
             * Recalc if needed
             */
            if (getRefobject().isReCalc()) {
            	if (pobjDocBuilder.getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
            		// Me.refObject.wordObject.Range.fields.Update
            		OpenOfficeDocument.updateFields(((Document)pobjDocBuilder.getWordNewDoc()).wordDoc());
            		
            	}
            }
            
            objRS.RSClose();
            objRS = null;
            
// doneRS:
            
        	return go;
        } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process DBBO2Grid action.", e);
	        
	        if (objRS != null) {
	        	objRS.RSClose();
	        }
	        
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
		DBBO2Grid objDBAction = null;
		
		try {
			objDBAction = (DBBO2Grid)super.clone();
			
			objDBAction.setQueryname(getQueryname());
			
			if (getRefobject() != null) {
				objDBAction.setRefobject((DBObject)getRefobject().clone());
			}
			
			objDBAction.setFixedrows(getFixedrows());
			objDBAction.setFixedcols(getFixedcols());
			objDBAction.setMaxrows(getMaxrows());
			objDBAction.setShowNumrows(isShowNumrows());
			objDBAction.setGridType(getGridType());
			objDBAction.setActive(getActive());
			
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
		
		toString.append(DBBO2GRID_ACTIVE, getActive());
		toString.append(DBBO2GRID_FIXEDCOLS, getFixedcols());
		toString.append(DBBO2GRID_FIXEDROWS, getFixedrows());
		toString.append(DBBO2GRID_GRIDTYPE, zXType.valueOf(getGridType()));
		toString.append(DBBO2GRID_MAXROWS, getMaxrows());
		toString.append(DBBO2GRID_QUERYNAME, getQueryname());
		toString.append(DBBO2GRID_REFOBJECT, getRefobject());
		toString.append(DBBO2GRID_SHOWNUMROWS, isShowNumrows());
		
		return toString.toString();
	}
}