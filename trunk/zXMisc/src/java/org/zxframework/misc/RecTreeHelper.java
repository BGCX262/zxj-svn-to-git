package org.zxframework.misc;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSRS;
import org.zxframework.property.LongProperty;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringUtil;

/**
 * @author Michael Brewer
 */
public class RecTreeHelper extends ZXObject {
	
    LinkedHashMap tree = new LinkedHashMap();
    
    /**
     * Default constructor.
     */
    public RecTreeHelper() {
    	super();
	}
    
    //------------------------- Getters/Setters
    
    /**
     * Get the tree for this entity by using its pk as a key.
     * 
     * @param pobjBOME The entity to get the tree for.
     * @return Return the tree node.
     */
    public TreeNode getTree(ZXBO pobjBOME) {
    	try {
    		return (TreeNode)this.tree.get(pobjBOME.getPKValue().getStringValue());
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    /**
     * Set a tree for a entity using its primary key as a key.
     * 
     * @param pobjBOME The entity that this tree is for.
     * @param pobjTree The treenode to store.
     */
    public void setTree(ZXBO pobjBOME, TreeNode pobjTree) {
    	try {
    		this.tree.put(pobjBOME.getPKValue().getStringValue(), pobjTree);
    	} catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    /**
     * The current rectree (ie me) has been populated and needs to be inserted into the tree as the parent of the rectree with the PK as passed.
     * 
     * <pre>
     * 
     * Assumes   :
     *  tx handled by calling routine
     * </pre>
     * 
     * @param pobjBOME The bo to add as parent.
     * @param plngParentOf PK of node that I will be the new parent of
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if addAsNewParent fails.
     */
    public zXType.rc addAsNewParent(ZXBO pobjBOME, int plngParentOf) throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjBOME", pobjBOME);
			getZx().trace.traceParam("plngParentOf", plngParentOf);
		}
		
		zXType.rc addAsNewParent = zXType.rc.rcOK;
		
		try {
		    /**
		     * This is slightly unnecessary because this problem will get caught
		     * in the persist at the end of the function, but that won't tell the user why it
		     * failed. Therefore generate a friendly message, since entering a duplicate name is
		     * by far the most likely reason adding a new parent could fail.
		     */
		    if (pobjBOME.doesExist(pobjBOME.getDescriptor().getUniqueConstraint())) {
		        getZx().trace.userErrorAdd("Inserting this parent would cause duplicates");
		        addAsNewParent = zXType.rc.rcWarning;
		        return addAsNewParent;
		    }
		    
		    /**
		     * Retrieve the node that I'll be the parent of
		     */
		    ZXBO objBO = pobjBOME.cloneBO();
		    if (objBO == null) {
		        getZx().trace.addError("Unable to clone instance of " + pobjBOME.getDescriptor().getName());
		        addAsNewParent = zXType.rc.rcError;
		        return addAsNewParent;
		    }
		    
		    objBO.setValue("id", new LongProperty(plngParentOf));
		    
		    if (objBO.loadBO().pos != zXType.rc.rcOK.pos) {
		        getZx().trace.addError("Unable to load instance of " + pobjBOME.getDescriptor().getName());
		        addAsNewParent = zXType.rc.rcError;
		        return addAsNewParent;
		    }
		    
		    /**
		     * Now load the tree
		     **/
		    ZXBO objRootBO = pobjBOME.cloneBO();
		    if (objRootBO == null) {
		        getZx().trace.addError("Unable to clone instance of " + pobjBOME.getDescriptor().getName());
		        addAsNewParent = zXType.rc.rcError;
		        return addAsNewParent;		    
		    }
		    
		    objRootBO.setValue("id", objBO.getValue("root"));
		    
		    if (resolve(objRootBO).pos != zXType.rc.rcOK.pos ) {
		        getZx().trace.addError("Unable to resolve acctGrp");
		        addAsNewParent = zXType.rc.rcError;
		        return addAsNewParent;
			}
		    
		    TreeNode objNode = null;
		    if (objBO.getValue("lvl").longValue() == 1) {
		        /**
		         * Look at me, I'll be the new root of the tree!!!!
		         **/
		        pobjBOME.setValue("root", pobjBOME.getValue("id"));
		        
		        objNode = new TreeNode();
		        objNode.init(pobjBOME, null);
		        
		    } else {
		        /**
		         * Just a mortal node somewhere down the tree
		         * Retrieve my new parent
		         **/
		        pobjBOME.setValue("root", objBO.getValue("root"));
		        
		        ZXBO objParentBO = pobjBOME.cloneBO();
		        if (objParentBO == null) {
		            getZx().trace.addError("Unable to clone instance of " + pobjBOME.getDescriptor().getName());
			        addAsNewParent = zXType.rc.rcError;
			        return addAsNewParent;
		        }
		        
		        objParentBO.setValue("id", objBO.getValue("prnt"));
		        if (objParentBO.loadBO().pos != zXType.rc.rcOK.pos) {
		            getZx().trace.addError("Unable to load instance of " + pobjBOME.getDescriptor().getName());
			        addAsNewParent = zXType.rc.rcError;
			        return addAsNewParent;
		        }
		        
		        objNode = new TreeNode();
		        objNode.init(pobjBOME, getTree(objRootBO).findNode(objParentBO.getValue("id").intValue()));
		    }
		    
		    /**
		     * Add my kid to the children
		     **/
		    TreeNode objChildNode = getTree(objRootBO).findNode(objBO.getValue("id").intValue());
		    if (objChildNode == null) {
		        getZx().trace.addError("Unable to find child node");
		        addAsNewParent = zXType.rc.rcError;
		        return addAsNewParent;
		    }
		    
		    objNode.getChildren().put(new Integer(objBO.getValue("id").intValue()), objChildNode);
		    
		    /**
		     * Tricky stuff: if I'm the new root, make me the tree!!!
		     **/
		    if (objNode.getLevel() == 1) {
		        setTree(objRootBO, objNode);
		    }

		    /**
		     * Recalculate the tree
		     **/
		    if (getTree(objRootBO).recalculate(getTree(objRootBO), null).pos != zXType.rc.rcOK.pos) {
		        getZx().trace.addError("Unable to recalculate acctGrp tree");
		        addAsNewParent = zXType.rc.rcError;
		        return addAsNewParent;
		    }
		        
		    /**
		     * And persist but first make sure that I'm seen as new
		     **/
		    pobjBOME.setPersistStatus(zXType.persistStatus.psNew);
		    if (getTree(objRootBO).persist().pos != zXType.rc.rcOK.pos) {
		        /**
		         * DGS10MAY2005: We need to do something to catch the fact that the insert may fail.
		         * Otherwise it commits the other changes without having inserted the new parent!
		         * Having said that, the new check for a duplicate name (see near the top of this
		         * function) will eliminate the most likely cause of this failing, so not such a
		         * serious problem now anyway, but still should be captured.
		         */
		        getZx().trace.userErrorAdd("Unable to insert new parent");
		        addAsNewParent = zXType.rc.rcError;
		        return addAsNewParent;
		    }
		    
			return addAsNewParent;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Persist a tree.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : plngParentOf = " + plngParentOf);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			addAsNewParent = zXType.rc.rcError;
			return addAsNewParent;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(addAsNewParent);
				getZx().trace.exitMethod();
			}
		}
    }
    
