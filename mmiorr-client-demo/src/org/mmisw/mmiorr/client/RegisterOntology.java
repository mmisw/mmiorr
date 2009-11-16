package org.mmisw.mmiorr.client;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;

/**
 * A program demonstrating the direct registration of an ontology file
 * in the MMI Ontology Registry and Repository.
 * 
 * @author Carlos Rueda
 */
public class RegisterOntology {
	private static final String FORM_ACTION = "http://mmisw.org/orr/direg";

	
	public static class RegistrationResult {
		public int status;
		public String message;
	}
	
	
	/**
	 * See build.xml
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		int arg = 0;
		String username = args[arg++];
		String password = args[arg++];
		String ontologyUri = args[arg++];
		String fileName = args[arg++];
		String graphId =  args[arg++];
		
		String fileContents = IOUtils.toString(new FileReader(fileName));

		RegistrationResult result = register(username, password, ontologyUri, fileName, fileContents, graphId);
		System.out.println("Response status: " +result.status+ ": " +HttpStatus.getStatusText(result.status));
		System.out.println("Response body:\n" +result.message);
	}
	
	
	
	public static RegistrationResult register(String username, String password, 
			String ontologyUri, String fileName, String fileContents, String graphId
	) throws HttpException, IOException {
		
		PartSource partSource = new ByteArrayPartSource(fileName, fileContents.getBytes());
		
		System.out.println("Executing POST request to " +FORM_ACTION);
		PostMethod post = new PostMethod(FORM_ACTION);
		post.addRequestHeader("accept", "text/plain");
		try {
			Part[] parts = new Part[] {
					new StringPart("username", username),
					new StringPart("password", password),
					new StringPart("ontologyUri", ontologyUri),
					new FilePart("ontologyFile", partSource),
					new StringPart("graphId", graphId),
			};
			post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			RegistrationResult result = new RegistrationResult();
			result.status = client.executeMethod(post);
			result.message = post.getResponseBodyAsString();
			return result;
		} 
		finally {
			post.releaseConnection();
		}
	}

}
