package org.zxframework.web;

import java.util.Calendar;
import java.util.Date;

import org.zxframework.ZX;
import org.zxframework.util.DateUtil;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests {@link org.zxframework.web.PFCalendar}.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class PFCalendarTest extends TestCase {

	private ZX zx;

	/**
	 * @param name Name of the unit test.
	 */
	public PFCalendarTest(String name) {
		super(name);
	}

	/**
	 * @param args Arguments.
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(PFDirectorTest.class);
	}

	/**
	 * @return Returns the test to run
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(PFDirectorTest.class);
		suite.setName("PFCalendar Tests");
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

	// -------------------------- Testing logic for getStartOfWeek()
	
	/**
	 * Calculate the first day of the week within the week of a month
	 */
	public void testGetStartOfWeek() {
		Date d = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		
		System.out.println(DateUtil.getRealDayofWeek(Calendar.MONDAY, cal.getTime()));
	}
	
}
