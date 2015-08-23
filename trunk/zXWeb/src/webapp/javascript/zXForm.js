//----
// What		:	zXForm.js
// Who		:	Bertus Dispa
// When		:	5SEP02
// Why		:	zX form functions
//
// Change	:	BD6FEB02
// Why		:	Added support for more intuitive select / option lists
//
// Change	:	BD6MAR03
// Why		:	Fixed a problem with the more intuitive select / option list
//				when the user 'opens' the select list, IE would no longer allow
//				programatic control and the standard behaviour and the new
//				would conflict and result in funny results
//
// Change	:	BD14MAY03
// Why		:	Added zXSubmitThisUrl to cater for multiple submit buttons
//
// Change	:	BD29MAY03
// Why		:	Added stub for zXUpdateDivForLockedField
//
// Change	:	BD7JUN03
// Why		:	Added zXOnChangeSearchOps
//
// Change	:	DGS20AUG2003
// Why		:	Added zXAddShow and zXAddReturn, for adding FK rows on the fly.
//
// Change	:	BD31OCT03
// Why		:	Added parameter to zXSubmitThisUrl to tell whether it was called
//				from submit button or not (as the first does an automatic submit of form)
//
// Change	:	BD21JAN03
// Why		:	Added support for popup expression editor
//
// Change	:	DGS28JAN2004
// Why		:	In zXOnChangeSearchOps now also disable lower limit control if null operator
//
// Change	:	DGS14APR2004
// Why		:	Heavily revised onChangeDate, for better validation.
//				Added new function zXCatchBackSpace and made it called on key down.
//				Amended zXSetCursorToFirstField so that fields are assessed forwards, not in reverse.
//
// Change	:	DGS30APR2004
// Why		:	New functions zXListSelectDisable and zXListSelected. Used to control the select
//				image on listforms when certain pageflow tags are set.
//
// Change	:	BD6MAY04
// Why		:	Added zXDocAttr* routines to deal with attributes on an edit form that
//				are a FK to zXDoc.
//				See zXDoc.asp for comments 
//
// Change	:	DGS08MAY2004
// Why		:	In zXReplaceText, the function is onchange, not onChange.
//
// Change	:	BD19MAY04
// Why		:	Better handling of checkboxes in enhancers
//
// Change	:	DGS14JUL2004
// Why		:	In zXFKPopup, must now pass the -pf name
//
// Change	:	BD14JUL2004
// Why		:	In set cursor to first field; do NOT give focus to select-one input control
//				when not named explicitly; caused too much grieve for user who accidentally
//				changed data when wanting to scroll
//
// Change	:	DGS21JUL2004
// Why		:	In onChangeDate, truncate year if longer than 4 digits
//
// Change	:	DGS29JUL2004
// Why		:	Moved zXCatchBackSpace from here to zX.js, so that it is always available
//
// Change	:	BD3AUG04
// Why		:	Changed FK popup; now no longer in frameset (ie no footer, no navigation)
//					to make it load faster; changed FKPopup to show scrollbars
//
// Change	:	03AUG2004 Domarque Workshop No. 44
// Why		:	New functions listSelect and listSelectStep2 for selecting items e.g. email addresses
//
// Change	:	BD11AUG04
// Why		:	Added support for confirmation with submit ref-buttons
//
// Change	:	DGS01SEP2004
// Why		:	New function setAllRadios sets all radio buttons of a particular value to checked
//
// Change	:	MB01NOV04
// Why		:	Added in browser independence code.
//
// Change	:	MB01NOV04
// Why		:	Added new calendar control.
//
// Change	:	MB03NOV04
// Why		:	Allow for sharing of js between jsp and asp projects
//
// Change	:	BD17DEC04 - V1.4:10
// Why		:	Fixed problem in submitThisUrl
//
// Change	:	V1.4:24 - BD20JAN05
// Why		:	Mark changes in multi-line fields as dirty as well
//
// Change	:	V1.4:25 - DGS21JAN2005
// Why		:	New function zXSubmitThisUrlCheck - like zXSubmitThisUrlConfirm but only
//				prompts with confirm message if a special variable has not been changed.
//				Useful to detect if a field has been changed or a button clicked etc., and
//				is used by spell checking to signify button has been clicked
//
// Change	:	DGS18FEB2005
// Why		:	Changes to zXOption.prototype.row - only consider a row to be a duplicate
//				if its FK attr is also identical (affects bound attrs only)   
//
// Change	:	MB10MAR2005
// Why		:	Fixed bug introduced in BD19MAY04 causing zXEnhCompare to fail.
//
// Change	:	V1.4:61 - MB24MAR2005
// Why		:	Fixed bugs in the new date control. 
//				1) Sometimes incorrect dates where being generated.
//				2) Date control overlapping over data. We can now reposition the date control.
//				3) Hide the date control when the user presses escape or clicks on the main document.
//
// Change	:	V1.4:84 - MB25MAY2005
// Why		:	Updated zXSelectTab to support vertical tabs as well as horizontal. 
//
// Change	:	V1.5:20 - BD1JUL05 
// Why		:	Added support for enhanced FK Labels 
//
// Change	:	V1.4:95 - DGS22JUL2005
// Why		:	Changes to zXOnKeyPress - was not recognizing the CR correctly
//
// Change	:	V1.5:40 - MB09AUG2005
// Why		:	Add support for new fklookup control
//
// Change	:	V1.5:41 - MB09AUG2005
// Why		:	The date control can now optionally append to the linked form input control.
//
//----

//----
// Function	:	zXOnKeyPress
//				onKeypress function handler
// In		:	event, datatype (as enum)
// Out		:	Allow pressed key yes / no
//
//----
function zXOnKeyPress(_event, _dataType) 
{
	var keyValue;
	var keyCode;
	
	if (_event.which) {
		keyCode = _event.which;
	} else {
		keyCode = _event.keyCode;
	}
	
	keyValue = String.fromCharCode(keyCode);
	
	//----
	// Cr-Lf will force submit of form
	//----
	if (keyCode == 13) {
		// TODO : Comment out?
		// document.forms(0).submit(); 
		
		return true;
	}
	
	if (keyCode == 46 || keyCode == 8 || keyCode == 37 || keyCode == 39 || keyCode == 9) return true;
	
	var pattern;
	switch(_dataType)  
	{
		case 0: // Auto
		case 2: // Long	
		    pattern = /[0-9-]/;
			break; 
		case 3: // Double
		    pattern = /[0-9-\.]/;
			break; 
		case 1: // String
			zXDirty = 1;
		    return true;
			break; 
		case 4: // Date 
		case 6: // Time
		case 7: // Timestamp
			// t / + / - are allowed as they translate into
			keyValue = keyValue.toUpperCase();
		    pattern = /[0-9\/t+-a-z]/i;
			break; 
	}
	
	if (pattern.test(keyValue))
	{
		zXDirty = 1;
		return true;
	}
	else
	{
		return false;
	}
}


//----
// Function	:	zXOnChange
//				onChange function handler
// In		:	value, datatype (as enum)
// Out		:	optionally alter the value as it has been changed to
//----
function zXOnChange(_value, _dataType) 
{
	// Mark form as dirty
	zXDirty = 1;
	
	switch(_dataType)  
	{
		case 0: // Auto
		case 2: // Long	
		case 3: // Double
		case 1: // String
		case 6: // Time
			return _value;
			break;
			
		case 7: // Timestamp
		case 4: // Date 
		    return onChangeDate(_value);
			break; 
			
//		case 7: // Timestamp - (has a different syntax)
//			return _value;
//			break;
	}
}

