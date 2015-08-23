/*
 * Created on May 23, 2005
 * $Id: DSHFileTest.java,v 1.1.2.7 2006/07/17 16:13:46 mike Exp $
 */
package org.zxframework.datasources;

import org.zxframework.ZX;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.datasources.DSHFile} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class DSHFileTest extends TestCase {
    
    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public DSHFileTest(String name) {
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
        TestSuite suite = new TestSuite(DSHFileTest.class);
        suite.setName("DSHFile Tests");
        return suite;
    }
    
    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        zx = new ZX(TestUtil.getCfgPath());
        zx.getSession().connect("test","test");
        
        super.setUp();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    } 
    
    //----------------------------------------------------- Tests.
    String id = "12211";
    String strName = "Michael Brewer";
    String strDesc = "The coolest man! :)";
    /**
     * @throws ZXException Thrown if test fails.
     */
    public void testInsertBO() throws ZXException {
        ZXBO objBO = zx.createBO("test/chap");
        
        // Setup test data
        objBO.setPKValue(id);
        objBO.setValue("nme", strName);
        objBO.setValue("dscrptn", strDesc);
        assertEquals(zXType.rc.rcOK.pos, objBO.insertBO().pos);
        objBO.resetBO();
        
        objBO.setPKValue(id);
        assertEquals(zXType.rc.rcOK.pos, objBO.loadBO().pos);
        assertEquals(strName, objBO.getValue("nme").getStringValue());
        objBO.resetBO();
        
        // Clean up
        objBO.setPKValue(id);
        objBO.deleteBO();
    }
    
    /**
     * @throws ZXException Thrown if test fails.
     */
    public void testLoadBO() throws ZXException {
        ZXBO objBO = zx.createBO("test/chap");
        
        // Setup test data
        objBO.setPKValue(id);
        objBO.setValue("nme", strName);
        objBO.setValue("dscrptn", strDesc);
        assertEquals("Setup test data", zXType.rc.rcOK.pos, objBO.insertBO().pos);
        objBO.resetBO();
        
        // Check load successful.
        objBO.setPKValue(id);
        objBO.setValue("nme", strName);
        objBO.setValue("dscrptn", strDesc);
        assertEquals("Check load successful.", zXType.rc.rcOK.pos,objBO.loadBO("*", "id,nme", false).pos);
        objBO.resetBO();
        
        // Check load
        objBO.setPKValue(id);
        assertEquals("Check load", zXType.rc.rcOK.pos,objBO.loadBO().pos);
        assertEquals("Check load", strName, objBO.getValue("nme").getStringValue());
        objBO.resetBO();
        
        // Failed to load
        objBO.setPKValue(id);
        objBO.setValue("nme", strDesc + "1");
        assertEquals("Should fail to load", zXType.rc.rcWarning.pos,objBO.loadBO("*", "id,nme", false).pos);
        objBO.resetBO();

        // Clean up
        objBO.setPKValue(id);
        objBO.deleteBO();
    }
    
    /**
     * @throws ZXException Thrown if test fails.
     */
    public void testDeleteBO() throws ZXException {
        ZXBO objBO = zx.createBO("test/chap");
        
        // Setup test data
        objBO.setPKValue(id);
        objBO.setValue("nme", strName);
        objBO.setValue("dscrptn", strDesc);
        objBO.insertBO();
        objBO.resetBO();
        
        objBO.setPKValue(id);
        objBO.loadBO();
        
        assertEquals("Check if delete was successfull", zXType.rc.rcOK.pos,objBO.deleteBO().pos);
        assertEquals("Check if we actually deleted the record",zXType.rc.rcWarning.pos,objBO.loadBO("*").pos);
    }
    
    /**
     * @throws ZXException Thrown if test fails.
     */
    public void testUpdateBO() throws ZXException {
        ZXBO objBO = zx.createBO("test/chap");
        
        // Setup test data
        objBO.setPKValue(id);
        objBO.setValue("nme", strName);
        objBO.setValue("dscrptn", strDesc);
        objBO.insertBO();
        objBO.resetBO();
        objBO.setPKValue("323244324");
        objBO.setValue("nme", "Dummy Data");
        objBO.setValue("dscrptn", "Dummy Data");
        objBO.insertBO();
        objBO.resetBO();
        
        
        // Do update
        objBO.setPKValue(id);
        objBO.loadBO();
        objBO.setValue("dscrptn", strDesc + "1");
        assertEquals("Check if update was successfull", zXType.rc.rcOK.pos, objBO.updateBO().pos);
        objBO.resetBO();
        
        // Check values
        objBO.setPKValue(id);
        objBO.setValue("dscrptn", strDesc + "1");
        objBO.loadBO();
        assertEquals("Check if value is updated", zXType.rc.rcOK.pos,objBO.loadBO("*", "id,nme", false).pos);
        assertEquals("Check if value is updated", strDesc + "1",objBO.getValue("dscrptn").getStringValue());
        objBO.resetBO();
        
        // Clean up
        objBO.setPKValue(id);
        objBO.deleteBO();
        objBO.setPKValue("323244324");
        objBO.deleteBO();
    }
    
    /**
     * @throws ZXException Thrown if tails fails.
     */
    public void testBORS() throws ZXException {
        ZXBO objBO = zx.createBO("test/chap");
        DSRS objRS = objBO.getDS().boRS(objBO);
        while (!objRS.eof()) {
            objRS.rs2obj(objBO, "*");
            // System.out.println(objBO.bo2XML("*"));
            objRS.moveNext();
        }
    }
    
    /**
     * @throws Exception Thrown if test fails.
     */
    public void testFindBORecord() throws Exception {
    	String strWhereGroup;
    	DSWhereClause objDSWhereClause;
    	
    	ZXBO objBO = zx.createBO("test/Company");
    	objBO.setValue("id", "2");
    	
        ZXBO objBOTmp = zx.createBO("test/Company");
        objBOTmp.setValue("id", "2");
        objBOTmp.setValue("name", "Funky");
        
    	strWhereGroup = "id";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(true, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
    	strWhereGroup = "<>id";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(false, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
    	strWhereGroup = ":id = 2";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(true, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
    	strWhereGroup = ":id <> 2";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(false, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
    	strWhereGroup = ":id > 3";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(false, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
    	strWhereGroup = ":id < 3";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(true, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));

    	strWhereGroup = ":id = 2 | id = 3";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(true, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
    	strWhereGroup = ":id = 2 | id = 3 | id = 4";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(true, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
        strWhereGroup = ":id <> 2 & id <> 4 & id <> 3";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(false, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
        strWhereGroup = ":id <> 1 & id <> 4 & id <> 3";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(true, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
        strWhereGroup = ":(id = 3 | id = 2 | id = 4) & (name = 'Funky')";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(true, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
        strWhereGroup = ":(id = 3 | id = 1 | id = 4) & (name = 'Funky')";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(false, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
        strWhereGroup = ":(id = 2) & (id <> 3 & id <> 4)";
        objDSWhereClause = new DSWhereClause();
        objDSWhereClause.parse(objBO, strWhereGroup);
        assertEquals(true, zx.getBos().isMatchingBO(objBOTmp, objDSWhereClause));
        
    }
}