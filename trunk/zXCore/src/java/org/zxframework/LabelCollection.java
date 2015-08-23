/*
 * Created on Jan 13, 2004 by michael
 * $Id: LabelCollection.java,v 1.1.2.8 2005/11/21 15:14:16 mike Exp $
 */
package org.zxframework;

import java.util.Iterator;

import org.zxframework.util.StringUtil;

/**
 * LabelCollection -I s a collection of Labels is extends ZXCollection and
 * contains a couple of helper methods is also needs a handle to zx to select
 * the correct language.
 * 
 * <pre>
 * 
 * NOTE : Was called ColLabel is the old vb code.
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class LabelCollection extends ZXCollection {

    //------------------------ Constructors      

    /**
     * Default constructor.
     */
    public LabelCollection() {
        super();
    }
    
    /**
     * @param size The initial size of the collection.
     */
    public LabelCollection(int size) {
        super(size);
    }
    
    //------------------------ Helper methods
    
    /**
     * Overrides the get() method of ZXCollection.
     * 
     * @param pstrLabel The name of the label to return.
     * @return Returns a label.
     */
    public Label get(String pstrLabel) {
        return (Label)super.get(pstrLabel);
    }
    
    /**
     * Gets the label the for the language of the application If there is none
     * the for that laguage english will be selected
     * 
     * @return The label in the language set in zxConfig, else the english
     *             version or finally "NO LABEL FOUND"
     */
    public String getLabel() {
        Label label = getLabelObj();
        
        if (label == null) {
            return "NO LABEL FOUND";
        }
        
        return label.getLabel();
    }

    /**
     * Gets the label description the for the language of the application If
     * there is none the for that laguage, english will be selected
     * 
     * @return The label getDescription in the language set in zxConfig, else
     *             the english version or finally "NO DESCRIPTION FOUND"
     */
    public String getDescription() {
        Label label = getLabelObj();
        
        if (label == null) {
            return "NO DESCRIPTION FOUND";
        }
        
        return label.getDescription();
    }

    /**
     * Get the label obj, by selecting it from the LabelCollection collection
     * by the zx.language and defaulting to english.
     * 
     * @return The label object according to the language.
     */
    private Label getLabelObj() {
        Label label = get(getZx().getLanguage());
        if (label == null) {
            label = get(Environment.DEFAULT_LANGUAGE);
        }
        return label;
    }

    //------------------------ Overloaded Object methods
    
    /**
     * Here is a nice string formatted version
     * 
     * @return Returns the string version of the label collection
     */
    public String toString() {
        StringBuffer toString = new StringBuffer(50);
        
        toString.append(StringUtil.stripPackageName(this.getClass()));
        toString.append(" : ");

        String selected = getZx().getLanguage();
        if (selected == null) {
            selected = "EN";
        }        
        
        Iterator iter = iteratorKey();
        String lng;
        Label labl;
        while (iter.hasNext()) {
            lng = (String)iter.next();
            labl = get(lng);
            
            toString.append(lng);
            toString.append(" - ");
            toString.append(labl.getLabel());
            toString.append(" : ");
            toString.append(labl.getDescription());
            if (lng.equals(selected)) {
                toString.append(" (SELECTED) ");
            }
            toString.append("");
        }
        
        return toString.toString();
    }
    
    /** 
     * @see java.lang.Object#clone()
     **/
    public Object clone() {
        LabelCollection objLabelCollection = new LabelCollection(size());
        objLabelCollection.setKey(getKey());
        
        Label objLabel;
        
        Iterator iter = iterator();
        Iterator iterKey = iteratorKey();
        while (iter.hasNext()) {
        	objLabel = (Label)iter.next();
        	objLabelCollection.put(iterKey.next(), objLabel.clone());
        }
        
        return objLabelCollection;
    }
}