//----
// Function	:	onChangeDate
//				onChange function handler for date values
// In		:	value
// Out		:	optionally alter the value as it has been changed to
//----
function onChangeDate(_value) 
{
	var strReturn = "";
	
	//---
	// If field is empty, do not do anything
	//---
	if (_value == "") {
		return "";
	}
	
 	var dtmDate = new Date();
	
	//----
	// Do the simple date parsing
	//----
	
	//----
	// If only a number has been entered, assume the days of current month / year
	//----
	if (!isNaN(_value) && _value <= 31 && _value >= 1) {
		//Ensure that there is no + or - in front.
		var LeftDigit = _value.substr(0,1);
		if (LeftDigit == "+") {
			strReturn = "";
		} else {
			// If days < todays day, assume next month
			if (dtmDate.getDate() > _value) {
				dtmDate.setMonth(dtmDate.getMonth() + 1);
			}
			
			dtmDate.setDate(_value);
			
			strReturn = printDate(dtmDate);
			// strReturn = CheckDays(_value, strReturn);
			return strReturn;
		}
	}
	
	//----
	// Feature: t / T is shortcut for today 
	//----
	if (_value == "t" || _value == "T") {
		dtmDate = new Date();
		
		strReturn = printDate(dtmDate);
		return strReturn;
	}
	
	//----
	// Feature: + may be used to add number of days to today
	// eg : t+100
	//----
	if (_value.indexOf("+") != -1) {
		var arrDate = _value.split("+"); 
		if (arrDate.length == 2 
			&& (arrDate[0] == "t" || arrDate[0] == "T") 
			&& (!isNaN(arrDate[1]))) {
			dtmDate = new Date();
			
			dtmDate.setDate(dtmDate.getDate() + parseInt(arrDate[1]));
			
			strReturn = printDate(dtmDate);
			return strReturn;
		}
	}
	
	//----
	// Feature: - may be used to subtract number of days from today
	// eg : t-100
	//----
	if (_value.indexOf("-") != -1)  {
		var arrDate = _value.split("-"); 
		if (arrDate.length == 2 
			&& (arrDate[0] == "t" || arrDate[0] == "T") 
			&& (!isNaN(arrDate[1]))) {
			dtmDate = new Date();
			
			dtmDate.setDate(dtmDate.getDate() - parseInt(arrDate[1]));
			
			strReturn = printDate(dtmDate);
			return strReturn;
		}
	}
	
	//Remove any spaces or slashes.
	_value = _value.replace(/\s|\//g, "");
	
	//----
	// Check if in numeric format, and if so convert month to alphabetic
	//----
	var pattern = /^[0-9]{3,8}/i;
	if (pattern.test(_value)) {
		// For convenience: prefix 0 if only single digit day is being used
		if ( _value.length == 3 || _value.length == 5 || _value.length == 7 ) {
			_value = "0" + _value;
		}
		
		_value = _value.substr(0,2) + monthNumberToName(_value.substr(2,2)) + _value.substr(4);
	}
	
	//----
	// Check if only days and months are given (alphabetic month)
	//----
	pattern = /^[0-9]{1,2}[a-zA-Z]{3}/i;
	if (pattern.test(_value)) {
		// For convenience: prefix 0 if only single digit day is being used
		if (isNaN(_value.substr(0,2))) {
			_value = "0" + _value;
		}
		var Days = _value.substr(0,2);
		var Year;
				
		//Previously we set day and month into dtmDate at this point, but the problem was that until the
		//year is established, it will reject a leap year date. Therefore don't set dtmDate until we know the year.
		if (_value.length == 5) {
			//----
			//Year is not included
			// If month < todays month: assume next year
			//----
			var today = new Date();
			
			//Don't want to set dtmDate yet but have to here to compare against today. However,
			//we don't put the resultant year into dtmDate but keep it in a variable for use later.
			dtmDate.setMonth(monthNameToNumber( _value.substr(2, 3)) -1);
			dtmDate.setDate(Days);
			
			if (dtmDate < today) {
				Year = dtmDate.getFullYear() + 1;
			} else {
				Year = dtmDate.getFullYear();
			}
			
		} else {
			//----
			//Year is included
			//----
			Year = _value.substr(5);
			if (Year.length < 4) {
				//Support for spanning the century.
				if (Year < 70 || Year > 99) {
					Year = 2000 + (Year * 1);
				} else {
					Year = 1900 + (Year * 1);
				}
				
			} else {
				//----
				// If year is longer than 4 digits, truncate at 4
				//----
				if (Year.length > 4) {
					Year = Year.substr(0,4);
				}
			}
		}
		
		//----
		// Make sure that the year is a number
		//----
		if (isNaN(Year)) {
			return "";
		}
		
		// Now set dtmDate using the year, month and day we have established. Doing it at this later
		// stage means it can handle leap year dates properly.
		dtmDate = new Date(Year, monthNameToNumber(_value.substr(2, 3)) -1, Days);
		
		strReturn = printDate(dtmDate);
		// strReturn = CheckDays(Days, strReturn);
	}
	
	return strReturn;
}

function printDate(dtmDate) {
	var strReturn = "";
	var strDay = "";
	
	if (dtmDate == undefined) {
		return strReturn;
	}
	
	strDay = dtmDate.getDate();
	if (dtmDate.getDate() < 10) {
		strDay = "0" + dtmDate.getDate();
	}
	
	strReturn = strDay + monthNumberToName(dtmDate.getMonth() + 1) + dtmDate.getFullYear();
	
	return strReturn;
}

function CheckDays(lngDays, strDate)
{
	var dtmDate = new Date(strDate);
	
	if (lngDays == dtmDate.getDate())
	{
		return strDate;
	}
	else
	{
		return "";
	}
}

function monthNumberToName(_monthNumber)
{
	var strReturn;
	
	// Multiply by 1 to take care of string input
  	switch(_monthNumber * 1) 
  	{
  		case 1:
  			strReturn = "Jan";
  			break;
  		case 2:
  			strReturn =  "Feb";
  			break;
  		case 3:
  			strReturn =  "Mar";
  			break;
  		case 4:
  			strReturn =  "Apr";
  			break;
  		case 5:
  			strReturn =  "May";
  			break;
  		case 6:
  			strReturn =  "Jun";
  			break;
  		case 7:
  			strReturn =  "Jul";
  			break;
  		case 8:
  			strReturn =  "Aug";
  			break;
  		case 9:
  			strReturn =  "Sep";
  			break;
  		case 10:
  			strReturn =  "Oct";
  			break;
  		case 11:
  			strReturn =  "Nov";
  			break;
  		case 12:
  			strReturn =  "Dec";
  			break;
  		default:
  			strReturn = "";
  			break;
	}

	return strReturn;
}


function monthNameToNumber(_monthName)
{
	var strCompare = new String(_monthName);
	var strReturn;
	
  	switch(strCompare.toUpperCase()) 
  	{
  		case "JAN":
  			strReturn = 1;
  			break;
  		case "FEB":
  			strReturn = 2;
  			break;
  		case "MAR":
  			strReturn = 3;
  			break;
  		case "APR":
  			strReturn = 4;
  			break;
  		case "MAY":
  			strReturn = 5;
  			break;
  		case "JUN":
  			strReturn = 6;
  			break;
  		case "JUL":
  			strReturn = 7;
  			break;
  		case "AUG":
  			strReturn = 8;
  			break;
  		case "SEP":
  			strReturn = 9;
  			break;
  		case "OCT":
  			strReturn = 10;
  			break;
  		case "NOV":
  			strReturn = 11;
  			break;
  		case "DEC":
  			strReturn = 12;
  			break;
		default:
			strReturn = 1;
			break;
	}
	
	return strReturn;
}


function zXPopupCalendar(_ctr) 
{
  	var params = "'toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no,";
       	params += "resizeable=yes, copyhistory=no, width=300, height=300, screenX=150, screenY=150'";
  
  	if (typeof(zXCalendar) != "undefined") 
	{
     		if (!zXCalendar.closed) 
		{
	    		zXCalendar.close();
	 	}
  	}

 	zXCalendar = window.parent.open("../" + SCRPT + "/zXCalendar." + SCRPT + "?-da=" + _ctr.name + "&-cdv=" + _ctr.value, "Calendar", params);
	zXCalendar.opener = self;
}


//=============================
// zXSetCursorToFirstField
//
// Set cursor (focus) to first editable field on form
// _ctrName - optional name of control
//=============================
function zXSetCursorToFirstField(_ctrName)
{
	self.onerror = function() { return false; };

	for(var f = 0; f < document.forms.length; ++f)
	{
		//----
		// Do it upside down as this will ensure that the last enabled field will get focus
		// No, do it right way round (Domarque issue 282BB), as that stops it scrolling down for the
		// last field (sometimes it doesn't scroll back up for the first one and looks odd). Now
		// break after focussing on one. Beware if first field is not focussable - might not work
		// properly then.
		// BD10JUL04 - Never set focus on option list (select-one) if we are looking for the
		// first control on the screen (i.e. _ctrName is not given) as this is very dangerous
		// from an input opint of view as the user can easily select an option when he / she
		// means to scroll using the mouse scroll button
		//---
		for(var e = 0; e <= document.forms[f].elements.length - 1; ++e)
		{
			if ( (document.forms[f].elements[e].type == "text" || 
					document.forms[f].elements[e].type == "textarea" ||
					document.forms[f].elements[e].type == "select-one" || 
					document.forms[f].elements[e].type == "password") &&			  
					(document.forms[f].elements[e].disabled == false) )
			{
				//----
				// Next we have to check whether the element is visible, this is
				// required because multi-tab pages will have a number of fields
				// that are not visible as they are on one of the non-active tabs;
				//----
				if (isVisible(document.forms[f].elements[e]))
				{
					if (_ctrName == undefined && document.forms[f].elements[e].type != "select-one")
					{
						document.forms[f].elements[e].focus();
						return;
					}
					else
					{
						if (document.forms[f].elements[e].name == _ctrName)
						{
							document.forms[f].elements[e].focus();
							return true;
						}
					}
				}
			}
		}
	}

	// Reset error handler
	self.onerror = function() { return false; };
}



//----
// isVisible
//		Check whether an element is visible, i.e. its style.display and non of its
//		parents style.display is set to none
//
// Returns true or false
// 
// _obj - Handle to element that we want to check the visibility of
//
//----
function isVisible(_obj)
{
	var objElement;
	
	objElement = _obj;

	while (objElement != null)
	{
		if (objElement.style.display == "none")
		{
			return false;
		}
		
		objElement = objElement.parentElement;
	}

	return true;
}

//=============================
// zXSetWaitCursor
//
// Set the wait cursor for the whole body and the control
// that has triggered the event. The latter is needed because
// IE does not seem to alter the cursor of the clicked button
// when you change the cursor of the body....
//=============================
function zXSetWaitCursor(_ctrl)
{
	document.body.style.cursor="wait";
	_ctrl.style.cursor="wait";

	return false;
}

//======================
// zXFKPopup
// _esize		- entity size (s/m/l)
// _session		- session id
// _type		- 1 = search, 0 = show
// _ctr			- control for primary key
// _e			- entity (ie the from bit, e.g. client)
// _attr		- The original FK attribute
// _fke			- foreign entity - the other entity, e.g. order
// _fka			- foreign entity attribute name 
// _fkta		- foreign entity to attribute name (used on return)
// _fkval		- foreign key value (to restrict the list)
//
// Handle popup window for FK relations
// DGS07APR2003: Heavily revised to reflect the new way of handling medium and large entities.
// BD1JUL505: Added -e and -attr

function zXFKPopup( _esize,
					_session, 
					_type, 
					_ctr, 
					_e,
					_attr,
					_fke,
					_fka,
					_fkta,
					_fkval)
{
 	var strURL;				// Querystring

	// Build URL
	strURL = "../" + SCRPT + "/zXFK." + SCRPT + "?-pf=zXFK&";
	strURL += "-s=" + _session; 				// Session
	strURL += "&-fke=" + _fke;					// Foreign entity
	strURL += "&-fka=" + _fka;					// Foreign entity attr
	strURL += "&-fkta=" + _fkta;				// Foreign entity attr to
	strURL += "&-esize=" + _esize;				// 
	strURL += "&-fkval=" + _fkval;				// 
	strURL += "&-ctr=" + _ctr;					// Name of PK control atribute
	strURL += "&-e=" + _e;					    // Original entity
	strURL += "&-attr=" + _attr;			    // FK attribute
	strURL += "&-ss=" + zXSubSessionId();		// Subsession identifier

  	switch(_type) {
  	case 0:
  		// Search
		FKPopup(strURL + "&-a=framesetSearchForm");
    	break;

 	 default:
 		// Show
 		if (_ctr.value != "") 
 		{
     		FKPopup(strURL + "&-a=framesetShowForm");
     	}
     	break;
 	 }
 	 
}

//
// elementByName
// Search an element on a window by name and return handle to
// control
//
function elementByName(_window, _elmnt)
{
	if (isNS6) 
	{
		// Nescape and IE Compatible or call findObj
		//return (_window.document.getElementById(_elmnt));
		return findObj(_elmnt, _window);
	} 
	else 
	{
		for(var e = 0; e < _window.document.all.length; ++e)
		{
			if(_window.document.all(e).name == _elmnt)
			{
				return (_window.document.all(e));
			}
		}
	}
}

//=============================
// FKPopup
// _url - Load URL in popup window
//=============================
function FKPopup(_url) 
{
	var intHeight;
	var intWidth;
  	var dtmTimeStamp = new Date();

	intHeight=550;
	intWidth=700;

 	var parms = "toolbar=no,location=no,left=0,top=0";
 		parms += ",screenX=50,screenY=50";
 		parms += ",height=" + intHeight;
 		parms += ",width=" + intWidth;
		parms += ",directories=no,status=no,menubar=no,scrollbars=yes,copyhistory=no,resizable=no";

  	win = parent.window.open(_url, "Win" + dtmTimeStamp.valueOf(), parms);

	win.opener = self;
}  

//
// Unlink optional FK attributes from their foreign keys
// _ctrPK - Control that contains PK
// _ctrFKLabel - Control that contains FK label
//
function zXFKUnlink(_ctrPK, _ctrFKLabel) 
{
	_ctrPK.value = "";
	_ctrFKLabel.innerHTML = "";
}

function zXTitle(_title)
{
	top.document.title = _title;
}

//
// zXFKCodeCheck
//
// Used for medium sized FK BO
// The user has entered a QS entry and has left the QS field
//
// _pk - Handle to PK (hidden) field
// _qs - Handle to QS field
// _label - Handle to label span
// _key - Key to check against 
// _pkValue - Actual PK associated with entry
// _labelValue - Value of label to set when ok
//
function zXFKCodeCheck(_pk, _label, _qs, _key, _pkValue, _labelValue)
{
	var strTmp;

	if ( _qs.value == "" )
	{
		// This entry is important as it handles the
		// recursive call caused by setting the
		// QS in the else-id bit
		return 1;
	}
	else if (_key.toLowerCase().match(_qs.value.toLowerCase())) 
	{
		// Set label
		strTmp = _label.id + ".innerHTML = '(" + _labelValue + ")';";	
		eval(strTmp);

		// Set PK
		_pk.value = _pkValue;
		
		// Delete QS
		_qs.value = "";

		return 1;
	}
	
	return 0;
}

//----
// zXSelect*
//	Support for select / option lists
//
// The idea is to allow the user to start typing the entry that they want to select
// and replace the standard HTML behaviour (by only looking at the first character of the
// label) to search for an entry that matches the string that has been typed so far
//
// - When a select box gets focus, the string that has been types so far is reset
// - When the user presses ESC while in a select box, the string typed so far is cleared
//			and the first option is selected
// - When the mouse goes down, it means that the user 'opens' the select option list, this
//			has a funny effect as we loose control over the select object as long as the list with
//			options is 'open' but the events are still fired; that is why we maintain the flag
//			blnSelectOpenStatus and basically not try to be clever when the select option list
//			is open
//----

var strSelectValueSoFar = '';
var blnSelectValueForcedChange = false;
var blnSelectOpenStatus = false;

function zXSelectOnKeyPress(_event, _ctr)
{
	var chrKeyValue;
	var i;

	if (blnSelectOpenStatus) 
	{
		return true;
	}
	
	chrKeyValue = String.fromCharCode(_event.keyCode);

	if (_event.keyCode != 27)
	{
		strSelectValueSoFar += chrKeyValue;
	}
	else
	{
		strSelectValueSoFar = '';
		return true;
	}

	for(i = 0; i < _ctr.options.length; ++i)
	{
		if ( _ctr.options[i].value.toLowerCase().substr(0, strSelectValueSoFar.length ) == strSelectValueSoFar.toLowerCase() ||
				_ctr.options[i].text.toLowerCase().substr(0, strSelectValueSoFar.length ) == strSelectValueSoFar.toLowerCase() )
		{
			_ctr.selectedIndex = i;	
			_event.cancelBubble = true;
			blnSelectValueForcedChange = true;
			return false;
		}
	}
}

function zXSelectOnChange(_event, _ctr)
{
	var i;

	if (blnSelectOpenStatus) 
	{
		return true;
	}

	if ( strSelectValueSoFar.length == 0 )
	{
		return true;
	}

	if (blnSelectValueForcedChange == true)
	{
		blnSelectValueForcedChange = false;

		for(i = 0; i < _ctr.options.length; ++i)
		{
			if ( _ctr.options[i].value.toLowerCase().substr(0, strSelectValueSoFar.length ) == strSelectValueSoFar.toLowerCase() ||
				_ctr.options[i].text.toLowerCase().substr(0, strSelectValueSoFar.length ) == strSelectValueSoFar.toLowerCase() )
			{
				_ctr.selectedIndex = i;	
				_event.cancelBubble = true;
				return false;
			}
		}
	}
	else
	{
		strSelectValueSoFar = '';
	}
}

function zXSelectOnFocus(_event, _ctr)
{
	strSelectValueSoFar = '';
}

function zXSelectOnMouseDown(_event, _ctr)
{
	blnSelectOpenStatus = ! blnSelectOpenStatus;
}

//----
// zXSelectTab
//		Select a tab when it has been clicked. Makes the related div visible.
//
// _tab - Number of tab to select
// _tabName - Optional, the name of the tab group. This will all for multiple groups of tabs on one page. Default is ""
// _orientation - Optional, the orientation of the tabs the default is "" which will be horizontal.
//----
function zXSelectTab(_tab, _tabname, _orientation)
{
	var i;
	var col;
	var pg;

	var tabLen;
	
	if (_orientation == undefined) 
	{
		_orientation = "";
	}
	
	// The tab name is made up of zXPage + pageflow action name + tabnumber.
	if (_tabname == undefined) 
	{
		_tabname = "zXPage";
	} 
	else 
	{
		_tabname = "zXPage" + _tabname;
	}
	tabLen = _tabname.length;
	
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
			if (col[i].id.substr(0,tabLen) == _tabname) 
			{
				pg = col[i].id.substr(tabLen, (col[i].id.length - tabLen));
				if (pg == _tab) 
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
				else 
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
			}
		}
	}

	if (isIE4) 
	{
		col = document.all.tags("TD");
	} 
	else 
	{
		col = document.getElementsByTagName("TD");
	}
	
	if (col != null) 
	{
	    for (i = 0; i < col.length; i++)  
	    {
			if (col[i].id.substr(0,tabLen) == _tabname) 
			{
				pg = col[i].id.substr(tabLen, (col[i].id.length - tabLen));
				if (pg == _tab) 
				{
					col[i].className="zxTabActive" + _orientation;
				} 
				else 
				{
					col[i].className="zxTabInactive" + _orientation;
				}
			}
		}
	}

	return true;
}

//----
// zXSpellCheck
//		Spell check the contents of a control. Uses ASP that invokes MS-Word to spell check.
//
// _ctr  - Handle to control whose contents are to be spell checked
// _lang - The user's current language id e.g. EN
//----
function zXSpellCheck(_ctr,_lang)
{
	var strURL;
	var intHeight;
	var intWidth;
  	var dtmTimeStamp = new Date();

	strURL = "../" + SCRPT + "/zXSpellCheck." + SCRPT + "?-a=frameset&-c='" + _ctr.name + "'&-lang=" + _lang;

	intHeight=550;
	intWidth=700;

 	var parms = "toolbar=no,location=no,left=0,top=0";
 		parms += ",screenX=50,screenY=50";
 		parms += ",height=" + intHeight;
 		parms += ",width=" + intWidth;
		parms += ",directories=no,status=yes,menubar=no,scrollbars=no,copyhistory=no,resizable=no";

  	win = parent.window.open(strURL, "Win" + dtmTimeStamp.valueOf(), parms);

	win.clipboardData.setData("text",_ctr.value);
	
	win.opener = self;

}

