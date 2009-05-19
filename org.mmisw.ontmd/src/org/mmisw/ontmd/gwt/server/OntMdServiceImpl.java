package org.mmisw.ontmd.gwt.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.ont.MmiUri;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.ontmd.gwt.client.rpc.BaseInfo;
import org.mmisw.ontmd.gwt.client.rpc.BaseResult;
import org.mmisw.ontmd.gwt.client.rpc.DataResult;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.rpc.OntMdService;
import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.PortalBaseInfo;
import org.mmisw.ontmd.gwt.client.rpc.ReviewResult;
import org.mmisw.ontmd.gwt.client.rpc.UploadResult;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.ConversionResult;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.Voc2RdfBaseInfo;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrGroup;
import org.mmisw.ontmd.gwt.server.portal.PortalImpl;
import org.mmisw.ontmd.gwt.server.voc2rdf.Voc2RdfImpl;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.drexel.util.rdf.JenaUtil;
import edu.drexel.util.rdf.OwlModel;



/**
 * Implementation of OntMdService. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class OntMdServiceImpl extends RemoteServiceServlet implements OntMdService {
	private static final long serialVersionUID = 1L;

	/** Ontology URI prefix including root: */
	// TODO read namespaceRoot from a configuration parameter
	private static final String namespaceRoot = "http://mmisw.org/ont";
	
	
	private final AppInfo appInfo = new AppInfo("MMI OntMd");
	
	
	private File previewDir;


	// TODO this mechanism copied from MmiUri (in ont project).
	private static final Pattern VERSION_PATTERN = 
				Pattern.compile("^\\d{4}(\\d{2}(\\d{2})?)?(T\\d{2})?(\\d{2}(\\d{2})?)?$");

	
	private final Log log = LogFactory.getLog(OntMdServiceImpl.class);
	
	private BaseInfo baseInfo = null;
	
	
	public void init() throws ServletException {
		super.init();
		log.info("initializing " +appInfo.getAppName()+ "...");
		ServletConfig servletConfig = getServletConfig();
		try {
			Config.getInstance().init(servletConfig, log, true);
			
			appInfo.setVersion(
					Config.Prop.VERSION.getValue()+ " (" +
						Config.Prop.BUILD.getValue()  + ")"
			);
					
			log.info(appInfo.toString());
			
			previewDir = new File(Config.Prop.ONTMD_PREVIEW_DIR.getValue());
			
			
			// voc2rdf initialization
			voc2rdf = new Voc2RdfImpl();
			
			// portal initialization
			portal = new PortalImpl();
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
	
	public BaseInfo getBaseInfo(Map<String, String> params) {
		log.info("getBaseInfo: params=" + params);
		
		// from the client, always re-create the base info:
		
		// include version if parameter/value "_xv=y" is given:
		boolean includeVersion = "y".equals(params.get("_xv"));
		try {
			prepareBaseInfo(includeVersion);
		}
		catch (Throwable thr) {
			thr.printStackTrace();
			baseInfo.setError(thr.toString());
		}
		return baseInfo;
	}
	
	
	/** prepares the baseInfo only if not already prepared */
	private void _getBaseInfoIfNull() {
		if ( baseInfo == null ) {
			prepareBaseInfo(false);
		}
	}
	
	/** always re-creates the baseInfo */
	private void prepareBaseInfo(boolean includeVersion) {
		log.info("preparing base info ...");
		baseInfo = new BaseInfo();
		
		baseInfo.setResourceTypeUri(Omv.acronym.getURI());
		
		MdHelper.prepareGroups(includeVersion);
		AttrGroup[] attrGroups = MdHelper.getAttrGroups();
		baseInfo.setAttrGroups(attrGroups);
		log.info("preparing base info ... DONE");
	}

	
	public AttrDef refreshOptions(AttrDef attrDef) {
		return MdHelper.refreshOptions(attrDef);
	}


	public LoginResult login(String userName, String userPassword) {
		LoginResult loginResult = new LoginResult();
		
		log.info(": authenticating user " +userName+ " ...");
		try {
			Login login = new Login(userName, userPassword);
			login.getSession(loginResult);
		}
		catch (Exception ex) {
			loginResult.setError(ex.getMessage());
		}

		return loginResult;
	}
	

	
	public OntologyInfo getOntologyInfoFromPreLoaded(String uploadResults) {
		OntologyInfo ontologyInfo = new OntologyInfo();
		
		uploadResults = uploadResults.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		log.info("getOntologyInfo: " +uploadResults);
		
		
		if ( uploadResults.matches(".*<error>.*") ) {
			log.info("<error>");
			ontologyInfo.setError(uploadResults);
			return ontologyInfo;
		}
		
		if ( false && !uploadResults.matches(".*success.*") ) {
			log.info("Not <success> !");
			// unexpected response.
			ontologyInfo.setError("Error while loading ontology. Please try again later.");
			return ontologyInfo;
		}
		

		String full_path;
		
		Pattern pat = Pattern.compile(".*<filename>([^<]+)</filename>");
		Matcher matcher = pat.matcher(uploadResults);
		if ( matcher.find() ) {
			full_path = matcher.group(1);
		}
		else {
			log.info("Could not parse uploadResults.");
			ontologyInfo.setError("Could not parse uploadResults.");
			return ontologyInfo;
		}

		
		File file = new File(full_path);
		
		try {
			ontologyInfo.setRdf(readRdf(file));
		}
		catch (Throwable e) {
			String error = "Cannot read RDF model: " +full_path+ " : " +e.getMessage();
			log.info(error);
			ontologyInfo.setError(error);
			return ontologyInfo;
		}

		// prepare the rest of the ontology info:
		String error = prepareOntologyInfo(file, ontologyInfo);
		if ( error != null ) {
			ontologyInfo.setError(error);
		}
	
		return ontologyInfo;
	}

	
	private void _addDetail(StringBuilder details, String a1, String a2, String a3) {
		String str = a1 + "|" + a2 + "|" + a3; 
		log.info(str);
		details.append(str + "\n");
	}
	
	/**
	 * Completes the ontology info object by assigning some of the members
	 * except the Rdf string and the new values map.
	 * 
	 * @param file  The ontology file.
	 * @param ontologyInfo  The object to be completed
	 */
	private String prepareOntologyInfo(File file, OntologyInfo ontologyInfo) {
		String full_path = file.getAbsolutePath();
		ontologyInfo.setFullPath(full_path);
		
		String uriFile = file.toURI().toString();
		log.info("Loading model: " +uriFile);

		return prepareOntologyInfoFromUri(uriFile, ontologyInfo);
	}
	
	/**
	 * Does the preparation by reading the model from the given URI.
	 * @param uriModel URI of the model to be loaded
	 * @param ontologyInfo  The object to be completed
	 * @return
	 */
	private String prepareOntologyInfoFromUri(String uriModel, OntologyInfo ontologyInfo) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("prepareOntologyInfoFromUri: uriModel=" +uriModel);
		}
		
		OntModel model;
		
		try {
			model = JenaUtil.loadModel(uriModel, false);
		}
		catch (Throwable ex) {
			String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
			log.info(error);
			return error;
		}
		
		if ( false && log.isDebugEnabled() ) {
			debugOntModel(model);
		}

		Resource ontRes = JenaUtil.getFirstIndividual(model, OWL.Ontology);
		
		_getBaseInfoIfNull();
		
		StringBuilder moreDetails = new StringBuilder();
		
		Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
		Map<String,String> originalValues = new HashMap<String, String>();
		
		if ( ontRes != null ) {
			//
			// Get values from the existing ontology resource
			//
			for ( AttrGroup attrGroup : baseInfo.getAttrGroups() ) {
				for ( AttrDef attrDef : attrGroup.getAttrDefs() ) {
					
					// get value of MMI property:
					Property mmiProp = uriPropMap.get(attrDef.getUri());
					String prefixedMmi = MdHelper.prefixedName(mmiProp);
					String value = JenaUtil.getValue(ontRes, mmiProp);
					
					// DC equivalent, which is obtained if necessary
					Property dcProp = null;
					
					if (value == null) {
						// try a DC equivalent to use:
						dcProp = MdHelper.getEquivalentDcProperty(mmiProp);
						if ( dcProp != null ) {
							value = JenaUtil.getValue(ontRes, dcProp);
						}
					}
					
					if ( value != null ) {
						
						// get value:
						if ( log.isDebugEnabled() ) {
							log.debug("Assigning: " +attrDef.getUri()+ " = " + value);
						}
						originalValues.put(attrDef.getUri(), value);
						
						// Special case: Omv.acronym/OmvMmi.shortNameUri  
						if ( Omv.acronym.getURI().equals(attrDef.getUri()) ) {
							// add also the value of OmvMmi.shortNameUri:
							String shortNameValue = JenaUtil.getValue(ontRes, OmvMmi.shortNameUri);
							if ( log.isDebugEnabled() ) {
								log.debug("Also assigning " +OmvMmi.shortNameUri.getURI()+ " = " +shortNameValue);
							}
							originalValues.put(OmvMmi.shortNameUri.getURI(), shortNameValue);
						}
						
						

						// add detail:
						if ( dcProp != null ) {
							String prefixedDc = MdHelper.prefixedName(dcProp);
							_addDetail(moreDetails, prefixedMmi, "not present", "Will use " +prefixedDc);
						}
						else {
							_addDetail(moreDetails, prefixedMmi, "present", " ");
						}
					}
					else {
						if ( attrDef.isRequired() && ! attrDef.isInternal() ) {
							if ( dcProp != null ) {
								String prefixedDc = MdHelper.prefixedName(dcProp);
								_addDetail(moreDetails, prefixedMmi, "not present", "and " +prefixedDc+ " not present either");
							}	
							else {
								_addDetail(moreDetails, prefixedMmi, "not present", " not equivalent DC");
							}
						}
					}
				}
			}
		}
		else {
			//
			// No ontology resource. Check required attributes to report in the details:
			//
			for ( AttrGroup attrGroup : baseInfo.getAttrGroups() ) {
				for ( AttrDef attrDef : attrGroup.getAttrDefs() ) {
					if ( attrDef.isRequired() && ! attrDef.isInternal() ) {
						Property mmiProp = uriPropMap.get(attrDef.getUri());
						String prefixedMmi = MdHelper.prefixedName(mmiProp);
						_addDetail(moreDetails, prefixedMmi, "not present", "required");
					}
				}
			}
		}
		
		// add the new details if any:
		if ( moreDetails.length() > 0 ) {
			String details = ontologyInfo.getDetails();
			if ( details == null ) {
				ontologyInfo.setDetails(moreDetails.toString());
			}
			else {
				ontologyInfo.setDetails(details + "\n" +moreDetails.toString());
			}
		}
		
		ontologyInfo.setOriginalValues(originalValues);
		
		// associate the original base URI:
		String uri = model.getNsPrefixURI("");
		if ( uri != null ) {
			String base_ = JenaUtil2.removeTrailingFragment(uri);
			ontologyInfo.setUri(base_);
		}

		// OK:
		return null;
	}
	

	private void debugOntModel(OntModel model) {
		StmtIterator stmts = model.listStatements();
		while ( stmts.hasNext() ) {
			Statement stmt = stmts.nextStatement();
			log.debug(" #### " +stmt);
		}
	}


	/**
	 * Replaces any statement having an element in the given oldNameSpace with a
	 * correponding statement in the new namespace.
	 * <p>
	 * (Doesn't jena have a utility for doing this?)
	 * 
	 * @param model
	 * @param oldNameSpace
	 * @param newNameSpace
	 */
	// TODO: Use function from "ont" project (the utility is replicated here for the moment)
	private void _replaceNameSpace(OntModel model, String oldNameSpace, String newNameSpace) {
		
		//log.info(" REPLACING NS " +oldNameSpace+ " WITH " +newNameSpace);
		
		// old statements to be removed:
		List<Statement> o_stmts = new ArrayList<Statement>(); 
		
		// new statements to be added:
		List<Statement> n_stmts = new ArrayList<Statement>(); 
		
		StmtIterator existingStmts = model.listStatements();
		while ( existingStmts.hasNext() ) {
			Statement o_stmt = existingStmts.nextStatement();
			Resource sbj = o_stmt.getSubject();
			Property prd = o_stmt.getPredicate();
			RDFNode obj = o_stmt.getObject();
			
			boolean any_change = false;
			Resource n_sbj = sbj;
			Property n_prd = prd;
			RDFNode  n_obj = obj;

			if ( oldNameSpace.equals(sbj.getNameSpace()) ) {
				n_sbj = model.createResource(newNameSpace + sbj.getLocalName());
				any_change = true;
			}
			if ( oldNameSpace.equals(prd.getNameSpace()) ) {
				n_prd = model.createProperty(newNameSpace + prd.getLocalName());
				any_change = true;
			}
			if ( (obj instanceof Resource) && oldNameSpace.equals(((Resource) obj).getNameSpace()) ) {
				n_obj = model.createResource(newNameSpace + ((Resource) obj).getLocalName());
				any_change = true;
			}

			if ( any_change ) {
				o_stmts.add(o_stmt);
				Statement n_stmt = model.createStatement(n_sbj, n_prd, n_obj);
				n_stmts.add(n_stmt);
				//log.info(" #### " +o_stmt);
			}
		}
		
		for ( Statement n_stmt : n_stmts ) {
			model.add(n_stmt);
		}
		
		for ( Statement o_stmt : o_stmts ) {
			model.remove(o_stmt);
		}
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

	/**
	 * Reviews the ontology and associated attributes for a subsequent upload (registration)
	 * (see {@link #upload(ReviewResult, LoginResult)})
	 * in the repository.
	 * 
	 * @param OntologyInfo General info about the ontology that is intended to be registered.
	 * 
	 * @param LoginResult Login information
	 * 
	 * @return the result of the review.
	 */
	public ReviewResult review(OntologyInfo ontologyInfo, LoginResult loginResult) {
		
		_getBaseInfoIfNull();
		
		ReviewResult reviewResult = new ReviewResult();
		reviewResult.setOntologyInfo(ontologyInfo);
		
		
		Map<String, String> newValues = ontologyInfo.getNewValues();
		
		////////////////////////////////////////////
		// check for errors
		
		if ( loginResult == null ) {
			reviewResult.setError("No login information");
		}
		else if ( loginResult.getError() != null ) {
			reviewResult.setError("Authentication has errors ");
		}
		
		if ( reviewResult.getError() != null ) {
			log.info(": error: " +reviewResult.getError());
			return reviewResult;
		}
		
		if ( ontologyInfo.getError() != null ) {
			String error = "there was an error while loading the ontology: " +ontologyInfo.getError();
			reviewResult.setError(error );
			log.info(error);
			return reviewResult;
		}
		
		if ( newValues == null ) {
			String error = "Unexpected: no new values assigned for review. Please report this bug";
			reviewResult.setError(error );
			log.info(error);
			return reviewResult;
		}
		
		final String orgAbbreviation = newValues.get(OmvMmi.origMaintainerCode.getURI());
		final String shortName = newValues.get(Omv.acronym.getURI());
		// TODO: shortName taken NOT from acronym but from a new field explicitly for the shortName piece

		if ( orgAbbreviation == null ) {
			log.info("missing origMaintainerCode");
			reviewResult.setError("missing origMaintainerCode");
			return reviewResult;
		}
		if ( shortName == null ) {
			log.info("missing acronym");
			reviewResult.setError("missing acronym");
			return reviewResult;
		}

		
		// to check if this is going to be a new submission (ontologyId == null) or, 
		// otherwise, a new version.
		String ontologyId = ontologyInfo.getOntologyId();

		if ( ontologyId == null ) {
			// This is a new submission. We need to check for any conflict with a preexisting
			// ontology in the repository with the same shortName+orgAbbreviation combination
			//
			if ( ! _checkNoPreexistingOntology(orgAbbreviation, shortName, reviewResult) ) {
				return reviewResult;
			}
		}
		else {
			// This is a submission of a *new version* of an existing ontology.
			// We need to check the shortName+orgAbbreviation combination as any changes here
			// would imply a *new* ontology, not a new version.
			//
			Map<String, String> originalValues = ontologyInfo.getOriginalValues();
			String originalOrgAbbreviation = originalValues.get(OmvMmi.origMaintainerCode.getURI());
			String originalShortName = originalValues.get(Omv.acronym.getURI());
			
			if ( ! _checkUriKeyCombinationForNewVersion(
					originalOrgAbbreviation, originalShortName, 
					orgAbbreviation, shortName, reviewResult) ) {
				return reviewResult;
			}
		}
		
		////////////////////////////////////////////
		// load pre-uploaded model

		String full_path = ontologyInfo.getFullPath();
		log.info("Loading model: " +full_path);
		
		File file = new File(full_path);
		if ( ! file.canRead() ) {
			log.info("Unexpected: cannot read: " +full_path);
			reviewResult.setError("Unexpected: cannot read: " +full_path);
			return reviewResult;
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
				reviewResult.setError(error);
				return reviewResult;
			}
		}
		else {
			// otherwise: assign it here:
			sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			version = sdf.format(date);
		}
		
		
		final String finalUri = namespaceRoot + "/" +
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
			reviewResult.setError(error);
			return reviewResult;
		}
		
		String uriForEmpty = model.getNsPrefixURI("");
		if ( uriForEmpty == null ) {
			// FIXME Get the original ns when model.getNsPrefixURI("") returns null
			// For now, returning error:
			String error = "Unexpected error: No namespace for prefix \"\"";
			log.info(error);
			reviewResult.setError(error);
			return reviewResult;
			
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
		_replaceNameSpace(model, original_ns_, ns_);

		
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
		_setDcAttributes(ont_);
		
		////////////////////////////////////////////////////////////////////////
		// Done with the model. 
		////////////////////////////////////////////////////////////////////////
		
		// Get resulting string:
		String rdf = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV") ;  // XXX newOntModel);
		
		
		reviewResult.setUri(base_);
		

		// write new contents to a new file under previewDir:
		
		File reviewedFile = new File(previewDir, file.getName());
		reviewResult.setFullPath(reviewedFile.getAbsolutePath());

		PrintWriter os;
		try {
			os = new PrintWriter(reviewedFile);
		}
		catch (FileNotFoundException e) {
			log.info("Unexpected: file not found: " +reviewedFile);
			reviewResult.setError("Unexpected: file not found: " +reviewedFile);
			return reviewResult;
		}
		StringReader is = new StringReader(rdf);
		try {
			IOUtils.copy(is, os);
			os.flush();
		}
		catch (IOException e) {
			log.info("Unexpected: IO error while writing to: " +reviewedFile);
			reviewResult.setError("Unexpected: IO error while writing to: " +reviewedFile);
			return reviewResult;
		}

		// Ok, we're done:
		reviewResult.setRdf(rdf);

		return reviewResult;
	}
	
	
	/**
	 * Checks the preexistence of an ontology to determine the possible conflict with an ontology that
	 * is about to be uploaded as *new*.
	 * 
	 * @param orgAbbreviation  Part of the key combination
	 * @param shortName        Part of the key combination
	 * 
	 * @param result setError will be called on this object in case an ontology exists with the given parameters
	 *           or if any error occurred while doing the check.
	 * 
	 * @return true if there is NO existing ontology with the given parameters; false if there IS an existing
	 *            ontology OR some error occurred.  If false is returned, result.getError() will be non-null.
	 */
	private boolean _checkNoPreexistingOntology(String orgAbbreviation, String shortName, BaseResult result) {
		// See issue 63: http://code.google.com/p/mmisw/issues/detail?id=63
		
		// the (unversioned) URI to check for preexisting ontology:
		String possibleOntologyUri = namespaceRoot + "/" +
        							orgAbbreviation + "/" +
        							shortName;

		if ( log.isDebugEnabled() ) {
			log.debug("New submission; checking for preexisting ontology with unversioned URI: " +possibleOntologyUri);
		}
		
		// we just need to know whether this URI resolves:
		boolean possibleOntologyExists = false;
		try {
			int statusCode = Util.httpGet(possibleOntologyUri, "application/rdf+xml");
			if ( log.isDebugEnabled() ) {
				log.debug("HTTP GET status code: " +statusCode+ ": " +HttpStatus.getStatusText(statusCode));
			}
			possibleOntologyExists = statusCode == HttpStatus.SC_OK;
		}
		catch (Exception e) {
			// report the error and return false (we shouldn't continue with the upload):
			String info = "Exception while checking for existence of URI: " +possibleOntologyUri+ " : " +e.getMessage();
			log.error(info, e);
			result.setError(info+ "\n\n Please try later.");
			return false;
		}
		
		if ( possibleOntologyExists ) {
			String info = "There is already a registered ontology with the same " +
							"authority and resource type combination:\n" +
							"   " +possibleOntologyUri;
			
			if ( log.isDebugEnabled() ) {
				log.debug(info);
			}
			
			result.setError(info+ "\n\n" +
					"You will need to change the authority and/or resource topic to be able to " +
					"submit your ontology.\n" +
					"\n" +
					"Note: if you want to submit a new version for the above ontology, " +
					"then you would need to browse to that entry in the main repository interface " +
					"and use one of the available options to create a new version."
			);
			return false;
		}
		
		// OK, no preexisting ontology:
		return true;
	}

	/**
	 * Checks the new ontology URI key combination for possible changes.
	 * 
	 * @param originalOrgAbbreviation  Part of the original key combination
	 * @param originalShortName        Part of the original key combination
	 * 
	 * @param orgAbbreviation  Part of the key combination
	 * @param shortName        Part of the key combination
	 * 
	 * @param result setError will be called on this object if there are any changes in the key combination.
	 * 
	 * @return true if OK. 
	 *         false if there IS any error (result.getError() will be non-null).
	 */
	private boolean _checkUriKeyCombinationForNewVersion(
			String originalOrgAbbreviation, String originalShortName, 
			String orgAbbreviation, String shortName, BaseResult result) {
		
		// See issue 98: http://code.google.com/p/mmisw/issues/detail?id=98
		//               "new version allows the shortName and authority to be changed"
		
		StringBuffer error = new StringBuffer();
		
		if ( ! originalOrgAbbreviation.equals(orgAbbreviation) ) {
			error.append("\n   New authority: \"" +orgAbbreviation+ "\"" +
				     "  Original: \"" +originalOrgAbbreviation+ "\"");
		}
		
		if ( ! originalShortName.equals(shortName) ) {
			error.append("\n   New resource type: \"" +shortName+ "\"" +
					     "  Original: \"" +originalShortName+ "\"");
		}

		if ( error.length() > 0 ) {
			String info = "Key component(s) for the ontology URI have changed: " +error;
			
			if ( log.isDebugEnabled() ) {
				log.debug(info);
			}
			
			result.setError(info+ "\n\n" +
					"The ontology would be submitted as a new entry in the repository " +
					"and not as a new version of the base ontology. " +
					"Please make sure the resource type and the authority are unchanged.\n" +
					"\n" +
					"Note: To submit a new ontology (and not a new version of an existing ontology), " +
					"please use the \"Submit New Ontology\" option in the main repository interface."
			);
			return false;
		}
		
		// OK:
		return true;
	}

	/**
	 * Sets the missing DC attrs that have defined equivalent MMI attrs: 
	 */
	private void _setDcAttributes(Ontology ont_) {
		for ( Property dcProp : MdHelper.getDcPropertiesWithMmiEquivalences() ) {
			
			// does dcProp already have an associated value?
			String value = JenaUtil.getValue(ont_, dcProp); 
			
			if ( value == null || value.trim().length() == 0 ) {
				// No.  
				// Then, take the value from the equivalent MMI attribute if defined:
				Property mmiProp = MdHelper.getEquivalentMmiProperty(dcProp);
				value = JenaUtil.getValue(ont_, mmiProp);
				
				if ( value != null && value.trim().length() > 0 ) {
					// we have a value for DC from the equivalente MMI attr.
					log.info(" Assigning DC attr " +dcProp+ " with " +mmiProp+ " = " +value);
					ont_.addProperty(dcProp, value.trim());
				}
			}
		}
	}


	/**
	 * Does the final upload of the reviewed ontology to the registry.
	 * 
	 * @param reviewResult The result of the preceding review operation
	 * 
	 * @param loginResult Login information
	 * 
	 * @return the result of the upload operation.
	 */
	public UploadResult upload(ReviewResult reviewResult, LoginResult loginResult) {
		
		_getBaseInfoIfNull();
		
		UploadResult uploadResult = new UploadResult();
		
		if ( reviewResult == null ) {
			uploadResult.setError("Please, do the review action first.");
			return uploadResult;
		}
		
		OntologyInfo ontologyInfo = reviewResult.getOntologyInfo();

		// the "new" values in the ontologyInfo are used to fill in some of the
		// fields required by the bioportal back-end
		Map<String, String> newValues = ontologyInfo.getNewValues();
		
		uploadResult.setUri(reviewResult.getUri());
		

		////////////////////////////////////////////
		// check for errors
		
		if ( loginResult == null ) {
			uploadResult.setError("No login information");
		}
		else if ( loginResult.getError() != null ) {
			uploadResult.setError("Authentication has errors ");
		}
		
		if ( uploadResult.getError() != null ) {
			log.info(": error: " +uploadResult.getError());
			return uploadResult;
		}
		
		if ( reviewResult.getError() != null ) {
			uploadResult.setError("there was an error while reviewing the ontology: " +reviewResult.getError());
			log.info(": there was an error while reviewing the ontology: " +reviewResult.getError());
			return uploadResult;
		}
		
		////////////////////////////////////////////
		// load the temporary file into a string

		String full_path = reviewResult.getFullPath();
		log.info("Reading in temporary file: " +full_path);
		
		File file = new File(full_path);
		if ( ! file.canRead() ) {
			log.info("Unexpected: cannot read: " +full_path);
			uploadResult.setError("Unexpected: cannot read: " +full_path);
			return uploadResult;
		}
		
		// Get resulting model:
		String rdf;
		try {
			rdf = readRdf(file);
		}
		catch (IOException e) {
			String error = "Unexpected: IO error while reading from: " +full_path+ " : " +e.getMessage();
			log.info(error);
			reviewResult.setError(error);
			return uploadResult;
		}
		
		// ok, we have our ontology:
		
		
		//////////////////////////////////////////////////////////////////////////
		// finally, do actual upload to MMI registry

		// Get final URI of resulting model
		// FIXME this uses the same original URI
		String uri = reviewResult.getUri();
		
		log.info(": uploading ...");
		String userId = loginResult.getUserId();
		String sessionId = loginResult.getSessionId();
		assert userId != null;
		assert sessionId != null;
		
		String ontologyId = ontologyInfo.getOntologyId();
		if ( ontologyId != null ) {
			log.info("Will create a new version for ontologyId = " +ontologyId);
		}
		
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
				final String orgAbbreviation = newValues.get(OmvMmi.origMaintainerCode.getURI());
				final String shortName = newValues.get(Omv.acronym.getURI());
				if ( ! _checkNoPreexistingOntology(orgAbbreviation, shortName, uploadResult) ) {
					return uploadResult;
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
					userId, sessionId,
					ontologyId,
					newValues
			);
			String res = createOnt.create();
			
			if ( res.startsWith("OK") ) {
				uploadResult.setInfo(res);
				
				// If there is a corresponding CSV, rename it with a name derived from the URI: 
				String origPathCsv = reviewResult.getOntologyInfo().getFullPathCsv();
				if ( origPathCsv != null ) {
					
					File origFileCsv = new File(origPathCsv);
					if ( origFileCsv.exists() ) {
						// the simple name will be the same path of the URI with separators replaced 
						// with underscore '_', and with ".csv" appended:
						String destPathCsv = new URL(uri).getPath();
						destPathCsv = destPathCsv.replaceAll("/|\\\\", "_") + ".csv";
						File destFileCsv = new File(origFileCsv.getParentFile(), destPathCsv);
						
						boolean renRes = origFileCsv.renameTo(destFileCsv);
						log.debug("upload: renaming " +origFileCsv+ " -> " +destFileCsv+ " returned " +renRes);
					}
				}
			}
			else {
				uploadResult.setError(res);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			uploadResult.setError(ex.getClass().getName()+ ": " +ex.getMessage());
		}
		
		log.info("uploadResult = " +uploadResult);
		
		return uploadResult;
	}

	
