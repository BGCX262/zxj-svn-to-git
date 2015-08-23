/*
 * Created on Jun 12, 2005
 * $Id: ToStringBuilder.java,v 1.1.2.2 2005/06/17 20:16:57 mike Exp $
 */
package org.zxframework.util;

import org.apache.commons.lang.enum.Enum;

/**
 * <p>
 * Assists in implementing {@link Object#toString()} methods.
 * </p>
 * 
 * <p>
 * This class enables a good and consistent <code>toString()</code> to be
 * built for any class or object. This class aims to simplify the process by:
 * </p>
 * <ul>
 * <li>allowing field names</li>
 * <li>handling all types consistently</li>
 * <li>handling nulls consistently</li>
 * <li>outputting arrays and multi-dimensional arrays</li>
 * <li>enabling the detail level to be controlled for Objects and Collections</li>
 * <li>handling class hierarchies</li>
 * </ul>
 * 
 * <p>
 * To use this class write code as follows:
 * </p>
 * 
 * <pre>
 * 
 *  public class Person {
 *    String name;
 *    int age;
 *    boolean isSmoker;
 *  
 *    ...
 *  
 *    public String toString() {
 *      return new ToStringBuilder(this).
 *        append(&quot;name&quot;, name).
 *        append(&quot;age&quot;, age).
 *        append(&quot;smoker&quot;, smoker).
 *        toString();
 *    }
 *  }
 *  
 * </pre>
 * 
 * <p>
 * This will produce a toString of the format:
 * <code>Person@7f54[name=Stephen,age=29,smoker=false]</code>
 * </p>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @version 0.01
 */
public class ToStringBuilder {
	
	//------------------------ Members
	
	/** Internal buffer holding the toString. */
	private StringBuffer buffer = new StringBuffer();
	
	/** The mode in which we build the toString*/
	private int mode;
	
	/** The number of fields built. */
	private int fieldCount;
	
	/** The default method for building a string. */
	protected static int defaultMode;
	
	//------------------------ Constants
	
	/** Include the full package name when printing the class name */
	protected static final int INCLUDE_PACKAGE_PREFIX = 0x1;
	/** Print the hashcode of the object. */
	protected static final int INCLUDE_HASHCODE = 0x02;
	
	/** Begin of class */
	public static final String CLASS_BEGIN = " [";
	/** <code>CLASS_END</code> - End of toString */
	public static final char CLASS_END = ']';
	
	//------------------------ Constructors
	
	/**
	 * Constructor for ToStringBuilder.
	 * 
	 * @param target the Object to build a toString for
	 */
	public ToStringBuilder(Object target) {
		this(target, defaultMode);
	}
	
	/**
	 * Constructor for ToStringBuilder specifying the output style.
	 * 
	 * @param pobjTarget the Object to build a toString for
	 * @param pintMode the style of the toString to create, may be null
	 */
	protected ToStringBuilder(Object pobjTarget, int pintMode) {
		this.mode = pintMode;
		
		appendClassName(pobjTarget);
		appendHashCode(pobjTarget);
	}
	
	//------------------------ Public methods
	
	/**
	 * Append to the toString a boolean value.
	 * 
	 * @param pstrFieldName the field name
	 * @param value the value to add to the toString
	 * @return Returns a reference to this ToStringBuilder
	 */
	public ToStringBuilder append(String pstrFieldName, boolean value) {
		appendValue(pstrFieldName, String.valueOf(value));
		
		return this;
	}
	
	/**
	 * Append to the toString a byte value.
	 * 
	 * @param pstrFieldName the field name
	 * @param value the value to add to the toString
	 * @return Returns a reference to this ToStringBuilder
	 */
	public ToStringBuilder append(String pstrFieldName, byte value) {
		appendValue(pstrFieldName, String.valueOf(value));
		
		return this;
	}
	
	/**
	 * Append to the toString a short value.
	 * 
	 * @param pstrFieldName the field name
	 * @param value the value to add to the toString
	 * @return Returns a reference to this ToStringBuilder
	 */
	public ToStringBuilder append(String pstrFieldName, short value) {
		appendValue(pstrFieldName, String.valueOf(value));
		
		return this;
	}

	/**
	 * Append to the toString an int value.
	 * 
	 * @param pstrFieldName the field name
	 * @param value the value to add to the toString
	 * @return Returns a reference to this ToStringBuilder
	 */
	public ToStringBuilder append(String pstrFieldName, int value) {
		appendValue(pstrFieldName, String.valueOf(value));
		
		return this;
	}
	
