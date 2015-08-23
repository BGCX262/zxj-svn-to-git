/*
 * Created on Apr 1, 2005
 * $Id: ExpressionHandlerTest.java,v 1.1.2.7 2006/07/17 16:13:46 mike Exp $
 */
package org.zxframework.expression;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.ZX;
import org.zxframework.ZXException;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.expression.ExpressionHandler} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ExpressionHandlerTest extends TestCase {

    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public ExpressionHandlerTest(String name) {
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
        TestSuite suite = new TestSuite(ExpressionHandlerTest.class);
        suite.setName("ExpressionHandler Tests");
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
    
    //---------------------------------------------------- Tests constructors 
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        assertNotNull(new ExprFHDefault());
        
        Constructor[] cons = ExpressionHandler.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        
        assertEquals(true, Modifier.isPublic(ExpressionHandler.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(ExpressionHandler.class.getModifiers()));
    }
    
    //----------------------------------------------------- Test new control keywords
    
    /**
     * @throws ZXException Thrown if testControlAND fails.
     */
    public void testControlAND() throws ZXException { 
        
        // Test whether the parsing and then handler is ok.
        assertEquals(true,  zx.getExpressionHandler().eval("_and(true())").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_and(false())").booleanValue());
        assertEquals(true,  zx.getExpressionHandler().eval("_and(true(),true())").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_and(false(),true())").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_and(true(),false())").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_and(true(),'false')").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_and(if(true(),true(),false()),'false')").booleanValue());
        assertEquals(true,  zx.getExpressionHandler().eval("_and(if(true(),true(),false()))").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_and(if(true(),if(true(),false(),true()),false()))").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_and(if(true(),_if(true(),false(),true()),false()))").booleanValue());
        
        // Test whether describe works properly.
        assertEquals("[control]  ( true )  and (false ) ", zx.getExpressionHandler().describe("_and('true','false')"));
    }
    
    /**
     * @throws ZXException Thrown if testControlCHOOSE fails.
     */
    public void testControlCHOOSE() throws ZXException { 
        
        // Test whether the parsing and then handler is ok.
        assertEquals("Hello",        zx.getExpressionHandler().eval("_choose(1, 'Hello')").getStringValue());
        assertEquals("Hello World",  zx.getExpressionHandler().eval("_choose(2, 'Hello','Hello World')").getStringValue());
        
        // 3 option for handling logic of errors in the expression
        // If it is an invalid choose value just live with it, but get some feedback on what went wrong.
        //assertEquals("The first value of a choose function should be greater than 0.",  ((Property)zx.getExpressionHandler().eval("_choose(0, 'Hello','Hello World')")).getStringValue());
        // If it is an invalid choose value just live with it.
        //assertEquals("",  ((Property)zx.getExpressionHandler().eval("_choose(0, 'Hello','Hello World')")).getStringValue());
        
        // Chosen option : 
        // If it is an invalid choose value just live with it, but get some feedback on what went wrong and throw an exception that gets logged.
        // WORKS : but to noisy.
        // assertEquals("Failed to execute function : java.lang.Exception: The first value of a choose function should be greater than 0.",  ((Property)zx.getExpressionHandler().eval("_choose(0, 'Hello','Hello World')")).getStringValue());
        
        // Test whether describe works properly.
        assertEquals("[control] get value number ( 2 ) of  ( Hello )  ( Hello World ) ", zx.getExpressionHandler().describe("_choose(2, 'Hello','Hello World')"));
        assertEquals("[control] get value number ( 1 ) of  ( Hello ) ", zx.getExpressionHandler().describe("_choose(1, 'Hello')"));
        
    }

    /**
     * @throws ZXException Thrown if testControlIF fails.
     */
    public void testControlIF() throws ZXException {
        
        // Test whether the parsing and then handler is ok.
        assertEquals(true, zx.getExpressionHandler().eval("_if(true(),true(),false())").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_if(false(),true(),false())").booleanValue());
        assertEquals("Hello", zx.getExpressionHandler().eval("_if(true(),'Hello',rubbish())").getStringValue());
        
        // Works but to noisy :
        // assertEquals("Failed to execute function : java.lang.ClassNotFoundException: org.zxframework.expression.ExprFHDefault$rubbish", ((Property)zx.getExpressionHandler().eval("_if(false(),'Hello',rubbish())")).getStringValue());
        
        assertEquals("Hello", zx.getExpressionHandler().eval("_if(_and('true'),'Hello','Failed')").getStringValue());
        assertEquals("Hello", zx.getExpressionHandler().eval("_if(_and(true(), true()),'Hello','Failed')").getStringValue());
        assertEquals("Hello", zx.getExpressionHandler().eval("_if(_choose(2, rubbish(), true()),'Hello','Failed')").getStringValue());
        
        // Test whether describe works properly.
        assertEquals("[control] if (true) then (true) else (false)", zx.getExpressionHandler().describe("_if('true','true','false')"));
        
    }

    /**
     * @throws ZXException Thrown if testControlLOOPOVER fails.
     */
    public void testControlLOOPOVER() {
        
        // Test whether the parsing and then handler is ok.
        assertEquals("over", zx.getExpressionHandler().eval("script(iterator('test','over'),_loopover('test',setqs('_',[test])),qs('_'))").getStringValue());
        assertEquals("three", zx.getExpressionHandler().eval("script(_loopover(iterator('iter','one','two','three'),setqs('val',[iter])),qs('val'))").getStringValue());
        assertEquals("over", zx.getExpressionHandler().eval("script(iterator('test',_if('false',dummy(),'over')),_loopover('test',setqs('_',[test])),qs('_'))").getStringValue());
        
        // Test whether describe works properly.
        // NOTE : Need to implement this first.
        
    }
    
    /**
     * @throws ZXException Thrown if testControlOR fails.
     */
    public void testControlOR() throws ZXException { 
        
        // Test whether the parsing and then handler is ok.
        assertEquals(true,  zx.getExpressionHandler().eval("_or(true())").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_or(false())").booleanValue());
        assertEquals(true, zx.getExpressionHandler().eval("_or(false(),false(),'false',,true(),random())").booleanValue());
        assertEquals(true,  zx.getExpressionHandler().eval("_or(true(),true())").booleanValue());
        assertEquals(true, zx.getExpressionHandler().eval("_or(false(),true())").booleanValue());
        assertEquals(true, zx.getExpressionHandler().eval("_or(true(),false())").booleanValue());
        assertEquals(true, zx.getExpressionHandler().eval("_or(true(),'false')").booleanValue());
        assertEquals(true, zx.getExpressionHandler().eval("_or(if(true(),true(),false()),'false')").booleanValue());
        assertEquals(true,  zx.getExpressionHandler().eval("_or(if(true(),true(),false()))").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_or(if(true(),if(true(),false(),true()),false()))").booleanValue());
        assertEquals(false, zx.getExpressionHandler().eval("_or(if(true(),_if(true(),false(),true()),false()))").booleanValue());
        
        // Test whether describe works properly.
        assertEquals("[control]  ( true )  or (false ) ", zx.getExpressionHandler().describe("_or('true','false')"));
        
    }

    /**
     * @throws ZXException Thrown if testControlSELECT fails.
     */
    public void testControlSELECT() throws ZXException { 
        
        // Test whether the parsing and then handler is ok.
        assertEquals("Hello", zx.getExpressionHandler().eval("_select(true(),'Hello')").getStringValue());
        assertEquals("Hello", zx.getExpressionHandler().eval("_select(true(),'Hello', random(), random())").getStringValue());
        assertEquals("Hello", zx.getExpressionHandler().eval("_select(false(),random(), true(), 'Hello')").getStringValue());
        
        // Test whether describe works properly.
        assertEquals("[control] if ( true )  then ( false ) ", zx.getExpressionHandler().describe("_select('true','false')"));
        assertEquals("[control] if ( true )  then ( false )  else if ( false )  then ( true ) ", zx.getExpressionHandler().describe("_select('true','false','false','true')"));
        
    }
    
    /**
     * @throws ZXException Thrown if testControlSELECT fails.
     */
    public void testEval() throws ZXException {
    	zx.getBOContext().setEntry("zXMe", zx.createBO("test/clntAcctGrp"));
    	
    	String strExpr = "script(" +
    						"bo.create('test/clntAcctGrp', 'cag'), " +
    						"bo.setAttr('cag', 'prnt', bo.attr('zXMe', 'id')), " +
    						"bo.load('cag', 'id', 'prnt'), " +
    						"if(isNull(bo.attr('cag', 'id')), '', '<img src=../images/smallGreenTick.gif>')" +
    					 ")";
    	
		zx.getExpressionHandler().eval(strExpr);
		
    }
    
    /**
     * @throws ZXException Thrown if testCompress fails.
     */
    public void testCompress() throws ZXException {
    	String strExpression = "if(eq(now(), \"today\"), \"hello\", \"bye\")";
    	assertEquals(strExpression, zx.getExpressionHandler().compress(strExpression));
    }
    
    /**
     * @throws ZXException Thrown if testCompress fails.
     */
    public void testUnCompress() throws ZXException {
    	String strExpression = "if(eq(now(), \"today\"), \"hello\", \"bye\")";
    	String strUncompress = "if(<nl><i>eq(<nl><i><i>now(<nl><i><i>)<nl><i><i>, \"today\"<nl><i>)<nl><i>, \"hello\"<nl><i>, \"bye\"<nl>)";
    	assertEquals(strUncompress, zx.getExpressionHandler().unCompress(strExpression, "<nl>", "<i>"));
    }
}