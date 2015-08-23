/*
 * Created on May 23, 2005
 * $Id$
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
 * Unit tests {@link org.zxframework.datasources.DSWhereClause} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class DSWhereClauseTest extends TestCase {
    
    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public DSWhereClauseTest(String name) {
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
        TestSuite suite = new TestSuite(DSWhereClauseTest.class);
        suite.setName("DSWhereClause Tests");
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
    
    /**
     * Class under test for zXType.rc parse(ZXBO, String, boolean, boolean)
     * @throws ZXException Thrown if the test fails.
     */
    public void testParseZXBO() throws ZXException {
        DSWhereClause objDSWhereClause = new DSWhereClause();
        ZXBO objBO = zx.createBO("zXUsrGrp");
        objBO.setPKValue("All");
        objBO.loadBO();
        
        assertEquals(zXType.rc.rcOK.pos, objDSWhereClause.parse(objBO, "<>+").pos);
        assertEquals(zXType.rc.rcOK.pos, objDSWhereClause.parse(objBO, ":id<>1").pos);
        assertEquals(zXType.rc.rcOK.pos, objDSWhereClause.parse(objBO, ":id <> 1 & id <> 12").pos);
    }
}