package org.mmisw.orrclient.gwt.client.rpc;

import java.io.Serializable;

/**
 * A base class for all objects or results that can have an error.
 * 
 * @author Carlos Rueda
 */
public abstract class Errorable implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	protected String error;

	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
}
