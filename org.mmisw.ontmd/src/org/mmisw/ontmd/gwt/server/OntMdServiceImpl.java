package org.mmisw.ontmd.gwt.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.drexel.util.rdf.JenaUtil;
import edu.drexel.util.rdf.OwlModel;



/**
 * Implementation of Voc2RdfService. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class OntMdServiceImpl extends RemoteServiceServlet implements OntMdService {
	private static final long serialVersionUID = 1L;

	
	private static class MyLog { void info(String m) { System.out.println("LOG: " +m); } }
	private final MyLog log = new MyLog();
//	private final Log log = LogFactory.getLog(OntMdServiceImpl.class);
	
	private BaseInfo baseInfo = null;
	
	
	public BaseInfo getBaseInfo() {
		if ( baseInfo == null ) {
			prepareBaseInfo();
		}
		return baseInfo;
	}
	
	private void prepareBaseInfo() {
		log.info("preparing base info ...");
		baseInfo = new BaseInfo();
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
	

	
	public OntologyInfo getOntologyInfo(String uploadResults) {
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
		catch (IOException e) {
			log.info("Unexpected: cannot read: " +full_path);
			ontologyInfo.setError("Unexpected: cannot read: " +full_path);
			return ontologyInfo;
		}

		String uriFile = file.toURI().toString();
		log.info("Loading model: " +uriFile);
		Model model = JenaUtil.loadModel(uriFile, false);

		Resource ontRes = JenaUtil.getFirstIndividual(model, OWL.Ontology);
		
		getBaseInfo();
		
		Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
		Map<String,String> values = new HashMap<String, String>();
		
		for ( AttrGroup attrGroup : baseInfo.getAttrGroups() ) {
			for ( AttrDef attrDef : attrGroup.getAttrDefs() ) {
				Property dcProp = uriPropMap.get(attrDef.getUri());
				String value = JenaUtil.getValue(ontRes, dcProp);
				if (value == null) {
					continue;
				}
				log.info("Assigning: " +attrDef.getUri()+ " = " + value);
				values.put(attrDef.getUri(), value);
			}
		}
		
		ontologyInfo.setValues(values);
		ontologyInfo.setFullPath(full_path);
		
		// associate the original base URI:
		String uri = model.getNsPrefixURI("");
		if ( uri != null ) {
			String base_ = JenaUtil.getURIForBase(uri);
			ontologyInfo.setUri(base_);
		}
	
		return ontologyInfo;
	}
	
	/**
	 * Reads a file.
	 * @param file the file to read in
	 * @return the contents of the text file.
	 * @throws IOException 
	 */
	private String readRdf(File file) throws IOException {
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
		
		getBaseInfo();
		
		ReviewResult reviewResult = new ReviewResult();
		reviewResult.setFullPath(ontologyInfo.getFullPath());
		reviewResult.setOntologyInfo(ontologyInfo);
		
		
		Map<String, String> values = ontologyInfo.getValues();
		
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
			reviewResult.setError("there was an error while loading the ontology: " +ontologyInfo.getError());
			log.info(": there was an error while loading the ontology: " +ontologyInfo.getError());
			return reviewResult;
		}
		
		String orgAbbreviation = values.get(OmvMmi.origMaintainerCode.getURI());
		String primaryClass = values.get(Omv.acronym.getURI());

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
		
		String uriFile = file.toURI().toString();
		OntModel model = JenaUtil.loadModel(uriFile, false);
		
		OwlModel newOntModel = new OwlModel(model);

		///////////////////////////////////////////////////////
		// Update attributes in model:

		log.info("Updating metadata attributes ...");

		
		// ontology root:
		final String namespaceRoot = "http://mmisw.org/ont";
		
		// version:
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		final String version = sdf.format(date);
		
//		String finalUri = model.getNsPrefixURI("");
		String finalUri = namespaceRoot + "/" +
		                        orgAbbreviation + "/" +
		                        version + "/" +
		                        primaryClass;

		
		
		String ns_ = JenaUtil.getURIForNS(finalUri);
		String base_ = JenaUtil.getURIForBase(finalUri);
		
		newOntModel.setNsPrefix("", ns_);
		Map<String, String> preferredPrefixMap = MdHelper.getPreferredPrefixMap();
		for ( String uri : preferredPrefixMap.keySet() ) {
			String prefix = preferredPrefixMap.get(uri);
			newOntModel.setNsPrefix(prefix, uri);
		}
		
		Ontology ont = newOntModel.createOntology(base_);
		log.info("New ontology created with namespace " + ns_ + " base " + base_);
		
		
		Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
		for ( String uri : values.keySet() ) {
			String value = values.get(uri).trim();
			if ( value.length() > 0 ) {
				Property prop = uriPropMap.get(uri);
				if ( prop == null ) {
					log.info("No property found for uri='" +uri+ "'");
					continue;
				}
				log.info("assigning: " +uri+ " = " +value);
				ont.addProperty(prop, value);
			}
		}
		
		// set Omv.uri from final
		ont.addProperty(Omv.uri, base_);
		ont.addProperty(Omv.version, version);

		
		reviewResult.setUri(base_);
		

		// Get resulting model:
		String rdf = JenaUtil.getOntModelAsString(newOntModel);
		
		// update the temporary file:
		if ( ! file.canWrite() ) {
			log.info("Unexpected: cannot write: " +full_path);
			reviewResult.setError("Unexpected: cannot write: " +full_path);
			return reviewResult;
		}
		
		PrintWriter os;
		try {
			os = new PrintWriter(file);
		}
		catch (FileNotFoundException e) {
			log.info("Unexpected: file not found: " +full_path);
			reviewResult.setError("Unexpected: file not found: " +full_path);
			return reviewResult;
		}
		StringReader is = new StringReader(rdf);
		try {
			IOUtils.copy(is, os);
			os.flush();
		}
		catch (IOException e) {
			log.info("Unexpected: IO error while writing to: " +full_path);
			reviewResult.setError("Unexpected: IO error while writing to: " +full_path);
			return reviewResult;
		}

		// Ok, we're done:
		reviewResult.setRdf(rdf);

		return reviewResult;
	}
	
	
	/**
	 * Does the final upload of the temporary file (which is assumed to be already updated)
	 * to the registry.
	 */
	public UploadResult upload(ReviewResult reviewResult, LoginResult loginResult) {
		
		getBaseInfo();
		
		// the values from the ontologyInfo are used to fill in some of the
		// fields required by the bioportal back-end
		Map<String, String> values = reviewResult.getOntologyInfo().getValues();
		
		UploadResult uploadResult = new UploadResult();
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
			log.info("Unexpected: IO error while reading from: " +full_path);
			reviewResult.setError("Unexpected: IO error while reading from: " +full_path);
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
			
			OntologyUploader createOnt = new OntologyUploader(uri, fileName, rdf, userId, sessionId, values);
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

}
