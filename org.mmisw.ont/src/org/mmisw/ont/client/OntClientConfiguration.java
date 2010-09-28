package org.mmisw.ont.client;

/**
 * Configuration properties for the Ont Client library.
 * 
 * @author Carlos Rueda
 */
public class OntClientConfiguration {
		
	private String ontServiceUrl = "http://localhost:8080/ont";

	/**
	 * Creates a default configuration.
	 */
	public OntClientConfiguration() {
		
	}
	
	/**
	 * Creates a copy.
	 */
	public OntClientConfiguration(OntClientConfiguration config) {
		this.ontServiceUrl = config.getOntServiceUrl();
	}
	
	public String getOntServiceUrl() {
		return ontServiceUrl;
	}
	
	/**
	 * Sets the URL of the Ont service.
	 * For example: "http://localhost:8080/ont";
	 * 
	 * @param ontServiceUrl    URL of the Ont service.
	 */
	public void setOntServiceUrl(String ontServiceUrl) {
		this.ontServiceUrl = ontServiceUrl;
	}
	
}