    /**
     * Prepare this BO to be added to an organisation.
     * 
     * @param pobjBOME The bo with a rectree.
     * @param plngParent  0: no parent (ie new root)
     * @return Returns the return code of the method.
     */
    public zXType.rc prepareNewChildNode(ZXBO pobjBOME, int plngParent) {
    	zXType.rc prepareNewChildNode = zXType.rc.rcOK;
    	try {
    		
    		/**
    		 * Reset and optionally assign a new key
    		 */
			pobjBOME.resetBO("*", true);
			
    	    if (plngParent == 0) {
    	    	/**
    	    	 * New root
    	    	 */
    	    	pobjBOME.setValue("root", pobjBOME.getValue("id"));
    	        pobjBOME.setValue("lvl", new LongProperty(1));
    	        pobjBOME.setValue("prnt", new LongProperty(0));
    	        
    	        /**
    	         * Determine hierarchy
    	         */
    	        pobjBOME.setValue("hrrchy", new StringProperty(determinePath(pobjBOME)));
    	        
    	    } else {
    	    	/**
    	    	 * Existing organisational tree
    	    	 */
    	        ZXBO objBO = pobjBOME.cloneBO();
    	        if (objBO == null) {
    	            getZx().trace.addError("Unable to clone " + pobjBOME.getDescriptor().getName());
    	            prepareNewChildNode = zXType.rc.rcError;
    	            return prepareNewChildNode;
    	        }
    	        
    	        /**
    	         * Load parent
    	         */
    	        objBO.setValue("id", new LongProperty(plngParent));
    	        if (objBO.loadBO().pos != zXType.rc.rcOK.pos) {
    	            getZx().trace.addError("Unable to load parent " + pobjBOME.getDescriptor().getName());
    	            prepareNewChildNode = zXType.rc.rcError;
    	            return prepareNewChildNode;
    	        }
    	        
    	        /**
    	         * Same root as parent, same type as parent, level one more than parent
    	         * and pk to parent
    	         */
    	        pobjBOME.setValue("root", objBO.getValue("root"));
    	        pobjBOME.setValue("lvl", new LongProperty(objBO.getValue("lvl").longValue() + 1));
    	        pobjBOME.setValue("prnt", new LongProperty(plngParent));
    	        
    	        /**
    	         * Determine hierarchy
    	         */
    	        pobjBOME.setValue("hrrchy", new StringProperty(determinePath(pobjBOME)));
    	        
    	    }
    	    
        	return zXType.rc.rcOK;
    	} catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }

