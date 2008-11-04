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

import org.apache.commons.io.IOUtils;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.ont.vocabulary.util.MdHelper;
import org.mmisw.ontmd.gwt.client.rpc.BaseInfo;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.rpc.OntMdService;
import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.ReviewResult;
import org.mmisw.ontmd.gwt.client.rpc.UploadResult;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrGroup;

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

	
	private static final File previewDir = new File(Config.ONTMD_PREVIEW_DIR);

	
	private static class MyLog { void info(String m) { System.out.println("LOG: " +m); } }
	private final MyLog log = new MyLog();
//	private final Log log = LogFactory.getLog(OntMdServiceImpl.class);
	
	private BaseInfo baseInfo = null;
	
	
	public BaseInfo getBaseInfo() {
		// from the client, always re-create the base info:
		prepareBaseInfo();
		return baseInfo;
	}
	
	
	/** prepares the baseInfo only if not already prepared */
	private void _getBaseInfoIfNull() {
		if ( baseInfo == null ) {
			prepareBaseInfo();
		}
	}
	
	/** always re-creates the baseInfo */
	private void prepareBaseInfo() {
		log.info("preparing base info ...");
		baseInfo = new BaseInfo();
		MdHelper.prepareGroups();
		AttrGroup[] attrGroups = MdHelper.getAttrGroups();
		baseInfo.setAttrGroups(attrGroups);
		log.info("preparing base info ... DONE");
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
	 * @param file  The ontology file.
	 * @param ontologyInfo  The object to be completed
	 */
	private String prepareOntologyInfo(File file, OntologyInfo ontologyInfo) {
		String uriFile = file.toURI().toString();
		log.info("Loading model: " +uriFile);
		OntModel model;
		
		try {
			model = JenaUtil.loadModel(uriFile, false);
		}
		catch (Throwable ex) {
			String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
			log.info(error);
			return error;
		}
		
		debugOntModel(model);

		String full_path = file.getAbsolutePath();
		
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
						log.info("Assigning: " +attrDef.getUri()+ " = " + value);
						originalValues.put(attrDef.getUri(), value);

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
		ontologyInfo.setFullPath(full_path);
		
		// associate the original base URI:
		String uri = model.getNsPrefixURI("");
		if ( uri != null ) {
			String base_ = JenaUtil.getURIForBase(uri);
			ontologyInfo.setUri(base_);
		}

		// OK:
		return null;
	}
	

	private void debugOntModel(OntModel model) {
		StmtIterator stmts = model.listStatements();
		while ( stmts.hasNext() ) {
			Statement stmt = stmts.nextStatement();
			log.info(" #### " +stmt);
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
	private void _replaceNameSpace(OntModel model, String oldNameSpace, String newNameSpace) {
		
		log.info(" REPLACING NS " +oldNameSpace+ " WITH " +newNameSpace);
		
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

			if ( sbj.getNameSpace().equals(oldNameSpace) ) {
				n_sbj = model.createResource(newNameSpace + sbj.getLocalName());
				any_change = true;
			}
			if ( prd.getNameSpace().equals(oldNameSpace) ) {
				n_prd = model.createProperty(newNameSpace + prd.getLocalName());
				any_change = true;
			}
			if ( (obj instanceof Resource) && ((Resource) obj).getNameSpace().equals(oldNameSpace) ) {
				n_obj = model.createResource(newNameSpace + ((Resource) obj).getLocalName());
				any_change = true;
			}

			if ( any_change ) {
				o_stmts.add(o_stmt);
				Statement n_stmt = model.createStatement(n_sbj, n_prd, n_obj);
				n_stmts.add(n_stmt);
				log.info(" #### " +o_stmt);
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
		
		String orgAbbreviation = newValues.get(OmvMmi.origMaintainerCode.getURI());
		String primaryClass = newValues.get(Omv.acronym.getURI());

		if ( orgAbbreviation == null ) {
			log.info("missing origMaintainerCode");
			reviewResult.setError("missing origMaintainerCode");
			return reviewResult;
		}
		if ( primaryClass == null ) {
			log.info("missing acronym");
			reviewResult.setError("missing acronym");
			return reviewResult;
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
		
		
		
		// ontology root:
		final String namespaceRoot = "http://mmisw.org/ont";
		
		// version:
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		final String version = sdf.format(date);
		
//		String finalUri = model.getNsPrefixURI("");
		final String finalUri = namespaceRoot + "/" +
		                        orgAbbreviation + "/" +
		                        version + "/" +
		                        primaryClass;
		
		final String ns_ = JenaUtil.getURIForNS(finalUri);
		final String base_ = JenaUtil.getURIForBase(finalUri);
		

		
		
		
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
		
		final String original_ns_ = JenaUtil.getURIForNS(uriForEmpty);
		
		
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
		
		//////////////////////////////////////////////////
		// transfer any preexisting attributes, and then remove all properties from
		// pre-existing ontRes so just the new OntModel gets added.
		if ( ontRes != null ) {
			for ( Statement st : prexistStatements ) {
				log.info("  Transferring: " +st.getSubject()+ " :: " +st.getPredicate()+ " :: " +st.getObject());
				newOntModel.add(ont_, st.getPredicate(), st.getObject());
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
		
		// set some internal attributes (ie, internally computed)
		
		ont_.addProperty(Omv.uri, base_);
		ont_.addProperty(Omv.version, version);

		// those internal attributes also updated in the values map:
		newValues.put(Omv.uri.getURI(), base_);
		newValues.put(Omv.version.getURI(), version);

		// Set the missing DC attrs that have defined e	equivalent MMI attrs: 
		_setDcAttributes(ont_);
		
		////////////////////////////////////////////////////////////////////////
		// Done with the model. 
		////////////////////////////////////////////////////////////////////////
		
		// Get resulting string:
		String rdf = JenaUtil.getOntModelAsString(model) ;  // XXX newOntModel);
		
		
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
	 * Does the final upload of the previewed file.
	 * to the registry.
	 */
	public UploadResult upload(ReviewResult reviewResult, LoginResult loginResult) {
		
		_getBaseInfoIfNull();
		
		UploadResult uploadResult = new UploadResult();
		
		if ( reviewResult == null ) {
			uploadResult.setError("Please, do the review action first.");
			return uploadResult;
		}

		// the "new" values in the ontologyInfo are used to fill in some of the
		// fields required by the bioportal back-end
		Map<String, String> newValues = reviewResult.getOntologyInfo().getNewValues();
		
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
		
		try {
			
			String fileName;
			fileName = new URL(uri).getPath();
			
			//
			// make sure the fileName ends with ".owl" as the aquaportal back-end seems
			// to add that fixed extension in some operations (at least in the parse operation)
			//
			if ( ! fileName.toLowerCase().endsWith(".owl") ) {
				log.info("upload: setting file extension to .owl per aquaportal requirement.");
				fileName += ".owl";
			}
			
			OntologyUploader createOnt = new OntologyUploader(uri, fileName, rdf, userId, sessionId, newValues);
			String res = createOnt.create();
			
			if ( res.startsWith("OK") ) {
				uploadResult.setInfo(res);
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

	
	public OntologyInfo getOntologyInfoFromRegistry(String ontologyUri) {
		OntologyInfo ontologyInfo = new OntologyInfo();
		
		String ontologyUri_lpath = ontologyUri+ "?_lpath";
		
		URL url;
		try {
			url = new URL(ontologyUri_lpath);
		}
		catch (MalformedURLException ex) {
			String error = ex.getClass().getName()+ " : " +ex.getMessage();
			log.info(error);
			ontologyInfo.setError(error);
			return ontologyInfo;
		}
		
		String full_path ;
		try {
			InputStream is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			full_path = br.readLine();
		}
		catch (IOException ex) {
			String error = ex.getClass().getName()+ " : " +ex.getMessage();
			log.info(error);
			ontologyInfo.setError(error);
			return ontologyInfo;
		}

		if ( full_path == null ) {
			String error = "Could not get local path of ontology";
			log.info(error);
			ontologyInfo.setError(error);
			return ontologyInfo;
		}

		if ( full_path.startsWith("ERROR") ) {
			String error = "Getting local path returned: " +full_path;
			log.info(error);
			ontologyInfo.setError(error);
			return ontologyInfo;
		}
		
		// full_path should be ok here.
		
		log.info("getOntologyInfoFromRegistry: local path: " +full_path);
		
		// now, complete the ontology object, except the Rdf string:
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

		String error = prepareOntologyInfo(file, ontologyInfo);
		if ( error != null ) {
			ontologyInfo.setError(error);
		}
		
		return ontologyInfo;
	}
}
