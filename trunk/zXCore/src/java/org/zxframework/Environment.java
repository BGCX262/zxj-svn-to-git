package org.zxframework;

/**
 * Provides access to configuration info passed in <tt>Properties</tt> objects.
 */
public class Environment {
    
    /**
     * <code>CONFIG_FILE</code> - The full path to the zX core configuration file.
     */
    public static final String CONFIG_FILE = "zx.cfg.path";
    
    /**
     * <code>CONFIG_FILE</code> - The jndi name that we can find the settings for zx.
     * May need to change this.
     */
    public static final String WEB_CONFIG = "java:comp/env/configFile";
    
    /**
     * Enable the global cache (disabled by default)
     */
    public static final String USE_CACHE = "zx.cache.user_cache";
    
    /**
     * The name of the region of cache we are using for zX Core.
     */
    public static final String CACHE_NAME = "zx.cache.region_name";
    
    /**
     * The <tt>CacheProvider</tt> implementation class
     */
    public static final String CACHE_PROVIDER = "zx.cache.provider_class";
    
    /**
     * The key for the cache zX config dom object.
     */
    public static final String CONFIG_DOM = "zxconfig";
    
    /**
     * The default language for a zX application.
     * NOTE: This should be configuarable.
     */
    public static final String DEFAULT_LANGUAGE = "EN";
}