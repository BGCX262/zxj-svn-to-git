/*
 * Created on Jun 28, 2004 by michael
 * $Id: RecTreeExpressionFunction.java,v 1.1.2.3 2005/05/12 14:18:51 mike Exp $
 */
package org.zxframework.misc;

import org.zxframework.expression.ExpressionFunction;

/**
 * RecTree expression function.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class RecTreeExpressionFunction extends ExpressionFunction {
    
    //-------------------------------------- Members
    
	/**
	 * NOTE : To much so code 1.3 compatible we need to make these members protected
	 * and not private.
	 */
    protected RecTreeNode node;
    protected RecTreeNode root;
    
    //--------------------------------------- Constructors

    /**
     * Default constructor.
     */
    public RecTreeExpressionFunction() {
        super();
    }
    
    //--------------------------------------- Getters and Setters
    
    /**
     * @param node The node to set.
     */
    public void setNode(RecTreeNode node) {
        this.node = node;
    }
    
    /**
     * @param root The root to set.
     */
    public void setRoot(RecTreeNode root) {
        this.root = root;
    }    
}