/*
 * Created on Feb 17, 2004 by michael
 * $Id: DateProperty.java,v 1.1.2.14 2006/07/17 16:40:44 mike Exp $
 */
package org.zxframework.property;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.DateUtil;
import org.zxframework.util.StringUtil;

/**
 * DateProperty - Represents the dataTypes : dtDate/dtTime and dtTimestamp.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DateProperty extends Property {

    //------------------------ Members    
    
    /** Unlying storage of the date. Defaults to today. */
	private Date value = new Date();
	
    /** Caching for the toString */
	private transient  DateFormat dateFormat;
    
    /** Defaults to date. */
    private zXType.dataType dataType = zXType.dataType.dtDate;
    
    //------------------------ Constructors      
	
	/**
	 * Default constructor.
	 */
	public DateProperty() {
	    super();
	}
	
    /**
     * Instant value property for date / time / timestamp values.
     * 
     * <pre>
     * 
     * Using static property are not yet supported. This constructor is used in special cases where you 
     * just want a property object with none of the over head. So the create property will not have an
     * associated property.
     * </pre>
     *  
     * @param pdatValue The Native type to set the value to
     */
    public DateProperty(Date pdatValue) {
        
        if (pdatValue != null) {
            this.value = pdatValue;
            this.isNull = false;
        } else {
            this.value = new Date();
            this.isNull = true;
        }
        
    }
    
	/**
	 * Instant value property for date / time / timestamp values.
	 * 
	 * <pre>
	 * 
	 * Using static property are not yet supported. This constructor is used in special cases where you 
	 * just want a property object with none of the over head. So the create property will not have an
	 * associated property.
	 * </pre>
	 *  
	 * @param pdatValue The Native type to set the value to
	 * @param pblnIsNull Whether the property is null.
	 */
	public DateProperty(Date pdatValue, boolean pblnIsNull) {
        
	    if (pdatValue != null) {
	        this.value = pdatValue;
	        this.isNull = pblnIsNull;
	    } else {
	        this.value = new Date();
	        this.isNull = true;
	    }
        
	}
	
    /**
     * Instant value property for date / time / timestamp values.
     * 
     * <pre>
     * 
     * Using static property are not yet supported. This constructor is used in special cases where you 
     * just want a property object with none of the over head. So the create property will not have an
     * associated property.
     * </pre>
     *  
     * @param pdatValue The Native type to set the value to
     * @param pblnIsNull Whether the property is null.
     * @param penmDataType The specific date type you want. As a DateProperty can be a Time/Date or Timestamp.
     */
    public DateProperty(Date pdatValue, boolean pblnIsNull, zXType.dataType penmDataType) {
        
        if (pdatValue != null) {
            this.value = pdatValue;
            this.isNull = pblnIsNull;
        } else {
            this.value = new Date();
            this.isNull = true;
        }
        
        /** Set the local datatype. **/
        this.dataType = penmDataType;
        
    }
    
    //------------------------ Public methods    	
	
	/** 
	 * Sets the DateProperty value from a String.
	 * 
	 * <pre>
	 * 
	 * NOTE : If the string is empty the date is set to now.
	 * NOTE : Look into support full time and date from forms
	 * </pre>
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
		    if (isNull && (StringUtil.len(pstrValue) == 0)) {
		        /**
		         * Handle null values.
                 * 
                 * Default is the current date.
		         */
                value = new Date();
                setValue = handleNull();
                if (setValue.pos == zXType.rc.rcError.pos) {
                    return setValue;
                }
                
		    } else {
				Date dateValue;
				/**
				 * Make sure we are not sending through an empty value
				 */
				if ( !StringUtil.isEmpty(pstrValue) ) {
					/**
					 * DGS16DEC2002: If the RECEIVING object is datatype dtDate, don't include any time element
					 * 
					 * zX version of CDate function that also supports the DDMMMYYYY (eg 13SEP2002) version
					 * 
					 * Parse date according to its dataType.
					 */
					DateFormat df = null;
					if (getDataType().pos == zXType.dataType.dtDate.pos) {
						df = getZx().getDateFormat();
					} else {
						df = getZx().getTimestampFormat();
					}
					
					dateValue = DateUtil.parse(df, pstrValue);
					
				} else {
					// Default to now.
					dateValue = new Date();
				}
				
				setValue(dateValue);
		    }
		    
		    return setValue;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Set date value", e);
			if (getZx().log.isErrorEnabled()) {
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
	 * Set the DateProperty by Date.
	 * 
	 * @param pdatValue Value to set
	 * @return Returns the return code of the method.
	 * @throws ZXException Throw if setValue fails
	 */
	public zXType.rc setValue(Date pdatValue) throws ZXException {
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pdatValue", pdatValue);
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
			updateFKLabel(this.value, pdatValue);
			
			/**
			 * Validate only if needed
			 */
			if (!this.bo.isValidate()) {
				if (this.value == null || !this.value.equals(pdatValue)) {
					setPersistStatusDirty();
				}
				
				this.value = pdatValue;
				this.isNull = false;
				
			} else {
				/**
				 * Validation
				 */
                
				/**
				 * See if the suggested value match any of the options (if any)
				 */
				if(this.attribute.getOptions() != null && this.attribute.getOptions().size() > 0 && !this.attribute.isCombobox()) {
					if (!isValueInOptions(pdatValue.toString())) {
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
				if (checkRange(pdatValue).pos == zXType.rc.rcError.pos) {
                    setValue = zXType.rc.rcError;
				    return setValue;
                }
				
			    /**
			     * If the new value is different from the old value: set the
			     * persist status.
			     **/
			    if (this.value == null || !this.value.equals(pdatValue)) {
			        setPersistStatusDirty();
			    }
				
			    /**
			     * Now we feel save to assign the value
			     **/
			    this.value = pdatValue;
				
			    this.isNull = false;
			}
			
            return setValue;
		} catch (Exception e) {
			getZx().trace.addError("Failed to  : Set date value", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pdatValue = " + pdatValue);
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
	 * @return Returns the value.
	 */
	public Date getValue() {
		return this.value;
	}
	
	protected void setDateValue(Date pdtValue) {
		this.value = pdtValue;
	}

	/***
	 * Return the current value as formatted string.
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
//				formattedValue = value.toString();
//				
//				/**
//				 * If not trim: bring to approriate length and left align
//				 */
//				if(!pblnTrim) {
//					formattedValue = StringUtil.padString(formattedValue, ' ', this.attribute.getLength(), false);
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
				
				//------------------------------------------- START DateProperty specific stuff
				  
				//------------------------------------------- START SIMILIAR CODE
				if(this.attribute.getOptions() != null && this.attribute.getOptions().size() > 0) {
					formattedValue = optionValueToLabel();
				} else {
					if (StringUtil.len(this.attribute.getOutputMask()) > 0) {
					    // We presume that the output mask for this property is the also the date format.
				        SimpleDateFormat formatter = new SimpleDateFormat(this.attribute.getOutputMask());
						formattedValue = formatter.format(this.value);
                        
					} else {
						/**
						 * Select the correct formatter for the date :
						 */
						DateFormat formatter = null;
						int intDataType = getDataType().pos;
						if (intDataType == zXType.dataType.dtTimestamp.pos) {
							formatter = getZx().getTimestampFormat();
						} else if (intDataType == zXType.dataType.dtDate.pos) {
							formatter = getZx().getDateFormat();
						} else if (intDataType == zXType.dataType.dtTime.pos) {
							formatter = getZx().getTimeFormat();
						} else {
							/**
							 * Unknown dateformat
							 */
							formatter = getZx().getDateFormat();
							getZx().trace.addError("Unknown dataType", zXType.valueOf(getDataType()));
						}
                        
						formattedValue = formatter.format(this.value);
					}
				}
				//------------------------------------------- END SIMILIAR CODE
				
				/**
				 * If not trim: bring to approriate length and right align
				 */
				if(!pblnTrim) {
					formattedValue = StringUtil.padString(formattedValue, ' ', this.attribute.getOutputLength(), false);
				}
				
				//------------------------------------------- END DateProperty specific stuff
			}
			return formattedValue;
			
		} catch (Exception e) {
			if (getZx().log.isErrorEnabled()) {
				getZx().trace.addError("Failed To : Return the current value as formatted string", e);
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
     * @return Returns the string form of the stored value in the object
     **/
    public String getStringValue() {
    	/**
    	 * Return empty string if the property is null.
    	 */
	    if (isNull()) {
	    	return "";
	    }
	    
        /**
         * Try to determine the dateFormat only once : 
         */
	    if (dateFormat == null) {
	        try {
	            if (this.attribute != null) {
	                int intDataType = getDataType().pos;
					if (intDataType == zXType.dataType.dtDate.pos) {
					    dateFormat = getZx().getDateFormat();
					} else if (intDataType == zXType.dataType.dtTime.pos){
					    dateFormat = getZx().getTimeFormat();
					} else {
					    dateFormat = getZx().getTimestampFormat();
					}
					
	            } else {
	                // Default to timestamp dateformat for adhoc properties.
	                dateFormat = getZx().getTimestampFormat();
	            }
	            
	        } catch (Exception e) {
	            getZx().log.error("Failed to create SimpleDateFormat",e);
	            return null;
	        }
	    }
	    
		return dateFormat.format(getValue());
    }
    
    //------------------------ Special Methods
    
    /**
     * Useful when we do not have a handle to the attribute,
     * like with instant property and the dataType is Time. 
     * 
     * @return Returns the dataType.
     */
    public zXType.dataType getDataType() {
        if (this.attribute != null) {
            return this.attribute.getDataType();
        }
        
        return this.dataType;
    }
}