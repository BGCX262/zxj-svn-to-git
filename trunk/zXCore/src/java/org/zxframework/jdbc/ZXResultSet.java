/*
 * Created on Jun 20, 2004
 */
package org.zxframework.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.zxframework.Attribute;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.property.BooleanProperty;
import org.zxframework.property.DateProperty;
import org.zxframework.property.DoubleProperty;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.JDBCUtil;

/**
 * Emulates the JDBC Result Object but without the SQLException and being jdbc specific.
 * 
 * <pre>
 * 
 * NOTE : We might have a different implementation for Hibernate.
 * NOTE : This class handles the SQLExceptions thrown.
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 * @since 0.01
 */
public class ZXResultSet extends ZXObject {
    
    /** <code>result</code> - The actaul resultset we are acting as a wrapper to. */
    private ResultSet result;
    private Statement stmt;

    private ColumnNameCache columnNameCache;
    private zXType.databaseType dbType;
    
    /**
     * Disable default constructor.
     */
    private ZXResultSet() { super(); }
    
    /**
     * Contructor for the ZXResultset object.
     * 
     * @param pobjStmt 
     * @param pobjResultSet A handle to the result set.
     * @param penmDBType The database type.
     */
    public ZXResultSet(Statement pobjStmt, ResultSet pobjResultSet, zXType.databaseType penmDBType) {
        this.dbType = penmDBType;
        this.result = wrapResultSet(pobjResultSet);
        this.stmt = pobjStmt;
    }
    
    //---------------------- Getters and Setters
    
    /**
     * @return Returns the result.
     */
    public ResultSet getTarget() {
        return result;
    }
    
    //------------------------------ Framework related helper methods
    
    /**
     * Get a column Property value.
     * 
     * <pre>
     * 
     * NOTE : Used when you have not yet calculated the column name
     * </pre>
     * 
     * @param pobjAttr The attribute associated with the column.
     * @param pobjBO The business object of the attribute.
     * @param penmColumnType The type of column name.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if getPropertyFromDB fails.
     */
    public Property getPropertyFromDB(ZXBO pobjBO, 
                                      Attribute pobjAttr,
                                      zXType.sqlObjectName penmColumnType) throws ZXException {
        Property getPropertyFromDB = null;
        
        try {
            if (pobjAttr != null) {
                /**
                 * Do not calculate the column name again :
                 */
                String strcolumnName = getZx().sql.columnName(pobjBO, pobjAttr, penmColumnType);

                // Try to get the value from the database
                Object obj = this.result.getObject(strcolumnName);
                
                boolean wasNull = this.result.wasNull();
                if (obj == null && wasNull) {
                    getPropertyFromDB = new StringProperty("", true);
                } else {
                    getPropertyFromDB = getPropertyFromDB(pobjAttr.getDataType().pos, obj, wasNull);
                }
            }
            
            return getPropertyFromDB;
            
        } catch (Exception e) {
            // We have an error what should we do? here.
            getZx().trace.addError("Failed to Property value from the database.", e);
            throw new ZXException(e);
        }
    }
    
    /**
     * Sets a property value from the database.
     * 
     * <pre>
     * 
     * Assumes : That the data in the database is valid.
     * </pre>
     * 
     * @param pobjBO The business object associated with this property
     * @param pobjAttr The attribute linked to the column.
     * @param pintColNo The column number
     * @return Returns the ZX property.
     * @throws ZXException Thrown if setPropertyFromDB fails.
     */
    public zXType.rc setPropertyFromDB(ZXBO pobjBO, 
                                       Attribute pobjAttr, 
                                       int pintColNo) throws ZXException {
        zXType.rc getProperty = zXType.rc.rcOK;
        
        try {
            
            // Try to get the value from the database
            Object obj = this.result.getObject(pintColNo);
            boolean wasNull = this.result.wasNull();
            if (obj == null && wasNull) {
                pobjBO.setValue(pobjAttr.getName(), "", false, true);
                
            } else {
                Property objProperty = getPropertyFromDB(pobjAttr.getDataType().pos, obj, wasNull);
                // Set the value
                pobjBO.setValue(pobjAttr.getName(), objProperty);
                
            }
            
	        return getProperty;
        } catch (Exception e) {
            // We have an error what should we do? here.
            getZx().trace.addError("Failed to Property value from the database.", e);
            throw new ZXException(e);
            
            // Return a property?
            // return getProperty;
        }
    }
    
