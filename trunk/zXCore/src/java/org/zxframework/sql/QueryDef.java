/*
 * Created on Apr 5, 2004 by Michael Brewer
 * $Id: QueryDef.java,v 1.1.2.13 2006/07/17 16:40:06 mike Exp $
 */
package org.zxframework.sql;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.digester.Digester;

import org.zxframework.Descriptor;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;

import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSRS;
import org.zxframework.exception.ParsingException;

import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Query definition class.
 * 
 * <pre>
 * 
 * This performs the parsing of the Query definition xml files.
 * 
 * Change    : BD28MAY03
 * Why       : Removed context; now uses zx BO and quick context
 * 
 * Change    : DGS21AUG2003
 * What      : Added comments and last change
 * 
 * Change    : BD03DEC03
 * What      : Initialise components collection
 * 
 * Change    : BD9JUN04
 * What      : Use CDATA for some key fields in XML
 * 
 * Change    : BD10JUN04
 * What      : Allow entity to be resolved without getting the group
 * 
 * Change    : BD4AUG04
 * What      : Take into consideration any aliases
 *  
 * Change    : BD29MAR05 - V1.5:1
 * What      : Support for data-sources
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class QueryDef extends ZXObject {

    //------------------------ Members
    
    private String name;
    private String version;
    private String lastChange;
    private String comment;
    private ArrayList components;
    
    private Digester digester;
    
    private DSHRdbms DSHandler;
    
    //------------------------ Constructor

    /**
     * Default constructor.
     */
    public QueryDef() {
        super();
        
        /** Init the collection of components. **/
        //this.components = new ArrayList();
    }
    
    //------------------------ Getters and Setters
    
    /**
     * Handle to relevant DSHandler. 
     * 
     * Note that we assume two things: all entities share same DSHandler (is safe assumption as
     * otherwise we cannot join) and DSHandler is not a channel (also safe
     * assumption as otherwise SQL is irrelevant)
     * 
     * @return Returns the dSHandler.
     */
    public DSHRdbms getDSHandler() {
        return DSHandler;
    }
    
    /**
     * @param handler The dSHandler to set.
     */
    public void setDSHandler(DSHRdbms handler) {
        DSHandler = handler;
    }
    
    /**
     * A developers comment for the query definition.
     * 
     * @return Returns the comment.
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @see QueryDef#getComment()
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * A collection (QSComponent) of components.
     * 
     * @return Returns the components.
     */
    public ArrayList getComponents() {
        return components;
    }
    
    /**
     * @see QueryDef#getComponents()
     * @param components The components to set.
     */
    public void setComponents(ArrayList components) {
        this.components = components;
    }
    
    /**
     * The last changed date of the descriptor. 
     * 
     * @return Returns the lastChange.
     */
    public String getLastChange() {
        return lastChange;
    }
    
    /**
     * @see QueryDef#getLastChange()
     * @param lastChange The lastChange to set.
     */
    public void setLastChange(String lastChange) {
        this.lastChange = lastChange;
    }
    
    /**
     * The name of the Query Definition.
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @see QueryDef#getName()
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * The version of the Query definition used by developers.
     * 
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * @see QueryDef#getVersion()
     * @param version The version to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    //------------------------ Public methods
    
    /**
     * Initialise the query def object. 
     * 
     * If the name is given, also load the querydef.
     *
     * @param pstrName Name of QueryDef 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if init fails. 
     */
     public zXType.rc init(String pstrName) throws ZXException{
         if(getZx().trace.isFrameworkCoreTraceEnabled()) {
             getZx().trace.enterMethod();
             getZx().trace.traceParam("pstrName", pstrName);
         }
         
         zXType.rc init = zXType.rc.rcOK; 
         
         try {    
             
             if (StringUtil.len(pstrName) != 0) {
                 String strFileName = getZx().fullPathName(getZx().getQueryDir())  + File.separatorChar + pstrName + ".xml";
                 
                 if (load(strFileName).pos != zXType.rc.rcOK.pos) {
                    throw new Exception("Unable to load XML file during init with ");
                 } // Can load XML file
                 
                 /**
                  * Bit complex: since 1.5 we need to have a handle to the DS handler used by the entities.
                  * All entities will share the same handler as otherwise they should not be combined in a
                  * single query definition.
                  */
                 if (this.components != null) {
                    QDComponent objComponent;
                    QDEntity objEntity;
                    
                    int intComponents = this.components.size();
                    for (int i = 0; i < intComponents; i++) {
                        objComponent = (QDComponent)this.components.get(i);
                        
                        int intEntities = objComponent.getEntities().size();
                        for (int j = 0; j < intEntities; j++) {
                            objEntity = (QDEntity)objComponent.getEntities().get(j);
                            
                            if (resolveEntity(objEntity).pos == zXType.rc.rcOK.pos) {
                                this.DSHandler = (DSHRdbms)objEntity.getBo().getDS();
                                
                                if (this.DSHandler == null) {
                                    throw new ZXException("Unable to get DS handler for entity", objEntity.getName());
                                }
                            }
                            
                        } // Loop over entities
                        
                    } // Loop over components
                }
                
             } // Name of QD passed
             
             return init;
         } catch (Exception e) {
             getZx().trace.addError("Failed to : Initialise the query def object.", e);
             if (getZx().log.isErrorEnabled()) {
                 getZx().log.error("Parameter : pstrName = "+ pstrName);
             }
             
             if (getZx().throwException) throw new ZXException(e);
             init = zXType.rc.rcError;
             return init;
         } finally {
             if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                 getZx().trace.returnValue(init);
                 getZx().trace.exitMethod();
             }
         }
     }

     /**
	  * Load querydefinition.
	  * 
	  * @param pstrName The given name is a full path
	  * @return Returns the return code of the method.
	  * @throws ParsingException Thrown if we have failed to parsing the QueryDef descriptor file.
	  */
     public zXType.rc load(String pstrName) throws ParsingException {
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrName", pstrName);
		}

		zXType.rc load = zXType.rc.rcOK;

		try {
			/**
			 * Clear any context / components
			 */
			this.components = new ArrayList();
			
			digester = new Digester();
			
			digester.push(this); // Do the local setters
			
	        /**
	         * NOTE : This is a performance optimisation, 
	         * but it might cause errors with concurrent query def parsing.
	         */
			digester.addBeanPropertySetter("querydef/name");
	        digester.addBeanPropertySetter("querydef/version");
	        digester.addBeanPropertySetter("querydef/lastChange");
	        digester.addBeanPropertySetter("querydef/comment");
	        
			QDComponentFactory factor = new QDComponentFactory(this); // Create a valid QSComponent
			digester.addFactoryCreate("querydef/components/component", factor, true);
			// Create a default QDEntity
			digester.addObjectCreate("querydef/components/component/entities/entity", QDEntity.class);
			// Add the entity.
			digester.addSetNext("querydef/components/component/entities/entity", "addEntity");
			// Do all of the setters for QDEntity
			digester.addSetNestedProperties("querydef/components/component/entities/entity"); 
			
			// Add the component
			digester.addSetNext("querydef/components/component", "addComponent"); 
			
			// Do all of the setters for QDComponent
			digester.addSetNestedProperties("querydef/components/component");

			synchronized (QueryDef.class) {
				digester.parse(pstrName);
			}

			return load;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Load querydefinition.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pstrName = " + pstrName);
			}
			
			if (getZx().throwException) throw new ParsingException("Failed to parse QueryDef xml.", e);
			load = zXType.rc.rcError;
			return load;
		} finally {
			if (getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(load);
				getZx().trace.exitMethod();
			}
		}
	}
     
    /**
	 * Called by the digester to add a new QSComponent
	 * 
	 * @param objQDComponent The QSComponent to add
	 */
    public void addComponent(QDComponent objQDComponent) {
         this.components.add(objQDComponent);
    }
    
    /**
	 * Bind / execute query and return recordset.
	 * 
	 * @return Returns a resultset from the query.
	 * @throws ZXException Thrown if rs fails.
	 */
    public DSRS rs() throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        DSRS objRS = null;
        
        try {
            /**
             * Force DSHandler to be set by forcing a call to resolveEntity
             */
            String strQury = qry();
            
            /**
             * If we do not have a DSHandler yet we are in deep trouble (see resolveEntity)
             */
            if (findHandler().pos != zXType.rc.rcOK.pos) {
                throw new ZXException("No DS handler available");
            }
            
            objRS = getDSHandler().sqlRS(strQury);
            if (objRS == null) {
                throw new ZXException("Unable to execute query : " + qry());
            }
            
            return objRS;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Bind / execute query and return recordset.", e);
            if (getZx().throwException) throw new ZXException(e);
            return objRS;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(objRS);
                getZx().trace.exitMethod();
            }
        }
    }    
    
    /**
    * Build the query as it currently stands.
    *
    * @param penmPFScope  The pageflow scope of the query
    * @param pstrScope The scope of the query
    * @return Returns the sql query
    * @throws ZXException Thrown if buildQuery fails. 
    */
    public String buildQuery(zXType.queryDefPageFlowScope penmPFScope, String pstrScope) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmPFScope", penmPFScope);
            getZx().trace.traceParam("pstrScope", pstrScope);
        }
        
        StringBuffer buildQuery = new StringBuffer(64); 
        
        try {
            QDComponent objComponent;
            String strTmp;
            
            if(findHandler().pos != zXType.rc.rcOK.pos) {
                throw new ZXException("No DS handler available");
            }
            
            int intComponents = getComponents().size();
            for (int i = 0; i < intComponents; i++) {
                objComponent = (QDComponent)getComponents().get(i);
                
                /**
                 * Skip any parts not for the current database type
                 */
                zXType.databaseType dbType = objComponent.getDatabaseType();
                if (dbType.equals(zXType.databaseType.dbAny) || dbType.equals(DSHandler.getDbType())) {
                    /**
                     * DGS 14JAN2003: And skip any not for the defined pageflow scope, unless 
                     * either side's scope is not applicable
                     */
                    zXType.queryDefPageFlowScope pfScope =  objComponent.getEnmpfscope();
                    if (pfScope.equals(penmPFScope) 
                        || pfScope.equals(zXType.queryDefPageFlowScope.qdpfsNotApplicable)
                        || penmPFScope.equals(zXType.queryDefPageFlowScope.qdpfsNotApplicable)) {
                        if (objComponent.getScope().equals(pstrScope)) {
                        	strTmp = objComponent.buildComponent();
                            if (StringUtil.len(strTmp) == 0) {
                            	throw new ZXException("Unable to build component", objComponent.toString());
                            }
                            buildQuery.append(strTmp).append(" ");
                        }
                    }
                }
            }
            
            return buildQuery.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build the query as it currently stands.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmPFScope = "+ penmPFScope);
                getZx().log.error("Parameter : pstrScope = "+ pstrScope);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return buildQuery.toString();
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildQuery);
                getZx().trace.exitMethod();
            }
        }
    }
    
	/**
	* Resolve an BO entiy associated with a component.
	* 
	* @param pobjEntity The QDEntity to resolve the BO for.
	* @return Returns the return code of the method.
	* @throws ZXException Thrown if resolveEntity fails. 
	*/
	protected zXType.rc resolveEntity(QDEntity pobjEntity) throws ZXException{
	    return resolveEntity(pobjEntity, false);
	}
    
    /**
     * Resolve an BO entiy associated with a component. 
     * 
     * <pre>
     * 
     * Check whether they are in the context, if not create and add. And then
     * it will resolve the attribute group.
     *</pre>
     *
     * @param pobjEntity  The QDEntity to resolve the BO for.
     * @param pblnNoGroup Whether to get the attribute group. Optional, default is false.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if resolveEntity fails. 
     */
    protected zXType.rc resolveEntity(QDEntity pobjEntity, boolean pblnNoGroup) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjEntity", pobjEntity);
            getZx().trace.traceParam("pblnNoGroup", pblnNoGroup);
        }
        
        zXType.rc resolveEntity = zXType.rc.rcOK;
        
        try {
            /**
             * See if the object is already in the context
             * 
             * Btw; note how we rely on the brute force error handling to
             *  take care of idiots (and one day that will be me, I know) that
             *  put a property in the context and retrieve it as if it was a BO
             * 
             *  In case of an alias we ensure that we have a private descriptor
             *  to make sure we do not re-use the same BO with a different
             *  alias....
             */
            ZXBO objBO;
            if ( StringUtil.len(pobjEntity.getAlias()) == 0 ) {
                objBO = getZx().getBOContext().getBO(pobjEntity.getEntity());
                if (objBO == null) {
                    pobjEntity.setBo(getZx().createBO(pobjEntity.getEntity()));
                } else {
                    pobjEntity.setBo(objBO);
                }
            } else {
                pobjEntity.setBo(getZx().createBO(pobjEntity.getEntity(), true));
                pobjEntity.getBo().getDescriptor().setAlias(pobjEntity.getAlias());
            }
            if (pobjEntity.getBo() == null) {
                throw new Exception("Unable to resolve entity," + pobjEntity.getName());
            }
            
            /**
             * And get the group as well, if requested
             */
            if (!pblnNoGroup) {
                pobjEntity.setAttrGroup(pobjEntity.getBo().getDescriptor().getGroup(pobjEntity.getGroup()));
                if (pobjEntity.getAttrGroup() == null) {
                    throw new Exception("Unable to get attribute group, " + pobjEntity.getName());
                }    
            }
            
            return resolveEntity;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Resolve an BO entiy associated with a component.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjEntity = "+ pobjEntity);
                getZx().log.error("Parameter : pblnNoGroup = "+ pblnNoGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            resolveEntity = zXType.rc.rcError;
            return resolveEntity;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(resolveEntity);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
	 * Construct the query and return.
	 * 
	 * <pre>
	 *  
	 *  NOTE : This calls, qry(null, null)
	 * </pre>
	 * 
	 * @return Returns a sql query.
	 * @throws ZXException Thrown if qry fails.
	 */
	public String qry() throws ZXException {
		return qry(null, null);
	}
    
    /**
     * Construct the query and return.
     *
     * @param penmPFScope  The pageflow scope. Optional, and the default is qdpfsNotApplicable.
     * @param pstrScope  The scope of the query
     * @return Returns a sql query.
     * @throws ZXException Thrown if qry fails. 
     */
    public String qry(zXType.queryDefPageFlowScope penmPFScope, String pstrScope) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmPFScope", penmPFScope);
            getZx().trace.traceParam("pstrScope", pstrScope);
        }
        
        String qry = null; 
        
        if (penmPFScope == null) {
            penmPFScope = zXType.queryDefPageFlowScope.qdpfsNotApplicable;
        }
        if (pstrScope == null) {
        	pstrScope = "";
        }
        
        try {
        	
            qry = buildQuery(penmPFScope, pstrScope);
            return qry;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Construct the query and return.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmPFScope = "+ penmPFScope);
                getZx().log.error("Parameter : pstrScope = "+ pstrScope);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return qry;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(qry);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Execute query and return number of affected rows.
     * 
     * <pre>
     * 
     * This is used for update or insert queries.
     * </pre>
     *
     * @return Returns The number of rows returned
     * @throws ZXException Thrown if execute fails. 
     */
    public int execute() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        int execute = 0;
        
        try {
            
            if (findHandler().pos != zXType.rc.rcOK.pos) {
                throw new ZXException("No DS handler available");
            }
            
            execute = DSHandler.sqlExecute(qry());
            
            return execute;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Execute query and return number of affected rows.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return execute;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(execute);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Repository Editor methods.
    
    /**
     * Dump query definition. This will be useful for the java version of the respository.
     *
     * @return Returns an xml dump of a QueryDef
     */
     public String dumpAsXML() {
         if(getZx().trace.isFrameworkCoreTraceEnabled()) {
             getZx().trace.enterMethod();
         }
         
         XMLGen dumpAsXML = new XMLGen(500);
         dumpAsXML.xmlHeader();
         
         try {
             dumpAsXML.openTag("querydef");
             
             dumpAsXML.taggedValue("name", this.name);
             dumpAsXML.taggedValue("version", this.version);
             
             dumpAsXML.taggedCData("lastchange", this.lastChange);
             dumpAsXML.taggedCData("comment", this.comment);
             
             dumpAsXML.openTag("components");
             QDComponent objComponent;
             int intComponents = getComponents().size();
             for (int i = 0; i < intComponents; i++) {
                 objComponent = (QDComponent)getComponents().get(i);
                 
                 dumpAsXML.openTag("component");
                 
                 dumpAsXML.taggedCData("comment", objComponent.getComment());
                 
                 dumpAsXML.taggedValue("database", objComponent.getDatabase());
                 dumpAsXML.taggedValue("type", objComponent.getType());
                 dumpAsXML.taggedValue("pfscope", objComponent.getPfscope());
                 dumpAsXML.taggedValue("scope", objComponent.getScope());
                 dumpAsXML.taggedValue("preliteral", objComponent.getPreliteral());
                 dumpAsXML.taggedValue("postliteral", objComponent.getPostliteral());
                 
                 zXType.queryDefType componentType = objComponent.getComponentType();
                 if (componentType.equals(zXType.queryDefType.qdtSelect)) {
                     dumpAsXML.taggedValue("distinct", objComponent.getCompareoperand());     
                     dumpAsXML.taggedValue("nojoin", objComponent.isNojoin());     
                     dumpAsXML.taggedValue("fromclauseonly", objComponent.isFromclauseonly());     
                     dumpAsXML.taggedValue("nofromclause", objComponent.isNofromclause());     
                     dumpAsXML.taggedValue("resolvefk", objComponent.isResolvefk());     
                     dumpAsXML.taggedValue("forceinner", objComponent.isForceinner());     
                     
                 } else if (componentType.equals(zXType.queryDefType.qdtTable)) {
                     dumpAsXML.taggedValue("nametype", objComponent.getNametype());     

                 } else if (componentType.equals(zXType.queryDefType.qdtWhereClause)) {
                     dumpAsXML.taggedValue("negate", objComponent.isNegate());     
                     dumpAsXML.taggedValue("useor", objComponent.isUseor());     
                     dumpAsXML.taggedValue("nofirstand", objComponent.isNofirstand());     
                     
                 } else if (componentType.equals(zXType.queryDefType.qdtWhereCondition)) {
                     dumpAsXML.taggedCData("value", objComponent.getValue());
                     dumpAsXML.taggedValue("compareoperand", objComponent.getCompareoperand());     
                     dumpAsXML.taggedValue("nofirstand", objComponent.isNofirstand());     

                 } else if (componentType.equals(zXType.queryDefType.qdtLiteral)) {
                     dumpAsXML.taggedValue("value", objComponent.getValue());     
                     
                 } else if (componentType.equals(zXType.queryDefType.qdtValue)) {
                     dumpAsXML.taggedValue("value", objComponent.getValue());
                     
                 }
                 
                 /** Dump the entities in a QDComponent. **/
                 int intEntities = objComponent.getEntities().size();
                 if (intEntities > 0) {
                    dumpAsXML.openTag("entities");
                    for (int j = 0; j < intEntities; j++) {
	                       QDEntity objEntity = (QDEntity)objComponent.getEntities().get(j);
	                       
	                       dumpAsXML.openTag("entity");
	                       dumpAsXML.taggedValue("name", objEntity.getName());
	                       dumpAsXML.taggedValue("entity", objEntity.getEntity());
	                       dumpAsXML.taggedCData("group", objEntity.getGroup());
	                       dumpAsXML.taggedValue("resolvefk", objEntity.isResolvefk());
	                       dumpAsXML.taggedValue("alias", objEntity.getAlias());
	                       dumpAsXML.closeTag("entity");
	   				}
                    dumpAsXML.closeTag("entities");
                 }
                 
                 dumpAsXML.closeTag("component");
             }
             dumpAsXML.closeTag("components");
             
             dumpAsXML.closeTag("querydef");
             
             return dumpAsXML.getXML();
         } finally {
             if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                 getZx().trace.returnValue(dumpAsXML);
                 getZx().trace.exitMethod();
             }
         }
     }
     
     //------------------------ Private methods.
     
     /**
      * Find data-source handler when not set yet; notice that the DSHandler is set
      * when parsing the definition and the only reason when it may not have been set
      * is when the query definition is used from the repository editor. When this
      * is the case, we cannot assume that the class associated with an entity is
      * available (may not have been compiled yet) so we cannot simply do a createBO
      * on the entity and have to fiddle around with descriptor itself
      * 
      * @return Returns the return code of the method.
      * @throws ParsingException Thrown if we have failed to parse the business object xml file.
      */
     private zXType.rc findHandler() throws ParsingException {
        zXType.rc findHandler = zXType.rc.rcOK;
        
        if (this.DSHandler != null) {
            return findHandler;
        }
        
        if (this.components != null) {
            int intComponents = this.components.size();
            QDComponent objComponent;
            QDEntity objEntity;
            Descriptor objDesc;
            
            for (int i = 0; i < intComponents; i++) {
                objComponent = (QDComponent)this.components.get(i);
                
                if (objComponent.getEntities() != null) {
                    int intEntities = objComponent.getEntities().size();
                    for (int j = 0; j < intEntities; j++) {
                        objEntity = (QDEntity)objComponent.getEntities().get(j);
                        
                        objDesc = new Descriptor();
                        objDesc.init(getZx().getBoDir() + File.separator + objEntity.getEntity() + ".xml", false);
                        this.DSHandler = (DSHRdbms)getZx().getDataSources().getDSByName(objDesc.getDataSource());
                        return findHandler;
                    }
                }
                
            }
        }
        
        return findHandler;
     }
}