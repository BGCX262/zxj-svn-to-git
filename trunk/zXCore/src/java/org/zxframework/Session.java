/*
 * Created on Feb 20, 2004
 * $Id: Session.java,v 1.1.2.39 2005/12/02 10:44:21 mike Exp $
 */
package org.zxframework;

import java.io.StringReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import org.zxframework.datasources.DSHandler;
import org.zxframework.exception.NestableException;
import org.zxframework.transaction.Transaction;
import org.zxframework.util.StringUtil;

/**
 * Session - zX framework session object.
 * 
 * <p>
 * The ZX Session object is a regular zX Business object with some of the 
 * default behaviour overridden. And there are some added optimizations for the live
 * environemnt. The Session object is used to store details for a logged in user for the
 * duration that he is logged in.
 * </p>
 * 
 * <p>
 * Here is a list of fixed attributes in the ZX Session object :
 * </p>
 * 
 * <p>
 *  <code>id</code> - Is the Session ID.<br/>
 *  <code>hshId</code> - Is the Hashed Session id of the object.<br/> 
 *  <code>usrPrf</code> - The name of the user logged in.<br/>
 *  <code>usrPrflXML</code> - Is the XML of the user profile as a BO2XML representation.
 * </p>
 * 
 * <pre><code>
 * &lt;BO>
 * 	&lt;entity>zxUsrPrf&lt;/entity>
 * 	&lt;attr>
 * 		&lt;Id>DAN&lt;/Id>
 * 		&lt;Nme>Dan Simons&lt;/Nme>
 * 		&lt;prntr1>DEFAULT&lt;/prntr1>
 * 		&lt;prntr2>&lt;/prntr2>
 * 		&lt;usrCode>D&lt;/usrCode>
 * 	&lt;/attr>
 * &lt;/BO>
 * </code></pre>
 * 
 * <p>
 * <code>cntxt</code> - Stores the XML session information. The data is stored as name value pairs
 * </p>
 * 
 * <pre><code>
 * &lt;c>
 * 	&lt;name>&lt;![CDATA[value]]>&lt;/name>
 * &lt;/c>
 * </code></pre>
 * 
 * <p>
 *  <code>lstUse</code> - The date the last time the session data was used.<br/>
 *  <code>usrGrps</code> - The User Groups the logged in user belongs to.<br/>
 *  <code>lngth</code> - THe number of character in the cntxt session data.<br/>
 *  <code>lngg</code> - The language of the logged in user.<br/>
 * </p>
 * 
 * <p>
 * The session can stored in the database or in the HttpSession.
 * </p>
 * 
 * <pre>
 * <b>HttpSession :</b>
 * Pros :
 * - No xml overhead (If storing the entire business object)
 * - Stored in memory and therefore alot faster.
 * 
 * Cons : 
 * - Can not be distrubuted accross non-websphere applications. ie : com+
 * - Can use alot of memory.
 * 
 * <b>Database :</b>
 * Pros :
 * - Can be used accross multiple applications
 * - You can keep your session if the server is restarted or crashes.
 * 
 * Cons :
 * - Poor performance.
 * </pre>
 * 
 * <p>
 * <b>NOTE :</b> The handling of this session can be improved!. Need to look into how to 
 * improve the xml generation and parsing etc..
 * </p>
 * <p>
 * <b>NOTE :</b> We may replace the HttpSession optimisation with a Caching optimisation.
 * </p>
 * 
 * <pre>
 * VB Changelog : 
 * 
 * Change    : BD11DEC02
 * Why       : Added length (of context) to the session table; basically to make
 *             it easier to debug the context handling but we may wish to use it in the
 *             future to warn the user when he / she is running out of context space
 * 
 * Change    : BD16DEC02
 * Why       : Do no longer use the help context as this was never
 *             really properly implemented
 * 
 * Change    : BD28OCT03
 * Why       : Fixed bug in addToContext that would cause the dirty flag to be
 *             set too often and would thus make store persist the session
 *             too often
 * 
 * Change    : BD22JUN04
 * Why       : Add -s to quick context as soon as available
 * 
 * Change    : BD30MAR05 V1.5:1
 * Why       : Support for data-sources
 * 
 * Change    : BD18APR05
 * Why       : Removed final references to zX.db
 * 
 * Change    : BD13JUN05 - V1.4:87
 * Why       : See BD28OCT03; still too often that we set dirty flag
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class Session extends ZXBO {
    
    //------------------------ Members
    
    private String sessionid;
    private Document context;
    private boolean dirty;
    
    // Handle to the httpSession.
    private HttpSession httpSession;
    // Map used to store the data.
    private Map contextData;
    
    //------------------------ Constuctor 

    /**
     * Default constructor.
     */
    public Session() {
        super();
    }  
    
    //------------------------ Getters/Setters
    
    /**
     * Returns the session id. This is a unique id with in this application for this
     * session data.
     * 
     * @return Returns the sessionid.
     */
    public String getSessionid() {
        return sessionid;
    }
    
    /**
     * @param sessionid The sessionid to set.
     */
    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }
    
    /**
     * Context data for this session.
     * 
     * @return Returns the context.
     */
    public Document getContext() {
        return context;
    }
    
    /**
     * @param context The context to set.
     */
    public void setContext(Document context) {
        this.context = context;
    }
    
    /**
     * Whether the session data needs to be updated.
     * 
     * @return Returns the dirty.
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * @param dirty The dirty to set.
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    //------------------------ Getters/Setters for HttpSession
    
    /**
     * The handle to the httpsession, used to store the session data in memory.
     * 
     * @return Returns the httpSession.
     */
    private HttpSession getHttpSession() {
        if (this.httpSession == null) {
            throw new RuntimeException("HttpSession is null");
        }
        return httpSession;
    }
    
    /**
     * @param httpSession The httpSession to set.
     */
    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    /**
     * Returns the context data that has been stored in memory.
     * 
     * @return Returns the context data.
     */
    private Map getContextData() {
        if (this.contextData == null) {
            this.contextData = new ZXCollection();
        }
        return contextData;
    }
    
    /**
     * @param contextData The sessionData to set.
     */
    private void setContextData(Map contextData) {
        this.contextData = contextData;
    }
    
    //------------------------ Public Methods 
    
    /**
     * Connect to framework.
     * 
     * @param pstrUserId The user id
     * @param pstrPassword The password
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if the connect fails
     */
    public zXType.rc connect(String pstrUserId, String pstrPassword) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrUserId", pstrUserId);
            getZx().trace.traceParam("pstrPassword", "xxxxx");
        }
        
        zXType.rc connect = zXType.rc.rcOK;
        
        try {
            /**
             * Reset all
             */
            resetBO();
            
            /**
             * Assign new id
             */
            try {
                setAutomatics("+");
            } catch (ZXException e) {
                throw new Exception("Unable to set automatics : " + e.getMessage());
            }
            
            /**
			 * Generate the session id and the hash id
			 */
            // String strSessionId = new Date().getTime() + "";
            // setValue("hshId", hash(strSessionId));
            // Fake a hash to match with VB implementation.
            String strSessionID = "07090";
            setValue("hshId", "102400000");
            setSessionid(strSessionID + getPKValue().getStringValue());
            
            /**
             * Now retrieve the user profile
             */
            if (getZx().getUserProfile().connect(pstrUserId, pstrPassword).pos != zXType.rc.rcOK.pos) {
                /**
                 * DGS01JUL2004: Don't add another error. We already have enough to show the user.
                 * Also, don't go to errExit because that adds another error.
                 */
                connect = zXType.rc.rcError;
                return connect;
                
            }
            
            /**
             * DGS01JUL2004: Don't add another error. We already have enough to show the user.
             * Also, don't go to errExit because that adds another error.
             */
            
            /**
             * Now we can store the user id and select language.
             */
            setValue("usrPrf", getZx().getUserProfile().getPKValue());
            setValue("lngg", getZx().getLanguage());
            
            /**
             * Mark as used
             */
            markUsed();
            
            /**
             * Ensure that contexts are clean
             */
            resetContext();
            
            /**
             * Dump userprofile for retrieval later
             */
            setValue("usrPrflXML", getZx().getUserProfile().bo2XML("session"));
            
            setValue("usrGrps", getZx().getUserProfile().getGroups());
            
            setValue("lngth", "5");
            
            /**
             * And store the session.
             */
            if (getZx().getSessionSource().equals(zXType.sessionSouce.ssHttpSession)) {
                getHttpSession().setAttribute(this.sessionid, this);
                
            } else {
                /**
                 * Make sure that the session data is
                 * inserted before carrying on.
                 */
                try {
                    getDS().beginTx();
                    insertBO();
                    getDS().commitTx();
                    
                } catch (Exception e) {
                    if (getDS().inTx()) {
                        getDS().rollbackTx();
                    }
                    
                    throw e;
                }
                
            }
            
            /**
             * And add session to quick context as -s
             */
            getZx().getQuickContext().setEntry("-s", this.sessionid);
            
            return connect;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to :  Connect to framework ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrUserId = " + pstrUserId);
                getZx().log.error("Parameter : pstrPassword = xxxxx");
            }
            
            if (getZx().throwException) throw new ZXException(e);
            connect = zXType.rc.rcError;
            return connect;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(connect);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Reset the session context.
     *
     * @throws ZXException Thrown if resetContext fails. 
     */
    public void resetContext() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        try {
            /**
             * Make sure we have a valid context XML
             */
            initContext();
            
            if (getZx().getSessionSource().equals(zXType.sessionSouce.ssHttpSession)) {
                /**
                 * Clean up the session data, but leave the subsession data.
                 */
                List list = new ArrayList(getContextData().keySet());
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    String strToDelete = (String)list.get(i);
                    if(!StringUtil.isSubSessionData(strToDelete)) {
                        getContextData().remove(strToDelete);
                    }
                }
                
            } else {
                /**
                 * We have to go through the context XML and remove all the items that have
                 *  NO subsessionid in the name
                 */
                Iterator objXMLNodes = this.context.getRootElement().getChildren().iterator();
                while (objXMLNodes.hasNext()) {
                    Element objXMLNodeToDelete = (Element)objXMLNodes.next();
                    if(!StringUtil.isSubSessionData(objXMLNodeToDelete.getName()) ) {
                        objXMLNodes.remove();
                    }
                }
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Reset the session context", e);
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add entry to the session context.
     * 
     * BD14JUN05 - V1.4:87; be more specific when to set dirt flag
     * 
     * @param pstrName Name of variable to add to context 
     * @param pstrValue Value of variable to add to context. If set to null, to will remove the entry. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if addToContext fails. 
     */
    public zXType.rc addToContext(String pstrName, String pstrValue) throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pstrValue", pstrValue);
        }
        
        zXType.rc addToContext = zXType.rc.rcOK;

        try {
            pstrName = pstrName.toLowerCase();
            
            if (getZx().getSessionSource().equals(zXType.sessionSouce.ssHttpSession)) {
                /**
                 * Use a local collection for session data.
                 */
                Object objOldValue = getContextData().get(pstrName);
                if (objOldValue != null && StringUtil.len(pstrValue) == 0) {
                    getContextData().remove(pstrName);
                } else {
                    getContextData().put(pstrName, pstrValue);
                }
                
            } else {
	            /**
	             * Make sure we have a valid context XML
	             */
	            initContext();
	            
	            /**
	             * Removed any \ and / from name; this can happen if users have BOs or pageflows
	             *  in subdirectories
	             */
	            pstrName = StringUtil.stripChars("/\\.", pstrName);
	            
	            /**
	             * Check whether node is already present
	             */
	            String strXPath = "//" + pstrName;
	            
	    		Element objNode = null;
	    		try {
	    		    objNode = (Element)XPath.selectSingleNode(this.context, strXPath);
	    		} catch (Exception e ) {
	    		    getZx().log.warn("Failed to get element for xpath," + strXPath, e);
	    		}
	    		
	            if (objNode == null) {
	                
	                /**
	                 * Add node (if there is a need for it)
	                 */
	                if (StringUtil.len(pstrValue) > 0) {
	                    Element objElement = new Element(pstrName);
	                    objElement = objElement.setText(pstrValue);
	                    this.context.getRootElement().addContent(objElement);
	                    setDirty(true);
	                } else {
	                    return addToContext;
	                }
	                
	            } else {
	                if (StringUtil.len(pstrValue) == 0) {
	                    /**
	                     * Empty value means delete node
	                     */
	                    this.context.getRootElement().removeContent(objNode);
	                    setDirty(true);
	                } else {
	                    /**
	                     * Replace value
	                     */
	                	if (!objNode.getText().equals(pstrValue)) {
		                    objNode.setText(pstrValue);
		                    setDirty(true);
	                	}
	                }
	            }
	            
	            /**
	             * Be much more carefull when to set it to avoid updating the session
	             * when there is no need to....
	             */
	            // setDirty(true);
            } 
            
            return addToContext;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Add entry to the session context", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrName = "+ pstrName);
                getZx().log.error("Parameter : pstrValue = "+ pstrValue);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            addToContext = zXType.rc.rcError;
            return addToContext;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(addToContext);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get entry from context.
     *
     * @param pstrName Name of from context 
     * @return Returns a value from the context.
     * @throws ZXException Thrown if getFromContext fails. 
     */
    public String getFromContext(String pstrName) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
        }
        
        String getFromContext = null; 
        
        try {
            
            pstrName = pstrName.toLowerCase();
            
            if (getZx().getSessionSource().equals(zXType.sessionSouce.ssHttpSession)) {
                /**
                 * Retrieve the session data from a local collection 
                 */
                getFromContext = (String)getContextData().get(pstrName);
                
            } else {
                
                /**
                 * Make sure we have a valid context XML
                 */
                initContext();
                
                /**
                 * Clean up the name
                 */
                pstrName = StringUtil.stripChars("/\\.", pstrName);
                
                /**
                 * Construct xpath
                 */
                String strXPath = "//c/" + pstrName;
                
                /**
                 * And get value, bit of a mission :(
                 */
                Object objValue  = XPath.selectSingleNode(this.context, strXPath);
        		if (objValue instanceof Element) {
        		    getFromContext = ((Element) objValue).getText();
        		} else if (objValue instanceof Text) {
        		    getFromContext = ((Text) objValue).getText();
        		} else if (objValue instanceof Attribute) {
        		    getFromContext = ((Attribute) objValue).getValue();
        		} else if (objValue instanceof CDATA) {
        		    getFromContext = ((CDATA) objValue).getText();
        		}
            }
            
            if (getFromContext == null) {
                getFromContext = "";
            }
            
            return getFromContext;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get entry from context", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrName = "+ pstrName);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getFromContext;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getFromContext);
                getZx().trace.exitMethod();
            }
        }    
    }
    
    /**
     * Store session to database or Http Session.
     */
    public void store() {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        try {
            
            if (isDirty() || getPersistStatus().equals(zXType.persistStatus.psDirty)) {
                /**
                 * Only add it to the HttpSession if one existss : 
                 * NOTE : May only need to store the Properties of the BO ? 
                 */
                if (getZx().getSessionSource().equals(zXType.sessionSouce.ssHttpSession)) {
                    /**
                     * Need to store the BOContext and QuickContext stuff.
                     * 
                     * Use the whole sessionid for storage as we do not need to worry about security
                     * as HttpSession does that for you.
                     */
                    getHttpSession().setAttribute(getSessionid(), this); 
                    
                } else {
    	            /**
    	             * Determine the smallest group possible to update for performance reasons
    	             */    
                    String strGroup = "lstUse";
                    
                    /**
                     * 'Zip' the contexts if required
                     */
                    if (context != null) {
                        XMLOutputter out = new XMLOutputter();
                        setValue("cntxt", out.outputString(this.context));
                        setValue("lngth", "" + getValue("cntxt").getStringValue().length());
                        strGroup = strGroup + ",cntxt,lngth";
                    }
                    
                    /**
                     * Set as used
                     */
                    markUsed();
                    
                    /**
                     * And update the database
                     * 
                     * DGS23SEP2003: Not sure if need to do this - think that any switching to
                     * alternate connection will have been done and switched back before or after
                     * this method:
                     */
                    boolean blnAlternateConnection = false;
                    if (getZx().getDataSources().getPrimary().isSecondaryActive()) {
                        blnAlternateConnection = true;
                        // this.zx.getDb().swapConnection();
                    }
                    
                    updateBO(strGroup);
                    
                    /**
                     * DGS23SEP2003: As above, not sure if need to do this
                     */
                    if (blnAlternateConnection) {
                        // this.zx.getDb().swapConnection();
                    }
                    
                }
            }
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Store session", e);
            
        } finally {
	        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
	            getZx().trace.exitMethod();
	        }
        }
    }
    
    /**
     * Mark the session as used. This will update the last used date to the current date.
     *
     * @throws ZXException Thrown if markUsed fails. 
     */
    public void markUsed() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        try {
            setValue("lstUse", "#now", true);
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Mark the session as used", e);
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Retrieve session.
     *
     * @param pstrSession The session id of the session you want to retrieve.
     * @return Returns the return code of the function.  
     * @throws ZXException Thrown if retrieve fails. 
     */
    public zXType.rc retrieve(String pstrSession) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrSession", pstrSession);
        }
        
        zXType.rc retrieve = zXType.rc.rcOK;
        
        try {     
            /**
             * No session id given....
             */
            if (StringUtil.len(pstrSession) == 0) {
                throw new Exception("No session-id passed");
            }
            
            /**
             * Only use HttpSession if it is available.
             */
            if (getZx().getSessionSource().equals(zXType.sessionSouce.ssHttpSession)) {
                retrieve = retrieveFromHttpsession(pstrSession);
                /** Try to retrieve it from the database **/
                if (retrieve.pos == zXType.rc.rcError.pos) {
                    retrieve = retriveFromDS(pstrSession);
                }
                
            } else {
                retrieve = retriveFromDS(pstrSession);
            }
            
            /**
             * And add session to quick context as -s
             */
            getZx().getQuickContext().setEntry("-s", pstrSession);
            
            return retrieve;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Retrieve session", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrSession = " + pstrSession);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            retrieve = zXType.rc.rcError;
            return retrieve;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
            
