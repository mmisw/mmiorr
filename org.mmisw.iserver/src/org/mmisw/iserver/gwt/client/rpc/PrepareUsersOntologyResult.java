package org.mmisw.iserver.gwt.client.rpc;


/**
 * Info about the result of preparing the users ontology.
 * 
 * @author Carlos Rueda
 */
public class PrepareUsersOntologyResult extends BaseResult {
	private static final long serialVersionUID = 1L;

	private String uri = null;
	private String info = null;

	public PrepareUsersOntologyResult() {
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String toString() {
		return getClass().getSimpleName()+"{uri=" +uri+ ", info=" +info+", error=" +error+ "}";
	}

	/** Sets the URI of the users instantiation ontology */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/** Gets the URI of the users instantiation ontology */
	public String getUri() {
		return uri;
	}


}
