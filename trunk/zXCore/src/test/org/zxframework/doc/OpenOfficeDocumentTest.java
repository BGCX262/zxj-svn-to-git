/*
 * Created on Jun 14, 2005
 * $Id: OpenOfficeDocumentTest.java,v 1.1.2.9 2005/08/25 14:37:02 mike Exp $
 */
package org.zxframework.doc;

import org.zxframework.ZX;
import org.zxframework.ZXBO;
import org.zxframework.zXType;
import org.zxframework.util.TestUtil;

import com.sun.star.table.XCellRange;
import com.sun.star.text.XTextTable;
import com.sun.star.uno.UnoRuntime;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.doc.OpenOfficeDocument} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class OpenOfficeDocumentTest extends TestCase {
	
    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public OpenOfficeDocumentTest(String name) {
        super(name);
    }
    
    /**
     * @param args Program parameters.
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    /**
     * @return Returns the test to run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(OpenOfficeDocumentTest.class);
        suite.setName("OpenOfficeDocument Tests");
        return suite;
    }
    
    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        zx = new ZX(TestUtil.getCfgPath());
        super.setUp();
    }
    
    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        zx.cleanup();
        super.tearDown();
    }
    
    //------------------------------------------ Tests
    
    /**
     * Test new Doc
     */
    public void testNewDoc() {
    	AbstractDocument doc = new OpenOfficeDocument();
    	try {
    		
	    	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
	    	assertEquals("Create new blank document", zXType.rc.rcOK.pos, doc.newDoc(null).pos);
	    	
    	} finally {
    		assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
    
    /**
     * Test new Doc
     */
    public void testOpenDoc() {
    	AbstractDocument doc = new OpenOfficeDocument();
    	try {
    		
	    	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
	    	assertEquals("Create new blank document", zXType.rc.rcOK.pos, doc.openDoc("c:/tmp/test.doc").pos);
	    	
    	} finally {
    		assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
    
    /**
     * Test new Doc
     */
    public void testGetParagraph() {
    	OpenOfficeDocument doc = new OpenOfficeDocument();
    	try {
    		
	    	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
	    	assertEquals("Create new blank document", zXType.rc.rcOK.pos, doc.openDoc("c:/tmp/test.doc").pos);
	    	assertEquals(false, doc.getParagraph(3) == null);
	    	
    	} finally {
    		assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
    
    /**
     * Test new Doc
     */
    public void testGetSection() {
    	OpenOfficeDocument doc = new OpenOfficeDocument();
    	try {
    		
	    	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
	    	assertEquals("Create new blank document", zXType.rc.rcOK.pos, doc.openDoc("c:/tmp/test.doc").pos);
	    	assertEquals(false, doc.getSection(3) == null);
	    	
    	} finally {
    		assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
    
    /**
     * Test new Doc
     */
    public void testSaveDocAs() {
    	AbstractDocument doc = new OpenOfficeDocument();
    	try {
    		
	    	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
	    	assertEquals("Open document", zXType.rc.rcOK.pos, doc.openDoc("c:/tmp/test.doc").pos);
	    	
        	Object objTable = doc.getTable(1, 1, zXType.wordSection.wsPage);
        	assertEquals("Set table cell", zXType.rc.rcOK.pos, doc.setTableCell(objTable, 1, 1, "Hello World!!!").pos);
	    	
	    	assertEquals("Save as", zXType.rc.rcOK.pos, doc.saveDocAs("c:/tmp/test.pdf").pos);
	    	
    	} finally {
    		assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
    
    /**
     * Tests setTableCell
     */
    public void testSetTableCell() {
    	AbstractDocument doc = new OpenOfficeDocument();
    	try {
    		
        	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
        	assertEquals("Open test document", zXType.rc.rcOK.pos, doc.openDoc("c:/tmp/test.doc").pos);
        	Object objTable = doc.getTable(1, 1, zXType.wordSection.wsPage);
        	assertEquals("Set table cell", zXType.rc.rcOK.pos, doc.setTableCell(objTable, 1, 1, "Hello World").pos);
        	
    	} finally {
        	assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
    
    /**
     * Tests setTableCell
     */
    public void testReplace() {
    	OpenOfficeDocument doc = new OpenOfficeDocument();
    	try {
    		
        	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
        	assertEquals("Open test document", zXType.rc.rcOK.pos, doc.openDoc("c:/tmp/test.doc").pos);
        	Object objTable = doc.getTable(1, 1, zXType.wordSection.wsPage);
        	doc.replaceRange(objTable, "test", "test");
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
        	assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
    
    /**
     * Tests setTableCell
     * @throws Exception 
     */
    public void testSearchSelectedText() throws Exception {
    	OpenOfficeDocument doc = new OpenOfficeDocument();
    	try {
    		
        	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
        	assertEquals("Open test document", zXType.rc.rcOK.pos, doc.openDoc("c:/tmp/test.doc").pos);
        	
        	//Object objTable = doc.getTable(1, 1, zXType.wordSection.wsPage);
        	//OpenOfficeDocument.selectObject(QI.XTextDocument(doc.wordDoc()), objTable);
        	OpenOfficeDocument.searchSelectedText(doc.wordDoc(), "cool", "tests");
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
        	assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
    
    /**
     * Tests populateTableCell
     */
    public void testPopulateTableCell() {
    	AbstractDocument doc = new OpenOfficeDocument();
    	try {
    		
	    	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
	    	assertEquals("Open test document", zXType.rc.rcOK.pos, doc.openDoc("c:/tmp/test.doc").pos);
	    	Object objTable = doc.getTable(1, 1, zXType.wordSection.wsPage);
	    	assertEquals("Populate table cell", zXType.rc.rcOK.pos, doc.populateTableCell(objTable, 1, 1, "(GONE)").pos);
	    	
    	} finally {
        	assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
    
    /**
     * Tests addTableRow
     */
    public void testAddTableRow() {
    	AbstractDocument doc = new OpenOfficeDocument();
    	try {
    		
	    	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
	    	assertEquals("Open test document", zXType.rc.rcOK.pos, doc.openDoc("c:/tmp/test.doc").pos);
	    	Object objTable = doc.getTable(1, 1, zXType.wordSection.wsPage);
	    	assertEquals("Add row to table", zXType.rc.rcOK.pos, doc.addTableRow(objTable).pos);
	    	
    	} finally {
        	assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
    
    /**
     * Tests boTableInit and boTableRowPopulate
     * 
     * @throws Exception
     */
    public void testBoTableInit() throws Exception {
    	AbstractDocument doc = new OpenOfficeDocument();
    	try {
    		String strGroup = "*";
    		ZXBO objBO = zx.createBO("test/test");
    		objBO.setPKValue("12");
    		objBO.loadBO();
    		
	    	assertEquals("Activate Openoffice", zXType.rc.rcOK.pos, doc.activate().pos);
	    	assertEquals("Open test document", zXType.rc.rcOK.pos, doc.openDoc("c:/tmp/test.doc").pos);
	    	
	    	/**
	    	 * Get the placeholder table.
	    	 */
	    	Object objTable = doc.getTable(1, 1, zXType.wordSection.wsPage);
	    	assertEquals("Setup the table", zXType.rc.rcOK.pos, doc.boTableInit(objTable, objBO, strGroup).pos);
	    	
	    	objTable = doc.getTable(1, 1, zXType.wordSection.wsPage);
			XTextTable xTable = (XTextTable)UnoRuntime.queryInterface(XTextTable.class, objTable);
			XCellRange objRow = (XCellRange)UnoRuntime.queryInterface(XCellRange.class, xTable);
			objRow = objRow.getCellRangeByName("A2:C2");
			
	    	assertEquals("Populate table row.", zXType.rc.rcOK.pos, doc.boTableRowPopulate(objRow, objBO, strGroup).pos);
	    	
    	} finally {
        	assertEquals("Close document", zXType.rc.rcOK.pos, doc.closeDoc(false).pos);
    	}
    }
}