//----
// zXReplaceText
//		Replace the text in a control.
//
// _ctrName - Name of the control
// _newText - Replacing text
//----
function zXReplaceText(_ctrName,_newText)
{
	var ctr;

	// ctr= elementByName(top.window.opener,_ctrName);
	ctr=  findObj(_ctrName, top.window.opener);
	
	if (ctr != null)
	{
		ctr.value = _newText;
		//----
		// DGS08MAY2004: function is onchange, not onChange.
		//----
		if (ctr.type != "textarea" && ctr.type != "hidden" ) {
			if (isIE5 || isIE4) {
				ctr.onchange();
			} else {
				// Need to do the NS+ version of this.
				ctr.onchange();
			}
		}
	}
}

//----
// zXReplaceString
//		Replace occurrence of a string in a control.
//
// _ctrName - Name of the control
// _oldString - original string
// _newString - Replacing string
// _attr - attribute e.g. g for global replace
// DGS05NOV2003: wrap the old text with \b to ensure only replaces bounded words. Otherwise
// you get parts of words replaced.
//----
function zXReplaceString(_ctr,_oldText,_newText,_attr)
{
	var txt;
	var regExpObj = new RegExp("\\b"+_oldText+"\\b",_attr);

	if (_ctr != null)
	{
		txt = _ctr.value;
		
		txt = txt.replace(regExpObj,_newText);
		
		_ctr.value = txt;
	}
	
}

//----
// zXCombo*
//	Support for combo boxes
//
// The idea is to mimic a VB combo box (i.e. the user can enter whatever he / she wants
// but can also select from a list of options. At run time, The zXComboinput matrix will
// be populated with the available values from the combo list
//
// - When a select box gets focus, ant variables will be reset
// - When the user does down / up in the combobox, the appropriate entry in the value matrix
//			will be selected
//----

var zXComboLine = 0;
var zXComboinput = new Array();

function zXComboOnFocus(_ctr)
{
	zXComboLine = 0;
}

function zXComboOnKeyDown(evt, _ctr)
{
	evt = (evt) ? evt : event;
	var keyCode = getKeyCode(evt);
	
	switch (keyCode)
	{
		case 38:
			//----
			// Arrow up
			//---- 
			zXComboLine--;
			break;
			
		case 40:
			//----
			// Arrow down
			//---- 
			zXComboLine++;
			break;
			
		default:
			return true;
	}
	
	zXComboSet(_ctr);
}

function zXComboSet(_ctr)
{
	if (zXComboLine < 0)
	{
		zXComboLine = zXComboinput[_ctr.name].length - 1;
	}
	else if (zXComboLine >= zXComboinput[_ctr.name].length )
	{
		zXComboLine = 0;;
	}
	
	_ctr.value = zXComboinput[_ctr.name][zXComboLine];
}

function zXComboDown(_ctr)
{
	zXComboLine--;
	zXComboSet(_ctr);
}

function zXComboUp(_ctr)
{
	zXComboLine++;
	zXComboSet(_ctr);
}

//----
// zXEnhDisable
//		Perform the on change disable other attr activity 
//		"if A = C then D"
//		where A is _ctr.value, = is _op, C is _val, D is disable or disable and blank
//
function zXEnhDisable(_ctr, _op, _val, _ctrrel, _blank) {
	//-----
	// The visual element you want to disable might be a new fk ajax control.
	//-----
	if (findObj("_id_" + _ctrrel.name, window, document) != undefined) {
		_ctrrel = findObj("_id_" + _ctrrel.name, window, document);
	}
	if (findObj("_id_" + _ctr.name, window, document) != undefined) {
		_ctr = findObj("_id_" + _ctr.name, window, document);
	}
	
	if (zXEnhCompare(_ctr,_op,_val) == true) {
		_ctrrel.disabled = true;
 		if (_blank == true) {
 			_ctrrel.value = "";
 		}
 		
	} else {
	 	_ctrrel.disabled = false;
	}
 
}

//----
// zXEnhSet
//		Perform the on change set other attr activity 
//		"if A = C then D = E"
//		where A = _ctr.value, = is _op, C = _val, D is _ctrrel, E = _ctrrel.value
//
function zXEnhSet(_ctr, _op, _val, _ctrrel, _newrelval)
{
	//-----
	// The visual element you want to disable might be a new fk ajax control.
	//-----
	if (findObj("_id_" + _ctr.name, window, document) != undefined) {
		_ctr = findObj("_id_" + _ctr.name, window, document);
	}
	
	if (zXEnhCompare(_ctr,_op,_val) == true)
	{
		_ctrrel.value = _newrelval;
	}
 
}

//----
// zXEnhCompare
//		
//
function zXEnhCompare(_ctr, _op, _val2)
{
	var blnRet = false;
	var val1;
	
	if (_ctr.type == "checkbox")
	{
		val1 = _ctr.checked;
	}
	else
	{
		val1 = _ctr.value;
	}
	
 	if (_op == "EQ" || _op == "GE" || _op == "LE" || _op == "IsNull")  
 	{
 		if (val1 == _val2)
 		{
 			blnRet = true;
 		}
	}
 	if (_op == "GT" || _op == "GE")  
 	{
 		if (val1 > _val2)
 		{
 			blnRet = true;
 		}
	}
 	if (_op == "LT" || _op == "LE")  
 	{
 		if (val1 < _val2)
 		{
 			blnRet = true;
 		}
	}
 	if (_op == "NE" || _op == "IsNotNull")  
 	{
 		if (val1 != _val2)
 		{
 			blnRet = true;
 		}
	}
 
	return blnRet; 
}



//----
// zXOption
//		Dynamic option lists, constructor class
//
// BD1JUL05 - V1.5:20 - Added -e and -attr
function zXOption(_name, _opt, _bnd, _ctr, _e, _attr, _session)
{
	this.arrRows = new Array();

 	this.name = _name;
 	this.opt = _opt;
 	this.bnd = _bnd;
 	this.ctr = _ctr;
 	this.session = _session;
 	this.boname = _e;
 	this.attr = _attr;
 	this.esize = "s";	//default to small entity size
 	this.sel = 0;
}

//----
// zXOption
//		Dynamic option lists, stores fk object name if this list is fk (simple options lists of 
//      values don't call this)
//
// BD1JUL05 - V1.5:20 - Added -boname and -attr
zXOption.prototype.fkbo = function(_fkboname, _fkfromattr, _fktoattr, _esize, _boname, _attr)
{
 	this.fkboname = _fkboname;
 	this.fkfromattr = _fkfromattr;
	//----
 	// Sometimes 'toattr' is not the PK of this entity, but if it isn't given, assume it is.
	//----
	if (_fktoattr == "")
	{
	 	this.fktoattr = _fkfromattr;
	}
	else
	{
	 	this.fktoattr = _fktoattr;
	}
 	this.esize = _esize;
 	this.boname = _boname;
 	this.attr = _attr;
}

//----
// zXOption.prototype.row
//		add a row for this object (class method)
// DGS27JUL2004: Only add the row if it does not already exist (same value).
// This allows us to use the .copyrows function and follow it by a .row call
// without adding duplicates (useful in medium/large grids/matrices). It is
// also better to avoid duplicates anyway.
//
zXOption.prototype.row = function(_fkfrom,_fkto,_val,_txt,_sel,_tpe)
{
	var intRow = this.arrRows.length;
	var blnHave = false;
	
	for (var j = 0; j < intRow && blnHave == false; j++)
	{
		if (this.arrRows[j].fkfrom == _fkfrom && this.arrRows[j].val == _val)
		{
			blnHave = true;
			intRow = j;
		}
	}

	if (blnHave == false)
	{
		this.arrRows[intRow] = new Object();

		this.arrRows[intRow].fkfrom = _fkfrom;
		this.arrRows[intRow].fkto = _fkto;
		this.arrRows[intRow].val = _val;
		this.arrRows[intRow].txt = _txt;
		this.arrRows[intRow].tpe = _tpe;
	}
	
	//----
	// If the added row is to be selected, select it now
	//----
	if (_sel == true)
	{
		this.sel = intRow;
		this.onChangeRel(_fkfrom);
	}
}

//----
// zXOption.prototype.copyrows
//		Copies rows from a similar object (class method). The second parameter is the current
//		value of this item, and that one that gets selected (the selected item in the _fromOption
//		could be some other value, so we don't just copy _fromOption.sel).
//		This was added 18JUN2004 by DGS to work with a change to zXWeb so that grids and matrixes
//		don't select FK option lists on any but the first row (subject to some restrictions).
//
zXOption.prototype.copyrows = function(_fromOption, _val)
{
	for (var i = 0; i < _fromOption.arrRows.length; i++)
	{
		this.arrRows[i] = new Object();

		this.arrRows[i].fkfrom = _fromOption.arrRows[i].fkfrom;
		this.arrRows[i].fkto = _fromOption.arrRows[i].fkto;
		this.arrRows[i].val = _fromOption.arrRows[i].val;
		this.arrRows[i].txt = _fromOption.arrRows[i].txt;
		this.arrRows[i].tpe = _fromOption.arrRows[i].tpe;
		
		if (this.arrRows[i].val == _val)
		{
			this.sel = i;
		}
	}
}

//----
// zXOption.prototype.onChangeRel
//		Perform the on change activity for this object from a related change (class method)
//
zXOption.prototype.onChangeRel = function(_val)
{
	var objOption;
	//var ctr = elementByName(window,this.ctr);
	var ctr = findObj(this.ctr, window);

	//----
	// Clear out the select list first:
	//----
	ctr.options.length = 0;

	this.fkval = _val;
	for (var i = 0; i < this.arrRows.length; i++)
	{
		//----
		// Include the row if any of these is true: Not a bound list; Is not a normal ("n" tpe) row
		// (i.e. the optional first (blank) or "..." last row); FKfrom equals the value passed in (which for 
		// bound lists will be some attr (probably PK) of the related (parent) field).
		//----
		if (this.bnd == false || (this.arrRows[i].tpe != "n") || this.arrRows[i].fkfrom == _val )
		{
			objOption = new Option(this.arrRows[i].txt,this.arrRows[i].val);

			//----
			// Using properties of our own to hold the fkto, array index position and type. Can then be 
			// accessed without referring back to the array:
			//----
			objOption.fkto = this.arrRows[i].fkto;
			objOption.arrIndx = i;
			objOption.tpe = this.arrRows[i].tpe;

			ctr.options[ctr.options.length] = objOption;
			
			//----
			// If this row has the initial value, select it:
			//----
			if (i == this.sel)
			{
				ctr.selectedIndex = ctr.options.length - 1;
			}
		}
	}

	//----
	// Medium and large entities need an additional option of dots (...) to invoke the FK popup
	// Type is "d" (dots). Add a few spaces to ensure the drop down is at least a certain width.
	//----	
	if (this.esize != "s")
	{
		//----
		// If there is nothing in the list, first need an additional option of blank (-), or the
		// user cannot invoke the 'onChange' by selecting the dots. This does mean could select this
		// option and it wouldn't be valid for a mandatory field, but zx classes catch that.
		//----	
		if (ctr.options.length == 0)
		{
			objOption = new Option("-","-");
			objOption.fkto = "";
			objOption.tpe = "b";
			ctr.options[ctr.options.length] = objOption;
		}
		objOption = new Option("...            ","-");
		objOption.fkto = "";
		objOption.tpe = "d";
		ctr.options[ctr.options.length] = objOption;
	}

}

//----
// zXOption.prototype.onChange
//		Perform the on change activity for this object (class method)
//
zXOption.prototype.onChange = function(_row)
{
	var fkval = "";
	var ctr;

	//ctr= elementByName(window,this.ctr);
	ctr=  findObj(this.ctr, window);
	
	if (_row >= 0)
	{
		if (ctr.options[_row].tpe == "b")
		{
			//----
			// Blank selected - blank out the value (optional first row "-")
			//----
			ctr.value = "";
			this.sel = 0;
		}
		else if (ctr.options[_row].tpe == "n")
		{
			//----
			// Normal row selected - note its position, in case we want to reposition on it
			// following a cancelled FK popup
			//----
			this.sel = ctr.options[_row].arrIndx;
		}
		else if (ctr.options[_row].tpe == "d")
		{
			//----
			// Dots selected (...) - prompt for another row for a medium/large entity ("..." row). 
			// Only pass the FK val if this is a bound list. For non-bound we don't want to restrict 
			// the list.
			//----
			if (this.bnd == true)
			{
				fkval = this.fkval;
			}
			
			zXFKPopup(this.esize,			// _esize
					  this.session, 		// _session
					  0,					// _type
					  this.name,			// _ctr
					  this.boname,			// _e
					  this.attr,			// _attr 
					  this.fkboname,		// _fke
					  this.fkfromattr,		// _fka
					  this.fktoattr,		// _fkta
					  fkval);				// _fkval
			
			//----
			// In case the user closed the FK popup without selecting, reposition on the latest value of this.sel
			//----
			this.onChangeRel(this.fkval);
		}
	}
}

//----
// zXOption.prototype.lookup
//		Perform the lookup activity for this object (class method)
//
zXOption.prototype.lookup = function()
{
	if (this.arrRows[this.sel].tpe == "n")
		zXFKPopup(this.esize,						// _esize
				  this.session,						// _session
				  1,								// _type
				  this.name,						// _ctr
				  this.boname,						// _e
				  this.attr, 						// _attr
				  this.fkboname,					// _fke
				  this.fkfromattr,					// _fka
				  this.fktoattr,					// _fkta
				  this.arrRows[this.sel].val);		// _fkval
}


