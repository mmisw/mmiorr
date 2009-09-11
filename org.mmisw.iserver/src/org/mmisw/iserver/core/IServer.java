package org.mmisw.iserver.core;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.ResetPasswordResult;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryInfo;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryResult;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.UserInfoResult;
import org.mmisw.iserver.gwt.client.rpc.vine.RelationInfo;

/**
 * Interface to get info from the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface IServer {
	
	/**
	 * Gets basic application info.
	 */
	AppInfo getAppInfo();
	
	/**
	 * Gets the list of entities associated with the given ontology. 
	 * @param ontologyUri URI of the desired ontology.
	 * @return list of entities
	 */
	public List<EntityInfo> getEntities(String ontologyUri);
	
	
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
	 * @param uploadResults Result from the UploadServlet
	 * @param includeContents  get also metadata and data?
	 * @param includeRdf       include the text of the RDF
	 * @return
	 */
	public TempOntologyInfo getTempOntologyInfo(String uploadResults, boolean includeContents, boolean includeRdf);
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// VINE:
	
	/**
	 * Gets the default list of RelationInfo's.
	 */
	List<RelationInfo> getVineRelationInfos();
	
	// :VINE
	////////////////////////////////////////////////////////////////////////////////////////////
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// Search:
	
	SparqlQueryResult runSparqlQuery(SparqlQueryInfo query);
	
	
	
	// login
	
	LoginResult authenticateUser(String userName, String userPassword);
	ResetPasswordResult resetUserPassword(String username);
	UserInfoResult getUserInfo(String username);

}
