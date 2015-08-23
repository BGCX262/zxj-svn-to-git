/*
 * Created on Nov 27, 2004 by mbrewer-admin
 * $Id: ZXDoc.java,v 1.1.2.9 2006/07/17 16:13:25 mike Exp $
 */
package org.zxframework.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * Document business object.
 * 
 * <pre>
 * Change    : BD2APR03
 * Why       : Added saveAs function
 * 
 * Change    : BD9JUN03
 * Why       : Added fllFleNme attribute
 * 
 * Change    : DGS31JUL2003
 * Why       : For embedded gif/jpg images (including tifs exported to gif), added dummy querystring
 *              value to make the src URL unique (as already done for those viewed with viewer and
 *              file types other than gif/jpg/tif).
 * 
 * Change    : BD6MAY04
 * Why       : Fixed a bug in getAttr where it failed to associate the
 *                fllFleNme with the attribute definition (as usual I may add...)
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @since 0.01
 * @version 0.0.1
 *  
 **/
public class ZXDoc extends ZXBO {

    //------------------------ Member variables
    
	private static Log log = LogFactory.getLog(ZXDoc.class);
    private DocTpe docTpe;
    
    //------------------------ Contructor

    /**
     * Default contructor.
     */
    public ZXDoc() {
        super();
    }

    //------------------------ Public methods

    /**
     * View the document.
     *
     * @param pstrLogDir  
     * @param pstrPhysDir  
     * @return Returns 
     * @throws ZXException Thrown if view fails. 
     */
    public StringBuffer view(String pstrLogDir, String pstrPhysDir) throws ZXException {
        return view(pstrLogDir, pstrPhysDir, false, true);
    }

    /**
     * View the document.
     *
     * @param pstrLogDir  
     * @param pstrPhysDir  
     * @param pblnUseViewer Optional, default is false 
     * @return Returns 
     * @throws ZXException Thrown if view fails. 
     */
    public StringBuffer view(String pstrLogDir, String pstrPhysDir, boolean pblnUseViewer) throws ZXException {
        return view(pstrLogDir, pstrPhysDir, pblnUseViewer, true);
    }

    /**
     * View the document.
     *
     * @param pstrLogDir Logical directory 
     * @param pstrPhysDir Physical directory 
     * @param pblnUseViewer Optional, default is false 
     * @param pblnDownload Optional, default is true 
     * @return Returns the html for the view button.
     * @throws ZXException Thrown if view fails. 
     */
    public StringBuffer view(String pstrLogDir, String pstrPhysDir, boolean pblnUseViewer, boolean pblnDownload) throws ZXException {
        StringBuffer view = new StringBuffer(1024);
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrLogDir", pstrLogDir);
            getZx().trace.traceParam("pstrPhysDir", pstrPhysDir);
            getZx().trace.traceParam("pblnUseViewer", pblnUseViewer);
            getZx().trace.traceParam("pblnDownload", pblnDownload);
        }

