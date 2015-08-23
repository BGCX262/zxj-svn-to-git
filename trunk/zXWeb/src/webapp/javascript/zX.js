//----
// What		:	zX.js
// Who		:	Bertus Dispa
// When		:	7SEP02
// Why		:	zX general functions
//
// Change	:	BD21MAR03
// Why		:	Added support for standard framework popup
//
// Change	:	BD27MAR03
// Why		:	Added dialog and msgBox popup window types
//
// Change	:	BD8APR03
// Why		:	Added zXCloseWindow* functions
//
// Change	:	BD15MAY03
// Why		:	- Changed zXMMShow so it can now also be used in framesets within the standard
//				zX frameset
//				- Added parent and grandparent openening
//
// Change	:	DGS15MAY03
// Why		:	- Standard Popups now use -zxpk rather than -pk to avoid conflict when used as non-popup.
//
// Change	:	BD22MAY03
// Why		:	- Added zXPrint function
//
// Change	:	BD26AUG03
// Why		:	- If -downLoad = 0 (zXPopupStep1) assume we do not want a download
//
// Change	:	DGS10OCT2003
// Why		:	New function refreshNavigationFrame based on existing tmsRefreshNavigationIcons for use 
//              by all apps (and is now called by the tms function).
//
// Change	:	DGS16OCT2003
// Why		:	Standard Popups zXStdPopup now has additional optional sixth parameter for setting the -vo (viewonly) flag in the url
//
// Change	:	DGS27NOV2003
// Why		:	Unlock from a standard popup (zXStdPopupUnlock) now executes the ASP in the calling frame's
//              parent window to give the ASP more chance of running. Otherwise get the odd lock left.
//
// Change	:	BD30NOV03
// Why		:	Added zXStdPopupResetTab
//
// Change	:	BD16FEB04
// Why		:	In zXStdPopup functions that refer to tabs by name, convert given name to lower case so that
//				callers do not need to be careful with case of tab names [change implemented by DGS on behalf of BD]
//
// Change	:	DGS11MAY2004
// Why		:	In zXSetFooter when calling setTimeout must wrap _txt in quotes
//
// When		:	14MAY04
// Why		:	Added better handling of scrollbars and other hidden frame visuals
//
// Change	:	DGS20MAY2004
// Why		:	In zXStdPopup make the opener the parent, not self
//
// When		:	BD23MAY04
// Why		:	Set opener for msgBox and dialog 
//
// When		:	BD26MAY04
// Why		:	Added zXSizedWindow; supported by sizedWindow frameNo where you can set width and height
//
// When		:	BD19JUN04
// Why		:	Added support for own sub-session in popup-inline
//
// Change	:	DGS22JUN2004
// Why		:	Major change to zXStdPopupUnlock to ensure unlocking always succeeds
//
// Change	:	BD16JUL04
// Why		:	Confirm when zXDirty set on zXStdPopupEndAction
//
// Change	:	DGS29JUL2004
// Why		:	Moved zXCatchBackSpace from zXForm.js to here, so that it is always available
//
// Change	:	DGS12AUG2004
// Why		:	Added optional parameter to zXWindow to set scrollbars (default 'no')
//
// Change	:	BD26AUG04
// Why		:	Added optional parameter to zXStdPopup, _popupDef
//
// Change	:	MB01NOV04
// Why		:	Added in browser independence code.
//
// Change	:	MB03NOV04
// Why		:	Allow for sharing of js between jsp and asp projects
//
// Change	:	V1.4:4 - BD7DEC04
// Why		:	Fixed bug in zXRef1; referred to incorrect frame
//
// Change	:	V1.4:24 - BD20JAN05
// Why		:	In standard popup, prompt user for confirmation when trying to
//					close standard popup if any of the tabs has zXDirty set
//
// Change	:	MB14MAR04
// Why	 	:   Using XHttpRequest to ensure clean up of Subsessions and unlocking
//				Also these method does not require a named window so people can login using zXStart2.html.
//
// Change	:	V1.4:57 - MB17MAR05
// Why		:	Fix bug in "zXDynPopup.prototype.show"; Popup menu does not size itself 
//			properly based on the stylesheet.
//
// Change	:	v1.5:??? DGS26APR2006
// Why		:	In zXMMToggleWholeMenu there is a new way of toggling IMG source.
//			zXIconFocusPath is a new function used by zXWeb menus.
//
//----

//--------------------
// Variable settings
//--------------------
var SCRPT;
if (SCRPT == undefined) {
	SCRPT = 'asp';
}
var imgDir = "../images/";

var zXDirty = 0;
var zXMMTreeOpen = 0;
var popupObj;
var ZXCONSTPOPUP_NONE = 0;		// Continue in same sub menu
var ZXCONSTPOPUP_START = 1;		// Start of popup menu
var ZXCONSTPOPUP_BREAK = 3;		// Break and start a new sub menu

// Browser Type Check :
isNS4 = (document.layers) ? true : false;
isIE4 = (document.all && !document.getElementById) ? true : false;
isIE5 = (document.all && document.getElementById) ? true : false;
isNS6 = (!document.all && document.getElementById) ? true : false;

//------
// Get handle to the parent window :
//------
var gSessionID;
var zXMainWindow;

if (gSessionID != undefined) {
	initMainWindow(true);
} else {
	// gSessionID = "CPAAdvanceMain";
}

//----
// Function	:	zXCatchBackSpace
//				Called onkeydown. Catches backspaces that are not within fields. 
//				If we don't stop this, a backspace is interpreted as back page, which we don't want.
// In		:	
// Out		:	Allow pressed key true / false
//
//----
function zXCatchBackSpace(e) {
	if (isIE4 || isIE5) {
		if (window.event.keyCode==8 && window.event.srcElement.type == undefined){
			return false;
		}
	} else {
		if (e.which == 8 && e.target.type == undefined) {
			return false;
		}
	}
}

//----
// Now invoke zXCatchBackSpace on key down:
//----
document.onkeydown = zXCatchBackSpace;
if (isNS4 || isNS6) {
	document.onkeypress = zXCatchBackSpace;
}

//=============================
// setCookie
// _name - name of cookie
// _value - value of cookie
// _date - expiry date
// _path - access path
// _domain - access domain
// _security - security crudentials
//=============================

function setCookie(_name, _value, _date, _path, _domain, _security) 
{
	var theCookie = _name + "=" + escape(_value);                                 	// Variable to hold the cookie string

  	if (_date)		theCookie += "; expires=" + _date.toGMTString();           // If an expire date was passed, then use it.
  	if (_path)      	theCookie += "; path=" + _path;                               	// If a path was passed, then use it.
  	if (_domain)    	theCookie += "; domain=" + _domain;                           	// If a domain was passed, then use it.
  	if (_security)  	theCookie += "; secure";                                      	// If this is for a secure connection, then request one.

  	document.cookie = theCookie;
}


//=============================
// getCookie
// _name - name of cookier to retrieve
// returns value of cookie
//=============================

function getCookie(_name) 
{
  	if (_name) 
	{
    		cookieArray = split(";",document.cookie);

    		for (var i=0; i<cookieArray.length; i++) 
		{
      			thisCookie = cookieArray[i];

      			if (thisCookie.indexOf(_name + "=") > -1) 
			{
        				var valueLoc = thisCookie.indexOf("=") + 1;
        				cookieValue = thisCookie.substring(valueLoc, thisCookie.length);
        				return unescape(cookieValue);
      			}
    		}
 	}

  	return false; 
}

//=============================
// split
// _delimiter - delimiter
// _string - the string
// 
// Split string up in tokens (delimited by _delimiter) and store in array
// returns array
//
//=============================

function split(_delimiter, _string) 
{
 	var theArray = new Array();
 
	while (_string != "") 
	{
    		endOfChunk = _string.indexOf(_delimiter);                                  	// Find the delimiter.

    		if (endOfChunk == -1) endOfChunk = _string.length;                   	// If delimiter not found, find end of string.
    		var thisChunk = _string.substring(0, endOfChunk);                       	// Get the next chunk of text.
    		_string = _string.substring(endOfChunk + 1, _string.length);    	 // Remove the chunk we got from the original string.
    		theArray[theArray.length] = thisChunk;                                      	// Add this chunk to the array.
  	}
  
	return theArray;
}

// zXIconFocus
//
// Icon gets / Looses focus, replace image with over /out version 
//
// _ctr	Handle to the control that gets the focus
// _img	Image to load

function zXIconFocus(_ctr, _img)
{
	_ctr.src = imgDir + _img;	
}

// zXIconFocusPath
//
// Icon gets / Loses focus, replace image with over /out version 
// v1.5:??? DGS26APR2006: New function used by zXWeb menus
//
// _ctr	Handle to the control that gets the focus
// _img	Image to load (with path - possibly relative)

function zXIconFocusPath(_ctr, _img)
{
	_ctr.src = _img;	
}

