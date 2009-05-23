package org.mmisw.ontmd.gwt.client.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about an original ontology.
 * 
 * It also allows to store the aquaportal ID of a pre-existing ontology ({@link #getOntologyId()})
 * in case the user wants to perform the submission of a new version. This ID will be null when
 * the user is performing a completely new submission.
 * 
 * <p>
 * Also, the full path to a corresponding CSV file can be stored.
 * 
 * @author Carlos Rueda
 */
//
// NOTE: This used to be called OntologyInfo, but was renamed to OntologyInfoPre to avoid conflicts
// with different class OntologyInfo in iserver module
//
public class OntologyInfoPre implements IsSerializable {

	private String error = null;
	private String fullPath;
	private String uri;
	private String rdf;
	private String details;
	
	private String fullPathCsv;
	
	/** aquaportal ontology ID used, if not null, to create a new version */
	private String ontologyId = null;
	private String ontologyUserId = null;

	
	/** original values -- once assigned, shouldn't be changed */
	private Map<String,String> originalValues;

	
	/** New values assigned during editing; these are values
	 * used by the review process
	 */
	private Map<String,String> newValues;
	
	
	public OntologyInfoPre () {
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

	/** @returns The full path of the ontology file on the server */
	public String getFullPath() {
		return fullPath;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	/** sets the full path of the ontology file on the server */
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

	
	/** @returns the aquaportal ontology ID used, if not null, to create a new version */
	public String getOntologyId() {
		return ontologyId;
	}

	/** @returns the aquaportal userId of the ontology ID used to create a new version */
	public String getOntologyUserId() {
		return ontologyUserId;
	}


	/** sets the aquaportal ontology ID used, if not null, to create a new version */
	public void setOntologyId(String ontologyId, String ontologyUserId) {
		this.ontologyId = ontologyId;
		this.ontologyUserId = ontologyUserId;
	}

	/**
	 * @return the fullPathCsv
	 */
	public String getFullPathCsv() {
		return fullPathCsv;
	}

	/**
	 * @param fullPathCsv the fullPathCsv to set
	 */
	public void setFullPathCsv(String fullPathCsv) {
		this.fullPathCsv = fullPathCsv;
	}
	
	

}