    /**
     * Delete a node and all its subnodes from the tree.
     * 
     * <pre>
     *  
     *  Assumes   :
     *     I have been loaded
     * </pre>
     * 
     * @param pobjBOME The entity to delete.
     * @return Returns the return code of the method
     */
    public zXType.rc deleteNode(ZXBO pobjBOME) {
    	try {
    		zXType.rc deleteNode = zXType.rc.rcOK;
    		
    		/**
    		 * Resolve the root
    		 */
    	    ZXBO objRootBO = getZx().createBO(pobjBOME.getDescriptor().getName());
    	    if (objRootBO == null) {
    	        getZx().trace.addError("Unable to create instance of " + pobjBOME.getDescriptor().getName());
    	        deleteNode = zXType.rc.rcError;
    	        return deleteNode;
    	    }
    	    
    	    objRootBO.setValue("id", pobjBOME.getValue("root"));
    	    
    	    /**
    	     * Resolve the whole tree from root down.
    	     */
    	    if (resolve(objRootBO).pos != zXType.rc.rcOK.pos) {
    	    	getZx().trace.addError("Unable to resolve hierarchy");
    	        deleteNode = zXType.rc.rcError;
    	    	return deleteNode;
    	    }
    	    
    	    /**
    	     * Find the node to delete 
    	     */
    	    TreeNode objNodeToDelete = getTree(objRootBO).findNode(pobjBOME.getValue("id").intValue());
    	    if (objNodeToDelete == null) {
    	        getZx().trace.addError("Unable to find node to delete in hierarchy");
    	        deleteNode = zXType.rc.rcOK;
    	        return deleteNode;
    	    }
    	    
    	    /**
    	     * And delete
    	     */
    	    int intRC = objNodeToDelete.deleteNode().pos;
    	    if (intRC == zXType.rc.rcError.pos) {
    	    	getZx().trace.addError("Unable to delete node and sub nodes");
    	    	deleteNode = zXType.rc.rcError;
    	    	return deleteNode;
    	    	
    	    } else if (intRC == zXType.rc.rcOK.pos) {
    	    	/**
    	    	 * All ok carry on
    	    	 */
    	    } else {
    	    	/**
    	    	 * Warning : Nothing to delete
    	    	 */
		        deleteNode = zXType.rc.rcWarning;
		        return deleteNode;
    	    }
    	    
    	    /**
    	     * And persist tree 
    	     */
    	    intRC = getTree(objRootBO).persist().pos;
    	    if (intRC == zXType.rc.rcOK.pos) {
    	        /**
    	         * All ok
    	         */
    	    	
    	    } else if (intRC == zXType.rc.rcWarning.pos) {
    	        /**
    	         * Not found 
    	         */
    	        deleteNode = zXType.rc.rcWarning;
    	        
    	    } else if (intRC == zXType.rc.rcError.pos) {
    	        /**
    	         * Still associations 
    	         */
    	        deleteNode = zXType.rc.rcError;
    	        
    	    }
    	    
    		return deleteNode;
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    /**
     * Move a node and all its subnodes to a new place in the tree.
     * 
     * <pre>
     * 
     * Assumes   :
     * 	resolve has been done
     * </pre>
     * 
     * @param pobjBOME The entity to move in the tree.
     * @param plngWhat What is the node to move?
     * @param plngTo What is going to be the new parent
     * @return Returns the return code of the method.
     */
    public zXType.rc moveNode(ZXBO pobjBOME, int plngWhat, int plngTo) {
    	try {
    		zXType.rc moveNode = zXType.rc.rcOK;
    		
    		/**
    		 * Retrieve the what and to nodes 
    		 */
    	    TreeNode objNodeWhat = getTree(pobjBOME).findNode(plngWhat);
    	    if (objNodeWhat == null) {
    	        getZx().trace.addError("Unable to retrieve node to move");
    	        moveNode = zXType.rc.rcError;
    	        return moveNode;
    	    }
    	    
    	    TreeNode objNodeNewParent = getTree(pobjBOME).findNode(plngTo);
    	    if (objNodeNewParent == null) {
    	        getZx().trace.addError("Unable to retrieve new parent node");
    	        moveNode = zXType.rc.rcError;
    	        return moveNode;
    	    }
    	    
    	    TreeNode objNodeOldParent = getTree(pobjBOME).findNode(objNodeWhat.getBo().getValue("prnt").intValue());
    	    if (objNodeOldParent == null) {
    	        getZx().trace.addError("Unable to retrieve old parent node");
    	        moveNode = zXType.rc.rcError;
    	        return moveNode;
    	    }
    	    
    	    /**
    	     * Remove the what-node from the child collection of the old parent 
    	     */
    	    int j = 0;
    	    TreeNode objNode = null;
    	    
    	    Iterator iter = objNodeOldParent.getChildren().values().iterator();
    	    while (iter.hasNext()) {
    	    	objNode = (TreeNode)iter.next();
    	        
    	    	j = j + 1;
    	        
    	        if (objNode.getBo().getValue("id").intValue() == objNodeWhat.getBo().getValue("id").intValue()) {
    	        	iter.remove();
    	    	    objNodeNewParent.getChildren().put(new Integer(objNodeWhat.getBo().getValue("id").intValue()), objNode);
    	    	    
    	    	    /**
    	    	     * And 'recalculate' the tree (resets level, parents and hierarchies)
    	    	     */
    	    	    if (getTree(pobjBOME).recalculate(getTree(pobjBOME), null).pos != zXType.rc.rcOK.pos) {
    	    	        getZx().trace.addError("Unable to recalculate tree after move");
    	    	        moveNode = zXType.rc.rcError;
    	    	        return moveNode;
    	    	    }
    	    	    
    	            return moveNode;
    	        }
    	    }
    	    
    	    if (objNode == null) {
    	        getZx().trace.addError("Unable to find node to move in children collection of old parent");
    	        moveNode = zXType.rc.rcError;
    	        return moveNode;
    	    }
    	    
    		return moveNode;
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    /**
     * Do a simple merge of 2 nodes.
     * 
     * @param pobjBOME The node entity to merge
     * @param pobjDestination And Where to merge it.
     * @return Returns the return code of the method.
     */
    public zXType.rc mergeNodes(ZXBO pobjBOME, ZXBO pobjDestination) {
    	try {
    		zXType.rc mergeNodes = zXType.rc.rcOK;
    		
    		int intSPK = 0;
    		if (StringUtil.isNumeric(getZx().getQuickContext().getEntry("-spk"))) {
    			intSPK = Integer.parseInt(getZx().getQuickContext().getEntry("-spk"));
    		}
    		int intSPK2 = 0;
    		if (StringUtil.isNumeric(getZx().getQuickContext().getEntry("-spk2"))) {
    			intSPK2 = Integer.parseInt(getZx().getQuickContext().getEntry("-spk2"));
    		}
    		
    	    /**
    	     * Find the nodes that we need to work on 
    	     */
    	    TreeNode objMergeNode = getTree(pobjBOME).findNode(intSPK);
    	    if (objMergeNode == null) {
    	        getZx().trace.addError("Unable to find node to merge");
    	        mergeNodes = zXType.rc.rcError;
    	        return mergeNodes;
    	    }
    	    
    	    TreeNode objDestinationNode = getTree(pobjDestination).findNode(intSPK2);
    	    if (objDestinationNode == null) {
    	        getZx().trace.addError("Unable to find node to merge to");
    	        mergeNodes = zXType.rc.rcError;
    	        return mergeNodes;
    	    }
    	    
    	    /**
    	     * The node to merge will be merged with the destination node, this
    	     * means a number of things are required:
    	     * - All subnodes need to be assigned to the new root
    	     * - Direct child nodes need to be assigned the new parent
    	     * - Merge direct child nodes into new tree
    	     * - Re-calculate the merge tree
    	     * - In other more complicated merges other actions may be applied.
    	     * - The node can be deleted 
    	     */
    	    
    	    /**
    	     * New root for whole sub tree
    	     **/
    	    if (objMergeNode.newRoot(pobjDestination.getValue("id").intValue()).pos != zXType.rc.rcOK.pos) {
    	        getZx().trace.addError("Unable to move nodes to new root");
    	        mergeNodes = zXType.rc.rcOK;
    	        return mergeNodes;
    	    }
    	    
    	    TreeNode objNode;
    	    Iterator iter = objMergeNode.getChildren().values().iterator();
    	    while (iter.hasNext()) {
    	    	objNode = (TreeNode)iter.next();
    	    	
    	        /**
    	         * New parent for the direct children of the merged node 
    	         */
    	        objNode.getBo().setValue("prnt", objDestinationNode.getBo().getValue("id"));
    	        
    	        objNode.init(objNode.getBo(), objDestinationNode);
    	                
    	        /**
    	         * And merge into new tree by re-initializing the node and all sub nodes
    	         */
    	        if (objNode.reInit().pos !=  zXType.rc.rcOK.pos) {
    	            getZx().trace.addError("Unable to re-initialise merged nodes");
    	            mergeNodes = zXType.rc.rcError;
    	            return mergeNodes;
    	        }
    	        
    	    } // Loop over merge node children
    	    
    	    /**
    	     * Recalculate the destination tree 
    	     */
    	    if (getTree(pobjDestination).recalculate(getTree(pobjDestination), null).pos != zXType.rc.rcOK.pos) {
    	        getZx().trace.addError("Unable to re-calculate the merged hierarchy");
    	        mergeNodes = zXType.rc.rcError;
    	        return mergeNodes;
    	    }
    	    
    	    /**
    	     * Delete the merged node 
    	     */
    	    if (objMergeNode.getBo().deleteBO().pos != zXType.rc.rcOK.pos) {
    	        getZx().trace.addError("Unable to delete merged node");
    	        mergeNodes = zXType.rc.rcError;
    	        return mergeNodes;
    	    }
    	    
    		return mergeNodes;
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    /**
     * Resolve an account group.
     * i.e. load the whole tree.
     * 
     * <pre>
     * 
     * Assumes   :
     *    PK of me has been set
     * </pre>
     * 
     * @param pobjBOME The rectree object to resolve its tree for.
     * @return Returns the return code of the method.
     */
    public zXType.rc resolve(ZXBO pobjBOME) {
    	try {
    		zXType.rc resolveRecursiveTree = zXType.rc.rcOK;
    		
    	    /**
    	     * Load me 
    	     */
    	    if (pobjBOME.loadBO().pos != zXType.rc.rcOK.pos) {
    	        getZx().trace.addError("Unable to load acctGrp");
    	        resolveRecursiveTree = zXType.rc.rcError;
    	        return resolveRecursiveTree;
    	    }

    	    String strQry = getZx().getSql().loadQuery(pobjBOME, "*");
    	    strQry = strQry + " AND " + getZx().getSql().singleWhereCondition(pobjBOME, 
    	    																  pobjBOME.getDescriptor().getAttribute("root"),
    	    																  zXType.compareOperand.coEQ, pobjBOME.getValue("id"));
    	    // strQry = strQry & i.zX.SQL.orderByClause(Me, "lvl,hrrchy") // Could be more reliable
    	    strQry = strQry + getZx().getSql().orderByClause(pobjBOME, "lvl,nme", false);
    	    
    	    DSHRdbms objRdbms = (DSHRdbms)pobjBOME.getDS();
    	    DSRS objRS = objRdbms.sqlRS(strQry);
    	    if (objRS == null) {
    	        getZx().trace.addError("Unable to execute query to retrieve acctGrp");
    	        resolveRecursiveTree = zXType.rc.rcError;
    	        return resolveRecursiveTree;
    	    }
    	    
    	    /**
    	     * Initialise the tree
    	     */
    	    TreeNode objTree = new TreeNode();
    	    objTree.init(null, null);
    	    setTree(pobjBOME, objTree);
    	    
    	    ZXBO objBO;
    	    TreeNode objNode;
    	    
    	    while (!objRS.eof()) {
    	        objBO = pobjBOME.cloneBO();
    	        if (objBO == null) {
    	            getZx().trace.addError("Unable to clone instance of agAcctGrp");
    	            resolveRecursiveTree = zXType.rc.rcError;
    	            return resolveRecursiveTree;
    	        }
    	        
    	        objRS.rs2obj(objBO, "*");
    	        
    	        /**
    	         * Find node in tree where this object is to be added 
    	         */
    	        TreeNode objParent = objTree.findNode(objBO.getValue("prnt").intValue());
    	        
    	        if (objParent == null) {
    	            /**
    	             * Must be the new root 
    	             */
    	            objTree.init(objBO, null);
    	            
    	        } else {
    	            /**
    	             * Add new node to tree
    	             */
    	        	objNode = new TreeNode();
    	            objNode.init(objBO, objParent);
    	            
    	        }
    	        
    	        objRS.moveNext();
    	    }
    	    
    		return resolveRecursiveTree;
    	    
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    /**
     * Determine the hierarchy string for this node.
     * 
     * @param pobjBOME The bo to start from
     * @return Returns the hierarchy string
     */
    private String determinePath(ZXBO pobjBOME) {
    	try {
    		String determinePath = "";
    		
    	    /**
    	     * If the tree is available, use the tree to get the hierarchy path
    	     * otherwise we have to retrieve all the elements up to the top
    	     */
    	    if (getTree(pobjBOME) == null) {
    	        if (pobjBOME.getValue("prnt").longValue() > 0) {
    	            ZXBO objAcctGrp = pobjBOME.cloneBO();
    	            if (objAcctGrp == null) {
    	                getZx().trace.addError("Unable to clone instance of agAcctGrp");
    	                throw new RuntimeException("Unable to clone instance of agAcctGrp");
    	            }
    	            
    	            objAcctGrp.setValue("id", pobjBOME.getValue("prnt"));
    	            if (objAcctGrp.loadBO("prnt").pos != zXType.rc.rcOK.pos) {
    	                getZx().trace.addError("Unable to load agAcctGrp");
    	                throw new RuntimeException("Unable to load agAcctGrp");
    	            }
    	            
    	            determinePath = determinePath(objAcctGrp) + pobjBOME.getValue("id").getStringValue() + "/";
    	            
    	        } else {
    	        	determinePath = "/" + pobjBOME.getValue("id").getStringValue() + "/";
    	        	
    	        }
    	        
    	    } else {
    	    	determinePath = getTree(pobjBOME).getPath();
    	    }
    	    
	    	return determinePath;
    	    
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
}