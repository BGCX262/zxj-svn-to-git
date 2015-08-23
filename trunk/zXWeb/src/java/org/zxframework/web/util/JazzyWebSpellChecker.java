/*
 * Created on Sep 2, 2004
 * $Id: JazzyWebSpellChecker.java,v 1.1.2.3 2006/07/17 14:00:10 mike Exp $
 */
package org.zxframework.web.util;

import java.io.File;
import java.util.List;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

/**
 * Used to spellcheck web forms.
 * 
 * PRO :
 * Light wieght for single languages.
 * Easy to install and use.
 * 
 * CONS :
 * Not as powerfull as Openoffice.
 * Not good for multiligual.
 * 
 * 
 * TODO : Add zX config entries for Spelling Dictionaries
 * TODO : Rename to JazzySpellChecker
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class JazzyWebSpellChecker extends AbstractSpellerWebSpellChecker implements SpellCheckListener {
    
    //------------------------ Members
	
    private static final String dictFile = "c:\\tmp\\eng_com.dic";
    
    /** Store handle to dictionary statically. */
    private static SpellDictionary dictionary;
    
    /** Handle to check checker. */
    private SpellChecker spellCheck = null;
    
    //------------------------ Constructors
    
    /**
     * Hide default constructor
     */
    private JazzyWebSpellChecker() {
    	super();
    }
    
    /**
     * JazzyWebSpellChecker constructor. 
     * 
     * @param parrTextInputs An array of text inputs to check
     */
    public JazzyWebSpellChecker(String [] parrTextInputs){
        this.textInputs = parrTextInputs;
    }
    
    /**
     * @param pstrCheck This text to spell check.
     */
    public JazzyWebSpellChecker(String pstrCheck){
        this.textInputs = new String[]{pstrCheck};
    }
    
    //------------------------ AbstractSpellerWebSpellChecker Implemented Methods

    /**
     * @see AbstractSpellerWebSpellChecker#checkSpelling(java.lang.String)
     */
	public void checkSpelling(String pstrCheck) {
        try {
	        if (dictionary == null) {
	            dictionary = new SpellDictionaryHashMap(new File(dictFile));
	        }
	        
	        spellCheck = new SpellChecker(dictionary);
	        spellCheck.addSpellCheckListener(this);
	        
            // Now check the spelling
            spellCheck.checkSpelling(new StringWordTokenizer(pstrCheck));
            
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }
    
    //------------------------ SpellCheckListener Implemented methods
    
    /** 
     * This will be triggered by checkSpelling method call if there is any spelling errors.
     * 
     * @see com.swabunga.spell.event.SpellCheckListener#spellingError(com.swabunga.spell.event.SpellCheckEvent)
     **/
    public void spellingError(SpellCheckEvent event){
        List list =  event.getSuggestions();
        
        try {
        	
            populateWordsElement(event.getInvalidWord());
            populateSuggsElement(list);
            
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
        
        this.errorIndex++;
    }
}