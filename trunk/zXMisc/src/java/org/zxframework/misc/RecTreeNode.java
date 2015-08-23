/*
 * Created on Jun 23, 2004 by Michael
 * $Id: RecTreeNode.java,v 1.1.2.9 2005/08/17 09:10:05 mike Exp $
 */
package org.zxframework.misc;

import java.util.Iterator;

import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * Single node of a recursive tree.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class RecTreeNode extends ZXObject {

    //------------------------ Members
	
	private static Log log = LogFactory.getLog(RecTreeNode.class);
	
    /** The root rectree node * */
    private RecTreeNode root;
    private RecTreeNode parent;
    private ZXCollection childNodes;
    private ZXBO bo;

    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public RecTreeNode() {
        super();
        
        // Init the childNodes collection.
        this.childNodes = new ZXCollection();
    }

    //------------------------ Getters/Setters
    
    /**
     * A collection of childern rectree nodes.
     * 
     * @return Returns the childNodes.
     */
    public ZXCollection getChildNodes() {
        return childNodes;
    }

	/**
	 * @return Returns the bo.
	 */
	public ZXBO getBo() {
		return bo;
	}
	
	/**
	 * @param bo The bo to set.
	 */
	public void setBo(ZXBO bo) {
		this.bo = bo;
	}
	
    /**
     * @param childNodes The childNodes to set.
     */
    public void setChildNodes(ZXCollection childNodes) {
        this.childNodes = childNodes;
    }

    /**
     * The parent of this rectree node.
     * 
     * @return Returns the parent
     */
    public RecTreeNode getParent() {
        return parent;
    }

    /**
     * @param parent The parent to set.
     */
    public void setParent(RecTreeNode parent) {
        this.parent = parent;
    }

    /**
     * Root of the tree.
     * 
     * @return Returns the root.
     */
    public RecTreeNode getRoot() {
        return root;
    }

    /**
     * @param root The root to set.
     */
    public void setRoot(RecTreeNode root) {
        this.root = root;
    }

    //------------------------ Public methods.

    /**
     * Return the distance from the me to the given node.
     * 
     * <pre>
     * 
     *  NOTE : Need to check implementation.
     *  
     *   -1 = parent
     *   -2 = grand parent
     *   1 = me
     *   2 = child
     *   3 = grandchild
     *   0 = no direct relationship 
     * </pre>
     * 
     * @param pobjNode
     *                 The node you want to calculate the distance to.
     * @return Returns the distance from the me to the given node.
     * @throws ZXException Thrown if distance fails.
     */
    public int distance(RecTreeNode pobjNode) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjNode", pobjNode);
        }
        
        int distance = 0;
        
        try {
        	
            /**
             * The key is in the hierarchies of 'me' and 'node'
             */
            String strMyPath = getBo().getValue("hrrchy").getStringValue();
            String strNodePath = pobjNode.getBo().getValue("hrrchy").getStringValue();

            if (strMyPath.equals(strNodePath)) {
				/**
				 * If they are both the same: distance = 1 (i.e. 'I' am 'node')
				 */
				distance = 1;

			} else if (strNodePath.startsWith(strMyPath)) {
				/**
				 * 'I' start with 'node' (e.g. /1/3/5/7/ in /1/3/5/) so 'node'
				 * must be an ancestor of mine
				 */
				String[] arrStrNodePath = StringUtil.split("/", strNodePath);
				String[] arrStrMyPath = StringUtil.split("/", strMyPath);

				distance = arrStrMyPath.length - arrStrNodePath.length;

			} else if (strMyPath.startsWith(strNodePath)) {
				/**
				 * 'Node' starts with 'me' (e.g. /1/3/5/ in /1/3/5/7/) so 'node'
				 * must be a child of mine
				 */
				String[] arrStrNodePath = StringUtil.split("/", strNodePath);
				String[] arrStrMyPath = StringUtil.split("/", strMyPath);

				distance = arrStrMyPath.length - arrStrNodePath.length + 1;

			} else {
				/**
				 * No direct relationship between 'me' and 'node'
				 */
				distance = 0;
				
            }

            return distance;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Return the distance from the me to the given node.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pobjNode = " + pobjNode);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return distance;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(distance);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * For debug purposes only.
     * 
     * @return Returns a string dump of this object and its childern.
     * @throws ZXException Thrown if dump fails.
     */
    public String dump() throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        String dump = "";
        
        try {
            /**
             * First me
             */
            dump = getBo().getValue("lvl").longValue() + "." + getBo().formattedString("Label");
            
            /**
             * And the kids
             */
            RecTreeNode objNode;
            
            Iterator iter = getChildNodes().iterator();
            while (iter.hasNext()) {
                objNode = (RecTreeNode) iter.next();
                
                dump = dump + "\n" + objNode.dump();
            }
            
            return dump;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : For debug purposes only.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return dump;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(dump);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Find a node by primary key.
     * 
     * @param plngKey The primary key
     * @return Returns the node by primary key.
     * @throws ZXException Thrown if findNode fails.
     */
    public RecTreeNode findNode(long plngKey) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("plngKey", plngKey);
        }

        RecTreeNode findNode = null;
        
        try {
            /**
             * First check me
             */
            if (getBo().getPKValue().longValue() == plngKey) {
                findNode = this;
                
            } else {
                /**
                 * Next try my children
                 */
                Iterator iter = getChildNodes().iterator();
                RecTreeNode objNode;
                RecTreeNode objParent;

                while (iter.hasNext()) {
                    objNode = (RecTreeNode) iter.next();
                    
                    objParent = objNode.findNode(plngKey);
                    if (objParent != null) {
                        findNode = objParent;
                        return findNode;
                    }
                }
            }

            return findNode;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Find a node by primary key", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : plngKey = " + plngKey);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return findNode;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(findNode);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Find the parent of the given BO.
     * 
     * @param pobjBO The business object linked to the rec tree
     * @return Returns the parent node from the current business.
     * @throws ZXException Thrown if findParentNode fails.
     */
    public RecTreeNode findParentNode(ZXBO pobjBO) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
        }

        RecTreeNode findParentNode = null;
        
        try {
            /**
             * First check me
             */
        	if (getBo().getPKValue().compareTo(pobjBO.getValue("prnt")) == 0) {
                findParentNode = this;
                
            } else {
                /**
                 * Next try my children.
                 */
                RecTreeNode objNode;
                RecTreeNode objParent;
                Iterator iter = getChildNodes().iterator();
                while (iter.hasNext()) {
                    objNode = (RecTreeNode)iter.next();
                    objParent = objNode.findParentNode(pobjBO);
                    if (objParent != null) {
                        findParentNode = objParent;
                        return findParentNode;
                    }
                }
                
            }
        	
            return findParentNode;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Find the parent of the given BO.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pobjBO = " + pobjBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return findParentNode;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(findParentNode);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Return the hierarchy of this tree node in the form of concatenated
     * labels.
     * 
     * @return Returns the hierarchy of the tree node.
     * @throws ZXException Thrown if hierarchyString fails.
     */
    public String hierarchyString() throws ZXException {
        String hierarchyString;
        
    	if (this.parent == null) {
            hierarchyString = '/' + getBo().getPKValue().getStringValue() + '/';
        } else {
            hierarchyString = this.parent.hierarchyString() + '/' + getBo().getPKValue().getStringValue() + '/';
        }
    	
        return hierarchyString;
    }
    
    /**
     * Return the hierarchy of this tree node in the form of concatenated
     * labels.
     * 
     * <pre>
     * 
     *  eg : america\north america\ us
     * </pre>
     * 
     * @return Returns the hierarchy of the tree node.
     * @throws ZXException Thrown if labelHierarchy fails.
     */
    public String labelHierarchy() throws ZXException {
    	return labelHierarchy('/');
    }

    /**
     * Return the hierarchy of this tree node in the form of concatenated
     * labels.
     * 
     * <pre>
     * 
     *  eg : america\north america\ us
     * </pre>
     * 
     * @param pstrSep Seperator character (defaults to /)
     * @return Returns the hierarchy of the tree node.
     * @throws ZXException Thrown if labelHierarchy fails.
     */
    public String labelHierarchy(char pstrSep) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrSep", pstrSep);
        }

        String labelHierarchy = null;
        
        try {

            if (this.parent == null) {
                labelHierarchy = getBo().formattedString("label");
            } else {
                labelHierarchy = this.parent.labelHierarchy(pstrSep) + pstrSep + getBo().formattedString("label");
            }

            return labelHierarchy;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Return the hierarchy of this tree node in the form of concatenated labels.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pstrSep = " + pstrSep);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return labelHierarchy;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(labelHierarchy);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Recalculate a node.
     * 
     * @param pobjParent The parent node of this node.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if recalc fails.
     */
    public zXType.rc recalc(RecTreeNode pobjParent) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjParent", pobjParent);
        }

        zXType.rc recalc = zXType.rc.rcOK;
        
        try {
        	
        	getBo().setValue("root", pobjParent.getBo().getValue("root"));
        	getBo().setValue("lvl", pobjParent.getBo().getValue("lvl").longValue() + 1 + "");
        	getBo().setValue("hrrchy", pobjParent.getBo().getValue("hrrchy").getStringValue() + getBo().getPKValue().getStringValue() + "/");
        	getBo().setValue("prnt", pobjParent.getBo().getPKValue());
            
            RecTreeNode objNode;
            Iterator iter = getChildNodes().iterator();
            while (iter.hasNext()) {
                objNode = (RecTreeNode) iter.next();
                
                objNode.recalc(this);
            }
            
            return recalc;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Recalculate a node.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pobjParent = " + pobjParent);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            recalc = zXType.rc.rcError;
            return recalc;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(recalc);
                getZx().trace.exitMethod();
            }
        }
    }
    
	/**
	 * Persist a node
	 *
	 * @param pstrGroup Optional, default is "*" 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if persist fails. 
	 */
	public zXType.rc persist(String pstrGroup) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrGroup", pstrGroup);
		}

		zXType.rc persist = zXType.rc.rcOK; 
		
		try {
			/**
			 * Persist the current BO
			 */
			if (getBo().persistBO(pstrGroup).pos != zXType.rc.rcOK.pos) {
				getZx().trace.addError("Unable to persist BO");
				persist = zXType.rc.rcError;
				return persist;
			}
			
			/**
			 * Persist the childern.
			 */
			RecTreeNode objNode;
			Iterator iter = getChildNodes().iterator();
			while (iter.hasNext()) {
				objNode = (RecTreeNode)iter.next();
				
				if (objNode.persist(pstrGroup).pos != zXType.rc.rcOK.pos) {
					persist = zXType.rc.rcError;
					return persist;
				}
			}
			
			return persist;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Persist a node", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrGroup = "+ pstrGroup);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			persist = zXType.rc.rcError;
			return persist;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(persist);
				getZx().trace.exitMethod();
			}
		}
	}
}