/*
 * Created on 03-Feb-2005, by Michael Brewer
 * $Id: ExprFHPageflowTest.java,v 1.1.2.2 2006/07/17 14:04:04 mike Exp $
 */
package org.zxframework.web.expression;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.ZX;
import org.zxframework.util.TestUtil;
import org.zxframework.web.PageBuilder;
import org.zxframework.web.Pageflow;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class ExprFHPageflowTest extends TestCase {

    private ZX zx;
    
    /**
     * Constructor for ExprFHPageflowTest.
     * @param name Name of the unit test.
     */
    public ExprFHPageflowTest(String name) {
        super(name);
    }

    /**
     * @param args Test arguments.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ExprFHPageflowTest.class);
    }
    
    /**
     * @return Returns the test to run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ExprFHPageflowTest.class);
        suite.setName("ExprFHPageflow Tests");
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
    
    //------------------------------------------------------------------- Tests constructors 
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        Pageflow objPageflow = new Pageflow();
        assertNotNull(new ExprFHPageflow(objPageflow));
        
        Constructor[] cons = ExprFHPageflow.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        
        assertEquals(true, Modifier.isPublic(ExprFHPageflow.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(ExprFHPageflow.class.getModifiers()));
    }
    
    /**
     * Test the evailable expressions
     */
    public void testExpressions() {
        Pageflow objPageflow = new Pageflow();
        PageBuilder objPage = new PageBuilder();
        objPageflow.setPage(objPage);
        ExprFHPageflow objExprPageflow = new ExprFHPageflow(objPageflow);
        zx.getExpressionHandler().registerFH("pf", objExprPageflow);
        
        // qs - Get a value from query string.
        objPageflow.getQs().setEntry("test", "hello");
        assertEquals("hello", (zx.getExpressionHandler().eval("pf.qs('test')")).getStringValue());
        assertEquals(true, (zx.getExpressionHandler().eval("pf.qs('test1')")).isNull);
        
        // req - Get a value from the request .
        
        // encode - Encode a string for proper Javascript / HTML use .
        assertEquals("test", (zx.getExpressionHandler().eval("pf.encode('test')")).getStringValue());
        assertEquals("test%2Fone", (zx.getExpressionHandler().eval("pf.encode('test/one')")).getStringValue());
        assertEquals(false, (zx.getExpressionHandler().eval("pf.encode('test')")).isNull);
        assertEquals("", (zx.getExpressionHandler().eval("pf.encode('')")).getStringValue());
        assertEquals(true, (zx.getExpressionHandler().eval("pf.encode('')")).isNull);
        
        // jsencode - Encode a string for proper Javascript / HTML use
        assertEquals("test", (zx.getExpressionHandler().eval("pf.jsencode('test')")).getStringValue());
        assertEquals("test/one", (zx.getExpressionHandler().eval("pf.jsencode('test/one')")).getStringValue());
        assertEquals("\\u00A3one", (zx.getExpressionHandler().eval("pf.jsencode('£one')")).getStringValue());
        assertEquals(false, (zx.getExpressionHandler().eval("pf.jsencode('test')")).isNull);
        assertEquals("", (zx.getExpressionHandler().eval("pf.jsencode('')")).getStringValue());
        assertEquals(true, (zx.getExpressionHandler().eval("pf.jsencode('')")).isNull);
        
        //bolabel - Get label value for BO from context or create a new one.
        assertEquals("BERTUS DISPA", (zx.getExpressionHandler().eval("pf.bolabel('test/clnt','1')")).getStringValue());
        
        // havelock - Check whether a lock exists for the given entity.
    }
}