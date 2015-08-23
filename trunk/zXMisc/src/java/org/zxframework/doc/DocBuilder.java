/*
 * Created on Jun 9, 2005
 * $Id$
 */
package org.zxframework.doc;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.zxframework.Tuple;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHandler;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringUtil;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * The document builder class.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 16 May 2003
 * 
 * Change    : BD25MAY03
 * Why       : Add action to buildDoc method so that you can specify
 *   		   the start action; this allow you to mic and match standard doc builder
 *   		   functionality with bespoke
 *
 * Change    : BD30JUN03
 * Why       : In runmode development do NOT print documents (instead show onnline)
 *             and thus do NOT close documents
 *
 * Change    : BD20OCT03
 * Why       : Added support for macros
 *
 * Change    : DGS19NOV2003
 * Why       : In printDoc in some circumstances it won't print if the doc is closed and
 *             set to nothing too quickly. Sleep for a second and all is ok.
 *
 * Change    : BD15JAN04
 * Why       : - Use background = false with printing in Word
 *             - Allow to have entity that we do not attempt to load
 *
 * Change    : DGS04FEB2004
 * Why       : New function 'textDoc' returns the document as plain text.
 *
 * Change    : BD12MAR04
 * Why       : Do not create BO in resolveEntities when already done
 *
 * Change    : DGS07MAY2004
 * Why       : In resolveEntities, use new ref method of 'context' to mean that the BO
 *             is already in the context so does not need to be created. Similarly in
 *             'loadEntities', does not need to be loaded.
 *
 * Change    : BD23FEB05 - V1.4:46
 * Why       : Work around restriction of Word that it can only handle find / replaces
 *             for strings up to 255 characters.
 *             Big thanks to Mike Naylor!!!!
 *
 * Change    : BD5APR05 - V1.5:1
 * Why       : Added support for data-sources
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DocBuilder extends ZXObject {
	
	private static Log log = LogFactory.getLog(DocBuilder.class);
	
	//------------------------ Members
	
	private DBDescriptor descriptor;
	
	private Document wordCopyDoc;
	private Document wordNewDoc;
	
	private Object wordLastUsedObject;
	private Map namedObjects;
	
	//------------------------  Constants for queries
	
    /** The sql select clause handle in the session object */
    public static final String QRYSELECTCLAUSE = "QrySelect";
    /** The sql where clause handle in the session object */
    public static final String QRYWHERECLAUSE = "QryWhere";
    /** The sql group by clause handle in the session object */
    public static final String QRYGROUPBYCLAUSE = "QryGroupBy";
    /** The sql order by clause handle in the session object */
    public static final String QRYORDERBYCLAUSE = "QryOrderBy";
    
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public DocBuilder() {
		super();
		
		this.namedObjects = new HashMap();
	}
	
	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the descriptor.
	 */
	public DBDescriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor The descriptor to set.
	 */
	public void setDescriptor(DBDescriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	/**
	 * @return Returns the namedObjects.
	 */
	public Map getNamedObjects() {
		return namedObjects;
	}
	
	/**
	 * @param namedObjects The namedObjects to set.
	 */
	public void setNamedObjects(Map namedObjects) {
		this.namedObjects = namedObjects;
	}
	
	/**
	 * @return Returns the wordCopyDoc.
	 */
	public Object getWordCopyDoc() {
		return wordCopyDoc;
	}
	
	/**
	 * @param wordCopyDoc The wordCopyDoc to set.
	 */
	public void setWordCopyDoc(Document wordCopyDoc) {
		this.wordCopyDoc = wordCopyDoc;
	}
	
	/**
	 * @return Returns the wordLastUsedObject.
	 */
	public Object getWordLastUsedObject() {
		return wordLastUsedObject;
	}
	
	/**
	 * @param wordLastUsedObject The wordLastUsedObject to set.
	 */
	public void setWordLastUsedObject(Object wordLastUsedObject) {
		this.wordLastUsedObject = wordLastUsedObject;
	}
	
	/**
	 * @return Returns the wordNewDoc.
	 */
	public Object getWordNewDoc() {
		return wordNewDoc;
	}
	
	/**
	 * @param wordNewDoc The wordNewDoc to set.
	 */
	public void setWordNewDoc(Document wordNewDoc) {
		this.wordNewDoc = wordNewDoc;
	}
	
	//------------------------ Public methods
	
	/**
	 * Start Doc Builder.
	 * 
	 * @param pstrDocTemplate The name of the doc builder descriptor.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc startDoc(String pstrDocTemplate){
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDocTemplate", pstrDocTemplate);
		}
		
		zXType.rc startDoc = zXType.rc.rcOK;
		
		try {
			
			/**
			 * Load and parse new descriptor
			 */
			String strFileName = getZx().fullPathName(
									getZx().getSettings().getTemplatesDir() 
								    + File.separatorChar + pstrDocTemplate 
								    + ".xml");
			
			/**
			 * Methods for retrieving the doc builder descriptor :
			 */
			if (getZx().getRunMode().pos == zXType.runMode.rmProduction.pos) {
				/**
				 * This has to be the most performant, so we do not care about 
				 * dynamic changes in the doc builder descriptor.
				 */
				DBDescriptor objDBDescCached = (DBDescriptor)getZx().getCachedValue(pstrDocTemplate);
				if (objDBDescCached != null) {
                    /** Create a clean copy of this pageflow */
			        synchronized (DocBuilder.class) {
			            this.descriptor = (DBDescriptor)objDBDescCached.clone();
			        }
			        
                } else {
                    /** Parse From afresh * */
                    this.descriptor = new DBDescriptor(strFileName);
                    
                    getZx().setCachedValue(pstrDocTemplate, this.descriptor);
                    
                }
				
            } else if (getZx().getRunMode().pos == zXType.runMode.rmDevelCache.pos) {
                /**
                 * Check whether the file has been modified recently.
                 */
            	DBDescriptor objDBDescCached = (DBDescriptor)getZx().getCachedValue(pstrDocTemplate);
                File file = new File(strFileName);
                long lastModified = file.lastModified();
                if (objDBDescCached != null 
                	&& lastModified == objDBDescCached.getLastModified()) {
                    /** Create a clean copy of this pageflow */
			        synchronized (DocBuilder.class) {
			            this.descriptor = (DBDescriptor)objDBDescCached.clone();
			        }
                } else {
                    /** 
                     * Parse From afresh 
                     **/
                    this.descriptor = new DBDescriptor(strFileName);
                    
                    // Make a seperate copy of PFDescriptor.
                    DBDescriptor objPFDesc = (DBDescriptor)this.descriptor.clone();
                    objPFDesc.setLastModified(lastModified);
                    
                    getZx().setCachedValue(pstrDocTemplate, objPFDesc);
                    
                }
                
            } else {
                /**
                 * Developer mode etc.. - when we are not in production mode the
                 * framework will be sensitive to changes in the doc builder xml
                 * file allow developers rad development.
                 * 
                 * Now we can initialise the descriptor
                 */
                this.descriptor = new DBDescriptor(strFileName);
                
            }
			
			/**
			 * Based on the engine type: start support objects
			 */
			if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
				/**
				 * Activate Word.
				 */
				getZx().word().activate();
				
				/**
				 * Open copy document if applicable
				 */
				String strCopyDoc = getZx().resolveDirector(getDescriptor().getCopydoc());
				if (StringUtil.len(strCopyDoc) > 0) {
					strCopyDoc = getZx().fullPathName(getZx().getSettings().getTemplatesDir()
													  + File.separator + strCopyDoc);
					
					this.wordCopyDoc = new OpenOfficeDocument();
					wordCopyDoc.openDoc(strCopyDoc);
					
					// Original VB Code
					// Set wordCopyDoc = .wordApp.Documents.Add(strCopyDoc)
				}
			}
			
			return startDoc;
		} catch (Exception e) {
            getZx().trace.addError("Failed to : Start Doc Builder.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pstrDocTemplate = "+ pstrDocTemplate);
            }
            throw new NestableRuntimeException(e);
		} finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(startDoc);
                getZx().trace.exitMethod();
            }
		}
	}
	
	/**
	 * Build document.
	 *
	 * @param pstrPk Optional, default should be null. 
	 * @param pstrAction Optional, default should be null. 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if buildDoc fails. 
	 */
	public zXType.rc buildDoc(String pstrPk, String pstrAction) throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrPk", pstrPk);
			getZx().trace.traceParam("pstrAction", pstrAction);
		}
		
		zXType.rc buildDoc = zXType.rc.rcOK; 

		try {
			/**
			 * Store pk in quick context
			 */
			if (StringUtil.len(pstrPk) > 0) {
				getZx().getQuickContext().setEntry(getZx().resolveDirector(getDescriptor().getPkqs()), 
												   pstrPk);
			}
			
			/**
			 * Open template document
			 */
			String strDoc = getZx().resolveDirector(getDescriptor().getTemplatedoc());
			
			if (StringUtil.len(strDoc) > 0) {
				strDoc = getZx().fullPathName(getZx().getSettings().getTemplatesDir() + File.separator + strDoc);
			}
			
			boolean blnProtectedForm = false;
			
			if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
				getZx().word().newDoc(strDoc);
				this.wordNewDoc = getZx().word();
				
				// TODO : Openoffice..
				// blnProtectedForm = (wordNewDoc.ProtectionType = wdAllowOnlyFormFields)
				// If blnProtectedForm Then
	            //	wordNewDoc.Unprotect
				// End If
			}
			
			if (StringUtil.len(pstrAction) == 0) {
				buildDoc = processActions(getDescriptor().getStartaction());
			} else {
				buildDoc = processActions(pstrAction);
			}
			
			if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
				if (blnProtectedForm) {
					// TODO : Openoffice..
					// wordNewDoc.Protect wdAllowOnlyFormFields
				}
			}
			
			return buildDoc;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Build document", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrPk = " + pstrPk);
				log.error("Parameter : pstrAction = " + pstrAction);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			buildDoc = zXType.rc.rcError;
			return buildDoc;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(buildDoc);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Process actions.
	 *
	 * @param pstrStartAction The start action.
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if processActions fails. 
	 */
	protected zXType.rc processActions(String pstrStartAction) throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrStartAction", pstrStartAction);
		}
		
		zXType.rc processActions = zXType.rc.rcOK; 
		
		try {
			DBAction objAction;
			
			String strAction = getZx().resolveDirector(pstrStartAction);
			
			while (StringUtil.len(strAction) > 0) {
				objAction = (DBAction)getDescriptor().getActions().get(strAction);
				if (objAction == null) {
					throw new ZXException("Unable to retrieve action", strAction);
				}
				
				int intRC = objAction.go(this).pos;
				if (intRC == zXType.rc.rcOK.pos) {
					// Ignored
				} else if (intRC == zXType.rc.rcWarning.pos) {
					// Ignored
				} else if (intRC == zXType.rc.rcError.pos) {
					throw new ZXException("Failed to process action");
				}
				
				handleQS(objAction);
				
				strAction = getZx().resolveDirector(objAction.getLinkaction());
			}
			
			return processActions;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Process actions.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrStartAction = " + pstrStartAction);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			processActions = zXType.rc.rcError;
			return processActions;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(processActions);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Execute command. 
	 * 
	 * Execute a command that is specific to the document builder engine.
	 *
	 * @param pstrCommand The command to execute.
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if executeCommand fails. 
	 */
	public zXType.rc executeCommand(String pstrCommand) throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrCommand", pstrCommand);
		}
		
		zXType.rc executeCommand = zXType.rc.rcOK; 

		try {
			
			if ("activatedoc".equalsIgnoreCase(pstrCommand)) {
				if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
					getZx().word().giveUserFocus();
				}
			}
			
			return executeCommand;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Execute command.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrCommand = " + pstrCommand);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			executeCommand = zXType.rc.rcError;
			return executeCommand;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(executeCommand);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Resolve the entities for a given action.
	 *
	 * @param pobjAction The action
	 * @return Returns a collection of entities related to this action.
	 * @throws ZXException Thrown if resolveEntities fails. 
	 */
	protected Map resolveEntities(DBAction pobjAction) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjAction", pobjAction);
		}

		Map resolveEntities = null; 
		
		try {
			DBAction objAction;
			
			/**
			 * See if there is a ref-action
			 */
			String strRefAction = getZx().resolveDirector(pobjAction.getEntityrefaction());
			
			if (StringUtil.len(strRefAction) > 0) {
				objAction = (DBAction)getDescriptor().getActions().get(strRefAction);
				if (objAction == null) {
					throw new ZXException("Unable to retrieve action", strRefAction);
				}
				
				resolveEntities = resolveEntities(objAction);
				
				if (resolveEntities == null) {
					throw new ZXException("Failed to resolve entities");
				}
				
			} else {
				/**
				 * No ref action
				 */
				resolveEntities = new ZXCollection();
				DBEntity objEntity;
				Iterator iter = pobjAction.getEntities().values().iterator();
				while (iter.hasNext()) {
					objEntity = (DBEntity)iter.next();
					
					/**
					 * Only create new object when not already done
					 */
					if (objEntity.getBo() == null) {
						String strEntity = getZx().resolveDirector(objEntity.getEntity());
						
		                if (StringUtil.len(strEntity) > 0) {
			                String strAlias = getZx().resolveDirector(objEntity.getAlias());
			                
			                /**
			                 * If BO already in context, no need to create etc.
			                 */
			                if (objEntity.getRefMethod().equals(zXType.docBuilderEntityRefMethod.dbermContext)) {
			                	objEntity.setBo(getZx().getBOContext().getEntry(objEntity.getName()));
			                } else {
			                	objEntity.setBo(getZx().createBO(strEntity, StringUtil.len(strAlias) > 0));
			                }
			                
			                if (objEntity.getBo() == null) {
			                	throw new ZXException("Failed to get business object", strEntity);
			                }
			                
			                objEntity.getBo().getDescriptor().setAlias(strAlias);
			                
			                /**
			                 * And get data-source handler
			                 */
			                objEntity.setDsHandler(objEntity.getBo().getDS());
			                if (objEntity.getDsHandler() == null) {
			                	throw new ZXException("Unable to retrieve data-source handler for entity", 
			                						  objEntity.getName());
			                }
			                
		                } // len(strentity) = 0
						
					} // BO is nothing
					
					if (objEntity.getBo() != null) {
						resolveEntities.put(objEntity.getName(),  objEntity);
		                getZx().getBOContext().setEntry(objEntity.getName(), objEntity.getBo());
					}
					
				} // Loop through entities
				
			} // ref action
			
			return resolveEntities;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Resolve the entities for a given action.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pobjAction = "+ pobjAction);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return resolveEntities;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(resolveEntities);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Load entities from database.
	 *
	 * @param pcolEntities Collection of entities 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if loadEntities fails. 
	 */
	protected zXType.rc loadEntities(Map pcolEntities) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pcolEntities", pcolEntities);
		}

		zXType.rc loadEntities = zXType.rc.rcOK; 
		
		try {
			DBEntity objEntity;
			
			Iterator iter = pcolEntities.values().iterator();
			while (iter.hasNext()) {
				objEntity = (DBEntity)iter.next();
				
				handleAttrValues(objEntity);
				
				if (objEntity.getRefMethod().equals(zXType.docBuilderEntityRefMethod.dbermPK)) {
					String strPK = getZx().resolveDirector(objEntity.getPk());
					
					/**
					 * Do not load unless we have a PK value
					 */
					if (StringUtil.len(strPK) > 0) {
						objEntity.getBo().setPKValue(strPK);
						
						if (objEntity.getBo().loadBO(getZx().resolveDirector(objEntity.getLoadgroup()), 
													 "+", 
													 false).pos == zXType.rc.rcError.pos) {
							throw new ZXException("Failed to loadBO");
						}
					}
					
				} else if (objEntity.getRefMethod().equals(zXType.docBuilderEntityRefMethod.dbermPKGroup)) {
					String strGroup = getZx().resolveDirector(objEntity.getPkwheregroup());
					
					if (objEntity.getBo().loadBO(getZx().resolveDirector(objEntity.getLoadgroup()), 
												 strGroup, 
												 false).pos == zXType.rc.rcError.pos) {
						throw new ZXException("Failed to loadBO");
					}
					
				}
				/**
				 * If neither dbermPK nor dbermPKGroup, will be dbermContext and thus will not
				 * need loading as is already loaded and in the context
				 */
				
			}
			
			return loadEntities;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Load entities from database.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pcolEntities = " + pcolEntities);
			}
			if (getZx().throwException) throw new ZXException(e);
			loadEntities = zXType.rc.rcError;
			return loadEntities;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(loadEntities);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Process attribute values.
	 *
	 * @param pobjEntity The doc builder entity 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if handleAttrValues fails. 
	 */
	protected zXType.rc handleAttrValues(DBEntity pobjEntity) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjEntity", pobjEntity);
		}

		zXType.rc handleAttrValues = zXType.rc.rcOK; 
		
		try {
			/**
			 * Exit early if null
			 */
			if (pobjEntity.getAttrvalues() == null) {
				return handleAttrValues;
			}
			
			Tuple objTuple;
			
			int intAttrValues = pobjEntity.getAttrvalues().size();
			for (int i = 0; i < intAttrValues; i++) {
				objTuple = (Tuple)pobjEntity.getAttrvalues().get(i);
				
				pobjEntity.getBo().setValue(getZx().resolveDirector(objTuple.getValue()),
											new StringProperty(getZx().resolveDirector(objTuple.getName())));
			}
			
			return handleAttrValues;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Process attribute values.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pobjEntity = " + pobjEntity);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			handleAttrValues = zXType.rc.rcError;
			return handleAttrValues;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(handleAttrValues);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Handle the QS entries.
	 *
	 * @param pobjAction The doc builder action 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if handleQS fails. 
	 */
	protected zXType.rc handleQS(DBAction pobjAction) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjAction", pobjAction);
		}
		
		zXType.rc handleQS = zXType.rc.rcOK; 

		try {
			/**
			 * Exit early if null
			 */
			if (pobjAction.getQs() == null) {
				return handleQS;
			}
			
			Tuple objTuple;
			
			int intQS = pobjAction.getQs().size();
			for (int i = 0; i < intQS; i++) {
				objTuple = (Tuple)pobjAction.getQs().get(i);
				
				/**
				 * Looks like it is the wrong way around but this is the
				 * good old source / destination thing....
				 */
				getZx().getQuickContext().setEntry(getZx().resolveDirector(objTuple.getValue()),
												   getZx().resolveDirector(objTuple.getName()));
			}
			
			return handleQS;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Handle the QS entries.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pobjAction = " + pobjAction);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			handleQS = zXType.rc.rcError;
			return handleQS;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(handleQS);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Get object from document and store in appropriate property of refObject.
	 *
	 * @param pobjRefObject The object to store it in. 
	 * @param pblnInCopyDoc Whether in Copy Doc. Optional, default is false.
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if getObject fails. 
	 */
	public zXType.rc getObject(DBObject pobjRefObject, 
							   boolean pblnInCopyDoc) throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjRefObject", pobjRefObject);
			getZx().trace.traceParam("pblnInCopyDoc", pblnInCopyDoc);
		}

		zXType.rc getObject = zXType.rc.rcOK; 
		
		try {
			String strClue;
			Object objWordObject = null;
			String strRef = "";
			if (pblnInCopyDoc) {
				strClue = "In copydoc";
			} else {
				strClue = "In newdoc";
			}
			
			
			if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
				OpenOfficeDocument document = (OpenOfficeDocument)(pblnInCopyDoc?wordCopyDoc:wordNewDoc);
				
				zXType.docBuilderObjectrefMethod enmRefMethod = pobjRefObject.getRefMethod();
				if (enmRefMethod.equals(zXType.docBuilderObjectrefMethod.dbormByName)) {
					/**
					 * Retrieve from collection of objects
					 */
					strClue = strClue + ",by name";
					
		            strRef = getZx().resolveDirector(pobjRefObject.getRef()).toLowerCase();
		            
		            objWordObject = this.namedObjects.get(strRef);
		            
				} else if (enmRefMethod.equals(zXType.docBuilderObjectrefMethod.dbormByNumber)) {
					/**
					 * Get table number X from section / pagepart
					 */
					strClue = strClue + ",by number";
					
		            strRef = getZx().resolveDirector(pobjRefObject.getRef());
		            
		            if (!StringUtil.isNumeric(strRef)) {
		            	throw new ZXException("Reference must be numeric when ref-method is 'by number'", 
		            						  strRef);
		            }
		            
		            Object objRange = wordGetRange(pobjRefObject, pblnInCopyDoc);
		            
		            objWordObject = document.getTable(objRange,
		            								  Integer.parseInt(strRef));
		            
				} else if (enmRefMethod.equals(zXType.docBuilderObjectrefMethod.dbormLast)) {
					/**
					 * Get last table in section / pagepart
					 */
					strClue = strClue + ",last";
					
		            Object objRange = wordGetRange(pobjRefObject, pblnInCopyDoc);
		            if (objRange == null) {
		            	throw new ZXException("Failed to get word range");
		            }
		            
		            objWordObject = document.getTable(objRange,
		            								  document.getTableCount(objRange));
		            
				} else if (enmRefMethod.equals(zXType.docBuilderObjectrefMethod.dbormLastUsed)) {
					/**
					 * Use the one that we refered to most recently
					 */
					strClue = strClue + ",last used";
					
					objWordObject = this.wordLastUsedObject;
					
				}
			}
			
			if (StringUtil.len(strRef) > 0) {
				strClue = strClue + ", ref: " + strRef;
			}
			
			if (objWordObject == null) {
				throw new ZXException("Unable to find object " + strClue);
			}
			
			/**
			 * Save it as last used ?
			 */
			if (pobjRefObject.isSaveAsLastused()) {
				this.wordLastUsedObject = objWordObject;
			}
			
			/**
			 * Store it for referal later?
			 */
			String strName = getZx().resolveDirector(pobjRefObject.getSaveas());
			if (StringUtil.len(strName) > 0) {
				strName = strName.toLowerCase();
				this.namedObjects.remove(strName);
				this.namedObjects.put(strName, objWordObject);
			}
			
			/**
			 * And store for use by calling application
			 */
			pobjRefObject.setWordobject(objWordObject);
			
			return getObject;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Get object from document", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pobjRefObject = " + pobjRefObject);
				log.error("Parameter : pblnInCopyDoc = " + pblnInCopyDoc);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			getObject = zXType.rc.rcError;
			return getObject;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(getObject);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Get header / footer / body from a particular section.
	 *
	 * TODO : Still need to code this correctly.
	 * 
	 * @param pobjRefObject The ref object.  
	 * @param pblnInCopyDoc Optional, default should be false 
	 * @return Returns the word range 
	 * @throws ZXException Thrown if wordGetRange fails. 
	 */
	public Object wordGetRange(DBObject pobjRefObject, boolean pblnInCopyDoc) throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjRefObject", pobjRefObject);
			getZx().trace.traceParam("pblnInCopyDoc", pblnInCopyDoc);
		}
		
		Object wordGetRange = null; 

		try {
			OpenOfficeDocument objRange;
			Object objSection;
			
			if (pblnInCopyDoc) {
				objRange = (OpenOfficeDocument)this.wordCopyDoc;
			} else {
				objRange = (OpenOfficeDocument)this.wordNewDoc;
			}
			
			if (objRange == null) {
				throw new ZXException("Unable to get range from document");
			}
			
			/**
			 * Get the section.
			 */
			String strSection = getZx().resolveDirector(pobjRefObject.getSection());
			if (StringUtil.len(strSection) > 0) {
				/**
				 * Assumes : There has to be section defined in the text document.
				 */
				if (!StringUtil.isNumeric(strSection)) {
					throw new ZXException("Section must be numeric");
				}
				
				objSection = objRange.getSection(Integer.parseInt(strSection));
				
			} else {
				objSection = objRange.getSection(1);
				
				/**
				 * There are sections defined in this document.
				 * 
				 * NOTE : By default in openoffice this is NOT true.
				 */
				if (objSection == null) {
					objSection = objRange.getTextRange();
				}
				
			}
			
			// TODO : Openoffice, code this.
			/**
			 * Get the range.
			 */
			if (pobjRefObject.getPagePart().equals(zXType.docBuilderPagePart.dbppBody)) {
				// Set wordGetRange = objSection.Range
				wordGetRange = objSection;
				
			} else if (pobjRefObject.getPagePart().equals(zXType.docBuilderPagePart.dbppFooter)) {
				// Set wordGetRange = objSection.Footers(Word.wdHeaderFooterPrimary).Range
				
			} else if (pobjRefObject.getPagePart().equals(zXType.docBuilderPagePart.dbppHeader)) {
				// Set wordGetRange = objSection.Headers(Word.wdHeaderFooterPrimary).Range
				
			}
			
			return wordGetRange;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Get header / footer / body from a particular section.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pobjRefObject = "+ pobjRefObject);
				log.error("Parameter : pblnInCopyDoc = "+ pblnInCopyDoc);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return wordGetRange;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(wordGetRange);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Store an entry in the context.
	 *
	 * @param pstrName The context name 
	 * @param pstrValue The context value 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if setInContext fails. 
	 */
	public zXType.rc setInContext(String pstrName, String pstrValue) throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrName", pstrName);
			getZx().trace.traceParam("pstrValue", pstrValue);
		}
		
		zXType.rc setInContext = zXType.rc.rcOK; 

		try {
			
			getZx().getSession().addToContext(getDescriptor().getName() + "." + pstrName, pstrValue);
			
			return setInContext;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Store an entry in the context.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrName = "+ pstrName);
				log.error("Parameter : pstrValue = "+ pstrValue);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			setInContext = zXType.rc.rcError;
			return setInContext;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(setInContext);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Retrieve something from context.
	 *
	 * @param pstrName The name of context value, 
	 * @return Returns the context value
	 * @throws ZXException Thrown if getFromContext fails. 
	 */
	public String getFromContext(String pstrName) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrName", pstrName);
		}
		
		String getFromContext = ""; 

		try {
			
			getFromContext = getZx().getSession().getFromContext(getDescriptor().getName() + "." + pstrName);
			
			return getFromContext;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Retrieve something from context.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrName = "+ pstrName);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return getFromContext;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(getFromContext);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Construct query from components in context.
	 *
	 * @param pstrName The query name 
	 * @return Returns the sql query from context.
	 * @throws ZXException Thrown if constructQuery fails. 
	 */
	public String constructQuery(String pstrName) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrName", pstrName);
		}

		String constructQuery = "";
		
		try {
			
			String strSelectClause = getFromContext(pstrName + QRYSELECTCLAUSE);
			String strWhereClause = getFromContext(pstrName + QRYWHERECLAUSE);
			String strOrderByClause = getFromContext(pstrName + QRYORDERBYCLAUSE);
			
			constructQuery = strSelectClause;
			if (strWhereClause != null) {
				/**
				 * QueryDef generates Where clauses with the preceding AND,
				 * whereas normal pageflow usually doesn't. Therefore, to avoid confusion
				 * when trying to use the same query in and out of pageflows, handle the
				 * situation here by not adding another AND when one exists at the start.
				 */
				if (!strWhereClause.toUpperCase().trim().startsWith("AND")) {
					constructQuery = constructQuery + "AND";
				}
				constructQuery = constructQuery + " " + strWhereClause;
 			}
			
			if (strOrderByClause != null) {
				/**
				 * ORDER BY is similar to AND above.
				 */
				if (!strOrderByClause.toUpperCase().trim().endsWith("ORDER BY")) {
					constructQuery = constructQuery + " ORDER BY";
				}
				
				constructQuery = constructQuery + " " + strOrderByClause;
			}
			
			return constructQuery;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Construct query from components in context.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrName = " + pstrName);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return constructQuery;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(constructQuery);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Does the active director indicate active.
	 *
	 * @param pstrDirector The director to determine if it is active. 
	 * @return Returns whether the director is true.
	 * @throws ZXException Thrown if isActive fails. 
	 */
	public boolean isActive(String pstrDirector) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrDirector", pstrDirector);
		}
		
		boolean isActive = false; 

		try {
			
			String strTmp = getZx().resolveDirector(pstrDirector);
			
			if (StringUtil.len(strTmp) == 0) {
				isActive = true;
			} else {
				isActive = StringUtil.booleanValue(strTmp);
			}
			
			return isActive;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Does the active director indicate active.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrDirector = " + pstrDirector);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return isActive;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(isActive);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Save doc.
	 *
	 * <pre>
	 *  Assumes   :
	 *    buildDoc has been successfully called
	 * </pre>
	 *
	 * @param pstrName The filename 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if saveDoc fails. 
	 */
	public zXType.rc saveDoc(String pstrName) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrName", pstrName);
		}
		
		zXType.rc saveDoc = zXType.rc.rcOK; 

		try {
			
			if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
				if (getZx().word().wordDoc() == null) {
					throw new ZXException("No active document available");
				}
				
				saveDoc = getZx().word().saveDocAs(pstrName);
			}
			
			return saveDoc;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Save doc.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrName = " + pstrName);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			saveDoc = zXType.rc.rcError;
			return saveDoc;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(saveDoc);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Print Doc.
	 *
	 * <pre>
	 * Assumes   :
	 *    buildDoc has been successfully called
	 * </pre>
	 * 
	 * @param pstrPrinter The printer name. 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if printDoc fails. 
	 */
	public zXType.rc printDoc(String pstrPrinter) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrPrinter", pstrPrinter);
		}

		zXType.rc printDoc = zXType.rc.rcOK; 
		
		try {
			
			if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
				if (getZx().word().wordDoc() == null) {
					throw new ZXException("No active document available");
				}
				
				/**
				 * In development mode, do not print but show doc instead
				 */
				if (getZx().getRunMode().pos == zXType.runMode.rmDevelopment.pos
				    || getZx().getRunMode().pos == zXType.runMode.rmDevelCache.pos) {
					getZx().word().giveUserFocus();
					
				} else {
					printDoc = getZx().word().setPrinter(pstrPrinter);
					
					/**
					 * Use background = false to make sure that we never delete Word
					 * before we are done printing
					 */
					// TODO : Openoffice
					// getZx().word().wordDoc().PrintOut False
					
					/**
					 * DGS19NOV2003: In some circumstances it won't print if the doc is closed and
					 * set to nothing too quickly. Sleep for a second and all is ok.
					 * BD15JAN04: no longer required now we use backGround=false
					 */
					// Sleep 1000
				}
				
			}
			
			return printDoc;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Print Doc.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pstrPrinter = " + pstrPrinter);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			printDoc = zXType.rc.rcError;
			return printDoc;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(printDoc);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Close doc.
	 *
	 * <pre>
	 * Assumes   :
	 *  buildDoc has been successfully called
	 * </pre>
	 * 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if closeDoc fails. 
	 */
	public zXType.rc closeDoc() throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
		}

		zXType.rc closeDoc = zXType.rc.rcOK; 
		
		try {
			
			if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
				if (getZx().word().wordDoc() == null) {
					throw new ZXException("No active document available");
				}
				
				/**
				 * Do NOT close document when in development mode as we have
				 * given the user focus
				 */
				if (getZx().getRunMode().pos != zXType.runMode.rmDevelopment.pos
					|| getZx().getRunMode().pos != zXType.runMode.rmDevelCache.pos) {
					closeDoc = getZx().word().closeDoc(false);
				}
				
			}
			
			return closeDoc;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Close doc.", e);
			
			if (getZx().throwException) throw new ZXException(e);
			closeDoc = zXType.rc.rcError;
			return closeDoc;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(closeDoc);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Returns the document as plain text.
	 * 
	 * <pre>
	 *  Assumes   :
	 *     buildDoc has been successfully called
	 * </pre>
	 * 
	 * @return Returns the document as plain text.
	 * @throws ZXException Thrown if textDoc fails. 
	 */
	public String textDoc() throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
		}
		
		String textDoc = ""; 

		try {
			
			if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
				if (getZx().word().wordDoc() == null) {
					throw new ZXException("No active document available");
				}
				
				// TODO : Openoffice
			}
			
			return textDoc;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Returns the document as plain text.", e);
			
			if (getZx().throwException) throw new ZXException(e);
			return textDoc;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(textDoc);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Set cell of object.
	 * 
	 * Optionally use macro
	 * 
	 * @param pobjDocObject The doc object. 
	 * @param pintRow The row number 
	 * @param pintCol The column number. 
	 * @param pstrValue The value to set 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if setCell fails. 
	 */
	public zXType.rc setCell(DBObject pobjDocObject, 
							 int pintRow, 
							 int pintCol, 
							 String pstrValue) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjDocObject", pobjDocObject);
			getZx().trace.traceParam("pintRow", pintRow);
			getZx().trace.traceParam("pintCol", pintCol);
			getZx().trace.traceParam("pstrValue", pstrValue);
		}

		zXType.rc setCell = zXType.rc.rcOK; 
		
		try {
			
			if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
				if (getZx().word().wordDoc() == null) {
					throw new ZXException("No active document available");
				}
				
				if (getDescriptor().isUseMacros()) {
					// TODO : Openoffice
					// zX.Word.wordApp.Run "setCell", pobjDocObject.wordObject, pintRow, pintCol, pstrValue
					
				} else {
					OpenOfficeDocument.setCell(pobjDocObject.getWordobject(), pintRow, pintCol, pstrValue);
					
				}
				
			}
			
			return setCell;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Set cell of object.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pobjDocObject = "+ pobjDocObject);
				log.error("Parameter : pintRow = "+ pintRow);
				log.error("Parameter : pintCol = "+ pintCol);
				log.error("Parameter : pstrValue = "+ pstrValue);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			setCell = zXType.rc.rcError;
			return setCell;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(setCell);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Add row to object.
	 * 
	 * Optionally use macro
	 * 
	 * @param pobjDocObject The doc object 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if addRow fails. 
	 */
	public zXType.rc addRow(DBObject pobjDocObject) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjDocObject", pobjDocObject);
		}
		
		zXType.rc addRow = zXType.rc.rcOK; 

		try {
			
			if (getDescriptor().getEnGine().equals(zXType.docBuilderEngineType.dbetWord9)) {
				if (getZx().word().wordDoc() == null) {
					throw new ZXException("No active document available");
				}
				
				if (getDescriptor().isUseMacros()) {
					// TODO : Openoffice
					// zX.Word.wordApp.Run "addRow", pobjDocObject.wordObject
				} else {
					// pobjDocObject.wordObject.Rows.Add
				}
			}
			
			return addRow;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Add row to object.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pobjDocObject = " + pobjDocObject);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			addRow = zXType.rc.rcError;
			return addRow;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(addRow);
				getZx().trace.exitMethod();
			}
		}
	}
	
	// TODO : Openoffice : rangeReplace
	
	/**
	 * Validate Datasource entities.
	 * 
	 * There are certain restriction what you can do
	 * with different data sources. This routine checks
	 * for the biggest known poblems
	 * 
	 * @param pcolEntities Collection of entities 
	 * @return Returns whether datasources are valid.
	 * @throws ZXException Thrown if validDataSourceEntities fails. 
	 */
	public boolean validDataSourceEntities(Map pcolEntities) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pcolEntities", pcolEntities);
		}
		
		boolean validDataSourceEntities = false; 

		try {		    
			DBEntity objEntity;
	        DSHandler objDSHandler = null;
	        
	        /**
	         * No entities does not impose any restrictions when it comes to different data sources
	         */
	        int intEntities = pcolEntities.size();
	        if (intEntities == 0) return validDataSourceEntities;
	        boolean blnFirst = true;
	        
	        Iterator iter = pcolEntities.values().iterator();
	        while (iter.hasNext()) {
	            objEntity = (DBEntity)iter.next();
	            
	            if (blnFirst) {
	                /**
	                 * Take the data-source type from the first data-source; all other entities must have
	                 * same type
	                 */
	                objDSHandler = objEntity.getDsHandler();
	                blnFirst = false;
	                
	            } else {
	                /**
	                 * Must be same type as first
	                 * Note that we do not raise an error (ie goto errExit) as we do not want an error
	                 * message to be generated
	                 */
	                if (objDSHandler != objEntity.getDsHandler()) {
	                    validDataSourceEntities = false;
	                    return validDataSourceEntities;
	                }
	                
	            } // First iteration?
	            
	        } // Loop over entities
	        
	        /**
	         * Another restriction: if we have multiple entities, they cannot obe of type channel
	         */
	        if (objDSHandler== null || (intEntities > 1 && objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos)) {
	            validDataSourceEntities = false;
	            return validDataSourceEntities;
	        }
	        
	        validDataSourceEntities = true;
        
	        return validDataSourceEntities;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Validate Datasource entities.", e);
			if (log.isErrorEnabled()) {
				log.error("Parameter : pcolEntities = "+ pcolEntities);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return validDataSourceEntities;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(validDataSourceEntities);
				getZx().trace.exitMethod();
			}
		}
	}
}