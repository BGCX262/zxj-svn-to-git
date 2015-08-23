/*
 * Created on Apr 14, 2005
 * $Id: DataSources.java,v 1.1.2.16 2006/07/17 16:24:42 mike Exp $
 */
package org.zxframework.datasources;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.exception.ParsingException;
import org.zxframework.util.StringUtil;

/**
 * The component that maintains all the data-sources for an application.
 * 
 * <pre>
 * Datasources are really only active for applications that require zX 1.5 or higher
 * earlier applications require clsDB. However, we initialise all database activities
 * through this object. Backward compatbility is taken care of in the init routine.
 * 
 * Change    : BD21APR05 - V1.5:5
 * Why       : Added central transaction management
 * 
 * Change    : BD21APR05 - V1.5:24
 * Why       : You can join a BO with datasource '' with a BO with datasource
 *             'primary'; see canDoDBJoin
 *              
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 1.5
 * @since 1.5
 */
public class DataSources extends ZXObject {
    
    /** Handle to the primary datasource which has to be a DSHRdbms. */
    private DSHRdbms primary;
    
    /** Collection of defined datasources */
    private Map dataSources;
    
    private TxHandlers txHandlers;
    
    // TEMP FIX : The xml node witht the datasources xml configuration.
    private Element configElement;
    
    //------------------------------- Constructors.
    
    /**
     * Hide Default constructor.
     * @deprecated Rather use the new xml config settings.
     */
    public DataSources() {
    	super();
    }
    
    /**
     * Create a DataSources with the xml node for the datasource settings.
     * 
     * @param pobjElement The config xml element.
     */
    public DataSources(Element pobjElement){
        this.configElement = pobjElement;
        
        // Make sure that the Txhandler is initialized.
        this.txHandlers = new TxHandlers();
    }
    
