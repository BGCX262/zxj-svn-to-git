/*
 * Created on Jun 14, 2005
 * $Id: WebSpellChecker.java,v 1.1.2.3 2006/07/17 14:01:24 mike Exp $
 */
package org.zxframework.web.util;

/**
 * Interface for building a web spell checker.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public interface WebSpellChecker {
	
    /**
     * Set the JavaScript variable to the submitted text.
     * Textinputs is an array, each element corresponding to the (url-encoded)
     * value of the text control submitted for spell-checking.
     * 
     * @return Returns the JavaScript variable to the submitted text
     */
    public String getCheckerHeader();
    
    /**
     * Get the list of misspelled words. Put the results in the javascript words array
     * for each misspelled word, get suggestions and put in the javascript suggs array
     * 
     * @return Returns a list of misspelled words
     */
    public String getCheckerResults();

	/**
	 * Get a list of supported languages.
	 * 
	 * @param selected The selected language
	 * @return Returns a option list of supported languages.
	 */
	public String listSupportedLanguages(String selected);
}