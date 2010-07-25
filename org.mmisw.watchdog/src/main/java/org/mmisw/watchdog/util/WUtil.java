package org.mmisw.watchdog.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

/**
 * Misc utilities.
 * 
 * @author Carlos Rueda
 */
public class WUtil {
	
	/**
	 * Returns the contents of the given URL (http or file).
	 * @param inputUrl 
	 * @return contents
	 * @throws Exception if protocol is not http* or file
	 */
	public static String getAsString(URL inputUrl) throws Exception {
		String protocol = inputUrl.getProtocol().toLowerCase();
		if ( protocol.startsWith("http") ) {
			return httpGetAsString(inputUrl.toString(), Integer.MAX_VALUE);
		}
		else if ( protocol.startsWith("file") ) {
			File file = new File(inputUrl.getPath());
			return IOUtils.toString(new FileInputStream(file));
		}
		throw new Exception("Only http* and file protocols supported");
	}

	/**
	 * Return the contents of the given URL.
	 * @param url
	 * @param maxlen
	 * @param acceptEntries
	 * @return
	 * @throws Exception
	 */
	public static String httpGetAsString(String url, int maxlen, String... acceptEntries) throws Exception {
		HttpClient client = new HttpClient();
	    GetMethod meth = new GetMethod(url);
	    for ( String acceptEntry : acceptEntries ) {
	    	meth.addRequestHeader("accept", acceptEntry);
	    }
	    try {
	        client.executeMethod(meth);

	        if (meth.getStatusCode() == HttpStatus.SC_OK) {
	            return meth.getResponseBodyAsString(maxlen);
	        }
	        else {
	          throw new Exception("Unexpected failure: " + meth.getStatusLine().toString());
	        }
	    }
	    finally {
	        meth.releaseConnection();
	    }
	}

}
