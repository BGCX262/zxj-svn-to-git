/*
 * Created on Feb 3, 2004
 * $Id: ZXException.java,v 1.1.2.6 2005/11/21 15:15:12 mike Exp $
 */
package org.zxframework;

import org.zxframework.exception.NestableException;
import org.zxframework.util.ToStringBuilder;

/**
 * ZXException - Core exception class for zx.
 * 
 * <p>
 * An exception that is thrown only if the method fails to execute correctly.
 * </p>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ZXException extends NestableException {

    private boolean logged = true;
    private String additional;

    /**
     * Default constructor.
     */
    public ZXException() {
        super();
    }

    /**
     * Use this constructor if you want to set this exception to be logged the
     * next time it gets catch. By default is
     * 
     * @param pstrMsg The main error message
     * @param pblnLogException Whether this exception has already been logged.
     */
    public ZXException(String pstrMsg, boolean pblnLogException) {
        super(pstrMsg);
        this.logged = pblnLogException;
    }
    
    /**
     * Special constructor to allow passing of additional info.
     * 
     * @param pstrMsg The main error messages
     * @param pstrAddittionalInfo Additional informational like parameters passed.
     */
    public ZXException (String pstrMsg, String pstrAddittionalInfo) {
        super(pstrMsg);
        this.logged = false; // Force logging of the exception only once.
        this.additional = pstrAddittionalInfo;
    }
    
    /**
     * Constructor with a message as a parameter.
     * 
     * @param arg0 The message of the exception.
     */
    public ZXException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor with a message and the cause.
     * 
     * @param arg0 A descriptive message.
     * @param arg1 The actual exception.
     */
    public ZXException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        this.logged = true;
    }

    /**
     * Constructor with just the cause of the exception.
     * @param arg0 The cause of this exception.
     */
    public ZXException(Throwable arg0) {
        super(arg0);
    }
    
    //-------------------------------------------- Getters and Setters
    
    /**
     * @return Returns the logged.
     */
    public boolean isLogged() {
        return this.logged;
    }

    /**
     * @param logException
     */
    public void setLogged(boolean logException) {
        this.logged = logException;
    }

    /**
     * @return Returns the additional.
     */
    public String getAdditional() {
        return additional;
    }
    
    /**
     * @param additional The additional to set.
     */
    public void setAdditional(String additional) {
        this.additional = additional;
    }
    
    /**
     * @see java.lang.Throwable#toString()
     */
    public String toString() {
    	ToStringBuilder toString = new ToStringBuilder(this);
    	toString.append("message", getLocalizedMessage());
    	toString.append("additional", getAdditional());
    	return toString.toString();
    }
}