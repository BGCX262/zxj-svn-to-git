/*
 * Created on Jun 15, 2005
 * $Id: OpenofficeWebSpellChecker.java,v 1.1.2.6 2006/07/17 14:00:22 mike Exp $
 */
package org.zxframework.web.util;

import org.zxframework.util.ArrayUtil;
import org.zxframework.util.StringUtil;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.linguistic2.XLinguServiceManager;
import com.sun.star.linguistic2.XSpellAlternatives;
import com.sun.star.linguistic2.XSpellChecker;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Used to spellcheck web forms.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class OpenofficeWebSpellChecker extends AbstractSpellerWebSpellChecker {

	//------------------------ Members

	/** The remote office ocntext */
	private XComponentContext mxRemoteContext = null;

	/** The MultiServiceFactory interface of the Office */
	private XMultiComponentFactory mxRemoteServiceManager = null;

	/** The LinguServiceManager interface */
	private XLinguServiceManager mxLinguSvcMgr = null;

	/** The SpellChecker interface */
	private XSpellChecker mxSpell = null;

	/** Spell check settings */
	private static final PropertyValue[] aEmptyProps = new PropertyValue[0];

	/** Use american english as language... for now. */
	private com.sun.star.lang.Locale aLocale = new com.sun.star.lang.Locale("en", "US", "");
	
	/** Staticly stored list of installed locales for the spellchecker. */
	private static com.sun.star.lang.Locale[] arrLocale;

	//------------------------ Constructors

	/**
	 * Hide default constructor
	 */
	public OpenofficeWebSpellChecker() {
		super();
	}

	/**
	 * @param parrTextInputs An array of text inputs to check.
	 */
	public OpenofficeWebSpellChecker(String[] parrTextInputs) {
		this.textInputs = parrTextInputs;
	}
	
	/**
	 * @param parrTextInputs An array of text inputs to check.
	 * @param language The language to use.
	 */
	public OpenofficeWebSpellChecker(String[] parrTextInputs, String language) {
		this.textInputs = parrTextInputs;
		
		/**
		 * Select the language to use
		 */
		if (language != null) {
			String[] arrString = StringUtil.split("_", language);
			this.aLocale = new com.sun.star.lang.Locale(arrString[0], arrString[1], "");
		}
	}
	
	/**
	 * @param pstrCheck This text to spell check.
	 */
	public OpenofficeWebSpellChecker(String pstrCheck) {
		this.textInputs = new String[] { pstrCheck };
	}
	
	//------------------------ WebSpellChecker Implemented Methods

	/**
	 * @see org.zxframework.web.util.AbstractSpellerWebSpellChecker#checkSpelling(java.lang.String)
	 */
	public void checkSpelling(String pstrCheck) {
		try {
			if (mxSpell == null) {
				connect();
				getLinguSvcMgr();
				getSpell();
			}
			
			pstrCheck = pstrCheck.replaceAll("\n", " ").replaceAll("\r", " ");
			
			String[] aWord = StringUtil.split(" ", pstrCheck);
			for (int j = 0; j < aWord.length; j++) {
				if (StringUtil.len(aWord[j]) > 0) {
					XSpellAlternatives xAlt = mxSpell.spell(aWord[j].trim(),
															aLocale, 
															aEmptyProps);
					/**
					 * We have found a spelling error.
					 */
					if (xAlt != null) {
						populateWordsElement(aWord[j]);
						populateSuggsElement(ArrayUtil.toList(xAlt.getAlternatives()));
						errorIndex++;
					}
				}
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @see org.zxframework.web.util.AbstractSpellerWebSpellChecker#listSupportedLanguages(String)
	 */
	public String listSupportedLanguages(String selected) {
		StringBuffer listSupportedLanguages = new StringBuffer();
		/**
		 * Default to English for now..
		 */
		if (selected == null || selected.length() == 0) {
			selected = "en_GB";
		}
		
		try {
			java.util.Locale jLocale;
			
			/**
			 * Cache the available Locales
			 */
			if (arrLocale == null || arrLocale.length == 0) {
				connect();
				getLinguSvcMgr();
				getSpell();
				arrLocale = mxSpell.getLocales();
			}
			
			for (int i = 0; i < arrLocale.length; i++) {
				com.sun.star.lang.Locale locale = arrLocale[i];
				jLocale = new java.util.Locale(locale.Language, locale.Country);
				
				String sLocale = locale.Language + "_" + locale.Country;
				listSupportedLanguages.append("<option value=\"");
				listSupportedLanguages.append(sLocale);
				listSupportedLanguages.append("\"");
				
				if (sLocale.equalsIgnoreCase(selected)) {
					listSupportedLanguages.append(" SELECTED ");
				}
				
				listSupportedLanguages.append(">");
				listSupportedLanguages.append(jLocale.getDisplayName());
				listSupportedLanguages.append("</option>\n");
			}
			
		} catch (Exception e) {
			/**
			 * Generate a select list non the less.
			 */
			java.util.Locale jLocale = java.util.Locale.getDefault();
			listSupportedLanguages.append("<option value=\"");
			listSupportedLanguages.append(jLocale.getLanguage() + "_" + jLocale.getCountry());
			listSupportedLanguages.append("\" SELECTED>");
			listSupportedLanguages.append(jLocale.getDisplayName());
			listSupportedLanguages.append("</option>\n");
		}
		
		return listSupportedLanguages.toString();
	}
	
	
	//------------------------ Openoffice helper methods.

	/**
	 * Connect to openoffice.
	 * 
	 * @throws Exception
	 */
	private void connect() throws Exception {
		if (mxRemoteContext == null || mxRemoteServiceManager == null) {
			/**
			 * Get the remote office context. If necessary a new office process
			 * is started
			 */
			mxRemoteContext = com.sun.star.comp.helper.Bootstrap.bootstrap();
			mxRemoteServiceManager = mxRemoteContext.getServiceManager();
		}
	}

	/**
	 * Get the LinguServiceManager to be used. 
	 * 
	 * @return Returns true if we can return the XLinguServiceManager
	 * @throws Exception
	 */
	public boolean getLinguSvcMgr() throws Exception {

		if (mxRemoteContext != null && mxRemoteServiceManager != null) {
			Object aObj = mxRemoteServiceManager.createInstanceWithContext("com.sun.star.linguistic2.LinguServiceManager", mxRemoteContext);
			mxLinguSvcMgr = (XLinguServiceManager) UnoRuntime.queryInterface(XLinguServiceManager.class, aObj);
		}
		
		return mxLinguSvcMgr != null;
	}

	/**
	 * Get the SpellChecker to be used.
	 * 
	 * @return Returns true if we have a handle to the Spell object.
	 */
	private boolean getSpell() {
		if (mxLinguSvcMgr != null) {
			mxSpell = mxLinguSvcMgr.getSpellChecker();
		}
		return mxSpell != null;
	}
}