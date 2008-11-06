package org.mmisw.ontmd.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about the result of an upload operation.
 * 
 * @author Carlos Rueda
 */
public class ReviewResult implements IsSerializable {

	private String error = null;
	private String uri = null;
	private String fullPath;
	private String rdf = null;
	
	// aquaportal ontology ID used, if not null, to create a new version
	private String ontologyId = null;
	
	private OntologyInfo ontologyInfo;

	public ReviewResult() {
	}

	
	public OntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}

	public void setOntologyInfo(OntologyInfo ontologyInfo) {
		this.ontologyInfo = ontologyInfo;
	}

	public String getError() {
		return error;
	}

	public String getRdf() {
		return rdf;
	}

	public void setRdf(String rdf) {
		this.rdf = rdf;
	}

	public void setError(String error) {
		this.error = error;		
	}

	public String toString() {
		return "ReviewResult{uri=" +uri+ ", rdf=" +rdf+" , error=" +error+ "}";
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

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}


	public String getOntologyId() {
		return ontologyId;
	}


	public void setOntologyId(String ontologyId) {
		this.ontologyId = ontologyId;
	}

}
