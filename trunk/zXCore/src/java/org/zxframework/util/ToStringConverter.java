/*
 * Created on 20-Feb-2005 by Michael
 * $Id: ToStringConverter.java,v 1.1.2.4 2006/07/17 16:16:16 mike Exp $
 */
package org.zxframework.util;

/**
 * Here we use the Chain-of-Responsibility design pattern that is very similar
 * to the Strategy pattern, except that we pass the request on if we cannot deal
 * with it. It helps us to reduce the number of multi-conditional if-else-if
 * statements that we need in our program to decide which strategy to use.
 */
public abstract class ToStringConverter {
    
    private final ToStringConverter successor;

    /**
     * We need to know the successor, if this handler cannot cope with the
     * request.
     * 
     * @param successor
     */
    protected ToStringConverter(ToStringConverter successor) {
        this.successor = successor;
    }

    /**
     * handle() decides whether this current object can handle the request;
     * otherwise it is passed onto the next in the sequence
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     * @return Returns to toString of the object.
     */
    protected final StringBuffer handle(Object o, StringBuffer buf) {
        if (!isHandler(o)) {
            return successor.handle(o, buf);
        }
        
        return toString(o, buf);
    }

    /**
     * Subclasses can specify whether they can handle the current object.
     * 
     * @param o The object we are trying to get the toString of.
     * @return Whether we can do a toString on this object. 
     */
    protected abstract boolean isHandler(Object o);

    /**
     * The toString() method is the main method that is called from within the
     * handle() method, once it is established which object should handle the
     * request.
     * 
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     * @return Returns the toString of the object. 
     */
    protected StringBuffer toString(Object o, StringBuffer buf) {
    	
    	if (o != null) {
    		if (o.getClass().isArray()) {
    			appendName(o, buf);
    		} else {
	        	ToStringBuilder toStringBuilder = new ToStringBuilder(o);
	        	buf.append(toStringBuilder.toString());
    		}
        	buf.append(ToStringBuilder.CLASS_BEGIN);
    	}
    	
    	appendValues(o, buf);
    	
    	if (o != null) {
    		buf.append(ToStringBuilder.CLASS_END);
    	}
    	
//        buf.append('(');
//        appendName(o, buf);
//        buf.append(getSeparator());
//        appendValues(o, buf);
//        buf.append(')');
    	
        return buf;
    }

    /**
     * The separator between name and value can be different for different types
     * of objects.
     * 
     * @return Returns the separator between values.
     */
    protected char getSeparator() {
        return '=';
    }

    /** 
     * This method will determine an identifier for the current object.
     * 
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     */
    protected void appendName(Object o, StringBuffer buf) {
    	// Nothing happens here
    }

    /** 
     * This method will determine the values for the current object.
     * 
     * @param o The object we are trying to get the toString of.
     * @param buf 
     */
    protected void appendValues(Object o, StringBuffer buf) {
    	// Nothing happens here
    }
}
