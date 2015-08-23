/*
 * Created on Apr 9, 2004 by Michael Brewer
 * $Id: PFAction.java,v 1.1.2.23 2006/07/17 16:07:03 mike Exp $ 
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import org.zxframework.LabelCollection;
import org.zxframework.Tuple;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

/**
 * Pageflow generic action object.
 *  
 * <pre>
 * 
 * This also is the "inteface" iPFAction" from the original framework. Doing it like this makes 
 * parsing infinitely easier and faster ;). Also the handling of PFActions is simpler.
 * 
 * UML Class Diagram :
 * 
 * <img src="../../../doc-files/pfaction.png">
 * 
 * Change    : DGS13FEB2003
 * Why       : Added collection of 'popups' to action.
 * 
 * Change    : 27MARFEB2003
 * Why       : Added iDefaultAction property for use by ASP when an action class's default
 *             interface is required.
 * 
 * Change    : DGS13JUN2003
 * Why       : Added Comment and narrative
 * 
 * Change    : DGS23JUN2003
 * Why       : Added narrative 'is director' property. Also added 'Recursive Tree' to the
 *             lists of action types in 'actionTypeAsString' get/let property.
 * 
 * Change    : DGS19SEP2003
 * Why       : New actions grid editform/createupdate
 * 
 * Change    : DGS26SEP2003
 * Why       : Added support for defining an Alternative Connection. This allows
 *             an application to access more than one database.
 * 
 * Change    : DGS12FEB2004
 * Why       : Minor changes to support the 'Matrix' edit form and createupdate
 * 
 * Change    : BD14FEB04
 * Why       : Added alertMsg feature (similar to infoMsg and errorMsg but different
 *             look & feel
 * 
 * Change    : BD4JUL04
 * Why       : Had to change top / left from integer to long to cater for
 *             complex pageflows
 *               
 * Change    : BD13APR05 - V1.5:3
 * Why       : Added support for BO context
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class PFAction extends ZXObject implements Cloneable {
	
    //------------------------ Constants
	
    protected static final String PFPattern = "pageflow/actions/action";

    //------------------------ Members
    
    //---- Runtime members
    
    private Pageflow pageflow;
    private PFDescriptor descriptor;
    
    //------------------------ Parsing stuff    
    
    private Element XMLNode;
    private boolean parsed;

    //-------------------------------- XML members
    
    private zXType.pageflowActionType actionType;
    private String name;
    private String helpid;
    private String comment;
    private LabelCollection narr;
    private boolean narrisdir;
    private PFComponent link;
    private String entityaction;
    private String left;
    private String top;
    private PFUrl formaction;
    private ZXCollection entities;
    private String stickyqsaction;
    private boolean cached;
    private List stickyqs;
    private ZXCollection entitynames;
    private List contextupdate;
    private ZXCollection security; 
    private ZXCollection tags;
    private LabelCollection title;
    private ArrayList buttons;
    private ArrayList refs;
    private ArrayList actions;
    private ArrayList popups;
    private LabelCollection infomsg;
    private LabelCollection errormsg;
    private LabelCollection alertmsg;
    private zXType.pageflowFrameHandling frameHandling;
    private String alternateconnection;
    /** BD13APR05 - Added **/
    private List BOContexts;
    private boolean limitrows;
    
    //-----------------------------------------------------------
    //------------------------ Fields moved up to the super class
    
    //------------------------ Generic Settings
    /**
     * Move field up into here that are shared by a couple of the subclasses 
     */
    
    private boolean resolvefk;
    private String queryname = "";
    
    //------------------------ Visual Settings
    
    private String clazz = "";
    private List editenhancers;
    
    //------------------------ Listing Settings
    
    private boolean addparitytoclass;
    private int maxrows;
    private String width = "";
    
    /**
     * Moved this up into the super class so that we do not have to have a 
     * GridEditForm point to a GridCreateUpdate.
     */
    private String qsprogress = "";
    private String newrows = "";
    private PFUrl url;
    
    //------------------------ Fields moved up to the super class
    //-----------------------------------------------------------
    
    //------------------------------------------------ XML members
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    protected PFAction() {
        super();
    }
    
    /**
     * Initialise the PFObject. 
     * 
     * @param pobjPageflow A handle to the Pageflow object
     * @param pobjPFDesc A handle to the current Pageflow Descriptor.
     * @return Returns the return code of init.
     */
    public zXType.rc init(Pageflow pobjPageflow, PFDescriptor pobjPFDesc) {
        this.pageflow = pobjPageflow;
        this.descriptor = pobjPFDesc;
        return zXType.rc.rcOK;
    }

    //------------------------ Gettes and Setters

    /**
     * The main pageflow descriptor.
     * 
     * @return Returns the descriptor.
     */
    public PFDescriptor getDescriptor() {
        return descriptor;
    }
    
    /**
     * @param descriptor The descriptor to set.
     */
    public void setDescriptor(PFDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    /**
     * @return Returns the pageflow.
     */
    public Pageflow getPageflow() {
        return pageflow;
    }
    
    /**
     * @param pageflow The pageflow to set.
     */
    public void setPageflow(Pageflow pageflow) {
        this.pageflow = pageflow;
    }
    
    /**
     * A collection (ArrayList)(PFRef) of actions.
     * 
     * @return Returns the actions.
     */
    public ArrayList getActions() {
        if (this.actions == null) {
            this.actions = new ArrayList();
        }
        return actions;
    }
    
    /**
     * @param actions The actions to set.
     */
    public void setActions(ArrayList actions) {
        this.actions = actions;
    }
    
    /**
     * A internationalized alert message to display.
     * 
     * @return Returns the alertmsg.
     */
    public LabelCollection getAlertmsg() {
        if (this.alertmsg == null) {
            this.alertmsg = new LabelCollection();
        }
        return alertmsg;
    }
    
    /**
     * @param alertmsg The alertmsg to set.
     */
    public void setAlertmsg(LabelCollection alertmsg) {
        this.alertmsg = alertmsg;
    }
    
    /**
     * The name of a alternative database datasource.
     * 
     * @return Returns the alternateconnection.
     */
    public String getAlternateconnection() {
        return alternateconnection;
    }
    
    /**
     * @param alternateconnection The alternateconnection to set.
     */
    public void setAlternateconnection(String alternateconnection) {
        this.alternateconnection = alternateconnection;
    }
    
    /**
     * A collection (ArrayList)(PFRef) of form buttons.
     * 
     * @return Returns a ArrayList (org.zxframework.web.PFRef) of buttons.
     */
    public ArrayList getButtons() {
        if (this.buttons == null) {
            this.buttons = new ArrayList();
        }
        return buttons;
    }
    
    /**
     * The number of new rows created in the transaction.
     * 
     * @return Returns the newrows.
     */
    public String getNewrows() {
        return newrows;
    }
    
    /**
     * @param newrows The newrows to set.
     */
    public void setNewrows(String newrows) {
        this.newrows = newrows;
    }
    
    /**
     * @param buttons The buttons to set.
     */
    public void setButtons(ArrayList buttons) {
        this.buttons = buttons;
    }
    
    /**
     * Whether to try and cache this pageflow.
     * 
     * @return Returns the cached.
     */
    public boolean isCached() {
        return cached;
    }
    
    /**
     * @param cached The cached to set.
     */
    public void setCached(boolean cached) {
        this.cached = cached;
    }
    
    /**
     * @return Returns the qsprogress.
     */
    public String getQsprogress() {
        return qsprogress;
    }
    
    /**
     * @param qsprogress The qsprogress to set.
     */
    public void setQsprogress(String qsprogress) {
        this.qsprogress = qsprogress;
    }
    
    /**
     * The developer comment for this pageflow action.
     * 
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
     * A collection (ArrayList)(PFDirector) of PFDirector to update the context.
     * 
     * @return Returns the contextupdate.
     */
    public List getContextupdate() {
        if (this.contextupdate == null) {
            this.contextupdate = new ArrayList();
        }
        return contextupdate;
    }
    
    /**
     * @param contextupdate The contextupdate to set.
     */
    public void setContextupdate(List contextupdate) {
        this.contextupdate = contextupdate;
    }
    
    /**
     * What type of frame handling do we have now.
     * 
     * @return Returns the frameHandling.
     */
    public zXType.pageflowFrameHandling getFrameHandling() {
        if (this.frameHandling == null) {
            this.frameHandling = zXType.pageflowFrameHandling.pfhNone;
        }
        return frameHandling;
    }
    
    /**
     * @param frameHandling The frameHandling to set.
     */
    public void setFrameHandling(zXType.pageflowFrameHandling frameHandling) {
        this.frameHandling = frameHandling;
    }
    
    /**
     * The enum of the type of pageflow action.
     * 
     * @return Returns the actionType.
     */
    public zXType.pageflowActionType getActionType() {
        return actionType;
    }
    
    /**
     * @param actionType The actionType to set.
     */
    public void setActionType(zXType.pageflowActionType actionType) {
        this.actionType = actionType;
    }
    
    /**
     * A collection (ZXCollection)(PFEntity) of PFEntities linked to the pageflow action. (PFEntity).
     * 
     * @return Returns the entities.
     */
    public ZXCollection getEntities() {
        if (this.entities == null) {
            this.entities = new ZXCollection();
        }
        return entities;
    }
    
    /**
     * @param entities The entities to set.
     */
    public void setEntities(ZXCollection entities) {
        this.entities = entities;
    }
    
    /**
     * The name of the entity action for this form.
     * 
     * @return Returns the entityaction.
     */
    public String getEntityaction() {
        return entityaction;
    }
    
    /**
     * @param entityaction The entityaction to set.
     */
    public void setEntityaction(String entityaction) {
        this.entityaction = entityaction;
    }
    
    /**
     * A collection (ZXCollection)(Tuple) of entity names.
     * 
     * @return Returns the entitynames.
     */
    public ZXCollection getEntitynames() {
        return entitynames;
    }
    
    /**
     * @param entitynames The entitynames to set.
     */
    public void setEntitynames(ZXCollection entitynames) {
        this.entitynames = entitynames;
    }
    
    /**
     * A internationalized error type message to display.
     * 
     * @return Returns the errormsg.
     */
    public LabelCollection getErrormsg() {
        return errormsg;
    }
    
    /**
     * @param errormsg The errormsg to set.
     */
    public void setErrormsg(LabelCollection errormsg) {
        this.errormsg = errormsg;
    }
    
    /**
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
     * Limit the rows.
     * 
     * @return Returns the limitrows.
     */
    public boolean isLimitrows() {
        return limitrows;
    }
    
    /**
     * @param limitrows The limitrows to set.
     */
    public void setLimitrows(boolean limitrows) {
        this.limitrows = limitrows;
    }
    
    /**
     * The action of the web  form.
     * 
     * @return Returns the formaction.
     */
    public PFUrl getFormaction() {
        return formaction;
    }
    
    /**
     * @param formaction The formaction to set.
     */
    public void setFormaction(PFUrl formaction) {
        this.formaction = formaction;
    }
    
    /**
     * The id of the associated help file for this pageflow action.
     * 
     * @return Returns the helpid.
     */
    public String getHelpid() {
        return helpid;
    }
    
    /**
     * @param helpid The helpid to set.
     */
    public void setHelpid(String helpid) {
        this.helpid = helpid;
    }
    
    /**
     * A internationalized informational type message to display.
     * 
     * @return Returns the infomsg.
     */
    public LabelCollection getInfomsg() {
        if (this.infomsg == null) {
            this.infomsg = new LabelCollection();
        }
        return infomsg;
    }
    
    /**
     * @param infomsg The infomsg to set.
     */
    public void setInfomsg(LabelCollection infomsg) {
        this.infomsg = infomsg;
    }
    
    /**
     * @return Returns the left.
     */
    public String getLeft() {
        return left;
    }
    
    /**
     * @param left The left to set.
     */
    public void setLeft(String left) {
        this.left = left;
    }
    
    /**
     * @return Returns the link.
     */
    public PFComponent getLink() {
        return link;
    }
    
    /**
     * @param link The link to set.
     */
    public void setLink(PFComponent link) {
        this.link = link;
    }
    
    /**
     * The name of the pageflow action, this should be unique per pageflow.
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
     * The narrative label for this pageflow action.
     * 
     * @return Returns the narr.
     */
    public LabelCollection getNarr() {
        if (this.narr == null) {
            this.narr = new LabelCollection();
        }
        
        return narr;
    }
    
    /**
     * @param narr The narr to set.
     */
    public void setNarr(LabelCollection narr) {
        this.narr = narr;
    }
    
    /**
     * A collection (ArrayList)(PFBOContext) of PF BO Contexts.
     * 
     * @return Returns the bOContexts.
     */
    public List getBOContexts() {
        return BOContexts;
    }
    
    /**
     * @param contexts The bOContexts to set.
     */
    public void setBOContexts(List contexts) {
        BOContexts = contexts;
    }
    
    /**
     * Whether the narrative is a directive.
     * 
     * @return Returns the narrisdir.
     */
    public boolean isNarrisdir() {
        return narrisdir;
    }
    
    /**
     * @param narrisdir The narrisdir to set.
     */
    public void setNarrisdir(boolean narrisdir) {
        this.narrisdir = narrisdir;
    }
    
    /**
     * A collection (ArrayList)(PFRef) of inline popups. Used for menus etc..
     * 
     * @return Returns the popups.
     */
    public ArrayList getPopups() {
        if (this.popups == null){
            this.popups = new ArrayList();
        }
        return popups;
    }
    
    /**
     * @param popups The popups to set.
     */
    public void setPopups(ArrayList popups) {
        this.popups = popups;
    }
    
    /**
     * A collection (ArrayList)(PFRef) of refs.
     * 
     * @return Returns the refs.
     */
    public ArrayList getRefs() {
        if (this.refs == null) {
            this.refs = new ArrayList();
        }
        return refs;
    }
    
    /**
     * @param refs The refs to set.
     */
    public void setRefs(ArrayList refs) {
        this.refs = refs;
    }
    
    /**
     * A collection (ZXCollection)(Tuple) of security settings for this pageflow action.
     * 
     * @return Returns the security collection. (Tuple)
     */
    public ZXCollection getSecurity() {
        if (this.security == null) {
            this.security = new ZXCollection();
        }
        return security;
    }
    
    /**
     * @param security The security to set.
     */
    public void setSecurity(ZXCollection security) {
        this.security = security;
    }
    
    /**
     * A collection (ArrayList)(PFDirector) of PFDirector for the stringqs.
     * 
     * @return Returns the stickyqs.
     */
    public List getStickyqs() {
        if (this.stickyqs == null) {
            this.stickyqs = new ArrayList();
        }
        return stickyqs;
    }
    
    /**
     * @param stickyqs The stickyqs to set.
     */
    public void setStickyqs(List stickyqs) {
        this.stickyqs = stickyqs;
    }
    
    /**
     * @return Returns the stickyqsaction.
     */
    public String getStickyqsaction() {
        return stickyqsaction;
    }
    
    /**
     * @param stickyqsaction The stickyqsaction to set.
     */
    public void setStickyqsaction(String stickyqsaction) {
        this.stickyqsaction = stickyqsaction;
    }
    
    /**
     * A collection (ZXCollection)(Tuple) of keys values we want to override. 
     * 
     * @return Returns the tags.
     */
    public Map getTags() {
        return tags;
    }
    
    /**
     * @param tags The tags to set.
     */
    public void setTags(ZXCollection tags) {
        this.tags = tags;
    }
    
    /**
     * A internationalized title for this pageflow action.
     * 
     * @return Returns the title.
     */
    public LabelCollection getTitle() {
        if (this.title == null) {
            this.title = new LabelCollection();
        }
        return title;
    }
    
    /**
     * @param title The title to set.
     */
    public void setTitle(LabelCollection title) {
        this.title = title;
    }
    
    /**
     * @return Returns the top.
     */
    public String getTop() {
        return top;
    }
    
    /**
     * @param top The top to set.
     */
    public void setTop(String top) {
        this.top = top;
    }
    
    /**
     * @return Returns the xMLNode.
     */
    public Element getXMLNode() {
        return XMLNode;
    }

    /**
     * @param node The xMLNode to set.
     */
    public void setXMLNode(Element node) {
        XMLNode = node;
    }
    
    /**
     * @return Returns the parsed.
     */
    public boolean isParsed() {
        return parsed;
    }
    
    /**
     * @param parsed The parsed to set.
     */
    public void setParsed(boolean parsed) {
        this.parsed = parsed;
    }    
    
    /**
     * Whether to have alternating colours.
     * 
     * @return Returns whether to alternate the colours.
     */
    public boolean isAddparitytoclass() {
        return addparitytoclass;
    }
    
    /**
     * @param addparitytoclass Whether to alternate the colours.
     */
    public void setAddparitytoclass(boolean addparitytoclass) {
        this.addparitytoclass = addparitytoclass;
    }
    
    /**
     * The css class used to override the look and feel on the site.
     * 
     * @return Returns the clazz.
     */
    public String getClazz() {
        return clazz;
    }
    
    /**
     * @param clazz The clazz to set.
     */
    public void setClass(String clazz) {
        this.clazz = clazz;
    }
    
    /**
     * @param clazz The clazz to set.
     */
    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
    
    /** 
     * A collection (ArrayList)(PFEditEnhancer) of visual enhancers.
     * 
     * @return Returns the editenhancers. 
     * */
    public List getEditenhancers() {
        if (this.editenhancers == null) {
            this.editenhancers = new ArrayList();
        }
        return editenhancers;
    }
    
    /**
     * @param editenhancers The editenhancers to set. 
     **/
    public void setEditenhancers(List editenhancers) {
        if (this.editenhancers == null) {
            this.editenhancers = new ArrayList();
        }
        this.editenhancers = editenhancers;
    }
    
    /**
     * Maximun number of rows to return.
     * 
     * @return Returns the maxrows.
     */
    public int getMaxrows() {
        return maxrows;
    }
    
    /**
     * @param maxrows The maxrows to set.
     */
    public void setMaxrows(int maxrows) {
        this.maxrows = maxrows;
    }
    
    /**
     * The width of the lists returned.
     * 
     * @return Returns the width.
     */
    public String getWidth() {
        return width;
    }
    
    /**
     * @param width The width to set.
     */
    public void setWidth(String width) {
        this.width = width;
    }
    
    /**
     * The base url for the url.
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
     * @param queryname The queryname to set.
     */
    public void setQueryname(String queryname) {
        this.queryname = queryname;
    }
    
    /**
     * The name of the query.
     * 
     * @return Returns the queryname. */
    public String getQueryname() {
        return queryname;
    }
    
    //------------------------ Digester helper methods.
    
    /**
     * @deprecated Using BooleanConverter
     * @param limitrows The limitrows to set.
     */
    public void setLimitrows(String limitrows) {
        this.limitrows = StringUtil.booleanValue(limitrows);
    }
    
    /**
     * @deprecated Using BooleanConverter
     * @param cached The cached to set.
     */
    public void setCached(String cached) {
        this.cached = StringUtil.booleanValue(cached);
    }
    
    /**
     * @deprecated Using BooleanConverter
     * @param resolvefk The resolvefk to set.
     */
    public void setResolvefk(String resolvefk) {
        this.resolvefk = StringUtil.booleanValue(resolvefk);
    }
    
    /**
     * @deprecated Using BooleanConverter
     * @param narrisdir The blnnarrisdir to set.
     */
    public void setNarrisdir(String narrisdir) {
        this.narrisdir = StringUtil.booleanValue(narrisdir);
    }
    
    /**
     * @deprecated Using BooleanConverter
     * @param addparitytoclass Whether to alternate the colours.
     */
    public void setAddparitytoclass(String addparitytoclass) {
        this.addparitytoclass = StringUtil.booleanValue(addparitytoclass);
    }
    
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.actionType = zXType.pageflowActionType.getEnum(type);
    }
    
    /**
     * @param framehandling The framehandling to set.
     */
    public void setFramehandling(String framehandling) {
        this.frameHandling = zXType.pageflowFrameHandling.getEnum(framehandling);
    }
    
    //------------------------ Abstract classes :
    
    /**
     * Handle this action
     * 
     * @return Returns whether this action was sucessfull.
     * @throws ZXException Thrown if go fails.
     */
    public abstract zXType.rc go() throws ZXException;
    
    /**
     * Dump this pageflow action to xml for a potential java repository editor.
     * 
     * <pre>
     * NOTE : The callee needs to wrap this with <action> tag.
     * </pre>
     * 
     */
    public void dumpAsXML() {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        Tuple objTuple;
        Iterator iter;
        
        try {
            XMLGen objXMLGen = this.descriptor.getXMLGen();
            
            objXMLGen.taggedValue("helpid", this.helpid);
            objXMLGen.taggedValue("name", this.name, true);
            objXMLGen.taggedCData("comment", this.comment);
            
            this.descriptor.xmlLabel("narr", getNarr());
            
            if (this.narrisdir) {
                objXMLGen.taggedValue("narrisdir", "true");
            }
            
            this.descriptor.xmlLabel("title", getTitle());
            
            objXMLGen.taggedValue("entityaction", this.entityaction);
            objXMLGen.taggedValue("left", String.valueOf(this.left));
            objXMLGen.taggedValue("top", String.valueOf(this.top));
            
            this.descriptor.xmlComponent("link", getLink());
            this.descriptor.xmlUrl("formaction", getFormaction());
            this.descriptor.xmlSecurity("security", getSecurity());
            this.descriptor.xmlLabel("infomsg", getInfomsg());
            this.descriptor.xmlLabel("errormsg", getErrormsg());
            
            /**
             * BD14FEB04 Added
             */
            this.descriptor.xmlLabel("alertmsg", getAlertmsg());
            
            if (getTags() != null && getTags().size() > 0) {
                objXMLGen.openTag("tags");
                
                iter = getTags().values().iterator();
                while (iter.hasNext()) {
                    objTuple = (Tuple)iter.next();
                    
                    /**
                     * DGS28MAR2003: Made the Optional boolean False so that
                     * tags are always written away even when no value is
                     * present.
                     * 
                     * BD6AUG04: Changed from value to cdata
                     */
                    objXMLGen.taggedCData(objTuple.getName(), objTuple.getValue());
                }
                
                objXMLGen.closeTag("tags");
            }
            
            if (this.entitynames != null && this.entitynames.size() > 0) {
                objXMLGen.openTag("entitynames");
                iter = this.entitynames.iterator();
                while (iter.hasNext()) {
                    objTuple = (Tuple)iter.next();
                    
                    objXMLGen.taggedValue("item", objTuple.getValue());
                }
                objXMLGen.closeTag("entitynames");
            }
            
            if (this.entities != null && this.entities.size() > 0) {
                objXMLGen.openTag("entities");
                PFEntity objEntity;
                iter = this.entities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    this.descriptor.xmlEntity(objEntity);
                }
                objXMLGen.closeTag("entities");
            }
            
            if (this.BOContexts != null) {
                int intBOContexts = this.BOContexts.size();
                if (intBOContexts > 0) {
                    objXMLGen.openTag("bocontexts");
                    PFBOContext objBOContext;
                    for (int i = 0; i < intBOContexts; i++) {
                        objBOContext = (PFBOContext)this.BOContexts.get(i);
                        this.descriptor.xmlBoContextEntry(objBOContext);
                    }
                    objXMLGen.closeTag("bocontexts");
                }
            }
            
            this.descriptor.xmlDirectors("contextupdate", getContextupdate());
            
            objXMLGen.taggedValue("stickyqsaction", getStickyqsaction());
            this.descriptor.xmlDirectors("stickyqs", getStickyqs());
            objXMLGen.taggedValue("cached", isCached());
            
            int intRefs;
            PFRef objRef;
            
            if (this.buttons != null && !this.buttons.isEmpty()) {
                objXMLGen.openTag("buttons");
                
                intRefs = this.buttons.size();
                for (int i = 0; i < intRefs; i++) {
                    objRef = (PFRef)this.buttons.get(i);
                    
                    this.descriptor.xmlRef("item", objRef);
                }
                objXMLGen.closeTag("buttons");
            }
            
            if (this.refs != null && !this.refs.isEmpty()) {
                objXMLGen.openTag("refs");

                intRefs = this.refs.size();
                for (int i = 0; i < intRefs; i++) {
                    objRef = (PFRef)this.refs.get(i);
                    
                    this.descriptor.xmlRef("item", objRef);
                }
                objXMLGen.closeTag("refs");
            }
            
            if (this.actions != null && !this.actions.isEmpty()) {
                objXMLGen.openTag("actions");
                
                intRefs = this.actions.size();
                for (int i = 0; i < intRefs; i++) {
                    objRef = (PFRef)this.actions.get(i);
                    
                    this.descriptor.xmlRef("item", objRef);
                }
                objXMLGen.closeTag("actions");
            }
            
            /**
             * DGS13FEB2003: added popups
             */
            if (this.popups != null && !this.popups.isEmpty()) {
                objXMLGen.openTag("popups");

                intRefs = this.popups.size();
                for (int i = 0; i < intRefs; i++) {
                    objRef = (PFRef)this.popups.get(i);
                    
                    this.descriptor.xmlRef("item", objRef);
                }
                objXMLGen.closeTag("popups");
            }
            
            objXMLGen.taggedValue("framehandling", zXType.valueOf(getFrameHandling()));
            
            objXMLGen.taggedValue("alternateconnection", getAlternateconnection());
            
            /**
             * PFAction has stolen some of the subclass members :
             */
            objXMLGen.taggedValue("qsprogress", getQsprogress());
            objXMLGen.taggedValue("resolvefk", isResolvefk());
            objXMLGen.taggedValue("queryname", getQueryname());
            // NOTE : In java getClass is a reserved method so we need to rename <class> to <clazz>
            objXMLGen.taggedValue("clazz", getClazz());
            objXMLGen.taggedValue("addparitytoclass", isAddparitytoclass());
            
            /**
             * Only write away if we have limitrows turned on.
             */
            if (isLimitrows()) {
                objXMLGen.taggedCData("limitrows", String.valueOf(isLimitrows()));
            }
            objXMLGen.taggedValue("maxrows", String.valueOf(getMaxrows()));
            objXMLGen.taggedValue("newrows", String.valueOf(getNewrows()));
            objXMLGen.taggedValue("width", String.valueOf(getWidth()));
            
            this.descriptor.xmlUrl("url", getUrl());
            
            if (this.editenhancers != null && !this.editenhancers.isEmpty()) {
                objXMLGen.openTag("editenhancers");
                
                PFEditEnhancer objEditEnhancer;
                
                iter = this.editenhancers.iterator();
                while (iter.hasNext()) {
                    objEditEnhancer = (PFEditEnhancer)iter.next();
                    
                    objXMLGen.openTag("editenhancer");
                    
                    objXMLGen.taggedValue("entity", objEditEnhancer.getEntity());
                    objXMLGen.taggedValue("attr", objEditEnhancer.getAttr());
                    objXMLGen.taggedValue("labelclass", objEditEnhancer.getLabelclass());
                    objXMLGen.taggedValue("inputClass", objEditEnhancer.getInputClass());
                    objXMLGen.taggedValue("stdbuttonclass", objEditEnhancer.getStdbuttonclass());
                    objXMLGen.taggedValue("spellcheck", objEditEnhancer.isSpellCheck());
                    
                    /**
                     * DGS09APR2003: Added the fklookup boolean to enhancers.
                     */
                    objXMLGen.taggedValue("fklookup", objEditEnhancer.isSpellCheck());
                    /**
                     * DGS20AUG2003: Added the fkadd boolean to enhancers.
                     */
                    objXMLGen.taggedValue("fkadd", objEditEnhancer.isFkAdd());
                    /**
                     * DGS 20OCT2003: Added FK where clause
                     */
                    objXMLGen.taggedValue("fkwhere", objEditEnhancer.getFkwhere());
                    
                    objXMLGen.taggedValue("tabindex", objEditEnhancer.getTabindex());
                    objXMLGen.taggedValue("disabled", objEditEnhancer.getDisabled());
                    objXMLGen.taggedValue("onfocus", objEditEnhancer.getOnfocus());
                    objXMLGen.taggedValue("onblur", objEditEnhancer.getOnblur());
                    objXMLGen.taggedValue("onchange", objEditEnhancer.getOnchange());
                    objXMLGen.taggedValue("onkeydown", objEditEnhancer.getOnkeydown());
                    objXMLGen.taggedValue("onkeypress", objEditEnhancer.getOnkeypress());
                    objXMLGen.taggedValue("onkeyup", objEditEnhancer.getOnkeyup());
                    objXMLGen.taggedValue("onclick", objEditEnhancer.getOnclick());
                    objXMLGen.taggedValue("onmouseover", objEditEnhancer.getOnmouseover());
                    objXMLGen.taggedValue("onmouseout", objEditEnhancer.getOnmouseout());
                    objXMLGen.taggedValue("onmousedown", objEditEnhancer.getOnmousedown());
                    objXMLGen.taggedValue("onmouseup", objEditEnhancer.getOnmouseup());
                    objXMLGen.taggedValue("entitysize", objEditEnhancer.getEntitysize());
                    objXMLGen.taggedValue("postlabel", objEditEnhancer.getPostlabel());
                    objXMLGen.taggedValue("postlabelclass", objEditEnhancer.getPostlabelclass());
                    
                    /**
                     * BD1MAR05 V1.4:54
                     */
                    objXMLGen.taggedCData("multilinerows", objEditEnhancer.getMultilinerows());
                    objXMLGen.taggedCData("multilinecols", objEditEnhancer.getMultilinecols());
                    
                    /**
                     * BFD19JUL04: Added support for merging fields
                     */
                    if (objEditEnhancer.isMergeWithPrevious()) {
                        objXMLGen.taggedValue("mergewithprevious", objEditEnhancer.isMergeWithPrevious());
                    }
                    if (objEditEnhancer.isMergeWithNext()) {
                        objXMLGen.taggedValue("mergewithnext", objEditEnhancer.isMergeWithNext());
                    }
                    if (objEditEnhancer.isNoLabel()) {
                        objXMLGen.taggedValue("nolabel", objEditEnhancer.isNoLabel());
                    }
                    
                    if (objEditEnhancer.getRefs() != null && !objEditEnhancer.getRefs().isEmpty()) {
                        objXMLGen.openTag("refs");
                        
                        intRefs = objEditEnhancer.getRefs().size();
                        for (int i = 0; i < intRefs; i++) {
                            objRef = (PFRef)objEditEnhancer.getRefs().get(i);
                            
                            this.descriptor.xmlRef("item", objRef);
                        }
                        objXMLGen.closeTag("refs");
                    }
                    
                    if (objEditEnhancer.getEditdependencies() != null && !objEditEnhancer.getEditdependencies().isEmpty()) {
                        objXMLGen.openTag("editdependencies");
                        
                        PFEditDependency objEditDependency;
                        
                        Iterator iterEditdependencies = objEditEnhancer.getEditdependencies().iterator();
                        while (iterEditdependencies.hasNext()) {
                            objEditDependency = (PFEditDependency)iter.next();
                            
                            objXMLGen.openTag("editdependency");
                            
                            objXMLGen.taggedValue("deptype", zXType.valueOf(objEditDependency.getDepType()));
                            objXMLGen.taggedCData("depentity", objEditDependency.getDepentity());
                            objXMLGen.taggedCData("depattr", objEditDependency.getDepattr());
                            objXMLGen.taggedCData("depvalue", objEditDependency.getDepvalue());
                            objXMLGen.taggedValue("operator", zXType.valueOf(objEditDependency.getOpeRator()));
                            
                            objXMLGen.taggedCData("relentity", objEditDependency.getRelentity());
                            objXMLGen.taggedCData("relattr", objEditDependency.getRelattr());
                            objXMLGen.taggedCData("relvalue", objEditDependency.getRelvalue());
                            objXMLGen.taggedCData("value", objEditDependency.getValue());
                            
                            // NOTE : The repostiory generates : FKFromAttr - but should be fkfromattr
                            objXMLGen.taggedCData("fkfromattr", objEditDependency.getFkfromattr());
                            // NOTE : The repostiory generates : FKToAttr - but should be fktoattr
                            objXMLGen.taggedCData("fktoattr", objEditDependency.getFktoattr());
                            
                            objXMLGen.closeTag("editdependency");
                        }
                        
                        objXMLGen.closeTag("editdependencies");
                    }
                    
                    objXMLGen.closeTag("editenhancer");
                }
                
                objXMLGen.closeTag("editenhancers");
            }
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    
    }

