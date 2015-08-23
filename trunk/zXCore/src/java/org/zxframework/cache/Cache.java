//$Id: Cache.java,v 1.1.2.1 2005/05/12 16:23:54 mike Exp $
package org.zxframework.cache;

import java.util.Map;

/**
 * Implementors define a caching algorithm. All implementors
 * <b>must</b> be threadsafe.
 */
public interface Cache {
    
    /**
     * Get an item from the cache.
     * 
     * @param key The key to use to retrieve the cached element.
     * @return the cached object or <tt>null</tt>
     * @throws CacheException Thrown if get fails.
     */
    public Object get(Object key) throws CacheException;
    
    /**
     * Add an item to the cache, nontransactionally, with
     * failfast semantics.
     * 
     * @param key The key to store.
     * @param value The value to cache.
     * @throws CacheException Thrown if put fails.
     */
    public void put(Object key, Object value) throws CacheException;
    
    /**
     * Add an item to the cache
     * @param key
     * @param value
     * @throws CacheException
     */
    public void update(Object key, Object value) throws CacheException;
    
    /**
     * Remove an item from the cache
     * 
     * @param key The key of the cache object to remove. 
     * @throws CacheException Thrown if remove fails.
     */
    public void remove(Object key) throws CacheException;
    
    /**
     * Clear the cache
     * 
     * @throws CacheException Thrown if clear fails.
     */
    public void clear() throws CacheException;
    
    /**
     * Clean up
     * 
     * @throws CacheException Thrown if destroy fails.
     */
    public void destroy() throws CacheException;
    
    /**
     * If this is a clustered cache, lock the item
     * 
     * @param key The key of the cache item to lock. 
     * @throws CacheException Thrown if lock fails.
     */
    public void lock(Object key) throws CacheException;
    
    /**
     * If this is a clustered cache, unlock the item
     * 
     * @param key The key of the cache item to unlock. 
     * @throws CacheException Thrown if unlock fails.
     */
    public void unlock(Object key) throws CacheException;
    
    /**
     * Generate a timestamp
     * 
     * @return Returns the generated timestamp. 
     */
    public long nextTimestamp();
    
    /**
     * Get a reasonable "lock timeout"
     * 
     * @return Returns the resonable lock timeout. 
     */
    public int getTimeout();
    
    /**
     * Get the name of the cache region
     * @return Returns the name of the cache region.
     */
    public String getRegionName();

    /**
     * The number of bytes is this cache region currently consuming in memory.
     *
     * @return The number of bytes consumed by this region; -1 if unknown or
     * unsupported.
     */
    public long getSizeInMemory();

    /**
     * The count of entries currently contained in the regions in-memory store.
     *
     * @return The count of entries in memory; -1 if unknown or unsupported.
     */
    public long getElementCountInMemory();

    /**
     * The count of entries currently contained in the regions disk store.
     *
     * @return The count of entries on disk; -1 if unknown or unsupported.
     */
    public long getElementCountOnDisk();
    
    /**
     * optional operation
     * @return Returns the map.
     */
    public Map toMap();
}