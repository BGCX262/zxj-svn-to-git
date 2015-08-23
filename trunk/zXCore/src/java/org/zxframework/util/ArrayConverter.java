/*
 * Created on 20-Feb-2005 by Michael
 * $Id: ArrayConverter.java,v 1.1.2.3 2005/06/08 10:07:09 mike Exp $
 */
package org.zxframework.util;

import java.lang.reflect.Array;

/**
 * This converter only supports Arrays.
 *  
 * It supports primitive arrays and multi-dimensional arrays.
 */
public class ArrayConverter extends ToStringConverter {
    
    /**
     * @param successor The successor object.
     */
    public ArrayConverter(ToStringConverter successor) {
        super(successor);
    }

    /** 
     * This handler only works for arrays.
     *  
     * @param o The object we are trying to get the toString of.
     * @return Whether NullConverter can do a toString on this object. 
     */
    protected boolean isHandler(Object o) {
        return o.getClass().isArray();
    }

    /**
     * Append the type of array to the buffer.
     * 
     * We want to append the type of the array and the number of dimensions,
     * e.g. for a three-dimensional array we want to append [][][]. Using += for
     * the postfix String is not ideal, but the most common case will probably
     * be a single dimension array, in which case there would not be any
     * concatenation of Strings.
     * 
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     */
    protected void appendName(Object o, StringBuffer buf) {
        String postfix = "[]";
        Class c = o.getClass().getComponentType();
        while (c.isArray()) {
            postfix += "[]";
            c = c.getComponentType();
        }
        buf.append(StringUtil.stripPackageName(c));
        buf.append(postfix);
    }

    /**
     * Append the values to the buffer.
     * 
     * We show the array using the dimensions and the toString() methods of the
     * values. This method is recursive to handle multi- dimensional arrays.
     * 
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     */
    protected void appendValues(Object o, StringBuffer buf) {
        buf.append('{');
        int length = Array.getLength(o);
        for (int i = 0; i < length; i++) {
            Object value = Array.get(o, i);
            if (value != null && value.getClass().isArray()) {
                appendValues(value, buf);
            } else if (o.getClass().getComponentType().isPrimitive()) {
                buf.append(value);
            } else if (value instanceof String) {
                buf.append('"').append(value).append('"');
            } else {
                ToStringFacade.toString(value, buf);
            }
            if (i < length - 1) {
                buf.append(',');
            }
        }
        buf.append('}');
    }
}