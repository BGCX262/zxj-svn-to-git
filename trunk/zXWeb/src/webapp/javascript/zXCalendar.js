function fToggleColor(myElement) 
{
	var toggleColor = "#ff0000";

	if (myElement.id == "calDateText") 
	{
		if (myElement.color == toggleColor) 
		{
			myElement.color = "";
		} 
		else 
		{
			myElement.color = toggleColor;
   		}
	} 
	else 
	{
		if (myElement.id == "calCell") 
		{
			for (var i in myElement.children) 
			{
				if (myElement.children[i].id == "calDateText") 
				{
					if (myElement.children[i].color == toggleColor) 
					{
						myElement.children[i].color = "";
					} 
					else 
					{
						myElement.children[i].color = toggleColor;
            		}
         		}
      		}
      	}
   	}
}

function fSetSelectedDay(_ctrName, myElement)
{
	if (myElement.id == "calCell") 
	{
   		if (!isNaN(parseInt(myElement.children["calDateText"].innerText))) 
		{
      			myElement.bgColor = "#c0c0c0";
				objPrevElement.bgColor = "";

				document.all.calSelectedDate.value = parseInt(myElement.children["calDateText"].innerText);

				var cookieValue = document.all.calSelectedDate.value + 
				                  monthNumberToName(frmCalendar.tbSelMonth.value) + 
				                  frmCalendar.tbSelYear.value;


				fCalendarSetValue(_ctrName, cookieValue);
 
				objPrevElement = myElement;

				window.close();
		}
	}
}

function fCalendarSetValue(_ctrName, _Value)
{
	var ctr;

	ctr = elementByName(top.window.opener, _ctrName);

	if (ctr != null) 
	{
		ctr.value = _Value;
		ctr.onchange();
	}
}

function elementByName(_window, _elmnt)
{
	for(var f = 0; f < _window.document.forms.length; ++f)
	{
		for(var e = 0; e < _window.document.forms(f).elements.length; ++e)
		{
			if(_window.document.forms(f).elements(e).name == _elmnt)
			{
				return (_window.document.forms(f).elements(e));
			}
		}
	}
}

function fGetDaysInMonth(iMonth, iYear) 
{
	var dPrevDate = new Date(iYear, iMonth, 0);

	return dPrevDate.getDate();
}

function fBuildCal(iYear, iMonth, iDayStyle) 
{
	var aMonth = new Array();

	aMonth[0] = new Array(7);
	aMonth[1] = new Array(7);
	aMonth[2] = new Array(7);
	aMonth[3] = new Array(7);
	aMonth[4] = new Array(7);
	aMonth[5] = new Array(7);
	aMonth[6] = new Array(7);

	var dCalDate = new Date(iYear, iMonth-1, 1);
	var iDayOfFirst = dCalDate.getDay();
	var iDaysInMonth = fGetDaysInMonth(iMonth, iYear);
	var iVarDate = 1;
	var i, d, w;

	if (iDayStyle == 2) 
	{
		aMonth[0][0] = "Sunday";
		aMonth[0][1] = "Monday";
		aMonth[0][2] = "Tuesday";
		aMonth[0][3] = "Wednesday";
		aMonth[0][4] = "Thursday";
		aMonth[0][5] = "Friday";
		aMonth[0][6] = "Saturday";
	} 
	else 
	{
		if (iDayStyle == 1) 
		{
			aMonth[0][0] = "Sun";
			aMonth[0][1] = "Mon";
			aMonth[0][2] = "Tue";
			aMonth[0][3] = "Wed";
			aMonth[0][4] = "Thu";
			aMonth[0][5] = "Fri";
			aMonth[0][6] = "Sat";
		} 
		else 
		{
			aMonth[0][0] = "Su";
			aMonth[0][1] = "Mo";
			aMonth[0][2] = "Tu";
			aMonth[0][3] = "We";
			aMonth[0][4] = "Th";
			aMonth[0][5] = "Fr";
			aMonth[0][6] = "Sa";
		}
	}
	
	for (d = iDayOfFirst; d < 7; d++) 
	{
		aMonth[1][d] = iVarDate;
		iVarDate++;
	}

	for (w = 2; w < 7; w++) 
	{
		for (d = 0; d < 7; d++) 
		{
			if (iVarDate <= iDaysInMonth) 
			{
				aMonth[w][d] = iVarDate;
				iVarDate++;
			}
	   }
	}

	return aMonth;
}

function fDrawCal(_ctrName, iYear, iMonth, iCellWidth, iCellHeight, sDateTextSize, sDateTextWeight, iDayStyle) 
{
	var myMonth;

	myMonth = fBuildCal(iYear, iMonth, iDayStyle);

	document.write("<table border='1'>");
	document.write("<tr>");
	document.write("<td class=zxTitle align='center' >" + myMonth[0][0] + "</td>");
	document.write("<td class=zxTitle align='center' >" + myMonth[0][1] + "</td>");
	document.write("<td class=zxTitle align='center' >" + myMonth[0][2] + "</td>");
	document.write("<td class=zxTitle align='center' >" + myMonth[0][3] + "</td>");
	document.write("<td class=zxTitle align='center' >" + myMonth[0][4] + "</td>");
	document.write("<td class=zxTitle align='center' >" + myMonth[0][5] + "</td>");
	document.write("<td class=zxTitle align='center' >" + myMonth[0][6] + "</td>");

	var sw = 1;
	var clss="zxNor";

	for (w = 1; w < 7; w++) 
	{
		document.write("<tr>");

		for (d = 0; d < 7; d++) 
		{
			if (sw != w) 
			{
				if (clss == 'zxNor') 
				{
				    clss = 'zxAlt'; 
				} 
				else 
				{
				    clss = 'zxNor';
				}
				sw = w;
			}

			document.write("<td class="+clss+" align='left' valign='top' width='" + iCellWidth + "' height='" + iCellHeight + "' id=calCell style='CURSOR:Hand' onMouseOver='fToggleColor(this)' onMouseOut='fToggleColor(this)' onclick='fSetSelectedDay(\"" + _ctrName + "\", this);'>");

			if (!isNaN(myMonth[w][d])) 
			{
				document.write("<font id=calDateText onMouseOver='fToggleColor(this)' style='CURSOR:Hand;FONT-FAMILY:Arial;FONT-SIZE:" + sDateTextSize + ";FONT-WEIGHT:" + sDateTextWeight + "' onMouseOut='fToggleColor(this)' onclick='fSetSelectedDay(\"" + _ctrName + "\", this);'>" + myMonth[w][d] + "</font>");
			} 
			else 
			{
				document.write("<font id=calDateText onMouseOver='fToggleColor(this)' style='CURSOR:Hand;FONT-FAMILY:Arial;FONT-SIZE:" + sDateTextSize + ";FONT-WEIGHT:" + sDateTextWeight + "' onMouseOut='fToggleColor(this)' onclick='fSetSelectedDay(\"" + _ctrName + "\", this);'> </font>");
			}

			document.write("</td>");
		}
		
		document.write("</tr>");
	}

	document.write("</table>");
}


function fUpdateCal(iYear, iMonth) 
{
	myMonth = fBuildCal(iYear, iMonth);

	objPrevElement.bgColor = "";
	document.all.calSelectedDate.value = "";

	for (w = 1; w < 7; w++) 
	{
		for (d = 0; d < 7; d++) 
		{
			if (!isNaN(myMonth[w][d])) 
			{
				calDateText[((7*w)+d)-7].innerText = myMonth[w][d];
			} 
			else 
			{
				calDateText[((7*w)+d)-7].innerText = " ";
	        }
		}
   }
}
