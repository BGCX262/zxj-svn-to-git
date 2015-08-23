/*
 * Created on Jun 6, 2005
 * $Id: MAPI.java,v 1.1.2.1 2005/06/06 10:57:37 mike Exp $
 */
package org.zxframework.mail;

import org.zxframework.zXType;

/**
 * Interface definition for zX MAPI implementations.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public interface MAPI {
	
	/**
	 * Connect to mail subsystem.
	 * 
	 * @param pstrProfile The user to connect as
	 * @param pstrPassword The password
	 * @param pstrServer The server name to connect to. 
	 * 					 Optional, default should be null.
	 * @return Returns whether connect was successfull.
	 */
	public zXType.rc connect(String pstrProfile, String pstrPassword, String pstrServer);
	
	/**
	 * Make a folder the active folder.
	 * 
	 * @param pstrFolder The folder to set.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc setFolder(String pstrFolder);
	
	/**
	 * Disconnect from the mail system.
	 * 
	 * @return Returns the return code of the method.
	 */
	public zXType.rc disconnect();
	
	/**
	 * Get next mail from current folder; returns nothing if no mails awaiting.
	 * 
	 * @return Return the next mail message. Null if no more mails.
	 */
	public MAPIMail nextMail();
	
	/**
	 * Send a file.
	 * 
	 * @param pobjMail The mail to send.
	 * @param pstrFolder Optional, default should be null.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc send(MAPIMail pobjMail, String pstrFolder);
	
	/**
	 * Move mail to a folder.
	 * 
	 * @param pobjMail The mail to move.
	 * @param pstrFolder Folder to move mail to. Optional, default should be null.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc moveTo(MAPIMail pobjMail, String pstrFolder);
	
	/**
	 * Set processing parameters.
	 * 
	 * @param pstrParameter The name of the parameter to set.
	 * @param pstrValue The value.
	 * @return Returns the return code of the method.
	 */
	public zXType.rc setParam(String pstrParameter, String pstrValue);
	
	/**
	 * Create a new mail instance (ready to send).
	 * 
	 * @return Returns a new mail message.
	 */
	public MAPIMail newMessage();
	
}