package org.mmisw.vine.gwt.client.rpc;

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
	
	
	public RelationInfo(String uri, String iconUri, String shortName, String description) {
		super();
		this.uri = uri;
		this.iconUri = iconUri;
		this.shortName = shortName;
		this.description = description;
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
