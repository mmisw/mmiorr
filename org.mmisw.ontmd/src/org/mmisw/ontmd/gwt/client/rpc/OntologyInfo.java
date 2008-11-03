package org.mmisw.ontmd.gwt.client.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about an original ontology.
 * @author Carlos Rueda
 */
public class OntologyInfo implements IsSerializable {

	private String error = null;
	private String fullPath;
	private String uri;
	private String rdf;
	private String details;
	
	
	/** original values -- once assigned, shouldn't be changed */
	private Map<String,String> originalValues;

	
	/** New values assigned during editing; these are values
	 * used by the review process
	 */
	private Map<String,String> newValues;
	
	
	public OntologyInfo () {
	}
	
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

	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}

	public String getFullPath() {
		return fullPath;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}


	public String getRdf() {
		return rdf;
	}

	public void setRdf(String rdf) {
		this.rdf = rdf;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getDetails() {
		return details;
	}


}
