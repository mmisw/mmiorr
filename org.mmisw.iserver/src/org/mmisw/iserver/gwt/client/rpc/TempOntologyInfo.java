package org.mmisw.iserver.gwt.client.rpc;


/**
 * Info about a "temporary" ontology. This means, an ontology that has been loaded
 * into the working space (supported by a saved file in the back-end), which will
 * be eventually submitted for registration.
 * 
 * @author Carlos Rueda
 */
public class TempOntologyInfo extends BaseOntologyInfo {
	private static final long serialVersionUID = 1L;

	/** location of the ontology file on the server */
	private String fullPath;
	
	/** string with the rdf contents */
	private String rdf;
	
	/** message to describe details about the metadata contents of the ontology */
	private String details;
	
	/** the value of the associated xml:base */
	private String xmlBase;
	
	// to be moved out from here
	private boolean preserveOriginalBaseNamespace;
	
	
	public TempOntologyInfo() {
	}
	
	
	/** @returns The full path of the ontology file on the server */
	public String getFullPath() {
		return fullPath;
	}

	/** sets the full path of the ontology file on the server */
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}


	public String getRdf() {
		return rdf;
	}

	public void setRdf(String rdf) {
		this.rdf = rdf;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getDetails() {
		return details;
	}


	/**
	 * @return the preserveOriginalBaseNamespace
	 */
	@Deprecated
	public boolean isPreserveOriginalBaseNamespace() {
		return preserveOriginalBaseNamespace;
	}


	/**
	 * @param preserveOriginalBaseNamespace the preserveOriginalBaseNamespace to set
	 */
	@Deprecated
	public void setPreserveOriginalBaseNamespace(
			boolean preserveOriginalBaseNamespace) {
		this.preserveOriginalBaseNamespace = preserveOriginalBaseNamespace;
	}


	public String getXmlBase() {
		return xmlBase;
	}


	public void setXmlBase(String xmlBase) {
		this.xmlBase = xmlBase;
	}

}
