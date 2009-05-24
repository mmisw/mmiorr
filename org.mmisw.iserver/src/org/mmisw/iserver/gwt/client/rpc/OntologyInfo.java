package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;
import java.util.List;

/**
 * Info about a registered ontology.
 * 
 * @author Carlos Rueda
 */
public class OntologyInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String error = null;
	
	
	// used only on the client side
	private transient char code;
	
	private String uri;
	private String displayLabel;
	
	
	private OntologyMetadata ontologyMetadata;
	
	/**
	 * @return the ontologyMetadata
	 */
	public OntologyMetadata getOntologyMetadata() {
		if ( ontologyMetadata == null ) {
			ontologyMetadata = new OntologyMetadata();
		}
		return ontologyMetadata;
	}

	private List<EntityInfo> entities;
	
	
	private String authority;
	private String type;

	private String userId;
	private String username;

	private String contactName;

	private String versionNumber;

	private String dateCreated;

	
	public char getCode() {
		return code;
	}
	public void setCode(char code) {
		this.code = code;
	}
	
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}


	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getDisplayLabel() {
		return displayLabel;
	}
	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}
	public List<EntityInfo> getEntities() {
		return entities;
	}
	public void setEntities(List<EntityInfo> entities) {
		this.entities = entities;
	}
	
	
	public boolean equals(Object other) {
		return other instanceof OntologyInfo && uri.equals(((OntologyInfo) other).uri);
	}
	public int hashCode() {
		return uri.hashCode();
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @return the contactName
	 */
	public String getContactName() {
		return contactName;
	}
	/**
	 * @return the versionNumber
	 */
	public String getVersionNumber() {
		return versionNumber;
	}
	/**
	 * @return the dateCreated
	 */
	public String getDateCreated() {
		return dateCreated;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	
}
