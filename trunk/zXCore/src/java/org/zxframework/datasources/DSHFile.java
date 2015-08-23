/*
 * Created on May 23, 2005
 * $Id: DSHFile.java,v 1.1.2.15 2006/07/17 16:26:14 mike Exp $
 */
package org.zxframework.datasources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jdom.Element;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.exception.ParsingException;
import org.zxframework.transaction.FakeTransaction;
import org.zxframework.transaction.Transaction;
import org.zxframework.util.StringUtil;
import org.zxframework.zXType.rc;

/**
 * File handler as a demo.
 * 
 * <pre>
 * 
 * This is just a demo proof of concept.
 * 
 * As this is a sequencial file that we are reading on we might presume the following
 * 
 * 1) The format is flexible. 
 *  eg : tab seperated/comma seperated etc..
 *  	 or it is compatible with loadBORecord.
 * 2) Performance is not really an issue.
 * 		- We should be using RandomacessFile deletes and updates.
 * 3) No transactions support
 * 4) This is not a full featured as the DSHRdbms handler.
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class DSHFile extends DSHandler {
    
    //------------------------ Members
    
    private String dataDir;
    private String seperator;
    
    //------------------------ Constants
    
    /** Used to replace a \n or \r in a field */
    private static final String NEW_LINE = "<NL>";
    
    //------------------------ Constructors
    
    /**
     * Default constructor
     */
    public DSHFile() {
    	super();
    }
    
    //------------------------ Getters/Setters
    
    /**
     * The base directory of where the file recide.
     * 
     * @return Returns the dataDir.
     */
    public String getDataDir() {
        return dataDir;
    }
    
    /**
     * @param dataDir The dataDir to set.
     */
    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }
    
    /**
     * The seperator for the columns.
     * 
     * @return Returns the seperator.
     */
    public String getSeperator() {
        return seperator;
    }
    
    /**
     * @param seperator The seperator to set.
     */
    public void setSeperator(String seperator) {
        this.seperator = seperator;
    }
    
    //------------------------------- Implemented methods
    
    /**
     * Parsing File Handler specific settings.
     * 
     * @see org.zxframework.datasources.DSHandler#parse(org.jdom.Element)
     */
    public rc parse(Element pobjElement) throws ParsingException {
        zXType.rc parse = zXType.rc.rcOK;
        
        setDataDir(pobjElement.getChildText("dataDir"));
        setSeperator(pobjElement.getChildText("seperator"));
        
        return parse;
    }

    /**
     * Load BO from a file.
     * 
     * @see org.zxframework.datasources.DSHandler#loadBO(ZXBO, String, String, boolean, String)
     */
    public rc loadBO(ZXBO pobjBO, 
                     String pstrLoadGroup, 
                     String pstrWhereGroup,
                     boolean pblnResolveFK, 
                     String pstrOrderByGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrLoadGroup", pstrLoadGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
        }
        
        zXType.rc loadBO = zXType.rc.rcOK;
        
        BufferedReader in = null;
        
        /**
         * Handle defaults.
         */
        if (pstrLoadGroup == null) {
            pstrLoadGroup = "*";
        }
        if (pstrWhereGroup == null) {
            pstrWhereGroup = "+";
        }
        
        try {
            if (!pobjBO.noTable()) {
                // Get a handle to the file
                File file = getFileRecord(pobjBO);
                in = new BufferedReader(new FileReader(file));
                
                // Make a local copy of the original bo.
                ZXBO objBOTmp = pobjBO.cloneBO("");
                AttributeCollection colAttributes = pobjBO.getDescriptor().getAttributes();
                
                // Get the column headers
                String record = in.readLine();
                Map colColumnHeaders = getColumnHeaders(record, colAttributes);
                
                // Load conditions
                DSWhereClause objDSWhereClause = new DSWhereClause();
                objDSWhereClause.parse(pobjBO, pstrWhereGroup);
                
                // Find the correct line.
                boolean blnFoundMatch = false;
                reading : while ((record = in.readLine()) != null) {
                	// Load the line in as a record.
                    loadBORecord(objBOTmp, colAttributes, colColumnHeaders, record);
                    
                    // Check whether this is the correct line.
                    blnFoundMatch = getZx().getBos().isMatchingBO(objBOTmp, objDSWhereClause);
                    // Exit on the first match.
                    if (blnFoundMatch) {
                        break reading;
                    }
                }

                if (blnFoundMatch) {
                	// Copy the loaded values into BO.
                    objBOTmp.bo2bo(pobjBO, pstrLoadGroup);
                } else {
                    //Failed to find any values
                    loadBO = zXType.rc.rcWarning;
                    return loadBO;
                }
                
            } // Dont bother for BOs without a table
            
            return loadBO;
            
        } catch (Exception e) {
            getZx().trace.addError("Load a BO instance from the database", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrLoadGroup = " + pstrLoadGroup);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
                getZx().log.error("Parameter : pblnResolveFK = " + pblnResolveFK);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            loadBO = zXType.rc.rcError;
            return loadBO;
            
        } finally {
            /**
             * Close InputStream
             */
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    getZx().log.error("Failed to close file.", e);
                }
            }
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(loadBO);
                getZx().trace.exitMethod();
            }
        }
    
    }

    /**
     * @see org.zxframework.datasources.DSHandler#insertBO(org.zxframework.ZXBO)
     */
    public rc insertBO(ZXBO pobjBO) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc insertBO = zXType.rc.rcOK;
        PrintStream out = null;
        
        try {
            /**
             * Dont bother for BOs without a file
             */
            if(!pobjBO.noTable()) {
                /// Get a handle to the file
                File file = getFileRecord(pobjBO);
                
                //  Open the file for appending.
                out = new PrintStream(new FileOutputStream(file, true));
                
                // Add record to the end of the file. 
                out.print(getBOASRecord(pobjBO));
            }
            
            return insertBO;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Insert a business object in the database", e);
            
            if (getZx().throwException) { throw new ZXException(e); }
            insertBO = zXType.rc.rcError;
            return insertBO;
            
        } finally {
            /**
             * Close Output Stream.
             */
            if (out != null) {
                try {
                    out.close();
                } catch(Exception e) { 
                    getZx().log.error("Failed to close stream.", e);
                }
            }
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(insertBO);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * We assume for now that we are not updating the id.
     * 
     * Should use a "RandomAccessFile" for performance.
     * 
     * @see org.zxframework.datasources.DSHandler#updateBO(ZXBO, String, String)
     */
    public rc updateBO(ZXBO pobjBO, 
                       String pstrUpdateGroup, 
                       String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrUpdateGroup", pstrUpdateGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }
        
        zXType.rc updateBO = zXType.rc.rcOK;
        
        BufferedReader in = null;
        PrintWriter copy = null;
        
        try {
        	
            if(!pobjBO.noTable()) {
                // Get a handle to the file
                File file = getFileRecord(pobjBO);
                in = new BufferedReader(new FileReader(file));
                
                // A virtual copy of the file
                StringWriter copyWrite = new StringWriter();
                copy = new PrintWriter(copyWrite);
                
                // Make a copy of the original
                ZXBO objBO = pobjBO.cloneBO("");
                AttributeCollection colAttributes = pobjBO.getDescriptor().getAttributes();
                
                // Get the column headers
                String record = in.readLine();
                copy.println(record); // Copy headers
                Map colColumnHeaders = getColumnHeaders(record, colAttributes);
                
                // Load conditions
                DSWhereClause objDSWhereClause = new DSWhereClause();
                objDSWhereClause.parse(pobjBO, pstrWhereGroup);
                
                // Find the correct line.
                boolean blnFoundMatch = false;
                reading : while ((record = in.readLine()) != null) {
                    /**
                     * Only load a record if we have not yet found a match to delete.
                     */
                    if (!blnFoundMatch) {
                        loadBORecord(objBO, colAttributes, colColumnHeaders, record);
                    }
                    
                    /**
                     * See if this is the correct record.
                     */
                    if (!blnFoundMatch
                        && getZx().getBos().isMatchingBO(objBO, objDSWhereClause)) {
                        blnFoundMatch = true;
                        
                        // Generate the new record.
                        String write = getBOASRecord(pobjBO);
                        
                        if (write.equals(record + "\n")) {
                            // Do not update if there is no difference
                        	// between the old and the new.
                            copy.close();
                            copy = null;
                            break reading;
                        }
                        
                        /// And then add the updated record.
                        copy.print(write);
                        
                    } else {
                        // We want to copy this record
                    	// as it is not the one that we be updated.
                        copy.println(record);
                        
                    }
                    
                }
                
                if (blnFoundMatch) {
                    /**
                     * Write update to the file.
                     * Only if there is something to update..
                     */
                    if (copy != null) {
                        // Close file and clean up.
                        in.close();
                        in = null;
                        // Flush ..
                        copy.flush();
                        
                        // Overwrite file with new contents.
                        String contents = copyWrite.toString();
                        PrintStream updatedRecord = null;
                        try {
                            // Open file and overide.
                            updatedRecord = new PrintStream(new FileOutputStream(file, false));
                            updatedRecord.write(contents.getBytes());
                            
                            // Update must have been successfull
                            updateBO = zXType.rc.rcOK;
                            
                        } catch (Exception e) {
                            getZx().trace.addError("Failed to write to db file.", file.getAbsolutePath(), e);
                            // Failed to save to file
                            updateBO = zXType.rc.rcError;
                            
                        } finally {
                            // Clean up.
                            if (updatedRecord!= null) {
                                updatedRecord.flush();
                                updatedRecord.close();
                                updatedRecord = null;
                            }
                            copy.close();
                            copy = null;
                            
                        }
                    }
                    
                } else {
                    // Could not find the record we where looking for.
                    updateBO = zXType.rc.rcWarning;
                    return updateBO;
                    
                }

            } // Dont bother for BOs without a table
            
            return updateBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Update a business object ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrUpdateGroup = " + pstrUpdateGroup);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            updateBO = zXType.rc.rcError;
            return updateBO;
            
        } finally {
            /**
             * Close Output Stream.
             */
            if (in != null) {
                try {
                    in.close();
                } catch(Exception e) { 
                    getZx().log.error("Failed to close stream.", e);
                }
                
            }
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(updateBO);
                getZx().trace.exitMethod();
            }
        } 
    
    }

    /**
     * Deletes the record.
     * 
     * NOTE: This only support simple deletes, none of the cascading deletes etc..
     * 
     * Should use a "RandomAccessFile" for performance.
     * 
     * @see org.zxframework.datasources.DSHandler#deleteBO(ZXBO, String)
     */
    public rc deleteBO(ZXBO pobjBO, String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }
        
        zXType.rc deleteBO = zXType.rc.rcOK;
        
        BufferedReader in = null;
        PrintWriter copy = null;
        
        try {
            /**
             * Dont bother for BOs with no table
             */
            if (pobjBO.noTable()) return deleteBO;
            
            zXType.deleteRule deleteRule = pobjBO.getDescriptor().getDeleteRule();
            if (deleteRule.equals(zXType.deleteRule.drAllowed)) {
                // Get a handle to the file
                File file = getFileRecord(pobjBO);
                in = new BufferedReader(new FileReader(file));
                
                // A virtual copy of the file
                StringWriter copyWrite = new StringWriter();
                copy = new PrintWriter(copyWrite);
                
                // Make a copy of the original
                ZXBO objBO = pobjBO.cloneBO("");
                AttributeCollection colAttributes = pobjBO.getDescriptor().getAttributes();
                
                // Get the column headers
                String record = in.readLine();
                copy.println(record); // Copy headers
                Map colColumnHeaders = getColumnHeaders(record, colAttributes);
                
                DSWhereClause objDSWhereClause = new DSWhereClause();
                objDSWhereClause.parse(pobjBO, pstrWhereGroup);
                
                /**
                 * Find the correct line.
                 */
                boolean blnFoundMatch = false;
                while ((record = in.readLine()) != null) {
                    /**
                     * Only load a record if we have not yet found a match to delete.
                     */
                    if (!blnFoundMatch) {
                        loadBORecord(objBO, colAttributes, colColumnHeaders, record);
                    }
                    
                    /**
                     * See if this is the correct record.
                     */
                    if (!blnFoundMatch && getZx().getBos().isMatchingBO(objBO, objDSWhereClause)) {
                        /**
                         * Once we have found a match
                         * we do not want to look for another.
                         * 
                         * We could load code this to delete more than
                         * one record...
                         */
                        blnFoundMatch = true;
                        
                    } else {
                        // We want to keep this record.
                        copy.println(record);
                    }
                }
                
                if (blnFoundMatch) {
                    /**
                     * Write copy to the file
                     */
                    in.close();
                    in = null;
                    copy.flush();
                    
                    /**
                     * Overwrite with new contents.
                     */
                    String contents = copyWrite.toString();
                    if (contents != null) {
                        PrintStream newFile = null;
                        try {
                            newFile = new PrintStream(new FileOutputStream(file, false));
                            newFile.write(contents.getBytes());
                            
                            // We have deleted one record.
                            deleteBO = zXType.rc.rcOK;
                            
                        } catch (Exception e) {
                            getZx().trace.addError("Failed to write to db file.", file.getAbsolutePath(), e);
                            deleteBO = zXType.rc.rcError;
                            
                        } finally {
                            /**
                             * Clean up.
                             */
                            if (newFile!= null) {
                                newFile.close();
                            }
                            copy.close();
                        }
                    }
                    
                } else {
                    /**
                     * Could not find the record we where looking for.
                     */
                    deleteBO = zXType.rc.rcWarning;
                    return deleteBO;
                }
                
            } else {
                /**
                 * We do not support any other type of delete.
                 */
            	
                /**
                 * Simply not allowed
                 */
                getZx().trace.userErrorAdd("Delete rule is set to not-allowed for this entity (" 
                						   + pobjBO.getDescriptor().getLabel().getLabel() 
                						   + ")");
                deleteBO = zXType.rc.rcError;
            }
            
            return deleteBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Delete BO", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrWhereAttributeGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
            deleteBO = zXType.rc.rcError;
            return deleteBO;
            
        } finally {
            /**
             * Close Output Stream.
             */
            if (in != null) {
                try {
                    in.close();
                    
                } catch(Exception e) { 
                    getZx().log.error("Failed to close stream.", e);
                }
            }
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(deleteBO);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * @see org.zxframework.datasources.DSHandler#boRS(ZXBO, String, String, boolean, String, boolean, int, int)
     */
    public DSRS boRS(ZXBO pobjBO, 
                     String pstrLoadGroup, 
                     String pstrWhereGroup,
                     boolean pblnResolveFK, 
                     String pstrOrderByGroup,
                     boolean pblnReverse, 
                     int plngStartRow, 
                     int plngBatchSize) throws ZXException {
        
        BufferedReader in = null;
        DSRS boRS = new DSRS(this);
        
        try {
            // Get a handle to the file
            File file = getFileRecord(pobjBO);
            in = new BufferedReader(new FileReader(file));
            
            // Make a copy of the original
            ZXBO objBO = pobjBO.cloneBO("");
            AttributeCollection colAttributes = pobjBO.getDescriptor().getAttributes();
            
            // Get the column headers
            String record = in.readLine();
            Map colColumnHeaders = getColumnHeaders(record, colAttributes);
            
            // Load conditions
            DSWhereClause objDSWhereClause = new DSWhereClause();
            objDSWhereClause.parse(pobjBO, pstrWhereGroup);
            
            int pos = 1;
            int readRows = 1;
            
            List values = new ArrayList();
            
            /**
             * Find the correct line.
             */
            reading : while ((record = in.readLine()) != null) {
                objBO = objBO.cloneBO(); // Make sure we always have a clean handle.
                
                loadBORecord(objBO, colAttributes, colColumnHeaders, record);
                
                if (getZx().getBos().isMatchingBO(objBO, objDSWhereClause)) {
                    /**
                     * Seek to the correct position
                     */
                    if (plngStartRow == 0 || (pos >= plngStartRow)) {
                        values.add(objBO);
                        
                        /**
                         * Batch size
                         */
                        if (plngBatchSize > 0) {
                            /**
                             * Check if number of rows adds are within
                             * the batch size.
                             */
                            if (readRows >= plngBatchSize) {
                                break reading;
                            }
                            readRows++;
                        }
                    }
                    pos++;
                }
            }
            
            boRS.setData(values);
            boRS.setDataCursor(0);
            
        } catch (Exception e) {
            /**
             * Close resultset.
             */
            boRS.RSClose();
            
            if (e instanceof ZXException) {
                throw (ZXException)e;
            }
            throw new ZXException("Fails to execute.", e);
            
        } finally {
            /**
             * Close InputStream
             */
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    getZx().log.error("Failed to close file.", e);
                }
            }
            
        }
        
        return boRS;
    }
    
    /**
     * Connect to this datasource.
     * 
     * NOTE : This is not a real connection, it just verifies that the dataDirectory actually exists.
     * 
     * @see org.zxframework.datasources.DSHandler#connect()
     */
    public rc connect() throws ZXException {
        zXType.rc connect = zXType.rc.rcOK;
        
        File objFile = new File(getDataDir());
        if (objFile.isDirectory() && objFile.canRead() && objFile.canWrite()) {
            connect = zXType.rc.rcOK;
        } else {
            connect = zXType.rc.rcError;
        }
        
        return connect;
    }
    
    //------------------------ Not implemented for DSHFile.
    
    /**
     * @see org.zxframework.datasources.DSHandler#disConnect()
     */
    public rc disConnect() throws ZXException {
        return zXType.rc.rcOK;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#RSMoveNext(org.zxframework.datasources.DSRS)
     */
    public rc RSMoveNext(DSRS pobjRS) throws ZXException {
        zXType.rc RSMoveNext = zXType.rc.rcOK;
        return RSMoveNext;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#RSClose(org.zxframework.datasources.DSRS)
     */
    public rc RSClose(DSRS pobjRS) throws ZXException {
        zXType.rc RSClose = zXType.rc.rcOK;
        return RSClose;
    }
    
    //------------------------ The file channel does not support trancastions.
    
    /**
     * @see org.zxframework.datasources.DSHandler#beginTransaction()
     */
    public Transaction beginTransaction() throws ZXException {
    	// Use a dummy transaction handler.
        return new FakeTransaction();
    }

    /**
     * @see org.zxframework.datasources.DSHandler#beginTx()
     */
    public rc beginTx() throws ZXException {
        return zXType.rc.rcOK;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#inTx()
     */
    public boolean inTx() throws ZXException {
        return false;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#rollbackTx()
     */
    public rc rollbackTx() throws ZXException {
        return zXType.rc.rcOK;
    }

    /**
     * @see org.zxframework.datasources.DSHandler#commitTx()
     */
    public rc commitTx() throws ZXException {
        return zXType.rc.rcOK;
    }

    //------------------------ Util methods.
    
    /**
     * Gets the column names from the file. 
     * 
     * <p>
     * This presumes that the file has been formatted  in a particular way. 
     * 
     * eg :
     * COLUMN_NAME<sep>COLUMN_NAME
     * record<sep>record<sep>
     * 
     * <p>
     * NOTE : The keys are all lower cased.
     * 
     * @param columnHeaderRow The line containing the headers. 
     * @param pobjLoadGroup The attribute group of columns to retrieve.
     * @return Returns the specified columns and there position.
     */
    private Map getColumnHeaders(String columnHeaderRow, AttributeCollection pobjLoadGroup) {
        Map getColumnHeaders = new HashMap();
        
        StringTokenizer tokens = new StringTokenizer(columnHeaderRow, getSeperator());
        
        int pos = 0;
        Map colTemp = new HashMap();
        while (tokens.hasMoreTokens()) {
            String columnHeader = tokens.nextToken().toLowerCase();
            colTemp.put(columnHeader, new Integer(pos));
            pos++;
        }
        
        /**
         * And returns the columns that we want.
         */
        Iterator iter = pobjLoadGroup.iterator();
        while (iter.hasNext()) {
            Attribute objAttr = (Attribute)iter.next();
            if (StringUtil.len(objAttr.getColumn()) > 0) {
                Object value = colTemp.get(objAttr.getColumn().toLowerCase());
                if (value != null) {
                    /**
                     * There is a matching column.
                     */
                    getColumnHeaders.put(objAttr.getColumn().toLowerCase(), value);
                    
                } else {
                    /**
                     * No matching column, this might be a bug.
                     */
                    getColumnHeaders.put(objAttr.getColumn().toLowerCase(), new Integer(-1));
                    
                }
                
            } // Has a column ?
            
        } // Loop through load group.
        
        return getColumnHeaders;
    }
    
    /**
     * Load a Bo from a line.
     * 
     * @param pobjBO The business object to load the values into.
     * @param colAttr The attribute group to load
     * @param colColumnHeaders The column headers.
     * @param record The record to load
     * @return Returns the return code of the metod
     * @throws ZXException Thrown if loadBORecord fails
     */
    private zXType.rc loadBORecord(ZXBO pobjBO, 
                                   AttributeCollection colAttr,
                                   Map colColumnHeaders,
                                   String record) throws ZXException {
        zXType.rc loadBORecord = zXType.rc.rcOK;
        List values = new ArrayList();
        
        String tokens[] = record.split(seperator);
        for (int i = 0; i < tokens.length; i++) {
            values.add(tokens[i]);
        }
        
        int intTokens = values.size();
        
        /**
         * Reset the values for this business object.
         * We may be calling this recursively.
         */
        pobjBO.resetBO();
        
        Attribute objAttr;
        String value;
        
        Iterator iter = colAttr.iterator();
        while (iter.hasNext()) {
            objAttr = (Attribute)iter.next();
            
            if (StringUtil.len(objAttr.getColumn()) > 0) {
                /**
                 * Get the position of the column.
                 */
                int pos = ((Integer)colColumnHeaders.get(objAttr.getColumn().toLowerCase())).intValue();
                if (pos < intTokens) {
                    /**
                     * Load by position and clean data.
                     */
                    value = (String)values.get(pos);
                    value = StringUtil.replaceAll(value, NEW_LINE, "\n");
                    
                    pobjBO.setValue(objAttr.getName(), value);
                }
            }
        }
        
        // Set the persisStatus to clean as we have freshly loaded this from the database.
        pobjBO.setPersistStatus(zXType.persistStatus.psClean);
        
        return loadBORecord;
    }
    
    /**
     * Returns a handle to the file with the data in it. If no such
     * file exists with create one of the fly for us.
     * 
     * @param pobjBO The business object we want to get the file for.
     * @return Returns the file that has the business object records.
     * @throws ZXException Thrown if getFileRecord fails.
     */
    private File getFileRecord(ZXBO pobjBO) throws ZXException {
        File createFileRecord = null;
        
        PrintStream out = null;

        try {
            createFileRecord = new File (getDataDir() + pobjBO.getDescriptor().getTable());
            
            if (!createFileRecord.exists()) {
                // Create the file and its headers.
                
                /**
                 * First create the file
                 */
                createFileRecord.createNewFile();
                
                /**
                 * Dont bother for BOs without a file
                 */
                out = new PrintStream(new FileOutputStream(createFileRecord, true));
                
                boolean blnPrntSep = false;
                Attribute objAtr;
                
                Iterator iter = pobjBO.getDescriptor().getAttributes().iterator();
                while (iter.hasNext()) {
                    objAtr = (Attribute)iter.next();
                    
                    if (StringUtil.len(objAtr.getColumn()) > 0) {
                        if (blnPrntSep) {
                            out.print(seperator);
                        }
                        blnPrntSep = true;
                        
                        out.print(objAtr.getColumn());
                    }
                }
                
                out.println();
            }
            
            return createFileRecord;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Create fileRecord", e);
            if (getZx().throwException) { throw new ZXException(e); }
            return createFileRecord;
            
        } finally {
            /**
             * Close output stream.
             */
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    getZx().log.error("Failed close outputstream", e);
                }
            }
        }
    }
    
    /**
     * Get the BO as a record string.
     * 
     * @param pobjBO The business object instance to get the string value of.
     * @return Returns the String form of the BO ready for inserting into the file
     * @throws ZXException Thrown if getBOASRecord fails.
     */
    private String getBOASRecord(ZXBO pobjBO) throws ZXException {
        StringBuffer bo2Record = new StringBuffer();
        
        String strValue;
        
        Iterator iter = pobjBO.getDescriptor().getGroup("*").iterator();
        while (iter.hasNext()) {
            Attribute objAtr = (Attribute)iter.next();
            
            if (StringUtil.len(objAtr.getColumn()) > 0) {
                strValue = pobjBO.getValue(objAtr.getName()).getStringValue();
                strValue = StringUtil.replaceAll(strValue,'\n', "");
                strValue = StringUtil.replaceAll(strValue, '\r', NEW_LINE);
                
                bo2Record.append(strValue);
                bo2Record.append(seperator);

            }
            
        }
        bo2Record.append('\n');
        
        return bo2Record.toString();
    }
}