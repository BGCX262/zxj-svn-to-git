package org.zxframework.web.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zxframework.Environment;
import org.zxframework.ZX;
import org.zxframework.zXType;
import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

import java.io.IOException;
import java.util.Enumeration;

/**
 * 
 */
public class ResponseHeaderFilter implements Filter {
    
    private static Log log = LogFactory.getLog(ResponseHeaderFilter.class);
    
	private FilterConfig filterConfig;
	private boolean checkLogin;
    private String strConfigFile;
    private String strErrorPage;
	
	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest)req;
		
		/**
		 * Check if user is logged on.
		 */
		if (this.checkLogin && !isUserLoggedIn(request)) {
	        /**
	         * Forward to error page
	         */
	        try {
	            response.sendRedirect(strErrorPage);
	        } catch (Exception e) {
	            log.error("Failed to forward to error page.", e);
	        }
	        
	        /**
	         * No point in updating the headers
	         */
            return;
		}
			
		/**
		 * We could do some special document handling stuff here.
		 * Like move document from upload directory. Maybe also do some
		 * WebDav stuff?
		 */
		
		/**
		 * Set the provided HTTP response parameters
		 */
		for (Enumeration e = this.filterConfig.getInitParameterNames(); e.hasMoreElements();) {
			String headerName = (String) e.nextElement();
			
			/**
			 * Ignore special config settings
			 */
			if (headerName.charAt(0) != '_') {
				response.addHeader(headerName, this.filterConfig.getInitParameter(headerName));
			}
		}
		
		/**
		 * Pass the request/response on
		 */
		chain.doFilter(req, response);
	}
	
	/**
	 * With this we can protect even static pages.
	 * 
	 * @param request
	 * @return Returns whether the user is logged on
	 */
	private boolean isUserLoggedIn(HttpServletRequest request) {
		/**
		 * Exit early for now.
		 */
        String sessionID = request.getParameter("-s");
        if (sessionID == null || sessionID.length() == 0) {
        	return true;
        }
        
        ZX objZX = null;
        
        try {
            if (log.isTraceEnabled()) log.trace("About to init zX : " + strConfigFile);
            
            try {
                objZX = new ZX(strConfigFile);
                
                if (objZX.getSessionSource().equals(zXType.sessionSouce.ssHttpSession)) {
                    objZX.getSession().setHttpSession(request.getSession());
                }
                
            } catch (Exception e) {
                log.fatal("FATAL ERROR : Failed to init zX configFile=" + strConfigFile, e);
                return false;
            }
            
            if (objZX.getSession().retrieve(sessionID).pos != zXType.rc.rcOK.pos) {
            	return false;
            }
            
        	return true;
            
        } catch (Exception e) {
        	log.error("Error accured in filter", e);
        	return false;
        	
        } finally {
        	/**
        	 * Ensure we always clean up stuff
        	 */
        	if (objZX != null) {
        		objZX.cleanup();
        		objZX = null;
        	}
        }
	}
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
		
		this.checkLogin = Boolean.valueOf(this.filterConfig.getInitParameter("_checkLogin")).booleanValue();
		
		if (this.checkLogin) {
			/**
			 * Do some setup work
			 */
	        try {
	            Context context = new InitialContext();
	            strConfigFile = (String)context.lookup(Environment.WEB_CONFIG);
	        } catch (NamingException e) {
	            log.error("Failed to look up jndi value for zx config", e);
	            throw new RuntimeException("Failed to get value for zX config", e);
	        }
	        /**
	         * Get error page from zX config
	         */
	        strErrorPage = "../html/zxtimeout.html";
		}
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		this.filterConfig = null;
		this.strConfigFile = null;
		this.strErrorPage = null;
	}
}