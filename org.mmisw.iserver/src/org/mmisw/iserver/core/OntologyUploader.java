package org.mmisw.iserver.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;

import com.hp.hpl.jena.vocabulary.DC;

/** 
 * A helper class to upload ontologies into the repository.
 * 
 * @author Carlos Rueda
 */
class OntologyUploader {
	
	private static String SERVER    = "http://mmisw.org";
	private static String REST      = SERVER+ "/bioportal/rest";
	static final String ONTOLOGIES  = REST+ "/ontologies";
	
	private final Log log = LogFactory.getLog(OntologyUploader.class);
	
	
	private PartSource partSource;
	private String uri;
	private LoginResult loginResult;
	
	private String ontologyId;
	private String ontologyUserId;

	private Map<String, String> values;
	

	
	/**
	 * Constructor.
	 * @param uri
	 * @param fileName
	 * @param RDF Contents of the ontology
	 * @param userId
	 * @param sessionId
	 * @param ontologyId Aquaportal ontology ID when creating a new version.
	 * 
	 * @param values   Used to fill in some of the fields in the aquaportal request
	 * @throws Exception
	 */
	OntologyUploader(String uri, String fileName, String RDF, 
			LoginResult loginResult,
			String ontologyId, String ontologyUserId,
			Map<String, String> values
	) throws Exception {
		PartSource partSource = new ByteArrayPartSource(fileName, RDF.getBytes());
		
		this.partSource = partSource;
		this.uri = uri;
		this.loginResult = loginResult;
		this.ontologyId = ontologyId;
		this.ontologyUserId = ontologyUserId;
		this.values = values;
	}
	

	/**
	 * Executes the POST operation to upload the ontology.
	 * 
	 * @return The message in the response from the POST operation, prefixed with "OK:" if
	 *         the result was successfull; otherwise, the description of the error 
	 *         prefixed with "ERROR:"
	 *         
	 * @throws Exception
	 */
	String create()	throws Exception {
		
		String sessionId = loginResult.getSessionId();
		
		PostMethod post = new PostMethod(ONTOLOGIES);
		try {
			List<Part> partList = new ArrayList<Part>();
			
			partList.add(new FilePart("filePath", partSource));
			partList.add(new StringPart("sessionid", sessionId));
			
			
			// aquaportal version handling:
			if (  ontologyId != null ) {
				// put the ontologyId as reference for the creation of the new version
				partList.add(new StringPart("ontologyId", ontologyId));
			}
				
			
			// if the user logged in is an administrator and ontologyUserId is not null,
			// then use the given ontologyUserId instead of the administrator:
			if ( loginResult.isAdministrator() && ontologyUserId != null ) {
				//
				// TODO: NOTE that this is possible because the back-end does not check
				// that the session has to be used in combination with the logged in user. 
				// So, the overall mechanism should be revisited later, especially if we upgrade.
				//
				partList.add(new StringPart("userId", ontologyUserId));
			}
			else {
				// otherwise just use the user logged in:
				partList.add(new StringPart("userId", loginResult.getUserId()));
			}

			
			partList.add(new StringPart("urn", uri));
			
			String displayLabel = values.get(Omv.name.getURI());
			if ( displayLabel == null ) {
				// shouldn't happen, but, well assign the same uri:
				displayLabel = uri;
			}
			partList.add(new StringPart("displayLabel", displayLabel));
			
			// TODO use a proper "dateReleased" md attribute - now using Omv.creationDate
			// NOTE: couldn't use the following directly:
			//String dateReleased = values.get(Omv.creationDate.getURI());
			// because it seems the REST service requires the date to be in MM/dd/yyyy format
			String dateReleased = null;  // ... so, ...
			if ( dateReleased == null ) {
				// take current date to provide this field:
				Date date = new Date(System.currentTimeMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
				dateReleased = sdf.format(date);
			}
			partList.add(new StringPart("dateReleased", dateReleased));

			String contactName = values.get(Omv.hasCreator.getURI());
			if ( contactName == null ) {
				// shouldn't happen; try with DC.creator
				contactName = values.get(DC.creator.getURI());
				if ( contactName == null ) {
					// shouldn't happen; just assign ""
					contactName = "";
				}
			}
			partList.add(new StringPart("contactName", contactName));

			// TODO: define something like: OmvMmi.contactEmail
			String contactEmail = "";
			partList.add(new StringPart("contactEmail", contactEmail));

			String versionNumber = values.get(Omv.version.getURI());
			if ( versionNumber == null ) {
				// shouldn't happen; 
				versionNumber = "0.0.0";
			}
			partList.add(new StringPart("versionNumber", versionNumber));
			
			// TODO a more general mechanism to assign versionStatus
			String versionStatus = "testing";
			partList.add(new StringPart("versionStatus", versionStatus));

			// FIXME: the following are hard-coded for now
			partList.add(new StringPart("format", "OWL-DL"));
			partList.add(new StringPart("isRemote", "0"));
			partList.add(new StringPart("statusId", "1"));
			partList.add(new StringPart("isReviewed", "1"));
			partList.add(new StringPart("codingScheme", "1"));
			partList.add(new StringPart("isManual", "1"));
			partList.add(new StringPart("isFoundry", "0"));

			// TODO: handle "categories" ?  For now, putting an arbitrary value
			// from the existing values in the default bioportal database
			partList.add(new StringPart("categoryId", "2809"));

			
			// now, perform the POST:
			Part[] parts = partList.toArray(new Part[partList.size()]);
			post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().
			getParams().setConnectionTimeout(5000);

			log.info("Executing POST ...");

			String msg;
			int status = client.executeMethod(post);
			if (status == HttpStatus.SC_OK) {
				msg = post.getResponseBodyAsString();
//				log.info("Upload complete, response=" + msg);
				msg = "OK:" +msg;
			} 
			else {
				String body = post.getResponseBodyAsString();
				msg = HttpStatus.getStatusText(status);
				log.info("Upload failed, response=" +msg+ "\n" +body);
				msg = "ERROR:" +msg+ "\n" +body ;
			}
			
			return msg;
		} 
		finally {
			post.releaseConnection();
		}

	}
}
