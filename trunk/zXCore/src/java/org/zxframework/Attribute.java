/*
 * Created on Jan 13, 2004 by Michael Brewer
 * $Id: Attribute.java,v 1.1.2.16 2006/07/17 15:38:10 mike Exp $
 */
package org.zxframework;

import java.util.Map;

import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;

/**
 * Represents a business object attribute.
 *
 * <pre>
 * The class contains all of the metadata necessary to discribe a business object attribute.
 * This object is clonable and serialisable.
 * 
 * Who    : Bertus Dispa
 * When   : 28 August 2002
 * 
 * Change    : BD14MAR03
 * Why       : Added case insensitive text case behaviour
 *
 * Change    : DGS13JUN2003
 * Why       : Added 'comment' property.
 *
 * Change    : DGS11AUG2003
 * Why       : Added 'inherited' property.
 *
 * Change    : DGS13OCT2003
 * Why       : Added attribute tags.
 *
 * Change    : BD20MAR04
 * Why       : Added isExplicit flag + tag support
 *
 * Change    : BD13JUN04
 * Why       : Added dynamicValue
 * 
 * Change    : BD18JUN05 - V1.5:18
 * Why       : Added support for ensureLoadGroup
 *  
 * Change    : BD30JUN05 - V1.5:20
 * Why       : Added support for enhanced FK label behaviour
 * 
 * Change    : BD01JUL05 - V1.5:25
 * Why       : Added support for sort attribute for dynamic attributes
 * 
 * Change    : BD13FEB06 - V1.5:91
 * Why       : Added support for test data generation
 * 
 * </pre>
 *
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.2
 */
public class Attribute implements Cloneable {

    //------------------------ Members  
    
    /** The name of the attribute. This is also the key in attribute group or business object. */
    private String name;
    /** Whether this attribute is inherited from another. */
    private boolean inherited;
    /** Documentation for the attribute. Only useful for developers */
    private String comment;
    /** Edit mask for simple validation of this field. */
    private String editMask;
    /** Used to store a cached version of the result set name. */
    private String trueRSName;
    /** The data type of the attribute. Like String or Date. This determines what type of property it will use. */
    private zXType.dataType dataType;
    /** If this is a foriegn key the search method will be used to determine how this will get displayed. */
    private zXType.searchMethod searchMethod;
    /** The case of the attribute when it gets displayed. */
    private zXType.textCase textCase;
    private String fkRelAttr;
    /** Whether this field is locked or not. Used to determine how this field is displayed. */
    private boolean locked;
    /** The label of this attribute, this supports internationalisation. */
    private LabelCollection label;
    /** The description of this attribute. This is used in the tooltip of the field. */
    private ZXCollection description;
    /** Each feild can have some associated help. */
    private String helpId;
    /** Whether this is a password field, used for display and storage purposes. */
    private boolean password;
    /** Whether to display the attribute over multiple lines. This would generate a textarea rather than a textfield. */
    private boolean multiLine;
    /** The length of the field, normally the same or less than the length of the field in the database. */
    private int length;
    /** The level of precision of this attribute, used by doubles for example. */
    private int precision;
    /** Whether this field is optional. Check in a edit form for example. */
    private boolean isOptional;
    /** The database column for the attribute. */
    private String column;
    /** Whether this attribute is actually mapped to a real db column of is a dynamic value. */
    private boolean virtualColumn;
    /** The output length of this attribute, used for display purposes. */
    private int outputLength;
    /** The lower range of values for the Properties. */
    private String lowerRange;
    private boolean lowerRangeInclude;
    private String upperRange;
    private boolean upperRangeInclude;
    /** Output mask for how to display this attribute's value. */
    private String outputMask;
    /** A regular expression used to validate the input for this attribute. */
    private String regularExpression;
    /** The name of the business object (table) this attribute (foriegn key) points to. */
    private String foreignKey = "";
    private String foreignKeyAlias;
    /** The default value for this object. */
    private String defaultValue;
    /** A collection of possible values for this attribute. Used normally for small fixed lists like gender or status. */
    private Map options;
    /** Whether to display this attribute as a combobox. (Used in the presentation) */
    private boolean combobox;
    /** DGS13OCT2003 Added tags. A collection (Tuple) of tags. */
    private ZXCollection tags;
    /** 
     * Flag to indicate that this attribute is only to be included in attribute groups when named expicitly. 
     * i.e. not in '*' 
     * */
    private boolean explicit;
    /** Epxression that yields a value at run-time (only resolved if the column is not provided). */
    private String dynamicValue;
    
