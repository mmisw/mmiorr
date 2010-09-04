package org.mmisw.iserver.gwt.client.rpc;


/**
 * Info about the result of creating a vocabulary, which means preparing the ontology
 * in the working space for subsequent registration.
 * 
 * @author Carlos Rueda
 */
public class CreateOntologyResult extends BaseResult {
	private static final long serialVersionUID = 1L;
	
	private CreateOntologyInfo createOntologyInfo;
	
	private String uri = null;
	private String fullPath;
	
	private boolean preserveOriginalBaseNamespace;
	

	public CreateOntologyResult() {
	}

	
	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}


	public String toString() {
		return "CreateVocabularyResult{uri=" +uri+ ", fullPath=" +fullPath+ ", error=" +error+ "}";
	}


	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}



	/**
	 * @return the createOntologyInfo
	 */
	public CreateOntologyInfo getCreateOntologyInfo() {
		return createOntologyInfo;
	}


	/**
	 * @param createOntologyInfo the createOntologyInfo to set
	 */
	public void setCreateOntologyInfo(CreateOntologyInfo createOntologyInfo) {
		this.createOntologyInfo = createOntologyInfo;
	}


	/**
	 * @return the preserveOriginalBaseNamespace
	 */
	public boolean isPreserveOriginalBaseNamespace() {
		return preserveOriginalBaseNamespace;
	}


	/**
	 * @param preserveOriginalBaseNamespace the preserveOriginalBaseNamespace to set
	 */
	public void setPreserveOriginalBaseNamespace(
			boolean preserveOriginalBaseNamespace) {
		this.preserveOriginalBaseNamespace = preserveOriginalBaseNamespace;
	}

	
}
