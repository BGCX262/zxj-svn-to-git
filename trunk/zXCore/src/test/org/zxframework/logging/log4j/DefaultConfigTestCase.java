/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.zxframework.logging.log4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * <p>TestCase for Log4J logging when running on a system with
 * zero configuration, and with Log4J present (so Log4J logging
 * should be automatically configured).</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1.2.1 $ $Date: 2005/02/28 13:29:47 $
 */

public class DefaultConfigTestCase extends TestCase {

    // ----------------------------------------------------------- Constructors

    /**
     * <p>Construct a new instance of this test case.</p>
     *
     * @param name Name of the test case
     */
    public DefaultConfigTestCase(String name) {
        super(name);
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * <p>The {@link LogFactory} implementation we have selected.</p>
     */
    protected LogFactory factory = null;


    /**
     * <p>The {@link Log} implementation we have selected.</p>
     */
    protected Log log = null;

    // ------------------------------------------- JUnit Infrastructure Methods

    /**
     * Set up instance variables required by this test case.
     * @throws Exception Thrown if setUp fails.
     */
    public void setUp() throws Exception {
        setUpFactory();
        setUpLog("TestLogger");
    }

    /**
     * Return the tests included in this test suite.
     * @return Return test.
     */
    public static Test suite() {
        return (new TestSuite(DefaultConfigTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        log = null;
        factory = null;
        LogFactory.releaseAll();
    }

    // ----------------------------------------------------------- Test Methods

    /**
     * Test pristine Log instance
     */ 
    public void testPristineLog() {
        checkLog();
    }

    /**
     * Test pristine LogFactory instance
     */
    public void testPristineFactory() {

        assertNotNull("LogFactory exists", factory);
        assertEquals("LogFactory class",
                     "org.zxframework.logging.impl.LogFactoryImpl",
                     factory.getClass().getName());

        String names[] = factory.getAttributeNames();
        assertNotNull("Names exists", names);
        assertEquals("Names empty", 0, names.length);

    }

    /**
     * Test Serializability of Log instance
     * @throws Exception 
     */
    public void testSerializable() throws Exception {
        // Serialize and deserialize the instance
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(log);
        oos.close();
        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        log = (Log) ois.readObject();
        ois.close();

        // Check the characteristics of the resulting object
        checkLog();

    }

    // -------------------------------------------------------- Support Methods

    /**
     * Check the log instance
     */ 
    protected void checkLog() {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.zxframework.logging.impl.Log4JLogger",
                     log.getClass().getName());

        // Can we call level checkers with no exceptions?
        log.isDebugEnabled();
        log.isErrorEnabled();
        log.isFatalEnabled();
        log.isInfoEnabled();
        log.isTraceEnabled();
        log.isWarnEnabled();
    }
    
    /**
     * Set up factory instance.
     * 
     * @throws Exception Thrown if setUpFactory fails.
     */
    protected void setUpFactory() throws Exception {
        factory = LogFactory.getFactory();
    }
    
    /**
     * Set up log instance
     * @param name 
     * @throws Exception 
     */ 
    protected void setUpLog(String name) throws Exception {
        log = LogFactory.getLog(name);
    }
}