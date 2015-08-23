/*
 * Created on 21-Feb-2005 by Michael Brewer
 * $Id: ExpressionParseException.java,v 1.1.2.5 2006/07/17 16:40:43 mike Exp $
 */
package org.zxframework.expression;

import org.zxframework.exception.NestableRuntimeException;

/**
 * Exceptions caused by a expression parsing error. This is used instead of
 * calling parseError like in the C1.4.
 * 
 * @author Michael Brewer
 */
public class ExpressionParseException extends NestableRuntimeException {
    
    private String parseErrorToken;
    private int parseErrorPosition;
    
    /**
     * Hide default constructor.
     */
    private ExpressionParseException() { 
    	super();
    }
    
    /**
     * @param e The cause of the exception.
     */
    public ExpressionParseException(Throwable e) { 
    	super(e);
    }
        
    /**
     * @param parseMsg The parse error message.
     */
    public ExpressionParseException(String parseMsg) {
        super(parseMsg);
    }
    
    /**
     * Constructs a ExpressionParseException.
     * 
     * @param parseMsg The parse error message.
     * @param parseErrorToken The token you we trying to parse
     * @param parseErrorPosition The position you where in.
     */
    public ExpressionParseException(String parseMsg, 
                            String parseErrorToken,
                            int parseErrorPosition) {
        super(parseMsg); // Set the exceptions general message.
        this.parseErrorToken = parseErrorToken;
        this.parseErrorPosition = parseErrorPosition;
    }
    
    /**
     * The position in the expression where the error accurred.
     * 
     * @return Returns the parseErrorPosition.
     */
    public int getParseErrorPosition() {
        return parseErrorPosition;
    }

    /**
     * The token we where trying to parse last.
     * 
     * @return Returns the parseErrorToken.
     */
    public String getParseErrorToken() {
        return parseErrorToken;
    }
}
