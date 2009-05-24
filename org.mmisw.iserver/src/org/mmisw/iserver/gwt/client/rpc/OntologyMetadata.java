package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;
import java.util.Map;

/**
 * Metadata about a registered ontology.
 * 
 * @author Carlos Rueda
 */
public class OntologyMetadata implements Serializable {
	private static final long serialVersionUID = 1L;

	private String error = null;
	
	private String details;
	
	
	public void setDetails(String details) {
		this.details = details;
	}

	public String getDetails() {
		return details;
	}


	
	/** original values -- once assigned, shouldn't be changed */
	private Map<String,String> originalValues;

	
	/** New values assigned during editing; these are values
	 * used by the review process
	 */
	private Map<String,String> newValues;
	

	public Map<String, String> getOriginalValues() {
		return originalValues;
	}


	public void setOriginalValues(Map<String, String> values) {
		this.originalValues = values;
	}

	
	public Map<String, String> getNewValues() {
		return newValues;
	}


	public void setNewValues(Map<String, String> values) {
		this.newValues = values;
	}


	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}


	/**
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}


}
