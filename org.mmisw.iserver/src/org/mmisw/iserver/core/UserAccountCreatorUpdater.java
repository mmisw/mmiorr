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
import org.mmisw.iserver.core.util.XmlAccessor;
import org.mmisw.iserver.gwt.client.rpc.CreateUpdateUserAccountResult;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;


/** 
 * A helper to create/update a user account.
 * 
 * @author Carlos Rueda
 */
class UserAccountCreatorUpdater {
	/** for the REST call */
	private static final String USERS     = "/users";

	private final Log log = LogFactory.getLog(UserAccountCreatorUpdater.class);
	
	private String userId;
	private Map<String,String> values;
	
	
	/**
	 * Constructor.
	 * @param create true to create new account; false to update existing account.
	 * @param values
	 */
	UserAccountCreatorUpdater(Map<String,String> values) {
		this.values = values;
		this.userId = values.get("id");
	}
	
	
	/** makes the request and return the response from the server */
	private String doPost() throws HttpException, IOException {
		String applicationid = "4ea81d74-8960-4525-810b-fa1baab576ff";
		log.info("applicationid=" +applicationid);
		
		String restUrl = ServerConfig.Prop.BIOPORTAL_REST_URL.getValue() + USERS;
		
		values.put("applicationid", applicationid);

		
		if ( userId != null ) {
			values.put("method", "PUT");
			restUrl += "/" + userId;
		}
		restUrl += "?&applicationid=" +applicationid;
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for ( String key : values.keySet() ) {
			String value = values.get(key);
			if ( value != null ) {
				pairs.add(new NameValuePair(key, value));
			}
		}
		NameValuePair[] data = pairs.toArray(new NameValuePair[pairs.size()]);

		PostMethod method = new PostMethod(restUrl);
		method.setRequestBody(data);

		log.info("preparing to call REST URL = " +restUrl);
		

		try {
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			log.info("Executing " +method.getName()+ " ...");

			String msg;
			int status = client.executeMethod(method);
			if (status == HttpStatus.SC_OK) {
				msg = method.getResponseBodyAsString();
				log.info("creation/update complete, response=[" + msg + "]");
			} 
			else {
				String statusText = HttpStatus.getStatusText(status);
				log.info("creation/update failed, status text=" + statusText);
				
				msg = statusText;
				String response = method.getResponseBodyAsString();
				if ( response != null ) {
					msg += "\n" + response;
				}
			}
			
			return msg;
		} 
		finally {
			method.releaseConnection();
		}
	}
	
	/**
	 * @return
	 * @throws Exception 
	 */
	void doIt(CreateUpdateUserAccountResult result) throws Exception  {
		String response = doPost();

		response = response.replaceAll("\\s+", " ");
		log.info("----response=" +response);
		
	
		XmlAccessor xa = new XmlAccessor(response);
		
		if ( xa.containsTag("error") ) {
			result.setError("Could not create/update account");
			return;
		}
		
		// Assign appropriate values to loginResult object
		String sessionId = xa.getString("success/sessionId");
		String id = xa.getString("success/data/user/id");
		String username = xa.getString("success/data/user/username");
		String role = xa.getString("success/data/user/roles/string");
//		String email = xa.getString("success/data/user/email");
//		String firstname = xa.getString("success/data/user/firstname");
//		String lastname = xa.getString("success/data/user/lastname");
//		String dateCreate = xa.getString("success/data/user/accessDate");

		// During account update, the roles are not reported (<roles/>); so, do no
		// check is role is emty.
		if ( sessionId == null || sessionId.trim().length() == 0
		||   id == null || id.trim().length() == 0
		||   username == null || username.trim().length() == 0
//		||   role == null || role.trim().length() == 0
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
			log.error("sessionId=" +sessionId+ ", id=" +id+ ", username=" +username);
			result.setError(error);
			return;
		}
		LoginResult loginResult = new LoginResult();
		loginResult.setSessionId(sessionId);
		loginResult.setUserId(id);
		loginResult.setUserName(username);
		loginResult.setUserRole(role);

		result.setLoginResult(loginResult);
		
	}
}