//----
// Function	:	zXSubmitThisUrl
// Arguments :	_url - a url
//			    _fromSubmitButton - indicates whether we need to submit the form or not;
//					if set, it is called from a button and the submit is done implicitly
//				_ctr - optional button		(added V1.4:58)
//
// Submit the form but replace the form action with the given url
//
//----
function zXSubmitThisUrl(_url, _fromSubmitButton, _ctr)
{
	// No need to submit since in clsPageflow we have changed
	// submit buttons with an URL to input of type button (in order to
	// make them listen to a return) and submitting the form is the
	// default behaviour
	
	// V1.4:10
	// Force the focus on the first element on the form (this is btw arbitrary). 
	// This is required to force processing any outstanding onChange events before
	// we submit the form. This was a potential problem with edit grids that could
	// be submitted from a URL with the submit option set.
	// Use zXSetCursorToFirstField as that handles all possibilities for different
	// field types
	
	zXSetCursorToFirstField();

	// V1.4:58
	// If we know the button that was clicked to call this, disable that button to prevent
	// it being clicked again
	
	if (_ctr != undefined)
	{
		_ctr.disabled = true;
	}
	
	document.forms[0].action = _url;
	
	if (_fromSubmitButton == undefined)
	{

		document.forms[0].submit();
	}
	else
	{
		// V1.4:58
		// If we know the button that was clicked to call this, we have disabled it above.
		// We now have to force the form to be submitted, as disabling the button prevents it.
		
		if (_ctr != undefined)
		{
			_ctr.form.submit();
		}
	}
}

//----
// Function	:	zXSubmitThisUrlConfirm
// Arguments :	_url - a url
//				_msg - Confirm message
//				_ctr - optional button		(added V1.4:58)
//
// Submit the form if user confirms but replace the form action with the given url
//
//----
function zXSubmitThisUrlConfirm(_url, _msg, _ctr)
{
	if (confirm(_msg))
	{
		// V1.4:10
		// Force the focus - see above for more information
		zXSetCursorToFirstField();

		// V1.4:58
		// Disable the submitting button to prevent a quick user clicking it again
		if (_ctr != undefined)
		{
			_ctr.disabled = true;
		}

		document.forms[0].action = _url;
		document.forms[0].submit();
	}
}

//----
// Function	:	zXSubmitThisUrlCheck
// Arguments :	_url - a url
//				_msg - Confirm message
//				_ctr - optional button		(added V1.4:58)
//
// Submit the form if user confirms but replace the form action with the given url.
// Check a particular variable and only prompt for confirm if indicates no change. 
//
//----
function zXSubmitThisUrlCheck(_url, _msg, _ctr)
{
	var blnNeedsConfirm = false;

	if(zXCheckClick != undefined )
	{
		if (zXCheckClick == 1)
		{
			blnNeedsConfirm = true;
		}
	}
	
	if (blnNeedsConfirm == true)
	{				
		if (confirm(_msg))
		{
			// V1.4:10
			// Force the focus - see above for more information
			zXSetCursorToFirstField();

			// V1.4:58
			// Disable the submitting button to prevent a quick user clicking it again
			if (_ctr != undefined)
			{
				_ctr.disabled = true;
			}

			document.forms[0].action = _url;
			document.forms[0].submit();
		}
	}
	else
	{
		// V1.4:10
		// Force the focus - see above for more information
		zXSetCursorToFirstField();

		// V1.4:58
		// Disable the submitting button to prevent a quick user clicking it again
		if (_ctr != undefined)
		{
			_ctr.disabled = true;
		}

		document.forms[0].action = _url;
		document.forms[0].submit();
		
	}
}

//----
// Function :	zXUpdateDivForLockedField
// Arguments :	_ctr - handle to control
//
// Update the div that is associated with the given hidden control; a div / hidden
// control pair is used for locked controls.
// This function is called in the onChange routine of the hidden field
//----
function zXUpdateDivForLockedField(_ctr)
{
	//----
	// HAS NOT YET BEEN TESTED
	//----
	eval('div' + _ctr + '.value=' + _ctr + '.value');
}

//----
// Function	:	zXOnChangeSearchOps
// Parms	:	_ctr - Handle to control with  search operands
//				_ctrLL - Handle to control for lower limit input
//				_ctrSpan - Handle to span for upper limit input control
//
// Make upperlimit control visible / invisible whether between operand has
// been selected
// And first, make lowerlimit control disabled when the operator involves null 
// (as the operand is not applicable in those instances). We could make it
// invisible using a span like the UL operand, but that causes the display row 
// size to change when it is multiline - looks neater to just disable (and clear any value).
//
//----
function zXOnChangeSearchOps(_ctr, _ctrLL, _ctrSpan)
{

    if (isNS4) {
        objElement = document.layers[_ctrSpan];
    } else if (isIE4) {
        objElement = document.all[_ctrSpan];
    } else if (isIE5 || isNS6) {
        objElement = document.getElementById(_ctrSpan);
    }
    
	if(_ctr.value.match("isNull") || _ctr.value.match("notNull")) {
		_ctrLL.disabled=true;
		_ctrLL.value = "";
	} else {
		_ctrLL.disabled=false;
	}
	
	if (objElement != "") {
	
		if(_ctr.value.match("between") || _ctr.value.match("btwn")) {
			if(isNS4 || isIE4){
	            objElement.style.visibility ="visible";
		    } else { 
	            objElement.style.display = "" ;
	        }
		} else {
			if(isNS4 || isIE4){
	            objElement.style.visibility ="hidden";
		    } else { 
	            objElement.style.display = "none" ;
	        }
		}
	}
}


//----
// zXAddShow
//		Shows a popup in which a new instance of the given item can be created. Used in
//      conjunction with zXAddReturn, which takes the details of the newly-created row
//      and loads them into the option list. Gives users the ability to create FK entries
//      on the fly.
//
// _entity  - Name of the business object entity to be created
// _objName - Name of the js object (option list)
// _session - zX session id 
//----
function zXAddShow(_entity,_objName, _session)
{
	var strURL;
	var intHeight;
	var intWidth;
  	var dtmTimeStamp = new Date();

	strURL = "../" + SCRPT + "/zXGPF." + SCRPT + "?-pf=zXAdd&-a=editNew&-sa=insert&-e=" + _entity + "&-obj=" + _objName + "&-s=" + _session;

	intHeight=550;
	intWidth=700;

 	var parms = "toolbar=no,location=no,left=0,top=0";
 		parms += ",screenX=50,screenY=50";
 		parms += ",height=" + intHeight;
 		parms += ",width=" + intWidth;
		parms += ",directories=no,status=yes,menubar=no,scrollbars=no,copyhistory=no,resizable=no";

  	win = parent.window.open(strURL, "Win" + dtmTimeStamp.valueOf(), parms);

	win.opener = self;

}

//----
// zXAddReturn
//		Add the details for the new row to an existing drop down list
//
// _objName - Name of the js object (option list)
// _newPK   - PK of the new instance of the entity
// _newText - Text of the new instance of the entity, to be shown in the option list
//----
function zXAddReturn(_objName,_newPK,_newText)
{
	var strTmp;

	strTmp = _objName + ".row('','" + _newPK + "','" + _newPK + "','" + _newText + "',true,'n')";
	eval(strTmp);
}

//----
// zXExprEdit
//		Start the popup expression editor
//
// _s - session
// _ctr  - Handle to control whose contents is an expression that wants editing
//----
function zXExprEdit(_s, _ctr) {
	var strURL;
	var intHeight;
	var intWidth;
	var ctr;
  	var dtmTimeStamp = new Date();
  	
  	ctr =  findObj(_ctr, window);
  	
	strURL = "../" + SCRPT + "/zXExprEdit." + SCRPT + "?-pf=zXExprEdit&-a=asp.start&-ctr=" + _ctr + "&-s=" + _s;

	intHeight=500;
	intWidth=800;
	
 	var parms = "toolbar=no,location=no,left=0,top=0";
 	parms += ",screenX=50,screenY=50";
 	parms += ",height=" + intHeight;
 	parms += ",width=" + intWidth;
	parms += ",directories=no,status=no,menubar=no,scrollbars=yes,copyhistory=no,resizable=no";
	
  	win = parent.window.open(strURL, "Win" + dtmTimeStamp.valueOf(), parms);
  	
  	// Does not work in Firefox anyhow.
	// win.clipboardData.setData("text", ctr.value);
	
	win.opener = self;
}

//----
// zXExprEditClose
//		Closing the expression editor popup so ready to populate the originating control
//
// _ctrName - Name of the control
// _expr - Replacing text
//----
function zXExprEditClose(_ctrName, _expr)
{
	var ctr;

	// ctr= elementByName(top.window.opener,_ctrName);
	ctr =  findObj(_ctrName, top.window.opener);
	
	if (ctr != null)
	{
		ctr.value = _expr;
		if (ctr.type != "textarea" && ctr.type != "hidden" ) ctr.onChange();		
	}
}

//----
// zXExprEditTrack
//		Store position when clicked in text area; should NOT be called directly by
//		developer
//
// _ctr - Handle to the textArea control
//----
var objTextRange;

function zXExprEditTrack(_ctr)
{
	if ( _ctr.createTextRange )
	{
		objTextRange = document.selection.createRange();
	}	
}

//----
// zXExprEditPaste
//		Called from onChange event on function pulldown list; paste
//		the function into the textArea at the correct position
//
// _ctr - Handle to the textArea control
// _func - Handle to control with functions
//----
function zXExprEditPaste(_ctr, _func)
{
  	if ( _func.value != "-" ) 
	{
		_ctr.focus();

		if ( _ctr.createTextRange && objTextRange )
		{
			objTextRange.text = _func.value;
			objTextRange.findText(_func.value, -1);
		}
		else
		{
			_ctr.innerText = _ctr.innerText + _func.value;
		}

		_func.selectedIndex = 0;
	}
}

//----
// Function	:	zXMultilineOnKeyUp
//				onKeyUp function handler, used by multiine fields 
// In		:	control, length
// Out		:	If control value exceeds length, is truncated
//
//----
function zXMultilineOnKeyUp(_ctr, _length) 
{
	// Always set zxDirty
	zXDirty = 1;
	
    if(_ctr.value.length>_length)
    {
		_ctr.value=_ctr.value.substr(0,_length);
    }
}

//----
// Function	:	zXListSelectDisable
//				Optionally called by listforms when the select icon is clicked.
//				Prevents the icon being clicked a second time, and changes the image.
// In		:	control
// Out		:	
//
//----
function zXListSelectDisable(_ctr) 
{
	_ctr.src = imgDir + "listItemDisabled.gif";
	_ctr.title='Item already selected - not available';
	_ctr.onmouseout='';
	_ctr.onmouseover='';
	_ctr.onmousedown='';
}

//----
// Function	:	zXListSelected
//				Optionally called by listforms when the select icon is clicked.
//				Changes the select icon but does not prevent it being clicked again.
// In		:	control
// Out		:	
//
//----
function zXListSelected(_ctr) 
{
	_ctr.src = imgDir + "listItemSelected.gif";
	_ctr.onmouseout='';
	_ctr.onmouseover='';
}

//----
// zXDocAttrView
//		View a document associated with a zXDoc pointed to by the value of a control
//
//
// _s - session
// _ctr  - Handle to control whose contents is the PK to a zXDoc
//
// This is likely to be used as a ref on an enhancer on the zXDoc FK attr field
// This field MUST be locked and a typical (fixed / notouch) URL would be:
//
// zXDocAttrView('#qs.-s#', #ctr.tsk.clntDcmnt#)
//
//----
function zXDocAttrView(_s, _ctr)
{
	var strURL;
	var intHeight;
	var intWidth;
  	var dtmTimeStamp = new Date();

	if(_ctr.value != undefined && _ctr.value != '')
	{
		strURL = "../" + SCRPT + "/zXDoc." + SCRPT + "?-pf=zXDoc&-a=docView";
		strURL += "&-s=" + _s;
		strURL += "&-pk.zXDoc=" + _ctr.value;

		zXPopupStep1(_s, strURL, 1, 1);
	}

}

//----
// zXDocAttrUpload
//		Allow user to upload a document; create a zXDoc instance and store pk of this
//		in control and the label of the zXDoc in the label control
//
//
// _s - session
// _ctr  - Handle to control whose contents is the PK to a zXDoc
// _ctrLabel - Handle to control that contains label
// _docType - Name of document type that zXDoc is for
//
// This is likely to be used as a ref on an enhancer on the zXDoc FK attr field
// This field MUST be locked and a typical (fixed / notouch) URL would be:
//
// zXDocAttrUpload('#qs.-s#', #ctr.tsk.clntDcmnt#, #ctrDivLock.tsk.clntDcmnt#, 'ATTACH')
//
//----
function zXDocAttrUpload(_s, _ctr, _ctrLabel, _docType)
{
	var intHeight;
	var intWidth;
	var strURL;
  	var dtmTimeStamp = new Date();
	var parms;
	var win;
	
	intHeight=250;
	intWidth=500;

 	parms = "toolbar=no,location=no,left=0,top=0";
 	parms += ",screenX=50,screenY=50";
 	parms += ",height=" + intHeight;
 	parms += ",width=" + intWidth;
	parms += ",directories=no,status=no,menubar=no,scrollbars=no,copyhistory=no,resizable=yes";

	strURL = "../" + SCRPT + "/zXDoc." + SCRPT + "?-pf=zXDoc&-a=docAttr.upload";
	strURL += "&-s=" + _s;
	strURL += "&-ctr=" + _ctr.name;
	strURL += "&-ctrLabel=" + _ctrLabel.id;
	strURL += "&-docType=" + _docType;

  	win = parent.window.open(strURL, "Win" + dtmTimeStamp.valueOf(), parms);
  	
	win.opener = self;
}

