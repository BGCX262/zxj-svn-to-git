package org.zxframework.doc;

/*
 * QI.java
 *
 * A module to sugar coat the UnoRuntime.queryInterface() procedure.
 *
 * Created on February 21, 2003, 5:13 PM
 *
 * Copyright 2003 Danny Brewer
 * Anyone may run this code.
 * If you wish to modify or distribute this code, then
 *  you are granted a license to do so only under the terms
 *  of the Gnu Lesser General Public License.
 * See:  http://www.gnu.org/licenses/lgpl.html
 */

//----------------------------------------------------------------------
//  OpenOffice.org imports
//----------------------------------------------------------------------
import com.sun.star.bridge.XUnoUrlResolver;

//import com.sun.star.awt.*;
import com.sun.star.beans.*;
import com.sun.star.chart.*;
import com.sun.star.container.*;
import com.sun.star.document.*;
import com.sun.star.drawing.*;
import com.sun.star.embed.*;
import com.sun.star.frame.*;
import com.sun.star.lang.*;
import com.sun.star.sheet.*;
//import com.sun.star.style.*;
import com.sun.star.table.*;
import com.sun.star.text.*;
import com.sun.star.uno.*;
import com.sun.star.util.*;
import com.sun.star.view.*;

/**
 * 
 * @author danny brewer
 */
public class QI {
	
	/**
	 * Hide default constructor.
	 */
	private QI() {
		super();
	}
	
	// The following are syntax sugar for UnoRuntime.queryInterface().

	// --------------------------------------------------
	// Beans com.sun.star.beans.*
	// --------------------------------------------------
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XPropertySet interface
	 */
	public static XPropertySet XPropertySet(Object obj) {
		return (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, obj);
	}

	// --------------------------------------------------
	// Bridge com.sun.star.bridge.*
	// --------------------------------------------------
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XUnoUrlResolver interface
	 */
	public static XUnoUrlResolver XUnoUrlResolver(Object obj) {
		return (XUnoUrlResolver) UnoRuntime.queryInterface(XUnoUrlResolver.class, obj);
	}

