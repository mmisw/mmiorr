package org.mmisw.ontmd.gwt.server;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;


/** 
 * A helper to authenticate a user.
 * 
 * @author Carlos Rueda
 */
class Login {
	
	private static final String SERVER   = "http://mmisw.org";
	private static final String REST     = SERVER+ "/bioportal/rest";
	private static final String AUTH     = REST+ "/auth";


	private String userName;
	private String userPassword;
	
	
	/**
	 * Constructor.
	 * @param userName
	 * @param userPassword
	 */
	Login(String userName, String userPassword) {
		this.userName = userName;
		this.userPassword = userPassword;
	}
	
	
	/** makes the request and return the response from the server */
	private String authenticate() throws HttpException, IOException {
		String applicationid = "4ea81d74-8960-4525-810b-fa1baab576ff";
		System.out.println("authenticating username=" +userName+ " password=" +userPassword);
		System.out.println("applicationid=" +applicationid);
		PostMethod post = new PostMethod(AUTH);
		try {
			NameValuePair[] data = {
					new NameValuePair("username", userName),
					new NameValuePair("password", userPassword),
					new NameValuePair("applicationid", applicationid ),
			};
			post.setRequestBody(data);

			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			System.out.println(this.getClass().getName()+ ": Executing POST ...");

			String msg;
			int status = client.executeMethod(post);
			if (status == HttpStatus.SC_OK) {
				msg = post.getResponseBodyAsString();
				System.out.println(this.getClass().getName()+ ": "+
						"Authentication complete, response=[" + msg + "]"
				);
				msg = "OK:" +msg;
			} 
			else {
				String statusText = HttpStatus.getStatusText(status);
				System.out.println(this.getClass().getName()+ ": "+
						"Authentication failed, status text=" + statusText);
				
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
	void getSession(LoginResult loginResult) throws Exception  {
		String response = authenticate();

		response = response.replaceAll("\\s+", " ");
		System.out.println("----response=" +response);
		
		// parse response:
		//
		// FIXME: this is a *very* simply examination of the response
		//
		if ( response.matches(".*<error>.*") ) {
			loginResult.setError("Invalid credentials");
			return;
		}
		
		if ( !response.matches(".*<success>.*") ) {
			// unexpected response.
			loginResult.setError("Error while validating credentials. Please try again later.");
			return;
		}
		
		// get user id and sessionId
		
		Pattern pat = Pattern.compile(
				".*<sessionId>([^<]+)</sessionId>" +
				".*<id>([^>]+)</id>" +
				".*"
		);
		
		Matcher matcher = pat.matcher(response);
		if ( matcher.find() ) {
			loginResult.setSessionId(matcher.group(1));
			loginResult.setUserId(matcher.group(2));
		}
		else {
			loginResult.setError("Could not parse response from registry server Please try again later.");
		}
	}

}
