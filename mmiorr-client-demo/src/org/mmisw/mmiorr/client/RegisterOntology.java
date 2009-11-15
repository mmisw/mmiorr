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
	static final String FORM_ACTION = "http://mmisw.org/orr/direg";

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
		
		register(username, password, ontologyUri, fileName, graphId);
	}
	
	
	
	public static String register(String username, String password, 
			String ontologyUri, String fileName, String graphId
	) throws HttpException, IOException {
		
		String contents = IOUtils.toString(new FileReader(fileName));
		
		PartSource partSource = new ByteArrayPartSource(fileName, contents.getBytes());
		
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

			int status = client.executeMethod(post);
			String msg = post.getResponseBodyAsString();
			System.out.println("Response status: " +status+ ": " +HttpStatus.getStatusText(status));
			System.out.println("Response body:\n" +msg);
			return msg;
		} 
		finally {
			post.releaseConnection();
		}
	}

}
