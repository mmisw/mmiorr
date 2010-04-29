package org.mmisw.iserver.gwt.client.rpc;


/**
 * Info about the result of removing an ontology from the registry/repository.
 * 
 * @author Carlos Rueda
 */
public class UnregisterOntologyResult extends BaseResult {
	private static final long serialVersionUID = 1L;

	private String uri = null;
	private String info = null;

	private String versionNumber;

	public UnregisterOntologyResult() {
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String toString() {
		return "UnregisterOntologyResult{info=" +info+", uri=" +uri+ ", versionNumber=" +versionNumber+ ", error=" +error+ "}";
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getVersionNumber() {
		return versionNumber;
	}


}
