package org.zxframework.cache;

import java.util.Map;
import java.util.Properties;

import org.zxframework.util.PropertiesUtil;
import org.zxframework.util.StringUtil;

import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * Adapter for the OSCache implementation.
 * 
 * @author <a href="mailto:m.bogaert@intrasoft.be">Mathias Bogaert</a>
 */
public class OSCache implements Cache {
    
    /** 
     * The <tt>OSCache</tt> cache capacity property suffix. 
     */
    public static final String OSCACHE_CAPACITY = "cache.capacity";

    private static final Properties OSCACHE_PROPERTIES = new Config().getProperties();
    
    /** 
     * The OSCache 2.0 cache administrator. 
     */
    private static GeneralCacheAdministrator cache = new GeneralCacheAdministrator();

    private static Integer capacity = PropertiesUtil.getInteger(OSCACHE_CAPACITY, OSCACHE_PROPERTIES);

    static {
        if (capacity != null) cache.setCacheCapacity(capacity.intValue());
    }
    
    private final int refreshPeriod;
    private final String cron;
    private final String regionName;
    private final String[] regionGroups;
    
    private String toString(Object key) {
        return String.valueOf(key) + StringUtil.DOT + regionName;
    }

    /**
     * @param refreshPeriod
     * @param cron
     * @param region
     */
    public OSCache(int refreshPeriod, String cron, String region) {
        this.refreshPeriod = refreshPeriod;
        this.cron = cron;
        this.regionName = region;
        this.regionGroups = new String[] {region};
    }

    /**
     * @see org.zxframework.cache.Cache#get(java.lang.Object)
     */
    public Object get(Object key) throws CacheException {
        try {
            return cache.getFromCache(toString(key), refreshPeriod, cron);
        } catch (NeedsRefreshException e) {
            cache.cancelUpdate(toString(key));
            return null;
        }
    }
    
    /**
     * @param key The key to use.
     * @return Returns the object in cache.
     * @throws CacheException Thrown if read fails.
     */
    public Object read(Object key) throws CacheException {
        return get(key);
    }
    
    /**
     * @see org.zxframework.cache.Cache#put(java.lang.Object, java.lang.Object)
     */
    public void put(Object key, Object value) throws CacheException {
        cache.putInCache( toString(key), value, regionGroups );
    }

    /**
     * @see org.zxframework.cache.Cache#remove(java.lang.Object)
     */
    public void remove(Object key) throws CacheException {
        cache.flushEntry( toString(key) );
    }

    /**
     * @see org.zxframework.cache.Cache#clear()
     */
    public void clear() throws CacheException {
        cache.flushGroup(regionName);
    }
    
    /**
     * @see org.zxframework.cache.Cache#destroy()
     */
    public void destroy() throws CacheException {
        synchronized (cache) {
            cache.destroy();
        }
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
        return CacheEntry.INDEFINITE_EXPIRY;
    }

    /**
     * @see org.zxframework.cache.Cache#update(java.lang.Object, java.lang.Object)
     */
    public void update(Object key, Object value) throws CacheException {
        put(key, value);
    }
    
    /**
     * @see org.zxframework.cache.Cache#getRegionName()
     */
    public String getRegionName() {
        return regionName;
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
        return -1;
    }
    
    /**
     * @see org.zxframework.cache.Cache#getElementCountOnDisk()
     */
    public long getElementCountOnDisk() {
        return -1;
    }

    /**
     * @see org.zxframework.cache.Cache#toMap()
     */
    public Map toMap() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "OSCache(" + regionName + ')';
    }
}