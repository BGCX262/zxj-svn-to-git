/*
 * Created on 20-Feb-2005 by Michael Brewer
 * $Id: ToStringFacade.java,v 1.1.2.2 2005/05/12 15:52:59 mike Exp $
 */
package org.zxframework.util;

/**
 * The default behaviour is to start with the NullConverter, followed by the
 * ArrayConverter and then the ObjectConverter.
 */
public class ToStringFacade {
    
    private final static ToStringConverter INITIAL = new NullConverter(
            new ArrayConverter(new ObjectConverter(null)));

    /**
     * @param o The object we are trying to get the toString of.
     * @return Returns the toString of a object.
     */
    public static String toString(Object o) {
        return toString(o, new StringBuffer()).toString();
    }

    /**
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     * @return Returns the toString of a object.
     */
    public static StringBuffer toString(Object o, StringBuffer buf) {
        return INITIAL.handle(o, buf);
    }
}