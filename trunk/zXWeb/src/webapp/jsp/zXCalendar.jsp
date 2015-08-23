<%
/**
 * File		: zXCalendar.asp
 * Who		: Bertus Dispa
 * Why		: Code for popup calendar
 * When		: 5SEP02
 *
 * Assumes that name of control to store selected date in has been 
 * passed as -da (in opener window) and the current date value as -cdv
 *
 * -cdv - The Current date value
 * -da - The Date attribute.
 **/
%>
<%@ page import="java.util.Calendar" %>
<%
String strDateAttr = request.getParameter("-da");
String strCurrentDateValue = request.getParameter("-cdv");
%>
<HTML>
<HEAD>
<title>Datepicker</title>

<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">

<script type="text/javascript" language="JavaScript" src="../javascript/zX.js"></script>
<script type="text/javascript" language="JavaScript" src="../javascript/zXForm.js"></script>
<script type="text/javascript" language="JavaScript" src="../javascript/zXCalendar.js"></script>

<script type="text/javascript" language="JavaScript">
<!-- Begin
<%
	try { out.write("	var strCurrentDateValue = '" + strCurrentDateValue + "';"); } catch (Exception e) {}
%>
var dDate = new Date();
var objPrevElement = new Object();

DetermineDate();

function DetermineDate()
{
	var arrDate;

	if (strCurrentDateValue != "") 
	{
		if (isNaN(strCurrentDateValue.substr(0, 2)))
		{
	 		dDate.setDate(strCurrentDateValue.substr(0, 1));
			dDate.setMonth( monthNameToNumber(strCurrentDateValue.substr(1, 3)) - 1);

			if (Number(strCurrentDateValue.substr(4)) < 100) 
			{
				dDate.setYear(strCurrentDateValue.substr(4)) + 2000;
			} 
			else 
			{
				dDate.setYear(strCurrentDateValue.substr(4));
			}
		}
		else
		{
	 		dDate.setDate(strCurrentDateValue.substr(0, 2));
			dDate.setMonth( monthNameToNumber(strCurrentDateValue.substr(2, 3)) - 1);

			if (Number(strCurrentDateValue.substr(5)) < 100) 
			{
				dDate.setYear(strCurrentDateValue.substr(5)) + 2000;
			} 
			else 
			{
				dDate.setYear(strCurrentDateValue.substr(5));
			}
		}	

	} 

	if(isNaN(dDate))
	{
		dDate = new Date();
	}

	strCurrentDateValue = dDate.getDate() +	
								monthNumberToName(dDate.getMonth() + 1) +
								dDate.getFullYear();


}
// End -->
</SCRIPT>
</HEAD>
</HEAD>
<BODY>
<FORM name="frmCalendar" method="post" action="">
	<INPUT type="hidden" name="calSelectedDate" value="">
	<TABLE border="1">
		<TR>
			<TD>
				<SELECT name="tbSelMonth" onChange='fUpdateCal(frmCalendar.tbSelYear.value, frmCalendar.tbSelMonth.value)'>
					<OPTION value="1">January</OPTION>
					<OPTION value="2">February</OPTION>
					<OPTION value="3">March</OPTION>
					<OPTION value="4">April</OPTION>
					<OPTION value="5">May</OPTION>
					<OPTION value="6">June</OPTION>
					<OPTION value="7">July</OPTION>
					<OPTION value="8">August</OPTION>
					<OPTION value="9">September</OPTION>
					<OPTION value="10">October</OPTION>
					<OPTION value="11">November</OPTION>
					<OPTION value="12">December</OPTION>
				</SELECT>
				<SELECT name="tbSelYear" onChange='fUpdateCal(frmCalendar.tbSelYear.value, frmCalendar.tbSelMonth.value)'>
				
<%
	//Set up the dates
	int intYearToStart = Calendar.getInstance().get(Calendar.YEAR) - 50;
	int intYearToEnd = intYearToStart + 100;
	
	StringBuffer strYearOptions = new StringBuffer(3500);
	for (;intYearToEnd > intYearToStart; intYearToStart++) {
        strYearOptions.append("	<OPTION value=\"").append(intYearToStart).append("\">").append(intYearToStart).append("</OPTION>\n");
    }
    
	try {
		out.write(strYearOptions.toString());
	} catch (Exception e) { }
%>
				
				</SELECT>
			</TD>
		</TR>
		<TR>
			<TD>
			
<script type='text/javascript' language='JavaScript'>
<% 
	try {
		out.write("fDrawCal('" + strDateAttr + "', dDate.getFullYear(), dDate.getMonth()+1, 30, 30, '12px', 'bold', 1);"); 
	} catch (Exception e) {}
%>

	
	// frmCalendar.tbSelMonth.options[dDate.getMonth()].selected = true;
	findObj("tbSelMonth", window).options[dDate.getMonth()].selected = true;
	tbSelYear = findObj("tbSelYear", window);
	
	for (i = 0; i < tbSelYear.length; i++)
	{
		if (tbSelYear.options[i].value == dDate.getFullYear())
		{
			tbSelYear.options[i].selected = true;
		}
	}
	
</script>
			</TD>
		</TR>
	</TABLE>
</FORM>
</BODY>
</HTML>