//----
// zXDocAttrEdit
//		Allow user to edit tke zXDoc keywords
//
//
// _s - session
// _ctr  - Handle to control whose contents is the PK to a zXDoc
// _ctrLabel - Handle to control that contains label
//
// This is likely to be used as a ref on an enhancer on the zXDoc FK attr field
// This field MUST be locked and a typical (fixed / notouch) URL would be:
//
// zXDocAttrEdit('#qs.-s#', #ctr.tsk.clntDcmnt#, #ctrDivLock.tsk.clntDcmnt#)
//
//----
function zXDocAttrEdit(_s, _ctr, _ctrLabel)
{
	var intHeight;
	var intWidth;
	var strURL;
  	var dtmTimeStamp = new Date();
	var parms;
	var win;
	
	intHeight=350;
	intWidth=500;

	if(_ctr.value != undefined && _ctr.value != '')
	{
 		parms = "toolbar=no,location=no,left=0,top=0";
 		parms += ",screenX=50,screenY=50";
 		parms += ",height=" + intHeight;
 		parms += ",width=" + intWidth;
		parms += ",directories=no,status=no,menubar=no,scrollbars=no,copyhistory=no,resizable=yes";

		strURL = "../" + SCRPT + "/zXDoc." + SCRPT + "?-pf=zXDoc&-a=docAttr.edit";
		strURL += "&-s=" + _s;
		strURL += "&-ctr=" + _ctr.name;
		strURL += "&-ctrLabel=" + _ctrLabel.id;
		strURL += "&-pk.doc=" + _ctr.value;

  		win = parent.window.open(strURL, "Win" + dtmTimeStamp.valueOf(), parms);
  	
		win.opener = self;
	}
}

//----
// zXDocAttrEdit
//		Allow user to clear zXDoc field
//
//
// _s - session
// _ctr  - Handle to control whose contents is the PK to a zXDoc
// _ctrLabel - Handle to control that contains label
// _confirm - Optional confirmation
//
// This is likely to be used as a ref on an enhancer on the zXDoc FK attr field
// This field MUST be locked and a typical (fixed / notouch) URL would be:
//
// zXDocAttrReset('#qs.-s#', #ctr.tsk.clntDcmnt#, #ctrDivLock.tsk.clntDcmnt#, 'Are you sure?')
//
//----
function zXDocAttrReset(_s, _ctr, _ctrLabel, _confirm)
{
	if (_confirm == undefined || _confirm == '')
	{
		_ctr.value = '';
		_ctrLabel.innerHTML = "-";
	}
	else
	{
		if (confirm(_confirm))
		{
			_ctr.value = '';
			_ctrLabel.innerHTML = "-";
		}
	}
}

//====
// listSelect
//	Provides a popup with search and list, and the ability to select multiple
//  to be added back into a field on the underlying window. Typically used to
//  select e-mail addresses.
//
// _s - session
// _ctr - handle to control to store selected details
// _pf - pageflow to be popped up (must use zXGPF.asp). Can be appended by QS items.
//=====
function listSelect(_s, _ctr, _pf)
{
	var strUrl;
	
	strUrl = "../" + SCRPT + "/zXGPF." + SCRPT + "?-pf=" + _pf;
	strUrl += "&-s=" + _s;	
	strUrl += "&-ctr=" + _ctr.name;	

	zXWindow(strUrl);
}

//====
// listSelectStep2
//	list select, step 2 (i.e. where the underlying field is populated
//			with the selected items
//
// _ctr - handle to control to store selected details
// _item - selected details
//
//=====
function listSelectStep2(_ctr, _item, _separator)
{
	var objWindow;
	var col;
	
	objWindow = top.window.opener;

	if (_separator == undefined)
	{
		_separator = ", ";
	}

	col = objWindow.document.all.tags("INPUT");
	if (col != null) 
	{
	    for (i = 0; i < col.length; i++) 
		{
			if(col[i].name == _ctr)
			{
				if ( col[i].value != '' )
				{
					col[i].value = col[i].value + _separator + unescape(_item);
				}
				else
				{
					col[i].value = unescape(_item);
				}

				return true;
			}
		}
	}
}

//----
// setAllRadios
//
// Sets all radio buttons of a particular value to checked
//
// _name = name of the control (without any trailing id to make unique)
// _value = the value of the radio button i.e. the one to be selected out of the group
//
//----
function setAllRadios(_name, _value)
{
	for(var e = 0; e < window.document.all.length; ++e)
	{
		if (window.document.all(e).name != undefined)
		{
			if(window.document.all(e).name.toLowerCase().substr(0, _name.length) == _name && isVisible(window.document.all(e)))
			{
				if (window.document.all(e).value == _value)
				{
					window.document.all(e).checked = true;
				}
			}
		}
	}
}


//	written	by Tan Ling	Wee	on 2 Dec 2001
//	last updated 20 June 2003
//	email :	fuushikaden@yahoo.com
// Added support for ddMMyyyy often used in the framework : 
var	fixedX = -1;			// x position (-1 if to appear below control)
var	fixedY = -1;			// y position (-1 if to appear below control)
var startAt = 1;				// 0 - sunday ; 1 - monday
var showWeekNumber = 1;	// 0 - don't show; 1 - show
var showToday = 1;				// 0 - don't show; 1 - show

var gotoString = "Go To Current Month";
var todayString = "Today is";
var weekString = "Wk";

var scrollLeftMessage = "Click to scroll to previous month. Hold mouse button to scroll automatically.";
var scrollRightMessage = "Click to scroll to next month. Hold mouse button to scroll automatically.";
var selectMonthMessage = "Click to select a month.";
var selectYearMessage = "Click to select a year.";
var selectDateMessage = "Select [date] as date."; // do not replace [date], it will be replaced by date.

var	crossobj, crossMonthObj, crossYearObj, monthSelected, yearSelected, dateSelected, omonthSelected, oyearSelected, odateSelected, monthConstructed, yearConstructed, intervalID1, intervalID2, timeoutID1, timeoutID2, ctlToPlaceValue, ctlNow, dateFormat, nStartingYear;

var	bPageLoaded=false;

var	ie=document.all;
var	ns4=document.layers;

var	dom=document.getElementById;

var	today =	new	Date();
var	dateNow	 = today.getDate();
var	monthNow = today.getMonth();
var	yearNow	 = today.getYear();
var	imgsrc = new Array("drop1.gif","drop2.gif","left1.gif","left2.gif","right1.gif","right2.gif");
var	img	= new Array();
var bgColor = "#0000aa";

var bShow = false;
var bAppend = false;

/** 
 * hides <select> and <applet> objects (for IE only) 
 **/
function hideElement( elmID, overDiv ) {
	if( ie ) {
		for( i = 0; i < document.all.tags( elmID ).length; i++ ) {
			obj = document.all.tags( elmID )[i];
			if( !obj || !obj.offsetParent ) {
        		continue;
      		}
      		
      		// Find the element's offsetTop and offsetLeft relative to the BODY tag.
      		objLeft   = obj.offsetLeft;
      		objTop    = obj.offsetTop;
      		objParent = obj.offsetParent;
      		
      		while( objParent.tagName.toUpperCase() != "BODY" ) {
      			objLeft  += objParent.offsetLeft;
      			objTop   += objParent.offsetTop;
      			objParent = objParent.offsetParent;
      		}
      		
      		objHeight = obj.offsetHeight;
      		objWidth = obj.offsetWidth;
      		
      		if(( overDiv.offsetLeft + overDiv.offsetWidth ) <= objLeft );
      		else if(( overDiv.offsetTop + overDiv.offsetHeight ) <= objTop );
      		else if( overDiv.offsetTop >= ( objTop + objHeight ));
      		else if( overDiv.offsetLeft >= ( objLeft + objWidth ));
      		else {
      			obj.style.visibility = "hidden";
      		}  
		}
	}
}
 
/**
 * unhides <select> and <applet> objects (for IE only)
 **/
function showElement( elmID ) {
	if( ie ) {
		for( i = 0; i < document.all.tags( elmID ).length; i++ ) {
			obj = document.all.tags( elmID )[i];
			
			if( !obj || !obj.offsetParent ) {
				continue;
			}
			
			obj.style.visibility = "";
		}
	}
}

function HolidayRec (d, m, y, desc) {
	this.d = d;
	this.m = m;
	this.y = y;
	this.desc = desc;
}

var HolidaysCounter = 0;
var Holidays = new Array();

function addHoliday (d, m, y, desc) {
	Holidays[HolidaysCounter++] = new HolidayRec ( d, m, y, desc );
}

if (dom) {
	for	(i=0;i<imgsrc.length;i++) {
		img[i] = new Image;
		img[i].src = imgDir + imgsrc[i];
	}
	
	document.write ("<div onclick='bShow=true' onMouseover='isDragHot=true;if(isNS4)isDragHotNS4(calendar);' onMouseout='isDragHot=false' id='calendar' style='z-index:+999;position:absolute;visibility:hidden;'><table	width="
							+((showWeekNumber==1)?250:220) + 
							" style='font-family:arial;font-size:11px;border-width:1;border-style:solid;border-color:#a0a0a0;font-family:arial; font-size:11px}' bgcolor='#ffffff'><tr bgcolor='" + bgColor + "' style='cursor:move;' id='draggable'><td><table width='"+((showWeekNumber==1)?248:218)+"'><tr><td style='padding:2px;font-family:arial; font-size:11px;'><font color='#ffffff'><B><span id='caption'></span></B></font></td><td align=right><a href='javascript:hideCalendar()'><IMG SRC='"+imgDir+"close.gif' WIDTH='15' HEIGHT='13' BORDER='0' ALT='Close the Calendar'></a></td></tr></table></td></tr><tr><td style='padding:5px' bgcolor=#ffffff><span id='content'></span></td></tr>");
	
	if (showToday==1) {
		document.write ("<tr bgcolor=#f0f0f0><td style='padding:5px' align=center><span id='lblToday'></span></td></tr>");
	}
		
	document.write ("</table></div><div id='selectMonth' style='z-index:+999;position:absolute;visibility:hidden;'></div><div id='selectYear' style='z-index:+999;position:absolute;visibility:hidden;'></div>");
}

/** Long month form **/
var	monthName =	new	Array("January","February","March","April","May","June","July","August","September","October","November","December");
/** Short month form **/
var	monthName2 = new Array("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec");

if (startAt==0) {
	dayName = new Array	("Sun","Mon","Tue","Wed","Thu","Fri","Sat");
} else {
	dayName = new Array	("Mon","Tue","Wed","Thu","Fri","Sat","Sun");
}

var	styleAnchor="text-decoration:none;color:black;";
var	styleLightBorder="border-style:solid;border-width:1px;border-color:#a0a0a0;";

/**
 * Change the image source (IE only)
 **/
function swapImage(srcImg, destImg){
	if (ie)	{ document.getElementById(srcImg).setAttribute("src", imgDir + destImg); }
}

/**
 * Init the calendar, this will only get executed once :
 **/
function init()	{
	// Javascript singleton - Ensure this only gets executed once.
	if (!ns4 && !bPageLoaded) {
		if (!ie) { yearNow += 1900; }
		
		crossobj=(dom)?document.getElementById("calendar").style : ie? document.all.calendar : document.calendar;
		hideCalendar();
		
		crossMonthObj=(dom)?document.getElementById("selectMonth").style : ie? document.all.selectMonth	: document.selectMonth;
		crossYearObj=(dom)?document.getElementById("selectYear").style : ie? document.all.selectYear : document.selectYear;
		
		monthConstructed=false;
		yearConstructed=false;
		
		if (showToday==1) {
			document.getElementById("lblToday").innerHTML =	todayString + " <a onmousemove='window.status=\""+gotoString+"\"' onmouseout='window.status=\"\"' title='"+gotoString+"' style='"+styleAnchor+"' href='javascript:monthSelected=monthNow;yearSelected=yearNow;constructCalendar();'>"+dayName[(today.getDay()-startAt==-1)?6:(today.getDay()-startAt)]+", " + dateNow + " " + monthName[monthNow].substring(0,3)	+ "	" +	yearNow	+ "</a>";
		}

		sHTML1="<span id='spanLeft'	style='border-style:solid;border-width:1;border-color:#3366FF;cursor:pointer' onmouseover='swapImage(\"changeLeft\",\"left2.gif\");this.style.borderColor=\"#88AAFF\";window.status=\""+scrollLeftMessage+"\"' onclick='javascript:decMonth()' onmouseout='clearInterval(intervalID1);swapImage(\"changeLeft\",\"left1.gif\");this.style.borderColor=\"#3366FF\";window.status=\"\"' onmousedown='clearTimeout(timeoutID1);timeoutID1=setTimeout(\"StartDecMonth()\",500)'	onmouseup='clearTimeout(timeoutID1);clearInterval(intervalID1)'>&nbsp<IMG id='changeLeft' SRC='"+imgDir+"left1.gif' width=10 height=11 BORDER=0>&nbsp</span>&nbsp;";
		sHTML1+="<span id='spanRight' style='border-style:solid;border-width:1;border-color:#3366FF;cursor:pointer'	onmouseover='swapImage(\"changeRight\",\"right2.gif\");this.style.borderColor=\"#88AAFF\";window.status=\""+scrollRightMessage+"\"' onmouseout='clearInterval(intervalID1);swapImage(\"changeRight\",\"right1.gif\");this.style.borderColor=\"#3366FF\";window.status=\"\"' onclick='incMonth()' onmousedown='clearTimeout(timeoutID1);timeoutID1=setTimeout(\"StartIncMonth()\",500)'	onmouseup='clearTimeout(timeoutID1);clearInterval(intervalID1)'>&nbsp<IMG id='changeRight' SRC='"+imgDir+"right1.gif'	width=10 height=11 BORDER=0>&nbsp</span>&nbsp";
		sHTML1+="<span id='spanMonth' style='border-style:solid;border-width:1;border-color:#3366FF;cursor:pointer'	onmouseover='swapImage(\"changeMonth\",\"drop2.gif\");this.style.borderColor=\"#88AAFF\";window.status=\""+selectMonthMessage+"\"' onmouseout='swapImage(\"changeMonth\",\"drop1.gif\");this.style.borderColor=\"#3366FF\";window.status=\"\"' onclick='popUpMonth()'></span>&nbsp;";
		sHTML1+="<span id='spanYear' style='border-style:solid;border-width:1;border-color:#3366FF;cursor:pointer' onmouseover='swapImage(\"changeYear\",\"drop2.gif\");this.style.borderColor=\"#88AAFF\";window.status=\""+selectYearMessage+"\"'	onmouseout='swapImage(\"changeYear\",\"drop1.gif\");this.style.borderColor=\"#3366FF\";window.status=\"\"'	onclick='popUpYear()'></span>&nbsp;";
		
		document.getElementById("caption").innerHTML  =	sHTML1;

		bPageLoaded=true;
	}
}

