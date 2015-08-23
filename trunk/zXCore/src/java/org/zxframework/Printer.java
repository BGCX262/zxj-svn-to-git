/*
 * Created on Mar 1, 2004
 * $Id: Printer.java,v 1.1.2.4 2005/07/12 14:06:02 mike Exp $
 */
package org.zxframework;

/**
 * The zX printer definition.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class Printer extends ZXBO {
    
    //------------------------ Members
    
    //------------------------ Constructors        
    
    /**
     * Default constructor.
     */
    public Printer() {
        super();
    }    
        
    /** 
     * Initialise a business object
     * 
     * @see org.zxframework.ZXBO#init(Descriptor)
     **/
    public void init(Descriptor pobjDesc) {
        
        // Init zx stuff first : 
        super.init(pobjDesc);
        
        /**
         * It may be that we have to populate the list of available printers
         */
    }
    
    //------------------------ Public Methods

    /**
     * Test the current printer.
     * 
     * @return The return code of the method.
     * @throws ZXException Thrown if testPrinter fails
     */
    public zXType.rc testPrinter() throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
        
        zXType.rc testPrinter = zXType.rc.rcOK;
		
		try {
		    
		    /**
		     * NOTE: Not needed, but it would be good to have something like this
		     * 
		           Set objWord = i.zX.word
    
				    If objWord.setPrinter(i.getAttr("prntr").strValue) <> rcOk Then
				        i.zX.trace.userErrorAdd "Unable to set printer", _
				            Err.description
				        GoTo errExit
				    End If
				    
				    If objWord.newDoc(vbNullString) <> rcOk Then
				        i.zX.trace.userErrorAdd "Unable to open document", _
				            Err.description
				        GoTo errExit
				    End If
				    
				    objWord.wordDoc.Range.InsertAfter "TEST PRINT"
				    
				    objWord.wordDoc.PrintOut
				
				    objWord.closeDoc
			 *
		     */
		    
			return testPrinter;
			
		} catch (Exception e) {
		    if (getZx().log.isErrorEnabled()) {
		    	getZx().trace.addError("Failed to : Test the current printer", e);
		    }
		    
		    if (getZx().throwException) throw new ZXException(e);
		    testPrinter = zXType.rc.rcError;
		    return testPrinter;
		} finally {
		    if(getZx().trace.isFrameworkTraceEnabled()) {
		        getZx().trace.exitMethod();
		    }
		}
    }
}