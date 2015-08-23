//$Id: CacheProvider.java,v 1.1.2.1 2005/05/12 16:23:54 mike Exp $
package org.zxframework.cache;

import java.util.Properties;

/**
 * Support for pluggable caches.
 *
 * @author Gavin King
 */
public interface CacheProvider {

    /**
     * Configure the cache
     *
     * @param regionName the name of the cache region
     * @param properties configuration settings
     * @return Returns the cache object.
     * @throws CacheException Thrown if buildCache fails.
     */
    public Cache buildCache(String regionName, Properties properties) throws CacheException;

    /**
     * Generate a timestamp.
     *
     * @return Returns the generated timestamp.
     */
    public long nextTimestamp();

    /**
     * Callback to perform any necessary initialization of the underlying cache implementation
     * during SessionFactory construction.
     *
     * @param properties current configuration settings.
     * @throws CacheException Thrown if start fails.
     */
    public void start(Properties properties) throws CacheException;

    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    public void stop();
    
    /**
     * @return Returns wheather Minimal puts is enabled by default.
     */
    public boolean isMinimalPutsEnabledByDefault();

}