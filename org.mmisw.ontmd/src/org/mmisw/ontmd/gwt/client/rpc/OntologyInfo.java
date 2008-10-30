package org.mmisw.ontmd.gwt.client.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about the result of a conversion.
 * @author Carlos Rueda
 */
public class OntologyInfo implements IsSerializable {

	private String error = null;
	private String fullPath;
	private Map<String,String> values;

	private String uri;
	private String rdf;
	
	public Map<String, String> getValues() {
		return values;
	}


	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	
	public OntologyInfo () {
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

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}


	public String getRdf() {
		return rdf;
	}

	public void setRdf(String rdf) {
		this.rdf = rdf;
	}


}
