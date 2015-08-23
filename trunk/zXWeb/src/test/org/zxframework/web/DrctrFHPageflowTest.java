/*
 * Created on 03-Feb-2005, by Michael Brewer
 * $Id: DrctrFHPageflowTest.java,v 1.1.2.4 2006/07/17 14:04:04 mike Exp $
 */
package org.zxframework.web;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.ZX;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests {@link org.zxframework.web.DrctrFHPageflow} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class DrctrFHPageflowTest extends TestCase {

    private ZX zx;
    
    /**
     * Constructor for DrctrFHPageflowTest.
     * @param name
     */
    public DrctrFHPageflowTest(String name) {
        super(name);
    }
    
    /**
     * @param args Arguments.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(DrctrFHPageflowTest.class);
    }
    
    /**
     * @return Returns the test to run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(DrctrFHPageflowTest.class);
        suite.setName("DrctrFHPageflow Tests");
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
     * 
     * @throws ZXException Thrown if testConstructor fails
     */
    public void testConstructor() {
        assertNotNull(new DrctrFHPageflow());
        
        Constructor[] cons = DrctrFHPageflow.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        
        assertEquals(true, Modifier.isPublic(DrctrFHPageflow.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DrctrFHPageflow.class.getModifiers()));
    }
    
    //--------------------------------------------------- Test Directors
    
    /**
     * Test the available directors.
     * 
     * @throws ZXException Thrown if testDirectors fails
     */
    public void testDirectors() throws ZXException {
        
        // Preparation :
        zx.getQuickContext().setEntry("name","test");
        zx.getQuickContext().setEntry("test","test");
        
        // Load the pageflow director handler.
        DrctrFHPageflow pageflowDirectorFuncHandler = new DrctrFHPageflow();
        Pageflow objPageflow = new Pageflow();
        objPageflow.setPage(new PageBuilder());
        pageflowDirectorFuncHandler.setPageflow(objPageflow);
        zx.getDirectorHandler().registerFH("pageflow", pageflowDirectorFuncHandler);
        
        assertEquals("", zx.resolveDirector("#qsn.*"));
        assertEquals("?name=test", zx.resolveDirector("#qsn.*-name"));
        assertEquals("?test=test", zx.resolveDirector("#qsn.*-test"));
        assertEquals("?test=test&name=test", zx.resolveDirector("#qsn.*-test-name"));
        
        assertEquals("", zx.resolveDirector("#qs.*"));
        assertEquals("?name=test", zx.resolveDirector("#qs.*-name"));
        assertEquals("?test=test", zx.resolveDirector("#qs.*-test"));
        assertEquals("?test=test&name=test", zx.resolveDirector("#qs.*-test-name"));
        
        // #request - Needs a dummy HttpRequest object
        // #context - Need to be "connected" to the framework.
        
        assertEquals("#empty", zx.resolveDirector("#empty"));
        
       // #fieldvalue - The value of a field on the form.
        ZXBO bo = zx.createBO("test/clnt");
        zx.getBOContext().setEntry("bert", bo);
        assertEquals("elementByName(window,'ctrTestclntId').value", zx.resolveDirector("#fieldvalue.id"));
        assertEquals("elementByName(window,'ctrTestclntId').value", zx.resolveDirector("#fieldvalue.bert.id"));
        
        // #control - The name of a control on the form.
        assertEquals("ctrTestclntId", zx.resolveDirector("#control.id"));
        assertEquals("ctrTestclntId", zx.resolveDirector("#control.bert.id"));
        assertEquals("ctrTestclntId", zx.resolveDirector("#ctr.id"));
        assertEquals("ctrTestclntId", zx.resolveDirector("#ctr.bert.id"));
        
        // #controldivlock - The name of the div associated with a locked control.
        assertEquals("divctrTestclntId", zx.resolveDirector("#controldivlock.id"));
        assertEquals("divctrTestclntId", zx.resolveDirector("#controldivlock.bert.id"));
        assertEquals("divctrTestclntId", zx.resolveDirector("#ctrdivlock.id"));
        assertEquals("divctrTestclntId", zx.resolveDirector("#ctrdivlock.bert.id"));
        
        // #controlbyname - The handle to a control on the form referred to by name.
        assertEquals("elementByName(window,'ctrTestclntId')", zx.resolveDirector("#controlbyname.id"));
        assertEquals("elementByName(window,'ctrTestclntId')", zx.resolveDirector("#controlbyname.bert.id"));
        assertEquals("elementByName(window,'ctrTestclntId')", zx.resolveDirector("#ctrbyname.id"));
        assertEquals("elementByName(window,'ctrTestclntId')", zx.resolveDirector("#ctrbyname.bert.id"));
        
        // #pf - Get the pageflow name.
        // #pfaction - Get the pageflow current action name.
        // #loopoverok - Result of most recent loopOver action.
        // #loopovererror - Result of most recent loopOver action.
    }
}