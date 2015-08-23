/*
 * Created on Feb 17, 2004 by michael
 * $Id: DoubleProperty.java,v 1.1.2.10 2006/07/17 16:17:54 mike Exp $
 */
package org.zxframework.property;

import java.math.BigDecimal;
import java.text.NumberFormat;

import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;

/**
 * DoublePropery - Property that holds Double precision ints and ints.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DoubleProperty extends Property {
    
    //------------------------ Members    
    
    /** The backend data storage for this property. */
	private double value;
    
    //------------------------ Constructors      
	
	/**
	 * Default constructor
	 */
	public DoubleProperty() { 
		super();
	}
	
	/**
	 * A convenience property constructor.
	 * 
	 * @param pdblValue The Native type to set the value to
	 */
	public DoubleProperty(double pdblValue) {
		this.value = pdblValue;
		this.isNull = false;
	}
	
	/**
	 * Instant value property for doubles values.
	 * 
	 * <pre>
	 * 
	 * Using static property are not yet supported. This constructor is used 
	 * in special cases where you just want a property object with none of 
	 * the over head. So the create property will not have an associated property.
	 * </pre>
	 * 
	 * @param pdblValue The Native type to set the value to
	 * @param pblnIsNull Is the value of the property null ?
	 */
	public DoubleProperty(double pdblValue, boolean pblnIsNull) {
	    this.value = pdblValue;
	    isNull = pblnIsNull;
	}
	
	/**
	 * Instant value property for doubles values.
	 * 
	 * <pre>
	 * 
	 * Using static property are not yet supported. This constructor is used in special 
	 * cases where you just want a property object with none of the over head. So the 
	 * create property will not have an associated property.
	 * </pre>
	 * 
	 * @param pdblValue The Native type to set the value to
	 * @param pblnIsNull Is the value of the property null ?
	 */
	public DoubleProperty(Double pdblValue, boolean pblnIsNull) {
	    if (pdblValue != null) {
	        this.value = pdblValue.doubleValue();
	        isNull = pblnIsNull;
	    } else {
	        this.value = 0;
	        this.isNull = true;
	    }
	}
	
    //------------------------ Public methods    

	/** 
	 * Sets the Double Property value from a String. 
	 * 
	 * <pre>
	 * 
	 * NOTE : If the string is empty then the default value if 0
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
			/**
			 * Make sure we are not sending through an empty value
			 */
			if (!this.isNull && !StringUtil.isEmptyTrim(pstrValue)) { 
				setValue = setValue(Double.parseDouble(pstrValue));
                
			} else {
                /**
                 * Handle null values.
                 */
				value = 0.0;
			    setValue = handleNull();
		        if (setValue.pos == zXType.rc.rcError.pos) {
		            return setValue;
		        }
                
			}
			
			return setValue;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Set double value", e);
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
	 * Validate / set double value.
	 * 
	 * @param pdblValue The value to set. 
	 * 					This is the object double which will get converted to its primitive datatype.
	 * @return Returns whether setValue was sucessfull.
	 * @throws ZXException Thrown if the setValue method failed
	 */
	public zXType.rc setValue(Double pdblValue) throws ZXException {
		return setValue(pdblValue.doubleValue());
	}

	/**
	 * Validate / set double value.
	 * 
	 * @param pdblValue The value to set.
	 * @return Returns whether setValue was sucessfull.
	 * @throws ZXException Thrown if the setValue method failed
	 */
	public zXType.rc setValue(double pdblValue) throws ZXException {
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pdblValue", pdblValue);
		}
		
		zXType.rc setValue =zXType.rc.rcOK;
		
		try {
			//------------------------------------------- DoubleProperty specific stuff
			
			/**
			 * Handle precision
			 */
			if (this.attribute.getPrecision() > 0) {
                NumberFormat  fmt = NumberFormat.getNumberInstance();
                fmt.setMaximumFractionDigits(this.attribute.getPrecision());
                pdblValue = fmt.parse(fmt.format(pdblValue)).doubleValue();
			}
			
			//------------------------------------------- DoubleProperty specific stuff 
			
			/**
			 * Reset FKLabel (and FKBO) as it is no longer relevant
			 * when there is a new value; also take into consideration whether
			 * there is a FK label expression as the expression could be such that
			 * even if the FK attr has the same value (ie points to the same FK BO)
			 * the label is still different!
			 */
			updateFKLabel(new Double(this.value), new Double(pdblValue));
			
			/**
			 * Validate only if needed
			 */
			if (!this.bo.isValidate()) {
				if (value != pdblValue) {
					setPersistStatusDirty();
				}
				
				this.value = pdblValue;
				this.isNull = false;
				
			} else {
				/**
				 * Validation
				 */
			    
				/**
				 * See if the suggested value match any of the options (if any)
				 */
				if(this.attribute.getOptions() != null && this.attribute.getOptions().size() > 0 
				   && !this.attribute.isCombobox()) {
					if (!isValueInOptions(String.valueOf(pdblValue))) {
						getZx().trace.userErrorAdd("Value is not in the option list for " + attribute.getLabel().getLabel() 
								+ " for attribute " + attribute.getName());
                        setValue = zXType.rc.rcError;
                        return setValue;
					}
				}
				
                /**
                 * Check whether the value is in the range. This will throw an 
                 * exception if out of range
                 */
                if (checkRange(new Double(pdblValue)).pos == zXType.rc.rcError.pos) {
                    setValue = zXType.rc.rcError;
                    return setValue;
                }
				
			    /**
			     * If the new value is different from the old value: set the
			     * persist status
			     **/
			    if (value != pdblValue) {
			        setPersistStatusDirty();
			    }
				
			    /**
			     * Now we feel safe to assign the value
			     **/
			    this.value = pdblValue;
			    
			    this.isNull = false;
			}
			
            return setValue;
		} catch (Exception e) {
			getZx().trace.addError("setValue - Set double value", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pdblValue = " + pdblValue);
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
	 * This will return the native backend datatype for this property.
	 * @return Returns the value.
	 */
	public double getValue() {
		return value;
	}
	
	protected void setDoubleValue(double pdblValue) {
		this.value = pdblValue;
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
			if (isNull) {
//				formattedValue = String.valueOf(value);
//				
//				/**
//				 * If not trim: bring to approriate length and right align
//				 */
//				if(!pblnTrim) {
//					formattedValue = StringUtil.padString(formattedValue, ' ', attribute.getLength(), true);
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
				if (StringUtil.len(getAttribute().getForeignKey()) > 0) {
					formattedValue = fkformattedValue();
					return formattedValue;
				}
                
				//------------------------------------------- END COMMON CODE
				
				//-------------------------------------------  START DoubleProperty specific stuff
				  
				//------------------------------------------- START SIMILIAR CODE
				if(getAttribute().getOptions() != null && getAttribute().getOptions().size() > 0) {
					formattedValue = optionValueToLabel();
				} else {
					if (StringUtil.len(getAttribute().getOutputMask()) > 0) {
						formattedValue = String.valueOf(value);
						
					} else {
						/**
						 * DGS16APR2003: If no output mask, format a double with the right
						 * number of decimal places.
						 */
						BigDecimal objBigDecimal = new BigDecimal(String.valueOf(value));
						objBigDecimal = objBigDecimal.setScale(getAttribute().getPrecision(), BigDecimal.ROUND_HALF_UP);	
						formattedValue = String.valueOf(objBigDecimal);
						
					}
					
				}
				//------------------------------------------- END SIMILIAR CODE
				
				/**
				 * If not trim: bring to approriate length and right align
				 */
				if(!pblnTrim) {
					formattedValue = StringUtil.padString(formattedValue, ' ', getAttribute().getOutputLength(), true);
				}
				
				//------------------------------------------- END DoubleProperty specific stuff
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
     **/
    public String getStringValue() {
        String getStringValue;
        
		/**
		 * DGS16APR2003: Although it is a raw value required, it does no harm to
		 * format a double with the right number of decimal places, and looks better.
		 * 
		 * BD02JUL04 - Well it does not hurt to check whether we
		 * have an attribute so we can actually look at things like
		 * length and precision
		 */
        if (isNull) {
            getStringValue = "";
            
        } else if (getAttribute() != null) {
                BigDecimal objBigDecimal = new BigDecimal(String.valueOf(value));
                objBigDecimal = objBigDecimal.setScale(getAttribute().getPrecision(), BigDecimal.ROUND_HALF_UP);    
        		getStringValue = String.valueOf(objBigDecimal);
        		
        } else {
            getStringValue = String.valueOf(value);
            
        }
        
		return getStringValue;
    }
}