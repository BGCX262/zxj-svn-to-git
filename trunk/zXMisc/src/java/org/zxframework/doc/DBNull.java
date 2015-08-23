/*
 * Created on Jun 9, 2005
 * $Id$
 */
package org.zxframework.doc;

import java.util.Map;

import org.w3c.dom.Element;

import org.zxframework.exception.NestableRuntimeException;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;
import org.zxframework.ZXException;
import org.zxframework.zXType;

/**
 * The null action implementation.
 * 
 * <pre>
 * 
 * This is usually used to preload entities.
 * 
 * Who    : Bertus Dispa
 * When   : 17 May 2003
 * 
 * Change    : BD3DEC03
 * Why       : Now load entities so we can use in expressions
 * </pre>
 *  
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBNull extends DBAction {
	
	//------------------------ DBAction implemented methods.
	
    /**
     * @see org.zxframework.doc.DBAction#dumpAsXML(XMLGen)
     */
    public void dumpAsXML(XMLGen objXMLGen) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        try {
        	// Call the super to get the first generic parts of the xml.
            super.dumpAsXML(objXMLGen);
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Dump xml", e);
            throw new NestableRuntimeException("Failed to : Dump xml", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * @see org.zxframework.doc.DBAction#parse(Element)
     */
    public zXType.rc parse(Element pobjXMLNode) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc parse = zXType.rc.rcOK;
        
        try {
        	
        	return parse;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Parse Action", e);
            throw new NestableRuntimeException("Failed to : Parse Action", e);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(parse);
                getZx().trace.exitMethod();
            }
        }    
    }
    
	/**
	 * @see org.zxframework.doc.DBAction#go(org.zxframework.doc.DocBuilder)
	 */
	public zXType.rc go(DocBuilder pobjDocBuilder) throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        zXType.rc go = zXType.rc.rcOK;
        
        try {
        	
        	/**
        	 * Get and load entities
        	 */
        	Map colEntities = pobjDocBuilder.resolveEntities(this);
        	if (colEntities == null) {
        		throw new ZXException("Failed to get entities");
        	}
        	
        	if (pobjDocBuilder.loadEntities(colEntities).pos != zXType.rc.rcOK.pos) {
        		throw new ZXException("Failed to load entities");
        	}
        	
        	return go;
        } catch (Exception e) {
	        getZx().trace.addError("Failed to : Process DBNull action.", e);
	        
	        if (getZx().throwException) throw new ZXException(e);
	        go = zXType.rc.rcError;
	        return go;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(go);
                getZx().trace.exitMethod();
            }
        } 
	}
	
	//------------------------ Object implemeted methods
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		toString.appendSuper(super.toString());
		return toString.toString();
	}
}