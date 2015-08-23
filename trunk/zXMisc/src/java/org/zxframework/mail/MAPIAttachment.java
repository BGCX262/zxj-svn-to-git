/*
 * Created on Jun 6, 2005
 * $Id: MAPIAttachment.java,v 1.1.2.1 2005/06/06 10:57:37 mike Exp $
 */
package org.zxframework.mail;

import org.zxframework.zXType;

/**
 * Attachement interface.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class MAPIAttachment {
	
	//---------------- Members
	
	private String filename;
	private long size;
	private boolean body;
	
	//---------------- Getters/Setters
	
	/**
	 * @return Returns the body.
	 */
	public boolean isBody() {
		return body;
	}
	
	/**
	 * @param body The body to set.
	 */
	public void setBody(boolean body) {
		this.body = body;
	}
	
	/**
	 * @return Returns the filename.
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * @param filename The filename to set.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	/**
	 * @return Returns the size.
	 */
	public long getSize() {
		return size;
	}
	
	/**
	 * @param size The size to set.
	 */
	public void setSize(long size) {
		this.size = size;
	}
	
	//---------------------- Abstract methods
	
	/**
	 * Save attachment.
	 * 
	 * @param pstrFileName The filename to save to.
	 * @return Returns the return code of the method.
	 */
	public abstract zXType.rc saveAs(String pstrFileName);
}