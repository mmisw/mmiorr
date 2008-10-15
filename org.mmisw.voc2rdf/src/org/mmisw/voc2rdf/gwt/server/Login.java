package org.mmisw.voc2rdf.gwt.server;


/** 
 * A helper to authenticate a user.
 * 
 * <p>
 * TODO do proper implementation. Currently it obtains a hard-coded sessionId.
 * 
 * @author Carlos Rueda
 */
class Login {
	
	private String userName;
	private String userPassword;
	private String sessionId;
	
	/**
	 * Constructor.
	 * @param userName
	 * @param userPassword
	 */
	Login(String userName, String userPassword) {
		this.userName = userName;
		this.userPassword = userPassword;
		
		// TODO
		this.sessionId = "9c188a9b8de0fe0c21b9322b72255fb939a68bb2";
	}
	
	String getSessionId() {
		return sessionId;
	}

}
