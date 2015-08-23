/*
 * Created on Apr 26, 2005
 * $Id: JDBCTransaction.java,v 1.1.2.1 2005/04/26 12:11:23 mike Exp $
 */
package org.zxframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import org.zxframework.ZXException;
import org.zxframework.ZXObject;

/**
 * Implements a basic transaction strategy for JDBC connections.This is the
 * default <tt>Transaction</tt> implementation used if none is explicitly
 * specified.
 * 
 * @author Anton van Straaten, Gavin King
 */
public class JDBCTransaction extends ZXObject implements Transaction {
    
	private Connection conn;
    
	private boolean toggleAutoCommit;
	private boolean rolledBack;
	private boolean committed;
	private boolean begun;
	private boolean commitFailed;
	
	/**
	 * Transaction constructor.
	 * 
	 * @param conn A handle to the active db connection.
	 */
	public JDBCTransaction(Connection conn) {
		this.conn = conn;
	}
	
	/**
	 * Begins a transaction.
	 * 
	 * @throws ZXException Thrown if begin fails
	 */
	public void begin() throws ZXException {
        if ( getZx().log.isDebugEnabled()) {
            getZx().log.debug("begin");
        }
        
		try {
		    toggleAutoCommit = conn.getAutoCommit();
            
			if ( getZx().log.isDebugEnabled()){
                getZx().log.debug("Current autocommit status : " + toggleAutoCommit);
            }
            
			if (toggleAutoCommit) {
                getZx().log.debug("Disabling autocommit");
				conn.setAutoCommit(false);
			}
            
		} catch (SQLException e) {
		    getZx().trace.addError("JDBC begin failed", e);
			throw new TransactionException("JDBC begin failed: ", e);
		}
        
		begun = true;
	}
	
	/**
	 * Flush the associated <tt>Session</tt> and end the unit of work.
	 * This method will commit the underlying transaction if and only
	 * if the transaction was initiated by this object.
	 *
	 * @throws ZXException Thrown if commit fails.
	 */
	public void commit() throws ZXException {
		if (!begun) {
            throw new TransactionException("Transaction not successfully started");
        }
        
        getZx().log.debug("commit");
        
		try {
			conn.commit();
            
            getZx().log.debug("Committed JDBC Connection");
			committed = true;
            
		} catch (SQLException e) {
		    getZx().trace.addError("JDBC commit failed", e);
			commitFailed = true;
            
			throw new TransactionException("Commit failed with SQL exception: ", e);
		} finally {
			toggleAutoCommit();
		}
	}
	
	/**
	 * Force the underlying transaction to roll back.
	 *
	 * @throws ZXException Thrown if rollback fails.
	 */
	public void rollback() throws ZXException {
		
		if (!begun) {
            throw new TransactionException("Transaction not successfully started");
        }
        getZx().log.debug("rollback");
        
		if (!commitFailed) {
            /**
             * Only rollback if the current transaction failed.
             */
			try {
				conn.rollback();
                getZx().log.debug("rolled back JDBC Connection");
				rolledBack = true;
                
			} catch (SQLException e) {
				getZx().log.error("JDBC rollback failed", e);
                
				throw new TransactionException("JDBC rollback failed", e);
                
			} finally {
				toggleAutoCommit();
			}
		}
	}
	
    /**
     * Turn autocommit back on if necessary.
     */
	private void toggleAutoCommit() {
		try {
			if (toggleAutoCommit) {
                getZx().log.debug("re-enabling autocommit");
				conn.setAutoCommit(true);
			}
		} catch (Exception sqle) {
		    getZx().trace.addError("Could not toggle autocommit", sqle);
			//swallow it (the transaction _was_ successful or successfully rolled back)
		}
	}
    
	/**
	 * Was this transaction rolled back or set to rollback only?
	 *
	 * @return boolean Whether we successfully rolled back.
	 */
	public boolean wasRolledBack() {
		return rolledBack;
	}
	
	/**
	 * Check if this transaction was successfully committed. This method
	 * could return <tt>false</tt> even after successful invocation
	 * of <tt>commit()</tt>.
	 *
	 * @return boolean Whether we sucessfully commited
	 */
	public boolean wasCommitted() {
		return committed;
	}
}