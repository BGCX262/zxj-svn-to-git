/*
 * Created on Apr 24, 2005
 * $Id: TXHandler.java,v 1.1.2.2 2005/05/26 12:00:21 mike Exp $
 */
package org.zxframework.datasources;

import org.jdom.Element;
import org.zxframework.ZXObject;
import org.zxframework.zXType;

/**
 * Interface definition for transaction handler.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 **/
public abstract class TXHandler extends ZXObject {
    
    private String name;
    private long txId;
    
    //--------------------- Getters/Setters
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the txId.
     */
    public long getTxId() {
        return txId;
    }
    
    /**
     * @param txId The txId to set.
     */
    public void setTxId(long txId) {
        this.txId = txId;
    }
    
    //-------------------- Abstract methods to implement.
    
    /**
     * Begins a transaction.
     * 
     * @return Returns the return code of the method.
     */
    public abstract zXType.rc beginTx();
    
    /**
     * Commit the current transaction.
     * 
     * @return Returns the return code of the method.
     */
    public abstract zXType.rc commitTx();
    
    /**
     * Rollback the current transaction.
     * 
     * @return Returns the return code of the method.
     */
    public abstract zXType.rc rollbackTx();
    
    /**
     * @return Returns whether we are in a transaction.
     */
    public abstract boolean inTx();
    
    /**
     * Parses the xml settings for the TXHandler.
     * 
     * @param pobjElment The xml node with the settings for this handler.
     * @return Returns the returns code of the method.
     */
    public abstract zXType.rc parse(Element pobjElment);
}