/*
 * Created on Jun 3, 2005
 * $Id: DSHttpSession.java,v 1.1.2.6 2006/07/17 13:48:38 mike Exp $
 */
package org.zxframework.datasources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.jdom.Element;

import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.exception.ParsingException;
import org.zxframework.transaction.FakeTransaction;
import org.zxframework.transaction.Transaction;

/**
 * Data Source Handler for HttpSession.
 * 
 * NOTE : As this is used to store session data performance is more important than functionality
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class DSHttpSession extends DSHandler {
	
	//------------------------ Members
	
	private HttpSession session;
	
	//------------------------ Constants
	
	/** The name space into with to store the session data. */
	private static final String schema = "SSNDTA.";
	/** The name of the handle to the session. */
	public static final String SSN = "HTTPSESSION";
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public DSHttpSession() {
		super();
	}
	
	//------------------------ Getters and Setters
	
	/**
	 * @return Returns the session.
	 */
	public HttpSession getSession() {
		/**
		 * Get a handle to the current HttpSession using a tag.
		 */
		if (this.session == null) {
			this.session = (HttpSession)getZx().getSettings().getTags().get(SSN);
		}
		
		return this.session;
	}
	
	/**
	 * @param session The session to set.
	 */
	public void setSession(HttpSession session) {
		this.session = session;
	}
	
	//------------------------ DSHandler methods.
	
	/**
	 * Parse HttpSession Handler specific settings.
	 * 
	 * @see org.zxframework.datasources.DSHandler#parse(Element)
	 */
	public zXType.rc parse(Element pobjElement) throws ParsingException {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#loadBO(ZXBO, String, String, boolean, String)
	 */
	public zXType.rc loadBO(ZXBO pobjBO, 
							String pstrLoadGroup, 
							String pstrWhereGroup,
							boolean pblnResolveFK, 
							String pstrOrderByGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrLoadGroup", pstrLoadGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
        }
        
        zXType.rc loadBO = zXType.rc.rcOK;
        
        try {
        	
            if (!pobjBO.noTable()) {
	        	Map colBOS = (Map)getSession().getAttribute(schema + pobjBO.getDescriptor().getTable());
	        	if (colBOS != null) {
	        		// NOTE : We only support load by PK for performance reasons.
	        		ZXBO objBOTmp = (ZXBO)colBOS.get(pobjBO.getPKValue().getStringValue());
	        		if (objBOTmp != null) {
	        			objBOTmp.bo2bo(pobjBO, "*", false);
	        			
	        			// Set persistStatus to clean as we have just freshly loaded it.
	        			pobjBO.setPersistStatus(zXType.persistStatus.psClean);
	        			loadBO = zXType.rc.rcOK;
	        			
	        		} else {
	        			loadBO = zXType.rc.rcWarning;
	        		}
	        		
	        	} else {
	        		getSession().setAttribute(schema + pobjBO.getDescriptor().getTable(), new LinkedHashMap());
	        		loadBO = zXType.rc.rcWarning;
	        		
	        	}
	        	
            }  // Dont bother for BOs without a table
            
        	return loadBO;
        	
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(loadBO);
                getZx().trace.exitMethod();
            }
        }
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#insertBO(ZXBO)
	 */
	public zXType.rc insertBO(ZXBO pobjBO) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc insertBO = zXType.rc.rcOK;
        
        try {
        	
        	if(!pobjBO.noTable()) {
        		Map colBOS = (Map)getSession().getAttribute(schema + pobjBO.getDescriptor().getTable());
        		if (colBOS == null) {
        			colBOS = new LinkedHashMap();
        		}
        		
        		if (colBOS.get(pobjBO.getPKValue().getStringValue()) == null) {
	        		colBOS.put(pobjBO.getPKValue().getStringValue(), 
	        				   pobjBO.cloneBO("*", false, false));
	        		
	        		getSession().setAttribute(schema + pobjBO.getDescriptor().getTable(), colBOS);
        		} else {
        			// Trying to override existing entry.
        			insertBO = zXType.rc.rcError;
        		}

        	}  // Dont bother for BOs without a table
        	
    		return insertBO;
    		
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(insertBO);
                getZx().trace.exitMethod();
            }
        }
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#updateBO(ZXBO, String, String)
	 */
	public zXType.rc updateBO(ZXBO pobjBO, 
							  String pstrUpdateGroup,
							  String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrUpdateGroup", pstrUpdateGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }
        
        zXType.rc updateBO = zXType.rc.rcOK;
        
        try {
        	
        	if(!pobjBO.noTable()) {
            	Map colBOS = (Map)getSession().getAttribute(schema + pobjBO.getDescriptor().getTable());
            	if (colBOS != null) {
            		// NOTE : We only support update by PK for performance reasons.
            		ZXBO objBOTmp = (ZXBO)colBOS.put(pobjBO.getPKValue().getStringValue(), 
            										 pobjBO.cloneBO("*", false, false));
            		if (objBOTmp != null) {
            			getSession().setAttribute(schema + pobjBO.getDescriptor().getTable(), colBOS);
            			updateBO = zXType.rc.rcOK;
            			
            		} else {
            			// We could not find a matching entry.
            			colBOS.remove(pobjBO.getPKValue().getStringValue());
            			updateBO = zXType.rc.rcWarning;
            			
            		}
            		
            	} else {
            		getSession().setAttribute(schema + pobjBO.getDescriptor().getTable(), new LinkedHashMap());
            		updateBO = zXType.rc.rcWarning;
            	}
            	
        	} // Dont bother with BOs without a "table"
        	
    		return updateBO;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(updateBO);
                getZx().trace.exitMethod();
            }
        }
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#deleteBO(ZXBO, String)
	 */
	public zXType.rc deleteBO(ZXBO pobjBO, String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }
        
        zXType.rc deleteBO = zXType.rc.rcOK;
        
        try {
        	
            if (!pobjBO.noTable()) {
	        	Map colBOS = (Map)getSession().getAttribute(schema + pobjBO.getDescriptor().getTable());
	        	if (colBOS != null) {   
	        		ZXBO objBOTmp = (ZXBO)colBOS.get(pobjBO.getPKValue().getStringValue());
	        		if (objBOTmp != null) {
	                    // Load conditions
	                    DSWhereClause objDSWhereClause = new DSWhereClause();
	                    objDSWhereClause.parse(pobjBO, pstrWhereGroup);
	                    
	                    // Only delete if we have a match.
	        			if (getZx().getBos().isMatchingBO(objBOTmp, objDSWhereClause)) {
	        				colBOS.remove(pobjBO.getPKValue().getStringValue());
	        			}
	        			
	        			getSession().setAttribute(schema + pobjBO.getDescriptor().getTable(), colBOS);
	        			deleteBO = zXType.rc.rcOK;
	        		} else {
	        			deleteBO = zXType.rc.rcWarning;
	        		}
	        		
	        	} else {
	        		getSession().setAttribute(schema + pobjBO.getDescriptor().getTable(), 
	        								  new LinkedHashMap());
	        		deleteBO = zXType.rc.rcWarning;
	        	}
	        	
            }  // Dont bother with BOs without a "table"
            
        	return deleteBO;
        	
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Delete BO", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrWhereAttributeGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
            deleteBO = zXType.rc.rcError;
            return deleteBO;
            
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(deleteBO);
                getZx().trace.exitMethod();
            }
        }
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
		DSRS boRS = null;
		
		try {
			List values = new ArrayList();
			boRS = new DSRS(this);
			
        	Map colBOS = (Map)getSession().getAttribute(schema + pobjBO.getDescriptor().getTable());
        	if (colBOS != null) {
                // Load conditions
                DSWhereClause objDSWhereClause = new DSWhereClause();
                objDSWhereClause.parse(pobjBO, pstrWhereGroup);
                
                ZXBO objBO;
                int pos = 1;
                int readRows = 1;
                
        		Iterator iter = colBOS.values().iterator();
        		while (iter.hasNext()) {
        			objBO = ((ZXBO)iter.next()).cloneBO("*", false, false);
        			
        			if (getZx().getBos().isMatchingBO(objBO, objDSWhereClause)) {
                        /**
                         * Seek to the correct position
                         */
                        if (plngStartRow == 0 || (pos >= plngStartRow)) {
                        	// Set the persist status to clean as we have just loaded it.
                        	objBO.setPersistStatus(zXType.persistStatus.psClean);
                            values.add(objBO);
                            
                            /**
                             * Batch size
                             */
                            if (plngBatchSize > 0) {
                                /**
                                 * Check if number of rows adds are within
                                 * the batch size.
                                 */
                                if (readRows >= plngBatchSize) {
                                    break;
                                }
                                readRows++;
                            }
                            
                        }
                        pos++;
                    }
        		}
        		
        	} else {
        		getSession().setAttribute(schema + pobjBO.getDescriptor().getTable(), 
        								  new LinkedHashMap());
        		
        	}
        	
            boRS.setData(values);
            boRS.setDataCursor(0);
            
            return boRS;
           
		} catch (Exception e) {
            /**
             * Close resultset.
             */
            if (boRS != null) boRS.RSClose();
            
            if (e instanceof ZXException) {
                throw (ZXException)e;
            }
            
            throw new ZXException("Failed to execute boRS.", e);
            
		} finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
		}
	}

	//------------------------ Not implemented for HttpSession.
	
	/**
	 * @see org.zxframework.datasources.DSHandler#beginTransaction()
	 */
	public Transaction beginTransaction() throws ZXException {
		// Use a dummy transaction handler.
		return new FakeTransaction();
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#beginTx()
	 */
	public zXType.rc beginTx() throws ZXException {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#inTx()
	 */
	public boolean inTx() throws ZXException {
		return false;
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#rollbackTx()
	 */
	public zXType.rc rollbackTx() throws ZXException {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#commitTx()
	 */
	public zXType.rc commitTx() throws ZXException {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#connect()
	 */
	public zXType.rc connect() throws ZXException {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#disConnect()
	 */
	public zXType.rc disConnect() throws ZXException {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#RSMoveNext(org.zxframework.datasources.DSRS)
	 */
	public zXType.rc RSMoveNext(DSRS pobjRS) throws ZXException {
		return zXType.rc.rcOK;
	}

	/**
	 * @see org.zxframework.datasources.DSHandler#RSClose(org.zxframework.datasources.DSRS)
	 */
	public zXType.rc RSClose(DSRS pobjRS) throws ZXException {
		return zXType.rc.rcOK;
	}
}