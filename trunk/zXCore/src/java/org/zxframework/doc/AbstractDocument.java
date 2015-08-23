/*
 * Created on Jun 12, 2005
 * $Id: AbstractDocument.java,v 1.1.2.2 2005/06/17 20:16:03 mike Exp $
 */
package org.zxframework.doc;

import java.util.ArrayList;

import org.zxframework.ZXBO;
import org.zxframework.ZXObject;
import org.zxframework.zXType;

/**
 * Document Abstract Class.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @version 0.01
 */
public abstract class AbstractDocument extends ZXObject implements Document {
	
	//------------------------ Document implemented methods
	
	/**
	 * @see Document#boTableRowPopulate(Object, 
	 * 							 org.zxframework.ZXBO, String,
	 *      					 org.zxframework.ZXBO, String, 
	 *      					 org.zxframework.ZXBO, String,
	 *       					 org.zxframework.ZXBO, String, 
	 *       					 org.zxframework.ZXBO, String)
	 */
	public zXType.rc boTableRowPopulate(Object pobjTableRow, 
								 ZXBO pobjBO1, String pstrGroup1,
						  		 ZXBO pobjBO2, String pstrGroup2, 
						  		 ZXBO pobjBO3, String pstrGroup3,
						  		 ZXBO pobjBO4, String pstrGroup4, 
						  		 ZXBO pobjBO5, String pstrGroup5) {
		
		ArrayList arrBO = new ArrayList();
		ArrayList arrGroup = new ArrayList();
		
		if(pobjBO1 != null && pstrGroup1 != null) {
			arrBO.add(pobjBO1);
			arrGroup.add(pstrGroup1);
		}
		if(pobjBO2 != null && pstrGroup2 != null) {
			arrBO.add(pobjBO2);
			arrGroup.add(pstrGroup2);
		}
		if(pobjBO3 != null && pstrGroup3 != null) {
			arrBO.add(pobjBO3);
			arrGroup.add(pstrGroup3);
		}
		if(pobjBO4 != null && pstrGroup4 != null) {
			arrBO.add(pobjBO4);
			arrGroup.add(pstrGroup4);
		}
		if(pobjBO5 != null && pstrGroup5 != null) {
			arrBO.add(pobjBO5);
			arrGroup.add(pstrGroup5);
		}
		
		return boTableRowPopulate(pobjTableRow, 
						   		  (ZXBO[])arrBO.toArray(new ZXBO[arrBO.size()]), 
						   		  (String[])arrGroup.toArray(new String[arrBO.size()]));
	}
	
	/**
	 * @see Document#boTableInit(Object, 
	 * 							 org.zxframework.ZXBO, String,
	 *      					 org.zxframework.ZXBO, String, 
	 *      					 org.zxframework.ZXBO, String,
	 *       					 org.zxframework.ZXBO, String, 
	 *       					 org.zxframework.ZXBO, String, 
	 *       					 boolean)
	 */
	public zXType.rc boTableInit(Object pobjTable, 
								 ZXBO pobjBO1, String pstrGroup1,
						  		 ZXBO pobjBO2, String pstrGroup2, 
						  		 ZXBO pobjBO3, String pstrGroup3,
						  		 ZXBO pobjBO4, String pstrGroup4, 
						  		 ZXBO pobjBO5, String pstrGroup5, 
						  		 boolean pblnBorders) {
		
		ArrayList arrBO = new ArrayList();
		ArrayList arrGroup = new ArrayList();
		
		if(pobjBO1 != null && pstrGroup1 != null) {
			arrBO.add(pobjBO1);
			arrGroup.add(pstrGroup1);
		}
		if(pobjBO2 != null && pstrGroup2 != null) {
			arrBO.add(pobjBO2);
			arrGroup.add(pstrGroup2);
		}
		if(pobjBO3 != null && pstrGroup3 != null) {
			arrBO.add(pobjBO3);
			arrGroup.add(pstrGroup3);
		}
		if(pobjBO4 != null && pstrGroup4 != null) {
			arrBO.add(pobjBO4);
			arrGroup.add(pstrGroup4);
		}
		if(pobjBO5 != null && pstrGroup5 != null) {
			arrBO.add(pobjBO5);
			arrGroup.add(pstrGroup5);
		}
		
		return boTableInit(pobjTable, 
						   (ZXBO[])arrBO.toArray(new ZXBO[arrBO.size()]), 
						   (String[])arrGroup.toArray(new String[arrBO.size()]), 
						   pblnBorders);
	}
	