        try {
        	
            if (loadBO("*").pos != zXType.rc.rcOK.pos) {
                getZx().trace.addError("Unable to load data for zxDoc");
                throw new ZXException();
            }
            
            /**
             * Need base dir from the doc's doc type
             */
            docTpe = (DocTpe)getZx().getBos().quickLoad("zxDocTpe", getValue("docTpe"), "", "bseDir");
            if (docTpe == null) {
                getZx().trace.addError("Unable to retrieve zxDocTpe");
                throw new ZXException();
            }
            
            /**
             * Always make sure we end with a / or \.
             */
            if (!pstrPhysDir.endsWith(File.separator)) {
            	pstrPhysDir = pstrPhysDir + File.separator;
            }
            if (!pstrLogDir.endsWith("/")) {
            	pstrPhysDir = pstrPhysDir + '/';
            }
            
            /**
             * Physical filename of the original attachment:
             */
            String strPath = docTpe.getValue("bseDir").getStringValue() + File.separatorChar + getValue("fleNme").getStringValue();
            
            /**
             * Now we derive logical and physical filename for the copy of the original attachment
             * (named 'Attachment<USER>.<EXTENSION>').
             */
            String strFileName = "Attachment" + getZx().getUserProfile().getValue("id").getStringValue();
            
            /**
             * Well Tomcat gives some grief when it comes to caching 
             * so we need to give a unique file name for this to work :(
             */
            strFileName = strFileName + "-" + getValue("id").getStringValue();
            
            String strExtension = extension().toLowerCase();
            if (StringUtil.len(strExtension) > 0) {
                strFileName = strFileName + "." + strExtension;
            }
            
            String strVirtualTempPath = pstrLogDir + strFileName;
            String strPhysicalTempPath = pstrPhysDir + strFileName;
            
            /**
             * Copy the file to a temp area that is a virtual directory
             */
            if (!new File(strPath).exists()) {
                getZx().trace.addError("Uploaded file does not exist", strPath);
            }
            
            /**
             * Load up the physical file of the original attachment:
             */
            copy(strPath, strPhysicalTempPath);
            
            /**
             * Unless the boolean has been set, set the download image anchor so that right clicking it
             *  will allow a download of the attachment. This assumes we are in a popup. If not, the
             *  calling ASP should have set the download boolean to false:
             *  Note that we download the full file, so if a tif is broken up into jpgs later, what is
             *  downloaded is the temp copy of the tif.
             */
            if (pblnDownload) {
                view.append("<script language=\"Javascript\" type=\"text/javascript\">\n");
                view.append("	elementByName(top.fraFooter, 'anchorDownLoad').href='").append(strVirtualTempPath).append("';\n");
                view.append("</script>\n");
            }
            
            if (pblnUseViewer) {
                /**
                 * If explicitly asked to use the viewer, don't format any files just rely on the browser
                 * and any viewers to show the file.
                 */
                view.append("<script language=\"Javascript\" type=\"text/javascript\">\n");
                view.append(" this.document.location = '").append(strVirtualTempPath)
                		.append("?dummy=").append(getValue("id").getStringValue()).append("';\n");
                view.append("</script>\n");
                
            } else {
                if (strExtension.equalsIgnoreCase("gif") || strExtension.equalsIgnoreCase("png") || strExtension.equalsIgnoreCase("jpg")) {
                    /**
                     * We could show these in the same way as the default (case else) behaviour,
                     * but it is better to do it like this and make good use of browser ability
                     * to show such images (some users might have problems with jpg viewers).
                     * DGS31JUL2003: Add a dummy querystring value to make the src URL unique.
                     */
                    view.append("<img src='").append(strVirtualTempPath)
            				.append("?dummy=").append(getValue("id").getStringValue())
            				.append("' title='").append(strPath).append("'").append(">\n");
                    
                } else if (strExtension.equalsIgnoreCase("tif")) {
                    /**
                     * .tif files are a special case because they can have multiple pages and cannot
                     *  be viewed natively in the browser. Need to use a tool to extract the pages,
                     *  convert to .jpg and show.
                     */
                    
                    /**
                     * TODO : We can use the JAI api. However for now fully the ZXDOC is not a hight priority.
                     * 
                            objImg.RegistrationKey = i.zX.configValue("//thbReg")
			                intNumFrames = objImg.FrameCount(strPath)
			        
			                For intFrame = 1 To intNumFrames
			                    objImg.FrameIndex = intFrame - 1
			                    objImg.LoadPictureFromFile (strPath)
			                    
			                    strTIFFilename = strFileName & ".p" & intFrame & "of" & intNumFrames & ".jpg"
			                    strVirtualTempPath = pstrLogDir & strTIFFilename
			                    strPhysicalTempPath = pstrPhysDir & strTIFFilename
			                    
			                    '----
			                    ' These next few settings are necessary to allow a tif to be converted
			                    ' to a jpg.
			                    '----
			                    objImg.ScaleToGray
			                    objImg.ConvertToBPP thbbpp24Bit, thbDitherFS, True
			                    objImg.JPGProgressive = False
			                    objImg.JPGQuality = 100  'Compression quality from 1-100
			                    objImg.JPGGrayscale = False 'Export to 8bit grayscale JPEG
			                    objImg.KeepAspect = True
			                    '----
			                    ' Write a phsyical jpg for this page of the tif:
			                    '----
			                    objImg.SavePictureToFile strPhysicalTempPath, thbifJPG
			                    
			                    '----
			                    ' DGS31JUL2003: Add a dummy querystring value to make the src URL unique. Note that
			                    ' there doesn't seem to be a problem with these jpgs when exported from a tif, but
			                    ' it is harmless to add this and might prevent the problem arising in future.
			                    '----
			                    strReturn = strReturn & "<img width='900'; height='1200' src='" & strVirtualTempPath & "?dummy=" & i.GetAttr("id").strValue & _
			                                    "' title='Page " & intFrame & " of " & intNumFrames & " pages in " & _
			                                    strPath & "'" & _
			                                    "><HR>"
			                    
			                Next
			                
			                Set objImg = Nothing
                     */
                    
                } else {
                    /**
                     * In all other cases rely on the browser and any viewers to show the file.
                     */
                    view.append("<script language=\"Javascript\" type=\"text/javascript\">\n");
                    view.append(" this.document.location = '").append(strVirtualTempPath)
                        .append("?dummy=").append(getValue("id").getStringValue()).append("';\n");
                    view.append("</script>\n");
                    
                }
            }
            
            return view;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : View the document.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pstrLogDir = " + pstrLogDir);
                log.error("Parameter : pstrPhysDir = " + pstrPhysDir);
                log.error("Parameter : pblnUseViewer = " + pblnUseViewer);
                log.error("Parameter : pblnDownload = " + pblnDownload);
            }
            if (getZx().throwException) throw new ZXException(e);
            return view;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(view);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Gets the extension from the filename of the document.
     * 
     * @return Returns the extension of the file.
     * @throws ZXException Thrown if extension fails
     */
    public String extension() throws ZXException {
        String extension = "";

        String strFleNme = getValue("fleNme").getStringValue();

        int intLen = StringUtil.len(strFleNme);
        if (intLen > 0) {
            int intDot = StringUtil.reverse(strFleNme).indexOf('.');
            if (intDot != -1) {
                extension = strFleNme.substring(intLen - intDot, intLen);
            }
        }

        return extension;
    }