    /** Attribute to sort by (must have column) only relevant for dynamic value attributes. */
    private String sortAttr;
    /** Group to use when generating FK label if you do no want to use the label attribute group. */
    private String fkLabelGroup;
    /** Optional expression that is used to format the FK label. */
    private String fkLabelExpression;
    /** Group that must be loaded when referring to this attribute. */
    private String ensureLoadGroup;
    
    /** BD13FEB06 - Added */
    private String testDataValue;
    
    //------------------------ Constructor    
    
    /**
     * Default constructor.
     */
    public Attribute() {
        super();
    }
    
    //------------------------ Getters and Setters    
    
    /**
     * Documentation for the attribute. Only useful for developers
     * 
     * @return Returns the comment.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @param pstrComment The comment to set.
     */
    public void setComment(String pstrComment) {
        this.comment = pstrComment;
    }

    /**
     * Collection of tags (name as key)
     * 
     * @return Returns the tags.
     */
    public ZXCollection getTags() {
        if (this.tags == null) {
            this.tags = new ZXCollection();
        }
        return tags;
    }
    
    /**
     * @param tags The tags to set.
     */
    public void setTags(ZXCollection tags) {
        this.tags = tags;
    }
    
    /**
     * The data type of the attribute. Like String or Date. 
     * This determines what type of property it will use.
     * 
     * @return Returns the dataType object.
     */
    public zXType.dataType getDataType() {
        return this.dataType;
    }
    
    /**
     * @param dataType The dataType to set.
     */
    public void setDataType(zXType.dataType dataType) {
        this.dataType = dataType;
    }

    /**
     * Edit mask for simple validation of this field.
     * 
     * @return Returns the editMask.
     */
    public String getEditMask() {
        return this.editMask;
    }

    /**
     * @param pstrEditMask The editMask to set.
     */
    public void setEditMask(String pstrEditMask) {
        this.editMask = pstrEditMask;
    }

    /**
     * Used to store a cached version of the result set name. 
     * This is not stored in the BO descriptor.
     * 
     * @return Returns the trueRSName.
     */
    public String getTrueRSName() {
        return this.trueRSName;
    }
    
    /**
     * Used to store a cached version of the result set name.
     * 
     * Do NOT use a cache for the standard zX columns as these are shared
     * by all BOs and caching any BO specific value would end in tears.
     * 
     * @param pstrTrueRSName The trueRSName to set.
     */
    public void setTrueRSName(String pstrTrueRSName) {
        this.trueRSName = pstrTrueRSName;
    }

    /**
     * If this is a foriegn key the search method will be used to determine 
     * how this will get displayed.
     * 
     * @return Returns the searchMethod.
     */
    public zXType.searchMethod getSearchMethod() {
        return this.searchMethod;
    }

    /**
     * @param penmSearchMethod The searchMethod to set.
     */
    public void setSearchMethod(zXType.searchMethod penmSearchMethod) {
        this.searchMethod = penmSearchMethod;
    }
    
    /**
     * The case of the attribute when it gets displayed.
     * 
     * @return Returns the textCase.
     */
    public zXType.textCase getTextCase() {
        return this.textCase;
    }

    /**
     * @param penmTextCase The textCase to set.
     */
    public void setTextCase(zXType.textCase penmTextCase) {
        this.textCase = penmTextCase;
    }

    /**
     * The database column for the attribute.
     * 
     * @return Returns the column.
     */
    public String getColumn() {
        return this.column;
    }

    /**
     * @param pstrColumn The column to set.
     */
    public void setColumn(String pstrColumn) {
        this.column = pstrColumn;
    }

    /**
     * Whether to display this attribute as a combobox. 
     * (Used in the presentation)
     * 
     * @return Returns the combobox.
     */
    public boolean isCombobox() {
        return this.combobox;
    }
    
