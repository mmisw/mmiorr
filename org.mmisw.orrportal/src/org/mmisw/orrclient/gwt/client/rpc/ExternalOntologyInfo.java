package org.mmisw.orrclient.gwt.client.rpc;


/**
 * Info about an external ontology.
 * 
 * @author Carlos Rueda
 */
public class ExternalOntologyInfo extends BaseOntologyInfo {
	private static final long serialVersionUID = 1L;

	public ExternalOntologyInfo() {
		setType(OntologyType.OTHER);
	}

	public void setType(OntologyType type) {
		if ( OntologyType.OTHER != type) {
			throw new IllegalArgumentException();
		}
		super.setType(type);
	}
}
