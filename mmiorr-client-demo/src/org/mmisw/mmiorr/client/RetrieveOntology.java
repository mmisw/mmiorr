package org.mmisw.mmiorr.client;

import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * A program demonstrating the access to a registered ontology in 
 * a desired format from the MMI Ontology Registry and Repository.
 * 
 * @author Carlos Rueda
 */
public class RetrieveOntology {
	static final String URI_RESOLVER = "http://mmisw.org/ont/";

	/**
	 * See build.xml
	 */
	public static void main(String[] args) throws Exception {
		int arg = 0;
		String ontologyUri = args[arg++];
		String format = args[arg++];
		String version = arg < args.length ? args[arg++] : null;
		
		String contents = retrieve(ontologyUri, version, format);
		System.out.println(contents);
	}
	
	
	
	public static String retrieve(String ontologyUri, String version, String format) throws Exception {
		
		String ontServiceUrl = URI_RESOLVER;
		ontologyUri = URLEncoder.encode(ontologyUri, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?uri=" +ontologyUri;
		ontServiceRequest += "&form=" +format;
		if ( version != null ) {
			ontServiceRequest += "&version=" +version;
		}
		System.out.println("HTTP GET: " +ontServiceRequest);
		
		HttpClient client = new HttpClient();
	    GetMethod meth = new GetMethod(ontServiceRequest);
	    try {
	        client.executeMethod(meth);

	        if (meth.getStatusCode() == HttpStatus.SC_OK) {
	            return meth.getResponseBodyAsString(Integer.MAX_VALUE);
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
