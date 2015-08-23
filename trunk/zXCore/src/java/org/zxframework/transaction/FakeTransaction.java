/*
 * Created on Jun 3, 2005
 * $Id: FakeTransaction.java,v 1.1.2.2 2006/07/17 16:17:10 mike Exp $
 */
package org.zxframework.transaction;

import org.zxframework.ZXException;

/**
 * A fake transaction.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class FakeTransaction implements Transaction {

	/**
	 * @see org.zxframework.transaction.Transaction#commit()
	 */
	public void commit() throws ZXException {
		/**
		 * Nothing happens here.
		 */
	}

	/**
	 * @see org.zxframework.transaction.Transaction#rollback()
	 */
	public void rollback() throws ZXException {
		/**
		 * Nothing happens here.
		 */
	}

	/**
	 * @see org.zxframework.transaction.Transaction#wasRolledBack()
	 */
	public boolean wasRolledBack() throws ZXException {
		return false;
	}

	/**
	 * @see org.zxframework.transaction.Transaction#wasCommitted()
	 */
	public boolean wasCommitted() throws ZXException {
		return false;
	}
}