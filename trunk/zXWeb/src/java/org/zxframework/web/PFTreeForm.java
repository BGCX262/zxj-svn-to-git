/*
 * Created on Apr 12, 2004 by Michael Brewer
 * $Id: PFTreeForm.java,v 1.1.2.28 2006/07/17 16:28:43 mike Exp $ 
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.Iterator;

import org.zxframework.LabelCollection;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * The pageflow tree form action object.
 * 
 * <pre>
 * 
 * Change    : DGS16DEC02
 * Why       : Replaced normalTreeForm2/3 and pivotTreeForm by a combined showTreeForm
 *             function that handles 2 - 5 levels of either type of treeform.
 * 
 * Change    : BD20FEB03
 * Why       : Added class functionality; this required to change the URL collection
 *             to a collection of treeLevels
 * 
 * Change    : DGS20FEB03
 * Why       : Added popup functionality; this required changes to showTreeForm and a new private
 *             function constructUrl that also generates javascript for a popup for each row if
 *             necessary.
 * 
 * Change    : BD28MAR03
 * Why       : - Made popup menu case-insensitive
 *             - Added support for dialog / msgbox type popup windows
 * 
 * Change    : BD8APR03
 * Why       : Use revised version of pageflow.processRef
 * 
 * Change    : BD15MAY03
 * Why       : Fixed bug: forgot to implement support for noTouch frameno somewhere
 * 
 * Change    : BD18JUN03
 * Why       : Confirmation support in popup menus
 * 
 * Change    : BD5APR05 - V1.5:1
 * Why       : Added support for data-sources
 * 
 * Change    : V1.4:79 DGS13MAY2005
 * Why       : In showTreeForm, don't try to call rs2obj if at eof
 * 
 * Change    : V1.5:65 - BD7NOV05
 * Why       : Added parameterBag support
 *  
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFTreeForm extends PFAction {

    //------------------------ Members
    
    private ArrayList treelevels;
    private int xpos;
    private int ypos;
    private zXType.pageflowTreeType enmtreetype;
    private LabelCollection treetitle;
    
    //------------------------ Constructors
    
    /**
     * Default contructor.
     */
    public PFTreeForm() {
        super();
        
        // Init the treelevel arraylist.
        setTreelevels(new ArrayList());
    }

    //------------------------ Getters and Setters
    
    /** 
     * The enm zXType.pageflowTreeType.
     * 
     * @return Returns the enmtreetype. 
     */
    public zXType.pageflowTreeType getEnmtreetype() {
        return enmtreetype;
    }

    /**
     * @param enmtreetype The enmtreetype to set.
     */
    public void setEnmtreetype(zXType.pageflowTreeType enmtreetype) {
        this.enmtreetype = enmtreetype;
    }

    /** 
     * A ArrayList(PFTreeLevel) of treelevels.
     * In the vb version this is the urls.
     * 
     * @return Returns the treelevels. 
     */
    public ArrayList getTreelevels() {
        return treelevels;
    }

    /**
     * @param treelevels The treelevels to set.
     */
    public void setTreelevels(ArrayList treelevels) {
        this.treelevels = treelevels;
    }

    /**
     * The title of the tree.
     * 
     * @return Returns the treetitle. 
     */
    public LabelCollection getTreetitle() {
        return treetitle;
    }

    /**
     * @param treetitle The treetitle to set.
     */
    public void setTreetitle(LabelCollection treetitle) {
        this.treetitle = treetitle;
    }
    
    /** 
     * The x position. 
     * 
     * @return Returns the xpos. 
     */
    public int getXpos() {
        return xpos;
    }

    /** 
     * @param xpos The xpos to set. 
     **/
    public void setXpos(int xpos) {
        this.xpos = xpos;
    }

    /** 
     * The y position.
     * 
     * @return Returns the ypos. 
     */
    public int getYpos() {
        return ypos;
    }

    /** 
     * @param ypos The ypos to set. 
     */
    public void setYpos(int ypos) {
        this.ypos = ypos;
    }
    
    //------------------------ Digester helper methods.
    
    /**
     * @param treetype The treetype to set.
     */
    public void setTreetype(String treetype) {
        this.enmtreetype = zXType.pageflowTreeType.getEnum(treetype);
    }
    
    //------------------------ Implemented Methods from PFAction
    
    /**
     * Process action.
     * 
     * Reviewed for V1.5:65
     * 
     * @see PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
		if(getZx().trace.isApplicationTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
		zXType.rc go = zXType.rc.rcOK;
        
        DSRS objRS = null;                     // Result set
		try {
            ZXCollection colEntities;           // Relevant entities
            PFEntity objEntity;                 // Loop over variable
            String strQry = "";                 // Query in case of RDBMS
            String strSelectClause = "";        // Query in case of channel
            
            String strWhereClause;
            
            String strOrderByClauseContext = "";
            boolean blnReverse = false;
            StringBuffer strOrderByClause = new StringBuffer();     // Order as it finally turns out
            String strQryName;                                      // Name of query as stored in context
            String strGroup;                                        // Group of attributes
            PFTreeLevel objTreeLevel;
            
		    /**
		     * Default value for maxrows
		     */
            int intMaxResultRows;
            if (getMaxrows() > 0) {
                intMaxResultRows = getMaxrows();
                
            } else {
                if (getPageflow().getPage().getWebSettings().getMaxListRows() > 0) {
                    intMaxResultRows = getPageflow().getPage().getWebSettings().getMaxListRows();
                } else {
                    intMaxResultRows = 250;
                }
                
            } // Maxrows has been specified explicitly
            
            /**
             * Set the limits for the number of rows that we retrieve.
             */
            int lngStartRow = 0;
            int lngBatchSize = 0;
            if (isLimitrows()) {
                // Get one more to allow for paging.
                lngBatchSize = intMaxResultRows + 1;
            }
		    
		    /**
		     * Get relevant entities and store in context.
		     */
            colEntities = getPageflow().getEntityCollection(this, 
                                                            zXType.pageflowActionType.patListForm, 
                                                            zXType.pageflowQueryType.pqtAll);
		    if (colEntities == null) {
		        // Failing to get any entities will cause the whole pageflow action to fail.
		        throw new Exception("Unable to get entities for action");
		    }
		    getPageflow().setContextEntities(colEntities);
		    getPageflow().setContextEntity((PFEntity)colEntities.iterator().next());
		    
            /**
             * Make sure we do not break the law
             */
            if (!getPageflow().validDataSourceEntities(colEntities)) {
                throw new ZXException("Unsupported combination of data-source handlers");
            }
            
            /**
             * Get data-source handler
             */
            PFEntity objTheEntity = getPageflow().getContextEntity();
            DSHandler objDSHandler = objTheEntity.getDSHandler();
            
            /**
             * In case of channel we can only have one entity and thus must be pivot and
             * we must be able to sort
             */
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                if (getEnmtreetype().equals(zXType.pageflowTreeType.pttNormal)) {
                    throw new ZXException("Only pivot tree type supported for channel data-sources");
                }
                
                if (objDSHandler.getOrderSupport().pos == zXType.dsOrderSupport.dsosNone.pos) {
                    throw new ZXException("Order support required for tree");
                }
                
                /**
                 * In case of simple / basic: the groupByGroup should only be one attribute as
                 * this means that only a sort on a single attribute is allowed
                 */
                if (objDSHandler.getOrderSupport().pos != zXType.dsOrderSupport.dsosFull.pos
                    && objTheEntity.getBODesc().getGroup(objTheEntity.getGroupbygroup()).size() > 1) {
                    throw new ZXException("Data-source handler only provides basic / simply orderBy support but groupByGroup has multiple columns");
                }
            }
            
		    /**
		     * Set validation of each object to false in case we do an outer join (and thus some mandatory
             * attributes may get set to null)
		     */
		    Iterator iter = colEntities.iterator();
		    while (iter.hasNext()) {
		        objEntity = (PFEntity)iter.next();
		        objEntity.getBo().setValidate(false);
		    }
		    
		    /**
		     * Handle any URLs with parameter bag references
		     */
		    if (getTreelevels() != null && getTreelevels().size() > 0) {
		    	int intTreeLevels = getTreelevels().size();
		    	for (int i = 0; i < intTreeLevels; i++) {
					objTreeLevel = (PFTreeLevel)getTreelevels().get(i);
					objTreeLevel.setUrl(getPageflow().tryToResolveParameterDirectorAsUrl(objTreeLevel.getUrl()));
				}
		    }
		    
		    /**
		     * Create header.
		     */
		    getPageflow().getPage().formTitle(zXType.webFormType.wftList, getPageflow().resolveLabel(getTitle()));
		    
		    getPageflow().processMessages(this);
		    
            
            /**
             * Construct the query to use
             */
            strQryName = getPageflow().resolveDirector(getQueryname());            
            
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                strWhereClause = getPageflow().retrieveQueryWhereClause(strQryName);
                
                /**
                 * In case of basic / simple orderBy support we cannot also support a user-defined order
                 * as we only support sorting on a single column
                 */
                if (objDSHandler.getOrderSupport().pos < zXType.dsOrderSupport.dsosFull.pos) {
                    strOrderByClauseContext = getPageflow().retrieveQueryOrderByClause(strQryName, true);
                    
                    /**
                     * When the order by clause was generated as a result of the query action (eg from search form),
                     * it may start with a - that indicatesd DESC
                     */
                    if (StringUtil.len(strOrderByClauseContext) > 1 && strOrderByClauseContext.charAt(0) == '-') {
                        strOrderByClauseContext = strOrderByClauseContext.substring(1);
                        blnReverse = true;
                    }
                    
                }
                
                /**
                 * Clause not stored with leading ':'
                 */
                if (StringUtil.len(strWhereClause) > 0) {
                    strWhereClause = ":" + strWhereClause;
                }
                
            } else {
                /**
                 * Retrieve and contruct the query to use.
                 */
                strSelectClause = getPageflow().getFromContext(strQryName + Pageflow.QRYSELECTCLAUSE);
                strWhereClause = getPageflow().getFromContext(strQryName + Pageflow.QRYWHERECLAUSE);
                strOrderByClauseContext = getPageflow().getFromContext(strQryName + Pageflow.QRYORDERBYCLAUSE);
            } // Channel or RDBMS
		    
		    /**
		     * Force order by on join columns (as you cannot safely assume that
		     * the DBMS does this for you)
		     */
		    String strTmp = "";
		    
		    int intLevels = 0;
		    iter = colEntities.iterator();
		    while (iter.hasNext()) {
		        objEntity = (PFEntity)iter.next();
		        
		        if (StringUtil.len(objEntity.getGroupbygroup()) > 0) {
		            /**
		             * If the first position of the group-by-group is a minus sign, it
		             * indicates that we have to order desc
		             */
		            if (objEntity.getGroupbygroup().charAt(0) == '-') {
		                strGroup = objEntity.getGroupbygroup().substring(1);
		            } else {
		                strGroup = objEntity.getGroupbygroup();
		            }
		            
		            /**
		             * DGS 14DEC2002: Pivot trees need to know how many attributes in the group by group
		             */
		            if (getEnmtreetype().equals(zXType.pageflowTreeType.pttPivot)) {
		                intLevels = intLevels + objEntity.getBODesc().getGroup(strGroup).size();
		            }
		            
                    if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                        strTmp = objEntity.getGroupbygroup();
                    } else {
                        strTmp = getZx().getSql().orderByClause(objEntity.getBo(),
                                strGroup,
                                objEntity.getGroupbygroup().charAt(0) == '-',
                                true);
                    } // Channel or RDBMS
		            
		            if (StringUtil.len(strOrderByClause) > 0 ) {
		                strOrderByClause.append(", ").append(strTmp);
		            } else {
		                strOrderByClause.append(strTmp);
		            }
		        }
		        
		        if (getEnmtreetype().equals(zXType.pageflowTreeType.pttNormal)) {
		            /**
		             * Normal trees order by primary key, except for the last entity
		             */
		            if (iter.hasNext()) {
                        /**
                         * No need to cater for channels as channels do not support normal tree types
                         */
		                strTmp = getZx().getSql().orderByClause(objEntity.getBo(), 
		                        								 objEntity.getBo().getDescriptor().getPrimaryKey(),
		                        								 false, true);
		                if (StringUtil.len(strTmp) > 0) {
                            if (StringUtil.len(strOrderByClause) > 0) {
                                strOrderByClause.append(", ");
                            }
                            strOrderByClause.append(strTmp);
                        }
		            }
		        }
		    }
		    
		    /**
		     * Construct the whole query / debug
		     */
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                if (getPageflow().isDebugOn()) {
                    getPageflow().getPage().debugMsg(objTheEntity.getName() 
                                                     + "." + getPageflow().getContextEntity().getSelectlistgroup());       
                    if (StringUtil.len(strWhereClause) > 0) {
                        getPageflow().getPage().debugMsg("Where " + strWhereClause);
                    }
                    if (StringUtil.len(strOrderByClauseContext) > 0) {
                        getPageflow().getPage().debugMsg("Order by " + strOrderByClauseContext);
                    }
                }
                
            } else {
                strQry = strSelectClause;
                if (StringUtil.len(strWhereClause) > 0 ) {
                    strQry = strQry + " AND " + strWhereClause;
                }
                
                if (StringUtil.len(strOrderByClauseContext) > 0) {
                    if (StringUtil.len(strOrderByClause) > 0) {
                        strOrderByClause.append(", ");
                    }
                    strOrderByClause.append(strOrderByClauseContext);
                }
                
                if (StringUtil.len(strOrderByClause) > 0) {
                    strQry = strQry + " ORDER BY " + strOrderByClause.toString();
                }
                
                /**
                 * Debug the query that we will use
                 */
                if (getPageflow().isDebugOn()) {
                    getPageflow().getPage().debugMsg(strQry);
                }
            } // RDBMS?
		    
		    /**
		     * Generate recordset
		     */
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                objRS = objDSHandler.boRS(objTheEntity.getBo(), 
                                          objTheEntity.getSelectlistgroup(),
                                          strWhereClause, 
                                          isResolvefk(),
                                          strOrderByClause.toString(), 
                                          blnReverse,
                                          lngStartRow, lngBatchSize);
                
            } else {
                objRS = ((DSHRdbms)objDSHandler).sqlRS(strQry,
                                                       lngStartRow, lngBatchSize);
                
            }
            
		    if (objRS == null) {
		        throw new Exception("Unable to execute treeform query");
		    }
		    
		    if (getEnmtreetype().equals(zXType.pageflowTreeType.pttNormal)) {
		        intLevels = colEntities.size();
		    } else {
		        /**
		         * DGS 14DEC2002: Pivot tree - have one more level than the number of group by attributes
		         */
		        intLevels = intLevels + 1;
		    }
            
		    getPageflow().getPage().s.appendNL("<table width='100%'>\n<tr>\n<td>\n");
		    
		    if (showTreeForm(objRS, colEntities, intLevels, getEnmtreetype(), intMaxResultRows).pos != zXType.rc.rcOK.pos) {
		        throw new Exception("Unable to generate " 
		        					+ zXType.valueOf(getEnmtreetype()) 
		        					+ " treeform for " + intLevels + " levels");
		    }
		    
		    getPageflow().getPage().s.appendNL("</td>\n</tr>\n</table>");
		    
		    /**
		     * Handle buttons; make sure that the buttons are aligned with the
		     * right column of the list
		     */
		    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftList);
		    getPageflow().processFormButtons(this);
		    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftList);
		    
		    // Display the title of the page. 
		    getPageflow().getPage().s.appendNL("<script type=\"text/javascript\" language=\"JavaScript\">");
		    
		    if (!hasTag("zXNoTitle")) {
		        strTmp = getPageflow().resolveLabel(getTitle());
		        if (StringUtil.len(strTmp) > 0) {
		            getPageflow().getPage().s.appendNL("  zXTitle('" + StringEscapeUtils.escapeJavaScript(strTmp) + "');");
		        }
		    }
		    
		    if (!hasTag("zXNoFooter")) {
		        getPageflow().getPage().s.appendNL("  zXSetFooter('Select row / action');");
		    }
		    
		    getPageflow().getPage().s.appendNL("</script>");
		    
		    /** Resolve the next action **/
		    getPageflow().setAction(getPageflow().resolveLink(getLink()));
		    
		    return go;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Execute the TreeForm action.", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    go = zXType.rc.rcError;
		    return go;
		} finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
		    if(getZx().trace.isApplicationTraceEnabled()) {
		        getZx().trace.returnValue(go);
		        getZx().trace.exitMethod();
		    }
		}
    }
    
    /**
     * Determine what the URL should be for this row (a popup).
     *
     * @param pobjUrl The url to use. 
     * @param pstrConfirm The message of the url. Optional, default is null. 
     * @return Returns a construct url, which wrapped urls and all.
     * @throws ZXException Thrown if constructPopupRowURL fails. 
     */
    private String constructPopupRowURL(PFUrl pobjUrl, String pstrConfirm) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjUrl", pobjUrl);
            getZx().trace.traceParam("pstrConfirm", pstrConfirm);
        }
        
        String constructPopupRowURL = ""; 
        
        try {
            /**
             * If no URL: we're done
             */
            if (pobjUrl == null) {
                return constructPopupRowURL;
            }
            
            constructPopupRowURL = getPageflow().wrapRefUrl(getPageflow().resolveDirector(pobjUrl.getFrameno()),
															constructPopupRowURL, 
															pstrConfirm,
															false,
															true);
            return constructPopupRowURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine what the URL should be for this row (a popup).", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjUrl = " + pobjUrl);
                getZx().log.error("Parameter : pstrConfirm = " + pstrConfirm);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return constructPopupRowURL;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(constructPopupRowURL);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Contruct the url for a link.
     * 
     * <pre>
     * 
     * DGS28FEB2003: New code for a popup. Similar to new function processPopups in clsPageflow
     * but this is specifically for treeform, and only looks for the single popup action name that
     * can apply to a treeform row.
     * </pre>
     *
     * @param pobjUrl Url to contruct from. This object maybe updated in this method.
     * @param pstrID The id of the linked element.
     * @return Returns a contructed url. 
     * @throws ZXException Thrown if constructURL fails. 
     */
    public String constructURL(PFUrl pobjUrl, String pstrID) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjUrl", pobjUrl);
            getZx().trace.traceParam("pstrID", pstrID);
        }

        String constructURL = ""; 
        
        try {
            
            if (pobjUrl.getUrlType().equals(zXType.pageflowUrlType.putPopup)) {
                /**
                 * url is appended by unique id. This is then undone after constructing the row URL
                 * and generating the popup (as the same url is used by all rows, not just this one).
                 */
                String strPopupName = pobjUrl.getUrl();
                pobjUrl.setUrl(pobjUrl.getUrl() + pstrID);
                constructURL = getPageflow().constructURL(pobjUrl);
                
                boolean blnStartMenu = true;
                
                PFRef objRef;
                Iterator iter = getPopups().iterator();
                while (iter.hasNext()) {
                    objRef = (PFRef)iter.next();
                    
                    /**
                     * Only interested in popups for this name...
                     */
                    if (objRef.getName().equalsIgnoreCase(strPopupName)) {
                        /**
                         *  ...that are active
                         */
                        if (getPageflow().isActive(objRef.getUrl().getActive())) {
                            if (blnStartMenu) {
                                blnStartMenu = false;
                                // Start new entire popup menu:
                                getPageflow().getPage().popupMenuStart(pobjUrl.getUrl());
                            }
                            
                            /**
                             * Popup menu name (in url) has already been suffixed by id to keep it unique
                             */
                            getPageflow().getPage().popupMenuOption(pobjUrl.getUrl(),
                            										getPageflow().resolveLabel(objRef.getLabel()),
                            										constructPopupRowURL(objRef.getUrl(),
                            															 getZx().resolveDirector(getPageflow().resolveLabel(objRef.getConfirm()))),
                            										objRef.getImg(),
                            										objRef.getImgover(),
                            										objRef.isStartsubmenu());
                        }
                    }
                }
                
                if (!blnStartMenu) {
                    /**
                     * We found at least one active popup, so end the popup
                     */
                    getPageflow().getPage().popupMenuEnd();
                }
                
                /**
                 * Copy the original url back (without the id suffix)
                 */
                pobjUrl.setUrl(strPopupName);
                
            } else {
                /**
                 * Here for a normal (non-popup) url - construct the url as normal
                 */
                constructURL = getPageflow().constructURL(pobjUrl);
                if (StringUtil.len(constructURL) == 0) {
                    return "";
                }
                constructURL = getPageflow().wrapRefUrl(getPageflow().resolveDirector(pobjUrl.getFrameno()), constructURL);
            }
            
            return constructURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Contruct the url for a link.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjUrl = "+ pobjUrl);
                getZx().log.error("Parameter : pstrID = "+ pstrID);
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
     * Opens one level of the tree.
     * 
     * <pre>
     * 
     * NOTE : Could add cellspacing = 0 but then it still looks a bit weird.
     * </pre>
     * 
     * @param pstrID The id of the link
     * @return Returns the html
     */
    private String openLevel(String pstrID) {
        StringBuffer openLevel = new StringBuffer(128);
        openLevel.append("<tr>\n")
						.append("	<td colspan='99'>\n")
						.append("		<div id='").append(pstrID).append("' style='display:none'>\n")
						.append("		<table width='100%' cellpadding='0'>\n")
						.append("			<tr>\n")
						.append("				<td width='8%' class='zxTreeVoid'>\n")
						.append("				</td>\n")
						.append("				<td>\n")
						.append("					<table width='100%' cellpadding='0'>\n")
						.append("						<tr>\n");
        
        return openLevel.toString();
    }
    
    /**
     * Closes one level of the tree.
     * 
     * @return Closes one level of the tree
     */
    private String closeLevel() {
        StringBuffer closeLevel = new  StringBuffer(64);
        closeLevel.append("							</tr>\n");
        closeLevel.append("						</table>\n");
        closeLevel.append("					</td>\n");
        closeLevel.append("				</tr>\n");
        closeLevel.append("			</table>\n");
        closeLevel.append("		</div>\n");
        closeLevel.append("	</td>\n");
        closeLevel.append("</tr>\n\n");
        return closeLevel.toString();
    }

    /**
     * Generate treeform.
     * 
     * <pre>
     * 
     * Note that this handles from 2 to 5 levels. The lowest (highest numbered) level is always
     * displayed using the fifth occurrence of arrays etc. The highest level uses arrays(1). If
     * the number of levels is less than 5, some interim levels don't get used. E.g. if there
     * are 2 levels, these will be occurrences 1 and 5. Some arrays are filled with data anyway
     * to make various tests easier.
     * 
     * DGS25FEB2003: Added support for 'active' rows. Uses the 'active' property of each level's URL
     * to determine if that row should be shown. If not, no lower levels are shown either. Most
     * of the code change to achieve this is setting and testing an array of booleans 'arrblnActive',
     * but there are one or two other small changes to ensure that certain statements are executed even
     * when the row is not active e.g. setting of last key for this level.
     * 
     * NOTE : In java arrays ALWAYS start with 0. So this tends to mess things up.
     * NOTE : Therefore the value of pintLevels needs to be altered.
     * </pre>
     *
     * @param pobjRS The resultset.
     * @param pcolEntities Collection of PFEntities.
     * @param pintLevels The level of the tree
     * @param penmTreeType The tree type.
     * @param pintMaxResultRows The maximum number of rows.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if showTreeForm fails. 
     */
    private zXType.rc showTreeForm(DSRS pobjRS, 
                                   ZXCollection pcolEntities, 
                                   int pintLevels, 
                                   zXType.pageflowTreeType penmTreeType,
                                   int pintMaxResultRows) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRs", pobjRS);
            getZx().trace.traceParam("pcolEntities", pcolEntities);
            getZx().trace.traceParam("pintLevels", pintLevels);
            getZx().trace.traceParam("penmTreeType", penmTreeType);
        }
        
        zXType.rc showTreeForm = zXType.rc.rcOK;
        
        try {
            // NOTE : 5 is the maximum number of entities a tree form supports. 
            boolean arrblnOdd[] = new boolean[5];
            boolean arrblnActive[] = new boolean[5];
            String arrLastKey[] = new String[5];
            String arrCurrentKey[] = new String[5];
            PFEntity arrobjEntity[] = new PFEntity[5];
            String arrstrAttr[] = new String[5];
            
            PFEntity objEntity;
            
            int intCurrEntity = 0;
            int intCurrAttr = 0;
            int intEntity = pcolEntities.size();
            
            ArrayList arrEnttites = new ArrayList(pcolEntities.getCollection());
            
            for (int j = 0; j < 5; j++) {
                /**
                 * Prepare entity BOs now. Can handle from 1 to 5 entities - for normal tree types, expect
                 * one entity per level. For pivot tree types, expect one attr per level, except for the
                 * lowest level, so 3 for a 4 level tree. The attributes are taken across all the entities'
                 * groupby groups summed together.
                 */
                arrobjEntity[j] = (PFEntity)arrEnttites.get(intCurrEntity);
                
                if (penmTreeType.equals(zXType.pageflowTreeType.pttNormal)) {
                    arrstrAttr[j] = arrobjEntity[j].getListgroup();
                    
                    /**
                     * CORRECTION ..
                     */
                    if (intCurrEntity < (intEntity - 1)) {
                        intCurrEntity++;
                    }
                    
                } else {
                    // Pivot form :
                    arrstrAttr[j] = arrobjEntity[j].getBo().getAttrByNumber(intCurrAttr, arrobjEntity[j].getGroupbygroup()).getName();
                    if (pintLevels > j + 2) {
                        if (intCurrAttr < arrobjEntity[j].getBODesc().getGroup(arrobjEntity[j].getGroupbygroup()).size()) {
                            /**
                             * Have more attrs in this entity's groupby group, so the next level will use the
                             * next attr:
                             */
                            intCurrAttr++;
                        } else {
                            /**
                             * No more attrs in this entity's groupby group, so the next level will use the
                             * first attr from the next entity:
                             */
                            intCurrEntity++;
                            intCurrAttr = 0;
                        }
                    }
                    
                }
            }
            
            arrstrAttr[4] = arrobjEntity[4].getListgroup();
            
            if( !penmTreeType.equals(zXType.pageflowTreeType.pttNormal) ) {
                /**
                 * The lowest level attr group is the list group. The list group should always include the
                 * pivot attrs so that an export will include them. However, don't want to show them in the
                 * lowest level of the tree as they already appear in the higher levels
                 */
                for (int j = 0; j <= pintLevels - 2; j++) {
                    arrstrAttr[4] = arrstrAttr[4] + ",-" + arrstrAttr[j];
                }
            }
            
            /**
             * Initialise the array that keeps track of the last keys
             */
            for (int j = 0;  j < 5; j++) {
                arrLastKey[j] = "-1";
                arrCurrentKey[j] = "-1";
            }
            
            int intRowCount = 1;
            
            /**
             * Open outer DIV and table and generate list header - was _mnu_rootDiv ?
             */
            getPageflow().getPage().s.append("<DIV ").appendAttr("id", "rootDiv").appendNL('>');
            
            getPageflow().getPage().s.append("<table ").appendAttr("width", "100%").appendAttr("cellpadding", "0").appendNL('>');
            
            /**
             * Top level tree gets special HTML function that includes an image to collapse/expand
             * the whole tree.
             */
            getPageflow().getPage().treeListHeaderOpenTop();
            getPageflow().getPage().treeListHeader(arrobjEntity[0].getBo(), arrstrAttr[0]);
            getPageflow().getPage().treeListHeaderClose();
            
            while(!pobjRS.eof()  && intRowCount <= pintMaxResultRows) {
                
                /**
                 * Populate all of the entities in this row :
                 */
                Iterator iter = pcolEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    getPageflow().setContextEntity(objEntity);
                    pobjRS.rs2obj(objEntity.getBo(), objEntity.getSelectlistgroup(), isResolvefk());
                }
                
                /**
                 * Save last key for each level
                 */
                for (int j = 0; j <= pintLevels - 2; j++) {
                    
                    if (penmTreeType.equals(zXType.pageflowTreeType.pttNormal)) {
                        arrCurrentKey[j] = arrobjEntity[j].getBo().getPKValue().getStringValue();
                    } else {
                        arrCurrentKey[j] = arrobjEntity[j].getBo().getValue(arrstrAttr[j]).formattedValue();
                    }
                    
                }
                
                arrLastKey[0] = arrCurrentKey[0];
                
                /**
                 * Generate string for this level. Rowcount keeps it unique
                 */
                String strId = "_mnu_Id1_" + intRowCount;
                
                /**
                 * If we only have 1 URL, we have to assume it applies to the lowest level and not
                 * to this level. Therefore only use urls(1) if there is more than 1 of them.
                 * DGS25FEB2003: Only include this line and its sublevels if it is 'active':
                 */
                String strUrl;
                arrblnActive[0] = true;
                
                int intTreelevels = getTreelevels().size();
                if (intTreelevels > 1) {
                    strUrl = constructURL(((PFTreeLevel)getTreelevels().get(0)).getUrl(), strId);
                    arrblnActive[0] = getPageflow().isActive( ((PFTreeLevel)getTreelevels().get(0)).getUrl().getActive());
                } else {
                    strUrl = "";
                }
                
                /**
                 * Open next level
                 * First show current row
                 * First determine the class
                 */
                String strClass;
                PFTreeLevel objPFTreeLevel;
                if (arrblnActive[0]) {
                    objPFTreeLevel = (PFTreeLevel)getTreelevels().get(0);
                    strClass = objPFTreeLevel.getClazz();
                    if (StringUtil.len(strClass) > 0) {
                        strClass = getPageflow().resolveDirector(strClass);
                        if (objPFTreeLevel.isAddparitytoclass() && StringUtil.len(strClass) > 0) {
                            if (arrblnOdd[0]) {
                                strClass = strClass + "Odd";
                            } else {
                                strClass = strClass + "Even";
                            }
                        }
                    } else {
                        strClass = "";
                    }
                    
                    getPageflow().getPage().treeListRowOpen(arrblnOdd[0], strId, strUrl, false, strClass);
                    getPageflow().getPage().treeListRow(arrobjEntity[0].getBo(), arrstrAttr[0]);
                    getPageflow().getPage().treeListRowClose();
                    
                    /**
                     * And open next level
                     */
                    getPageflow().getPage().s.appendNL(openLevel(strId));
                    
                    if (pintLevels > 2) {
                        getPageflow().getPage().treeListHeaderOpen();
                        getPageflow().getPage().treeListHeader(arrobjEntity[1].getBo(), arrstrAttr[1]);
                        getPageflow().getPage().treeListHeaderClose();
                        
                        arrblnOdd[1] = !arrblnOdd[0]; // Starts opposite to parent 
                    }
                } // Is level 1 active
                
                while (!pobjRS.eof() 
                       && intRowCount <= pintMaxResultRows
                       && arrLastKey[0].equals(arrCurrentKey[0]) ) {
                    
                    if (pintLevels > 2) {
                        /**
                         * Generate string for this level
                         */
                        strId = "_mnu_Id2_" + intRowCount;
                        
                        /**
                         * If we have 1 or 2 URLs, we have to assume the first one applies to level 1
                         * and the second to the top level and not to this level.
                         * DGS25FEB2003: Only include this line and its sublevels if its parent is
                         * 'active' and this row itself is active:
                         */
                        objPFTreeLevel = (PFTreeLevel)getTreelevels().get(1);
                        arrblnActive[1] = arrblnActive[0]; // As active as its parent...
                        if (intTreelevels > 2) {
                            strUrl = constructURL(objPFTreeLevel.getUrl(), strId);
                            if (arrblnActive[1]) { // If parent is active, see if this is:
                                arrblnActive[1] = getPageflow().isActive(objPFTreeLevel.getUrl().getActive());
                            }
                        } else if (intTreelevels > 1) {
                            strUrl = constructURL(((PFTreeLevel)getTreelevels().get(intTreelevels - 2)).getUrl(), strId);
                            if (arrblnActive[1]) { // If parent is active, see if this is:
                                arrblnActive[1] = getPageflow().isActive(((PFTreeLevel)getTreelevels().get(intTreelevels - 2)).getUrl().getActive());
                            }
                        } else {
                            strUrl = "";
                        }
                        
                        if (arrblnActive[1]) {
                            /**
                             * Determine the class to be used
                             * Determine class to use
                             */
                            if (StringUtil.len(objPFTreeLevel.getClazz()) > 0) {
                                strClass = getPageflow().resolveDirector(objPFTreeLevel.getClazz());
                                
                                if (objPFTreeLevel.isAddparitytoclass() && StringUtil.len(strClass) > 0) {
                                    if (arrblnOdd[1]) {
                                        strClass = strClass + "Odd";
                                    } else {
                                        strClass = strClass + "Even";
                                    }
                                }
                            } else {
                                strClass = "";
                            }
                            
                            getPageflow().getPage().treeListRowOpen(arrblnOdd[1], strId, strUrl, false, strClass);
                            getPageflow().getPage().treeListRow(arrobjEntity[1].getBo(), arrstrAttr[1]);
                            getPageflow().getPage().treeListRowClose();
                            
                            /**
                             * And open next level
                             */
                            getPageflow().getPage().s.append(openLevel(strId));
                            
                        } // Is level 2 active
                        
                        /**
                         * Save last key for this level
                         */
                        arrLastKey[1] = arrCurrentKey[1];
                        
                        if (pintLevels > 3  && arrblnActive[1]) {
                            getPageflow().getPage().treeListHeaderOpen();
                            getPageflow().getPage().treeListHeader(arrobjEntity[2].getBo(), arrstrAttr[2]);
                            getPageflow().getPage().treeListHeaderClose();
                            arrblnOdd[2] = !arrblnOdd[2];
                        }
                    }
                    
                    /**
                     * Note that the interim (levels 2 to 4) 'Do While...Loop' code is always executed
                     * regardless of how many levels we have, but the condition makes it work for
                     * different numbers of levels. In effect it says 'only break at this level if we
                     * have 3 levels or more and the level 2 key has changed.'
                     */
                    while (
                           !pobjRS.eof() 
                           && intRowCount <= pintMaxResultRows
                           && arrLastKey[0].equals(arrCurrentKey[0]) 
                           && ( pintLevels < 3 || arrLastKey[1].equals(arrCurrentKey[1]) ) 
                            ) {
                        
                        if (pintLevels > 3) {
                            /**
                             * Generate string for this level
                             */
                            strId = "_mnu_Id3_" + intRowCount;
                            
                            arrblnActive[2] = arrblnActive[2];
                            
                            if (intTreelevels > 3) {
                                objPFTreeLevel = (PFTreeLevel)getTreelevels().get(2);
                                strUrl = constructURL(objPFTreeLevel.getUrl(), strId);
                                if (arrblnActive[2]) {
                                    arrblnActive[2] = getPageflow().isActive(objPFTreeLevel.getUrl().getActive());
                                }
                            } else if (intTreelevels > 1) {
                                strUrl = constructURL(((PFTreeLevel)getTreelevels().get(intTreelevels - 2)).getUrl(), strId);
                                if (arrblnActive[2]) {
                                    arrblnActive[2] = getPageflow().isActive(((PFTreeLevel)getTreelevels().get(intTreelevels - 2)).getUrl().getActive());
                                }
                            } else {
                                strUrl = "";
                            }
                            
                            if (arrblnActive[2]) {
                                objPFTreeLevel = (PFTreeLevel)getTreelevels().get(2);
                                strClass = getPageflow().resolveDirector(objPFTreeLevel.getClazz());
                                if (objPFTreeLevel.isAddparitytoclass() && StringUtil.len(strClass) > 0) {
                                    if (arrblnOdd[2]) {
                                        strClass = strClass + "Odd";
                                    } else {
                                        strClass = strClass + "Even";
                                    }
                                } else {
                                    strClass = "";
                                }
                                
                                getPageflow().getPage().treeListRowOpen(arrblnOdd[2], strId, strUrl, false, strClass);
                                getPageflow().getPage().treeListRow(arrobjEntity[2].getBo(), arrstrAttr[2]);
                                getPageflow().getPage().treeListRowClose();
                                
                                /**
                                 * And open next level
                                 */
                                getPageflow().getPage().s.append(openLevel(strId));
                                
                                if (pintLevels > 4) {
                                    getPageflow().getPage().treeListHeaderOpen();
                                    getPageflow().getPage().treeListHeader(arrobjEntity[3].getBo(), arrstrAttr[3]);
                                    getPageflow().getPage().treeListHeaderClose();
                                    arrblnOdd[3] = !arrblnOdd[2];
                                }
                            }  // Is level 3 active
                        }
                        
                        /**
                         * Save last key for this level
                         */
                        arrLastKey[2] = arrCurrentKey[2];
                        
                        while (!pobjRS.eof() 
                               && intRowCount <= pintMaxResultRows
                               && ( arrLastKey[0].equals(arrCurrentKey[0]) )
                               && ( pintLevels < 3 || arrLastKey[1].equals(arrCurrentKey[1]) )
                               && ( pintLevels < 4 || arrLastKey[2].equals(arrCurrentKey[2]) )
                                 ) {
                            
                            if (pintLevels > 4) {
                                /**
                                 * Generate string for this level
                                 */
                                strId = "_mnu_Id4_" + intRowCount;
                                
                                arrblnActive[3] = arrblnActive[2];
                                if (intTreelevels > 4) {
                                    objPFTreeLevel = (PFTreeLevel)getTreelevels().get(3);
                                    strUrl = constructURL(objPFTreeLevel.getUrl(), strId);
                                    if(arrblnActive[3]) {
                                        arrblnActive[3] = getPageflow().isActive(objPFTreeLevel.getUrl().getActive());
                                	}
                                } else if (intTreelevels > 0) {
                                    strUrl = constructURL(((PFTreeLevel)getTreelevels().get(intTreelevels - 2)).getUrl(), strId);
                                    if (arrblnActive[3]) {
                                        arrblnActive[3] = getPageflow().isActive(((PFTreeLevel)getTreelevels().get(intTreelevels - 2)).getUrl().getActive());
                                    }
                                } else {
                                    strUrl = "";
                                }
                                
                                if (arrblnActive[3]) {
                                    objPFTreeLevel = (PFTreeLevel)getTreelevels().get(3);
                                    if (StringUtil.len(objPFTreeLevel.getClazz()) > 0) {
                                        strClass = getPageflow().resolveDirector(objPFTreeLevel.getClazz());
                                        if (objPFTreeLevel.isAddparitytoclass() && StringUtil.len(strClass) > 0) {
                                            if (arrblnOdd[3]) {
                                                strClass = strClass + "Odd";
                                            } else {
                                                strClass = strClass + "Even";
                                            }
                                        }    
                                    } else {
                                        strClass = "";
                                    }
                                
                                    getPageflow().getPage().treeListRowOpen(arrblnOdd[3], strId, strUrl, false, strClass);
                                    getPageflow().getPage().treeListRow(arrobjEntity[3].getBo(), arrstrAttr[3]);
                                    getPageflow().getPage().treeListRowClose();
                                    
                                    /**
                                     * And open next level
                                     */
                                    getPageflow().getPage().s.append(openLevel(strId));
                                }
                                
                                /**
                                 * Save last key for this level
                                 */
                                arrLastKey[3] = arrCurrentKey[3];
                            }
                            
                            if (arrblnActive[pintLevels - 2]) {
                                getPageflow().getPage().treeListHeaderOpen();
                                getPageflow().getPage().treeListHeader(arrobjEntity[4].getBo(), arrstrAttr[4]);
                                getPageflow().getPage().treeListHeaderClose();
                                arrblnOdd[4] = !arrblnOdd[pintLevels - 2]; // Starts opposite to last displayed parent
                            }
                            
                            while (
                                    !pobjRS.eof()
                                    && intRowCount <= pintMaxResultRows
                                    && ( arrLastKey[0].equals(arrCurrentKey[0]) )
                                    && ( pintLevels < 3 || arrLastKey[1].equals(arrCurrentKey[1]) )
                                    && ( pintLevels < 4 || arrLastKey[2].equals(arrCurrentKey[2]) )
                                    && ( pintLevels < 5 || arrLastKey[3].equals(arrCurrentKey[3]) )
                            		) {
                                
                                arrblnActive[4] = arrblnActive[pintLevels - 2];
                                if (arrblnActive[4]) {
                                    arrblnActive[4] = getPageflow().isActive(((PFTreeLevel)getTreelevels().get(intTreelevels -1)).getUrl().getActive());
                                }
                                if (arrblnActive[4]) {
                                    /**
                                     * Lowest-level URL is always required
                                     */
                                    objPFTreeLevel = (PFTreeLevel)getTreelevels().get(intTreelevels -1);
                                    
                                    /**
                                     * Generate string for this level
                                     */
                                    strId = "_mnu_Id5_" + intRowCount;
                                    strUrl = constructURL(objPFTreeLevel.getUrl(), strId);
                                    
                                    if (StringUtil.len(objPFTreeLevel.getClazz()) > 0) {
                                        // Get the last element.
                                        strClass = getPageflow().resolveDirector(objPFTreeLevel.getClazz());
                                        if (objPFTreeLevel.isAddparitytoclass() && StringUtil.len(strClass) > 0) {
                                            if (arrblnOdd[4]) {
                                                strClass = strClass + "Odd";
                                            } else {
                                                strClass = strClass + "Even";
                                            }
                                        }
                                        
                                    } else {
                                        strClass = "";
                                    }
                                    
                                    getPageflow().getPage().treeListRowOpen(arrblnOdd[4], strId, strUrl, true, strClass);
                                    getPageflow().getPage().treeListRow(arrobjEntity[4].getBo(), arrstrAttr[4]);
                                    getPageflow().getPage().treeListRowClose();
                                    
                                } // Is level 5 active
                                
                                pobjRS.moveNext();
                                
                                /**
                                 * We may have gone one record to far.
                                 * 
                                 * V1.4:79 DGS13MAY2005: Don't try to call rs2obj if at eof as it will
                                 * generate a log entry. Otherwise this was harmless, but better not to
                                 * call it. Also can catch any non-ok now and treat it as true failure.
                                 */
                                if (!pobjRS.eof()) {
                                    // Populate the next row of entries :
                                    iter = pcolEntities.iterator();
                                    while (iter.hasNext()) {
                                        objEntity = (PFEntity)iter.next();
                                        pobjRS.rs2obj(objEntity.getBo(), objEntity.getSelectlistgroup(), isResolvefk());  
                                    }
                                }
                                
                                for (int i = 0; i <= pintLevels - 2; i++) {
                                    if (penmTreeType.equals(zXType.pageflowTreeType.pttNormal)) {
                                        arrCurrentKey[i] = arrobjEntity[i].getBo().getPKValue().getStringValue();
                                    } else {
                                        arrCurrentKey[i] = arrobjEntity[i].getBo().getValue(arrstrAttr[i]).formattedValue();
                                    }
                                }
                                
                                intRowCount = intRowCount + 1;
                                if (arrblnActive[4]) {
                                    arrblnOdd[4] = !arrblnOdd[4];
                                }
                                
                            }
                            
                            if (pintLevels > 4 && arrblnActive[4]) {
                                getPageflow().getPage().s.append(closeLevel());
                                arrblnOdd[3] = !arrblnOdd[3];
                            }
                            
                        }
                        
                        if (pintLevels > 3 && arrblnActive[3]) {
                            getPageflow().getPage().s.append(closeLevel());
                            arrblnOdd[2] = !arrblnOdd[2];
                        }
                        
                    }
                    
                    if (pintLevels > 2 && arrblnActive[1]) {
                        getPageflow().getPage().s.append(closeLevel());
                        arrblnOdd[1] = !arrblnOdd[1];
                    }
                    
                }
                
                if (arrblnActive[0]) {
                    getPageflow().getPage().s.append(closeLevel());
                    arrblnOdd[0] = !arrblnOdd[0];
                }
                
            }
            
            /**
             * Close outer table / div
             */
            getPageflow().getPage().s.appendNL("</table>");
            getPageflow().getPage().s.appendNL("</div>");
            
            if (intRowCount == 1) {
                /** No results found */
                getPageflow().getPage().infoMsg("No matches found");
            } else if (intRowCount >= pintMaxResultRows){
                getPageflow().getPage().softMsg("Resultset truncated, " + (intRowCount - 1) + " rows displayed");
            } else {
                getPageflow().getPage().softMsg((intRowCount - 1) + " row" + (intRowCount == 2 ? "" : "s") + " displayed");
            }
            
            return showTreeForm;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate treeform.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRs = "+ pobjRS);
                getZx().log.error("Parameter : pcolEntities = "+ pcolEntities);
                getZx().log.error("Parameter : pintLevels = "+ pintLevels);
                getZx().log.error("Parameter : penmTreeType = "+ penmTreeType);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            showTreeForm = zXType.rc.rcError;
            return showTreeForm;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(showTreeForm);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ PFAction overidden methods
    
    /** 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
        
        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();
        
        Iterator iter; 
        PFTreeLevel objTreeLevel;
        
        if (getTreelevels() != null && !getTreelevels().isEmpty()) {
            objXMLGen.openTag("treelevels");
            
            iter = getTreelevels().iterator();
            while (iter.hasNext()) {
                objTreeLevel = (PFTreeLevel)iter.next();
                getDescriptor().xmlTreeLevel("treelevel", objTreeLevel);
            }
            
            objXMLGen.closeTag("openTag");
        }
        
        objXMLGen.taggedValue("xpos", String.valueOf(getXpos()));
        objXMLGen.taggedValue("ypos", String.valueOf(getYpos()));
        
        objXMLGen.taggedValue("treetype", zXType.valueOf(getEnmtreetype()));
        
        getDescriptor().xmlLabel("treetitle", getTreetitle());
        
    }
    
    /** 
     * @see PFAction#clone(Pageflow)
     **/
    public PFAction clone(Pageflow pobjPageflow) {
        PFTreeForm cleanClone = (PFTreeForm)super.clone(pobjPageflow);
        
        try {
            
            if (getTreelevels() != null) {
                int intTreelevels = getTreelevels().size();
                ArrayList objTreeLevels = new ArrayList(intTreelevels);
                PFTreeLevel objTreeLevel;
                
                for (int i = 0; i < intTreelevels; i++) {
                    objTreeLevel = (PFTreeLevel)getTreelevels().get(i);
                    objTreeLevels.add(objTreeLevel.clone());
                }
                
                cleanClone.setTreelevels(objTreeLevels);
            }
            
            if (getTreetitle() != null) {
                cleanClone.setTreetitle((LabelCollection)getTreetitle().clone());
            }
            
            cleanClone.setEnmtreetype(getEnmtreetype());
            cleanClone.setXpos(getXpos());
            cleanClone.setYpos(getYpos());
            
        } catch (Exception e) {
            getZx().log.error("Failed to clone object", e);
        }
        
        return cleanClone;
    }
}