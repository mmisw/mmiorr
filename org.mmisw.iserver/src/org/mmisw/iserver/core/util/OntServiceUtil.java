package org.mmisw.iserver.core.util;

import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpStatus;
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

	/**
	 * Determines if the URI corresponds to a registered ontology.
	 * It uses similar approach {@link  }
	 * 
	 * @param uriModel  The URI of the desired ontlogy.
	 * @param acceptEntries list of accept header entries
	 * @return true iff the ontology is registered.
	 * @throws Exception
	 */
	public static boolean isRegisteredOntologyUri(String uriModel, String... acceptEntries) throws Exception {
		
		String ontServiceUrl = Config.Prop.ONT_SERVICE_URL.getValue();
		uriModel = URLEncoder.encode(uriModel, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?uri=" +uriModel;
		int statusCode = HttpUtil.httpGetStatusCode(ontServiceRequest, acceptEntries);
		
		return statusCode == HttpStatus.SC_OK;
	}
}

