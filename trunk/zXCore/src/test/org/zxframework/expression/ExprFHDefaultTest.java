/*
 * Created on Sep 5, 2004
 * $Id: ExprFHDefaultTest.java,v 1.1.2.8 2006/01/27 18:12:57 mike Exp $
 */
package org.zxframework.expression;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.ZX;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.expression.ExprFHDefault} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ExprFHDefaultTest extends TestCase {

    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public ExprFHDefaultTest(String name) {
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
        TestSuite suite = new TestSuite(ExprFHDefaultTest.class);
        suite.setName("ExprFHDefault Tests");
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
    
    //------------------------ Tests constructors 
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        assertNotNull(new ExprFHDefault());
        
        Constructor[] cons = ExprFHDefault.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        
        assertEquals(true, Modifier.isPublic(ExprFHDefault.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(ExprFHDefault.class.getModifiers()));
    }
    
    //------------------------ Test expressions with 0 parameters 
    
    /**
	 * Tests expressions with no arguments.
	 */
    public void testNoArgs() {
        
        assertEquals("", zx.getExpressionHandler().eval("null()").getStringValue());
        assertEquals(true, zx.getExpressionHandler().eval("null()").isNull);
        
        assertEquals("\r", zx.getExpressionHandler().eval("cr()").getStringValue());
        
        //assertEquals(new Date().toString(), ((Property)zx.getExpressionHandler().eval("now()")).dateValue().toString());
        
        zx.trace.addError("test");
        assertEquals("test", zx.getExpressionHandler().eval("systemmsg()").getStringValue());
        
    }
    
    //------------------------ Test expressions with 2 parameters 
    
    /**
	 * Test expressions with a single argument.
	 */
    public void testOneArgs() {
        // char(3) - > 3
        assertEquals("3", zx.getExpressionHandler().eval("chr(3)").getStringValue());
        
        assertEquals(true, zx.getExpressionHandler().eval("setqs('test','hello')").booleanValue());
        assertEquals("hello", zx.getExpressionHandler().eval("qs('test')").getStringValue());
        assertEquals(true, zx.getExpressionHandler().eval("setqs('test','')").booleanValue());
        assertEquals(true, zx.getExpressionHandler().eval("qs('test')").isNull);
        
        assertEquals(10, zx.getExpressionHandler().eval("add(5,5)").longValue());
        assertEquals(5.5, zx.getExpressionHandler().eval("add(5,0.5)").doubleValue(), 0.0);
        
        assertEquals(25, zx.getExpressionHandler().eval("mul(5,5)").longValue());
        assertEquals(2.5, zx.getExpressionHandler().eval("mul(5,0.5)").doubleValue(), 0.0);
        
        assertEquals(0, zx.getExpressionHandler().eval("sub(5,5)").longValue());
        assertEquals(4.5, zx.getExpressionHandler().eval("sub(5,0.5)").doubleValue(), 0.0);
        
        assertEquals(1, zx.getExpressionHandler().eval("div(5,5)").longValue());
        assertEquals(10, zx.getExpressionHandler().eval("div(5,0.5)").doubleValue(), 0.0);
        
        assertEquals("Hello", zx.getExpressionHandler().eval("getline('Hello',0)").getStringValue());
        assertEquals("Hello", zx.getExpressionHandler().eval("getline('Hello',1)").getStringValue());
        
        assertEquals("Hello", zx.getExpressionHandler().eval("getline('Hello','-1')").getStringValue());
        assertEquals("Hello", zx.getExpressionHandler().eval("getline('Hello',-1)").getStringValue());
        assertEquals("World", zx.getExpressionHandler().eval("getline('Hello\nWorld','-1')").getStringValue());
        
        // getword
        assertEquals("", zx.getExpressionHandler().eval("getword('', 0, ' ')").getStringValue());
        assertEquals("", zx.getExpressionHandler().eval("getword('', 2, ' ')").getStringValue());
        assertEquals("", zx.getExpressionHandler().eval("getword('', -1)").getStringValue());
        assertEquals("t", zx.getExpressionHandler().eval("getword('t', -1, ' ')").getStringValue());
        assertEquals("t", zx.getExpressionHandler().eval("getword('t', 1)").getStringValue());
        assertEquals("", zx.getExpressionHandler().eval("getword('testing', 2, ' ')").getStringValue());
        assertEquals("", zx.getExpressionHandler().eval("getword('testing', -2, ' ')").getStringValue());
        assertEquals("testing", zx.getExpressionHandler().eval("getword('testing', 1, ' ')").getStringValue());
        assertEquals("yes", zx.getExpressionHandler().eval("getword('yes test', 1, ' ')").getStringValue());
        assertEquals("test", zx.getExpressionHandler().eval("getword('yes test', 2, ' ')").getStringValue());
        assertEquals("four", zx.getExpressionHandler().eval("getword('yes test one four you', 4, ' ')").getStringValue());
    }

    //------------------------ Test expressions with 2 parameters 

    /**
     * Tests the stripchars expression.
     */
    public void testStripchars() {
        
        assertEquals("test", zx.getExpressionHandler().eval("stripchars('t1e2s3t4', '[1234]')").getStringValue());
        // Same as stripnonxchars('t-e!s.t@', '[a-zA-Z0-9]')
        assertEquals("test", zx.getExpressionHandler().eval("stripchars('t-e!s.t@', '[^a-zA-Z0-9]')").getStringValue()); 
        
    }
    
    //------------------------ Test variable parameters
    
    /**
     * Test expressions with variable arguements
	 */
    public void testVarArgs() {
        
        // choose(3, 'a', 'b', 'c', 'd') - > c
        assertEquals("c", zx.getExpressionHandler().eval("choose(3, 'a', 'b', 'c', 'd')").getStringValue());
        
        // translate(1, 0, 'a', 1, 'b', 2, 'c', 3, 'd') - > b
        assertEquals("b", zx.getExpressionHandler().eval("translate(1, 0, 'a', 1, 'b', 2, 'c', 3, 'd')").getStringValue());
        assertEquals("charlie", zx.getExpressionHandler().eval("translate('c', 'a', 'alpha', 'b', 'bravo', 'c', 'charlie', 'd', 'delta')").getStringValue());
        
        assertEquals("abcd", zx.getExpressionHandler().eval("concat('a','b','c','d')").getStringValue());
        assertEquals("12)4", zx.getExpressionHandler().eval("concat('1', '2', ')', '4')").getStringValue());
        
    }
    
//    /**
//     * Test expression language support of groovy
//     */
//    public void testGroovy() {
//        assertEquals("Hello World", zx.getExpressionHandler().eval("groovy('\"Hello World\"')").getStringValue());
//    }
}