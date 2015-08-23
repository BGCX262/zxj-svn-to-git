<%
//----
// Pass on the selected language.
//----
String strLocale = request.getParameter("locale");
if (strLocale == null) {
	strLocale = "";
}
%>
<script type="text/javascript" language="JavaScript">
var wordWindow = null;
var controlWindow = null;

function init_spell( spellerWindow ) {

	if( spellerWindow ) {
		if( spellerWindow.windowType == "wordWindow" ) {
			wordWindow = spellerWindow;
		} else if ( spellerWindow.windowType == "controlWindow" ) {
			controlWindow = spellerWindow;
		}
	}
	
	if( controlWindow && wordWindow ) {
		// populate the speller object and start it off!
		var speller = opener.speller;
		wordWindow.speller = speller;
		speller.startCheck( wordWindow, controlWindow );
	}
}

// encodeForPost
function encodeForPost( str ) {
	var s = new String( str );
	s = encodeURIComponent( s );
	// additionally encode single quotes to evade any PHP 
	// magic_quotes_gpc setting (it inserts escape characters and 
	// therefore skews the btye positions of misspelled words)
	return s.replace( /\'/g, '%27' );
}

// post the text area data to the script that populates the speller
function postWords() {
	var bodyDoc = window.frames[0].document;
	bodyDoc.open();
	bodyDoc.write('<html>');
	bodyDoc.write('<meta http-equiv="Content-Type" content="text/html; charset=utf-8">');
	bodyDoc.write('<link rel="stylesheet" type="text/css" href="spellerStyle.css"/>');
	if (opener) {
		var speller = opener.speller;
		bodyDoc.write('<body class="normalText" onLoad="document.forms[0].submit();">');
		bodyDoc.write('<p>Spell check in progress...</p>');
		bodyDoc.write('<form action="'+speller.spellCheckScript+'" method="post">');
		for( var i = 0; i < speller.textInputs.length; i++ ) {
			bodyDoc.write('<input type="hidden" name="textinputs[]" value="'+encodeForPost(speller.textInputs[i].value)+'">');
		}
		
		bodyDoc.write('<input type="hidden" name="locale" value="<%=strLocale%>">');
		bodyDoc.write('</form>');
		bodyDoc.write('</body>');
	} else {
		bodyDoc.write('<body class="normalText">');
		bodyDoc.write('<p><b>This page cannot be displayed</b></p><p>The window was not opened from another window.</p>');
		bodyDoc.write('</body>');
	}
	bodyDoc.write('</html>');
	bodyDoc.close();
}
</script>

<html>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<head>
<title>Speller Pages</title>
</head>
<frameset rows="*,215" onLoad="postWords();" name="spellchecker">
<frame src="../html/blank.html">
<frame src="controls.jsp?locale=<%=strLocale%>">
</frameset>
</html>