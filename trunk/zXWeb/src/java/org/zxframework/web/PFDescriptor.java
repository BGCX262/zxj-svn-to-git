/*
 * Created on Apr 9, 2004 by Michael Brewer
 * $Id: PFDescriptor.java,v 1.1.2.38 2006/07/24 15:44:52 mike Exp $
 */
package org.zxframework.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.Rule;

import org.xml.sax.Attributes;

import org.zxframework.Label;
import org.zxframework.LabelCollection;
import org.zxframework.Tuple;
import org.zxframework.ZXCollection;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.exception.ParsingException;
import org.zxframework.util.BooleanConverter;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Pageflow descriptor.
 *
 *<pre>
 * We may need to seperate the parsing from the Descriptor object and also make
 * sure that failed parsing only throws a unchecked exception.
 * 
 * Change    : DGS13JAN2003
 * Why       : Changes to action type Query for QueryDef
 * 
 * Change    : DGS13FEB2003
 * Why       : Added 'active' property to URL and collection of 'popups' to action.
 *             Added support for popup actions.
 *             Added methods parseEditPage(s) and parseEntityGroup(s) to support new functionality
 *             for editform pages (tabs/columns).
 *             Added image and image over URLs to buttons.
 * 
 * Change    : BD19FEB03
 * Why       : Added class support to list form
 * 
 * Change    : DGS24FEB2003
 * Why       : New create update property for edit form start action
 * 
 * Change    : DGS28FEB2003
 * Why       : Implement popups
 * 
 * Change    : BD21MAR03
 * Why       : Support for standard popup window action type
 * 
 * Change    : 27MARFEB2003
 * Why       : When creating action objects in parseAction, first set through the iDefaultAction
 *             property to ensure the default interface is available to ASP.
 * 
 * Change    : DGS09APR2003
 * Why       : Added the fklookup edit enhancer
 * 
 * Change    : BD11APR03
 * Why       : Added paging to list form
 * 
 * Change    : DGS14APR2003
 * Why       : Added Entity Massagers
 * 
 * Change    : BD28APR03
 * Why       : Added propagateQS funcion
 * 
 * Change    : BD15MAY03
 * Why       : Added support for footer-less standard popup
 * 
 * Change    : BD22MAY03
 * Why       : Use Cdata for link action and some of the groups where more
 *             flexibility is required. This means support for the new
 *             style of directors that contains &lt; and &gt; characters
 * 
 * Change    : DGS13JUN2003
 * Why       : Added 'comment', 'lastchange' and 'version' properties.
 *             Also change related to 'narrative' added to action.
 * 
 * Change    : BD23JUN03
 * Why       : Added recTree action type
 * 
 * Change    : DGS23JUN2003
 * Why       : Added narrative 'is director' property
 * 
 * Change    : DGS27JUN2003
 * Why       : Added edit sub actions
 * 
 * Change    : DGS03JUL2003
 * Why       : Some recent additions such as edit sub actions are generating empty XML tags
 *             and I noticed other older ones too. Have changed it so that don't generate
 *             them unnecessarily. Saves a bit when parsing and clears clutter from files.
 * 
 * Change    : DGS20AUG2003
 * Why       : Added the fkadd edit enhancer
 * 
 * Change    : DGS27AUG2003
 * Why       : Added the 'save searchform query' tag for PFSearchForm
 * 
 * Change    : DGS18SEP2003
 * Why       : Added grid edit form and grid create update
 * 
 * Change    : DGS26SEP2003
 * Why       : Added support for parsing/writing out XML for an Alternative Connection. This allows
 *             an application to access more than one database.
 * 
 * Change    : DGS20OCT2003
 * Why       : Added new enhancer property 'FKWhere' to restrict the recordset of FK items.
 * 
 * Change    : BD27OCT03
 * Why       : Use cdata rather than tag when generating XML for enhancer stuff
 * 
 * Change    : DGS02FEB2004
 * Why       : All querynames are written as CData.
 * 
 * Change    : DGS12FEB2004
 * Why       : Minor changes to support the 'Matrix' edit form and createupdate.
 *             Also new properties for listform to support multiple columns.
 *             Also new enhancer ability to partially override entity size in dropdowns.
 * 
 * Change    : BD14FEB04
 * Why       : Added support for alertMsg
 * 
 * Change    : BD25FEB04
 * Why       : Added support for framesetCols in stdPopup
 * 
 * Change    : DGS21APR2004
 * Why       : xmlAction: Added Query Where Clause, Source Query Name and Group By
 *             properties to PFQuery
 * 
 * Change    : BD10JUL04
 * Why       : Write queryefName and other tags as CDATA to cater for expressions
 *             and directors
 * 
 * Change    : BD19JUL04
 * Why       : Added support for merging multiple fields onto single line
 *             on edit form (through enhancers)
 * 
 * Change    : BD6AUG04
 * Why       : Changed tagged XML from simple value to cdata
 * 
 * Change    : BD3SEP04
 * Why       : Add support to force user to tick at least one box in refine action
 *  
 * Change    : BD10SEP04
 * Why       : Added caching of XML DOM documents. For this to work, we have
 *             to use freeThreadedDOMDocument. This is slightly slower
 *             than the 'rental' DOMDocument but it pays off with most
 *             pageflow files given their size. The average speed gain
 *             is between 10 - 20% for a page that does little or no
 *             IO; obviously, if there is a lot of IO, the gain in percentage
 *             is less spectacular.
 *             For avarage sized pageflows the absolute gain is about 60ms,
 *             for larger pageflows it can gow up to 100ms. May not sound like
 *             a lot but with 50+ users....
 *              
 * Change    : DGSOCT2004
 * Why       : Added Calendar action type
 * 
 * Change    : MB08NOV2004
 * Why       : Added Re-sort support to GridEditForms.
 * 
 * Change    : MB08NOV2004
 * Why       : Fixed left and top being to short when diagrams are large by casting to a long.
 * 
 * Change    : BD1MAR05 - V1.4:54
 * Why       : Added support for enhancers for multi-line cols and rows
 * 
 * Change    : BD12APR05 - V1.5:3
 * Why       : Added support for BO context
 * 
 * Change    : V1.4:73 DGS04MAY2005
 * Why       : When parsing, for forward compatibility, don't raise an error if an unknown tag
 *             is found. Do trace it if tracing is on.
 *
 * Change    : V1.5:13 BD16MAY05
 * Why       : Added support for paging on result-sets; introduced the 1.4 feature of limitRows
 * 
 * Change    : V1.4:84 - MB25MAY2005
 * Why       : Added new PFLayout control for the web.
 *
 * Change    : BD13JUL05 - V1.5:36
 * Why       : Add noDB option; setting this flag has same effect as passing
 *             -sa=editNoDB around but with having to worry about doing so
 *
 * Change    : BD13JUL05 - V1.5:32
 * Why       : Added support for createUpdateAction
 *
 * Change    : BD4AUG05 - V1.5:43
 * Why       : Added support for re-using boContext of edit form
 * 
 * Change    : V1.5:60 DGS03OCT2005
 * Why       : Added support for List Form totalling. New function 'parseTotalGroups', plus
 *             changes to 'xmlAction' to write new tags.
 * 
 * Change    : V1.5:65 BD17OCT05
 * Why       : Added support for external pageflows and parameter bags
 * 
 * Change    : V1.5:75 - BD3JAN06
 * Why       : Added support for cell action for calendar
 * 
 * Change    : V1.5:77 - BD9JAN06
 * Why       : Multilinerows  / cols enhancers not written away for grid / matrix
 * 
 * Change    : V1.5:95 DGS/BD28APR2006
 * Why       : New feature to be able to make ref buttons appear at the top of forms and
 *             behave like a menu bar
 *   
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFDescriptor extends ZXObject {
	
	//------------------------ Members
	
	private String name;
	private String helpid;
	private LabelCollection title;
	private String version;
	private String lastchange;
	private String comment;
	private ArrayList includes;
	private boolean propagateqs;
	private String startaction;
	private String bodescriptor;
	private String contextid;
	private zXType.pageflowDebugMode debugMode;
	private PFUrl baseurl;
	private ZXCollection security;
	private List contextupdate;
	private ZXCollection actions;
	
	private ZXCollection receiveParameterBags;
	private ZXCollection sendParameterBags;
	
	//------------------------ Constants
	
	// Class names of the objects created
	private static final Class LABEL_CLAZZ = org.zxframework.Label.class;
	private static final Class LABELCOL_CLAZZ = org.zxframework.LabelCollection.class;
	private static final Class PFDIRECTOR_CLAZZ = org.zxframework.web.PFDirector.class;
	private static final Class PFURL_CLAZZ = org.zxframework.web.PFUrl.class;
	private static final Class PFEDITPAGE_CLAZZ = org.zxframework.web.PFEditPage.class;
	private static final Class PFCOMPONENT_CLAZZ = org.zxframework.web.PFComponent.class;
	private static final Class PFLAYOUTCOMPONENT_CLAZZ = org.zxframework.web.PFLayoutComponent.class;
	private static final Class PFCALENDARCATEGORY_CLAZZ = org.zxframework.web.PFCalendarCategory.class;
	private static final Class PFLAYOUTCELL_CLAZZ = org.zxframework.web.PFLayoutCell.class;
	private static final Class PFREF_CLAZZ = org.zxframework.web.PFRef.class;
	private static final Class PFENTITY_CLAZZ = org.zxframework.web.PFEntity.class;
	private static final Class PFCUACTION_CLAZZ = org.zxframework.web.PFCuAction.class;
	private static final Class PFBOCONTEXT_CLAZZ = org.zxframework.web.PFBOContext.class;
	private static final Class PFENTITYMASSAGERS_CLAZZ = org.zxframework.web.PFEntityMassager.class;
	private static final Class PFQRYEXPR_CLAZZ = org.zxframework.web.PFQryExpr.class;
	private static final Class PFEDITENHANCER_CLAZZ = org.zxframework.web.PFEditEnhancer.class;
	private static final Class PFEDITDEPENCY_CLAZZ = org.zxframework.web.PFEditDependency.class;
	
	/**
	 * List of possible Pageflow action classes :
	 */
	protected static final Class PFSEARCHFORM_CLAZZ = org.zxframework.web.PFSearchForm.class;
	protected static final Class PFCREATEUPDATE_CLAZZ = org.zxframework.web.PFCreateUpdate.class;
	protected static final Class PFDBACTION_CLAZZ = org.zxframework.web.PFDBAction.class;
	protected static final Class PFEDITFORM_CLAZZ = org.zxframework.web.PFEditForm.class;
	protected static final Class PFGRIDCREATEUPDATE_CLAZZ = org.zxframework.web.PFGridCreateUpdate.class;
	protected static final Class PFGRIDEDITFORM_CLAZZ = org.zxframework.web.PFGridEditForm.class;
	protected static final Class PFLISTFORM_CLAZZ = org.zxframework.web.PFListForm.class;
	protected static final Class PFLOOPOVER_CLAZZ = org.zxframework.web.PFLoopOver.class;
	protected static final Class PFMATRIXCREATEUPDATE_CLAZZ = org.zxframework.web.PFMatrixCreateUpdate.class;
	protected static final Class PFMATRIXEDITFORM_CLAZZ = org.zxframework.web.PFMatrixEditForm.class;
	protected static final Class PFNULL_CLAZZ = org.zxframework.web.PFNull.class;
	protected static final Class PFQUERY_CLAZZ = org.zxframework.web.PFQuery.class;
	protected static final Class PFRECTREE_CLAZZ = org.zxframework.web.PFRecTree.class;
	protected static final Class PFREFINE_CLAZZ = org.zxframework.web.PFRefine.class;
	protected static final Class PFSTDPOPUP_CLAZZ = org.zxframework.web.PFStdPopup.class;
	protected static final Class PFTREEFORM_CLAZZ = org.zxframework.web.PFTreeForm.class;
	protected static final Class PFLAYOUT_CLAZZ = org.zxframework.web.PFLayout.class;
	protected static final Class PFCALENDAR_CLAZZ = org.zxframework.web.PFCalendar.class;
	
	//----------------------- Parsing and Repository members
	
	// Factories for creating objects - this is cache for the duration of the pageflow action .
	private PFActionFactory pfActionFactory;
	private Digester digester;
	private boolean justInTimeParsing;
	private long lastModified;
	
	/** XML Gen for creating a dump of the pageflow descriptor. **/
	protected XMLGen objXMLGen;
	
	//------------------------ Constructor
	
	/**
	 * Default constructor.
	 */
	public PFDescriptor() {
		super();
		
		/**
		 * Override how booleans get parsed.
		 */
		ConvertUtils.register(new BooleanConverter(), boolean.class);
	}
	
	//------------------------ Getter/Setter
	
	/**
	 * A collection of PFActions.
	 *  
	 * @return Returns the actions. 
	 **/
	public ZXCollection getActions() {
	    if (this.actions == null) {
	        this.actions = new ZXCollection();
	    }
	    
	    return actions;
	}
	
	/**
	 * This gets a Pageflow action of the cached pageflow action. 
	 * 
	 * @param pstrName The name of the pageflow action.
	 * @return Retuns the pageflow action. Null if none is returned.
	 */
	public PFAction getAction(String pstrName) {
	    return (PFAction)this.actions.getCASEINSENSITIVE(pstrName);
	}
    
	/**
	 * @param actions The actions to set.
	 */
	public void setActions(ZXCollection actions) {
	    this.actions = actions;
	}
	
    /**
     * Pageflow parameters.
     * 
     * This defines the pageflow interface.
     * 
	 * @return Returns the receiveParameterBags.
	 */
	public Map getReceiveParameterBags() {
		return receiveParameterBags;
	}

	/**
	 * @param receiveParameterBags The receiveParameterBags to set.
	 */
	public void setReceiveParameterBags(ZXCollection receiveParameterBags) {
		this.receiveParameterBags = receiveParameterBags;
	}

	/**
	 * Send parameter bag.
	 * 
	 * A list of parameters to pass to a external pageflow.
	 * 
	 * @return Returns the sendParameterBags.
	 */
	public Map getSendParameterBags() {
		return sendParameterBags;
	}

	/**
	 * @param sendParameterBags The sendParameterBags to set.
	 */
	public void setSendParameterBags(ZXCollection sendParameterBags) {
		this.sendParameterBags = sendParameterBags;
	}

	/** 
     * The baseurl of the pageflow.
     *  eg : ../jsp/zXGPF.jsp
     * 
     * @return Returns the baseurl. 
     */
    public PFUrl getBaseurl() {
        return baseurl;
    }
    
    /**
     * @param baseurl The baseurl to set.
     */
    public void setBaseurl(PFUrl baseurl) {
        this.baseurl = baseurl;
    }

    /** 
     * @return Returns the bodescriptor. 
     */
    public String getBodescriptor() {
        return bodescriptor;
    }

    /**
     * @param bodescriptor The bodescriptor to set.
     */
    public void setBodescriptor(String bodescriptor) {
        this.bodescriptor = bodescriptor;
    }

    /**
     * Developer comment of the pagflow.
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

    /** @return Returns the contextid. */
    public String getContextid() {
        return contextid;
    }

    /**
     * @param contextid The contextid to set.
     */
    public void setContextid(String contextid) {
        this.contextid = contextid;
    }

    /**
     * @return Returns a ZXCollection (org.zxframework.web.PFDirector) of
     *         contextupdate.
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
     * The enum for the bebug level for the pageflow.
     * 
     * @return Returns the debugMode. 
     */
    public zXType.pageflowDebugMode getDebugMode() {
        return debugMode;
    }

    /**
     * @param debugMode The debugMode to set.
     */
    public void setDebugMode(zXType.pageflowDebugMode debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * The help document linked to this pageflow.
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
     * A Collection of includes for this pageflow.
     * 
     * @return Returns a Collection (org.zxframework.web.PFInclude) of
     *         includes.
     */
    public ArrayList getIncludes() {
        return includes;
    }

    /**
     * @param includes The includes to set.
     */
    public void setIncludes(ArrayList includes) {
        this.includes = includes;
    }

    /**
     * Last change info for developers.
     * 
     * @return Returns the lastchange. 
     */
    public String getLastchange() {
        return lastchange;
    }

    /**
     * @param lastchange The lastchange to set.
     */
    public void setLastchange(String lastchange) {
        this.lastchange = lastchange;
    }

    /**
     * The name of the pageflow.
     * 
     * This should match the file name without extension of the descriptor. It should
     * also be unique for an entire project.
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
     * Whether to propogate the query string.
     * 
     * @return Returns the propagateqs. 
     */
    public boolean isPropagaTeqs() {
        return propagateqs;
    }

    /**
     * @param propagateqs The propagateqs to set.
     */
    public void setPropagateqs(boolean propagateqs) {
        this.propagateqs = propagateqs;
    }
    
    /**
     * A ZXCollection of user groups that is allowed to access this pageflow.
     * 
     * @return Returns a ZXCollection(Tuple) of security settings. 
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
     * The name of the pageflow action to start with.
     * 
     * @return Returns the startaction. 
     */
    public String getStartaction() {
        return startaction;
    }

    /**
     * @param startaction The startaction to set.
     */
    public void setStartaction(String startaction) {
        this.startaction = startaction;
    }

    /**
     * The title of the pageflow.
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
     * The version number of the pageflow used by developers.
     * 
     * @return Returns the version. 
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    //------------------------ Digester helper methods.
    
    /**
     * @deprecated Using BooleanConverter
     * @param propagateqs The propagateqs to set.
     */
    public void setPropagateqs(String propagateqs) {
        this.propagateqs = StringUtil.booleanValue(propagateqs);
    }

    /**
     * This also sets the Enum of the html debug
     * 
     * @param htmldebug The htmldebug to set.
     */
    public void setHtmldebug(String htmldebug) {
        if (StringUtil.len(htmldebug) == 0) {
            htmldebug = "off";
        }
        this.debugMode = zXType.pageflowDebugMode.getEnum(htmldebug);
    }
    
    //------------------------ Parsing getters/setters
    
    /**
     * @return Returns a handle to the XMLGen object.
     */
    public XMLGen getXMLGen() {
        return this.objXMLGen;
    }
    
	/**
	 * The last modified date of the BO descriptor.
	 * 
	 * @return Returns the lastModified.
	 */
	public long getLastModified() {
		return lastModified;
	}
	 
	/**
	 * @param lastModified The lastModified to set.
	 */
	public void setLastModified(long lastModified) {
	    this.lastModified = lastModified;
	}
	
	//------------------------ Parsing methods.
	    
	/**
	 * Initialise the pageflow descriptor.
	 *	
	 * @param pobjPageflow A handle to the main PageFlow object.
	 * @param pstrXML The full url of the PageFlow page file.
	 * @param pblnJustInTimeParsing Whether to enable just in time parsing.
	 * @return Returns the return code of the method.
	 * @throws ParsingException Thrown if init fails. This is probably due to a parsing error.
	 */
	public zXType.rc init(Pageflow pobjPageflow, 
						  String pstrXML, 
						  boolean pblnJustInTimeParsing) throws ParsingException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjPageflow", pobjPageflow);
			getZx().trace.traceParam("pstrXML", pstrXML);
			getZx().trace.traceParam("pblnJustInTimeParsing", pblnJustInTimeParsing);
		}
		 
		zXType.rc init = zXType.rc.rcOK; 
	
		try {
			// Initialize local variables.
			this.justInTimeParsing = pblnJustInTimeParsing;
			if (this.justInTimeParsing) {
				getZx().log.warn("We do not support just in time parsing with Digester");
			}
			
			// Default to off :
			this.debugMode = zXType.pageflowDebugMode.pdmOff;
				         
			if (digester == null) {
				digester = new Digester();
			}
			
			digester.setValidating(false);
			digester.setRules(new ExtendedBaseRules());
			
			digester.push(this); // Do the local setters
			
			digester.addBeanPropertySetter("pageflow/name");
			digester.addBeanPropertySetter("pageflow/helpid");
			parseLabels("pageflow/title");
			digester.addSetNext("pageflow/title", "setTitle");
			digester.addBeanPropertySetter("pageflow/version");
			
			// Only init these values if in the Editor runmode.
			if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
				digester.addBeanPropertySetter("pageflow/lastchange");
				digester.addBeanPropertySetter("pageflow/comment");
			}
			
			parseIncludes("pageflow/includes");
			digester.addSetNext("pageflow/includes", "setIncludes");
			 
			digester.addBeanPropertySetter("pageflow/propagateqs");
			digester.addBeanPropertySetter("pageflow/startaction");
			digester.addBeanPropertySetter("pageflow/bodescriptor");
			digester.addBeanPropertySetter("pageflow/contextid");
			digester.addBeanPropertySetter("pageflow/htmldebug");
			 
			parseUrl("pageflow/baseurl");
			digester.addSetNext("pageflow/baseurl", "setBaseurl");
			 
			parseSingleValues("pageflow/security");
			digester.addSetNext("pageflow/security", "setSecurity");
			 
			parseDirectors("pageflow/contextupdate");
			digester.addSetNext("pageflow/contextupdate", "setContextupdate");                                     
			
			parseActions("pageflow/actions", pobjPageflow);
			digester.addSetNext("pageflow/actions", "setActions");
			
			/**
			 * Parse parameter bags.
			 */
			parseParameterBags("pageflow/sendparameterbags", "sendparameterbag");
			digester.addSetNext("pageflow/sendparameterbags", "setSendParameterBags");
			parseParameterBags("pageflow/receiveparameterbags", "receiveparameterbag");
			digester.addSetNext("pageflow/receiveparameterbags", "setReceiveParameterBags");
			 
			File file = new File(pstrXML);
			setLastModified(file.lastModified());
			
			digester.parse(file);
			
			return init;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Initialise the pageflow descriptor.", e);
			if (getZx().log.isErrorEnabled()) {
			    getZx().log.error("Parameter : pobjPageflow = "+ pobjPageflow);
			    getZx().log.error("Parameter : pstrXML = "+ pstrXML);
			    getZx().log.error("Parameter : pblnJustInTimeParsing = "+ pblnJustInTimeParsing);
		    }
		    
		    if (getZx().throwException) throw new ParsingException(e);
		    init = zXType.rc.rcError;
		    return init;
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.returnValue(init);
		        getZx().trace.exitMethod();
		    }
		}
	}
	     
	/**
	 * Parsing the Query Expression used in a PFQuery.
	 * 
	 * @param pstrPattern The xpath to the beginning of the node.
	 */
	private void parseQueryExpr(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, PFQRYEXPR_CLAZZ);

		digester.addBeanPropertySetter(pstrPattern + "/lhs");
		digester.addBeanPropertySetter(pstrPattern + "/rhs");
		digester.addBeanPropertySetter(pstrPattern + "/attrlhs");
		digester.addBeanPropertySetter(pstrPattern + "/attrrhs");
		digester.addBeanPropertySetter(pstrPattern + "/operator");
		
		parseQueryExpr2(pstrPattern + "/queryexprlhs");
		digester.addSetNext(pstrPattern + "/queryexprlhs", "setQueryexprlhs");
		parseQueryExpr2(pstrPattern + "/queryexprrhs");
		digester.addSetNext(pstrPattern + "/queryexprrhs", "setQueryexprrhs");    
	}

	/**
	 * Parsing the queryexprlhs or queryexprrhs expression.
	 * 
	 * @param pstrPattern The xpath to the beginning of the node.
	 */
	private void parseQueryExpr2(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, PFQRYEXPR_CLAZZ);
		
		digester.addBeanPropertySetter(pstrPattern + "/lhs");
		digester.addBeanPropertySetter(pstrPattern + "/rhs");
		digester.addBeanPropertySetter(pstrPattern + "/attrlhs");
		digester.addBeanPropertySetter(pstrPattern + "/attrrhs");
		digester.addBeanPropertySetter(pstrPattern + "/operator");
	}
	    
	/**
	 * Parse edit enhancers.
	 * 
	 * @param pstrPattern The Xpath of a edit enhancer.
	 */
	private void parseEditEnhancers(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, ArrayList.class);
		
		digester.addObjectCreate(pstrPattern + "/editenhancer", PFEDITENHANCER_CLAZZ);
		
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/entity");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/attr");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/labelclass");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/inputClass"); // TODO : Note to DB that this is an exception in the xml naming convension
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/stdbuttonclass");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/spellcheck");
		// DGS09APR2003: Added the fklookup boolean to enhancers.
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/fklookup");
		// DGS20AUG2003: Added the fkadd boolean to enhancers.
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/fkadd");
		// DGS 20OCT2003: Added FK where clause
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/fkwhere");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/tabindex");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/disabled");
		
		// Replace with <eventhandlers><event>action</event> ?? 
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onfocus");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onblur");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onchange");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onkeydown");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onkeypress");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onkeyup");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onclick");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onmouseover");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onmouseout"); 
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onmousedown");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/onmouseup");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/entitysize");
		
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/postlabel");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/postlabelclass");
		
		/**
		 * BD1MAR05 - V1.4:54.
		 */
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/multilinerows");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/multilinecols");
		
		parseRefs(pstrPattern + "/editenhancer/refs", zXType.pageflowRefType.prtFieldRef);
		digester.addSetNext(pstrPattern + "/editenhancer/refs", "setRefs");
		parseEditDependencies(pstrPattern + "/editenhancer/editdependencies");
		digester.addSetNext(pstrPattern + "/editenhancer/editdependencies", "setEditdependencies");
		
		/**
		 * BD19JUL04 - Added
		 */
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/mergewithprevious");
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/mergewithnext");		
		digester.addBeanPropertySetter(pstrPattern + "/editenhancer/nolabel");		
		
		digester.addSetNext(pstrPattern + "/editenhancer", "add");         
	}
	    
    /**
     * @param pstrPattern
     */
    private void parseEditDependencies(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, ArrayList.class);
		digester.addObjectCreate(pstrPattern + "/editdependency", PFEDITDEPENCY_CLAZZ);
		digester.addSetNestedProperties(pstrPattern + "/editdependency");
 		digester.addSetNext(pstrPattern + "/editdependency", "add");         
    }
	     
	/**
     * @param pstrPattern
     */
    private void parseEntities(String pstrPattern) {
        digester.addObjectCreate(pstrPattern, ZXCollection.class);
        
        parseEntity(pstrPattern + "/item");
        
        digester.addSetNext(pstrPattern + "/item", "put");
    }
    
    /**
     * @param pstrPattern
     */
    private void parseEntity(String pstrPattern) {
        digester.addObjectCreate(pstrPattern, PFENTITY_CLAZZ);
        
        digester.addBeanPropertySetter(pstrPattern + "/name");
        
        if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
            digester.addBeanPropertySetter(pstrPattern + "/comment");
        }
        
        digester.addBeanPropertySetter(pstrPattern + "/entity");
        digester.addBeanPropertySetter(pstrPattern + "/refboaction");
        digester.addBeanPropertySetter(pstrPattern + "/pk");
        digester.addBeanPropertySetter(pstrPattern + "/alias");
        digester.addBeanPropertySetter(pstrPattern + "/wheregroup");
        digester.addBeanPropertySetter(pstrPattern + "/selecteditgroup");
        digester.addBeanPropertySetter(pstrPattern + "/selectlistgroup");
        digester.addBeanPropertySetter(pstrPattern + "/groupbygroup");
        digester.addBeanPropertySetter(pstrPattern + "/lockgroup");
        digester.addBeanPropertySetter(pstrPattern + "/visiblegroup");
        digester.addBeanPropertySetter(pstrPattern + "/pkwheregroup");
        digester.addBeanPropertySetter(pstrPattern + "/listgroup");
        digester.addBeanPropertySetter(pstrPattern + "/lcopygroup");
        digester.addBeanPropertySetter(pstrPattern + "/resolvefk");
        digester.addBeanPropertySetter(pstrPattern + "/allownew");
        
        parseDirectors(pstrPattern + "/attrvalues");
        digester.addSetNext(pstrPattern + "/attrvalues", "setAttrvalues");
        
        parseEntityMassagers(pstrPattern + "/entitymassagers");
        digester.addSetNext(pstrPattern + "/entitymassagers", "setEntitymassagers");
    }
	    
    /**
     * Parse the create update actions.
     * 
     * @param pstrPattern The Xpath of a create update actions.
     */
    private void parseCUActions(String pstrPattern) {
        digester.addObjectCreate(pstrPattern, ArrayList.class);
        
        digester.addObjectCreate(pstrPattern + "/cuaction", PFCUACTION_CLAZZ);

        digester.addBeanPropertySetter(pstrPattern + "/cuaction/name");
        digester.addBeanPropertySetter(pstrPattern + "/cuaction/comment");
        digester.addBeanPropertySetter(pstrPattern + "/cuaction/active");
        
		parseLabels(pstrPattern + "/cuaction/msg");
		digester.addSetNext(pstrPattern + "/cuaction/narr", "setMsg");
		
		digester.addBeanPropertySetter(pstrPattern + "/cuaction/rc");
        digester.addBeanPropertySetter(pstrPattern + "/cuaction/focusattr");
        digester.addBeanPropertySetter(pstrPattern + "/cuaction/action");
        
        digester.addSetNext(pstrPattern + "/cuaction", "put");
    }
	    
    /**
     * @param pstrPattern The full xpath to the bo context nodes.
     */
    private void parsePFBOContexts(String pstrPattern) {
        
        digester.addObjectCreate(pstrPattern, ArrayList.class);
        
        digester.addObjectCreate(pstrPattern + "/bocontext", PFBOCONTEXT_CLAZZ);
        
        digester.addBeanPropertySetter(pstrPattern + "/bocontext/name");
        
        if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
            digester.addBeanPropertySetter(pstrPattern + "/bocontext/comment");
        }
        
        digester.addBeanPropertySetter(pstrPattern + "/bocontext/entity");
        digester.addBeanPropertySetter(pstrPattern + "/bocontext/alias");
        digester.addBeanPropertySetter(pstrPattern + "/bocontext/active");
        digester.addBeanPropertySetter(pstrPattern + "/bocontext/loadgroup");
        digester.addBeanPropertySetter(pstrPattern + "/bocontext/identificationmethod");
        digester.addBeanPropertySetter(pstrPattern + "/bocontext/identification");
        digester.addBeanPropertySetter(pstrPattern + "/bocontext/resolvefk");
        
        parseDirectors(pstrPattern + "/bocontext/attrvalues");
        digester.addSetNext(pstrPattern + "/bocontext/attrvalues", "setAttrvalues");
        
        digester.addSetNext(pstrPattern + "/bocontext", "add");
    }
    
     /**
      * This will parse entitymassagers that appear in PFActions Entities. This
      * will populate a collections of PFEntityMassager objects.
      * 
      * <pre>
      * 
      *  Usages : 
      *   parseEntityMassagers(pstrPattern + &quot;/action/entitymassagers&quot;);
      *   digester.addSetNext(pstrPattern + &quot;/action/entitymassagers&quot;, &quot;setEntitymassagers&quot;);                                     
      *  
      *  Example page : 
      *  &lt;entitymassagers&gt;
      *  	&lt;entitymassager&gt;
      *  		&lt;attr&gt;smmry&lt;/attr&gt;
      *  		&lt;property&gt;outputlength&lt;/property&gt;
      *  		&lt;value&gt;20&lt;/value&gt;
      *  	&lt;/entitymassager&gt;
      *  	&lt;entitymassager&gt;
      *  		&lt;attr&gt;txt&lt;/attr&gt;
      *  		&lt;property&gt;outputlength&lt;/property&gt;
      *  		&lt;value&gt;50&lt;/value&gt;
      *  	&lt;/entitymassager&gt;
      *  &lt;/entitymassagers&gt;
      *  
     *  
      * </pre>
      * 
      * @param pstrPattern The XPATH of entity massagers 
      * 			eg : pageflow/actions/action/entitymassagers
      */
     private void parseEntityMassagers(String pstrPattern) {
         digester.addObjectCreate(pstrPattern, ArrayList.class);
         digester.addObjectCreate(pstrPattern + "/entitymassager", PFENTITYMASSAGERS_CLAZZ);
         // this will set all of the bean property avaiable in the page
         digester.addSetNestedProperties(pstrPattern + "/entitymassager"); 
         digester.addSetNext(pstrPattern + "/entitymassager", "add");         
     }
     
	/**
	 * @param pstrPattern The XPATH of the pageflow actions.
	 * @param pobjPageflow A handle to the current pageflow.
	 */
	private void parseActions(String pstrPattern, Pageflow pobjPageflow) {
		digester.addObjectCreate(pstrPattern, ZXCollection.class);
		
        if (this.pfActionFactory == null) {
            this.pfActionFactory = new PFActionFactory(pobjPageflow, this);
        }
        
        digester.addFactoryCreate(pstrPattern + "/action", pfActionFactory, true);

        digester.addBeanPropertySetter(pstrPattern + "/action/name");
        digester.addBeanPropertySetter(pstrPattern + "/action/helpid");
        if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
            digester.addBeanPropertySetter(pstrPattern + "/action/comment");
        }
		parseLabels(pstrPattern + "/action/narr");
		digester.addSetNext(pstrPattern + "/action/narr", "setNarr");
		digester.addBeanPropertySetter(pstrPattern + "/action/narrisdir");
		parseComponent(pstrPattern + "/action/link");
		digester.addSetNext(pstrPattern + "/action/link", "setLink");
		digester.addBeanPropertySetter(pstrPattern + "/action/entityaction");
		digester.addBeanPropertySetter(pstrPattern + "/action/left");
		digester.addBeanPropertySetter(pstrPattern + "/action/top");
		
		parseUrl(pstrPattern + "/action/formaction");
        digester.addSetNext(pstrPattern + "/action/formaction", "setFormaction");
		
		parseEntities(pstrPattern + "/action/entities");
		digester.addSetNext(pstrPattern + "/action/entities", "setEntities");
		
        parsePFBOContexts(pstrPattern + "/action/bocontexts");
        digester.addSetNext(pstrPattern + "/action/bocontexts", "setBOContexts");
        
		digester.addBeanPropertySetter(pstrPattern + "/action/stickyqsaction");
		digester.addBeanPropertySetter(pstrPattern + "/action/cached");
		parseDirectors(pstrPattern + "/action/stickyqs");
		digester.addSetNext(pstrPattern + "/action/stickyqs", "setStickyqs");                                     
		
		parseSingleValues(pstrPattern + "/action/entitynames");
		digester.addSetNext(pstrPattern + "/action/entitynames", "setEntitynames");
		
		parseDirectors(pstrPattern + "/action/contextupdate");
		digester.addSetNext(pstrPattern + "/action/contextupdate", "setContextupdate");       
		
		parseSingleValues(pstrPattern + "/action/security");
		digester.addSetNext(pstrPattern + "/action/security", "setSecurity");
		
		parseTags(pstrPattern + "/action/tags");
		digester.addSetNext(pstrPattern + "/action/tags", "setTags");
		
		parseLabels(pstrPattern + "/action/title");
		digester.addSetNext(pstrPattern + "/action/title", "setTitle");		
		parseRefs(pstrPattern + "/action/buttons", zXType.pageflowRefType.prtButton);
		digester.addSetNext(pstrPattern + "/action/buttons", "setButtons");
		parseRefs(pstrPattern + "/action/refs", zXType.pageflowRefType.prtRef);
		digester.addSetNext(pstrPattern + "/action/refs", "setRefs");
		parseRefs(pstrPattern + "/action/actions", zXType.pageflowRefType.prtAction);
		digester.addSetNext(pstrPattern + "/action/actions", "setActions");
		parseRefs(pstrPattern + "/action/popups", zXType.pageflowRefType.prtAction);
		digester.addSetNext(pstrPattern + "/action/popups", "setPopups");
		parseLabels(pstrPattern + "/action/infomsg");
		digester.addSetNext(pstrPattern + "/action/infomsg", "setInfomsg");
		parseLabels(pstrPattern + "/action/errormsg");
		digester.addSetNext(pstrPattern + "/action/errormsg", "setErrormsg");
		parseLabels(pstrPattern + "/action/alertmsg");
		digester.addSetNext(pstrPattern + "/action/alertmsg", "setAlertmsg");
		digester.addBeanPropertySetter(pstrPattern + "/action/framehandling");
		digester.addBeanPropertySetter(pstrPattern + "/action/alternateconnection");
        
        /**
         * Added support for limit rows.
         * We might as well implement this globally accross all pageflow actions.
         * As GridEditForm/Calendar/ListForm etc.. use MaxRows setting.
         */
        digester.addBeanPropertySetter(pstrPattern + "/action/limitrows");
		
		//--------------------------------------- Promoted from subclassses 
        digester.addBeanPropertySetter(pstrPattern + "/action/resolvefk");
        digester.addBeanPropertySetter(pstrPattern + "/action/queryname");
		//---------------------------------------- Generic type queries
        
        digester.addBeanPropertySetter(pstrPattern + "/action/addparitytoclass");
        // Setting the class is trick, coz class is a retricted word in java
        digester.addBeanPropertySetter(pstrPattern + "/action/class", "clazz");  
		parseEditEnhancers(pstrPattern + "/action/editenhancers");
		digester.addSetNext(pstrPattern + "/action/editenhancers", "setEditenhancers");
		
		//--------------------------------------------------------------- List type queries
        digester.addBeanPropertySetter(pstrPattern + "/action/maxrows");
        digester.addBeanPropertySetter(pstrPattern + "/action/width");
        digester.addBeanPropertySetter(pstrPattern + "/action/newrows");
        parseUrl(pstrPattern + "/action/url");
        digester.addSetNext(pstrPattern + "/action/url", "setUrl");
		//----------------------------------------------------------- Promoted from subclassses 
        
        
        /***********************************************************************
         * ** OK, well this SHOULD go into the individual pageflow types. 
         * ** But it is SAX not DOM :).
         * ** Will EVENTUALLY use XMLPull. Like DOM but faster than SAX :).
         **********************************************************************/
        
        //----------------------------------------------------------------------- PFDBAction
        digester.addBeanPropertySetter(pstrPattern + "/action/dbactionentity");
        parseLabels(pstrPattern + "/action/dbactionerrormsg");
        digester.addSetNext(pstrPattern + "/action/dbactionerrormsg", "setDbactionerrormsg");
        parseLabels(pstrPattern + "/action/dbactioninfomsg");
        digester.addSetNext(pstrPattern + "/action/dbactioninfomsg", "setDbactioninfomsg");
        digester.addBeanPropertySetter(pstrPattern + "/action/dbactiontype");
        digester.addBeanPropertySetter(pstrPattern + "/action/reset");
        digester.addBeanPropertySetter(pstrPattern + "/action/setautomatics");
        // DGS24APR2003: New tag for error action (a component):
        parseComponent(pstrPattern + "/action/erroraction");
        digester.addSetNext(pstrPattern + "/action/erroraction", "setErroraction");
        //------------------------------------------------------------------------ PFDBAction
        
        //------------------------------------------------------------------------ PFEditForm
		digester.addBeanPropertySetter(pstrPattern + "/action/editformtype");
		digester.addBeanPropertySetter(pstrPattern + "/action/nodb");
        
		parseTags(pstrPattern + "/action/editsubactions");
        digester.addSetNext(pstrPattern + "/action/editsubactions", "setEditsubactions");
		
        parseEditPages(pstrPattern + "/action/editpages");
        digester.addSetNext(pstrPattern + "/action/editpages", "setEditpages");
        
		digester.addBeanPropertySetter(pstrPattern + "/action/qsprogress");
		digester.addBeanPropertySetter(pstrPattern + "/action/qssubaction");
        //----------------------------------------------------------------------- PFEditForm
		
        //----------------------------------------------------------------------- PFQuery
        digester.addBeanPropertySetter(pstrPattern + "/action/querytype");
        digester.addBeanPropertySetter(pstrPattern + "/action/distinct"); 
        digester.addBeanPropertySetter(pstrPattern + "/action/outerjoin");
        digester.addBeanPropertySetter(pstrPattern + "/action/whereclauseonly");
        digester.addBeanPropertySetter(pstrPattern + "/action/entityl");
        digester.addBeanPropertySetter(pstrPattern + "/action/entitym");
        digester.addBeanPropertySetter(pstrPattern + "/action/entityr");
        digester.addBeanPropertySetter(pstrPattern + "/action/searchform");
        /**
         * DGS21APR2004: New tags for Group By, Source Query Name and Query
         * Where Clause:
         */
        digester.addBeanPropertySetter(pstrPattern + "/action/groupby");
        digester.addBeanPropertySetter(pstrPattern + "/action/sourcequeryname");
        digester.addBeanPropertySetter(pstrPattern + "/action/querywhereclause");
        digester.addBeanPropertySetter(pstrPattern + "/action/querydefname");
        digester.addBeanPropertySetter(pstrPattern + "/action/querydefscope");
        /** DGS29APR2003: New tag for Query Expression (an object): */
        parseQueryExpr(pstrPattern + "/action/queryexpr");
        digester.addSetNext(pstrPattern + "/action/queryexpr", "setQueryexpr");
        //---------------------------------------------------------------------- PFQuery
        
        //---------------------------------------------------------------------- PFLoopOver
        parseComponent(pstrPattern + "/action/loopoveraction");
        digester.addSetNext(pstrPattern + "/action/loopoveraction", "setLoopoveraction");
        parseLabels(pstrPattern + "/action/loopovererrormsg");
        digester.addSetNext(pstrPattern + "/action/loopovererrormsg", "setLoopovererrormsg");
        parseLabels(pstrPattern + "/action/loopoverinfomsg");
        digester.addSetNext(pstrPattern + "/action/loopoverinfomsg", "setLoopoverinfomsg");
        parseLabels(pstrPattern + "/action/loopoverrefineerrormsg");
        digester.addSetNext(pstrPattern + "/action/loopoverrefineerrormsg", "setLoopoverrefineerrormsg");
        digester.addBeanPropertySetter(pstrPattern + "/action/transaction");
        digester.addBeanPropertySetter(pstrPattern + "/action/refinelist");
        //---------------------------------------------------------------------- PFLoopOver
        
        //---------------------------------------------------------------------- PFRefine/PFLoopOver
        digester.addBeanPropertySetter(pstrPattern + "/action/mustrefine");
        //---------------------------------------------------------------------- PFRefine/PFLoopOver
        
        //---------------------------------------------------------------------- PFRefine
        parseComponent(pstrPattern + "/action/refineerroraction");
        digester.addSetNext(pstrPattern + "/action/refineerroraction", "setRefineerroraction");
        parseLabels(pstrPattern + "/action/refineerrormsg");
        digester.addSetNext(pstrPattern + "/action/refineerrormsg", "setRefineerrormsg");
        //---------------------------------------------------------------------- PFRefine
        
        //--------------------------------------------------------------------- PFLoopOver/PFQuery
        /**
         * DGS 13JAN2003: As part of coding new query type 'QueryDef', replace
         * defunct 5 args with new querydef properties
         */
        digester.addBeanPropertySetter(pstrPattern + "/action/query");
        //--------------------------------------------------------------------- PFLoopOver/PFQuery
        
        //------------------------------------------------ PFCreateUpdate/PFGridCreateUpdate/PFMatrixCreateUpdate
        digester.addBeanPropertySetter(pstrPattern + "/action/linkededitform");
        parseComponent(pstrPattern + "/action/editformstartaction");
        digester.addSetNext(pstrPattern + "/action/editformstartaction", "setEditformstartaction");
        digester.addBeanPropertySetter(pstrPattern + "/action/useeditbocontext");
        parseCUActions(pstrPattern + "/action/cuactions");
        digester.addSetNext(pstrPattern + "/action/cuactions", "setEditformstartaction");
        //------------------------------------------------ PFCreateUpdate/PFGridCreateUpdate/PFMatrixCreateUpdate
        
        //------------------------------------------------------------------- PFStdPopup
        digester.addBeanPropertySetter(pstrPattern + "/action/lockid");
        digester.addBeanPropertySetter(pstrPattern + "/action/lockentity");
        digester.addBeanPropertySetter(pstrPattern + "/action/framesetcols");
        digester.addBeanPropertySetter(pstrPattern + "/action/navigation");
        digester.addBeanPropertySetter(pstrPattern + "/action/footer");
        parseUrl(pstrPattern + "/action/headerurl");
        digester.addSetNext(pstrPattern + "/action/headerurl", "setHeaderurl");
        parseUrl(pstrPattern + "/action/footerurl");
        digester.addSetNext(pstrPattern + "/action/footerurl", "setFooterurl");
        digester.addBeanPropertySetter(pstrPattern + "/action/framesetrows");
		parseRefs(pstrPattern + "/action/tabs", zXType.pageflowRefType.prtRef);
		digester.addSetNext(pstrPattern + "/action/tabs", "setTabs");
        parseUrl(pstrPattern + "/action/navigationurl");
        digester.addSetNext(pstrPattern + "/action/navigationurl", "setNavigationurl");
        //------------------------------------------------------------------ PFStdPopup
        
        //------------------------------------------------------------------ PFStdPopup/PFListForm
        digester.addBeanPropertySetter(pstrPattern + "/action/qspk");
        //------------------------------------------------------------------ PFStdPopup/PFListForm
        
        //------------------------------------------------------------------ PFListForm 
        parseUrl(pstrPattern + "/action/resorturl");
        digester.addSetNext(pstrPattern + "/action/resorturl", "setResorturl");
        digester.addBeanPropertySetter(pstrPattern + "/action/autocheck");
        digester.addBeanPropertySetter(pstrPattern + "/action/multilist");
        digester.addBeanPropertySetter(pstrPattern + "/action/multicolumn");
        digester.addBeanPropertySetter(pstrPattern + "/action/rowspercolumn");
        digester.addBeanPropertySetter(pstrPattern + "/action/qsentity");
        /** v1.5:60 DGS19SEP2005: Added totals */
        parseTotalGroups(pstrPattern + "/action/totalgroups", pstrPattern + "/action/totalgroups/totalgroup");
        digester.addSetNext(pstrPattern + "/action/totalgroups", "setTotalgroups");
        parseTotalGroups(pstrPattern + "/action/totalgroupbys", pstrPattern + "/action/totalgroupbys/totalgroupby");
        digester.addSetNext(pstrPattern + "/action/totalgroupbys", "setTotalgroupbys");
        digester.addBeanPropertySetter(pstrPattern + "/action/totalrowclass");
        digester.addBeanPropertySetter(pstrPattern + "/action/totalcellclass");
        digester.addBeanPropertySetter(pstrPattern + "/action/grandtotal");
        //------------------------------------------------------------------ PFListForm 
        
        //------------------------------------------------------------------ PFListForm/PFGridEditForm
        parseUrl(pstrPattern + "/action/pagingurl");
        digester.addSetNext(pstrPattern + "/action/pagingurl", "setPagingurl");
        //------------------------------------------------------------------ PFListForm/PFGridEditForm

        //------------------------------------------------------------------ PFSearchForm
        digester.addBeanPropertySetter(pstrPattern + "/action/savesearch");
        //------------------------------------------------------------------ PFSearchForm
        
        //------------------------------------------------------------------ PFTreeForm
        // tag 'urls' and the new tag 'treelevel'
        parseTreeLevels(pstrPattern + "/action/treelevels");
        digester.addSetNext(pstrPattern + "/action/treelevels", "setTreelevels");
        parseLabels(pstrPattern + "/action/treetitle");
        digester.addSetNext(pstrPattern + "/action/treetitle", "setTreetitle");
        digester.addBeanPropertySetter(pstrPattern + "/action/treetype");
        digester.addBeanPropertySetter(pstrPattern + "/action/xpos");
        digester.addBeanPropertySetter(pstrPattern + "/action/ypos");
        //------------------------------------------------------------------ PFTreeForm
        
        // ------------------------------------------------------------------ PFRecTree
        digester.addBeanPropertySetter(pstrPattern + "/action/treeentity");
        // active - Duplicat see PFCalendar.
        digester.addBeanPropertySetter(pstrPattern + "/action/nodeclass");
        digester.addBeanPropertySetter(pstrPattern + "/action/nodeaddparity");
        digester.addBeanPropertySetter(pstrPattern + "/action/itemclass");
        digester.addBeanPropertySetter(pstrPattern + "/action/itemaddparity");
        parseUrl(pstrPattern + "/action/treenodeurl");
        digester.addSetNext(pstrPattern + "/action/treenodeurl", "setTreenodeurl");
        parseUrl(pstrPattern + "/action/itemurl");
        digester.addSetNext(pstrPattern + "/action/itemurl", "setItemurl");
        digester.addBeanPropertySetter(pstrPattern + "/action/nodeopen");
        digester.addBeanPropertySetter(pstrPattern + "/action/treeopenmode");
        // ------------------------------------------------------------------ PFRecTree
        
        //------------------------------------------------------------------ PFMatrixEditForm
        digester.addBeanPropertySetter(pstrPattern + "/action/rowslinkedquery");
        digester.addBeanPropertySetter(pstrPattern + "/action/colslinkedquery");
        digester.addBeanPropertySetter(pstrPattern + "/action/rowsentity");
        digester.addBeanPropertySetter(pstrPattern + "/action/colsentity");
        digester.addBeanPropertySetter(pstrPattern + "/action/rowslabelswidth");
        digester.addBeanPropertySetter(pstrPattern + "/action/colslabels");
        digester.addBeanPropertySetter(pstrPattern + "/action/cellsentity");
        digester.addBeanPropertySetter(pstrPattern + "/action/verticallystacked");
        digester.addBeanPropertySetter(pstrPattern + "/action/deletewhen");
        //------------------------------------------------------------------ PFMatrixEditForm
        
        //------------------------------------------------------------------ PFCalendar
        digester.addBeanPropertySetter(pstrPattern + "/action/labelwidth");
        digester.addBeanPropertySetter(pstrPattern + "/action/colwidth");
        digester.addBeanPropertySetter(pstrPattern + "/action/minheight");
        digester.addBeanPropertySetter(pstrPattern + "/action/maxrows");
        digester.addBeanPropertySetter(pstrPattern + "/action/rowspercell");
        digester.addBeanPropertySetter(pstrPattern + "/action/cellmode");
        digester.addBeanPropertySetter(pstrPattern + "/action/orientation");
        digester.addBeanPropertySetter(pstrPattern + "/action/currentclass");
        digester.addBeanPropertySetter(pstrPattern + "/action/normalclass");
        digester.addBeanPropertySetter(pstrPattern + "/action/anchorentity");
        digester.addBeanPropertySetter(pstrPattern + "/action/anchorattr");
        digester.addBeanPropertySetter(pstrPattern + "/action/currentdate");
        digester.addBeanPropertySetter(pstrPattern + "/action/startperiod");
        digester.addBeanPropertySetter(pstrPattern + "/action/zoomfactor");
        digester.addBeanPropertySetter(pstrPattern + "/action/active"); // PFRecTree & PFCalendar
        digester.addBeanPropertySetter(pstrPattern + "/action/labelformat");
        parseUrl(pstrPattern + "/action/prevurl");
        digester.addSetNext(pstrPattern + "/action/prevurl", "setPrevurl");
        parseUrl(pstrPattern + "/action/nexturl");
        digester.addSetNext(pstrPattern + "/action/nexturl", "setNexturl");
        parseUrl(pstrPattern + "/action/zoomurl");
        digester.addSetNext(pstrPattern + "/action/zoomurl", "setZoomurl");
        parseUrl(pstrPattern + "/action/cellurl");
        digester.addSetNext(pstrPattern + "/action/cellurl", "setCellurl");
		parseLabels(pstrPattern + "/action/calendartitle");
		digester.addSetNext(pstrPattern + "/action/calendartitle", "setCalendartitle");
		parseCategories(pstrPattern + "/action/categories");
		digester.addSetNext(pstrPattern + "/action/categories", "setCategories");
        //--------------------------------------------------------------------- PFCalendar
        
        //--------------------------------------------------------------------- PFLayout
        // The template file to use.
        digester.addBeanPropertySetter(pstrPattern + "/action/template");
		// Used for the cell layout method
        digester.addBeanPropertySetter(pstrPattern + "/action/layout");
        // The subactions to perform.
		parseLayoutComponents(pstrPattern + "/action/subactions");
		digester.addSetNext(pstrPattern + "/action/subactions", "setSubactions");
		parseCells(pstrPattern + "/action/cells");
		digester.addSetNext(pstrPattern + "/action/cells", "setCells");
        //-------------------------------------------------------------------- PFLayout
        
        
        digester.addBeanPropertySetter(pstrPattern + "/action/**");
        
        /** Store the action name case insensitive. **/
		digester.addSetNext(pstrPattern + "/action", "putCASEINSENSITIVE");
    }
	     
	/**
	 * Parse the xml for categories of a PFCalendar.
	 * 
	 * @param pstrPattern The xpath pattern to access the starting node of a Categories.
	 */
	private void parseCategories(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, ArrayList.class);
		parseCategory(pstrPattern + "/category");
		digester.addSetNext(pstrPattern + "/category", "add");
	}     
	 	
	/**
	 * Parse the xml for PFCategory of a PFCalendar.
	 * 
	 * @param pstrPattern The xpath pattern to access the starting node of a PFLayoutCompent.
	 */
	private void parseCategory(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, PFCALENDARCATEGORY_CLAZZ);
	    
	    digester.addBeanPropertySetter(pstrPattern + "/name");
	    digester.addBeanPropertySetter(pstrPattern + "/expression");
	    digester.addBeanPropertySetter(pstrPattern + "/class", "clazz");          
	    digester.addBeanPropertySetter(pstrPattern + "/addparitytoclass");
	    digester.addBeanPropertySetter(pstrPattern + "/showwhenzero");
	    
	    parseUrl(pstrPattern + "/action/url");
	    digester.addSetNext(pstrPattern + "/action/url", "setUrl");
	    
		parseLabels(pstrPattern + "/action/label");
		digester.addSetNext(pstrPattern + "/action/label", "setLabel");
	}
		
	/**
	 * Parse the xml for subActions of a PFLayout.
	 * 
	 * @param pstrPattern The xpath pattern to access the starting node of a SubActions.
	 */
	private void parseLayoutComponents(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, ArrayList.class);
		parseLayoutComponent(pstrPattern + "/subaction");
		digester.addSetNext(pstrPattern + "/subaction", "add");
	}
		
	/**
	 * Parse the xml for PFLayoutCompent of a PFLayout.
	 * 
	 * @param pstrPattern The xpath pattern to access the starting node of a PFLayoutCompent.
	 */
	private void parseLayoutComponent(String pstrPattern) {
	    digester.addObjectCreate(pstrPattern, PFLAYOUTCOMPONENT_CLAZZ);
		
	    digester.addBeanPropertySetter(pstrPattern + "/name");
	    digester.addBeanPropertySetter(pstrPattern + "/active");
	    
		parseLabels(pstrPattern + "/title");
		digester.addSetNext(pstrPattern + "/title", "setTitle");
	
		parseComponent(pstrPattern + "/component");
		digester.addSetNext(pstrPattern + "/component", "setComponent");
	}
	
	/**
	 * Parse the xml for cells of a PFLayout.
	 * 
	 * @param pstrPattern The xpath pattern to access the starting node of a Cells.
	 */
	private void parseCells(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, ZXCollection.class);
		parsePFLayoutCell(pstrPattern + "/cell");
		digester.addSetNext(pstrPattern + "/cell", "put");
	}
	     
	/**
	 * Parse the xml for cell of a PFLayoutCell.
	 * 
	 * @param pstrPattern The xpath pattern to access the starting node of a PFLayoutCell.
	 */
	private void parsePFLayoutCell(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, PFLAYOUTCELL_CLAZZ);
		
		digester.addBeanPropertySetter(pstrPattern + "/align");
		digester.addBeanPropertySetter(pstrPattern + "/bgcolor");
		digester.addBeanPropertySetter(pstrPattern + "/border");
		digester.addBeanPropertySetter(pstrPattern + "/bordercolor");
		digester.addBeanPropertySetter(pstrPattern + "/clazz");
		digester.addBeanPropertySetter(pstrPattern + "/height");
		digester.addBeanPropertySetter(pstrPattern + "/name");
		digester.addBeanPropertySetter(pstrPattern + "/orientation");
		digester.addBeanPropertySetter(pstrPattern + "/style");
		digester.addBeanPropertySetter(pstrPattern + "/valign");
		digester.addBeanPropertySetter(pstrPattern + "/width");
		
		parseLabels(pstrPattern + "/title");
		digester.addSetNext(pstrPattern + "/title", "setTitle");
		
	}
	
	/**
	 * Parse the xml for TotalGroups.
	 * 
	 * @param pstrPattern The xpath pattern to access the starting node of TotalGroups.
	 * @param pstrTotalName The name of the type of totaler.
	 * @param penmRefType The ref type.
	 */
	private void parseTotalGroups(String pstrPattern, String pstrTotalName) {
	    digester.addObjectCreate(pstrPattern, ArrayList.class);
	    
		digester.addObjectCreate(pstrTotalName, Tuple.class);
	    digester.addBeanPropertySetter(pstrTotalName + "/entity", "name");
	    digester.addBeanPropertySetter(pstrTotalName + "/group", "value");
	    
        digester.addSetNext(pstrTotalName, "add");
	}
	
	/**
	 * Parse the xml for PFRefs.
	 * 
	 * @param pstrPattern The xpath pattern to access the starting node of a PFRef.
	 * @param penmRefType The ref type.
	 */
	private void parseRefs(String pstrPattern, zXType.pageflowRefType penmRefType) {
	    digester.addObjectCreate(pstrPattern, ArrayList.class);
	    
	    parseRef(pstrPattern + "/item", penmRefType);
	    
	    digester.addSetNext(pstrPattern + "/item", "add");
	}
	
	private void parseRef(String pstrPattern, zXType.pageflowRefType penmRefType) {
	    digester.addObjectCreate(pstrPattern, PFREF_CLAZZ);
	    
	    digester.addRule(pstrPattern, new PFRefRule(penmRefType));
	    
	    digester.addBeanPropertySetter(pstrPattern + "/name");
	    digester.addBeanPropertySetter(pstrPattern + "/startsubmenu");
	    digester.addBeanPropertySetter(pstrPattern + "/img");
	    digester.addBeanPropertySetter(pstrPattern + "/imgover");
	    digester.addBeanPropertySetter(pstrPattern + "/tabindex");
	    
	    parseUrl(pstrPattern + "/url");
	    digester.addSetNext(pstrPattern + "/url", "setUrl");
	    
	    parseLabels(pstrPattern + "/label");
	    digester.addSetNext(pstrPattern + "/label", "setLabel");
	    
	    parseLabels(pstrPattern + "/description");
	    digester.addSetNext(pstrPattern + "/description", "setDescription");
	    
	    parseLabels(pstrPattern + "/accesskey");
	    digester.addSetNext(pstrPattern + "/accesskey", "setAccesskey");
	    
	    parseLabels(pstrPattern + "/confirm");
	    digester.addSetNext(pstrPattern + "/confirm", "setConfirm");
	    
	    digester.addBeanPropertySetter(pstrPattern + "/align");		
	}
	
	/**
	 * @param pstrPattern
	 */
	private void parseComponent(String pstrPattern) {
	    digester.addObjectCreate(pstrPattern, PFCOMPONENT_CLAZZ);
	    digester.addBeanPropertySetter(pstrPattern + "/action");
	    digester.addBeanPropertySetter(pstrPattern + "/name");
	    parseDirectors(pstrPattern + "/querystring");
	    digester.addSetNext(pstrPattern +"/querystring", "setQuerystring");
	}
	     
	/**
	 * @param pstrPattern The xpath of the beginning of the PFDirect Node.
	 */
	private void parseDirectors(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, ArrayList.class);
		digester.addObjectCreate(pstrPattern + "/item", PFDIRECTOR_CLAZZ);
		digester.addSetNestedProperties(pstrPattern + "/item");
		digester.addSetNext(pstrPattern + "/item", "add");
	}
     
	/**
	 * Parse a collection of name/value pairs stored as a Tuple. Same as
	 * parseTags.
	 * 
	 * @param pstrPattern The xpath of the beginning node.
	 */
	private void parseSingleValues(String pstrPattern) {
		digester.addObjectCreate(pstrPattern, ZXCollection.class);
		digester.addObjectCreate(pstrPattern + "/*", Tuple.class);
		digester.addRule(pstrPattern + "/*", new SingleValuesRule());
		digester.addSetNext(pstrPattern + "/*", "put");
	}
     
	/**
	 * Parse name/value pairs.
	 * @param pstrPattern  The xpath of the beginning node.
	 */
	private void parseTags(String pstrPattern) {
	    digester.addObjectCreate(pstrPattern, ZXCollection.class);
	    digester.addObjectCreate(pstrPattern + "/*", Tuple.class);
	    digester.addRule(pstrPattern + "/*", new TagRule());
	    digester.addSetNext(pstrPattern + "/*", "put");
	}
	
	/**
	 * Same as parseTags.
	 * @param pstrPattern The XPATH url.
	 */
	private void parseEntityGroups(String pstrPattern) {
	    digester.addObjectCreate(pstrPattern, ArrayList.class);
	    
	    digester.addObjectCreate(pstrPattern + "/entitygroup", Tuple.class);
	    
	    digester.addBeanPropertySetter(pstrPattern + "/entitygroup/entity", "name");
	    digester.addBeanPropertySetter(pstrPattern + "/entitygroup/group", "value");
	    
	    digester.addSetNext(pstrPattern + "/entitygroup", "add");
	}
 
	/**
	 * @param pstrPattern The XPATH url.
	 */
	private void parseEditPages(String pstrPattern) {
	    digester.addObjectCreate(pstrPattern, ArrayList.class);
	    
	    digester.addObjectCreate(pstrPattern +  "/editpage", PFEDITPAGE_CLAZZ);
	    
	    parseLabels(pstrPattern + "/editpage/label");
	    digester.addSetNext(pstrPattern + "/editpage/label", "setLabel");
	    
	    parseEntityGroups(pstrPattern + "/editpage/entitygroups");
	    digester.addSetNext(pstrPattern + "/editpage/entitygroups", "setEntitygroups");
	    
	    digester.addSetNext(pstrPattern + "/editpage", "add");
	}
     
	/**
	 * @param pstrPattern The XPATH url.
	 */
	private void parseIncludes(String pstrPattern) {
	    digester.addObjectCreate(pstrPattern, ArrayList.class);
	    digester.addFactoryCreate(pstrPattern + "/include", new PFIncludeFactory(), true);
	    digester.addBeanPropertySetter(pstrPattern + "/include/path");
	    digester.addSetNext(pstrPattern + "/include", "add");
	}
 
	/**
	 * @param pstrPattern The XPATH url.
	 */
	private void parseLabels(String pstrPattern) {
	    digester.addObjectCreate(pstrPattern, LABELCOL_CLAZZ);
	    digester.addObjectCreate(pstrPattern + "/*", LABEL_CLAZZ);
	    digester.addRule(pstrPattern + "/*", new LabelRule());
	    digester.addSetNext(pstrPattern + "/*", "put");
	}
	
	/**
	 * Parse a PFUrl.
	 * 
	 * @param pstrPattern The XPATH url.
	 */
	private void parseUrl(String pstrPattern) {
	    digester.addObjectCreate(pstrPattern, PFURL_CLAZZ);
	    
	    digester.addBeanPropertySetter(pstrPattern +"/urltype");
	    digester.addBeanPropertySetter(pstrPattern +"/url");
	    digester.addBeanPropertySetter(pstrPattern +"/urlclose");
	    digester.addBeanPropertySetter(pstrPattern +"/frameno");
	    digester.addBeanPropertySetter(pstrPattern +"/active");
	    
	    /**
	     * Do not parse the comments when necessary : 
	     */
	    if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
	        digester.addBeanPropertySetter(pstrPattern +"/comment");
	    }
	    
	    parseDirectors(pstrPattern + "/querystring");
	    digester.addSetNext(pstrPattern +"/querystring", "setQuerystring");
	}
     
	/**
	 * Parse TreeLevels XML.
	 * 
	 * @param pstrPattern The XPATH url.
	 */
	private void parseTreeLevels(String pstrPattern) {
	    digester.addObjectCreate(pstrPattern, ArrayList.class);
	    digester.addObjectCreate(pstrPattern + "/treelevel", PFTreeLevel.class);
	    parseUrl(pstrPattern + "/treelevel/url");
	    digester.addSetNext(pstrPattern + "/treelevel/url", "setUrl");
	    digester.addBeanPropertySetter(pstrPattern + "/treelevel/class", "class");
	    digester.addBeanPropertySetter(pstrPattern + "/treelevel/addparitytoclass");
	    digester.addSetNext(pstrPattern + "/treelevel", "add");
	}
	
	/**
	 * Parse parameter-bags.
	 * 
	 * @param pstrPattern The xml pattern
	 * @param pstrParameterType The parameterbag type.
	 */
	private void parseParameterBags(String pstrPattern, String pstrParameterType) {
		boolean blnSender = pstrParameterType.equalsIgnoreCase("sendparameterbag");
		String strPattern = pstrPattern + "/" + pstrParameterType;
		
		digester.addObjectCreate(pstrPattern, ZXCollection.class);
		
		digester.addObjectCreate(strPattern, blnSender?SendParameterBag.class:ReceiveParameterBag.class);
		
		digester.addBeanPropertySetter(strPattern + "/name");
		digester.addBeanPropertySetter(strPattern + "/comment");
		
		/**
		 * Parse Sender parameter-bag related stuff.
		 */
		if (blnSender) {
			digester.addBeanPropertySetter(strPattern + "/callpageflow");
			digester.addBeanPropertySetter(strPattern + "/parameterebagdefinition");
			
			/**
			 * Parser send PF bo contexts.
			 */
			parsePFBOContexts(strPattern + "/bocontexts");
			digester.addSetNext(strPattern + "/bocontexts", "setBocontexts");
		}
		
		/**
		 * Parse Parameters
		 */
		parseParameterBagEntries(strPattern + "/parameters", blnSender);
		digester.addSetNext(strPattern + "/parameters", "setParameters");
		
		digester.addSetNext(strPattern, "putCASEINSENSITIVE");
	}
	
	/**
	 * Parse parameter-bag entries.
	 * 
	 * @param pstrPattern The xml pattern
	 * @param pblnSender The parameterbag type.
	 */
	private void parseParameterBagEntries(String pstrPattern, boolean pblnSender) {
		digester.addObjectCreate(pstrPattern, ZXCollection.class);
		
		digester.addObjectCreate(pstrPattern + "/parameter", pblnSender?SendParameterBagEntry.class:ReceiveParameterBagEntry.class);
		
		digester.addBeanPropertySetter(pstrPattern + "/parameter/name");
		digester.addBeanPropertySetter(pstrPattern + "/parameter/comment");
		digester.addBeanPropertySetter(pstrPattern + "/parameter/entrytype");
		
		/**
		 * Parse receiver parmaeter-bag related stuff.
		 */
		if (!pblnSender) {
			digester.addBeanPropertySetter(pstrPattern + "/parameter/mandatory");
			digester.addBeanPropertySetter(pstrPattern + "/parameter/description");
		}
		
		/**
		 * Parse parameter values
		 */
		parseParameterBagEntriesValues(pstrPattern + "/parameter/value");
		
		digester.addSetNext(pstrPattern + "/parameter", "putCASEINSENSITIVE");
	}
	
	/**
	 * Parse parameter-bag entry value.
	 * 
	 * @param pstrPattern The xml pattern
	 */
	private void parseParameterBagEntriesValues(String pstrPattern) {
		/**
		 * Each value datatype is stored in its own element :
		 * eg : PFComponent - component/PFRef - ref etc..
		 */
		
		// String value
		digester.addBeanPropertySetter(pstrPattern + "/string", "value");
		
		// PFComponent value
		parseComponent(pstrPattern + "/component");
		digester.addSetNext(pstrPattern + "/component", "setValue");
		
		// Entities value
		parseEntities(pstrPattern + "/entities");
		digester.addSetNext(pstrPattern + "/entities", "setValue");
		
		// Entity value
		parseEntity(pstrPattern + "/entity/item");
		digester.addSetNext(pstrPattern + "/entity/item", "setValue");
		
		// Label value
		parseLabels(pstrPattern + "/label");
		digester.addSetNext(pstrPattern + "/label", "setValue");
		
		// PFRef value
		parseRef(pstrPattern + "/ref/item", zXType.pageflowRefType.prtRef);
		digester.addSetNext(pstrPattern + "/ref/item", "setValue");
		
		// PFUrl value
		parseUrl(pstrPattern + "/url");
		digester.addSetNext(pstrPattern + "/url", "setValue");
	}
	
	//------------------------ Inner classes used for parsing the config
    
    /**
     * A rule for setting tuples from page.
     * 
     * <pre> 
     * So the element name is the key or name of the tuple, and the value of the node it is the value of the tuple.
     *  
     * EG :
     *  &lt;tuple&gt;
     *  		&lt;name&gt;value&lt;/name&gt;
     *  &lt;/tuple&gt;
     *  
     * </pre>
     */
    public class TagRule extends Rule {
		/** Default constructor */
		public TagRule() { super(); }
		
		/**
		 * @see org.apache.commons.digester.Rule#body(java.lang.String,
		 *      java.lang.String, java.lang.String)
		 */
		public void body(String pstrNamespace, String pstrName, String pstrText) throws Exception {
		    Tuple tuple = (Tuple)this.digester.peek();
		    tuple.setName(pstrName);
		    tuple.setValue(pstrText);
		}
    }
    
    /**
     * A rule for PFRef
     */
    public class PFRefRule extends Rule {
		private zXType.pageflowRefType refType;
		
		/** 
		 * Default constructor 
		 * @param penmRefType The ref type.
		 */
		public PFRefRule(zXType.pageflowRefType penmRefType) { 
		    super(); 
		    this.refType = penmRefType;
		}
		
		/**
		 * @see org.apache.commons.digester.Rule#body(java.lang.String,
		 *      java.lang.String, java.lang.String)
		 */
		public void body(String pstrNamespace, String pstrName, String pstrText) throws Exception {
		    PFRef objRef = (PFRef)this.digester.peek();
		    objRef.setRefType(refType);
		}
    }
    
    /**
     * A rule for setting tuples from page.
     * 
     * <pre> 
     * 
     *  
     * EG :
     *  &lt;tuple&gt;
     *  		&lt;value&gt;value&lt;/name&gt;
     *  &lt;/tuple&gt;
     *  
     * </pre>
     */
    public class SingleValuesRule extends Rule {
        /** Default constructor */
        public SingleValuesRule() { super(); }
        
        /**
         * @see org.apache.commons.digester.Rule#body(java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        public void body(String pstrNamespace, String pstrName, String pstrText) throws Exception {
            Tuple tuple = (Tuple)this.digester.peek();
            tuple.setName(pstrText);
            tuple.setValue(pstrText);
        }
    }
    
    /**
     * A factory of creating PFObjects suitable for the Digester.
     */
    public class PFActionFactory extends AbstractObjectCreationFactory {
        
        private Pageflow objPageflow;
        private PFDescriptor objPFDesc;
        
        /** Hide the default constructor **/
        private PFActionFactory() {
        	super();
        }
        
        /**
         * @param pobjPageflow A handle to the Pageflow object
         * @param pobjPFDesc  A handle to the Pageflow descriptor
         */
        public PFActionFactory(Pageflow pobjPageflow, PFDescriptor pobjPFDesc) {
            this.objPageflow = pobjPageflow;
            this.objPFDesc = pobjPFDesc;
        }
        
        /**
         * @see org.apache.commons.digester.ObjectCreationFactory#createObject(org.xml.sax.Attributes)
         */
        public Object createObject(Attributes pobjAattributes){
            PFAction  createObject = null;
            try {
                /**
                 * Try to get the pageflow type : 
                 */
                String strType = pobjAattributes.getValue("type").toLowerCase();
                if (strType.charAt(0) == 'z') {
                    strType = strType.substring(1);
                }
                zXType.pageflowActionType type = zXType.pageflowActionType.getEnum(strType);
                
                Class clazz = PFSEARCHFORM_CLAZZ;
                if (type.equals(zXType.pageflowActionType.patCreateUpdate)) {
                    clazz = PFCREATEUPDATE_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patDBAction)) {
                    clazz = PFDBACTION_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patEditForm)) {
                    clazz = PFEDITFORM_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patGridCreateUpdate)) {
                    clazz = PFGRIDCREATEUPDATE_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patGridEditForm)) {
                    clazz = PFGRIDEDITFORM_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patListForm)) {
                    clazz = PFLISTFORM_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patLoopOver)) {
                    clazz = PFLOOPOVER_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patMatrixCreateUpdate)) {
                    clazz = PFMATRIXCREATEUPDATE_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patMatrixEditForm)) {
                    clazz = PFMATRIXEDITFORM_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patNull)) {
                    clazz = PFNULL_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patQuery)) {
                    clazz = PFQUERY_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patRecTree)) {
                    clazz = PFRECTREE_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patRefine)) {
                    clazz = PFREFINE_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patSearchForm)) {
                    clazz = PFSEARCHFORM_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patStdPopup)) {
                    clazz = PFSTDPOPUP_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patTreeForm)) {
                    clazz = PFTREEFORM_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patCalendar)) {
                    clazz = PFCALENDAR_CLAZZ;
                } else if (type.equals(zXType.pageflowActionType.patLayout)) {
                    clazz = PFLAYOUT_CLAZZ;
                }
                
                createObject = (PFAction)getZx().createObject(clazz);
                createObject.init(this.objPageflow, this.objPFDesc);
                createObject.setType(strType);
                
            } catch (Exception e) {
            	/**
            	 * Only trace the exception
            	 */
            	getZx().trace.addError("Failed during parsing", pobjAattributes.toString(), e);
            	
            }
            return createObject;
        }
    }
    
    /**
     * A factory of creating PFIncludes suitable for the Digester.
     */
    public class PFIncludeFactory extends AbstractObjectCreationFactory {
        /** 
         * Default constructor
         */
        public PFIncludeFactory() {
        	super();
        }
        
        /**
         * @see org.apache.commons.digester.ObjectCreationFactory#createObject(org.xml.sax.Attributes)
         */
        public Object createObject(Attributes pobjAattributes){
            PFInclude  createObject = new PFInclude();
            createObject.setIncludeType(pobjAattributes.getValue("type"));
            return createObject;
        }
    }
    
    //------------------------ Dump the pageflow to xml used by repository editor.
    
    /**
     * Dump me in XML format.
     * 
     * <pre>
     * 
     *  
     * This can be used in various places : 
     * 1) In the repostory editor.
     * 2) To cache pageflow informations.
     * 3) Can used to debug whether the xml file was initials parsed correctly.
     *  
     *  V1.5:32 - Reviewed
     *  V1.5:43 - Reviewed
     *  V1.5:65 - Reviewed
     *  V1.5:75 - Reviewed
     *  V1.5:77 - Reviewed
     *  
     * </pre>
     * 
     * @return Returns a page dump of the pageflow descriptor.
     * @throws NestableRuntimeException Thrown if dumpAsXml fails.
     */
    public String dumpAsXml() {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        String dumpAsXml = null;
        
        try {
            /**
             * Initialise XML generator class
             */
            this.objXMLGen = new XMLGen(1000);  
            this.objXMLGen.xmlHeader();
            
            this.objXMLGen.openTag("pageflow");
            this.objXMLGen.taggedValue("name", getName());
            this.objXMLGen.taggedValue("helpid", getHelpid());
            xmlLabel("title", getTitle());
            this.objXMLGen.taggedValue("htmldebug", zXType.valueOf(getDebugMode()));
            objXMLGen.taggedValue("startaction", getStartaction());
            objXMLGen.taggedValue("bodescriptor", getBodescriptor());
            objXMLGen.taggedValue("contextid", getContextid());
            objXMLGen.taggedValue("propagateqs", isPropagaTeqs());
            objXMLGen.taggedCData("version", getVersion());
            objXMLGen.taggedCData("lastchange", getLastchange());
            objXMLGen.taggedCData("comment", getComment());
            xmlUrl("baseurl", getBaseurl());
            xmlDirectors("contextupdate", getContextupdate());
            xmlSecurity("security", getSecurity());
            xmlIncludes("includes", getIncludes());
            
            objXMLGen.openTag("actions");
            if (getActions() != null && getActions().size() >0) {
                PFAction objAction;
                Iterator iter = getActions().iterator();
                while (iter.hasNext()) {
                    objAction = (PFAction)iter.next();
                    
                    objXMLGen.openTagWithAttr("action", "type", zXType.valueOf(objAction.getActionType()));
                    objAction.dumpAsXML();
                    this.objXMLGen.closeTag("action");
            	}
            }
            objXMLGen.closeTag("actions");
            
            /**
             * Dump parameter bags
             */
            if (getSendParameterBags() != null && getSendParameterBags().size() > 0) {
	            objXMLGen.openTag("sendparameterbags");
	            
	            SendParameterBag objSendParameterBag;
	            Iterator iter = getSendParameterBags().values().iterator();
	            while (iter.hasNext()) {
	            	objSendParameterBag = (SendParameterBag)iter.next();
	            	xmlParameterBag("sendparameterbag", objSendParameterBag);
	            }
	            
	            objXMLGen.closeTag("sendparameterbags");
            }
            
            objXMLGen.closeTag("pageflow");
            
            /**
             * And return the result of all this hard work....
             */
            dumpAsXml = objXMLGen.getXML();
            return dumpAsXml;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Dump me in XML format.", e);
            
            if (getZx().throwException) throw new NestableRuntimeException(e);
            return dumpAsXml;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(dumpAsXml);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add a label to the XML. This will update the PFDescriptor member variable
     * XMLGen.
     *
     * @param pstrName The name of the xml element which contain the label data.
     * @param pcolLabel The label collection to dump to xml.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if xmlLabel fails.
     */
    protected zXType.rc xmlLabel(String pstrName, LabelCollection pcolLabel) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pcolLabel", pcolLabel);
        }

        zXType.rc xmlLabel = zXType.rc.rcOK; 
        
        try {
            /**
             * Short circuit if the label is empty :
             */
            if (pcolLabel == null || pcolLabel.size() == 0) { return xmlLabel; }
            
            this.objXMLGen.openTag(pstrName);
            Label objLabel;
            Iterator iter = pcolLabel.iterator();
            while (iter.hasNext()){
                objLabel = (Label)iter.next();
                this.objXMLGen.taggedCData(objLabel.getLanguage(), objLabel.getLabel());
            }
            this.objXMLGen.closeTag(pstrName);
            
            return xmlLabel;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlLabel);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add an URL to the XML.
     *
     * @param pstrName The name of the xml element that will contain the PFURL xml.
     * @param pobjURL The PFURL to dump to URL.
     * @return Returns the return code of the method.
     */
    protected zXType.rc xmlUrl(String pstrName, PFUrl pobjURL) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pobjURL", pobjURL);
        }

        zXType.rc xmlUrl = zXType.rc.rcOK; 
        
        try {
            /**
             * Short circuit if the PFURL is null.
             */
            if (pobjURL == null) { return xmlUrl; }
            
            this.objXMLGen.openTag(pstrName);
            
            this.objXMLGen.taggedCData("url", pobjURL.getUrl());
        	this.objXMLGen.taggedValue("urltype", zXType.valueOf(pobjURL.getUrlType()));
            this.objXMLGen.taggedCData("urlclose", pobjURL.getUrlclose());
            this.objXMLGen.taggedValue("frameno", pobjURL.getFrameno());
            
            /** DGS13FEB2003: added active property **/
            this.objXMLGen.taggedValue("active", pobjURL.getActive());
            
            if (pobjURL.getQuerystring() != null && pobjURL.getQuerystring().size() > 0) {
                xmlDirectors("querystring", pobjURL.getQuerystring());
            }
            
            this.objXMLGen.taggedCData("comment", pobjURL.getComment());
            
            this.objXMLGen.closeTag(pstrName);
            
            return xmlUrl;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlUrl);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add directors to XML.
     *
     * @param pstrName The name of the xml attribute containing the directors.
     * @param pcolDirectors The collections of ZX Directors
     * @return Returns the return code of the method.
     */
    protected zXType.rc xmlDirectors(String pstrName, List pcolDirectors) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pcolDirectors", pcolDirectors);
        }

        zXType.rc xmlDirectors = zXType.rc.rcOK; 
        
        try {
            /**
             * Short circuit if pcolDirectors is null.
             */
            if (pcolDirectors == null || pcolDirectors.size() == 0) { return xmlDirectors; }
            
            this.objXMLGen.openTag(pstrName);
            
            PFDirector objDirector;
            int intDirectors = pcolDirectors.size();
            for (int i = 0; i < intDirectors; i++) {
                objDirector = (PFDirector)pcolDirectors.get(i);
                
                this.objXMLGen.openTag("item");
                this.objXMLGen.taggedCData("source", objDirector.getSource(), true);
                this.objXMLGen.taggedCData("destination", objDirector.getDestination(), true);
                this.objXMLGen.closeTag("item");
            }
            
            this.objXMLGen.closeTag(pstrName);

            return xmlDirectors;
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlDirectors);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add entity massager object to XML.
     *
     * @param pstrName The name of the attribute that contains the entity messagers
     * @param pobjEntityMassager The entity massager to get the xml dump of.
     * @return Returns the return code of the method.
     */
    protected zXType.rc xmlEntityMassager(String pstrName, PFEntityMassager pobjEntityMassager) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pobjEntityMassager", pobjEntityMassager);
        }

        zXType.rc xmlEntityMassager = zXType.rc.rcOK; 
        
        try {
            /**
             * Short circuit if pobjEntityMassager is null.
             */
        	if (pobjEntityMassager == null) { return xmlEntityMassager; }
            
            this.objXMLGen.openTag(pstrName);
            
            this.objXMLGen.taggedValue("attr", pobjEntityMassager.getAttr());
            this.objXMLGen.taggedValue("property", pobjEntityMassager.getProperty());
            this.objXMLGen.taggedValue("value", pobjEntityMassager.getValue());
            
            this.objXMLGen.closeTag(pstrName);
            
            return xmlEntityMassager;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlEntityMassager);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add an tree level to the XML.
     *
     * @param pstrName The name of the xml element containing the treelevel.
     * @param pobjTreeLevel The pageflow treelevel object.
     * @return Returns the return code of the method.
     */
    protected zXType.rc xmlTreeLevel(String pstrName, PFTreeLevel pobjTreeLevel) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pobjTreeLevel", pobjTreeLevel);
        }

        zXType.rc xmlTreeLevel = zXType.rc.rcOK; 
        
        try {
        	/**
        	 * Short circuit if pobjTreeLevel is null.
        	 */
            if (pobjTreeLevel == null) { return xmlTreeLevel; }
            
            this.objXMLGen.openTag(pstrName);
            
            xmlUrl("url", pobjTreeLevel.getUrl());
            this.objXMLGen.taggedCData("class", pobjTreeLevel.getClazz());
            this.objXMLGen.taggedValue("addparitytoclass", String.valueOf(pobjTreeLevel.isAddparitytoclass())); // or zYes and zNo
            
            this.objXMLGen.closeTag(pstrName);
            
            return xmlTreeLevel;
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlTreeLevel);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add collection of security settings to XML.
     *
     * @param pstrName The name of the xml element that contains the security
     *            settings.
     * @param pcolSecurities A collection of security settings
     * @return Returns the return code of the method.
     */
    protected zXType.rc xmlSecurity(String pstrName, ZXCollection pcolSecurities) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pcolSecurities", pcolSecurities);
        }
        
        zXType.rc xmlSecurity = zXType.rc.rcOK; 

        try {
        	/**
        	 * Short circuit if pcolSecurities is null or empty
        	 */
            if (pcolSecurities == null || pcolSecurities.size() == 0) { return xmlSecurity; }
            
            this.objXMLGen.openTag(pstrName);
            
            Tuple objTuple;
            
            Iterator iter = pcolSecurities.iterator();
            while (iter.hasNext()) {
                objTuple = (Tuple)iter.next();
                this.objXMLGen.taggedValue("group", objTuple.getValue());
            }
            
            this.objXMLGen.closeTag(pstrName);
            
            return xmlSecurity;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlSecurity);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add ref collection to XML.
     *
     * @param pstrName The name of the xml element containing the PF Ref object.
     * @param pobjRef The pf ref object to get a xml dump of.
     * @return Returns the return code of the method.
     */
    protected zXType.rc xmlRef(String pstrName, PFRef pobjRef) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pobjRef", pobjRef);
        }

        zXType.rc xmlRef = zXType.rc.rcOK; 
        
        try {
        	/**
        	 * Short circuit if pobjRef is null
        	 */
            if (pobjRef == null) { return xmlRef; }
            
            this.objXMLGen.openTag("item");
            
            this.objXMLGen.taggedValue("name", pobjRef.getName());
            /** DGS28FEB2003: Popup Refs have start sub menu y/n **/
            this.objXMLGen.taggedValue("startsubmenu", (pobjRef.isStartsubmenu() ? "true" : "false")); 
            
            /**
             * DGS14FEB2003: Refs can have image URL (and optional 'over' image)
             * to generate image not button:
             */
            this.objXMLGen.taggedValue("img", pobjRef.getImg());
            this.objXMLGen.taggedValue("imgover", pobjRef.getImgover());
            this.objXMLGen.taggedValue("tabindex", pobjRef.getTabindex());
            xmlUrl("url", pobjRef.getUrl());
            xmlLabel("label", pobjRef.getLabel());
            xmlLabel("confirm", pobjRef.getConfirm());
            xmlLabel("description", pobjRef.getDescription());
            xmlLabel("accesskey", pobjRef.getAccesskey());
            
            this.objXMLGen.taggedValue("align", zXType.valueOf(pobjRef.getEnmAlign()));
            
            this.objXMLGen.closeTag("item");
            
            return xmlRef;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlRef);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add CUActions to XML
     *
     * @param pcolCUActions 
     * @return Returns the return code of the method.
     */
    protected zXType.rc xmlCuActions(List pcolCUActions) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pcolCUActions", pcolCUActions);
        }

        zXType.rc xmlCuActions = zXType.rc.rcOK; 
        
        try {
        	/**
        	 * Short circuit if pcolCUActions is null
        	 */
            if (pcolCUActions == null) return xmlCuActions; 
            int intCUActions = pcolCUActions.size();
            if (intCUActions == 0) return xmlCuActions;
            
            this.objXMLGen.openTag("cuactions");
            
            PFCuAction objCUAction;
            
            for (int i = 0; i < intCUActions; i++) {
            	objCUAction = (PFCuAction)pcolCUActions.get(i);
            	
                this.objXMLGen.openTag("cuaction");
            	
            	this.objXMLGen.taggedValue("name", objCUAction.getName(), true);
            	this.objXMLGen.taggedValue("active", objCUAction.getActive());
            	
            	xmlLabel("msg", objCUAction.getMsg());
            	
            	this.objXMLGen.taggedValue("focusattr", objCUAction.getFocusattr());
            	this.objXMLGen.taggedValue("action", objCUAction.getAction());
            	this.objXMLGen.taggedValue("comment", objCUAction.getComment());
            	
                this.objXMLGen.closeTag("cuaction");
			}
            
            this.objXMLGen.closeTag("cuactions");
            
            return xmlCuActions;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlCuActions);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add query expression to XML. Can be recursive.
     *
     * @param pstrName The name of the xml element containing the query expression.
     * @param pobjQryExpr The QryExpr to dump to xml.
     * @return Returns the return code of the method.
     */
    protected zXType.rc xmlQueryExpr(String pstrName, PFQryExpr pobjQryExpr) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pobjQryExpr", pobjQryExpr);
        }

        zXType.rc xmlQueryExpr = zXType.rc.rcOK;
        
        try {
            /**
             * Short circuit if pobjQryExpr is null.
             */
            if (pobjQryExpr == null) { return xmlQueryExpr; }
            
            /**
             * Don't generate anything if not recursive and nothing in either
             * side - otherwise get pointless tag with just the operator in,
             * which means nothing.
             */
            if (!pobjQryExpr.operatorIsRecursive()) {
                if (StringUtil.len(pobjQryExpr.getLhs()) == 0 && StringUtil.len(pobjQryExpr.getRhs()) == 0) {
                    return xmlQueryExpr;
                }
            }
            
            this.objXMLGen.openTag(pstrName);
            
            this.objXMLGen.taggedValue("operator", zXType.valueOf(pobjQryExpr.getEnmoperator()));
            this.objXMLGen.taggedValue("lhs", pobjQryExpr.getLhs());
            this.objXMLGen.taggedValue("rhs", pobjQryExpr.getRhs());
            this.objXMLGen.taggedValue("attrlhs", pobjQryExpr.getAttrlhs());
            this.objXMLGen.taggedValue("attrrhs", pobjQryExpr.getAttrrhs());
            
            if (pobjQryExpr.operatorIsRecursive()) {
                xmlQueryExpr("queryexprlhs", pobjQryExpr.getQueryexprlhs());
                xmlQueryExpr("queryexprrhs", pobjQryExpr.getQueryexprrhs());
            }
            
            this.objXMLGen.closeTag(pstrName);
            
            return xmlQueryExpr;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlQueryExpr);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add directors to XML.
     *
     * @param pstrName Name of xml element containing the includes
     * @param pcolIncludes The includes collection
     * @return Returns the return code of the method.
     */
    protected zXType.rc xmlIncludes(String pstrName, ArrayList pcolIncludes) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pcolIncludes", pcolIncludes);
        }

        zXType.rc xmlIncludes = zXType.rc.rcOK; 
        
        try {
            /**
             * Short circuit if pcolIncludes is null.
             */
            if (pcolIncludes == null) { 
            	return xmlIncludes; 
            }
            
            this.objXMLGen.openTag(pstrName);
            
            PFInclude objInclude;
            int intIncludes = pcolIncludes.size();
            for (int i = 0; i < intIncludes; i++) {
                objInclude = (PFInclude)pcolIncludes.get(i);
                
                this.objXMLGen.openTagWithAttr("include", "type", objInclude.getIncludeType());
                this.objXMLGen.taggedCData("path", objInclude.getPath(), true);
                this.objXMLGen.closeTag("include");
            }
            
            this.objXMLGen.closeTag(pstrName);
            
            return xmlIncludes;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlIncludes);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add component  to XML.
     *
     * @param pstrName The name of the xml element containing the component
     * @param pobjComponent The PFComponent to dump to xml
     * @return Returns the return code of the method.
     */
    protected zXType.rc xmlComponent(String pstrName, PFComponent pobjComponent) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pobjComponent", pobjComponent);
        }

        zXType.rc xmlComponent = zXType.rc.rcOK; 
        
        try {
            /**
             * Short circuit if pobjComponent is null.
             */
            if (pobjComponent == null) { return xmlComponent; }
            
            this.objXMLGen.openTag(pstrName);
            
            this.objXMLGen.taggedCData("action", pobjComponent.getAction());
            xmlDirectors("querystring", pobjComponent.getQuerystring());
            
            this.objXMLGen.closeTag(pstrName);
            
            return xmlComponent;
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(xmlComponent);
                getZx().trace.exitMethod();
            }
        }
    }
        
    /**
     * @param pobjEntity The PFEntity to dump.
     * @return Reutrn code of the method.
     */
    public zXType.rc xmlEntity(PFEntity pobjEntity) {
    	zXType.rc xmlEntities = zXType.rc.rcOK;
    	
        this.objXMLGen.openTag("item");
        
        this.objXMLGen.taggedValue("name", pobjEntity.getName());
        this.objXMLGen.taggedCData("entity", pobjEntity.getEntity());
        this.objXMLGen.taggedValue("refboaction", pobjEntity.getRefboaction());
        this.objXMLGen.taggedValue("pk", pobjEntity.getPk());
        this.objXMLGen.taggedValue("alias", pobjEntity.getAlias());
        this.objXMLGen.taggedValue("wheregroup", pobjEntity.getWheregroup());
        this.objXMLGen.taggedCData("selecteditgroup", pobjEntity.getSelecteditgroup());
        this.objXMLGen.taggedValue("selectlistgroup", pobjEntity.getSelectlistgroup());
        this.objXMLGen.taggedValue("groupbygroup", pobjEntity.getGroupbygroup());
        this.objXMLGen.taggedValue("lockgroup", pobjEntity.getLockgroup());
        this.objXMLGen.taggedValue("visiblegroup", pobjEntity.getVisiblegroup());
        this.objXMLGen.taggedCData("pkwheregroup", pobjEntity.getPkwheregroup());
        this.objXMLGen.taggedCData("listgroup", pobjEntity.getListgroup());
        this.objXMLGen.taggedValue("copygroup", pobjEntity.getCopygroup());
        
        // New way of handling booleans :
        this.objXMLGen.taggedValue("resolvefk", StringUtil.valueOf(pobjEntity.isResolveFK()));
        this.objXMLGen.taggedValue("allownew", StringUtil.valueOf(pobjEntity.isAllowNew()));
        
        xmlDirectors("attrvalues", pobjEntity.getAttrvalues());
        
        /**
         * DGS14APR2003: Added Entity Massagers
         */
        PFEntityMassager objEntityMassager;
        if (pobjEntity.getEntitymassagers() != null && pobjEntity.getEntitymassagers().size() > 0) {
            objXMLGen.openTag("entitymassagers");
            int intEntitymassgers = pobjEntity.getEntitymassagers().size();
            for (int i = 0; i < intEntitymassgers; i++) {
                objEntityMassager = (PFEntityMassager)pobjEntity.getEntitymassagers().get(i);
                xmlEntityMassager("entitymassager", objEntityMassager);
			}
            objXMLGen.closeTag("entitymassagers");
        }
        
        this.objXMLGen.closeTag("item");
        
        return xmlEntities;
    }
    
    /**
     * @param pobjEntry The bo context entry to dump
     * @return Returns the return code of the methods
     */
    public zXType.rc xmlBoContextEntry(PFBOContext pobjEntry) {
    	zXType.rc xmlBoContextEntry = zXType.rc.rcOK;
    	
        this.objXMLGen.openTag("bocontext");
        
        this.objXMLGen.taggedValue("name", pobjEntry.getName());
        this.objXMLGen.taggedCData("entity", pobjEntry.getEntity());
        this.objXMLGen.taggedValue("identification", pobjEntry.getIdentification());
        this.objXMLGen.taggedValue("active", pobjEntry.getActive());
        this.objXMLGen.taggedValue("alias", pobjEntry.getAlias());
        this.objXMLGen.taggedValue("loadgroup", pobjEntry.getLoadgroup());
        
        // New way of handling booleans :
        this.objXMLGen.taggedValue("resolvefk", StringUtil.valueOf(pobjEntry.isResolveFK()));

        this.objXMLGen.taggedValue("identificationmethod", zXType.valueOf(pobjEntry.getIdentificationMethod()));
        this.objXMLGen.taggedValue("comment", pobjEntry.getComment());
        
        xmlDirectors("attrvalues", pobjEntry.getAttrvalues());
        
        this.objXMLGen.taggedCData("comment", pobjEntry.getComment());
        
        this.objXMLGen.closeTag("bocontext");
        
    	return xmlBoContextEntry;
    }
    
    /**
     * Add parameter-bag to XML.
     * 
     * @param pstrName The name of the xml element containing the parameter bag
     * @param pobjReceiveParameterBag The parameter bag to dump
     * @return Returns the return code of the method
     */
    private zXType.rc xmlParameterBag(String pstrName, ReceiveParameterBag pobjReceiveParameterBag) {
    	zXType.rc xmlParameterBag = zXType.rc.rcOK;
    	
    	this.objXMLGen.openTag(pstrName);
    	
    	this.objXMLGen.taggedValue("name", pobjReceiveParameterBag.getName());
    	this.objXMLGen.taggedValue("comment", pobjReceiveParameterBag.getComment());
    	
    	/**
    	 * Dump Send Parameter specific stuff.
    	 */
    	if (pobjReceiveParameterBag instanceof SendParameterBag) {
    		SendParameterBag objSendParameterBag = (SendParameterBag)pobjReceiveParameterBag;
    		
    		this.objXMLGen.taggedValue("callpageflow", objSendParameterBag.getCallpageflow());
    		this.objXMLGen.taggedValue("parameterebagdefinition", objSendParameterBag.getParameterbagdefinition());
    		
    		/**
    		 * Dump the BO Context entries :
    		 */
    		if (objSendParameterBag.getBocontexts() != null) {
    			int intBOContexts = objSendParameterBag.getBocontexts().size();
    			if (intBOContexts > 0) {
        			this.objXMLGen.openTag("bocontexts");
        			PFBOContext objPFBOContext;
        			for (int i = 0; i < intBOContexts; i++) {
        				objPFBOContext = (PFBOContext)objSendParameterBag.getBocontexts().get(i);
        				xmlBoContextEntry(objPFBOContext);
    				}
        			this.objXMLGen.closeTag("bocontexts");
    			}
    		}
    	}
    	
    	if (pobjReceiveParameterBag.getParameters() != null && pobjReceiveParameterBag.getParameters().size() > 0) {
	    	this.objXMLGen.openTag("parameters");
	    	SendParameterBagEntry objSendParameterBagEntry;
	    	
	    	Iterator iter = pobjReceiveParameterBag.getParameters().values().iterator();
	    	while (iter.hasNext()) {
	    		objSendParameterBagEntry = (SendParameterBagEntry)iter.next();
	    		xmlParameterBagEntry(objSendParameterBagEntry);
	    	}
	    	
	    	this.objXMLGen.closeTag("parameters");
    	}
    	
    	this.objXMLGen.closeTag(pstrName);
    	
    	return xmlParameterBag;
    }
    
    /**
     * Add parameter-bag entry to XML.
     * 
     * @param pobjSendParameterBagEntry
     * @return Returns the return code of the method.
     */
    private zXType.rc xmlParameterBagEntry(SendParameterBagEntry pobjSendParameterBagEntry) {
    	zXType.rc xmlParameterBagEntry = zXType.rc.rcOK;
    	
    	this.objXMLGen.openTag("parameter");
    	
    	this.objXMLGen.taggedValue("name", pobjSendParameterBagEntry.getName());
    	this.objXMLGen.taggedValue("comment", pobjSendParameterBagEntry.getComment());
    	this.objXMLGen.taggedValue("entrytype", zXType.valueOf(pobjSendParameterBagEntry.getEntryType()));
    	
    	/**
    	 * Dump Receive Parameter bag entry
    	 */
    	if (pobjSendParameterBagEntry instanceof ReceiveParameterBagEntry) {
    		ReceiveParameterBagEntry objReceiveParameterBagEntry = (ReceiveParameterBagEntry)pobjSendParameterBagEntry;
    		this.objXMLGen.taggedValue("description", objReceiveParameterBagEntry.getDescription());
    		this.objXMLGen.taggedValue("mandatory", objReceiveParameterBagEntry.isMandatory());
    	}
    	
    	/**
    	 * Dump the parameter value :
    	 */
        int intEntryType = pobjSendParameterBagEntry.getEntryType().pos;
        switch (intEntryType) {
		case 0: // ppbetUrl
			this.objXMLGen.openTag("value");
			xmlUrl("url", (PFUrl)pobjSendParameterBagEntry.getValue());
			this.objXMLGen.closeTag("value");
			break;
			
		case 1: // ppbetRef
			this.objXMLGen.openTag("value");
			this.objXMLGen.openTag("ref");
            xmlRef("", (PFRef)pobjSendParameterBagEntry.getValue());
            this.objXMLGen.closeTag("ref");
            this.objXMLGen.closeTag("value");
			break;
			
		case 2: // ppbetEntities
			Map colEntities = (Map)pobjSendParameterBagEntry.getValue();
			if (colEntities != null && colEntities.size() > 0) {
				this.objXMLGen.openTag("value");
				this.objXMLGen.openTag("entities");
				
				PFEntity objEntity;
				Iterator iter = colEntities.values().iterator();
				while (iter.hasNext()) {
					objEntity = (PFEntity)iter.next();
					xmlEntity(objEntity);
				}
				
				this.objXMLGen.closeTag("entities");
				this.objXMLGen.closeTag("value");
			}
			break;
			
		case 3: // ppbetEntity
			this.objXMLGen.openTag("value");
			this.objXMLGen.openTag("entity");
            xmlEntity((PFEntity)pobjSendParameterBagEntry.getValue());
            this.objXMLGen.closeTag("entity");
            this.objXMLGen.closeTag("value");
			break; 
			
		case 4: // ppbetLabel
			this.objXMLGen.openTag("value");
			xmlLabel("label", (LabelCollection)pobjSendParameterBagEntry.getValue());
			this.objXMLGen.closeTag("value");
			break;
			
		case 5: // ppbetComponent
			this.objXMLGen.openTag("value");
			xmlComponent("component", (PFComponent)pobjSendParameterBagEntry.getValue());
			this.objXMLGen.closeTag("value");
			break;
			
		case 6: // ppbetString
			this.objXMLGen.openTag("value");
			this.objXMLGen.taggedCData("string", (String)pobjSendParameterBagEntry.getValue());
			this.objXMLGen.closeTag("value");
			break;
			
		}
        
    	this.objXMLGen.closeTag("parameter");
    	
    	return xmlParameterBagEntry;
    }
    
    //------------------------ Cloning
    
	/**
	 * @param pobjPageflow The handle to the current pageflow. 
	 * 					   This will have run time information and will replace stale handles to this object.
	 * @return Returns a clean clone of the pageflow descriptor, as it would be just after parsing it.
	 */
	public Object clone(Pageflow pobjPageflow) {
        /**
         * Start afresh :
         */
        PFDescriptor cleanClone = new PFDescriptor();
        
        // this.comment
        // this.digester
        // this.justInTimeParsing
        
        cleanClone.setKey(getKey());
        
        /** 
         * Clone the pageflow actions. 
         **/
        if (this.actions != null) {
            /**
             * Clone the entire pageflow, even though we may only want one part of it.
             */ 
            ZXCollection colActions = new ZXCollection(this.actions.size());
            PFAction objAction;
            Iterator iter = this.actions.iterator();
            Iterator iterKey = this.actions.iteratorKey();
            while (iter.hasNext()) {
                objAction = (PFAction)iter.next();
                
                /** Relink to the current pfdescriptor and pageflow objects. */
                objAction.setPageflow(pobjPageflow);
                objAction.setDescriptor(cleanClone);
                
                colActions.put(iterKey.next(), objAction.clone(pobjPageflow));
            }
            cleanClone.setActions(colActions);
            
            /**
             * Just in time cloning : Do not seem to improve performance.
             */
            // This only does cloning when needed to
            //cleanClone.setCacheActions(this.actions);
        }
        
        if (getBaseurl() != null) {
            cleanClone.setBaseurl((PFUrl)getBaseurl().clone());
        }
        
        cleanClone.setPropagateqs(isPropagaTeqs());
        cleanClone.setBodescriptor(getBodescriptor());
        
        cleanClone.setContextid(getContextid());
        
        if (this.contextupdate != null) {
            cleanClone.setContextupdate(CloneUtil.clone((ArrayList)this.contextupdate));
        }
        
		cleanClone.setDebugMode(getDebugMode());
		
        cleanClone.setHelpid(getHelpid());
        cleanClone.setDebugMode(getDebugMode());
        
        if (this.includes != null) {
            cleanClone.setIncludes(CloneUtil.clone(this.includes));
        }
        
        cleanClone.setName(getName());
        cleanClone.setStartaction(getStartaction());
        
        if (this.security != null) {
            cleanClone.setSecurity((ZXCollection)this.security.clone());
        }
        
        if (this.title != null) {
            cleanClone.setTitle((LabelCollection)this.title.clone());
        }
        
        cleanClone.setVersion(getVersion());
        
        if (this.receiveParameterBags != null) {
        	cleanClone.setReceiveParameterBags((ZXCollection)this.receiveParameterBags.clone());
        }
        if (this.sendParameterBags != null) {
        	cleanClone.setSendParameterBags(this.sendParameterBags);        	
        }
        
        return cleanClone;
    }
}