    /**
     * Sets a property value from the database.
     * 
     * <pre>
     * 
     * Assumes : That the data in the database is already validated
     * 
     * </pre>
     * 
     * @param pobjAttr The attribute associated with the column.
     * @param pobjBO The business object of the attribute.
     * @param pstrColumnName The name of the column for the attribute. Optional, should be supplied if penmObjName is not
     * @return Returns the value from the coulmn name as a ZX Property
     * @throws ZXException Thrown if setPropertyFromDB fails.
     */
    public zXType.rc setPropertyFromDB(ZXBO pobjBO, 
                                       Attribute pobjAttr,
                                       String pstrColumnName) throws ZXException {
        zXType.rc getProperty = zXType.rc.rcOK;
        
        try {
            /**
             * Temp hack for Access
             */
            // Try to get the value from the database
//            Object obj = ((ResultSetWrapper)this.result).getObject(pstrColumnName);
//            boolean wasNull = this.result.wasNull();
//            if (obj == null && wasNull) {
//                pobjBO.setValue(pobjAttr.getName(), "", false, true);
//                
//            } else {
//                Property objProperty = getPropertyFromDB(pobjAttr.getDataType().pos, obj, wasNull);
//                // Set the value
//                pobjBO.setValue(pobjAttr.getName(), objProperty);
//                
//            }
//            return getProperty;
            
            // Try to get the value from the database
            // Get column name by position
            int colNo = this.result.findColumn(pstrColumnName);
            getProperty = setPropertyFromDB(pobjBO, pobjAttr, colNo);
            return getProperty;
            
        } catch (Exception e) {
            // We have an error what should we do? here.
            getZx().trace.addError("Failed to : Get property value from the database.", e);
            throw new ZXException(e);
            // Return a property?
            // return getProperty;
        }
    }
    
    //----------
    // Internal method to get property values fromt the datebase.
    //----------
    private Property getPropertyFromDB(int dataType, Object obj, boolean wasNull) {
        Property objProperty = null;
        
        //---- WARNING DATE HANDLING
        if (dataType == zXType.dataType.dtDate.pos 
            || dataType == zXType.dataType.dtTimestamp.pos
            || dataType == zXType.dataType.dtTime.pos) {
            if (obj instanceof java.sql.Timestamp) {
                java.util.Date dtResult = (java.sql.Timestamp)obj;
                objProperty = new DateProperty(dtResult, wasNull);
            } else if (obj instanceof java.sql.Time) {
                java.util.Date dtResult = (java.sql.Time)obj;
                objProperty = new DateProperty(dtResult, wasNull);
            } else if (obj instanceof java.sql.Date) {
                java.util.Date dtResult = (java.sql.Date)obj;
                objProperty = new DateProperty(dtResult, wasNull);
            }
        //---- WARNING DATE HANDLING    
            
        } else if (dataType == zXType.dataType.dtLong.pos ||dataType == zXType.dataType.dtAutomatic.pos) {
            if (obj instanceof Long) {
                objProperty = new LongProperty((Long)obj, wasNull);
            } else if (obj instanceof Integer){
                objProperty = new LongProperty(((Integer)obj).intValue(), wasNull);
            } else if (obj instanceof BigDecimal){
                objProperty = new LongProperty(((BigDecimal)obj).longValue(), wasNull);
            } else if (obj instanceof BigInteger){
                objProperty = new LongProperty(((BigInteger)obj).longValue(), wasNull);
            } else {
            	objProperty = new LongProperty(Integer.parseInt(obj.toString()), wasNull);
            }
            
        } else if (dataType == zXType.dataType.dtDouble.pos) {
        	if (obj instanceof Double) {
                objProperty = new DoubleProperty((Double)obj, wasNull);
        	} else if (obj instanceof BigDecimal) {
                objProperty = new DoubleProperty(((BigDecimal)obj).doubleValue(), wasNull);
        	} else if (obj instanceof BigInteger) {
                objProperty = new DoubleProperty(((BigInteger)obj).doubleValue(), wasNull);
        	}
        	
        // WARNING BOOLEAN HANDLING
        } else if (dataType == zXType.dataType.dtBoolean.pos) {
            if (obj instanceof Boolean) {
                objProperty = new BooleanProperty(((Boolean)obj).booleanValue(), wasNull);
            }
        // WARNING BOOLEAN HANDLING
            
        }
        
        /**
         * String/Automatics/Expression - Are all
         * handled as string.
         */
        if (objProperty == null) {
        	String strValue;
        	if (obj instanceof String) {
        		strValue = (String)obj;
        		
        	} else if (obj instanceof byte[]) {
        		strValue = new String((byte[])obj);
        		
        	} else if (obj instanceof Clob) {
        		Clob objClob = (Clob)obj;
        		try {
            		strValue = objClob.getSubString(1, (int)objClob.length());
        		} catch (Exception e) {
        			throw new NestableRuntimeException("Failed to read clob", e);
        		}
        		
        	} else {
        		strValue = obj.toString();
        	}
        	
            objProperty = new StringProperty(strValue, wasNull);
        }
        
        return objProperty;
    }
    
