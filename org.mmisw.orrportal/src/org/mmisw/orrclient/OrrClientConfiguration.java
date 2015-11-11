package org.mmisw.orrclient;

/**
 * Configuration properties for the OrrClient library.
 * 
 * @author Carlos Rueda
 */
public class OrrClientConfiguration {
		
	private String ontServiceUrl = "http://localhost:8080/ont";

	/**
	 * Creates a default configuration.
	 */
	public OrrClientConfiguration() {
		
	}
	
	/**
	 * Creates a copy.
	 */
	public OrrClientConfiguration(OrrClientConfiguration config) {
		this.ontServiceUrl = config.getOntServiceUrl();
	}
	
	public String getOntServiceUrl() {
		return ontServiceUrl;
	}
	
	public void setOntServiceUrl(String ontServiceUrl) {
		this.ontServiceUrl = ontServiceUrl;
	}
	
}
