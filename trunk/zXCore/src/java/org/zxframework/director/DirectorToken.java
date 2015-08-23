/*
 * Created on Mar 17, 2004
 * $Id: DirectorToken.java,v 1.1.2.2 2005/07/12 07:28:43 mike Exp $
 */
package org.zxframework.director;

import org.zxframework.ZXObject;
import org.zxframework.zXType;

/**
 * Single token in parsed director.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1 */
public class DirectorToken extends ZXObject {
    
    //------------------------ Members
    
    private zXType.drctrNextToken nextToken;
    private String token;
    private int position;
    
    //------------------------ Constructor
    
    /**
     * Allow for the default constructor.
     */
    public DirectorToken() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    /**
     * @return Returns the position.
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * @param position The position to set.
     */
    public void setPosition(int position) {
        this.position = position;
    }
    
    /**
     * @return Returns the nextToken.
     */
    public zXType.drctrNextToken getNextToken() {
        return nextToken;
    }
    
    /**
     * @param nextToken The nextToken to set.
     */
    public void setNextToken(zXType.drctrNextToken nextToken) {
        this.nextToken = nextToken;
    }
    
    /**
     * @return Returns the token.
     */
    public String getToken() {
        return token;
    }
    
    /**
     * @param token The token to set.
     */
    public void setToken(String token) {
        this.token = token;
    }
}