//==============================
// zXMMShow
// Session - session-id
// 
// Show the main menu by re-sizing the frameset, blanking out all frames except for the main menu one and
// calling action reset of zStart to refresh the session contexts
//==============================

function zXMMShow(_session) 
{
	if (parent.fraDetails1) {
		parent.fraDetails1.location='../' + SCRPT + '/zXLogin.' + SCRPT + '?-a=reset&-s=' + _session;
		parent.fraDetails2.location='../html/zXBlank.html';
		parent.fraDetails3.location='../html/zXBlank.html';
		parent.fraDetails4.location='../html/zXBlank.html';
		parent.fraDetails5.location='../html/zXBlank.html';
		
		parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','*,0,0,0,0,0');
	
		// IE4+ hide scroll bar
		parent.fraDetailsMenu.document.body.style.overflow='';
		
		// Non IE4+
		//parent.fraDetailsMenu.scrollbars.visibility = true;
		
		zXSetFooter('Make selection');
		
	} else {
		// Go to the main menu: 
		top.window.document.location.href = '../' + SCRPT + '/zXMainMenu.' + SCRPT + '?-a=show&-s=' + _session;
	}
	
	top.document.title = "Main menu";
	document.body.style.cursor="";
}

//----
// zXMMToggleWholeMenu
//		Open / close whole main menu tree
//
// BD17DEC02: more clever way of going through all DIV and IMF tags
// v1.5:95 DGS26APR2006: New way of toggling IMG source - we now hold the open and close
// image filenames in variables.
//----
function zXMMToggleWholeMenu()
{
	var i;
	var col;
	var strImgNme;
		
	if (isIE4) 
	{
		col = document.all.tags("DIV");
	} 
	else 
	{
		col = document.getElementsByTagName("DIV");
	}
	
	if (col != null) 
	{
	    for (i = 0; i < col.length; i++) 
		{
			if (col[i].id.match("_mnu_") )
			{
				if (zXMMTreeOpen == 1)
				{
					if(isNS4 || isIE4) 
					{
	            		col[i].style.visibility ="hidden";
		    		} 
		    		else 
		    		{ 
						col[i].style.display="none";
	        		}
				}
				else
				{
					if(isNS4 || isIE4)
					{
	            		col[i].style.visibility ="visible";
		    		} 
		    		else 
		    		{ 
						col[i].style.display="";
	        		}
				}
			}
		}
	}
	
	if (isIE4) 
	{
		col = document.all.tags("IMG");
	} 
	else 
	{
		col = document.getElementsByTagName("IMG");
	}
	
	if (col != null) 
	{
	    for (i = 0; i < col.length; i++) 
		{
			if (col[i].id.match("_mnuLvlImg_") )
			{
				// v1.5:??? DGS26APR2006: New way of toggling IMG source - 
				// get the open or close filename from a variable whose name
				// is the same as the image plus _OPEN or _CLOSED

				strImgNme = col[i].src;			
				if (zXMMTreeOpen == 1)
				{
					//col[i].src = "../images/menuClosed.gif";
					try {
						eval("strImgNme = "+col[i].id+"_CLOSED");
					} catch(e) {
						strImgNme = "../images/menuClosed.gif";
					}
				}
				else
				{
					//col[i].src = "../images/menuOpen.gif";
					try {
						eval("strImgNme = "+col[i].id+"_OPEN");
					} catch(e) {
						strImgNme = "../images/menuOpen.gif";
					}
				}
				col[i].src = strImgNme;				
			}
		}
	}
	
	if (zXMMTreeOpen == 1)
	{
		zXMMTreeOpen = 0;
	}
	else
	{
		zXMMTreeOpen = 1;
	}
}

//==============================
// zXSetFooter
// _txt - Text to set footer to
//
// Set the footer message span
// DGS04APR2003: If the footer doesn't exist yet (we can't control the timing of frames being created),
// introduce a half-second delay before trying it. Otherwise do it immediately.
// DGS09APR2003: Apparent IE6 problem - 'self' is not always the same as 'window' - use window.onerror
// DGS11MAY2004: When using setTimeout, must wrap _txt in quotes, as it is its value that is used
//==============================

function zXSetFooter(_txt) 
{
	window.onerror = function() { return true; };

	objFrame = top.fraFooter;
	objSpan = objFrame.document.getElementById("spnFooter");
	
	if (objSpan == null) {
 		setTimeout("top.fraFooter.document.getElementById('spnFooter').innerHTML = '"+_txt + "'",500);
	} else {
		objSpan.innerHTML =_txt;
	}

	return false;
}


//=============================
// zXOnPageStart
// Executed on start of body rendering
//=============================
function zXOnPageStart()
{
	document.body.style.cursor="wait";
}

//=============================
// zXOnPageEnd
// Executed on end of body rendering
// DGS30SEP2003: Apparent IE6 problem - 'self' is not always the same as 'window' - use window.onerror
//=============================
function zXOnPageEnd()
{
	var iFrames = 0;
	var iForms = 0;
	var iElements = 0;

	window.onerror = function() { return true; };

	//----
	// BD13DEC02
	// Hope this will solve the problem that occurs every now & then with the
	// hourglass staying on!
	//----
	 
	top.window.document.body.style.cursor = "";
	document.body.style.cursor="";

	for(iFrames=0;iFrames<parent.frames.length;iFrames++)
	{
		// Turn Hourglass off for body
		parent.frames[iFrames].document.body.style.cursor = "";
		for(iForms=0;iForms<parent.frames[iFrames].document.forms.length;iForms++)
		{
			// Turn Hourglass off for element
			for(iElements=0;iElements<parent.frames[iFrames].document.forms[iForms].elements.length;iElements++)
			{
				parent.frames[iFrames].document.forms[iForms].elements[iElements].style.cursor = "";
			}
		}
	}

	window.onerror = function() { return false; };

	return true;
}


// ============================
// zXRefx Functions
// - Load URL in specific / current frame
// - Load URL in specific / current frame if user confirms
// ============================


// Current frame version
function zXRef(_URL, _ctl) {
	//zXMaxThisFrame();
	if (_URL.charAt(0) == '#') {
		document.location.hash = _URL;
	} else {
		if (_ctl != undefined) {
			_ctl.disabled = true; 
		}
		document.location = _URL;
	}
	return false;
}

function zXRefConfirm(_URL, _msg, _ctl) {
	if (confirm(_msg)) zXRef(_URL, _ctl);
	return false;
}

function zXRefConfirmWhenDirty(_URL, _ctl) {
	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			zXRef(_URL, _ctl);
		}
	} else {
		zXRef(_URL, _ctl);
	}
	return false;
}


// Parent frame version
function zXRefParent(_URL, _ctl) {
	if (_ctl != undefined) {
		_ctl.disabled = true; 
	}
	parent.document.location = _URL;
	return false;
}

function zXRefConfirmParent(_URL, _msg, _ctl) {
	if (confirm(_msg))zXRefParent(_URL, _ctl);
	return false;
}

function zXRefConfirmWhenDirtyParent(_URL, _ctl) {
	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			zXRefParent(_URL, _ctl);
		}
	} else {
		zXRefParent(_URL, _ctl);
	}
	return false;
}

// Grand parent frame version
function zXRefGrandParent(_URL, _ctl) {
	if (_ctl != undefined) {
		_ctl.disabled = true; 
	}
	parent.parent.document.location = _URL;
	return false;
}

function zXRefConfirmGrandParent(_URL, _msg, _ctl) {
	if (confirm(_msg)) zXRefGrandParent(_URL, _ctl);
	return false;
}

function zXRefConfirmWhenDirtyGrandParent(_URL, _ctl) {
	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			zXRefGrandParent(_URL, _ctl);
		}
	} else {
		zXRefGrandParent(_URL, _ctl);
	}
	return false;
}


// Numbered frame version. Reason for not making this parameter driven is a trick in pageflow that is used often to
// alter the frame on already generated HTML

// ==== 1

function zXRef1(_URL) {
	if (parent.fraDetails1) {
		parent.fraDetails1.document.location = _URL;
	} else if (top.fraDetails1){
		top.fraDetails1.document.location = _URL;
	} else if (window.fraDetails1){
		window.fraDetails1.document.location = _URL;
	}
	
	return false;
}


function zXRefConfirm1(_URL, _msg) {
	if (confirm(_msg)) zXRef1(_URL);
	return false;
}


function zXRefConfirmWhenDirty1(_URL) {

	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			zXRef1(_URL);
		}
	} else {
		zXRef1(_URL);
	}
	return false;
}


// ==== 2
 
function zXRef2(_URL) {
	if (parent.fraDetails2) {
		parent.fraDetails2.document.location = _URL;
	} else if (top.fraDetails2){
		top.fraDetails2.document.location = _URL;
	} else if (window.fraDetails2){
		window.fraDetails2.document.location = _URL;
	}
	
	return false;
}


function zXRefConfirm2(_URL, _msg) {
	if (confirm(_msg)) zXRef2(_URL);
	return false;
}


