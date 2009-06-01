package org.mmisw.iserver.gwt.client.rpc;


/**
 * Info about the result of an upload operation.
 * 
 * @author Carlos Rueda
 */
public class UploadOntologyResult extends BaseResult {
	private static final long serialVersionUID = 1L;

	private String uri = null;
	private String info = null;

	public UploadOntologyResult() {
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String toString() {
		return "UploadResult{info=" +info+" , error=" +error+ "}";
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}


}
