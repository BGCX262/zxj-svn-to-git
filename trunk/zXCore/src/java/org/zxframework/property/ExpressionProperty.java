/*
 * Created on Feb 20, 2004 by michael
 * $Id: ExpressionProperty.java,v 1.1.2.3 2006/07/17 16:17:27 mike Exp $
 */
package org.zxframework.property;

/**
 * ExpressionProperty - Is that property representing an Expression.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ExpressionProperty extends StringProperty {

    /**
     * Default constructor.
     */
    public ExpressionProperty() {
    	super();
    }
    
	/**
	 * Instant value property for doubles values.
	 * 
	 * <pre>
	 * 
	 * Using static property are not yet supported. This constructor is used in special cases where you 
	 * just want a property object with none of the over head. So the create property will not have an
	 * associated property.
	 * </pre>
	 * 
	 * @param pstrValue The Native type to set the value to
	 * @param pblnIsNull Is the value of the property null ?
	 */
	public ExpressionProperty(String pstrValue, boolean pblnIsNull) {
	    
	    if (pstrValue != null) {
		    this.value = pstrValue;
		    isNull = pblnIsNull;
	    } else {
	        this.value = "";
	        isNull = true;
	    }
	    
	}
}