/*
 * Created on Jun 12, 2004
 * $Id: PageflowExpressionFunction.java,v 1.1.2.3 2005/07/11 08:08:15 mike Exp $
 */
package org.zxframework.web.expression;

import org.zxframework.expression.ExpressionFunction;
import org.zxframework.web.Pageflow;

/**
 * Pageflow expresion function. This is a special expression function type 
 * at it needs a handle to the Pageflow object.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class PageflowExpressionFunction extends ExpressionFunction {

    //------------------------ Members
    
    private Pageflow pageflow;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PageflowExpressionFunction() {
        super();
    }
    
    //------------------------ Getters and Setters
    
    /**
     * @return Returns the pageflow.
     */
    public Pageflow getPageflow() {
        return pageflow;
    }
    
    /**
     * @param pageflow The pageflow to set.
     */
    public void setPageflow(Pageflow pageflow) {
        this.pageflow = pageflow;
    }
}