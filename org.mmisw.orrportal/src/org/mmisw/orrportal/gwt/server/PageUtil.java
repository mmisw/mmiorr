package org.mmisw.orrportal.gwt.server;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;

/**
 * Some utilities for the main page, index.jsp.
 * 
 * @author Carlos Rueda
 */
public class PageUtil {
	
	/**
	 * Returns the contents of the given file if it can be read and omitting all lines
	 * starting with hash (#). If any of these lines has the form
	 * <code># ignore: yes</code>, then the whole file is ignored and "" is returned.
	 * 
	 * <p>
	 * Upon any exception, the exception is logged out 
	 * (using {@link org.apache.commons.logging.Log#warn(Object, Throwable)}) 
	 * and "" is returned.
	 * 
	 * @param filename Name of the file
	 * @return the contents of the given file, or "" if the file does not exist, 
	 *         cannot be read, has a line with <code># ignore: yes</code>, or any exception occurs.
	 */
	public static String include(String filename)  {
		try {
			File file = new File(filename);
			if ( file.canRead() ) {
				StringBuffer contents = new StringBuffer();
				List<?> xx = IOUtils.readLines(new FileInputStream(file), "utf-8");
				for ( Object obj : xx ) {
					String str = obj.toString().trim();
					if ( str.startsWith("#") ) {
						if ( str.matches("#+\\s*ignore\\s*(:|=)\\s*yes.*") ) {
							return "";
						}
					}
					else {
						contents.append(str + "\n");
					}
				}
				return contents.toString();
			}
		}
		catch (Throwable e) {
			LogFactory.getLog(PageUtil.class).warn("Exception while reading include file " +filename, e);
		}
		return "";
	}
}
