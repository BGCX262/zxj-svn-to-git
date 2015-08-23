/*
 * Created on Apr 24, 2005
 * $Id: TxHandlers.java,v 1.1.2.3 2005/05/03 12:55:01 mike Exp $
 */
package org.zxframework.datasources;

import java.util.Iterator;
import java.util.Map;

import org.jdom.Element;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;

/**
 * The object that manages all transaction handlers.
 * 
 * <pre>
 * 
 * It is important to understand how this all works: zX has handle to datasources who
 * has handle this txHandlers (ie 'me').
 * In the application configuration file you can specify tx handlers (this is optional)
 * and for each data source you can specify which tx handler to use. Thus you can have
 * multiple data sources using a single tx handler.
 *
 * The developer can either begin / rollback or commit a transaction through this object
 * or using the data source individually.
 * The data source handler MUST use the tx Handler if one has been assigned to actually implement
 * the tx support, otherwise the datasource can implement tx handling however it pleases
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class TxHandlers extends ZXObject {
    
    private Map txHandlers;
    
    /**
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if init fails.
     */
    public zXType.rc init() throws ZXException {
        zXType.rc init = zXType.rc.rcOK;
        
        init = parse(getZx().configXMLNode("//txHandlers"));
        
        return init;
    }
    
    /**
     * Parses the TXhandlers settings.
     * 
     * @param pobjElement The xml node of the TXhandler settings.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if parse fails.
     */
    private zXType.rc parse(Element pobjElement) throws ZXException {
        zXType.rc parse = zXType.rc.rcOK;
        Element objXMLNode;
        
        Iterator iter = pobjElement.getChildren().iterator();
        while (iter.hasNext()) {
            objXMLNode = (Element)iter.next();
            
            /**
             * Create new handler based on the details provided
             */
            if (createHandler(objXMLNode).pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Unable to create new TX handler");
            }
            
        } // Loop over all data sources
        
        return parse;
    }
    
    /**
     * Create a new handler based on the details provided in the XML Node.
     * 
     * @param pobjElement The xml node with the settings.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if createHandler fails.
     */
    private zXType.rc createHandler(Element pobjElement) throws ZXException {
        zXType.rc createHandler = zXType.rc.rcOK;
        
        /**
         * Name MUST be provided
         */
        String strName = pobjElement.getAttributeValue("name");
        if (StringUtil.len(strName) == 0) {
            throw new ZXException("No name provided for alternative data source");
        } // Name must be provided
        
        /**
         * Class MUST be provided
         */
        String strClass = pobjElement.getAttributeValue("class");
        if (StringUtil.len(strClass) == 0) {
            throw new ZXException("Class not provided");
        } // Class not provided
        
        TXHandler objHandler;
        
        objHandler = (TXHandler)getZx().createObject(strClass);
        
        /** init the TXHandler. **/
        objHandler.parse(pobjElement);
        
        this.txHandlers.put(strName, objHandler);
        
        return createHandler;
    }
    
    /**
     * Get a TX handler by name.
     * 
     * @param pstrName The name of the txhandler.
     * @return Returns the txhandler.
     */
    public TXHandler getHandlerByName(String pstrName) {
        TXHandler getHandlerByName;
        
        getHandlerByName = (TXHandler)this.txHandlers.get(pstrName);
        
        return getHandlerByName;
    }
       
    /**
     * Begin transaction for all tx handlers.
     * 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if beginTx fails.
     */
    public zXType.rc beginTx() throws ZXException {
        zXType.rc beginTx = zXType.rc.rcOK;
        
        /**
         * Generate a new transaction id
         */
        long lngTxId = System.currentTimeMillis();
        
        DSHandler objDSHandler;
        Iterator iter = getZx().getDataSources().getDataSources().values().iterator();
        while (iter.hasNext()) {
            objDSHandler =(DSHandler)iter.next();
            
            if (objDSHandler.beginTx().pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Unable to begin transaction", objDSHandler.getName());
            }
            
            objDSHandler.setTxId(lngTxId);
            
            /**
             * If the DS handler has an explicit tx handler defined, tell this handler what
             * transaction id we are using; this is important for a scenario as follows:
             *
             * datasource    tx handler
             * -------------------------
             * DS-1          TX-1
             * DS-2          TX-1
             * DS-3          TX-1
             *
             * Now imagine that the developer does not use this beginTx method but the the beginTx
             * method of each data source handler.
             * Next he does a commitTx on each one individually, so after comitting DS-1, TX-1 is
             * no longer in a transaction. The idea now is as follows: as long as the txId associated
             * with DS-2 (and DS-3) is the same as the txId as the Tx handlers txId, it is fine...
             */
            if (objDSHandler.getTxHandler() != null) {
                objDSHandler.getTxHandler().setTxId(lngTxId);
            }
            
        } // Loop over datasources
        
        return beginTx;
    }
    
    /**
     * Commit transaction for all tx handlers.
     * 
     * @return Returns whether there is a open transaction.
     * @throws ZXException Thrown if commitTx fails.
     */
    public zXType.rc commitTx() throws ZXException {
        zXType.rc commitTx = zXType.rc.rcOK;
        DSHandler objDSHandler;
        
        Iterator iter = getZx().getDataSources().getDataSources().values().iterator();
        while(iter.hasNext()) {
            objDSHandler = (DSHandler)iter.next();
            
            if (objDSHandler.inTx()) {
                if (objDSHandler.commitTx().pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to commit transaction", objDSHandler.getName());
                } // Can commit?
                
            } // In tx?
            
        } // Loop over datasources
        
        return commitTx;
    }
    
    /**
     * Rollback transaction for all tx handlers
     * 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if rollbackTx fails.
     */
    public zXType.rc rollbackTx() throws ZXException {
        zXType.rc rollbackTx = zXType.rc.rcOK;
        DSHandler objDSHandler;
        
        Iterator iter = getZx().getDataSources().getDataSources().values().iterator();
        while(iter.hasNext()) {
            objDSHandler = (DSHandler)iter.next();
            
            if (objDSHandler.inTx()) {
                
                if (objDSHandler.rollbackTx().pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to rollback transaction", objDSHandler.getName());
                } // Can rollback?
                
            } // In tx?
            
        } // Loop over datasources
        
        return rollbackTx;
    }    
    /**
     * Is there any datasource in tx?
     * 
     * @return Returns whether there is a open transaction.
     * @throws ZXException Thrown if inTx fails.
     */
    public boolean inTx() throws ZXException {
        boolean inTx = false;
        DSHandler objDSHandler;
        
        Iterator iter = getZx().getDataSources().getDataSources().values().iterator();
        while(iter.hasNext()) {
            objDSHandler = (DSHandler)iter.next();
            
            if (objDSHandler.inTx()) {
                inTx = true;
                return inTx;
            } // In tx?
            
        } // Loop over datasources
        
        return inTx;
    }
}