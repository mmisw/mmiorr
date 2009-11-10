package org.mmisw.ontmd.gwt.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.mmisw.iserver.core.util.Utf8Util;
import org.mmisw.iserver.core.util.Util2;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.HostingType;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.OtherDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.UserInfoResult;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ontmd.gwt.server.portal.PortalImpl;

/**
 * This servlet handles the direct registration of an ontology.
 * 
 * @author Carlos Rueda
 */
public class DirectRegistrationServlet extends UploadServlet {
	private static final long serialVersionUID = 1L;

	private PortalImpl portal;
	
	
	public void init() throws ServletException {
		super.init();
		log.info("initializing " +getClass().getSimpleName()+ "...");
		ServletConfig servletConfig = getServletConfig();
		try {
			portal = PortalImpl.getInstance();
			
			if ( portal == null ) {
				String logMsg = getClass().getSimpleName()+ ": PortalImpl instance not yet created. Creating...";
				System.out.println(logMsg);
				log.info(logMsg);
				
				PortalConfig.getInstance().init(servletConfig, log, true);

				// portal initialization
				String ontServiceUrl = PortalConfig.Prop.ONT_SERVICE_URL.getValue();
				String bioportalRestUrl = PortalConfig.Prop.BIOPORTAL_REST_URL.getValue();
				portal = PortalImpl.createInstance(ontServiceUrl, bioportalRestUrl);
			}
			else {
				String logMsg = getClass().getSimpleName()+ ": PortalImpl instance already created";
				System.out.println(logMsg);
				log.info(logMsg);
			}
		}
		catch (Exception ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
			throw new ServletException("Cannot initialize", ex);
		}
	}
	