function zXRefConfirmWhenDirty2(_URL) {
	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			zXRef2(_URL);
		}
	} else {
		zXRef2(_URL);
	}
	return false;
}

// ==== 3
 
function zXRef3(_URL) {
	if (parent.fraDetails3) {
		parent.fraDetails3.document.location = _URL;
	} else if (top.fraDetails3){
		top.fraDetails3.document.location = _URL;
	} else if (window.fraDetails3){
		window.fraDetails3.document.location = _URL;
	}
	
	return false;
}


function zXRefConfirm3(_URL, _msg) {
	if (confirm(_msg)) zXRef3(_URL);
	return false;
}


function zXRefConfirmWhenDirty3(_URL) {
	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			zXRef3(_URL);
		}
	} else {
		zXRef3(_URL);
	}
	return false;
}

// ==== 4
 
function zXRef4(_URL) {
	if (parent.fraDetails4) {
		parent.fraDetails4.document.location = _URL;
	} else if (top.fraDetails4){
		top.fraDetails4.document.location = _URL;
	} else if (window.fraDetails4){
		window.fraDetails4.document.location = _URL;
	}
	
	return false;
}


function zXRefConfirm4(_URL, _msg) {
	if (confirm(_msg)) zXRef4(_URL);
	return false;
}


function zXRefConfirmWhenDirty4(_URL) {
	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			zXRef4(_URL);
		}
	} else {
		zXRef4(_URL);
	}
	return false;
}

// ==== 5
 
function zXRef5(_URL) {
	if (parent.fraDetails5) {
		parent.fraDetails5.document.location = _URL;
	} else if (top.fraDetails5){
		top.fraDetails5.document.location = _URL;
	} else if (window.fraDetails5){
		window.fraDetails5.document.location = _URL;
	}
	
	return false;
}


function zXRefConfirm5(_URL, _msg) {
	if (confirm(_msg)) zXRef5(_URL);
	return false;
}


function zXRefConfirmWhenDirty5(_URL) {
	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			zXRef5(_URL);
		}
	} else {
		zXRef5(_URL);
	}
	return false;
}


function zXCloseWindow()
{
	top.window.close();
}

function zXCloseWindowConfirm(_msg)
{
	if (confirm(_msg)) 
	{
		top.window.close();	
	}
	return false;
}

function zXCloseWindowConfirmWhenDirty()
{
	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			top.window.close();
		}
	} else
	{
		top.window.close();
	}
	return false;
}

function zXCloseFrames()
{
	parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,*,0,0,0,0');
}

function zXRefreshFrame1()
{
	parent.fraDetails1.document.location = parent.fraDetails1.document.location;
}

function zXCloseFramesConfirmWhenDirty()
{
	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,*,0,0,0,0');
		}
	} else
	{
		parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,*,0,0,0,0');
	}
}

//=============================
// zXSetDirty
//
// Sert dirty flag on edit page to force confirmation message 
// when leaving page without submitting
//=============================

function zXSetDirty() 
{
	zXDirty = 1;
	return false;
}


// Make sure that this frame has the maximum size
function zXMaxThisFrame()
{
	if (self == parent.fraDetails1) 
	{
		parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,*,0,0,0,0');
	}
	else if (self == parent.fraDetails2) 
	{
		parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,*,0,0,0');
	}
	else if (self == parent.fraDetails3) 
	{
		parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,*,0,0');
	}
	else if (self == parent.fraDetails4) 
	{
		parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,0,*,0');
	}
	else if (self == parent.fraDetails5) 
	{
		parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,0,0,0,0,*');
	}
}

//=============================
// zXPopup
// _url - Load URL in a full-size popup window
//=============================

function zXPopup(_url) 
{
	var intHeight;
	var intWidth;

	intHeight=500;
	intWidth=700;

 	var parms = "toolbar=no,location=no,left=0,top=0";
 		parms += ",screenX=50,screenY=50";
 //		parms += ",height=" + intHeight;
 //		parms += ",width=" + intWidth;
		parms += ",directories=no,status=no,menubar=no,scrollbars=no,copyhistory=no,resizable=yes";

  	var dtmTimeStamp = new Date();

  	win = parent.window.open(_url, "Win" + dtmTimeStamp.valueOf(), parms);
}  

//=============================
// zXWindow
// _url - Load URL in a half-size popup window
//
// DGS12AUG2004: New optional second parameter to set scrollbars (default 'no')
//=============================

function zXWindow(_url,_scrollbars) 
{
	var intHeight;
	var intWidth;
	var strScrollbars;

	intHeight=500;
	intWidth=500;
	strScrollbars="no";

	if(_scrollbars != undefined)
	{
		strScrollbars = _scrollbars;
	}

 	var parms = "toolbar=no,location=no,left=0,top=0";
 		parms += ",screenX=50,screenY=50";
 		parms += ",height=" + intHeight;
 		parms += ",width=" + intWidth;
		parms += ",directories=no,status=no,menubar=no,scrollbars="+strScrollbars+",copyhistory=no,resizable=yes";

  	var dtmTimeStamp = new Date();

  	win = parent.window.open(_url, "Win" + dtmTimeStamp.valueOf(), parms);
  	win.opener = self;
}  

//=============================
// zXSizedWindow
// _url - Load URL in a popup window and allow to set size
//=============================

function zXSizedWindow(_url, _width, _height) 
{
	var intHeight;
	var intWidth;

	intHeight=500;
	intWidth=500;

	if(_width != undefined)
	{
		intWidth = _width;
	}

	if(_height != undefined)
	{
		intHeight = _height;
	}

 	var parms = "toolbar=no,location=no,left=0,top=0";
 		parms += ",screenX=50,screenY=50";
 		parms += ",height=" + intHeight;
 		parms += ",width=" + intWidth;
		parms += ",directories=no,status=no,menubar=no,scrollbars=yes,copyhistory=no,resizable=yes";

  	var dtmTimeStamp = new Date();

  	win = parent.window.open(_url, "Win" + dtmTimeStamp.valueOf(), parms);
  	win.opener = self;
}  


//=============================
// zXDialog
// _url - Load URL in a dialog style popup window
//=============================

function zXDialog(_url) 
{
	var intHeight;
	var intWidth;

	intHeight=250;
	intWidth=400;

 	var parms = "toolbar=no,location=no,left=0,top=0";
 		parms += ",screenX=50,screenY=50";
 		parms += ",height=" + intHeight;
 		parms += ",width=" + intWidth;
		parms += ",directories=no,status=no,menubar=no,scrollbars=no,copyhistory=no,resizable=yes";

  	var dtmTimeStamp = new Date();

  	win = parent.window.open(_url, "Win" + dtmTimeStamp.valueOf(), parms);

	win.opener = self;
}  


//=============================
// zXMsgBox
// _url - Load URL in a msgBox style popup window
//=============================

function zXMsgBox(_url) 
{
	var intHeight;
	var intWidth;

	intHeight=150;
	intWidth=250;

 	var parms = "toolbar=no,location=no,left=0,top=0";
 		parms += ",screenX=50,screenY=50";
 		parms += ",height=" + intHeight;
 		parms += ",width=" + intWidth;
		parms += ",directories=no,status=no,menubar=no,scrollbars=no,copyhistory=no,resizable=yes";

  	var dtmTimeStamp = new Date();

  	win = parent.window.open(_url, "Win" + dtmTimeStamp.valueOf(), parms);

	win.opener = self;
}  

//=============================
// zXPopupStep1
//
// The first step in the popup process: store url in cookie and
// open window where we load a new base frameset
//
// _session - Session
// _url - Load URL in popup window
// _noNavigationFrame - Optional indicator that no navigation frame is required
//=============================

function zXPopupStep1(_session, _url, _noNavigationFrame, _downLoad) 
{
 	var strParms = "toolbar=no,location=no,left=0,top=0";
 		strParms += ",screenX=50,screenY=50";
		// DGS24JAN2003: Previously availHeight - 150.
 		strParms += ",height=" + (screen.availHeight - 100);
 		strParms += ",width=" + (screen.availWidth - 50);
		strParms += ",directories=no,status=no,menubar=no,scrollbars=yes,copyhistory=no,resizable=yes";

  	var datTimeStamp = new Date();
	var strUrl;
	var objWin;
	var strSubsession;
	

	if (_url.indexOf('-ss') == -1)
	{
		strSubsession = zXSubSessionId();
		_url += "&-ss=" + strSubsession;
	}

  	setCookie("zXPopupUrl", _url, "", "/");

	strUrl = '../' + SCRPT + '/zXPopup.' + SCRPT + '?-s=' + _session + "&-ss=" + strSubsession;

	if (_noNavigationFrame != undefined)
	{
		strUrl += "&-noNavigationFrame=1";
	}
	
	if (_downLoad != undefined)
	{
		if ( _downLoad != '0') 
		{
			strUrl += "&-downLoad=1";
		}
	}
		
   	objWin = parent.open(strUrl, 
   						"Win" + datTimeStamp.valueOf(), 
   						strParms);
	objWin.opener = self;

	document.body.style.cursor="";
	
	return false;
}  

