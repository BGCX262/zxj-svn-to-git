/*
 * Created on Jun 6, 2005
 * $Id: MailHndlrIn.java,v 1.1.2.1 2005/06/06 10:57:36 mike Exp $
 */
package org.zxframework.mail;

import org.zxframework.zXType;

/**
 * Interface definition for incoming mail handler plugin.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public interface MailHndlrIn {
	
	/**
	 * Initialise the mail handler plugin.
	 * 
	 * @param pobjMailServer Handle to initialise mail server object
	 * @return Returns the return code of the method.
	 */
	public zXType.rc initProcess(MAPI pobjMailServer);
	
	/**
	 * A new mail has been found, process it.
	 * 
	 * Assumes : The current message will have been set
	 * 
	 * @param pobjMail The mail to proccess.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc process(MAPIMail pobjMail);
	
	/**
	 * Gently term of the mail handler plugin.
	 * 
	 * @return Returns the return code of the method.
	 */
	public zXType.rc termProcess();
	
	/**
	 * An exception has been found, handle it.
	 * 
	 * Assumes   : The current message will have been set
	 * 
	 * @param pobjMail The mail to send
	 * @return Returns the return code of the method.
	 */
	public zXType.rc handleException(MAPIMail pobjMail);
	
}