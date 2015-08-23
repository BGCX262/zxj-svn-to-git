/*
 * Created on Feb 10, 2004
 * $Id: LongProperty.java,v 1.1.2.15 2006/07/17 16:17:27 mike Exp $
 */
package org.zxframework.property;

import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;

/**
 * LongProperty - Handles the dataTypes Long and Automatics. 
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class LongProperty extends Property {
	
    //------------------------ Members    
    
    /** The underlying variable for storing the value. **/
	private long value = 0 ;
    
    //------------------------ Constructors      
	
	/**
	 * Default constructor
	 */
	public LongProperty() { 
		super();
	}
	
	/**
	 * Instant value property for doubles values.
	 * 
	 * @param plngValue The Native type to set the value to
	 * @param pblnIsNull Is the value of the property null ?
	 */
	public LongProperty(Long plngValue, boolean pblnIsNull) {
	    
	    if (plngValue != null) {
	        this.value = plngValue.longValue();
		    isNull = pblnIsNull;
	    } else {
	        this.value = 0;
		    isNull = true;
	    }
	    
	}
    
    /**
     * Instant value property for longs and ints values.
     * 
     * @param plngValue The Native type to set the value to
     * @param pblnIsNull Is the value of the property null ?
     */
    public LongProperty(int plngValue, boolean pblnIsNull) {
        this.value = plngValue;
        isNull = pblnIsNull;
    }
    
	/**
	 * @param plngValue The Native type to set the value to
	 */
	public LongProperty(long plngValue) {
		this.value = plngValue;
		this.isNull = false;
	}
	
	/**
	 * Instant value property for longs and ints values.
	 * 
	 * @param plngValue The Native type to set the value to
	 * @param pblnIsNull Is the value of the property null ?
	 */
	public LongProperty(long plngValue, boolean pblnIsNull) {
	    this.value = plngValue;
	    isNull = pblnIsNull;
	}
	
    //------------------------ Public methods    
	
	/**
	 * Validate / set long value.
	 * 
	 * @param plngValue The value to set.
	 * @return Returns the return code for this method.
	 * @throws ZXException Thrown if the setValue method failed
	 */
	public zXType.rc setValue(Long plngValue) throws ZXException {
	    return setValue(plngValue.longValue());
	}
	
	/**
	 * Validate / set long value.
	 * 
	 * @param plngValue The value to set.
	 * @return Returns the return code for this method
	 * @throws ZXException Thrown if the setValue method failed
	 */
	public zXType.rc setValue(long plngValue) throws ZXException {
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("plngValue", plngValue);
		}
		
	    zXType.rc setValue = zXType.rc.rcOK;
		
		try {
			
			/**
			 * Reset FKLabel (and FKBO) as it is no longer relevant
			 * when there is a new value; also take into consideration whether
			 * there is a FK label expression as the expression could be such that
			 * even if the FK attr has the same value (ie points to the same FK BO)
			 * the label is still different!
			 */
			updateFKLabel(new Long(this.value), new Long(plngValue));
			
			/**
			 * Validate only if needed
			 */
			if (!this.bo.isValidate()) {
				if (this.value !=plngValue) {
					setPersistStatusDirty();
				}
				
				this.value = plngValue;
				this.isNull = false;
                
			} else {
				/**
				 * Validation
				 */
				/**
				 * See if the suggested value match any of the options (if any)
				 */
				if(this.attribute.getOptions() != null && this.attribute.getOptions().size() > 0 && !this.attribute.isCombobox()) {
					if (!isValueInOptions(String.valueOf(plngValue))) {
						getZx().trace.userErrorAdd("Value is not in the option list for " + this.attribute.getLabel().getLabel() 
								+ " for attribute " + this.attribute.getName());
                        setValue = zXType.rc.rcError;
                        return setValue;
					}
				}
				
                /**
                 * Check whether the value is in the range. This will throw an 
                 * exception if out of range
                 */
                if (checkRange(new Long(plngValue)).pos == zXType.rc.rcError.pos) {
                    setValue = zXType.rc.rcError;
                    return setValue;
                }
                
			    /**
			     * If the new value is different from the old value: set the
			     * persist status
			     **/
			    if (this.value != plngValue) {
			        setPersistStatusDirty();
			    }
				
			    /**
			     * Now we feel save to assign the value
			     **/
			    this.value = plngValue;
				
			    this.isNull = false;
			    
			}
			
			return setValue;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Set long value", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : plngValue = " + plngValue);
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
	 * Sets the Long Property value from a String. 
	 * 
	 * NOTE : If the string is empty.
	 * 
	 * @see org.zxframework.property.Property#setValue(String)
	 **/
	public zXType.rc setValue(String pstrValue) throws ZXException {
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrValue", pstrValue);
		}
		
	    zXType.rc setValue = zXType.rc.rcOK;
	    
		try {
			/**
			 * Make sure we are not sending through an empty value or a null value.
			 */
			if (!this.isNull && !StringUtil.isEmptyTrim(pstrValue) ) {
                // Try to parse the string as a long
			    setValue = setValue(Long.parseLong(pstrValue.trim()));
                
			} else {
                /**
                 * Handle null values.
                 */
                this.value = 0;
			    setValue = handleNull();
		        if (setValue.pos == zXType.rc.rcError.pos) {
		            return setValue;
		        }
			}
            
			return setValue;
		} catch (Exception e) {
			if (getZx().log.isErrorEnabled()) {
				getZx().trace.addError("Failed to : Set long value", e);
				getZx().log.error("Parameter : pstrValue = " + pstrValue);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return setValue;
		} finally {
			if (getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * @return Returns the value.
	 */
	public long getValue() {
		return this.value;
	}
	protected void setLongValue(long plngValue) {
		this.value = plngValue;
	}
	
	/***
	 * Return the current value as formatted string
	 * 
	 * @param pblnTrim Trim the outputted string.
	 * @see org.zxframework.property.Property#formattedValue(boolean)
	 * @throws ZXException If it failes
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
//				formattedValue = this.value + "";
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
				
				
				//------------------------------------------- START LongProperty specific stuff
				  
				//------------------------------------------- START SIMILIAR CODE
				if(this.attribute.getOptions() != null && this.attribute.getOptions().size() > 0) {
					formattedValue = optionValueToLabel();
				} else {
					if (StringUtil.len(this.attribute.getOutputMask()) > 0) {
						// Need to do something like : formattedValue = format(value, outputMask)
						formattedValue = this.value + ""; 
					} else {
						formattedValue = this.value + "";
					}
				}
				//------------------------------------------- END SIMILIAR CODE
				
				/**
				 * If not trim: bring to approriate length and right align
				 */
				if(!pblnTrim) {
					formattedValue = StringUtil.padString(formattedValue, ' ', this.attribute.getOutputLength(), false);
				}
				
				//------------------------------------------- END LongProperty specific stuff
			}
			
			return formattedValue;
		} catch (Exception e) {
			if (getZx().log.isErrorEnabled()) {
				getZx().trace.addError("Failed To : formattedValue - Return the current value as formatted string", e);
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

	//------------------------ Property's overidden methods

    /** 
	 * If requested raw: simply convert to string but do not apply any formatting rules.
	 * 
     * @see org.zxframework.property.Property#getStringValue()
	 * @return Returns the raw format string format of this object.
     **/
    public String getStringValue() {
    	/**
    	 * Return empty string if the property is null.
    	 */
	    if (isNull()) {
	    	// return "0";
	    	return "";
	    }
	    
        return String.valueOf(this.value);
    }
}