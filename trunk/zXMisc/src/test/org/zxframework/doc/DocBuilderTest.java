/*
 * Created on Jun 12, 2005
 * $Id: DocBuilderTest.java,v 1.1.2.5 2006/07/17 16:09:23 mike Exp $
 */
package org.zxframework.doc;

import org.zxframework.ZX;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.doc.DocBuilder} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class DocBuilderTest extends TestCase {
	
    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public DocBuilderTest(String name) {
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
        TestSuite suite = new TestSuite(DocBuilderTest.class);
        suite.setName("DocBuilder Tests");
        return suite;
    }
    
    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        zx = new ZX(TestUtil.getCfgPath());
        zx.getSession().connect("test","test");
        
        super.setUp();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    //------------------------  Tests
    
    /**
     */
    public void testStartDoc() {
    	DocBuilder objDocBuilder = new DocBuilder();
    	
    	assertEquals(zXType.rc.rcOK, objDocBuilder.startDoc("test/doc"));
    	assertEquals(zXType.rc.rcOK, objDocBuilder.startDoc("test/printAttachment"));
    }
    
    /**
     * @throws ZXException Thrown if test fails.
     */
    public void testBuildDoc() throws ZXException {
    	DocBuilder objDocBuilder = new DocBuilder();
    	
    	try {
	    	
	    	assertEquals(zXType.rc.rcOK, objDocBuilder.startDoc("test/printAttachment"));
	    	assertEquals(zXType.rc.rcOK, objDocBuilder.buildDoc("", ""));
	    	
    	} finally {
    		assertEquals(zXType.rc.rcOK, zx.word().closeDoc(false));
    		assertEquals(zXType.rc.rcOK, ((Document)objDocBuilder.getWordNewDoc()).closeDoc(false));
    	}
    }
    
    /**
     * @throws ZXException Thrown if test fails.
     */
    public void testDBMerge() throws ZXException {
    	DocBuilder objDocBuilder = new DocBuilder();
    	
    	try {
	    	
	    	assertEquals(zXType.rc.rcOK, objDocBuilder.startDoc("test/merge"));
	    	assertEquals(zXType.rc.rcOK, objDocBuilder.buildDoc("", ""));
	    	
    	} finally {
    		assertEquals(zXType.rc.rcOK, zx.word().closeDoc(false));
    		assertEquals(zXType.rc.rcOK, ((Document)objDocBuilder.getWordNewDoc()).closeDoc(false));
    	}
    }
    
}