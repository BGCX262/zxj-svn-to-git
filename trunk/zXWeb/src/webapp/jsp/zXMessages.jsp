<%@page contentType="text/xml"
%><%@page language="java" import="org.zxframework.*"
%><%@page language="java" import="org.zxframework.misc.*"
%><%@ taglib uri="/WEB-INF/zX.tld" prefix="zx" 
%><zx:zx jsheader="false"/><% 
ZX objZX = null;
try {
	objZX = ThreadLocalZX.getZX();
	Text objTxt = (Text)objZX.createBO("zXTxt");
	if (objTxt == null) throw new Exception("Unable to create instance of zXTxt");
	int intTxt = objTxt.countUnread();
	if (intTxt < 0) throw new Exception("Unable to count unread texts");
%><?xml version="1.0" encoding="ISO-8859-1"?>
<m>
	<unread><%=intTxt%></unread>
</m>
<%
} catch (Exception e) {
	try {
		if (objZX != null){
			out.write(objZX.trace.formatStack(true));
		} else {
			out.write(e.getMessage());
		}
	} catch (Exception e1) {}
} finally {
	if (objZX != null) objZX.cleanup();
}
%>