/*
 * Created on Nov 27, 2004 by Michael Brewer
 * $Id: DocTpe.java,v 1.1.2.4 2006/07/17 16:12:39 mike Exp $
 */
package org.zxframework.misc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * ZX Document Type business object.
 * 
 * <pre>
 * 
 *  Change    : DGS06FEB2003
 *  Why       : In getDoc, encode filename to remove unfriendly characters.
 * 
 *  Change    : DGS18FEB2003
 *  Why       : In getDoc, encode original filename to remove unfriendly characters.
 * 
 *  Change    : DGS02APR2003
 *  Why       : In getDoc, pulled out code to get the path into its own public function so
 *              other code can call it (getPath).
 * 
 *  Change    : DGS05DEC2003
 *  Why       : In getDoc, if incoming filename is nullstring, set that of the new zXDoc
 *              to the filename (without path) that we derive.
 * 
 *  Change    : DGS14APR2004
 *  Why       : In getPath, if 'keyword' doc type, when the keywords are split into an
 *              array a UBound of equal to zero also means we have one (as when > zero) * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @since 0.01
 * @version 0.0.1 
 */
public class DocTpe extends ZXBO {
	
	//------------------------ Members
	
	private static Log log = LogFactory.getLog(DocTpe.class);
	
	//------------------------ Constructor
	
    /**
     * Default contructor.
     */
    public DocTpe() {
        super();
    }

    /**
     * Creates a new zxDoc row and returns a populated zxDoc object.
     *
     * @param pstrDocTpe The documents type of the ZXDOC
     * @param pstrOrgFleNme Optional, default is "" 
     * @param pstrKeyWrds Optional, default is "" 
     * @return Returns a populate ZXDoc object.
     * @throws ZXException Thrown if getDoc fails. 
     */
    public ZXDoc getDoc(String pstrDocTpe, String pstrOrgFleNme, String pstrKeyWrds) throws ZXException {
        ZXDoc getDoc = null;
        
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDocTpe", pstrDocTpe);
            getZx().trace.traceParam("pstrOrgFleNme", pstrOrgFleNme);
            getZx().trace.traceParam("pstrKeyWrds", pstrKeyWrds);
        }

