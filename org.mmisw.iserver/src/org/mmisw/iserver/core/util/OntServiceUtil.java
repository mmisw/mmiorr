package org.mmisw.iserver.core.util;

import java.net.URLEncoder;


/**
 * Utilities based on the "Ont" URI resolution service.
 * 
 * @author Carlos Rueda
 */
public class OntServiceUtil {
	
	// TODO use a param
	private static final String ONT = "http://mmisw.org/ont";
	
	/**
	 * Returns the URL of the "Ont" service.
	 * @return the URL of the "Ont" service.
	 */
	// TODO perhaps move this to a configuration related class
	public static String getOntServiceUrl() {
		return ONT;
	}
	
	
	/**
	 * Resolves a URI against the "Ont" service.
	 * Note that instead of trying to use the uri as given (for auto resolution), the
	 * "uri" parameter is used. This, in particular, allows to resolve ontologies in the
	 * database that have an external namespace (re-hosting case).
	 * 
	 * @param uriModel  The URI of the desired ontlogy.
	 * @param acceptEntries list of accept header entries
	 * @return The response of the "ont" service.
	 * @throws Exception
	 */
	public static String resolveOntologyUri(String uriModel, String... acceptEntries) throws Exception {
		
		uriModel = URLEncoder.encode(uriModel, "UTF-8");
		String ontServiceRequest = ONT + "?uri=" +uriModel;
		String str = HttpUtil.getAsString(ontServiceRequest, acceptEntries);
		
		return str;
	}

}