    /**
     * Should a combobox be shown for this attr
     * 
     * @param pblnCombobox The combobox to set.
     */
    public void setCombobox(boolean pblnCombobox) {
        this.combobox = pblnCombobox;
    }

    /**
     * The default value for this object.
     * 
     * Used when calling resetBO or when displaying a Edit form for the first time, 
     * this value will be used to populate the property.
     * 
     * @return Returns the defaultValue.
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * @param pstrDefaultValue The defaultValue to set.
     */
    public void setDefaultValue(String pstrDefaultValue) {
        this.defaultValue = pstrDefaultValue;
    }

    /**
     * The description of this attribute. This is used in the tooltip of the field.
     * 
     * @return Returns the description.
     */
    public ZXCollection getDescription() {
        if (this.description == null) {
            this.description = new ZXCollection();
        }
        return this.description;
    }

    /**
     * @param pcolDescription The description to set.
     */
    public void setDescription(ZXCollection pcolDescription) {
        
        this.description = pcolDescription;
    }
    
    /**
     * @return Returns the fkRelAttr.
     */
    public String getFkRelAttr() {
        return this.fkRelAttr;
    }

    /**
     * @param pstrFKRelAttr The fkRelAttr to set.
     */
    public void setFkRelAttr(String pstrFKRelAttr) {
        this.fkRelAttr = pstrFKRelAttr;
    }

    /**
     * The name of the business object (table) this attribute (foriegn key) points to.
     * 
     * @return Returns the foreignKey.
     */
    public String getForeignKey() {
        return this.foreignKey;
    }

    /**
     * @param foreignKey The foreignKey to set.
     */
    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    /**
     * Alias to use for foreign key descriptor
     * 
     * @return Returns the foreignKeyAlias.
     */
    public String getForeignKeyAlias() {
        return this.foreignKeyAlias;
    }

    /**
     * @param pstrForeignKeyAlias The foreignKeyAlias to set.
     */
    public void setForeignKeyAlias(String pstrForeignKeyAlias) {
        this.foreignKeyAlias = pstrForeignKeyAlias;
    }

    /**
     * Each feild can have some associated help.
     * 
     * @return Returns the helpId.
     */
    public String getHelpId() {
        return this.helpId;
    }

    /**
     * @param pstrHelpId The helpId to set.
     */
    public void setHelpId(String pstrHelpId) {
        this.helpId = pstrHelpId;
    }

    /**
     * Whether this field is optional. Check in a edit form for example.
     * 
     * @return Returns the isOptional.
     */
    public boolean isOptional() {
        return this.isOptional;
    }
    
    /**
     * @param pblnIsOptional The isOptional to set.
     */
    public void setOptional(boolean pblnIsOptional) {
        this.isOptional = pblnIsOptional;
    }

    /**
     * The label of this attribute, this supports internationalisation.
     * 
     * @return Returns the label.
     */
    public LabelCollection getLabel() {
        return this.label;
    }
    
    /**
     * @param pcolLabel The label to set.
     */
    public void setLabel(LabelCollection pcolLabel) {
        this.label = pcolLabel;
    }

    /**
     * The length of the field, normally the same or less than the length of the field in the database.
     * 
     * @return Returns the length.
     */
    public int getLength() {
        return this.length;
    }

    /**
     * @param pintLength The length to set.
     */
    public void setLength(int pintLength) {
        this.length = pintLength;
    }

    /**
     * Whether this field is locked or not. Used to determine how this field is displayed.
     * 
     * @return Returns the locked.
     */
    public boolean isLocked() {
        return this.locked;
    }
    
    /**
     * @param pblnLocked The locked to set.
     */
    public void setLocked(boolean pblnLocked) {
        this.locked = pblnLocked;
    }

    /**
     * The lower range of values for the Properties.
     * 
     * @return Returns the lowerRange.
     */
    public String getLowerRange() {
        return this.lowerRange;
    }

    /**
     * @param pstrLowerRange The lowerRange to set.
     */
    public void setLowerRange(String pstrLowerRange) {
        this.lowerRange = pstrLowerRange;
    }