//            try {
//	            int traceLevel = (int)((LongProperty)getZx().getUserProfile().getValue("trclvl")).getValue();
//	            if (!getZx().trace.isTraceEnabled() && traceLevel > 0) {
//	                String userName = getZx().getUserProfile().getValue("Nme").getStringValue();
//	                String ss = getZx().getQuickContext().getEntry("-ss");
//	                
//	                StringBuffer strFileName = new StringBuffer("..//..//..//");
//	                strFileName.append(userName).append("-").append(this.sessionid);
//	                if (StringUtil.len(ss) > 0) {
//	                    strFileName.append("-").append(ss);
//	                }
//	                strFileName.append(".trc");
//	                
//	                // Overide the trace file :
//	                getZx().setTraceFileName(strFileName.toString());
//	                
//	                getZx().setTraceActive(true);
//	                getZx().setTraceLevel(traceLevel);
//	                getZx().trace.setActive(true);
//	                getZx().trace.setCurrentTraceLevel(traceLevel);
//	
//	                Log4jFactory.configure(getZx());
//	                
//	                getZx().trace.startTracing();
//	                
//	                getZx().trace.trace("Starting tracing");
//	            }
//            } catch (Exception e) {
//                getZx().log.error("Failed to get tracing info.", e);
//            }
        }
    }

	/**
     * Try to retrieve the session data from the data source.
     * 
	 * @param pstrSession Session id.
	 * @return Return the return code of the method.
	 * @throws ZXException Thrown if retriveFromDS fails.
	 */
	private zXType.rc retriveFromDS(String pstrSession) throws ZXException {
		zXType.rc retriveFromDS;
        
		/**
		 * The session-id is made up of two parts:
		 * - 5 digit id
		 * - session id
		 * 
		 * We now hash the 5 digit check id and use it to retrieve the
		 * session. This schema is used to avoid people taking over
		 * other people session by simply trying some session id's
		 * around their own session
		 */
		setPKValue(pstrSession.substring(5));
		
		/**
		 * Try to load the session
		 */
		retriveFromDS = loadBO("*", "+", false);
		if (retriveFromDS.pos == zXType.rc.rcOK.pos) {
		    /**
		     * And retrieve the userprofile information etc..
		     */
		    getZx().getUserProfile().xml2bo(getValue("usrPrflXML").getStringValue(), "session");
		    getZx().getUserProfile().setGroups(getValue("usrGrps").getStringValue());
		    getZx().setLanguage(getValue("lngg").getStringValue());
		    
		    /**
		     * Now we can assume that the session id is correct
		     */
		    setSessionid(pstrSession);
		    
		} else {
		    getZx().trace.addError("Unable to load session", pstrSession);
            getZx().log.error(" enmSessionSource = " + zXType.valueOf(getZx().getSessionSource()));
            getZx().log.error(" pstrSession = " + pstrSession);
            getZx().log.error(" sessionID = " +  pstrSession.substring(5));
            
		    retriveFromDS = zXType.rc.rcError;
		}
        
		return retriveFromDS;
	}

	/**
     * Try to retrive from the httpsession.
     * 
     * @param pstrSession Session id.
     * @return Where retrieveFromHttpsession failed or successed
     * @throws ZXException Thrown if retrieveFromHttpsession fails.
	 */
	private zXType.rc retrieveFromHttpsession(String pstrSession) throws ZXException {
        zXType.rc retrieveFromHttpsession = zXType.rc.rcOK;
        
		/**
		 * Need to copy the values from the session etc.. ? 
		 * Probably better to be in ZX.
		 */
		Session objSession = (Session)getHttpSession().getAttribute(pstrSession);
		if (objSession != null) {
		    /**
		     * Retrieve the session data.
		     */
		    setSessionid(objSession.getSessionid());
		    setProperties(objSession.getProperties()); // This is a direct copy.
		    setContext(objSession.getContext());
		    setContextData(objSession.getContextData());
		    
		    /**
		     * And retrieve the userprofile information etc..
		     */
		    getZx().getUserProfile().xml2bo(objSession.getValue("usrPrflXML").getStringValue(), "session");
		    getZx().getUserProfile().setGroups(objSession.getValue("usrGrps").getStringValue());
		    getZx().setLanguage(objSession.getValue("lngg").getStringValue());
		    
		} else {
            getZx().log.error("Failed to get Session from HttSession.");
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error(" enmSessionSource = " + zXType.valueOf(getZx().getSessionSource()));
                getZx().log.error(" pstrSession = " + pstrSession);
            }
            
            retrieveFromHttpsession = zXType.rc.rcError;
		}
        
		return retrieveFromHttpsession;
	}
    
    /**
     * Disconnect from system.
     * 
     * <pre>
     * 
     * This does the much needed clean-up of resources.
     * </pre>
     *
     * @throws ZXException Thrown if disconnect fails. 
     */
    public void disconnect() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        try {
            /**
             * Only allowed when connected
             */
            if (StringUtil.isEmpty(getSessionid())) {
                throw new Exception("Cannot disconnect when not connected");
            }
            
            /**
             * Remove from HttpSession if there is one accosiated to ZX : 
             */
            if (getZx().getSessionSource().equals(zXType.sessionSouce.ssHttpSession)) {
                getHttpSession().removeAttribute(getSessionid());
                getHttpSession().invalidate();
                
            } else {
                /**
                 * Delete session row from the database.
                 */
                deleteBO();
            }
            
            /**
             * Mark session as unusable
             */
            setSessionid("");
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Disconnect from system", e);
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Delete all session entries .
     * 
     * <pre>
     * 
     * Delete all session entries that have not been used for some time
     *</pre>
     *
     * @throws ZXException Thrown if cleanup fails. 
     */
    public void cleanup() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        DSHandler objDSHandler = null;
        Transaction objTransaction = null;
        
        try {
        	
            if (getZx().getSessionSource().equals(zXType.sessionSouce.ssHttpSession)) {
                /**
                 * Need to come up with a way of deleting older session data
                 * We could use the session time out settings in Websphere to handle this 
                 * automatically.
                 */
            	
            } else {
                if (getZx().getSessionTimeOut() > 0) {
		            /**
		             * Delete all sessions that have been used more than x minutes ago
		             */
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MINUTE, - getZx().getSessionTimeOut());
                    Date date = calendar.getTime();
                    DateFormat df = getZx().getTimestampFormat();
                    String strWhereClause = ":lstUse<#" + df.format(date)  + "#";
                    
                    objDSHandler = getDS();
		            objTransaction = objDSHandler.beginTransaction();
                    objDSHandler.deleteBO(this, strWhereClause);
                    objTransaction.commit();
                }
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Delete all session entries .", e);
            
            /**
             * Rollback if failed.
             */
            if (objTransaction != null) {
                objTransaction.rollback();
            }
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Initialise context (is just in time).
     * 
     * <pre>
     * 
     * NOTE : It will only do it once for instance of the Session.
     *</pre>
     *
     * @throws ZXException Thrown if initContext fails. 
     */
    public void initContext() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        try {
            
            if (this.context == null) {
                if (StringUtil.len(getValue("cntxt").getStringValue()) == 0) {
                    setValue("cntxt", "<c/>");
                }
                
                StringReader objReader = new StringReader(getValue("cntxt").getStringValue());
                SAXBuilder objBuilder = new SAXBuilder(false);
                try {
                    this.context = objBuilder.build(objReader);
                } catch (Exception e) {
                    throw new NestableException("Unable to parse context XML", e);
                }
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Initialise context (is just in time)", e);
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
     
    /**
     * Close a subsession.
     *
     * @param pstrSubSessionId The sub sessionid to close 
     * @throws ZXException Thrown if closeSubsession fails. 
     */
    public void closeSubsession(String pstrSubSessionId) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrSubSessionId", pstrSubSessionId);
        }
        
        try {
            /**
             * If no subsession has been given; assume the same logic as resetting the context
             */
            if (StringUtil.isEmpty(pstrSubSessionId)) {
                /**
                 * BD28NOV02
                 * No: this routine is also called when NOT having a subsession (eg
                 * when you close a frame)
                 */
                // resetContext();
            } else {
                /**
                 * Make sure we have a valid context XML
                 */
                initContext();
                
                /**
                 * We have to go through the context XML and remove all the items that have
                 * the subsessionid in the name
                 */
                Element objXMLNodeToDelete;
                ArrayList toRemove = new ArrayList();
                Iterator iter = this.context.getRootElement().getChildren().iterator();
                while (iter.hasNext()) {
                    objXMLNodeToDelete = (Element)iter.next();
                    if (objXMLNodeToDelete.getName().indexOf(pstrSubSessionId) != -1) {
                        toRemove.add(objXMLNodeToDelete.getName());
                    }
                }
                int length = toRemove.size();
                for (int i = 0; i < length; i++) {
                    addToContext((String)toRemove.get(i), null);
                }
            }
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Close a subsession", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrSubSessionId = "+ pstrSubSessionId);
            }
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    // Dump due to overhead :)
//    /** 
//     * Store the session in the database when the session is garbage collected.
//     * 
//     * @see java.lang.Object#finalize()
//     **/
//    protected void finalize() throws Throwable {
//        store();
//        super.finalize();
//    }
}
