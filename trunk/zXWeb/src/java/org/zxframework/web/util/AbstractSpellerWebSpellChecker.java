/*
 * Created on Jun 14, 2005
 * $Id: AbstractSpellerWebSpellChecker.java,v 1.1.2.3 2006/07/17 13:59:51 mike Exp $
 */
package org.zxframework.web.util;

import java.net.URLDecoder;
import java.util.List;

import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;

/**
 * Abstract Class that helps people who want to implment a spellchecker for the 
 * Speller interface.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class AbstractSpellerWebSpellChecker implements WebSpellChecker {
	
    //------------------------ Members
	
    /** A String array of text inputs to check. **/
    protected String[] textInputs;
    
    /** The index of current text input being checked. **/
    protected int textInputIndex;
    
    /** The index of the error in the string **/
    protected int errorIndex = 0;
    
    /** The spellcheck results */
    protected StringBuffer out;
    
    //------------------------ WebSpellChecker Implemented Methods
    
    /**
     * Set the JavaScript variable to the submitted text.
     * Textinputs is an array, each element corresponding to the (url-encoded)
     * value of the text control submitted for spell-checking.
     * 
     * @return Returns the JavaScript variable to the submitted text
     */
    public String getCheckerHeader() {
        return getTextIntputsVar();
    }
    
	/**
	 * @see org.zxframework.web.util.WebSpellChecker#getCheckerResults()
	 */
	public String getCheckerResults() {
        this.out = new StringBuffer();
        
        try {
	        for (int i = 0; i < this.textInputs.length; i++) {
	            this.textInputIndex = i;
	            
	            // Print the declaration for this input form to check :
	            populateTextInputsIndexDeclaratons();
	            
	            String check = URLDecoder.decode(this.textInputs[this.textInputIndex], "UTF-8");
	            if (StringUtil.len(check) > 0) {
	            	// Spell check
	            	checkSpelling(check);
	            }
	        }
	        
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
        
        return this.out.toString();
	}
    
	
	/**
	 * @see org.zxframework.web.util.WebSpellChecker#listSupportedLanguages(java.lang.String)
	 */
	public String listSupportedLanguages(String selected) {
		return "";
	}
	
	//------------------------ Abstract Methods.
	
	/**
	 * @param pstrCheck The string you want to spell check.
	 */
	public abstract void checkSpelling(String pstrCheck);
	
    //------------------------ Protected Methods
    
    /**
     * Set the JavaScript variable to the submitted text.
     * Textinputs is an array, each element corresponding to the (url-encoded)
     * value of the text control submitted for spell-checking.
     * 
     * @return Returns the JavaScript variable to the submitted text
     */
    public String getTextIntputsVar() {
        StringBuffer strTextinputs = new StringBuffer(textInputs.length * 42);
        
        for (int i = 0; i < textInputs.length; i++) {
            strTextinputs.append("textinputs[").append(i).append("] = decodeURIComponent('")
            			 .append(textInputs[i]).append("');\n");
        }
        
        return strTextinputs.toString();
    }
    
    /**
     * Make declarations for the text input index
     * 
     * @throws Exception Failed to write to file
     */
    protected void  populateTextInputsIndexDeclaratons() throws Exception {
    	this.out.append("words[").append(this.textInputIndex).append("] = [];\n");
    	this.out.append("suggs[").append(this.textInputIndex).append("] = [];\n");
    }
    
    /**
     * Set an element of the JavaScript 'words' array to a misspelled word.
     * 
     * @param word The word  to print
     * @throws Exception Failed to write to file
     */
    protected void populateWordsElement(String word)  throws Exception {
    	String strCleanWord = StringEscapeUtils.escapeJavaScript(word);
    	
    	this.out.append("words[").append(this.textInputIndex)
    			.append("][").append(this.errorIndex)
    			.append("] = '").append(strCleanWord).append("';\n");
    }
    
    /**
     * Set an element of the JavaScript 'suggs' array to a list of suggestions.
     * 
     * @param suggs A collection (String) of suggestions
     * @throws Exception Failed to write to file
     */
    protected void populateSuggsElement(List suggs) throws Exception {
        this.out.append("suggs[").append(textInputIndex).append("][")
        		.append(this.errorIndex).append("] = [");
        
        int count = suggs.size();
        for (int i = 0; i < count; i++) {
            this.out.append('\'').append(StringEscapeUtils.escapeJavaScript(suggs.get(i).toString())).append('\'');
            
            if (i + 1 < count) {
            	this.out.append(", ");
            }
        }
        
        this.out.append("];\n");
    }
    
    /**
     * Escape single quote.
     * 
     * @param str The string to escape
     * @return Returns an escaped string
     */
    public String escapeQuote(String str ) {
        return StringUtil.replaceAll(str, "/'/", "\\'");
    }
}