	public void destroy() {
		super.destroy();
		log.info(getClass().getSimpleName()+ ": destroy called.\n\n");
	}

	
	/** 
	 * Overrides the parent method to check for the username/password fields,
	 * prepare, and complete the registration.
	 * 
	 * @throws IOException 
	 */
	@Override
	protected void processItems(HttpServletRequest request,
			HttpServletResponse response, List<?> items) throws IOException {
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		Map<String,String> fields = new HashMap<String,String>();
		
		try {
			File file = null;
			
			Iterator<?> iter = items.iterator();
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();
	
			    if (item.isFormField()) {
			    	fields.put(item.getFieldName(), item.getString());
			    } 
			    else {
			        file = processUploadedFile(request, item);
			    }
			}
			
			String error = null;
			
			if ( fields.get("username") == null || fields.get("username").trim().length() == 0 ) {
				error = "No username specified";
			}
			else if ( fields.get("password") == null || fields.get("password").trim().length() == 0 ) {
				error = "No password specified";
			}
			else if ( fields.get("ontologyUri") == null || fields.get("ontologyUri").trim().length() == 0 ) {
				error = "No ontology URI specified";
			}
			else if ( file == null ) {
				error = "No file included";
			}
			
			if ( error != null ) {
				log.error("SC_BAD_REQUEST: " +error);
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
				return;
			}
			
			System.out.println("file = " +file);
			
			processRegistration(request, response, out, fields, file);
		}
		catch (Exception ex) {
			out.println("<error>" +ex.getMessage()+ "</error>");
			log.error("Error while processing registration: " +ex.getMessage(), ex);
			ex.printStackTrace();
		}
		
	}


	private void processRegistration(HttpServletRequest request, HttpServletResponse response, 
			PrintWriter out,
			Map<String, String> fields, File file) 
	throws Exception {
		
		LoginResult loginResult = validateUser(fields, out);
		
		
		TempOntologyInfo tempOntologyInfo = getTempOntologyInfo(file);
		
		String ontologyUri = fields.get("ontologyUri").trim();
		RegisteredOntologyInfo registeredOntologyInfo = portal.getOntologyInfo(ontologyUri);
		
		if ( registeredOntologyInfo == null ) {
			//
			// Case: new ontology.
			//
			registerNewOntology(loginResult, ontologyUri, tempOntologyInfo);
			
			StringBuffer responseString = new StringBuffer();
			
			String msg = "New ontology registered";
			
			responseString.append("<success>\n");
			responseString.append("  <ontologyUri>" +ontologyUri+ "</ontologyUri>\n");
			responseString.append("  <msg>" +msg+ "</msg>\n");
			responseString.append("</success>\n");
			
			out.println(responseString);
			
			System.out.println("registerNewOntology response: " +responseString);
			if ( log.isDebugEnabled() ) {
				log.debug("registerNewOntology response: " +responseString);
			}
		}
		else {
			//
			// Case: new version for existing ontology.
			//
			registerNewVersion(loginResult, ontologyUri, registeredOntologyInfo, tempOntologyInfo);
			
			StringBuffer responseString = new StringBuffer();
			
			String msg = "New version of ontology registered";
			
			responseString.append("<success>\n");
			responseString.append("  <ontologyUri>" +ontologyUri+ "</ontologyUri>\n");
			responseString.append("  <msg>" +msg+ "</msg>\n");
			responseString.append("</success>\n");
			
			out.println(responseString);
			
			System.out.println("registerNewVersion response: " +responseString);
			if ( log.isDebugEnabled() ) {
				log.debug("registerNewVersion response: " +responseString);
			}
		}
	}

	
	private LoginResult validateUser(Map<String, String> fields, PrintWriter out) throws Exception {
		String username = fields.get("username");
		String password = fields.get("password");
		LoginResult logingResult = portal.authenticateUser(username, password);
		if ( logingResult == null ) {
			throw new Exception("Error authenticating user: " +username);
		}
		if ( logingResult.getError() != null ) {
			throw new Exception("Error authenticating user: " +username+ ": " +logingResult.getError());
		}
		
		// OK.
		return logingResult;
	}

	
	

	private TempOntologyInfo getTempOntologyInfo(File file) throws Exception {
		TempOntologyInfo tempOntologyInfo = new TempOntologyInfo();

		try {
			Utf8Util.verifyUtf8(file);
		}
		catch (Exception e) {
			String error = "verifyUtf8: " +e.getMessage();
			log.error(error, e);
			throw new Exception(error, e);
		}
		
		String full_path = file.getAbsolutePath();
		
		try {
			Util2.readRdf(file);
		}
		catch (Throwable e) {
			String error = "Cannot read RDF model: " +full_path+ " : " +e.getMessage();
			log.info(error, e);
			tempOntologyInfo.setError(error);
			return tempOntologyInfo;
		}
	
		tempOntologyInfo.setFullPath(full_path);

		return tempOntologyInfo;
	}

	
	
	private RegisterOntologyResult registerNewOntology(
			LoginResult loginResult,
			String ontologyUri,
			TempOntologyInfo tempOntologyInfo
	) throws Exception {
		
		CreateOntologyInfo createOntologyInfo = new CreateOntologyInfo();
		createOntologyInfo.setHostingType(HostingType.RE_HOSTED);
		createOntologyInfo.setUri(ontologyUri);
		
		setSomeValues(loginResult, createOntologyInfo);
		
		OtherDataCreationInfo dataCreationInfo = new OtherDataCreationInfo();
		dataCreationInfo.setTempOntologyInfo(tempOntologyInfo);
		createOntologyInfo.setDataCreationInfo(dataCreationInfo);
		
		// set info of original ontology:
		createOntologyInfo.setBaseOntologyInfo(tempOntologyInfo);
		
		CreateOntologyResult createOntologyResult = portal.createOntology(createOntologyInfo);
		if ( createOntologyResult.getError() != null ) {
			throw new Exception("Error creating ontology for registration: " +createOntologyResult.getError());
		}
		
		RegisterOntologyResult registerOntologyResult = portal.registerOntology(createOntologyResult, loginResult);
		if ( registerOntologyResult.getError() != null ) {
			throw new Exception("Error registering ontology: " +registerOntologyResult.getError());
		}
		
		// OK!
		return registerOntologyResult;
	}


	/**
	 * Registers a new version of an ontology.
	 * NOTE that NO data/metadata whatsoever is used from the previous version.
	 * 
	 * @param loginResult
	 * @param ontologyUri
	 * @param registeredOntologyInfo
	 * @param tempOntologyInfo
	 * @return
	 * @throws Exception
	 */
	private RegisterOntologyResult registerNewVersion(
			LoginResult loginResult, 
			String ontologyUri,
			RegisteredOntologyInfo registeredOntologyInfo,
			TempOntologyInfo tempOntologyInfo
	) throws Exception {
		
		// verify hostingType is re-hosted:
		HostingType hostingType = registeredOntologyInfo.getHostingType();
		if ( hostingType != HostingType.RE_HOSTED ) {
			throw new Exception("Hosting type of existing ontology is not 're-hosted'.");
		}
		
		// verify that the submitting user is the same:
		if ( ! loginResult.getUserId().equals(registeredOntologyInfo.getUserId()) ) {
			throw new Exception("User '" +loginResult.getUserName()+ "' is not allowed to " +
					"register new version for existing ontology with URI: " +ontologyUri);
		}
		
		
		CreateOntologyInfo createOntologyInfo = new CreateOntologyInfo();
		createOntologyInfo.setHostingType(HostingType.RE_HOSTED);
		createOntologyInfo.setUri(ontologyUri);
		
		createOntologyInfo.setPriorOntologyInfo(
				registeredOntologyInfo.getOntologyId(), 
				registeredOntologyInfo.getOntologyUserId(), 
				registeredOntologyInfo.getVersionNumber()
		);
		
		setSomeValues(loginResult, createOntologyInfo);
		
		OtherDataCreationInfo dataCreationInfo = new OtherDataCreationInfo();
		dataCreationInfo.setTempOntologyInfo(tempOntologyInfo);
		createOntologyInfo.setDataCreationInfo(dataCreationInfo);
		
		// set info of original ontology:
		createOntologyInfo.setBaseOntologyInfo(tempOntologyInfo);
		
		CreateOntologyResult createOntologyResult = portal.createOntology(createOntologyInfo);
		if ( createOntologyResult.getError() != null ) {
			throw new Exception("Error creating ontology for registration: " +createOntologyResult.getError());
		}
		
		RegisterOntologyResult registerOntologyResult = portal.registerOntology(createOntologyResult, loginResult);
		if ( registerOntologyResult.getError() != null ) {
			throw new Exception("Error registering ontology: " +registerOntologyResult.getError());
		}
		
		// OK!
		return registerOntologyResult;

	}

	
	/**
	 * Sets some of the metadata values that are used by iserver to register the ontology.
	 */
	private void setSomeValues(LoginResult loginResult, CreateOntologyInfo createOntologyInfo) {
		
		// contactName: first, try firstname/lastname from userInfo:
		String contactName= "";
		
		UserInfoResult userInfoResult = portal.getUserInfo(loginResult.getUserName());
		if ( userInfoResult != null && userInfoResult.getError() == null ) {
			Map<String, String> userProps = userInfoResult.getProps();
			contactName = "";
			if ( userProps.get("firstname") != null ) {
				contactName += userProps.get("firstname");
			}
			if ( userProps.get("lastname") != null ) {
				contactName += " " +userProps.get("lastname");
			}
		}
		
		if ( contactName.length() == 0 ) {
			// no userInfo?  ok, just use the username:
			contactName = loginResult.getUserName();
		}
		
		Map<String, String> values = new HashMap<String, String>();
		values.put(Omv.hasCreator.getURI(), contactName);
		createOntologyInfo.setMetadataValues(values);
	}


}