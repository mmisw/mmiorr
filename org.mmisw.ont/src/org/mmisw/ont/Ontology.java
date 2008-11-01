package org.mmisw.ont;

/**
 * Some general info about an ontology.
 * @author Carlos Rueda
 */
class Ontology {
	
	String id;
	String ontology_id;
	String file_path;
	
	String filename;
	
	private String uri;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
