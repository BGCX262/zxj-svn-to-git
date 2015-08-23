/*
 * Created on 20-Feb-2005
 * $Id: ToStringTest.java,v 1.1.2.3 2005/06/17 20:16:22 mike Exp $
 */
package org.zxframework.util;

import java.util.ArrayList;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test the toString Utils.
 * 
 * Unit tests {@link org.zxframework.util.StringUtil} methods.
 */
public class ToStringTest extends TestCase {
    
    /**
     * @param name The name of the test
     */
    public ToStringTest(String name) {
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
        TestSuite suite = new TestSuite(ToStringTest.class);
        suite.setName("ToString Tests");
        return suite;
    }
    
    //------------------------------------- Tests
    
    /**
     * Tests stripPackageName
     */
    public void testPackageStripping() {
        assertEquals("Integer", StringUtil.stripPackageName(Integer.class));
        assertEquals("Map.Entry", StringUtil.stripPackageName(Map.Entry.class));
        assertEquals("ToStringTest", StringUtil.stripPackageName(ToStringTest.class));
    }
    
    /**
     * Tests the NullConverter.
     */
    public void testNull() {
        assertEquals("(null)", ToStringFacade.toString(null));
    }

    /**
     * Tests support for Intergers.
     */
    public void testInteger() {
        assertEquals("Integer [value=42]", ToStringFacade.toString(new Integer(
                42)));
    }

    /**
     * Tests support for strings.
     */
    public void testString() {
        assertEquals("String [value=char[] [{H,e,l,l,o, ,W,o,r,l,d,!}], "
                + "offset=0, count=12, hash=0]", ToStringFacade
                .toString("Hello World!"));
    }

    /**
     * Tests support for arrays.
     */
    public void testArray() {
        assertEquals("int[] [{1,2,3}]", ToStringFacade.toString(new int[] { 1,
                2, 3 }));
    }

    /**
     * Tests support for multidimensional arrays.
     */
    public void testMultiArray() {
        assertEquals("long[][][][] [{{{{1,2,3},{4}},{{5}}}}]", ToStringFacade
                .toString(new long[][][][] { { { { 1, 2, 3 }, { 4 } },
                        { { 5 } } } }));
    }

    /**
     * Tests support for classes.
     */
    public void testTestClass() {
        assertEquals("ToStringTest.TestClass [names="
                     + "String[] [{\"Heinz\",\"Joern\",\"Pieter\",\"Herman\""
                     + ",\"John\"}], totalIQ=900]", ToStringFacade
                     .toString(new TestClass()));
    }
    
    /**
     * Used by testTestClass
     */
    private static class TestClass {
        private final String[] names = { "Heinz", "Joern", "Pieter", "Herman", "John" };
        private final int totalIQ = 180 * 5;
    }
    
    /**
     * Tests support for collections.
     */
    public void testArrayList() {
        ArrayList list = new ArrayList();
        list.add("Heinz");
        list.add("Helene");
        list.add("Maxi");
        list.add("Connie");
        assertEquals("ArrayList [elementData=Object[] [{\"Heinz\""
                + ",\"Helene\",\"Maxi\",\"Connie\",(null),(null),(null),"
                + "(null),(null),(null)}], size=4, AbstractList.modCount=4]",
                ToStringFacade.toString(list));
    }
    
}