/*
 * Created on Jun 27, 2004 by michael
 * $Id: ExprFHRecTree.java,v 1.1.2.8 2005/08/17 09:10:05 mike Exp $
 */
package org.zxframework.misc;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.expression.ExprFH;
import org.zxframework.property.BooleanProperty;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringUtil;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * The expression handler that implements the expression recursive tree library.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ExprFHRecTree extends ExprFH {
    
    //------------------------ Members
	
	private static Log log = LogFactory.getLog(ExprFHRecTree.class);

    private RecTreeNode root;
    protected RecTreeNode node;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public ExprFHRecTree() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    /**
     * @return Returns the root.
     */
    public RecTreeNode getRoot() {
        return root;
    }
    
    /**
     * @return Returns the node.
     */
    public RecTreeNode getNode() {
        return node;
    }
    
    /**
     * @param node The node to set.
     */
    public void setNode(RecTreeNode node) {
        this.node = node;
    }
    
    /**
     * @param root The root to set.
     */
    public void setRoot(RecTreeNode root) {
        this.root = root;
    }
    
    //------------------------ Inner Classes
    //------------------------ All functions with 0 parameters :
    
    /**
     * labelhierarchy - Return the hierarchy of the current node.
     * 
     * <pre>
     * 
     * e.g. america/north america/us
     * </pre>
     */
    public static class labelhierarchy extends RecTreeExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Return the hierarchy of the current node", "S", "0", "-"};
        /** Default constructor. */
        public labelhierarchy() { super(); }

        /** 
         * @see RecTreeExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 0);
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
            	
                if (this.node == null) {
                    exec = new StringProperty("", true);
                } else {
                    exec = new StringProperty(this.node.labelHierarchy(), true);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * childnodes - Return number of childnodes for current node.
     */
    public static class childnodes extends RecTreeExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Return number of childnodes for current node", "L", "0", "-"};
        /** Default constructor */
        public childnodes() { super(); }

        /** 
         * @see RecTreeExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 0);
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
            	if (this.node == null) {
                    exec = new LongProperty(-1, false);
                } else {
                    exec = new  LongProperty(this.node.getChildNodes().size(), false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    //------------------------ All functions with 1 parameters : 
    
    /**
     * distance(pk) ==> a number; 0 indicates no direct relationship.
     * 
     * <pre>
     * 
     * Gives the distance from the given node (pk) to the current node
     * in the tree
     * 
     *  E.g.
     * Tree / PK	1       2   3   4   5   6   7   8   9
     * 1             	1       -1  -2  -2  -1  -2  -1  -2  -2
     * 2           	0       1   -1  -1  0   0   0   0   0
     * 3       		0       2   1   0   0   0   0   0   0
     * 4       		0       2   0   1   0   0   0   0   0
     * 5           	0       0   0   0   1   -1  0   0   0
     * 6       		0       0   0   0   2   1   0   0   0
     * 7           	0       0   0   0   0   0   1   -1  -1
     * 8       		0       0   0   0   0   0   2   1   0
     * 9       		0       0   0   0   0   0   2   0   1
     * </pre>
     */
    public static class distance extends RecTreeExpressionFunction {
    	/** Discribes the expression */
        public static final String[] describe = {"Gives the distance from the given node (pk) to the current node in the tree", "L", "1", "lng"};
        /** Default constructor. */
        public distance() { super(); }
        
        /** 
         * @see RecTreeExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property)pcolArgs.get(0);
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
            	
                if (this.node == null) {
                    exec = new LongProperty(0, false);
                    
                } else {
                    /**
                     * Get the node that we want to check
                     */
                    RecTreeNode objNode = this.root.findNode(objArg.longValue());
                    if (this.node == null) {
                        exec = new LongProperty(0, false); 
                    } else {
                        exec = new LongProperty(this.node.distance(objNode), false);
                    }
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC], false);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS], false);
            }
            
            return exec;
            
        }
    }
    
    /**
     *inPath(pk) ==> true | false
     *See if the current node is in the path of the node with <pk>.
     *
     *<pre>
     *
     *e.g.
     *current node /12/13/8/2
     *node 13 inPath = true
     *node 27 inPath = false
     *</pre>
     *
     */
    public static class inpath extends RecTreeExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "See if the current node is in the path of the node with <pk>", "B", "1", "lng"};
        /** Default constructor. */
        public inpath() { super(); }

        /** 
         * @see RecTreeExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
            	
                /**
                 * Get the node that we want to check
                 */
                if (this.node == null) {
                    exec = new BooleanProperty(false);
                } else {
                	/**
                	 * Get the node that we want to check
                	 */
                    RecTreeNode objNode = this.root.findNode(objArg.longValue());
                    
                    /** The hrrchy we are looking in. **/
                    String strHrrchy = objNode.getBo().getValue("hrrchy").getStringValue();
                    /** The node we are looking for. **/
                    String strNode = '/' + this.node.getBo().getPKValue().getStringValue() + "/";
                    
                    if (strHrrchy.indexOf(strNode) != -1) {
                        exec = new BooleanProperty(true);
                    } else {
                        exec = new BooleanProperty(false);
                    }
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    //------------------------ Overloaded methods
    
    /**
     * Execute function. This over loads the default default behaviour of
     * the expression handler.
     *
     * @param pstrFunction The function to execute 
     * @param pcolArgs List of arguements passed to the function.
     * @param penmPurpose The purpose of the execution 
     * @return Returns the result of the executed function
     * @throws ZXException  Thrown if go fails. 
     */
     public Property go(String pstrFunction, ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException{
         if(getZx().trace.isFrameworkTraceEnabled()) {
             getZx().trace.enterMethod();
             getZx().trace.traceParam("pstrFunction", pstrFunction);
             getZx().trace.traceParam("pcolArgs", pcolArgs);
             getZx().trace.traceParam("penmPurpose", penmPurpose);
         }
         
         Property go = null; 
         
         try {
             
             Class cls = Class.forName(this.getClass().getName() +  "$" + StringUtil.makeClassName(pstrFunction));
             
             if (cls != null) {
             	Constructor c =  cls.getConstructor(new  Class []{});
				RecTreeExpressionFunction objFH = (RecTreeExpressionFunction)c.newInstance(new Object[]{});
				// Node may be null, depends from where the expression is being used 
				objFH.setNode(this.node);
				objFH.setRoot(this.root);
				try {
				    go = objFH.exec(pcolArgs, penmPurpose);
				} catch (Exception e) {
				    log.error("Failed to : execute director : " + pstrFunction, e);
				}
             }
             return go;
         } catch (Exception e) {
             getZx().trace.addError("Failed to : Execute function", e);
             if (log.isErrorEnabled()) {
                 log.error("Parameter : pstrFunction = "+ pstrFunction);
                 log.error("Parameter : pcolArgs = "+ pcolArgs);
                 log.error("Parameter : penmPurpose = "+ penmPurpose);
             }
             
             if (getZx().throwException) throw new ZXException(e);
             return go;
         } finally {
             if(getZx().trace.isFrameworkTraceEnabled()) {
                 getZx().trace.returnValue(go);
                 getZx().trace.exitMethod();
             }
         }
     }
}