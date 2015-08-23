/*
 * Created on Aug 8, 2004 by Michael
 * $Id: ZXTag.java,v 1.1.2.17 2006/07/17 14:01:52 mike Exp $
 */
package org.zxframework.web.taglib;

import java.io.IOException;
import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.zxframework.Environment;
import org.zxframework.ThreadLocalZX;
import org.zxframework.ZX;
import org.zxframework.ZXException;
import org.zxframework.zXType;

import org.zxframework.datasources.DSHttpSession;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

import org.zxframework.util.StringUtil;

/**
 * Creates a zx object and puts it in the pageContext.
 * 
 * <pre>
 * 
 * The roles of the zXTag is as follows : 
 * 1) Init zX.
 * 2) Check whether the use is logged in and forward him to a error page if not.
 * 3) Set the javascript environment variable.
 * 4) Store the httpSession handle if needed.
 * 5) Set the quickContext value for -ss as soon as possible.
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ZXTag extends TagSupport {
    
    //------------------------ Members
    
    private static String strConfigFile;
    private static String strErrorPage;
    private boolean jsheader = true;
    private boolean checklogin = true;
    
    private static Log log = LogFactory.getLog(ZXTag.class);

    //------------------------ Getters/Setters
    
    /**
     * @return Returns the jsheader.
     */
    public boolean isJsheader() {
        return jsheader;
    }
    
    /**
     * @param jsheader The jsheader to set.
     */
    public void setJsheader(boolean jsheader) {
        this.jsheader = jsheader;
    }

    /**
     * @return Returns the checklogin.
     */
    public boolean isChecklogin() {
        return checklogin;
    }
    
    /**
     * @param checklogin The checklogin to set.
     */
    public void setChecklogin(boolean checklogin) {
        this.checklogin = checklogin;
    }
    
    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    public int doStartTag() throws JspException {
        return (SKIP_BODY);
    }
    
    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() {
        ZX objZX = null;
        
        try {
        	/**
             * STEP 1 : Get the zx config file name and init zX.
             * 
             * Get the filename of the config file for zX from the web.xml.
             * 
             * Do once only as creating a handle to the Context can be intensive :
             */
            log.trace("Getting value for zX config.");
            if (strConfigFile == null) {
                try {
                    Context context = new InitialContext();
                    strConfigFile = (String)context.lookup(Environment.WEB_CONFIG);
                } catch (NamingException e) {
                    log.error("Failed to look up jndi value for zx config", e);
                    throw new NestableRuntimeException("Fatal error in zX init tag.", e);
                }
                
                strErrorPage = "/html/zxtimeout.html"; // Get the error page from web.xml.
            }
            
            /**
             * Get the show started by initialising zX and html objects
             */
            log.trace("Loading zX.");
            try {
                objZX = new ZX(strConfigFile);
            } catch (ZXException e) {
                objZX = ThreadLocalZX.getZX();
                throw new NestableRuntimeException("Failed to init zX", e);
            }
            
            if (StringUtil.len(objZX.getSettings().getStatusFileDir()) == 0) {
                /**
                 * Tell zX where the imTransient dir is.
                 * This allows for Zero configuration.
                 */
            	objZX.getSettings().setStatusFileDir(this.pageContext.getServletContext().getRealPath("/imTransient") + java.io.File.separatorChar);
            }
            
            /**
             * Set up the session stuff.
             * If the session source is set to httpsession, all runtime variables for the session will
             * not be stored in the httpsession object, instead of in the datbase.
             */
            HttpSession session = this.pageContext.getSession();
            if (objZX.getSessionSource().equals(zXType.sessionSouce.ssHttpSession)) {
                try {
                    objZX.log.trace("Using HTTPSession sessionStore");
                    
                    /**
                     * Try to get a handle to the Current HttpSession to
                     * store session data.
                     */
                    if (session == null) {
                        objZX.log.fatal("FATAL ERROR : Can not get handle to HttpSession.");
                        throw new RuntimeException("Could not get a handle to HttpSession.");
                    }
                    objZX.getSession().setHttpSession(session);
                    
                } catch (Exception e) {
                    objZX.log.error("FATAL ERROR : Failed to get handle to HtttpSession", e);
                    throw new NestableRuntimeException("Failed to get httpsession", e);
                }
            }
            
            /**
             * TEMP HACK: Somehow communicate with the DSHttpSession.
             */
            objZX.getSettings().getTags().put(DSHttpSession.SSN, session);
            
            /**
             * STEP 2 : Check whether the user has a session with the system. 
             * 
             * First make sure that the sessionid is correct.
             * This can be disabled for pages that do not session id like the login page.
             */
            if (this.checklogin) {
                /**
                 * Get the session id :
                 */
                // Using Cookies as an alternative to passing -s arround :
            	//String sessionID = ServletUtil.getCookieValue(pageContext.getRequest(), "-s");
            	
                /** 
                 * Get the session id from the http request.
                 */
                String sessionID = this.pageContext.getRequest().getParameter("-s");
                int enmRC = objZX.getSession().retrieve(sessionID).pos;
                if (enmRC != zXType.rc.rcOK.pos) {
                    forwardToFailure(objZX);
                    return (SKIP_PAGE);
                }
            }
            
            /**
             * Populate the QuickContext asap soon as possible from the Request object.
             * 
             * This will populate all of the request variables starting with "-". Like "-a" and "-ss".
             */
            String paramName;
            Enumeration enm = pageContext.getRequest().getParameterNames();
            while (enm.hasMoreElements()) {
                paramName = (String)enm.nextElement();
                
                /**
                 * Set into quick context but do NOT set the status as we use the
                 * status later to tell whether an entry was manually added or
                 * simply copied from the incoming querystring
                 * Now make sure that we do NOT replace any entries that have been
                 * added to the quick context since it was first initialised
                 */
                if (objZX.getQuickContext().getStatus(paramName) != 1 && paramName.charAt(0) == '-') {
                    objZX.getQuickContext().setEntry(paramName, 
                                                     pageContext.getRequest().getParameter(paramName));
                }
            }
            
            /**
             * Set the env type for the javascripts. 
             * ie: jsp for J2EE
             */
            if (this.jsheader) {
                try {
                    this.pageContext.getOut().write("<script type=\"text/javascript\" language=\"JavaScript\">\n");
                    
                    this.pageContext.getOut().write("var SCRPT = 'jsp'; \n");
                    if (StringUtil.len(objZX.getSession().getSessionid())> 0) {
                        pageContext.getOut().write(" var gSessionID = '" + objZX.getSession().getSessionid() + "';\n");
                    }
                    
                    this.pageContext.getOut().write("</script>\n");
                    
                    this.pageContext.getOut().flush();
                } catch (IOException ioe) {
                    objZX.log.error("FATAL ERROR : Failed to init zXTag", ioe);
                }
            }
            
        } catch (ZXException e) {
            try {
                if (objZX != null) {
                    objZX.log.error("FATAL ERROR : Failed to init zXTag", e);
                    objZX.trace.addError("FATAL ERROR : Failed to init jsp servlet.", e);
                } else {
                    log.error("FATAL ERROR : Failed to init zXTag", e);
                }
                
                /** 
                 * Clean up zX db connections etc.. and forward web browser to the standard error page. 
                 **/
                forwardToFailure(objZX);
                return (SKIP_PAGE);
                
            } catch (Exception e1) {
                new JspException(e.toString());
            }
        } catch (RuntimeException e) {
            try {
                if (objZX != null) {
                    pageContext.getOut().write("<pre>" + objZX.trace.formatStack(true) + "</pre>");
                } else {
                    pageContext.getOut().write("Fatal error in zXTag init : <br/>" + e.getMessage());
                }
                return (SKIP_PAGE);
                
            } catch (Exception e1) { 
            	/**
            	 * Ignore any errors
            	 */
            }
            
        }
        
        return (EVAL_PAGE);
    }
    
    /**
     * Go to failure and clean things up.
     * 
     * @param pobjZX A handle to zx to clean up.
     */
    private void forwardToFailure(ZX pobjZX) {
        /**
         * Clean up - Make sure db connection is closed.
         */
        if (pobjZX != null) {
            pobjZX.cleanup();
            
            /** Log the full stacktrace. */
            pobjZX.log.error(pobjZX.trace.formatStack(true));
        }
        
        /**
         * Try to get the error page from the webapp configuration ;
         * 
         * Forward to error page :
         */
        try {
            pageContext.forward(strErrorPage);
        } catch (Exception ieo) {
            String strError = "Failed to forward to error page.";
            if (pobjZX != null) {
                pobjZX.log.error(strError, ieo);
            } else {
                log.error(strError, ieo);
            }
        }
    }
}