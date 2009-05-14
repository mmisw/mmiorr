package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Info about an entity.
 * 
 * TODO include properties associated with the entity.
 * 
 * @author Carlos Rueda
 */
public abstract class EntityInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	// used only on the client side
	private transient char code;
	
	private String uri;
	private String localName;
	private String displayLabel;
	private String comment;
	
	private List<PropValue> props;
	
	
	public char getCode() {
		return code;
	}
	public void setCode(char code) {
		this.code = code;
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

}
