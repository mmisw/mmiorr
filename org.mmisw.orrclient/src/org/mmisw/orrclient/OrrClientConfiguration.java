package org.mmisw.orrclient;

/**
 * Configuration properties for the OrrClient library.
 * 
 * @author Carlos Rueda
 */
public class OrrClientConfiguration {
		
	private String ontServiceUrl = "http://localhost:8080/ont";
	private String previewDirectory =  "/Users/Shared/mmiregistry/ontmd/previews/";
	private String voc2rdfDirectory = "/Users/Shared/mmiregistry/ontmd/preuploads/voc2rdf/";

	// TODO this should be obtained from the Ont service
	private String bioportalRestUrl = "http://localhost:8080/bioportal/rest";
		
	private String mailUser = null;
	private String mailPassword = null;
	
	/**
	 * Creates a default configuration.
	 */
	public OrrClientConfiguration() {
		
	}
	
	/**
	 * Creates a copy.
	 */
	public OrrClientConfiguration(OrrClientConfiguration config) {
		this.previewDirectory = config.getPreviewDirectory();
		this.voc2rdfDirectory = config.getVoc2rdfDirectory();
		this.bioportalRestUrl = config.getBioportalRestUrl();
		this.ontServiceUrl = config.getOntServiceUrl();
		this.mailUser = config.getMailUser();
		this.mailPassword = config.getMailPassword();
	}
	
	public String getPreviewDirectory() {
		return previewDirectory;
	}
	
	public String getVoc2rdfDirectory() {
		return voc2rdfDirectory;
	}
	
	public String getBioportalRestUrl() {
		return bioportalRestUrl;
	}
	
	public String getOntServiceUrl() {
		return ontServiceUrl;
	}
	
	public String getMailUser() {
		return mailUser;
	}
	
	public String getMailPassword() {
		return mailPassword;
	}
	
	public void setPreviewDirectory(String previewDirectory) {
		this.previewDirectory = previewDirectory;
	}
	
	public void setVoc2rdfDirectory(String voc2rdfDirectory) {
		this.voc2rdfDirectory = voc2rdfDirectory;
	}
	
	public void setBioportalRestUrl(String bioportalRestUrl) {
		this.bioportalRestUrl = bioportalRestUrl;
	}
	
	public void setOntServiceUrl(String ontServiceUrl) {
		this.ontServiceUrl = ontServiceUrl;
	}
	
	public void setMailUser(String mailUser) {
		this.mailUser = mailUser;
	}
	
	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}
}
