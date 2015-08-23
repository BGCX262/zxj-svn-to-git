/*
 * Created on Feb 20, 2004
 * $Id: CacheException.java,v 1.1.2.3 2005/05/20 15:02:21 mike Exp $
 */
package org.zxframework.cache;

import org.zxframework.exception.NestableRuntimeException;

/**
 * Something went wrong in the cache.
 */
public class CacheException extends NestableRuntimeException {
    
    /**
     * @param s The error message.
     */
    public CacheException(String s) {
        super(s);
    }

    /**
     * @param s The error message
     * @param e The cause of the error.
     */
    public CacheException(String s, Exception e) {
        super(s, e);
    }
    
    /**
     * @param e The cause of the error.
     */
    public CacheException(Exception e) {
        super(e);
    }
}