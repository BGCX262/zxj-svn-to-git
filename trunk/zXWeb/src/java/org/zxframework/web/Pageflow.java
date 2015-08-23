/*
 * Created on Apr 10, 2004 by michael
 * $Id: Pageflow.java,v 1.1.2.58 2006/07/26 09:50:32 mike Exp $
 */
package org.zxframework.web;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.CntxtEntry;
import org.zxframework.Descriptor;
import org.zxframework.Environment;
import org.zxframework.LabelCollection;
import org.zxframework.Tuple;
import org.zxframework.WebSettings;
import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.expression.ExprFHBO;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;
import org.zxframework.web.expression.ExprFHPageflow;
import org.zxframework.web.util.HTMLGen;
import org.zxframework.web.ReceiveParameterBag;
import org.zxframework.web.SendParameterBag;
import org.zxframework.web.PFUrl;

/**
 * The famous zX pageflow object. 
 * 
 * <pre>
 * 
 * Associated with this object are all the clsPF* objects.
 * This object is supposed to be created by the HTML object as factory, i.e.
 * 
 *  set objPageflow = objHTML.getPageflow
 * 
 * NOTE: For this to really work, we need the PageFlow object and the XML/HTML outputter to be independent 
 * from the Servlet stuff.
 * 
 * 
 * Change    : BD11DEC02
 * Why       : Different class for submit buttons
 * 
 * Change    : DGS17JAN2003
 * Why       : QueryDef generates Where clauses with the preceding AND,
 *             whereas normal pageflow usually doesn't. Therefore, to avoid confusion
 *             when trying to use the same query in and out of pageflows, handle the
 *             situation by not adding another AND when one exists at the start.
 *             Order By clause is similar.
 * 
 * Change    : DGS14FEB2003
 * Why       : processRef will now generate a clickable image instead of a ref or submit button
 *             if the PF descriptor includes the image URL.
 * 
 * Change    : BD19FEB03
 * Why       : - Process any HTML related tags associated with the action
 *             - Added BO function handler to expression
 * 
 * Change    : BD20FEB03
 * Why       : - Implement active director
 * 
 * Change    : DGS28FEB2003
 * Why       : Implement popups
 * 
 * Change    : BD23MAR02
 * Why       : Added #havelock director
 * 
 * Change    : BD27MAR03
 * Why       : - Better default label for submit buttons
 *             - Added the ingroup director
 * 
 * Change    : BD28MAR03
 * Why       : Made resolveDirector bit smarter so that we can leave
 *             entity out if there is only one entity in the collection
 * 
 * Change    : BD31MAR03
 * Why       : Added directors #viewonlygroup and #notviewonly to make the
 *             handling of locking a bit easier
 * 
 * Change    : BD1APR03
 * Why       : Make sure the expression BO context is empty when we start
 * 
 * Change    : BD8APR03
 * Why       : Allow directors in Javascript tag
 * 
 * Change    : BD8APR03
 * Why       : Revised the processRef function
 * 
 * Change    : BD8APR03
 * Why       : Added #null / #notnull functions
 * 
 * Change    : BD9APR03
 * Why       : Fixed bug in processPKWhereGroup; taking wrong offset
 *             when we have 2-character length operators (e.g. <= or >= or %%)
 * 
 * Change    : DGS14APR2003
 * Why       : Implement Entity Massagers
 * 
 * Change    : BD16APR03
 * Why       : Added #inqs and #notinqs director added
 * 
 * Change    : DGS16APR2003
 * Why       : Further tweak to Entity Massagers to allow wider use
 * 
 * Change    : BD28APR03
 * Why       : Added facility to allow incoming querystring to be copied to
 *             outgoing 'as-is'. This will make the pageflows a lot easier to maintain
 * 
 * Change    : BD12MAY03
 * Why       : - Moved director support to zX level
 *             - Added support for multiple submit buttons
 * 
 * Change    : BD15MAY03
 * Why       : - Fixed bug with not treating frameno 0 and notouch the same....
 *             - Added parent / grandparent frameno
 * 
 * Change    : BD22MAY03
 * Why       : - Fixed bug in resolveEntity: group to load can be a director
 * 
 * Change    : BD5JUN03
 * Why       : Fixed bug in processAttributeValues where boolean value
 *             was always set to true!
 * 
 * Change    : DGS23JUN2003
 * Why       : ProcessMessages now also processes narrative, if any.
 *             New function 'getLabel', similar to existing 'resolveLabel' (and which now calls
 *             getLabel) but doesn't resolve directors in the retrieved label. Used when narrative
 *             has checkbox 'is a director' not set.
 * 
 * Change    : BD23JUN03
 * Why       : Made re-usable handle window footer and title method
 * 
 * Change    : DGS27JUN2003
 * Why       : Change to getEntityCollection for Edit sub actions. Now expects a string as the
 *             final parameter rather than a sub action enum. The string can contain various
 *             combinations of pre and post edit form sub actions.
 * 
 * Change    : DGS15JUL2003
 * Why       : Suppress Copy buttons when New persist status (like Create button is suppressed)
 * 
 * Change    : BD12AUG03
 * Why       : Save / restore quick context when generating URL's. This is essential
 *             as otherwise the query string entries that are used in an URL are permanently
 *             stored into the quick context and thus find there way to all subsequent
 *             URL's if propagate QS is switched on
 * 
 * Change    : BD15AUG03
 * Why       : Added tag support for treeColumn1, 2 and 3
 * 
 * Change    : BD27AUG03
 * Why       : Added ability to refer to external actions
 * 
 * Change    : BD4SEP03
 * Why       : Added advanced support for where groups
 * 
 * Change    : DGS19SEP2003
 * Why       : New Grid editform/createupdate actions: uses a QS entry -pgmore, and behaves
 *             similar to normal editform/createupdate in some case statements
 * 
 * Change    : DGS26SEP2003
 * Why       : Added support for swapping to/from Alternative Connections. This allows
 *             an application to access more than one database.
 * 
 * Change    : BD23OCT03
 * Why       : Change input type to submit for ref submit buttons that have
 *             an URL associated with it as submit buttons are default
 * 
 * Change    : BD31OCT03
 * Why       : Add parameter to zXSubmitThisUrl to distinguish between
 *             submit button and non-submit button
 * 
 * Change    : DGS24NOV03
 * Why       : when processing attribute values, switch validate off as
 *             it may be that because of the context the system is trying to
 *             set attributes to invalid values and yet this is correct
 * 
 * Change    : BD26NOV03
 * Why       : - Allow action in go method to be reference to external
 *               pageflow
 *             - When debugging is on, use liveDump to trace expressions
 * 
 * Change    : BD20NOV03
 * Why       : No longer clear the BOContext in getEntityCollection
 *             NOTE THAT THIS IS A VERY IMPOTRANT CHANGE WITH POTENTIAL
 *             CONSEQUENCES FOR OLDER PAGEFLOWS
 * 
 * Change    : BD22JAN04
 * Why       : Use the label for the title for buttons with an image
 * 
 * Change    : DGS02FEB2004
 * Why       : In getEntityCollection, now resolve attr group directors using a new
 *             function of the entity. This preserves 'original' values so that the
 *             resolve can be repeated using the same entity.
 *            : Also made sure all querynames are resolved.
 * 
 * Change    : DGS12FEB2004
 * Why       : Minor changes to support the 'Matrix' edit form. Also new function
 *             'handleEnhancers' to eliminate duplicate code in edit form, grid edit form
 *             and new matrix edit form.
 * 
 * Change    : BD14FEB04
 * Why       : - Added support for alert message
 *             - Pass -zXInStdPopupTab around when o all pageflows in stdPopup tab
 * 
 * Change    : BD16FEB04
 * Why       : In relevantEntity, a gridForm entity is relevant when either
 *             the selectEditGroup or selectList group is set
 * 
 * Change    : BD23FEB04
 * Why       : Allow strAttr to be nullstring in processAttributeValues; this
 *             is interpreted as not wanting to set any attrs
 * 
 * Change    : BD27FEB04
 * Why       : When generating popup menus, use the action name as well to
 *             make the name unique; this allows for combining different
 *             actions with popup menus on a single page
 * 
 * Change    : DGS02MAR2004
 * Why       : In constructURL, treat an empty URL the same as #dummy i.e. no URL
 * 
 * Change    : BD5MAR04
 * Why       : Complex hack: see comment marked with BD5MAR04 
 * 
 * Change    : BD30MAR04
 * Why       : Fixed bug: forgot to reset alertMsg after processing
 * 
 * Change    : DGS21APR2004
 * Why       : Retrieve SQL: Added Group By clause to queries.
 *             Also: stop generating empty javascript tags (in a couple of places).
 * 
 * Change    : BD6MAY04
 * Why       : Change context to use main context rather than sub-context
 * 
 * Change    : DGS11MAY2004
 * Why       : Uses existing 'actions' refs (previously not used) to generate follow-on
 *             actions.
 *             New frame type of 'simple ref'
 * 
 * Change    : BD22MAY04
 * Why       : Added the very special director '#js.'; this is not really
 *             a director as it is not resolved using the director handler;
 *             it is ONLY supported as a source of a querystring
 *             and allow you to include javascript inside standard pageflow URLs
 *             This can be very useful to include values of fields on the
 *             current form in URLs without having to submit and processthe
 *             form first.
 *             E.g.
 *             URL - doSomething
 *             Frame - window
 *             Type - action
 *             Query string entries
 *                #js.#fieldValue.clnt.nme    -nme
 * 
 *             This will pass the current value of the field clnt.nme
 *             in the querystring as 'name'
 * 
 * Change    : BD26MAY04
 * Why       : Added sizedWindow framenumber; bit of a special case, see
 *             comments in wrapRefUrl
 * 
 * Change    : BD4JUN04
 * Why       : Fixed serious thing in processAtrValues: now also support zxDates (ie
 *             in ddmmmyy / ddmmyyyy) format
 * 
 * Change    : BD14JUN04
 * Why       : Added forceFocusAttr; see editForm for use
 * 
 * Change    : BD4UL04
 * Why       : - Fixed bug with -wrn qs entry (used for warnings in edit form); do NOT
 *               include in qs as result of propagate qs
 *             - Fixed bug in resolveLink when using external pageflow action
 * 
 * Change    : BD12JUL04
 * Why       : Fixed bug in handling actions; active was not implements
 * 
 * Change    : 03AUG2004 Domarque Workshop 59
 * Why       : In exportList, was generating an empty column when 'null' group.
 * 
 * Change    : BD8AUG04
 * Why       : Fixed bug in processAttributeValues: was not able to set
 *             a non-string value to null using reserved #null source (ie value|
 * 
 * Change    : BD11AUG04
 * Why       : Support confirmation on submit-ref buttons
 * 
 * Change    : BD10SEP04
 * Why       : Changed way that debugging is done to ensure that generated
 *             debug messages do not screw up the generated HTML / Javascript
 * Why       : Added basic server-side page timing when in debug mode
 *  
 * Change    : DGSOCT2004
 * Why       : Added Calendar action type
 * 
 * Change    : DGS5NOV2004
 * Why       : In processHTMLTags, some tag names were not lower case when compared to lcase
 *  
 * Change    : BD06DEC04
 * Why       : Undid a terrible hack introduced at 22MAY04 (not even added to
 *             this comment area but is commented in code)
 *                 
 * Change    : V1.4:14 - BD11JAN05
 * Why       : - Undoing the 22MAY04 bug (see 6DEC04) invalidated
 *               a feature that only worked because of the bug of 22MAY04
 *             - Also minor performance thing in resolveDirector
 *                
 * Change    : V1.4:19 - BD17JAN05
 * Why       : - Use seperate instance of HTML object for the pageflow
 *               used in external pageflow actions; sharing the HTML object
 *               caused some funny side-effects
 *                
 * Change    : V1.4:25 - DGS21JAN2005
 * Why       : In wrapRefUrl, added new frame type of 'submitcheck', which behaves just like
 *             'submit' except that, if there is a confirm message' the javascript will call
 *             'zXSubmitThisUrlCheck' rather than zXSubmitThisUrlConfirm'.
 *
 * Change    : V1.4:27 - BD24JAN05
 * Why       : In processAttributeValues, a bug caused all attributes to be
 *             set to null as soon as one #null was found
 *
 * Change    : V1.4:32 - BD6FEB05
 * Why       : Added support for image in search form order by label (is now HTML tag)
 * 
 * Change    : V1.4:33 - BD6FEB05
 * Why       : On export list, make progress go all the way to 100%; otherwise looks
 *             silly when user has configured machine so that XLS / CSV opens in
 *             a separate window; it leaves the export window open and this used to
 *             go only as far a 90%
 * 
 * Change    : V1.4:39 - BD/DGS11FEB2005
 * Why       : In constructURL, must restore the -pf QS entry as it may have been altered.
 *             In processTags, minor adjustment to the recent zxsearchformorderbyimage tag name.
 *             In getEntityCollection, was looping through all entities in two places where
 *             should only loop through returned entities.
 * 
 * Change    : V1.4:44 - DGS18FEB2005
 * Why       : In processFrameHandling changed the 'bigger' percentages from 80/20 to 70/30
 * 
 * Change    : V1.4:58 - DGS/MB22MAR2005
 * Why       : In processRefByFormType now disables submitted buttons on click. Also change
 *             here and in wrapRefUrl to the calls of the javascript function zXSubmitThisUrl
 *             (and similar variants) to pass 'this', as the js will disable the button.
 *             This is all designed to prevent multiple submits.
 * 
 * Change    : V1.5:1 - BD30MAR05
 * Why       : Added support for data sources
 *
 * Change    : V1.5:3 - BD15APR05
 * Why       : Added support for page-flow action BO context
 * 
 * Change    : BD18MAY05 - V1.5:18
 * Why       : Added support for property persistStatus
 * 
 * Change    : V1.4:90 - DGS16JUN2005
 * Why       : In exportList now has slightly adjusted logic for showing the progress bar
 * 			   to fix a problem found when exporting large numbers of rows
 * 
 * Change    : BD18MAY05 - V1.5:32
 * Why       : Added support for createUpdateActions
 * 
 * Change    : BD3OCT05  V1.5:61
 * Why       : Added support for confirmation message for stdPopup action
 * 
 * Change    : BD18OCT05  V1.5:65
 * Why       : Added support for parameterBags and pageflow calls
 *
 * Change    : V1.5:95 DGS/BD28APR2006
 * Why       : - New feature to be able to make ref buttons appear at the top of forms and
 *               behave like a menu bar
 *             - processCUActions can ignore warnings
 *
 * Change    : V1.5:101 / BD15JUN06
 * Why       : Fixed bug when having a linke action with an external pageflow in combination
 *
 * Change    : V1.5:102 / BD27JUN06
 * Why       : Export to csv when there is a group with selectListGroup <> null and listGroup null.
 *               This can cause problems in a scenario as the following: imagine you have 3 entities;
 *               2 selectListGroup something and listGroup null and one with a group that has several
 *               attributes with dynamic values. These dynamic values can rely on the selectListGroup
 *               attributes to be loaded for the other entitieswith popup menu on the action that the
 *               link action is from. See processPopups        
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class Pageflow extends ZXObject {

	//------------------------ Constants
	
    /** The sql select clause handle in the session object */
    public static final String QRYSELECTCLAUSE = "QrySelectClause";
    /** The sql where clause handle in the session object */
    public static final String QRYWHERECLAUSE = "QryWhereClause";
    /** The sql group by clause handle in the session object */
    public static final String QRYGROUPBYCLAUSE = "QryGroupByClause";
    /** The sql order by clause handle in the session object */
    public static final String QRYORDERBYCLAUSE = "QryOrderByClause";
    
    //------------------------ Members
    
    private PageBuilder page;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String pageflow;
    private Pageflow otherPageflow;
    private PFDescriptor PFDesc;
    private DrctrFHPageflow pageflowDirectorFuncHandler;
    private PFQS qs;
    private String baseURL;
    private ExprFHPageflow exprFHPageflow;
    private ExprFHBO exprFHBO;
    private ZXCollection contextEntities;
    private PFEntity contextEntity;
    private PFAction contextAction;
    private String action = "";
    private String infoMsg = "";
    private String errorMsg = "";
    private String alertMsg = "";
    private boolean ownForm;
    private int contextLoopOverOk;
    private int contextLoopOverError;
    private String forceFocusAttr;
    private long startClickCount;
	private SendParameterBag lastSendParameterBag;
	private String lastSendParameterBagName;
	private ReceiveParameterBag lastReceiveParameterBag;
	
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public Pageflow() {
        super();
    }
    
    /**
     * Initialise the pageflow object. This is the init for the Web servlet environment.
     *
     * @param pobjPage A handle to the output builder
     * @param pobjRequest A handle to the HttpServlet request.
     * @param pobjResponse A handle to the HttpServlet response.
     * @param pstrPageflow The page flows to execute
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if init fails. 
     */
     public zXType.rc init(PageBuilder pobjPage, 
                           HttpServletRequest pobjRequest, 
                           HttpServletResponse pobjResponse, 
                           String pstrPageflow) throws ZXException {
         if(getZx().trace.isFrameworkTraceEnabled()) {
             getZx().trace.enterMethod();
             getZx().trace.traceParam("pobjXML", pobjPage);
             getZx().trace.traceParam("pobjRequest", pobjRequest);
             getZx().trace.traceParam("pobjResponse", pobjResponse);
             getZx().trace.traceParam("pstrPageflow", pstrPageflow);
         }

         zXType.rc init = zXType.rc.rcOK;
         
         try {
             
            setPage(pobjPage);
            setRequest(pobjRequest);
            setResponse(pobjResponse);
             
            /**
             * Used for page timing
             */
            // setStartClickCount(System.currentTimeMillis());
             
            /**
             * Initialise querystring object
             */
            this.qs = new PFQS();
            getQs().init(this);
            
            /**
             * Get the name of the pageflow definition to process if it has
             * not already been set by the calling ASP page
             */
            this.pageflow = pstrPageflow;
            if (StringUtil.len(this.pageflow) == 0) {
                this.pageflow = pobjRequest.getParameter("-pf");
                if (StringUtil.len(this.pageflow) == 0) {
                    throw new Exception("No pageflow name passed (-pf)");
                }
            }
             
            /**
             * Add pageflow director function handler
             */
            this.pageflowDirectorFuncHandler = new DrctrFHPageflow();
            this.pageflowDirectorFuncHandler.setPageflow(this);
            getZx().getDirectorHandler().registerFH("pageflow", pageflowDirectorFuncHandler);
            
            String strFilename = getZx().fullPathName(getZx().getPageflowDir()) + File.separatorChar + this.pageflow + ".xml";
            
            /**
             * Methods for retrieving the pageflow descriptor :
             */
            if (getZx().getRunMode().pos == zXType.runMode.rmProduction.pos) {
                /**
                 * Check whether the file has been modified recently.
                 */
                PFDescriptor objPFDescCached = (PFDescriptor) getZx().getCachedValue(getPageflow());
                
                if (objPFDescCached != null) {
                    /** 
                     * Relink the zx and pageflow objects 
                     **/
                	this.PFDesc = (PFDescriptor)objPFDescCached.clone(this);
                    
                } else {
                    /** Parse From afresh * */
                	PFDescriptor objPFDesc = new PFDescriptor();
                	objPFDesc.init(this, strFilename, true);
                    getZx().setCachedValue(getPageflow(), objPFDesc);
                    
                    this.PFDesc = (PFDescriptor)objPFDesc.clone(this);
                }
                
            } else if (getZx().getRunMode().pos == zXType.runMode.rmDevelCache.pos) {
                /**
                 * Check whether the file has been modified recently.
                 */
                File file = new File(strFilename);
                long lastModified = file.lastModified();
                
                /**
                 * Retrieve pageflow descriptor from the cache by name.
                 */
                PFDescriptor objPFDescCached = (PFDescriptor)getZx().getCachedValue(getPageflow());
                
                /**
                 * Check if this descriptor has been update recently.
                 */
                if (objPFDescCached != null && lastModified == objPFDescCached.getLastModified()) {
                    /** 
                     * Relink the zx and pageflow objects 
                     **/
                	this.PFDesc = (PFDescriptor)objPFDescCached.clone(this);
                    
                } else {
                    /** 
                     * Parse From afresh 
                     **/
                	PFDescriptor objPFDesc = new PFDescriptor();
                	objPFDesc.init(this, strFilename, true);
                    getZx().setCachedValue(getPageflow(), objPFDesc);
                    
                    this.PFDesc = (PFDescriptor)objPFDesc.clone(this);
                    objPFDesc.setLastModified(lastModified);
                }
                
            } else {
                /**
                 * Developer mode etc.. - when we are not in production mode the
                 * framework will be sensitive to changes in the pageflow xml
                 * file allow developers rad development.
                 * 
                 * Now we can initialise the descriptor
                 */
                this.PFDesc = new PFDescriptor();
                this.PFDesc.init(this, strFilename, true);
            }
             
            /**
             * Handle the pageflow context update
             */
            processContextUpdate(this.PFDesc.getContextupdate());
            
            /**
             * Parse query string.
             * NOTE : This now happens in the zX tag.
             */
            // this.qs.parseQuerystring();
             
            /**
             * Determine base url; one of the following methods: - Either
             * the 'old' way (i.e. construct the baseUrl and explicitly add
             * the QS entries) - The new way (i.e. propagate all the
             * incoming querystring entries)
             */
            if (this.PFDesc.isPropagaTeqs()) {
                this.baseURL = resolveDirector(this.PFDesc.getBaseurl().getUrl());
            } else {
                queryString2QS(this.PFDesc.getBaseurl().getQuerystring());
                this.baseURL = constructURL(this.PFDesc.getBaseurl(), false);
            }
             
            /**
             * Register our expression function handlers
             */
            this.exprFHPageflow = new ExprFHPageflow(this);
            getZx().getExpressionHandler().registerFH("pf", getExprFHPageflow());
             
            this.exprFHBO = new ExprFHBO();
            getZx().getExpressionHandler().registerFH("bo", getExprFHBO());
 
            return init;
         } catch (Exception e) {
             getZx().trace.addError("Failed to : Initialise the pageflow object.", e);
             if (getZx().log.isErrorEnabled()) {
                 getZx().log.error("Parameter : pobjXML = "+ pobjPage);
                 getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
                 getZx().log.error("Parameter : pobjResponse = "+ pobjResponse);
                 getZx().log.error("Parameter : pstrPageflow = "+ pstrPageflow);
             }
             
             if (getZx().throwException) throw new ZXException(e);
             init = zXType.rc.rcError;
             return init;
         } finally {
             if(getZx().trace.isFrameworkTraceEnabled()) {
                 getZx().trace.returnValue(init);
                 getZx().trace.exitMethod();
             }
         }
     }
    
    //------------------------ Getters/Setters
     
    /**
     * @return Returns whether debug messages are on.
     */
    public boolean isDebugOn() {
        if (this.PFDesc.getDebugMode() != null) {
            return this.PFDesc.getDebugMode().equals(zXType.pageflowDebugMode.pdmOn);
        }
        return false;
    }
     
    /**
     * Name of attribute that should get focus (instead of the first editable field). 
     * Only an error takes precedence
     * 
     * @return Returns the forceFocusAttr.
     */
    public String getForceFocusAttr() {
        return forceFocusAttr;
    }
    
    /**
     * @param forceFocusAttr The forceFocusAttr to set.
     */
    public void setForceFocusAttr(String forceFocusAttr) {
        this.forceFocusAttr = forceFocusAttr;
    }
    
    /**
     * @return Returns the startClickCount.
     */
    public long getStartClickCount() {
        return startClickCount;
    }
    
    /**
     * @param startClickCount The startClickCount to set.
     */
    public void setStartClickCount(long startClickCount) {
        this.startClickCount = startClickCount;
    }
    
    /**
     * @return Returns the pageflow.
     */
    public String getPageflow() {
        return pageflow;
    }
    
    /**
     * @param pageflow The pageflow to set.
     */
    public void setPageflow(String pageflow) {
        this.pageflow = pageflow;
    }
    
    /**
     * @return Returns the otherPageflow.
     */
    public Pageflow getOtherPageflow() {
        return otherPageflow;
    }
    
    /**
     * @param otherPageflow The otherPageflow to set.
     */
    public void setOtherPageflow(Pageflow otherPageflow) {
        this.otherPageflow = otherPageflow;
    }
    
    /**
     * @return Returns the pageflowDirectorFuncHandler.
     */
    public DrctrFHPageflow getPageflowDirectorFuncHandler() {
        return pageflowDirectorFuncHandler;
    }
    
    /**
     * @param pageflowDirectorFuncHandler The pageflowDirectorFuncHandler to set.
     */
    public void setPageflowDirectorFuncHandler(DrctrFHPageflow pageflowDirectorFuncHandler) {
        this.pageflowDirectorFuncHandler = pageflowDirectorFuncHandler;
    }
    
    /**
     * @return Returns the pFDesc.
     */
    public PFDescriptor getPFDesc() {
        return PFDesc;
    }
    
    /**
     * @param desc The pFDesc to set.
     */
    public void setPFDesc(PFDescriptor desc) {
        PFDesc = desc;
    }
    
    /**
     * @return Returns the request.
     */
    public HttpServletRequest getRequest() {
        return request;
    }
    
    /**
     * @param request The request to set.
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    /**
     * @return Returns the exprFHPageflow.
     */
    public ExprFHPageflow getExprFHPageflow() {
        return exprFHPageflow;
    }
    
    /**
     * @param exprFHPageflow The exprFHPageflow to set.
     */
    public void setExprFHPageflow(ExprFHPageflow exprFHPageflow) {
        this.exprFHPageflow = exprFHPageflow;
    }
    
    /**
     * @return Returns the errorMsg.
     */
    public String getErrorMsg() {
        return errorMsg;
    }
    
    /**
     * @param errorMsg The errorMsg to set.
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    
    /**
     * @return Returns the infoMsg.
     */
    public String getInfoMsg() {
        return infoMsg;
    }
    
    /**
     * @param infoMsg The infoMsg to set.
     */
    public void setInfoMsg(String infoMsg) {
        this.infoMsg = infoMsg;
    }
    
    /**
     * @return Returns the alertMsg.
     */
    public String getAlertMsg() {
        return alertMsg;
    }
    
    /**
     * @param alertMsg The alertMsg to set.
     */
    public void setAlertMsg(String alertMsg) {
        this.alertMsg = alertMsg;
    }
    
    /**
     * @return Returns the exprFHBO.
     */
    public ExprFHBO getExprFHBO() {
        return exprFHBO;
    }
    
    /**
     * @return Returns the contextEntities.
     */
    public ZXCollection getContextEntities() {
        if (this.contextEntities == null) {
            this.contextEntities = new ZXCollection();
        }
        return contextEntities;
    }
    
    /**
     * @param contextEntities The contextEntities to set.
     */
    public void setContextEntities(ZXCollection contextEntities) {
        this.contextEntities = contextEntities;
    }
    
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
     * @return Returns the contextAction.
     */
    public PFAction getContextAction() {
        return contextAction;
    }
    
    /**
     * @param contextAction The contextAction to set.
     */
    public void setContextAction(PFAction contextAction) {
        this.contextAction = contextAction;
    }
    
    /**
     * @return Returns the contextEntity.
     */
    public PFEntity getContextEntity() {
        return contextEntity;
    }
    
    /**
     * @param contextEntity The contextEntity to set.
     */
    public void setContextEntity(PFEntity contextEntity) {
        this.contextEntity = contextEntity;
    }
    
    /**
     * @param exprFHBO The exprFHBO to set.
     */
    public void setExprFHBO(ExprFHBO exprFHBO) {
        this.exprFHBO = exprFHBO;
    }
    
    /**
     * @return Returns the baseURL.
     */
    public String getBaseURL() {
        return baseURL;
    }
    
    /**
     * @param baseURL The baseURL to set.
     */
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
    
    /**
     * Tell pageflow object that for open / close tags are handled by the calling ASP
     * 
     * @return Returns the ownForm.
     */
    public boolean isOwnForm() {
        return ownForm;
    }
    
    /**
     * @param ownForm The ownForm to set.
     */
    public void setOwnForm(boolean ownForm) {
        this.ownForm = ownForm;
    }
    
    /**
     * @return Returns the contextLoopOverError.
     */
    public int getContextLoopOverError() {
        return contextLoopOverError;
    }
    
    /**
     * @param contextLoopOverError The contextLoopOverError to set.
     */
    public void setContextLoopOverError(int contextLoopOverError) {
        this.contextLoopOverError = contextLoopOverError;
    }
    
    /**
     * @return Returns the contextLoopOverOk.
     */
    public int getContextLoopOverOk() {
        return contextLoopOverOk;
    }
    
    /**
     * @param contextLoopOverOk The contextLoopOverOk to set.
     */
    public void setContextLoopOverOk(int contextLoopOverOk) {
        this.contextLoopOverOk = contextLoopOverOk;
    }
    
    /**
     * @return Returns the response.
     */
    public HttpServletResponse getResponse() {
        return response;
    }
    
    /**
     * @return Returns the qs.
     */
    public PFQS getQs() {
        if (this.qs == null) {
            this.qs = new PFQS();
        }
        return qs;
    }
    
    /**
     * @param qs The qs to set.
     */
    public void setQs(PFQS qs) {
        this.qs = qs;
    }
    
    /**
     * @param response The response to set.
     */
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
    
    /**
     * @return Returns the page.
     */
    public PageBuilder getPage() {
        return page;
    }
    
    /**
     * @param page The page to set.
     */
    public void setPage(PageBuilder page) {
        this.page = page;
    }
	
	/**
	 * Gets this last used receive parameter bag.
	 * 
	 * @return the lastReceiveParameterBag
	 */
	public ReceiveParameterBag getLastReceiveParameterBag() {
		return lastReceiveParameterBag;
	}

	/**
	 * @param lastReceiveParameterBag the lastReceiveParameterBag to set
	 */
	public void setLastReceiveParameterBag(
			ReceiveParameterBag lastReceiveParameterBag) {
		this.lastReceiveParameterBag = lastReceiveParameterBag;
	}

	/**
	 * Gets this last used send parameter bag.
	 * 
	 * @return the lastSendParameterBag
	 */
	public SendParameterBag getLastSendParameterBag() {
		return lastSendParameterBag;
	}

	/**
	 * @param lastSendParameterBag the lastSendParameterBag to set
	 */
	public void setLastSendParameterBag(SendParameterBag lastSendParameterBag) {
		this.lastSendParameterBag = lastSendParameterBag;
	}

	/**
	 * Gets this last used send parameter bag name.
	 * 
	 * @return the lastSendParameterBagName
	 */
	public String getLastSendParameterBagName() {
		return lastSendParameterBagName;
	}

	/**
	 * @param lastSendParameterBagName the lastSendParameterBagName to set
	 */
	public void setLastSendParameterBagName(String lastSendParameterBagName) {
		this.lastSendParameterBagName = lastSendParameterBagName;
	}
	
    //------------------------ Public Methods

	/**
     * Resolve a director.
     * 
     * <pre>
     * 
     * ie a string that starts with  a # and that represents a special meaning
     * 
     * Reviewed for V1.5:65
     * </pre>
     *
     * @param pstrDirector A special string with a direct in it.
     * @return Returns a evaluated version of the director.
     * @throws ZXException Thrown if resolveDirector fails. 
     */
    public String resolveDirector(String pstrDirector) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDirector", pstrDirector);
        }

        String resolveDirector = null;
        
        try {
            /**
             *  V1.4:14 - BD11JAN05
             *  Small performance gain: on erro goto is a relatively expensive hobby and should
             *  be avoided; move it a few lines down as 99% of the directors will not start with
             *  a # and will therefore exit almost straight away; likelihood of an error occuring
             *  at this stage are slim
             */
            
            /**
             * For backward compatibility we only consider a string to be a director if it starts with a '#'
             * V1.4:14 BD11JAN05 - Small performance gain, checking length of string is cheaper
             * than concatenating
             */
            if (StringUtil.len(pstrDirector) == 0) {
                resolveDirector = "";
                return resolveDirector;
            }
            
            if (pstrDirector.charAt(0) != '#') {
                resolveDirector = pstrDirector;
                return resolveDirector;
            }
            
            if (pstrDirector.toLowerCase().startsWith("#expr.")) {
                /**
                 * If we are dealing with an expression, lets not attempt to parse
                 *  the remainder (ie the expression) as if it were a director
                 */
                resolveDirector = getZx().getExpressionHandler().eval(pstrDirector.substring(6)).getStringValue();
                
                /**
                 * Special attention in case of #expr!
                 */
                if (isDebugOn()) {
                    this.page.debugMsg(getZx().getExpressionHandler().liveDump(pstrDirector.substring(6)));
                }
                
            } else if (pstrDirector.toLowerCase().startsWith("#pb.")) {
            	/**
            	 * Special case of reference to propertybag parameter
            	 */
            	resolveDirector = resolveParameterDirectorAsString(pstrDirector.substring(4));
            	
            } else {
                resolveDirector = getZx().getDirectorHandler().resolve(pstrDirector);
                
            }
            
            return resolveDirector;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Resolve a director.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrDirector = "+ pstrDirector);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return resolveDirector;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(resolveDirector);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Process contextupdate for an action.
     *
     * @param pcolUpdaters  A collection of values to add to the context.
     * @return Returns the return code of the method.
     * @throws ZXException  Thrown if processContextUpdate fails. 
     */
    public zXType.rc processContextUpdate(List pcolUpdaters) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pcolUpdaters", pcolUpdaters);
        }

        zXType.rc processContextUpdate = zXType.rc.rcOK;
        
        try {
            if (pcolUpdaters != null) {
                PFDirector objDirector;
                int intUpdaters = pcolUpdaters.size();
                for (int i = 0; i < intUpdaters; i++) {
                    objDirector = (PFDirector)pcolUpdaters.get(i);
                    
                    addToContext(resolveInstringDirector(objDirector.getDestination()), 
                            	 resolveInstringDirector(objDirector.getSource()));
                }
            }
            return processContextUpdate;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process contextupdate for an action.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pcolUpdaters = "+ pcolUpdaters);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            processContextUpdate = zXType.rc.rcError;
            return processContextUpdate;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processContextUpdate);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add entry to context and prefix entry name with the name of the pageflow.
     * 
     * <pre>
     * 
     * This gets added to the ZX Session object in the following format : 
     * :pageflow:Subsession:Name:
     * </pre>
     *
     * @param pstrName The name of the value you want to add  
     * @param pstrValue  The value to add.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if addToContext fails. 
     */
    public zXType.rc addToContext(String pstrName, String pstrValue) throws ZXException {
        return addToContext(pstrName, pstrValue, false);
    }
    
    /**
     * Add entry to context and prefix entry name with the name of the pageflow.
     * 
     * <pre>
     * 
     * This gets added to the ZX Session object in the following format : 
     * :pageflow:Subsession:Name:
     * </pre>
     *
     * @param pstrName The name of the value you want to add  
     * @param pstrValue  The value to add.
     * @param pblnBaseContext Whether to add it to the base contect.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if addToContext fails. 
     */
    public zXType.rc addToContext(String pstrName, String pstrValue, boolean pblnBaseContext) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pstrValue", pstrValue);
            getZx().trace.traceParam("pblnBaseContext", pblnBaseContext);
        }

        zXType.rc addToContext = zXType.rc.rcOK;
        
        try {
            
            /**
             * If not in base context prefix the variable namne with pageflow and
             * sub context
             */
            if (pblnBaseContext) {
                getZx().getSession().addToContext(pstrName, pstrValue);
            } else {
                getZx().getSession().addToContext(getPageflow() + getQs().getEntryAsString("-ss") + pstrName, pstrValue);
            }
            
            return addToContext;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Add entry to context and prefix entry name with the name of the pageflow.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrName = " + pstrName);
                getZx().log.error("Parameter : pstrValue = " + pstrValue);
                getZx().log.error("Parameter : pblnBaseContext = " + pblnBaseContext);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            addToContext = zXType.rc.rcError;
            return addToContext;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(addToContext);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
	 * Contruct a URL.
	 * 
	 * <pre>
	 * 
	 * NOTE : This calls, constructURL(pobjUrl, false);
	 * </pre>
	 * 
	 * @param pobjUrl  The base Url to use.
	 * @return Returns a constructed url based on the context entities.
	 * @throws ZXException Thrown if constructURL fails.
	 */
	public String constructURL(PFUrl pobjUrl) throws ZXException {
		return constructURL(pobjUrl, false);
	}
     
    /**
	 * Contruct a URL.
	 * 
	 * @param pobjUrl  The base Url to use.
	 * @param pblnRecursive Has this routine been called. 
	 * 						Optional, default should be false.
	 * @return Returns a constructed url based on the context entities.
	 * @throws ZXException Thrown if constructURL fails.
	 */
    public String constructURL(PFUrl pobjUrl, boolean pblnRecursive) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjUrl", pobjUrl);
            getZx().trace.traceParam("pblnRecursive", pblnRecursive);
        }
        
        String constructURL = ""; 
        boolean blnNoPropagatePF= false;
        
        try {
            /**
             * Do not bother to construct url if there is nothing to add to :
             */
            if (pobjUrl == null) {
                return constructURL;
            }
            
            /**
             * Handle reserved url
             * DGS02MAR2004: and an empty url
             */
            if (StringUtil.len(pobjUrl.getUrl()) == 0 
            	|| pobjUrl.getUrl().equalsIgnoreCase("#dummy")) {
                return constructURL;
            }
            
            /**
             * First handle any potential inline directors
             */
            String strUrl = resolveInstringDirector(pobjUrl.getUrl());
            Iterator iter;
            
            zXType.pageflowUrlType urlType = pobjUrl.getUrlType();
            if (zXType.pageflowUrlType.putFixed.equals(urlType)) {
                /**
                 * Easy case: just return the url
                 */
                constructURL = strUrl;
                
            } else if (zXType.pageflowUrlType.putRelative.equals(urlType)) {
                /**
                 * Easy case: just append the url to the base url (but first construct this one)
                 */
                if (PFDesc.getBaseurl() == null) {
                    throw new Exception("No base URL defined, cannot use relative URL types");
                }
                
                /**
                 * Create basis of url (i.e. without taking querystring into consideration)
                 */
                constructURL = appendToUrl(this.baseURL, strUrl);
                
            } else if (zXType.pageflowUrlType.putPopup.equals(urlType)) {
                /**
                 * DGS28FEB2003 - new code for popups
                 * Popup - construct a call to the generated popup function
                 */
                constructURL = "zXDynPopup" + strUrl + ".show()";
                
            } else {
                /**
                 * Either we want to go to another action within this
                 * pageflow: actioname
                 * or to one in aother pageflow: pageflow:actionname
                 */
                if (strUrl.indexOf(':') != -1) {
                    /**
                     * Basically jump to an action in another pageflow
                     * Does NOT work when propagate QS is not on in either this
                     * or in base url
                     */
                    if (!getPFDesc().isPropagaTeqs()) {
                        throw new Exception("External actions are only allowed when propagate QS is on");
                    }
                    
                    /**
                     * Get pageflow part and action part :
                     * arrUrl[0] = is the pageflow
                     * arrUrl[1] is the action
                     */
                    String[] arrUrl = StringUtil.split(":", strUrl); 
                    String strPageflow = arrUrl[0];
                    String strAction = arrUrl[1]; 
                    
                    /**
                     * Make sure we did not end up with non-sense
                     */
                    if (arrUrl.length != 2 || StringUtil.len(strPageflow) ==0|| StringUtil.len(strAction) ==0) {
                        throw new Exception("URL has not got the <pageflow>:<action> syntax : " + strUrl);
                    }
                    
                    constructURL = externalPFBaseUrl(strPageflow, strAction);
                    if (StringUtil.len(constructURL) == 0) {
                        throw new Exception("Failed to construct a url");
                    }
                    
                    /**
                     * Now keep track of the fact that we do NOT want to
                     * include the -pf from the current pageflow!!!!
                     * BD11FEB2005: Must restore this pageflow in the quick context as
                     * it may have been changed during construction of the URL
                     */
                    blnNoPropagatePF = true;
                    getZx().getQuickContext().setEntry("-pf", this.PFDesc.getName());
                    getZx().getQuickContext().setEntry("-a", strAction);
                    
                } else {
                    constructURL = this.baseURL;
                    if (this.PFDesc.isPropagaTeqs()) {
                        this.qs.setEntry("-a", strUrl);
                    } else {
                        constructURL = appendToUrl(constructURL, "-a=" + strUrl);
                    }
                }
                
            }
            
            /**
             * Append any querystring entries to the QS collection. This could be either
             * the 'old' way of working (i.e. explictly build the querystring) or
             * the new way (i.e. propagate the incoming querystring)
             * In both cases, when the frame is 0 do not touch
             * 
             * DGS11MAY2004: Also do not add these when frame is simple ref
             */
            String strFrameno = resolveDirector(pobjUrl.getFrameno());
            if (!"0".equals(strFrameno) 
		         && !"notouch".equalsIgnoreCase(strFrameno)
		         && !"simpleref".equalsIgnoreCase(strFrameno)
		         && !zXType.pageflowUrlType.putPopup.equals(pobjUrl.getUrlType())
		        ) {
            	
                int intQuerystring = pobjUrl.getQuerystring()!=null?pobjUrl.getQuerystring().size():0;
                if (this.PFDesc.isPropagaTeqs()) {
                    if (intQuerystring > 0) {
                        /**
                         *  Before runining the quick context, make sure we take a safety copy
                         *  that we restore afterwards otherwise the querystring entries
                         *  are added to the quick context and are included in any subsequent
                         *  URL that is being generated
                         */
                        getZx().getQuickContext().saveState();
                        queryString2QS(pobjUrl.getQuerystring());
                    }
                    
                    CntxtEntry objEntry;
                    String strName;
                    iter = getZx().getQuickContext().getEntries().values().iterator();
                    while (iter.hasNext()) {
                        objEntry = (CntxtEntry)iter.next();
                        strName = objEntry.getName().toLowerCase();
                        
                        /**
                         * Now here is something interesting: when in propagation mode there
                         *  are a number of zX QS entries that we do not want to copy automatically
                         *  from the input to the output as they only make sense when explicitly
                         *  added to the querystring; i.e. when the status = 1
                         */
                        if (StringUtil.equalsAnyOf(new String[]{"-err", "-sa", "-prgs", "-pgmore", "-wrn"}, strName)) {
                            if (objEntry.getStatus() == 1) {
                                constructURL = appendToUrl(constructURL, constructQSEntry(objEntry.getName(), objEntry.getStrValue()));
                            }
                            
                        } else if (strName.equalsIgnoreCase("-pf")) {
                            if (!blnNoPropagatePF) {
                                constructURL = appendToUrl(constructURL, constructQSEntry(objEntry.getName(), objEntry.getStrValue()));
                            }
                            
                        } else if (strName.equalsIgnoreCase("-zxinstdpopuptab")) {
                            if (!blnNoPropagatePF) {
                                constructURL = appendToUrl(constructURL, constructQSEntry(objEntry.getName(), objEntry.getStrValue()));
                            }
                            
                        } else {
                            constructURL = appendToUrl(constructURL, constructQSEntry(objEntry.getName(), objEntry.getStrValue()));
                        }
                    }
                    
                    if (intQuerystring > 0) {
                        /**
                         * And restore the original quick context again
                         */
                        getZx().getQuickContext().restoreState();
                    }
                    
                } else {
                    if (intQuerystring > 0) {
                        PFDirector objDirector;
                        for (int i = 0; i < intQuerystring; i++) {
                            objDirector = (PFDirector)pobjUrl.getQuerystring().get(i);
                            constructURL = appendToUrl(constructURL, constructQSEntry(objDirector));
                        }
                    }
                }
            }
            
            /**
             * Add any URL close if applicable
             */
            if (StringUtil.len(pobjUrl.getUrlclose()) > 0) {
                constructURL = constructURL + pobjUrl.getUrlclose();
            }
            
            /**
             * DONE :)
             */
            return constructURL;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Contruct a URL.", e);
                getZx().log.error("Parameter : pobjUrl = "+ pobjUrl);
                getZx().log.error("Parameter : pblnRecursive = "+ pblnRecursive);
            }
            if (getZx().throwException) throw new ZXException(e);
            return constructURL;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(constructURL);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get collection of relevant entities.
     * 
     * @param pobjAction The PFAction to get the entity collection from.  
     * @param penmActionType The pageflow action type.
     * @param penmQueryType The pageflow query type.
     * @return Returns a Collection of a relavant Entitys.
     * @throws ZXException
     */
    public ZXCollection getEntityCollection(PFAction pobjAction, 
    										zXType.pageflowActionType penmActionType,
    										zXType.pageflowQueryType penmQueryType) throws ZXException {
        return getEntityCollection(pobjAction, penmActionType, penmQueryType, "");
    }
    
    /**
     * Get collection of relevant entities.
     * 
     * <pre>
     * 
     * It takes into consideration the following:
     * 
     *  - objAction.entityNames
     *  - objAction.entityAction
     *  - clsEntity.xxxxGroup in combination with action, querytype
     * 
     * DGS27JUN2003: SubAction parameter is now a string not an enum. 
     * Can contain various combinations of pre and post editform sub actions.
     * 
     * NOTE :As the PFAction is an instance of the PFAction type then the 
     * penmActionType of penmQueryType should not be needed ?
     * </pre>
     *
     * @param pobjAction The PFAction to get the entity collection from.  
     * @param penmActionType The pageflow action type.
     * @param penmQueryType The pageflow query type.
     * @param pstrSubAction The subaction if present.
     * @return Returns a Collection of a relavant Entitys.  
     * @throws ZXException Thrown if getEntityCollection fails. 
     */
    public ZXCollection getEntityCollection(PFAction pobjAction, 
                                            zXType.pageflowActionType penmActionType, 
                                            zXType.pageflowQueryType penmQueryType, 
                                            String pstrSubAction) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAction", pobjAction);
            getZx().trace.traceParam("penmActionType", penmActionType);
            getZx().trace.traceParam("penmQueryType", penmQueryType);
            getZx().trace.traceParam("pstrSubAction", pstrSubAction);
        }
        
        ZXCollection getEntityCollection = new ZXCollection();
        
        zXType.rc rc = zXType.rc.rcOK;
        
        try {
        	ZXCollection colEntities = null;
        	
            /**
             * Get the action that is referred to by this action for the entities.
             */
            PFAction objEntityAction = resolveEntityAction(pobjAction);
            if (objEntityAction == null) {
                throw new ZXException("Entity action not found.", pobjAction.getEntityaction());
            }
            
            colEntities = objEntityAction.getEntities();
            
            if (colEntities == null) {
                throw new ZXException("Entities collection for action is not defined.", pobjAction.getName());
            }
            
            if (colEntities.size() == 0) {
                /** 
                 * Null actions often do not have any entities associated with them, some as layout actions.
                 **/
                if (penmActionType.equals(zXType.pageflowActionType.patNull)
                    || penmActionType.equals(zXType.pageflowActionType.patLayout)) {
                    getEntityCollection = new ZXCollection();
                    return getEntityCollection;
                    
                }
                
                throw new ZXException("Entities collection for action has no entries.", pobjAction.getName());
            }
            
            PFEntity objEntity;
            Iterator iter;
            
            /**
             * The next step is to resolve all the entities that use the
             * parameterBagEntry option
             **/
            iter = colEntities.iterator();
            ZXCollection colTmp = new ZXCollection(colEntities.size());
            while (iter.hasNext()) {
            	objEntity = (PFEntity)iter.next();
            	
            	if (objEntity.getName().toLowerCase().startsWith("#pb.")) {
            		
            		String strParamBag = objEntity.getName().substring(4);
            		objEntity = resolveParameterDirectorAsEntity(strParamBag);
	                if (objEntity == null) {
	                	throw new RuntimeException("Unable to resolve director as entity parameterBagEntry : " + strParamBag);
	                } // Unable to resolve reference
	                
            	} // Reference to parameterBagEntry
            	
        		colTmp.put(objEntity.getName(), objEntity);
        		
            } // Resolve any parameterBagEntries
            
            colEntities = colTmp;
            
            if (penmQueryType.equals(zXType.pageflowQueryType.pqtAssociatedWith)
            	|| penmQueryType.equals(zXType.pageflowQueryType.pqtNotAssociatedWith)) {
                /**
                 * Add the left / middle and right entities to the collection
                 */
                PFQuery objQuery = (PFQuery)pobjAction;
                objEntity = (PFEntity)colEntities.get(objQuery.getEntityl());
                if (objEntity != null) {
                    getEntityCollection.put(objEntity.getName(), objEntity);
                }
                
                objEntity = (PFEntity)colEntities.get(objQuery.getEntitym());
                if (objEntity != null) {
                    getEntityCollection.put(objEntity.getName(), objEntity);
                }
                
                objEntity = (PFEntity)colEntities.get(objQuery.getEntityr());
                if (objEntity != null) {
                    getEntityCollection.put(objEntity.getName(), objEntity);
                }
                
            } else {
                if (pobjAction.getEntitynames() == null || pobjAction.getEntitynames().size() == 0) {
                    /**
                     * There are no entitynames given so use all entities in the
                     * collection
                     */
                    iter = colEntities.iterator();
                    while (iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        
                        if (relevantEntity(objEntity, penmActionType, penmQueryType)) {
                            getEntityCollection.put(objEntity.getName(), objEntity);
                        }
                    }
                    
                } else {
                    /**
                     * Entity names are given so use only the ones that are available
                     */
                    Tuple objEntityName;
                    
                    iter = pobjAction.getEntitynames().iterator();
                    while (iter.hasNext()) {
                        objEntityName = (Tuple)iter.next();
                        
                        objEntity = (PFEntity)colEntities.get(objEntityName.getValue() );
                        if (objEntity == null) {
                            throw new ZXException("Entity name not found in collection.", objEntityName.getValue());
                        }
                        
                        if (relevantEntity(objEntity, penmActionType, penmQueryType)) {
                            getEntityCollection.put(objEntity.getName(), objEntity);
                        }
                        
                    }
                    
                }
                
            }
            
            /**
             * DGSOCT2004: Added Calendar action type
             */
            iter = getEntityCollection.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                if (penmActionType.equals(zXType.pageflowActionType.patQuery)
                    || penmActionType.equals(zXType.pageflowActionType.patListForm)
                    || penmActionType.equals(zXType.pageflowActionType.patSearchForm)
                    || penmActionType.equals(zXType.pageflowActionType.patCalendar)) {
                    
                	rc = resolveEntity(objEntity, penmActionType, true, false);
                    
                } else if (penmActionType.equals(zXType.pageflowActionType.patCreateUpdate) 
                           || penmActionType.equals(zXType.pageflowActionType.patGridCreateUpdate)) {
                    
                    rc = resolveEntity(objEntity, penmActionType, true, false);
                    
                    /**
                     * Bit weird but since createUpdate and editForm might
                     * be called in sequence without unloading the process
                     * (this in case of erroneous input), the persist status
                     * may not be what is expected by clsPFEditForm.go
                     * DGS27JUN2003: Changes to sub actions. Now uses string, and can combine sub actions
                     * in more ways.
                     */
                    if (editSubActionIncludes(pstrSubAction, "u")) {
                        objEntity.getBosaver().setPersistStatus(zXType.persistStatus.psDirty);
                        objEntity.getBo().setPersistStatus(zXType.persistStatus.psDirty);
                    }
                    
                } else if (penmActionType.equals(zXType.pageflowActionType.patEditForm)) {
                    
                    if (editSubActionIncludes(pstrSubAction, "s")) {
                        rc = resolveEntity(objEntity, penmActionType, true, true);
                    } else {
                        rc = resolveEntity(objEntity, penmActionType, true, false);
                    }
                    
                } else {
                    rc = resolveEntity(objEntity, penmActionType, true, false);
                    
                }
                
                if (rc.pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to resolve single entity.", objEntity.getName());
                }
            }
            
            /**
             * Finished building the collection of entities for this actions,
             * now we need to populate them.
             */
            if (getEntityCollection.size() == 0) {
                if (penmActionType.equals(zXType.pageflowActionType.patNull)
                    || penmActionType.equals(zXType.pageflowActionType.patLayout)) {
                    return getEntityCollection; // Return an empty collection.
                }
                
                throw new ZXException("No entitities found after all that hard work.");
            }
            
            /**
             * Set the bo context as soon as possible so that in resolveEntity you can use the bo conext.
             * eg : get a attr value from a business object.
             * 
             * Also these are the only entities that are used in this pageflow.
             * 
             * Add the entities to the zX BO context; make sure that we do start with
             *  a fresh and empty collection
             * 
             * VERY IMPORTANT CHANGE BD29NOV03
             * We used to clear the BO context as otherwise #pk or #attrValue (without specifying
             * the entity) would give unexpected results. WEe have now decided that this is very
             * bad practice anyway and so we may as well leave the context intact. This has big
             * advantages when having a number of linked actions
             *
             * zX.BOContext.Clear
             * 
             * BD11FEB2005: This previously looped through each entity in colEntities, but in fact
             * we should only loop through each one in colReturn. Quite a fundamental change.
             */
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                getZx().getBOContext().setEntry(objEntity.getName(), objEntity.getBo());
            }

            /**
             * Resolve any directors associated with entity group names.
             * DGS02FEB2004: Now use a called function that doesn't overwrite 'original' values
             * by the resolved directors so that they can be re-evaluated later. 
             * 
             * BD11FEB2005: This previously looped through each entity in colEntities, but in fact
             * we should only loop through each one in colReturn. See just above too.
             */
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                resolveGroups(objEntity);
            }
            
            return getEntityCollection;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get collection of relevant entities.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjAction = "+ pobjAction);
                getZx().log.error("Parameter : penmActionType = "+ penmActionType);
                getZx().log.error("Parameter : penmQueryType = "+ penmQueryType);
                getZx().log.error("Parameter : pstrSubAction = "+ pstrSubAction);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getEntityCollection;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(getEntityCollection);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
	 * Resolve attribute groups without overwriting 'original' unresolved
	 * values.
	 * 
	 * @param pobjEntity The entity to resolve.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if resolveGroups fails.
	 */
	public zXType.rc resolveGroups(PFEntity pobjEntity) throws ZXException {
		if (getZx().trace.isFrameworkTraceEnabled()) {
			getZx().trace.enterMethod();
		}
		
		zXType.rc resolveGroups = zXType.rc.rcOK;
		
		try {
			/**
			 * DGS20FEB2004: Use pageflow resolveDirector not zX.
			 */
			pobjEntity.resolveSelectlistgroup(resolveDirector(pobjEntity.getSelectlistgroupOrig()));
			pobjEntity.resolveSelecteditgroup(resolveDirector(pobjEntity.getSelecteditgroupOrig()));
			pobjEntity.resolveWheregroup(resolveDirector(pobjEntity.getWheregroupOrig()));
			pobjEntity.resolveLockgroup(resolveDirector(pobjEntity.getLockgroupOrig()));
			pobjEntity.resolveListgroup(resolveDirector(pobjEntity.getListgroupOrig()));
			pobjEntity.resolveVisiblegroup(resolveDirector(pobjEntity.getVisiblegroupOrig()));
			pobjEntity.resolveCopygroup(resolveDirector(pobjEntity.getCopygroupOrig()));
			pobjEntity.resolvePkwheregroup(resolveDirector(pobjEntity.getPkwheregroupOrig()));
			pobjEntity.resolveGroupbygroup(resolveDirector(pobjEntity.getGroupbygroupOrig()));

			return resolveGroups;
		} catch (Exception e) {

			getZx().trace.addError("Failed to : Resolve attribute groups.", e);

			if (getZx().throwException) throw new ZXException(e);
			resolveGroups = zXType.rc.rcError;
			return resolveGroups;
		} finally {
			if (getZx().trace.isFrameworkTraceEnabled()) {
				getZx().trace.returnValue(resolveGroups);
				getZx().trace.exitMethod();
			}
		}
	}  
    
    /**
	 * Load the 'other' pageflow but make sure not to load it when there is no
	 * need.
	 * 
	 * BD17JAN04 - V1.4:19 Use static local instance of HTML; because of the
	 * completely absurd way of initialising a pageflow (for which I apologise)
	 * we have to create another instance of HTML as the HTML has a handle to
	 * pageflow again....
	 * 
	 * @param pstrPageflow The name of the other pageFlow.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if getOtherPageflow fails.
	 */
    public zXType.rc getOtherPageflow(String pstrPageflow) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrPageflow", pstrPageflow);
        }
        
        zXType.rc getOtherPageflow = zXType.rc.rcOK;
        
        try {
            
            /**
             * Perhaps the other pageflow has already been loaded
             */
            if (this.otherPageflow != null) {
                if (this.otherPageflow.getPFDesc().getName().equalsIgnoreCase(pstrPageflow)) {
                    return getOtherPageflow;
                }
            }
            
            /**
             * Create and initialise
             */
            this.otherPageflow = this.page.getPageflow(this.request, this.response, pstrPageflow);
            
            if (this.otherPageflow == null) {
                throw new Exception("Failed to get the other pageflow : " + pstrPageflow);
            }
            
            /**
             * Try to be clever: a very likely scenario is that the other pageflow
             * needs to refer to this pageflow so set its other pageflow to me
             */
            this.otherPageflow.setOtherPageflow(this);
            
            return getOtherPageflow;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Load the 'other' pageflow but make sure not to load it when there is no need.", e);
                getZx().log.error("Parameter : pstrPageflow = "+ pstrPageflow);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            getOtherPageflow = zXType.rc.rcError;
            return getOtherPageflow;
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(getOtherPageflow);
                getZx().trace.exitMethod();
            }
            
        }
    }
    
    /**
     * Kick-off the pageflow action!.
     *
     *<pre>
     *
     * This can call an external pageflow as will, the URL has to have the <pageflow>:<action> syntax.
     *</pre>
     *
     * @param pstrAction  The Pageflow action to perform.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if go fails. 
     */
    public zXType.rc go(String pstrAction) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrAction", pstrAction);
        }
        
        zXType.rc go = zXType.rc.rcOK; 

        try {
            // Set the current action.
            setAction(pstrAction);
            
            /**
             * If no action passed: see if we can get it from the querystring
             */
            if (StringUtil.len(getAction()) == 0) {
                // If it is not set then use the -a parameter.
            	setAction(this.request.getParameter("-a")); 
                
                /**
                 * If still no action: try start action
                 */
                if (StringUtil.len(getAction()) == 0) {
                	setAction(this.PFDesc.getStartaction());
                }
                
                /**
                 * Now it is time to give up and assume that the calling ASP/JSP/Servlet/Other? is handling
                 *  the action
                 */
                if (StringUtil.len(getAction()) == 0) {
                    go = zXType.rc.rcWarning;
                    return go;
                }
            }
            
            PFAction objAction;
            int intLoopCounter = 0;
            
            while (StringUtil.len(getAction()) > 0 && intLoopCounter < 10) {
                /**
                 * It can be that we have an external pageflow action reference
                 */
                if (this.action.indexOf(':') != -1) {
                    /**
                     * Get pageflow part and action part
                     */
                    String[] arrAction = StringUtil.split(":", this.action);
                    String strPageflow = arrAction[0];
                    this.action = arrAction[1];
                    
                    /**
                     * Make sure we did not end up with non-sense
                     */
                    if (StringUtil.len(strPageflow) == 0 || StringUtil.len(this.action) ==0) {
                        throw new Exception("URL has not got the <pageflow>:<action> syntax : " + pstrAction);
                    }
                    
                    /**
                     * Switch pageflow
                     */
                    switchPageflow(strPageflow);
                }
                
                /**
                 * Get handle to action
                 */
                objAction = this.PFDesc.getAction(getAction());
                if (objAction == null) {
                    /**
                     * This is NO problem as it could very well be that the action is implemented
                     * in the ASP/JSP/Servlet/Other. page
                     */
                    go = zXType.rc.rcWarning;
                    return go;
                    
                }
                
                /**
                 * And go!
                 */
                if (dispatchAction(objAction).pos != zXType.rc.rcOK.pos) {
                	/**
                	 * Anything other than ok is assumed to be an error.
                	 */
                	getZx().trace.addError("Unable to dispatch action", getAction());
                	go = zXType.rc.rcError;
                	return go;
                }
                
                intLoopCounter++;
            }
            
            /**
             * Handle debug messages; note that debug messages are first
             * stored in html.debugMsg and should now be added to the
             * proper HTML
             */
            if (this.isDebugOn()) {
                // MERGE : May need to merge later on.
                // This is handles elsewhere so long : 
                // this.page.debugMsg("Server time (ms): " + CStr(zXInclude.GetTickCount - Me.startClickCount));
//                this.page.s.append("<span class=\"zxDebugPFBlockTitle\">Debug Messages</span>").append(HTMLGen.NL);
//                this.page.s.append("<div class=\"zxDebugPFBlock\"").append("> ").append(HTMLGen.NL);
                this.page.s.append(this.page.getDebugHTML()).append(HTMLGen.NL);
//                this.page.s.append("</div>").append(HTMLGen.NL);
                
                this.page.setDebugHTML(new StringBuffer(""));
            }
            
            return go;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Kick-off the pageflow action!.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrAction = "+ pstrAction);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            go = zXType.rc.rcError;
            return go;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(go);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Execute a single action.
     *
     * Reviewed for V1.5:1
     * 
     * @param pobjAction The pageflow action to perform.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if dispatchAction fails. 
     */
    public zXType.rc dispatchAction(PFAction pobjAction) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAction", pobjAction);
        }
        
        zXType.rc dispatchAction = zXType.rc.rcOK;
        
        try {
        	/**
        	 * Save original pageflow name; this may change in the go action as a result of an
        	 * external pageflow link (ie otherPageflow:query) and yet we need to work with the
        	 * pageflow when we handle the popups. You could argue that the resolveLink should be
        	 * done here (and as very last) or the processPopup in the respective go actions. Well,
        	 * hindsight.....
        	 */
        	String strOrgPF = getPageflow();
        	
            /**
             * Make this action the current context action
             */
            setContextAction(pobjAction);
            
            /**
             * Do context update
             */
            processContextUpdate(pobjAction.getContextupdate());
            
            String strAlternateConnection = resolveDirector(pobjAction.getAlternateconnection());
            if (StringUtil.len(strAlternateConnection) > 0) {
                /**
                 * Alternate connections should no longer be used in the post data-source era
                 */
                if (getZx().zXVersionSupport(1, 5)) {
                    throw new ZXException("Alternate connection no longer spported since 1.5; use data-source instead",
                                          strAlternateConnection);
                }
            }
            
            String debugStlye = "";
            if (this.isDebugOn()) {
                
                // Used to visually denote pageflow actions on a page.
                this.page.s.append("<span class=\"zxDebugPFBlockTitle\">").append(pobjAction.getName()).append("</span>").append(HTMLGen.NL);
                debugStlye = " class=\"zxDebugPFBlock\"";
                
                // Denote the start of the pageflow action messages.
                this.page.getDebugHTML().append("<span class=\"zxDebugPFBlockTitle\">").append(pobjAction.getName()).append("</span>").append(HTMLGen.NL);
                this.page.getDebugHTML().append("<div class=\"zxDebugPFBlock\">").append(HTMLGen.NL);
                
                // MERGE : Possible merge
                //this.getPage().debugMsg("Start of action " + pobjAction.getName());
                //this.getPage().debugMsg(this.qs.dump());
            }
            
            this.page.s.append("<div id=\"").append(pobjAction.getName()).append("\" ").append(debugStlye).append("> ").append(HTMLGen.NL);
            
            processHTMLTags(pobjAction);
            
            /**
             * Set the action context (can be used in pre/post persist)
             */
            getZx().setActionContext(pageflow + "." + pobjAction.getName());
            
            /**
             * Handle BO context
             */
            if (pobjAction.getBOContexts() != null && pobjAction.getBOContexts().size() > 0) {
                processBOContexts(pobjAction);
            }
            
            /**
             * Process any menubar (aligned top) ref buttons
             */
            processMenuBar(pobjAction);
            
            /**
             * And go!
             */
            dispatchAction = pobjAction.go();
            
            /**
             * Framehandling
             */
            processFramehandling(pobjAction.getFrameHandling());
            
            /**
             * DGS28FEB2003: Generate code for any popups
             */
            
            /**
             * If the pageflow had changed (see comment above), set the old pageflow in qs as -pf
             * and restore afterwards
             **/
            String strNewPF = "";
            if (!strOrgPF.equals(getPageflow())) {
                strNewPF = getPageflow();
                getZx().getQuickContext().setEntry("-pf", strOrgPF);
            }
            
            processPopups(pobjAction);
            
            /**
             * And restore again
             **/
            if (StringUtil.len(strNewPF) > 0) {
                getZx().getQuickContext().setEntry("-pf", strNewPF);
            }
            
            /**
             * 11MAY2004: Generate follow on action javascript
             */
            int intActions = pobjAction.getActions().size();
            
            if (intActions > 0) {
                this.page.s.append("<script type=\"text/javascript\" language=\"JavaScript\">").append(HTMLGen.NL);
                PFRef objRef;
                for (int i = 0; i < intActions; i++) {
                    objRef = (PFRef)pobjAction.getActions().get(i);
                    
                    if (isActive(objRef.getUrl().getActive())) {
                        this.page.s.append(
                                            wrapRefUrl(resolveDirector( objRef.getUrl().getFrameno()).toLowerCase(), 
                                            constructURL(objRef.getUrl()))
                                          ).append(HTMLGen.NL);
                    }
                }
                
                this.page.s.append("</script>").append(HTMLGen.NL);
            }
            
            /**
             * Handle any Javascript tags
             */
            processJavascriptTags(pobjAction);
            
            /**
             * BD9JAN04 Process the optional post Javascript commands that
             * may have been collected by the HTML object under obscure
             * circumstances like dependancy enhancers
             */
            int size = this.page.getPostLoadJavascript().size();
            if (size > 0) {
                this.page.s.append("<script language='Javascript'>").append(HTMLGen.NL);
                String strJavascriptCommand;
                
                for (int i = 0; i < size; i++) {
                    strJavascriptCommand = (String)this.page.getPostLoadJavascript().get(i);
                    this.page.s.appendNL(strJavascriptCommand);
                }
                
                this.page.s.append("</script>").append(HTMLGen.NL);
                
                /**
                 * And clear
                 */
                this.page.setPostLoadJavascript(new ArrayList());
            }
            
            if (this.isDebugOn()) {
                // Used to denote the end of debug messages.
                // MERGE : Could be merged.
                // this.getPage().debugMsg("End of action " + pobjAction.getName() + this.getPage().br());
            	//this.getPage().debugMsg(this.qs.dump());
            	
                this.page.getDebugHTML().append("</div>").append(HTMLGen.NL);
            }
            
            /**
             * Keep html clean effort :).
             * PFStdPopup generates framesets.
             */
            if ( !(pobjAction instanceof PFStdPopup) ) {
                this.page.s.append("</div>").append(HTMLGen.NL);
            }
            
            return dispatchAction;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Execute a single action.", e);
                getZx().log.error("Parameter : pobjAction = "+ pobjAction);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            dispatchAction = zXType.rc.rcError;
            return dispatchAction;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(dispatchAction);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Create or load the BO for an entity.
     *
     * <pre>
     * 
     * 1.5:18 - BD17JUN05 - Support for property persistStatus
     * </pre>
     * 
     * @param pobjEntity The entity you want to resolve.
     * @param penmActionType The pageflow action type.
     * @param pblnCreateBO  Whether to create the business object.
     * @param pblnLoadBO Whether to load values into the business object.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if resolveEntity fails. 
     */
    public zXType.rc resolveEntity(PFEntity pobjEntity, 
                                   zXType.pageflowActionType penmActionType, 
                                   boolean pblnCreateBO, 
                                   boolean pblnLoadBO) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjEntity", pobjEntity);
            getZx().trace.traceParam("penmActionType", penmActionType);
            getZx().trace.traceParam("pblnCreateBO", pblnCreateBO);
            getZx().trace.traceParam("pblnLoadBO", pblnLoadBO);
        }

        zXType.rc resolveEntity = zXType.rc.rcOK;
        
        try {
            /**
             * First determine the attribute group that determines whether an entity
             * needs to be considered (eg ignore entity with no wheregroup if the
             * action is search)
             */
            String strRelevantGroup = "";
            if (penmActionType.equals(zXType.pageflowActionType.patSearchForm)) {
                strRelevantGroup = pobjEntity.getWheregroup();
            } else {
                strRelevantGroup = pobjEntity.getSelecteditgroup();
            }
            
            if (StringUtil.len(pobjEntity.getRefboaction()) > 0) {
                /**
                 * Added refBoAction. This allows 'local' entity definitions whilst
                 * sharing a business object + descriptor of another action / entity
                 */
                PFEntity objOtherEntity = getEntity(pobjEntity.getRefboaction(), pobjEntity.getName());
                if (objOtherEntity == null) {
                    throw new Exception("Entity not found Action " 
                                        + pobjEntity.getRefboaction() + ", entity " 
                                        + pobjEntity.getName());
                }
                
                pobjEntity.setBo(objOtherEntity.getBo());
                pobjEntity.setBODesc(objOtherEntity.getBODesc());
                pobjEntity.setBosaver(objOtherEntity.getBosaver());
                
            } else {
                /**
                 * Create instance of BO descriptor and assign this to the entity
                 * if this was not already done
                 */
                if (pobjEntity.getBODesc() == null) {
                	int intEntitymassagers = pobjEntity.getEntitymassagers() != null?pobjEntity.getEntitymassagers().size():0;
                	
                    if (StringUtil.len(pobjEntity.getAlias()) > 0) {
                        pobjEntity.setBo(getZx().createBO(resolveDirector(pobjEntity.getEntity()), true));
                    } else {
                        /**
                         * Use a private descriptor, to prevent problems caused by massaging and descriptor caching
                         */
                        pobjEntity.setBo(getZx().createBO(resolveDirector(pobjEntity.getEntity()), intEntitymassagers > 0));
                    }
                    if (pobjEntity.getBo() == null) {
                        throw new Exception("Unable to create entity BO : " 
                                            + resolveDirector(pobjEntity.getEntity()));
                    }
                    
                    /**
                     * BD26NOV02 Reset new objects except when for a search form as this would
                     * result in some of the search form entries already set
                     */
                    if (!penmActionType.equals(zXType.pageflowActionType.patSearchForm)
                        && !penmActionType.equals(zXType.pageflowActionType.patCreateUpdate)) {
                        pobjEntity.getBo().resetBO();
                    }
                    
                    if (StringUtil.len(pobjEntity.getAlias()) > 0) {
                        pobjEntity.getBo().getDescriptor().setAlias(pobjEntity.getAlias());
                    }
                    
                    /**
                     * DGS14APR2003: New property of 'Entity Massagers'. Allows some BO descriptor
                     * properties to be overridden at run time.
                     * 
                     * DGS16APR2003: Use CallByName so that any property can be used. Allow it to
                     * fail silently on error. Only change the error handling if definitely have
                     * a massager, as there is a performance hit.
                     */
                    if (intEntitymassagers > 0) {
                        PFEntityMassager objEntityMassager;
                        Object obj = pobjEntity.getBo().getDescriptor();
                        
                        /**
                         * Massaged properties - assume the user knows what they are doing and
                         * wants to override a property of the entity or attribute
                         */
                        for (int i = 0; i < intEntitymassagers; i++) {
                            objEntityMassager = (PFEntityMassager)pobjEntity.getEntitymassagers().get(i);
                            
                            /**
                             * Massaging a attribute of a entity
                             */
                            if (StringUtil.len(objEntityMassager.getAttr()) > 0) {
                                obj = ((Descriptor)obj).getAttribute(objEntityMassager.getAttr());
                            }
                            
                            try {
                                String strName = objEntityMassager.getProperty();
                                String strValue = resolveDirector(objEntityMassager.getValue());
                                
                                BeanUtils.setProperty(obj, strName, strValue);
                                
                            } catch (Exception e) {
                            	/**
                            	 * Ignore any errors but do trace any error messages
                            	 */
                                getZx().trace.addError("Failed to call setter for the descriptor.", e);
                            }
                        }
                    }
                    
                    pobjEntity.setBODesc(pobjEntity.getBo().getDescriptor());
                    pobjEntity.setBosaver(pobjEntity.getBo().cloneBO(""));
                    pobjEntity.getBosaver().setValidate(false);
                    
                }
                
            }
            
            /**
             * Since this is called at start of an action, we should restore the
             * property persistStatus
             */
            pobjEntity.getBo().setPropertyPersistStatus("*", zXType.propertyPersistStatus.ppsNew, null);
            pobjEntity.getBosaver().setPropertyPersistStatus("*", zXType.propertyPersistStatus.ppsNew, null);
            
            /**
             * If creation of a BO was requested: do so if not already done
             */
            if (pblnCreateBO) {
                if (pobjEntity.getBo() == null) {
                    pobjEntity.setBo(getZx().createBO(resolveDirector(pobjEntity.getEntity())));
                    if (pobjEntity.getBo() == null) {
                        throw new Exception("Unable to create entity BO " 
                                            + resolveDirector(pobjEntity.getEntity()));
                    }
                    
                    /**
                     * Save a copy and turn of the validation, this is used in the failed web forms
                     */
                    pobjEntity.setBosaver(pobjEntity.getBo().cloneBO());
                    pobjEntity.getBosaver().setValidate(false);
                }
                
                /**
                 * And get DS handler if not already done
                 */
                if (pobjEntity.getDSHandler() == null) {
                    pobjEntity.setDSHandler(pobjEntity.getBo().getDS(false));
                    if (pobjEntity.getDSHandler() == null) {
                        throw new ZXException("Unable to get data-source handler for BO", 
                                              pobjEntity.getBo().getDescriptor().getName());
                    }
                }
            }
            
            /**
             * BD13NOV02 Make sure not to load twice
             */
            if (pblnLoadBO && !pobjEntity.isLoaded()) {
                /**
                 * Also load the business object if the action requires so
                 */
                pobjEntity.getBo().setPKValue(resolveDirector(pobjEntity.getPk()));
                pobjEntity.getBo().loadBO(resolveDirector(strRelevantGroup));
                
                /**
                 * BD13NOV02 Mark as loaded
                 */
                pobjEntity.setLoaded(true);
            }
            
            return resolveEntity;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Create / load the BO for an entity.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjEntity = "+ pobjEntity);
                getZx().log.error("Parameter : penmActionType = "+ penmActionType);
                getZx().log.error("Parameter : pblnCreateBO = "+ pblnCreateBO);
                getZx().log.error("Parameter : pblnLoadBO = "+ pblnLoadBO);
            }
            if (getZx().throwException) throw new ZXException(e);
            resolveEntity = zXType.rc.rcError;
            return resolveEntity;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(resolveEntity);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
	 * Get entry from context and prefix key with name of pageflow.
	 * 
	 * @param pstrName Name of the key.
	 * @return Returns the entry from context and prefix key with name of
	 *         pageflow.
	 * @throws ZXException Thrown if getFromContext fails.
	 */
	public String getFromContext(String pstrName) throws ZXException {
		return getFromContext(pstrName, false);
	}
    
    /**
	 * Get entry from context and prefix key with name of pageflow.
	 * 
	 * @param pstrName The key to use to retrive the value from context.
	 * @param pblnBaseContext Whether to get from the base context. 
	 * 						  Optional, default should be false.
	 * @return Returns the entry from context and prefix key with name of pageflow.
	 * @throws ZXException Thrown if getFromContext fails.
	 */
    public String getFromContext(String pstrName, boolean pblnBaseContext) throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pblnBaseContext", pblnBaseContext);
        }
        
        String getFromContext = null; 
        try {
            
            /**
             * If not from base context, we prefix the context variable name with
             * pageflow and subsession name
             */
            if (pblnBaseContext) {
                getFromContext = getZx().getSession().getFromContext(pstrName);
            } else {
                getFromContext = getZx().getSession().getFromContext(this.pageflow + this.qs.getEntryAsString("-ss") + pstrName);
            }
            
            return getFromContext;
            
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Get entry from context and prefix key with name of pageflow.", e);
                getZx().log.error("Parameter : pstrName = "+ pstrName);
                getZx().log.error("Parameter : pblnBaseContext = "+ pblnBaseContext);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getFromContext;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getFromContext);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Process any attribute values that are associated with an entity.
     *
     * @param pobjEntity The PFEntity 
     * @return Returns null, nothing ? 
     * @throws ZXException Thrown if processAttributeValues fails. 
     */
    public zXType.rc processAttributeValues(PFEntity pobjEntity) throws ZXException {
       return processAttributeValuesGeneric(pobjEntity.getBo(), pobjEntity.getAttrvalues());
    }
    
    /**
     * Process any attribute values that are associated with an entity.
     *
     * @param pobjBO The PFEntity 
     * @param pcolAttrValues The list of attributes to process. 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if processAttributeValues fails. 
     */
    public zXType.rc processAttributeValuesGeneric(ZXBO pobjBO, List pcolAttrValues) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjEntity", pobjBO);
        }
        
        zXType.rc processAttributeValues = zXType.rc.rcOK; 
        
        /**
         * Save validation settings
         */
        boolean blnValidate = pobjBO.isValidate();
        
    	/**
    	 * Exit early if no attribute values.
    	 */
        if (pcolAttrValues == null) return processAttributeValues;
        int intAttrvalues = pcolAttrValues.size();
        if (intAttrvalues == 0) return processAttributeValues;
        
        try {
            /**
             * DGS24NOV2003: Make sure validation of the BO is off before trying to set attrs
             *  - it is quite possible that some of the values we try to set are null for example
             *  in non-optional attrs. Preserve current setting of validate just in case.
             */
            pobjBO.setValidate(false);
            
            /**
             * Only bother when there is a attrvalue above 0
             */
            PFDirector objDirector;
            String strAttr;
            String[] arrAttr;
            String strValue = "";
            Attribute objAttr;
            int intRC = zXType.rc.rcOK.pos;
            
            for (int i = 0; i < intAttrvalues; i++) {
                objDirector = (PFDirector)pcolAttrValues.get(i);
                
                strAttr = objDirector.getDestination();
                
                /**
                 * Special case
                 */
                if (strAttr.startsWith("#descattr")) {
                    /**
                     * #descattr.xxxxx¬yyyy
                     * where xxxxx is the attribute name and yyyy is the property
                     */
                    strAttr = strAttr.substring(strAttr.indexOf('.')); // xxxxx¬yyyy
                    arrAttr = StringUtil.split("¬", strAttr); // arrAttr[0] = xxxxx; arrAttr[1] = yyyy 
                    
                    strValue = resolveDirector(objDirector.getSource());
                    
                    objAttr = pobjBO.getDescriptor().getAttribute(arrAttr[0]);
                    if (objAttr == null) {
                        throw new Exception ("Attribute not found : " + strAttr);
                        
                    }
                    
                    if (arrAttr[1].equalsIgnoreCase("locked")) {
                        if (strValue.equalsIgnoreCase("t")) {
                            objAttr.setLocked(true);
                        } else {
                            objAttr.setLocked(false);
                        }
                    }
                    
                } else {
                	/**
                	 * Resolve the attribute name : 
                	 */
                    strAttr = resolveDirector(objDirector.getDestination());
                    if (StringUtil.len(strAttr) > 0) {
                        /**
                         * Attribute name can evaluate to nullstring; this is interpreted
                         * as not interested in updating anything...
                         */
                        boolean blnNull = false;
                        if (objDirector.getSource().equalsIgnoreCase("#empty") || objDirector.getSource().equalsIgnoreCase("#null")) {
                            blnNull = true;
                        } else {
                            /**
                             * BD24JAN05 - V1.4:27 Also set to false otherwise it will
                             * stay true once set....
                             * 
                             * TODO : What if the expression returns a null value, so we really
                             * do not want to return if as not null.?
                             */
                            strValue = resolveDirector(objDirector.getSource());
                            blnNull = false;
                        }
                        
                        try {
                            intRC = pobjBO.setValue(strAttr, strValue, false, blnNull).pos;
                        } catch (Exception e) {
                            // Ignore errors for now
                        }
                        
                        if (intRC == zXType.rc.rcWarning.pos) {
                        	// Ignored case
                        } else if (intRC == zXType.rc.rcError.pos){
                            processAttributeValues = zXType.rc.rcError;
                        }
                        
                        /**
                         * Old style
                         */
//                            int dataType = pobjEntity.getBo().getDescriptor().getAttribute(strAttr).getDataType().pos;
//                            if (dataType == zXType.dataType.dtAutomatic.pos
//                               || dataType == zXType.dataType.dtLong.pos) {
//                                
//                                if (blnNull) {
//                                    intRC = pobjEntity.getBo().setValue(strAttr, new LongProperty(0, true)).pos;
//                                } else if (StringUtil.isNumeric(strValue)){
//                                    intRC = pobjEntity.getBo().setValue(strAttr, strValue, false, blnNull).pos;
//                                }
//                                
//                            } else if (dataType == zXType.dataType.dtBoolean.pos) {
//                                intRC = pobjEntity.getBo().setValue(strAttr, strValue, false, blnNull).pos;
//                                
//                            } else if (dataType == zXType.dataType.dtDate.pos
//                                     || dataType == zXType.dataType.dtTime.pos
//                                     || dataType == zXType.dataType.dtTimestamp.pos) {
//                                if (blnNull) {
//                                    intRC = pobjEntity.getBo().setValue(strAttr, new DateProperty(new Date(), true)).pos;
//                                } else if (DateUtil.isValid(strValue, getZx().getDb().getDateFormat())){
//                                    intRC = pobjEntity.getBo().setValue(strAttr, strValue, false, blnNull).pos;
//                                }
//                                
//                            } else if (dataType == zXType.dataType.dtDouble.pos) {
//                                if (blnNull) {
//                                    intRC = pobjEntity.getBo().setValue(strAttr, new DoubleProperty(0, true)).pos;
//                                } else if (StringUtil.isDouble(strValue)){
//                                    intRC = pobjEntity.getBo().setValue(strAttr, strValue, false, blnNull).pos;
//                                }
//                                
//                            } else if (dataType == zXType.dataType.dtString.pos) {
//                                intRC = pobjEntity.getBo().setValue(strAttr, strValue, false, blnNull).pos;
//                            }
                        
                    } // strAttr > 0?
                    
                } // expression or not?
                
            }
            
            return processAttributeValues;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process any attribute values that are associated with an entity.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pcolAttrValues = " + pcolAttrValues);
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            processAttributeValues = zXType.rc.rcError;
            return processAttributeValues;
        } finally {
        	/**
        	 * Restore bo validation value
        	 */
        	pobjBO.setValidate(blnValidate);
        	
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processAttributeValues);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
	 * Show any error / warning messages that may been outstanding.
	 * 
	 * @param pobjAction The Pageflow action linked to these message. Optional and can be null.
	 * @throws ZXException Thrown if processMessages fails.
	 */
	public void processMessages(PFAction pobjAction) throws ZXException {
		if (getZx().trace.isFrameworkTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjAction", pobjAction);
		}

		try {

			/**
			 * Collect messages
			 */
			collectMessages(pobjAction);

			/**
			 * Generate messages
			 */
			if (StringUtil.len(this.alertMsg) > 0)
				this.page.alertMsg(this.alertMsg);
			if (StringUtil.len(this.infoMsg) > 0)
				this.page.infoMsg(this.infoMsg);
			if (StringUtil.len(this.errorMsg) > 0)
				this.page.errorMsg(this.errorMsg);

			/**
			 * Generate narrative, but only if there is one, and it doesn't
			 * evaluate to the empty string.
			 */
			if (pobjAction != null) {
				if (pobjAction.getNarr().size() > 0) {
					String strNarr;
					if (pobjAction.isNarrisdir()) {
						strNarr = resolveLabel(pobjAction.getNarr());
					} else {
						strNarr = getLabel(pobjAction.getNarr());
					}
					if (StringUtil.len(strNarr) > 0) {
						this.page.narrative(strNarr);
					}
				}
			}

			/**
			 * And reset
			 */
			this.infoMsg = null;
			// this.alertMsg = null;
			this.errorMsg = null;

		} catch (Exception e) {
			if (getZx().log.isErrorEnabled()) {
				getZx().trace.addError("Failed to : Show any error / warning messages that may been outstanding.", e);
				getZx().log.error("Parameter : pobjAction = " + pobjAction);
			}
			
			if (getZx().throwException)throw new ZXException(e);
		} finally {
			if (getZx().trace.isFrameworkTraceEnabled()) {
				getZx().trace.exitMethod();
			}
		}
	}   
     
	/**
	 * New code for popups process actions popups (if any).
	 * 
	 * <pre>
	 * 
	 * DGS28FEB2003: New code for popups
	 * process actions popups (if any)
	 * BD16JUN06 - Reviewed for V1.5:101
	 * 
	 * Tricky: this routine is called from dispatchAction and is thus process after the go method
	 * of the action. In the go action, we do resolveLink; this can point to an external pageflow
	 * in which case we set -pf to that of the external pageflow.
	 * When we are dealing with popup options, we must however use the 'old' -pf value
	 * 
	 * </pre>
	 * 
	 * @param pobjAction The actoin associated.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if processPopups fails.
	 */
	public zXType.rc processPopups(PFAction pobjAction) throws ZXException {
          if(getZx().trace.isFrameworkTraceEnabled()) {
              getZx().trace.enterMethod();
              getZx().trace.traceParam("pobjAction", pobjAction);
          }
          
          zXType.rc processPopups = zXType.rc.rcOK; 

          try {
              
              /**
               * If no popups, exit without further ado
               */
              if (pobjAction.getPopups().size() == 0) {
                  return processPopups;
              }
              
              String strName = "";
              
              /**
               * Construct name used for the Javascript components
               */
              String strActionName = StringEscapeUtils.escapeHTMLTag(pobjAction.getName());
              
              /**
               * DGS28FEB2003: Adjusted this code so that a popup menu is always generated, even if
               * no actions are active. Prevents error when all are inactive and try to show popup.
               */
              boolean blnStartMenu;
              PFRef objRef;
              
              int intPopups = pobjAction.getPopups().size();
              for (int i = 0; i < intPopups; i++) {
                  objRef = (PFRef)pobjAction.getPopups().get(i);
                  
                  String strRefName = StringEscapeUtils.escapeHTMLTag(objRef.getName().toUpperCase());
                  
                  blnStartMenu = true;
                  
                  if (strName.equalsIgnoreCase(objRef.getName())) {
                      blnStartMenu = false;
                  }
                  
                  if (blnStartMenu) {
                      if (StringUtil.len(strName) > 0) {
                          //End previous popup menu:
                          this.page.popupMenuEnd();
                      }
                      
                      /**
                       * Start new entire popup menu:
                       */
                      this.page.popupMenuStart(strActionName + strRefName);
                  }
                  
                  if (isActive(objRef.getUrl().getActive())) {
                      String strUrl = wrapRefUrl(resolveDirector(objRef.getUrl().getFrameno()),
                    		  					 constructURL(objRef.getUrl()),
                    		  					 resolveDirector(resolveLabel(objRef.getConfirm())),
                    		  					 false, true);
                      
                      this.page.popupMenuOption(strActionName + strRefName,
                              					resolveLabel(objRef.getLabel()),
                              					strUrl,
                              					objRef.getImg(),
                              					objRef.getImgover(),
                              					objRef.isStartsubmenu());
                  }
                  
                  strName = objRef.getName();
              }
              
              if (StringUtil.len(strName) > 0) {
                  this.page.popupMenuEnd();
              }
              
              return processPopups;
          } catch (Exception e) {
              getZx().trace.addError("Failed to : New code for popups process actions popups (if any).", e);
              if (getZx().log.isErrorEnabled()) {
                  getZx().log.error("Parameter : pobjAction = "+ pobjAction);
              }
              
              if (getZx().throwException) throw new ZXException(e);
              processPopups = zXType.rc.rcError;
              return processPopups;
          } finally {
              if(getZx().trace.isFrameworkTraceEnabled()) {
                  getZx().trace.returnValue(processPopups);
                  getZx().trace.exitMethod();
              }
          }
      }
      
	 /**
	  * Get the label for the appropriate language.
	  *
	  * @param pcolLabel  The label collection from which you want the label text.
	  * @return Returns the label text for you language.
	  * @throws Exception 
	  */
     public String getLabel(LabelCollection pcolLabel) throws Exception {
		if(getZx().trace.isFrameworkTraceEnabled()) {
		    getZx().trace.enterMethod();
		    getZx().trace.traceParam("pcolLabel", pcolLabel);
		}
		
        String getLabel = ""; 
		try {
			/**
			 * Short circuit if pcolLabel is null.
			 */
			if (pcolLabel == null) return getLabel;
			
		    getLabel = pcolLabel.getLabel();
		    
		    if (StringUtil.len(getLabel) == 0 && !Environment.DEFAULT_LANGUAGE.equals(getZx().getLanguage())) {
			    /**
			     * Use default language.
			     */
			    getLabel = pcolLabel.get(Environment.DEFAULT_LANGUAGE).getLabel();
		    }
		    
		    /**
		     * It may be that the label is a #pb director in which case
		     * we have to check whether we can find a parameterbag
		     **/
		    if (getLabel != null && getLabel.toLowerCase().startsWith("#pb.") ) {
		    	
		    	String strParamBag = getLabel.substring(4);
		    	int intType = getParameterDirectorType(strParamBag).pos;
		    	if (zXType.pageflowParameterBagEntryType.ppbetLabel.pos == intType) {
		            LabelCollection colLabel = resolveParameterDirectorAsLabel(strParamBag);
		            if (colLabel == null) {
		                throw new RuntimeException("Unable to resolve parameterBagEntry as label : " + strParamBag);
		            }
		            
		            /**
		             * NOTE : This could cause a recursive call.
		             */
		            getLabel = getLabel(colLabel);
		            
		    	} else if (zXType.pageflowParameterBagEntryType.ppbetString.pos == intType) {
		    		getLabel = resolveParameterDirectorAsString(strParamBag);
		    		
		    	} else {
		    		throw new RuntimeException("Entry in parameterbag of wrong type : " + strParamBag);
		    	}

		    } // Parameter bag?				    
		    
		    return getLabel;
		} finally {
			if(getZx().trace.isFrameworkTraceEnabled()) {
			    getZx().trace.returnValue(getLabel);
			    getZx().trace.exitMethod();
			}
		}
	}
     
    /**
     * Generates string that can be used to postfix QS entries used to resort lists.
     *
     * @return Returns the postFix for the QS.
     */
    public String QSSortKeyPostFix() {
        String QSSortKeyPostFix = "";
        if (StringUtil.len(getZx().getActionContext()) > 0) {
            QSSortKeyPostFix = getZx().getActionContext().toLowerCase();
        }
        return QSSortKeyPostFix;
    }
    
    /**
     * Constructs base url.
     * 
     * <pre>
     * 
     * NOTE : This is NOT the getter method for baseURL.
     * </pre>
     * 
     * @return Returns a constructed baseUrl.
     * @throws ZXException  Thrown if getBaseURL fails. 
     */
    public String getBaseUrl() throws ZXException {
        String getBaseUrl;
        getBaseUrl = constructURL(getPFDesc().getBaseurl(), false);
        return getBaseUrl;
    }
    
    /**
     * Append a string to a url.
     *
     * @param pstrUrlSoFar The url build so far.
     * @param pstrAdd What add to the URL.
     * @return Returns a resolved URL.
     */
    public String appendToUrl(String pstrUrlSoFar, String pstrAdd) {
        String appendToUrl;
        
        /**
         * Do not bother adding if there is nothing to add :
         */
        if (StringUtil.len(pstrAdd) == 0) {
            appendToUrl = pstrUrlSoFar;
            return appendToUrl;
        }
        
        /**
         * First safeguard against a ? or & in str2 on position one
         * eg : drop the first ?or &
         */
        pstrAdd = (pstrAdd.charAt(0)=='?' || pstrAdd.charAt(0)=='&') ? pstrAdd.substring(1) : pstrAdd;
        
        /**
         * Add the & or ? parameter seperator.
         */
        if (pstrUrlSoFar.endsWith("?") || pstrUrlSoFar.endsWith("&")) {
            appendToUrl = pstrUrlSoFar + pstrAdd;
            
        } else {
            if (pstrUrlSoFar.indexOf('?') != -1) {
                appendToUrl = pstrUrlSoFar + "&"+ pstrAdd;
            } else {
                appendToUrl = pstrUrlSoFar + "?" + pstrAdd;
            }
        }
        
        return appendToUrl;
    }

    /**
     * Get the base URL of an external pageflow.
     *
     * @param pstrPageflow The pageflow name
     * @param pstrAction The pagelfow action's name
     * @return Returns the url of an external pageflow action.
     * @throws ZXException  Thrown if externalPFBaseUrl fails. 
     */
    public String externalPFBaseUrl(String pstrPageflow, String pstrAction) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrPageflow", pstrPageflow);
            getZx().trace.traceParam("pstrAction", pstrAction);
        }
        
        String externalPFBaseUrl = null;
        
        try {
            getOtherPageflow(pstrPageflow);
            
            /**
             * Now all good old stuff:
             *  remove any instance of -pf in the quick context as we do not want this
             *  to be included in the url as it is most likely to be the
             *  pageflow name for the CURRENT pageflow
             */
            String strTmp = getZx().getQuickContext().getEntry("-pf");
            getZx().getQuickContext().setEntry("-pf", pstrPageflow);
            
            /**
             * Now also disable propagate QS so we do not include all sorts of rubish
             *  in the URL
             */
            boolean blnPropagateQS = this.otherPageflow.getPFDesc().isPropagaTeqs();
            this.otherPageflow.getPFDesc().setPropagateqs(false);
            
            externalPFBaseUrl = otherPageflow.getBaseUrl();
            
            /**
             * And restore...
             */
            getZx().getQuickContext().setEntry("-pf", strTmp);
            this.otherPageflow.getPFDesc().setPropagateqs(blnPropagateQS);
            
            return externalPFBaseUrl;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Get the base URL of an external pageflow.", e);
                getZx().log.error("Parameter : pstrPageflow = "+ pstrPageflow);
                getZx().log.error("Parameter : pstrAction = "+ pstrAction);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return externalPFBaseUrl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(externalPFBaseUrl);
                getZx().trace.exitMethod();
            }
        }
    }    
    
    /**
     * Save a select clause of a query.
     *
     * @param pstrName The name of the Query to be identified by  
     * @param pstrQry The actual sql query.
     * @return Returns the return code of the method.
     * @throws ZXException  Thrown if saveQuerySelectClause fails. 
     */
    public zXType.rc saveQuerySelectClause(String pstrName, String pstrQry) throws ZXException {
        zXType.rc saveQuerySelectClause = zXType.rc.rcOK;
        saveQuerySelectClause = addToContext(pstrName + QRYSELECTCLAUSE, pstrQry);
        return saveQuerySelectClause;
    }

    /**
     * Save a Where clause of a query.
     *
     * @param pstrName Name of the Query to save 
     * @param pstrQry The actual SQL query 
     * @return Returns the return code of the method.
     * @throws ZXException  Thrown if saveQueryWhereClause fails. 
     */
    public zXType.rc saveQueryWhereClause(String pstrName, String pstrQry) throws ZXException {
        zXType.rc saveQueryWhereClause = zXType.rc.rcOK; 
        saveQueryWhereClause = addToContext(pstrName + QRYWHERECLAUSE, pstrQry);
        return saveQueryWhereClause;
    }
    
    /**
     * Save a OrderBy clause of a query.
     *
     * @param pstrName Name of the Query 
     * @param pstrQry The actual sql query 
     * @return Returns the return code of the method. @see zXType#rc 
     * @throws ZXException  Thrown if saveQueryOrderByClause fails. 
     */
    public zXType.rc saveQueryOrderByClause(String pstrName, String pstrQry) throws ZXException {
        zXType.rc saveQueryOrderByClause = zXType.rc.rcOK; 
        saveQueryOrderByClause = addToContext(pstrName + QRYORDERBYCLAUSE, pstrQry);
        return saveQueryOrderByClause;
    }
    
    /**
     * Optionally add the current format stack to the HTML.
     *
     *<pre>
     *
     *NOTE : OUCH !!! : This is web specific and it calls the write method directly !!!
     *NOTE : Maybe this should be moved out of the Pageflow object.
     *</pre>
     */
    public void dumpAllErrors() {
        if (this.PFDesc.getDebugMode().equals(zXType.pageflowDebugMode.pdmAllErrors)) {
            this.page.debugMsg(getZx().trace.formatStack(true));
            
            try {
				this.response.getOutputStream().write(this.page.toString().getBytes());
			} catch (IOException e) {
				throw new RuntimeException("Failed to write to output string", e);
			}
        }
    }
    
    /**
	 * Process actions framehandling.
	 * 
	 * @param penmFrameHandling The type of frame handling.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc processFramehandling(zXType.pageflowFrameHandling penmFrameHandling) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFrameHandling", penmFrameHandling);
        }
        
        zXType.rc processFramehandling = zXType.rc.rcOK;
        
        try {
        	if (penmFrameHandling == null) return processFramehandling;
        	
            /**
             * DGS16APR2004: Stop generating empty script tags. Probably harmless but always pointless.
             * DGS18FEB2005 C1.4:44: Use 70/30 instead of 80/20 for the 'bigger'
             */
            if (!zXType.pageflowFrameHandling.pfhNone.equals(penmFrameHandling)) {
                this.page.s.appendNL("<script type=\"text/javascript\" language=\"JavaScript\">");
                
                if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhBigger1)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,*,30%,0,0,0');");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhBigger2)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,30%,*,0,0,0');");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhBigger3)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,*,30%,0');");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhBigger4)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,0,*,30%');");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhBigger5)) {
                	this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,0,0,*');");
                	
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhMax1)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,*,0,0,0,0');");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhMax2)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,*,0,0,0');");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhMax3)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,*,0,0');");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhMax4)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,0,*,0');");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhMax5)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,0,0,*');");
                    
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhBlank1)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','*,0,0,0,0,0');");
                    this.page.s.appendNL("parent.fraDetails1.location = '../html/zXblank.html';");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhBlank2)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,*,0,0,0,0');");
                    this.page.s.appendNL("parent.fraDetails2.location = '../html/zXblank.html';");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhBlank3)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,*,0,0,0');");
                    this.page.s.appendNL("parent.fraDetails3.location = '../html/zXblank.html';");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhBlank4)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,*,0,0');");
                    this.page.s.appendNL("parent.fraDetails4.location = '../html/zXblank.html';");
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhBlank5)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,0,*,0');");
                    this.page.s.appendNL("parent.fraDetails5.location = '../html/zXblank.html';");
                    
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhClose)) {
                    this.page.s.appendNL("top.window.close();");
                    
                } else if (penmFrameHandling.equals(zXType.pageflowFrameHandling.pfhEqual)) {
                    this.page.s.appendNL("parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,50%,50%,0');");
                }
                
                this.page.s.appendNL("</script>");
            }
            
            return processFramehandling;
            
         } finally {
             if(getZx().trace.isFrameworkTraceEnabled()) {
                 getZx().trace.returnValue(processFramehandling);
                 getZx().trace.exitMethod();
             }
         }
	}
     
    /**
     * Process any Javascript tags that are associated with this action.
     *
     *<pre>
     *
     *This should be handled by the HTML specific version of the PageBuilder.
     *</pre>
     *
     * @param pobjAction The action you want to do the javascript tags for. 
     * @return Returns the return code of the method.
     * @throws ZXException  Thrown if processJavascriptTags fails. 
     */
    public zXType.rc processJavascriptTags(PFAction pobjAction) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAction", pobjAction);
        }
        
        zXType.rc processJavascriptTags = zXType.rc.rcOK;
        
        try {
            
//            boolean blnOpened = false;
//            Iterator iter = pobjAction.getTags().iterator();
//            Tuple objTuple;
//            while(iter.hasNext()) {
//                objTuple = (Tuple)iter.next();
//                if (objTuple.getName().equalsIgnoreCase("zxjavascript")) {
//                    if(!blnOpened) {
//						this.page.s.append("<script language='javascript'>");
//						blnOpened = true;
//                    }
//                    this.page.s.append(resolveInstringDirector(objTuple.getValue()));
//                }
//            }
//            if (blnOpened) {
//                this.page.s.append("</script>");
//            }
            
            String strTag = pobjAction.tagValue("zXJavascript");
            if (StringUtil.len(strTag) > 0) {
                this.page.s.appendNL("<script type=\"text/javascript\" language=\"JavaScript\">");
                this.page.s.appendNL(resolveInstringDirector(strTag));
                this.page.s.appendNL("</script>");
            }
            
            return processJavascriptTags;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Process any Javascript tags that are associated with this action.", e);
                getZx().log.error("Parameter : pobjAction = "+ pobjAction);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            processJavascriptTags = zXType.rc.rcError;
            return processJavascriptTags;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processJavascriptTags);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Process any tags associated with this action. That have anything to do with HTML parameters.
     * 
     * @param pobjAction The Pageflow Action to use.
     * @return Returns the return code of the method.
     * @throws ZXException  Thrown if processHTMLTags fails. 
     */
    public zXType.rc processHTMLTags(PFAction pobjAction) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAction", pobjAction);
        }

        zXType.rc processHTMLTags = zXType.rc.rcOK;

        try {
        	/**
        	 * Short circuit if there are not tags.
        	 */
        	if(pobjAction.getTags() == null) return processHTMLTags;
        	
            if (pobjAction.getTags().size() > 0) {
                Tuple objTuple;
                String strName;

                /** 
                 * Create a clean clone as we only want to change these settings for this
                 * pageflow only.
                 **/
                this.page.setWebSettings((WebSettings) getZx().getSettings().getWebSettings().clone());

                Iterator iter = pobjAction.getTags().values().iterator();
                while (iter.hasNext()) {
                    objTuple = (Tuple) iter.next();
                    strName = objTuple.getName().toLowerCase();
                    
                    // An alternative :
                    // Should work if we can settings case insensitve matches.
                    // BeanUtils.setProperty(this.page, strName.toLowerCase(), objTuple.getValue());

                    if (strName.equals("zxeditformcolumn1")) {
                        this.page.getWebSettings().setEditFormColumn1(objTuple.getValue());
                    } else if (strName.equals("zxeditformcolumn2")) {
                        this.page.getWebSettings().setEditFormColumn2(objTuple.getValue());
                    } else if (strName.equals("zxeditformcolumn3")) {
                        this.page.getWebSettings().setEditFormColumn3(objTuple.getValue());
                    } else if (strName.equals("zxeditformcolumn4")) {
                        this.page.getWebSettings().setEditFormColumn4(objTuple.getValue());
                    } else if (strName.equals("zxsearchformcolumn1")) {
                        this.page.getWebSettings().setSearchFormColumn1(objTuple.getValue());
                    } else if (strName.equals("zxsearchformcolumn2")) {
                        this.page.getWebSettings().setSearchFormColumn2(objTuple.getValue());
                    } else if (strName.equals("zxsearchformcolumn3")) {
                        this.page.getWebSettings().setSearchFormColumn3(objTuple.getValue());
                    } else if (strName.equals("zxsearchformcolumn4")) {
                        this.page.getWebSettings().setSearchFormColumn4(objTuple.getValue());
                    } else if (strName.equals("zxsearchformorderbyimage")) {
                        this.page.getWebSettings().setSearchOrderByImage(objTuple.getValue());
                    } else if (strName.equals("zxlistformcolumn1")) {
                        this.page.getWebSettings().setListFormColumn1(objTuple.getValue());
                    } else if (strName.equals("zxmenucolumn1")) {
                        this.page.getWebSettings().setMenuColumn1(objTuple.getValue());
                    } else if (strName.equals("zxmenucolumn2")) {
                        this.page.getWebSettings().setMenuColumn2(objTuple.getValue());
                    } else if (strName.equals("zxmenucolumn3")) {
                        this.page.getWebSettings().setMenuColumn3(objTuple.getValue());
                    } else if (strName.equals("zxmenucolumn4")) {
                        this.page.getWebSettings().setMenuColumn4(objTuple.getValue());
                    } else if (strName.equals("zxtreecolumn1")) {
                        this.page.getWebSettings().setTreeColumn1(objTuple.getValue());
                    } else if (strName.equals("zxtreecolumn2")) {
                        this.page.getWebSettings().setTreeColumn2(objTuple.getValue());
                    } else if (strName.equals("zxtreecolumn3")) {
                        this.page.getWebSettings().setTreeColumn3(objTuple.getValue());
                    } else if (strName.equals("zxmultilinecols")) {
                        this.page.getWebSettings().setMultilineCols(new Integer(objTuple.getValue()).intValue());
                    } else if (strName.equals("zxmultilinerows")) {
                        this.page.getWebSettings().setMultilineRows(new Integer(objTuple.getValue()).intValue());
                    } else if (strName.equals("zxmaxlistrows")) {
                        this.page.getWebSettings().setMaxListRows(new Integer(objTuple.getValue()).intValue());
                    } else if (strName.equals("zxtitlebulletimage")) {
                        this.page.getWebSettings().setTitleBulletImage(objTuple.getValue());
                    } else if (strName.equals("zxbgcolordark")) {
                        this.page.getWebSettings().setBgColorDark(objTuple.getValue());
                    } else if (strName.equals("zxbgcolorlight")) {
                        this.page.getWebSettings().setBgColorLight(objTuple.getValue());
                    } else if (strName.equals("zxownform")) {
                        this.ownForm = true;
                    }
                    
                }
            }
            
            return processHTMLTags;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process any tags associated with this action.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjAction = " + pobjAction);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            processHTMLTags = zXType.rc.rcError;
            return processHTMLTags;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processHTMLTags);
                getZx().trace.exitMethod();
            }
        }
    }
     
	/**
	 * Copy querystring entries to the QS collection.
	 *
	 * @param pcolQueryString  
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if queryString2QS fails. 
	 */
	public zXType.rc queryString2QS(List pcolQueryString) throws ZXException {
	    if(getZx().trace.isFrameworkTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("pcolQueryString", pcolQueryString);
	    }
	    
	    zXType.rc queryString2QS = zXType.rc.rcOK; 
	    try {
	        if (pcolQueryString == null) {
	            throw new Exception("No query string supplied");
	        }
	        
	        PFDirector objTuple;
	        int intQueryString =  pcolQueryString.size();
            for (int i = 0; i < intQueryString; i++) {
	            objTuple = (PFDirector)pcolQueryString.get(i);
                
	            if (!objTuple.getSource().startsWith("#qsn.*") && !objTuple.getSource().startsWith("#qs.*")) {
	                /**
	                 * BD22MAY04 
	                 * Potentially very risky change:
	                 * The source and destination are also resolved when in
	                 * constructURL; this means we actually do it twice and there
	                 * is simply no need for that!!!
                     * 
                     * BD6DEC04: Ok, caught out here. This was NOT a good idea.
                     * Remember when this function is being called: we simply
                     * copy all query string items to the quick context and let
                     * the constructURL do all the hard work by looping over the
                     * quick context.
                     * Now imagine the following scenario: there are two modes
                     * for a specific list (lets say 'my tasks' and 'all tasks'),
                     * indicated by -mode=1 or -mode=2.
                     * On the screen is a button to switch between the modes and the
                     * label and querystring item for -mode are both expressions; say
                     *
                     * -mode = #expr.if( eq( qs('-mode'), 1), 2, 1)
                     *
                     * The 22nd of May change will do the following:
                     *
                     * Store the expression as -mode in the quickcontext and let
                     * quickcontext resolve the director. The original value
                     * of -mode (either 1 or 2) has now been replaced with an
                     * expression that refers to -mode (ie not a value but an expression!).
                     * All wrong, changed back...
                     *
                     * V1.4:14 - BD11JAN05: there is one important exception and that is the
                     * #js director. This is not really a director as it is not handled by any
                     * of the director handlers. Instead, it is handled in constructQSEntry
                     * and considered a special case. If we simply apply a resolveDirector on
                     * this, this will be lost...
	                 */
                    if (objTuple.getSource().toLowerCase().startsWith("#js.")) {
                        this.qs.setEntry(resolveDirector(objTuple.getDestination()), objTuple.getSource());
                    } else {
                        this.qs.setEntry(resolveDirector(objTuple.getDestination()), resolveDirector(objTuple.getSource()));
                    }
                    
	            }
	        }
	        return queryString2QS;
	    } catch (Exception e) {
	        if (getZx().log.isErrorEnabled()) {
	            getZx().trace.addError("Failed to : Copy querystring entries to the QS collection.", e);
	            getZx().log.error("Parameter : pcolQueryString = "+ pcolQueryString);
	        }
	        
	        if (getZx().throwException) throw new ZXException(e);
	        queryString2QS = zXType.rc.rcError;
	        return queryString2QS;
	    } finally {
	        if(getZx().trace.isFrameworkTraceEnabled()) {
	            getZx().trace.returnValue(queryString2QS);
	            getZx().trace.exitMethod();
	        }
	    }
	}
	
    /**
     * Contruct an entry to be added to a URL based on this director.
     * 
     * @param pobjDirector The director to use. 
     * @return Returns a constructed url based on this director.
     * @throws ZXException Thrown if constructQSEntry fails.
     */
    public String constructQSEntry(PFDirector pobjDirector) throws ZXException {
    	return constructQSEntry(pobjDirector.getDestination(), pobjDirector.getSource());
    }
    
	/**
	 * Create a name=value entry to be added to a querystring.
	 * 
	 * @param pstrName The parameter name  
	 * @param pstrValue The parameter value
	 * @return Returns a name=value entry to be added to a query string.
	 * @throws ZXException Thrown if constructQSEntry fails. 
	 */
	public String constructQSEntry(String pstrName, String pstrValue) throws ZXException {
	    if(getZx().trace.isFrameworkTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("pstrName", pstrName);
	        getZx().trace.traceParam("pstrValue", pstrValue);
	    }
	    
	    String constructQSEntry = null; 
	    
	    try {
	    	
	        constructQSEntry = resolveDirector(pstrName);
	        
	        if (StringUtil.len(constructQSEntry) > 0) {
	        	/**
	        	 * BD22MAY04 Special case if the first 4 characters of the
	        	 * value (ie destination) are #js.
	        	 * This means we have to include the value in the URL as a Javascript
	        	 * call.
	        	 * 
	        	 * For example:
	        	 *   source = #js.#fieldValue.clnt.name
	        	 *   destination = -name
	        	 */
	        	if (pstrValue.toLowerCase().startsWith("#js.")) {
	        		constructQSEntry = new StringBuffer(constructQSEntry)
    									.append("=' + escape(")
    									.append(resolveDirector(pstrValue.substring(4)))
    									.append(") + '").toString();
	        		
	        	} else {
	        		constructQSEntry = new StringBuffer(constructQSEntry)
    									.append('=')
    									.append(URLEncoder.encode(resolveDirector(pstrValue), "UTF-8")).toString();
	        	}
	        	
	        } else {
	            constructQSEntry = resolveDirector(pstrValue);
	        }
	        
	        return constructQSEntry;
	        
	    } catch (Exception e) {
            getZx().trace.addError("Failed to : Create a name=value entry to be added to a querystring.", e);
	        if (getZx().log.isErrorEnabled()) {
	            getZx().log.error("Parameter : pstrName = "+ pstrName);
	            getZx().log.error("Parameter : pstrValue = "+ pstrValue);
	        }
	        
	        if (getZx().throwException) throw new ZXException(e);
	        return constructQSEntry;
	        
	    } finally {
	        if(getZx().trace.isFrameworkTraceEnabled()) {
	            getZx().trace.returnValue(constructQSEntry);
	            getZx().trace.exitMethod();
	        }
	    }
	}
	
    /**
     * Generate Javascript to set footer message and window title.
     *
     * <pre>
     *
     * NOTE : This has html specific stuff in it.
     * NOTE : This has been hardcoded to word in Framesets. For this to work in CPA Direct ect this needs to
     * be frameset independent !.
     * </pre>
     *
     * @param pobjAction The PF action to put the headers on 
     * @param pstrFooter  If not give, assume zXFooter tag to.
     * @return Returns the return code of the method. 
     * @throws ZXException  Thrown if handleFooterAndTitle fails. 
     */
    public zXType.rc handleFooterAndTitle(PFAction pobjAction, String pstrFooter) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAction", pobjAction);
            getZx().trace.traceParam("pstrFooter", pstrFooter);
        }

        zXType.rc handleFooterAndTitle = zXType.rc.rcOK; 
        try {
            
            this.page.s.append("<script type=\"text/javascript\" language=\"JavaScript\">").append(HTMLGen.NL);
            
            if (!pobjAction.hasTag("zXNoTitle") 
                    && StringUtil.len(getZx().getQuickContext().getEntry("-zXInStdPopupTab")) == 0) {
                String strTitle = resolveLabel(pobjAction.getTitle());
                if (StringUtil.len(strTitle) > 0) {
                    this.page.s.append("  zXTitle('").append(StringEscapeUtils.escapeJavaScript(strTitle)).append("');").append(HTMLGen.NL);
                }
            }
            
            /**
             * This is frameset specific stuff. We need to be able to toggle this.
             */
            if (StringUtil.len(pstrFooter) > 0) {
                this.page.s.append("  zXSetFooter('").append(StringEscapeUtils.escapeJavaScript(pstrFooter)).append("');").append(HTMLGen.NL);
            } else {
                if (!pobjAction.hasTag("zXNoFooter")) {
                    String strTag = pobjAction.tagValue("zXFooter");
                    if (StringUtil.len(strTag) > 0) {
                        this.page.s.append("  zXSetFooter('")
                        		   .append(StringEscapeUtils.escapeJavaScript(resolveDirector(strTag)))
                        		   .append("');").append(HTMLGen.NL);
                    }
                }
            }
            
            this.page.s.append("</script>").append(HTMLGen.NL);
            
            return handleFooterAndTitle;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Generate Javascript to set footer message and window title.", e);
                getZx().log.error("Parameter : pobjAction = "+ pobjAction);
                getZx().log.error("Parameter : pstrFooter = "+ pstrFooter);
            }
            if (getZx().throwException) throw new ZXException(e);
            handleFooterAndTitle = zXType.rc.rcError;
            return handleFooterAndTitle;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(handleFooterAndTitle);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Generate the HTML for refs, buttons and actions
     * 
     * New for v1.5:95
     * 
     * @param pobjAction The action to get the form buttons for. 
     * @return Returns the return code of the method. @see zXType#rc 
     * @throws ZXException  Thrown if processFormButtons fails. 
     */
    public zXType.rc processMenuBar(PFAction pobjAction) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAction", pobjAction);
        }
        
        zXType.rc processMenuBar = zXType.rc.rcOK; 
        
        try {
            PFRef objRef;
            boolean blnAreaOpen = false;
            
            int intRefs = pobjAction.getRefs().size();
            if(intRefs > 0) {
                for (int i = 0; i < intRefs; i++) {
                    objRef = (PFRef)pobjAction.getRefs().get(i);
                    objRef.setRefType(zXType.pageflowRefType.prtRef);
                    
                    if (objRef.getEnmAlign().pos == zXType.pageflowElementAlign.peaTop.pos) {
	                    if (!blnAreaOpen) {
	                        this.page.menubarAreaOpen();
	                        blnAreaOpen = true;
	                    }
	                    
	                    processRef(pobjAction, objRef);
                    }
                }
            }
            
            if (blnAreaOpen) {
            	this.page.menubarAreaClose();
            }
            
            return processMenuBar;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate the HTML for refs, buttons and actions", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjAction = "+ pobjAction);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            processMenuBar = zXType.rc.rcError;
            return processMenuBar;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processMenuBar);
                getZx().trace.exitMethod();
            }
        }
    }    
    
    /**
     * Generate the HTML for refs, buttons and actions.
     *
     * @param pobjAction The action to get the form buttons for. 
     * @return Returns the return code of the method. @see zXType#rc 
     * @throws ZXException  Thrown if processFormButtons fails. 
     */
    public zXType.rc processFormButtons(PFAction pobjAction) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAction", pobjAction);
        }
        
        zXType.rc processFormButtons = zXType.rc.rcOK; 
        
        try {
            PFRef objRef;
            
            int intRefs = pobjAction.getRefs().size();
            if(intRefs > 0) {
                for (int i = 0; i < intRefs; i++) {
                    objRef = (PFRef)pobjAction.getRefs().get(i);
                    objRef.setRefType(zXType.pageflowRefType.prtRef);
                    
                    /**
                     * v1.5:95 DGS28APR2006: Don't do this for top-aligned buttons as we've already drawn them
                     */
                    if (objRef.getEnmAlign().pos != zXType.pageflowElementAlign.peaTop.pos) {
	                    processRef(pobjAction, objRef);
                	}
                }
            }
            
            intRefs = pobjAction.getButtons().size();
            if(intRefs > 0) {
                for (int i = 0; i < intRefs; i++) {
                    objRef = (PFRef)pobjAction.getButtons().get(i);
                    objRef.setRefType(zXType.pageflowRefType.prtButton);
                    
                    processRef(pobjAction, objRef);
                }
            }
            
            return processFormButtons;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate the HTML for refs, buttons and actions.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjAction = "+ pobjAction);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            processFormButtons = zXType.rc.rcError;
            return processFormButtons;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processFormButtons);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Process single ref object.
     *
     * @param pobjAction  
     * @param pobjRef  
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if processRef fails. 
     */
    public zXType.rc processRef(PFAction pobjAction, PFRef pobjRef) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAction", pobjAction);
            getZx().trace.traceParam("pobjRef", pobjRef);
        }
        
        zXType.rc processRef = zXType.rc.rcOK;
        
        try {
            
            /**
             * Now call processRefByFormType; this method has been added later so
             * it can also be called from clsHTML where there is no concept of
             * actions
             */
            if (pobjAction.getActionType().equals(zXType.pageflowActionType.patSearchForm)) {
                processRef = processRefByFormType(pobjRef, zXType.webFormType.wftSearch);
            } else {
                processRef = processRefByFormType(pobjRef, zXType.webFormType.wftEdit);
            }
            
            return processRef;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Process single ref object.", e);
                getZx().log.error("Parameter : pobjAction = "+ pobjAction);
                getZx().log.error("Parameter : pobjRef = "+ pobjRef);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            processRef = zXType.rc.rcError;
            return processRef;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processRef);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Newly added function called by processRef.
     * Instead of objAction it simply requires the webFormType
     *
     * @param pobjRef  The PFRef
     * @param penmFormType  The type of web form.
     * @return Returns Returns a form element.
     * @throws ZXException  Thrown if processRefByFormType fails. 
     */
    public zXType.rc processRefByFormType(PFRef pobjRef,
    									  zXType.webFormType penmFormType) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRef", pobjRef);
            getZx().trace.traceParam("penmFormType", penmFormType);
        }
        
        zXType.rc processRefByFormType = zXType.rc.rcOK; 
        
        try {
            String strLabel = "";
            String strAccessKey = "";
            String strTmp = "";
            
            /**
             * It may be that the ref is a reference to a parameter bag entry
             **/
            if (pobjRef.getName() != null && pobjRef.getName().toLowerCase().startsWith("#pb.")) {
            	
            	String strParamBag = pobjRef.getName().toLowerCase().substring(4);
            	int intParamType = getParameterDirectorType(strParamBag).pos;
            	if (zXType.pageflowParameterBagEntryType.ppbetRef.pos == intParamType) {
                	pobjRef = resolveParameterDirectorAsRef(strParamBag);
                    if (pobjRef == null) {
                    	throw new RuntimeException("Unable to resolve parameterBagEntry as ref : " + strParamBag);
                    }
                    
            	} else {
            		throw new RuntimeException("ParameterBagEntry not of type ref or not found : " + strParamBag);
            	}
            	
            }
            
            /**
             * It may be that this ref is no active at all
             */
            if (!isActive(pobjRef.getUrl().getActive())) {
                if (getZx().trace.isTraceEnabled()) {
                    getZx().trace.trace("Ref button not active: " + pobjRef.getName());
                }
                return processRefByFormType;
            }
            
            /**
             * Label is optional for all standard buttons, so generate if absent
             */
            strLabel = resolveLabel(pobjRef.getLabel());
            
            /**
             * Make sure that some buttons are only displayed under
             * strict circumstances
             */
            String strRefName = pobjRef.getName().toLowerCase();
            if (strRefName.endsWith("back")) {
                strAccessKey = "b";
                if (StringUtil.len(strLabel) == 0) strLabel ="Back";
                
            } else if (strRefName.endsWith("submit")) {
                strAccessKey = "s";
                if (StringUtil.len(strLabel) == 0) {
                    if(penmFormType.equals(zXType.webFormType.wftSearch)) {
                        strLabel = "Search";
                    } else {
                        /**
                         * DGS27JUN2003: Note that this should not rely on -sa = insert any more,
                         *  now we have the editsubactions tag in editforms, -sa is now a name.
                         *  Recommend coder writes own code to handle this label, such as an expression
                         *  using the -sa value, as we can't tell here what the -sa name means.
                         */
                        if (this.qs.getEntryAsString("-sa").equals("insert")) {
                            strLabel = "Create";
                        } else {
                            strLabel = "Update";
                        }
                    }
                }
                
                if (penmFormType.equals(zXType.webFormType.wftEdit)) {
                    if (this.contextEntity != null) {
                        if (!this.contextEntity.getBo().mayUpdate()) {
                            /**
                             * User not allowed to update (assume that this is the case from
                             *    an edit form)
                             */
                            return processRefByFormType;
                        }
                    }
                }
                
            } else if (strRefName.endsWith("create")) {
                if (StringUtil.len(strLabel) == 0) strLabel = "Create";
                if (this.contextEntity != null) {
                    if (!this.contextEntity.getBo().mayInsert()) {
                        /**
                         * User not allowed to insert
                         */
                        return processRefByFormType;
                    }
                }
                
            } else if (strRefName.endsWith("copy")) {
                if (StringUtil.len(strLabel) == 0) strLabel = "Copy";
                if (this.contextEntity.getBo().getPersistStatus().equals(zXType.persistStatus.psNew)) {
                    /**
                     * DGS15JUL2003: No copy allowed for new items
                     */
                    return processRefByFormType;
                }
                
            } else if (strRefName.endsWith("delete")) {
                if (StringUtil.len(strLabel) == 0) strLabel = "Delete";
                if (this.contextEntity.getBo().getPersistStatus().equals(zXType.persistStatus.psNew)) {
                    /**
                     * No delete allowed for new items
                     */
                    return processRefByFormType;
                } else if (this.contextEntity.getBo().getDescriptor().getDeleteRule().equals(zXType.deleteRule.drNotAllowed)) {
                    /**
                     * No delete button if BO is not deletable
                     */
                    return processRefByFormType;
                } else if (!this.contextEntity.getBo().mayDelete()) {
                    /**
                     * User not allowed to delete
                     */
                    return processRefByFormType;
                }
                
            } else if (strRefName.endsWith("refine")) {
                if (StringUtil.len(strLabel) == 0) strLabel = "Refine";
                
            } else if (strRefName.endsWith("print")) {
                if (StringUtil.len(strLabel) == 0) strLabel = "Print";
                
            } else if (strRefName.endsWith("save")) {
                strAccessKey = "s";
                if (StringUtil.len(strLabel) == 0) strLabel = "Save";
                
            }
            
            /**
             * Determine the accesskey and the description of the button.
             */
            
            /**
             * See if there is an access key defined.
             * If there is one, override any existing accesskey.
             **/
            strTmp = getLabel(pobjRef.getAccesskey());
            if (StringUtil.len(strTmp) > 0) {
                strAccessKey = strTmp;
            }
            
            /**
             * Get description
             **/
            String strDescription = getLabel(pobjRef.getDescription());
            if (StringUtil.len(strDescription) == 0) {
                if (zXType.pageflowRefType.prtButton.equals(pobjRef.getRefType())) {
                    strDescription = "Submit";
                } else {
                	boolean blnAction = false;
                	if (pobjRef.getUrl() != null && pobjRef.getUrl().getUrlType() != null) {
                		blnAction = pobjRef.getUrl().getUrlType().equals(zXType.pageflowUrlType.putAction);
                	}
                	
                    strDescription = (blnAction?"Go to ":"") + strLabel;
                }
            }
            
            /**
             * Add the access key to the description.
             */
            if (StringUtil.len(strAccessKey) > 0) {
                strDescription = strDescription + " (Alt-" + strAccessKey + ")";
            }
            
            zXType.pageflowRefType refType = pobjRef.getRefType();
            String strUrl = null;
            if (refType.equals(zXType.pageflowRefType.prtRef) 
                    || refType.equals(zXType.pageflowRefType.prtFieldRef)) {
                /**
                 * Determine the url
                 */
                if (pobjRef.getUrl().getUrlType().equals(zXType.pageflowUrlType.putPopup)) {
                    /**
                     * BD5MAR04 - Terrible hack; for popup menus we will
                     * generate Javascript objects. If we combine multiple actions
                     * to generate a page this can result in name clashes. Therefore
                     * we want to prefix the object name with ther pageflow action name
                     * Since this is effectively a bug fix (i.e. overlooked in the initial
                     * design) we decided that this is the best place to include
                     * the action name for ref buttons and we do so by tempoarily changing
                     * the URL
                     */
                    strTmp = pobjRef.getUrl().getUrl();
                    pobjRef.getUrl().setUrl(StringEscapeUtils.escapeHTMLTag(this.contextAction.getName()) + pobjRef.getUrl().getUrl().toUpperCase());
                    strUrl = constructURL(pobjRef.getUrl());
                    pobjRef.getUrl().setUrl(strTmp);
                } else {
                    strUrl = constructURL(pobjRef.getUrl(), false);
                }
                
                /**
                 * Bit weird: if url is empty, do not bother
                 *  this allow us to do smart things with expressions and optional buttons
                 */
                if (StringUtil.len(strUrl) == 0) {
                    return processRefByFormType;
                }
                
                /**
                 * If there is a URL close bit, we assume that we should never touch the
                 * constructed URL as the developer is likely to know best; same for
                 * popup menus where all the clever stuff is already in the URL
                 */
                if (StringUtil.len(pobjRef.getUrl().getUrlclose()) == 0
                    && !zXType.pageflowUrlType.putPopup.equals(pobjRef.getUrl().getUrlType())) {
                    strUrl = wrapRefUrl(resolveDirector(pobjRef.getUrl().getFrameno()),
                    					strUrl, resolveLabel(pobjRef.getConfirm()),
                    					zXType.webFormType.wftEdit.equals(penmFormType), false);
                }
                
                strTmp = "this.style.cursor='wait';window.document.body.style.cursor='wait';" + strUrl +
                              ";this.style.cursor='';window.document.body.style.cursor=''";
                
                /**
                 * BD11DEC02: Use different class for ref buttons and submit buttons
                 * 
                 * DGS14FEB2003: If an image name is given, present an image. Otherwise a button.
                 * If an 'over' image is not given, use the same image i.e. no change on 'over'.
                 * 
                 * DGS31MAR2003: Minor fix to the 'tooltip' (alt) - if it is an image, use the label.
                 * If it is not, continue to use the label as before, but only prefix with 'Go to ' if
                 * the url type is 'Action'.
                 */
                if (StringUtil.len(pobjRef.getImg()) > 0) {
                    this.page.s.append("<img ")
                    	.appendAttr("src", pobjRef.getImg())
                    	
                    	.appendAttr("alt", strDescription)
                        .appendAttr("title", strDescription)
                        .appendAttr("tabindex", pobjRef.getTabindex())
                        .appendAttr("accessKey", strAccessKey)
                        
                    	.appendAttr("onMouseDown", strTmp)
                        .appendAttr("onMouseOver", "javascript:this.src='" + ( StringUtil.len(pobjRef.getImgover()) ==0?pobjRef.getImg():pobjRef.getImgover()) + "'")
                        .appendAttr("onMouseOut", "javascript:this.src='" + pobjRef.getImg() + "'").appendNL('>');
                    
                } else {
                    /**
                     * Use refButton or smallButton class depending on whether
                     * it is for a field ref
                     */
                    String strClass;
                    if(refType.equals(zXType.pageflowRefType.prtFieldRef)) {
                        strClass = "\"zxSmallButton\"";
                    } else {
                        /**
                         * v1.5:95 DGS28APR2006: Different class for top-aligned buttons
                         **/
                        if (pobjRef.getEnmAlign().pos == zXType.pageflowElementAlign.peaTop.pos) {
                            strClass = "\"zxMenuButton\"";
                    	} else {
                            strClass = "\"zxRefButton\"";
                    	}
                    }
                    
                    this.page.s.append("<input type=\"button\" class=" + strClass + " ")
                        	   .appendAttr("name", pobjRef.getName())
                        	   
                        	   .appendAttr("value", strLabel)
		                       .appendAttr("title", strDescription)
		                       .appendAttr("tabindex", pobjRef.getTabindex())
		                       .appendAttr("accessKey", strAccessKey)
		                       
                        	   .appendAttr("onClick", strTmp)
                        	   .appendNL('>');
                }
                
            } else if (refType.equals(zXType.pageflowRefType.prtButton)) {
                strTmp = "this.style.cursor='wait';window.document.body.style.cursor='wait';";
                
                if (StringUtil.len(pobjRef.getImg()) > 0) {
                    this.page.s.append("<input type=\"image\" ")
                    		   .appendAttr("src", pobjRef.getImg())
                    		   .appendAttr("name", pobjRef.getName())
                    		   
                    		   .appendAttr("value", strLabel)
                    		   .appendAttr("title", strDescription)
		                       .appendAttr("tabindex", pobjRef.getTabindex())
		                       .appendAttr("accessKey", strAccessKey)
                    		   
                    		   .appendAttr("onMouseDown", strTmp)
                    		   .appendAttr("onMouseOver", "javascript:this.src='" + (StringUtil.len(pobjRef.getImgover())==0?pobjRef.getImg():pobjRef.getImgover()) + "'")
                    		   .appendAttr("onMouseOut", "javascript:this.src='" + pobjRef.getImg() + "'")
                    		   .appendNL('>');
                    
                } else {
                    /**
                     * When the submit button has a URL associated with it,
                     *  use that....
                     */
                    if (StringUtil.len(pobjRef.getUrl().getUrl()) > 0) {
                        strUrl = constructURL(pobjRef.getUrl());
                        
                        /**
                         * Second parameter tells the function that this is called
                         *  from a submit button and does thus not need to
                         *  submit the form as a input type  submit does that
                         *  automatically
                         */
                        strTmp = strTmp + "zXSubmitThisUrl('" + strUrl + "', 1, this);";
                        
                        /**
                         * Note that onClick is used (where we normally use onMouseDown)
                         *  as the mouseDown event doesn't work well with the
                         *  input type submit; use submit to make the
                         *  button default
                         */
                        this.page.s.append("<input type=\"submit\" class=\"zxSubmitButton\" ")
                        		   .appendAttr("name", pobjRef.getName())
                        		   
                        		   .appendAttr("value", strLabel)
                                   .appendAttr("title", strDescription)
                        		   .appendAttr("tabindex", pobjRef.getTabindex())
                        		   .appendAttr("accesskey", strAccessKey)
                        		   
                        		   .appendAttr("onClick", strTmp)
                        		   .appendNL('>');
                        
                    } else {
                        this.page.s.append("<input type=\"submit\" class=\"zxSubmitButton\" ")
                                   .appendAttr("name", pobjRef.getName())
                                   
                                   .appendAttr("value", strLabel)
                                   .appendAttr("title", strDescription)
                                   .appendAttr("tabindex", pobjRef.getTabindex())
                                   .appendAttr("accesskey", strAccessKey)
                                   
                                   .appendAttr("onClick", "javascript:this.form.submit();this.disabled=true;")
                                   .appendAttr("onMouseDown", strTmp)
                                   .appendNL('>');
                    }
                }
                
            }
            
            return processRefByFormType;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Newly added function called by processRef.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRef = "+ pobjRef);
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return processRefByFormType;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processRefByFormType);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Check whether someting is active by looking at the active director.
     *
     * @param pstrDirector The director you want check. 
     * @return Returns 
     * @throws ZXException  Thrown if isActive fails. 
     */
    public boolean isActive(String pstrDirector) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDirector", pstrDirector);
        }
        
        boolean isActive = false; 
        
        try {
            if (StringUtil.len(pstrDirector) == 0) {
              /**
               * No active director is interpreted as active
               */  
                isActive = true;
            } else {
                String str = resolveDirector(pstrDirector);
                if (StringUtil.booleanValue(str)) {
                    isActive = true;
                } else {
                    isActive = false;
                }
            }
            return isActive;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Check whether someting is active by looking at the active director.", e);
                getZx().log.error("Parameter : pstrDirector = "+ pstrDirector);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return isActive;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(isActive);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate where clause entry based on PK where group (may be empty).
     * 
     * Reviewed for V1.5:1
     * 
     * @param pobjEntity The pageflow action entity. 
     * @return Returns a pk where group sql clause.
     * @throws ZXException  Thrown if processPKWhereGroup fails. 
     */
    public String processPKWhereGroup(PFEntity pobjEntity) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjEntity", pobjEntity);
        }
        
        String processPKWhereGroup = null; 
        
        try {
        	/**
        	 * No where clause
        	 */
            if (StringUtil.len(pobjEntity.getPkwheregroup()) == 0) {
                return processPKWhereGroup;
            }
            
            processPKWhereGroup = getZx().getSql().processWhereGroup(pobjEntity.getBo(), pobjEntity.getPkwheregroup());
            
            return processPKWhereGroup;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate where clause entry based on PK where group (may be empty).", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjEntity = "+ pobjEntity);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return processPKWhereGroup;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processPKWhereGroup);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Create a collection of any editform enhancers that apply to each attribute and
     * associate it with the descriptor. 
     * 
     * <pre>
     * 
     * This will be used by clsHTML to override standard behaviour.
     * 
     * Also create a second collection of enhancers where this attribute is the dependant
     * attribute of some other attribute's enhancer (e.g. is bound to it).
     * 
     * This code was extracted from clsPFEditForm because it is now used by Grid and Matrix
     * edit forms too, and was being repeated in those places.
     * </pre>
     *
     * @param pcolEditEnhancers collection of enhancers 
     * @param pcolEntities collection of entities 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if handleEnhancers fails. 
     */
    public zXType.rc handleEnhancers(List pcolEditEnhancers, ZXCollection pcolEntities) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pcolEditEnhancers", pcolEditEnhancers);
            getZx().trace.traceParam("pcolEntities", pcolEntities);
        }
        
        zXType.rc handleEnhancers = zXType.rc.rcOK;
        
        try {    
            
            if (pcolEditEnhancers != null && pcolEditEnhancers.size() > 0) {
                int intEditDependency;
                PFEditEnhancer objEditEnhancer;
                PFEditDependency objEditDependency;
                Iterator iterAttr;
                PFEntity objEntity;
                String strAttrName;
                
                /**
                 * First resolve the enhancer entity and attr to speed things up a bit
                 */
                int intEditEnhancer = pcolEditEnhancers.size();
                for (int i = 0; i < intEditEnhancer; i++) {
                    objEditEnhancer = (PFEditEnhancer)pcolEditEnhancers.get(i);
                    
                    objEditEnhancer.setResolvedEntity(resolveDirector(objEditEnhancer.getEntity()).toLowerCase());
                    objEditEnhancer.setResolvedAttr(resolveDirector(objEditEnhancer.getAttr()).toLowerCase());
                    
                    intEditDependency = objEditEnhancer.getEditdependencies().size();
                    for (int j = 0; j < intEditDependency; j++) {
                        objEditDependency = (PFEditDependency)objEditEnhancer.getEditdependencies().get(j);
                        
                        objEditDependency.setResolvedEntity(resolveDirector(objEditDependency.getDepentity()).toLowerCase());
                        objEditDependency.setResolvedAttr(resolveDirector(objEditDependency.getDepattr()).toLowerCase());
                        
                    }
                }
                
                Iterator iter = pcolEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    /**
                     * Reset any old cache edit enhancers.
                     * NOTE : This is really a bad side effect of poor cache management, should really find a way round this.
                     */
                    objEntity.getBo().setEditEnhancers(null);
                    objEntity.getBo().setEditEnhancersDependant(null);
                    
                    iterAttr = objEntity.getBo().getDescriptor().getAttributes().iterator();
                    while (iterAttr.hasNext()) {
                        strAttrName = ((Attribute)iterAttr.next()).getName();
                        
                        for (int i = 0; i < intEditEnhancer; i++) {
                            objEditEnhancer = (PFEditEnhancer)pcolEditEnhancers.get(i);
                            
                            if (objEditEnhancer.getResolvedEntity().equalsIgnoreCase(objEntity.getName())
                                && objEditEnhancer.getResolvedAttr().equalsIgnoreCase(strAttrName)) {
                            	objEntity.getBo().initEditEnhancers(strAttrName);
                                objEntity.getBo().getEditEnhancers(strAttrName).add(objEditEnhancer);
                            }
                            
                            intEditDependency = objEditEnhancer.getEditdependencies().size();
                            for (int j = 0; j < intEditDependency; j++) {
                                objEditDependency = (PFEditDependency)objEditEnhancer.getEditdependencies().get(j);
                                if (objEditDependency.getResolvedAttr().equalsIgnoreCase(strAttrName) 
                                    && objEditDependency.getResolvedEntity().equalsIgnoreCase(objEntity.getName())) {
                                	objEntity.getBo().initEditEnhancersDependant(strAttrName);
                                    objEntity.getBo().getEditEnhancersDependant(strAttrName).add(objEditEnhancer);
                                }
                            }
                        }
                    }
                    
                    /**
                     * Add enhancers to the bo saver
                     */
                    objEntity.getBosaver().setEditEnhancers(objEntity.getBo().getEditEnhancers());
                    objEntity.getBosaver().setEditEnhancersDependant(objEntity.getBo().getEditEnhancersDependant());
                }
            }
            
            return handleEnhancers;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Create a collection of any editform enhancers that apply to each attribute and", e);
                getZx().log.error("Parameter : pcolEditEnhancers = "+ pcolEditEnhancers);
                getZx().log.error("Parameter : pcolEntities = "+ pcolEntities);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            handleEnhancers = zXType.rc.rcError;
            return handleEnhancers;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(handleEnhancers);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * @param pstrSubActionValue The string with the sub action.
     * @param pstrArg The string to check.
     * @return Returns whether the editSubActionsIncludes.
     */
    public boolean editSubActionIncludes(String pstrSubActionValue, String pstrArg) {
        boolean editSubActionIncludes = false;
        if (pstrSubActionValue.indexOf(pstrArg) != -1) {
            editSubActionIncludes = true;
        }
        return editSubActionIncludes;
    }
    
    /**
     * Return a specific entity from any action definition.
     *
     * @param pstrAction The Pageflow action you want to retrieve the entity from. (Case Insensitive)
     * @param pstrEntity The PFEntity you want retrieve. (Case Sensitive)
     * @return Returns a PFEntity.
     * @throws ZXException Thrown if getEntity fails. 
     */
    public PFEntity getEntity(String pstrAction, String pstrEntity) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrAction", pstrAction);
            getZx().trace.traceParam("pstrEntity", pstrEntity);
        }
        
        PFEntity getEntity = null;
        
        try {
            /**
             * pstrAction - Is case insensitive.
             */
            PFAction objAction = getPFDesc().getAction(pstrAction);
            if (objAction != null) {
                ZXCollection colEntities = getEntityCollection(objAction, objAction.getActionType(), zXType.pageflowQueryType.pqtAll);
                getEntity = (PFEntity)colEntities.get(pstrEntity);
            }
            return getEntity;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Return a specific entity from any action definition.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrAction = "+ pstrAction);
                getZx().log.error("Parameter : pstrEntity = "+ pstrEntity);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getEntity;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(getEntity);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
    * Return a specific ZXBO from any action definition.
    *
    * @param pstrAction The Pageflow action you want to retrieve the entity from. (Case Insensitive)
    * @param pstrBO The ZXBO you want retrieve. (Case Sensitive)
    * @return Returns a ZXBO.
    * @throws ZXException Thrown if getBO fails. 
     */
    public ZXBO getBO(String pstrAction, String pstrBO) throws ZXException {
        return getEntity(pstrAction, pstrBO).getBo();
    }
    
    /**
     * Determine wheter the given entity is relevant given the action and query type.
     *
     * @param pobjEntity  The PF Entity you want to check
     * @param penmActionType The type of action the entity belongs to.  
     * @param penmQueryType The type of query being performed.
     * @return Returns true if the PFEntity is relevant for the situation.
     * @throws ZXException Thrown if relevantEntity fails. 
     */
    private boolean relevantEntity(PFEntity pobjEntity, 
                                   zXType.pageflowActionType penmActionType,
                                   zXType.pageflowQueryType penmQueryType) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjEntity", pobjEntity);
            getZx().trace.traceParam("penmActionType", penmActionType);
            getZx().trace.traceParam("penmQueryType", penmQueryType);
        }

        boolean relevantEntity = false; 
        
        try {
            
            if (penmActionType.equals(zXType.pageflowActionType.patEditForm)
                || penmActionType.equals(zXType.pageflowActionType.patMatrixEditForm)) {
                relevantEntity = (StringUtil.len(pobjEntity.getSelecteditgroup()) > 0);
                
            } else if (penmActionType.equals(zXType.pageflowActionType.patGridEditForm)) {
                relevantEntity = (StringUtil.len(pobjEntity.getSelecteditgroup()) > 0 || StringUtil.len(pobjEntity.getSelectlistgroup()) > 0);
                
            } else if (penmActionType.equals(zXType.pageflowActionType.patCreateUpdate)) {
                relevantEntity = (StringUtil.len(pobjEntity.getSelecteditgroup()) > 0);
                
            } else if (penmActionType.equals(zXType.pageflowActionType.patSearchForm)) {
                relevantEntity = (StringUtil.len(pobjEntity.getWheregroup()) > 0);
                
            /**
             * DGSOCT2004: Added Calendar action type
             */
            } else if (penmActionType.equals(zXType.pageflowActionType.patListForm) 
                       || penmActionType.equals(zXType.pageflowActionType.patCalendar)) {
                relevantEntity = (StringUtil.len(pobjEntity.getListgroup()) > 0 || StringUtil.len(pobjEntity.getSelectlistgroup()) > 0);
                
            } else if (penmActionType.equals(zXType.pageflowActionType.patQuery)) {
                if (penmQueryType.equals(zXType.pageflowQueryType.pqtSearchForm)) {
                    if (StringUtil.len(pobjEntity.getSelectlistgroup()) > 0 
                        || StringUtil.len(pobjEntity.getWheregroup()) > 0) {
                        relevantEntity = true;
                    }
                    
                } else {
                    if (StringUtil.len(pobjEntity.getSelectlistgroup()) > 0) {
                        relevantEntity = true;
                    }
                    
                }
                
            } else if (penmActionType.equals(zXType.pageflowActionType.patNull)
                       || penmActionType.equals(zXType.pageflowActionType.patLayout)) {
                relevantEntity = true;
                
            } else if (penmActionType.equals(zXType.pageflowActionType.patGridCreateUpdate)) {
                relevantEntity = true;
                
            }
            
            return relevantEntity;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Determine wheter the given entity is relevant given the action and query type.", e);
                getZx().log.error("Parameter : pobjEntity = "+ pobjEntity);
                getZx().log.error("Parameter : penmActionType = "+ penmActionType);
                getZx().log.error("Parameter : penmQueryType = "+ penmQueryType);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return relevantEntity;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(relevantEntity);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Try and get the PFStdPopup.
     * 
     * Start with getting the action from the actions collection with name #qs.-action
     * if this is of type 10 (ie stdPopup) -> Hooray, we are done; otherwise we
     * follow the link to see if anywhere in the chain we have a stdPopup
     * 
     * @param pstrAction The name of the pageflow action to start from.
     * @param pstrPopupDef The name of the pageflow action to start from. Should be a Standard Popup.
     * @return Returns a PFStdPopup.
     * @throws ZXException Thrown if getStdPopupAction fails
     */
    public PFStdPopup getStdPopupAction(String pstrAction, String pstrPopupDef) throws ZXException {
        /**
         * Simply follow the link actions until we have found the one 
         * DGS28APR2004: Slight rejig of the logic here so that never try to use a nothing action.
         * Also, resolveDirector on the link action.
         * BD26AUG04: Another enhancement, you can now pass an optional 5th parameter to the
         * zXStdPopup Javascript function. This becomes the -zXPopupDef querystring parameter.
         * If this is present, we use this as the popup definition action instead of trying to
         * search for one
         */
        
        PFStdPopup getStdPopupAction = null;
        if (StringUtil.len(getZx().getQuickContext().getEntry("-zXPopupDef")) == 0) {
            PFAction objAction = this.PFDesc.getAction(pstrAction);
            /**
             * Carry on going through the link actions untill we get to the standard popup action.
             */
            while (objAction != null) {
                if (objAction.getActionType().equals(zXType.pageflowActionType.patStdPopup)) {
                    getStdPopupAction = (PFStdPopup)objAction;
                    return getStdPopupAction;
                }
                
                if (objAction.getLink() != null) {
                    objAction = this.PFDesc.getAction(resolveDirector(objAction.getLink().getAction()));
                } else {
                    return getStdPopupAction;
                }
            }
            
        } else {
            // Should be the first one else this will fail
            getStdPopupAction = (PFStdPopup)this.PFDesc.getAction(pstrPopupDef);
        }
        
        return getStdPopupAction;
    }
    
    /**
    * Determine what the URL should be for this popup option.
    *
    * @param pobjUrl The PF Url to construct the popup url. 
    * @return Returns a popup url 
    * @throws ZXException Thrown if constructPopupURL fails. 
    */
    public String constructPopupURL(PFUrl pobjUrl) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjUrl", pobjUrl);
        }
        
        String constructPopupURL = "";
        
        try {
            
            /**
             * If no URL: we're done
             */
            if (pobjUrl == null) {
                return constructPopupURL;
            }
            
            constructPopupURL = wrapRefUrl(resolveDirector(pobjUrl.getFrameno()),
                    					   constructURL(pobjUrl), null, false, true);
            
            return constructPopupURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine what the URL should be for this popup option.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjUrl = "+ pobjUrl);
            }
            if (getZx().throwException) throw new ZXException(e);
            return constructPopupURL;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(constructPopupURL);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get the action that is referred to by this action for the entities.
     * 
     * <pre>
     * 
     * NOTE : may call itself recursively
     * NOTE : May been to may this to PFAction.
     * </pre>
     *
     * @param pobjAction The Entity to get the Entity action from.
     * @return Returns the Entity Action. 
     * @throws ZXException Thrown if resolveEntityAction fails. 
     */
    private PFAction resolveEntityAction(PFAction pobjAction) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAction", pobjAction);
        }

        PFAction resolveEntityAction = null;
        
        try {
        	
        	if (pobjAction.getEntityaction() != null && pobjAction.getEntityaction().toLowerCase().startsWith("#pb.")
                && getParameterDirectorType(pobjAction.getEntityaction().substring(4)) == zXType.pageflowParameterBagEntryType.ppbetEntities) {
        		/**
        		 * Entity action is a parameter bag, so resolve the entity collection for this action explicitly.
        		 */
        		pobjAction.setEntities(resolveParameterDirectorAsEntities(pobjAction.getEntityaction().substring(4)));
        		resolveEntityAction = pobjAction;
        		
        	} else {
                String strAction = resolveDirector(pobjAction.getEntityaction());
                
                /**
                 * Simple case: if the action is null. 
                 * Simply assume we do not refer to another action for the entities 
                 * and use the local ones....
                 */
                if (StringUtil.len(strAction) == 0) {
                    resolveEntityAction = pobjAction;
                    return resolveEntityAction;
                }
                
                /**
                 * If the action is in pageflow:action format, we actually refer to 
                 * an action in another pageflow.
                 * 
                 * NOTE : this has some overhead.
                 */
                if (strAction.indexOf(':') != -1) {
                    String[] arrPair = strAction.split(":", 2);
                    
                    getOtherPageflow(arrPair[0]);
                    
                    resolveEntityAction = this.otherPageflow.getPFDesc().getAction(arrPair[1]);
                    if (resolveEntityAction == null) {
                        throw new Exception("Unable to retrieve entity action " + arrPair[0] + "." + arrPair[1]);
                    }
                    
                    /**
                     * Now it could be that even this action does not refer to a 
                     * final action yet (i.e. it also refers to another action for the entities
                     */
                    if (StringUtil.len(resolveEntityAction.getEntityaction()) > 0) {
                        resolveEntityAction = this.otherPageflow.resolveEntityAction(resolveEntityAction);
                    }
                    
                } else {
                    
                    /**
                     * Simple case: action can be found in this pageflow
                     */
                    resolveEntityAction = this.PFDesc.getAction(strAction);
                    if (resolveEntityAction == null) {
                        throw new Exception("Unable to retrieve entity action" + this.pageflow + "." + strAction);
                    }
                    
                    if (StringUtil.len(resolveEntityAction.getEntityaction()) > 0) {
                        resolveEntityAction = resolveEntityAction(resolveEntityAction);
                    }
                }
                
        	}
            
            return resolveEntityAction;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Get the action that is referred to by this action for the entities.", e);
                getZx().log.error("Parameter : pobjAction = "+ pobjAction);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return resolveEntityAction;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(resolveEntityAction);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Resolve an instring-director.
     *
     * @param pstrDirector The director to resolve.
     * @return Returns a resolved instring-director.
     * @throws ZXException Thrown if resolveInstringDirector fails. 
     */
    public String resolveInstringDirector(String pstrDirector) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDirector", pstrDirector);
        }
        
        String resolveInstringDirector = null; 

        try {
        	/**
        	 * Exit early if no director.
        	 */
        	if (StringUtil.len(pstrDirector) == 0) return "";
        	
        	/**
        	 * If first character is a # we may have to treat it in a special way
        	 * for pageflow directors
        	 */
        	if (pstrDirector.charAt(0) == '#') {
        		resolveInstringDirector = resolveDirector(pstrDirector);
        	} else {
        		resolveInstringDirector = getZx().getDirectorHandler().resolve(pstrDirector);
        	}
        	
            return resolveInstringDirector;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Resolve an instring-director.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrDirector = "+ pstrDirector);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return resolveInstringDirector;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(resolveInstringDirector);
                getZx().trace.exitMethod();
            }
        }
    }
    
	/**
	 * Get the label for the appropriate language.
	 * <pre>
	 * 
	 * Reviewed for V1.5:65
	 * <pre>
	 * @param pcolLabel The Label collection to use to resolve the message.
	 * @return Returns the label for the current language.
	 * @throws NestableRuntimeException Thrown if resolveLabel fails. 
	 */
	public String resolveLabel(LabelCollection pcolLabel) throws NestableRuntimeException {
	    if(getZx().trace.isFrameworkTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("pcolLabel", pcolLabel);
		}
	    
	    String resolveLabel = "";
	    
		try {
			/**
			 * Exit early if there is no label.
			 */
			if (pcolLabel == null || pcolLabel.size() == 0) return resolveLabel;
			
		    resolveLabel = getLabel(pcolLabel);
		    resolveLabel = resolveInstringDirector(resolveLabel);
		    
		    return resolveLabel;
		} catch (Exception e) {
		    if (getZx().log.isErrorEnabled()) {
		        getZx().trace.addError("Failed to : Resolve label.", e);
		        getZx().log.error("Parameter : pcolLabel = " + pcolLabel);
	        }
		    
	        if (getZx().throwException) throw new NestableRuntimeException(e);
	        return resolveLabel;
	    } finally {
	        if(getZx().trace.isFrameworkTraceEnabled()) {
	            getZx().trace.returnValue(resolveLabel);
	            getZx().trace.exitMethod();
	        }
	    }
	}
	
    /**
     * Determine next action.
     *
     * @param pobjLink This is the link action of this page. 
     * @return Returns the next action.
     * @throws ZXException Thrown if resolveLink fails. 
     */
	public String resolveLink(PFComponent pobjLink) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjLink", pobjLink);
        }

        String resolveLink = "";
        
        try {
            /**
             * Short circuit if pobjLink is null.
             */
            if (pobjLink == null) return resolveLink;
            
            
            /**
             * Could be a parameterBag entry
             **/
            if (pobjLink.getAction() != null && pobjLink.getAction().toLowerCase().startsWith("#pb.")) {
            	
                pobjLink = resolveParameterDirectorAsComponent(pobjLink.getAction().substring(4));
                
            } //Parameterbag entry?
            
            resolveLink = resolveDirector(pobjLink.getAction());
            
            /**
             * Now this is an interesting new feature (at day of writing
             * 20AUG03): the link action can actually be in a different
             * pageflow!! If the linkaction name is in the format
             * <pageflow>:<action> than the system 'simply' re-initialises
             * itself to be the other pageflow.
             * 
             * Important restriction:
             * - The link action cannot be an ASP action nor can it have
             * an ASP action in its link
             */
            if (resolveLink.indexOf(':') != -1) {
                /**
                 * Get pageflow part and action part
                 */
                String[] arrLink = StringUtil.split(":", resolveLink);
                String strPageflow = arrLink[0];
                String strAction = arrLink[1];
                
                /**
                 * Make sure we did not end up with non-sense
                 */
                if (StringUtil.len(strPageflow) == 0 || StringUtil.len(strAction) == 0) {
                    throw new Exception("URL has not got the <pageflow>:<action> syntax : " + resolveLink);
                }
                
                /**
                 * Switch pageflow
                 */
                switchPageflow(strPageflow);
                resolveLink = strAction;
                
                /**
                 * And set the proper -pf as otheriwse the 'old' -pf is still in
                 * the quick context and would be used to generate URLs.....
                 */
                this.qs.setEntry("-pf", strPageflow);
            }
            
            /**
             * Add any querystring items
             */
            if (pobjLink.getQuerystring() != null && pobjLink.getQuerystring().size() > 0) {
                PFDirector objDirector;
                int intQuerystring = pobjLink.getQuerystring().size();
                for (int i = 0; i < intQuerystring; i++) {
                    objDirector = (PFDirector)pobjLink.getQuerystring().get(i);
                    this.qs.setEntry(resolveDirector(objDirector.getDestination()), 
                            		 resolveDirector(objDirector.getSource()));
                }
            }
            
            return resolveLink;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Determine next action.", e);
                getZx().log.error("Parameter : pobjLink = "+ pobjLink);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return resolveLink;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(resolveLink);
                getZx().trace.exitMethod();
            }
        }
    }
	
    /**
     * Retrieve / construct the SQL from context.
     * 
     * <pre>
     * 
     * Calls : retrieveSQL(pstrQueryName, false)
     * </pre>
     * 
     * @param pstrQueryName The name of the query you want to construct
     * @return Returns a fully built sql query.
     * @throws ZXException Thrown if retrieveSQL fails.
     * @see #retrieveSQL(String, boolean)
     */
    public String retrieveSQL(String pstrQueryName) throws ZXException {
        return retrieveSQL(pstrQueryName, false);
    }
    
	/**
     * Retrieve / construct the SQL from context.
     * 
     * <pre>
     * 
     *  -oa , (order attribute) has been passed, user has requested a re-order
     * </pre>
     * 
     * @param pstrQueryName The name of the query you want to construct
	 * @param pblnBaseContext Whether to read from the base context. Default should be false.
     * @return Returns a fully built sql query.
     * @throws ZXException Thrown if retrieveSQL fails.
     */
    public String retrieveSQL(String pstrQueryName, boolean pblnBaseContext) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrQueryName", pstrQueryName);
            getZx().trace.traceParam("pblnBaseContext", pblnBaseContext);
        }

        StringBuffer retrieveSQL = new StringBuffer(32);
        
        try {
            String strSelectClause = retrieveQuerySelectClause(pstrQueryName, pblnBaseContext);
            String strWhereClause = retrieveQueryWhereClause(pstrQueryName, pblnBaseContext);
            String strGroupByClause = retrieveQueryGroupByClause(pstrQueryName, pblnBaseContext);
            String strOrderByClause = retrieveQueryOrderByClause(pstrQueryName, pblnBaseContext);

            retrieveSQL.append(strSelectClause);
            if (StringUtil.len(strWhereClause) > 0) {
                /**
                 * DGS 17JAN2003: QueryDef generates Where clauses with the
                 * preceding AND, whereas normal pageflow usually doesn't.
                 * Therefore, to avoid confusion when trying to use the same
                 * query in and out of pageflows, handle the situation here by
                 * not adding another AND when one exists at the start.
                 */
                if (!strWhereClause.toUpperCase().startsWith("AND")) {
                    retrieveSQL.append(" AND");
                }
                retrieveSQL.append(" ").append(strWhereClause);

            }

            /**
             * DGS 21APR2004: GROUP BY clause.
             */
            if (StringUtil.len(strGroupByClause) > 0) {
                retrieveSQL.append(" GROUP BY ").append(strOrderByClause);
            }

            if (StringUtil.len(strOrderByClause) > 0) {
                /**
                 * DGS 17JAN2003: ORDER BY is similar to AND above.
                 */
                if (strOrderByClause.toUpperCase().indexOf("ORDER BY") == -1) {
                    retrieveSQL.append(" ORDER BY");
                }
                
                retrieveSQL.append(" ").append(strOrderByClause);
            }

            return retrieveSQL.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Retrieve / construct the SQL from context.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrQueryName = " + pstrQueryName);
                getZx().log.error("Parameter : pblnBaseContext = " + pblnBaseContext);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return retrieveSQL.toString();
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(retrieveSQL);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Retrieve / construct the order by query part from context.
     * 
     * <pre>
     * 
     * Calls : retrieveQueryOrderByClause(pstrQueryName, false, false);
     * </pre>
     * 
     * @param pstrQueryName The name of the query to retrieve 
     * @return Returns the Order By Clause.
     * @throws ZXException Thrown if retrieveQueryOrderByClause fails. 
     * @see #retrieveQueryOrderByClause(String, boolean, boolean)
     */
    public String retrieveQueryOrderByClause(String pstrQueryName) throws ZXException {
        return retrieveQueryOrderByClause(pstrQueryName, false, false);
    }    
    
    /**
     * Retrieve / construct the order by query part from context.
     * 
     * <pre>
     * 
     * Calls : retrieveQueryOrderByClause(pstrQueryName, pblnBaseContext, false);
     * </pre>
     * 
     * @param pstrQueryName The name of the query to retrieve 
     * @param pblnBaseContext Whether it is for the base context. Optional, default should be false. 
     * @return Returns the Order By Clause.
     * @throws ZXException Thrown if retrieveQueryOrderByClause fails. 
     * @see #retrieveQueryOrderByClause(String, boolean, boolean)
     */
    public String retrieveQueryOrderByClause(String pstrQueryName, boolean pblnBaseContext) throws ZXException {
        return retrieveQueryOrderByClause(pstrQueryName, pblnBaseContext, false);
    }
    
    /**
     * Retrieve / construct the order by query part from context.
     * 
     * Note that we have a special parameter here: forChannel. This parameter is only set when this.
     * routine is called directly (ie not from retrieveSQL) and the callee know that he / she is dealing
     * with a channel. In this case we cannot use ASC / DESC but have to use a minus sign..
     * 
     * Reviewed for V1.5:1.
     * 
     * @param pstrQueryName The name of the query to retrieve 
     * @param pblnBaseContext Whether it is for the base context. Optional, default should be false. 
     * @param pblnForChannel Whether the query is for a channel type datasource. Optional, default should be false. 
     * @return Returns the Order By Clause.
     * @throws ZXException Thrown if retrieveQueryOrderByClause fails. 
     */
    public String retrieveQueryOrderByClause(String pstrQueryName, 
                                             boolean pblnBaseContext, 
                                             boolean pblnForChannel) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrQueryName", pstrQueryName);
            getZx().trace.traceParam("pblnBaseContext", pblnBaseContext);
            getZx().trace.traceParam("pblnForChannel", pblnForChannel);
        }

        String retrieveQueryOrderByClause = "";
        
        try {
            /**
             * Process resort option
             */
            String strTmp = getQs().getEntryAsString("-oa" + QSSortKeyPostFix());
            if (StringUtil.len(strTmp) > 0) {
                /**
                 * If -oa (order attribute) has been passed, user has requested a re-order
                 */
                retrieveQueryOrderByClause = strTmp;
                
                /**
                 * And add sort direction
                 */
                strTmp = getQs().getEntryAsString("-od" + QSSortKeyPostFix());
                if (pblnForChannel) {
                    /**
                     * Channels do not support SQL and can thus not use ASC / DESC; must use - for DESC
                     */
                    if (strTmp.equalsIgnoreCase("desc")) {
                        retrieveQueryOrderByClause = "-" + retrieveQueryOrderByClause;
                    }
                } else {
                    retrieveQueryOrderByClause = retrieveQueryOrderByClause + " " + strTmp;
                }
                
                /**
                 * And store in context
                 */
                addToContext(pstrQueryName + QRYORDERBYCLAUSE, retrieveQueryOrderByClause);
                
            } else {
                retrieveQueryOrderByClause = getFromContext(pstrQueryName + QRYORDERBYCLAUSE, pblnBaseContext);
            }
            
            return retrieveQueryOrderByClause;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Retrieve / construct the order by query part from context.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrQueryName = "+ pstrQueryName);
                getZx().log.error("Parameter : pblnBaseContext = "+ pblnBaseContext);
                getZx().log.error("Parameter : pblnForChannel = "+ pblnForChannel);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return retrieveQueryOrderByClause;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(retrieveQueryOrderByClause);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Retrieve / construct the query groupBy part from context.
     * 
     * <pre>
     * 
     * Calls : retrieveQueryGroupByClause(pstrQueryName, false)
     * </pre>
     * 
     * @param pstrQueryName The name of the query to retrieve. 
     * @return Returns the group by clause.
     * @version J1.5
     * @throws ZXException Thrown if retrieveQueryGroupByClause fails. 
     * @see #retrieveQueryGroupByClause(String, boolean)
     */
    public String retrieveQueryGroupByClause(String pstrQueryName) throws ZXException {
        return retrieveQueryGroupByClause(pstrQueryName, false);
    }
    
    /**
     * Retrieve / construct the query groupBy part from context.
     * 
     * Reviewed for V1.5:1
     * 
     * @param pstrQueryName The name of the query to retrieve. 
     * @param pblnBaseContext Whether to retrieve from the base context. Optional, default should be false.
     * @return Returns the group by clause.
     * @version J1.5
     * @throws ZXException Thrown if retrieveQueryGroupByClause fails. 
     */
    public String retrieveQueryGroupByClause(String pstrQueryName, boolean pblnBaseContext) throws ZXException {
        String retrieveQueryGroupByClause; 
        retrieveQueryGroupByClause = getFromContext(pstrQueryName + QRYGROUPBYCLAUSE, pblnBaseContext);
        return retrieveQueryGroupByClause;
    }

    /**
     * Retrieve or construct the query select part from context.
     * 
     * <pre>
     * 
     * Calls : retrieveQuerySelectClause(pstrQueryName, false)
     * </pre>
     * 
     * @param pstrQueryName The name of the query to retrieve. 
     * @return Returns the select clause.
     * @version J1.5
     * @throws ZXException Thrown if retrieveQuerySelectClause fails. 
     * @see #retrieveQuerySelectClause(String, boolean)
     */
    public String retrieveQuerySelectClause(String pstrQueryName) throws ZXException {
        return retrieveQuerySelectClause(pstrQueryName, false);
    }
    
    /**
     * Retrieve / construct the query select part from context
     * 
     * Reviewed for V1.5:1
     * 
     * @param pstrQueryName The name of the query to retrieve. 
     * @param pblnBaseContext Whether to retrieve from the base context. Optional, default should be false.
     * @return Returns the select clause.
     * @version J1.5
     * @throws ZXException Thrown if retrieveQuerySelectClause fails. 
     */
    public String retrieveQuerySelectClause(String pstrQueryName, boolean pblnBaseContext) throws ZXException {
        String retrieveQuerySelectClause; 
        retrieveQuerySelectClause = getFromContext(pstrQueryName + QRYSELECTCLAUSE, pblnBaseContext);
        return retrieveQuerySelectClause;
    }

    /**
     * Retrieve or construct the query whereClause from context.
     * 
     * <pre>
     * 
     * Calls : retrieveQueryWhereClause(pstrQueryName, false)
     * </pre>
     * 
     * @param pstrQueryName The name of the query to retrieve. 
     * @return Returns the where clause.
     * @version J1.5
     * @throws ZXException Thrown if retrieveQueryWhereClause fails. 
     * @see #retrieveQueryWhereClause(String, boolean)
     */
    public String retrieveQueryWhereClause(String pstrQueryName) throws ZXException {
        return retrieveQueryWhereClause(pstrQueryName, false);
    }
    
    /**
     * Retrieve / construct the query whereClause from context
     * 
     * Reviewed for V1.5:1
     * 
     * @param pstrQueryName The name of the query to retrieve. 
     * @param pblnBaseContext Whether to retrieve from the base context. Optional, default should be false.
     * @return Returns the where clause.
     * @version J1.5
     * @throws ZXException Thrown if retrieveQueryWhereClause fails. 
     */
    public String retrieveQueryWhereClause(String pstrQueryName, boolean pblnBaseContext) throws ZXException {
        String retrieveQueryWhereClause; 
        retrieveQueryWhereClause = getFromContext(pstrQueryName + QRYWHERECLAUSE, pblnBaseContext);
        return retrieveQueryWhereClause;
    }
    
    /**
     * Make the current pageflow be based on a different pageflow definition.
     *
     * @param pstrNewPageflow The new pageflow to load 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if switchPageflow fails. 
     */
    public zXType.rc switchPageflow(String pstrNewPageflow) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrNewPageflow", pstrNewPageflow);
        }

        zXType.rc switchPageflow = zXType.rc.rcOK;
        
        try {
            
            /**
             * Simply re-initialise me but with the new pageflow name
             */
            init(page, getRequest(), getResponse(), pstrNewPageflow);
            
            return switchPageflow;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Make the current pageflow be based on a different pageflow definition.", e);
                getZx().log.error("Parameter : pstrNewPageflow = "+ pstrNewPageflow);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            switchPageflow = zXType.rc.rcError;
            return switchPageflow;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(switchPageflow);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Wrap a URL in the appropriate Javascript function.
     *
     * @param pstrFrameNo The frame number. 
     * @param pstrUrl The url to wrap. 
     * @return Returns a wrap javascript url.
     * @throws ZXException  Thrown if wrapRefUrl fails. 
     */
    public String wrapRefUrl(String pstrFrameNo, String pstrUrl) throws ZXException {
    	return wrapRefUrl(pstrFrameNo, pstrUrl, "", false, false);
    }
     
    /**
     * Wrap a URL in the appropriate Javascript function.
     * 
     * <pre>
     * 
     * The frameno is a bit of an overused feature:
     *  - If left blank, the system will simply construct an URL and use
     *   the standard zX Javascript routines to wrap around the href (e.g. zXRef)
     *   and that the URL is to be loaded in the current frame
     * - If it is a number > 0 that it assumes that the URL has to be loaded in
     *   the frame identified by that number (1 to 5)
     * - If the frameno is 0 or notouch it means that the constructed URL should not be
     *   touched at all
     * - If the frameno is close it closes the current window (and thus
     *   ignores the URL all together
     * - If it is any of the predfined words it means the URL will be loaded
     *   in a popup of some sort:
     *   - window: standard window with footer and navigation bar
     *   - docWindow: same as window but with download facility
     *   - windowNoNav: standard window with footer but no navigation bar
     *   - submit: submit page and advance to given url
     *   - popup: large popup with no footer and no navigation bar
     *   - dialog: medium size popup with no footer and no navigation bar
     *   - msgBox: small popup with no footer and no navigation bar
     *   - parent / grandparent: load the given URL in the parent or parent.parent
     *       document.location
     *   - sizedWindow: special situation: the frame is expected to something like
     *           sizedWindow.800.200 or so meaning 800 width, 200 height
     *   - stdPopup: can only be used when in standard popup mode; will load the
     *   URL in the action frame
     * 
     * Assumes   :
     *   All parameters have been resolved / constructed
     *   
     * Reviewed for V1.5:62 - Added support for confirmation message
     * 						  for stdpopup frame handling
     * </pre>
     *
     * @param pstrFrameNo The frame number. 
     * @param pstrUrl The url to wrap. 
     * @param pstrConfirmMsg The confirm message on deletes. Optional. 
     * @param pblnConfirmWhenDirty  Optional default is false.
     * @param pblnPopup Whether to popup. Optional default is false.
     * @return Returns a wrap javascript url.
     * @throws ZXException Thrown if wrapRefUrl fails. 
     */
    public String wrapRefUrl(String pstrFrameNo, String pstrUrl, 
            				 String pstrConfirmMsg, boolean pblnConfirmWhenDirty, 
            				 boolean pblnPopup) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrFrameNo", pstrFrameNo);
            getZx().trace.traceParam("pstrUrl", pstrUrl);
            getZx().trace.traceParam("pstrConfirmMsg", pstrConfirmMsg);
            getZx().trace.traceParam("pblnConfirmWhenDirty", pblnConfirmWhenDirty);
            getZx().trace.traceParam("pblnPopup", pblnPopup);
        }
        
        String wrapRefUrl = null;
        
        if (pstrFrameNo == null) {
        	pstrFrameNo = "";
        }
        
        try {
            String strPre = "";
            String strPost = "";
            
            if (pstrFrameNo.equalsIgnoreCase("stdpopup")) {
                /**
                 * Generate a standard popup :
                 */
                if (StringUtil.len(pstrConfirmMsg) > 0) {
                	strPre = "top.fraTabButtons.zXStdPopupStartActionConfirm('";
                	pstrUrl = pstrConfirmMsg + "', '" + pstrUrl;
                	strPost = "');";
                } else {
                	strPre = "top.fraTabButtons.zXStdPopupStartAction('";
                	strPost = "');";
                }
                
            } else if (pstrFrameNo.equalsIgnoreCase("close")) {
                /**
                 * Simple scenario: just close current window and ignore URL alltogether
                 */
                if (pblnConfirmWhenDirty) {
                    strPre ="zXCloseWindowConfirmWhenDirty(";
                    strPost = "');";
                } else if (StringUtil.len(pstrConfirmMsg) > 0) {
                    strPre = "zXCloseWindowConfirm('" + pstrConfirmMsg;
                    pstrUrl = pstrUrl + "', '" + pstrConfirmMsg; 
                    strPost = "');";
                } else {
                    strPre ="zxWindowClose(";
                    strPost = "');";
                }
                
                // pstrUrl is now reset
                pstrUrl = ""; 
                
            } else if (pstrFrameNo.equalsIgnoreCase("window")) {
                /**
                 * Alll in popup so no impact on current document and we can thus
                 *  ignore confirmation stuff
                 */
                strPre = "zXPopupStep1('" + getZx().getSession().getSessionid() + "','";
                strPost = "');";
                
            } else if (pstrFrameNo.equalsIgnoreCase("docwindow")) {
                /**
                 * All in popup so no impact on current document and we can thus
                 * ignore confirmation stuff; add download facility
                 */
                strPre = "zXPopupStep1('" + getZx().getSession().getSessionid() + "','";
                strPost = "', '1', '1');";
                
            } else if (pstrFrameNo.equalsIgnoreCase("windownonav")) {
                /**
                 * Same as window but tell Javascript that we do not want a navigation bar
                 */
                strPre = "zXPopupStep1('" + getZx().getSession().getSessionid() + "','";
                strPost = "', 1);";
                
            } else if (pstrFrameNo.equalsIgnoreCase("dialog")) {
                /**
                 * Dialog box; no confirm support
                 */
                strPre = "zXDialog('";
                strPost = "');";
                
            } else if (pstrFrameNo.equalsIgnoreCase("popup")) {
                strPre = "zXPopup('";
                strPost = "');";
                
            } else if (pstrFrameNo.equalsIgnoreCase("msgbox")) {
                strPre = "zXMsgBox('";
                strPost = "');";
                
            } else if (pstrFrameNo.equalsIgnoreCase("submit")) {
                /**
                 * Submit form and advance to url
                 */
                if(StringUtil.len(pstrConfirmMsg) > 0) {
                    strPre = "zXSubmitThisUrlConfirm('";
                    pstrUrl = pstrUrl + "', '" + pstrConfirmMsg;
                    strPost = "', this);";
                } else {
                    strPre = "zXSubmitThisUrl('";
                    strPost = "', undefined, this);";
                }
                
            } else if (pstrFrameNo.equalsIgnoreCase("submitcheck")) {
                /**
                 * Submit form and advance to url. If message is set, use a special js function
                 * that checks the value of a variable to see if something has changed on the form.
                 * If no message, behaves just like normal submit
                 */
                if(StringUtil.len(pstrConfirmMsg) > 0) {
                    strPre = "zXSubmitThisUrlCheck('";
                    pstrUrl = pstrUrl + "', '" + pstrConfirmMsg;
                    strPost = "', this);";
                } else {
                    strPre = "zXSubmitThisUrl('";
                    strPost = "', undefined, this);";
                }
                
            } else if (pstrFrameNo.equalsIgnoreCase("0") || pstrFrameNo.equalsIgnoreCase("notouch")) {
                // Ignore case
                
            } else if (pstrFrameNo.equalsIgnoreCase("parent")) {
                if (StringUtil.len(pstrConfirmMsg) > 0) {
                    strPre = "zXRefConfirmParent('";
                    pstrUrl = pstrUrl + "', '" + pstrConfirmMsg; 
                    strPost = "', this);";
                } else if (pblnConfirmWhenDirty) {
                    strPre = "zXRefConfirmWhenDirtyParent('";
                    strPost = "', this);";
                } else {
                    strPre = "zXRefParent('";
                    strPost = "', this);";
                }
                
            } else if (pstrFrameNo.equalsIgnoreCase("grandparent")) {
                if (StringUtil.len(pstrConfirmMsg) > 0) {
                    strPre = "zXRefConfirmGrandParent('";
                    pstrUrl = pstrUrl + "', '" + pstrConfirmMsg; 
                    strPost = "', this);";
                } else if (pblnConfirmWhenDirty) {
                    strPre = "zXRefConfirmWhenDirtyGrandParent('";
                    strPost = "', this);";
                } else {
                    strPre = "zXRefGrandParent('";
                    strPost = "', this);";
                }         
                
            } else {
            	/**
            	 * More dynamic frameno's
            	 */
                if (StringUtil.isNumeric(pstrFrameNo)) {
                    if (StringUtil.len(pstrConfirmMsg) > 0) {
                        strPre = "zXRefConfirm" + pstrFrameNo + "('";
                        pstrUrl = pstrUrl + "', '" + pstrConfirmMsg;
                        strPost = "');";
                    } else if (pblnConfirmWhenDirty) {
                        strPre = "zXRefConfirmWhenDirty" + pstrFrameNo + "('";
                        strPost = "');";
                    } else {
                        strPre = "zXRef" + pstrFrameNo + "('";
                        strPost = "');";
                    }
                } else if (pstrFrameNo.toLowerCase().startsWith("sizedwindow")) {
                	/**
                	 * Assume that frameno is in format sizedwindow.xxx.yyy (xxx=width, yyy=height)
                	 */
                	strPre = "zXSizedWindow('";
                	
                	int indexOf = pstrFrameNo.indexOf('.');
                	if (indexOf != -1) {
                		String strWidth, strHeight;
                		
                		strWidth = pstrFrameNo.substring(indexOf + 1);
                		indexOf = strWidth.indexOf('.');
                		if (indexOf != -1) {
                			strHeight = strWidth.substring(0, indexOf);
                            strWidth = strWidth.substring(indexOf + 1);
                		} else {
                			strHeight = "500";
                		}
                		
                		strPost = "'," + strWidth + "," + strHeight + ");";
                		
                	} else {
                		strPost = "');";
                	}
                	
                } else {
                    if (StringUtil.len(pstrConfirmMsg) > 0) {
                        strPre = "zXRefConfirm('";
                        pstrUrl = pstrUrl + "', '" + pstrConfirmMsg;
                        strPost = "', this);";
                    } else if (pblnConfirmWhenDirty) {
                        strPre = "zXRefConfirmWhenDirty('";
                        strPost = "', this);";
                    } else {
                        strPre = "zXRef('";
                        strPost = "', this);";
                    }
                    
                }
                
            }
            
            if(StringUtil.len(strPre) > 0 && pblnPopup) {
                 strPre = "parent." + strPre;
            }
            
            wrapRefUrl = strPre + pstrUrl + strPost;
            
            return wrapRefUrl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Wrap a URL in the appropriate Javascript function.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrFrameNo = "+ pstrFrameNo);
                getZx().log.error("Parameter : pstrUrl = "+ pstrUrl);
                getZx().log.error("Parameter : pstrConfirmMsg = "+ pstrConfirmMsg);
                getZx().log.error("Parameter : pblnConfirmWhenDirty = "+ pblnConfirmWhenDirty);
                getZx().log.error("Parameter : pblnPopup = "+ pblnPopup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return wrapRefUrl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(wrapRefUrl);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Show any error / warning messages that may been outstanding.
     *
     * @param pobjAction The action this message is from 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if collectMessages fails. 
     */
    public zXType.rc collectMessages(PFAction pobjAction) throws ZXException {
       return collectMessages(pobjAction, false, zXType.logLevel.llInfo.pos); 
    }
    
    /**
     * Show any error / warning messages that may been outstanding.
     *
     * @param pobjAction The action this message is for. 
     * @param pblnSpecificLevel Whether to use a specific log level. Optional default is false.
     * @param pintLogLevel The log level of the messages. Optional default is info level  
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if collectMessages fails. 
     */
    public zXType.rc collectMessages(PFAction pobjAction, boolean pblnSpecificLevel, int pintLogLevel) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAction", pobjAction);
            getZx().trace.traceParam("pblnSpecificLevel", pblnSpecificLevel);
            getZx().trace.traceParam("pintLogLevel", pintLogLevel);
        }

        zXType.rc collectMessages = zXType.rc.rcOK; 
        
        try {
            
            String strTmp;
            
            /**
             * Collect messages
             * DGS16JUL2004: If explicitly asked for error messages or not explicitly asked for anything,
             * collect error messages. Similarly for info messages.
             */
            if (!pblnSpecificLevel || pintLogLevel == zXType.logLevel.llError.pos) {
                strTmp = getFromContext("zxErrorMsg");
                if (StringUtil.len(strTmp) > 0) {
                    if (StringUtil.len(this.errorMsg) == 0) {
                        this.errorMsg =  strTmp;
                    } else {
                        this.errorMsg = errorMsg + "\n" + strTmp;
                    }
                }
                
                /**
                 * Reset the messages in the context
                 */
                addToContext("zxErrorMsg", null);
                
                /**
                 * Get messages related to action
                 */
                if (pobjAction != null) {
                    if (pobjAction.getErrormsg() != null && !pobjAction.getErrormsg().isEmpty()) {
                        if (StringUtil.len(this.errorMsg) == 0) {
                            this.errorMsg = resolveLabel(pobjAction.getErrormsg());
                        } else {
                            this.errorMsg = errorMsg + "\n" + resolveLabel(pobjAction.getErrormsg());
                        }
                    }
                }
            }
            
            if (!pblnSpecificLevel || pintLogLevel == zXType.logLevel.llInfo.pos) {
                strTmp = getFromContext("zxInfoMsg");
                if (StringUtil.len(strTmp) > 0) {
                    if (StringUtil.len(this.infoMsg) == 0) {
                        infoMsg = strTmp;
                    } else {
                        infoMsg = infoMsg + "\n" + strTmp;
                    }
                }
                
                strTmp = getFromContext("zxAlertMsg");
                if (StringUtil.len(strTmp) > 0) {
                    if (StringUtil.len(alertMsg) == 0) {
                        this.alertMsg = strTmp;
                    } else {
                        this.alertMsg = alertMsg + "\n" + strTmp;
                    }
                }
                
                /**
                 * Reset the messages in the context
                 */
                addToContext("zxInfoMsg", null);
                addToContext("zxAlertMsg", null);
                
                /**
                 * Get messages related to action
                 */
                if (pobjAction != null) {
                    
                    if (pobjAction.getInfomsg() != null && !pobjAction.getInfomsg().isEmpty()) {
                        if (StringUtil.len(this.infoMsg) == 0) {
                            this.infoMsg = resolveLabel(pobjAction.getInfomsg());
                        } else {
                            this.infoMsg = infoMsg + "\n" + resolveLabel(pobjAction.getInfomsg());
                        }
                    }
                    
                    if (pobjAction.getAlertmsg() != null && !pobjAction.getAlertmsg().isEmpty()) {
                        if (StringUtil.len(this.alertMsg) == 0) {
                            this.alertMsg = resolveLabel(pobjAction.getAlertmsg());
                        } else {
                            this.alertMsg = alertMsg + "\n" + resolveLabel(pobjAction.getAlertmsg());
                        }
                    }
                    
                }
            }
            
            return collectMessages;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : Show any error / warning messages that may been outstanding", e);
                getZx().log.error("Parameter : pobjAction = "+ pobjAction);
                getZx().log.error("Parameter : pblnSpecificLevel = "+ pblnSpecificLevel);
                getZx().log.error("Parameter : pintLogLevel = "+ pintLogLevel);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            collectMessages = zXType.rc.rcError;
            return collectMessages;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(collectMessages);
                getZx().trace.exitMethod();
            }
        }
    }
    
	/**
	 * Export the resultlist displayed in a list to a csv file.
	 *
     * <pre>
     * 
     * Assumes   :
     *    Called from asp page with buffering set
     *    off to support progress report
     *    
     * Reviewed for V1.5:1
     * Reviewed for V1.5:102
     * </pre>
     *    
	 * @param pstrFileName Name of file to export to. 
	 * @param pstrAction Name of action (must be a list action) 
	 * @param pstrProgressControl Name of control on the screen to send progress report to 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if exportList fails. 
	 */
	public zXType.rc exportList(String pstrFileName, String pstrAction, String pstrProgressControl) throws ZXException {
		if(getZx().trace.isFrameworkTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrFileName", pstrFileName);
			getZx().trace.traceParam("pstrAction", pstrAction);
			getZx().trace.traceParam("pstrProgressControl", pstrProgressControl);
		}
		
		zXType.rc exportList = zXType.rc.rcOK;
        
        DSRS objRS = null;
        
		try {
            String strQry = "";                  // Query to run (for RDBMS)
            String strSelectClause = "";         // For channel
            String strWhereClause = "";
            String strOrderByClause = "";
            boolean blnReverse = false;
            
		    /**
             * Start progress 
		     */
            this.page.showProgress(pstrProgressControl, 10, "Creating file");
            this.response.getOutputStream().print(this.page.flush());
            
            /**
             * Create file
             */
            PrintWriter objStream;
            try {
                // File file = new File(pstrFileName); 
                FileWriter objFileWriter = new FileWriter(pstrFileName, false); // JDK 1.3.1
                objStream = new PrintWriter(objFileWriter);
            } catch (Exception e) {
                throw new Exception("Unable to open file for writing '" + pstrFileName + "'");
            }
            
            /**
             * Get handle to action
             */
            PFAction objAction = (PFAction)this.PFDesc.getActions().get(pstrAction);
            if (objAction == null) {
                throw new Exception("Unable to find action'" + pstrAction + "'");
            }
            
            /**
             * Action must of the correct type.
             */
            if ( !(objAction instanceof PFListForm) || !(objAction instanceof PFTreeForm) ) {
                throw new Exception("Action is neither list- not treeform");
            }
            
            /**
             * Get relevant entities and store in context
             */
            ZXCollection colEntities = getEntityCollection(objAction, 
                                                           zXType.pageflowActionType.patListForm, 
                                                           zXType.pageflowQueryType.pqtAll);
            if (colEntities == null || colEntities.size() == 0) {
                throw new Exception("Unable to get entities for action");
            }
            
            /**
             * Get data-source handler
             */
            PFEntity objEntity1 = (PFEntity)colEntities.iterator().next();
            DSHandler objDSHandler = objEntity1.getDSHandler();
            
            this.page.showProgress(pstrProgressControl, 20, "Retrieving query");
            this.response.getOutputStream().print(this.page.flush());
            
            /**
             * Get query
             */
            String strQryName = resolveDirector(objAction.getQueryname());
            
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                strSelectClause = retrieveQuerySelectClause(strQryName);
                strWhereClause = retrieveQueryWhereClause(strQryName);
                strOrderByClause = retrieveQueryOrderByClause(strQryName, false, true);
                
                if (StringUtil.len(strWhereClause) > 0) strWhereClause = ":" + strWhereClause;
                
                if (StringUtil.len(strOrderByClause) > 1 && strOrderByClause.charAt(0) == '-') {
                    strOrderByClause = strOrderByClause.substring(1);
                    blnReverse = true;
                }
                
            } else {
                /**
                 * Get query
                 */
                strQry = retrieveSQL(strQryName);
                
                if (StringUtil.len(strQry) == 0) {
                    throw new Exception("Unable to retrieve query from context");
                }
                
            } // Channel or RDBMS

            /**
             * Export header
             */
            this.page.showProgress(pstrProgressControl, 30, "Export header");
            this.response.getOutputStream().print(this.page.flush());
            
            PFEntity objEntity;
            StringBuffer strLine = new StringBuffer();
            AttributeCollection colAtrr;
            
            /**
             * Create CSV header
             */
            Iterator iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                /**
                 * 03AUG2004 Domarque Workshop 59: Was generating an empty column when 'null' group.
                 * Now evaluates real number of attributes in group, not just length of name > 0.
                 */
                colAtrr = objEntity.getBo().getDescriptor().getGroup(objEntity.getListgroup());
                if (colAtrr.size() > 0) {
                    if (strLine.length() > 0) {
                        strLine.append(",");
                    }
                    
                    strLine.append(objEntity.getBo().csvHeader(objEntity.getListgroup()));
                }
                
            } // Loop over entities
            
            this.response.getOutputStream().println(strLine.toString());
            
            this.page.showProgress(pstrProgressControl, 40, "Executing query");
            this.response.getOutputStream().print(this.page.flush());
            
            /**
             * Create recordset
             */
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                objRS = objDSHandler.boRS(objEntity1.getBo(), 
                                          strSelectClause, 
                                          strWhereClause, 
                                          objAction.isResolvefk(), 
                                          strOrderByClause, 
                                          blnReverse,
                                          0, 0); // New options for limiting the results returned.
                /**
                 * NOTE : We could use limit to reduce the number of records returns.
                 */
                
            } else {
                objRS = ((DSHRdbms)objDSHandler).sqlRS(strQry
                                                       ,0 ,0); // New options for limiting the returns returned
            }
            
            if (objRS == null) {
                throw new Exception("Unable to execute query");
            }
            
            int intPerc = 45;
            int j = 0;
            
            while (!objRS.eof()) {
                /**
                 * V1.4:90 DGS16JUN2005: Slightly adjusted the progress bar logic to fix a problem:
                 * when lots of rows were being returned, the 'intPerc' figure kept being incremented
                 * way past 100 (although showProgress was not called unless < 100), and ultimately
                 * got past the max integer size and went negative - then showProgress got called
                 * repeatedly with negative numbers, causing many javascript errors.
                 */
                strLine = new StringBuffer();
                
                if (intPerc < 95) {
                    j++;
                    
                    /**
                     * Keep user informed on progress
                     */
                    if (j == 10) {
                        intPerc = intPerc + 5;
                        
                        this.page.showProgress(pstrProgressControl, intPerc, "Executing query");
                        this.response.getOutputStream().print(this.page.flush());
                        
                        j = 0;
                    }
                    
                }
                
                iter = colEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    /**
                     * 03AUG2004 Domarque Workshop 59: Was generating an empty column when 'null' group.
                     * Now evaluates real number of attributes in group, not just length of name > 0.
                     * Also does not do any of the appending to strLine unless there is something in the
                     * group (i.e. all this now inside the if..end if).
                     */
                    colAtrr = objEntity.getBo().getDescriptor().getGroup(objEntity.getSelectlistgroup());
                    if (colAtrr.size() > 0) {
                        objRS.rs2obj(objEntity.getBo(), 
                                     objEntity.getSelectlistgroup(), 
                                     objAction.isResolvefk());
                        
                        if (strLine.length() > 0) {
                            strLine.append(",");
                        }
                        
                        objEntity.getBo().csvRow(objEntity.getListgroup());
                        
                        if (objEntity.getBo().getDescriptor().getGroup(objEntity.getListgroup()).size() > 0) {
                        	if (StringUtil.len(strLine) > 0) {
                            	strLine.append(",");
                        	}
                        	
                        	strLine.append(objEntity.getBo().csvRow(objEntity.getListgroup()));
                        	
                        } // Has attributes?
                    }
                } // Loop over entities
                
                objStream.println(strLine.toString());
                
                objRS.moveNext();
                
            } // Loop over records
            
            /**
             * And close file.
             */
            objStream.close();
            
            /**
             * And done....
             */
            this.page.showProgress(pstrProgressControl, 100, "Done");
            this.response.getOutputStream().print(this.page.flush());
            
			return exportList;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Export the resultlist displayed in a list to a csv file.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pstrFileName = " + pstrFileName);
				getZx().log.error("Parameter : pstrAction = " + pstrAction);
				getZx().log.error("Parameter : pstrProgressControl = " + pstrProgressControl);
			}
			if (getZx().throwException) throw new ZXException(e);
			exportList = zXType.rc.rcError;
			return exportList;
		} finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
			if(getZx().trace.isFrameworkTraceEnabled()) {
				getZx().trace.returnValue(exportList);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * There are certain restriction what you can do with different data sources. This routine checks.
	 * for the biggest known poblems
     * 
	 * Added for 1.5:1
     * 
	 * @param pcolEntities Collection of entities to check.
	 * @return Returns true if entities are valid.
     * @version J1.5
	 * @throws ZXException Thrown if validDataSourceEntities fails. 
	 */
	public boolean validDataSourceEntities(Map pcolEntities) throws ZXException {
		if(getZx().trace.isFrameworkTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pcolEntities", pcolEntities);
		}

		boolean validDataSourceEntities = false;
		
		try {
		    PFEntity objEntity;
            DSHandler objDSHandler = null;
            
            /**
             * No entities does not impose any restrictions when it comes to different data sources
             */
            int intEntities = pcolEntities.size();
            if (intEntities == 0) return validDataSourceEntities;
            boolean blnFirst = true;
            Iterator iter = pcolEntities.values().iterator();
            
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                if (blnFirst) {
                    /**
                     * Take the data-source type from the first data-source; all other entities must have
                     * same type
                     */
                    objDSHandler = objEntity.getDSHandler();
                    blnFirst = false;
                    
                } else {
                    /**
                     * Must be same type as first
                     * Note that we do not raise an error (ie goto errExit) as we do not want an error
                     * message to be generated
                     */
                    if (objDSHandler != objEntity.getDSHandler()) {
                        validDataSourceEntities = false;
                        return validDataSourceEntities;
                    }
                    
                } // First iteration?
                
            } // Loop over entities
            
            /**
             * Another restriction: if we have multiple entities, they cannot obe of type channel
             */
            if (intEntities > 1 && objDSHandler != null && objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                validDataSourceEntities = false;
                return validDataSourceEntities;
            }
            
            validDataSourceEntities = true;
            
			return validDataSourceEntities;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : There are certain restriction what you can do with different data sources. This routine checks", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pcolEntities = " + pcolEntities);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return validDataSourceEntities;
		} finally {
			if(getZx().trace.isFrameworkTraceEnabled()) {
				getZx().trace.returnValue(validDataSourceEntities);
				getZx().trace.exitMethod();
			}
		}
	}
    
	/**
	 * Handle BO contexts for an action.
	 *
	 * @param pobjAction The pageflow action to check. 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if processBOContexts fails. 
	 */
	public zXType.rc processBOContexts(PFAction pobjAction) throws ZXException {
		if(getZx().trace.isFrameworkTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjAction", pobjAction);
		}
		
		zXType.rc processBOContexts = zXType.rc.rcOK; 

		try {
		    PFBOContext objBOContext;
            
            int intBOContexts = pobjAction.getBOContexts().size();
            for (int i = 0; i < intBOContexts; i++) {
                objBOContext = (PFBOContext)pobjAction.getBOContexts().get(i);
                
                if (objBOContext.process(this).pos != zXType.rc.rcOK.pos) {
                	getZx().trace.addError("Unable to process BO context", objBOContext.getName());
                	processBOContexts = zXType.rc.rcError;
                	return processBOContexts;
                }
                
            } // Loop over BO Contexts
            
			return processBOContexts;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Handle BO contexts for an action.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjAction = "+ pobjAction);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			processBOContexts = zXType.rc.rcError;
			return processBOContexts;
		} finally {
			if(getZx().trace.isFrameworkTraceEnabled()) {
				getZx().trace.returnValue(processBOContexts);
				getZx().trace.exitMethod();
			}
		}
	}	
	
	/**
	 * Handle create update actions.
	 * 
	 * (Potentially move this into PFAbstractCreateUpdate)
	 * 
	 * @param pcolCuActions The collection of create update actions.
	 * @param pblnIgnoreWarnings Whether to ignore error messages.
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if processCUActions fails. 
	 */
	public zXType.rc processCUActions(List pcolCuActions, boolean pblnIgnoreWarnings) throws ZXException {
		if(getZx().trace.isFrameworkTraceEnabled()) {
			getZx().trace.enterMethod();
		}
		
		zXType.rc processCUActions = zXType.rc.rcOK; 

		try {
			/**
			 * Short circuit if pcolCuActions is null.
			 */
			if (pcolCuActions == null) return processCUActions;
			
		    PFCuAction objCuAction;
		    zXType.rc enmRC;
		    
            int intBOContexts = pcolCuActions.size();
            for (int i = 0; i < intBOContexts; i++) {
            	objCuAction = (PFCuAction)pcolCuActions.get(i);
            	
            	if (!pblnIgnoreWarnings || objCuAction.getRC().pos != zXType.rc.rcWarning.pos) {
            		
					/**
					 * Store the highest error returned by process.
					 */
            		enmRC = objCuAction.process(this);
            		if (enmRC.pos == zXType.rc.rcWarning.pos) {
            			if(processCUActions.pos == zXType.rc.rcOK.pos) processCUActions = enmRC;
            		} else if (enmRC.pos == zXType.rc.rcError.pos){
            			processCUActions = enmRC;
            		}
            		
            	} // Warning returncode?
            	
            } // Loop over CU Actions
            
			return processCUActions;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Handle create update actions.", e);
			
			if (getZx().throwException) throw new ZXException(e);
			processCUActions = zXType.rc.rcError;
			return processCUActions;
		} finally {
			if(getZx().trace.isFrameworkTraceEnabled()) {
				getZx().trace.returnValue(processCUActions);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Process any includes.
	 * @return Returns the html for includes
	 */
	public String processPFIncludes() {
		if (getPFDesc().getIncludes() != null) {
			String PFINCLUDE_CSS = "zCSS";
			String PFINCLUDE_JS = "zJS";
			
			StringBuffer processPFIncludes = new StringBuffer();
			
			int intIncludes = getPFDesc().getIncludes().size();
			PFInclude objPFInclude;
			for (int i = 0; i < intIncludes; i++) {
				objPFInclude = (PFInclude)getPFDesc().getIncludes().get(i);
				
				if (PFINCLUDE_CSS.equalsIgnoreCase(objPFInclude.getIncludeType())) {
					processPFIncludes.append("<link href=\"")
							   .append(objPFInclude.getPath())
							   .append("\" rel=\"stylesheet\" type=\"text/css\">\n");
					
				} else if (PFINCLUDE_JS.equalsIgnoreCase(objPFInclude.getIncludeType())){
					processPFIncludes.append("<script type=\"text/javascript\" language=\"JavaScript\" src=\"")
							   .append(objPFInclude.getPath())
							   .append("\"></script>\n");
					
				}
				
			} // Print out any of the includes.
			return processPFIncludes.toString();
			
		} // Are there any includes defined.
		
		return "";
	}
	
	//------------------------ Parameter Bag Methods.
	
	/**
	 * Return the type of parameter bag entry.
	 *
	 * @param pstrKey The parameter bag entry name 
	 * @return Returns the type of parameter bag entry.
	 * @throws ZXException Thrown if getParameterDirectorType fails. 
	 */
	private zXType.pageflowParameterBagEntryType getParameterDirectorType(String pstrKey) throws ZXException {
		zXType.pageflowParameterBagEntryType getParameterDirectorType = null; 
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrKey", pstrKey);
		}

		try {
			/**
			 * Split bag name and entry name
			 */
			Tuple objTuple = splitParameterBagReference(pstrKey);
			String strBagName = objTuple.getName();
			String strEntry = objTuple.getValue();
			
			/**
			 * Get resolved parameter-bag
			 */
			ReceiveParameterBag objParameterBag = resolveParameterBag(strBagName);
			if (objParameterBag == null) {
				throw new RuntimeException("Unable to resolve parameterbag : " + strBagName);
			}
			
			/**
			 * Get entry and return type
			 */
			ReceiveParameterBagEntry objParameterBagEntry = (ReceiveParameterBagEntry)objParameterBag.getParameters().get(strEntry);
			if (objParameterBagEntry == null) {
				throw new RuntimeException("Can't find entry in parameter bag : " + strBagName + "." + strEntry);
			}
			
			getParameterDirectorType = objParameterBagEntry.getEntryType();
			
			return getParameterDirectorType;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Return the type of parameter bag entry.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pstrKey = " + pstrKey);
			}
			if (getZx().throwException) throw new ZXException(e);
			return getParameterDirectorType;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(getParameterDirectorType);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Split bagName.entry into the name and entry component.
	 *
	 * @param pstrKey The key to split out. 
	 * @return Returns a tuple with bagname and entry name.
	 */
	private Tuple splitParameterBagReference(String pstrKey) {
		Tuple splitParameterBagReference = new Tuple(); 
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrKey", pstrKey);
		}
		/**
		 * Exit early if no key is specified.
		 */
		if (StringUtil.len(pstrKey) == 0) return splitParameterBagReference;
		
		String strKey = pstrKey.toLowerCase(); // Case insensitive keys
		
		/**
		 * There are two options:
		 *   bagName.entryName
		 * or
		 *   entryName
		 *
		 * The latter will assume the first parameterbag
		 */
		int intKey = strKey.indexOf('.');
		if (intKey != -1) {
			/**
			 * bagName.entry syntax so split up
			 */
			splitParameterBagReference.setName(strKey.substring(0, intKey));
			splitParameterBagReference.setValue(strKey.substring(intKey + 1));
			
		} else {
			/**
			 * Entryname only so must refer to first bag.
			 * NOTE : This is really just a hack and the user should really use the fully qualified name.
			 */
			if (getPFDesc().getReceiveParameterBags() != null && getPFDesc().getReceiveParameterBags().size() > 0) {
				/**
				 * Entryname only so must refer to first bag
				 */
				ReceiveParameterBag objParameterBag = (ReceiveParameterBag)getPFDesc().getReceiveParameterBags().values().iterator().next();
				splitParameterBagReference.setName(objParameterBag.getName().toLowerCase()); // Case insensitive keys
				splitParameterBagReference.setValue(strKey);
				
			} else {
				/**
				 * Programming error : If no bag is found, we have a serious problem
				 */
				throw new RuntimeException("Failed to find any parameter bags");
			}
			
		} // bagName.entry or entry syntax
		
		return splitParameterBagReference;
	}
	
	/**
	 * Resolve the parameter bag.
	 *
	 * @param pstrBagName The name of the parameter bag 
	 * @return Returns parameter bag.
	 * @throws ZXException Thrown if resolveParameterBag fails. 
	 */	
	private ReceiveParameterBag resolveParameterBag(String pstrBagName) throws ZXException {
		ReceiveParameterBag resolveParameterBag = null; 
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrBagName", pstrBagName);
		}

		try {
			SendParameterBag objSendBag;
			SendParameterBagEntry objSendBagEntry;
			ReceiveParameterBagEntry objReceiveBagEntry;
			PFBOContext objBOContext;
			
			/**
			 * Perhaps we already have the bag readily available
			 */
			if (getLastReceiveParameterBag() != null) {
				
				if (pstrBagName.equalsIgnoreCase(getLastReceiveParameterBag().getName())) {
					resolveParameterBag = getLastReceiveParameterBag();
					return resolveParameterBag;
				} // Bag we are looking for?
				
			} // Have a bag?
			
			/**
			 * Get the bag definition
			 */
			setLastReceiveParameterBag((ReceiveParameterBag)getPFDesc().getReceiveParameterBags().get(pstrBagName));
			if (getLastReceiveParameterBag() == null) {
				throw new RuntimeException("ParameterBag definition not found for : " + pstrBagName);
			}
			
			/**
			 * Get the senders bag
			 */
			objSendBag = getSendParameterBag();
			if (objSendBag == null) {
				throw new RuntimeException("Cannot get handle to send parameter bag");
			}
			
			/**
			 * Now we have both receivers and senders bag so lets process
			 */
			if (getLastReceiveParameterBag().getParameters() != null && getLastReceiveParameterBag().getParameters().size() > 0) {
				Iterator iter = getLastReceiveParameterBag().getParameters().values().iterator();
				while (iter.hasNext()) {
					objReceiveBagEntry = (ReceiveParameterBagEntry)iter.next();
					
					/**
					 * Get the same entry from the senders bag
					 */
					objSendBagEntry = (SendParameterBagEntry)objSendBag.getParameters().get(objReceiveBagEntry.getName());
					
					if (objSendBagEntry == null) {
						/**
						 * It is possible that this entry does not exists but only
						 * if it is not mandatory
						 */
						if (objReceiveBagEntry.isMandatory()) {
							throw new RuntimeException("Mandatory parameterBagEntry not found for : " +
									                   objSendBag.getName() + "." + objReceiveBagEntry.getName());
						}
						
						/**
						 * May be that a default exists
						 */
						objReceiveBagEntry.setActualValueObject(objReceiveBagEntry.getValue());
						
					} else {
						/**
						 * Entry was found in send parameterbag so use that but firs check that
						 * types are consistent
						 */
						if (objReceiveBagEntry.getEntryType().pos != objSendBagEntry.getEntryType().pos) {
							throw new RuntimeException("Type mismatch between send- and receive parameterBag entry :" 
													   + objSendBag.getName() + "." + objSendBagEntry.getName());
						}
						
						objReceiveBagEntry.setActualValueObject(objSendBagEntry.getValue());
						
					} // No send entry found
					
				} // Loop over receive bag entries
				
			} // Must sure there are some parameters.
			
			/**
			 * Process the bo context associated with senders bag
			 */
			if (objSendBag.getBocontexts() != null) {
				int intBOContexts = objSendBag.getBocontexts().size();
				for (int i = 0; i < intBOContexts; i++) {
					
					objBOContext = (PFBOContext)objSendBag.getBocontexts().get(i);
					
					if (objBOContext.process(this).pos != zXType.rc.rcOK.pos) {
						throw new RuntimeException("Unable to process BO context : " + objBOContext.getName());
					}
					
				} // Loop over bo context entries
			}
			
			/**
			 * And return
			 */
			resolveParameterBag = getLastReceiveParameterBag();
			
			return resolveParameterBag;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(resolveParameterBag);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Resolve the send parameter bag.
	 *
	 * @return Returns 
	 */
	private SendParameterBag getSendParameterBag() {
		SendParameterBag getSendParameterBag = null; 
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
		}

		try {
			String strBagLookingFor = getZx().getQuickContext().getEntry("-pb");
			
			/**
			 * May already have the right bag!
			 */
			if (getLastSendParameterBag() != null && StringUtil.len(getLastSendParameterBagName()) > 0 && getLastSendParameterBagName().equalsIgnoreCase(strBagLookingFor)) {
				/**
				 * Make sure that this is a clean clone.
				 */
				getSendParameterBag = (SendParameterBag)getLastSendParameterBag().clone();
				return getSendParameterBag;
			}
			
			/**
			 * Get the name of the senders bag (pageflow:bag)
			 */
			int intPos = strBagLookingFor.indexOf(':');
			if (intPos != -1) {
			    String strPageflow = strBagLookingFor.substring(0, intPos);
			    String strBagName = strBagLookingFor.substring(intPos + 1).toLowerCase(); // Key insensitive keys
			    String strFilename = getZx().fullPathName(getZx().configValue("//zX/pageflowDir") + File.separator + strPageflow + ".xml");
			    
			    PFDescriptor objPFDesc = new PFDescriptor();
			    if (objPFDesc.init(this, strFilename, false).pos != zXType.rc.rcOK.pos) {
			    	throw new RuntimeException("Unable to initialise pageflow for sendParameterbag : " + strPageflow);
			    }
			    
			    /**
			     * Store for caching
			     */
			    setLastSendParameterBagName(strBagLookingFor);
			    setLastSendParameterBag((SendParameterBag)objPFDesc.getSendParameterBags().get(strBagName));
			    if (getLastSendParameterBag() == null) {
			    	throw new RuntimeException("Unable to get sendBag definition from pageflow : " + strBagLookingFor);
			    }
			}
			
			/**
			 * And return
			 */
		    getSendParameterBag = getLastSendParameterBag();
		    
			return getSendParameterBag;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(getSendParameterBag);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Resolve #pb.parameterBag as a Object.
	 *
	 * @param pstrKey The key of the parameter to retrieve. 
	 * @return Returns the value of parameter bag entry.
	 * @throws ZXException Thrown if resolveParameterDirectorAsObject fails. 
	 */
	private Object resolveParameterDirectorAsObject(String pstrKey) throws ZXException {
		Object resolveParameterDirectorAsObject = null; 
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrKey", pstrKey);
		}

		try {
			/**
			 * Split bag name and entry name
			 */
			Tuple objTuple = splitParameterBagReference(pstrKey);
			String strBagName = objTuple.getName();
			String strEntry = objTuple.getValue();
			
			/**
			 * Get resolved parameter bag
			 */
			ReceiveParameterBag objParameterBag = resolveParameterBag(strBagName);
			if (objParameterBag == null) {
				throw new ZXException("Unable to resolve parameterbag", strBagName);
			}
			
			/**
			 * Get entry and return type
			 */
			ReceiveParameterBagEntry objParameterBagEntry = (ReceiveParameterBagEntry)objParameterBag.getParameters().get(strEntry);
			if (objParameterBagEntry == null) {
				throw new RuntimeException("Can't find entry in parameter bag : " + strBagName + "." + strEntry);
			}
			
			resolveParameterDirectorAsObject = objParameterBagEntry.getActualValueObject();
			
			return resolveParameterDirectorAsObject;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Resolve #pb.parameterBag.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pstrKey = "+ pstrKey);
			}
			if (getZx().throwException) throw new ZXException(e);
			return resolveParameterDirectorAsObject;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(resolveParameterDirectorAsObject);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * @see Pageflow#resolveParameterDirectorAsObject(String)
	 * @param pstrKey The key of the parameter to retrieve
	 * @return Returns the value of parameter bag entry.
	 * @throws ZXException Thrown if resolveParameterDirectorAsString fails.
	 */
	public String resolveParameterDirectorAsString(String pstrKey) throws ZXException {
		if (getParameterDirectorType(pstrKey).pos != zXType.pageflowParameterBagEntryType.ppbetString.pos) {
			return "";
		}
		
		return (String)resolveParameterDirectorAsObject(pstrKey);
	}
	
	/**
	 * @see Pageflow#resolveParameterDirectorAsObject(String)
	 * @param pstrKey The key of the parameter to retrieve
	 * @return Returns the value of parameter bag entry.
	 * @throws ZXException Thrown if resolveParameterDirectorAsString fails.
	 */
	public LabelCollection resolveParameterDirectorAsLabel(String pstrKey) throws ZXException {
		if (getParameterDirectorType(pstrKey).pos != zXType.pageflowParameterBagEntryType.ppbetLabel.pos) {
			return new LabelCollection();
		}
		
		return (LabelCollection)resolveParameterDirectorAsObject(pstrKey);
	}
	
	/**
	 * @see Pageflow#resolveParameterDirectorAsObject(String)
	 * @param pstrKey The key of the parameter to retrieve
	 * @return Returns the value of parameter bag entry.
	 * @throws ZXException Thrown if resolveParameterDirectorAsString fails.
	 */	
	public PFRef resolveParameterDirectorAsRef(String pstrKey) throws ZXException {
		if (getParameterDirectorType(pstrKey).pos != zXType.pageflowParameterBagEntryType.ppbetRef.pos) {
			return null;
		}
		
		return (PFRef)resolveParameterDirectorAsObject(pstrKey);
	}
	
	/**
	 * @see Pageflow#resolveParameterDirectorAsObject(String)
	 * @param pstrKey The key of the parameter to retrieve
	 * @return Returns the value of parameter bag entry.
	 * @throws ZXException Thrown if resolveParameterDirectorAsString fails.
	 */
	public PFUrl resolveParameterDirectorAsUrl(String pstrKey) throws ZXException {
		if (getParameterDirectorType(pstrKey).pos != zXType.pageflowParameterBagEntryType.ppbetUrl.pos) {
			return null;
		}
		
		return (PFUrl)resolveParameterDirectorAsObject(pstrKey);
	}
	
	/**
	 * @see Pageflow#resolveParameterDirectorAsObject(String)
	 * @param pstrKey The key of the parameter to retrieve
	 * @return Returns the value of parameter bag entry.
	 * @throws ZXException Thrown if resolveParameterDirectorAsString fails.
	 */
	public PFEntity resolveParameterDirectorAsEntity(String pstrKey) throws ZXException {
		if (getParameterDirectorType(pstrKey).pos != zXType.pageflowParameterBagEntryType.ppbetEntity.pos) {
			return null;
		}
		
		return (PFEntity)resolveParameterDirectorAsObject(pstrKey);
	}
	
	/**
	 * @see Pageflow#resolveParameterDirectorAsObject(String)
	 * @param pstrKey The key of the parameter to retrieve
	 * @return Returns the value of parameter bag entry.
	 * @throws ZXException Thrown if resolveParameterDirectorAsString fails.
	 */
	public ZXCollection resolveParameterDirectorAsEntities(String pstrKey) throws ZXException {
		if (getParameterDirectorType(pstrKey).pos != zXType.pageflowParameterBagEntryType.ppbetEntities.pos) {
			return null;
		}
		
		return (ZXCollection)resolveParameterDirectorAsObject(pstrKey);
	}
	
	/**
	 * @see Pageflow#resolveParameterDirectorAsObject(String)
	 * @param pstrKey The key of the parameter to retrieve
	 * @return Returns the value of parameter bag entry.
	 * @throws ZXException Thrown if resolveParameterDirectorAsString fails.
	 */
	public PFComponent resolveParameterDirectorAsComponent(String pstrKey) throws ZXException {
		if (getParameterDirectorType(pstrKey).pos != zXType.pageflowParameterBagEntryType.ppbetComponent.pos) {
			return null;
		}
		
		return (PFComponent)resolveParameterDirectorAsObject(pstrKey);
	}
	
	/**
	 * Try To Resolve parameter director as url.
	 *
	 * @param pobjUrl The url to resolve. 
	 * @return Returns the resolved url.
	 * @throws ZXException Thrown if tryToResolveParameterDirectorAsUrl fails. 
	 */
	public PFUrl tryToResolveParameterDirectorAsUrl(PFUrl pobjUrl) throws ZXException {
		PFUrl tryToResolveParameterDirectorAsUrl = null; 
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjUrl", pobjUrl);
		}
		
		try {
			/**
			 * Not likely but can be nothing
			 */
		    if (pobjUrl == null) return tryToResolveParameterDirectorAsUrl;
		    
		    /**
		     * If not a parameterbag reference than simply return itself
		     */
		    String strUrl = pobjUrl.getUrl();
		    if (StringUtil.len(strUrl) == 0 || !strUrl.toLowerCase().startsWith("#pb.")) {
		        tryToResolveParameterDirectorAsUrl = pobjUrl;
		        return tryToResolveParameterDirectorAsUrl;
		    }
		    
		    /**
		     * So we are dealing with a parameterbag entry
		     */
		    if (getParameterDirectorType(strUrl.substring(4)).pos !=  zXType.pageflowParameterBagEntryType.ppbetUrl.pos) {
		    	/**
		    	 * Unlucky situation where the #pb in the URL field is actually
		    	 * not of type URL; simply return itself; probably a parameter
		    	 * bag reference of type string and this will be resolved later
		    	 */
		        tryToResolveParameterDirectorAsUrl = pobjUrl;
		        return tryToResolveParameterDirectorAsUrl;
		    }
		    
		    /**
		     * Resolve the parameter bag.
		     */
		    tryToResolveParameterDirectorAsUrl = resolveParameterDirectorAsUrl(strUrl.substring(4));
		    
			return tryToResolveParameterDirectorAsUrl;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Try To Resolve parameter director as url.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjUrl = "+ pobjUrl);
			}
			if (getZx().throwException) throw new ZXException(e);
			return tryToResolveParameterDirectorAsUrl;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(tryToResolveParameterDirectorAsUrl);
				getZx().trace.exitMethod();
			}
		}
	}
}