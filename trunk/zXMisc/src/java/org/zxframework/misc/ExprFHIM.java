package org.zxframework.misc;

import java.util.ArrayList;

import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.expression.ExprFH;
import org.zxframework.expression.ExpressionFunction;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringUtil;

/**
 * CaseMaster - Instant Messaging expression handler
 * 
 * (C) 2005 - 9 Knots Business Solutions Ltd
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ExprFHIM extends ExprFH {
	
	//------------------------ Constructor
	
	/**
	 * Default constructor.
	 */
	public ExprFHIM() {
		super();
	}
	
	//------------------------ Inner classes
	
	/**
	 * Get the list of receivers for a message.
	 * 
	 * <pre>
	 * 
	 * Usage : 
	 *  receiverList(<pk>) -> string
	 *  receiverList(<bo>) -> string
	 * </pre>     
	 */
	public static class receiverlist extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Get the list of receivers for a message", "S", "1", "str|lng"};
        /** Default constructor. */
        public receiverlist() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            checkArgs(pcolArgs, 1);
            
            Property objArg = (Property) pcolArgs.get(0);
            InstantMessage objMssge;
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
            	if (StringUtil.isNumeric(objArg.getStringValue())) {
            		/**
            		 * Must be PK
            		 */
            		objMssge = (InstantMessage)getZx().getBos().quickLoad("im/mssge", objArg);
            		if (objMssge == null) {
            			throw new ZXException("Unable to load instance of im/mssge", objArg.getStringValue());
            		}
            		
            	} else {
            		/**
            		 * Must be reference to bo Context entry
            		 */
            		objMssge = (InstantMessage)getZx().getBOContext().getBO(objArg.getStringValue());
            		if (objMssge == null) {
            			throw new ZXException("Unable to get instance of im/mssge");
            		}
            		
            	}
            	
            	exec = new StringProperty(objMssge.receiverList());
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Get receiver list for message " + objArg.getStringValue() + " exists in BO context");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
	}
	
    /**
     * getnextunreadimmediatercvr - Get the PK of the next immediate unread message for the current user (no parameters)
     * or the given user
     * 
     * <pre>
     * 
     * Usage : 
     *  getNextUnreadImmediateRcvr -> 0 or PK of im/rcvr
     *  getNextUnreadImmediateRcvr(user) -> 0 or PK of im/rcvr
     * </pre>
     */
    public static class getnextunreadimmediatercvr extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Get the PK of the next immediate unread message", "S", "0", "str"};
        /** Default constructor. */
        public getnextunreadimmediatercvr() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int intColArgs = pcolArgs.size();
            if (intColArgs > 1) {
            	throw new ZXException("One or zero parameters expected for function getNextUnreadImmediateRcvr; found ", intColArgs + "");
            }
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
            	InstantMessage objMssge = (InstantMessage)getZx().createBO("im/mssge");
            	if (objMssge == null) {
            		throw new ZXException("Unable to create instance of im/mssge");
            	}
            	
            	InstantMessage objRcvr;
            	if (intColArgs == 0) {
            		objRcvr = objMssge.getNextUnreadImmediateRcvr(getZx().getUserProfile().getValue("id").getStringValue());
            	} else {
            		Property objArg = (Property)pcolArgs.get(0);
            		objRcvr = objMssge.getNextUnreadImmediateRcvr(objArg.getStringValue());
            	}
            	
            	if (objRcvr == null) {
            		exec = new LongProperty(0, true);
            	} else {
            		exec = objRcvr.getValue("id");
            	}
            	
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
            	if (intColArgs == 0) {
            		exec = new StringProperty("Get PK for next unread immediate rcvr for current user");
            	} else {
            		exec = new StringProperty("Get PK for next unread immediate rcvr for '" + pcolArgs.get(0) + "'");
            	}
            	
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * writestatusfile - Write the status file (with number of messages, unread messages, unread immediate
     * messages etc) as used by Instant Messaging client polling routine. Writes files for current user or user that is provided
     * 
     * <pre>
     * 
     * Usage : 
     *  writeStatusFile -> 0 or PK of im/rcvr
     *  writeStatusFile(user) -> 0 or PK of im/rcvr
     * </pre>
     */
    public static class writestatusfile extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Write the status file", "S", "0", "str"};
        /** Default constructor. */
        public writestatusfile() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int intColArgs = pcolArgs.size();
            if (intColArgs > 1) {
            	throw new ZXException("One or zero parameters expected for function writeStatusFile; found ", intColArgs + "");
            }
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
            	InstantMessage objMssge = (InstantMessage)getZx().createBO("im/mssge");
                if (objMssge == null) {
                	throw new ZXException("Failed to create an instance of im/mssge");
                }
                
                if (pcolArgs.size() == 0) {
                    exec = new LongProperty(objMssge.writeMessageStatusFile(getZx().getUserProfile().getValue("id").getStringValue()).pos, false);
                    
                } else {
                    Property objArg1 = (Property)pcolArgs.get(0);
                    exec = new LongProperty(objMssge.writeMessageStatusFile(objArg1.getStringValue()).pos, false);
                    
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
            	if (pcolArgs.size() == 0) {
            		exec = new StringProperty("Write IM status file for current user");
            	} else {
                	exec = new StringProperty("Write IM status file for '" + pcolArgs.get(0)+  "'");
            	}
            	
            } else {
            	exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    
    
}