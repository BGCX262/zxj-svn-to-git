/*
 * Created on Feb 9, 2004 by michael
 * $Id: Property.java,v 1.1.2.25 2006/07/17 16:40:44 mike Exp $
 */
package org.zxframework.property;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zxframework.Attribute;
import org.zxframework.Option;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.DateUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;

/**
 * Property represents a value of a Attribute of a ZXBO. 
 * 
 * <p>This is used by the framework to store values of a ZXBO.</p>
 * 
 * <p> 
 * 	<img src="../../doc-files/property.png">
 * </p>
 * 
 * <pre>
 * 
 * Single property of the business object property collection
 * 
 * Change    : BD11DEC02
 * Why       : When the length of an attribute > 32000, do not truncate
 *        	   the length as this is really a synonym for very long!
 * 
 * Change    : DGS16DEC02
 * Why       : In SetValue, for datatype dtDate, don't include any time element.
 *             Also, in property let datValue, set the datatype to timestamp rather
 *             than date when initializing, because dates get truncated.
 * 
 * Change    : DGS28JAN03
 * Why       : In formattedValue, handle time differently to date.
 * 
 * Change    : BD27MAR03
 * Why       : Can now also do a lngValue on str value properties as long as it
 *             contains a proper numeric value; same for double
 * 
 * Change    : BD7APR03
 * Why       : Support for zExpression datatype
 * 
 * Change    : DGS16APR2003
 * Why       : In formattedValue, handle precision of doubles (even when raw)
 * 
 * Change    : BD5JUN03
 * Why       : Fixed nice bug: when we want to check for 32000 we should check
 *             for 32000 and not for 320000!
 * 
 * Change    : BD17JUN03
 * Why       : In case of a combo box, do not force the value to be in the
 *             options list, that is the whole point of a combo!
 * 
 * Change    : DGS28OCT2003
 * Why       : In clone, must be able to cope with attr not set when initializing.
 * 
 * Change    : BD26NOV03
 * Why       : In compare, allow to compare properties of different datatypes
 * 
 * Change    : BD6JAN04
 * Why       : Added support for inputMask
 * 
 * Change    : DGS09MAR2004
 * Why       : Couple of changes in formattedValue:
 *             - Don't reduce multi-lines to output length when not trimming
 *             - Long and Double fields should be left aligned if they have option lists
 *               and it is the string option label that is being returned
 * 
 * Change    : BD13JUN04
 * Why       : Fixed problem; always treat automatics same as longs
 * 
 * Change    : BD2JUL04
 * Why       : Fix problem with formattedValue for ad-hoc properties
 * 
 * Change    : BD15SEP04
 * Why       : The expression check-syntax method works now slightly different
 * 
 * Change    : BD9APR05 V1.5:2
 * Why       : Do not initialise all properties to have null-flag set; only optional ones
 * 
 * Change    : BD17JUN05 - V1.5:18
 * Why       : Added property persistStatus
 * 
 * Change    : BD17JUN05 - V1.5:19
 * Why       : Added isNotNull property (to be used instead of 'not objProperty.isNull')
 * 
 * Change    : BD30JUN05 - V1.5:20
 * Why       : Added support for enhanced FK label behaviour
 * 
 * Change    : BD11JUL05 - V1.5:30
 * Why       : Reset FKLabel whenever attribute value is set to ensure that
 * 			   previous value is never lingering about....
 * 
 * Change    : BD21JAN06 - V1.5:82
 * Why       : lngValue on a dbl must be truncate double, not round it
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class Property extends ZXObject {

    //------------------------ Members    
    
    /** <code>bo</code> - The business this object belongs to. Note this may be null if it is a instant property ie : adhoc property. **/
    protected ZXBO bo;
    
    /** <code>attribute</code> - The linked attribute used for formatting and validation. This can be null for instant properties. **/
    protected Attribute attribute;
    
    /** <code>fkbo</code> - The foreign business object of the property. **/
    private ZXBO fkbo;
    
    /** <code>fkLabel</code> -  The foriegn label of  a property*/
    private String fkLabel;
    
    /** <code>name</code> - The name of the property. **/
    protected String name;
    
    /** <code>isNull</code> - Whether the property isNull */
    public boolean isNull;
    
    private zXType.propertyPersistStatus persistStatus;
    
    //------------------------ Constructors      
    
    /**
     * Default constructor.
     */
    public Property() {  
        /**
         * Set to null
         */
       this.isNull = true;
    }

    //------------------------ Public Super class methods

    /**
     * init - Initialise property.
     * 
     * @param pobjBO Handle to BO that this property belongs to. Not used for instant value properties
     * @param pstrAttribute The name of the attribute.
     * @throws ZXException Thrown if the initilastion fails.
     */
    public void init(ZXBO pobjBO, String pstrAttribute) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrAttribute", pstrAttribute);
        }
        
        try {
            
            this.bo = pobjBO;
            this.name = pstrAttribute;
            
            if (this.bo != null) {
                
                this.attribute = this.bo.getDescriptor().getAttribute(pstrAttribute);
                if (this.attribute == null) {
                    throw new Exception("Handle to attr could not be retrieved : " + name);
                }
                
                /**
                 * If bound, only set to null if attribute is optional
                 */
                if (this.attribute.isOptional()) {
                    this.isNull = true;
                }
                
            } else {
                /**
                 * Set to null if not bound
                 */
                this.isNull = true;
                
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Initialise property ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrAttribute = " + pstrAttribute);
            }
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    //------------------------ Getters and Setters.
    
    /**
     * @see Property#fkLabel
     * @return Returns the fkLabel.
     */
    public String getFkLabel() {
        return fkLabel;
    }
    
    /**
     * @see Property#fkLabel
     * @param fkLabel The fkLabel to set.
     */
    public void setFkLabel(String fkLabel) {
        this.fkLabel = fkLabel;
    }    
    
    /**
	 * @return Returns the persistStatus.
	 */
	public zXType.propertyPersistStatus getPersistStatus() {
		/**
		 * Default to new for now.
		 */
		if (persistStatus == null) {
			persistStatus = zXType.propertyPersistStatus.ppsNew;
		}
		
		return persistStatus;
	}
	
	/**
	 * @param persistStatus The persistStatus to set.
	 */
	public void setPersistStatus(zXType.propertyPersistStatus persistStatus) {
		this.persistStatus = persistStatus;
	}
	
	/**
     * @see Property#isNull
     * @return Returns the isNull.
     */
    public boolean isNull() {
        return isNull;
    }
    
    /**
     * Is there a null value?
     * @return Returns Is there a null value?
     */
    public boolean isNotNull() {
    	return !this.isNull;
    }
    
    /**
     * @see Property#isNull
     * @param isNull The isNull to set.
     */
    public void setNull(boolean isNull) {
        this.isNull = isNull;
    }
    
    /**
     * @see Property#attribute
     * @param attribute The attribute to set.
     */
    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }
    
    /**
     * @return Returns the attribute.
     */
    public Attribute getAttribute() {
        return attribute;
    }
    
    /**
     * @see Property#bo
     * @param bo The bo to set.
     */
    public void setBo(ZXBO bo) {
        this.bo = bo;
    }
    
    /**
     * @see Property#name
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Return the current value as formatted string.
     * 
     * @return Returns a formatted string of the property value.
     * @throws ZXException Thrown if the formattedValue fails
     */
    public String formattedValue() throws ZXException {
        return formattedValue(true);
    }

    /**
     * Return BO associated with foreign key for this attribute.
     * 
     * @return Returns the FKBO.
     * @throws ZXException Throws an ZXException if it cannot get a Foriegn key's BO.
     */
    public ZXBO getFKBO() throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        ZXBO getFKBO = null;

        try {
            /**
             * Singleton : If already set: simply return this
             */
            if (this.fkbo != null) {
                getFKBO = this.fkbo;
                
            } else {
                /**
                 * In case of an alias: get a private descriptor
                 */
                if (StringUtil.len(this.attribute.getForeignKeyAlias()) > 0) {
                    getFKBO = getZx().createBO(this.attribute.getForeignKey(), true); // We do not want a old copy.
                } else {
                    getFKBO = getZx().createBO(this.attribute.getForeignKey());
                }

                if (getFKBO == null) { 
                    throw new Exception("Unable to create BO for foriegn key : " + this.attribute.getForeignKey()); 
                }
                
                /**
                 * Set alias if so required
                 */
                if ( !StringUtil.isEmpty(this.attribute.getForeignKeyAlias()) ) {
                    getFKBO.getDescriptor().setAlias(this.attribute.getForeignKeyAlias());
                }
                this.fkbo = getFKBO;
                
            }
            return getFKBO;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Return BO associated with foreign key for this attribute", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return getFKBO;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getFKBO);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * setPersistStatusDirty - Set the persist status for BO associated with
     * this property to dirty taken into account the logical status transitions.
     */
    public void setPersistStatusDirty() {
        if  (this.bo.getPersistStatus() == null ||
        	 !(this.bo.getPersistStatus().equals(zXType.persistStatus.psNew) 
             || this.bo.getPersistStatus().equals(zXType.persistStatus.psDeleted))) {
            this.bo.setPersistStatus(zXType.persistStatus.psDirty);
        }
    }

    /**
     * Set the value of the Property with a Property.
     * 
     * @param pobjProp The Property to set this property to.
     * @return Returns the exit value of the setValue.
     * @throws ZXException Thrown if setValue fails.
     */
	public zXType.rc setValue(Property pobjProp) throws ZXException {
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjProperty", pobjProp);
		}
		
		zXType.rc setValue = zXType.rc.rcOK;

		try {
			if (getClass().getName().equals(pobjProp.getClass().getName())) {
				/**
				 * The 2 properties are of the same type.
				 */
				if (pobjProp.getAttribute() != null && pobjProp.getAttribute().equals(getAttribute())) {
					/**
					 * Based on the same attribute so we do not need any bo validation
					 */
					if (pobjProp instanceof BooleanProperty) {
						((BooleanProperty)this).setBooleanValue(((BooleanProperty)pobjProp).getValue());
					} else if (pobjProp instanceof DateProperty) {
						((DateProperty)this).setDateValue(((DateProperty)pobjProp).getValue());
					} else if (pobjProp instanceof DoubleProperty) {
						((DoubleProperty)this).setDoubleValue(((DoubleProperty)pobjProp).getValue());
					} else if (pobjProp instanceof LongProperty) {
						((LongProperty)this).setLongValue(((LongProperty)pobjProp).getValue());
					} else {
						((StringProperty)this).setStringValue(((StringProperty)pobjProp).getValue());
					}
					
				} else {
					if (pobjProp instanceof BooleanProperty) {
						setValue = ((BooleanProperty)this).setValue(((BooleanProperty)pobjProp).getValue());
					} else if (pobjProp instanceof DateProperty) {
						setValue = ((DateProperty)this).setValue(((DateProperty)pobjProp).getValue());
					} else if (pobjProp instanceof DoubleProperty) {
						setValue = ((DoubleProperty)this).setValue(((DoubleProperty)pobjProp).getValue());
					} else if (pobjProp instanceof LongProperty) {
						setValue = ((LongProperty)this).setValue(((LongProperty)pobjProp).getValue());
					} else {
						setValue = ((StringProperty)this).setValue(((StringProperty)pobjProp).getValue());
					}
					
				}
				
//				/**
//				 * TODO : We should probably do this.
//				 * Handle null values.
//				 */
//	            if (pobjProp.isNull) {
//	            	setValue = handleNull();
//	            }
				
			} else {
				String strValue = pobjProp.getAttribute() == null?pobjProp.getStringValue():pobjProp.formattedValue(true);
				this.isNull = pobjProp.isNull;
				setValue = setValue(strValue);
				
			}
            
            /**
             * Copy across the null flag.
             */
            this.isNull = pobjProp.isNull;
            
			return setValue;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Set the Property from another Property", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjProperty = " + pobjProp);
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
	
    //------------------------ Abstract methods

    /**
     * setValue - Set the value of the Property with a String.
     * 
     * <pre>
     * 
     * NOTE : This is the more common way of setting a Property and hence it is
     * a abstract method.
     * </pre>
     *  
     * @param pstrValue The String value you want to set the property to.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown the setValue fails.
     */
    public abstract zXType.rc setValue(String pstrValue) throws ZXException;

    /**
     * formattedValue - Return the current value as formatted string.
     * 
     * @param pblnTrim Trim the result ?
     * @return Returns a formatted string of the property value.
     * @throws ZXException
     */
    public abstract String formattedValue(boolean pblnTrim) throws ZXException;
    
    /**
     * getStringValue - Get the string version of the stored value in the object .
     * 
     * NOTE : This is like the old clsProperty#formattedValue(pblnRaw:=True)
     * TODO : We need to handle not values currently. Currently in the vb version
     * a "" will return a null value in the system.
     * 
     * @return Returns the raw string value of the object
     */
    public abstract String getStringValue();
    
    /**
     * Get the string version of the stored value in the object .
     * 
     * NOTE : This just calls getStringValue.
     * NOTE : This is like the old clsProperty#formattedValue(pblnRaw:=True)
     * 
     * @return Returns the raw string value of the object
     */
    public String strValue() {
        return getStringValue();
    }

    //------------------------ Protected method

    /**
     * isValueInOption - Check whether value is in option list.
     * 
     * <pre>
     * 
     * NOTE : The method signature could just be a String. but then when you
     * call it from LongProperty is the key may be Long and not String.
     * </pre>
     * 
     * @param pobjValue The value to check.
     * @return Returns true if the value is in the option list.
     */
    protected boolean isValueInOptions(String pobjValue) {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjValue", pobjValue);
        }
        
        boolean isValueInOption = false;

        try {
        	
        	if (this.attribute != null && this.attribute.getOptions() != null) {
        		isValueInOption = this.attribute.getOptions().containsKey(pobjValue);
        	}
        	
            return isValueInOption;
            
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(isValueInOption);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Retrieve label in appropriate language for property
     * that has an option list.
     * 
     * <pre>
     * NOTE: Only call if you are sure that you have a 
     * handle to the attribute and there is at least one
     * entry in the options collection.
     * </pre>
     * 
     * @return Returns the label of that property.
     */
    protected String optionValueToLabel() {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        String optionValueToLabel = null;
        
        try {
            /**
             * Get option from list
             */
            Option objOption = (Option)getAttribute().getOptions().get(getStringValue());
            
            if (objOption == null) {
                /**
                 * It is possible that the option associated with the value is
                 * not found in case of a combo
                 */
                if (getAttribute().isCombobox()) {
                    /**
                     * Use the value as label.
                     * 
                     * NOTE : Do not call formattedValue() as this
                     * will result in a recursive loop.
                     */
                    optionValueToLabel = getStringValue();
                    
                } else {
                    /**
                     * Do not throw an exception, but make a note of it .
                     */
                    getZx().log.error("Option entry not found for value " + getAttribute().getName()
                            		  + " ( value: " + getStringValue() + ")");
                    optionValueToLabel = "";
                }
                
            } else {
            	/**
            	 * We have selected a value option.
            	 */
                optionValueToLabel = objOption.getLabelAsString();
            }
            
            return optionValueToLabel;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(optionValueToLabel);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Set the property to its default value.
     * 
     * <pre>
     * 
     * Get the format from the property's attribute descriptor default value
     * 
     * <b>Supported special values :</b>
     * #date, #zdate - Sets the property value to the date now, fomatted 
     * 	according to //zX/db/dateFormat in the config file
     * #time, #ztime - Sets the property value to the time now, formatted 
     * 	as "HH:mm:ss"
     * #now, #znow  - Sets the property value to the date/time now, 
     * 	formatted according to //zX/db/timestampFormat in the config file
     * #clock, #zclock - Sets the property value to a timstamp from 1970.
     * #user, #zuser, #who, #zwho - Sets the property value to the current user id.
     * #true, #ztrue, #yes, #zyes -  Sets the property value to "true"
     * #false, #zfalse, #no, #zno - Sets the property value to "false"
     * #null, #znull - Sets the property value to ""
     * #>preference-value< - Sets the property value to any of the user preferences.
     * </pre>
     * 
     * @param pstrDefaultValue
     *                 The value to set the property, these can be special values
     *                 like #now,#user etc..
     * @throws ZXException Thrown if setDefaultValue fails.
     */
    public void setDefaultValue(String pstrDefaultValue) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDefaultValue", pstrDefaultValue);
        }

        try {
            /**
             * Be extra sure if someone called this incorrectly
             */
            if (pstrDefaultValue.startsWith("##")) {
                /**
                 * This is a desriptor : 
                 */
                throw new Exception("You need to escape the # like so \\# or call zx.resolveDirector : " + pstrDefaultValue);
            } else if (pstrDefaultValue.charAt(0) == '#'){
                /**
                 * Ok so we are dealing with a special value : 
                 */
                if (pstrDefaultValue.endsWith("date")) { // #date, #zdate
                    Date objDate = new Date();
                    DateFormat df = getZx().getDateFormat();
                    setValue(df.format(objDate));
                } else if (pstrDefaultValue.endsWith("time")) { // #time, #ztime
                    Date objDate = new Date();
                    DateFormat df = new SimpleDateFormat("HH:mm:ss");
                    setValue(df.format(objDate));
                } else if (pstrDefaultValue.endsWith("now")) {  // #now, #znow
                    Date objDate = new Date();
                    DateFormat df = getZx().getTimestampFormat();
                    setValue(df.format(objDate));
                } else if (pstrDefaultValue.endsWith("clock")) { // #clock, #zclock
                    Date objDate = new Date();
                    setValue("" + StringUtil.right(String.valueOf(objDate.getTime()),'9',9)); // Convert to String. -- Trancate down to 9
                } else if (pstrDefaultValue.endsWith("user") || pstrDefaultValue.endsWith("who")) { // #user, #zuser, #who, #zwho
                    setValue(getZx().getUserProfile().getPKValue());                             
                } else if (pstrDefaultValue.endsWith("true") || pstrDefaultValue.endsWith("yes")) { // #true, #ztrue, #yes, #zyes
                    setValue("true"); 
                } else if (pstrDefaultValue.endsWith("false") || pstrDefaultValue.endsWith("no")) { // #false, #zfalse, #no, #zno
                    setValue("false"); 
                } else if (pstrDefaultValue.endsWith("null")) { // #null, #znull
                    setValue(""); 
                } else {
                    setValue(getZx().getUserProfile().getPreference(pstrDefaultValue.substring(1))); // Else it is a user preference
               }                
            } else {
                /**
                 * False alarm we are not calling the setDefault with special value
                 *  \# indicates that the first # is to be escaped
                 * e.g. \#hello -> #hello
                 */
                if (pstrDefaultValue.startsWith("\\#")) {
                    setValue(pstrDefaultValue.substring(1)); // #hello etc..
                } else {
                    /**
                     * Else it is what it is....
                     */
                    setValue(pstrDefaultValue);
                }                
            }
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Set the property to its default value", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrDefaultValue = "+ pstrDefaultValue);
            }
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }    
    
    //------------------------ Methods that should be implemented and called via super

    /**
     * setValuesToDefault - Reset values to defaults.
     */
    public void setValuesToDefault() {
        this.fkLabel = null;
    }
    
    //------------------------ Object's overidden methods
    
    /**
     * Check if 2 properties are equal.
     * 
     * @param obj The property to check again
     * @see java.lang.Object#equals(java.lang.Object)
     * @return Returns true if the 2 properties are equals
     */
    public boolean equals(Property obj) {
        
        if (this == obj) {
            // Same handle.
            return true;
        } else if (this.equals(obj)) {
            return true;
        } else {
            return this.getStringValue().equals(obj.getStringValue());  
        }
        
    }
    
    /**
     * Use this to get the string format of the object. NOTE : This is only useful during debugging.
     * 
     * NOTE : Uses getStringValue to get the raw string formatted version of the property
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
        // Add the business object name
        if (this.bo != null) {
            toString.append("bo",this.bo.getDescriptor().getName());
        }
        
        // Add the attribute name
        if (this.attribute != null) {
            toString.append("attribute", this.attribute.getName());
        }
        
        toString.append("value", getStringValue());
        
        return toString.toString();
    }

    //------------------------ Utility methods    
    
    /**
     * checkRange - Check against lower and upper range boundaries.
     * 
     * NOTE : At the moment this only works with Date/Long/Double. Also this is
     * only used inside of the Property's class hierarchy.
     * 
     * @param pobjValue The property value that you are trying to check.
     * @return Return whether the range check was sucessfull
     * @throws ZXException Thrown if checkRange fails.
     */
    protected zXType.rc checkRange(Object pobjValue) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjValue", pobjValue);
        }
        
        zXType.rc checkRange = zXType.rc.rcOK;
        
        try {
            boolean blnMatch;
            
            /**
             * Check against lower range boundaries
             */
            if (StringUtil.len(this.attribute.getLowerRange()) > 0) {
                
                if (this.attribute.isLowerRangeInclude()) {
                    blnMatch = (compare(pobjValue, this.attribute.getLowerRange()) >= 0) ? true : false;
                } else {
                    blnMatch = (compare(pobjValue, this.attribute.getLowerRange()) > 0) ? true : false;
                }

                if (blnMatch) {
                    getZx().trace.addError("Value for "
                                            + this.attribute.getLabel().getLabel()
                                            + (this.attribute.isLowerRangeInclude() ? " >" : " >=") 
                                            + " lower range boundary '"
                                            + pobjValue + "', boundary ' "
                                            + this.attribute.getLowerRange()
                                            + "' for attribute " + this.attribute.getName());
                    
                    checkRange = zXType.rc.rcError;
                    return checkRange;
                }
            }

            /**
             * Check against upper range boundaries
             */
            if (StringUtil.len(this.attribute.getUpperRange()) > 0) {
                if (this.attribute.isUpperRangeInclude()) {
                    blnMatch = (compare(pobjValue, this.attribute
                            .getUpperRange()) <= 0) ? true : false;
                } else {
                    blnMatch = (compare(pobjValue, this.attribute
                            .getUpperRange()) < 0) ? true : false;
                }
                
                if (blnMatch) {
                    getZx().trace.addError("Value for "
                                            + this.attribute.getLabel().getLabel()
                                            + (this.attribute.isUpperRangeInclude() ? " >" : " >=") 
                                            + " upper range boundary '"
                                            + pobjValue + "', boundary ' "
                                            + this.attribute.getUpperRange()
                                            + "' for attribute " + this.attribute.getName());
                    
                    checkRange = zXType.rc.rcError;
                    return checkRange;
                }
            }
            
            return checkRange;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Check against lower and upper range boundaries.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjValue = " + pobjValue);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
            return checkRange;
            
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Check if a value object is greater or lesser than the base value.
     * 
     * @param pobjValue The value to check. This can be a Date/Long or String.
     * @param pstrBase The value to check against, this is read from the zx config files.
     * @return the value <code>0</code> if the argument object is equal to
     *         this string; a value less than <code>0</code> if this string
     *         is less than the string argument; and a value greater than
     *         <code>0</code> if this string is greater than the string
     *         argument.
     * @throws ZXException Thrown if it fails to execute compare.
     */
    private int compare(Object pobjValue, String pstrBase) throws ZXException {
        
        if (pobjValue instanceof String) {
            return (pstrBase).compareTo((String)pobjValue);
            
        } else if (pobjValue instanceof Long) {
            return new Long(pstrBase).compareTo((Long)pobjValue);
            
        } else if (pobjValue instanceof Double) {
            return new Double(pstrBase).compareTo((Double)pobjValue);
            
        } else if (pobjValue instanceof java.util.Date) {
            try {
                java.util.Date date = DateUtil.parse(getZx().getDateFormat(), pstrBase);
                return date.compareTo((java.util.Date) pobjValue);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse");
            }
        } else {
            throw new ZXException("Unhandled data type for : " + pobjValue);
        }
        
    }
    
    /**
     * Compare to propertys.
     * 
     * @param v The property to compare against
     * @return the value <code>0</code> if this <code>Property</code> is
     * 		equal to the argument <code>Property</code>; a value less than
     * 		<code>0</code> if this <code>Property</code> is numerically less
     * 		than the argument <code>Property</code>; and a value greater 
     * 		than <code>0</code> if this <code>Property</code> is numerically
     * 		greater than the argument <code>Property</code> (signed
     * 		comparison).
     */
    public int compareTo(Property v) {
    	/**
         * Handle null values
         */ 
        if (isNull) {
            if (v.isNull) {
                return 0;
            }
            return -1;
        } else if (!isNull && v.isNull){
            return 1;
        }
        
        /**
         * Handle the other dataTypes
         */
        if (this instanceof DateProperty && v instanceof DateProperty) {
           return ((DateProperty)this).getValue().compareTo(((DateProperty)v).getValue()); 
           
        } else if (this instanceof BooleanProperty && v instanceof BooleanProperty){
        	boolean blnThis = ((BooleanProperty)this).getValue();
        	boolean blnThat = ((BooleanProperty)v).getValue();
        	return (blnThat == blnThis ? 0 : (blnThat ? 1 : -1));
        	
        } else if (this instanceof LongProperty && v instanceof LongProperty){
            return new Long(((LongProperty)this).getValue()).compareTo(new Long(((LongProperty)v).getValue()));
            
        } else if (this instanceof DoubleProperty && v instanceof DoubleProperty){
            return new Double(((DoubleProperty)this).getValue()).compareTo(new Double(((DoubleProperty)v).getValue()));
            
        } else if (this instanceof ExpressionProperty && v instanceof ExpressionProperty){
            return ((StringProperty)this).getValue().compareTo(((StringProperty)v).getValue());
            
        } else {
            /**
             * The are not of the same type. So we will try out best.
             */
        	
        	/**
        	 * Booleans are treated differently.
        	 */
            if (this instanceof BooleanProperty) {
                return booleanValue() == v.booleanValue() ? 0 : 1; 
            }
            
            return this.getStringValue().compareTo(v.getStringValue());
        }
    }
    
    /**
     * @param penmOperand
     * @param pobjProperty
     * @return Returns the return code of the evaluate.
     */
    public zXType.rc evaluate(zXType.compareOperand penmOperand, Property pobjProperty) {
        zXType.rc evaluate = zXType.rc.rcOK;
        
        /**
         * Early exit.
         */
        if (!comparable(pobjProperty)) {
            evaluate = zXType.rc.rcError;
            return evaluate;
        }
        if (!canEvaluate(this, penmOperand, pobjProperty)) {
            evaluate = zXType.rc.rcError;
            return evaluate;
        }
        
        int intOperand = penmOperand.pos;
        if (intOperand == zXType.compareOperand.coEQ.pos) {
            if (this.compareTo(pobjProperty) == 0) {
                evaluate = zXType.rc.rcOK;
            } else {
                evaluate = zXType.rc.rcWarning;
            }
        } else if (intOperand == zXType.compareOperand.coNE.pos) {
            if (this.compareTo(pobjProperty) != 0) {
                evaluate = zXType.rc.rcOK;
            } else {
                evaluate = zXType.rc.rcWarning;
            }
            
        } else if (intOperand == zXType.compareOperand.coGE.pos) {
            if (this.compareTo(pobjProperty) >= 0) {
                evaluate = zXType.rc.rcOK;
            } else {
                evaluate = zXType.rc.rcWarning;
            }
            
        } else if (intOperand == zXType.compareOperand.coGT.pos) {
            if (this.compareTo(pobjProperty) > 0) {
                evaluate = zXType.rc.rcOK;
            } else {
                evaluate = zXType.rc.rcWarning;
            }
            
        } else if (intOperand == zXType.compareOperand.coLE.pos) {
            if (this.compareTo(pobjProperty) <= 0) {
                evaluate = zXType.rc.rcOK;
            } else {
                evaluate = zXType.rc.rcWarning;
            }
            
        } else if (intOperand == zXType.compareOperand.coLT.pos) {
            if (this.compareTo(pobjProperty) < 0) {
                evaluate = zXType.rc.rcOK;
            } else {
                evaluate = zXType.rc.rcWarning;
            }
            
        } else if (intOperand == zXType.compareOperand.coSW.pos) {
            if (this.getStringValue().startsWith(pobjProperty.getStringValue())) {
                evaluate = zXType.rc.rcOK;
            } else {
                evaluate = zXType.rc.rcWarning;
            }
            
        } else if (intOperand == zXType.compareOperand.coNSW.pos) {
            if (!this.getStringValue().startsWith(pobjProperty.getStringValue())) {
                evaluate = zXType.rc.rcOK;
            } else {
                evaluate = zXType.rc.rcWarning;
            }
            
        } else if (intOperand == zXType.compareOperand.coCNT.pos) {
            if (this.getStringValue().indexOf(pobjProperty.getStringValue()) != -1) {
                evaluate = zXType.rc.rcOK;
            } else {
                evaluate = zXType.rc.rcWarning;
            }
            
        } else if (intOperand == zXType.compareOperand.coNCNT.pos) {
            if (this.getStringValue().indexOf(pobjProperty.getStringValue()) == -1) {
                evaluate = zXType.rc.rcOK;
            } else {
                evaluate = zXType.rc.rcWarning;
            }
        }
        
        return evaluate;
    }
    
    /**
     * Check whether we can do this kind of comparison.
     * 
     * @param pobjLHS The LHS property
     * @param penmOperand The operator
     * @param pobjRHS The RHS property
     * @return Returns true if we can use this type of comparison.
     */
    private static boolean canEvaluate(Property pobjLHS, 
                                	   zXType.compareOperand penmOperand, 
                                	   Property pobjRHS) {
        // NOTE : This is not implemented at the moment.
        // This is fine for now as we are not currently using this features.
        return true;
    }
    
    /**
     * Whether we can compare this propery with another property.
     * 
     * @param pobjProperty The property you want to compare with.
     * @return Returns true if we can compare the 2 properties.
     */
    public boolean comparable(Property pobjProperty) {
        /**
         * The same data type.
         */
        if (this.getClass().equals(pobjProperty.getClass())) return true;
        
        /**
         * Only dates are really fussy about who they compare with.
         */
        if (this instanceof DateProperty) {
            return isDateComparable(this instanceof DateProperty?pobjProperty:this);
        } else if (pobjProperty instanceof DateProperty) {
            return isDateComparable(this);
        }
        
        /**
         * Otherwise properties seem to be happy to compare with each other.
         */
        return true;
    }
    
    private static boolean isDateComparable(Property prop) {
        return !(prop instanceof StringProperty) && !(prop instanceof ExpressionProperty);
    }
    
    //------------------------ Data Type Conversion
    
    /**
     * Try to return the double value of the property.
     * 
     * @return Returns the double value of the property
     */
    public double doubleValue() {
        double dblValue;
        if (this instanceof DoubleProperty) {
            dblValue = ((DoubleProperty)this).getValue();
        } else if (this instanceof LongProperty) {
            dblValue = ((LongProperty)this).getValue();
        } else if (this instanceof BooleanProperty) {
            dblValue = ((BooleanProperty)this).getValue()? 1:0;
        } else if (this instanceof DateProperty) {
            dblValue = ((DateProperty)this).getValue().getTime();
        } else {
            dblValue = new Double(this.getStringValue()).doubleValue();
        }
        return dblValue;
    }
    
    /**
     * Try to return the long value of the property.
     * 
     * <pre>
     * 
     * NOTE: Casting from to double to long will result in some data loss. Also
     * casting a String to long might fail.
     * NOTE : Dates are converted to its timestamp
     * </pre>
     * 
     * @return Returns the long value of the property
     */
    public long longValue() {
        long lngValue;
        if (this instanceof DoubleProperty) {
            lngValue = new Double( ((DoubleProperty)this).getValue() ).longValue();
        } else if (this instanceof LongProperty) {
            lngValue = ((LongProperty)this).getValue();
        } else if (this instanceof BooleanProperty) {
            lngValue = ((BooleanProperty)this).getValue()? 1:0;
        } else if (this instanceof DateProperty) {
            lngValue = ((DateProperty)this).getValue().getTime();
        } else {
            lngValue = new Long(this.getStringValue()).longValue();
        }
        return lngValue;
    }

    /**
     * Try to return the int value of the property.
     * 
     * @return Returns the int value of a property
     */
    public int intValue() {
        int intValue;
        if (this instanceof DoubleProperty) {
            intValue = new Double( ((DoubleProperty)this).getValue() ).intValue();
        } else if (this instanceof LongProperty) {
            intValue = new Long(((LongProperty)this).getValue()).intValue();
        } else if (this instanceof BooleanProperty) {
            intValue = ((BooleanProperty)this).getValue()? 1:0;
        } else if (this instanceof DateProperty) {
            intValue = new Long(((DateProperty)this).getValue().getTime()).intValue();
        } else {
            intValue = new Long(getStringValue()).intValue();
        }
        return intValue;
    }

    /**
     * Try to return the boolean value of the property.
     * 
     * <pre>
     * 
     * NOTE : This default for false, so if it is a date it will be casted to false
     * </pre>
     * 
     * @return Returns the boolean value of the property
     */
    public boolean booleanValue() {
        boolean blnValue = false;
        if (this instanceof BooleanProperty) {
            blnValue = ((BooleanProperty)this).getValue();
        } else {
            blnValue = StringUtil.booleanValue(getStringValue());
        }
        return blnValue;
    }
    
    /**
     * Try to return the date value of the property.
     * 
     * <pre>
     * 
     * NOTE : If the data type is long or double it will try and construct a date.
     * NOTE : If the date is not valid, like a boolean, it will return the current date.
     * </pre>
     * 
     * @return Returns the date value of the property
     */
    public Date dateValue() {
        Date dateValue;
        
        if (this instanceof DateProperty) {
            dateValue = ((DateProperty)this).getValue();
            
        } else if (this instanceof LongProperty){
        	//----
            // It might be a timestamp.
        	//----
            dateValue = new Date(((LongProperty)this).getValue());
            
        } else if (this instanceof DoubleProperty){
        	//----
            // It might be  a timestamp
        	//----
            dateValue = new Date( new Double(((DoubleProperty)this).getValue()).longValue() );
            
        } else {
            DateFormat arrDateFormats[] = {getZx().getTimestampFormat(),
            							   getZx().getDateFormat(),
            							   getZx().getTimeFormat()};
            
            dateValue = DateUtil.parse(arrDateFormats, getStringValue());
            
            if (dateValue == null) {
                getZx().log.error("Failed to parse as a date :" + getStringValue());
            }
        }
        
        return dateValue;
    }
    
    /**
     * Handle null values for the property.
     *
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if handleNull fails. 
     */
    protected zXType.rc handleNull() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc handleNull = zXType.rc.rcOK; 

        try {
            /**
             * Does the desitination property allow null values?
             */
            if (this.bo.isValidate() && !this.attribute.isOptional()) {
                getZx().trace.userErrorAdd(this.attribute.getLabel().getLabel() + " is not optional.", "", 
                						   this.attribute.getName());
                handleNull = zXType.rc.rcError;
                return handleNull;
            }
            
            /**
             * Set persist status to dirty
             */
            if (!this.isNull) {
                setPersistStatusDirty();
            }
            
            /**
             * Mark as null and, for safety, reset values
             */
            setNull(true);
            setValuesToDefault();
            
            return handleNull;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Handle null values for the property.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            handleNull = zXType.rc.rcError;
            return handleNull;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(handleNull);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Try to resolve the foriegn key label for a propery.
     * 
     * BD11JUL05 - V1.5:31 - Reset FKLabel
     * 
     * @return Returns the formatted value for the foriegn key.
     * @throws ZXException Thrown if fkformattedValue failed.
     */
    protected String fkformattedValue() throws ZXException {
        String fkformattedValue = "";
        
        if(StringUtil.len(this.fkLabel) > 0) {
            fkformattedValue = this.fkLabel;
            
        } else {
        	if (resolveFKLabel(false).pos != zXType.rc.rcOK.pos) {
        		throw new ZXException("Unable to resolve FK label", this.attribute.getName());
        	}
        	
        	fkformattedValue = this.fkLabel;
        }
        
        return fkformattedValue;
    }
    
	/**
	 * Sets the FKLabel string of me.
	 * 
	 * Assumes   :
	 * 	- me has been set or noLoad is true
	 * 	- FKLabel will be set on success.
	 *
	 * @param pblnNoLoad Indicates that values do never have to be loaded; 
	 * 					 use this when you know the FKBO has been loaded already.
	 * 					 Optional default should be false.
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if resolveFKLabel fails. 
	 */
	public zXType.rc resolveFKLabel(boolean pblnNoLoad) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pblnNoLoad", pblnNoLoad);
		}
		
		zXType.rc resolveFKLabel = zXType.rc.rcOK; 
		
		try {
			
			/**
			 * Check if FKBO has yet been created
			 */
			if (this.fkbo == null) {
				if (getFKBO() == null) {
					getZx().trace.addError("Unable to get FK BO", this.attribute.getName());
					resolveFKLabel = zXType.rc.rcError;
					return resolveFKLabel;
				}
			}
			
			/**
			 * Determine the FK label group to use
			 */
			String strLabelGroup;
			if (StringUtil.len(this.attribute.getFkLabelGroup()) > 0) {
				strLabelGroup = this.attribute.getFkLabelGroup();
			} else {
				strLabelGroup = "label";
			}
			
			if (!pblnNoLoad) {
				/**
				 * Make sure that appropriate fields of FKBO have been loaded
				 */
				this.fkbo.setPKValue(getStringValue());
				
				if (this.fkbo.ensureGroupIsLoaded(strLabelGroup).pos != zXType.rc.rcOK.pos) {
					getZx().trace.addError("Unable to ensure label group is loaded for FK BO", 
										   this.attribute.getName() + " / " + strLabelGroup);
					resolveFKLabel = zXType.rc.rcError;
					return resolveFKLabel;
				}
				
			} // Never load?
			
			if (StringUtil.len(this.attribute.getFkLabelExpression()) == 0) {
				this.fkLabel = this.fkbo.formattedString(strLabelGroup);
				
			} else {
				/**
				 * Expression can refer to zXFK
				 */
				getZx().getBOContext().setEntry("zXFK", this.fkbo);
				
				Property objProperty = getZx().getExpressionHandler().eval(this.attribute.getFkLabelExpression());
				
				if (objProperty == null) {
					getZx().trace.addError("Unable to resolve FK expression for FK attribute", this.attribute.getName());
					resolveFKLabel = zXType.rc.rcError;
					return resolveFKLabel;
				}
				
				this.fkLabel = objProperty.getStringValue();
				
			} // FK label expression?
			
			return resolveFKLabel;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Sets the FKLabel string of me.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pblnNoLoad = "+ pblnNoLoad);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			resolveFKLabel = zXType.rc.rcError;
			return resolveFKLabel;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(resolveFKLabel);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * @param pobjOriginal The current property value.
	 * @param pobjNew The new property value
	 */
	protected void updateFKLabel(Object pobjOriginal, Object pobjNew) {
		/**
		 * Short circuit if fklabel is already empty.
		 */
		if (StringUtil.len(this.fkLabel) == 0) return;
		
		if (this.attribute != null && StringUtil.len(this.attribute.getForeignKey()) > 0) {
			if (pobjOriginal == null || !pobjOriginal.equals(pobjNew)) {
				this.fkLabel = "";
				
				try {
					if (this.fkbo != null) {
		                if (StringUtil.len(this.attribute.getFkLabelGroup()) == 0) {
		                	this.fkbo.setPropertyPersistStatus("label", 
		                									   zXType.propertyPersistStatus.ppsReset, null);
		                } else {
		                	this.fkbo.setPropertyPersistStatus(this.attribute.getFkLabelGroup(), 
		                									   zXType.propertyPersistStatus.ppsReset, null);
		                }
		                
					} // FKBO had been set?
					
				} catch (Exception e) {
					throw new NestableRuntimeException(e);
				}
				
			} // New PK ?
		}
	}
}