function zXPopupStep2()
{
	var urlToLoad = getCookie("zXPopupUrl");

	if (isNS6) {
		document.location = urlToLoad;
	} else {
		parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,*,0,0,0,0,0');
		parent.fraDetails1.document.location = urlToLoad;
	}
	
	if (window.focus) {
		window.focus();
	}
}

//
// Reload document
//
function zXReload()
{
	document.location.reload(true);
}


//----
// zXSubSessionId
//		Generate 'unique' id to be used to indicate a subsession
//
//----
function zXSubSessionId()
{
	var strReturn;
	var datDate;
	var length;
	datDate = new Date();
	
	strReturn = datDate.getTime() + '';
	
	// Make it safe for vb longs.
	length = strReturn.length;
	if (length > 9) strReturn = strReturn.substring(length-9, length);
	
	return strReturn;
}

//----
// zXCloseSubSession
//		Close a subsession
//
// _session - Session
// _subSession - Subsession that you want to close
//----
function zXCloseSubSession(_session, 
						_subSession)
{
	var strUrl;
	
	if (_subSession != "") {
		
		strUrl = "../" + SCRPT + '/zXLogin.' + SCRPT + "?-a=aspCloseSubSession";
		strUrl += "&-s=" + _session;
		strUrl += "&-ss=" + _subSession;
		
		var req = new XMLHttpRequest();
		if (req && false) {
			if(callInProgress(req)) {
				req.abort();
				req = new XMLHttpRequest();
			}
			
			req.open("GET", strUrl + "&" + new Date().getTime(), false);
			
			if (window.ActiveXObject) { 
				req.send();
			} else {
				req.send(null);
			}
						
			if (isNS6) delay(100);
				
		} else {
			var win = window.open("", gSessionID);
			if (win.fraDetails5 != undefined) {
				win.fraDetails5.document.location = strUrl;
			} else {
				// This actually replaces the url in NON-ie browsers.
				document.location.href = strUrl;
				window.setInterval("strUrl = '';", 50);
			}
		}
	}
	
	return false;	
}

//----
// zXSelectInSelectList
//		Select an item in a select list by its value
//
// _select - Handle to the select control
// _value - Value to look for
//
function zXSelectInSelectList(_select, _value)
{
	var i;

	for (i = 0; i < _select.options.length; ++i)
	{
		if (_select.options[i].value == _value)
		{
			_select.options[i].selected = true;
		}
	}
}

//----
// zXExportList
//		Export the result set shown on a list form to a csv delimited 
//		file
//
// _session - Session
// _ss - Sub-session id
// _pf - Name of pageflow
// _action - Name of list action
//----
function zXExportList(_session, _ss, _pageflow, _action)
{
	var strUrl;
	
	strUrl = "../" + SCRPT + "/zXExprtLst." + SCRPT + "?-a=aspStart";
	strUrl += "&-s=" + _session;
	strUrl += "&-ss=" + _ss;
	strUrl += "&-pf=" + _pageflow;
	strUrl += "&-action=" + _action;

	zXPopup(strUrl);
}

//--------------------  BEGIN : CREATEPOPUP HACK
var _e;
var yyy=-1;
var Xoffset=-1;

if (window.createPopup == undefined) {
	document.write ("<span ID='createpopup' style='' onClick='this.style.visibility =\"hidden\";'></span>\n");
	
	document.addEventListener('mousedown', setE, true);
	document.captureEvents(Event.MOUSEDOWN);
	//document.onmousedown= setE;
}
function setE(e) {
	_e = e;
}

function zXDynPopupHide() {
	if(isNS4) 
		popupObj.visibility="hidden";
	else if (isNS6)	
		popupObj.display="none";
}

//----
// Which window/frame should this popup menu appear.
//----
zXDynPopup.prototype.setWindow = function(_window)
{
	this.window = _window;
}

//----
// zXDynPopup
//		Dynamic popup constructor class
//
function zXDynPopup()
{
	if (window.createPopup == undefined) {
		// NN hacks - Default to self :
		this.window = window;
	}
	
	this.arrURLs = new Array();
}

//----
// zXDynPopup.prototype.row
//		Add a row to a dynamic popup object (class method)
//
zXDynPopup.prototype.row = function(_label,_url,_img,_imgOver,_startSubMenu)
{
	var intRow = this.arrURLs.length;

	this.arrURLs[intRow] = new Object();

	this.arrURLs[intRow].label = _label;
	this.arrURLs[intRow].url = _url;
	this.arrURLs[intRow].img = _img;
	this.arrURLs[intRow].imgOver = _imgOver;
	this.arrURLs[intRow].startSubMenu = _startSubMenu;
}

//----
// zXDynPopup.prototype.show
//		Shows a dynamic popup (class method)
//
zXDynPopup.prototype.show = function()
{
	var str;
	var realHeight;
	var realWidth;
	var rowStyle;

	if (this.arrURLs.length == 0)
		return;
		
	if (window.createPopup) {	
		popupObj = window.createPopup();

		str = "<html>";
		str += "<body>";
		str += "<table bgcolor='White' cellspacing ='0' cellpadding='0' >";
		str += "<tr>";
		str += "<td onMouseOver='javascript:parent.popupObj.hide();'><image src='" + imgDir + "spacer.gif' width='1' height='2'></td>";
		str += "<td onMouseOver='javascript:parent.popupObj.hide();'><image src='" + imgDir + "spacer.gif' ></td>";
		str += "<td onMouseOver='javascript:parent.popupObj.hide();'><image src='" + imgDir + "spacer.gif' width='2'></td>";
		str += "</tr>";
		str += "<tr>";
		str += "<td onMouseOver='javascript:parent.popupObj.hide();'><image src='" + imgDir + "spacer.gif'></td>";
		str += "<td>";
		str += "<table bgcolor='Black' cellspacing ='2' cellpadding='0' >";

	} else {
	
		if (isNS4) {
			popupObj = this.window.document.createpopup;
		} else {
			popupObj = this.window.document.getElementById("createpopup").style;
		}
		
		// NN hacks - Set popupObj on the window it is going to be displayed on ..
		this.window.popupObj = popupObj;
		
		str = "";
		
		str += "<table bgcolor='White' cellspacing ='0' cellpadding='0' >";
		str += "<tr>";
		str += "<td onMouseOver='javascript:zXDynPopupHide();'><image src='" + imgDir + "spacer.gif' width='1' height='2'></td>";
		str += "<td onMouseOver='javascript:zXDynPopupHide();'><image src='" + imgDir + "spacer.gif' ></td>";
		str += "<td onMouseOver='javascript:zXDynPopupHide();'><image src='" + imgDir + "spacer.gif' width='2'></td>";
		str += "</tr>";
		str += "<tr>";
		str += "<td onMouseOver='javascript:zXDynPopupHide();'><image src='" + imgDir + "spacer.gif'></td>";
		str += "<td>";
		str += "<table bgcolor='Black' cellspacing ='2' cellpadding='0' >";
		
	}
	
	str += "<table bgcolor='Black' cellspacing ='2' cellpadding='0' >";

	for (var i = 0; i < this.arrURLs.length; i++)
	{
		if (i == 0)
		{
			// Force a new start for the first row, regardless of what was requested:
			rowStyle = ZXCONSTPOPUP_START;
		}
		else
		{
			if (this.arrURLs[i].startSubMenu == true)
			{
				rowStyle = ZXCONSTPOPUP_BREAK;
			}
			else
			{
				rowStyle = ZXCONSTPOPUP_NONE;
			}
		}
		
		str += zXPopupOption(this.arrURLs[i].label,this.arrURLs[i].url,this.arrURLs[i].img,this.arrURLs[i].imgOver,rowStyle);
	}

	//----
	// End the last sub-menu
	//----
	str += "</table>";
	
	if (window.createPopup) {
		str += "</td></tr>";
		str += "</table>";
		str += "</td>";
		str += "<td onMouseOver='javascript:parent.popupObj.hide();'><img src='" + imgDir + "spacer.gif'></td>";
		str += "</tr>";
		str += "<tr>";
		str += "<td onMouseOver='javascript:parent.popupObj.hide();'><img src='" + imgDir + "spacer.gif' height='2'></td>";
		str += "<td onMouseOver='javascript:parent.popupObj.hide();'><img src='" + imgDir + "spacer.gif'></td>";
		str += "<td onMouseOver='javascript:parent.popupObj.hide();'><img src='" + imgDir + "spacer.gif'></td>";
		str += "</tr>";
		str += "</table>";
		str += "</body>";
		str += "</html>";
		
	} else {
		str += "</td></tr>";
		str += "</table>";
		str += "</td>";
		str += "<td onMouseOver='javascript:zXDynPopupHide();'><img src='" + imgDir + "spacer.gif'></td>";
		str += "</tr>";
		str += "<tr>";
		str += "<td onMouseOver='javascript:zXDynPopupHide();'><img src='" + imgDir + "spacer.gif' height='2'></td>";
		str += "<td onMouseOver='javascript:zXDynPopupHide();'><img src='" + imgDir + "spacer.gif'></td>";
		str += "<td onMouseOver='javascript:zXDynPopupHide();'><img src='" + imgDir + "spacer.gif'></td>";
		str += "</tr>";
		str += "</table>";
		
	}
	
//alert(str);
	
	// NN hacks - Display the popup
	if (window.createPopup) {
		popupObj.document.body.innerHTML = str;
		popupObj.show(0, 0, 0, 0);
		
		realHeight = popupObj.document.body.scrollHeight;
		realWidth = popupObj.document.body.scrollWidth;
		
		popupObj.hide();
		
		popupObj.show(window.event.x - 10, window.event.y - (realHeight - 10), realWidth, realHeight, document.body);
	} else {
		if(isNS4) { 
	 		popupObj.document.write(str);
	 		popupObj.document.close();
	 		popupObj.visibility="visible";
	 		
	 	} else {
	 		this.window.document.getElementById("createpopup").innerHTML=str;
	 		popupObj.display='';
	 		popupObj.visibility="visible";
	 	}
	 	
	 	var x=_e.pageX;
		var y=_e.pageY;
	 	if (window == this.window || top.iframe) {
			popupObj.top=y+yyy-5;
	 		popupObj.left=x+Xoffset-5;
	 	} else {
	 		popupObj.left=Xoffset;
			popupObj.top=y;
	 	}
	}
}


