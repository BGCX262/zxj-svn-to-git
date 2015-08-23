package org.zxframework.cache;

import java.util.Properties;

import org.zxframework.util.PropertiesUtil;
import org.zxframework.util.StringUtil;

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.Config;

/**
 * Support for OpenSymphony OSCache. 
 * 
 * This implementation assumes that identifiers have well-behaved <tt>toString()</tt> methods.
 * 
 * @author <a href="mailto:m.bogaert@intrasoft.be">Mathias Bogaert</a>
 */
public class OSCacheProvider implements CacheProvider {

    /** 
     * The <tt>OSCache</tt> refresh period property suffix. 
     */
    public static final String OSCACHE_REFRESH_PERIOD = "refresh.period";
    /** 
     * The <tt>OSCache</tt> CRON expression property suffix. 
     */
    public static final String OSCACHE_CRON = "cron";
    
    private static final Properties OSCACHE_PROPERTIES = new Config().getProperties();

    /**
     * Builds a new {@link Cache} instance, and gets it's properties from the OSCache {@link Config}
     * which reads the properties file (<code>oscache.properties</code>) from the classpath.
     * If the file cannot be found or loaded, an the defaults are used.
     *
     * @param region The cache region
     * @param properties The properties setting to load.
     * @return Returns the cache object for OSCache.
     * @throws CacheException Thrown if buildCache fails.
     */
    public Cache buildCache(String region, Properties properties) throws CacheException {

        int refreshPeriod = PropertiesUtil.getInt(
                                    StringUtil.qualify(region, OSCACHE_REFRESH_PERIOD), 
                                    OSCACHE_PROPERTIES, 
                                    CacheEntry.INDEFINITE_EXPIRY
                                    );
        String cron = OSCACHE_PROPERTIES.getProperty(StringUtil.qualify(region, OSCACHE_CRON));

        // construct the cache        
        return new OSCache(refreshPeriod, cron, region);
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