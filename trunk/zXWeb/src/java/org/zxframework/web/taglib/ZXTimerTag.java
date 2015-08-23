/*
 * Created on Sep 2, 2004
 */
package org.zxframework.web.taglib;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.vladium.utils.timing.ITimer;
import com.vladium.utils.timing.TimerFactory;

/**
 * Used for timing the duration of page execution.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ZXTimerTag  extends TagSupport {
    
    //------------------------ Members
    
    /** The start action **/
    private static final int ACTION_START = 1;
    /** The stop action **/
    private static final int ACTION_STOP = 2;
    
    /** The type of action to perform **/
    private int action;
    
    private static ITimer timer;
    static {
        timer = TimerFactory.newTimer();
    }
    
    //------------------------ Constuctor
    
    /**
     * Default constructor
     */
    public ZXTimerTag() {
        super();
    }
    
    //------------------------ Attribute accessors
    
    /**
     * @param pstrAction Set the action to start or stop
     */
    public void setAction(String pstrAction) {
        if (pstrAction.equalsIgnoreCase("start")) {
            this.action = ACTION_START;
        } else {
            this.action = ACTION_STOP;
        }
    }
    
    //------------------------ Main logic
    
    /** 
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     **/
    public int doEndTag() throws JspException {
        /** Select with action is being performed **/
        switch (this.action) {
            case ACTION_START:
                try {
                    timer.start();
                } catch (Exception e) {
                    // Supress errors as we use this only for performance testing
                }
                break;

            default:
            	try {
                    timer.stop();
                    double duration = timer.getDuration();
                    
                    HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
            	    pageContext.getOut().write("<script type=\"text/javascript\" language=\"JavaScript\">\n");
                    pageContext.getOut().write("window.status='" + request.getRequestURI() + " : " + duration + " milliseconds.';\n");
            	    pageContext.getOut().write("</script>\n");
                } catch (Exception e) {
                    // Supress errors as we use this only for performance testing
                } finally {
                    try {
                        timer.reset();
                    } catch (Exception e) {
                    	/**
                    	 * Ignore any exceptions.
                    	 */
                    }
                }
                break;
        }
        return (EVAL_PAGE);
    }
}