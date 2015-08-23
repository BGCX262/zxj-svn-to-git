/*
 * Created on Apr 15, 2005
 * $Id: DSHRdbms.java,v 1.1.2.45 2006/07/17 16:40:17 mike Exp $
 */
package org.zxframework.datasources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jdom.Element;

import org.zxframework.Attribute;
import org.zxframework.BORelation;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.exception.NestableException;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.exception.ParsingException;
import org.zxframework.jdbc.ZXResultSet;
import org.zxframework.property.BooleanProperty;
import org.zxframework.property.DateProperty;
import org.zxframework.property.DoubleProperty;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.sql.Query;
import org.zxframework.sql.QueryDef;
import org.zxframework.transaction.JDBCTransaction;
import org.zxframework.transaction.Transaction;
import org.zxframework.util.JDBCUtil;
import org.zxframework.util.StringUtil;

/**
 * The data-source handler for a relational database. 
 * 
 * <pre>
 * 
 * This handler implementation will
 * be used for the primary and all alternative data sources.
 * 
 * It replaces much of clsDB and the more recordset related methods of clsBOS although these
 * will remain active for backward compatability
 *
 * There is one obscure setting: asChannel; when this is set to true (or 'on' or 'yes' or '1')
 * it means that this data-source behaves like a channel. This can be used to test
 * functionality that is different for a channel than for a RDBMS
 * 
 * Change    : BD21APR05 - V1.5:5
 * Why       : Added support for transaction manager
 * 
 * Change    : BD6MAY05 - V1.4:75
 * Why       : In updateBO we could get an ugly error message in the log if the
 *             uniqueConstraint group contained attributes whose values had not been
 *             set. Ideally, we would ensure that all the values in the uniqueConstraint
 *             are set or, if not set by developer, loaded from DB. However, in this 1.4
 *             version we cannot tell a true null value from a null value because the value
 *             was never set and so we do the next best thing: if the updateGroup does not
 *             fully contain the uniqueConstraint group we will NOT bother to check whether
 *             the uniqueConstraint is being violated.
 *             Note: we will implement a more complete solution as soon as we
 *             support persistStatus on properties
 *             
 * Change    : BD21APR05 - V1.5:13
 * Why       : Implement paging for recordsets
 * 
 * Change    : BD18MAY05 - V1.5:14
 * Why       : sql.DbRowLimit (see V1.5:13) needs to have additional parameter (dsHandler) as we
 *             are no longer allowed to tell the database type by looking at zX.db.dbType
 *
 * Change    : BD2SEP05 - V1.5:44
 * Why       : For updateBO only do a doesExsist on the uniqueConstraint if any of the attributes
 *             in uniqueConstraint are actually in the updateGroup; if not a) the update can never
 *             violate the unique constraint and b) it may be that some of the attributes in the
 *             uniqueConstraint group do not have a value but are mandatory and thus generate an
 *             error in the log message
 *
 * Change    : BD2SEP05 - V1.5:46
 * Why       : UpdateBO and deleteBO can end-up in endless loop as we do not check the return
 *             code for the moneNext when we loop over the rows to update / delete.
 *             The moveNext should really always work but Access has one known problem
 *             with boolen (ie yes/no columns) that are optional; leaving such a column
 *             blank will cause the moveNext to fail and thus the deleteBO / updateBO to
 *             end up in an endless loop
 *               
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class DSHRdbms extends DSHandler {
    
    //------------------------ Implementation specific properties
    
    private zXType.databaseType dbType;
    private String dsn;
    private int timeOut;
    private String schema = "";
    
    /** TEST MODE ONLY  **/ 
    private String jdbcdriver;
    private String jdbcurl;
    private String username;
    private String password;
    /** TEST MODE ONLY  **/ 
    
    /** Connection to the database. **/
    private Connection connection;
    
    /**
     * Unused at the moment..
     */
