/*
 * Created on Apr 15, 2005
 * $Id: DSChnnlTst.java,v 1.1.2.9 2006/07/17 16:13:46 mike Exp $
 */
package org.zxframework.datasources;

import org.jdom.Element;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.exception.ParsingException;
import org.zxframework.transaction.Transaction;
import org.zxframework.util.StringUtil;

/**
 * Testing data-source.
 * 
 * <pre>
 * 
 * This is a data-source handler that is designed for testing / simulation purposes.
 * It is implemented using an RDBMS as a source but behaves as a channel that
 * can be configured to either have a result-set type of 'collection' or 'stream'.
 * 
 * This data source handler can be in environment where normally a data source
 * handler would be used that relies on some external system to be available (e.g.
 * a service oriented architecture or mainframe or so).
 * On a development machine, you can change the application configuration section so
 * that this data source is used instead of the real one. This will allow you to develop
 * a system and test it against the real thing by simply changing the data source section
 * of the configuration file.
 * 
 * You may wonder why not simply to use the useHandler attribute on the datasource to make
 * a handler use another handler for emulation purposes. The simple reason is that this feature
 * is supported at such a low level that the other handler simply seems to be the main handler
 * for the BO and you simply have all the features of that handler. I.e., if the emulation handler
 * is a RDBMS and the actual live handler is a channel, you are not testing your application
 * against the limitations of a channel handler and some errors may go unnoticed....
 * 
 * Requires the following bespoke XML tag:
 * 
 * <datasource>...</datasource>
 * 
 * Name of other data source in XML file of type primary or alternative that is used
 * for the actual persistence actions.
 * 
 * And the following optional tag:
 * 
 * <delay>...</delay>
 * 
 * This is a delay (in milliseconds) that will be forced to emulate the possible performance you would
 * get with a slow channel
 * 
 * Change    : BD21APR05 - V1.5:5
 * Why       : Added support for transaction manager
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class DSChnnlTst extends DSHandler {
    
    // Implementation specific
    private int delay;
    private DSHandler DSHandler;
    private DSHandler orgDS;
    
    //------------------------- Getters and Setters
    
    /**
     * Delay (in milliseconds) that can be used to emulate
     * the delay caused by the channel that we are
     * trying to emulate.
     * 
     * @return Returns the delay.
     */
    public int getDelay() {
        return delay;
    }
    
    /**
     * @param delay The delay to set.
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }
    
    /**
     * @return Returns the dSHandler.
     */
    public DSHandler getDSHandler() {
        return DSHandler;
    }
    
    /**
     * @param handler The dSHandler to set.
     */
    public void setDSHandler(DSHandler handler) {
        DSHandler = handler;
    }
    
    /**
     * @return Returns the orgDS.
     */
    public DSHandler getOrgDS() {
        return orgDS;
    }
    
    /**
     * @param orgDS The orgDS to set.
     */
    public void setOrgDS(DSHandler orgDS) {
        this.orgDS = orgDS;
    }
    
    //------------------------------- Implemented methods.

    /**
     * @see org.zxframework.datasources.DSHandler#parse(org.jdom.Element)
     */
    public zXType.rc parse(Element pobjElement) throws ParsingException {
        zXType.rc parse = zXType.rc.rcOK;
        
        String strTmp = pobjElement.getChildText("datasource");
        
        this.DSHandler = getZx().getDataSources().getDSByName(strTmp);
        if (this.DSHandler == null) {
            throw new ParsingException("Failed to parse dshandler.");
        }
        
        strTmp = pobjElement.getChildText("delay");
        if (StringUtil.len(strTmp) > 0) {
            if (StringUtil.isNumeric(strTmp)) {
                throw new ParsingException("Delay not numeric; ignored : " + strTmp);
            }
            this.delay = Integer.parseInt(strTmp);
            
        } // Has delay tag?
        
        return parse;
    }
    
    /**
     * @see org.zxframework.datasources.DSHandler#loadBO(ZXBO, String, String, boolean, String)
     */
    public zXType.rc loadBO(ZXBO pobjBO, 
                            String pstrLoadGroup, 
                            String pstrWhereGroup, 
                            boolean pblnResolveFK, 
                            String pstrOrderByGroup) throws ZXException {
        zXType.rc loadBO = zXType.rc.rcOK;
        
        /**
         * Complex stuff: the DSHandler associated with the BO is not the DSHandler that we really
         * want to use; however, many calls down a routine called by loadBO may do a pobjBO.descriptor.getDS
         * to get the DSHandler. This would NOT be the same as DSHandler and this can cause all sorts of
         * problems mainly because I (and thus the DSHandler associated with the BO) am a channel handler
         * and DSHandler is a RDBMS handler.
         * Therefore we overwrite the BO handler for a split second and restore it afterwards. This may
         * cause yet other problems in case of recursive calls but that is the price to pay for this
         * emulation / test channel
         */
        doDelay();
        
        swapDS(pobjBO);
        
        loadBO = this.DSHandler.loadBO(pobjBO, pstrLoadGroup, pstrWhereGroup, pblnResolveFK, pstrOrderByGroup);
        
        restoreDS(pobjBO);
        
        return loadBO;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#insertBO(org.zxframework.ZXBO)
     */
    public zXType.rc insertBO(ZXBO pobjBO) throws ZXException {
        zXType.rc insertBO = zXType.rc.rcOK;
        
        doDelay();
        
        swapDS(pobjBO);
        
        insertBO = this.DSHandler.insertBO(pobjBO);
        
        restoreDS(pobjBO);
        
        return insertBO;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#updateBO(org.zxframework.ZXBO, java.lang.String, java.lang.String)
     */
    public zXType.rc updateBO(ZXBO pobjBO, String pstrUpdateGroup, String pstrWhereGroup) throws ZXException {
        zXType.rc updateBO = zXType.rc.rcOK;
        
        doDelay();
        
        swapDS(pobjBO);
        
        updateBO = this.DSHandler.updateBO(pobjBO, pstrUpdateGroup, pstrWhereGroup);
        
        restoreDS(pobjBO);
        
        return updateBO;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#deleteBO(org.zxframework.ZXBO, java.lang.String)
     */
    public zXType.rc deleteBO(ZXBO pobjBO, String pstrWhereGroup) throws ZXException {
        zXType.rc deleteBO = zXType.rc.rcOK;
        
        doDelay();
        
        swapDS(pobjBO);
        
        deleteBO = this.DSHandler.deleteBO(pobjBO, pstrWhereGroup);
        
        restoreDS(pobjBO);
        
        return deleteBO;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#boRS(ZXBO, String, String, boolean, String, boolean, int, int)
     */
    public DSRS boRS(ZXBO pobjBO, 
                     String pstrLoadGroup, 
                     String pstrWhereGroup, 
                     boolean pblnResolveFK, 
                     String pstrOrderByGroup, 
                     boolean pblnReverse,
                     int plngStartRow,
                     int plngBatchSize) throws ZXException {
        DSRS boRS = new DSRS(this);
        
        doDelay();
        
        swapDS(pobjBO);
        
        DSRS objRS = this.DSHandler.boRS(pobjBO,
                                    pstrLoadGroup,
                                    pstrWhereGroup,
                                    pblnResolveFK,
                                    pstrOrderByGroup,
                                    pblnResolveFK,
                                    plngStartRow,
                                    plngBatchSize);
        
        /**
         * Copy result to collection
         */
        ZXBO objBO;
        while (!objRS.eof()) {
            objBO = pobjBO.cloneBO();
            if (objBO == null) {
                throw new RuntimeException("Failed to load business object");
            }
            
            objRS.rs2obj(objBO, pstrLoadGroup, pblnResolveFK);
            boRS.getData().add(objBO);
            boRS.setDataCursor(1);
            objRS.moveNext();
        } // Loop over result set
        
        restoreDS(pobjBO);
        
        return boRS;
    }
    
    /**
     * @see org.zxframework.datasources.DSHandler#beginTransaction()
     */
    public Transaction beginTransaction() throws ZXException {
        return this.DSHandler.beginTransaction();
    }
    
    /**
     * @see org.zxframework.datasources.DSHandler#beginTx()
     */
    public zXType.rc beginTx() throws ZXException {
        return this.DSHandler.beginTx();
    }

    /**
     * @see org.zxframework.datasources.DSHandler#inTx()
     */
    public boolean inTx() throws ZXException {
        return this.DSHandler.inTx();
    }

    /**
     * @see org.zxframework.datasources.DSHandler#rollbackTx()
     */
    public zXType.rc rollbackTx() throws ZXException {
        doDelay();
        
        return this.DSHandler.rollbackTx();
    }

    /**
     * @see org.zxframework.datasources.DSHandler#commitTx()
     */
    public zXType.rc commitTx() throws ZXException {
        doDelay();
        
        return this.DSHandler.commitTx();
    }

    /**
     * Connect to the channel.
     * 
     * @see org.zxframework.datasources.DSHandler#connect()
     */
    public zXType.rc connect() throws ZXException {
        zXType.rc connect = zXType.rc.rcOK;
        
        doDelay();
        
        if (this.DSHandler.getState().pos != zXType.dsState.dssActive.pos) {
            this.DSHandler.connect();
        }
        
        setState(zXType.dsState.dssActive);
        
        return connect;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#disConnect()
     */
    public zXType.rc disConnect() throws ZXException {
        zXType.rc disConnect = zXType.rc.rcOK;
        
        doDelay();
        
        this.DSHandler.disConnect();
        
        setState(zXType.dsState.dssClosed);
        
        return disConnect;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#RSMoveNext(DSRS)
     */
    public zXType.rc RSMoveNext(DSRS pobjRS) throws ZXException {
        zXType.rc RSMoveNext = zXType.rc.rcOK;
        
        pobjRS.moveNext();
        
        return RSMoveNext;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#RSClose(DSRS)
     */
    public zXType.rc RSClose(DSRS pobjRS) throws ZXException {
        return pobjRS.RSClose();
    }
    
    //-------------------------------- Implementation specific methods.
    
    /**
     * Only relevant when RSType for the data source
     * handler is stream.
     * Is result set EOF; the result set is optional and
     * when not passed, assumed to be the most
     * recently created result set
     * @param pobjRS The result object
     * @return Whether at the end of the results.
     */
    public boolean RSEof(DSRS pobjRS) {
        return pobjRS.eof();
    }
    
    /**
     * Emulate a delay
     */
    private void doDelay() {
        if (this.delay > 0) {
            try {
				Thread.sleep(this.delay);
			} catch (InterruptedException e) {
                throw new RuntimeException(e);
			}
        }
    }
    
    /**
     * Set the DS handler of the BO to my true DS handler.
     * 
     * <pre>
     * 
     * No a potential catch: since the descriptor is cached, we may set it to our DSHandler
     * and call seomthing like loadBO or updateBO. If there is an event action associated with
     * this that also accesses a BO of this type (and that thus uses the same descriptor), we
     * may find ourselves with the wrong DSHandler..... No risk, no fun...
     * 
     * @param pobjBO The business object to modify.
     * @return Returns the return code of swapDS.
     */
    public zXType.rc swapDS(ZXBO pobjBO) {
        zXType.rc swapDS = zXType.rc.rcOK;
        
        if (pobjBO.getDSHandler() == this.DSHandler) {
            this.orgDS = pobjBO.getDSHandler();
            pobjBO.setDSHandler(this.DSHandler);
        }
        
        return swapDS;
    }
    
    /**
     * Undo he results of swapDS.
     * 
     * @param pobjBO The business object to restore.
     * @return Returns the return code of restoreDS.
     */
    public zXType.rc restoreDS(ZXBO pobjBO) {
        zXType.rc restoreDS = zXType.rc.rcOK;
        
        if (this.orgDS != null) {
            pobjBO.setDSHandler(this.orgDS);
        }
        
        return restoreDS;
    }
}