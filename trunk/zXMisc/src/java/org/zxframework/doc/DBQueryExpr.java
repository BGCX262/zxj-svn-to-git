/*
 * Created on Jun 9, 2005
 * $Id: DBQueryExpr.java,v 1.1.2.6 2006/07/17 16:10:54 mike Exp $
 */
package org.zxframework.doc;

import org.zxframework.ZXObject;
import org.zxframework.zXType;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * The doc builder query expression (used by clsDBQuery)
 * 
 * </pre>
 *  
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBQueryExpr extends ZXObject {
	
	private static Log log = LogFactory.getLog(DBQueryExpr.class);
	
	//------------------------ Members
	
	private String lhs;
	private String rhs;
	private String attrlhs;
	private String attrrhs;
	private DBQueryExpr qryexprlhs;
	private DBQueryExpr qryexprrhs;
	private zXType.pageflowQueryExprOperator opeRator;
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public DBQueryExpr() {
		super();
	}
	
	//------------------------ Getters/Setterss
	
	/**
	 * @return Returns the attrlhs.
	 */
	public String getAttrlhs() {
		return attrlhs;
	}
	
	/**
	 * @param attrlhs The attrlhs to set.
	 */
	public void setAttrlhs(String attrlhs) {
		this.attrlhs = attrlhs;
	}
	
	/**
	 * @return Returns the attrrhs.
	 */
	public String getAttrrhs() {
		return attrrhs;
	}
	
	/**
	 * @param attrrhs The attrrhs to set.
	 */
	public void setAttrrhs(String attrrhs) {
		this.attrrhs = attrrhs;
	}
	
	/**
	 * @return Returns the lhs.
	 */
	public String getLhs() {
		return lhs;
	}
	
	/**
	 * @param lhs The lhs to set.
	 */
	public void setLhs(String lhs) {
		this.lhs = lhs;
	}
	
	/**
	 * @return Returns the opeRator.
	 */
	public zXType.pageflowQueryExprOperator getOpeRator() {
		return opeRator;
	}
	
	/**
	 * @param opeRator The opeRator to set.
	 */
	public void setOpeRator(zXType.pageflowQueryExprOperator opeRator) {
		this.opeRator = opeRator;
	}

	/**
	 * @return Returns the qryexprlhs.
	 */
	public DBQueryExpr getQryexprlhs() {
		return qryexprlhs;
	}
	
	/**
	 * @param qryexprlhs The qryexprlhs to set.
	 */
	public void setQryexprlhs(DBQueryExpr qryexprlhs) {
		this.qryexprlhs = qryexprlhs;
	}
	
	/**
	 * @return Returns the qryexprrhs.
	 */
	public DBQueryExpr getQryexprrhs() {
		return qryexprrhs;
	}
	
	/**
	 * @param qryexprrhs The qryexprrhs to set.
	 */
	public void setQryexprrhs(DBQueryExpr qryexprrhs) {
		this.qryexprrhs = qryexprrhs;
	}
	
	/**
	 * @return Returns the rhs.
	 */
	public String getRhs() {
		return rhs;
	}
	
	/**
	 * @param rhs The rhs to set.
	 */
	public void setRhs(String rhs) {
		this.rhs = rhs;
	}
	
	//--------------------- Digester helper method
	
	/**
	 * @param opeRator The opeRator to set.
	 */
	public void setOperator(String opeRator) {
		this.opeRator = zXType.pageflowQueryExprOperator.getEnum(opeRator);
	}
	
	//--------------------- Util methods
	
	/**
	 * @return Returns whether the operator is recursive.
	 */
	public boolean isOperatorRecursive() {
		return (this.opeRator.equals(zXType.pageflowQueryExprOperator.pqeoAND) 
				|| this.opeRator.equals(zXType.pageflowQueryExprOperator.pqeoOR));
	}
	
	//------------------------ Object implemeted methods
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		DBQueryExpr objDBQueryExpr = null;
		
		try {
			objDBQueryExpr = new DBQueryExpr();
			objDBQueryExpr.setLhs(getLhs());
			objDBQueryExpr.setRhs(getRhs());
			objDBQueryExpr.setAttrlhs(getAttrlhs());
			objDBQueryExpr.setAttrrhs(getAttrrhs());
			
			if (getQryexprlhs() != null) {
				objDBQueryExpr.setQryexprlhs((DBQueryExpr)getQryexprlhs().clone());
			}
			
			if (getQryexprrhs() != null) {
				objDBQueryExpr.setQryexprrhs((DBQueryExpr)getQryexprrhs().clone());
			}

			objDBQueryExpr.setOpeRator(getOpeRator());
			
        } catch (Exception e) {
            log.error("Failed to clone object", e);
        }
        
        return objDBQueryExpr;
	}
}