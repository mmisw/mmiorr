package org.mmisw.mmiorr.client;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
		
		RetrieveResult result = retrieveOntology(ontologyUri, version, format);
		System.out.println("Response status: " +result.status+ ": " +HttpStatus.getStatusText(result.status));
		System.out.println("Response body:\n" +result.body);
	}
	
	
	/**
	 * A helper method to perform an HTTP GET request.
	 */
	public static RetrieveResult httpGet(String ontServiceRequest) throws Exception {
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

	
	/**
	 * Retrieves an ontology.
	 */
	public static RetrieveResult retrieveOntology(String ontologyUri, String version, String format) throws Exception {
		
		String ontServiceUrl = URI_RESOLVER;
		ontologyUri = URLEncoder.encode(ontologyUri, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?uri=" +ontologyUri;
		ontServiceRequest += "&form=" +format;
		if ( version != null ) {
			ontServiceRequest += "&version=" +version;
		}
		
		return httpGet(ontServiceRequest);
	}
	
	/**
	 * Gets the available versions for a fully-hosted ontology
	 * @param ontologyUri URI of the fully-hosted ontology. Note that tis URI
	 *                    is self-resolvable.
	 * @return
	 * @throws Exception
	 */
	public static List<String> getVersions(String ontologyUri) throws Exception {
		
		String ontServiceUrl = ontologyUri;
		String ontServiceRequest = ontServiceUrl + "?_versions";
		
		RetrieveResult result = httpGet(ontServiceRequest);

		List<String> list = new ArrayList<String>();
		for ( String line : result.body.split("\n") ) {
			list.add(line.trim());
		}
		return list;
	}

	/**
	 * Issues a DESCRIBE query for the given URI.
	 */
	public static RetrieveResult describeUri(String uri, String format) throws Exception {
		
		String ontServiceUrl = URI_RESOLVER;
		uri = URLEncoder.encode(uri , "UTF-8");
		// The query is: DESCRIBE <uri>
		// Need to encode < (%3C) and > (%3E)
		String query = "describe%3C" +uri+ "%3E";
		String ontServiceRequest = ontServiceUrl + "?form=" +format+ "&sparql=" +query;
		
		return httpGet(ontServiceRequest);
	}
	

}
