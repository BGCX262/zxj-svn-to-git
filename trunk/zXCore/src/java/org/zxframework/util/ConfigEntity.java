/*
 * Created on Feb 16, 2004 by Michael Brewer
 * $Id: ConfigEntity.java,v 1.1.2.1 2005/02/16 21:50:32 mike Exp $
 */
package org.zxframework.util;

import java.io.File;
import java.util.ArrayList;

import org.jdom.Document;

/**
 * Represents a part/or parent of the zx app config file.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class ConfigEntity {
    
    private ArrayList childern;
    private File file;
    private Document document;
    private boolean dirty;
    
    /**
     * Default constructor.
     */
    protected ConfigEntity() {
        this.childern = new ArrayList();
        this.dirty = false;
    }
    
    /**
     * @return Returns the document.
     */
    public Document getDocument() {
        return document;
    }
    
    /**
     * @param document The document to set.
     */
    public void setDocument(Document document) {
        this.document = document;
    }
    
    /**
     * @return Returns the childern.
     */
    public ArrayList getChildern() {
        return childern;
    }
    
    /**
     * @param childern The childern to set.
     */
    public void setChildern(ArrayList childern) {
        this.childern = childern;
    }
    
    /**
     * @return Returns the fileName.
     */
    public File getFile() {
        return file;
    }
    
    /**
     * @param file The fileName to set.
     */
    public void setFile(File file) {
        this.file = file;
    }
    
    /**
     * @return Returns the dirty.
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * @param dirty The dirty to set.
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}