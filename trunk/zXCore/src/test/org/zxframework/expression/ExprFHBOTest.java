/*
 * Created on Sep 5, 2004
 * $Id: ExprFHBOTest.java,v 1.1.2.4 2006/07/17 16:13:46 mike Exp $
 */
package org.zxframework.expression;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.ZX;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.expression.ExprFHBO} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ExprFHBOTest extends TestCase {
    
    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public ExprFHBOTest(String name) {
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
        TestSuite suite = new TestSuite(ExprFHBOTest.class);
        suite.setName("ExprFHBO Tests");
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
    
    //------------------------------------------------------- Tests constructors 
    
    /**
     * Test the available constructors.
     */
    public void testConstructor(){
        
        assertNotNull(new ExprFHBO());
        
        Constructor[] cons = ExprFHBO.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        
        assertEquals(true, Modifier.isPublic(ExprFHBO.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(ExprFHBO.class.getModifiers()));
    }
    
    //---------------------------------------------------- Test expressions with 0 parameters 

    /**
     */
    public void testZeroArg() {
        
        assertEquals("true", zx.getExpressionHandler().eval("true()").getStringValue());
        assertEquals("false", zx.getExpressionHandler().eval("false()").getStringValue());
        
    }
    
    //---------------------------------------------------- Test expressions with 1 parameters 

    /**
     */
    public void testOneArg() {
        
        assertEquals("Office", zx.getExpressionHandler().eval("bo.entitylabel('zxOffce')").getStringValue());
        
    }

    //---------------------------------------------------- Test expressions with 2 parameters 

    /**
     * 
     */
    public void testTwoArg() {
        // Ignore
    }
    
    //---------------------------------------------------- Test expressions with any number parameters 
    
    /**
     * @throws ZXException Thrown if testAnyArg fails
     */
    public void testAnyArg() throws ZXException{
        
        //testing : quickload()
        // Check whether it was sucessfully executed
        assertEquals(zXType.rc.rcOK.pos  + "", zx.getExpressionHandler().eval("bo.quickload('zxOffce', 'offce', '2')").getStringValue());
        // Check if it did set the bo context
        assertNotNull(this.zx.getBOContext().getEntry("offce"));
        assertEquals("Create instance of zxOffce and load * where pk = '2' and save as offce",zx.getExpressionHandler().describe("bo.quickload('zxOffce', 'offce', '2')"));
        
        // sumgroup
        assertEquals("0.0", zx.getExpressionHandler().eval("bo.sumgroup('offce', '*')").getStringValue());
        assertEquals("Sum attributes offce.*", zx.getExpressionHandler().describe("bo.sumgroup('offce', '*')"));
        
    }

}