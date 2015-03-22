package org.mmisw.orrclient.gwt.client.rpc;


/**
 * Info about the result of a reset password operation.
 * 
 * @author Carlos Rueda
 */
public class ResetPasswordResult extends BaseResult {
	private static final long serialVersionUID = 1L;

	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}
