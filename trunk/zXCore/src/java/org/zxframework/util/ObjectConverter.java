/*
 * Created on 20-Feb-2005 by Michael
 * $Id: ObjectConverter.java,v 1.1.2.3 2005/06/08 10:07:09 mike Exp $
 */
package org.zxframework.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * The most complicated handler is for general objects. We want to show the
 * class name and all the fields and their values. This is done recursively.
 */
public class ObjectConverter extends ToStringConverter {

    /**
     * @param successor
     */
    public ObjectConverter(ToStringConverter successor) {
        super(successor);
    }

    /**
     * This is the end-point of the chain. If we get here, we use this handler.
     * 
     * @param o The object to try and perform a toString on.
     * @return Return true if this handler can do the toString.
     */
    protected boolean isHandler(Object o) {
        return true;
    }

    /**
     * We specify a different separator for between name and values to make the
     * output look nicer.
     * 
     * @return Returns the separator between values.
     */
    protected char getSeparator() {
        return ':';
    }

    /**
     * For the name of the class, we strip off the package name.
     * 
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     */
    protected void appendName(Object o, StringBuffer buf) {
        buf.append(StringUtil.stripPackageName(o.getClass()));
    }

    /**
     * The values are a bit more complicated. We first have to find all fields.
     * To find all private fields, we have to recurse up the hierarchy of the
     * object. We then go through all the fields and append the name and the
     * value. Unless we have reached the end, we append ", ".
     * 
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     */
    protected void appendValues(Object o, StringBuffer buf) {
        Iterator it = findAllFields(o);
        while (it.hasNext()) {
            Field f = (Field) it.next();
            appendFieldName(f, o, buf);
            buf.append('=');
            appendFieldValue(f, o, buf);
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
    }

    /**
     * Append field name to buffer.
     * If the field's class is not the object's class (i.e. the field is
     * declared in a superclass) then we print out the superclass' name together
     * with the field name.
     * 
     * @param f The object field to use.
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     */
    private void appendFieldName(Field f, Object o, StringBuffer buf) {
        if (f.getDeclaringClass() != o.getClass()) {
            buf.append(StringUtil.stripPackageName(f.getDeclaringClass()));
            buf.append('.');
        }
        buf.append(f.getName());
    }

    /**
     * Append the field value to the buffer.
     * 
     * We set the field to be "accessible", i.e. public. If the type of the
     * field is primitive, we simply append the value; otherwise we recursively
     * print the value using our ToStringFacade.
     * 
     * @param f The object field to use.
     * @param o The object we are trying to get the toString of.
     * @param buf The buffer storing the contents of the toString.
     */
    private void appendFieldValue(Field f, Object o, StringBuffer buf) {
        try {
            f.setAccessible(true);
            Object value = f.get(o);
            if (f.getType().isPrimitive()) {
                buf.append(value);
            } else {
                ToStringFacade.toString(value, buf);
            }
        } catch (IllegalAccessException e) {
            // assert false : "We have already set it accessible!";
        }
    }

    /**
     * Find all fields of an object, whether private or public. We also look at
     * the fields in super classes.
     * 
     * @param o The object we are trying to get the toString of.
     * @return Returns all fields of an object.
     */
    private Iterator findAllFields(Object o) {
        Collection result = new LinkedList();
        Class c = o.getClass();
        while (c != null) {
            Field[] f = c.getDeclaredFields();
            for (int i = 0; i < f.length; i++) {
                if (!Modifier.isStatic(f[i].getModifiers())) {
                    result.add(f[i]);
                }
            }
            c = c.getSuperclass();
        }
        return result.iterator();
    }
}
