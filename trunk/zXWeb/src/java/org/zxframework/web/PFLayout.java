/*
 * Created on Nov 12, 2004 by Michael Brewer
 * $Id: PFLayout.java,v 1.1.2.23 2006/07/17 16:28:17 mike Exp $ 
 */
package org.zxframework.web;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * PFLayout - Allows for custom layouts.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFLayout extends PFAction {

    //------------------------ Members
    
    private String template;
    private List subactions;
    
    private String layout;
    private ZXCollection cells;
    
    /** The token that represents a action placeholder. **/
    private static final String ACTION_TOKEN = "$action";
    /** Safety check to prevent recursive calls due to programming errors.. **/
    private int actionsExecuted = 1;
    /** The resolved value of the layout type.**/
    private zXType.pageflowLayout  enmPageflowLayout;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFLayout() {
        super();
    }
    
    //------------------------ Getters & Setters.
    
    /**
     * The filename of the template to use. Relative to the pageflow directory.
     * 
     * @return Returns the template filename.
     */
    public String getTemplate() {
        return template;
    }
    
    /**
     * @param template The filename of the template.
     */
    public void setTemplate(String template) {
        this.template = template;
    }
    
    /**
     * A collection (ArrayList)(PFLayoutComponent) of components  within this pageflow action.
     * 
     * @return Returns the subactions for this pageflow actions.
     */
    public List getSubactions() {
        return subactions;
    }
    
    /**
     * @param subactions The subactions for this pageflow action.
     */
    public void setSubactions(List subactions) {
        this.subactions = subactions;
    }

    /**
     * A collection (ZXCollection)(PFLayoutCell) of cells in the layout.
     * 
     * @return Returns the cells.
     */
    public ZXCollection getCells() {
        return cells;
    }
    
    /**
     * @param cells The cells to set.
     */
    public void setCells(ZXCollection cells) {
        this.cells = cells;
    }
    
    /**
     * The layout method to use.
     * 
     * @return Returns the layout.
     */
    public String getLayout() {
        return layout;
    }
    
    /**
     * @param layout The layout to set.
     */
    public void setLayout(String layout) {
        this.layout = layout;
    }
    
    //------------------------ Private helper methods.
    
    /**
     * Generate the page from a template file.
     * 
     * <pre>
     * Create a template file or reuse one of the standard templates. 
     * To specify a postion of a sub action is to use the $action placeholder.
     * Add the actions to be embedded. The position of the sub action is relavant.
     * OR
     * Just use the html template as it. This will allow you to put in any despoke html.
     * 
     * When using a template you do not need to define cells.
     * </pre>
     * 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if goTemplate fails.
     */
    private zXType.rc goTemplateLayout() throws ZXException {
    	if(getZx().trace.isFrameworkTraceEnabled()) {
    		getZx().trace.enterMethod();
    	}
    	
        zXType.rc goTemplate = zXType.rc.rcOK;
        
        try {
	        String fileName = getZx().fullPathName(getZx().getPageflowDir())  
	        				  + File.separatorChar + getPageflow().resolveDirector(this.template);
	        File file = new File(fileName);
	        
	        if (file.isFile()) {
	            
	            /** Read in the template file. **/
	            FileReader in = new FileReader(file);
	            int pos = 0;
	            char[] c = new char[1024];
	            StringBuffer temp = new StringBuffer(1024);
	            while (in.read(c, pos, pos + c.length) != -1) {
	               temp.append(c);
	            }
	            // NOTE : StringBuffer in 1.4 has indexOf but not in 1.3.1
	            if (this.subactions != null && this.subactions.size() > 0 && temp.toString().indexOf(ACTION_TOKEN + "1") != -1) {
	                
	                StringBuffer html = new StringBuffer(getPageflow().getPage().flush(true));
	                StringBuffer strTemplateCopy = new StringBuffer(temp.toString());
	                
	                int intIndexOf =0;
	                int i = 1;
	                String tmpltePlchldr;
	                String actionHtml;
	                
	                PFLayoutComponent objPFLayoutComponent;
	                int intSubActions = this.subactions.size();
	                for (int j = 0; j < intSubActions; j++) {
	                    objPFLayoutComponent = (PFLayoutComponent)this.subactions.get(j);
	                    
	                    this.actionsExecuted++;
	                    if (this.actionsExecuted > 50) {
	                        getPageflow().getPage().errorMsg("Max pageflow actions executions exceeded.");
	                        break;
	                    }
	                    
	                    if (getPageflow().isActive(objPFLayoutComponent.getActive())) {
		                    tmpltePlchldr = ACTION_TOKEN + i;
		                    
		                    intIndexOf = strTemplateCopy.toString().indexOf(tmpltePlchldr);
		                    if (intIndexOf == -1) {
		                        i = 1;
		                        tmpltePlchldr = ACTION_TOKEN + i;
		                        html.append(strTemplateCopy);
		                        // Start again 
		                        strTemplateCopy = new StringBuffer(temp.toString());
		                        intIndexOf = strTemplateCopy.toString().indexOf(tmpltePlchldr);
		                    }
		                    if (intIndexOf != -1) {
		                        String strAction = getPageflow().resolveLink(objPFLayoutComponent.getComponent());
		                        try {
		                            goTemplate = getPageflow().go(strAction);
		                            
		                            if (goTemplate.pos == zXType.rc.rcWarning.pos) {
		                                getPageflow().getPage().errorMsg("Failed to find or execute action : " + strAction);
		                            }
		                            
		                        } catch (Exception e) {
		                        	getZx().trace.addError("Failed to execute action", strAction, e);
		                        	
		                        	/**
		                        	 * Carry on even if we have an exception :
		                        	 */
		                            getPageflow().getPage().errorMsg("Failed to execute action " + strAction);
		                            // break;
		                        }
		                        actionHtml = getPageflow().getPage().flush(true);
		                        strTemplateCopy.replace(intIndexOf, intIndexOf + tmpltePlchldr.length(), actionHtml);
		                    } else {
		                        getZx().log.error("Failed to replace or execute subaction placeholder");
		                    }
		                    i++;
		                    
	                    }
	                    
	                }
	                
	                getPageflow().getPage().s.append(html);
	                
	                /**
	                 * Remove any remaining action tokens.
	                 */
	                if (strTemplateCopy.toString().indexOf(ACTION_TOKEN) != -1) {
	                    tmpltePlchldr = ACTION_TOKEN + i;
	                    intIndexOf = strTemplateCopy.toString().indexOf(tmpltePlchldr);
	                    while (intIndexOf != -1) {
	                        strTemplateCopy.replace(intIndexOf, intIndexOf + tmpltePlchldr.length(), "");
	                        i++;
	                        tmpltePlchldr = ACTION_TOKEN + i;
	                        intIndexOf = strTemplateCopy.toString().indexOf(tmpltePlchldr);
	                    }
	                }
	                getPageflow().getPage().s.append(strTemplateCopy);
	                
	            } else {
	                /** Print Template as is **/
	                getPageflow().getPage().s.append(temp);
	            }
	        } else {
	            throw new ZXException("Failed to find template file : " + fileName);
	        }
	        
	        return goTemplate;
	    } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process PFLayout action.", e);
	        if (getZx().throwException) throw new ZXException(e);
	        goTemplate = zXType.rc.rcError;
	        return goTemplate;
	    } finally {
	        if(getZx().trace.isFrameworkTraceEnabled()) {
	            getZx().trace.returnValue(goTemplate);
	            getZx().trace.exitMethod();
	        }
	    }    
    }
    
    /**
     * Display the page using the cell layout method.
     * 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if goCellLayout fails.
     */
    private zXType.rc goCellLayout() throws ZXException {
    	if (getZx().trace.isFrameworkTraceEnabled()) {
    		getZx().trace.enterMethod();
    	}
    	
        zXType.rc goCellLayout = zXType.rc.rcOK;
        
        try {
            /**
             * Check if we have just the right number of cells defined. The repository editor
             * could help us with this.
             */
            if (!verifyLayout()) {
                throw new Exception("Incorrect number of cells for the specific layout.");
            }
            
            /**
             * Start build the beginning of the cell layout.
             */
            goCellLayout = beginCells();
            
            if (getCells() != null) {
                // Defaults :
                int intCell = 1;
                
                PFLayoutCell objPFLayoutCell;
	            Iterator iter = getCells().values().iterator();
	            while (iter.hasNext()) {
	                objPFLayoutCell = (PFLayoutCell)iter.next();
	                
	                /**
	                 * Print the indevidual cells
	                 */
	                goCell(objPFLayoutCell, intCell);
	                
	                intCell++;
	            }
            }
            
            goCellLayout = endCells();
            
            return goCellLayout;
	    } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process goCellLayout.", e);
	        if (getZx().throwException) throw new ZXException(e);
	        goCellLayout = zXType.rc.rcError;
	        return goCellLayout;
	    } finally {
	        if(getZx().trace.isFrameworkTraceEnabled()) {
	            getZx().trace.returnValue(goCellLayout);
	            getZx().trace.exitMethod();
	        }
	    }    
	}
	
	/**
	 * Verify the we have the correct number of cells defined for the layout selected. 
	 * 
	 * @return Returns the return code of the method.
	 */
	private boolean verifyLayout() {
		boolean verifyLayout = false;
		
		/**
		 * Short circuit if null.
		 */
		if (getCells() == null) return verifyLayout;
		
		if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plOne)) {
			verifyLayout = getCells().size()==1;
		} else if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plTwo)) {
			verifyLayout = getCells().size()==2;
		} else if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plThree)) {
			verifyLayout = getCells().size()==2;
		} else if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plFour)) {
			verifyLayout = getCells().size()==3;
		} else if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plFive)) {
			verifyLayout = getCells().size()==3;
		} else if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plSix)) {
			verifyLayout = getCells().size()==4;
		} else if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plTab)) {
		    verifyLayout = getCells().size() >= 1;
		} else if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plTabv)) {
		    verifyLayout = getCells().size() >= 1;
		}
		return verifyLayout;
	}
   
	/**
	 * Builds the very beginning of the pageflow action cell layout.
	 * 
	 * @return Returns the return code of the method.
	 */
	private zXType.rc beginCells() {
		zXType.rc beginCells = zXType.rc.rcOK;
		
		/**
		 * Allow for a tab layout method :
		 */
		boolean blnVertical = this.enmPageflowLayout.equals(zXType.pageflowLayout.plTabv);
		
		if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plTab) || blnVertical) {
	        /**
	         * Generate the tabs across the top, one per page. The id must be 'zXPage' + actionName + 'N' where N
	         * is the page number. This will be used by javascript in zx.js when a tab is clicked.
	         */
	        String strTmp;
	        String strTitle;
	        
	        if (blnVertical) {
	            strTmp = "height=\"400\" width=\"20\" align=\"left\" ";
	        } else {
	            strTmp = " width=\"" + (StringUtil.len(getWidth())>0 ?getWidth():"90%") + "\" ";
	        }
	        
	        getPageflow().getPage().s.append("<table ").append(strTmp).append(">\n		<tr>").appendNL();
	        
	        /**
	         * Used to make the text vertical in the tabs :
	         */
	        if (blnVertical) {
	            strTmp = " class=\"zxVertical\" style=\"writing-mode: tb-rl; filter:flipH() flipV();\" ";
	        } else {
	            strTmp ="";
	        }
	        
	        int intTabs = 1;
	        PFLayoutCell objPFLayoutCell;
	        
	        Iterator iter = getCells().values().iterator();
	        while (iter.hasNext()) {
	            objPFLayoutCell = (PFLayoutCell)iter.next();
	            // Name format is zXPage<layoutname><tabnumber>
	            getPageflow().getPage().s.append("			<td NOWRAP id=\"zXPage").append(getName()).append(intTabs)
	            						 .append("\" onMouseDown=\"javascript:zXSelectTab(").append(intTabs).append(",'").append(getName()).append("'")
	            						 .append(blnVertical?",'Vertical'":"")
	            						 .append(");\"")
	            						 .append(strTmp)
	            						 .appendNL('>');
	            /**
	             * Get the title of the tab - If the title is not we will generate one for them
	             */
	            if (objPFLayoutCell.getTitle() != null && !objPFLayoutCell.getTitle().isEmpty()) {
	                strTitle = getPageflow().resolveLabel(objPFLayoutCell.getTitle());
	            } else {
	                strTitle = "Tab" + intTabs;
	            }
	            
	            getPageflow().getPage().s.appendNL(strTitle);
	            
	            getPageflow().getPage().s.appendNL("			</td>");
	            
	            /**
	             * Force a new row for vertical tabs except for the very last tab :
	             */
		        if (blnVertical && iter.hasNext()) {
		            getPageflow().getPage().s.appendNL("		</tr>");
		            getPageflow().getPage().s.appendNL("		<tr>");
		        }
		        
	            intTabs++;
	        }
	        
	        getPageflow().getPage().s.appendNL("		</tr>");
	        
	        /**
	         * Add some space
	         */
	        getPageflow().getPage().s.append("		<tr>\n		<td colspan=\"")
	        						 .append(getCells().size())
	        						 .append("\"><img src=\"../images/spacer.gif\" height=9></td>\n		</tr>")
	        						 .appendNL();
	        getPageflow().getPage().s.appendNL("</table>");
	        
		} else {
			/**
			 * Begin the main table that wraps the whole layout. 
			 */
			getPageflow().getPage().s.append("<table ")
									 .appendAttr("border", getPageflow().isDebugOn()?"1":"0")
									 .appendAttr("width", StringUtil.len(getWidth())>0?getWidth():"100%")
									 .appendNL('>');
			getPageflow().getPage().s.appendNL("	<tr>");
			
		}
		
		return beginCells;
	}
	
	/**
	 * Builds the very end of the pageflow action cell layout.
	 * 
	 * @return Returns the return code of the method.
	 */
	private zXType.rc endCells() {
		zXType.rc endCells = zXType.rc.rcOK;
		
		boolean blnVertical = this.enmPageflowLayout.equals(zXType.pageflowLayout.plTabv);
		
		if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plTab) || blnVertical) {
	        getPageflow().getPage().s.appendNL("<SCRIPT type=\"text/javascript\" language=\"JavaScript\">");
	        getPageflow().getPage().s.append("zXSelectTab(1,\"").append(getName()).append("\"")
	        									 .append(blnVertical?",\"Vertical\"":"")
	        									 .append(");")
	        									 .appendNL();
	        getPageflow().getPage().s.appendNL("</SCRIPT>");
	        
		} else {
			getPageflow().getPage().s.appendNL("</tr>");
			getPageflow().getPage().s.appendNL("</table>");
			
		}
		
		return endCells;
	}
    
	/**
	 * Build an individual cell.
	 * 
	 * @param pobjPFLayoutCell The cell to build.
	 * @param pintCell The cell null that we are to build.
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if goCell fails. 
	 */
	private zXType.rc goCell(PFLayoutCell pobjPFLayoutCell, int pintCell) throws ZXException{
	    zXType.rc goCell = zXType.rc.rcOK;
	    
	    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("pobjPFLayoutCell", pobjPFLayoutCell);
	        getZx().trace.traceParam("pintCell", pintCell);
	    }
	    
	    try {
	        /**
	         * Defaults :
	         */
            // Draw the contents vertically
            String strOrientation = "v";
            String strClazz = "";
            String strStyle = "";
            boolean blnBorder = false;
            String strBgColor = "";
            String strBorderColor = "";
            String strHeight = "";
            String strWidth = "";
            String strAlign = "left";
            String strValign = "top";
            
            if (StringUtil.len(pobjPFLayoutCell.getOrientation()) > 0) {
                strOrientation = getPageflow().resolveDirector(pobjPFLayoutCell.getOrientation());
            }
            if (StringUtil.len(pobjPFLayoutCell.getClazz()) > 0) {
                strClazz = getPageflow().resolveDirector(pobjPFLayoutCell.getClazz());
            }
            if (StringUtil.len(pobjPFLayoutCell.getStyle()) > 0) {
                strStyle = getPageflow().resolveDirector(pobjPFLayoutCell.getStyle());
            }
            if (StringUtil.len(pobjPFLayoutCell.getBgcolor()) > 0) {
                strBgColor = getPageflow().resolveDirector(pobjPFLayoutCell.getBgcolor());
            }
            if (StringUtil.len(pobjPFLayoutCell.getBorder()) > 0) {
                blnBorder = StringUtil.booleanValue(getPageflow().resolveDirector(pobjPFLayoutCell.getBorder()));
            }
            if (StringUtil.len(pobjPFLayoutCell.getBordercolor()) > 0) {
                strBorderColor = getPageflow().resolveDirector(pobjPFLayoutCell.getBordercolor());
            }
            if (StringUtil.len(pobjPFLayoutCell.getHeight()) > 0) {
                strHeight = getPageflow().resolveDirector(pobjPFLayoutCell.getHeight());
            }
            if (StringUtil.len(pobjPFLayoutCell.getWidth()) > 0) {
                strWidth= getPageflow().resolveDirector(pobjPFLayoutCell.getWidth());
            }
            if (StringUtil.len(pobjPFLayoutCell.getAlign()) > 0) {
                strAlign = getPageflow().resolveDirector(pobjPFLayoutCell.getAlign());
            }
            if (StringUtil.len(pobjPFLayoutCell.getValign()) > 0) {
                strValign = getPageflow().resolveDirector(pobjPFLayoutCell.getValign());
            }
            
            /**
             * Build the begining of the cell :
             */
            getPageflow().getPage().s.append("<!-- Begin cell").append(pintCell).append(" -->").appendNL();
            goCell = goCellBegin(pobjPFLayoutCell, pintCell);
            
            String strActionName;
            
            /**
             * Contain the pageflow action contents in a table.
             */
            getPageflow().getPage().s.append("<table ")
            					
							.append(blnBorder?" border=\"1\" ":"")
							.appendAttr("bgcolor", strBgColor)
							.appendAttr("bordercolor", strBorderColor)
							.appendAttr("height", strHeight)
							.appendAttr("width", strWidth)
							
							.appendAttr("class", strClazz)
							.appendAttr("style", strStyle)
							
							.appendNL('>');
            
            getPageflow().getPage().s.appendNL("		<tr>");
            
            /**
             * Because tabbed layout is so different we can only do the alignment settings over here.
             * This is because the tabs are not cell a bigger table... like the other layouts.. 
             */
            getPageflow().getPage().s.append("<td ")
						.appendAttr("align", strAlign)
						.appendAttr("valign", strValign)
						.appendAttr("height", StringUtil.len(strHeight)==0?"":strHeight)
						.appendAttr("width", StringUtil.len(strWidth)==0?"":strWidth)
						.appendNL('>');
             
            /**
             * If the orientation is horizontal. 
             * Then we will want to keep the sub action next to each other.
             */
            boolean blnHorizontal = strOrientation.charAt(0) == 'h'; 
            if (blnHorizontal) {
	            getPageflow().getPage().s.appendNL("<table>");
	            getPageflow().getPage().s.appendNL("		<tr>");
            }
            
            // Resolve the name of the cell
            String strAction;
            PFLayoutComponent objPFLayoutComponent;
            
            // Info for nest tabbed.
            int intTabs = 0; // Actual number of tabs
            String strCellName = getPageflow().resolveDirector(pobjPFLayoutCell.getName());
            String strTabName = getName() + strCellName;
            boolean blnTabbed = !blnHorizontal && strOrientation.charAt(0) == 't';
            
            int intSubActions = this.subactions.size();
            
            for (int i = 0; i < intSubActions; i++) {
                objPFLayoutComponent = (PFLayoutComponent)this.subactions.get(i);
                
                // Resolve the name of the action to perform.
                strActionName = getPageflow().resolveDirector(objPFLayoutComponent.getName());
                
                /**
                 * Check whether this subactions is assigned to this cell.
                 */
                if (strActionName.equalsIgnoreCase(strCellName)) {
                    
                    // Also make sure that the component is active
                    if (getPageflow().isActive(objPFLayoutComponent.getActive())) {
                        
                        if (blnTabbed) {
                            intTabs ++;
                            getPageflow().getPage().s.append("<div id=\"zXPage")
                            						.append(strTabName)
                            						.append(intTabs).append("\" style=\"display:none\">").appendNL();
                        } else if (blnHorizontal) {
            	            getPageflow().getPage().s.appendNL("			<td valign=\"top\">");
                        }
                        
                        /**
                         * Execute action of build contents of the cell.
                         */
                        strAction = getPageflow().resolveLink(objPFLayoutComponent.getComponent());
                        
                        try {
                            /**
                             * Perform the subaction :
                             */
                            goCell = getPageflow().go(strAction);
                            if (goCell.pos == zXType.rc.rcWarning.pos) {
                                getPageflow().getPage().errorMsg("Failed to find or execute action : " + strAction);
                            }
                            
                        } catch (Exception e) {
                        	getZx().trace.addError("Failed to execute action", strAction, e);
                        	
                        	/**
                        	 * Carry on even if we have an exception :
                        	 */
                            getPageflow().getPage().errorMsg("Failed to execute action " + strAction);
                        }
                        
                        if (blnHorizontal) {
            	            getPageflow().getPage().s.appendNL("			</td>");
                        } else if (blnTabbed) {
                            getPageflow().getPage().s.appendNL("</div>");
                        }
                        
                    }
                    
                }
                
            }
            
            if (blnHorizontal) {
	            getPageflow().getPage().s.appendNL("		</tr>");
	            getPageflow().getPage().s.appendNL("</table>");
            }
            
            getPageflow().getPage().s.appendNL("			</td>");
            getPageflow().getPage().s.appendNL("		</tr>");
            getPageflow().getPage().s.appendNL("</table>");
            
            // Nested Tabs - Only if we have done at least one action :
            if (blnTabbed && intTabs > 0) {
                /**
                 * Select the default tab :
                 */
    	        getPageflow().getPage().s.appendNL("<SCRIPT type=\"text/javascript\" language=\"JavaScript\">");
    	        getPageflow().getPage().s.append("zXSelectTab(1,\"").append(strTabName).append("\"")
    	        									 .append(strOrientation.indexOf('v')!=-1?",\"Vertical\"":"")
    	        									 .append(");")
    	        									 .appendNL();
    	        getPageflow().getPage().s.appendNL("</SCRIPT>");
            }
            
            /**
             * Build the end of the cell
             */
            goCell = goCellEnd(pintCell);
            getPageflow().getPage().s.append("<!-- End cell").append(pintCell).append(" -->").appendNL();
            
	        return goCell;
	    } catch (Exception e) {
	        getZx().trace.addError("Failed to : Build an individual cell.", e);
	        if (getZx().log.isErrorEnabled()) {
	            getZx().log.error("Parameter : pobjPFLayoutCell = "+ pobjPFLayoutCell);
	            getZx().log.error("Parameter : pintCell = "+ pintCell);
	        }
	        if (getZx().throwException) throw new ZXException(e);
	        goCell = zXType.rc.rcError;
	        return goCell;
	    } finally {
	        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
	            getZx().trace.returnValue(goCell);
	            getZx().trace.exitMethod();
	        }
	    }
	}
	
    /**
     * Build up the beginning of a cell.
     * 
     * <pre>
     * 
     * This method will also set the width/height and alignment of each cell.
     * </pre>
     * 
     * @param pobjPFLayoutCell The cell to draw.
     * @param pintCell The cell number to draw.  
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if goCellBegin fails. 
     */
    private zXType.rc goCellBegin(PFLayoutCell pobjPFLayoutCell, int pintCell) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjPFLayoutCell", pobjPFLayoutCell);
            getZx().trace.traceParam("pintCell", pintCell);
        }
        
        zXType.rc goCellBegin = zXType.rc.rcOK;
        
        try {
    	    /**
    	     * The tab layout has a completely different 
    	     * behaviour from the cell layout.
    	     * 
    	     * We wrap each "cell" in a div instead of a table cell.
    	     * 
    	     * For now we do not have the layout flexibility.
    	     */
            if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plTab) || this.enmPageflowLayout.equals(zXType.pageflowLayout.plTabv)) {
                getPageflow().getPage().s.append("<div id=\"zXPage" + getName() + pintCell + "\" style=\"display:none\">").appendNL();
                return goCellBegin;
            }
            
            // Defaults :
            String strWidth = "";
            String strHeight = "";
    	    String strAlign = "left";
            String strValign = "top";
            
            // Try to resolve the settings for this cell.
            if (StringUtil.len(pobjPFLayoutCell.getAlign()) > 0) {
                strAlign = getPageflow().resolveDirector(pobjPFLayoutCell.getAlign());
            }
            if (StringUtil.len(pobjPFLayoutCell.getHeight()) > 0) {
                strHeight = getPageflow().resolveDirector(pobjPFLayoutCell.getHeight());
            }
            if (StringUtil.len(pobjPFLayoutCell.getValign()) > 0) {
                strValign = getPageflow().resolveDirector(pobjPFLayoutCell.getValign());
            }
            if (StringUtil.len(pobjPFLayoutCell.getWidth()) > 0) {
                strWidth= getPageflow().resolveDirector(pobjPFLayoutCell.getWidth());
            }
            
    	    switch (pintCell) {
                case 1:
                    // Cell 1
                    // Always starts with a TD as the cellsStart already adds the first TR
                    getPageflow().getPage().s.append("<td ")
									.appendAttr("align", strAlign)
									.appendAttr("valign", strValign)
									.appendAttr("height", StringUtil.len(strHeight)==0?"":strHeight)
									.appendAttr("width", StringUtil.len(strWidth)==0?"":strWidth)
									// Only "four" layout for has a rowspan :
									.append(this.enmPageflowLayout.equals(zXType.pageflowLayout.plFour)?" rowspan=\"2\">":">")
									.appendNL();
                    break;
                    
                case 2:
                    // Cell 2
                    if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plThree)) {
                        getPageflow().getPage().s.appendNL("<tr>");
                    }
                    
                    getPageflow().getPage().s.append("<td ")
									.appendAttr("align", strAlign)
									.appendAttr("valign", strValign)
									.appendAttr("height", StringUtil.len(strHeight)==0?"":strHeight)
									.appendAttr("width", StringUtil.len(strWidth)==0?"":strWidth)
									// Only "five" layout for has a rowspan :
									.append(this.enmPageflowLayout.equals(zXType.pageflowLayout.plFive)?" rowspan=\"2\">":">")
									.appendNL();
                    break;
                    
                case 3:
                    // Cell 3
                    // All 3 cell layouts need a TR. 
                    // NOTE : layout 1-3 has LESS than 3 cells.
                    getPageflow().getPage().s.appendNL("<tr>");
                    
                    getPageflow().getPage().s.append("<td ")
									.appendAttr("align", strAlign)
									.appendAttr("valign", strValign)
									.appendAttr("height", StringUtil.len(strHeight)==0?"":strHeight)
									.appendAttr("width", StringUtil.len(strWidth)==0?"":strWidth)
									.appendNL('>');
                    break;
                    
                case 4:
                    // Cell 4
                    getPageflow().getPage().s.append("<td ")
									.appendAttr("align", strAlign)
									.appendAttr("valign", strValign)
									.appendAttr("height", StringUtil.len(strHeight)==0?"":strHeight)
									.appendAttr("width", StringUtil.len(strWidth)==0?"":strWidth)
									.appendNL('>');
                    break;

                default:
                    break;
            }
    	    
    	    /**
    	     * Nested tabs : These can be tab or tabv
    	     */
    	    if (StringUtil.len(pobjPFLayoutCell.getOrientation()) > 0) {
    	     
    	        String strOrientation = pobjPFLayoutCell.getOrientation();
    	        
    	        if (getPageflow().resolveDirector(strOrientation).charAt(0) == 't') {
    	            
    	            // Whether we have vertical tabs or now
    	            boolean blnVertical = strOrientation.indexOf('v')!=-1;
    	            
    		        /**
    		         * Generate the tabs across the top, one per page. The id must be 'zXPage' + actionName + 'N' where N
    		         * is the page number. This will be used by javascript in zx.js when a tab is clicked.
    		         */
    		        String strTmp;
    		        if (blnVertical) {
    		            strTmp = "height=\"90%\" width=\"20\" align=\"left\" ";
    		        } else {
    		            strTmp =" width=\"90%\" ";
    		        }
    		        
    		        getPageflow().getPage().s.append("<table ").append(strTmp).append(">\n		<tr>").appendNL();
    		        
    		        /**
    		         * Used to make the text vertical :
    		         */
    		        if (blnVertical) {
    		            strTmp = " class=\"zxVertical\" style=\"writing-mode: tb-rl; filter:flipH() flipV();\" ";
    		        } else {
    		            strTmp ="";
    		        }
    		        
    		        String strActionName;
    		        PFLayoutComponent objPFLayoutComponent;
    		        String strTitle = "";
    		        
    		        int intTabs = 1;
    		        int intSubActions = this.subactions.size();
    		        String strCellName = getPageflow().resolveDirector(pobjPFLayoutCell.getName());
    		        // Ensure we have a unique name :
    		        String strTabName = getName() + strCellName;
    		        
    		        for (int i = 0; i < intSubActions; i++) {
    		            objPFLayoutComponent = (PFLayoutComponent)this.subactions.get(i);
    		            
    		            // Resolve the name of the action to perform.
    	                strActionName = getPageflow().resolveDirector(objPFLayoutComponent.getName());
    	                
    		            if (strCellName.equalsIgnoreCase(strActionName)) {
    		                
    		                if (getPageflow().isActive(objPFLayoutComponent.getActive())) {
    		                    
            		            // Name format is zXPage<layoutname><cellnumber><tabnumber>
            		            getPageflow().getPage().s
            		            				.append("			<td NOWRAP id=\"zXPage")
            		            				.append(strTabName).append(intTabs)
            		            				.append("\" onMouseDown=\"javascript:zXSelectTab(").append(intTabs).append(",'")
            		            				.append(strTabName).append("'")
            		            				.append(blnVertical?", 'Vertical'":"").append(");\" ")
            		            				.append(strTmp)
            		            				.appendNL('>');
            		            
            		            /**
            		             * Get the title of the tab.
            		             */
            		            
            		            if (objPFLayoutComponent.getTitle() != null && !objPFLayoutComponent.getTitle().isEmpty()) {
        		                    strTitle = getPageflow().resolveLabel(objPFLayoutComponent.getTitle());
            		            } else {
            		            	/**
            		            	 * Generate a bogus name for now.
            		            	 */
            		                strTitle = "Tab" + intTabs;
            		            }
            		            
            		            getPageflow().getPage().s.appendNL(strTitle);
            		            
            		            getPageflow().getPage().s.appendNL("			</td>");
            		            
            		            /**
            		             * Force a new row for vertical tabs except for the very last tab :
            		             */
            			        if (blnVertical &&  i < intSubActions) {
            			            getPageflow().getPage().s.appendNL("		</tr>");
            			            getPageflow().getPage().s.appendNL("		<tr>");
            			        }
            			        
            		            intTabs++;
            		            
    		                }
    		                
    		            }
    		            
    		        }
    		        
    		        getPageflow().getPage().s.appendNL("		</tr>");
    		        
    		        /**
    		         * Add some space
    		         */
    		        getPageflow().getPage().s.append("		<tr>\n		<td colspan=\"")
    		        						 .append(intSubActions)
    		        						 .append("\"><img src=\"../images/spacer.gif\" height=9></td>\n		</tr>")
    		        						 .appendNL();
    		        
    		        getPageflow().getPage().s.appendNL("</table>");
    		        
    	        }
    	    }
    	    
            return goCellBegin;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build up the beginning of a cell.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjPFLayoutCell = "+ pobjPFLayoutCell);
                getZx().log.error("Parameter : pintCell = "+ pintCell);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            goCellBegin = zXType.rc.rcError;
            return goCellBegin;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(goCellBegin);
                getZx().trace.exitMethod();
            }
        }
	}	
	
	/**
	 * Build the end part of a cell.
	 * 
	 * @param pintCell The cell number to build.
	 * @return Returns the return code of the method.
	 */
	private zXType.rc goCellEnd(int pintCell) {
	    zXType.rc goCellEnd = zXType.rc.rcOK;
	    
	    /**
	     * Just close off the div for the tab layout.
	     */
        if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plTab) 
            || this.enmPageflowLayout.equals(zXType.pageflowLayout.plTabv)) {
            getPageflow().getPage().s.appendNL("</div>");
            return goCellEnd;
        }
	    
        /**
         * Close of the cell :
         */
	    switch (pintCell) {
            case 1:
                if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plThree))
                    getPageflow().getPage().s.appendNL("</tr>");
                break;
            case 2:
                if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plFour) 
                        || this.enmPageflowLayout.equals(zXType.pageflowLayout.plFive)
                        || this.enmPageflowLayout.equals(zXType.pageflowLayout.plSix))
                    getPageflow().getPage().s.appendNL("</tr>");
                	
                break;
            case 3:
                // No need
            case 4:
                // No need
                break;
            default:
                // We may add some flexibility here :
                break;
        }
	    
	    return goCellEnd;
	}
	
	//------------------------ PFAction overriden methods.
	
    /** 
     * @see org.zxframework.web.PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
    	if (getZx().trace.isApplicationTraceEnabled()) {
    		getZx().trace.enterMethod();
    	}
    	
        zXType.rc go = zXType.rc.rcOK;
        
        try {
            
            /**
             * Resolve entities as we may want to use them. 
             * NOTE : PFLayout is treated the same as PFNull.
             */
            ZXCollection colEntities = getPageflow().getEntityCollection(this, 
                                                                         zXType.pageflowActionType.patNull, 
                                                                         zXType.pageflowQueryType.pqtAll);
            if (colEntities != null) {
                getPageflow().setContextEntities(colEntities);
                
                String strPK;
                PFEntity objEntity;
                
                Iterator iter = colEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    getPageflow().setContextEntity(objEntity);
                    strPK = getPageflow().resolveDirector(objEntity.getPk());
                    if (StringUtil.len(strPK) > 0) {
                        objEntity.getBo().setPKValue(strPK);
                        objEntity.getBo().loadBO((StringUtil.len(objEntity.getSelecteditgroup())>0)?objEntity.getSelecteditgroup():objEntity.getSelectlistgroup());
                    } else {
                        if (StringUtil.len(objEntity.getPkwheregroup()) > 0) {
                            objEntity.getBo().loadBO(objEntity.getSelectlistgroup(), objEntity.getPkwheregroup(), false);
                        }
                    }
                }
            }
            
            /**
             * Title of this action : 
             */
            String strTag = tagValue("zxForm");
            
            if (StringUtil.len(strTag) > 0) {
                if (strTag.equalsIgnoreCase("menu")) {
                    getPageflow().getPage().formTitle(zXType.webFormType.wftMenu, getPageflow().resolveLabel(getTitle()));
                } else if (strTag.equalsIgnoreCase("edit")) {
                    getPageflow().getPage().formTitle(zXType.webFormType.wftEdit, getPageflow().resolveLabel(getTitle()));
                } else if (strTag.equalsIgnoreCase("list")) {
                    getPageflow().getPage().formTitle(zXType.webFormType.wftList, getPageflow().resolveLabel(getTitle()));
                } else if (strTag.equalsIgnoreCase("search")) {
                    getPageflow().getPage().formTitle(zXType.webFormType.wftSearch, getPageflow().resolveLabel(getTitle()));
                } else {
                    getPageflow().getPage().formTitle(zXType.webFormType.wftEdit, getPageflow().resolveLabel(getTitle()));
                }
                
            } else {
                getPageflow().getPage().formTitle(zXType.webFormType.wftEdit, getPageflow().resolveLabel(getTitle()));
            }
            
            /**
             * Handle any outstanding messages
             */
            getPageflow().processMessages(this);
            
            /**
             * Form header (if not handled by calling asp/jsp)
             */
            if (!getPageflow().isOwnForm() && getPageflow().resolveDirector(tagValue("zXFormStart")).equals("1")) {
                getPageflow().getPage().s.append("<form ")
                						 .appendAttr("method", "post")
                						 .appendAttr("action", getPageflow().constructURL(this.getFormaction()))
                						 .appendNL('>');
            }
            
            /**
             * Decide which method we will use to draw the screen.
             */
            if (StringUtil.len(this.layout) > 0) {
                this.enmPageflowLayout = zXType.pageflowLayout.getEnum(getPageflow().resolveDirector(this.layout));
            } else {
                this.enmPageflowLayout = zXType.pageflowLayout.plOne;
            }
            
            if (this.enmPageflowLayout.equals(zXType.pageflowLayout.plTemplate)) {
                
                if (StringUtil.len(this.template) > 0) {
	                // If a template file is specified then use it to draw the page.
	                go = goTemplateLayout();
            	}
                
            } else if (getCells() != null && getCells().size() > 0 && this.subactions != null && !this.subactions.isEmpty()){
                // If not template has beed set then we will use the cell layout method.
                go = goCellLayout();
                
            }
            
            /**
             * Handle buttons. With the tag zXForm you can control the placement of the button
             */
            if (StringUtil.len(strTag) == 0) {
                getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftNull);
            } else {
                if (strTag.equalsIgnoreCase("menu")) {
                    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftMenu);
                } else if (strTag.equalsIgnoreCase("edit")) {
                    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftEdit);
                } else if (strTag.equalsIgnoreCase("list")) {
                    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftList);
                } else if (strTag.equalsIgnoreCase("search")) {
                    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftSearch);
                } else {
                    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftNull);
                }
            }
            
            getPageflow().processFormButtons(this);
            
            if (StringUtil.len(strTag) == 0) {
                getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftNull);
            } else {
                if (strTag.equalsIgnoreCase("menu")) {
                    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftMenu);
                } else if (strTag.equalsIgnoreCase("edit")) {
                    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftEdit);
                } else if (strTag.equalsIgnoreCase("list")) {
                    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftList);
                } else if (strTag.equalsIgnoreCase("search")) {
                    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftSearch);
                } else {
                    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftNull);
                }
            }
            
            /** 
             * Close the form. 
             **/
            if (!getPageflow().isOwnForm() && getPageflow().resolveDirector(tagValue("zXFormEnd")).equals("1")) {
                getPageflow().getPage().s.appendNL("</form>");
            }
            
            /**
             * Handle window title and footer
             */
            getPageflow().handleFooterAndTitle(this, null);
            
            /**
             * Determine next action
             */
            getPageflow().setAction(getPageflow().resolveLink(getLink()));
            
	        return go;
	    } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process PFLayout action.", e);
	        
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
        
        objXMLGen.taggedValue("template", getTemplate());
        objXMLGen.taggedValue("layout", getLayout());
        
        if (this.subactions != null && !this.subactions.isEmpty()) {
            objXMLGen.openTag("subactions");
            PFLayoutComponent objLayoutComponent;
            int intSubActions = this.subactions.size();
            for (int i = 0; i < intSubActions; i++) {
            	objLayoutComponent = (PFLayoutComponent)this.subactions.get(i);
                objXMLGen.openTag("subaction");
                objXMLGen.taggedCData("name", objLayoutComponent.getName());
                objXMLGen.taggedCData("active", objLayoutComponent.getActive(), true);
                getDescriptor().xmlComponent("component", objLayoutComponent.getComponent());
                getDescriptor().xmlLabel("title", objLayoutComponent.getTitle());
                objXMLGen.closeTag("subaction");
            }
            objXMLGen.closeTag("subactions");
        }
        
        if (getCells() != null && !getCells().isEmpty()) {
            objXMLGen.openTag("cells");
            PFLayoutCell objPFLayoutCell;
            Iterator iter = getCells().values().iterator();
            while(iter.hasNext()) {
                objPFLayoutCell = (PFLayoutCell)iter.next();
                objXMLGen.openTag("cell");
                objXMLGen.taggedCData("align", objPFLayoutCell.getAlign());
                objXMLGen.taggedCData("bgcolor", objPFLayoutCell.getBgcolor());
                objXMLGen.taggedCData("border", objPFLayoutCell.getBorder());
                objXMLGen.taggedCData("bordercolor", objPFLayoutCell.getBordercolor());
                objXMLGen.taggedCData("clazz", objPFLayoutCell.getClazz());
                objXMLGen.taggedCData("height", objPFLayoutCell.getHeight());
                objXMLGen.taggedCData("name", objPFLayoutCell.getName());
                objXMLGen.taggedCData("orientation", objPFLayoutCell.getOrientation());
                objXMLGen.taggedCData("style", objPFLayoutCell.getStyle());
                objXMLGen.taggedCData("valign", objPFLayoutCell.getValign());
                objXMLGen.taggedCData("width", objPFLayoutCell.getWidth());
                getDescriptor().xmlLabel("title", objPFLayoutCell.getTitle());
                objXMLGen.closeTag("cell");
            }
            objXMLGen.closeTag("cells");
        }
    }
    
    /**
     * @see PFAction#clone(Pageflow)
     */
    public PFAction clone(Pageflow pobjPageflow) {
        PFLayout cleanClone = (PFLayout)super.clone(pobjPageflow);
        
        cleanClone.setLayout(getLayout());
        
        /**
         * Clone the list of cells defined for the actions. 
         */
        if (getCells() != null && getCells().size() > 0) {
            cleanClone.setCells((ZXCollection)getCells().clone());
        }
        
        cleanClone.setTemplate(getTemplate());
        
        /**
         * Clone the list of sub actions.
         */
        if (getSubactions() != null && getSubactions().size() > 0) {
	        cleanClone.setSubactions(CloneUtil.clone((ArrayList)getSubactions()));
        }
        
        return cleanClone;
    }
}