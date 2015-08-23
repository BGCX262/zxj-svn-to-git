/*
 * Created on Jun 5, 2005
 * $Id: PrcssMntr.java,v 1.1.2.4 2005/12/02 10:33:36 mike Exp $
 */
package org.zxframework.misc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.property.DateProperty;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.DateUtil;
import org.zxframework.util.StringUtil;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * ZX Document Type business object.
 * 
 * <pre>
 * What   : zxMisc.clsPrcssMntr
 * Who    : David Swann
 * When   : 12 November 2002
 * 
 * Change    : DGS28JAN2003
 * Why       : Added status change and pause/resume ability
 * 
 * Change    : DGS18FEB2003
 * Why       : In nextCycle, revised time comparison logic to cope with typical pause 
 * 			   window of 18:00 to 06:00 (i.e. paused overnight)
 * 
 * Change    : BD29JUL03
 * Why       : When no-retain is set on init, do not clear the pause / resume settings
 * 
 * Change    : BD19JAN04
 * Why       : - Added validation for process control
 *             - Retain is now obsolete; we always reset on start
 * 
 * Change    : TM2 DGS01MAR2005
 * Why       : Extensive revision to the logic, primarily to ensure processes in 
 * 			   'Automatic' status are started as soon as the process controller 
 * 			   is running.
 * 
 * Change    : BD5APR05 - V1.5:1
 * Why       : Added support for data sources
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PrcssMntr extends ZXBO {
	
	//-------------------- Members
	
	private static Log log = LogFactory.getLog(PrcssMntr.class);
	
	private int persistFreq;
	private boolean persistFreqTime;
	
	private Date persistedDate;
	private int persisted;
	private boolean swappedDBConnection;
	
	//--------------------- Constructors.
	
	/**
	 * Default constructor.
	 */
	public PrcssMntr() {
		super();
	}
	
	//---------- Getters/Setters
	
	/**
	 * @return Returns the persistFreq.
	 */
	public int getPersistFreq() {
		return persistFreq;
	}
	
	/**
	 * Frequency of cycle at which details are to be persisted to the DB.
	 * 
	 * @param persistFreq The persistFreq to set.
	 */
	public void setPersistFreq(int persistFreq) {
		this.persistFreq = persistFreq;
	}
	
	/**
	 * Persist Frequency is a time in seconds, not a count (true or false).
	 * 
	 * @return Returns the persistFreqTime.
	 */
	public boolean isPersistFreqTime() {
		return persistFreqTime;
	}
	
	/**
	 * @param persistFreqTime The persistFreqTime to set.
	 */
	public void setPersistFreqTime(boolean persistFreqTime) {
		this.persistFreqTime = persistFreqTime;
	}
	
	//----------------------------------- Overidden methods.
	
    /**
     * getValue - Return property for a BO.
     * 
     * @param pstrAttribute The name of the attribute to get the Property.
     * @return Returns the property of a entity by its Attribute name
     * @throws ZXException Thrown if getValue fails to get the Property.
     */
    public Property getValue(String pstrAttribute) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrAttribute", pstrAttribute);
        }
        
    	Property getValue = null;
        try {
        	
        	if (pstrAttribute.equalsIgnoreCase("errdscrhtml")) {
        		getValue = super.getValue(pstrAttribute);
        		getValue.setValue("<div class=\"zxErrorCell\">" 
        						  + getValue.getStringValue() 
        						  + "</div>");
        		
        	} else {
        		getValue = super.getValue(pstrAttribute);
        		
        	}
        	
            return getValue;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Return property for a BO ", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pstrAttribute = " + pstrAttribute);
            }
            if (getZx().throwException) throw new ZXException(e);
            
            return getValue;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getValue);
                getZx().trace.exitMethod();
            }
        }
    }
	
	/**
	 * prePersist - Allow developer to validate / massage entity before something
	 * happens.
	 * 
	 * <pre>
	 * 
	 * May replace with event actions.
	 * </pre>
	 * 
	 * @param penmPersistAction Optional, defaults to paProcess
	 * @param pstrGroup Optional, defaults to null
	 * @param pstrWhereGroup Optional defaults to null
	 * @param pstrId Optional defaults to null
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if postPersist fails
	 */
	public zXType.rc prePersist(zXType.persistAction penmPersistAction, 
	        					String pstrGroup, 
	        					String pstrWhereGroup,
	        					String pstrId)throws ZXException {
	    zXType.rc prePersist = zXType.rc.rcOK;
	    
	    /**
	     * Handle defaults :
	     */
	    if (penmPersistAction == null) {
	        penmPersistAction = zXType.persistAction.paProcess;
	    }
	    
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("penmPersistAction", penmPersistAction);
	        getZx().trace.traceParam("pstrGroup", pstrGroup);
	        getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
	        getZx().trace.traceParam("pstrId", pstrId);
	    }
	    
	    try {
	    	
	    	if (penmPersistAction.equals(zXType.persistAction.paInsert)
	    	    || penmPersistAction.equals(zXType.persistAction.paUpdate)) {
	    		
	    		/**
	    		 * Cannot kill a process when we have no id
	    		 */
	    		if (getValue("stts").longValue() == zXType.processMonitorStatus.pmsKill.pos) {
	    			if (getValue("prcssCntrlId").isNull) {
	    				getZx().trace.userErrorAdd("You cannot kill a process when no process-id is available", 
	    										   "", "prcssCntrlActn");
	    				prePersist = zXType.rc.rcError;
	    				return prePersist;
	    			}
	    		}
	    	}
	    	
	        return prePersist;
	    } catch (Exception e) {
            getZx().trace.addError("Failed to : Do pre persist action ", e);
	        if (log.isErrorEnabled()) {
	            log.error("Parameter : penmPersistAction = " + penmPersistAction);
	            log.error("Parameter : pstrGroup = " + pstrGroup);
	            log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
	            log.error("Parameter : pstrId = " + pstrId);
	        }
	        
	        if (getZx().throwException) throw new ZXException(e);
	        return prePersist;
	    } finally {
	        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	        	getZx().trace.returnValue(prePersist);
	            getZx().trace.exitMethod();
	        }
	    }
	}
	
	//-------------------- Implemented methods.
	
	/**
	 * Process is paused, manually or automatically  (true or false)
	 * DGS28JAN2003: New property added now that status can be stopped or paused
	 * 
	 * @return Returns whether the process is paused.
	 * @throws ZXException Thrown if isPaused fails.
	 */
	public boolean isPaused() throws ZXException {
		return (getValue("lstCycldStts").longValue() == zXType.processMonitorStatus.pmsPause.pos);
	}
	
	/**
	 * Initializes an instance of the process monitor.
	 * 
	 * Can optionally retain existing data to continue monitoring - otherwise resets to initial values.
	 *
	 * @param pstrPrcssId Unique ID of the process 
	 * @param pblnRetain (OBSOLETE) If True (default), retrieves existing 
	 * 					 details (if any) and continues to use them. 
	 * 					 Otherwise resets any existing values. Optional default is true.
	 * @param pstrPersistFreq Defines the persist frequency. 
	 * 						  Can be a number of cycles or a number of seconds (suffixed by s).
	 * 						  Optional default is 5s.
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if init fails. 
	 */
	public zXType.rc init(String pstrPrcssId, boolean pblnRetain, String pstrPersistFreq) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrPrcssId", pstrPrcssId);
			getZx().trace.traceParam("pblnRetain", pblnRetain);
			getZx().trace.traceParam("pstrPersistFreq", pstrPersistFreq);
		}
		
		zXType.rc init = zXType.rc.rcOK; 
		
		try {
			/**
			 * Retain is now obsolete so we never retain values
			 */
			pblnRetain = false;
			
			/**
			 * Swap to the secondary connection
			 */
			this.swappedDBConnection = false;
			swapDBConnection();
			
			boolean blnNew = false;
			
			if (setValue("id", new StringProperty(pstrPrcssId)).pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to set id in prcssMntr");
			}
			
			if (loadBO("*", "id", false).pos != zXType.rc.rcOK.pos) {
				blnNew = true;
			}
			
			/**
			 * Didn't find existing row or did but want to reset. Either way, reset values:
			 */
			if (blnNew || !pblnRetain) {
				/**
				 * When not new, do not reset all attributes as we do not want
				 * to delete the resume / pause settings
				 */
				String strGroup;
				if (blnNew) {
					strGroup = "*";
				} else {
					strGroup = "resetOnInit";
				}
				
				resetBO(strGroup);
				
				if (!blnNew) {
					setPersistStatus(zXType.persistStatus.psDirty);
				}
				
			}
			
			if (setValue("id", new StringProperty(pstrPrcssId)).pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to reset id in prcssMntr");
			}
			
			if (setValue("lstStrtd", new DateProperty(new Date(), false)).pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to set lstStrtd in prcssMntr");
			}
			
			/**
			 * DGS20JUN2003: If the status is stopped at the start of this run, set it to
			 * automatic. It makes no sense to start and immediately stop. You could set it
			 * to automatic every time, but I think it might be valid to set the status to
			 * manual or paused before starting it:
			 * TM2 DGS01MAR2005: No longer do this. Processes will now start automatically
			 * unless they are in stopped status. Must not clear a stopped job or it will
			 * immediately start again.
			 */
			
			/**
			 * Store persist frequency in this object.
			 */
			String strPersistFreq;
			if (pstrPersistFreq.endsWith("s")) {
				strPersistFreq = pstrPersistFreq.substring(0, pstrPersistFreq.length() - 1);
		        this.persistFreqTime = true;
				
			} else {
				strPersistFreq = pstrPersistFreq;
		        this.persistFreqTime = false;
			
			}
			
			if (!StringUtil.isNumeric(strPersistFreq)) {
				throw new ZXException("PersistFreq expected as numeric or numeric followed by letter s");
			}
			
			this.persistFreq = Integer.parseInt(strPersistFreq);
			
			/**
			 * Set process id
			 */
			setValue("prcssCntrlId", new LongProperty(getCurrentProcessId(), false));
			
			if (persistBO("*").pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to persist prcssMntr");
			}
			
			/**
			 * Swap back from the secondary connection
			 */
			swapDBConnection();
			
			init = zXType.rc.rcOK;
			
			return init;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Initializes an instance of the process monitor.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrPrcssId = "+ pstrPrcssId);
				log.error("Parameter : pblnRetain = "+ pblnRetain);
				log.error("Parameter : pstrPersistFreq = "+ pstrPersistFreq);
			}
			
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
	
	/**
	 * Increments the cycle number and last cycled date.
	 * 
	 * May cause details to be persisted to the DB if enough cycles have occurred.
	 *
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if nextCycle fails. 
	 */
	public zXType.rc nextCycle() throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
		}
		
		zXType.rc nextCycle = zXType.rc.rcOK; 
		
		try {
			
			/**
			 * Swap to the secondary connection
			 */
			swapDBConnection();
			
			boolean blnPersist = false;
			
			/**
			 * Set the latest cycle date to now...
			 */
			Date dtLstCycld = new Date();
			
			if (setValue("lstCycld", new DateProperty(dtLstCycld)).pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to set lstCycld in prcssMntr");
			}
			
			/**
			 * Increment the cycle number...
			 */
			int lngNumCycls = getValue("numCycls").intValue() + 1;
			
			if (setValue("numCycls", new LongProperty(lngNumCycls, false)).pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to set numCycls in prcssMntr");
			}
			
			if (this.persistFreqTime) {
				/**
				 * Persists every n seconds - see if this is long enough after the last time it was persisted:
				 */
				long dateDiff = DateUtil.datediff(DateUtil.SEC_DIFF, persistedDate, dtLstCycld);
				if (dateDiff >= this.persistFreq) {
		            blnPersist = true;
		            this.persistedDate = dtLstCycld;
				}
				
			} else {
				/**
				 * Persists every n cycles - see if enough cycles have occurred since the last cycle it was persisted:
				 */
				if (lngNumCycls - this.persisted >= this.persistFreq) {
		            blnPersist = true;
		            this.persisted = lngNumCycls;
				}
				
			}
			
			if (blnPersist) {
				/**
				 * Before persisting, see if a stop request has been made:
				 * DGS27JAN2003: Previously did this AFTER persisting. Now do it before,
				 * and get the status instead of defunct stop request boolean, and pause/
				 * resume times to determine if should be running or paused:
				 * TM2 DGS08MAR2005: If the job has a preferred controller defined, and is not
				 * running under that controller and was started more than a configured amount
				 * of time ago, ask it to stop. When it gets automatically restarted it should
				 * be picked up by the preferred controller
				 */
				if (loadBO("stts,autoPse,autoRsme,lstStrtd").pos != zXType.rc.rcOK.pos) {
					throw new ZXException("Unable to reload when cycling in prcssMntr");
				}
				
				if (getValue("stts").intValue() == zXType.processMonitorStatus.pmsAutomatic.pos) {
					if (getValue("lstCycldStts").intValue() == zXType.processMonitorStatus.pmsRun.pos 
					    && getValue("nnPrfrrdIntrvl").intValue() > 0
					    && !getValue("prfrrdId").isNull
					    && getValue("prfrrdId").compareTo(getValue("cntrllngId")) != 0
					    && DateUtil.datediff(DateUtil.SEC_DIFF, getValue("lstStrtd").dateValue(), new Date()) > getValue("nnPrfrrdIntrvl").intValue()) {
						if (setValue("lstCycldStts", new LongProperty(zXType.processMonitorStatus.pmsStop.pos, false)).pos != zXType.rc.rcOK.pos) {
							throw new ZXException("Unable to set lstCycldStts in prcssMntr to stop non-preferred controller");
						}
					} else {
						/**
						 * Here the status is automatic - if we are within the pause window, set the last cycled
						 * status to paused, otherwise to resumed:
						 * DGS18FEB2003: Revised logic to cope with typical pause window of 18:00 to 06:00
						 */
						DateFormat df = new SimpleDateFormat("hhMMss");
						Date autoPse = df.parse(df.format(getValue("autoPse").dateValue()));
						Date autoRsme = df.parse(df.format(getValue("autoRsme").dateValue()));
						Date now = df.parse(df.format(new Date()));
						
						if ((autoPse.compareTo(autoRsme) <= 0
						    && autoPse.compareTo(now) < 0
						    && autoRsme.compareTo(now) > 0)
						  ||
						  	(autoPse.compareTo(autoRsme) < 0
						     && autoPse.compareTo(now) < 0
							 && autoRsme.compareTo(now) > 0)) {
							if (setValue("lstCycldStts", new LongProperty(zXType.processMonitorStatus.pmsPause.pos, false)).pos != zXType.rc.rcOK.pos) {
								throw new ZXException("Unable to set lstCycldStts for auto pause in prcssMntr");
							}
							
						} else {
							if (setValue("lstCycldStts", new LongProperty(zXType.processMonitorStatus.pmsRun.pos, false)).pos != zXType.rc.rcOK.pos) {
								throw new ZXException("Unable to set lstCycldStts for auto resume in prcssMntr");
							}
						}
						
					}
					
				} else {
					/**
					 * Here the status is not automatic - set the last cycled status to whatever
					 * has been manually requested:
					 */
					if (setValue("lstCycldStts", getValue("stts")).pos != zXType.rc.rcOK.pos) {
						throw new ZXException("Unable to set lstCycldStts in prcssMntr");
					}
					
				}
				
				if (updateBO("lstCycld,numCycls,lstCycldStts").pos != zXType.rc.rcOK.pos) {
					throw new ZXException("Unable to update prcssMntr");
				}
				
			}
			
			/**
			 * Normally return OK, but if a stop has been requested, return a warning. Calling
			 * program must take its own action to stop itself.
			 * DGS27JAN2003: Similarly return a warning if status is now paused:
			 */
			int intProcessMonitorStatus = getValue("lstCycldStts").intValue();
			if (intProcessMonitorStatus == zXType.processMonitorStatus.pmsPause.pos
			    || intProcessMonitorStatus == zXType.processMonitorStatus.pmsStop.pos) {
				nextCycle = zXType.rc.rcWarning;
			} else {
				nextCycle = zXType.rc.rcOK;
			}
			
			/**
			 * Swap back from the secondary connection
			 */
			swapDBConnection();
			
			return nextCycle;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Increments the cycle number and last cycled date.", e);
			
			/**
			 * If necessary, swap back from the secondary connection and rollback
			 */
			if (this.swappedDBConnection) {
				swapDBConnection(false);
			}
			
			nextCycle = zXType.rc.rcError;
			
			if (getZx().throwException) throw new ZXException(e);
			return nextCycle;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(nextCycle);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Sets the error description and persists all data.
	 *
	 * @param pstrErrDscr  
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if foundError fails. 
	 */
	public zXType.rc foundError(String pstrErrDscr) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrErrDscr", pstrErrDscr);
		}
		
		zXType.rc foundError = zXType.rc.rcOK; 

		try {
			/**
			 * Swap to the secondary connection
			 */
			swapDBConnection();
			
			if (setValue("errDscr", new StringProperty(pstrErrDscr)).pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to set errDscr in prcssMntr");
			}
			
			if (setValue("numErrs", new LongProperty(getValue("numErrs").intValue(), false)).pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to increment numErrs in prcssMntr");
			}
			
			/**
			 * DGS22JAN2003: New attr date/time last error raised:
			 */
			if (setValue("lstErr", new DateProperty(new Date())).pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to set lstErr in prcssMntr");
			}
			
			if (updateBO("errDscr,numErrs,lstErr").pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to update prcssMntr");
			}
			
			/**
			 * Swap back from the secondary connection
			 */
			swapDBConnection();
			
			foundError = zXType.rc.rcOK;
			
			
			return foundError;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Sets the error description and persists all data.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrErrDscr = "+ pstrErrDscr);
			}
			
			/**
			 * If necessary, swap back from the secondary connection and rollback
			 */
			if (this.swappedDBConnection) {
				swapDBConnection(false);
			}
			foundError = zXType.rc.rcError;
			
			if (getZx().throwException) throw new ZXException(e);
			return foundError;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(foundError);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Resets the error description to null and persists all data.
	 *
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if resetError fails. 
	 */
	public zXType.rc resetError() throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
		}
		
		zXType.rc resetError = zXType.rc.rcOK; 

		try {
			/**
			 * Swap to the secondary connection
			 */
			swapDBConnection();
			
			if (setValue("errDscr", new StringProperty("", true)).pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to reset errDscr in prcssMntr");
			}
			
			if (updateBO("errDscr").pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to update prcssMntr");
			}
			
			/**
			 * Swap back from the secondary connection
			 */
			swapDBConnection();
			
			resetError = zXType.rc.rcOK;
			
			return resetError;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Resets the error description to null and persists all data.", e);
			
			/**
			 * If necessary, swap back from the secondary connection and rollback
			 */
			if (this.swappedDBConnection) {
				swapDBConnection(false);
			}
			resetError = zXType.rc.rcError;
			
			if (getZx().throwException) throw new ZXException(e);
			return resetError;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(resetError);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Records a request to change the status of this process. 
	 * 
	 * The process will be informed on return from the next call of nextCycle and must take its own action according
	 * to the status..
	 * 
	 * <pre>
	 * 
	 * DGS28JAN2003: Old stopRequest method replaced by this to support other status codes.
	 * </pre>
	 *
	 * @param penmStts  
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if statusChange fails. 
	 */
	public zXType.rc statusChange(zXType.processMonitorStatus penmStts) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("penmStts", penmStts);
		}

		zXType.rc statusChange = zXType.rc.rcOK; 
		
		try {
			/**
			 * Swap to the secondary connection
			 */
			swapDBConnection();
			
			if (setValue("stts", new LongProperty(penmStts.pos, false)).pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to set status in prcssMntr");
			}
			
			/**
			 * If action is set to 4 (statr) or 5 (kill) we need to check whether
			 * all related fields are in place
			 * TM2 DGS01MAR2005: no such status as 4 any more
			 * If penmStts = pmsKill Or penmStts = pmsStart Then
			 */
			if (penmStts.pos == zXType.processMonitorStatus.pmsKill.pos) {
				if (loadBO("prcssCntrl").pos != zXType.rc.rcOK.pos) {
					throw new ZXException("Failed to load.");
				}
				
				/**
				 * Can only kill if we have process id
				 */
				if (penmStts.pos == zXType.processMonitorStatus.pmsKill.pos
				    && getValue("prcssCntrlId").isNull) {
					return statusChange;
				}
				
				/**
				 * Can only start if we have a command set
				 * TM2 DGS01MAR2005: no such status as 4 any more
				 */
			}
			
			if (updateBO("stts").pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to persist prcssMntr");
			}
			
			/**
			 * Swap back from the secondary connection
			 */
			swapDBConnection();
			statusChange = zXType.rc.rcOK;

			return statusChange;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Records a request to change the status of this process. The process will be informed", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : penmStts = "+ penmStts);
			}
			
			/**
			 * If necessary, swap back from the secondary connection and rollback
			 */
			if (this.swappedDBConnection) {
				swapDBConnection(false);
			}
			statusChange = zXType.rc.rcError;
			
			if (getZx().throwException) throw new ZXException(e);
			return statusChange;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(statusChange);
				getZx().trace.exitMethod();
			}
		}
	}
	
	
	/**
	 * Process has been stopped. 
	 * Records last stopped date and persists latest data.
	 *
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if stopped fails. 
	 */
	public zXType.rc stopped() throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
		}
		
		zXType.rc stopped = zXType.rc.rcOK; 

		try {
			/**
			 * Swap to the secondary connection
			 */
			swapDBConnection();
			
			setValue("lstStppd", new DateProperty(new Date()));
			
			/**
			 * TM2 DGS01MAR2005: Don't set status back to Auto or it will start again - set it stopped
			 */
			
			setValue("prcssCntrlId", new LongProperty(0, true));
			setValue("lstCycldStts", new LongProperty(zXType.processMonitorStatus.pmsStop.pos, false)); // Stopped
			/**
			 * TM2 DGS01MAR2005: Clear these two request fields out now the process has stopped
			 */
			setValue("rqstdWhn", new DateProperty(new Date()));
			setValue("rqstdId", new StringProperty("", true));
			
			if (updateBO("prcssCntrlId,lstStppd,stts,lstCycldStts,rqstdWhn,rqstdId").pos != zXType.rc.rcOK.pos) {
				throw new ZXException("Unable to update prcssMntr");
			}
			
			/**
			 * Swap back from the secondary connection
			 */
			swapDBConnection();
			
			stopped = zXType.rc.rcOK;
			
			return stopped;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Process has been stopped.", e);
			
			/**
			 * If necessary, swap back from the secondary connection and rollback
			 */
			if (this.swappedDBConnection) {
				swapDBConnection(false);
			}
			stopped = zXType.rc.rcError;
			
			if (getZx().throwException) throw new ZXException(e);

			return stopped;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(stopped);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Swaps the DB connection.
	 * 
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if swapDBConnection fails. 
	 */
	public zXType.rc swapDBConnection() throws ZXException{
		return swapDBConnection(true);
	}
	
	/**
	 * Swaps the DB connection.
	 * 
	 * Also maintains a boolean that is used in error handlers.
	 * to swap it back if necessary. If swapping to the secondary 
	 * connection, starts a transaction. If swapping from,
	 * commits, unless the parameter specifies not to in which case rolls back.
	 *
	 * @param pblnCommit Whether to commit failed transaction.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if swapDBConnection fails. 
	 */
	public zXType.rc swapDBConnection(boolean pblnCommit) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pblnCommit", pblnCommit);
		}
		
		zXType.rc swapDBConnection = zXType.rc.rcOK;
		
		try {
			DSHRdbms objDSHandler = (DSHRdbms)getDS();
			
			if (this.swappedDBConnection) {
				if (pblnCommit) {
					objDSHandler.commitTx();
				} else {
					objDSHandler.rollbackTx();
				}
				
			}
			
			objDSHandler.swapConnection();
			
			if (!this.swappedDBConnection) {
				objDSHandler.beginTx();
			}
			
			swappedDBConnection = !swappedDBConnection;
			
			return swapDBConnection;
			
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Swaps the DB connection.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pblnCommit = "+ pblnCommit);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			swapDBConnection = zXType.rc.rcError;
			return swapDBConnection;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(swapDBConnection);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * @return Returns the current proccess id.
	 */
	private int getCurrentProcessId() {
		return 0;
	}
}