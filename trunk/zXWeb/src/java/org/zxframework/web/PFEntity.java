/*
 * Created on Mar 16, 2004 by Michael Brewer
 * $Id: PFEntity.java,v 1.1.2.12 2006/07/17 16:29:24 mike Exp $
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.Descriptor;
import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXObject;
import org.zxframework.datasources.DSHandler;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;

/**
 * Pageflow entity object.
 * 
 * <pre>
 *  WARNING : Using this class is a but combersome and may be heavily refactored.
 *  
 *   
 * Change    : DGS13JUN2003
 * Why       : Added Comment
 * 
 * Change    : DGS19SEP2003
 * Why       : Added collection of grid bus objects (for grid edit form)
 * 
 * Change    : DGS02FEB2004
 * Why       : New function to resolve attr groups. This preserves 'original' values so
 *             that the resolve can be repeated using the same entity.
 *             While doing this created new public properties for the 'orig' groups, but
 *             these aren't used as yet. If breaking compatibility in future might
 *             consider removing these.
 * 
 * Change    : DGS20FEB2004
 * Why       : Bug fix to above - use pageflow resolveDirector not zX.
 * 
 * Change    : BD2MAR04
 * Why       : Added generatedFields property
 * 
 * Change    : BD30MAR05  V1.5:1
 * Why       : Added support for data sources
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFEntity extends ZXObject {

    //------------------------ Members
    
    private String name;
    private String entity;
    private String alias;
    private String pk;
    
    private boolean resolveFK;
    private boolean allowNew;
    private String refboaction;
    
    // Can be used to store the resolved values
    private String selectlistgroup;
    private String selecteditgroup;
    private String wheregroup;
    private String lockgroup;
    private String listgroup;
    private String visiblegroup;
    private String copygroup;
    private String pkwheregroup;
    private String groupbygroup;
    
    // Used to ensure safe cloning. This will always store the original value.
    private String selectlistgroupOrig;
    private String selecteditgroupOrig;
    private String wheregroupOrig;
    private String lockgroupOrig;
    private String listgroupOrig;
    private String visiblegroupOrig;
    private String copygroupOrig;
    private String pkwheregroupOrig;
    private String groupbygroupOrig;

    /**
     * DGS14APR2003 Added entity massagers
     */
    private List entitymassagers;
    
    /**
     * DGS13JUN2003 Added Comment
     */
    private String comment;
    
    //------------------------ Runetime members
    
    /** Runtime values **/
    private ZXBO bo;
    private ZXBO bosaver;
    private Descriptor BODesc;
    /**
     * BD30MAR05 Added V1.5:1
     */
    private DSHandler DSHandler;
    
    /**
     * BD12NOV02 Use pre-allocation for ease of use
     */
    private List attrvalues;
    
    /**
     * BD13NOV02 Added this to avoid duplicate loading
     */
    private boolean loaded;
    
    /**
     * DGS17SEP2003 Added collection of grid objects (for grid edit form)
     */
    private ZXCollection BOSavers;
    
    private String generatedFields;
    
    //------------------------ Constructor

    /**
     * Default constructor.
     */
    public PFEntity() {
        super();
    }

    //------------------------ Getters and Setters.
    
    /**
     * Called "Alias" in repository editor.
     * 
     * NOTE : Have not been used.
     * NOTE : Can been an expression.
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
     * Called "Allow New" in repository editor.
     * 
     * NOTE : Have not been used.
     * 
     * @return Returns the allownew.
     */
    public boolean isAllowNew() {
        return allowNew;
    }
    
    /**
     * @param allownew The allownew to set.
     */
    public void setAllowNew(boolean allownew) {
        this.allowNew = allownew;
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
     * @param attrvalues The attrvalues to set.
     */
    public void setAttrvalues(List attrvalues) {
        this.attrvalues = attrvalues;
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
     * NOTE : Have not been used.
     * 
     * @return Returns the copygroup.
     */
    public String getCopygroup() {
        return copygroup;
    }
    
    /**
     * The full name of an entity.
     * 
     * Called "Entity" in repository editor.
     * 
     * NOTE : Can be an expression.
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
     * A collection (ArrayList)(PFEntityMassager) of entity massagers.
     * 
     * @return Returns the entitymassagers.
     */
    public List getEntitymassagers() {
        return entitymassagers;
    }
    
    /**
     * @param entitymassagers The entitymassagers to set.
     */
    public void setEntitymassagers(List entitymassagers) {
        this.entitymassagers = entitymassagers;
    }
    
    /**
     * The order by attribute group.
     * 
     * Called "Order By" in repository editor.
     * 
     * NOTE : Can be an expression.
     * 
     * @return Returns the groupbygroup.
     */
    public String getGroupbygroup() {
        return groupbygroup;
    }
    
    /**
     * @return Returns the listgroup.
     */
    public String getListgroup() {
        return listgroup;
    }
    
    /**
     * The lock attribute group used in edit forms.
     * 
     * These fields are locked but the values are still submitted as hidden values.
     * 
     * NOTE : Can be an expression.
     * 
     * @return Returns the lockgroup.
     */
    public String getLockgroup() {
        return lockgroup;
    }
    
    /**
     * The entity identifier in the pageflow.
     * 
     * Called "Name" in repository editor.
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
        setKey(name);
    }
    
    /**
     * The primary value.
     * 
     * NOTE : This can be an expression.
     * 
     * @return Returns the pk.
     */
    public String getPk() {
        return pk;
    }
    
    /**
     * @param pk The pk to set.
     */
    public void setPk(String pk) {
        this.pk = pk;
    }
    
    /**
     * The where groups used in queries and loading entities.
     * 
     * Called "Where" in repository editor.
     * 
     * @return Returns the pkwheregroup.
     */
    public String getPkwheregroup() {
        return pkwheregroup;
    }

    /**
     * Added refBoAction. This allows 'local' entity definitions whilst
     * sharing a business object + descriptor of another action / entity
     * 
     * Called "Ref BO" in repositor editor.
     * 
     * NOTE : This can be an expression.
     * 
	 * @return Returns the refboaction.
     */
    public String getRefboaction() {
        return refboaction;
    }
    
    /**
     * @param refboaction The refboaction to set.
     */
    public void setRefboaction(String refboaction) {
        this.refboaction = refboaction;
    }
    
    /**
     * Whether to resolve the values for attributes with foriegn keys.
     * 
     * Called "Resolve Foreign Key" in repository editor.
     * 
     * NOTE : This has not been used that often.
     * 
     * @return Returns the resolvefk.
     */
    public boolean isResolveFK() {
        return resolveFK;
    }
    
    /**
     * @param resolvefk The resolvefk to set.
     */
    public void setResolveFK(boolean resolvefk) {
        this.resolveFK = resolvefk;
    }
    
    /**
     * The attribute used for edit forms like EditForm/GroupEditForm and MatrixEdit form and also used in corresponding
     * createupdate actions.
     * 
     * Called "Select Edit" in repository editor.
     * 
     * @return Returns the selecteditgroup.
     */
    public String getSelecteditgroup() {
        return selecteditgroup;
    }
    
    /**
     * The attribute used in queries for list type forms (PFListForm/PFTreeForm/PFCalendar etc..), this should include the primary key and any other
     * fields referenced and contain all of the attribute of the list group.
     * 
     * <pre>
     * 
     * Called "Select List" in repository editor.
     * </pre>
     * 
     * @return Returns the selectlistgroup.
     */
    public String getSelectlistgroup() {
        return selectlistgroup;
    }

    /**
     * This is the group of attributes you want to be visible in edit forms. If nothing is specified all
     * attributes are shown.
     * 
     * <pre>
     * 
     * Called "Visible" in repository editor.
     * </pre>
     * 
     * @return Returns the visiblegroup.
     */
    public String getVisiblegroup() {
        return visiblegroup;
    }

    /**
     * The search group used by search form.
     * 
     * <pre>
     * 
     * So for a search form for example. The attribute used here is what 
     * will be visible in a search form.
     * 
     * Called "Search" in repository editor.
     * </pre>
     * 
     * @return Returns the wheregroup.
     */
    public String getWheregroup() {
        return wheregroup;
    }
    
    //------------------------ Resloved fields
    
    /**
     * This property is used to keep track of what
     * fields are generated in a multi-tab / column edit page.
     * 
     * @return Returns the generatedFields.
     */
    public String getGeneratedFields() {
        return generatedFields;
    }
    
    /**
     * @param generatedFields The generatedFields to set.
     */
    public void setGeneratedFields(String generatedFields) {
        this.generatedFields = generatedFields;
    }
    
    /**
     * @return Returns the loaded.
     */
    public boolean hasBeenLoaded() {
        return loaded;
    }
    
    /**
     * @return Returns the loaded.
     */
    public boolean isLoaded() {
        return loaded;
    }
    
    /**
     * @param loaded The loaded to set.
     */
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
    
    /**
     * @return Returns the bODesc.
     */
    public Descriptor getBODesc() {
        return BODesc;
    }
    
    /**
     * @param desc The bODesc to set.
     */
    public void setBODesc(Descriptor desc) {
        BODesc = desc;
    }
    
    /**
     * @return Returns the bOSavers.
     */
    public ZXCollection getBOSavers() {
        
        if (this.BOSavers == null) {
            this.BOSavers = new ZXCollection();
        }
        
        return BOSavers;
    }
    
    /**
     * @param savers The bOSavers to set.
     */
    public void setBOSavers(ZXCollection savers) {
        BOSavers = savers;
    }
    
    /**
     * The Business object this PFEntity is linked to.
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
     * @return Returns the bosaver.
     */
    public ZXBO getBosaver() {
        return bosaver;
    }
    
    /**
     * @param bosaver The bosaver to set.
     */
    public void setBosaver(ZXBO bosaver) {
        this.bosaver = bosaver;
    }
    
    /**
     * @return Returns the dSHandler.
     */
    public DSHandler getDSHandler() {
        return DSHandler;
    }
    
    /**
     * @param handler The dSHandler to set.
     */
    public void setDSHandler(DSHandler handler) {
        DSHandler = handler;
    }
    
    //------------------------ Digester helper methods.
    
    /**
     * @param allownew The allownew to set.
     */
    public void setAllownew(String allownew) {
        this.allowNew = StringUtil.booleanValue(allownew);
    }
    
    /**
     * @param resolvefk The resolvefk to set.
     */
    public void setResolvefk(String resolvefk) {
        this.resolveFK = StringUtil.booleanValue(resolvefk);
    }
    
    //------------------------ Special Setters calls during parsing and cloning ONLY!
    
    /**
     * Sets copygroup AND copygroupOrig.
     * 
     * @param copygroup The copygroup to set.
     */
    public void setCopygroup(String copygroup) {
        this.copygroupOrig = copygroup;
        this.copygroup = copygroup;
    }
    
    /**
     * Sets groupbygroup AND groupbygroupOrig.
     * 
     * @param groupbygroup The groupbygroup to set.
     */
    public void setGroupbygroup(String groupbygroup) {
        this.groupbygroupOrig = groupbygroup;
        this.groupbygroup = groupbygroup;
    }
    
    /**
     * Sets listgroup AND listgroupOrig.
     * 
     * @param listgroup The listgroup to set.
     */
    public void setListgroup(String listgroup) {
        this.listgroupOrig = listgroup;
        this.listgroup = listgroup;
    }
    
    /**
     * Sets lockgroup AND lockgroupOrig.
     * 
     * @param lockgroup The lockgroup to set.
     */
    public void setLockgroup(String lockgroup) {
        this.lockgroupOrig = lockgroup;
        this.lockgroup = lockgroup;
    }
    
    /**
     * Sets pkwheregroup AND pkwheregroupOrig.
     * 
     * @param pkwheregroup The pkwheregroup to set.
     */
    public void setPkwheregroup(String pkwheregroup) {
        this.pkwheregroupOrig = pkwheregroup;
        this.pkwheregroup = pkwheregroup;
    }
    
    /**
     * Sets selecteditgroup AND selecteditgroupOrig.
     * 
     * @param selecteditgroup The selecteditgroup to set.
     */
    public void setSelecteditgroup(String selecteditgroup) {
        this.selecteditgroupOrig = selecteditgroup;
        this.selecteditgroup = selecteditgroup;
    }
    
    /**
     * Sets selectlistgroup AND selectlistgroupOrig.
     * 
     * @param selectlistgroup The selectlistgroup to set.
     */
    public void setSelectlistgroup(String selectlistgroup) {
        this.selectlistgroupOrig = selectlistgroup;
        this.selectlistgroup = selectlistgroup;
    }
    
    /**
     * Sets visiblegroup AND visiblegroupOrig.
     * 
     * @param visiblegroup The visiblegroup to set.
     */
    public void setVisiblegroup(String visiblegroup) {
        this.visiblegroupOrig = visiblegroup;
        this.visiblegroup = visiblegroup;
    }
    
    /**
     * Sets wheregroup AND wheregroupOrig.
     * 
     * @param wheregroup The wheregroup to set.
     */
    public void setWheregroup(String wheregroup) {
        this.wheregroupOrig = wheregroup;
        this.wheregroup = wheregroup;
    }
    
    //------------------------ Set Resolved the runtime values.
    
    /**
     * @param copygroupResolved The resolved copygroup.
     */
    public void resolveCopygroup(String copygroupResolved) {
        this.copygroup = copygroupResolved;
    }
    
    /**
     * @param groupbygroupResolved The resolved groupbygroup.
     */
    public void resolveGroupbygroup(String groupbygroupResolved) {
        this.groupbygroup = groupbygroupResolved;
    }
    
    /**
     * @param listgroupResolved The resolved listgroup.
     */
    public void resolveListgroup(String listgroupResolved) {
        this.listgroup = listgroupResolved;
    }
    
    /**
     * @param lockgroupResolved The resolved lockgroup.
     */
    public void resolveLockgroup(String lockgroupResolved) {
        this.lockgroup = lockgroupResolved;
    }
    
    /**
     * @param pkwheregroupResolved The resolved pkwheregroup.
     */
    public void resolvePkwheregroup(String pkwheregroupResolved) {
        this.pkwheregroup = pkwheregroupResolved;
    }
    
    /**
     * @param selecteditgroupResolved The resolved selecteditgroup.
     */
    public void resolveSelecteditgroup(String selecteditgroupResolved) {
        this.selecteditgroup = selecteditgroupResolved;
    }
    
    /**
     * @param selectlistgroupResolved The resolved selectlistgroup.
     */
    public void resolveSelectlistgroup(String selectlistgroupResolved) {
        this.selectlistgroup = selectlistgroupResolved;
    }
    
    /**
     * @param visiblegroupResolved The resolved visiblegroup.
     */
    public void resolveVisiblegroup(String visiblegroupResolved) {
        this.visiblegroup = visiblegroupResolved;
    }
    
    /**
     * @param wheregroupResolved The resolved wheregroup.
     */
    public void resolveWheregroup(String wheregroupResolved) {
        this.wheregroup = wheregroupResolved;
    }

    //------------------------ Readonly values.
    
    /**
     * @return Returns the copygroupOrig.
     */
    public String getCopygroupOrig() {
        return copygroupOrig;
    }
    
    /**
     * @return Returns the groupbygroupOrig.
     */
    public String getGroupbygroupOrig() {
        return groupbygroupOrig;
    }
    
    /**
     * @return Returns the listgroupOrig.
     */
    public String getListgroupOrig() {
        return listgroupOrig;
    }
    
    /**
     * @return Returns the lockgroupOrig.
     */
    public String getLockgroupOrig() {
        return lockgroupOrig;
    }
    
    /**
     * @return Returns the pkwheregroupOrig.
     */
    public String getPkwheregroupOrig() {
        return pkwheregroupOrig;
    }
    
    /**
     * @return Returns the selecteditgroupOrig.
     */
    public String getSelecteditgroupOrig() {
        return selecteditgroupOrig;
    }
    
    /**
     * @return Returns the selectlistgroupOrig.
     */
    public String getSelectlistgroupOrig() {
        return selectlistgroupOrig;
    }
    
    /**
     * @return Returns the visiblegroupOrig.
     */
    public String getVisiblegroupOrig() {
        return visiblegroupOrig;
    }
    
    /**
     * @return Returns the wheregroupOrig.
     */
    public String getWheregroupOrig() {
        return wheregroupOrig;
    }
    
    //------------------------ Object overidden methods.
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        
        PFEntity objNewEntity = null;
        try {
            
            // Create and initialize a clean PFEntity :
            objNewEntity = new PFEntity();
            
	        objNewEntity.setAlias(getAlias());
	        objNewEntity.setAllowNew(isAllowNew());
	        
	        if (getAttrvalues() != null && getAttrvalues().size() > 0) {
                objNewEntity.setAttrvalues(CloneUtil.clone((ArrayList)getAttrvalues()));
	        }
            
	        objNewEntity.setEntity( getEntity() );
	        
	        if (getEntitymassagers() != null && getEntitymassagers().size() > 0) {
		        objNewEntity.setEntitymassagers(CloneUtil.clone((ArrayList)getEntitymassagers()));
	        }
	        
	        objNewEntity.setPk(getPk());
	        objNewEntity.setRefboaction(getRefboaction());
	        
	        // This peforms the setter for the key aswell.
	        objNewEntity.setName(getName());
	        
	        // Reset to original values before they where resolved. 
	        objNewEntity.setCopygroup(getCopygroupOrig());
	        objNewEntity.setGroupbygroup(getGroupbygroupOrig());
	        objNewEntity.setListgroup(getListgroupOrig());
	        objNewEntity.setLockgroup(getLockgroupOrig());
	        objNewEntity.setPkwheregroup(getPkwheregroupOrig());	        
	        objNewEntity.setSelecteditgroup(getSelecteditgroupOrig());
	        objNewEntity.setSelectlistgroup(getSelectlistgroupOrig());
	        objNewEntity.setVisiblegroup(getVisiblegroupOrig());
	        objNewEntity.setWheregroup(getWheregroupOrig());
	        // Reset to original values before they where resolved.
	        
	        // This entity is not loaded.
	        objNewEntity.setLoaded(false);
            
        } catch (Exception e) {
            getZx().log.error("Failed to clone ",e);
        }
        
        return objNewEntity;
    }
    
    /**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
		toString.append("name", getName());
		toString.append("pk", getPk());
		toString.append("allowNew", isAllowNew());
		toString.append("alias", getAlias());
		toString.append("attrvalues", getAttrvalues());
		toString.append("entity", getEntity());
		toString.append("comment", getComment());

		toString.append("copygroup", getCopygroup());
		toString.append("groupbygroup", getGroupbygroup());
		toString.append("listgroup", getListgroup());
		toString.append("lockgroup", getLockgroup());
		toString.append("pkwheregroup", getPkwheregroup());
		toString.append("refboaction", getRefboaction());
		toString.append("selecteditgroup", getSelecteditgroup());
		toString.append("selectlistgroup", getSelectlistgroup());
		toString.append("visiblegroup", getVisiblegroup());
		toString.append("wheregroup", getWheregroup());
		
		toString.append("entitymassagers", getEntitymassagers());
		
		if (getBODesc() != null) {
			toString.append("BODesc", getBODesc().getName());
		}
		if (getDSHandler() != null) {
			toString.append("dstype", getDSHandler().getDsType());
		}
		toString.append("generatedFields", getGeneratedFields());
		
		return toString.toString();
	}
}