package org.mmisw.ontmd.gwt.server;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mmisw.ont.vocabulary.util.MdHelper;
import org.mmisw.ontmd.gwt.client.rpc.BaseInfo;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.rpc.OntMdService;
import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.UploadResult;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrGroup;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.drexel.util.rdf.JenaUtil;



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
		if ( ! file.canRead() ) {
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
		ontologyInfo.setRdf("TODO(contents of RDF)"); // TODO put RDF also?
	
		return ontologyInfo;
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
	
	public UploadResult upload(OntologyInfo conversionResult, LoginResult loginResult) {
		
		UploadResult uploadResult = new UploadResult();

		String userId = null;
		String sessionId = null;

		if ( loginResult == null ) {
			uploadResult.setError("No login information");
		}
		else if ( loginResult.getError() != null ) {
			uploadResult.setError("Authentication has errors ");
		}
		else {
			userId = loginResult.getUserId();
			sessionId = loginResult.getSessionId();
		}
		
		if ( uploadResult.getError() != null ) {
			log.info(": error: " +uploadResult.getError());
			return uploadResult;
		}
		
		log.info(": uploading ...");
		try {
			String uri = conversionResult.getFinalUri();
			String fileName;
			fileName = new URL(uri).getPath();
			String rdf = conversionResult.getRdf();
			
			// TODO: use some of the metadata in values map to set some of the aquaportal attributes
			OntologyUploader createOnt = new OntologyUploader(uri, fileName, rdf, userId, sessionId);
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
