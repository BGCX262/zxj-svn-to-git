/*
 * Created on Sep 8, 2004
 */
package org.zxframework;

import com.vladium.utils.timing.ITimer;
import com.vladium.utils.timing.TimerFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.ZX} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class zXTypeTest extends TestCase {
    
    String strRCResult = "Warning";
    String strDatatypeResult = "Double";
    
    zXType.rc enumRC = zXType.rc.rcError;
    zXType.dataType enumDataType = zXType.dataType.dtTime;

    final ITimer time = TimerFactory.newTimer();

    /**
     * @param name The name of the test suite.
     */
    public zXTypeTest(String name) {
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
        TestSuite suite = new TestSuite(zXTypeTest.class);
        suite.setName("zXType Tests");
        return suite;
    }
    
    //--------------------------------------------- Test performance :
    
    /**
     * Tests speed of ==
     */
    public void testPerformanceOfEqualsPos() {
        
        time.start();
        int intRC = enumRC.pos;
        int intDataType = enumDataType.pos;
        for (int i = 0; i < 100000; i++) {
            if (intRC == zXType.rc.rcWarning.pos) {
                strRCResult = "Warning";
            } else if (intRC == zXType.rc.rcOK.pos){
                strRCResult = "Ok";
            } else if (intRC == zXType.rc.rcError.pos){
                strRCResult = "Error";
            }
            if (intDataType == zXType.dataType.dtLong.pos) {
                strDatatypeResult = "Long";
            } else if (intDataType == zXType.dataType.dtTime.pos){
                strDatatypeResult = "Time";
            }
        }
        time.stop();
        System.out.println("pos = " + time.getDuration());
        time.reset();
        
        assertEquals("Error", strRCResult);
        assertEquals(zXType.rc.rcError.pos, enumRC.pos);
        assertEquals(zXType.rc.rcError, enumRC);
        
        assertEquals("Time", strDatatypeResult);
        assertEquals(zXType.dataType.dtTime.pos, enumDataType.pos);
        assertEquals(zXType.dataType.dtTime, enumDataType);
    }
    
    /**
     * Tests speed of nornal .equals
     */
    public void testPerformanceOfEquals() {
        
        time.start();
        for (int i = 0; i < 100000; i++) {
            if (enumRC.equals(zXType.rc.rcWarning)) {
                strRCResult = "Warning";
            } else if (enumRC.equals(zXType.rc.rcError)){
                strRCResult = "Error";
            } else if (enumRC.equals(zXType.rc.rcOK)){
                strRCResult = "Ok";
            }
            
            if (enumDataType.equals(zXType.dataType.dtLong)) {
                strDatatypeResult = "Long";
            } else if (enumDataType.equals(zXType.dataType.dtTime)){
                strDatatypeResult = "Time";
            }
        }
        time.stop();
        System.out.println("equals = " + time.getDuration());
        time.reset();
        
        assertEquals("Error", strRCResult);
        assertEquals(zXType.rc.rcError.pos, enumRC.pos);
        assertEquals(zXType.rc.rcError, enumRC);
        
        assertEquals("Time", strDatatypeResult);
        assertEquals(zXType.dataType.dtTime.pos, enumDataType.pos);
        assertEquals(zXType.dataType.dtTime, enumDataType);
    }
    
}