    /**
     * @return Returns the lowerRangeInclude.
     */
    public boolean isLowerRangeInclude() {
        return this.lowerRangeInclude;
    }

    /**
     * @param pblnLowerRangeInclude The lowerRangeInclude to set.
     */
    public void setLowerRangeInclude(boolean pblnLowerRangeInclude) {
        this.lowerRangeInclude = pblnLowerRangeInclude;
    }
    
    /**
     * Whether to display the attribute over multiple lines. This would generate a textarea rather than a textfield.
     * 
     * @return Returns the multiLine.
     */
    public boolean isMultiLine() {
        return this.multiLine;
    }
    
    /**
     * @param pblnMultiLine The multiLine to set.
     */
    public void setMultiLine(boolean pblnMultiLine) {
        this.multiLine = pblnMultiLine;
    }
    
    /**
     * This returns the name of the attribute, which is also the key of the object.
     * NOTE : Names of attributes are lower case for case insensitive matches.
     * 
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param pstrName The name to set.
     */
    public void setName(String pstrName) {
        this.name = pstrName;
    }
    
    /**
     * A collection (Option) of possible values for this attribute. 
     * Used normally for small fixed lists like gender or status.
     * 
     * @return Returns the options.
     */
    public Map getOptions() {
        if (this.options == null) {
            this.options = new ZXCollection();
        }
        return this.options;
    }
    
    /**
     * @param pcolOptions The options to set.
     */
    public void setOptions(Map pcolOptions) {
        this.options = pcolOptions;
    }

    /**
     * The output length of this attribute, used for display purposes.
     * 
     * @return Returns the outputLength.
     */
    public int getOutputLength() {
        return this.outputLength;
    }

    /**
     * @param pintOutputLength The outputLength to set.
     */
    public void setOutputLength(int pintOutputLength) {
        this.outputLength = pintOutputLength;
    }

    /**
     * Output mask for how to display this attribute's value.
     * 
     * @return Returns the outputMask.
     */
    public String getOutputMask() {
        return this.outputMask;
    }

    /**
     * @param pstrOutputMask The outputMask to set.
     */
    public void setOutputMask(String pstrOutputMask) {
        this.outputMask = pstrOutputMask;
    }

    /**
     * Whether this is a password field, used for display and storage purposes.
     * 
     * @return Returns the password.
     */
    public boolean isPassword() {
        return this.password;
    }

    /**
     * @param pblnPassword The password to set.
     */
    public void setPassword(boolean pblnPassword) {
        this.password = pblnPassword;
    }

    /**
     * The level of precision of this attribute, used by doubles for example.
     * 
     * @return Returns the precision.
     */
    public int getPrecision() {
        return this.precision;
    }

    /**
     * @param pintPrecision The precision to set.
     */
    public void setPrecision(int pintPrecision) {
        this.precision = pintPrecision;
    }

    /**
     * @return Returns the regularExpression.
     */
    public String getRegularExpression() {
        return this.regularExpression;
    }

    /**
     * @param pstrRegularExpression The regularExpression to set.
     */
    public void setRegularExpression(String pstrRegularExpression) {
        this.regularExpression = pstrRegularExpression;
    }

    /**
     * @return Returns the upperRange.
     */
    public String getUpperRange() {
        return this.upperRange;
    }

    /**
     * @param pstrUpperRange The upperRange to set.
     */
    public void setUpperRange(String pstrUpperRange) {
        this.upperRange = pstrUpperRange;
    }

    /**
     * @return Returns the upperRangeInclude.
     */
    public boolean isUpperRangeInclude() {
        return this.upperRangeInclude;
    }

    /**
     * @param pblnUpperRangeInclude The upperRangeInclude to set.
     */
    public void setUpperRangeInclude(boolean pblnUpperRangeInclude) {
        this.upperRangeInclude = pblnUpperRangeInclude;
    }

    /**
     * @return Returns the virtualColumn.
     */
    public boolean isVirtualColumn() {
        return this.virtualColumn;
    }

    /**
     * @param pblnVirtualColumn The virtualColumn to set.
     */
    public void setVirtualColumn(boolean pblnVirtualColumn) {
        this.virtualColumn = pblnVirtualColumn;
    }

