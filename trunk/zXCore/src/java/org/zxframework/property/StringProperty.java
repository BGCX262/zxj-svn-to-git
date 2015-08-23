/*
 * Created on Feb 9, 2004 by michael
 * $Id: StringProperty.java,v 1.1.2.11 2006/07/17 16:17:43 mike Exp $
 */
package org.zxframework.property;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;

/**
 * StringProperty - Represents dataTypes : Strings and Expression.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class StringProperty extends Property {

    //------------------------ Members    
    
    /** The underlying data type used for storing string values. **/
	protected String value = "";
    
    //------------------------ Constructors      
	
	/**
	 * Default constructor
	 */
	public StringProperty() {
		super();
	}
    
    /**
     * Instant value property for String values.
     * 
     * <pre>
     * 
     * Using static property are not yet supported. This constructor is used in special cases where you 
     * just want a property object with none of the over head. So the create property will not have an
     * associated property.
     * </pre>
     * 
     * @param pstrValue The Native type to set the value to
     */
    public StringProperty(String pstrValue) {
        this.value = pstrValue;
        isNull = (pstrValue == null);
    }
    
	/**
	 * Instant value property for String values.
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
	public StringProperty(String pstrValue, boolean pblnIsNull) {
        
	    /**
	     * Handle null values :
	     */
	    if (pstrValue != null) {
		    this.value = pstrValue;
		    isNull = pblnIsNull;
	    } else {
	        this.value= "";
	        this.isNull = true;
	    }
	    
	}
	
    //------------------------ Public methods    
	
	/**
	 * setValue - Validate / massage / set string value.
	 * 
	 * @param pstrValue Value to set the property to.
	 * @return Returns the return code of the method.
	 * @throws ZXException If the setValue fails.
	 */
	public zXType.rc setValue(String pstrValue) throws ZXException {
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrValue", pstrValue);
		}
		
		zXType.rc setValue = zXType.rc.rcOK;
		
		try {
			/**
			 * Set defaults
			 */
		    if (pstrValue == null) {
		        pstrValue = "";		    
		    }
		    
		    /**
		     * Handle null values
		     */
		    if (this.isNull && (StringUtil.len(pstrValue) == 0)) {
                /**
                 * Handle null values.
                 */
                value = "";
		        setValue = handleNull();
		        if (setValue.pos == zXType.rc.rcError.pos) {
		            return setValue;
		        }
                
		    }
		    
			/**
			 * Convert to ucase / lcase if needed
			 */
			if (this.attribute.getTextCase().equals(zXType.textCase.tcLower)) {
				pstrValue = pstrValue.toLowerCase();
			} else if (this.attribute.getTextCase().equals(zXType.textCase.tcUpper)){
				pstrValue = pstrValue.toUpperCase();
			}
			
		    /**
		     *  Truncate at length
		     **/
			
			/**
		     * BD11DEC02: bit ugly: when the length >= 32000 we do not check on
		     * length
		     * This might be irralavent in java. :)
		     **/
		    if(this.attribute.getLength() < 32000) {
		        if(pstrValue.length() > this.attribute.getLength()) {
		            pstrValue = pstrValue.substring(0, this.attribute.getLength());
		        }
		    }
			
			/**
			 * Reset FKLabel (and FKBO) as it is no longer relevant
			 * when there is a new value; also take into consideration whether
			 * there is a FK label expression as the expression could be such that
			 * even if the FK attr has the same value (ie points to the same FK BO)
			 * the label is still different!
			 */
		    updateFKLabel(this.value, pstrValue);
		    
		    /**
		     * Validate only if needed
		     **/
		    if (!this.bo.isValidate()) {
                
		        //------------------------------------------- START OF TEMPLATE		        
		        if (!this.value.equals(pstrValue)) {
		            setPersistStatusDirty();
		        }
			    
		        this.value = pstrValue;
		        
		        if (this.value.trim().length() > 0) {
		            this.isNull = false;
		        } else {
		            this.isNull = true;
		        }
		        //------------------------------------------- END OF TEMPLATE
                
		    } else {
		    
		        //------------------------------------------- START OF TEMPLATE
		    	
			    /**
			     * Validation
			     * 
			     * First empty string (not allowed when not optional)
			     */
			    if (!this.attribute.isOptional() &&  pstrValue.length() == 0 ) {
			    	getZx().trace.userErrorAdd("Attribute " + this.attribute.getLabel().getLabel() 
			    					+ " is not optional for attribute name : " + this.attribute.getName());
			    	setValue = zXType.rc.rcError;
			    	return setValue;
			    }
			    
		        //------------------------------------------- END OF TEMPLATE
			    
			    /**
			     * If empty; no further validations required
			     **/
			    if(pstrValue.trim().length() == 0) {
			        if(!this.value.equals(pstrValue)) {
			            setPersistStatusDirty();
			        }
			        this.value = pstrValue;
			        this.isNull = true;
			    } else {
			    	
			        //------------------------------------------- START OF TEMPLATE
				    /**
				     * See if the suggested value match any of the options (if any). Needless
				     * to say that this is not required when we are dealing with a combo box
				     **/
				    if (this.attribute.getOptions() != null &&  this.attribute.getOptions().size() > 0 && !this.attribute.isCombobox()) {
				        if(!isValueInOptions(pstrValue)) {
				        	getZx().trace.userErrorAdd("Value " + pstrValue + "is not in the option list for " + this.attribute.getLabel().getLabel() 
				        			+ " for attribute name " + this.attribute.getName());
                            setValue = zXType.rc.rcError;
                            return setValue;
				        }
				    }
			        //------------------------------------------- END OF TEMPLATE

				    /**
				     * Check against regular expression
				     **/
				    if (StringUtil.len(this.attribute.getRegularExpression()) > 0) {
				    	/**
				         * We need a dummy test here otherwise the first test fails
				         **/
				    	Pattern pattern = Pattern.compile(this.attribute.getRegularExpression(), Pattern.CASE_INSENSITIVE);
				    	Matcher matcher = pattern.matcher(pstrValue);
				    	boolean blnMatch = matcher.matches();
				    	
				    	if (!blnMatch) {
				    		getZx().trace.userErrorAdd("Value for" + this.attribute.getLabel().getLabel() + " does not match pattern '" +
				    				pstrValue + "', pattern " + this.attribute.getRegularExpression() + " for attribute" + this.attribute.getName());
				    		setValue = zXType.rc.rcError;
				    		return setValue;
				    	}
				    }
				    
                    /**
                     * Check whether the value is in the range. This will throw an 
                     * exception if out of range
                     */
                    if (checkRange(pstrValue).pos == zXType.rc.rcError.pos) {
                        setValue = zXType.rc.rcError;
                        return setValue;
                    }
                    
					/**
					 * Check validity of the expression :
					 */
					if (this.attribute.getDataType().pos == zXType.dataType.dtExpression.pos) {
					    /**
					     * BD15SEP04 - Checksyntax is no changed and will have set the
					     * error msg, so all we have to do is set the attribute to force
					     * focus if the error is generated from an editform
					     */
					    try {
					        getZx().getExpressionHandler().checkSyntax(pstrValue);
					    } catch (Exception e) {
					        getZx().trace.setUserErrorAttr(this.attribute.getName());
					    }
					}
					
			        //------------------------------------------- START OF TEMPLATE
					
				    /**
				     * If the new value is different from the old value: set the
				     * persist status
				     **/
				    if (!this.value.equals(pstrValue)) {
				        setPersistStatusDirty();
				    }
				    
				    /**
				     * Now we feel save to assign the value
				     **/
				    this.value = pstrValue;
				    
				    this.isNull = false;
				    
			        //------------------------------------------- END OF TEMPLATE
			    }
		    }
		    
		    return setValue;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Set string value", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pstrValue = " + pstrValue);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			setValue = zXType.rc.rcError;
			return setValue;
		} finally {
			if (getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.exitMethod();
			}
		}
		
	}
	
	/**
	 * @return Returns the string value of the  property.
	 **/
	public String getValue() {
		return value;
	}
	
	/**
	 * Only call this when you are sure.
	 * 
	 * @param pstrValue The value to set the unlying data to.
	 */
	void setStringValue(String pstrValue) {
		this.value = pstrValue;
	}

	/***
	 * formattedValue - Return the current value as formatted string.
	 * 
	 * @param pblnTrim Trim the outputted string.
	 * @see org.zxframework.property.Property#formattedValue(boolean)
	 * @throws ZXException Thrown if setValue fails.
	 **/
	public String formattedValue(boolean pblnTrim) throws ZXException {
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pblnTrim", pblnTrim);
		}
		
		String formattedValue = null;
		
		try {
			
			//------------------------------------------- START COMMON CODE
			if (this.isNull) {
//				formattedValue = this.value;
//				
//				/**
//				 * If not trim: bring to approriate length and left align
//				 */
//				if(!pblnTrim) {
//					formattedValue = StringUtil.padString(formattedValue, ' ', this.attribute.getLength(), true);
//				}
				
			    formattedValue = "";
			    return formattedValue;
                
			} else if (this.attribute == null) {
			    /**
			     * If this property is not associated with any attribute we
			     * cannot do any fancy stuff (like formatting) as we do not have a clue
			     * what to do as all formatting related settings are held in the attribute
			     */
			    formattedValue = getStringValue();
			    
			} else {
				/**
				 * Not isnull
				 */
				
				/**
				 * If FKLabel set, return that
				 */
				if (StringUtil.len(this.attribute.getForeignKey()) > 0) {
				    formattedValue = fkformattedValue();
					return formattedValue;
				}
				//------------------------------------------- END COMMON CODE
				
				//------------------------------------------- START String specific stuff
				  
				//------------------------------------------- START SIMILIAR CODE
				if(this.attribute.getOptions() != null && this.attribute.getOptions().size() > 0) {
					formattedValue = optionValueToLabel();
                    
				} else {
					if (StringUtil.len(this.attribute.getOutputMask()) > 0) {
                         // TODO : Need to add support of outputMasks. 
                        // strReturn = format(strValue, .outputMask)
						formattedValue = this.value;
					} else {
						formattedValue = this.value;
					}
                    
				}
				//------------------------------------------- END SIMILIAR CODE
				
				/**
				 * Show as password when required
				 */
				if (this.attribute.isPassword()) {
					formattedValue = StringUtil.padString("",'*',formattedValue.length(),true);
				}
				
				/**
				 * If not trim: bring to approriate length and left align
				 */
				if(!pblnTrim) {
					formattedValue = StringUtil.padString(formattedValue, ' ', this.attribute.getOutputLength(),true);
				}
				
				//------------------------------------------- END String specific stuff
			}
			return formattedValue;
		} catch (Exception e) {
			getZx().trace.addError("Failed To : Return the current value as formatted string", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pblnTrim = " + pblnTrim);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return formattedValue;
		} finally {
			if (getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(formattedValue);
				getZx().trace.exitMethod();
			}
		}
	}
	
	//------------------------ Methods that should be implemented and called via super
	
	/**
	 * setValuesToDefault - Reset values to defaults.
	 */
	public void setValuesToDefault() {
		super.setValuesToDefault();
		
		this.value = "";
	}
	
	//------------------------ Property's overidden methods

    /** 
	 * toString - If requested raw: simply convert to string but do not apply any formatting rules.
	 * 
	 * @return Returns the raw format string format of this object.
     **/
    public String getStringValue() {
    	/**
    	 * Return empty string if the property is null.
    	 */
	    if (isNull()) {
	    	return "";
	    }
	    
        return this.value;
    }
}