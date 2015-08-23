/*
 * Created on Mar 15, 2004 by Michael Brewer
 * $Id: DrctrFHPageflow.java,v 1.1.2.7 2006/07/17 13:50:49 mike Exp $
 */
package org.zxframework.web;

import java.net.URLEncoder;

import org.zxframework.CntxtEntry;
import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.director.Director;
import org.zxframework.director.DirectorFunction;
import org.zxframework.director.DrctrFH;
import org.zxframework.util.StringUtil;

/**
 * The pageflow director function handler.
 * 
 * <pre>
 * 
 * Change    : BD15JAN04
 * Why       : Added pfaction
 * 
 * Change    : BD6MAY04
 * Why       : #context now gets from main context
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1 
 */
public class DrctrFHPageflow extends DrctrFH {

    //------------------------ Members
    
    /** A handle to the pageflow object.**/
    protected Pageflow pageflow;
    
    //------------------------ Constructor
    
    /**
     * Default constructor.
     */
    public DrctrFHPageflow() {
        super();
    }
    
    /**
     * @param pobjPageflow A handle to the pageflow.
     */
    public void init(Pageflow pobjPageflow) {
        this.pageflow = pobjPageflow;
    }
    
    //------------------------ Public Methods
    
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
    
    //------------------------ Directors
    
    /**
     * #qsn - Get query string.
     * 
     * <pre>
     * 
     * Only handle this case for the famous #qs.*-... syntax
     * otherwise the zx default FH handler will take care of the
     * qs function
     * </pre>
     */
    public class qsn implements Director {
        /** Discribes me*/
        public static final String describe = "Get query string";
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = "";
            
            if (pobjFunc.getRemainder().charAt(0) == '*') {
            	/**
                 * Tricky situation: an '*' at position one means that we have to copy all
                 * querystring entries accross that are listed. E.g. *-pk-e-s
                 * means copy -pk, -e and -s to the output query string
                 */
                String strArg = pobjFunc.getRemainder().substring(1).toLowerCase();
                
                CntxtEntry objEntry;
                String [] arrArg = StringUtil.split("-", strArg);
                for (int i = 0; i < arrArg.length; i++) {
                	strArg = '-' + arrArg[i];
                	objEntry = (CntxtEntry)getZx().getQuickContext().getEntries().get(strArg);
                    
                    if (objEntry != null) {
                    	try {
                    		strArg = objEntry.getName() + '=' + URLEncoder.encode(objEntry.getStrValue(), "UTF-8");
                    		exec = pageflow.appendToUrl(exec, strArg);
                    	} catch (Exception e) {
                        	getZx().log.error("Failed to url encode " + objEntry.getStrValue(), e);
                        }
                    }
                    
                }
            }
            