//----
// zXPopupOption
//		Generate one row in a dynamic popup. The label and images are optional, so that any
//      combination of image and label can be shown (although neither wouldn't be sensible)
//
// _label - Label to show for this row
// _url - URL to go to on click
// _img - image to be shown for the row
// _imgOver - alternative image on mouse over
// _strtSub - whether or not to start a new sub-menu (affects the appearance)
// _endPrevSub - whether or not need to end the previous sub-menu
//
function zXPopupOption(_label, _url, _img, _imgOver, _rowStyle)
{
	var str = "";

	if (_rowStyle == ZXCONSTPOPUP_BREAK)
	{
		//----
		// End the previous sub-menu:
		//----
		str += "</table></td></tr>";
	}
	
	if (_rowStyle == ZXCONSTPOPUP_NONE)
	{
		//----
		// If not BREAK (ending a sub-menu and starting another), and this is not START (the start of 
		// a new sub menu), draw a separator line under the previous option:
		//----
		str += "<tr height='1' bgcolor='Gray'><td> </td></tr>";
	}
	else
	{
		//----
		// Start a new sub-menu if BREAK (ending previous and starting another) 
		// or START (starting brand new):
		//----
		if (window.createPopup) {
			str += "<tr><td style='" + getStyle(".zxPopupMenuAlt").cssText + "'><table width='100%' >";
		} else {
			str += "<tr><td class='zxPopupMenuAlt'><table width='100%' >";
		}
	}
	
	str += "<tr>";

	//----
	// Now show the menu line details
	//----
	if (window.createPopup) {
		str += "<td nowrap style='" + getStyle(".zxPopupMenuAlt").cssText + "'" +
				" onMouseOver='javascript:parent.zXOnMouseOverPopupOption(this);'" +
				" onMouseOut='javascript:parent.zXOnMouseOutPopupOption(this);'" +
				" onClick=\"javascript:parent.popupObj.hide();" + _url + ";\"" +
				">";
	} else {
		str += "<td nowrap class='zxPopupMenuAlt'" +
				" onMouseOver='javascript:zXOnMouseOverPopupOption(this);'" +
				" onMouseOut='javascript:zXOnMouseOutPopupOption(this);'" +
				// HACK - Set parent to selt to fool the generated js and then back again.
				" onClick=\"javascript:zXDynPopupHide();origParent=parent;parent=window.self;" + _url + ";parent=origParent;\"" +
				">";
	}

	//----
	// If an image URL has been given, show the image at the start of the cell
	//----
	if (_img != "")
	{
		//----
		// If no 'over' image, just use the same one
		//----
		if (_imgOver == "")
		{
			_imgOver = _img;
		}

		str += "<img src='" + _img + "'" +
			" onMouseOver=\"javascript:this.src='" + _imgOver + "';\"" +
			" onMouseOut=\"javascript:this.src='" + _img + "';\"" +
			">";
	}

	if (_label != "")
	{
		str += "&nbsp;" + _label + "&nbsp;";
	}
	
	str += "</td>";
	str += "</tr>";

	return str;
}

//----
// zXOnMouseOverPopupOption
// zXOnMouseOutPopupOption
//		These two functions are called from the HTML inside the pop-up menu window and are used
// to visualise the selection of one of the options
//
// _ctr - Handle to <td> cell
//----
function zXOnMouseOverPopupOption(_ctr)
{
	if (window.createPopup) {
		_ctr.style.cssText = getStyle(".zxPopupMenuNor").cssText;
	} else {
		_ctr.className = "zxPopupMenuNor";
	}
}

function zXOnMouseOutPopupOption(_ctr)
{
	if (window.createPopup) {
		_ctr.style.cssText = getStyle(".zxPopupMenuAlt").cssText;
	} else {
		_ctr.className = "zxPopupMenuAlt";
	}
}

//--------------------  END : CREATEPOPUP HACK

//=============================
// zXStdPopup
//
// The standard framework popup window
//
// _session - Session
// _pf - Name of pageflow where popup definition can be found
// _action - Name of action of popup definition (within pageflow)
// _pk - primary key of the entity to load
// _width - Optional width in pixels
// _height - Optional height in pixels
// _viewonly - Optional viewonly flag ('0' or '1' expected) - results in -vo querystring item in url
// _popupDef - Optional name of action that is the popup definition to use
//=============================
function zXStdPopup(_session, _pf, _action, _pk, _width, _height, _viewonly, _popupDef) 
{
	// HACK -- TODO : Replace
	var winHack;
	if (window.createPopup == undefined) {
		winHack = parent;
		parent = window.self;
	}
	
	var strSubsession;
	var objWin;
	var strUrl;
 	var strParms;
 	
	strParms = "toolbar=no,location=no,left=0,top=0";
 	strParms += ",screenX=50,screenY=50";
	strParms += ",directories=no,status=no,menubar=no,scrollbars=yes";
	strParms += ",copyhistory=no,resizable=yes";

	if (_height == undefined)
	{
		_height = '';
	}
	if (_height == '')
	{
 		strParms += ",height=" + (screen.availHeight - 100);
 	}
 	else
 	{
 		strParms += ",height=" + _height;
 	}
 	
	if (_width == undefined)
	{
		_width = '';
	}
	if (_width == '')
	{
 		strParms += ",width=" + (screen.availWidth - 100);
 	}
 	else
 	{
 		strParms += ",width=" + _width;
 	}

	strSubsession = zXSubSessionId();

	strUrl = "../" + SCRPT + "/zXStdPopup." + SCRPT + "?-s=" + _session;
	strUrl += "&-a=aspFrameset";
	strUrl += "&-ss=" + strSubsession;
	strUrl += "&-pf=" + _pf;
	strUrl += "&-action=" + _action;
	strUrl += "&-zxpk=" + _pk;

	if (_viewonly != undefined)
	{
 		strUrl += "&-vo=" + _viewonly;
 	}

	if (_popupDef != undefined)
	{
 		strUrl += "&-zXPopupDef=" + _popupDef;
 	}
 	
   	objWin = parent.open(strUrl, 
   					"Win" + strSubsession, 
   					strParms);

// DGS20MAY2004: make opener the parent - if we are in a dynamic popup, 'self' will be wrong
//		objWin.opener = self;
		objWin.opener = parent;

	document.body.style.cursor="";
	
	// HACK ??? TODO - Replace
	if (window.createPopup == undefined) {
		parent = winHack;
	}
	
	return false;
}  



//======
// zXStdPopupInline
//
// Very similar to zXStdPopup but now does not create popup window so it can be
// used to load a standard zX popup in an existing window or frame
//
// _ownSubSession: can be handy when you already have a subsession so you can
//			share queries between the calling window and the stdPopup environment
//======
function zXStdPopupInline(_session, _pf, _action, _pk, _ownSubSession) 
{
	var strSubsession;
	var objWin;
	var strUrl;
 	var strParms;
 	
	if (_ownSubSession == undefined)
	{
		strSubsession = zXSubSessionId();
	}
	else
	{
		strSubsession = _ownSubSession;
	}
	
	strUrl = "../" + SCRPT + "/zXStdPopup." + SCRPT + "?-s=" + _session;
	strUrl += "&-a=aspFrameset";
	strUrl += "&-ss=" + strSubsession;
	strUrl += "&-pf=" + _pf;
	strUrl += "&-action=" + _action;
	strUrl += "&-zxpk=" + _pk;

	document.location = strUrl;
	document.body.style.cursor="";
	
	return false;
}  

