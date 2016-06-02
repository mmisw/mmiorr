package org.mmisw.ont.client.repoclient.bioportal;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.client.SignInResult;
import org.mmisw.ont.client.util.XmlAccessor;


/** 
 * A helper to authenticate a user.
 */
class UserAuthenticator {
	/** the authentication piece for the REST call */
	private static final String AUTH     = "/auth";

	private final Log log = LogFactory.getLog(UserAuthenticator.class);

    private final String aquaportalRestUrl;


    UserAuthenticator(String aquaportalRestUrl) {
		this.aquaportalRestUrl = aquaportalRestUrl;
	}
	
	/** makes the request and returns the response from the server 
	 * @throws Exception
	 */
	private String authenticate(String userName, String userPassword) throws Exception {
		String applicationid = "4ea81d74-8960-4525-810b-fa1baab576ff";
		log.info("authenticating username=" +userName+ " password=" +userPassword.replaceAll(".", "*"));
		log.info("applicationid=" +applicationid);
		
		String authRestUrl = aquaportalRestUrl + AUTH;
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
    public SignInResult getSession(String userName, String userPassword) throws Exception {
		String response = authenticate(userName, userPassword);

		response = response.replaceAll("\\s+", " ");
		log.info("----response=" +response);
		
		
		XmlAccessor xa = new XmlAccessor(response);
		
		if ( xa.containsTag("error") ) {
			throw new Exception("Invalid credentials");
		}
		
		// Assign appropriate values to loginResult object
		String sessionId = xa.getString("success/sessionId");
		String id = xa.getString("success/data/user/id");
		String username = xa.getString("success/data/user/username");
		String role = xa.getString("success/data/user/roles/string");

		// fix #406 "cannot parse response when signing in".
		// For some reason, role is now reported under success/data/user/roles/java.lang.String
		// and not under success/data/user/roles/string.  Fix: consider both cases as needed.
		if(log.isInfoEnabled()) {
			log.info("getSession:\n" +
					"  sessionId=" +sessionId+ "\n" +
					"  id="        +id+ "\n" +
					"  username="  +username+ "\n" +
					"  role="      +role
			);
		}
		if (role == null || role.trim().length() == 0) {
			role = xa.getString("success/data/user/roles/java.lang.String");
			if(log.isInfoEnabled()) {
				log.info("getSession: from 2nd option: role="+role);
			}
		}

		if ( sessionId == null || sessionId.trim().length() == 0
		||   id == null || id.trim().length() == 0
		||   username == null || username.trim().length() == 0
		||   role == null || role.trim().length() == 0
		) {
			if ( ! xa.containsTag("success") ) {
				// unexpected response.
				throw new Exception("Unexpected: server did not respond with a success nor an error message. Please try again later.");
			}
			else {
				throw new Exception("Could not parse response from registry server. Please try again later. response=" +response);
			}
		}
		
		SignInResult signInResult = new SignInResult();
		signInResult.setSessionId(sessionId);
		signInResult.setUserId(id);
		signInResult.setUserName(username);
		signInResult.setUserRole(role);
		
		return signInResult;
	}

}
