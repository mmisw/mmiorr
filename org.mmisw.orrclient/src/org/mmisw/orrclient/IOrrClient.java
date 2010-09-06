package org.mmisw.orrclient;

import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.core.OrrClientImpl;
import org.mmisw.orrclient.gwt.client.rpc.AppInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.CreateUpdateUserAccountResult;
import org.mmisw.orrclient.gwt.client.rpc.EntityInfo;
import org.mmisw.orrclient.gwt.client.rpc.InternalOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.orrclient.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.ResetPasswordResult;
import org.mmisw.orrclient.gwt.client.rpc.ResolveUriResult;
import org.mmisw.orrclient.gwt.client.rpc.SparqlQueryInfo;
import org.mmisw.orrclient.gwt.client.rpc.SparqlQueryResult;
import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.UnregisterOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.UserInfoResult;
import org.mmisw.orrclient.gwt.client.rpc.vine.RelationInfo;

/**
 * Interface to the OrrClient library.
 * 
 * @author Carlos Rueda
 */
public interface IOrrClient {
	
	/**
	 * OrrClient library manager.
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
		 */
		public static IOrrClient init(OrrClientConfiguration config) {
			return OrrClientImpl.init(config);
		}
		
		/**
		 * Returns the library interface object.
		 * @return the library interface object created by {@link #init(OrrClientConfiguration)}
		 *         or null if such method has not been called.
		 */
		public static IOrrClient getOrrClient() {
			return OrrClientImpl.getInstance();
		}
	}
	
	/**
	 * Gets a read-only version of the configuration object given at creation time.
	 * Any setXXX call on this configuration object will throw UnsupportedOperationException.
	 * If you need to change the configuration for the OrrClient library, you will need to 
	 * re-create the OrrClient object.
	 * @return a read-only version of the configuration given at creation time.
	 */
	public OrrClientConfiguration getConfiguration();
	
	
	/**
	 * Gets basic application info.
	 */
	AppInfo getAppInfo();
	
	/**
	 * Gets all (latest versions of the) ontologies in the registry.
	 * 
	 * <p>
	 * Note that the all corresponding URIs (getUri()) of the main entries in the list 
	 * will be in UNversioned form. The elements in the priorVersion lists will be in
	 * versioned form.  
	 * Note that the priorVersion list WILL also contain the main (most recent) element, but in 
	 * versioned form (that is, getUri() for that element will be versioned.
	 * 
	 * @param includeAllVersions true to include all versions for each element
	 * @return
	 * @throws Exception
	 */
	List<RegisteredOntologyInfo> getAllOntologies(boolean includeAllVersions) throws Exception;
	
	/**
	 * Gets an ontology from the registry.
	 */
	RegisteredOntologyInfo getOntologyInfo(String ontologyUri);

	/**
	 * Obtains the entities of an ontology.
	 * Note: No specific version is requested, but the latest version should be obtained.
	 * 
	 * @param registeredOntologyInfo
	 * @return ontologyInfo
	 */
	RegisteredOntologyInfo getEntities(RegisteredOntologyInfo registeredOntologyInfo);
	
	
	
	/**
	 * Resolves a URI against the registry
	 * 
	 * Upon success, this result is either a {@link RegisteredOntologyInfo} or an {@link EntityInfo}.
	 * 
	 * Both members will be null if the URI could not be resolved (not found).
	 */
	public ResolveUriResult resolveUri(String uri);
	
	
	
	public MetadataBaseInfo getMetadataBaseInfo(boolean includeVersion, 
			String resourceTypeClassUri, String authorityClassUri);
	
	
	
	/**
	 * Gets both the metadata and the entities.
	 * 
	 * @param registeredOntologyInfo
	 * @param version   Desired version; can be null
	 * @return
	 */
	public RegisteredOntologyInfo getOntologyContents(RegisteredOntologyInfo registeredOntologyInfo, String version);
	
	
	/**
	 * Prepares a new ontology with the given information. The resulting ontology will be stored
	 * in a temporary space. A subsequent call to {@link #registerOntology(CreateOntologyResult, LoginResult)}
	 * will register the ontology.
	 * 
	 * @param createOntologyInfo
	 * @return
	 */
	public CreateOntologyResult createOntology(CreateOntologyInfo createOntologyInfo) ;

	
	public RegisterOntologyResult registerOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult) ;
	
	
	/**
	 * 
	 * @param fileType         Type of the file associated with uploadResults
	 * @param uploadResults    Result from the UploadServlet
	 * @param includeContents  get also metadata and data?
	 * @param includeRdf       include the text of the RDF
	 * @return
	 */
	public TempOntologyInfo getTempOntologyInfo(
			String fileType, String uploadResults, 
			boolean includeContents, boolean includeRdf
	);
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// VINE:
	
	/**
	 * Gets the default list of RelationInfo's. This list is to be used for the creation of
	 * brand new mapping ontologies
	 */
	List<RelationInfo> getDefaultVineRelationInfos();
	
	// :VINE
	////////////////////////////////////////////////////////////////////////////////////////////
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// Search:
	
	SparqlQueryResult runSparqlQuery(SparqlQueryInfo query);
	
	
	
	// login
	
	LoginResult authenticateUser(String userName, String userPassword);
	ResetPasswordResult resetUserPassword(String username);
	UserInfoResult getUserInfo(String username);
	CreateUpdateUserAccountResult createUpdateUserAccount(Map<String,String> values);

	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// OOI CI semantic prototype
	
	/**
	 * Registers an ontology file "directly," meaning with no changes at all in the contents of the file.
	 * 
	 * @param loginResult               user info
	 * @param registeredOntologyInfo    current registration, if any, to create new version
	 * @param createOntologyInfo        info for the new registration
	 * @param graphId                   desired grapth to be updated
	 * @return
	 */
	public RegisterOntologyResult registerOntologyDirectly(
			LoginResult loginResult, 
			RegisteredOntologyInfo registeredOntologyInfo,
			CreateOntologyInfo createOntologyInfo, 
			String graphId
	);
	
	
	/**
	 * Prepares the users instantiation ontology.
	 * 
	 * @param loginResult Only the administrator can perform this operation.
	 * @return result of the operation.
	 */
	public InternalOntologyResult prepareUsersOntology(LoginResult loginResult);
	
	/**
	 * Creates and registers the groups instantiation ontology.
	 * @param loginResult Only the administrator can perform this operation.
	 * @return result of the operation.
	 */
	public InternalOntologyResult createGroupsOntology(LoginResult loginResult);
	
	public UnregisterOntologyResult unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo oi);
}
