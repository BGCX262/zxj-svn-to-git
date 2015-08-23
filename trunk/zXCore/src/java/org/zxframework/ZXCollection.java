/*
 * Created on Jan 15, 2004 by michael
 */
package org.zxframework;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.StringUtil;

//import com.vladium.utils.timing.ITimer;
//import com.vladium.utils.timing.TimerFactory;

/**
 * The superclass of the ZX Collection objects.
 * 
 * <p>This needs to used by AttributeCollection ColAtributeGroups and
 * LabelCollection etc..</p>
 * 
 * <pre>
 * NOTE : Very important. We need to be able to mimic the behaviour of the VB collection 
 * 1) Add to a collection without the key
 * 2) Be able to iterator through the object in the same order as when you put them in
 * 3) Key have to be case insensitive
 * 4) Not allow multiple keys
 * 5) Allow for keys etc.
 * </pre>
 * 
 * <p>The collection container is a LinkedHashmap as it keeps it orders and it can
 * save values with a key.</p>
 * 
 * <img src="../../doc-files/zxcollection.png">
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ZXCollection extends ZXObject implements Map {
    
    //--------------------------------------------------------------- Members    
    
    /**
     * This is the primary storage for all of the zXCollections.
     * 
     * NOTE : I need to hide this from the subclasses so that we can change this to a more 
     * appropiate class
     */
    protected Map collection;
    
    //---------------------------------------------------------------- Constructors    
    
    /**
     * Strandard default constructor. This class does not need any tracing, it is only a wrapper to
     * LinkedHashMap.
     */
    public ZXCollection() {
        super();
        
        /** Always init a collection. */
        this.collection = new LinkedHashMap();
    }
    
    /**
     * Use this if you know the expected size of the collection.
     * 
     * @see LinkedHashMap#LinkedHashMap(int)
     * @param  initialCapacity the initial capacity.
     */
    public ZXCollection(int initialCapacity) {
        super();
        /** Always init a collection. */
        this.collection = new LinkedHashMap(initialCapacity);
    }

    //--------------------------------------------------------- Public methods
    
    /**
     * Returns a object in the collection by the key
     * 
     * @see LinkedHashMap#get(java.lang.Object)
     * @param pobjKey The key of the collection.
     * @return An object.
     */
    public Object get(Object pobjKey) {
        return this.collection.get(pobjKey);
    }
    
    /**
     * They to get a Object from the Map by the position it was put in
     * 
     * @deprecated Rather use an ArrayList
     * @param pintKey The number of the element you want to return.
     * @return Returns a object from the Collection by is position.
     */
    private Object get(int pintKey) {
        Object get = null;
        int i = 0;
        
        Iterator iter = iterator();
        while(iter.hasNext()) {
            get = iter.next();
            
            if(pintKey == i)  break;
            i++;
        }
        
        return get;
    }
    
    /**
     * Special checked put method that throws an Exception if the object that
     * you are trying to add already exists
     * 
     * @param pobjKey The key of the collection
     * @param pobjValue The object to store.
     * @throws RuntimeException
     *                  If the key already exists in the HashMap the exception will
     *                  be thrown.
     */
    public void checkPut(Object pobjKey, Object pobjValue) throws RuntimeException {
        pobjKey = clean(pobjKey);
        
        if (containsKey(pobjKey)) {
            throw new RuntimeException("Duplicate key." + pobjKey);
        }
        
        this.collection.put(pobjKey, pobjValue);
    }

    /**
     * This is the more generic put method for objects that do not extend
     * ZXObject.
     * 
     * @see LinkedHashMap#put(java.lang.Object, java.lang.Object)
     * @param pobjKey The key of the collection, for a ZXObject type object the key attribute can be used.
     * @param pobjValue The value that you want to set.
     * @return Returns previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key, if the implementation supports 
     */
    public Object put(Object pobjKey, Object pobjValue) {
        return this.collection.put(pobjKey, pobjValue);
    }
    
    /**
     * <pre>
     * 
     * All object that inherit from ZXObject can use this helper method. You
     * must remember to set the key for the object of generate one. Also if
     * there is an existing object with the same key in the collection. it will
     * be overriden
     * 
     * NOTE : This is a temp hack for case insensitivety.
     * VB is AMAZING, and its keys in collections are CASE-INsensitive.
     * CASE INSENSITIVE - Keys of the pageflow actions are lowercase.
     * FOR NOW ONLY. Bertus and Dave need to agreee on some.
     * NOTE : Being case SENSITIVE will be faster.
     * </pre>
     * 
     * @param pobjKeyedObject The keyed object to add.
     */
    public void putCASEINSENSITIVE(KeyedObject pobjKeyedObject) {
        String strKey = pobjKeyedObject.getKey();
        if (StringUtil.len(strKey) == 0) {
            add(pobjKeyedObject);
            getZx().log.error("Key should not be null");
            
        } else {
            put(strKey.toLowerCase(), pobjKeyedObject);
        }
    }
    
    /**
     * Returns a object in the collection by the key
     * 
     * @see LinkedHashMap#get(java.lang.Object)
     * @param pobjKey The key of the collection.
     * @return An object.
     */
    public Object getCASEINSENSITIVE(String pobjKey) {
        return this.collection.get(pobjKey.toLowerCase());
    }
    
    /**
     * Special checked put method that throws an Exception if the object that
     * you are trying to add already exists
     * 
     * @param pobjKeyedObject The keyed object to store.
     * @throws RuntimeException If the key already exists in the HashMap the exception will
     *          be thrown.
     */
    private void checkPut(KeyedObject pobjKeyedObject) throws RuntimeException {
        checkPut(pobjKeyedObject.getKey(), pobjKeyedObject);
    }

    /**
     * If it is a String key, then it will trim it etc..
     * 
     * @param pobjDirty
     *                 The object you want to clean.
     * @return Returns a clean object.
     */
    public Object clean(Object pobjDirty) {
        return pobjDirty instanceof String ? ((String) pobjDirty).toLowerCase().trim() : pobjDirty;
    }


    /**
     * Is the collecion empty
     * 
     * @return True if the collection is empty.
     */
    public boolean isEmpty() {
        return this.collection.isEmpty();
    }

    /**
     * Get a iterator, this allow you to loop through collection.
     * 
     * @return Returns a iterator to loop through
     */
    public Iterator iterator() {
        return this.collection.values().iterator();
    }

    /**
     * @see LinkedHashMap#clear()
     */
    public void clear() {
        getLinkedHashmapCollection().clear();
    }

    /**
     * @see LinkedHashMap#containsKey(java.lang.Object)
     * @param pobjKey
     *                 Key to check
     * @return True if the collection contains such a key
     */
    public boolean containsKey(Object pobjKey) {
        return this.collection.containsKey(pobjKey);
    }

    /**
     * @see LinkedHashMap#containsValue(java.lang.Object)
     * @param value
     *                 Value to check
     * @return True if the collection contains such a value.
     */
    public boolean containsValue(Object value) {
        return this.collection.containsValue(value);
    }

    /**
     * A quick way to copy to collections.
     * 
     * @see LinkedHashMap#putAll(java.util.Map)
     * @param t
     *                 A Map to put into the collection
     */
    public void putAll(Map t) {
    	if (t instanceof ZXCollection && t != null) {
			/**
			 * Prevent a class 
			 */
			this.collection.putAll(((ZXCollection)t).collection);
			
    	} else {
            this.collection.putAll(t);
    	}
    }

    /**
     * @see LinkedHashMap#size()
     * @return The size of the collection
     */
    public int size() {
        return this.collection.size();
    }
    
    /**
     * For the VB developers :).
     * @see LinkedHashMap#size()
     * @return The size of the collection
     */
    public int count() {
        return this.collection.size();
    }

    /**
     * Remove object for the collection by the key.
     * 
     * @see LinkedHashMap#remove(java.lang.Object)
     * @param pobjKey
     *                 The key to select which object to remove.
     * @return The object that is going to be removed, null if none is being
     *             removed.
     */
    public Object remove(Object pobjKey) {
        return this.collection.remove(pobjKey);
    }
    
    /**
     * Remove object for the collection by its postion.
     * 
     * <pre>
     * 
     * NOTE :  This has performance issues.
     * </pre>
     * 
     * @see LinkedHashMap#remove(java.lang.Object)
     * @param pintKey
     *                 The position in the collection to remove.
     * @return The object that is going to be removed, null if none is being
     *             removed.
     */
    public Object remove(int pintKey) {
        Iterator iter = iterator();
        int j = 0;
        Object remove = null;
        while (iter.hasNext()) {
            remove = iter.next();
            j++;
            if (j == pintKey) {
                iter.remove();
                return remove;
            }
        }
        return remove;
    }


    /**
     * A helper method to allow for iteration by key.
     * 
     * @return A iterator by key.
     */
    public Iterator iteratorKey() {
        return this.collection.keySet().iterator();
    }

    /**
     * Only call this method when you really need to. 
     * Try to get away with the follwing.
     * 
     * <code><pre>
     * ZXCollection col = new ZXCollection();
     * int number =0;
     * while (...) {
     * 	...
     * 	... 
     * 	number++;
     * 	col.put(new Integar(number), obj);
     * }
     * 
     * </pre></code>
     * 
     * or if you are inserting a ZXObject into the collection
     * and the ZXObject key is going to be difinetely 
     * not null and unique then do the following :
     * 
     * <code><pre>
     * ZXCollection col = new ZXCollection();
     * while (...) {
     * 	...
     * 	... 
     *     objAttr.setKey(..);
     * 	col.put(objAttr);
     * }
     * </pre></code>
     * 
     * @param obj The object you want to add to the collection.
     */
    public void add(Object obj) {
        
        int size = this.collection.size();
        
        Integer num;
        if (size > 0) {
            if (size == 1) {
                num = new Integer(size);
            } else {
                num = new Integer(size + 1);
            }
        } else {
            num = new Integer(0);
        }
        
        put(num, obj);
    }
    
    /**
     * Returns a collection view of the values contained in this map.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection values() {
        return this.collection.values();
    }
    
    /**
     * Returns the actual collection constainer.
     * 
     * DO NOT USE UNLESS SURE. VERY SURE :)
     * 
     * @return Returns the collection.
     */
    private Map getLinkedHashmapCollection() {
        return this.collection;
    }
    
    /**
     * @return Returns the collection.
     */
    public Collection getCollection() {
        return this.collection.values();
    }
    
    //---------------------------------------------------------- Object overidden method.
    
    /** 
     * String formatted version of ZXCollection.
     * @see java.lang.Object#toString()
     **/
    public String toString() {
//        StringBuffer toString = new StringBuffer(size() * 10);
//        
//        Iterator iter = iterator();
//        Object objEntry;
//        while (iter.hasNext()) {
//            objEntry = iter.next();
//            toString.append(objEntry.toString()).append(",");
//        }     
//        return toString.toString();
        return this.collection.toString();
    }
    
    /** 
     * Returns the hash code value for this map.  The hash code of a map is
     * defined to be the sum of the hash codes of each entry in the map's
     * <tt>entrySet()</tt> view.  This ensures that <tt>t1.equals(t2)</tt>
     * implies that <tt>t1.hashCode()==t2.hashCode()</tt> for any two maps
     * <tt>t1</tt> and <tt>t2</tt>, as required by the general contract of
     * Object.hashCode.<p>
     *
     * This implementation iterates over <tt>entrySet()</tt>, calling
     * <tt>hashCode</tt> on each element (entry) in the Collection, and adding
     * up the results.
     *
     * @return the hash code value for this map.
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see java.lang.Object#hashCode()
     **/
    public int hashCode() {
        return this.collection.hashCode();
    }
    
    /**
     * Used by Digester.
     * 
     * @param pobjKeyedObject The object you want to add.
     */
    public void put(KeyedObject pobjKeyedObject) {
		String strKey = pobjKeyedObject.getKey();
		
		/**
		 * If we have a key use it otherwise we
		 * can presume that this is a List?
		 */
        if (StringUtil.len(strKey) == 0) {
        	/**
        	 * You should rather use an ArrayList
        	 */
            add(pobjKeyedObject);
            
        } else {
            put(strKey, pobjKeyedObject);
        }
    }
    
    /**
     * Used by Digester.
     * 
     * @param pobj The object you want to add.
     */
    public void put(Object pobj) {
		add(pobj);
    }
    
