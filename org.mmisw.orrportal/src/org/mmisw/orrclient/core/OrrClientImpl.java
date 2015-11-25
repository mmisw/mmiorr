package org.mmisw.orrclient.core;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.OntVersion;
import org.mmisw.ont.client.IOntClient;
import org.mmisw.ont.client.OntClientConfiguration;
import org.mmisw.ont.client.SignInResult;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.orrclient.IOrrClient;
import org.mmisw.orrclient.core.ontmodel.OntModelUtil;
import org.mmisw.orrclient.core.util.MailSender;
import org.mmisw.orrclient.core.util.OntServiceUtil;
import org.mmisw.orrclient.core.util.TempOntologyHelper;
import org.mmisw.orrclient.core.util.Util2;
import org.mmisw.orrclient.core.util.ontinfo.OntInfoUtil;
import org.mmisw.orrclient.core.util.ontype.OntTypeUtil;
import org.mmisw.orrclient.core.vine.MappingOntologyCreator;
import org.mmisw.orrclient.core.vine.VineUtil;
import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo.PriorOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.CreateUpdateUserAccountResult;
import org.mmisw.orrclient.gwt.client.rpc.DataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.EntityInfo;
import org.mmisw.orrclient.gwt.client.rpc.ExternalOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.GetAllOntologiesResult;
import org.mmisw.orrclient.gwt.client.rpc.HostingType;
import org.mmisw.orrclient.gwt.client.rpc.InternalOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.MappingDataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.orrclient.gwt.client.rpc.OntologyType;
import org.mmisw.orrclient.gwt.client.rpc.OtherDataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.PropValue;
import org.mmisw.orrclient.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.ResetPasswordResult;
import org.mmisw.orrclient.gwt.client.rpc.ResolveUriResult;
import org.mmisw.orrclient.gwt.client.rpc.SparqlQueryInfo;
import org.mmisw.orrclient.gwt.client.rpc.SparqlQueryResult;
import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.UnregisterOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.UserInfoResult;
import org.mmisw.orrclient.gwt.client.rpc.VocabularyDataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrGroup;

import com.opencsv.CSVReader;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import org.mmisw.orrportal.gwt.server.OrrConfig;


/**
 * Implementation of OrrClient operations.
 *
 * @author Carlos Rueda
 * @version $Id$
 */
public class OrrClientImpl implements IOrrClient {

	private static final Log log = LogFactory.getLog(OrrClientImpl.class);

	private static final String LISTALL = "?listall";

	/** Ontology URI prefix including root: */
	private static String defaultNamespaceRoot;


	private final File previewDir;

	private static IOrrClient _instance;

	private IOntClient ontClient;


	/**
	 * Creates and returns the instance of this class.
	 */
	public static IOrrClient init() throws Exception {
		_instance = new OrrClientImpl();
		return _instance;
	}

	/**
	 * Returns the instance of this class.
	 */
	public static IOrrClient getInstance() {
		return _instance;
	}

	private OrrClientImpl() throws Exception {

		defaultNamespaceRoot = OrrConfig.instance().ontServiceUrl;
		log.info("ontServiceUrl = " +OrrConfig.instance().ontServiceUrl);

		OntClientConfiguration ontClientConfig = new OntClientConfiguration();
		ontClientConfig.setOntServiceUrl(OrrConfig.instance().ontServiceUrl);

		ontClient = IOntClient.Manager.init(ontClientConfig);
		OntServiceUtil.setOntClient(ontClient);
		log.info("Ont library version = " +OntVersion.getVersion()+ " (" +OntVersion.getBuild()+ ")");

		previewDir = OrrConfig.instance().previewDir;
	}

	private MetadataBaseInfo metadataBaseInfo = null;

	public MetadataBaseInfo getMetadataBaseInfo(
			boolean includeVersion, String resourceTypeClassUri,
			String authorityClassUri) {


		if ( metadataBaseInfo == null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("preparing base info ...");
			}

			metadataBaseInfo = new MetadataBaseInfo();

//			metadataBaseInfo.setResourceTypeUri(Omv.acronym.getURI());
			metadataBaseInfo.setResourceTypeUri(OmvMmi.hasResourceType.getURI());

			metadataBaseInfo.setAuthorityAbbreviationUri(OmvMmi.origMaintainerCode.getURI());

			MdHelper.prepareGroups(includeVersion, resourceTypeClassUri, authorityClassUri);
			AttrGroup[] attrGroups = MdHelper.getAttrGroups();
			metadataBaseInfo.setAttrGroups(attrGroups);

			metadataBaseInfo.setAuthorityAttrDef(MdHelper.getAuthorityAttrDef());

			metadataBaseInfo.setResourceTypeAttrDef(MdHelper.getResourceTypeAttrDef());

			metadataBaseInfo.setUriAttrDefMap(MdHelper.getUriAttrDefMap());

			if ( log.isDebugEnabled() ) {
				log.debug("preparing base info ... DONE");
			}
		}

