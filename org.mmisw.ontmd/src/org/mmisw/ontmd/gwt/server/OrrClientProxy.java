package org.mmisw.ontmd.gwt.server;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.IServer;
import org.mmisw.iserver.core.Server;
import org.mmisw.iserver.core.ServerConfig;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.CreateUpdateUserAccountResult;
import org.mmisw.iserver.gwt.client.rpc.InternalOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.ResetPasswordResult;
import org.mmisw.iserver.gwt.client.rpc.ResolveUriResult;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryInfo;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryResult;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.UnregisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.UserInfoResult;
import org.mmisw.iserver.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.ontmd.gwt.client.rpc.PortalBaseInfo;


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
	 * if {@link #createInstance(String, String)} has not been called.
	 * @return
	 */
	public static OrrClientProxy getInstance() {
		return instance;
	}
	
	/**
	 * Crates the instance of this class, if not already created.
	 * @param ontServiceUrl
	 * @param bioportalRestUrl
	 * @return the instance
	 */
	public static OrrClientProxy createInstance(String ontServiceUrl, String bioportalRestUrl) {
		if ( instance != null ) {
			throw new IllegalStateException(OrrClientProxy.class.getName()+ " instance already created");
		}
		instance = new OrrClientProxy(ontServiceUrl, bioportalRestUrl);
		return instance;
	}

	
	private final Log log = LogFactory.getLog(OrrClientProxy.class);
	
	private PortalBaseInfo portalBaseInfo = null;
	
	
	private IServer iserver;
	

	
	private OrrClientProxy(String ontServiceUrl, String bioportalRestUrl) {
		log.info("initializing " +getClass().getSimpleName()+ "...");
		
		iserver = Server.getInstance(ontServiceUrl, bioportalRestUrl);
		log.info("Using: " +iserver.getAppInfo());
		
		ServerConfig.Prop.MAIL_USER.setValue(PortalConfig.Prop.MAIL_USER.getValue());
		ServerConfig.Prop.MAIL_PASSWORD.setValue(PortalConfig.Prop.MAIL_PASSWORD.getValue());
	}

	public PortalBaseInfo getBaseInfo() {
		if ( portalBaseInfo == null ) {
			prepareBaseInfo();
		}
		return portalBaseInfo;
	}
	
	private void prepareBaseInfo() {
		log.info("preparing base info ...");
		
		portalBaseInfo = new PortalBaseInfo();
		
		portalBaseInfo.setAppServerUrl(PortalConfig.Prop.APPSERVER_HOST.getValue());
		portalBaseInfo.setOntServiceUrl(PortalConfig.Prop.ONT_SERVICE_URL.getValue());
		portalBaseInfo.setOntbrowserServiceUrl(PortalConfig.Prop.ONTBROWSER_SERVICE_URL.getValue());

		portalBaseInfo.setGaUaNumber(PortalConfig.Prop.GA_UA_NUMBER.getValue());

		log.info("preparing base info ... Done.");
	}
	
	
	
	public List<RegisteredOntologyInfo> getAllOntologies(boolean includePriorVersions) throws Exception {
		return iserver.getAllOntologies(includePriorVersions);
	}
	
	public ResolveUriResult resolveUri(String uri) {
		return iserver.resolveUri(uri);
	}

	/**
	 * Gets an ontology from the registry.
	 */
	public RegisteredOntologyInfo getOntologyInfo(String ontologyUri) {
		return iserver.getOntologyInfo(ontologyUri);
	}
	
	
	public MetadataBaseInfo getMetadataBaseInfo(boolean includeVersion) {
		String resourceTypeClassUri = PortalConfig.Prop.RESOURCE_TYPE_CLASS.getValue();
		String authorityClassUri = PortalConfig.Prop.AUTHORITY_CLASS.getValue();
		
		return iserver.getMetadataBaseInfo(includeVersion, resourceTypeClassUri, authorityClassUri);
	}
	
	
	public RegisteredOntologyInfo getOntologyContents(RegisteredOntologyInfo ontologyInfo, String version) {
		return iserver.getOntologyContents(ontologyInfo, version);
	}

	public CreateOntologyResult createOntology(CreateOntologyInfo createOntologyInfo) {
		return iserver.createOntology(createOntologyInfo);
	}
	
	
	public RegisterOntologyResult registerOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult) {
		return iserver.registerOntology(createOntologyResult, loginResult);
	}
	
	
	public TempOntologyInfo getTempOntologyInfo(
			String fileType, String uploadResults, boolean includeContents,
			boolean includeRdf) {
		return iserver.getTempOntologyInfo(fileType, uploadResults, includeContents, includeRdf);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// VINE:
	

	public List<RelationInfo> getDefaultVineRelationInfos() {
		return iserver.getDefaultVineRelationInfos();
	}
	
	// :VINE
	////////////////////////////////////////////////////////////////////////////////////////////

	
	// Search:
	
	public SparqlQueryResult runSparqlQuery(SparqlQueryInfo query) {
		return iserver.runSparqlQuery(query);
	}
	
	
	// login
	
	public LoginResult authenticateUser(String userName, String userPassword) {
		return iserver.authenticateUser(userName, userPassword);
	}
	
	public ResetPasswordResult resetUserPassword(String username) {
		return iserver.resetUserPassword(username);
	}

	public UserInfoResult getUserInfo(String username) {
		return iserver.getUserInfo(username);
	}
	
	public CreateUpdateUserAccountResult createUpdateUserAccount(Map<String,String> values) {
		return iserver.createUpdateUserAccount(values);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// OOI CI semantic prototype
	public RegisterOntologyResult registerOntologyDirectly(
			LoginResult loginResult, 
			RegisteredOntologyInfo registeredOntologyInfo,
			CreateOntologyInfo createOntologyInfo, 
			String graphId
	) {
		return iserver.registerOntologyDirectly(loginResult, registeredOntologyInfo, createOntologyInfo, graphId);
	}

	
	public InternalOntologyResult prepareUsersOntology(LoginResult loginResult) {
		return iserver.prepareUsersOntology(loginResult);
	}
	
	public InternalOntologyResult createGroupsOntology(LoginResult loginResult) {
		return iserver.createGroupsOntology(loginResult);
	}
	
	
	public UnregisterOntologyResult unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo oi) {
		return iserver.unregisterOntology(loginResult, oi);
	}
}
