package org.mmisw.ont.client;


/**
 * A read-only configuration.
 * The setXXX methods will throw UnsupportedOperationException
 * @author Carlos Rueda
 */
class OntReadOnlyConfiguration extends OntClientConfiguration {

	/**
	 * Makes a read-only copy of the given configuration.
	 */
	OntReadOnlyConfiguration(OntClientConfiguration config) {
		super(config);
	}
	
	public void setOntServiceUrl(String ontServiceUrl) {
		throw new UnsupportedOperationException();
	}
}
