package org.mmisw.ont.client;

import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.client.util.HttpUtil;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * Utilities based on the "Ont" URI resolution service.
 * 
 * @author Carlos Rueda
 */
class OntClientUtil {
	private OntClientUtil() {}
	
	private static final Log log = LogFactory.getLog(OntClientUtil.class);
	
	private static String aquaportalRestUrl = null;
	
	
	private static OntClientConfiguration _config() {
		OntClientConfiguration config = IOntClient.Manager.getOntClient().getConfiguration();
		return config;
	}
	
	/**
	 * Resolves a URI against the "Ont" service.
	 * Note that instead of trying to use the uri as given (for auto resolution), the
	 * "uri" parameter is used. This, in particular, allows to resolve ontologies in the
	 * database that have an external namespace (re-hosting case).
	 * 
	 * @param uriModel  The URI of the desired ontology.
	 * @param version   Desired version; can be null
	 * @param acceptEntries list of accept header entries
	 * @return The response of the "ont" service.
	 * @throws Exception
	 */
	static String resolveOntologyUri(String uriModel, String version, String... acceptEntries) throws Exception {
		
		String ontServiceUrl = _config().getOntServiceUrl();
		uriModel = URLEncoder.encode(uriModel, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?uri=" +uriModel;
		if ( version != null ) {
			ontServiceRequest += "&version=" +version;
		}
		if ( log.isDebugEnabled() ) {
			log.debug("resolveOntologyUri: ontServiceRequest=" +ontServiceRequest);
		}
		String str = HttpUtil.getAsString(ontServiceRequest, acceptEntries);
		
		return str;
	}

	
	/**
	 * Gets the aquaportal rest url.
	 * Upon a successful retrieval, the value is cached for subsequent calls of this method. 
	 * In case of exception, a subsequent call will attempt the retrieval again.
	 * 
	 * @return the aquaportal rest url.
	 * @throws Exception If any error happens during the request or if a MalformedURLException
	 *         happens while trying to parse the retrieved value.
	 */
	static String getAquaportalRestUrl() throws Exception {
		if ( aquaportalRestUrl == null ) {
			String ontServiceUrl = _config().getOntServiceUrl();
			String ontServiceRequest = ontServiceUrl + "?_aqrest";
			String test = HttpUtil.getAsString(ontServiceRequest);
			test = test.trim();
			new URL(test); // in case of MalformedURLException
			aquaportalRestUrl = test;
		}
		return aquaportalRestUrl;
	}
	
	
	/**
	 * Determines if the URI corresponds to a registered ontology.
	 * 
	 * @param uriModel  The URI of the desired ontology.
	 * @return true iff the ontology is registered.
	 * @throws Exception
	 */
	static boolean isRegisteredOntologyUri(String uriModel) throws Exception {
		/*
		 * Use the "oe" (ontology exists) request.
		 */
		String ontServiceUrl = _config().getOntServiceUrl();
		uriModel = URLEncoder.encode(uriModel, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?oe=" +uriModel;
		if (log.isDebugEnabled()) {
			log.debug("isRegisteredOntologyUri: ontServiceRequest = " + ontServiceRequest);
		}
		int statusCode = HttpUtil.httpGetStatusCode(ontServiceRequest);
		if (log.isDebugEnabled()) {
			log.debug("isRegisteredOntologyUri: ontServiceRequest = " + ontServiceRequest + 
					" => statusCode=" +statusCode);
		}
		
		return statusCode == HttpStatus.SC_OK;
	}
	
	
	/**
	 * Loads a model resolving the URI against the "ont" service.
	 * 
	 * @param uriModel  The URI of the desired ontology.
	 * @param version   Desired version; can be null
	 * @return
	 * @throws Exception
	 */
	static OntModel retrieveModel(String uriModel, String version) throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("retrieveModel: uri: " +uriModel+ "  version: " +version);
		}
		
		String str = resolveOntologyUri(uriModel, version, "application/rdf+xml");
		
		OntModel model = createDefaultOntModel();
		uriModel = JenaUtil2.removeTrailingFragment(uriModel);
		
		StringReader sr = new StringReader(str);
		
		try {
			model.read(sr, uriModel);
		}
		catch (Exception e) {
			log.warn("Error reading model from retrieved contents: [" +str+ "]", e);
			throw e;
		}
		
		return model;
	}
	
	private static OntModel createDefaultOntModel() {
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		OntDocumentManager docMang = new OntDocumentManager();
		spec.setDocumentManager(docMang);
		OntModel model = ModelFactory.createOntologyModel(spec, null);
		// removeNotNeccesaryNamespaces(model);

		return model;
	}

	
	
