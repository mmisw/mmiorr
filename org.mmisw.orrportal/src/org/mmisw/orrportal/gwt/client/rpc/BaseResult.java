package org.mmisw.orrportal.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Common information for all results.
 * 
 * @author Carlos Rueda
 */
public abstract class BaseResult implements IsSerializable {
	
	protected String error;

	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
}
