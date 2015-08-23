// $Id: ColumnNameCache.java,v 1.1.2.3 2006/07/17 16:18:27 mike Exp $
package org.zxframework.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of ColumnNameCache.
 *
 * @author Steve Ebersole
 */
public class ColumnNameCache {

	private final Map columnNameToIndexCache;

	/**
	 * @param columnCount
	 */
	public ColumnNameCache(int columnCount) {
		// should *not* need to grow beyond the size of the total number of columns in the rs
		this.columnNameToIndexCache = new HashMap(columnCount);
	}

	/**
	 * @param columnName The name of the column.
	 * @param rs The resultset.
	 * @return Returns the int postion in the resultset.
	 * @throws SQLException Thrown if findColumn fails.
	 */
	public int getIndexForColumnName(String columnName, ResultSetWrapper rs)throws SQLException {
		Integer cached = ( Integer ) columnNameToIndexCache.get( columnName );
		if ( cached != null ) {
			return cached.intValue();
		}
		
        int index = rs.getTarget().findColumn(columnName);
        columnNameToIndexCache.put(columnName, new Integer(index) );
        return index;
	}
}
