/*
 * Created on Jun 14, 2005
 * $Id: OpenOfficeDocument.java,v 1.1.2.26 2006/07/17 16:31:12 mike Exp $
 */
package org.zxframework.doc;

import java.io.File;
import java.util.Iterator;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.StringUtil;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;

import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.comp.helper.Bootstrap;

import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;

import com.sun.star.document.UpdateDocMode;
import com.sun.star.document.XDocumentInsertable;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XStorable;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;

import com.sun.star.table.BorderLine;
import com.sun.star.table.TableBorder;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;

import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextFieldsSupplier;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextRangeCompare;
import com.sun.star.text.XTextSectionsSupplier;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XNamingService;

import com.sun.star.util.XCloseable;
import com.sun.star.util.XRefreshable;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;
import com.sun.star.util.XSearchDescriptor;
import com.sun.star.util.XSearchable;

import com.sun.star.view.XPrintable;
import com.sun.star.view.XSelectionSupplier;

/**
 * OpenOffice Document Implementation.
 * 
 * TODO : Add a method to embed graphics in Word documents.
 * TODO : Organise this code abit.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @version 0.01
 */
public class OpenOfficeDocument extends AbstractDocument {
	
	//------------------------ Members
	
	private XComponentContext mxRemoteContext = null;
	private XMultiComponentFactory mxRemoteServiceManager = null;
	private XComponent mxDoc = null;
	
	//------------------------  Constants
	
	private static final int OO_WRITER = 0;
	private static final int OO_CALC = 1;
	private static final int OO_IMPRESS = 2;
	private static final int OO_DRAW = 3;
	private static final int OO_MATH = 4;
	private static final int OO_CHART = 5;
	
	private static final String OO_WRITER_EXT = "odt";
	private static final String OO_CALC_EXT = "ods";
	private static final String OO_IMPRESS_EXT = "odp";
	private static final String OO_DRAW_EXT = "odg";
	
	private static final String DOCTYPE_SCALC = "com.sun.star.sheet.SpreadsheetDocument";
	private static final String DOCTYPE_SWRITER = "com.sun.star.text.TextDocument";
	private static final String DOCTYPE_SDRAW = "com.sun.star.drawing.DrawingDocument";
	private static final String DOCTYPE_SMATH = "com.sun.star.formula.FormulaProperties";
	private static final String DOCTYPE_SIMPRESS = "com.sun.star.presentation.PresentationDocument";
	private static final String DOCTYPE_SCHART = "com.sun.star.chart.ChartDocument";
	
	//------------------------ Document Interface methods 
	
	/**
	 * @see org.zxframework.doc.Document#wordDoc()
	 */
	public Object wordDoc() {
		return mxDoc;
	}

	/**
	 * @see org.zxframework.doc.Document#wordApp()
	 */
	public Object wordApp() {
		return mxRemoteContext;
	}

	/**
	 * @see org.zxframework.doc.Document#activate()
	 */
	public zXType.rc activate() {
		zXType.rc activate = zXType.rc.rcOK;

		try {
			
			mxRemoteServiceManager = getRemoteServiceManager(null);
			
			return activate;
		} catch (Exception e) {
			throw new NestableRuntimeException(e);
			//activate = zXType.rc.rcError;
			//return activate;
		}
	}

	/**
	 * @see org.zxframework.doc.Document#newDoc(java.lang.String)
	 */
	public zXType.rc newDoc(String pstrDocURL) {
		zXType.rc newDoc = zXType.rc.rcOK;

		try {
			
			newDoc = openDoc(pstrDocURL, true, true, "_blank");

			return newDoc;
		} catch (Exception e) {
			getZx().trace.addError("Failed to create new document", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrDocURL = " + pstrDocURL);
            }
            
            newDoc = zXType.rc.rcError;
			return newDoc;
		}
	}

	/**
	 * @see org.zxframework.doc.Document#saveDocAs(java.lang.String)
	 */
	public zXType.rc saveDocAs(String pstrSaveAs) {
		zXType.rc saveDocAs = zXType.rc.rcOK;
		try {
			/**
			 * Build url for Openoffice
			 */
			pstrSaveAs = getOpenOfficeFilename(pstrSaveAs);
			
	        XStorable xStorable = QI.XStorable(mxDoc);
	        
	        PropertyValue[] propertyValue = new PropertyValue[ 2 ];
	        
	        /**
	         * Overwrite file
	         */
	        propertyValue[0] = new PropertyValue();
	        propertyValue[0].Name = "Overwrite";
	        propertyValue[0].Value = new Boolean(true);
	        
	        /**
	         * Auto detect which filter to use.
	         */
	        String strToExtension = getFileExtension(pstrSaveAs);
	        
	        String strFilterName = detectFilterName(getDocumentType(mxDoc), strToExtension);
	        
	        propertyValue[1] = new PropertyValue();
	        propertyValue[1].Name = "FilterName";
	        propertyValue[1].Value = strFilterName;
	        
	        xStorable.storeToURL(pstrSaveAs.toString(), propertyValue);
	        
	        return saveDocAs;
		} catch (Exception e) {
			getZx().trace.addError("Failed to save document", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrSaveAs = " + pstrSaveAs);
            }
            
			saveDocAs = zXType.rc.rcError;
			return saveDocAs;
		}
	}

	/**
	 * @see org.zxframework.doc.Document#giveUserFocus()
	 */
	public zXType.rc giveUserFocus() {
		return null;
	}

