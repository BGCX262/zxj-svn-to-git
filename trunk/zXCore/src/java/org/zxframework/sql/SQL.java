/*
 * Created on Feb 24, 2004 by Michael Brewer
 * $Id: SQL.java,v 1.1.2.39 2006/07/17 16:40:06 mike Exp $
 */
package org.zxframework.sql;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSRS;
import org.zxframework.datasources.DSWhereClause;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.DateUtil;
import org.zxframework.util.StringUtil;

/**
 * SQL generation class.
 * 
 * <p><b>NOTE :</b> As this object will never be stored in a ZXCollection, it does not need a key identifier.</p> 
 * 
 * <p>Changelog from ZX vb framework :</p>
 * 
 * <pre>
 * 
 * Change    : BD6DEC02
 * Why       : Increased length of Oracle id before we start to use
 *             the checksum trick from 15 to 25. Also maximum length is now
 *             a constant
 * 
 * Change    : DGS22JAN2003
 * Why       : Change to selectQuery to support requirements of the new QueryDef
 *             object. Should not make any difference to existing users of this
 *             function but allows consituent parts of the select query to be
 *             obtained individually.
 * 
 * Change    : DGS28JAN2003
 * Why       : Further adjustment to selectQuery for QueryDef - don't include
 *             WHERE at all if no join etc.
 * 
 * Change    : BD10MAR03
 * Why       : Implemented case-insenstitive query handling for Oracle
 * 
 * Change    : BD21MAR03
 * Why       : Fixed stupid bug introduced with case insensitive searching with
 *             starts with singleWhereCondition
 * 
 * Change    : BD7APR03
 * Why       : Fixed bug in makeCaseInsensitive where there was no else clause
 *             for non-Oracle DB so the function always returned an empty string
 * 
 * Change    : 24APR2003
 * Why       : In singleWhereCondition, don't use makeCaseInsensitive when the rh value
 *             is null - it causes an error in Oracle 'IS NULL UPPER()'
 * 
 * Change    : DGS29APR2003
 * Why       : Created new public function dbGetStrValue but it is basically code pulled out
 *             of existing dbStrValue (which now calls the new function too) so that external
 *             code can use it to get the value of an attribute for use in a DB query.
 * 
 * Change    : DGS07MAY2003
 * Why       : In columnName, for 'Pure' names, previously put square brackets around
 *             Access/SQLServer column names but does not now.
 * 
 * Change    : BD18MAY03
 * Why       : Added processWhereGroup to introduce support for flexible
 *             where groups
 * 
 * Change    : BD20AUG03
 * Why       : Added support for special where group in where condition
 * 
 * Change    : BD4SEP03
 * Why       : Added support for full-expression where groups
 * 
 * Change    : DGS01OCT2003
 * Why       : In QSWhereClause, if cannot use a QS attr, was repeating the previous
 *             attr's where clause (harmlessly but pointlessly)
 * 
 * Change    : BD28NOV03
 * Why       : In processWhereGroup always wrap the result in ( and ) to make sure
 *             that an 'or' does not catch us out
 * 
 * Change    : DGS16JAN2004
 * Why       : In processFullExpressionWhereGroup, fixed minor error where 'not equals'
 *             was "!" rather than "!=" 
 * 
 * Change    : DGS04FEB2004
 * Why       : New method 'whereDataConstants' for Data Constant attribute group.
 * 
 * Change    : DGS27FEB2004
 * Why       : - In QSWhereClause, if the attr is a 'soundex', convert the QS value to its
 *                soundex equivalent and compare equal to that
 *             - In processWhereGroup, change for efficiency.
 * 
 * Change    : BD1MAR04
 * Why       : Very detailed change when joining tables; see comment marked
 *             with BD1MAR04 in buildQuery
 * 
 * Change    : DGS05MAR2004
 * Why       : In QSWhereClause, if the attr is a 'soundex' but shorter than the full size
 *             allowed for a soundex, truncate it and compare 'like' not 'equal'. Also, and
 *             importantly, use the new 'simplifiedSoundex' function.
 * 
 * Change    : CBM17MAR04
 * Why       : Added support for SQL Server dates
 * 
 * Change    : CBM17MAR04
 * Why       : Changed boolean return type for SQL
 * 
 * Change    : DGS22APR2004
 * Why       : In processFullExpressionWhereGroup, if the parsing gets stuck in a loop, exit.
 * 
 * Change    : BD5MAY04
 * Why       : In processFullExpressionWhereGroup, support spaces in expression
 * 
 * Change    : BD16MAY04
 * Why       : In whereCondition we used to support - for <> but this is now
 *             changed to ! or <> to support the - for order by group
 * 
 * Change    : BD9JUN04
 * Why       : Added concurrencyControl function
 * 
 * Change    : BD3AUG04
 * Why       : In QS where clause; if qs value starts with = use as-is
 *             and do not try to be clever with soundex and likes....
 * 
 * Change    : BD10AUG04
 * Why       : Fixed bug in tableName; now also take into consideration the
 *             alias when deciding whether to cache or not
 *                
 * Change    : MB14OCT2004
 * Why       : Added mySql DB type
 *  
 * Change    : MB01NOV2004
 * Why       : Fix the mysql data from %Y-%m-%D to %Y-%m-%d
 *  
 * Change    : BD10DEC04 - V1.4:5
 * Why       : Completely redone extended where conditions to support advanced features
 *  
 * Change    : BD10DEC04 - V1.4:36
 * Why       : Fixed problem in processFullExpressionWhereGroup; trailing spaces
 *             for special values (eg #true, #user) made that the special value
 *             was no longer recognised
 *                 
 * Change    : DGS21FEB2005 - V1.4:50
 * Why       : In concurrencyControlWhereCondition when including zXUpdtId in the query use
 *             the rightmost 9 characters, not the leftmost as was the case, for maximum
 *             chance of uniqueness
 *              
 * Change    : BD25MAR05 - V1.5:1
 * Why       : Added support for data-sources / channels
 *
 * Change    : DGS26APR2005 - V1.4:71 / V1.5:13
 * Why       : New function dbRowLimit to generate db-specific row limit for select statement
 *             used to support record-set paging
 *
 * Change    : BD18MAY05 - V1.5:14
 * Why       : DbRowLimit (see V1.5:13) needs to have additional parameter (dsHandler) as we
 *             are no longer allowed to tell the database type by looking at zX.db.dbType
 *
 * Change    : BD28APR2006 - V1.5:95
 * Why       : In orderByClause now support the special attribute tag zXOrderDesc which has been
 *             set in descriptor.getGroup when the \- construct is being used
 *
 * Change    : V1.5:95 - BD30APR06
 * Why       : Bug in dbGetStrValue; when in V1.5 mode and when using datasources, this routine
 *             was not aware of the data source and thus not of the dateFormat setting; still
 *             not 100% right but at least works now with Access
 *             
 * Change    : V1.5:98 - MB2JUN06
 * Why       : Added support for where condition in notAssociatedWithClause
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class SQL extends ZXObject {

    //------------------------ Members
    
    /**
     * Cache the last recieved name :
     * However if you try to access the samed table over and over again...
     */
    /** The last cached business object used in table name method. **/
    private static ZXBO objLastTable;
    /** The last cached table name used in the table name method. **/
    private static String strLastTableName;
    /** The last type of SQL Object type used in the table name method. **/
    private static zXType.sqlObjectName enmLastTableType;
    /** The last alias used **/
    private static String strLastAlias;
    
    /** The maximum field size for a oracle id. **/
    private static int MAX_LENGTH_ORA_ID = 25;
    
    /** Used to stored the exit position of the processFullExpressionWhereGroup */
    // private int intChars = 0;
    
    //------------------------ Constructors
	
    /**
     * Default constructor. 
     * 
     * <pre>
     * 
     * Assumes : zX.DB has already been initialised to get access to the
     * database connection. 
     * </pre>
     */
    public SQL() {
        super();
    }
    
    //------------------------ Public methods
    
	/**
	 * Generate load query.
	 * 
	 * <pre>
	 * 
	 * NOTE : This calls : loadQuery(pobjBO, pstrAttributeGroup,false,false);
	 * </pre>
	 * 
	 * @param pobjBO The business object to use.
	 * @param pstrAttributeGroup Attributes in select clause
	 * @return Returns sql query to execute
	 * @throws ZXException Thrown if loadQuery fails
	 */
    public String loadQuery(ZXBO pobjBO, String pstrAttributeGroup) throws ZXException {
        return loadQuery(pobjBO, pstrAttributeGroup,false,false);
    }
    
	/**
	 * Generate load query.
	 * 
	 * @param pobjBO The business object to load the query for.
	 * @param pstrAttributeGroup Attributes in select clause
	 * @param pblnResolveFK Do we wish to join in the foreign tables?
	 * 			  			Optional. Default is false.
	 * @param pblnPureNames Use pure names only (ie no aliases) (is mutually exclusive with resolveFK)
	 * 			  			Optional. Default is false.	
	 * @return Returns sql query to execute
	 * @throws ZXException Thrown if loadQuery fails
	 */
    public String loadQuery(ZXBO pobjBO, 
            				String pstrAttributeGroup, 
            				boolean pblnResolveFK, 
            				boolean pblnPureNames) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrAttributeGroup", pstrAttributeGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
            getZx().trace.traceParam("pblnPureNames", pblnPureNames);
        }
        
        StringBuffer loadQuery = new StringBuffer(32);

        try {
            /**
             *  For performance reasons we have 2 variations; the lite and the heavy one
             */
            if(pblnResolveFK) {
               loadQuery.append(selectQuery(pobjBO, pstrAttributeGroup, pblnResolveFK));
               
            } else {
                /**
                 * Get attribute group. For auditable BOs and not being called
                 * recursively, add the audit columns
                 * BD9JUN04 - Now concurrency control
                 */
                AttributeCollection colAttr = null;
                if(pobjBO.getDescriptor().isConcurrencyControl()) {
                    colAttr = pobjBO.getDescriptor().getGroup(pstrAttributeGroup + ",~");
                } else {
                    colAttr = pobjBO.getDescriptor().getGroup(pstrAttributeGroup);
                }
                if (colAttr == null) {
                    throw new Exception("Unable to get attributegroup");
                }
                
                Attribute objAttr;
                Iterator iter = colAttr.iterator();
                while(iter.hasNext()) {
                    objAttr = (Attribute)iter.next();
                    if (StringUtil.len(objAttr.getColumn()) > 0) {
	                    /**
	                     * Only append if there are more elements to add : 
	                     */
	                    if(loadQuery.length() > 1) {
	                        loadQuery.append(", ");
	                    }
                        if(pblnPureNames) {
                            loadQuery.append(columnName(pobjBO,objAttr, zXType.sqlObjectName.sonName));
                        } else {
                            loadQuery.append(columnName(pobjBO,objAttr, zXType.sqlObjectName.sonClause));
                        }
                    }
                }
                
                loadQuery.insert(0,"SELECT "); // Insert the SELECT at the beginning
                
                if(pblnPureNames) {
                    loadQuery.append(" FROM ").append(tableName(pobjBO, zXType.sqlObjectName.sonName));
                } else {
                    loadQuery.append(" FROM ").append( tableName(pobjBO, zXType.sqlObjectName.sonClause));
                }
                loadQuery.append(" WHERE (1 = 1) ");
            }
            
            return loadQuery.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate load query ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrAttributeGroup = " + pstrAttributeGroup);
                getZx().log.error("Parameter : pblnResolveFK = " + pblnResolveFK);
                getZx().log.error("Parameter : pblnPureNames = " + pblnPureNames);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return loadQuery.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(loadQuery);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate select query for one or more business objects.
     * 
     * <pre>
     * 
     * NOTE : This calles : selectQuery(
     * 	new ZXBO[]{pobjBO}, new String[]{pobjAttributeGroup} , new boolean[]{pblnResolveFK}, 
     * 	false, false, false, false, false);
     * </pre>
     * 
     * @param pobjBO Collection of ZXBO's
     * @param pobjAttributeGroup The matching AttributeGroup's
     * @param pblnResolveFK The matching boolean resolve fk flags
     * @return Returns a sql query
     * @throws ZXException Thrown if selectQuery fails
     */
    public String selectQuery(ZXBO pobjBO, 
                              String pobjAttributeGroup, 
                              boolean pblnResolveFK) throws ZXException {
        return selectQuery(new ZXBO[]{pobjBO}, 
                           new String[]{pobjAttributeGroup} , 
                           new boolean[]{pblnResolveFK},
                           false, false, false, false, false);
    }
    
    /**
     * Generate select query for one or more business objects.
     * 
     * <pre>
     * 
     * NOTE : This calls selectQuery(pobjColBO, pobjColAttributeGroup, pblnColResolveFK, false, false, false, true, false)
     * </pre>
     * 
     * @param pobjColBO Collection of ZXBO's
     * @param pobjColAttributeGroup Collection of matching AttributeGroup's
     * @param pblnColResolveFK A Collection of matching boolean resolve fk flags
     * @return Returns a sql query
     * @throws ZXException Thrown if selectQuery fails
     */    
    public String selectQuery(ZXBO[] pobjColBO, 
                              String[] pobjColAttributeGroup,
                              boolean[] pblnColResolveFK) throws ZXException {
        return selectQuery(pobjColBO, 
                           pobjColAttributeGroup, 
                           pblnColResolveFK, 
                           false, false, false, false, false);
    }
    
    /**
     * Generate select query for one or more business objects.
     * 
     * @param pobjColBO Collection of ZXBO's
     * @param pobjColAttributeGroup Collection of matching AttributeGroup's
     * @param pblnColResolveFK A Collection of matching boolean resolve fk flags
     * @param pblnDistinct Whether to generate a DINTINCT select query. Optional,
     *                     default is false
     * @param pblnOuterJoin Whether todo a outer join or not. Optional, default is false
     * @param pblnFromClauseOnly Whether to only to the from clause. Optional, default is
     *                           false
     * @param pblnNoFromClause Whether to not do the from clause. Optional, default is false
     * @param pblnNoJoin Whether to do a join or not. Optional, default is false
     * 
     * @return Returns a sql query
     * @throws ZXException Thrown if selectQuery fails
     */
    public String selectQuery(ZXBO[] pobjColBO, 
                              String[] pobjColAttributeGroup,
                              boolean[] pblnColResolveFK,
                              boolean pblnDistinct, 
                              boolean pblnOuterJoin, 
                              boolean pblnFromClauseOnly,
                              boolean pblnNoFromClause, 
                              boolean pblnNoJoin) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjColBO", pobjColBO);
            getZx().trace.traceParam("pobjColAttributeGroup",pobjColAttributeGroup);
            getZx().trace.traceParam("pblnColResolveFK",pblnColResolveFK);
            getZx().trace.traceParam("pblnDistinct",pblnDistinct);
            getZx().trace.traceParam("pblnOuterJoin",pblnOuterJoin);
            getZx().trace.traceParam("pblnFromClauseOnly",pblnFromClauseOnly);
            getZx().trace.traceParam("pblnNoFromClause",pblnFromClauseOnly);
            getZx().trace.traceParam("pblnNoJoin",pblnNoJoin);
        }
        
        String selectQuery = "";
        
        try {

            if(pobjColBO.length != pobjColAttributeGroup.length 
                    || pobjColAttributeGroup.length != pblnColResolveFK.length ) {
                throw new Exception("Failes to supply the correct number of parameters");
            }

            SQLQuery objQry = buildQuery(pobjColBO, pobjColAttributeGroup, pblnColResolveFK, pblnDistinct, pblnOuterJoin);
            
            if (objQry == null) {
                throw new Exception("Unable to build query");
            }
            
            /**
             * Now generate query
             */
            if(pblnFromClauseOnly || pblnNoFromClause  || pblnNoJoin) {
                /**
                 * Not a standard generate of a query - we only want some portion of it. Note
                 * that some combinations are not logical - these are expected (the first line
                 * 
                 * *** shows what would result of none were set):
                 * NoFromClause     FromClauseOnly              NoJoin      E.G.
                 * ***   
                 * N             	N            		   	   	N     		SELECT cols FROM tables WHERE conditions
                 * Y             	N            			   	N     		SELECT cols
                 * N             	N            				Y     		SELECT cols FROM tables
                 * N            	Y            				N     		FROM tables WHERE conditions
                 * N            	Y            				Y     		FROM tables
                 */
                if (!pblnFromClauseOnly) {
                    /**
                     * We want the SELECT clause:
                     */
                    selectQuery = objQry.generateSelectClause();
                    
                    if (selectQuery == null) {
                        throw new Exception("Unable to generate select clause of query");
                    }
                }
                
                if (!pblnNoFromClause) {
                    /**
                     * We want the FROM clause - with or without join:
                     */
                    String strTmp = null;
                    if (pblnNoJoin) {
                        /**
                         * We don't want to join the tables, so just get table names:
                         */
                        strTmp = objQry.generateFromClauseTables();
                       if(strTmp == null) {
                           throw new Exception("Unable to generate from tables clause of query");
                       }
                    } else {
                        /**
                         * We want to join the tables as well as get table names:
                         */
                        strTmp = objQry.generateFromClause();
                        if (strTmp == null) {
                            throw new Exception("Unable to generate from clause of query");
                        }
                    }
                    selectQuery = selectQuery + " " + strTmp;
                }
            } else{
                /**
                 * Standard generate of a query - equivalent to this in the examples above:
                 * SELECT cols FROM tables WHERE conditions
                 */
                objQry.generateSQL();
                selectQuery = objQry.getQuery();
            }
            
            return selectQuery;
        } catch (Exception e) {
            getZx().trace.addError("Failed to :  Generate select query for one or more business objects ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjColBO = "+ pobjColBO);
                getZx().log.error("Parameter : pobjColAttributeGroup = " + pobjColAttributeGroup);
                getZx().log.error("Parameter : pblnColResolveFK = " + pblnColResolveFK);
                getZx().log.error("Parameter : pblnDistinct = " + pblnDistinct);
                getZx().log.error("Parameter : pblnOuterJoin = " + pblnOuterJoin);
                getZx().log.error("Parameter : pblnFromClauseOnly = " + pblnFromClauseOnly);
                getZx().log.error("Parameter : pblnNoFromClause = " + pblnNoFromClause);
                getZx().log.error("Parameter : pblnNoJoin = " + pblnNoJoin);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return selectQuery;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(selectQuery);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * This method will construct a clsSQLQry object that is a join on all the
     * objects that are listed.
     * 
     * <pre>
     * 
     * If resolve fk has been requested for an object,
     * the foreign key object table will be joined as well and this is done
     * recursively until the label attribute group does not contain any
     * foreign keys anymore.
     * 
     * E.g. If FK for order needs to be resolved and this contains order type and
     * product, the final query will have order, order type and product joined.
     * If the label attribute group of product contain product type, product
     * type will end up in the final query as well.
     * 
     * Much attention is paid to avoiding duplicate tables and column aliases, but
     * still some scenarios are possible that will not work.
     * Notoriously where an object has 2 foreign key attributes to the same
     * object (e.g. exchange rate with from ccy and to ccy). This can only be solved
     * by NOT doing a resolve FK and simply calling getQry with exchRate, ccy1, ccy2
     * where ccy2 has the alias set for the descriptor
     * </pre>
     * 
     * @param pobjColBO A collection of business object's
     * @param pstrColAttributeGroup A collection of AttributeGroups
     * @param pblnResolveFK A collection of resolve fk flags
     * @param pblnDistinct Whether to generate a DISTINCT query or not
     * @param pblnOuterJoin Whether to generate a outer join
     * @return Returns a SQLQuery object from where you can generate queries
     * @throws ZXException Thrown ig buildQuery fails
     */
    private SQLQuery buildQuery(ZXBO[] pobjColBO, 
                                String[] pstrColAttributeGroup,
                                boolean[] pblnResolveFK,
                                boolean pblnDistinct, 
                                boolean pblnOuterJoin) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjColBO",pobjColBO);
            getZx().trace.traceParam("pstrColAttributeGroup",pstrColAttributeGroup);
            getZx().trace.traceParam("pblnResolveFK",pblnResolveFK);
            getZx().trace.traceParam("pblnDistinct",pblnDistinct);
            getZx().trace.traceParam("pblnOuterJoin",pblnOuterJoin);
        }
        
        SQLQuery buildQuery = null;

        /**
         * Some local members :
         */
        ZXBO objBO = null;
        String strAttributeGroup = null;
        boolean blnResolveFK;
        
        try {
            AttributeCollection colAttr;
            Attribute objAttr;
            
            // Initialize SQLQuery
            buildQuery = new SQLQuery();
            
            buildQuery.setOuterJoin(pblnOuterJoin);
            buildQuery.setAtLeastOneOuterJoin(pblnOuterJoin);
            buildQuery.setDistinct(pblnDistinct);
            
            /**
             * Add all objects to the collection of tables;
             * this will ensure that we never add any duplicates as a
             * result of resolve fk
             */
            int intColBO = 0; // To handle empty arrays + performance :)
            for (int i = 0; i < pobjColBO.length; i++) {
                objBO = pobjColBO[i];
                
                /**
                 * Assumes that all objects in the parameter list are
                 * consecutive
                 */
                if (objBO == null) {
                    break;
                }
                intColBO++;
                
                /**
                 * Add the table associated with current object to the table
                 */
                buildQuery.addTable(objBO);
            }
            
            /**
             * We should at least have one table to generate a query from
             */
            if (buildQuery.getTables().size() == 0) {
                throw new Exception("Unable to build query (no tables found)");
            }
            
            /**
             * Loop over parameters
             */
            int i = 0; // Give visibility to the inner loop
            for (i = 0; i < intColBO; i++) {
                objBO = pobjColBO[i];
                strAttributeGroup = pstrColAttributeGroup[i];
                blnResolveFK = pblnResolveFK[i];
                
                /**
                 * Check the relationship between the objects directly 
                 * passed to this method
                 * Only check downwards so:
                 * order : product - order type will work
                 * order : type - product - order will not work
                 * This gives the user some control over what joins are actually requested
                 */
                ZXBO objOtherBO = null;
                for (int j = i+1; j < intColBO; j++) {
                    objOtherBO = pobjColBO[j];
                    
                    /**
                     * First try 1:N (e.g. order to product)
                     * Note that blnFoundRelation may be set in this function
                     */
                    boolean blnFoundRelation = false;
                    blnFoundRelation  = buildQuery.resolveJoin(objBO, objOtherBO, blnFoundRelation);
                    
                    /**
                     * If we have found a relation from product to order do not bother to
                     * look for a relation from order to product as well....
                     */
                    if (!blnFoundRelation) {
                        /**
                         * Than the N:1 (product to order)
                         * NOTE : I do not need to test outcome 
                         */
                        buildQuery.resolveJoin(objOtherBO, objBO, true);
                    }
                }
                
                /**
                 * Now handle the columns
                 */
                if ( StringUtil.len(strAttributeGroup) > 0 ) {
                    /**
                     * If the object is auditable, add the audit columns
                     * BD9JUN04 - Now Concurrency control
                     */
                    if (objBO.getDescriptor().isConcurrencyControl()) {
                        colAttr = objBO.getDescriptor().getGroup(strAttributeGroup + ",~");
                        
                        /**
                         * BD1MAR04
                         * Very special case: if we have resolved the group including the
                         * audit attributes and we only have the audit columns (i.e.
                         * count = 1) it means that the strGroup was actually an empty group
                         * (e.g. 'null' or an explicitly empty group).
                         * 
                         * If distinct was requsted it could be that including the audit columns
                         * screws up the distinct so in this very special case we ensure
                         * that we have an empty attribute collection
                         */
                        if ((colAttr == null || colAttr.size() == 1) && !pblnDistinct) {
                            colAttr = new AttributeCollection();
                        }
                        
                    } else {
                        colAttr = objBO.getDescriptor().getGroup(strAttributeGroup);
                    }
                    
                    if (colAttr == null) { throw new Exception("Unable to get retrieve group : " + strAttributeGroup); }
                    
                    Iterator iter = colAttr.iterator();
                    while (iter.hasNext()) {
                        objAttr = (Attribute) iter.next();
                        
                        if (StringUtil.len(objAttr.getColumn()) > 0) {
                            buildQuery.addColumn(objBO, objAttr);
                            
                            if ( StringUtil.len(objAttr.getForeignKey()) > 0 && blnResolveFK) {
                                /**
                                 * If a foreign key and resolve requested,
                                 * go off and determine what columns / tables
                                 * are requested to resolve this FK
                                 * Last three parameters are:
                                 * Level
                                 *    Sequence of column (for future use)
                                 *    Inner / outer (see method for details)
                                 */
                                buildQuery.resolvefk(objBO, objAttr, false, 1);
                            }
                        }
                    }
                }
            }
            
            return buildQuery;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Build SQL query object", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjColBO = " + pobjColBO);
                getZx().log.error("Parameter : pstrColAttributeGroup = " + pstrColAttributeGroup);
                getZx().log.error("Parameter : pblnResolveFK = " + pblnResolveFK);
                getZx().log.error("Parameter : pblnDistinct, pblnOuterJoin = " + pblnDistinct);
                getZx().log.error("Parameter : pblnOuterJoin = " + pblnOuterJoin);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return buildQuery;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(buildQuery);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate insert query.
     * 
     * @param pobjBO The object to get the insert statement
     * @return Returns the SQL for a insert statement
     * @throws ZXException Thrown if insertQuery fails
     */
    public String insertQuery(ZXBO pobjBO) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
        }

        StringBuffer insertQuery = new StringBuffer("INSERT INTO "); // Get the ball rolling :) 

        try {
            // Added the table : 
            insertQuery.append(tableName(pobjBO, zXType.sqlObjectName.sonPureName));
            
            // Prepare for adding the columns
            insertQuery.append(" (");
            
            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup("*");
            if (colAttr == null) {
                throw new Exception("Unable to handle to attributes for attributegroup of * ");
            }
            
            // Temp storage 
            StringBuffer insertValues = new StringBuffer();
            Attribute objAttr;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if ( StringUtil.len(objAttr.getColumn()) > 0 && !objAttr.isVirtualColumn()) {
                    // Add the seperator :
                    if (insertValues.length() > 0) {
                        insertQuery.append(" ,");
                        insertValues.append(" ,");
                    }
                    
                    // Add column name : 
                    insertQuery.append(columnName(pobjBO, objAttr, zXType.sqlObjectName.sonPureName));
                    
                    // Add column values : 
                    insertValues.append(dbValue(pobjBO, objAttr));
                }
            }
            
            insertQuery.append(")  VALUES (");
            insertQuery.append(insertValues);
            insertQuery.append(")");
            
            return insertQuery.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate insert query ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return insertQuery.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(insertQuery);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate insert query for a prepared statement.
     * 
     * @param pobjBO The object to get the insert statement
     * @return Returns the SQL for a insert statement
     * @throws ZXException Thrown if insertQuery fails
     */
    public Query insertPreparedQuery(ZXBO pobjBO) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
        }
        
        Query insertPreparedQuery = new Query();
        insertPreparedQuery.setSql(new StringBuffer("INSERT INTO ")); // Get the ball rolling :) 

        try {
            // Added the table : 
        	insertPreparedQuery.getSql().append(tableName(pobjBO, zXType.sqlObjectName.sonPureName));
            
            // Prepare for adding the columns
        	insertPreparedQuery.getSql().append(" (");
            
            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup("*");
            if (colAttr == null) {
                throw new Exception("Unable to handle to attributes for attributegroup of * ");
            }
            
            // Temp storage 
            StringBuffer insertValues = new StringBuffer();
            Attribute objAttr;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if (StringUtil.len(objAttr.getColumn()) > 0 && !objAttr.isVirtualColumn()) {
                    // Add the seperator :
                    if (insertValues.length() > 0) {
                    	insertPreparedQuery.getSql().append(" ,");
                        insertValues.append(" ,");
                    }
                    
                    // Add column name : 
                    insertPreparedQuery.getSql().append(columnName(pobjBO, objAttr, zXType.sqlObjectName.sonPureName));
                    
                    // Add column values : 
                    insertValues.append("?");
                    // Add value to collection at same point.
                    insertPreparedQuery.getValues().add(pobjBO.getValue(objAttr.getName()));
                }
            }
            
            insertPreparedQuery.getSql().append(")  VALUES (");
            insertPreparedQuery.getSql().append(insertValues);
            insertPreparedQuery.getSql().append(")");
            
            return insertPreparedQuery;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate insert query ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return insertPreparedQuery;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(insertPreparedQuery);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * BO delete query.
     * 
     * @param pobjBO The business object the delete query is for.
     * @return Returns the sql delete query
     * @throws ZXException Thrown if deleteQuery fails.
     */
    public String deleteQuery(ZXBO pobjBO) throws ZXException {
        String deleteQuery;
        
        deleteQuery = "DELETE FROM " 
            		  + tableName(pobjBO, zXType.sqlObjectName.sonPureName) 
            		  + " WHERE (1 = 1)";
        
        return deleteQuery;
    }
    
    /**
     * Generate an update query.
     * 
     * @param pobjBO The BO the get the updateQuery from
     * @param pstrAttributeGroup The attribure group to perform the query on
     * @return Returns the sql update query
     * @throws ZXException Thrown if updateQuery fails
     */
    public String updateQuery(ZXBO pobjBO, String pstrAttributeGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
        }
        
        String updateQuery = null;

        try {
            /**
             * Get attribute group. For auditable BOs and not being called 
             * recursively, add the audit columns
             * BD9JUN04 - Now concurrency control
             */
            AttributeCollection colAttr = null;
            if (pobjBO.getDescriptor().isConcurrencyControl()) {
                colAttr = pobjBO.getDescriptor().getGroup(pstrAttributeGroup + ",~");
            } else {
                colAttr = pobjBO.getDescriptor().getGroup(pstrAttributeGroup);
            }
            
            if (colAttr == null) { 
                throw new Exception("Unable to handle to attributes : " + pstrAttributeGroup); 
            }
            
            StringBuffer strColumnList = new StringBuffer();
            Attribute objAttr;
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                /**
                 * Skip the created by / when audit columns and where no column is present
                 * and the virtual columns
                 */
                if (StringUtil.len(objAttr.getColumn()) > 0) {
                    if( !(objAttr.getColumn().equalsIgnoreCase("zXCrtdBy") || objAttr.getColumn().equalsIgnoreCase("zXCrtdWhn")) ) {
                        if (!objAttr.isVirtualColumn()) {
                            if (strColumnList.length() > 0) {
                                strColumnList.append(", ");
                            }
                            
                            /** 
                             * Add the column name
                             */
                            strColumnList.append(columnName(pobjBO, objAttr, zXType.sqlObjectName.sonPureName));
                            
                            /**
                             * Add the comparison 
                             */
                            if (pobjBO.getValue(objAttr.getName()).isNull) {
                                // Handle null fields :
                                strColumnList.append(" = NULL ");
                            } else {
                                strColumnList.append(" = ");
                                strColumnList.append(dbValue(pobjBO, objAttr));
                            }
                        } // Virtual column?
                    } // Is a auditing column.
                } // Have a column
            } // Loop through attributes.
            
            updateQuery = " UPDATE " + tableName(pobjBO, zXType.sqlObjectName.sonPureName) + " SET " +  strColumnList.toString() + " WHERE (1=1) ";
            
            return updateQuery;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate an update query", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return updateQuery;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(updateQuery);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate an update query for a prepared statement.
     * 
     * @param pobjBO The BO the get the updateQuery from
     * @param pstrAttributeGroup The attribure group to perform the query on
     * @return Returns the sql update query.
     * @throws ZXException Thrown if updateQuery fails
     */
    public Query updatePreparedQuery(ZXBO pobjBO, String pstrAttributeGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
        }
        
        Query updateQuery = new Query();

        try {
            /**
             * Get attribute group. For auditable BOs and not being called 
             * recursively, add the audit columns
             * BD9JUN04 - Now concurrency control
             */
            AttributeCollection colAttr = null;
            if (pobjBO.getDescriptor().isConcurrencyControl()) {
                colAttr = pobjBO.getDescriptor().getGroup(pstrAttributeGroup + ",~");
            } else {
                colAttr = pobjBO.getDescriptor().getGroup(pstrAttributeGroup);
            }
            
            if (colAttr == null) { 
                throw new Exception("Unable to handle to attributes : " + pstrAttributeGroup); 
            }
            
            StringBuffer strColumnList = new StringBuffer();
            Attribute objAttr;
            Property objProp;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                /**
                 * Skip the created by / when audit columns and where no column is present
                 * and the virtual columns
                 */
                if (StringUtil.len(objAttr.getColumn()) > 0) {
                    if( !(objAttr.getColumn().equalsIgnoreCase("zXCrtdBy") || objAttr.getColumn().equalsIgnoreCase("zXCrtdWhn")) ) {
                        if (!objAttr.isVirtualColumn()) {
                            if (strColumnList.length() > 0) {
                                strColumnList.append(", ");
                            }
                            
                            /** 
                             * Add the column name
                             */
                            strColumnList.append(columnName(pobjBO, objAttr, zXType.sqlObjectName.sonPureName));
                            
                            /**
                             * Add the comparison and the placeholder.
                             * 
                             * NOTE : Nulls are handled when preparing the statement.
                             */
                            strColumnList.append(" = ");
                            strColumnList.append("?");
                            
                            /**
                             * Store the value in a collection to be used later on.
                             */
                            objProp = pobjBO.getValue(objAttr.getName());
                            updateQuery.getValues().add(objProp);
                            
                        } // Virtual column?
                    } // Is a auditing column.
                } // Have a column
            } // Loop through attributes.
            
            updateQuery.setSql(new StringBuffer(" UPDATE " + tableName(pobjBO, zXType.sqlObjectName.sonPureName) + " SET " +  strColumnList.toString() + " WHERE (1=1) "));
            
            return updateQuery;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate an update query", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return updateQuery;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(updateQuery);
                getZx().trace.exitMethod();
            }
        }
    }
        
    /**
     * Generate name / alias / clause for column.
     * 
     * Reviewed for 1.5:1.
     * 
     * @param pobjBO The business object the column belongs to.
     * @param pobjAttr The attribute linked to the column
     * @param penmType The type of query the column is used in.
     * @return Returns the column name formatted for SQL
     * @throws ZXException Thrown if columnName fails
     * @since J1.5
     */
    public String columnName(ZXBO pobjBO, 
                             Attribute pobjAttr,
                             zXType.sqlObjectName penmType) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("penmType", penmType);
        }

        StringBuffer columnName = new StringBuffer();
        
        try {
            DSHRdbms objDSHandler = (DSHRdbms)pobjBO.getDS();
            zXType.databaseType databaseType = objDSHandler.getDbType();
            int intType = penmType.pos;
            
            /**
             *If attribute is not associated with any column: return
             */
            String strColumn = pobjAttr.getColumn();
            if( StringUtil.len(strColumn) == 0 ) {
                return columnName.toString();
            }
            
            /**
             * Perhaps we can use the cached version
             */
            if(intType == zXType.sqlObjectName.sonRSName.pos) {
                if( StringUtil.len(pobjAttr.getTrueRSName()) > 0 ) {
                    columnName.append(pobjAttr.getTrueRSName());
                    return columnName.toString();
                }
            }
            
            if (intType == zXType.sqlObjectName.sonPureName.pos) {
                /**
                 * DGS07MAY2003: Previously put square brackets around Access/SQLServer column names
                 * but that is wrong in this instance because the SELECT will not have used AS, and
                 * only the column name is returned. This does mean this might not work with column
                 * names with spaces in, but that should not be commonly encountered.
                 */
                columnName.append(strColumn);
                
            } else if (intType == zXType.sqlObjectName.sonName.pos) {
                if(pobjAttr.isVirtualColumn()) {
                    columnName.append(strColumn);
                } else {
                    /**
                     * Either the alias name (if there is one) or the column name
                     */
                    String tableName = tableName(pobjBO, zXType.sqlObjectName.sonAlias);
                    
                    if(databaseType.equals(zXType.databaseType.dbAccess) || databaseType.equals(zXType.databaseType.dbSQLServer)) {
                        /**
                         * Looks odd that table can be blank but this is the case for super
                         * dynamic descriptors that are based on a query
                         */
                        if (StringUtil.len(tableName) > 0) {
                            columnName.append('[').append(tableName).append("].[").append(strColumn).append(']');
                        } else {
                            columnName.append('[').append(strColumn).append(']');
                        }
                        
                    } else if (databaseType.equals(zXType.databaseType.dbMysql)) {
                        if (StringUtil.len(tableName) > 0) {
                            columnName.append('`').append(tableName).append("`.`").append(strColumn).append('`');
                        } else {
                            columnName.append('`').append(strColumn).append('`');
                        }
                        
                    } else {
                        columnName.append(tableName).append(".").append(strColumn);
                    }
                }
                
            } else if (intType == zXType.sqlObjectName.sonAlias.pos || intType == zXType.sqlObjectName.sonRSName.pos) {
                /**
                 * Generated from table name & _ & column name
                 */
                columnName.append(tableName(pobjBO, zXType.sqlObjectName.sonAlias));
                
                columnName.append('_');
                
                if (pobjAttr.isVirtualColumn()) {
                    columnName.append(pobjAttr.getName());
                } else {
                    columnName.append(strColumn);
                }
                
                /**
                 * Oracle identifiers can only be 30 in length so create
                 * a name constructed of first 25 positions, followed by a checksum of 5
                 * BD6DEC02: Increased from 15 positions + use constant now
                 * DGS08SEP2003: Same for DB2
                 */
                if((databaseType.equals(zXType.databaseType.dbOracle) 
                    ||  databaseType.equals(zXType.databaseType.dbDB2)
                    ||  databaseType.equals(zXType.databaseType.dbAS400))
                    && columnName.length() > MAX_LENGTH_ORA_ID) {
                    
                    columnName = new StringBuffer(
                    		     columnName.substring(0, MAX_LENGTH_ORA_ID) 
                            	 + StringUtil.checkSum( columnName.substring(MAX_LENGTH_ORA_ID + 1) )
                            					);
                    
                }
                
                /**
                 * And store cached version of rs name in the attribute variable trueRSName
                 */
                if(intType == zXType.sqlObjectName.sonRSName.pos) {
                    /**
                     * Do NOT use a cache for the standard zX columns as these are shared
                     * by all BOs and caching any BO specific value would end in tears.
                     */
                    if(!pobjAttr.getName().toLowerCase().startsWith("zx")) {
                        pobjAttr.setTrueRSName(columnName.toString());
                    }
                    
                } else {
                    /**
                     * For Access / SQL Server people can use real silly column names
                     * (eg with spaces and slashes). When you quote them with [ and ]
                     * this is allright but these do not end up in the fields collection
                     * of a recordset
                     **/
                    if(databaseType.equals(zXType.databaseType.dbSQLServer) 
                       || databaseType.equals(zXType.databaseType.dbAccess)) {
                        columnName.insert(0,'[');
                    	columnName.append(']');
                    	
                    } else if (databaseType.equals(zXType.databaseType.dbMysql)) {
                        columnName.insert(0,'`');
                    	columnName.append('`');
                    }
                    
                }
                
            } else if (intType == zXType.sqlObjectName.sonClause.pos) {
                /**
                 * columnname AS alias (non Oracle)
                 * columnname alias (Oracle)
                 */
                columnName.append(columnName(pobjBO, pobjAttr, zXType.sqlObjectName.sonName));
                
                if(databaseType.equals(zXType.databaseType.dbOracle)) {
                    columnName.append(' ');
                } else {
                    columnName.append(" AS ");
                }
                
                columnName.append(columnName(pobjBO, pobjAttr, zXType.sqlObjectName.sonAlias));
                
            }
            
            return columnName.toString();
        } catch (Exception e) {
            getZx().trace.addError("Generate name / alias / clause for column", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pobjAttr = " + pobjAttr);
                getZx().log.error("Parameter : penmType = " + penmType);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return columnName.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(columnName);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate a query to retrieve a many-to-many relation. Base HAS Has IS Is
     * (e.g. client HAS order IS produdct)
     * 
     * <pre>
     * 
     * NOTE : This calls hasIsQuery(pobjBaseBO, pobjHasBO, pobjIsBO, "*", false, "*", false);
     * </pre>
     * 
     * @param pobjBaseBO The main object (ie the client)
     * @param pobjHasBO The HAS object (ie order)
     * @param pobjIsBO The IS object (ie product)
     * @return Returns a many-to-many query.
     * @throws ZXException Thrown if hasIsQuery fails
     */
    public String hasIsQuery(ZXBO pobjBaseBO, ZXBO pobjHasBO, ZXBO pobjIsBO) throws ZXException {
        return hasIsQuery(pobjBaseBO, pobjHasBO, pobjIsBO, "*", false, "*", false);
    }
    
    /**
     * Generate a query to retrieve a many-to-many relation. Base HAS Has IS Is
     * (e.g. client HAS order IS produdct)
     * 
     * @param pobjBaseBO The main object (ie the client)
     * @param pobjHasBO The HAS object (ie order)
     * @param pobjIsBO The IS object (ie product)
     * @param pstrHasGroup Attributes to retrieve from HAS object
     * @param pblnHasResolveFK Resolve FK for HAS?
     * @param pstrIsGroup Attributes to retrieve from IS
     * @param pblnIsResolveFK Resolve FK for IS?
     * @return Returns a many-to-many query.
     * @throws ZXException Thrown if hasIsQuery fails
     */
    public String hasIsQuery(ZXBO pobjBaseBO, 
                             ZXBO pobjHasBO, 
                             ZXBO pobjIsBO,
                             String pstrHasGroup, 
                             boolean pblnHasResolveFK,
                             String pstrIsGroup, 
                             boolean pblnIsResolveFK) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBaseBO", pobjBaseBO);
            getZx().trace.traceParam("pobjHasBO", pobjHasBO);
            getZx().trace.traceParam("pobjIsBO", pobjIsBO);
            getZx().trace.traceParam("pstrHasGroup", pstrHasGroup);
            getZx().trace.traceParam("pblnHasResolveFK", pblnHasResolveFK);
            getZx().trace.traceParam("pstrIsGroup", pstrIsGroup);
            getZx().trace.traceParam("pblnIsResolveFK", pblnIsResolveFK);
        }

        StringBuffer hasIsQuery = new StringBuffer();
        
        /**
         * Defaults : 
         */
        if (pstrHasGroup == null) {
            pstrHasGroup = "*";
        }
        if (pstrIsGroup == null) {
            pstrIsGroup = "*";
        }
        
        try {
            /**
             * Generate the base query
             */
            hasIsQuery.append(selectQuery(new ZXBO[]{pobjHasBO, pobjIsBO}, 
                                          new String[]{pstrIsGroup,  pstrHasGroup}, 
                                          new boolean[]{pblnIsResolveFK, pblnHasResolveFK}));
            
            /**
             * Get attribute on HAS that is the FK to base (ie order.client)
             */
            Attribute objFKAttr = pobjHasBO.getFKAttr(pobjBaseBO);
            if (objFKAttr == null) {
                throw new Exception(
                        "Unable to find FK to base object. "
                        + " Base : " + pobjBaseBO.getDescriptor().getName() 
                        + " Other : " + pobjHasBO.getDescriptor().getName()
                        );
            }
            
            pobjHasBO.setValue(objFKAttr.getName(), pobjBaseBO.getPKValue());
            
            hasIsQuery.append(" AND ");
            hasIsQuery.append(whereCondition(pobjHasBO, objFKAttr.getName()));
            
            return hasIsQuery.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate a query to retrieve a many-to-many relation", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBaseBO = " + pobjBaseBO);
                getZx().log.error("Parameter : pobjHasBO = " + pobjHasBO);
                getZx().log.error("Parameter : pobjIsBO = " + pobjIsBO);
                getZx().log.error("Parameter : pstrHasGroup= " + pstrHasGroup);
                getZx().log.error("Parameter : pblnHasResolveFK = " + pblnHasResolveFK);
                getZx().log.error("Parameter : pstrIsGroup = " + pstrIsGroup);
                getZx().log.error("Parameter : pblnIsResolveFK = " + pblnIsResolveFK);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return hasIsQuery.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(hasIsQuery);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate order by clause.
     * 
     * <pre>
     * 
     * NOTE : This calls orderByClause(pobjBO, pstrOrderByGroup, pblnReverse, false);
     * </pre>
     * 
     * @param pobjBO The ZXBO for which this orderby is for.
     * @param pstrOrderByGroup The order by group to use
     * @param pblnReverse Descending (true) or ascending (false). Optional, default is false.
     * @return Returns a standard order by clause for the select group and business
     * @throws ZXException Thrown if orderByClause fails
     */
    public String orderByClause(ZXBO pobjBO, String pstrOrderByGroup, boolean pblnReverse) throws ZXException {
        return orderByClause(pobjBO, pstrOrderByGroup, pblnReverse, false);
    }
    
    /**
     * Generate order by clause.
     * 
     * Reviewed for 1.5:1.
     * 
     * @param pobjBO The ZXBO for which this orderby is for.
     * @param pstrOrderByGroup The order by group to use
     * @param pblnReverse Descending (true) or ascending (false). Optional, default is false.
     * @param pblnColumnsOnly Include ORDER BY (false) or not (true). Optional, default is false.
     * @return Returns a sql order by clause
     * @throws ZXException Thrown if orderByClause fails.
     */
    public String orderByClause(ZXBO pobjBO, String pstrOrderByGroup, boolean pblnReverse, boolean pblnColumnsOnly) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO, pstrOrderByGroup, pblnReverse, pblnColumnsOnly", pobjBO);
            getZx().trace.traceParam("pstrOrderByGroup", pstrOrderByGroup);
            getZx().trace.traceParam("pblnReverse, pblnColumnsOnly", pblnReverse);
            getZx().trace.traceParam("pblnColumnsOnly", pblnColumnsOnly);
        }
        
        StringBuffer orderByClause = new StringBuffer();

        try {
            DSHRdbms objDSHandler = (DSHRdbms)pobjBO.getDS();
            
            /**
             * Get the AttributeCollection for the AttributeGroup
             */
            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup(pstrOrderByGroup);
            if (colAttr == null) { throw new Exception("Unable to get retrieve group : " + pstrOrderByGroup); }

            Attribute objAttr;
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute) iter.next();
                
                if ( StringUtil.len(objAttr.getColumn()) > 0 ) {
                    
                    if (orderByClause.length() > 0) {
                        orderByClause.append(" , ");
                    }
                    
                    /**
                     * Add the column : 
                     */
                    String strTmp = columnName(pobjBO, objAttr, zXType.sqlObjectName.sonName);
                    
                    /**
                     * Added case-insensitive searching for Oracle
                     */
                    if (objAttr.getDataType().pos == zXType.dataType.dtString.pos 
                            && objAttr.getTextCase().equals(zXType.textCase.tcInsensitive)) {
                        strTmp = makeCaseInsensitive(strTmp, objDSHandler.getDbType());
                    }
                    orderByClause.append(strTmp);
                    
                    /**
                     * DGS27JUN2003: Add the 'descending' after each attr, not just at the end,
                     * because it only applies to the attr immediately preceding, and as things
                     * stand we translate a leading "-" as applying to all attrs in the group.
                     * 
                     * Also now support the special attribute tag zXOrderDesc which has been
                     * set in descriptor.getGroup when when the \- construct is being used
                     */
                    if (orderByClause.length()  > 0) {
                        if (pblnReverse) {
                        	orderByClause.append(" DESC ");
                        } else if ("1".equals(objAttr.tagValue("zXOrderDesc"))) {
                        	orderByClause.append(" DESC ");
                        }
                    }
                    
                }
            }
            
            /**
             * Optional add the ORDER BY clause
             */
            if (!pblnColumnsOnly && orderByClause.length() > 0) {
                orderByClause.insert(0," ORDER BY ");
            }
            
            return orderByClause.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate order by clause", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrOrderByGroup = " + pstrOrderByGroup);
                getZx().log.error("Parameter : pblnReverse = " + pblnReverse);
                getZx().log.error("Parameter : pblnColumnsOnly = " + pblnColumnsOnly);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return orderByClause.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(orderByClause);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Generate group by clause.
     * 
     * <pre>
     * 
     * NOTE : groupByClause(pobjBO, pstrAttributeGroup, false);
     * </pre>
     * 
     * @param pobjBO This ZXBO to get the group by clause for
     * @param pstrAttributeGroup The attribute group to select the group by for
     * @return Returns a group by clause for attribute group
     * @throws ZXException Thrown if groupByClause fails
     */
    public String groupByClause(ZXBO pobjBO, String pstrAttributeGroup) throws ZXException {
        return groupByClause(pobjBO, pstrAttributeGroup, false);
    }
    
    /**
     * Generate group by clause.
     * 
     * @param pobjBO This ZXBO to get the group by clause for
     * @param pstrAttributeGroup The attribute group to select the group by for
     * @param pblnColumnsOnly Where or not to add the GROUP BY. Optional, default is false
     * @return Returns a group by clause for attribute group
     * @throws ZXException Thrown if groupByClause fails
     */
    public String groupByClause(ZXBO pobjBO, 
            	                String pstrAttributeGroup, 
                                boolean pblnColumnsOnly) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrAttributeGroup", pstrAttributeGroup);
            getZx().trace.traceParam("pblnColumnsOnly", pblnColumnsOnly);
        }
        
        StringBuffer groupByClause = new StringBuffer();
        
        try {
            /**
             * Get the AttributeCollection for the AttributeGroup
             */
            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup(pstrAttributeGroup);
            if (colAttr == null) { throw new Exception("Unable to get retrieve group : " + pstrAttributeGroup); }

            Attribute objAttr;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute) iter.next();
                if (StringUtil.len(objAttr.getColumn()) > 0) {    
                    if (groupByClause.length() > 0) {
                        groupByClause.append(", ");
                    }
                    groupByClause.append(columnName(pobjBO, objAttr, zXType.sqlObjectName.sonName));
                }
            }
            
            /**
             * Optionally add the GROUP BY keyword
             */
            if (!pblnColumnsOnly && groupByClause.length() > 0) {
                groupByClause.insert(0," GROUP BY ");
            }
            
            return groupByClause.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate group by clause ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrAttributeGroup = " + pstrAttributeGroup);
                getZx().log.error("Parameter : pblnColumnsOnly = " + pblnColumnsOnly);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return groupByClause.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(groupByClause);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Name / alias / clause for BO table.
     * 
     * Reviewed for 1.5:1
     * 
     * @param pobjBO The business object that you want the table name for.
     * @param penmType The type of context the table name is being used.
     * @return Returns the name of the table for the SQL query.
     * @throws ZXException Thrown if tableName fails
     */
    public String tableName(ZXBO pobjBO, zXType.sqlObjectName penmType) throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("pobjBO", pobjBO);
	        getZx().trace.traceParam("penmType", penmType);
	    }
	    
        String tableName = "";
        	
	    try {
	        /**
	         * Caching : Check if we have already retrieved the tablename for this entity recently.
	         */
	        if(objLastTable != null) {
	            if(objLastTable.getDescriptor().equals(pobjBO.getDescriptor())) {
	                if(penmType.equals(enmLastTableType) && pobjBO.getDescriptor().getAlias().equalsIgnoreCase(strLastAlias)) {
	                    tableName = strLastTableName;
	                    return tableName;
                        
	                }
	            }
	        }
            
	        tableName = pobjBO.getDescriptor().getTable();
            
	        DSHRdbms objDSHandler = (DSHRdbms)pobjBO.getDS();
	        zXType.databaseType databaseType = objDSHandler.getDbType();
            
	        if(penmType.equals(zXType.sqlObjectName.sonPureName)) {
                tableName = objDSHandler.getSchema() + tableName;
                
	            /**
	             *  Bracket name for MS DBMS to cater for unusual characters like spaces and slashed
	             */
	            if(databaseType.equals(zXType.databaseType.dbAccess) 
                   || databaseType.equals(zXType.databaseType.dbSQLServer)) {
	                tableName = '[' + tableName + ']';
	                
	            } else if (databaseType.equals(zXType.databaseType.dbMysql)) {
	            	tableName = '`' + tableName + '`';
	            }
	            
	        } else if (penmType.equals(zXType.sqlObjectName.sonName)) {
                tableName = objDSHandler.getSchema() + tableName;
                
	            /**
	             * Bracket name for MS DBMS to cater for unusual characters like spaces and slashed
	             */
	            if(databaseType.equals(zXType.databaseType.dbAccess) || databaseType.equals(zXType.databaseType.dbSQLServer)) {
	                tableName = "[" + tableName + "]";
	            
	            } else if (databaseType.equals(zXType.databaseType.dbMysql)) {
	            	tableName = '`' + tableName + '`';
	            }  
	            
	        } else if (penmType.equals(zXType.sqlObjectName.sonAlias)) {
	            /**
	             * The alias name is either the table name or the alias name (if one
	             * is given)
	             */
	            if( StringUtil.len(pobjBO.getDescriptor().getAlias()) > 0 ) {
	                tableName = pobjBO.getDescriptor().getAlias();
	            }
	            
                tableName = objDSHandler.getSchema() + tableName;
                
	            /** 
	             * Oracle identifiers can only be 30 in length so create
	             * a name constructed of first 25 positions, followed by a checksum of 5
	             * 
	             * BD6DEC02: Increased from 15 positions + use constant now
	             * DGS08SEP2003: Same for DB2
	             */
	            if (databaseType.equals(zXType.databaseType.dbOracle) && tableName.length() > MAX_LENGTH_ORA_ID) {
	                tableName = tableName.substring(0, MAX_LENGTH_ORA_ID) + StringUtil.checkSum(tableName.substring(MAX_LENGTH_ORA_ID + 1));
	            }
	            
	        } else if (penmType.equals(zXType.sqlObjectName.sonClause)) {
	            /**
	             * When in a clause and you need to have aliases ? 
	             * 
	             * The table name clause is something like:
	             * table AS alias (if an alias is present)
	             */
	            if( StringUtil.len(pobjBO.getDescriptor().getAlias()) == 0 ) {
	                tableName = tableName(pobjBO,zXType.sqlObjectName.sonName);
	            } else {
	                if( !databaseType.equals(zXType.databaseType.dbOracle) ) {
	                    tableName = tableName(pobjBO, zXType.sqlObjectName.sonName) + " AS " + tableName(pobjBO,zXType.sqlObjectName.sonAlias);
	                } else {
	                    tableName = tableName(pobjBO, zXType.sqlObjectName.sonName) + ' ' + tableName(pobjBO,zXType.sqlObjectName.sonAlias);
	                }
	            }
                
	        } // Name type
	        
	        /**
	         * Cache the last entry.
	         */ 
	        strLastTableName = tableName;
	        objLastTable = pobjBO;
	        enmLastTableType = penmType;
            
	        return tableName;
	        
	    } catch (Exception e) {
            getZx().trace.addError("Failed to : Get table name", e);
	        if (getZx().log.isErrorEnabled()) {
	            getZx().log.error("Parameter : pobjBO = " + pobjBO);
	            getZx().log.error("Parameter : penmType = " + penmType);
	        }
	        
	        if (getZx().throwException) throw new ZXException(e);
	        return tableName;
	    } finally {
	        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	            getZx().trace.returnValue(tableName);
	            getZx().trace.exitMethod();
	        }
	    } 
    }
    
    /**
     * Generate clause that can be added to a query's where clause 
     * that restricts the rows to the ones that are associated with 
     * the right table through the middle table.
     * 
     * <pre>
     * 
     *  Example:
     * 
     *  Left - client
     *  Middle - order
     *  Right - product
     * 
     *  product.id in (select order.product
     *    from order
     *    where order.client = client.id)
     * 
     * Assumes   : PK of client (left) has been set
     * </pre>
     * 
     * @param pobjLeft The left Business Object
     * @param pobjMiddle The middle Businness Object
     * @param pobjRight The right Business Obect
     * @return Returns a associated with sql clause
     * @throws ZXException Thrown if associatedWithClause
     */
    public String associatedWithClause(ZXBO pobjLeft, ZXBO pobjMiddle, ZXBO pobjRight) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjLeft", pobjLeft);
            getZx().trace.traceParam("pobjMiddle", pobjMiddle);
            getZx().trace.traceParam("pobjRight", pobjLeft);
        }
        
        StringBuffer associatedWithClause = new StringBuffer();
        
        try {
            /**
             * product.id
             * right.id
             */
            associatedWithClause.append(columnName(pobjRight, pobjRight.getDescriptor().pkAttr(), zXType.sqlObjectName.sonName));
            
            /**
             * IN (SELECT
             */
            associatedWithClause.append("  IN (SELECT ");
            
            /**
             * Find the FK from objBO to
             */
            Attribute objFKAttr = pobjMiddle.getFKAttr(pobjRight);
            if (objFKAttr == null) {
                throw new Exception(
                        "Unable to find FK from middle to right. "
                        + " Middle : " + pobjMiddle.getDescriptor().getName() 
                        + " Right : " + pobjRight.getDescriptor().getName()
                        );
            }
            
            associatedWithClause.append(columnName(pobjMiddle, objFKAttr, zXType.sqlObjectName.sonName));
            
            /**
             * FROM middle
             */
            associatedWithClause.append(" FROM ");
            associatedWithClause.append(tableName(pobjMiddle, zXType.sqlObjectName.sonName));
            
            /**
             * WHERE middle.left = left.id
             */
            associatedWithClause.append(" WHERE ");

            objFKAttr = pobjMiddle.getFKAttr(pobjLeft);
            if (objFKAttr == null) {
                throw new Exception(
                        "Unable to find FK from middle to left. "
                        + " Middle : " + pobjMiddle.getDescriptor().getName() 
                        + " Right : " + pobjLeft.getDescriptor().getName()
                        );
            }
            
            associatedWithClause.append(columnName(pobjMiddle, objFKAttr, zXType.sqlObjectName.sonName));
            associatedWithClause.append("=");
            associatedWithClause.append( dbValue(pobjLeft, pobjLeft.getDescriptor().pkAttr()) );
            
            associatedWithClause.append(")");
            
            return associatedWithClause.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate query for associated with clause ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjLeft = " + pobjLeft);
                getZx().log.error("Parameter : pobjMiddle = "  + pobjMiddle);
                getZx().log.error("Parameter : pobjRight = " + pobjRight);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return associatedWithClause.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(associatedWithClause);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate query for not associated with clause, see associated with for
     * more information.
     * 
     * <pre>
     * 
     * MB2JUN06 - V1.5:98
     * </pre>
     * 
     * @param pobjLeft The left Business Object
     * @param pobjMiddle The middle Businness Object
     * @param pobjRight The right Business Obect
     * @param pstrMiddleGroup Optional, defaults to "". And attribute group of the middle group to help restrict the association.
     * @return Returns a not associated with sql clause
     * @see SQL#associatedWithClause(ZXBO, ZXBO, ZXBO)
     * @throws ZXException Thrown if notAssociatedWithClause
     */
    public String notAssociatedWithClause(ZXBO pobjLeft, ZXBO pobjMiddle, ZXBO pobjRight, String pstrMiddleGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjLeft", pobjLeft);
            getZx().trace.traceParam("pobjMiddle", pobjMiddle);
            getZx().trace.traceParam("pobjRight", pobjLeft);
            getZx().trace.traceParam("pstrMiddleGroup", pstrMiddleGroup);
        }
        
        StringBuffer notAssociatedWithClause = new StringBuffer();
        
        try {
        	/**
        	 * order - orderline - product (left - middle - right)
        	 * Middle is assumed to have FK to left and right; return all
        	 * rights for which no middle exists for current left
        	 */
            /**
             * product.id
             * right.id
             */
            notAssociatedWithClause.append(columnName(pobjRight, pobjRight.getDescriptor().pkAttr(), zXType.sqlObjectName.sonName));
            
            /**
             * NOT IN (SELECT
             */
            notAssociatedWithClause.append(" NOT IN (SELECT ");
            
            /**
             * Find the FK from objBO to
             */
            Attribute objFKAttr = pobjMiddle.getFKAttr(pobjRight);
            if (objFKAttr == null) {
                throw new ZXException(
                        "Unable to find FK from middle to right. "
                        + " Middle : " + pobjMiddle.getDescriptor().getName() 
                        + " Right : " + pobjRight.getDescriptor().getName()
                        );
            }
            
            notAssociatedWithClause.append(columnName(pobjMiddle, objFKAttr, zXType.sqlObjectName.sonName));
            
            /**
             * FROM middle
             */
            notAssociatedWithClause.append(" FROM ");
            notAssociatedWithClause.append(tableName(pobjMiddle, zXType.sqlObjectName.sonName));
            
            /**
             * WHERE middle.left = left.id
             */
            notAssociatedWithClause.append(" WHERE ");

            objFKAttr = pobjMiddle.getFKAttr(pobjLeft);
            if (objFKAttr == null) {
                throw new ZXException(
                        "Unable to find FK from middle to left. "
                        + " Middle : " + pobjMiddle.getDescriptor().getName() 
                        + " Right : " + pobjLeft.getDescriptor().getName());
            }
            
            notAssociatedWithClause.append(columnName(pobjMiddle, objFKAttr, zXType.sqlObjectName.sonName));
            notAssociatedWithClause.append("=");
            
            /**
             * Left entity my be on another channel, but all we want is the value for it.
             */
            String dbValue;
            Property objProp = pobjLeft.getValue(pobjLeft.getDescriptor().pkAttr().getName());
            if (objProp == null || objProp.isNull) {
                dbValue = " NULL ";
                
            } else {
            	/**
            	 * The middle entity has to be on a database persistence channel.
            	 */
                DSHRdbms objDSHandler = (DSHRdbms)pobjMiddle.getDS();
                zXType.databaseType enmDBType = objDSHandler.getDbType();
                
                /**
                 * Ensure the value is correct escaped for a particular database.
                 */
                dbValue = dbStrValue(pobjLeft.getDescriptor().pkAttr().getDataType().pos, objProp, enmDBType);    
            }
            
            notAssociatedWithClause.append(dbValue);
            
            objFKAttr = pobjMiddle.getFKAttr(pobjRight);
            if (objFKAttr == null) {
                throw new ZXException(
                        "Unable to find FK from middle to right. "
                        + " Middle : " + pobjMiddle.getDescriptor().getName() 
                        + " Right : " + pobjRight.getDescriptor().getName()
                        );
            }
            
            if (objFKAttr.isOptional()) {
            	notAssociatedWithClause.append(" AND ").append(columnName(pobjMiddle, objFKAttr, zXType.sqlObjectName.sonName));
                notAssociatedWithClause.append(" IS NOT NULL ");
            }
            
            /**
             * Optional build on the middle entity where group
             */
            if (StringUtil.len(pstrMiddleGroup) > 0) {
            	String strTmp = processWhereGroup(pobjMiddle, pstrMiddleGroup);
            	if (StringUtil.len(strTmp) > 0) {
            		notAssociatedWithClause.append(" AND ");
                	notAssociatedWithClause.append(strTmp);
            	}
            }
            
            notAssociatedWithClause.append(")");
            
            return notAssociatedWithClause.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate query for not associated with clause ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjLeft = " + pobjLeft);
                getZx().log.error("Parameter : pobjMiddle = "  + pobjMiddle);
                getZx().log.error("Parameter : pobjRight = " + pobjRight);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return notAssociatedWithClause.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(notAssociatedWithClause);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate quick search where clause.
     * 
     * Complete rewrite for 1.5:1
     * 
     * @param pobjBO The ZXBO to build the quick search for.
     * @param pobjValue The value to use for the search.
     * @param pstrAttributeGroup The attribute group to do the search against. Optional, default is "QS".
     * @return Returns a where clause for a quick search across multiple fields
     * @throws ZXException Thrown if QSWhereClause failes
     */
    public String QSWhereClause(ZXBO pobjBO, Property pobjValue, String pstrAttributeGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjValue", pobjValue);
            getZx().trace.traceParam("pstrAttributeGroup", pstrAttributeGroup);
        }
        
        String QSWhereClause = "";
        
        /**
         * Defaults 
         */
        if (pstrAttributeGroup == null) {
            pstrAttributeGroup = "QS";
        }
        
        try {
            
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            if (objDSWhereClause.QSClause(pobjBO, 
                                          pobjValue.getStringValue(), 
                                          pstrAttributeGroup).pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Unable to generate QSA where clause");
            }
            
            QSWhereClause = objDSWhereClause.getAsSQL();
            
            return QSWhereClause;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate quick search where clause", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pobjValue = " + pobjValue);
                getZx().log.error("Parameter : pstrAttributeGroup = " + pstrAttributeGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return QSWhereClause;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(QSWhereClause);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Turn where group into a where clause.
     * 
     * <pre>
     * 
     * Assumes : That the pstrWhereGroup is valid attribute group.
     * 
     * A yet more powerful; this is how it works:
     *    the pkwhere group can be a list of zero or more attributes
     *    separated by commas; note that this is different from a
     *    traditional virtual attribute group
     *    Each element is considered to be either:
     *    - An attribute (but NOT a virtual attribute)
     *    - A group (but NOT a virtual group )
     *    And each item can be prefixed with the following:
     *    - Nothing   --> use =
     *    - <> or -   --> use <>
     *    - >=        --> use >=
     *    - >         --> use >
     *    - <=        --> use <=
     *    - <         --> use <
     *    - | and any of the above:
     *                --> OR with where clause so far rather than AND
     *                
     * Complete rewrite for 1.5:1              
     * </pre>
     * 
     * @param pobjBO The ZXBO the Attribute Groups belongs to.
     * @param pstrWhereGroup A special comma seperated where group
     * @see DSWhereClause#parse(ZXBO, String, boolean, boolean)
     * @return Returns a where clause according the a special attribute group
     * @throws ZXException Thrown if processWhereGroup fails
     */
    public String processWhereGroup(ZXBO pobjBO, String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }

        StringBuffer processWhereGroup = new StringBuffer();
        
        try {
            
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            if (objDSWhereClause.parse(pobjBO, pstrWhereGroup).pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Unable to generate where clause", pstrWhereGroup);
            }
            
            processWhereGroup.append(objDSWhereClause.getAsSQL());
            
            /**
             * Always enclose in ( and ) so we are never caught out when
             * we have an or in the expression
             */
            if (processWhereGroup.length() > 0) {
                processWhereGroup.insert(0, '(');
                processWhereGroup.append(')');
            }
            
            return processWhereGroup.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Turn where group into a where clause", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return processWhereGroup.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(processWhereGroup);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Turn a full-expression-where-group into an SQL where clause.
     * 
     * <pre>
     * 
     * :id=12&name%'bertus'
     * :(id>12&id<24)&orderDate>#date
     * 
     * BD14DEC04 V1.4:5 - Added support for advanced features; now supports full 
     * value operator value syntax where left value and right value can be anything including
     * references to attributes of current BO or any other BO
     * 
     * Complete rewrite for 1.5:1
     * </pre>
     * 
     * @param pobjBO The ZXBO that the where attribute group it part of.
     * @param pstrWhereGroup A special where attribute group that contains special expressions. 
     * @return Returns a where sql clause.
     * @throws ZXException Thrown if processFullExpressionWhereGroup fails.
     */
    public String processFullExpressionWhereGroup(ZXBO pobjBO, String pstrWhereGroup) throws ZXException {
       return processWhereGroup(pobjBO, pstrWhereGroup);
    }
    
    /**
     * Return where condition based on attribute group and values.
     * 
     * <pre>
     * 
     * NOTE : This calls : whereCondition(pobjBO,pstrAttributeGroup,true,false)
     * </pre>
     * 
     * @param pobjBO The BO to get the where clause.
     * @param pstrGroup Group to use for where clause (defaults to PK). Optional,
     *                  defaults to "+"
     * @return Returns the sql where clause or condition
     * @throws ZXException Thrown if whereCondition fails
     */
    public String whereCondition(ZXBO pobjBO, String pstrGroup) throws ZXException {
        return whereCondition(pobjBO,pstrGroup, true, false);
    }
    
    /**
     * Return where condition based on attribute group and values.
     * 
     * <pre>
     * 
     * Example: 
     * 	name = 'Bertus Dispa' 
     * 	and salary = 123.45
     * 
     * However: if the where group is 'special' (i.e. contains >, <, !, = or %)
     * than we create a special where group but ignore the useOr and useEqual
     * flags. See processWhereGroup for the implementation
     * 
     * Even worse: we now also support the full-expression syntax; This is indicates
     * by a ':' as first character and is the all-singing-all-dancing but CPU intensive version
     * 
     * Complete rewrite for 1.5:1 - Now all handles in dsWhereClause object
     * </pre>
     * 
     * @param pobjBO The BO to get the where clause.
     * @param pstrGroup Group to use for where clause (defaults to PK). Optional, defaults to "+"
     * @param pblnUseEqual All = (true) or all <>(false). Optional, defaults to true
     * @param pblnUseOr All OR (true) or all AND (false). Optional, defaults to false
     * @return Returns the sql where clause or condition
     * @throws ZXException Thrown if whereCondition
     */
    public String whereCondition(ZXBO pobjBO, 
                                 String pstrGroup, 
                                 boolean pblnUseEqual, 
                                 boolean pblnUseOr) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pblnUseEqual", pblnUseEqual);
            getZx().trace.traceParam("pblnUseOr", pblnUseOr);
        }
        
        String whereCondition = "";
        
        /**
         * Handle defaults.
         * 
         * Attribute Group defaults to +, which is the primary key.
         */
        if (pstrGroup == null) {
            pstrGroup = pobjBO.getDescriptor().getPrimaryKey();
        }
        
        try {
            if (pstrGroup.equals("+")) {
                pstrGroup = pobjBO.getDescriptor().getPrimaryKey();
            }
            
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            if (objDSWhereClause.parse(pobjBO, pstrGroup, pblnUseEqual, pblnUseOr).pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Unable to parse where-clause", pstrGroup);
            }
            
            whereCondition = objDSWhereClause.getAsSQL();
            
            /**
             * Always include in ( )  for safety
             */
            if (StringUtil.len(whereCondition) > 0) {
                whereCondition = '(' + whereCondition + ')';
            }
            
            return whereCondition;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Return where condition based on attribute group and values.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pblnUseEqual = " + pblnUseEqual);
                getZx().log.error("Parameter : pblnUseOr = " + pblnUseOr);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return whereCondition;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(whereCondition);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Return where condition based on data constants attribute group and
     * default values.
     * 
     * <pre>
     *  NOTE: This will reset the data constant group attribute values.
     * </pre>
     * 
     * @param pobjBo The ZXBO to work on
     * @return Returns the where clause for the data constant group
     * @throws ZXException Thrown if whereDataConstants fails.
     */
    public String whereDataConstants(ZXBO pobjBo) throws ZXException {
        String whereDataConstants = "";

        if (StringUtil.len(pobjBo.getDescriptor().getDataConstantGroup()) > 0) {
            pobjBo.resetBO(pobjBo.getDescriptor().getDataConstantGroup());

            whereDataConstants = whereCondition(pobjBo, pobjBo.getDescriptor().getDataConstantGroup());
        }

        return whereDataConstants;
    }
     
    /**
     * Generate sngle entry for inclusion in where clause.
     * 
     * Complete rewrite for 1.5:2
     *  
     * @param pobjBO The ZXBO to get the where condition
     * @param pobjAttr The Attribute to use
     * @param penmOps How to do the matching.
     * @param pobjValue The value to compare againsts
     * @return Returns a simple where condition
     * @throws ZXException Thrown if singleWhereCondition
     */
    public String singleWhereCondition(ZXBO pobjBO, 
                                       Attribute pobjAttr, 
                                       zXType.compareOperand penmOps, 
                                       Property pobjValue) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("penmOps", penmOps);
            getZx().trace.traceParam("pobjValue", pobjValue);
        }
        
        String singleWhereCondition = "";
        
        try {
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            if (objDSWhereClause.singleWhereCondition(pobjBO,
                                                      pobjAttr,
                                                      penmOps,
                                                      pobjValue,
                                                      zXType.dsWhereConditionOperator.dswcoNone).pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Unable to construct single where condition");
            }
            
            singleWhereCondition = objDSWhereClause.getAsSQL();
            
            return singleWhereCondition;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate sngle entry for inclusion in where clause", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pobjAttr = " + pobjAttr);
                getZx().log.error("Parameter : penmOps = " + penmOps);
                getZx().log.error("Parameter : pobjValue = " + pobjValue);
            }
            if (getZx().throwException) throw new ZXException(e);
            return singleWhereCondition;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(singleWhereCondition);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Return value of attribute as string that can be used in query.
     * 
     * @param pobjBO The BO that the attribute value belongs to
     * @param pobjAttr The Attribute
     * @return Returns the db formatted string value of the Attribute's Property
     * @throws ZXException Thrown if dbValue fails
     */
    public String dbValue(ZXBO pobjBO, Attribute pobjAttr) throws ZXException {
        String dbValue;
        Property objProp = pobjBO.getValue(pobjAttr.getName());
        if (objProp == null || objProp.isNull) {
            // Generate a NULL sql statement.
            dbValue = " NULL ";
            
        } else {
            DSHRdbms objDSHandler = (DSHRdbms)pobjBO.getDS();
            zXType.databaseType enmDBType = objDSHandler.getDbType();
            
            dbValue = dbStrValue(pobjAttr.getDataType().pos, objProp, enmDBType);
        }
        return dbValue;
    }
    
    /**
     * Get the string value of a property.
     * 
     * @param penmDataType The datatype of the property
     * @param pobjProperty The property.
     * @param penmDBType The database type.
     * @return Returns a db string value from a property object.
     * @throws ZXException Thrown if dbStrValue fails.
     */
    public String dbStrValue(int penmDataType, Property pobjProperty, zXType.databaseType penmDBType) throws ZXException {
        String dbStrValue;
        /**
         * Should use native data type like dates for dates etc.. :
         */
        if (penmDataType == zXType.dataType.dtDate.pos 
               || penmDataType == zXType.dataType.dtTime.pos 
               || penmDataType == zXType.dataType.dtTimestamp.pos) {
            // Special case for dates.
            dbStrValue = dbStrValue(penmDataType, pobjProperty.dateValue(), penmDBType);
            
        } else {
            dbStrValue = dbStrValue(penmDataType, pobjProperty.getStringValue(), penmDBType);
        }
        return dbStrValue;
    }
    
    /**
     * Db string value from a date object.
     * 
     * <pre>
     * 
     *  Revised for 1.5:95 - Temporary fix: use hardwired formates for date and time although
     *  settings exist in the datasource definition; need to start passing handle to
     *  datasource around
     * </pre>
     * 
     * @param penmDataType The date type.
     * @param pdatValue The date object.
     * @param penmDBType The database type.
     * @return Returns a db string value from a date object.
     */
    public String dbStrValue(int penmDataType, Date pdatValue, zXType.databaseType penmDBType) {
        String dbStrValue;
        // The output dateformat, this should match the dataformat of the database.
        DateFormat df;
        
        if (penmDataType == zXType.dataType.dtDate.pos)  {
            if (penmDBType.equals(zXType.databaseType.dbOracle)) {
                df = new SimpleDateFormat("ddMMyyyy");
                dbStrValue =  "to_date('" + df.format(pdatValue) + "', 'DDMMYYYY')";
                
            } else if (penmDBType.equals(zXType.databaseType.dbMysql)) {
                df = new SimpleDateFormat("yyyy-MM-dd");
                dbStrValue  =  "DATE_FORMAT('" + df.format(pdatValue) + "', '%Y-%m-%d')";
                
            } else if (penmDBType.equals(zXType.databaseType.dbHsql)) {
                df = new SimpleDateFormat("yyyy-MM-dd");
                dbStrValue  =  "'" + df.format(pdatValue) + "'";
                
            } else if (penmDBType.equals(zXType.databaseType.dbDB2) || penmDBType.equals(zXType.databaseType.dbAS400)) {
                // DB2 and AS400 for now share behaviour with AS400 being the preferred for now.
                df = new SimpleDateFormat("yyyy-MM-dd");
                dbStrValue  =  "DATE('" + df.format(pdatValue) + "')";
                
            } else if (penmDBType.equals(zXType.databaseType.dbSQLServer)) {
                df = new SimpleDateFormat("yyyy.mm.dd");
                dbStrValue  =  "CONVERT(datetime, '" + df.format(pdatValue) + "', 102)";
                
            } else if (penmDBType.equals(zXType.databaseType.dbAccess)) {
                df = new SimpleDateFormat("dd MMM yyyy");
                dbStrValue  =  "#" + df.format(pdatValue) + "#";
                
            } else {
            	/**
            	 * Default to using zX's setting for the date format
            	 */
                df = getZx().getDateFormat();
                dbStrValue = "#" + df.format(pdatValue) + "#";
            }
            
        } else if (penmDataType == zXType.dataType.dtTime.pos)  {
            if (penmDBType.equals(zXType.databaseType.dbOracle)) {
                df = new SimpleDateFormat("HHmmss");
                dbStrValue =  "to_date('" + df.format(pdatValue) + "', 'HH24MISS')";
                
            } else if (penmDBType.equals(zXType.databaseType.dbMysql)) {
                df = new SimpleDateFormat("HH:mm:ss");
                dbStrValue  =  "DATE_FORMAT('" + df.format(pdatValue) + "', '%H:%i:%S')";
                
            } else if (penmDBType.equals(zXType.databaseType.dbHsql)) {
                df = new SimpleDateFormat("HH:mm:ss");
                dbStrValue  =  "'" + df.format(pdatValue) + "'";
                
            } else if (penmDBType.equals(zXType.databaseType.dbDB2) 
                       || penmDBType.equals(zXType.databaseType.dbAS400)) {
                df = new SimpleDateFormat("HH:mm:ss");
                dbStrValue  =  "TIME('" + df.format(pdatValue) + "')";
                
            } else if (penmDBType.equals(zXType.databaseType.dbSQLServer)) {
                df = new SimpleDateFormat("HH:mm:ss");
                dbStrValue  =  "CONVERT(datetime, '" + df.format(pdatValue) + "', 108)";
                
            } else if (penmDBType.equals(zXType.databaseType.dbAccess)) {
                df = new SimpleDateFormat("HH:mm:ss");
                dbStrValue = "#" + df.format(pdatValue) + "#";
            	
            } else {
            	/**
            	 * Default to using zX's setting for the date format
            	 */
                df = getZx().getTimeFormat();
                dbStrValue = "#" + df.format(pdatValue) + "#";
            }
            
        } else {
            if (penmDBType.equals(zXType.databaseType.dbOracle)) {
                df = new SimpleDateFormat("ddMMyyyy HHmmss");
                dbStrValue =  "to_date('" + df.format(pdatValue) + "', 'DDMMYYYY HH24MISS')";
                
            } else if (penmDBType.equals(zXType.databaseType.dbMysql)) {
                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dbStrValue  =  "DATE_FORMAT('" + df.format(pdatValue) + "', '%Y-%m-%d %H:%i:%S')";
                
            } else if (penmDBType.equals(zXType.databaseType.dbHsql)) {
                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dbStrValue  =  "'" + df.format(pdatValue) + "'";
                
            } else if (penmDBType.equals(zXType.databaseType.dbDB2) || penmDBType.equals(zXType.databaseType.dbAS400)) {
                /**
                 * AS400 : http://publib.boulder.ibm.com/html/as400/v4r5/ic2924/info/db2/rbafzmstscale.htm#Header_468
                 * It must be a timestamp, a valid string representation of a 
                 * timestamp, or a character string of length 14.
                 * A character string of length 14 must be a string of digits that represents a valid date and time in 
                 * the form yyyyxxddhhmmss, where yyyy is year, xx is month, dd is day, hh is hour, mm is minute, and ss is seconds. 
                 */ 
                df = new SimpleDateFormat("yyyyMMddHHmmss");
                dbStrValue  =  "TIMESTAMP('" + df.format(pdatValue) + "')";
                
            } else if (penmDBType.equals(zXType.databaseType.dbSQLServer)) {
                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dbStrValue  =  "CONVERT(datetime, '" + df.format(pdatValue) + "', 120)"; // ODBC canonical : yyyy-mm-dd hh:mi:ss(24h)
                
            } else if (penmDBType.equals(zXType.databaseType.dbAccess)) {
                df = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
                dbStrValue = "#" + df.format(pdatValue) + "#";
                
            } else {
            	/**
            	 * Default to using zX's setting for the date format
            	 */
                df = getZx().getTimestampFormat();
                dbStrValue = "#" + df.format(pdatValue) + "#";
                
            }
            
        }
        
        return dbStrValue;
    }
    
    /**
     * Get a db string value from a string.
     * 
     * <pre>
     * 
     * NOTE : There is no dbGetStrValue. Check if this causes any problems.
     * 
     * Extracted from dbStrValue, to be independent of having a BO or Attr.
     * 
     * Revised for 1.5:1 - Added database type
	 * </pre>
     * 
     * @param penmDataType The dataType of the value
     * @param pstrValue Value to wrap in sql escapes
     * @param penmDBType The databasetype.
     * @return Return db string value
     * @throws ZXException Thrown if dbStrValue fails
     */
    public String dbStrValue(int penmDataType, String pstrValue, zXType.databaseType penmDBType) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pemnDataType", penmDataType);
            getZx().trace.traceParam("pstrValue", pstrValue);
        }
        
        String dbStrValue = null;
        
        try {
            /**
             * Database type should really be passed but to be back-ward compatible we
             * had to make this parameter optional in which case we assume we are pre-data source
             * era and can refer to DB databasetype
             */
            // Java version will not be backwards compatiable with 1.4 applications.
            
            if (penmDataType == zXType.dataType.dtAutomatic.pos 
                    || penmDataType == zXType.dataType.dtDouble.pos
                    || penmDataType == zXType.dataType.dtLong.pos) {
                dbStrValue = pstrValue;
                
            } else if (penmDataType == zXType.dataType.dtBoolean.pos) {
                boolean blnValue = StringUtil.booleanValue(pstrValue);
                if (penmDBType.equals(zXType.databaseType.dbOracle) 
                        || penmDBType.equals(zXType.databaseType.dbDB2)
                        || penmDBType.equals(zXType.databaseType.dbAS400)
                    ) {
                    /**
                     * For Oracle and DB2, we assume that the datatype to contain a boolean = char(1)
                     */
                    if (blnValue) {
                        dbStrValue = "'Y'";
                    } else {
                        dbStrValue = "'N'";
                    }
                    
                } else if (penmDBType.equals(zXType.databaseType.dbMysql) 
                        || penmDBType.equals(zXType.databaseType.dbHsql)
                        || penmDBType.equals(zXType.databaseType.dbSQLServer)
                    ) {
                    /**
                     * CBM17MAR04
                     * Changed to support SQLServer boolean values
                     */
                    /**
                     * BIT
                     * BOOL
                     * BOOLEAN
                     * These are synonyms for TINYINT(1). 
                     * The BOOLEAN synonym was added in version 4.1.0 
                     * Full boolean type handling will be introduced in accordance with SQL-99.
                     */
                    if (blnValue) {
                        dbStrValue = "1";
                    } else {
                        dbStrValue = "0";
                    }
                } else {
                    /**
                     * In Access use true / false
                     */
                    if (blnValue) {
                        dbStrValue = "true";
                    } else {
                        dbStrValue = "false";
                    }
                }
                
            } else if(penmDataType == zXType.dataType.dtString.pos 
                    	|| penmDataType == zXType.dataType.dtExpression.pos) {
		        /**
		         * Escape any single quotes.
		         **/
		        pstrValue = StringUtil.replaceAll(pstrValue, '\'', "''");
                
		        /**
		         * Handle null values.
		         * NOTE : This should really be trapped by the callee of this method.
		         */
		        if (pstrValue == null) pstrValue = "";
		        
		        /**
		         * Wrap in single quotes : 
		         */
		        dbStrValue = "'" + pstrValue + "'";
		    
            /**
             * pstrValue - Should be formatted according to its display date form.
             * This will convert it to the format used in the database.
             */    
            } else if (penmDataType == zXType.dataType.dtDate.pos)  {
                Date date = DateUtil.parse(getZx().getDateFormat(), pstrValue);
                dbStrValue = dbStrValue(penmDataType, date, penmDBType);
                
            } else if (penmDataType == zXType.dataType.dtTime.pos)  {
				Date date =  DateUtil.parse(getZx().getTimeFormat(), pstrValue);
				dbStrValue = dbStrValue(penmDataType, date, penmDBType);
                
            } else if (penmDataType == zXType.dataType.dtTimestamp.pos) {
            	Date date =  DateUtil.parse(getZx().getTimestampFormat(), pstrValue);
				dbStrValue = dbStrValue(penmDataType, date, penmDBType);
				
            }
            
            return dbStrValue;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Extracted from dbStrValue, to be independent of having a BO or Attr", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmDataType = " + penmDataType);
                getZx().log.error("Parameter : pstrValue = " + pstrValue);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return dbStrValue;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(dbStrValue);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate an in clause retrieving data from the database.
     *
     * <pre>
     *  
     * Assumes : returns something like
     * 
     * (1, 2, 5, 7)
     * 
     * or empty string in case of disaster
     * 
     * Rewrite for 1.5:1
     * </pre>
     * 
     * @param pobjBO The business object you want the in clause for.
     * @param pstrAttr The attribute you want to get the in clause for.
     * @param pstrWhereGroup The primary key attribute group you want to use. Optional
     *                       whereGroup, default is ""
     * @param pblnUseEqual Whether to use equals or not
     * @param pblnUseOr Whether to use OR.
     * @return Returns a IN CLAUSE sql statement.
     * @throws ZXException Thrown if inClause fails.
     */
    public String inClause(ZXBO pobjBO, String pstrAttr, String pstrWhereGroup,
            			   boolean pblnUseEqual, boolean pblnUseOr) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrAttr",  pstrAttr);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
            getZx().trace.traceParam("pblnUseEqual", pblnUseEqual);
            getZx().trace.traceParam("pblnUseOr", pblnUseOr);
        }
        
        StringBuffer inClause = new StringBuffer();
        
        DSRS objRS = null;
        
        /**
         * Defaults :
         */
        if (pstrWhereGroup == null) {
            pstrWhereGroup = "";
        }
        
        try {
            /**
             * Create where clause
             */
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            if (objDSWhereClause.parse(pobjBO, pstrWhereGroup, pblnUseEqual, pblnUseOr).pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Unable to generate where clause");
            }
            
            /**
             * Get DS handler
             */
            DSHRdbms objDSHandler = (DSHRdbms)pobjBO.getDS();
            
            objRS = objDSHandler.boRS(pobjBO, pstrAttr, objDSWhereClause.getAsWhereClause());
            if (objRS == null) {
                throw new ZXException("Unable to create recordset");
            }
            
            while (!objRS.eof()) {
                objRS.rs2obj(pobjBO, pstrAttr);
                
                if (inClause.length() > 0) {
                    inClause.append(", ");
                }
                
                inClause.append(dbValue(pobjBO, pobjBO.getDescriptor().getAttribute(pstrAttr)));
                
                objRS.moveNext();
            }
            
            objRS.RSClose();
            
            /**
             * Wrap SQL Statement in brackets.
             */
            inClause.insert(0,'(');
            inClause.append(')');
            
            return inClause.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate an in clause retrieving data from the database ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrAttr = " + pstrAttr);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
                getZx().log.error("Parameter : pblnUseEqual = " + pblnUseEqual);
                getZx().log.error("Parameter : pblnUseOr = " + pblnUseOr);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return inClause.toString();
        } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) {
            	objRS.RSClose();
            }
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(inClause);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Take into consideration the database and make the argument case
     * insensitive.
     * 
     * Added penmDBType for 1.5:1
     * 
     * @param pstrArgument The value to make case insensative.
     * @param penmDBType The database type.
     * @return Returns a sql expression of allow for case insensative searches
     */
    public String makeCaseInsensitive(String pstrArgument, zXType.databaseType penmDBType) {
        String makeCaseInsensitive;
        
        if (penmDBType.equals(zXType.databaseType.dbMysql) 
        	|| penmDBType.equals(zXType.databaseType.dbHsql)
            || penmDBType.equals(zXType.databaseType.dbOracle)
            || penmDBType.equals(zXType.databaseType.dbDB2)
            || penmDBType.equals(zXType.databaseType.dbAS400)) {
            makeCaseInsensitive = "UPPER(" + pstrArgument + ")";
            
        } else {
        	/**
        	 * Acess/SQL Server only can do case insensitive searches.
        	 */
            makeCaseInsensitive = pstrArgument;
        }
        
        return makeCaseInsensitive;
    }
    
    /**
     * Return database variation for system date.
     * 
     * @param penmDBType The database type.
     * @return Returns the database variation of the system date
     */
    public String systemDate(zXType.databaseType penmDBType){
        String systemDate = "";
        
        if (penmDBType.equals(zXType.databaseType.dbAccess)) {
            systemDate = "now";
        } else if (penmDBType.equals(zXType.databaseType.dbSQLServer)) {
            systemDate = "GETDATE()";
        } else if (penmDBType.equals(zXType.databaseType.dbMysql)) {
            systemDate = "now";
        } else if (penmDBType.equals(zXType.databaseType.dbHsql)) {
            systemDate = "now()";
        } else if (penmDBType.equals(zXType.databaseType.dbOracle)) {
            systemDate = "sysdate";
        } else if (penmDBType.equals(zXType.databaseType.dbDB2)
                   || penmDBType.equals(zXType.databaseType.dbAS400)) {
            systemDate = "current date";
        }
        
        return systemDate;
    }
    
    /**
    * Returns where condition that can be added to update / delete query
    * to implement concurrency control.
    * 
    * <pre>
    * 
    * Basically when we update / delete a row we make sure that it is the same
    * row as when we retrieved it. We can tell by looking at the zXUPdtdId.
    * This is either the same value as when we retrieved it or
    * our session / sub-session id. The latter means that we can update a row
    * ourselves (e.g. in a different frame) and all will still be fine.
    * 
    * There is the 1 in a 10000000 chance that our session-id or sub-session-id
    * happens to coincide with an existing zXUpdtdId; if this happens I buy the
    * drinks....
    * 
    * Also, since we truncate the subsession to 9 positions (as it is a string and
    * may thus cause overflow when too long) it could be that we make this even worse.
    * Also, for performance we only look at the length of the -ss entry and assume it
    * is a number. So NEVER enter -ss ni a querystring or so as a non-number!
    *</pre>
    *
    * @param pobjBO The business object to use.  
    * @return Returns the where condition sql for concurrenct control.
    * @throws ZXException Thrown if concurrencyControlWhereCondition fails. 
    */
    public String concurrencyControlWhereCondition(ZXBO pobjBO) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
        }
        
        StringBuffer concurrencyControlWhereCondition = new StringBuffer();
        
        try {
            /**
             * Only if BO is set-up for concurrency control
             */
            if (pobjBO.getDescriptor().isConcurrencyControl()) {
                concurrencyControlWhereCondition.append('(').append(whereCondition(pobjBO, "zXUpdtdId")).append(" OR ");
                
                Property objProperty;
                
                /**
                 * If a subsession is available, use that, otherwise use
                 * session-id. See zX.clsBOS.setAuditAttr for more info
                 */
                String strSS = getZx().getQuickContext().getEntry("-ss");
                int length = StringUtil.len(strSS); 
                if (length > 0) {
                    // Trim the session down to size ? - This may not be an issue in the java version.
                    /**
                     * DGS21FEB2005 - V1.4:50: Take the rightmost 9, not leftmost,
                     * for maximum chance of uniqueness
                     */
                    if (length > 9) strSS = strSS.substring(length-9);
                    objProperty = new StringProperty(strSS);
                } else {
                    objProperty = getZx().getSession().getValue("id");
                }
                
                concurrencyControlWhereCondition.append(singleWhereCondition(pobjBO,
                                                        pobjBO.getDescriptor().getAttribute("zXUpdtdId"),
                                                        zXType.compareOperand.coEQ, objProperty));
                concurrencyControlWhereCondition.append(')');
            }
            
            return concurrencyControlWhereCondition.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Returns where condition that can be added to update / delete query", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return concurrencyControlWhereCondition.toString();
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(concurrencyControlWhereCondition);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Util methods.
    
    /**
     * Use the db to convert the column to String.
     *
     * EG : for Oracle do a "TO_CHAR("  +  pstrColumn + ")"
     * Revised for v1.5:67
     * 
     * @param penmType The database type.
     * @param pstrColumn The column to wrap.
     * @param pintLength The length of the column's data.
     * @return Returns a wrapped column
     */
    public static String dbStringWrapper(zXType.databaseType penmType, String pstrColumn, int pintLength) {
        StringBuffer getDBToChar = new StringBuffer(32);
        if (penmType.equals(zXType.databaseType.dbOracle)) {
            getDBToChar.append("TO_CHAR(").append(pstrColumn).append(")");
            
        } else if (penmType.equals(zXType.databaseType.dbDB2) 
        		   || penmType.equals(zXType.databaseType.dbAS400)) {
            getDBToChar.append("CHAR(").append(pstrColumn).append(")");
            
        } else if (penmType.equals(zXType.databaseType.dbMysql)) {
            getDBToChar.append("CHAR(").append(pstrColumn).append(")");
            
        } else if (penmType.equals(zXType.databaseType.dbHsql)) {
            getDBToChar.append("CHAR(").append(pstrColumn).append(")");
            
        } else if (penmType.equals(zXType.databaseType.dbAccess)) {
            getDBToChar.append("CSTR(").append(pstrColumn).append(")");
            
        } else if (penmType.equals(zXType.databaseType.dbSQLServer)) {
        	/**
        	 * v1.5:67 DGS08NOV2005: SQL Server is not the same as Access. Use CAST.
        	 */
            getDBToChar.append("CAST(").append(pstrColumn).append(" AS  VARCHAR(").append(pintLength).append("))");
            
        } else {
        	/**
        	 * Default to the most common.
        	 */
            getDBToChar.append("CHAR(").append(pstrColumn).append(")");
        }
        
        return getDBToChar.toString();
    }
    
    /**
     * Generate row limiting code for this DB type
     * C1.4:71: New function that is used for example by pageflow listform
     * 
     * NOTE : This can also support offsets.
     * 
     * @param pstrQuery The sql query to execute.
     * @param plngMaximumRows The number rows to retrieve
     * @param penmDBType The database type.
     * @return Returns a sql query wrapped in a sql db row limit.
     */
    public String dbRowLimit(String pstrQuery, int plngMaximumRows, zXType.databaseType penmDBType) {
        int selectIndex = pstrQuery.toLowerCase().indexOf("select");
        
        /**
         * Not a select query so do not wrap query
         */
        if (selectIndex == -1) return pstrQuery;
        
        if (penmDBType.equals(zXType.databaseType.dbOracle)) {
            /**
             * NOTE : Oracle supports offsets
             */
            StringBuffer dbRowLimit = new StringBuffer(pstrQuery.length() + 38);
            dbRowLimit.append("select * from ( ");
            dbRowLimit.append(pstrQuery);
            dbRowLimit.append(" ) where rownum <= ").append(plngMaximumRows);
            return dbRowLimit.toString();
            
        } else if (penmDBType.equals(zXType.databaseType.dbAS400)) {
            /**
             * This is correct for AS/400 and Universal Database, but other dialects of DB2
             * may need something using the rownumber() function
             */
            return new StringBuffer(pstrQuery.length() + 40)
                        .append(pstrQuery)
                        .append(" fetch first ")
                        .append(plngMaximumRows)
                        .append(" rows only ")
                        .toString();
            
        } else if (penmDBType.equals(zXType.databaseType.dbDB2)) {
            /**
             * NOTE : DB2 supports offsets
             */
            StringBuffer dbRowLimit = new StringBuffer(pstrQuery.length()+100);
            dbRowLimit.append(pstrQuery.substring(0, selectIndex)); //add the comment
            dbRowLimit.append("select * from ( select ");           //nest the main query in an outer select
            
            StringBuffer rownumber = new StringBuffer(50).append("rownumber() over(");
            int orderByIndex = pstrQuery.toLowerCase().indexOf("order by");
            if (orderByIndex > 0 && pstrQuery.toLowerCase().indexOf("select distinct")!= -1) {
                rownumber.append(pstrQuery.substring(orderByIndex));
            }
            rownumber.append(") as rownumber_,");
            
            dbRowLimit.append(rownumber); //add the rownnumber bit into the outer query select list
            dbRowLimit.append(pstrQuery.substring(selectIndex + 6)); //add the main query
            dbRowLimit.append(" ) as temp_ where rownumber_ ");
            dbRowLimit.append("<= ?");
            
            return dbRowLimit.toString();
            
        } else if (penmDBType.equals(zXType.databaseType.dbSQLServer)) {
            /**
             * Try to get the insert point.
             */
            final int selectDistinctIndex = pstrQuery.toLowerCase().indexOf("select distinct");
            selectIndex = selectIndex + ( selectDistinctIndex == selectIndex ? 15 : 6 );
            
            return new StringBuffer(pstrQuery.length()+8)
                           .append(pstrQuery)
                           .insert(selectIndex, " top " + plngMaximumRows) // Insert the TOP keyword.
                           .toString();
            
        } else if (penmDBType.equals(zXType.databaseType.dbMysql)) {
            /**
             * NOTE : Mysql also supports offsets.
             * This is useful in large record sets
             */
            return new StringBuffer(pstrQuery.length() + 8)
                            .append(pstrQuery)
                            .append(" LIMIT ").append(plngMaximumRows)
                            .toString();
            
        } else if (penmDBType.equals(zXType.databaseType.dbHsql)) {
            /**
             * NOTE : Hsql supports offsets.
             */
            return new StringBuffer(pstrQuery.length() + 8)
                            .append(pstrQuery)
                            .append(" LIMIT ").append(plngMaximumRows)
                            .toString();
            
        } else {
            /**
             * Otherwise not supported
             */
            return pstrQuery;
        }
        
    }
}