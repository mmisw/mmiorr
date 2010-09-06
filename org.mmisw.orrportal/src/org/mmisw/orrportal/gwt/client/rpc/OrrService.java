package org.mmisw.orrportal.gwt.client.rpc;

import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.rpc.AppInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.CreateUpdateUserAccountResult;
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
import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface to interact with the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface OrrService extends RemoteService {
	
	/**
	 * Gets basic application info.
	 */
	AppInfo getAppInfo();

	/**
	 * Gets some general properties
	 */
	public PortalBaseInfo getPortalBaseInfo();

	
	/**
	 * Gets general information about the metadata for ontologies. 
	 * @param includeVersion
	 * @return
	 */
	public MetadataBaseInfo getMetadataBaseInfo(boolean includeVersion);


	/**
	 * Gets all (latest versions of the) ontologies in the registry.
	 * @param includePriorVersions
	 * @return
	 */
	public List<RegisteredOntologyInfo> getAllOntologies(boolean includePriorVersions);
	

	/**
	 * Refreshes the options of the given attribute.
	 * @return the given argument.
	 */
	AttrDef refreshOptions(AttrDef attrDef);
	

	/**
	 * Both members of the result will be null if the URI could not be resolved (not found). 
	 */
	public ResolveUriResult resolveUri(String uri);
	
	
	
	public RegisteredOntologyInfo getOntologyContents(RegisteredOntologyInfo ontologyInfo, String version);
	
	
	/**
	 * Creates an ontology.
	 * 
	 * @param createOntologyInfo
	 * @return result of the creation
	 */
	public CreateOntologyResult createOntology(CreateOntologyInfo createOntologyInfo) ;



	/**
	 * Registers an ontology.
	 * 
	 * @param createOntologyResult Indicates the ontology to be registered.
	 * @param loginResult
	 * @return Result of the registration
	 */
	public RegisterOntologyResult registerOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult);
	

	
	
	/**
	 * Gets ontology info from a pre-loaded file.
	 */
	public TempOntologyInfo getTempOntologyInfo(
			String fileType, String uploadResults, boolean includeContents,
			boolean includeRdf);



	////////////////////////////////////////////////////////////////////////////////////////////
	// VINE:
	
	/**
	 * Gets the default list of RelationInfo's.
	 */
	public List<RelationInfo> getDefaultVineRelationInfos();
	
	// :VINE
	////////////////////////////////////////////////////////////////////////////////////////////

	
	// Search:
	
	public SparqlQueryResult runSparqlQuery(SparqlQueryInfo query);

	
	// login/user info
	
	/**
	 * Authenticates a user.
	 * 
	 * @param userName
	 * @param userPassword
	 * @return
	 */
	public LoginResult authenticateUser(String userName, String userPassword);

	public ResetPasswordResult resetUserPassword(String username);
	public UserInfoResult getUserInfo(String username);
	public CreateUpdateUserAccountResult createUpdateUserAccount(Map<String,String> values);
	
	public InternalOntologyResult prepareUsersOntology(LoginResult loginResult);
	public InternalOntologyResult createGroupsOntology(LoginResult loginResult);
	
	public UnregisterOntologyResult unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo oi);
	
	
	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////

	//
	// TODO REMOVE THESE OLD OPERATIONS
	//
	
	/**
	 * Gets ontology info from an ontology that can be resolved by the Ont service
	 * (ie., from the MMI registry).
	 */
	OntologyInfoPre getOntologyInfoFromRegistry(String ontologyUri);

	/**
	 * Gets ontology info from a pre-loaded file.
	 */
	OntologyInfoPre getOntologyInfoFromPreLoaded(String uploadResults);

	/**
	 * Gets ontology info from a file located in the server.
	 * The main "client" for this service is Voc2Rdf
	 */
	OntologyInfoPre getOntologyInfoFromFileOnServer(String fullPath);

	/**
	 * Reviews the pre-loaded model with the associated new values.
	 */
	ReviewResult_Old review(OntologyInfoPre ontologyInfoPre, LoginResult loginResult_Old);

	/**
	 * Uploads a reviewed model to the MMI Registry.
	 */
	UploadResult upload(ReviewResult_Old reviewResult_Old, LoginResult loginResult_Old);
	

	/**
	 * Gets the base information (metadata attribute definitions).
	 */
	// TODO REMOVE -- UNUSED
	MetadataBaseInfo getBaseInfo(Map<String, String> params);
	
	
}