    //------------------------------------------------------------------- SPECIAL UPDATE FUNCTION
    
    /**
     * @param columnName The name of the column to update.
     * @param pobjProperty The property value to set.
     * @param penmDBType The type of database.
     * @throws ZXException Thrown if update fails.
     */
    public void updateProperty(String columnName, Property pobjProperty, zXType.databaseType penmDBType) throws ZXException {
        try {
            if (pobjProperty == null) {
                /**
                 * Null property.
                 */
                this.result.updateString(columnName, null);
                
            } else {
                if (pobjProperty instanceof LongProperty) {
                    // This will handle zxtype.dataType.dtAutomatic and zxtype.dataType.dtLong
                    this.result.updateLong(columnName, ((LongProperty)pobjProperty).getValue());
                    
                } else if (pobjProperty instanceof DoubleProperty) {
                    this.result.updateDouble(columnName, ((DoubleProperty)pobjProperty).getValue());
                
                //--------------- WARNING DATE HANDLING
                
                } else if (pobjProperty instanceof DateProperty) {
                    DateProperty objProperty = (DateProperty)pobjProperty;
                    long lngDate = objProperty.getValue().getTime();
                    int intDataType = objProperty.getDataType().pos;
                    
                    if (intDataType == zXType.dataType.dtDate.pos) {
                        this.result.updateDate(columnName, new java.sql.Date(lngDate));
                        
                    } else if (intDataType == zXType.dataType.dtTimestamp.pos) {
                        java.sql.Timestamp timeStamp = new java.sql.Timestamp(lngDate);
                        this.result.updateTimestamp(columnName, timeStamp);
                        
                    } else if (intDataType == zXType.dataType.dtTime.pos) {
                        this.result.updateTime(columnName, new java.sql.Time(lngDate));
                        
                    }
                    
                //--------------- WARNING DATE HANDLING
                    
                } else if (pobjProperty instanceof BooleanProperty) {
                    boolean blnValue = ((BooleanProperty)pobjProperty).getValue();
                    if (penmDBType.equals(zXType.databaseType.dbAccess)) {
                        this.result.updateBoolean(columnName, blnValue);
                        
                    } else {
                        String dbStrValue;
                        if (penmDBType.equals(zXType.databaseType.dbOracle) 
                            || penmDBType.equals(zXType.databaseType.dbDB2)
                            || penmDBType.equals(zXType.databaseType.dbAS400)) {
                            dbStrValue = blnValue?"Y":"N";
                            
                        } else {
                            dbStrValue = blnValue?"1":"0";
                        }
                        
                        this.result.updateString(columnName, dbStrValue);
                    }
                    
                } else {
                	this.result.updateString(columnName, pobjProperty.getStringValue());
                }
            }
            
        } catch (Exception e) {
            getZx().log.error("failed to update property", e);
            throw new ZXException("Failed to update property" , e);
        }
    }
    
    //------------------------------------------------------------ Column name Optimization
    
