package org.mmisw.iserver.gwt.client.rpc;


/**
 * Info about the result of a review operation.
 * 
 * @author Carlos Rueda
 */
public class ReviewResult extends BaseResult {
	private static final long serialVersionUID = 1L;
	
	
	private String uri = null;
	private String fullPath;
	
	private OntologyInfo ontologyInfo;


	public ReviewResult() {
	}

	
	public OntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}

	public void setOntologyInfo(OntologyInfo ontologyInfo) {
		this.ontologyInfo = ontologyInfo;
	}

	public String toString() {
		return "ReviewResult{uri=" +uri+ ", error=" +error+ "}";
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

}