//    private static ITimer timer;
//    static {
//        timer = TimerFactory.newTimer();
//    }
    
    /**
     * Create a clean clone of the collection.
     * 
     * <pre>
     * If the entries implement CloneableObject interface we 
     * performance a clone on the entry.
     * </pre>
     * 
     * @see java.lang.Object#clone()
     **/
    public Object clone() {
    	
//    	timer.start();
    	
        //---- BEGIN
        
//        ZXCollection colCollection = new ZXCollection(this.collection.size());
//        
//        colCollection.setKey(getKey());
//        
//        // Performance tweak.
//        boolean isCloneableObject = false;
//        
//        Object objKey;
//        Object objValue;
//        
//        Iterator iterKeys = this.collection.keySet().iterator();
//        while (iterKeys.hasNext()) {
//            objKey = iterKeys.next();
//            objValue = this.collection.get(objKey);
//            
//            if (isCloneableObject || objValue instanceof CloneableObject) {
//                objValue = ((CloneableObject)objValue).clone();
//                isCloneableObject = true;
//                
//            } else {
//                // Maybe the object implements Clonable?
//            }
//            
//            colCollection.put(objKey, objValue);
//        }
        
        //---- END
    	
    	// --- OR - More peformant ? 
    	
        //---- BEGIN
    	
        ZXCollection colCollection = new ZXCollection(this.collection.size());
        // Performance tweak.
        boolean isCloneableObject = false;
        
        Object objValue;
        
        Iterator iterKeys = this.collection.keySet().iterator();
        Iterator iterValues = this.collection.values().iterator();
        while (iterKeys.hasNext()) {
            objValue = iterValues.next();
            
            if (isCloneableObject || objValue instanceof CloneableObject) {
                objValue = ((CloneableObject)objValue).clone();
                isCloneableObject = true;
            } else {
                // Maybe the object implements Clonable?
            	throw new NestableRuntimeException("Invalid collection entry");
            }
            
            colCollection.put(iterKeys.next(), objValue);
        }
        
        //---- END
        
//        timer.stop();
//        System.err.println("Cloning complete : " + timer.getDuration() + " size : "  + this.collection.size());
//        timer.reset();
        
        return colCollection;
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        return this.collection.keySet();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        return this.collection.keySet();
    } 
}