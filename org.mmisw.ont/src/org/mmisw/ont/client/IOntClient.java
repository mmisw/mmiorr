package org.mmisw.ont.client;

import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;


/**
 * Interface to the OntClient library.
 * 
 * This library offers operations to interact with a deployment of the "Ont" service.
 * Initialization requires the URL of the Ont service to interact with.
 * 
 * @author Carlos Rueda
 */
public interface IOntClient {
	
	/**
	 * OntClient library manager.
	 * @author Carlos Rueda
	 */
	public static class Manager {
		/**
		 * Initializes the library.
		 * This must be called before any other library operation.
		 * 
		 * @param config 
		 *         The configuration for the library. Note that a copy is made internally.
		 *         Subsequent changes to the given configuration will not have any effect. If you
		 *         need to change any configuration parameters, the library will need to be
		 *         initialized again.
		 *         Pass a null reference to use a default configuration.
		 *          
		 * @return A library interface object.
		 * @throws Exception  if an error occurs
		 */
		public static IOntClient init(OntClientConfiguration config) throws Exception {
			return OntClientImpl.init(config);
		}
		
		/**
		 * Returns the library interface object.
		 * @return the library interface object created by {@link #init(OntClientConfiguration)}
		 *         or null if such method has not been called.
		 */
		public static IOntClient getOntClient() {
			return OntClientImpl.getInstance();
		}
	}
	
	/**
	 * Gets a read-only version of the configuration object given at creation time.
	 * Any setXXX call on this configuration object will throw UnsupportedOperationException.
	 * If you need to change the configuration for the OntClient library, you will need to 
	 * re-create the OntClient object.
	 * @return a read-only version of the configuration given at creation time.
	 */
	public OntClientConfiguration getConfiguration();
	
	
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
	public String resolveOntologyUri(String uriModel, String version, String... acceptEntries) throws Exception;

	/**
	 * Determines if the URI corresponds to a registered ontology.
	 * 
	 * @param uriModel  The URI of the desired ontlogy.
	 * @param acceptEntries list of accept header entries
	 * @return true iff the ontology is registered.
	 * @throws Exception
	 */
	public boolean isRegisteredOntologyUri(String uriModel, String... acceptEntries) throws Exception ;


	/**
	 * Loads a model resolving the URI against the "ont" service.
	 * 
	 * @param uriModel  The URI of the desired ontology.
	 * @param version   Desired version; can be null
	 * @return
	 * @throws Exception
	 */
	public OntModel retrieveModel(String uriModel, String version) throws Exception;
	
	
	/**
	 * Determines whether the given URI corresponds to an ontology or term
	 * served or that can be served by the "ont" service.  
	 * It uses the "ontServiceUrl" property value provided
	 * by the configuration object.
	 * 
	 * @param uri The URI to check.
	 * @return if the given URI has the Ont service URL as a prefix (ignoring case).
	 * @throws IllegalStateException if the configuration object's getOntServiceUrl() method
	 *         returns null
	 */
	public boolean isOntResolvableUri(String uri) ;
	
	
	/**
	 * Runs a SPARQL query using the the "Ont" service.
	 * 
	 * <p>
	 * TODO better dispatch mechanism, includig paging
	 * 
	 * @param query  The query
	 * @param format Desired format ("form" parameter). Can be null.
	 * @param acceptEntries list of accept header entries
	 * @return The response of the "ont" service.
	 * @throws Exception
	 */
	public String runSparqlQuery(String query, String format, String... acceptEntries) throws Exception ;

	
	/**
	 * Runs a SPARQL query using the the "Ont" service.
	 * 
	 * <p>
	 * TODO better dispatch mechanism, includig paging
	 * 
	 * @param endPoint
	 * @param query
	 * @param format
	 * @param acceptEntries
	 * @return The response of the "ont" service.
	 * @throws Exception
	 */
	public String runSparqlQuery(
			String endPoint,
			String query,
			String format,
			String... acceptEntries
			
	) throws Exception;
	
	
	/**
	 * Makes the request to load the given ontology in the graph maintained by the
	 * "ont" service.
	 * 
	 * @param uriModel  The URI of the desired ontlogy.
	 * @param graphId   desired grapth to be updated. Can be null meaning the main graph.
	 * @return true iff "ont" responds with an OK return code.
	 * @throws Exception
	 */
	public boolean loadOntologyInGraph(String uriModel, String graphId) throws Exception ;
	
	

	/**
	 * Makes the request to get user info.
	 * 
	 * @param username  username
	 * @return info properties
	 * @throws Exception
	 */
	public Map<String,String> getUserInfo(String username) throws Exception ;
	
	
	
	/**
	 * Gets the (synthetic) users RDF from the "Ont" service.
	 * @throws Exception
	 */
	public String getUsersRdf() throws Exception;
	
	
	/**
	 * Makes the request to the "ont" service to remove the given ontology.
	 * 
	 * @param ontUri  The URI of the desired ontlogy.
	 * @param version the version to be removed.
	 * @return true iff "ont" responds with an OK return code.
	 * @throws Exception
	 */
	public boolean unregisterOntology(String ontUri, String version) throws Exception ;
	
	
	
	
	/**
	 * Authenticates a user returning a Session object.
	 * @return
	 * @throws Exception 
	 */
	public SignInResult getSession(String userName, String userPassword) throws Exception;
	
	
	/**
	 * Creates a new user account or updates an existing one.
	 * 
	 * <p>
	 * TODO document me!
	 * 
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public SignInResult createUpdateUserAccount(Map<String,String> values)
	throws Exception;
	
}
