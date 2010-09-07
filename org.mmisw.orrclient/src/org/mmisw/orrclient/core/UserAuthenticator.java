package org.mmisw.orrclient.core;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.orrclient.core.util.OntServiceUtil;
import org.mmisw.orrclient.core.util.XmlAccessor;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;


/** 
 * A helper to authenticate a user.
 * 
 * @author Carlos Rueda
 */
class UserAuthenticator {
	/** the authentication piece for the REST call */
	private static final String AUTH     = "/auth";

	private final Log log = LogFactory.getLog(UserAuthenticator.class);
	
	private String userName;
	private String userPassword;
	
	
	/**
	 * Constructor.
	 * @param userName
	 * @param userPassword
	 */
	UserAuthenticator(String userName, String userPassword) {
		this.userName = userName;
		this.userPassword = userPassword;
	}
	
	/** makes the request and return the response from the server 
	 * @throws Exception */
	private String authenticate() throws Exception {
		String applicationid = "4ea81d74-8960-4525-810b-fa1baab576ff";
		log.info("authenticating username=" +userName+ " password=" +userPassword.replaceAll(".", "*"));
		log.info("applicationid=" +applicationid);
		
		String bioPortalRestUrl = OntServiceUtil.getAquaportalRestUrl();
		String authRestUrl = bioPortalRestUrl + AUTH;
		log.info("authentication REST URL =" +authRestUrl);
		
		PostMethod post = new PostMethod(authRestUrl);
		try {
			NameValuePair[] data = {
					new NameValuePair("username", userName),
					new NameValuePair("password", userPassword),
					new NameValuePair("applicationid", applicationid ),
			};
			post.setRequestBody(data);

			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			log.info("Executing POST ...");

			int status = client.executeMethod(post);

			String msg = post.getResponseBodyAsString();
			
			if (status == HttpStatus.SC_OK) {
				log.info("Authentication complete, response=[" + msg + "]");
			} 
			else {
				String statusText = HttpStatus.getStatusText(status);
				log.info("Authentication failed, status text=" + statusText);
				log.info("Authentication failed, response=" + msg);
				if ( msg == null ) {
					msg = statusText;
				}
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
		log.info("----response=" +response);
		
		
		XmlAccessor xa = new XmlAccessor(response);
		
		if ( xa.containsTag("error") ) {
			loginResult.setError("Invalid credentials");
			return;
		}
		
		// Assign appropriate values to loginResult object
		String sessionId = xa.getString("success/sessionId");
		String id = xa.getString("success/data/user/id");
		String username = xa.getString("success/data/user/username");
		String role = xa.getString("success/data/user/roles/string");
		
		
		if ( sessionId == null || sessionId.trim().length() == 0
		||   id == null || id.trim().length() == 0
		||   username == null || username.trim().length() == 0
		||   role == null || role.trim().length() == 0
		) {
			String error;
			if ( ! xa.containsTag("success") ) {
				// unexpected response.
				error = "Unexpected: server did not respond with a success nor an error message. Please try again later.";
			}
			else {
				error = "Could not parse response from registry server. Please try again later. response=" +response;
			}
			log.error(error);
			loginResult.setError(error);
			return;
		}
		loginResult.setSessionId(sessionId);
		loginResult.setUserId(id);
		loginResult.setUserName(username);
		loginResult.setUserRole(role);
	}

}
