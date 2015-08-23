package org.zxframework.misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.zxframework.ZXBO;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.property.LongProperty;
import org.zxframework.property.StringProperty;

/**
 * @author Michael Brewer
 */
public class TreeNode extends ZXObject{
	
	private TreeNode parent;
	private ZXBO bo;
	private int root;
	private int level;
	private String hierarchy;
	private Map children;
	
	/**
	 * @return the bo
	 */
	public ZXBO getBo() {
		return bo;
	}

	/**
	 * @param bo the bo to set
	 */
	public void setBo(ZXBO bo) {
		this.bo = bo;
	}

	/**
	 * @return the children
	 */
	public Map getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(Map children) {
		this.children = children;
	}

	/**
	 * @return the hierarchy
	 */
	public String getHierarchy() {
		return hierarchy;
	}

	/**
	 * @param hierarchy the hierarchy to set
	 */
	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the parent
	 */
	public TreeNode getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	/**
	 * @return the root
	 */
	public int getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(int root) {
		this.root = root;
	}

	/**
	 * Default constructor.
	 */
	public TreeNode() {
		this.children = new HashMap();
	}
	
	/**
	 * @param pobjAcctGrp
	 * @param pobjParent
	 */
	public void init(ZXBO pobjAcctGrp, TreeNode pobjParent) {
	    this.parent = pobjParent;
	    this.bo = pobjAcctGrp;
	    
	    try {
		    if (pobjAcctGrp != null) {
		        /**
		         * Set parent, level and hierarchy
		         */
		        if (getParent() != null) {
		        	getParent().getChildren().put(new Integer(pobjAcctGrp.getValue("id").intValue()), this);
		        }
		        
		        LevelAndRoot objLevelAndRoot = new LevelAndRoot(0,0);
		        determineLevelAndRoot(objLevelAndRoot);
		        this.level = objLevelAndRoot.intLevel;
		        this.hierarchy = pobjAcctGrp.getValue("hrrchy").getStringValue();
		        this.root = objLevelAndRoot.intRoot;
		    }
	    } catch (Exception e) {
	    	throw new RuntimeException(e);
	    }
	}
	