//=============================
// zXStdPopupUnlock
//
// Standard popup window to unlock an entity
//
// _s = Session
// _ss = Sub-session
// _entity = Entity
// _pk = Primary key
//=============================
function zXStdPopupUnlock(_session, _ss, _entity, _pk)
{
	var strUrl;
	
	if (_ss != "") {
		
		strUrl = "../" + SCRPT + "/zXStdPopup." + SCRPT + "?-a=aspUnlock";
		strUrl += "&-s=" + _session;
		strUrl += "&-ss=" + _ss;
		strUrl += "&-entity=" + escape(_entity);
		strUrl += "&-zxpk=" + _pk;
		
		//----
		// NOTE : "fraDetails5" has to exist.
		//----
		initMainWindow(false);
		if (zXMainWindow && zXMainWindow.top.fraDetails5 != undefined) {
			//----
			// This is the preferred method as it is not visible.
			//----
			zXMainWindow.top.zXCallUrl(strUrl);
			
		} else {
			//-----
			// Either the main window is closed or we do not have a handle to it anymore.
			// or "fraDetails5" does not exist
			//-----
			var req = new XMLHttpRequest();
			if (req && false) {
				// Stop any current requests
				if(req && callInProgress(req)) {
			           req.abort();
			           req = new XMLHttpRequest();
			    }
			    
				req.open("GET", strUrl + "&" + new Date().getTime(), false);
				
				if (window.ActiveXObject) { 
					req.send();
				} else {
					req.send(null);
					delay(100);
				}
				
			} else {
				//----
				// Browser does  not support XMLHttpRequest and the Main window has been closed.
				//
				// DGS27NOV2003: Execute this in the popup's main window, not the footer that is being
				// closed. It seems to help ensure the asp actually gets run to perform the unlock.
				// DGS22JUN2004: Major change. Execute it in a hidden frame of the main window, as this
				// should always exist and thus ensure locks are always released. If the main window has
				// been closed, this script will momentarily show another window while unlocking.
				//----
			 	var parms = "toolbar=no,location=no,left=0,top=0";
		 		parms += ",screenX=0,screenY=0";
		 		parms += ",width=5,height=5,close=no";
				parms += ",directories=no,status=no,menubar=no,resizable=no";
				win = window.open("", gSessionID, parms);
				
				if (win.location.href != "about:blank") {
					win.top.zXCallUrl(strUrl);
				} else {
					window.location.href = strUrl + "&-close=1";
				}
			}
		}
	}
}

//=============================
// zXStdPopupRefresh*
//
// Functions to refresh header, navigation or user frame in a standard popup window
//
// Calling this function only makes sense from within a standard popup window
//=============================
function zXStdPopupRefreshHeader()
{
	parent.fraTabButtons.zXStdPopupRefreshHeader();
}

function zXStdPopupRefreshNavigation()
{
	parent.fraTabButtons.zXStdPopupRefreshNavigation();
}

function zXStdPopupRefreshTab(_strTab)
{
	eval('parent.fraTabButtons.zXStdPopupRefreshTab(parent.fra' + _strTab.toLowerCase() + ');');
}

function zXStdPopupResetTab(_strTab)
{
	eval('parent.fraTabButtons.zXStdPopupResetTab(parent.fra' + _strTab.toLowerCase() + ');');
}


function zXStdPopupClickTab(_strTab)
{
	eval('parent.fraTabButtons.zXStdPopupClickTab(\'' + _strTab.toLowerCase() + '\');');
}

function zXStdPopupScroll(_x, _y)
{
	parent.parent.fraTabButtons.frazXActiveFrame.scrollTo(_x, _y);
}

function zXStdPopupClose()
{
	parent.fraTabButtons.zXStdPopupClose();
}

function zXStdPopupCheck()
{
	return parent.fraTabButtons.zXStdPopupCheck();
}

function zXStdPopupEndAction(_s, _ss)
{
	var intDo;
	intDo = 0;
	if (zXDirty != 0) {
		if (confirm("Ok to discard any changes?")) {
			intDo = 1;
		}
	} else
	{
		intDo = 1;
	}
	
	if (intDo == 1)
	{
		//Must change the active tab's class from deactive or the click will be ignored:
		parent.fraTabButtons.tabzXActiveTab.className = "zxTabInactive";

		// Blank out the last action
		parent.frazxdoaction.document.location = "../html/zXBlank.html";

		//Now simulate clicking the active tab:
		parent.fraTabButtons.zXStdPopupHandleTabClick(parent.fraTabButtons.tabzXActiveTab,_s,_ss);
	}
}

//--------------------  BEGIN : CREATEPOPUP HACK
//=============================
// zXPopupTxt
//
// Function to present text messaging options in a popup menu
//
//=============================
function zXPopupTxt(_session)
{
		var zXPopupObj;
		var strFrameRows;
		
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
		
		zXPopupObj.row("Show text messages","parent.zXPopupStep1('" + _session + "','../" + SCRPT + '/zXGPF.' + SCRPT +"?-pf=zXTxt&-s=" + _session + "',1);","","",true);
		zXPopupObj.row("Send new text message","parent.zXPopupStep1('" + _session + "','../" + SCRPT + '/zXGPF.' + SCRPT + "?-pf=zXTxt&-a=efNew&-sa=editnodb&-s=" + _session + "',1);","","",true);
		
		zXPopupObj.show();
}

//=============================
// getActiveFrame
//
// Util function to get the current active frame of the main frame.
//
//=============================
function getActiveFrame() {
	var getActiveFrame;
	var strFrameRows;
	
	strFrameRows = parent.document.getElementsByTagName('frameset')[2].getAttribute('rows');
	if (strFrameRows == "*,0,0,0,0,0") {
		getActiveFrame = top.fraDetailsMenu;
	}	else if (strFrameRows == "0,*,0,0,0,0") {
		getActiveFrame = top.fraDetails1;
	}	else if (strFrameRows == "0,0,*,0,0,0") {
		getActiveFrame = top.fraDetails2;
	}	else if (strFrameRows == "0,0,0,*,0,0") {
		getActiveFrame = top.fraDetails3;
	}	else if (strFrameRows == "0,0,0,0,*,0") {
		getActiveFrame = top.fraDetails4;
	}	else if (strFrameRows == "0,0,0,0,0,*") {
		getActiveFrame = top.fraDetails5;
	}
	
	return getActiveFrame;
}
//--------------------  END : CREATEPOPUP HACK

//======
// zXPrintRequest
//
// Function that will load the pageflow that will ask for a printer (if needed) and
// add a row to the document builder request table
//
// _s - Session
// _type - Type of print
// _tmplt - Template to use
// _pk - Primary key of item to print
// _printer - One of the following values:
//		1 - Use zXUsrPrf.prntr1; if not set: ask for it
//		2 - Use zXUsrPrf.prntr2; if not set: ask for it
//		Non blank - If exists as zXPrntr: use this but prompt user
//		Blank - Assume the document builder handler knows what to do
// _saveAs - Either number of zXDoc entry or name of file
// _addInfo1 .. 5 - Additional information
//      
//=====
function zXPrintRequest(_s, _type, _tmplt, _pk, 
			_printer, _saveAs, 
			_addInfo1, _addInfo2, _addInfo3, _addInfo4, _addInfo5)
{
	var strUrl;
	
	strUrl = "../" + SCRPT + "/zXStdPrntRqst." + SCRPT + "?-s=" + _s;
	strUrl += "&-a=asp.determineFirstAction";
	strUrl += "&-pk=" + escape(_pk);
	strUrl += "&-tpe=" + escape(_type);
	
	if (_tmplt == null)
	{
		strUrl += "&-tmplt=zXStdBO";
	}
	else
	{
		strUrl += "&-tmplt=" + escape(_tmplt);
	}

	strUrl += "&-prntr=" + escape(_printer);
	strUrl += "&-saveAs=" + escape(_saveAs);
	if (_addInfo1 != undefined) strUrl += "&-ai1=" + escape(_addInfo1);
	if (_addInfo2 != undefined) strUrl += "&-ai2=" + escape(_addInfo2);
	if (_addInfo3 != undefined) strUrl += "&-ai3=" + escape(_addInfo3);
	if (_addInfo4 != undefined) strUrl += "&-ai4=" + escape(_addInfo4);
	if (_addInfo5 != undefined) strUrl += "&-ai5=" + escape(_addInfo5);
	
	zXDialog(strUrl);
}

