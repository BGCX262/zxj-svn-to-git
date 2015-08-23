//----
// What		:	im.js
// Who		:	Bertus Dispa
// When		:	7DEC05
// Why		:	Javascript routines for 9 Knots Instant Messaging
//
//-------------------------------------------------
// This software is protected under internatial copyright law
// 9 Knots Business Solutions Ltd (c)
//-------------------------------------------------
//----

var intUnread = 0;				// Most recent number of unread messages
var intImmediate = 0;			// Immediate unread messages
var intNextMsgId = 0;			// PK of next untread immediate messsage
var intLastMsgId = 0;			// PK of unread immediate message currently being processed
var objMsgCntrWndw;				// A handle to the message centre

//----
// nkImActionsMenu
// 
// Show popup with relevant im actions
//
// _session - valid session id (string)
//----
function imActionsMenu(_session)
{
	var zXPopupObj;
	var strUrl;

	zXPopupObj = new zXDynPopup();
	
	//===============================================
	// Set the current window to the right hand frame
	//===============================================
	if (window.createPopup == undefined) {
		//=======================
		// The browser do not support window.createPopup feature.
		//=======================
		if (top.fraDetailsMenu) {
			//======================
			// We want the popup menu to appear on the right active frameset.
			//======================
			zXPopupObj.setWindow(getActiveFrame());
		} else {
			//======================
			// IFRAME : Temp hack.
			// We want the popup menu to appear above all the frames.
			//======================
			zXPopupObj.setWindow(top);
		}
	}
	
	strUrl = '../' + SCRPT + '/im.' + SCRPT + '?-pf=im/sendMssge&-s=' + _session;
	zXPopupObj.row("Send message","javascript:parent.zXSizedWindow('" + strUrl + "', 900, 500);","","",false);

	strUrl = '../' + SCRPT + '/im.' + SCRPT + '?-pf=im/sendQuickMssge&-pk.stndrdMssge=0&-singleUser=0&-s=' + _session;
	zXPopupObj.row("Send quick message","javascript:parent.zXSizedWindow('" + strUrl + "', 900, 500);","","",false);

	strUrl = '../' + SCRPT + '/im.' + SCRPT + '?-pf=im/sendQuickMssge&-pk.stndrdMssge=1&-allowEdit=1&-singleUser=1&-s=' + _session;
	zXPopupObj.row("Telephone message","javascript:parent.zXSizedWindow('" + strUrl + "', 900, 500);","","",false);

	zXPopupObj.row("Message centre","javascript:parent.zXStdPopup('" + _session + "', 'im/centre',  'start',  '0');","","",true);

	strUrl = '../' + SCRPT + '/zXLeft.' + SCRPT + '?-imPoll=5000&-s=' + _session;
	zXPopupObj.row("Poll every 5 seconds","javascript:parent.zXRef('" + strUrl + "');","","",true);
	
	strUrl = '../' + SCRPT + '/zXLeft.' + SCRPT + '?-imPoll=30000&-s=' + _session;
	zXPopupObj.row("Poll every 30 seconds","javascript:parent.zXRef('" + strUrl + "');","","",false);

	strUrl = '../' + SCRPT + '/zXLeft.' + SCRPT + '?-imPoll=0&-s=' + _session;
	zXPopupObj.row("Dont poll","javascript:parent.zXRef('" + strUrl + "');","","",false);

	zXPopupObj.show();
}


