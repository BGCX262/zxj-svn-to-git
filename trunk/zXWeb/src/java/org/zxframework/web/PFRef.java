/*
 * Created on Apr 9, 2004 by Michael Brewer
 * $Id: PFRef.java,v 1.1.2.15 2006/07/17 13:58:59 mike Exp $ 
 */
package org.zxframework.web;

import org.zxframework.CloneableObject;
import org.zxframework.LabelCollection;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;

/**
 * Pageflow ref object which may be a submit button or a image link.
 * 
 * <pre>
 * This is usually for the submit buttons on forms.
 * 
 * Change    : DGS18FEB2003
 * Why       : Added img and imgOver properties
 *
 * Change    : DGS28FEB2003
 * Why       : Implement popups - added 'start sub menu' property used by popups
 * 
 * Change    : MB04SEP05  V1.5:44
 * Why       : Added support for access-key, description and tab-index (BD15NOV04)
 * 
 * Change    : V1.5:95 DGS28APR2006
 * Why       : New feature to be able to make ref buttons appear at the top of forms
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFRef implements CloneableObject {
    
    //------------------------ Members
    
    private String name;
    private boolean startsubmenu;
    private String img;
    private String imgover;
    private PFUrl url;
    private LabelCollection label;
    private LabelCollection confirm;
    private zXType.pageflowRefType refType;
    private LabelCollection description;
    private LabelCollection accesskey;
    private String tabindex;
    
    /** v1.5:95 DGS28APR2006: Property to align buttons at the top (like a menubar) */
    private zXType.pageflowElementAlign enmAlign;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFRef() {
        super();
        
        /**
         * Default to bottom
         */
        this.enmAlign = zXType.pageflowElementAlign.peaBottom;
    }
    
    //------------------------ Getters and Setters
    
    /**
     * A confiirmation message to display.
     * 
     * @return Returns the confirm.
     */
    public LabelCollection getConfirm() {
        return confirm;
    }
    
    /**
     * A confiirmation message to display.
     * 
     * @param confirm The confirm to set.
     */
    public void setConfirm(LabelCollection confirm) {
        this.confirm = confirm;
    }
    
    /**
     * The image to use for the ref.
     * 
     * @return Returns the img.
     */
    public String getImg() {
        return img;
    }
    
    /**
     * The image to use for the ref.
     * 
     * @param img The img to set.
     */
    public void setImg(String img) {
        this.img = img;
    }
    
    /**
     * The rollover button to display.
     * 
     * @return Returns the imgover.
     */
    public String getImgover() {
        return imgover;
    }
    
    /**
     * @param imgover The imgover to set.
     */
    public void setImgover(String imgover) {
        this.imgover = imgover;
    }
    
    /**
     * The label for the button/picture.
     * 
     * @return Returns the label.
     */
    public LabelCollection getLabel() {
        return label;
    }
    
    /**
     * @param label The label to set.
     */
    public void setLabel(LabelCollection label) {
        this.label = label;
    }
    
    /**
     * The name of the ref button or image.
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
        // Converting this object to a non ZXObject.
        // setKey(name);
    }
    
    /**
     * The type of ref button to display.
     * 
     * @return Returns the refType.
     */
    public zXType.pageflowRefType getRefType() {
        return refType;
    }
    
    /**
     * @param refType The refType to set.
     */
    public void setRefType(zXType.pageflowRefType refType) {
        this.refType = refType;
    }
    
    /**
     * Whether to start a sub menu.
     * 
     * @return Returns the startsubmenu.
     */
    public boolean isStartsubmenu() {
        return startsubmenu;
    }
    
    /**
     * @param startsubmenu The startsubmenu to set.
     */
    public void setStartsubmenu(boolean startsubmenu) {
        this.startsubmenu = startsubmenu;
    }
    
	/**
     * The link of the ref button.
     * 
     * @return Returns the url.
     */
    public PFUrl getUrl() {
        return url;
    }
    
    /**
     * @param url The url to set.
     */
    public void setUrl(PFUrl url) {
        this.url = url;
    }
    
    /**
	 * @return Returns the accesskey.
	 */
	public LabelCollection getAccesskey() {
		return accesskey;
	}

	/**
	 * @param accesskey The accesskey to set.
	 */
	public void setAccesskey(LabelCollection accesskey) {
		this.accesskey = accesskey;
	}

	/**
	 * @return Returns the description.
	 */
	public LabelCollection getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(LabelCollection description) {
		this.description = description;
	}

	/**
	 * @return the enmAlign
	 */
	public zXType.pageflowElementAlign getEnmAlign() {
		return enmAlign;
	}

	/**
	 * @param enmAlign the enmAlign to set
	 */
	public void setEnmAlign(zXType.pageflowElementAlign enmAlign) {
		this.enmAlign = enmAlign;
	}

	/**
	 * @return Returns the tabindex.
	 */
	public String getTabindex() {
		return tabindex;
	}

	/**
	 * @param tabindex The tabindex to set.
	 */
	public void setTabindex(String tabindex) {
		this.tabindex = tabindex;
	}
	
	//------------------------ Digester helper methods.
    
    /**
     * @deprecated Using BooleanConverter
     * @param startsubmenu The startsubmenu to set.
     */
    public void setStartsubmenu(String startsubmenu) {
        this.startsubmenu = StringUtil.booleanValue(startsubmenu);
    }
    
	/**
	 * @param align the align to set
	 */
	public void setAlign(String align) {
		this.enmAlign = zXType.pageflowElementAlign.getEnum(align);
	}
	
    //------------------------ Override Objects methods
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFRef objRef =  new PFRef();
        
        objRef.setName(getName());
        objRef.setStartsubmenu(isStartsubmenu());
        objRef.setImg(getImg());
        objRef.setImgover(getImgover());
        
        if (getUrl() != null) {
        	objRef.setUrl((PFUrl)getUrl().clone());
        }
        
        if (getLabel() != null) {
        	objRef.setLabel((LabelCollection)getLabel().clone());
        }
        
        if (getConfirm() != null) {
        	objRef.setConfirm((LabelCollection)getConfirm().clone());
        }
        
    	objRef.setRefType(getRefType());
    	
    	if (getDescription() != null) {
    		objRef.setDescription((LabelCollection)getDescription().clone());
    	}
        
    	if (getAccesskey() != null) {
    		objRef.setAccesskey((LabelCollection)getAccesskey().clone());
    	}
    	
    	if (getEnmAlign() != null) {
    		objRef.setEnmAlign(getEnmAlign());
    	}
    	
    	objRef.setTabindex(getTabindex());
    	
        return objRef;
    }
    
	/**
	 * @see java.lang.Object#toString()
	 */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
	
		toString.append("name", getName());
		
		if (getUrl() != null) {
			toString.append("url", getUrl().getUrl());  
		}
	 
		return toString.toString();
	}
}