function hideCalendar()	{
	crossobj.visibility="hidden";
	if (crossMonthObj != null){crossMonthObj.visibility="hidden";}
	if (crossYearObj !=	null){crossYearObj.visibility="hidden";}
	
	showElement( 'SELECT' );
	showElement( 'APPLET' );
}

function padZero(num) {
	return (num	< 10)? '0' + num : num ;
}

// IMPORTANT - This writes out the date :
function constructDate(d,m,y) {
	sTmp = dateFormat;
	
	sTmp = sTmp.replace	("yyyy","<y>");
	sTmp = sTmp.replace	("yyy","<y>");
	sTmp = sTmp.replace ("yy", "<y2>");
	sTmp = sTmp.replace	("<y2>", padZero(y%100));
	sTmp = sTmp.replace	("<y>", y);
	
	sTmp = sTmp.replace	("dd","<e>");
	sTmp = sTmp.replace	("d","<d>");
	sTmp = sTmp.replace	("<e>",padZero(d));
	sTmp = sTmp.replace	("<d>",d);
	
	sTmp = sTmp.replace	("mmmm","<p>");
	sTmp = sTmp.replace	("mmm","<o>");
	sTmp = sTmp.replace	("mm","<n>");
	sTmp = sTmp.replace	("m","<m>");
	sTmp = sTmp.replace	("<m>", m+1);
	sTmp = sTmp.replace	("<n>", padZero(m+1));
	sTmp = sTmp.replace	("<p>", monthName[m]);
	sTmp = sTmp.replace	("<o>", monthName2[m]);
	
	return sTmp.replace ();
}

function closeCalendar() {
	var	sTmp;
	
	if (bAppend) {
		if (ctlToPlaceValue.value.length > 0) {
			ctlToPlaceValue.value = ctlToPlaceValue.value + ",";
		}
		ctlToPlaceValue.value = ctlToPlaceValue.value + constructDate(dateSelected,monthSelected,yearSelected);
		
	} else {
		hideCalendar();
		ctlToPlaceValue.value =	constructDate(dateSelected,monthSelected,yearSelected);
		
	}
	
	//----
	// Notify the control that the value has been changed programmtically
	//----
	ctlToPlaceValue.onchange();
}

/*** Month Pulldown	***/

function StartDecMonth() {
	intervalID1=setInterval("decMonth()",80);
}

function StartIncMonth() {
	intervalID1=setInterval("incMonth()",80);
}

function incMonth () {
	monthSelected++;
	if (monthSelected>11) {
		monthSelected=0;
		yearSelected++;
	}
	constructCalendar();
}

function decMonth () {
	monthSelected--;
	if (monthSelected<0) {
		monthSelected=11;
		yearSelected--;
	}
	constructCalendar();
}

function constructMonth() {
	popDownYear();
	if (!monthConstructed) {
		sHTML =	"";
		for	(i=0; i<12;	i++) {
			sName =	monthName[i];
			if (i==monthSelected){
				sName = "<B>" + sName + "</B>";
			}
			sHTML += "<tr><td id='m" + i + "' onmouseover='this.style.backgroundColor=\"#FFCC99\"' onmouseout='this.style.backgroundColor=\"\"' style='cursor:pointer' onclick='monthConstructed=false;monthSelected=" + i + ";constructCalendar();popDownMonth();event.cancelBubble=true'>&nbsp;" + sName + "&nbsp;</td></tr>";
		}
		
		document.getElementById("selectMonth").innerHTML = "<table width=70	style='font-family:arial; font-size:11px; border-width:1; border-style:solid; border-color:#a0a0a0;' bgcolor='#FFFFDD' cellspacing=0 onmouseover='clearTimeout(timeoutID1)'	onmouseout='clearTimeout(timeoutID1);timeoutID1=setTimeout(\"popDownMonth()\",100);event.cancelBubble=true'>" +	sHTML +	"</table>";
		
		monthConstructed=true
	}
}

function popUpMonth() {
	constructMonth();
	crossMonthObj.visibility = (dom||ie)? "visible"	: "show";
	crossMonthObj.left = parseInt(crossobj.left) + 50;
	crossMonthObj.top =	parseInt(crossobj.top) + 26;
	
	hideElement( 'SELECT', document.getElementById("selectMonth") );
	hideElement( 'APPLET', document.getElementById("selectMonth") );			
}

function popDownMonth()	{
	crossMonthObj.visibility= "hidden";
}

/*** Year Pulldown ***/

function incYear() {
	for	(i=0; i<7; i++){
		newYear	= (i+nStartingYear)+1;
		if (newYear==yearSelected) { txtYear =	"&nbsp;<B>"	+ newYear +	"</B>&nbsp;"; }
		else { txtYear =	"&nbsp;" + newYear + "&nbsp;"; }
		document.getElementById("y"+i).innerHTML = txtYear;
	}
	nStartingYear ++;
	bShow=true;
}

function decYear() {
	for	(i=0; i<7; i++){
		newYear = (i+nStartingYear)-1;
		if (newYear==yearSelected) { txtYear =	"&nbsp;<B>"	+ newYear +	"</B>&nbsp;"; }
		else { txtYear = "&nbsp;" + newYear + "&nbsp;"; }
		document.getElementById("y"+i).innerHTML = txtYear;
	}
	nStartingYear --;
	bShow=true;
}

function selectYear(nYear) {
	yearSelected=parseInt(nYear+nStartingYear);
	yearConstructed=false;
	constructCalendar();
	popDownYear();
}

function constructYear() {
	popDownMonth();
	sHTML = "";
	if (!yearConstructed) {
		
		sHTML = "<tr><td align='center'	onmouseover='this.style.backgroundColor=\"#FFCC99\"' onmouseout='clearInterval(intervalID1);this.style.backgroundColor=\"\"' style='cursor:pointer'	onmousedown='clearInterval(intervalID1);intervalID1=setInterval(\"decYear()\",30)' onmouseup='clearInterval(intervalID1)'>-</td></tr>";
		
		j = 0;
		nStartingYear =	yearSelected-3;
		for	(i=(yearSelected-3); i<=(yearSelected+3); i++) {
			sName =	i;
			if (i==yearSelected){
				sName =	"<B>" +	sName +	"</B>";
			}
			
			sHTML += "<tr><td id='y" + j + "' onmouseover='this.style.backgroundColor=\"#FFCC99\"' onmouseout='this.style.backgroundColor=\"\"' style='cursor:pointer' onclick='selectYear("+j+");event.cancelBubble=true'>&nbsp;" + sName + "&nbsp;</td></tr>";
			j ++;
		}
		
		sHTML += "<tr><td align='center' onmouseover='this.style.backgroundColor=\"#FFCC99\"' onmouseout='clearInterval(intervalID2);this.style.backgroundColor=\"\"' style='cursor:pointer' onmousedown='clearInterval(intervalID2);intervalID2=setInterval(\"incYear()\",30)'	onmouseup='clearInterval(intervalID2)'>+</td></tr>";
		
		document.getElementById("selectYear").innerHTML	= "<table width=44 style='font-family:arial; font-size:11px; border-width:1; border-style:solid; border-color:#a0a0a0;'	bgcolor='#FFFFDD' onmouseover='clearTimeout(timeoutID2)' onmouseout='clearTimeout(timeoutID2);timeoutID2=setTimeout(\"popDownYear()\",100)' cellspacing=0>"	+ sHTML	+ "</table>";
		
		yearConstructed	= true;
	}
}

function popDownYear() {
	clearInterval(intervalID1);
	clearTimeout(timeoutID1);
	clearInterval(intervalID2);
	clearTimeout(timeoutID2);
	crossYearObj.visibility= "hidden";
}

function popUpYear() {
	var	leftOffset;
	
	constructYear();
	crossYearObj.visibility	= (dom||ie)? "visible" : "show";
	leftOffset = parseInt(crossobj.left) + document.getElementById("spanYear").offsetLeft;
	/** The pixel position for IE is different **/
	if (ie) {
		leftOffset += 6;
	}
	
	crossYearObj.left =	leftOffset;
	crossYearObj.top = parseInt(crossobj.top) + 26;
}

/*** calendar ***/
function WeekNbr(n) {
	// Algorithm used:
	// From Klaus Tondering's Calendar document (The Authority/Guru)
	// hhtp://www.tondering.dk/claus/calendar.html
	// a = (14-month) / 12
	// y = year + 4800 - a
	// m = month + 12a - 3
	// J = day + (153m + 2) / 5 + 365y + y / 4 - y / 100 + y / 400 - 32045
	// d4 = (J + 31741 - (J mod 7)) mod 146097 mod 36524 mod 1461
	// L = d4 / 1460
	// d1 = ((d4 - L) mod 365) + L
	// WeekNumber = d1 / 7 + 1
	
	year = n.getFullYear();
	month = n.getMonth() + 1;
	if (startAt == 0) {
		day = n.getDate() + 1;
	} else {
		day = n.getDate();
	}
	
	a = Math.floor((14-month) / 12);
	y = year + 4800 - a;
	m = month + 12 * a - 3;
	b = Math.floor(y/4) - Math.floor(y/100) + Math.floor(y/400);
	J = day + Math.floor((153 * m + 2) / 5) + 365 * y + b - 32045;
	d4 = (((J + 31741 - (J % 7)) % 146097) % 36524) % 1461;
	L = Math.floor(d4 / 1460);
	d1 = ((d4 - L) % 365) + L;
	week = Math.floor(d1/7) + 1;
	
	return week;
}

function constructCalendar () {
	var aNumDays = Array (31,0,31,30,31,30,31,31,30,31,30,31);
	
	var dateMessage;
	var startDate = new Date (yearSelected,monthSelected,1);
	var endDate;
	
	if (monthSelected==1) {
		endDate = new Date (yearSelected,monthSelected+1,1);
		endDate = new Date (endDate	- (24*60*60*1000));
		numDaysInMonth = endDate.getDate();
	} else {
		numDaysInMonth = aNumDays[monthSelected];
	}
		
	datePointer = 0;
	dayPointer = startDate.getDay() - startAt;
	
	if (dayPointer<0) {
		dayPointer = 6;
	}
	
	sHTML =	"<table	 border=0 style='font-family:verdana;font-size:10px;'><tr>";

	if (showWeekNumber==1) {
		sHTML += "<td width=27><b>" + weekString + "</b></td><td width=1 rowspan=7 bgcolor='#d0d0d0' style='padding:0px'><img src='"+imgDir+"divider.gif' width=1></td>";
	}
	
	for	(i=0; i<7; i++)	{
		sHTML += "<td width='27' align='right'><B>"+ dayName[i]+"</B></td>";
	}
	sHTML +="</tr><tr>";
	
	if (showWeekNumber==1) {
		sHTML += "<td align=right>" + WeekNbr(startDate) + "&nbsp;</td>";
	}
	
	for	( var i=1; i<=dayPointer;i++ ) {
		sHTML += "<td>&nbsp;</td>";
	}
	
	for	( datePointer=1; datePointer<=numDaysInMonth; datePointer++ ) {
		dayPointer++;
		sHTML += "<td align=right>";
		sStyle=styleAnchor;
		if ((datePointer==odateSelected) &&	(monthSelected==omonthSelected)	&& (yearSelected==oyearSelected))
		{ sStyle+=styleLightBorder; }
		
		sHint = "";
		for (k=0;k<HolidaysCounter;k++) {
			if ((parseInt(Holidays[k].d)==datePointer)&&(parseInt(Holidays[k].m)==(monthSelected+1))) {
				if ((parseInt(Holidays[k].y)==0)||((parseInt(Holidays[k].y)==yearSelected)&&(parseInt(Holidays[k].y)!=0))) {
					sStyle+="background-color:#FFDDDD;";
					sHint+=sHint==""?Holidays[k].desc:"\n"+Holidays[k].desc;
				}
			}
		}
		
		var regexp= /\"/g;
		sHint=sHint.replace(regexp,"&quot;");
		
		dateMessage = "onmousemove='window.status=\""+selectDateMessage.replace("[date]",constructDate(datePointer,monthSelected,yearSelected))+"\"' onmouseout='window.status=\"\"' ";
		
		if ((datePointer==dateNow)&&(monthSelected==monthNow)&&(yearSelected==yearNow))
		{ sHTML += "<b><a "+dateMessage+" title=\"" + sHint + "\" style='"+sStyle+"' href='javascript:dateSelected="+datePointer+";closeCalendar();'><font color=#ff0000>&nbsp;" + datePointer + "</font>&nbsp;</a></b>"; }
		else if	(dayPointer % 7 == (startAt * -1)+1)
		{ sHTML += "<a "+dateMessage+" title=\"" + sHint + "\" style='"+sStyle+"' href='javascript:dateSelected="+datePointer + ";closeCalendar();'>&nbsp;<font color=#909090>" + datePointer + "</font>&nbsp;</a>"; }
		else
		{ sHTML += "<a "+dateMessage+" title=\"" + sHint + "\" style='"+sStyle+"' href='javascript:dateSelected="+datePointer + ";closeCalendar();'>&nbsp;" + datePointer + "&nbsp;</a>"; }
		
		sHTML += "";
		if ((dayPointer+startAt) % 7 == startAt) { 
			sHTML += "</tr><tr>";
			if ((showWeekNumber==1)&&(datePointer<numDaysInMonth)) {
				sHTML += "<td align=right>" + (WeekNbr(new Date(yearSelected,monthSelected,datePointer+1))) + "&nbsp;</td>";
			}
		}
	}
	
	document.getElementById("content").innerHTML = sHTML;
	document.getElementById("spanMonth").innerHTML = "&nbsp;" +	monthName[monthSelected] + "&nbsp;<IMG id='changeMonth' SRC='"+imgDir+"drop1.gif' WIDTH='12' HEIGHT='10' BORDER=0>";
	document.getElementById("spanYear").innerHTML =	"&nbsp;" + yearSelected	+ "&nbsp;<IMG id='changeYear' SRC='"+imgDir+"drop1.gif' WIDTH='12' HEIGHT='10' BORDER=0>";
}

