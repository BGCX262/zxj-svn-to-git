<%
//----
// File	:	zXDoc.asp
// By	:	Bertus Dispa
// Date	:	20AUG03
//
// Standard document routines
//
// Change	: BD11MAR04
// Why		: See comment marked with BD11MAR04
//
// Change	: BD16MAY04
// Why		: Fixed problem with docView; allow download now
//
// Change	: BD28MAY04
// Why		: Added -noDownLoad option to adhoc view
//
// Change	: DGS07SEP2004
// Why		: Domarque 427JF: Not the original problem (was in prQte timing out), but once that
//			  one was fixed, the same thing arose in this, so allow longer timeout
//
// Change	: BD4NOV04
// Why		: Fixed bug with ad-hoc printing (must not be background otherwise Word is gone
//				before we have the ability to print!)
//
// TO DO : need to do this, requires openoffice.
// Change	: BD18NOV04
// Why		: Added option to print multiple copies
//
//Change	: DGS13DEC2004
// Why		: Added option to not use viewer in docView
 //
// Change	: V1.4:21 BD27DEC04
// Why		: For ad-hoc document build where only a print is requested (ie no view), close the
 //			  popup window before generating and printing document; this allows the user to do
//			  more important things than waiting for a document to be made and send to the printer
//
// Change	: DGS05JAN2005
// Why		: Added option to upload without an FK entity
//
// Change	: BD20APR056 - V1.5:4 / V1.5:5 
// Why		: Added support for data-sources and use transaction management for this
// 
// Change	: BD20APR056 - V1.5:18 
// Why		: Fixed problem in asp.docprint.go; there be no numberOfPrints provided in which
//				case we have to default to 1
//
// ============
// AD-HOC BUILD
// ============
//
// -a = adHocBuild (or asp.adHocBuild)
//		Build a document using the document builder and show and / or print and 
//		optionally associate with an entity
//
// -template = name of document template to use
// -titleAction = optional action that is used for title only; if left blank
//					standard title is being used; in pageflow:action format
// -pk = pk for the document subject (passed to document builder as pk)
// -view = If 1 default to view-checkbox checked otherwise unchecked
// -printer = name of user preference to use for printer (no printer selected if blank)
// -backAction = if present will result in back button rather than close button; 
//						if not present assumes that action is loaded into popup window
// -nextAction = if present will go to this action when done, otherwise will
//						simply close popup window
// -persist = specifies the type of persistence; one of the following values
//				'' (blank): true adhoc build; do not save document at all
//				'one': the entity (-e) will have a foreign key to zXDoc; -pk is the pk
//						of the main entity and the instance exists; it will ALWAYS create
//						a new instance of zXDoc and associate it with the entity, even
//						if this as already associated with one 
//				'reuse': very similar to 'one' but now if there is already a zXDoc (i.e.
//						the -fk.doc attribute is not null) we will re-use the zXDoc
//				'many': the entity (-e) will be a linking entity between another entity
//						and zXDoc; a new instance of this entity will be created
// -e = Either the main entity (-persist=one), the linking entity (-persist-many) or not relevant 
// -fk.attr = (only relevant for persists=many); the FK from the linking entity (-e) to
//						the main entity that -pk applies to
// -fk.doc = (relevant for -persist=one and -persist=many) the FK in the entity (-e) that points to
//						the document; if not available we assume that there is only one
//						FK to zXDoc in -e and we figure it out ourselves
// -fk.pk = (only relevant for persists=many); optional anchor PK to link many-to-many
//						entity to; if not passed we assume that -fk.attr should be set to
//						-pk
// -docType = (relevant for -persist=one and -persist=many) The document type for zXDoc
//
// ============
// DOCUMENT VIEW
// ============
//
// -a = docView (or asp.docView)
//			Show the document associated with zXDoc instance
// -pk.zXDoc = pk of zXDoc instance
// -nextAction = optional, next action to go to
// -noDownLoad = optional, 1 means no download
// -noViewer = optional, 1 means do not use browser's viewer, convert instead (see clsDoc.view)
//
// ============
// DOCUMENT PRINT
// ============
//
// -a = docPrint or asp.docprint
//			Print a single document; prompt user to select printer and print a document
//			on this printer. The printer is saved as a user preference. The	document
//			is indicated by a zXDoc instance and is assumed to be a Word document
// -pk.zXdoc = PK of zXDoc pointing to the requested document
// -titleAction = optional action (in pageflow:action format) that contains the
//			title for the print 
// -prefPrinter = name of the user preference that holds the printer name; the
//			selected printer will be stored in this user preference
// -backAction = optional action to go back to (in pageflow:action format); assumed in
//			popup otherwise
// -nextAction = optional action where to go when done(in pageflow:action format); assumed in
//			popup otherwise
//		
// ============
// DOCUMENT LIST
// ============
//
// -a = docList (or asp.docList)
//			Display a list of associated documents associated and allow user to maintain
//			this list
// -entityAction = action (in pageflow:action format) where the entities are defined. This
//			has to have at least 2 entities but can have more.
//			One entity HAS to be named 'entity' with a pk of '#qs.-zXDoc.pk.entity' and
//				this is the entity that has a FK to zXDoc
//			One entity HAS to be zXDoc, named 'zXDoc' with a pk of '#qs.-zXDoc.pk.zXDoc' 
// OR (alternative to -entityAction):
// -entity = name of the entity that is the many2many and has a FK to zXDoc and an FK
//				to the pin entity (see -fk.attr and -fk.value)
// -fk.attr = The attribute in -entity (or 'entity' when using -entityAction) that
//				is the FK to the pin entity
// -fk.value = The value to restrict by
// -fk.doc = foreign key of entity -e to zXDoc; if left blank we try to figure out ourselves
// -allowDelete = if 1 allow user to delete document; not allowed otherwise; only
//			relevant when -allowEdit=1		
// -allowEdit = if 1 allow user to edit the details of the entity; not allowed otherwise
// -allowView = if 1 allow user to view document; not allowed otherwise
 // -allowUpload = if 1 allow user to upload documents; not allowed otherwise 
