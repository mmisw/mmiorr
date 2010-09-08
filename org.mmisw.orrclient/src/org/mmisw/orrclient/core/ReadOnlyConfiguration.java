package org.mmisw.orrclient.core;

import org.mmisw.orrclient.OrrClientConfiguration;

/**
 * A read-only configuration.
 * The setXXX methods will throw UnsupportedOperationException
 * @author Carlos Rueda
 */
class ReadOnlyConfiguration extends OrrClientConfiguration {

	/**
	 * Makes a read-only copy of the given configuration.
	 */
	ReadOnlyConfiguration(OrrClientConfiguration config) {
		super(config);
	}
	
	public void setPreviewDirectory(String previewDirectory) {
		throw new UnsupportedOperationException();
	}
	
	public void setVoc2rdfDirectory(String voc2rdfDirectory) {
		throw new UnsupportedOperationException();
	}
	
	public void setOntServiceUrl(String ontServiceUrl) {
		throw new UnsupportedOperationException();
	}
	
	public void setMailUser(String mailUser) {
		throw new UnsupportedOperationException();
	}
	
	public void setMailPassword(String mailPassword) {
		throw new UnsupportedOperationException();
	}
}
