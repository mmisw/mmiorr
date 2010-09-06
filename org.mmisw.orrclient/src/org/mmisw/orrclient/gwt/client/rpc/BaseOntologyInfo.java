package org.mmisw.orrclient.gwt.client.rpc;

import java.io.Serializable;


/**
 * The base class for information about an ontology, either registered or not (eg.,
 * obtained from an external source).
 * 
 * @author Carlos Rueda
 */
public abstract class BaseOntologyInfo extends Errorable implements Serializable {
	private static final long serialVersionUID = 1L;

	private String displayLabel;
	
	private String uri;
	
	private OntologyMetadata ontologyMetadata;
	
	private OntologyData ontologyData;

	private OntologyType type;

	
	public BaseOntologyInfo() {
	}
	
	public String getDisplayLabel() {
		return displayLabel;
	}
	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public boolean equals(Object other) {
		return other instanceof BaseOntologyInfo && uri.equals(((BaseOntologyInfo) other).uri);
	}
	public int hashCode() {
		return uri.hashCode();
	}

	/**
	 * @return the type
	 */
	public OntologyType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(OntologyType type) {
		this.type = type;
	}

	public OntologyMetadata getOntologyMetadata() {
		if ( ontologyMetadata == null ) {
			ontologyMetadata = new OntologyMetadata();
		}
		return ontologyMetadata;
	}
	
	public void setOntologyData(OntologyData ontologyData) {
		this.ontologyData = ontologyData;
	}

	/**
	 * @return the ontologyData. null if setOntologyData hasn't been called with a non-null arg
	 */
	public OntologyData getOntologyData() {
		return ontologyData;
	}
}
