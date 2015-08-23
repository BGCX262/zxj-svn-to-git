package org.zxframework.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zxframework.CloneableObject;
import org.zxframework.ZXCollection;
import org.zxframework.exception.NestableRuntimeException;

/**
 * Cloning Util stuff.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class CloneUtil {
	
	/**
	 * @param pcol The collection to clone
	 * @return Return a cloned collection.
	 */
	public Map clone(Map pcol) {
		if (pcol == null) return null;
		
		try {
			Map clone = (Map)pcol.getClass().newInstance();
			
	        // Performance tweak.
	        boolean isCloneableObject = false;
	        
	        Object objKey;
	        Object objValue;
	        
	        Iterator iterKeys = pcol.keySet().iterator();
	        while (iterKeys.hasNext()) {
	            objKey = iterKeys.next();
	            objValue = pcol.get(objKey);
	            
	            if (isCloneableObject || objValue instanceof CloneableObject) {
	                objValue = ((CloneableObject)objValue).clone();
	                isCloneableObject = true;
	                
	            } else {
	                // Maybe the object implements Clonable?
	            }
	            
	            clone.put(objKey, objValue);
	        }
			
			return clone;
		} catch (Exception e) {
			throw new NestableRuntimeException("Failed to clone object", e);
		}
	}
	
	/**
	 * @param pcol The collection to clone
	 * @return Return a cloned collection.
	 */
	public List clone(List pcol) {
		if (pcol == null) return null;
		
		try {
			List clone = (List)pcol.getClass().newInstance();
			
			int intColSize = pcol.size();
	        for (int i = 0; i < intColSize; i++) {
	            clone.add(((CloneableObject)pcol.get(i)).clone());
			}
	        
			return clone;
		} catch (Exception e) {
			throw new NestableRuntimeException("Failed to clone object", e);
		}
	}
	
	/**
	 * @param pcol The collection to clone
	 * @return Return a cloned collection.
	 */
	public static ZXCollection clone(ZXCollection pcol) {
		if (pcol == null) return null;
		
		try {
			ZXCollection clone = new ZXCollection(pcol.size());
			clone.setKey(pcol.getKey());
			
	        // Performance tweak.
	        boolean isCloneableObject = false;
	        
	        Object objKey;
	        Object objValue;
	        
	        Iterator iterKeys = pcol.keySet().iterator();
	        while (iterKeys.hasNext()) {
	            objKey = iterKeys.next();
	            objValue = pcol.get(objKey);
	            
	            if (isCloneableObject || objValue instanceof CloneableObject) {
	                objValue = ((CloneableObject)objValue).clone();
	                isCloneableObject = true;
	                
	            } else {
	                // Maybe the object implements Clonable?
	            }
	            
	            clone.put(objKey, objValue);
	        }
			
			return clone;
		} catch (Exception e) {
			throw new NestableRuntimeException("Failed to clone object", e);
		}
	}
	
	/**
	 * Clone collection of CloneableObjects.
	 * 
	 * <pre>
	 * 
	 * Calling pcol.get() is less performant way of getting a handle
	 * to an entry but less likely to cause concurrency problems than calling
	 * iter.next().
	 * </pre>
	 * 
	 * @param pcol The collection to clone
	 * @return Return a cloned collection.
	 */
	public HashMap clone(HashMap pcol) {
		if (pcol == null) return null;
		
		HashMap clone = new HashMap(pcol.size());
        Object objKey;
        Iterator iterKeys = pcol.keySet().iterator();
        while (iterKeys.hasNext()) {
            objKey = iterKeys.next();
            // Could be clone.put(iterKeys.next(), ((CloneableObject)iterValues.next()).clone()); see method comment.
            clone.put(objKey, ((CloneableObject)pcol.get(objKey)).clone());
        }
		return clone;
	}
	
	/**
	 * @param pcol The collection to clone
	 * @return Return a cloned collection.
	 */
	public static ArrayList clone(ArrayList pcol) {
		if (pcol == null) return null;
		int intColSize = pcol.size();
		ArrayList clone = new ArrayList(intColSize);
        for (int i = 0; i < intColSize; i++) {
            clone.add(((CloneableObject)pcol.get(i)).clone());
		}
		return clone;
	}
	
	/**
	 * @param pobj Object to clone
	 * @return Return a cloned object.
	 */
	public static Object clone(CloneableObject pobj) {
		if (pobj == null) return null;
		return pobj.clone();
	}
}