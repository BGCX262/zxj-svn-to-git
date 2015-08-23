<%
//======================================================================================
// File	:	im.asp
// By	:	Bertus Dispa
// Date	:	DEC05
//
// ASP pageflow driver for instant messaging pageflow
//
// Copyright 9 Knots Business Solutions Ltd 2005 (c)
// 
//======================================================================================
%><%@page import="org.zxframework.*" 
%><%@page import="org.zxframework.web.*" 
%><%@page import="org.zxframework.util.*" 
%><%@page import="org.zxframework.property.*"
%><%@page import="org.zxframework.misc.InstantMessage"
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:timer action="start"/><zx:zx/><% 
ZX objZX = null;
PageBuilder objPage = null;
try {
	objZX = ThreadLocalZX.getZX();
	objPage = new PageBuilder();
%>
<html>

<head>
	<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">
	
	<script type="text/javascript" language="JavaScript" src="../javascript/zX.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXForm.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXList.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/spellChecker.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXTree.js"></script>
	
	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>

<body background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed" scroll="auto">
<%
	//----
	// Get handle to the pageflow object
	//----
	Pageflow objPageflow = objPage.getPageflow(request, response, "");
	if (objPageflow == null) {
		throw new Exception("Unable to initialise pageflow");
	}
	
	//----
	// Safeguard against development errors
	//----
	int intLoopCounter = 0;
	
	String strAction = request.getParameter("-a");
	if (strAction != null) {
		strAction.toLowerCase();
	} else {
		strAction = "";
	}
	
	do {
		if (strAction.equalsIgnoreCase("asp.selectReceivers.done")) {
			//----
			// The user is done selecting users; now we have to set the field that shows the
			// selected users. The name of this control is passed as #qs.-ctrRcvrs
			//----
			InstantMessage objMssge = (InstantMessage)
									  objZX.getBos().quickLoad("im/mssge",
															   new LongProperty(Integer.parseInt(objZX.getQuickContext().getEntry("-pk.mssge"))));
			if (objMssge == null) {
				objPage.fatalError("Unable to create instance / load of im/mssge");
				return;
			}
			
			String strTmp = objMssge.receiverList();
			
			//----
			// No point in showing massive list so truncate after so many positions
			//----
			if (StringUtil.len(strTmp) > 50) {
				strTmp = strTmp.substring(0, 48) + "...";
			}
			
			strTmp = strTmp + "&nbsp;&nbsp;";
			
			//----
			// Now set the receiver field in the opener window; note that we set both the visible and the 
			// not-visible field; I guess we are not too bothered with the not-visible field but you never
			// know....
			//----
			out.write("<script language='javascript'>\n");
			out.write("	elementByName(opener, '" + objZX.getQuickContext().getEntry("-ctr.rcvrs") + "').value = '" + strTmp + "';\n");
			//----
			// MB : Allow this to work on all browsers.
			//----
			out.write(" if (opener.div" + objZX.getQuickContext().getEntry("-ctr.rcvrs") + ")");
			out.write("		opener.div" + objZX.getQuickContext().getEntry("-ctr.rcvrs") + ".innerHTML = '" + strTmp + "';\n");
			out.write("	else \n");
			out.write("		elementByName(opener, 'div" + objZX.getQuickContext().getEntry("-ctr.rcvrs") + "').innerHTML = '" + strTmp + "';\n");
			out.write("	top.window.close();\n");
			out.write("</script>\n");
			
			strAction = "";
			
		} else if (strAction.equalsIgnoreCase("asp.sendMessage")) {
			//----
			// The user wants to send a message -pk.mssge
			//----	
			InstantMessage objMssge = (InstantMessage)
									   objZX.getBos().quickLoad("im/mssge",
														        new StringProperty(objZX.getQuickContext().getEntry("-pk.mssge"))); 
			if (objMssge == null) {
				objPage.fatalError("Unable to create instance of im/mssge");
			}
			
			try {
				objZX.getDataSources().getPrimary().beginTx();
				
				if (objMssge.send().pos != zXType.rc.rcOK.pos) {
					throw new Exception("Unable to send message");
					
				} else {
					objZX.getDataSources().getPrimary().commitTx();
					strAction = "close";
				}
				
			} catch (Exception e) {
				objZX.getDataSources().getPrimary().rollbackTx();
				objPageflow.setErrorMsg("Unable to send message \n" + objZX.trace.formatStack(false));
				strAction = "edit";
			}
			
		} else if (strAction.equalsIgnoreCase("asp.prepareReply")) {
			//----
			// User wants to reply to a message
			// -pk.mssge - Message to reply to
			// -toAll = 1: reply to all; 0 = reply to sender
			//----
			InstantMessage objMssge = (InstantMessage)
									   objZX.getBos().quickLoad("im/mssge", 
															    new LongProperty(Integer.parseInt(objZX.getQuickContext().getEntry("-pk.mssge"))));
			if (objMssge == null) {
				objPage.fatalError("Unable to create instance / load of im/mssge");
			}
			
			try {
				objZX.getDataSources().getPrimary().beginTx();
				
				if (objZX.getQuickContext().getEntry("-toAll") == "1") {
					objMssge = objMssge.reply(true);
				} else {
					objMssge = objMssge.reply(false);
				}
				
				if (objMssge == null) {
					throw new Exception("Failed to create reply");
					
				} else {
					objZX.getDataSources().getPrimary().commitTx();
									
					//----				
					// Store the id of the message we are replying to.				
					//----
					objZX.getQuickContext().setEntry("-pk.mssgeParent", objZX.getQuickContext().getEntry("-pk.mssge"));
					objZX.getQuickContext().setEntry("-pk.replyMssge", objMssge.getValue("id").getStringValue());
					
					strAction = "reply.mssge.view";
				}
				
			} catch (Exception e) {
				objZX.getDataSources().getPrimary().rollbackTx();
				objPageflow.setErrorMsg("Unable to create reply");
				strAction = "read.edit";
			}
			
		} else if (strAction.equalsIgnoreCase("asp.quickReply")) {
			//----
			// User has selected a standard reply and want to send this
			// -pk.mssge - Message to reply to
			// -pk.stndrdMssge - PK of message to reply with
			// -toAll = 1: reply to all; 0 = reply to sender
			//----
			InstantMessage objMssge = (InstantMessage)
									   objZX.getBos().quickLoad("im/mssge", 
															    new LongProperty(Integer.parseInt(objZX.getQuickContext().getEntry("-pk.mssge"))));
			if (objMssge == null) {
				objPage.fatalError("Unable to create instance / load of im/mssge");
			}
			
			InstantMessage objStndrdMssge = (InstantMessage)
											 objZX.getBos().quickLoad("im/stndrdMssge", 
																	  new LongProperty(Integer.parseInt(objZX.getQuickContext().getEntry("-pk.stndrdMssge"))));
			if (objStndrdMssge == null) {
				objPage.fatalError("Unable to create instance / load of im/stndrdMssge");
			}
			
			try {
				objZX.getDataSources().getPrimary().beginTx();
				
				int intRC = 0;
				if (objZX.getQuickContext().getEntry("-toAll") == "1") {
					intRC = objStndrdMssge.sendQuickReply(objMssge, true).pos;
				} else {
					intRC = objStndrdMssge.sendQuickReply(objMssge, false).pos;
				}
				
				if (intRC != zXType.rc.rcOK.pos) {
					objZX.trace.addError("Unable to send quick reply");
				}
				
				if (objMssge == null) {
					throw new Exception("Unable to send reply");	
				} else {
					objZX.getDataSources().getPrimary().commitTx();
					objZX.getQuickContext().setEntry("-pk.mssge", objMssge.getValue("id").getStringValue());
					strAction = "asp.markAsRead";
				}
				
			} catch (Exception e) {
				objZX.getDataSources().getPrimary().rollbackTx();
				objPageflow.setErrorMsg("Unable to send reply");
				strAction = "read.edit";
			}
			
		} else if (strAction.equalsIgnoreCase("asp.markAsRead")) {
			//----
			// User wants to mark a message as read
			// -pk.rcvr - rcvr instance to mark as read
			//----
			InstantMessage objRcvr = (InstantMessage)
									  objZX.getBos().quickLoad("im/rcvr", 
															   new StringProperty(objZX.getQuickContext().getEntry("-pk.rcvr"))); 
			if (objRcvr == null) {
				objPage.fatalError("Unable to create instance of im/rcvr");
			}
			
			try {
				objZX.getDataSources().getPrimary().beginTx();
				
				if (objRcvr.markAsRead("").pos != zXType.rc.rcOK.pos) {
					throw new Exception("Unable to mark as read");
				} else {
					//----
					// Write message status file
					//----
					objRcvr.writeMessageStatusFile(objZX.getUserProfile().getValue("id").getStringValue());
					objZX.getDataSources().getPrimary().commitTx();
					strAction = "close";
				}
				
			} catch (Exception e) {
				objZX.getDataSources().getPrimary().rollbackTx();
				objPageflow.setErrorMsg("Unable to mark as read \n" + objZX.trace.formatStack(false));
				strAction = "read.edit";
			}
			
		} else if (strAction.equalsIgnoreCase("asp.sendReply")) {
			//----
			// The user wants to send the reply -pk.mssge
			//----	
			InstantMessage objMssge = (InstantMessage)
									   objZX.getBos().quickLoad("im/mssge", 
															    new StringProperty(objZX.getQuickContext().getEntry("-pk.replyMssge"))); 
			if (objMssge == null) {
				objPage.fatalError("Unable to create instance of im/mssge");
			}
			
			try {
				objZX.getDataSources().getPrimary().beginTx();
				
				if (objMssge.send().pos != zXType.rc.rcOK.pos) {
					throw new Exception("Unable to send message");
					
				} else {
					objZX.getDataSources().getPrimary().commitTx();
					
					//----
					// Mark this message as read
					//----
					strAction = "asp.markAsRead";
				}
				
			} catch (Exception e) {
				objZX.getDataSources().getPrimary().rollbackTx();
				objPageflow.setErrorMsg("Unable to send message \n" + objZX.trace.formatStack(false));
				strAction = "edit";
			}
			
		} else if (strAction.equalsIgnoreCase("asp.inbox.rewriteStatusFile")) {
			//----
			// Regenerate users message status file
			//----
			objZX.getDataSources().getPrimary().beginTx();
			
			InstantMessage objRcvr = (InstantMessage)objZX.createBO("im/rcvr");
			objRcvr.writeMessageStatusFile(objZX.getUserProfile().getValue("id").getStringValue());
				
			objZX.getDataSources().getPrimary().commitTx();

			strAction = "inbox.filter.edit";
			
		} else if (strAction.equalsIgnoreCase("asp.inbox.markAsRead")) {
			//----
			// The user wants to view a message thus we can safely mark as read + update the status file message
			//----
			InstantMessage objRcvr = (InstantMessage)
									  objZX.getBos().quickLoad("im/rcvr", 
															   new LongProperty(Integer.parseInt(objZX.getQuickContext().getEntry("-pk.rcvr"))));
			
			if (objRcvr.getValue("rdWhn").isNull()) {
				objZX.getDataSources().getPrimary().beginTx();
				
				objRcvr.setValue("rdWhn", new DateProperty(objZX.getAppDate()));
				objRcvr.setValue("stts", new LongProperty(2));
				
				objRcvr.updateBO("rdWhn,stts"); 
			
				objRcvr.writeMessageStatusFile(objZX.getUserProfile().getValue("id").getStringValue());

				objZX.getDataSources().getPrimary().commitTx();
			}
			
			strAction = "inbox.view";
			
		} else if (strAction.equalsIgnoreCase("asp.inbox.prepareReply")) {
			//----
			// User wants to reply to a message
			// -pk.mssge - Message to reply to
			// -toAll = 1: reply to all; 0 = reply to sender
			//----
			InstantMessage objMssge = (InstantMessage)
									   objZX.getBos().quickLoad("im/mssge", 
															    new LongProperty(Integer.parseInt(objZX.getQuickContext().getEntry("-pk.mssge"))));
			if (objMssge == null) {
				objPage.fatalError("Unable to create instance / load of im/mssge");
			}
			
			try {
				objZX.getDataSources().getPrimary().beginTx();
				
				if (objZX.getQuickContext().getEntry("-toAll") == "1"){
					objMssge = objMssge.reply(true);
				} else {
					objMssge = objMssge.reply(false);
				}
				
				if (objMssge == null) {
					throw new Exception("Unable to create reply");
					
				} else {
					objZX.getDataSources().getPrimary().commitTx();
					
					//----				
					// Store the id of the message we are replying to.				
					//----
					objZX.getQuickContext().setEntry("-pk.mssgeParent", objZX.getQuickContext().getEntry("-pk.mssge"));
					objZX.getQuickContext().setEntry("-pk.replyMssge", objMssge.getValue("id").getStringValue());
					
					strAction = "reply.mssge.view";
				}
				
			} catch (Exception e) {
				objZX.getDataSources().getPrimary().rollbackTx();
				objPageflow.setErrorMsg("Unable to create reply");
				strAction = "inbox.view";
				strAction = "";
			}
			
		} else if (strAction.equalsIgnoreCase("asp.inbox.sendReply")) {
			//----
			// The user wants to send the reply -pk.mssge
			//----	
			InstantMessage objMssge = (InstantMessage)
									   objZX.getBos().quickLoad("im/mssge", 
															    new StringProperty(objZX.getQuickContext().getEntry("-pk.replyMssge"))); 
			if (objMssge == null) {
				objPage.fatalError("Unable to create instance of im/mssge");
			}
			
			try {
				objZX.getDataSources().getPrimary().beginTx();
				
				if (objMssge.send().pos != zXType.rc.rcOK.pos) {
					throw new Exception("Unable to send message.");
				} else {
					objZX.getDataSources().getPrimary().commitTx();
				}
				
			} catch (Exception e) {
				objZX.getDataSources().getPrimary().rollbackTx();
				objPageflow.setErrorMsg("Unable to send message \n" + objZX.trace.formatStack(false));
			}
						
			strAction = "inbox.view";
			
		} else if (strAction.equalsIgnoreCase("asp.immediateUnread.markAsRead")) {
			//----
			// The user wants to view a message thus we can safely mark as read + update the status file message
			//----
			InstantMessage objRcvr = (InstantMessage)
									  objZX.getBos().quickLoad("im/rcvr", 
															   new LongProperty(Integer.parseInt(objZX.getQuickContext().getEntry("-pk.rcvr"))));
			
			if (objRcvr.getValue("rdWhn").isNull()) {
				objZX.getDataSources().getPrimary().beginTx();

				objRcvr.setValue("rdWhn", new DateProperty(objZX.getAppDate()));
				objRcvr.setValue("stts", new LongProperty(2));

				objRcvr.updateBO("rdWhn,stts"); 
			
				objRcvr.writeMessageStatusFile(objZX.getUserProfile().getValue("id").getStringValue());

				objZX.getDataSources().getPrimary().commitTx();
			}
			
			strAction = "read.edit";
			
		} else if (strAction.equalsIgnoreCase("asp.onNoImmediateMessageFound") || strAction.equals("asp.rewriteStatusFile")) {
			//----
			// This one is called when the system thinks there is an unread immediate
			// message but in fact there isn't; this can happen when the user refreshes
			// the left pane or when the user is connected to the system from two workstations.
			// What we do is simply rewrtiting the status file and call it a day...
			//----
			
			try {
				objZX.getDataSources().getPrimary().beginTx();
				
				InstantMessage objRcvr = (InstantMessage)objZX.createBO("im/rcvr");
				objRcvr.writeMessageStatusFile(objZX.getUserProfile().getValue("id").getStringValue());
				
			} catch (Exception e) {
				objZX.getDataSources().getPrimary().rollbackTx();
			} finally {
				objZX.getDataSources().getPrimary().commitTx();
			}
			
			strAction = "close";
			
		} else if (strAction.equalsIgnoreCase("asp.sendQuickReply")) {
			//----
			// User wants to reply to a message quickly
			//
			// -pk.mssge - Message to reply to
			// -toAll = 1: reply to all; 0 = reply to sender
			//----
			InstantMessage objMssge = (InstantMessage)
									   objZX.getBos().quickLoad("im/mssge", 
															    new LongProperty(Integer.parseInt(objZX.getQuickContext().getEntry("-pk.mssge"))));
			if (objMssge == null) {
				objPage.fatalError("Unable to create instance / load of im/mssge");
			}
			
			try {
				objZX.getDataSources().getPrimary().beginTx();
				
				if (objZX.getQuickContext().getEntry("-toAll") == "1") {
					objMssge = objMssge.reply(true);
				} else {
					objMssge = objMssge.reply(false);
				}
				
				if (objMssge == null) {
					throw new Exception("Unable to create reply");
					
				} else {
					objZX.getDataSources().getPrimary().commitTx();
					
					ZXBO objMssgeRply = objPageflow.getBO("quickReply.edit", "quckRply");
					objMssge.setValue("mssge", objMssgeRply.getValue("mssge"));
					objMssge.updateBO("mssge");
					
					objZX.getDataSources().getPrimary().beginTx();
				
					if (objMssge.send().pos != 0) {
						objZX.getDataSources().getPrimary().rollbackTx();
						objPageflow.setErrorMsg("Unable to send message \n" + objZX.trace.formatStack(false));
						strAction = "read.edit";
						
					} else {
						objZX.getDataSources().getPrimary().commitTx();
						strAction = "close";
					}
					
					strAction = "close";
				}
				
			} catch (Exception e) {
				objZX.getDataSources().getPrimary().rollbackTx();
				objPageflow.setErrorMsg("Unable to create reply");
				strAction = "read.edit";
			}
			
		} else {
			//----
			// Standard pageflow action
			//----
			zXType.rc enmRC = objPageflow.go(strAction);
			if (enmRC.pos == zXType.rc.rcWarning.pos) {
				//----
				// Action not defined in pageflow definition file
				//----
				strAction = objPageflow.getAction();	
							
			} else {
		 		out.write(objPage.flush());
				out.flush();
				strAction = objPageflow.getAction();
			}
			
		}
		
		intLoopCounter ++;
	} while (StringUtil.len(strAction) > 0 && intLoopCounter < 20);
	
	if (intLoopCounter >= 20) {
		out.write(objPage.fatalError("Unprocessed action : " + strAction));
		// return;
	}
	
	//----
	// Dump any major errors still lingering
	//----
	objPageflow.dumpAllErrors();
	
	//----
	// And store the session if not logged-off
	//----
	if (StringUtil.len(objZX.getSession().getSessionid()) > 0) {
		objZX.getSession().store();
	}
%>
<zx:timer action="stop"/>
</body>
</html>
<%

} catch (ZXException e) {
	PageBuilder.handleJSPException(objPage, out, e);
	
} catch (Exception e) {
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>