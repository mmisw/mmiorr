package org.mmisw.voc2rdf.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about the result of an upload operation.
 * 
 * @author Carlos Rueda
 */
public class UploadResult implements IsSerializable {

	private String error = null;
	private String info = null;

	public UploadResult() {
	}

	public String getError() {
		return error;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public void setError(String error) {
		this.error = error;		
	}

	public String toString() {
		return "UploadResult{info=" +info+" , error=" +error+ "}";
	}


}
