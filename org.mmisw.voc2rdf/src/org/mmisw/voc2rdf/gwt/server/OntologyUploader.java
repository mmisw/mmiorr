package org.mmisw.voc2rdf.gwt.server;

import java.io.File;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;

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
	
	/**
	 * Constructor.
	 * @param uri
	 * @param fileName
	 * @param RDF Contents of the ontology
	 * @param userId
	 * @param sessionId
	 * @throws Exception
	 */
	OntologyUploader(String uri, String fileName, String RDF, String userId, String sessionId)
	throws Exception {
		PartSource partSource = new ByteArrayPartSource(fileName, RDF.getBytes());
		
		this.partSource = partSource;
		this.uri = uri;
		this.userId = userId;
		this.sessionId = sessionId;
	}
	
	/**
	 * Constructor.
	 * @param uri
	 * @param targetFile File containing the ontology.
	 * @param userId
	 * @param sessionId
	 * @throws Exception
	 */
	OntologyUploader(String uri, File targetFile, String userId, String sessionId)
	throws Exception {
		PartSource partSource = new FilePartSource(targetFile.getName(), targetFile);
		
		this.partSource = partSource;
		this.uri = uri;
		this.userId = userId;
		this.sessionId = sessionId;
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

			Part[] parts = {
					new FilePart("filePath", partSource),
					new StringPart("urn", uri.toString()),
					new StringPart("sessionid", sessionId),
					new StringPart("userId", userId),

					// TODO: the following are hard-coded/made up for now
					new StringPart("statusId", "1"),
					new StringPart("isReviewed", "1"),
					new StringPart("codingScheme", "1"),
					new StringPart("isManual", "1"),
					new StringPart("versionNumber", "0.1"),
					new StringPart("versionStatus", "testing"),
					new StringPart("isRemote", "0"),
					new StringPart("dateReleased", "10/5/2008"),
					new StringPart("contactEmail", "temporary@example.com"),
					new StringPart("contactName", "Onto Logy"),
					new StringPart("displayLabel", uri.toString()),
					new StringPart("format", "OWL-DL"),
					new StringPart("isFoundry", "0"),
					new StringPart("categoryId", "2809"),
					new StringPart("categoryId", "2810"),
					
			};
			post.setRequestEntity(
					new MultipartRequestEntity(parts, post.getParams())
			);

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
				msg = HttpStatus.getStatusText(status);
				System.out.println(this.getClass().getName()+ ": "+
						"Upload failed, response=" + msg
						+ "\n" +
						post.getResponseBodyAsString()
				);
				msg = "ERROR:" +msg;
			}
			
			return msg;
		} 
		finally {
			post.releaseConnection();
		}

	}
}
