package org.mmisw.vine.gwt.client.rpc;

import java.io.Serializable;
import java.util.List;

/**
 * Info about a registered ontology.
 * 
 * @author Carlos Rueda
 */
public class OntologyInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	
	private String uri;
	private String displayLabel;
	
	private List<EntityInfo> entities;
	

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
	
}
