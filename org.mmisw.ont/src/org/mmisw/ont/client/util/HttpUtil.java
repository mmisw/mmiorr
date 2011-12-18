package org.mmisw.ont.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

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
	
	
	
	/////////////////////////////////////////////////////////
	/*
	 * NEW 
	 */
	
	public static class HttpResponse {
		public final int statusCode;
		public final String statusLine;
		public final String contentType;
		public final String body;

		HttpResponse(int statusCode, String statusLine, String contentType,
				String body) {
			super();
			this.statusCode = statusCode;
			this.statusLine = statusLine;
			this.contentType = contentType;
			this.body = body;
		}

		public String toString() {
			return String
					.format(
							"HttpResponse{statusCode=%d; statusLine=%s; contentType=%s; body=%s}",
							statusCode, statusLine, contentType, body);
		}
	}

	/**
	 * Makes an HTTP GET request.
	 * 
	 * @param urlRequest
	 * @param acceptEntries
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse httpGet(String urlRequest,
			String... acceptEntries) throws Exception {
		return httpGet(urlRequest, Integer.MAX_VALUE, acceptEntries);
	}

	/**
	 * Makes an HTTP GET request.
	 * 
	 * @param urlRequest
	 * @param maxlen
	 *            Max lenght for the body of the response.
	 * @param acceptEntries
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse httpGet(String urlRequest, int maxlen,
			String... acceptEntries) throws Exception {

		HttpClient client = new HttpClient();
		GetMethod meth = new GetMethod(urlRequest);
		for (String acceptEntry : acceptEntries) {
			meth.addRequestHeader("accept", acceptEntry);
		}

		try {
			client.executeMethod(meth);

			int statusCode;
			String statusLine;
			String contentType;
			String body;

			statusCode = meth.getStatusCode();
			statusLine = meth.getStatusLine().toString();
			contentType = meth.getResponseHeader("Content-Type").toString();
			body = meth.getResponseBodyAsString(maxlen);

			return new HttpResponse(statusCode, statusLine, contentType, body);
		}
		finally {
			meth.releaseConnection();
		}
	}
	
	/**
	 * Makes an HTTP POST request.
	 * 
	 * @param urlRequest
	 * @param vars
	 * @param acceptEntries
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse httpPost(String urlRequest, Map<String,String> vars,
			String... acceptEntries) throws Exception {

		PostMethod meth = new PostMethod(urlRequest);
		
		List<Part> partList = new ArrayList<Part>();
		
		for (String acceptEntry : acceptEntries) {
			meth.addRequestHeader("accept", acceptEntry);
		}
		
		for (Entry<String, String> entry : vars.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			partList.add(new StringPart(key, val));
		}
		
		// now, perform the POST:
		Part[] parts = partList.toArray(new Part[partList.size()]);
		meth.setRequestEntity(new MultipartRequestEntity(parts, meth.getParams()));
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

		try {
			client.executeMethod(meth);

			int statusCode;
			String statusLine;
			String contentType;
			String body;

			statusCode = meth.getStatusCode();
			statusLine = meth.getStatusLine().toString();
			contentType = meth.getResponseHeader("Content-Type").toString();
			body = meth.getResponseBodyAsString();

			return new HttpResponse(statusCode, statusLine, contentType, body);
		}
		finally {
			meth.releaseConnection();
		}
	}

}