	/**
	 * @see org.zxframework.doc.Document#getTable(int, int, org.zxframework.zXType.wordSection)
	 */
	public Object getTable(int pintTable, 
						   int pintSection, 
						   zXType.wordSection penmArea) {
		Object getTable = null;
		pintTable = pintTable - 1;
		
		try {
			
			if (penmArea.equals(zXType.wordSection.wsFooter)) {
				// TODO : Openoffice, unsupported for now
				
			} else if (penmArea.equals(zXType.wordSection.wsHeader)) {
				// TODO : Openoffice, unsupported for now
				
			} else if (penmArea.equals(zXType.wordSection.wsPage)) {
				XTextTablesSupplier xTablesSupplier = QI.XTextTablesSupplier(mxDoc);
				
				/**
				 * Get the tables collection
				 */
				XNameAccess xNamedTables = xTablesSupplier.getTextTables();
				
				/**
				 * now query the XIndexAccess from the tables collection
				 */
				XIndexAccess xIndexedTables = QI.XIndexAccess(xNamedTables);
				
		        getTable = xIndexedTables.getByIndex(pintTable);
		        
			}
			
			return getTable;
		} catch (Exception e) {
			getZx().trace.addError("Failed to get table.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pintTable = " + pintTable);
                getZx().log.error("Parameter : pintSection = " + pintSection);
                getZx().log.error("Parameter : penmArea = " + penmArea);
            }
            
            getTable = zXType.rc.rcError;
			return getTable;
		}
	}
	
	/**
	 * @see Document#setTableCell(java.lang.Object, int, int, java.lang.String)
	 */
	public zXType.rc setTableCell(Object pobjTable, 
								  int pintRowNum, 
								  int pintColNum, 
								  String pstrValue) {
		zXType.rc setTableCell = zXType.rc.rcOK;
		
		pintRowNum = pintRowNum - 1;
		pintColNum = pintColNum - 1;
		
		try {
			
			XCell xCell = (XCell)getTableCell(pobjTable, pintRowNum, pintColNum);
            XText xCellText = QI.XText(xCell);
            xCellText.setString(pstrValue);
            
            return setTableCell;
		} catch (Exception e) {
			getZx().trace.addError("Failed to set table cell", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjTable = " + pobjTable);
                getZx().log.error("Parameter : pintRowNum = " + pintRowNum);
                getZx().log.error("Parameter : pintColNum = " + pintColNum);
                getZx().log.error("Parameter : pstrValue = " + pstrValue);
            }
            
            setTableCell = zXType.rc.rcError;
			return setTableCell;
		}
	}
	
	/**
	 * @see Document#populateTableCell(Object, int, int, String, String, String, String, String)
	 */
	public zXType.rc populateTableCell(Object pobjTable, int pintRowNum,
									   int pintColNum,
									   String pstrValue1,
									   
									   String pstrValue2,
									   String pstrValue3,
									   String pstrValue4,
									   String pstrValue5) {
		
		zXType.rc populateTableCell = zXType.rc.rcOK;
		
		pintRowNum = pintRowNum - 1;
		pintColNum = pintColNum - 1;
		
		try {
			String [] arrSearch = new String[]{"$1", "$2", "$3", "$4", "$5"};
			String[] arrReplace = new String[]{pstrValue1, pstrValue2, pstrValue3, pstrValue4, pstrValue5};
			
			XTextTable xTable = QI.XTextTable(pobjTable);
			
			XCellRange xCellRange = QI.XCellRange(xTable);
            XCell xCell = xCellRange.getCellByPosition(pintRowNum, pintColNum);
            
            XText xCellText = QI.XText(xCell);
            String strCellText = xCellText.getString();
            
			for (int i = 0; i < arrSearch.length; i++) {
				if (arrReplace[i] != null && arrSearch[i] != null) {
					/**
					 * Replace all words
					 */
					strCellText = StringUtil.replaceAll(strCellText, arrSearch[i], arrReplace[i]);
				}
			}
			
			xCellText.setString(strCellText);
			
			return populateTableCell;
		} catch (Exception e) {
			getZx().trace.addError("Failed to populate cells", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjTable = " + pobjTable);
                getZx().log.error("Parameter : pintRowNum = " + pintRowNum);
                getZx().log.error("Parameter : pintColNum = " + pintColNum);
                getZx().log.error("Parameter : pstrValue1 = " + pstrValue1);
                getZx().log.error("Parameter : pstrValue2 = " + pstrValue2);
                getZx().log.error("Parameter : pstrValue4 = " + pstrValue4);
                getZx().log.error("Parameter : pstrValue5 = " + pstrValue5);
            }
            
            populateTableCell = zXType.rc.rcError;
			return populateTableCell;
		}
	}
	
	/**
	 * @see Document#openDoc(java.lang.String)
	 */
	public zXType.rc openDoc(String pstrDocURL) {
		zXType.rc openDoc = zXType.rc.rcOK;

		try {
			
			openDoc = openDoc(pstrDocURL, false, false, "_blank");
			
			return openDoc;

		} catch (Exception e) {
			getZx().trace.addError("Failed to open document", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrDocURL = " + pstrDocURL);
            }
            
            openDoc = zXType.rc.rcError;
			return openDoc;
		}
	}
	
	/**
	 * @see org.zxframework.doc.Document#setPrinter(java.lang.String)
	 */
	public zXType.rc setPrinter(String pstrPrinter) {
		zXType.rc setPrinter = zXType.rc.rcOK;
		try {
	        /**
	         * Querying for the interface XPrintable on the loaded document
	         */
	        XPrintable xPrintable = QI.XPrintable(mxDoc);
	        
	        /**
	         * Setting the property "Name" for the favoured printer (name of IP address)
	         */
	        PropertyValue propertyValue[] = new PropertyValue[1];
	        propertyValue[0] = new PropertyValue();
	        propertyValue[0].Name = "Name";
	        propertyValue[0].Value = pstrPrinter;
	        
	        /**
	         * Setting the name of the printer
	         */
	        xPrintable.setPrinter(propertyValue);
	        
	        return setPrinter;
		} catch (Exception e) {
			getZx().trace.addError("Failed to set printer", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrPrinter = " + pstrPrinter);
            }
            
            setPrinter = zXType.rc.rcError;
			return setPrinter;
		}
        
	}

	/**
	 * @see org.zxframework.doc.Document#closeDoc(boolean)
	 */
	public zXType.rc closeDoc(boolean pblnSaveChanges) {
		zXType.rc closeDoc = zXType.rc.rcOK;
		
		try {
	        XCloseable xCloseable = QI.XCloseable(mxDoc);
	        
		    if (xCloseable != null ) {
		        xCloseable.close(false);
		        
		    } else {
		        XComponent xComp = QI.XComponent(mxDoc);
		        xComp.dispose();
		    }
		    
		    return closeDoc;
		} catch (Exception e) {
			getZx().trace.addError("Failed to close document", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pblnSaveChanges = " + pblnSaveChanges);
            }
            
            closeDoc = zXType.rc.rcError;
			return closeDoc;
		}
	}

	/**
	 * @see org.zxframework.doc.Document#addTableRow(java.lang.Object)
	 */
	public zXType.rc addTableRow(Object pobjTable) {
		zXType.rc addTableRow = zXType.rc.rcOK;
		try {
			XTextTable xTable = QI.XTextTable(pobjTable);
			
			/**
			 * Get the index of the last row.
			 */
			int intCount = Math.abs(xTable.getRows().getCount());
			xTable.getRows().insertByIndex(intCount, 1);
			
			return addTableRow;
		} catch (Exception e) {
			getZx().trace.addError("Failed to add row to table", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : addTableRow = " + addTableRow);
            }
            
            addTableRow = zXType.rc.rcError;
			return addTableRow;
		}
	}
	
	//------------------------ AbstractDocument abstract methods.
	
	/**
	 * @see AbstractDocument#boTableInit(java.lang.Object, 
	 * 									 org.zxframework.ZXBO[], 
	 * 									 java.lang.String[], boolean)
	 */
	public zXType.rc boTableInit(Object pobjTable, 
								 ZXBO[] parrBO, 
								 String[] parrGroup, 
								 boolean pblnBorders) {
		zXType.rc boTableInit = zXType.rc.rcOK;
		
		try {
			/**
			 * First get the total number of columns that we have to display
			 */
			int intColumns = getZx().getBos().countAttr(parrBO, parrGroup);
			if (intColumns == -1) {
				throw new RuntimeException("Unable to count attributes");
			}
			
			int intRows = 2;
			
			XTextTable xOldTable = QI.XTextTable(pobjTable);
			
	        /**
	         * create instance of a text table
	         */
	        XMultiServiceFactory xDocMSF = QI.XMultiServiceFactory(mxDoc);
	        XTextTable xNewTable = QI.XTextTable(xDocMSF.createInstance("com.sun.star.text.TextTable"));
	        xNewTable.initialize(intRows, intColumns);
	        
			replaceTable(xOldTable, xNewTable);
			
			/**
			 * FORMATTING : Set some details especially for the header row and table as a whole
			 */
			XCellRange xCellRange = QI.XCellRange(xNewTable);
			XPropertySet xTableProps = QI.XPropertySet(xCellRange);
			BorderLine theLine = new BorderLine();
			theLine.Color = 0x000099;
			theLine.OuterLineWidth = 10;
			/**
			 * Create a border.
			 */
			TableBorder bord = new TableBorder();
			bord.VerticalLine = bord.HorizontalLine =
			bord.LeftLine = bord.RightLine =
			bord.TopLine = bord.BottomLine =
			theLine;
			xTableProps.setPropertyValue("TableBorder", bord);
			bord.IsVerticalLineValid = bord.IsHorizontalLineValid =
			bord.IsLeftLineValid = bord.IsRightLineValid =
			bord.IsTopLineValid = bord.IsBottomLineValid = pblnBorders;
			xTableProps.setPropertyValue("TableBorder", bord);
			/**
			 * Style the columns.
			 */
            XIndexAccess xTTRows = xNewTable.getRows();
            XPropertySet xTTRowPS = QI.XPropertySet(xTTRows.getByIndex(0));
	        XPropertySet xTTPS = QI.XPropertySet(xNewTable);
            xTTPS.setPropertyValue("BackTransparent", new Boolean(false));
            xTTPS.setPropertyValue("BackColor",new Integer(13421823));
            xTTRowPS.setPropertyValue("BackTransparent", new Boolean(false));
            xTTRowPS.setPropertyValue("BackColor",new Integer(6710932));
	        
            /**
             * POPULATE : Populate the header row and set the width
             */
            ZXBO objBO;
            String strGroup;
            AttributeCollection colGroup;
            Iterator iter;
            Attribute objAttr;
            int intColNo = 1;
            for (int i = 0; i < parrBO.length; i++) {
				objBO = parrBO[i];
				strGroup = parrGroup[i];
				
				if (StringUtil.len(strGroup) > 0) {
					colGroup = objBO.getDescriptor().getGroup(strGroup);
					iter = colGroup.iterator();
					while (iter.hasNext()) {
						objAttr = (Attribute)iter.next();
						setTableCell(xNewTable, 1, intColNo, objAttr.getLabel().getLabel());
						intColNo++;
					}
				}
			}
            
            return boTableInit;
		} catch (Exception e) {
			if (!(e instanceof ZXException)) {
	        	getZx().trace.addError("Failed to build table", e);
			}
			boTableInit = zXType.rc.rcError;
			return boTableInit;
		}
	}
	
	/**
	 * @see AbstractDocument#boTableRowPopulate(java.lang.Object, 
	 * 									 org.zxframework.ZXBO[], 
	 * 									 java.lang.String[])
	 */
	public zXType.rc boTableRowPopulate(Object pobjTable, 
								 		ZXBO[] parrBO, 
								 		String[] parrGroup) {
		zXType.rc boTableRowPopulate = zXType.rc.rcOK;
		
		try {
			XCellRange xCellRange = (XCellRange)pobjTable;
			
			String strGroup;
			ZXBO objBO;
			AttributeCollection colGroup;
			Attribute objAttr;
			Iterator iter;
			int intColNum = 0;
			
			for (int i = 0; i < parrBO.length; i++) {
				objBO = parrBO[i];
				strGroup = parrGroup[i];
				
				if (StringUtil.len(strGroup) > 0) {
					colGroup = objBO.getDescriptor().getGroup(strGroup);
					
					iter = colGroup.iterator();
					while(iter.hasNext()) {
						objAttr = (Attribute)iter.next();
						
			            XCell xCell = xCellRange.getCellByPosition(intColNum, 0);
			            XText xCellText = QI.XText(xCell);
			            
			            xCellText.setString(objBO.getValue(objAttr.getName()).formattedValue());
			            
						intColNum++;
					}
				}
			}
			
            return boTableRowPopulate;
		} catch (Exception e) {
			if (!(e instanceof ZXException)) {
	        	getZx().trace.addError("Failed to build table", e);
			}
			boTableRowPopulate = zXType.rc.rcError;
			return boTableRowPopulate;
		}
	}
	
	//------------------------ Openoffice Helper methods

	/**
	 * TODO : Openoffice, this should be within the defined range.
	 * 
	 * @param pobjRange The text range to look in. 
	 * @param pintTable The table number.
	 * @return Returns the text table.
	 */
	public Object getTable(Object pobjRange,
						   int pintTable) {
		Object getTable = null;
		
		pintTable = pintTable - 1;
		
		try {
			
			XTextTablesSupplier xTablesSupplier = QI.XTextTablesSupplier(mxDoc);
			
			/**
			 * Get the tables collection
			 */
			XNameAccess xNamedTables = xTablesSupplier.getTextTables();
			
			/**
			 * now query the XIndexAccess from the tables collection
			 */
			XIndexAccess xIndexedTables = QI.XIndexAccess(xNamedTables);
			
	        getTable = xIndexedTables.getByIndex(pintTable);
	        
			return getTable;
		} catch (Exception e) {
			getZx().trace.addError("Failed to get table.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRange = " + pobjRange);
                getZx().log.error("Parameter : pintTable = " + pintTable);
            }
            
            getTable = zXType.rc.rcError;
			return getTable;
		}
	}
	
	/**
	 * @param pobjRange The range to look in.
	 * @return Returns the number of textables in range.
	 */
	public int getTableCount(Object pobjRange) {
		int getTableCount = 0;
		try {
			
			XTextTablesSupplier xTablesSupplier = QI.XTextTablesSupplier(mxDoc);
			
			/**
			 * Get the tables collection
			 */
			XNameAccess xNamedTables = xTablesSupplier.getTextTables();
			if (xNamedTables != null) {
				XIndexAccess xIndexedTables = QI.XIndexAccess(xNamedTables);
				if (xIndexedTables != null) {
					getTableCount = xIndexedTables.getCount();
				}
			}
			return getTableCount;
			
		} catch (Exception e) {
			getZx().trace.addError("Failed to get table.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRange = " + pobjRange);
            }
			return getTableCount;
		}
	}
	
	/**
	 * Do a search and replace on an entire document.
	 * 
	 * @param pstrSearch The textto search.
	 * @param pstrReplace The replace text
	 * @return Returns the return code of the method.
	 */
	public zXType.rc replaceAll(String pstrSearch, String pstrReplace) {
		zXType.rc replaceAll = zXType.rc.rcOK;
		
		try {
			/**
			 * Should be pobjTable not mxDoc
			 */
			XReplaceable xReplaceable = QI.XReplaceable(mxDoc);
			
			/**
			 * You need a descriptor to set properies for Replace
			 */
			XReplaceDescriptor xReplaceDescr = xReplaceable.createReplaceDescriptor();

			/**
			 * Set the properties the replace method need
			 */
			xReplaceDescr.setSearchString(pstrSearch);
			xReplaceDescr.setReplaceString(pstrReplace);

			/**
			 * Replace all words
			 */
			xReplaceable.replaceAll(xReplaceDescr);
			
			return replaceAll;
		} catch (Exception e) {
			getZx().trace.addError("Failed to perform find and replace", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrSearch = " + pstrSearch);
                getZx().log.error("Parameter : pstrReplace = " + pstrReplace);
            }
            
            replaceAll = zXType.rc.rcError;
			return replaceAll;
		}
	}
	
	/**
	 * Do a search and replace in a table.
	 * 
	 * @param pobjTable The table you want to search in.
	 * @param pstrSearch The textto search.
	 * @param pstrReplace The replace text
	 * @return Returns the return code of the method.
	 */
	public zXType.rc replaceRange(Object pobjTable, String pstrSearch, String pstrReplace) {
		zXType.rc replaceRange = zXType.rc.rcOK;
		
		try {
			/**
			 * Move the view cursor to where the table is.
			 */
			XTextDocument xTextDoc = QI.XTextDocument(mxDoc);
			XText xText = xTextDoc.getText();
			XController xController = xTextDoc.getCurrentController();
			XTextViewCursorSupplier xViewCursorSupplier = QI.XTextViewCursorSupplier(xController);
	        XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();
	        xViewCursor.gotoStart(false);
	        XSelectionSupplier xSelectionSupplier = QI.XSelectionSupplier(xController);
	        xSelectionSupplier.select(pobjTable); 
	        
	        // Should be pobjTable not mxDoc
			XReplaceable xReplaceable = QI.XReplaceable(mxDoc);
			XSearchable xSearchable = QI.XSearchable(mxDoc);
			XTextRangeCompare xTextRangeCompare = QI.XTextRangeCompare(xText);
			
			/**
			 * You need a descriptor to set properies for Replace
			 */
			XReplaceDescriptor xReplaceDescr = xReplaceable.createReplaceDescriptor();
			
			XTextCursor oFind = xText.createTextCursorByRange(xViewCursor.getStart());
			XTextCursor oEndTC = xText.createTextCursorByRange(xViewCursor.getEnd()); 
			
			oFind = (XTextCursor)xSearchable.findNext(oFind.getEnd(), xReplaceDescr);
			while (oFind != null && xTextRangeCompare.compareRegionEnds(oFind, oEndTC) > 0) {
				oFind.setString(pstrReplace);
				oFind = (XTextCursor)xSearchable.findNext(oFind.getEnd(), xReplaceDescr);
			}
			
			return replaceRange;
		} catch (Exception e) {
			getZx().trace.addError("Failed to perform find and replace", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjTable = " + pobjTable);
                getZx().log.error("Parameter : pstrSearch = " + pstrSearch);
                getZx().log.error("Parameter : pstrReplace = " + pstrReplace);
            }
            
            replaceRange = zXType.rc.rcError;
			return replaceRange;
		}
	}
	
	//------------------------ Private internal methods
	
	private XMultiComponentFactory getRemoteServiceManager(String unoUrl) throws java.lang.Exception {
		if (mxRemoteContext == null) {
			if (unoUrl != null) {
				/**
				 * First step: create local component context, get local
				 * servicemanager and ask it to create a UnoUrlResolver object with an
				 * XUnoUrlResolver interface
				 */
				XComponentContext xLocalContext = Bootstrap.createInitialComponentContext(null);

				XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager();

				Object urlResolver = xLocalServiceManager.createInstanceWithContext("com.sun.star.bridge.UnoUrlResolver", xLocalContext);

				/**
				 * query XUnoUrlResolver interface from urlResolver object
				 */
				XUnoUrlResolver xUnoUrlResolver = QI.XUnoUrlResolver(urlResolver);

				/**
				 * Second step: use xUrlResolver interface to import the remote
				 * StarOffice.ServiceManager, retrieve its property
				 * DefaultContext and get the remote servicemanager
				 */
				Object initialObject = xUnoUrlResolver.resolve(unoUrl);
				XPropertySet xPropertySet = QI.XPropertySet(initialObject);
				Object context = xPropertySet.getPropertyValue("DefaultContext");
				mxRemoteContext = QI.XComponentContext(context);
				
			} else {
				/**
				 * Local only
				 */
				mxRemoteContext = Bootstrap.bootstrap();
			}
		}
		
		return mxRemoteContext.getServiceManager();
	}
	
	private zXType.rc openDoc(String pstrDocURL, 
							  boolean pblnTemplate, 
							  boolean pblnHideDoc, 
							  String pstrFrame) {
		zXType.rc openDoc = zXType.rc.rcOK;

		try {
			// get the remote service manager
			mxRemoteServiceManager = this.getRemoteServiceManager(null);
			
			Object oDesktop = mxRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop", mxRemoteContext);
			XComponentLoader xCompLoader = QI.XComponentLoader(oDesktop);
			
			/**
			 * Build url for Openoffice
			 */
			if (StringUtil.len(pstrDocURL) == 0) {
				/**
				 * Create new doc if null.
				 */
				pstrDocURL = "private:factory/swriter";
				
			} else {
				pstrDocURL = getOpenOfficeFilename(pstrDocURL);
				
			}
			
			/**
			 * Show document when in debug mode.
			 */
			if (getZx().getRunMode().pos == zXType.runMode.rmTest.pos) {
				pblnHideDoc = false;
			}
			
			PropertyValue[] loadProps = new PropertyValue[3];
			
			/**
			 * Whether to show to doc or not.
			 */
			loadProps[0] = new com.sun.star.beans.PropertyValue();
			loadProps[0].Name = "Hidden";
			loadProps[0].Value = new Boolean(pblnHideDoc);
			
			/**
			 * Update document if it does not require a dialog. 
			 * Otherwise do not update. For example a link to a 
			 * database can require a dialog to get password for an update.
			 */
			loadProps[1] = new com.sun.star.beans.PropertyValue();
			loadProps[1].Name = "UpdateDocMode";
			loadProps[1].Value = new Short(UpdateDocMode.QUIET_UPDATE);
			
			/**
			 * Define load properties according to
			 * com.sun.star.document.MediaDescriptor the boolean property
			 * AsTemplate tells the office to create a new document from the
			 * given file
			 */
			loadProps[2] = new PropertyValue();
			loadProps[2].Name = "AsTemplate";
			loadProps[2].Value = new Boolean(pblnTemplate);
			
			/**
			 * Load a Writer document in the current open window
			 */
			mxDoc = xCompLoader.loadComponentFromURL(pstrDocURL, pstrFrame, 0, loadProps);
			
			if (mxDoc != null) {
				openDoc = zXType.rc.rcOK;
			} else {
				openDoc = zXType.rc.rcError;
			}
			
			return openDoc;
		} catch (Exception e) {
			getZx().trace.addError("Failed to open document", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrDocURL = " + pstrDocURL);
            }
            
            openDoc = zXType.rc.rcError;
			return openDoc;
		}
	}
	
	/**
	 * Get the file extension for a file.
	 * 
	 * @param pstrFileName The file you want to extension of.
	 * @return The file extension. Defaults to odt.
	 */
	public static String getFileExtension(String pstrFileName) {
		String getFileExtension = OO_WRITER_EXT;
		
		int intFileName = StringUtil.len(pstrFileName);
		if (intFileName > 4) {
			int intIndexOf = pstrFileName.indexOf(".", intFileName - 5);
			if (intIndexOf != -1) {
				getFileExtension = pstrFileName.toLowerCase().substring(intIndexOf + 1, intFileName);
			}
		}
		
		return getFileExtension;
	}
	
	/**
	 * @param pintFromType
	 * @param pstrToExtension
	 * @return Returns the required filter
	 */
	public static String detectFilterName(int pintFromType, String pstrToExtension) {
		String detectFilterName = "writer8";
		
		if (StringUtil.len(pstrToExtension) > 0) {
			
			if (pstrToExtension.equalsIgnoreCase("pdf")) {
				detectFilterName = getPDFFilter(pintFromType);
				
			} else if (pstrToExtension.equalsIgnoreCase("html") || pstrToExtension.equalsIgnoreCase("htm")) {
				switch (pintFromType) {
					case OO_WRITER:
						detectFilterName = "HTML (StarWriter)";
						break;
					case OO_CALC:
						detectFilterName = "HTML (StarCalc)";
						break;
					case OO_IMPRESS : case OO_DRAW :
						detectFilterName = "impress_html_Export";
				}
				
			/**
			 * Test documents only.
			 */
			} else if (pstrToExtension.equals("doc")) {
				detectFilterName = "MS Word 97";
			} else if (pstrToExtension.equals("rtf")) {
				detectFilterName = "Rich Text Format";
			} else if (pstrToExtension.equals("txt")) {
				detectFilterName = "Text (encoded)";
				
			/**
			 * Spreedsheets only.
			 */
			} else if (pstrToExtension.equals("csv")) {
				// Different for xls and doc.
				detectFilterName = "Text - txt - csv (StarCalc)";
			} else if (pstrToExtension.equals("xls")) {
				//detectFilterName = "MS Excel 2003 XML";
				detectFilterName = "MS Excel 97 Vorlage/Template";
				
			/**
			 * Native openoffice documents.
			 */
			} else if (pstrToExtension.equals(OO_WRITER_EXT)) {
				detectFilterName = "writer8";
			} else if (pstrToExtension.equals(OO_CALC_EXT)) {
				detectFilterName = "calc8";
			} else if (pstrToExtension.equals(OO_DRAW_EXT)) {
				detectFilterName = "draw8";
			} else if (pstrToExtension.equals(OO_IMPRESS_EXT)) {
				detectFilterName = "impress8";
				
			} else {
				/**
				 * Other.. jpg etc..
				 */
				switch (pintFromType) {
					case OO_WRITER:
						throw new NestableRuntimeException("Unsupported file type");
						
					case OO_CALC:
						throw new NestableRuntimeException("Unsupported file type");
						
					case OO_IMPRESS : case OO_DRAW: case OO_CHART:
						if (pstrToExtension.equals("jpg")) {
							detectFilterName = "impress_jpg_Export";
							
						} else if (pstrToExtension.equals("png")){
							detectFilterName = "impress_png_Export";
							
						} else if (pstrToExtension.equals("gif")){
							detectFilterName = "impress_gif_Export";
							
						} else if (pstrToExtension.equals("swf")) {
							detectFilterName = "impress_flash_Export";
							
						} else {
							throw new NestableRuntimeException("Unsupported file type");
						}
						
						break;
				}
				
			}
		}
		
		return detectFilterName;
	}
	
	/**
	 * @param oDoc The currently open document.
	 * @return Returns the documentType.
	 * @throws Exception
	 */
	public static int getDocumentType(XComponent oDoc) throws Exception {
		int getDocumentType = OO_WRITER;
		
		XServiceInfo xServiceInfo = QI.XServiceInfo(oDoc);
		
		  if (xServiceInfo.supportsService(DOCTYPE_SCALC)) {
		  	getDocumentType = OO_CALC;
		  } else if (xServiceInfo.supportsService(DOCTYPE_SWRITER)) {
		  	getDocumentType = OO_WRITER;
		  } else if (xServiceInfo.supportsService(DOCTYPE_SDRAW)) {
		  	getDocumentType = OO_DRAW;
		  } else if (xServiceInfo.supportsService(DOCTYPE_SMATH)) {
		  	getDocumentType = OO_MATH;
		  } else if (xServiceInfo.supportsService(DOCTYPE_SIMPRESS)) {
			  	getDocumentType = OO_IMPRESS;
		  } else if (xServiceInfo.supportsService(DOCTYPE_SCHART)) {
			  	getDocumentType = OO_CHART;
		  }
		  
		return getDocumentType;
	}
	
	/**
	 * @param pstrExtention File extension.
	 * @return Returns the documentType.
	 */
	public static int getDocumentType(String pstrExtention) {
		int getDocumentType = OO_WRITER;
		
		if (StringUtil.equalsAnyOf(StringUtil.split(",", "sxw,stw,sdw,odt,ott,oth,odm,doc"), pstrExtention)) {
			getDocumentType = OO_WRITER;
			
		} else if (StringUtil.equalsAnyOf(StringUtil.split(",", "sxc,stc,sdc,ods,ots,xls"), pstrExtention)) {
			getDocumentType = OO_CALC;
			
		} else if (StringUtil.equalsAnyOf(StringUtil.split(",", "sxi,sti,sdd,sdp,odp,otp,ppt"), pstrExtention)) {
			getDocumentType = OO_IMPRESS;
			
		} else if (StringUtil.equalsAnyOf(StringUtil.split(",", "odg"), pstrExtention)) {
			getDocumentType = OO_DRAW;
			
		}
		
		return getDocumentType;
	}
	
	/**
	 * @param documentType The openoffice doc type.
	 * @return Returns the correct pdf filter.
	 */
	public static String getPDFFilter(int documentType) {
		String getPDFFilter = "writer_pdf_Export";
		
		switch (documentType) {
		case OO_CALC:
			getPDFFilter = "calc_pdf_Export";
			break;
		case OO_DRAW:
			getPDFFilter = "draw_pdf_Export";
			break;
		case OO_IMPRESS:
			getPDFFilter = "impress_pdf_Export";
			break;
		case OO_MATH:
			getPDFFilter = "math_pdf_Export";
			break;
		case OO_WRITER:
			getPDFFilter = "writer_pdf_Export";
			break;
		}
		
		return getPDFFilter;
	}
	
	/**
	 * @param xTextDoc The currently open document.
	 * @param xSelect The object to select.
	 * @return Returns the View cursor for the selected object.
	 * @throws Exception
	 */
	public static XTextViewCursor selectObject(XTextDocument xTextDoc, Object xSelect) throws Exception {
		XController xController = xTextDoc.getCurrentController();
		XTextViewCursorSupplier xViewCursorSupplier = QI.XTextViewCursorSupplier(xController);
		
        XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();
        xViewCursor.gotoStart(false);
        
        XSelectionSupplier xSelectionSupplier = QI.XSelectionSupplier(xController);
        xSelectionSupplier.select(xSelect);
        
        return xViewCursor;
	}
	
	/**
	 * Replace this table with a new table.
	 * 
	 * @param pobjOldTable The placeholder table you want to replace.
	 * @param pobjNewTable The new table
	 */
	private void replaceTable(XTextTable pobjOldTable, XTextTable pobjNewTable) {
		try {
	        /**
	         * Get the XTextViewCursor and position to where pobjOldTable is
	         */
			XTextDocument xTextDoc = QI.XTextDocument(mxDoc);
			
	        XTextViewCursor xViewCursor = selectObject(xTextDoc, pobjOldTable);
	        
	        /**
	         * Delete the old table
	         */
	        pobjOldTable.dispose();
	        
	        /**
	         * Insert the new table.
	         */
			XText xText = xTextDoc.getText();
			xText.insertTextContent(xViewCursor, pobjNewTable, false);
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to replace table.", e);
		}
	}
	
	/**
	 * Get a section by number from a document.
	 * 
	 * @param pintSection The position of the text section.
	 * @return Returns the text section.
	 */
	public Object getSection(int pintSection) {
		Object getSection = null;
		// Index correction
		pintSection = pintSection - 1;
		
		try {
			XTextDocument xTextDoc = QI.XTextDocument(mxDoc);
			XTextSectionsSupplier xTextSupplier = QI.XTextSectionsSupplier(xTextDoc);
			
			/**
			 * Get the tables collection
			 */
			XNameAccess xNamedSection = xTextSupplier.getTextSections();
			
			/**
			 * now query the XIndexAccess from the tables collection
			 */
			XIndexAccess xIndexedTables = QI.XIndexAccess(xNamedSection);
			
			int count = xIndexedTables.getCount();
			if (count != 0 && count >= pintSection) {
				getSection = xIndexedTables.getByIndex(pintSection);
			}
			
	        return getSection;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get a paragraph by number.
	 * 
	 * @param pintParagraph The position of the text section.
	 * @return Returns the text section.
	 */
	public Object getParagraph(int pintParagraph) {
		Object getParagraph = null;
		try {
			XTextDocument xTextDoc = QI.XTextDocument(mxDoc);
			XText xText = xTextDoc.getText();
			
			/**
			 * The service 'com.sun.star.text.Text' supports the XEnumerationAccess interface to
			 * provide an enumeration
			 * of the paragraphs contained by the text the service refers to.
			 * Here, we access this interface
			 */
			XEnumerationAccess xParaAccess = QI.XEnumerationAccess(xText);
			
			/**
			 * Call the XEnumerationAccess's only method to access the actual Enumeration
			 */
			XEnumeration xParaEnum = xParaAccess.createEnumeration();
			
			int i = 0;
			
			/**
			 * While there are paragraphs, do things to them
			 */
			while (xParaEnum.hasMoreElements()) {
				/**
				 * Get a reference to the next paragraphs XServiceInfo interface. TextTables
				 * are also part of this
				 * enumeration access, so we ask the element if it is a TextTable, if it
				 * doesn't support the
				 * com.sun.star.text.TextTable service, then it is safe to assume that it
				 * really is a paragraph
				 */
				Object objElement = xParaEnum.nextElement();
				
				XServiceInfo xInfo = QI.XServiceInfo(objElement);
				
				if (!xInfo.supportsService("com.sun.star.text.TextTable")) {
					i++;
					
					if (i == pintParagraph) {
						getParagraph = objElement;
						break;
					}
				}
				
			}
			
			return getParagraph;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This method sets the text colour of the cell refered to by sCellName to
	 * white and inserts the string sText in it
	 * 
	 * @param xTable A handle to the table to set.
	 * @param pstrCellName The cell name (A1)
	 * @param pstrText The value to set the cell to.
	 */
	protected static void setTableCell(XTextTable xTable, 
									   String pstrCellName, 
									   String pstrText) {
		/**
		 * Access the XText interface of the cell referred to by sCellName
		 */
		XText xCellText = QI.XText(xTable.getCellByName(pstrCellName));
		
		/**
		 * Set the text in the cell to pstrText
		 */
		xCellText.setString(pstrText);
		
	}
	
	/**
	 * Get a cell in a table.
	 * 
	 * @param pobjTable The table with the containing cell.
	 * @param pintRowNum The row number
	 * @param pintColNum The column number.
	 * @return Returns table cell.
	 */
	public static Object getTableCell(Object pobjTable, 
									  int pintRowNum, 
									  int pintColNum) {
		Object getTableCell = null;
		try {
			
			XTextTable xTable = QI.XTextTable(pobjTable);
			
			XCellRange xCellRange = QI.XCellRange(xTable);
            XCell xCell = xCellRange.getCellByPosition(pintColNum, pintRowNum);
            
            getTableCell = xCell;
            
	        return getTableCell;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Inserting a document into a XText.
	 * 
	 * @param pobjText The text object handle.
	 * @param pstrFileName The file to insert.
	 * @return Returns the return code of the method.
	 */
	public static zXType.rc insertFile(Object pobjText,
									   String pstrFileName) {
		zXType.rc insertFile = zXType.rc.rcOK;
		
		try {
			/**
			 * Build url for Openoffice
			 */
			pstrFileName = getOpenOfficeFilename(pstrFileName);
            
        	XText xText = ((XText)pobjText);
	        try {
	            /**
	             * Inserting the content
	             */
	        	XTextCursor xTextCursor = xText.createTextCursor();
	        	XDocumentInsertable xDocInsertable = QI.XDocumentInsertable(xTextCursor);
	        	PropertyValue[] arrProp = new PropertyValue[0];
	        	xDocInsertable.insertDocumentFromURL(pstrFileName, arrProp);
	        	
	        } catch (Exception e) {
	        	throw new NestableRuntimeException("Could not insert Content",e);
	        }
	        
	        return insertFile;
	        
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Inserting a document into a XText.
	 * 
	 * TODO : Still need to object this.
	 * 
	 * @param pobjDoc Text Document 
	 * @param pobjText The text object handle.
	 * @param pstrFileName The file to insert.
	 * @return Returns the return code of the method.
	 */
	public static zXType.rc insertOLEObject(Object pobjDoc, Object pobjText, String pstrFileName) {
		zXType.rc insertFile = zXType.rc.rcOK;
		return insertFile;
	}
	
	/**
	 * Insert a graphic into a Text Document.
	 * 
	 * @param pobjDoc The document to insert the graphic into.
	 * @param pobjText The text object handle.
	 * @param pstrFileName The file to insert.
	 * @param pblnBackGroundImage Whether to resize. Optional, default should be false.
	 * @return Returns the return code of the method.
	 */
	public static zXType.rc insertGraphic(Object pobjDoc,
										  Object pobjText, 
										  String pstrFileName,
										  boolean pblnBackGroundImage) {
		zXType.rc insertGraphic = zXType.rc.rcOK;
		
		try {
			/**
			 * Build url for Openoffice
			 */
            pstrFileName = getOpenOfficeFilename(pstrFileName);
            
            /**
             * Creating a string for the graphic url
             */
	        if (pblnBackGroundImage) {
	        	XText xText = ((XText)pobjText);
	        	XPropertySet xPropSet = QI.XPropertySet(xText);
	        	xPropSet.setPropertyValue("BackGraphicURL", pstrFileName);
	        	xPropSet.setPropertyValue("BackGraphicLocation", com.sun.star.style.GraphicLocation.AREA);
	        	
	        } else {
				/**
				 * Querying for the interface XTextDocument on the xcomponent
				 */
	            XTextDocument xTextDoc = QI.XTextDocument(pobjDoc);
	            
	            /**
	             * Querying for the interface XMultiServiceFactory on the xtextdocument
	             */
	            XMultiServiceFactory xMSFDoc = QI.XMultiServiceFactory(xTextDoc);
	            
		        Object oGraphic = null;
		        
		        try {
		            /**
		             * Creating the service GraphicObject
		             */
		            oGraphic = xMSFDoc.createInstance("com.sun.star.text.GraphicObject");
		            
		        } catch (Exception e) {
		            throw new ZXException("Could not create instance", e);
		        }
		    	
		        /**
		         * Querying for the interface XTextContent on the GraphicObject
		         */
		        XTextContent xTextContent = QI.XTextContent(oGraphic);
	        	XText xText = ((XText)pobjText);
		        try {
		            /**
		             * Inserting the content
		             */
		        	XTextCursor xTextCursor = xText.createTextCursor();
		        	((XText)pobjText).insertTextContent(xTextCursor, xTextContent, false);
		        	
		        } catch (Exception e) {
		        	throw new NestableRuntimeException("Could not insert Content",e);
		        }
		        
		        /**
		         * Querying for the interface XPropertySet on GraphicObject
		         */
		        XPropertySet xPropSet = QI.XPropertySet(oGraphic);
		        try {
		            /**
		             * Setting the graphic url
		             */
		            xPropSet.setPropertyValue("GraphicURL", pstrFileName);
		            
		        } catch (Exception e) {
		            throw new NestableRuntimeException("Couldn't set property 'GraphicURL'", e);
		        }
	        }
	        
	        return insertGraphic;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @return Returns the text range for a document.
	 */
	public Object getTextRange() {
		return getTextRange(mxDoc);
	}	
	
	/**
	 * @param xDoc A handle to the open document.
	 * @return Returns the text range for a document.
	 */
	public static Object getTextRange(Object xDoc) {
		XTextRange xTextRange = QI.XTextRange(xDoc);
		return xTextRange;
	}
	
	/**
	 * @return Returns a XMultiServiceFactory.
	 * @throws com.sun.star.uno.Exception
	 * @throws com.sun.star.uno.RuntimeException
	 * @throws Exception
	 */
	public static XMultiServiceFactory connectOfficeGetServiceFactory() throws com.sun.star.uno.Exception, 
																		   com.sun.star.uno.RuntimeException,
																		   Exception {
	    // Get component context
        XComponentContext xComponentContext = Bootstrap.createInitialComponentContext(null);
        
        // initial serviceManager
        XMultiComponentFactory xLocalServiceManager = xComponentContext.getServiceManager();
                
        // create a connector, so that it can contact the office
        Object  oUrlResolver  = xLocalServiceManager.createInstanceWithContext("com.sun.star.bridge.UnoUrlResolver", xComponentContext);
        XUnoUrlResolver xUrlResolver = QI.XUnoUrlResolver(oUrlResolver);
        
        Object oInitialObject = xUrlResolver.resolve(OOoUtils.LOCAL_OO_SERVICE_MGR_URL);
        XNamingService xName = QI.XNamingService(oInitialObject);
        
        XMultiServiceFactory xMSF = null;
        if( xName != null ) {
            Object oMSF = xName.getRegisteredObject("StarOffice.ServiceManager");
            xMSF = QI.XMultiServiceFactory(oMSF);
        }
        
        return xMSF;
	}
	
    /**
     * Unlock all cell's table to remove
     *
     * @param table The table to unlock.
     */
    public static void unlockTable(XTextTable table) {
        try {
        	
            // Get all cells name
            String [] cellNames = table.getCellNames();
            XPropertySet xCellProp = null;
            for (int i = 0; i < cellNames.length; i++) {
               // Unprotect the value cell
               xCellProp = QI.XPropertySet(table.getCellByName(cellNames[i]));
               xCellProp.setPropertyValue("IsProtected", new Boolean( false ));
            }
            
        } catch ( Exception e) {
        	throw new RuntimeException(e);
        }
    }
    
    /**
     * Updates all of the field defined in the current document.
     * 
     * @param pobjDoc The document that contains the fields to update.
     */
    public static void updateFields(Object pobjDoc) {
        XTextFieldsSupplier xTextFieldsSupplier = QI.XTextFieldsSupplier(pobjDoc);
        XEnumerationAccess xEnumeratedFields = xTextFieldsSupplier.getTextFields();
        XRefreshable xRefreshable = QI.XRefreshable(xEnumeratedFields);
        xRefreshable.refresh();
    }
    
    /**
     * @param oDoc Handle to the open text document.
     * @return Returns true if anything is selected.
     * @throws Exception
     */
    public static boolean isAnythingSelected(Object oDoc) throws Exception {
    	boolean isAnythingSelected = false;
    	
    	/** All of the selections */
    	XIndexAccess oSelections;
    	/** A single selection */
    	XTextRange oSel;
    	/** A temporary cursor */
    	XTextCursor oCursor;
    	
    	if (oDoc == null) return isAnythingSelected;
    	
    	/**
    	 * The current selection in the current controller.
    	 * If there is no current controller, it returns NULL.
    	 */
    	XTextDocument xTextDoc = QI.XTextDocument(oDoc);
    	oSelections = QI.XIndexAccess(xTextDoc.getCurrentSelection());
    	if (oSelections == null) return isAnythingSelected;
    		
    	//I have never seen a selection count of zero
    	if (oSelections.getCount() == 0) return isAnythingSelected;
    	
    	// If there are multiple selections, then certainly something is selected
    	if (oSelections.getCount() > 1) { 
    		isAnythingSelected = true;
    		
    	} else {
    		/**
    		 * If only one thing is selected, however, then check to see if the
    		 * selection is collapsed. In other words, see if the end location is
    		 * the same as the starting location.
    		 * Notice that I use the text object from the selection object
    		 * because it is safer than assuming that it is the same as the
    		 * documents text object. 
    		 */
    		oSel = QI.XTextRange(oSelections.getByIndex(0));
    		
    		oCursor = oSel.getText().createTextCursorByRange(oSel);
    		if(!oCursor.isCollapsed()) {
    			isAnythingSelected = true;
    		}
    		
    	}
    	
    	return isAnythingSelected;
    }
    
    /**
     * @param oDoc Handle to the current open document.
     * @return Returns the selected cursors
     * @throws Exception
     */
    private static XTextCursor[][] createSelectedTextIterator(Object oDoc) throws Exception {
    	XTextCursor[][] oCursors;
    	
    	/** Number of selected sections. */
    	int lSelCount;
    	/** Current selection item. */
    	int lWhichSelection;
    	/** All of the selections */
    	XIndexAccess oSelections;
    	/** A single selection. */
    	XTextRange oSel;
    	/** Cursor to the left of the current selection. */
    	XTextCursor oLCurs;
    	/** Cursor to the right of the current selection. **/
    	XTextCursor oRCurs; 
    	
		XTextDocument xTextDoc = QI.XTextDocument(oDoc);
    	
    	if (!isAnythingSelected(oDoc)) {
	        oLCurs = xTextDoc.getText().createTextCursor();
	        oLCurs.gotoStart(false);
	        oRCurs = xTextDoc.getText().createTextCursor();
	        oRCurs.gotoEnd(false);
	        
	        oCursors = new XTextCursor[1][2];
	        oCursors[0][0] = oLCurs;
	        oCursors[0][1] = oRCurs;
	        
    	} else {
    		oSelections = QI.XIndexAccess(xTextDoc.getCurrentSelection());
    		lSelCount = oSelections.getCount();
    		oCursors = new XTextCursor[lSelCount][2];
    		for (lWhichSelection = 0; lWhichSelection < lSelCount; lWhichSelection++) {
    			oSel = QI.XTextRange(oSelections.getByIndex(lWhichSelection));
    			
    			/**
    			 * If I want to know if NO text is selected, I could
    			 * do the following:
    			 * oLCurs = oSel.getText().CreateTextCursorByRange(oSel)
    			 * If oLCurs.isCollapsed() Then ...
    			 */
    			oLCurs = getLeftMostCursor(oSel);
    			oRCurs = getRightMostCursor(oSel);
    			oCursors[lWhichSelection][0] = oLCurs;
    			oCursors[lWhichSelection][1] = oRCurs;
    		}
    	}
    	
    	return oCursors;
    }
    
    /**
     * @param oSel is a text selection or cursor range
     * @return Returns left most cursor.
     * @throws Exception
     */
    public static XTextCursor getLeftMostCursor(XTextRange oSel) throws Exception {
    	XTextCursor getLeftMostCursor;
    	
    	/** Left most range. */
    	XTextRange oRange;
    	/** Cursor at the left most range. */
    	XTextCursor oCursor;
    	
    	XTextRangeCompare xSelTextRangeCompare = QI.XTextRangeCompare(oSel.getText());
    	if (xSelTextRangeCompare.compareRegionStarts(oSel.getEnd(), oSel) >= 0) {
    		oRange = oSel.getEnd();
    	} else {
    		oRange = oSel.getStart();
    	}
    	
    	oCursor = oSel.getText().createTextCursorByRange(oRange);
    	oCursor.goRight((short)0, false);
    	
    	getLeftMostCursor = oCursor;
    	
    	return getLeftMostCursor;
    }
    
    /**
     * @param oSel is a text selection or cursor range
     * @return Returns right most cursor.
     * @throws Exception
     */
    public static XTextCursor getRightMostCursor(XTextRange oSel) throws Exception {
    	XTextCursor getLeftMostCursor;
    	
    	/** Right most range. */
    	XTextRange oRange;
    	/** Cursor at the right most range. */
    	XTextCursor oCursor;
    	
    	XTextRangeCompare xSelTextRangeCompare = QI.XTextRangeCompare(oSel.getText());
    	
    	if (xSelTextRangeCompare.compareRegionStarts(oSel.getEnd(), oSel) >= 0) {
    		oRange = oSel.getStart();
    	} else {
    		oRange = oSel.getEnd();
    	}
    	
    	oCursor = oSel.getText().createTextCursorByRange(oRange);
    	oCursor.goLeft((short)0, false);
    	getLeftMostCursor = oCursor;
    	
    	return getLeftMostCursor;
    }
    
    /**
     * @param oDoc A handle to the open text document.
     * @param strSearch What to search for
     * @param strReplace What to replace with.
     * @throws Exception
     */
    public static void searchSelectedText(Object oDoc, String strSearch, String strReplace) throws Exception {
    	XTextCursor[][] oCursors = createSelectedTextIterator(oDoc);
    	if (oCursors == null) return;
    	
    	for (int i = 0; i < oCursors.length; i++) {
    		searchReplaceSelectedWorker(oDoc, oCursors[i][0], oCursors[i][1], 
    									strSearch, strReplace);
		}
    }
    
	/**
	 * Sets a table cell.
	 * 
	 * @param pobjTable The table to set the value in.
	 * @param pintRowNum The rum number. 0 being the first.
	 * @param pintColNum The column number. 0 being the first.
	 * @param pstrValue The value to set the cell.
	 * @return Returns the return value of the method.
	 */
	public static zXType.rc setCell(Object pobjTable, 
								  	int pintRowNum, 
								  	int pintColNum, 
								  	String pstrValue) {
		zXType.rc setTableCell = zXType.rc.rcOK;
		
		try {
			
			XCell xCell = (XCell)getTableCell(pobjTable, pintRowNum, pintColNum);
            XText xCellText = QI.XText(xCell);
            xCellText.setString(pstrValue);
            
            return setTableCell;
		} catch (Exception e) {
            setTableCell = zXType.rc.rcError;
			return setTableCell;
		}
	}
	
    /**
     * @param oDoc A handle to the open text document.
     * @param oLCurs The left hand cursor
     * @param oRCurs The right hand cursor.
     * @param strSearch What to search for
     * @param strReplace What to replace with.
     * @throws Exception
     */
    public static void searchReplaceSelectedWorker(Object oDoc, 
    											   XTextCursor oLCurs, XTextCursor oRCurs,
    											   String strSearch, String strReplace) throws Exception {
		if (oLCurs == null ||oRCurs == null || oDoc == null) return;
		
		XTextDocument xTextDoc = QI.XTextDocument(oDoc);
		XText xText = xTextDoc.getText();
		XTextRangeCompare xTextRangeCompare = QI.XTextRangeCompare(xText);
		if(xTextRangeCompare.compareRegionEnds(oLCurs, oRCurs) <= 0) return;
		oLCurs.goRight((short)0, false);
		
		XSearchDescriptor vDescriptor;
		XTextCursor vFound;
		
		XSearchable xSearchable = QI.XSearchable(xTextDoc);
		vDescriptor = xSearchable.createSearchDescriptor();
		vDescriptor.setSearchString(strSearch);
		
		/**
		 * There is no reason to perform a findFirst.
		 */
		vFound = QI.XTextCursor(xSearchable.findNext(oLCurs, vDescriptor));
		
		/**
		 * Would you kill for short-circuit evaluation?
		 */
		while (vFound != null) {
			// See if we searched past the end
			if (xTextRangeCompare.compareRegionEnds(vFound, oRCurs) == -1) break;
			
			/**
			 * Replace value.
			 */
			vFound.setString(strReplace);
			vFound = QI.XTextCursor(xSearchable.findNext(vFound.getEnd(), vDescriptor));
		}
    }
	
	/**
	 * Build a openoffice compatible url.
	 * @param pstrFileName The tranditional filename
	 * @return Returns a url suitable for Openoffice.
	 */
	public static String getOpenOfficeFilename(String pstrFileName) {
		try {
			StringBuffer sUrl = new StringBuffer("file:///");
			File sourceFile = new File(pstrFileName);
			sUrl.append(sourceFile.getCanonicalPath().replace('\\', '/'));
			return sUrl.toString();
		} catch (Exception e) {
			throw new RuntimeException("Failed to build openoffice url for : " + pstrFileName, e);
		}
	}
}