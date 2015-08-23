/*
 * Created on Sep 7, 2004
 * $Id: ThreadLocalZX.java,v 1.1.2.6 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

/**
 * Local Thread Copy of the zX instance.
 * 
 * <pre>
 * 
 * This is the best way of "passing" around a handle to zx to every class that
 * extends zXObject. 
 * 
 * However this may coz problems with spawning off multiple threads
 * and may coz performance problems. When it comes to create stateless objects
 * that need a handle to zX you will have to use another means of passing zX 
 * around.
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class ThreadLocalZX {
    
    private static ThreadLocal zx = new ThreadLocal();

    /**
     * Hide constructor.
     */
    private ThreadLocalZX() {
    	super();
    }

    /**
     * @return Returns the ThreadLocal copy of zx.
     */
    public static ZX getZX() {
        return (ZX)zx.get();
    }

    /**
     * ZX the local thread variable for zx.
     * 
     * @param zx Do this once per zx instance per thread only !.
     */
    public static void setZX(ZX zx) {
        ThreadLocalZX.zx.set(zx);
    }    
}