/*
 * Created on 09-Feb-2005, by Michael Brewer
 * $Id: MainMenuRuleSet.java,v 1.1.2.9 2006/07/17 13:55:22 mike Exp $
 */
package org.zxframework.web;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.RuleSet;

import org.xml.sax.Attributes;

import org.zxframework.ZXObject;
import org.zxframework.util.StringUtil;

/**
 * The Ruleset for parsing HierMenu example file.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class MainMenuRuleSet  extends ZXObject implements RuleSet {
    
	//------------------------ Members
	
    private Digester digester;
    
	//------------------------ Constants
	
    private static final Class LABEL_CLAZZ = org.zxframework.Label.class;
    private static final Class LABELCOL_CLAZZ = org.zxframework.LabelCollection.class;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public MainMenuRuleSet() {
    	super();
    }
    
    //------------------------ RuleSet implemented methods
    
    /**
     * @see org.apache.commons.digester.RuleSet#getNamespaceURI()
     */
    public String getNamespaceURI() {
        return null;
    }

    /**
     * @see org.apache.commons.digester.RuleSet#addRuleInstances(org.apache.commons.digester.Digester)
     */
    public void addRuleInstances(Digester digester) {
        this.digester = digester;
        
        digester.setValidating(false);
        digester.setRules(new ExtendedBaseRules());
        
        parseLabels("mainmenu/title");
        digester.addSetNext("mainmenu/title", "setTitle");
        
        /**
         * DGS19MAR2004: New properties for lineselect, indentation and notextindentation
         */
        digester.addBeanPropertySetter("mainmenu/lineselect");
        digester.addBeanPropertySetter("mainmenu/indentation");
        digester.addBeanPropertySetter("mainmenu/notextindentation");
        
        /**
         * v1.5:95 DGS26APR2006: New properties to override images
         */
        digester.addBeanPropertySetter("mainmenu/menuitem");
        digester.addBeanPropertySetter("mainmenu/menuitemover");
        digester.addBeanPropertySetter("mainmenu/menuopen");
        digester.addBeanPropertySetter("mainmenu/menuclosed");
        
        parseItems("mainmenu/items");
        digester.addSetNext("mainmenu/items", "setItems");

    }
    
    //------------------------ Parsing methods
    
    /**
     * @param pstrPattern The XPath to the label defintion.
     */
    private void parseLabels(String pstrPattern) {
        digester.addObjectCreate(pstrPattern, LABELCOL_CLAZZ);
        
        digester.addObjectCreate(pstrPattern + "/*", LABEL_CLAZZ);
        digester.addRule(pstrPattern + "/*", new LabelRule());
        
        digester.addSetNext(pstrPattern + "/*", "put");
    }
    
    /**
     * @param pstrPattern The XPath to the menu item defintion.
     */
    private void parseItems(String pstrPattern) {
        parseItems(pstrPattern, 1);
    }
    
    /**
     * Parse menu items.
     * 
     * <pre>
     * 
     * NOTE : Limited to 10 levels deep.
     * </pre>
     * 
     * @param pstrPattern The XPath to the menu item defintion.
     * @param depth The depth of menu.
     */
    private void parseItems(String pstrPattern, int depth) {
        if (depth < 10) {
            depth++;
            
            digester.addObjectCreate(pstrPattern, ArrayList.class);
            
            digester.addObjectCreate(pstrPattern + "/item", HierMenu.class);
            digester.addBeanPropertySetter(pstrPattern + "/item/name");
            
            /**
             * DGS19MAR2004: New property to start this submenu as open
             */
            digester.addBeanPropertySetter(pstrPattern + "/item/startopen");
            
            parseLabels(pstrPattern + "/item/title");
            digester.addSetNext(pstrPattern + "/item/title", "setTitle");
            
            digester.addObjectCreate(pstrPattern + "/item/groups", ArrayList.class);
            digester.addCallMethod(pstrPattern + "/item/groups/group", "add", 0, new Class[]{String.class});
            digester.addSetNext(pstrPattern + "/item/groups", "setGroups");
            
            // Hack as this is a nested structure.
            parseItems(pstrPattern + "/item/items", depth);
            digester.addSetNext(pstrPattern + "/item/items", "setItems");
            
            MenuURLFactory menuFactory = new MenuURLFactory();
            digester.addFactoryCreate(pstrPattern + "/item/url", menuFactory, true);
            digester.addBeanPropertySetter(pstrPattern + "/item/url");
            digester.addSetNext(pstrPattern + "/item/url", "setUrl");
            
            /**
             * v1.5:95 DGS26APR2006: New properties to override images
             */
            digester.addBeanPropertySetter(pstrPattern + "/item/menuitem");
            digester.addBeanPropertySetter(pstrPattern + "/item/menuitemover");
            digester.addBeanPropertySetter(pstrPattern + "/item/menuopen");
            digester.addBeanPropertySetter(pstrPattern + "/item/menuclosed");
            
            digester.addSetNext(pstrPattern + "/item", "add");
        }
    }
    
    /**
     * A factory of creating MenuURL suitable for the Digester.
     */
    final class MenuURLFactory extends AbstractObjectCreationFactory implements Serializable {
        /** The default constructor **/
        protected MenuURLFactory() { super(); }
        
        /**
         * @see org.apache.commons.digester.ObjectCreationFactory#createObject(org.xml.sax.Attributes)
         */
        public Object createObject(Attributes pobjAattributes) {
            MenuURL  createObject = new MenuURL();
            
            createObject.setAppendsession(StringUtil.booleanValue(pobjAattributes.getValue("appendsession")));
            createObject.setNewwindow(StringUtil.booleanValue(pobjAattributes.getValue("newwindow")));
            createObject.setDirector(StringUtil.booleanValue(pobjAattributes.getValue("isdirector")));
            
            return createObject;
            
        }
    }
}