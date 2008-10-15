package org.mmisw.voc2rdf.gwt.server;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * misc utilities
 * 
 * @author Carlos Rueda
 *
 */
public class Util {
	public static String getAsString(String uri) throws Exception {
		System.out.println("getAsString. uri= " +uri);
		return getAsString(uri, Integer.MAX_VALUE);
	}
	
	public static String getAsString(String uri, int maxlen) throws Exception {
		HttpClient client = new HttpClient();
	    GetMethod meth = new GetMethod(uri);
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
