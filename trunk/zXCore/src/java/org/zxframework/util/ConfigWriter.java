/*
 * Created on Feb 16, 2004 by Michael Brewer
 * $Id: ConfigWriter.java,v 1.1.2.6 2006/07/17 16:15:03 mike Exp $
 */
package org.zxframework.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.zxframework.zXType;
import org.zxframework.exception.NestableException;

/**
 * A utility class to allow people to read and write values from the zX app config file.
 * 
 * NOTE : This only works on JDK 1.4+
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class ConfigWriter {

    private String cfgFile;
    private ConfigEntity parentConfig;
    private Document cfgDoc;
    
    /**
     * Hide default constructor.
     */
    private ConfigWriter() {
    	super();
    }
    
    /**
     * Creates a config writer object.
     * 
     * @param pstrCfgFile The full filename of the root zX app config file.
     */
    public ConfigWriter(String pstrCfgFile) {
        this.cfgFile = pstrCfgFile;
    }
    
    /**
     * @return Returns the parentConfig.
     */
    public ConfigEntity getParentConfig() {
        return parentConfig;
    }
    /**
     * @param parentConfig The parentConfig to set.
     */
    public void setParentConfig(ConfigEntity parentConfig) {
        this.parentConfig = parentConfig;
    }
    
    /**
     * @throws Exception Thrown if init fails
     */
    public void init() throws Exception {
        
        /**
         * A custom entity resolver to builds up a list of external entities references.
         */
        class CustomEntityResolver implements EntityResolver{
            
            private ArrayList entityURIS;
            
            /**
             * Default constructor.
             */
            public CustomEntityResolver() {
                this.entityURIS = new ArrayList();
            }
            
            /**
             * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
             */
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                if (systemId != null) {
                    entityURIS.add(systemId);
                }
                return null;
            }
            
            /**
             * @return Returns an ArrayList(String) of file uri.
             */
            public ArrayList getEntityURIS() {
                return entityURIS;
            }
        }
        
        //-------------------------------------- Get the external entities
        
        /**
         * Top config file to read.
         */
        File file = new File(this.cfgFile);
        if (!file.isFile()) {
            throw new Exception("Failed to get file");
        }
        
        /**
         * Used to get the external entities filenames.
         */
        EntityResolver objEntityResolver = new CustomEntityResolver(); 
        SAXBuilder sax = new SAXBuilder();
        sax.setValidation(false);
        sax.setEntityResolver(objEntityResolver);
        sax.setExpandEntities(true);
        
        // Set the root document for searching.
        Document doc = sax.build(file);
        this.cfgDoc = doc;
        
        //-------------------------------------- Get the external entities
        
        //------------------------------------------ Build the ConfigEntities
        
        /**
         * Load xml for change :
         */
        sax = new SAXBuilder();
        sax.setExpandEntities(false);
        doc = sax.build(file);
        
        ConfigEntity objParentEntity = new ConfigEntity();
        objParentEntity.setFile(file);
        objParentEntity.setDocument(doc);
        
        ConfigEntity objChildEntity;
        
        ArrayList colExternalURIs = ((CustomEntityResolver)objEntityResolver).getEntityURIS();
        int intExternal = colExternalURIs.size();
        for (int i = 0; i < intExternal; i++) {
            URI uri = new URI((String)colExternalURIs.get(i));
            file = new File(uri);
            if (!file.isFile()) {
                throw new Exception("Failed to get file");
            }
            sax = new SAXBuilder();
            sax.setExpandEntities(false);
            sax.setValidation(false);
            doc = sax.build(new StringReader("<zX>" + getContents(file) + "</zX>"));
            
            objChildEntity = new ConfigEntity();
            objChildEntity.setFile(file);
            objChildEntity.setDocument(doc);
            
            objParentEntity.getChildern().add(objChildEntity);
        }
        
        setParentConfig(objParentEntity);
        
        //------------------------------------------ Build the ConfigEntities
    }
    
    /**
     * Update the configuration files.
     * 
     * @return Return true if one of the config files where updated.
     * @throws Exception Thrown if updateConfig fails
     */
    public boolean updateConfig() throws Exception {
        boolean updateConfig = false;
        
        /**
         * Update the main config file.
         */
        XMLOutputter out = new XMLOutputter();
        
        if (getParentConfig().isDirty()) {
            setContents(getParentConfig().getFile(), out.outputString(getParentConfig().getDocument()));
            updateConfig = true;
        }
        
        ConfigEntity objChildEntity;
        int intChildEntities = getParentConfig().getChildern().size();
        for (int i = 0; i < intChildEntities; i++) {
            objChildEntity = (ConfigEntity)getParentConfig().getChildern().get(i);
            
            if (objChildEntity.isDirty()) {
                /**
                 * Update this file.
                 */
                out = new XMLOutputter();
                setContents(objChildEntity.getFile(), out.outputString(objChildEntity.getDocument().getRootElement()).replaceAll("<zX>", "").replaceAll("</zX>", ""));
                updateConfig = true;
            }
        }
        return updateConfig;
    }
    
    
    /**
     * Update a dom object value.
     * @param name The xpath to the element you want to update.
     * @param value The value you want to set it to.
     * @return Returns true if the config settings was updated.
     */
    public int setConfigValue(String name, String value) {
        /**
         * Update a specific value :
         */
        int setConfigValue = setConfigValue(getParentConfig(), name, value);
        if (setConfigValue == zXType.rc.rcError.pos) {
            /**
             * Go through the external entities.
             */
            ConfigEntity objChildEntity;
            int intChildEntities = getParentConfig().getChildern().size();
            for (int i = 0; i < intChildEntities; i++) {            
                objChildEntity = (ConfigEntity)getParentConfig().getChildern().get(i);
                
                setConfigValue = setConfigValue(objChildEntity, name, value);
                if (setConfigValue == zXType.rc.rcOK.pos || setConfigValue == zXType.rc.rcWarning.pos) return setConfigValue;
            }
        }
        return setConfigValue;
    }
    
    /**
     * Try to update the value of a specific dom element.
     * 
     * <pre>
     * 
     * rcOK = Updated the value
     * rcError = Not such value was found.
     * rcWarning = Value found but identical.
     * </pre>
     * 
     * @param configEntity The document to try to update.
     * @param name XPath of the element you want to update.
     * @param value The value you want to set it to.
     * @return Returns the return code of setConfigValue.
     */
    private int setConfigValue(ConfigEntity configEntity, String name, String value){
        Object valueObj = null;
        try {
            valueObj = XPath.selectSingleNode(configEntity.getDocument(), name);
        } catch (JDOMException e) { 
        	return zXType.rc.rcError.pos;
        }
        
        if (valueObj != null) {
            if (valueObj instanceof Element) {
                Element objElement = (Element)valueObj;
                if (!value.equals(objElement.getText())) {
                    objElement.setText(value);
                    configEntity.setDirty(true);
                    return zXType.rc.rcOK.pos;
                }
                return zXType.rc.rcWarning.pos;
                
            } else if (valueObj instanceof Attribute) {
                Attribute objAttr = (Attribute)valueObj;
                if (!value.equals(objAttr.getValue())) {
                    objAttr.setValue(value);
                    configEntity.setDirty(true);
                    return zXType.rc.rcOK.pos;
                }
                return zXType.rc.rcWarning.pos;
                
            } else if (valueObj instanceof Text) {
                Text objText = (Text)valueObj;
                if (!value.equals(objText.getValue())) {
                    objText.setText(value);
                    configEntity.setDirty(true);
                    return zXType.rc.rcOK.pos;
                }
                return zXType.rc.rcWarning.pos;
                
            }
        }
        return zXType.rc.rcError.pos;
    }
    
    /**
     * Returns the requested config value from the zXConfigFile.
     * If the xpath selects a attribute it will return the value 
     * surrounded in quotes.
     * 
     * @param xpath Xpath uri of the config you want to select. 
     * @return Returns the requested value, if the element does not exist null is returned.
     * @throws Exception Thrown if configValue failes
     */
    public String getConfigValue(String xpath) throws Exception {
        String configValue= null;
        
        Object valueObj = null;
        try {
            valueObj = XPath.selectSingleNode(this.cfgDoc, xpath);
        } catch (JDOMException e) {
            throw new NestableException("Xpath failed to select a node : " + xpath, e);
        }
        
        /**
         * We only want the string value out : 
         */
        if (valueObj instanceof Element) {
            configValue = ((Element) valueObj).getText();
        } else if (valueObj instanceof Text) {
            configValue = ((Text) valueObj).getText();
        } else if (valueObj instanceof Attribute) {
            configValue = ((Attribute) valueObj).getValue();
        } else if (valueObj instanceof CDATA) {
            configValue = ((CDATA) valueObj).getText();
        }
        
        return configValue;
    }
    
    
    /**
     * @param pobjFile File to override.
     * @param pstrContents The contents to write to the file.
     * @throws Exception Thrown if setContents fails
     */
    private static void setContents(File pobjFile, String pstrContents) throws Exception {
        if (!pobjFile.exists()) throw new Exception ("File does not exist: " + pobjFile);
        if (!pobjFile.isFile()) throw new Exception("Should not be a directory: " + pobjFile);
        if (!pobjFile.canWrite()) throw new Exception("File cannot be written: " + pobjFile);
        
        Writer output = null;
        try {
            output = new BufferedWriter( new FileWriter(pobjFile) );
            output.write( pstrContents );
        } finally {
            if (output != null) output.close();
        }
    }    
    
    /**
     * Reads from a file.
     * 
     * @param pobjFile File to get from.
     * @return Returns the contents of a file. 
     * @throws Exception 
     */
    private static String getContents(File pobjFile) throws Exception {
        StringBuffer getContents = new StringBuffer(512);
        BufferedReader input = null;
        try {
          input = new BufferedReader(new FileReader(pobjFile));
          String line = null;
          while ((line = input.readLine()) != null){
            getContents.append(line);
            getContents.append(System.getProperty("line.separator"));
          }
        } finally {
            if (input!= null) input.close();
        }
        return getContents.toString();
    }
    
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        /**
         * Tests the class.
         */
        ConfigWriter objWriter = new ConfigWriter(TestUtil.getCfgPath());
        objWriter.init();
        
        updateStatus(objWriter.setConfigValue("//username", "root"));
        
        // Write to the configuration files.
        if (objWriter.updateConfig()) 
            System.out.println("updateConfig : Updated config file.");
        else 
            System.out.println("updateConfig : Unchanged config file.");
    }
    private static void updateStatus(int rc) {
        if (rc == zXType.rc.rcOK.pos) {
            System.out.println("setConfigValue : Updated.");
        } else if (rc == zXType.rc.rcWarning.pos) {
            System.out.println("setConfigValue : Unchanged.");
        } else {
            System.out.println("setConfigValue : No matching element.");
        }
    }
}