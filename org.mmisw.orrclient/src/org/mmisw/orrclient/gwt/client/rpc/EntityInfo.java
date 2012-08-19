package org.mmisw.orrclient.gwt.client.rpc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Info about an entity.
 * 
 * This class can be instantiated directly for generic info about an entity,
 * but there a few subclasses for particular types of entities.
 * 
 * @author Carlos Rueda
 */
public class EntityInfo implements Serializable, Comparable<EntityInfo> {
	private static final long serialVersionUID = 1L;

	private String uri;
	private String localName;
	private String displayLabel;
	private String comment;
	
	private List<PropValue> props;
	
	
	public EntityInfo() {
		
	}
	
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public int hashCode() {
		return uri.hashCode();
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof EntityInfo)) {
			return false;
		}
		return this.uri.equals(((EntityInfo) other).uri);
	}
	
	public int compareTo(EntityInfo o) {
		return this.uri.compareTo(o.uri);
	}

	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	public String getDisplayLabel() {
		return displayLabel;
	}
	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public List<PropValue> getProps() {
		if ( props == null ) {
			props = new ArrayList<PropValue>();
		}
		return props;
		
	}
	
	public String toString() {
		return getClass().getName()+ ": " +uri;
	}

}
