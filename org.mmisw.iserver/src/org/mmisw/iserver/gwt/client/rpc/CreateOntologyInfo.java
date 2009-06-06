package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author Carlos Rueda
 */
public class CreateOntologyInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String authority;
	private String shortName;
	
	private Map<String,String> metadataValues;
	
	private DataCreationInfo dataCreationInfo;
	
	/** aquaportal ontology ID used, if not null, to create a new version */
	private String ontologyId = null;
	private String ontologyUserId = null;
	

	/**
	 * ctor.
	 */
	public CreateOntologyInfo() {
		
	}
	
	
	/**
	 * @return the authority
	 */
	public String getAuthority() {
		return authority;
	}


	/**
	 * @param authority the authority to set
	 */
	public void setAuthority(String authority) {
		this.authority = authority;
	}


	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}


	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return the metadataValues
	 */
	public Map<String, String> getMetadataValues() {
		return metadataValues;
	}

	/**
	 * @param metadataValues the metadataValues to set
	 */
	public void setMetadataValues(Map<String, String> metadataValues) {
		this.metadataValues = metadataValues;
	}


	/**
	 * @return the dataCreationInfo
	 */
	public DataCreationInfo getDataCreationInfo() {
		return dataCreationInfo;
	}


	/**
	 * @param dataCreationInfo the dataCreationInfo to set
	 */
	public void setDataCreationInfo(DataCreationInfo dataCreationInfo) {
		this.dataCreationInfo = dataCreationInfo;
	}


	/**
	 * Gets the ID of the ontology base for the creation of a new version.
	 * @return the ontologyId. null for the case of a brand new ontology.
	 */
	public String getOntologyId() {
		return ontologyId;
	}


	/**
	 * Sets the ID of the ontology base for the creation of a new version.
	 * @param ontologyId the ontologyId to set
	 */
	public void setOntologyId(String ontologyId) {
		this.ontologyId = ontologyId;
	}


	/**
	 * Gets the ID of user associated with the ontology base for the creation of a new version.
	 * @return the ontologyUserId
	 */
	public String getOntologyUserId() {
		return ontologyUserId;
	}


	/**
	 * Sets the ID of user associated with the ontology base for the creation of a new version.
	 * @param ontologyUserId the ontologyUserId to set
	 */
	public void setOntologyUserId(String ontologyUserId) {
		this.ontologyUserId = ontologyUserId;
	}

	
}
