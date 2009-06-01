package org.mmisw.iserver.gwt.client.rpc;


/**
 * Info about the result of creating a vocabulary
 * 
 * @author Carlos Rueda
 */
public class CreateOntologyResult extends BaseResult {
	private static final long serialVersionUID = 1L;
	
	private BasicOntologyInfo basicOntologyInfo;
	private CreateOntologyInfo createOntologyInfo;
	
	private String uri = null;
	private String fullPath;
	

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
	 * @return the basicOntologyInfo
	 */
	public BasicOntologyInfo getBasicOntologyInfo() {
		return basicOntologyInfo;
	}



	/**
	 * @param basicOntologyInfo the basicOntologyInfo to set
	 */
	public void setBasicOntologyInfo(BasicOntologyInfo basicOntologyInfo) {
		this.basicOntologyInfo = basicOntologyInfo;
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

	
}
