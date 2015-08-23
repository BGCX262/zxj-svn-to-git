/*
 * Created on Apr 9, 2004 by michael
 * $Id: PageBuilder.java,v 1.1.2.86 2006/07/17 17:14:17 mike Exp $
 */
package org.zxframework.web;

import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.Descriptor;
import org.zxframework.Option;
import org.zxframework.WebSettings;
import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.datasources.DSWhereClause;
import org.zxframework.property.BooleanProperty;
import org.zxframework.property.DateProperty;
import org.zxframework.property.DoubleProperty;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.DateUtil;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;
import org.zxframework.web.util.HTMLGen;

/**
 * XML/HTML generator.
 * 
 * <pre>
 * 
 * This XML component should not be independent of the HTTP stuff. Also at the moment this is 
 * generatiing HTML until we come up with a feasiable class hierarchy ?
 * 
 * Note  :  Should not use the getParameter stuff and use the of HttpServletRequest.
 * 
 * Change    : BD6DEC02
 * Why       : Remove any '/' from control name as this can
 *             cause problems when passed as parameter to a Javascript function
 * Why       : For locked fields, replace any vbcrlf with <br> for better
 *             display
 * 
 * Change    : DGS17DEC02
 * Why       : Added function treeListHeaderOpenTop - same as existing treeListHeaderOpen but
 *             includes the top-level menu image that can be clicked to toggle the whole tree
 *             open and closed. Also changes to treeListRowOpen to give the tree open/close image
 *             an id so that the javascript that toggles can recognize it.
 * 
 * Change    : DGS10JAN03
 * Why       : Made table row closing tag /TR (not TR) as was intended in infoMsg and errorMsg
 * 
 * Change    : BD14JAN02
 * Why       : Escape values for input controls
 * 
 * Change    : BD28JAN02
 * Why       : Support for more pre- and post persist
 * 
 * Change    : BD6FEB02
 * Why       : Added support for more intuitive select / option lists
 * 
 * Change    : DGS28FEB2003
 * Why       : Implement popups
 * 
 * Change    : BD4MAR03
 * Why       : Serious typo: zXOnChnage rather than onChange
 * 
 * Change    : BD6MAR03
 * Why       : Fixed weird side-effect of more-intuitive-select/option-list
 * 
 * Change    : BD6MAR03
 * Why       : Aded support for combo-boxes
 * 
 * Change    : DGS10MAR2003
 * Why       : Added code for editform enhancers
 * 
 * Change    : BD14MAR03
 * Why       : Added case insensitive order by
 * 
 * Change    : DGS07APR2003
 * Why       : Changes to the way FKs are handled, especially for medium and large entities
 * 
 * Change    : DGS15MAY2003
 * Why       : In processEditForm, can expect a blank FK value even from a non-optional
 *             field. Without this change was not expecting one, and thus tried to put "-"
 *             into numeric PK fields etc. The reason can now get one is that a change has
 *             had to be made to zx.js to allow a blank FK value when there are no others,
 *             although it will always cause an error that the user will have to solve by
 *             selecting a non-blank value.
 * 
 * Change    : DGS16MAY2003
 * Why       : In processEditForm, if have error so far, let postPersist know that. Also
 *             don't allow call to postPersist to downgrade the rc.
 * 
 * Change    : DGS19MAY2003
 * Why       : In formEntryLockedControl use given class, not hard-coded.
 * 
 * Change    : BD24MAY03
 * Why       : Added support for enhancer.refs
 * 
 * Change    : DGS05JUN2003
 * Why       : In processEditForm, hidden locked booleans will have a value like 'False'.
 * 
 * Change    : BD8JUN03
 * Why       : - Added support for booleans on search forms
 *             - Added support for 'between' on search forms
 * 
 * Change    : BD9JUN03
 * Why       : And fixed stupid bug introduced on the 8th
 * 
 * Change    : DGS12JUN2003
 * Why       : In entryInputControl, if case insensitive and a search form,
 *             make the field upper case. Also fix here for problems with combobox.
 * 
 * Change    : DGS23JUN2003
 * Why       : New function 'narrative' to generate HTML for the narrative
 * 
 * Change    : BD15AUG03
 * Why       : Introduce treeColumn1, 2 and 3
 * 
 * Change    : DGS27AUG2003
 * Why       : New functions to support saved searchform queries
 * 
 * Change    : DGS19SEP2003
 * Why       : New functions to support new actions grid editform/createupdate
 * 
 * Change    : BD17OCT03
 * Why       : - In listHeader optionally postfix the -oa / -od qs entries (for resorting
 *               a list); see clsPFListForm for additional information
 *             - Take CrLf out of locked div field (upsets the barcode)
 * 
 * Change    : DGS20OCT2003
 * Why       : In formEntryFKControl, can now use a new enhancer property 'FKWhere' to
 *             restrict the recordset of FK items. The enhancer property is obtained in
 *             called function editFormOptions.
 * 
 * Change    : BD27OCT03
 * Why       : Added escapeHTMLTag and use it when generating names for grid
 *             related tags
 * 
 * Change    : BD21NOV03
 * Why       : Allow lookup for locked fields as well
 * 
 * Change    : DGS17DEC2003 (raised during e-DT Phase 2 development)
 * Why       : In formEntryFKControl and formEntrySelectControl (i.e. where drop-downs are generated),
 *             will also include the blank (-) option if the current attr value is null. This means that
 *             new instances of a BO (where they don't have a default value) will initially show blank,
 *             even if it is a mandatory attr. The validation is already coded so that saving it as blank
 *             is not allowed, so this has the effect of forcing the user to make an explicit selection.
 *             Previously it auto selected the first in the list, which meant there was a danger the user
 *             might not notice and could leave it set wrong.
 * 
 * Change    : BD9JAN04
 * Why       : The dependancy enhancers work really well but we forgot one thing:
 *             when a page is first generated the enhancers are not invoked (as they
 *             are only invoked by the onChange event). This means that although the
 *             dependancy enhancer may indicate that a field should be dis-abled
 *             (because of the value of a related attribute), the field is actually
 *             fully available until the user changes the relevant value.
 *             The solution (...) is to maintain a collection of commands
 *             (postLoadJavaScript) that will be added to the generated HTML.
 *             This collection is actually used by the pageflow class.
 * 
 * Change    : BD22JAN04
 * Why       : Added support for expression popup
 * 
 * Change    : DGS28JAN2004
 * Why       : Now have is null and not null search operators. This has required changes
 *             in various places such as generating the operator and the operands as well
 *             as when processing the search form entry.
 * 
 * Change    : DGS12FEB2004
 * Why       : New functions and changes to support the 'Matrix' edit form.
 *             Also added new enhancer ability to partially override entity size in dropdowns
 * 
 * Change    : BD14FEB04
 * Why       : Added alertMsg support
 * 
 * Change    : DGS19FEB2004
 * Why       : - New feature for matrix edit form where visible group can be blank to show just
 *               a checkbox for existence of the cell entity instance at that intersection between
 *               row and column. Also altered matrix row open to include class, for better appearance
 *               (unfortunately breaks compatibility unless built against pre-matrix zXWeb).
 *             - Adjustment to how many lines are shown for a multiline edit cell - now based
 *               on the attr length and output length.
 *             - A minor tweak to tooltip as was missing for expression datatype.
 * 
 * Change    : BD23FEB04
 * Why       : - Do NOT force a width in grid edit form but let HTML rendering
 *               take care of it
 * 
 * Change    : DGS23FEB2004
 * Why       : - In formEntryFKControl, if the restrict value is a zero-length string, explicitly
 *                set the 'isNull' property of the str value used in the wherecondition.
 *             - In formEntryFKControl, escape labels used in dropdowns so that they are javascript-safe.
 *             - In gridRowOpen, fixed minor html tags error.
 * 
 * Change    : BD23FEB04
 * Why       : Fixed problem in gridHeader: follow same algorithm as
 *             in gridEditForm as otherwise the column headers may not line
 *             up with the columns!
 * 
 * Change    : DGS26FEB2004
 * Why       : - In controlName, remove dots if present (can get them from grid and matrix
 *               actions when generating BO aliases on the fly).
 *             - In gridRowOpen, do not open the <select> tag at all if no applicable options.
 * 
 * Change    : BD27FEB04
 * Why       : Fix small problem where enhancer refbuttons are not generated
 *             for locked controls
 * 
 * Change    : DGS10MAR2004
 * Why       : In entryInputControl, if the 'Disabled' enhancer is set, make the field unlocked.
 *             This has the effect of showing the field in its normal style but disabled, which
 *             looks better than locked in some circumstances e.g. grids and matrixes and still behaves
 *             like a locked field.
 * 
 * Change    : DGS15MAR2004
 * Why       : In formEntryMultiLIneControl, set the column width according to the zXMultiLineCols tag if set
 * 
 * Change    : BD21MAR04
 * Why       : Now call BOS.pre- and posrpersist as wrappers to the BO
 *             versions, this in preparetion for things like auditing and
 *             BO validation
 * 
 * Change    : BD28MAR04
 * Why       : Added lastRC parameter to postPersist (see clsZX and clsBOS for details)
 * 
 * Change    : DGS30APR2004
 * Why       : In ListRowOpen, use new tags for 'select disable' and 'selected' to affect behaviour
 *             after a row is selected
 * 
 * Change    : BD6MAY04
 * Why       : In lockedEditFormEntry (or so...) changed div to span; first bug solved
 *             was to generate enhancer ref buttons outside div but next div
 *             did not render any good as a div is a span followed by a break
 * 
 * Change    : BD25MAY04
 * Why       : Do not rely on Pageflow.contextAction to be available even in
 *             methods that should only be called from the pageflow. The reason
 *             for this is that under certian circumstances (to do with
 *             using external pageflow actions) the context action may
 *             not be set
 * 
 * Change    : BD2JUN04
 * Why       : ProcessSearchForm may be called even though no search form was submitted
 *             This can be the case if a query action (we are talking pageflows now)
 *             is used for different scenarios and has the searchForm box ticked.
 *             Check the number of submitted bytes to see if we need to do anything
 * 
 * Change    : BD8JUN04
 * Why       : Small performance gain by not calling getEnhancerProperty each
 *             time when there are no enhancers
 * 
 * Change    : BD9JUN04
 * Why       : Added support for concurrencyControl (rather than auditable) and
 *             limit audit columns to ~ instead of !
 * 
 * Change    : DGS16JUN2004
 * Why       : Changes to how multicheck radio buttons handled. Now supports multiple on each
 *             line, with different behaviour to when a single on each line
 * 
 * Change    : DGS09JUL2004
 * Why       : Treat restricted FK dropdowns as small entities
 * 
 * Change    : BD19JUL04
 * Why       : Added support for merging multiple fields onto
 *             a single edit form line
 * 
 * Change    : DGS21JUL2004
 * Why       : In processOrderBy, accept optional extra parameter of an attribute collection,
 *             and if present, use it to only process the order by if the attribute is not
 *             already in that collection (and add it to the collection if not already in)
 * 
 * Change    : DGS27JUL2004
 * Why       : In formEntryFKControl: Use JS copyrows function for medium and large entities
 *             as well as small - will have the effect of copying selected values from previous
 *             lines, which is often useful.
 * 
 * Change    : BD4AUG04
 * Why       : Change process multi-list to generate IN statement rather then OR
 *             to avoid having 'too complex query' exceptions
 * 
 * Change    : DGS07SEP2004
 * Why       : In formEntryLockedControl, replace any single LF by <br>
 * 
 * Change    : BD10SEP04
 * Why       : Moved postlabel to be the true last thing for a field as this
 *             looks better (who am I to say this...)
 *             
 * Why       : Changed way that debugging works; each debug message is now
 *             first stored in seperate property (debugHTML) and should be
 *             added to HTML separately to avoid incorrect HTML / Javascript
 *             being generated
 * 
 * Change    : MB15OCT2004
 * Why       : Fix for single radio button to work vertically
 * 
 * Change    : BD19OCT04
 * Why       : Added postLabel for locked fields as well
 * 
 * Change:     MB01NOV04
 * Why       : Allow for browser independence.
 * 
 * Change    : MB01NOV04
 * Why       : Using new calendar control.
 * 
 * Change    : MB01NOV04
 * Why       : Added support for resorting in the gridHeader for GridEditForms.
 * 
 * Change    : DGS15NOV2004
 * Why       : In multilistrowopen, fixed bug for radio with single button on line
 * 
 * Change    : DGS06JAN2005
 * Why       : processMultiList and processMultiListNonSQL now call a common internal function
 * 
 * Change    : V1.4:25 - DGS21JAN2005
 * Why       : In formSpellCheckButton, when creating a spellcheck button, javascript creates a
 *             variable 'zXCheckClick = 1'. The 'onMouseDown' of the spell check button sets that
 *             variable to 2.
 * 
 * Change    : V1.4:32 - BD6FEB05
 * Why       : Added support for image in search form order by label
 * 
 * Change    : V1.4:43 - DGS18FEB2005
 * Why       : Improved bound enhancers so that they still work when the dependant entity
 *             can have duplicate 'PK's with different relationship to master entity.
 *             
 * Change    : V1.4:53 - BD28FEB05
 * Why       : In edit forms (edit, grid, matrix) if there is a visible group we want to
 *             show the attributes in the sequence of the visible attribute group (ie not
 *             in the sequence of the edit group).
 *             One could argue that this was better sorted out in the clsPFEditxxx but
 *             it was much easier (and thus with lower risk) to implement at the
 *             HTML level
 *             
 * Change    : V1.4:58 - DGS/MB22MAR2005
 * Why       : In submitButton now disables the button on click. Prevents multiple submits.
 *
 * Change    : V1.5:1 - BD30MAR05
 * Why       : Support for data sources
 * 
 * Change    : V1.5:19 - BD31MAY05
 * Why       : Bug in listform with sort option activated; forgot to reset strColumn so that if
 *             a non-sort-able column (eg dynamic value) would be preceded by a sort-able column
 *             the non-sortable column would have same sort option as the previous sort-able one
 *
 * Change    : V1.5:20 - BD1JUL05
 * Why       : Support for enhanced FK label behaviour
 *
 * Change    : V1.5:25 - BD1JUL05
 * Why       : Support for sort attribute for dynamic values
 * 
 * Change    : V1.5:37 - BD13JUL05
 * Why       : Fixed bug introduced in 1.5:20 with locked FK controls
 * 
 * Change    : V1.5:41 - MB09AUG05
 * Why       : Add support for new fklookup control
 *
 * Change    : V1.5:51 - BD5SEP05
 * Why       : Bug with attributes with a default value and an option list on
 *             search-forms; the system assigns the default value to the attribute
 *             and this is the value that is automatically selected as the value on
 *             the search form; if you do not pay attention than this is thus included
 *             in the search
 *             
 * Change    : V1.5:61 - BD3OCT05
 * Why       : Fixed support with search form FK fields that have a value;
 *             simply ignore this....
 * 
 * Change    : V1.5:83 - BD21JAN06
 * Why       : Medum sizd entities with a restrictive enhancer must turn to small entities
 *
 * Change    : V1.5:85 - MB06FEB2006
 * Why       : The new AJAX control now supports bound attributes.
 *
 * Change    : V1.5:95 BD/DGS28APR2006
 * Why       : - New feature to be able to make ref buttons appear at the top of forms and
 *               behave like a menubar
 *             - Replace CR and CRLF by <br> in multiline fields
 *             - Very serious bug in FKControl when dealing with optional FKs to large
 *               entities that are null
 * 
 * Change    : V1.5:99 - BD2JUN06
 * Why       : Fixed bug when mandatory FK attribute has value #null (possible for new objects)
 *
 * Change    : V1.5:100 - MB12JUN06
 * Why       : Fixed bug when processing the editEnhancers; possibility of generating duplicate
 *               Javascript code because loop-variables where not cleared with every iteration
 *         
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PageBuilder extends ZXObject {

    //------------------------------------------------------------- Memebers
    
    private Pageflow pageflow;
    
    protected HTMLGen s; // Equiv to clsConcat ?
    
    /** Represents a html space. **/
    protected static final char[] NBSP = "&nbsp;".toCharArray();
    
    /** The name to store the handle of the Pagebuilder in the JSP Pagecontext. **/
    public static final String JSPCNTXT = "zXPage";
    
    protected ArrayList postLoadJavascript;
    
    private HierMenu hierMenu;
    
    private StringBuffer debugHTML = new StringBuffer();
    
    //------------------------------------------------------------- Constructors
    
    /**
     * Default constructor.
     */
    public PageBuilder() {
        super();
        
        /**
         * New empty string buffer:
         */
        this.s = new HTMLGen(1024);
        
        // Init any values that are needed.
        postLoadJavascript = new ArrayList();
    }
    
    //------------------------------------------------------------- Getters and Setters.
    
    /**
     * @return The debug message built up so far.
     */
    public StringBuffer getDebugHTML() {
        return debugHTML;
    }
    
    /**
     * @param debugHTML
     */
    public void setDebugHTML(StringBuffer debugHTML) {
        this.debugHTML = debugHTML;
    }
    
    /**
     * Collection of Javascript commands that can be
     *  included at end of page. This collection is only
     *  relevant when used from pageflow and is populated
     *  by enhancers and must be cleared and used by
     *  pageflow class.
     * 
     * @return Returns the postLoadJavascript.
     */
    public ArrayList getPostLoadJavascript() {
        return postLoadJavascript;
    }
    
    /**
     * @param postLoadJavascript The postLoadJavascript to set.
     */
    public void setPostLoadJavascript(ArrayList postLoadJavascript) {
        this.postLoadJavascript = postLoadJavascript;
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
     * @return Returns the hierMenu.
     */
    public HierMenu getHierMenu() {
        if(this.hierMenu == null) {
            this.hierMenu = new HierMenu();
            this.hierMenu.init(this);
        }
        return hierMenu;
    }
    
    /**
     * @param hierMenu The hierMenu to set.
     */
    public void setHierMenu(HierMenu hierMenu) {
        this.hierMenu = hierMenu;
    }
    
    private WebSettings webSettings;
    
    /**
     * Short have for getZx().getSettings().getWebSettings().
     * 
     * Also handles messaged websettings.
     * 
     * @return Returns the webSettings.
     */
    public WebSettings getWebSettings() {
        /**
         * Host a clean cloned version of the Html settings.
         */
        if (this.webSettings != null) {
            return this.webSettings;
        }
        
        return getZx().getSettings().getWebSettings();
    }
    
    /**
     * Update update to Websettings.
     * 
     * Package level.
     * 
     * @param webSettings Update the handle to Websettings.
     */
    void setWebSettings(WebSettings webSettings) {
        this.webSettings = webSettings;
    }
    
    //------------------------------------------------------------- Public methods.
    
    /**
     * Factory method for pageflow object in a Servlet environment.
     *
     * <pre>
     * 
     * NOTE  : Make the XMLBuilider a subclass of Builder and more the HTML specific stuff into 
     * HTMLBuilder. For now, PageBuilder with genernate HTML :( until we have decided on the format etc..
     * NOTE : Make getPageFlow environment independent so that it can work in a Servlet and Client Server environ.
     * </pre>
     *
     * @param pobjRequest The HttpServlet Request.
     * @param pobjResponse The HttpServlet Response.
     * @param pstrPageflow The pageflow to execute
     * @return Returns the pageflow.
     * @throws ZXException Thrown if getPageflow fails. 
     */
    public Pageflow getPageflow(HttpServletRequest pobjRequest, 
    							HttpServletResponse pobjResponse, 
    							String pstrPageflow) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pobjResponse", pobjResponse);
            getZx().trace.traceParam("pstrPageflow", pstrPageflow);
        }
        
        Pageflow getPageflow = null; 
        try {
            
            this.pageflow = new Pageflow();
            this.pageflow.init(this, pobjRequest, pobjResponse, pstrPageflow);
            
            // Could be used like this in the COM+ version.
            //s.append(this.pageflow.processPFIncludes());
            
            getPageflow = this.pageflow;
            
            return getPageflow;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Factory method for pageflow object.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
                getZx().log.error("Parameter : pobjResponse = "+ pobjResponse);
                getZx().log.error("Parameter : pstrPageflow = "+ pstrPageflow);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getPageflow;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(getPageflow);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for form title.
     *
     * @param penmFormtype The type action the title is for.
     * @param pstrTitle The title of the pageflow action.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formTitle fails. 
     */
    public zXType.rc formTitle(zXType.webFormType penmFormtype, String pstrTitle) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormtype", penmFormtype);
            getZx().trace.traceParam("pstrTitle", pstrTitle);
        }
        
        zXType.rc formTitle = zXType.rc.rcOK; 

        try {
            /**
             * If no title: not much to do...
             */
            if (StringUtil.len(pstrTitle) == 0) {
                formTitle = zXType.rc.rcOK;
                return formTitle;
            }
            
            s.appendNL("<table width=\"100%\">");
            s.appendNL("<tr>");
            
            /**
             * Force left margin for some form types
             */
            if (penmFormtype.equals(zXType.webFormType.wftMenu)) {
                s.append("<td ").appendAttr("width", getWebSettings().getMenuColumn1()).appendNL('>');
                s.appendNL("</td>");
            }
            
            s.append("<td ").appendAttr("class", "zXFormTitle").appendAttr("width", "*").appendNL('>');
            
            /**
             * See if a bullet image is required
             */
            if (StringUtil.len(getWebSettings().getTitleBulletImage()) > 0) {
                s.append("<img ").appendAttr("src", "../images/" + getWebSettings().getTitleBulletImage()).appendNL("/>");
            }
            
            s.appendNL(pstrTitle);
            s.appendNL("</td>");
            s.appendNL("</tr>");
            s.appendNL("</table>");
            
            return formTitle;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for form title.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormtype = "+ penmFormtype);
                getZx().log.error("Parameter : pstrTitle = "+ pstrTitle);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            formTitle = zXType.rc.rcError;
            return formTitle;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formTitle);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Some special s appenders.
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.s.toString();
    }
    
    /**
     * @return Returns the html generated and flushes it.
     */
    public String flush() {
        return flush(true);
    }
    
    /**
     * @param pblnReset Whether to flush the contents.
     * @return Returns the contents.
     */
    public String flush(boolean pblnReset) {
        String toString = this.s.toString();
        if (pblnReset) {
            s = new HTMLGen();
        }
        return toString;
    }
    
    /**
     * @param pintSpaces The number of spaces
     * @return Return HTML space code ie : "&amp;nbsp;"
     */
    public static char[] space(int pintSpaces) {
        int intNBSP = NBSP.length;
        int p = 0;
        char space[] =  new char[pintSpaces*intNBSP];
        for (int i = 0; i < pintSpaces; i++) {
            for (int j = 0; j < intNBSP; j++) {
                space[p] = NBSP[j];
                p++;
            }
        }
        return space;
    }
    
    /**
     * Reset the build html.
     */
    public void reset() {
        s = new HTMLGen(1024);
    }
    
    /**
     *<pre>
     *
     * Putting tracing code in here is overkill.
     *</pre>
     * @return Returns a single br tag.
     */
    public String br() {
        return "<br/>\n";
    }
    
    /**
     * Return HTML br code. 
     * 
     *<pre>
     *
     *Putting tracing code in here is overkill.
     *</pre>
	 * @param pintHowMany The number of br's to return.
	 * @return Returns the htmls br tags.
	 */
    public String br(int pintHowMany) {
		StringBuffer br = new StringBuffer( ( 5* pintHowMany) + 1 );
		
		for (int i = 0; i < pintHowMany; i++) {
		    br.append("<br/>");
		}
		
		return br.append(HTMLGen.NL).toString();
	}
    
    /**
     * Generate HTML for errorrmational message.
     *
     * @param pstrMsg The message to print. 
     */
    public void alertMsg(String pstrMsg) {
        s.appendNL("<table width=\"100%\">");
        s.appendNL("<tr>");
        s.appendNL("<td width=\"*\" class=\"zxAlert\" >");
        s.appendNL(StringUtil.replaceAll(pstrMsg, '\n', "<br>"));
        s.appendNL("</td>");
        s.appendNL("</tr>");
        s.appendNL("</table>");
    }
    
    /**
     * Generate HTML for informational message.
     *
     * @param pstrMsg The informational message 
     */
    public void infoMsg(String pstrMsg) {
        s.appendNL("<table width=\"100%\">");
        s.appendNL("<tr>");
        s.appendNL("<td valign=\"top\" width=\"7%\"><IMG src=\"../images/softAttention.gif\" /></td>");
        s.appendNL("<td width=\"*\" class=\"zxInfoMsg\" >");
        s.appendNL(StringUtil.replaceAll(pstrMsg, '\n', "<br>"));
        s.appendNL("</td>");
        s.appendNL("</tr>");
        s.appendNL("</table>");
    }
    
    /**
     * Generate HTML for errorrmational message.
     *
     * @param pstrMsg The error message 
     */
    public void errorMsg(String pstrMsg) {
        if (StringUtil.len(pstrMsg) > 0) {
            s.appendNL("<table width=\"100%\">");
            s.appendNL("<tr>");
            s.appendNL("<td valign=\"top\" width=\"7%\"><IMG src=\"../images/attention.gif\" /></td>");
            s.appendNL("<td width=\"*\" class=\"zxErrorMsg\" >");
            s.appendNL(StringUtil.replaceAll(pstrMsg, '\n', "<br>"));
            s.appendNL("</td>");
            s.appendNL("</tr>");
            s.appendNL("</table>");
        }
    }
    
    /**
     * No return from this - serious error to display and then end.
     * 
     * @param pstrMsg The message to display.
     * @return Returns a message.
     */
    public String fatalError(String pstrMsg) {
        String fatalError = "";
        
        fatalError = "Severe error: " + pstrMsg + "<br/>";
        
        reset();
        
        // Display the full error message.
        formatErrorStack(true);
        
        fatalError = fatalError + flush();
        
        return fatalError;
    }
    
    /**
     * Generate HTML with the outstanding error stack.
     */
     public void formatErrorStack() {
         formatErrorStack(false);
     }
     
    /**
     * Generate HTML with the outstanding error stack.
     *
     * @param pblnFull Whether to print out the full error message 
     */
    public void formatErrorStack(boolean pblnFull) {
        errorMsg(getZx().trace.formatStack(pblnFull));
    }
    

    /**
     * Generate HTML for debug message.
     * 
     *<pre>
     *
     *Putting tracing code in here is overkill.
     *</pre>
     *
     * @param pstrMsg The message to print out.
     */
    public void debugMsg(String pstrMsg){
        
        // OLD Way -- Well this had one advantage is was context aware ...
        //s.append("<SPAN class=\"zxDebug\">").append(HTMLGen.NL);
        //s.append("<b>DEBUG :</b> ").append(pstrMsg).append(br()).append(HTMLGen.NL);
        //s.append("</SPAN>").append(HTMLGen.NL);
        
        this.debugHTML.append("<SPAN class=\"zxDebug\">").append(HTMLGen.NL);
        this.debugHTML.append("DEBUG : ").append(pstrMsg).append(br());
        this.debugHTML.append("</SPAN>").append(HTMLGen.NL);
        
    }
    
    /**
     * Generate HTML for narrative. 
     * 
     * <pre>
     * 
     * The narrative is displayed as given, except that any LF are
     * replaced by <BR>. Note that vbCrLf will not exist in the narrative because it is held as
     * CDATA, and CDATA converts CR/LF into LF.
     * </pre>
     *
     * @param pstrNarr The narrative to print out on the screen 
     * @throws ZXException  Thrown if narrative fails. 
     */
    public void narrative(String pstrNarr) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrNarr", pstrNarr);
        }
        
        try {
            
            s.append("<table width=\"100%\">").append(HTMLGen.NL);
            s.append("<tr>").append(HTMLGen.NL);
            s.append("<td valign=\"top\" width=\"3%\"><IMG src=\"../images/narr.gif\"></td>").append(HTMLGen.NL);
            s.append("<td width=\"*\" class=\"zXNarr\" >").append(HTMLGen.NL);
            s.append(StringUtil.replaceAll(pstrNarr, '\n', "<br>")).append(HTMLGen.NL);
            s.append("</td>").append(HTMLGen.NL);
            s.append("</tr>").append(HTMLGen.NL);
            s.append("</table>").append(HTMLGen.NL);
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for narrative.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrNarr = "+ pstrNarr);
            }
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for search form.
     * 
     * Reviewed for 1.5:1
     * 
     * @param pobjBO The business object to display on the search form.  
     * @param pstrGroup The attribute group to display on the search form./
     * @return Returns the return code of the method. 
     * @throws ZXException  Thrown if searchForm fails. 
     */
    public zXType.rc searchForm(ZXBO pobjBO, String pstrGroup) throws ZXException{
        zXType.rc searchForm = zXType.rc.rcOK; 
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        try {
            DSHandler objDSHandler = pobjBO.getDS();
            
            /**
             * Does ds handler support search?
             */
            if (StringUtil.len(objDSHandler.getSearchGroup()) == 0) {
                throw new ZXException("Datasource handler has no search group support");
            }
            
            AttributeCollection colSearchGroup = pobjBO.getDescriptor().getGroup(objDSHandler.getSearchGroup());
            if (colSearchGroup == null) {
                throw new ZXException("Could not resolve data-source handler search group", objDSHandler.getSearchGroup());
            }
            
            /**
             * Allow user to do something special in prepersist
             */
            if (pobjBO.prePersist(zXType.persistAction.paSearchForm, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of pre-persist");
            }
            
            /**
             * Get group
             */
            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) {
                throw new Exception("Unable to generate entry for attribute");
            }
            
            s.append("<table width=\"100%\">").append(HTMLGen.NL);
            
            Attribute objAttr; 
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if (colSearchGroup.inGroup(objAttr.getName())) {
                    formEntry(zXType.webFormType.wftSearch, pobjBO, objAttr, false, true);
                } // Attribute in search group?
                
            }
            
            s.append("</table>").append(HTMLGen.NL);
            
            /**
             * Allow user to do something special in postpersist
             */
            pobjBO.setLastPostPersistRC(searchForm);
            if (pobjBO.postPersist(zXType.persistAction.paSearchForm, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of post-persist");
            }
            searchForm = pobjBO.getLastPostPersistRC();
            
            return searchForm;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for search form.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            searchForm = zXType.rc.rcError;
            return searchForm;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(searchForm);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for an edit form entry.
     *
     * @param penmFormType The type of form. 
     * @param pobjBO The business object used. 
     * @param pobjAttr The attribute to be displayed 
     * @param pblnLocked Whether to locked the field  or not? 
     * @param pblnVisible Whether to make the field visible or not 
     * @return Returns the return code of the method. 
     * @throws ZXException  Thrown if formEntry fails. 
     */
    public zXType.rc formEntry(zXType.webFormType penmFormType, 
                               ZXBO pobjBO, 
                               Attribute pobjAttr, 
                               boolean pblnLocked, 
                               boolean pblnVisible) throws ZXException{
        return formEntry(penmFormType, pobjBO, pobjAttr, pblnLocked, pblnVisible, null, null, null);
    }
    
    /**
     * Generate HTML for an edit form entry.
     *
     * @param penmFormType The type of form. 
     * @param pobjBO The business object used. 
     * @param pobjAttr The attribute to be displayed 
     * @param pblnLocked Whether to locked the field  or not? 
     * @param pblnVisible Whether to make the field visible or not 
     * @param pstrOnBlur The onBlur action.
     * @param pstrOnChange The onChange action.
     * @param pstrOnKeyPress The onKeyPress action
     * @return Returns the return code of the method. 
     * @throws ZXException  Thrown if formEntry fails. 
     */
    public zXType.rc formEntry(zXType.webFormType penmFormType, 
                                ZXBO pobjBO, 
                                Attribute pobjAttr, 
                                boolean pblnLocked, 
                                boolean pblnVisible, 
                                String pstrOnBlur, 
                                String pstrOnChange, 
                                String pstrOnKeyPress) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pblnLocked", pblnLocked);
            getZx().trace.traceParam("pblnVisible", pblnVisible);
            getZx().trace.traceParam("pstrOnBlur", pstrOnBlur);
            getZx().trace.traceParam("pstrOnChange", pstrOnChange);
            getZx().trace.traceParam("pstrOnKeyPress", pstrOnKeyPress);
        }
        
        zXType.rc formEntry = zXType.rc.rcOK; 
        
        /**
         * Handle defaults :
         */
        if (StringUtil.len(pstrOnBlur) == 0)   pstrOnBlur = ""; 
        if (StringUtil.len(pstrOnChange) == 0) pstrOnChange = "";
        if (StringUtil.len(pstrOnKeyPress) == 0) pstrOnKeyPress = "";
        
        try {
            boolean blnMergeWithPrevious = getEditEnhancerBoolean(pobjBO, pobjAttr,"mergeWithPrevious");
            
            /**
             * Each form entry has 2 or 3 columns (depending on the form type):
             * - The label
             * - search operand (only search form)
             * - The actual input field, followed by optional items
             *    like a FK search button or a calendar button
             */
            if (pblnVisible) {
                /**
                 * We probably have to open a new line (as by default all
                 * fields are on a new line; however, an enhancer may exists for an
                 * edit form that tells to merge with the previous
                 */
                if (penmFormType.equals(zXType.webFormType.wftEdit)) {
                    if(!blnMergeWithPrevious) {
                        s.append("<tr>").append(HTMLGen.NL);
                    }
                } else {
                    s.append("<tr>").append(HTMLGen.NL);   
                }
                
                /**
                 * Generate the label column only of this is a visible field
                 */
                formEntryLabel(penmFormType, pobjBO, pobjAttr);
                
            } else {
                /**
                 * Make the whole row invisible.
                 * See comment above for why a new row may not be started
                 */
                if(!blnMergeWithPrevious) {
                    s.append("<tr style=\"display:none\">").append(HTMLGen.NL);
                    
                    /**
                     * Generate the label equivalent (obviously there is not label
                     * for hidden fields
                     */
                    if (penmFormType.equals(zXType.webFormType.wftEdit)) {
                        s.append("<td ").appendAttr("width", getWebSettings().getEditFormColumn1()).append("/>").append(HTMLGen.NL);
                    } else {
                        s.append("<td ").appendAttr("width", getWebSettings().getSearchFormColumn1()).append("/>").append(HTMLGen.NL);
                    }
                    
                } // Merged with previous line
                
            }
            
            if (penmFormType.equals(zXType.webFormType.wftSearch)) {
                formSearchOpsControl(pobjBO, pobjAttr);
            }
            
            /**
             * Generate the actual input box
             */
            entryInputControl(penmFormType, pobjBO, pobjAttr, pblnLocked, pblnVisible, pstrOnBlur, pstrOnChange, pstrOnKeyPress);
            
            /**
             * If we merge with the next line, do not end this line
             */
            if (!getEditEnhancerBoolean(pobjBO, pobjAttr, "mergeWithNext")) {
                s.append("</tr>").append(HTMLGen.NL);
            }
            
            return formEntry;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for a form entry.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pblnLocked = "+ pblnLocked);
                getZx().log.error("Parameter : pblnVisible = "+ pblnVisible);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            formEntry = zXType.rc.rcError;
            return formEntry;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntry);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for an form entry label.
     *
     * @param penmFormtype The form type 
     * @param pobjBO The business object the label belongs to. 
     * @param pobjAttr The attribute to display the label for. 
     * @return Returns the return code of the method.
     * @throws ZXException  Thrown if formEntryLabel fails. 
     */
    public zXType.rc formEntryLabel(zXType.webFormType penmFormtype, ZXBO pobjBO, Attribute pobjAttr) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormtype", penmFormtype);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
        }

        zXType.rc formEntryLabel = zXType.rc.rcOK; 
        
        try {
            boolean blnMergeWithPrevious = getEditEnhancerBoolean(pobjBO, pobjAttr, "mergeWithPrevious");
            
            /**
             * DGS10MAR2003: Editform enhancer can override the standard label class
             */
            String strLabelClass = getEditEnhancerProperty(pobjBO, pobjAttr, "labelclass");
            if (StringUtil.len(strLabelClass) == 0) {
                if (!blnMergeWithPrevious) {
                    strLabelClass = "zXFormLabel";
                } else {
                    strLabelClass = "zXFormLabelMerged";
                }
            }
            
            boolean blnUsedSpanInsteadOfCell = false;
            
            if (penmFormtype.equals(zXType.webFormType.wftEdit)) {
                /**
                 * Perhaps no label is required
                 */
                if (getEditEnhancerBoolean(pobjBO,pobjAttr, "noLabel")) return formEntryLabel;
                
                /**
                 * In case of a merge with previous field, we cannot start a new
                 * table cell (as this is done in the previous field handler); in
                 * order to be able to use a class, we use a span instead
                 */
                if (!blnMergeWithPrevious) {
                    s.append("<td ")
                     .appendAttr("class", strLabelClass)
                     .appendAttr("width", getWebSettings().getEditFormColumn1())
                     .appendAttr("xxvalign", "top") // disabled? 
                     .appendNL('>');
                } else {
                    s.append("<span ")
                     .appendAttr("class", strLabelClass)
                     .appendNL('>');
                    blnUsedSpanInsteadOfCell = true;
                }
                
            } else {
                s.append("<td ")
                 .appendAttr("class", strLabelClass)
                 .appendAttr("width", getWebSettings().getSearchFormColumn1())
                 .appendAttr("xxvalign", "top") 
                 .appendNL('>');
                
            }
            
            s.append("<label for=\"_id_").append(controlName(pobjBO, pobjAttr)).appendNL("\">");
            s.appendNL(pobjAttr.getLabel().getLabel());
            s.appendNL("</label>");
            
            
            /**
             * Close either span or cell (see comment above)
             */
            if (blnUsedSpanInsteadOfCell) {
                s.append("&nbsp;</span>").append(HTMLGen.NL);
            } else {
                s.append("</td>").append(HTMLGen.NL);
            }
            
            return formEntryLabel;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for an form entry label.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormtype = "+ penmFormtype);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            formEntryLabel = zXType.rc.rcError;
            return formEntryLabel;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntryLabel);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get a property value from the edit enhancer for this attribute. 
     * 
     * <pre>
     * 
     * If there is none, return the empty string. If there is more 
     * than one enhancer, return the string from the first. 
     * Sometimes it is valid to have more than one enhancer 
     * for an attribute but when this function is called we only 
     * expect one, and will use the first.
     * </pre>
     * 
     * @param pobjBO The bo the has the property.
     * @param pobjAttr The attribute to get the edit enhancer for. 
     * @param pstrProperty The property to select 
     * @return Returns the HTML for a single edit enhancer.
     * @throws ZXException Thrown if getEditEnhancerProperty fails. 
     */
    private String getEditEnhancerProperty(ZXBO pobjBO, Attribute pobjAttr, String pstrProperty) throws ZXException{
    	
        String getEditEnhancerProperty = null; 
        
        try {
            PFEditEnhancer objEditEnhancer;
            if (pobjBO.getEditEnhancers(pobjAttr.getName()) != null) {
                objEditEnhancer = (PFEditEnhancer)pobjBO.getEditEnhancers(pobjAttr.getName()).get(0);
            } else {
                getEditEnhancerProperty = "";
                return getEditEnhancerProperty;
            }
            
            /**
             * Short cut way of accessing an edit enhancer.
             * NOTE : All properties must be lower cases
             */
            // getEditEnhancerProperty = this.pageflow.resolveDirector((String)BeanUtils.getProperty(objEditEnhancer, pstrProperty.toLowerCase()));
            
            if (objEditEnhancer != null) {
            	if (pstrProperty.equalsIgnoreCase("labelclass")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getLabelclass());
	                
            	} else if (pstrProperty.equalsIgnoreCase("inputclass")) {
            		getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getInputClass());
            
            	} else if (pstrProperty.equalsIgnoreCase("stdbuttonclass")) {
            		getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getStdbuttonclass());
            
            	} else if (pstrProperty.equalsIgnoreCase("tabindex")) {
            		getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getTabindex());
            
	            } else if (pstrProperty.equalsIgnoreCase("onfocus")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getOnfocus());
	            
	        	} else if (pstrProperty.equalsIgnoreCase("onblur")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getOnblur());
	                
	        	} else if (pstrProperty.equalsIgnoreCase("onchange")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getOnchange());
	            
	    		} else if (pstrProperty.equalsIgnoreCase("onkeydown")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getOnkeydown());
	            
	            } else if (pstrProperty.equalsIgnoreCase("onkeypress")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getOnkeypress());
	            
	            } else if (pstrProperty.equalsIgnoreCase("onkeyup")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getOnkeyup());
	            
	            } else if (pstrProperty.equalsIgnoreCase("onmouseover")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getOnmouseover());
	            
	            } else if (pstrProperty.equalsIgnoreCase("onmouseout")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getOnmouseout());
	            
	            } else if (pstrProperty.equalsIgnoreCase("onmousedown")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getOnmousedown());
	            
	            } else if (pstrProperty.equalsIgnoreCase("onmouseup")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getOnmouseup());
	            
	            } else if (pstrProperty.equalsIgnoreCase("entitysize")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getEntitysize());
	                
	            } else if (pstrProperty.equalsIgnoreCase("postlabel")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getPostlabel());
	            
	            } else if (pstrProperty.equalsIgnoreCase("postlabelclass")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getPostlabelclass());
	                
	            /**
	             * BD1MAR05 - V1.4:54
	             **/
	            } else if (pstrProperty.equalsIgnoreCase("multilinerows")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getMultilinerows());
	            
	            } else if (pstrProperty.equalsIgnoreCase("multilinecols")) {
	                getEditEnhancerProperty = this.pageflow.resolveDirector(objEditEnhancer.getMultilinecols());
	                
	            } else {
	            	/**
	            	 * At least return an empty string.
	            	 */
	            	getEditEnhancerProperty = "";
	            }
            }
        
            return getEditEnhancerProperty;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get a property value from the edit enhancer for this attribute.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrProperty = "+ pstrProperty);
            }
            if (getZx().throwException) throw new ZXException(e);
            return getEditEnhancerProperty;
        }
    }
    
    /**
     * Generate HTML for search operand box.
     * 
     * Reviewed for 1.5:1
     * 
     * @param pobjBO The business object for this form 
     * @param pobjAttr The attribute you want the list for 
     * @return Returns the return code of the method. 
     * @throws ZXException  Thrown if formSearchOpsControl fails. 
     */
    public zXType.rc formSearchOpsControl(ZXBO pobjBO, Attribute pobjAttr) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
        }

        zXType.rc formSearchOpsControl = zXType.rc.rcOK;
        
        try {
            DSHandler objDSHandler = pobjBO.getDS();
            
            String strControlName = controlName(pobjBO, pobjAttr);
            
            s.append("<td ")
             .appendAttr("width", getWebSettings().getSearchFormColumn2())
             .appendAttr("align","right")
             .appendAttr("xxxvalign", "top")
             .appendNL('>');
            
            s.append("<select name=\"zXOps").append(strControlName).append("\" ")
             .appendAttr("class", "zxSearchOps")
             .appendAttr("onChange", 
                          new StringBuffer("javascript:zXOnChangeSearchOps(zXOps")
                          .append(strControlName).append(",")
                          .append(strControlName)
                          .append(",'zXSpan").append(strControlName)
                          .append("');").toString()
                          )
              .appendNL('>');
            
            /**
             * For booleans we have a special operator '-' that basically
             * indicates that we can ignore this entry for the search. This
             * is needed as a boolean is represented as a checkbox and is thus
             * always true or false and never null
             */
            if (pobjAttr.getDataType().pos == zXType.dataType.dtBoolean.pos) {
                s.append("<option value=\"-\">-</option>").append(HTMLGen.NL);
            }
            
            if (objDSHandler.getSearchSupport().pos == zXType.dsSearchSupport.dsssFull.pos) {
                if (pobjAttr.getDataType().pos == zXType.dataType.dtString.pos 
                    && StringUtil.len(pobjAttr.getForeignKey()) == 0) {
                    s.append("<option value=\"sw\">Starts with</option>").append(HTMLGen.NL);
                    s.append("<option value=\"contains\">Contains</option>").append(HTMLGen.NL);
                }
            } // DS handler search support
            
            s.append("<option value=\"eq\">=</option>").append(HTMLGen.NL);
            
            /**
             * In case of a 'normal' control allow between as well
		     **/
            if (objDSHandler.getSearchSupport().pos >= zXType.dsSearchSupport.dsssStandard.pos) {
    		    if (usesNormalControl(pobjBO, pobjAttr)){
    		        s.append("<option value=\"btwn\">Between</option>").append(HTMLGen.NL);
    		    }
            }
            
            if (objDSHandler.getSearchSupport().pos > zXType.dsSearchSupport.dsssSimple.pos) {
                s.append("<option value=\"ne\">!=</option>").append(HTMLGen.NL);
            }
            
            if (objDSHandler.getSearchSupport().pos >= zXType.dsSearchSupport.dsssStandard.pos) {
                if (StringUtil.len(pobjAttr.getForeignKey()) == 0) {
                    s.append("<option value=\"gt\">></option>").append(HTMLGen.NL);
                    s.append("<option value=\"ge\">>=</option>").append(HTMLGen.NL);
                    s.append("<option value=\"lt\"><</option>").append(HTMLGen.NL);
                    s.append("<option value=\"le\"><=</option>").append(HTMLGen.NL);
                }
            }
            
            /**
             * DGS28JAN2004: Optional attributes get the 'is null' and 'not null' operators
             **/
            if (pobjAttr.isOptional()) {
                s.append("<option value=\"isNull\">Is Null</option>").append(HTMLGen.NL);
                s.append("<option value=\"notNull\">Is Not Null</option>").append(HTMLGen.NL);
            }
            
            s.append("</select>").append(HTMLGen.NL);
            
            s.append("</td>").append(HTMLGen.NL);
            
            return formSearchOpsControl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for search operand box.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            formSearchOpsControl = zXType.rc.rcError;
            return formSearchOpsControl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formSearchOpsControl);
                getZx().trace.exitMethod();
            }
        }
    }  
    
    /**
     * Generate control name.
     * 
     * <pre>
     * 
     * This is a generic function.
     * </pre>
     *
     * @param pobjBO The business object the control belongs to. 
     * @return Returns the controlname for use in javascripts.
     */
    public String controlName(ZXBO pobjBO) {
        return controlName(pobjBO, null);
    }
    
    /**
     * Generate control name.
     * 
     * <pre>
     * 
     * This is a generic function.
     * </pre>
     *
     * @param pobjBO The business object the control belongs to. 
     * @param pobjAttr The actual control 
     * @return Returns the controlname for use in javascripts.
     */
    public String controlName(ZXBO pobjBO, Attribute pobjAttr) {
		StringBuffer controlName = new StringBuffer(16); 
		
		controlName.append("ctr");
		
		if (StringUtil.len(pobjBO.getDescriptor().getAlias()) > 0) {
		    controlName.append( properCase(pobjBO.getDescriptor().getAlias()) );
		} else {
		    controlName.append(properCase(pobjBO.getDescriptor().getName()));
		}
		
		if (pobjAttr != null) {
		    controlName.append(properCase(pobjAttr.getName()));
		}
		
		/**
		 * BD6DEC02 remove any instance of '/' from the end result as this can
		 *  confuse Javascript. These characters can be around as a result of
		 *  business objects with a sub-classed name (e.g. tms/tmsTsk)
		 * 
		 *  DGS26FEB2004: Same with dots, which are often present in an alias
		 *  when it is constructed from grid and matrix edit form action names.
		 * 
		 * Replace . and / from the control name
		 */
		return StringUtil.stripChars("/.", controlName.toString());
    }
    
    /**
     * Give string first character as capital, all others lower case.
     * 
     * @param pstrValue The value to format.
     * @return Returns a proper formatted name.
     */
    public String properCase(String pstrValue) {
        StringBuffer properCase = new StringBuffer(pstrValue.length());
        
        properCase.append(Character.toUpperCase(pstrValue.charAt(0)));
        if (StringUtil.len(pstrValue) > 1) {
            properCase.append(pstrValue.substring(1).toLowerCase());
        }
        
        return properCase.toString();
    }
    
    /**
     * Close the button area.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with close button area
     * </pre>
     *
     * @param penmFormType The type of pageflow action this is being to used for.
     * @return Returns the return code of the method.
     */
    public zXType.rc buttonAreaClose(zXType.webFormType penmFormType) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
        }
        
        zXType.rc buttonAreaClose = zXType.rc.rcOK;
        try {
            /**
             * close button area
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit)) {
                // Do nothing.
            } else if (penmFormType.equals(zXType.webFormType.wftList)) {
                s.append("</td>").append(HTMLGen.NL)
               	 .append("<td ")
                 .appendAttr("width", getWebSettings().getEditFormColumn3())
                 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
                
            } else if (penmFormType.equals(zXType.webFormType.wftSearch)) {
                s.append("</td>").append(HTMLGen.NL)
                 .append("<td ")
                 .appendAttr("width", getWebSettings().getSearchFormColumn2())
                 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
                
            } else if (penmFormType.equals(zXType.webFormType.wftMenu)) {
                s.append("</td>").append(HTMLGen.NL);
                s.append("<td ")
                 .appendAttr("width", getWebSettings().getMenuColumn3())
                 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
                s.append("</td>").append(HTMLGen.NL);
                s.append("<td ")
            	 .appendAttr("width", getWebSettings().getMenuColumn4())
            	 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
            }
            
            s.append("</td>").append(HTMLGen.NL);
            s.append("</tr>").append(HTMLGen.NL);
            s.append("</table>").append(HTMLGen.NL);
            
            return buttonAreaClose;
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(buttonAreaClose);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Open the button area.
     * 
     * <pre>
     * 
     * Assumes   :
     *   Used in combination with close button area
     * </pre>
     *
     * @param penmFormType The type of web form. 
     * @return Returns the return code of the method.
     */
    public zXType.rc buttonAreaOpen(zXType.webFormType penmFormType) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
        }
        
        zXType.rc buttonAreaOpen = zXType.rc.rcOK; 
        try {
            /**
             * Open button area
             */
            s.append("<table width=\"100%\">").append(HTMLGen.NL);
            s.append("<tr>").append(HTMLGen.NL);
            
            /**
             * Edit form
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit)) {
                s.append("<td ")
            	 .appendAttr("width", getWebSettings().getEditFormColumn1())
           		 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
                s.append("</td>").append(HTMLGen.NL);
                s.append("<td ")
            	 .appendAttr("width", getWebSettings().getEditFormColumn2())
            	 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
            
            /**
             * Search Form
             */
            } else if (penmFormType.equals(zXType.webFormType.wftSearch)) {
                s.append("<td ")
            	 .appendAttr("width", getWebSettings().getSearchFormColumn1())
            	 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
                s.append("</td>").append(HTMLGen.NL);
                s.append("<td ")
            	 .appendAttr("width", getWebSettings().getSearchFormColumn3())
            	 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
            
            /**
             * List Form
             */
            } else if (penmFormType.equals(zXType.webFormType.wftList)) {
                s.append("<td ")
	             .appendAttr("width", getWebSettings().getListFormColumn1())
	             .appendAttr("class", "zxLabelPlain")
	             .appendNL('>');
                s.append("</td>").append(HTMLGen.NL);
                s.append("<td ")
	             .appendAttr("width", getWebSettings().getSearchFormColumn3())
	             .appendAttr("class", "zxLabelPlain")
	             .appendNL('>');
                
            } else if (penmFormType.equals(zXType.webFormType.wftMenu)) {
                s.append("<td ")
            	 .appendAttr("width", getWebSettings().getMenuColumn1())
            	 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
                s.append("</td>").append(HTMLGen.NL);
                s.append("<td ")
            	 .appendAttr("width", getWebSettings().getMenuColumn2())
            	 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
                
            /**
             * Null action
             */    
            } else if (penmFormType.equals(zXType.webFormType.wftNull)) {
                s.append("<td ")
            	 .appendAttr("width", getWebSettings().getSearchFormColumn3())
            	 .appendAttr("class", "zxLabelPlain")
                 .appendNL('>');
                
            }
            
            return buttonAreaOpen;
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(buttonAreaOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Open the menubar area.
     */
    public void menubarAreaOpen() {
        /**
         * Open menubar area 
         */
        s.appendNL("<table width=\"100%\">")
         .appendNL("<tr>")
         .append("<td ")
             .appendAttr("width", "100%")
             .appendAttr("class", "zxMenuBar")
         .appendNL(">");
    }
    
    /**
     * Close the menubar area.
     * 
     * <pre>
     * 
     * New for v1.5:95
     * Assumes   :
     *    Used in combination with close menubar area
     * </pre>
     */
    public void menubarAreaClose() {
        /**
         * close menubar area
         */
        s.appendNL("</td>")
         .appendNL("</tr>")
         .appendNL("</table>");
    }
    
    /**
     * Returns true if this attribute will use a 'normal' entry control, false otherwise.
     *
     * <pre>
     *
     * Assumes   :
     *	 Not normal:
     *     - Multiline
     *     - Foreign key
     *     - Option list
     *     - Boolean
     *</pre>
     *
     * @param pobjBO The business object the control belong to. 
     * @param pobjAttr The attribute you want to check 
     * @return Returns true if this attribute will use a 'normal' entry control, false otherwise.
     * @throws ZXException Thrown if usesNormalControl fails. 
     */
    public boolean usesNormalControl(ZXBO pobjBO, Attribute pobjAttr) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
        }

        boolean usesNormalControl = false; 
        
        try {
        	
            if(pobjAttr.isMultiLine()) {
            	/**
            	 * Textarea control.
            	 */
            	usesNormalControl = false;
            } else if (pobjAttr.getDataType().pos == zXType.dataType.dtBoolean.pos) {
            	/**
            	 * Checkbox control
            	 */
            	usesNormalControl = false;
            } else if (pobjAttr.getOptions().size() > 0) {
            	/**
            	 * Select list control.
            	 */
            	usesNormalControl = false;
            } else if (StringUtil.len(pobjAttr.getForeignKey())> 0) {
            	/**
            	 * FK control.
            	 */
            	usesNormalControl = false;
            } else {
            	/**
            	 * When it is none of the above it is a "normal" control.
            	 */
                usesNormalControl = true;
            }
            
            return usesNormalControl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Returns true if this attribute will use a 'normal' entry control, false otherwise.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
            }
            if (getZx().throwException) throw new ZXException(e);
            return usesNormalControl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(usesNormalControl);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for edit form input box.
     * 
     *<pre>
     *
     *NOTE : This calls entryInputControl(penmFormType, pobjBO, pobjAttr, true, false, null, null, null);
     *</pre>
     *
     * @param penmFormType The form type. 
     * @param pobjBO The business object of the control. 
     * @param pobjAttr The attribute for the control. 
     * @return Returns the return code of the method.
     * @throws ZXException  Thrown if entryInputControl fails. 
     */
    public zXType.rc entryInputControl(zXType.webFormType penmFormType, ZXBO pobjBO, Attribute pobjAttr) throws ZXException{
        return entryInputControl(penmFormType, pobjBO, pobjAttr, true, false, null, null, null);
    }
    
    /**
     * Generate HTML for edit form input box.
     *
     * @param penmFormType The form type. 
     * @param pobjBO The business object of the control. 
     * @param pobjAttr The attribute for the control. 
     * @param pblnLocked Whether the control is locked or not? Optional, default should be false. 
     * @param pblnVisible Whether the control is visible or not. Optional, the default should be false. 
     * @param pstrOnBlur The onBlur action.Optional, default is null. 
     * @param pstrOnChange The onChange action. Optional, the default should be null. 
     * @param pstrOnKeyPress The onKeyPress action. Optional, the default should  be null 
     * @return Returns the return code of the method. 
     * @throws ZXException  Thrown if entryInputControl fails. 
     */
    public zXType.rc entryInputControl(zXType.webFormType penmFormType, 
    								   ZXBO pobjBO, 
    								   Attribute pobjAttr, 
    								   boolean pblnLocked, 
    								   boolean pblnVisible, 
    								   String pstrOnBlur, 
    								   String pstrOnChange, 
    								   String pstrOnKeyPress) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pblnLocked", pblnLocked);
            getZx().trace.traceParam("pblnVisible", pblnVisible);
            getZx().trace.traceParam("pstrOnBlur", pstrOnBlur);
            getZx().trace.traceParam("pstrOnChange", pstrOnChange);
            getZx().trace.traceParam("pstrOnKeyPress", pstrOnKeyPress);
        }

        zXType.rc entryInputControl = zXType.rc.rcOK; 
        
        try {
            /**
             * START OF SETUP
             */
            
            /**
             * DGS10MAR2003: These event handlers are not passed in, so get them from edit form
             *  enhancers (if any):
             * 
             * BD8JUN04 - Small performance gain as many attributes would not have an
             * enhancer
             */
            String strOnFocus = "";
            String strOnKeyDown = "";
            String strOnKeyUp = "";
            String strOnMouseOver = "";
            String strOnMouseOut = "";
            String strOnMouseDown = "";
            String strOnMouseUp = "";
            String strOnClick = "";
            String strClass = "";
            String strPostLabel = "";
            String strPostLabelClass = "";
            boolean blnDisabled = false;
            String strTabIndex = "";
            
            if (pobjBO.getEditEnhancers(pobjAttr.getName()) != null) {
            	
                strOnFocus = getEditEnhancerProperty(pobjBO, pobjAttr, "onfocus");
                strOnKeyDown = getEditEnhancerProperty(pobjBO, pobjAttr, "onkeydown");
                strOnKeyUp = getEditEnhancerProperty(pobjBO, pobjAttr, "onkeyup");
                strOnMouseOver = getEditEnhancerProperty(pobjBO, pobjAttr, "onmouseover");
                strOnMouseOut = getEditEnhancerProperty(pobjBO, pobjAttr, "onmouseout");
                strOnMouseDown = getEditEnhancerProperty(pobjBO, pobjAttr, "onmousedown");
                strOnMouseUp = getEditEnhancerProperty(pobjBO, pobjAttr, "onmouseup");
                strOnClick = getEditEnhancerProperty(pobjBO, pobjAttr, "onclick");
                blnDisabled = getEditEnhancerBoolean(pobjBO, pobjAttr, "disabled");
                strTabIndex = getEditEnhancerProperty(pobjBO, pobjAttr, "tabindex");
                
                /**
                 * DGS10MAR2003: Append these event handlers from edit form enhancers (if any):
                 */
                pstrOnBlur = pstrOnBlur + getEditEnhancerProperty(pobjBO, pobjAttr, "onBlur");
                pstrOnChange = pstrOnChange + getEditEnhancerProperty(pobjBO, pobjAttr, "onChange");
                pstrOnKeyPress = pstrOnKeyPress + getEditEnhancerProperty(pobjBO, pobjAttr, "onKeyPress");
                
                /**
                 * DGS10MAR2003: Prepend the incoming 'OnChange' by any dependency enhancer code:
                 */
                StringBuffer strEnhancersOnChange = new StringBuffer();
                editFormEnhancers(pobjBO, pobjAttr, strEnhancersOnChange);
                pstrOnChange = strEnhancersOnChange + pstrOnChange;
                
                /**
                 * Determine the class. This is made up of a number of components:
                 *    zxFormInput
                 *    Locked | Mandatory | Optional
                 *    Numeric | Lower | Capital | Mixed | Upper
                 *  DGS10MAR2003: But overriding all of this can be an edit enhancer class:
                 */
                strClass = getEditEnhancerProperty(pobjBO, pobjAttr, "inputclass");            
                
                /**
                 * DGS28JUN2004: Get the post label and the label class enhancers
                 */
                strPostLabel = getEditEnhancerProperty(pobjBO, pobjAttr, "postlabel");
                strPostLabelClass = getEditEnhancerProperty(pobjBO, pobjAttr, "postLabelClass");
            }
            
            /**
             * Add tooltips :
             */
            // strOnMouseOver = "tt_tooltip(this, event, '" + tooltip(pobjBO, pobjAttr) + "');" + strOnMouseOver;
            strOnMouseOver = "";
            
            /**
             * DGS10MAR2004: Bit of an about face, but now we want to set the field to be NOT locked if
             * the enhancer Disabled is true. The effect of this is that disabled fields are shown in
             * their normal style but not enabled. This looks much better on the screen, especially in
             * grids and matrixes.
             */
            if (blnDisabled){
                pblnLocked = false;
            }
            
            /**
             * Build up the stylesheet class for the input control.
             */
            if (StringUtil.len(strClass) == 0) {
                strClass = "zxFormInput";
                
                if (pblnLocked) {
                    strClass = strClass + "Locked";
                    
                } else if (pobjAttr.isOptional()){
                    strClass = strClass + "Optional";
                    
                } else {
                    if (penmFormType.equals(zXType.webFormType.wftEdit) || penmFormType.equals(zXType.webFormType.wftGrid)) {
                        strClass = strClass + "Mandatory";
                    } else {
                        strClass = strClass + "Optional";
                    }
                    
                }
                
                if (pobjAttr.getDataType().pos == zXType.dataType.dtAutomatic.pos 
                        || pobjAttr.getDataType().pos == zXType.dataType.dtDouble.pos 
                        || pobjAttr.getDataType().pos == zXType.dataType.dtLong.pos ) {
                    /**
                     * Do not right align (this is what numeric causes) if locked
                     */
                    if (pblnLocked) {
                        strClass = strClass + "Mixed";
                    } else {
                        strClass = strClass + "Numeric";
                    }
                        
                } else {
                    zXType.textCase textCase = pobjAttr.getTextCase();
                    if (textCase.equals(zXType.textCase.tcCapital)) {
                        strClass = strClass + "Capital";
                        
                    } else if (textCase.equals(zXType.textCase.tcLower)){
                        strClass = strClass + "Lower";
                        
                    } else if (textCase.equals(zXType.textCase.tcUpper)){
                        strClass = strClass + "Upper";
                        
                     /**
                      * DGS12JUN2003: If case insensitive and a search form, make the field upper case. 
                      * It makes no difference to the resulting search but indicates to the user that it
                      *  is case insensitive.
                      */
                    } else if (textCase.equals(zXType.textCase.tcInsensitive)){
                        if (penmFormType.equals(zXType.webFormType.wftSearch)) {
                            strClass = strClass + "Upper";
                        } else {
                            strClass = strClass + "Mixed";
                        }
                        
                    } else {
                        strClass = strClass + "Mixed";
                        
                    }
                    
                }
                
            } // No specified class
            
            /**
             * END OF SETUP
             */
            
            /**
             * Here comes all the hard work; how this is done depends on various things:
             *    - Is the field locked?
             *    - Is the field visible?
             *    - Does the field have a foreign key?
             *    - Is it a boolean field (i.e. a checkbox)?
             *    - Does the field have an option list?
             *    - Is the field a multi-line field?
             *    - Any other scenario
             * 
             *  DGS10MAR2003: Changes to parameters passed to the following functions, as now have extensive
             *  range of event handlers definable in edit form enhancers.
             */
            if (!pblnVisible) {
                formEntryHiddenControl(penmFormType, 
                                       pobjBO, pobjAttr, strClass);
                
            } else if (pblnLocked) {
                
                formEntryLockedControl(penmFormType, 
                                       pobjBO, pobjAttr, strClass,
                                       pstrOnBlur,
                                       strPostLabel, strPostLabelClass);
                
            } else if (StringUtil.len(pobjAttr.getForeignKey()) > 0) {
                formEntryFKControl(penmFormType, 
                                   pobjBO, pobjAttr, strClass, 
                                   pstrOnBlur, pstrOnChange, 
                                   pstrOnKeyPress, blnDisabled, strTabIndex, 
                                   strOnFocus, strOnKeyDown, strOnKeyUp, strOnClick, 
                                   strOnMouseOver, strOnMouseOut, strOnMouseDown, strOnMouseUp,
                                   strPostLabel, strPostLabelClass);
                
            } else if (pobjAttr.getDataType().pos == zXType.dataType.dtBoolean.pos) {
                formEntryCheckboxControl(penmFormType, pobjBO, pobjAttr, strClass, 
                                         pstrOnBlur, pstrOnChange, 
                                         pstrOnKeyPress, blnDisabled, strTabIndex, 
                                         strOnFocus, strOnKeyDown, strOnKeyUp, strOnClick, 
                                         strOnMouseOver, strOnMouseOut, strOnMouseDown, strOnMouseUp,
                                         strPostLabel, strPostLabelClass);
                
            } else if (pobjAttr.getOptions().size() > 0){
                /**
                 * If there is an option list it may be a combo box
                 */
                if (!pobjAttr.isCombobox()) {
                    formEntrySelectControl(penmFormType, 
                                            pobjBO, pobjAttr, strClass, 
                                            pstrOnBlur, pstrOnChange, 
                                            pstrOnKeyPress, blnDisabled, strTabIndex, 
                                            strOnFocus, strOnKeyDown, strOnKeyUp, strOnClick, 
                                            strOnMouseOver, strOnMouseOut, strOnMouseDown, strOnMouseUp,
                                            strPostLabel, strPostLabelClass);
                    
                } else {
                    /**
                     * DGS10MAR2003: If have an editform enhancer for onfocus and/or onkeydown, append
                     *  to the standard handlers that must be called for combos:
                     *  DGS16JUN2003: No, don't append zXComboOnFocus to strOnFocus nor zXComboOnKeyDown
                     *  to strOnKeyDown here, because also do it in formEntryComboControl:
                     */
                    formEntryComboControl(penmFormType, 
                                          pobjBO, pobjAttr, strClass, 
                                          pstrOnBlur, pstrOnChange, pstrOnKeyPress, blnDisabled, strTabIndex, 
                                          strOnFocus, strOnKeyDown, strOnKeyUp, strOnClick, 
                                          strOnMouseOver, strOnMouseOut, strOnMouseDown, strOnMouseUp,
                                          strPostLabel, strPostLabelClass);
                    
                }
                
            } else if (pobjAttr.isMultiLine()){
                formEntryMultilineControl(penmFormType, 
                                          pobjBO, pobjAttr, strClass,
                                          pstrOnBlur, pstrOnChange, pstrOnKeyPress,
                                          blnDisabled, strTabIndex,
                                          strOnFocus, strOnKeyDown, strOnKeyUp, strOnClick,
                                          strOnMouseOver, strOnMouseOut, strOnMouseDown, strOnMouseUp,
                                          strPostLabel, strPostLabelClass);
                
            } else {
                formEntryNormalInputControl(penmFormType, 
                                            pobjBO, pobjAttr, strClass, 
                                            pstrOnBlur, pstrOnChange, pstrOnKeyPress, 
                                            blnDisabled, strTabIndex, 
                                            strOnFocus, strOnKeyDown, strOnKeyUp, strOnClick, 
                                            strOnMouseOver, strOnMouseOut, strOnMouseDown, strOnMouseUp,
                                            strPostLabel, strPostLabelClass);
                
            }
            
            return entryInputControl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for edit form input box.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pblnLocked = "+ pblnLocked);
                getZx().log.error("Parameter : pblnVisible = "+ pblnVisible);
                getZx().log.error("Parameter : pstrOnBlur = "+ pstrOnBlur);
                getZx().log.error("Parameter : pstrOnChange = "+ pstrOnChange);
                getZx().log.error("Parameter : pstrOnKeyPress = "+ pstrOnKeyPress);
            }
            if (getZx().throwException) throw new ZXException(e);
            entryInputControl = zXType.rc.rcError;
            return entryInputControl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(entryInputControl);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Get a boolean property of the edit enhancer for this attribute. 
     * 
     * <pre>
     * 
     * If there is none, return false. If there is more than one enhancer, 
     * return the value from the first. Sometimes it is valid to have more 
     * than one enhancer for an attribute but when this function is called 
     * we only expect one, and will use the first.
     * </pre>
     *
     * @param pobjBO The bo the has the property.
     * @param pobjAttr The attribute you want the enhancer from. 
     * @param pstrProperty The property you want to get. 
     * @return Returns the boolean value of the selected edit enhancer.
     * @throws ZXException Thrown if getEditEnhancerBoolean fails. 
     */
    public boolean getEditEnhancerBoolean(ZXBO pobjBO, Attribute pobjAttr, String pstrProperty) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pstrProperty", pstrProperty);
        }

        boolean getEditEnhancerBoolean = false; 
        
        try {
            
            PFEditEnhancer objEditEnhancer;
            if (pobjBO.getEditEnhancers(pobjAttr.getName()) != null) {
                objEditEnhancer = (PFEditEnhancer)pobjBO.getEditEnhancers(pobjAttr.getName()).get(0);
            } else {
                return getEditEnhancerBoolean;
            }
            
            if (objEditEnhancer != null) {
                if (pstrProperty.equalsIgnoreCase("spellcheck")){
                    getEditEnhancerBoolean = objEditEnhancer.isSpellCheck();
                    
                /**
                 * DGS09APR2003: New enhancer boolean to override FK show button behaviour
                 */
                } else if (pstrProperty.equalsIgnoreCase("fklookup")) {
                    getEditEnhancerBoolean = objEditEnhancer.isFkLookup();
                    
                /**
                 * DGS20AUG2003: New enhancer boolean to allow on-the-fly create of FK instance
                 */
                } else if (pstrProperty.equalsIgnoreCase("fkadd")) {
                    getEditEnhancerBoolean = objEditEnhancer.isFkAdd();
                    
                } else if (pstrProperty.equalsIgnoreCase("disabled")) {
                    /**
                     * Anything starting with Y or T is true, anything else is false
                     */
                    String strDisabled = objEditEnhancer.getDisabled();
                    if (StringUtil.len(strDisabled) > 0) {
                        char chr = Character.toUpperCase(this.pageflow.resolveDirector(objEditEnhancer.getDisabled()).charAt(0));
                        if (chr == 'Y' || chr == 'T') {
                            getEditEnhancerBoolean = true;
                        }
                    }
                    
                /**
                 * BD19JUL04 - Added to support merging multiple fields on a line
                 */
                } else if (pstrProperty.equalsIgnoreCase("mergewithprevious")) {
                    getEditEnhancerBoolean = objEditEnhancer.isMergeWithPrevious();
                } else if (pstrProperty.equalsIgnoreCase("mergewithnext")) {
                    getEditEnhancerBoolean = objEditEnhancer.isMergeWithNext();
                } else if (pstrProperty.equalsIgnoreCase("nolabel")) {
                    getEditEnhancerBoolean = objEditEnhancer.isNoLabel();
                }
            }
            
            return getEditEnhancerBoolean;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get a boolean property of the edit enhancer for this attribute.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrProperty = "+ pstrProperty);
            }
            if (getZx().throwException) throw new ZXException(e);
            return getEditEnhancerBoolean;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(getEditEnhancerBoolean);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for edit form field for normal input.
     *
     * @param penmFormType The formtype. 
     * @param pobjBO The business object. 
     * @param pobjAttr The attibute. 
     * @param pstrClass The css class. 
     * @param pstrOnBlur The onBlur action 
     * @param pstrOnChange The onChange action. 
     * @param pstrOnKeyPress The onKeyPress action. 
     * @param pblnDisabled Whether the form element is disabled 
     * @param pstrTabIndex The tab index. 
     * @param pstrOnFocus The onFocus action 
     * @param pstrOnKeyDown The onKeyDown action 
     * @param pstrOnKeyUp The onKeyUp action 
     * @param pstrOnClick The onClick action 
     * @param pstrOnMouseOver The onMouseOver action 
     * @param pstrOnMouseOut The onMouseOut action. 
     * @param pstrOnMouseDown The onMouseDown action 
     * @param pstrOnMouseUp The onMouseUp action 
     * @param pstrPostLabel The post label 
     * @param pstrPostLabelClass The post lable class 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formEntryNormalInputControl fails. 
     */
    public zXType.rc formEntryNormalInputControl(zXType.webFormType penmFormType, 
                                                 ZXBO pobjBO, 
                                                 Attribute pobjAttr, 
                                                 String pstrClass, 
                                                 String pstrOnBlur, 
                                                 String pstrOnChange, 
                                                 String pstrOnKeyPress, 
                                                 boolean pblnDisabled, 
                                                 String pstrTabIndex, 
                                                 String pstrOnFocus, 
                                                 String pstrOnKeyDown, 
                                                 String pstrOnKeyUp, 
                                                 String pstrOnClick, 
                                                 String pstrOnMouseOver, 
                                                 String pstrOnMouseOut, 
                                                 String pstrOnMouseDown, 
                                                 String pstrOnMouseUp,
                                                 String pstrPostLabel,
                                                 String pstrPostLabelClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pstrClass", pstrClass);
            getZx().trace.traceParam("pstrOnBlur", pstrOnBlur);
            getZx().trace.traceParam("pstrOnChange", pstrOnChange);
            getZx().trace.traceParam("pstrOnKeyPress", pstrOnKeyPress);
            getZx().trace.traceParam("pblnDisabled", pblnDisabled);
            getZx().trace.traceParam("pstrTabIndex", pstrTabIndex);
            getZx().trace.traceParam("pstrOnFocus", pstrOnFocus);
            getZx().trace.traceParam("pstrOnKeyDown", pstrOnKeyDown);
            getZx().trace.traceParam("pstrOnKeyUp", pstrOnKeyUp);
            getZx().trace.traceParam("pstrOnClick", pstrOnClick);
            getZx().trace.traceParam("pstrOnMouseOver", pstrOnMouseOver);
            getZx().trace.traceParam("pstrOnMouseOut", pstrOnMouseOut);
            getZx().trace.traceParam("pstrOnMouseDown", pstrOnMouseDown);
            getZx().trace.traceParam("pstrOnMouseUp", pstrOnMouseUp);
            getZx().trace.traceParam("pstrPostLabel", pstrPostLabel);
            getZx().trace.traceParam("pstrPostLabelClass", pstrPostLabelClass);
        }
        
        zXType.rc formEntryNormalInputControl = zXType.rc.rcOK; 
        
        try {
            String strControlName = controlName(pobjBO, pobjAttr);
            
            /**
             * Do the standard form entry header.
             */
            formEntryHeader(pobjBO, pobjAttr, penmFormType);
            
            /**
             * If edit, we have to put the current value of the field into the box
             */
            String strValue = "";
            DateFormat df;
            
            int intDataType = pobjAttr.getDataType().pos;
            if ((penmFormType.equals(zXType.webFormType.wftEdit) || penmFormType.equals(zXType.webFormType.wftGrid)) 
                && !pobjBO.getValue(pobjAttr.getName()).isNull) {
                if (intDataType == zXType.dataType.dtDate.pos) {
                    df = getZx().getDateFormat();
                    strValue = df.format(pobjBO.getValue(pobjAttr.getName()).dateValue());
                    
                } else if (intDataType == zXType.dataType.dtTimestamp.pos) {
                    df = getZx().getTimestampFormat();
                    strValue = df.format(pobjBO.getValue(pobjAttr.getName()).dateValue());
                    
                } else if (intDataType == zXType.dataType.dtTime.pos) {
                    df = getZx().getTimeFormat();
                    strValue = df.format(pobjBO.getValue(pobjAttr.getName()).dateValue());
                    
                } else {
                    strValue = pobjBO.getValue(pobjAttr.getName()).getStringValue();
                }
                
            }
            
            /**
             * The onChange event handler is a concatenation of the optional user-provided
             *  one and the standard zX one
             */
            StringBuffer strOnChange = new StringBuffer(42);
            if (StringUtil.len(pstrOnChange) > 0) {
                strOnChange.append(pstrOnChange).append("; ");
            }
            strOnChange.append("this.value = zXOnChange(this.value, ").append(pobjAttr.getDataType().pos).append(");");
            
            /**
             * Same for the keypress event handler
             */
            String strOnKeyPress = "return zXOnKeyPress(event, " + pobjAttr.getDataType().pos + ");";
            if (StringUtil.len(pstrOnChange) > 0) {
                strOnKeyPress = pstrOnKeyPress + "; " + strOnKeyPress;
            }
            
            /**
             * Ensure that the dates are always fully visible :
             */
            int intLength = pobjAttr.getOutputLength();
            if (intDataType == zXType.dataType.dtDate.pos && intLength < getZx().getSettings().getStrDateFormat().length() + 3) {
                intLength = getZx().getSettings().getStrDateFormat().length() + 3;
            } else if (intDataType == zXType.dataType.dtTimestamp.pos && intLength < getZx().getSettings().getStrTimestampFormat().length() + 3) {
                intLength = getZx().getSettings().getStrTimestampFormat().length() + 6;
            }
            
            s.append("<input ")
             .appendAttr("id", "_id_" + strControlName)
             .appendAttr("name", strControlName)
             .appendAttr("value", StringEscapeUtils.escapeHtml(strValue))
             .appendAttr("size", String.valueOf(intLength))
             .appendAttr("maxlength", String.valueOf(pobjAttr.getLength()))
             .appendAttr("type", pobjAttr.isPassword()?"password":"text" )
			 .appendAttr("class", pstrClass)
			 .appendAttr("disabled", pblnDisabled?"true":"")
			 .appendAttr("tabIndex", pstrTabIndex)
			 .appendAttr("onChange", strOnChange.toString())
			 .appendAttr("onKeypress", strOnKeyPress)
			 .appendAttr("onBlur", pstrOnBlur)
			 .appendAttr("onFocus", pstrOnFocus)
			 .appendAttr("onClick", pstrOnClick)
			 .appendAttr("onKeyDown", pstrOnKeyDown)
			 .appendAttr("onKeyUp", pstrOnKeyUp)
			 .appendAttr("onMouseOver", pstrOnMouseOver)
			 .appendAttr("onMouseOut", pstrOnMouseOut)
			 .appendAttr("onMouseDown", pstrOnMouseDown)
			 .appendAttr("onMouseUp", pstrOnMouseUp)
             .appendAttr("title", tooltip(pobjBO, pobjAttr)) 
             .appendNL('>');
            
            /**
             * In case of a date field: add the calendar button
             * DGS10MAR2003: but not if disabled
             */
            if (intDataType == zXType.dataType.dtDate.pos
                ||intDataType  == zXType.dataType.dtTimestamp.pos
                && !pblnDisabled) {
            	
            	buildDateButton(pobjBO, pobjAttr, "_id_" + strControlName, pblnDisabled);
            	
            }
            
            /**
             * In case of a search form we are going to generate 2 instances 
             * of the input control in case of the between function; note how 
             * we make the name unique by prefixing it wis zXUL (Upper Limit) 
             * and how all is enclosed in a span that is invisible to start with
             * (only displayed when the user selects the between option)
             */
            if (penmFormType.equals(zXType.webFormType.wftSearch)) {
                this.s.append("<span").append(HTMLGen.NL);
                this.s.appendAttr("id", "zXSpan" + strControlName).append(HTMLGen.NL);
                this.s.appendAttr("style", "display:none").append(HTMLGen.NL);
                this.s.appendNL('>');
                
                this.s.append("<input ")
	                .appendAttr("id", "_id_zXUL" + strControlName)
	                .appendAttr("name", "zXUL" + strControlName)
	                .appendAttr("value", StringEscapeUtils.escapeHtml(strValue))
	                .appendAttr("size", String.valueOf(intLength))
	                .appendAttr("maxlength", String.valueOf(pobjAttr.getLength()))
	                .appendAttr("type", pobjAttr.isPassword()?"password":"text")
	                .appendAttr("class", pstrClass)
	                .appendAttr("disabled", pblnDisabled?"true":"")
	                .appendAttr("tabIndex", pstrTabIndex)
	                .appendAttr("onChange", strOnChange.toString())
	                .appendAttr("onKeypress", strOnKeyPress)
	                .appendAttr("onBlur", pstrOnBlur)
	                .appendAttr("onFocus", pstrOnFocus)
	                .appendAttr("onClick", pstrOnClick)
	                .appendAttr("onKeyDown", pstrOnKeyDown)
	                .appendAttr("onKeyUp", pstrOnKeyUp)
	                .appendAttr("onMouseOver", pstrOnMouseOver)
	                .appendAttr("onMouseOut", pstrOnMouseOut)
	                .appendAttr("onMouseDown", pstrOnMouseDown)
	                .appendAttr("onMouseUp", pstrOnMouseUp)
	                .appendAttr("title", tooltip(pobjBO, pobjAttr))
	                .appendNL('>');
	                
                /**
                 * In case of a date field: add the calendar button
                 * DGS10MAR2003: but not if disabled
                 */
                if (intDataType == zXType.dataType.dtDate.pos 
                	||intDataType == zXType.dataType.dtTimestamp.pos
                    && !pblnDisabled) {
                	
                    buildDateButton(pobjBO, pobjAttr, "_id_zXUL" + strControlName, pblnDisabled);
                    
                }
                
                this.s.append("</span>").append(HTMLGen.NL);
            }
            
            /**
             * Handle any enhancer refs
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit)
                    || penmFormType.equals(zXType.webFormType.wftGrid)) {
                /**
                 * Add the spellcheck button if edit enhancer specifies it, but not if disabled:
                 */
                if(!pblnDisabled && pobjBO.getEditEnhancers(pobjAttr.getName()) != null) {
                    formSpellcheckButton(pobjBO, pobjAttr);
                }
                
                processFieldRef(penmFormType, pobjBO, pobjAttr);
            }
            
            /**
             * DGS28JUN2004: Handle an enhancer post label
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit) || penmFormType.equals(zXType.webFormType.wftGrid)) {
                if (StringUtil.len(pstrPostLabel) > 0) {
                    if (StringUtil.len(pstrPostLabelClass) == 0) {
                         pstrPostLabelClass = "zXFormLabel";
                    }
                    s.append("<span ").appendAttr("class", pstrPostLabelClass).appendNL('>');
                    s.append(pstrPostLabel).append(HTMLGen.NL);
                    s.append("</span>").append(HTMLGen.NL);
                }
            }
            
            if (!getEditEnhancerBoolean(pobjBO, pobjAttr, "mergeWithNext")) {
                s.append("</td>").append(HTMLGen.NL);
            }
            
            return formEntryNormalInputControl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for edit form field for normal input.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
                getZx().log.error("Parameter : pstrOnBlur = "+ pstrOnBlur);
                getZx().log.error("Parameter : pstrOnChange = "+ pstrOnChange);
                getZx().log.error("Parameter : pstrOnKeyPress = "+ pstrOnKeyPress);
                getZx().log.error("Parameter : pblnDisabled = "+ pblnDisabled);
                getZx().log.error("Parameter : pstrTabIndex = "+ pstrTabIndex);
                getZx().log.error("Parameter : pstrOnFocus = "+ pstrOnFocus);
                getZx().log.error("Parameter : pstrOnKeyDown = "+ pstrOnKeyDown);
                getZx().log.error("Parameter : pstrOnKeyUp = "+ pstrOnKeyUp);
                getZx().log.error("Parameter : pstrOnClick = "+ pstrOnClick);
                getZx().log.error("Parameter : pstrOnMouseOver = "+ pstrOnMouseOver);
                getZx().log.error("Parameter : pstrOnMouseOut = "+ pstrOnMouseOut);
                getZx().log.error("Parameter : pstrOnMouseDown = "+ pstrOnMouseDown);
                getZx().log.error("Parameter : pstrOnMouseUp = "+ pstrOnMouseUp);
            }
            if (getZx().throwException) throw new ZXException(e);
            formEntryNormalInputControl = zXType.rc.rcError;
            return formEntryNormalInputControl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntryNormalInputControl);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate tooltip message.
     *
     * @param pobjBO The business object. 
     * @param pobjAttr The attribute. 
     * @return Returns a string of the tooltip. 
     */
    public String tooltip(ZXBO pobjBO, Attribute pobjAttr) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
        }

        StringBuffer tooltip = new StringBuffer(50); 
        
        try {
            
            tooltip.append(pobjAttr.getLabel().getDescription());
            
            /**
             * Optional just do what the else statement does : 
             */
            int dataType = pobjAttr.getDataType().pos ;
            if (dataType == zXType.dataType.dtAutomatic.pos) {
                tooltip.append(" (Automatic,");
            } else if (dataType == zXType.dataType.dtBoolean.pos) {
                tooltip.append(" (Boolean, ");
            } else if (dataType == zXType.dataType.dtDate.pos) {
                tooltip.append(" (Date, length ").append(pobjAttr.getLength());
            } else if (dataType == zXType.dataType.dtDouble.pos) {
                tooltip.append(" (Double, length ").append(pobjAttr.getLength()).append(".").append(pobjAttr.getPrecision());
            } else if (dataType == zXType.dataType.dtExpression.pos) {
                tooltip.append(" (Expression, length ").append(pobjAttr.getLength());
            } else if (dataType == zXType.dataType.dtLong.pos) {
                tooltip.append(" (Long, length ").append(pobjAttr.getLength());
            } else if (dataType == zXType.dataType.dtString.pos) {
                tooltip.append(" (String, length ").append(pobjAttr.getLength());
            } else if (dataType == zXType.dataType.dtTime.pos) {
                tooltip.append(" (Time, length ").append(pobjAttr.getLength());
            } else if (dataType == zXType.dataType.dtTimestamp.pos) {
                tooltip.append(" (Timestamp, length ").append(pobjAttr.getLength());
            } else {
                // This could be used generically, except for Booleans and Doubles.
                tooltip.append(" (").append(zXType.valueOf(pobjAttr.getDataType())).append(", length ").append(pobjAttr.getLength());
            }
            
            if (pobjAttr.isOptional()) {
                tooltip.append(" optional)");
            } else {
                tooltip.append(" mandatory)");
            }
            
            if (pobjAttr.isCombobox()) {
                tooltip.append(" (combo-box, use arrowkeys to see available values)");
            }
            
            return tooltip.toString();
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(tooltip);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for start of order by entries.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with close
     * </pre>
     *
     * @param pobjBO The Business Object for the object by. 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if orderByOpen fails. 
     */
    public zXType.rc orderByOpen(ZXBO pobjBO) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
        }

        zXType.rc orderByOpen = zXType.rc.rcOK; 
        
        try {
            
            this.s.append("<br/>").append(HTMLGen.NL);
            this.s.append("<table width=\"100%\">").append(HTMLGen.NL);
            this.s.append("<tr>").append(HTMLGen.NL);
            
            this.s.append("<td ")
                    	.appendAttr("class", "zXFormLabel")
                    	.appendAttr("width", getWebSettings().getSearchFormColumn1())
                    	.appendNL('>');
            
            /**
             * BD6FEB05 - V1.4:32 See if we support an image to make this clearer
             */
            if (StringUtil.len(getWebSettings().getSearchOrderByImage()) > 0) {
                this.s.append("<img ")
                    .appendAttr("src", getWebSettings().getSearchOrderByImage())
                    .appendAttr("title", getZx().getMsg().getMsg("ZX/SEARCHFORM_ORDERBY_TOOLTIP"))
                    .append(">&nbsp;&nbsp;&nbsp;").append(HTMLGen.NL);
            }
            
            this.s.append(pobjBO.getDescriptor().getLabel().getLabel()).append(HTMLGen.NL);
            this.s.append("</td>").append(HTMLGen.NL);
                
            this.s.append("<td ")
                    	.appendAttr("class", "zXLabelPlain")
                    	.appendAttr("width", getWebSettings().getSearchFormColumn3())
                    	.appendNL('>');
            
            return orderByOpen;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for start of order by entries.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
            }
            if (getZx().throwException) throw new ZXException(e);
            orderByOpen = zXType.rc.rcError;
            return orderByOpen;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(orderByOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for end of order by entries.
     *
     * @return Returns the return code of the method. 
     */
    public zXType.rc orderByClose() {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc orderByClose = zXType.rc.rcOK;
        
        try {
            
            this.s.append("</td>").append(HTMLGen.NL);
            this.s.append("<td ")
                  .appendAttr("class", "zXLabelPlain")
                  .appendAttr("width", getWebSettings().getSearchFormColumn2())
                  .appendNL('>');
            this.s.append("</td>").append(HTMLGen.NL);
            this.s.append("</tr>").append(HTMLGen.NL);
            this.s.append("</table>").append(HTMLGen.NL);

            return orderByClose;
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(orderByClose);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for actual order by control.
     * 
     * Reviewed for 1.5:1.
     * 
     * @param pobjBO The business object of the order by list 
     * @param pstrGroup The attribute group used 
     * @return Returns the return code of the function.
     * @throws ZXException Thrown if orderBy fails. 
     */
    public zXType.rc orderBy(ZXBO pobjBO, String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        zXType.rc orderBy = zXType.rc.rcOK; 
        
        try {
            
            DSHandler objDSHandler = pobjBO.getDS();
            
            if (objDSHandler.getOrderSupport().pos == zXType.dsOrderSupport.dsosNone.pos) return orderBy;
            
            AttributeCollection colOrderByGroup = pobjBO.getDescriptor().getGroup(objDSHandler.getOrderGroup());
            if (colOrderByGroup == null) {
                return orderBy;
            }
            
            /**
             * Try to get group
             */
            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            if(colAttr == null || colAttr.size() == 0) {
                /**
                 * No problem if group not found and Ignore empty groups
                 */
                return orderBy;
            }
            
            String controlName = controlName(pobjBO);
            
            // Add a tooltip for the order by list :
            String strOnMouseOver = ""; // "tt_tooltip(this, event, 'Select " + pobjBO.getDescriptor().getLabel().getLabel() + " order by');";
            
            this.s.append("<select name=\"").append(controlName).append(pstrGroup).append("OrderBy\" onMouseOver=\"").append(strOnMouseOver).append("\">").append(HTMLGen.NL);
            this.s.append("<option value=\"-\">-</option>").append(HTMLGen.NL);
            
            Attribute objAttr;
            boolean blnFirstAttr = true;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if (colOrderByGroup.inGroup(objAttr.getName())) {
                    this.s.append("<option ").append(blnFirstAttr?"SELECTED":"").append(" value=\"").append(objAttr.getName()).append("\">").append(objAttr.getLabel().getLabel()).append("</option>").append(HTMLGen.NL);
                    blnFirstAttr = false;
                } // In supported order by group?
                
            } // Loop over atts
            
            this.s.append("</select>").append(HTMLGen.NL);
            
            if (objDSHandler.getOrderSupport().pos > zXType.dsOrderSupport.dsosSimple.pos) {
                this.s.append("<input name=\"").append(controlName)
                		.append(pstrGroup).append("Reverse\"  type=\"checkbox\" value=\"descending\">").append(HTMLGen.NL);
                
                this.s.append("Reverse order").append(HTMLGen.NL);
            } // Supports reverse?
            
            this.s.append("<br/>").append(HTMLGen.NL);
            
            return orderBy;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for actual order by control.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            return orderBy;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(orderBy);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for an edit form.
     *
     * @param pobjBO The business object for the edit form. 
     * @param pstrGroup The attribute group for the form. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if editForm fails. 
     */
    public zXType.rc editForm(ZXBO pobjBO, String pstrGroup) throws ZXException{
    	return editForm(pobjBO, pstrGroup, null, "*");
    }
    
    /**
     * Generate HTML for an edit form.
     *
     *<pre>
     *
     * Form entries will be marked as locked when : 
     * - it is in the lockGroup
     * - the attribute is an automatic
     * - the attribute is the primary key and has already been set
     * - the attribute is marked as locked
     * 
     *</pre>
     *
     * @param pobjBO The business object for the edit form. 
     * @param pstrGroup The attribute group for the form. 
     * @param pstrLockGroup The lock attibute group. Optional, default should be  null. 
     * @param pstrVisibleGroup The attribute group of visible controls. Optional, the default should be "*".
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if editForm fails. 
     */
    public zXType.rc editForm(ZXBO pobjBO, String pstrGroup, String pstrLockGroup, String pstrVisibleGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrLockGroup", pstrLockGroup);
            getZx().trace.traceParam("pstrVisibleGroup", pstrVisibleGroup);
        }

        zXType.rc editForm = zXType.rc.rcOK; 
        
        /**
         * Handle defaults
         */
        if (pstrVisibleGroup == null) pstrVisibleGroup = "*";
        
        try {
            
            /**
             * Do a pre-persist to allow user to do some special handling
             */
            if (pobjBO.prePersist(zXType.persistAction.paEditForm, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of pre-persist");
            }
            
            /**
             * START OF SETUP
             */
            /**
             * If the visible group is not given, assume that all attributes should be visible
             */
            if (StringUtil.len(pstrVisibleGroup) == 0) {
                pstrVisibleGroup = pstrGroup;
            }
            
            /**
             * Get handle to the attribute groups
             * If auditable, add the audit columns to the group
             * 
             * BD9JUN04 - Now concurrency control
             */
            AttributeCollection colAttr;
            if (pobjBO.getDescriptor().isConcurrencyControl()) {
                colAttr = pobjBO.getDescriptor().getGroup(pstrGroup + ",~");
            } else {
                colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            }
            
            AttributeCollection colLockAttr = pobjBO.getDescriptor().getGroup(pstrLockGroup);
            AttributeCollection colVisibleAttr = pobjBO.getDescriptor().getGroup(pstrVisibleGroup);
            
            if (colAttr == null || colLockAttr == null || colVisibleAttr == null) {
                throw new Exception("Unable to retrieve attr / lock or visible group");
            }
            
            /**
             * BD28FEB05 - V1.4:53 - Use sequence of visible attribute group rather
             * than sequence of edit group if applicable
             */
            if (StringUtil.len(pstrVisibleGroup) > 0 && !pstrGroup.equalsIgnoreCase(pstrVisibleGroup)) {
                colAttr = forceEditGroupSequence(colAttr, colVisibleAttr);
                if (colAttr == null) {
                    editForm = zXType.rc.rcError;
                    return editForm;
                }
            } // Need to force sequence of edit group
            
            /**
             * END OF SETUP
             */
            
            this.s.append("<table width=\"100%\">").append(HTMLGen.NL);
            
            Attribute objAttr;
            boolean blnLocked = false;
            boolean blnVisible = true;
            
            boolean blnIsPersistStatusNew = pobjBO.getPersistStatus().equals(zXType.persistStatus.psNew);
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                /**
                 * Figure out whether this attribute is locked. This can be because
                 *  - it is in the lockGroup
                 *  - the attribute is an automatic
                 *  - the attribute is the primary key and has already been set
                 *  - the attribute is marked as locked
                 */
                if (colLockAttr.inGroup(objAttr.getName())) {
                    blnLocked = true;
                    
                } else if (objAttr.getName().equalsIgnoreCase(pobjBO.getDescriptor().getPrimaryKey()) 
                           && !blnIsPersistStatusNew) {
                    // Locked if the primary key.
                    blnLocked = true;
                    
                } else if (objAttr.getDataType().pos == zXType.dataType.dtAutomatic.pos) {
                    blnLocked = true;
                    
                } else {
                    blnLocked = objAttr.isLocked();
                    
                }
                
                /**
                 * Figure out whether this attribute is visible
                 */
                if (!colVisibleAttr.inGroup(objAttr.getName())) {
                    blnVisible = false;
                } else {
                    blnVisible = true;
                }
                
                /**
                 * Now we have enough information to generate an entry
                 */
                formEntry(zXType.webFormType.wftEdit, pobjBO, objAttr, blnLocked, blnVisible);
            }
            
            this.s.append("</table>").append(HTMLGen.NL);
            
            /**
             * Do a post-persist to allow user to do some special handling
             */
            if (pobjBO.postPersist(zXType.persistAction.paEditForm, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of post-persist");
            }
            
            return editForm;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for an edit form.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
                getZx().log.error("Parameter : pstrLockGroup = "+ pstrLockGroup);
                getZx().log.error("Parameter : pstrVisibleGroup = "+ pstrVisibleGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            editForm = zXType.rc.rcError;
            return editForm;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(editForm);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for an edit form.
     *
     * @param pintRow The row number. 
     * @param pobjBO The business object associate with the grid edit. 
     * @param pstrGroup The attribute group of the grid edit. 
     * @param pstrLockGroup The locked attribute group. Optional, default is null. 
     * @param pstrVisibleGroup The visible attribute group. Optional,  default is  "" 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if gridEditForm fails. 
     */
    public zXType.rc gridEditForm(int pintRow, 
    							  ZXBO pobjBO, 
    							  String pstrGroup, 
    							  String pstrLockGroup, 
    							  String pstrVisibleGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pintRow", pintRow);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrLockGroup", pstrLockGroup);
            getZx().trace.traceParam("pstrVisibleGroup", pstrVisibleGroup);
        }
        
        zXType.rc gridEditForm = zXType.rc.rcOK;
        
        try {
            /**
             * Do a pre-persist to allow user to do some special handling
             */
            if (pobjBO.prePersist(zXType.persistAction.paEditForm, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of pre-persist");
            }
            
            /**
             * START OF SETUP
             */
            /**
             * If the visible group is not given, assume that all attributes should be visible
             */
            if (StringUtil.len(pstrVisibleGroup) == 0) {
                pstrVisibleGroup = pstrGroup;
            }
            
            /**
             * Get handle to the attribute groups
             * If auditable, add the audit columns to the group
             * 
             * BD9JUN04 - Now concurrency control
             */
            AttributeCollection colAttr;
            if (pobjBO.getDescriptor().isConcurrencyControl()) {
                colAttr = pobjBO.getDescriptor().getGroup(pstrGroup + ",~");
            } else {
                colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            }
            
            AttributeCollection colLockAttr = pobjBO.getDescriptor().getGroup(pstrLockGroup);
            AttributeCollection colVisibleAttr = pobjBO.getDescriptor().getGroup(pstrVisibleGroup);
            
            if (colAttr == null || colLockAttr == null || colVisibleAttr == null) {
                throw new Exception("Unable to retrieve attr / lock or visible group");
            }
            
            /**
             * BD28FEB05 - V1.4:53 - Use sequence of visible attribute group rather
             * than sequence of edit group if applicable
             */
            if (StringUtil.len(pstrVisibleGroup) > 0 && !pstrGroup.equalsIgnoreCase(pstrVisibleGroup)) {
                colAttr = forceEditGroupSequence(colAttr, colVisibleAttr);
                if (colAttr == null) {
                    gridEditForm = zXType.rc.rcError;
                    return gridEditForm;
                }
            } // Need to force sequence of edit group
            
            
            boolean blnMayUpdate = true;
            if (pobjBO.getPersistStatus().equals(zXType.persistStatus.psNew)) {
                blnMayUpdate = pobjBO.mayUpdate();
            }
            /**
             * END OF SETUP
             */
            
            Attribute objAttr;
            boolean blnLocked = false;
            boolean blnVisible = true;
            String strOnChange = "";
            
            boolean blnIsPersistStatusNew = pobjBO.getPersistStatus().equals(zXType.persistStatus.psNew);
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                /**
                 * Figure out whether this attribute is locked. This can be because
                 *  - it is in the lockGroup
                 *  - the attribute is an automatic
                 *  - the attribute is the primary key and has already been set
                 *  - the attribute is marked as locked
                 */
                if (colLockAttr.inGroup(objAttr.getName())) {
                    blnLocked = true;
                    
                } else if (objAttr.getName().equalsIgnoreCase(pobjBO.getDescriptor().getPrimaryKey()) 
                           && !blnIsPersistStatusNew) {
                    blnLocked = true;
                    
                } else if (objAttr.getDataType().pos == zXType.dataType.dtAutomatic.pos) {
                    blnLocked = true;
                    
                } else {
                    blnLocked = objAttr.isLocked();
                    
                }
                
                if (!blnMayUpdate) {
                    blnLocked = true;
                }
                
                /**
                 * Figure out whether this attribute is visible
                 */
                if (!colVisibleAttr.inGroup(objAttr.getName())) {
                    blnVisible = false;
                }
                
                if (!blnLocked && blnVisible) {
                    strOnChange = new StringBuffer("zXPersist")
                    			 .append(StringEscapeUtils.escapeHTMLTag(this.pageflow.getContextAction().getName()))
                    			 .append(pintRow).append(".selectedIndex = 1;").toString();
                }
                
                /**
                 * Now we have enough information to generate an entry
                 */
                gridFormEntry(pobjBO, objAttr, blnLocked, blnVisible, null, strOnChange, "");
            }
            
            /**
             * Do a post-persist to allow user to do some special handling
             */
            if (pobjBO.postPersist(zXType.persistAction.paEditForm, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of post-persist");
            }
            
            return gridEditForm;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for an edit form.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pintRow = "+ pintRow);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
                getZx().log.error("Parameter : pstrLockGroup = "+ pstrLockGroup);
                getZx().log.error("Parameter : pstrVisibleGroup = "+ pstrVisibleGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            gridEditForm = zXType.rc.rcError;
            return gridEditForm;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(gridEditForm);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for an edit form entry.
     *
     * @param pobjBO The business object of the grid edit form entry 
     * @param pobjAttr The form element attribute. 
     * @param pblnLocked Whether the form entry is locked or not. 
     * @param pblnVisible Whether the form entry is visible or not. 
     * @param pstrOnBlur The onBlur action. 
     * @param pstrOnChange The onChange action 
     * @param pstrOnKeyPress The onKeyPress action. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if gridFormEntry fails. 
     */
    public zXType.rc gridFormEntry(ZXBO pobjBO, 
    							   Attribute pobjAttr, 
    							   boolean pblnLocked, 
    							   boolean pblnVisible, 
    							   String pstrOnBlur, 
    							   String pstrOnChange, 
    							   String pstrOnKeyPress) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pblnLocked", pblnLocked);
            getZx().trace.traceParam("pblnVisible", pblnVisible);
            getZx().trace.traceParam("pstrOnBlur", pstrOnBlur);
            getZx().trace.traceParam("pstrOnChange", pstrOnChange);
            getZx().trace.traceParam("pstrOnKeyPress", pstrOnKeyPress);
        }

        zXType.rc gridFormEntry = zXType.rc.rcOK; 
        
        try {
            /**
             * Generate the actual input box
             */
            entryInputControl(zXType.webFormType.wftGrid, pobjBO, pobjAttr, pblnLocked, pblnVisible, pstrOnBlur, pstrOnChange, pstrOnKeyPress);
            
            return gridFormEntry;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for an edit form entry.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pblnLocked = "+ pblnLocked);
                getZx().log.error("Parameter : pblnVisible = "+ pblnVisible);
                getZx().log.error("Parameter : pstrOnBlur = "+ pstrOnBlur);
                getZx().log.error("Parameter : pstrOnChange = "+ pstrOnChange);
                getZx().log.error("Parameter : pstrOnKeyPress = "+ pstrOnKeyPress);
            }
            if (getZx().throwException) throw new ZXException(e);
            gridFormEntry = zXType.rc.rcError;
            return gridFormEntry;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(gridFormEntry);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for a multiline text box.
     *
     * @param penmFormType The type of form the control in on. 
     * @param pobjBO The business object the control belongs to. 
     * @param pobjAttr The linked attributes of the control. 
     * @param pstrClass The stylesheet class of the attribute. 
     * @param pstrOnBlur The onBlur action. 
     * @param pstrOnChange The onChange action. 
     * @param pstrOnKeyPress The onKeyPress action. 
     * @param pblnDisabled Whether the control is disabled or not. 
     * @param pstrTabIndex Which tab does it belong to 
     * @param pstrOnFocus The onFocus action. 
     * @param pstrOnKeyDown The onKeyDown action. 
     * @param pstrOnKeyUp The onKeyUp action. 
     * @param pstrOnClick The onClick action. 
     * @param pstrOnMouseOver The onMouseOver action 
     * @param pstrOnMouseOut The onMouseOut action. 
     * @param pstrOnMouseDown The onMouseDown action. 
     * @param pstrOnMouseUp The onMouseUp action. 
     * @param pstrPostLabel Post Label.
     * @param pstrPostLabelClass Post Label class. 
     * @return Returns the return of the methods.
     * @throws ZXException Thrown if formEntryMultilineControl fails. 
     */
    public zXType.rc formEntryMultilineControl(zXType.webFormType penmFormType, 
                                          ZXBO pobjBO, 
                                          Attribute pobjAttr,
                                          String pstrClass, 
                                          String pstrOnBlur, 
                                          String pstrOnChange, 
                                          String pstrOnKeyPress, 
                                          boolean pblnDisabled, 
                                          String pstrTabIndex, 
                                          String pstrOnFocus, 
                                          String pstrOnKeyDown, 
                                          String pstrOnKeyUp, 
                                          String pstrOnClick, 
                                          String pstrOnMouseOver, 
                                          String pstrOnMouseOut, 
                                          String pstrOnMouseDown, 
                                          String pstrOnMouseUp,
                                          String pstrPostLabel,
                                          String pstrPostLabelClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pstrClass", pstrClass);
            getZx().trace.traceParam("pstrOnBlur", pstrOnBlur);
            getZx().trace.traceParam("pstrOnChange", pstrOnChange);
            getZx().trace.traceParam("pstrOnKeyPress", pstrOnKeyPress);
            getZx().trace.traceParam("pblnDisabled", pblnDisabled);
            getZx().trace.traceParam("pstrTabIndex", pstrTabIndex);
            getZx().trace.traceParam("pstrOnFocus", pstrOnFocus);
            getZx().trace.traceParam("pstrOnKeyDown", pstrOnKeyDown);
            getZx().trace.traceParam("pstrOnKeyUp", pstrOnKeyUp);
            getZx().trace.traceParam("pstrOnClick", pstrOnClick);
            getZx().trace.traceParam("pstrOnMouseOver", pstrOnMouseOver);
            getZx().trace.traceParam("pstrOnMouseOut", pstrOnMouseOut);
            getZx().trace.traceParam("pstrOnMouseDown", pstrOnMouseDown);
            getZx().trace.traceParam("pstrOnMouseUp", pstrOnMouseUp);
            getZx().trace.traceParam("pstrPostLabel", pstrPostLabel);
            getZx().trace.traceParam("pstrPostLabelClass", pstrPostLabelClass);
        }
        
        zXType.rc formEntryMultilineControl = zXType.rc.rcOK;

        try {
            
           /**
            * Add the standard header part of the form entry.
            */
            formEntryHeader(pobjBO, pobjAttr, penmFormType);
           
            /**
             *  BD1MAR05 - 1.4:54 Added support for control over rows and
             *  cols for multi-line fields
             */
            String strMultiLineRows = getZx().resolveDirector(getEditEnhancerProperty(pobjBO, pobjAttr, "multiLineRows"));
            String strMultiLineCols = getZx().resolveDirector(getEditEnhancerProperty(pobjBO, pobjAttr, "multiLineCols"));
            
            /**
             * DGS19FEB2004: Keep the number of lines reasonable for the length of field. This will round.
             */
            int intMultiLineRows;
            if (StringUtil.isNumeric(strMultiLineRows)) {
                intMultiLineRows = Integer.parseInt(strMultiLineRows);
            } else {
                intMultiLineRows = pobjAttr.getLength() / pobjAttr.getOutputLength();
                if (intMultiLineRows > getWebSettings().getMultilineRows()) {
                    intMultiLineRows = getWebSettings().getMultilineRows();
                }
            }
            
            /**
             * DGS15MAR2004: If a tag exists to set the multiline cols, use that value (will already be
             *  set in the HTML property). Otherwise use attr output length as was previously the case.
             */
            int intMultiLineCols; 
            if (StringUtil.isNumeric(strMultiLineCols)) {
                intMultiLineCols = Integer.parseInt(strMultiLineCols);
            } else {
                intMultiLineCols = pobjAttr.getOutputLength();
                if (this.pageflow.getContextAction()!= null) {
                    if (this.pageflow.getContextAction().hasTag("zXMultiLineCols")) {
                        intMultiLineCols = getWebSettings().getMultilineCols();
                    }
                }
            }
            
            /**
             * DGS01APR2004: (Test issue no. TBA.) Use new javascript function to ensure cannot enter more than max length
             */
            pstrOnKeyUp = pstrOnKeyUp + "zXMultilineOnKeyUp(this," + pobjAttr.getLength() + ");";
            
            String strControlName = controlName(pobjBO, pobjAttr);
            s.append("<textarea name=\"").append(strControlName).append("\" ") 
		     .appendAttr("id", "_id_"+ strControlName)
		     .appendAttr("cols", ""+ intMultiLineCols)
		     .appendAttr("rows", ""+ intMultiLineRows)
		     .appendAttr("class", pstrClass)
		     .appendAttr("disabled", (pblnDisabled?"true":"")) 
		     .appendAttr("tabIndex", pstrTabIndex)
		     .appendAttr("onChange", pstrOnChange)
		     .appendAttr("onKeypress", pstrOnKeyPress)
		     .appendAttr("onBlur", pstrOnBlur)
		     .appendAttr("onFocus", pstrOnFocus) 
		     .appendAttr("onClick", pstrOnClick)
		     .appendAttr("onKeyDown", pstrOnKeyDown) 
		     .appendAttr("onKeyUp", pstrOnKeyUp)
		     .appendAttr("onMouseOver", pstrOnMouseOver) 
		     .appendAttr("onMouseOut", pstrOnMouseOut)
		     .appendAttr("onMouseDown", pstrOnMouseDown)
		     .appendAttr("onMouseUp", pstrOnMouseUp)
		     .appendAttr( "title", tooltip(pobjBO, pobjAttr))
		     .appendNL('>');
            
            s.append(pobjBO.getValue(pobjAttr.getName()).formattedValue(true));
            
            s.append("</textarea>").append(HTMLGen.NL);
            
            if (!pblnDisabled) {
            	/**
            	 * Add the spellcheck button if required.
            	 */
            	if (pobjBO.getEditEnhancers(pobjAttr.getName()) != null) {
            		formSpellcheckButton(pobjBO, pobjAttr);
            	}
            	
                /**
                 * Show the expression builder button.
                 */
                if (pobjAttr.getDataType().pos == zXType.dataType.dtExpression.pos) {
                	if (formExprEditButton(pobjBO, pobjAttr).pos  != zXType.rc.rcOK.pos) {
                		formEntryMultilineControl = zXType.rc.rcError;
                		return formEntryMultilineControl;
                	}
                }
            }
            
		    /**
		     * Handle any enhancer refs
		     **/
		    processFieldRef(penmFormType, pobjBO, pobjAttr);
            
            /**
             * DGS28JUN2004: Handle an enhancer post label
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit) 
               || penmFormType.equals(zXType.webFormType.wftGrid)) {
                if (StringUtil.len(pstrPostLabel) > 0) {
                    if (StringUtil.len(pstrPostLabelClass) == 0) {
                        pstrPostLabelClass = "zXFormLabel";
                    }
                    s.append("<span ").appendAttr("class", pstrPostLabelClass).appendNL('>');
                    s.append(pstrPostLabel).append(HTMLGen.NL);
                    s.append("</span>").append(HTMLGen.NL);
                }
            }

            /**
             * DGS28JAN2004: If in a search form we need a span here, although there's never
             * anything in it for a multiline. Otherwise the onChange JS doesn't work.
             */
            if (penmFormType.equals(zXType.webFormType.wftSearch)) {
                s.append("<span ");
                s.appendAttr("id", "zXSpan" + strControlName);
                s.appendAttr("style", "display:none");
                s.append("/>").append(HTMLGen.NL);
            }
            
            if (!getEditEnhancerBoolean(pobjBO, pobjAttr, "mergeWithNext")) {
                this.s.append("</td>");
            }
            
            return formEntryMultilineControl;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for a multiline text box.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
                getZx().log.error("Parameter : pstrOnBlur = "+ pstrOnBlur);
                getZx().log.error("Parameter : pstrOnChange = "+ pstrOnChange);
                getZx().log.error("Parameter : pstrOnKeyPress = "+ pstrOnKeyPress);
                getZx().log.error("Parameter : pblnDisabled = "+ pblnDisabled);
                getZx().log.error("Parameter : pstrTabIndex = "+ pstrTabIndex);
                getZx().log.error("Parameter : pstrOnFocus = "+ pstrOnFocus);
                getZx().log.error("Parameter : pstrOnKeyDown = "+ pstrOnKeyDown);
                getZx().log.error("Parameter : pstrOnKeyUp = "+ pstrOnKeyUp);
                getZx().log.error("Parameter : pstrOnClick = "+ pstrOnClick);
                getZx().log.error("Parameter : pstrOnMouseOver = "+ pstrOnMouseOver);
                getZx().log.error("Parameter : pstrOnMouseOut = "+ pstrOnMouseOut);
                getZx().log.error("Parameter : pstrOnMouseDown = "+ pstrOnMouseDown);
                getZx().log.error("Parameter : pstrOnMouseUp = "+ pstrOnMouseUp);
                getZx().log.error("Parameter : pstrPostLabel = "+ pstrPostLabel);
                getZx().log.error("Parameter : pstrPostLabelClass = "+ pstrPostLabelClass);
            }
            if (getZx().throwException) throw new ZXException(e);
            
            formEntryMultilineControl = zXType.rc.rcError;
            return formEntryMultilineControl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Process refs that are associated with the enhancers of an Attribute.
     *
     * @param penmFormType The type of web form. 
     * @param pobjBO The business object associate with the element. 
     * @param pobjAttr The attribute the enhancers belong to. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if processFieldRef fails. 
     */
    public zXType.rc processFieldRef(zXType.webFormType penmFormType, 
    								 ZXBO pobjBO, 
    								 Attribute pobjAttr) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
        }
        
        zXType.rc processFieldRef = zXType.rc.rcOK; 
        
        try {
            
            /**
             * Handle any enhancer.refs; only apply to edit forms and only
             * to visible fields
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit) || penmFormType.equals(zXType.webFormType.wftGrid)) {
                
                PFRef objRef;
                List colRefs;
                
                List colEditEnhancers = pobjBO.getEditEnhancers(pobjAttr.getName());
                if (colEditEnhancers == null) return processFieldRef;
                
                int intEditEnhancers = colEditEnhancers.size();
                for (int i = 0; i < intEditEnhancers; i++) {
                    colRefs = ((PFEditEnhancer)colEditEnhancers.get(i)).getRefs();
                    
                    if (colRefs != null) {
	                    int intRefs = colRefs.size();
	                    for (int j = 0; j < intRefs; j++) {
	                        objRef = (PFRef)colRefs.get(j);
	                        
	                        /**
	                         * Note that we always assume pageflow to be set when we use enhancers!!!
	                         */
	                        this.pageflow.processRefByFormType(objRef, penmFormType);
	                    } // Loop over refs
                    } // Null check.
                    
                } // Loop over enhancers
                
            }
            
            return processFieldRef;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process refs that are associated with enhancer.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
            }
            if (getZx().throwException) throw new ZXException(e);
            processFieldRef = zXType.rc.rcError;
            return processFieldRef;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processFieldRef);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for a Hidden control.
     *
     * <pre>
     *
     *  NOTE : This method only really needs a handle to the business property of the attribute.
     * </pre>
     *
     * @param penmFormType  The type of web form. This is not used at the moment.
     * @param pobjBO The business object the form entry belongs to.
     * @param pobjAttr The attribute of the form entry.
     * @param pstrClass The css class name. This is not used at the moment. 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if formEntryHiddenControl fails. 
     */
    public zXType.rc formEntryHiddenControl(zXType.webFormType penmFormType, 
                                            ZXBO pobjBO, 
                                            Attribute pobjAttr, 
                                            String pstrClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pstrClass", pstrClass);
        }

        zXType.rc formEntryHiddenControl = zXType.rc.rcOK; 
        
        try {
            
            /**
             * Get the true value. This is going to be put in the hidden control
             * BD14JAN02 and escape it to be sure (in case of quotes and all that)
             */
            String strValue = StringEscapeUtils.escapeHtml( pobjBO.getValue(pobjAttr.getName()).getStringValue() );
            
            s.append("<div style=\"display:none\">").append(HTMLGen.NL);
            
            s.append("<input type=\"hidden\" ")
             .appendAttr("name", controlName(pobjBO, pobjAttr))
             .appendAttr("value", strValue)
             .appendNL('>');
            
            s.append("</div>").append(HTMLGen.NL);
            
            return formEntryHiddenControl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for a Hidden control.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
            }
            if (getZx().throwException) throw new ZXException(e);
            formEntryHiddenControl = zXType.rc.rcError;
            return formEntryHiddenControl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntryHiddenControl);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * The standard footer and header of a form entry control.
     * 
     * @param pobjBO The business object of the attribute.
     * @param pobjAttr The attribute to display.
     * @param penmFormType The webform type.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formEntryHeader fails. 
     */
    public boolean formEntryHeader(ZXBO pobjBO, 
    							   Attribute pobjAttr, 
    							   zXType.webFormType penmFormType) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
        }
        
        boolean formEntryHeader = false; 
        try {

            if (penmFormType.equals(zXType.webFormType.wftEdit)) { 
                if (!getEditEnhancerBoolean(pobjBO, pobjAttr, "mergeWithPrevious")) {
                    this.s.append("<td ").appendAttr("width", getWebSettings().getEditFormColumn2()).appendNL('>');
                    
                } else {
                    /**
                     * Keep track fo fact that we have merged as
                     * it has some serious implications for attributes
                     */
                    formEntryHeader = true;
                }
            } else if (penmFormType.equals(zXType.webFormType.wftGrid)) {
                this.s.append("<td NOWRAP>").append(HTMLGen.NL);
            } else {
                this.s.append("<td ").appendAttr("width", getWebSettings().getSearchFormColumn3()).appendNL('>');
            }
            
            return formEntryHeader;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : The standard footer and header of a form entry control.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return formEntryHeader;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntryHeader);
                getZx().trace.exitMethod();
            }
        }
        
    }
    
    /**
     * Generate HTML for a locked control.
     * 
     * Reviewed for 1.5:20 - Enhanced FK label behaviour.
     * 
     * @param penmFormType The type of web form 
     * @param pobjBO The business object the form entry belongs to. 
     * @param pobjAttr The attribute of the form entry. 
     * @param pstrClass The css class name. 
     * @param pstrOnBlur The onBlur action. Optional, default should be null. 
     * @param pstrPostLabel The post label message. 
     * @param pstrPostLabelClass The post label css class
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if formEntryLockedControl fails. 
     */
    public zXType.rc formEntryLockedControl(zXType.webFormType penmFormType, 
                                            ZXBO pobjBO, 
                                            Attribute pobjAttr, String pstrClass, String pstrOnBlur,
                                            String pstrPostLabel, String pstrPostLabelClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pstrClass", pstrClass);
            getZx().trace.traceParam("pstrOnBlur", pstrOnBlur);
            getZx().trace.traceParam("pstrPostLabel", pstrPostLabel);
            getZx().trace.traceParam("pstrPostLabelClass", pstrPostLabelClass);
        }

        zXType.rc formEntryLockedControl = zXType.rc.rcOK;
        
        try {
            
            /**
             * Do the standard header part of the form entry :
             */
            formEntryHeader(pobjBO, pobjAttr, penmFormType);
            
            /**
             * Get the true value. This is going to be put in a hidden control
             * so when the form is submitted, the server script can simply
             * treat this as an ordinary box
             * BD14JAN02 and escape any quotes
             */
            String strValue = StringEscapeUtils.escapeHtml(pobjBO.getValue(pobjAttr.getName()).getStringValue());
            
            /**
             * And the value that we want to show. This has some variations:
             *  - For FK: retrieve the label of the FK object
             *  - For password: simply return 5 times '*'
             *  - Other: return formatted string
             */
            String strShowValue = "";
            boolean blnNormalField = false;
            ZXBO objFKBO= null;
            if (StringUtil.len(pobjAttr.getForeignKey()) > 0) {
            	Property objProp;
            	
            	objProp = pobjBO.getValue(pobjAttr.getName());
                if (!objProp.isNull) {
                	if (objProp.resolveFKLabel(false).pos != zXType.rc.rcOK.pos) {
                		throw new ZXException("Unable to resolve FK label for attribute", pobjAttr.getName());
                	}
                	
                	objFKBO = objProp.getFKBO();
                	
                	strShowValue = objProp.getFkLabel();
                } // FK Attr not null?
                
            } else if (pobjAttr.isPassword()) {
                strShowValue = "*****";
                
            } else {
                strShowValue = pobjBO.getValue(pobjAttr.getName()).formattedValue();
                blnNormalField = true;
                
            }
            
            /**
             * And create locked textbox for it (or text area in case of multi-line
             *  or a checkbox in case of a boolean)
             */
            if (StringUtil.len(strShowValue) == 0) {
                strShowValue = "-";
            } else {
                /**
                 * BD6DEC02 Replace any vbcrlf with <br> for proper display
                 */
                strShowValue = StringUtil.replaceAll(strShowValue, '\n', "<br/>");
            }
            
            /**
             * Get the controlName once and cache if for the whole method :
             */
            String strControlName = controlName(pobjBO, pobjAttr);
            
            /**
             * Use given class, not hard-coded (allows enhancers to influence).
             */
            this.s.append("<span ")
            	  .appendAttr("class", pstrClass)
            	  .appendAttr("id", "div" + strControlName)
            	  .appendNL('>');
            
            this.s.append(strShowValue).append(HTMLGen.NL);
            
            /**
             * Can show a clickable image next to FK lists to view the details.
             */
            if (objFKBO != null) {
                boolean isSmall = objFKBO.getDescriptor().getSize().equals(zXType.entitySize.esSmall);
                boolean fklookupExists = getEditEnhancerBoolean(pobjBO, pobjAttr, "fklookup");
                
                if ( (isSmall && fklookupExists) ||  (!isSmall && !fklookupExists)) {
                    // Build up the Popup javascript.
                    StringBuffer strJS = new StringBuffer(150);
                    strJS.append("zXFKPopup(");
                    
                    if (isSmall) {
                        strJS.append("'s'");
                    } else if (objFKBO.getDescriptor().getSize().equals(zXType.entitySize.esMedium)) {
                        strJS.append("'m'");
                    } else {
                        strJS.append("'l'");
                    }
                    
                    strJS.append(",'").append(getZx().getSession().getSessionid());
                    strJS.append("',1,'").append(strControlName);
                    
                    strJS.append("','").append(objFKBO.getDescriptor().getName());
                    strJS.append("','").append(pobjAttr.getName());

                    strJS.append("','").append(objFKBO.getDescriptor().getName());
                    strJS.append("','','','").append(StringEscapeUtils.escapeJavaScript(pobjBO.getValue(pobjAttr.getName()).getStringValue())).append("');");
                    
                    s.append("<img ")
                     .appendAttr("src", "../images/listItem.gif")
                     .appendAttr("alt", "Show details " + pobjAttr.getLabel().getDescription())
                     .appendAttr("title", "Show details " + pobjAttr.getLabel().getDescription())
                     .appendAttr("onMouseDown", "this.style.cursor='wait';window.document.body.style.cursor='wait';javascript:" + strJS.toString() + ";this.style.cursor='';window.document.body.style.cursor=''")
                     .appendAttr("onMouseOver", "javascript:this.src='../images/listItemOver.gif'")
                     .appendAttr("onMouseOut", "javascript:this.src='../images/listItem.gif'")
                     .appendNL('>');
                }
            }
            
            this.s.append("</span>").append(HTMLGen.NL);
            
            /**
             * And generate enhancer refs
             */
            processFieldRef(penmFormType, pobjBO, pobjAttr);
            
            /**
             * BD19OCT04: Handle an enhancer post label
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit)
                || penmFormType.equals(zXType.webFormType.wftGrid)) {
                
                if (StringUtil.len(pstrPostLabel) > 0) {
                    if (StringUtil.len(pstrPostLabelClass) == 0) {
                        pstrPostLabelClass = "zXFormLabel";
                    }
                    
                    s.append("<span ").appendAttr("class", pstrPostLabelClass).appendNL('>');
                    s.append(pstrPostLabel);
                    s.append("</span>").append(HTMLGen.NL);
                }
            }
            
            /**
             * And now a hidden field with the proper control name and
             *  the actual value
             *  In case of a 'normal' field (i.e. where the displayed value
             *  is the same as the value in the hidden field) we also have a
             *  onChange handler
             */
            if (blnNormalField) {
                s.append("<input type=\"hidden\" ")
                 .appendAttr("name", strControlName)
                 .appendAttr("onChange", "zXUpdateDivForLockedField(this);")
                 .appendAttr("value", strValue)
                 .appendNL('>');
            } else {
                s.append("<input type=\"hidden\" ")
                 .appendAttr("name", strControlName)
                 .appendAttr("value", strValue)
                 .appendNL('>');
            }
            
            if (!getEditEnhancerBoolean(pobjBO, pobjAttr, "mergeWithNext")) {
                s.append("</td>").append(HTMLGen.NL);
            }
            
            return formEntryLockedControl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for a locked control.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
                getZx().log.error("Parameter : pstrOnBlur = "+ pstrOnBlur);
                getZx().log.error("Parameter : pstrPostLabel = "+ pstrPostLabel);
                getZx().log.error("Parameter : pstrPostLabelClass = "+ pstrPostLabelClass);
            }
            if (getZx().throwException) throw new ZXException(e);
            formEntryLockedControl = zXType.rc.rcError;
            return formEntryLockedControl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntryLockedControl);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for a form entry that has an FK.
     * 
     * <pre>
     * 
     * DGS10MAR2003: Revised for edit form enhancers.
     * DGS07APR2003: Revised further for edit form enhancers - can now restrict a FK list
     * according to the value of one of its attributes. This can be combined with binding to another field.
     * 
     * Reviewed for 1.5:1
     * Reviewed for 1.5:20 - Enhanced FK label behaviour
     * Reviewed for 1.5:61 - Fixed problem with FK attributes on search form that 
     * 						 have a value
     * </pre>
     *
     * @param penmFormType The web form type 
     * @param pobjBO The business object of the form entry 
     * @param pobjAttr The attribute the form entry belongs to. 
     * @param pstrClass The css class name. 
     * @param pstrOnBlur The onBlur javascript action. 
     * @param pstrOnChange The onChange javascript action. 
     * @param pstrOnKeyPress The onKeyPress action. 
     * @param pblnDisabled Whether the form entry is disabled or not. 
     * @param pstrTabIndex The tab index of the form. 
     * @param pstrOnFocus The onFocus javascript action. 
     * @param pstrOnKeyDown The onKeyDown javascript action 
     * @param pstrOnKeyUp The onKeyUp javascript action. 
     * @param pstrOnClick The onClick action. 
     * @param pstrOnMouseOver The onMouseOver action 
     * @param pstrOnMouseOut The onMouseOut action 
     * @param pstrOnMouseDown The onMouseDown action 
     * @param pstrOnMouseUp The onMouseUp action 
     * @param pstrPostLabel Post Label
     * @param pstrPostLabelClass Post Lable class. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formEntryFKControl fails. 
     */
    public zXType.rc formEntryFKControl(zXType.webFormType penmFormType, 
                                        ZXBO pobjBO, 
                                        Attribute pobjAttr, 
                                        String pstrClass, 
                                        String pstrOnBlur, 
                                        String pstrOnChange, 
                                        String pstrOnKeyPress, 
                                        boolean pblnDisabled, 
                                        String pstrTabIndex, 
                                        String pstrOnFocus, 
                                        String pstrOnKeyDown, 
                                        String pstrOnKeyUp, 
                                        String pstrOnClick, 
                                        String pstrOnMouseOver, 
                                        String pstrOnMouseOut, 
                                        String pstrOnMouseDown, 
                                        String pstrOnMouseUp,
                                        String pstrPostLabel,
                                        String pstrPostLabelClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pstrClass", pstrClass);
            getZx().trace.traceParam("pstrOnBlur", pstrOnBlur);
            getZx().trace.traceParam("pstrOnChange", pstrOnChange);
            getZx().trace.traceParam("pstrOnKeyPress", pstrOnKeyPress);
            getZx().trace.traceParam("pblnDisabled", pblnDisabled);
            getZx().trace.traceParam("pstrTabIndex", pstrTabIndex);
            getZx().trace.traceParam("pstrOnFocus", pstrOnFocus);
            getZx().trace.traceParam("pstrOnKeyDown", pstrOnKeyDown);
            getZx().trace.traceParam("pstrOnKeyUp", pstrOnKeyUp);
            getZx().trace.traceParam("pstrOnClick", pstrOnClick);
            getZx().trace.traceParam("pstrOnMouseOver", pstrOnMouseOver);
            getZx().trace.traceParam("pstrOnMouseOut", pstrOnMouseOut);
            getZx().trace.traceParam("pstrOnMouseDown", pstrOnMouseDown);
            getZx().trace.traceParam("pstrOnMouseUp", pstrOnMouseUp);
            getZx().trace.traceParam("pstrPostLabel", pstrPostLabel);
            getZx().trace.traceParam("pstrPostLabelClass", pstrPostLabelClass);
        }

        zXType.rc formEntryFKControl = zXType.rc.rcOK;
        
        DSRS objRS = null;
                
        try {
            /**
             * Get handle to FK BO
             */
            Property objProp = pobjBO.getValue(pobjAttr.getName());
            ZXBO objFKBO = objProp.getFKBO();
            if(objFKBO == null) {
                throw new Exception("Unable to get handle to the FK object :" + pobjAttr.getName());
            }
            
            /**
             * DGS07APR2003: Let the option list know the fk bo and entity size
             * DGS13FEB2004: Can be overriden by an edit enhancer, to get list and search ability
             * even on small entities. Does NOT affect the rows loaded into the drop down.
             * DGS08JUL2004: If restrict, treat as small entity unless explicitly enhanced
             */
            boolean blnOptional = (pobjAttr.isOptional() || penmFormType.equals(zXType.webFormType.wftSearch));
            EditFormOptions objEditFormOptions = initEditFormOptions(pobjBO, pobjAttr, blnOptional);
            if (objEditFormOptions == null) {
            	throw new ZXException("Unable to initiate edit form options list", pobjAttr.getName());
            }
            
            int intFKBOSize = objFKBO.getDescriptor().getSize().pos;
            
            /**
             * DGS07APR2003: Let the option list know the fk bo and entity size
             * DGS13FEB2004: Can be overriden by an edit enhancer, to get list and search ability
             * even on small entities. Does NOT affect the rows loaded into the drop down.
             * DGS08JUL2004: If restrict, treat as small entity unless explicitly enhanced
             */
            String strEntitySize = getEditEnhancerProperty(pobjBO, pobjAttr, "entitySize");
            if (StringUtil.len(strEntitySize) == 0) {
                strEntitySize = (intFKBOSize == zXType.entitySize.esSmall.pos ? "s" : (intFKBOSize == zXType.entitySize.esMedium.pos? "m" : "l")) ;
            }
            
            /**
             * Use new control for medium-sized entities only.
             * Large entities uses the old popup behaviour.
             * Do not look at entitySize as defined for FKBO but look at the entitySize we have determined it should
             * be after taking into consideration any enhancers that may apply
             */
            if (strEntitySize.equals("m")) {
            	formEntryFKControl = formEntryFKAJAXControl(penmFormType, 
            												pobjBO, pobjAttr,
            												
            												pstrClass,
            												pstrOnBlur,
            												pstrOnChange,
            												pstrOnKeyPress,
            												pblnDisabled,
            												pstrTabIndex,
            												pstrOnFocus,
            												pstrOnKeyDown,
            												pstrOnKeyUp,
            												pstrOnClick,
            												pstrOnMouseOver,
            												pstrOnMouseOut,
            												pstrOnMouseDown,
            												pstrOnMouseUp,
            												
            												pstrPostLabel,
            												pstrPostLabelClass);
            	return formEntryFKControl;
            }
            
            /**
             * Handle the default form entry header :
             */
            boolean blnMerged = formEntryHeader(pobjBO, pobjAttr, penmFormType);
            
            String strControlName = controlName(pobjBO, pobjAttr);
            
            /**
             * Different behaviour for small & medium / large entity
             * 
             * DGS07APR2003: Basically similar now, with differences here and there
             * Add our own little thing to the onChange handle
             */
            pstrOnChange = "zXDirty=1;zXSelectOnChange(event, this);" + pstrOnChange + ";";
            pstrOnFocus = "zXSelectOnFocus(event, this);" + pstrOnFocus + ";";
            pstrOnKeyPress = "zXSelectOnKeyPress(event, this);" + pstrOnKeyPress + ";";
            pstrOnMouseDown = "zXSelectOnMouseDown(event, this);" + pstrOnMouseDown + ";";
            
            /**
             * DGS07APR2003: Changing the selected item needs to be caught in the javascript:
             */
            pstrOnChange = pstrOnChange + "zXOption" + strControlName + ".onChange(this.selectedIndex);";
            
            /**
             * Open select box
             */
            this.s.append("<select ")
                  .appendAttr("id", "_id_" + strControlName)
            	  .appendAttr("name", strControlName)
                  .appendAttr("align", blnMerged?"":"left")
                  .appendAttr("style", "text-align:left") // Overide the alignment for Firefox.
                  .appendAttr("class", pstrClass)
                  .appendAttr("disabled", pblnDisabled?"true":"")
                  .appendAttr("tabIndex", pstrTabIndex)
                  .appendAttr("onFocus", pstrOnFocus)
                  .appendAttr("onKeyPress", pstrOnKeyPress)
                  .appendAttr("onMouseDown", pstrOnMouseDown)
                  .appendAttr("onChange", pstrOnChange)
                  .appendAttr("onBlur", pstrOnBlur)
                  .appendAttr("onClick", pstrOnClick)
                  .appendAttr("onKeyDown", pstrOnKeyDown)
                  .appendAttr("onKeyUp", pstrOnKeyUp)
                  .appendAttr("onMouseOver", pstrOnMouseOver)
                  .appendAttr("onMouseOut", pstrOnMouseOut)
                  .appendAttr("onMouseUp", pstrOnMouseUp)
                  .appendAttr("title", tooltip(pobjBO, pobjAttr))
                  .appendNL('>');
            
            /**
             * Close select box
             */
            this.s.append("</select>").append(HTMLGen.NL);
            
            /**
             * Now generate the JS part for the editOptions
             */
            editFormOptionsJS(pobjBO, pobjAttr, blnOptional, objEditFormOptions.isBound());
            
            /**
             * If we don't have anything in the FK from or to attr, give a default value. Otherwise
             * can get problems in the zXFK pageflow when it tries to evaluate -fka and -fkat.
             */
            if (StringUtil.len(objEditFormOptions.getFKFromAttr()) == 0) {
                objEditFormOptions.setFKFromAttr(objFKBO.getDescriptor().getPrimaryKey());
            }
            if (StringUtil.len(objEditFormOptions.getFKToAttr()) == 0) {
                objEditFormOptions.setFKToAttr(objEditFormOptions.getFKFromAttr());
            }
            
            /**
             * DGS07APR2003: Let the option list know the fk bo and entity size
             * DGS13FEB2004: Can be overriden by an edit enhancer, to get list and search ability
             * even on small entities. Does NOT affect the rows loaded into the drop down.
             */           
            // zXForm.js = zXOption.prototype.fkbo
            this.s.append("zXOption").append(strControlName).append(".fkbo('")
            	  // _fkboname
            	  .append(objFKBO.getDescriptor().getName()).append("','")
            	  // _fkfromattr
                  .append(objEditFormOptions.getFKFromAttr()).append("','")
                  // _fktoattr
                  .append(objEditFormOptions.getFKToAttr()).append("','")
                  // _esize
                  .append(strEntitySize)
                  
                  // JAVASCRIPT CHANGES
                  .append("','")
                  // _boname
                  .append(pobjBO.getDescriptor().getName()).append("','")
                  // _attr
                  .append(pobjAttr.getName())
                  // JAVASCRIPT CHANGES
                  
                  .append("');").appendNL();
            
            /**
             * If optional or a search form: add the - option
             * DGS17DEC2003 (raised during e-DT Phase 2 development): Also include the - option if the
             * current attr value is null (see module history comment for more details)
             */
            if (objProp.isNull 
                    || pobjAttr.isOptional() 
                    || penmFormType.equals(zXType.webFormType.wftSearch) 
                    || penmFormType.equals(zXType.webFormType.wftGrid)){
                /**
                 * DGS10MAR2003: Options lists now populated from an array of the zXOption javascript object:
                 * DGS07APR2003: This is type "b" (blank option) i.e. selecting this blanks out the selection
                 */
                this.s.append("zXOption").append(strControlName).append(".row(\"-\",\"-\",\"-\",\"-\",false,\"b\");").append(HTMLGen.NL);
            }
            
            /**
             * DGS10MAR2003: If this field is bound to another, also need to get the FK to that entity:
             * BD1JUL05: May have an FK label overrule
             */
            String strAttrGroup;
            if (StringUtil.len(pobjAttr.getFkLabelGroup()) > 0) {
            	strAttrGroup = "+," + pobjAttr.getFkLabelGroup();
            } else {
            	strAttrGroup = "+,label";
            }
            
            String strFKAttrValCurrent = "";
            if (objEditFormOptions.isBound()) {
                strAttrGroup = strAttrGroup + ',' + objEditFormOptions.getFKFromAttr();
                /**
                 * DGS18FEB2005 V1.4:43: If bound to another attr get the current value of that attr within this BO
                 */
                strFKAttrValCurrent = pobjBO.getValue(objEditFormOptions.getBoundAttr()).getStringValue();
            }
            
            if(StringUtil.len(objEditFormOptions.getFKToAttr()) > 0) {
                strAttrGroup = strAttrGroup + "," + objEditFormOptions.getFKToAttr(); 
            }
            
            /**
             * BUILD Qurery STATEMENT : 
             */
            
            DSHandler objDSHandler = getZx().getDataSources().getDS(objFKBO);
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            /**
             * DGS07APR2003: If there is a 'Restrict' dependency, add a where clause accordingly.
             */
            zXType.compareOperand enmOps;
            if (objEditFormOptions.isRestrict()) {
                /**
                 * Convert operator to SQL-aware enum. Not an exact conversion but close (can't do
                 * startswith for example, and not an exact equivalent for isnull/notnull but should
                 * work)
                 */
                if (objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoGE)) {
                    enmOps = zXType.compareOperand.coGE;
                } else if (objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoGT)) {
                    enmOps = zXType.compareOperand.coGT;
                } else if (objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoLE)) {
                    enmOps = zXType.compareOperand.coLE;
                } else if (objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoLT)) {
                    enmOps = zXType.compareOperand.coLT;
                } else if (objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoNE)
                        || objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoNotNull)) {
                    enmOps = zXType.compareOperand.coNE;
                } else {
                    enmOps = zXType.compareOperand.coEQ;
                }
                
                /**
                 * Get the attr. If the developer has fouled up the expression, this could fail to
                 * be found, in which case get error situation.
                 */
                Attribute objAttr = objFKBO.getDescriptor().getAttribute(objEditFormOptions.getRestrictFKFromAttr());
                if (objAttr == null) {
                    throw new ZXException("Unable to get attribute from FK business object ", objEditFormOptions.getRestrictFKFromAttr());
                }
                
                /**
                 * Generate the additional where clause
                 * DGS23FEB2004: If the restrict value is a zero-length string, explicitly set the 'isNull' property
                 * of the str value used in the wherecondition. Otherwise get no rhs operand and a SQL error.
                 */
                boolean blnIsNull = StringUtil.len(objEditFormOptions.getRestrictValue()) == 0;
                objDSWhereClause.singleWhereCondition(objFKBO,
                                                      objAttr,
                                                      enmOps,
                                                      new StringProperty(objEditFormOptions.getRestrictValue(), blnIsNull),
                                                      zXType.dsWhereConditionOperator.dswcoNone);
            } // Restrict?
            
            /**
             * DGS07APR2003: For medium and large, only select the current value's row, not all rows
             * 
             * TODO : Decide with option is the best the commented one or the uncommented one.
             */
            if (!objEditFormOptions.isRestrict() && !strEntitySize.equals("s")) { // intFKBOSize != zXType.entitySize.esSmall.pos) {
                /**
                 * It is possible that the FK is null even though the FK attribute is not optional;
                 * this is the case for search forms and new edit forms where there is no default
                 * value for the FK attribute
                 */
                if (pobjBO.getValue(pobjAttr.getName()).isNull) {
                    objDSWhereClause.addClauseWithAND(pobjBO, ":1=0");
                    
                } else {
	                objDSWhereClause.singleWhereCondition(objFKBO,
	                									  objFKBO.getDescriptor().getAttribute(objFKBO.getDescriptor().getPrimaryKey()),
	                									  zXType.compareOperand.coEQ, 
	                									  pobjBO.getValue(pobjAttr.getName()),
	                									  zXType.dsWhereConditionOperator.dswcoAnd); // Add to AND statement.
	                
            	} // FK value is null yet not optional?
                
            } else {
                /**
                 * DGS20OCT2003: For small, can further restrict the returned recordset by an optional where clause
                 */
                if (StringUtil.len(objEditFormOptions.getFKWhere()) > 0) {
                    objDSWhereClause.addClauseWithAND(objFKBO, objEditFormOptions.getFKWhere());
                }
                
            } // Restricted or small?
            
            
            boolean blnHaveOptions = false;
            
            /**
             * Do this for medium and large entities too - it will have the effect of copying
             * selected values from previous lines, which is often useful. However, do not set
             * the 'haveOptions' boolean for medium and large because we still must do the DB select
             * a few lines down to get this row's fk value.
             * This also means we could end up with duplicates as we keep copying the previous row's
             * JS zXOption list, so have made a change to zXForm.js in the .rows prototype such that
             * it does not add a row if it is already in the list.
             */
            if (!objEditFormOptions.isRestrict() && StringUtil.len(objEditFormOptions.getFKWhere()) == 0) {
                /**
                 * DGS18JUN2004: If already generated an option list and it isn't affected by
                 * restrictions or where clauses, can just copy the previous one without
                 * selecting the FK data again. This requires some fiddling around with the alias
                 * of the BO. Also depends on grid and matrix BO aliases being tweaked in a fixed
                 * formatted way (MtrxRRRRRCCCCC or GridRRRRRCCCCC where RRRRR = row and
                 * CCCCC = column). Not very elegant but should work ok.
                 * 
                 * NOTE : May need refactoring
                 * NOTE : Document this hack :).
                 */
                int intAlias = StringUtil.len(pobjBO.getDescriptor().getAlias());
                if (intAlias > 14) {
                    String strFormSource = pobjBO.getDescriptor().getAlias().substring(0, 4);
                    String strRowCol = pobjBO.getDescriptor().getAlias().substring(10);
                    
                    if ((strFormSource.equalsIgnoreCase("Grid") || strFormSource.equalsIgnoreCase("Mtrx")) 
                        && StringUtil.isNumeric(strRowCol)) {
                        int intRow = Integer.parseInt(strRowCol.substring(0, strRowCol.length() - 5));
                        
                        if (intRow > 1) {
                            if (objFKBO.getDescriptor().getSize().pos == zXType.entitySize.esSmall.pos) {
                                blnHaveOptions = true;
                            }
                            
                            String strSaveAlias = pobjBO.getDescriptor().getAlias();
                            pobjBO.getDescriptor().setAlias(strFormSource 
                                                            + StringUtil.right((intRow - 1) + "", '0', 5) 
                                                            + pobjBO.getDescriptor().getAlias().substring(intAlias - 5));
                            
                            String strPrevOption = "zXOption" + controlName(pobjBO, pobjAttr);
                            pobjBO.getDescriptor().setAlias(strSaveAlias);
                            this.s.append("zXOption")
                                  .append(controlName(pobjBO, pobjAttr))
                                  .append(".copyrows(").append(strPrevOption).append(",\"").append(pobjBO.getValue(pobjAttr.getName()).getStringValue()).append("\");").appendNL();
                            
                        } // Not first row so I can copy from first?
                        
                    } // Valid grid or matrix entity?
                    
                } // Alias fiddled for grid or matrix?
                
            } // Not restricted in anyway?
            
            
            if (!blnHaveOptions) {
                /**
                 * If optional or a search form: add the - option
                 * DGS17DEC2003 (raised during e-DT Phase 2 development): Also include the - option if the
                 * current attr value is null (see module history comment for more details)
                 */
                if (pobjBO.getValue(pobjAttr.getName()).isNull || pobjAttr.isOptional() 
                   || penmFormType.equals(zXType.webFormType.wftSearch) || penmFormType.equals(zXType.webFormType.wftGrid)) {
                    /**
                     * DGS10MAR2003: Options lists now populated from an array of the zXOption javascript object:
                     * DGS07APR2003: This is type "b" (blank option) i.e. selecting this blanks out the selection.
                     */
                    this.s.append("zXOption").append(controlName(pobjBO, pobjAttr)).append(".row(\"-\",\"-\",\"-\",\"-\",false,\"b\");").append(HTMLGen.NL);
                    
                } // Optional, search form or grid / matrix?
                
                /**
                 * Add order by clause; if there is a group called FKSort use this, otherwise
                 * use label
                 */
                String strTmp = "";
                if (objDSHandler.getOrderSupport().pos > zXType.dsOrderSupport.dsosNone.pos) {
                    if (objFKBO.getDescriptor().getGroup("FKSort").size() == 0) {
                    	if (StringUtil.len(pobjAttr.getFkLabelGroup()) > 0) {
                    		strTmp = pobjAttr.getFkLabelGroup();
                    	} else {
                    		strTmp = "label";
                    	} // FK label override?
                    	
                    } else {
                        strTmp = "FKSort";
                    } // FKSort group exists?
                    
                } // Supports order by?
                
                /**
                 * v1.5:6 - In case of non-small entity for a searchform: no need to populate dropdown
                 * 
                 * NOTE : We do not want to perform a query if there this is a search form AND the enity is not
                 * Small.
                 */
                if (!(penmFormType.equals(zXType.webFormType.wftSearch) && objFKBO.getDescriptor().getSize().pos != zXType.entitySize.esSmall.pos)) {
                    /**
                     * In case of a search form: no where clause (want to see all)
                     **/
                    if (penmFormType.equals(zXType.webFormType.wftSearch)) {
                        objRS = objDSHandler.boRS(objFKBO,
                                                      strAttrGroup,
                                                      "",
                                                      true,
                                                      strTmp,
                                                      false,
                                                      0,0);
                        
                    } else {
                        objRS = objDSHandler.boRS(objFKBO,
                                                  strAttrGroup,
                                                  objDSWhereClause.getAsCompleteWhereClause(),
                                                  true,
                                                  strTmp,
                                                  false,
                                                  0,0);
                        
                    } // Search form?
                    
                    if (objRS == null) {
                        throw new Exception("Unable to retrieve values for FK " + pobjAttr.getName());
                    }
                    
                    
                    String strFKFromAttrVal = "";
                    String strFKToAttrVal = "";
                    String strSel = "";
                    
                    while (!objRS.eof()) {
                        objRS.rs2obj(objFKBO, strAttrGroup);
                        
                        /**
                         * DGS10MAR2003: Now load options lists by first populating an array:
                         */
                        String strAttrVal = objFKBO.getPKValue().getStringValue();
                        
                        // String strAttrLabel = objFKBO.formattedString("label");
                        pobjBO.getValue(pobjAttr.getName()).resolveFKLabel(true);
                        String strAttrLabel = pobjBO.getValue(pobjAttr.getName()).getFkLabel();
                        
                        /**
                         * If bound to another attr - get the PK of that attr:
                         */
                        if (objEditFormOptions.isBound()) {
                            strFKFromAttrVal = objFKBO.getValue(objEditFormOptions.getFKFromAttr()).getStringValue();
                        }
                        strFKToAttrVal = objFKBO.getValue(objEditFormOptions.getFKToAttr()).getStringValue();
                        
                        /**
                         * Set strSel to note whether or not this option in the list is the current value
                         * Never relevant for search form
                         */
                        if (!penmFormType.equals(zXType.webFormType.wftSearch)) {
	                        if (objProp.compareTo(objFKBO.getPKValue()) == 0) {
	                            /**
	                             * DGS18FEB2005 V1.4:43: When bound, the comparison is not simply against
	                             * this attr. If bound, and not only this value matches, but also the bound
	                             * entity's related attr matches this edit form's value of that attr,
	                             * then this is the row to be selected.
	                             */
	                            if (objEditFormOptions.isBound()) {
	                                if (strFKAttrValCurrent.equals(strFKFromAttrVal)) {
	                                    strSel = "true";
	                                } else {
	                                    strSel = "false";
	                                }
	                            } else {
	                                /**
	                                 * Here NOT bound, so as this value matches this is the row to be selected.
	                                 */
	                                strSel = "true";
	                            }
	                        } else {
	                            strSel = "false"; 
	                        }
	                        
                        } else {
                        	strSel = "false";
                        } // Search form?
                        
                        /**
                         * DGS07APR2003: This is type "n" (normal option) i.e. selecting this replaces the previous selection.
                         * DGS23FEB2004: Escape the label to make it javascript safe (e.g. quotes cause problems).
                         */
                        this.s.append("zXOption").append(strControlName).append(".row(\"").append(strFKFromAttrVal).append("\",\"")
                        	  .append(strFKToAttrVal).append("\",\"").append(strAttrVal)
                        	  .append("\",\"").append(StringEscapeUtils.escapeJavaScript(strAttrLabel)).append("\",").append(strSel).append(",\"n\");").appendNL();
                        
                        objRS.moveNext();
                        
                    } // Loop over FK values
                    
                } //  Search form / non-small?
                
                
            } // Have options?
            
            /**
             * Force it to load the options list by simulating either the changing of the related
             *  fk obj attr, or if there is no such relationship, pass an empty string to make it
             *  load all options.
             */
            this.s.append("zXOption").append(strControlName).append(".onChangeRel('").append(strFKAttrValCurrent).append("');").append(HTMLGen.NL);
            
            this.s.append("</script>").append(HTMLGen.NL);
            
            /**
             * DGS09APR2003: Can show a clickable image next to FK lists to view the details.
             */
            formFKLookupButton(pobjBO, pobjAttr, objFKBO.getDescriptor().getSize());
            
            /**
             * DGS20AUG2003: Can show a clickable image next to FK lists to add a new instance.
             */
            formFKAddButton(pobjBO, pobjAttr, objFKBO);
            
            /**
             * Handle any enhancer refs
             */
            processFieldRef(penmFormType, pobjBO, pobjAttr);
            
            /**
             * DGS28JUN2004: Handle an enhancer post label
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit) 
               || penmFormType.equals(zXType.webFormType.wftGrid)) {
                
                if (StringUtil.len(pstrPostLabel) > 0) {
                    if (StringUtil.len(pstrPostLabelClass) == 0) {
                        pstrPostLabelClass = "zXFormLabel";
                    }
                    s.append("<span ").appendAttr("class", pstrPostLabelClass).appendNL('>');
                    s.append(pstrPostLabel).append(HTMLGen.NL);
                    s.append("</span>").append(HTMLGen.NL);
                }
                
            }
            
            /**
             * DGS28JAN2004: If in a search form we need a span here, although there's never
             * anything in it for an FK. Otherwise the onChange JS doesn't work.
             */
            if (penmFormType.equals(zXType.webFormType.wftSearch)) {
                this.s.append("<span").append(HTMLGen.NL);
                this.s.appendAttr("id", "zXSpan" + strControlName).append(HTMLGen.NL);
                this.s.appendAttr("style", "display:none").append(HTMLGen.NL);
                this.s.appendNL('>');
                this.s.append("</span>").append(HTMLGen.NL);
            }
            
            if (!getEditEnhancerBoolean(pobjBO, pobjAttr,"mergeWithNext")) {
                this.s.append("</td>");
            }
            
            return formEntryFKControl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for a form entry that has an FK.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
                getZx().log.error("Parameter : pstrOnBlur = "+ pstrOnBlur);
                getZx().log.error("Parameter : pstrOnChange = "+ pstrOnChange);
                getZx().log.error("Parameter : pstrOnKeyPress = "+ pstrOnKeyPress);
                getZx().log.error("Parameter : pblnDisabled = "+ pblnDisabled);
                getZx().log.error("Parameter : pstrTabIndex = "+ pstrTabIndex);
                getZx().log.error("Parameter : pstrOnFocus = "+ pstrOnFocus);
                getZx().log.error("Parameter : pstrOnKeyDown = "+ pstrOnKeyDown);
                getZx().log.error("Parameter : pstrOnKeyUp = "+ pstrOnKeyUp);
                getZx().log.error("Parameter : pstrOnClick = "+ pstrOnClick);
                getZx().log.error("Parameter : pstrOnMouseOver = "+ pstrOnMouseOver);
                getZx().log.error("Parameter : pstrOnMouseOut = "+ pstrOnMouseOut);
                getZx().log.error("Parameter : pstrOnMouseDown = "+ pstrOnMouseDown);
                getZx().log.error("Parameter : pstrOnMouseUp = "+ pstrOnMouseUp);
                getZx().log.error("Parameter : pstrPostLabel = "+ pstrPostLabel);
                getZx().log.error("Parameter : pstrPostLabelClass = "+ pstrPostLabelClass);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            formEntryFKControl = zXType.rc.rcError;
            return formEntryFKControl;
        } finally {
            /**
             * Close resultset
             */
            if (objRS != null) objRS.RSClose();
            
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntryFKControl);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Generate HTML for a form entry that has an FK.
     * 
     * <pre>
     * 
     * Reviewed for V1.5:83
     * </pre>
     * 
     * @param penmFormType The web form type 
     * @param pobjBO The business object of the form entry 
     * @param pobjAttr The attribute the form entry belongs to. 
     * @param pstrClass The css class name. 
     * @param pstrOnBlur The onBlur javascript action. 
     * @param pstrOnChange The onChange javascript action. 
     * @param pstrOnKeyPress The onKeyPress action. 
     * @param pblnDisabled Whether the form entry is disabled or not. 
     * @param pstrTabIndex The tab index of the form. 
     * @param pstrOnFocus The onFocus javascript action. 
     * @param pstrOnKeyDown The onKeyDown javascript action 
     * @param pstrOnKeyUp The onKeyUp javascript action. 
     * @param pstrOnClick The onClick action. 
     * @param pstrOnMouseOver The onMouseOver action 
     * @param pstrOnMouseOut The onMouseOut action 
     * @param pstrOnMouseDown The onMouseDown action 
     * @param pstrOnMouseUp The onMouseUp action 
     * @param pstrPostLabel Post Label
     * @param pstrPostLabelClass Post Lable class.
     * 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formEntryFKControl fails. 
     */
     public zXType.rc formEntryFKAJAXControl(zXType.webFormType penmFormType, 
                                         	 ZXBO pobjBO,
                                         	 Attribute pobjAttr,
                                             String pstrClass,
                                             String pstrOnBlur,
                                             String pstrOnChange,
                                             String pstrOnKeyPress,
                                             boolean pblnDisabled,
                                             String pstrTabIndex,
                                             String pstrOnFocus,
                                             String pstrOnKeyDown,
                                             String pstrOnKeyUp,
                                             String pstrOnClick,
                                             String pstrOnMouseOver,
                                             String pstrOnMouseOut,
                                             String pstrOnMouseDown,
                                             String pstrOnMouseUp,
                                         	 String pstrPostLabel,
                                         	 String pstrPostLabelClass) throws ZXException {
		if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            /**
             * Some paramerters as previous call.
             */
        }
		
		zXType.rc formEntryFKAJAXControl = zXType.rc.rcOK;
		
		try {
			/**
			 * Handle the default form entry header :
			 */
			formEntryHeader(pobjBO, pobjAttr, penmFormType);

			/**
			 * Get handle to FK BO
			 */
			Property objProp = pobjBO.getValue(pobjAttr.getName());
			ZXBO objFKBO = objProp.getFKBO();
			if (objFKBO == null) {
				throw new ZXException("Unable to get handle to the FK object.", pobjAttr.getName());
			}

			String strControlName = controlName(pobjBO, pobjAttr);

			/**
			 * If edit, we have to put the current value of the field into the
			 * box. Get the value that we want to submit.
			 */
			String strValue = "";
			int intDataType = pobjAttr.getDataType().pos;
			if ((penmFormType.equals(zXType.webFormType.wftEdit) || penmFormType.equals(zXType.webFormType.wftGrid))
				&& !pobjBO.getValue(pobjAttr.getName()).isNull) {
				DateFormat df;
				if (intDataType == zXType.dataType.dtDate.pos) {
					df = getZx().getDateFormat();
					strValue = df.format(pobjBO.getValue(pobjAttr.getName()).dateValue());

				} else if (intDataType == zXType.dataType.dtTimestamp.pos) {
					df = getZx().getTimestampFormat();
					strValue = df.format(pobjBO.getValue(pobjAttr.getName()).dateValue());

				} else if (intDataType == zXType.dataType.dtTime.pos) {
					df = getZx().getTimeFormat();
					strValue = df.format(pobjBO.getValue(pobjAttr.getName()).dateValue());

				} else {
					strValue = pobjBO.getValue(pobjAttr.getName()).getStringValue();
				}
				
			}
			
			/**
			 * Determine the label attribute group. 
			 */
            String strAttrGroup;
            if (StringUtil.len(pobjAttr.getFkLabelGroup()) > 0) {
            	strAttrGroup = pobjAttr.getFkLabelGroup();
            } else {
            	strAttrGroup = "label";
            }
            
			/**
			 * Get the value that we want to display in the input field
			 */
			String visualValue = "";
			if (StringUtil.len(strValue) > 0) {
				objFKBO.setPKValue(strValue);
				objFKBO.loadBO(strAttrGroup);
				
				if (StringUtil.len(pobjAttr.getFkLabelExpression()) > 0) {
					pobjBO.getValue(pobjAttr.getName()).resolveFKLabel(true);
					visualValue = pobjBO.getValue(pobjAttr.getName()).getFkLabel();
					
				} else {
					visualValue = objFKBO.formattedString(strAttrGroup);
					
				}
				
			}
			
			/**
			 * Try to calculate the length and output length.
			 */
			int intOutputLength = 0;
			int intLength = 0;
			AttributeCollection colAttr = objFKBO.getDescriptor().getGroup(strAttrGroup);
			Iterator iter = colAttr.iterator();
			while (iter.hasNext()) {
				Attribute objAttr = (Attribute)iter.next();
				intOutputLength = intOutputLength + objAttr.getOutputLength();
				intLength = intLength + objAttr.getLength();
			}
			
			/**
			 * The input control. This represents the visual value of the field.
			 * 
			 * If in a edit form it is disabled by default.
			 */
			this.s.append("<input autocomplete=\"off\" ")
			
				  .appendAttr("id", "_id_" + strControlName)
				  // This value is not processed on the server side.
				  .appendAttr("name", "_dummy_" + strControlName)
				  
				  .appendAttr("value", StringEscapeUtils.escapeHtml(visualValue))
				  
				  .appendAttr("size", intOutputLength)
				  .appendAttr("maxlength", intLength)
				  
				  .appendAttr("type", "text")
				  .appendAttr("style", "text-align:left;")
				  .appendAttr("disabled", StringUtil.len(strValue)> 0?"true": (pblnDisabled?"true":""))
				  .appendAttr("class", pstrClass)
				  .appendAttr("title", tooltip(pobjBO, pobjAttr))
				  
				  .appendAttr("onBlur", pstrOnBlur)
				  .appendAttr("onChange", pstrOnChange)
				  .appendAttr("onKeyPress", pstrOnKeyPress)
				  .appendAttr("onFocus", pstrOnFocus)
				  .appendAttr("onKeyDown", pstrOnKeyDown)
				  .appendAttr("onKeyUp", pstrOnKeyUp)
				  .appendAttr("onClick", pstrOnClick)
				  .appendAttr("onMouseOver", pstrOnMouseOver)
				  .appendAttr("onMouseOut", pstrOnMouseOut)
				  .appendAttr("onMouseDown", pstrOnMouseDown)
				  .appendAttr("onMouseUp", pstrOnMouseUp)
				  
				  .appendAttr("tabindex", pstrTabIndex)
				  
				  .appendNL('>');
			
			/**
			 * The actual value that gets submitted by the form
			 */
			this.s.append("<input ")
				  .appendAttr("id", strControlName)
				  .appendAttr("name", strControlName)
				  .appendAttr("value", strValue)
				  .appendAttr("type", "hidden")
				  .appendNL('>');

			/**
			 * The div to display the results in.
			 * This is hidden by default.
			 */
			this.s.append("<div ")
				  .appendAttr("id", "_popup_" + strControlName)
				  .appendAttr("class", "zxfklookup")
				  .append("><ul></ul></div>").appendNL();
			
			/**
			 * Unlock button
			 * 
			 * This should unlock the input control and hide this button.
			 */
			if (StringUtil.len(strValue) > 0) {
				/**
				 * Display a Unlock image
				 * NOTE : Onclick with unlock the input field and then this image 
				 * will have the same functionality as the search icon below.
				 */
				this.s.append("<input ")
					  .appendAttr("type", "image")
					  .appendAttr("alt", "Unlock")
					  .appendAttr("title", "Unlock")
					  .appendAttr("value", "")
			      	  .appendAttr("id", "_search_" + strControlName)
					  .appendAttr("src", "../images/unlock.gif")
					  .appendNL('>');
				
			} else {
				/**
				 * Display a Search Icon
				 * 
				 * NOTE : Onlick with trigger a search. While searching the icon will 
				 * become a searching animation and it will block any double search requests. 
				 */
				this.s.append("<input ")
					  .appendAttr("type", "image")
					  .appendAttr("alt", "Search")
					  .appendAttr("title", "Search")
					  .appendAttr("value", "")
					  .appendAttr("id", "_search_" + strControlName)
					  .appendAttr("src", "../images/search_icon.gif")
					  .appendNL('>');
				
//				/**
//				 * Using _search_button_ :
//				 * 
//				 * NOTE : This is an alternative to the above.
//				 * This fixes some of the behaviour problems with
//				 * this control.
//				 */
//				this.s.append("<button name=\"button\" type=\"button\"")
//					  .appendAttr("id", "_search_button_" + strControlName)
//				      .appendAttr("class", "zxLookupButton")
//				      .appendAttr("style","width: 30px;font-size: 8px;") 
//					  .appendNL('>');
//				this.s.append("<img ")
//					  .appendAttr("id", "_search_" + strControlName)
//					  .appendAttr("src", "../images/search_icon2.gif")
//					  .appendAttr("alt", "Search")
//					  .appendAttr("title", "Search")
//					  .appendNL('>');
//				this.s.append("</button>");
				
			}

			/**
			 * Can show a clickable image next to FK lists to view the details.
			 * Only show any image if there is a result.
			 */
			String strURL = "../jsp/zXFK.jsp?-pf=zXFK&";
			strURL += "-s=" + getZx().getSession().getSessionid();
			strURL += "&-fke=" + objFKBO.getDescriptor().getName();
			strURL += "&-fkval=' + value + '";
			strURL += "&-ss=' + zXSubSessionId() + '&-a=framesetShowForm";
			
			this.s.append("<img ")
				  .appendAttr("id", "_display_" + strControlName)
				  .appendAttr("src", "../images/listItem.gif")
				  .appendAttr("alt", "Show details " + pobjAttr.getLabel().getDescription())
				  .appendAttr("title", "Show details " + pobjAttr.getLabel().getDescription())
				  .appendAttr("onMouseDown", "this.style.cursor='wait';window.document.body.style.cursor='wait';javascript:"
											 + "var value = document.getElementById('" + strControlName + "').value;"
											 + " if(value!=''){ FKPopup('" + strURL + "'); };"
											 + "this.style.cursor='';window.document.body.style.cursor=''")
				  .appendAttr("onMouseOver", "javascript:this.src='../images/listItemOver.gif'")
				  .appendAttr("onMouseOut", "javascript:this.src='../images/listItem.gif'")
				  .appendNL('>');
			
			/**
			 * Can show a clickable image next to FK lists to add a new
			 * instance.
			 */
			formFKAddButton(pobjBO, pobjAttr, objFKBO);
			
            /************************
             * Handle edit enhancers
             *************************/
            boolean blnOptional = (pobjAttr.isOptional() || penmFormType.equals(zXType.webFormType.wftSearch));
            EditFormOptions objEditFormOptions = initEditFormOptions(pobjBO, pobjAttr, blnOptional);
            
            String strFKWhereClause = "";
            
            if (objEditFormOptions.isRestrict() || StringUtil.len(objEditFormOptions.getFKWhere()) > 0) {
	            DSWhereClause objDSWhereClause = new DSWhereClause();
	            
	            /**
	             * If there is a 'Restrict' dependency, add a where clause accordingly.
	             */
	            zXType.compareOperand enmOps;
	            if (objEditFormOptions.isRestrict()) {
	                /**
	                 * Convert operator to SQL-aware enum. Not an exact conversion but close (can't do
	                 * startswith for example, and not an exact equivalent for isnull/notnull but should
	                 * work)
	                 */
	                if (objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoGE)) {
	                    enmOps = zXType.compareOperand.coGE;
	                } else if (objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoGT)) {
	                    enmOps = zXType.compareOperand.coGT;
	                } else if (objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoLE)) {
	                    enmOps = zXType.compareOperand.coLE;
	                } else if (objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoLT)) {
	                    enmOps = zXType.compareOperand.coLT;
	                } else if (objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoNE)
	                           || objEditFormOptions.getRestrictOp().equals(zXType.pageflowDependencyOperator.pdoNotNull)) {
	                    enmOps = zXType.compareOperand.coNE;
	                } else {
	                    enmOps = zXType.compareOperand.coEQ;
	                }
	                
	                /**
	                 * Get the attr. If the developer has fouled up the expression, this could fail to
	                 * be found, in which case get error situation.
	                 */
	                Attribute objAttr = objFKBO.getDescriptor().getAttribute(objEditFormOptions.getRestrictFKFromAttr());
	                if (objAttr == null) {
	                    throw new ZXException("Unable to get attribute from FK business object ", objEditFormOptions.getRestrictFKFromAttr());
	                }
	                
	                /**
	                 * Generate the additional where clause
	                 */
	                boolean blnIsNull = StringUtil.len(objEditFormOptions.getRestrictValue()) == 0;
	                objDSWhereClause.singleWhereCondition(objFKBO,
	                                                      objAttr,
	                                                      enmOps,
	                                                      new StringProperty(objEditFormOptions.getRestrictValue(), blnIsNull),
	                                                      zXType.dsWhereConditionOperator.dswcoNone);
	            } // Restrict?
	            
	            /**
	             * Restrict the returned recordset by an optional where clause
	             */
	            if (StringUtil.len(objEditFormOptions.getFKWhere()) > 0) {
	                objDSWhereClause.addClauseWithAND(objFKBO, objEditFormOptions.getFKWhere());
	            }
	            
	            strFKWhereClause = objDSWhereClause.getAsCompleteWhereClause();
	            
            } // Do we have any edit enhancers
            /************************
             * Handle edit enhancers
             *************************/
            
			/**
			 * Init the fklookup listener.
			 */
			this.s.append("<script type=\"text/javascript\">").appendNL();
			
			this.s.append("zxfkloopup_init(" +
				   // _session = session id
				   "'" + getZx().getSession().getSessionid() + "', " +
				   
				   // _fke	= The foriegn entity
				   "'" + objFKBO.getDescriptor().getName() + "', " +
				   // _fkwhere = Optional where condtion used to restrict the list of values.
			       "'" + (strFKWhereClause==null?"":strFKWhereClause) + "'," +
				   // _e = The entity with the foriegn key
			       "'" + pobjBO.getDescriptor().getName() + "'," +
				   // _attr = The attribute with the foriegn key
			       "'" + pobjAttr.getName() + "'," +
			       
				   // _inputid = The id of the input field used to diplay the result.
			       "'_id_" + strControlName + "', " +
			       // _hiddenid = The id of the hidden field used to submit the result.
			       "'" + strControlName + "', " +
			       // _popupid = The id the div used to display the results.
			       "'_popup_" + strControlName + "', " +
			       
			       // _searchiconid = The id of the icon to use to push start searches.
			       "'_search_" + strControlName + "', " +
			       
			       // _displayresultid = The id of the icon to show the selected result.
			       "'_display_" + strControlName + "'," +
			       
			       // _searchtype = The type of seach to use.
			       "'" + zXType.valueOf(getWebSettings().getFKSearchType()) + "'," +
			       // _fksearchdelay = The delay between searches
			       " '" + getWebSettings().getFkSearchDelay() + "');").appendNL();
	
			this.s.append("</script>").appendNL();
			
			/**
			 * Handle any enhancer refs
			 */
			processFieldRef(penmFormType, pobjBO, pobjAttr);
			
			/**
			 * Print post label.
			 */
			if (penmFormType.equals(zXType.webFormType.wftEdit)
				|| penmFormType.equals(zXType.webFormType.wftGrid)) {
				if (StringUtil.len(pstrPostLabel) > 0) {
					if (StringUtil.len(pstrPostLabelClass) == 0) {
						pstrPostLabelClass = "zXFormLabel";
					}
					s.append("<span ")
					 .appendAttr("class", pstrPostLabelClass)
					 .appendNL('>');
					s.append(pstrPostLabel).append(HTMLGen.NL);
					s.append("</span>").append(HTMLGen.NL);
					
				}
				
			}
			
			/**
			 * Dummy span for search forms as this control does not 
			 * support between searches.
			 */
			if (penmFormType.equals(zXType.webFormType.wftSearch)) {
				this.s.append("<span").append(HTMLGen.NL);
				this.s.appendAttr("id", "zXSpan" + strControlName).append(HTMLGen.NL);
				this.s.appendAttr("style", "display:none").append(HTMLGen.NL);
				this.s.appendNL('>');
				this.s.append("</span>").append(HTMLGen.NL);
			}
			
			if (!getEditEnhancerBoolean(pobjBO, pobjAttr, "mergeWithNext")) {
				this.s.append("</td>");
			}

			return formEntryFKAJAXControl;
			
		} finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntryFKAJAXControl);
                getZx().trace.exitMethod();
            }
		}
	}    
    
    /**
	 * Generate HTML for a option list.
	 * 
	 * Reviewed for V1.5:51
	 * 
	 * @param penmFormType The type of web form
	 * @param pobjBO The business object of the form entry
	 * @param pobjAttr The attribute of the form entry
	 * @param pstrClass The css class name
	 * @param pstrOnBlur The onBlur javascript action
	 * @param pstrOnChange The onChange action
	 * @param pstrOnKeyPress The onKeyPress javascript action.
	 * @param pblnDisabled Whether the form entry is disabled or not.
	 * @param pstrTabIndex The tab index of the form.
	 * @param pstrOnFocus The onFocus javascript
	 * @param pstrOnKeyDown The onKeyDown action
	 * @param pstrOnKeyUp The onKeyUp action.
	 * @param pstrOnClick The onClick action
	 * @param pstrOnMouseOver The onMouseOver action
	 * @param pstrOnMouseOut The onMouseOut action
	 * @param pstrOnMouseDown The onMouseDown action.
	 * @param pstrOnMouseUp The onMouseUp action
	 * @param pstrPostLabel Post Label title
	 * @param pstrPostLabelClass Post Label title class
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if formEntrySelectControl fails.
	 */
    public zXType.rc formEntrySelectControl(zXType.webFormType penmFormType, 
                                            ZXBO pobjBO, 
                                            Attribute pobjAttr, 
                                            String pstrClass, 
                                            String pstrOnBlur, 
                                            String pstrOnChange, 
                                            String pstrOnKeyPress, 
                                            boolean pblnDisabled, 
                                            String pstrTabIndex, 
                                            String pstrOnFocus, 
                                            String pstrOnKeyDown, 
                                            String pstrOnKeyUp, 
                                            String pstrOnClick, 
                                            String pstrOnMouseOver, 
                                            String pstrOnMouseOut, 
                                            String pstrOnMouseDown, 
                                            String pstrOnMouseUp,
                                            String pstrPostLabel,
                                            String pstrPostLabelClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pstrClass", pstrClass);
            getZx().trace.traceParam("pstrOnBlur", pstrOnBlur);
            getZx().trace.traceParam("pstrOnChange", pstrOnChange);
            getZx().trace.traceParam("pstrOnKeyPress", pstrOnKeyPress);
            getZx().trace.traceParam("pblnDisabled", pblnDisabled);
            getZx().trace.traceParam("pstrTabIndex", pstrTabIndex);
            getZx().trace.traceParam("pstrOnFocus", pstrOnFocus);
            getZx().trace.traceParam("pstrOnKeyDown", pstrOnKeyDown);
            getZx().trace.traceParam("pstrOnKeyUp", pstrOnKeyUp);
            getZx().trace.traceParam("pstrOnClick", pstrOnClick);
            getZx().trace.traceParam("pstrOnMouseOver", pstrOnMouseOver);
            getZx().trace.traceParam("pstrOnMouseOut", pstrOnMouseOut);
            getZx().trace.traceParam("pstrOnMouseDown", pstrOnMouseDown);
            getZx().trace.traceParam("pstrOnMouseUp", pstrOnMouseUp);
            getZx().trace.traceParam("pstrPostLabel", pstrPostLabel);
            getZx().trace.traceParam("pstrPostLabelClass", pstrPostLabelClass);
        }
        
        zXType.rc formEntrySelectControl = zXType.rc.rcOK;
        
        try {
            /**
             * Handle the default header part of a form entry
             */
            boolean blnMerged = formEntryHeader(pobjBO, pobjAttr, penmFormType);
            
            /**
             * Add our own little thing to the onChange handle
             */
            pstrOnChange = "zXSetDirty();zXSelectOnChange(event, this);" + pstrOnChange + ";";
            pstrOnFocus = "zXSelectOnFocus(event, this);" + pstrOnFocus + ";";
            pstrOnKeyPress = "zXSelectOnKeyPress(event, this);" + pstrOnKeyPress + ";";
            pstrOnMouseDown = "zXSelectOnMouseDown(event, this);" + pstrOnMouseDown + ";";
            
            /**
             * Deal with some weird browser rendering stuff:
             * Do NOT use align = left when merged with previous field nor
             * use a class
             */
            String strControlName = controlName(pobjBO, pobjAttr);
            s.append("<select ")
			 .appendAttr("id", "_id_" + strControlName)
			 .appendAttr("name", strControlName)			 
			 .appendAttr("align", blnMerged?"":"left")
             .appendAttr("style", "text-align:left") // Overide the alignment for Firefox.
			 .appendAttr("class", blnMerged?"":pstrClass)
			 .appendAttr("disabled", (pblnDisabled?"true":""))
			 .appendAttr("tabIndex", pstrTabIndex)
			 .appendAttr("onChange", pstrOnChange)
			 .appendAttr("onBlur", pstrOnBlur)
			 .appendAttr("onFocus", pstrOnFocus)
			 .appendAttr("onKeypress", pstrOnKeyPress)
			 .appendAttr("onMouseDown", pstrOnMouseDown)
			 .appendAttr("onClick", pstrOnClick)
			 .appendAttr("onKeyDown", pstrOnKeyDown)
			 .appendAttr("onKeyUp", pstrOnKeyUp)
			 .appendAttr("onMouseOver", pstrOnMouseOver)
			 .appendAttr("onMouseOut", pstrOnMouseOut)
			 .appendAttr("onMouseUp", pstrOnMouseUp)
			 .appendAttr("title", tooltip(pobjBO, pobjAttr))
			 .appendNL('>');
            
            /**
             * If optional or a search form: add the - option
             * DGS17DEC2003 (raised during e-DT Phase 2 development): Also include the - option if the
             * current attr value is null (see module history comment for more details)
             */
            if (pobjBO.getValue(pobjAttr.getName()).isNull || pobjAttr.isOptional()
                || (penmFormType.equals(zXType.webFormType.wftSearch) || penmFormType.equals(zXType.webFormType.wftGrid))) {
                s.appendNL("<option value =\"-\"> - </option>");
            }
            
            Option objOption;
            
            /**
             * TODO : This could be a bug in the getStringValue(), why? coz LongProperty will return a 0.
             */
            String strValue = "";
            Property objProperty = pobjBO.getValue(pobjAttr.getName());
            if (!objProperty.isNull) {
                strValue = objProperty.getStringValue();
            }
            
            Iterator iter = pobjAttr.getOptions().values().iterator();
            while (iter.hasNext()) {
                objOption = (Option)iter.next();
                
                boolean blnSelected = !penmFormType.equals(zXType.pageflowActionType.patSearchForm) && objOption.getValue().equals(strValue);
                s.append("<option ").appendAttr("value", objOption.getValue())
                 .append(blnSelected?" SELECTED>":">")
                 .append(objOption.getLabelAsString())
                 .append("</option>").appendNL();
            }
            
            s.appendNL("</select>");
            
            /**
             * Handle any enhancer refs
             */
            processFieldRef(penmFormType, pobjBO, pobjAttr);
            
            /**
             * DGS28JUN2004: Handle an enhancer post label
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit) || penmFormType.equals(zXType.webFormType.wftGrid)) {
                if (StringUtil.len(pstrPostLabel) > 0) {
                    if (StringUtil.len(pstrPostLabelClass) == 0) {
                         pstrPostLabelClass = "zXFormLabel";
                    }
                    s.append("<span ").appendAttr("class", pstrPostLabelClass).appendNL('>');
                    s.append(pstrPostLabel).append(HTMLGen.NL);
                    s.append("</span>").append(HTMLGen.NL);
                }
            }
            
            /**
             * DGS28JAN2004: If in a search form we need a span here, although there's never
             * anything in it for this kind of control. Otherwise the onChange JS doesn't work.
             */
            if (penmFormType.equals(zXType.webFormType.wftSearch)) {
                s.append("<span ");
                s.appendAttr("id", "zXSpan" + strControlName);
                s.appendAttr("style", "display:none");
                s.append("/>").append(HTMLGen.NL);
            }
            
            if (!getEditEnhancerBoolean(pobjBO, pobjAttr, "mergeWithNext")) {
                s.append("</td>").append(HTMLGen.NL);
            }
            
            return formEntrySelectControl;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for a option list.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
                getZx().log.error("Parameter : pstrOnBlur = "+ pstrOnBlur);
                getZx().log.error("Parameter : pstrOnChange = "+ pstrOnChange);
                getZx().log.error("Parameter : pstrOnKeyPress = "+ pstrOnKeyPress);
                getZx().log.error("Parameter : pblnDisabled = "+ pblnDisabled);
                getZx().log.error("Parameter : pstrTabIndex = "+ pstrTabIndex);
                getZx().log.error("Parameter : pstrOnFocus = "+ pstrOnFocus);
                getZx().log.error("Parameter : pstrOnKeyDown = "+ pstrOnKeyDown);
                getZx().log.error("Parameter : pstrOnKeyUp = "+ pstrOnKeyUp);
                getZx().log.error("Parameter : pstrOnClick = "+ pstrOnClick);
                getZx().log.error("Parameter : pstrOnMouseOver = "+ pstrOnMouseOver);
                getZx().log.error("Parameter : pstrOnMouseOut = "+ pstrOnMouseOut);
                getZx().log.error("Parameter : pstrOnMouseDown = "+ pstrOnMouseDown);
                getZx().log.error("Parameter : pstrOnMouseUp = "+ pstrOnMouseUp);
                getZx().log.error("Parameter : pstrPostLabel = "+ pstrPostLabel);
                getZx().log.error("Parameter : pstrPostLabelClass = "+ pstrPostLabelClass);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            formEntrySelectControl = zXType.rc.rcError;
            return formEntrySelectControl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntrySelectControl);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Process a submitted search form and generate where clause for query based
     * on it.
     * 
     * Reviewed for 1.5:1
     * 
     * @param pobjRequest The http specific request.
     * @param pobjBO The business object the form is linked to.
     * @param pstrGroup The attribute group used.
     * @return Returns the generated where clause.
     * @throws ZXException Thrown if processSearchForm fails.
     */
    public String processSearchForm(HttpServletRequest pobjRequest, 
    								ZXBO pobjBO, 
    								String pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        StringBuffer processSearchForm = new StringBuffer();
        
        try {
            DSHandler objDSHandler = pobjBO.getDS();
            
            /**
             * Does ds handler support search?
             */
            if (StringUtil.len(objDSHandler.getSearchGroup()) == 0) {
                throw new ZXException("Datasource handler has no search group support",objDSHandler.getName());
            }

            AttributeCollection colSearchGroup = pobjBO.getDescriptor().getGroup(objDSHandler.getSearchGroup());
            if (colSearchGroup == null) {
                throw new ZXException("Could not resolve data-source handler search group", objDSHandler.getSearchGroup());
            }

            /**
             * Do a pre-persist to allow user to do some special handling
             */
            if (pobjBO.prePersist(zXType.persistAction.paProcessSearchForm, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of pre-persist");
            }

            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) {
                throw new Exception("Unable to retrieve group");
            }

            /**
             * If there are no bytes in the request (i.e. we are not dealing
             * with a submitted form) do not bother; this can happen if a query
             * action (in a pageflow) has the searchForm tick box checked but is
             * not always called to process a submitted search form
             */
            if (pobjRequest.getParameterNames().hasMoreElements()) {
                Attribute objAttr;
                String strTmp;

                Iterator iter = colAttr.iterator();
                while (iter.hasNext()) {
                    objAttr = (Attribute) iter.next();
                    
                    if (colSearchGroup.inGroup(objAttr.getName())) {
                        strTmp = processSearchFormEntry(pobjRequest, pobjBO, objAttr);
                        
                        if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                            if (StringUtil.len(strTmp) > 0) {
                                if (processSearchForm.length() > 0) {
                                    processSearchForm.append(" & ").append(strTmp);
                                } else {
                                    processSearchForm.append(strTmp);
                                }
                            }
                            
                        } else {
                            if (StringUtil.len(strTmp) > 0) {
                                if (processSearchForm.length() > 0) {
                                    processSearchForm.append(" AND ").append(strTmp);
                                } else {
                                    processSearchForm.append(strTmp);
                                }
                            }
                            
                        } // Channler or RDBMS
                        
                    } // Attr in search group?
                    
                } // Loop over attributes

                /**
                 * If anything has been generated, wrap the whole thing in
                 * parenthesis so we can safely add this expression to any query
                 */
                if (StringUtil.len(processSearchForm) > 0) {
                    processSearchForm.insert(0, "(");
                    processSearchForm.append(")");
                }
                
            } // Any bytes submitted?

            pobjBO.setLastPostPersistRC(zXType.rc.rcOK);
            if (pobjBO.postPersist(zXType.persistAction.paSearchForm, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of post-persist");
            }

            return processSearchForm.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process a submitted search form and generate where clause for query based on it.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = " + pobjRequest);
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            return processSearchForm.toString();
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processSearchForm);
                getZx().trace.exitMethod();
            }
        }
    }
     
    /**
     * Process a single entry from a search form.
     *
     * Reviewed for 1.5:1
     * 
     * @param pobjRequest The http request 
     * @param pobjBO The ZXBO object for the request. 
     * @param pobjAttr The attribute you want to get the values. 
     * @return Returns sql for a single search form entry.
     * @throws ZXException Thrown if processSearchFormEntry fails. 
     */
    public String processSearchFormEntry(HttpServletRequest pobjRequest, 
    									 ZXBO pobjBO, 
    									 Attribute pobjAttr) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
        }

        String processSearchFormEntry = ""; 
        
        try {
            DSHandler objDSHandler = pobjBO.getDS();
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            String strControlName = controlName(pobjBO, pobjAttr);
            int dataType = pobjAttr.getDataType().pos;
            
            /**
             * Translate string operator code into enumerated value
             * DGS28JAN2004: Now support 'is null' and 'not null' operators. These are mostly
             * handled like equals/not equals except when loading the value to compare.
             */
            String strOps = pobjRequest.getParameter("zXOps" + strControlName);
            if (strOps == null) {
            	strOps = "eq";
            }
            boolean blnNullOp = (strOps.equalsIgnoreCase("isNull") || strOps.equalsIgnoreCase("notNull"));
            
            /**
             * This is a very special entry only used for booleans; it indicates that
             *  the user does not want to include the boolean in the search
             */
            if (strOps.equalsIgnoreCase("-")) {
                return processSearchFormEntry;
            } else if (strOps.equalsIgnoreCase("isNull")) {
                strOps = "eq";
            } else if (strOps.equalsIgnoreCase("notNull")) {
                strOps = "ne";
            }
            zXType.compareOperand enmOps = zXType.compareOperand.getEnum(strOps);
            
            /**
             * Get the value - If it is null, which could be a bug in the previous form make the value a empty string.
             */
            String strValue = pobjRequest.getParameter(strControlName);
            if (strValue == null) {
                strValue = "";
            }
            
            /**
             * Handle browser thing, if class is set to upper,
             * all data is displayed in uppercase but not actually submitted in
             * upper case....
             */
            if (dataType == zXType.dataType.dtString.pos) {
                if (pobjAttr.getTextCase().equals(zXType.textCase.tcLower)) {
                    strValue = strValue.toLowerCase();
                } else if (pobjAttr.getTextCase().equals(zXType.textCase.tcUpper)){
                    strValue = strValue.toUpperCase();
                }
            }
            
            /**
             * Take into consideration the (none) option...
             */
            if (pobjAttr.getOptions().size() > 0 || StringUtil.len(pobjAttr.getForeignKey())> 0) {
                if (strValue.equals("-")) {
                    strValue = "";
                }
            }
            
            /**
             * Handle booleans correctly
             */
            if (dataType == zXType.dataType.dtBoolean.pos) {
                if (StringUtil.len(strValue) > 0) {
                    strValue = "True";
                } else {
                    strValue = "False";
                }
            }
            
            /**
             * Check if its worth going further
             *  DGS28JAN2004: For 'is null' and 'not null' operators we must not do this test,
             *  as of course the value will be empty
             */
            if (!blnNullOp) {
                if (usesNormalControl(pobjBO, pobjAttr)) {
                    if (StringUtil.len(strValue) == 0) {
                        return processSearchFormEntry;
                    }
                } else {
                    if (StringUtil.len(strValue) == 0) {
                        return processSearchFormEntry;
                    }
                }
            }
            
            /**
             * And the upper value (if applicable)
             */
            String strUpperValue = "";
            if(usesNormalControl(pobjBO, pobjAttr)) {
                strUpperValue = pobjRequest.getParameter("zXUL" + strControlName);
                
                /**
                 * Handle browser thing, if class is set to upper,
                 * all data is displayed in uppercase but not actually submitted in
                 * upper case....
                 */
                if (dataType == zXType.dataType.dtString.pos) {
                    if (pobjAttr.getTextCase().equals(zXType.textCase.tcLower)) {
                        strUpperValue = strUpperValue.toLowerCase();
                    } else if (pobjAttr.getTextCase().equals(zXType.textCase.tcUpper)){
                        strUpperValue = strUpperValue.toUpperCase();
                    }  
                }
            }
            
            /**
             * DGS28JAN2004: When calling singleWhereCondition in all the instances below, if the
             * search operator is 'is null' or 'not null', explicitly set the .isNull property of
             * the zx property that is passed in and make sure the value has something that avoids
             * an error in the cstr, cdbl etc. Don't need to do any of this if on the side of the 'if'
             * statement where the operator is coBTWN as that means it cannot be a 'null' type
             * operator anyway.
             */
            if (dataType == zXType.dataType.dtAutomatic.pos || dataType == zXType.dataType.dtLong.pos) {
                if (enmOps.equals(zXType.compareOperand.coBTWN)) {
                    // Between Search :
                    if (StringUtil.isNumeric(strValue)) {
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              zXType.compareOperand.coGE,
                                                              new LongProperty(Long.parseLong(strValue), false),
                                                              zXType.dsWhereConditionOperator.dswcoNone);
                    }
                    
                    if (StringUtil.isNumeric(strUpperValue)) {
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              zXType.compareOperand.coLE,
                                                              new LongProperty(Long.parseLong(strUpperValue), false), 
                                                              zXType.dsWhereConditionOperator.dswcoAnd); // Add to AND statement
                    }
                    
                } else {
                    if (blnNullOp) strValue = "0";
                    if (StringUtil.isNumeric(strValue)) {
                         objDSWhereClause.singleWhereCondition(pobjBO, 
                                                               pobjAttr, 
                                                               enmOps,
                                                               new LongProperty(Long.parseLong(strValue), blnNullOp),
                                                               zXType.dsWhereConditionOperator.dswcoNone); 
                    } else {
                        return processSearchFormEntry;
                    }
                    
                }
            } else if (dataType == zXType.dataType.dtBoolean.pos) {
                if (StringUtil.booleanValue(strValue)) {
                    objDSWhereClause.singleWhereCondition(pobjBO, pobjAttr, enmOps, new BooleanProperty(true),
                                                          zXType.dsWhereConditionOperator.dswcoNone);
                } else {
                    objDSWhereClause.singleWhereCondition(pobjBO, pobjAttr, enmOps, new BooleanProperty(false),
                                                          zXType.dsWhereConditionOperator.dswcoNone);
                }
                
            } else if (dataType == zXType.dataType.dtDate.pos || dataType == zXType.dataType.dtTime.pos) {
            	/**
            	 * Try date Format first and then time.
            	 * As the date format might contain a time component, but time is only includes the time.
            	 */
            	DateFormat[] arrDateFormats = {getZx().getDateFormat(), getZx().getTimeFormat()};
            	
                /**
                 * DGS 14DEC2002: Converted to use zx versions of IsDate and CDate to support DDMMMYYYY
                 */
                if (enmOps.equals(zXType.compareOperand.coBTWN)) {
                    // Between Search :
                    if (DateUtil.isValid(arrDateFormats, strValue)) {
                        Date date = DateUtil.parse(arrDateFormats, strValue);
                        
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              zXType.compareOperand.coGE,
                                                              new DateProperty(date),
                                                              zXType.dsWhereConditionOperator.dswcoNone);
                    }
                    
                    if (DateUtil.isValid(arrDateFormats, strUpperValue)) {
                    	Date date = DateUtil.parse(arrDateFormats, strUpperValue);
                    	
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              zXType.compareOperand.coLE,
                                                              new DateProperty(date),
                                                              zXType.dsWhereConditionOperator.dswcoAnd); // Add to AND statement
                    }
                    
                } else {
                	//----
                    // Normal Search
                	//----
                    if (blnNullOp) {
                        /** Generate any date, we are not going to actually use it.**/
                        Date date = new  Date();
                        DateFormat df = getZx().getDateFormat();
                        strValue = df.format(date);
                    }
                    
                    if (DateUtil.isValid(arrDateFormats, strValue)) {
                        Date date = DateUtil.parse(arrDateFormats, strValue);
                        
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              enmOps, new DateProperty(date, blnNullOp),
                                                              zXType.dsWhereConditionOperator.dswcoNone);
                    } else {
                        return processSearchFormEntry;
                    }
                    
                }
                
            } else if (dataType == zXType.dataType.dtTimestamp.pos) {
                /**
                 * Different behaviour for timestamps; it is not very likely that
                 * the user has entered a time part for the date
                 * so we have to construct the most logical one given the search
                 * operand:
                 * > - 23:59:59
                 * >= - 00:00:01
                 * < - 00:00:01
                 * <= - 23:59:59
                 * 
                 * This also presumes that the dates from web forms do not include the time.
                 */
                if (enmOps.equals(zXType.compareOperand.coBTWN)) {
                    // Between Search :
                    if (DateUtil.isValid(getZx().getTimestampFormat(), strValue)) {
                    	Date date = DateUtil.parse(getZx().getTimestampFormat(), strValue + " 00:00:01");
                        
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              zXType.compareOperand.coGE, 
                                                              new DateProperty(date),
                                                              zXType.dsWhereConditionOperator.dswcoNone);
                    }
                    
                    if (DateUtil.isValid(getZx().getTimestampFormat(), strUpperValue)) {
                    	Date date = DateUtil.parse(getZx().getTimestampFormat(), strUpperValue + " 23:59:59");
                        
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              zXType.compareOperand.coGE, 
                                                              new DateProperty(date),
                                                              zXType.dsWhereConditionOperator.dswcoAnd); // Add to AND statement
                    }
                    
                } else {
                    // Normal Search :
                    String strTime = "";
                    if (enmOps.equals(zXType.compareOperand.coLE)) {
                        strTime = " 23:59:59";
                    } else if (enmOps.equals(zXType.compareOperand.coLT)) {
                        strTime = " 00:00:01";
                    } else if (enmOps.equals(zXType.compareOperand.coGE)) {
                        strTime = " 00:00:01";
                    } else if (enmOps.equals(zXType.compareOperand.coGT)) {
                        strTime = " 23:59:59";
                    }
                    
                    if (blnNullOp) {
                        /** Generate any date **/
                        Date date = new  Date();
                        strValue = getZx().getDateFormat().format(date);
                    }
                    
                    if (DateUtil.isValid(getZx().getTimestampFormat(), strValue)) {
                    	Date date = DateUtil.parse(getZx().getTimestampFormat(), strValue + strTime);
                        
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              enmOps, 
                                                              new DateProperty(date, blnNullOp),
                                                              zXType.dsWhereConditionOperator.dswcoNone);
                    } else {
                        return processSearchFormEntry;
                    }
                    
                }
                
            } else if (dataType == zXType.dataType.dtDouble.pos) {
                if (enmOps.equals(zXType.compareOperand.coBTWN)) {
                    // Between Search :
                    if (StringUtil.isDouble(strValue)) {
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              zXType.compareOperand.coGE,
                                                              new DoubleProperty(Double.parseDouble(strValue), false),
                                                              zXType.dsWhereConditionOperator.dswcoNone);
                    }
                    
                    if (StringUtil.isDouble(strUpperValue)) {
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr,
                                                              zXType.compareOperand.coLE,
                                                              new DoubleProperty(Double.parseDouble(strUpperValue), false),
                                                              zXType.dsWhereConditionOperator.dswcoAnd); // Add to AND statement
                    }
                    
                } else {
                    // Normal Search :
                    if (blnNullOp) strValue = "0";
                    if (StringUtil.isDouble(strValue)) {
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              enmOps,
                                                              new DoubleProperty(Double.parseDouble(strValue), blnNullOp),
                                                              zXType.dsWhereConditionOperator.dswcoNone);
                        
                    } else {
                        return processSearchFormEntry;
                    }
                    
                }
                
            } else if (dataType == zXType.dataType.dtString.pos || dataType == zXType.dataType.dtExpression.pos) {
                if (enmOps.equals(zXType.compareOperand.coBTWN)) {
                    // Between Search :
                    if (StringUtil.len(strValue) > 0) {
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                                              pobjAttr, 
                                                              zXType.compareOperand.coGE,
                                                              new StringProperty(strValue),
                                                              zXType.dsWhereConditionOperator.dswcoNone);
                    }
                    
                    if (StringUtil.len(strUpperValue) > 0) {
                        objDSWhereClause.singleWhereCondition(pobjBO, 
                                pobjAttr, 
                                zXType.compareOperand.coLE,
                                new StringProperty(strUpperValue),
                                zXType.dsWhereConditionOperator.dswcoAnd); // Add to AND statement
                    }
                    
                } else {
                    // Normal Search : 
                    objDSWhereClause.singleWhereCondition(pobjBO, 
                                                          pobjAttr, 
                                                          enmOps,
                                                          new StringProperty(strValue, blnNullOp),
                                                          zXType.dsWhereConditionOperator.dswcoNone);
                }
                
            }
            
            /**
             * Get either as SQL or as where clause
             */
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                processSearchFormEntry = objDSWhereClause.getAsWhereClause();
            } else {
                processSearchFormEntry = objDSWhereClause.getAsSQL();
            }
            
            return processSearchFormEntry;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process a single entry from a search form.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
            }
            if (getZx().throwException) throw new ZXException(e);
            return processSearchFormEntry;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processSearchFormEntry);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Read submitted order by fields and turn into SQL order by clause.
     * 
     * <pre>
     * 
     * Change    : DGS21JUL2004
     * Why       : In processOrderBy, accept optional extra parameter of an attribute collection,
     * and if present, use it to only process the order by if the attribute is not
     * already in that collection (and add it to the collection if not already in)
     * 
     * Reviewed for V1.5:1
     * </pre>
     * 
     * @param pobjRequest The request from the previous form.
     * @param pobjBO The business object for the form. 
     * @param pstrGroup The attribute group. 
     * @param pcolAttr A collection of attributes already in the order by.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if processOrderBy fails. 
     */
    public String processOrderBy(HttpServletRequest pobjRequest, 
    							 ZXBO pobjBO, 
    							 String pstrGroup, 
    							 AttributeCollection pcolAttr) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pcolAttr", pcolAttr);
        }
        
        String processOrderBy = ""; 
        
        try {
            DSHandler objDSHandler = pobjBO.getDS();
            
            
            /**
             * Get order by attribute
             */
            String controlName = controlName(pobjBO);
            String strAttr = pobjRequest.getParameter(controlName+ pstrGroup + "OrderBy");
            
            /**
             * No entry selected
             * 
             * "-" - Is a special value in the drop down list meaning nothing has been selected. We can safely assume this
             * as we generate the Order By list.
             */
            if (StringUtil.len(strAttr)== 0 || strAttr.equals("-")) {
                return processOrderBy;
            }
            
            /**
             * If we have been given a collection of attributes, only use the current one if 
             * it isn't already in the collection
             */
            if (pcolAttr != null) {
                if (pcolAttr.inGroup(strAttr)) {
                    return processOrderBy;
                }
                
                pcolAttr.put(strAttr, pobjBO.getDescriptor().getAttribute(strAttr));
            }
            
            /**
             * Check reverse
             */
            String strTmp = pobjRequest.getParameter(controlName + pstrGroup + "Reverse");
            
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                if (StringUtil.len(strTmp) > 0) {
                    processOrderBy = "-" + strAttr;
                } else {
                    processOrderBy = strAttr;
                }
                
            } else {
                // We are only interested in the columns we do not want to add the ORDER BY clause as we may have multiple entities :
                processOrderBy = getZx().getSql().orderByClause(pobjBO, strAttr, (StringUtil.len(strTmp) > 0), true);
                
            }
            
            return processOrderBy;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Read submitted order by fields and turn into SQL order by clause.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = " + pobjRequest);
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pcolAttr = " + pcolAttr);
            }
            if (getZx().throwException) throw new ZXException(e);
            return processOrderBy;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processOrderBy);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Read submitted processSavedQuery field and turn into SQL where clause.
     *
     *<pre>
     *
     * The pstrQry and pstrOrdrBy string buffers are used by the callee.
     *</pre>
     *
     * @param pstrPageflowName The name of the pageflow in which the search form belongs.
     * @param pobjRequest The http request. 
     * @param pstrEntity The entity to save the query to. 
     * @param pstrQry The SQL query save. 
     * @param pstrOrdrBy The order by part of the clause 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if processSavedQuery fails. 
     */
    public zXType.rc processSavedQuery(String pstrPageflowName,
    								   HttpServletRequest pobjRequest, 
    								   String pstrEntity, 
    								   StringBuffer pstrQry, 
    								   StringBuffer pstrOrdrBy) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pstrEntity", pstrEntity);
            getZx().trace.traceParam("pstrQry", pstrQry);
            getZx().trace.traceParam("pstrOrdrBy", pstrOrdrBy);
        }

        zXType.rc processSavedQuery = zXType.rc.rcOK; 
        
        try {
            String strSavedQryNme = pobjRequest.getParameter("zXSavedQuery");
            /**
             * If a new saved name entered, we will (later) save the current query, and therefore
             * do not want to retrieve a selected one.
             * Nor if the searchform didn't allow saved searches and therefore the returned id is blank.
             * Nor do we retrieve if no existing entry selected.
             * Return Code of Warning means no existing saved name to be used.
             */
            if (StringUtil.len(strSavedQryNme) == 0) {
                processSavedQuery = zXType.rc.rcWarning;
                return processSavedQuery;
            }
            
            ZXBO objZXQryBO = getZx().createBO("zXQry");
            objZXQryBO.setValue("nme", strSavedQryNme);
            objZXQryBO.setValue("usrPrf", getZx().getUserProfile().getValue("id"));
            objZXQryBO.setValue("pf", pstrPageflowName);
            objZXQryBO.setValue("entity", pstrEntity);
            
            String strCompoundKey = "nme,usrPrf,pf,entity";
            if (objZXQryBO.doesExist(strCompoundKey)) {
                objZXQryBO.loadBO("qry,ordrBy", strCompoundKey, false);
            } else {
                processSavedQuery = zXType.rc.rcWarning;
                return processSavedQuery;
            }
            
            /**
             * Uses the last query.
             */
            pstrQry.append(objZXQryBO.getValue("qry").getStringValue());
            pstrOrdrBy.append(objZXQryBO.getValue("ordrBy").getStringValue());
            
            return processSavedQuery;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Read submitted processSavedQuery field and turn into SQL where clause.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
                getZx().log.error("Parameter : pstrEntity = "+ pstrEntity);
                getZx().log.error("Parameter : pstrQry = "+ pstrQry);
                getZx().log.error("Parameter : pstrOrdrBy = "+ pstrOrdrBy);
            }
            if (getZx().throwException) throw new ZXException(e);
            processSavedQuery = zXType.rc.rcError;
            return processSavedQuery;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processSavedQuery);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Read submitted processSavedQuery field and turn into SQL where clause.
     * 
     * @param pstrPageflowName The name of the pageflow in which the search form belongs. 
     * @param pobjRequest The HTTP Servlet request. 
     * @param pstrEntity The Business object name 
     * @param pstrWhereClause The whereClause. 
     * @param pstrOrderByClause The orderByClause 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if persistSavedQuery fails. 
     */
    public zXType.rc persistSavedQuery(String pstrPageflowName, 
    								   HttpServletRequest pobjRequest, 
    								   String pstrEntity, 
    								   StringBuffer pstrWhereClause, 
    								   StringBuffer pstrOrderByClause) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrRequest", pobjRequest);
            getZx().trace.traceParam("pstrEntity", pstrEntity);
            getZx().trace.traceParam("pobjWhereClause", pstrWhereClause);
            getZx().trace.traceParam("pobjOrderByClause", pstrOrderByClause);
        }
        
        zXType.rc persistSavedQuery = zXType.rc.rcOK; 
        
        try {
            String strSavedQryNme = pobjRequest.getParameter("zXSavedQuery");
            
            /**
             * If name entered, save the current query. Might already exist.
             */
            if (StringUtil.len(strSavedQryNme) > 0) {
                ZXBO objZXQryBO = getZx().createBO("zXQry");
                objZXQryBO.setValue("nme", strSavedQryNme);
                objZXQryBO.setValue("usrPrf", getZx().getUserProfile().getValue("id"));
                objZXQryBO.setValue("pf", pstrPageflowName);
                objZXQryBO.setValue("entity", pstrEntity);
                objZXQryBO.setValue("qry", pstrWhereClause.toString());
                objZXQryBO.setValue("ordrBy", pstrOrderByClause.toString());
                
                String strCompoundKey = "nme,usrPrf,pf,entity";
                if (objZXQryBO.doesExist(strCompoundKey)) {
                    
                    if (objZXQryBO.loadBO("id", strCompoundKey, false).pos != zXType.rc.rcOK.pos) {
                        throw new ZXException(); //GoTo errExit
                    }
                    if (objZXQryBO.updateBO("qry,ordrBy").pos != zXType.rc.rcOK.pos) {
                        throw new ZXException(); //GoTo errExit
                    }
                    
                    this.infoMsg("Updated " + objZXQryBO.getDescriptor().getLabel().getLabel() + " '" + strSavedQryNme + "' ");
                    
                } else {
                    objZXQryBO.setAutomatics("+");
                    
                    if (objZXQryBO.insertBO().pos != zXType.rc.rcOK.pos) {
                        throw new ZXException(); //GoTo errExit
                    }
                    
                    this.infoMsg("Inserted " + objZXQryBO.getDescriptor().getLabel().getLabel() + " '" + strSavedQryNme + "' ");                    
                }
            }
            
            return persistSavedQuery;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Read submitted processSavedQuery field and turn into SQL where clause.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrRequest = "+ pobjRequest);
                getZx().log.error("Parameter : pstrEntity = "+ pstrEntity);
                getZx().log.error("Parameter : pobjWhereClause = "+ pstrWhereClause);
                getZx().log.error("Parameter : pobjOrderByClause = "+ pstrOrderByClause);
            }
            if (getZx().throwException) throw new ZXException(e);
            persistSavedQuery = zXType.rc.rcError;
            return persistSavedQuery;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(persistSavedQuery);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Generate HTML for list header open bit.
     *
     *<pre>
     *
     *Assumes   :
     *   Used in combination with
     *    BOListHeader and closeListHeader
     *    Table already been opened
     *</pre>
     *
     * @return Returns the return code of the method. 
     */
    public zXType.rc listHeaderOpen() {
        return listHeaderOpen(true, null);
    }

    /**
     * Generate HTML for list header open bit.
     *
     *<pre>
     *
     *Assumes   :
     *   Used in combination with
     *    BOListHeader and closeListHeader
     *    Table already been opened
     *</pre>
     *
     * @param pblnSelectColumn Include select column? Optional, default should be true. 
     * @param pstrLabel Label for select button column. Optional, default should be "" 
     * @return Returns the return code of the method. 
     */
    public zXType.rc listHeaderOpen(boolean pblnSelectColumn, String pstrLabel) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pblnSelectColumn", pblnSelectColumn);
            getZx().trace.traceParam("pstrLabel", pstrLabel);
        }
        
        zXType.rc listHeaderOpen = zXType.rc.rcOK;
        
        /**
         * Handle defaults
         */
        if (pstrLabel == null) {
            pstrLabel = "Select";
        }
        
        try {
        	s.appendNL("<theader>");
            s.appendNL("<tr>");
            
            /**
             * Optionally create column for select button
             */
            if (pblnSelectColumn) {
                s.append("<td ")
                 .appendAttr("width", getWebSettings().getListFormColumn1())
                 .appendAttr("class", "zxListHeader").appendNL('>');
                
                s.appendNL(pstrLabel);
                
                s.appendNL("</td>");
            }
            
            return listHeaderOpen;
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(listHeaderOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for list header BO columns bit.
     * 
     * <pre>
     * 
     * Assumes   :
     *   Used in combination with
     *    ListHeader and ListHeaderOpen
     *    
     * - Reviewed for V1.5:1
     * - Fixed bug for V1.5:19
     * - Reviewed for V1.5:20
     * - Reviewed for V1.5:21
     * </pre>
     * 
     * @param pobjBO The Business object of the column header. 
     * @param pstrGroup The attribute group used for the header 
     * @param pstrSortURLBase The base url for the resorting url. Optional, default is null 
     * @param pblnResolveFK Whether to resolve the foriegn keys. Optional, default is true. 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if listHeader fails. 
     */
    public zXType.rc listHeader(ZXBO pobjBO, 
                                String pstrGroup, 
                                String pstrSortURLBase, 
                                boolean pblnResolveFK) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrSortURLBase", pstrSortURLBase);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
        }

        zXType.rc listHeader = zXType.rc.rcOK;
        
        // Attribute to use for sorting
        Attribute objSortAttr;
        
        try {
            /**
             * Get DS handler and order by attribute group
             */
            DSHandler objDSHandler = pobjBO.getDS();
            
            zXType.databaseType enmDBType = null;
            if (objDSHandler instanceof DSHRdbms) {
                enmDBType = ((DSHRdbms)objDSHandler).getDbType();
            }
            
            AttributeCollection colOrderByGroup = pobjBO.getDescriptor().getGroup(objDSHandler.getOrderGroup());
            if (colOrderByGroup == null) {
                return listHeader;
            }
            
            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) {
                throw new Exception("Unable to get group");
            }
            
            Attribute objAttr;
            String strColumnName = "";
            ZXBO objFKBO; 
            Descriptor objFKDesc;
            AttributeCollection colFKAttr;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                s.append("<td ").appendAttr("class", "zxListHeader").appendNL('>');
                s.append(objAttr.getLabel().getLabel()).append(HTMLGen.NL);
                
                /**
                 * Optionally, we may have to add the up / down sort columns; sorting is only
                 * relevant when sorting is allowed and the order-by-group has any attributes
                 */
                if (StringUtil.len(pstrSortURLBase) > 0
                    && (objDSHandler.getOrderSupport().pos != zXType.dsOrderSupport.dsosNone.pos 
                    && colOrderByGroup.size() > 0)) {
                    /**
                     * Reset variable for each iteration
                     */
                	strColumnName = "";
                	
                	/**
                	 * The attribute to use for sorting may be different from the actual
                	 * attribute. This can be the case for dynamic values where, for example,
                	 * you translate the status into a nice image but want to allow the user
                	 * to sort by status
                	 */
                	if (StringUtil.len(objAttr.getSortAttr()) > 0) {
                		objSortAttr = pobjBO.getDescriptor().getAttribute(objAttr.getSortAttr());
                		if (objSortAttr == null) {
                			throw new ZXException("Unable to find sort attribute", objAttr.getSortAttr());
                		}
                		
                	} else {
                		objSortAttr = objAttr;
                		
                	} // Has sortAttr override
                	
                    /**
                     * Different for ordinary and foreign key:
                     * - Ordinary: Simply order by column name
                     * - Foreign key: Order by first attr.column of FK label group (if resolveFK set)
                     * 
                     * Also, special treatment for channels
                     * 
                     * Order by is not relevant when the column is not in the order by group
                     */
                    if (colOrderByGroup.inGroup(objSortAttr.getName())) {
                        /**
                         * Different behaviour for FKs and simple attributes: for FKs we want to sort on the
                         * first attribute of the label rather than the simple attribute. This is obviously not
                         * supported for channel data-sources
                         */
                        if(StringUtil.len(objSortAttr.getForeignKey()) ==  0 
                           || !pblnResolveFK
                           || objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                        	/**
                             * For a channel we simply include the attribute name, for RDBMS the column name
                             */
                            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                                strColumnName = objSortAttr.getName();
                                
                            } else {
                                strColumnName = getZx().getSql().columnName(pobjBO, objSortAttr, zXType.sqlObjectName.sonName);
                                
                                /**
                                 * Deal with case-insensitive sort
                                 */
                                if (objSortAttr.getTextCase().equals(zXType.textCase.tcInsensitive) 
                                	&& objSortAttr.getDataType().pos  == zXType.dataType.dtString.pos) {
                                    strColumnName = getZx().getSql().makeCaseInsensitive(strColumnName, enmDBType);
                                }
                            }
                            
                        } else {
                            /**
                             * Has FK so user wants to sort on label of FK (for RDBMS)
                             */
                            objFKBO = pobjBO.getValue(objSortAttr.getName()).getFKBO();
                            
                            if (objFKBO == null) {
                                strColumnName = "";
                                
                            } else if (getZx().getDataSources().canDoDBJoin(pobjBO, objFKBO)){
                            	/**
                            	 * We can do a join between these to entities.
                            	 */
                                objFKDesc = objFKBO.getDescriptor();
                                
                                if (StringUtil.len(objSortAttr.getFkLabelGroup()) == 0) {
                                	colFKAttr = objFKDesc.getGroup("label");
                                } else {
                                	colFKAttr = objFKDesc.getGroup(objSortAttr.getFkLabelGroup());
                                }
                                
                                if (colFKAttr == null) {
                                    strColumnName = "";
                                    
                                } else {
                                    /**
                                     * Check that first attr is associated with a column
                                     */
                                    Attribute objAttr1 = (Attribute)colFKAttr.iterator().next();
                                    if (StringUtil.len(objAttr1.getColumn()) > 0){
                                        strColumnName = getZx().getSql().columnName(objFKBO, objAttr1, zXType.sqlObjectName.sonName);
                                        
                                        /**
                                         * Deal with case-insensitive sort
                                         */
                                        if (objAttr1.getTextCase().equals(zXType.textCase.tcInsensitive) && objAttr1.getDataType().pos  == zXType.dataType.dtString.pos) {
                                            strColumnName = getZx().getSql().makeCaseInsensitive(strColumnName, enmDBType);
                                        }
                                        
                                    } else {
                                        strColumnName = "";
                                        
                                    } // First attr is associated with column
                                    
                                } // Has label attribute group
                                
                            } else {
                            	/**
                            	 * We can't do a join so we want to use the column
                            	 */
                            	
                            	/**
                            	 * ?????? 
                            	 * objFKBO.getDB().getOrderSupport().pos != zXType.dsOrderSupport.dsosNone.pos 
                            	 */
                            	
                                strColumnName = getZx().getSql().columnName(pobjBO, objSortAttr, zXType.sqlObjectName.sonName);
                                
                                /**
                                 * Deal with case-insensitive sort
                                 */
                                if (objSortAttr.getTextCase().equals(zXType.textCase.tcInsensitive) 
                                	&& objSortAttr.getDataType().pos  == zXType.dataType.dtString.pos) {
                                    strColumnName = getZx().getSql().makeCaseInsensitive(strColumnName, enmDBType);
                                }
                                
                            } // Has FKBO
                            
                        } // FK and resolveFK
                        
                    } // Attribute in colOrderByGroup?
                    
                    
                    if (StringUtil.len(strColumnName) > 0) {
                        if (objDSHandler.getOrderSupport().pos > zXType.dsOrderSupport.dsosSimple.pos) {
                            /**
                             * First determine the postFix; if this routine is called
                             *  from a pageflow it may require the resort entries added
                             *  to the querystring; these should be post-fixed with the
                             *  pageflow / action name as this makes it possible
                             *  to have multiple list forms on a single page
                             */
                            String strPostFix = "";
                            if (this.pageflow != null) {
                                strPostFix = this.pageflow.QSSortKeyPostFix();
                            }
                            
                            /**
                             * Down button.
                             * this is NOT available if the orderSupport is simple
                             */
                            String strUrl = "zXListReSort('" + pstrSortURLBase  + "&-oa" + strPostFix + "=" + strColumnName 
                            + "&-od" + strPostFix + "=asc');";
                            
                            s.append("&nbsp;");
                            
                            s.append("<img ")
                             .appendAttr("src", "../images/sortDown.gif")
                             .appendAttr("onMouseDown", strUrl)
                             .appendAttr("onMouseOver", "javascript:this.src='../images/sortDownOver.gif'")
                             .appendAttr("onMouseOut", "javascript:this.src='../images/sortDown.gif'")
                             .appendAttr("title", "Sort by " + objAttr.getLabel().getLabel())
                             .appendNL('>');
                            
                            /**
                             * Up button.
                             * this is NOT available if the orderSupport is simple
                             */
                        	strUrl = "zXListReSort('"  + pstrSortURLBase 
                                     + "&-oa" + strPostFix + "=" + strColumnName 
                                     + "&-od" + strPostFix + "=desc');";
                            
                            s.append("<img ")
                             .appendAttr("src", "../images/sortUp.gif")
                             .appendAttr("onMouseDown", strUrl)
                             .appendAttr("onMouseOver", "javascript:this.src='../images/sortUpOver.gif'")
                             .appendAttr("onMouseOut", "javascript:this.src='../images/sortUp.gif'")
                             .appendAttr("title", "Sort by " + objAttr.getLabel().getLabel())
                             .appendNL('>');
                            
                        } // Order by supports both ASC and DESC
                        
                    } // Has 'columns name' (ie something to sort by on this column)
                    
                } // Wants sorting
                
                s.append("</td>").append(HTMLGen.NL);
                
            } // Loop over attributes
            
            return listHeader;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for list header BO columns bit.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
                getZx().log.error("Parameter : pstrSortURLBase = "+ pstrSortURLBase);
                getZx().log.error("Parameter : pblnResolveFK = "+ pblnResolveFK);
            }
            if (getZx().throwException) throw new ZXException(e);
            listHeader = zXType.rc.rcError;
            return listHeader;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(listHeader);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for list header close bit.
     * 
     * <pre>
     * 
     * Assumes   :
     *   Used in combination with
     *   ListHeader and ListHeaderOpen
     * </pre>
     */
    public void listHeaderClose() {
    	this.s.appendNL("</tr>");
    	this.s.appendNL("</theader>");
    }
    
    /**
     * Generate HTML for list header BO columns bit.
     * 
     * <pre>
     * 
     * Assumes   :
     *   Used in combination with
     *    multiListHeader and multiListHeaderOpen
     * </pre>
     *
     * @param pobjBO The business object of the column 
     * @param pstrGroup The attribute group used. 
     * @param pstrSortURLBase The base url of the sort. Optional, default is null. 
     * @param pblnResolveFK Whether to resolve the fk or  not. Optional, default is true. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if multiListHeader fails. 
     */
    public zXType.rc multiListHeader(ZXBO pobjBO, String pstrGroup, String pstrSortURLBase, boolean pblnResolveFK) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrSortURLBase", pstrSortURLBase);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
        }

        zXType.rc multiListHeader = zXType.rc.rcOK; 
        
        try {
            
            multiListHeader = listHeader(pobjBO, pstrGroup, pstrSortURLBase, pblnResolveFK);

            return multiListHeader;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for list header BO columns bit.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
                getZx().log.error("Parameter : pstrSortURLBase = "+ pstrSortURLBase);
                getZx().log.error("Parameter : pblnResolveFK = "+ pblnResolveFK);
            }
            if (getZx().throwException) throw new ZXException(e);
            multiListHeader = zXType.rc.rcError;
            return multiListHeader;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(multiListHeader);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for list header open bit.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with
     *    BOmultiListHeader and closemultiListHeader
     *    Table already been opened
     * </pre>
     *
     * @param pblnSelectColumn Include select column? Optional, default is true. 
     * @param pstrLabel The label of the select column 
     * @return Returns the return code of the method. 
     */
    public zXType.rc multiListHeaderOpen(boolean pblnSelectColumn, String pstrLabel) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pblnSelectColumn", pblnSelectColumn);
            getZx().trace.traceParam("pstrLabel", pstrLabel);
        }

        zXType.rc multiListHeaderOpen = zXType.rc.rcOK; 
        
        try {
            /**
             * Always include the column, if not for the select button than we still need
             *  it for the checkbox
             */
            multiListHeaderOpen = listHeaderOpen(true, pstrLabel);
            
            return multiListHeaderOpen;
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(multiListHeaderOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for list header close bit.
     * 
     * <pre>
     * 
     * Assumes   :
     *   Used in combination with
     *    multiListHeader and multiListHeaderOpen
     * </pre>
     */
    public void multiListHeaderClose() {
    	listHeaderClose();
    }
    
    /**
     * Generate the HTML to open a listrow.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with listRowClose and listRow
     *    Table has already been opened
     * </pre>
     *
     * @param pblnEvenRow Used to determine the color of the row 
     * @param pstrUrl If blank, assume a listrow with no button associated with it
     * @param pstrClass The css stylesheet 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if listRowOpen fails. 
     */
    public zXType.rc listRowOpen(boolean pblnEvenRow, String pstrUrl, String pstrClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pblnEvenRow", pblnEvenRow);
            getZx().trace.traceParam("pstrUrl", pstrUrl);
            getZx().trace.traceParam("pstrClass", pstrClass);
        }

        zXType.rc listRowOpen = zXType.rc.rcOK; 
        
        try {
        	
            /**
             * Open row
             */
            if (StringUtil.len(pstrClass) == 0) {
                s.append("<tr ").appendAttr("class", pblnEvenRow?"zxNor":"zxAlt").appendNL('>');
            } else {
                s.append("<tr ").appendAttr("class", pstrClass).appendNL('>');
            }
            
            /**
             * Only add the column for select if there is a select button
             */
            if (StringUtil.len(pstrUrl) > 0) {
                /** 
                 * Include javascript code to set the cursor.
                 */
            	pstrUrl = "window.document.body.style.cursor='wait';" + pstrUrl + ";window.document.body.style.cursor='';";
                
                /**
                 * BD25MAY04 Fixed problem that can occur when using external pageflow actions
                 * it is possible that pageflow.contextaction is not set
                 */
                if (this.pageflow.getContextAction() != null) {
	                /**
	                 * DGS29APR2004: If the 'select disable' tag is set, the select button will be
	                 * disabled after being clicked on. Similarly, if the 'selected' tag is set,
	                 * the select button will change but will not be disabled 
	                 */
	                if (this.pageflow.resolveDirector(this.pageflow.getContextAction().tagValue("zXListSelectDisable")).equals("1")) {
	                    pstrUrl = pstrUrl + ";zXListSelectDisable(this);";
	                } else if (this.pageflow.resolveDirector(this.pageflow.getContextAction().tagValue("zXListSelected")).equals("1")) {
	                    pstrUrl = pstrUrl + ";zXListSelected(this);";
	                }
                }
                
                s.append("<td ") 
					  .appendAttr("width", getWebSettings().getListFormColumn1()) 
					  .appendAttr("align", "center") 
					  .appendNL(">")
					  .append("<img ") 
					  .appendAttr("src", "../images/listItem.gif")
					  
					  .appendAttr("accessKey", "l") 
					  .appendAttr("tabindex", "1") 
					  .appendAttr("onKeyPress", pstrUrl)
					  .appendAttr("onFocus", "javascript:this.src='../images/listItemOver.gif';") 
					  .appendAttr("onBlur", "javascript:this.src='../images/listItem.gif'")
					  
					  .appendAttr("onMouseDown", pstrUrl)
					  .appendAttr("onMouseOver", "javascript:this.src='../images/listItemOver.gif'") 
					  .appendAttr("onMouseOut", "javascript:this.src='../images/listItem.gif'")
					  .appendNL('>')
					  .append("</td>").appendNL();
            }
            
            return listRowOpen;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate the HTML to open a listrow.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pblnEvenRow = "+ pblnEvenRow);
                getZx().log.error("Parameter : pstrUrl = "+ pstrUrl);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
            }
            if (getZx().throwException) throw new ZXException(e);
            listRowOpen = zXType.rc.rcError;
            return listRowOpen;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(listRowOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate the HTML to a listrow.
     * 
     * <pre>
     * 
     * Reviewed for V1.5:52
     * 
     * Assumes   :
     *   Used in combination with listRowClose and listRowOpen
     * </pre>
     *
     * @param pobjBO The business object the row elements belong to. 
     * @param pstrGroup The attribute group to display. 
     * @param pintLevel The level of the column. Optional, default is 0.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if listRow fails. 
     */
    public zXType.rc listRow(ZXBO pobjBO, String pstrGroup, int pintLevel) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pintLevel", pintLevel);
        }
        
        zXType.rc listRow = zXType.rc.rcOK; 

        try {
            /**
             * Try prepersist
             */
            if (pobjBO.prePersist(zXType.persistAction.paListRow, pstrGroup).pos == zXType.rc.rcError.pos) {
                throw new ZXException("Exit on request of pre-persist");
            }
            
            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) {
                throw new Exception("Unable to get group " + pstrGroup);
            }
            
            Attribute objAttr;
            String strValue;
            String strAlign;
            int dataType;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                strValue = pobjBO.getValue(objAttr.getName()).formattedValue();
                
                dataType = objAttr.getDataType().pos;
                if (dataType == zXType.dataType.dtAutomatic.pos
                    || dataType == zXType.dataType.dtLong.pos
                    || dataType == zXType.dataType.dtDouble.pos) {
                    /**
                     * Do not right-align when the numeric value has been translated into
                     * something meaningful
                     * Expression split out for performance reasons as VB does not
                     * do expression short cutting
                     */
                    if (objAttr.getOptions().size() > 0) {
                        strAlign = "left";
                        
                    } else if (StringUtil.len(objAttr.getForeignKey()) > 0) {
                    	/**
                    	 * We use formattedValue to generate the value of the column; in case
                    	 * of a FK, this will ALWAYS resolve; if the FKLabel had already been
                    	 * set it is because the query and listForm had resolveFK switched on;
                    	 * otherwise it will resolve on a case-by-case basis. This may be OK
                    	 * bu we generate a trace message to notify of this potential
                    	 * issue
                    	 */
                    	strAlign = "left";
                    	
//                        if (StringUtil.len(pobjBO.getValue(objAttr.getName()).getFkLabel()) > 0) {
//                            strAlign = "left";
//                            
//                        } else {
//                            // Well this might note be true
//                            if (StringUtil.isNumeric(strValue)) {
//                                strAlign = "right";
//                            } else {
//                                strAlign = "left";
//                            }
//                        }
                        
                    } else {
                        strAlign = "right";
                        
                    }
                    
                } else {
                    strAlign = "left";
                    
                }
                
                /**
                 * V1.5:95 BD28APR2006 Replace CR and CRLF by <br> in multiline fields
                 */
                if (objAttr.isMultiLine()) {
                    strValue = strValue.replaceAll("\n", "<br>");
                }
                
                s.append("<td align=\"").append(strAlign).append("\">").append(strValue).append("</td>").appendNL();
                
            }
            
            /**
             * Try postpersist
             */
            pobjBO.setLastPostPersistRC(listRow);
            if (pobjBO.postPersist(zXType.persistAction.paListRow, pstrGroup).pos == zXType.rc.rcError.pos) {
            	throw new ZXException("Exit on request of post-persist");
            }
            listRow = pobjBO.getLastPostPersistRC(); 
                
            return listRow;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate the HTML to a listrow.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
                getZx().log.error("Parameter : pintLevel = "+ pintLevel);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            listRow = zXType.rc.rcError;
            return listRow;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(listRow);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate the HTML to close a listrow.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with listRow and listRowOpen
     * </pre>
     */
    public void listRowClose() {
        s.appendNL("</tr>");
    }
    
    /**
     * Generate the HTML to open a multiListRow.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with multiListRowClose and multiListRow
     *    Table has already been opened
     * </pre>
     *
     * @param pobjBO The business object used. 
     * @param pblnEvenRow Used to determine the color of the row 
     * @param pstrUrl If blank, assume a multiListRow with no button associated with it 
     * @param pblnChecked Indicates that checkbox should be checked. Optional, default is false 
     * @param pintNumCheckboxes Indicates how many checkboxes should be displayed. Optional, default is 1. 
     * @param pstrClass Optional class override 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if multiListRowOpen fails. 
     */
    public zXType.rc multiListRowOpen(ZXBO pobjBO, 
                                      boolean pblnEvenRow, 
                                      String pstrUrl, 
                                      boolean pblnChecked, 
                                      int pintNumCheckboxes, 
                                      String pstrClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pblnEvenRow", pblnEvenRow);
            getZx().trace.traceParam("pstrUrl", pstrUrl);
            getZx().trace.traceParam("pblnChecked", pblnChecked);
            getZx().trace.traceParam("pintNumCheckboxes", pintNumCheckboxes);
            getZx().trace.traceParam("pstrClass", pstrClass);
        }

        zXType.rc multiListRowOpen = zXType.rc.rcOK; 
        
        try {
            
            /**
             * Open row
             */
            if (StringUtil.len(pstrClass) == 0) {
                // Alternate the colours
                s.append("<tr ").appendAttr("class", pblnEvenRow?"zxNor":"zxAlt").appendNL('>');
            } else {
                // A custom class for the row.
                s.append("<tr ").appendAttr("class", pstrClass).appendNL('>');
            }
            
            /**
             * Small hack here : We do not want this to wrap as it does not look good.
             */
            s.append("<td NOWRAP ")
             .appendAttr("width", getWebSettings().getListFormColumn1())
             .appendAttr("align", "center")
             .appendNL('>');
            
            /**
             * Only add select button if there is a select url
             */
            if (StringUtil.len(pstrUrl) > 0 ) {
				/**
				 * Include javascript code to set the cursor
				 */
                pstrUrl = "window.document.body.style.cursor='wait';" + pstrUrl + ";window.document.body.style.cursor='';";
                
                s.append("<img ")
                 .appendAttr("src", "../images/listItem.gif")
                 .appendAttr("onMouseDown", pstrUrl)
                 .appendAttr("onMouseOver", "javascript:this.src='../images/listItemOver.gif'")
                 .appendAttr("onMouseOut", "javascript:this.src='../images/listItem.gif'")
                 .appendNL('>');
            }
            
            /**
             * DGS 22NOV2002: Use a radio button if a negative number of checkboxes is given
             */
            boolean blnRadio = (pintNumCheckboxes < 0);
            pintNumCheckboxes = Math.abs(pintNumCheckboxes);
            String strControlType = (blnRadio?"radio":"checkbox");
            
            if (blnRadio && pintNumCheckboxes > 1) {
                s.append("<input ")
               	 .appendAttr("type", "hidden")
                 .appendAttr("name", "ctrMultilistRadio" + pobjBO.getDescriptor().getName())
                 .appendAttr("value", pobjBO.getPKValue().toString())
                 .appendNL('>');
            }
            
            /**
             * MB15OCT2004: Fix for single radio button to work vertically
             * DGS15NOV2004: Bug introduced back in June - should only set the value to 1 if is
             * a radio AND have more than one button on the line. Previously always set it to 1.
             */
            s.append("<input ")
             .appendAttr("type", strControlType)
             .appendAttr("name", "ctrMultilist" + pobjBO.getDescriptor().getName() + (blnRadio && pintNumCheckboxes > 1? pobjBO.getPKValue().getStringValue() : "") )
             .appendAttr("value", blnRadio && pintNumCheckboxes > 1? "1" : pobjBO.getPKValue().getStringValue());
            if (pblnChecked) s.appendAttr("checked", "Y");
            s.appendNL('>');
            
            for (int j = 2; j < pintNumCheckboxes; j++) {
                s.append("<input ")
            	 .appendAttr("type", strControlType)
            	 .appendAttr("name", "ctrMultilist" +pobjBO.getDescriptor().getName() + (blnRadio?pobjBO.getPKValue().getStringValue():j + ""))
            	 .appendAttr("value", blnRadio?j+"":pobjBO.getPKValue().getStringValue())
            	 .appendNL('>');
            }
            
            s.append("</td>").append(HTMLGen.NL);
            
            return multiListRowOpen;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate the HTML to open a multiListRow.", e);
            
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pblnEvenRow = "+ pblnEvenRow);
                getZx().log.error("Parameter : pstrUrl = "+ pstrUrl);
                getZx().log.error("Parameter : pblnChecked = "+ pblnChecked);
                getZx().log.error("Parameter : pintNumCheckboxes = "+ pintNumCheckboxes);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            multiListRowOpen = zXType.rc.rcError;
            return multiListRowOpen;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(multiListRowOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate the HTML to a multiListRow.
     *
     *<pre>
     *
     *Assumes   :
     *   Used in combination with multiListRowClose and multiListRowOpen
     * </pre>
     * 
     * @param pobjBO The business object used. 
     * @param pstrGroup The attribute group used. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if multiListRow fails. 
     */
    public zXType.rc multiListRow(ZXBO pobjBO, String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        zXType.rc multiListRow = zXType.rc.rcOK; 
        
        try {
            multiListRow = listRow(pobjBO, pstrGroup,0);
            return multiListRow;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate the HTML to a multiListRow.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            multiListRow = zXType.rc.rcError;
            return multiListRow;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(multiListRow);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate the HTML to close a multiListRow.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with multiListRow and multiListRowOpen
     * </pre>
     */
    public void multiListRowClose() {
    	listRowClose();
    }
    
    /**
     * Generate HTML for the end of a javascript popup function.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with
     *    popupMenuStart and popupMenuOption
     * </pre>
     */
    public void popupMenuEnd() {
        /**
         * Simply ends the javascript
         */
        this.s.appendNL("</script>");
    }
    
    /**
     * Generate HTML for an option within a javascript popup function.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with
     *    popupMenuStart and popupMenuEnd
     * </pre>
     *
     * @param pstrAction The action name. 
     * @param pstrLabel The label of the url. 
     * @param pstrUrl The url for the link 
     * @param pstrImage The image for the link. 
     * @param pstrImageOver The rollover image. 
     * @param pblnStartSubMenu Whether to start a sub menu. 
     */
    public void popupMenuOption(String pstrAction, 
    								 String pstrLabel, 
    								 String pstrUrl, 
    								 String pstrImage, 
    								 String pstrImageOver, 
    								 boolean pblnStartSubMenu) {
        if (pstrImage == null) {
            pstrImage = "";
        }
        if (pstrImageOver == null){
            pstrImageOver = "";
        }
        
        /**
         * Use the 'row' method of the zXDynPopup object to add a row
         */
        this.s.append("zXDynPopup").append(pstrAction)
        	  .append(".row(\"").append(pstrLabel).append("\",\"").append(pstrUrl).append("\",\"")
        	  .append(pstrImage).append("\",\"").append(pstrImageOver) 
        	  .append("\",").append((pblnStartSubMenu?1:0)).append(");").appendNL();
    }
    
    /**
     * Generate HTML for the start of a javascript popup function.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with
     *    popupMenuOption and popupMenuEnd
     * </pre>
     *
     * @param pstrAction The pageflow action to kick off. 
     */
    public void popupMenuStart(String pstrAction) {
        /**
         * Instantiate a zXDynPopup object of name zXDynPopupXXXX where XXXX is the popup action name.
         */
        this.s.appendNL("<script type=\"text/javascript\" language=\"JavaScript\">");
        this.s.append("var zXDynPopup").append(pstrAction).appendNL(" = new zXDynPopup();");
    }

    /**
     * Generate where clause part based on checked boxes in multi list.
     *
     * @param pobjRequest Handle to ASP request object
     * @param pobjBO The associated business object. 
     * @return Returns where clause from a multi list.
     * @throws ZXException Thrown if processMultilist fails. 
     */
    public String processMultilist(HttpServletRequest pobjRequest, ZXBO pobjBO) throws ZXException{
    	return processMultilist(pobjRequest, pobjBO, null, 1);
    }
     
    /**
     * Generate where clause part based on checked boxes in multi list.
     *
     * @param pobjRequest Handle to ASP request object
     * @param pobjBO The associated business object. 
     * @param pstrAttr (defaults to PK) What attribute to use when generating where clause
     * @param pintSeqNo (defaults to 1) Used in case of multiple checkboxes
     * @return Returns where clause from a multi list.
     * @throws ZXException Thrown if processMultilist fails. 
     */
    public String processMultilist(HttpServletRequest pobjRequest, ZXBO pobjBO, String pstrAttr, int pintSeqNo) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrAttr", pstrAttr);
            getZx().trace.traceParam("pintSeqNo", pintSeqNo);
        }

        String processMultilist = null; 
        
        try {
            
            processMultilist = processMultilistInternal(pobjRequest, pobjBO, pstrAttr, pintSeqNo, ",", true);
            
            
            return processMultilist;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate where clause part based on checked boxes in multi list.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrAttr = "+ pstrAttr);
                getZx().log.error("Parameter : pintSeqNo = "+ pintSeqNo);
            }
            if (getZx().throwException) throw new ZXException(e);
            return processMultilist;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processMultilist);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate where clause part based on checked boxes in multi list.
     *
     * <pre>
     * TM2 06JAN2005 DGS: Merged code of processMultilist and processMultilistNonSQL into
     * this one internal function, as they were very similar and I needed to bring the non-SQL
     * function into line with the normal one.
     * 
     * Reviewed for 1.5:1
     * 
     * </pre>
     * @param pobjRequest Handle to ASP request object
     * @param pobjBO The associated business object. 
     * @param pstrAttr (defaults to PK) What attribute to use when generating where clause
     * @param pintSeqNo (defaults to 1) Used in case of multiple checkboxes
     * @param pstrSep The seperator to use.
     * @param pblnSQL Whether to generate sql or not.
     * @return Returns where clause from a multi list.
     * @throws ZXException Thrown if processMultilist fails. 
     */
    public String processMultilistInternal(HttpServletRequest pobjRequest, 
                                           ZXBO pobjBO, 
                                           String pstrAttr, 
                                           int pintSeqNo,
                                           String pstrSep,
                                           boolean pblnSQL) throws ZXException {
		if(getZx().trace.isFrameworkTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjRequest", pobjRequest);
			getZx().trace.traceParam("pobjBO", pobjBO);
			getZx().trace.traceParam("pstrAttr", pstrAttr);
			getZx().trace.traceParam("pintSeqNo", pintSeqNo);
			getZx().trace.traceParam("pstrSep", pstrSep);
			getZx().trace.traceParam("pblnSQL", pblnSQL);
		}
		
		StringBuffer processMultilistInternal = new StringBuffer();
		
		try {
			DSHandler objDSHandler = pobjBO.getDS();
			DSWhereClause objDSWhereClause = new DSWhereClause();
			/** Is the datasource a channel ? */
			boolean blnDSIsChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;

			/**
			 * Determine attr to use
			 */
			if (StringUtil.len(pstrAttr) == 0) {
				pstrAttr = pobjBO.getDescriptor().getPrimaryKey();
			}

			Attribute objAttr = pobjBO.getDescriptor().getAttribute(pstrAttr);
             
			/**
			 * The number of checkboxes for a multilist can be controlled
			 * using the tag zXNumSelectCB, passed into here as pintSeqNo.
			 * This would be a negative number for a radio button, and the
			 * way it would be used is that the calling program (probably
			 * ASP) would first call this with sequence 1 to get a query
			 * where the first radio is selected, and then call again with
			 * sequence 2 for the second raio, and so on. The newer tag
			 * zXNumSelectRB is preferred now for radio buttons, and is not
			 * negative when entered, but will be here.
			 */
			boolean blnRadio  = (pintSeqNo < 0);
			pintSeqNo = Math.abs(pintSeqNo);
			
			String strItemName;
			if (blnRadio) {
				/**
				 * Radio buttons - only need special processing for these if have multiple radio
				 * buttons on the same line. A single radio button that behaves as making one
				 * line unique behaves just like a check box here.
				 * When multiple radio buttons on a line, a hidden field contains the value of the PK,
				 * and the visible fields determine if we need to include that PK in the where clause.
				 */
				strItemName = "ctrMultilistRadio" + pobjBO.getDescriptor().getName();
			} else {
				/**
				 * BD22NOV02 - Change to contain the entity name
				 */
				if (pintSeqNo == 1) {
					strItemName = "ctrMultilist" + pobjBO.getDescriptor().getName();
				} else {
					strItemName = "ctrMultilist" + pobjBO.getDescriptor().getName() + pintSeqNo;
				}                
			}
			
			String[] arrAttrValues = pobjRequest.getParameterValues(strItemName);
			if (arrAttrValues != null) {
			    String strTmp = "";
			    for (int i = 0; i < arrAttrValues.length; i++) {
			    	String strAttrValue = arrAttrValues[i];
			    	strTmp = "";
			    	
			    	if (blnRadio) {
			    		/**
			    		 * Radio buttons across the line. The name of all the buttons is the fixed string
			    		 * 'ctrMultilist' plus the BO name plus the PK of this line (as held in the hidden
			    		 * field for this line)
			    		 */
						String strItemNameRadioValue = pobjRequest.getParameter("ctrMultilist" + pobjBO.getDescriptor().getName() + strAttrValue);
						if (strItemNameRadioValue.equals(pintSeqNo + "")) {
							/**
							 * If the selected radio button is the same as the one we are processing (e.g. we are
							 * refining based on the second button being selected, and the selected one is the second)
							 * it gets added to the where clause
							 */
							if (pblnSQL) {
								if (blnDSIsChannel) {
									objDSWhereClause.singleWhereCondition(pobjBO, 
																			objAttr,
																			zXType.compareOperand.coEQ,
																			new StringProperty(strAttrValue),
																			zXType.dsWhereConditionOperator.dswcoOr); // Add to OR statement
								} else {
									pobjBO.setValue(pstrAttr, strAttrValue);
									strTmp = getZx().getSql().dbValue(pobjBO, objAttr);
								} // Channel or RDBMS?
								
							} else {
								strTmp = strAttrValue;
							}
						}
						
			        } else {
			            if (pblnSQL) {
			               if (blnDSIsChannel) {
			                   objDSWhereClause.singleWhereCondition(pobjBO, 
			                                                         objAttr,
			                                                         zXType.compareOperand.coEQ,
			                                                         new StringProperty(strAttrValue),
			                                                         zXType.dsWhereConditionOperator.dswcoOr); // Add to OR statement.
			               } else {
			                   pobjBO.setValue(pstrAttr, strAttrValue);
			                   strTmp = getZx().getSql().dbValue(pobjBO, objAttr);
			               } // Channel or RDBMS?
			            } else {
			               strTmp = strAttrValue;
			            }
			            
			        } // Radio button
			    	
			        /**
			         * Check for strTmp; may be blank in case of radio buttons
			         */
			        if (StringUtil.len(strTmp) > 0) {
			        	if (processMultilistInternal.length() == 0) {
			        		if (pblnSQL) {
			        			if (!blnDSIsChannel) {
			        				processMultilistInternal.append(getZx().getSql().columnName(pobjBO, objAttr, zXType.sqlObjectName.sonName)).append(" IN (").append(strTmp);
			        			}
			        			
			        		} else {
			        			processMultilistInternal.append(strTmp);
			        		}
			        		
			        	} else {
			        		if (!blnDSIsChannel) {
			        			processMultilistInternal.append(pstrSep).append(strTmp);
			        		}
			        	}
			        	
			        } // strTmp has value
			        
			    } // Loop over number of items
			    
			} // Item from request <> blank
             
			if (pblnSQL && processMultilistInternal.length() > 0 && !blnDSIsChannel) {
				processMultilistInternal.append(")");
			}
			
			if (blnDSIsChannel) {
				processMultilistInternal = new StringBuffer(objDSWhereClause.getAsWhereClause());
			}
			
			return processMultilistInternal.toString();
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Generate where clause part based on checked boxes in multi list.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
				getZx().log.error("Parameter : pobjBO = "+ pobjBO);
				getZx().log.error("Parameter : pstrAttr = "+ pstrAttr);
				getZx().log.error("Parameter : pintSeqNo = "+ pintSeqNo);
			}
			if (getZx().throwException) throw new ZXException(e);
			return processMultilistInternal.toString();
		} finally {
			if(getZx().trace.isFrameworkTraceEnabled()) {
				getZx().trace.returnValue(processMultilistInternal);
				getZx().trace.exitMethod();
			}
		}
	}
     
    /**
     * Generate HTML for list header BO columns bit.
     * 
     * <pre>
     * 
     *  Assumes   :
     *   Used in combination with
     *    treeListHeader and treeListHeaderOpen
     * </pre>
     *
     * @param pobjBO The business object linked to the tree list header 
     * @param pstrGroup The attribute group. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if treeListHeader fails. 
     */
    public zXType.rc treeListHeader(ZXBO pobjBO, String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        zXType.rc treeListHeader = zXType.rc.rcOK; 
        
        try {
        	ZXCollection colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) {
                throw new Exception("Unable to get group : " + pstrGroup);
            }
            
            Attribute objAttr;
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                /**
                 * BD17DEC02
                 * Bit of a dodge: when there is only one attribute in the attribute group,
                 * we are likely dealing with a header for a pivot table header in which case
                 * a left align looks better
                 */
                this.s.append("<td ")
                	  .appendAttr("class", colAttr.size()==1?"zXListHeaderLeftAlign":"zXListHeader")
                	  .appendNL('>');
                this.s.appendNL(objAttr.getLabel().getLabel());
                this.s.appendNL("</td>");
            }
            
            return treeListHeader;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for list header BO columns bit.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            treeListHeader = zXType.rc.rcError;
            return treeListHeader;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(treeListHeader);
                getZx().trace.exitMethod();
            }
        }
	}
    
    /**
     * Generate HTML for list header close bit.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with
     *    treeListHeader and treeListHeaderOpen
     * </pre>
     */
    public void treeListHeaderClose() {
		this.s.appendNL("</tr>");
	}
    
    /**
     * Generate HTML for list header open bit.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with
     *    BOtreeListHeader and closetreeListHeader
     *    Table already been opened
     * </pre>
     */
    public void treeListHeaderOpen() {
        this.s.appendNL("<tr>");
        this.s.append("<td ")
              .appendAttr("width", getWebSettings().getListFormColumn1())
              .appendAttr("align", "center")
              .appendAttr("class", "zXListHeader")
              .appendNL('>');
        this.s.appendNL("</td>");
    }
    
    /**
     * Generate HTML for tree list header open.
     * 
     * <pre>
     * 
     * For the top level (has the global toggle open/close image)
     * 
     * Assumes   :
     *    Used in combination with
     *    BOtreeListHeader and closetreeListHeader
     *    Table already been opened
     * </pre>
     *
     */
    public void treeListHeaderOpenTop() {
    	
        this.s.append("<tr>").append(HTMLGen.NL);
        this.s.append("<td ")
              .appendAttr("width", getWebSettings().getListFormColumn1())
              .appendAttr("align", "center")
              .appendAttr("class", "zXListHeader")
              .appendNL('>');
        this.s.append("<img ")
              .appendAttr("id", "_mnuLvlImg_Main")
              .appendAttr("src", "../images/menuClosed.gif")
              .appendAttr("onClick", "javascript:zXMMToggleWholeMenu();")
              .appendNL('>');
        this.s.appendNL("</td>");
        
//      this.s.appendNL("<script language='Javascript'>");
//		this.s.appendNL("var _mnuLvlImg_Main_OPEN = '../images/menuOpen.gif';");
//		this.s.appendNL("var _mnuLvlImg_Main_CLOSED = '../images/menuClosed.gif';");
//		this.s.appendNL("</script>");
    }
    
    /**
     * Generate the HTML to a treeListRow.
     * 
     * @param pobjBO The business object. 
     * @param pstrGroup The attribute for the row 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if treeListRow fails. 
     */
    public zXType.rc treeListRow(ZXBO pobjBO, String pstrGroup) throws ZXException{
		return listRow(pobjBO, pstrGroup, 0);
    }
    
    /**
     * Generate the HTML to a treeListRow.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with treeListRowClose and treeListRowOpen
     * </pre>
     *
     * @param pobjBO The business object. 
     * @param pstrGroup The attribute for the row 
     * @param pintLevel The level of the row. Optional, default is 0
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if treeListRow fails. 
     */
    public zXType.rc treeListRow(ZXBO pobjBO, String pstrGroup, int pintLevel) throws ZXException{
		return listRow(pobjBO, pstrGroup, pintLevel);
    }
    
    /**
     * Generate the HTML to close a treeListRow.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with treeListRow and treeListRowOpen
     * </pre>
     */
    public void treeListRowClose() {
        this.s.appendNL("</tr>");
    }
    
    /**
     * Generate the HTML to open a treeListRow.
     * 
     * <pre>
     * 
     * Assumes   :
     *   Used in combination with treeListRowClose and treeListRow
     *   Table has already been opened
     * </pre>
     *
     * @param pblnEvenRow Used to determine the color of the row 
     * @param pstrID Name of the div associated with this entry. 
     * @param pstrUrl If blank, assume a treeListRow with no. 
     * @param pblnLowestLevel Indicates whether a sub level is expected 
     * @param pstrClass Optional class override. 
     * @return Returns the return code of the method.
     */
    public zXType.rc treeListRowOpen(boolean pblnEvenRow, 
                                     String pstrID, 
                                     String pstrUrl, 
                                     boolean pblnLowestLevel, 
                                     String pstrClass) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pblnEvenRow", pblnEvenRow);
            getZx().trace.traceParam("pstrID", pstrID);
            getZx().trace.traceParam("pstrUrl", pstrUrl);
            getZx().trace.traceParam("pblnLowestLevel", pblnLowestLevel);
            getZx().trace.traceParam("pstrClass", pstrClass);
        }
        
        zXType.rc treeListRowOpen = zXType.rc.rcOK; 
        
        try {
            /**
             * Open row
             */
            if (StringUtil.len(pstrClass) == 0) {
                this.s.append("<tr ")
                	  .appendAttr("class", pblnEvenRow?"zxNor":"zxAlt")
                	  .appendNL('>');
            } else {
                this.s.append("<tr ")
                      .appendAttr("class", pstrClass)
                      .appendNL('>');
            }
            
            this.s.append("<td ")
                  .appendAttr("width", getWebSettings().getListFormColumn1())
                  .appendAttr("align", "center")
                  .appendNL('>');
            
            /**
             * Only add the image for select if there is a select button
             */
            if (StringUtil.len(pstrUrl) > 0) {
                /**
                 * Include javascript code to set the cursor
                 */
                // pstrUrl = "window.document.body.style.cursor='wait';" + pstrUrl + ";window.document.body.style.cursor='';";
                
                this.s.append("<img ")
                	  .appendAttr("src", "../images/listItem.gif")
                	  .appendAttr("onMouseDown", pstrUrl)
                	  .appendAttr("onMouseOver", "javascript:this.src='../images/listItemOver.gif'")
                	  .appendAttr("onMouseOut", "javascript:this.src='../images/listItem.gif'")
                	  .appendNL('>');
            }
            
            /**
             * If this is not the lowest level: add menu open / close button
             * DGS17DEC02: Added id so that this image gets toggled when the top-level menu is
             * expanded; the javascript uses a match on '_mnuLvlImg_' to toggle images.
             */
            if (!pblnLowestLevel) {
                this.s.append("<img ")
                	  .appendAttr("id", "_mnuLvlImg_Sub")
                	  .appendAttr("src", "../images/menuClosed.gif")
                	  .appendAttr("onMouseDown", "zXTreeLevelToggle(this, '" + pstrID + "');")
                	  .appendNL('>');
                
//	            this.s.appendNL("<script language='Javascript'>");
//	            this.s.appendNL("var _mnuLvlImg_Sub_OPEN = '../images/menuOpen.gif';");
//	            this.s.appendNL("var _mnuLvlImg_Sub_CLOSED = '../images/menuClosed.gif';");
//	            this.s.appendNL("</script>");
            }
            
            this.s.appendNL("</td>");
            
            return treeListRowOpen;
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(treeListRowOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for saved Query control.
     * 
     * @param pstrPageflowName The name of the pageflow in which this search form belongs.
     * @param pstrEntity The PF Entity for the saved query 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if savedQuery fails. 
     */
    public zXType.rc savedQuery(String pstrPageflowName, String pstrEntity) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrEntity", pstrEntity);
        }

        zXType.rc savedQuery = zXType.rc.rcOK; 
        
        try {
            ZXBO objZXQryBO = getZx().createBO("zXQry");
            Attribute objAttr = objZXQryBO.getDescriptor().getAttributes().get("nme");
            
            /**
             * Input field for saved query names
             */
            this.s.append("<input ")
				  .appendAttr("name", "zXSavedQuery")
				  .appendAttr("value", "")
				  .appendAttr("size", "" + objAttr.getOutputLength())
				  .appendAttr("maxlength", "" + objAttr.getLength())
				  .appendAttr("type", "text")
				  .appendAttr("class", "zxFormInputOptional")
				  .appendAttr("title", tooltip(objZXQryBO, objAttr))
                  .appendNL('>');
            
            /**
             * Button to get list of existing saved queries
             */
            StringBuffer strUrl = new StringBuffer(150);
            strUrl.append("../jsp/zXGPF.jsp?-s=").append(this.pageflow.getQs().getEntryAsString("-s"))
            	  .append("&-ss=").append(this.pageflow.getQs().getEntryAsString("-ss"))
            	  .append("&-pf=zXSavedQuery")
            	  .append("&-qrypf=").append(pstrPageflowName)
            	  .append("&-qryentity=").append(pstrEntity)
            	  .append("&-sa=insert");
            
            this.s.append("<input ")
	              .appendAttr("name", "zXSavedQueryButton")
	              .appendAttr("value", "...")
	              .appendAttr("onClick", "zXWindow('" + strUrl + "');")
	              .appendAttr("type", "button")
	              .appendAttr("class", "zxLookupButton")
	              .appendAttr("title", "Click to select a saved search query")
                  .appendNL('>');
            
            return savedQuery;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for saved Query control.", e);
            if (getZx().throwException) throw new ZXException(e);
            savedQuery = zXType.rc.rcError;
            return savedQuery;
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(savedQuery);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for end of saved query entries.
     * 
     * <pre>
     * 
     * Assumes   :
     *   Used in combination with open
     * </pre>
     *
     * @return Returns the return code of the method.
     */
    public zXType.rc savedQueryClose() {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc savedQueryClose = zXType.rc.rcOK;
        
        try {
            
            this.s.append("</td>").append(HTMLGen.NL);
            this.s.append("<td ")
            	  .appendAttr("class", "zXLabelPlain")
            	  .appendAttr("width", getWebSettings().getSearchFormColumn2())
            	  .append('>');
            this.s.append("</td>").append(HTMLGen.NL);
            this.s.append("</tr>").append(HTMLGen.NL);
            this.s.append("</table>").append(HTMLGen.NL);
            
            return savedQueryClose;
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(savedQueryClose);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for start of saved query entries.
     * 
     * <pre>
     * 
     * Assumes   :
     *   Used in combination with close
     * </pre>
     *
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if savedQueryOpen fails. 
     */
    public zXType.rc savedQueryOpen() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc savedQueryOpen = zXType.rc.rcOK; 

        try {
            ZXBO objZXQryBO = getZx().createBO("zXQry");
            
            this.s.append("<br>").append(HTMLGen.NL);
            this.s.append("<table width=\"100%\">").append(HTMLGen.NL);
            this.s.append("<tr>").append(HTMLGen.NL);
            
            this.s.append("<td ")
                  .appendAttr("class", "zXFormLabel")
                  .appendAttr("width", getWebSettings().getSearchFormColumn1())
                  .appendNL('>');
            this.s.append(objZXQryBO.getDescriptor().getLabel().getLabel()).append(HTMLGen.NL);
            this.s.append("</td>").append(HTMLGen.NL);
            
            this.s.append("<td ")
		          .appendAttr("class", "zXLabelPlain")
		          .appendAttr("width", getWebSettings().getSearchFormColumn3())
		          .appendNL('>');
            
            return savedQueryOpen;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for start of saved query entries.", e);
            if (getZx().throwException) throw new ZXException(e);
            savedQueryOpen = zXType.rc.rcError;
            return savedQueryOpen;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(savedQueryOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate JS code that supports the progress bar.
     *
     * @param pstrProgressControl Name of progress control. 
     * @param pintPercentage Percentage done 
     * @param pstrMessage Message to display. Not used any more ?
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if showProgress fails. 
     */
    public zXType.rc showProgress(String pstrProgressControl, int pintPercentage, String pstrMessage) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrProgressControl", pstrProgressControl);
            getZx().trace.traceParam("pintPercentage", pintPercentage);
            getZx().trace.traceParam("pstrMessage", pstrMessage);
        }
        
        zXType.rc showProgress = zXType.rc.rcOK; 

        try {
            
            this.s.append("<script type=\"text/javascript\" language=\"JavaScript\">").append(HTMLGen.NL);
            this.s.append(pstrProgressControl + ".width='" + pintPercentage + "%';").append(HTMLGen.NL);
            pstrMessage = pintPercentage + "%";
            this.s.append(pstrProgressControl + ".innerHTML='" + pstrMessage + "';").append(HTMLGen.NL);
            this.s.append("</script>").append(HTMLGen.NL);
            
            return showProgress;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate JS code that supports the progress bar.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrProgressControl = "+ pstrProgressControl);
                getZx().log.error("Parameter : pintPercentage = "+ pintPercentage);
                getZx().log.error("Parameter : pstrMessage = "+ pstrMessage);
            }
            if (getZx().throwException) throw new ZXException(e);
            showProgress = zXType.rc.rcError;
            return showProgress;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(showProgress);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for soft informational message.
     *
     * @param pstrMsg The message to display. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if softMsg fails. 
     */
    public zXType.rc softMsg(String pstrMsg) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrMsg", pstrMsg);
        }

        zXType.rc softMsg = zXType.rc.rcOK; 
        
        try {
            
            this.s.append("<table width=\"100%\">").append(HTMLGen.NL);
            this.s.append("<tr>").append(HTMLGen.NL);
            this.s.append("<td valign=\"top\" width=\"7%\"></td>").append(HTMLGen.NL);
            this.s.append("<td width=\"*\" class=\"zxSoftMsg\" >").append(HTMLGen.NL);
            this.s.append(StringUtil.replaceAll(pstrMsg, '\n', "<br/>"));
            this.s.append("</td>").append(HTMLGen.NL);
            this.s.append("</tr>").append(HTMLGen.NL);
            this.s.append("</table>").append(HTMLGen.NL);
            
            return softMsg;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for soft informational message.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrMsg = "+ pstrMsg);
            }
            if (getZx().throwException) throw new ZXException(e);
            softMsg = zXType.rc.rcError;
            return softMsg;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(softMsg);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for a submit button.
     *
     * @param pstrLabel  Label for the submit button
     * @param pstrTitle  Optional tooltip for the button
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if submitButton fails. 
     */
    public zXType.rc submitButton(String pstrLabel, String pstrTitle) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrLabel", pstrLabel);
            getZx().trace.traceParam("pstrTitle", pstrTitle);
        }

        zXType.rc submitButton = zXType.rc.rcOK; 
        
        try {
            
        	/**
        	 * If there is no label for the button
        	 * we generate one if the user is english.
        	 */
            if (getZx().getLanguage().equalsIgnoreCase("EN")) {
                if (StringUtil.len(pstrLabel) == 0) {
                    pstrLabel = "OK";
                } 
                if (StringUtil.len(pstrTitle) == 0) {
                    pstrTitle = "Submit this form";
                }
            }
            
            this.s.append("<input type=\"submit\" ")
	              .appendAttr("class", "zxSubmitButton")
	              .appendAttr("value", pstrLabel)
	              .appendAttr("title", pstrTitle)
	              .appendAttr("onClick", "zXSetWaitCursor(this);this.form.submit();this.disabled=true;")
	              .appendNL('>');
            
            return submitButton;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for a submit button.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrLabel = "+ pstrLabel);
                getZx().log.error("Parameter : pstrTitle = "+ pstrTitle);
            }
            if (getZx().throwException) throw new ZXException(e);
            submitButton = zXType.rc.rcError;
            return submitButton;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(submitButton);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Process a submitted edit form.
     *
     * @param pobjRequest The http servlet request. 
     * @param pobjBO The business object for the edti form. 
     * @param pstrGroup Attribute group. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if processEditForm fails. 
     */
    public zXType.rc processEditForm(HttpServletRequest pobjRequest, ZXBO pobjBO, String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        zXType.rc processEditForm = zXType.rc.rcOK; 
        
        try {
            
            processEditForm = processEditFormInternal(pobjRequest, pobjBO, pstrGroup, false);
            
            return processEditForm;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process a submitted edit form.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            processEditForm = zXType.rc.rcError;
            return processEditForm;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processEditForm);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Process a submitted edit form.
     *
     * @param pobjRequest The http servlet request 
     * @param pobjBO The business object for the edit form. 
     * @param pstrGroup The attribute group updated in the edit form. 
     * @param pblnAllowEmptyAutomatics Allow empty automatics. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if processEditFormInternal fails. 
     */
    private zXType.rc processEditFormInternal(HttpServletRequest pobjRequest, ZXBO pobjBO, String pstrGroup, boolean pblnAllowEmptyAutomatics) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pblnAllowEmptyAutomatics", pblnAllowEmptyAutomatics);
        }

        zXType.rc processEditFormInternal = zXType.rc.rcOK;
        
        try {
            /**
             * Do a pre-persist to allow user to do some special handling; no support
             *  for warning on this one
             */
            pobjBO.prePersist(zXType.persistAction.paProcessEditForm, pstrGroup);
            
            /**
             * Get handle to group
             * BD9JUN04 - Changed from audtable to concurrency control
             */
            AttributeCollection colAttr;
            if (pobjBO.getDescriptor().isConcurrencyControl()) {
                colAttr = pobjBO.getDescriptor().getGroup(pstrGroup + ",~");
            } else {
                colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            }
            if (colAttr == null) {
                throw new Exception("Unable to get handle to group" + pstrGroup);
            }
            
            Attribute objAttr;
            String strValue = "";
            int dataType;
            zXType.rc enmRC = zXType.rc.rcOK;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                /**
                 * BD28NOV02
                 *  Added index of 1; this is most of the time not really needed but
                 *  under some circumstances however there may be multiple instances
                 *  of the same field on the form. For example, this may happen if
                 *  you manualy create a form with 2 columns for an auditable business object
                 * 
                 * NOTE : This is not need for the java implementation, as getParameter
                 * returns a string of the first value that matches.
                 * 
                 * If left$(objAttr.name, 2) = "zX" And objAttr.name <> "zXRqd" Then
                 * 		strValue = pobjRequest(controlName(pobjBO, objAttr))(1)
                 * 	Else
                 * 		strValue = pobjRequest(controlName(pobjBO, objAttr))
                 * End If
                 * 
                 */
                strValue = pobjRequest.getParameter(controlName(pobjBO, objAttr));
                boolean blnIsNull = (strValue == null);
                if (blnIsNull) {
                    strValue = "";
                }
                
                /**
                 * If there was an option list and the value = '-', the user
                 *  had selected the none option
                 */
                if (objAttr.getOptions().size() > 0 || StringUtil.len(objAttr.getForeignKey()) > 0) {
                    /**
                     * DGS15MAY2003: The following "If" statement used to say "And objAttr.isOptional" but now
                     *  a blank value is possible even when mandatory (see zxForm.js zXOption.prototype.onChangeRel
                     *  for more information):
                     */
                    if ("-".equals(strValue)) {
                        strValue = "";
                    }
                }
                
                /**
                 * Ready to set value
                 */
                 dataType = objAttr.getDataType().pos;
                 if (dataType == zXType.dataType.dtAutomatic.pos || dataType == zXType.dataType.dtLong.pos) {
                     if (StringUtil.len(strValue) == 0) {
                         if (dataType == zXType.dataType.dtAutomatic.pos && pblnAllowEmptyAutomatics) {
                             /**
                              * In the case of a grid edit form, the automatics are not present
                              *  on the screen. This is not an error - the code will set them
                              *  before updating.
                              *  Matrix edit forms can leave any field blank, even if mandatory
                              */
                             enmRC = zXType.rc.rcOK;
                         } else {
                             enmRC = pobjBO.setValue(objAttr.getName(), "0", false, true);
                         }
                     } else {
                         enmRC = pobjBO.setValue(objAttr.getName(), strValue);
                     }
                     
                 } else if (dataType == zXType.dataType.dtDate.pos
                         || dataType == zXType.dataType.dtTime.pos
                         || dataType == zXType.dataType.dtTimestamp.pos) {
                    
                     if (StringUtil.len(strValue) == 0) {
                         // Let the dateProperty decide how to handle empty data values : 
                         enmRC = pobjBO.setValue(objAttr.getName(), "", false, true);
                     } else {
                         enmRC = pobjBO.setValue(objAttr.getName(), strValue);
                     }
                     
                 } else if (dataType == zXType.dataType.dtBoolean.pos) {
                     /**
                      * If the attr is a boolean, translate the checkbox value
                      * to a meaningful internal value
                      * DGS5JUN2003: Hidden locked booleans will have a value like 'False'. Note
                      * that it is theoretically possible for a visible boolean to have a value of
                      * 'False' when it is checked (i.e. field has a name of 'False'). However, this
                      *  is impossible given our field naming generation, so 'false' here is false.
                      */
                     if (StringUtil.len(strValue) > 0 && !"false".equalsIgnoreCase(strValue)) {
                         enmRC = pobjBO.setValue(objAttr.getName(), "true");
                     } else {
                         // Default to false.
                         enmRC = pobjBO.setValue(objAttr.getName(), "false");
                     }
                     
                 } else if (dataType == zXType.dataType.dtDouble.pos) {
                     if (StringUtil.len(strValue) == 0) {
                         enmRC = pobjBO.setValue(objAttr.getName(), "0.0", false, true);
                     } else {
                         enmRC = pobjBO.setValue(objAttr.getName(), strValue);
                     }
                     
                 /**
                  * BD07APR2003: Expression behaves the same as string in this case:
                  */
                 } else if (dataType == zXType.dataType.dtString.pos
                         || dataType == zXType.dataType.dtExpression.pos) {
                     enmRC = pobjBO.setValue(objAttr.getName(),strValue, false, StringUtil.len(strValue) == 0);
                 }
                 
                 if (enmRC.equals(zXType.rc.rcError)) {
                     processEditFormInternal = zXType.rc.rcError;
                 } else if (enmRC.equals(zXType.rc.rcWarning)) {
                     if (processEditFormInternal.equals(zXType.rc.rcOK)) {
                         processEditFormInternal = zXType.rc.rcWarning;
                     }
                 }
            }
            
            /**
             * And post-persist for some final validation.
             * 
             * DGS16MAY2003: Two things - first, let the postPersist method know if there is an
             * error in this edit form (by using a different enum). Secondly, don't allow the
             * return code from postPersist to downgrade the current status of this method from
             * error or warning.
             */
            if (processEditFormInternal.pos == zXType.rc.rcError.pos) {
                pobjBO.setLastPostPersistRC(zXType.rc.rcError);
                enmRC = pobjBO.postPersist(zXType.persistAction.paProcessEditFormError, pstrGroup);
            } else {
                pobjBO.setLastPostPersistRC(zXType.rc.rcOK);
                enmRC = pobjBO.postPersist(zXType.persistAction.paProcessEditForm, pstrGroup);
            }
            
            if (enmRC.pos == zXType.rc.rcError.pos) {
                processEditFormInternal = zXType.rc.rcError;
            } else if (enmRC.equals(zXType.rc.rcWarning)) {
                if (processEditFormInternal.equals(zXType.rc.rcOK)) {
                    processEditFormInternal = zXType.rc.rcWarning;
                }
            }
            
            return processEditFormInternal;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process a submitted edit form.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
                getZx().log.error("Parameter : pblnAllowEmptyAutomatics = "+ pblnAllowEmptyAutomatics);
            }
            if (getZx().throwException) throw new ZXException(e);
            processEditFormInternal = zXType.rc.rcError;
            return processEditFormInternal;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processEditFormInternal);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Process a submitted edit form.
     *
     * @param pobjRequest The http servlet request 
     * @param pobjBO The business object of the request. 
     * @param pstrGroup The attribute group used. 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if processGridEditForm fails. 
     */
    public zXType.rc processGridEditForm(HttpServletRequest pobjRequest, ZXBO pobjBO, String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        zXType.rc processGridEditForm = zXType.rc.rcOK; 
        
        try {
            
            processGridEditForm = processEditFormInternal(pobjRequest, pobjBO, pstrGroup, true);
            
            return processGridEditForm;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process a submitted edit form.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            processGridEditForm = zXType.rc.rcError;
            return processGridEditForm;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processGridEditForm);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Process a submitted edit form.
     *
     * @param pobjRequest The http servlet request 
     * @param pobjBO The business object used. 
     * @param pstrGroup The attribute group used 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if processMatrixEditForm fails. 
     */
    public zXType.rc processMatrixEditForm(HttpServletRequest pobjRequest, ZXBO pobjBO, String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        zXType.rc processMatrixEditForm = zXType.rc.rcOK; 
        
        try {
            processMatrixEditForm = processEditFormInternal(pobjRequest, pobjBO, pstrGroup, true);
            return processMatrixEditForm;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process a submitted edit form.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            processMatrixEditForm = zXType.rc.rcError;
            return processMatrixEditForm;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processMatrixEditForm);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get multilist as seperated list.
     * 
     * <pre>
     * 
     * Much the same as processMultiList but instead of returning a where clause 
     * it returns a separated list.
     * 
     * TM2 06JAN2005 DGS: Now calls an internal function called processMultilistInternal
     * so that the same logic can be used as in the similar function processMultilist
     * </pre>
     *
     * @param pobjRequest The http servlet request. 
     * @param pobjBO The business object. 
     * @param pchrSep character to use when separting items in list. Optional, default should be "|".
     * @param pstrAttr (defaults to PK) What attribute to use when generating where clause. 
     * @param pintSequence (defaults to 1) Used in case of multiple checkboxes. 
     * @return Returns a seperated list.
     * @throws ZXException Thrown if processMultiListNonSQL fails. 
     */
    public String processMultiListNonSQL(HttpServletRequest pobjRequest, 
                                        ZXBO pobjBO, 
                                        String pchrSep, 
                                        String pstrAttr, 
                                        int pintSequence) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRequest", pobjRequest);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pchrSep", pchrSep);
            getZx().trace.traceParam("pstrAttr", pstrAttr);
            getZx().trace.traceParam("pintSequence", pintSequence);
        }

        String processMultiListNonSQL = null; 
        
        try {
            
            processMultiListNonSQL = processMultilistInternal(pobjRequest, pobjBO, pstrAttr, pintSequence, pchrSep, false);
            
            return processMultiListNonSQL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get multilist as seperated list.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRequest = "+ pobjRequest);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pchrSep = "+ pchrSep);
                getZx().log.error("Parameter : pstrAttr = "+ pstrAttr);
                getZx().log.error("Parameter : pintSequence = "+ pintSequence);
            }
            if (getZx().throwException) throw new ZXException(e);
            return processMultiListNonSQL;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processMultiListNonSQL);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Return time in milliseconds since 1970.
     *
     * @return Returns time in milliseconds since 1970.
     * @throws ZXException Thrown if clockTickCount fails. 
     */
    public long clockTickCount() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        long clockTickCount = 0; 
        try {
            
            clockTickCount = System.currentTimeMillis();
            
            return clockTickCount;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Return time in milliseconds since 1970.", e);
            if (getZx().throwException) throw new ZXException(e);
            return clockTickCount;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(clockTickCount);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate javascript enhancers relating to this attribute.
     * 
     * <pre>
     * 
     * MB12JUN06 - V1.5:10
     * </pre>
     * 
     * @param pobjBO The business object for the form enhancer. 
     * @param pobjAttr The attribute with the enhancer 
     * @param pstrOnChange The onChange action. This is used by the callee.
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if editFormEnhancers fails. 
     */
    public zXType.rc editFormEnhancers(ZXBO pobjBO, Attribute pobjAttr, StringBuffer pstrOnChange) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pstrOnChange", pstrOnChange);
        }

        zXType.rc editFormEnhancers = zXType.rc.rcOK; 
        
        try {
            PFEditEnhancer objEditEnhancer;
            PFEditDependency objEditDependency;
            int intEditDependency;
            PFEntity objEntity;
            
            List colEditEnhancers = pobjBO.getEditEnhancers(pobjAttr.getName());
            if (colEditEnhancers == null) return editFormEnhancers;
            
            String strThisAsElementByName = "elementByName(self, '" + controlName(pobjBO, pobjAttr) + "')";
            
            int intEditEnhancers = colEditEnhancers.size();
            for (int i = 0; i < intEditEnhancers; i++) {
                objEditEnhancer = (PFEditEnhancer)colEditEnhancers.get(i);
                
                intEditDependency = objEditEnhancer.getEditdependencies().size();
                for (int j = 0; j < intEditDependency; j++) {
                    objEditDependency = (PFEditDependency)objEditEnhancer.getEditdependencies().get(j);
                    zXType.pageflowDependencyType depType = objEditDependency.getDepType();
                    
                    /**
                     * Reset values otherwise they may be added twice
                     */
                    StringBuffer strSinglePostJavaScript = new StringBuffer();
                    StringBuffer strSingleOnChange = new StringBuffer();
                    
                    if(!depType.equals(zXType.pageflowDependencyType.pdtRestrict)) {
                        /**
                         * DGS07APR2003: Don't do the following for dependency type 'Restrict'. There's
                         * no javascript needed, and anyway we won't have anything in objEditDependency.depEntity
                         * so the code would fail.
                         */
                        objEntity = (PFEntity)this.pageflow.getContextEntities().get(this.pageflow.resolveDirector(objEditDependency.getDepentity()));
                        String strRelCtr = controlName(objEntity.getBo(), objEntity.getBODesc().getAttribute(this.pageflow.resolveDirector(objEditDependency.getDepattr())));
                        
                        /**
                         * BD9JAN04 Also make a version for the elementByName variation
                         */
                        String strRelCtrAsElementByName = "elementByName(self, '" + strRelCtr + "')";
                        
                        if (depType.equals(zXType.pageflowDependencyType.pdtDisable)
                            || depType.equals(zXType.pageflowDependencyType.pdtDisableBlank)) {
                            /**
                             * Disable and Disable/Blank: the onChange event will call the javascript function that
                             *  sets the disabled property according to the value and operator.
                             */
                        	strSingleOnChange.append("zXEnhDisable(this,");
                        	strSingleOnChange.append("'").append(zXType.valueOf(objEditDependency.getOpeRator()).toUpperCase()).append("',");
                        	strSingleOnChange.append(this.pageflow.resolveDirector(objEditDependency.getValue())).append(",");
                        	strSingleOnChange.append(strRelCtr).append(",");
                        	strSingleOnChange.append(depType.equals(zXType.pageflowDependencyType.pdtDisableBlank)?"true":"false");
                        	strSingleOnChange.append(");");
                            
                            strSinglePostJavaScript.append("zXEnhDisable(").append(strThisAsElementByName).append(",");
                            strSinglePostJavaScript.append("'").append(zXType.valueOf(objEditDependency.getOpeRator()).toUpperCase()).append("',");
                            strSinglePostJavaScript.append(this.pageflow.resolveDirector(objEditDependency.getValue())).append(",");
                            strSinglePostJavaScript.append(strRelCtrAsElementByName).append(",");
                            strSinglePostJavaScript.append(depType.equals(zXType.pageflowDependencyType.pdtDisableBlank)?"true":"false");
                            strSinglePostJavaScript.append(");");
                            
                        } else if (depType.equals(zXType.pageflowDependencyType.pdtSet)){
                            /**
                             * Set: the onChange event will call the javascript function that sets the value
                             * of the field according to the value and operator.
                             */
                        	strSingleOnChange.append("zXEnhSet(this,");
                        	strSingleOnChange.append("'").append(zXType.valueOf(objEditDependency.getOpeRator()).toUpperCase()).append("',");
                        	strSingleOnChange.append(this.pageflow.resolveDirector(objEditDependency.getValue())).append(",");
                        	strSingleOnChange.append(strRelCtr).append(",");
                            strSingleOnChange.append(this.pageflow.resolveDirector(objEditDependency.getDepvalue()));
                            strSingleOnChange.append(");");
                            
                            strSinglePostJavaScript.append("zXEnhSet(").append(strThisAsElementByName).append(",");
                            strSinglePostJavaScript.append("'").append(zXType.valueOf(objEditDependency.getOpeRator()).toUpperCase()).append("',");
                            strSinglePostJavaScript.append(this.pageflow.resolveDirector(objEditDependency.getValue())).append(",");
                            strSinglePostJavaScript.append(strRelCtrAsElementByName).append(",");
                            strSinglePostJavaScript.append(this.pageflow.resolveDirector(objEditDependency.getDepvalue()));
                            strSinglePostJavaScript.append(");");
                            
                        } else if (depType.equals(zXType.pageflowDependencyType.pdtBound)){
                            /**
                             * Bound: the onChange event will call the javascript method of a dynamically generated
                             * zXOption object, relating to the child (To) field. This causes the other attribute's
                             * option list to be reloaded according to the new value of this attribute. Note that it
                             * is not actually the '.value' of the field but the .fkto property of the current selected
                             * option. This allows the other list to be bound by something other than the PK of this list.
                             */
                        	strSingleOnChange.append("zXOption").append(strRelCtr).append(".onChangeRel(this.options[this.selectedIndex].fkto);");
                        	
                            strSinglePostJavaScript.append("zXOption")
                                                   .append(strRelCtr)
                                                   .append(".onChangeRel(").append(strThisAsElementByName)
                                                   .append(".options[").append(strThisAsElementByName)
                                                   .append(".selectedIndex].fkto);");
                        	
                        }
                    }
                    
                    /**
                     * BD9JAN04 Complex stuff: we have to maintain a collection
                     * of commands that will be added to the generated HTML
                     * by the pageflow class
                     */
                    if (strSinglePostJavaScript.length() > 0) {
                    	/**
                    	 * IE Seems to render this really slowly.
                    	 */
                    	// postLoadJavascript.add(strSinglePostJavaScript.toString());
                    }
                    
                    pstrOnChange.append(strSingleOnChange.toString());
                }
            }
            
            return editFormEnhancers;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate javascript enhancers relating to this attribute.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrOnChange = "+ pstrOnChange);
            }
            if (getZx().throwException) throw new ZXException(e);
            editFormEnhancers = zXType.rc.rcError;
            return editFormEnhancers;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(editFormEnhancers);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate the 'ignore warning' checkbox for edit forms (used in multi-step warning mechanism).
     *
     * @return Returns the return code of the method. 
     */
    public zXType.rc editFormIgnoreWarning() {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc editFormIgnoreWarning = zXType.rc.rcOK; 

        try {
            
            String strTitle = "Check this box to ignore the warning(s)";
            
            this.s.append("<table width='100%'><tr>").appendNL();
            
            this.s.append("<td ")
	            	.appendAttr("class", "zXInfoMsg")
	            	.appendAttr("width", getWebSettings().getEditFormColumn1())
	            	.appendNL('>');
            
            this.s.append("<input type=\"checkbox\" ")
		            .append(" checked ")
		            .appendAttr("name", "zXIgnoreWarning")
		            .appendAttr("title", strTitle).appendNL('>');
            
            if (getZx().getLanguage().equalsIgnoreCase("EN")) {
                this.s.appendNL("Ignore warning(s)");
            }
            
            this.s.appendNL("</td>");
            this.s.appendNL("</tr></table>");
            
            return editFormIgnoreWarning;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(editFormIgnoreWarning);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for form SubTitle.
     *
     * @param penmFormType The form type. 
     * @param pstrSubTitle The sub title 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formSubTitle fails. 
     */
    public zXType.rc formSubTitle(zXType.webFormType penmFormType, String pstrSubTitle) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pstrSubTitle", pstrSubTitle);
        }

        zXType.rc formSubTitle = zXType.rc.rcOK; 
        
        try {
            this.s.append("<table width=\"100%\">").append(HTMLGen.NL);
            this.s.append("<tr>").append(HTMLGen.NL);
            
            /**
             * Force left margin for some form types
             */
            if (penmFormType.equals(zXType.webFormType.wftMenu)) {
                this.s.append("<td ")
	                    .appendAttr("width", getWebSettings().getMenuColumn1())
	                    .appendNL('>');
                this.s.append("</td>").append(HTMLGen.NL);
            }
            
            this.s.append("<td ")
	              .appendAttr("class", "zXFormSubTitle")
	              .appendAttr("width", "*")
	              .appendNL('>');
            this.s.append(pstrSubTitle).append(HTMLGen.NL);
            this.s.append("</td>").append(HTMLGen.NL);
            this.s.append("</tr>").append(HTMLGen.NL);
            this.s.append("</table>").append(HTMLGen.NL);
            
            return formSubTitle;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for form SubTitle.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pstrSubTitle = "+ pstrSubTitle);
            }
            if (getZx().throwException) throw new ZXException(e);
            formSubTitle = zXType.rc.rcError;
            return formSubTitle;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formSubTitle);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for form Spellcheck button.
     *
     * @param pobjBO The business object used. 
     * @param pobjAttr The attribute to add the spell check to. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formSpellcheckButton fails. 
     */
    public zXType.rc formSpellcheckButton(ZXBO pobjBO, Attribute pobjAttr) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
        }
        
        zXType.rc formSpellcheckButton = zXType.rc.rcOK; 
        
        try {
            
            if (getEditEnhancerBoolean(pobjBO, pobjAttr, "spellCheck")) {
                
//                this.s.append("<img ")
//			            .appendAttr("src", "../images/spellCheck.gif"))
//			            .appendAttr("alt", "Spell Check ")).append(tooltip(pobjBO, pobjAttr))
//			            .appendAttr("title", "Spell Check ")).append(tooltip(pobjBO, pobjAttr))
//			            .appendAttr("onMouseDown", "this.style.cursor='wait';window.document.body.style.cursor='wait';javascript:zXSpellCheck(" + controlName(pobjBO, pobjAttr) + ",'" + this.zx.getLanguage() + "');this.style.cursor='';window.document.body.style.cursor=''"))
//			            .appendAttr("onMouseOver", "javascript:this.src='../images/spellCheckOver.gif'"))
//			            .appendAttr("onMouseOut", "javascript:this.src='../images/spellCheck.gif'"))
//			            .appendNL('>');

                this.s.append("<img ")
	            .appendAttr("src", "../images/spellCheck.gif")
	            .appendAttr("alt", "Spell Check " + tooltip(pobjBO, pobjAttr))
	            .appendAttr("title", "Spell Check " + tooltip(pobjBO, pobjAttr))
	            .appendAttr("onMouseDown", "new spellChecker(findObj('" + controlName(pobjBO, pobjAttr) + "')).openChecker();")
	            .appendAttr("onMouseOver", "javascript:this.src='../images/spellCheckOver.gif'")
	            .appendAttr("onMouseOut", "javascript:this.src='../images/spellCheck.gif'")
	            .appendNL('>');

            }
            
            return formSpellcheckButton;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for form Spellcheck button.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
            }
            if (getZx().throwException) throw new ZXException(e);
            formSpellcheckButton = zXType.rc.rcError;
            return formSpellcheckButton;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formSpellcheckButton);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for form FK Lookup button.
     * 
     * <pre>
     * 
     * DGS09APR2003: New function created as part of changes to the way FKs 
     * are presented for medium/large entities.
     * </pre>
     *
     * @param pobjBO The business object used. 
     * @param pobjAttr The attribute for the fklookup button 
     * @param penmSize The size of the look up. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formFKLookupButton fails. 
     */
    public zXType.rc formFKLookupButton(ZXBO pobjBO, Attribute pobjAttr, zXType.entitySize penmSize) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("penmSize", penmSize);
        }

        zXType.rc formFKLookupButton = zXType.rc.rcOK; 
        
        try {
            /**
             * If small entity, the default is not to show the button unless the enhancer is checked.
             * If medium/large entity the default is the opposite way around i.e. enhancer switches it off.
             */
            if ( (penmSize.equals(zXType.entitySize.esSmall)  
                    && getEditEnhancerBoolean(pobjBO, pobjAttr, "fklookup") )
                    ||
                    (!penmSize.equals(zXType.entitySize.esSmall)  
                            && !getEditEnhancerBoolean(pobjBO, pobjAttr, "fklookup") )) {
                this.s.append("<img ")
				      .appendAttr("src", "../images/listItem.gif")
				      .appendAttr("alt", "Show details " + pobjAttr.getLabel().getDescription())
				      .appendAttr("title", "Show details " + pobjAttr.getLabel().getDescription())
				      .appendAttr("onMouseDown", "this.style.cursor='wait';window.document.body.style.cursor='wait';javascript:zXOption" + controlName(pobjBO, pobjAttr) + ".lookup();this.style.cursor='';window.document.body.style.cursor=''")
				      .appendAttr("onMouseOver", "javascript:this.src='../images/listItemOver.gif'")
				      .appendAttr("onMouseOut", "javascript:this.src='../images/listItem.gif'")
				      .appendNL('>');
            }
            
            return formFKLookupButton;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for form FK Lookup button.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : penmSize = "+ penmSize);
            }
            if (getZx().throwException) throw new ZXException(e);
            formFKLookupButton = zXType.rc.rcError;
            return formFKLookupButton;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formFKLookupButton);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for form FK Add button.
     * 
     * <pre>
     * 
     * DGS20AUG2003: New function created.
     * </pre>
     *
     * @param pobjBO The business object 
     * @param pobjAttr The attribute for the button. 
     * @param pobjFKBO The foriegn business object. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formFKAddButton fails. 
     */
    public zXType.rc formFKAddButton(ZXBO pobjBO, Attribute pobjAttr, ZXBO pobjFKBO) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pobjFKBO", pobjFKBO);
        }

        zXType.rc formFKAddButton = zXType.rc.rcOK; 
        
        try {
            
            if (pobjFKBO.mayInsert()) {
                /**
                 * This user can insert this bo...
                 */
                if (getEditEnhancerBoolean(pobjBO, pobjAttr, "fkadd")) {
                    /**
                     * ...and the FKAdd enhancer has been checked:
                     */
                    String strFKObj = pobjFKBO.getDescriptor().getName();
                    
                    this.s.append("<img ")
			              .appendAttr("src", "../images/fknew.gif")
			              .appendAttr("alt", "Create new instance of " + pobjAttr.getLabel().getDescription())
			              .appendAttr("title", "Create new instance of " + pobjAttr.getLabel().getDescription())
			              
			              .appendAttr("onMouseDown", "this.style.cursor='wait';window.document.body.style.cursor='wait';javascript:zXAddShow('"
			              							 + strFKObj + "','zXOption" + controlName(pobjBO, pobjAttr) + "','"
			              							 + this.pageflow.getQs().getEntryAsString("-s") + "');this.style.cursor='';window.document.body.style.cursor=''")
			              
			              .appendAttr("onMouseOver", "javascript:this.src='../images/fknewOver.gif'")
			              .appendAttr("onMouseOut", "javascript:this.src='../images/fknew.gif'")
			              .appendNL('>');
                }
            }
            
            return formFKAddButton;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for form FK Add button.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pobjFKBO = "+ pobjFKBO);
            }
            if (getZx().throwException) throw new ZXException(e);
            formFKAddButton = zXType.rc.rcError;
            return formFKAddButton;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formFKAddButton);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for form Expression edit button.
     *
     * @param pobjBO The business object for the button 
     * @param pobjAttr The attribute linked to the button 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formExprEditButton fails. 
     */
    public zXType.rc formExprEditButton(ZXBO pobjBO, Attribute pobjAttr) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
        }
        
        zXType.rc formExprEditButton = zXType.rc.rcOK; 

        try {
        	
            this.s.append("<img ")
		        .appendAttr("src", "../images/exprEdit.gif")
		        .appendAttr("alt", "Edit expression " + tooltip(pobjBO, pobjAttr))
		        .appendAttr("title", "Edit expression " + tooltip(pobjBO, pobjAttr))
		        .appendAttr("onClick", "javascript:zXExprEdit('" + getZx().getSession().getSessionid() + "','" + controlName(pobjBO, pobjAttr) + "');")
		        .appendAttr("onMouseOver", "javascript:this.src='../images/exprEditOver.gif'")
		        .appendAttr("onMouseOut", "javascript:this.src='../images/exprEdit.gif'")
		        .appendNL('>');
            
            return formExprEditButton;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for form Expression edit button.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
            }
            if (getZx().throwException) throw new ZXException(e);
            formExprEditButton = zXType.rc.rcError;
            return formExprEditButton;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formExprEditButton);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for a boolean entry box that is not locked.
     *
     * @param penmFormType The type of form. 
     * @param pobjBO The business object for the form. 
     * @param pobjAttr The attribute that have the checkbox 
     * @param pstrClass The css class. 
     * @param pstrOnBlur The onBlur javascript action. 
     * @param pstrOnChange The onChange javascript action. 
     * @param pstrOnKeyPress The onKeyPress javascript  action. 
     * @param pblnDisabled Whether the control is enabled or not. 
     * @param pstrTabIndex The tab index 
     * @param pstrOnFocus The onFocus action. 
     * @param pstrOnKeyDown The onKeyDown action. 
     * @param pstrOnKeyUp The onKeyUp action 
     * @param pstrOnClick The onClick action. 
     * @param pstrOnMouseOver The onMouseOver action. 
     * @param pstrOnMouseOut The onMouseOut javascript action. 
     * @param pstrOnMouseDown The OnMouseDown action 
     * @param pstrOnMouseUp The onMouseUp action 
     * @param pstrPostLabel The post label message. 
     * @param pstrPostLabelClass The post label css class.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if formEntryCheckboxControl fails. 
     */
    public zXType.rc formEntryCheckboxControl(zXType.webFormType penmFormType, 
                                                ZXBO pobjBO, 
                                                Attribute pobjAttr, 
                                                String pstrClass, 
                                                String pstrOnBlur, 
                                                String pstrOnChange, 
                                                String pstrOnKeyPress, 
                                                boolean pblnDisabled, 
                                                String pstrTabIndex, 
                                                String pstrOnFocus, 
                                                String pstrOnKeyDown, 
                                                String pstrOnKeyUp, 
                                                String pstrOnClick, 
                                                String pstrOnMouseOver, 
                                                String pstrOnMouseOut, 
                                                String pstrOnMouseDown, 
                                                String pstrOnMouseUp,
                                                String pstrPostLabel,
                                                String pstrPostLabelClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pstrClass", pstrClass);
            getZx().trace.traceParam("pstrOnBlur", pstrOnBlur);
            getZx().trace.traceParam("pstrOnChange", pstrOnChange);
            getZx().trace.traceParam("pstrOnKeyPress", pstrOnKeyPress);
            getZx().trace.traceParam("pblnDisabled", pblnDisabled);
            getZx().trace.traceParam("pstrTabIndex", pstrTabIndex);
            getZx().trace.traceParam("pstrOnFocus", pstrOnFocus);
            getZx().trace.traceParam("pstrOnKeyDown", pstrOnKeyDown);
            getZx().trace.traceParam("pstrOnKeyUp", pstrOnKeyUp);
            getZx().trace.traceParam("pstrOnClick", pstrOnClick);
            getZx().trace.traceParam("pstrOnMouseOver", pstrOnMouseOver);
            getZx().trace.traceParam("pstrOnMouseOut", pstrOnMouseOut);
            getZx().trace.traceParam("pstrOnMouseDown", pstrOnMouseDown);
            getZx().trace.traceParam("pstrOnMouseUp", pstrOnMouseUp);
            getZx().trace.traceParam("pstrPostLabel", pstrPostLabel);
            getZx().trace.traceParam("pstrPostLabelClass", pstrPostLabelClass);
        }
        
        zXType.rc formEntryCheckboxControl = zXType.rc.rcOK;
        
        try {
            /**
             * Do the standard header part of the form entry :
             */
            formEntryHeader(pobjBO, pobjAttr, penmFormType);
            
            /**
             * Add our own little thing to the onChange handle
             */
            String strOnChange = "zXDirty=1;";
            if (StringUtil.len(pstrOnChange) > 0) {
                strOnChange = pstrOnChange + "; " + strOnChange;
            }
            
            this.s.append("<input type=\"checkbox\" ")
		            .append(pobjBO.getValue(pobjAttr.getName()).booleanValue()?" checked ":"")
		            
		            .appendAttr("id", "_id_" + controlName(pobjBO, pobjAttr))
		            .appendAttr("name", controlName(pobjBO, pobjAttr))
		            
		            .appendAttr("class", pstrClass)
		            .appendAttr("disabled", pblnDisabled?"true":"")
		            .appendAttr("tabIndex", pstrTabIndex)
		            .appendAttr("onChange", strOnChange)
		            .appendAttr("onBlur", pstrOnBlur)
		            .appendAttr("onKeyPress", pstrOnKeyPress)
		            .appendAttr("onFocus", pstrOnFocus)
		            .appendAttr("onKeyDown", pstrOnKeyDown)
		            .appendAttr("onKeyUp", pstrOnKeyUp)
		            .appendAttr("onClick", pstrOnClick)
		            .appendAttr("onMouseOver", pstrOnMouseOver)
		            .appendAttr("onMouseOut", pstrOnMouseOut)
		            .appendAttr("onMouseDown", pstrOnMouseDown)
		            .appendAttr("onMouseUp", pstrOnMouseUp)
		            //.appendAttr("title", tooltip(pobjBO, pobjAttr)))
		            .appendNL('>');
            
            /**
             * Handle any enhancer refs
             */
            processFieldRef(penmFormType, pobjBO, pobjAttr);
            
            /**
             * DGS28JUN2004: Handle an enhancer post label
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit) 
               || penmFormType.equals(zXType.webFormType.wftGrid)) {
                if (StringUtil.len(pstrPostLabel) > 0) {
                    if (StringUtil.len(pstrPostLabelClass) == 0) {
                        pstrPostLabelClass = "zXFormLabel";
                    }
                    s.append("<span ").appendAttr("class", pstrPostLabelClass).appendNL('>');
                    s.append(pstrPostLabel).append(HTMLGen.NL);
                    s.append("</span>").append(HTMLGen.NL);
                }
            }
            
            if (!getEditEnhancerBoolean(pobjBO, pobjAttr,"mergeWithNext")) {
                this.s.append("</td>");
            }
            
            return formEntryCheckboxControl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for a boolean entry box that is not locked.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
                getZx().log.error("Parameter : pstrOnBlur = "+ pstrOnBlur);
                getZx().log.error("Parameter : pstrOnChange = "+ pstrOnChange);
                getZx().log.error("Parameter : pstrOnKeyPress = "+ pstrOnKeyPress);
                getZx().log.error("Parameter : pblnDisabled = "+ pblnDisabled);
                getZx().log.error("Parameter : pstrTabIndex = "+ pstrTabIndex);
                getZx().log.error("Parameter : pstrOnFocus = "+ pstrOnFocus);
                getZx().log.error("Parameter : pstrOnKeyDown = "+ pstrOnKeyDown);
                getZx().log.error("Parameter : pstrOnKeyUp = "+ pstrOnKeyUp);
                getZx().log.error("Parameter : pstrOnClick = "+ pstrOnClick);
                getZx().log.error("Parameter : pstrOnMouseOver = "+ pstrOnMouseOver);
                getZx().log.error("Parameter : pstrOnMouseOut = "+ pstrOnMouseOut);
                getZx().log.error("Parameter : pstrOnMouseDown = "+ pstrOnMouseDown);
                getZx().log.error("Parameter : pstrOnMouseUp = "+ pstrOnMouseUp);
            }
            if (getZx().throwException) throw new ZXException(e);
            formEntryCheckboxControl = zXType.rc.rcError;
            return formEntryCheckboxControl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntryCheckboxControl);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for combo box.
     *
     * @param penmFormType The web form type. 
     * @param pobjBO The business object for the form 
     * @param pobjAttr The attribute for the control. 
     * @param pstrClass The css stylesheet class 
     * @param pstrOnBlur The OnBlur javascript action. 
     * @param pstrOnChange The OnChange javascript action 
     * @param pstrOnKeyPress The OnKeyPress javascript action 
     * @param pblnDisabled Whether the control is disabled or not. 
     * @param pstrTabIndex The tag is belongs in. 
     * @param pstrOnFocus The OnFocus javascript action. 
     * @param pstrOnKeyDown The OnKeyDown javascript action. 
     * @param pstrOnKeyUp The OnKeyUp javascript action. 
     * @param pstrOnClick The onClick javasript action 
     * @param pstrOnMouseOver The onMouseOver javascript action. 
     * @param pstrOnMouseOut The onMouseOut javascript action. 
     * @param pstrOnMouseDown The onMouseDown javascript action. 
     * @param pstrOnMouseUp The onMouseUp javascript action. 
     * @param pstrPostLabel  Post label
     * @param pstrPostLabelClass Post label class
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if formEntryComboControl fails. 
     */
    public zXType.rc formEntryComboControl(zXType.webFormType penmFormType, 
                                           ZXBO pobjBO, 
                                           Attribute pobjAttr, 
                                           String pstrClass, 
                                           String pstrOnBlur, 
                                           String pstrOnChange, 
                                           String pstrOnKeyPress, 
                                           boolean pblnDisabled, 
                                           String pstrTabIndex, 
                                           String pstrOnFocus, 
                                           String pstrOnKeyDown, 
                                           String pstrOnKeyUp, 
                                           String pstrOnClick, 
                                           String pstrOnMouseOver, 
                                           String pstrOnMouseOut, 
                                           String pstrOnMouseDown, 
                                           String pstrOnMouseUp,
                                           String pstrPostLabel,
                                           String pstrPostLabelClass) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmFormType", penmFormType);
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pstrClass", pstrClass);
            getZx().trace.traceParam("pstrOnBlur", pstrOnBlur);
            getZx().trace.traceParam("pstrOnChange", pstrOnChange);
            getZx().trace.traceParam("pstrOnKeyPress", pstrOnKeyPress);
            getZx().trace.traceParam("pblnDisabled", pblnDisabled);
            getZx().trace.traceParam("pstrTabIndex", pstrTabIndex);
            getZx().trace.traceParam("pstrOnFocus", pstrOnFocus);
            getZx().trace.traceParam("pstrOnKeyDown", pstrOnKeyDown);
            getZx().trace.traceParam("pstrOnKeyUp", pstrOnKeyUp);
            getZx().trace.traceParam("pstrOnClick", pstrOnClick);
            getZx().trace.traceParam("pstrOnMouseOver", pstrOnMouseOver);
            getZx().trace.traceParam("pstrOnMouseOut", pstrOnMouseOut);
            getZx().trace.traceParam("pstrOnMouseDown", pstrOnMouseDown);
            getZx().trace.traceParam("pstrOnMouseUp", pstrOnMouseUp);
            getZx().trace.traceParam("pstrPostLabel", pstrPostLabel);
            getZx().trace.traceParam("pstrPostLabelClass", pstrPostLabelClass);
        }

        zXType.rc formEntryComboControl = zXType.rc.rcOK; 
        
        try {
            /**
             * Do the standard header part of the form entry :
             */
            formEntryHeader(pobjBO, pobjAttr, penmFormType);
            
            /**
             * DGS10MAR2003: Editform enhancer can override the standard button class
             */
            String strButtonClass = getEditEnhancerProperty(pobjBO, pobjAttr, "stdButtonClass");
            if (StringUtil.len(strButtonClass) == 0) {
                strButtonClass = "zxLookupButton";
            }
            
            /**
             * If edit, we have to put the current value of the field into the box
             */
            String strValue = "";
            DateFormat df;
            
            int intDataType = pobjAttr.getDataType().pos;
            if (penmFormType.equals(zXType.webFormType.wftEdit) && !pobjBO.getValue(pobjAttr.getName()).isNull) {
                if (intDataType == zXType.dataType.dtDate.pos) {
                    df = getZx().getDateFormat();
                    strValue = df.format(pobjBO.getValue(pobjAttr.getName()).dateValue());
                    
                } else if (intDataType == zXType.dataType.dtTimestamp.pos) {
                    df = getZx().getTimestampFormat();
                    strValue = df.format(pobjBO.getValue(pobjAttr.getName()).dateValue());
                    
                } else if (intDataType == zXType.dataType.dtTime.pos) {
                    df = getZx().getTimeFormat();
                    strValue = df.format(pobjBO.getValue(pobjAttr.getName()).dateValue());
                    
                } else {
                    strValue = pobjBO.getValue(pobjAttr.getName()).getStringValue();
                    
                }
            }
            
            /**
             * The onChange event handler is a concatenation of the optional user-provided 
             * one and the standard zX one
             */
            String strOnChange = "this.value = zXOnChange(this.value, " + pobjAttr.getDataType().pos + ");";
            if (StringUtil.len(pstrOnChange) > 0) {
                strOnChange = pstrOnChange + "; " + strOnChange;
            }
            
            /**
             * Same for the keypress event handler
             */
            String strOnKeyPress = "return zXOnKeyPress(event, " + pobjAttr.getDataType().pos +  ");";
            if (StringUtil.len(pstrOnKeyDown) > 0) {
                strOnKeyPress = pstrOnKeyPress + "; " + strOnKeyPress;
            }
            
            /**
             * DGS10MAR2003: Same for more event handlers, definable in editform enhancers
             */
            String strOnKeyDown = "zXComboOnKeyDown(event, this);";
            if (StringUtil.len(pstrOnKeyDown) > 0) {
                strOnKeyDown = pstrOnKeyDown + "; " + strOnKeyDown;
            }
            
            String strOnFocus = "zXComboOnFocus(this);";
            if (StringUtil.len(pstrOnFocus) > 0) {
                strOnFocus = pstrOnFocus + "; " + strOnFocus;
            }
            
            String strControlName = controlName(pobjBO, pobjAttr);
            
            this.s.append("<input ")
                  .appendAttr("id", "_id_" + strControlName)
                  .appendAttr("name", strControlName)
                  .appendAttr("value", StringEscapeUtils.escapeHtml(strValue))
                  .appendAttr("size", pobjAttr.getOutputLength() + "")
                  .appendAttr("maxlength", pobjAttr.getLength() + "")
                  .appendAttr("type", pobjAttr.isPassword()?"password":"text")
                  .appendAttr("class", pstrClass)
                  .appendAttr("disabled", pblnDisabled?"true":"")
                  .appendAttr("tabIndex", pstrTabIndex)
                  .appendAttr("onChange", strOnChange)
                  .appendAttr("onKeypress", strOnKeyPress)
                  .appendAttr("onKeyDown", strOnKeyDown)
                  .appendAttr("onFocus", strOnFocus)
                  .appendAttr("onBlur", pstrOnBlur)
                  .appendAttr("onMouseDown", pstrOnMouseDown)
                  .appendAttr("onClick", pstrOnClick)
                  .appendAttr("onKeyUp", pstrOnKeyUp)
                  .appendAttr("onMouseOver", pstrOnMouseOver)
                  .appendAttr("onMouseOut", pstrOnMouseOut)
                  .appendAttr("onMouseUp", pstrOnMouseUp)
                  .appendAttr("title", tooltip(pobjBO, pobjAttr))
                  .appendNL('>');
            
            if (!pblnDisabled) {
                /**
                 * Now add the combo box values to the combo box value matrix
                 */
                this.s.appendNL("<script type=\"text/javascript\" language=\"JavaScript\">");
                this.s.append("zXComboinput['").append(strControlName).append("'] = new Array();").appendNL();
                
                int j = 0;
                Option objOption;
                Iterator iter = pobjAttr.getOptions().values().iterator();
                while (iter.hasNext()) {
                    objOption = (Option)iter.next();
                    this.s.append("zXComboinput['" + strControlName + "'][" + j + "] =")
                          .append("'").append(StringEscapeUtils.escapeJavaScript(objOption.getValue())).append("';").appendNL();
                    j++;
                }
                
                this.s.appendNL("</script>");
                
                /**
                 * Add little icon to show that this is a combo-box
                 */
                this.s.append("<img src='../images/combo.gif'")
		              .appendAttr("title", "Combobopx, click to get available values")
		              .appendAttr("onClick", "zXComboUp(findObj('" + strControlName + "', this.window, this.document));")
		              .appendNL('>');
                
                /**
                 * In case of a date field: add the calendar button
                 */
                if (intDataType == zXType.dataType.dtDate.pos) {
                    /**
                     * Inlined calendar :
                     * This will be presented with the same page at the form
                     */
                	buildDateButton(pobjBO, pobjAttr, "_id_" + strControlName, pblnDisabled);
                }
            }
            
            /**
             * Handle any enhancer refs
             */
            processFieldRef(penmFormType, pobjBO, pobjAttr);
            
            
            /**
             * DGS28JUN2004: Handle an enhancer post label
             */
            if (penmFormType.equals(zXType.webFormType.wftEdit) 
               || penmFormType.equals(zXType.webFormType.wftGrid)) {
                if (StringUtil.len(pstrPostLabel) > 0) {
                	if (StringUtil.len(pstrPostLabelClass) == 0) {
                        pstrPostLabelClass = "zXFormLabel";
                    }
                    
                    s.append("<span ").appendAttr("class", pstrPostLabelClass).appendNL('>');
                    s.appendNL(pstrPostLabel);
                    s.appendNL("</span>");
                }
            }
            
            /**
             * DGS28JAN2004: If in a search form we need a span here, although there's never
             * anything in it for this kind of control. Otherwise the onChange JS doesn't work.
             */
            if (penmFormType.equals(zXType.webFormType.wftSearch)) {
                this.s.append("<span").appendNL()
                	  .appendAttr("id", "zXSpan" + strControlName).appendNL()
                      .appendAttr("style", "display:none").appendNL()
                      .appendNL('>');
                this.s.appendNL("</span>");
            }
            
            if (!getEditEnhancerBoolean(pobjBO, pobjAttr,"mergeWithNext")) {
                this.s.appendNL("</td>");
            }
            
            return formEntryComboControl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for combo box.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmFormType = "+ penmFormType);
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
                getZx().log.error("Parameter : pstrOnBlur = "+ pstrOnBlur);
                getZx().log.error("Parameter : pstrOnChange = "+ pstrOnChange);
                getZx().log.error("Parameter : pstrOnKeyPress = "+ pstrOnKeyPress);
                getZx().log.error("Parameter : pblnDisabled = "+ pblnDisabled);
                getZx().log.error("Parameter : pstrTabIndex = "+ pstrTabIndex);
                getZx().log.error("Parameter : pstrOnFocus = "+ pstrOnFocus);
                getZx().log.error("Parameter : pstrOnKeyDown = "+ pstrOnKeyDown);
                getZx().log.error("Parameter : pstrOnKeyUp = "+ pstrOnKeyUp);
                getZx().log.error("Parameter : pstrOnClick = "+ pstrOnClick);
                getZx().log.error("Parameter : pstrOnMouseOver = "+ pstrOnMouseOver);
                getZx().log.error("Parameter : pstrOnMouseOut = "+ pstrOnMouseOut);
                getZx().log.error("Parameter : pstrOnMouseDown = "+ pstrOnMouseDown);
                getZx().log.error("Parameter : pstrOnMouseUp = "+ pstrOnMouseUp);
                getZx().log.error("Parameter : pstrPostLabel = "+ pstrPostLabel);
                getZx().log.error("Parameter : pstrPostLabelClass = "+ pstrPostLabelClass);
            }
            if (getZx().throwException) throw new ZXException(e);
            formEntryComboControl = zXType.rc.rcError;
            return formEntryComboControl;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(formEntryComboControl);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate the HTML to close a matrix vertical axis leftmost cell (for labels etc.).
     */
    public void matrixAxisClose() {
    	this.s.appendNL("</tr></table></td>");
    }
    
    /**
     * Generate the HTML to open the matrix vertical axis leftmost cell (for row labels etc.).
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with matrixAxisClose and other list-type functions
     * </pre>
     *
     * @param pstrRowsLabelsWidth The width of the labels. 
     */
    public void matrixAxisOpen(String pstrRowsLabelsWidth) {
    	this.s.appendNL("<td width='" + pstrRowsLabelsWidth + "'><table width='100%'>");
    }
    
    /**
     * Generate the HTML to close a matrix row.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with matrixCellOpen and others
     * </pre>
     * 
     */
    public void matrixCellClose() {
    	this.s.appendNL("</tr></table></td>");
    }
    
    /**
     * Generate the HTML to open a matrix cell.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with many others
     *    Table has already been opened
     * </pre>
     *
     * @param pblnEvenRow Used to determine the color of the row (unless optional class also given). 
     * @param pstrClass Optional class - if not given will use zxNor and zxAlt alternately 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if matrixCellOpen fails. 
     */
    public zXType.rc matrixCellOpen(boolean pblnEvenRow, String pstrClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pblnEvenRow", pblnEvenRow);
            getZx().trace.traceParam("pstrClass", pstrClass);
        }
        
        zXType.rc matrixCellOpen = zXType.rc.rcOK; 

        try {
        	this.s.append("<td><table width='100%' ");
        	
            if (StringUtil.len(pstrClass) == 0) {
            	this.s.appendAttr("class", pblnEvenRow?"zxNor":"zxAlt");
            } else {
            	this.s.appendAttr("class", pstrClass);
            }
            
            this.s.append("><tr>").append(HTMLGen.NL);
            
            return matrixCellOpen;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate the HTML to open a matrix cell.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pblnEvenRow = "+ pblnEvenRow);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
            }
            if (getZx().throwException) throw new ZXException(e);
            matrixCellOpen = zXType.rc.rcError;
            return matrixCellOpen;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(matrixCellOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for matrix header BO columns bit.
     *
     *<pre>
     *
     * Assumes   : 
     *   Used in combination with 
     *   ListHeaderOpen and ListHeaderClose
     *</pre>
     *
     * @param pcolEntities Collection of PFEntities 
     * @param pblnResolveFK Whether to resolve the foriegn keys 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if matrixHeader fails. 
     */
    public zXType.rc matrixHeader(ZXCollection pcolEntities, boolean pblnResolveFK) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pcolEntities", pcolEntities);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
        }
        
        zXType.rc matrixHeader = zXType.rc.rcOK; 

        try {
            this.s.append("<td ")
                  .appendAttr("class", "zxListHeader")
                  .appendNL('>');
            
            /**
             * Note that we don't do anything with 'resolve FK' that is passed into this function.
             * Perhaps we should loop through each attr in the listgroup and see if we need to
             * resolve it as an FK (see listHeader above). Or is it done already through the query?
             */
            Iterator iter = pcolEntities.iterator();
            PFEntity objEntity;
            StringBuffer strColHeading = new StringBuffer();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                if (StringUtil.len(objEntity.getListgroup()) > 0) {
                    strColHeading.append(objEntity.getBo().formattedString(objEntity.getListgroup())).append(" ");
                }
            }
            this.s.append(strColHeading);
            
            this.s.append("</td>").append(HTMLGen.NL);
            
            return matrixHeader;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for matrix header BO columns bit.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pcolEntities = "+ pcolEntities);
                getZx().log.error("Parameter : pblnResolveFK = "+ pblnResolveFK);
            }
            if (getZx().throwException) throw new ZXException(e);
            matrixHeader = zXType.rc.rcError;
            return matrixHeader;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(matrixHeader);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate the HTML to a matrixRow.
     *
     *<pre>
     *
     *Assumes   :
     *   Used in combination with matrixRowClose and matrixRowOpen
     *</pre>
     *
     * @param pobjBO The business object for the row. 
     * @param pstrGroup The attribute group for the list 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if matrixRow fails. 
     */
    public zXType.rc matrixRow(ZXBO pobjBO, String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        zXType.rc matrixRow = zXType.rc.rcOK; 

        try {
            /**
             * Try prepersist
             */
            if (pobjBO.prePersist(zXType.persistAction.paListRow, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of pre-persist");
            }
            
            this.s.append("<td>").append(pobjBO.formattedString(pstrGroup)).append("</td>").append(HTMLGen.NL);
            
            /**
             * Try postpersist
             */
            pobjBO.setLastPostPersistRC(matrixRow);
            if (pobjBO.postPersist(zXType.persistAction.paListRow, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of post-persist");
            }
            matrixRow = pobjBO.getLastPostPersistRC();
            
            return matrixRow;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate the HTML to a matrixRow.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            matrixRow = zXType.rc.rcError;
            return matrixRow;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(matrixRow);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
    * Generate the HTML to close a matrix row.
    * 
    * <pre>
    * 
    * Assumes   :
    *    Used in combination with matrixRowOpen, matrixRow and others by MatrixEditForm
    * </pre>
    */
    public void matrixRowClose() {
        this.s.appendNL("</tr>");
    }
    
    /**
     * Generate the HTML to start a new matrix row.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with matrixRow and matrixRowClose, and others (used by MatrixEditForm)
     * </pre>
     *
     * @param pblnEvenRow Used to determine the color of the row (unless optional class also given). 
     * @param pstrClass Optional class - if not given will use zxNor and zxAlt alternately. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if matrixRowOpen fails. 
     */
    public zXType.rc matrixRowOpen(boolean pblnEvenRow, String pstrClass) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pblnEvenRow", pblnEvenRow);
            getZx().trace.traceParam("pstrClass", pstrClass);
        }

        zXType.rc matrixRowOpen = zXType.rc.rcOK; 
        
        try {
            this.s.append("<tr ");
            
            if (StringUtil.len(pstrClass) == 0) {
                this.s.appendAttr("class", pblnEvenRow?"zxNor":"zxAlt");
            } else {
            	this.s.appendAttr("class", pstrClass);
            }
            
            this.s.appendNL('>');
            
            return matrixRowOpen;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate the HTML to start a new matrix row.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pblnEvenRow = "+ pblnEvenRow);
                getZx().log.error("Parameter : pstrClass = "+ pstrClass);
            }
            if (getZx().throwException) throw new ZXException(e);
            matrixRowOpen = zXType.rc.rcError;
            return matrixRowOpen;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(matrixRowOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for an edit form.
     *
     * @param pobjBO The business object for the form 
     * @param pstrGroup The attribute group used 
     * @param pstrLockGroup The locked attribute group. Optional, and default should be null. 
     * @param pstrVisibleGroup The visible attribute group. Optional, the default should be "" 
     * @param pblnVerticallyStacked Whether the elements are vertically stacked. Optional, the default should be false. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if matrixEditForm fails. 
     */
    public zXType.rc matrixEditForm(ZXBO pobjBO, 
    								String pstrGroup, 
    								String pstrLockGroup, 
    								String pstrVisibleGroup, 
    								boolean pblnVerticallyStacked) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrLockGroup", pstrLockGroup);
            getZx().trace.traceParam("pstrVisibleGroup", pstrVisibleGroup);
            getZx().trace.traceParam("pblnVerticallyStacked", pblnVerticallyStacked);
        }

        zXType.rc matrixEditForm = zXType.rc.rcOK; 
        
        try {
            /**
             * Do a pre-persist to allow user to do some special handling
             */
            if (pobjBO.prePersist(zXType.persistAction.paEditForm, pstrGroup).pos == zXType.rc.rcError.pos) {
                getZx().trace.addError("Exit on request of pre-persist");
            }
            
            /**
             * If the visible group is not given, assume that all attributes should be
             *  visible
             * DGS19FEB2004: No, unlike other edit forms, must explicitly specify the visible
             * group. If it is blank we will show a checkbox which means that the 'cell' entity
             * exists if checked, but there is no data to enter for it.
             * If Len(pstrVisibleGroup) = 0 Then
             * pstrVisibleGroup = pstrGroup
             * End If
             */
            
            /**
             * Get handle to the attribute groups
             * If auditable, add the audit columns to the group
             * 
             * BD9JUN04 - Now concurrency control
             */
            AttributeCollection colAttr;
             if (pobjBO.getDescriptor().isConcurrencyControl()) {
                 colAttr = pobjBO.getDescriptor().getGroup(pstrGroup + ",~");
            } else {
                colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            }
            
             AttributeCollection colLockAttr = pobjBO.getDescriptor().getGroup(pstrLockGroup);
             AttributeCollection colVisibleAttr = pobjBO.getDescriptor().getGroup(pstrVisibleGroup);
             
             if (colAttr == null || colLockAttr == null || colVisibleAttr == null) {
                throw new Exception("Unable to retrieve attr / lock or visible group");
             }
             
             /**
              * BD28FEB05 - V1.4:53 - Use sequence of visible attribute group rather
              * than sequence of edit group if applicable
              */
             if (StringUtil.len(pstrVisibleGroup) > 0 && !pstrGroup.equalsIgnoreCase(pstrVisibleGroup)) {
                 colAttr = forceEditGroupSequence(colAttr, colVisibleAttr);
                 if (colAttr == null) {
                     matrixEditForm = zXType.rc.rcError;
                     return matrixEditForm;
                 }
             } // Need to force sequence of edit group
             
             boolean blnMayUpdate = true;
             if (pobjBO.getPersistStatus().equals(zXType.persistStatus.psNew)) {
                 blnMayUpdate = pobjBO.mayUpdate();
             }
             
             this.s.append("<div style='display:none'>").append(HTMLGen.NL);
             this.s.append("<input type=\"hidden\" ")
			       .appendAttr("name", "zXMatrixPersist" + pobjBO.getDescriptor().getAlias())
			       .appendAttr("value", zXType.valueOf(pobjBO.getPersistStatus()))
			       .appendNL('>');
             this.s.append("</div>").append(HTMLGen.NL);
             
             /**
              * DGS19FEB2004: If nothing visible, show a checkbox that just means that the row in the
              * database exists of not. The user can check or uncheck to insert or delete. Still want
              * to continue to call gridFormEntry for each attr in the selectEdit group, but in this
              * case they will all be invisible. Important to do this for PK for example.
              */
             if (colVisibleAttr.size() == 0) {
                 this.s.append("<td ")
	                   .appendAttr("width", getWebSettings().getEditFormColumn2())
	                   .appendAttr("align", "center")
	                   .appendNL('>');
                 this.s.append("<input type=\"checkbox\" ")
                       .append(!pobjBO.getPersistStatus().equals(zXType.persistStatus.psNew)?" checked ":"")
                       .appendAttr("name", "zXMatrixCheck" + controlName(pobjBO))
                       .appendAttr("class", "zxFormInputOptional")
                       .appendAttr("title", pobjBO.getDescriptor().getLabel().getDescription())
                       .appendNL('>');
                 this.s.append("</td>").append(HTMLGen.NL);
             }
             
             Attribute objAttr;
             String strOnChange;
             boolean blnLocked = false;
             boolean blnVisible = false;
             
             boolean blnIsPersistStatusNew = pobjBO.getPersistStatus().equals(zXType.persistStatus.psNew);
             
             Iterator iter = colAttr.iterator();
             while (iter.hasNext()) {
                 objAttr = (Attribute)iter.next();
                 
                 /**
                  * Figure out whether this attribute is locked. This can be because
                  * - it is in the lockGroup
                  * - the attribute is an automatic
                  * - the attribute is the primary key and has already been set
                  * - the attribute is marked as locked
                  * - overriding all this - if the user may not update this BO
                  */
                 strOnChange = "";
                 if (colLockAttr.inGroup(objAttr.getName())) {
                     blnLocked = true;
                     
                 } else if (objAttr.getName().equalsIgnoreCase(pobjBO.getDescriptor().getPrimaryKey()) &&
                            !blnIsPersistStatusNew) {
                     blnLocked = true;
                     
                 } else if (objAttr.getDataType().equals(zXType.dataType.dtAutomatic)) {
                     blnLocked = true;
                     
                 } else {
                     blnLocked = objAttr.isLocked();
                 }
                 
                 if (!pobjBO.getPersistStatus().equals(zXType.persistStatus.psNew)) {
                     if (!blnMayUpdate) {
                         blnLocked = true;
                     }
                 }
                 
                 /**
                  * Figure out whether this attribute is visible
                  */
                 if (!colVisibleAttr.inGroup(objAttr.getName())) {
                     blnVisible = false;
                 } else {
                     blnVisible = true;
                 }
                 
                 /**
                  * Now we have enough information to generate an entry
                  */
                 gridFormEntry(pobjBO, objAttr, blnLocked, blnVisible, null, strOnChange, null);
                 
                 /**
                  *  If vertically stacked fields required, achieve this by closing and opening a row
                  */
                 if (pblnVerticallyStacked && blnVisible) {
                     this.s.append("</tr><tr>").append(HTMLGen.NL);
                 }
             }
             
             /**
              * Do a post-persist to allow user to do some special handling
              */
             pobjBO.setLastPostPersistRC(matrixEditForm);
             if (pobjBO.postPersist(zXType.persistAction.paEditForm, pstrGroup).pos == zXType.rc.rcError.pos) {
                 getZx().trace.addError("Exit on request of post-persist");
             }
             matrixEditForm = pobjBO.getLastPostPersistRC();
             
            return matrixEditForm;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for an edit form.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
                getZx().log.error("Parameter : pstrLockGroup = "+ pstrLockGroup);
                getZx().log.error("Parameter : pstrVisibleGroup = "+ pstrVisibleGroup);
                getZx().log.error("Parameter : pblnVerticallyStacked = "+ pblnVerticallyStacked);
            }
            if (getZx().throwException) throw new ZXException(e);
            matrixEditForm = zXType.rc.rcError;
            return matrixEditForm;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(matrixEditForm);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Initiates the javascript for an options list.
     * 
     * <pre>
     * 
     * Also sees if this attr is a dependant of another.
     * If so, the related (parent, or From) attr's PK will be recorded along with each options row for
     * this (the child or To) attr.
     * DGS07APR2003: Additional parameters to capture dependency 'Restrict' info.
     * DGS20OCT2003: Added foreign key where clause
     * DGS18FEB2005 V1.4:43 Added pstrBoundAttr - this is the attribute that this one is bound to,
     * in terms of its name as an attribute of this BO, not when used in FK BOs etc.
     * BD1JUL05 - V1.5:20 - Added support for enhanced FK labels
     * 
     * Out       :
     * 		String - FK From attribute name (if the field is bound upwards)
     * 		String - FK To attribute name (if the field is bound downwards)
     * 		Boolean - is the field bound
     * </pre>
     *
     * @param pobjBO The business object used. 
     * @param pobjAttr The attribute used. 
     * @param pblnOpt is the field optional 
     * @param pblnGenerateJS Whether or not to generate any javascript.
     * @return Returns whether the field is bound
     * @throws ZXException Thrown if editFormOptions fails. 
     */
    private EditFormOptions initEditFormOptions(ZXBO pobjBO, Attribute pobjAttr, boolean pblnOpt) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pblnOpt", pblnOpt);
        }
        
        EditFormOptions initEditFormOptions = new EditFormOptions();
        
        try {
            PFEditEnhancer objEditEnhancer;
            PFEditDependency objEditDependency;
            PFEntity objEntity;
            ZXBO objFKBOFrom;
            ZXBO objFKBOTo;
            
            /**
             * If this attr is bound UP (from) another attr's enhancer, pass back the attr name
             * that is the fk to the other enhancer.
             */
            List colEditEnhancersDependent = pobjBO.getEditEnhancersDependant(pobjAttr.getName());
            if (colEditEnhancersDependent != null) {
            	
                int intEditEnhancersDependent = colEditEnhancersDependent.size();
                for (int i = 0; i < intEditEnhancersDependent; i++) {
                    objEditEnhancer = (PFEditEnhancer)colEditEnhancersDependent.get(i);
                    
                    int intEditDependency = objEditEnhancer.getEditdependencies().size();
                    for (int j = 0; j < intEditDependency; j++) {
                        objEditDependency = (PFEditDependency)objEditEnhancer.getEditdependencies().get(j);
                        
                        /**
                         * Need to be sure that this attribute is really a bound attribute
                         */
                        if (objEditDependency.getDepType().equals(zXType.pageflowDependencyType.pdtBound) 
                            && pobjAttr.getName().equalsIgnoreCase(objEditDependency.getDepattr())) {
                        	
                            initEditFormOptions.setBound(true);
                            initEditFormOptions.setFKFromAttr(this.pageflow.resolveDirector(objEditDependency.getFkfromattr()));
                            
                            /**
                             * DGS18FEB2005 V1.4:43: Pass back the name of the attr that this one is bound to,
                             * in terms of its name as an attribute of this BO, not when used in FK BOs etc.
                             */
                            initEditFormOptions.setBoundAttr(objEditEnhancer.getAttr());
                            
                            if (StringUtil.len(initEditFormOptions.getFKFromAttr()) == 0) { // ' Or Len(objEditDependency.FKToAttr) = 0 Then
                                /**
                                 * Don't have the FK from attr name, so will need to get it. To do this we
                                 * have to get the BO of the parent (enhancer) and child (dependant) fields,
                                 * then look for the FK attr that relates the two (using BOS.getFKAttr)
                                 */
                                objEntity = (PFEntity)this.pageflow.getContextEntities().get(this.pageflow.resolveDirector(objEditEnhancer.getEntity()));
                                objFKBOFrom = objEntity.getBo().getValue(this.pageflow.resolveDirector(objEditEnhancer.getAttr())).getFKBO();
                                if (objFKBOFrom == null) {
                                    throw new Exception("Unable to get handle to FK From object " + objEditEnhancer.getAttr());
                                }
                                
                                objEntity = (PFEntity)this.pageflow.getContextEntities().get(this.pageflow.resolveDirector(objEditDependency.getDepentity()));
                                objFKBOTo = objEntity.getBo().getValue(this.pageflow.resolveDirector(objEditDependency.getDepattr())).getFKBO();
                                if (objFKBOTo == null) {
                                    throw new Exception("Unable to get handle to FK To object " + objEditDependency.getDepattr());
                                }
                                
                                initEditFormOptions.setFKFromAttr(objFKBOTo.getFKAttr(pobjBO).getName());
                            }
                            
                            /**
                             * No need to keep looking for dependencies - we can't handle more than one where
                             * this attr is the dependant:
                             */
                            break;
                        }
                    }
                }
                
            } // Process any edit enchancer dependents
            
            /**
             * If this attr is bound DOWN (to) a dependant attr, pass back the TO FK attr name (if any)
             * DGS07APR2003: Also pass back 'Restrict' details if applicable.
             */
            List colEditEnhancers = pobjBO.getEditEnhancers(pobjAttr.getName());
            if (colEditEnhancers == null) return initEditFormOptions;
            
            int intEditEnhancers = colEditEnhancers.size();
            for (int i = 0; i < intEditEnhancers; i++) {
                objEditEnhancer = (PFEditEnhancer)colEditEnhancers.get(i);
                
                int intEditDependency = objEditEnhancer.getEditdependencies().size();
                for (int j = 0; j < intEditDependency; j++) {
                    objEditDependency = (PFEditDependency)objEditEnhancer.getEditdependencies().get(j);
                    
                    if (objEditDependency.getDepType().equals(zXType.pageflowDependencyType.pdtBound)) {
                        initEditFormOptions.setFKToAttr(this.pageflow.resolveDirector(objEditDependency.getFktoattr()));
                    } else if (objEditDependency.getDepType().equals(zXType.pageflowDependencyType.pdtRestrict)) {
                        initEditFormOptions.setRestrict(true);
                        initEditFormOptions.setRestrictFKFromAttr(this.pageflow.resolveDirector(objEditDependency.getFkfromattr()));
                        initEditFormOptions.setRestrictOp(objEditDependency.getOpeRator()); 
                        initEditFormOptions.setRestrictValue(this.pageflow.resolveDirector(objEditDependency.getValue()));
                    }
                }
                
                /**
                 * DGS 20OCT2003: Added FK where clause
                 */
                initEditFormOptions.setFKWhere(this.pageflow.resolveDirector(objEditEnhancer.getFkwhere()));
            }
            
            return initEditFormOptions;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Initiates the javascript for an options list.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : pblnOpt = "+ pblnOpt);
            }
            if (getZx().throwException) throw new ZXException(e);
            return initEditFormOptions;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(initEditFormOptions);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate the JS to make editOptions work
     * 
     * V1.5:83 - Introduced after splitting up editFormOptions
     * 
     * @param pobjBO The business object with the enhancer
     * @param pobjAttr The attribute with the enhancer
     * @param pblnOpt Whether is it optional
     * @param pblnBound Whether it is bound
     * @return Returns the return code of the method.
     */
    private zXType.rc editFormOptionsJS(ZXBO pobjBO, Attribute pobjAttr, boolean pblnOpt, boolean pblnBound) {
    	zXType.rc editFormOptionsJS = zXType.rc.rcOK;
    	
    	String strControlName = controlName(pobjBO, pobjAttr);
    	
        /**
         * DGS07APR2003: Additional parameters, particularly used when calling zXFKPopup.
         * Always generate an options javascript object. The parameters are:
         *  - the object's own name (useful within the prototype classes)
         *  - is this attribute optional
         *  - is this attribute bound to another
         *  - the control's name
         *  - the session id
         */
        this.s.append("<script type=\"text/javascript\" language=\"JavaScript\">").appendNL();
        this.s.append("var zXOption").append(strControlName).append(" = new zXOption(").appendNL();
        // _name
        this.s.append("'zXOption").append(strControlName).append("',").appendNL();
        // _opt
        this.s.append(pblnOpt?"true":"false").append(",").appendNL();
        // _bnd - Whether to bound control
        this.s.append(pblnBound?"true":"false").append(",").appendNL();
        // _ctr - Control name
        this.s.append("'").append("_id_" + strControlName).append("',").appendNL();
        
        // JAVASCRIPT CHANGES
        // _e - Entity with the foriegn key
        this.s.append("'").append(pobjBO.getDescriptor().getName()).append("',").appendNL();
        // _att - The attribute with the foriegn key.
        this.s.append("'").append(pobjAttr.getName()).append("',").appendNL();
        // JAVASCRIPT CHANGES
        
        // _session - The session id.
        this.s.append("'").append(getZx().getSession().getSessionid()).append("'").appendNL();
        this.s.append(");").appendNL();
        
        /**
         * Don't include the closing </script> tag here because after calling this function we
         * we will further javascript calls to add each option row.
         */
        return editFormOptionsJS;
    }
    
    /**
     * Generate HTML for list header BO columns bit.
     * 
     * <pre>
     * Same as listHeader expect we want to submit the details.
     * 
     * Assumes   :
     * Used in combination with
     * GridHeader and GridHeaderOpen
     * 
     * TODO : BUG in COM+ C1.5. Need to merge this back to the COM+ version.
     * 
     *  - Reviewed for V1.5:20 - BD1JUL05
     *  - Reviewed for V1.5:21 - BD1JUL05
     * </pre>
     *
     * @param pobjBO The business object associated with the grid. 
     * @param pstrGroup The attribute group to restricted it by 
     * @param pstrVisibleGroup The visible attribute group. Optional, the default should be "*" 
     * @param pstrSortURLBase The resorturl for a grid edit form.
     * @param pblnResolveFK Whether to resolve the foriegn key. Optional, the default should be true.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if gridHeader fails. 
     */
    public zXType.rc gridHeader(ZXBO pobjBO, 
                                String pstrGroup, 
                                String pstrVisibleGroup, 
                                String pstrSortURLBase, 
                                boolean pblnResolveFK) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrVisibleGroup", pstrVisibleGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
        }

        zXType.rc gridHeader = zXType.rc.rcOK;

        /**
         * Handle defaults :
         */
        if (pstrVisibleGroup == null) {
            pstrVisibleGroup = "*";
        }
        
        try {
        	/**
             * Get DS handler and order by attribute group
             */
            DSHandler objDSHandler = pobjBO.getDS();
            
            /**
             * Try to resolve the database type when applicable.
             */
            zXType.databaseType enmDBType = null;
            if (objDSHandler instanceof DSHRdbms) {
            	enmDBType = ((DSHRdbms)objDSHandler).getDbType();
            }
            
            AttributeCollection colOrderByGroup = pobjBO.getDescriptor().getGroup(objDSHandler.getOrderGroup());
            
            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) {
            	throw new Exception("Unable to get group");
            }
            
            AttributeCollection colVisibleAttr = null;
            if (StringUtil.len(pstrVisibleGroup) > 0) {
            	colVisibleAttr = pobjBO.getDescriptor().getGroup(pstrVisibleGroup);
            	if (colVisibleAttr == null) {
            		throw new ZXException("Failed to get visible group.", pstrVisibleGroup);
            	}
            	
                /**
                 * BD28FEB05 - V1.4:53 - Use sequence of visible attribute group
                 * rather than sequence of edit group if applicable
                 */
                if (!pstrGroup.equalsIgnoreCase(pstrVisibleGroup)) {
                	colAttr = forceEditGroupSequence(colAttr, colVisibleAttr);
                	if (colAttr == null) {
                    	gridHeader = zXType.rc.rcError;
                    	return gridHeader;
                	}
                } // Need to force sequence of edit group
            }
            
            Attribute objAttr;
            Attribute objSortAttr;
            boolean blnVisible = true;
            String strColumnName = "";
            ZXBO objFKBO;
            Descriptor objFKDesc;
            AttributeCollection colFKAttr;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if (colVisibleAttr != null) {
                	if (colVisibleAttr.inGroup(objAttr.getName())) {
                		blnVisible = true;
                	} else {
                		blnVisible = false;
                	}
                }
                
                if (blnVisible) {
                    /**
                     * NOTE : Using "NOWRAP" makes most GridEditForms look
                     * better as it prevents wrapping of longer fields.
                     */
                	this.s.append("<td ").appendAttr("class", "zxListHeader").append(" NOWRAP>").append(HTMLGen.NL);
                	this.s.append(objAttr.getLabel().getLabel()).append(HTMLGen.NL);
                	
                    /**
                     * Optionally, we may have to add the up / down sort
                     * columns; sorting is only relevant when sorting is allowed
                     * and the order-by-group has any attributes
                     */
                    if (StringUtil.len(pstrSortURLBase) > 0
                        && (objDSHandler.getOrderSupport().pos != zXType.dsOrderSupport.dsosNone.pos 
                        && colOrderByGroup.size() > 0)) {
                    	
                    	/**
                    	 * May be possible to have a sortAttr set; this means we use
                    	 * that attribute for sort purposes
                    	 */
                    	if (StringUtil.len(objAttr.getSortAttr()) > 0) {
                    		objSortAttr = pobjBO.getDescriptor().getAttribute(objAttr.getSortAttr());
                    		if(objSortAttr == null) {
                    			throw new ZXException("Unable to retrieve sort attribute", objAttr.getSortAttr());
                    		}
                    	} else {
                    		objSortAttr = objAttr;
                    	}
                    	
                        /**
                         * Different for ordinary and foreign key: - Ordinary:
                         * Simply order by column name - Foreign key: Order by
                         * first attr.column of FK label group (if resolveFK
                         * set) Also, special treatment for channels
                         * 
                         * Order by is not relevant when the column is not in
                         * the order by group
                         */
                        if (colOrderByGroup.inGroup(objSortAttr.getName())) {
                        	/**
                        	 * Different for ordinary and foreign key:
                             *  - Ordinary: Simply order by column name
                             *  - Foreign key: Order by first attr.column of FK label group (if resolveFK set)
                             *
                             * Also, special treatment for channels
                             * 
                             * Order by is not relevant when the column is not in the order by group
                             */
                        	if (StringUtil.len(objSortAttr.getForeignKey()) == 0
                                || !pblnResolveFK
                                || objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                        		/**
                                 * For a channel we simply include the attribute
                                 * name, for RDBMS the column name
                                 */
                            	if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                            		strColumnName = objSortAttr.getName();
                                    
                                } else {
                                	strColumnName = getZx().getSql().columnName(pobjBO,
                                    											objSortAttr,
                                                                                zXType.sqlObjectName.sonName);
                                    /**
                                     * Deal with case-insensitive sort
                                     */
                                    if (objSortAttr.getTextCase().pos == zXType.textCase.tcInsensitive.pos
                                    	&& objSortAttr.getDataType().pos == zXType.dataType.dtString.pos) {
                                    	strColumnName = getZx().getSql().makeCaseInsensitive(strColumnName, enmDBType);
                                    }
                                }
                            	
                            } else {
                                /**
                                 * Has FK so user wants to sort on label of FK
                                 * (for RDBMS)
                                 */
                                objFKBO = pobjBO.getValue(objSortAttr.getName()).getFKBO();

                                if (objFKBO == null) {
                                    strColumnName = "";
                                    
                                } else {
                                    objFKDesc = objFKBO.getDescriptor();
                                    
                                    if (StringUtil.len(objSortAttr.getFkLabelGroup()) == 0) {
                                    	colFKAttr = objFKDesc.getGroup("label");
                                    } else {
                                    	colFKAttr = objFKDesc.getGroup(objSortAttr.getFkLabelGroup());
                                    }
                                    
                                    if (colFKAttr == null) {
                                        strColumnName = "";
                                        
                                    } else {
                                        /**
                                         * Check that first attr is associated
                                         * with a column
                                         */
                                        Attribute objAttr1 = (Attribute)colFKAttr.iterator().next();
                                        if (StringUtil.len(objAttr1.getColumn()) > 0) {
                                            strColumnName = getZx().getSql().columnName(objFKBO,
                                                                                        objAttr1,
                                                                                        zXType.sqlObjectName.sonName);

                                            /**
                                             * Deal with case-insensitive sort
                                             */
                                            if (objAttr1.getTextCase().equals(zXType.textCase.tcInsensitive)
                                                && objAttr1.getDataType().pos == zXType.dataType.dtString.pos) {
                                                strColumnName = getZx().getSql().makeCaseInsensitive(strColumnName, enmDBType);
                                            }
                                            
                                        } else {
                                            strColumnName = "";
                                            
                                        } // First attr is associated with column
                                        
                                    } // Has label attribute group
                                    
                                } // Has FKBO
                                
                            } // FK and resolveFK
                            
                        } // Attribute in colOrderByGroup?
                        
                        if (StringUtil.len(strColumnName) > 0) {
                            if (objDSHandler.getOrderSupport().pos > zXType.dsOrderSupport.dsosSimple.pos) {
                                /**
                                 * First determine the postFix; if this routine is
                                 * called from a pageflow it may require the resort
                                 * entries added to the querystring; these should be
                                 * post-fixed with the pageflow / action name as
                                 * this makes it possible to have multiple list
                                 * forms on a single page
                                 */
                                String strPostFix = "";
                                if (this.pageflow != null) {
                                    strPostFix = this.pageflow.QSSortKeyPostFix();
                                }
                                
                                /**
                                 * Down button.
                                 * Should submit details to allow for a createUpdate
                                 * This is NOT available if the orderSupport is simple
                                 */
                                String strUrl = "zXSubmitThisUrl('"
                                                + pstrSortURLBase + "&-oa" + strPostFix
                                                + "=" + strColumnName + "&-od" + strPostFix
                                                + "=asc');";
                                
                                s.append("&nbsp;");
                                
                                s.append("<img ")
                                 .appendAttr("src","../images/sortDown.gif")
                                 .appendAttr("onMouseDown", strUrl)
                                 .appendAttr("onMouseOver","javascript:this.src='../images/sortDownOver.gif'")
                                 .appendAttr("onMouseOut","javascript:this.src='../images/sortDown.gif'")
                                 .appendAttr("title", "Sort by " + objAttr.getLabel().getLabel())
                                 .appendNL('>');
                                
                                /**
                                 * Up button.
                                 * Should submit details to allow for a createUpdate
                                 * This is NOT available if the orderSupport is simple
                                 */
                                strUrl = "zXSubmitThisUrl('" + pstrSortURLBase 
                                         + "&-oa" + strPostFix + "="
                                         + strColumnName + "&-od" + strPostFix
                                         + "=desc');";
                                
                                s.append("<img ")
                                 .appendAttr("src","../images/sortUp.gif")
                                 .appendAttr("onMouseDown", strUrl)
                                 .appendAttr("onMouseOver","javascript:this.src='../images/sortUpOver.gif'")
                                 .appendAttr("onMouseOut","javascript:this.src='../images/sortUp.gif'")
                                 .appendAttr("title", "Sort by " + objAttr.getLabel().getLabel())
                                 .appendNL('>');
                                
                            } // Order by supports both ASC and DESC
                            
                        } // Has 'columns name' (ie something to sort by on this column)
                        
                    } // Wants sorting/Supports sorting.
                    
                    this.s.appendNL("</td>");
                }
            }
            
            return gridHeader;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for list header BO columns bit.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pstrVisibleGroup = " + pstrVisibleGroup);
                getZx().log.error("Parameter : pblnResolveFK = " + pblnResolveFK);
            }
            if (getZx().throwException) throw new ZXException(e);
            gridHeader = zXType.rc.rcError;
            return gridHeader;
            
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(gridHeader);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for list header close bit.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with
     *    GridHeader and GridHeaderOpen
     * </pre>
     */
    public void gridHeaderClose() {
    	this.s.appendNL("</tr>");
    }
    
    /**
     * Generate HTML for list header open bit.
     * 
     * <pre>
     * Assumes   :
     *    Used in combination with
     *    BOListHeader and closeListHeader
     *    Table already been opened
     * </pre>
     *
     * @param pblnSelectColumn  Include select column?. Optional, default is true. 
     * @param pstrSelectLabel Label for select button column. Optional, default is null
     * @param pstrActionLabel The action label. This is Optional default is null
     */
    public void gridHeaderOpen(boolean pblnSelectColumn, String pstrSelectLabel, String pstrActionLabel) {
    	this.s.appendNL("<tr>");
    	
        /**
         * Optionally create column for select button
         */
        if (pblnSelectColumn) {
        	this.s.append("<td ")
        		  .appendAttr("width", getWebSettings().getListFormColumn1())
            	  .appendAttr("class", "zxListHeader")
            	  .appendNL('>');
            
            /**
             * Add in the select label.
             */
            if (StringUtil.len(pstrSelectLabel) > 0) {
            	this.s.appendNL(pstrSelectLabel);
            } else {
                this.s.appendNL("Select");
            }
            this.s.appendNL("</td>");
        }
        
        /**
         * create column for action button
         */
        this.s.append("<td ")
        	  .appendAttr("width", getWebSettings().getListFormColumn1())
        	  .appendAttr("class", "zxListHeader")
        	  .appendNL('>');
        
        /**
         * Add in the action label.
         */
        if (StringUtil.len(pstrActionLabel) > 0) {
            this.s.appendNL(pstrActionLabel);
        } else {
            this.s.appendNL("Action");
        }
        
        this.s.appendNL("</td>");
    }
    
    /**
     * Generate the HTML to close a gridrow.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with gridRowOpen
     * </pre>
     */
    public void gridRowClose() {
            this.s.appendNL("</tr>");
    }
    
    /**
     * Generate the HTML to open a gridrow.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Used in combination with gridRowClose
     *    Table has already been opened
     * </pre>
     *
     * @param pintRow  
     * @param pblnEvenRow Used to determine the color of the row 
     * @param penmPersistStatus
     * @param pblnSelected  
     * @param pblnMayInsert  
     * @param pblnMayUpdate  
     * @param pblnMayDelete  
     * @param pstrUrl If blank, assume a listrow with no button associated with it 
     * @param pstrClass  The defined css class.
     * @return Returns the return code of the method.
     */
    public zXType.rc gridRowOpen(int pintRow, boolean pblnEvenRow, 
            					zXType.persistStatus penmPersistStatus, 
            					boolean pblnSelected, 
            					boolean pblnMayInsert, 
            					boolean pblnMayUpdate, 
            					boolean pblnMayDelete, 
            					String pstrUrl, 
            					String pstrClass) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pintRow", pintRow);
            getZx().trace.traceParam("pblnEvenRow", pblnEvenRow);
            getZx().trace.traceParam("penmPersistStatus", penmPersistStatus);
            getZx().trace.traceParam("pblnSelected", pblnSelected);
            getZx().trace.traceParam("pblnMayInsert", pblnMayInsert);
            getZx().trace.traceParam("pblnMayUpdate", pblnMayUpdate);
            getZx().trace.traceParam("pblnMayDelete", pblnMayDelete);
            getZx().trace.traceParam("pstrUrl", pstrUrl);
            getZx().trace.traceParam("pstrClass", pstrClass);
        }

        zXType.rc gridRowOpen = zXType.rc.rcOK; 
        
        try {
            /**
             * Open row
             */
            if (StringUtil.len(pstrClass) == 0) {
            	/**
            	 * Auto configure class for row.
            	 */
            	this.s.append("<tr ")
                	  .appendAttr("class", penmPersistStatus.equals(zXType.persistStatus.psNew)?"zxGridNew": (pblnEvenRow?"zxNor":"zxAlt") )
                	  .appendNL('>');
            } else {
                this.s.append("<tr ").appendAttr("class", pstrClass).appendNL('>');
            }
            
            /**
             * Only add the column for select if there is a select button
             */
            if (StringUtil.len(pstrUrl) > 0) {
                if (penmPersistStatus.equals(zXType.persistStatus.psNew)) {
                    this.s.appendNL("<td></td>");
                } else {
                    /** 
                     * Include javascript code to set the cursor.
                     */
                    pstrUrl = "window.document.body.style.cursor='wait';" + pstrUrl + ";window.document.body.style.cursor='';";
                    
                    this.s.append("<td ")
                          .appendAttr("width", getWebSettings().getListFormColumn1()) 
                          .appendAttr("align", "center")
                          .appendNL('>');
                    
                    this.s.append("<img ")  
                		  .appendAttr("src", "../images/listItem.gif")  
                		  .appendAttr("onMouseDown", pstrUrl)
                		  .appendAttr("onMouseOver", "javascript:this.src='../images/listItemOver.gif'")  
                		  .appendAttr("onMouseOut", "javascript:this.src='../images/listItem.gif'")
                          .appendNL('>');
                	
                	this.s.appendNL("</td>");
                }
            }
            
            this.s.append("<td ")
                  .appendAttr("width", getWebSettings().getListFormColumn1())
                  .appendAttr("align", "right")
                  .appendNL('>');
            
            if ( (penmPersistStatus.equals(zXType.persistStatus.psNew) && pblnMayInsert) // New row and may insert
                 || (!penmPersistStatus.equals(zXType.persistStatus.psNew) && (pblnMayUpdate || pblnMayDelete)) // Update row and may update or delete
            	) {
                
                this.s.append("<select name=\"zXPersist").append(StringEscapeUtils.escapeHTMLTag(this.pageflow.getContextAction().getName())).append(pintRow).append("\" ")
                		.appendAttr("class", (pblnEvenRow ? "zxBoldOdd" : "zxBoldEven")) 
                		.appendAttr("tabindex", "-1")  
                		.appendNL('>');
                
                this.s.appendNL("<option value=\"-\"> </option>");
                if (penmPersistStatus.equals(zXType.persistStatus.psNew)) {
                    if (pblnMayInsert) {
                        this.s.append("<option value=\"I\" ")
                        	  .append((pblnSelected?"SELECTED":""))
                        	  .appendNL(">Insert</option>");
                    }
                    
                } else {
                    if (pblnMayUpdate) {
                        this.s.append("<option value=\"U\" ")
                        	  .append((pblnSelected && penmPersistStatus.equals(zXType.persistStatus.psDirty)?"SELECTED":""))
                        	  .appendNL(">Update</option>");
                    }
                    if (pblnMayDelete) {
                        this.s.append("<option value=\"D\" ")
                        	  .append((pblnSelected && penmPersistStatus.equals(zXType.persistStatus.psDeleted)?"SELECTED":""))
                        	  .appendNL(">Delete</option>");
                    }
                    
                }
                this.s.appendNL("</select>");
            }
            
            this.s.appendNL("</td>");
            
            return gridRowOpen;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(gridRowOpen);
                getZx().trace.exitMethod();
            }
        }
    }
    
	/**
	 * Force the Edit Groups sequence.
	 * 
     * <pre>
     * 
     * BD28FEB05 - V1.4:53
     * 
	 * Almost impossible to explain: in editForm (and grid and matrix) we can have 
	 * an edit form (ie what do you want on the edit form) and a visible group
	 * (ie which fields need to be visible).
	 * Until now, we have generated edit forms using the sequence of the edit group.
	 * This can be confusing when the visible group actually has a different
	 * sequence. For example:
	 * 
	 *  edit group: a b c d e f
	 *  visible group: d b a
	 * 
	 * Will generate an edit form with fields a b c d e f (in that order) and
	 * with a b and d visible (in that order) where poor old developer was
	 * expecting the visible fields in sequence d b a
	 * So the answer is to re-hash the edit group so that the visible group
	 * attributes are first..
	 * </pre>
     * 
	 * @param pcolEditGroup The attribute collection to force the sequence on. 
	 * @param pcolVisibleGroup The attribute collectio to base the sequence on. 
	 * @return Returns a fresh attribute collection in sequence according to the visible group.
	 * @throws ZXException Thrown if forceEditGroupSequence fails. 
	 */
	private AttributeCollection forceEditGroupSequence(AttributeCollection pcolEditGroup, 
                                                       AttributeCollection pcolVisibleGroup) throws ZXException{
		if(getZx().trace.isFrameworkTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pcolEditGroup", pcolEditGroup);
			getZx().trace.traceParam("pcolVisibleGroup", pcolVisibleGroup);
		}

		AttributeCollection forceEditGroupSequence = new AttributeCollection(); 
		
		try {
            Attribute objAttr;
		    
            /**
             * First add the attributes that are in visible group and edit group
             */
            Iterator iter = pcolVisibleGroup.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                if (pcolEditGroup.inGroup(objAttr.getName())) {
                    forceEditGroupSequence.put(objAttr.getName(), objAttr);
                }
            }
            
            /**
             * Next add all the entries in edit group that are not in visible group
             */
            iter = pcolEditGroup.iterator();
            while(iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                if (!pcolVisibleGroup.inGroup(objAttr.getName())) {
                    forceEditGroupSequence.put(objAttr.getName(), objAttr);
                }
            }
            
			return forceEditGroupSequence;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Force the Edit Groups sequence.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pcolEditGroup = "+ pcolEditGroup);
				getZx().log.error("Parameter : pcolVisibleGroup = "+ pcolVisibleGroup);
			}
			if (getZx().throwException) throw new ZXException(e);
			return forceEditGroupSequence;
		} finally {
			if(getZx().trace.isFrameworkTraceEnabled()) {
				getZx().trace.returnValue(forceEditGroupSequence);
				getZx().trace.exitMethod();
			}
		}
	}
	
    /**
     * This is a static method so that we do not need a handle to the Pagebuilder to use it.
     * 
     * @param pobjPage A handle to the pagebuilder.
     * @param out A handle to the jsp writer.
     * @param e The raised exception.
     */
    public static void handleJSPException(PageBuilder pobjPage, Writer out, Exception e) {
        String strError;
        
        if (pobjPage != null) {
            strError = pobjPage.getZx().trace.formatStack(true);
            if (StringUtil.len(strError) == 0) {
                /**
                 * If this is a top level jsp error then
                 * we need to print a nice error message.
                 */
                pobjPage.getZx().trace.addError(e);
                strError = pobjPage.getZx().trace.formatStack(true);
            }
            
            /**
             * Format the error for html.
             */
            pobjPage.errorMsg(strError);
            
            strError = pobjPage.flush();
            
        } else {
            /**
             * We should always have a handle to the pagebuilder. So
             * we should never reach this piece of code.
             */
            strError = "Fatal error : " + e.getMessage();
        }
        
        try {
            out.write(strError);
            
            /**
             * TODO : Add facility for standard error message. 
             * This might have to support multi-languages. 
             * 
             * Example : 
             * 	- Failed to perform action..
             *  - An Internal Server error accurred etc..
             */
            
        } catch(Exception e1) {
            if (pobjPage != null) {
                pobjPage.getZx().log.fatal("Failed to write jsp error message", e);
            }
        }
    }
    
    /**
     * @param pstrControlName The id of the date input control.
     * @param pobjBO The business object
     * @param pobjAttr The date attribute.
     * @param pblnDisabled Whether this is disabled or not.
     * @throws ZXException
     */
    private void buildDateButton(ZXBO pobjBO, 
    							 Attribute pobjAttr,
    							 String pstrControlName, 
    							 boolean pblnDisabled) throws ZXException {
    	
        String strButtonClass = getEditEnhancerProperty(pobjBO, pobjAttr, "stdButtonClass");
        if (StringUtil.len(strButtonClass) == 0) {
            strButtonClass = "zxLookupButton";
        }
        
        /**
         * Inline Calendar (Getting old)
         */
        this.s.append("<input  onclick=\"init();popUpCalendar(this, ").append(pstrControlName)
	          .append(", '").append(getZx().getSettings().getStrDateFormat().toLowerCase()).append("')\" ")
	          .append("value=\"...\" ") 
	          .appendAttr("class", strButtonClass)
	          .appendAttr("disabled", pblnDisabled?"true":"")
	          .append("style=\"font-size: 11px;\"") 
	          .append("type=\"button\">").appendNL();        
        
//        /**
//         * The format for the date control.
//         */
//        int dataType = pobjAttr.getDataType().pos;
//        String strJSDateFormat = "";
//        if (dataType == zXType.dataType.dtDate.pos) {
//            strJSDateFormat = getWebSettings().getDateFormat();
//        } else if (dataType == zXType.dataType.dtTimestamp.pos) {
//            strJSDateFormat = getWebSettings().getTimestampFormat();
//        }
//        
//		/**
//		 * Calendar button
//		 */
//		this.s.append("<button ")
//		      .appendAttr("id", "_dte_" + pstrControlName)
//		      .appendAttr("value","...") 
//		      .appendAttr("type","reset")
//		      .appendAttr("class", strButtonClass)
//		      // Manually size the calendar button for now.
//		      .appendAttr("style","width: 30px;font-size: 8px;") 
//		      .appendAttr("disabled", pblnDisabled?"true":"")
//		      .appendNL(">");
//		this.s.append("<img src=\"../images/calendar.gif\">");
//		this.s.append("</button>");
//		
//		/**
//		 * Init the javascript calendar component.
//		 */
//        this.s.appendNL("<script type=\"text/javascript\">");
//        this.s.appendNL(" Calendar.setup(");
//        this.s.appendNL(" {");
//        this.s.append("    inputField  : \"").append(pstrControlName).appendNL("\", ");
//        this.s.append("    ifFormat    : \"").append(strJSDateFormat).appendNL("\", ");
//        /**
//         * Also show the time component if it is a timestamp.
//         */
//        if (dataType == zXType.dataType.dtTimestamp.pos) {
//        	this.s.appendNL("    showsTime    : \"true\", ");
//        }
//        this.s.append("\tbutton 	: \"").append("_dte_" + pstrControlName).appendNL("\"");
//        this.s.appendNL(" }");
//        this.s.appendNL(" );");
//        this.s.appendNL("</script>");
    }
}