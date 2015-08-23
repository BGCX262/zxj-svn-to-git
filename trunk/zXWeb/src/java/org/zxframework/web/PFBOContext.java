/*
 * Created on Apr 21, 2005
 * $Id: PFBOContext.java,v 1.1.2.10 2006/07/17 16:29:24 mike Exp $
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;

/**
 * Definition of a BO context preparation for a page-flow action.
 * 
 * <pre>
 * 
 * Introduced with V1.5:3
 * 
 * Change    : BD30MAR05  V1.5:27
 * Why       : Added support for bo context entries that simply create
 *             a BO instance without trying to load it
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 * @since J1.5
 */
public class PFBOContext extends ZXObject {
    
    //---------------------- Members
    
    private List attrvalues;
    private String comment;
    private zXType.pageflowBOContextIdentificationMethod identificationMethod;
    private String name;
    private String entity;
    private String alias;
    private String active;
    private String identification;
    private String loadgroup;
    private boolean resolveFK;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFBOContext() {
        super();
        
        // Init attrvalues
        setAttrvalues(new ArrayList());
    }
    
    //------------------------ Getters/Settters
    
    /**
     * Director indicating whether to load this BO yes or no.
     * 
     * @return Returns the active.
     */
    public String getActive() {
        return active;
    }
    
    /**
     * @param active The active to set.
     */
    public void setActive(String active) {
        this.active = active;
    }
    
    /**
     * Alias to assign to entity.
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
     * A collection (ArrayList)(PFDirector) of attribute values.
     * 
     * @return Returns the attrvalues.
     */
    public List getAttrvalues() {
        return attrvalues;
    }
    
    /**
     * @param attrValues The attrvalues to set.
     */
    public void setAttrvalues(List attrValues) {
        this.attrvalues = attrValues;
    }
    
    /**
     * @return Returns the comment.
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * True entity-name.
     * 
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
     * Depending on the identification method:
     *  - PK
     *  - Where group
     *  - Context name
     *  
     * @return Returns the identification.
     */
    public String getIdentification() {
        return identification;
    }
    
    /**
     * @param identification The identification to set.
     */
    public void setIdentification(String identification) {
        this.identification = identification;
    }
    
    /**
     * Way we identify the BO that we want to load.
     * 
     * @return Returns the identificationMethod.
     */
    public zXType.pageflowBOContextIdentificationMethod getIdentificationMethod() {
        return identificationMethod;
    }
    
    /**
     * @param identificationMethod The identificationMethod to set.
     */
    public void setIdentificationMethod(zXType.pageflowBOContextIdentificationMethod identificationMethod) {
        this.identificationMethod = identificationMethod;
    }
    
    /**
     * @return Returns the loadgroup.
     */
    public String getLoadgroup() {
        return loadgroup;
    }
    
    /**
     * @param loadGroup The loadgroup to set.
     */
    public void setLoadgroup(String loadGroup) {
        this.loadgroup = loadGroup;
    }
    
    /**
     * Logical name of entity (as stored in context).
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
        
        // The name of the bo context happens to be the key in the bo context
    	setKey(name);
    }
    
    /**
     * @return Returns the resolveFK.
     */
    public boolean isResolveFK() {
        return resolveFK;
    }
    
    /**
     * @param resolveFK The resolveFK to set.
     */
    public void setResolveFK(boolean resolveFK) {
        this.resolveFK = resolveFK;
    }

    //----------------------------- Digester Util methods.
    
    /**
     * @param resolvefk The resolvefk to set.
     */
    public void setResolvefk(String resolvefk) {
        this.resolveFK = StringUtil.booleanValue(resolvefk);
    }
    
    /**
     * @param identificationmethod The identificationMethod to set.
     */
    public void setIdentificationmethod(String identificationmethod) {
        this.identificationMethod = zXType.pageflowBOContextIdentificationMethod.getEnum(identificationmethod);
        if (this.identificationMethod == null) {
            this.identificationMethod = zXType.pageflowBOContextIdentificationMethod.pbocimByPK;
        }
    }
    
    //----------------------------- Public methods.
    
