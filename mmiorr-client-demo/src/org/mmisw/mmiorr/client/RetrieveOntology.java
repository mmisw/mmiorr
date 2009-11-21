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

	public static class RetrieveResult {
		public int status;
		public String body;
	}

	/**
	 * See build.xml
	 */
	public static void main(String[] args) throws Exception {
		int arg = 0;
		String ontologyUri = args[arg++];
		String format = args[arg++];
		String version = arg < args.length ? args[arg++] : null;
		
		RetrieveResult result = retrieve(ontologyUri, version, format);
		System.out.println("Response status: " +result.status+ ": " +HttpStatus.getStatusText(result.status));
		System.out.println("Response body:\n" +result.body);
	}
	
	
	
	public static RetrieveResult retrieve(String ontologyUri, String version, String format) throws Exception {
		
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
	    	RetrieveResult result = new RetrieveResult();
	    	result.status = client.executeMethod(meth);

	        if (result.status == HttpStatus.SC_OK) {
	        	result.body = meth.getResponseBodyAsString(Integer.MAX_VALUE);
	        }
	        else {
	        	result.body = meth.getStatusLine().toString();
	        }
	        return result;
	    }
	    finally {
	        meth.releaseConnection();
	    }

	}

}
