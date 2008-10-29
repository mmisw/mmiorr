package org.mmisw.ontmd.gwt.client.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about the result of a conversion.
 * @author Carlos Rueda
 */
public class OntologyInfo implements IsSerializable {

	private String error = null;
	private String rdf = "";
	private Map<String,String> values;
	
	public Map<String, String> getValues() {
		return values;
	}


	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	private String finalUri;
	
	public OntologyInfo () {
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