    /**
     * Process me.
     * 
     * @param pobjPageflow A handle to the current pageflow.
     * @return Returns the success code of proccess
     * @throws ZXException Thrown if process fails.
     */
    public zXType.rc process(Pageflow pobjPageflow) throws ZXException {
        zXType.rc process = zXType.rc.rcOK;
        
        ZXBO objBO = null;                      // Handle to BO we have created and will store in context
        String strAlias = "";                   // Resolved alias
        String strEntity = "";                  // Resolved entity
        String strIdentification = "";          // Resolved identification
        ZXBO objOtherBO = null;                 // BO retrieved from BO context
        String strLoadGroup = "";               // Resolved load group
        
        /**
         * Am I active?
         */
        if (!pobjPageflow.isActive(getActive())) return process;
        
        /**
         * Resolve stuff that can be directors
         */
        strAlias = pobjPageflow.resolveDirector(getAlias());
        strEntity = pobjPageflow.resolveDirector(getEntity());
        strIdentification = pobjPageflow.resolveDirector(getIdentification());
        strLoadGroup = pobjPageflow.resolveDirector(getLoadgroup());
        
        /**
         * Create instance of entity, use private descriptor when we have an alias
         */
        if (StringUtil.len(strAlias) == 0) {
            objBO = getZx().createBO(strEntity);
            
        } else {
            objBO = getZx().createBO(strEntity, true);
            
        } // Has alias
        
        if (objBO == null) {
        	getZx().trace.addError("Unable to create instance of BO context entity", strEntity);
            process = zXType.rc.rcError;
            return process;
            
        }
        
        /**
         * And set alias
         */
        if (StringUtil.len(strAlias) > 0) objBO.getDescriptor().setAlias(strAlias);
        
        /**
         * Handle attributevalues
         */
        pobjPageflow.processAttributeValuesGeneric(objBO, getAttrvalues());
        
        /**
         * Now it is time to load the BO
         */
        int intIdentificationMethod = this.identificationMethod.pos;
        
        if (intIdentificationMethod == zXType.pageflowBOContextIdentificationMethod.pbocimByContextName.pos) {
            /**
             * Basically there is supposed to be a BO in the BO context that we
             * want to copy and store under new name
             */
            objOtherBO = getZx().getBOContext().getBO(strIdentification);
            
            if (objOtherBO == getZx().getBOContext().getBO(strIdentification)) {
                getZx().trace.addError("Unable to find BO in context", strIdentification);
                process = zXType.rc.rcError;
                return process;
            }
            
            objOtherBO.bo2bo(objBO, strLoadGroup);
            
        } else if (intIdentificationMethod == zXType.pageflowBOContextIdentificationMethod.pbocimByWhereGroup.pos) {
            /**
             * Load by where-group
             */
            if (objBO.loadBO(strLoadGroup, strIdentification, isResolveFK()).pos != zXType.rc.rcOK.pos) {
            	getZx().trace.addError("Unable to load BO by wheregroup", strIdentification);
                process = zXType.rc.rcError;
                return process;
            }
            
        } else if (intIdentificationMethod == zXType.pageflowBOContextIdentificationMethod.pbocimCreateOnly.pos) {
        	/**
        	 * Simply create but do not create
        	 */
        	
        } else {
            /**
             * Load by PK
             */
        	objBO.setPKValue(strIdentification);
        	
            if (objBO.loadBO(strLoadGroup, "+", isResolveFK()).pos == zXType.rc.rcError.pos) {
            	getZx().trace.addError("Unable to load BO by wheregroup.", strIdentification);
                process = zXType.rc.rcError;
                return process;
            }
            
        } // Identification method
        
        /**
         * And store in BO context using the name of the bo instance as the key.
         */
        getZx().getBOContext().setEntry(getName(), objBO);
        
        return process;
    }
    
    //----------------------- Object overridden methods
    
    /**
     * Performs a deep clone on this object.
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFBOContext clone = new PFBOContext();
        
        clone.setActive(getActive());
        clone.setAlias(getAlias());
        
        if (getAttrvalues() != null && getAttrvalues().size() > 0) {
            clone.setAttrvalues(CloneUtil.clone((ArrayList)getAttrvalues()));
        }
        
        clone.setComment(getComment());
        clone.setEntity(getEntity());
        clone.setIdentification(getIdentification());
        clone.setIdentificationMethod(getIdentificationMethod());
        clone.setLoadgroup(getLoadgroup());
        clone.setName(getName());
        clone.setResolveFK(isResolveFK());
        
        return clone;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
        
        toString.append("name", getName());
        toString.append("entity", getEntity());
        toString.append("active", getActive());
        toString.append("identificationMethod", zXType.valueOf(getIdentificationMethod()));
        toString.append("identification", getIdentification());
        toString.append("loadgroup", getLoadgroup());
        toString.append("resolveFK", isResolveFK());
        toString.append("alias", getAlias());
        toString.append("comment", getComment());
        toString.append("attrvalues", getAttrvalues());
        
        return toString.toString();
    }
}