/*
 * Created on Apr 9, 2004 by Michael Brewer
 * $Id: PFUrl.java,v 1.1.2.14 2005/11/21 15:18:31 mike Exp $ 
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.CloneableObject;
import org.zxframework.zXType;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.ToStringBuilder;

/**
 * Pageflow URL object.
 * 
 * <pre>
 * 
 * NOTE: url is the key of this ZXObject object.
 * 
 * Change    : DGS13FEB2003
 * Why       : Added 'active' property and extended action types by adding 'popup' type.
 * 
 * Change    : DGS13JUN2003
 * Why       : Added Comment
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFUrl implements CloneableObject {
    
    //------------------------ Members
    
	private String url;
    private zXType.pageflowUrlType urlType;
    private String urlclose;
    private String frameno;
    /** DGS13FEB2003: Added active property */
    private String active;
    /** BD12NOV02 Pre-allocate for ease of use */
    private List querystring;
    /** DGS13JUN2003 Added Comment */
    private String comment;
    
    //------------------------ Constructor
    
    /**
     * Default constructor.
     */
    public PFUrl() {
        super();
        
        /**
         * Set the default url ty[e to fixed.
         */
        setUrlType(zXType.pageflowUrlType.putFixed);
    }
    
    //------------------------ Getters and Setters
    
    /**
     * Gets the url. 
     * 
     * NOTE : This is also the key of this ZXObject.
     * 
     * @return Returns the url.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets the url of the PFUrl.
     *  
     * @param url The url to set.
     */
    public void setUrl(String url) {
        this.url = url;
        
        // Converting this object to a non ZXObject.
        // setKey(url);
    }

    /**
     * Get the actual Enum of the pageflowurltype.
     * 
     * @return Returns the urlType.
     */
    public zXType.pageflowUrlType getUrlType() {
        return urlType;
    }

    /**
     * @param urlType The urlType to set.
     */
    public void setUrlType(zXType.pageflowUrlType urlType) {
        this.urlType = urlType;
    }

    /**
     * @return Returns the active.
     */
    public String getActive() {
        return active;
    }

    /**
     * @param active The active to set.
     */
    public void setActive(String active) {
        this.active = active;
    }

    /** 
     * @return Returns the comment. 
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /** 
     * @return Returns the frameno. 
     */
    public String getFrameno() {
        return frameno;
    }

    /**
     * @param frameno The frameno to set.
     */
    public void setFrameno(String frameno) {
        this.frameno = frameno;
    }

    /** 
     * A collection (PFDirector) of Query String Directors.
     * 
     * @return Returns the queryString (A Collection of PFDirector).
     **/
    public List getQuerystring() {
        return querystring;
    }

    /**
     * @param querystring The querystring to set.
     */
    public void setQuerystring(List querystring) {
        this.querystring = querystring;
    }

    /** 
     * @return Returns the urlclose. 
     */
    public String getUrlclose() {
        return urlclose;
    }

    /**
     * @param urlclose The urlclose to set.
     */
    public void setUrlclose(String urlclose) {
        this.urlclose = urlclose;
    }
    
    //------------------------ Digester helper methods.
    
    /**
     * Sets the Urltype of the PFUrl.
     * 
     * <pre>
     * 
     *  NOTE : This actually sets the Enum pageflowUrlType and sets the
     *  urlType to the cleaned string value. 
     * </pre>
     * 
     * @param urltype The urltype to set.
     */
    public void setUrltype(String urltype) {
        this.urlType = zXType.pageflowUrlType.getEnum(urltype);
    }
    
    //------------------------ Object overidden methods.
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFUrl objPFUrl  = new PFUrl();
        
        objPFUrl.setActive(getActive());
        objPFUrl.setComment(getComment());
        objPFUrl.setFrameno(getFrameno());
        
        if (this.querystring != null && this.querystring.size() > 0) {
            objPFUrl.setQuerystring(CloneUtil.clone((ArrayList)this.querystring));
        }
        
        objPFUrl.setUrl(getUrl());
        objPFUrl.setUrlclose(getUrlclose());
        objPFUrl.setUrlType(getUrlType());
        
        return objPFUrl;
    }
    
    /** 
     * @see java.lang.Object#toString()
     **/
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);

        toString.append("url", getUrl());
        toString.append("urlclose", getUrlclose());
        toString.append("urltype", zXType.valueOf(getUrlType()));
        toString.append("active", getActive());
        toString.append("frameno", getFrameno());
        
        return toString.toString();
    }
}