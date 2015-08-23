package org.zxframework;

/**
 * This interface indicates that this object has a unique indentifier in a collection.
 * 
 * When should you implment this interface?
 * 
 * You only have to implement this interface if you are using ZXCollection to store
 * this object in a collection and you are using Digester to populate this collection from
 * an xml configuration file.
 * 
 * If your object extends ZXObject it does this automatically for you.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public interface KeyedObject {
	
	/**
	 * Returns the key of the object.
	 * This is used by ZXCollection to autodetect
	 * the key of objects put in the collection from Digester.
	 * 
	 * NOTE: You should return the key property of this object.
	 * 
	 * @return Returns the key of this object in a collection
	 */
	public String getKey();
}