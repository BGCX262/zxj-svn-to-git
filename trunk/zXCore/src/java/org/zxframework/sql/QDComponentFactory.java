/*
 * Created on Apr 5, 2004
 * $Id: QDComponentFactory.java,v 1.1.2.3 2006/07/17 16:38:46 mike Exp $
 */
package org.zxframework.sql;

import org.apache.commons.digester.AbstractObjectCreationFactory;

/**
 * A factory for create QDComponent object correctly.
 * 
 * <pre>
 * 
 * This is used by the Digester when parsing the xml config file for a Query Definition.
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class QDComponentFactory extends AbstractObjectCreationFactory {
	
    //------------------------ Members
    
    /** A handle to the Query Definition used by the Object factory **/
    private QueryDef queryDef;
    
    //------------------------------------ Constructors
    
    /**
     * Hide the default constructor
     */
    private QDComponentFactory() {
    	super();
    }
    
    /**
     * Constructor that takes the variables needed to create a proper QSComponent.
     * 
     * @param pobjQueryDef A handle to the QueryDef.
     */
    public QDComponentFactory(QueryDef pobjQueryDef) {
        this.queryDef = pobjQueryDef;
    }
    
    //------------------------ Public Methods
    
	/** 
	 * Creates a proper QDComponent.
	 * @see org.apache.commons.digester.AbstractObjectCreationFactory#createObject(org.xml.sax.Attributes)
	 **/
	public Object createObject(org.xml.sax.Attributes attributes) throws Exception {
        QDComponent objQDComponent = new QDComponent();
        objQDComponent.init(this.queryDef); // Init the object.
        
        return objQDComponent;
	}
}