//
// What	: zXFooter.js
// Why	: Function for footer
// Who	: Bertus Dispa
// When	: 8SEP02
//
// Change	: BD27OCT03
// Why		: Added zXStsQS for standard QS handling
//
// Change	: BD15FEB04
// Why		: Added whereGroup option to zXStdQS
//
// Change	:	MB03NOV04
// Why		:	Allow for sharing of js between jsp and asp projects
//


//==============================
// zXScrollFrames
// _x - X coordinate
// _y - Y coordinate
//
// Scroll all frames up / down (implements top-of-page / bottom-of-page buttons)
// BD15OCT01 - Do not include frames with a name containing Tab in the scrolling
// DGS30SEP2003: Apparent IE6 problem - 'self' is not always the same as 'window' - use window.onerror
//==============================
function zXScrollFrames(_x, _y, _noMenu) 
{
	var iFrames = 0;
	var iFrames2 = 0;
	var iFrames3 = 0;
	
	window.onerror = function() { return true; };

	for(iFrames=0;iFrames<parent.frames.length;iFrames++)
	{
		if (parent.frames[iFrames].name != "fraFooter") 
		{
			parent.frames[iFrames].scrollTo(_x, _y);
		}

		for(iFrames2=0;iFrames2<parent.frames[iFrames].frames.length;iFrames2++)
		{
			if (parent.frames[iFrames].frames[iFrames2].name.match("Tab") == null) 
			{
				parent.frames[iFrames].frames[iFrames2].scrollTo(_x, _y);

				for(iFrames3=0;iFrames3<parent.frames[iFrames].frames[iFrames2].frames.length;iFrames3++)
				{
					if (parent.frames[iFrames].frames[iFrames2].frames[iFrames3].name.match("Tab") == null) 
					{
						parent.frames[iFrames].frames[iFrames2].frames[iFrames3].scrollTo(_x, _y);
					}
				}
			}
		}
	}
			
	window.onerror = function() { return false; };

	return false;
}

//==============================
// zXDisconnect
// _session - Session id
//
// Disconnect
//==============================

function zXDisconnect(_session) 
{	
	var url;
	
	if (confirm("Are you sure you want to logout from the system?"))
	{
		url = "../" + SCRPT + '/zXLogin.' + SCRPT + "?-a=logOff&-s=" + (_session);
	
		parent.fraDetails1.location = url;
		// parent.document.all.fraDetailsInnerSet.rows = "1%,*,1%,1%,1%,1%";  
	}
	
	return false;
}

//
// Handle QS request
//
// _session - session-id
// _type - the type of QS requested - normally expects a corresponding ASP name of ../asp/<_type>.asp
// _qs - the quick search control
//
function zXQS(_session, _type, _qs)
{
	zXPopupStep1(_session, '../' + SCRPT + '/' + _type + '.' + SCRPT + '?-a=aspQS&-s=' + _session + '&-qs=' + _qs.value);

	_qs.value = "";
}


function zXOnKeyPress(_event, _session, _type, _qs) 
{
	//----
	// Cr-Lf will force submit of form
	//----
	if (window.event && window.event.keyCode == 13)
	{
		zXQS(_session, _type, _qs);
		return true;
	}
	else
	{
		return true;
	}
}

//
// Handle QS request using standard QS handling
//
// _session - session-id
// _entity - entity
// _qs - the quick search control
// _pf - name of pageflow that implements the standard popup
// _action = name of action within pageflow that implements standard popup
// _where (optional) = optional where clause to be added to query
//
function zXStdQS(_session, _entity, _qs, _pf, _action, _whereGroup)
{
	var strUrl;
	
	strUrl = '../' + SCRPT + '/zXStdQS.' + SCRPT;
	strUrl += '?-s=' + _session;
	strUrl += '&-pf=zXStdQS';
	strUrl += '&-entity=' + _entity;
	strUrl += '&-qs=' + _qs.value;
	strUrl += '&-pageflow=' + _pf;
	strUrl += '&-action=' + _action;
	
	if (_whereGroup != undefined)
	{
		strUrl += '&-whereGroup=' + escape(_whereGroup);
	}
	
	zXPopupStep1(_session, strUrl);

	_qs.value = "";
}

//
// Handle typing into QS fields. If return is pressed, automatically calls 
// QS request using standard QS handling
//
// _session - session-id
// _entity - entity
// _qs - the quick search control
// _pf - name of pageflow that implements the standard popup
// _action = name of action within pageflow that implements standard popup
// _where (optional) = optional where clause to be added to query
//
function zXStdOnKeyPress(evt, _session, _entity, _qs, _pf, _action, _whereGroup) 
{
	if (!evt) var evt = window.event;
	
	//----
	// Cr-Lf will force submit of form
	//----
	if (evt && getKeyCode(evt) == 13)
	{
		zXStdQS(_session, _entity, _qs, _pf, _action, _whereGroup);
		return true;
	}
	else
	{
		return true;
	}
}

function getQSTypeValue() {
	var qsTypeValue = findObj("qsType", this.window, this.document).value;
	return qsTypeValue;
}

function getQSControl() {
	return findObj('qs', this.window, this.document);
}