    /**
    * @return Returns the return code of the method. 
    * @throws ZXException Thrown if init fails. 
    */
    public zXType.rc init() throws ZXException{
        zXType.rc init = zXType.rc.rcOK; 
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        try {
            
            // Parsing part has already been handled.
            
            DSHandler objDatasource; // Loop over handlers to assign tx handler
            TXHandler objTxHandler;  // Loop over tx handlers to assign to datasource
            
            /**
             * Initialise txHandlers and assign the appropriate txHandler to 
             * each data source
             */
            Iterator iter = this.dataSources.values().iterator();
            while (iter.hasNext()) {
                objDatasource = (DSHandler)iter.next();
                
                /**
                 * If a tx handler had explicitly been defined for this data source handler, we
                 * have to get it from the collection of tx handlers and assign; otherwise we
                 * assume that the handler takes care of its own tx handling
                 */
                if (StringUtil.len(objDatasource.getTxHandlerName()) > 0) {
                    objTxHandler = this.txHandlers.getHandlerByName(objDatasource.getTxHandlerName());
                    
                    if (objTxHandler == null) {
                        throw new ZXException("Cannot find tx handler specified for data source"
                                              , "Data source " + objDatasource.getName() 
                                                + " - TX handler " + objDatasource.getTxHandlerName());
                    } // No tx handler found
                    
                    objDatasource.setTxHandler(objTxHandler);
                    
                } // Has tx handler specified ?
                
            } // Loop over datasources to resolve tx handler
            
            return init;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Execute init", e);
            if (getZx().throwException) throw new ZXException(e);
            init = zXType.rc.rcError;
            return init;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(init);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //--------------------------- Getters and Setters.
    
    /**
     * @return Returns the primary.
     */
    public DSHRdbms getPrimary() {
        return primary;
    }
    
    /**
     * @param primary The primary to set.
     */
    public void setPrimary(DSHRdbms primary) {
        this.primary = primary;
    }
    
    /**
     * @return Returns the dataSources.
     */
    public Map getDataSources() {
        return dataSources;
    }
    
    /**
     * @param dataSources The dataSources to set.
     */
    public void setDataSources(Map dataSources) {
        this.dataSources = dataSources;
    }
    
    /**
     * @return Returns the txHandlers.
     */
    public TxHandlers getTxHandlers() {
        return txHandlers;
    }
    
    /**
     * @param txHandlers The txHandlers to set.
     */
    public void setTxHandlers(TxHandlers txHandlers) {
        this.txHandlers = txHandlers;
    }
    
    //----------------------------- Parsing Methods.
    /**
     * Parses the database settings for the framework. 
     * 
     * <pre>
     * 
     * This is used because the configValue is to slow and these variables 
     * are used quite often, unfortunately these values need to be stored in the ZX objects.
     * 
     * NOTE : The timeformat/dateFormat etc.. are using all over the place so this 
     * needs to be called early in the zx init.
     * </pre>
     * 
     * @param pobjElement The element containing the database settings
     * @throws Exception Thrown if the parsing fails.
     */
    public void parseOldConfig(Element pobjElement) throws Exception {
        
        this.primary = new DSHRdbms();
        initHandler(this.primary, "primary", zXType.dsType.dstPrimary);
        
        String elementName;
        Element element;
        Iterator iter = pobjElement.getChildren().iterator();
        while (iter.hasNext()) {
            element = (Element)iter.next();
            
            elementName = element.getName().intern();
            
            if (elementName == "type") {
                /**
                 * Get database type
                 */
                this.primary.setDbType(zXType.databaseType.getEnum(element.getText()));
                if(this.primary.getDbType() == null) {
                    throw new Exception("Unknown / unsupported database type : " + element.getText());
                }
                
            } else if (elementName == "timeout") {
                /**
                 * Get timeout value
                 */
                this.primary.setTimeOut(new Integer(element.getText()).intValue());
                
            } else if (elementName == "dateFormat") {
                /**
                 * Get date format
                 */
                this.primary.setDateFormat(element.getText());
                
            } else if (elementName == "timeFormat") {
                /**
                 * Get time format
                 */
                this.primary.setTimeFormat(element.getText());
                
            } else if (elementName == "timestampFormat") {
                /**
                 * Get time format
                 */
                this.primary.setTimestampFormat(element.getText());
                
            } else if (elementName == "datasource") {
                /**
                 * Get DSN and try to connect to database
                 */               
                this.primary.setDsn(element.getText());
                
            } else if (elementName == "jdbcdriver") {
                /**
                 * Get the jdbc driver to load for test mode.
                 */               
                this.primary.setJdbcdriver(element.getText());
                
            } else if (elementName == "jdbcurl") {
                /**
                 * The jdbc url used to connect to the db
                 */               
                this.primary.setJdbcurl(element.getText());
                
            } else if (elementName == "username") {
                /**
                 * Username
                 */               
                this.primary.setUsername(element.getText());
                
            } else if (elementName == "password") {
                /**
                 * xxxx
                 */               
                this.primary.setPassword(element.getText());
            }
        }        
    }
    
    /**
     * Parse the datasource config settings.
     * 
     * @throws Exception Thrown if parseConfig fails.
     */
    public void parseConfig() throws Exception {
        Element element;
        
        Iterator iter = configElement.getChildren().iterator();
        while (iter.hasNext()) {
            element = (Element)iter.next();
            
            /**
             * Create new handler based on the details provided
             */
            if (createHandler(element).pos != zXType.rc.rcOK.pos) {
                throw new RuntimeException("Unable to create new data-source handler : " + element);
            }
            
        } // Loop over all data sources
        
    }
    
	/**
	* Set all values for a handler to their logical default.
	*
	* @param pobjHandler The datasource handler to setup.
	* @param pstrName The name of the datasource handler.
	* @param penmDsType The dsType.
	* @return Returns the return code of the method. 
	* @throws ZXException Thrown if initHandler fails. 
	*/
	public zXType.rc initHandler(DSHandler pobjHandler, String pstrName, zXType.dsType penmDsType) throws ZXException{
		zXType.rc initHandler = zXType.rc.rcOK; 
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjHandler", pobjHandler);
			getZx().trace.traceParam("pstrName", pstrName);
			getZx().trace.traceParam("penmDsType", penmDsType);
		}

		try {
		    pobjHandler.setName(pstrName);
            pobjHandler.setDsType(penmDsType);
            pobjHandler.setState(zXType.dsState.dssUnused);
            
            if (penmDsType.pos == zXType.dsType.dstChannel.pos) {
                pobjHandler.setOrderSupport(zXType.dsOrderSupport.dsosSimple);
                pobjHandler.setOrderGroup("+");
                
                pobjHandler.setSearchSupport(zXType.dsSearchSupport.dsssSimple);
                pobjHandler.setSearchGroup("+");
                
                pobjHandler.setRsType(zXType.dsRSType.dsrstCollection);
                
            } else {
                pobjHandler.setOrderSupport(zXType.dsOrderSupport.dsosFull);
                pobjHandler.setOrderGroup("*");
                
                pobjHandler.setSearchSupport(zXType.dsSearchSupport.dsssFull);
                pobjHandler.setSearchGroup("*");
                
                pobjHandler.setRsType(zXType.dsRSType.dsrstRS);
                
            } // Channel or database
            
            pobjHandler.setSupportsDelete(true);
            pobjHandler.setSupportsInsert(true);
            pobjHandler.setSupportsLoad(true);
            pobjHandler.setSupportsUpdate(true);
            
            pobjHandler.setTxSupport(zXType.dsTxSupport.dstxsLocal);
            
			return initHandler;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Set all values for a handler to their logical default.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjHandler = "+ pobjHandler);
				getZx().log.error("Parameter : pstrName = "+ pstrName);
				getZx().log.error("Parameter : penmDsType = "+ penmDsType);
			}
			if (getZx().throwException) throw new ZXException(e);
			initHandler = zXType.rc.rcError;
			return initHandler;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(initHandler);
				getZx().trace.exitMethod();
			}
		}
	}
    
    /**
     * Get handler for data sources collection by name.
     * 
     * @param pstrName The name of the datasource handler.
     * @return Returns a handle DSHandler.
     */
    public DSHandler getDSByName(String pstrName) {
        return getDSByName(pstrName, true);
    }
    
    /**
     * Get handler for data sources collection by name.
     * 
     * @param pstrName The name of the datasource handler.
     * @param pblnActivate Whether to activate that handler. Optional, default should be true.
     * @return Returns a handle DSHandler.
     */
    public DSHandler getDSByName(String pstrName, boolean pblnActivate) {
        DSHandler getDSByName = null;
        
        /**
         * Empty name means 'primary'
         */
        if (StringUtil.len(pstrName) == 0) {
            getDSByName = this.primary;
        } else {
            getDSByName = (DSHandler)this.dataSources.get(pstrName);
        }
        
        if (getDSByName == null) {
            throw new RuntimeException("Unable to retrieve data source by name : " + pstrName);
        }
        
        /**
         * See if we need to active
         */
        if (pblnActivate) {
            int intState = getDSByName.getState().pos;
            if (intState == zXType.dsState.dssActive.pos) {
                /**
                 * Nothing to do, already active
                 */
                
            } else if (intState == zXType.dsState.dssClosed.pos || intState == zXType.dsState.dssUnused.pos) {
                /**
                 * Connect
                 */
            	try {
	                if (getDSByName.connect().pos != zXType.rc.rcOK.pos) {
	                    throw new RuntimeException("Unable to connect to channel : " + getDSByName.getName());
	                }
            	} catch (Exception e) {
            		throw new NestableRuntimeException("Unable to connect to channel : " + getDSByName.getName(), e);
            	}
                
            } else if (intState == zXType.dsState.dssError.pos){
                /**
                 * Connect
                 */
            	try {
	                if (getDSByName.connect().pos != zXType.rc.rcOK.pos) {
	                	throw new RuntimeException("Cannot connect to channel as state is 'error' : " + getDSByName.getName());
	                }
            	} catch (Exception e) {
            		throw new NestableRuntimeException("Cannot connect to channel as state is 'error' : " + getDSByName.getName(), e);
            	}
            }
            
        } // Need to activate?
        
        return getDSByName;
    }
    
    /**
     * Get the data source handler (in activated state or not) for this BO.
     * 
     * @param pobjBO The business object with a handle to the dshandler.
     * @return Returns the DSHandle for a specific business object.
     */
    public DSHandler getDS(ZXBO pobjBO) {
        return getDS(pobjBO, true);
    }
    
    /**
     * Get the data source handler (in activated state or not) for this BO.
     * 
     * @param pobjBO The business object with a handle to the dshandler.
     * @param pblnActivate Optional, default is true.
     * @return Returns the DSHandle for a specific business object.
     */
    public DSHandler getDS(ZXBO pobjBO, boolean pblnActivate) {
        /**
         * If no data source has been set explicitly, simply assume primary
         */
        if (StringUtil.len(pobjBO.getDescriptor().getDataSource()) == 0) {
            return getPrimary();
        }
        
        return getDSByName(pobjBO.getDescriptor().getDataSource(), pblnActivate);
    }
    
    /**
     * Get DS handler but insist that it is of data-source
     * type primary or alternative.
     * 
     * @param pobjBO The business object to get the dshandler
     * @return Returns the rdbms handler.
     */
    public DSHRdbms getRdbmsDS(ZXBO pobjBO) {
        return (DSHRdbms)getDS(pobjBO);
    }
    
	/**
	* Create a new handler based on the details provided in the XML Node.
    * 
	* jclass - Is used install of class as class is already used in the COM+ version.
    * 
	* @param pobjElement The xml node containing a datasource setting 
	* @return Returns the return code of the method. 
	* @throws ParsingException Thrown if createHandler fails. 
	*/
	public zXType.rc createHandler(Element pobjElement) throws ParsingException {
		zXType.rc createHandler = zXType.rc.rcOK; 
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjElement", pobjElement);
		}

		try {
            String strType = pobjElement.getAttributeValue("type").toLowerCase();
            zXType.dsType enmType = zXType.dsType.getEnum(strType);
            int intType = enmType.pos;
            
            String strName = pobjElement.getAttributeValue("name");
            String strClass = "";
            
            if (intType == zXType.dsType.dstPrimary.pos) {
                /**
                 * Can only have one primary type
                 */
                if (this.primary != null) {
                    throw new ZXException("Found multiple data sources of type primary");
                } // Multiple primary sourced found
                
                /**
                 * Name MUST be primary if available 
                 */
                if (strName == null || !strName.equalsIgnoreCase("primary")) {
                    throw new ZXException("Name for primary data-source must be 'primary'", 
                                          strName);
                } // Name provided but incorrect
                
                /**
                 * Class MUST be zX.clsDSHRdbms
                 */
                strClass = DSHRdbms.class.getName();
                
                String strTmp = pobjElement.getAttributeValue("jclass");
                if (strTmp == null || !strTmp.equals(strClass)) {
                    throw new ZXException("Class for primary data-source must be '" 
                                          + strClass + "'", strTmp);
                } // Class provided but incorrect
                
                strName = "primary";
                
            } else if (intType == zXType.dsType.dstAlternative.pos) {
                /**
                 * Name MUST be provided
                 */
                if (strName == null) {
                    throw new ZXException("No name provided for alternative data source", 
                                          pobjElement.toString());
                } // Name must be provided
                
                /**
                 * Class MUST be zX.clsDSHRdbms
                 */
                strClass = DSHRdbms.class.getName();
                String strTmp = pobjElement.getAttributeValue("jclass");
                if (strTmp == null || !strTmp.equals(strClass)) {
                    throw new ZXException("Class for alternative data-sources must be 'zX.clsDSHRdbms'", 
                                          strTmp);
                } // Class provided but incorrect
                
            } else if (intType == zXType.dsType.dstChannel.pos) {
                /**
                 * Channel
                 */
                /**
                 * Name MUST be provided
                 */
                if (strName == null) {
                    throw new ZXException("No name provided for alternative data source", 
                                          pobjElement.toString());
                } // Name must be provided
                
                /**
                 * Class MUST be provided
                 */
                strClass = pobjElement.getAttributeValue("jclass");
                if (strClass == null) {
                    throw new ZXException("Class for channel data-sources must be provided", 
                                          strName);
                } // Class not provided
                
            }
            
            /**
             * Any data source but the primary may use the 'useHandler' feature. This is designed for
             * development / test / emulation purposes. You can specify a handler in the configuration file
             * but use the 'useHandler' feature to say that actually you want to use a different handler.
             * This can be used in places where a handler cannot be available, for example because your
             * developers laptop does not connect to the mainframe that is required by the true handler.
             * Using this feature you can develop / testu using a more standard handler with no or a minimal
             * impact on your application or configuration file.
             * Notes:
             * 
             * 1. That the handler we want to use must already have been declared and cannot have the
             *    useHandler feature set itself and that we use all the capabilities for this data source
             * 
             * 2. We do NOT support the useHandler feature when in production mode as it is designed for test
             *    and development mode only
             * 
             *  3. We use all capabilities of the original data source so that users see the limitations of that
             *     in the application with one exception: rsType: this is so low-level technical that it can
             *     cause a real problem if a handler claims to have rsType stream but actually has rsType rs
             * 
             */
            String strUseHandler = null;
            if (intType != zXType.dsType.dstPrimary.pos) {
                strUseHandler = pobjElement.getAttributeValue("usehandler");
            } // UseHandler feature not supported for primary
            
            /**
             * If a useHandler has been specified: get this
             */
            DSHandler objHandler;
            if (StringUtil.len(strUseHandler) > 0 ) {
                /**
                 * Retrieve the handler
                 */
                objHandler = getDSByName(strUseHandler, false);
                if (objHandler == null) {
                    throw new ZXException("Cannot find handler that 'useHandler' feature refers to", 
                                          strUseHandler);
                }
                
                /**
                 * Used handler cannot be itself a useHandler
                 */
                if (StringUtil.len(objHandler.getUseHandler()) > 0) {
                    /**
                     * TODO : Nothing is happening here yet.
                     */
                }
                
                if (getZx().log.isInfoEnabled()) {
                    getZx().log.info("Use handler feature used for " 
                                     + strName + " (uses " + strUseHandler + ")");
                }
                
            } else {
                /**
                 * Create the handler
                 */
                objHandler = (DSHandler)getZx().createObject(strClass);
                
                initHandler(objHandler, strName, enmType);
                
            } // Get handler
            
            /**
             * Loop over the common data source handler tags
             */
            Element element;
            String elementName;
            String strTmp;
            
            List colElement = pobjElement.getChildren();
            int size = colElement.size();
            for (int i = 0; i < size; i++) {
                element = (Element)colElement.get(i);
                elementName = element.getName();
                
                if (elementName.equalsIgnoreCase("ordersupport")) {
                    strTmp = element.getText().toLowerCase();
                    objHandler.setOrderSupport(zXType.dsOrderSupport.getEnum(strTmp));
                    
                } else if (elementName.equalsIgnoreCase("txhandler")) {
                    objHandler.setTxHandlerName(element.getText());
                    
                } else if (elementName.equalsIgnoreCase("ordergroup")) {
                    objHandler.setOrderGroup(element.getText());
                    
                } else if (elementName.equalsIgnoreCase("searchsupport")) {
                    strTmp = element.getText().toLowerCase();
                    objHandler.setSearchSupport(zXType.dsSearchSupport.getEnum(strTmp));
                    
                } else if (elementName.equalsIgnoreCase("searchgroup")) {
                    objHandler.setSearchGroup(element.getText());
                    
                } else if (elementName.equalsIgnoreCase("txsupport")) {
                    strTmp = element.getText().toLowerCase();
                    objHandler.setTxSupport(zXType.dsTxSupport.getEnum(strTmp));
                    
                } else if (elementName.equalsIgnoreCase("rstype")) {
                    /**
                     * Note that rsType is NOT used in case of a use-handler; see comments above for reasons
                     */
                    if (StringUtil.len(strUseHandler) > 0) {
                        strTmp = element.getText().toLowerCase();
                        objHandler.setRsType(zXType.dsRSType.getEnum(strTmp));
                    } // Use handler?
                    
                } else if (elementName.equalsIgnoreCase("supportsdelete")) {
                    objHandler.setSupportsDelete(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName.equalsIgnoreCase("supportsinsert")) {
                    objHandler.setSupportsInsert(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName.equalsIgnoreCase("supportsupdate")) {
                    objHandler.setSupportsUpdate(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName.equalsIgnoreCase("supportsload")) {
                    objHandler.setSupportsLoad(StringUtil.booleanValue(element.getText()));
                    
                /**
                 * Get date format for database persistence.
                 */
                } else if (elementName.equalsIgnoreCase("dateFormat")) {
                    objHandler.setDateFormat(element.getText());
                } else if (elementName.equalsIgnoreCase("timeFormat")) {
                    objHandler.setTimeFormat(element.getText());
                } else if (elementName.equalsIgnoreCase("timestampFormat")) {
                    objHandler.setTimestampFormat(element.getText());
                }
                
            } // Loop over tags
            
            /**
             * And initialise the handler if it is not a useHandler (otherwise it was already initialised)
             */
            if (StringUtil.len(strUseHandler) == 0) {
                if (objHandler.parse(pobjElement).pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to initialise handler for data-source", 
                                          strName + " (" + strClass + ")");
                }
                
            } // Use handler?
            
            /**
             * If the primary, also set the primary handle and connect
             */
            if (intType == zXType.dsType.dstPrimary.pos) {
                this.primary = (DSHRdbms)objHandler;
                if (this.primary.connect().pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to connect to primary data source");
                }
            }
            
            // Finally ..
            
            /**
             * Add to collection (will fail if name is not unique)
             */
            if (this.dataSources == null) this.dataSources = new ZXCollection();
            this.dataSources.put(strName, objHandler);
            
			return createHandler;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Create a new handler based on the details provided in the XML Node.", e);
            
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjElement = "+ pobjElement);
			}
			if (getZx().throwException) throw new ParsingException(e);
			createHandler = zXType.rc.rcError;
			return createHandler;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(createHandler);
				getZx().trace.exitMethod();
			}
		}
	}
    
    /**
     * Can these two BOs be included in a single join.
     * 
     * <pre>
     * 
     * V1.5:24 - Fixed
     * </pre>
     * 
     * @param pobjBO1 The left bo.
     * @param pobjBO2 The right bo.
     * @return Returns true if we can do a join.
     */
    public boolean canDoDBJoin(ZXBO pobjBO1, ZXBO pobjBO2) {
        boolean canDoDBJoin = false;
        
        /**
         * We can include two BOs in a join when:
         * 1. They both share the same data source (note that primary and '' are the same)
         * 2. The data source is either primary or alternative (ie not a channel)
         */
        String strDS1 = pobjBO1.getDescriptor().getDataSource();
        if (StringUtil.len(strDS1) == 0) strDS1 = "primary";
        String strDS2 = pobjBO2.getDescriptor().getDataSource();
        if (StringUtil.len(strDS2) == 0) strDS2 = "primary";
        
        if (strDS1.equalsIgnoreCase(strDS2)) {
            if (getDS(pobjBO1, false).getDsType().pos == zXType.dsType.dstChannel.pos){
                // So channels can never have joins? Pity.
                canDoDBJoin = false;
            } else {
                // They are either both Alternative or DB.
                canDoDBJoin = true;
            }
            
        } else {
            canDoDBJoin = false;
        }
        
        return canDoDBJoin;
    }
    
    //------------------------------ Util methods.
    
    /**
     * Disconnect all of the datasources.
     * 
     * @return Returns the return code of the method.
     */
    public zXType.rc disConnect() {
        zXType.rc disConnect = zXType.rc.rcOK;
        
        DSHandler objDSHandler;
        Iterator iter = this.dataSources.values().iterator();
        while (iter.hasNext()) {
            objDSHandler = (DSHandler)iter.next();
            
            try {
                objDSHandler.disConnect();
            } catch (Exception e) {
                // This may fail, but we want to carry on.
                getZx().log.error("Failed disconnect dshandle : " + objDSHandler.getName(), e);
            }
            
        }
        return disConnect;
    }
}