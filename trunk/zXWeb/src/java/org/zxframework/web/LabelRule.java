/*
 * Created on May 25, 2004 by Michael Brewer
 * $Id: LabelRule.java,v 1.1.2.3 2005/06/07 08:51:49 mike Exp $
 */
package org.zxframework.web;

import org.apache.commons.digester.Rule;

import org.zxframework.Label;

/**
 * Used to get the Language from a xml during parsing of the xml file.
 * 
 * <pre>
 * 
 * EG :
 * &lt;label>
 * 		&lt;EN>English label&lt;/EN>
 * &lt;label>
 * </pre>
 * 
 */
final class LabelRule extends Rule {
    /** Default constructor */
    public LabelRule() { super(); }

    /**
     * @see org.apache.commons.digester.Rule#body(java.lang.String, java.lang.String, java.lang.String)
     */
    public void body(String pstrNamespace, String pstrName, String pstrText) throws Exception {
        Label label = (Label) digester.peek();
        label.setLanguage(pstrName);
        label.setLabel(pstrText);
    }
}