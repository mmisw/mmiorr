package org.mmisw.iserver.gwt.client.rpc;


/**
 * Info about the result of a createUpdateUserAccount operation.
 * 
 * @author Carlos Rueda
 */
public class CreateUpdateUserAccountResult extends BaseResult {
	private static final long serialVersionUID = 1L;

	private LoginResult loginResult;

	/**
	 * Returns the login result corresponding to a successful creation or
	 * update of a user account.
	 * @return
	 */
	public LoginResult getLoginResult() {
		return loginResult;
	}

	public void setLoginResult(LoginResult loginResult) {
		this.loginResult = loginResult;
	}
	
	
}