//    private boolean alternateActive;
//    private zXType.databaseType alternateDatabaseType;
//    private Connection alternateConnection;
//    private String alternateDateFormat;
//    private int alternateTimeout;
//    private String alternateRef;
//    private ArrayList alternateInTransaction;

    
/** Keep track of open statements and resultsets **/
//    private ArrayList openRs = new ArrayList();
//    private ArrayList openStmt = new ArrayList();
/** Keep track of open statements and resultsets **/
    
    /** Track the number of open connections : **/ 
    private static int DEBUG_open_connections = 0;

    // Allow for multiple database connection, this is sometimes used for nested transactions.
    private Connection secondaryConnection;
    private boolean secondaryActive;
    
    private ArrayList inTX = new ArrayList();
    private ArrayList secondaryInTX = new ArrayList();
    private boolean secondaryInTransaction;

    // Some extra caching, yeah :).
    /** The last BO loaded **/
    private static String strPrevBO;
    /** The last alias used to load BO **/
    private static String strPrevAlias;
    /** The last attribute group used to load **/
    private static String strPrevGroup;
    /** The last resolvefk setting **/
    private static boolean blnPrevResolveFK;
    /** The last loadBO sql query used.  **/
    private static String strPrevQry;
    
    /**
     * DSHandler managed transactions.
     **/
    private JDBCTransaction currentTransaction;
    private boolean inTransaction;
    
    //------------------------ Getters Setters
    
    /**
     * @return Returns the dbType.
     */
    public zXType.databaseType getDbType() {
        return dbType;
    }
    
    /**
     * @param dbType The dbType to set.
     */
    public void setDbType(zXType.databaseType dbType) {
        this.dbType = dbType;
    }
    
    /**
     * @return Returns the dsn.
     */
    public String getDsn() {
        return dsn;
    }
    
    /**
     * @param dsn The dsn to set.
     */
    public void setDsn(String dsn) {
        this.dsn = dsn;
    }
    
    /**
     * @return Returns the jdbcdriver.
     */
    public String getJdbcdriver() {
        return jdbcdriver;
    }
    
    /**
     * @param jdbcdriver The jdbcdriver to set.
     */
    public void setJdbcdriver(String jdbcdriver) {
        this.jdbcdriver = jdbcdriver;
    }
    
    /**
     * @return Returns the jdbcurl.
     */
    public String getJdbcurl() {
        return jdbcurl;
    }
    
    /**
     * @param jdbcurl The jdbcurl to set.
     */
    public void setJdbcurl(String jdbcurl) {
        this.jdbcurl = jdbcurl;
    }
    
    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Optional schema used when generating table-names.
     * 
     * @return Returns the schema.
     */
    public String getSchema() {
        return schema;
    }
    
    /**
     * @param schema The schema to set.
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    /**
     * @return Returns the timeOut.
     */
    public int getTimeOut() {
        return timeOut;
    }
    
    /**
     * @param timeOut The timeOut to set.
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }
    
    /**
     * Indicates what database connection is currently
     * active (primary = false, secondary = true).
     * 
     * @return Returns the secondaryActive.
     */
    public boolean isSecondaryActive() {
        return secondaryActive;
    }

    /**
     * Indicates what database connection is currently active (primary = false,
     * secondary = true)
     * 
     * @param secondaryActive The secondaryActive to set.
     */
    public void setSecondaryActive(boolean secondaryActive) {
        this.secondaryActive = secondaryActive;
    }

    /**
     * @return Returns the secondaryInTransaction.
     */
    public boolean isSecondaryInTransaction() {
        return this.secondaryInTransaction;
    }
    
    //------------------------ Special getters and setters
    
    /**
     * Returns a handle to the connection to the database or optionally to the hibernate session.
     * 
     * @return Returns the connection.
     */
    private Connection getConnection() {  
        try {
            
            if (this.connection == null) {
                this.connection = openDBConnection();
                getZx().log.error("Connection is NULL");
            } else if (this.connection.isClosed()) {
                getZx().log.error("Connection is closed");
                this.connection = openDBConnection();
            }
            
        } catch (Exception e) {
            getZx().log.error("Failed to get connection", e);
        }
        return this.connection;
    }
    
    //------------------------ OLD DB
    
    /**
     * Get a connection to the database.
     * 
     * @return Returns a connection to be used.
     * @throws Exception Thrown if we fail to get a connection the datbase.
     */
    private Connection openDBConnection() throws Exception {
        Connection openDBConnection = null;
        
        if (StringUtil.len(this.dsn) > 0) {
            /**
             * Get the JNDI context
             */
    		Context ctx = new InitialContext();
            if(ctx == null ) throw new RuntimeException("Boom - No Context");
            
            /**
             * Get the datasource : 
             */
            DataSource ds;
            try {
                ds = (DataSource)ctx.lookup(dsn);
            } catch (Exception e) {
                throw new NestableRuntimeException("Failed to get a jndi datasource for : " + dsn + " check your configuration settings.", e);
            }
            
            /**
             * Open database connection (no error handling required; will bomb out on failure)
             */
            openDBConnection = ds.getConnection();
            
        } else {
        	/**
        	 * Used for unit testing and running as a standalone application.
        	 */
        	try {
                Class.forName(getJdbcdriver());
        	} catch (Exception e) {
        		throw new NestableRuntimeException("Failed to find correct driver, check your class path or config settings.", e);
        	}
            
            Properties prop = new Properties();
            prop.setProperty("user", getUsername());
            if (StringUtil.len(getPassword()) > 0) {
                prop.setProperty("password", getPassword());
            }
            
            try {
                openDBConnection = DriverManager.getConnection(getJdbcurl(), prop);
            } catch (Exception e) {
            	throw new NestableRuntimeException("Failed to open a connection to the database : " + getJdbcurl(), e);
            }
        }
        
        /**
         * Retry, maybe we some how got a stale connection
         */
        if (openDBConnection == null || openDBConnection.isClosed()) {
            getZx().log.error("Failed the first time to get a connection");
            openDBConnection = openDBConnection();
        }
        
        if (openDBConnection.isClosed()) {
            throw new NestableRuntimeException("We have a bad connection here, try tweaking the database connection settings.");
        }
        
        if (getZx().log.isInfoEnabled()) {
            DEBUG_open_connections = DEBUG_open_connections + 1;
            getZx().log.info("Opened a connectoin : "  + DEBUG_open_connections); // , new Exception());
        }
        
        return openDBConnection;
    }
    
    /**
     * Make the secondary connection the first one
     * and the first the second one (or the other way
     * around as it may be).
     * 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if swapConnection fails
     */
    public zXType.rc swapConnection() throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc swapConnection = zXType.rc.rcOK;
        
        try {
            Connection objSwap = this.connection;
            boolean blnSwap = this.inTransaction;
            ArrayList arrSwap = this.inTX;
            
            this.connection = this.secondaryConnection;
            this.inTX = this.secondaryInTX;
            this.inTransaction = this.secondaryInTransaction;
            
            this.secondaryConnection = objSwap;
            this.secondaryInTX = arrSwap;
            this.secondaryInTransaction = blnSwap;
            
            this.secondaryActive = !this.secondaryActive;
            
            /**
             * We only make the secondary connection when there is a need to
             */
            if (this.secondaryActive) {
                /**
                 * And connect the secondary connection as well
                 */
                if (this.connection != null && !this.connection.isClosed()) {
                    return swapConnection;
                }
                
                this.connection = openDBConnection();
            }
            
            return swapConnection;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Swap connections", e);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return swapConnection;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Execute a query definition and return a specific column as property.
     *
     * @param pstrQueryDef The name of the query def. 
     * @return Returns a quick property with no attribute associated with it. 
     * @throws ZXException  Thrown if executeQuickQueryDef fails. 
     */
	public Property executeQuickQueryDef(String pstrQueryDef) throws ZXException {
		return executeQuickQueryDef(pstrQueryDef, 1);
	}
	
    /**
     * Execute a query definition and return a specific column as property.
     * 
     * <pre>
     * 
     * NOTE: In java the resultset is 1 based and in vb it is 0 based.
     * </pre>
     * 
     * @param pstrQueryDef The name of the query def. 
     * @param pintColumn The column index to return. Optional, default is 1.
     * @return Returns a quick property with no attribute associated with it. 
     * @throws ZXException  Thrown if executeQuickQueryDef fails. 
     */
     public Property executeQuickQueryDef(String pstrQueryDef, int pintColumn) throws ZXException {
         if(getZx().trace.isFrameworkCoreTraceEnabled()) {
             getZx().trace.enterMethod();
             getZx().trace.traceParam("pstrQueryDef", pstrQueryDef);
             getZx().trace.traceParam("pintColumn", pintColumn);
         }

         Property executeQuickQueryDef = null;
         
         DSRS objRS = null;
         
         try {
             /**
              * Initialise the query definition
              */
             QueryDef objQryDef = new QueryDef();
             objQryDef.init(pstrQueryDef);
             
             objRS = objQryDef.rs();
             int intColumnCount = objRS.getRs().getTarget().getMetaData().getColumnCount();
             if (pintColumn > intColumnCount) {
                 throw new ZXException("Invalid column number," 
                		               + pintColumn + ", only " 
                		               + intColumnCount + " available");
             }
             
             String strValue = objRS.getRs().getTarget().getString(pintColumn);
             if (strValue == null) {
                 executeQuickQueryDef = new StringProperty("", true);
             } else {
                 executeQuickQueryDef = new StringProperty(strValue, false);
             }
             
             return executeQuickQueryDef;
         } catch (Exception e) {
             getZx().trace.addError("Failed to : Execute a query definition and return a specific column as property.", e);
             if (getZx().log.isErrorEnabled()) {
                 getZx().log.error("Parameter : pstrQueryDef = "+ pstrQueryDef);
                 getZx().log.error("Parameter : pintColumn = "+ pintColumn);
             }
             
             if (getZx().throwException) throw new ZXException(e);
             return executeQuickQueryDef;
         } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
             if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                 getZx().trace.returnValue(executeQuickQueryDef);
                 getZx().trace.exitMethod();
             }
         }
     }
     
    /**
     * Execute sql query.
     * 
     * NOTE : This should only be use when executing a ddl script.
     * 
     * @param pstrQry The ddl sql query to run.
     * @throws Exception Thrown if executeDDL fails.
     */
    public void executeDDL(String pstrQry) throws Exception {
        // The callee has do all of the exception handling etc..
        getConnection().createStatement().execute(pstrQry);
    }
    
    /**
     * Generate sequence number
     * 
     * @param pstrSeqName Name of sequence to use
     * @return Returns the sequence
     * @throws ZXException Thrown if getSeqNo fails.
     */
    public int getSeqNo(String pstrSeqName) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrSeqName", pstrSeqName);
        }
        
        int getSeqNo = -1;
        
        Transaction transaction = null;
        DSRS objRS = null;

        try {
            String strQry;
            
            if (this.dbType.equals(zXType.databaseType.dbOracle)) {
                strQry = " SELECT " + pstrSeqName.trim() + ".NEXTVAL FROM DUAL";
                
                objRS = sqlRS(strQry);
                if (objRS == null) {
                    throw new Exception("Unable to execute query to retrieve nextval from dual");
                }
                
                /**
                 * return next sequence number
                 */
                getSeqNo = objRS.getRs().getTarget().getInt(1);
                
            } else if (this.dbType.equals(zXType.databaseType.dbDB2) && false){
                strQry = "VALUES NEXTVAL FOR " + pstrSeqName.trim();
                
                objRS = sqlRS(strQry);
                if (objRS == null) {
                    throw new Exception("Unable to execute query to retrieve nextval");
                }
                try {
                    /**
                     * return next sequence number
                     */
                    getSeqNo = objRS.getRs().getTarget().getInt(1);
                    
                } catch (Exception e) {
                    getZx().trace.addError("Failed to get the sequence number", e);
                } finally {
                    /**
                     * Clean up code goes here.
                     */
                }
                
            } else {
                /**
                 * This is for any generic db : 
                 */
                
                /**
                 * In databases without the sequence facility, we have to do it
                 * a bit more advanced:
                 * Swap database connection so that we can create a new transaction
                 * Lock row 
                 * Update row
                 */
                swapConnection();
                
                /**
                 * Start tx.
                 */
                transaction = beginTransaction();
                
                /**
                 * 
                 */
                strQry = "UPDATE zxSeqNo SET nxtSeqNo = nxtSeqNo + 1 WHERE id = '" + pstrSeqName + "'";
                getSeqNo = sqlExecute(strQry);
                
                if (getSeqNo != 1) {
                    /**
                     * Could be that there is no sequence set up : So lets create one :
                     */
                    strQry = "INSERT INTO zxSeqNo (id, nxtSeqNo) VALUES ('" + pstrSeqName + "', 1)";
                    getSeqNo = sqlExecute(strQry);
                    if (getSeqNo != 1) {
                        throw new Exception("Unable to create zXSeqNo.");
                    }
                    
                    /**
                     * And commit
                     */
                    transaction.commit();
                    
                    /**
                     * Start new tx
                     */
                    transaction = beginTransaction();
                }
                
                /**
                 * And select the next value
                 */
                strQry = "SELECT nxtSeqNo FROM zxSeqNo WHERE id = '" + pstrSeqName + "'";
                
                objRS = sqlRS(strQry);
                if (objRS == null) {
                    throw new Exception("Unable to execute query to retrieve next sequence");
                }
                
                /**
                 * return next sequence number
                 */
                getSeqNo = objRS.getRs().getTarget().getInt(1);
                
                /**
                 * And commit. 
                 */
                transaction.commit();
                
                /**
                 * Swap connection back.
                 */
                swapConnection();
            }
            
            return getSeqNo;
        } catch (Exception e) {
            getZx().trace.addError("Failed : Generate sequence number", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrSeqName = " + pstrSeqName);
            }
            
            try {
                /**
                 * If we banged out while the secondary connection is active: swap back
                 */
                if (transaction != null) {
                    transaction.rollback();
                }
                
            } catch (Exception e1) {
                getZx().log.error("Failed to close trans", e1);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getSeqNo;
        } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getSeqNo);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Performs a select sql statement and returns an updatable resultset.
     * 
     * <pre>
     * 
     * NOTE : The table that the business object sits on has to have a primary key to have use this.
     * NOTE : Used in the updatBO method.
     * </pre>
     * 
     * @param pstrSQL The SQL statement you want to execute.
     * @return Returns the ZXResultset object.
     * @throws ZXException Thrown if tableRS fails
     */
    private DSRS tableRS(String pstrSQL) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrSQL", pstrSQL);
        }
        
        DSRS tableRS = new DSRS(this);
        Statement stmt = null;
        
        try {
            /**
             * 29JUN2004: SQL Server does not fully support nested firehose cursors within the
             * same transaction. Therefore we must use a dynamic recordset for that DBMS.
             * 30JUN2004: Only when in a transaction.
             */
            
            /**
             * This resultset IS updatable :)
             */
            stmt = getConnection().prepareStatement(pstrSQL, 
                                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                    ResultSet.CONCUR_UPDATABLE);
                                                    
            tableRS.setRs(new ZXResultSet(stmt, ((PreparedStatement)stmt).executeQuery(), getDbType()));
            
            tableRS.moveNext();
            
            return tableRS;
            
        } catch (Exception e) {
            // Clean up in case anything goes wrong.
        	JDBCUtil.closeStatement(stmt);
        	
            getZx().trace.addError("Execute query and return recordset", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrSQL = " + pstrSQL);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return tableRS;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(tableRS);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Returns a updatable resultset, this is usefull for the insertBO.
     * 
     * @param pobjBO The ZXBO the have the rowset of
     * @return Returns a rowset
     * @throws ZXException Thrown if tableRS fails
     */
    public DSRS tableRS(ZXBO pobjBO) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
        }
        
        DSRS tableRS = null;
        
        try {
            /**
             * 29JUN2004: SQL Server does not fully support nested firehose cursors within the
             * same transaction. Therefore we must use a dynamic recordset for that DBMS.
             * 30JUN2004: Only when in a transaction.
             */
            String strSQL = "SELECT " + pobjBO.getDescriptor().getTable() + ".* FROM " + pobjBO.getDescriptor().getTable();
            
            tableRS = tableRS(strSQL);
            
            setLastErrorNumber(zXType.rc.rcOK);
            setLastErrorString("");
            
            return tableRS;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Return a recordset of type adTable ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return tableRS;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(tableRS);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * The simple version of deleteBO; simply allowed with no relations to other
     * entities.
     * 
     * @param pobjBO The business object to delete
     * @param pstrWhereGroup The SQL Where group to use.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if simpleDeleteBO fails.
     */
    private zXType.rc simpleDeleteBO(ZXBO pobjBO, String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }

        zXType.rc simpleDeleteBO = zXType.rc.rcOK;
        
        try {

            /**
             * Generate query DGS09FEB2005: It is valid to pass an empty where
             * condition, in which case don't append the 'AND'. Better process
             * it first in case it is an expression that evaluates to empty
             * string.
             */
            String strQry = getZx().getSql().deleteQuery(pobjBO);
            String strWhereCondition = getZx().getSql().whereCondition(pobjBO, pstrWhereGroup);
            if (StringUtil.len(strWhereCondition) > 0) {
                strQry = strQry + " AND " + strWhereCondition;
            }

            /**
             * For an audit BO and with the PK as wheregroup: add the audit
             * clause to the where clause only when zXUpDtdId has been set
             * 
             * BD9JUN04 - Now make distinction between auditable and concurrency
             * control
             */
            if (pobjBO.getDescriptor().isConcurrencyControl()
                && pstrWhereGroup.equals("+")) {
                if (!pobjBO.getValue("zXUpdtdId").isNull) {
                    // strQry = strQry + " AND " +
                    // this.zx.getSql().whereCondition(this, "zXUpdtdId");
                    strQry = strQry + " AND " 
                                    + getZx().getSql().concurrencyControlWhereCondition(pobjBO);
                }
            }

            /**
             * And execute
             */
            int intRowsAffected = sqlExecute(strQry);
            if (intRowsAffected == 0) {
                simpleDeleteBO = zXType.rc.rcWarning;
            }

            return simpleDeleteBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : The simple version of deleteBO.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            simpleDeleteBO = zXType.rc.rcError;
            return simpleDeleteBO;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(simpleDeleteBO);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * The version of deleteBO that uses the BO model table.
     * 
     * NOTE : DO NOT USE THIS ANY MORE.
     * 
     * @param pobjBO The business object to delete
     * @param pstrWhereGroup The where group to use in the delete
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if boModelDeleteBO fails.
     */
    private zXType.rc boModelDeleteBO(ZXBO pobjBO, String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }

        zXType.rc boModelDeleteBO = zXType.rc.rcOK;
        
        DSRS objRS = null;
        DSRS objBOMdlRs = null;
        
        /**
         * Handle defaults
         */
        if (pstrWhereGroup == null) {
            pstrWhereGroup = "+";
        }
        
        try {

            /**
             * BD7DEC02; before we delete anything, we check wether there are no
             * BOs out there that are associated with the BO that we are about
             * to delete
             */

            /**
             * Create instance of zXBOMdl to check for associated BOs
             */
            ZXBO objBOMdl = getZx().createBO("zXBOMdl");
            if (objBOMdl == null) {
                throw new Exception("Unable to create instance of zXBOMdl");
            }

            /**
             * Create a select query so we can treat each BO that we want to
             * delete individually
             * 
             * BD9DEC02 - Use purenames as Oracle doesnt like it when we delete
             * on a recordset when we use aliases
             */
            String strQry = getZx().sql.loadQuery(pobjBO, 
                                                  pobjBO.getDescriptor().getPrimaryKey(), 
                                                  false, true);

            /**
             * Add the where clause :
             */
            if (StringUtil.len(pstrWhereGroup) > 0) {
                strQry = strQry + " AND " + getZx().sql.whereCondition(pobjBO, pstrWhereGroup);
            }

            /**
             * For an audit BO and with the PK as wheregroup: add the audit
             * clause to the where clause
             * 
             * BD9JUN04 - Now make distinction between auditable and concurrency
             * control
             */
            if (pobjBO.getDescriptor().isConcurrencyControl()
                    && pstrWhereGroup.equals("+")) {
                // strQry = strQry + " AND " +
                // zx.sql.whereCondition(this,"zXUpdtdId");
                strQry = strQry + " AND "
                         + getZx().sql.concurrencyControlWhereCondition(pobjBO);
            }

            objRS = tableRS(strQry);
            if (objRS == null) {
                throw new Exception("Unable to execute select query for delete");
            }

            // Track the number of deleted rows :
            int intNumDeleted = 0;
            
            while (!objRS.eof()) {
                /**
                 * DGS15APR2003: Set the PK into the BO so that the
                 * Me.GetPK(pobjBO) works later on. Note that we must use 'Pure'
                 * attr name here because that is how we have them in the RS (or
                 * Oracle won't delete).
                 */
                pobjBO.rs2obj(objRS.getRs(), pobjBO.getDescriptor().getPrimaryKey(), false, true);

                /**
                 * For each BO that we may wich to delete: check for associated
                 * BOs
                 */
                objBOMdl.setValue("toBO", pobjBO.getDescriptor().getName());

                String strBOMdlQry = getZx().sql.loadQuery(objBOMdl, "*", false, false);
                strBOMdlQry = strBOMdlQry + " AND "
                                          + getZx().sql.whereCondition(objBOMdl, "toBO");
                
                objBOMdlRs = sqlRS(strBOMdlQry);
                if (objBOMdlRs == null) {
                    throw new Exception("Unable to execute query for BOMdl");
                }
                
                while (!objBOMdlRs.eof()) {
                    objBOMdlRs.rs2obj(objBOMdl, "*");

                    /**
                     * We have found a BO that may be associated with this BO so
                     * we better delete those as well
                     */
                    ZXBO objAssociatedBO = getZx().createBO(objBOMdl.getValue("frmBO").getStringValue());
                    if (objAssociatedBO == null) {
                        throw new Exception("Unable to create instance of associated BO : "
                                            + objBOMdl.getValue("frmBO").getStringValue());
                    }

                    objAssociatedBO.setValue(objBOMdl.getValue("lnkAttr").getStringValue(), 
                                             pobjBO.getPKValue());

                    /**
                     * DGS11AUG2004: If the delete rule is noAssociates, don't
                     * try to delete the associates, just see if there are any.
                     * If so, we don't delete this row.
                     */
                    int lngCount;
                    zXType.rc deleteBOrc;
                    if (pobjBO.getDescriptor().getDeleteRule().equals(zXType.deleteRule.drNoAssociates)) {
                        lngCount = pobjBO.countByGroup(objBOMdl.getValue("lnkAttr").getStringValue());
                        if (lngCount == -1) {
                            deleteBOrc = zXType.rc.rcError;
                        } else if (lngCount == 0) {
                            deleteBOrc = zXType.rc.rcWarning;
                        } else {
                            deleteBOrc = zXType.rc.rcOK;
                        }

                    } else {
                        deleteBOrc = objAssociatedBO.deleteBO(objBOMdl.getValue("lnkAttr").getStringValue());
                    }

                    if (deleteBOrc == null || deleteBOrc.equals(zXType.rc.rcOK)) {
                        /**
                         * We have deleted associated BOs; this could be bad or
                         * good news:
                         */
                        if (pobjBO.getDescriptor().getDeleteRule().equals(zXType.deleteRule.drNoAssociates)) {
                            /**
                             * It was no good news: the delete rule of this BO
                             * dictates that we cannot delete if any BOs are
                             * still associated with us; so we better behave and
                             * return to the caller We assume that the caller
                             * handles the transaction properly
                             */
                            throw new Exception("Delete failed, still instance of "
                                                + objAssociatedBO.getDescriptor().getLabel().getLabel().trim() 
                                                + " associated");
                        }
                        
                        /**
                         * Only allowed when the associated BO delete rule
                         * is set to cascade. This allows some associations
                         * to be excluded from a cascading delete
                         */
                        if (!((BooleanProperty) objBOMdl.getValue("cscde")).getValue()) { 
                            throw new Exception("Delete failed, still instance of "
                                                + objAssociatedBO.getDescriptor().getLabel().getLabel()
                                                + " associated");
                        }
                        
                    } else if (deleteBOrc.equals(zXType.rc.rcWarning)) {
                        /**
                         * No associated BO deleted, this is always good news
                         */
                    } else {
                        throw new Exception("Unable to delete associated BO");
                    }
                    
                    objBOMdlRs.moveNext();
                }

                /**
                 * Is the BO delete-able?
                 * 
                 * You may wonder why we only do this check so late in the
                 * process: it is to make sure that when no items are deleted
                 * (because there are no items to delete) this does not cause an
                 * error message
                 */
                if (pobjBO.getDescriptor().getDeleteRule().equals(zXType.deleteRule.drNotAllowed)) {
                    throw new Exception("Business object "
                                        + pobjBO.getDescriptor().getLabel().getLabel()
                                        + " cannot be deleted according to delete rule");
                }

                intNumDeleted++;

                /**
                 * Now we have to delete this one as well
                 */
                objRS.getRs().deleteRow();
                
                objRS.moveNext();
            }

            if (intNumDeleted == 0) {
                boModelDeleteBO = zXType.rc.rcWarning;
            } else {
                // objRS.updateRow();
            }

            return boModelDeleteBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : The version of deleteBO that uses the BO model table.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            boModelDeleteBO = zXType.rc.rcError;
            return boModelDeleteBO;
        } finally {
            /**
             * Close resultset.
             */
            if (objBOMdlRs != null) objBOMdlRs.RSClose();
            if (objRS != null) objRS.RSClose();
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(boModelDeleteBO);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * The version of deleteBO that uses the BO relations. Need to look at
     * Hibernate and see how they implement deletes and batch updates.
     * 
     * Reviewed for 1.5:46
     * 
     * @param pobjBO The business object to delete.
     * @param pstrWhereGroup The wheregroup used. Optional, default should be "+"
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if boRelationDeleteBO fails.
     */
    private zXType.rc boRelationDeleteBO(ZXBO pobjBO, String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }

        zXType.rc boRelationDeleteBO = zXType.rc.rcOK;
        
        DSRS objRS = null;
        
        try {

            /**
             * Create a select query so we can treat each BO that we want to
             * delete individually
             * 
             * Use purenames as Oracle doesnt like it when we delete on a
             * recordset when we use aliases
             */
            String strQry = getZx().getSql().loadQuery(pobjBO,
                                                       pobjBO.getDescriptor().getPrimaryKey(), 
                                                       false, true);
            // Add the where group attribute clause.
            if (StringUtil.len(pstrWhereGroup) > 0) {
                strQry = strQry + " AND " + getZx().getSql().whereCondition(pobjBO, pstrWhereGroup);
            }

            /**
             * For an audit BO and with the PK as wheregroup: add the audit
             * clause to the where clause
             * 
             * BD9JUN04 - Now make distinction between auditable and concurrency
             * control
             */
            if (pobjBO.getDescriptor().isConcurrencyControl() && pstrWhereGroup.equals("+")) {
                strQry = strQry + " AND " + getZx().getSql().concurrencyControlWhereCondition(pobjBO);
                // strQry = strQry + " AND " +
                // this.zx.getSQL().whereCondition(this, "zXUpdtdId");
            }

            objRS = tableRS(strQry);
            if (objRS == null) {
                throw new Exception("Failed to execute SQL query : " + strQry);
            }

            int intNumDeleted = 0;
            BORelation objRelation;
            ZXBO objAssociatedBO;
            String strFKAttr;
            Attribute objAttr;
            zXType.rc rcRc;

            while (!objRS.eof()) {
                /**
                 * Set the PK into the BO so that the Me.GetPK(pobjBO) works
                 * later on. Note that we must use 'Pure' attr name here because
                 * that is how we have them in the RS (or Oracle won't delete).
                 */
                pobjBO.rs2obj(objRS.getRs(), pobjBO.getDescriptor().getPrimaryKey(), blnPrevResolveFK, true);

                /**
                 * Loop over the relations
                 */
                int intBORelations = pobjBO.getDescriptor().getBORelations().size();
                for (int i = 0; i < intBORelations; i++) {
                    objRelation = (BORelation) pobjBO.getDescriptor().getBORelations().get(i);

                    /**
                     * We have found a BO that may be associated with this BO so
                     * we better delete those as well
                     */
                    objAssociatedBO = getZx().createBO(objRelation.getEntity());
                    if (objAssociatedBO == null) {
                        throw new Exception("Unable to create instance of associated BO "
                                            + objRelation.getEntity());
                    }

                    /**
                     * Either FK attr is set or we have to determine it
                     * ourselves
                     */
                    if (StringUtil.len(objRelation.getFKAttr()) == 0) {
                        objAttr = objAssociatedBO.getFKAttr(pobjBO);
                        if (objAttr == null) {
                            throw new Exception("Unable to find FK from "
                                                + objAssociatedBO.getDescriptor().getName()
                                                + " to " + pobjBO.getDescriptor().getName());
                        }

                        strFKAttr = objAttr.getName();
                    } else {
                        strFKAttr = objRelation.getFKAttr();
                    }

                    objAssociatedBO.setValue(strFKAttr, pobjBO.getPKValue());

                    /**
                     * DGS11AUG2004: If the delete rule is noAssociates, don't
                     * try to delete the associates, just see if there are any.
                     * If so, we don't delete this row.
                     */
                    if (objRelation.getDeleteRule().equals(zXType.deleteRule.drNoAssociates)) {
                        double lngCount = objAssociatedBO.countByGroup(strFKAttr);
                        if (lngCount == -1) {
                            rcRc = zXType.rc.rcError;
                        } else if (lngCount == 0) {
                            rcRc = zXType.rc.rcWarning;
                        } else {
                            rcRc = zXType.rc.rcOK;
                        }

                    } else {
                        rcRc = objAssociatedBO.deleteBO(strFKAttr);
                    }

                    if (rcRc.equals(zXType.rc.rcOK)) {
                        /**
                         * We have deleted associated BOs; this could be bad or
                         * good news: DGS11AUG2004: If the relation rule is
                         * noAssociates the meaning of rcRC is different: rcOk
                         * means we counted some rows - this is indeed bad news
                         */
                        if (objRelation.getDeleteRule().equals(zXType.deleteRule.drNoAssociates)) {
                            /**
                             * It was no good news: the delete rule of this BO
                             * dictates that we cannot delete if any BOs are
                             * still associated with us; so we better behave and
                             * return to the caller We assume that the caller
                             * handles the transaction properly
                             */
                            // throw new ZXException("Delete failed, still
                            // instance of " +
                            // objAssociatedBO.getDescriptor().getLabel().getLabel()
                            // + " associated");
                            getZx().trace.userErrorAdd("Delete failed, still instance of "
                                                        + objAssociatedBO.getDescriptor().getLabel().getLabel()
                                                        + " associated");
                            boRelationDeleteBO = zXType.rc.rcError;
                            return boRelationDeleteBO;
                        }

                    } else if (rcRc.equals(zXType.rc.rcWarning)) {
                        /**
                         * No associated BO deleted, this is always good news
                         */
                    } else {
                        getZx().trace.addError("Unable to delete associated BO "
                                            + objAssociatedBO.getDescriptor().getLabel().getLabel());
                        boRelationDeleteBO = rcRc;
                        return boRelationDeleteBO;
                    }
                } // Loop over relations

                /**
                 * Is the BO delete-able? You may wonder why we only do this
                 * check so late in the process: it is to make sure that when no
                 * items are deleted (because there are no items to delete) this
                 * does not cause an error message
                 */
                if (pobjBO.getDescriptor().getDeleteRule().equals(zXType.deleteRule.drNotAllowed)) {
                    throw new Exception("Business object "
                                        + pobjBO.getDescriptor().getLabel().getLabel()
                                        + " cannot be deleted according to delete rule");
                }

                intNumDeleted = intNumDeleted + 1;

                /**
                 * Now we have to delete this one as well
                 */
                objRS.getRs().deleteRow(); // Not 100% sure how well this will work for
                                           // different jdbc drivers etc..
                
                if (!objRS.moveNext()) {
                	// Failed to move to the next prosition
                	// boRelationDeleteBO = zXType.rc.rcError;
                	break;
                }
                
            } // Loop over rows to delete

            if (intNumDeleted == 0) {
                boRelationDeleteBO = zXType.rc.rcWarning;

            } else {
                // VB Code : objRS.UpdateBatch - There does not seem to be an
                // equivalent.
            	
                // TODO : Look at Hibernate and see how they handle batch jobs inserts/deletes etc.
            }
            
            return boRelationDeleteBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : The version of deleteBO that uses the BO relations.",e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            boRelationDeleteBO = zXType.rc.rcError;
            return boRelationDeleteBO;
            
        } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(boRelationDeleteBO);
                getZx().trace.exitMethod();
            }
        }
    }
       
    //------------------------ DSHRdbms specific methods.
    
    /**
     * Execute query and return recordset.
     * 
     * @param pstrSQL Query to be executed
     * @return Returns a recordset.
     * @see #sqlRS(String, int, int)
     * @throws ZXException Thrown if rs fails or exceeds the query timeout.
     */
    public DSRS sqlRS(String pstrSQL) throws ZXException {
        return sqlRS(pstrSQL, 0, 0);
    }
    
    /**
     * Execute query and return recordset. The result returned is scrollable and updatable.
     * 
     * <pre>
     * 
     * We need to use prepared statements properly ie :
     * 
     * 1) generate statements like so :
     * select * from table where id = ?;
     * 
     * 2) then set the values 
     * smt.setDate(bo.getValue.dateValue());
     * 
     * This will increase performance as we can cache the statement, and make is easier for use to support 
     * new database as we can then rely on the driver to support java to sql type conversion.
     * 
     * </pre>
     * 
     * 
     * @param pstrSQL Query to be executed
     * @param plngStartRow The row to start on. Optional, default should be 0.
     * @param plngBatchSize The number of rows to return. Optional, default should be 0.
     * @return Returns a recordset.
     * @throws ZXException Thrown if rs fails or exceeds the query timeout.
     */
    public DSRS sqlRS(String pstrSQL,
                      int plngStartRow,
                      int plngBatchSize) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrSQL", pstrSQL);
        }
        
        DSRS rs = new DSRS(this);
        
        Statement stmt = null;
        
        try {
            /**
             * Handle any batch size stuff; note that at this moment in time (MAY05) we do not
             * support the startRow for RDBMS so we have to translate the startRow + batchSize
             * to the first x records
             */
            if (plngBatchSize > 0) {
                int lngFirstXRows = plngStartRow + plngBatchSize;
                pstrSQL = getZx().getSql().dbRowLimit(pstrSQL, lngFirstXRows, getDbType());
            }
            
            /**
             * 29JUN2004: SQL Server does not fully support nested firehose cursors within the
             * same transaction. Therefore we must use a dynamic recordset for that DBMS.
             * 30JUN2004: Only when in a transaction.
             * 02JUL2004: And still got problems sometimes, solved by using a client-side cursor
             */
            // Need to check this in Java.
            
            /**
             * This is a very paranoid resultset : Does not allow updating or scrolling. 
             * Only forwards.
             */
            stmt = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                                   ResultSet.CONCUR_READ_ONLY);
            // stmt.setFetchSize(Integer.MIN_VALUE);
            
            // Set the timeout for the query execute :
            // stmt.setQueryTimeout(this.timeout);
            
            /**
             * ZXResultSet will now be responsible to closing the statement.
             */
            ZXResultSet objRS = new ZXResultSet(stmt, stmt.executeQuery(pstrSQL), getDbType());
            
            rs.setRs(objRS);
            
            setLastErrorNumber(zXType.rc.rcOK);
            setLastErrorString("");
            
            /**
             * NOTE : Should move to the very first record.
             */
            rs.moveNext();
            
            /**
             * Save startRow and batchSize
             */
            rs.setStartRow(plngStartRow);
            rs.setBatchSize(plngBatchSize);
            
            /**
             * We may need to skip the first x rows.
             * This should only really be applicable to databases that do
             * not support offsets.
             */
            if (plngStartRow > 0) {
                rs.move(plngStartRow - 1);
            }
            
            return rs;
        } catch (Exception e) {
            // Clean up in case anything goes wrong.
        	JDBCUtil.closeStatement(stmt);
        	
            getZx().trace.addError("Execute query and return recordset.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrSQL = " + pstrSQL);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return rs;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(rs);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Execute a UPDATE or DELETE command and return number of affected rows.
     * 
     * @param pstrQry The sql query to execute.
     * @return Returns the number of results.
     * @throws ZXException Thrown if sqlExecute fails.
     */
    public int sqlExecute(String pstrQry) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrQry", pstrQry);
        }
        
        int execute = 0;

        PreparedStatement stmt = null;
        
        try {
            stmt = getConnection().prepareStatement(pstrQry, 
                                                    ResultSet.TYPE_FORWARD_ONLY,
                                                    ResultSet.CONCUR_READ_ONLY);
            
            execute = stmt.executeUpdate();
            if (execute == -1) {
                execute = 0;
            }
            
            /**
             * DGS 25NOV2003: -1 is interpreted as an error in calling programs so replace
             * it by 0 here, because we now know there are no errors in this instance.
             **/
            // if (zx.db.getDatabaseType().equals(zXType.databaseType.dbDB2) && execute == -1) {
            
            return execute;
        } catch (SQLException e) {
            getZx().trace.addError("Execute command and return number of affected rows", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrQry = " + pstrQry);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return execute;
        } finally {
            // Clean up in case anything goes wrong.
        	JDBCUtil.closeStatement(stmt);
        	
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(execute);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Create a prepared statement
     * 
     * @param pstrQry The sql query to execute.
     * @return Returns the perpared statement
     * @throws ZXException Thrown if sqlExecute fails.
     */
    public PreparedStatement prepareStatement(String pstrQry) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrQry", pstrQry);
        }
        
        PreparedStatement prepareStatement = null;
        try {
        	prepareStatement = getConnection().prepareStatement(pstrQry, 
                                                    ResultSet.TYPE_FORWARD_ONLY,
                                                    ResultSet.CONCUR_READ_ONLY);
        	return prepareStatement;
        	
       } catch (SQLException e) {
            getZx().trace.addError("Execute command and return number of affected rows", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrQry = " + pstrQry);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return prepareStatement;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(prepareStatement);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Implement abstract methods.
    
    /**
     * @see org.zxframework.datasources.DSHandler#parse(org.jdom.Element)
     */
    public zXType.rc parse(Element pobjElement) throws ParsingException {
        zXType.rc parse = zXType.rc.rcOK;
        
        /**
         * There are two scenarios: we are either dealing with an old- or a new style
         * application (pre- or post 1.5).
         * 
         * Old: initialise clsDB and copy values from there
         * New: read proper datasource element and copy values back to DB
         */
        
        /**
         * New style so get the data source definition
         */
        /**
         * Get database type
         */
        setDbType(zXType.databaseType.getEnum(pobjElement.getChildText("type")));
        if(getDbType() == null) {
            throw new ParsingException("Unknown / unsupported database type : " + pobjElement.getChildText("type"));
        }
        
        /**
         * Get timeout value
         */
        setTimeOut(Integer.parseInt(pobjElement.getChildText("timeout")));
        
        /**
         * Get DSN and try to connect to database
         */
        setDsn(pobjElement.getChildText("datasource")); // DSN in VB.
        
        /**
         * Set timeouts
         */
        //connection.CommandTimeout = timeOut
        //connection.ConnectionTimeout = timeOut
        
        /**
         * Get schema
         */
        String strSchema = pobjElement.getChildText("schema");
        if (strSchema != null) {
            setSchema(strSchema);
        }
        
        //-------------------------- Test mode settings.
        
        /**
         * Get the jdbc driver to load for test mode.
         */
        setJdbcdriver(pobjElement.getChildText("jdbcdriver"));
        /**
         * The jdbc url used to connect to the db
         */
        setJdbcurl(pobjElement.getChildText("jdbcurl"));
        /**
         * Username
         */               
        setUsername(pobjElement.getChildText("username"));
        setPassword(pobjElement.getChildText("password"));
        
        //-------------------------- Test mode settings.
        
        return parse;
    }
    
    /**
     * @see org.zxframework.datasources.DSHandler#loadBO(ZXBO, String, String, boolean, String)
     */
    public zXType.rc loadBO(ZXBO pobjBO, 
                            String pstrLoadGroup, 
                            String pstrWhereGroup, 
                            boolean pblnResolveFK, 
                            String pstrOrderByGroup) throws ZXException {

        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrLoadGroup", pstrLoadGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
        }

        zXType.rc loadBO = zXType.rc.rcOK;
        
        DSRS objRS = null;
        
        /**
         * Defaults :
         */
        if (pstrLoadGroup == null) {
            pstrLoadGroup = "*";
        }
        if (pstrWhereGroup == null) {
            pstrWhereGroup = "+";
        }
        
        try {
            /**
             * Dont bother with the database stuff if we do not have a table
             */
            if (!pobjBO.noTable()) {
                /**
                 * Generate query if required; note that we do remember
                 * name and alias to make sure that we do not cache query
                 * if one has an alias and subsequent does not or vice versa
                 * or different aliases
                 */
                String strQry;
                if (pobjBO.getDescriptor().getName().equals(strPrevBO)
                     && pobjBO.getDescriptor().getAlias().equals(strPrevAlias)
                     && pstrLoadGroup.equals(strPrevGroup)
                     && blnPrevResolveFK == pblnResolveFK) {
                    
                    strQry = strPrevQry;
                    
                } else {
                    strQry = getZx().sql.loadQuery(pobjBO, pstrLoadGroup, pblnResolveFK, false);
                    
                    blnPrevResolveFK = pblnResolveFK;
                    strPrevAlias = pobjBO.getDescriptor().getAlias();
                    strPrevBO = pobjBO.getDescriptor().getName();
                    strPrevGroup = pstrLoadGroup;
                    strPrevQry = strQry;
                }
                
                /**
                 * Add the where clause
                 */
                if(!StringUtil.isEmpty(pstrWhereGroup)) {
                    String strWhere = getZx().sql.whereCondition(pobjBO, pstrWhereGroup);
                    strQry = strQry + " AND " + strWhere;
                }
                
                /**
                 * Now generate recordset
                 * 
                 * NOTE : Hardcode to return db stuff .
                 */
                objRS = sqlRS(strQry);
                if(objRS == null) {
                    loadBO = zXType.rc.rcWarning;
                    return loadBO;
                }
                
                /**
                 * And copy fields (if a row was found)
                 */
                if(objRS.eof()) {
                    loadBO = zXType.rc.rcWarning;
                    
                } else {
                    try {
                        objRS.rs2obj(pobjBO, pstrLoadGroup, pblnResolveFK);
                    } catch (Exception e) {
                        throw new NestableException("Unable to rs2obj ", e);
                    }
                    
                }
            }
            
            return loadBO;
            
        } catch (Exception e) {
            getZx().trace.addError("Load a BO instance from the database", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrLoadGroup = " + pstrLoadGroup);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
                getZx().log.error("Parameter : pblnResolveFK = " + pblnResolveFK);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return loadBO;
        } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(loadBO);
                getZx().trace.exitMethod();
            }
        }
    
    }

    /**
     * @see org.zxframework.datasources.DSHandler#insertBO(org.zxframework.ZXBO)
     */
    public zXType.rc insertBO(ZXBO pobjBO) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        zXType.rc insertBO = zXType.rc.rcOK;
        
        DSRS objRS = null;
        
        try {

            /**
             * Dont bother for BOs without a table
             */
            if(!pobjBO.noTable()) {
                
                /**
                 * Check that this would not create any duplicates
                 */
                if (!StringUtil.isEmpty(pobjBO.getDescriptor().getUniqueConstraint())) {
                    if (pobjBO.doesExist(pobjBO.getDescriptor().getUniqueConstraint())) {
                        // throw new Exception("Inserting this record would cause duplicate " + pobjBO.getDescriptor().getLabel().getLabel());
                        getZx().trace.userErrorAdd("Inserting this record would cause duplicates");
                        insertBO = zXType.rc.rcError;
                        return insertBO;
                    }
                }
                
                /**
                 * Handle audit attributes
                 */
                if (pobjBO.getDescriptor().isAuditable()) {
                    pobjBO.setAuditAttr(zXType.persistAction.paInsert);
                }
                
                /**
                 * BD7DEC02 Use recordsets to do the insert rather than good old
                 * queries as the latter didn't seem to work well with long
                 * Oracle LONG columns ('literal too long for my liking' or some
                 * message like that)
                 */
                if (getDbType().equals(zXType.databaseType.dbSQLServer) 
             	    || getDbType().equals(zXType.databaseType.dbAccess)
                    // || getDbType().equals(zXType.databaseType.dbOracle)
                    ) {
                    /**
                     * Generate insert query
                     */
                    String strSQL = getZx().sql.insertQuery(pobjBO);
                    
                    /**
                     * And execute
                     */
                    if (sqlExecute(strSQL) != 1) {
                        String strMsg = getZx().trace.formatStack(false);
                        getZx().trace.resetStack();
                        throw new Exception("Unable to insert \"" 
                                            + pobjBO.getDescriptor().getLabel().getLabel() 
                                            +  "\" [" + strMsg + "]");
                    }
                    
                } else if (getDbType().equals(zXType.databaseType.dbOracle)
                		   || getDbType().equals(zXType.databaseType.dbAS400)
                		   || getDbType().equals(zXType.databaseType.dbDB2)
                		   || getDbType().equals(zXType.databaseType.dbMysql)
                		   || getDbType().equals(zXType.databaseType.dbHsql)
                		   ) {
                	/**
                	 * Use prepared statements
                	 */
                	
                    /**
                     * Generate insert query
                     */
                    Query query = getZx().sql.insertPreparedQuery(pobjBO);
                    
                    int row = 0;
                    PreparedStatement stmt = null;
                    
                    try {
                        stmt = prepareStatement(query.getSql().toString());
                        
                        /**
                         * Set the values in the prepared statement
                         */
                        List list = query.getValues();
                        for (int i = 0; i < list.size(); i++) {
                        	insertBO = setPreparedStatementValue(stmt, i+1, pobjBO, (Property)list.get(i));
                        	if (insertBO.pos == zXType.rc.rcError.pos) {
                        		return insertBO;
                        	}
                        }
                        
                        row = stmt.executeUpdate();
                    	
                    } finally {
                    	// Ensure we close PreparedStament
                    	JDBCUtil.closeStatement(stmt);
                    }
                    
                    if (row != 1) {
                        String strMsg = getZx().trace.formatStack(false);
                        getZx().trace.resetStack();
                        throw new Exception("Unable to insert \"" 
                                            + pobjBO.getDescriptor().getLabel().getLabel() 
                                            +  "\" [" + strMsg + "]");
                    }
                    
                } else {
                    /**
                     * Look and see how hibernate does inserts with databases like this.
                     */
                    
                    /**
                     * Create the resultset we will be using to populate.
                     */
                    objRS = tableRS(pobjBO);
                    if (objRS == null) {
                        throw new Exception("Unable to create table resultset for " 
                                + pobjBO.getDescriptor().getLabel().getLabel()
                                + " (" + pobjBO.getDescriptor().getName() +")");
                    }
                    
                    /** 
                     * Move to the insert postion in the resultset.
                     */
                    objRS.getRs().moveToInsertRow();
                    
                    /** 
                     * Copy the values from the business object to the resultset object.
                     */
                    if (objRS.bo2rs(pobjBO).pos != zXType.rc.rcOK.pos) {
                        throw new Exception("Unable to copy fields to table rs for " 
                                            + pobjBO.getDescriptor().getName());
                    }
                    
                    /**
                     * Now try to final insert it into the database.
                     */
                    try {
                        objRS.getRs().insertRow();
                    } catch (Exception e) {
                        throw new ZXException("Unable to insert \"" + 
                                              pobjBO.getDescriptor().getLabel().getLabel() 
                                              +  "\" [" + e.getMessage()+ "]", e);
                    }
                }
            }
            
            return insertBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Insert a business object in the database", e);
            
            if (getZx().throwException) { throw new ZXException(e); }
            insertBO = zXType.rc.rcError;
            return insertBO;
        } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(insertBO);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Reviewed for 1.5:44
     * Reviewed for 1.5:46
     * 
     * @see org.zxframework.datasources.DSHandler#updateBO(ZXBO, String, String)
     */
    public zXType.rc updateBO(ZXBO pobjBO, 
                              String pstrUpdateGroup, 
                              String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrUpdateGroup", pstrUpdateGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }

        zXType.rc updateBO = zXType.rc.rcOK;
        
        String strAlias = "";
        int lngNumUpdates = 0;
        DSRS objRS = null;
        
        try {
            /**
             * BD10AUG04 - Cannot afford to generate where clause with alias; this would
             * result in rubbsh like select * from test where alias.id = 12
             * we cannot use the alias in the select from clause either as Oracle
             * does not like batch updates on aliases;
             * so the cure is to save alias, blank it out and restore
             */
            strAlias = pobjBO.getDescriptor().getAlias();
            pobjBO.getDescriptor().setAlias("");
            
            /**
             * Dont bother for BOs without a table
             */
            if(!pobjBO.noTable()) {
                /**
                 * For an audit BO and with the PK as wheregroup:
                 * add the audit clause to the where clause
                 * Now we have to generate this first because we want the latest and
                 * updated version in the value list and than it is too late....
                 * Now also when the zXUpdtdId is currently null we do NOT
                 * support the feature of checking whether the object was updated
                 * before by someone else as it is likely that we do not come
                 * from a web environment
                 * 
                 * BD9JUN04 - Now make a different between auditable and
                 * concurrency control
                 */
                String strAuditWhere = null;
                if (pobjBO.getDescriptor().isConcurrencyControl() && pstrWhereGroup.equals("+")) {
                    if (!pobjBO.getValue("zXUpdtdId").isNull) {
                        //strAuditWhere = zx.sql.whereCondition(this, "zXUpdtdId");
                        strAuditWhere = getZx().sql.concurrencyControlWhereCondition(pobjBO);
                    }
                }
                
                /**
                 * Check that this would not create any duplicates
                 * DGS28APR2003: Don't perform this check if the unique constraint is the PK - we
                 * have to assume that this is not editable. If it is, and the user has made it a
                 * duplicate of another row, a DB error will ensue later. It is impossible for us
                 * to make the check here because the user may or may not have changed the PK value
                 * to another one. If not there will be one 'duplicate' (itself), so not a useful test.
                 * 
                 * V1.4:75 - BD6MAY05 - If the uniqueConstraint group is not fully included in the
                 *           updateGroup we should not bother checking; see comment in header
                 *           for additional important information!
                 *           
                 * Rules :
                 *  1) The unique constrainst most not be the primary key.
                 *  2) UniqueConstriant must be the intersect of UpdateGroup.
                 */
                String strUniqueConstriant = pobjBO.getDescriptor().getUniqueConstraint();
                if (StringUtil.len(strUniqueConstriant) > 0) {
                	
                    if (!strUniqueConstriant.equals("+")
                        && !strUniqueConstriant.equalsIgnoreCase(pobjBO.getDescriptor().getPrimaryKey())) {
                    	
                        /**
                         * Only check if the attributes we update actually contain at least one
                         * of the attributes in the uniqueConstraint (see comment for 1.5:44)
                         */
                        if (pobjBO.getDescriptor().groupIntersect(strUniqueConstriant, pstrUpdateGroup).size() > 0) {
                        	
	                        if (pobjBO.doesExist(strUniqueConstriant)) {
	                            throw new Exception("Inserting this record would cause duplicates");
	                        } // Does exists
	                        
                        } // updateGroup contains attrs from uniqueConstraint
                        
                    } // Unique constraint <> primary key
                    
                } // Has unique constraint
                
                /**
                 * Handle audit attributes
                 */
                if (pobjBO.getDescriptor().isAuditable()) {
                    pobjBO.setAuditAttr(zXType.persistAction.paUpdate);
                }
                
                /**
                 * BD7DEC02 Use recordsets to do the insert rather than good old 
                 * queries as the latter didn't seem to work well with long
                 * Oracle LONG columns ('literal too long for my liking' or some
                 * message like that)
                 */
                if (getDbType().equals(zXType.databaseType.dbSQLServer) 
                    || getDbType().equals(zXType.databaseType.dbAS400)
                    || getDbType().equals(zXType.databaseType.dbDB2)
         		    || getDbType().equals(zXType.databaseType.dbAccess)) {
                    
                    /**
                     * Generate query
                     */
                    StringBuffer strQry = new StringBuffer(getZx().sql.updateQuery(pobjBO, pstrUpdateGroup));
                    
                    /**
                     * Add where clause
                     */
                    strQry.append(" AND ").append(getZx().sql.whereCondition(pobjBO, pstrWhereGroup));
                    
                    /**
                     * Add optional audit where clause
                     */
                    if (StringUtil.len(strAuditWhere) > 0) {
                        strQry.append(" AND ").append(strAuditWhere);
                    }
                    
                    /**
                     * Execute the update statement
                     */
                    int rows = sqlExecute(strQry.toString());
                    if (rows < 0) {
                        throw new Exception("Unable to execute update query");
                    } else if (rows == 0){
                        /**
                         * Did not update any records : 
                         */
                        updateBO = zXType.rc.rcWarning;
                    }
                    
                } else if (getDbType().equals(zXType.databaseType.dbOracle)
		         		   || getDbType().equals(zXType.databaseType.dbAS400)
		         		   || getDbType().equals(zXType.databaseType.dbDB2)
		         		   || getDbType().equals(zXType.databaseType.dbMysql) 
                		   || getDbType().equals(zXType.databaseType.dbHsql)
                		   ) {
                    /**
                     * Generate query
                     */
                    Query qury = getZx().sql.updatePreparedQuery(pobjBO, pstrUpdateGroup);
                    
                    /**
                     * Add where clause
                     */
                    qury.getSql().append(" AND ").append(getZx().sql.whereCondition(pobjBO, pstrWhereGroup));
                    
                    /**
                     * Add optional audit where clause
                     */
                    if (StringUtil.len(strAuditWhere) > 0) {
                    	qury.getSql().append(" AND ").append(strAuditWhere);
                    }
                	
                    int rows = 0;
                    PreparedStatement stmt = null;
                    
                    try {
                        stmt = prepareStatement(qury.getSql().toString());
                        List list = qury.getValues();
                        
                        for (int i = 0; i < list.size(); i++) {
    						updateBO = setPreparedStatementValue(stmt, i+1, pobjBO, (Property)list.get(i));
    						
    						if (updateBO.pos == zXType.rc.rcError.pos) {
    							return updateBO;
    						}
    					}
                        
                        rows = stmt.executeUpdate();
                        
                    } finally {
                    	JDBCUtil.closeStatement(stmt);
                    }
                    
                    if (rows < 0) {
                        throw new Exception("Unable to execute update query");
                        
                    } else if (rows == 0){
                        /**
                         * Did not update any records : 
                         */
                        updateBO = zXType.rc.rcWarning;
                        
                    }
                    
                } else {
                    /**
                     * Use a updatable resultset to update the business object :
                     */
                    StringBuffer strQry = new StringBuffer("SELECT ").append(pobjBO.getDescriptor().getTable()).append(".* FROM ")
                                          .append(pobjBO.getDescriptor().getTable()).append(" WHERE (1=1) ");
                    
                    /**
                     * Add where clause
                     */
                    if (StringUtil.len(pstrWhereGroup) > 0) {
                        strQry.append(" AND ").append(getZx().sql.whereCondition(pobjBO, pstrWhereGroup));
                    }
                    
                    /**
                     * Add optional audit where clause
                     */
                    if (StringUtil.len(strAuditWhere) > 0) {
                        strQry.append(" AND ").append(strAuditWhere);
                    }
                    
                    /**
                     * Get a updatable resultset
                     */
                    objRS = tableRS(strQry.toString());
                    
                    while (!objRS.eof()) {
                        lngNumUpdates = lngNumUpdates + 1;
                        // Updated the result with the values from the bo
                        objRS.bo2rs(pobjBO, pstrUpdateGroup);
                        
                        // Actually performs the update to the database.
                        objRS.getRs().updateRow();
                        
                        if (!objRS.moveNext()) {
                        	updateBO = zXType.rc.rcError;
                        	break;
                        }
                    }
                    
                    if (lngNumUpdates == 0) {
                        updateBO = zXType.rc.rcWarning;
                    } else {
                    	// May do a batch update.
                    }
                    
                }
            }
            
            return updateBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Update a business object ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrUpdateGroup = " + pstrUpdateGroup);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            
            /**
             * Cancel any outstanding work
             */
            if (lngNumUpdates > 0) {
                if (objRS != null) {
                    try {
                        objRS.getRs().cancelRowUpdates();
                    } catch (Exception e1) {
                    	throw new RuntimeException(e);
                    }
                }
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return updateBO;
        } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
            /**
             * Always restore alias.
             */
            if (StringUtil.len(strAlias) > 0) {
                pobjBO.getDescriptor().setAlias(strAlias);
            }
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(updateBO);
                getZx().trace.exitMethod();
            }
        } 
    
    }

    /**
     * @see org.zxframework.datasources.DSHandler#deleteBO(org.zxframework.ZXBO, java.lang.String)
     */
    public zXType.rc deleteBO(ZXBO pobjBO, 
                              String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }
        
        zXType.rc deleteBO = zXType.rc.rcOK;

        try {
            /**
             * Dont bother for BOs with no table
             */
            if (pobjBO.noTable()) return deleteBO;
            
            zXType.deleteRule deleteRule = pobjBO.getDescriptor().getDeleteRule();
            if (deleteRule.equals(zXType.deleteRule.drAllowed)) {
                /**
                 * The simple version
                 */
                deleteBO = simpleDeleteBO(pobjBO, pstrWhereGroup);
                
            } else if (deleteRule.equals(zXType.deleteRule.drNoAssociates)) {
                /**
                 * The version that uses the BO model table
                 */
                deleteBO = boModelDeleteBO(pobjBO, pstrWhereGroup);
                
            } else if (deleteRule.equals(zXType.deleteRule.drPerRelation)) {
                /**
                 * Need to take relations into consideration
                 * 
                 * Create a select query so we can treat each BO that we want to
                 * delete individually
                 * 
                 * Use purenames as Oracle doesnt like it when we delete on
                 * a recordset when we use aliases
                 */
                deleteBO = boRelationDeleteBO(pobjBO, pstrWhereGroup);
                
            } else {
                /**
                 * Simply not allowed
                 */
                getZx().trace.userErrorAdd("Delete rule is set to not-allowed for this entity (" 
						   				   + pobjBO.getDescriptor().getLabel().getLabel() 
						   				   + ")");
                deleteBO = zXType.rc.rcError;
            }
            
            return deleteBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Delete BO", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
            deleteBO = zXType.rc.rcError;
            return deleteBO;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(deleteBO);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * @see org.zxframework.datasources.DSHandler#boRS(org.zxframework.ZXBO, java.lang.String, java.lang.String, boolean, java.lang.String, boolean, int, int)
     */
    public DSRS boRS(ZXBO pobjBO, 
                     String pstrLoadGroup, 
                     String pstrWhereGroup, 
                     boolean pblnResolveFK, 
                     String pstrOrderByGroup, 
                     boolean pblnReverse,
                     int plngStartRow,
                     int plngBatchSize) throws ZXException {
        DSRS boRS = null;
        try {
            /**
             * Generate query
             */
            String strQry = getZx().getSql().loadQuery(pobjBO, pstrLoadGroup, pblnResolveFK, false);
            
            /**
             * Add optional where clause
             */
            if (StringUtil.len(pstrWhereGroup) > 0) {
                strQry = strQry + " AND " + getZx().getSql().whereCondition(pobjBO, pstrWhereGroup);
            }
            
            /**
             * Add the optional order by clause
             */
            if(StringUtil.len(pstrOrderByGroup) > 0) {
                strQry = strQry + getZx().getSql().orderByClause(pobjBO, pstrOrderByGroup, pblnReverse);
            }
            
            /**
             * Turn into recordset
             */
            boRS = sqlRS(strQry, 
            		     plngStartRow, plngBatchSize);
            
        } catch (Exception e) {
            /**
             * Close resultset.
             */
            if (boRS != null) {
            	boRS.RSClose();
            }
            
            if (e instanceof ZXException) {
                throw (ZXException)e;
            }
            throw new ZXException("Fails to execute.", e);
            
        }
        
        return boRS;
    }
    
    /**
     * Creates a new transaction and returns a handle to the current transaction.
     * 
     * @return Returns a handle to the transaction
     * @throws ZXException Thrown if beginTransaction fails.
     */
    public Transaction beginTransaction() throws ZXException {
        JDBCTransaction beginTransaction = new JDBCTransaction(this.connection);
        beginTransaction.begin();
        
        return beginTransaction;
    }
    
    /**
     * @see org.zxframework.datasources.DSHandler#beginTx()
     * begin transaction in current connection
     * 
     * This transaction state is managed by this handler.
     * 
     * @return Returns a handle to the transaction
     * @throws ZXException Thrown if beginTx
     */
    public zXType.rc beginTx() throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc beginTx = zXType.rc.rcOK;
        
        try {
            
            if (getTxHandler() == null) {
                /**
                 * No predefined transaction handler.
                 */
                if (!inTx()) {
                	/**
                	 * Only begin a transaction if we already have a
                	 * connection.
                	 */
                	if (this.connection != null) {
	                    this.currentTransaction = new JDBCTransaction(this.connection);
	                    this.currentTransaction.begin();
	                }
                	
                    this.inTransaction = true;
                }
                
            } else {
                /**
                 * If we already have a transaction on the go: simply copy that txId so later we
                 * can tell that a commit or rollback was done by another datasource handler (using the
                 * same tx handler)
                 */
                if (!getTxHandler().inTx()) {
                    beginTx = getTxHandler().beginTx();
                    
                    long lngTxId = System.currentTimeMillis();
                    getTxHandler().setTxId(lngTxId);
                    
                    this.inTransaction = true;
                    
                } else {
                    setTxId(getTxHandler().getTxId());
                }
                
            }
            
            return beginTx;
            
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : begin transaction in current connection", e);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return beginTx;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * @see org.zxframework.datasources.DSHandler#inTx()
     */
    public boolean inTx() throws ZXException {
        /** Check whether DSHandler managed transaction is still busy. **/
        return this.inTransaction; // && this.currentTransaction != null;
    }
    
    /**
     * Rollback transaction in current connection.
     * 
     * <pre>
     * 
     * You need a version 2 jdbc driver to support this.
     * </pre>
     * 
     * @see org.zxframework.datasources.DSHandler#rollbackTx()
     */
    public zXType.rc rollbackTx() throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc rollbackTx = zXType.rc.rcOK;
        
        try {
            
            
            if (getTxHandler() == null) {
                if (inTx()) {
                	/**
                	 * Rollback transaction only if we have
                	 * got a handle to the current transaction.
                	 */
                    if (this.currentTransaction != null) {
                    	this.currentTransaction.rollback();
                    }
                    
                    this.inTransaction = false;
                    
                } else {
                    throw new ZXException("Tried to rollbackTx transaction but no transaction active");
                }
                
            } else {
                /**
                 * Has handler
                 */
                if (getTxHandler().inTx()) {
                    rollbackTx = getTxHandler().rollbackTx();
                    this.inTransaction = false;
                    
                } else {
                    /**
                     * It is possible that the transaction was already rollbackTxted through another
                     * data-source handler that uses the same tx handler
                     */
                    if (getTxHandler().getTxId() != getTxId()) {
                        throw new ZXException("Tried to rollback transaction but no transaction active");
                    }
                    
                } // In tx
                
            } // Has tx handler?
            
            return rollbackTx;
            
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : rollback transaction in current connection", e);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            rollbackTx = zXType.rc.rcError;
            return rollbackTx;
        } finally {
            
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
            
        }
    }

    /**
     * Commit transaction in current connection.
     * 
     * <pre>
     * 
     * You need a jdbc version 2 driver to support this.
     * </pre>
     * 
     * @see org.zxframework.datasources.DSHandler#commitTx()
     */
    public zXType.rc commitTx() throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc commitTx = zXType.rc.rcOK;
        
        try {
            
            if (getTxHandler() == null) {
                
                /**
                 * NEW CODE.
                 */
                if (inTx()) {
                	if (this.currentTransaction != null) {
                		this.currentTransaction.commit();
                	}
                	
                    this.inTransaction = false;
                    
                } else {
                    throw new ZXException("Tried to commit transaction but no transaction active");
                }
                
            } else {
                /**
                 * Has handler
                 */
                if (!getTxHandler().inTx()) {
                    commitTx = getTxHandler().commitTx();
                    this.inTransaction = false;
                    
                } else {
                    /**
                     * It is possible that the transaction was already committed through another
                     * data-source handler that uses the same tx handler
                     */
                    if (getTxHandler().getTxId() != getTxId()) {
                        throw new ZXException("Tried to commit transaction but no transaction active");
                    }
                    
                }
            }
            
            return commitTx;
            
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : commit transaction in current connection", e);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            commitTx = zXType.rc.rcError;
            return commitTx;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * @see org.zxframework.datasources.DSHandler#connect()
     */
    public zXType.rc connect() throws ZXException {
        zXType.rc connect = zXType.rc.rcOK;
        try {
        	
            if (this.connection == null) {
                this.connection = openDBConnection();
            } else if (this.connection.isClosed()) {
                getZx().log.error("Connection is closed");
                this.connection = openDBConnection();
            }
            
            /**
             * Start a new transaction if needed.
             */
            if (this.inTransaction && this.currentTransaction == null) {
            	this.currentTransaction = new JDBCTransaction(this.connection);
            	this.currentTransaction.begin();
            }
            
            setState(zXType.dsState.dssActive);
            
        } catch (Exception e) {
            setState(zXType.dsState.dssError);
            getZx().trace.addError("Failed to connect to Database.", e);
            throw new ZXException(e);
        }
        return connect;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#disConnect()
     */
    public zXType.rc disConnect() throws ZXException {
        
        if (this.connection != null) {
            
            try {
                if (!this.connection.isClosed()) {
                    /** 
                     * Try to make this kind of thread-safe.
                     * Although this will slow things down a little. 
                     **/
                    this.connection.close();
                    this.connection = null;
                    
                    // Track the number of open connections for debugging :
                    if (getZx().log.isInfoEnabled()) {
                        DEBUG_open_connections = DEBUG_open_connections - 1;
                        getZx().log.info("Closed a connection : " + DEBUG_open_connections);
                    }
                    
                } else {
                    getZx().log.error("close - The connection is aleady closed.", new Exception());
                }
                
            } catch (Exception e) { 
                getZx().log.error("Failed to close connection.", e);
            }
            
        } else if (getState().pos == zXType.dsState.dssActive.pos){
        	/**
        	 * The connection may be null if the state is not active.
        	 */
            // Throw an exceptions to get the call trace.
            getZx().log.error("Connectoin is null", new Exception());
            
        }
        
        /**
         * Close secondary connection as well.
         */
        if (this.secondaryConnection != null) {
            try {
                
                if (!this.secondaryConnection.isClosed()) {
                    /** 
                     * Try to make this kind of thread-safe ?
                     * Although this will slow things down a little. 
                     **/
                    this.secondaryConnection.close();
                    this.secondaryConnection = null;
                    
                    // Track the number of open connections for debugging :
                    if (getZx().log.isInfoEnabled()) {
                        DEBUG_open_connections = DEBUG_open_connections - 1;
                        getZx().log.info("Closed a secondary connection : " + DEBUG_open_connections);
                    }
                }
                
            } catch (Exception e) { 
                getZx().log.error("Failed to close connection.", e);
            }
        }
        
        /**
         * Set the state to closed :
         */
        setState(zXType.dsState.dssClosed);
        
        return null;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#RSMoveNext(DSRS)
     */
    public zXType.rc RSMoveNext(DSRS pobjRS) throws ZXException {
        zXType.rc RSMoveNext = zXType.rc.rcOK;
        
        pobjRS.moveNext();
        
        return RSMoveNext;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#RSClose(DSRS)
     */
    public zXType.rc RSClose(DSRS pobjRS) throws ZXException {
        zXType.rc RSClose = zXType.rc.rcOK;
        
        pobjRS.RSClose();
        
        return RSClose;
    }  
    
    //--------------------------- Util Methods
    
    /**
     * Return DB error string
     * 
     * @return Returns the string form of the db error
     * @throws ZXException Thrown if DBErrorString fails
     */
    public String DBErrorString() throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        String DBErrorString = null;

        try {
            
            DBErrorString = this.connection.getWarnings().getMessage();
            if (DBErrorString == null) {
                DBErrorString = "";
            }
            
            return DBErrorString;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Get error message", e);
            }
            if (getZx().throwException) throw new ZXException(e);
            return DBErrorString;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(DBErrorString);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Check whether the connection to the db is closed
     * 
     * @return True if it is closed.
     * @throws Exception Thrown if isClosed fails.
     */
    public boolean isClosed() {
        if (this.connection == null) {
            return true;
        }
   
        try {
            return this.connection.isClosed();
        } catch (Exception e) {
        	return true;
        }
    }
    
    /**
     * Sets a value in a prepared statement.
     *  
     * @param stmt The prepared statement
     * @param pos The position in the statment
     * @param pobjProperty The value to set.
     * @throws Exception
     */
    private zXType.rc setPreparedStatementValue(PreparedStatement stmt, int pos, ZXBO pobjBO, Property pobjProperty) throws Exception {
    	zXType.rc setPreparedStatementValue = zXType.rc.rcOK;
        if (pobjProperty == null || pobjProperty.isNull) {
        	if (pobjProperty == null) {
        		/**
        		 * Stricter
        		 */
        		getZx().trace.addError("Application error: Property is null", pobjBO.getDescriptor().getName());
        		setPreparedStatementValue = zXType.rc.rcError;
        		return setPreparedStatementValue;
        		
        	} else if (pobjProperty.getAttribute() == null 
        	    || (pobjProperty.getAttribute().isOptional() || !pobjBO.isValidate()) ) {
                /**
                 * Null property.
                 */
            	stmt.setString(pos, null);
        	} else {
        		/**
        		 * Stricter
        		 */
        		getZx().trace.addError("Application error: attempt to assign null to a non-null field",  
        							   pobjBO.getDescriptor().getName() + " / " + pobjProperty.getAttribute().getName());
        		setPreparedStatementValue = zXType.rc.rcError;
        		return setPreparedStatementValue;
        	}
        	
        } else {
            if (pobjProperty instanceof LongProperty) {
                // This will handle zxtype.dataType.dtAutomatic and zxtype.dataType.dtLong
            	stmt.setLong(pos, ((LongProperty)pobjProperty).getValue());
                
            } else if (pobjProperty instanceof DoubleProperty) {
            	stmt.setDouble(pos, ((DoubleProperty)pobjProperty).getValue());
            	
            } else if (pobjProperty instanceof DateProperty) {
                DateProperty objProperty = (DateProperty)pobjProperty;
                long lngDate = objProperty.getValue().getTime();
                
                int intDataType = objProperty.getDataType().pos;
                if (intDataType == zXType.dataType.dtDate.pos) {
                	stmt.setDate(pos, new java.sql.Date(lngDate));
                    
                } else if (intDataType == zXType.dataType.dtTimestamp.pos) {
                    java.sql.Timestamp timeStamp = new java.sql.Timestamp(lngDate);
                    stmt.setTimestamp(pos, timeStamp);
                    
                } else if (intDataType == zXType.dataType.dtTime.pos) {
                	stmt.setTime(pos, new java.sql.Time(lngDate));
                	
                }
                
            } else if (pobjProperty instanceof BooleanProperty) {
                boolean blnValue = ((BooleanProperty)pobjProperty).getValue();
                if (getDbType().equals(zXType.databaseType.dbAccess)) {
                	stmt.setBoolean(pos, blnValue);
                    
                } else {
                    String dbStrValue;
                    if (getDbType().equals(zXType.databaseType.dbOracle) 
                        || getDbType().equals(zXType.databaseType.dbDB2)
                        || getDbType().equals(zXType.databaseType.dbAS400)) {
                        dbStrValue = blnValue?"Y":"N";
                        
                    } else {
                        dbStrValue = blnValue?"1":"0";
                    }
                    
                    stmt.setString(pos, dbStrValue);
                }
                
            } else {
            	/**
            	 * All string dataTypes.
            	 */
            	stmt.setString(pos, pobjProperty.getStringValue());
            	
            }
        }
        
        return setPreparedStatementValue;
    }
}