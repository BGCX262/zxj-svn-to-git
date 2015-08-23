package org.zxframework.util;

/**
 * Dom Element helper
 */
public class DomElementUtil {
	
	private org.w3c.dom.Element element;
	
	/**
	 * Hide constructor.
	 *
	 */
	private DomElementUtil() {
		super();
	}
	
	/**
	 * @param element The dom element.
	 */
	public DomElementUtil(org.w3c.dom.Element element) {
		this.element = element;
	}
	
	/**
	 * @return Returns the textual content directly held under this element as a string.
	 */
	public String getText() {
		String getText = "";
		org.w3c.dom.Node node = element.getFirstChild();
		if (node != null) {
			getText = node.getNodeValue();
		}
		return getText;
	}

	/**
	 * Returns the textual content of the named child element, or null if
     * there's no such child. This method is a convenience because calling
     * <code>getChild().getText()</code> can throw a NullPointerException.
     * 
	 * @param child the name of the child
	 * @return Returns the textual content of the named child element
	 */
	public String getChildText(String child) {
		String getChildText = null;
		org.w3c.dom.NodeList nodeList = element.getElementsByTagName(child);
		if (nodeList != null) {
			org.w3c.dom.Node node = nodeList.item(0);
			if (nodeList.item(0) instanceof org.w3c.dom.Element) {
				DomElementUtil element = new DomElementUtil((org.w3c.dom.Element)node);
				getChildText = element.getText();
			}
		}
		return getChildText;
	}

	/**
	 * This returns the attribute value for the attribute with the given name.
	 * 
	 * @param attr name of the attribute whose value to be returned
	 * @return the named attribute's value, or null if no such attribute
	 */
	public String getAttributeValue(String attr) {
		return element.getAttribute(attr);
	}
}