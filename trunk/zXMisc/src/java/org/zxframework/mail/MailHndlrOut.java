/*
 * Created on Jun 6, 2005
 * $Id: MailHndlrOut.java,v 1.1.2.1 2005/06/06 10:57:38 mike Exp $
 */
package org.zxframework.mail;

import org.zxframework.zXType;

/**
 * Interface definition for outgoing mail handler plugin.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public interface MailHndlrOut {
	
	/**
	 * Initialise the mail handler plugin.
	 * 
	 * @param pobjMailServer Handle to initialise mail server object
	 * @return Returns the return code of the method.
	 */
	public zXType.rc initProcess(MAPI pobjMailServer);
	
	/**
	 * Process a send request
	 * 
	 * @param pobjMailSndRqst Handle to the mail send request.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc process(MailSndRqst pobjMailSndRqst);
	
	/**
	 * Gently term of the mail handler plugin.
	 * 
	 * @return Returns the return code of the method.
	 */
	public zXType.rc termProcess();
	
}