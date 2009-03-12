package org.mmisw.ontmd.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about the result of an upload operation.
 * 
 * @author Carlos Rueda
 */
public class UploadResult extends BaseResult implements IsSerializable {

	private String uri = null;
	private String info = null;

	public UploadResult() {
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