//----
// refreshNavigationFrame
//
// When a popup window is closed, this routine is called from the unload event
// and it will refresh the navigation frame if the opener of the popup window
// was the main window (who has the navigation frame)
//----
function refreshNavigationFrame()
{
	window.onerror = function() { return true; };

	if (top.window.opener.top.name == gSessionID)
	{
		top.window.opener.top.fraLeft.document.location.reload(true);
	}
	else
	{
		//----
		// And see if the parents parents was the main
		//----
		if (top.window.opener.opener != undefined)
		{
			if (top.window.opener.opener.top.name == gSessionID)
			{
				top.window.opener.opener.top.fraLeft.document.location.reload(true);
			}
		}
	}
}

/**
 * A generic way of getting a handle to a object
 **/
function findObj( oName, oFrame, oDoc ) {
	/** 
     * This function is slightly bigger than the DreamWeaver
     * function but is more efficient as it can also find
     * anchors, frames, variables, functions, and check through
     * any frame structure
     * 
     * If not working on a layer, document should be set to the
     * document of the working frame
     * if the working frame is not set, use the window object
     * of the current document
     *
     * WARNING: - cross frame scripting will cause errors if
     * your page is in a frameset from a different domain 
     */
    if( !oDoc ) { 
    	if( oFrame ) {
    		oDoc = oFrame.document; 
    	} else {
        	oDoc = window.document; 
       	} 
	}
	     
     

    /**
     *check for images, forms, layers
     */
    if( oDoc[oName] ) { 	
    	return oDoc[oName]; 
	}
	
	/**
     *check for pDOM layers
     */
    if( oDoc.all && oDoc.all[oName] ) { 
    	return oDoc.all[oName]; 
   	}
	
    /**
     *check for DOM layers
     */
    if( oDoc.getElementById && oDoc.getElementById(oName) ) {
        return oDoc.getElementById(oName); 
    }

    /**
     * check for form elements
     **/
    for( var x = 0; x < oDoc.forms.length; x++ ) {
        if( oDoc.forms[x][oName] ) { 
        	return oDoc.forms[x][oName]; 
        } 
	}
	
    /**
     * check for anchor elements
     *
     * NOTE: only anchor properties will be available,
     * NOT link properties!
     */
    for( var x = 0; x < oDoc.anchors.length; x++ ) {
        if( oDoc.anchors[x].name == oName ) {
            return oDoc.anchors[x]; 
		} 
	}

    /**
     * check for any of the above within a layer in layers browsers
     */
    for( var x = 0; document.layers && x < oDoc.layers.length; x++ ) {
        var theOb = findObj( oName, null, oDoc.layers[x].document );
        if( theOb ) { 
        	return theOb; 
        } 
	}
	
    /**
     * Check for frames, variables or functions
     */
    if( !oFrame && window[oName] ) { return window[oName]; }
    if( oFrame && oFrame[oName] ) { return oFrame[oName]; }

    /**
     * If checking through frames, check for any of the above within
     * each child frame
     */
    for( var x = 0; oFrame && oFrame.frames && x < oFrame.frames.length; x++ ) {
		var theOb = findObj( oName, oFrame.frames[x], oFrame.frames[x].document ); 
		if( theOb ) { 
			return theOb; 
		} 
	}
	
    return null;
}

//=============================
// getStyle
// 
// Returns a style object for the specified class name.
// 
// Reference for the style object :
// IE : (http://msdn.microsoft.com/workshop/author/dhtml/reference/objects/obj_style.asp)
// Mozilla : (http://www.mozilla.org/docs/dom/domref/dom_style_ref18.html)
//
// _name = The class name of the style element.
//============================
function getStyle(_name) {
	var colStyle;
	if (document.styleSheets != undefined) {
		for (i = 0; i < document.styleSheets.length; i++){
			if (document.styleSheets[i].cssRules) {
				colStyle = document.styleSheets[i].cssRules;
			} else {
				colStyle = document.styleSheets[i].rules;
			}
			
			for (j = 0; j < colStyle.length; j++) {
				if (colStyle[j].selectorText == _name) {
					return colStyle[j].style;
					break;
				}	
			}
		}
	}
}

//=============================
// Cross-browser XMLHttpRequest instantiation.
//=============================
if (typeof XMLHttpRequest == 'undefined') {
	XMLHttpRequest = function () {
		var msxmls = ['MSXML3', 'MSXML2', 'Microsoft']
		for (var i=0; i < msxmls.length; i++) {
			try {
				return new ActiveXObject(msxmls[i]+'.XMLHTTP')
			} catch (e) { 
			}
		}
		throw new Error("No XML component installed!")
	}
}

//=============================
// callInProgress
//
// Checks whether the current xmlhttp request is still
// in progress.
//
// xmlhttp = The xmlhttp request currently in process.
//=============================
function callInProgress(xmlhttp) {
	switch (xmlhttp.readyState) {
		case 1, 2, 3:
			return true;
			break;
		
		// Case 4 and 0
		default:
			return false;
			break;
	}
}

//=============================
// delay
//
// period - The period in milliseconds to delay for. Should be a positive value
//=============================
function delay(period){ 
	var then,now;
	then=new Date().getTime();
	now=then;
	
	while((now-then)< period) {
		now=new Date().getTime();
	}
}

//=============================
// getKeyCode - Get the keycode of an event.
//
// evt - The event trigger.
//=============================
function getKeyCode(evt) {
	var getKeyCode;
	
	if (!evt) var evt = window.event;
	
	if (evt.keyCode) {
		getKeyCode = evt.keyCode;
	} else if (evt.which) {
		getKeyCode = evt.which;
	}
	return getKeyCode;
}

//==================
// ALPHA CODE : Using XMLHttpRequest for Displaying the Message count
//==================
function zXPollMessages(_sessionid) {
	var req = new XMLHttpRequest();
	if (req) {
		//===============================
		// Browser Supports XMLHttpRequest
		//================================
		
		// Stop any current requests
		if(req && callInProgress(req)) {
	           req.abort();
	           req = new XMLHttpRequest();
	    }
	    
		var strUrl = "../" + SCRPT + '/zXMessages.' + SCRPT;
		strUrl += "?-s=" + _sessionid + "&" + new Date().getTime();
		
		req.open("GET", strUrl, true);
		
		if (window.ActiveXObject) { 
			req.send();
		} else {
			req.send(null);
		}
		
		req.onreadystatechange = function() {
			
		    if (req.readyState == 4) {
		        if (req.status == 200) {
		        	
		        	// Method 1 : Using Html
		        	// Pro : Easy to code.
		        	// Con : Not reusable.
		        	//var zXMessages = document.getElementById("zXMessages");
		        	//zXMessages.innerHTML = messages_req.responseText;
		        	
		        	// Method 2 : Using XML
		        	// Pro : Can be reusable, Clean look.
		        	// Con : Javascript is combersome :(.
		        	
		        	//=====
		        	// Step 1 : Interprit the data
		        	//=====
					var strImg;
					var strImgOver;
					var strTitle;
					
		    	    var strCount = req.responseXML.getElementsByTagName("unread").item(0).firstChild.nodeValue;
		        	if (strCount == null || strCount == "" || strCount == "0"){
						strImg = "textRead.gif";
						strImgOver = "textReadOver.gif";
						strTitle = "No unread texts";
						
					} else {
						strImg = "textUnread.gif";
						strImgOver = "textUnreadOver.gif";
						strTitle = strCount + " unread text" 
								   + (strCount== "1" ? " ":"s ") + "available";
								   
					}
					
					//=======================================
					// Step 2 : Massage the img object values
					//=======================================
		        	var zXMsgImg = document.getElementById("zXMsgImg");
					if (zXMsgImg.getAttribute("title") != strTitle) {
						zXMsgImg.setAttribute('title', strTitle, 0);
						zXMsgImg.setAttribute('src', imgDir + strImg, 0);
						if (window.ActiveXObject) {
							zXMsgImg.onmouseover =  function() { zXIconFocus(this, strImgOver); }
							zXMsgImg.onmouseout = function() { zXIconFocus(this, strImg); }
						} else {
							// Works better on Safari.
							strTmp = 'zXIconFocus(this, "' + strImgOver + '")';
							zXMsgImg.setAttribute('onMouseOver',strTmp, 0);
							strTmp = 'zXIconFocus(this, "' + strImg +'")';
							zXMsgImg.setAttribute('onMouseOut',strTmp, 0);
						}
					}
					
		        } else {
		            // alert("There was a problem retrieving the data:\n" + zXPollMessagesReq.statusText);
		        }
		    }
		    
		} // end of function onreadystatechange
		
	} else {
		//=======
		// Browser does not support XMLHttpRequest so we 
		// will fall back to the old behaviour :
		//=======
		document.location.reload();
	}
}