/**
 * Call this to get the whole thing started :
 **/
function popUpCalendar(ctl,	ctl2, format, _bAppend) {
	var	leftpos=0;
	var	toppos=0;
	
	bAppend = _bAppend;
	
	if (bPageLoaded) {
		if ( crossobj.visibility ==	"hidden" ) {
			ctlToPlaceValue	= ctl2;
			dateFormat=format;
			
			/** 
			 * Handle ddmmmyyy/ddmmmyyyy :
			 *
			 * This is done by converting it temporarly to "dd/mm/yyyy"
			 **/
			
			// Use temp variables so not to curropt the originals :
			tmpDateFormat = format;
			tmpValue = ctl2.value;
			
			if (tmpDateFormat == "dd mmm yyy" || tmpDateFormat == "dd mmm yyyy") {
				tmpValue = replaceAll(tmpValue, ' ', '');
				tmpDateFormat = replaceAll(tmpDateFormat, ' ', '');
			}
			
			if (tmpDateFormat == "ddmmmyyy" || tmpDateFormat == "ddmmmyyyy") {
				if (ctl2.value.length == 9) {
					tmpDateFormat = "dd/mm/yyyy";
					
					// Convert from ddmmmyyy => dd/mm/yyyy				
					month = tmpValue.substr(2,3).toUpperCase();
					for	(j=0; j<12;	j++) {
						if (month==monthName2[j].toUpperCase()) { // May case insensitive :)
							// increment ;)
							month=j + 1;
						}
					}
					tmpValue= tmpValue.substr(0,2) + "/" + month + "/" + tmpValue.substr(5,4);
				}
			}
			/** 
			 * Handle ddmmmyyy
			 **/
			 
			// Get the sepeator :
			formatChar = " ";
			aFormat	= tmpDateFormat.split(formatChar);
			if (aFormat.length<3) {
				formatChar = "/";
				aFormat	= tmpDateFormat.split(formatChar);
				
				if (aFormat.length<3) {
					formatChar = ".";
					aFormat	= tmpDateFormat.split(formatChar);
					
					if (aFormat.length<3) {
						formatChar = "-";
						aFormat	= tmpDateFormat.split(formatChar);
						
						if (aFormat.length<3) {
							// invalid date	format
							formatChar="";
						}
					}
				}
			}
			// Get the sepeator :
			
			tokensChanged =	0;
			if ( formatChar	!= "" ) {
				// use user's date
				aData =	tmpValue.split(formatChar);
				
				for	(i=0;i<3;i++) {
					if ((aFormat[i]=="d") || (aFormat[i]=="dd")) {
						dateSelected = parseInt(aData[i], 10);
						tokensChanged ++;
					} else if	((aFormat[i]=="m") || (aFormat[i]=="mm")) {
						monthSelected =	parseInt(aData[i], 10) - 1
						tokensChanged ++;
					} else if (aFormat[i]=="yyyy") {
						yearSelected = parseInt(aData[i], 10);
						tokensChanged ++;
					} else if	(aFormat[i]=="mmm") {
						for	(j=0; j<12;	j++) {
							if (aData[i]==monthName2[j]) {
								monthSelected=j;
								tokensChanged ++;
							}
						}
					} else if (aFormat[i]=="mmmm") {
						for	(j=0; j<12;	j++) {
							if (aData[i]==monthName[j]) {
								monthSelected=j;
								tokensChanged ++;
							}
						}
					}
				}
			}
			
			if ((tokensChanged!=3)||isNaN(dateSelected)||isNaN(monthSelected)||isNaN(yearSelected)) {
				dateSelected = dateNow;
				monthSelected =	monthNow;
				yearSelected = yearNow;
			}
			
			odateSelected=dateSelected;
			omonthSelected=monthSelected;
			oyearSelected=yearSelected;

			aTag = ctl;
			do {
				aTag = aTag.offsetParent;
				leftpos	+= aTag.offsetLeft;
				toppos += aTag.offsetTop;
			} while(aTag.tagName!="BODY");
			
			crossobj.left = fixedX==-1 ? ctl.offsetLeft	+ leftpos :	fixedX;
			crossobj.top = fixedY==-1 ? ctl.offsetTop +	toppos + ctl.offsetHeight +	2 : fixedY;
			constructCalendar (1, monthSelected, yearSelected);
			crossobj.visibility=(dom||ie)? "visible" : "show";
			
			hideElement( 'SELECT', document.getElementById("calendar") );
			hideElement( 'APPLET', document.getElementById("calendar") );
			
			bShow = true;
		} else {
			hideCalendar();
			if (ctlNow!=ctl) {popUpCalendar(ctl, ctl2, format);}
		}
		ctlNow = ctl;
	}
}

/**
 * Hide the calendar when it loses focus :
 **/
if (document.addEventListener) {
	document.addEventListener('click', hidecal2, false);
} else {
	document.attachEvent('onclick', hidecal2);
}
function hidecal2 () { 		
	if (bPageLoaded) {
		if (!bShow) {
			hideCalendar();
		}
		bShow = false;
	}
}

/**
 * Hide the calender when the user presses escape.
 **/
if (document.addEventListener) {
	document.addEventListener('keypress', hidecal1, true);
} else {
	document.attachEvent('onkeypress', hidecal1);
}
function hidecal1 (evt) { 
	evt = (evt) ? evt : event;
	if (bPageLoaded) {
		key = (evt.charCode)?evt.charCode:((evt.which)?evt.which:evt.keyCode);
		if (key==27) {
			hideCalendar()
		}
	}
}

function replaceAll( str, from, to ) {
    var idx = str.indexOf( from );
    while ( idx > -1 ) {
        str = str.replace( from, to );
        idx = str.indexOf( from );
    }
    return str;
}

//-----------------
// Draggable layers
//-----------------
var isDragHot=false;

//-------------------------
// dragInit
//
// evt = The event that triggered the start of drag like "onmousedown"
// whichElem = The id of the element that you want to drag.
//-------------------------
function dragInit(evt, whichElem){
	evt = (evt) ? evt : event;
	
	topElem = document.all ? "BODY" : "HTML";
	hotElem = (evt.target) ? evt.target : evt.srcElement;
	
	while (hotElem.id != "draggable" && hotElem.tagName != topElem){
		hotElem = document.all ? hotElem.parentElement : hotElem.parentNode;
	}
	
	if (hotElem.id == "draggable"){
		offsetx = evt.clientX;
		offsety = evt.clientY;
		nowX = parseInt(whichElem.style.left);
		nowY = parseInt(whichElem.style.top);
		dragEnabled = true;
		hide = true;
		
		// Actually move the layer around
		document.onmousemove = function dragIt(evt){
			if (!dragEnabled) return;
			evt = (evt) ? evt : event;
			
			if (hide) {
				showElement('SELECT');
				showElement('APPLET');
				hide = false;
			}

			whichElem.style.left = nowX + evt.clientX - offsetx; 
			whichElem.style.top= nowY + evt.clientY - offsety;
			return false;  
		}
	}
}

//-----------------------------
// isDragHotNS4
//
// Netscape 4 specific version of draggable layers.
//
// whatElem = The element to drag
//-----------------------------
function isDragHotNS4(whatElem){
	if (!isNS4) return;
	N4=eval(whatElem);
	N4.captureEvents(Event.MOUSEDOWN|Event.MOUSEUP);
	N4.onmousedown=function(evt){
		N4.captureEvents(Event.MOUSEMOVE);
		N4x=evt.x;
		N4y=evt.y;
	}
	N4.onmousemove=function(evt){
		if (isDragHot){
			N4.moveBy(evt.x-N4x,evt.y-N4y);
			return false;
		}
	}
	N4.onmouseup=function(){
		N4.releaseEvents(Event.MOUSEMOVE);
	}
}

//-----------------------------
// Allow for dragging for the calendar
// control if it is visible.
//-----------------------------
function dragCalendarInit(evt) {
	var elem = (document.getElementById)?document.getElementById("calendar"):document.all.calendar;
	if (elem != undefined) {
		visible = ((elem.visibility)?elem.visibility:elem.style.visibility) != "hidden";
		if (visible) dragInit(evt, elem);
	}
}
document.onmousedown=dragCalendarInit;

// Disable all dragging when the mouse button is up.
document.onmouseup=Function("dragEnabled=false");

/**
 * Tooltip javascript function (namespace is tt_)
 **/
var zXTT;
if (window.createPopup == undefined) {
	document.write ("<div ID='tooltip'></div>\n");
}

/**
 * tooltip - Displays a tooltip
 * Usage : zXTooltip(this, event, "msg");
 **/
function zXTooltip(owner, evt, msg){
	evt = (evt) ? evt : event;
	
	if (window.createPopup == undefined) {
		if (isNS4) {
			zXTT=document.tooltip;
			document.captureEvents(Event.MOUSEMOVE);
	 		zXTT.document.write(msg);
	 		zXTT.document.close();
	 		zXTT.visibility="visible";
	 		
		} else {
			zXTT=document.getElementById("tooltip").style;
			zXTT.visibility="visible";
	 		zXTT.display='';
	 		document.getElementById("tooltip").innerHTML=msg;
			zXTT.maxWidth = 300;
			
		}
		
		zXTT.left = evt.pageX+-10;
		zXTT.top = evt.pageY+15;
		
	} else {
		// IE divs cannot cover select list etc.. so we use createPopup.
 		zXTT = window.createPopup();
  		zXTT.document.body.innerHTML = "<html><body><div ID='tooltipvisible' style='" 
  									+ getStyle("#tooltipvisible")+ "'>" + msg 
  									+ "</div></body></html>";
		zXTT.show(0, 0, 50, 0, zXTT.document.body);
		realHeight = zXTT.document.body.scrollHeight;
		realWidth = zXTT.document.body.scrollWidth;
		zXTT.hide();
		zXTT.show(evt.x + 100, evt.y + 10, realWidth, realHeight, zXTT.document.body);
		
 	}
 	
	if (owner.attachEvent) {
		owner.attachEvent('onmouseout', zXTooltiphide);
	} else {
		owner.addEventListener('mouseout', zXTooltiphide, true);
	}
}

/**
 * tt_kill - Hides the tooltip
 **/
function zXTooltiphide(){
	if(isNS4) 
		zXTT.visibility="hidden";
	else if (isNS6)	
		zXTT.display="none";
	else
		zXTT.hide();
}

//===================
// fkloopup and related Methods
//
// Copyright 2004 Leslie A. Hensley
// hensleyl@papermountain.org
// you have a license to do what ever you like with this code
// orginally from Avai Bryant 
// http://www.cincomsmalltalk.com/userblogs/avi/blogView?entry=3268075684
//===================
var isSafari;
var isMoz;
var isIE;
if (navigator.userAgent.indexOf("Safari") > 0) {
	isSafari = true;
	isMoz = false;
	isIE = false;
} else if (navigator.product == "Gecko") {
	isSafari = false;
	isMoz = true;
	isIE = false;
} else {
	isSafari = false;
	isMoz = false;
	isIE = true;
}

