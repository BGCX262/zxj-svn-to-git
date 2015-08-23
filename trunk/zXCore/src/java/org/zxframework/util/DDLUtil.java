/*
 * Created on 04-Feb-2005, by Michael Brewer
 * $Id: DDLUtil.java,v 1.1.2.15 2006/07/17 16:15:31 mike Exp $
 */
package org.zxframework.util;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.Descriptor;
import org.zxframework.ZX;
import org.zxframework.ZXBO;
import org.zxframework.zXType;
import org.zxframework.datasources.DSRS;
import org.zxframework.jdbc.ZXResultSet;

/**
 * A set of utilities to sync the database with the business objects. 
 * This is more of a push philosophy. Although we now have datasources for
 * ddlutil we will just use the primary datasource only.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class DDLUtil {
    
    private ZX zx;
    
    /** A collection (String) of sql statements to execute. **/
    private ArrayList statements;
    
    /** Whether to check whether the table has altered. **/
    private boolean chkAlter;
    
    /** Whether to a create statement for the table to the ddl script. **/
    private boolean addTable;
    
    /** Whether to add a create sequence statement to the ddl script. **/
    private boolean addSequence;
    
    /** Whether to add a create foriegn constraints to the ddl script. **/
    private boolean addFKConstraint;
    
    /** Whether to add drop table/index statements to the ddl script. */
    private boolean addDropStatement;
    
    /** Whether to add a create unique index and other indexes statements to the ddl script. **/
    private boolean addIndex;
    
    /** Whether to add any foriegn key indexes. **/
    private boolean addFKIndex = true;
    
    
    /** What type of database to generate the ddl for. **/
    private zXType.databaseType databaseType;
    
    /**
     * Default constructor.
     */
    public DDLUtil() {
        this.statements = new ArrayList();
    }
    
    /**
     * Allow for a commandline utility ?
     * 
     * @param args Arguments to parse.
     * @throws Exception Thrown if failed to load.
     */
    public static void main(String[] args) throws Exception {
        ZX objZX = new ZX(TestUtil.getCfgPath());
        
        if (args.length == 0) {
            // Quick test :
            DDLUtil objDDLUtil = new DDLUtil();
            objDDLUtil.setZx(objZX);
            
            // Settings :
            objDDLUtil.setDatabaseType(zXType.databaseType.dbDB2);
            
            objDDLUtil.setAddDropStatement(false);
            objDDLUtil.setChkAlter(true);
            objDDLUtil.setAddTable(false);
            objDDLUtil.setAddFKConstraint(false);
            objDDLUtil.setAddFKIndex(true);
            objDDLUtil.setAddIndex(false);
            objDDLUtil.setAddSequence(false);
            
//            Descriptor objDesc = objDDLUtil.getZx().createBO("zxSssn").getDescriptor();
//            objDDLUtil.doCreateTable(objDesc);
//            objDDLUtil.doCreateSequence(objDesc);
//            objDDLUtil.doCreateIndexes(objDesc);
//            objDDLUtil.doAlter(objDesc);
            
//            objDDLUtil.scanBODir();
//            System.out.println(objDDLUtil.dumpStatements());
            objDDLUtil.generateInserts();
            System.out.println(objDDLUtil.dumpStatements());
            
//            try {
//                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//                String str = "";
//                while (str != null) {
//                    System.out.print("> prompt ");
//                    str = in.readLine();
//                    //process(str);
//                    System.out.println(str);
//                }
//            } catch (IOException e) {
//            }
            
//            objDDLUtil.executeStatements();
        }
    }
    
    //-------------------------------------------- Getters and setters.
    
    /**
     * @return Returns the statements.
     */
    public ArrayList getStatements() {
        return statements;
    }
    
    /**
     * @param statements The statements to set.
     */
    public void setStatements(ArrayList statements) {
        this.statements = statements;
    }
    
    /**
     * @return Returns the zx.
     */
    public ZX getZx() {
        return zx;
    }
    
    /**
     * @param zx The zx to set.
     */
    public void setZx(ZX zx) {
        this.zx = zx;
    }
    
    /**
     * Whether to add drop table/index statements to the ddl script.
     * 
     * @return Returns the addDropStatement.
     */
    public boolean isAddDropStatement() {
        return addDropStatement;
    }
    
    /**
     * @param addDropStatement The addDropStatement to set.
     */
    public void setAddDropStatement(boolean addDropStatement) {
        this.addDropStatement = addDropStatement;
    }
    
    /**
     * @return Returns the databaseType.
     */
    public zXType.databaseType getDatabaseType() {
        return databaseType;
    }
    
    /**
     * @param databaseType The databaseType to set.
     */
    public void setDatabaseType(zXType.databaseType databaseType) {
        this.databaseType = databaseType;
    }
    
    /**
     * Whether to check whether the table has altered.
     * 
     * NOTE : This option by itself will be enough if you are syncing the business object with the database.
     * 
     * @return Returns the chkAlter.
     */
    public boolean isChkAlter() {
        return chkAlter;
    }
    
    /**
     * @param chkAlter The chkAlter to set.
     */
    public void setChkAlter(boolean chkAlter) {
        this.chkAlter = chkAlter;
    }
    
    /**
     * Whether to a create statement for the table to the ddl script.
     * 
     * NOTE : This option by itself will NOT add the create sequence to the ddl script.
     * 
     * @return Returns the addTable.
     */
    public boolean isAddTable() {
        return addTable;
    }
    
    /**
     * Whether to add any foriegn key indexes.
     * 
     * NOTE: This is true by default.
     * 
     * @return Returns the addFKIndex.
     */
    public boolean isAddFKIndex() {
        return addFKIndex;
    }
    
    /**
     * @param addFKIndex The addFKIndex to set.
     */
    public void setAddFKIndex(boolean addFKIndex) {
        this.addFKIndex = addFKIndex;
    }
    
    /**
     * @param addTable The addTable to set.
     */
    public void setAddTable(boolean addTable) {
        this.addTable = addTable;
    }
    
    /**
     * Whether to add a create unique index and other indexes statements to the ddl script.
     * 
     * NOTE : By itself it will not create the necessary foriegn indexes.
     * 
     * @return Returns the addIndex.
     */
    public boolean isAddIndex() {
        return addIndex;
    }
    
    /**
     * @param addIndex The addIndex to set.
     */
    public void setAddIndex(boolean addIndex) {
        this.addIndex = addIndex;
    }
    
    /**
     * @return Returns the addFKConstraint.
     */
    public boolean isAddFKConstraint() {
        return addFKConstraint;
    }
    
    /**
     * @param addFKConstraint The addFKConstraint to set.
     */
    public void setAddFKConstraint(boolean addFKConstraint) {
        this.addFKConstraint = addFKConstraint;
    }
    
    /**
     * Whether to add a create sequence statement to the ddl script.
     * 
     * @return Returns the addSequence.
     */
    public boolean isAddSequence() {
        return addSequence;
    }
    
    /**
     * @param addSequence The addSequence to set.
     */
    public void setAddSequence(boolean addSequence) {
        this.addSequence = addSequence;
    }
    
    //-------------------------------------------------------------- Public methods.
    
    /**
     * @return Returns the filename filter for business object xml files.
     */
    private FilenameFilter getBOFilter() {
        /**
         * We are only interested in specfic files.
         */
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (dir.isDirectory()) {
                    // Directory : Must not be current dir or a CVS dir.
                    return !name.startsWith(".") && !name.equalsIgnoreCase("CVS");
                }
                
                // File : We are only interested in xml files.
                return name.toLowerCase().endsWith(".xml");
            }
        };
        return filter;
    }
    
    /**
     * @throws Exception Thrown if scanBODir fails.
     */
    public void scanBODir() throws Exception {
        File objDir = new File(getZx().fullPathName(getZx().getBoDir()));
        scanDir(objDir);
    }
    
    /**
     * @param pobjDir The director to scan.
     * @throws Exception Thrown if scanDir fails.
     */
    public void scanDir(File pobjDir) throws Exception{
        
        /**
         * We are only interested in specfic files.
         */
        FilenameFilter filter = getBOFilter();
        
        File objFile;
        
        File[] arrfiles = pobjDir.listFiles(filter);
        for (int i = 0; i < arrfiles.length; i++) {
            objFile = arrfiles[i];
            
            if (objFile.isDirectory()) {
                // Scan recursively :
                scanDir(objFile);
                
            } else {
                /**
                 * We have a xml file, this might be a business object.
                 */
                // String strBO = objFile.getName().substring(0, objFile.getName().length()-4);
                try {
                    /**
                     * BD15MAR04 
                     * Bit tricky: zX.createBO handles audit attributes where descriptor init
                     * does not. We could simply do a zX.createBO however this causes
                     * another problem: if the BO has a special class this utility will only
                     * work if this class is actually available. So what we do is a 2-stage rocket:
                     *  - Load descriptor
                     *   - Overwrite classname so we always use good old zX.clsBO
                     *    - use createBO and use that descriptor
                     */
                    Descriptor objDesc = new Descriptor();
                    objDesc.init(objFile.getAbsolutePath(), false);
                    objDesc.setClassName("org.zxframework.ZXBO");
                    
                    ZXBO objBO = new ZXBO();
                    objBO.init(objDesc);
                    
                    if (!objDesc.getTable().equals("-")) {
                        
                        if (isChkAlter()) {
                            /**
                             * NOTE : This is not implemented. But could be :
                             * 
                             * Shift and Generate (Go) when Alter is checked is a specialist option
                             * that generates a SQL Server check constraint statement. Otherwise
                             * (normally) does the usual alter checks
                             * 
                             * addStatement "ALTER TABLE " & objDesc.Table & " CHECK CONSTRAINT ALL"
                             */
                            // Check the table is different from the business object.
                            doAlter(objDesc);
                            
                        } else {
                            /**
                             * Do specific creates.
                             * NOTE : This does not check whether the table does not exists etc..
                             */
                            // Does the create table sql + unique index.
                            if (isAddTable()) {
                                doCreateTable(objDesc);
                            }
                            
                            // Does the create indexes on the foriegn keys.
                            if (isAddIndex()) {
                                doCreateIndexes(objDesc);
                            }
                            
                            // Adds the sql for a sequence.
                            if (isAddSequence()) {
                                doCreateSequence(objDesc);
                            }
                            
                            // Add the Foriegn key constraints.
                            if (isAddFKConstraint()){
                                doCreateFKConstraints(objDesc);
                            }
                            
                        }
                    }
                    
//                    String strStatement = dumpStatements();
//                    if (StringUtil.len(strStatement) > 0) { 
//                        System.out.println(strStatement);
//                        clearStatements();
//                    }
                    
                }catch (Exception e) {
                    // Failed to parse
                    System.out.println(objFile.getName() + " --->  failed to parse descriptor. ");
                }
                
            }
        }
    }

    /**
     * Scans the bo directory recursively and generates the insert statements.
     * 
     * NOTE : This may be slow and use alot of memory, so do not use this against very large datasources.
     * 
     * @throws Exception Thrown if scanBODir fails.
     */
    public void generateInserts() throws Exception {
        File objDir = new File(getZx().fullPathName(getZx().getBoDir()));
        generateInserts(objDir);
    }    
    
    /**
     * Scans a specific directory in the business object directory. It may call itself recursively.
     * 
     * @param pobjDir The director to scan.
     * @throws Exception Thrown if scanDir fails.
     */
    public void generateInserts(File pobjDir) throws Exception{
        
        /**
         * Get a file filter that will only match .xml files.
         */
        FilenameFilter filter = getBOFilter();
        File objFile;
        File[] arrfiles = pobjDir.listFiles(filter);
        
        for (int i = 0; i < arrfiles.length; i++) {
            objFile = arrfiles[i];
            
            if (objFile.isDirectory()) {
                // Scan recursively :
                generateInserts(objFile);
                
            } else {
                /**
                 * We have a xml file, this might be a business object.
                 */
                try {
                    /**
                     * BD15MAR04 
                     * Bit tricky: zX.createBO handles audit attributes where descriptor init
                     * does not. We could simply do a zX.createBO however this causes
                     * another problem: if the BO has a special class this utility will only
                     * work if this class is actually available. So what we do is a 2-stage rocket:
                     *  - Load descriptor
                     *   - Overwrite classname so we always use good old zX.clsBO
                     *    - use createBO and use that descriptor
                     */
                    Descriptor objDesc = new Descriptor();
                    objDesc.init(objFile.getAbsolutePath(), false);
                    objDesc.setClassName("org.zxframework.ZXBO");
                    
                    generateInserts(objDesc);
                    
                }catch (Exception e) {
                    // Failed to parse, but carry on regardless.
                    System.out.println(objFile.getName() + " --->  failed to parse descriptor. ");
                }
                
            }
        }
    }
    
    /**
     * Generates the insert statements for a specific business object.
     * 
     * @param pobjDesc The descriptor of the business object.
     * @throws Exception Thrown if generateInsert fails.
     */
    public void generateInserts(Descriptor pobjDesc) throws Exception {
        
        ZXBO objBO = new ZXBO();
        objBO.init(pobjDesc);
        String strQry;
        DSRS objRS;
        zXType.databaseType enmCurrentDB = getZx().getDataSources().getPrimary().getDbType();
        
        if (!pobjDesc.getTable().equals("-")) {
            /**
             * Generate sql before we move to other database type
             */
            strQry = getZx().getSql().loadQuery(objBO, "*");
            objRS = getZx().getDataSources().getPrimary().sqlRS(strQry);
            
            try {
                
                /**
                 * Generate the insert statement.
                 */
                while (!objRS.eof()) {
                    objRS.rs2obj(objBO, "*");
                    
                    addStatement(getZx().getSql().insertQuery(objBO));
                    
                    objRS.moveNext();
                }
                
            } catch (Exception e) {
                getZx().trace.addError("Failed to generate insert statement", e);
                // Carry on non the less.
            } finally {
                if (objRS != null) objRS.RSClose();
                
                /**
                 * Alway restore current db type, in case of failure.
                 */
                getZx().getDataSources().getPrimary().setDbType(enmCurrentDB);
            }
        }
    }
    
    /**
     * Generate alter table statement. Checks what the differences as between current database and the business object.
     * 
     * @param pobjDesc The business object's descriptor.
     * @throws Exception Thrown if doAlter fails.
     */
    public void doAlter(Descriptor pobjDesc) throws Exception {
        /**
         * See if the table exists
         */
        ZXResultSet objFieldsRS = null;
        try {
            String strTableName = pobjDesc.getTable();
            boolean blnTableExists = false;
            boolean blnVirtual = strTableName.equals("-");
            if (!blnVirtual) {
            	try {
                    objFieldsRS = getZx().getDataSources().getPrimary().sqlRS("select * from " + strTableName).getRs();
                } catch (Exception e) {
                	throw new RuntimeException(e);
                }
                
                if (objFieldsRS !=null) {
                    blnTableExists = true;
                }
            }
            
            if (!blnTableExists && !blnVirtual) {
                // Protection against miss use.
                boolean chkTableOrig = addTable;
                // Turn on addTable flag, as we has actually created the table.
                addTable = true;
                
                // Create the table + unique index.
                doCreateTable(pobjDesc);
                // Create all of the other necessary indexes... (exept for unique index ?)
                doCreateIndexes(pobjDesc);
                // Create the sequence for the table in needed.
                doCreateSequence(pobjDesc);
                // Create the foriegn constraints.
                doCreateFKConstraints(pobjDesc);
                
                // Revert to original setting.
                addTable = chkTableOrig;
                
            } else if (!blnVirtual){
                /**
                 * Table already exists but may be out of date.
                 * NOTE : We presume we do not need to create any sequences either.
                 */
                ResultSetMetaData objResultSetMetaData = objFieldsRS.getTarget().getMetaData();
                Map objFields = new HashMap();
                
                Field objField = new Field();
                int intColumns = objResultSetMetaData.getColumnCount() + 1;
                for (int i = 1; i < intColumns; i++) {
                    objField = new Field();
                    
                    objField.setType(objResultSetMetaData.getColumnType(i));
                    objField.setDefinedSize(objResultSetMetaData.getColumnDisplaySize(i));
                    objField.setName(objResultSetMetaData.getColumnName(i));
                    
                    objFields.put(objField.getName().toLowerCase(), objField);
                }
                
                /**
                 * Loop over the objFieldsRS from the BO. For each field:
                 */
                Attribute objAttr;
                String strSQL;
                zXType.dataType enmColumnType;
                String strColumnName;
                
                AttributeCollection colAttr = pobjDesc.getGroup("*");
                Iterator iter = colAttr.iterator();
                while (iter.hasNext()) {
                    objAttr = (Attribute)iter.next();
                    
                    if (StringUtil.len(objAttr.getColumn()) > 0) {
                        strSQL = ""; // Reset sql.
                        
                        enmColumnType = objAttr.getDataType();
                        strColumnName = objAttr.getColumn();
                        
                        objField = (Field)objFields.get(strColumnName.toLowerCase());
                        
                        if (objField != null) {
                            /**
                             * Field exists, but there might be a difference :
                             */
                            if (checkType(enmColumnType.pos, objField.getType())) {
                                /**
                                 * Alter the data type
                                 */
                                if (databaseType.equals(zXType.databaseType.dbAccess) || databaseType.equals(zXType.databaseType.dbSQLServer)) {
                                    // Access and SQL Server
                                    strSQL = "ALTER TABLE " + strTableName + " ALTER COLUMN " +
                                                    strColumnName + " ";
                                    
                                } else if (databaseType.equals(zXType.databaseType.dbOracle)) {
                                    // Oracle support
                                    strSQL = "ALTER TABLE " + strTableName + " MODIFY " +
                                                    strColumnName + " ";
    
                                } else if (databaseType.equals(zXType.databaseType.dbMysql)) {
                                    // Mysql support
                                    strSQL = "ALTER TABLE " + strTableName + " MODIFY COLUMN " +
                                                    strColumnName + " ";
    
                                } else {
                                    strSQL = "ALTER TABLE " + strTableName + " ALTER " +
                                                    strColumnName + " ";
                                }
                                strSQL = strSQL + columnTypeSQL(pobjDesc, objAttr);
                                addStatement(strSQL);
                                
                             } else {
                                /**
                                 * Same type but maybe different length
                                 */
                                if (objAttr.getDataType().pos == zXType.dataType.dtExpression.pos || objAttr.getDataType().pos == zXType.dataType.dtString.pos) {
                                    if (objField.getDefinedSize() != objAttr.getLength()) {
                                        //  Alter the data type unless it is a memo
                                        if (objField.getDefinedSize() < 32000) {
                                            /**
                                             * Alter the data type
                                             */
                                            if (databaseType.equals(zXType.databaseType.dbAccess) || databaseType.equals(zXType.databaseType.dbSQLServer)) {
                                                // Access and SQL Server
                                                strSQL = "ALTER TABLE " + strTableName + " ALTER COLUMN " + strColumnName + " ";
                                                
                                            } else if (databaseType.equals(zXType.databaseType.dbOracle)) {
                                                // Oracle support
                                                strSQL = "ALTER TABLE " + strTableName + " MODIFY " + strColumnName + " ";
    
                                            } else if (databaseType.equals(zXType.databaseType.dbMysql)) {
                                                // Mysql support
                                                strSQL = "ALTER TABLE " + strTableName + " MODIFY COLUMN " + strColumnName + " ";
    
                                            } else {
                                                strSQL = "ALTER TABLE " + strTableName + " ALTER " + strColumnName + " ";
                                                
                                            }
                                            
                                            strSQL = strSQL + columnTypeSQL(pobjDesc, objAttr);
                                            addStatement(strSQL);
                                            
                                        } // Memo ?
                                        
                                    } // Length difference
                                    
                                } // String or expression so check for length changes
                                
                            } // Different datatypes ?
                        } else {
                            /**
                             * Field not found so add
                             */
                            if (databaseType.equals(zXType.databaseType.dbAccess)) {
                                strSQL = "ALTER TABLE " + strTableName + " ADD COLUMN " +
                                                strColumnName + " ";
                            } else {
                                strSQL = "ALTER TABLE " + strTableName + " ADD " +
                                                strColumnName + " ";
                            }
                            strSQL = strSQL + columnTypeSQL(pobjDesc, objAttr);
                            
                            addStatement(strSQL);
                            
                            if (StringUtil.len(objAttr.getForeignKey()) > 0) {
                                /**
                                 * Add the index
                                 */
                                doCreateIndex(strTableName, objAttr);
                                // addStatement("CREATE INDEX " + objAttr.getColumn() + "_IX" + " ON " + strTableName + " (" + objAttr.getColumn() + ")");
                                
                                /**
                                 * Add the FK constraint
                                 */
                                doCreateFKConstraint(strTableName, objAttr);
                                
                            }
                            
                        } // Field found
                        
                    }  // Attr has column
                    
                } // Next attribute
                
                /**
                 * Now check for any DB objFieldsRS that are not in the BO
                 */
                iter = objFields.values().iterator();
                while (iter.hasNext()) {
                    objField = (Field)iter.next();
                    
                    strSQL = "";
                    boolean blnColumnFound = false;
                    
                    Iterator iter2 = colAttr.iterator();
                    while (iter2.hasNext()) {
                        objAttr = (Attribute)iter2.next();
                        
                        if (StringUtil.len(objAttr.getColumn()) > 0 
                                && objField.getName().toLowerCase().equals(objAttr.getColumn().toLowerCase())) {
                            blnColumnFound = true;
                            break;
                        }
                    }
                    
                    if (!blnColumnFound && isAddDropStatement()) {
                        /**
                         *  All objFieldsRS from the BO have been checked, and not found
                         *  Drop the field
                         */
                        addStatement("ALTER TABLE " + strTableName + " DROP COLUMN " + objField.getName());
                        
                    }
                }
            } // Table exists ?
            
        } finally {
            if (objFieldsRS != null) objFieldsRS.close();
        }
    }
    
    /**
     * @param pobjDesc The business object to get the create statement.
     * @throws Exception Thrown if boCreateSQL fails.
     */
    public void doCreateTable(Descriptor pobjDesc) throws Exception {
        String strPrimaryKey = pobjDesc.getPrimaryKey();
        
        /** Add the drop table sql. **/
        if (addDropStatement) {
            addStatement("DROP TABLE " + pobjDesc.getTable());
        }
        
        String strSQL = "CREATE TABLE "+ pobjDesc.getTable() + " (";
        
        boolean blnFirst = true;
        String strTmp;
        Attribute objAttr;
        AttributeCollection colAttr = pobjDesc.getGroup("*");
        
        Iterator iter = colAttr.iterator();
        while (iter.hasNext()) {
            objAttr = (Attribute)iter.next();
            
            if (StringUtil.len(objAttr.getColumn()) > 0 && !objAttr.isVirtualColumn()) {
                if (blnFirst) {
                    strTmp = objAttr.getColumn();
                    blnFirst = false;
                    
                } else {
                    strTmp = ", " + objAttr.getColumn();
                    
                }
                
                strTmp = strTmp + columnTypeSQL(pobjDesc, objAttr);
                strSQL = strSQL + strTmp;
            } // Has column and not virtual
            
        } // Loop over attributes
        
        strSQL = strSQL + " )";
        
        /** Add the create table sql. **/
        addStatement(strSQL);
        
        /**
         * Create unique index for the primary key.
         */
        if (StringUtil.len(strPrimaryKey) > 0 && (addIndex || chkAlter) ) {
            // Create the unique index for this column.
            doCreateUniqueIndex(pobjDesc);
        } // Has PK
    }
    
    /**
     * Create the unique indexes for a table.
     * 
     * @param pobjDesc The business object descriptor.
     * @throws Exception Thrown if doCreateUniqueIndex fails.
     */
    public void doCreateUniqueIndex(Descriptor pobjDesc) throws Exception {
        /**
         * If also creating table, will already have created PK. Otherwise do it now:
         * If 'drop' is checked, drop the index first (if appropriate)
         */
        if (StringUtil.len(pobjDesc.getPrimaryKey()) > 0) {
            
            if (databaseType.equals(zXType.databaseType.dbAccess)) {
                /**
                 * Access
                 */
                if (addDropStatement) {
                    addStatement("DROP INDEX pk_" + pobjDesc.getTable() + " ON " + pobjDesc.getTable());
                }
                addStatement("CREATE UNIQUE INDEX pk_" + pobjDesc.getTable() + " ON " + pobjDesc.getTable() +
                                        "(" + pobjDesc.getAttribute(pobjDesc.getPrimaryKey()).getColumn() + ") WITH PRIMARY");
                
            } else if (databaseType.equals(zXType.databaseType.dbOracle)) {
                /**
                 * Oracle
                 */
                if (addDropStatement) {
                    addStatement("DROP INDEX pk_" + pobjDesc.getTable() + " ON " + pobjDesc.getTable());
                }
                addStatement("ALTER TABLE " + pobjDesc.getTable() 
                                        + " ADD CONSTRAINT " + "PK_" + pobjDesc.getTable() + " PRIMARY KEY  " 
                                        + "(" + pobjDesc.getAttribute(pobjDesc.getPrimaryKey()).getColumn() + ")");
                
            } else if (databaseType.equals(zXType.databaseType.dbDB2)) {
                /**
                 * TO DO: DB2
                 */
                
            } else if (databaseType.equals(zXType.databaseType.dbSQLServer)) {
                /**
                 * SQL Server
                 * Cannot drop PK, so don't try
                 */
                addStatement("ALTER TABLE " + pobjDesc.getTable() + " ADD CONSTRAINT " +
                                        "PK_" + pobjDesc.getTable() + " PRIMARY KEY  " +
                                        "(" + pobjDesc.getAttribute(pobjDesc.getPrimaryKey()).getColumn() + ")");
                
            } else if (databaseType.equals(zXType.databaseType.dbMysql)) {
                /**
                 * Mysql - Think mysql does primary key indexes automatically ?
                 */
                if (addDropStatement) {
                    addStatement("DROP INDEX pk_" + pobjDesc.getTable() + " ON " + pobjDesc.getTable());
                }
                addStatement("CREATE UNIQUE INDEX pk_" + pobjDesc.getTable() + " ON " + pobjDesc.getTable() +
                        "(" + pobjDesc.getAttribute(pobjDesc.getPrimaryKey()).getColumn() + ")");
                
                /**
                 * This is a special type of index, this is need for all tables otherwise the primary keys will not work.
                 * 
                 * NOTE : This will also create the index, but this index cannot be dropped!
                 */
                addStatement("ALTER TABLE " + pobjDesc.getTable() + " ADD PRIMARY KEY(`"+ pobjDesc.getAttribute(pobjDesc.getPrimaryKey()).getColumn() +"`)");
                
            } // Database type.
            
        } // Has PK and it not checking for new tables
        
    }
    
    /**
     * Create a index on a column (Most probably one with a foriegn key).
     * 
     * @param pstrTable The table to create the index on.
     * @param pobjAttr The attribute with a foriegn key.
     */
    public void doCreateIndex(String pstrTable, Attribute pobjAttr) {

        String strName;
        
        // Creates the foriegn key indexes.
        
        if (databaseType.equals(zXType.databaseType.dbAccess)) {
            /**
             * Access
             */
            strName = pstrTable + "_" + pobjAttr.getName();
            
            /**
             * If 'drop' is checked, drop the index, but don't do this if the table is also
             * being dropped because that will already have dropped the indexes.
             */
            if (!addTable && addDropStatement) {
                addStatement("DROP INDEX " + strName + " ON " + pstrTable);
            }
            addStatement("CREATE INDEX " + strName + " ON " + pstrTable + " (" + pobjAttr.getColumn() + ")");
            
        } else if (databaseType.equals(zXType.databaseType.dbOracle)) {
            /**
             * Oracle
             */
            strName = nameDDLitem(pstrTable, pobjAttr.getName());
            
            /**
             * If 'drop' is checked, drop the index, but don't do this if the table is also
             * being dropped because that will already have dropped the indexes.
             */
            if (!addTable && addDropStatement) {
                addStatement("DROP INDEX " + strName);
            }
            addStatement("CREATE INDEX " + strName + " ON " + pstrTable + " (" + pobjAttr.getColumn() + ")");
            
            if (pobjAttr.getTextCase().equals(zXType.textCase.tcInsensitive)) {
                /**
                 * Create an index if the column is case insensitive. In Oracle we can create a function
                 * index directly on the column (although behind the scenes it seems to generate its
                 * own column anyway, similar to DB2):
                 */
                strName = nameDDLitem(pstrTable, "UPPER_" + pobjAttr.getName());
                
                if (addDropStatement) {
                    addStatement("DROP INDEX " + strName);
                }
                addStatement("CREATE INDEX " + strName + " ON " + pstrTable + " (UPPER(" + pobjAttr.getColumn() + "))");
                
            } // Case insensitive
            
        } else if (databaseType.equals(zXType.databaseType.dbDB2)) {
            /**
             * DB2
             */
            strName = nameDDLitem(pstrTable, pobjAttr.getName());
            
            /**
             * If 'drop' is checked, drop the index, but don't do this if the table is also
             * being dropped because that will already have dropped the indexes.
             */
            if (!addTable && addDropStatement) {
                addStatement("DROP INDEX " + strName);
            }
            addStatement("CREATE INDEX " + strName + " ON " + pstrTable + " (" + pobjAttr.getColumn() + ")");
            
            if (pobjAttr.getTextCase().equals(zXType.textCase.tcInsensitive)) {
                /**
                 * Create an index if the column is case insensitive. In DB2, we will have created
                 * a 'generated' column in the table DDL, with a column name of UPPER_<attrname>. We
                 * now create the index for that generated column:
                 */
                strName = nameDDLitem(pstrTable, "UPPER_" + pobjAttr.getName());
                
                if (addDropStatement) {
                    addStatement("DROP INDEX " + strName);
                }
                addStatement("CREATE INDEX " + strName + " ON " + pstrTable + " (UPPER(" + pobjAttr.getColumn() + "))");
                
            } // Case insensitive
            
        } else if (databaseType.equals(zXType.databaseType.dbSQLServer)) {
            /**
             * SQL Server
             */
            strName = "IX_" + pstrTable + "_" + pobjAttr.getName();
            
            /**
             * If 'drop' is checked, drop the index, but don't do this if the table is also
             * being dropped because that will already have dropped the indexes.
             */
            if (!addTable && addDropStatement) {
                addStatement("DROP INDEX " + pstrTable + "." + strName );
            }
            
            addStatement("CREATE INDEX " + strName + " ON " + pstrTable + " (" + pobjAttr.getColumn() + ")");
            
        } else if (databaseType.equals(zXType.databaseType.dbMysql)) {
            /**
             * Mysql
             */
            strName = "IX_" + pstrTable + "_" + pobjAttr.getName();
            
            /**
             * If 'drop' is checked, drop the index, but don't do this if the table is also
             * being dropped because that will already have dropped the indexes.
             */
            if (!addTable && addDropStatement) {
                addStatement("DROP INDEX " + pstrTable + "." + strName );
            }
            
            addStatement("CREATE INDEX " + strName + " ON " + pstrTable + " (" + pobjAttr.getColumn() + ")");
            
        } // Database type
        
    }
    /**
     * Generates the sql for creating an index.
     * 
     * @param pobjDesc The business object descriptor.
     * @throws Exception Thrown if doCreateIndexes fails.
     */
    public void doCreateIndexes(Descriptor pobjDesc) throws Exception {
        
        /**
         * If also creating table, will already have created PK. 
         * Otherwise do it now: If 'drop' is checked, drop the index first (if appropriate)
         */
        if (StringUtil.len(pobjDesc.getPrimaryKey()) > 0 && !addTable) {
            doCreateUniqueIndex(pobjDesc);
            
        } // Has PK and it not checking for new tables
        
        /**
         * NOTE : This no longer applies, this is an option of the DDLUtil object. This may be implemented like 
         * this in the BO2DDL gui.
         * 
         * Shift and Generate (Go) when Index is checked is a specialist option that only
         * generates PKs. Otherwise (normally) also generate FKs
         */
        if (addFKIndex) {
            Attribute objAttr;
            AttributeCollection colAttr = pobjDesc.getGroup("*");
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if (StringUtil.len(objAttr.getColumn()) > 0 && StringUtil.len(objAttr.getForeignKey()) > 0) {
                    // Create an index on the foriegn key.
                    doCreateIndex(pobjDesc.getTable(), objAttr);
                } // If relevant
                
            } // Loop over attributes
            
        } // Check for foriegn keys
        
    }
    
    /**
     * Create the sequence if any for a specific business object.
     * 
     * @param pobjDesc The business object's descriptor.
     * @throws Exception Thrown if doCreateSequence fails.
     */
    public void doCreateSequence(Descriptor pobjDesc) throws Exception {
        
        String strSequence = pobjDesc.getSequence();
        if (StringUtil.len(strSequence) > 0) {
            
            if (databaseType.equals(zXType.databaseType.dbAccess)) {
                /**
                 * Access
                 */
                if (addDropStatement) {
                    addStatement("DELETE FROM zXSeqNo WHERE id = '" + strSequence + "'");
                }
                addStatement("INSERT INTO zXSeqNo VALUES('" + strSequence + "', 1)");
                
            } else if (databaseType.equals(zXType.databaseType.dbOracle)) {
                /**
                 * Oracle
                 */
                if (addDropStatement) {
                    addStatement("DROP SEQUENCE " + strSequence + "'");
                }
                addStatement("CREATE SEQUENCE " + strSequence);
                
            } else if (databaseType.equals(zXType.databaseType.dbDB2)) {
                /**
                 * DB2
                 */
                if (addDropStatement) {
                    addStatement("DROP SEQUENCE " + strSequence + "'");
                }
                addStatement("CREATE SEQUENCE " + strSequence);
                
            } else if (databaseType.equals(zXType.databaseType.dbSQLServer)) {
                /**
                 * SQL Server
                 */
                if (addDropStatement) {
                    addStatement("DELETE FROM zXSeqNo WHERE id = '" + strSequence + "'");
                }
                addStatement("INSERT INTO zXSeqNo VALUES('" + strSequence + "', 1)");
                
            } else if (databaseType.equals(zXType.databaseType.dbMysql)) {
                /**
                 * Mysql
                 */
                if (addDropStatement) {
                    addStatement("DELETE FROM zXSeqNo WHERE id = '" + strSequence + "'");
                }
                addStatement("INSERT INTO zXSeqNo VALUES('" + strSequence + "', 1)");
                
            } // Database type
            
        }
        
    }
    
    /**
     * Create the foriegn key constriants for a table.
     * Syntax appears to be the same for all DBMSs. If found not to be, will have to
     * introduce DB-specific case statement here as in other functions.
     * 
     * @param pobjDesc The business object de&scriptor.
     * @throws Exception Thrown if doCreateFKConstraint fails
     */
    public void doCreateFKConstraints(Descriptor pobjDesc) throws Exception {
        
        Attribute objAttr;
        AttributeCollection colAttr = pobjDesc.getGroup("*");
        Iterator iter = colAttr.iterator();
        while (iter.hasNext()) {
            objAttr = (Attribute)iter.next();
            if (StringUtil.len(objAttr.getColumn()) > 0 && StringUtil.len(objAttr.getForeignKey()) > 0) {
                // Create a foreign key constraint on a column :
                doCreateFKConstraint(pobjDesc.getTable(), objAttr);
            }
        }
        
    }
    
    /**
     * Create a foriegn key constraint on the column selected.
     * 
     * @param pstrTable The table that has the foriegn key
     * @param pobjAttr The attribute with the foriegn key.
     * @throws Exception Thrown if doCreateFKConstraint fails.
     */
    public void doCreateFKConstraint(String pstrTable, Attribute pobjAttr) throws Exception {
        Descriptor objDescFK;
        
        if (StringUtil.len(pobjAttr.getColumn()) > 0 && StringUtil.len(pobjAttr.getForeignKey()) > 0) {
            objDescFK = new Descriptor();
            objDescFK.init(getZx().fullPathName(getZx().getBoDir() + File.separatorChar + pobjAttr.getForeignKey() + ".xml"), false);
            objDescFK.setClassName("org.zxframework.ZXBO");
            
            String strName = nameDDLitem(pstrTable, pobjAttr.getName());
            
            /**
             * If 'drop' is checked, drop the constraint, but don't do this if the table is also
             * being dropped because that will already have dropped it.
             */
            if (!addTable && addDropStatement) {
                addStatement("ALTER TABLE " + pstrTable + " DROP CONSTRAINT " + strName);
            }
            addStatement("ALTER TABLE " + pstrTable +
                                    " ADD CONSTRAINT " + strName + " FOREIGN KEY (" + pobjAttr.getColumn() + ")" +
                                    " REFERENCES " + objDescFK.getTable() + " (" + objDescFK.getAttribute(objDescFK.getPrimaryKey()).getColumn() + ")");
        }        
        
    }
    
    /**
     * @return Return the statements built up so far.
     */
    public String dumpStatements() {
        StringBuffer dumpStatements = new StringBuffer();
        String strSeperator;
        if (databaseType.equals(zXType.databaseType.dbSQLServer)) {
            /**
             * SQL Server - follow each line by GO on its own line
             */
            strSeperator = "\nGO\n";
        } else {
            strSeperator = ";\n";
        }
        String strSQL;
        int intStatements = this.statements.size();
        for (int i = 0; i < intStatements; i++) {
            strSQL = (String)this.statements.get(i);
            dumpStatements.append(strSQL).append(strSeperator);
        }
        
        return dumpStatements.toString();
    }
    
    /**
     * Execute all of the statements generated.
     * @throws Exception Thrown if executeStatements fails
     */
    public void executeStatements() throws Exception {
        String strSQL;
        int intStatements = this.statements.size();
        for (int i = 0; i < intStatements; i++) {
            strSQL = (String)this.statements.get(i);
            // Use the primary datasource for executing ddl script.
            getZx().getDataSources().getPrimary().executeDDL(strSQL);
        }
    }
    
    /**
     * Clear the statements.
     */
    public void clearStatements() {
        this.statements = new ArrayList();
    }
    
    //------------------------------------------------------------------------------- Private statements
    
    /**
     * @param pobjDesc The business objects descriptor.
     * @param pobjAttr The attribute to get the sql type for.
     * @return Returns the sql datatype for the specific column.
     * @throws Exception Thrown if columnTypeSQL fails.
     */
    private String columnTypeSQL(Descriptor pobjDesc, Attribute pobjAttr) throws Exception {
        String columnTypeSQL = "";
        
        int dataType = pobjAttr.getDataType().pos;
        
        if (databaseType.equals(zXType.databaseType.dbAccess)) {
            /**
             * Access
             */
            if (dataType == zXType.dataType.dtString.pos || dataType == zXType.dataType.dtExpression.pos) {
                if (pobjAttr.getLength() > 255) {
                    columnTypeSQL = columnTypeSQL + " MEMO";
                } else {
                    columnTypeSQL = columnTypeSQL + " TEXT(" + pobjAttr.getLength() + ")";
                }
                
            } else if (dataType == zXType.dataType.dtBoolean.pos) {
                columnTypeSQL = columnTypeSQL + " YESNO";
                
            } else if (dataType == zXType.dataType.dtDate.pos || dataType == zXType.dataType.dtTimestamp.pos) {
                columnTypeSQL = columnTypeSQL + " DATE";
                
            } else if (dataType == zXType.dataType.dtTime.pos) {
                columnTypeSQL = columnTypeSQL + " TIME";
                
            } else if (dataType == zXType.dataType.dtLong.pos || dataType == zXType.dataType.dtAutomatic.pos) {
                columnTypeSQL = columnTypeSQL + " LONG";
                
            } else if (dataType == zXType.dataType.dtDouble.pos) {
                columnTypeSQL = columnTypeSQL + " DOUBLE";
                
            }
            
            if (pobjAttr.getName().equals(pobjDesc.getPrimaryKey())) {
                columnTypeSQL = columnTypeSQL + " NOT NULL";
            }
            
        } else if (databaseType.equals(zXType.databaseType.dbOracle)) {
            /**
             * Oracle
             */
            if (dataType == zXType.dataType.dtString.pos || dataType == zXType.dataType.dtExpression.pos) {
                if (pobjAttr.getLength() == 1) {
                    columnTypeSQL = columnTypeSQL + " CHAR(1)";
                } else if (pobjAttr.getLength() <= 4000) {
                    columnTypeSQL = columnTypeSQL + " VARCHAR2(" + pobjAttr.getLength() + ")";
                } else {
                    columnTypeSQL = columnTypeSQL + " LONG";
                }
                
            } else if (dataType == zXType.dataType.dtBoolean.pos) {
                columnTypeSQL = columnTypeSQL + " CHAR(1)";
                
            } else if (dataType == zXType.dataType.dtDate.pos 
                            || dataType == zXType.dataType.dtTimestamp.pos 
                            || dataType == zXType.dataType.dtTime.pos) {
                columnTypeSQL = columnTypeSQL + " DATE";
                
            } else if (dataType == zXType.dataType.dtLong.pos 
                            || dataType == zXType.dataType.dtAutomatic.pos 
                            || dataType == zXType.dataType.dtDouble.pos) {
                columnTypeSQL = columnTypeSQL + " NUMBER(" + pobjAttr.getLength() + "," + pobjAttr.getPrecision() + ")";
                
            }
            
            if (pobjAttr.getName().equals(pobjDesc.getPrimaryKey())) {
                columnTypeSQL = columnTypeSQL + " NOT NULL";
            }
            
        } else if (databaseType.equals(zXType.databaseType.dbDB2)) {
            /**
             * DB2 UDB
             */
            if (dataType == zXType.dataType.dtString.pos || dataType == zXType.dataType.dtExpression.pos) {
                if (pobjAttr.getLength() == 1) {
                    columnTypeSQL = columnTypeSQL + " CHAR(1) ";
                } else if (pobjAttr.getLength() <= 4000) {
                    columnTypeSQL = columnTypeSQL + " VARCHAR(" + pobjAttr.getLength() + ") ";
                } else {
                    columnTypeSQL = columnTypeSQL + " LONG VARCHAR ";
                }
                
            } else if (dataType == zXType.dataType.dtBoolean.pos) {
                columnTypeSQL = columnTypeSQL + " CHAR(1)";
                
            } else if (dataType == zXType.dataType.dtDate.pos 
                            || dataType == zXType.dataType.dtTimestamp.pos 
                            || dataType == zXType.dataType.dtTime.pos) {
                columnTypeSQL = columnTypeSQL + " DATE ";
                
            } else if (dataType == zXType.dataType.dtLong.pos || dataType == zXType.dataType.dtAutomatic.pos) {
                if (pobjAttr.getPrecision() == 0) {
                    columnTypeSQL = columnTypeSQL + " INTEGER ";
                } else {
                    columnTypeSQL = columnTypeSQL + " NUMBER(" + pobjAttr.getLength() + "," + pobjAttr.getPrecision() + ") ";
                }
                
            } else if (dataType == zXType.dataType.dtDouble.pos) {
                columnTypeSQL = columnTypeSQL + " DECIMAL(" + pobjAttr.getLength() + "," + pobjAttr.getPrecision() + ") ";
                
            }
            
            if (pobjAttr.getName().equals(pobjDesc.getPrimaryKey())) {
                columnTypeSQL = columnTypeSQL + " NOT NULL ";
            }            
            
        } else if (databaseType.equals(zXType.databaseType.dbSQLServer)) {
            /**
             * SQL server
             */
            if (dataType == zXType.dataType.dtString.pos || dataType == zXType.dataType.dtExpression.pos) {
                if (pobjAttr.getLength() > 8000) {
                    columnTypeSQL = columnTypeSQL + " TEST";
                } else {
                    columnTypeSQL = columnTypeSQL + " VARCHAR(" + pobjAttr.getLength() + ")";
                }
                
            } else if (dataType == zXType.dataType.dtBoolean.pos) {
                columnTypeSQL = columnTypeSQL + " BIT ";
                
            } else if (dataType == zXType.dataType.dtDate.pos 
                            || dataType == zXType.dataType.dtTimestamp.pos 
                            || dataType == zXType.dataType.dtTime.pos) {
                columnTypeSQL = columnTypeSQL + " DATETIME";
                
            } else if (dataType == zXType.dataType.dtLong.pos || dataType == zXType.dataType.dtAutomatic.pos) {
                columnTypeSQL = columnTypeSQL + " INT";
                
            } else if (dataType == zXType.dataType.dtDouble.pos) {
                columnTypeSQL = columnTypeSQL + " FLOAT(53)";
                
            }
            
            if (pobjAttr.getName().equals(pobjDesc.getPrimaryKey())) {
                columnTypeSQL = columnTypeSQL + " NOT NULL ";
            } else {
                columnTypeSQL = columnTypeSQL + "  NULL ";
            }
            
        } else if (databaseType.equals(zXType.databaseType.dbMysql)) {
            /**
             * Mysql
             */
            if (dataType == zXType.dataType.dtString.pos || dataType == zXType.dataType.dtExpression.pos) {
                if (pobjAttr.getLength() == 1) {
                    columnTypeSQL = columnTypeSQL + " CHAR(1) ";
                } else if (pobjAttr.getLength() <= 255) {
                    columnTypeSQL = columnTypeSQL + " VARCHAR(" + pobjAttr.getLength() + ")";
                } else if (pobjAttr.getLength() <= 65535) {
                    columnTypeSQL = columnTypeSQL + " TEXT";
                } else if (pobjAttr.getLength() <= 16777215) {
                    columnTypeSQL = columnTypeSQL + " MEDIUMTEXT";
                } else {
                    columnTypeSQL = columnTypeSQL + " LONGTEXT";
                }
                
            } else if (dataType == zXType.dataType.dtBoolean.pos) {
                columnTypeSQL = columnTypeSQL + "  CHAR(1)";
                
            } else if (dataType == zXType.dataType.dtDate.pos) {
                columnTypeSQL = columnTypeSQL + " DATE";
                
            } else if (dataType == zXType.dataType.dtTimestamp.pos) {
                columnTypeSQL = columnTypeSQL + " DATETIME"; // TIMESTAMP
                
            } else if (dataType == zXType.dataType.dtTime.pos) {
                columnTypeSQL = columnTypeSQL + " TIME"; // DATETIME
                
            } else if (dataType == zXType.dataType.dtLong.pos || dataType == zXType.dataType.dtAutomatic.pos) {
                if (pobjAttr.getPrecision() == 0) {
                    columnTypeSQL = columnTypeSQL + " INTEGER";
                } else {
                    // Maybe not needed. DECIMAL(M,D), NUMERIC(M,D)
                    columnTypeSQL = columnTypeSQL + " DECIMAL(" + pobjAttr.getLength() + "," + pobjAttr.getPrecision() + ")";
                }
                
            } else if (dataType == zXType.dataType.dtDouble.pos) {
                columnTypeSQL = columnTypeSQL + " DECIMAL(" + pobjAttr.getLength() + "," + pobjAttr.getPrecision() + ")";
                
            }
            
            if (pobjAttr.getName().equals(pobjDesc.getPrimaryKey())) {
                columnTypeSQL = columnTypeSQL + " NOT NULL";
            }
        }
        
        return columnTypeSQL;
    }
    
    /**
     * Add the statement to the collection of statements.
     * 
     * @param pstrSQL SQL statement.
     */
    private void addStatement(String pstrSQL) {
        this.statements.add(pstrSQL);
    }
    
    /**
     * Some database types have restrictions on item names. We have to assume that table and column
     * names are defined within these limits. However, indexes are a special case because we
     * automatically generate these by combining the table and column names. Therefore we need to
     * truncate where necessary. If the combined table and column name is longer than the limit we
     * shorten the name to something less than that and combine it with as much of the column name
     * as possible. We use the 'checkSum' routine to convert too-long names into numbers. The checkSum
     * function is defined in this form but lifted more-or-less identically from zX.clsSQL.
     * 
     * @param pstrTable The name of the table.
     * @param pstrColumn The name of the column
     * @return Return a safe name.
     */
    private String nameDDLitem(String pstrTable, String pstrColumn) {
        String nameDDLitem = "";
        
        int intMaxLen;
        int intTableLen;
        
        if (databaseType.equals(zXType.databaseType.dbDB2)) {
            intMaxLen = 18;
            intTableLen = 12;
        } else {
            /**
             * Presumably Oracle or same 30 length restriction
             */
            intMaxLen = 30;
            intTableLen = 20;
        }
        
        if (StringUtil.len(pstrTable + "_" + pstrColumn) > intMaxLen) {
            
            if (StringUtil.len(pstrTable) > intTableLen) {
                pstrTable = checkSum(pstrTable);
            }
            
            if (StringUtil.len(pstrTable + "_" + pstrColumn) > intMaxLen) {
                pstrColumn = checkSum(pstrColumn);
            }
            
            /**
             * VB Code :
            If Len(pstrTable) > intTableLen Then
                pstrTable = Left$(pstrTable, (intTableLen - 5)) & checkSum(Mid(pstrTable, (intTableLen - 5) + 1))
            End If
            
            If Len(pstrTable & "_" & pstrColumn) > intMaxLen Then
                pstrColumn = Left$(pstrColumn, ((intMaxLen - 1) - (Len(pstrTable) + 5))) & checkSum(Mid(pstrColumn, ((intMaxLen - 1) - (Len(pstrTable) + 5)) + 1))
            End If
             */
        }
        
        // NOTE : Table name may be to long is the total is less than intMaxLen
        nameDDLitem = pstrTable + "_" + pstrColumn;
        
        return nameDDLitem;
    }
    /**
     * Translate string into checksum that is shorter 
     * in length. This is used to handle generated
     * table / columnnames that are too long
     * for the database to handle.
     * 
     * @param pstr The string to encode.
     * @return Return a shorter encoded string.
     */
    private String checkSum(String pstr) {
        String checkSum = "";
        /**
         * The algorithm used is very simple:
         * - for each character determine the offset in the string of possible characters
         * - Add the offset to a total and add the offset in the original string
         * - Return the total as a formatted string of 5 positions
         */
        /**
         * VB Code :
            intLength = Len(pstrStr)
            For i = 1 To intLength
                strChar = UCase$(Mid(pstrStr, i, 1))
                
                lngCheckSum = (lngCheckSum) + InStr(cstrValidChars, strChar) + i
            Next
            '----
            ' Was using hash - zero is better as forces all positions to be used.
            '----
            checkSum = Left$(Format(lngCheckSum, "00000"), 5)

         * 
         */
        checkSum = pstr.hashCode() + "";
        
        return checkSum;
    }
    
    /**
     * Check whether the bo attribute type is the same as the database.
     * 
     * @param pintBOType The bo type
     * @param pintDBType The db type.
     * @return Returns whether that are the same or not.
     */
    private boolean checkType (int pintBOType, int pintDBType) {
        // No change as default
        boolean checkType = false;
        
        if (pintBOType == zXType.dataType.dtAutomatic.pos || pintBOType == zXType.dataType.dtLong.pos) {
            checkType = (pintDBType != Types.INTEGER && pintDBType != Types.NUMERIC);
            
        } else if (pintBOType == zXType.dataType.dtBoolean.pos) {
            boolean blnCharDB = databaseType.equals(zXType.databaseType.dbOracle) 
                                                    || databaseType.equals(zXType.databaseType.dbMysql) 
                                                    || databaseType.equals(zXType.databaseType.dbDB2);
            checkType = (pintDBType != Types.BOOLEAN && (blnCharDB && pintDBType != Types.CHAR));
            
        } else if (pintBOType == zXType.dataType.dtDate.pos) {
            boolean blnTimestampDB = databaseType.equals(zXType.databaseType.dbAccess);
            checkType = (pintDBType != Types.DATE && (blnTimestampDB && pintDBType != Types.TIMESTAMP));
            
        } else if (pintBOType == zXType.dataType.dtDouble.pos) {
            checkType = pintDBType != Types.DOUBLE;
            
        } else if (pintBOType == zXType.dataType.dtString.pos || pintBOType == zXType.dataType.dtExpression.pos) {
            checkType = (pintDBType != Types.CHAR 
                                && pintDBType != Types.CLOB 
                                && pintDBType != Types.VARCHAR 
                                && pintDBType != Types.LONGVARCHAR);
            
        } else if (pintBOType == zXType.dataType.dtTime.pos) {
            checkType = (pintDBType != Types.TIME);
            
        } else if (pintBOType == zXType.dataType.dtTimestamp.pos) {
            /**
             *  BD15MAR04 Timestamp in access is implemented as a date
             */
            if (databaseType.equals(zXType.databaseType.dbAccess)) {
                checkType = (pintDBType != Types.TIMESTAMP && pintDBType != Types.TIME);
            } else {
                checkType = (pintDBType != Types.TIMESTAMP);
            }
        }
        
        return checkType;
    }
}