	//------------------------ Useful wrappers
	
	/**
	 * Create new document, optionally based on a template.
	 * 
	 * @return Returns the return code of the method.
	 */
	public zXType.rc newDoc() {
		return newDoc(null);
	}
	
	/**
	 * Select table that is treated as an X Word Table object.
	 * 
	 * @param pintTable Sequence number of table within. 
	 * @return Returns the return code of the method.
	 */
	public Object getTable(int pintTable) {
		return getTable(pintTable, 1 , zXType.wordSection.wsPage);
	}
	
	/**
	 * Select table that is treated as an X Word Table object.
	 * 
	 * @param pintTable Sequence number of table within.
	 * @param pintSection Sequence number of section within. 
	 * 					  Optional default should be 1 
	 * @return Returns the return code of the method.
	 */
	public Object getTable(int pintTable, int pintSection) {
		return getTable(pintTable, pintSection , zXType.wordSection.wsPage);
	}
	
	/**
	 * Replace occurences of $x in cell with a value.
	 * 
	 * @param pobjTable The table cell object.
	 * @param pintRowNum The row number.
	 * @param pintColNum The column number.
	 * @param pstrValue1 Value
	 * @return Returns the return code of the method.
	 */
	public zXType.rc populateTableCell(Object pobjTable,
									   int pintRowNum,
									   int pintColNum,
									   String pstrValue1) {
		return populateTableCell(pobjTable,
								 pintColNum,
								 pintRowNum,
								 pstrValue1,
								 null,
								 null,
								 null,
								 null);
	}
	
	/**
	 * Close active document.
	 * 
	 * @return Returns the return code of the method.
	 */
	public zXType.rc closeDoc() {
		return closeDoc(false);
	}
	
	
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
	 * @return Returns the return code of the method.
	 */
	public zXType.rc boTableInit(Object pobjTable,
								 ZXBO pobjBO1,
								 String pstrGroup1) {
		return boTableInit(pobjTable,
						   pobjBO1,
						   pstrGroup1,
						   null,
						   null,
						   null,
						   null,
						   null,
						   null,
						   null,
						   null,
						   true);
	}
	
	/**
	 * Add row to Word table and populate.
	 * 
	 * @param pobjRow
	 * @param pobjBO1
	 * @param pstrGroup1
	 * @return Returns the return code of the method.
	 */
	public zXType.rc boTableRowPopulate(Object pobjRow,
										ZXBO pobjBO1,
										String pstrGroup1) {
		return boTableRowPopulate(pobjRow,
				  				  pobjBO1,
				  				  pstrGroup1,
				  				  null,
				  				  null,
				  				  null,
				  				  null,
				  				  null,
				  				  null,
				  				  null,
				  				  null);
	}
	
	//------------------------ Abstract methods
	
	/**
	 * Populate a table with business object data.
	 * 
	 * @param pobjTable The handle to the table to populate.
	 * @param parrBO1 An array of business object
	 * @param parrGroup1 An array of display attribute groups
	 * @param pblnBorders With to print any borders.
	 * @return Returns the return code of the method.
	 */
	public abstract  zXType.rc boTableInit(Object pobjTable, 
										   ZXBO[] parrBO1, 
										   String[] parrGroup1,
										   boolean pblnBorders);
	
	/**
	 * Add row to Word table and populate.
	 * 
	 * @param pobjTableRow The handle to the table row to populate.
	 * @param parrBO An array of business object
	 * @param parrGroup An array of display attribute groups
	 * @return Returns the return code of the method.
	 */
	public abstract  zXType.rc boTableRowPopulate(Object pobjTableRow, 
										   		  ZXBO[] parrBO, 
										   		  String[] parrGroup);
}