// NOTE : Ideally we would like to do it like this, however the Digester does not like adding of duplicate
// rules, look in PFDescriptor for the list of parsing rules.

//    /**
//     * Addeds a groups of static parsing rules. No debug code it needed for this.
//     * 
//     * @param pobjDigester A handle to the digester
//     */
//    public abstract void parse(Digester pobjDigester);
    
    //------------------------ Public Methods
    
    /**
     * Get if this tag exists.
     * 
     * <pre>
     * 
     *  returns true if a certain tag exists
     * </pre>
     *
     * @param pstrName Name of the tag to check 
     * @return Returns true if the tag exists.
     */
    public boolean hasTag(String pstrName) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
        }
        
        boolean hasTag = false; 
        try {
        	
            hasTag = ((getTags() == null)?false:((getTags().get(pstrName)==null)?false:true));
            
            return hasTag;
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(hasTag);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Return the value of a tag or a empty string in case the tag had not been found
     *
     * @param pstrName Name of the tagValue 
     * @return Returns the tags value fron the name.
     */
    public String tagValue(String pstrName) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
        }
        
        String tagValue = "";
        
        try {
        	/**
        	 * Short circuit if tags is null.
        	 */
        	if (getTags() == null) return tagValue;
        	
            Tuple objTuple = (Tuple)getTags().get(pstrName);
            if (objTuple != null) {
                tagValue = objTuple.getValue();
            }
            
            return tagValue;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(tagValue);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Object overridden methods 
    
    /**
     * Creates a cloned version with values set during pageflow descriptor parsing.
     * 
     * NOTE : If you are implementing a new pageflow action you will also need to implement this method.
     * @param pobjPageflow The current runtime pageflow.
     * 
     * @return Returns a deep cloned pageflow action.
     */
    public PFAction clone(Pageflow pobjPageflow) {
        PFAction cleanClone = null;
        try {
            // Create a new clean PFAction according to its classname.
            cleanClone = (PFAction)this.getClass().newInstance();
            cleanClone.init(pobjPageflow, pobjPageflow.getPFDesc());
            
            cleanClone.setCached(true); // Check up on this ?
            
            cleanClone.setActionType(getActionType());
            
            cleanClone.setName(getName());
            cleanClone.setHelpid(getHelpid());
            
            if (this.narr != null && this.narr.size() > 0) {
                cleanClone.setNarr((LabelCollection)this.narr.clone());
            }
            
            cleanClone.setNarrisdir(isNarrisdir());
            
            if (getLink() != null) {
                cleanClone.setLink((PFComponent)getLink().clone());
            }
            
            cleanClone.setEntityaction(getEntityaction());
            cleanClone.setLeft(getLeft());
            cleanClone.setTop(getTop());
            
            if (getFormaction() != null) {
                cleanClone.setFormaction((PFUrl)getFormaction().clone());
            }
            
            if (this.entities != null && !this.entities.isEmpty()) {
                cleanClone.setEntities((ZXCollection)this.entities.clone());
            }
            
            cleanClone.setStickyqsaction(getStickyqsaction());
            
            cleanClone.setCached(isCached()); // Overriddening the above ?
            
            if (this.stickyqs != null && this.stickyqs.size() > 0) {
                cleanClone.setStickyqs(CloneUtil.clone((ArrayList)this.stickyqs));
            }
            
            if (this.entitynames != null && this.entitynames.size() > 0) {
                cleanClone.setEntitynames((ZXCollection)this.entitynames.clone());
            }
            
            if (this.contextupdate != null && this.contextupdate.size() > 0) {
                cleanClone.setContextupdate(CloneUtil.clone((ArrayList)this.contextupdate));
            }
            
            if (this.security != null && this.security.size() > 0) {
                cleanClone.setSecurity((ZXCollection)this.security.clone());
            }
            
            if (this.tags != null && this.tags.size() > 0) {
                cleanClone.setTags((ZXCollection)this.tags.clone());
            }
            
            if (this.title != null && !this.title.isEmpty()) {
                cleanClone.setTitle((LabelCollection)getTitle().clone());
            }
            
            if (this.buttons != null && this.buttons.size() > 0) {
                cleanClone.setButtons(CloneUtil.clone(this.buttons));
            }
            
            if (this.refs != null && this.refs.size() > 0) {
                cleanClone.setRefs(CloneUtil.clone(this.refs));
            }
            
            if (this.actions != null && this.actions.size() > 0) {
                cleanClone.setActions(CloneUtil.clone(this.actions));
            }
            
            if (this.popups != null && this.popups.size() > 0) {
                cleanClone.setPopups(CloneUtil.clone(this.popups));
            }
            
            if (this.infomsg != null && this.infomsg.size() > 0) {
                cleanClone.setInfomsg((LabelCollection)getInfomsg().clone());
            }
            
            if (this.errormsg != null && this.errormsg.size() > 0) {
                cleanClone.setErrormsg((LabelCollection)getErrormsg().clone());
            }
            
            if (this.alertmsg != null && this.alertmsg.size() > 0) {
                cleanClone.setAlertmsg((LabelCollection)getAlertmsg().clone());
            }
            
            cleanClone.setFrameHandling(getFrameHandling());
            cleanClone.setAlternateconnection(getAlternateconnection());
            cleanClone.setResolvefk(isResolvefk());
            cleanClone.setAddparitytoclass(isAddparitytoclass());
            cleanClone.setClass(getClazz()); // getClass is from the Object class.
            
            if (this.editenhancers != null && this.editenhancers.size() > 0) {
                cleanClone.setEditenhancers(CloneUtil.clone((ArrayList)this.editenhancers));
            }
            
            if (getBOContexts() != null && getBOContexts().size() > 0) {
            	cleanClone.setBOContexts(CloneUtil.clone((ArrayList)getBOContexts()));
            }
            
            cleanClone.setLimitrows(isLimitrows());
            cleanClone.setMaxrows(getMaxrows());
            cleanClone.setWidth(getWidth());
            cleanClone.setNewrows(getNewrows());
            
            if (getUrl() != null) {
                cleanClone.setUrl((PFUrl)getUrl().clone());
            }
            
            cleanClone.setQueryname(getQueryname());
            cleanClone.setQsprogress(getQsprogress());
            
        } catch (Exception e) {
            getZx().log.error("Failed to clone object", e);
        }
        
        return cleanClone;
    }
    
    /** 
     * @see java.lang.Object#toString()
     **/
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
        toString.append("name", getName());
        toString.append("type", zXType.valueOf(getActionType()));
        return toString.toString();
    }
}