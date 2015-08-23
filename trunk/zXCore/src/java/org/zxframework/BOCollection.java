/*
 * Created on Feb 27, 2004
 * $Id: BOCollection.java,v 1.1.2.7 2005/11/21 15:14:17 mike Exp $
 */
package org.zxframework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.zxframework.util.StringUtil;

/**
 * BOCollection, is a collection of ZXBO.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class BOCollection extends ZXCollection {
    
    //------------------------ Members
    
    //------------------------ Constructors

    /**
     * Default constructor.
     */
    public BOCollection() {
        super();
    }
    
    /**
     * @param size The initial size of the collection.
     */
    public BOCollection(int size) {
        super(size);
    }

    //------------------------ Helper methods

    /**
     * A helper method that return ZXBO from the collection, instead of just
     * Object. It is safer to do it like this when you initialised the
     * ZXCollection as a BOCollection.
     * 
     * NOTE : Called the super class get and Casts it to a ZXBO
     * 
     * @param pstrBO The key of the the ColAttribute, this will be the name of the Attribute.
     * @return Returns a attribute but the key.
     */
    public ZXBO get(String pstrBO) {
        return (ZXBO) super.get(pstrBO);
    }

    /**
     * Persist a collection of BOs.
     * 
     * <pre>
     * Calles : persistCollection(pstrGroup, "+");
     * </pre>
     * 
     * @param pstrGroup The attribute group to persist. Optional, default should be "*".
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if persistCollection fails.
     */
    public zXType.rc persistCollection(String pstrGroup) throws ZXException {
        return persistCollection(pstrGroup, "+");
    }

    /**
     * Persist a collection of BOs.
     * 
     * @param pstrGroup The attribute group to persist. Optional, default should be "*".
     * @param pstrWhereGroup The where group to use. Optional, default should be "+".
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if persistCollection fails.
     */
    public zXType.rc persistCollection(String pstrGroup, String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }

        zXType.rc persistCollection = zXType.rc.rcOK;

        if (pstrGroup == null) {
            pstrGroup = "*";
        }
        if (pstrWhereGroup == null) {
            pstrWhereGroup = "+";
        }

        try {
            
            ZXBO objBO;
            Iterator iter = iterator();
            while (iter.hasNext()) {
                objBO = (ZXBO) iter.next();
                objBO.persistBO(pstrGroup, pstrWhereGroup);
            }
            
            return persistCollection;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Persist a collection of BOs.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            persistCollection = zXType.rc.rcError;
            return persistCollection;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(persistCollection);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Create a string with values from the BOs in a collection.
     * 
     * @param pstrAttributeGroup Attributes to use to generate each
     * @param pstrAttributeSep Attribute separator. Optional, default is null
     * @param pstrBOSep The seperate for the string. Optional default is null
     * @return Returns a string of  from the BOs in a collection.
     * @throws ZXException Thrown if col2String fails
     */
    public String col2String(String pstrAttributeGroup, 
                                        String pstrAttributeSep, String pstrBOSep) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrAttributeGroup", pstrAttributeGroup);
            getZx().trace.traceParam("pstrAttributeSep", pstrAttributeSep);
            getZx().trace.traceParam("pstrBOSep", pstrBOSep);
        }

        StringBuffer col2String = new StringBuffer(32);
        
        try {
            ZXBO objBO;
            Iterator iter = iterator();
            while (iter.hasNext()) {
                objBO = (ZXBO)iter.next();
                if(col2String.length() > 0) {
                    col2String.append(pstrBOSep);
                }
                col2String.append(objBO.formattedString(pstrAttributeGroup, false, pstrAttributeSep));
            }
            
            return col2String.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Create a string with values from the BOs in a collection ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrAttributeGroup = " + pstrAttributeGroup);
                getZx().log.error("Parameter : pstrAttributeSep = " + pstrAttributeSep);
                getZx().log.error("Parameter : pstrBOSep = " + pstrBOSep);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return col2String.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(col2String);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add BO to a collection of BOs but maintain order.
     *
     * @param pobjBO The business object to add to the collection.
     * @param pstrOrderGroup The order by group to use,
     * @param pstrKeyGroup The key used to store the business object. 
     * @param pblnReverse The order of the collection.
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if colAddOrdered fails. 
     */
    public zXType.rc colAddOrdered(ZXBO pobjBO, 
                                   String pstrOrderGroup, 
                                   String pstrKeyGroup, 
                                   boolean pblnReverse) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrOrderGroup", pstrOrderGroup);
            getZx().trace.traceParam("pstrKeyGroup", pstrKeyGroup);
            getZx().trace.traceParam("pblnReverse", pblnReverse);
        }

        zXType.rc colAddOrdered = zXType.rc.rcOK; 
        
        try {
            
            /**
             * Simple case of empty collection
             */
            if (size() == 0) {
                if (StringUtil.len(pstrKeyGroup) > 0) {
                    put(pobjBO.formattedString(pstrKeyGroup), pobjBO);
                } else {
                    // Not advisible, rather use a Arraylist.
                    add(pobjBO);
                }
                return colAddOrdered;
            }
            
            ZXBO objBO;
            int j = -1;
            boolean blnMath = false;
            
            Iterator iter = iterator();
            zxbo : while (iter.hasNext()) {
                objBO = (ZXBO)iter.next();
                j++;
                if (pblnReverse) {
                    if (objBO.compare(pobjBO, pstrOrderGroup) < 0) { // Less Than
                        blnMath = true;
                        break zxbo;
                    }
                    
                } else {
                    if (objBO.compare(pobjBO, pstrOrderGroup) > 0) { // Greater Than
                        blnMath = true;
                        break zxbo;
                    }
                    
                }
            }
            
            if (j != -1 && blnMath) {
                /**
                 * Found a postion to put the business object.
                 */
                if (StringUtil.len(pstrKeyGroup) > 0) {
                    /**
                     * Manually copy the variable into a 
                     * new collection.
                     */
                    Map colTmp = new LinkedHashMap();
                    int i = -1;
                    iter = iterator();
                    Iterator iterKey = iteratorKey();
                    while (iter.hasNext()) {
                        i++;
                        if (i == j) {
                            colTmp.put(pobjBO.formattedString(pstrKeyGroup), pobjBO);
                        }
                        colTmp.put(iterKey.next(), iter.next());
                    }
                    this.collection = colTmp;
                    
                } else {
                    /**
                     * ArrayList type of collection:
                     */
                    ArrayList colBO = new ArrayList(getCollection());
                    colBO.add(j, pobjBO);
                }
                
            } else {
                /**
                 * We have reached the end of the collection without finding the right slot
                 */
                if (StringUtil.len(pstrKeyGroup) > 0) {
                    put(pobjBO.formattedString(pstrKeyGroup), pobjBO);
                } else {
                    // Not advisible, rather use a Arraylist.
                    add(pobjBO);
                }
            }

            return colAddOrdered;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Add BO to a collection of BOs but maintain order.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrOrderGroup = " + pstrOrderGroup);
                getZx().log.error("Parameter : pstrKeyGroup = " + pstrKeyGroup);
                getZx().log.error("Parameter : pblnReverse = " + pblnReverse);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            colAddOrdered = zXType.rc.rcError;
            return colAddOrdered;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(colAddOrdered);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Sort a collection of BOs.
     *
     * @param pstrGroup The order by group.  
     * @param pstrKeyGroup The key to use when inserting the business object.
     * @param pblnReverse Which order the collection is in.
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if colSort fails. 
     */
    public zXType.rc colSort(String pstrGroup, String pstrKeyGroup, boolean pblnReverse) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrKeyGroup", pstrKeyGroup);
            getZx().trace.traceParam("pblnReverse", pblnReverse);
        }

        zXType.rc colSort = zXType.rc.rcOK; 
        
        try {
            
            ZXBO objBO;
            Iterator iter = iterator();
            while (iter.hasNext()) {
                objBO = (ZXBO)iter.next();
                colAddOrdered(objBO, pstrGroup,pstrKeyGroup, pblnReverse);
            }
            
            return colSort;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Sort a collection of BOs.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pstrKeyGroup = " + pstrKeyGroup);
                getZx().log.error("Parameter : pblnReverse = " + pblnReverse);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            colSort = zXType.rc.rcError;
            return colSort;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(colSort);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Object overloaded method

    /**
     * @see java.lang.Object#toString()
     * @return Returns a formatted string of all of the attributes in the
     *         attribute collection
     */
    public String toString() {
        StringBuffer toString = new StringBuffer(15);
        toString.append(getKey()).append(" [").append(size()).append("] ");
        toString.append(super.toString());
        return toString.toString();
    }

    /**
     * Clone the BO collection object.
     * 
     * <pre>
     * 
     * NOTE : This is only a shallow clone.
     * </pre>
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        BOCollection colBOCollection = new BOCollection(size());
        
        ZXBO objBO;
        Iterator iter = iterator();
        Iterator iterKey = iteratorKey();
        while (iter.hasNext()) {
            objBO = (ZXBO) iter.next();
            colBOCollection.put(iterKey.next(), objBO);
        }
        
        return colBOCollection;
    }
}