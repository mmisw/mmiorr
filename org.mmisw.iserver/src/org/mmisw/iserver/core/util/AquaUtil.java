package org.mmisw.iserver.core.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Some utilities related with aquaportal (our deployment of bioportal).
 * 
 * @author Carlos Rueda
 */
public class AquaUtil {
	
	private static final Log log = LogFactory.getLog(AquaUtil.class);
	
	/**
	 * make sure the fileName ends with ".owl" as the aquaportal back-end seems
	 * to add that fixed extension in some operations (at least in the parse operation)
	 */
	public static String getAquaportalFilename(String uri) throws MalformedURLException {
		// this is to get the filename for the registration
		String fileName = new URL(uri).getPath();
		if ( ! fileName.toLowerCase().endsWith(".owl") ) {
			if ( log.isDebugEnabled() ) {
				log.debug("Setting file extension to .owl per aquaportal requirement.");
			}
			fileName += ".owl";
		}
		return fileName;
	}

}
