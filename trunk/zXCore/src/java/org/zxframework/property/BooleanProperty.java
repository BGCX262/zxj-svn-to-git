/*
 * Created on Feb 17, 2004 by michael
 * $Id: BooleanProperty.java,v 1.1.2.9 2006/07/17 16:17:27 mike Exp $
 */
package org.zxframework.property;

import java.text.MessageFormat;

import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;

/**
 * <p>BooleanProperty - Handles boolean datatype.</p> 
 * 
 * <pre>
 * 
 * NOTE :  This class is using a primitive boolean for the storage. 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class BooleanProperty extends Property {
	
    //------------------------ Members    
    
    /** The underlying storage variavble for the boolean property. **/
	private boolean value = false;
	
    //------------------------ Constructors      
	
	/**
	 * Default constructor
	 */
	public BooleanProperty() {
	    super();
	}
    
    /**
     * Instant value property for boolean values.
     * 
     * @param pblnValue The Native type to set the value to
     */
    public BooleanProperty(boolean pblnValue){
        this.value = pblnValue;
        this.isNull = false;
    }
    
	/**
	 * Instant value property for boolean values.
	 * 
	 * @param pblnValue The Native type to set the value to
	 * @param pblnIsNull Whether the property is null.
	 */
	public BooleanProperty(boolean pblnValue, boolean pblnIsNull) {
	    this.value = pblnValue;
	    this.isNull = pblnIsNull;
	}
	
	/**
	 * Instant value property for boolean values.
	 * 
	 * @param pblnValue The Native type to set the value to
	 * @param pblnIsNull Whether the property is null
	 */
	public BooleanProperty(Boolean pblnValue, boolean pblnIsNull) {
		
	    if (pblnValue != null) {
	        this.value = pblnValue.booleanValue();
	    } else {
	        this.value = false;
	    }
	    
	    this.isNull = pblnIsNull;
	}
	
    //------------------------ Public methods    
	
	/** 
	 * Sets the Boolean Property value from a String. 
	 * 
	 * <pre>
	 * 
	 * NOTE : If the string is empty then the default is false
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
			boolean blnValue = false;
			
			/**
			 * Make sure we are not sending through an empty value
			 */
			if (!StringUtil.isEmpty(pstrValue)) {
				/**
				 * NOTE : is 0 true ? or is 1 true ?
				 */
				blnValue = StringUtil.booleanValue(pstrValue);
			}
			
			setValue(blnValue);
			
			return setValue;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Set booloean value", e);
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
	 * Validate / set boolean value.
	 * 
	 * @param pblnValue The value to set.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if the setValue method failed
	 */
	public zXType.rc setValue(Boolean pblnValue) throws ZXException {
	    return setValue(pblnValue.booleanValue());
	}
	
	/**
	 * Validate / set boolean value.
	 * 
	 * @param pblnValue The value to set.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if the setValue method failed
	 */
	public zXType.rc setValue(boolean pblnValue) throws ZXException {
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pblnValue", pblnValue);
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
			updateFKLabel(new Boolean(this.value), new Boolean(pblnValue));
			
			/**
			 * Validate only if needed
			 */
			if (!bo.isValidate()) {
				if (this.value !=pblnValue) {
					setPersistStatusDirty();
				}
				
				this.value = pblnValue;
				this.isNull = false;
				
			} else {
				/**
				 * No meaningful validation for boolean fields
				 */
				
			    /**
			     * If the new value is different from the old value: set the
			     * persist status
			     **/
			    if (this.value != pblnValue) {
			        setPersistStatusDirty();
			    }
				
			    /**
			     * Now we feel save to assign the value
			     **/
			    this.value = pblnValue;
				
			    this.isNull = false;
			}
			
			return setValue;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Set boolean value", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : plngValue = " + pblnValue);
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
	public boolean getValue() {
		return this.value;
	}
	
	protected void setBooleanValue(boolean pblnValue) {
		this.value = pblnValue;
	}
	
	/***
	 * Return the current value as formatted string.
	 * 
	 * @param pblnTrim Trim the outputted string.
	 * @see org.zxframework.property.Property#formattedValue(boolean)
	 * @throws ZXException Is thrown if formattedValue fails
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
			    
//				 formattedValue = String.valueOf(this.value);
//				/**
//				 * If not trim: bring to approriate length and left align
//				 */
//				if(!pblnTrim) {
//					formattedValue = StringUtil.padString(formattedValue, ' ', this.attribute.getLength(), false);
//				}
			    
			    // Null boolean properties string value should be ? 
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
				
				//------------------------------------------- START BooleanProperty specific stuff
				  
				//------------------------------------------- START SIMILIAR CODE
				if(this.attribute.getOptions() != null && this.attribute.getOptions().size() > 0) {
					formattedValue = optionValueToLabel();
				} else {
					if (StringUtil.len(this.attribute.getOutputMask()) > 0) {
						/**
						 * Format does apply to 1 / 0 but not to booleans
						 * strReturn = format(IIf(blnValue, 1, 0), .outputMask)
						 **/
						// formattedValue = String.valueOf(this.value);
                        MessageFormat formatter = new MessageFormat(this.attribute.getOutputMask());
                        formattedValue = formatter.format(this.value?"1":"0");
                        
					} else {
						formattedValue = String.valueOf(this.value);
					}
				}
                
				//------------------------------------------- END SIMILIAR CODE
				
				/**
				 * If not trim: bring to approriate length and left align
				 */
				if(!pblnTrim) {
					formattedValue = StringUtil.padString(formattedValue, ' ', this.attribute.getOutputLength(), false);
				}
				
				//------------------------------------------- END BooleanProperty specific stuff
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
	
	//------------------------ Property's overidden methods
	
	/**
	 * If requested raw: simply convert to string but do not apply any formatting rules.
	 * 
	 * @return Returns the raw format string format of this object.
	 */
	public String getStringValue() {
    	/**
    	 * Return empty string if the property is null.
    	 */
	    if (isNull()) {
	    	return "";
	    }
	    
        return String.valueOf(this.value);
	}
}