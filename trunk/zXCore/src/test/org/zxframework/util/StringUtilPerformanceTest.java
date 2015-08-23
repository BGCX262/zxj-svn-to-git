/*
 * Created on May 21, 2005
 * $Id: StringUtilPerformanceTest.java,v 1.1.2.1 2005/05/22 18:06:47 mike Exp $
 */
package org.zxframework.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.vladium.utils.timing.ITimer;
import com.vladium.utils.timing.TimerFactory;

/**
 * Unit tests {@link org.zxframework.util.StringUtil} methods performance.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class StringUtilPerformanceTest extends TestCase {
    
    //-------------------------------- Members
    
    private static ITimer timer;
    static {
        timer = TimerFactory.newTimer ();
    }
    
    //--------------------------------- Constructors.
    
    /**
     * @param name The name of the test
     */
    public StringUtilPerformanceTest(String name) {
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
        TestSuite suite = new TestSuite(StringUtilPerformanceTest.class);
        suite.setName("StringUtilPerformanceTest Tests");
        return suite;
    }
    
    //------------------------------------------ Performance tests.
    
    private final int COUNT_SML = 10000;
    private final int COUNT_LRG = 1000000;
    
    /**
     * Tests the performance of StringUtil.split versus String.split.
     */
    public void testSplit() {
        
        String strSplit = "one,two,three,four,five,six,seven,eight,nine,ten";
        String[] arrSplit1 = null;
        String[] arrSplit2 = null;

        timer.start();
        for (int i = 0; i < COUNT_SML; i++) {
            arrSplit1 = StringUtil.split(",", strSplit);
        }
        timer.stop();
        System.out.println("StringUtil.split [" + COUNT_SML + "] " + timer.getDuration());
        timer.reset();
        
        timer.start();
        for (int i = 0; i < COUNT_SML; i++) {
            arrSplit2 = strSplit.split(",");
        }
        timer.stop();
        System.out.println("String.split [" + COUNT_SML + "] " + timer.getDuration());
        timer.reset();
        
        assertEquals(true, ArrayUtil.isEquals(arrSplit1, arrSplit2));
    }
    
    /**
     * Tests the replaceAll performance.
     */
    public void testReplaceAll() {
        
        timer.start();
        for (int i = 0; i < COUNT_LRG; i++) {
            assertEquals("19191919", StringUtil.replaceAll("10101010", '0', '9'));
        }
        timer.stop();
        System.out.println("StringUtil.replaceAll(str,chr,chr) [" + COUNT_LRG + "] " + timer.getDuration());
        timer.reset();

        timer.start();
        for (int i = 0; i < COUNT_LRG; i++) {
            assertEquals("19191919", StringUtil.replaceAll("10101010", '0', "9"));
        }
        timer.stop();
        System.out.println("StringUtil.replaceAll(str,chr,str) [" + COUNT_LRG + "] " + timer.getDuration());
        timer.reset();
        
        timer.start();
        for (int i = 0; i < COUNT_LRG; i++) {
            assertEquals("19191919", StringUtil.replaceAll("10101010", "0", "9"));
        }
        timer.stop();
        System.out.println("StringUtil.replaceAll(str,str,str) [" + COUNT_LRG + "] " + timer.getDuration());
        timer.reset();    
        
        timer.start();
        for (int i = 0; i < COUNT_LRG; i++) {
            assertEquals("19191919", "10101010".replaceAll("0", "9"));
        }
        timer.stop();
        System.out.println("str.replaceAll(str,str) [" + COUNT_LRG + "] " + timer.getDuration());
        timer.reset();
        
    }
    
    
}