	// --------------------------------------------------
	// Chart com.sun.star.chart.*
	// --------------------------------------------------
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XChartDocument interface
	 */
	public static XChartDocument XChartDocument(Object obj) {
		return (XChartDocument) UnoRuntime.queryInterface(XChartDocument.class,obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XDiagram interface
	 */
	public static XDiagram XDiagram(Object obj) {
		return (XDiagram) UnoRuntime.queryInterface(XDiagram.class, obj);
	}

	// --------------------------------------------------
	// Container com.sun.star.container.*
	// --------------------------------------------------

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XIndexAccess interface
	 */
	public static XIndexAccess XIndexAccess(Object obj) {
		return (XIndexAccess) UnoRuntime.queryInterface(XIndexAccess.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XNameAccess interface
	 */
	public static XNameAccess XNameAccess(Object obj) {
		return (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XEnumerationAccess interface
	 */
	public static XEnumerationAccess XEnumerationAccess(Object obj) {
		return (XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XUnoUrlResolver interface
	 */
	public static XNameContainer XNameContainer(Object obj) {
		return (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
				obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XNameReplace interface
	 */
	public static XNameReplace XNameReplace(Object obj) {
		return (XNameReplace) UnoRuntime.queryInterface(XNameReplace.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XNamed interface
	 */
	public static XNamed XNamed(Object obj) {
		return (XNamed) UnoRuntime.queryInterface(XNamed.class, obj);
	}

	// --------------------------------------------------
	// Document com.sun.star.document.*
	// --------------------------------------------------

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XEmbeddedObjectSupplier interface
	 */
	public static XEmbeddedObjectSupplier XEmbeddedObjectSupplier(Object obj) {
		return (XEmbeddedObjectSupplier) UnoRuntime.queryInterface(XEmbeddedObjectSupplier.class, obj);
	}
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XDocumentInsertable interface
	 */
	public static XDocumentInsertable XDocumentInsertable(Object obj) {
		return (XDocumentInsertable) UnoRuntime.queryInterface(XDocumentInsertable.class, obj);
	}

	// --------------------------------------------------
	// Drawing com.sun.star.drawing.*
	// --------------------------------------------------

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XDrawPage interface
	 */
	public static XDrawPage XDrawPage(Object obj) {
		return (XDrawPage) UnoRuntime.queryInterface(XDrawPage.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XDrawPageSupplier interface
	 */
	public static XDrawPageSupplier XDrawPageSupplier(Object obj) {
		return (XDrawPageSupplier) UnoRuntime.queryInterface(XDrawPageSupplier.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XDrawPages interface
	 */
	public static XDrawPages XDrawPages(Object obj) {
		return (XDrawPages) UnoRuntime.queryInterface(XDrawPages.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XDrawPagesSupplier interface
	 */
	public static XDrawPagesSupplier XDrawPagesSupplier(Object obj) {
		return (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XLayerManager interface
	 */
	public static XLayerManager XLayerManager(Object obj) {
		return (XLayerManager) UnoRuntime.queryInterface(XLayerManager.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XLayerSupplier interface
	 */
	public static XLayerSupplier XLayerSupplier(Object obj) {
		return (XLayerSupplier) UnoRuntime.queryInterface(XLayerSupplier.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XShape interface
	 */
	public static XShape XShape(Object obj) {
		return (XShape) UnoRuntime.queryInterface(XShape.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XShapes interface
	 */
	public static XShapes XShapes(Object obj) {
		return (XShapes) UnoRuntime.queryInterface(XShapes.class, obj);
	}

	// --------------------------------------------------
	// Embeb com.sun.star.embed.*
	// --------------------------------------------------
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XStorage interface
	 */
	public static XStorage XStorage(Object obj) {
		return (XStorage) UnoRuntime.queryInterface(XStorage.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XEmbedObjectCreator interface
	 */
	public static XEmbedObjectCreator XEmbedObjectCreator(Object obj) {
		return (XEmbedObjectCreator) UnoRuntime.queryInterface(XEmbedObjectCreator.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XEmbeddedObject interface
	 */
	public static XEmbeddedObject XEmbeddedObject(Object obj) {
		return (XEmbeddedObject) UnoRuntime.queryInterface(XEmbeddedObject.class, obj);
	}

	// --------------------------------------------------
	// Frame com.sun.star.frame.*
	// --------------------------------------------------

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XComponentLoader interface
	 */
	public static XComponentLoader XComponentLoader(Object obj) {
		return (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XDispatchProvider interface
	 */
	public static XDispatchHelper XDispatchHelper(Object obj) {
		return (XDispatchHelper) UnoRuntime.queryInterface(XDispatchHelper.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XDispatchProvider interface
	 */
	public static XDispatchProvider XDispatchProvider(Object obj) {
		return (XDispatchProvider) UnoRuntime.queryInterface(XDispatchProvider.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XModel interface
	 */
	public static XModel XModel(Object obj) {
		return (XModel) UnoRuntime.queryInterface(XModel.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XStorable interface
	 */
	public static XStorable XStorable(Object obj) {
		return (XStorable) UnoRuntime.queryInterface(XStorable.class, obj);
	}

	// --------------------------------------------------
	// Lang com.sun.star.lang.*
	// --------------------------------------------------

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XMultiComponentFactory interface
	 */
	public static XMultiComponentFactory XMultiComponentFactory(Object obj) {
		return (XMultiComponentFactory) UnoRuntime.queryInterface(XMultiComponentFactory.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XComponent interface
	 */
	public static XComponent XComponent(Object obj) {
		return (XComponent) UnoRuntime.queryInterface(XComponent.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XMultiServiceFactory interface
	 */
	public static XMultiServiceFactory XMultiServiceFactory(Object obj) {
		return (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XSingleServiceFactory interface
	 */
	public static XSingleServiceFactory XSingleServiceFactory(Object obj) {
		return (XSingleServiceFactory) UnoRuntime.queryInterface(XSingleServiceFactory.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XServiceInfo interface
	 */
	public static XServiceInfo XServiceInfo(Object obj) {
		return (XServiceInfo) UnoRuntime.queryInterface(XServiceInfo.class, obj);
	}

	// --------------------------------------------------
	// Sheet com.sun.star.sheet.*
	// --------------------------------------------------

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XCellRangeAddressable interface
	 */
	public static XCellRangeAddressable XCellRangeAddressable(Object obj) {
		return (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XSpreadsheet interface
	 */
	public static XSpreadsheet XSpreadsheet(Object obj) {
		return (XSpreadsheet) UnoRuntime.queryInterface(XSpreadsheet.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XSpreadsheetDocument interface
	 */
	public static XSpreadsheetDocument XSpreadsheetDocument(Object obj) {
		return (XSpreadsheetDocument) UnoRuntime.queryInterface(XSpreadsheetDocument.class, obj);
	}

	// --------------------------------------------------
	// Table com.sun.star.table.*
	// --------------------------------------------------

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTableChart interface
	 */
	public static XTableChart XTableChart(Object obj) {
		return (XTableChart) UnoRuntime.queryInterface(XTableChart.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XCellRange interface
	 */
	public static XCellRange XCellRange(Object obj) {
		return (XCellRange) UnoRuntime.queryInterface(XCellRange.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTableCharts interface
	 */
	public static XTableCharts XTableCharts(Object obj) {
		return (XTableCharts) UnoRuntime.queryInterface(XTableCharts.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTableChartsSupplier interface
	 */
	public static XTableChartsSupplier XTableChartsSupplier(Object obj) {
		return (XTableChartsSupplier) UnoRuntime.queryInterface(XTableChartsSupplier.class, obj);
	}

	// --------------------------------------------------
	// Text com.sun.star.text.*
	// --------------------------------------------------

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XText interface
	 */
	public static XText XText(Object obj) {
		return (XText) UnoRuntime.queryInterface(XText.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextTable interface
	 */
	public static XTextTable XTextTable(Object obj) {
		return (XTextTable) UnoRuntime.queryInterface(XTextTable.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextTableCursor interface
	 */
	public static XTextTableCursor XTextTableCursor(Object obj) {
		return (XTextTableCursor) UnoRuntime.queryInterface(XTextTableCursor.class, obj);
	}
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextContent interface
	 */
	public static XTextContent XTextContent(Object obj) {
		return (XTextContent) UnoRuntime.queryInterface(XTextContent.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextDocument interface
	 */
	public static XTextDocument XTextDocument(Object obj) {
		return (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, obj);
	}
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextRange interface
	 */
	public static XTextRange XTextRange(Object obj) {
		return (XTextRange) UnoRuntime.queryInterface(XTextRange.class, obj);
	}
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextCursor interface
	 */
	public static XTextCursor XTextCursor(Object obj) {
		return (XTextCursor) UnoRuntime.queryInterface(XTextCursor.class, obj);
	}
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextRangeCompare interface
	 */
	public static XTextRangeCompare XTextRangeCompare(Object obj) {
		return (XTextRangeCompare) UnoRuntime.queryInterface(XTextRangeCompare.class, obj);
	}
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextViewCursorSupplier interface
	 */
	public static XTextViewCursorSupplier XTextViewCursorSupplier(Object obj) {
		return (XTextViewCursorSupplier) UnoRuntime.queryInterface(XTextViewCursorSupplier.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextTablesSupplier interface
	 */
	public static XTextTablesSupplier XTextTablesSupplier(Object obj) {
		return (XTextTablesSupplier) UnoRuntime.queryInterface(XTextTablesSupplier.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextSectionsSupplier interface
	 */
	public static XTextSectionsSupplier XTextSectionsSupplier(Object obj) {
		return (XTextSectionsSupplier) UnoRuntime.queryInterface(XTextSectionsSupplier.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XTextFieldsSupplier interface
	 */
	public static XTextFieldsSupplier XTextFieldsSupplier(Object obj) {
		return (XTextFieldsSupplier) UnoRuntime.queryInterface(XTextFieldsSupplier.class, obj);
	}

	// --------------------------------------------------
	// Uno com.sun.star.uno.*
	// --------------------------------------------------
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XComponentContext interface
	 */
	public static XComponentContext XComponentContext(Object obj) {
		return (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, obj);
	}
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XNamingService interface
	 */
	public static XNamingService XNamingService(Object obj) {
		return (XNamingService) UnoRuntime.queryInterface(XNamingService.class, obj);
	}
	
	// --------------------------------------------------
	// Util com.sun.star.util.*
	// --------------------------------------------------

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XCloseable interface
	 */
	public static XCloseable XCloseable(Object obj) {
		return (XCloseable) UnoRuntime.queryInterface(XCloseable.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XReplaceable interface
	 */
	public static XReplaceable XReplaceable(Object obj) {
		return (XReplaceable) UnoRuntime.queryInterface(XReplaceable.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XSearchable interface
	 */
	public static XSearchable XSearchable(Object obj) {
		return (XSearchable) UnoRuntime.queryInterface(XSearchable.class, obj);
	}
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XRefreshable interface
	 */
	public static XRefreshable XRefreshable(Object obj) {
		return (XRefreshable) UnoRuntime.queryInterface(XRefreshable.class, obj);
	}
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XMergeable interface
	 */
	public static XMergeable XMergeable(Object obj) {
		return (XMergeable) UnoRuntime.queryInterface(XMergeable.class, obj);
	}
	
	/**
	 * @param obj The object implementing this interface
	 * @return Returns XNumberFormatsSupplier interface
	 */
	public static XNumberFormatsSupplier XNumberFormatsSupplier(Object obj) {
		return (XNumberFormatsSupplier) UnoRuntime.queryInterface(XNumberFormatsSupplier.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XNumberFormatTypes interface
	 */
	public static XNumberFormatTypes XNumberFormatTypes(Object obj) {
		return (XNumberFormatTypes) UnoRuntime.queryInterface(XNumberFormatTypes.class, obj);
	}

	// --------------------------------------------------
	// View com.sun.star.view.*
	// --------------------------------------------------

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XPrintable interface
	 */
	public static XPrintable XPrintable(Object obj) {
		return (XPrintable) UnoRuntime.queryInterface(XPrintable.class, obj);
	}

	/**
	 * @param obj The object implementing this interface
	 * @return Returns XSelectionSupplier interface
	 */
	public static XSelectionSupplier XSelectionSupplier(Object obj) {
		return (XSelectionSupplier) UnoRuntime.queryInterface(XSelectionSupplier.class, obj);
	}
}