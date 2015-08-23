/*
 * Created on Apr 26, 2005
 * $Id: JTATransaction.java,v 1.1.2.3 2006/07/17 16:17:10 mike Exp $
 */
package org.zxframework.transaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;

// Requires a JTA implementation.
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.util.JTAHelper;

/**
 * Implements a basic transaction strategy for JTA transactions. Instances check to
 * see if there is an existing JTA transaction. If none exists, a new transaction
 * is started. If one exists, all work is done in the existing context. The
 * following properties are used to locate the underlying <tt>UserTransaction</tt>:
 * <br><br>
 * <table>
 * <tr><td><tt>hibernate.jndi.url</tt></td><td>JNDI initial context URL</td></tr>
 * <tr><td><tt>hibernate.jndi.class</tt></td><td>JNDI provider class</td></tr>
 * <tr><td><tt>jta.UserTransaction</tt></td><td>JNDI name</td></tr>
 * </table>
 * 
 * @author Gavin Kin
 */
public class JTATransaction extends ZXObject implements Transaction {
    
    //--------------------------- Members
    
    private UserTransaction ut;
    private boolean newTransaction;
    private boolean begun;
    private boolean commitFailed;
    
    //---------------------------- Constructor
   
    /**
     * Transaction constructor.
     */
    public JTATransaction() {
    	super();
    }    
    
    //----------------- Implementation spec.
    
    /**
     * @param context
     * @param utName
     * @throws ZXException
     */
    public void begin(InitialContext context, String utName) throws ZXException {
        getZx().log.debug("begin");

        getZx().log.debug("Looking for UserTransaction under: " + utName);
        try {
            ut = (UserTransaction) context.lookup(utName);
        }
        catch (NamingException ne) {
            getZx().log.error("Could not find UserTransaction in JNDI", ne);
            throw new TransactionException("Could not find UserTransaction in JNDI: ", ne);
        }
        
        if (ut==null) {
            throw new TransactionException("A naming service lookup returned null");
        }

        getZx().log.debug("Obtained UserTransaction");

        try {
            newTransaction = ut.getStatus() == Status.STATUS_NO_TRANSACTION;
            if (newTransaction) {
                ut.begin();
                getZx().log.debug("Began a new JTA transaction");
            }
        }
        catch (Exception e) {
            getZx().log.error("JTA transaction begin failed", e);
            throw new TransactionException("JTA transaction begin failed", e);
        }

        /*if (newTransaction) {
            // don't need a synchronization since we are committing
            // or rolling back the transaction ourselves - assuming
            // that we do no work in beforeTransactionCompletion()
            synchronization = false;
        }*/
        
        begun = true;
    }    
    
    //----------------- Implemented methods.
    
    /**
     * @see org.zxframework.transaction.Transaction#commit()
     */
    public void commit() throws ZXException {
        if (!begun) {
            throw new TransactionException("Transaction not successfully started");
        }
        
        getZx().log.debug("commit");
        
        if (this.newTransaction) {
            try {
                ut.commit();
                getZx().log.debug("Committed JTA UserTransaction");
                
            } catch (Exception e) {
                commitFailed = true; // so the transaction is already rolled back, by JTA spec
                getZx().log.error("JTA commit failed", e);
                throw new TransactionException("JTA commit failed: ", e);
            }
        }
        
    }
    
    /**
     * @see org.zxframework.transaction.Transaction#rollback()
     */
    public void rollback() throws ZXException {
        if (!begun) {
            throw new TransactionException("Transaction not successfully started");
        }

        getZx().log.debug("rollback");

        /*if (!synchronization && newTransaction && !commitFailed) {
            jdbcContext.beforeTransactionCompletion(this);
        }*/
        
        try {
            if (newTransaction) {
                if (!commitFailed) {
                    ut.rollback();
                    getZx().log.debug("Rolled back JTA UserTransaction");
                }
            } else {
                ut.setRollbackOnly();
                getZx().log.debug("set JTA UserTransaction to rollback only");
            }
            
        } catch (Exception e) {
            getZx().log.error("JTA rollback failed", e);
            throw new TransactionException("JTA rollback failed", e);
        }        
    }
    
    /**
     * @see org.zxframework.transaction.Transaction#wasRolledBack()
     */
    public boolean wasRolledBack() throws ZXException {

        if (!begun) return false;
        if (commitFailed) return true;

        final int status;
        try {
            status = ut.getStatus();
            
        } catch (SystemException se) {
            getZx().log.error("Could not determine transaction status", se);
            throw new TransactionException("Could not determine transaction status", se);
        }
        
        if (status == Status.STATUS_UNKNOWN) {
            throw new TransactionException("Could not determine transaction status");
        }
        
        return JTAHelper.isRollback(status);
    }
    
    /**
     * @see org.zxframework.transaction.Transaction#wasCommitted()
     */
    public boolean wasCommitted() throws ZXException {
        if (!begun || commitFailed) return false;
        
        final int status;
        try {
            status = ut.getStatus();
        } catch (SystemException se) {
            getZx().log.error("Could not determine transaction status", se);
            throw new TransactionException("Could not determine transaction status: ", se);
        }
        
        if (status==Status.STATUS_UNKNOWN) {
            throw new TransactionException("Could not determine transaction status");
        }
        
        return status == Status.STATUS_COMMITTED;
    }
}