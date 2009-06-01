package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;

/**
 * Common information for all results.
 * 
 * @author Carlos Rueda
 */
public abstract class BaseResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	protected String error;

	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
}
