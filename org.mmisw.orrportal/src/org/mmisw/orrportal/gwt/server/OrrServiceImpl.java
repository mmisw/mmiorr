package org.mmisw.orrportal.gwt.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.orrclient.core.MdHelper;
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
import org.mmisw.orrportal.gwt.client.rpc.OrrService;
import org.mmisw.orrportal.gwt.client.rpc.PortalBaseInfo;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;



/**
 * Implementation of the OrrService interface. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class OrrServiceImpl extends RemoteServiceServlet implements OrrService {
	private static final long serialVersionUID = 1L;

	
	private final AppInfo appInfo = new AppInfo("ORR Portal");
	
	private OrrClientProxy orrClient;
	
	
	private static final List<RegisteredOntologyInfo> EMPTY_ONTOLOGY_INFO_LIST = new ArrayList<RegisteredOntologyInfo>();

	
	private final Log log = LogFactory.getLog(OrrServiceImpl.class);
	
	
	/**
	 * Prepares the application info, and configures and initializes the
	 * orrclient library.
	 */
	public void init() throws ServletException {
		super.init();
		log.info("initializing " +appInfo.getAppName()+ "...");
		ServletConfig servletConfig = getServletConfig();
		try {
			PortalConfig.getInstance().init(servletConfig, log, true);
			
			appInfo.setVersion(PortalConfig.Prop.VERSION.getValue());
			appInfo.setBuild(PortalConfig.Prop.BUILD.getValue());
			
			log.info(appInfo.toString());
			
			// orrclient initialization
			orrClient = OrrClientProxy.createInstance();
		}
		catch (Exception ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
			throw new ServletException("Cannot initialize", ex);
		}
	}
	
	public void destroy() {
		super.destroy();
		log.info(appInfo+ ": destroy called.\n\n");
	}
	

	public AppInfo getAppInfo() {
		return appInfo;
	}
	

	
	public AttrDef refreshOptions(AttrDef attrDef) {
		return MdHelper.refreshOptions(attrDef);
	}


	
	public PortalBaseInfo getPortalBaseInfo() {
		return orrClient.getBaseInfo();
	}
	
	public List<RegisteredOntologyInfo> getAllOntologies(boolean includePriorVersions) {
		try {
			return orrClient.getAllOntologies(includePriorVersions);
		}
		catch (Exception e) {
			log.error("error getting list of ontologies", e);
			return EMPTY_ONTOLOGY_INFO_LIST;
		}
	}
	
	public ResolveUriResult resolveUri(String uri) {
		return orrClient.resolveUri(uri);
	}

	
	public MetadataBaseInfo getMetadataBaseInfo(boolean includeVersion) {
		return orrClient.getMetadataBaseInfo(includeVersion);
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
			String fileType, String uploadResults, boolean includeContents,
			boolean includeRdf
	) {
		return orrClient.getTempOntologyInfo(fileType, uploadResults, includeContents, includeRdf);
	}

	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// VINE:
	

	public List<RelationInfo> getDefaultVineRelationInfos() {
		return orrClient.getDefaultVineRelationInfos();
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
	
	public InternalOntologyResult prepareUsersOntology(LoginResult loginResult) {
		return orrClient.prepareUsersOntology(loginResult);
	}

	public InternalOntologyResult createGroupsOntology(LoginResult loginResult) {
		return orrClient.createGroupsOntology(loginResult);
	}

	public UnregisterOntologyResult unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo oi) {
		return orrClient.unregisterOntology(loginResult, oi);
	}
	
}
