/*
 * Created on Apr 15, 2005
 * $Id: DSHandler.java,v 1.1.2.11 2006/07/17 16:26:01 mike Exp $
 */
package org.zxframework.datasources;

import org.jdom.Element;

import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.exception.ParsingException;
import org.zxframework.transaction.Transaction;

/**
 * Interface definition for data sources.
 * 
 * <pre>
 * 
 * What  : BD21APR05 - V1.5:5
 * Why   : Added support for central transaction management
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public abstract class DSHandler extends ZXObject {
    
    private String name;
    private zXType.dsType dsType;
    private zXType.dsState state;
    private zXType.dsSearchSupport searchSupport;
    private String searchGroup;
    private zXType.dsOrderSupport orderSupport;
    private String orderGroup;
    private boolean supportsInsert;
    private boolean supportsDelete;
    private boolean supportsUpdate;
    private boolean supportsLoad;
    private zXType.dsTxSupport txSupport;
    private zXType.rc lastErrorNumber;
    private String lastErrorString;
    private zXType.dsRSType rsType;
    private String useHandler;
    
    private long txId;
    private String txHandlerName;
    private TXHandler txHandler;
    
    //----------- Dateformatting
    private String dateFormat;
    private String timeFormat;
    private String timestampFormat;

    /**
     * Default constructor.
     */
    public DSHandler() {
        super();
    }
    
    /**
     * @return Returns the dsType.
     */
    public zXType.dsType getDsType() {
        return dsType;
    }
    
    /**
     * @param dsType The dsType to set.
     */
    public void setDsType(zXType.dsType dsType) {
        this.dsType = dsType;
    }
    
    /**
     * @return Returns the txHandler.
     */
    public TXHandler getTxHandler() {
        return txHandler;
    }
    
    /**
     * @param txHandler The txHandler to set.
     */
    public void setTxHandler(TXHandler txHandler) {
        this.txHandler = txHandler;
    }
    
    /**
     * @return Returns the txHandlerName.
     */
    public String getTxHandlerName() {
        return txHandlerName;
    }
    
    /**
     * @param txHandlerName The txHandlerName to set.
     */
    public void setTxHandlerName(String txHandlerName) {
        this.txHandlerName = txHandlerName;
    }
    
    /**
     * The identifier of the most recent transaction.
     * 
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
    
    /**
     * @return Returns the lastErrorNumber.
     */
    public zXType.rc getLastErrorNumber() {
        return lastErrorNumber;
    }
    
    /**
     * @param lastErrorNumber The lastErrorNumber to set.
     */
    public void setLastErrorNumber(zXType.rc lastErrorNumber) {
        this.lastErrorNumber = lastErrorNumber;
    }
    
    /**
     * @return Returns the lastErrorString.
     */
    public String getLastErrorString() {
        return lastErrorString;
    }
    
    /**
     * @param lastErrorString The lastErrorString to set.
     */
    public void setLastErrorString(String lastErrorString) {
        this.lastErrorString = lastErrorString;
    }
    
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
     * @return Returns the orderGroup.
     */
    public String getOrderGroup() {
        return orderGroup;
    }
    
    /**
     * @param orderGroup The orderGroup to set.
     */
    public void setOrderGroup(String orderGroup) {
        this.orderGroup = orderGroup;
    }
    
    /**
     * @return Returns the orderSupport.
     */
    public zXType.dsOrderSupport getOrderSupport() {
        return orderSupport;
    }
    
    /**
     * @param orderSupport The orderSupport to set.
     */
    public void setOrderSupport(zXType.dsOrderSupport orderSupport) {
        this.orderSupport = orderSupport;
    }
    
    /**
     * Type of result-set for this data-source handler.
     * 
     * @return Returns the rsType.
     */
    public zXType.dsRSType getRsType() {
        return rsType;
    }
    
    /**
     * @param rsType The rsType to set.
     */
    public void setRsType(zXType.dsRSType rsType) {
        this.rsType = rsType;
    }
    
    /**
     * @return Returns the searchGroup.
     */
    public String getSearchGroup() {
        return searchGroup;
    }
    
    /**
     * @param searchGroup The searchGroup to set.
     */
    public void setSearchGroup(String searchGroup) {
        this.searchGroup = searchGroup;
    }
    
    /**
     * @return Returns the searchSupport.
     */
    public zXType.dsSearchSupport getSearchSupport() {
        return searchSupport;
    }
    
    /**
     * @param searchSupport The searchSupport to set.
     */
    public void setSearchSupport(zXType.dsSearchSupport searchSupport) {
        this.searchSupport = searchSupport;
    }
    
    /**
     * @return Returns the state.
     */
    public zXType.dsState getState() {
        return state;
    }
    
    /**
     * @param state The state to set.
     */
    public void setState(zXType.dsState state) {
        this.state = state;
    }
    
    /**
     * @return Returns the supportsDelete.
     */
    public boolean isSupportsDelete() {
        return supportsDelete;
    }
    
    /**
     * @param supportsDelete The supportsDelete to set.
     */
    public void setSupportsDelete(boolean supportsDelete) {
        this.supportsDelete = supportsDelete;
    }
    
    /**
     * @return Returns the supportsInsert.
     */
    public boolean isSupportsInsert() {
        return supportsInsert;
    }
    
    /**
     * @param supportsInsert The supportsInsert to set.
     */
    public void setSupportsInsert(boolean supportsInsert) {
        this.supportsInsert = supportsInsert;
    }
    
    /**
     * @return Returns the supportsLoad.
     */
    public boolean isSupportsLoad() {
        return supportsLoad;
    }
    
    /**
     * @param supportsLoad The supportsLoad to set.
     */
    public void setSupportsLoad(boolean supportsLoad) {
        this.supportsLoad = supportsLoad;
    }
    
    /**
     * @return Returns the supportsUpdate.
     */
    public boolean isSupportsUpdate() {
        return supportsUpdate;
    }
    
    /**
     * @param supportsUpdate The supportsUpdate to set.
     */
    public void setSupportsUpdate(boolean supportsUpdate) {
        this.supportsUpdate = supportsUpdate;
    }
    
    /**
     * @return Returns the txSupport.
     */
    public zXType.dsTxSupport getTxSupport() {
        return txSupport;
    }
    
    /**
     * @param txSupport The txSupport to set.
     */
    public void setTxSupport(zXType.dsTxSupport txSupport) {
        this.txSupport = txSupport;
    }
    
    /**
     * Special feature that can be used for simulations:
     * this means effectively use the handler as specified
     * 
     * @return Returns the useHandler.
     */
    public String getUseHandler() {
        return useHandler;
    }
    
    /**
     * @param useHandler The useHandler to set.
     */
    public void setUseHandler(String useHandler) {
        this.useHandler = useHandler;
    }
    
    /**
     * @return Returns the dateFormat.
     */
    public String getDateFormat() {
        return dateFormat;
    }
    
    /**
     * @param dateFormat The dateFormat to set.
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
    
    /**
     * @return Returns the timeFormat.
     */
    public String getTimeFormat() {
        return timeFormat;
    }
    
    /**
     * @param timeFormat The timeFormat to set.
     */
    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }
    
    /**
     * @return Returns the timestampFormat.
     */
    public String getTimestampFormat() {
        return timestampFormat;
    }
    
    /**
     * @param timestampFormat The timestampFormat to set.
     */
    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }
    
    //------------------------------- Abstract methods
    
    /**
     * Parse the xml configuration for this datasource handler.
     * 
     * @param pobjElement The xml node to parse.
     * @return Returns the return code of parse.
     * @throws ParsingException Thrown if parse fails.
     */
    public abstract zXType.rc parse(Element pobjElement) throws ParsingException;
    
    /**
     * @param pobjBO The business object to load.
     * @param pstrLoadGroup Optional, default should be "*"
     * @param pstrWhereGroup Optional, default should be "+"
     * @param pblnResolveFK Optional, default should be false
     * @param pstrOrderByGroup Optional, default should be null.
     * @return Returns whether loadBO succeeded.
     * @throws ZXException Thrown if loadBO fails.
     */
    public abstract zXType.rc loadBO(ZXBO pobjBO, 
                                     String pstrLoadGroup, 
                                     String pstrWhereGroup, 
                                     boolean pblnResolveFK, 
                                     String pstrOrderByGroup) throws ZXException;
    
    /**
     * @param pobjBO The business object to load.
     * @return Returns whether loadBO succeeded.
     * @throws ZXException Thrown if loadBO fails.
     */
    public zXType.rc loadBO(ZXBO pobjBO) throws ZXException {
        return loadBO(pobjBO, "*", "+", false, null);
    }
    
    /**
     * @param pobjBO The business object to load.
     * @param pstrLoadGroup Optional, default should be "*"
     * @return Returns whether loadBO succeeded.
     * @throws ZXException Thrown if loadBO fails.
     */
    public zXType.rc loadBO(ZXBO pobjBO, String pstrLoadGroup) throws ZXException {
        return loadBO(pobjBO, pstrLoadGroup, "+", false, null);
    }
    
    /**
     * @param pobjBO The business object to load.
     * @param pstrLoadGroup Optional, default should be "*"
     * @param pstrWhereGroup Optional, default should be "+"
     * @return Returns whether loadBO succeeded.
     * @throws ZXException Thrown if loadBO fails.
     */
    public zXType.rc loadBO(ZXBO pobjBO, 
                            String pstrLoadGroup, 
                            String pstrWhereGroup) throws ZXException {
        return loadBO(pobjBO, pstrLoadGroup, pstrWhereGroup, false, null);
    }
    
    /**
     * @param pobjBO The business object to load.
     * @param pstrLoadGroup Optional, default should be "*"
     * @param pstrWhereGroup Optional, default should be "+"
     * @param pblnResolveFK Optional, default should be false
     * @return Returns whether loadBO succeeded.
     * @throws ZXException Thrown if loadBO fails.
     */
    public zXType.rc loadBO(ZXBO pobjBO, 
                            String pstrLoadGroup, 
                            String pstrWhereGroup, 
                            boolean pblnResolveFK) throws ZXException {
        return loadBO(pobjBO, pstrLoadGroup, pstrWhereGroup, pblnResolveFK, null);
    }
    
    
    /**
     * @param pobjBO The business object to insert.
     * @return Returns whether insertBO succeeded.
     * @throws ZXException Thrown if insertBO fails.
     */
    public abstract zXType.rc insertBO(ZXBO pobjBO) throws ZXException;
    
    /**
     * @param pobjBO The business object to update.
     * @param pstrUpdateGroup Optional, default should "*"
     * @param pstrWhereGroup Optional, default should "+"
     * @return Returns whether insertBO succeeded.
     * @throws ZXException Thrown if updateBO fails.
     */
    public abstract zXType.rc updateBO(ZXBO pobjBO, 
                                       String pstrUpdateGroup, 
                                       String pstrWhereGroup) throws ZXException;
    
    /**
     * @param pobjBO The business object to update.
     * @return Return whether updateBO succeeded.
     * @throws ZXException Thrown if updateBO fails.
     */
    public zXType.rc updateBO(ZXBO pobjBO) throws ZXException {
        return updateBO(pobjBO, "*", "+");
    }
    
    /**
     * @param pobjBO The business object to update.
     * @param pstrUpdateGroup Optional, default should "*".
     * @return Return whether updateBO succeeded.
     * @throws ZXException Thrown if updateBO fails.
     */
    public zXType.rc updateBO(ZXBO pobjBO, String pstrUpdateGroup) throws ZXException {
        return updateBO(pobjBO, pstrUpdateGroup, "+");
    }
    
    /**
     * 
     * @param pobjBO The business object to update.
     * @param pstrWhereGroup Optional, default should be null.
     * @return Return whether deleteBO succeeded.
     * @throws ZXException Thrown if deleteBO fails
     */
    public abstract zXType.rc deleteBO(ZXBO pobjBO, String pstrWhereGroup) throws ZXException;
    
    /**
     * @param pobjBO The business object to update.
     * @return Return whether updateBO succeeded.
     * @throws ZXException Thrown if deleteBO fails
     */
    public zXType.rc deleteBO(ZXBO pobjBO) throws ZXException{
        return deleteBO(pobjBO, null);
    }
    
    /**
     * @param pobjBO The business object to populate the result object.
     * @param pstrLoadGroup Optional, default should be "*".
     * @param pstrWhereGroup Optional, default should be null.
     * @param pblnResolveFK Optional, default should be false.
     * @param pstrOrderByGroup Optional, default should be null.
     * @param pblnReverse Optional, default should be false.
     * @param plngStartRow The start row.
     * @param plngBatchSize The number of rows to return.
     * @return Returns the DSRS.
     * @throws ZXException Thrown if boRS fails.
     */
    public abstract DSRS boRS(ZXBO pobjBO, 
                              String pstrLoadGroup, 
                              String pstrWhereGroup,
                              boolean pblnResolveFK, 
                              String pstrOrderByGroup, 
                              boolean pblnReverse,
                              int plngStartRow,
                              int plngBatchSize) throws ZXException;
    
    /**
     * @param pobjBO The business object to populate the result object.
     * @return Returns the DSRS.
     * @throws ZXException Thrown if boRS fails.
     */    
    public DSRS boRS(ZXBO pobjBO) throws ZXException {
        return boRS(pobjBO, "*", null, false, null, false, 0, 0);
    }
    
    /**
     * @param pobjBO The business object to populate the result object.
     * @param pstrLoadGroup Optional, default should be "*". 
     * @return Returns the DSRS.
     * @throws ZXException Thrown if boRS fails.
     */    
    public DSRS boRS(ZXBO pobjBO, String pstrLoadGroup) throws ZXException {
        return boRS(pobjBO, pstrLoadGroup, null, false, null, false, 0, 0);
    }
    
    /**
     * @param pobjBO The business object to populate the result object.
     * @param pstrLoadGroup Optional, default should be "*". 
     * @param pstrWhereGroup Optional, default should be null.
     * @return Returns the DSRS.
     * @throws ZXException Thrown if boRS fails.
     */    
    public DSRS boRS(ZXBO pobjBO, String pstrLoadGroup, String pstrWhereGroup) throws ZXException {
        return boRS(pobjBO, pstrLoadGroup, pstrWhereGroup, false, null, false, 0, 0);
    }
    
    /**
     * @param pobjBO The business object to populate the result object.
     * @param pstrLoadGroup Optional, default should be "*". 
     * @param pstrWhereGroup Optional, default should be null.
     * @param pblnResolveFK Optional, default should be false.
     * @return Returns the DSRS.
     * @throws ZXException Thrown if boRS fails.
     */    
    public DSRS boRS(ZXBO pobjBO, 
    				 String pstrLoadGroup, 
    				 String pstrWhereGroup, 
    				 boolean pblnResolveFK) throws ZXException {
        return boRS(pobjBO, pstrLoadGroup, pstrWhereGroup, pblnResolveFK, null, false, 0, 0);
    }
    
    /**
     * @param pobjBO The business object to populate the result object.
     * @param pstrLoadGroup Optional, default should be "*". 
     * @param pstrWhereGroup Optional, default should be null.
     * @param pblnResolveFK Optional, default should be false.
     * @param pstrOrderByGroup Optional, default should be null.
     * @return Returns the DSRS.
     * @throws ZXException Thrown if boRS fails.
     */    
    public DSRS boRS(ZXBO pobjBO, 
    				 String pstrLoadGroup, 
    				 String pstrWhereGroup, 
    				 boolean pblnResolveFK, 
    				 String pstrOrderByGroup) throws ZXException {
        return boRS(pobjBO, pstrLoadGroup, pstrWhereGroup, pblnResolveFK, pstrOrderByGroup, false, 0, 0);
    }
    
    //--------------------------- Transaction handling.
    
    /**
     * Creates a new transaction and returns a handle to the current transaction.
     * 
     * @return Returns the transaction object.
     * @throws ZXException Thrown if begin fails.
     */
    public abstract Transaction beginTransaction() throws ZXException;
    
    /**
     * @return Returns the return code of beginTx.
     * @throws ZXException Thrown if beginTx fails.
     */
    public abstract zXType.rc beginTx() throws ZXException;
    
    /**
     * @return Returns the return code of inTx.
     * @throws ZXException Thrown if inTx fails.
     */
    public abstract boolean inTx()throws ZXException;
    
    /**
     * @return Returns the return code of rollbackTx.
     * @throws ZXException Thrown if rollbackTx fails.
     */
    public abstract zXType.rc rollbackTx()throws ZXException;
    
    /**
     * @return Returns the return code of commitTx.
     * @throws ZXException Thrown if commitTx fails.
     */
    public abstract zXType.rc commitTx()throws ZXException;
    
    /**
     * @return Returns the return code of commitTx.
     * @throws ZXException Thrown if connect fails.
     */
    public abstract zXType.rc connect()throws ZXException;
    
    /**
     * @return Returns the return code of commitTx.
     * @throws ZXException Thrown if disConnect fails.
     */
    public abstract zXType.rc disConnect()throws ZXException;
    
    /**
     * Movenext on result set.
     * 
     * @param pobjRS
     * @return Returns the return code of commitTx.
     * @throws ZXException Thrown if RSMoveNext fails.
     */
    public abstract zXType.rc RSMoveNext(DSRS pobjRS)throws ZXException;
    
    /**
     * Close result set.
     * 
     * @param pobjRS The result set to close.
     * @return Returns the return code of RSClose.
     * @throws ZXException Thrown if RSClose fails.
     */
    public abstract zXType.rc RSClose(DSRS pobjRS)throws ZXException;
}