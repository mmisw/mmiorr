package org.mmisw.iserver.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
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
import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.BasicOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.CreateVocabularyInfo;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.UploadOntologyResult;
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
 * Implementation of IServerService. 
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


	
	/**
	 * 
	 */
	public List<OntologyInfo> getAllOntologies(boolean includePriorVersions) throws Exception {
		
		//
		// strategy
		// - for each unversioned URI, collect all related, versioned ontologies 
		// - then populate list to be returned with the latest version of each group
		// - if includePriorVersions is true, add the prior versions to each main entry
		//
		
		// unversionedUri -> list of versioned URIs
		Map<String, List<OntologyInfo>> unversionedToVersioned = new LinkedHashMap<String, List<OntologyInfo>>();
		
		String uri = LISTALL;
		
		if ( log.isDebugEnabled() ) {
			log.debug("getAsString. uri= " +uri);
		}

		String response = getAsString(uri, Integer.MAX_VALUE);
		
		String[] lines = response.split("\n|\r\n|\r");
		for ( String line : lines ) {
			// remove leading and trailing quote:
			line = line.replaceAll("^'|'$", "");
			String[] toks = line.trim().split("'\\|'");
			OntologyInfo ontologyInfo = new OntologyInfo();
			String ontologyUri = toks[0];
			
			ontologyInfo.setUri(ontologyUri);
			ontologyInfo.setDisplayLabel(toks[1]);

			ontologyInfo.setType(toks[2]);
			
			ontologyInfo.setUserId(toks[3]);
			ontologyInfo.setContactName(toks[4]);
			ontologyInfo.setVersionNumber(toks[5]);
			ontologyInfo.setDateCreated(toks[6]);
			ontologyInfo.setUsername(toks[7]);
			ontologyInfo.setOntologyId(toks[8], toks[3]);
			
			String unversionedUri;
			
			try {
				MmiUri mmiUri = new MmiUri(ontologyUri);
				ontologyInfo.setAuthority(mmiUri.getAuthority());
				ontologyInfo.setShortName(mmiUri.getTopic());
				
				unversionedUri = mmiUri.copyWithVersion(null).getOntologyUri();
				
			}
			catch (URISyntaxException e) {
				// shouldn't happen.
				log.error("error creating MmiUri from: " +ontologyUri, e);
				continue;
			}

			
			List<OntologyInfo> versionedList = unversionedToVersioned.get(unversionedUri);
			if ( versionedList == null ) {
				versionedList = new ArrayList<OntologyInfo>();
				unversionedToVersioned.put(unversionedUri, versionedList);
			}
			versionedList.add(ontologyInfo);
			
			
		}
		
		List<OntologyInfo> onts = new ArrayList<OntologyInfo>();
		
		for ( String unversionedUri : unversionedToVersioned.keySet() ) {

			List<OntologyInfo> versionedList = unversionedToVersioned.get(unversionedUri);
			
			// sort in descending versionNumber
			Collections.sort(versionedList, new Comparator<OntologyInfo>() {
				public int compare(OntologyInfo arg0, OntologyInfo arg1) {
					return - arg0.getVersionNumber().compareTo(arg1.getVersionNumber());
				}
			});
			
			// extract first element, ie., most recent ontology version, which will be added
			// to the returned list:
			OntologyInfo ontologyInfo = versionedList.remove(0);
			
			// assign UNversioned URI just to this main entry:
			ontologyInfo.setUnversionedUri(unversionedUri);

			// if requested, include prior versions in the priorVersions property 
			if ( includePriorVersions ) {
				ontologyInfo.getPriorVersions().addAll(versionedList);
			}
			
			// add it to returned list:
			onts.add(ontologyInfo);
		}
		
		return onts;
	}
	
	
	public OntologyInfo getOntologyInfo(String ontologyUri) {
		// simple implementation: get all ontologies an pick the one requested
		try {
			List<OntologyInfo> onts = getAllOntologies(false);
			for ( OntologyInfo oi : onts ) {
				if ( ontologyUri.equals(oi.getUri()) ) {
					return oi;
				}
			}
			return null;  // not found
		}
		catch (Exception e) {
			String error = e.getMessage();
			log.error("Error getting list of all ontologies. ", e);
			OntologyInfo oi = new OntologyInfo();
			oi.setError(error);
			return oi;
		}
		
	}

	public OntologyInfo getEntities(OntologyInfo ontologyInfo) {
		if ( log.isDebugEnabled() ) {
			log.debug("getEntities(OntologyInfo) starting");
		}
		Util.getEntities(ontologyInfo, null);
		return ontologyInfo;
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
	
	
	
	
	public OntologyInfo getOntologyContents(OntologyInfo ontologyInfo) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(OntologyInfo): loading model");
		}
		
		OntModel ontModel = Util.loadModel(ontologyInfo.getUri());

		// Metadata:
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(OntologyInfo): getting metadata");
		}
		MetadataExtractor.prepareOntologyMetadata(metadataBaseInfo, ontModel, ontologyInfo);

		
		// Data
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(OntologyInfo): getting entities");
		}
		Util.getEntities(ontologyInfo, ontModel);
		
		
		return ontologyInfo;
	
	}

	
	
	
	// TODO this mechanism copied from MmiUri (in ont project).
	private static final Pattern VERSION_PATTERN = 
				Pattern.compile("^\\d{4}(\\d{2}(\\d{2})?)?(T\\d{2})?(\\d{2}(\\d{2})?)?$");


	////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	
	public CreateOntologyResult createVocabulary(
			BasicOntologyInfo basicOntologyInfo, CreateVocabularyInfo createOntologyInfo) {
		
//		_getBaseInfoIfNull();
		
		CreateOntologyResult createVocabResult = new CreateOntologyResult();
		createVocabResult.setBasicOntologyInfo(basicOntologyInfo);
		createVocabResult.setCreateOntologyInfo(createOntologyInfo);
		
		
		Map<String, String> newValues = createOntologyInfo.getMetadataValues();
		
		////////////////////////////////////////////
		// check for errors
		
		if ( createVocabResult.getError() != null ) {
			log.info(": error: " +createVocabResult.getError());
			return createVocabResult;
		}
		
		if ( basicOntologyInfo.getError() != null ) {
			String error = "there was an error while loading the ontology: " +basicOntologyInfo.getError();
			createVocabResult.setError(error );
			log.info(error);
			return createVocabResult;
		}
		
		if ( newValues == null ) {
			String error = "Unexpected: no new values assigned for review. Please report this bug";
			createVocabResult.setError(error );
			log.info(error);
			return createVocabResult;
		}
		
		
		final String orgAbbreviation = newValues.get(OmvMmi.origMaintainerCode.getURI());
		final String shortName = newValues.get(Omv.acronym.getURI());
		// TODO: shortName taken NOT from acronym but from a new field explicitly for the shortName piece

		if ( orgAbbreviation == null ) {
			log.info("missing origMaintainerCode");
			createVocabResult.setError("missing origMaintainerCode");
			return createVocabResult;
		}
		if ( shortName == null ) {
			log.info("missing acronym");
			createVocabResult.setError("missing acronym");
			return createVocabResult;
		}

		
		// to check if this is going to be a new submission (ontologyId == null) or, 
		// otherwise, a new version.
		String ontologyId = basicOntologyInfo.getOntologyId();

		if ( ontologyId == null ) {
			// This is a new submission. We need to check for any conflict with a preexisting
			// ontology in the repository with the same shortName+orgAbbreviation combination
			//
			if ( ! Util2.checkNoPreexistingOntology(orgAbbreviation, shortName, createVocabResult) ) {
				return createVocabResult;
			}
		}
		else {
			// This is a submission of a *new version* of an existing ontology.
			// We need to check the shortName+orgAbbreviation combination as any changes here
			// would imply a *new* ontology, not a new version.
			//
			String originalOrgAbbreviation = basicOntologyInfo.getAuthority();
			String originalShortName = basicOntologyInfo.getShortName();
			
			if ( ! Util2.checkUriKeyCombinationForNewVersion(
					originalOrgAbbreviation, originalShortName, 
					orgAbbreviation, shortName, createVocabResult) ) {
				return createVocabResult;
			}
		}
		
		
		_createTempVocabularyOntology(basicOntologyInfo, createOntologyInfo, createVocabResult);
		if ( createVocabResult.getError() != null ) {
			return createVocabResult;
		}
		
		////////////////////////////////////////////
		// load  model

		String full_path = createVocabResult.getFullPath();
		log.info("Loading model: " +full_path);
		
		File file = new File(full_path);
		if ( ! file.canRead() ) {
			log.info("Unexpected: cannot read: " +full_path);
			createVocabResult.setError("Unexpected: cannot read: " +full_path);
			return createVocabResult;
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
				createVocabResult.setError(error);
				return createVocabResult;
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
			createVocabResult.setError(error);
			return createVocabResult;
		}
		
		String uriForEmpty = model.getNsPrefixURI("");
		if ( uriForEmpty == null ) {
			// FIXME Get the original ns when model.getNsPrefixURI("") returns null
			// For now, returning error:
			String error = "Unexpected error: No namespace for prefix \"\"";
			log.info(error);
			createVocabResult.setError(error);
			return createVocabResult;
			
			// This case was manifested with the platform.owl ontology.
		}
		
		log.info("review: model.getNsPrefixURI(\"\") = " +uriForEmpty);
		
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
		
		
		createVocabResult.setUri(base_);
		

		// write new contents to a new file under previewDir:
		
		File reviewedFile = new File(previewDir , file.getName());
		createVocabResult.setFullPath(reviewedFile.getAbsolutePath());

		PrintWriter os;
		try {
			os = new PrintWriter(reviewedFile);
		}
		catch (FileNotFoundException e) {
			log.info("Unexpected: file not found: " +reviewedFile);
			createVocabResult.setError("Unexpected: file not found: " +reviewedFile);
			return createVocabResult;
		}
		StringReader is = new StringReader(rdf);
		try {
			IOUtils.copy(is, os);
			os.flush();
		}
		catch (IOException e) {
			log.info("Unexpected: IO error while writing to: " +reviewedFile);
			createVocabResult.setError("Unexpected: IO error while writing to: " +reviewedFile);
			return createVocabResult;
		}

		// Done.

		return createVocabResult;
	}

	/**
	 * This creates a new vocabulary ontology under the previewDir direcory with the given info.
	 * @param createOntologyInfo
	 * @param createVocabResult 
	 */
	private void _createTempVocabularyOntology(
			BasicOntologyInfo basicOntologyInfo, 
			CreateVocabularyInfo createOntologyInfo,
			CreateOntologyResult createVocabResult
	) {
		
		VocabCreator vocabCreator = new VocabCreator(basicOntologyInfo, createOntologyInfo);
		
		vocabCreator.createOntology(createVocabResult);
	}

	
	
	
	public UploadOntologyResult uploadOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult)  {
		UploadOntologyResult uploadOntologyResult = new UploadOntologyResult();
		
		
		String full_path = createOntologyResult.getFullPath();
		
		log.info("Reading in temporary file: " +full_path);
		
		File file = new File(full_path);
		if ( ! file.canRead() ) {
			log.info("Unexpected: cannot read: " +full_path);
			uploadOntologyResult.setError("Unexpected: cannot read: " +full_path);
			return uploadOntologyResult;
		}
		
		// Get resulting model:
		String rdf;
		try {
			rdf = readRdf(file);
		}
		catch (IOException e) {
			String error = "Unexpected: IO error while reading from: " +full_path+ " : " +e.getMessage();
			log.info(error);
			uploadOntologyResult.setError(error);
			return uploadOntologyResult;
		}
		
		// ok, we have our ontology:
		
		
		//////////////////////////////////////////////////////////////////////////
		// finally, do actual upload to MMI registry

		// Get final URI of resulting model
		// FIXME this uses the same original URI
		String uri = createOntologyResult.getUri();
		assert uri != null;
		assert loginResult.getUserId() != null;
		assert loginResult.getSessionId() != null;
		
		log.info(": uploading ...");

		BasicOntologyInfo basicOntologyInfo = createOntologyResult.getBasicOntologyInfo();
		String ontologyId = basicOntologyInfo.getOntologyId();
		String ontologyUserId = basicOntologyInfo.getOntologyUserId();
		if ( ontologyId != null ) {
			log.info("Will create a new version for ontologyId = " +ontologyId+ ", userId=" +ontologyUserId);
		}
		
		
		CreateOntologyInfo createOntologyInfo = createOntologyResult.getCreateOntologyInfo();
		Map<String, String> newValues = createOntologyInfo .getMetadataValues();
		

		try {
			
			String fileName = new URL(uri).getPath();
			
			//
			// make sure the fileName ends with ".owl" as the aquaportal back-end seems
			// to add that fixed extension in some operations (at least in the parse operation)
			//
			if ( ! fileName.toLowerCase().endsWith(".owl") ) {
				log.info("upload: setting file extension to .owl per aquaportal requirement.");
				fileName += ".owl";
			}
			
			// We are about to do the actual upload. But first, re-check that there is NO a preexisting
			// ontology that may conflict with this one.
			// NOTE: this check has been done already in the review operation; however, we repeat it here
			// in case there is a new registration done by other user in the meantime. Of course, we
			// are NOT completely solving the potential concurrency problem with this re-check; we are just
			// reducing the chances of that event.
			if ( ontologyId == null ) {
				
				String originalOrgAbbreviation = basicOntologyInfo.getAuthority();
				String originalShortName = basicOntologyInfo.getShortName();

				final String orgAbbreviation = newValues.get(OmvMmi.origMaintainerCode.getURI());
				final String shortName = newValues.get(Omv.acronym.getURI());

				if ( ! Util2.checkUriKeyCombinationForNewVersion(
						originalOrgAbbreviation, originalShortName, 
						orgAbbreviation, shortName, uploadOntologyResult) ) {
					return uploadOntologyResult;
				}
				
			}
			else {
				// This is a submission of a *new version* of an existing ontology.
				// Nothing needs to be checked here.
				// NOTE: We don't need to repeat the _checkUriKeyCombinationForNewVersion step here
				// as any change in the contents of the metadata forces the user to explicitly
				// do the "review" operation, which already takes care of that check.
			}

			// OK, now do the actual upload:
			OntologyUploader createOnt = new OntologyUploader(uri, fileName, rdf, 
					loginResult,
					ontologyId, ontologyUserId,
					newValues
			);
			String res = createOnt.create();
			
			if ( res.startsWith("OK") ) {
				uploadOntologyResult.setUri(uri);
				uploadOntologyResult.setInfo(res);
			}
			else {
				uploadOntologyResult.setError(res);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			uploadOntologyResult.setError(ex.getClass().getName()+ ": " +ex.getMessage());
		}
		
		log.info("uploadOntologyResult = " +uploadOntologyResult);

		
		return uploadOntologyResult;
	}
	
	
	/**
	 * Reads an RDF file.
	 * @param file the file to read in
	 * @return the contents of the text file.
	 * @throws IOException 
	 */
	private String readRdf(File file) throws IOException {
		
		// make sure the file can be loaded as a model:
		String uriFile = file.toURI().toString();
		try {
			JenaUtil.loadModel(uriFile, false);
		}
		catch (Throwable ex) {
			String error = ex.getClass().getName()+ " : " +ex.getMessage();
			throw new IOException(error);
		}
		

		
		BufferedReader is = null;
		try {
			is = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			StringWriter sw = new StringWriter();
			PrintWriter os = new PrintWriter(sw);
			IOUtils.copy(is, os);
			os.flush();
			String rdf = sw.toString();
			return rdf;
		}
		finally {
			if ( is != null ) {
				try {
					is.close();
				}
				catch(IOException ignore) {
				}
			}
		}
	}

}
