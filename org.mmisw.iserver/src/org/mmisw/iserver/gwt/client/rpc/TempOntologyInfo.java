package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;

import org.mmisw.iserver.gwt.client.rpc.OntologyMetadata;

/**
 * Info about a "temporary" ontology. This means, an ontology that has been loaded
 * into the working space (supported by a saved file in the back-end), which will
 * be eventually submitted for registration.
 * 
 * @author Carlos Rueda
 */
public class TempOntologyInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String error = null;
	private String fullPath;
	private String uri;
	private String rdf;
	private String details;
	
	private OntologyMetadata ontologyMetadata;
	
	public TempOntologyInfo() {
	}
	
	
	public OntologyMetadata getOntologyMetadata() {
		if ( ontologyMetadata == null ) {
			ontologyMetadata = new OntologyMetadata();
		}
		return ontologyMetadata;
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


}
