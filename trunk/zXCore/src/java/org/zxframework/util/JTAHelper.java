/*
 * Created on Apr 26, 2005
 * $Id: JTAHelper.java,v 1.1.2.3 2006/07/17 16:15:53 mike Exp $
 */
package org.zxframework.util;

//Requires a JTA implementation.
import javax.transaction.Status;

/**
 * @author Gavin King
 */
public final class JTAHelper {
    
    private JTAHelper() {
    	super();
    }

    /**
     * @param status The JTA status id.
     * @return Returns true if the status rollback.
     */
    public static boolean isRollback(int status) {
        return status==Status.STATUS_MARKED_ROLLBACK 
               || status==Status.STATUS_ROLLING_BACK 
               || status==Status.STATUS_ROLLEDBACK;
    }
    
    /**
     * @param status The JTA status id.
     * @return Returns true if the status is in process.
     */
    public static boolean isInProgress(int status) {
        return status==Status.STATUS_ACTIVE 
               || status==Status.STATUS_MARKED_ROLLBACK;
    }
}