package org.mmisw.vine.gwt.client.rpc;

import java.io.Serializable;

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
	
	
	private String localName;
	private String displayLabel;
	private String comment;
	
	
	public char getCode() {
		return code;
	}
	public void setCode(char code) {
		this.code = code;
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

}
