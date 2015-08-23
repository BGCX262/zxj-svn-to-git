package org.zxframework.cache;

import java.util.Properties;

/**
 * A simple in-memory Hashtable-based cache impl.
 * 
 * @author Gavin King
 */
public class HashtableCacheProvider implements CacheProvider {

    /**
     * @see org.zxframework.cache.CacheProvider#buildCache(java.lang.String, java.util.Properties)
     */
    public Cache buildCache(String regionName, Properties properties) throws CacheException {
        return new HashtableCache( regionName );
    }

    /**
     * @see org.zxframework.cache.CacheProvider#nextTimestamp()
     */
    public long nextTimestamp() {
        return Timestamper.next();
    }

    /**
     * Callback to perform any necessary initialization of the underlying cache implementation
     * during SessionFactory construction.
     *
     * @param properties current configuration settings.
     * @throws CacheException Thrown if start fails.
     */
    public void start(Properties properties) throws CacheException {
    	// Not implemented
    }

    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    public void stop() {
    	// Not implemented
    }

    /**
     * @see org.zxframework.cache.CacheProvider#isMinimalPutsEnabledByDefault()
     */
    public boolean isMinimalPutsEnabledByDefault() {
        return false;
    }
}