    /**
     * Whether this attribute is inherited from another.
     * 
     * @return Returns the inherited.
     */
    public boolean isInherited() {
        return this.inherited;
    }

    /**
     * @param pblnInherited The inherited to set.
     */
    public void setInherited(boolean pblnInherited) {
        this.inherited = pblnInherited;
    }
    
    /**
     * Flag to indicate that this attribute is only to be included 
     * in attribute groups when named expicitly.
     *  
     * i.e. not in '*'
     * 
     * @return Returns the explicit.
     */
    public boolean isExplicit() {
        return explicit;
    }
    
    /**
     * @param pblnExplicit The explicit to set.
     */
    public void setExplicit(boolean pblnExplicit) {
        this.explicit = pblnExplicit;
    }
    
    /**
     * Epxression that yields a value at run-time (only resolved if the column is not provided).
     * 
     * @return Returns the dynamicValue.
     */
    public String getDynamicValue() {
        return dynamicValue;
    }
    
    /**
     * @param pstrDynamicValue The dynamicValue to set.
     */
    public void setDynamicValue(String pstrDynamicValue) {
        this.dynamicValue = pstrDynamicValue;
    }
    
    /**
     * Group that must be loaded when referring to this
     * attribute (can be very usefull when dealing with
     * dynamic attribute values or in event actions).
     * 
     * BD17JUN05 - V1.5:18
     * 
	 * @return Returns the ensureLoadGroup.
	 */
	public String getEnsureLoadGroup() {
		return ensureLoadGroup;
	}

	/**
	 * @param ensureLoadGroup The ensureLoadGroup to set.
	 */
	public void setEnsureLoadGroup(String ensureLoadGroup) {
		this.ensureLoadGroup = ensureLoadGroup;
	}

	/**
	 * Optional expression that is used to format the FK label.
	 * 
	 * BD30JUN05 - V1.5:20
	 * 
	 * @return Returns the fkLabelExpression.
	 */
	public String getFkLabelExpression() {
		return fkLabelExpression;
	}

	/**
	 * @param fkLabelExpression The fkLabelExpression to set.
	 */
	public void setFkLabelExpression(String fkLabelExpression) {
		this.fkLabelExpression = fkLabelExpression;
	}

	/**
	 * Group to use when generating FK label if you do no want to use the label attribute group.
	 * 
	 * BD30JUN05 - V1.5:20
	 * 
	 * @return Returns the fkLabelGroup.
	 */
	public String getFkLabelGroup() {
		return fkLabelGroup;
	}

	/**
	 * @param fkLabelGroup The fkLabelGroup to set.
	 */
	public void setFkLabelGroup(String fkLabelGroup) {
		this.fkLabelGroup = fkLabelGroup;
	}

	/**
	 * Attribute to sort by (must have column) only relevant for dynamic value attributes.
	 * 
	 * BD1JUL05 - V1.5:21
	 * 
	 * @return Returns the sortAttr.
	 */
	public String getSortAttr() {
		return sortAttr;
	}

	/**
	 * @param sortAttr The sortAttr to set.
	 */
	public void setSortAttr(String sortAttr) {
		this.sortAttr = sortAttr;
	}
	
    /**
	 * @return the testDataValue
	 */
	public String getTestDataValue() {
		return testDataValue;
	}
	
	/**
	 * @param testDataValue the testDataValue to set
	 */
	public void setTestDataValue(String testDataValue) {
		this.testDataValue = testDataValue;
	}
	
	//------------------------ Public helper methods

    /**
     * @return Returns String form of dataType.
     */
    public String getDataTypeAsString() {
        return zXType.valueOf(getDataType());
    }
    
    /**
     * @return Returns the String form of searchMethod
     */
    public String getSearchMethodAsString() {
        return zXType.valueOf(getSearchMethod());
    }

    /**
     * @return Returns the String form of textCase
     */
    public String getTextCaseAsString() {
        return zXType.valueOf(getTextCase());
    }
    
    /**
     * String representation of the label
     * 
     * @return Returns the label in string form.
     */
    public String getLabelAsString() {
        return this.label.toString();
    }
    
