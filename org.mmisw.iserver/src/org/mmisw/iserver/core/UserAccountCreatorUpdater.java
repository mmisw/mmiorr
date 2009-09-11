package org.mmisw.iserver.core;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.gwt.client.rpc.CreateUpdateUserAccountResult;


/** 
 * A helper to create/update a user account.
 * 
 * @author Carlos Rueda
 */
class UserAccountCreatorUpdater {
	/** for the REST call */
	private static final String REST     = "/users";

	// TODO revise this pattern
//	private static final Pattern RESPONSE_PATTERN = Pattern.compile(
//			".*<sessionId>([^<]+)</sessionId>" +
//			".*<id>([^<]+)</id>" +
//			".*<username>([^<]+)</username>" +
//			".*<email>([^<]+)</email>" +
//			".*<firstname>([^<]+)</firstname>" +
//			".*<lastname>([^<]+)</lastname>" +
//			".*<dateCreated>([^<]+)</dateCreated>" +
//			".*<roles>.*(ROLE_[^<]*)" +
//			".*"
//	);
	

	private final Log log = LogFactory.getLog(UserAccountCreatorUpdater.class);
	
	private Map<String,String> values;
	
	
	/**
	 * Constructor.
	 * @param userName
	 * @param userPassword
	 */
	UserAccountCreatorUpdater(Map<String,String> values) {
		this.values = values;
	}
	
	
	/** makes the request and return the response from the server */
	private String doPost() throws HttpException, IOException {
		String applicationid = "4ea81d74-8960-4525-810b-fa1baab576ff";
		log.info("applicationid=" +applicationid);
		
		String restUrl = ServerConfig.Prop.BIOPORTAL_REST_URL.getValue() + REST;
		log.info("preparing to call REST URL =" +restUrl);
		
		values.put("applicationid", applicationid);
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		
		for ( String key : values.keySet() ) {
			pairs.add(new NameValuePair(key, values.get(key)));
		}
		
		NameValuePair[] data = pairs.toArray(new NameValuePair[pairs.size()]);
		
		PostMethod post = new PostMethod(restUrl);
		try {
			post.setRequestBody(data);

			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			log.info("Executing POST ...");

			String msg;
			int status = client.executeMethod(post);
			if (status == HttpStatus.SC_OK) {
				msg = post.getResponseBodyAsString();
				log.info("Authentication complete, response=[" + msg + "]");
				msg = "OK:" +msg;
			} 
			else {
				String statusText = HttpStatus.getStatusText(status);
				log.info("Authentication failed, status text=" + statusText);
				
				msg = statusText;
				String response = post.getResponseBodyAsString();
				if ( response != null ) {
					msg += "\n" + response;
				}
				msg = "ERROR:" +msg;
			}
			
			return msg;
		} 
		finally {
			post.releaseConnection();
		}
	}
	
	/**
	 * Does the authentication returning a Session object.
	 * @return
	 * @throws Exception 
	 */
	void doIt(CreateUpdateUserAccountResult result) throws Exception  {
		String response = doPost();

		response = response.replaceAll("\\s+", " ");
		log.info("----response=" +response);
		
		// parse response:
		//
		// FIXME: this is a *very* simply examination of the response
		//
		if ( response.matches(".*<error>.*") ) {
			result.setError("Could not create/update account");
			return;
		}
		
		if ( !response.matches(".*<success>.*") ) {
			// unexpected response.
			result.setError("Error while creating/updating account. Please try again later.");
			return;
		}
		
		// TODO
//		Matcher matcher = RESPONSE_PATTERN.matcher(response);
//		
//		if ( matcher.find() ) {
//			// Assign approrpriate values to result object
//			String sessionId = matcher.group(1);
//			String id = matcher.group(2);
//			String username = matcher.group(3);
//			String email = matcher.group(4);
//			String firstname = matcher.group(5);
//			String lastname = matcher.group(6);
//			String dateCreate = matcher.group(7);
//			String role = matcher.group(8);
//		}
//		else {
//			result.setError("Could not parse response from registry server. Please try again later. response=" +response);
//		}
	}

}