            return exec;
        }
    }
    
    /**
     * #qsn - Get query string.
     * 
     * <pre>
     * 
     * Only handle this case for the famous #qs.*-... syntax
     * otherwise the zx default FH handler will take care of the
     * qs function
     * </pre>
     */
    public class qs extends qsn {
    	/**
    	 * This class does not override anything that qsn does, it
    	 * just adds backwards compatible support for qs directors
    	 */
    }
    
    /**
     * #pfentitylabel - Well retrieve a Business object label from its Pageflow entity name.
     */
    public class pfentitylabel implements Director {
        /** Discribes me*/
        public static final String describe = "Take <argument> from request";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = "";
            
            /** Get a handle to the current pageflow action : */
            PFAction objPFAction = pageflow.getContextAction();
            if (objPFAction == null) {
                return exec;
            }
            
            /** Make sure that we have some entries. **/
            ZXCollection colEntities = pageflow.getEntityCollection(objPFAction, zXType.pageflowActionType.patSearchForm, zXType.pageflowQueryType.pqtSearchForm);
            if (colEntities == null || colEntities.isEmpty()) {
                return exec;
            }
            
            PFEntity objPFEntity;
            String pstrArgument = pobjFunc.getRemainder();
            if (StringUtil.len(pstrArgument) == 0) {
                // Get the first entry.
                objPFEntity = (PFEntity)colEntities.iterator().next();
            } else {
                // Get the entity by the name :
                objPFEntity = (PFEntity)colEntities.get(pstrArgument);
            }
            
            if (objPFEntity != null) {
                exec = objPFEntity.getBo().getDescriptor().getLabel().getLabel();
            }
            
            return exec;
        }
    }
    
    /**
     * #request - Take <code>argument</code> from request.
     */
    public class request implements Director {
        /** Discribes me*/
        public static final String describe = "Take <argument> from request";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;
            
            if (pageflow.getRequest() != null) {
                exec = pageflow.getRequest().getParameter(pobjFunc.getRemainder());
            }
            
            // Make we do not return null.
            if (exec == null) {
                exec ="";
            }
            
            return exec;
        }
    }

    /**
     * #context - Take <code>argument</code> from session context.
     */
    public class context implements Director {
        /** Discribes me*/
        public static final String describe = "Take <argument> from session context";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;
            
            exec = getZx().getSession().getFromContext(pobjFunc.getRemainder());
            if (exec == null) {
                exec = "";
            }
            
            return exec;
        }
    }
    
    /**
     * #empty - Empty (used by processAttributeValues).
     */
    public class empty implements Director {
        /** Discribes me*/
        public static final String describe = "Empty (used by processAttributeValues)";
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;
            exec = "#empty";
            return exec;
        }
    }
    
    /**
     * #fieldvalue - Generates the javascript to get the field value of a specific attribute.
     * <p/>
     * 
     * Usage :
     * <pre>
     * #fieldvalue.bo.attr
     * #fieldvalue.attr
     * </pre>
     * <ul>
     * <li>bo - Is the name of the business object in the bo context.
     * If not supplied the most recently added business object to the 
     * bocontext will be used.</li>
     * <li>attr - The name of the attribute.</li>
     * </ul> 
     */
    public class fieldvalue implements Director {
        /** Discribes me*/
        public static final String describe = "The value of a field on the form";
        
        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;
            
            /**
             * At most 2 arguments
             */
            int intNumArgs = pobjFunc.numArgs(); 
            if (intNumArgs > 2 || intNumArgs < 1) {
                throw new ZXException("One or two parameters required for #fieldvalue");
            }
            
            /**
             * Try to get the BO from the BO Context.
             */
            ZXBO objBO;
            if (intNumArgs == 1) {
                objBO = getZx().getBOContext().getBO(null);
            } else {
                objBO = getZx().getBOContext().getBO(pobjFunc.getArg(0));
            }
            if (objBO == null) {
                throw new ZXException("Cannot find BO", (intNumArgs == 2? pobjFunc.getArg(0):""));
            }
            
            /**
             * Get the attribute name
             */
            String strAttr;
            if (intNumArgs == 2) {
                strAttr = pobjFunc.getArg(1);
            } else {
                strAttr = pobjFunc.getArg(0);
            }
            
            exec = "findObj('" 
            	   + pageflow.getPage().controlName(objBO, objBO.getDescriptor().getAttribute(strAttr))  
            	   + "',window).value";
            
            return exec;
        }
    }
    
    /**
     * #control - The name of a control in the form.
     * <p/>
     * 
     * Usage :
     * <pre>
     * #control.bo.attr
     * #control.attr
     * </pre>
     * <ul>
     * <li>bo - Is the name of the business object in the bo context.
     * If not supplied the most recently added business object to the 
     * bocontext will be used.</li>
     * <li>attr - The name of the attribute.</li>
     * </ul>
     */
    public class control implements Director {
        /** Discribes me*/
        public static final String describe = "The name of a control on the form";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;
            
            /**
             * At most 2 arguments
             */
            int intNumArgs = pobjFunc.numArgs(); 
            if (intNumArgs > 2 || intNumArgs < 1) {
                throw new ZXException("One or two parameters required for #control");
            }
            
            /**
             * Try to get the BO from the BO Context.
             */
            ZXBO objBO;
            if (intNumArgs == 1) {
                objBO = getZx().getBOContext().getBO(null);
            } else {
                objBO = getZx().getBOContext().getBO(pobjFunc.getArg(0));
            }
            if (objBO == null) {
                throw new ZXException("Cannot find BO", (intNumArgs == 2? pobjFunc.getArg(0):""));
            }
            
            /**
             * Get the attribute name.
             */
            String strAttr;
            if (intNumArgs == 2) {
            	strAttr = pobjFunc.getArg(1);
            } else {
                strAttr = pobjFunc.getArg(0);
            }
            
            exec = pageflow.getPage().controlName(objBO, objBO.getDescriptor().getAttribute(strAttr));
            
            return exec;
        }
    }
    
    /**
     * #ctr - The name of a control on the form.
     * @see control
     */
    public class ctr extends control {
    	/**
    	 * This is an alis for control and does not override any of it's bahaviour
    	 */
    }
    
    /**
     * #controldivlock - The name of the div associated with a locked control.
     * <p/>
     * 
     * Usage :
     * <pre>
     * #controldivlock.bo.attr
     * #controldivlock.attr
     * </pre>
     * <ul>
     * <li>bo - Is the name of the business object in the bo context.
     * If not supplied the most recently added business object to the 
     * bocontext will be used.</li>
     * <li>attr - The name of the attribute.</li>
     * </ul>
     */
    public class controldivlock implements Director {
        /** Discribes me*/
        public static final String describe = "The name of the div associated with a locked control";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = null;
            
            /**
             * At most 2 arguments
             */
            int intNumArgs = pobjFunc.numArgs(); 
            if (intNumArgs > 2 || intNumArgs < 1) {
                throw new ZXException("One or two parameters required for #controldivlock or #ctrdivlock");
            }
            
            /**
             * Try to get the BO from the BO Context.
             */
            ZXBO objBO;
            if (intNumArgs == 1) {
                objBO = getZx().getBOContext().getBO(null);
            } else {
                objBO = getZx().getBOContext().getBO(pobjFunc.getArg(0));
            }
            if (objBO == null) {
                throw new ZXException("Cannot find BO", (intNumArgs == 2? pobjFunc.getArg(0):""));
            }
            
            /**
             * Get the attribute name.
             */
            String strAttr;
            if (intNumArgs == 2) {
            	strAttr = pobjFunc.getArg(1);
            } else {
                strAttr = pobjFunc.getArg(0);
            }
            
            exec = "div" + pageflow.getPage().controlName(objBO, objBO.getDescriptor().getAttribute(strAttr));
            
            return exec;
        }
    }
    
    /**
     * #ctrdivlock - The name of the div associated with a locked control.
     * @see controldivlock
     */
    public class ctrdivlock extends controldivlock {
    	/**
    	 * Alias of controldivlock and does not override any of its behaviour
    	 */
    }
    
    /**
     * #controlbyname - Generates the javscript to get the handle to a control on the form referred to by name.
     * <p/>
     * 
     * Usage :
     * <pre>
     * #controlbyname.bo.attr
     * #controlbyname.attr
     * </pre>
     * <ul>
     * <li>bo - Is the name of the business object in the bo context.
     * If not supplied the most recently added business object to the 
     * bocontext will be used.</li>
     * <li>attr - The name of the attribute.</li>
     * </ul>
     */
    public class controlbyname implements Director {
        /** Discribes me*/
        public static final String describe = "The handle to a control on the form referred to by name";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec;
            
            /**
             * At most 2 arguments
             */
            int intNumArgs = pobjFunc.numArgs(); 
            if (intNumArgs > 2 || intNumArgs < 1) {
                throw new ZXException("One or two parameters required for #controlbyname or #ctrbyname");
            }
            
            /**
             * Try to get the BO from the BO Context.
             */
            ZXBO objBO;
            if (intNumArgs == 1) {
                objBO = getZx().getBOContext().getBO(null);
            } else {
                objBO = getZx().getBOContext().getBO(pobjFunc.getArg(0));
            }
            if (objBO == null) {
                throw new ZXException("Cannot find BO", (intNumArgs == 2?pobjFunc.getArg(0):""));
            }
            
            /**
             * Get the attribute name
             */
            String strAttr;
            if (intNumArgs == 2) {
            	strAttr = pobjFunc.getArg(1);
            } else {
            	strAttr = pobjFunc.getArg(0);
            }
            
            exec = "elementByName(window,'" 
            	   + pageflow.getPage().controlName(objBO, objBO.getDescriptor().getAttribute(strAttr)) 
            	   + "')";
            
            return exec;
        }
    }
    
    /**
     * #ctrbyname - The handle to a control on the form referred to by name.
     * @see controlbyname
     */
    public class ctrbyname extends controlbyname {
    	// Implemented by controlbyname
    }    
    
    /**
     * #pf - Get the pageflow name.
     */
    public class pf implements Director {
        /** Discribes me*/
        public static final String describe = "Use pageflow name";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = pageflow.getPFDesc().getName();
            return exec;
        }
    }
    
    /**
     * #pfaction - Get the pageflow current action name.
     */
    public class pfaction implements Director {
        /** Discribes me*/
        public static final String describe = "Use pageflow current action name";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec =  null;
            
            if (pageflow.getContextAction() != null) {
            	exec = pageflow.getContextAction().getName();
            }
            
            return exec;
        }
    }
    
    /**
     * #loopoverok - Result of most recent loopOver action.
     */
    public class loopoverok implements Director {
        /** Discribes me*/
        public static final String describe = "Result of most recent loopOver action";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = String.valueOf(pageflow.getContextLoopOverOk());
            return exec;
        }
    }
    
    /**
     * #loopovererror - Result of most recent loopOver action.
     */
    public class loopovererror implements Director {
        /** Discribes me*/
        public static final String describe = "Result of most recent loopOver action";

        /** @see Director#exec(DirectorFunction) */
        public String exec(DirectorFunction pobjFunc) throws ZXException {
            String exec = String.valueOf(pageflow.getContextLoopOverError());
            return exec;
        }
    }
}