    /**
     * @param rs The resultset you want to wrap.
     * @return Returns a wrapped Resultset that allows for column caching.
     */
    private synchronized ResultSet wrapResultSet(final ResultSet rs) {
        try {
            if (getZx().log.isDebugEnabled()) {
                getZx().log.debug("Wrapping result set [" + rs + "]");
            }
            
            /**
             * Special wrapper to deal with Access.
             */
            if (this.dbType.equals(zXType.databaseType.dbAccess)) {
                return new AccessResultsetWrapper(rs, retreiveColumnNameToIndexCache(rs));
            }
            
            return new ResultSetWrapper(rs, retreiveColumnNameToIndexCache(rs));
            
        } catch(SQLException e) {
            getZx().log.info("Error wrapping result set", e);
            return rs;
        }
    }
    
    /**
     * @param rs The resultset you want to get the column cache for.
     * @return Returns the column cache for the result set.
     * @throws SQLException Thrown if getColumnCount fails.
     */
    private ColumnNameCache retreiveColumnNameToIndexCache(ResultSet rs) throws SQLException {
        if (columnNameCache == null) {
            getZx().log.trace("Building columnName->columnIndex cache");
            columnNameCache = new ColumnNameCache(rs.getMetaData().getColumnCount());
        }
        return columnNameCache;
    }
    
    //----------------------------------------------- Delegated methods.
    
    /**
     * Deletes the current row.
     * 
     * @throws Exception Thrown if deleteRow fails.
     */
    public void deleteRow() throws Exception {
        result.deleteRow();
    }
    
    /**
     * Move the result set cursor to the insert position.
     * 
     * @throws Exception Thrown if moveToInsertRow fails
     */
    public void moveToInsertRow() throws Exception {
        result.moveToInsertRow();
    }
    
    /**
     * Insert the current row.
     * 
     * @throws Exception Thrown if insertRow fails.
     */
    public void insertRow() throws Exception {
        result.insertRow();
    }
    
    /**
     * @throws Exception Thrown if cancelRowUpdates fails.
     */
    public void cancelRowUpdates() throws Exception {
        result.cancelRowUpdates();
    }
    
    /**
     * @throws Exception Thornw if updateRow fails.
     */
    public void updateRow() throws Exception {
        result.updateRow();
    }
    
    /**
     * Seek to specific postion in a resultset.
     * 
     * <pre>
     * 
     * As not all version of the jdbc drivers allow you to seek to a 
     * specific position in the resultset we
     * need this utility method to do to.
     * 
     * Moves the cursor a relative number of rows, either positive or negative.
     * Attempting to move beyond the first/last row in the
     * result set positions the cursor before/after the
     * the first/last row. Calling <code>relative(0)</code> is valid, but does
     * not change the cursor position.
     *
     * <p>Note: Calling the method <code>relative(1)</code>
     * is identical to calling the method <code>next()</code> and 
     * calling the method <code>relative(-1)</code> is identical
     * to calling the method <code>previous()</code>.
     * 
     * </pre>
     * 
     * @param pintPos The postion to move to.
     * @return Returns true if it was successful.
     * @throws Exception Thrown if relative fails.
     */
    public boolean relative(int pintPos) throws Exception {
        boolean relative = true;
        
        if (pintPos == 0) return true;
        try {
            return this.result.relative(pintPos);
            
        } catch (Exception e) {
            int skipped = 0;
            if (pintPos > 0) {
                /**
                 * Seek forwards
                 */
                while (this.result.next() && skipped < pintPos) {
                    skipped ++;
                }
                
                if (skipped < pintPos) {
                    relative = false;
                }
                
            } else if (pintPos < 0){
                /**
                 * Seek backwards
                 */
                while (this.result.previous() && skipped > pintPos) {
                    skipped--;
                }
                
                if (skipped > pintPos) {
                    relative = false;
                }
                
            } else {
                /**
                 * 0 will just stay in the current position.
                 */
            }
        }
        
        return relative;
    }
    
    //------------------------------ Clean up
    
    /**
     * Try to close the resultset and the statement which created it for clean up perposes.
     */
    public void close() {
        // Close the resultset.
        JDBCUtil.closeResultSet(this.result);
        
        // Close the prepared statement.
        // Clean up in case anything goes wrong.
    	JDBCUtil.closeStatement(stmt);
    	
        // Reset to null
        this.result = null;
        this.stmt = null;
    }
}