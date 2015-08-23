/*
 * Created on Aug 24, 2004
 * $Id: EventAction.java,v 1.1.2.8 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.util.ToStringBuilder;

/**
 * The class that represents an event action.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 15 May 2004
 * 
 * Change    : BD13JUN04
 * Why       : Added action
 * 
 * Change    : BD8JUL05 - V1.5:29
 * Why       : Added ensureLoad to action action
 * 
 * Change    : BD28APR2006 (BD26MAR06) - V1.5:95
 * Why       : Added extendGroup option
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class EventAction {
    
    //------------------------ Members 
    
    /** The name of the event action. **/
    private String name;
    /** The description for the event action. **/
    private String comment;
    private zXType.eaTiming timing;
    private String events;
    private String activeGroup;
    private zXType.eaGroupBehaviour groupBehaviour;
    private String activeId;
    private zXType.eaIDBehaviour IDBehaviour;
    private String active;
    private LabelCollection msg; // or ZXCollection
    private zXType.rc returnCode;
    private List attrValues;
    private String notNullGroup;
    private String whereGroup;
    private String focusAttribute;
    private zXType.eaContinuation continuation;
    private boolean BOValidation;
    private String action;
    /** Group for zXMe that will be loaded when the event action fires. */
    private String ensureLoaded;
    private String extendgroup;
    
    //------------------------ Constuctor
    
    /**
     * Default constructor.
     */
    public EventAction() {
        super();
    }
    
    //------------------------ Getters and Setters
    
    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }
    
    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
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
     * Group for zXMe that will be loaded when the event action fires.
     * 
	 * @return Returns the ensureLoaded.
	 */
	public String getEnsureLoaded() {
		return ensureLoaded;
	}

	/**
	 * @param ensureLoaded The ensureLoaded to set.
	 */
	public void setEnsureLoaded(String ensureLoaded) {
		this.ensureLoaded = ensureLoaded;
	}

	/**
     * @return Returns the activeGroup.
     */
    public String getActiveGroup() {
        return activeGroup;
    }
    /**
     * @param activeGroup The activeGroup to set.
     */
    public void setActiveGroup(String activeGroup) {
        this.activeGroup = activeGroup;
    }
    
    /**
     * @return Returns the activeId.
     */
    public String getActiveId() {
        return activeId;
    }
    
    /**
     * @param activeId The activeId to set.
     */
    public void setActiveId(String activeId) {
        this.activeId = activeId;
    }
    
    /**
     * @return Returns the attrValues.
     */
    public List getAttrValues() {
        if (attrValues == null) {
            return new ArrayList();
        }
        return attrValues;
    }
    
    /**
     * @param attrValues The attrValues to set.
     */
    public void setAttrValues(List attrValues) {
        this.attrValues = attrValues;
    }
    
    /**
     * @return Returns the bOValidation.
     */
    public boolean isBOValidation() {
        return BOValidation;
    }
    
    /**
     * @param validation The bOValidation to set.
     */
    public void setBOValidation(boolean validation) {
        BOValidation = validation;
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
     * @return Returns the continuation.
     */
    public zXType.eaContinuation getContinuation() {
        return continuation;
    }
    
    /**
     * @param continuation The continuation to set.
     */
    public void setContinuation(zXType.eaContinuation continuation) {
        this.continuation = continuation;
    }
    
    /**
     * @return Returns the events.
     */
    public String getEvents() {
        return events;
    }
    
    /**
     * @param events The events to set.
     */
    public void setEvents(String events) {
        this.events = events;
    }
    
    /**
     * @return Returns the focusAttribute.
     */
    public String getFocusAttribute() {
        return focusAttribute;
    }
    
    /**
     * @param focusAttribute The focusAttribute to set.
     */
    public void setFocusAttribute(String focusAttribute) {
        this.focusAttribute = focusAttribute;
    }
    
    /**
     * @return Returns the groupBehaviour.
     */
    public zXType.eaGroupBehaviour getGroupBehaviour() {
        return groupBehaviour;
    }
    
    /**
     * @param groupBehaviour The groupBehaviour to set.
     */
    public void setGroupBehaviour(zXType.eaGroupBehaviour groupBehaviour) {
        this.groupBehaviour = groupBehaviour;
    }
    
    /**
     * @return Returns the iDBehaviour.
     */
    public zXType.eaIDBehaviour getIDBehaviour() {
        return IDBehaviour;
    }
    
    /**
     * @param behaviour The iDBehaviour to set.
     */
    public void setIDBehaviour(zXType.eaIDBehaviour behaviour) {
        IDBehaviour = behaviour;
    }
    
    /**
     * @return Returns the msg.
     */
    public LabelCollection getMsg() {
        return msg;
    }
    
    /**
     * @param msg The msg to set.
     */
    public void setMsg(LabelCollection msg) {
        this.msg = msg;
    }
    
    /**
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
     * @return Returns the notNullGroup.
     */
    public String getNotNullGroup() {
        return notNullGroup;
    }
    
    /**
     * @param notNullGroup The notNullGroup to set.
     */
    public void setNotNullGroup(String notNullGroup) {
        this.notNullGroup = notNullGroup;
    }
    
    /**
     * @return Returns the returnCode.
     */
    public zXType.rc getReturnCode() {
        return returnCode;
    }
    
    /**
     * @param returnCode The returnCode to set.
     */
    public void setReturnCode(zXType.rc returnCode) {
        this.returnCode = returnCode;
    }
    
    /**
     * @return Returns the timing.
     */
    public zXType.eaTiming getTiming() {
        return timing;
    }
    
    /**
     * @param timing The timing to set.
     */
    public void setTiming(zXType.eaTiming timing) {
        this.timing = timing;
    }
    
    /**
     * @return Returns the whereGroup.
     */
    public String getWhereGroup() {
        return whereGroup;
    }
    
    /**
     * @param whereGroup The whereGroup to set.
     */
    public void setWhereGroup(String whereGroup) {
        this.whereGroup = whereGroup;
    }
    
    /**
	 * @return the extendgroup
	 */
	public String getExtendgroup() {
		return extendgroup;
	}

	/**
	 * @param extendgroup the extendgroup to set
	 */
	public void setExtendgroup(String extendgroup) {
		this.extendgroup = extendgroup;
	}
	
    //------------------------ Helper methods
    
    /**
     * @return Returns the continuation.
     */
    public String getContinuationAsString() {
        return zXType.valueOf(getContinuation());
    }
    
    /**
     * @return Returns the bOValidation as a String.
     */
    public String getBOValidationAsString() {
        return String.valueOf(BOValidation);
    }
    
    /**
     * @return Returns the iDBehaviour.
     */
    public String getIDBehaviourAsString() {
        return zXType.valueOf(getIDBehaviour());
    }
    
    /**
     * @return Returns the groupBehaviour.
     */
    public String getGroupBehaviourAsString() {
        return zXType.valueOf(getGroupBehaviour());
    }
    
    /**
     * @return Returns the returnCode.
     */
    public String getReturnCodeAsString() {
        return zXType.valueOf(getReturnCode());
    }
    
	/**
     * @return Returns the timing.
     */
    public String getTimingAsString() {
        return zXType.valueOf(getTiming());
    }
    
    //------------------------  Overloading objects methods

    /**
     * @see java.lang.Object#toString()
     **/
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
		toString.append("name", getName());
		toString.append("comment", getComment());
		toString.append("timing", getTimingAsString());
		toString.append("events", getEvents());
		toString.append("activeGroup", getActiveGroup());
		toString.append("groupBehaviour", getGroupBehaviourAsString());
		toString.append("activeId", getActiveId());
		toString.append("IDBehaviour", getIDBehaviourAsString());
		toString.append("active", getActive());
		
		if (getMsg() != null) {
			toString.append("msg", getMsg().getLabel());
		}
		
		toString.append("returnCode", getReturnCodeAsString());
		
		if (getAttrValues() != null) {
			toString.append("attrValues", getAttrValues());
		}
		
		toString.append("notNullGroup", getNotNullGroup());
		toString.append("whereGroup", getWhereGroup());
		toString.append("focusAttribute", getFocusAttribute());
		toString.append("continuation", getContinuationAsString());
		toString.append("BOValidation", getBOValidationAsString());
		toString.append("action", getAction());
		
        return toString.toString();
    }
}