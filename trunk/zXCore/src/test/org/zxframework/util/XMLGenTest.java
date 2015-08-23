/*
 * Created on Aug 30, 2004
 * $Id: XMLGenTest.java,v 1.1.2.1 2005/05/12 15:52:43 mike Exp $
 */
package org.zxframework.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.util.XMLGen} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class XMLGenTest extends TestCase {
    
    /**
     * Constructor for XMLGenTest.
     * @param name The name of the test suite.
     */
    public XMLGenTest(String name) {
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
        
        TestSuite suite = new TestSuite(XMLGenTest.class);
        suite.setName("XMLGen Tests");
        
        return suite;
    }
    
    //--------------------------------------------------------- Constructor tests
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        
        assertNotNull(new XMLGen());
        
        Constructor[] cons = XMLGen.class.getDeclaredConstructors();
        assertEquals(2, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(cons[1].getModifiers()));
        assertEquals(true, Modifier.isPublic(XMLGen.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(XMLGen.class.getModifiers()));
    }
    
    /**
     * Testing the openTag method
     */
    public void testOpenTag() {
        
        XMLGen objXMLGen = new XMLGen();
        
        objXMLGen.openTag("test");
        assertEquals("<test>\n", objXMLGen.getXML());
        
        objXMLGen.openTag(null);
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.openTag("");
        assertEquals("", objXMLGen.getXML());
    }
    
    /**
     * Testing the closeTag method
     */
    public void testCloseTag() {
        
        XMLGen objXMLGen = new XMLGen();
        
        objXMLGen.closeTag("test");
        assertEquals("</test>\n", objXMLGen.getXML());
        
        objXMLGen.closeTag(null);
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.closeTag("");
        assertEquals("", objXMLGen.getXML());
        
    }
    
    /**
     * Testing the openTag\closeTag method combination
     */
    public void testCloseOpenTag() {
        
        XMLGen objXMLGen = new XMLGen();
        
        objXMLGen.openTag("test");
        objXMLGen.closeTag("test");
        assertEquals("<test>\n</test>\n", objXMLGen.getXML());
        
        objXMLGen.openTag("");
        objXMLGen.closeTag("");
        assertEquals("", objXMLGen.getXML());
        
    }
    
    /**
     * Testing the openTag method
     */
    public void testOpenTagWithAttr() {
        
        XMLGen objXMLGen = new XMLGen();
        
        objXMLGen.openTagWithAttr(null, null, null);
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.openTagWithAttr("", null, null);
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.openTagWithAttr("test", null, null);
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.openTagWithAttr("test", "name", null);
        assertEquals("<test name=\"\">\n", objXMLGen.getXML());
        
        objXMLGen.openTagWithAttr("test", "name", "value");
        assertEquals("<test name=\"value\">\n", objXMLGen.getXML());
        
    }
    
    /**
     * Testing the taggedValue method combination
     */
    public void testTaggedValue() {
        
        XMLGen objXMLGen = new XMLGen();
        
        objXMLGen.taggedValue(null, false);
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.taggedValue("", false);
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.taggedValue("", "value");
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.taggedValue("", "");
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.taggedValue("test", true);
        assertEquals("<test>true</test>\n", objXMLGen.getXML());
        
        objXMLGen.taggedValue("test", null, true);
        assertEquals("<test></test>\n", objXMLGen.getXML());

        objXMLGen.taggedValue("test", "", true);
        assertEquals("<test></test>\n", objXMLGen.getXML());

        objXMLGen.taggedValue("test", null);
        assertEquals("", objXMLGen.getXML());
        
    }
    
    /**
     * Testing the taggedCData method combination
     */
    public void testTaggedCData() {
        
        XMLGen objXMLGen = new XMLGen();
        
        objXMLGen.taggedCData(null, "value");
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.taggedCData("", "value");
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.taggedCData("test", "value");
        assertEquals("<test><![CDATA[value]]></test>\n", objXMLGen.getXML());
        
        objXMLGen.taggedCData("test", null, true);
        assertEquals("<test><![CDATA[]]></test>\n", objXMLGen.getXML());

        objXMLGen.taggedCData("test", "", true);
        assertEquals("<test><![CDATA[]]></test>\n", objXMLGen.getXML());

        objXMLGen.taggedCData("test", null);
        assertEquals("", objXMLGen.getXML());
        
    }
    
    /**
     * Test taggedValueWithAttr
     */
    public void testTaggedValueWithAttr() {
        
        XMLGen objXMLGen = new XMLGen();
        
        objXMLGen.taggedValueWithAttr(null, null, null, null);
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.taggedValueWithAttr("test", null, null, null);
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.taggedValueWithAttr("test", null, "attrname", null);
        assertEquals("<test attrname=\"\"></test>\n", objXMLGen.getXML());
        
        objXMLGen.taggedValueWithAttr("test", "value", "attrname", "attrvalue");
        assertEquals("<test attrname=\"attrvalue\">value</test>\n", objXMLGen.getXML());

    }
    
    /**
     * Test taggedValueWithAttr
     */
    public void testTaggedCDataWithAttr() {
        
        XMLGen objXMLGen = new XMLGen();
        
        objXMLGen.taggedCDataWithAttr(null, null, null, "test");
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.taggedCDataWithAttr("test", null, null, "test");
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.taggedCDataWithAttr("test", null, "attrname", null);
        assertEquals("<test attrname=\"\"><![CDATA[]]></test>\n", objXMLGen.getXML());
        
        objXMLGen.taggedCDataWithAttr("test", "value", "attrname", "attrvalue");
        assertEquals("<test attrname=\"attrvalue\"><![CDATA[value]]></test>\n", objXMLGen.getXML());
        
    }
    
    /**
     * Test addCustomContent.
     */
    public void testAddCustomContent() {
        
        XMLGen objXMLGen = new XMLGen();
        
        objXMLGen.addCustomContent(null);
        assertEquals("", objXMLGen.getXML());
        
        objXMLGen.addCustomContent("test");
        assertEquals("test", objXMLGen.getXML());
        
    }
    
    /**
     * Test the equals method
     */
    public void testEquals() {
        
        XMLGen objXMLGen = new XMLGen();
        objXMLGen.xmlHeader();
        objXMLGen.taggedValue("test", "value");
        
        XMLGen objXMLGen2 = new XMLGen();
        objXMLGen2.xmlHeader();
        objXMLGen2.taggedValue("test", "value");
        
        assertEquals(true, objXMLGen.equals(objXMLGen2));
        
    }
    
    /**
     * Test the various method
     */
    public void testXMLGen() {
        
        XMLGen objXMLGen = new XMLGen(100);
        objXMLGen.xmlHeader();
        objXMLGen.openTag("test");
        objXMLGen.taggedValueWithAttr("xmlnode", "value", "attrname", "attrvalue");
        objXMLGen.closeTag("test");
        
        assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<test>\n\t<xmlnode attrname=\"attrvalue\">value</xmlnode>\n</test>\n", objXMLGen.getXML());
    }
}
