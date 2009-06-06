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
import org.mmisw.iserver.core.util.TempOntologyHelper;
import org.mmisw.iserver.core.util.Util;
import org.mmisw.iserver.core.util.Util2;
import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.OtherDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.DataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.VocabularyDataCreationInfo;
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
	
	private static final String ONT = "http://mmisw.org/ont";
	private static final String LISTALL = ONT + "?listall";

	
	private final AppInfo appInfo = new AppInfo("MMISW IServer");
	private final Log log = LogFactory.getLog(Server.class);

	private File previewDir;
	
	
	private static IServer _instance ;
	
	public static IServer getInstance() {
		if ( _instance == null ) {
			_instance = new Server();
		}
		
		return _instance;
	}
	
	private Server() {
		log.info("initializing " +appInfo.getAppName()+ "...");
		try {
			Config.getInstance();
			
			appInfo.setVersion(
					Config.Prop.VERSION.getValue()+ " (" +
						Config.Prop.BUILD.getValue()  + ")"
			);
					
			log.info(appInfo.toString());
			
			previewDir = new File(Config.Prop.ONTMD_PREVIEW_DIR.getValue());
			
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
		return  Util.getEntities(ontologyUri, null);
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
		
		String uri = LISTALL;
		
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
			
			try {
				MmiUri mmiUri = new MmiUri(ontologyUri);
				authority = mmiUri.getAuthority();
				shortName = mmiUri.getTopic();
				unversionedUri = mmiUri.copyWithVersion(null).getOntologyUri();
			}
			catch (URISyntaxException e) {
				// shouldn't happen.
				log.error("error creating MmiUri from: " +ontologyUri, e);
				continue;
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
		
		return unversionedToVersioned;
	}

	
	public List<RegisteredOntologyInfo> getAllOntologies(boolean includePriorVersions) throws Exception {
		
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

			// if requested, include prior versions: 
			if ( includePriorVersions ) {
				registeredOntologyInfo.getPriorVersions().addAll(versionedList);
			}
			
			// add this main entry to returned list:
			onts.add(registeredOntologyInfo);
		}
		
		return onts;
	}
	
	
	public RegisteredOntologyInfo getOntologyInfo(String ontologyUri) {
		try {
			MmiUri mmiUri = new MmiUri(ontologyUri);
			
			// get elements associated with the unversioned form of the requested URI:
			String unversOntologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
			Map<String, List<RegisteredOntologyInfo>> unversionedToVersioned = getUnversionedToOntologyInfoListMap(unversOntologyUri);

			if ( unversionedToVersioned.isEmpty() ) {
				return null; // not found
			}
			assert unversionedToVersioned.size() == 1;
			
			// get the list of ontologies with same unversioned URI
			List<RegisteredOntologyInfo> list = unversionedToVersioned.values().iterator().next();
			
			// Two main cases: 
			//
			//  a) unversioned URI request -> just return first entry in list, which is the most recent,
			//     but making the Uri property equal to the UnversionedUri property
			//
			//  b) versioned URI request -> search the list for the exact match, if any
			//
			
			
			if ( ontologyUri.equals(unversOntologyUri) ) {
				// a) unversioned URI request, eg., http://mmisw.org/ont/seadatanet/qualityFlag
				// just return first entry in list
				RegisteredOntologyInfo oi = list.get(0);
				oi.setUri(oi.getUnversionedUri());
				return oi;
			}
			else {
				// b) versioned URI request, eg., http://mmisw.org/ont/seadatanet/20081113T205440/qualityFlag
				// Search list for exact match
				for ( RegisteredOntologyInfo oi : list ) {
					if ( ontologyUri.equals(oi.getUri()) ) {
						return oi;
					}
				}
				return null;  // not found
			}
		}
		catch (Exception e) {
			String error = e.getMessage();
			log.error("Error getting list of all ontologies. ", e);
			RegisteredOntologyInfo oi = new RegisteredOntologyInfo();
			oi.setError(error);
			return oi;
		}
		
	}

	public RegisteredOntologyInfo getEntities(RegisteredOntologyInfo registeredOntologyInfo) {
		if ( log.isDebugEnabled() ) {
			log.debug("getEntities(OntologyInfo) starting");
		}
		Util.getEntities(registeredOntologyInfo, null);
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
	
	
	
	
	public RegisteredOntologyInfo getOntologyContents(RegisteredOntologyInfo registeredOntologyInfo) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(OntologyInfo): loading model");
		}
		
		OntModel ontModel = Util.loadModel(registeredOntologyInfo.getUri());

		// Metadata:
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(OntologyInfo): getting metadata");
		}
		MetadataExtractor.prepareOntologyMetadata(metadataBaseInfo, ontModel, registeredOntologyInfo);

		
		// Data
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(OntologyInfo): getting entities");
		}
		Util.getEntities(registeredOntologyInfo, ontModel);
		
		
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
		
		
		final String orgAbbreviation = newValues.get(OmvMmi.origMaintainerCode.getURI());
		final String shortName = newValues.get(Omv.acronym.getURI());
		// TODO: shortName taken NOT from acronym but from a new field explicitly for the shortName piece

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

		
		// to check if this is going to be a new submission (ontologyId == null) or, 
		// otherwise, a new version.
		String ontologyId = createOntologyInfo.getOntologyId();

		if ( ontologyId == null ) {
			// This is a new submission. We need to check for any conflict with a preexisting
			// ontology in the repository with the same shortName+orgAbbreviation combination
			//
			if ( ! Util2.checkNoPreexistingOntology(orgAbbreviation, shortName, createOntologyResult) ) {
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
		else if (  dataCreationInfo instanceof OtherDataCreationInfo) {
			// external ontology case: the base ontology is already available, just use it
			// by setting the full path in the createOntologyResult:
			
			OtherDataCreationInfo otherDataCreationInfo = (OtherDataCreationInfo) dataCreationInfo;
			TempOntologyInfo tempOntologyInfo = otherDataCreationInfo.getTempOntologyInfo();
			
			String full_path = tempOntologyInfo.getFullPath();
			createOntologyResult.setFullPath(full_path);
		}
		else {
			createOntologyResult.setError("Sorry, creation of " +dataCreationInfo.getClass().getSimpleName()+ " not implemented yet");
			return createOntologyResult;
		}

		
		
		////////////////////////////////////////////
		// load  model

		String full_path = createOntologyResult.getFullPath();
		log.info("Loading model: " +full_path);
		
		File file = new File(full_path);
		if ( ! file.canRead() ) {
			log.info("Unexpected: cannot read: " +full_path);
			createOntologyResult.setError("Unexpected: cannot read: " +full_path);
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
		
		
		final String finalUri = Util2.namespaceRoot + "/" +
		                        orgAbbreviation + "/" +
		                        version + "/" +
		                        shortName;
		
		final String ns_ = JenaUtil2.appendFragment(finalUri);
		final String base_ = JenaUtil2.removeTrailingFragment(finalUri);
		

		
		
		String uriFile = file.toURI().toString();
		OntModel model = null;
		try {
			model = JenaUtil.loadModel(uriFile, false);
		}
		catch ( Throwable ex ) {
			String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
			log.info(error);
			createOntologyResult.setError(error);
			return createOntologyResult;
		}
		
		String uriForEmpty = Util2.getDefaultNamespace(model, file, createOntologyResult);
			
		if ( uriForEmpty == null ) {
			return createOntologyResult;
		}

		
		log.info("createOntology: using '" +uriForEmpty+ "' as default namespace");
		
		// Why using JenaUtil.getURIForNS(uriForEmpty) to get the namespace?
		// model.getNsPrefixURI("") should provide the base namespace, in fact,
		// I verified that this call gives the right URI associated with "" in two
		// cases, one with xxxx/ (slash) and xxxxx# (pound) at the end.
		// So, instead of:
		//    final String original_ns_ = JenaUtil.getURIForNS(uriForEmpty);
		// I just take the reported URI as given by model.getNsPrefixURI(""):
		final String original_ns_ = uriForEmpty;
		
		
		log.info("original namespace: " +original_ns_);
		log.info("Setting prefix \"\" for URI " + ns_);
		model.setNsPrefix("", ns_);
		log.info("     new namespace: " +ns_);

		
		// Update statements  according to the new namespace:
		Util2.replaceNameSpace(model, original_ns_, ns_);

		
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
					newOntModel.add(ont_, st.getPredicate(), st.getObject());
				}
				else {
					log.info(" Not Transferring: " +prd+ " from previous version because new value " +newValue);
				}
			}	
			
			log.info("Removing original OWL.Ontology individual");
			ontRes.removeProperties();
			// TODO the following may be unnecesary but doesn't hurt:
			model.remove(ontRes, RDF.type, OWL.Ontology); 
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
		
		
		createOntologyResult.setUri(base_);
		

		// write new contents to a new file under previewDir:
		
		File reviewedFile = new File(previewDir , file.getName());
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
		
		VocabCreator vocabCreator = new VocabCreator(createOntologyInfo, vocabularyCreationInfo);
		
		vocabCreator.createOntology(createVocabResult);
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
		String uri = createOntologyResult.getUri();
		assert uri != null;
		assert loginResult.getUserId() != null;
		assert loginResult.getSessionId() != null;
		
		log.info(": registering ...");

		CreateOntologyInfo createOntologyInfo = createOntologyResult.getCreateOntologyInfo();

		String ontologyId = createOntologyInfo.getOntologyId();
		String ontologyUserId = createOntologyInfo.getOntologyUserId();
		
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
			
			// We are about to do the actual registration. But first, re-check that there is NO a preexisting
			// ontology that may conflict with this one.
			// NOTE: this check has been done already in the review operation; however, we repeat it here
			// in case there is a new registration done by other user in the meantime. Of course, we
			// are NOT completely solving the potential concurrency problem with this re-check; we are just
			// reducing the chances of that event.
			if ( ontologyId == null ) {
				
				final String orgAbbreviation = newValues.get(OmvMmi.origMaintainerCode.getURI());
				final String shortName = newValues.get(Omv.acronym.getURI());

				if ( ! Util2.checkNoPreexistingOntology(orgAbbreviation, shortName, registerOntologyResult) ) {
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
			OntologyUploader createOnt = new OntologyUploader(uri, fileName, rdf, 
					loginResult,
					ontologyId, ontologyUserId,
					newValues
			);
			String res = createOnt.create();
			
			if ( res.startsWith("OK") ) {
				registerOntologyResult.setUri(uri);
				registerOntologyResult.setInfo(res);
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
		Util.getEntities(tempOntologyInfo, ontModel);
		
		
		return tempOntologyInfo;
	
	}

}
