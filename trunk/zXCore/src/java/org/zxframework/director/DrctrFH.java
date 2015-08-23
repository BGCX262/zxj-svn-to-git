/*
 * Created on Mar 12, 2004
 * $Id: DrctrFH.java,v 1.1.2.5 2005/07/12 07:28:43 mike Exp $
 */
package org.zxframework.director;

import java.lang.reflect.Constructor;

import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.util.StringUtil;

/**
 * The framework director function handler definition.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class DrctrFH extends ZXObject {
    
    //------------------------ Members
    
    /** <code>isTouched</code> - Indicates that we support this function. */
    private boolean isTouched = false;
    
    //------------------------ Constructor
    
    /**
     * Default constructor.
     */
    public DrctrFH() {
        super();
    }
    
    //------------------------ Public methods 
    
    /**
     * @return Whether we have resolved a handler in the resolve function
     */
    public boolean isTouched() {
        return isTouched;
    }

    /**
     * @param touch Set whether it has be visited
     */
    public void setTouched(boolean touch) {
        this.isTouched = touch;
    }
    
    //------------------------ Abstract methods
    //------------------------ Util methods
    
    /**
     * Resolve a director function.
     * 
     * @param pobjFunc The director the resolve
     * @return Returns the result of a director
     * @throws ZXException Thrown if resolve fails, this is caused either 
     *                     by not find the director or a failed director
     */
    public String resolve(DirectorFunction pobjFunc)  throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjFunc", pobjFunc);
        }
        
        String resolve = null;

        try {
            
            Class cls = null;
            String strClassname = this.getClass().getName() +  "$" + StringUtil.makeClassName(pobjFunc.getFunc());
            try {
                cls = Class.forName(strClassname);
            } catch (Exception e) {
                if (getZx().log.isInfoEnabled()) {
                    getZx().log.info("Failed to load class :" + strClassname, e);
                }
            }
            
            if (cls != null) {
	            Constructor c =  cls.getConstructor(new  Class []{this.getClass()});
	            Director objDirector = (Director)c.newInstance(new Object[]{this});
	            
	            try {
	                
	                resolve = objDirector.exec(pobjFunc);
		            setTouched(true);
		            
	            } catch (Exception e) {
	                getZx().log.error("Failed to : execute director : " + pobjFunc.getFunc(), e);
	            }
            }
            
            return resolve;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Resolve a director function ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjFunc = " + pobjFunc);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return resolve;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(resolve);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Comma separated string of supported directors in the form of name|use.
     * 
     * @return Returns a comma seperated string of supported directors
     * @throws ZXException Thrown if supports fails
     */
    public String supports()  throws ZXException {
        StringBuffer supports = new StringBuffer();
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
		try {
		    
		    // Use reflection to get the director information.
		    
	        // Class[] cls = DrctrFHDefault.class.getClasses();
	        Class[] cls = this.getClass().getClasses();
	        for (int i = 0; i < cls.length; i++) {
	            Class class1 = cls[i];
	            try {
	                
	                if (!class1.isInterface()) {
	                    if (supports.length() > 0) {
	                        supports.append("\n");
	                    }
	                    String name = class1.getName().substring(class1.getName().indexOf('$')+1);
	                    supports.append("#" + name +  " : " + class1.getField("describe").get(null));
	                }
	                
	            } catch (SecurityException e) {
	                getZx().log.error("SecurityException",e);
	            } catch (IllegalArgumentException e) {
	                getZx().log.error("IllegalArgumentException",e);
	            } catch (IllegalAccessException e) {
	                getZx().log.error("IllegalAccessException",e);
	            } catch (NoSuchFieldException e) {
	                getZx().log.error("NoSuchFieldException",e);
	            }
	        }
		    return supports.toString();
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Get supported directors", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    return supports.toString();
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.returnValue(supports);
		        getZx().trace.exitMethod();
		    }
		}
    }
}