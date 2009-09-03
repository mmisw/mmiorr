package org.mmisw.iserver.core.util;

import java.io.StringReader;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpStatus;
import org.mmisw.iserver.core.Config;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * Utilities based on the "Ont" URI resolution service.
 * 
 * @author Carlos Rueda
 */
public class OntServiceUtil {
	private OntServiceUtil() {}
	
	
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
	public static String resolveOntologyUri(String uriModel, String version, String... acceptEntries) throws Exception {
		
		String ontServiceUrl = Config.Prop.ONT_SERVICE_URL.getValue();
		uriModel = URLEncoder.encode(uriModel, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?uri=" +uriModel;
		if ( version != null ) {
			ontServiceRequest += "&version=" +version;
		}
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
	
	
	/**
	 * Loads a model resolving the URI against the "ont" service.
	 * 
	 * @param uriModel  The URI of the desired ontology.
	 * @param version   Desired version; can be null
	 * @return
	 * @throws Exception
	 */
	public static OntModel retrieveModel(String uriModel, String version) throws Exception {
		
		String str = resolveOntologyUri(uriModel, version, "application/rdf+xml");
		
		OntModel model = createDefaultOntModel();
		uriModel = JenaUtil2.removeTrailingFragment(uriModel);
		
		StringReader sr = new StringReader(str);
		
		model.read(sr, uriModel);
		
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
	 * It uses the "ont.service.url" property value provided
	 * by the configuration object.
	 * 
	 * <p>
	 * A similar method exists in the "ont" module.
	 * 
	 * @param uri The URI to check.
	 * @return if the given URI has the Ont service URL as a prefix (ignoring case).
	 * @throws IllegalStateException if the configuration object returns a null value for the 
	 *         {@link OntConfig.Prop.ONT_SERVICE_URL} parameter.
	 */
	public static boolean isOntResolvableUri(String uri) {
		 String ontServiceUrl = Config.Prop.ONT_SERVICE_URL.getValue();
		 if ( ontServiceUrl == null ) {
			 throw new IllegalStateException("Config.Prop.ONT_SERVICE_URL.getValue() returned null");
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
	public static String runSparqlQuery(String query, String format, String... acceptEntries) throws Exception {
		
		String ontServiceUrl = Config.Prop.ONT_SERVICE_URL.getValue();
		query = URLEncoder.encode(query, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?sparql=" +query;
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
	 * @return true iff "ont" responds with an OK return code.
	 * @throws Exception
	 */
	public static boolean loadOntologyInGraph(String uriModel) throws Exception {
		
		String ontServiceUrl = Config.Prop.ONT_SERVICE_URL.getValue();
		uriModel = URLEncoder.encode(uriModel, "UTF-8");
		String ontServiceRequest = ontServiceUrl + "?_lo=" +uriModel;
		int statusCode = HttpUtil.httpGetStatusCode(ontServiceRequest);
		
		return statusCode == HttpStatus.SC_OK;
	}
	
	


}

