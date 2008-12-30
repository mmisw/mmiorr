package org.mmisw.vine.gwt.client.rpc;

import java.io.Serializable;

/**
 * Basic info about a registered ontology.
 * 
 * @author Carlos Rueda
 */
public class OntologyInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	
	private String uri;
	private String displayLabel;

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
}
