/*
 * Created on May 20, 2005
 */
package org.zxframework.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A very nasty hack to support Access.
 * 
 * Why ? Well if in Access i call
 * rs.getString() twice on the same column we get a SQLException.
 * 
 * Lets not try to understand why, coz we may fix this by ensure that 
 * in RS2OBJ etc do not call getObject twice etc..
 * 
 * NOTE : called getString etc.. will coz the same failure.
 * 
 * @author Michael Brewer
 */
public class AccessResultsetWrapper extends ResultSetWrapper {
    
    private Object[] value;
    private final int columnCount;
    private boolean next;
    
    /** Inner class representing null resultset values. */
    private final class WASNULL { 
        /** Resultset Value */
        public final Object o;
        private WASNULL(Object o) { this.o = o; }
    }
    
    /**
     * Constructs a AccessResultsetWrapper.
     * 
     * @param resultSet The result set.
     * @param columnNameCache The column cache to use.
     */
    public AccessResultsetWrapper(ResultSet resultSet, ColumnNameCache columnNameCache) {
        super(resultSet, columnNameCache);
        
        try {
            /** One more as getObject is 1 based and Arrays are 0 based. */
            this.columnCount = resultSet.getMetaData().getColumnCount() + 1;
            this.value = new Object[this.columnCount];
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get column count.");
        }
    }
    
    /**
     * NOTE : This is used by ZXResultset.
     * 
     * @see java.sql.ResultSet#getObject(int)
     */
    public Object getObject(int columnIndex) throws SQLException {
        Object getObject = this.value[columnIndex];
        if (getObject != null) {
            if (getObject instanceof WASNULL) {
                return ((WASNULL)getObject).o;
            }
            
            return getObject;
        }
        
        /**
         * True if this is the first time we request this column.
         */
        getObject = getTarget().getObject(columnIndex);
        
        boolean wasNull = getTarget().wasNull();
        if (wasNull) {
            this.value[columnIndex] = new WASNULL(getObject);
        } else {
            this.value[columnIndex] = getObject;
        }
        return getObject;
        
    }

    /**
     * @see java.sql.ResultSet#next()
     */
    public boolean next() throws SQLException {
        /** Reset the resultSet cache. */
        this.value = new Object[this.columnCount];
        this.next = getTarget().next();
        
        return this.next;
    }

    /**
     * @see java.sql.ResultSet#isAfterLast()
     */
    public boolean isAfterLast() throws SQLException {
        try {
            return getTarget().isAfterLast();
        } catch (Exception e) {
            /** Access does not support JDBC 2.0 */
            return (!next);
        }
    }
}