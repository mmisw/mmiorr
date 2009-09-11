package org.mmisw.iserver.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.util.JenaUtil2;
import org.mmisw.iserver.core.util.MailSender;
import org.mmisw.iserver.core.util.OntServiceUtil;
import org.mmisw.iserver.core.util.QueryUtil;
import org.mmisw.iserver.core.util.TempOntologyHelper;
import org.mmisw.iserver.core.util.Utf8Util;
import org.mmisw.iserver.core.util.Util2;
import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.DataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.MappingDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.OtherDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.ResetPasswordResult;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryInfo;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryResult;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.UserInfoResult;
import org.mmisw.iserver.gwt.client.rpc.VocabularyDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo.PriorOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.iserver.gwt.client.vocabulary.AttrGroup;
import org.mmisw.ont.MmiUri;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.drexel.util.rdf.JenaUtil;
import edu.drexel.util.rdf.OwlModel;



/**
 * Implementation of IServer. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class Server implements IServer {
	private static final long serialVersionUID = 1L;
	
	private static final String LISTALL = "?listall";

	/** Ontology URI prefix including root: */
	public static String defaultNamespaceRoot;


	private final AppInfo appInfo = new AppInfo("MMISW IServer");
	private final Log log = LogFactory.getLog(Server.class);

	private File previewDir;
	
	
	private static IServer _instance ;
	
	public static IServer getInstance(String ontServiceUrl, String bioportalRestUrl) {
		if ( _instance == null ) {
			_instance = new Server(ontServiceUrl, bioportalRestUrl);
		}
		
		return _instance;
	}
	
	private Server(String ontServiceUrl, String bioportalRestUrl) {
		defaultNamespaceRoot = ontServiceUrl;
		log.info("basic init " +appInfo.getAppName()+ "...");
		try {
			ServerConfig.getInstance();
			
			appInfo.setVersion(
					ServerConfig.Prop.VERSION.getValue()+ " (" +
						ServerConfig.Prop.BUILD.getValue()  + ")"
			);
					
			log.info(appInfo.toString());
			
			previewDir = new File(ServerConfig.Prop.ONTMD_PREVIEW_DIR.getValue());
			
			ServerConfig.Prop.ONT_SERVICE_URL.setValue(ontServiceUrl);
			log.info("ontServiceUrl = " +ontServiceUrl);
			
			ServerConfig.Prop.BIOPORTAL_REST_URL.setValue(bioportalRestUrl);
			log.info("bioportalRestUrl = " +bioportalRestUrl);
		}
		catch (Throwable ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
		}
	}
	
	public void destroy() {
		log.info(appInfo+ ": destroy called.\n\n");
	}
	
	public AppInfo getAppInfo() {
		return appInfo;
	}
	

	private MetadataBaseInfo metadataBaseInfo = null;
	
	public MetadataBaseInfo getMetadataBaseInfo(
			boolean includeVersion, String resourceTypeClassUri,
			String authorityClassUri) {
		
		log.info("preparing base info ...");
		
		if ( metadataBaseInfo == null ) {
			metadataBaseInfo = new MetadataBaseInfo();
			
			metadataBaseInfo.setResourceTypeUri(Omv.acronym.getURI());
			metadataBaseInfo.setAuthorityAbbreviationUri(OmvMmi.origMaintainerCode.getURI());
			
			MdHelper.prepareGroups(includeVersion, resourceTypeClassUri, authorityClassUri);
			AttrGroup[] attrGroups = MdHelper.getAttrGroups();
			metadataBaseInfo.setAttrGroups(attrGroups);
			log.info("preparing base info ... DONE");

		}
		
		return metadataBaseInfo;
	}

	public List<EntityInfo> getEntities(String ontologyUri) {
		if ( log.isDebugEnabled() ) {
			log.debug("getEntities(String) starting");
		}
		try {
			return  QueryUtil.getEntities(ontologyUri, null);
		}
		catch (Exception e) {
			String error = "Error getting entities: " +e.getMessage();
			log.error(error, e);
			return null;
		}
	}

	
	private static RegisteredOntologyInfo _createOntologyInfo(
			String ontologyUri,   // = toks[0];
			String displayLabel,  // = toks[1];
			String type,          // = toks[2];
			String userId,        // = toks[3];
			String contactName,   // = toks[4];
			String versionNumber, // = toks[5];
			String dateCreated,   // = toks[6];
			String userName,      // = toks[7];
			String ontologyId,     // = toks[8];
			
			String unversionedUri,
			String authority,
			String shortName
	) {
		RegisteredOntologyInfo registeredOntologyInfo = new RegisteredOntologyInfo();
		
		registeredOntologyInfo.setUri(ontologyUri);
		registeredOntologyInfo.setDisplayLabel(displayLabel);
		registeredOntologyInfo.setType(type);
		registeredOntologyInfo.setUserId(userId);
		registeredOntologyInfo.setContactName(contactName);
		registeredOntologyInfo.setVersionNumber(versionNumber);
		registeredOntologyInfo.setDateCreated(dateCreated);
		registeredOntologyInfo.setUsername(userName);
		registeredOntologyInfo.setOntologyId(ontologyId, userId);
		
		registeredOntologyInfo.setUnversionedUri(unversionedUri);
		registeredOntologyInfo.setAuthority(authority);
		registeredOntologyInfo.setShortName(shortName);

		return registeredOntologyInfo;
	}
	
	
	/**
	 * Gets the ontologies from the registry as a map { unversionedUri -> list of OntologyInfos }.
	 * Elements in each list are sorted by descending versionNumber.
	 * 
	 *  @param onlyThisUnversionedUri If this is not null, only this URI (assumed to be unversioned) will be considered,
	 *         so the returned map will at most contain just that single key,
	 */
	private Map<String, List<RegisteredOntologyInfo>> getUnversionedToOntologyInfoListMap(String onlyThisUnversionedUri) throws Exception {
		
		// unversionedUri -> list of corresponding OntologyInfos
		Map<String, List<RegisteredOntologyInfo>> unversionedToVersioned = new LinkedHashMap<String, List<RegisteredOntologyInfo>>();
		
		String uri = ServerConfig.Prop.ONT_SERVICE_URL.getValue()+ LISTALL;
		
		if ( log.isDebugEnabled() ) {
			log.debug("getUnversionedToVersioned. uri= " +uri);
		}

		String response = getAsString(uri, Integer.MAX_VALUE);
		
		String[] lines = response.split("\n|\r\n|\r");
		for ( String line : lines ) {
			// remove leading and trailing quote:
			line = line.replaceAll("^'|'$", "");
			String[] toks = line.trim().split("'\\|'");
			
			String ontologyUri =  toks[0];
			String displayLabel  = toks[1];
			String type          = toks[2];
			String userId        = toks[3];
			String contactName   = toks[4];
			String versionNumber = toks[5];
			String dateCreated   = toks[6];
			String userName      = toks[7];
			String ontologyId    = toks[8];

			String unversionedUri;
			String authority;
			String shortName;
			
			if ( OntServiceUtil.isOntResolvableUri(ontologyUri) ) {
				try {
					MmiUri mmiUri = new MmiUri(ontologyUri);
					authority = mmiUri.getAuthority();
					shortName = mmiUri.getTopic();
					unversionedUri = mmiUri.copyWithVersion(null).getOntologyUri();
				}
				catch (URISyntaxException e) {
					// shouldn't happen.
					
					String error = "Shouldn't not happen: ont-resolvable URI is not an MmiUri: " 
						+ontologyUri+ "  Error: " +e.getMessage();
					log.error("getUnversionedToOntologyInfoListMap: " +error, e);
					continue;
				}
			}
			else {
				// FIXME  setting and hoc values temporarily
				authority = ontologyUri;
				shortName = "tempshortname";
				unversionedUri = ontologyUri;
			}

			if ( onlyThisUnversionedUri != null && ! onlyThisUnversionedUri.equals(unversionedUri) ) {
				continue;
			}
			
			RegisteredOntologyInfo registeredOntologyInfo = _createOntologyInfo(
					ontologyUri,
					displayLabel,
					type,
					userId,
					contactName,
					versionNumber,
					dateCreated,
					userName,
					ontologyId,
					
					unversionedUri,
					authority,
					shortName
			);


			List<RegisteredOntologyInfo> versionedList = unversionedToVersioned.get(unversionedUri);
			if ( versionedList == null ) {
				versionedList = new ArrayList<RegisteredOntologyInfo>();
				unversionedToVersioned.put(unversionedUri, versionedList);
			}
			versionedList.add(registeredOntologyInfo);
			registeredOntologyInfo.setUnversionedUri(unversionedUri);
			
		}
		
		// sort all list by descending versionNumber
		for ( String unversionedUri : unversionedToVersioned.keySet() ) {
			List<RegisteredOntologyInfo> versionedList = unversionedToVersioned.get(unversionedUri);
			Collections.sort(versionedList, new Comparator<RegisteredOntologyInfo>() {
				public int compare(RegisteredOntologyInfo arg0, RegisteredOntologyInfo arg1) {
					return - arg0.getVersionNumber().compareTo(arg1.getVersionNumber());
				}
			});
		}
		
		if ( log.isDebugEnabled() ) {
			log.debug("getUnversionedToOntologyInfoListMap: " +unversionedToVersioned.size()+ " ontologies.");
		}


		return unversionedToVersioned;
	}

	
	public List<RegisteredOntologyInfo> getAllOntologies(boolean includeAllVersions) throws Exception {
		
		// {unversionedUri -> list of versioned URIs }  for all unversioned URIs ontologies
		Map<String, List<RegisteredOntologyInfo>> unversionedToVersioned = getUnversionedToOntologyInfoListMap(null);
		
		// the list to be returned
		List<RegisteredOntologyInfo> onts = new ArrayList<RegisteredOntologyInfo>();
		
		for ( String unversionedUri : unversionedToVersioned.keySet() ) {

			List<RegisteredOntologyInfo> versionedList = unversionedToVersioned.get(unversionedUri);
			
			// copy first element, ie., most recent ontology version, for the entry in the main list;
			// Note: the Uri of this main entry is set equal to the UnversionedUri property:
			
			RegisteredOntologyInfo mostRecent = versionedList.get(0);
			RegisteredOntologyInfo registeredOntologyInfo = _createOntologyInfo(
					mostRecent.getUnversionedUri(),      // NOTE: UnversionedURI for the URI
					mostRecent.getDisplayLabel(),
					mostRecent.getType(),
					mostRecent.getUserId(),
					mostRecent.getContactName(),
					mostRecent.getVersionNumber(),
					mostRecent.getDateCreated(),
					mostRecent.getUsername(),
					mostRecent.getOntologyId(),
					
					mostRecent.getUnversionedUri(),
					mostRecent.getAuthority(),
					mostRecent.getShortName()
			);

			// if requested, include all versions: 
			if ( includeAllVersions ) {
				registeredOntologyInfo.getPriorVersions().addAll(versionedList);
			}
			
			// add this main entry to returned list:
			onts.add(registeredOntologyInfo);
		}
		
		return onts;
	}
	
	public RegisteredOntologyInfo getOntologyInfo(String ontologyUri) {
		
		log.debug("getOntologyInfo: ontologyUri=" +ontologyUri);
		
		String[] toks = ontologyUri.split("\\?");
		ontologyUri = toks[0];
		
		String version = null;
		if ( toks.length > 1 && toks[1].startsWith("version=") ) {
			version = toks[1].substring("version=".length());
		}
		
		log.debug("getOntologyInfo: ontologyUri=" +ontologyUri+ "  version=" +version);
		
		if ( OntServiceUtil.isOntResolvableUri(ontologyUri) ) {
			try {
				MmiUri mmiUri = new MmiUri(ontologyUri);
				return getOntologyInfoFromMmiUri(ontologyUri, mmiUri, version);
			}
			catch (URISyntaxException e) {
				String error = e.getMessage();
				log.error("getOntologyInfo: Error in URI: " +ontologyUri, e);
				RegisteredOntologyInfo oi = new RegisteredOntologyInfo();
				oi.setError(error);
				return oi;
			}
		}
		else {
			// "external" URI
			String unversOntologyUri = ontologyUri;
			try {
				boolean includeAllVersions = true;
				return getOntologyInfoWithVersionParams(ontologyUri, unversOntologyUri, version, includeAllVersions);
			}
			catch (Exception e) {
				String error = e.getMessage();
				log.error("Error getting RegisteredOntologyInfo: " +ontologyUri, e);
				RegisteredOntologyInfo oi = new RegisteredOntologyInfo();
				oi.setError(error);
				return oi;
			}
		}
	}

	private RegisteredOntologyInfo getOntologyInfoFromMmiUri(String ontologyUri, MmiUri mmiUri, String version) {
		try {	
			// get elements associated with the unversioned form of the requested URI:
			String unversOntologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
			boolean includeAllVersions = true;
			return getOntologyInfoWithVersionParams(ontologyUri, unversOntologyUri, version, includeAllVersions);
		}
		catch (Exception e) {
			String error = e.getMessage();
			log.error("getOntologyInfoFromMmiUri: Error.", e);
			RegisteredOntologyInfo oi = new RegisteredOntologyInfo();
			oi.setError(error);
			return oi;
		}
	}
	
	/**
	 * Get info about a registered ontology using unversion and explicit version parameters.
	 * 
	 * @param ontologyUri           original requested URI
	 * @param unversOntologyUri     corresponding unversioned form
	 * @param version               explicit version
	 * @return
	 * @throws Exception
	 */
	private RegisteredOntologyInfo getOntologyInfoWithVersionParams(String ontologyUri, String unversOntologyUri,
			String version, boolean includeAllVersions) throws Exception {

		// first, get list of entries for the requested ontology using the unversioned form as key:
		Map<String, List<RegisteredOntologyInfo>> unversionedToVersioned = getUnversionedToOntologyInfoListMap(unversOntologyUri);

		log.debug("getOntologyInfoWithVersionParams: getUnversionedToOntologyInfoListMap => " +unversionedToVersioned);
		
		if ( unversionedToVersioned.isEmpty() ) {
			return null; // not found
		}
		assert unversionedToVersioned.size() == 1;

		// get the list of ontologies with same unversioned URI
		List<RegisteredOntologyInfo> list = unversionedToVersioned.values().iterator().next();

		// Three cases: 
		//
		//  a) explicit version given: search for exact match using 'version' field
		//
		//  b) unversioned URI request -> just return first entry in list, which is the most recent,
		//     but making the Uri property equal to the UnversionedUri property
		//
		//  c) versioned URI request -> search the list for the exact match using the 'uri' field
		//


		RegisteredOntologyInfo foundRoi = null;
		
		if ( version != null ) {
			log.debug("Server.getOntologyInfoWithVersionParams case a) version = " +version);
			//  a) explicit version given: search for exact match using the 'version' field:
			for ( RegisteredOntologyInfo oi : list ) {
				if ( version.equals(oi.getVersionNumber()) ) {
					foundRoi = oi;
					break;
				}
			}
		}
		else if ( ontologyUri.equals(unversOntologyUri) ) {
			log.debug("Server.getOntologyInfoWithVersionParams case b) unversioned request = " +unversOntologyUri);
			// b) unversioned URI request, eg., http://mmisw.org/ont/seadatanet/qualityFlag
			// just return first entry in list
			RegisteredOntologyInfo oi = list.get(0);
			oi.setUri(oi.getUnversionedUri());
			foundRoi = oi;
		}
		else {
			log.debug("Server.getOntologyInfoWithVersionParams case c) versioned request = " +ontologyUri);
			// c) versioned URI request, eg., http://mmisw.org/ont/seadatanet/20081113T205440/qualityFlag
			// Search list for exact match of 'uri' field:
			for ( RegisteredOntologyInfo oi : list ) {
				if ( ontologyUri.equals(oi.getUri()) ) {
					foundRoi = oi;
					break;
				}
			}
		}
		
		if ( foundRoi != null && includeAllVersions ) {
			foundRoi.getPriorVersions().addAll(list);
		}
		
		return foundRoi;
	}

	public RegisteredOntologyInfo getEntities(RegisteredOntologyInfo registeredOntologyInfo) {
		if ( log.isDebugEnabled() ) {
			log.debug("getEntities(RegisteredOntologyInfo) starting");
		}
		
		
		OntModel ontModel;
		try {
			// note, version==null -> not specific version, so get the latest version
			final String version = null;
			ontModel = OntServiceUtil.retrieveModel(registeredOntologyInfo.getUri(), version);
		}
		catch (Exception e) {
			String error = "Error loading model: " +e.getMessage();
			log.error(error, e);
			registeredOntologyInfo.setError(error);
			return registeredOntologyInfo;
		}

		
		try {
			QueryUtil.getEntities(registeredOntologyInfo, ontModel);
		}
		catch (Exception e) {
			String error = "Error loading model: " +e.getMessage();
			log.error(error, e);
			registeredOntologyInfo.setError(error);
			return registeredOntologyInfo;
		}
		
		return registeredOntologyInfo;
	}


	private static String getAsString(String uri, int maxlen) throws Exception {
		HttpClient client = new HttpClient();
	    GetMethod meth = new GetMethod(uri);
	    try {
	        client.executeMethod(meth);

	        if (meth.getStatusCode() == HttpStatus.SC_OK) {
	            return meth.getResponseBodyAsString(maxlen);
	        }
	        else {
	          throw new Exception("Unexpected failure: " + meth.getStatusLine().toString());
	        }
	    }
	    finally {
	        meth.releaseConnection();
	    }
	}
	
	
	
	
	public RegisteredOntologyInfo getOntologyContents(RegisteredOntologyInfo registeredOntologyInfo, String version) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(RegisteredOntologyInfo): loading model");
		}
		
		OntModel ontModel;
		try {
			ontModel = OntServiceUtil.retrieveModel(registeredOntologyInfo.getUri(), version);
		}
		catch (Exception e) {
			String error = "Error loading model: " +e.getMessage();
			log.error(error, e);
			registeredOntologyInfo.setError(error);
			return registeredOntologyInfo;
		}

		// Metadata:
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(RegisteredOntologyInfo): getting metadata");
		}
		MetadataExtractor.prepareOntologyMetadata(metadataBaseInfo, ontModel, registeredOntologyInfo);

		
		// Data
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(RegisteredOntologyInfo): getting entities");
		}
		
		try {
			QueryUtil.getEntities(registeredOntologyInfo, ontModel);
		}
		catch (Exception e) {
			String error = "Error getting entities: " +e.getMessage();
			log.error(error, e);
			registeredOntologyInfo.setError(error);
			return registeredOntologyInfo;
		}
		
		return registeredOntologyInfo;
	
	}

	
	
	
	// TODO this mechanism copied from MmiUri (in ont project).
	private static final Pattern VERSION_PATTERN = 
				Pattern.compile("^\\d{4}(\\d{2}(\\d{2})?)?(T\\d{2})?(\\d{2}(\\d{2})?)?$");


	////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	
	public CreateOntologyResult createOntology(CreateOntologyInfo createOntologyInfo) {
			
		log.info("createOntology: called.");
		
		CreateOntologyResult createOntologyResult = new CreateOntologyResult();
		
		DataCreationInfo dataCreationInfo = createOntologyInfo.getDataCreationInfo();
		if ( dataCreationInfo == null ) {
			createOntologyResult.setError("No data creation info provided! (please report this bug)");
			return createOntologyResult;
		}
		

		createOntologyResult.setCreateOntologyInfo(createOntologyInfo);
		
		// to check if this is going to be a new submission (if ontologyId == null) or, otherwise, a new version.
		final String ontologyId = createOntologyInfo.getPriorOntologyInfo().getOntologyId();
		
		
		//{pons      pons: sections related with preserveOriginalBaseNamespace
		
		// this flag will be only true in the case where an external ontology is to be registered
		// and the user indicates that the original base namespace be preserved.
		createOntologyResult.setPreserveOriginalBaseNamespace(false);
		
		if ( dataCreationInfo instanceof OtherDataCreationInfo ) {
			OtherDataCreationInfo odci = (OtherDataCreationInfo) dataCreationInfo;
			TempOntologyInfo toi = odci.getTempOntologyInfo();
			createOntologyResult.setPreserveOriginalBaseNamespace(toi != null && toi.isPreserveOriginalBaseNamespace());
		}

		
		if ( ! createOntologyResult.isPreserveOriginalBaseNamespace() ) {
			// Note: if this is the submission of a new version (ontologyId != null) of an "external" (ie, re-hosted)
			// ontology, then set this flag to true
			if ( ontologyId != null && ! OntServiceUtil.isOntResolvableUri(createOntologyInfo.getUri()) ) {
				createOntologyResult.setPreserveOriginalBaseNamespace(true);
				
				// TODO However, note that we're goint to preserve the URI of the given ontology in this submission,
				// which may not coincide with the previous one.
			}
		}
		//}pons

		
		Map<String, String> newValues = createOntologyInfo.getMetadataValues();
		
		////////////////////////////////////////////
		// check for errors
		
		if ( createOntologyResult.getError() != null ) {
			log.info(": error: " +createOntologyResult.getError());
			return createOntologyResult;
		}
		
		
		if ( newValues == null ) {
			String error = "Unexpected: no new values assigned for review. Please report this bug";
			createOntologyResult.setError(error );
			log.info(error);
			return createOntologyResult;
		}
		
		
		final String namespaceRoot = defaultNamespaceRoot;
		
		final String orgAbbreviation = newValues.get(OmvMmi.origMaintainerCode.getURI());
		final String shortName = newValues.get(Omv.acronym.getURI());
		// TODO: shortName taken NOT from acronym but from a new field explicitly for the shortName piece

		if ( ! createOntologyResult.isPreserveOriginalBaseNamespace() ) {
			//pons: check the following if regular assignment of namespace
			
			if ( orgAbbreviation == null ) {
				log.info("missing origMaintainerCode");
				createOntologyResult.setError("missing origMaintainerCode");
				return createOntologyResult;
			}
			if ( shortName == null ) {
				log.info("missing acronym");
				createOntologyResult.setError("missing acronym");
				return createOntologyResult;
			}

			if ( ontologyId == null ) {
				// This is a new submission. We need to check for any conflict with a preexisting
				// ontology in the repository with the same shortName+orgAbbreviation combination
				//
				if ( ! Util2.checkNoPreexistingOntology(namespaceRoot, orgAbbreviation, shortName, createOntologyResult) ) {
					return createOntologyResult;
				}
			}
			else {
				// This is a submission of a *new version* of an existing ontology.
				// We need to check the shortName+orgAbbreviation combination as any changes here
				// would imply a *new* ontology, not a new version.
				//
				
				String originalOrgAbbreviation = createOntologyInfo.getAuthority();
				String originalShortName = createOntologyInfo.getShortName();
				
				if ( ! Util2.checkUriKeyCombinationForNewVersion(
						originalOrgAbbreviation, originalShortName, 
						orgAbbreviation, shortName, createOntologyResult) ) {
					return createOntologyResult;
				}
			}
		}
		// Else: see below, where we obtain the original namespace.
		
		
		////////////////////////////////////////////////////////////////////////////
		// section to create the ontology the base:
		
		if ( dataCreationInfo instanceof VocabularyDataCreationInfo ) {
			// vocabulary (voc2rdf) case:
			
			VocabularyDataCreationInfo vocabularyDataCreationInfo = (VocabularyDataCreationInfo) dataCreationInfo;
			
			_createTempVocabularyOntology(createOntologyInfo, vocabularyDataCreationInfo, createOntologyResult);
			if ( createOntologyResult.getError() != null ) {
				return createOntologyResult;
			}
		}
		else if (  dataCreationInfo instanceof MappingDataCreationInfo ) {
			// mapping (vine case):
			
			MappingDataCreationInfo mappingDataCreationInfo = (MappingDataCreationInfo) dataCreationInfo;
			
			_createTempMappingOntology(createOntologyInfo, mappingDataCreationInfo, createOntologyResult);
			if ( createOntologyResult.getError() != null ) {
				return createOntologyResult;
			}

		}
		else if (  dataCreationInfo instanceof OtherDataCreationInfo) {
			// external ontology case: the base ontology is already available, just use it
			// by setting the full path in the createOntologyResult:
			
			OtherDataCreationInfo otherDataCreationInfo = (OtherDataCreationInfo) dataCreationInfo;
			TempOntologyInfo tempOntologyInfo = otherDataCreationInfo.getTempOntologyInfo();
			
			String full_path;
			
			if ( tempOntologyInfo != null ) {
				// new contents were provided. Use that:
				full_path = tempOntologyInfo.getFullPath();
			}
			else {
				// No new contents. Only possible way for this to happen is that this is 
				// a new version of an existing ontology.
				
				if ( ontologyId != null ) {
					// Just indicate a null full_path; see below.
					full_path = null;
				}
				else {
					// This shouldn't happen!
					String error = "Unexpected: Submission of new ontology, but not contents were provided. " +
					"This should be detected before submission. Please report this bug";
					createOntologyResult.setError(error);
					log.info(error);
					return createOntologyResult;
				}
				
			}
			createOntologyResult.setFullPath(full_path);
		}
		else {
			createOntologyResult.setError("Unexpected creation of " +
					dataCreationInfo.getClass().getSimpleName()+ ". Please report this bug.");
			return createOntologyResult;
		}

		
		// current date:
		final Date date = new Date(System.currentTimeMillis());
		
		///////////////////////////////////////////////////////////////////
		// creation date:
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		final String creationDate = sdf.format(date);
		

		///////////////////////////////////////////////////////////////////
		// version:
		// Note, if the version is given from the client, then use it
		String version = newValues.get(Omv.version.getURI());
		if ( version != null && version.trim().length() > 0 ) {
			// check that the given version is OK:
			boolean ok = VERSION_PATTERN.matcher(version).find();
			if ( ! ok ) {
				String error = "Given version is invalid: " +version;
				log.info(error);
				createOntologyResult.setError(error);
				return createOntologyResult;
			}
		}
		else {
			// otherwise: assign it here:
			sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			version = sdf.format(date);
		}
		
		
		////////////////////////////////////////////
		// load  model

		OntModel model;
		String uriForEmpty;
		String newContentsFileName;

		if ( createOntologyResult.getFullPath() != null ) {
			//
			// new contents to check.
			// Get model from the new contents.
			//
			String full_path = createOntologyResult.getFullPath();
			log.info("Loading model: " +full_path);

			File file = new File(full_path);
			try {
				Utf8Util.verifyUtf8(file);
			}
			catch (Exception e) {
				String error = "Error reading model: " +e.getMessage();
				log.error(error, e);
				createOntologyResult.setError(error);
				return createOntologyResult;
			}
			
			String uriFile = file.toURI().toString();
			try {
				model = JenaUtil.loadModel(uriFile, false);
			}
			catch ( Throwable ex ) {
				String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
				log.info(error);
				createOntologyResult.setError(error);
				return createOntologyResult;
			}
			
			uriForEmpty = Util2.getDefaultNamespace(file, createOntologyResult);

			if ( uriForEmpty == null ) {
				String error = "Cannot get base URI for the ontology";
				log.info(error);
				createOntologyResult.setError(error);
				return createOntologyResult;
			}
			
			newContentsFileName = file.getName();
		}
		else {
			// NO new contents.
			// Use contents from prior version.
			PriorOntologyInfo priorVersionInfo = createOntologyInfo.getPriorOntologyInfo();
			
			try {
				model = OntServiceUtil.retrieveModel(createOntologyInfo.getUri(), priorVersionInfo.getVersionNumber());
			}
			catch (Exception e) {
				String error = "error while retrieving registered ontology: " +e.getMessage();
				log.info(error, e);
				createOntologyResult.setError(error);
				return createOntologyResult;
			}
			
			uriForEmpty = model.getNsPrefixURI("");
			if ( uriForEmpty == null ) {
				// Shouldn't happen -- we're reading in an already registered version.
				String error = "error while getting URI for empty prefix for a registered version. " +
						"Please report this bug.";
				log.info(error);
				createOntologyResult.setError(error);
				return createOntologyResult;
			}

			// replace ':', '/', or '\' for '_'
			newContentsFileName = uriForEmpty.replaceAll(":|/|\\\\", "_");
		}

			

		
		log.info("createOntology: using '" +uriForEmpty+ "' as base URI");
		
		final String original_ns_ = uriForEmpty;
		log.info("original namespace: " +original_ns_);

		
		String ns_;
		String base_;

		
		if ( createOntologyResult.isPreserveOriginalBaseNamespace() ) {
			//pons:  just use original namespace
			ns_ = original_ns_;
			base_ = JenaUtil2.removeTrailingFragment(ns_);
			
			///////////////////////////////////////////////////////
			
			if ( ontologyId == null ) {
				// This is a new submission. We need to check for any conflict with a preexisting
				// ontology in the repository with the same URI, base_
				//
				if ( ! Util2.checkNoPreexistingOntology(base_, createOntologyResult) ) {
					return createOntologyResult;
				}
			}
			else {
				// This is a submission of a *new version* of an existing ontology.
				// NO check needed--the given URI (base_) is respected.
			}
			///////////////////////////////////////////////////////
		}
		else {
			
			final String finalUri = namespaceRoot + "/" +
										orgAbbreviation + "/" +
										version + "/" +
										shortName;
										
			ns_ = JenaUtil2.appendFragment(finalUri);
			base_ = JenaUtil2.removeTrailingFragment(finalUri);
			
			
			log.info("Setting prefix \"\" for URI " + ns_);
			model.setNsPrefix("", ns_);
			
			
			// Update statements  according to the new namespace:
			Util2.replaceNameSpace(model, original_ns_, ns_);
			
		}
		
		
		/////////////////////////////////////////////////////////////////
		// Is there an existing OWL.Ontology individual?
		// TODO Note that ONLY the first OWL.Ontology individual is considered.
		Resource ontRes = JenaUtil.getFirstIndividual(model, OWL.Ontology);
		List<Statement> prexistStatements = null; 
		if ( ontRes != null ) {
			prexistStatements = new ArrayList<Statement>();
			log.info("Getting pre-existing properties for OWL.Ontology individual: " +ontRes.getURI());
			StmtIterator iter = ontRes.listProperties();
			while ( iter.hasNext() ) {
				Statement st = iter.nextStatement();
				prexistStatements.add(st);
			}	
		}

		
		// The new OntModel that will contain the pre-existing attributes (if any),
		// plus the new and updated attributes:
		final OwlModel newOntModel = new OwlModel(model);
		final Ontology ont_ = newOntModel.createOntology(base_);
		log.info("New ontology created with namespace " + ns_ + " base " + base_);
		newOntModel.setNsPrefix("", ns_);
		
		// set preferred prefixes:
		Map<String, String> preferredPrefixMap = MdHelper.getPreferredPrefixMap();
		for ( String uri : preferredPrefixMap.keySet() ) {
			String prefix = preferredPrefixMap.get(uri);
			newOntModel.setNsPrefix(prefix, uri);
		}
		
		
		// Set internal attributes, which are updated in the newValues map itself
		// so we facilite the processing below:
		newValues.put(Omv.uri.getURI(), base_);
		newValues.put(Omv.version.getURI(), version);
		
		newValues.put(Omv.creationDate.getURI(), creationDate);


		//////////////////////////////////////////////////
		// transfer any preexisting attributes, and then remove all properties from
		// pre-existing ontRes so just the new OntModel gets added.
		if ( ontRes != null ) {
			for ( Statement st : prexistStatements ) {
				Property prd = st.getPredicate();

				//
				// Do not tranfer pre-existing/pre-assigned-above attributes
				//
				String newValue = newValues.get(prd.getURI());
				if ( newValue == null || newValue.trim().length() == 0 ) {
					log.info("  Transferring: " +st.getSubject()+ " :: " +prd+ " :: " +st.getObject());
					newOntModel.add(ont_, prd, st.getObject());
				}
				else {
					log.info(" Removing pre-existing values for predicate: " +prd+ " because of new value " +newValue);
					newOntModel.removeAll(ont_, prd, null);
				}
			}	
			
			
			if ( ! createOntologyResult.isPreserveOriginalBaseNamespace() ) {
				
				// 
				// Only, when we're creating a new model, ie., per the new namespace, do the following removals.
				// (If we did this on a model with the same original namespace, we would remove the owl:Ontology 
				// entry altogether and get an "rdf:Description" instead.
				//
				
				log.info("Removing original OWL.Ontology individual");
				ontRes.removeProperties();
				// TODO the following may be unnecesary but doesn't hurt:
				model.remove(ontRes, RDF.type, OWL.Ontology); 
			}
		}

		
		
		///////////////////////////////////////////////////////
		// Update attributes in model:

		Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
		for ( String uri : newValues.keySet() ) {
			String value = newValues.get(uri).trim();
			if ( value.length() > 0 ) {
				Property prop = uriPropMap.get(uri);
				if ( prop == null ) {
					log.info("No property found for uri='" +uri+ "'");
					continue;
				}

				log.info(" Assigning: " +uri+ " = " +value);
				ont_.addProperty(prop, value);
			}
		}
		


		// Set the missing DC attrs that have defined e	equivalent MMI attrs: 
		Util2.setDcAttributes(ont_);
		
		////////////////////////////////////////////////////////////////////////
		// Done with the model. 
		////////////////////////////////////////////////////////////////////////
		
		// Get resulting string:
		String rdf = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV") ;  // XXX newOntModel);
		
		//TODO: pons: print result RDF for testing
		System.out.println(rdf);
		if ( log.isDebugEnabled() ) {
			if ( createOntologyResult.isPreserveOriginalBaseNamespace() ) {
				log.debug(rdf);
			}
		}
		
		log.debug("createOntology: setting URI: " +base_);
		createOntologyResult.setUri(base_);
		

		// write new contents to a new file under previewDir:
		
		File reviewedFile = new File(previewDir, newContentsFileName);
		createOntologyResult.setFullPath(reviewedFile.getAbsolutePath());

		PrintWriter os;
		try {
			os = new PrintWriter(reviewedFile);
		}
		catch (FileNotFoundException e) {
			log.info("Unexpected: file not found: " +reviewedFile);
			createOntologyResult.setError("Unexpected: file not found: " +reviewedFile);
			return createOntologyResult;
		}
		StringReader is = new StringReader(rdf);
		try {
			IOUtils.copy(is, os);
			os.flush();
		}
		catch (IOException e) {
			log.info("Unexpected: IO error while writing to: " +reviewedFile);
			createOntologyResult.setError("Unexpected: IO error while writing to: " +reviewedFile);
			return createOntologyResult;
		}

		// Done.

		return createOntologyResult;
	}

	/**
	 * This creates a new vocabulary ontology under the previewDir directory with the given info.
	 * @param createOntologyInfo
	 * @param vocabularyCreationInfo
	 * @param createVocabResult 
	 */
	private void _createTempVocabularyOntology(
			CreateOntologyInfo createOntologyInfo,
			VocabularyDataCreationInfo vocabularyCreationInfo,
			CreateOntologyResult createVocabResult
	) {
		
		VocabCreator vocabCreator;
		try {
			vocabCreator = new VocabCreator(createOntologyInfo, vocabularyCreationInfo);
		}
		catch (Exception e) {
			String error = "Error in conversion to RDF: " +e.getMessage();
			log.error(error, e);
			createVocabResult.setError(error);
			return;
		}
		
		vocabCreator.createOntology(createVocabResult);
	}

	
	/**
	 * This creates a new mapping ontology under the previewDir directory with the given info.
	 * @param createOntologyInfo
	 * @param mappingDataCreationInfo
	 * @param createVocabResult 
	 */
	private void _createTempMappingOntology(
			CreateOntologyInfo createOntologyInfo,
			MappingDataCreationInfo mappingDataCreationInfo,
			CreateOntologyResult createVocabResult
	) {

		MappingOntologyCreator ontCreator;
		try {
			ontCreator = new MappingOntologyCreator(createOntologyInfo, mappingDataCreationInfo);
		}
		catch (Exception e) {
			String error = "Error creating mapping ontology: " +e.getMessage();
			log.error(error, e);
			createVocabResult.setError(error);
			return;
		}
		
		ontCreator.createOntology(createVocabResult);
	}

	
	
	
	public RegisterOntologyResult registerOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult)  {
		RegisterOntologyResult registerOntologyResult = new RegisterOntologyResult();
		
		
		String full_path = createOntologyResult.getFullPath();
		
		log.info("registerOntology: Reading in temporary file: " +full_path);
		
		File file = new File(full_path);
		if ( ! file.canRead() ) {
			String error = "Unexpected: cannot read: " +full_path;
			log.info(error);
			registerOntologyResult.setError(error);
			return registerOntologyResult;
		}
		
		// Get resulting model:
		String rdf;
		try {
			rdf = Util2.readRdf(file);
		}
		catch (IOException e) {
			String error = "Unexpected: IO error while reading from: " +full_path+ " : " +e.getMessage();
			log.info(error);
			registerOntologyResult.setError(error);
			return registerOntologyResult;
		}
		
		// ok, we have our ontology:
		
		
		//////////////////////////////////////////////////////////////////////////
		// finally, do actual registration to MMI registry

		// Get final URI of resulting model
		// FIXME this uses the same original URI
		final String uri = createOntologyResult.getUri();
		assert uri != null;
		assert loginResult.getUserId() != null;
		assert loginResult.getSessionId() != null;
		
		log.info(": registering ...");

		CreateOntologyInfo createOntologyInfo = createOntologyResult.getCreateOntologyInfo();

		String ontologyId = createOntologyInfo.getPriorOntologyInfo().getOntologyId();
		String ontologyUserId = createOntologyInfo.getPriorOntologyInfo().getOntologyUserId();
		
		if ( ontologyId != null ) {
			log.info("Will create a new version for ontologyId = " +ontologyId+ ", userId=" +ontologyUserId);
		}
		
		
		Map<String, String> newValues = createOntologyInfo .getMetadataValues();
		

		try {
			
			String fileName = new URL(uri).getPath();
			
			//
			// make sure the fileName ends with ".owl" as the aquaportal back-end seems
			// to add that fixed extension in some operations (at least in the parse operation)
			//
			if ( ! fileName.toLowerCase().endsWith(".owl") ) {
				log.info("register: setting file extension to .owl per aquaportal requirement.");
				fileName += ".owl";
			}
			
			
			if ( ! createOntologyResult.isPreserveOriginalBaseNamespace() ) {
				// We are about to do the actual registration. But first, re-check that there is NO a preexisting
				// ontology that may conflict with this one.
				// NOTE: this check has been done already in the review operation; however, we repeat it here
				// in case there is a new registration done by other user in the meantime. Of course, we
				// are NOT completely solving the potential concurrency problem with this re-check; we are just
				// reducing the chances of that event.
				if ( ontologyId == null ) {
					
					final String namespaceRoot = newValues.get("namespaceRoot") != null 
							? newValues.get("namespaceRoot")
							:  defaultNamespaceRoot;
	
					final String orgAbbreviation = newValues.get(OmvMmi.origMaintainerCode.getURI());
					final String shortName = newValues.get(Omv.acronym.getURI());
	
					if ( ! Util2.checkNoPreexistingOntology(namespaceRoot, orgAbbreviation, shortName, registerOntologyResult) ) {
						return registerOntologyResult;
					}
	
				}
				else {
					// This is a submission of a *new version* of an existing ontology.
					// Nothing needs to be checked here.
					// NOTE: We don't need to repeat the _checkUriKeyCombinationForNewVersion step here
					// as any change in the contents of the metadata forces the user to explicitly
					// do the "review" operation, which already takes care of that check.
				}
			}
			
			// OK, now do the actual registration:
			OntologyUploader createOnt = new OntologyUploader(uri, fileName, rdf, 
					loginResult,
					ontologyId, ontologyUserId,
					newValues
			);
			String res = createOnt.create();
			
			if ( res.startsWith("OK") ) {
				registerOntologyResult.setUri(uri);
				registerOntologyResult.setInfo(res);
				
				// issue #168 fix:
				// request that the ontology be loaded in the "ont" graph:
				OntServiceUtil.loadOntologyInGraph(uri);
			}
			else {
				registerOntologyResult.setError(res);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			registerOntologyResult.setError(ex.getClass().getName()+ ": " +ex.getMessage());
		}
		
		log.info("registerOntologyResult = " +registerOntologyResult);

		
		return registerOntologyResult;
	}

	
	
	
	public TempOntologyInfo getTempOntologyInfo(String uploadResults, boolean includeContents,
			boolean includeRdf
	) {
		TempOntologyInfo tempOntologyInfo = new TempOntologyInfo();
		
		if ( metadataBaseInfo == null ) {
			tempOntologyInfo.setError("IServer not properly initialized!");
			return tempOntologyInfo;
		}
		
		TempOntologyHelper tempOntologyHelper = new TempOntologyHelper(metadataBaseInfo);
		tempOntologyHelper.getTempOntologyInfo(uploadResults, tempOntologyInfo, includeRdf);
		
		if ( tempOntologyInfo.getError() != null ) {
			return tempOntologyInfo;
		}
		
		if ( includeContents ) {
			_getOntologyContents(tempOntologyInfo);
		}
		
		return tempOntologyInfo;
	}
	
	/** similar to {@link #getOntologyContents(RegisteredOntologyInfo)} but reading the model
	 * from the internal path.
	 */
	private TempOntologyInfo _getOntologyContents(TempOntologyInfo tempOntologyInfo) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_getOntologyContents(TempOntologyInfo): loading model");
		}
		
		String full_path = tempOntologyInfo.getFullPath();
		File file = new File(full_path );
		String uriFile = file.toURI().toString();
		log.info("Loading model: " +uriFile);

		OntModel ontModel;
		try {
			ontModel = JenaUtil.loadModel(uriFile, false);
		}
		catch (Throwable ex) {
			String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
			log.info(error);
			tempOntologyInfo.setError(error);
			return tempOntologyInfo;
		}


		// Metadata:
		if ( log.isDebugEnabled() ) {
			log.debug("_getOntologyContents(TempOntologyInfo): getting metadata");
		}
		MetadataExtractor.prepareOntologyMetadata(metadataBaseInfo, ontModel, tempOntologyInfo);

		
		// Data
		if ( log.isDebugEnabled() ) {
			log.debug("_getOntologyContents(TempOntologyInfo): getting entities");
		}
		
		try {
			QueryUtil.getEntities(tempOntologyInfo, ontModel);
		}
		catch (Exception e) {
			String error = "Error loading model: " +e.getMessage();
			log.error(error, e);
			tempOntologyInfo.setError(error);
			return tempOntologyInfo;
		}
		
		
		return tempOntologyInfo;
	
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////
	// VINE:
	
	public List<RelationInfo> getVineRelationInfos() {
		if ( log.isDebugEnabled() ) {
			log.debug("getRelationInfos starting");
		}
	
		// TODO: determine mechanism to obtain the list of default mapping relations, for example,
		// from an ontology.
		
		// For now, creating a hard-coded list
		
		List<RelationInfo> relInfos = new ArrayList<RelationInfo>();
		
//      URI="http://www.w3.org/2008/05/skos#exactMatch"
//      icon="icons/exactMatch28.png"
//      name="exactMatch"
//      tooltip="The property skos:exactMatch is used to link two concepts, indicating a high degree of confidence that the concepts can be used interchangeably across a wide range of information retrieval applications. [SKOS Section 10.1] (transitive, symmetric)"
		relInfos.add(new RelationInfo(
				"http://www.w3.org/2008/05/skos#exactMatch", 
				"exactMatch28.png", 
				"exactMatch",
				"The property skos:exactMatch is used to link two concepts, indicating a high degree of confidence that the concepts can be used interchangeably across a wide range of information retrieval applications. [SKOS Section 10.1] (transitive, symmetric)"
		));
		
//      URI="http://www.w3.org/2008/05/skos#closeMatch"
//      icon="icons/closeMatch28.png"
//      name="closeMatch"
//      tooltip="A skos:closeMatch link indicates that two concepts are sufficiently similar that they can be used interchangeably in some information retrieval applications. [SKOS Section 10.1] (symmetric)"
		relInfos.add(new RelationInfo(
				"http://www.w3.org/2008/05/skos#closeMatch", 
				"closeMatch28.png", 
				"closeMatch",
				"A skos:closeMatch link indicates that two concepts are sufficiently similar that they can be used interchangeably in some information retrieval applications. [SKOS Section 10.1] (symmetric)"
		));
		
//      URI="http://www.w3.org/2008/05/skos#broadMatch"
//      icon="icons/broadMatch28.png"
//      name="broadMatch"
//      tooltip="'has the broader concept': the second (object) concept is broader than the first (subject) concept [SKOS Section 8.1] (infers broaderTransitive, a transitive relation)"
		relInfos.add(new RelationInfo(
				"http://www.w3.org/2008/05/skos#broadMatch", 
				"broadMatch28.png", 
				"broadMatch",
				"'has the broader concept': the second (object) concept is broader than the first (subject) concept [SKOS Section 8.1] (infers broaderTransitive, a transitive relation)"
		));

//      URI="http://www.w3.org/2008/05/skos#narrowMatch"
//      icon="icons/narrowMatch28.png"
//      name="narrowMatch"
//      tooltip="'has the narrower concept': the second (object) concept is narrower than the first (subject) concept [SKOS Section 8.1] (infers narrowTransitive, a transitive relation)"
		relInfos.add(new RelationInfo(
				"http://www.w3.org/2008/05/skos#narrowMatch", 
				"narrowMatch28.png", 
				"narrowMatch",
				"'has the narrower concept': the second (object) concept is narrower than the first (subject) concept [SKOS Section 8.1] (infers narrowTransitive, a transitive relation)"
		));

//      URI="http://www.w3.org/2008/05/skos#relatedMatch"
//      icon="icons/relatedMatch28.png"
//      name="relatedMatch"
//      tooltip="The property skos:relatedMatch is used to state an associative mapping link between two concepts. [SKOS Section 8.1] (symmetric)"
		relInfos.add(new RelationInfo(
				"http://www.w3.org/2008/05/skos#relatedMatch", 
				"relatedMatch28.png", 
				"relatedMatch",
				"The property skos:relatedMatch is used to state an associative mapping link between two concepts. [SKOS Section 8.1] (symmetric)"
		));

		if ( log.isDebugEnabled() ) {
			log.debug("getRelationInfos returning: " +relInfos);
		}

		return relInfos;

	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// Search:
	
	public SparqlQueryResult runSparqlQuery(SparqlQueryInfo query) {
		SparqlQueryResult sparqlQueryResult = new SparqlQueryResult();
		try {
			String result = OntServiceUtil.runSparqlQuery(query.getQuery(), query.getFormat(), "application/rdf+xml");
			sparqlQueryResult.setResult(result);
		}
		catch (Exception e) {
			String error = "Error while dispatching query: " +e.getMessage();
			sparqlQueryResult.setError(error);
		}
		return sparqlQueryResult;
	}

	
	// login:
	
	
	public LoginResult authenticateUser(String userName, String userPassword) {
		LoginResult loginResult = new LoginResult();
		
		log.info(": authenticating user " +userName+ " ...");
		try {
			UserAuthenticator login = new UserAuthenticator(userName, userPassword);
			login.getSession(loginResult);
		}
		catch (Exception ex) {
			loginResult.setError(ex.getMessage());
		}

		return loginResult;
		
	}
	
	public ResetPasswordResult resetUserPassword(String username) {
		ResetPasswordResult result = new ResetPasswordResult();
		
		// Get email address for the user
		UserInfoResult userInfoResult = getUserInfo(username);
		if ( userInfoResult.getError() != null ) {
			result.setError(userInfoResult.getError());
			return result;
		}
		final String email = userInfoResult.getProps().get("email");
		if ( email == null ) {
			result.setError("No email associated with username: " +username);
			return result;
		}
		
		// get new password
		String newPassword = Util2.generatePassword();
		
		// TODO update password in back-end
		// ...
		
		// ...
		
		// update in back-end successful.
		
		// send email with new password
		String mail_user = ServerConfig.Prop.MAIL_USER.getValue();
		String mail_password = ServerConfig.Prop.MAIL_PASSWORD.getValue();
		if ( mail_user == null ) {
			String error = "Email server account not configured. Please report this bug. (u)";
			result.setError(error);
			log.error(error);
			return result;
		}
		if ( mail_password == null ) {
			String error = "Email server account not configured. Please report this bug. (p)";
			result.setError(error);
			log.error(error);
			return result;
		}

		
		boolean debug = false;
		final String from = "MMI-ORR <techlead@marinemetadata.org>";
		final String replyTo = "techlead@marinemetadata.org";
		final String subject = "Password reset";
		final String text = "Your MMI ORR password has been reset.\n" +
				"\n" +
				"Username: " +username+ "   email: " +email+ "\n" +
				"Password: " +newPassword+ "\n" +
				"\n"
		;
		
		try {
			MailSender.sendMessage(mail_user, mail_password, debug , from, email, replyTo, subject, text);
			result.setEmail(email);
		}
		catch (Exception e) {
			String error = "Error sending email: " +e.getMessage();
			result.setError(error);
			log.error(error, e);
		}
		
		return result;
	}

	
	public UserInfoResult getUserInfo(String username) {
		UserInfoResult result = new UserInfoResult();
		
		try {
			Map<String, String> props = OntServiceUtil.getUserInfo(username);
			result.setProps(props);
		}
		catch (Exception e) {
			String error = "error getting user information: " +e.getMessage();
			result.setError(error);
			log.error(error, e);
		}
		
		return result;
	}
}
