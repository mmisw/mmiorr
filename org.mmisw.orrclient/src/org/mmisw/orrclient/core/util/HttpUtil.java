package org.mmisw.orrclient.core.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * HTTP client utilities.
 * 
 * @author Carlos Rueda
 */
public class HttpUtil {
	public static String getAsString(String uri, String... acceptEntries) throws Exception {
//		System.out.println("getAsString. uri= " +uri);
		return getAsString(uri, Integer.MAX_VALUE, acceptEntries);
	}
	
	private static String getAsString(String uri, int maxlen, String... acceptEntries) throws Exception {
		HttpClient client = new HttpClient();
	    GetMethod meth = new GetMethod(uri);
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

	/** Executes an HTTP GET request.
	 * @returns the returned status code. 
	 */
	public static int httpGetStatusCode(String uri, String... acceptEntries) throws Exception {
		HttpClient client = new HttpClient();
	    GetMethod meth = new GetMethod(uri);
	    for ( String acceptEntry : acceptEntries ) {
	    	meth.addRequestHeader("accept", acceptEntry);
	    }
	    try {
	        client.executeMethod(meth);
	        return meth.getStatusCode();
	    }
	    finally {
	        meth.releaseConnection();
	    }
	}

}
