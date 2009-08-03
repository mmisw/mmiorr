package org.mmisw.iserver.core.util;

import java.net.URLEncoder;

import org.mmisw.iserver.core.Config;


/**
 * Utilities based on the "Ont" URI resolution service.
 * 
 * @author Carlos Rueda
 */
public class OntServiceUtil {
	
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
		
		String ontServiceUrl = Config.Prop.ONT_SERVICE_URL.getValue();
		uriModel = URLEncoder.encode(uriModel, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?uri=" +uriModel;
		String str = HttpUtil.getAsString(ontServiceRequest, acceptEntries);
		
		return str;
	}

}

