/*
 * Created on Feb 27, 2004
 * $Id: SQLQuery.java,v 1.1.2.18 2006/07/17 16:40:06 mike Exp $
 */
package org.zxframework.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.util.StringUtil;

/**
 * <p>Query object, used only by SQL object to generate complex, multi BO select queries.</p>
 * 
 * 
 * <pre>
 * 
 * NOTE : This was originally called clsSQLQry
 * 
 * Copying changelog from zx vb framework:
 * 
 * Change    : DGS22JAN2003
 * Why       :  Change to generateSQL to support requirements of the new QueryDef
 * 				object. Should not make any difference to existing users of this
 * 				function but allows consituent parts of the select query to be
 * 				obtained individually.
 * 				Also slight adjustment to the way inner join is determined.
 * 
 * Change    : DGS22JAN2003
 * Why       :  In resolveFK: If this attr has an FK alias, we must use an FK alias	
 * 				on the related object. Typically we have an alias because the same	
 * 				entity is linked twice, so we must continue up through FKs with each	
 * 				having its own linked FK entity.
 * 
 * Change    : DGS24NOV2003
 * Why       :	In generateFromClause check to see if one datatype is a string and the
 * 				other is not. If so, cast the numeric to a string (DBMS-specific). This
 * 				might affect performance but prevents DBMS-generated errors, and in any
 * 				case it is unusual for us to join FKs in this way.
 * 
 * Change    : DGS04FEB2004
 * Why       : In 'generateFromClause' also includes where clause for new Data Constant attribute group
 * 
 * Change    : BD4AUG04
 * Why       : Fixed problem with having an alias and no joins
 * 
 * Change    : MB14OCT2004
 * Why       : Added mySql DB type
 * 
 * Change    : BD15NOV04
 * Why       : See BD7APR04: imagine a more complex scenario when resoving FK
 *             is active: imagine that a BO has a FK to itself (e.g. 'parent'
 *             or so); obviously this must have an alias set. Now also image
 *             that the label group of this BO has a foreign key. This means that
 *             this BO is included in the query twice: once for main BO and
 *             once for resolve purposes. So we have to generate unique alias
 *             for this BO as well
 *                
 * Change    : BD25MAR05 - V1.5:1
 * Why       : Added support for data sources
 * 
 * Change    : BD25MAR05 - V1.5:20
 * Why       : Added support for enhanced FK label behaviour
 * 
 * Change    : BD2SEP05 - V1.5:48
 * Why       : If we generate a query and request attributes that do not have a column
 *             associated with them, than this results in an error (invalid SQL);
 *             this can mainly happen when such an attribute is part of the labelGroup
 *             and we have resolveFK switched on
 *             
 * Change    : DGS08NOV2005 - V1.5:67
 * Why       : In generateFromClause when converting non-strings for comparison with strings for
 *             SQL Server, was trying to use CSTR (as Access) but that is not valid - now uses CAST.
 *             
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class SQLQuery extends ZXObject {
    
    //------------------------ Members  
    
    /** The full SQL query. **/
    private String query;
    /** A collection of SQLColumns used in this Query. **/
    private ZXCollection columns;
    /** A collection of SQLRelations used in the Query. **/
    private List relations;
    /** A collection of SQLTables used in the Query. **/
    private ZXCollection tables;
    /** Whether to perform a OUTER join. **/
    private boolean outerJoin;
    /** Whether there is at least one OUTER join for the Query. **/
    private boolean atLeastOneOuterJoin;
    /** Whether to use the DISTINCT sql keyword. **/
    private boolean distinct;
    
    //------------------------ Constructors  
    
    /**
     * @see ZXObject#ZXObject()
     */
    public SQLQuery() {
        super();
        
        // Init the various collections :
        setColumns(new ZXCollection());
        setTables(new ZXCollection());
        setRelations(new ArrayList());
    }
    
    //------------------------ Getters and Setters  
    
    /**
     * @return Returns the atLeastOneOuterJoin.
     */
    public boolean isAtLeastOneOuterJoin() {
        return atLeastOneOuterJoin;
    }
    
    /**
     * @param atLeastOneOuterJoin The atLeastOneOuterJoin to set.
     */
    public void setAtLeastOneOuterJoin(boolean atLeastOneOuterJoin) {
        this.atLeastOneOuterJoin = atLeastOneOuterJoin;
    }
    
    /**
     * @return Returns the columns.
     */
    public ZXCollection getColumns(){
        return columns;
    }
    
    /**
     * @param columns The columns to set.
     */
    public void setColumns(ZXCollection columns) {
        this.columns = columns;
    }
    
    /**
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
     * @return Returns the outerJoin.
     */
    public boolean isOuterJoin() {
        return outerJoin;
    }
    
    /**
     * @param outerJoin The outerJoin to set.
     */
    public void setOuterJoin(boolean outerJoin) {
        this.outerJoin = outerJoin;
    }
    
    /**
     * @return Returns the query.
     */
    public String getQuery() {
        return query;
    }
    
    /**
     * @param query The query to set.
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * @return Returns the relations.
     */
    public List getRelations() {
        return this.relations;
    }
    
    /**
     * @param relations The relations to set.
     */
    public void setRelations(List relations) {
        this.relations = relations;
    }
    
    /**
     * @return Returns the tables.
     */
    public ZXCollection getTables() {
        return tables;
    }
    
    /**
     * @param tables The tables to set.
     */
    public void setTables(ZXCollection tables) {
        this.tables = tables;
    }

    //------------------------ Public methods
    
    /**
     * Add a table to the tables collection and make
     * sure that we do not accept duplicates (this 
     * can happen as a result of wrong parameter
     * or as a result of resolve FK).
     * 
     * @param pobjBO The BO to add
     * @throws ZXException If addTable fails
     */
    public void addTable(ZXBO pobjBO) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
        }

        try {
            
            /**
             * Initialise the table object
             */
            SQLTable objTable = new SQLTable();
            objTable.setName(pobjBO.getDescriptor().getTable());
            objTable.setAlias(getZx().sql.tableName(pobjBO, zXType.sqlObjectName.sonAlias));
            objTable.setBo(pobjBO);
            
            /**
             * And add to collection
             */
            getTables().put(objTable.getAlias(), objTable);

        } catch (Exception e) {
            getZx().trace.addError("Failed to : Add BO to the table collection ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Add relation to the collection.
     * 
     * @param pobjBO From Business Object
     * @param pobjOtherBO To Business Object
     * @param pobjAttr The attribute which allow for the join, this might have to be the attribute group in the future
     * @param pblnNTo1 Is it a one to many relationship ?
     */
    public void addRelation(ZXBO pobjBO, ZXBO pobjOtherBO, Attribute pobjAttr,
            				    boolean pblnNTo1) {
        addRelation(pobjBO, pobjOtherBO, pobjAttr, pblnNTo1, false);
    }    
    
    /**
     * Add relation to the collection.
     * 
     * @param pobjBO The from business object.
     * @param pobjOtherBO The to business object.
     * @param pobjAttr The attribute which allow for the join, this might have to be the attribute group in the future
     * @param pblnNTo1 Is it a one to many relationship ?
     * @param pblnForceOuter Whether to force the outer join or not? Optional, default it false
     */
    public void addRelation(ZXBO pobjBO, ZXBO pobjOtherBO, Attribute pobjAttr,
                            boolean pblnNTo1, boolean pblnForceOuter) {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBOr", pobjBO);
            getZx().trace.traceParam("pobjOtherBO", pobjOtherBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pblnNTo1", pblnNTo1);
            getZx().trace.traceParam("pblnForceOuter", pblnForceOuter);
        }

        try {
            SQLRelation objRelation = new SQLRelation();
            objRelation.setLeft(pobjBO);
            objRelation.setRight(pobjOtherBO);
            objRelation.setAttrName(pobjAttr.getName());
            objRelation.setNTo1(pblnNTo1);
            
            /**
             * See if we have to determine whether it is an inner or outer join
             * DGS 17JAN2003: If already had one outer join, this join must also be outer:
             */
            objRelation.setInner(!isAtLeastOneOuterJoin() && !pblnForceOuter && !pobjAttr.isOptional());
            
            /**
             * Inner join is required if:
             * - FK attribute is not optional
             * - If outerjoin is not requested
             */
            
            /**
             * If this is not an inner join, we have found at least one outer join
             */
            if (!objRelation.isInner()) {
                setAtLeastOneOuterJoin(true);
            }
            
            /**
             * And add to collection
             */
            getRelations().add(objRelation);
            
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add column to columns collection.
     * 
     * Reviewed for V1.5:48
     * 
     * @param pobjBO The BO that the column belongs to.
     * @param pobjAttr The attribute to which the column belongs
     * @throws ZXException Thrown if addColumn fails
     */
    public void addColumn(ZXBO pobjBO, Attribute pobjAttr) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
        }

        try {
        	/**
        	 * Ignore when no column as this will result in incorrect SQL later-on
        	 */
        	if (StringUtil.len(pobjAttr.getColumn()) == 0) {
        		return;
        	}
        	
            SQLColumn objColumn = new SQLColumn();
            objColumn.setBo(pobjBO);
            objColumn.setAttrName(pobjAttr.getName());
            objColumn.setName(pobjAttr.getColumn());
            objColumn.setAlias(getZx().sql.columnName(pobjBO, pobjAttr, zXType.sqlObjectName.sonAlias));
           
            /**
             * And add to collection to the Collumns collection
             */
            getColumns().put(objColumn.getAlias(), objColumn);
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Add column to columns collection.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pobjAttr = " + pobjAttr);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * See if there is a path from one BO to another; if so, add the
     * appropriate relation object in the relation collection.
     * 
     * @param pobjBO The from Business object.
     * @param pobjOtherBO The to Business object.
     * @param pblnNTo1 Indicates whether to look for pobjBO to pobjOtherBO (true) or
     *                 OtherBO to BO (false)
     *                 false when no relation is found
     * @return Returns if a relationship is found
     * @throws ZXException Thrown if resolveJoin fails
     */
    public boolean resolveJoin(ZXBO pobjBO, ZXBO pobjOtherBO, boolean pblnNTo1) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjOtherBO", pobjOtherBO);
            getZx().trace.traceParam("pblnNTo1", pblnNTo1);
        }
        
        /**
         * Set foundRelation to false (will only be set to true when a relation has been found)
         */          
        boolean resolveJoin = false; // Whether we have found a join
        
        try {
            
            /**
             * Get name of other object (so we do not have to generate alias in loop)
             */
            String strOtherBO = null;
            if ( !StringUtil.isEmpty(pobjOtherBO.getDescriptor().getAlias()) ) {
                strOtherBO = pobjOtherBO.getDescriptor().getAlias().toLowerCase();
            } else {
                strOtherBO = pobjOtherBO.getDescriptor().getName().toLowerCase();
            }
            
            /**
             * Loop over attributes
             */
            Attribute objAttr;
            Iterator iter = pobjBO.getDescriptor().getAttributes().iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if ((StringUtil.isEmpty(objAttr.getForeignKeyAlias())?objAttr.getForeignKey():objAttr.getForeignKeyAlias()).equalsIgnoreCase(strOtherBO) ) {    
                    /**
                     * We have found a connection
                     * Tell the calling routine that we have done so
                     */
                    resolveJoin = true;
                    
                    /**
                     * Set the alias if a foreignKeyAlias has been provided
                     */
                    if (!StringUtil.isEmpty(objAttr.getForeignKeyAlias())) {
                        pobjOtherBO.getDescriptor().setAlias(objAttr.getForeignKeyAlias());
                    }
                    
                    /**
                     * Add the relation to the collection
                     */
                    addRelation(pobjBO, pobjOtherBO, objAttr, pblnNTo1);
                }
            }
            
            return resolveJoin;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Revolve joins.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pobjOtherBO = " + pobjOtherBO);
                getZx().log.error("Parameter : pblnNTo1 = " + pblnNTo1);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
            return resolveJoin;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(resolveJoin);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Resolve a foreign key; this means loading the BO in question and processing all the
     * label attributes. This can be called recursively.
     * 
     * @param pobjBO The business object to resolke the foriegn keys for.
     * @param pobjAttr The attribute to resolve.
     * @param pblnInner Whether to use inner joins.
     * @param pintLevel Level, as this can be called
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if resolvefk fails.
     */
    public zXType.rc resolvefk(ZXBO pobjBO, Attribute pobjAttr, boolean pblnInner, int pintLevel) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("pblnInner", pblnInner);
            getZx().trace.traceParam("pintLevel", pintLevel);
        }
        
        zXType.rc resolvefk = zXType.rc.rcOK;
        
        try {
            boolean blnInner = false;
            
            /**
             * Get handle to BO related to this attribute
             */
            ZXBO objFKBO =  pobjBO.getValue(pobjAttr.getName()).getFKBO();
            if (objFKBO == null) {
                throw new Exception("Unable to get FK BO for attribute " + pobjAttr.getName());
            }
            
            /**
             * Do NOT include the FK BO in the query when it cannot be joined because of
             * different data-sources
             */
            if (!getZx().getDataSources().canDoDBJoin(pobjBO, objFKBO)) {
                return resolvefk;
            }
            
            /**
             * If we already have this table in table collection: do not bother to add it again....
             */
            if (getTables().get(getZx().sql.tableName(objFKBO, zXType.sqlObjectName.sonAlias)) == null) { 
            
	            /**
	             * Add to the table collection
	             */
	            addTable(objFKBO);
	            
	            /**
	             * If this is a recursive call, we have to be carefull: once a table has been
	             * joined as 'outer', all knock-on tables have to resolved as outer as well
	             * so when called recursively (pintLevel > 1) we will never overwrite inner=false
	             * (but may overwrite inner=true)
	             */
	            if (pintLevel <= 1) {
	                if (isOuterJoin()) {
	                    blnInner = false;
	                } else {
	                    blnInner = !pobjAttr.isOptional();
	                }
	            } else {
	                if (blnInner) {
	                    blnInner = !pobjAttr.isOptional();
	                }
	            }
	            
	            /**
	             * Normally addRelation will figure out whether we are dealing with an
	             * outer or inner join, for this one however we know better!
	             */
	            addRelation(pobjBO, objFKBO, pobjAttr, false, !blnInner);
	            
	            /**
	             * BD7APR04 Important change: not that we execute the following
	             * code regardless of whether we already had this BO included yes
	             * or no. The reason is that we may list a BO explicitly in a BO
	             * collection but only select a hand-ful of columns. If this BO
	             * is also required for the resolve FK, we actually may need columns
	             * that have not been selected by the user.
	             * It does rely on addColumn to handle duplicate column names
	             * correctly
	             * 
	             * Now handle the columns of the label group (or the group that is required
	             * instead of the label group)
                 */
                AttributeCollection colAttr;
                if (StringUtil.len(pobjAttr.getFkLabelGroup()) > 0) {
                    colAttr = objFKBO.getDescriptor().getGroup(pobjAttr.getFkLabelGroup());
                } else {
                    colAttr = objFKBO.getDescriptor().getGroup("label");
                } // Has FK Label group override?
                
                if (colAttr == null) {
                	if (StringUtil.len(pobjAttr.getFkLabelGroup()) > 0) {
                    	throw new Exception("Unable to get FK label attribute group " + pobjAttr.getFkLabelGroup()); 
                	}
                	
                	throw new Exception("Unable to get retrieve group : label"); 
                	
                } // Resolved label group?
                
                Attribute objAttr;
                
                Iterator iter = colAttr.iterator();
                while (iter.hasNext()) {
                    objAttr = (Attribute) iter.next();
                    
                    /**
                     * If the attribute of also is a foreign key; do the recursive trick
                     */
                    if( StringUtil.len(objAttr.getForeignKey()) > 0 ) {
                        /**
                         * 23APR2003: If this attr has an FK alias, we must use an FK alias
                         * on the related object. Typically we have an alias because the same
                         * entity is linked twice, so we must continue up through FKs with each
                         * having its own linked FK entity. Otherwise the FK entity is only linked
                         * once, and will be wrong on the aliased relationship.
                         */
                        if (  StringUtil.len(objFKBO.getDescriptor().getAlias()) > 0 ) {
                            
                            /**
                             * BD15NOV04 - Generate a unique alias: the BO associated with this 
                             * attribute may already have been included if the original BO
                             * has a FK to itself (e.g. parent) and the label of the BO has
                             * a FK attribute in it
                             */
                            //if( StringUtil.len(objAttr.getForeignKeyAlias()) ==0) {
                            //    objAttr.setForeignKeyAlias(objFKBO.getDescriptor().getAlias() + pintLevel);
                            //}
                            
                            if (StringUtil.len(objAttr.getForeignKeyAlias()) == 0) {
                                objAttr.setForeignKeyAlias(objAttr.getColumn() + objFKBO.getDescriptor().getAlias() + (pintLevel + ""));
                            } else {
                                objAttr.setForeignKeyAlias(objAttr.getColumn() + (pintLevel + ""));
                            }
                            
                        }
                        resolvefk(pobjBO, objAttr, blnInner, pintLevel++);
                    }
                    
                    /**
                     * Now add the 'plain' column
                     */
                    addColumn(objFKBO, objAttr);
                }
            }
            
            return resolvefk;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Resolve fk", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pobjAttr = " + pobjAttr);
                getZx().log.error("Parameter : pblnInner = " + pblnInner);
                getZx().log.error("Parameter : pintLevel = " + pintLevel);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
            resolvefk = zXType.rc.rcError;
            return resolvefk;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
        
    }
    
    /**
     * Generate select clause based on the current tables / columns and relations collections.
     * 
     * @return Returns the SQL select clause
     * @throws ZXException Thrown if generateSelectClause fails
     */
    public String generateSelectClause() throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
        StringBuffer generateSelectClause = new StringBuffer();
		
		try {
		    /**
		     * Generate the column list
		     */
		    Iterator iter = getColumns().iterator();
		    SQLColumn objColumn;
		    while (iter.hasNext()) {
		        objColumn = (SQLColumn)iter.next();
		        
		        if (generateSelectClause.length() > 0) {
		            generateSelectClause.append(", ");
		        }
		        
		        generateSelectClause.append(getZx().sql.columnName(objColumn.getBo(), 
		                										   objColumn.getBo().getDescriptor().getAttribute(objColumn.getAttrName()), 
		                										   zXType.sqlObjectName.sonClause));    
		    }
		    
		    /**
		     * Prefix distinct if requested
		     * BD17DEC02 - Only from clause when no columns
		     */
		    if (getColumns().size() > 0) {
			    if (isDistinct()) {
			        generateSelectClause.insert(0,"DISTINCT ");
			    }
			    generateSelectClause.insert(0,"SELECT ");
		    }
		    
		    return generateSelectClause.toString();
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Generate select clause.", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    return generateSelectClause.toString();
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.returnValue(generateSelectClause);
		        getZx().trace.exitMethod();
		    }
		}
    }
    
    /**
     * Generate query based on the current tables / columns and relations collections.
     * 
     * @throws ZXException Thrown if generateSQL fails
     */
    public void generateSQL() throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
		try {
		    
		    query = generateSelectClause() + " " + generateFromClause();
		    
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Generate query.", e);
		    if (getZx().throwException) {
		    	throw new ZXException(e);
		    }
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.exitMethod();
		    }
		}        
    }
    
    /**
     * Generate from clause based on the current tables / columns and relations collections.
     * Reviewed for 1.5:1
     * 
     * @return Returns sql FROM clause
     * @throws ZXException Thrown if generateFromClause
     */
    public String generateFromClause() throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
        StringBuffer generateFromClause = new StringBuffer();
        DSHRdbms objDSHandler;
        		
		try {
            /**
             * Get data-source handler for first table
             * NOTE : If getTables is null we will have a null pointer exception.
             */
            objDSHandler = (DSHRdbms)((SQLTable)getTables().iterator().next()).getBo().getDS();
            
            zXType.databaseType dbType = objDSHandler.getDbType();
            
		    /**
		     * FROM 
		     * 	a,b,c
		     */
		    if(dbType.equals(zXType.databaseType.dbOracle) && !isAtLeastOneOuterJoin()) {
		        
		        if (getTables().size() == 0) {
		            /**
		             * If there is only one table, very simple
		             */
		            generateFromClause.append(" FROM ");
		            generateFromClause.append(getZx().sql.tableName(((SQLTable)getTables().iterator().next()).getBo(), null));
		            generateFromClause.append(" WHERE (1 = 1) ");
		            
		        } else {
		            /**
		             * For multiple tables slightly more complex
		             */
		            SQLTable objTable;
		            
		            Iterator iter = getTables().iterator();
		            while(iter.hasNext()) {
		                objTable = (SQLTable)iter.next();
		                
		                // Add the comma's
		                if (generateFromClause.length() > 0) {
		                    generateFromClause.append(", ");
		                }
		                generateFromClause.append(getZx().sql.tableName(objTable.getBo(), zXType.sqlObjectName.sonClause));
                        
		            }             
		            generateFromClause.insert(0," FROM "); // insert the from at the beginning
		            generateFromClause.append(" WHERE "); // append the where at the end
		            
		            /**
		             * a.b_fk = b.pk
		             * AND b.c_fk = c.pk
		             * 
		             * For Oracle outer join, add the (+) at strategic places
		             * 
		             * NOTE : This needs to end in AND or be empty.
		             */
		            SQLRelation objRelation;
		            
		            iter = getRelations().iterator(); // reuse the last iterator
		            while (iter.hasNext()) {
		                objRelation = (SQLRelation)iter.next();
		                
		                /**
		                 * Left side : 
		                 */
		                String strTmp = getZx().sql.columnName(objRelation.getLeft(), 
		                        							   objRelation.getLeft().getDescriptor().getAttribute(objRelation.getAttrName()), 
		                        							   zXType.sqlObjectName.sonName);
		                /**
		                 * If Oracle and outer join: add (+) to first parameter if this is an Nto1 (ie right to left)
		                 */
		                if (dbType.equals(zXType.databaseType.dbOracle)
		                    && !objRelation.isInner() && objRelation.isNTo1()) {
		                    /**
		                     * Make sure that we have never done an outer join to this table before
		                     */
		                    SQLTable objJoinTable = (SQLTable)getTables().get(getZx().sql.tableName(objRelation.getLeft(), 
                                                                                                    zXType.sqlObjectName.sonAlias));
		                    if (!objJoinTable.isDoneOuterJoinYet()) {
		                        /**
		                         * Do outer join and mark table to avoid using it in outer join 
                                 * ever again
		                         */
		                        objJoinTable.setDoneOuterJoinYet(true);
		                        strTmp = strTmp + " (+) ";
		                    }
		                }
		                
		                /**
		                 * If both the left and right are String.
		                 */
		                boolean blnIsString = false; // Save some type :)
		                
		                Attribute objLeftAttr = objRelation.getLeftAttributeByAttrName();
		                Attribute objRightAttr = objRelation.getRightAttributeByAttrName();
		                
		                if (objLeftAttr.getDataType().pos != zXType.dataType.dtString.pos
		                    && objRightAttr.getDataType().pos == zXType.dataType.dtString.pos        
		                    ) {
		                    blnIsString = true;
                            /**
                             * Wrap with db toString.
                             */
	                        strTmp = SQL.dbStringWrapper(dbType, 
	                        							 strTmp, 
	                        							 objRightAttr.getLength());
                            
		                }
		                
		                generateFromClause.append(strTmp);
		                generateFromClause.append(" = ");
		                
		                /**
		                 * Right  side : 
		                 */
		                strTmp = getZx().sql.columnName(
                                             objRelation.getRight(), 
		                        			 objRelation.getRight().getDescriptor().getAttribute(objRelation.getRight().getDescriptor().getPrimaryKey()), 
		                        			 zXType.sqlObjectName.sonName
                                                        );
		                /**
		                 * If Oracle and outer join: add (+) to second parameter
                         * if this is an Nto1 (ie left to right)
		                 */
		                if (dbType.equals(zXType.databaseType.dbOracle)
		                    && !objRelation.isInner() 
                            && !objRelation.isNTo1()) {
		                    /**
		                     * Make sure that we have never done an outer join to this table before
		                     */
		                    SQLTable objJoinTable = (SQLTable)getTables().get(getZx().sql.tableName(objRelation.getRight(), 
                                                                                                    zXType.sqlObjectName.sonAlias));
		                    if (!objJoinTable.isDoneOuterJoinYet()) {
		                        /**
		                         * Do outer join and mark table to avoid using it in outer join 
                                 * ever again
		                         */
		                        objJoinTable.setDoneOuterJoinYet(true);
		                        strTmp = strTmp + " (+) ";
		                    }
		                }
		                
		                /**
		                 * If both the left and right are String.
		                 */
		                if (blnIsString) {
	                        strTmp = SQL.dbStringWrapper(dbType,
	                        							 strTmp,
	                        							 objLeftAttr.getLength());
		                }

		                generateFromClause.append(strTmp);
		                generateFromClause.append(" AND "); // End on a AND
		            }
		            
		            // Append with a always true clause :
		            generateFromClause.append(" (1 = 1) ");
		        }
		        
		    } else {
		        /**
		         * The complex one is the Acces outer join 
		         * (a INNER JOIN b ON a.b_fk = b.pk (LEFT JOIN c ON b.c_fk = c.pk))
		         */
		        if (getRelations().size() == 0) {
		            
		            generateFromClause.append(" FROM ");
		            SQLTable objSQLTable = (SQLTable)getTables().iterator().next();
		            /**
		             *  BD4AUG04 Presumed to be a bug, will never work like this
		             */
		            generateFromClause.append(getZx().sql.tableName(objSQLTable.getBo(), zXType.sqlObjectName.sonClause));
		            generateFromClause.append(" WHERE (1 = 1) ");
		            
		        } else {
		            
		            /**
		             * Keep track of first join as this is different
		             * than all subsequent ones
		             */
		            boolean blnFirstOne = true;
		            
		            // Temp values : 
		            String strTmp = null; // StringBuffer 
		            SQLTable objJoinTable = null;
		            boolean blnFoundOuter = false;
		            String strColumnLeft = null;
		            String strColumnRight = null;
		            
		            String strFromClause = null;
		            
		            SQLRelation objRelation;
		            
		            Iterator iter = getRelations().iterator();
		            while(iter.hasNext()) {
		                strTmp = ""; // Reset to nothing 
	                    objRelation = (SQLRelation)iter.next();
	                    
	                    if (blnFirstOne) {
	                        strTmp = getZx().sql.tableName(objRelation.getLeft(), zXType.sqlObjectName.sonClause);
	                    }
	                    
	                    /**
	                     * Process table
	                     */
	                    if (objRelation.isNTo1() && !blnFirstOne) {
	                        objJoinTable = (SQLTable)getTables().get(getZx().sql.tableName(objRelation.getLeft(), zXType.sqlObjectName.sonAlias));
	                    } else {
	                        objJoinTable = (SQLTable)getTables().get(getZx().sql.tableName(objRelation.getRight(), zXType.sqlObjectName.sonAlias));
	                    }
	                    
	                    if (!objJoinTable.isDoneOuterJoinYet()) { 
	                        /**
	                         * Mark table as used to avoid using it again
	                         * ever again
	                         */
	                        objJoinTable.setDoneOuterJoinYet(true);
	                        
	                        /**
	                         * What type of Join ?
	                         */
	                        if (objRelation.isInner() && !isOuterJoin() && !blnFoundOuter) {
	                            strTmp = strTmp + " INNER JOIN ";
	                        } else {
	                            /**
	                             * Use right join for first table
	                             */
	                            if (objRelation.isNTo1() && blnFirstOne) {
	                                strTmp = strTmp + " RIGHT JOIN ";
	                            } else {
	                                strTmp = strTmp + " LEFT JOIN ";
	                            }
	                            blnFoundOuter = true;
	                        }
	                        
	                        
	                        if (objRelation.isNTo1() && !blnFirstOne) {
	                            strTmp = strTmp + getZx().sql.tableName(objRelation.getLeft(), zXType.sqlObjectName.sonClause);
	                        } else {
	                            strTmp = strTmp + getZx().sql.tableName(objRelation.getRight(), zXType.sqlObjectName.sonClause);
	                        }
	                        
	                        strTmp = strTmp + " ON ";
	                        
	                        Attribute objLeftAttr =  objRelation.getLeftAttributeByAttrName();
	                        Attribute objRightAttr =  objRelation.getRightAttributeByAttrName();
	                        
	                        strColumnLeft = getZx().sql.columnName(objRelation.getLeft(), objLeftAttr, zXType.sqlObjectName.sonName);
	                        strColumnRight = getZx().sql.columnName(objRelation.getRight(), objRightAttr, zXType.sqlObjectName.sonName);
	                        
	                        if (objLeftAttr.getDataType().pos != zXType.dataType.dtString.pos 
	                            && objRightAttr.getDataType().pos == zXType.dataType.dtString.pos) {
	                            strColumnLeft = SQL.dbStringWrapper(dbType,
	                            									strColumnLeft,
	                            									objRightAttr.getLength());
	                            
	                        } else if (objLeftAttr.getDataType().pos == zXType.dataType.dtString.pos 
	                                   && objRightAttr.getDataType().pos != zXType.dataType.dtString.pos) {
	                            strColumnRight = SQL.dbStringWrapper(dbType,
	                            								 	 strColumnRight,
	                            								 	 objLeftAttr.getLength());
	                            
	                        }
	                        strTmp = strTmp + strColumnLeft + " = " + strColumnRight;
	                        
	                        if (strFromClause == null) {
	                            strFromClause = strTmp;
	                        } else {
	                        	if (dbType.equals(zXType.databaseType.dbHsql)) {
		                            strFromClause = strFromClause + " " + strTmp;
	                        	} else {
		                            strFromClause = "(" + strFromClause + ") " + strTmp;
	                        	}
	                        }
	                        
	                        blnFirstOne = false;
	                    }
		            }		            
                    strFromClause = " FROM " + strFromClause + " WHERE (1=1) ";
                    
                    generateFromClause.append(strFromClause);
		        }
		    }
		    
		    /**
		     * DGS04FEB2004: Where clause for optional group of attributes that are constant values
		     */
		    String strTmp;
		    SQLTable objTable;
		    
		    Iterator iter = getTables().iterator();
		    while(iter.hasNext()) {
		        objTable = (SQLTable)iter.next();
                
		        strTmp = getZx().getSql().whereDataConstants(objTable.getBo());
		        if (StringUtil.len(strTmp) > 0) {
		            generateFromClause.append(" AND (").append(strTmp).append(") ");
		        }
		    }
		    
		    return generateFromClause.toString();
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Generate from clause.", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    return generateFromClause.toString();
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.returnValue(generateFromClause);
		        getZx().trace.exitMethod();
		    }
		}
    }
    
    /**
     * Generate list of tables for from clause based on the current tables / columns and 
     * relations collections.
     *  
     * @return Returns the from clause using the tables collection.
     * @throws ZXException Thrown if generateFromClauseTables fails
     */
    public String generateFromClauseTables() throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
        StringBuffer generateFromClauseTables = new StringBuffer();
        
		try {
		    
		    /**
		     * FROM 
		     * 	a, b, c
		     */
		    SQLTable objTable;
		    Iterator iter = getTables().iterator();
		    while (iter.hasNext()) {
		        objTable = (SQLTable)iter.next();
		        
		        if (generateFromClauseTables.length() > 0) {
		            generateFromClauseTables.append(", ");
		        }
		        
		        generateFromClauseTables.append(getZx().sql.tableName(objTable.getBo(), zXType.sqlObjectName.sonClause));
		    }
		    generateFromClauseTables.append(' '); // end with a space
		    
		    generateFromClauseTables.insert(0, " FROM"); // insert the from
		    
		    return generateFromClauseTables.toString();
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Generate list of tables for from clause.", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    return generateFromClauseTables.toString();
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.returnValue(generateFromClauseTables);
		        getZx().trace.exitMethod();
		    }
		}
    }
    
    //------------------------ Objects overloaded methods
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer toString = new StringBuffer(32);
        
        toString.append("Dump of query \n\n");  
        toString.append("Tables : \n\n");
        
        Iterator iter;
        ZXBO objBO;
        String strBOName = "";
        if (getTables() != null) {
            SQLTable objTable;
            iter = getTables().iterator();
            while(iter.hasNext()) {
                objTable = (SQLTable)iter.next();
                
                objBO = objTable.getBo();
                if (objBO != null) {
                	strBOName = objBO.getDescriptor().getName();
                }
                
                toString.append("\t Table : " + objTable.getName());
                toString.append("\n\t Alias : " + objTable.getAlias());
                toString.append("\n\t BO : " + strBOName);
            }
        }
        
        if (getRelations() != null) {
	        toString.append("Relations : \n\n");
	        
	        SQLRelation objRelation;
	        iter = getRelations().iterator();
	        while(iter.hasNext()) {
	            objRelation = (SQLRelation)iter.next();
	            
                objBO = objRelation.getLeft();
                if (objBO != null) {
                	strBOName = objBO.getDescriptor().getName();
                }
	            toString.append("\t Left : " + strBOName);
	            
                objBO = objRelation.getRight();
                if (objBO != null) {
                	strBOName = objBO.getDescriptor().getName();
                }
	            toString.append("\n\t Right : " + strBOName);
	            
	            toString.append("\n\t Via : " + objRelation.getAttrName());
	            toString.append("\n\t 1 To n : " + (objRelation.isNTo1()?"Yes":"No"));
	        }
        }
        
        return toString.toString();
    }
}