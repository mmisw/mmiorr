package org.mmisw.orrportal.gwt.server;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.orrclient.IOrrClient;
import org.mmisw.orrclient.OrrClientFactory;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.CreateUpdateUserAccountResult;
import org.mmisw.orrclient.gwt.client.rpc.ExternalOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.GetAllOntologiesResult;
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
import org.mmisw.orrportal.gwt.client.rpc.PortalBaseInfo;


// TODO remove this class, along with DirectRegistrationServlet

/**
 * A proxy object that creates, configures and interacts with the OrrClient object.
 *
 * @author Carlos Rueda
 * @version $Id$
 */
public class OrrClientProxy  {

	private static OrrClientProxy instance = null;

	/**
	 * Returns the instance of this class, note the it will be null
	 * if {@link #createInstance()} has not been called.
	 * @return
	 */
	public static OrrClientProxy getInstance() {
		return instance;
	}

	/**
	 * Creates the instance of this class, if not already created.
	 * It uses PortalConfig.
	 *
	 * @return the instance
	 * @throws Exception
	 * @throws IllegalStateException if instance already created.
	 */
	public static OrrClientProxy createInstance() throws Exception {
		if ( instance != null ) {
			throw new IllegalStateException(OrrClientProxy.class.getName()+ " instance already created");
		}
		instance = new OrrClientProxy();
		return instance;
	}


	private final Log log = LogFactory.getLog(OrrClientProxy.class);

	private final PortalBaseInfo portalBaseInfo;


	private IOrrClient orrClient;


	/**
	 * Creates this object
	 * @throws Exception
	 */
	private OrrClientProxy() throws Exception {
		log.info("initializing " +getClass().getSimpleName()+ "...");

		portalBaseInfo = _prepareBaseInfo();

		orrClient = OrrClientFactory.init();
	}

	private PortalBaseInfo _prepareBaseInfo() {
		PortalBaseInfo pbi = new PortalBaseInfo();
		pbi.setOntServiceUrl(OrrConfig.instance().ontServiceUrl);
		log.info("portal base info: done.");
		return pbi;
	}



	public GetAllOntologiesResult getAllOntologies(boolean includePriorVersions) {
		return orrClient.getAllOntologies(includePriorVersions);
	}

	public ResolveUriResult resolveUri(String uri) {
		return orrClient.resolveUri(uri);
	}

	/**
	 * Gets an ontology from the registry.
	 */
	public RegisteredOntologyInfo getOntologyInfo(String ontologyUri) {
		return orrClient.getOntologyInfo(ontologyUri);
	}


	public MetadataBaseInfo getMetadataBaseInfo(boolean includeVersion) {
		String resourceTypeClassUri = OrrConfig.instance().resourceTypeClass;
		String authorityClassUri    = OrrConfig.instance().authorityClass;

		return orrClient.getMetadataBaseInfo(includeVersion, resourceTypeClassUri, authorityClassUri);
	}

	public AttrDef refreshOptions(AttrDef attrDef) {
		return orrClient.refreshOptions(attrDef);
	}

	public RegisteredOntologyInfo getOntologyMetadata(RegisteredOntologyInfo ontologyInfo, String version) {
		return orrClient.getOntologyMetadata(ontologyInfo, version);
	}

	public RegisteredOntologyInfo getOntologyContents(RegisteredOntologyInfo ontologyInfo, String version) {
		return orrClient.getOntologyContents(ontologyInfo, version);
	}

	public CreateOntologyResult createOntology(CreateOntologyInfo createOntologyInfo) {
		return orrClient.createOntology(createOntologyInfo);
	}


	public RegisterOntologyResult registerOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult) {
		return orrClient.registerOntology(createOntologyResult, loginResult);
	}


	public TempOntologyInfo getTempOntologyInfo(
			String fileType, String filename, boolean includeContents,
			boolean includeRdf) {
		return orrClient.getTempOntologyInfo(fileType, filename, includeContents, includeRdf);
	}


	////////////////////////////////////////////////////////////////////////////////////////////
	// VINE:


	public List<RelationInfo> getDefaultVineRelationInfos() {
		return orrClient.getDefaultVineRelationInfos();
	}

	/**
	 * Gets an external ontology.
	 */
	public ExternalOntologyInfo getExternalOntologyInfo(String ontologyUri) {
		return orrClient.getExternalOntologyInfo(ontologyUri);
	}

	// :VINE
	////////////////////////////////////////////////////////////////////////////////////////////


	// Search:

	public SparqlQueryResult runSparqlQuery(SparqlQueryInfo query) {
		return orrClient.runSparqlQuery(query);
	}


	// login

	public LoginResult authenticateUser(String userName, String userPassword) {
		return orrClient.authenticateUser(userName, userPassword);
	}

	public ResetPasswordResult resetUserPassword(String username) {
		return orrClient.resetUserPassword(username);
	}

	public UserInfoResult getUserInfo(String username) {
		return orrClient.getUserInfo(username);
	}

	public CreateUpdateUserAccountResult createUpdateUserAccount(Map<String,String> values) {
		return orrClient.createUpdateUserAccount(values);
	}


	/////////////////////////////////////////////////////////////////////////////////////////////
	// OOI CI semantic prototype
	public RegisterOntologyResult registerOntologyDirectly(
			LoginResult loginResult,
			RegisteredOntologyInfo registeredOntologyInfo,
			CreateOntologyInfo createOntologyInfo,
			String graphId
	) {
		return orrClient.registerOntologyDirectly(loginResult, registeredOntologyInfo, createOntologyInfo, graphId);
	}


	public InternalOntologyResult prepareUsersOntology(LoginResult loginResult) {
		return orrClient.prepareUsersOntology(loginResult);
	}

	public InternalOntologyResult createGroupsOntology(LoginResult loginResult) {
		return orrClient.createGroupsOntology(loginResult);
	}


	public UnregisterOntologyResult unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo oi) {
		return orrClient.unregisterOntology(loginResult, oi);
	}

	public String markTestingOntology(LoginResult loginResult, RegisteredOntologyInfo oi, boolean markTesting) {
		return orrClient.markTestingOntology(loginResult, oi, markTesting);
	}
}
