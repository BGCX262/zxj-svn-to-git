<%@ page import="org.zxframework.web.util.*" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">
<link rel="stylesheet" type="text/css" href="../includes/spellerStyle.css" />

<script language="javascript" src="../javascript/wordWindow.js"></script>
<script language="javascript">
var suggs = new Array();
var words = new Array();
var textinputs = new Array();
var error;

<%
	try {
		//----
		// Find the selected locale.
		// NOTE : A user can select a alternative locale 
		//		  therefore we read this selection from the request.
		//----
		String strLocale = request.getParameter("locale");
		if (strLocale == null || strLocale.length() == 0) {
			//----
			// Get locale from cookie
			//----
			strLocale = org.zxframework.web.util.ServletUtil.getCookieValue(request, "-locale");
			if (strLocale == null) {
				strLocale = java.util.Locale.getDefault().toString();
			}
		}
		
		String strCheckerHeader = "";
		String strCheckerResults = "";
		try {
			WebSpellChecker objSpellChecker = new OpenofficeWebSpellChecker(request.getParameterValues("textinputs[]"), strLocale);
			strCheckerHeader = objSpellChecker.getCheckerHeader();
			strCheckerResults = objSpellChecker.getCheckerResults();
		} catch (Exception e) {
			//----
			// The user has not installed openoffice or the SDK is not installed properly.
			// We should use DI to make this configurable.
			//----
			WebSpellChecker objSpellChecker = new JazzyWebSpellChecker(request.getParameterValues("textinputs[]"));
			strCheckerHeader = objSpellChecker.getCheckerHeader();
			strCheckerResults = objSpellChecker.getCheckerResults();		
		}
		
		// Write out the text to check :
		out.write(strCheckerHeader);
		out.flush();
		
		// Writes out the results :
		out.write(strCheckerResults);
		out.flush();
		
	} catch (Exception e) {
	}
%>

var wordWindowObj = new wordWindow();
wordWindowObj.originalSpellings = words;
wordWindowObj.suggestions = suggs;
wordWindowObj.textInputs = textinputs;

function init_spell() {
	// check if any error occured during server-side processing
	if( error ) {
		alert( error );
	} else {
		// call the init_spell() function in the parent frameset
		if (parent.frames.length) {
			parent.init_spell(wordWindowObj);
		} else {
			alert('This page was loaded outside of a frameset. It might not display properly');
		}
	}
}
</script>
</head>

<body onLoad="init_spell();">

<script type="text/javascript">
wordWindowObj.writeBody();
</script>

</body>
</html>