/*
 * Created on Jun 9, 2005
 * $Id: DBDescriptor.java,v 1.1.2.12 2006/07/17 16:10:54 mike Exp $
 */
package org.zxframework.doc;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.zxframework.ZXCollection;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.DomElementUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * The document builder descriptor.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 10 March 2003
 * 
 * Change    : BD20OCT03
 * Why       : Added support for macros
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBDescriptor extends ZXObject {
	
	private static Log log = LogFactory.getLog(DBDescriptor.class);
	
	//------------------------ Members
	
	private String name;
	private String version;
	private String comment;
	private String templatedoc;
	private String copydoc;
	private String startaction;
	private String pkqs;
	private zXType.docBuilderEngineType enGine;
	private Map actions;
	private boolean useMacros;
	
	//------------------------ Runtime variable
	
	private XMLGen objXMLGen;
	private long lastModified;
	
	//------------------------ XML node names
	
	private static final String DBDESC_STARTTAG = "docbuilder";
	private static final String DBDESC_NAME = "name";
	private static final String DBDESC_VERSION = "version";
	private static final String DBDESC_COMMENT = "comment";
	private static final String DBDESC_TEMPLATEDOC = "templatedoc";
	private static final String DBDESC_COPYDOC = "copydoc";
	private static final String DBDESC_ENGINE = "engine";
	private static final String DBDESC_STARTACTION = "startaction";
	private static final String DBDESC_PKQS = "pkqs";
	private static final String DBDESC_ACTIONS = "actions";
	private static final String DBDESC_USEMACROS = "usemacros";
	
	//------------------------ Constructors
	
	/**
	 * Hide default constructor.
	 * 
	 * Except for cloning.
	 */
	private DBDescriptor() {
		super();
	}
	
	/**
	 * Preferred constructor.
	 * 
	 * @param pstrFile The full filename
	 */
	public DBDescriptor(String pstrFile) {
		if (pstrFile != null) {
			init(pstrFile);
		}
	}
	
	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the actions.
	 */
	public Map getActions() {
		return actions;
	}

	/**
	 * @param actions The actions to set.
	 */
	public void setActions(Map actions) {
		this.actions = actions;
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
	 * @return Returns the copydoc.
	 */
	public String getCopydoc() {
		return copydoc;
	}

	/**
	 * @param copydoc The copydoc to set.
	 */
	public void setCopydoc(String copydoc) {
		this.copydoc = copydoc;
	}

	/**
	 * @return Returns the engine.
	 */
	public zXType.docBuilderEngineType getEnGine() {
		return this.enGine;
	}

	/**
	 * @param engine The engine to set.
	 */
	public void setEnGine(zXType.docBuilderEngineType engine) {
		this.enGine = engine;
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
	 * @return Returns the pkqs.
	 */
	public String getPkqs() {
		return this.pkqs;
	}

	/**
	 * @param pkQS The pkQS to set.
	 */
	public void setPkqs(String pkQS) {
		this.pkqs = pkQS;
	}

	/**
	 * @return Returns the startAction.
	 */
	public String getStartaction() {
		return startaction;
	}

	/**
	 * @param startAction The startAction to set.
	 */
	public void setStartaction(String startAction) {
		this.startaction = startAction;
	}

	/**
	 * @return Returns the templatedoc.
	 */
	public String getTemplatedoc() {
		return templatedoc;
	}

	/**
	 * @param templatedoc The templatedoc to set.
	 */
	public void setTemplatedoc(String templatedoc) {
		this.templatedoc = templatedoc;
	}

	/**
	 * @return Returns the usemacros.
	 */
	public boolean isUseMacros() {
		return useMacros;
	}

	/**
	 * @param usemacros The usemacros to set.
	 */
	public void setUseMacros(boolean usemacros) {
		this.useMacros = usemacros;
	}

	/**
	 * @param usemacros The usemacros to set.
	 */
	public void setUsemacros(String usemacros) {
		this.useMacros = StringUtil.booleanValue(usemacros);
	}

	/**
	 * @return Returns the version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version The version to set.
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	//------------------------ Digester helper methods.
	
	/**
	 * @param engine The engine to set.
	 */
	public void setEngine(String engine) {
		if (engine == null) {
			this.enGine = zXType.docBuilderEngineType.dbetWord9;
		} else {
			this.enGine = zXType.docBuilderEngineType.getEnum(engine);
		}
	}
	
	//------------------------ Runtime Getters/Setters

	/**
	 * @return Returns the xMLGen.
	 */
	public XMLGen getXMLGen() {
		return objXMLGen;
	}

	/**
	 * @param gen The xMLGen to set.
	 */
	public void setXMLGen(XMLGen gen) {
		objXMLGen = gen;
	}
	
	/**
	 * The time when this descriptor was last modified.
	 * 
	 * @return Returns the lastModified.
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified The lastModified to set.
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	
	//------------------------ Parsing Code
	
	/**
	 * Init the DBDescriptor.
	 * 
	 * @param pstrFile The full filename of the xml descriptor.
	 * @return Returns the return code of the method.
	 */
	private zXType.rc init(String pstrFile) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrFile", pstrFile);
        }
        
        zXType.rc init = zXType.rc.rcOK;
        
        try {
        	File file = new File(pstrFile);
        	if (!file.exists() || !file.canRead()) {
        		throw new RuntimeException("Cannot read descriptor file :" + pstrFile);
        	}
        	
        	/**
        	 * Parsing code..
        	 */
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	factory.setValidating(false);
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			docBuilder.setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException ex) throws SAXException {
					throw ex;
				}
				public void fatalError(SAXParseException ex) throws SAXException {
					throw ex;
				}
				public void warning(SAXParseException ex) {
					log.warn("Ignored XML validation warning: " + ex.getMessage(), ex);
				}
			});
			
			Document doc = docBuilder.parse(file);
			
			/**
			 * Default value
			 */
			this.pkqs = "-pk";
			
			Element root = doc.getDocumentElement();
			DomElementUtil element;
			Node node;
			String nodeName;
			
			NodeList nodeList = root.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				
				if (node instanceof Element) {
					element = new DomElementUtil((Element)node);
					nodeName = node.getNodeName();
					
					if (DBDESC_NAME.equalsIgnoreCase(nodeName)) {
						setName(element.getText());
						
					} else if (DBDESC_VERSION.equalsIgnoreCase(nodeName)) {
						setVersion(element.getText());
						
					} else if (DBDESC_COMMENT.equalsIgnoreCase(nodeName)) {
						setComment(element.getText());
						
					} else if (DBDESC_TEMPLATEDOC.equalsIgnoreCase(nodeName)) {
						setTemplatedoc(element.getText());
						
					} else if (DBDESC_COPYDOC.equalsIgnoreCase(nodeName)) {
						setCopydoc(element.getText());
						
					} else if (DBDESC_ENGINE.equalsIgnoreCase(nodeName)) {
						setEngine(element.getText());
						
					} else if (DBDESC_STARTACTION.equalsIgnoreCase(nodeName)) {
						setStartaction(element.getText());
						
					} else if (DBDESC_PKQS.equalsIgnoreCase(nodeName)) {
						setPkqs(element.getText());
						
					} else if (DBDESC_ACTIONS.equalsIgnoreCase(nodeName)) {
						setActions(parseActions((Element)node));
						
					} else if (DBDESC_USEMACROS.equalsIgnoreCase(nodeName)) {
						setUsemacros(element.getText());
						
					}
					
				}
				
			}
			
        	return init;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Initialise the docbuilder descriptor.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pstrFile = "+ pstrFile);
            }
            throw new NestableRuntimeException(e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(init);
                getZx().trace.exitMethod();
            }
        }
        
	}
	
	/**
	 * Parse Docbuilder actions.
	 * 
	 * @param pobjXMLNode XML Node for the actions.
	 * @return Returns a map of actions.
	 */
	public Map parseActions(Element pobjXMLNode) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        
        Map parseActions = new ZXCollection();
		try {
			DBAction objAction;
			Node node;
			
			NodeList nodeList = pobjXMLNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				if (node instanceof Element) {
					objAction = DBAction.parseAction((Element)node);
					
					parseActions.put(objAction.getName(), objAction);
				}
			}
			
			return parseActions;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Parse Docbuilder Actions.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pobjXMLNode = "+ pobjXMLNode);
            }
            throw new NestableRuntimeException(e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(parseActions);
                getZx().trace.exitMethod();
            }
        }
	}
	
	//------------------------ Repository methods

	/**
	 * Dump value of current descriptor as XML.
	 * 
	 * @return Returns xml dump.
	 */
	public String dumpAsXML() {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        String dumpAsXML = "";
        
        try {
        	this.objXMLGen = new XMLGen(1000);  
            this.objXMLGen.xmlHeader();
        	
            this.objXMLGen.openTag(DBDESC_STARTTAG);
            
            this.objXMLGen.taggedValue(DBDESC_NAME, getName());
            this.objXMLGen.taggedValue(DBDESC_VERSION, getVersion());
            this.objXMLGen.taggedCData(DBDESC_COMMENT, getComment(), false);
            this.objXMLGen.taggedCData(DBDESC_TEMPLATEDOC, getTemplatedoc(), false);
            this.objXMLGen.taggedCData(DBDESC_COPYDOC, getCopydoc(), false);
            this.objXMLGen.taggedCData(DBDESC_PKQS, getPkqs(), false);
            this.objXMLGen.taggedCData(DBDESC_STARTACTION, getStartaction(), false);
        	this.objXMLGen.taggedValue(DBDESC_ENGINE, zXType.valueOf(getEnGine()));
            this.objXMLGen.taggedValue(DBDESC_USEMACROS, isUseMacros());
            
            this.objXMLGen.openTag(DBDESC_ACTIONS);
            DBAction objAction;
            Iterator iter = getActions().values().iterator();
            while (iter.hasNext()) {
            	objAction = (DBAction)iter.next();
            	
            	this.objXMLGen.openTag(DBAction.DBACTION_STARTTAG);
            	objAction.dumpAsXML(this.objXMLGen);
            	this.objXMLGen.closeTag(DBAction.DBACTION_STARTTAG);
            }
            this.objXMLGen.closeTag(DBDESC_ACTIONS);
            
            this.objXMLGen.closeTag(DBDESC_STARTTAG);
            
        	return this.objXMLGen.toString();
        	
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Dump XML.", e);
            dumpAsXML = this.objXMLGen.toString();
            return dumpAsXML.toString();
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(dumpAsXML);
                getZx().trace.exitMethod();
            }
        }
	}
	
	//------------------------ Object implemeted methods
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		DBDescriptor objDBDescriptor = new DBDescriptor();
		
		try {
			
			objDBDescriptor.setName(getName());
			objDBDescriptor.setVersion(getVersion());
			objDBDescriptor.setComment(getComment());
			objDBDescriptor.setTemplatedoc(getTemplatedoc());
			objDBDescriptor.setCopydoc(getCopydoc());
			objDBDescriptor.setStartaction(getStartaction());
			objDBDescriptor.setPkqs(getPkqs());
			objDBDescriptor.setEnGine(getEnGine());
			
			if (getActions() != null && getActions().size() > 0) {
				objDBDescriptor.setActions((ZXCollection)((ZXCollection)getActions()).clone());
			}
			
			objDBDescriptor.setUseMacros(isUseMacros());
			
        } catch (Exception e) {
            log.error("Failed to clone object", e);
        }
        
        return objDBDescriptor;
	}
}