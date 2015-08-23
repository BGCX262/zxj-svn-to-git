<html>
<head>

<link rel="stylesheet" type="text/css" href="../includes/spellerStyle.css"/>
<script type="text/javascript" language="JavaScript" src="../javascript/controlWindow.js"></script>

<script type="text/javascript" language="JavaScript">
var spellerObject;
var controlWindowObj;

if( parent.opener ) {
	spellerObject = parent.opener.speller;
}

function ignore_word() {
	if( spellerObject ) {
		spellerObject.ignoreWord();
	}
}

function ignore_all() {
	if( spellerObject ) {
		spellerObject.ignoreAll();
	}
}

function replace_word() {
	if( spellerObject ) {
		spellerObject.replaceWord();
	}
}

function replace_all() {
	if( spellerObject ) {
		spellerObject.replaceAll();
	}
}

function end_spell() {
	if( spellerObject ) {
		spellerObject.terminateSpell();
	}
}

function undo() {
	if( spellerObject ) {
		spellerObject.undo();
	}
}

function suggText() {
	if( controlWindowObj ) {
		controlWindowObj.setSuggestedText();
	}
}

function init_spell() {
	var controlForm = document.spellcheck;

	// create a new controlWindow object
 	controlWindowObj = new controlWindow( controlForm );

	// call the init_spell() function in the parent frameset
	if( parent.frames.length ) {
		parent.init_spell( controlWindowObj );
	} else {
		alert( 'This page was loaded outside of a frameset. It might not display properly' );
	}
}

</script>

</head>

<body class="controlWindowBody" onLoad="init_spell();">

<form action="spellchecker.jsp" method="get" target="spellchecker" name="spellcheck">

<table border="0" cellpadding="0" cellspacing="0">
<tr>
	<td colspan="3" class="normalLabel">Not in dictionary:</td>
</tr>
<tr>
	<td colspan="3"><input class="readonlyInput" type="text" name="misword" readonly /></td>
</tr>
<tr>
	<td colspan="3" height="5"></td>
</tr>
<tr>
	<td class="normalLabel">Change to:</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
</tr>

<tr valign="top">
	<td>
		<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td class="normalLabel">
			<input class="textDefault" type="text" name="txtsugg"/>
			</td>
		</tr>
		<tr>
			<td>
			<select class="suggSlct" name="sugg" size="7" onChange="suggText();" onDblClick="replace_word();">
				<option></option>
			</select>
			</td>
		</tr>
		</table>
	</td>
	<td>&nbsp;&nbsp;</td>
	<td valign="top">
		<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td>
			<input class="buttonDefault" type="button" value="Ignore" onClick="ignore_word();">
			</td>
			<td>&nbsp;&nbsp;</td>
			<td>
			<input class="buttonDefault" type="button" value="Ignore All" onClick="ignore_all();">
			</td>
		</tr>
		<tr>
			<td colspan="3" height="5"></td>
		</tr>
		<tr>

			<td>
			<input class="buttonDefault" type="button" value="Replace" onClick="replace_word();">
			</td>
			<td>&nbsp;&nbsp;</td>
			<td>
			<input class="buttonDefault" type="button" value="Replace All" onClick="replace_all();">
			</td>
		</tr>
		<tr>
			<td colspan="3" height="5"></td>
		</tr>
		<tr>
			<td>
			<input class="buttonDefault" type="button" name="btnUndo" value="Undo" onClick="undo();" disabled>
			</td>
			<td>&nbsp;&nbsp;</td>
			<td>
			<input class="buttonDefault" type="button" value="Close" onClick="end_spell();">
			</td>
		</tr>
		</table>
		
	<%
	try {
		//----
		// Find the selected language.
		//----
		String strLocale = request.getParameter("locale");
		if (strLocale == null || strLocale.length() == 0) {
			// Get locale from cookie
			strLocale = org.zxframework.web.util.ServletUtil.getCookieValue(request, "-locale");
			if (strLocale == null) {
				strLocale = java.util.Locale.getDefault().toString();
			}
		}

		//----
		// Only show any languages if we can support them.
		//----
		org.zxframework.web.util.WebSpellChecker objSpell = new org.zxframework.web.util.OpenofficeWebSpellChecker();
		String strLanguages = objSpell.listSupportedLanguages(strLocale);
		if (strLanguages != null && strLanguages.length() > 0) { 
			%>
				<div class="normalLabel">
				Language : <br/>
				<select onchange="document.forms[0].submit();" name="locale">
					<%=strLanguages%>
				</select>
				</div>
			<% 
		}
		
	} catch (Exception e) {
	}
	%>
	</td>
</tr>
</table>
</form>
</body>
</html>