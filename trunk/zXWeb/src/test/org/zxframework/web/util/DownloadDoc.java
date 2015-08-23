/*
 * Created on 17-Feb-2005
 */
package org.zxframework.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Michael Brewer
 */
public class DownloadDoc {
    
    /**
     * @param pstFilename
     * @param response
     */
    public static void downloadDoc (String pstFilename, HttpServletResponse response) {
        OutputStream w = null;
        try {
            
            File file = new File(pstFilename);
            byte[] buffer = new byte[8192];
            
            response.reset();
            
            response.setContentType("application/msword");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            
            w = response.getOutputStream();
            FileInputStream fileInputStream = new FileInputStream(file);
            while (fileInputStream.available() > 0) {
                int cnt = fileInputStream.read(buffer);
                w.write(buffer, 0, cnt);
            }
            
        } catch(Exception e) {
        	/**
        	 * Ignore errors
        	 */
        } finally {
            try {
            	if (w != null) {
                    w.close();
            	}
            } catch (Exception e) {
            	/**
            	 * Ignore errors
            	 */
            }
        }
    }
}