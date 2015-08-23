//----
// What		:	zXList.js
// Who		:	Bertus Dispa
// When		:	10SEP02
// Why		:	zX list functions
//
// Change	:	BD4JUN03
// Why		:	Added zXTickAll / zXUntickAll methods
//
// Change	:	MB01NOV04
// Why		:	Allow for checkAll and uncheck all when having multiple list forms on same page.
//
//
//----

// User has clicked on resort button for a list
function zXListReSort(_url)
{
	document.body.style.cursor="wait";
	zXRef(_url);
	return false;
}

//----
// Function	:	zXTickAll
//
// Select all checkboxes with given entity name
// _actionName -  The name of the root element the checkbox belongs to.
//----
function zXTickAll(_name, _actionName)
{
	var i;
	var col;
	
	if (_actionName != null) {
		var element = findObj(_actionName, window, document);
		col = element.getElementsByTagName("input");
	} else {
		if (isIE4) {
			col = document.all.tags("input");
		} else {
			col = document.getElementsByTagName("input");
		}
	}
	
	if (col != null) 
	{
	    for (i = 0; i < col.length; i++) 
		{
			if (col[i].name.toLowerCase().match("ctrmultilist") )
			{
				if (col[i].name.toLowerCase().match(_name.toLowerCase()) )
				{
					col[i].checked = true;
				}
			}
		}
	}
}

//----
// Function	:	zXUntickAll
//
// Deselect all checkboxes with given entity name
// _actionName -  The name of the root element the checkbox belongs to.
//----
function zXUntickAll(_name, _actionName)
{
	var i;
	var col;
	
	if (_actionName != null) {
		var element = findObj(_actionName, window, document);
		col = element.getElementsByTagName("input");
	} else {
		if (isIE4) {
			col = document.all.tags("input");
		} else {
			col = document.getElementsByTagName("input");
		}
	}
	
	if (col != null) 
	{
	    for (i = 0; i < col.length; i++) 
		{
			if (col[i].name.toLowerCase().match("ctrmultilist") )
			{
				if (col[i].name.toLowerCase().match(_name.toLowerCase()) )
				{
					col[i].checked = false;
				}
			}
		}
	}
}