/*
 * Created on Mar 16, 2004
 * $Id: Director.java,v 1.1.2.1 2005/04/08 09:18:36 mike Exp $
 */
package org.zxframework.director;

import org.zxframework.ZXException;

/**
 * This is the interface for all directors like #pk,#inqs etc..
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public interface Director {

    /**
     * @param pobjFunc The fuction to perform.
     * @return Returns the result of the director
     * @throws ZXException Thrown if the constructor fails
     */
    public String exec(DirectorFunction pobjFunc) throws ZXException;
}