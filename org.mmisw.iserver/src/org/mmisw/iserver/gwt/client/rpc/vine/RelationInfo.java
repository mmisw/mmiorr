package org.mmisw.iserver.gwt.client.rpc.vine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	
	// secondary URIs
	private List<String> uris2;

	/** no-arg ctor required for the serialization */
	RelationInfo() {
	}
	
	
	/**
	 * @param iconUri
	 * @param shortName
	 * @param description
	 * @param uris First element will be primary URI with the rest being secondary URIs.
	 */
	public RelationInfo(String iconUri, String shortName, String description, String... uris) {
		super();
		this.iconUri = iconUri;
		this.shortName = shortName;
		this.description = description;
		if ( uris.length > 0 ) {
			this.uri = uris[0];
			for ( int i = 1; i < uris.length; i++ ) {
				addSecondaryUri(uris[i]);
			}
		}
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

	public List<String> getSecondaryUris() {
		return uris2;
	}
	
	public void addSecondaryUri(String uri2) {
		if ( uris2 == null ) {
			uris2 = new ArrayList<String>();
		}
		uris2.add(uri2);
	}

	public String toString() {
		return uri;
	}
}
