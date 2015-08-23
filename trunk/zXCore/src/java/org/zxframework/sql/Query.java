/*
 * Created on July 22, 2005
 * $Id: Query.java,v 1.1.2.2 2006/07/17 16:40:06 mike Exp $
 */
package org.zxframework.sql;

import java.util.ArrayList;

/**
 * SQL statement we are preparing.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class Query {
	
	//------------------------ Members
	
	private StringBuffer sql;
	private ArrayList values;
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public Query() {
		values = new ArrayList();
	}
	
	//------------------------ Getters & Setters
	
	/**
	 * The sql statement
	 * 
	 * @return Returns the sql.
	 */
	public StringBuffer getSql() {
		return sql;
	}
	
	/**
	 * @param sql The sql to set.
	 */
	public void setSql(StringBuffer sql) {
		this.sql = sql;
	}
	
	/**
	 * The statement values.
	 * 
	 * @return Returns the values.
	 */
	public ArrayList getValues() {
		return values;
	}
	
	/**
	 * @param values The values to set.
	 */
	public void setValues(ArrayList values) {
		this.values = values;
	}
	
	//------------------------ Object overidden methods
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (this.sql != null) {
			return this.sql.toString();
		}
		
		return super.toString();
	}
}