//----
// fkloopup_init
//
// XMLHttp method of looking up foriegn key attributes in a
// edit/search form.
//
// _session = The current sessionid. This could be removed as we not have gSessionID.
// _fke	= The foriegn entity
// _fkwhere = Optional where condtion used to restrict the list of values.
// _e = The entity with the foriegn key
// _attr = The attribute with the foriegn key
// _inputid = The id of the input field used to diplay the result.
// _hiddenid = The id of the hidden field used to submit the result.
// _popupid = The id the div used to display the results.
// _searchiconid = The id of the icon to use to push start searches.
// _displayresultid = The id of the icon to show the selected result.
// _searchtype = The type of seach to use.
//----
function zxfkloopup_init(_session, _fke, _fkwhere, _e, _attr, _inputid, _hiddenid, _popupid, _searchiconid, _displayresultid, _searchtype, _fksearchdelay) {

	// The display input field
	var inputField = document.getElementById(_inputid);
	// The hidden input field, this value is used in the submit.
	var hiddenField = document.getElementById(_hiddenid);
	// The search icon.
	var searchControl = document.getElementById(_searchiconid);
	
	// Get the div we will use to display the results.
	var popup = document.getElementById(_popupid);
	// Get the display result icon. This should only be visible if we have selected a foriegn key.
	var display = document.getElementById(_displayresultid);
	    
	var current = 0;
	var blnDoSearch = false;
    
	// Hide the display result icon if there is no result to display.
	if (inputField.value == "" || hiddenField.value == "") {
		hideObj(display);
	}
    
	var updater = liveUpdater(constructUri, post);
    
	var autocomplete;
    
	//----
	// Default search type is a onclick of the search icon.
	//----
	if (_searchtype == undefined) { 
		_searchtype = "onclick";
	}
	
	//----
	// Set up the event traps.
	//----
	if (_searchtype == "onkeydown") {
		//----
		// USE CASE :
		// Do search as the user types.
		//----
		if (_fksearchdelay == undefined) {
			_fksearchdelay = 400;
		}
		
		addKeyListener(inputField, start);
		addKeyListener(inputField, handlespecialkeys);
		
		addListener(searchControl, "click", doSearchClick);
		addListener(searchControl, "mouseup", doSearchMouseUp);
		addListener(searchControl, "mouseout", doSearchMouseOut);
		addKeyListener(searchControl, doSearchClick);
		
		addClickListener(document, function() {popup.style.visibility = 'hidden';});
		
	} else if (_searchtype == "onblur") {
		//----
		// USE CASE :
		// As soon the input field loses focus do a search.
		//----
		if (_fksearchdelay == undefined) {
			_fksearchdelay = 0;
		}
		
		addKeyListener(inputField, handlespecialkeys);
		addListener(inputField, "blur", doBlur);
		
		addListener(searchControl, "click", doSearchClick);
		addListener(searchControl, "mouseup", doSearchMouseUp);
		addListener(searchControl, "mouseout", doSearchMouseOut);
		addKeyListener(searchControl, doSearchClick);
		
		addClickListener(document, function() {popup.style.visibility = 'hidden';});
		
	} else if (_searchtype == "onclick") {
		//----
		// USE CASE :
		// User has to explicitly click on the search icon to get the search results
		//----
		addKeyListener(inputField, handlespecialkeys);
		
		addListener(searchControl, "click", doSearchClick);
		addListener(searchControl, "mouseup", doSearchMouseUp);
		addListener(searchControl, "mouseout", doSearchMouseOut);
		addKeyListener(searchControl, doSearchClick);
		
		addClickListener(document, function() {popup.style.visibility = 'hidden';});
		
//		//----
//		// Using _search_button_ :
//		//---
//		var searchBtn = document.getElementById("_search_button_" + _hiddenid);
//		addListener(searchBtn, "click", doSearchClick);
//		addKeyListener(searchBtn, doSearchClick);
	}
	
	//----
	// Handles a search control click.
	//----
	function doSearchClick(evt) {
		evt = (evt) ? evt : event;
		var keyCode = getKeyCode(evt);
		
		if (!blnDoSearch && evt.type == 'click') {
			return;
		}
		
		//----
		// We only want to listen to a mouse click or a enter key press.
		// Otherwise exit early
		//----
		if (keyCode != undefined && keyCode != 1 && keyCode != 13) {
			return;
		}
		
		//----
		// Unlock the form control if necessary
		//----
		fklookup_unlock(evt);
		
		//----
		// Prevent form submit
		//----
		if(isIE) {
			evt.returnValue = false;
		} else {
			evt.preventDefault();
		}
		
		inputField.focus();
		
		if (searchControl != undefined && searchControl.alt != "block") {
			//----
			// Do a search
			//----
			searchControl.src = imgDir + "searching.gif";
			return updater();
			
		} else {
			searchControl.alt = "Search";
		}
    }
    
	function doSearchMouseUp(evt) {
		evt = (evt) ? evt : event;
		
		blnDoSearch = true;
	}
	
	function doSearchMouseOut(evt) {
		evt = (evt) ? evt : event;
		
		blnDoSearch = false;
	}
		
	//----
	// Unlocks the input field
	//----
	function fklookup_unlock(evt) {
		evt = (evt) ? evt : event;
		
		if (searchControl != undefined && searchControl.src.indexOf("search_icon.gif") == -1) {
			
			inputField.disabled = false;
			inputField.focus();		
			inputField.select();
			
			searchControl.src = imgDir + "search_icon.gif";
			searchControl.alt = "block";
		}
	}
	
	function doBlur(evt) {
		evt = (evt) ? evt : event;
    	
		if (display.style.visibility == "hidden") {
			if(isIE) {
				evt.returnValue = false;
			} else {
				evt.preventDefault();
			}
			
			return updater();
		} else {
			return false;
		}
	}
    
	//----
	// Used to get the url
	//----
	function constructUri() {
		var strURL = "../" + SCRPT + "/zXFKQS." + SCRPT + "?";
		strURL += "-s=" + _session; // Session
		strURL += "&-fke=" + escape(_fke);  // Foreign entity
		strURL += "&-e=" + escape(_e);  // Entity with the foriegn
		strURL += "&-attr=" + escape(_attr);  // Attribute with the foriegn key
		strURL += "&-fkval=" + escape(inputField.value); // The value to search on
		strURL += "&-fkwhere=" + escape(_fkwhere); // A optional where condition.
		strURL += "&" + new Date().getTime(); // Ensure that the url is always unique so that the browser does not cache any values.
	    return strURL;
	}
	
	//----
	// Constructs a url for the zxfk popup.
	//----
	function contructFKPopupURL() {
		var strURL = "../" + SCRPT + "/zXFK." + SCRPT + "?-pf=zXFK&";
		strURL += "-s=" + _session;
		strURL += "&-e=" + escape(_e);  // Entity with the foriegn
		strURL += "&-attr=" + escape(_attr);  // Attribute with the foriegn key
		strURL += "&-fke=" + _fke;
		strURL += "&-a=framesetSearchForm";
		strURL += "&-ctr=" +  _hiddenid + "&-esize=l";
		
		// strURL += "&-fka=id&-fkta=id";
		
		strURL += "&-fkval=&-fkfilterval=" + escape(inputField.value);
		strURL += "&-fkwhere=" + escape(_fkwhere); // A optional where condition.
		strURL += "&-fklookup=1";
		strURL += "&-ss=" + zXSubSessionId();
		return strURL;
	}
	
	//----
	// Used once we have a search result from the xmlhttp request.
	//----
	function post() {
		// Reset current select item to 0.
		current = 0;
		var options = popup.firstChild.childNodes;
		
		if (searchControl != undefined) {	
			searchControl.src = imgDir + "search_icon.gif";
		}
				
		var doPopup = false;
		if ((options.length > 1) || (options.length == 1 && options[0].innerHTML != inputField.value)) {
			if (options.length == 1 && options[0].innerHTML == inputField.value + " [" + options[0].id + "]") {
					doPopup = false;
			} else {
				doPopup = true;
			}
		}
		
		if(doPopup) {
			setPopupStyles();
			for(var i = 0; i < options.length; i++) {
				options[i].index = i;
				addOptionHandlers(options[i]);
			}
			options[0].className = 'selected';
			
        } else {
			// Auto select
			if (options.length == 1 && options[0].innerHTML == inputField.value) {
				hiddenField.value = options[0].id;
				showObj(display);		
			} else {
				// We have no results.
			}
			
			popup.style.visibility = 'hidden';
        }
    }
    
    //----
    // Handle the user typing.
    //----
    var timeout = false;
	function start(evt) {
		evt = (evt) ? evt : event;
		var keyCode = getKeyCode(evt);
		
		if (timeout) window.clearTimeout(timeout);
		
		// IGNORE SPECIAL CHARS
		//enter or tab for autocomplete
		if((keyCode == 13 || keyCode == 9) ) { //  && popup.style.visibility == 'visible') {
		//up arrow    
		} else if(keyCode == 38) {
		//down arrow	
		} else if(keyCode == 40) {
		// esc - Resets the values
		} else if (keyCode == 27) {
		// IGNORE SPECIAL CHARS
		
		} else {
			//----
			// Show a searching icon.
			//----
			if (searchControl != undefined && searchControl.src.indexOf("searching") == -1) {	
				searchControl.src = imgDir + "searching.gif";
			}
					
			// Only send through to server if the user has stopped typing for longer
			// than 1/2 sec.
			if (_fksearchdelay != 0) {
				timeout = window.setTimeout(updater, _fksearchdelay);
			}
		}
	}
	
	//----
	// Handle special key strokes.
	//----
	function handlespecialkeys(evt) {
		evt = (evt) ? evt : event;
		var keyCode = getKeyCode(evt);
		
		//----
		//enter or tab for autocomplete
		//----
		if((keyCode == 13 || keyCode == 9)) {
			if (popup.style.visibility == 'visible') {
				if (popup.firstChild.childNodes[current].innerHTML == "..." 
				    && popup.firstChild.childNodes[current].id == "...") {
					//----
					// The current selected item is a "..." and therefore we
					// want a popup.
					//----
					FKPopup(contructFKPopupURL());
					popup.style.visibility = 'hidden';
										
				} else {
					inputField.value = popup.firstChild.childNodes[current].innerHTML;
					hiddenField.value = popup.firstChild.childNodes[current].id; // Actual submitted value
					showObj(display);
				    
					popup.style.visibility = 'hidden';
					inputField.focus();
				}
				
				//----
				// Stop the form submit.
				//----
				if(isIE) {
					event.returnValue = false;
				} else {
					evt.preventDefault();
				}
							    
			//-----    
			// Only active when :
			// We have press submit and we have not already selected a value and the input field is not empty.    
			//-----
			} else if (keyCode == 13 && hiddenField.value == "-" && inputField.value != "") {
				// Do a instant resolve
					
				// Clear previously cached values
				popup.innerHTML = "";
				
				// Start the look up.
				updater();
				
				// Stop the browser submit.
				if(isIE) {
					event.returnValue = false;
				} else {
					evt.preventDefault();
				}
				
			} else {
				if (keyCode == 13) {
					removeListener(searchControl, "click", doSearchClick);
				}
			}
		//----    
		//up arrow
		//----
		} else if(keyCode == 38) {
			if(current > 0) {
				popup.firstChild.childNodes[current].className = '';
				current--;
				
				popup.firstChild.childNodes[current].className = 'selected';
				popup.firstChild.childNodes[current].scrollIntoView(true);
			}
			
		//----
		//down arrow
		//----
		} else if(keyCode == 40) {
			if(current < popup.firstChild.childNodes.length - 1) {
				popup.firstChild.childNodes[current].className = '';
				current++;
				
				popup.firstChild.childNodes[current].className = 'selected';
				popup.firstChild.childNodes[current].scrollIntoView(false);
			}
		
		//----
		// esc - Resets the values
		//----
		} else if (keyCode == 27) {
			// Reset all fields
			inputField.value = "";
			hiddenField.value = "-";
			popup.style.visibility = 'hidden';
			inputField.focus();
			hideObj(display);
		
		//----    
		// any other key press
		//----
		} else {
			// Field is not dirty.
			hiddenField.value = "-";
			popup.style.visibility = 'hidden';
			hideObj(display);
		    
		}
	}
	
	//----
	// Popup javascripts
	//----
	function handleClick(evt) {
		evt = (evt) ? evt : event;
		
		if (eventElement(evt).innerHTML == "..." && eventElement(evt).id == "...") {
			FKPopup(contructFKPopupURL());
			
		} else {
			inputField.value = eventElement(evt).innerHTML;
			hiddenField.value = eventElement(evt).id;
			popup.style.visibility = 'hidden';
			inputField.focus();
			showObj(display);
			
		}
	}
	
	//----
	// Handle a mouse over of the drop down.
	//----
	function handleOver(evt) {
		evt = (evt) ? evt : event;
		
		popup.firstChild.childNodes[current].className = '';
		current = eventElement(evt).index;
		popup.firstChild.childNodes[current].className = 'selected';
	}
	
	//----
	// Add the javascripts to the popup
	//----
	function addOptionHandlers(option) {
		if(isMoz) {
			option.addEventListener("mouseover", handleOver, false);
			option.addEventListener("click", handleClick, false);
		} else {
			option.attachEvent("onmouseover", handleOver, false);
			option.attachEvent("onclick", handleClick, false);
		}
	}
	
	//----
	// Setups the style of a visible popup.
	//----	
	function setPopupStyles() {
		var maxHeight
		
		if(isIE) {
			maxHeight = 200;
		} else {
			maxHeight = window.outerHeight/3;
		}
		
		if(popup.offsetHeight < maxHeight) {
			popup.style.overflow = 'hidden';
		} else if(isMoz) {
			popup.style.maxHeight = maxHeight + 'px';
			popup.style.overflow = '-moz-scrollbars-vertical';
		} else {
			popup.style.height = maxHeight + 'px';
			popup.style.overflowY = 'auto';
		}
		
		popup.scrollTop = 0;
		popup.style.visibility = 'visible';
	}
	
	//----
	// liveUpdater
	//
	//uriFunc = Function to get the url to post to.
	//postFunc = Function to call once we have processed the xmlhttp request.
	//----
	function liveUpdater(uriFunc, postFunc) {
		var request = new XMLHttpRequest();
		
		function update() {
			/** Abort any unprocessed requests **/
			if(request && request.readyState < 4) {
				request.abort();
			}
			
			request = new XMLHttpRequest();
			
			request.onreadystatechange = function processRequestChange() {
				if(request.readyState == 4) {
					if (request.status == 200) {
						if (request.responseText) {
							popup.innerHTML = request.responseText;
							// Call the post xmlhttp function
							postFunc();
						}
					}
				}
			}
		    
			/** Open a async request **/
			request.open("GET", uriFunc());
	        
			if (window.ActiveXObject) { 
				request.send();
			} else {
				request.send(null);
			}
			
			return false;
		}
		
		// Call the update function
		return update;
	}
	
}

//----
// Called from the zxfk popup.
//----
function zxfkloopup_populate(_window, _controlName, _value, _label) {
	// The display input field
	var inputField = findObj("_id_" + _controlName, _window);
	// The hidden input field, this value is used in the submit.
	var hiddenField = findObj(_controlName, _window);
	// The display icon
	var display = findObj("_display_" + _controlName, _window);
	
	inputField.value = _label;
	inputField.focus();
	
	hiddenField.value = _value;
	showObj(display);
}

//=============================
// Functions to handle browser incompatibilites
//=============================
function eventElement(evt) {
	if(evt.currentTarget) {
		return evt.currentTarget;
	} else {
		return (evt.target) ? evt.target : evt.srcElement;
	}
}

function addClickListener(element, listener) {
	if (isMoz) {
		element.addEventListener('click', listener, true);
	} else {
		element.attachEvent('onclick', listener);
	}
}

function addKeyListener(element, listener) {
	if (isSafari) {
		element.addEventListener("keydown", listener, false);
	} else if (isMoz) {
		element.addEventListener("keypress", listener, false);
	} else {
		element.attachEvent("onkeydown", listener);
	}
}

function addBlurListener(element, listener) {
	if(isIE) {
		element.attachEvent("onblur", listener);
	} else {
		element.addEventListener("blur", listener, false);
	}
}

function addListener(element, type, listener){
	if(element.addEventListener) {
		element.addEventListener(type, listener, false);
	} else {
		element.attachEvent('on' + type, listener);
	}
}

function removeListener(element, type, listener) {
	if(element.removeEventListener) {
		element.removeEventListener(type, listener, false);
	} else {
		element.detachEvent('on' + type, listener);
	}
}

function hideObj(element) {
	element.style.visibility='hidden';
}

function showObj(element) {
	element.style.visibility='visible';
}