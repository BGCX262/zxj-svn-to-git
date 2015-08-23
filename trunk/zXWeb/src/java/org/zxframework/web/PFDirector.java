/*
 * Created on Mar 15, 2004 by Michael Brewer
 * $Id: PFDirector.java,v 1.1.2.10 2006/07/17 16:29:24 mike Exp $  
 */
package org.zxframework.web;

import org.zxframework.CloneableObject;
import org.zxframework.util.ToStringBuilder;

/**
 * Pageflow director object.
 * 
 * <pre>
 * 
 * NOTE : We will refactor this to a Tuple and move "constructQSEntry" into the Pageflow object
 *  
 * The key of this zXObject  is &quot;destination&quot;. 
 *           
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFDirector implements CloneableObject {
    
    //------------------------ Members

    private String source;
    private String destination;

    //------------------------ Constructor

    /**
     * Default constructor.
     */
    public PFDirector() {
        super();
    }

    //------------------------ Getters and Setters.

    /**
     * @return Returns the destination.
     */
    public String getDestination() {
        return this.destination;
    }

    /**
     * @param destination The destination to set.
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @return Returns the source.
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source The source to set.
     */
    public void setSource(String source) {
        this.source = source;
    }

    //------------------------ Public Methods
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFDirector objPFDirector = new PFDirector();
        objPFDirector.setDestination(getDestination());
        objPFDirector.setSource(getSource());
        return objPFDirector;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
		toString.append("destination", getDestination());
		toString.append("source", getSource());
		
        return toString.toString();
    }
}