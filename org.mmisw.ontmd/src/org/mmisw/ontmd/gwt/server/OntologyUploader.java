package org.mmisw.ontmd.gwt.server;

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
import org.mmisw.ont.vocabulary.Omv;

import com.hp.hpl.jena.vocabulary.DC;

/** 
 * A helper to upload ontologies.
 * 
 * @author Carlos Rueda
 */
class OntologyUploader {
	
	
	private static String SERVER    = "http://mmisw.org";
	private static String REST      = SERVER+ "/bioportal/rest";
	static final String ONTOLOGIES  = REST+ "/ontologies";

	
	private PartSource partSource;
	private String uri;
	private String userId;
	private String sessionId;
	private Map<String, String> values;
	
	/**
	 * Constructor.
	 * @param uri
	 * @param fileName
	 * @param RDF Contents of the ontology
	 * @param userId
	 * @param sessionId
	 * @param values   Used to fill in some of the fields in the aquaportal request
	 * @throws Exception
	 */
	OntologyUploader(String uri, String fileName, String RDF, String userId, String sessionId, Map<String, String> values)
	throws Exception {
		PartSource partSource = new ByteArrayPartSource(fileName, RDF.getBytes());
		
		this.partSource = partSource;
		this.uri = uri;
		this.userId = userId;
		this.sessionId = sessionId;
		this.values = values;
	}
	

	/**
	 * Executes the POST operation to upload the ontology.
	 * @return
	 * @throws Exception
	 */
	String create()	throws Exception {
//		File targetFile = new File("/Users/carueda/new_mapping.owl");
		
		PostMethod post = new PostMethod(ONTOLOGIES);
		try {
			// TODO keep in mind USE_EXPECT_CONTINUE
//			post.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);

			
			List<Part> partList = new ArrayList<Part>();
			
			partList.add(new FilePart("filePath", partSource));
			partList.add(new StringPart("sessionid", sessionId));
			partList.add(new StringPart("userId", userId));
			
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

			System.out.println(this.getClass().getName()+ ": Executing POST ...");

			String msg;
			int status = client.executeMethod(post);
			if (status == HttpStatus.SC_OK) {
				msg = post.getResponseBodyAsString();
				System.out.println(this.getClass().getName()+ ": "+
						"Upload complete, response=" + msg
				);
				msg = "OK:" +msg;
			} 
			else {
				String body = post.getResponseBodyAsString();
				msg = HttpStatus.getStatusText(status);
				System.out.println(this.getClass().getName()+ ": "+
						"Upload failed, response=" + msg
						+ "\n" +body
				);
				msg = "ERROR:" +msg+ "\n" +body ;
			}
			
			return msg;
		} 
		finally {
			post.releaseConnection();
		}

	}
}
