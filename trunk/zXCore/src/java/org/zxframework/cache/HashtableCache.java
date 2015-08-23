package org.zxframework.cache;

import java.util.Collections;
import java.util.Hashtable;

import java.util.Map;

/**
 * A lightweight implementation of the <tt>Cache</tt> interface
 * @author Gavin King
 */
public class HashtableCache implements Cache {
    
    private final Map hashtable = new Hashtable();
    private final String regionName;
    
    /**
     * @param regionName The name of the cache region.
     */
    public HashtableCache(String regionName) {
        this.regionName = regionName;
    }

    /**
     * @see org.zxframework.cache.Cache#getRegionName()
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * @param key The key of the cache.
     * @return Returns the cached object.
     * @throws CacheException Thrown if read fails.
     */
    public Object read(Object key) throws CacheException {
        return hashtable.get(key);
    }

    /**
     * @see org.zxframework.cache.Cache#get(java.lang.Object)
     */
    public Object get(Object key) throws CacheException {
        return hashtable.get(key);
    }

    /**
     * @see org.zxframework.cache.Cache#update(java.lang.Object, java.lang.Object)
     */
    public void update(Object key, Object value) throws CacheException {
        put(key, value);
    }
    
    /**
     * @see org.zxframework.cache.Cache#put(java.lang.Object, java.lang.Object)
     */
    public void put(Object key, Object value) throws CacheException {
        hashtable.put(key, value);
    }

    /**
     * @see org.zxframework.cache.Cache#remove(java.lang.Object)
     */
    public void remove(Object key) throws CacheException {
        hashtable.remove(key);
    }

    /**
     * @see org.zxframework.cache.Cache#clear()
     */
    public void clear() throws CacheException {
        hashtable.clear();
    }

    /**
     * @see org.zxframework.cache.Cache#destroy()
     */
    public void destroy() throws CacheException {
    	// Not implemented
    }

    /**
     * @see org.zxframework.cache.Cache#lock(java.lang.Object)
     */
    public void lock(Object key) throws CacheException {
        // local cache, so we use synchronization
    }

    /**
     * @see org.zxframework.cache.Cache#unlock(java.lang.Object)
     */
    public void unlock(Object key) throws CacheException {
        // local cache, so we use synchronization
    }

    /**
     * @see org.zxframework.cache.Cache#nextTimestamp()
     */
    public long nextTimestamp() {
        return Timestamper.next();
    }

    /**
     * @see org.zxframework.cache.Cache#getTimeout()
     */
    public int getTimeout() {
        return Timestamper.ONE_MS * 60000; //ie. 60 seconds
    }

    /**
     * @see org.zxframework.cache.Cache#getSizeInMemory()
     */
    public long getSizeInMemory() {
        return -1;
    }

    /**
     * @see org.zxframework.cache.Cache#getElementCountInMemory()
     */
    public long getElementCountInMemory() {
        return hashtable.size();
    }

    /**
     * @see org.zxframework.cache.Cache#getElementCountOnDisk()
     */
    public long getElementCountOnDisk() {
        return 0;
    }
    
    /**
     * @see org.zxframework.cache.Cache#toMap()
     */
    public Map toMap() {
        return Collections.unmodifiableMap(hashtable);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "HashtableCache(" + regionName + ')';
    }
}