/*
 * Created on Apr 14, 2004 by Michael Brewer
 * $Id: PFQuery.java,v 1.1.2.28 2006/07/17 16:08:58 mike Exp $ 
 */
package org.zxframework.web;

import java.text.DateFormat;
import java.util.Iterator;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSWhereClause;
import org.zxframework.property.StringProperty;
import org.zxframework.sql.QueryDef;
import org.zxframework.util.DateUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Pageflow query object.
 * 
 * <pre>
 * 
 * This is a non visual pageflow action and therefore normally has a visual link action associated with it.
 * PFQuery role is to generate sql queries (Select queries) and put them into the session to be executed later on.
 * 
 * Change    : DGS13JAN2003
 * Why       : Changes for QueryDef. Also fixed problem where some literals with zx have
 *             at some point been inadvertently changed to i.zX.
 * 
 * Change    : BD28MAY03
 * Why       : Querydef no longer has its own context
 * 
 * Change    : DGS30JUN2003
 * Why       : In GoSearchForm added ability to use Order By from entity definition as well
 *             as existing use of X1, X2, etc. Also changed GoAll so that entity Order By
 *             takes precedence over X1, X2, etc.
 * 
 * Change    : DGS27AUG2003
 * Why       : Change to goSearchForm to support saved queries.
 * 
 * Change    : DGS02FEB2004
 * Why       : All querynames now resolved as directors (some were already)
 * 
 * Change    : DGS25FEB2004
 * Why       : Support for null and not null operators
 * 
 * Change    : DGS21APR2004
 * Why       : New group by, source query name and query where clause properties
 * 
 * Change    : BD23JUN04
 * Why       : Allow querydef name to be director
 * 
 * Change    : DGS09JUL2004
 * Why       : In goSearchForm, if the user has selected an order by, use it to completely
 *             override any in the PF order by attr group. Otherwise there is a danger
 *             that the user could choose to order by the same column and get an error
 * 
 * Change    : DGS21JUL2004
 * Why       : In goSearchForm and other gos, similar problem to previous, in this instance
 *             if the user has selected the same column in X1 and X2 for example
 * 
 * Change    : DGS17AUG2004
 * Why       : In goSearchForm and goAll, if appending an OR or AND query, don't try
 *             to append ( ) with nothing in the parentheses as it causes a DB error.
 * 
 * Change    : BD17DEC04 - V1.4:9
 * Why       : Added support for QS queryType
 * 
 * Change    : DGS11JAN2005
 * Why       : In goSearchForm and goAll, the change made on 17AUG2004 was not quite
 *             right. Now fixed.
 * 
 * Change    : DGS13JAN2005
 * Why       : In the various  go* functions, when preparing the order by group the returned
 *             collection can be modified, so must get a safe copy using getGroupSafe, not getGroup
 * 
 * Change    : BD6FEB05 - V1.4:35
 * Why       : Finally implemented the feature 'where clause only'
 *             Note that this is implemented in a way that is not the
 *             most elegant; we simply do not store the select / order by /
 *             group by in the context but may still generate these.
 *             I decided to do it this way to limit the risk of breaking
 *             things that work well...
 *                
 * Change    : BD18FEB05 - V1.4:42
 * Why       : Fixed problem with QS query type when there are multiple
 *             entities: the where conditions for each entity should be
 *             OR-ed together, not AND-ed
 *                
 * Change    : DGS21FEB2005 - V1.4:48
 * Why       : In goAll, when being used by the QS query type, use the entity's search group
 *             (whereGroup) if it is set, otherwise use literal 'QS' as before.
 * 
 * Change    : BD31MAR05 - V1.5:1
 * Why       : Support for data-sources
 * 
 * Change    : BD29JUN05 - V1.5:23
 * Why       : Bug in notAssociatedWith when middle and right entity cannot
 *             be joined and no instances of middle entity are found and
 *             middle entity is not a channel (so no wonder we only found it
 *             now...)
 * 
 * Change    : MB2JUN06 - V1.5:98
 * Why       : Support for where condition on middle entity in notAssociatedWith
 *
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFQuery extends PFAction {

    //------------------------ Members
    
    private boolean distinct;
    private boolean searchform;
    private boolean outerjoin;
    private boolean whereclauseonly;
    private zXType.pageflowQueryType queryType;
    private String entityl;
    private String entitym;
    private String entityr;
    private String query;
    private String querydefname;
    private String querydefscope;
    private PFQryExpr queryexpr;
    
    /**
     * DGS 21APR2004: Added Group By, Source Query Name and Query Where Clause.
     */
    private boolean groupby;
    private String sourcequeryname = "";
    private zXType.pageflowQueryWhereClause queryWhereClause = zXType.pageflowQueryWhereClause.pqwcStandard;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFQuery() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    /**
     * Whether to use the distinct sql keyword to return only unique results.
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
     * What type of query do we want to perform.
     *  
     * @return Returns the queryType. 
     */
    public zXType.pageflowQueryType getQueryType() {
        return queryType;
    }

    /**
     * @param queryType The queryType to set.
     */
    public void setQueryType(zXType.pageflowQueryType queryType) {
        this.queryType = queryType;
    }

    /**
     * The name of the left entity.
     *  
     * @return Returns the entityl. 
     */
    public String getEntityl() {
        return entityl;
    }

    /**
     * @param entityl The entityl to set.
     */
    public void setEntityl(String entityl) {
        this.entityl = entityl;
    }

    /**
     * The name of the middle entity.
     * 
     * @return Returns the entitym. 
     */
    public String getEntitym() {
        return entitym;
    }

    /**
     * @param entitym The entitym to set.
     */
    public void setEntitym(String entitym) {
        this.entitym = entitym;
    }

    /**
     * The name of the right entity.
     * 
     * @return Returns the entityr. 
     */
    public String getEntityr() {
        return entityr;
    }

    /**
     * @param entityr The entityr to set.
     */
    public void setEntityr(String entityr) {
        this.entityr = entityr;
    }

    /**
     * Whether to perform a SQL outer join in this query.
     * 
     * @return Returns the outerjoin. 
     */
    public boolean isOuterjoin() {
        return outerjoin;
    }

    /**
     * @param outerjoin The outerjoin to set.
     */
    public void setOuterjoin(boolean outerjoin) {
        this.outerjoin = outerjoin;
    }
    
    /**
     * The query.
     *  
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
     * The name of the query defintion.
     * 
     * @return Returns the querydefname. 
     **/
    public String getQuerydefname() {
        return querydefname;
    }

    /**
     * @param querydefname The querydefname to set.
     */
    public void setQuerydefname(String querydefname) {
        this.querydefname = querydefname;
    }

    /**
     * The scope of the query.
     * 
     * @return Returns the querydefscope. 
     */
    public String getQuerydefscope() {
        return querydefscope;
    }

    /**
     * @param querydefscope The querydefscope to set.
     */
    public void setQuerydefscope(String querydefscope) {
        this.querydefscope = querydefscope;
    }

    /**
     * The query expression.
     * 
     * @return Returns the queryexpr. 
     */
    public PFQryExpr getQueryexpr() {
        return queryexpr;
    }

    /**
     * @param queryexpr The queryexpr to set.
     */
    public void setQueryexpr(PFQryExpr queryexpr) {
        this.queryexpr = queryexpr;
    }
    
    /** 
     * Whether this query is for a search form. (Was the prior action a search form).
     * 
     * @return Returns the searchform. 
     */
    public boolean isSearchform() {
        return searchform;
    }

    /**
     * @param searchform The searchform to set.
     */
    public void setSearchform(boolean searchform) {
        this.searchform = searchform;
    }

    /** 
     * Whether to have the where clause only.
     * 
     * @return Returns the whereclauseonly. 
     */
    public boolean isWhereclauseonly() {
        return whereclauseonly;
    }

    /** 
     * @param whereclauseonly The whereclauseonly to set. 
     */
    public void setWhereclauseonly(boolean whereclauseonly) {
        this.whereclauseonly = whereclauseonly;
    }
    
    /**
     * The type of where clause to perform - Default is standard.
     * 
     * @return Returns the queryWhereClause.
     */
    public zXType.pageflowQueryWhereClause getQueryWhereClause() {
        if (this.queryWhereClause == null) {
           this.queryWhereClause = zXType.pageflowQueryWhereClause.pqwcStandard;
        }
        return queryWhereClause;
    }
    
    /**
     * @param queryWhereClause The queryWhereClause to set.queryWhereClause
     */
    public void setQueryWhereClause(zXType.pageflowQueryWhereClause queryWhereClause) {
        this.queryWhereClause = queryWhereClause;
    }
    
    /**
     * Whether we want to group the results.
     * 
     * @return Returns the groupby.
     */
    public boolean isGroupby() {
        return groupby;
    }
    
    /**
     * @param groupby The groupby to set.
     */
    public void setGroupby(boolean groupby) {
        this.groupby = groupby;
    }
    
    /**
     * The name of the source query.
     * 
     * @return Returns the sourcequeryname.
     */
    public String getSourcequeryname() {
        return sourcequeryname;
    }
    
    /**
     * @param sourcequeryname The sourcequeryname to set.
     */
    public void setSourcequeryname(String sourcequeryname) {
        this.sourcequeryname = sourcequeryname;
    }    
    
    //------------------------ Digester helper methods
    
    /**
     * @deprecated Using BooleanConverter
     * @param groupby The groupby to set.
     */
    public void setGroupby(String groupby) {
        this.groupby = StringUtil.booleanValue(groupby);
    }

    /**
     * @deprecated Using BooleanConverter
     * @param distinct The distinct to set.
     */
    public void setDistinct(String distinct) {
        this.distinct = StringUtil.booleanValue(distinct);
    }

    /**
     * @deprecated Using BooleanConverter
     * @param outerjoin The outerjoin to set.
     */
    public void setOuterjoin(String outerjoin) {
        this.outerjoin = StringUtil.booleanValue(outerjoin);
    }

    /**
     * @deprecated Using BooleanConverter
     * @param searchform The searchform to set.
     */
    public void setSearchform(String searchform) {
        this.searchform = StringUtil.booleanValue(searchform);
    }

    /**
     * @deprecated Using BooleanConverter
     * @param whereclauseonly The whereclauseonly to set. 
     */
    public void setWhereclauseonly(String whereclauseonly) {
        this.whereclauseonly = StringUtil.booleanValue(whereclauseonly);
    }
    
    /**
     * @param queryWhereClause The queryWhereClause to set.
     */
    public void setQuerywhereclause(String queryWhereClause) {
        this.queryWhereClause = zXType.pageflowQueryWhereClause.getEnum(queryWhereClause);
    }
    
    /**
     * @param queryType The queryType to set.
     */
    public void setQuerytype(String queryType) {
        this.queryType = zXType.pageflowQueryType.getEnum(queryType);
    }
    
    //------------------------ Private helper methods
    
    /**
     * Handle search form query.
     * 
     * Reviewed for V1.5:1
     * 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if goSearchForm fails. 
     */
    private zXType.rc goSearchForm() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc goSearchForm = zXType.rc.rcOK; 

        try {
            Iterator iter;
            PFEntity objEntity;                     // To loop over entities
            DSHandler objDSHandler;                 // Handle to handler shared by all entities
            String strSelectClause = "";                 // Select clause either for RDBMS or channel
            StringBuffer strWhereClause;                  // Where clauese
            StringBuffer strOrderByClause;                // Order by clause
            StringBuffer strGroupByClause;                // Group by clause (only for RDBMS)
            
            /**
             * Determine entity collection and set context
             */
            ZXCollection colEntities = getPageflow().getEntityCollection(this,
            															 zXType.pageflowActionType.patQuery,
            															 zXType.pageflowQueryType.pqtSearchForm);
            if (colEntities == null) {
                throw new Exception("Unable to retrieve entity collection");
            }
            
            /**
             * We do not support more than one entity when we are dealing with a channel data source
             * as this will never support a join
             */
            if (!getPageflow().validDataSourceEntities(colEntities)) {
                throw new Exception("Entity collection has data-source combination that is not supported");
            }
            
            getPageflow().setContextEntities(colEntities);
            PFEntity objEntity1 = (PFEntity)colEntities.iterator().next();
            getPageflow().setContextEntity(objEntity1);
            
            /**
             * Take the data-source handler from the first entity
             */
            objDSHandler = getPageflow().getContextEntity().getDSHandler();
            boolean blnIsDSChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            /**
             * Different select clause for channel and RDBMS data source
             */
            if (blnIsDSChannel) {
                /**
                 * For channels we never support multiple entities in single query so simply
                 * store attribute group as select clause
                 */
                strSelectClause = getPageflow().getContextEntity().getSelectlistgroup();
                
            } else {
                /**
                 * Full SQL query for RDBMS data sources
                 */
                
                /**
                 * BD6FEB05 - Take into consideration the where claus only feature
                 * should not have evaluated the selected clause at all but since
                 * this feature was backfitted later, decided to do it like this
                 * (ie simply not store) instead of trying to be too clever
                 */
                if (!isWhereclauseonly()) {
                    /**
                     * First do the select / from / join bit
                     */
                    int arrSize = colEntities.size();
                    ZXBO[] arrZXBO = new ZXBO[arrSize];
                    String[] arrSelectlistStr = new String[arrSize];
                    boolean[] arrResolveBln = new boolean[arrSize];
                    int j = 0;
                    
                    iter = colEntities.iterator();
                    while(iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        
                        /**
                         * Include entity when either the result is requested (listGroup) or the
                         * where clause is requested (whereGroup)
                         */
                        if (StringUtil.len(objEntity.getSelectlistgroup()) > 0 || StringUtil.len(objEntity.getWheregroup()) > 0) {
                            arrZXBO[j] = objEntity.getBo();
                            arrSelectlistStr[j] = objEntity.getSelectlistgroup();
                            arrResolveBln[j] = isResolvefk();
                            // NOTE :This may leave empty array elements at the end.
                            j++;
                        }
                    }
                    
                    strSelectClause = getZx().getSql().selectQuery(arrZXBO, 
                                                                            arrSelectlistStr, 
                                                                            arrResolveBln, 
                                                                            this.distinct, 
                                                                            this.outerjoin, 
                                                                            false, false, false);
                    if (StringUtil.len(strSelectClause) == 0) {
                        throw new Exception("Unable to generate select query");
                    }
                }
                
            } // DS handler type
            
            String strQueryName = getPageflow().resolveDirector(getQueryname());
            
            /**
             * And store it in the context
             *
             * BD6FEB05 - Take into consideration the where claus only feature
             * should not have evaluated the selected clause at all but since
             * this feature was backfitted later, decided to do it like this
             * (ie simply not store) instead of trying to be too clever
             */
            if (!this.whereclauseonly) {
                getPageflow().addToContext(strQueryName + Pageflow.QRYSELECTCLAUSE, strSelectClause);
            }
            
            /**
             * Now do the where clause
             * DGS27AUG2003: Assume no criteria entered by user, but as soon as any found,
             * set this boolean to True. That tells us not to try to get a saved query.
             */
            String strTmp = "";
            // Set when user has entered search criteria
            // (otherwise may want to use saved query)
            boolean blnUserQuery = false;
            
            strWhereClause = new StringBuffer(32);
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                if (StringUtil.len(objEntity.getWheregroup()) > 0) {
                    strTmp = getPageflow().getPage().processSearchForm(getPageflow().getRequest(), objEntity.getBo(), objEntity.getWheregroup());
                    if (StringUtil.len(strTmp) > 0) {
                        /**
                         * If user has entered criteria, must overwrite any
                         *  selected saved query by new criteria, so set boolean accordingly:
                         */
                        blnUserQuery = true;
                        
                        /**
                         * Difference in channel and RDBMS
                         */
                        if (blnIsDSChannel) {
                            objDSWhereClause.addClauseWithAND(objEntity.getBo(), ":" + strTmp);
                            
                        } else {
                            if (strWhereClause.length() > 0) {
                                strWhereClause.append(" AND ").append(strTmp);
                            } else {
                                strWhereClause.append(strTmp);
                            }
                            
                        } // Channel or RDBMS
                        
                    } // processSearchForm has generated something?
                    
                } // Has where group
                
            } // Loop over entities
            
            /**
             * DGS21JUL2004: Ensure no duplication between PF Group by group and user-selected X1 to X5 
             * Otherwise there is a danger that the user could choose to order by the same column we have 
             * already included, and that causes a nasty error in some DBMSs (well, in SQL Server).
             */
            strOrderByClause = new StringBuffer();
            AttributeCollection colAttrOrderBy;
            
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                
                if (StringUtil.len(objEntity.getGroupbygroup()) > 0) {
                    
                    /**
                     * Difference between channel and RDBMS data source handler
                     */
                    if (blnIsDSChannel) {
                        /**
                         * For channels there will ever be only one entity so can store directly
                         * in strOrderByClause and not worry that this is overwritten in next iteration
                         */
                        strOrderByClause = new StringBuffer(objEntity.getGroupbygroup());
                        
                    } else {
                        /**
                         * For RDBMS may have multiple entities in which case we have to construct
                         * single ORDER BY for all of them
                         */
                        
                        // "-" means descending.
                        if (objEntity.getGroupbygroup().charAt(0) == '-') {
                            strTmp = getZx().getSql().orderByClause(objEntity.getBo(), objEntity.getGroupbygroup().substring(1), true, true);
                        } else {
                            strTmp = getZx().getSql().orderByClause(objEntity.getBo(), objEntity.getGroupbygroup(), false, true);
                        } // ASC or DESC?
                        
                        if (StringUtil.len(strTmp) > 0) {
                            if (strOrderByClause.length() > 0) {
                                strOrderByClause.append(",").append(strTmp);
                            } else {
                                strOrderByClause.append(strTmp);
                            }
                        }
                        
                    } // Channel or RDBMS?
                    
                } // Has groupByGroup?
                
                /**
                 * First the entity groupby group.
                 * 
                 * DGS13JAN2005: This returned collection can be modified, so must get a safe copy
                 * of the collection of attributes. Using getGroup is not safe because it returns
                 * a reference to the group, and can thus cause corruption in the source group.
                 */
                colAttrOrderBy = objEntity.getBODesc().getGroupSafe(objEntity.getGroupbygroup());
                
                /**
                 * Now see of we have to add the order by from the search form
                 */
                String[] arrTmp = {"X1","X2","X3","X4","X5"};
                for (int i = 0; i < arrTmp.length; i++) {
                    strTmp = arrTmp[i];
                    
                    strTmp = getPageflow().getPage().processOrderBy(getPageflow().getRequest(), objEntity.getBo(), strTmp, colAttrOrderBy);
                    
                    /**
                     * Notice that there is no difference between channel and RDBMS
                     */
                    if (StringUtil.len(strTmp) > 0) {
                        if (strOrderByClause.length() > 0) {
                            strOrderByClause.append(", ");
                        }
                        strOrderByClause.append(strTmp);
                    }
                    
                } // Loop over X1 .. X5
                
            } // Loop over entities
            
            /**
             * DGS21APR2004: Can have Group By clause (only for non-channel data sources)
             */
            strGroupByClause = new StringBuffer(16);
            if (this.groupby && !blnIsDSChannel) {
                iter = colEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    /**
                     * Use the entity groupby group
                     */
                    if (StringUtil.len(objEntity.getGroupbygroup()) > 0) {
                        strTmp = getZx().getSql().groupByClause(objEntity.getBo(), objEntity.getGroupbygroup(), true);
                        
                        if (StringUtil.len(strTmp) > 0) {
                            if (strGroupByClause.length() > 0) {
                                strGroupByClause.append(", ");
                            }
                            strGroupByClause.append(strTmp);
                        }
                        
                    } // Has groupByGroup
                    
                } // Loop over enities
                
            } // Need groupby and is not a channel?
            
            /**
             * DGS27AUG2003: If have selected an existing query where clause from the screen,
             * get the where clause and order by. Note the meaning of the rc here:
             * rcError   = error (as usual)
             * rcWarning = either no saved query name entered, or the one entered is new and does
             *              not exist on the database
             * rcOk      = user entered or selected a saved query name that exists
             */
            StringBuffer strSavedWhereClause = new StringBuffer(20);
            StringBuffer strSavedOrderByClause = new StringBuffer(20);
            StringBuffer strInterimWhereClause = new StringBuffer(20);
            
            // return code of saved query handler
            zXType.rc rcSavedQuery = zXType.rc.rcOK;
            if (blnUserQuery) {
                rcSavedQuery = zXType.rc.rcWarning;
            } else {
                rcSavedQuery = getPageflow().getPage().processSavedQuery(getPageflow().getPFDesc().getName(),
                														 getPageflow().getRequest(),
                														 
                                                                         objEntity1.getBo().getDescriptor().getName(), 
                                                                         strSavedWhereClause, 
                                                                         strSavedOrderByClause);
            }
            
            if (rcSavedQuery.equals(zXType.rc.rcWarning)) {
                /**
                 * Remember the where clause at this interim stage because we might need to save it,
                 * and we don't want all the subsequent clauses saving. The order by is different.
                 */
                strInterimWhereClause = strWhereClause;
            } else {
                /**
                 * We found a saved search and it wasn't overridden in the where clause or order by...
                 */
                strWhereClause = strSavedWhereClause;
                strOrderByClause = strSavedOrderByClause;
            }
            
            /**
             * Add any conditions to whereclause based on pkWhereGroup
             */
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                getPageflow().processAttributeValues(objEntity);
                
                /**
                 * Difference between channels and RDBMS
                 */
                if (blnIsDSChannel) {
                    /**
                     * Channel handler, bit tricky as we have to merge where clauses
                     * Note how we once more rely on the fact that there is ever only going
                     * to be one entity in entity collection for channels
                     */
                    if (StringUtil.len(objEntity.getPkwheregroup()) > 0) {
                        objDSWhereClause.addClauseWithAND(objEntity.getBo(), objEntity.getPkwheregroup());
                        
                    } // No PK where group
                } else {
                    /**
                     * RDBMS handler
                     */
                    strTmp = getPageflow().processPKWhereGroup(objEntity);
                    
                    if (StringUtil.len(strTmp) > 0) {
                        if (StringUtil.len(strWhereClause) > 0) {
                            strWhereClause.append(" AND ").append(strTmp);
                        } else {
                            strWhereClause.append(strTmp);
                        }
                    }
                    
                } // Channel or RDBMS
                
            } // Loop over enities
            
            /**
             * DGS29042003: Query Expression
             */
            strTmp = processQueryExpr(getQueryexpr(), colEntities);
            
            if (StringUtil.len(strTmp) > 0) {
                if (blnIsDSChannel) {
                    objDSWhereClause.addClauseWithAND(getPageflow().getContextEntity().getBo(), ":" + strTmp);
                    
                } else {
                    if (StringUtil.len(strWhereClause) > 0) {
                        strWhereClause.append(" AND ( ").append(strTmp).append(" )");
                    } else {
                        strWhereClause = new StringBuffer(strTmp);
                    }
                    
                } // Channel or RDBMS?
                
            } // Has queryExpr resulted in anything?
            
            /**
             * DGS21APR2004: New processing for query where clause. If not 'standard' (default/normal
             * behaviour), get existing query using this query name or a different source query name
             * if supplied. Then either replace the one just generated by the one from the context, or
             * use the one from the context as the base and then either AND or OR the generated query
             */
            if (!this.queryWhereClause.equals(zXType.pageflowQueryWhereClause.pqwcStandard)) {
                
                if (StringUtil.len(this.sourcequeryname) > 0) {
                    strTmp = getPageflow().getFromContext(this.sourcequeryname + Pageflow.QRYWHERECLAUSE);
                } else {
                    strTmp = getPageflow().getFromContext(strQueryName + Pageflow.QRYWHERECLAUSE);
                }
                
                if (blnIsDSChannel) {
                    
                    if (queryWhereClause.equals(zXType.pageflowQueryWhereClause.pqwcReplace)) {
                        objDSWhereClause.parse(getPageflow().getContextEntity().getBo(), strTmp);
                        
                    } else if (queryWhereClause.equals(zXType.pageflowQueryWhereClause.pqwcAnd)) {
                        objDSWhereClause.addClauseWithAND(getPageflow().getContextEntity().getBo(), strTmp);
                        
                    } else {
                        objDSWhereClause.addClauseWithOR(getPageflow().getContextEntity().getBo(), strTmp);
                        
                    } // Type of where clause handling
                    
                } else {
                    
                    if (this.queryWhereClause.equals(zXType.pageflowQueryWhereClause.pqwcReplace)
                            || StringUtil.len(strWhereClause) == 0) {
                        strWhereClause = new StringBuffer(strTmp);
                        
                    } else {
                        /**
                         * DGS17AUG2004: Don't try to append ( ) with nothing in the parentheses as it causes DB error.
                         * 
                         * DGS11JAN2005: Above was not quite right - was testing length of strTmp but it is actually
                         * strWhereClause that ends up in the parentheses. Now if whereclause is empty, just replace
                         * it by strTmp (by extending the 'if' statement above).
                         */
                        if (StringUtil.len(strTmp) > 0) {
                            // Prepend the where clause with a AND or OR.
                            strWhereClause.insert(0,(this.queryWhereClause.equals(zXType.pageflowQueryWhereClause.pqwcAnd) ? " AND ( " : " OR ( ")); 
                            // Put the strTmp right at the beginning
                            strWhereClause.insert(0, strTmp);
                            // End of the Whereclause.
                            strWhereClause.append(" ) ");
                        }
                    }
                    
                } // Channel or RDBMS?
                
            } // Not standard where clause behaviour
            
            /**
             * And store it in the context
             */
            if (blnIsDSChannel) {
                getPageflow().addToContext(strQueryName + Pageflow.QRYWHERECLAUSE, objDSWhereClause.getAsWhereClause());
            } else {
                getPageflow().addToContext(strQueryName + Pageflow.QRYWHERECLAUSE, strWhereClause.toString());
            }
            
            /**
             * And store order / group by clauses if requested
             */
            if (!isWhereclauseonly()) {
                getPageflow().addToContext(strQueryName + "QryGroupByClause", strGroupByClause.toString());
                
                getPageflow().addToContext(strQueryName + "QryOrderByClause", strOrderByClause.toString());
            }
            
            /**
             * If didn't use an existing saved query, or did but overrode it,
             * and there is something in the query or order by, we need to persist this new one
             */
            if (rcSavedQuery.pos == zXType.rc.rcWarning.pos && StringUtil.len(strInterimWhereClause.toString() + strOrderByClause.toString()) > 0) {
                if (!getPageflow().getPage().persistSavedQuery(getPageflow().getPFDesc().getName(),
                											   getPageflow().getRequest(),
                											   
                                                               objEntity1.getBo().getDescriptor().getName(), 
                                                               strInterimWhereClause, 
                                                               strOrderByClause).equals(zXType.rc.rcOK)) {
                    throw new Exception("Failed to save the SQL query");
                }
            }
            
            /**
             * Determine where to go next
             */
            getPageflow().setAction(getPageflow().resolveLink(getLink()));
            
            return goSearchForm;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Handle search form query.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            goSearchForm = zXType.rc.rcError;
            return goSearchForm;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(goSearchForm);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Handle QueryDef query type.
     *
     * @param pobjQryExpr The Query Definition to use. 
     * @param pcolEntities A collection of linked form entities. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if processQueryExpr fails. 
     */
    public String processQueryExpr(PFQryExpr pobjQryExpr, ZXCollection pcolEntities) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjQryExpr", pobjQryExpr);
            getZx().trace.traceParam("pcolEntities", pcolEntities);
        }
        
        String processQueryExpr = ""; 
        
        /**
         * Exit early
         */
        if (pobjQryExpr == null) {
            return processQueryExpr;
        }
        
        try {
            Iterator iter;
            PFEntity objEntity;
            Attribute objAttr;
            String strAttr;
            String strLHS;
            String strRHS;
            
            objEntity = (PFEntity)pcolEntities.iterator().next();
            DSHandler objDSHandler = objEntity.getDSHandler();
            boolean blnIsDSChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            zXType.databaseType enmDBType = zXType.databaseType.dbAny;
            if (blnIsDSChannel) {
                enmDBType = ((DSHRdbms)objDSHandler).getDbType();
            }
            
            /**
             * Actually should never be 'nothing' because is preallocated, but just in case someone
             * changes that leave the above in for safety. The real test is here: if not recursive
             * and both sides are blank, there is no query expression
             */
            if (!pobjQryExpr.operatorIsRecursive() 
                && StringUtil.len(pobjQryExpr.getRhs()) == 0 && StringUtil.len(pobjQryExpr.getLhs()) == 0) {
                return processQueryExpr;
            }
            
            if (pobjQryExpr.operatorIsRecursive()) {
                /**
                 * Recursive - call to get next level down LHS and RHS
                 */
            	String strTmp = processQueryExpr(pobjQryExpr.getQueryexprlhs(), pcolEntities);
            	if (StringUtil.len(strTmp) > 0) {
                    strLHS = " ( " + strTmp + " )";
            	} else {
            		strLHS = "";
            	}
            	
                strTmp = processQueryExpr(pobjQryExpr.getQueryexprrhs(), pcolEntities);
                if (StringUtil.len(strTmp) > 0) {
                	strRHS = " ( " + strTmp + " )";
                } else {
                	strRHS = "";
                }
                
            } else {
                /**
                 * Not recursive
                 */
            	
                /**
                 * Datesformats to use for testing
                 */
                DateFormat arrDateFormats[] = {getZx().getDateFormat(),
                							   getZx().getTimestampFormat(),
                							   getZx().getTimeFormat()};
                /**
                 * LHS : Resolve the left handside expression :
                 */
                strLHS = getPageflow().resolveDirector(pobjQryExpr.getLhs());
                strAttr = getPageflow().resolveDirector(pobjQryExpr.getAttrlhs());
                
                if (StringUtil.len(strAttr) == 0) {
                    /** 
                     * No attribute - must be a simple director. Convert to best guess datatype 
                     **/
                    if (blnIsDSChannel) {
                        if (StringUtil.isNumeric(strLHS) || StringUtil.isDouble(strLHS)) {
                        	/**
                        	 * No need to modify left hand side.
                        	 */
                        } else if (DateUtil.isValid(arrDateFormats, strLHS)) {
                            strLHS = "#" + strLHS + "#";
                        } else {
                             strLHS = "'" + StringUtil.encodezXString(strLHS) + "'";
                        }
                        
                    } else {
                        if (StringUtil.isNumeric(strLHS)) {
                            strLHS = getZx().getSql().dbStrValue(zXType.dataType.dtLong.pos, strLHS, enmDBType);
                        } else if (StringUtil.isDouble(strLHS)) {
                            strLHS = getZx().getSql().dbStrValue(zXType.dataType.dtDouble.pos, strLHS , enmDBType);
                        } else if (DateUtil.isValid(arrDateFormats, strLHS)) {
                        	java.util.Date objDate = DateUtil.parse(arrDateFormats, strLHS);
                            strLHS = getZx().getSql().dbStrValue(zXType.dataType.dtDate.pos, objDate, enmDBType);
                        } else {
                            strLHS = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, strLHS, enmDBType);
                        }
                        
                    }
                    
                } else {
                    /**
                     * Have an attribute - is a database column name.
                     */
                    iter = pcolEntities.iterator();
                    while (iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        
                        if (objEntity.getName().equals(strLHS)) {
                            if (blnIsDSChannel){
                                strLHS = strAttr;
                                
                            } else {
                                objAttr = objEntity.getBo().getDescriptor().getAttribute(strAttr);
                                strLHS = getZx().getSql().columnName(objEntity.getBo(), 
                                                                     objAttr, 
                                                                     zXType.sqlObjectName.sonName);
                                
                            } // Channel or RDBMS
                            break;
                            
                        }
                        
                    } // Loop over entities
                    
                } // Attribute provided?
                
                /**
                 * RHS :  Resolve the right handside expression :
                 */
                strRHS = getPageflow().resolveDirector(pobjQryExpr.getRhs());
                strAttr = getPageflow().resolveDirector(pobjQryExpr.getAttrrhs());
                
                if (StringUtil.len(strAttr) == 0) {
                    /** 
                     * No attribute - must be a simple director. Convert to best guess datatype 
                     */
                    if (blnIsDSChannel) {
                        /**
                         * DGS25FEB2004: If a null-type operator, the right hand operand is not used
                         */
                        if (pobjQryExpr.getEnmoperator().equals(zXType.pageflowQueryExprOperator.pqeoNULL)
                            || pobjQryExpr.getEnmoperator().equals(zXType.pageflowQueryExprOperator.pqeoNOTNULL)) {
                        	/**
                        	 * We will handle nulls later on.
                        	 */
                            strRHS = "";
                            
                        } else if (StringUtil.isNumeric(strRHS) || StringUtil.isDouble(strRHS)) {
                        	/**
                        	 * No need to modify the right hand side value.
                        	 */
                        	
                        } else if (DateUtil.isValid(arrDateFormats, strRHS)) {
                            strRHS = "#" + strRHS + "#";
                            
                        } else {
                            strRHS = "'" + StringUtil.encodezXString(strRHS) + "'";
                            
                        }

                    } else {
                        /**
                         * DGS25FEB2004: If a null-type operator, the right hand operand is not used.
                         */
                        if (pobjQryExpr.getEnmoperator().equals(zXType.pageflowQueryExprOperator.pqeoNULL) 
                            || pobjQryExpr.getEnmoperator().equals(zXType.pageflowQueryExprOperator.pqeoNOTNULL)) {
                            strRHS = "";
                            
                        } else if (StringUtil.isNumeric(strRHS)) {
                            strRHS = getZx().getSql().dbStrValue(zXType.dataType.dtLong.pos, strRHS, enmDBType);
                            
                        }else if (StringUtil.isDouble(strRHS)) {
                            strRHS = getZx().getSql().dbStrValue(zXType.dataType.dtDouble.pos, strRHS, enmDBType);
                            
                        } else if (DateUtil.isValid(arrDateFormats, strRHS)) {
                        	java.util.Date objDate = DateUtil.parse(arrDateFormats, strRHS);
                            strRHS = getZx().getSql().dbStrValue(zXType.dataType.dtDate.pos, objDate, enmDBType);
                            
                        } else {
                            strRHS = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, strRHS, enmDBType);
                            
                        }
                        
                    } // Channel or RDBMS
                    
                } else {
                    /**
                     * Have an attribute - is a database column name.
                     */
                    iter = pcolEntities.iterator();
                    while (iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        
                        if (objEntity.getName().equalsIgnoreCase(strRHS)) {
                            if (blnIsDSChannel) {
                                strLHS = strAttr;
                                
                            } else {
                                objAttr = objEntity.getBo().getDescriptor().getAttribute(strAttr);
                                strRHS = getZx().getSql().columnName(objEntity.getBo(), 
                                                                     objAttr, 
                                                                     zXType.sqlObjectName.sonName);
                                
                            } // Channel or RDBMS
                            
                            break;
                        }
                        
                    } // Loop over entities
                    
                } // Attribute provided?
                
            } // Recursive ?
            
            /**
             * If the result of all this is nothing on either side, don't put an operator
             * in the middle - just exit with nothing.
             */
            if (StringUtil.len(strLHS) == 0 && StringUtil.len(strRHS) == 0) {
                return processQueryExpr;
            }
            
            if (blnIsDSChannel) {
                int intOperator = pobjQryExpr.getEnmoperator().pos;
                if (intOperator ==  zXType.pageflowQueryExprOperator.pqeoEQ.pos) {
                    processQueryExpr = strLHS + "=" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoGE.pos) {
                    processQueryExpr = strLHS + ">=" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoGT.pos) {
                    processQueryExpr = strLHS + ">" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoLE.pos) {
                    processQueryExpr = strLHS + "<=" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoLT.pos) {
                    processQueryExpr = strLHS + "<" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoNE.pos) {
                    processQueryExpr = strLHS + "<>" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoSW.pos) {
                    processQueryExpr = strLHS + "%" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoCNT.pos) {
                    processQueryExpr = strLHS + "%%" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoNOTNULL.pos) {
                    processQueryExpr = strLHS + "<>#null";
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoNULL.pos) {
                    processQueryExpr = strLHS + "=#null";
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoAND.pos) {
                    processQueryExpr = strLHS + "&" + strRHS;
                    
                } else if (intOperator == zXType.pageflowQueryExprOperator.pqeoOR.pos) {
                    processQueryExpr = strLHS + "|" + strRHS;
                    
                }
                
            } else {
                String strOperator = zXType.pageflowQueryExprOperator.getStringValue(pobjQryExpr.getEnmoperator());
                processQueryExpr = strLHS + ' ' + strOperator + ' ' + strRHS;
                
            } // RDBMS or channel?
            
            return processQueryExpr;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Handle QueryDef query type.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjQryExpr = "+ pobjQryExpr);
                getZx().log.error("Parameter : pcolEntities = "+ pcolEntities);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return processQueryExpr;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(processQueryExpr);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Handle all query type.
     * 
     * Reviewed for V1.5:1
     * 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if goAll fails. 
     */
    private zXType.rc goAll() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc goAll = zXType.rc.rcOK; 

        try {
            Iterator iter;
            PFEntity objEntity;                             // To loop over entities
            
            /**
             * Determine entity collection and set context  
             */
            ZXCollection colEntities = getPageflow().getEntityCollection(this, 
                                                                         zXType.pageflowActionType.patQuery,
                                                                         zXType.pageflowQueryType.pqtSearchForm);
            if (colEntities == null) {
                throw new Exception("Unable to retrieve entity collection");
            }
            
            /**
             * We do not support more than one entity when we are dealing with a channel data source
             * as this will never support a join
             */
            if (!getPageflow().validDataSourceEntities(colEntities)) {
                throw new Exception("Entity collection has data-source combination that is not supported");
            }
            
            /**
             * Set context variable
             */
            getPageflow().setContextEntities(colEntities);
            PFEntity objEntity1 = (PFEntity)colEntities.iterator().next();
            getPageflow().setContextEntity(objEntity1);
            
            String strQueryName = getPageflow().resolveDirector(getQueryname());
            
            /**
             * Take the data-source handler from the first entity
             */
            DSHandler objDSHandler = getPageflow().getContextEntity().getDSHandler();
            boolean blnIsDSChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            if (!isWhereclauseonly()) {
                String strSelectClause;
                
                /**
                 * Different select clause for channel and RDBMS data source
                 */
                if (blnIsDSChannel) {
                    /**
                     * For channels we never support multiple entities in single query so simply
                     * store attribute group as select clause
                     */
                    strSelectClause = getPageflow().getContextEntity().getSelectlistgroup();
                    
                } else {
                    /**
                     * Full SQL query for RDBMS data sources
                     */
                    
                    /**
                     * First do the select / from / join bit
                     */
                    int arrSize = colEntities.size();
                    ZXBO[] arrZXBO = new ZXBO[arrSize];
                    String[] arrSelectListStr = new String[arrSize];
                    boolean[] arrResolveBln = new boolean[arrSize];
                    int j = 0;
                    
                    iter = colEntities.iterator();
                    while(iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        
                        /**
                         * Include entity when either the result is requested (listGroup) or the
                         * where clause is requested (whereGroup)
                         */
                        if (StringUtil.len(objEntity.getSelectlistgroup()) > 0 || StringUtil.len(objEntity.getWheregroup()) > 0) {
                            arrZXBO[j] = objEntity.getBo();
                            arrSelectListStr[j] = objEntity.getSelectlistgroup();
                            arrResolveBln[j] = isResolvefk();
                            j++;
                        }
                    }
                    
                    strSelectClause = getZx().getSql().selectQuery(arrZXBO, 
                                                                   arrSelectListStr, 
                                                                   arrResolveBln, 
                                                                   this.distinct, this.outerjoin, 
                                                                   false, false, false);
                    
                    if (StringUtil.len(strSelectClause) == 0) {
                        throw new Exception("Unable to generate select query");
                    }
                    
                } // DS handler type
                
                /**
                 * And store it in the context
                 * 
                 * BD6FEB05 - Take into consideration the where claus only feature
                 * should not have evaluated the selected clause at all but since
                 * this feature was backfitted later, decided to do it like this
                 * (ie simply not store) instead of trying to be too clever
                 */
                getPageflow().addToContext(strQueryName + "QrySelectClause", strSelectClause);
                
            }
            
            /**
             * And the order by stuff
             * DGS21JUL2004: Ensure no duplication between PF Group by group and user-selected X1 to X5
             * Otherwise there is a danger that the user could choose to order by the same column we have
             * already included, and that causes a nasty error in some DBMSs (well, in SQL Server).
             */
            StringBuffer strOrderByClause = new StringBuffer(10);
            AttributeCollection colAttrOrderBy;
            String strTmp;
            
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                
                /**
                 * First see of we have to add the order by from any search form
                 */
                if (this.searchform) {
                    /**
                     * First the entity groupby group.
                     * 
                     * DGS13JAN2005: This returned collection can be modified, so must get a safe copy
                     * of the collection of attributes. Using getGroup is not safe because it returns
                     * a reference to the group, and can thus cause corruption in the source group.
                     */
                    colAttrOrderBy = objEntity.getBODesc().getGroupSafe(objEntity.getGroupbygroup());
                    
                    /**
                     * Note that there is no difference between channels and RDBMS
                     */
                    String[] arrTmp = {"X1","X2","X3","X4","X5"};
                    for (int i = 0; i < arrTmp.length; i++) {
                        strTmp = arrTmp[i];
                        strTmp = getPageflow().getPage().processOrderBy(getPageflow().getRequest(), objEntity.getBo(), strTmp, colAttrOrderBy);
                        
                        if (StringUtil.len(strTmp) > 0) {
                            if (strOrderByClause.length() > 0) {
                                strOrderByClause.append(", ");
                            }
                            strOrderByClause.append(strTmp);
                        }
                        
                    } // Loop over X1 .. X5
                    
                } // Based on search form (and thus can have search form triggered order by ?
                
                /**
                 * And next the entity groupby group
                 */
                if (StringUtil.len(objEntity.getGroupbygroup()) > 0) {
                    if (blnIsDSChannel) {
                        strTmp = objEntity.getGroupbygroup();
                        
                    } else {
                    	/**
                    	 * TODO : We should be smarter order by groups.
                    	 * 
                    	 * IE :-(Description,-id),srnme,-dte
                    	 */
                        if (objEntity.getGroupbygroup().charAt(0) == '-') {
                            strTmp = getZx().getSql().orderByClause(objEntity.getBo(), objEntity.getGroupbygroup().substring(1), true, true);
                        } else {
                            strTmp = getZx().getSql().orderByClause(objEntity.getBo(), objEntity.getGroupbygroup(), false, true);
                        }
                        
                    } // Channel or RDBMS
                    
                    if (StringUtil.len(strTmp) > 0) {
                        if (strOrderByClause.length() > 0) {
                            strOrderByClause.append(",").append(strTmp);
                        } else {
                            strOrderByClause.append(strTmp);
                        }
                    }
                    
                } // Has groupByGroup
                
            } // Loop over entities
            
            /**
             * DGS21APR2004: Can have Group By clause (only for non-channel data sources)
             */
            StringBuffer strGroupByClause = new StringBuffer();
            if (this.groupby && !blnIsDSChannel) {
                iter = colEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    /**
                     * Use the entity groupby group
                     */
                    if (StringUtil.len(objEntity.getGroupbygroup()) > 0) {
                        strTmp = getZx().getSql().groupByClause(objEntity.getBo(), objEntity.getGroupbygroup(), true);
                        
                        if (StringUtil.len(strTmp) > 0) {
                            if (strGroupByClause.length() > 0) {
                                strGroupByClause.append(", ");
                            }
                            strGroupByClause.append(strTmp);
                        }
                        
                    } // Has groupByGroup
                    
                } // Loop over enities
                
            } // Need groupby and is not a channel?
            
            /**
             * Optionally, the query could be based on a search form
             */
            StringBuffer strWhereClause = new StringBuffer(40);
            if (this.searchform) {
                iter = colEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    if (StringUtil.len(objEntity.getWheregroup()) > 0) {
                        strTmp = getPageflow().getPage().processSearchForm(getPageflow().getRequest(), 
                                                                           objEntity.getBo(), 
                                                                           objEntity.getWheregroup());
                        
                        if (StringUtil.len(strTmp) > 0) {
                            if (blnIsDSChannel) {
                                objDSWhereClause.addClauseWithAND(objEntity.getBo(), ":" + strTmp);
                                
                            } else {
                                /**
                                 * If user has entered criteria, must overwrite any
                                 *  selected saved query by new criteria, so set boolean accordingly:
                                 */
                                if (strWhereClause.length() > 0) {
                                    strWhereClause.append(" AND ").append(strTmp);
                                } else {
                                    strWhereClause.append(strTmp);
                                }
                            } // Channel or RDBMS
                            
                        } // Has processSearchForm generated anything.
                        
                    } // Has wheregroup?
                    
                } // Loop over entities
                
            } // Based on submitted searchform?
            
            /**
             * Add any conditions to whereclause based on pkWhereGroup
             */
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                getPageflow().processAttributeValues(objEntity);
                
                /**
                 * Difference between channels and RDBMS
                 */
                if (blnIsDSChannel) {
                    /**
                     * Channel handler, bit tricky as we have to merge where clauses
                     * Note how we once more rely on the fact that there is ever only going
                     * to be one entity in entity collection for channels
                     */
                    if (StringUtil.len(objEntity.getPkwheregroup()) > 0) {
                        objDSWhereClause.addClauseWithAND(objEntity.getBo(), objEntity.getPkwheregroup());
                    } // No PK where group
                    
                } else {
                    /**
                     *  RDBMS handler
                     */
                    strTmp = getPageflow().processPKWhereGroup(objEntity);
                    
                    if (StringUtil.len(strTmp)> 0) {
                        if (strWhereClause.length() > 0) {
                            strWhereClause.append(" AND ");
                        } 
                        strWhereClause.append(strTmp);
                    }
                    
                } // Channel or RDBMS
                
            } // Loop over enities
            
            /**
             * DGS29042003: Query Expression
             */
            strTmp = processQueryExpr(getQueryexpr(), colEntities);
            
            if (StringUtil.len(strTmp) > 0) {
                if (blnIsDSChannel) {
                    objDSWhereClause.addClauseWithAND(getPageflow().getContextEntity().getBo(), ":" + strTmp);
                    
                } else {
                    if (StringUtil.len(strWhereClause) > 0) {
                        strWhereClause.append(" AND ( ").append(strTmp).append(" )");
                    } else {
                        strWhereClause = new StringBuffer(strTmp);
                    }
                    
                } // Channel or RDBMS?
                
            } // Has queryExpr resulted in anything?
            
            /**
             * BD17DEC04 - Added support for QS query type
             * Note how we abuse the query property as the director
             * indicating the quick search.
             *  DGS21FEB2005 - V1.4:48: If the entity has a search group (whereGroup), use that
             *  for the QS group too, but if no search group is defined it will default to 'QS'
             */
            String strQSWhereGroup;
            if (queryType.equals(zXType.pageflowQueryType.pqtQs)) {
                String strQS = getZx().resolveDirector(this.query);
                if (StringUtil.len(strQS) > 0) {
                    iter = colEntities.iterator();
                    while (iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        
                        if (StringUtil.len(objEntity.getWheregroup()) == 0) {
                            strQSWhereGroup = "QS";
                        } else {
                            strQSWhereGroup = objEntity.getWheregroup();
                        }
                        strTmp = getZx().getSql().QSWhereClause(objEntity.getBo(), new StringProperty(strQS, false), strQSWhereGroup);
                        
                        if (StringUtil.len(strTmp) > 0) {
                            if (StringUtil.len(strWhereClause) > 0) {
                                strWhereClause.append(" AND ");
                            }
                            strWhereClause.append(strTmp);
                        }
                    } // Loop over entities
                    
                } // QS value found
                
            } // QS query type
            
            /**
             * DGS21APR2004: New processing for query where clause. If not 'standard' (default/normal
             * behaviour), get existing query using this query name or a different source query name
             * if supplied. Then either replace the one just generated by the one from the context, or
             * use the one from the context as the base and then either AND or OR the generated query
             */
            if (!this.queryWhereClause.equals(zXType.pageflowQueryWhereClause.pqwcStandard)) {
                if (StringUtil.len(this.sourcequeryname) > 0) {
                    strTmp = getPageflow().getFromContext(this.sourcequeryname + Pageflow.QRYWHERECLAUSE);
                } else {
                    strTmp = getPageflow().getFromContext(strQueryName + Pageflow.QRYWHERECLAUSE);
                }
                if (this.queryWhereClause.equals(zXType.pageflowQueryWhereClause.pqwcReplace) 
                    || StringUtil.len(strWhereClause) == 0) {
                    strWhereClause = new StringBuffer(strTmp);
                    
                } else {
                    /**
                     * DGS17AUG2004: Don't try to append ( ) with nothing in the parentheses as it causes DB error
                     * DGS11JAN2005: Above was not quite right - was testing length of strTmp but it is actually
                     * strWhereClause that ends up in the parentheses. Now if whereclause is empty, just replace
                     * it by strTmp (by extending the 'if' statement above).
                     */
                    if (StringUtil.len(strTmp) > 0) {
                        // Join the where Clause with a AND or OR.
                        strWhereClause.insert(0, (this.queryWhereClause.equals(zXType.pageflowQueryWhereClause.pqwcAnd) ? " AND ( ":" OR ( ")); 
                        // Put the strTmp right at the beginning
                        strWhereClause.insert(0, strTmp);
                        // End of the Whereclause.
                        strWhereClause.append(" ) ");
                    }
                    
                }
                
            }
            
            /**
             * And store it in the context
             */
            if (blnIsDSChannel) {
                getPageflow().addToContext(strQueryName + Pageflow.QRYWHERECLAUSE,  objDSWhereClause.getAsWhereClause());
            } else {
                getPageflow().addToContext(strQueryName + Pageflow.QRYWHERECLAUSE,  strWhereClause.toString());
            }
            
            /**
             * And store order / group by clauses if requested
             */
            if (!isWhereclauseonly()) {
                getPageflow().addToContext(strQueryName + Pageflow.QRYGROUPBYCLAUSE, strGroupByClause.toString());
                
                getPageflow().addToContext(strQueryName + Pageflow.QRYORDERBYCLAUSE, strOrderByClause.toString());
            }
            
            zXType.rc rcSavedQuery = zXType.rc.rcOK;        // return code of saved query handler
            StringBuffer strInterimWhereClause = new StringBuffer();
            
            /**
             *  If didn't use an existing saved query, or did but overrode it,
             *  and there is something in the query or order by, we need to persist this new one
             */
            if (rcSavedQuery.pos == zXType.rc.rcWarning.pos 
                && StringUtil.len(strInterimWhereClause.toString() + strOrderByClause) > 0) {
                getPageflow().getPage().persistSavedQuery(getPageflow().getPFDesc().getName(),
                										  getPageflow().getRequest(),
                                                          
                										  objEntity1.getBo().getDescriptor().getName(),
                                                          strInterimWhereClause,
                                                          strOrderByClause);
                
            }
            
            /**
             * Determine where to go next
             */
            getPageflow().setAction(getPageflow().resolveLink(getLink()));
            
            return goAll;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Handle all query type.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            goAll = zXType.rc.rcError;
            return goAll;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(goAll);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Construct associated with query
     * 
     * Reviewed for V1.5:1
     * 
     * For RDBMS we generate something that selects where R.FK not in (M.PK where M.FK = L.PK);
     * for channels we do not support multiple entities so this is a bit more tricky: first the
     * entity that is being selected MUST be R; next we retrieve all M where M.FK = L.PK and
     * use this to construct a where clause (e.g. R.PK <> a & R.PK <> b....)
     * 
     * This does mean that the query may no longer reflect the current data source situation if
     * the query is executed much later than when it was first created.
     * 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if goAssociatedWith fails. 
     */
    private zXType.rc goAssociatedWith() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        zXType.rc goAssociatedWith = zXType.rc.rcOK; 
        
        try {
            Iterator iter;
            PFEntity objEntity;
            
            /**
             * Determine entity collection and set context
             * Note that we have two entity collections: one with the entities
             * that we are interested in (colEntities) and one with
             * at least the Left / Middle and Right entity (colLMREntities)
             */
            ZXCollection colLMREntities = getPageflow().getEntityCollection(this,
                                                                            zXType.pageflowActionType.patQuery, 
                                                                            zXType.pageflowQueryType.pqtAssociatedWith);
            if (colLMREntities == null) {
                throw new Exception("Unable to retrieve Left / Middle and Right entity collection.");
            }
            
            ZXCollection colEntities = getPageflow().getEntityCollection(this, 
                                                                         zXType.pageflowActionType.patQuery,
                                                                         zXType.pageflowQueryType.pqtSearchForm);
            if (colEntities == null) {
                throw new Exception("Unable to retrieve entity collection");
            }
            
            /**
             * We do not support more than one entity when we are dealing with a channel data source
             * as this will never support a join
             */
            if (!getPageflow().validDataSourceEntities(colEntities)) {
                throw new Exception("Entity collection has data-source combination that is not supported");
            }
            
            /**
             * Set the Context Entities :
             */
            getPageflow().setContextEntities(colEntities);
            PFEntity objEntity1 = (PFEntity)colEntities.iterator().next();
            getPageflow().setContextEntity(objEntity1);
            
            String strQueryName = getPageflow().resolveDirector(getQueryname());
            
            /**
             * Take the data-source handler from the first entity
             */
            DSHandler objDSHandler = getPageflow().getContextEntity().getDSHandler();
            boolean blnIsDSChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            /**
             * Get handle to L / M and R entities
             */
            PFEntity objLeft = (PFEntity)colLMREntities.get(this.entityl);
            PFEntity objMiddle = (PFEntity)colLMREntities.get(this.entitym);
            PFEntity objRight = (PFEntity)colLMREntities.get(this.entityr);
            
            if (objLeft == null || objMiddle == null || objRight == null) {
                throw new Exception("Unable to retrieve left, middle or right entity");
            }
            
            if (blnIsDSChannel) {
                /**
                 * For channels the entity that is selected MUST be right
                 */
                if (objRight != getPageflow().getContextEntity()) {
                    throw new Exception("For channel data-source handler, the right entity must be the entity included in the query");
                }
            }
            
            if (!isWhereclauseonly()) {
                String strSelectClause;
                
                if (blnIsDSChannel) {
                    /**
                     * For channel we only support single entity so must be the first
                     */
                    strSelectClause = getPageflow().getContextEntity().getSelectlistgroup();
                    
                } else {
                    /**
                     * First do the select / from / join bit
                     */
                    int arrSize = colEntities.size();
                    ZXBO[] arrZXBO = new ZXBO[arrSize];
                    String[] arrSelectlistStr = new String[arrSize];
                    boolean[] arrResolveBln = new boolean[arrSize];
                    int j = 0;
                    
                    iter = colEntities.iterator();
                    while(iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        
                        /**
                         * Include entity when either the result is requested (listGroup) or the
                         * where clause is requested (whereGroup)
                         */
                        if (StringUtil.len(objEntity.getSelectlistgroup()) > 0 || StringUtil.len(objEntity.getWheregroup()) > 0) {
                            arrZXBO[j] = objEntity.getBo();
                            arrSelectlistStr[j] = objEntity.getSelectlistgroup();
                            arrResolveBln[j] = isResolvefk();
                            j++; // Do increment so we do not have any holes in the array.
                        }
                    }
                    
                    strSelectClause = getZx().getSql().selectQuery(arrZXBO, 
                                                                   arrSelectlistStr, 
                                                                   arrResolveBln,
                                                                   this.distinct, this.outerjoin,
                                                                   false, false, false);
                    if (StringUtil.len(strSelectClause) == 0) {
                        throw new Exception("Unable to generate select query");
                    }
                        
                } // Channel or RDBMS
                
                
                /**
                 * And store it in the context
                 */
                getPageflow().addToContext(strQueryName + "QrySelectClause", strSelectClause);

            } // Where clause only?
            
            /**
             * Load all instances in case we refer to them in any director
             */
            String strPK;
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                getPageflow().setContextEntity(objEntity);
                
                strPK = getPageflow().resolveDirector(objEntity.getPk());
                
                if (StringUtil.len(strPK) > 0) {
                    objEntity.getBo().setPKValue(strPK);
                    objEntity.getBo().loadBO( objEntity.getListgroup() );
                } // Know PK?
                
            } // Loop over entities
            getPageflow().setContextEntity(objEntity1); // Restore to the first entity

            /**
             * Optionally, the query could be based on a search form
             */
            String strTmp = "";
            StringBuffer strWhereClause = new StringBuffer(64);
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            if (this.searchform) {
	            iter = colEntities.iterator();
	            while (iter.hasNext()) {
	                objEntity = (PFEntity)iter.next();
	                
	                if (StringUtil.len(objEntity.getWheregroup()) > 0) {
	                    strTmp = getPageflow().getPage().processSearchForm(getPageflow().getRequest(), objEntity.getBo(), objEntity.getWheregroup());
                        
                        if (StringUtil.len(strTmp) > 0) {
                            if (blnIsDSChannel) {
                                objDSWhereClause.addClauseWithAND(objEntity.getBo(), ":" + strTmp);
                                
                            } else {
                                /**
                                 * If user has entered criteria, must overwrite any
                                 * selected saved query by new criteria, so set boolean accordingly:
                                 */
                                if (strWhereClause.length() > 0) {
                                    strWhereClause.append(" AND ").append(strTmp);
                                } else {
                                    strWhereClause.append(strTmp);
                                }
                                
                            } // Channel or RDBMS
                            
                        }
                        
	                } // Has wheregroup?
                    
	            } // Loop over entities
                
            } // Based on submitted searchform?
            
            /**
             * Add any conditions to whereclause based on pkWhereGroup
             */
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                getPageflow().processAttributeValues(objEntity);
                
                if (blnIsDSChannel) {
                    objDSWhereClause.addClauseWithAND(objEntity.getBo(), objEntity.getPkwheregroup());
                    
                } else {
                    strTmp = getPageflow().processPKWhereGroup(objEntity);
                    
                    if (StringUtil.len(strTmp)> 0) {
                        if (strWhereClause.length() > 0) {
                            strWhereClause.append(" AND ");
                        } 
                        strWhereClause.append(strTmp);
                    }
                    
                } // Channel or RDBMS
                
            } // Loop over entities
            
            /**
             * And the order by stuff.
             * DGS21JUL2004: Ensure no duplication between PF Group by group and user-selected X1 to X5
             * Otherwise there is a danger that the user could choose to order by the same column we have
             * already included, and that causes a nasty error in some DBMSs (well, in SQL Server).
             */
            AttributeCollection colAttrOrderBy;
            StringBuffer strOrderByClause = new StringBuffer(10);
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                /**
                 * First see of we have to add the order by from any search form
                 */
                if (this.searchform) {
                    /**
                     * DGS13JAN2005: This returned collection can be modified, so must get a safe copy
                     * of the collection of attributes. Using getGroup is not safe because it returns
                     * a reference to the group, and can thus cause corruption in the source group.
                     */
                    colAttrOrderBy = objEntity.getBODesc().getGroupSafe(objEntity.getGroupbygroup());
                    
                    /**
                     * Note that there is no difference between channels and RDBMS
                     */
	                String[] arrTmp = {"X1","X2","X3","X4","X5"};
	                for (int i = 0; i < arrTmp.length; i++) {
	                    strTmp = arrTmp[i];
	                    strTmp = getPageflow().getPage().processOrderBy(getPageflow().getRequest(), objEntity.getBo(), strTmp, colAttrOrderBy);
	                    
	                    if (StringUtil.len(strTmp) > 0) {
	                        if (strOrderByClause.length() > 0) {
	                            strOrderByClause.append(", ");
	                        }
                            strOrderByClause.append(strTmp);
	                    }
                        
	                } // Loop over X1 .. X5
                    
                } // Based on search form (and thus can have search form triggered order by ?
                
                /**
                 *And next the entity groupby group
                 */
                if (StringUtil.len(objEntity.getGroupbygroup()) > 0) {
                    if (blnIsDSChannel) {
                        strTmp = objEntity.getGroupbygroup();
                        
                    } else {
                        if (objEntity.getGroupbygroup().charAt(0) == '-') {
                            strTmp = getZx().getSql().orderByClause(objEntity.getBo(), objEntity.getGroupbygroup().substring(1), true, true);
                        } else {
                            strTmp = getZx().getSql().orderByClause(objEntity.getBo(), objEntity.getGroupbygroup(), false, true);
                        }
                        
                    } // Channel or RDBMS
                    
                    if (StringUtil.len(strTmp) > 0) {
                        if (strOrderByClause.length() > 0) {
                            strOrderByClause.append(",");
                        }
                        strOrderByClause.append(strTmp);
                    }
                    
                } // Has groupByGroup
                
            } // Loop over entities
            
            /**
             * And store it in the context
             */
            if (!isWhereclauseonly()) {
                getPageflow().addToContext(strQueryName + Pageflow.QRYORDERBYCLAUSE, strOrderByClause.toString());
            }
            
            /**
             * Now the difficult bit; the actual associated where clause. Remember that
             * we may already have a chunck of where clause so whatever we construct we have
             * 'AND' with what we already have
             */
            
            /**
             * Set PK of left.
             */
            objLeft.getBo().setPKValue(getPageflow().resolveDirector(objLeft.getPk()));
            
            if (!getZx().getDataSources().canDoDBJoin(objMiddle.getBo(), objRight.getBo())) {
                /**
                 * if we cant join Middle and Right all a bit more complex:
                 * First retrieve collection of all Ms that are related to L and construct a where clause
                 * like R.FK = M.PK | R.FK = M.PK | R.FK = M.PK
                 */
                
                /**
                 * Determine FK from middle to right
                 */
                String strFKAttr = objMiddle.getBo().getFKAttr(objRight.getBo()).getName();
                
                ZXCollection colMs = objLeft.getBo().quickFKCollection(objMiddle.getBODesc().getName(), // pstrEntity
                                                                       null, "+", // pstrFkAttr, pstrKeyAttr
                                                                       "+," + strFKAttr, // pstrGroup 
                                                                       false, null, null, false);
                if (colMs == null) {
                    throw new Exception("Unable to retrieve instances of 'middle' associated with 'left'");
                }
                ZXBO objBO;
                DSWhereClause objDSWhereClauseTmp = new DSWhereClause();
                iter = colMs.iterator();
                while (iter.hasNext()) {
                    objBO = (ZXBO)iter.next();
                    
                    objDSWhereClauseTmp.singleWhereCondition(objRight.getBo(),
                                                             objRight.getBODesc().getAttribute(objRight.getBODesc().getPrimaryKey()),
                                                             zXType.compareOperand.coEQ,
                                                             objBO.getValue(strFKAttr),
                                                             zXType.dsWhereConditionOperator.dswcoOr); // add as a OR statement.
                    
                } // Loop over colMs
                
                /**
                 * And merge with what we already had
                 */
                if (colMs.size() == 0) {
                    /**
                     * If there are no middles, we have to make this very clear
                     */
                    objDSWhereClause.addClauseWithAND(objRight.getBo(), ":1=0");
                    
                } else {
                    objDSWhereClause.addClauseWithAND(objRight.getBo(), ":" + objDSWhereClauseTmp.getAsWhereClause());
                    
                }
                
                /**
                 * We have done this if either middle or right is a channel. If the dsHandler is not a channel
                 * we must translate to a SQL query; there is however a problem with this:
                 */
                if (!blnIsDSChannel) {
                    if (StringUtil.len(strWhereClause) > 0) {
                        strWhereClause = new StringBuffer(strWhereClause + " AND (" + objDSWhereClause.getAsSQL() + ")");
                    } else {
                        strWhereClause = new StringBuffer("(" + objDSWhereClause.getAsSQL() + ")");
                    }
                    
                }
                
            } else {
                strTmp = getZx().getSql().associatedWithClause(objLeft.getBo(), objMiddle.getBo(), objRight.getBo());
                if (StringUtil.len(strTmp) == 0) {
                    throw new Exception("Unable to generate associated with clause");
                }
                
                if (strWhereClause.length() > 0) {
                    strWhereClause.append(" AND ");
                }
                strWhereClause.append(strTmp);
                
            } // Channel or RDBMS
             
            
            /**
             * And store it in the context
             */
            if (blnIsDSChannel) {
                getPageflow().addToContext(strQueryName + Pageflow.QRYWHERECLAUSE, objDSWhereClause.getAsWhereClause());
                
            } else {
                getPageflow().addToContext(strQueryName + Pageflow.QRYWHERECLAUSE, strWhereClause.toString());
                
            } // Channel or RDBMS?
            
            /**
             * Determine where to go next
             */
            getPageflow().setAction(getPageflow().resolveLink(getLink()));
            
            return goAssociatedWith;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Construct associated with query", e);
            
            if (getZx().throwException) throw new ZXException(e);
            goAssociatedWith = zXType.rc.rcError;
            return goAssociatedWith;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(goAssociatedWith);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Construct not associated with query
     * 
     * Bug fixed for V1.5:23
     * 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if goNotAssociatedWith fails. 
     */
    private zXType.rc goNotAssociatedWith() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc goNotAssociatedWith = zXType.rc.rcOK; 

        try {
            PFEntity objEntity;             // Loop over entities
            Iterator iter;
            
            /**
             * Determine entity collection and set context
             * Note that we have two entity collections: one with the entities
             * that we are interested in (colEntities) and one with
             * at least the Left / Middle and Right entity (colLMREntities)
             * 
             * Left / Middle / Right entities
             */
            ZXCollection colLMREntities = getPageflow().getEntityCollection(this,
                                                                            zXType.pageflowActionType.patQuery, 
                    														zXType.pageflowQueryType.pqtAssociatedWith); // pqtNotAssociatedWith
            // All relevant entities
            ZXCollection colEntities = getPageflow().getEntityCollection(this, 
                                                                         zXType.pageflowActionType.patQuery, 
                                                                         zXType.pageflowQueryType.pqtSearchForm);
            if (colEntities == null || colLMREntities == null) {
                throw new Exception("Unable to retrieve entity collection");
            }
            
            /**
             * We do not support more than one entity when we are dealing with a channel data source
             * as this will never support a join
             */
            if (!getPageflow().validDataSourceEntities(colEntities)) {
                throw new Exception("Entity collection has data-source combination that is not supported");
            }
            
            // Used for channel where clauses
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            /**
             * Set the Context Entities :
             */
            getPageflow().setContextEntities(colEntities);
            PFEntity objEntity1 = (PFEntity)colEntities.iterator().next();
            getPageflow().setContextEntity(objEntity1);
            
            /**
             * Take the data-source handler from the first entity
             */
            // Handle to common data source handler
            DSHandler objDSHandler = getPageflow().getContextEntity().getDSHandler();
            boolean blnIsDSChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            /**
             * Get handle to L / M and R entities
             * 
             * Handles to left / middle and right entities
             */
            PFEntity objLeft = (PFEntity)colLMREntities.get(this.entityl);
            PFEntity objMiddle = (PFEntity)colLMREntities.get(this.entitym);
            PFEntity objRight = (PFEntity)colLMREntities.get(this.entityr);
            if (objLeft == null || objMiddle == null || objRight == null) {
                throw new Exception("Unable to retrieve left, middle or right entity");
            }
            
            // Name to store query as
            String strQueryName = getPageflow().resolveDirector(getQueryname());
            
            /**
             * Only do what we need to in the case of a whereclause only PFQuery.
             */
            if (!isWhereclauseonly()) {
                // Select clause
                String strSelectClause;
                
                if (blnIsDSChannel) {
                    /**
                     * For channel we only support single entity so must be the first
                     */
                    strSelectClause = getPageflow().getContextEntity().getSelectlistgroup();
                    
                } else {
                    /**
                     * First do the select / from / join bit
                     */
                    int arrSize = colEntities.size();
                    ZXBO[] arrZXBO = new ZXBO[arrSize];
                    String[] arrSelectlistStr = new String[arrSize];
                    boolean[] arrResolveBln = new boolean[arrSize];
                    int j = 0;
                    
                    iter = colEntities.iterator();
                    while(iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        
                        /**
                         * Include entity when either the result is requested (listGroup) or the
                         * where clause is requested (whereGroup)
                         */
                        if (StringUtil.len(objEntity.getSelectlistgroup()) > 0 || StringUtil.len(objEntity.getWheregroup()) > 0) {
                            arrZXBO[j] = objEntity.getBo();
                            arrSelectlistStr[j] = objEntity.getSelectlistgroup();
                            arrResolveBln[j] = isResolvefk();
                            j++;
                        }
                    }
                    
                    strSelectClause = getZx().getSql().selectQuery(arrZXBO, 
                                                                   arrSelectlistStr, 
                                                                   arrResolveBln, 
                                                                   this.distinct, 
                                                                   this.outerjoin, 
                                                                   false, false, false);
                    
                    if (StringUtil.len(strSelectClause) == 0) {
                        throw new Exception("Unable to generate select query");
                    }
                    
                } // Channel or RDBMS
                
                /**
                 * And store it in the context
                 */
                getPageflow().addToContext(strQueryName + Pageflow.QRYSELECTCLAUSE, strSelectClause);
                
            } // Where clause only?
        
            /**
             * Load all instances in case we refer to them in any director
             */
            String strPK;
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                getPageflow().setContextEntity(objEntity);
                
                strPK = getPageflow().resolveDirector(objEntity.getPk());
                
                if (StringUtil.len(strPK) > 0) {
                    objEntity.getBo().setPKValue(strPK);
                    objEntity.getBo().loadBO(objEntity.getListgroup());
                    
                } // Know PK?
                
            } // Loop over entities
            getPageflow().setContextEntity(objEntity1); // Restore to the first entity
            
            /**
             * Optionally, the query could be based on a search form
             */
            String strTmp = "";
            
            // Where clause
            StringBuffer strWhereClause = new StringBuffer(40);
            if (this.searchform) {
	            iter = colEntities.iterator();
	            while (iter.hasNext()) {
	                objEntity = (PFEntity)iter.next();
	                
	                if (StringUtil.len(objEntity.getWheregroup()) > 0) {
	                    strTmp = getPageflow().getPage().processSearchForm(getPageflow().getRequest(), 
                                                                           objEntity.getBo(), 
                                                                           objEntity.getWheregroup());
                        
	                    if (StringUtil.len(strTmp) > 0) {
                            if (blnIsDSChannel) {
                                objDSWhereClause.addClauseWithAND(objEntity.getBo(), ":" + strTmp);
                                
                            } else {
                                /**
                                 * If user has entered criteria, must overwrite any
                                 *  selected saved query by new criteria, so set boolean accordingly:
                                 */
                                if (strWhereClause.length() > 0) {
                                    strWhereClause.append(" AND ").append(strTmp);
                                } else {
                                    strWhereClause.append(strTmp);
                                }
                            }
                            
	                    } // Channel or RDBMS
                        
	                } // Has wheregroup?
                    
	            } // Loop over entities
                
            } // Based on submitted searchform?
            
            /**
             * Add any conditions to whereclause based on pkWhereGroup
             */
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                getPageflow().processAttributeValues(objEntity);
                
                if (blnIsDSChannel) {
                    objDSWhereClause.addClauseWithAND(objEntity.getBo(), objEntity.getPkwheregroup());
                    
                } else {
                    strTmp = getPageflow().processPKWhereGroup(objEntity);
                    
                    if (StringUtil.len(strTmp)> 0) {
                        if (strWhereClause.length() > 0) {
                            strWhereClause.append(" AND ");
                        } 
                        strWhereClause.append(strTmp);
                    }
                } // Channel or RDBMS
                
            } // Loop over entities
            
            if (!isWhereclauseonly()) {
                /**
                 * And the order by stuff.
                 * DGS21JUL2004: Ensure no duplication between PF Group by group and user-selected X1 to X5
                 * Otherwise there is a danger that the user could choose to order by the same column we have
                 * already included, and that causes a nasty error in some DBMSs (well, in SQL Server).
                 * 
                 * Order by clause
                 */
                StringBuffer strOrderByClause = new StringBuffer(10);
                AttributeCollection colAttrOrderBy;
                
                iter = colEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    /**
                     * First see of we have to add the order by from any search form
                     */
                    if (this.searchform) {
                        /**
                         * DGS13JAN2005: This returned collection can be modified, so must get a safe copy
                         * of the collection of attributes. Using getGroup is not safe because it returns
                         * a reference to the group, and can thus cause corruption in the source group.
                         */
                        colAttrOrderBy = objEntity.getBODesc().getGroupSafe(objEntity.getGroupbygroup());
                        
                        /**
                         * Note that there is no difference between channels and RDBMS
                         */
    	                String[] arrTmp = {"X1","X2","X3","X4","X5"};
    	                for (int i = 0; i < arrTmp.length; i++) {
    	                    strTmp = arrTmp[i];
    	                    strTmp = getPageflow().getPage().processOrderBy(getPageflow().getRequest(), 
                                                                            objEntity.getBo(), 
                                                                            strTmp, 
                                                                            colAttrOrderBy);
    	                    
    	                    if (StringUtil.len(strTmp) > 0) {
    	                        if (strOrderByClause.length() > 0) {
    	                            strOrderByClause.append(", ");
    	                        }
                                strOrderByClause.append(strTmp);
    	                    }
    	                    
    	                } // Loop over X1 .. X5
                        
                    } // Based on search form (and thus can have search form triggered order by ?
                    
                    /**
                     *And next the entity groupby group
                     */
                    if (StringUtil.len(objEntity.getGroupbygroup()) > 0) {
                        
                        if(blnIsDSChannel) {
                            strTmp = objEntity.getGroupbygroup();
                            
                        } else {
                            if (objEntity.getGroupbygroup().charAt(0) == '-') {
                                strTmp = getZx().getSql().orderByClause(objEntity.getBo(), 
                                                                        objEntity.getGroupbygroup().substring(1), 
                                                                        true, true);
                                
                            } else {
                                strTmp = getZx().getSql().orderByClause(objEntity.getBo(), objEntity.getGroupbygroup(), false, true);
                            }
                            
                        } // Channel or RDBMS
                        
                        if (StringUtil.len(strTmp) > 0) {
                            if (strOrderByClause.length() > 0) {
                                strOrderByClause.append(",");
                            }
                            strOrderByClause.append(strTmp);
                        }
                        
                    } // Has groupByGroup
                    
                } // Loop over entities
            
                /**
                 * And store it in the context
                 */
                getPageflow().addToContext(strQueryName + "QryOrderByClause",  strOrderByClause.toString());
                
            } // Is where clause only ?
            
            /**
             * Now the difficult bit; the actual associated where clause. Remember that
             * we may already have a chunk of where clause
             */
            
            /**
             * Set PK of left
             */
            objLeft.getBo().setPKValue(getPageflow().resolveDirector(objLeft.getPk()));
            
            if (!getZx().getDataSources().canDoDBJoin(objMiddle.getBo(), objRight.getBo())) {
                /**
                 * For a channel it is all a bit more complex:
                 * First retrieve collection of all Ms that are related to L and construct a where clause
                 * like R.FK <> M.PK & R.FK <> M.PK & R.FK <> M.PK
                 */
                /**
                 * Determine FK from middle to right
                 */
                // Name of FK from Middle to Right
                String strFKAttr = objMiddle.getBo().getFKAttr(objRight.getBo()).getName();
                
                // Collection of Middles associated with Left
                ZXCollection colMs = objLeft.getBo().quickFKCollection(objMiddle.getBODesc().getName(), // pstrEntity
                                                                       null, "+", // pstrFkAttr, pstrKeyAttr
                                                                       "+," + strFKAttr, // pstrGroup 
                                                                       false, null, null, false);
                if (colMs == null) {
                    throw new Exception("Unable to retrieve instances of 'middle' associated with 'left'");
                }
                
                DSWhereClause objDSWhereClauseTmp = new DSWhereClause();
                // Loop over variable
                ZXBO objBO;
                iter = colMs.iterator();
                while (iter.hasNext()) {
                    objBO = (ZXBO)iter.next();
                    
                    objDSWhereClauseTmp.singleWhereCondition(objRight.getBo(),
                                                            objRight.getBODesc().getAttribute(objRight.getBODesc().getPrimaryKey()),
                                                            zXType.compareOperand.coNE,
                                                            objBO.getValue(strFKAttr),
                                                            zXType.dsWhereConditionOperator.dswcoAnd); // Add to AND clause
                } // Loop over colMs
                
                /**
                 * And merge with what we already had
                 */
                if (colMs.size() > 0) {
                    objDSWhereClause.addClauseWithAND(objRight.getBo(), ":" + objDSWhereClauseTmp.getAsWhereClause());
                }
                
                /**
                 * We have done this if either middle or right is a channel. If the dsHandler is not a channel
                 * we must translate to a SQL query; there is however a problem with this:
                 */
                if (!blnIsDSChannel) {
                    if(StringUtil.len(strWhereClause) > 0 && colMs.size() > 0) {
                        strWhereClause.append(" AND (").append(objDSWhereClause.getAsSQL()).append(")");
                    } else {
                        if (colMs.size() > 0) {
                            strWhereClause = new StringBuffer("(" + objDSWhereClause.getAsSQL() + ")");
                        }
                    }
                }
                
            } else {
                iter = colLMREntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    getPageflow().processAttributeValues(objEntity);
                }
                
                strTmp = getZx().getSql().notAssociatedWithClause(objLeft.getBo(), objMiddle.getBo(), objRight.getBo(), objMiddle.getPkwheregroup());
                if (StringUtil.len(strTmp) == 0) {
                    throw new Exception("Unable to generate associated with clause");
                }
                
                if (strWhereClause.length() > 0) {
                    strWhereClause.append(" AND ");
                }
                strWhereClause.append(strTmp);
                
            } // Channel or RDBMS
            
            /**
             * DGS29042003: Query Expression
             */
            strTmp = processQueryExpr(getQueryexpr(), colEntities);
            
            if (StringUtil.len(strTmp) > 0) {
                if (blnIsDSChannel) {
                    objDSWhereClause.addClauseWithAND(getPageflow().getContextEntity().getBo(), ":" + strTmp);
                    
                } else {
                    if (StringUtil.len(strWhereClause) > 0) {
                        strWhereClause.append(" AND ( ").append(strTmp).append(" )");
                    } else {
                        strWhereClause = new StringBuffer(strTmp);
                    }
                    
                } // Channel or RDBMS?
                
            } // Has queryExpr resulted in anything?
            
            /**
             * And store it in the context
             */
            if (blnIsDSChannel) {
                getPageflow().addToContext(strQueryName + Pageflow.QRYWHERECLAUSE,  objDSWhereClause.getAsWhereClause());
            } else {
                getPageflow().addToContext(strQueryName + Pageflow.QRYWHERECLAUSE,  strWhereClause.toString());
            }
            
            /**
             * Determine where to go next
             */
            getPageflow().setAction(getPageflow().resolveLink(getLink()));
            
            return goNotAssociatedWith;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Construct not associated with query", e);
            
            if (getZx().throwException) throw new ZXException(e);
            goNotAssociatedWith = zXType.rc.rcError;
            return goNotAssociatedWith;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(goNotAssociatedWith);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Handle QueryDef query type.
     * Reviewed for V1.5:1
     * 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if goQueryDef fails. 
     */
    private zXType.rc goQueryDef() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc goQueryDef = zXType.rc.rcOK; 

        try {
            QueryDef objQueryDef = new QueryDef();
            if (!objQueryDef.init( getPageflow().resolveDirector(this.querydefname) ).equals(zXType.rc.rcOK)) {
                throw new Exception("Unable to initialize query def object '" + this.querydefname + "'");
            }
            
            /**
             * Determine entity collection and set context  
             */
            ZXCollection colEntities = getPageflow().getEntityCollection(this,
                                                                         zXType.pageflowActionType.patQuery,
                                                                         zXType.pageflowQueryType.pqtSearchForm);
            if (colEntities == null) {
                throw new Exception("Unable to retrieve entity collection");
            }
            
            /**
             * Set context variable
             */
            getPageflow().setContextEntities(colEntities);
            PFEntity objEntity1 = (PFEntity)colEntities.iterator().next();
            getPageflow().setContextEntity(objEntity1);
            
            DSHandler objDSHandler = getPageflow().getContextEntity().getDSHandler();
            
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                throw new ZXException("Query-def query type not supported for channel data source handler", 
                                      objDSHandler.getName());
            }
            
            /**
             *Load all instances in case we refer to them in any director 
             *and handle the attribute values
             */
            String strPK;
            Iterator iter = colEntities.iterator();
            PFEntity objEntity;
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                getPageflow().setContextEntity(objEntity);
                
                /**
                 * Process any attribute values before building where clause etc.
                 */
                getPageflow().processAttributeValues(objEntity);
                
                /**
                 * Load into the querydef's context : 
                 * Obsolete: querydef now uses zx.BOContext
                 * objQueryDef.addToContext objEntity.name, objEntity.BO
                 */
                
                strPK = getPageflow().resolveDirector(objEntity.getPk());
                if (StringUtil.len(strPK) > 0) {
                    objEntity.getBo().setPKValue(strPK);
                    objEntity.getBo().loadBO(objEntity.getListgroup());
                }
            }
            getPageflow().setContextEntity(objEntity1); // Restore to the first entity
            
            /**
             * Get the SELECT clause
             */
            String strSelectClause = objQueryDef.qry(zXType.queryDefPageFlowScope.qdpfsSelect, this.querydefscope);
            
            if (StringUtil.len(strSelectClause) > 0) {
                throw new Exception("Unable to generate select query");
            }
            
            String strQueryName = getPageflow().resolveDirector(getQueryname());
            
            /**
             * And store it in the context
             */
            if (!isWhereclauseonly()) {
                getPageflow().addToContext(strQueryName + "QrySelectClause",  strSelectClause);
            }
            
            /**
             * Optionally, the query could be based on a search form
             */
            String strTmp = "";
            
            StringBuffer strWhereClause = new StringBuffer(objQueryDef.qry(zXType.queryDefPageFlowScope.qdpfsWhere, this.querydefscope));
            
            if (this.searchform) {
	            iter = colEntities.iterator();
	            while (iter.hasNext()) {
	                objEntity = (PFEntity)iter.next();
	                
	                if (StringUtil.len(objEntity.getWheregroup()) > 0) {
	                    strTmp = getPageflow().getPage().processSearchForm(getPageflow().getRequest(), objEntity.getBo(), objEntity.getWheregroup());
	                    if (StringUtil.len(strTmp) > 0) {
	                        /**
	                         * If user has entered criteria, must overwrite any
	                         * selected saved query by new criteria, so set boolean accordingly:
	                         */
	                        if (strWhereClause.length() > 0) {
	                            strWhereClause.append(" AND ").append(strTmp);
	                        } else {
	                            strWhereClause.append(strTmp);
	                        }
	                    }
	                }
	            }
            }
            
            /**
             * Add any conditions to whereclause based on pkWhereGroup. Unlike goAll above, there 
             * is no need to process attr values here because we did it earlier in this function
             */
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                strTmp = getZx().getSql().processWhereGroup(objEntity.getBo(), objEntity.getPkwheregroup());
                
                if (StringUtil.len(strTmp) > 0) {
                    if (StringUtil.len(strWhereClause) > 0) {
                        strWhereClause.append(" AND ").append(strTmp);
                    } else {
                        strWhereClause.append(strTmp);
                    }
                }
                
            }
            
            /**
             * DGS29042003: Query Expression
             */
            strTmp = processQueryExpr(getQueryexpr(), colEntities);
            if (StringUtil.len(strTmp) > 0) {
                if (StringUtil.len(strWhereClause) > 0) {
                    strWhereClause.append(" AND ( ").append(strTmp).append(" )");
                } else {
                    strWhereClause = new StringBuffer(strTmp);
                }
            }
            
            /**
             * And store it in the context
             */
            getPageflow().addToContext(strQueryName + Pageflow.QRYWHERECLAUSE, strWhereClause.toString());
            
            /**
             * And the order by stuff
             * DGS21JUL2004: Ensure no duplication between user-selected X1 to X5 (can't allow for qrydef)
             * Otherwise there is a danger that the user could choose to order by the same column twice,
             * and that causes a nasty error in some DBMSs (well, in SQL Server).
             */
            StringBuffer strOrderByClause = new StringBuffer(objQueryDef.qry(zXType.queryDefPageFlowScope.qdpfsOrderBy, this.querydefscope));
            
            iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                /**
                 * First see of we have to add the order by from any search form
                 */
                if (this.searchform) {
                    AttributeCollection colAttrOrderBy = new AttributeCollection();
                    
	                String[] arrTmp = {"X1","X2","X3","X4","X5"};
	                for (int i = 0; i < arrTmp.length; i++) {
	                    strTmp = arrTmp[i];
	                    strTmp = getPageflow().getPage().processOrderBy(getPageflow().getRequest(), objEntity.getBo(), strTmp, colAttrOrderBy);
	                    
	                    if (StringUtil.len(strTmp) > 0) {
	                        if (strOrderByClause.length() > 0) {
	                            strOrderByClause.append(", ");
	                        }
                            strOrderByClause.append(strTmp);
	                    }
	                    
	                }
                }
                
                /**
                 *And next the entity groupby group
                 */
                if (StringUtil.len(objEntity.getGroupbygroup()) > 0) {
                    if (objEntity.getGroupbygroup().charAt(0) == '-') {
                        strTmp = getZx().getSql().orderByClause(objEntity.getBo(), objEntity.getGroupbygroup().substring(1), true, true);
                    } else {
                        strTmp = getZx().getSql().orderByClause(objEntity.getBo(), objEntity.getGroupbygroup(), false, true);
                    }
                    
                    if (StringUtil.len(strTmp) > 0) {
                        if (strOrderByClause.length() > 0) {
                            strOrderByClause.append(",");
                        }
                        strOrderByClause.append(strTmp);
                    }
                    
                }
            }
            
            /**
             * And store it in the context, or not if "isWhereClauseOnly"
             */
            if (!isWhereclauseonly()) {
                getPageflow().addToContext(strQueryName + "QryOrderByClause",  strOrderByClause.toString());
            }
            
            /**
             * Determine where to go next
             */
            getPageflow().setAction(getPageflow().resolveLink(getLink()));
            
            return goQueryDef;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Handle QueryDef query type.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            goQueryDef = zXType.rc.rcError;
            return goQueryDef;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(goQueryDef);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Implemented Methods from PFAction
    
    /**
     * @see PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
        zXType.rc go = zXType.rc.rcOK;
        if (getQueryType().equals(zXType.pageflowQueryType.pqtSearchForm)) {
            go = goSearchForm();
            
        } else if (getQueryType().equals(zXType.pageflowQueryType.pqtAssociatedWith)) {
            go = goAssociatedWith();
            
        } else if (getQueryType().equals(zXType.pageflowQueryType.pqtNotAssociatedWith)) {
            go = goNotAssociatedWith();
            
        } else if (getQueryType().equals(zXType.pageflowQueryType.pqtAll)) {
            go = goAll();
            
        } else if (getQueryType().equals(zXType.pageflowQueryType.pqtQs)) {
            /**
             * BD17DEC04 - Uses all query type handler, difference in handled there
             */
            go = goAll();
            
        } else if (getQueryType().equals(zXType.pageflowQueryType.pqtQueryDef)) {
            /**
             * DGS 13JAN2003: New query type 'QueryDef'
             */
            go = goQueryDef();
        }
        return go;
    }
    
    /** 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();
        
        objXMLGen.taggedValue("distinct", isDistinct());
        objXMLGen.taggedValue("searchform", isSearchform());
        objXMLGen.taggedValue("outerjoin", isOuterjoin());
        objXMLGen.taggedValue("whereclauseonly", isWhereclauseonly());
        
        objXMLGen.taggedValue("entityl", getEntityl());
        objXMLGen.taggedValue("entityr", getEntityr());
        objXMLGen.taggedValue("entitym", getEntitym());

        objXMLGen.taggedValue("query", getQuery());
        objXMLGen.taggedValue("queryType", zXType.valueOf(getQueryType()));
        
        /**
         * GS 13JAN2003: Added querydef tags
         */
        objXMLGen.taggedValue("querydefname", getQuerydefname());
        objXMLGen.taggedValue("querydefscope", getQuerydefscope());
        
        /**
         * DGS 29APR2003: Added Query Expression
         */
        if (this.queryexpr != null) {
            getDescriptor().xmlQueryExpr("queryexpr", getQueryexpr());    
        }
        
        /**
         * DGS 21APR2004: Added Query Where Clause, Source Query Name and Group By
         * For backwards compatibility, only write these tags out if different to
         * previous (and now default) behaviour.
         */
        if (getQueryWhereClause().equals(zXType.pageflowQueryWhereClause.pqwcStandard)) {
            objXMLGen.taggedValue("queryWhereClause", zXType.valueOf(getQueryWhereClause()));
        }
        if (StringUtil.len(getSourcequeryname()) > 0) {
            objXMLGen.taggedValue("sourcequeryname", getSourcequeryname());
        }
        if (isGroupby()) {
            objXMLGen.taggedValue("groupby", isGroupby());
        }
        
    }   
    
    /**
     * Creates a clean clone of this pageflow action.
     * 
     * @see PFAction#clone(Pageflow)
     **/
    public PFAction clone(Pageflow pobjPageflow){
        PFQuery cleanClone = (PFQuery)super.clone(pobjPageflow);
        
        cleanClone.setDistinct(isDistinct());
        
        cleanClone.setQueryType(getQueryType());
        cleanClone.setQueryWhereClause(getQueryWhereClause());
        
        cleanClone.setEntityl(getEntityl());
        cleanClone.setEntitym(getEntitym());
        cleanClone.setEntityr(getEntityr());
        cleanClone.setGroupby(isGroupby());
        cleanClone.setOuterjoin(isOuterjoin());
        cleanClone.setQuery(getQuery());
        cleanClone.setQuerydefname(getQuerydefname());
        cleanClone.setQuerydefscope(getQuerydefscope());
        
        if (getQueryexpr() != null) {
            cleanClone.setQueryexpr((PFQryExpr)getQueryexpr().clone());
        }
        
        cleanClone.setSearchform(isSearchform());
        cleanClone.setSourcequeryname(getSourcequeryname());
        cleanClone.setWhereclauseonly(isWhereclauseonly());
        
        return cleanClone;
    }
}