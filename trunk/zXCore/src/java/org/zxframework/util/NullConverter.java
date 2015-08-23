/*
 * Created on 20-Feb-2005 by Michael
 * $Id: NullConverter.java,v 1.1.2.2 2005/05/12 15:52:59 mike Exp $
 */
package org.zxframework.util;

/** 
 * This class follows the Null Object Pattern by Bobby Woolf. 
 */
public class NullConverter extends ToStringConverter {
    
    /**
     * @param successor
     */
    public NullConverter(ToStringConverter successor) {
        super(successor);
    }

    /** 
     * This handler is only used if the object is null.
     *  
     * @param o The object we are trying to get the toString of. 
     * @return Whether NullConverter can do a toString on this object.
     */
    protected boolean isHandler(Object o) {
        return o == null;
    }
    
    /**
     * 
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     * @return Returns the toString of a null object.
     */
    protected StringBuffer toString(Object o, StringBuffer buf) {
        buf.append("(null)");
        return buf;
    }
}