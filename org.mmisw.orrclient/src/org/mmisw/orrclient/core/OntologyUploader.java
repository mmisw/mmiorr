package org.mmisw.orrclient.core;

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
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.orrclient.IOrrClient;
import org.mmisw.orrclient.OrrClientConfiguration;
import org.mmisw.orrclient.core.util.OntServiceUtil;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;

import com.hp.hpl.jena.vocabulary.DC;

/** 
 * A helper class to upload ontologies into the repository.
 * 
 * @author Carlos Rueda
 */
class OntologyUploader {
	
	static final String ONTOLOGIES  = "/ontologies";

	private static final String CHARSET_UTF8 = "UTF-8";
	
	private final Log log = LogFactory.getLog(OntologyUploader.class);
	
	private FilePart filePart;
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
		
		this.uri = uri;
		this.loginResult = loginResult;
		this.ontologyId = ontologyId;
		this.ontologyUserId = ontologyUserId;
		this.values = values;
		
		PartSource partSource = new ByteArrayPartSource(fileName, RDF.getBytes(CHARSET_UTF8));
		filePart = new FilePart("filePath", partSource);
		filePart.setCharSet(CHARSET_UTF8);
	}
	
	
	/**
	 * Executes the POST operation to upload the ontology.
	 * 
	 * @return The message in the response from the POST operation, prefixed with "OK:" if
	 *         the result was successful; otherwise, the description of the error 
	 *         prefixed with "ERROR:"
	 *         
	 * @throws Exception
	 */
	String create()	throws Exception {
		
		String sessionId = loginResult.getSessionId();
		
		String bioPortalRestUrl = OntServiceUtil.getAquaportalRestUrl();
		String ontologiesRestUrl = bioPortalRestUrl + ONTOLOGIES;
		if ( log.isDebugEnabled() ) {
			log.debug("ontologiesRestUrl = " +ontologiesRestUrl);
		}
		PostMethod post = new PostMethod(ontologiesRestUrl);
		try {
			List<Part> partList = new ArrayList<Part>();
			
			partList.add(filePart);
			partList.add(new StringPart("sessionid", sessionId));
			
			
			// aquaportal version handling:
			if (  ontologyId != null ) {
				// put the ontologyId as reference for the creation of the new version
				partList.add(new StringPart("ontologyId", ontologyId));
			}
				
			String userId = _getUserId();
			partList.add(new StringPart("userId", userId));
			
			partList.add(new StringPart("urn", uri));
			
			String displayLabel = _getDisplayLabel();
			partList.add(new StringPart("displayLabel", displayLabel));
			
			String dateReleased = _getDateReleased();
			partList.add(new StringPart("dateReleased", dateReleased));

			String contactName = _getContactName();
			partList.add(new StringPart("contactName", contactName));

			String contactEmail = _getContactEmail();
			partList.add(new StringPart("contactEmail", contactEmail));

			String versionNumber = _getVersionNumber();
			partList.add(new StringPart("versionNumber", versionNumber));
			
			String versionStatus = _getVersionStatus();
			partList.add(new StringPart("versionStatus", versionStatus));

			// FIXME: the following are hard-coded for now as they are NOT used by us (ORR)
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

	private String _getVersionStatus() {
		// TODO a more general mechanism to assign versionStatus
		String versionStatus = "testing";
		return versionStatus;
	}


	private String _getVersionNumber() {
		String versionNumber = values.get(Omv.version.getURI());
		if ( versionNumber == null ) {
			// shouldn't happen; 
			versionNumber = "0.0.0";
		}
		return versionNumber;
	}


	private String _getContactEmail() {
		// TODO: define something like: OmvMmi.contactEmail
		String contactEmail = "";
		return contactEmail;
	}


	/** Gets the value for the StringPart "userId" */
	private String _getUserId() {
		//
		// if the user logged in is an administrator and ontologyUserId is not null,
		// then use the given ontologyUserId instead of the administrator:
		//
		if ( loginResult.isAdministrator() && ontologyUserId != null ) {
			//
			// TODO: NOTE this is possible because the back-end does not check
			// that the session has to be used in combination with the logged in user. 
			// So, the overall mechanism should be revisited later, especially if we upgrade.
			//
			return ontologyUserId;
		}
		else {
			// otherwise just use the user logged in:
			return loginResult.getUserId();
		}
	}


	private String _getDisplayLabel() {
		String displayLabel = values.get(Omv.name.getURI());
		if ( displayLabel == null ) {
			// shouldn't happen, but, well assign the same uri:
			displayLabel = uri;
		}
		return displayLabel;
	}


	private String _getDateReleased() {
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
		return dateReleased;
	}


	/**
	 * Gests the value to use for the "contact_name".
	 * <p>
	 * Issue #236 "Author column should show Content Creator" <br/>
	 * 
	 * Note: the value that is shown in the main ontology table at ORR comes from
	 * the contact_name field in the aquaportal database. ORR could extract the
	 * contentCreator value from the metadata associated with the ontology, but
	 * such metadata is NOT yet available when the table is displayed (the metadata
	 * is obtained on demand when a particular ontology is to be displayed in more detail).
	 * So, instead of doing that change in the ORR module, the following change is done
	 * here: use the value of OmvMmi.hasContentCreator to fill in contact_name if such
	 * value is available; otherwise, just use the value of Omv.hasCreator or DC.creator
	 * as was done prior to this change.
	 */
	private String _getContactName() {
		// try hasContentCreator:
		String value = values.get(OmvMmi.hasContentCreator.getURI());
		if ( value != null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("_getContactName: using value of OmvMmi.hasContentCreator: " +value);
			}
			return value;
		}

		// try Omv.hasCreator or DC.creator as before:
		value = values.get(Omv.hasCreator.getURI());
		if ( value != null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("_getContactName: using value of Omv.hasCreator: " +value);
			}
			return value;
		}
		
		// shouldn't happen; try with DC.creator
		value = values.get(DC.creator.getURI());
		if ( value != null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("_getContactName: using value of DC.creator: " +value);
			}
			return value;
		}

		// shouldn't happen; just assign ""
		value = "";
		if ( log.isDebugEnabled() ) {
			log.debug("_getContactName: no value available. Using \"\"");
		}
		
		return value;
	}
}
