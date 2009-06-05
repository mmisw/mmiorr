package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;

/**
 * Implement this interface to be notified whenever a TempOntologyInfo object has been
 * obtained. This is mainly to update the metadata in an OntologyPanel upload the
 * upload of a local file. It could also be used in the case of obtaining a remote
 * ontology. 
 * 
 * @see UploadLocalOntologyPanel
 * 
 * @author Carlos Rueda
 */
public interface TempOntologyInfoListener {
	
	/** 
	 * Called when a new TempOntologyInfo has been otained,.
	 * 
	 * @param tempOntologyInfo
	 */
	public void tempOntologyInfoObtained(TempOntologyInfo tempOntologyInfo);

}
