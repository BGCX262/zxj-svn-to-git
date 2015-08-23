/*
 * Created on Jun 9, 2005
 * $Id: DBAction.java,v 1.1.2.15 2006/07/17 16:10:54 mike Exp $
 */
package org.zxframework.doc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zxframework.Tuple;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.DomElementUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * The interface definition for a specific doc builder action.
 * 
 * <pre>
 * <b>NOTE :</b> This merges iDBAction and clsDBAction.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 16 May 2003
 * 
 * Change    : BD28NOV03
 * Why       : Added new action type gridMerge
 *
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class DBAction extends ZXObject {
	
	private static Log log = LogFactory.getLog(DBAction.class);
	
	//------------------------ iDBAction Members
	
	private DocBuilder docBuilder;
	
	//------------------------ DBAction Members
	
	private String name;
	private String comment;
	private zXType.docBuilderActionType actionType;
	private String linkaction;
	private Map entities;
	private List qs;
	private String entityrefaction;
	
	//------------------------ XML Element constants
	
	protected static final String DBACTION_STARTTAG = "action";
	private static final String DBACTION_NAME = "name";
	private static final String DBACTION_COMMENT = "comment";
	private static final String DBACTION_ACTIONTYPE = "actiontype";
	private static final String DBACTION_LINKACTION = "linkaction";
	private static final String DBACTION_ENTITYREFACTION = "entityrefaction";
	private static final String DBACTION_ENTITIES = "entities";
	private static final String DBACTION_QS = "qs";
	protected static final String DBACTION_TUPLE_NAME = "source";
	protected static final String DBACTION_TUPLE_VALUE = "destination";
    
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public DBAction() {
		super();
	}
	
	//------------------------ iDBAction Getters/Setters
	
	/**
	 * @return Returns the docBuilder.
	 */
	public DocBuilder getDocBuilder() {
		return docBuilder;
	}

	/**
	 * @param docBuilder The docBuilder to set.
	 */
	public void setDocBuilder(DocBuilder docBuilder) {
		this.docBuilder = docBuilder;
	}
	
	//------------------------ DBAction Getters/Setters
	
	/**
	 * @return Returns the actiontype.
	 */
	public zXType.docBuilderActionType getActionType() {
		return actionType;
	}
	
	/**
	 * @param actiontype The actiontype to set.
	 */
	public void setActionType(zXType.docBuilderActionType actiontype) {
		this.actionType = actiontype;
	}
	
	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * A collection (DBEntity) of entities.
	 * 
	 * @return Returns the entities.
	 */
	public Map getEntities() {
		if (this.entities == null) {
			this.entities = new ZXCollection();
		}
		return entities;
	}

	/**
	 * @param entities The entities to set.
	 */
	public void setEntities(Map entities) {
		this.entities = entities;
	}

	/**
	 * @return Returns the entityrefaction.
	 */
	public String getEntityrefaction() {
		return entityrefaction;
	}

	/**
	 * @param entityrefaction The entityrefaction to set.
	 */
	public void setEntityrefaction(String entityrefaction) {
		this.entityrefaction = entityrefaction;
	}

	/**
	 * @return Returns the linkaction.
	 */
	public String getLinkaction() {
		return linkaction;
	}

	/**
	 * @param linkaction The linkaction to set.
	 */
	public void setLinkaction(String linkaction) {
		this.linkaction = linkaction;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * A collection (ArrayList)(Tuple) of query string entries.
	 * 
	 * @return Returns the qs.
	 */
	public List getQs() {
		return qs;
	}

	/**
	 * @param qs The qs to set.
	 */
	public void setQs(List qs) {
		this.qs = qs;
	}
	
	//------------------------ Digester util methods.
	
	/**
	 * Special setter used during parsing.
	 * 
	 * @param actiontype The actiontype to set.
	 */
	public void setActiontype(String actiontype) {
		this.actionType = zXType.docBuilderActionType.getEnum(actiontype);
	}
	
	//------------------------ DBAction Inteface methods
	
    /**
     * XML dump.
     * 
     * The overidden method should called this via super.dumpAsXML();
     * @param objXMLGen Handle to xml generator.
     */
    public void dumpAsXML(XMLGen objXMLGen) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        try {
        	objXMLGen.taggedValue(DBACTION_NAME, getName());
    		objXMLGen.taggedValue("actiontype", zXType.valueOf(getActionType()));
        	
        	objXMLGen.taggedCData(DBACTION_COMMENT, getComment(), false);
        	objXMLGen.taggedCData(DBACTION_LINKACTION, getLinkaction(), false);
        	objXMLGen.taggedCData(DBACTION_ENTITYREFACTION, getEntityrefaction(), false);
        	
        	if (getEntities() != null && getEntities().size() > 0) {
	        	objXMLGen.openTag(DBACTION_ENTITIES);
	        	
	        	DBEntity objEntity;
	        	Iterator iter = getEntities().values().iterator();
	        	while(iter.hasNext()) {
	        		objEntity = (DBEntity)iter.next();
	        		objEntity.dumpAsXML(objXMLGen);
	        	}
	        	
	        	objXMLGen.closeTag(DBACTION_ENTITIES);
        	}
        	
        	if (getQs() != null && getQs().size() > 0) {
        		objXMLGen.openTag(DBACTION_QS);
        		
        		Tuple objTuple;
        		for (int i = 0; i < getQs().size(); i++) {
        			objTuple = (Tuple)getQs().get(i);
        			
        			objXMLGen.openTag(DBACTION_QS);
        			
        			objXMLGen.taggedCData(DBACTION_TUPLE_NAME, objTuple.getName());
        			objXMLGen.taggedCData(DBACTION_TUPLE_VALUE, objTuple.getValue());
        			
        			objXMLGen.closeTag(DBACTION_QS);
				}
        		
        		objXMLGen.closeTag(DBACTION_QS);
        	}
        	
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Dump XML.", e);
            throw new NestableRuntimeException("Failed to : Dump XML.", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
	/**
	 * Perform the action.
	 * 
	 * @param pobjDocBuilder The current doc builder
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if go fails
	 */
	public abstract zXType.rc go(DocBuilder pobjDocBuilder) throws ZXException;
	
	/**
	 * @param pobjXMLNode The xml node representing this action.
	 * @return Returns the return code for this method.
	 */
	public abstract zXType.rc parse(org.w3c.dom.Element pobjXMLNode);
	
	//------------------------ DBAction specific parsing
	
	/**
	 * Parse Action.
	 * 
	 * @param pobjXMLNode The xml node with the action definition
	 * @return Returns the DBAction
	 */
	public static DBAction parseAction(org.w3c.dom.Element pobjXMLNode) {
		DBAction parseAction = null;
		try {
			/**
			 * Get this specific action type
			 */
			int intActionType = zXType.docBuilderActionType.getEnum(new DomElementUtil(pobjXMLNode).getChildText(DBACTION_ACTIONTYPE)).pos;
			if (intActionType == zXType.docBuilderActionType.dbatNull.pos) {
				parseAction = new DBNull();
				
			} else if (intActionType == zXType.docBuilderActionType.dbatBOMerge.pos) {
				parseAction = new DBBOMerge();
				
			} else if (intActionType == zXType.docBuilderActionType.dbatQuery.pos) {
				parseAction = new DBQuery();
				
			} else if (intActionType == zXType.docBuilderActionType.dbatBO2Grid.pos) {
				parseAction = new DBBO2Grid();
				
			} else if (intActionType == zXType.docBuilderActionType.dbatBO2Table.pos) {
				parseAction = new DBBO2Table();
				
			} else if (intActionType == zXType.docBuilderActionType.dbatMerge.pos) {
				parseAction = new DBMerge();

			} else if (intActionType == zXType.docBuilderActionType.dbatAddObject.pos) {
				parseAction = new DBAddObject();
				
			} else if (intActionType == zXType.docBuilderActionType.dbatEmbed.pos) {
				parseAction = new DBEmbed();

			} else if (intActionType == zXType.docBuilderActionType.dbatLoopOver.pos) {
				parseAction = new DBLoopOver();

			} else if (intActionType == zXType.docBuilderActionType.dbatGridMerge.pos) {
				parseAction = new DBGridMerge();
				
			} else {
				throw new RuntimeException("unknown action type : " + intActionType);
			}
			
			/**
			 * Parge DBAction specific stuff
			 */
			Node node;
			DomElementUtil element;
			String nodeName;
			NodeList nodeList = pobjXMLNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				if (node instanceof org.w3c.dom.Element) {
					element = new DomElementUtil((org.w3c.dom.Element)node);
					nodeName = node.getNodeName();
					
					if (DBACTION_NAME.equalsIgnoreCase(nodeName)) {
						parseAction.setName(element.getText());
						
					} else if (DBACTION_COMMENT.equalsIgnoreCase(nodeName)) {
						parseAction.setComment(element.getText());
						
					} else if (DBACTION_ACTIONTYPE.equalsIgnoreCase(nodeName)) {
						parseAction.setActiontype(element.getText());
						
					} else if (DBACTION_LINKACTION.equalsIgnoreCase(nodeName)) {
						parseAction.setLinkaction(element.getText());
						
					} else if (DBACTION_ENTITYREFACTION.equalsIgnoreCase(nodeName)) {
						parseAction.setEntityrefaction(element.getText());
						
					} else if (DBACTION_ENTITIES.equalsIgnoreCase(nodeName)) {
						parseAction.setEntities(parseEntities((org.w3c.dom.Element)node));
						
					} else if (DBACTION_QS.equalsIgnoreCase(nodeName)) {
						parseAction.setQs(parseTuples((org.w3c.dom.Element)node));
						
					}
				}
			}
			
			parseAction.parse(pobjXMLNode);
			
			return parseAction;
		} catch (Exception e) {
			throw new NestableRuntimeException("Failed to : Parse action", e);
		}
	}
	
    /**
     * Parse a collection of tuples.
     * 
     * @param pobjXMLNode The xml node
     * @return Returns the return code of the method.
     */
    public static List parseTuples(Element pobjXMLNode) {
        List parseTuples = null;
        
        try {
			Node node;
			parseTuples = new ArrayList();
			
			NodeList nodeList = pobjXMLNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				
				if (node instanceof org.w3c.dom.Element) {
					parseTuples.add(parseTuple((Element)node));
				}
			}
        	
        	return parseTuples;
        } catch (Exception e) {
            throw new NestableRuntimeException("Failed to : Parse tuples", e);
        }  
    }
	
    /**
     * Parse Tuple.
     * 
     * @param pobjXMLNode The xml node
     * @return Returns the return code of the method.
     */
    public static Tuple parseTuple(Element pobjXMLNode) {
        Tuple parseTuple = new Tuple();
        
        try {
        	DomElementUtil element;
			String nodeName;
			Node node;
			
			NodeList nodeList = pobjXMLNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				if (node instanceof Element) {
					element = new DomElementUtil((Element)node);
					nodeName = node.getNodeName();
					
					if ("source".equalsIgnoreCase(nodeName)) {
						parseTuple.setName(element.getText());
					} else if ("destination".equalsIgnoreCase(nodeName)) {
						parseTuple.setValue(element.getText());
					}
				}
			}
        	
        	return parseTuple;
        } catch (Exception e) {
            throw new NestableRuntimeException("Failed to : Parse Tuple", e);
        }   
    }
    
    /**
     * Parse Entities.
     * 
     * @param pobjXMLNode The xml node
     * @return Returns the return code of the method.
     */
    public static Map parseEntities(Element pobjXMLNode) {
        Map parseEntities = new ZXCollection();
        
        try {
			Node node;
			DBEntity objEntity;
			
			NodeList nodeList = pobjXMLNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				
				if (node instanceof org.w3c.dom.Element) {
					objEntity = new DBEntity();
					objEntity.parse((Element)node);
					
					parseEntities.put(objEntity.getName(), objEntity);
				}
			}
        	
        	return parseEntities;
        } catch (Exception e) {
            throw new NestableRuntimeException("Failed to : Parse Entities", e);
        }  
    }
    
	//------------------------ Object implemeted methods
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			DBAction objDBAction = (DBAction)this.getClass().newInstance();
			
			objDBAction.setName(getName());
			objDBAction.setComment(getComment());
			objDBAction.setActionType(getActionType());
			objDBAction.setLinkaction(getLinkaction());
				
			if (this.entities != null && this.entities.size() > 0) {
				objDBAction.setEntities((ZXCollection)((ZXCollection)this.entities).clone());
			}
			
			if (getQs() != null && getQs().size() > 0) {
				objDBAction.setQs(CloneUtil.clone((ArrayList)getQs()));
			}
			
			objDBAction.setEntityrefaction(getEntityrefaction());
				
	        return objDBAction;
		} catch (Exception e) {
			log.error("Failed to clone object", e);
			throw new NestableRuntimeException(e);
		}
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
		toString.append(DBACTION_ACTIONTYPE, zXType.valueOf(getActionType()));
		toString.append(DBACTION_COMMENT, getComment());
		toString.append(DBACTION_ENTITIES, getEntities());
		toString.append(DBACTION_ENTITYREFACTION, getEntityrefaction());
		toString.append(DBACTION_LINKACTION, getLinkaction());
		toString.append(DBACTION_NAME, getName());
		toString.append(DBACTION_QS, getQs());
		
		return toString.toString();
	}
}