    //------------------------ Utility method
    
    /**
     * Does this descriptor have a certain tag.
     * 
     * @param pstrTag The name of the tag to look for.
     * @return Returns true if this descriptor has a certian tag
     */
    public boolean hasTag(String pstrTag) {
        return this.tags != null && this.tags.get(pstrTag) != null;
    }

    /**
     * Get the value of a named tag.
     * 
     * @param pstrTag The name of the tag to get the value of.
     * @return Returns the value of a tag or a empty in case
     * 		   the tag had not been found
     */
    public String tagValue(String pstrTag) {
		String tagValue = "";
        
		if (this.tags != null) {
			Tuple objTuple = (Tuple) this.tags.get(pstrTag);
			if (objTuple != null) {
				tagValue = objTuple.getValue();
			}
		}
        
		return tagValue;
    }
    
    /**
     * Set a certain tag; same as remove when value is ''.
     * 
     * @param pstrName The name of the tag
     * @param pstrValue The value of the tag, if this is "" then the tag is removed.
     */
    public void setTag(String pstrName, String pstrValue) {
        if (StringUtil.len(pstrValue) > 0) {
            Tuple objTuple = new Tuple();
            objTuple.setName(pstrName);
            objTuple.setValue(pstrValue);
            
            getTags().put(pstrName, objTuple);
            
        } else {
            /** Just delete the tag. */
            if (this.tags != null) {
                this.tags.remove(pstrName);
            }
        }
    }
    
    //------------------------ Overloading objects methods

	/**
     * Does a very deep clone of the attribute. 
     * This is used for when we apply entity messaging 
     * and cache the business object descriptor.
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        /** Construct a new Attribute object. */
        Attribute objAttr = new Attribute();
        
        objAttr.setColumn(getColumn());
        objAttr.setCombobox(isCombobox());
        objAttr.setComment(getComment());
        objAttr.setDataType(getDataType());
        objAttr.setDefaultValue(getDefaultValue());
        objAttr.setDynamicValue(getDynamicValue());
        objAttr.setEditMask(getEditMask());
        objAttr.setExplicit(isExplicit());
        objAttr.setEnsureLoadGroup(getEnsureLoadGroup());
        objAttr.setFkLabelExpression(getFkLabelExpression());
        objAttr.setFkLabelGroup(getFkLabelGroup());
        objAttr.setFkRelAttr(getFkRelAttr());
        objAttr.setForeignKey(getForeignKey());
        objAttr.setForeignKeyAlias(getForeignKeyAlias());
        objAttr.setHelpId(getHelpId());
        objAttr.setInherited(isInherited());
        
        if (getLabel() != null) {
            objAttr.setLabel((LabelCollection)getLabel().clone());
        }
        
        objAttr.setLength(getLength());
        objAttr.setLocked(isLocked());
        objAttr.setLowerRange(getLowerRange());
        objAttr.setLowerRangeInclude(isLowerRangeInclude());
        objAttr.setMultiLine(isMultiLine());
        objAttr.setName(getName());
        objAttr.setOptional(isOptional());
        
        objAttr.setOptions(getOptions());
        
        objAttr.setOutputLength(getOutputLength());
        objAttr.setOutputMask(getOutputMask());
        objAttr.setPassword(isPassword());
        objAttr.setPrecision(getPrecision());
        objAttr.setRegularExpression(getRegularExpression());
        objAttr.setSearchMethod(getSearchMethod());
        objAttr.setSortAttr(getSortAttr());
        
        if (this.tags != null) {
            objAttr.setTags((ZXCollection)this.tags.clone());
        }
        
        objAttr.setTextCase(getTextCase());
        objAttr.setTrueRSName(getTrueRSName());
        objAttr.setUpperRange(getUpperRange());
        objAttr.setUpperRangeInclude(isUpperRangeInclude());
        objAttr.setVirtualColumn(isVirtualColumn());
        
        return objAttr;
    }
    
    /**
     * @see java.lang.Object#toString()
     **/
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		toString.append("name", getName());
		toString.append("dataType", getDataTypeAsString());
        return toString.toString();
    }


}