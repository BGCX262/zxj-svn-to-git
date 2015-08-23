//$Id: Transaction.java,v 1.1.2.1 2005/04/26 12:11:23 mike Exp $
package org.zxframework.transaction;

import org.zxframework.ZXException;

/**
 * Allows the application to define units of work, while
 * maintaining abstraction from the underlying transaction
 * implementation (eg. JTA, JDBC).<br>
 * <br>
 * A transaction is associated with a <tt>Session</tt> and is
 * usually instantiated by a call to <tt>Session.beginTransaction()</tt>.
 * A single session might span multiple transactions since
 * the notion of a session (a conversation between the application
 * and the datastore) is of coarser granularity than the notion of
 * a transaction. However, it is intended that there be at most one
 * uncommitted <tt>Transaction</tt> associated with a particular
 * <tt>Session</tt> at any time.<br>
 * <br>
 * Implementors are not intended to be threadsafe.
 *
 * @author Anton van Straaten
 */
public interface Transaction {
	
	/**
	 * Flush the associated <tt>Session</tt> and end the unit of work.
	 * This method will commit the underlying transaction if and only
	 * if the transaction was initiated by this object.
	 *
	 * @throws ZXException
	 */
	public void commit() throws ZXException;
	
	/**
	 * Force the underlying transaction to roll back.
	 *
	 * @throws ZXException Thrown if rollback fails.
	 */
	public void rollback() throws ZXException;
	
	/**
	 * Was this transaction rolled back or set to rollback only?
	 *
	 * @return boolean Returns true if transaction was rolled back.
	 * @throws ZXException Thrown if wasRolledBack failed.
	 */
	public boolean wasRolledBack() throws ZXException;
	
	/**
	 * Check if this transaction was successfully committed. This method
	 * could return <tt>false</tt> even after successful invocation
	 * of <tt>commit()</tt>.
	 *
	 * @return boolean
	 * @throws ZXException
	 */
	public boolean wasCommitted() throws ZXException;
}