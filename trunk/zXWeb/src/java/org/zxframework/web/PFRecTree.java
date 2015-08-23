/*
 * Created on Apr 14, 2004 by Michael Brewer
 * $Id: PFRecTree.java,v 1.1.2.25 2006/07/17 16:28:43 mike Exp $ 
 */
package org.zxframework.web;

import java.util.Iterator;

import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.misc.ExprFHRecTree;
import org.zxframework.misc.RecTree;
import org.zxframework.misc.RecTreeNode;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;
import org.zxframework.web.PFUrl;

/**
 * The recursive tree pageflow action.
 * 
 * <pre>
 * 
 * Change    : BD7DEC04 - V1.4:3
 * Why       : Fixed problem with display style for open / closed nodes
 * 
 * Change    : BD5MAR05 - V1.5:1
 * Why       : Add support for data-sources
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
public class PFRecTree extends PFAction {
    
    //------------------------ Members
    
    private String treeentity;
    private String active;
    private String nodeclass;
    private boolean nodeaddparity;
    private String itemclass;
    private boolean itemaddparity;
    private PFUrl treenodeurl;
    private PFUrl itemurl;
    private String nodeopen;
    private zXType.treeOpenMode treeOpenmode;
    private String itemBaseQuery;
    
    //------------------------ Runtime member
    
    private ExprFHRecTree exprFHRecTree;
    private RecTree recTree;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFRecTree() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    /**
     * Note : This is not yet used yet?
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
     * NOTE : This is not yet used.
     * 
     * @return Returns the itemaddparity. 
     */
    public boolean isItemaddparity() {
        return itemaddparity;
    }

    /**
     * @param itemaddparity The itemaddparity to set.
     */
    public void setItemaddparity(boolean itemaddparity) {
        this.itemaddparity = itemaddparity;
    }
    
    /** 
     * @return Returns the itemclass. 
     */
    public String getItemclass() {
        return itemclass;
    }

    /**
     * @param itemclass The itemclass to set.
     */
    public void setItemclass(String itemclass) {
        this.itemclass = itemclass;
    }

    /** 
     * @return Returns the itemurl. 
     */
    public PFUrl getItemurl() {
        return itemurl;
    }

    /**
     * @param itemurl The itemurl to set.
     */
    public void setItemurl(PFUrl itemurl) {
        this.itemurl = itemurl;
    }
    
    /** 
     * @return Returns the nodeaddparity. 
     */
    public boolean isNodeaddparity() {
        return nodeaddparity;
    }

    /**
     * @param nodeaddparity The nodeaddparity to set.
     */
    public void setNodeaddparity(boolean nodeaddparity) {
        this.nodeaddparity = nodeaddparity;
    }
    
    /** 
     * @return Returns the nodeclass. 
     */
    public String getNodeclass() {
        return nodeclass;
    }

    /**
     * @param nodeclass The nodeclass to set.
     */
    public void setNodeclass(String nodeclass) {
        this.nodeclass = nodeclass;
    }

    /** 
     * @return Returns the nodeopen. 
     */
    public String getNodeopen() {
        return nodeopen;
    }

    /**
     * @param nodeopen The nodeopen to set.
     */
    public void setNodeopen(String nodeopen) {
        this.nodeopen = nodeopen;
    }
    
    /** 
     * @return Returns the treeentity. 
     */
    public String getTreeentity() {
        return treeentity;
    }

    /**
     * @param treeentity The treeentity to set.
     */
    public void setTreeentity(String treeentity) {
        this.treeentity = treeentity;
    }

    /** 
     * @return Returns the treenodeurl. 
     */
    public PFUrl getTreenodeurl() {
        return treenodeurl;
    }

    /**
     * @param treenodeurl The treenodeurl to set.
     */
    public void setTreenodeurl(PFUrl treenodeurl) {
        this.treenodeurl = treenodeurl;
    }
    
    /**
     * NOTE: This is not yet used.
     * 
     * @return Returns the treeOpenmode.
     */
    public zXType.treeOpenMode getTreeOpenmode() {
        return treeOpenmode;
    }
    
    /**
     * @param treeOpenmode The treeOpenmode to set.
     */
    public void setTreeOpenmode(zXType.treeOpenMode treeOpenmode) {
        this.treeOpenmode = treeOpenmode;
    }
    
    /**
     * @return Returns the exprFHRecTree.
     */
    public ExprFHRecTree getExprFHRecTree() {
        if (this.exprFHRecTree == null) {
            this.exprFHRecTree = new ExprFHRecTree();
        }
        return exprFHRecTree;
    }
    
    /**
     * @param exprFHRecTree The exprFHRecTree to set.
     */
    public void setExprFHRecTree(ExprFHRecTree exprFHRecTree) {
        this.exprFHRecTree = exprFHRecTree;
    }
    
    /**
     * @return Returns the itemBaseQuery.
     */
    public String getItemBaseQuery() {
        return itemBaseQuery;
    }
    
    /**
     * @param itemBaseQuery The itemBaseQuery to set.
     */
    public void setItemBaseQuery(String itemBaseQuery) {
        this.itemBaseQuery = itemBaseQuery;
    }
    
    /**
     * @return Returns the recTree.
     */
    public RecTree getRecTree() {
        return recTree;
    }
    
    /**
     * @param pobjRecTree The recTree to set.
     */
    public void setRecTree(RecTree pobjRecTree) {
        this.recTree = pobjRecTree;
    }
    
    //------------------------ Digester helper methods.
    
    /**
     * @deprecated Using BooleanConverter
     * @param nodeaddparity The nodeaddparity to set.
     */
    public void setNodeaddparity(String nodeaddparity) {
    	this.nodeaddparity = StringUtil.booleanValue(nodeaddparity);
    }
    
    /**
     * @deprecated Using BooleanConverter
     * @param itemaddparity The itemaddparity to set.
     */
    public void setItemaddparity(String itemaddparity) {
    	this.itemaddparity = StringUtil.booleanValue(itemaddparity);
    }
    
    /**
     * @param treeopenmode The treeopenmode to set.
     */
    public void setTreeopenmode(String treeopenmode) {
        if (treeopenmode != null) {
            this.treeOpenmode = zXType.treeOpenMode.getEnum(treeopenmode);
        } else {
            this.treeOpenmode = zXType.treeOpenMode.tomClosed;
        }
    }
    
    //------------------------ Private helper methods.
    
    /**
     * Contruct the url for a popup.
     * 
     * <pre>
     * 
     * DGS28FEB2003: New code for a popup
     * Determine what the URL should be for this row (a popup). Note that this function is similar
     * to the normal constructRowURL but uses the given URL rather than that of the listform itself.
     * We use the entity object to get entity name and PK, and we use the URL object to get the
     * main url (this has come from a popup ref url, not the listform's url).
     * </pre>
     *
     * @param pobjEntity The entity for url 
     * @param pobjUrl The url to start from. 
     * @param pstrConfirm The confirm message to use. This optiona, default should be null. 
     * @return Returns a popup url.
     * @throws ZXException Thrown if constructPopupRowURL fails. 
     */
    private String constructPopupRowURL(PFEntity pobjEntity, 
    								    PFUrl pobjUrl, 
    								    String pstrConfirm) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjEntity", pobjEntity);
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
            
            constructPopupRowURL = getPageflow().constructURL(pobjUrl);
            
            /**
             * Be smart and replace rRef with fRefx if frame x is requested
             * Only replace first instance
             * DGS28FEB2003: Again, don't do this for popups
             */
            constructPopupRowURL = getPageflow().wrapRefUrl(getPageflow().resolveDirector(pobjUrl.getFrameno()),
                    							            constructPopupRowURL,
                    										pstrConfirm,
                    										false, true);
            
            return constructPopupRowURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Contruct the url for a popup.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjEntity = "+ pobjEntity);
                getZx().log.error("Parameter : pobjUrl = "+ pobjUrl);
                getZx().log.error("Parameter : pstrConfirm = "+ pstrConfirm);
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
     * Contruct a url for a row.
     *
     * @param pobjURL The Pageflow url. 
     * @param pobjEntity The pageflow object the link belongs to. 
     * @param pstrID The id of the linked page 
     * @return Returns the url for the link
     * @throws ZXException Thrown if constructURL fails. 
     */
    private String constructURL(PFUrl pobjURL, 
    						    PFEntity pobjEntity, 
    						    String pstrID) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjURL", pobjURL);
            getZx().trace.traceParam("pobjEntity", pobjEntity);
            getZx().trace.traceParam("pstrID", pstrID);
        }

        String constructURL = ""; 
        
        try {
            
            /**
             * If no URL for the list: we're done
             */
            if (StringUtil.len(pobjURL.getUrl()) == 0) {
                return constructURL;
            }
            
            if (pobjURL.getUrlType().equals(zXType.pageflowUrlType.putPopup)) {
                /**
                 * url is appended by unique id. This is then undone after constructing the row URL
                 * and generating the popup (as the same url is used by all rows, not just this one).
                 * Note that we use constructUrl here, not constructURL, because we don't want the
                 * entity and pk appending to the url, so the standard function is correct.
                 */
                String strPopupName = pobjURL.getUrl();
                pobjURL.setUrl(strPopupName + "_" + pstrID);
                
                constructURL = getPageflow().constructURL(pobjURL);
                
                /**
                 * Now generate the popup menu javascript.
                 */
                boolean blnStartMenu = true;
                
                PFRef objRef;
                
                int intRefs = getPopups().size();
                for (int i = 0; i < intRefs; i++) {
                    objRef = (PFRef)getPopups().get(i);
                    
                    /**
                     * Only interested in popups for this name...
                     */
                    if (objRef.getName().equalsIgnoreCase(strPopupName)) {
                        /**
                         * ...that are active
                         */
                        if (getPageflow().isActive(objRef.getUrl().getActive())) {
                            /**
                             * Can only start the menu once.
                             */
                            if (blnStartMenu) {
                                blnStartMenu = false;
                                
                                /**
                                 * Start new entire popup menu:
                                 */
                                getPageflow().getPage().popupMenuStart(pobjURL.getUrl());
                            }
                            
                            /**
                             * Popup menu name (in url) has already been suffixed by PK to keep it unique
                             */
                            getPageflow().getPage().
                            	popupMenuOption(pobjURL.getUrl(),
                            	        		getPageflow().resolveLabel(objRef.getLabel()),
                            	        		constructPopupRowURL(pobjEntity,
                            	        		objRef.getUrl(),
                            	        		getZx().resolveDirector(getPageflow().resolveLabel(objRef.getConfirm()))),
                            	        		objRef.getImg(), 
                            	        		objRef.getImgover(), 
                            	        		objRef.isStartsubmenu());
                            
                        }
                    }
                }
                
                if (!blnStartMenu) {
                    /**
                     * We found at least one active popup, so end the popu
                     */
                    getPageflow().getPage().popupMenuEnd();
                }
                
                /**
                 * Copy the original url back (without the PK suffix)
                 */
                pobjURL.setUrl(strPopupName);
                
            } else {
                String strFrameno = getPageflow().resolveDirector(pobjURL.getFrameno());
                constructURL = getPageflow().wrapRefUrl(strFrameno,
                                                        getPageflow().constructURL(pobjURL),
                        								"", false, false);
            } // popup yes / no
            
            return constructURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Contruct a url for a row.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjURL = "+ pobjURL);
                getZx().log.error("Parameter : pobjEntity = "+ pobjEntity);
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
     * Generate HTML for a single node and its sub nodes.
     * 
     * @param pobjTreenodeurl Tree node url.
     * @param pobjItemUrl Item url.
     * @param pcolEntities Collectoin of PFEntity's 
     * @param pobjNode The Rec Tree Node 
     * @param pobjTreeEntity The tree PFEntity 
     * @param pblnEven Whether the row is odd or even. 
     * @param pintLevel The level of the tree node. Default should be 0.
     * @param pintSeq The sequence number. Default should be 0.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if generateNodeHTML fails. 
     */
    private zXType.rc generateNodeHTML(PFUrl pobjTreenodeurl,
    								   PFUrl pobjItemUrl,
    								   ZXCollection pcolEntities, 
                                       RecTreeNode pobjNode, 
                                       PFEntity pobjTreeEntity, 
                                       boolean pblnEven, 
                                       int pintLevel, 
                                       int pintSeq) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pcolEntities", pcolEntities);
            getZx().trace.traceParam("pobjNode", pobjNode);
            getZx().trace.traceParam("pobjTreeEntity", pobjTreeEntity);
            getZx().trace.traceParam("pblnEven", pblnEven);
            getZx().trace.traceParam("pintLevel", pintLevel);
            getZx().trace.traceParam("pintSeq", pintSeq);
        }

        zXType.rc generateNodeHTML = zXType.rc.rcOK;
        
        try {
            
            boolean blnNodeOpen = false; 
            RecTreeNode objNode;
            String strSubMenuClass = "";
            String strJSCAll;
            String strNodePK = pobjNode.getBo().getPKValue().getStringValue(); // Resolve the pk earlier so we can reuse it.
            
            /**
             * Make the BO of the tree entity the active one in the BO context
             * as it is probably refered to in directors. Note that in other types
             * of lists (eg listForms), we re-use the same BO over and over again
             * so we do not have to bother but a tree is read in core
             * and thus has many different BO's
             */
            getZx().getBOContext().setEntry(pobjTreeEntity.getName(), pobjNode.getBo());
            pobjTreeEntity.setBo(pobjNode.getBo());
            
            /**
             * Set node into context of recTree expression library
             */
            this.exprFHRecTree.setNode(pobjNode);
            
            /**
             * Create a unique node id (used to make JS functions unique)
             * by concatenating level and sequence
             */
            String strNodeId = pintLevel + "_" + pintSeq + "_" + strNodePK;
            
            /**
             * Start level
             */
            getPageflow().getPage().s.append("<tr>");
            
            /**
             * Main button, level opener
             */
            String strMenuItemClass = "";
            getPageflow().getPage().s.append("<td ")
		            				 .appendAttr("class", strMenuItemClass)
		            				 .appendAttr("width", getPageflow().getPage().getWebSettings().getTreeColumn1())
		            				 .appendNL('>');
            
            getPageflow().getPage().s.append(PageBuilder.space(pobjNode.getBo().getValue("lvl").intValue() * 0));
            
            int intNodes = pobjNode.getChildNodes().size();
            if (intNodes > 0) {
                blnNodeOpen = getPageflow().isActive(getNodeopen());
                String strImage;
                if (blnNodeOpen) {
                    strImage = "../images/menuOpen.gif";
                } else {
                    strImage = "../images/menuClosed.gif";
                }
                
                strJSCAll = "window.document.body.style.cursor='wait';zXTreeLevelToggle(this, '_tree_" 
                            + strNodePK + "');window.document.body.style.cursor='';";
                
                getPageflow().getPage().s.append("<img ").appendAttr("id", "_treeImg_" + strNodePK)
                                        .appendAttr("src", strImage)
                                        .appendAttr("border", 0)
                                        .appendAttr("onMouseDown", strJSCAll)
                                        .appendNL('>');
                
            } else {
                /**
                 * End node
                 */
                getPageflow().getPage().s.appendNL();
            }
            
            getPageflow().getPage().s.appendNL("</td>");	
            
            strMenuItemClass = getPageflow().resolveDirector(getNodeclass());
            if (StringUtil.len(strMenuItemClass) == 0) {
                if (pblnEven) {
                    strMenuItemClass = "zxNor"; 
                    strSubMenuClass = "zxNor";
                } else {
                    strMenuItemClass = "zxAlt";
                    strSubMenuClass = "zxAlt";
                }
            } else {
                if (isNodeaddparity()) {
                    if (pblnEven) {
                        strSubMenuClass = strMenuItemClass + "odd";
                        strMenuItemClass = strMenuItemClass + "odd";
                    } else {
                        strSubMenuClass = strMenuItemClass + "even";
                        strMenuItemClass = strMenuItemClass + "even";
                    }
                }
            }

            /**
             * Node URL
             */
            getPageflow().getPage().s.append("<td ")
            				.appendAttr("class", strMenuItemClass)
            				.appendAttr("align", "center")
            				.appendAttr("width", getPageflow().getPage().getWebSettings().getTreeColumn2())
            				.appendNL('>');
            if (!"#dummy".equals(pobjTreenodeurl.getUrl())) {
	            strJSCAll = constructURL(pobjTreenodeurl, pobjTreeEntity, strNodeId);
	            getPageflow().getPage().s.append("<img src='../images/menuItem.gif' ")
	                	.appendAttr("onMouseOver", "this.src='../images/menuItemOver.gif'")
	                	.appendAttr("onMouseOut", "this.src='../images/menuItem.gif'")
	                	.appendAttr("onClick", strJSCAll)
	                	.appendNL('>');
            }
            getPageflow().getPage().s.appendNL("</td>");
                
            /**
             * Generate label bit
             */
            getPageflow().getPage().s.append("<td ")
                				.appendAttr("class", strSubMenuClass)
                				.appendAttr("width", getPageflow().getPage().getWebSettings().getTreeColumn3())
                				.appendNL('>');
            
            getPageflow().getPage().s.append(PageBuilder.space(pobjNode.getBo().getValue("lvl").intValue() * 0));
            
            getPageflow().getPage().s.append(pobjNode.getBo().formattedString("label")).appendNL();
            getPageflow().getPage().s.appendNL("</td>");
            
            getPageflow().getPage().s.appendNL("</tr>");
            
            /**
             * Open row / table where div is held
             */
            getPageflow().getPage().s.appendNL("<tr>\n<td></td>\n<td></td>\n<td>");
            
            /**
             * V1.4:3 - Set the displayStyle according to the open mode for
             * the node
             */
            String strDisplayStyle;
            if (blnNodeOpen) {
                strDisplayStyle = "";
            } else {
                strDisplayStyle = "none";
            }
            
            getPageflow().getPage().s.append("<div id=\"_tree_" + strNodePK + "\" style='display:" + strDisplayStyle + "'>").appendNL();
            getPageflow().getPage().s.appendNL("<table width='100%' border=0>");
            
            /**
             * First handle the items
             */
            handleItems(pobjItemUrl, pcolEntities, pobjNode, pintLevel);
            
            /**
             * And now for the children
             */
            if (intNodes > 0) {
                Iterator iter = pobjNode.getChildNodes().iterator();
                while (iter.hasNext()) {
                    objNode = (RecTreeNode)iter.next();
                    pintSeq = pintSeq + 1;
                    pblnEven = !pblnEven;
                    
                    generateNodeHTML(pobjTreenodeurl,
                    				 pobjItemUrl,
                    				 pcolEntities,
                    				 objNode,
                    				 pobjTreeEntity,
                    				 pblnEven,
                    				 pintLevel + 1,
                    				 pintSeq);
                }
            } // No kids
            
            /**
             * Close table div
             */
            getPageflow().getPage().s.appendNL("</table>");
            getPageflow().getPage().s.appendNL("</div>");
            
            getPageflow().getPage().s.appendNL("</td></tr>");
            
            return generateNodeHTML;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for a single node and its sub nodes.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pcolEntities = "+ pcolEntities);
                getZx().log.error("Parameter : pobjNode = "+ pobjNode);
                getZx().log.error("Parameter : pobjTreeEntity = "+ pobjTreeEntity);
                getZx().log.error("Parameter : pblnEven = "+ pblnEven);
                getZx().log.error("Parameter : pintLevel = "+ pintLevel);
                getZx().log.error("Parameter : pintSeq = "+ pintSeq);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            generateNodeHTML = zXType.rc.rcError;
            return generateNodeHTML;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(generateNodeHTML);
                getZx().trace.exitMethod();
            }
        }
    }
     
    /**
     * Process the items (if there are any).
     * 
     * @param pobjItemUrl Tree Item Url.
     * @param pcolEntities A collection of PFEntities. 
     * @param pobjNode The Rec tree node 
     * @param pintLevel The level of the Rec Tree node. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if handleItems fails. 
     */
    private zXType.rc handleItems(PFUrl pobjItemUrl,
    							  ZXCollection pcolEntities, 
                                  RecTreeNode pobjNode, 
                                  int pintLevel) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pcolEntities", pcolEntities);
            getZx().trace.traceParam("pobjNode", pobjNode);
            getZx().trace.traceParam("pintLevel", pintLevel);
        }

        zXType.rc handleItems = zXType.rc.rcOK;
        DSRS objRS = null;
        
        try {
            PFEntity objEntity;             // Loop over variable
            String strQry = "";             // Query components
            String strSelectClause = "";
            String strWhereClause = "";
            String strOrderBy = "";
            boolean blnReverse = false;
            boolean blnEven = false;        // Odd / even for class
            String strClass;                // Class to use
            String strUrl;                  // URL to associate with row
            DSHandler objDSHandler;         // Data-source handler
            PFEntity objTheEntity;          // For channels we only have one entity
            
            /**
             * If there is only one entity, it is the tree node entity and thus
             * we should not bother with items
             */
            if (pcolEntities.size() <= 1) {
                return handleItems;
            }
            
            /**
             * Make sure we do not break any rules
             */
            if (!getPageflow().validDataSourceEntities(pcolEntities)) {
                throw new ZXException("Unsupported combination of data-source handlers");
            }
            
            objTheEntity = (PFEntity)pcolEntities.iterator().next();
            
            // TODO : Bug in COM+ C1.5 should notify DB of this.
            objDSHandler = objTheEntity.getBo().getDS();
            
            /**
             * Now we have to create query
             */
            Iterator iter;
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                strSelectClause = objTheEntity.getSelectlistgroup();
                
            } else {
                iter = pcolEntities.iterator();
                int intNumItemEntities = 0;
                
                /**
                 * This array may be larger than the actual number of element in them.
                 */
                ZXBO [] arrBO = new ZXBO[pcolEntities.size()];
                String [] arrGrp = new String[pcolEntities.size()];
                boolean[] arrResolveFK = new boolean[pcolEntities.size()];
                
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    /**
                     * Skip the tree entity one
                     */
                    if (!objEntity.getName().equalsIgnoreCase(getTreeentity())) {
                        
                        intNumItemEntities ++;
                        
                        arrBO[intNumItemEntities] = objEntity.getBo();
                        arrGrp[intNumItemEntities] = objEntity.getSelectlistgroup();
                        arrResolveFK[intNumItemEntities] = isResolvefk();
                    }
                }
                
                if (StringUtil.len(getItemBaseQuery()) ==0) {
                    setItemBaseQuery(getZx().getSql().selectQuery(arrBO, arrGrp, arrResolveFK));
                }
                
            }
            
            /**
             * Handle where clauses
             */
            iter = pcolEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                /**
                 * Skip the tree entity one
                 */
                if (!objEntity.getName().equalsIgnoreCase(getTreeentity())) {
                    if (StringUtil.len(objEntity.getWheregroup()) > 0) {
                        getPageflow().processAttributeValues(objEntity);
                        
                        if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                            strWhereClause = objEntity.getPkwheregroup();
                        } else {
                            strQry = strQry + " AND " + getZx().getSql().processWhereGroup(objEntity.getBo(), objEntity.getPkwheregroup());
                        }
                    }
                }
                
            }
            
            /**
             * And order by
             */
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                
                if (objTheEntity.getGroupbygroup().charAt(0) == '-') {
                    strOrderBy = objTheEntity.getGroupbygroup().substring(1);
                    blnReverse = true;
                }
                
                objRS = objDSHandler.boRS(objTheEntity.getBo(),
                                          strSelectClause,
                                          strWhereClause,
                                          isResolvefk(),
                                          strOrderBy, blnReverse,
                                          // TODO : We may want to add limit support to PFRecTree as well.
                                          0, 0);
                        
            } else {
                iter = pcolEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    /**
                     * Skip the tree entity one
                     */
                    if (!objEntity.getName().equalsIgnoreCase(getTreeentity())) {
                        if (StringUtil.len(objEntity.getGroupbygroup()) > 0) {
                            
                            if (StringUtil.len(strOrderBy) > 0) {
                                strOrderBy = strOrderBy + ",";
                            }
                            
                            if (objEntity.getGroupbygroup().startsWith("-")) {
                                strOrderBy = strOrderBy + getZx().getSql().orderByClause(objEntity.getBo(),objEntity.getGroupbygroup().substring(2), true);
                            } else {
                                strOrderBy = strOrderBy + getZx().getSql().orderByClause(objEntity.getBo(), objEntity.getGroupbygroup(), false);
                            }
                        }
                    }
                }
                
                if (StringUtil.len(strOrderBy) > 0) {
                    strQry = strQry + " ORDER BY " + strOrderBy;
                }
                
                objRS = ((DSHRdbms)objDSHandler).sqlRS(itemBaseQuery + strQry);
            } // Channel or RDBMS
            
            /**
             * Start generating the html :
             */
            getPageflow().getPage().s.appendNL("<tr> <td></td> <td></td> <td>");
            
            getPageflow().getPage().s.append("<table ")
            				.appendAttr("width", StringUtil.len(getWidth()) > 0? getWidth() :"100%")
            				.appendNL('>');
            
            getPageflow().getPage().listHeaderOpen();
            
            iter = pcolEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                if (!objEntity.getName().equalsIgnoreCase(getTreeentity())) {
                    getPageflow().getPage().listHeader(objEntity.getBo(), objEntity.getListgroup(),"", isResolvefk());
                }
            }
            
            getPageflow().getPage().listHeaderClose();
            
            int j = 0;
            while (!objRS.eof()) {
                j++;
                
                iter = pcolEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    if (!objEntity.getName().equalsIgnoreCase(getTreeentity())) {
                        objRS.rs2obj(objEntity.getBo(), objEntity.getSelectlistgroup());
                    }
                }
                
                strClass = getPageflow().resolveDirector(getItemclass());
                if (StringUtil.len(strClass) > 0) {
                    if (blnEven) {
                        strClass = strClass + "Even";
                    } else {
                        strClass = strClass + "Odd";
                    }
                }
                
                strUrl = constructURL(pobjItemUrl, 
                					  (PFEntity)pcolEntities.iterator().next(), 
                					  pintLevel +  "_" +  j);
                
                getPageflow().getPage().listRowOpen(blnEven, strUrl, strClass);
                
                blnEven = !blnEven;
                
                iter = pcolEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    if (!objEntity.getName().equalsIgnoreCase(getTreeentity())) {
                        getPageflow().getPage().listRow(objEntity.getBo(), objEntity.getListgroup(), 0);
                    }
                }
                
                getPageflow().getPage().listRowClose();
                
                objRS.moveNext();
            }
            
            getPageflow().getPage().s.appendNL("</table>");
            
            getPageflow().getPage().s.appendNL("</td><td></td></tr>");
            
            return handleItems;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process the items (if there are any).", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pcolEntities = "+ pcolEntities);
                getZx().log.error("Parameter : pobjNode = "+ pobjNode);
                getZx().log.error("Parameter : pintLevel = "+ pintLevel);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            handleItems = zXType.rc.rcError;
            return handleItems;
        } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(handleItems);
                getZx().trace.exitMethod();
            }
        }
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
        
        ZXCollection colEntities;       // Relevant entries
        PFEntity objEntity;             // Loop over variable
        String strPK;                   // PK as string
        PFEntity objTreeEntity;         // Entity for tree (recursive)
        RecTreeNode objTreeRoot;        // Resolved root of the tree
        
		try {
		    /**
		     * Resolve entities
		     */
		    colEntities = getPageflow().getEntityCollection(this, 
                                                            zXType.pageflowActionType.patNull,
                                                            zXType.pageflowQueryType.pqtAll);
		    Iterator iter;
		    if (colEntities != null) {
		        getPageflow().setContextEntities(colEntities);
                
		        iter = colEntities.iterator();
		        while (iter.hasNext()) {
		            objEntity = (PFEntity)iter.next();
                    
                    getPageflow().setContextEntity(objEntity);
		            getPageflow().processAttributeValues(objEntity);
		            
		            strPK = getPageflow().resolveDirector(objEntity.getPk());
		            if (StringUtil.len(strPK) > 0) {
		                objEntity.getBo().setPKValue(strPK);
		                objEntity.getBo().loadBO(objEntity.getSelectlistgroup());
		            } else {
		                if (StringUtil.len(objEntity.getPkwheregroup()) > 0) {
		                    objEntity.getBo().loadBO(objEntity.getSelectlistgroup(), objEntity.getPkwheregroup(), isResolvefk());
		                }
		            }
		            
		        }
		        
		    } else {
                getZx().trace.addError("Unable to get entities for action");
                go = zXType.rc.rcError;
                return go;
		    }
		    
		    /**
		     * Handle title and any outstanding messages
		     */
		    getPageflow().getPage().formTitle(zXType.webFormType.wftEdit, getPageflow().resolveLabel(getTitle()));
		    getPageflow().processMessages(this);
		    
		    /**
		     * Clear the item base query (may not be empty if the tree action is called
		     * multiple times for one page
		     */
		    setItemBaseQuery("");
		    
		    /**
		     * Get the tree entity
		     */
		    objTreeEntity = (PFEntity)colEntities.get(getPageflow().resolveDirector(getTreeentity()));
		    if (objTreeEntity == null) {
		        throw new Exception("Unable to find tree entity : " 
		        		   	        + getPageflow().resolveDirector(getTreeentity()));
		    }
		    
            /**
             * Create instance of recTree
             */
            setRecTree(new RecTree());
            
            /**
             * Install expression handler
             */
            try {
                setExprFHRecTree(new ExprFHRecTree());
                getZx().getExpressionHandler().registerFH("recTree", getExprFHRecTree());
            } catch (Exception e) {
                throw new RuntimeException("FATAL EXCEPTION : Failed to init the expression handler.");
            }
            
		    /**
		     * Resolve the tree
		     */
		    objTreeRoot = getRecTree().resolve(objTreeEntity.getBo());
		    if (objTreeRoot == null) {
		        throw new Exception("Failed to get a root RecTree");
		    }
		    
		    /**
		     * Set the resolved tree in the expression function handler so we
		     * can use the recTree library in directors
		     */
		    getExprFHRecTree().setRoot(objTreeRoot);
            
		    /**
		     * Handle any parameter bag references in URL
		     */
		    PFUrl objTreenodeurl = getPageflow().tryToResolveParameterDirectorAsUrl(getTreenodeurl());
		    PFUrl objItemUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getItemurl());
		    
            /**
             * And generate the HTML
             */
            
		    /**
		     * Generate top border
		     */
		    getPageflow().getPage().s.append("<table border=0 width=\"").append(getWidth()).append("\">").appendNL();
		    
		    getPageflow().getPage().s.appendNL("<tr>");
		    
		    /**
		     * Action menu button
		     */
		    getPageflow().getPage().s.append("<td ")
                	.appendAttr("class", "zXMenuTopNoAlign")
                	.appendAttr("align", "left")
                	.appendAttr("valign", "center")
                	.appendAttr("width", getPageflow().getPage().getWebSettings().getTreeColumn1()).appendNL('>');
		    
		    /**
		     * Add toggle image to main level of menu
		     */
		    getPageflow().getPage().s.append("<img ")
			                .appendAttr("id", "_mnuLvlImg_Main")
			                .appendAttr("src", "../images/menuClosed.gif")
			                .appendAttr("onClick", "javascript:zXToggleWholeTree(this);")
			                .appendNL('>');
            
		    getPageflow().getPage().s.appendNL("</td>");
		    
		    /**
		     * Level openener, main button
		     */
		    getPageflow().getPage().s.append("<td ")
			                .appendAttr("class", "zxListHeader")
			                .appendAttr("width", getPageflow().getPage().getWebSettings().getTreeColumn2())
			                .appendNL('>');
		    getPageflow().getPage().s.appendNL("</td>");
		    
		    /**
		     * Header area
		     */
		    getPageflow().getPage().s.append("<td ")
                	.appendAttr("class", "zxListHeader")
                	.appendAttr("width", getPageflow().getPage().getWebSettings().getTreeColumn3()).appendNL('>');
		    getPageflow().getPage().s.appendNL("</td>");
		    
		    getPageflow().getPage().s.appendNL("</tr>");
            
		    generateNodeHTML(objTreenodeurl,
		    				 objItemUrl,
		    				 colEntities,
		    				 objTreeRoot,
		    				 objTreeEntity,
		    				 true,
		    				 0,
		    				 0);
		    
		    getPageflow().getPage().s.appendNL("</table>");
		    
		    /**
		     * Handle buttons
		     */
		    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftMenu);
		    getPageflow().processFormButtons(this);
		    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftMenu);
		    
		    /**
		     * Handle footer and window title
		     */
		    getPageflow().handleFooterAndTitle(this, "Select node and action");
		    
		    /**
		     * And see where we want to go next
		     */
		    getPageflow().setAction(getPageflow().resolveLink(getLink()));
		    
		    /**
		     * Set node to nothing again
		     */
		    getExprFHRecTree().setNode(null);
		    
		    return go;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Execute the RecTree pageflow action", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    go = zXType.rc.rcError;
		    return go;
		} finally {
		    if(getZx().trace.isApplicationTraceEnabled()) {
		        getZx().trace.returnValue(go);
		        getZx().trace.exitMethod();
		    }
		}
    }
    
    /** 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();
        
        objXMLGen.taggedCData("treeentity", getTreeentity());
        objXMLGen.taggedCData("nodeclass", getNodeclass());
        objXMLGen.taggedValue("nodeaddparity", isNodeaddparity());
        objXMLGen.taggedCData("itemclass", getItemclass());
        objXMLGen.taggedValue("itemaddparity", isItemaddparity());
        objXMLGen.taggedCData("active", getActive());
        objXMLGen.taggedCData("nodeopen", getNodeopen());
        objXMLGen.taggedValue("treeopenmode", zXType.valueOf(getTreeOpenmode()));
        
        getDescriptor().xmlUrl("treenodeurl", getTreenodeurl());
        getDescriptor().xmlUrl("itemurl", getItemurl());
    }
    
    /**
     * @see PFAction#clone(Pageflow)
     */
    public PFAction clone(Pageflow pobjPageflow) {
        PFRecTree cleanClone = (PFRecTree)super.clone(pobjPageflow);
        
        cleanClone.setActive(getActive());
        cleanClone.setItemaddparity(isItemaddparity());
        cleanClone.setItemBaseQuery(getItemBaseQuery());
        cleanClone.setItemclass(getItemclass());
        
        if (getItemurl() != null) {
            cleanClone.setItemurl((PFUrl)getItemurl().clone());
        }
        
        cleanClone.setNodeaddparity(isNodeaddparity());
        cleanClone.setNodeclass(getNodeclass());
        cleanClone.setNodeopen(getNodeopen());
        cleanClone.setTreeentity(getTreeentity());
        
        if (getTreenodeurl() != null) {
        	cleanClone.setTreenodeurl((PFUrl)getTreenodeurl().clone());
        }
        
        cleanClone.setTreeOpenmode(getTreeOpenmode());
        
        return cleanClone;
    }
}