/*
 * Created on May 31, 2005
 * $Id$
 */
package org.zxframework;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.Trace} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class TraceTest extends TestCase {
    
    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public TraceTest(String name) {
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
        TestSuite suite = new TestSuite(TraceTest.class);
        suite.setName("Trace Tests");
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
        this.zx.cleanup();
        super.tearDown();
    }
    
    //---------------------------------------------------------- Test constructor 
    
    /**
     * Test the available constructors.
     * 
     * @throws ZXException Thrown if tests fails
     */
    public void testConstructor() throws ZXException {
        assertNotNull(new Trace(zx));
        
        Constructor[] cons = Trace.class.getDeclaredConstructors();
        assertEquals(2, cons.length);
        
        if (Modifier.isPrivate(cons[0].getModifiers())) {
            assertEquals(0, cons[0].getParameterTypes().length);
            assertEquals(1, cons[1].getParameterTypes().length);
            
        } else {
            assertEquals(1, cons[0].getParameterTypes().length);
            assertEquals(0, cons[1].getParameterTypes().length);
            
        }
        
        assertEquals(true, Modifier.isPublic(Trace.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(Trace.class.getModifiers()));
    }
    
    /**
     * @throws Exception Thrown if test failes.
     */
    public void testErrorLogging() throws Exception {
    	Dummy du = new Dummy();
		zXType.runMode runMode = zx.getSettings().getRunMode();
		zx.getSettings().setRunMode(zXType.runMode.rmProduction);
    	try {
        	du.divide(10, 0);
    	} catch (Exception e) {
        	System.out.println(zx.trace.formatStack(false));
    	}
		zx.getSettings().setRunMode(runMode);
    }
    
    /**
     * Dummy Test class 
     */
    public class Dummy extends ZXObject {
    	
    	/**
    	 * Default constructor
    	 */
		public Dummy() {
			super();
		}

		/**
		 * @param numerator The value to divide
		 * @param denominator The value to divide by.
		 * @return Returns result
		 * @throws ZXException Thrown if dividebyzero failes.
		 */
		public int divide(int numerator, int denominator) throws ZXException {
			if (getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.enterMethod();
				getZx().trace.traceParam("numerator", numerator);
				getZx().trace.traceParam("denominator", denominator);
			}

			int divide = 0;
			try {
				
				divide = numerator/denominator;
				
				return divide;
			} catch (Exception e) {
				getZx().trace.addError("Failed to : Perform divide.", e);
				if (getZx().log.isErrorEnabled()) {
					getZx().log.error("Parameter : numerator = " + numerator);
					getZx().log.error("Parameter : denominator = " + denominator);
				}

				if (getZx().throwException) throw new ZXException(e);
				return divide;
			} finally {
				if (getZx().trace.isFrameworkCoreTraceEnabled()) {
					getZx().trace.returnValue(divide);
					getZx().trace.exitMethod();
				}
			}
		}
	}
}