	/**
	 * Determines whether the given URI corresponds to an ontology or term
	 * served or that can be served by the "ont" service.  
	 * It uses the "ontServiceUrl" property value provided
	 * by the configuration object.
	 * 
	 * <p>
	 * A similar method exists in the "ont" module.
	 * 
	 * @param uri The URI to check.
	 * @return if the given URI has the Ont service URL as a prefix (ignoring case).
	 * @throws IllegalStateException if the configuration object's getOntServiceUrl() method
	 *         returns null
	 */
	static boolean isOntResolvableUri(String uri) {
		String ontServiceUrl = _config().getOntServiceUrl();
		 if ( ontServiceUrl == null ) {
			 throw new IllegalStateException("_config().getOntServiceUrl() returned null");
		 }
		 return uri.toLowerCase().startsWith(ontServiceUrl.toLowerCase());
	}
	
	
	/**
	 * Runs a SPARQL query using the the "Ont" service.
	 * 
	 * <p>
	 * TODO this is a very basic dispatch-- pending a better mechanism, includig paging
	 * 
	 * @param query  The query
	 * @param format Desired format ("form" parameter). Can be null.
	 * @param acceptEntries list of accept header entries
	 * @return The response of the "ont" service.
	 * @throws Exception
	 */
	static String runSparqlQuery(String query, boolean infer, 
			String format, String... acceptEntries) throws Exception {
		
		String ontServiceUrl = _config().getOntServiceUrl();
		query = URLEncoder.encode(query, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?infer=" +infer+ "&sparql=" +query;
		if ( format != null ) {
			ontServiceRequest += "&form=" +format;
		}
		String str = HttpUtil.getAsString(ontServiceRequest, acceptEntries);
		
		return str;
	}


	
	/**
	 * Runs a SPARQL query using the the "Ont" service.
	 * 
	 * <p>
	 * TODO this is a very basic dispatch-- pending a better mechanism, includig paging
	 * 
	 * @param endPoint
	 * @param query
	 * @param format
	 * @param acceptEntries
	 * @return The response of the "ont" service.
	 * @throws Exception
	 */
	static String runSparqlQuery(
			String endPoint,
			String query,
			boolean infer, 
			String format,
			String... acceptEntries
			
	) throws Exception {
		
		String ontServiceUrl = endPoint == null ? _config().getOntServiceUrl() : endPoint;
		
		query = URLEncoder.encode(query, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?infer=" +infer+ "&sparql=" +query;
		if ( format != null ) {
			ontServiceRequest += "&form=" +format;
		}
		String str = HttpUtil.getAsString(ontServiceRequest, acceptEntries);
		
		return str;
	}


	
	/**
	 * Makes the request to load the given ontology in the graph maintained by the
	 * "ont" service.
	 * 
	 * @param uriModel  The URI of the desired ontlogy.
	 * @param graphId   desired grapth to be updated. Can be null meaning the main graph.
	 * @return true iff "ont" responds with an OK return code.
	 * @throws Exception
	 */
	static boolean loadOntologyInGraph(String uriModel, String graphId) throws Exception {
		
		String ontServiceUrl = _config().getOntServiceUrl();
		uriModel = URLEncoder.encode(uriModel, "UTF-8");
		String ontServiceRequest;
		if ( graphId != null && graphId.trim().length() > 0 ) {
			ontServiceRequest = ontServiceUrl + "?_gi=" +graphId.trim()+ "&_lo=" +uriModel;
		}
		else {
			ontServiceRequest = ontServiceUrl + "?_lo=" +uriModel;
		}
		int statusCode = HttpUtil.httpGetStatusCode(ontServiceRequest);
		
		return statusCode == HttpStatus.SC_OK;
	}
	
	

	/**
	 * Makes the request to get user info.
	 * 
	 * @param username  username
	 * @return info properties
	 * @throws Exception
	 */
	static Map<String,String> getUserInfo(String username) throws Exception {
		
		String ontServiceUrl = _config().getOntServiceUrl();
		username = URLEncoder.encode(username, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?_usri=" +username;
		String str = HttpUtil.getAsString(ontServiceRequest);

		Map<String,String> props = new LinkedHashMap<String,String>();
		
		String[] lines = str.split("\n");
		for (String string : lines) {
			String[] toks = string.split(":", 2);
			if ( toks.length == 2 ) {
				props.put(toks[0].trim(), toks[1].trim());
			}
		}
		
		return props;
	}
	
	
	/**
	 * Gets the (synthetic) users RDF from the "Ont" service.
	 * @throws Exception
	 */
	static String getUsersRdf() throws Exception {
		
		String ontServiceUrl = _config().getOntServiceUrl();
		String ontServiceRequest = ontServiceUrl + "?_usrsrdf";
		String str = HttpUtil.getAsString(ontServiceRequest);
		
		return str;
	}


	/**
	 * Makes the request to the "ont" service to remove the given ontology.
	 * 
	 * @param ontUri  The URI of the desired ontlogy.
	 * @param version the version to be removed.
	 * @return true iff "ont" responds with an OK return code.
	 * @throws Exception
	 */
	static boolean unregisterOntology(String ontUri, String version) throws Exception {
		
		String ontServiceUrl = _config().getOntServiceUrl();
		ontUri = URLEncoder.encode(ontUri, "UTF-8");
		version = URLEncoder.encode(version, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?_unr=" +ontUri+ "&version=" +version;
		int statusCode = HttpUtil.httpGetStatusCode(ontServiceRequest);
		
		return statusCode == HttpStatus.SC_OK;
	}
}