//----
// imPollMessages
// 
// Check the messages outstanding for given user
//
// _image - handle to image showing the text message status
// _user - user-id 
// _session - zX session id
// _interval - Interval in milliseconds to next check
//
// This function will set intUnread and intImmediate
//----
function imPollMessages(_image, _user, _session, _interval) 
{
	var request = new XMLHttpRequest();

	request.onreadystatechange = 
			function processRequestChange() 
			{
				if(request.readyState == 4) 
				{ 
					if (request.responseText && request.status == 200)
					{
						//
						// The im file takes the form of valid Javascript code
						//
						eval(request.responseText);

						if (intUnread > 0)
						{
							_image.title = 'Unread = ' + intUnread;
							_image.src = '../images/textUnread.gif';
							
							if (window.ActiveXObject) 
							{
								_image.onmouseover = function() { zXIconFocus(this, '../images/textUnreadOver.gif'); }
								_image.onmouseout = function() { zXIconFocus(this, '../images/textUnread.gif'); }
							}
							else
							{
								strTmp = 'zXIconFocus(this, "../images/textUnreadOver.gif")';
								_image.setAttribute('onMouseOver', strTmp, 0);
								strTmp = 'zXIconFocus(this, "../images/textUnread.gif")';
								_image.setAttribute('onMouseOut', strTmp, 0);
							}

							if (intImmediate > 0)
							{
								
								if (intImmediate == 1) 
								{
									//
									// We only open message window when the last message we had a look at 
									// has a different PK; this is done to avoid opening a message window
									// for the same message as we already have a window open for
									//
									if (intNextMsgId != intLastMsgId)
									{
										intLastMsgId = intNextMsgId;
											
										imOpenImmediate(_session);
									}
								}
								else
								{ 
									imOpenMessageCentre(_session);
								}
							}
							
						}
						else
						{
							_image.title = 'No unread messages';
							_image.src = '../images/textRead.gif';

							if (window.ActiveXObject) 
							{
								_image.onmouseover = function() { zXIconFocus(this, '../images/textReadOver.gif'); }
								_image.onmouseout = function() { zXIconFocus(this, '../images/textRead.gif'); }
							}
							else
							{
								strTmp = 'zXIconFocus(this, "../images/textReadOver.gif")';
								_image.setAttribute('onMouseOver', strTmp, 0);
								strTmp = 'zXIconFocus(this, "../images/textRead.gif")';
								_image.setAttribute('onMouseOut', strTmp, 0);
							}
							
						}
						
						//----
						// Make sure we do not consume all the memory of this poor client
						//----
						request = null;
						if (window.CollectGarbage) {
							CollectGarbage();
						}
						
						//
						// And launch next check cycle
						//
						setTimeout("imPollMessages(findObj('" + _image.id + "'),'" +  _user + "','" +  _session + "'," + _interval + ");", _interval);
				
					} // End of responseText
					
				}  // End of readyState == 4
				
			} // End of anonymous function    
			
	request.open('GET', '../imTransient/' + _user + '.im');
	        
	if (window.ActiveXObject) 
	{ 
		request.send();
	} else 
	{
		request.send(null);
	}

}

function imOpenImmediate(_session)
{
 	var parms = "toolbar=no,location=no,left=0,top=0";
 		parms += ",screenX=50,screenY=50";
 		parms += ",width=850,height=500";
		parms += ",directories=no,status=no,menubar=no,resizable=yes";
	var strUrl;

	strUrl = "../" + SCRPT + "/im." + SCRPT + "?-pf=im/immediateUnread&-a=start&-s=" + _session;

  	var dtmTimeStamp = new Date();
	win = window.open(strUrl, "nkImWin_" + dtmTimeStamp.valueOf(), parms);
}

function imOpenMessageCentre(_session)
{
	//
	// Check if the message window is still open
	//
	var blnOpn = false;
	if (objMsgCntrWndw != undefined && objMsgCntrWndw != null) 
	{
		if (objMsgCntrWndw.closed != undefined) 
		{
			blnOpn = objMsgCntrWndw.closed;
		} 
		else 
		{
			blnOpn = false;
		}
	} 
	else 
	{
		blnOpn = true;
	}
	
	if (blnOpn) {					
	 	var parms = "toolbar=no,location=no,left=0,top=0";
	 		parms += ",screenX=50,screenY=50";
	 		parms += ",width=800,height=600";
			parms += ",directories=no,status=no,menubar=no,resizable=yes";
		var strUrl;
		
		strUrl = "../" + SCRPT + "/zXStdPopup." + SCRPT + "?-a=aspFrameset&-ss=" + zXSubSessionId() +
				 "&-pf=im/centre&-action=start&-s=" + _session;
		
		objMsgCntrWndw = window.open(strUrl, "nkImWin_MsgCntr", parms);
	}
}