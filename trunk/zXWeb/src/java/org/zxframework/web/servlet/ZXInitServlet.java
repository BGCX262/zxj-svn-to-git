/*
 * Created on Aug 4, 2004
 * $Id: ZXInitServlet.java,v 1.1.2.8 2006/07/17 14:02:37 mike Exp $
 */
package org.zxframework.web.servlet;

import java.io.File;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.zxframework.Environment;
import org.zxframework.ZX;
import org.zxframework.zXType;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

import org.zxframework.util.TestUtil;
import org.zxframework.web.DrctrFHPageflow;
import org.zxframework.web.HierMenu;
import org.zxframework.web.PFDescriptor;
import org.zxframework.web.PFQS;
import org.zxframework.web.PageBuilder;
import org.zxframework.web.Pageflow;

/**
 * ZX Init Servlet :
 * 
 * This servlet is used to preinitialise some of the pageflows and the main ZX object.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ZXInitServlet extends HttpServlet {
    
    private static Log log = LogFactory.getLog(ZXInitServlet.class);
    
    /** 
     * @see javax.servlet.GenericServlet#init()
     **/
    public void init() throws ServletException {
        super.init();
        
        ZX objZX = null;
        try {
            Context context = new InitialContext();
            String strConfigFile = "";
            
            /**
             * Try to get the path of the zX configuration file : 
             */
            try {
                strConfigFile = (String)context.lookup(Environment.WEB_CONFIG);
            } catch (Exception e) {
                log.error("Failed to get zX config filename via web config JNDI Name: " + Environment.WEB_CONFIG, e);
            }
            /**
             * We must have failed to get the web settings for zX config xml.
             */
            if (strConfigFile == null) {
                strConfigFile = TestUtil.getCfgPath();
                log.warn("Using Testing path : " + strConfigFile);
            }
            
            objZX = new ZX(strConfigFile);
            
            /**
             * Only init when in production mode : 
             */
            if (objZX.getRunMode().pos == zXType.runMode.rmProduction.pos 
                || objZX.getRunMode().pos == zXType.runMode.rmDevelCache.pos) {
                
                PageBuilder objPage = new PageBuilder();
                Pageflow objPageFlow = new Pageflow();
                /**
                 * Initialise querystring object
                 */
                objPageFlow.setQs(new PFQS());
                objPageFlow.getQs().init(objPageFlow);
                
                /**
                 * Add in the director handler :
                 */
                objPageFlow.setPageflowDirectorFuncHandler(new DrctrFHPageflow());
                objPageFlow.getPageflowDirectorFuncHandler().setPageflow(objPageFlow);
                objZX.getDirectorHandler().registerFH("pageflow", objPageFlow.getPageflowDirectorFuncHandler());
                
                /**
                 * It is wise to pre cache all of the more complex pageflows and most used pageflows like ZXDM.
                 */
                PFDescriptor objPFDesc = new PFDescriptor();
                objPFDesc.init(objPageFlow, objZX.fullPathName(objZX.getPageflowDir())  + File.separatorChar + "zXDM.xml", true);
                objZX.setCachedValue("zXDM", objPFDesc);
               	
                objPFDesc = new PFDescriptor();
                objPFDesc.init(objPageFlow, objZX.fullPathName(objZX.getPageflowDir())  + File.separatorChar + "zXFK.xml", true);
                objZX.setCachedValue("zXFK", objPFDesc);
                
                /**
                 * Preload the menu and cache it.
                 */
    			HierMenu objMenu = objPage.getHierMenu();
     			objMenu.readMenuFile("mainMenu.xml");
                objZX.setCachedValue("menu", objMenu);
            }
            
        } catch (Exception e) {
            if (objZX != null) {
                objZX.log.error(e);
            } else {
                log.error("Failed to init servlet", e);
            }
            
        } finally {
            // Make sure we clean up !
            if (objZX != null) {
                objZX.cleanup();    
            }
        }
    }
}