		return metadataBaseInfo;
	}

	public AttrDef refreshOptions(AttrDef attrDef) {
		return MdHelper.refreshOptions(attrDef);
	}

	private static RegisteredOntologyInfo _createOntologyInfo(
			String ontologyUri,   // = toks[0];
			String displayLabel,  // = toks[1];
			OntologyType type,    // = toks[2];
			String userId,        // = toks[3];
			String contactName,   // = toks[4];
			String versionNumber, // = toks[5];
			String dateCreated,   // = toks[6];
			String userName,      // = toks[7];
			String ontologyId,    // = toks[8];
			String versionStatus, // = toks[9];

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
		registeredOntologyInfo.setVersionStatus(versionStatus);

		registeredOntologyInfo.setUnversionedUri(unversionedUri);
		registeredOntologyInfo.setAuthority(authority);
		registeredOntologyInfo.setShortName(shortName);

		_setHostingType(registeredOntologyInfo);

		return registeredOntologyInfo;
	}


	/**
	 * Gets the ontologies from the registry as a map { unversionedUri -> list of OntologyInfos }.
	 * Elements in each list are sorted by descending versionNumber.
	 *
	 *  @param onlyThisUnversionedUri If this is not null, only this URI (assumed to be unversioned) will be considered,
	 *         so the returned map will at most contain just that single key,
	 */
	private Map<String, List<RegisteredOntologyInfo>> _getUnversionedToOntologyInfoListMap(String onlyThisUnversionedUri) throws Exception {

		// unversionedUri -> list of corresponding OntologyInfos
		Map<String, List<RegisteredOntologyInfo>> unversionedToVersioned = new LinkedHashMap<String, List<RegisteredOntologyInfo>>();

		String uri = OrrConfig.instance().ontServiceUrl + LISTALL;

		if ( log.isDebugEnabled() ) {
			log.debug("getUnversionedToVersioned. uri= " +uri);
		}

		String response = _getAsString(uri, Integer.MAX_VALUE);

		String[] lines = response.split("\n|\r\n|\r");
		for ( String line : lines ) {
			// remove leading and trailing quote:
			line = line.replaceAll("^'|'$", "");

			if ( line.trim().length() == 0 ) {
				continue;
			}

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
			String versionStatus = toks[9];

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

					String error = "Shouldn't happen: ont-resolvable URI is not an MmiUri: "
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
					OntTypeUtil.map(type),
					userId,
					contactName,
					versionNumber,
					dateCreated,
					userName,
					ontologyId,
                    versionStatus,

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

		// sort all lists by descending versionNumber
		Comparator<RegisteredOntologyInfo> comparator = new Comparator<RegisteredOntologyInfo>() {
			public int compare(RegisteredOntologyInfo arg0, RegisteredOntologyInfo arg1) {
				return - arg0.getVersionNumber().compareTo(arg1.getVersionNumber());
			}
		};
		for ( String unversionedUri : unversionedToVersioned.keySet() ) {
			List<RegisteredOntologyInfo> versionedList = unversionedToVersioned.get(unversionedUri);
			Collections.sort(versionedList, comparator);
		}


		if ( log.isDebugEnabled() ) {
			log.debug("getUnversionedToOntologyInfoListMap: " +unversionedToVersioned.size()+ " ontologies.");
		}


		return unversionedToVersioned;
	}


	private static void _setHostingType(RegisteredOntologyInfo registeredOntologyInfo) {
		String uri = registeredOntologyInfo.getUri();
		boolean ontResolvableUri = OntServiceUtil.isOntResolvableUri(uri);

		HostingType hostingType;
		if ( ontResolvableUri ) {
			hostingType = HostingType.FULLY_HOSTED;
		}
		else {
			hostingType = HostingType.RE_HOSTED;
		}
		// TODO: Determine HostingType.INDEXED case.

		registeredOntologyInfo.setHostingType(hostingType);

        if(log.isTraceEnabled()) {
            log.trace("_setHostingType: '" + uri + "' ontResolvableUri: " + ontResolvableUri +
                      "-> hostingType=" + hostingType);
        }
	}

	public GetAllOntologiesResult getAllOntologies(boolean includeAllVersions) {
		GetAllOntologiesResult result = new GetAllOntologiesResult();
		try {
			List<RegisteredOntologyInfo> list = _doGetAllOntologies(includeAllVersions);
			result.setOntologyList(list);
		}
		catch (Throwable ex) {
			String error = "Error getting list of all ontologies: " +ex.getMessage();
			log.warn(error, ex);
			result.setError(error);
		}
		return result;
	}


	private List<RegisteredOntologyInfo> _doGetAllOntologies(boolean includeAllVersions) throws Exception {
		// {unversionedUri -> list of versioned URIs }  for all unversioned URIs ontologies
		Map<String, List<RegisteredOntologyInfo>> unversionedToVersioned = _getUnversionedToOntologyInfoListMap(null);

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
					mostRecent.getVersionStatus(),

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


	public ResolveUriResult resolveUri(String uri) {
		ResolveUriResult resolveUriResult = new ResolveUriResult(uri);

		// try ontology:
		RegisteredOntologyInfo roi = getOntologyInfo(uri);
		if ( roi != null && roi.getError() == null ) {
			resolveUriResult.setRegisteredOntologyInfo(roi);
			return resolveUriResult;
		}

		// try term:
		_getEntityInfo(uri, resolveUriResult);

		return resolveUriResult;
	}

	private void _getEntityInfo(String uri, ResolveUriResult resolveUriResult) {

		// NOTE that the query needs to be against Ont, and not in this module (as those done by QueryUtil),
		// because the query needs to be against all registered ontologies.

		String query =
			"SELECT DISTINCT ?prop ?value " +
			"WHERE { <" +uri+ "> ?prop ?value . }"
		;
		String format = "csv";

		if ( log.isDebugEnabled() ) {
			log.debug(" format=" +format+ " query=[" +query+ "]");
		}

		String result;
		try {
			result = OntServiceUtil.runSparqlQuery(query, format, "text/plain");
		}
		catch (Exception e) {
			String error = "Error while dispatching query: " +e.getMessage();
			resolveUriResult.setError(error);
			return;
		}

		if ( log.isDebugEnabled() ) {
			log.debug("RESULT=" +result);
		}

		boolean ok = true;

		CSVReader reader = new CSVReader(new StringReader(result));
		List<String[]> lines;
		try {
			lines = reader.readAll();
		}
		catch (IOException e) {
			// Should not happen.
			String error = "Error while parsing CSV: " +e.getMessage();
			resolveUriResult.setError(error);
			return;
		}

		if (lines.size() > 0) {
			String[] line = lines.get(0);
			if (line.length > 0 && line[0].toLowerCase().startsWith("error:") ) {
				ok = false;
			}
		}

		if ( ok ) {
			EntityInfo entityInfo = new EntityInfo();

			for (String[] toks : lines) {
				if ( toks.length != 2 ) {
					continue;
				}
				String prop = _removeBrackets(toks[0]);
				String value = _removeBrackets(toks[1]);

				if ("prop".equals(prop) && "value".equals(value)) {
					continue;
				}

				Resource propResource = ResourceFactory.createResource(prop);

				PropValue pv = new PropValue();
				pv.setPropName(propResource.getLocalName());
				pv.setPropUri(prop);

				if ( _isAbsoluteUri(value) ) {
					pv.setValueUri(value);
					Resource objResource = ResourceFactory.createResource(value);
					pv.setValueName(objResource.getLocalName());
				}
				else {
					pv.setValueName(value);
				}

				entityInfo.getProps().add(pv);
			}

			int size = entityInfo.getProps().size();
			if ( size > 0 ) {
				entityInfo.setUri(uri);
				if ( log.isDebugEnabled() ) {
					log.debug("Added " +size+ " property/value pairs to " +uri);
				}
				resolveUriResult.setEntityInfo(entityInfo);
			}
			else {
				ok = false;
			}
		}

		if ( ! ok ) {
			// is it at least a valid URL?
			resolveUriResult.setIsUrl(_isUrl(uri));
		}
	}

	/**
	 * Removes leading '<' and trailing '>' if any.
	 */
	private static String _removeBrackets(String string) {
		if (string.matches("<[^>]*>")) {
			return string.substring(1, string.length() -1);
		}
		else{
			return string;
		}
	}

	/**
	 * Does the string represent an absolute URI?
	 */
	private static boolean _isAbsoluteUri(String string) {
		try {
			URI jUri = new URI(string);
			return jUri.isAbsolute();
		}
		catch (URISyntaxException ignore) {
		}
		return false;
	}

	/**
	 * Does the string represent a valid URL?
	 */
	private static boolean _isUrl(String string) {
		try {
			// is it at least a valid URL?
			new URL(string);
			return true;
		}
		catch (MalformedURLException ignore) {
		}
		return false;
	}

	public ExternalOntologyInfo getExternalOntologyInfo(String ontologyUri) {

		log.debug("getExternalOntologyInfo: ontologyUri=" +ontologyUri);

		ExternalOntologyInfo oi = new ExternalOntologyInfo();

		oi.setUri(ontologyUri);
		oi.setDisplayLabel(ontologyUri);

		String error = null;
		Exception ex = null;
		try {
			// 298: Load of external ontology for mapping does not use content negotiation
			// first load the model with this new supporting routine:
			OntModel ontModel = OntInfoUtil.loadExternalModel(ontologyUri);
			OntInfoUtil.getEntities(oi, ontModel);
		}
		catch (FileNotFoundException e) {
			error = "File not found: '" +ontologyUri+ "'";
		}
		catch (UnknownHostException e) {
			error = "Unknown host: '" +ontologyUri+ "'";
		}
		catch (Exception e) {
			ex = e;
			error = e.getClass().getName() + ": " + e.getMessage();
		}
		if (log.isDebugEnabled() && ex != null) {
			log.debug("Error getting ExternalOntologyInfo: " +ontologyUri, ex);
		}
		oi.setError(error);
		return oi;
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
				return _getOntologyInfoFromMmiUri(ontologyUri, mmiUri, version);
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
				return _getOntologyInfoWithVersionParams(ontologyUri, unversOntologyUri, version, includeAllVersions);
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

	private RegisteredOntologyInfo _getOntologyInfoFromMmiUri(String ontologyUri, MmiUri mmiUri, String version) {
		try {
			// get elements associated with the unversioned form of the requested URI:
			String unversOntologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
			boolean includeAllVersions = true;
			return _getOntologyInfoWithVersionParams(ontologyUri, unversOntologyUri, version, includeAllVersions);
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
	private RegisteredOntologyInfo _getOntologyInfoWithVersionParams(String ontologyUri, String unversOntologyUri,
			String version, boolean includeAllVersions) throws Exception {

		// first, get list of entries for the requested ontology using the unversioned form as key:
		Map<String, List<RegisteredOntologyInfo>> unversionedToVersioned = _getUnversionedToOntologyInfoListMap(unversOntologyUri);

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
			if ( log.isDebugEnabled() ) {
				log.debug(getClass().getSimpleName()+ "getOntologyInfoWithVersionParams case a) version = " +version);
			}
			//  a) explicit version given: search for exact match using the 'version' field:
			for ( RegisteredOntologyInfo oi : list ) {
				if ( version.equals(oi.getVersionNumber()) ) {
					foundRoi = oi;
					break;
				}
			}
		}
		else if ( ontologyUri.equals(unversOntologyUri) ) {
			if ( log.isDebugEnabled() ) {
				log.debug(getClass().getSimpleName()+ "getOntologyInfoWithVersionParams case b) unversioned request = " +unversOntologyUri);
			}
			// b) unversioned URI request, eg., http://mmisw.org/ont/seadatanet/qualityFlag
			// just return first entry in list

			// do not alter the first entry in the list!
//			RegisteredOntologyInfo oi = list.get(0);
//			oi.setUri(oi.getUnversionedUri());
//			foundRoi = oi;

			// here is how it should be done:
			RegisteredOntologyInfo mostRecent = list.get(0);
			foundRoi = _createOntologyInfo(
					mostRecent.getUnversionedUri(),      // NOTE: UnversionedURI for the URI
					mostRecent.getDisplayLabel(),
					mostRecent.getType(),
					mostRecent.getUserId(),
					mostRecent.getContactName(),
					mostRecent.getVersionNumber(),
					mostRecent.getDateCreated(),
					mostRecent.getUsername(),
					mostRecent.getOntologyId(),
					mostRecent.getVersionStatus(),

					mostRecent.getUnversionedUri(),
					mostRecent.getAuthority(),
					mostRecent.getShortName()
			);

		}
		else {
			if ( log.isDebugEnabled() ) {
				log.debug(getClass().getSimpleName()+ ".getOntologyInfoWithVersionParams case c) versioned request = " +ontologyUri);
			}
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


	public RegisteredOntologyInfo getOntologyMetadata(RegisteredOntologyInfo registeredOntologyInfo, String version) {

		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyMetadata(RegisteredOntologyInfo): loading model");
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
			log.debug("getOntologyMetadata(RegisteredOntologyInfo): getting metadata");
		}
		MetadataExtractor.prepareOntologyMetadata(metadataBaseInfo, ontModel, registeredOntologyInfo);

		registeredOntologyInfo.setSize(ontModel.size());
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyMetadata(RegisteredOntologyInfo): ontology size=" + registeredOntologyInfo.getSize());
		}

		return registeredOntologyInfo;
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
		// NOTE: the metadata are retrieved here regardless of whether have been already retrieved.
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(RegisteredOntologyInfo): getting metadata");
		}
		MetadataExtractor.prepareOntologyMetadata(metadataBaseInfo, ontModel, registeredOntologyInfo);


		// Data
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(RegisteredOntologyInfo): getting entities");
		}

		try {
			OntInfoUtil.getEntities(registeredOntologyInfo, ontModel);
		}
		catch (Exception e) {
			String error = "Error getting entities: " +e.getMessage();
			log.error(error, e);
			registeredOntologyInfo.setError(error);
			return registeredOntologyInfo;
		}

		return registeredOntologyInfo;

	}


	private static String _getAsString(String uri, int maxlen) throws Exception {
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


	// TODO this mechanism copied from MmiUri (in ont project).
	private static final Pattern VERSION_PATTERN =
				Pattern.compile("^\\d{4}(\\d{2}(\\d{2})?)?(T\\d{2})?(\\d{2}(\\d{2})?)?$");


	////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////

	public CreateOntologyResult createOntology(CreateOntologyInfo createOntologyInfo) {

		HostingType hostingType = createOntologyInfo.getHostingType();
		log.info("createOntology: called. hostingType = " +hostingType);

		CreateOntologyResult createOntologyResult = null;

		if ( hostingType != null ) {
			// use of this attribute indicates to use the new method
			createOntologyResult = _createOntology_newMethod(createOntologyInfo);
		}
		else {
			createOntologyResult = _createOntology_oldMethod(createOntologyInfo);
		}

		return createOntologyResult;
	}


	private CreateOntologyResult _createOntology_newMethod(CreateOntologyInfo createOntologyInfo) {

		final HostingType hostingType = createOntologyInfo.getHostingType();

		CreateOntologyResult createOntologyResult = new CreateOntologyResult();

		if ( createOntologyInfo.getMetadataValues() == null ) {
			String error = "Unexpected: createOntologyInfo.getMetadataValues returned null. Please report this bug";
			createOntologyResult.setError(error);
			log.info(error);
			return createOntologyResult;
		}

		createOntologyResult.setCreateOntologyInfo(createOntologyInfo);

		switch ( hostingType ) {
			case FULLY_HOSTED:
				return _createOntologyFullyHosted(createOntologyInfo, createOntologyResult);
			case RE_HOSTED:
				return _createOntologyReHosted(createOntologyInfo, createOntologyResult);
			default: {
				String error = "Hosting type "+hostingType+ " NOT yet implemented.";
				createOntologyResult.setError(error);
				log.info(error);
				return createOntologyResult;
			}
		}
	}


	private CreateOntologyResult _createOntologyFullyHosted(CreateOntologyInfo createOntologyInfo, CreateOntologyResult createOntologyResult) {

		final String createOntUri = createOntologyInfo.getUri();
		log.debug("#356 _createOntologyFullyHosted; createOntologyInfo.getUri()=" + createOntUri);

		Map<String, String> newValues = createOntologyInfo.getMetadataValues();
		assert ( newValues != null ) ;

		DataCreationInfo dataCreationInfo = createOntologyInfo.getDataCreationInfo();
		assert ( dataCreationInfo instanceof OtherDataCreationInfo ) ;
		final OtherDataCreationInfo otherDataCreationInfo = (OtherDataCreationInfo) dataCreationInfo;
		final TempOntologyInfo tempOntologyInfo = otherDataCreationInfo.getTempOntologyInfo();


		// to check if this is going to be a new submission (if ontologyId == null) or, otherwise, a new version.
		final String ontologyId = createOntologyInfo.getPriorOntologyInfo().getOntologyId();


		final String namespaceRoot = defaultNamespaceRoot;
		final String orgAbbreviation = createOntologyInfo.getAuthority();
		final String shortName = createOntologyInfo.getShortName();


		if ( orgAbbreviation == null ) {
			// should not happen.
			String error = "missing authority abbreviation";
			log.info(error);
			createOntologyResult.setError(error);
			return createOntologyResult;
		}
		if ( shortName == null ) {
			// should not happen.
			String error = "missing short name";
			log.info(error);
			createOntologyResult.setError(error);
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

			BaseOntologyInfo baseOntologyInfo = createOntologyInfo.getBaseOntologyInfo();
			assert baseOntologyInfo instanceof RegisteredOntologyInfo;
			RegisteredOntologyInfo roi = (RegisteredOntologyInfo) baseOntologyInfo;

			String originalOrgAbbreviation = roi.getAuthority();
			String originalShortName = roi.getShortName();

			if ( ! Util2.checkUriKeyCombinationForNewVersion(
					originalOrgAbbreviation, originalShortName,
					orgAbbreviation, shortName, createOntologyResult) ) {
				return createOntologyResult;
			}
		}


		////////////////////////////////////////////////////////////////////////////
		// section to create the ontology the base:

		// external ontology case: the base ontology is already available, just use it
		// by setting the full path in the createOntologyResult:

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
			full_path = createOntologyResult.getFullPath();
			log.info("Loading model: " +full_path);

			File file = new File(full_path);

			try {
				model = Util2.loadModelWithCheckingUtf8(file, null);
			}
			catch ( Throwable ex ) {
				String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
				log.info(error);
				createOntologyResult.setError(error);
				return createOntologyResult;
			}

			// get original namespace associated with the ontology, if any:
			uriForEmpty = Util2.getDefaultNamespace2(model, file, createOntologyResult);
			// 2009-12-21: previously returning error if uriForEmpty==null. Not anymore; see below.

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


		final String original_ns_ = uriForEmpty;

		String ns_;
		String base_;


		final String finalUri = namespaceRoot + "/" +
		orgAbbreviation + "/" +
		version + "/" +
		shortName;

		ns_ = JenaUtil2.appendFragment(finalUri);
		base_ = JenaUtil2.removeTrailingFragment(finalUri);


		log.info("Setting prefix \"\" for URI " + ns_);
		model.setNsPrefix("", ns_);


		if ( original_ns_ != null ) {
			// Update statements  according to the new namespace:
			log.info("createOntologyFullyHosted: original namespace: '" +uriForEmpty+ "'. " +
				"Elements here will be transferred to new namespace " +ns_
			);
			Util2.replaceNameSpace(model, original_ns_, ns_);
		}
		else {
			log.info("createOntologyFullyHosted: no original namespace, so no transfer will be done.");
		}




		/////////////////////////////////////////////////////////////////
		// Is there an existing OWL.Ontology individual?
		Resource ontRes = null;
		if ( original_ns_ != null ) {
			String originalOntologyUri = JenaUtil2.removeTrailingFragment(original_ns_);
			ontRes = model.getOntology(originalOntologyUri);
			log.debug("#356: createOntologyFullyHosted: model.getOntology(" +originalOntologyUri+ ") = " + ontRes);
		}
		//else {
		//  // #356: don't do this; arbitrarily some OWL.Ontology individual would be considered
		//	ontRes = JenaUtil2.getFirstIndividual(model, OWL.Ontology);
		//}

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
		final OntModel newOntModel = _createOntModel(model);
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
		newValues.put(Omv.version.getURI(), version);

		newValues.put(Omv.creationDate.getURI(), creationDate);


		// set some properties from the explicit values
		newValues.put(OmvMmi.origMaintainerCode.getURI(), orgAbbreviation);


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
		JenaUtil2.removeUnusedNsPrefixes(model);
		String rdf = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV") ;  // XXX newOntModel);

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

		_writeRdfToFile(rdf, reviewedFile, createOntologyResult);

		// Done.

		return createOntologyResult;
	}

	private OntModel _createOntModel(OntModel ontModel) {
		ontModel = ModelFactory.createOntologyModel(ontModel.getSpecification(), ontModel);
		return ontModel;
	}



	private CreateOntologyResult _createOntologyReHosted(CreateOntologyInfo createOntologyInfo, CreateOntologyResult createOntologyResult) {

		Map<String, String> newValues = createOntologyInfo.getMetadataValues();
		assert ( newValues != null ) ;

		DataCreationInfo dataCreationInfo = createOntologyInfo.getDataCreationInfo();
		assert ( dataCreationInfo instanceof OtherDataCreationInfo ) ;
		final OtherDataCreationInfo otherDataCreationInfo = (OtherDataCreationInfo) dataCreationInfo;
		final TempOntologyInfo tempOntologyInfo = otherDataCreationInfo.getTempOntologyInfo();


		// to check if this is going to be a new submission (if ontologyId == null) or, otherwise, a new version.
		final String ontologyId = createOntologyInfo.getPriorOntologyInfo().getOntologyId();

		final String ontUri = createOntologyInfo.getUri();


		////////////////////////////////////////////////////////////////////////////
		// section to create the ontology the base:

		// external ontology case: the base ontology is already available, just use it
		// by setting the full path in the createOntologyResult:

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
		Ontology ont;
		String uriForEmpty;
		String newContentsFileName;

		if ( createOntologyResult.getFullPath() != null ) {
			//
			// new contents to check.
			// Get model from the new contents.
			//
			full_path = createOntologyResult.getFullPath();
			log.info("Loading model: " +full_path);

			File file = new File(full_path);

			try {
				model = Util2.loadModelWithCheckingUtf8(file, null);
			}
			catch ( Throwable ex ) {
				String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
				log.info(error);
				createOntologyResult.setError(error);
				return createOntologyResult;
			}

			// fix #354: get ontology resource by ontUri from the model:
			ont = model.getOntology(ontUri);
			if (ont != null) {
				if (log.isDebugEnabled()) {
					log.debug("#354: _createOntologyReHosted: got ontology object by ontUri=" + ontUri);
				}
				uriForEmpty = ontUri;
			}
			else {
				uriForEmpty = null;
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

			ont = model.getOntology(ontUri);  // #356 get Ontology for the specific URI
			if ( ont == null ) {
				// Shouldn't happen -- we're reading in an already registered version.
				String error = "#356: _createOntologyReHosted: dit not get ontology object from previous " +
						"version by ontUri=" + ontUri + ". Please report this bug.";
				log.info(error);
				createOntologyResult.setError(error);
				return createOntologyResult;
			}
			uriForEmpty = ont.getURI();

			// replace ':', '/', or '\' for '_'
			newContentsFileName = uriForEmpty.replaceAll(":|/|\\\\", "_");
		}

		final String original_base_ = uriForEmpty == null ? null : JenaUtil2.removeTrailingFragment(uriForEmpty);

		final String base_ = JenaUtil2.removeTrailingFragment(ontUri);

		log.info("createOntologyReHosted: original namespace: " +original_base_);
		if ( original_base_ != null && ! original_base_.equals(base_) ) {
			// In this re-hosted case, we force the original URI and the new URI to be the same.
			// This may happen only in the case of a submission of a new version.
			String error = "The new base URI (" +base_+ ") is not equal to the registered " +
				"base URI (" +original_base_+ ") "
			;
			log.debug(error);
			createOntologyResult.setError(error);
			return createOntologyResult;
		}

		/////////////////////////////////////////////////////////////////
		// If there is an pre-existing Ontology resource, get the associated statements:
		List<Statement> prexistStatements = null;
		if ( ont != null ) {
			prexistStatements = new ArrayList<Statement>();
			if ( log.isDebugEnabled() ) {
				log.debug("Getting pre-existing properties from Ontology: " +ont.getURI());
			}
			StmtIterator iter = ont.listProperties();
			while ( iter.hasNext() ) {
				Statement st = iter.nextStatement();
				prexistStatements.add(st);
			}
		}


		// The new OntModel that will contain the pre-existing attributes (if any),
		// plus the new and updated attributes:
		final OntModel newOntModel = OntModelUtil.createOntModel(base_, model);
		final Ontology ont_ = newOntModel.getOntology(ontUri);
		if ( log.isDebugEnabled() ) {
			log.debug("New ontology created with namespace " + newOntModel.getNsPrefixURI("") + " base " + base_);
		}

		// Set internal attributes, which are updated in the newValues map itself
		// so we facilite the processing below:
		newValues.put(Omv.version.getURI(), version);
		newValues.put(Omv.creationDate.getURI(), creationDate);


		//////////////////////////////////////////////////
		// transfer any preexisting attributes, and then remove all properties from
		// pre-existing ont so just the new OntModel gets added.
		if ( prexistStatements != null ) {
			for ( Statement st : prexistStatements ) {
				Property prd = st.getPredicate();

				//
				// Do not tranfer pre-existing/pre-assigned-above attributes
				//
				String newValue = newValues.get(prd.getURI());
				if ( newValue == null || newValue.trim().length() == 0 ) {
					if ( log.isDebugEnabled() ) {
						log.debug("  Transferring: " +st.getSubject()+ " :: " +prd+ " :: " +st.getObject());
					}
					newOntModel.add(ont_, prd, st.getObject());
				}
				else {
					if ( log.isDebugEnabled() ) {
						log.debug(" Removing pre-existing values for predicate: " +prd+ " because of new value " +newValue);
					}
					newOntModel.removeAll(ont_, prd, null);
				}
			}

		}



		///////////////////////////////////////////////////////
		// Update attributes in model:

		Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
		for ( String uri : newValues.keySet() ) {
			String value = newValues.get(uri);
			if ( value != null && value.trim().length() > 0 ) {
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
		JenaUtil2.removeUnusedNsPrefixes(model);
		String rdf = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV") ;  // XXX newOntModel);

		if ( log.isDebugEnabled() ) {
			log.debug(rdf);
		}

		log.debug("createOntology: setting URI: " +base_);
		createOntologyResult.setUri(base_);


		// write new contents to a new file under previewDir:

		File reviewedFile = new File(previewDir, newContentsFileName);
		createOntologyResult.setFullPath(reviewedFile.getAbsolutePath());

		_writeRdfToFile(rdf, reviewedFile, createOntologyResult);

		// Done.

		return createOntologyResult;
	}




	private void _writeRdfToFile(String rdf, File reviewedFile, CreateOntologyResult createOntologyResult) {
		PrintWriter os;
		try {
			// #43: Handle non-UTF8 inputs
			// keep "UTF-8" character for consistency:
			os = new PrintWriter(reviewedFile, "UTF-8");
		}
		catch (FileNotFoundException e) {
			String error = "Unexpected: cannot open file for writing: " +reviewedFile;
			log.info(error);
			createOntologyResult.setError(error);
			return;
		}
		catch (UnsupportedEncodingException e) {
			String error = "Unexpected: cannot create file in UTF-8 encoding: " +reviewedFile;
			log.info(error);
			createOntologyResult.setError(error);
			return;
		}
		StringReader is = new StringReader(rdf);
		try {
			IOUtils.copy(is, os);
			os.flush();
		}
		catch (IOException e) {
			String error = "Unexpected: IO error while writing to: " +reviewedFile;
			log.info(error);
			createOntologyResult.setError(error);
			return;
		}
	}


	// TODO remove when new mechanism is in place.
	@SuppressWarnings("deprecation")
	private CreateOntologyResult _createOntology_oldMethod(CreateOntologyInfo createOntologyInfo) {

		log.warn("~~~~CALLED _createOntology_oldMethod");

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
		String shortName = newValues.get(Omv.acronym.getURI());
		// TODO: shortName taken NOT from acronym but from a new field explicitly for the shortName piece

		if ( ! createOntologyResult.isPreserveOriginalBaseNamespace() ) {
			//pons: check the following if regular assignment of namespace

			if ( orgAbbreviation == null ) {
				log.info("missing origMaintainerCode");
				createOntologyResult.setError("missing origMaintainerCode");
				return createOntologyResult;
			}

			if ( shortName == null ) {

				if ( ontologyId == null ) {
					// This is a new submission.
					log.info("missing acronym (to be used as shortName)");
					createOntologyResult.setError("missing acronym (to be used as shortName)");
					return createOntologyResult;
				}
				else {
					// take from previous information
					String originalShortName = createOntologyInfo.getShortName();
					shortName = originalShortName;
				}
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
				model = Util2.loadModelWithCheckingUtf8(file, null);
			}
			catch ( Throwable ex ) {
				String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
				log.info(error);
				createOntologyResult.setError(error);
				return createOntologyResult;
			}

			uriForEmpty = Util2.getDefaultNamespace2(model, file, createOntologyResult);
			log.debug("#356 _createOntology_oldMethod: now using getDefaultNamespace2, got uriForEmpty=" + uriForEmpty);

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
		Resource ontRes = JenaUtil2.getFirstIndividual(model, OWL.Ontology);
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
		final OntModel newOntModel = _createOntModel(model);
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
		JenaUtil2.removeUnusedNsPrefixes(model);
		String rdf = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV") ;  // XXX newOntModel);

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

		_writeRdfToFile(rdf, reviewedFile, createOntologyResult);

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


	public RegisterOntologyResult registerOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult) {
		RegisterOntologyResult registerOntologyResult = null;

        CreateOntologyInfo createOntologyInfo = createOntologyResult.getCreateOntologyInfo();
		if ( createOntologyInfo.getHostingType() != null ) {
			// use of this attribute indicates to use the new method
			registerOntologyResult = registerOntology_newMethod(createOntologyResult, loginResult);
		}
		else {
			registerOntologyResult = registerOntology_oldMethod(createOntologyResult, loginResult);
		}

        if (registerOntologyResult.getError() == null) {
            /////////////////////////////////////////////////////////////////////////
            // send email to notify successful registration
            // (this is very ad hoc -- will be more configurable in a next version)
            /////////////////////////////////////////////////////////////////////////

            // TODO update to reuse auxiliary methods introduced later

            final Set<String> recipients = new LinkedHashSet<String>();

						final String notifyEmailsFilename = OrrConfig.instance().notifyEmailsFilename;
						if (notifyEmailsFilename != null) {
							try {
								File f = new File(notifyEmailsFilename);
								FileInputStream is = new FileInputStream(f);
								for (Object line : IOUtils.readLines(is)) {
									String email = String.valueOf(line).trim();
									if (email.length() > 0 && !email.startsWith("#")) {
										recipients.add(email);
									}
								}
								IOUtils.closeQuietly(is);
							}
							catch (Exception e) {
								log.warn("could not read in: " + notifyEmailsFilename, e);
							}
						}
            if (recipients.size() > 0) {
                final Map<String, String> data = new LinkedHashMap<String, String>();
                try {
                    String ontologyUri = registerOntologyResult.getUri();
                    String version = null;
                    String withUriParam = null;

                    if ( OntServiceUtil.isOntResolvableUri(ontologyUri) ) {
                        // prefer to show the unversioned URI
                        try {
                            MmiUri mmiUri = new MmiUri(ontologyUri);
                            version = mmiUri.getVersion();
                            ontologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
                        }
                        catch (URISyntaxException ignore) {
                        }
                    }
                    else {
                        withUriParam = OrrConfig.instance().ontServiceUrl + "?uri=" + ontologyUri;
                    }

                    data.put("URI", ontologyUri);
                    if (version != null) {
                        data.put("version", version);
                    }
                    if (withUriParam != null) {
                        data.put("Resolve with", withUriParam);
                    }
                    data.put("Registered by",  loginResult.getUserName());
                    BaseOntologyInfo baseOntologyInfo = createOntologyInfo.getBaseOntologyInfo();
                    if (baseOntologyInfo != null) {
                        String displayLabel = baseOntologyInfo.getDisplayLabel();
                        if (displayLabel != null) {
                            data.put("DisplayLabel", baseOntologyInfo.getDisplayLabel());
                        }
                        long size = baseOntologyInfo.getSize();
                        if (size > 0) {
                            data.put("Size", String.valueOf(size));
                        }
                    }
                    Thread t = new Thread() {
                        public void run() {
                            final String mail_user     = OrrConfig.instance().emailUsername;
                            final String mail_password = OrrConfig.instance().emailPassword;
                            if ( mail_user == null || mail_user.equals("-") ) {
                                String error = "Email server account not configured. Please report this bug. (u)";
                                log.error(error);
                                return;
                            }
                            if ( mail_password == null  || mail_password.equals("-") ) {
                                String error = "Email server account not configured. Please report this bug. (p)";
                                log.error(error);
                                return;
                            }

                            boolean debug = false;
                            final String from    = OrrConfig.instance().emailFrom;
                            final String replyTo = OrrConfig.instance().emailReplyTo;
                            final String subject = "Ontology registered";

                            String text = "The following ontology has been registered:\n";
                            for (String key: data.keySet()) {
                                text += "\n  " + key + ": " + data.get(key);
                            }
                            text += "\n\n";
                            text += "(you have received this email because your address is included in " +notifyEmailsFilename + ")";

                            String to = "", comma = "";
                            for (String recipient : recipients) {
                                to += comma + recipient;
                                comma = ",";
                            }
                            log.debug("sending email to notify ontology registration: " +data+ "; recipients: " +to);
                            try {
                                MailSender.sendMessage(mail_user, mail_password, debug, from, to, replyTo, subject, text);
                                log.debug("email sent to notify Ontology registered: " + data + "; recipients: " + to);
                            }
                            catch (Exception e) {
                                String error = "Error sending email to notify ontology registered: " + data
                                        + "; recipients: " +to+ ": " +e.getMessage();
                                log.error(error, e);
                            }
                        }
                    };
                    t.setDaemon(true);
                    t.start();
                }
                catch (Exception e) {
                    log.error("Error preparing to send email to notify Ontology registered: " + data
                              + ": " +e.getMessage(), e);
                }
            }
            else {
                log.debug("no recipients to send email: " + notifyEmailsFilename);
            }
        }

		return registerOntologyResult;

	}

	public RegisterOntologyResult registerOntology_newMethod(CreateOntologyResult createOntologyResult, LoginResult loginResult) {
		final HostingType hostingType = createOntologyResult.getCreateOntologyInfo().getHostingType();

		log.info("registerOntology: called. hostingType = " +hostingType);
		RegisterOntologyResult registerOntologyResult = new RegisterOntologyResult();

		switch ( hostingType ) {
			case FULLY_HOSTED:
				return registerOntologyFullyHosted(createOntologyResult, registerOntologyResult, loginResult);
			case RE_HOSTED:
				return registerOntologyReHosted(createOntologyResult, registerOntologyResult, loginResult);
			default: {
				String error = "Hosting type "+hostingType+ " NOT yet implemented.";
				registerOntologyResult.setError(error);
				log.info(error);
				return registerOntologyResult;
			}
		}

	}

	public RegisterOntologyResult registerOntologyFullyHosted(CreateOntologyResult createOntologyResult, RegisterOntologyResult registerOntologyResult, LoginResult loginResult) {

		String full_path = createOntologyResult.getFullPath();

		log.info("registerOntology: Reading in temporary file: " +full_path);

		File file = new File(full_path);

		// Get resulting model:
		String rdf;
		try {
//			rdf = Util2.readRdf(file);
			rdf = Util2.readRdfWithCheckingUtf8(file);
		}
		catch (Throwable e) {
			String error = "Unexpected: error while reading from: " +full_path+ " : " +e.getMessage();
			log.info(error);
			registerOntologyResult.setError(error);
			return registerOntologyResult;
		}

		// ok, we have our ontology:


		//////////////////////////////////////////////////////////////////////////
		// finally, do actual registration to MMI registry

		// Get final URI of resulting model
		final String uri = createOntologyResult.getUri();
		assert uri != null;
		assert loginResult.getUserId() != null;
		assert loginResult.getSessionId() != null;

		log.info(": registering URI: " +uri+ " ...");

		CreateOntologyInfo createOntologyInfo = createOntologyResult.getCreateOntologyInfo();

		String ontologyId = createOntologyInfo.getPriorOntologyInfo().getOntologyId();
		String ontologyUserId = createOntologyInfo.getPriorOntologyInfo().getOntologyUserId();

		if ( ontologyId != null ) {
			log.info("Will create a new version for ontologyId = " +ontologyId+ ", userId=" +ontologyUserId);
		}


		Map<String, String> newValues = createOntologyInfo .getMetadataValues();


		try {
			// this is to get the filename for the registration
			String fileName = new URL(uri).getPath();

			//
			// make sure the fileName ends with ".owl" as the aquaportal back-end seems
			// to add that fixed extension in some operations (at least in the parse operation)
			//
			if ( ! fileName.toLowerCase().endsWith(".owl") ) {
				if ( log.isDebugEnabled() ) {
					log.debug("register: setting file extension to .owl per aquaportal requirement.");
				}
				fileName += ".owl";
			}


			if ( ontologyId == null ) {
				//
				// Submission of a new ontology (not version of a registered one)
				//

				// We are about to do the actual registration. But first, re-check that there is NO a preexisting
				// ontology that may conflict with this one.
				// NOTE: this check has been done already in the review operation; however, we repeat it here
				// in case there is a new registration done by other user in the meantime. Of course, we
				// are NOT completely solving the potential concurrency problem with this re-check; we are just
				// reducing the chances of that event.


				// TODO: the following can be simplified by obtaining the unversioned form of the URI and
				// calling Util2.checkNoPreexistingOntology(unversionedUri, registerOntologyResult)

				final String namespaceRoot = newValues.get("namespaceRoot") != null
						? newValues.get("namespaceRoot")
						:  defaultNamespaceRoot;

				final String orgAbbreviation = createOntologyInfo.getAuthority();
				final String shortName = createOntologyInfo.getShortName();

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

			// OK, now do the actual registration:
            String res = uploadOntology(uri, fileName, rdf,
                    loginResult,
                    ontologyId, ontologyUserId,
                    newValues
            );

			if ( res.startsWith("OK") ) {
				registerOntologyResult.setUri(uri);
				registerOntologyResult.setInfo(res);

				// issue #168 fix:
				// request that the ontology be loaded in the "ont" graph:
				OntServiceUtil.loadOntologyInGraph(uri, null);
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


	// TODO
	public RegisterOntologyResult registerOntologyReHosted(CreateOntologyResult createOntologyResult, RegisterOntologyResult registerOntologyResult, LoginResult loginResult) {

		String full_path = createOntologyResult.getFullPath();

		log.info("registerOntology: Reading in temporary file: " +full_path);

		File file = new File(full_path);

		// Get resulting model:
		String rdf;
		try {
			rdf = Util2.readRdfWithCheckingUtf8(file);
		}
		catch (Throwable e) {
			String error = "Unexpected: error while reading from: " +full_path+ " : " +e.getMessage();
			log.info(error);
			registerOntologyResult.setError(error);
			return registerOntologyResult;
		}

		// ok, we have our ontology:


		//////////////////////////////////////////////////////////////////////////
		// finally, do actual registration to MMI registry

		// Get final URI of resulting model
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


			// OK, now do the actual registration:
            String res = uploadOntology(uri, fileName, rdf,
                    loginResult,
                    ontologyId, ontologyUserId,
                    newValues
            );

			if ( res.startsWith("OK") ) {
				registerOntologyResult.setUri(uri);
				registerOntologyResult.setInfo(res);

				// issue #168 fix:
				// request that the ontology be loaded in the "ont" graph:
				OntServiceUtil.loadOntologyInGraph(uri, null);
			}
			else {
				registerOntologyResult.setError(res);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			registerOntologyResult.setError(ex.getClass().getName()+ ": " +ex.getMessage());
		}

		if ( log.isDebugEnabled() ) {
			log.debug("registerOntologyResult = " +registerOntologyResult);
		}

		return registerOntologyResult;
	}


	public RegisterOntologyResult registerOntology_oldMethod(CreateOntologyResult createOntologyResult, LoginResult loginResult) {

		log.warn("~~~~CALLED registerOntology_oldMethod");

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
//			rdf = Util2.readRdf(file);
			rdf = Util2.readRdfWithCheckingUtf8(file);
		}
		catch (Throwable e) {
			String error = "Unexpected: error while reading from: " +full_path+ " : " +e.getMessage();
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
            String res = uploadOntology(uri, fileName, rdf,
					loginResult,
					ontologyId, ontologyUserId,
					newValues
			);

			if ( res.startsWith("OK") ) {
				registerOntologyResult.setUri(uri);
				registerOntologyResult.setInfo(res);

				// issue #168 fix:
				// request that the ontology be loaded in the "ont" graph:
				OntServiceUtil.loadOntologyInGraph(uri, null);
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




	public TempOntologyInfo getTempOntologyInfo(
			String fileType, String filename,
			boolean includeContents, boolean includeRdf
	) {
		TempOntologyInfo tempOntologyInfo = new TempOntologyInfo();

		if ( metadataBaseInfo == null ) {
			tempOntologyInfo.setError(OrrClientImpl.class.getSimpleName()+ " not properly initialized. Please report this bug. (metadataBaseInfo not initialized)");
			return tempOntologyInfo;
		}

		TempOntologyHelper tempOntologyHelper = new TempOntologyHelper(metadataBaseInfo);
		tempOntologyHelper.getTempOntologyInfo(fileType, filename, tempOntologyInfo, includeRdf);

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
			ontModel = JenaUtil2.loadModel(uriFile, false);
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
			OntInfoUtil.getEntities(tempOntologyInfo, ontModel);
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

	public List<RelationInfo> getDefaultVineRelationInfos() {
		if ( log.isDebugEnabled() ) {
			log.debug("getDefaultVineRelationInfos starting");
		}

		List<RelationInfo> relInfos = VineUtil.getDefaultVineRelationInfos();

		if ( log.isDebugEnabled() ) {
			log.debug("getDefaultVineRelationInfos returning: " +relInfos);
		}

		return relInfos;
	}


	////////////////////////////////////////////////////////////////////////////////////////////
	// Search:

	public SparqlQueryResult runSparqlQuery(SparqlQueryInfo query) {
		SparqlQueryResult sparqlQueryResult = new SparqlQueryResult();
		try {
			String result;
			if ( query.getAcceptEntries().length == 0 ) {
				// this is the call used for previous version of the SparqlQueryInfo object, which didn't
				// have the acceptEntries attribute
				result = OntServiceUtil.runSparqlQuery(query.getQuery(), query.getFormat(), "application/rdf+xml");
			}
			else {
				// use the new call:
				result = OntServiceUtil.runSparqlQuery(query);
			}
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
			SignInResult signInResult = ontClient.getSession(userName, userPassword);
			loginResult.setSessionId(signInResult.getSessionId());
			loginResult.setUserId(signInResult.getUserId());
			loginResult.setUserName(signInResult.getUserName());
			loginResult.setUserRole(signInResult.getUserRole());
		}
		catch (Exception ex) {
			loginResult.setError(ex.getMessage());
		}

		return loginResult;

	}

	public ResetPasswordResult resetUserPassword(String username) {
		ResetPasswordResult result = new ResetPasswordResult();

		// Issue 258:"reset password in effect but lost if error while sending email"
		// Part of the fix is to first check the mail account parameters are given:
		final String mail_user     = OrrConfig.instance().emailUsername;
		final String mail_password = OrrConfig.instance().emailPassword;
		if ( mail_user == null || mail_user.equals("-") ) {
			String error = "Email server account not configured. Please report this bug. (u)";
			result.setError(error);
			log.error(error);
			return result;
		}
		if ( mail_password == null  || mail_password.equals("-") ) {
			String error = "Email server account not configured. Please report this bug. (p)";
			result.setError(error);
			log.error(error);
			return result;
		}

		///////////////////////////////////////////////////////////////
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

		///////////////////////////////////////////////////////////////
		// get new password
		String newPassword = Util2.generatePassword();

		///////////////////////////////////////////////////////////////
		// update password in back-end
		Map<String, String> values = userInfoResult.getProps();
		values.put("id", userInfoResult.getProps().get("id"));
		values.put("password", newPassword);
		values.put("sessionid", "4444444444444");
		CreateUpdateUserAccountResult updatePwResult = createUpdateUserAccount(values);
		if ( updatePwResult.getError() != null ) {
			result.setError(updatePwResult.getError());
			return result;
		}


		///////////////////////////////////////////////////////////////
		// send email with new password

		boolean debug = false;
		final String from = OrrConfig.instance().emailFrom;
		final String replyTo = OrrConfig.instance().emailReplyTo;
		final String subject = "Password reset";
		final String text = "Your password has been reset.\n" +
				"\n" +
				"Username: " +username+ "\n" +
				"   email: " +email+ "\n" +
				"Password: " +newPassword+ "\n" +
				"\n"
		;

		try {
			MailSender.sendMessage(mail_user, mail_password, debug , from, email, replyTo, subject, text);
			result.setEmail(email);
		}
		catch (Exception e) {
			String error = "Error sending email. Please try again later." +
					"<br/>" +
					"If the problem persists please contact the administrator." +
					"<br/>" +
					"Error: " +e.getMessage();
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


	public CreateUpdateUserAccountResult createUpdateUserAccount(Map<String,String> values) {
		CreateUpdateUserAccountResult result = new CreateUpdateUserAccountResult();

		String email = values.get("email");
		if ( email != null ) {
			try {
				new InternetAddress(email, true);
			}
			catch (AddressException e) {
				String error = "Malformed email address: " +e.getMessage();
				result.setError(error);
				return result;
			}
		}

		try {
			SignInResult signInResult = ontClient.createUpdateUserAccount(values);
			final LoginResult loginResult = new LoginResult();
			loginResult.setSessionId(signInResult.getSessionId());
			loginResult.setUserId(signInResult.getUserId());
			loginResult.setUserName(signInResult.getUserName());
			loginResult.setUserRole(signInResult.getUserRole());
			result.setLoginResult(loginResult);

            _notifyUserCreated(loginResult);
		}
		catch (Exception e) {
			String error = "error updating user information: " +
				e.getClass().getName()+ " : " +e.getMessage();
			result.setError(error);
			log.error(error, e);
		}

		return result;

	}

	private Set<String> _getRecipients() {
		final Set<String> recipients = new LinkedHashSet<>();
		String notifyEmailsFilename = OrrConfig.instance().notifyEmailsFilename;
		if (notifyEmailsFilename != null) {
			try {
				File f = new File(notifyEmailsFilename);
				FileInputStream is = new FileInputStream(f);
				for (Object line : IOUtils.readLines(is)) {
					String email = String.valueOf(line).trim();
					if (email.length() > 0 && !email.startsWith("#")) {
						recipients.add(email);
					}
				}
				IOUtils.closeQuietly(is);
			}
			catch (Exception e) {
				log.warn("could not read in: " + notifyEmailsFilename, e);
			}
		}
		if (recipients.size() == 0) {
				log.debug("no recipients to send email: " + notifyEmailsFilename);
		}
			return recipients;
	}

	private void _notifyUserCreated(final LoginResult loginResult) {
        Thread t = new Thread() {
            public void run() {
                final Set<String> recipients = _getRecipients();
                if (recipients.size() == 0) {
                    return;
                }
                final Map<String, String> data = new LinkedHashMap<String, String>();
                String username = loginResult.getUserName();
                data.put("username", username);
                try {
                    Map<String,String> userInfo = ontClient.getUserInfo(username);
                    String[] skip = {"username", "id", "date_created"};
                    for (String k: skip) {
                        if (userInfo.containsKey(k)) {
                            userInfo.remove(k);
                        }
                    }
                    data.putAll(userInfo);
                }
                catch (Exception e) {
                    log.warn("getUserInfo: username=" + username+ ": " +e.getMessage());
                }

                _sendEmail("User created/updated",
                        "The following user account has been created or updated:",
                        data, recipients);
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private void _sendEmail(String subject, String header, Map<String, String> data, Set<String> recipients) {
        final String mail_user     = OrrConfig.instance().emailUsername;
        final String mail_password = OrrConfig.instance().emailPassword;
        if ( mail_user == null || mail_user.equals("-") ) {
            String error = "Email server account not configured. Please report this bug. (u)";
            log.error(error);
            return;
        }
        if ( mail_password == null || mail_password.equals("-") ) {
            String error = "Email server account not configured. Please report this bug. (p)";
            log.error(error);
            return;
        }

        boolean debug = false;
        final String from    = OrrConfig.instance().emailFrom;
        final String replyTo = OrrConfig.instance().emailReplyTo;

        String text = header + "\n";
        for (String key: data.keySet()) {
            text += "\n    " + key + ": " + data.get(key);
        }
        text += "\n\n";
        text += "(you have received this email because your address is included in "
						+OrrConfig.instance().notifyEmailsFilename + ")";

        String to = "", comma = "";
        for (String recipient : recipients) {
            to += comma + recipient;
            comma = ",";
        }
        log.debug("sending email to notify event: " +data+ "; recipients: " +to);
        try {
            MailSender.sendMessage(mail_user, mail_password, debug, from, to, replyTo, subject, text);
            log.debug("email sent to notify event: " + data + "; recipients: " + to);
        }
        catch (Exception e) {
            String error = "Error sending email to notify event: " + data
                    + "; recipients: " +to+ ": " +e.getMessage();
            log.error(error, e);
        }
    }

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// OOI CI semantic prototype
	public RegisterOntologyResult registerOntologyDirectly(
			LoginResult loginResult,
			RegisteredOntologyInfo registeredOntologyInfo,
			CreateOntologyInfo createOntologyInfo,
			String graphId
	) {
		return OoiCi.registerOntologyDirectly(loginResult, registeredOntologyInfo, createOntologyInfo, graphId);
	}


	public InternalOntologyResult prepareUsersOntology(LoginResult loginResult) {
		InternalOntologyResult result = new InternalOntologyResult();

		try {
			InternalManager.prepareUsersOntology(this, ontClient, loginResult, result);
		}
		catch (Exception e) {
			String error = e.getMessage();
			result.setError(error);
			log.error(error, e);
		}

		return result;
	}

	public InternalOntologyResult createGroupsOntology(LoginResult loginResult) {
		InternalOntologyResult result = new InternalOntologyResult();

		try {
			InternalManager.createGroupsOntology(this, ontClient, loginResult, result);
		}
		catch (Exception e) {
			String error = e.getMessage();
			result.setError(error);
			log.error(error, e);
		}

		return result;
	}


	public UnregisterOntologyResult unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo oi) {
		UnregisterOntologyResult result = new UnregisterOntologyResult();

		log.debug("unregisterOntology called.");

		if ( loginResult == null || ! loginResult.isAdministrator() ) {
			String error = "Unregister ontology: Only an administrator can perform this operation.";
			log.debug(error);
			result.setError(error);
			return result;
		}

		String ontUri = oi.getUri();
		String version = oi.getVersionNumber();

		result.setUri(ontUri);
		result.setVersionNumber(version);

		String error = null;
		Throwable thr = null;

		try {
			if ( ! OntServiceUtil.unregisterOntology(ontUri, version) ) {
				error = "Unregister ontology: Ont service could not perform the removal. " +
						"Please try again later.\n\n" +
						"If the problem persists, please notify the developers.";
			}
		}
		catch (Exception e) {
			error = e.getMessage();
			thr = e;
		}

		if ( error != null ) {
			log.debug(error, thr);
			result.setError(error);
			return result;
		}
		else {
			result.setInfo("Unregistration completed.");
		}

		return result;

	}

	public String markTestingOntology(LoginResult loginResult, RegisteredOntologyInfo oi, boolean markTesting) {

		log.debug("markTestingOntology called.");

		if ( loginResult == null || ! loginResult.isAdministrator() ) {
			String error = "mark testing ontology: Only an administrator can perform this operation.";
			log.debug(error);
			return "Error: " + error;
		}

		String ontUri = oi.getUri();
		String version = oi.getVersionNumber();

		String error = null;
		Throwable thr = null;

		try {
			if ( ! OntServiceUtil.markTestingOntology(ontUri, version, markTesting) ) {
				error = "mark testing  ontology: Ont service could not perform the operation. " +
						"Please try again later.\n\n" +
						"If the problem persists, please notify the developers.";
			}
		}
		catch (Exception e) {
			error = e.getMessage();
			thr = e;
		}

		if ( error != null ) {
			log.debug(error, thr);
			return "Error: " + error;
		}
		else {
			return (markTesting
                    ? "Ontology has been marked as 'testing'."
                    : "The 'testing' mark has been removed.") + "\n" +
                    "\n" +
                    "URI: " + ontUri + "\n" +
                    "version: " + version
            ;
		}
	}


    private String uploadOntology(String uri, String fileName, String RDF,
                                  LoginResult loginResult,
                                  String ontologyId, String ontologyUserId,
                                  Map<String, String> values
    ) throws Exception {
        SignInResult signInResult = new SignInResult();
        signInResult.setSessionId(loginResult.getSessionId());
        signInResult.setUserId(loginResult.getUserId());
        signInResult.setUserName(loginResult.getUserName());
        signInResult.setUserRole(loginResult.getUserRole());

        return ontClient.uploadOntology(uri, fileName, RDF,
                signInResult,
                ontologyId, ontologyUserId,
                values);
    }

}
