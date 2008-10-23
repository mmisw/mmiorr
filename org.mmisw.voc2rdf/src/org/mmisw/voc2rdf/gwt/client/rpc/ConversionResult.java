package org.mmisw.voc2rdf.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about the result of a conversion.
 * @author Carlos Rueda
 */
public class ConversionResult implements IsSerializable {

	private String error = null;
	private String rdf = "";
	
	private String finalNamespace;
	
	public ConversionResult () {
	}
	
	
	public void setFinalNamespace(String finalNamespace) {
		this.finalNamespace = finalNamespace;
	}
	
	public String getFinalNamespace() {
		return finalNamespace;
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
