/*
 * Created on Mar 28, 2004
 * $Id: ExprFHBO.java,v 1.1.2.15 2006/07/17 16:40:43 mike Exp $
 */
package org.zxframework.expression;

import java.util.ArrayList;

import org.zxframework.Audit;
import org.zxframework.BOCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.property.BooleanProperty;
import org.zxframework.property.DoubleProperty;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;

/**
 * The expression function handler implementing the BO specific functions.
 * 
 * <pre>
 * 
 *  Change    : BD12MAY03
 *  Why       : Make use of zX BO context rather than local BO context
 * 
 *  Change    : BD26AUG03
 *  Why       : Added entityLabel as function
 * 
 *  Change    : DGS28AUG2003
 *  Why       : Added zXRqd function
 * 
 *  Change    : BD4SEP03
 *  Why       : Added more functions
 * 
 *  Change    : BD16OCT03
 *  Why       : Added sum function
 * 
 *  Change    : BD17OCT03
 *  Why       : Added count function
 * 
 *  Change    : BD22JAN04
 *  Why       : Added alias property
 * 
 * Change    : BD01MAY04
 * Why       : Fixed problem with missing static:=false parameter
 * 
 * Change    : BD16MAY04
 * Why       : Added quickLoad and quickFKLoad functions
 * 
 * Change    : BD23MAY04
 * Why       : Added sumGroup function
 * 
 * Change    : BD28MAY04
 * Why       : Changed main case construct for performance
 * 
 * Change    : BD4JUL04
 * Why       : Corrected describe for quickload function
 * 
 * Change    : BD14JUL04
 * Why       : Added tag function
 * 
 * Change    : BD22JUL04
 * Why       : Added db2string function
 * 
 * Change    : BD31JUL04
 * Why       : Fixed verbose of db2string function
 * 
 * Change    : BD6AUG04
 * Why       : Added writeAudit function
 * 
 * Change    : BD20AUG04
 * Why       : Added may* functions
 * 
 * Change    : DGS21OCT2004
 * Why       : Couple of errors in 'supports'
 * 
 * Change    : BD18DEC04 - V1.4:11
 * Why       : Added support for bo.inContext
 * 
 * Change    : BD14FEB05 - 40
 * Why       : - Fixed minor bugs and improved error handling
 * 
 * Change    : BD1JUL05 - V1.5:20
 * Why       : Added support for bo.fkLabel
 * 
 * Change    : BD1JUL05 - V1.5:28
 * Why       : Added support for bo.ensureLoaded
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ExprFHBO extends ExprFH {

    //---------------------------------------------------- Members

    //---------------------------------------------------- Constructors
    
    /**
     * Default constructor.
     */
    public ExprFHBO() {
        super();
    }

    //----------------------------------------------------- Inner classes :
    
    // ---------------------------------------------------- All functions with no parameters : 
    
    /**
     * incontext -  Check whether 'bo' exists in the bo context
     * 
     * <pre>
     * 
     * Usage : 
     * incontext('bo')
     * </pre>
     * 
     */
    public static class incontext extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Check whether 'bo' exists in the bo context", "B", "1", "str"};
        /** Default constructor. */
        public incontext() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property)pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = getZx().getBOContext().getBO(objArg.getStringValue());
                exec = new BooleanProperty( !(objBO == null) );
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("does " + objArg.getStringValue() + " exists in BO context");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * entitylabel -  Return label of BO (if exists in BO context).
     * 
     * <pre>
     * 
     * entityLabel('bo')Return label of BO (if exists in BO context) or
     *  entity (if not) in user language
     *  'bo' can be name of BO in context or name of entity
     * 
     * Usage : 
     * entitylabel(name)
     * </pre>
     * 
     */
    public static class entitylabel extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Return label of BO (if exists in BO context)", "S", "1", "str"};
        /** Default constructor. */
        public entitylabel() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property)pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = getZx().getBOContext().getCreateBO(objArg.getStringValue());
                if (objBO == null) {
                    throw new ZXException("Failed to create or get the ZXBO from the context :" + objArg.getStringValue());
                }
                exec = new StringProperty(objBO.getDescriptor().getLabel().getLabel(), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("get label for " + objArg.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * insertbo - Check whether current user is allowed to do something with a BO.
     * 
     * <pre>
     * The 'bo' is either a BO in the context (first try) or the name of an entity
     * 
     * Usage :
     *		mayXXXX('bo') -> boolean
     * </pre> 
     */
    public static class mayinsert extends ExpressionFunction {
        
        protected static final int MAY_INSERT = 0;
        protected static final int MAY_DELETE = 1;        
        protected static final int MAY_UPDATE = 2;
        protected static final int MAY_SELECT = 3;
        protected int type;
        /** Discribes the expression */
        public static final String[] describe = {"Check whether current user is allowed to do something with a BO", "S", "1", "str"};

        /** 
         * Default constructor. 
         */
        public mayinsert() { 
            super(); 
            this.type = MAY_INSERT;
        }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO =  getZx().getBOContext().getEntry(objArg.getStringValue());
                if (objBO == null) {
                    objBO = getZx().createBO(objArg.getStringValue());
                    if (objBO == null) {
                        throw new ZXException("Unable to retrieve / create BO " + objArg.getStringValue());
                    }
                }
                
                switch (type) {
                    case MAY_INSERT:
                        exec = new BooleanProperty(objBO.mayInsert());
                        break;
                    case MAY_UPDATE:
                        exec = new BooleanProperty(objBO.mayUpdate());
                        break;
                    case MAY_SELECT:
                        exec = new BooleanProperty(objBO.maySelect());
                        break;
                    default:
                        exec = new BooleanProperty(objBO.mayDelete());
                        break;
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                switch (type) {
                    case MAY_INSERT:
                        exec = new StringProperty("May user insert " + objArg.getStringValue());
                        break;
                    case MAY_UPDATE:
                        exec = new StringProperty("May user update " + objArg.getStringValue());
                        break;
                    case MAY_SELECT:
                        exec = new StringProperty("May user select " + objArg.getStringValue());
                        break;
                    default:
                        exec = new StringProperty("May user delete " + objArg.getStringValue());
                        break;
                }
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * @see ExprFHBO.mayinsert
     */
    public static class maydelete extends mayinsert {
        /** 
         * Default constructor. 
         */
        public maydelete() { 
            super(); 
            this.type = MAY_DELETE;
        }       
    }
    
    /**
     * @see ExprFHBO.mayinsert
     */
    public static class mayselect extends mayinsert {
        /** 
         * Default constructor. 
         */
        public mayselect() { 
            super(); 
            this.type = MAY_SELECT;
        }       
    }
    
    /**
     * @see ExprFHBO.mayinsert
     */
    public static class mayupdate extends mayinsert {
        /** 
         * Default constructor. 
         */
        public mayupdate() { 
            super(); 
            this.type = MAY_UPDATE;
        }       
    }
    
    /**
     * insertbo - Insert BO into database.
     * 
     * <pre>
     * 
     * Usage :
     * insertbo(strBO)
     * </pre> 
     */
    public static class insertbo extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the application date", "S", "1", "str"};
        /** Default constructor. */
        public insertbo() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            
            Property objArg = (Property) pcolArgs.iterator().next();
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg.getStringValue() + "' from BOContext", e);
                }
                exec = new LongProperty(objBO.insertBO().pos, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Insert BO " + objArg.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
  
    /**
     * insert - Insert BO into database.
     * 
     * <pre>
     * 
     * Usage :
     * insert(strBO)
     * </pre> 
     */
    public static class insert extends insertbo {
        /** Default constructor. */
        public insert() { super(); }       
    }
    
    /**
     * zxrqd - Return true or false if BO instance is required for system data .
     * 
     * <pre>
     * 
     * i.e.
     *  bo has the zXRqd column AND its value is True, returns True else False
     * 
     * Usage :
     * 	zxRqd(strBO)
     * </pre> 
     */
    public static class zxrqd extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Return true or false if BO instance is required for system data", "P", "1", "str"};
		/** Default constructor. */
		public zxrqd() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);            
            Property objArg = (Property)pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = getZx().getBOContext().getCreateBO(objArg.getStringValue());
                if (objBO == null) {
                    throw new ZXException("Failed to create or get the ZXBO from BO Context : " + objArg.getStringValue());
                }
                
                if (objBO.getDescriptor().getAttribute("zXRqd") != null) {
                    exec = objBO.getValue("zXRqd");
                } else {
                    exec = new BooleanProperty(false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Is instance of " + objArg.getStringValue() + " required");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    //----------------------------------------------------------- The functions with two parameters
    
    /**
     * attr - Returns the value of an attribute of a BO in the expression BO context.
     * 
     * <pre>
     * DGS09JUN2003: Can be formatted value e.g. option list label
     * 
     * Usage :
     * bo.attr(objentity, strattr)
     * </pre>
     */
    public static class attr extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Returns the value of an attribute of a BO in the expression BO context", "P", "2", "str,str"};
		/** Default constructor. */
		public attr() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                    
                    /**
                     * Could be that this bo is not in context yet.
                     */
                    if (objBO == null) {
                    	exec = new StringProperty("", true);
                    	return exec;
                    }
                    
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                
                if (this instanceof attrformatted) {
                    exec = new StringProperty(objBO.getValue(objArg2.getStringValue()).formattedValue(), false);
                } else {
                    exec = objBO.getValue(objArg2.getStringValue());
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("get attribute " + objArg2.getStringValue() 
                                          + " from context BO " + objArg1.getStringValue() 
                                          + (this instanceof attrformatted ? "(formatted)" : ""));
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * getattr - Returns the value of an attribute of a BO in the expression BO context.
     * 
     * <pre>
     * 
     * Usage :
     * bo.getattr(objentity, strattr)
     * </pre>
     */
    public static class getattr extends attr {
        /** Default constructor. */
        public getattr() { super(); }
    }
    
    /**
     * attrformatted - Returns the value of an attribute of a BO in the expression BO context.
     * 
     * <pre>
     * NOTE : This will return a formatted value
     * 
     * Usage :
     * bo.attrformatted(objentity, strattr)
     * </pre>
     */
    public static class attrformatted extends attr {
        /** Default constructor. */
        public attrformatted() { super(); }
    }    
    
    /**
     * deletebo - Delete BO from database.
     * 
     * <pre>
     * 
     * Usage :
     * bo.deletebo(bo,wheregroup)
     * </pre> 
     */
    public static class deletebo extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Delete BO from database", "S", "2", "str,str"};
		/** Default constructor. */
		public deletebo() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                exec = new LongProperty(objBO.deleteBO().pos, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Delete BO "+ objArg1.getStringValue() + " by pk " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * delete - Delete BO from database.
     * 
     * <pre>
     * 
     * Usage :
     * bo.delete(bo,wheregroup)
     * </pre> 
     */
    public static class delete extends deletebo {
        /** Default constructor. */
        public delete() { super(); }
    }  
    
    /**
     * sumgroup - Sum group of attributes.
     * 
     * <pre>
     * 
     * Usage :
     * sumGroup("bo","attribute group")
     * </pre>
     * 
     */
    public static class sumgroup extends ExpressionFunction {
        /** Discribes the expression*/
        public static final String[] describe = { "Sum group of attributes", "D", "2", "str,str"};
        /** Default constructor. */
        public sumgroup() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                if (objBO == null) {
                    throw new ZXException("Failed to get entry " + objArg1.getStringValue() + " from the BO Context");
                }
                exec = new DoubleProperty(objBO.sumGroup(objArg2.getStringValue()), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Sum attributes " + objArg1.getStringValue() + "." + objArg2.getStringValue());
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }

    /**
     * tag - Return bo tag of BO.
     * 
     * <pre>
     * 
     * Usage :
     * tag("bo","tag name")
     * </pre>
     */
    public static class tag extends ExpressionFunction {
        /** Discribes the expression*/
        public static final String[] describe = { "Sum group of attributes", "S", "2", "str,str"};
        /** Default contructor. */
        public tag() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                // Get BO from BO context :
                ZXBO objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                if (objBO == null) {
                    throw new ZXException("Unable to retrieve BO " + objArg1.getStringValue() + " from the BO Context");
                }
                exec = new StringProperty(objBO.getDescriptor().tagValue(objArg2.getStringValue()), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Get tag " + objArg1.getStringValue() + "." + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
	}
    
    /**
     * count - Count BO in database .
     * 
     * <pre>
     * 
     * Usage:
     * 
     * count(bo,wheregroup)
     * </pre>
     */
    public static class count extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Count BO in database", "L", "2", "str,str"};
		/** Default constructor. */
		public count() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                exec = new LongProperty(objBO.countByGroup(objArg2.getStringValue()), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Count instances of " + objArg1.getStringValue() + " by pk " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * createbo - Create a business object and store in context .
     * 
     * <pre>
     * 
     * Updated : 
     * bo.createBO(bo,context)
     * </pre>
     */
    public static class createbo extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Create a business object and store in context", "B", "2", "str,str"};
		/** Default constructor. */
		public createbo() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = getZx().createBO(objArg1.getStringValue());
                if (objBO == null) {
                    throw new ZXException("Cannot create '" + objArg1.getStringValue() + "'");
                }
                getZx().getBOContext().setEntry(objArg2.getStringValue(), objBO);
                
                exec = new  BooleanProperty(true);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("create object " + objArg1.getStringValue() + " from context BO " + objArg1.getStringValue() + " and store in context as " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * create - Create a business object and store in context. 
     * 
     * <pre>
     * 
     * Updated : 
     * bo.create(bo,context)
     * </pre>
     */
    public static class create extends createbo {
        /** Default constructor. */
        public create() { super(); }
    }
    
    /**
     * bo2xml - Serialise BO to xml .
     * 
     *<pre>
     *
     * Usage :
     *	bo.bo2xml(bo, attributegroup)
     *</pre>
     */
    public static class bo2xml extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Serialise BO to xml", "S", "2", "str,str"};
		/** Default constructor. */
		public bo2xml() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                exec = new StringProperty(objBO.bo2XML(objArg2.getStringValue()), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("create object " + objArg1.getStringValue() 
                                          + " from context BO " + objArg2.getStringValue() + " and store in context as " + objArg1);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * getfkattr - Get the attribute that is the foreign key from one BO to another.
     * 
     * <pre>
     * 
     * Usage :
     * getfkattr(from, to)
     * 
     * Both from and to can be either identifiers or entity names
     * </pre>
     */
    public static class getfkattr extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Get the attribute that is the foreign key from one BO to another.", "S", "2", "str,str"};
		/** Default constructor. */
		public getfkattr() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                /**
                 * First look in BO context
                 */
                ZXBO objBO = getZx().createBO(objArg1.getStringValue());
                if (objBO == null) {
                    throw new ZXException("Cannot create or get instance of '" + objArg1.getStringValue() + "'");
                }
                
                /**
                 * And the to BO
                 */
                ZXBO objBO2 = getZx().createBO(objArg2.getStringValue());
                if (objBO2 == null) {
                    throw new ZXException("Cannot create or get instance of '" + objArg2.getStringValue() + "'");
                }                    
                exec = new StringProperty(objBO.getFKAttr(objBO2).getName(), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Get foreign key attribute from " + objArg1.getStringValue() 
                                            + " to " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }

    /**
     * writeaudit - Write BO audit.
     * 
     * <pre>
     * 
     * bo - The business object you want to write a audit for.
     * type - The type of audit record to write. This is defined in the auditType table.
     * 
     * Usage :
     * writeaudit(bo, type)
     * </pre>
     */
    public static class writeaudit extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Write BO audit.", "S", "2", "str,str"};
		/** Default constructor. */
		public writeaudit() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                /**
                 * First look in BO context
                 */
                ZXBO objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                if (objBO == null) {
                    throw new ZXException("Cannot BO retrieve '" + objArg1.getStringValue() + "'");
                }
                
                Audit objAudt = (Audit)getZx().createBO("zXAudt");
                if (objAudt == null) {
                    throw new ZXException("Failed to create the audit business object");
                }
                exec = new LongProperty(objAudt.writeAudit(objBO, objArg2.getStringValue()).pos, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("write audit " + objArg1.getStringValue() + "." + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    //------------------------------------------------------------------------------The functions with three parameters
    
    /**
     * setattr - Set value of an attribute of a BO in the context .
     * 
     * <pre>
     * 
     * Usage: 
     * setAttr('bo', 'attr', 'value')
     * </pre>
     */
    public static class setattr extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Set value of an attribute of a BO in the context", "S", "3", "str,str,str|int|dbl|dat|bln"};
		/** Default constructor. */
		public setattr() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 3);
            
            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            Property objArg3 = (Property)pcolArgs.get(2);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                
                exec = new LongProperty(objBO.setValue(objArg2.getStringValue(), objArg3.getStringValue()).pos, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Set " + objArg1.getStringValue() +  "." 
                                          + objArg2.getStringValue() + " to " + objArg3.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }

    /**
     * fklabel - Get the fklabe .
     * 
     * <pre>
     * 
     * V1.5:20
     * 
     * bo.fkLabel('bo', 'attr', 'value') -> string
     * 
     * Return the FK label for bo.attr providing that value is the
     * PK of the FK BO that this attribute is pointing to.
     * 
     * Note that bo is either a bo from the bo context or an entity name
     * </pre>
     */
    public static class fklabel extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Get FKLabel", "S", "3", "str,str,str"};
		/** Default constructor. */
		public fklabel() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 3);
            
            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            Property objArg3 = (Property)pcolArgs.get(2);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                if (objBO == null) {
                	objBO = getZx().createBO(objArg1.getStringValue());
                }
                
                if (objBO == null) {
                	throw new ZXException("Cannot create / retrieve instance of '" + objArg1.getStringValue() + "'");
                }
                
                if (objBO.setValue(objArg2.getStringValue(), objArg3.getStringValue()).pos == zXType.rc.rcError.pos) {
                	throw new ZXException("Cannot set PK for '" 
                						  + objArg1.getStringValue() + "' to '" + objArg3.getStringValue() + "'");
                }
                
                if (objBO.getValue(objArg2.getStringValue()).resolveFKLabel(false).pos == zXType.rc.rcError.pos) {
                	throw new ZXException("Cannot resolve FK label for '" + objArg1.getStringValue() 
                						  + "'.'" + objArg2.getStringValue() + "'");
                }
                
                exec = new StringProperty(objBO.getValue(objArg2.getStringValue()).getFkLabel());
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Return FK label for " + objArg1.getStringValue() 
                						  + "." + objArg2.getStringValue() + " where PK is " + objArg3.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            } // purpose
            
            return exec;
        }
    }
    
    /**
     * sum - Do a sum(attr) on the given attribute / attribute group.
     * 
     * <pre>
     * 
     * Usage : 
     * sum('bo', 'attr', 'where group')
     * </pre> 
     */
    public static class sum extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Do a sum(attr) on the given attribute / attribute group", "S", "3", "str,str,str"};
		/** Default constructor. */
		public sum() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 3);

            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            Property objArg3 = (Property)pcolArgs.get(2);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                exec = new LongProperty(objBO.selectSum(objArg2.getStringValue(), objArg3.getStringValue()).pos, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Sum " + objArg1.getStringValue() + "." 
                                          + objArg2.getStringValue() + " using where group " 
                                          + objArg3.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * max - Do a max(attr) on the given attribute / attribute group. 
     *
     *<pre>
     *
     *Usage:
     *max('bo', 'attr', 'where group')
     *</pre>
     */
    public static class max extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Do a max(attr) on the given attribute / attribute group", "S", "3", "str,str,str"};
		/** Default constructor. */
		public max() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 3);
            
            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            Property objArg3 = (Property)pcolArgs.get(2);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                exec = new LongProperty(objBO.selectMax(objArg2.getStringValue(), objArg3.getStringValue()).pos, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Max " + objArg1.getStringValue() + "." 
                                            + objArg2.getStringValue() + " using where group " 
                                            + objArg3.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * min - Do a min(attr) on the given attribute / attribute group.
     * 
     * <pre>
     * 
     * Usage :
     * min('bo', 'attr', 'where group')
     * </pre> 
     */
    public static class min extends ExpressionFunction {
        /** Discribes the expression */
    	public static final String[] describe = {"Do a min(attr) on the given attribute / attribute group", "S", "3", "str,str,str"};
    	/** Default constructor. */
    	public min() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 3);

            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            Property objArg3 = (Property)pcolArgs.get(2);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                exec = new LongProperty(objBO.selectMin(objArg2.getStringValue(), objArg3.getStringValue()).pos, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Min " + objArg1.getStringValue() + "." + objArg2.getStringValue() 
                                          + " using where group " + objArg3.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * avg - Do a avg(attr) on the given attribute / attribute group.
     * 
     * <pre>
     * 
     * Usage :
     *  avg('bo', 'attr', 'where group')
     * </pre>
     */
    public static class avg extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Do a avg(attr) on the given attribute / attribute group", "S", "3", "str,str,str"};
		/** Default constructor. */
		public avg() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 3);
            
            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            Property objArg3 = (Property)pcolArgs.get(2);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                exec = new LongProperty(objBO.selectAvg(objArg2.getStringValue(), objArg3.getStringValue()).pos, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Avg" + objArg1.getStringValue() + "." + objArg2.getStringValue() 
                                            + " using where group " + objArg3.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * clonebo - Clone a business object and store in context.
     * 
     * <pre>
     * 
     * Usage :
     * clonebo('bo', 'save as', 'group')
     * </pre>
     *  
     */
    public static class clonebo extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Clone a business object and store in context", "S", "3", "str,str,str"};
		/** Default constructor. */
        public clonebo() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 3);

            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            Property objArg3 = (Property)pcolArgs.get(2);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                
                objBO = objBO.cloneBO(objArg3.getStringValue());
                if (objBO == null) {
                    throw new ZXException("Unable to clone '" + objArg1.getStringValue() + "' using attribute group " + objArg3.getStringValue());
                }
                
                /**
                 * This will return a warning now as we
                 * are trying to replace an existing value
                 * in the bo context. But this is exactly what
                 * we expect in this circumstances.
                 */
                int intRC = getZx().getBOContext().setEntry(objArg2.getStringValue(), objBO).pos;
                if (intRC == zXType.rc.rcWarning.pos) {
                	intRC = zXType.rc.rcOK.pos;
                }
                
                exec = new LongProperty(intRC, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Clone " + objArg1.getStringValue() + ", copy values " + objArg3.getStringValue() 
                                            + " and store as " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * clone - Clone a business object and store in context.
     * 
     * <pre>
     * 
     * Usage :
     * clone('bo', 'save as', 'group')
     * </pre>
     *  
     */
    public static class clone extends clonebo {
        /** Default constructor. */
        public clone() { super(); }        
    }
    
    /**
     * loadbo - Load a business object from database .
     * 
     * <pre>
     * 
     * 
     * Usage :
     * loadbo('bo', 'group', 'where')
     * </pre>
     */
    public static class loadbo extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Load a business object from database", "S", "3", "str,str,str"};
        /** Default constructor. */
        public loadbo() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 3);
            
            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            Property objArg3 = (Property)pcolArgs.get(2);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                
                /**
                 * Try to load the business object.
                 */
                int intRC;
                try {
                	intRC = objBO.loadBO(objArg2.getStringValue(), objArg3.getStringValue(), false).pos;
                } catch (Exception e) {
                	intRC = zXType.rc.rcError.pos;
                }
                
                exec = new LongProperty(intRC, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Load attributes " + objArg2.getStringValue() + " of BO " 
                                        + objArg1.getStringValue() + " by group " + objArg3.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * load - Load a business object from database .
     * 
     * <pre>
     * 
     * Usage :
     * load('bo', 'group', 'where')
     * </pre>
     */
    public static class load extends loadbo {
        /** Default constructor. */
        public load() { super(); }       
    }
    
    /**
     * updatebo - Update a business object.
     * 
     * <pre>
     * 
     * Usage :
     * 	update('bo', 'group', 'where')
     * </pre> 
     */
    public static class updatebo extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Update a business object", "S", "3", "str,str,str"};
		/** Default constructor. */
		public updatebo() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;

            checkArgs(pcolArgs, 3);
            
            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            Property objArg3 = (Property)pcolArgs.get(2);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = null;
                try {
                    objBO = getZx().getBOContext().getEntry(objArg1.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Cannot retrieve Business Object : '" + objArg1.getStringValue() + "' from BOContext", e);
                }
                exec = new LongProperty(objBO.updateBO(objArg2.getStringValue(), objArg3.getStringValue()).pos, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Update attributes " + objArg2.getStringValue() + " of BO " + objArg1.getStringValue() 
                                            + " by group " + objArg3.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * update - Update a business object.
     * 
     * <pre>
     * 
     * Usage :
     * 	update('bo', 'group', 'where')
     * </pre> 
     */
    public static class update extends updatebo {
        /** Default constructor. */
        public update() { super(); }        
    }
    
    //------------------------------------------------------------------- Variable number of parameters
    
    /**
     * quickload - Create instance of BO and load it and then adds it to the bo context.
     * 
     * <pre>
     * 
     * Usage :
     * 		quickLoad(entity, bo key to store, value, [key attr = +], [load = *])
     * </pre>
     */
    public static class quickload extends ExpressionFunction {
        /** Discribes the expression*/
        public static final String[] describe = { "Create instance of BO and load it and then adds it to the bo context.", "L", "3-5", "str,str,str|dbl|int|dat|bln,..."};
        /** Default constructor. */
        public quickload() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;

            // Check args size
            int size = pcolArgs.size();
            if (size < 3 || size > 5) {
                throw new ZXException("2, 3 or 4 parameters expected for quickLoad; found " + size);
            }
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO;
                
                switch (size) {
                    case 3:
                        objBO = getZx().getBos().quickLoad( ((Property)pcolArgs.get(0)).getStringValue(), (Property)pcolArgs.get(2));
                        break;

                    case 4:
                        objBO = getZx().getBos().quickLoad( ((Property)pcolArgs.get(0)).getStringValue(), 
                                											 (Property)pcolArgs.get(2), 
                                											 ((Property)pcolArgs.get(3)).getStringValue());
                        break;
                        
                    default:
                        // 5
                        objBO = getZx().getBos().quickLoad(((Property)pcolArgs.get(0)).getStringValue(),
                                											(Property)pcolArgs.get(2), 
                                											((Property)pcolArgs.get(3)).getStringValue(), 
                                											((Property)pcolArgs.get(4)).getStringValue());
                        break;
                }
                
                if (objBO != null) {
                    getZx().getBOContext().setEntry(((Property)pcolArgs.get(1)).getStringValue(), objBO);
                    exec = new LongProperty(zXType.rc.rcOK.pos, false);
                } else {
                    exec = new LongProperty(zXType.rc.rcError.pos, false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                
                switch (size) {
                    case 3:
                        exec = new StringProperty(
                                "Create instance of " + ((Property)pcolArgs.get(0)).getStringValue() 
                                + " and load * where pk = '" + ((Property)pcolArgs.get(2)).getStringValue() + "' and save as " 
                                + ((Property)pcolArgs.get(1)).getStringValue());
                        break;

                    case 4:
                        exec = new StringProperty(
                                "Create instance of " + ((Property)pcolArgs.get(0)).getStringValue() 
                                + " and load * where " +  ((Property)pcolArgs.get(3)).getStringValue() + " = '" 
                                + ((Property)pcolArgs.get(2)).getStringValue() + "' and save as " 
                                + ((Property)pcolArgs.get(1)).getStringValue());
                        break;
                        
                    default:
                        // 5
                        exec = new StringProperty(
                                "Create instance of " + ((Property)pcolArgs.get(0)).getStringValue() 
                                + " and load " + ((Property)pcolArgs.get(4)).getStringValue() 
                                + " where " +  ((Property)pcolArgs.get(3)).getStringValue() + " = '" 
                                + ((Property)pcolArgs.get(2)).getStringValue() + "' and save as " 
                                + ((Property)pcolArgs.get(2)).getStringValue());
                        break;
                }
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }

    /**
     * ensureloaded - Ensures that all attributes in bo have been loaded.
     * 
     * <pre>
     * 
     * Usage :
     * 	 ensureLoaded(<bo>,<group>, <wheregroup>)
     * 	 ensureLoaded(<bo>,<group>)
     * </pre>
     */
    public static class ensureloaded extends ExpressionFunction {
        /** Discribes the expression*/
        public static final String[] describe = { "Ensures that all attributes in bo have been loaded", "L", "2-3", "str,str,..."};
        /** Default constructor. */
        public ensureloaded() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;

            // Check args size
            int size = pcolArgs.size();
            if (size < 2 || size > 3) {
                throw new ZXException("2 or 3 parameters expected for ensureloaded; found " + size);
            }
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO;
                
                Property objProp = (Property)pcolArgs.get(0);
                objBO = getZx().getBOContext().getEntry(objProp.getStringValue());
                
                if (objBO == null) {
                	throw new ZXException("Unable to retrieve BO " + objProp.getStringValue());
                }
                
            	objProp = (Property)pcolArgs.get(1);
            	
                if (size == 2) {
                	/**
                	 * No wheregroup so assume +
                	 */
                	exec = new LongProperty(objBO.ensureGroupIsLoaded(objProp.getStringValue()).pos, false);
                	
                } else {
                	exec = new LongProperty(objBO.ensureGroupIsLoaded(objProp.getStringValue(), 
                													  ((Property)pcolArgs.get(1)).getStringValue(),
                													  false).pos, false);
                	
                } // Two or 3 arguments (= where clause)
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
            	if (size == 2) {
            		exec = new StringProperty("Ensure that " + ((Property)pcolArgs.get(1)).getStringValue() 
            						   		  + " of " + ((Property)pcolArgs.get(0)).getStringValue() + " is loaded");
            	} else {
            		exec = new StringProperty("Ensure that " + ((Property)pcolArgs.get(1)).getStringValue() 
 						   					  + " of " + ((Property)pcolArgs.get(0)).getStringValue() 
 						   					  + " is where " + ((Property)pcolArgs.get(2)).getStringValue());
            	}
            	
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }

    /**
     * quickfkload - Create instance of BO and load it and then adds it to the bo context.
     * 
     * <pre>
     * 
     * Usage :
     * 		quickfkload(bo key, attr, bo key to store, [load = *])
     * </pre>
     */
    public static class quickfkload extends ExpressionFunction {
        /** Discribes the expression*/
        public static final String[] describe = { "Create instance of BO and load it and then adds it to the bo context.", "L", "3-4", "str,str,..."};
        /** Default constructor. */
        public quickfkload() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;

            // Check args size
            int size = pcolArgs.size();
            if (size < 3 || size > 4) {
                throw new ZXException("3 or 4 parameters expected for quickFKLoad; found " + size);
            }
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ZXBO objBO = getZx().getBOContext().getBO(((Property)pcolArgs.get(0)).getStringValue());
                
                switch (size) {
                    case 3:
                        objBO = objBO.quickFKLoad(((Property)pcolArgs.get(1)).getStringValue());
                        break;
                        
                    default:
                        // 4
                        objBO = objBO.quickFKLoad(((Property)pcolArgs.get(1)).getStringValue(), ((Property)pcolArgs.get(3)).getStringValue() );
                        break;
                }
                
                if (objBO != null) {
                    getZx().getBOContext().setEntry(((Property)pcolArgs.get(2)).getStringValue(), objBO);
                    exec = new LongProperty(zXType.rc.rcOK.pos, false);
                } else {
                    exec = new LongProperty(zXType.rc.rcError.pos, false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                
                switch (size) {
                    case 3:
                        exec = new StringProperty(
                                "Create BO related to of " + ((Property)pcolArgs.get(0)).getStringValue() 
                                + "." + ((Property)pcolArgs.get(1)).getStringValue()
                                +  " and load * and store as " + ((Property)pcolArgs.get(2)).getStringValue()
                                , false);
                        break;

                    default:
                        // 4
                        exec = new StringProperty(
                                "Create BO related to of " + ((Property)pcolArgs.get(0)).getStringValue() 
                                + "." + ((Property)pcolArgs.get(1)).getStringValue()
                                +  " and load " +  ((Property)pcolArgs.get(3)).getStringValue() + " and store as " 
                                + ((Property)pcolArgs.get(2)).getStringValue());
                        break;
                }
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * bolabel - Get label value for BO from context or create a new one.
     * 
     * <pre>
     * 
     * Usage : 
     * 	bolabel - boLabel('bo') - Get label value for BO from context
     * 	boLabel('entity', 'pk') - Get label for newly created BO and pk
     * </pre> 
     */
    public static class bolabel extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Get label value for BO from context", "S", "1-2", "str,..."};
		/** Default constructor. */
		public bolabel() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
                Property exec;
                
                int size = pcolArgs.size();
                if (size != 1 && size != 2) {
                    throw new ZXException("One or two parameters expected for function 'bolabel' found " + size);
                }
                
                Property objArg1 = (Property)pcolArgs.get(0);
                if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                    ZXBO objBO; 
                    
                    if (size == 1) {
                        // We only have one argument
                        objBO = getZx().getBOContext().getBO(objArg1.getStringValue());
                        if (objBO == null) {
                            throw new ZXException("Cannot retrieve '" + objArg1.getStringValue() + "'");
                        }                        
                    } else {
                        Property objArg2 = (Property)pcolArgs.get(1);
                        objBO = getZx().createBO(objArg1.getStringValue());
                        if (objBO == null) {
                            throw new ZXException("Cannot create instance of '" + objArg1.getStringValue() + "'");
                        }
                        
                        objBO.setPKValue(objArg2.getStringValue());
                        
                        if (objBO.loadBO("Label", "+", true).equals(zXType.rc.rcOK)) {
                            throw new ZXException("Cannot load instance of '" 
                                                    + objArg1.getStringValue() + "' with key " + objArg2.getStringValue());
                        }
                    }
                    exec = new StringProperty(objBO.formattedString("Label"), false);
                    
                } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                    if (size == 1) {
                        exec = new StringProperty("Get label for context business object " + objArg1.getStringValue());
                    } else {
                        exec = new StringProperty("Get label for business object " + objArg1.getStringValue() 
                                                   + " and primary key '" + ((Property)pcolArgs.get(1)).getStringValue() + "'");
                    }
                    
                } else {
                    exec = new StringProperty(describe[EXPR_ARGS]);
                }
                
                return exec;                
        }
    }
    
    /**
     * db2string - Combination of db2collection and col2string.
     * 
     * <pre>
     * 
     * Load instances of entity where whereGroup order by orderByGroup
     * Generate string of group seperated by separator
     * 
     * Usage :
     * 		db2string(entity, where group, order by group, group, [separator]
     * </pre>
     */
    public static class db2string extends ExpressionFunction {
        /** Discribes the expression*/
        public static final String[] describe = { "Combination of db2collection and col2string", "S", "4-5", "str,str,str|dbl|int|dat|bln,..."};
        /** Default constructor. */
        public db2string() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int size = pcolArgs.size();
            if (size < 4 || size > 5) {
                throw new ZXException("4 or 5 parameters expected for db2string; found " + size);
            }
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            Property objArg3 = (Property) pcolArgs.get(2);
            Property objArg4 = (Property) pcolArgs.get(3);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
                ZXBO objBO = getZx().getBOContext().getBO(objArg1.getStringValue());
                if (objBO == null) {
                    throw new ZXException("BO not found in bo context", objArg1.getStringValue());
                }
                
                /**
                 * Deal with reverse order by group (if group starts with -)
                 */
                BOCollection colTmp = objBO.db2Collection("", 
			                            objArg4.getStringValue(), 
			                            true, 
			                            objArg2.getStringValue(),
			                            objArg3.getStringValue().substring(1), 
			                            objArg3.getStringValue().charAt(0) == '-' );
                
                String strTmp;
                if (size == 4) {
                    strTmp = colTmp.col2String(objArg4.getStringValue(), null, null);
                } else {
                    strTmp = colTmp.col2String(objArg4.getStringValue(), null, ((Property)pcolArgs.get(4)).getStringValue());
                }
                
                exec = new StringProperty(strTmp, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {                
				if (size == 4) {
				    exec= new StringProperty("Create string by concatenating " +
                                            objArg1.getStringValue() +
                                            "." +
                                            objArg4.getStringValue() +
                                            ", where  " +
                                            objArg2.getStringValue() +
                                            ", order by " +
                                            objArg3.getStringValue());
				} else {                
				    exec= new StringProperty("Create string by concatenating " +
			                                objArg1.getStringValue() +
			                                "." +
			                                objArg4.getStringValue() +
			                                " seperate with " +
			                                ((Property)pcolArgs.get(4)).getStringValue() +
			                                ", where  " +
			                                objArg2.getStringValue() +
			                                ", order by " +
			                                objArg3.getStringValue());
				}
				
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
}