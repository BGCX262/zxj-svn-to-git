package org.zxframework.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Tests for the {@link org.zxframework.util.ToStringBuilder} class.
 *  
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @version 0.01
 */
public class ToStringBuilderTest extends TestCase {
	
    private int _originalDefaultMode;

    /**
     * @param name The name of the test
     */
    public ToStringBuilderTest(String name) {
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
        TestSuite suite = new TestSuite(ToStringBuilderTest.class);
        suite.setName("ToStringBuilder Tests");
        return suite;
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ToStringBuilder.defaultMode = _originalDefaultMode;
    }

    protected void setUp() throws Exception {
    	super.setUp();
        _originalDefaultMode = ToStringBuilder.defaultMode;
    }
    
    //------------------------ Tests

    /**
     * Tests building a toString for a null object.
     */
    public void testNull() {
        try {
            new ToStringBuilder(null);
            TestUtil.unreachable();
        } catch (NullPointerException ex) {
        	// Ignore
        }
    }

    /**
     * Tests simple used of the toStrinBuilder.
     */
    public void testSimple() {
        ToStringBuilder b = new ToStringBuilder(this);
        assertEquals("ToStringBuilderTest", b.toString());

        try {
            b.toString();
            TestUtil.unreachable();
        } catch (NullPointerException ex) {
            // Can't invoke toString() twice!
        }
    }

    /**
     * Tests building of a hashcode.
     */
    public void testWithHashCode() {
        ToStringBuilder b = new ToStringBuilder(this, ToStringBuilder.INCLUDE_HASHCODE);
        assertEquals("ToStringBuilderTest@" + Integer.toHexString(hashCode()), b.toString());
    }

    /**
     * Tests setting the toString mode.
     */
    public void testSetDefault() {
        int mode = ToStringBuilder.INCLUDE_HASHCODE | ToStringBuilder.INCLUDE_PACKAGE_PREFIX;
        ToStringBuilder b1 = new ToStringBuilder(this, mode);
        ToStringBuilder.defaultMode = mode;
        ToStringBuilder b2 = new ToStringBuilder(this);
        assertEquals(b1.toString(), b2.toString());
    }

    /**
     * Tests appending a String field.
     */
    public void testAppendString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.append("fred", "flintstone");
        assertEquals("ToStringBuilderTest [fred='flintstone']", b.toString());
    }

    /**
     * Tests appending a Null String field.
     */
    public void testAppendNullString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.append("attr", (String) null);
        assertEquals("ToStringBuilderTest [attr=null]", b.toString());
    }

    /**
     * Tests appending of 2 fields.
     */
    public void testAppendTwo() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.append("fred", "flintstone");
        b.append("barney", "rubble");
        assertEquals("ToStringBuilderTest [fred='flintstone',barney='rubble']", b.toString());
    }

    /**
     * Tests appending of a object.
     */
    public void testAppendObject() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.append("number", new Integer(27));
        assertEquals("ToStringBuilderTest [number=27]", b.toString());
    }

    /**
     * Tests appending of a null object.
     */
    public void testAppendNullObject() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.append("object", null);
        assertEquals("ToStringBuilderTest [object=null]", b.toString());
    }

    /**
     * Tests appending of a boolean.
     */
    public void testAppendBoolean() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.append("yes", true);
        b.append("no", false);
        assertEquals("ToStringBuilderTest [yes=true,no=false]", b.toString());
    }

    /**
     * Tests appending of a byte.
     */
    public void testAppendByte() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.append("byte", (byte)32);
        assertEquals("ToStringBuilderTest [byte=32]", b.toString());
    }

    /**
     * Tests appending of a short.
     */
    public void testAppendShort() {
        ToStringBuilder b = new ToStringBuilder(this);

        b.append("short", (short)-37);
        assertEquals("ToStringBuilderTest [short=-37]", b.toString());
    }

    /**
     * Tests appending of an int.
     */
    public void testAppendInt() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.append("int", 217);
        assertEquals("ToStringBuilderTest [int=217]", b.toString());
    }

    /**
     * Tests appending of an long.
     */
    public void testAppendLong() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.append("long", (long)217);
        assertEquals("ToStringBuilderTest [long=217]", b.toString());
    }
}