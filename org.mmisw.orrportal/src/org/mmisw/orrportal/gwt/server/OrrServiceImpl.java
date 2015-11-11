package org.mmisw.orrportal.gwt.server;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.orrclient.IOrrClient;
import org.mmisw.orrclient.OrrClientFactory;
import org.mmisw.orrclient.gwt.client.rpc.AppInfo;
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
import org.mmisw.orrportal.gwt.client.rpc.OrrService;
import org.mmisw.orrportal.gwt.client.rpc.PortalBaseInfo;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;



/**
 * Implementation of the OrrService interface. 
 */
public class OrrServiceImpl extends RemoteServiceServlet implements OrrService {
	private static final long serialVersionUID = 1L;

	
	private final AppInfo appInfo = new AppInfo("ORR Portal");

	private PortalBaseInfo portalBaseInfo;

	private IOrrClient orrClient;
	
	
	private final Log log = LogFactory.getLog(OrrServiceImpl.class);
	
	
	/**
	 * Prepares the application info, and configures and initializes the
	 * orrclient library.
	 */
	public void init() throws ServletException {
		super.init();
		log.info("initializing " +appInfo.getAppName()+ "...");

		try {
			OrrConfig.init();

			ServletConfig servletConfig = getServletConfig();
			PortalConfig.getInstance().init(servletConfig, log, true);
			
			appInfo.setVersion(OrrVersion.getVersion());
			appInfo.setBuild(OrrVersion.getBuild());
			
			log.info(appInfo.toString());

			portalBaseInfo = _prepareBaseInfo();

			orrClient = OrrClientFactory.init();
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
		return orrClient.refreshOptions(attrDef);
	}


	
	public PortalBaseInfo getPortalBaseInfo() {
		return portalBaseInfo;
	}
	
	public GetAllOntologiesResult getAllOntologies(boolean includePriorVersions) {
		return orrClient.getAllOntologies(includePriorVersions);
	}
	
	public MetadataBaseInfo getMetadataBaseInfo(boolean includeVersion) {
		String resourceTypeClassUri = OrrConfig.instance().resourceTypeClass;
		String authorityClassUri    = OrrConfig.instance().authorityClass;

		return orrClient.getMetadataBaseInfo(includeVersion, resourceTypeClassUri, authorityClassUri);
	}

	public ResolveUriResult resolveUri(String uri) {
		return orrClient.resolveUri(uri);
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
			String fileType, String uploadResults, boolean includeContents,
			boolean includeRdf
	) {
		// extract the filename from the upload result string:
		String filename;
		try {
			filename = _getFilePathFromUploadResults(uploadResults);
		}
		catch (Exception e) {
			TempOntologyInfo tempOntologyInfo = new TempOntologyInfo();
			tempOntologyInfo.setError(e.getMessage());
			return tempOntologyInfo;
		}
		
		return orrClient.getTempOntologyInfo(fileType, filename, includeContents, includeRdf);
	}

	/**
	 * Extracts the filename from the given upload results string.
	 * @param uploadResults
	 * @return the filename
	 * @throws Exception if the upload results indicate error, or if there is any other error.
	 */
	private String _getFilePathFromUploadResults(String uploadResults) throws Exception {
		
		uploadResults = uploadResults.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		
		if ( log.isDebugEnabled() ) {
			log.debug("_getFilePathFromUploadResults: " +uploadResults);
		}
		
		if ( uploadResults.matches(".*<error>.*") ) {
			throw new Exception(uploadResults);
		}
		
		if ( false && !uploadResults.matches(".*success.*") ) {
			if ( log.isDebugEnabled() ) {
				log.debug("Not <success> !");
			}
			// unexpected response.
			throw new Exception("Error while loading ontology. Please try again later.");
		}
		
		Pattern pat = Pattern.compile(".*<filename>([^<]+)</filename>");
		Matcher matcher = pat.matcher(uploadResults);
		if ( matcher.find() ) {
			String filename = matcher.group(1);
			return filename;
		}
		else {
			if ( log.isDebugEnabled() ) {
				log.debug("Could not parse uploadResults.");
			}
			throw new Exception("Could not parse uploadResults.");
		}
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// VINE:
	

	public List<RelationInfo> getDefaultVineRelationInfos() {
		return orrClient.getDefaultVineRelationInfos();
	}
	
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

	///////////////////////////////////////////////////////////////////////////

	private PortalBaseInfo _prepareBaseInfo() {
		PortalBaseInfo pbi = new PortalBaseInfo();
		pbi.setOntServiceUrl(OrrConfig.instance().ontServiceUrl);
		pbi.setGaUaNumber(PortalConfig.Prop.GA_UA_NUMBER.getValue());
		log.info("portal base info: done.");
		return pbi;
	}
}
