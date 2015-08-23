/*
 * Created on Jan 12, 2004 by michael
 * $Id: ErrorEntry.java,v 1.1.2.9 2005/07/13 07:12:41 mike Exp $
 */
package org.zxframework;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.zxframework.util.LocationInfo;
import org.zxframework.util.StringUtil;

/**
 * A bean representing an Error.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 28 August 2002
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class ErrorEntry extends ZXObject {

    //------------------------ Members
    
    private String description = "";
    private String additionalInfo = "";
    private Exception exception;
    
    private LocationInfo loc;
    
    private StringBuffer formattedString;
    
    //------------------------ Constructors
    
    /**
     * Hide the default constructor 
     */
    private ErrorEntry() { super(); }
    
    /**
     * Main constructor for an error
     * 
     * @param loc The location that the error accurred.
     * @param description The description of the error.
     * @param additionalInfo Extra info. Like some variables
     * @param e Exception that triggered the error
     */
    public ErrorEntry(LocationInfo loc, String description, String additionalInfo, Exception e) {
        super();
        this.loc = loc;
        
        this.description = description;
        this.additionalInfo = additionalInfo;
        this.exception = e;
    }

    //------------------------ Getters and Setters.
    
    /**
     * @return Returns the exception.
     */
    public Exception getException() {
        return exception;
    }
    
    /**
     * @param exception The exception to set.
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }
    
    /**
     * @return Returns the additionalInfo.
     */
    public String getAdditionalInfo() {
        return this.additionalInfo;
    }

    /**
     * @param additionalInfo The additionalInfo to set.
     */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Format me in human readable form
     * 
     * @return Returns a formatted string of the error
     * @see ErrorEntry#format(boolean)
     */
    public String format() {
        return format(true);
    }

    /**
     * Format me in human readable form
     * 
     * @param pblnFull Whether or not to display the full message.
     * @return Returns a formatted string of the error.
     */
    public String format(boolean pblnFull) {
//        if (this.formattedString != null) {
//            return this.formattedString.toString();
//        }
        
        this.formattedString = new StringBuffer();
        
        if (pblnFull) {
            boolean blnIsProd = getZx().getRunMode().pos == zXType.runMode.rmProduction.pos;
            
            if (!blnIsProd) {
            	// Print out where the error accured.
	            this.formattedString.append(this.loc.getClassName()).append('.').append(this.loc.getMethodName())
						            .append(" (line ").append(this.loc.getLineNumber()).append("): ");
            }
            
		    this.formattedString.append(getDescription());
            
            // Is there any additional information to print ?
            if (StringUtil.len(this.additionalInfo) > 0) {
                boolean blnWrap = this.formattedString.length() != 0;
                if (blnWrap) {
                    this.formattedString.append(" (");
                }
                this.formattedString.append(this.additionalInfo);
                if (blnWrap) {
                    this.formattedString.append(")");
                }
                
            }
            
            // Print out a verbose error message.
            if (!blnIsProd) {
                // Is there an exception and are we allowed to show it?
                boolean blnPrintException = this.exception != null;
                if (blnPrintException) {
                    if (this.exception instanceof ZXException) {
                        blnPrintException = false;
                    } else {
                        // The cause can NOT be an ZXException
                        blnPrintException = !(this.exception.getCause() instanceof ZXException);
                        if (blnPrintException) {
                            // The cause can NOT be an ZXException
                            if (this.exception.getCause() != null) {
                                blnPrintException = !(this.exception.getCause().getCause() instanceof ZXException);
                                if (this.exception.getCause().getCause() != null) {
                                    blnPrintException = !(this.exception.getCause().getCause().getCause() instanceof ZXException);
                                }
                            }
                        }
                    }
                }
                
                // Are we allowed to print this exception?
                if (blnPrintException) {
                    this.formattedString.append("\n\r ---PRINTSTACKTRACE \n\r");
                    
                    /**
                     * Print the full stack trace.
                     */
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    this.exception.printStackTrace(pw);
                    
                    this.formattedString.append(sw.toString());
                    this.formattedString.append(" ---PRINTSTACKTRACE \n\r\n\r");
                }
                
            }
            
        } else {
            // Only print out the error message.
            this.formattedString.append(getDescription());
        }
        
        return this.formattedString.toString();
    }
}