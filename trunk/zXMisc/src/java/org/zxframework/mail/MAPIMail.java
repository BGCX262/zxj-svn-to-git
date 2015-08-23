/*
 * Created on Jun 6, 2005
 * $Id: MAPIMail.java,v 1.1.2.1 2005/06/06 10:57:37 mike Exp $
 */
package org.zxframework.mail;

import java.util.Map;

import org.zxframework.zXType;

/**
 * Object that represents a single mail.
 * 
 * <pre>
 * 
 * Change    : DGS07FEB2003
 * Why       : Can now also save the body of the e-mail as an attachment
 *             Also holds the status of the mail after it has been obtained
 *             but before it is processed.
 * 
 * Change    : DGS31JUL2003
 * Why       : New function to check for situations that will result in an alert
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class MAPIMail {
	
	//------------------ Members
	
	private MAPI mapi;
	private zXType.rc mailStatus;
	private String subject;
	private String sender;
	private String receiver;
	private String cc;
	private String bcc;
	private String body;
	private Map attachments;
	
	//------------------------- Getters/Setters
	
	/**
	 * @return Returns the attachments.
	 */
	public Map getAttachments() {
		return attachments;
	}
	/**
	 * @param attachments The attachments to set.
	 */
	public void setAttachments(Map attachments) {
		this.attachments = attachments;
	}
	/**
	 * @return Returns the bcc.
	 */
	public String getBcc() {
		return bcc;
	}
	/**
	 * @param bcc The bcc to set.
	 */
	public void setBcc(String bcc) {
		this.bcc = bcc;
	}
	/**
	 * @return Returns the body.
	 */
	public String getBody() {
		return body;
	}
	/**
	 * @param body The body to set.
	 */
	public void setBody(String body) {
		this.body = body;
	}
	/**
	 * @return Returns the cc.
	 */
	public String getCc() {
		return cc;
	}
	/**
	 * @param cc The cc to set.
	 */
	public void setCc(String cc) {
		this.cc = cc;
	}
	/**
	 * @return Returns the mailStatus.
	 */
	public zXType.rc getMailStatus() {
		return mailStatus;
	}
	/**
	 * @param mailStatus The mailStatus to set.
	 */
	public void setMailStatus(zXType.rc mailStatus) {
		this.mailStatus = mailStatus;
	}
	/**
	 * @return Returns the mapi.
	 */
	public MAPI getMapi() {
		return mapi;
	}
	/**
	 * @param mapi The mapi to set.
	 */
	public void setMapi(MAPI mapi) {
		this.mapi = mapi;
	}
	/**
	 * @return Returns the receiver.
	 */
	public String getReceiver() {
		return receiver;
	}
	/**
	 * @param receiver The receiver to set.
	 */
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	/**
	 * @return Returns the sender.
	 */
	public String getSender() {
		return sender;
	}
	/**
	 * @param sender The sender to set.
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}
	/**
	 * @return Returns the subject.
	 */
	public String getSubject() {
		return subject;
	}
	/**
	 * @param subject The subject to set.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	//---------------------------- Abstract methods.
	
	/**
	 * Add attachment to a mail.
	 * 
	 * @param pstrFileName The file to attach
	 * @return Returns the return code of the method.
	 */
	public abstract zXType.rc addAttachment(String pstrFileName);
	
	/**
	 * Add body as attachment to a mail.
	 * 
	 * @param pstrFileName File attachment.
	 * @return Returns the return code of the method.
	 */
	public abstract zXType.rc addBodyAsAttachment(String pstrFileName);
	
	/**
	 * Check for any email type-specific alerts.
	 * 
	 * @return Returns the alerts.
	 */
	public abstract String checkForAlerts();
	
}