/*
 * Created on Jun 12, 2005
 * $Id: NoOpsDocument.java,v 1.1.2.3 2005/06/17 20:16:02 mike Exp $
 */
package org.zxframework.doc;

import org.zxframework.ZXBO;
import org.zxframework.zXType;
import org.zxframework.zXType.rc;
import org.zxframework.zXType.wordSection;

/**
 * Document.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @version 0.01
 */
public class NoOpsDocument extends AbstractDocument {
	
	//------------------------ Document Interface methods
	
	/**
	 * @see org.zxframework.doc.Document#wordDoc()
	 */
	public Object wordDoc() {
		return null;
	}

	/**
	 * @see org.zxframework.doc.Document#wordApp()
	 */
	public Object wordApp() {
		return null;
	}

	/**
	 * @see org.zxframework.doc.Document#activate()
	 */
	public rc activate() {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.doc.Document#newDoc(java.lang.String)
	 */
	public rc newDoc(String pstrTemplate) {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.doc.Document#saveDocAs(java.lang.String)
	 */
	public rc saveDocAs(String pstrSaveAs) {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.doc.Document#giveUserFocus()
	 */
	public rc giveUserFocus() {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.doc.Document#getTable(int, int, org.zxframework.zXType.wordSection)
	 */
	public Object getTable(int pintTable, int pintSection, wordSection penmArea) {
		return null;
	}

	/**
	 * @see org.zxframework.doc.Document#setTableCell(java.lang.Object, int, int, java.lang.String)
	 */
	public rc setTableCell(Object pobjTable, 
						   int pintRowNum, 
						   int pintColNum,
						   String pstrValue) {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.doc.Document#populateTableCell(java.lang.Object, int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public rc populateTableCell(Object pobjTable, 
								int pintRowNum,
								int pintColNum, 
								String pstrValue1, 
								String pstrValue2,
								String pstrValue3, 
								String pstrValue4, 
								String pstrValue5) {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.doc.Document#openDoc(java.lang.String)
	 */
	public rc openDoc(String pstrDoc) {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.doc.Document#setPrinter(java.lang.String)
	 */
	public rc setPrinter(String pstrPrinter) {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.doc.Document#closeDoc(boolean)
	 */
	public rc closeDoc(boolean pblnSaveChanges) {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.doc.Document#addTableRow(java.lang.Object)
	 */
	public rc addTableRow(Object pobjTable) {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.doc.Document#boTableRowPopulate(java.lang.Object, org.zxframework.ZXBO, java.lang.String, org.zxframework.ZXBO, java.lang.String, org.zxframework.ZXBO, java.lang.String, org.zxframework.ZXBO, java.lang.String, org.zxframework.ZXBO, java.lang.String)
	 */
	public rc boTableRowPopulate(Object pobjRow, 
								 ZXBO pobjBO1, String pstrGroup1, 
								 ZXBO pobjBO2, String pstrGroup2, 
								 ZXBO pobjBO3, String pstrGroup3, 
								 ZXBO pobjBO4, String pstrGroup4, 
								 ZXBO pobjBO5, String pstrGroup5) {
		return zXType.rc.rcOK;	
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
		return zXType.rc.rcOK;
	}
	
	/**
	 * @see AbstractDocument#boTableRowPopulate(java.lang.Object, 
	 * 									 org.zxframework.ZXBO[], 
	 * 									 java.lang.String[])
	 */
	public zXType.rc boTableRowPopulate(Object pobjTable, 
								 		ZXBO[] parrBO, 
								 		String[] parrGroup) {
		return zXType.rc.rcOK;
	}
}