	/**
	 * Append to the toString an long value.
	 * 
	 * @param pstrFieldName the field name
	 * @param value the value to add to the toString
	 * @return Returns a reference to this ToStringBuilder
	 */
	public ToStringBuilder append(String pstrFieldName, long value) {
		appendValue(pstrFieldName, String.valueOf(value));
		
		return this;
	}
	
	/**
	 * Append to the toString a Object value.
	 * 
	 * @param pstrFieldName the field name
	 * @param pobjValue the value to add to the toString
	 * @return Returns a reference to this ToStringBuilder
	 */
	public ToStringBuilder append(String pstrFieldName, Object pobjValue) {
		if (pobjValue instanceof Enum) {
			appendEnum(pstrFieldName, (Enum)pobjValue);
		} else {
			appendValue(pstrFieldName, String.valueOf(pobjValue));
		}
		
		return this;
	}
	
	/**
	 * Append to the toString a String value.
	 * 
	 * We will include inverted commas for better presentation.
	 * 
	 * @param pstrFieldName the field name
	 * @param pstrValue the value to add to the toString
	 * @return Returns a reference to this ToStringBuilder
	 */
	public ToStringBuilder append(String pstrFieldName, String pstrValue) {
		if (pstrValue != null) pstrValue = '\'' + pstrValue + '\'';
		appendValue(pstrFieldName, pstrValue);
		
		return this;
	}
	
	/**
	 * Append to the toString a boolean value.
	 * 
	 * @param pstrSuper The super object.
	 * @return Returns a reference to this ToStringBuilder
	 */
	public ToStringBuilder appendSuper(String pstrSuper) {
		if (pstrSuper != null) {
			
			if (fieldCount++ == 0) {
				buffer.append(CLASS_BEGIN);
			} else {
				buffer.append(',');
			}
			
			/**
			 * Clean up the super toString.
			 */
			int intIndexOf = pstrSuper.indexOf(ToStringBuilder.CLASS_BEGIN);
			if (intIndexOf != -1 && pstrSuper.endsWith(String.valueOf(ToStringBuilder.CLASS_END))) {
				pstrSuper = pstrSuper.substring(intIndexOf + ToStringBuilder.CLASS_BEGIN.length(), pstrSuper.length()-1);
			}
			
			System.out.println(pstrSuper);

			buffer.append(pstrSuper);
		}
		
		return this;
	}
	
	//------------------------ Private methods
	
	/**
	 * Append to the toString a String value.
	 * 
	 * We will include inverted commas for better presentation.
	 * 
	 * @param pstrFieldName the field name
	 * @param pobjValue the value to add to the toString
	 */
	private void appendEnum(String pstrFieldName, Enum pobjValue) {
		String strValue;
		if (pobjValue != null) {
			strValue = pobjValue.getName();
		} else {
			strValue = String.valueOf(pobjValue);
		}
		append(pstrFieldName, strValue);
	}
	
	/**
	 * Add the classname to the toString.
	 * 
	 * @param the Object to build a toString for
	 */
	private void appendClassName(Object target) {
		String className = target.getClass().getName();
		
		if ((mode & INCLUDE_PACKAGE_PREFIX) != 0) {
			buffer.append(className);
			return;
		}
		
		int lastdotx = className.lastIndexOf('.');
		className = className.replace('$', '.');
		buffer.append(className.substring(lastdotx + 1));
	}
	
	/**
	 * Add the hashcode of the object to the toString.
	 * 
	 * @param target the Object to build a toString for
	 */
	private void appendHashCode(Object target) {
		if ((mode & INCLUDE_HASHCODE) == 0) {
			return;
		}
		
		buffer.append('@');
		buffer.append(Integer.toHexString(target.hashCode()));
	}
	
	/**
	 * Append a single field and value to the toString.
	 * 
	 * @param pstrFieldName The field name.
	 * @param pstrValue The field value.
	 */
	private void appendValue(String pstrFieldName, String pstrValue) {
		if (fieldCount++ == 0) {
			buffer.append(CLASS_BEGIN);
		} else {
			buffer.append(',');
		}
		
		buffer.append(pstrFieldName);
		buffer.append('=');
		buffer.append(pstrValue);
	}
	
	//------------------------ Object overidden methods.
	
	/**
	 * Returns the final assembled string. This may only be invoked once, after
	 * all attributes have been appended.
	 * 
	 * @return Returns the final assembled string.
	 */
	public String toString() {
		if (fieldCount > 0) {
			buffer.append(CLASS_END);
		}

		String result = buffer.toString();
		buffer = null;
		return result;
	}

}