//------------
// isMainWindowOpen
//
// Try to see if the main window is open.
//
//------------
function isMainWindowOpen() {
	if (gSessionID != undefined) {
		if (top.fraDetailsMenu && top.fraFooter) {
			//----
			// We are in or part of the main window
			//----
			return true;
			
		} else {
			//----
			// Go through the chain of open windows to the root window.
			//----
			var win = top.window.opener;
			try {
				//----
				// Find the root window
				//----
				if (win) {
					/**
					blnWinOpener = true;
					while (blnWinOpener) {
						if (win.opener) {
							win = win.opener;
						} else {
							blnWinOpener = false;
						}
					}
					**/
					
					if (win.opener) {
						win = win.opener;
						if (win.opener) {
							win = win.opener;
							if (win.opener) {
								win = win.opener;
								if (win.opener) {
									win = win.opener;
								}
							}
						}
					}
				}
				
				if (win) {
					if (win.name = gSessionID) {
						return true;
					}
				}
			} catch (e) {
				return false;
			}
		}
	}
	
	return false;
}

//---------
// initMainWindow
// 
// Try to init the mainwindow handle.
// 
// _check = Do a safety check
//---------
function initMainWindow(_check) {
	if (!_check || isMainWindowOpen()) {
		if (zXMainWindow == undefined && !zXMainWindow && gSessionID != undefined) {
			//----
			// If the main window was closed minimize the affect of opening a new window
			//----
			var parms = "toolbar=no,location=no,left=1,top=1";
		 	parms += ",screenX=1,screenY=1";
		 	parms += ",width=100,height=100";
			parms += ",directories=no,status=no,menubar=no,resizable=no";
			
			zXMainWindow = window.open("", gSessionID, parms);
			if (zXMainWindow != undefined && zXMainWindow.location.href == "about:blank") {
				// Mainwindow has been closed
				zXMainWindow.close();
				zXMainWindow = false;
			}
			
		} else {
			//----
			// We already called initMainWindow once.
			//----
			if (zXMainWindow.closed != undefined && zXMainWindow.closed) {
				zXMainWindow = false;
			}
		}
		
	} else {
		// The main window is closed or we cannot get a handle to it safely.
		zXMainWindow = false;
	}
}

//=============================
// zXCallUrl
//
// Calls a url from the zXMainWindow, this is usually used for zXStdPopup.
//
// strUrl = The url to call.
//=============================
function zXCallUrl(strUrl) {
	var req = new XMLHttpRequest();
	if (req && false) {
		
		// Stop any current requests
		if(req && callInProgress(req)) {
			req.abort();
			req = new XMLHttpRequest();
		}
		
		req.open("GET", strUrl + "&" + new Date().getTime(), false);
		
		req.onreadystatechange = processRequestChange;
		
		if (window.ActiveXObject) { 
			req.send();
		} else {
			req.send(null);
			delay(100);
		}
		
	} else {
		// Init handle to the main window.
		initMainWindow(false);
		zXMainWindow.top.fraDetails5.document.location = strUrl;
	}
	
	function processRequestChange() {
        if(req.readyState == 4) {
	        if (req.status == 200) {
	        	if (req.responseText) {
	        		window.status = "Background Task completed";
                }
            }
        }
    }
}

//----
// New messaging system : 
// IM polling method.
//----
function zXPollIMMsg(_username, _sessionid) {
	var lngStart = new Date().getTime();
	var req = new XMLHttpRequest();
	var strUrl;
	
	if (req) {
		//===============================
		// Browser Supports XMLHttpRequest
		//================================
		
		// Stop any current requests
		if (callInProgress(req)) {
			//----
			// Cleanup
			//----
			req.abort();
			freeReqMemory(req);
			req = null;
			
	        req = new XMLHttpRequest();
		}
		
	    // TODO : Make configurable for javascript
		strUrl = "../tmp/im/" + _username + ".xml?" + lngStart;
		// We may use apache for example to server this url.
		//strUrl = "/im/" + _username + ".xml?" + lngStart;
		
		req.open("GET", strUrl, true);
		
		if (window.ActiveXObject) { 
			req.send();
		} else {
			req.send(null);
		}
		
		//============================
		// Handle the reponse from the sytem.
		//============================
		req.onreadystatechange = function () { zXPollIMOnreadyState(req, _sessionid) };
		
	} else {
		//=======
		// Browser does not support XMLHttpRequest so we 
		// will fall back to the old behaviour :
		//=======
		document.location.reload();
	}
}

var msgWindow;
function zXPollIMOnreadyState(req, _sessionid) {
	var strUrl;
	var strImg;
	var strImgOver;
	var strTitle;
	var objResponseXML;
	var objNode = null;
	var intCount = 0;
	var intUrgentCount = 0;
	
    if (req.readyState == 4) {
    	//=====
    	// Step 1 : Interprit the data
    	//=====
        if (req.status == 200) {
        	//----
        	// Parse the AJAX response
        	//----
            objResponseXML = req.responseXML;
            objNode = objResponseXML.getElementsByTagName("unread").item(0);
    	    if (objNode != null) {
    	    	intCount = objNode.firstChild.nodeValue;
    	   	}
			objNode = objResponseXML.getElementsByTagName("urgent").item(0);
			if (objNode != null) {
		    	intUrgentCount = objNode.firstChild.nodeValue;
			}
			
			//----
			// Cleanup
			//----
			freeReqMemory(req);
			req = null;
			
    	   	//======
    	   	// Show whether we have any messages.
    	   	//======
        	if (intCount == null || intCount == "" || intCount == 0){
				strImg = "textRead.gif";
				strImgOver = "textReadOver.gif";
				strTitle = "No unread texts";
				
			} else {
				strImg = "textUnread.gif";
				strImgOver = "textUnreadOver.gif";
				strTitle = intCount + " unread text" 
						   + (intCount == 1 ? " ":"s ") + "available";
			}
	   	
   			if (msgWindow == undefined || msgWindow == null 
				|| msgWindow.closed == undefined || msgWindow.closed) {
   				//======
				// Show popup for new urgent messages
				//======
				if (intUrgentCount != null && intUrgentCount != "" && intUrgentCount > 0) {
					//======
					// We have at least one urgent message
					//======
					if (intUrgentCount == 1) {
						//-----
						// Open message directly
						//-----
						strUrl = "../" + SCRPT + "/zXGPF." + SCRPT +"?-pf=nk/im/msg&-a=autoopen&-s=" + _sessionid;
					} else {
						//-----
						// List messages
						//-----
						strUrl = "../" + SCRPT + "/zXGPF." + SCRPT +"?-pf=nk/im/msg&-s=" + _sessionid;
					}
					
					strSubsession = zXSubSessionId();
					strUrl += "&-ss=" + strSubsession;
					
					//=====
					// New windows paramaters
					//===== 
					var parms = "toolbar=no,location=no,left=1,top=1";
				 	parms += ",screenX=1,screenY=1";
				 	parms += ",width=800,height=600";
					parms += ",directories=no,status=no,menubar=no,resizable=yes";
					
					msgWindow = window.open(strUrl, "messageWindow", parms);
				}
			}
					
        } else {
			//----
			// Cleanup
			//----
			freeReqMemory(req);
			req = null;
			
        	//====================
        	// Handle error response (Page may not exist)
        	//====================
			strImg = "textRead.gif";
			strImgOver = "textReadOver.gif";
			strTitle = "No unread texts";
        }
		        
		//=======================================
		// Step 2 : Massage the img object values
		//=======================================
    	var zXMsgImg = document.getElementById("zXIMMsg");
		if (zXMsgImg.getAttribute("title") != strTitle) {
			zXMsgImg.setAttribute('title', strTitle, 0);
			zXMsgImg.setAttribute('src', imgDir + strImg, 0);
			
			if (window.ActiveXObject) {
				zXMsgImg.onmouseover =  function() { zXIconFocus(this, strImgOver); }
				zXMsgImg.onmouseout = function() { zXIconFocus(this, strImg); }
			} else {
				// Works better on Safari.
				strTmp = 'zXIconFocus(this, "' + strImgOver + '")';
				zXMsgImg.setAttribute('onMouseOver',strTmp, 0);
				strTmp = 'zXIconFocus(this, "' + strImg +'")';
				zXMsgImg.setAttribute('onMouseOut',strTmp, 0);
			}
		}
    }	    
} // end of function onreadystatechange

//=============================
// zXPopupMsg
//
// Function to present text messaging options in a popup menu
//
//=============================
function zXPopupMsg(_session)
{
		var zXPopupObj;
		var strFrameRows;
		
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
		
		zXPopupObj.row("Show text messages","parent.zXPopupStep1('" + _session + "','../" + SCRPT + "/zXGPF." + SCRPT + "?-pf=nk/im/msg&-s=" + _session + "',1);","","",true);
		zXPopupObj.row("Send new text message","parent.zXPopupStep1('" + _session + "','../" + SCRPT + '/zXGPF.' + SCRPT + "?-pf=nk/im/msgsend&-s=" + _session + "',1);","","",true);
		
		zXPopupObj.show();
}

//----
// Clean memory for req object
//----
function freeReqMemory(req) {
    req = null;
    
    //----
    // IE Only garbage collection
    //----
	if (window.createPopup) {
		CollectGarbage();
	}
}