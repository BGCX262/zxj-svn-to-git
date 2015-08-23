/*
 * Created on Sep 1, 2004
 * $Id: DrctrFHDefaultTest.java,v 1.1.2.5 2006/01/27 18:12:57 mike Exp $
 */
package org.zxframework.director;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.ZX;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.director.DrctrFHDefault} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DrctrFHDefaultTest extends TestCase {

    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public DrctrFHDefaultTest(String name) {
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
        TestSuite suite = new TestSuite(DrctrFHDefaultTest.class);
        suite.setName("DrctrFHDefault Tests");
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
    
    //----------------------------------------------------- Tests constructors 
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        assertNotNull(new DrctrFHDefault());
        
        Constructor[] cons = DrctrFHDefault.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        
        assertEquals(true, Modifier.isPublic(DrctrFHDefault.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DrctrFHDefault.class.getModifiers()));
    }
    
    //---------------------------------------------------- Test directors with 0 parameters 

    /**
     * @throws ZXException Thrown if testZeroArg fails
     */
    public void testZeroArg() throws ZXException{
        
        assertEquals("T", zx.resolveDirector("#true"));
        assertEquals("F", zx.resolveDirector("#false"));
        assertEquals("EN", zx.resolveDirector("#language"));
        
        // notviewonly
        // uniqueid
        // user
        // whoami
        
    }
    
    //---------------------------------------------------------- Test directors that has up to 1 parameters
    
    /**
     * @throws ZXException Thrown if testZeroArg fails
     */
    public void testOneArg() throws ZXException{
        
        zx.getQuickContext().setEntry("name","test");
        assertEquals("test", zx.resolveDirector("#qs.name"));
        assertEquals("test", zx.resolveDirector("#qc.name"));
        assertEquals("T", zx.resolveDirector("#eval.#true"));
        
        zx.getBOContext().setEntry("test", zx.createBO("zXUsrGrp"));
        assertEquals("zXUsrGrp", zx.resolveDirector("#entityname.test"));
        assertEquals("Usergroup", zx.resolveDirector("#entitylabel.test"));
        
        ZXBO objZXBO = zx.getBOContext().getEntry("test");
        objZXBO.setPKValue("ADMIN");
        objZXBO.loadBO();
        assertEquals("Administrators", zx.resolveDirector("#labelvalue.test"));
        
        // 
        zx.getBOContext().setEntry("usrprf", zx.createBO("zxUsrPrf"));
        objZXBO = zx.getBOContext().getEntry("usrprf");
        objZXBO.setPKValue("TEST");
        objZXBO.loadBO();
        assertEquals("Michael Brewer", zx.resolveDirector("#labelvalue.usrprf"));
        
        assertEquals("ADMIN", zx.resolveDirector("#pk.test"));
        assertEquals("id", zx.resolveDirector("#pkname.test"));
        
        assertEquals("true", zx.resolveDirector("#expr.true()"));
        
        assertEquals("true", zx.resolveDirector("#expr.eval('true()')"));
        assertEquals("10.1", zx.resolveDirector("#expr.round(10.1,1)"));
        assertEquals("10.0", zx.resolveDirector("#expr.round(10.1,0)"));
        assertEquals("10.1234", zx.resolveDirector("#expr.round(10.12344, 4)"));
        assertEquals("10.1235", zx.resolveDirector("#expr.round(10.12346, 4)"));
        
        // inqs
        // notinqs
        // ingroup
        // havelock
        // date
        // time
        
    }
    
    //---------------------------------------------------------- Test directors that has up to 2 parameters
    
    /**
     * @throws ZXException Throw if testTwoArgs fails
     */
    public void testTwoArgs() throws ZXException {
        /**
         * Load in data.
         */
        zx.getBOContext().setEntry("test", zx.createBO("zXUsrGrp"));
        ZXBO objZXBO = zx.getBOContext().getEntry("test");
        objZXBO.setPKValue("ADMIN");
        objZXBO.loadBO();
        
        assertEquals("Administrators", zx.resolveDirector("#attrvalue.test.nme"));
        assertEquals("Administrators", zx.resolveDirector("#attrvalue.nme"));
        assertEquals("ADMIN", zx.resolveDirector("#attrvalue.id"));
        
        assertEquals("Administrators", zx.resolveDirector("#attrvalue.test.nme"));
        assertEquals("Administrators", zx.resolveDirector("#attrvalue.nme"));
        assertEquals("ADMIN", zx.resolveDirector("#attrvalue.id"));

        assertEquals("Administrators", zx.resolveDirector("#attrformattedvalue.test.nme"));
        assertEquals("Administrators", zx.resolveDirector("#attrformattedvalue.nme"));
        assertEquals("ADMIN", zx.resolveDirector("#attrformattedvalue.id"));
        
        // fkattrname
        // notnull
        // null
        
    }
}