        try {
            
            /**
             * If not already got this docType in 'me', load now
             */
            if (!getValue("id").getStringValue().equalsIgnoreCase(pstrDocTpe)) {
                setValue("id", pstrDocTpe);
                if (loadBO().pos != zXType.rc.rcOK.pos) {
                    getZx().trace.addError("Unable to load zXDocTpe");
                    throw new ZXException();
                }
            }
            
            /**
             * Create instance of zxDoc
             */
            getDoc = (ZXDoc)getZx().createBO("zxDoc");
            if (getDoc == null) {
                getZx().trace.addError("Unable to create instance of zxDoc");
                throw new ZXException();
            }
            
            /**
             * Reset all
             */
            getDoc.resetBO();
            
            /**
             * Assign new id
             */
            getDoc.setAutomatics("+");
            
            /**
             * Set sub directory portion of path, if anything
             * DGS02APR2003: Removed code into own function getPath to make it available elsewhere.
             */
            String strSubDir = getPath(pstrDocTpe, pstrKeyWrds);
            
            /**
             * String together the elements to make up the doc's path relative to the base path
             *  First the sub dir, if any...
             *  DGS05DEC2003: Do it a bit later so we can extract just the filename
             */
            // strFleNme = strSubDir
            
            // ...then the Doc Type, if applicable:
            String strFleNme = "";
            if (getValue("inclDocTpeInNme").booleanValue()) {
                strFleNme = strFleNme + pstrDocTpe + "-";
            }
            
            // ...then the Doc ID, if applicable:
            if (getValue("inclDocIdInNme").booleanValue()) {
                strFleNme = strFleNme + getDoc.getValue("id").getStringValue();
            }
            
            // ...then the incoming original filename, if anything. Otherwise add the default extension:
            if (StringUtil.len(pstrOrgFleNme) == 0) {
                strFleNme = strFleNme + "." + getValue("defExtn").getStringValue();
                pstrOrgFleNme = strFleNme;
            } else {
                // ' DGS 06FEB2003: Encode string to remove unfriendly characters:
                strFleNme = strFleNme + "-" + pstrOrgFleNme; // encodeASCIIString(pstrOrgFleNme)
            }
            
            /**
             * DGS05DEC2003: Do this here instead of a few lines earlier so we can extract just the filename
             */
            strFleNme = strSubDir + strFleNme;
            
            /**
             * Put the doc's path relative to the base path into the doc object:
             */
            if (getDoc.setValue("fleNme", strFleNme).pos != zXType.rc.rcOK.pos) {
                getZx().trace.addError("Unable to set filename in zxDoc");
                throw new ZXException();
            }
            
            /**
             * Put the original filename into the doc object:
             * DGS 18FEB2003: Encode string to remove unfriendly characters:
             * // encodeASCIIString(pstrOrgFleNme)
             */
            if (getDoc.setValue("orgFleNme", pstrOrgFleNme).pos != zXType.rc.rcOK.pos) { 
                getZx().trace.addError("Unable to set original filename in zxDoc");
                throw new ZXException();
            }
            
            /**
             * Put the keywords into the doc object:
             */
            if (getDoc.setValue("keyWrds", pstrKeyWrds).pos != zXType.rc.rcOK.pos) { 
                getZx().trace.addError("Unable to set keywords in zxDoc");
                throw new ZXException();
            }
            
            /**
             * Put the doc type into the doc object:
             */
            if (getDoc.setValue("docTpe", pstrDocTpe).pos != zXType.rc.rcOK.pos) { 
                getZx().trace.addError("Unable to set doc type in zxDoc");
                throw new ZXException();
            }
            
            /**
             * Insert the new doc row
             */
            if (getDoc.insertBO().pos != zXType.rc.rcOK.pos) {
                getZx().trace.addError("Unable to insert zxDoc row");
                throw new ZXException();
            }
            
            return getDoc;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Creates a new zxDoc row and returns a populated zxDoc object.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pstrDocTpe = " + pstrDocTpe);
                log.error("Parameter : pstrOrgFleNme = " + pstrOrgFleNme);
                log.error("Parameter : pstrKeyWrds = " + pstrKeyWrds);
            }
            if (getZx().throwException) throw new ZXException(e);
            return getDoc;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getDoc);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Determines the path of the doc. 
     * 
     * This is without the base dir on the front and without the filename on the end..
     *
     * @param pstrDocTpe The document type.
     * @param pstrKeyWrds Optional, default is "" 
     * @return Returns the path of the doc.
     * @throws ZXException Thrown if getPath fails. 
     */
    public String getPath(String pstrDocTpe, String pstrKeyWrds) throws ZXException {
        String getPath = "";
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDocTpe", pstrDocTpe);
            getZx().trace.traceParam("pstrKeyWrds", pstrKeyWrds);
        }

        try {
            
            /**
             * If not already got this docType in 'me', load now
             */
            if (!getValue("id").getStringValue().equalsIgnoreCase(pstrDocTpe)) {
                setValue("id", pstrDocTpe);
                if (loadBO().pos != zXType.rc.rcOK.pos) {
                    getZx().trace.addError("Unable to load zXDocTpe");
                    throw new ZXException();
                }
            }
            
            /**
             * Get the base directory of the path (used if sub dir has to be created)
             */
            String strBseDir = getValue("bseDir").getStringValue(); // Some filesystems are case sensetive .toUpperCase();
            
            /**
             * Set sub directory portion of path, if anything
             */
            String strSubDirTpe = getValue("subDirTpe").getStringValue(); // .toUpperCase();
            
            if (strSubDirTpe.equalsIgnoreCase("DATE")) {
                
                // Set to three-level sub dir like 2002\11\05\ for 5Nov2002:
                Date objDate = new Date();
                SimpleDateFormat df;
                
                df = new SimpleDateFormat("yyyy");
                getPath = df.format(objDate);
                if (createFolderIfNeeded(strBseDir + File.separatorChar + getPath).pos != zXType.rc.rcOK.pos) {
                    getZx().trace.addError("Unable to create current year folder");
                    throw new ZXException();
                }
                
                df = new SimpleDateFormat("mm");
                getPath = getPath + File.separatorChar + df.format(objDate);
                if (createFolderIfNeeded(strBseDir + File.separatorChar + getPath).pos != zXType.rc.rcOK.pos) {
                    getZx().trace.addError("Unable to create current month folder");
                    throw new ZXException();
                }
                
                df = new SimpleDateFormat("dd");
                getPath = getPath + File.separatorChar + df.format(objDate);
                if (createFolderIfNeeded(strBseDir + File.separatorChar + getPath).pos != zXType.rc.rcOK.pos) {
                    getZx().trace.addError("Unable to create current day folder");
                    throw new ZXException();
                }    
                
                // getPath = getPath + File.separatorChar;
                
            } else if (strSubDirTpe.equalsIgnoreCase("USER")) {
                
                // Use the user user id :
                getPath = getZx().getUserProfile().getValue("id").getStringValue() + File.separatorChar;
                
                if (createFolderIfNeeded(strBseDir + File.separatorChar + getPath).pos != zXType.rc.rcOK.pos) {
                    getZx().trace.addError("Unable to create current user folder");
                    throw new ZXException();
                }
                
            } else if (strSubDirTpe.equalsIgnoreCase("KEYWORD")) {
                
                // Set to first keyword passed in, if any:
                if (StringUtil.len(pstrKeyWrds) > 0) {
                    int intIndexOf = pstrKeyWrds.indexOf(',');
                    if (intIndexOf != -1) {
                        getPath = pstrKeyWrds.substring(0, intIndexOf);
                    }
                }
                
                if (createFolderIfNeeded(strBseDir + File.separatorChar + getPath).pos != zXType.rc.rcOK.pos) {
                    getZx().trace.addError("Unable to create current keyword folder");
                    throw new ZXException();
                }
                
            } else {
                getPath = "";
            }
            
            return getPath;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determines the path of the doc.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pstrDocTpe = " + pstrDocTpe);
                log.error("Parameter : pstrKeyWrds = " + pstrKeyWrds);
            }
            if (getZx().throwException) throw new ZXException(e);
            return getPath;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getPath);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
    * Create the folders if necessary.
    *
    * @param pstrFolderName The name of the folder to check for. 
    * @return Returns the return code of the method. 
    * @throws ZXException Thrown if createFolderIfNeeded fails. 
    */
    public zXType.rc createFolderIfNeeded(String pstrFolderName) throws ZXException{
        zXType.rc createFolderIfNeeded = zXType.rc.rcOK; 
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrFolderName", pstrFolderName);
        }

        try {
            
            File objFile = new File(pstrFolderName);
            if (!objFile.exists()) {
                if (!objFile.createNewFile()) {
                    // Failed to create file.
                    createFolderIfNeeded = zXType.rc.rcError;
                }
            }
            
            return createFolderIfNeeded;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Create the folders if necessary.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pstrFolderName = "+ pstrFolderName);
            }
            if (getZx().throwException) throw new ZXException(e);
            createFolderIfNeeded = zXType.rc.rcError;
            return createFolderIfNeeded;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(createFolderIfNeeded);
                getZx().trace.exitMethod();
            }
        }
    }
}