	/**
	 * Re-initialise node and all sub-nodes.
	 * @return Returns the return code of the method.
	 */
	protected zXType.rc reInit() {
		try {
			zXType.rc reInit = zXType.rc.rcOK;
			
		    /**
		     * For me 
		     */
		    init(getBo(), getParent());
		    setHierarchy(getPath());
		    
		    /**
		     * And the children 
		     */
		    TreeNode objNode;
		    Iterator iter = getChildren().values().iterator();
		    while (iter.hasNext()) {
		    	objNode = (TreeNode)iter.next();
		    	
		        objNode.init(objNode.getBo(), this);
		        objNode.setHierarchy(objNode.getPath());
		        
		        if (objNode.reInit().pos != zXType.rc.rcOK.pos) {
		            getZx().trace.addError("Unable to re-init for node");
		            reInit = zXType.rc.rcError;
		            return reInit;
		        }
		        
		    } // Loop over child nodes			
			
			
			return reInit;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	class LevelAndRoot {
		protected int intLevel;
		protected int intRoot;
		private LevelAndRoot(int level, int root) {
			this.intLevel = level;
			this.intRoot = root;
		}
	}
	protected void determineLevelAndRoot(LevelAndRoot pobjLevelAndRoot) {
		try {
			pobjLevelAndRoot.intLevel = pobjLevelAndRoot.intLevel + 1;
		    pobjLevelAndRoot.intRoot = getBo().getValue("id").intValue();
		    if (parent != null) {
		        parent.determineLevelAndRoot(pobjLevelAndRoot);
		    }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Find a node by primary key.
	 * 
	 * @param plngKey The primary key of the node.
	 * @return Returns a node by its primary key.
	 */
	public TreeNode findNode(int plngKey) {
		try {
			TreeNode findNode = null;
			
		    /**
		     * It could be that we are dealing with a brand new tree where the
		     * root has not even be initialised
		     */
		    if (getBo() == null) {
		        return findNode;
		    }
		    
		    /**
		     * First check me 
		     */
		    if (getBo().getValue("id").intValue() == plngKey) {
		        findNode = this;
		        return findNode;
		    }
		    
		    /**
		     * One of my childern?
		     */
		    findNode = (TreeNode)getChildren().get(new Integer(plngKey));
		    if (findNode != null) {
		    	return findNode;
		    }
		    
	        /**
	         * One of my grandchildern? 
	         */
	    	TreeNode objNode;
	    	TreeNode objParent;
	    	
	    	Iterator iter = getChildren().values().iterator();
	    	while (iter.hasNext()) {
				objNode = (TreeNode)iter.next();
				
	            objParent = objNode.findNode(plngKey);
	            if (objParent != null) {
	            	findNode = objParent;
	            	return findNode;
	            }
	            
			}
	    	
	    	return findNode;
	    	
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Recalculate all nodes in the tree
	 *  - level
	 *  - parent
	 *  - hierarcy
	 *  
	 * @param pobjRoot The root node of the tree.
	 * @param pobjParent The current parent
	 * @return Returns the return code of the method.
	 */
	public zXType.rc recalculate(TreeNode pobjRoot, TreeNode pobjParent) {
		try {
			zXType.rc recalculate = zXType.rc.rcOK;
			
		    if (pobjParent == null) {
		        /**
		         * Root
		         */
		        setLevel(1);
		        setParent(null);
		        setRoot(getBo().getValue("id").intValue());
		        setHierarchy(getPath());
		        
		    } else {
		        /**
		         * Non-root 
		         */
		        setLevel(pobjParent.getLevel() + 1);
		        setParent(pobjParent);
		        setRoot(pobjRoot.getRoot());
		        setHierarchy(getPath());
		        
		    }
		    
		    if (getParent() == null) {
		        getBo().setValue("prnt", new LongProperty(0, true));
		    } else {
		        getBo().setValue("prnt", getParent().getBo().getValue("id"));
		    }
		    
		    getBo().setValue("lvl", new LongProperty(getLevel()));
		    getBo().setValue("hrrchy", new StringProperty(getHierarchy()));
		    getBo().setValue("root", new LongProperty(getRoot()));
		    
		    TreeNode objNode;
		    Iterator iter = getChildren().values().iterator();
		    while (iter.hasNext()) {
		    	objNode = (TreeNode)iter.next();
		        objNode.recalculate(pobjRoot, this);
		    }
		    
			return recalculate;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Persist the tree.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc persist() {
		try {
			zXType.rc persist = zXType.rc.rcOK;
			
			TreeNode objNode;
			
			Iterator iter = getChildren().values().iterator();
			while (iter.hasNext()) {
				objNode = (TreeNode)iter.next();
				
				if (objNode.persist().pos != zXType.rc.rcOK.pos) {
		            getZx().trace.addError("Unable to persist bo");
		            persist = zXType.rc.rcError;
		            return persist;
		        }
			}
			
			if (getBo().persistBO("*").pos != zXType.rc.rcOK.pos) {
		        /**
		         * Still associations 
		         */
				persist = zXType.rc.rcError;
				return persist;
			}
			
			return persist;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Delete this node and all its subnodes.
	 * 
	 * @return Returns the return code of the method.
	 */
	public zXType.rc deleteNode() {
		try {
			zXType.rc deleteNode = zXType.rc.rcOK;
			
		    /**
		     * Delete the account group
		     */
		    if (getBo() != null) {
		        getBo().setPersistStatus(zXType.persistStatus.psDeleted);
		    }
		    
		    TreeNode objNode;
		    
		    Iterator iter = getChildren().values().iterator();
		    while (iter.hasNext()) {
				objNode = (TreeNode)iter.next();
				
				int intRC = objNode.deleteNode().pos;
				if (intRC == zXType.rc.rcOK.pos) {
					// Ignored
					
				} else if (intRC == zXType.rc.rcWarning.pos) {
		            getZx().trace.addError("Unable to delete account node");
		            deleteNode = zXType.rc.rcWarning;
					return deleteNode;
				} else if (intRC == zXType.rc.rcError.pos) {
		            getZx().trace.addError("Unable to delete account node");
		            deleteNode = zXType.rc.rcError;
		            return deleteNode;
				}
				
		    }
		    
			return deleteNode;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get path from me to root.
	 * 
	 * @return Get path from me to root
	 */
	public String getPath() {
		try {
			String getPath = "";
			
		    if (getParent() == null) {
		    	getPath = "/" + getBo().getValue("id").getStringValue() + "/";
		    	
		    } else {
	            getPath = getParent().getPath() + getBo().getValue("id").getStringValue() + "/";
	            
		    }
		    
		    return getPath;
		    
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Give node and all subnodes a new root.
	 * 
	 * @param plngNewRoot New root pk
	 * @return Returns the return code of the method.
	 */
	public zXType.rc newRoot(int plngNewRoot) {
		try {
			zXType.rc newRoot = zXType.rc.rcOK;
			
		    /**
		     * For me
		     */
		    getBo().setValue("root", new LongProperty(plngNewRoot));
		    
		    /**
		     * And the children 
		     */
		    TreeNode objNode;
		    Iterator iter = getChildren().values().iterator();
		    while (iter.hasNext()) {
		    	objNode = (TreeNode)iter.next();
		    	
		        objNode.getBo().setValue("root", new LongProperty(plngNewRoot));
		        
		        if (objNode.newRoot(plngNewRoot).pos != zXType.rc.rcOK.pos) {
		            getZx().trace.addError("Unable to set root for node");
		            newRoot = zXType.rc.rcError;
		            return newRoot;
		        }
		        
		    } // Loop over child nodes
		    
		    return newRoot;
		    
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