// -editAfterUpload = if 1 the user will be allowe to edit directly after an upload
// -close = if 1 have close button (and thus assume in popup window); otherwise not
// -docType = document type to use when uploading new documents
// -backAction = action to go back to (in pageflow:action format)
// -titleAction.list = optional action (in pageflow:action format) that specifies title for list
//				no title if not specified
// -titleAction.edit = optional action (in pageflow:action format) that specifies title for
//				edit page; standard title if not present
// -titleAction.upload = optional action (in pageflow:action format) that specifies title for
//				upload page; standard title if not present
//
//
// ============
// FK DOCUMENT UPLOAD
// ============
// -a = asp.fkUpload or fkUpload
//			Allow the user to upload a document and set the zXDoc FK attribute of 
//			an entity
// -e = name of entity that has FK to zXDoc
// -fk.value = the pk of the entity specified in -e
// -fk.doc = (optional) the name of the attribute (from the -e entity) that points to zXDoc;
//			if left blank we try to figure out ourselves
// -docType = Document type that weuse to create new instance of zXDoc
// -noKeyWords = (optional) if  1, the user cannot specify any keywords
// -nextAction = (optional) in pageflow:action format; where to go when done
//			if left blank we assume we are in a popup window and close the window
// -backAction = (optional) in pageflow:action format; action associated with back button
//			if left blank we assume we are in a popup window and close the window
// -titleAction = (alternatively use -titleMsg) null action (in pageflow:action format
//			used to generate title
// -titleMsg = (alternatively use -titleAction) Key of zXMsg to use to generate
//			title for upload page
// -allowDelete = (optional) if 1, we will unset the fk.doc attribut if no file
//			was uploaded 
//
// ============
// SIMPLE DOCUMENT UPLOAD
// ============
// -a = asp.upload or upload
//			Allow the user to upload a document that stands alone. Can set the new zXDoc PK
//			into the pageflow context, presumably for use by the calling program
// -pkcontext = (optional) name of pageflow context entry to write PK to. Will append
//			existing entry as csv. Will not write it if this parameter is left blank
//
//			All other parameters as per FK DOCUMENT UPLOAD except that -e, -fk.value and 
//			-fk.doc are expected to be blank. 
//	 
// ============
// DOCATTR.*
// ============
// These are actions that are most likely to be invoked using the associated Javascript
// routines that can be found in zXForm.js.
// These routines are designed to be used in combination with an entity attribute that is
// a foreign key to zXDoc. 
// This attribute should be locked and on the edit form have 
// an enhancer associated with a number of refs that invoke any of following functions:
// -a = docattr.view: view the document
// -a = docattr.upload: allow user to upload a document, create a new zXDoc for this
//		and store PK and label in appropriate fields on editoform; note that if the user
//		cancels the edit form, the zXDoc will be 'dangling'
// -a = docattr.reset: simply reset the edit form fields to null
// -a = docattr.edit: allow user to edit the zXDoc keywords
//
// Important note: THE ZXDOC ATTRIBUTE MUST BE LOCKED ON THE EDT FORM
//
// Common parameters:
//
// -ctr = name of control that holds PK of zXDoc (use director #ctr.entity.attr)
// -ctrLabel = name of control that holds label (use director (#ctrdivlock.entity.attr)
// -docType = name of document type (for upload only)
// -pk.doc = zXDoc pk
//
// These have the following Javascript equivalents:
//
// zXDocAttrView(_session, _ctr)
// zXDocAttrUpload(_session, _ctr, _ctrLabel, _docType)
// zXDocAttrReset(_session, _ctr, _ctrLabel)
// zXDocAttrEdit(_session, _ctr, _ctrLabel, _confirm)
//
// When invoking the Javascript routines the _ctr and _ctrLabel are the HANDLES
// to the controls, not the name of the controls
//----
%><%@ page import="org.zxframework.*" 
%><%@ page import="org.zxframework.web.*" 
%><%@ page import="org.zxframework.misc.ZXDoc" 
%><%@ page import="org.zxframework.misc.DocTpe" 
%><%@ page import="org.zxframework.property.*" 
%><%@ page import="org.zxframework.util.*" 
%><%@ page import="org.apache.commons.fileupload.*" 
%><%@taglib uri="/WEB-INF/zX.tld" prefix="zx"
%><zx:zx/><%
ZX objZX = null;
PageBuilder objPage = null;
try {
	objZX = ThreadLocalZX.getZX();
	objPage = new PageBuilder();
	
	String strAction = request.getParameter("-a");
	if (strAction != null) {
		strAction.toLowerCase();
	} else {
		strAction = "";
	}
	
	if (strAction.equals("adhocbuild.requestinfo.createupdate")
		|| strAction.equals("docprint.requestinfo.createupdate")) {
		// @ page autoFlush="false"
	} else {
		// Normal
	}
%>
<html>

<head>
	<link href="../includes/zXStylesheet.css" rel="stylesheet" type="text/css">

	<script type="text/javascript" language="JavaScript" src="../javascript/zX.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXForm.js"></script>
	<script type="text/javascript" language="JavaScript" src="../javascript/zXList.js"></script>

	<title><%=objPage.getWebSettings().getPageTitle()%></title>
</head>

<body  background="<%=objPage.getWebSettings().getBackgroundImg()%>" bgproperties="fixed" scroll="auto">
<%
	//----
	// Safeguard against development errors
	//----
	int intLoopCounter = 0;
	
	//----	
	// Create html and pageflow objects
	//----
	Pageflow objPageflow = objPage.getPageflow(request, response, "zXDoc");
	if (objPageflow == null) {
		out.write(objPage.fatalError("Unable to initialise pageflow"));
		return;
	}
	
	zXType.rc rc = null;
	do {
		
		if (strAction.endsWith("adhocbuild")) {
			//----
			// User wants to build a document and view / print / save
			//----
			objZX.getQuickContext().setEntry("-sa", "editNoDB");
			
			if (StringUtil.len(objZX.getQuickContext().getEntry("-titleAction")) > 0) {
				// We have a special title action that we want to show
				objPageflow.go("adHocBuild.requestInfo.externalTitle");
				// And switch back to the zXDoc method again
				objPageflow.go("zXDoc");
			}
			strAction = "adHocBuild.requestInfo.edit";
			
		} else if (strAction.equals("asp.adhocbuild.go")) {
			//----
			// The user has entered the relevant information (i.e. wants to view,
			// wants to print)
			//----
			objPageflow.go("adHocBuild.progress.start");
			out.write(objPage.flush());
			out.flush();
			
			// Some ground work
			ZXBO objDocMisc = objPageflow.getEntity("adhocBuild.requestInfo.edit", "zXDocMsc").getBo();
			if (objDocMisc == null) {
				out.write(objPage.fatalError("Unable to get handle to adhocBuild.requestInfo.edit/zXDocMsc"));
				return;
			}
			
			DocTpe objDocTpe = (DocTpe)objZX.createBO("zxDocTpe");
			if (objDocTpe == null) {
				out.write(objPage.fatalError("Unable to create instance of zxDocTpe"));
				return;
			}
			
			//----
			// V1.4:21 BD27DEC04
 			// If the user is not interested in viewing the document, we should close the window here and now
			// to allow the user to continue working whilst this ASP will generate and print the document
			//----
			if (!objDocMisc.getValue("vwDcmnt").booleanValue() && StringUtil.len(request.getParameter("-nextAction")) == 0) {
				out.write("<script Language=Javascript>\n");
				out.write("top.window.close();\n");
				out.write("</script>\n");
				
				//----
				// Force the window close instruction to the browser
				//----
				out.flush();
			}
			
			//----
			// Init so we can compare to nothing later
			//----
			ZXBO objBO = null;
			
			//----
			// Updating user preference with printer
			//----
			if (StringUtil.len(request.getParameter("-printer") ) > 0) {
				objZX.getUserProfile().getDS().beginTx();
				objZX.getUserProfile().setPreference(request.getParameter("-printer"), objDocMisc.getValue("prntr").getStringValue());
				objZX.getUserProfile().getDS().commitTx();
			}
			
			//----
			// Build document
			//----
			org.zxframework.doc.DocBuilder objDocBuilder = new org.zxframework.doc.DocBuilder();
			
			objPageflow.go("adHocBuild.progress.load");
			out.write(objPage.flush());
			out.flush();			
			
			if (objDocBuilder.startDoc(request.getParameter("-template")).pos != zXType.rc.rcOK.pos) {
				objPageflow.go("adHocBuild.progress.error");
				out.write(objPage.flush());
				out.flush();
				
				out.write(objPage.fatalError("Unable to load document template"));
				return;
			}
			
			objPageflow.go("adHocBuild.progress.build");
			out.write(objPage.flush());
			out.flush();
			
			if (objDocBuilder.buildDoc(request.getParameter("-pk"), null).pos != zXType.rc.rcOK.pos) {
				objPageflow.go("adHocBuild.progress.error");
				out.write(objPage.flush());
				out.flush();
				
				out.write(objPage.fatalError("Unable to build document template"));
				return;
			}
			
			//----
			// If the caller wants to either view the document or persist it,
			// we must save the physical document
			//----
			String strFilename = "";
			String strPhysicalPath = "";
			String strPersist = request.getParameter("-persist");
			if (objDocMisc.getValue("vwDcmnt").booleanValue() || StringUtil.len(strPersist) > 0) {
				objPageflow.go("adHocBuild.progress.saveOpen");
				out.write(objPage.flush());
				out.flush();
				
				strFilename = objZX.getUserProfile().getValue("id").getStringValue() + ".doc";
				strPhysicalPath = getServletContext().getRealPath("/tmp") + "/" + strFilename;
				
				if (objDocBuilder.saveDoc(strPhysicalPath).pos != zXType.rc.rcOK.pos) {
					objPageflow.go("adHocBuild.progress.error");
					out.write(objPage.flush());
					out.flush();					
					
					out.write(objPage.fatalError("Unable to save generated document"));
					return;
				}
				
			} //  Need to save file for view or persist
			
			if (StringUtil.len(strPersist) > 0) {
				//----
				// User wants to persist the document; if the persist is 'one' it could be
				// that we already have a zXDoc
				// First determine the FK to the document 
				//----
				String strFKDoc = objZX.getQuickContext().getEntry("-fk.doc");
				if (StringUtil.len(strFKDoc) == 0) {
					//----
					// Fk.doc not passed explicitly so need to figure out what it is
					//----
					objBO = objZX.createBO(objZX.getQuickContext().getEntry("-e"));
					if (objBO == null) {
						out.write(objPage.fatalError("Unable to create instance of '"  
										   			 + objZX.getQuickContext().getEntry("-e") + "'"));
						return;	
					}
					
					ZXDoc objDoc = (ZXDoc)objZX.createBO("zXDoc");
					if (objDoc == null) {
						out.write(objPage.fatalError("Unable to create instance of zXDoc"));
						return;
					}
					
					strFKDoc = objBO.getFKAttr(objDoc).getName();
					
					if (StringUtil.len(strFKDoc) == 0) {
						out.write(objPage.fatalError("Unable to get FK attribute to zXDoc from '" 
										   			 + objZX.getQuickContext().getEntry("-e") + "'"));
						return;
					}
					
				} //  Determined strFKDoc
				
				if (strPersist.equalsIgnoreCase("many")) {
					//----
					//  -e is the name of the linking entity
					// strFKDoc is now the FK to the zXDoc
					// -fk.attr is the attribute that points to an entity with pk -pk
					//----
					if (objBO == null) {
						objBO = objZX.createBO(objZX.getQuickContext().getEntry("-e"));
					}
					
					if (objBO == null) {
						out.write(objPage.fatalError("Unable to create instance of '" 
										   			 + objZX.getQuickContext().getEntry("-e") + "'"));
						return;
					}
					
					//----
					// Create instance of zXDoc
					//----
					ZXDoc objDoc = objDocTpe.getDoc(objZX.getQuickContext().getEntry("-docType"),
													"",
													objDocMisc.getValue("dcmntNts").getStringValue());
					 
					if (objDoc == null) {
						out.write(objPage.fatalError("Unable to create instance of zXDoc for type '" 
										   			 + objZX.getQuickContext().getEntry("-docType") + "'"));
						return;
					}
					
					objBO.resetBO("*", true);
					
					//----
					// Now set the foreign keys to both zXDoc and 'the other' entity
					//----
					objBO.setValue(strFKDoc, objDoc.getValue("id"));
					
					if (StringUtil.len(objZX.getQuickContext().getEntry("-fk.pk")) == 0) {
						objBO.setValue(objZX.getQuickContext().getEntry("-fk.attr"),	
									   			  new StringProperty(objZX.getQuickContext().getEntry("-pk")));
						
					} else {
						objBO.setValue(objZX.getQuickContext().getEntry("-fk.attr"),	
									   			  new StringProperty(objZX.getQuickContext().getEntry("-fk.pk")));
					}
					
					//----
					// And insert
					//----
					objBO.getDSHandler().beginTx();
					
					try {
						
						if (objBO.insertBO().pos != zXType.rc.rcOK.pos) {
							if (objBO.getDSHandler().inTx()) {
								objBO.getDSHandler().rollbackTx();
							}
														
							out.write(objPage.fatalError("Unable to insert instance of '" 
											   			 + objZX.getQuickContext().getEntry("-e") + "'"));
							return;
							
						}
						
						objBO.getDSHandler().commitTx();
						
						//----
						// And now save the temporary file
						//----
						objDoc.replaceFile(strPhysicalPath);
						
					} catch (Exception e) {
						if (objBO.getDSHandler().inTx()) {
							objBO.getDSHandler().rollbackTx();
						}
						
						out.write(objPage.fatalError("Unable to insert instance of '" 
										   			 + objZX.getQuickContext().getEntry("-e") + "'"));
						return;
					}
					
				} else if (strPersist.equalsIgnoreCase("reuse")) {
					//----
					// There is a one-to-many relationship between the entity (-e)
					// and zXDoc; 'reuse' means that we re-use if already there
					//----
					objBO = objZX.getBos().quickLoad(objZX.getQuickContext().getEntry("-e"),
														  new LongProperty(Integer.parseInt(objZX.getQuickContext().getEntry("-pk")), false),
														  "",
														  strFKDoc);
					if (objBO == null) {
						out.write(objPage.fatalError("Unable to load instance of '" 
										   			 + objZX.getQuickContext().getEntry("-e") + "'"));
						return;
					}
					
					ZXDoc objDoc;
					if (objBO.getValue(strFKDoc).isNull) {
						objDoc = objDocTpe.getDoc(objZX.getQuickContext().getEntry("-docType"),
												  "",
												  objDocMisc.getValue("dcmntNts").getStringValue());
						if (objDoc == null) {
							out.write(objPage.fatalError("Unable to create instance of zXDoc for type '" 
											   			 + objZX.getQuickContext().getEntry("-docType") + "'"));
							return;
						}
						
					} else {
						objDoc = (ZXDoc)objBO.quickFKLoad(strFKDoc);
						
						if (objDoc == null) {
							out.write(objPage.fatalError("Unable to retrieve instance of zXDoc"));
							return;
						}
						
					}
					
					//----
					// Save file and update the entity
					//----
					objDoc.replaceFile(strPhysicalPath);
					
					objBO.setPKValue(objZX.getQuickContext().getEntry("-pk"));
					objBO.setValue(strFKDoc, objDoc.getValue("id"));
					
					objBO.getDSHandler().beginTx();
					
					try {
						if (objBO.updateBO(strFKDoc).pos != zXType.rc.rcOK.pos) {
							if (objBO.getDSHandler().inTx()) {
								objBO.getDSHandler().rollbackTx();
							}
							
							out.write(objPage.fatalError("Unable to update instance of '" 
											   			 + objZX.getQuickContext().getEntry("-e") + "'"));
							return;
							
						}
						
						//----
						// In case of re-use we have to set the keywords
						//----
						objDoc.setValue("id", objBO.getValue(strFKDoc));
						objDoc.setValue("keyWrds", objDocMisc.getValue("dcmntNts"));
						
						try {
							if (objDoc.updateBO().pos != zXType.rc.rcOK.pos) {
								if (objDoc.getDSHandler().inTx()) {
									objDoc.getDSHandler().rollbackTx();
								}
								
								out.write(objPage.fatalError("Unable to update instance of zXDoc"));
								return;
							}
							
							objDoc.getDSHandler().commitTx();
							
						} catch (Exception e) {
							if (objDoc.getDSHandler().inTx()) {
								objDoc.getDSHandler().rollbackTx();
							}
							
							out.write(objPage.fatalError("Unable to update instance of zXDoc"));
							return;
						}
						
					} catch (Exception e) {
						if (objBO.getDSHandler().inTx()) {
							objBO.getDSHandler().rollbackTx();
						}
						
						out.write(objPage.fatalError("Unable to update instance of '" 
											   		 + objZX.getQuickContext().getEntry("-e") + "'"));
						return;
					} // Done updating zXDoc and entity
					
				} else if (strPersist.equalsIgnoreCase("one")) {
					//----
					// There is a one-to-many relationship between the entity (-e)
					// and zXDoc; 'one' always create new zXDoc even when already one available
					//----
					objBO = objZX.getBos().quickLoad(objZX.getQuickContext().getEntry("-e"),
													 new LongProperty(Integer.parseInt(objZX.getQuickContext().getEntry("-pk")), false),
													 "",
													 strFKDoc);
					if (objBO == null) {
						out.write(objPage.fatalError("Unable to load instance of '" 
										   			 + objZX.getQuickContext().getEntry("-e") + "'"));
						return;
					}
					
					ZXDoc objDoc = objDocTpe.getDoc(objZX.getQuickContext().getEntry("-docType"),
											  		"",
											  		objDocMisc.getValue("dcmntNts").getStringValue());
					if (objDoc == null) {
						out.write(objPage.fatalError("Unable to create instance of zXDoc for type '" 
										   			 + objZX.getQuickContext().getEntry("-docType") + "'"));
						return;
					}
					
					//----
					// Save file and update the entity
					//----
					objDoc.replaceFile(strPhysicalPath);
					
					objBO.setPKValue(objZX.getQuickContext().getEntry("-pk"));
					objBO.setValue(strFKDoc, objDoc.getValue("id"));
					
					objBO.getDSHandler().beginTx();
					
					try {
						
						if (objBO.updateBO(strFKDoc).pos != zXType.rc.rcOK.pos) {
							if (objBO.getDSHandler().inTx()) {
								objBO.getDSHandler().rollbackTx();
							}
							
							out.write(objPage.fatalError("Unable to update instance of '" 
														 + objZX.getQuickContext().getEntry("-e") + "'"));
							return;
						} // Done updating zXDoc and entity
						
						objBO.getDSHandler().commitTx();
						
					} catch (Exception e) {
						if (objBO.getDSHandler().inTx()) {
							objBO.getDSHandler().rollbackTx();
						}
							
						out.write(objPage.fatalError("Unable to update instance of '" 
													 + objZX.getQuickContext().getEntry("-e") + "'"));
						return;
					}
					
				}
				
			} // Handled -persist flag
			
			//----
			// May need to view doc
			//----
			if (objDocMisc.getValue("vwDcmnt").booleanValue()) {
				
				String strUrl = "../jsp/zXDoc.jsp?";
				strUrl = strUrl + "-s=" +  request.getParameter("-s");
				strUrl = strUrl + "&-a=asp.adHocBuild.view";
				strUrl = strUrl + "&-pf=zXDoc";
				strUrl = strUrl + "&-file=" + StringEscapeUtils.escapeJavaScript("../tmp/" + strFilename);
				
				out.write("<script Language=Javascript>");
				out.write("zXPopupStep1('" + request.getParameter("-s") + "', '" + strUrl + "', 0, 1);");
				out.write("</script>");
				
			} // Done view
			
			//----
			// May need to print doc
			//----
			if (!objDocMisc.getValue("prntr").isNull) {
				ZXBO objPrntr = objDocMisc.quickFKLoad("prntr");
				if (objPrntr == null) {
					out.write(objPage.fatalError("Unable to load printer definition"));
					return;
				}
				
				objPageflow.go("adHocBuild.progress.print");
				out.write(objPage.flush());
				out.flush();
				
				//----
				// BD18NOV04 - Added number of copies
				//-----
				int intCopies = objDocMisc.getValue("nmbrOfCps").intValue();
				for (int i = 0; i < intCopies; i++) {
					if (objDocBuilder.printDoc(objPrntr.getValue("prntr").getStringValue()).pos != zXType.rc.rcOK.pos) {
						objPageflow.go("adHocBuild.progress.error");
						out.write(objPage.flush());
						out.flush();					
						
						out.write(objPage.fatalError("Unable to print generated document"));
						return;
					}
					
					objPageflow.go("adHocBuild.progress.printedCopy");
					out.write(objPage.flush());
					out.flush();
				}
				
			} // Done print
			
			//----
			// Properly close doc build
			//----
			objDocBuilder.closeDoc();
			objDocBuilder = null;
			
			//----
			// And handle next action
			//----
			if (StringUtil.len(request.getParameter("-nextAction")) == 0) {
				//----
				// V1.4:21 BD27DEC04 Already done before generating the document if no view document
				// was requested; so only required 
				//----
				if (objDocMisc.getValue("vwDcmnt").booleanValue()) {
					out.write("<script Language=Javascript>");
					out.write("top.window.close();");
					out.write("</script>");
				}
				
				strAction = "";
			} else {
				//----
				// link to the next action through a null action named 'nextAction.null'. This allows the
				// use of pageflow:action style next actions.
				//----
				strAction = "adhocBuild.nextAction";
				
			}
			
		} else if (strAction.equals("asp.adhocbuild.view")) {	
			//----
			// Main frame for viewing an adhoc built document
			//----
			out.write("<script language=\"Javascript\" type=\"text/javascript\">\n");
			out.write("this.document.location='" + request.getParameter("-file") + "';\n");
			out.write("top.fraFooter.anchorDownLoad.href = '" + request.getParameter("-file") + "';\n");
			out.write("</script>\n");
			strAction = "";
			
//--------------------------------
// DOCUMENT VIEW ACTIONS
//--------------------------------			
		} else if (strAction.endsWith("docview")) {
			
			// #qs.-pk.zXDoc is the pk to the zXDoc object
			ZXDoc objDoc = (ZXDoc)objZX.getBos().quickLoad("zXDoc",
														   new StringProperty(objZX.getQuickContext().getEntry("-pk.zXDoc"))
														   );
			if (objDoc == null) {
				out.write(objPage.fatalError("Unable to create and load instance of zXDoc"));
				return;
			}
			
			// Here is the call to the Doc object's 'view' method, which does all the work.
			// Pass in the logical and physical paths to use for temporary attachments.
			boolean blnViewer = false;
			if (objZX.getQuickContext().getEntry("-noViewer").equals("1") ) {
				blnViewer = false;			
			} else {
				blnViewer = true;
			}
			boolean blnDownload = false;
			if (objZX.getQuickContext().getEntry("-noDownLoad").equals("1") ) {
				blnDownload = false;			
			} else {
				blnDownload = true;
			}
			out.write(objDoc.view("../tmp/", getServletContext().getRealPath("tmp/"), blnViewer, blnDownload).toString());
			
			// Next action maybe #qs.-nextAction
			strAction = "docView.nextAction";  // objZX.getQuickContext().getEntry("-nextAction");
			
//----------------------------------
// DOCUMENT PRINT ACTIONS
//----------------------------------
		} else if (strAction.endsWith("docprint")) {
			//----
			// User wants to print a Word document
			//----
			if (StringUtil.len(objZX.getQuickContext().getEntry("-titleAction")) > 0) {
				// We have a special title action that we want to show
				objPageflow.go("docPrint.requestInfo.externalTitle");
				// And switch back to the zXDoc method again
				objPageflow.switchPageflow("zXDoc");
			}
			
			objZX.getQuickContext().setEntry("-sa", "editNoDB");
			strAction = "docPrint.requestInfo.edit";
			
		} else if (strAction.equals("asp.docprint.go")) {
			//----
			// The user has selected a printer and is ready to roll....
			//----
			objPageflow.go("docPrint.progress.start");
			out.write(objPage.flush());
			out.flush();
			
			// Some ground work
			ZXBO objDocMisc = objPageflow.getEntity("adhocBuild.requestInfo.edit", "zXDocMsc").getBo();
			if (objDocMisc == null) {
				out.write(objPage.fatalError("Unable to get handle to adhocBuild.requestInfo.edit/zXDocMsc"));
				return;
			}
			
			// Updating user preference with printer
			String strPrinter = request.getParameter("-prefPrinter");
			if (StringUtil.len(strPrinter) > 0) {
				objZX.getUserProfile().getDS().beginTx();
				objZX.getUserProfile().setPreference(strPrinter, objDocMisc.getValue("prntr").getStringValue());
				objZX.getUserProfile().getDS().commitTx();				
			}
			
			// Load and print document
			objPageflow.go("printDoc.progress.load");
			out.write(objPage.flush());
			out.flush();
			
			ZXDoc objDoc = (ZXDoc)objZX.getBos().quickLoad("zXDoc", 
														   new StringProperty(objZX.getQuickContext().getEntry("-pk.zXDoc")), 
														   "id");
			
			if (objDoc == null) {
				objPageflow.go("printDoc.progress.error");
				out.write(objPage.flush());
				out.flush();
				
				out.write(objPage.fatalError("Unable to load instance of zXDoc"));
				return;
			}
			
			// TODO - Doc printing support.
			// if objzX.word.openDoc(objDoc.iBO_getAttr("fllFleNme").strValue) <> 0 then
			//	objPageflow.go "adHocBuild.progress.error"
			//	Response.Write objHTML.html
			//		
			//	fatalError "Unable to open document"
			//end if
			
			objPageflow.go("adHocBuild.progress.print");
			out.write(objPage.flush());
			out.flush();
			
			// Set printer and print
			Printer objPrntr = (Printer)objDocMisc.quickFKLoad("prntr");
			if (objPrntr == null) {
				objPageflow.go("printDoc.progress.error");
				out.write(objPage.flush());
				
				out.write(objPage.fatalError("Unable to retrieve printer definition"));
				return;
			}
			
//			'----
//			' Set printer and print
//			'----
//			set objPrntr = objzX.BOS.quickFKLoad(objDocMisc, "prntr")
//			if objPrntr is nothing then
//				objPageflow.go "adHocBuild.progress.error"
//				Response.Write objHTML.html
//					
//				fatalError "Unable to retrieve printer definition"
//			end if
//			
//			if objzX.word.setPrinter( objPrntr.iBO_getAttr("prntr").strValue ) <> 0 then
//				objPageflow.go "adHocBuild.progress.error"
//				Response.Write objHTML.html
//					
//				fatalError "Unable to set printer"
//			end if
//
//			'----
//			' BD18NOV04 - Added number of copies and default to 1
//			'----
//			if objDocMisc.iBO_getAttr("nmbrOfCps").lngValue = 0 then
//				objDocMisc.iBO_setAttr "nmbrOfCps", objzX.lngValue(1)
//			end if
//			
//			for intCopy = 1 to objDocMisc.iBO_getAttr("nmbrOfCps").lngValue
//				if objzX.word.wordDoc.PrintOut((false)) <> 0 then
//					objPageflow.go "adHocBuild.progress.error"
//					Response.Write objHTML.html
//						
//					fatalError "Unable to print document"
//				end if
//
//				objPageflow.go "adHocBuild.progress.printedCopy"
//				Response.Write objHTML.html
//			next 
//			
//			'----
//			' Properly close doc
//			'----
//			objzX.word.closeDoc() 
			
			// And handle next action
			if (StringUtil.len(request.getParameter("-nextAction") ) == 0) {
				out.write("<script language=\"Javascript\" type=\"text/javascript\">\n");
				out.write("top.window.close();\n");
				out.write("</script>\n");
			} else {
				// link to the next action through a null action named 'nextAction.null'. This allows the
				// use of pageflow:action style next actions.
				strAction = "docPrint.nextAction";
			}

//--------------------------------------------
//SINGLE DOCUMENT UPLOAD ACTIONS
//--------------------------------------------			
		} else if (strAction.equalsIgnoreCase("asp.fkupload")
				   || strAction.equalsIgnoreCase("fkupload")
				   || strAction.equalsIgnoreCase("asp.upload")
				   || strAction.equalsIgnoreCase("upload")) {
			//----
			// User wants to set an FK to zXDoc of an entity by uploading a file (fkupload actions)
			// or just wants to upload a document and save the zXDoc entry (upload)
			//----
			if (StringUtil.len(objZX.getQuickContext().getEntry("-titleAction")) > 0) {
				// We may have an external title action
				objPageflow.go(objZX.getQuickContext().getEntry("-titleAction"));
				
				// And make zXDoc active again
				objPageflow.switchPageflow("zXDoc");
			} else {
				// Otherwise use our internal title
				objPageflow.go("fkUpload.title");
			}
			out.write(objPage.flush());
			
			%>

			<form enctype="multipart/form-data" method="post" action="dummy" id="form1" name="form1">
			<table width="100%" border="0" cellPadding="3" cellSpacing="3">
			  <tr>
			    <td class="zXFormLabel">Document: </td>
			    <td><input NAME="File1" TYPE="file" class="zxFormInputOptionalMixed"></td>
<%
			if (!objZX.getQuickContext().getEntry("-noKeywords").equals("1")) {
				// Some html
%>
			    <td class="zXFormLabel">Keywords: </td>
			    <td><input NAME="KeyWrds1" TYPE="text" class="zxFormInputOptionalMixed" size="60" maxlength="255"></td>
<%
			}
%>
			  </tr>
		    </table>
			<BR>

			<%
			// Add in the upload buttons.
			objPageflow.go("fkUpload.buttons");
			out.write(objPage.flush());
			%>
			
			</form>
			<%
			strAction = "";
			
		} else if (strAction.equalsIgnoreCase("asp.fkupload.process")) {
			//-----
			// User has submitted upload files
			//-----
			FileUpload objUpload = new FileUpload();
			
			// Create required BOs
			DocTpe objDocTpe = (DocTpe)objZX.createBO("zxDocTpe");
			if (objDocTpe == null) {
				out.write(objPage.fatalError("Unable to create instance of zxDocTpe"));
				return;
			}
			
			ZXDoc objDoc = (ZXDoc)objZX.createBO("zxDoc");
			if (objDoc == null) {
				out.write(objPage.fatalError("Unable to create instance of zxDoc"));
				return;
			}
			
			ZXBO objBO = null;
			if (StringUtil.len(objZX.getQuickContext().getEntry("-e")) > 0) {
				objBO = objZX.createBO(objZX.getQuickContext().getEntry("-e"));
				if (objBO == null) {
					out.write(objPage.fatalError("Unable to create instance of " 
												 + objZX.getQuickContext().getEntry("-e")));
					return;
				}
			}
			
			//----
			//Set maximum file size allowed to approx. 8 MB
			//----
			objUpload.setSizeMax(8388608);
			
			//----
			// Save all uploaded form data to memory.  Note that this also populates the Files and
			// Form collections with ALL uploaded form data
			//----
			java.util.List colItems = objUpload.parseRequest(request);
						
			//----
			// Get document type
			//----
			String strDocType = objZX.getQuickContext().getEntry("-docType");
			if (StringUtil.len(strDocType) == 0) {
				strDocType = "ATTACH";
			}
			
			//----
			// Get FK to zXDoc
			// But not if we don't have an FK entity to worry about here
			//----
			String strFKDoc = "";
			if (StringUtil.len(objZX.getQuickContext().getEntry("-e")) > 0) {
				if (StringUtil.len(objZX.getQuickContext().getEntry("-fk.doc")) == 0 && objBO != null) {
					strFKDoc = objBO.getFKAttr(objDoc).getName();
				} else {
					strFKDoc = objZX.getQuickContext().getEntry("-fk.doc");
				}
			}
			
			if (colItems.size() > 0) {
				FileItem item;
				java.io.File file;
				int lItem = 0;
				
				java.util.Iterator iterItem = colItems.iterator();
				while (iterItem.hasNext()) {
					item = (FileItem) iterItem.next();
					
					if (!item.isFormField()) {
						lItem++;
						
						objZX.getDataSources().getTxHandlers().beginTx();
						
						//----
						// Note that the user could leave, say, the first filename blank but use one of the 
						// others. The collection of files only includes those that were not left blank, but
						// we can use the tagName property to see which input file field name was used, and
						// take the last character (a number from 1 to 4) to get the corresponding keyWrds.
						//----
						String strFilename = new java.io.File(item.getName()).getName();
						if (objZX.getQuickContext().getEntry("-noKeyWords").equals("1")) {
							objDoc = objDocTpe.getDoc(strDocType, strFilename, "");
						} else {
							objDoc = objDocTpe.getDoc(strDocType, strFilename, request.getParameter("KeyWrds" + lItem));
						}
						if (objDoc == null) {
							out.write(objPage.fatalError("Unable to get physical document details for file " 
														 + strFilename));
							return;
						}
						
						file = new java.io.File(objDoc.getFullFileName());
						
						try {
							item.write(file);
						} catch (Exception e1) {
							out.write(objPage.fatalError("Unable to save file " 
														 + objDoc.getFullFileName() + " (" + e1.toString() + ")"));
							// break; // Need to exit.
							return;
						}
						
						//----
						// And set the FK to zXDoc
						// But not if we don't have an FK entity to worry about here
						//----
						if (StringUtil.len(objZX.getQuickContext().getEntry("-e")) > 0 && objBO != null) {
							objBO.setPKValue(objZX.getQuickContext().getEntry("-fk.value")); 
							objBO.setValue(strFKDoc, objDoc.getValue("id"));
							
							if (objBO.updateBO(strFKDoc).pos != zXType.rc.rcOK.pos) {
								out.write(objPage.fatalError("Unable to update " 
															 + objBO.getDescriptor().getName()));
								return;
							}
							
						}
						
						objPageflow.setInfoMsg("Document uploaded");
						
						objZX.getDataSources().getTxHandlers().commitTx();
						
						//----
						// If -pkcontext is set, add the new zXDoc PK to the pageflow context
						// entry of that name
						//----
						String strPKContext = objZX.getQuickContext().getEntry("-pkcontext");
						if (StringUtil.len(strPKContext) > 0) {
							String strTmp = objPageflow.getFromContext(strPKContext,true);
							objPageflow.addToContext(strPKContext, strTmp + objDoc.getValue("id").getStringValue() + ",", true);
						}
					}
				}
				
			} else {
				//----
				// Maybe have to remove the setting
				// But not if we don't have an FK entity to worry about here
				//----
				if (StringUtil.len(objZX.getQuickContext().getEntry("-e")) >  0 && objBO != null) {
					objZX.getDataSources().getTxHandlers().beginTx();
					
					objBO.setValue(strFKDoc, new StringProperty("", true));
					if (objBO.updateBO(strFKDoc).pos != zXType.rc.rcOK.pos) {
						out.write(objPage.fatalError("Unable to update " 
													 + objBO.getDescriptor().getName()));
						return;
					}
					
					objZX.getDataSources().getTxHandlers().commitTx();
				}
			}
			
			// And goto next action
			if (StringUtil.len(request.getParameter("-nextAction")) == 0) {
				out.write("<script Language=Javascript>");
				out.write("top.window.close();");
				out.write("</script>");
				
				strAction = "";
			} else {
				strAction = "fkUpload.nextAction";
			}
			
		} else if (strAction.endsWith("doclist")) {
			//----
			// Handle document list
			// If -fk.doc is not passed, we have to determine it
			//----
			if (StringUtil.len(objZX.getQuickContext().getEntry("-fk.doc")) == 0) {
				strAction = "docList.getFkDoc";
			} else {
				strAction = "docList.query";
			}
			
		} else if (strAction.equals("asp.doclist.list.title")) {
			//----
			// We may have an external title
			//----
			if (StringUtil.len(objZX.getQuickContext().getEntry("-titleAction.list")) > 0) {
				objPageflow.go(objZX.getQuickContext().getEntry("-titleAction.list"));
				
				//----
				// And make zXDoc active again
				//----
				objPageflow.switchPageflow("zXDoc");
			}
			strAction = "docList.list";
			
		} else if (strAction.equals("asp.doclist.edit.title")) {
			//----
			// We may have an external title
			//----
			if (StringUtil.len(objZX.getQuickContext().getEntry("-titleAction.edit")) > 0) {
				objPageflow.go(objZX.getQuickContext().getEntry("-titleAction.edit"));
				
				// And make zXDoc active again
				objPageflow.switchPageflow("zXDoc");
			}
			strAction = "docList.edit";
			
		} else if (strAction.equals("asp.doclist.view")) {
			//----
			// Here to view an attachment
			// -pk.zXDoc = PK of doc to view
			//----
			ZXDoc objDoc = (ZXDoc)objZX.getBos().quickLoad("zxDoc", 
														   new StringProperty(objZX.getQuickContext().getEntry("-pk.zXDoc"))
														   );
			if (objDoc == null) {
				out.write(objPage.fatalError("Unable to create instance of zxDoc"));
				return;
			}
			
			//----
			// Here is the call to the Doc object's 'view' method, which does all the work.
			// Pass in the logical and physical paths to use for temporary attachments.
			//----
			out.write(objDoc.view("../tmp/", getServletContext().getRealPath("/tmp"), true, true).toString());
			
			// No next action as all is done in popup
			strAction = "";
			
		} else if (strAction.equals("asp.doclist.delete")) {
			//----
			// User wants to delete entity
			//----
			ZXBO objBO = objPageflow.getEntity("docList.query", "entity").getBo();
			if (objBO == null) {
				out.write(objPage.fatalError("Unable to retrieve docList.query.entity"));
				return;
			}
			
			objBO.setPKValue(objZX.getQuickContext().getEntry("-zXDoc.pk.entity"));
			objZX.getDataSources().getTxHandlers().beginTx();
			
			try {
				if (objBO.deleteBO().pos != 0) {
					objZX.getDataSources().getTxHandlers().rollbackTx();
					strAction = "docList.delete.errMsg";
				} else {
					objZX.getDataSources().getTxHandlers().commitTx();
					strAction = "docList.delete.okMsg";
				}
			} catch (Exception e) {
				objZX.getDataSources().getTxHandlers().rollbackTx();
				strAction = "docList.delete.errMsg";
			}
			
		} else if (strAction.equals("asp.doclist.upload")) {
			//----
			// The user wants to add documents
			//----
			
			//----
			// We may have an external title
			//----
			if (StringUtil.len(objZX.getQuickContext().getEntry("-titleAction.upload")) > 0) {
				objPageflow.go(objZX.getQuickContext().getEntry("-titleAction.upload"));
				
				// And make zXDoc active again
				objPageflow.switchPageflow("zXDoc");
			} else {
				objPageflow.go("docList.upload.title");
			}
			
			out.write(objPage.flush());

			%>

			<form enctype="multipart/form-data" method="post" action="dummy" id=form1 name=form1>
			
			<table width="100%" border="0" cellPadding="3" cellSpacing="3">
			  <tr>
			    <td class="zXFormLabel">Document: </td>
			    <td><input NAME="File1" TYPE="file" class="zxFormInputOptionalMixed"></td>
			    <td class="zXFormLabel">Keywords: </td>
			    <td><input NAME="KeyWrds1" TYPE="text" class="zxFormInputOptionalMixed" size="60" maxlength="255"></td>
			  </tr>
		    </table>
			<BR>
			
			<%
			objPageflow.go("docList.upload.buttons");
			out.write(objPage.flush());
			%>
									
			</form>
			<%
			
			strAction = "";
			
		} else if (strAction.equals("asp.doclist.upload.process")) {
			//-----
			// User has submitted upload files
			//-----
			DiskFileUpload objUpload = new DiskFileUpload();
			
			//----
			// Create required BOs
			//----
			DocTpe objDocTpe = (DocTpe)objZX.createBO("zxDocTpe");
			if (objDocTpe == null) {
				out.write(objPage.fatalError("Unable to create instance of zxDocTpe"));
				return;
			}
			
			ZXBO objBO = objPageflow.getEntity("docList.query", "entity").getBo();
			if (objBO == null) {
				out.write(objPage.fatalError("Unable to get handle to docList.query.entity"));
				return;
			}
			
			//----
			//Set maximum file size allowed to approx. 8 MB
			//----
			objUpload.setSizeMax(8388608);
			
			//----
			// Save all uploaded form data to memory.  Note that this also populates the Files and
			// Form collections with ALL uploaded form data
			//----
			java.util.List colItems = objUpload.parseRequest(request);
						
			// Get document type
			String strDocType = objZX.getQuickContext().getEntry("-docType");
			if (StringUtil.len(strDocType) == 0) {
				strDocType = "ATTACH";
			}
			
			if (colItems.size() > 0) {
				FileItem item;
				java.io.File file;
				int lItem = 0;
				ZXDoc objDoc = null;
				
				java.util.Iterator iterItem = colItems.iterator();
				while (iterItem.hasNext()) {
					item = (FileItem) iterItem.next();
					
					if (!item.isFormField()) {
						lItem++;
						
						objZX.getDataSources().getTxHandlers().beginTx();
						
						//----
						// Note that the user could leave, say, the first filename blank but use one of the 
						// others. The collection of files only includes those that were not left blank, but
						// we can use the tagName property to see which input file field name was used, and
						// take the last character (a number from 1 to 4) to get the corresponding keyWrds.
						//----
						String strFilename = new java.io.File(item.getName()).getName();
						if (objZX.getQuickContext().getEntry("-noKeyWords").equals("1")) {
							objDoc = objDocTpe.getDoc(strDocType,  strFilename, "");
						} else {
							objDoc = objDocTpe.getDoc(strDocType,  strFilename, request.getParameter("KeyWrds" + lItem));
						}
						if (objDoc == null) {
							out.write(objPage.fatalError("Unable to get physical document details for file " 
														 + strFilename));
							return;
						}
						
						file = new java.io.File(objDoc.getFullFileName());
						
						try {
							item.write(file);
						} catch (Exception e1) {
							out.write(objPage.fatalError("Unable to save file " 
														 + objDoc.getFullFileName() + " (" + e1.toString() + ")"));
							return;
						}
						
						try {
							objBO.resetBO("*", true);
						} catch (Exception e) {
							out.write(objPage.fatalError("Unable to set automatics " 
														 + objBO.getDescriptor().getName()));
							return;
						}
						
						//----
						// Now process the attribute values as this will assign the entity
						// to whatever base-entity it is related to
						//----
						objPageflow.processAttributeValues(objPageflow.getEntity("docList.query", "entity"));
						
						//And set the FK to zXDoc
						objBO.setValue(objZX.getQuickContext().getEntry("-fk.attr"),
									   objZX.getQuickContext().getEntry("-fk.value"));
						objBO.setValue(objZX.getQuickContext().getEntry("-fk.doc"), objDoc.getValue("id"));
						
						// Insert the BO for the upload :
						if (objBO.insertBO().pos != zXType.rc.rcOK.pos) {
							out.write(objPage.fatalError("Unable to insert " + objBO.getDescriptor().getName()));
							return;
						}
						
						objPageflow.setInfoMsg("Document uploaded");
						
						objZX.getDataSources().getTxHandlers().commitTx();
					}
				}
				
				//----
				// Allow user to edit details; PK is -zXDoc.pk.entity
				//----
				if (StringUtil.len(request.getParameter("-editAfterUpload")) > 0) {
					//----
					// BD11MAR04
					// VERY IMPORTANT CHANGE: the '1' at the end is very important as this tells
					// the quick context that we have manually added the entry and this should
					// thus NOT be replaced by copying the ASP querystring context
					// Needs a more permanent solution but for now this works
					//----
					
					objZX.getQuickContext().setEntry("-zXDoc.pk.entity", objBO.getPKValue().getStringValue(), 1);
					objZX.getQuickContext().setEntry("-zXDoc.pk.zXDoc", objDoc.getPKValue().getStringValue(), 1);
					
					// Flag to tell that we do not want a delete button just yet
					objZX.getQuickContext().setEntry("-zXDoc.brandNew", "1");
					
					strAction = "docList.edit";
					
				} else {
					strAction = "docList.list";
					
				}
				
			} else {
				strAction = "docList.list";
			}
			
//------------------------
// DOCATR* ACTIONS
//------------------------
		} else if (strAction.endsWith("docattr.upload")) {
			//----
			// The user wants to upload a document
			//----
			objPageflow.go("docAttr.upload.title");
			
			out.write(objPage.flush());
			%>

			<form enctype="multipart/form-data" method="post" action="dummy" id="form1" name="form1">
			
			<table width="100%" border="0" cellPadding="3" cellSpacing="3">
			  <tr>
			    <td class="zXFormLabel">Document: </td>
			    <td><input NAME="File1" TYPE="file" class="zxFormInputOptionalMixed" ></td>
			  </tr>
			  
			  <tr>
			    <td class="zXFormLabel">Keywords: </td>
			    <td><input NAME="KeyWrds1" TYPE="text" class="zxFormInputOptionalMixed" size="60" maxlength="255"></td>
			  </tr>
		    </table>
			<BR>

			<%
			objPageflow.go("docAttr.upload.buttons");
			out.write(objPage.flush());
			%>
			
			</form>
			<%
			
			strAction = "";
			
		} else if (strAction.equalsIgnoreCase("asp.docattr.upload.process")) {
			//----
			// User has submitted upload files
			//----
			DiskFileUpload objUpload = new DiskFileUpload();
						
			// Create required BOs
			DocTpe objDocTpe = (DocTpe)objZX.createBO("zxDocTpe");
			if (objDocTpe == null) {
				out.write(objPage.fatalError("Unable to create instance of zxDocTpe"));
				return;
			}
			
			//----
			//Set maximum file size allowed to approx. 8 MB
			//----
			objUpload.setSizeMax(8388608);
			
			//----
			// Save all uploaded form data to memory.  Note that this also populates the Files and
			// Form collections with ALL uploaded form data
			//----
			java.util.List colItems = objUpload.parseRequest(request);
			
			// Get document type
			String strDocType = objZX.getQuickContext().getEntry("-docType");
			if (StringUtil.len(strDocType) == 0) {
				strDocType = "ATTACH";
			}
			
			if (colItems.size() > 0) {
				
				FileItem item;
				java.io.File file;
				int lItem = 0;
				
				java.util.Iterator iterItem = colItems.iterator();
				while (iterItem.hasNext()) {
					item = (FileItem) iterItem.next();
					
					if (!item.isFormField()) {
						lItem++;
						
						objZX.getDataSources().getTxHandlers().beginTx();
						
						//----
						// Note that the user could leave, say, the first filename blank but use one of the 
						// others. The collection of files only includes those that were not left blank, but
						// we can use the tagName property to see which input file field name was used, and
						// take the last character (a number from 1 to 4) to get the corresponding keyWrds.
						//----
						ZXDoc objDoc;
						String strFilename = new java.io.File(item.getName()).getName();
						if (objZX.getQuickContext().getEntry("-noKeyWords").equals("1")) {
							objDoc = objDocTpe.getDoc(strDocType,  strFilename, "");
						} else {
							objDoc = objDocTpe.getDoc(strDocType,  strFilename, request.getParameter("KeyWrds" + lItem));
						}
						
						if (objDoc == null) {
							out.write(objPage.fatalError("Unable to get physical document details for file " 
														 + strFilename));
							return;
						}
						
						file = new java.io.File(objDoc.getFullFileName());
						
						try {
							item.write(file);
						} catch (Exception e1) {
							out.write(objPage.fatalError("Unable to save file " 
														 + objDoc.getFullFileName() + " (" + e1.toString() + ")"));
							return;
						}
						
						objZX.getDataSources().getTxHandlers().commitTx();
						
						//----
						// Generate Javascript to populate appropriate fields
						//----
						out.write("<script language=\"Javascript\" type=\"text/javascript\">\n");
						out.write("	var _ctr; \n");
						
						out.write("	_ctr = elementByName(top.window.opener, '" +  objZX.getQuickContext().getEntry("-ctr") + "'); \n");
						out.write("	_ctr.value = " + objDoc.getValue("id").getStringValue() + "; \n");
						
						String strLabelCtrl = StringEscapeUtils.escapeJavaScript(objZX.getQuickContext().getEntry("-ctrLabel"));
						out.write("	_ctr = top.window.opener." + strLabelCtrl + "; \n");
						out.write("	if (_ctr == undefined) _ctr = elementByName(top.window.opener, '" + strLabelCtrl + "'); \n");
						out.write("	_ctr.innerHTML = '" + objDoc.formattedString("Label") + "'; \n");
						
						out.write("	top.window.close();\n");
						out.write("</script>\n");
					}
				}
				
				strAction = "";
				
			} else {
				//----
				// User has not selected any file, give him / here another chance
				//----
				strAction = "asp.docattr.upload";
			}
			
		} else if (strAction.equals("asp.docattr.edit.done")) {
			//----
			// User has submitted changed to document and now wants to close window and all that
			//----
			ZXBO objDoc = objPageflow.getEntity("docAttr.edit", "zXDoc").getBo();
			if (objDoc == null) {
				out.write(objPage.fatalError("Unable to get handle to docAttr.edit.zXDoc"));
				return;
			}
			
			out.write("<script language=\"Javascript\" type=\"text/javascript\"> \n");
			out.write("	var _ctr; \n");
			
			out.write("	_ctr = elementByName(top.window.opener, '" +  objZX.getQuickContext().getEntry("-ctr") + "'); \n");
			out.write("	_ctr.value = " + objDoc.getValue("id").getStringValue() + "; \n");
			
			String strLabelCtrl = StringEscapeUtils.escapeJavaScript(objZX.getQuickContext().getEntry("-ctrLabel"));
			out.write("	_ctr = top.window.opener." + strLabelCtrl + "; \n");
			out.write("	if (_ctr == undefined) _ctr = elementByName(top.window.opener, '" + strLabelCtrl + "'); \n");
			out.write("	_ctr.innerHTML = '" + objDoc.formattedString("Label") + "';\n");
			
			out.write("	top.window.close();\n");

			out.write("</script>\n");
				
			strAction = "";
			
		} else {
			
		 	rc = objPageflow.go(strAction);    
		 	if (rc.pos == zXType.rc.rcOK.pos) {
		 		out.flush();
		 		out.write(objPage.flush());
		 		strAction = objPageflow.getAction();
		 		
		 	} else if (rc.pos == zXType.rc.rcWarning.pos){
		 		//----
				// Action not defined in pageflow definition file
				//----
				strAction = objPageflow.getAction().toLowerCase();
				
	        } else {
	        	objPage.reset();
	        	objPage.formatErrorStack(true);
	        	out.write(objPage.flush());
	        	out.flush();
	        }
		}
				
		intLoopCounter++;
	} while (StringUtil.len(strAction) > 0 && intLoopCounter < 20); 
	
	if (intLoopCounter >= 20) {
		out.write(objPage.fatalError("Unprocessed action : " + strAction));
		// return;
	}
	
	//----
	// And store the session if not logged-off
	//----
	if (StringUtil.len(objZX.getSession().getSessionid()) > 0) {
		objZX.getSession().store();
	}
		
	//----
	// Dump any major errors still lingering
	//----
	objPageflow.dumpAllErrors();

%>
</body>
</html>
<%
} catch (Exception e) {
	try {
		if (objZX != null) {
			if (objZX.getDataSources().getTxHandlers().inTx()) {
				objZX.getDataSources().getTxHandlers().rollbackTx();
			}
		}
	} catch (Exception e1) {
		// Ignore exception
	}
	
	PageBuilder.handleJSPException(objPage, out, e);
} finally {
	if (objZX != null) objZX.cleanup();
}
%>