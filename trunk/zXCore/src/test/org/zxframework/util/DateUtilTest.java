/*
 * Created on Aug 30, 2004
 * $Id: DateUtilTest.java,v 1.1.2.2 2006/07/17 16:13:46 mike Exp $
 */
package org.zxframework.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.util.DateUtil} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class DateUtilTest extends TestCase {
    
    /**
     * @param name The name of the test
     */
    public DateUtilTest(String name) {
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
        TestSuite suite = new TestSuite(DateUtilTest.class);
        suite.setName("DateUtil Tests");
        return suite;
    }

    //----------------------------------------------------------------------- Construstor testing
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        assertNotNull(new DateUtil());
        
        Constructor[] cons = DateUtil.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(DateUtil.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DateUtil.class.getModifiers()));
    }
    
    /**
     * Test datediff method
     * @throws ParseException If the date format sucks
     */
    public void testDatediff() throws ParseException {
        
        Date date1 = new Date();
        Date date2 = date1;
        
        assertEquals(0, DateUtil.datediff(DateUtil.MILLI_DIFF, date1, date2));
        assertEquals(0, DateUtil.datediff(DateUtil.SEC_DIFF, date1, date2));
        assertEquals(0, DateUtil.datediff(DateUtil.DAY_DIFF, date1, date2));
        assertEquals(0, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2));
        assertEquals(0, DateUtil.datediff(DateUtil.YEAR_DIFF, date1, date2));
        
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        
        date1 = df.parse("1/12/2005");
        date2 = df.parse("2/12/2005");
        assertEquals(1, DateUtil.datediff(DateUtil.DAY_DIFF, date1, date2));
        
        date1 = df.parse("25/1/2006");
        date2 = df.parse("26/2/2006");
        assertEquals(32, DateUtil.datediff(DateUtil.DAY_DIFF, date1, date2));
        
        date1 = df.parse("25/12/2004");
        date2 = df.parse("5/2/2006");
        assertEquals(13, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2));
        
        date1 = df.parse("5/2/2006");
        date2 = df.parse("25/12/2004");
        assertEquals(-13, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2));
        
        date1 = df.parse("5/2/2006");
        date2 = df.parse("25/12/2004");
        assertEquals(-14, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2, false));

        date1 = df.parse("1/1/2005");
        date2 = df.parse("2/1/2005");
        assertEquals(0, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2, false));

        date1 = df.parse("1/1/2005");
        date2 = df.parse("2/1/2005");
        assertEquals(0, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2));

        date1 = df.parse("5/2/2006");
        date2 = df.parse("5/12/2004");
        assertEquals(-14, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2));

        date1 = df.parse("5/11/2003");
        date2 = df.parse("5/12/2003");
        assertEquals(1, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2));
        
        date1 = df.parse("1/12/2003");
        date2 = df.parse("30/11/2003");
        assertEquals(0, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2));
        
        // Testing leap years :)
        date1 = df.parse("28/2/2005");
        date2 = df.parse("31/1/2005");
        assertEquals(0, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2));

        date1 = df.parse("28/2/2005");
        date2 = df.parse("30/1/2005");
        assertEquals(0, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2));
        
        date1 = df.parse("28/2/2005");
        date2 = df.parse("31/1/2005");
        assertEquals(-28, DateUtil.datediff(DateUtil.DAY_DIFF, date1, date2));
        
        date1 = df.parse("5/12/2003");
        date2 = df.parse("5/11/2003");
        assertEquals(-1, DateUtil.datediff(DateUtil.MONTH_DIFF, date1, date2));
        
        date1 = df.parse("5/12/2004");
        date2 = df.parse("5/12/2005");
        assertEquals(1, DateUtil.datediff(DateUtil.YEAR_DIFF, date1, date2));
        
        date1 = df.parse("1/12/2005");
        date2 = df.parse("1/12/2002");
        assertEquals(-3, DateUtil.datediff(DateUtil.YEAR_DIFF, date1, date2));
        
        date1 = df.parse("1/12/2005");
        date2 = df.parse("2/12/2002");
        assertEquals(-2, DateUtil.datediff(DateUtil.YEAR_DIFF, date1, date2));
        
        date1 = df.parse("1/12/2005");
        date2 = df.parse("2/12/2002");
        assertEquals(-3, DateUtil.datediff(DateUtil.YEAR_DIFF, date1, date2, false));
        
        date1 = df.parse("1/12/2002");
        date2 = df.parse("1/12/2005");
        assertEquals(3, DateUtil.datediff(DateUtil.YEAR_DIFF, date1, date2));

        date1 = df.parse("1/12/2005");
        date2 = df.parse("2/12/2005");
        assertEquals(86400000, DateUtil.datediff(DateUtil.MILLI_DIFF, date1, date2));
        
    }
}