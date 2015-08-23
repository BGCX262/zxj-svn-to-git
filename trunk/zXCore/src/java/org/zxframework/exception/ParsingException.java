package org.zxframework.exception;

/**
 * Parsing exception which usually accurs in the parsing of the xml descriptors.
 * This is an unchecked exception as if there is a parsing error then there is
 * normally nothing we can do about it.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since J1.5
 * @version 0.0.1
 */
public class ParsingException extends NestableRuntimeException {
	
	//------------------------ Constructors
	
	/**
	 * Default contructor.
	 */
	public ParsingException() {
		super();
	}

	/**
	 * @param msg The exception message
	 * @param cause The cause of the exception.
	 */
	public ParsingException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @param msg The exception message
	 */
	public ParsingException(String msg) {
		super(msg);
	}

	/**
	 * @param cause The cause of the exception.
	 */
	public ParsingException(Throwable cause) {
		super(cause);
	}
}