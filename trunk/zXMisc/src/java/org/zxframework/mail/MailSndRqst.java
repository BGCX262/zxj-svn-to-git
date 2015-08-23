/*
 * Created on Jun 5, 2005
 * $Id: MailSndRqst.java,v 1.1.2.2 2005/12/02 10:32:52 mike Exp $
 */
package org.zxframework.mail;

import org.zxframework.ZXBO;

/**
 * The business object for mail send request.
 * Processed by the mail out handler
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class MailSndRqst extends ZXBO {
	
    //------------------------ Members
	
	private String template;
	
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
	public MailSndRqst() {
		super();
	}
	
    //------------------------ Getters/Setters
	
	/**
	 * @return Returns the template.
	 */
	public String getTemplate() {
		return template;
	}
	
	/**
	 * @param template The template to set.
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
}