//	private String getOntologyOwl(String ontologyUri) {
//		
//		URL url = new URL(ontologyUri);
//	}
	
	
	private File getLocalOntologyFile(String ontologyUri) throws Exception {
		String ontologyUri_lpath = ontologyUri+ "?_lpath";
		
		URL url = new URL(ontologyUri_lpath);

		InputStream is = url.openStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String full_path = br.readLine();

		if ( full_path.startsWith("ERROR") ) {
			String error = "Getting local path returned: " +full_path;
			log.info(error);
			throw new Exception(error);
		}
		
		// full_path should be ok here.
		
		log.info("getLocalOntologyFile: local path: " +full_path);
		
		File file = new File(full_path);
		
		return file;
	}
	
	
	public OntologyInfo getOntologyInfoFromRegistry(String ontologyUri) {
		OntologyInfo ontologyInfo = new OntologyInfo();
		
		
		if ( false ) {        // TODO remove this previous mechanism
			
			// Note: we assume this ontmd service is located in the same server as the "ont" service.
			// We request the local path and then directly load the ontology to obtain
			// some of the associated attributes.

			File file;
			try {
				file = getLocalOntologyFile(ontologyUri);
			}
			catch (Exception ex) {
				String error = ex.getClass().getName()+ " : " +ex.getMessage();
				log.info(error);
				ontologyInfo.setError(error);
				return ontologyInfo;
			}
			String error = prepareOntologyInfo(file, ontologyInfo);
			if ( error != null ) {
				ontologyInfo.setError(error);
				return ontologyInfo;
			}

			
			try {
				ontologyInfo.setRdf(readRdf(file));
			}
			catch (Throwable e) {
				 error = "Cannot read RDF model: " +file+ " : " +e.getMessage();
				log.info(error);
				ontologyInfo.setError(error);
				return ontologyInfo;
			}

			///////////////////////////////////////////////////////////////////////////////
			// .csv
			String destPathCsv;
			try {
				destPathCsv = new URL(ontologyUri).getPath();
				destPathCsv = destPathCsv.replaceAll("/|\\\\", "_") + ".csv";
				File fileCsv = new File(Config.Prop.ONTMD_VOC2RDF_DIR.getValue() + destPathCsv);
				if ( fileCsv.exists() ) {
					ontologyInfo.setFullPathCsv(fileCsv.getAbsolutePath());
				}
				
				if ( log.isDebugEnabled() ) {
					log.debug("getOntologyInfoFromRegistry: fileCsv=" +fileCsv+ " exists=" +fileCsv.exists());
				}

			}
			catch (MalformedURLException e) {
				log.error("shouldn't happen", e);
				ontologyInfo.setError(e.getMessage());
				return ontologyInfo;
			}
			///////////////////////////////////////////////////////////////////////////////

		}
		else { // new mechanism
			
			
			File file;
			try {
				file = getLocalOntologyFile(ontologyUri);
			}
			catch (Exception ex) {
				String error = ex.getClass().getName()+ " : " +ex.getMessage();
				log.info(error);
				ontologyInfo.setError(error);
				return ontologyInfo;
			}
			String error = prepareOntologyInfo(file, ontologyInfo);
			if ( error != null ) {
				ontologyInfo.setError(error);
				return ontologyInfo;
			}
			
			
			// note: make sure we request the OWL format of the ontology, so adjust extension;
			try {
				MmiUri mmiUri = new MmiUri(ontologyUri);
				ontologyUri = mmiUri.getOntologyUriWithExtension(".owl");
			}
			catch (URISyntaxException e1) {
				error = "shouldn't happen: " +e1.getMessage();
				log.error(error, e1);
				ontologyInfo.setError(error);
				return ontologyInfo;
			}
			
			if ( log.isDebugEnabled() ) {
				log.debug("getOntologyInfoFromRegistry: ontologyUri=" +ontologyUri);
			}
			
			try {
				URL url = new URL(ontologyUri);
				InputStream is = url.openStream();
				StringWriter os = new StringWriter();
				IOUtils.copy(is, os);
				ontologyInfo.setRdf(os.toString());
				
				error = prepareOntologyInfoFromUri(ontologyUri, ontologyInfo);
				if ( error != null ) {
					ontologyInfo.setError(error);
					return ontologyInfo;
				}
			}
			catch (Exception e) {
				error = "Cannot read RDF model: " +ontologyUri+ " : " +e.getMessage();
				log.info(error);
				ontologyInfo.setError(error);
				return ontologyInfo;
			}
			
			// CSV not prepared here.  See getData
			
		}
		
		return ontologyInfo;
	}
	
	
	/**
	 * Gets info about an ontology stored in a filesystem file on the server.
	 * 
	 * If the given path is relative, then it is taken as relative to {@link Config.Prop#ONTMD_VOC2RDF_DIR}.
	 * In this case, if a similar filename but with extension ".csv" is found, then
	 * the .
	 * 
	 * @param path the path to the file. 
	 */
	public OntologyInfo getOntologyInfoFromFileOnServer(String path) {
		log.info("getOntologyInfoFromFileOnServer: local path: " +path);
		File file = new File(path);
		
		File fileCsv = null;
		
		if ( !file.isAbsolute() ) {
			
			// then, the the full path is to be completed here.
			// TODO Note: Currently, this is only handled for the Voc2Rdf case:
			
			file = new File(Config.Prop.ONTMD_VOC2RDF_DIR.getValue() + path);
			
			fileCsv = new File(Config.Prop.ONTMD_VOC2RDF_DIR.getValue() + path + ".csv");
			if ( ! fileCsv.exists() ) {
				fileCsv = null;
			}
		}

		String full_path = file.getAbsolutePath();
		
		OntologyInfo ontologyInfo = new OntologyInfo();
		
		try {
			ontologyInfo.setRdf(readRdf(file));
		}
		catch (Throwable e) {
			String error = "Cannot read RDF model: " +full_path+ " : " +e.getMessage();
			log.info(error);
			ontologyInfo.setError(error);
			return ontologyInfo;
		}

		String error = prepareOntologyInfo(file, ontologyInfo);
		if ( error == null ) {
			if ( fileCsv != null ) {
				String fullPathCsv = fileCsv.getAbsolutePath();
				ontologyInfo.setFullPathCsv(fullPathCsv);
			}
		}
		else {
			ontologyInfo.setError(error);
		}
		
		return ontologyInfo;
	}

	///////////////////////////////////////////////////////////////////////
	// Voc2RDF
	
	
	private Voc2RdfImpl voc2rdf;
	
	public AppInfo getVoc2RdfAppInfo() {
		return voc2rdf.getAppInfo();
	}
	
	public Voc2RdfBaseInfo getVoc2RdfBaseInfo() {
		return voc2rdf.getBaseInfo();
	}
	
	public ConversionResult convert2Rdf(Map<String,String> values) {
		return voc2rdf.convert(values);
	}

	
	///////////////////////////////////////////////////////////////////////
	// data
	
	public DataResult getData(OntologyInfo ontologyInfo) {
		DataResult dataResult = new DataResult();
		dataResult.setOntologyInfo(ontologyInfo);
		
		String ontologyUri = ontologyInfo.getUri();
		if ( log.isDebugEnabled() ) {
			log.debug("getData: ontologyUri=" +ontologyUri);
		}
		
		try {
			URL url = new URL(ontologyUri  + "?_csv");
			InputStream is = url.openStream();
			StringWriter os = new StringWriter();
			IOUtils.copy(is, os);
			
			String result = os.toString();
			if ( result.startsWith("ERROR") ) {
				log.info(result);
				dataResult.setError(result);
			}
			else {
				dataResult.setCsv(result);
			}
		}
		catch (Exception e) {
			String error = "Cannot read CSV for: " +ontologyUri+ " : " +e.getMessage();
			log.info(error);
			dataResult.setError(error);
		}
		
		return dataResult;
	}
	
	
	///////////////////////////////////////////////////////////////////////
	// Portal
	
	private PortalImpl portal;
	
	public AppInfo getPortalAppInfo() {
		return portal.getAppInfo();	
	}
	
	public PortalBaseInfo getPortalBaseInfo() {
		return portal.getBaseInfo();
	}
	
	public List<org.mmisw.iserver.gwt.client.rpc.OntologyInfo> getAllOntologies() {
		return portal.getAllOntologies();
	}


}
