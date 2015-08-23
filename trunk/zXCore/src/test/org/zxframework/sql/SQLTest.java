/*
 * Created on 08-Feb-2005, by Michael Brewer
 * $Id: SQLTest.java,v 1.1.2.12 2006/07/17 16:13:46 mike Exp $
 */
package org.zxframework.sql;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zxframework.Attribute;
import org.zxframework.ZX;
import org.zxframework.ZXBO;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.property.StringProperty;
import org.zxframework.sql.SQL;
import org.zxframework.util.StringUtil;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.sql.SQL} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class SQLTest extends TestCase {

    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public SQLTest(String name) {
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
        TestSuite suite = new TestSuite(SQLTest.class);
        suite.setName("SQL Tests");
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

    //------------------------------------------------------------------- Tests constructors 
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        assertNotNull(new SQL());
        
        Constructor[] cons = SQL.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        
        assertEquals(true, Modifier.isPublic(SQL.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(SQL.class.getModifiers()));
    }
    
    //----------------------------------------------------------------------- Testing public methods.
    
    /**
     * @throws Exception Thrown if testConcurrencyControlWhereCondition fails.
     */
    public void testConcurrencyControlWhereCondition() throws Exception {
        ZXBO objBO = zx.createBO("test/wtch");
        objBO.setAutomatics("+");
        objBO.setValue("trdmrk", "test");
        objBO.setValue("clsss", "11");
        objBO.insertBO();
        
        //objBO.setPKValue("16");
        
        objBO.loadBO("*");

        String strTimestamp = System.currentTimeMillis() + "";
        zx.getQuickContext().setEntry("-ss", strTimestamp);
        
        // Shorten for the concurrencyControlWhereCondition call :
        int length = StringUtil.len(strTimestamp);
        if (length > 9) strTimestamp = strTimestamp.substring(length-9, length);
        String strResult = "((" + objBO.getDescriptor().getTable() + ".zXUpdtdId = " + objBO.getValue("zXUpdtdId").getStringValue() 
                           + ") OR " + objBO.getDescriptor().getTable() + ".zXUpdtdId = " + strTimestamp + ")";
        
        assertEquals(strResult, zx.getSql().concurrencyControlWhereCondition(objBO));
        
        // Clean up
        objBO.deleteBO();
    }
    
    /**
     * @throws Exception Thrown if testSystemDate fails
     */
    public void testSystemDate() throws Exception {
        DSHRdbms objDSHandler = zx.getDataSources().getPrimary();
        zXType.databaseType enmDatatype = objDSHandler.getDbType();
        try {
            assertEquals("now", zx.getSql().systemDate(zXType.databaseType.dbAccess));
            assertEquals("", zx.getSql().systemDate(zXType.databaseType.dbAny));
            assertEquals("current date", zx.getSql().systemDate(zXType.databaseType.dbAS400));
            assertEquals("current date", zx.getSql().systemDate(zXType.databaseType.dbDB2));
            assertEquals("now", zx.getSql().systemDate(zXType.databaseType.dbMysql));
            assertEquals("GETDATE()", zx.getSql().systemDate(zXType.databaseType.dbSQLServer));
        } catch (Exception e) {
        	throw new RuntimeException(e);
        } finally {
            objDSHandler.setDbType(enmDatatype);
        }
    }
    
    /**
     * @throws Exception Thrown if testMakeCaseInsensitive fails
     */
    public void testMakeCaseInsensitive() throws Exception {
        String strTest = "hello";
        String strResult  = "UPPER(" + strTest + ")";
        
        DSHRdbms objDSHandler = zx.getDataSources().getPrimary();
        zXType.databaseType enmDatatype = objDSHandler.getDbType();
        
        try {
            assertEquals(strTest,   zx.getSql().makeCaseInsensitive(strTest, zXType.databaseType.dbAccess));
            assertEquals(strTest,   zx.getSql().makeCaseInsensitive(strTest, zXType.databaseType.dbAny));
            assertEquals(strResult, zx.getSql().makeCaseInsensitive(strTest, zXType.databaseType.dbAS400));
            assertEquals(strResult, zx.getSql().makeCaseInsensitive(strTest, zXType.databaseType.dbDB2));
            assertEquals(strResult, zx.getSql().makeCaseInsensitive(strTest, zXType.databaseType.dbMysql));
            assertEquals(strTest,   zx.getSql().makeCaseInsensitive(strTest, zXType.databaseType.dbSQLServer));
        } catch(Exception e) {
        	throw new RuntimeException(e);
        } finally {
            objDSHandler.setDbType(enmDatatype);        
        }
    }
    
    /**
     * @throws Exception Thrown if testInClause fails
     */
    public void testInClause() throws Exception {
        // NOTE : Data for this test may change, so we need to have some default test data set up that does not change.
        ZXBO objBO = zx.createBO("test/wtch");
        objBO.setPKValue("16");
        objBO.loadBO();
        
        assertEquals("(16)", zx.getSql().inClause(objBO,"id","id",true,false));
        assertEquals("(9, 10, 11)", zx.getSql().inClause(objBO,"id","id",false,false));
    }
    
    /**
     * @throws Exception Thrown if testDbStrValue fails
     */
    public void testDbStrValue() throws Exception {
        
        try {
            
            // ------------------------------ Booleans

            // Access has a special boolean datatype. - access
            assertEquals("true", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "true", zXType.databaseType.dbAccess));
            assertEquals("false", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "false", zXType.databaseType.dbAccess));
            assertEquals("true", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "true", zXType.databaseType.dbAny));
            assertEquals("false", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "false", zXType.databaseType.dbAny));
            
            // Char for performance. - Oracle/DB2
            assertEquals("'Y'", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "true", zXType.databaseType.dbAS400));
            assertEquals("'N'", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "false", zXType.databaseType.dbAS400));
            assertEquals("'Y'", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "true", zXType.databaseType.dbDB2));
            assertEquals("'N'", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "false", zXType.databaseType.dbDB2));
            
            assertEquals("'Y'", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "true", zXType.databaseType.dbOracle));
            assertEquals("'N'", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "false", zXType.databaseType.dbOracle));
            
            // Using a tiny int is the most performant. - sql/mysql
            assertEquals("1", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "true", zXType.databaseType.dbMysql));
            assertEquals("0", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "false", zXType.databaseType.dbMysql));
            assertEquals("1", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "true", zXType.databaseType.dbSQLServer));
            assertEquals("0", zx.getSql().dbStrValue(zXType.dataType.dtBoolean.pos, "false", zXType.databaseType.dbSQLServer));
            
            // -------------------------------- Dates
            
            String strResult;
            DateFormat df;
            Date date = new Date();
            
            // Access and Any share the same implementation :
            df = new SimpleDateFormat("dd/MM/yyyy");
            strResult = "#" + df.format(date) + "#";
            assertEquals(strResult, zx.getSql().dbStrValue(zXType.dataType.dtDate.pos, date, zXType.databaseType.dbAny));
            df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            strResult = "#" + df.format(date) + "#";
            assertEquals(strResult, zx.getSql().dbStrValue(zXType.dataType.dtTimestamp.pos, date, zXType.databaseType.dbAny));
            df = new SimpleDateFormat("HH:mm:ss");
            strResult = "#" + df.format(date) + "#";
            assertEquals(strResult, zx.getSql().dbStrValue(zXType.dataType.dtTime.pos, date, zXType.databaseType.dbAny));
            
            // DB2 and AS400 are handled the same : (Hopefully UDB compliant)
            df = new SimpleDateFormat("yyyy-MM-dd");
            strResult  =  "DATE('" + df.format(date) + "')";
            assertEquals(strResult, zx.getSql().dbStrValue(zXType.dataType.dtDate.pos, date, zXType.databaseType.dbAS400));
            df = new SimpleDateFormat("yyyyMMddHHmmss");
            strResult  =  "TIMESTAMP('" + df.format(date) + "')";
            assertEquals(strResult, zx.getSql().dbStrValue(zXType.dataType.dtTimestamp.pos, date, zXType.databaseType.dbAS400));
            df = new SimpleDateFormat("HH:mm:ss");
            strResult  =  "TIME('" + df.format(date) + "')";
            assertEquals(strResult, zx.getSql().dbStrValue(zXType.dataType.dtTime.pos, date, zXType.databaseType.dbAS400));
            
            
            // ------------------------------- Strings/Expressions
            assertEquals("'test'", zx.getSql().dbStrValue(zXType.dataType.dtString.pos,"test", zXType.databaseType.dbDB2));
            assertEquals("'te''st'", zx.getSql().dbStrValue(zXType.dataType.dtString.pos,"te'st", zXType.databaseType.dbDB2));
            assertEquals("'''te''''st'", zx.getSql().dbStrValue(zXType.dataType.dtString.pos,"'te''st", zXType.databaseType.dbDB2));
            assertEquals("''", zx.getSql().dbStrValue(zXType.dataType.dtString.pos,"", zXType.databaseType.dbDB2));
            String test = null;
            assertEquals("''", zx.getSql().dbStrValue(zXType.dataType.dtString.pos,test, zXType.databaseType.dbDB2));
            
            // ------------------------------ Longs/Doubles/Automatics
            assertEquals("", zx.getSql().dbStrValue(zXType.dataType.dtLong.pos,"", zXType.databaseType.dbDB2));
            assertEquals("11", zx.getSql().dbStrValue(zXType.dataType.dtLong.pos,"11", zXType.databaseType.dbDB2));
            assertEquals("1.1", zx.getSql().dbStrValue(zXType.dataType.dtDouble.pos,"1.1", zXType.databaseType.dbDB2));
            assertEquals("11", zx.getSql().dbStrValue(zXType.dataType.dtAutomatic.pos,"11", zXType.databaseType.dbDB2));
            
        } catch (Exception e) {
        	throw new RuntimeException(e);
        } finally {
        	// Clean up ?
        }
    }
    
    /**
     * @throws Exception Thrown if testDbValue fails
     */
    public void testDbValue() throws Exception {
        
        ZXBO objBO = zx.createBO("test/wtch");
        String strValue = "16";
        objBO.setPKValue(strValue);
        
        Attribute objAttr = objBO.getDescriptor().getAttribute(objBO.getDescriptor().getPrimaryKey());
        assertEquals(strValue, zx.getSql().dbValue(objBO, objAttr));
        
        objBO.setValue(objBO.getDescriptor().getPrimaryKey(), new StringProperty(strValue, true));
        assertEquals(" NULL ", zx.getSql().dbValue(objBO, objAttr));
        
        strValue = null;
        objBO.setValue(objBO.getDescriptor().getPrimaryKey(), strValue);
        assertEquals(" NULL ", zx.getSql().dbValue(objBO, objAttr));
        
        strValue = "0";
        objBO.setValue(objBO.getDescriptor().getPrimaryKey(), strValue);
        assertEquals("0", zx.getSql().dbValue(objBO, objAttr));
        
        strValue = "";
        objBO.setValue(objBO.getDescriptor().getPrimaryKey(), strValue);
        assertEquals("0", zx.getSql().dbValue(objBO, objAttr)); // Could be a problem
        
    }
    
    /**
     * @throws Exception Thrown if testSingleWhereCondition fails.
     */
    public void testSingleWhereCondition() throws Exception {
        ZXBO objBO = zx.createBO("test/wtch");
        
        Attribute objAttr = objBO.getDescriptor().getAttribute(objBO.getDescriptor().getPrimaryKey());
        
        // Get the column name
        String strColumn = zx.getSql().columnName(objBO, objAttr, zXType.sqlObjectName.sonName);
        
        String strValue = "16";
        objBO.setPKValue(strValue);
        
        // Equals
        zXType.compareOperand enmCompareOperand = zXType.compareOperand.coEQ;
        String strTest = zx.getSql().singleWhereCondition(objBO, objAttr, 
                                                          enmCompareOperand, objBO.getPKValue());
        
        assertEquals(strColumn + " = 16", strTest);

        enmCompareOperand = zXType.compareOperand.coNE;
        strTest = zx.getSql().singleWhereCondition(objBO, objAttr, 
                enmCompareOperand, objBO.getPKValue());
        assertEquals(strColumn + " <> 16", strTest);
        
        // Starts with
        enmCompareOperand = zXType.compareOperand.coSW;
        strTest = zx.getSql().singleWhereCondition(objBO, objAttr, 
                                                   enmCompareOperand, objBO.getPKValue());
        assertEquals(strColumn + " LIKE '16%'", strTest);
    }
    
    /**
     * @throws Exception Thrown if testProcessFullExpressionWhereGroup fails.
     */
    public void testProcessFullExpressionWhereGroup() throws Exception {
        ZXBO objBO = zx.createBO("test/wtch");
        String strValue = "16";
        objBO.setPKValue(strValue);
        zx.getBOContext().setEntry("testwtch", objBO);
        
        assertEquals("(tmwtch.id = 15)", zx.getSql().processFullExpressionWhereGroup(objBO, ":id=15"));
        assertEquals("(tmwtch.id > 15 AND tmwtch.id < 15)",
                	 zx.getSql().processFullExpressionWhereGroup(objBO, ":id>15&id<15"));
        assertEquals("(tmwtch.id > 15 OR tmwtch.id < 15)",
                	 zx.getSql().processFullExpressionWhereGroup(objBO, ":id>15|id<15"));
        assertEquals("(tmwtch.id > 15 OR tmwtch.clsss LIKE 'bertus%')",
        			 zx.getSql().processFullExpressionWhereGroup(objBO, ":id>15|clsss%'bertus'"));
        assertEquals("(tmwtch.id > 15 OR tmwtch.clsss LIKE '%bertus%')",
                	 zx.getSql().processFullExpressionWhereGroup(objBO, ":id>15|clsss%%'bertus'"));
        // TODO : Check with Bertus why != is no longer supported.
        assertEquals("(tmwtch.id > 15 OR tmwtch.clsss LIKE '%bertus%' AND tmwtch.zXCrtdWhn <> DATE_FORMAT('2005-02-18', '%Y-%m-%d'))",
                	 zx.getSql().processFullExpressionWhereGroup(objBO, ":id>15|clsss%%'bertus'&zXCrtdWhn<>#date"));
        assertEquals("(tmwtch.id = 14 AND >>>Unable to get handle to BO.attr referred to in where condition<<<)",
                	 zx.getSql().processFullExpressionWhereGroup(objBO, ":id=14&xxx=15"));
        assertEquals("(tmwtch.id = 15 OR  (tmwtch.id = tmwtch.id AND 'clss' <> tmwtch.id ))",
                	 zx.getSql().processFullExpressionWhereGroup(objBO, ":id=15|(:id=id&'clss'!=id)"));
        
    }
}