package org.mmisw.orrportal.gwt.client.voc2rdf.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about the result of a conversion.
 * @author Carlos Rueda
 */
@Deprecated
public class ConversionResult implements IsSerializable {

	private String error = null;
	
//	RDF string no longer sent to client (there is now a "download" action)
//	private String rdf = "";
//	public String getRdf() {
//		return rdf;
//	}
//	public void setRdf(String rdf) {
//		this.rdf = rdf;
//	}


	private String finalShortName;
	private String finalUri;
	private String pathOnServer;
	
	public ConversionResult () {
	}
	
	
	public void setFinalShortName(String finalShortName) {
		this.finalShortName = finalShortName;
	}
	
	public void setFinalUri(String finalNamespace) {
		this.finalUri = finalNamespace;
	}
	
	public String getFinalUri() {
		return finalUri;
	}

	public String getFinalShortName() {
		return finalShortName;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}


	public void setPathOnServer(String pathOnServer) {
		this.pathOnServer = pathOnServer;
	}


	public String getPathOnServer() {
		return pathOnServer;
	}

}
