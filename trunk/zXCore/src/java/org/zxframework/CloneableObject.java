package org.zxframework;

/**
 * Used as an indicator that the object implements clonable and also
 * exposes the clone method so that it can be called generically.
 * 
 * This is used by out despoke ZXCollection object.
 * 
 * When should i implement this interface?
 * 
 * You only have to implement this when this object is being stored in the ZXCollection
 * and you want to automatically call the clone method on this object doing cloning 
 * of that collection.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public interface CloneableObject extends Cloneable {
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone();
}