/*
 * Created on Jun 22, 2005
 * $Id: ServletUtil.java,v 1.1.2.2 2006/07/17 14:01:07 mike Exp $
 */
package org.zxframework.web.util;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet Util Class.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ServletUtil {
	
	/**
	 * Hide default constructor.
	 */
	private ServletUtil() {
		super();
	}
	
	/**
	 * Get the value for a cookie.
	 * 
	 * @param request The ServletRequest.
	 * @param name The name of the cookie to retrieve
	 * @return Returns the cookies value
	 */
	public static String getCookieValue(ServletRequest request, String name) {
		String getCookieValue = null;
		
		if (request instanceof HttpServletRequest) {
			Cookie[] arrCookies = ((HttpServletRequest)request).getCookies();
			for (int i = 0; i < arrCookies.length; i++) {
				Cookie cookie = arrCookies[i];
				if (cookie.getName().equals(name)) {
					getCookieValue = cookie.getValue();
					break;
				}
			}
		}
		return getCookieValue;
	}
}