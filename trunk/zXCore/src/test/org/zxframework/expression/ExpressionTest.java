/*
 * Created on 21-Feb-2005 by Michael Brewer
 * $Id: ExpressionTest.java,v 1.1.2.5 2006/07/17 16:13:46 mike Exp $
 */
package org.zxframework.expression;

import java.util.ArrayList;

import org.zxframework.ZX;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Michael Brewer
 */
public class ExpressionTest extends TestCase {
    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public ExpressionTest(String name) {
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
        TestSuite suite = new TestSuite(ExpressionTest.class);
        suite.setName("Expression Tests");
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
     * Tests the parse method
     */
    public void testParse() {
        Expression expr = new Expression();
        
        try {
//            expr.parse("test(123,'helllworld',s()))");
            expr.parse("test(now(), 2123.0,'helllworld',ps.s())");
            
            ExprToken objToken;
            ArrayList arrTokens = expr.getTokens();
            int intTokens = arrTokens.size();
            for (int i = 0; i < intTokens; i++) {
                objToken = (ExprToken)arrTokens.get(i);
                System.out.println(objToken.toString());
            }
            
        } catch (ExpressionParseException e) {
            System.out.println("Failed to parse expression : " + expr.getExpression());
            System.out.println(e.getMessage() + " for token '" + e.getParseErrorToken() + "' at position " + e.getParseErrorPosition());
            e.printStackTrace();
            
        } catch (Exception e1) {
            System.out.println("Unhandled exception " + e1);
        }
    }
}
