/*
 * Created on Jan 13, 2004 by michael
 * $Id: ExprToken.java,v 1.1.2.5 2005/08/30 08:44:11 mike Exp $
 */
package org.zxframework.expression;

import org.zxframework.zXType;
import org.zxframework.util.ToStringBuilder;

/**
 * A token for an expression.
 *
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ExprToken {
    
    //------------------------ Members
    
	private String token;
	private int position;
	private zXType.exprTokenType tokenType;
	private char startCharacter;
	private String functionResult;

    //------------------------ Default constructor

    /**
     * Default constructor, does not need zx.
     */
    public ExprToken() {
        super();
    }
    
    //------------------------ Getters and Setters
	
	/**
	 * @return Returns the position.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position The position to set.
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return Returns the token.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token The token to set.
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return Returns the tokenType.
	 */
	public zXType.exprTokenType getTokenType() {
		return tokenType;
	}

	/**
	 * @param tokenType The tokenType to set.
	 */
	public void setTokenType(zXType.exprTokenType tokenType) {
		this.tokenType = tokenType;
	}
	
    /**
     * @return Returns the functionResult.
     */
    public String getFunctionResult() {
        return functionResult;
    }
    
    /**
     * @param functionResult The functionResult to set.
     */
    public void setFunctionResult(String functionResult) {
        this.functionResult = functionResult;
    }
    
    /**
     * @return Returns the startCharacter.
     */
    public char getStartCharacter() {
        return startCharacter;
    }
    
    /**
     * @param startCharacter The startCharacter to set.
     */
    public void setStartCharacter(char startCharacter) {
        this.startCharacter = startCharacter;
    }
    
    //------------------------ Object overidden methods
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
		toString.append("token", this.token);
        toString.append("tokenType", this.tokenType);
        toString.append("position", this.position);
        if (this.functionResult!= null) {
            toString.append("functionResult", this.functionResult);
        }
        toString.append("startCharacter", this.startCharacter);
        
        return toString.toString();
    }
}