package org.mmisw.voc2rdf.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about the result of a conversion.
 * @author Carlos Rueda
 */
public class ConversionResult implements IsSerializable {

	private String error = null;
	private String rdf = "";
	
	private String finalUri;
	
	public ConversionResult () {
	}
	
	
	public void setFinalUri(String finalNamespace) {
		this.finalUri = finalNamespace;
	}
	
	public String getFinalUri() {
		return finalUri;
	}

	public String getRdf() {
		return rdf;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setRdf(String rdf) {
		this.rdf = rdf;
	}

}
