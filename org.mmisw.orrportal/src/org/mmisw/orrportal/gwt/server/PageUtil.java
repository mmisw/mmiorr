package org.mmisw.orrportal.gwt.server;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Some utilities for the main page, index.jsp.
 * 
 * @author Carlos Rueda
 */
public class PageUtil {

    private static final Log log = LogFactory.getLog(PageUtil.class);

    private ServletContext servletContext;

    public PageUtil(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String includeHeadTitle() {
        String title = "MMI Ontology Registry and Repository";
        String brandingAppTitle = OrrConfig.instance().brandingAppTitle;
        if (brandingAppTitle != null) {
            title = brandingAppTitle.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        }
        return "<title>" + title + "</title>";
    }

    public String includeTop() {
        return include("_btop.html");
    }

    public String includeBottom() {
        return include("_bbot.html");
    }

    private String include(String filename) {
        File workspaceDir = OrrConfig.instance().workspaceDir;
        File file = new File(workspaceDir, filename);
        String contents = readFile(file);
        if (contents == null) {
            file = new File(servletContext.getRealPath("/" + filename));
            contents = readFile(file);
        }
        return contents == null ? "" : contents;
    }

	/**
	 * Returns the contents of the given file if it can be read and omitting all lines
	 * starting with hash (#). If any of these lines has the form
	 * <code># ignore: yes</code>, then the whole file is ignored and "" is returned.
     * Returns null if any error.
	 */
	private String readFile(File file)  {
        log.debug("Trying to read " + file + " ...");
		try {
            StringBuilder contents = new StringBuilder();
            List<?> xx = IOUtils.readLines(new FileInputStream(file), "utf-8");
            for ( Object obj : xx ) {
                String str = obj.toString().trim();
                if ( str.startsWith("#") ) {
                    if ( str.matches("#+\\s*ignore\\s*(:|=)\\s*yes.*") ) {
                        return "";
                    }
                }
                else {
                    contents.append(str).append("\n");
                }
            }
            return contents.toString();
		}
		catch (Throwable e) {
			log.debug("Exception while reading readFile file " + file + ": " + e.getMessage());
	    	return null;
		}
	}
}
