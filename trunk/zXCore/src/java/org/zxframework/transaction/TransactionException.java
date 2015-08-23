/*
 * Created on Apr 26, 2005
 * $Id: TransactionException.java,v 1.1.2.1 2005/04/26 12:11:23 mike Exp $
 */
package org.zxframework.transaction;

import org.zxframework.ZXException;

/**
 * Indicates that a transaction could not be begun, committed
 * or rolled back.
 * 
 * @see Transaction
 * @author Anton van Straaten
 **/
public class TransactionException extends ZXException {
    /**
     * @param message The exception message.
     * @param root The cause exception
     */
    public TransactionException(String message, Exception root) {
        super(message,root);
    }
    
    /**
     * @param message The exception message.
     */
    public TransactionException(String message) {
        super(message);
    }
}