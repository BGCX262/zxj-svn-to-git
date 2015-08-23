/*
 * Created on Feb 25, 2005, by Michael Brewer
 * $Id: ZXCollectionPerformanceTest.java,v 1.1.2.3 2005/05/22 19:05:49 mike Exp $
 */
package org.zxframework;

// Various Java collections to benchmark against.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import java.util.Iterator;
import java.util.Map;

// Try one of apache collections as a comparison.
import org.apache.commons.collections.FastHashMap;

import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;

import com.vladium.utils.timing.ITimer;
import com.vladium.utils.timing.TimerFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Benchmarking the zXCollection against various other options.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class ZXCollectionPerformanceTest extends TestCase {

    private int COUNT = 500000;
    // private int COUNT_SMLL = 500;
    private int COUNT_MDM = 100000;
    
    private static ITimer timer;
    static {
        timer = TimerFactory.newTimer ();
    }
    
    /**
     * @param name
     *            The name of the test
     */
    public ZXCollectionPerformanceTest(String name) {
        super(name);
    }

    /**
     * @param args
     *            Program parameters.
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * @return Returns the test to run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ZXCollectionPerformanceTest.class);
        suite.setName("ZXCollection Performance Tests");
        return suite;
    }

    // ------------------------------------- Tests

    /**
     * Testing adding/removing and iterating through in sequence.
     */
    public void testIterate() {
        ZXCollection colZX;
        ArrayList colArrayList;
        String strTmp;
        
        String strTest = "123456789012345678901234567890";
        Property objProp = new StringProperty(strTest);
        Integer inT = new Integer(100);
        
        timer.start();
        colZX = new ZXCollection();
        for (int i = 0; i < COUNT; i++) {
            colZX.add(strTest);
        }
        timer.stop();
        System.out.println("ZXCollection()[" + COUNT + "]          adding(String)       : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        colZX = new ZXCollection();
        for (int i = 0; i < COUNT; i++) {
            colZX.add(objProp);
        }
        timer.stop();
        System.out.println("ZXCollection()[" + COUNT + "]          adding(Property)     : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        colZX = new ZXCollection();
        for (int i = 0; i < COUNT; i++) {
            colZX.add(inT);
        }
        timer.stop();
        System.out.println("ZXCollection()[" + COUNT + "]          adding(Integer)      : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        colZX = new ZXCollection(COUNT);
        for (int i = 0; i < COUNT; i++) {
            colZX.add(strTest);
        }
        timer.stop();
        System.out.println("ZXCollection(" + COUNT + ")            adding(String)       : " + timer.getDuration());
        timer.reset();

        
        timer.start();
        colZX = new ZXCollection(COUNT);
        for (int i = 0; i < COUNT; i++) {
            colZX.add(objProp);
        }
        timer.stop();
        System.out.println("ZXCollection(" + COUNT + ")            adding(Property)     : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        colZX = new ZXCollection(COUNT);
        for (int i = 0; i < COUNT; i++) {
            colZX.add(inT);
        }
        timer.stop();
        System.out.println("ZXCollection(" + COUNT + ")            adding(Integer)      : " + timer.getDuration());
        timer.reset();
        
//        timer.start();
//        for (int i = 0; i < COUNT/100; i++) {
//            strTmp = (String)colZX.get(i);
//        }
//        timer.stop();
//        System.out.println("ZXCollection()          getting : " + (timer.getDuration()*100));
//        timer.reset();
        
        colZX = new ZXCollection(COUNT);
        for (int i = 0; i < COUNT; i++) {
            colZX.add(strTest);
        }
        timer.start();
        ArrayList arrZX = new ArrayList(colZX.getCollection());
        for (int i = 0; i < COUNT; i++) {
            strTmp = (String)arrZX.get(i);
        }
        strTmp = null;
        
        timer.stop();
        System.out.println("ZXCollection()[" + COUNT + "]          getting(String)      : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        colArrayList = new ArrayList();
        for (int i = 0; i < COUNT; i++) {
            colArrayList.add(strTest);
        }
        timer.stop();
        System.out.println("Arraylist()[" + COUNT + "]             adding(String)       : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        colArrayList = new ArrayList();
        for (int i = 0; i < COUNT; i++) {
            colArrayList.add(objProp);
        }
        timer.stop();
        System.out.println("Arraylist()[" + COUNT + "]             adding(Property)     : " + timer.getDuration());
        timer.reset();

        
        timer.start();
        colArrayList = new ArrayList();
        for (int i = 0; i < COUNT; i++) {
            colArrayList.add(inT);
        }
        timer.stop();
        System.out.println("Arraylist()[" + COUNT + "]             adding(Integer)      : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        colArrayList = new ArrayList(COUNT);
        for (int i = 0; i < COUNT; i++) {
            colArrayList.add(strTest);
        }
        timer.stop();
        System.out.println("Arraylist(" + COUNT + ")               adding(String)       : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        colArrayList = new ArrayList(COUNT);
        for (int i = 0; i < COUNT; i++) {
            colArrayList.add(objProp);
        }
        timer.stop();
        System.out.println("Arraylist(" + COUNT + ")               adding(Property)     : " + timer.getDuration());
        timer.reset();
        
        colArrayList = new ArrayList(COUNT);
        for (int i = 0; i < COUNT; i++) {
            colArrayList.add(strTest);
        }
        timer.start();
        for (int i = 0; i < COUNT; i++) {
            strTmp = (String)colArrayList.get(i);
        }
        timer.stop();
        System.out.println("Arraylist()[" + COUNT + "]             getting(String)      : " + timer.getDuration() + " [get(i)]");
        timer.reset();
        timer.start();
        for (int i = 0; i < COUNT; i++) {
            strTmp = (String)colArrayList.get(i);
        }
        timer.stop();
        System.out.println("Arraylist()[" + COUNT + "]             getting(String)      : " + timer.getDuration() + " [get(i)]");
        timer.reset();

        
        colArrayList = new ArrayList(COUNT);
        for (int i = 0; i < COUNT; i++) {
            colArrayList.add(strTest);
        }
        timer.start();
        Iterator iter = colArrayList.iterator();
        while (iter.hasNext()) {
            strTmp = (String)iter.next();
        }
        timer.stop();
        System.out.println("Arraylist()[" + COUNT + "]             getting(String)      : " + timer.getDuration() + " [iterator().next()]");
        timer.reset();
        timer.start();
        iter = colArrayList.iterator();
        while (iter.hasNext()) {
            strTmp = (String)iter.next();
        }
        timer.stop();
        System.out.println("Arraylist()[" + COUNT + "]             getting(String)      : " + timer.getDuration() + " [iterator().next()]");
        timer.reset();
        
        if (strTmp != null) strTmp = null;
    }
    
    /**
     * Testing collections with keys.
     */
    public void testKey() {
        ZXCollection colZX;
        Map map;
        String strTmp = null;
        
        timer.start();
        colZX = new ZXCollection();
        for (int i = 0; i < COUNT_MDM; i++) {
            colZX.put("test" + i, "value" + i);
        }
        timer.stop();
        System.out.println("ZXCollection(" + COUNT_MDM + ")  put(String)      : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        for (int i = 0; i < COUNT_MDM; i++) {
            colZX.put("test" + i, "value" + i);
        }
        timer.stop();
        System.out.println("ZXCollection(" + COUNT_MDM + ")  replace(String)  : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        for (int i = 0; i < COUNT_MDM; i++) {
            strTmp = (String)colZX.get("test" + i);
        }
        timer.stop();
        System.out.println("ZXCollection(" + COUNT_MDM + ")  get(String)      : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        map = new HashMap();
        for (int i = 0; i < COUNT_MDM; i++) {
            map.put("test" + i, "value" + i);
        }
        timer.stop();
        System.out.println("HashMap(" + COUNT_MDM + ")       put(String)      : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        for (int i = 0; i < COUNT_MDM; i++) {
            map.put("test" + i, "value" + i);
        }
        timer.stop();
        System.out.println("HashMap(" + COUNT_MDM + ")       replace(String)  : " + timer.getDuration());
        timer.reset();

        timer.start();
        for (int i = 0; i < COUNT_MDM; i++) {
            strTmp = (String)map.get("test" + i);
        }
        timer.stop();
        System.out.println("HashMap(" + COUNT_MDM + ")       get(String)      : " + timer.getDuration());
        timer.reset();

        
        timer.start();
        map = new FastHashMap();
        for (int i = 0; i < COUNT_MDM; i++) {
            map.put("test" + i, "value" + i);
        }
        timer.stop();
        System.out.println("FastHashMap(" + COUNT_MDM + ")   put(String)      : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        for (int i = 0; i < COUNT_MDM; i++) {
            map.put("test" + i, "value" + i);
        }
        timer.stop();
        System.out.println("FastHashMap(" + COUNT_MDM + ")   replace(String)  : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        for (int i = 0; i < COUNT_MDM; i++) {
            strTmp = (String)map.get("test" + i);
        }
        timer.stop();
        System.out.println("FastHashMap(" + COUNT_MDM + ")   get(String)      : " + timer.getDuration());
        timer.reset();

        
        timer.start();
        map = new Hashtable();
        for (int i = 0; i < COUNT_MDM; i++) {
            map.put("test" + i, "value" + i);
        }
        timer.stop();
        System.out.println("Hashtable(" + COUNT_MDM + ")     put(String)      : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        for (int i = 0; i < COUNT_MDM; i++) {
            map.put("test" + i, "value" + i);
        }
        timer.stop();
        System.out.println("Hashtable(" + COUNT_MDM + ")     replace(String)  : " + timer.getDuration());
        timer.reset();
        
        timer.start();
        for (int i = 0; i < COUNT_MDM; i++) {
            strTmp = (String)map.get("test" + i);
        }
        timer.stop();
        System.out.println("Hashtable(" + COUNT_MDM + ")     get(String)      : " + timer.getDuration());
        timer.reset();
        
        if (strTmp != null) strTmp = null;
    }
}