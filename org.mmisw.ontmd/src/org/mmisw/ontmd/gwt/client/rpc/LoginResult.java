package org.mmisw.ontmd.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LoginResult implements IsSerializable {
	
	private String error;
	private String sessionId;
	private String userId;

	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	

	public String toString() {
		return "LoginResult{userId=" +userId+", sessionId=" +sessionId+", error=" +error+ "}";
	}

}
