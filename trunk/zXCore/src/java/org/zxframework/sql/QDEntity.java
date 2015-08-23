/*
 * Created on Jan 14, 2004 by michael
 * $Id: QDEntity.java,v 1.1.2.3 2005/07/12 07:28:56 mike Exp $
 */
package org.zxframework.sql;

import org.zxframework.AttributeCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXObject;
import org.zxframework.util.StringUtil;

/**
 * The query def entity class.
 *
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class QDEntity extends ZXObject {
    
    //------------------------ Members
    
    /** The Business object linked to this Query Definition object. */
	private ZXBO bo;
	/** The name of this entity for uniqueless. */
	private String entity;
	/** The attribute group to use. */
	private String group;
	/** Whether SQL alias to use if at all. */
	private String alias;
	/** The entity name. */
	private String name;
	/** The attribute group collection derived from the group. */
	private AttributeCollection attrGroup;
	/** Whether to resolve the foriegn keys. */
    private boolean resolvefk;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public QDEntity() {
        super();
    }
    
    //------------------------ Getters and Setters
	
    /**
     * The Business object linked to this Query Definition object.
     * 
     * @return Returns the bo.
     */
    public ZXBO getBo() {
        return bo;
    }
    
    /**
     * @param bo The bo to set.
     */
    public void setBo(ZXBO bo) {
        this.bo = bo;
    }
    
	/**
	 * Whether SQL alias to use if at all.
	 * 
	 * @return Returns the alias.
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias The alias to set.
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * The attribute group collection derived from the group.
	 * 
	 * @return Returns the attrGroup.
	 */
	public AttributeCollection getAttrGroup() {
	    if (this.attrGroup == null) {
	        this.attrGroup = new AttributeCollection();
	    }
		return attrGroup;
	}

	/**
	 * @param attrGroup The attrGroup to set.
	 */
	public void setAttrGroup(AttributeCollection attrGroup) {
		this.attrGroup = attrGroup;
	}

	/**
	 * @return Returns the entity.
	 */
	public String getEntity() {
		return entity;
	}

	/**
	 * @param entity The entity to set.
	 */
	public void setEntity(String entity) {
		this.entity = entity;
	}

	/**
	 * The attribute group to use.
	 * 
	 * @return Returns the group.
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group The group to set.
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * The entity name.
	 * 
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Whether to resolve the foriegn keys.
	 * 
	 * @return Returns the resolvefk.
	 */
	public boolean isResolvefk() {
		return resolvefk;
	}
	
	/**
	 * @param resolvefk The resolvefk to set.
	 */
	public void setResolvefk(boolean resolvefk) {
		this.resolvefk = resolvefk;
	}
	
	/**
	 * Get a string formatted version 
	 * 
	 * @return Return the string formatted version 
	 */
	public String getResolveFKAsString() {
		return resolvefk?"true":"false";
	}
	
	/**
	 * Set the resolveFk with a string. 
	 * 
	 * @param pstr The resolvefk setting as a string.
	 */
	public void setResolveFKAsString(String pstr) {
		if(StringUtil.booleanValue(pstr)) {
			resolvefk = true;
		} else {
			resolvefk = false;
		}
	}
}