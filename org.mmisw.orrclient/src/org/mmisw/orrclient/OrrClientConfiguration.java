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
