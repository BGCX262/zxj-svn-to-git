/*
 * Created on Apr 5, 2004 by Michael Brewer
 * $Id: QDComponent.java,v 1.1.2.12 2005/11/24 17:45:08 mike Exp $
 */
package org.zxframework.sql;

import java.util.ArrayList;
import java.util.Iterator;

import org.zxframework.Attribute;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;

/**
 * Query definition component class.
 * 
 * <pre>
 * 
 * Change    : BD24FEB03
 * Why       : table component type was never properly implemented
 *  
 * Change    : BD28MAY03
 * Why       : Support for directors
 *  
 * Change    : DGS21AUG2003
 * What      : Added comment.
 *  
 * Change    : BD4SEP03
 * What      : Fixed serious bug with handling where group
 * 
 * Change    : BD9JUN04
 * What      : Added support for concurrency control
 *              Fixed problem with group in whereClause; can now be director
 *              
 * Change    : BD29MAR05 - V1.5:1
 * Why       : Only reviewed and removed obsolete code that had been commented out for long enough
 *             
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class QDComponent extends ZXObject {

    //------------------------ Members
    
    private String database;
    private zXType.databaseType enmDatabase;
    private String type;
    /** The type of query definition as a enum. **/
    private zXType.queryDefType enmtype;
    /** The pageflow scope of this query definition. **/
    private String pfscope;
    /** The pageflow scope of this query definition. **/
    private zXType.queryDefPageFlowScope enmpfscope;
    /** The type of compare operand to use. **/
    private zXType.compareOperand enmcompareoperand;
    private String nametype;
    private zXType.sqlObjectName enmnametype;
    
    /** The scope of the query definition component. **/
    private String scope;
    private String preliteral;
    private String postliteral;
    private String value;
    private String compareoperand;
    /** Whether to have not first AND in the where clause. **/
    private boolean nofirstand;
    /** Whether to use the DISTINCT keyword to use unique results. **/
    private boolean distinct;
    /** Whether to have no SQL joins at all or not. **/
    private boolean nojoin;
    /** Whether to only have a from clause. **/
    private boolean fromclauseonly;
    /** Whether to have no from clause. **/
    private boolean nofromclause;
    /** Whether to resolve the foriegn keys of the business units. **/
    private boolean resolvefk;
    /** Whether to force a inner join in the query. **/
    private boolean forceinner;
    /** A developers only comment. **/
    private String comment;
    /** Whether to use OR's in the where clause. **/
    private boolean useor;
    /** Whether to negate the SQL query. Probably put in NOT's **/
    private boolean negate;
    /** Whether the sort order of the query is descending. **/
    private boolean descending;
    /** Whether the query is columns only.  **/
    private boolean columnsonly;
    private int componentoption;
    
    /** A handle to the Query Definition object. **/
    private QueryDef querydef;
    /** A collection of QDEntity's **/
    private ArrayList entities;
    
    //------------------------ Constructor
    
    /**
     * Hide the default constructor.
     */
    public QDComponent() {
        super();
        
        /** Init the entities collection. **/
        this.entities = new ArrayList();
    }
    
    /**
     * @param objQueryDef
     */
    public void init(QueryDef objQueryDef) {
        this.querydef = objQueryDef;
    }

    //------------------------ Getters and Setters

    /**
     * Whether the query is columns only.
     * 
     * @return Returns the columnsOnly.
     */
    public boolean isColumnsonly() {
        return columnsonly;
    }

    /**
     * @param columnsOnly The columnsOnly to set.
     */
    public void setColumnsonly(boolean columnsOnly) {
        this.columnsonly = columnsOnly;
    }

    /**
     * A developer comment for the Query Definition component.
     * 
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
     * @return Returns the compareOperand.
     */
    public String getCompareoperand() {
        return this.compareoperand;
    }

    /**
     * @param compareOperand The compareOperand to set.
     */
    public void setCompareoperand(String compareOperand) {
    	
        this.compareoperand = compareOperand;
        /** NOTE : This is going to be stricter than the COM+ version as it only supports on of each type.  **/
        this.enmcompareoperand = zXType.compareOperand.getEnum(compareOperand);
        
    }

    /**
     * @return Returns the componentOption.
     */
    public int getComponentoption() {
        return componentoption;
    }

    /**
     * @param componentOption The componentOption to set.
     */
    public void setComponentoption(int componentOption) {
        this.componentoption = componentOption;
    }
    
    /**
     * The type of query definition as a string.
     * 
     * @return Returns the componentType.
     */
    public String getType() {
        return this.type;
    }
    
    /**
     * The type of query definition as a string.
     * 
     * @param componentType The componentType to set.
     */
    public void setType(String componentType) {
        this.type = componentType;
        this.enmtype = zXType.queryDefType.getEnum(componentType);
    }
    
    /**
     * The type of query definition as a enum.
     * 
     * @return Returns the type of Component
     */
    public zXType.queryDefType getComponentType() {
        return this.enmtype;
    }


    /**
     * The database type as a string.
     * 
     * @return Returns the database.
     */
    public String getDatabase() {
        return this.database;
    }
    
    /**
     * The database type as a string.
     * 
     * @param database The database to set.
     */
    public void setDatabase(String database) {
        this.database = database;
        this.enmDatabase = zXType.databaseType.getEnum(this.database);
    }

    /**
     * The database type as a enum.
     * 
     * @return Returns the enum of the database type.
     */
    public zXType.databaseType getDatabaseType() {
        return this.enmDatabase;
    }

    /**
     * Whether the sort order of the query is descending. 
     * 
     * @return Returns the descending.
     */
    public boolean isDescending() {
        return this.descending;
    }

    /**
     * @param descending The descending to set.
     */
    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    /**
     * Whether to use the DISTINCT keyword to use unique results.
     * 
     * @return Returns the distinct.
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * @param distinct The distinct to set.
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * A collection of QDEntity's.
     * 
     * @return Returns the entities.
     */
    public ArrayList getEntities() {
        return entities;
    }

    /**
     * @return Returns the forceInner.
     */
    public boolean isForceinner() {
        return forceinner;
    }

    /**
     * @param forceInner The forceInner to set.
     */
    public void setForceinner(boolean forceInner) {
        this.forceinner = forceInner;
    }

    /**
     * @return Returns the fromClauseOnly.
     */
    public boolean isFromclauseonly() {
        return fromclauseonly;
    }

    /**
     * @param fromClauseOnly The fromClauseOnly to set.
     */
    public void setFromclauseonly(boolean fromClauseOnly) {
        this.fromclauseonly = fromClauseOnly;
    }

    /**
     * @return Returns the nameType.
     */
    public String getNametype() {
        return this.nametype;
    }

    /**
     * @param nameType The nameType to set.
     */
    public void setNametype(String nameType) {
        this.nametype = nameType;
        this.enmnametype = zXType.sqlObjectName.getEnum(nameType);
    }

    /**
     * Whether to negate the SQL query. Probably put in NOT's.
     * 
     * @return Returns the negate.
     */
    public boolean isNegate() {
        return negate;
    }

    /**
     * @param negate The negate to set.
     */
    public void setNegate(boolean negate) {
        this.negate = negate;
    }

    /**
     * Whether to have not first AND in the where clause.
     * 
     * @return Returns the noFirstAnd.
     */
    public boolean isNofirstand() {
        return nofirstand;
    }

    /**
     * @param noFirstAnd
     *                 The noFirstAnd to set.
     */
    public void setNofirstand(boolean noFirstAnd) {
        this.nofirstand = noFirstAnd;
    }

    /**
     * @return Returns the noFromClause.
     */
    public boolean isNofromclause() {
        return nofromclause;
    }

    /**
     * @param noFromClause The noFromClause to set.
     */
    public void setNofromclause(String noFromClause) {
        this.nofromclause = StringUtil.booleanValue(noFromClause);
    }

    /**
     * Whether to have no SQL joins at all or not.
     * 
     * @return Returns the noJoin.
     */
    public boolean isNojoin() {
        return nojoin;
    }

    /**
     * @param noJoin The noJoin to set.
     */
    public void setNojoin(String noJoin) {
        this.nojoin = StringUtil.booleanValue(noJoin);
    }

    /**
     * The pageflow scope of this query definition.
     *  
     * @return Returns the pFScope.
     */
    public String getPfscope() {
        return this.pfscope;
    }
    
    /**
     * The pageflow scope of this query definition.
     * 
     * @return Returns the enum for the PFScope
     */
    public zXType.queryDefPageFlowScope getEnmpfscope() {
        return this.enmpfscope;
    }

    /**
     * @param scope The pFScope to set.
     */
    public void setPfscope(String scope) {
        this.pfscope = scope;
        this.enmpfscope = zXType.queryDefPageFlowScope.getEnum(scope);
    }

    /**
     * @return Returns the postLiteral.
     */
    public String getPostliteral() {
        return postliteral;
    }

    /**
     * @param postLiteral The postLiteral to set.
     */
    public void setPostliteral(String postLiteral) {
        this.postliteral = postLiteral;
    }

    /**
     * @return Returns the preLiteral.
     */
    public String getPreliteral() {
        return preliteral;
    }

    /**
     * @param preLiteral The preLiteral to set.
     */
    public void setPreliteral(String preLiteral) {
        this.preliteral = preLiteral;
    }

    /**
     * @return Returns the resolvefk.
     */
    public boolean isResolvefk() {
        return resolvefk;
    }

    /**
     * @param resolvefk The resolvefk to set.
     */
    public void setResolvefk(boolean resolvefk) {
        this.resolvefk = resolvefk;
    }

    /**
     * The scope of the query definition component.
     * 
     * @return Returns the scope.
     */
    public String getScope() {
        return scope;
    }

    /**
     * @param scope The scope to set.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * @return Returns the useOr.
     */
    public boolean isUseor() {
        return useor;
    }

    /**
     * @param useOr The useOr to set.
     */
    public void setUseor(boolean useOr) {
        this.useor = useOr;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    //------------------------ Public methods

    /**
     * This is called by the digester to add the to the QDComponent
     * @param qdentity Add a QDEntity.
     */
    public void addEntity(QDEntity qdentity) {
        this.entities.add(qdentity);
    }

    /**
     * Build single component.
     *
     * <pre>
     * This will call the relavent builds according to the type of component.
     * </pre>
     *
     * @return Returns the sql of this QDComponet. 
     * @throws ZXException Thrown if buildComponent fails. 
     */
    public String buildComponent() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        StringBuffer buildComponent = new StringBuffer(); 
        
        try {
        	
            if (enmtype.equals(zXType.queryDefType.qdtColumn)) {
                buildComponent.append(buildColumn());
            } else if (enmtype.equals(zXType.queryDefType.qdtTable)) {
            	buildComponent.append(buildTable());
            } else if (enmtype.equals(zXType.queryDefType.qdtLiteral)) {
            	buildComponent.append(buildLiteral());
            } else if (enmtype.equals(zXType.queryDefType.qdtDelete)) {
            	buildComponent.append(buildDelete());
            } else if (enmtype.equals(zXType.queryDefType.qdtSelect)) {
            	buildComponent.append(buildSelect());
            } else if (enmtype.equals(zXType.queryDefType.qdtUpdate)) {
            	buildComponent.append(buildUpdate());
            } else if (enmtype.equals(zXType.queryDefType.qdtInsert)) {
            	buildComponent.append(buildInsert());
            } else if (enmtype.equals(zXType.queryDefType.qdtOrderBy)) {
            	buildComponent.append(buildOrderBy());
            } else if (enmtype.equals(zXType.queryDefType.qdtWhereClause)) {
            	buildComponent.append(buildWhereClause());
            }  else if (enmtype.equals(zXType.queryDefType.qdtWhereCondition)) {
            	buildComponent.append(buildWhereCondition());
            }  else if (enmtype.equals(zXType.queryDefType.qdtValue)) {
            	buildComponent.append(buildValue());
            }
            
            if (StringUtil.len(getPreliteral()) > 0) {
            	buildComponent.insert(0, getPreliteral() + " ");
            }
            
            if (StringUtil.len(getPostliteral()) > 0) {
            	buildComponent.append(" ").append(getPostliteral());
            }
            
            if (StringUtil.len(buildComponent) == 0) {
                throw new Exception("Failed to build compoent. The length is null");
            }
            
            return buildComponent.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build single component.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildComponent.toString();
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildComponent);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build single component of column type.
     *
     * @return Returns a single component of column type.
     * @throws ZXException Thrown if buildColumn fails. 
     */
    public String buildColumn() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        StringBuffer buildColumn = new StringBuffer(); 

        try {
            QDEntity objEntity;
            Attribute objAttr;
            
            int intEntities = getEntities().size();
            for (int i = 0; i < intEntities; i++) {
                objEntity = (QDEntity)getEntities().get(i);
                
                /**
                 * Resolve the entity BO
                 */
                this.querydef.resolveEntity(objEntity);
                
                Iterator iter = objEntity.getAttrGroup().iterator();
                while (iter.hasNext()) {
                    objAttr = (Attribute)iter.next();
                    
                    if (StringUtil.len(objAttr.getColumn()) > 0) {
                        if (buildColumn.length() > 0) {
                            buildColumn.append(", ");            
                        }
                        
                        buildColumn.append(getZx().getSql().columnName(objEntity.getBo(), 
                        											   objAttr, this.enmnametype));
                    }
                }
			}
            
            return buildColumn.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build single component of column type.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildColumn.toString();
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildColumn);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build single OrderBy clause component.
     *
     * @return Returns a single component of column type.
     * @throws ZXException Thrown if buildOrderBy fails. 
     */
    public String buildOrderBy() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        StringBuffer buildOrderBy = new StringBuffer();
        
        try {
            
            QDEntity objEntity;
            
            int intEntities = getEntities().size();
            for (int i = 0; i < intEntities; i++) {
                objEntity = (QDEntity)getEntities().get(i);
                
                /**
                 * Resolve the entity BO
                 */
                querydef.resolveEntity(objEntity);
                
                if (buildOrderBy.length() > 0) {
                    buildOrderBy.append(", ");
                }
                
                buildOrderBy.append(getZx().getSql().orderByClause(objEntity.getBo(), 
                												   objEntity.getGroup(), 
                												   this.descending,true));
            }
            
            return buildOrderBy.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build single OrderBy clause component.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildOrderBy.toString();
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildOrderBy);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build a single where clause.
     *
     * @return Returns a single component of column type.
     * @throws ZXException Thrown if buildWhereClause fails. 
     */
    public String buildWhereClause() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        StringBuffer buildWhereClause = new StringBuffer(); 

        try {
            QDEntity objEntity;
            
            int intEntities = getEntities().size();
            for (int i = 0; i < intEntities; i++) {
                objEntity = (QDEntity)getEntities().get(i);
                
                /**
                 * Resolve the entity BO, but do NOT resolve the group as it
                 * may not be a true attribute group but an where clause expression
                 */
                this.querydef.resolveEntity(objEntity, true);
                
                if (this.nofirstand) { // and or :)
                    if (buildWhereClause.length() > 0) {
                        buildWhereClause.append(this.useor?" OR ":" AND ");
                    }
                    
                } else {
                    buildWhereClause.append(this.useor?" OR ":" AND ");
                }
                
                buildWhereClause.append(getZx().getSql().whereCondition(objEntity.getBo(), 
                        				    getZx().resolveDirector(objEntity.getGroup()), 
                        				    !this.negate,  
                        				    this.useor)
                        				);
            }
            
            if (buildWhereClause.length() == 0) {
                throw new  Exception("Unable to create the where clause.");
            }
            
            return buildWhereClause.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build a single where clause.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildWhereClause.toString();
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildWhereClause);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build single component of column type.
     * 
     * <pre>
     * 
     * Assumes   :
     *   objentity.group is a single attribute
     * </pre>
     *
     * @return Returns a single component of column type.
     * @throws ZXException Thrown if buildWhereCondition fails. 
     */
    public String buildWhereCondition() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        StringBuffer buildWhereCondition = new StringBuffer();
        
        try {
            
        	int intEntities = getEntities().size();
        	
            QDEntity objEntity = (QDEntity)getEntities().get(0); // get the fist element
            this.querydef.resolveEntity(objEntity);
            
            if (intEntities == 1) {
                
                if (!this.nofirstand) { // and or :)
                    buildWhereCondition.append(this.useor?" OR ":" AND ");
                }
                
                /**
                 * If only one entity, use the supplied value
                 * The value can be special:
                 * #xxxx - refer to attribute from entity
                 */
                if (this.value.charAt(0) == '#') {
                    if (this.value.toLowerCase().equals("#null")) {
                        buildWhereCondition.append(
                        	getZx().getSql().singleWhereCondition(objEntity.getBo(), 
                                objEntity.getBo().getDescriptor().getAttribute(objEntity.getGroup()), 
                                this.enmcompareoperand, 
                                new StringProperty(null, true)));
                    } else {
                        buildWhereCondition.append(
                                getZx().getSql().singleWhereCondition(objEntity.getBo(), 
                                                objEntity.getBo().getDescriptor().getAttribute(objEntity.getGroup()),
                                                this.enmcompareoperand, 
                                                new StringProperty(getZx().resolveDirector(this.value),false)));
                    }
            	} else {
            	    buildWhereCondition.append(
            	            getZx().getSql().singleWhereCondition(objEntity.getBo(), 
            	            objEntity.getBo().getDescriptor().getAttribute(objEntity.getGroup()),
            	            this.enmcompareoperand, 
            	            new StringProperty(this.value,false)));
            	}
                
            } else {
                /**
                 * More than one entity - ignore the supplied value and simply compare the
                 *  entities, like 'table1.col = table2.col"
                 */
                QDEntity objOtherEntity;
                zXType.sqlObjectName sqlObjectType = zXType.sqlObjectName.sonName;
                
                for (int i = 0; i < intEntities; i++) {
                    objOtherEntity = (QDEntity)getEntities().get(i);
                    
                    this.querydef.resolveEntity(objOtherEntity);
                    if (this.nofirstand && buildWhereCondition.length() > 0) { // and or :)
                        buildWhereCondition.append(this.useor?" OR ":" AND ");
                    } else {
                        buildWhereCondition.append(this.useor?" OR ":" AND ");
                    }
                    
                    /**
                     * And generate condition
                     * 
                     * The objEntity group  has to be a valid attribute
                     */
                    buildWhereCondition.append(
                          getZx().getSql().columnName(objEntity.getBo(), 
                          			objEntity.getBo().getDescriptor().getAttribute(objEntity.getGroup()), 
                          			sqlObjectType)
                        					   );
                    
                    if (this.compareoperand.equals(zXType.compareOperand.coCNT) 
                    	|| this.compareoperand.equals(zXType.compareOperand.coSW)) {
                        buildWhereCondition.append(" LIKE ");
                    } else if (this.compareoperand.equals(zXType.compareOperand.coEQ)) {
                        buildWhereCondition.append(" = ");
                    } else if (this.compareoperand.equals(zXType.compareOperand.coNE)) {
                        buildWhereCondition.append(" <> ");
                    } else if (this.compareoperand.equals(zXType.compareOperand.coSW)) {
                        buildWhereCondition.append(" <> ");
                    } else if (this.compareoperand.equals(zXType.compareOperand.coGE)) {
                        buildWhereCondition.append(" >= ");
                    } else if (this.compareoperand.equals(zXType.compareOperand.coGT)) {
                        buildWhereCondition.append(" > ");
                    } else if (this.compareoperand.equals(zXType.compareOperand.coLE)) {
                        buildWhereCondition.append(" <= ");
                    } else if (this.compareoperand.equals(zXType.compareOperand.coLT)) {
                        buildWhereCondition.append(" < ");
                    }
                    
                    buildWhereCondition.append(
                        getZx().getSql().columnName(objOtherEntity.getBo(), 
                          			objOtherEntity.getBo().getDescriptor().getAttribute(objOtherEntity.getGroup()), 
                          			sqlObjectType)
                        					  );
                }
            }
            
            return buildWhereCondition.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build single where condition.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildWhereCondition.toString();
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildWhereCondition);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build single component of column type.
     * 
     * <pre>
     * 
     * Assumes   :
     *    objentity.group is a single attribute
     * </pre>
     *
     * @return Returns a single component of column type.
     * @throws ZXException Thrown if buildValue fails. 
     */
    public String buildValue() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        String buildValue = ""; 

        try {
            
            /**
             * Only one entity supported for this component type
             */
            QDEntity objEntity = (QDEntity)getEntities().get(0);
            this.querydef.resolveEntity(objEntity);
            
            DSHRdbms objDSHandler = (DSHRdbms)objEntity.getBo().getDS();
            Attribute objAttr = objEntity.getBo().getDescriptor().getAttribute(objEntity.getGroup());
            
            buildValue = getZx().getSql().dbStrValue(objAttr.getDataType().pos, 
            										 this.value, 
            										 objDSHandler.getDbType());
            
            return buildValue;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build value.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildValue;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildValue);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build single component of Table type.
     *
     * @return Returns a single component of Table type.
     * @throws ZXException Thrown if buildTable fails. 
     */
    public String buildTable() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        StringBuffer buildTable = new StringBuffer();

        try {
            
            QDEntity objEntity;
            
            int intEntities = getEntities().size();
            for (int i = 0; i < intEntities; i++) {
                objEntity = (QDEntity)getEntities().get(i);
                
                /**
                 * Resolve the entity BO
                 */
                this.querydef.resolveEntity(objEntity);
                
                if (buildTable.length() > 0) {
                    buildTable.append(", ");
                }
                
                buildTable.append(getZx().getSql().tableName(objEntity.getBo(), this.enmnametype));
            }
            
            return buildTable.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build single component of Table type.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildTable.toString();
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildTable);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build single component of Table type.
     *
     * @return Returns a single component of Table type.
     * @throws ZXException Thrown if buildSelect fails. 
     */
    public String buildSelect() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        String buildSelect = ""; 
        
        try {
        	
            int intEntities = getEntities().size();
            
            ZXBO[] arrBO = new ZXBO[intEntities];
            String[] arrGroup = new String[intEntities];
            boolean[] arrResolveFK = new boolean[intEntities];
            boolean[] arrAuditable = new boolean[intEntities];
            boolean [] arrConcurrencyControl = new boolean[intEntities];
            
            QDEntity objEntity;
            
            for (int i = 0; i < intEntities; i++) {
                objEntity = (QDEntity)getEntities().get(i);
                
                /** Resolve the entity BO **/
                this.querydef.resolveEntity(objEntity);
                
                arrBO[i] = objEntity.getBo();
                arrGroup[i] = objEntity.getGroup();
                arrResolveFK[i] = objEntity.isResolvefk();
                
                /**
                 * Save original state of auditability then temporarily turn it off -  we
                 * don't want any audit columns adding to select unless we ask for them:
                 * BD9JUN04 - Now also save concurrencyConrol setting
                 */
                arrAuditable[i] = objEntity.getBo().getDescriptor().isAuditable();
                objEntity.getBo().getDescriptor().setAuditable(false);
                arrConcurrencyControl[i] = objEntity.getBo().getDescriptor().isConcurrencyControl();
                objEntity.getBo().getDescriptor().setConcurrencyControl(false);
            }
            
            boolean blnOuterJoin = !this.forceinner; 
            buildSelect = getZx().getSql().selectQuery(arrBO, 
            										   arrGroup, 
            										   arrResolveFK, 
            										   this.distinct, 
            										   blnOuterJoin, 
            										   this.fromclauseonly, 
            										   this.nofromclause, 
            										   this.nojoin);
            
            /**
             * Set auditability back to original state:
             * BD9JUN04 - And concurrency control
             */
            for (int i = 0; i < intEntities; i++) {
                objEntity = (QDEntity)getEntities().get(i);
                
                objEntity.getBo().getDescriptor().setAuditable(arrAuditable[i]);
                objEntity.getBo().getDescriptor().setConcurrencyControl(arrConcurrencyControl[i]);
            }
            
            return buildSelect;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build a single select query.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildSelect;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildSelect);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build a single delete statement for the first entity of this component.
     *
     *<pre>
     *
     * For delete we only look at one entity
     *</pre>
     *
     * @return Returns a single component of Table type.
     * @throws ZXException Thrown if buildDelete fails. 
     */
    public String buildDelete() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        String buildDelete = "";

        try {
            
            /**
             * For delete we only look at one entity
             */
            QDEntity objEntity = (QDEntity)getEntities().get(0);
            this.querydef.resolveEntity(objEntity);
            buildDelete = getZx().getSql().deleteQuery(objEntity.getBo());
            
            return buildDelete;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build a single delete statement.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildDelete;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildDelete);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build a single update statement for the first entity of this component.
     * 
     * <pre>
     * 
     * For update we only look at one entity
     * </pre>
     *
     * @return Returns a single component of Table type.
     * @throws ZXException Thrown if buildUpdate fails. 
     */
    public String buildUpdate() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        String buildUpdate = null; 

        try {
            
            /**
             * For update we only look at one entity
             */
            QDEntity objEntity = (QDEntity)getEntities().get(0);
            this.querydef.resolveEntity(objEntity);
            buildUpdate = getZx().getSql().updateQuery(objEntity.getBo(), objEntity.getGroup());
            
            return buildUpdate;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build a single update statement.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildUpdate;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildUpdate);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build a single insert statement for the first entity of this component.
     * 
     * <pre>
     * 
     * For insert we only look at one entity
     * </pre>
     *
     * @return Returns a insert statement for a single entity.
     * @throws ZXException Thrown if buildInsert fails. 
     */
    public String buildInsert() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        String buildInsert = "";

        try {
            
            /**
             * For insert we only look at one entity
             */
            QDEntity objEntity = (QDEntity)getEntities().get(0);
            this.querydef.resolveEntity(objEntity);
            buildInsert = getZx().getSql().insertQuery(objEntity.getBo());
            
            return buildInsert;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build a single insert statement.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return buildInsert;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildInsert);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Build the literal query def component.
     *
     * @return Returns a literal query def component
     */
    public String buildLiteral() {
    	/** Very simple: just return the value **/
        String buildLiteral = this.value; 
        return buildLiteral;
    }
    
    //------------------------ Object Overidden Methods.
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
    	toString.append("columnsonly", this.columnsonly);
    	toString.append("comment", this.comment);
    	toString.append("compareoperand", this.compareoperand);   
    	toString.append("componentoption", this.componentoption);   
    	toString.append("database", this.database);
    	toString.append("descending", this.descending);
    	toString.append("distinct", this.distinct);
    	toString.append("forceinner", this.forceinner);
    	toString.append("fromclauseonly", this.fromclauseonly);
    	toString.append("negate", this.negate);
    	toString.append("nofirstand", this.nofirstand);
    	toString.append("nojoin", this.nojoin);
    	toString.append("pfscope", this.pfscope);
    	toString.append("postliteral", this.postliteral);
    	toString.append("preliteral", this.preliteral);
    	toString.append("pfscope", this.pfscope);
    	toString.append("resolvefk", this.resolvefk);    	
    	toString.append("type", this.type);    	
    	toString.append("useor", this.useor);
    	
    	return toString.toString();
    }
}