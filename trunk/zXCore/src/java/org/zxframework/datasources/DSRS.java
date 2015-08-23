/*
 * Created on Apr 15, 2005
 * $Id: DSRS.java,v 1.1.2.17 2006/07/17 16:32:05 mike Exp $
 */
package org.zxframework.datasources;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.Attribute;
import org.zxframework.ZX;
import org.zxframework.ZXBO;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.jdbc.ZXResultSet;
import org.zxframework.property.Property;

/**
 * Generic datasource result-set
 * 
 * <pre>
 * 
 * What  : BD21APR05 - V1.5:13
 * Why   : Implement paging for recordsets
 * 
 * Change    : BD18MAY05 - V1.5:18
 * Why       : Added support for property persistStatus
 * 
 * Change    : BD13JUL05 - V1.5:35
 * Why       : Check recordset status before closing recordset as attempting to
 *             close an already closed recordset generates and error
 * 
 * Change    : BD15SEP05 - V1.5:57
 * Why       : In rscOlumn, on eof, simply return null-property (rather than goto errExit); this
 *               makes a construct like
 *
 *           "do while not objrs.eof and objRS.rsColumn(objBO, objAttr).strValue <> strLastClient"
 *
 *               possible
 * 
 * Change    : BD1JUN06 - V1.5:96
 * Why       : Added the 'newData' property (private). On an init / move / moveNext we set the
 *               newData flag to true; this indicates that with the next rs2obj we are dealing
 *               with a new row in the recordset in which case we can reset all the property
 *               persist statuses; every subsequent rs2obj call (for the same row in the recordset,
 *               when newData is false) we leave the persist status as-is
 *               
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class DSRS extends ZXObject {
    
    //-------------------------- Members
    
    private int dataCursor;
    private List data;
    
    private boolean pureNames;
    private ZXResultSet rs;
    private String source;
    private DSHandler DSHandler;
    
    private int intRSType;
    private boolean isOpen;
    private boolean moveNext;
    
    private int startRow;
    private int batchSize;
    
    private boolean newData;
    
    //--------------------------- Constructors
    
    /**
     * Hide default constructor.
     */
    private DSRS() {
    	super();
    }
    
    /**
     * @param pobjDSHandler The handle to the Datasource that created this resultset.
     */
    public DSRS(DSHandler pobjDSHandler) {
        super();
        
        this.moveNext = true;
        this.isOpen = true;
        
        this.DSHandler = pobjDSHandler;
        this.intRSType = this.DSHandler.getRsType().pos;
        
        /**
         * We are dealing with a new recordset and thus certainly with new data 
         */
        this.newData = true;
    }
    
    //--------------------------- Getters and Setters
    
    /**
     * This boolean is set on init / move and moveNext and
     * indicates that the next time we do rs2obj, we are
     * dealing with new data (hence the name) and not
     * a subsequent rs2obj on the same row.
     * 
	 * @return the newData
	 */
	public boolean isNewData() {
		return newData;
	}

	/**
	 * @param newData the newData to set
	 */
	public void setNewData(boolean newData) {
		this.newData = newData;
	}
	
	/**
     * In case of a channel as a data-source, the result
     * will be stored in this collection.
     * 
     * @return Returns the data.
     */
    public List getData() {
        return data;
    }
    
    /**
     * @param data The data to set.
     */
    public void setData(List data) {
        this.data = data;
    }
    
    /**
     * Points to NEXT element in data collection
     * Only relevant when the rsType = collection.
     * 
     * @return Returns the dataCursor.
     */
    public int getDataCursor() {
        return dataCursor;
    }
    
    /**
     * @param dataCursor The dataCursor to set.
     */
    public void setDataCursor(int dataCursor) {
        this.dataCursor = dataCursor;
    }
    
    /**
     * @return Returns the batchSize.
     */
    public int getBatchSize() {
        return batchSize;
    }
    
    /**
     * @param batchSize The batchSize to set.
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    /**
     * @return Returns the startRow.
     */
    public int getStartRow() {
        return startRow;
    }
    
    /**
     * @param startRow The startRow to set.
     */
    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }
    
    /**
     * Flag indicating that the query was generated with purenames set.
     * 
     * @return Returns the pureNames.
     */
    public boolean isPureNames() {
        return pureNames;
    }
    
    /**
     * @param pureNames The pureNames to set.
     */
    public void setPureNames(boolean pureNames) {
        this.pureNames = pureNames;
    }
    
    /**
     * @return Returns the rs.
     */
    public ZXResultSet getRs() {
        return rs;
    }
    
    /**
     * @param rs The rs to set.
     */
    public void setRs(ZXResultSet rs) {
        this.rs = rs;
    }
    
    /**
     * @return Returns the source.
     */
    public String getSource() {
        return source;
    }
    
    /**
     * @param source The source to set.
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    //----------------------------- Public Methods.
    
    /**
     * @return Returns true if at end of the Results.
     */
    public boolean eof() {
        boolean eof = false;
        try {
            /**
             * If we call moveNext and it failed to go to the next position
             * then we must be at the end of the collection/results.
             */
            if (!moveNext) {
                return true;
            }
            
            if (intRSType == zXType.dsRSType.dsrstCollection.pos) {
                eof = (this.dataCursor > this.data.size()) || this.data.size() == 0;
                
            } else if (intRSType == zXType.dsRSType.dsrstRS.pos) {
                try {
                    eof = this.rs.getTarget().isAfterLast();
                } catch (Exception e) {
                    return !moveNext;
                }
                
            } else if (intRSType == zXType.dsRSType.dsrstStream.pos) {
                // eof = this.DSHandler.
            }
            return eof;
            
        } catch (Exception e) {
            return eof;
        }
    }
    
    /**
     * Move the cursor to the next record.
     * 
     * <pre>
     * 
     * NOTE : This will resolve the moveNext flag for the result.
     * </pre>
     * 
     * @return Returns true if we have successfully move to the next cursor, 
     * and false if there was an error or if there was no more records.
     */
    public boolean moveNext() {
        // Default to false in case of any failure.
        this.moveNext = false;
        
        try {
            
            if (intRSType == zXType.dsRSType.dsrstCollection.pos) {
                this.dataCursor++;
                moveNext = (this.dataCursor < this.data.size());
                
            } else if (intRSType == zXType.dsRSType.dsrstRS.pos) {
                /**
                 * We may need to verify we are not already at the end.
                 */ 
                moveNext = this.rs.getTarget().next();
                
            } else if (intRSType == zXType.dsRSType.dsrstStream.pos) {
                /**
                 * Tell the DS to move to the next record in the stream.
                 */
                moveNext = this.DSHandler.RSMoveNext(this).pos == zXType.rc.rcOK.pos;
                
            }
            
            /**
             * We are dealing with a new row and thus with new data
             */
            setNewData(true);
            
            return moveNext;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to move to the next row.", e);
            return moveNext;
        }
    }
    
    /**
     * Populate BO from values in recordset.
     * 
     * @param pobjBO The business object to populate.
     * @param pstrGroup The attribute to use.
     * @return Returns the return code of rs2obj.
     */
    public zXType.rc rs2obj(ZXBO pobjBO, String pstrGroup) {
        return rs2obj(pobjBO, pstrGroup, false);
    }
    
    /**
     * Populate BO from values in recordset.
     * 
     * BD18MAY05 - V1.5:18 - Added support for propertyPersistStatus
     * 
     * @param pobjBO The business object to populate.
     * @param pstrGroup The attribute to use.
     * @param pblnResolveFK Whether to resolve the foriegn key. Optional default should be false.
     * @return Returns the return code of rs2obj.
     */
    public zXType.rc rs2obj(ZXBO pobjBO, String pstrGroup, boolean pblnResolveFK) {
        zXType.rc rs2obj = zXType.rc.rcOK;
        try {
            
        	/**
        	 * First we have to set all properties to 'new' that are currently 'loaded'; this
            ' is to overcome some weird scenario as the following. Imagine we have
            ' 5 attributes (A, B, C, D, E) where E is a dynamic value that requires
            ' D to be loaded. The developer loops over a recordset (and thus calls
            ' rs2obj for each row) but only loads A, B and C but does want to show
            ' E on ths screen. The ensureLoad logic that is called when trying to get
            ' the value of E forces D to be loaded. However, the next time around (ie
            ' next call to rs2obj) A, B and C and re-loaded and D still has the persist status
            ' of loaded although it has the value of the previous record!
            '
            ' Note that we only do so when the newData flag is set; ie the first call to rs2obj after
            ' a new recordset has been created or a move or a moveNext; for any subsequent call to
            ' rs2obj for the same row, we leave the status as-is
        	 */
            if (isNewData()) {
                setNewData(false);
                pobjBO.setPropertyPersistStatus("*", zXType.propertyPersistStatus.ppsNew, zXType.propertyPersistStatus.ppsLoaded);
            }
            
            if (intRSType == zXType.dsRSType.dsrstCollection.pos) {
            	/**
            	 *  Need set set property persist status (this is done in rs2obj
            	 *  but not in bo2bo); see bos.rs2obj for a long explanation on why
            	 *  we need to reset all properties to 'new' that currently have
            	 *  a perist status of 'loaded'
            	 */
            	pobjBO.setPropertyPersistStatus(pstrGroup, 
            									zXType.propertyPersistStatus.ppsNew, 
            									zXType.propertyPersistStatus.ppsLoaded);
                rs2obj = ((ZXBO)this.data.get(this.dataCursor)).bo2bo(pobjBO, pstrGroup);
            	pobjBO.setPropertyPersistStatus(pstrGroup, 
												zXType.propertyPersistStatus.ppsLoaded, 
												null);
            	
            } else if (intRSType == zXType.dsRSType.dsrstRS.pos) {
                if (eof()) {
                    rs2obj = zXType.rc.rcError;
                    return rs2obj;
                }
                rs2obj = pobjBO.rs2obj(this.rs, pstrGroup, pblnResolveFK, this.pureNames);
                
            } else if (intRSType == zXType.dsRSType.dsrstStream.pos) {
                /**
                 * Note that the assumption is that a move next on a stream will have populated the first
                 * element of the data collection
                 */
                if (this.data.size() > 1) {
                    getZx().trace.addError("Data source handler with result-set type 'stream' has not set first element on data collection on MoveNext");
                }
                
            	pobjBO.setPropertyPersistStatus(pstrGroup, 
						zXType.propertyPersistStatus.ppsNew, 
						zXType.propertyPersistStatus.ppsLoaded);
                rs2obj = ((ZXBO)this.data.get(0)).bo2bo(pobjBO, pstrGroup);
            	pobjBO.setPropertyPersistStatus(pstrGroup, 
						zXType.propertyPersistStatus.ppsLoaded, 
						null);
                
            }
            
            return rs2obj;
        } catch (Exception e) {
            throw new NestableRuntimeException(e);
        }
    }
    
    /**
     * Copy BO values to fields in recordset.
     * 
     * @param pobjBO The business object to populate.
     * @return Returns the return code of bo2rs.
     */
    public zXType.rc bo2rs(ZXBO pobjBO) {
        return bo2rs(pobjBO, "*");
    }
    
    /**
     * Copy BO values to fields in recordset.
     * 
     * @param pobjBO The business object to populate.
     * @param pstrGroup The Atribute group to load. Optional, default should be "*"
     * @return Returns the return code of bo2rs.
     */
    public zXType.rc bo2rs(ZXBO pobjBO, String pstrGroup) {
        zXType.rc bo2rs = zXType.rc.rcOK;
        
        try {
            
            if (intRSType == zXType.dsRSType.dsrstCollection.pos) {
                /**
                 * Not supported for collections
                 */
                throw new RuntimeException("Unsupported method : bo2rs");
                
            } else if (intRSType == zXType.dsRSType.dsrstRS.pos && this.rs != null) {
                bo2rs = pobjBO.bo2rs(this.rs, pstrGroup, true);
                
            } else if (intRSType == zXType.dsRSType.dsrstStream.pos) {
                /**
                 * Not supported for streams
                 */
                throw new RuntimeException("Unsupported method : bo2rs");
                
            } else {
                throw new RuntimeException("Failed to execute bo2rs");
            }
            
            return bo2rs;
        } catch (Exception e) {
            bo2rs = zXType.rc.rcError;
            return bo2rs;
        }
        
    }
    
    /**
     * Get value of single attribute from recordset.
     * 
     * @param pobjBO The business object.
     * @param pobjAttr The attribute you want the column name of.
     * @return Returns the property value.
     */
    public Property rsColumn(ZXBO pobjBO, Attribute pobjAttr) {
        Property rsColumn = null;
        
        try {
            if (intRSType == zXType.dsRSType.dsrstCollection.pos) {
                if (eof()) {
                	rsColumn = ZX.nullValue();
                    return rsColumn;
                }
                
                ZXBO objBO = (ZXBO)this.data.get(this.dataCursor);
                // .clone was called in the vb version.
                rsColumn = objBO.getValue(pobjAttr.getName()); 
                
            } else if (intRSType == zXType.dsRSType.dsrstRS.pos) {
                if (eof()) {
                	rsColumn = ZX.nullValue();
                    return rsColumn;
                }
                
                rsColumn = rs.getPropertyFromDB(pobjBO, pobjAttr, zXType.sqlObjectName.sonRSName);
                
            } else if (intRSType == zXType.dsRSType.dsrstStream.pos) {
                /**
                 * Note that the assumption is that a move next on a stream will have populated the first
                 * element of the data collection
                 */
                if (this.data.size() < 1) {
                    throw new RuntimeException("Data source handler with result-set type 'stream' has not set first element on data collection on MoveNext");
                }
                
                ZXBO objBO = (ZXBO)this.data.get(0);
                // clone was called in vb version.
                rsColumn = objBO.getValue(pobjAttr.getName());
            }
            
            return rsColumn;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get value of single attribute from recordset.", e);
            return rsColumn;
        }
    }
    
    /**
     * Close recordset.
     * 
     * @return Returns the return code of RSClose.
     */
    public zXType.rc RSClose() {
        zXType.rc RSClose = zXType.rc.rcOK;
        
        try {
            if (intRSType == zXType.dsRSType.dsrstCollection.pos) {
                this.data = new ArrayList();
                this.dataCursor = 0;
                
            } else if (intRSType == zXType.dsRSType.dsrstStream.pos) {
                RSClose = this.DSHandler.RSClose(this);
                
            } else if (intRSType == zXType.dsRSType.dsrstRS.pos && this.rs != null) {
                /**
                 * Close the resultset
                 */
                this.rs.close();
            }
            
            this.isOpen = false;
            
            return RSClose;
        } catch (Exception e) {
            // Do not give.
            getZx().log.error("Failed to close resultset", e);
            RSClose = zXType.rc.rcError;
            return RSClose;
        }
    }
    
    /**
     * Move x places in recordset.
     * 
     * @param pintNumRecords The number of positions to seek to.
     * @return Returns the return code of the method.
     */
    public zXType.rc move(int pintNumRecords) {
        zXType.rc move = zXType.rc.rcOK;
        
        try {
            
            if (intRSType == zXType.dsRSType.dsrstCollection.pos) {
                getData().get(this.dataCursor + pintNumRecords);
                
//                for (int i = 0; i < pintNumRecords; i++) {
//                    moveNext();
//                    if (eof()) {
//                        move = zXType.rc.rcWarning;
//                        return move;
//                    }
//                }
                
            } else if (intRSType == zXType.dsRSType.dsrstRS.pos) {
                // We may need to verify we are not already at the end.
                getRs().relative(pintNumRecords);
                
            } else if (intRSType == zXType.dsRSType.dsrstStream.pos) {
                /**
                 * Not yet supported
                 */
                throw new RuntimeException("Unsupported method move for rsType stream.");
            }
            
            /**
             * We are dealing with a new row and thus with new data
             */
            setNewData(true);
            
            return move;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Move x places in recordset.", e);
            move = zXType.rc.rcError;
            return move;
        }
    }
    
    //------------------------------------ Some extra methods..    
    
    // column count? etc..
    
    //------------------------------------ Garbage collection code.
    
    /**
     * Do any cleanup work over here. 
     */
    public void finalize() {
        try {
            if (isOpen) {
                RSClose();
                getZx().log.error("Miss code somewhere ???");
            }
        } catch (Exception e){
            getZx().log.error("Failed to cleanup");
        }
    }
}