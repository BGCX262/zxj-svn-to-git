package org.zxframework.util;

import org.apache.commons.beanutils.Converter;

/**
 * Converts text to Boolean. This is richer than the currently available ones.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class BooleanConverter implements Converter {

	/**
	 * Default constructor
	 */
	public BooleanConverter() {
		super();
	}
	
	/**
	 * @see org.apache.commons.beanutils.Converter#convert(java.lang.Class, java.lang.Object)
	 */
	public Object convert(Class type, Object value) {
		if (value instanceof String) {
			boolean convert = StringUtil.booleanValue((String)value);
			return new Boolean(convert);
		}
		
		/**
		 * We will try our best, but this is most probably a bug.
		 */
		boolean convert = StringUtil.booleanValue(value.toString());
		return new Boolean(convert);
	}
}