package org.mmisw.iserver.gwt.client.rpc.vine;

import java.io.Serializable;

/**
 * Info about a relation.
 * 
 * @author Carlos Rueda
 */
public class RelationInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String uri;
	private String iconUri;
	private String shortName;
	private String description;
	

	/** no-arg ctor required for the serialization */
	RelationInfo() {
	}
	
	
	/**
	 * @param iconUri
	 * @param shortName
	 * @param description
	 * @param uri The associated URI.
	 */
	public RelationInfo(String iconUri, String shortName, String description, String uri) {
		super();
		this.iconUri = iconUri;
		this.shortName = shortName;
		this.description = description;
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getIconUri() {
		return iconUri;
	}
	public void setIconUri(String uri) {
		this.iconUri = uri;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return uri;
	}
}
