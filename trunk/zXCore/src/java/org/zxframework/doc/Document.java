/*
 * Created on Jun 12, 2005
 * $Id: Document.java,v 1.1.2.1 2005/06/13 07:03:49 mike Exp $
 */
package org.zxframework.doc;

import org.zxframework.ZXBO;
import org.zxframework.zXType;

/**
 * Document Inteface.
 * 	
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @version 0.01
 */
public interface Document {
	
	/**
	 * Handle to current Word document.
	 * 
	 * @return Returns handle to current Word document
	 */
	public Object wordDoc();
	
	/**
	 * Handle to Word application
	 * @return Return handle to Word application
	 */
	public Object wordApp();
	
	/**
	 * Make sure that the ActiveX Exe is still available for us.
	 * 
	 * @return Returns the return code of the method.
	 */
	public zXType.rc activate();
	
	/**
	 * Create new document, optionally based on a template.
	 * 
	 * @param pstrTemplate The name of the new file. Optional, default should be null.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc newDoc(String pstrTemplate);
	
	/**
	 * Save active document.
	 * 
	 * @param pstrSaveAs The filename to save to.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc saveDocAs(String pstrSaveAs);
	
	/**
	 * Give Word the user focus.
	 * 
	 * @return Returns the return code of the method.
	 */
	public zXType.rc giveUserFocus();
	
	/**
	 * Select table that is treated as an X Word Table object.
	 * 
	 * @param pintTable Sequence number of table within. 
	 * @param pintSection Sequence number of section within. Section (starting at 1)
	 * @param penmArea Page, header or footer. Optional default is wsPage
	 * @return Returns the return code of the method.
	 */
	public Object getTable(int pintTable, 
						   int pintSection, 
						   zXType.wordSection penmArea);
	
	/**
	 * Set single cell in table to a value.
	 * 
	 * @param pobjTable The table cell to set.
	 * @param pintRowNum The row number.
	 * @param pintColNum The column number.
	 * @param pstrValue The value.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc setTableCell(Object pobjTable, 
								  int pintRowNum, 
								  int pintColNum,
								  String pstrValue);
	
	
	/**
	 * Replace occurences of $x in cell with a value.
	 * 
	 * @param pobjTable The table cell object.
	 * @param pintRowNum The row number.
	 * @param pintColNum The column number.
	 * @param pstrValue1 Value
	 * @param pstrValue2 Value (optional)
	 * @param pstrValue3 Value (optional)
	 * @param pstrValue4 Value (optional)
	 * @param pstrValue5 Value (optional)
	 * @return Returns the return code of the method.
	 */
	public zXType.rc populateTableCell(Object pobjTable,
									   int pintRowNum,
									   int pintColNum,
									   String pstrValue1,
									   // Optional value. maybe use Collect.
									   String pstrValue2,
									   String pstrValue3,
									   String pstrValue4,
									   String pstrValue5);
	
	/**
	 * Opens document.
	 * 
	 * @param pstrDoc The document to open.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc openDoc(String pstrDoc);
	
	/**
	 * Set active printer.
	 * 
	 * @param pstrPrinter The printer to use.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc setPrinter(String pstrPrinter);
	
	/**
	 * Close active document.
	 * 
	 * @param pblnSaveChanges Optional, default should be false.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc closeDoc(boolean pblnSaveChanges);
	
	/**
	 * Add a row to given table.
	 * 
	 * @param pobjTable The table object
	 * @return Returns the return code of the method.
	 */
	public zXType.rc addTableRow(Object pobjTable);
	
	/**
	 * Replace a table-place holder with a BO
	 * aware table.
	 * 
	 * <pre>
	 * Assumes   :
	 *  wordDoc set and contains a table
	 *  place holder
	 * </pre>
	 * 
	 * @param pobjTable
	 * @param pobjBO1
	 * @param pstrGroup1
	 * @param pobjBO2
	 * @param pstrGroup2
	 * @param pobjBO3
	 * @param pstrGroup3
	 * @param pobjBO4
	 * @param pstrGroup4
	 * @param pobjBO5
	 * @param pstrGroup5
	 * @param pblnBorders
	 * @return Returns the return code of the method.
	 */
	public zXType.rc boTableInit(Object pobjTable,
								 ZXBO pobjBO1,
								 String pstrGroup1,
								 // Optional value. maybe use Collect.
								 ZXBO pobjBO2,
								 String pstrGroup2,
								 ZXBO pobjBO3,
								 String pstrGroup3,
								 ZXBO pobjBO4,
								 String pstrGroup4,
								 ZXBO pobjBO5,
								 String pstrGroup5,
								 boolean pblnBorders);
	
	/**
	 * Add row to Word table and populate.
	 * 
	 * @param pobjRow
	 * @param pobjBO1
	 * @param pstrGroup1
	 * @param pobjBO2
	 * @param pstrGroup2
	 * @param pobjBO3
	 * @param pstrGroup3
	 * @param pobjBO4
	 * @param pstrGroup4
	 * @param pobjBO5
	 * @param pstrGroup5
	 * @return Returns the return code of the method.
	 */
	public zXType.rc boTableRowPopulate(Object pobjRow,
										ZXBO pobjBO1,
										String pstrGroup1,
										// Optional value. maybe use Collect.
										ZXBO pobjBO2,
										String pstrGroup2,
										ZXBO pobjBO3,
										String pstrGroup3,
										ZXBO pobjBO4,
										String pstrGroup4,
										ZXBO pobjBO5,
										String pstrGroup5);
	
}