    /**
     * Gets the mime type from the extension of the filename of the document.
     * 
     * @return Returns the mime type from the extension.
     */
    public String mimeType() {
        // May want to get the type from the internal mime type.
        return "";
    }

    /**
     * Save file associated with this doc to another place.
     * 
     * @param pstrFileName The file to copy.
     * @throws ZXException Thrown if saveAs fails.
     */
    public void saveAs(String pstrFileName) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        try {
            
            copy(getFullFileName(), pstrFileName);
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Save file associated with this doc to another place.", e);
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Replace 'my' file with the named file.
     * 
     * @param pstrFileName The file to copy
     * @throws ZXException Thrown if replaceFile fails
     */
    public void replaceFile(String pstrFileName) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        try {
            
            copy(pstrFileName, getFullFileName());
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Replace 'my' file with the named file.", e);
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Gets the full filename for the document, including the base path from the related zxDocType
     *
     * @return Returns the full filename for the document.
     * @throws ZXException Thrown if getFullFileName fails. 
     */
    public String getFullFileName() throws ZXException {
        String getFullFileName = "";
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        try {
            /**
             * If not already got this docType, load now (after creating BO if not already created)
             */
            boolean blnGetDocTpe = false;
            if (docTpe == null) {
                blnGetDocTpe = true;
                
                docTpe = (DocTpe)getZx().createBO("zxDocTpe");
                if (docTpe == null) {
                    getZx().trace.addError("Unable to create instance of zxDocTpe");
                    throw new ZXException();
                }
            } else {
                if (!docTpe.getValue("id").getStringValue().equals(getValue("docTpe").getStringValue())) {
                    blnGetDocTpe = true;
                }
            }
            
            if (blnGetDocTpe) {
                docTpe.setValue("id",getValue("docTpe"));
                if (docTpe.loadBO().pos != zXType.rc.rcOK.pos) {
                    getZx().trace.addError("Unable to load zXDocTpe");
                    throw new ZXException();
                }
            }
            
            /**
             * Return the base dir from the doc type prepended to the filename from this doc:
             */
            String strBaseDir = docTpe.getValue("bseDir").getStringValue();
            if (strBaseDir.endsWith(File.separator)) {
                getFullFileName = strBaseDir + getValue("fleNme").getStringValue();
            } else {
                getFullFileName = strBaseDir + File.separatorChar + getValue("fleNme").getStringValue();
            }
            
            return getFullFileName;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Gets the full filename for the document, including the base path from the related zxDocType", e);
            if (getZx().throwException) throw new ZXException(e);
            return getFullFileName;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getFullFileName);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Quick helper method used to copy files :
    
    /**
     * A util method used to copy files.
     * 
     * @param pstrFileFrom The full filename to copy from.
     * @param pstrFileTo The full filename to copy to.
     */
    public static void copy(String pstrFileFrom, String pstrFileTo) {
        try {
        	// NOTE: java.nio.channels.FileChannel is only in JDK 1.4+
        	
            // Create channel on the source
            FileChannel srcChannel = new FileInputStream(pstrFileFrom).getChannel();
            
            // Create channel on the destination
            FileChannel dstChannel = new FileOutputStream(pstrFileTo).getChannel();
            
            // Copy file contents from source to destination
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            
            // Close the channels
            srcChannel.close();
            dstChannel.close();
            
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }
}