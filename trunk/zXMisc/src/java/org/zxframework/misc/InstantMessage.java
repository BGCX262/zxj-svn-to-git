package org.zxframework.misc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.Iterator;

import org.zxframework.BOCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSRS;
import org.zxframework.property.LongProperty;
import org.zxframework.property.DateProperty;
import org.zxframework.util.StringUtil;

/**
 * CaseMaster - Instant Messaging
 * 
 * (C) 2005 - 9 Knots Business Solutions Ltd
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class InstantMessage extends ZXBO {
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public InstantMessage() {
		super();
	}
	
	//------------------------ Public methods.
	
	/**
	 * Send the message to the receivers.
	 * 
	 * <pre>
	 *
	 * Note that this has a potential small problem:
	 *
	 * It could be that the updating of some users message status files
	 * works but somewhere, something fails and we thus stop / rollback.
	 * This would mean that some users will have unread messages according
	 * to the status file but when they go into their inbox they may find
	 * no unread messages.
	 * 
	 * Assumes   :
	 *   - Me is im/mssge
	 *   - Me is loaded
	 *   - Db tx in progress
	 *   - Me stts = 1 (incomplete)
	 * </pre>
	 * 
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if send fails.
	 */
	public zXType.rc send() throws ZXException {
		zXType.rc send = zXType.rc.rcOK;
		
		/**
		 * Now make the message active
		 */
	    setValue("stts", new LongProperty(2)); 	// Active
	    
	    if(updateBO("stts").pos != zXType.rc.rcOK.pos) {
	        throw new ZXException("Unable to update message status");
	    }

	    /**
	     * We need to call updateStatusFile for each of the receivers 
	     */
	    BOCollection colRcvr = quickFKCollection("im/rcvr", null);
	    if (colRcvr == null) {
	        throw new ZXException("Unable to retrieve im/rcrvr associated with message");
	    }
	    
	    InstantMessage objRcvr;
	    
	    Iterator iter = colRcvr.iterator();
	    while (iter.hasNext()) {
	    	objRcvr = (InstantMessage)iter.next();
	        if (objRcvr.writeMessageStatusFile(objRcvr.getValue("usrPrfle").getStringValue()).pos != zXType.rc.rcOK.pos ) {
	            throw new ZXException("Unable to write message status file for receiver", objRcvr.formattedString("usrPrfle"));
	        }
	    } // Loop over receivers
	    
	    return send;
	}
	
	/**
	 * Write the message status file for the given
	 * user. This file is used by instant messaging
	 * client software to check for unread and unread
	 * urgent messages.
	 * 
	 * <pre>
	 * 
	 * Assumes   :
	 *   - Me is an iBO; not a specific one
	 *   - Me is loaded
	 * </pre>
	 * 
	 * 
	 * @param pstrUser The current logged in user.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if writeMessageStatusFile fails
	 */
	public zXType.rc writeMessageStatusFile(String pstrUser) throws ZXException {
		zXType.rc writeMessageStatusFile = zXType.rc.rcOK;
		
		InstantMessage objMssge = null;
		InstantMessage objRcvr = null;
		
    	objMssge = (InstantMessage)getZx().createBO("im/mssge");
    	if (objMssge == null){
    		getZx().trace.addError("Failed to create instance of im/mssge");
    		writeMessageStatusFile = zXType.rc.rcError;
    		return writeMessageStatusFile;
    	}
    	
    	objRcvr = (InstantMessage)getZx().createBO("im/rcvr");
    	if (objRcvr == null) {
    		getZx().trace.addError("Failed to create instance of im/rcvr");
    		writeMessageStatusFile = zXType.rc.rcError;
    		return writeMessageStatusFile;
    	}
    	
	    String strFileName = getZx().getSettings().getStatusFileDir() + pstrUser.trim() + ".im";
	    
	    String strSQL = "";
	    String strBaseSQL = "";
	    
    	if (StringUtil.len(strBaseSQL) == 0) {
	        strBaseSQL = "select count(*), min(" + getZx().getSql().columnName(objRcvr, 
	        																   objRcvr.getDescriptor().getAttribute("id"), 
	        																   zXType.sqlObjectName.sonName) 
	        			 + ") ";
	        
	        strBaseSQL = strBaseSQL + getZx().getSql().selectQuery(new ZXBO[]{objMssge,objRcvr}, 
	        													   new String[]{"null","null"}, 
	        													   new boolean[]{false,false},
	        													   false,false,false,false,false);
	        
	        strBaseSQL = strBaseSQL + " AND " + getZx().getSql().whereCondition(objMssge, ":stts=2&expryDte>=#date");
    	}
    	
    	objRcvr.setValue("usrPrfle", pstrUser);
    	
    	strSQL = strBaseSQL + " AND " + getZx().getSql().whereCondition(objRcvr, ":usrPrfle=#value&rdWhn=#null");
    	
    	DSRS objRS = getZx().getDataSources().getPrimary().sqlRS(strSQL);
    	if (objRS == null) {
	        getZx().trace.addError("Unable to execute query to count unread messages");
	        writeMessageStatusFile = zXType.rc.rcError;
	        return writeMessageStatusFile;
    	}
    	
	    int intUnread = 0;
	    int intImmediate = 0;
	    int lngNextUnread = 0;
	    
	    try {
		    intUnread = objRS.getRs().getTarget().getInt(1);
		    
		    strSQL = strSQL + " AND " + getZx().getSql().whereCondition(objMssge, ":immdte=#true");
		    objRS = getZx().getDataSources().getPrimary().sqlRS(strSQL);
		    
		    if (objRS == null) {
		        getZx().trace.addError("Unable to execute query to count immediate messages");
		        writeMessageStatusFile = zXType.rc.rcError;
		        return writeMessageStatusFile;
		    }
		    
		    intImmediate = objRS.getRs().getTarget().getInt(1);
		    
		    lngNextUnread = objRS.getRs().getTarget().getInt(2);
		    
		    /**
		     * Default to 0 if null.
		     */
		    if (objRS.getRs().getTarget().wasNull()) {
		        lngNextUnread = 0;
			}
	    } catch (SQLException e) {
	    	throw new RuntimeException(e);
	    }
	    
	    /**
	     * Write to the messages files
	     */
	    BufferedOutputStream out = null;
	    
	    try {
	    	File file = new File(strFileName);
	    	
	    	/**
			 * Create a new file if needed
			 */
			if (!file.exists()) {
    			file.createNewFile();
			}
			
			out = new BufferedOutputStream(new FileOutputStream(file));
			
			StringBuffer strWrite = new StringBuffer("intUnread = ").append(intUnread).append(";\n");
			strWrite.append("intImmediate = ").append(intImmediate).append(";\n");
			strWrite.append("intNextMsgId = ").append(lngNextUnread).append(";\n");
			
			out.write(strWrite.toString().getBytes());
			
			out.close();
			
	    } catch (Exception e) {
	    	try {
		    	if (out != null) out.close();
	    	} catch (Exception e1){ 
	    		// Ignore, there is nothing more that we can do here.
	    	}
	    }
	    
		return writeMessageStatusFile;
	}
	
	/**
	 * Get handle to next im/rcvr for next unread immediate message for user.
	 * 
	 * <pre>
	 * 
	 * Assumes   :
	 *    - Me is iBO of clsIM
	 *   - Returns nothing on none found
	 *   - Returns populated im/rcvr otherwise
	 * </pre>
	 * 
	 * @param pstrUser The current user logged in.
	 * @return Returns the handle of the reciever for the next message.
	 * @throws ZXException Thrown if getNextUnreadImmediateRcvr fails.
	 */
	public InstantMessage getNextUnreadImmediateRcvr(String pstrUser) throws ZXException {
	    InstantMessage getNextUnreadImmediateRcvr = (InstantMessage)getZx().createBO("im/rcvr");
	    if (getNextUnreadImmediateRcvr == null) {
	    	throw new ZXException("Could not create instance of im/rcvr");
	    }
	    
	    ZXBO objMssge = getZx().createBO("im/mssge");
	    if (objMssge == null) {
	    	throw new ZXException("Could not create instance of im/mssge");
	    }
	    
	    /**
	     * Get receiver that is unread for imediate message that is active and not expired
	     * and order by id so we get the oldest
	     */
	    String strSQL = getZx().getSql().selectQuery(new ZXBO[]{objMssge, getNextUnreadImmediateRcvr}, 
	    											 new String[]{"null","*"},  
	    											 new boolean[]{false, false});
	    
	    strSQL = strSQL + " AND " + getZx().getSql().whereCondition(objMssge, ":immdte=#true&stts=2&expryDte>=#date");
	    
	    getNextUnreadImmediateRcvr.setValue("usrPrfle", pstrUser);
	    strSQL = strSQL + " AND " + getZx().getSql().whereCondition(getNextUnreadImmediateRcvr, ":rdWhn=#null&usrPrfle=#value");
	    
	    strSQL = strSQL + getZx().getSql().orderByClause(getNextUnreadImmediateRcvr, "id", false);
	    
	    DSRS objRS = getZx().getDataSources().getPrimary().sqlRS(strSQL);
	    if (objRS == null) {
	        throw new ZXException("Unable to execute query", strSQL);
	    }
	    
	    if (!objRS.eof()) {
	        objRS.rs2obj(getNextUnreadImmediateRcvr, "*");
	    }
	    
	    return getNextUnreadImmediateRcvr;
	}
	
	/**
	 * Construct receiver list for this msg.
	 * 
	 * @return Returns a comma seperated list of recievers.
	 * @throws ZXException Thrown if recieverList fails.
	 */
	public String receiverList() throws ZXException {
		String receiverList;
		
		BOCollection colRcvr = quickFKCollection("im/rcvr", "", "+", "+,usrPrfle", false, null, "usrPrfle", false);
	    if (colRcvr == null) {
	        throw new ZXException("Unable to retrieve receivers for message");
	    }

	    receiverList = colRcvr.col2String("usrPrfle", "", "; ");
	    
		return receiverList;
	}
	
	/**
	 * Reply to a message.
	 *
	 * <pre>
	 * 
	 * Assumes   :
	 *    - Me is im/mssge
	 *   - Me is loaded
	 *   - DB tx in progress
	 *   - im/mssge is inserted into database
	 *   and returned
	 * </pre>
	 * 
	 * @param pblnToAll Whether to reply to all. Optional, default is false.
	 * @return Returns the reply message.
	 * @throws ZXException Thrown if reply fails.
	 */
	public InstantMessage reply(boolean pblnToAll) throws ZXException {
		InstantMessage reply;
		
	    InstantMessage objMssge = (InstantMessage)cloneBO();
	    if (objMssge == null) {
	    	throw new ZXException("Failed to clone business object");
	    }
	    
	    objMssge.resetBO("*", true);
	    
	    /**
	     * Start new thread?
	     */
	    if (getValue("thrd").longValue() == 0) {
	        setValue("thrd", getValue("id"));
	        
	        if (updateBO("thrd").pos != zXType.rc.rcOK.pos) {	
	        	getZx().trace.addError("Failed to update thrd");
	        }
	        
	    } // New thread?
	    
	    /**
	     * Copy some fields 
	     */
	    bo2bo(objMssge, "immdte,dcmnt,pk,tpe,thrd");
	    
	    
	    boolean blnOptional = objMssge.getDescriptor().getAttribute("mssge").isOptional();
	    
	    try {
		    /**
		     * We want to do an insert but message is a mandatory field and is blank so
		     * massage the descriptor 
		     */
		    objMssge.getDescriptor().getAttribute("mssge").setOptional(true);
		    
		    /**
		     * And insert
		     */
		    if (objMssge.insertBO().pos != zXType.rc.rcOK.pos) {
		        throw new ZXException("Unable to insert instance of im/mssge");
		    }
		    
	    } finally {
		    /**
		     * And back again
		     */
		    objMssge.getDescriptor().getAttribute("mssge").setOptional(blnOptional);
	    }
	    
	    /**
	     * Create appropriate reply 
	     */
	    if (pblnToAll) {
	        /**
	         * Get all receivers
	         **/
	        BOCollection colRcvr = quickFKCollection("im/rcvr", "");
	        if (colRcvr == null) {
	        	throw new ZXException("Unable to retrieve receivers");
	        }
	        
	        InstantMessage objRcvr;
	        Iterator iter = colRcvr.iterator();
	        
	        while (iter.hasNext()) {
	        	objRcvr = (InstantMessage)iter.next();
	            
	        	/**
	             * Do not reply to myself
	             **/
	            if (objRcvr.getValue("usrPrfle").compareTo(getZx().getUserProfile().getValue("id")) != 0) {
	                if (objMssge.addRcvr(objRcvr.getValue("usrPrfle").getStringValue()).pos != zXType.rc.rcOK.pos) {
	                    throw new ZXException("Unable to create im/rcvr for reply");
	    			}
	    		} // Not to myself
	    	} // Loop over all receivers
	    }
	    
	    /**
	     * Always send to sender
	     **/
	    if (objMssge.addRcvr(getValue("zXCrtdBy").getStringValue()).pos != zXType.rc.rcOK.pos) {
	        throw new ZXException("Unable to create im/rcvr for reply");
	    }
	    
	    reply = objMssge;
	    
	    return reply;
	}
	
	/**
	 * Add receiver to a message.
	 * 
	 * <pre>
	 * 
	 * Assumes   :
	 *   - Me is im/mssge
	 *   - Me is loaded
	 *   - DB tx in progress
	 *   - Will insert instance of im/rcvr
	 * </pre>
	 * 
	 * @param pstrUser The user id.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if addRcvr fails.
	 */
	public zXType.rc addRcvr(String pstrUser) throws ZXException {
		zXType.rc addRcvr = zXType.rc.rcOK;
		
	    InstantMessage objRcvr = (InstantMessage)getZx().createFKBO(this, "im/rcvr");
	    if (objRcvr == null) {
	    	throw new ZXException("Failed to create instance of im/rcvr");
	    }

	    objRcvr.setValue("usrPrfle", pstrUser);
	    
	    if (objRcvr.insertBO().pos == zXType.rc.rcError.pos) {
	        throw new ZXException("Unable to insert instance of im/rcvr");
	    }
		
		return addRcvr;
	}
	
	/**
	 * Mark im/rcvr as read; either me (when me is im/rcvr) or for given user (when me is im/mssge).
	 * 
	 * @param pstrUser Mark im/rcvr as read; either me (when me is im/rcvr) or for given user (when me is im/mssge). Optional, default is null.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if markAsRead fails.
	 */
	public zXType.rc markAsRead(String pstrUser) throws ZXException {
		zXType.rc markAsRead = zXType.rc.rcOK;
		
	    if (getDescriptor().getName().equals("im/mssge")){
	        /**
	         * Is im/mssge thus requires a user
	         **/
	        if (StringUtil.len(pstrUser) == 0) {
	            throw new ZXException("When this routine is called with me is im/mssge, a user is required");
	        }
	        
	        /**
	         * Find receiver and mark as read
	         **/
	        InstantMessage objRcvr = (InstantMessage)getZx().createBO("im/rcvr");
	        if (objRcvr == null) {
	        	throw new ZXException("Failed to create an instance of im/rcvr");
	        }
	        
	        objRcvr.setValue("mssge", getValue("id"));
	        objRcvr.setValue("usrPrfle", pstrUser);
	        objRcvr.setValue("rdWhn", new DateProperty(getZx().getAppDate()));
	        
	        if (objRcvr.updateBO("rdWhn", "mssge,usrPrfle").pos == zXType.rc.rcError.pos) {
	            throw new ZXException("Unable to mark receiver as read");
	    	}
	        
	    } else {
	        setValue("rdWhn", new DateProperty(getZx().getAppDate()));
	        
	        if (updateBO("rdWhn").pos == zXType.rc.rcError.pos) {
		        throw new ZXException("Unable to mark receiver as read");
	        }
            
	    } // im/mssge or im/rcvr
	    
		return markAsRead;
	}
	
	/**
	 * Send quick reply to a message.
	 * 
	 * <pre>
	 * 
	 * Assumes   :
	 *   - Me is im/stndrdMssge
	 *   - Me is loaded
	 *   - DB tx in progress
	 *   - im/mssge and im/rcvr created on success and status files regenerated
	 * </pre>
	 * 
	 * @param pobjMssge Loaded instance of im/mssge to reply to
	 * @param pblnToAll Reply to all (true) or to sender (false). Optional, default is false.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if sendQuickReply fails.
	 */
	public zXType.rc sendQuickReply(InstantMessage pobjMssge, boolean pblnToAll) throws ZXException {
		zXType.rc sendQuickReply = zXType.rc.rcOK;
		
	    InstantMessage objMssge = prepareQuickReply(pobjMssge, pblnToAll);
	    if (objMssge == null) {
	        throw new ZXException("Unable to create instance of reply message");
	    }
	    
	    if (objMssge.send().pos != zXType.rc.rcOK.pos) {
	        throw new ZXException("Unable to send message");
	    }
	    
		return sendQuickReply;
	}
	
	/**
	 * Prepare  quick reply message.
	 * 
	 * <pre>
	 * 
	 * Assumes   :
	 *   - Me is im/stndrdMssge
	 *   - Me is loaded
	 *   - DB tx in progress
	 *   - im/rcvrs and im/mssge inserted
	 * </pre>
	 * 
	 * @param pobjMssge Loaded instance of im/mssge to reply to. 
	 * @param pblnToAll Reply to all (true) or to sender (false). Optional, default should be false.
	 * @return Returns the quick message.
	 * @throws ZXException Thrown if prepareQuickReply fails.
	 */
	public InstantMessage prepareQuickReply(InstantMessage pobjMssge, boolean pblnToAll) throws ZXException {
	    InstantMessage prepareQuickReply = pobjMssge.reply(pblnToAll);
	    if (prepareQuickReply == null) {
	        throw new ZXException("Unable to create reply message");
	    }
	    
	    /**
	     * Now set message (reply creates a message with no message set)
	     */ 
	    prepareQuickReply.setValue("mssge", getValue("mssge"));
	    
	    if (prepareQuickReply.updateBO("mssge").pos != zXType.rc.rcOK.pos) {
	        throw new ZXException("Unable to update message");
	    }
		return prepareQuickReply;
	}
}