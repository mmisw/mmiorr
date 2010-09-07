package org.mmisw.ont.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.OntConfig;


/** 
 * A helper class to delete ontologies from the repository.
 * 
 * @author Carlos Rueda
 */
public class OntologyDeleter {
	
	private static final String ONTOLOGIES  = "/ontologies/";

	private final Log log = LogFactory.getLog(OntologyDeleter.class);
	
	private String sessionId;
	private String id;
	

	
	/**
	 * Constructor.
	 * @param sessionId  eg., "9c188a9b8de0fe0c21b9322b72255fb939a68bb2"
	 * @param id  Aquaportal ontology version ID
	 */
	public OntologyDeleter(String sessionId, String id) {
		this.sessionId = sessionId;
		this.id = id;
	}
	

	/**
	 * Executes the POST operation to delete the ontology.
	 * 
	 * @return The message in the response from the POST operation, prefixed with "OK:" if
	 *         the result was successfull; otherwise, the description of the error 
	 *         prefixed with "ERROR:"
	 *         
	 * @throws Exception
	 */
	public String execute() throws Exception  {
		
		String action = OntConfig.Prop.AQUAPORTAL_REST_URL.getValue() + ONTOLOGIES + id;
		
		PostMethod post = new PostMethod(action);
		try {
			List<Part> partList = new ArrayList<Part>();
			
			partList.add(new StringPart("sessionid", sessionId));
			partList.add(new StringPart("id", id));

			partList.add(new StringPart("method", "DELETE"));
			return _performPost(post, partList);
		} 
		finally {
			post.releaseConnection();
		}
	}


	private String _performPost(PostMethod post, List<Part> partList) throws Exception  {
		
		Part[] parts = partList.toArray(new Part[partList.size()]);
		post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().
		getParams().setConnectionTimeout(5000);

		log.info("OntologyDeleter: Executing POST " +post.getURI());

		String msg;
		int status = client.executeMethod(post);
		if (status == HttpStatus.SC_OK) {
			msg = post.getResponseBodyAsString();
//			log.info("Deletion complete, response=" + msg);
			msg = "OK:" +msg;
		} 
		else {
			String body = post.getResponseBodyAsString();
			msg = HttpStatus.getStatusText(status);
			log.info("Deletion failed, response=" +msg+ "\n" +body);
			msg = "ERROR:" +msg+ "\n" +body ;
		}
		
		return msg;	
	}
}
